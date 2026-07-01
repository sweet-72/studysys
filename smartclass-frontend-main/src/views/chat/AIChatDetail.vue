<template>
  <div class="chat-detail">
    <chat-header :title="assistant.name || 'AI助手'" />

    <div class="message-container">
      <message-list
        :messages="messages"
        :assistant-avatar="assistant.avatar"
        :assistant-name="assistant.name || 'AI助手'"
        :user-avatar="userInfo?.avatar || ''"
        :loading="isAITyping"
        :custom-format-message="formatMessage"
        @regenerate="handleRegenerateResponse"
      />
    </div>

    <stop-response-button :show="isAITyping" @stop="stopStreamingResponse" />

    <chat-input-area
      v-model="inputMessage"
      :is-loading="isAITyping"
      @send="sendMessage"
      @emoji="showEmojiPicker = true"
      @image="uploadImage"
      @voice="startVoiceRecord"
      @fullscreen="showFullscreenInput = true"
    />

    <emoji-picker v-model:show="showEmojiPicker" @select="selectEmoji" />

    <fullscreen-input
      v-model:show="showFullscreenInput"
      v-model="inputMessage"
      :is-loading="isAITyping"
      @send="sendFullscreenMessage"
      @emoji="showEmojiPicker = true"
      @image="uploadImage"
      @voice="startVoiceRecord"
    />
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { showLoadingToast, showToast } from 'vant';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import katex from 'katex';
import 'katex/dist/katex.min.css';
import {
  MessageList,
  ChatHeader,
  EmojiPicker,
  FullscreenInput,
  StopResponseButton,
  ChatInputArea,
  useChatMessages,
} from '../../components/Dialogue';
import { useUserStore } from '../../stores/userStore';
import { AiAvatarChatControllerService } from '../../services';
import type { Assistant, UserInfo } from '../../components/Dialogue/ChatMessageHandler';
import { queryAiAvatarDetail, resolveAiAvatarImage } from '../../api/aiAvatar';
import { recognizeSpeech } from '../../api/speech';

// 本地格式化函数，支持 Markdown 和 KaTeX
const formatMarkdownWithKatex = (content: string): string => {
  const blockRendered = content.replace(/\$\$([\s\S]+?)\$\$/g, (_m, formula) => {
    try {
      return `<div class="katex-block">${katex.renderToString(formula.trim(), {
        displayMode: true,
        throwOnError: false,
      })}</div>`;
    } catch {
      return String(_m);
    }
  });

  const inlineRendered = blockRendered.replace(/\$([^$\n]+?)\$/g, (match, formula) => {
    if (/^\$\d/.test(match) || /\d\$$/.test(match)) {
      return match;
    }

    try {
      return katex.renderToString(formula.trim(), {
        displayMode: false,
        throwOnError: false,
      });
    } catch {
      return match;
    }
  });

  const html = marked.parse(inlineRendered, { async: false });
  return DOMPurify.sanitize(html, {
    ADD_TAGS: [
      'math',
      'mrow',
      'mi',
      'mo',
      'mn',
      'msup',
      'msub',
      'mfrac',
      'mspace',
      'mtext',
      'annotation',
      'semantics',
      'svg',
      'line',
      'path',
      'g',
    ],
    ADD_ATTR: [
      'xlink:href',
      'href',
      'fill',
      'stroke',
      'stroke-width',
      'stroke-linecap',
      'stroke-linejoin',
      'd',
      'width',
      'height',
      'viewBox',
      'style',
      'class',
    ],
    ALLOW_DATA_ATTR: true,
  });
};

const route = useRoute();
const userStore = useUserStore();
const inputMessage = ref('');
const showEmojiPicker = ref(false);
const showFullscreenInput = ref(false);
const isRecording = ref(false);
const mediaRecorder = ref<MediaRecorder | null>(null);
const recordedChunks = ref<Blob[]>([]);
const recordingStream = ref<MediaStream | null>(null);

const userInfo = ref<UserInfo>({
  id: 0,
  name: '',
  avatar: '',
});

const assistant = ref<Assistant>({
  id: Number(route.params.assistantId) || 0,
  name: '',
  avatar: '',
  description: '',
});

// 思考标签常量
const THINK_OPEN_TAG = '<think>';
const THINK_CLOSE_TAG = '</think>';

// 规范化消息块，合并多余换行
const normalizeMessageBlock = (value: string) => value.replace(/\n{3,}/g, '\n\n').trim();

// 解析 AI 消息，分离思考内容和回答内容（支持多个思考块）
const getAssistantMessageParts = (content: string) => {
  if (!content.includes(THINK_OPEN_TAG)) {
    return { thinkingContent: '', answerContent: content };
  }

  const thinkingBlocks: string[] = [];
  const answerBlocks: string[] = [];
  let cursor = 0;

  while (cursor < content.length) {
    const openIndex = content.indexOf(THINK_OPEN_TAG, cursor);

    if (openIndex === -1) {
      answerBlocks.push(content.slice(cursor));
      break;
    }

    if (openIndex > cursor) {
      answerBlocks.push(content.slice(cursor, openIndex));
    }

    const thinkStart = openIndex + THINK_OPEN_TAG.length;
    const closeIndex = content.indexOf(THINK_CLOSE_TAG, thinkStart);

    if (closeIndex === -1) {
      thinkingBlocks.push(content.slice(thinkStart));
      cursor = content.length;
      break;
    }

    thinkingBlocks.push(content.slice(thinkStart, closeIndex));
    cursor = closeIndex + THINK_CLOSE_TAG.length;
  }

  return {
    thinkingContent: normalizeMessageBlock(thinkingBlocks.join('\n\n')),
    answerContent: normalizeMessageBlock(answerBlocks.join('')),
  };
};

const {
  messages,
  isAITyping,
  sessionId,
  addWelcomeMessage,
  stopStreamingResponse,
  sendMessage,
  regenerateResponse,
} = useChatMessages(assistant.value);

// 自定义格式化消息内容，处理 <think> 标签
const formatMessage = (content: string): string => {
  const { thinkingContent, answerContent } = getAssistantMessageParts(content);
  
  let html = '';
  
  // 如果有思考内容，添加折叠的思考块
  if (thinkingContent) {
    const hasAnswer = answerContent.length > 0;
    html += `<details class="thinking-block" ${!hasAnswer ? 'open' : ''}>`;
    html += `<summary class="thinking-summary">${hasAnswer ? '查看思考过程' : '正在思考...'}</summary>`;
    html += `<div class="thinking-content">`;
    html += formatMarkdownWithKatex(thinkingContent);
    html += `</div>`;
    html += `</details>`;
  }
  
  // 如果有回答内容，添加回答
  if (answerContent) {
    html += `<div class="answer-content">`;
    html += formatMarkdownWithKatex(answerContent);
    html += `</div>`;
  } else if (thinkingContent) {
    // 有思考但还没有回答
    html += `<div class="answer-placeholder">正在整理最终回答...</div>`;
  }
  
  return html || content;
};

const formatMessageForDisplay = (content: string): string => {
  const result = formatMessage(content);
  return result;
};

const initializeChat = async () => {
  try {
    const aiAvatarId = Number(route.params.assistantId) || 1;
    const response = await AiAvatarChatControllerService.createSessionUsingPost(aiAvatarId);

    if (response.code === 0 && response.data) {
      sessionId.value = response.data;
    } else {
      showToast('创建会话失败');
    }
  } catch {
    showToast('创建会话失败');
  }
};

const loadAiAvatarInfo = async () => {
  try {
    const aiAvatarId = Number(route.params.assistantId) || 0;
    const detail = await queryAiAvatarDetail(aiAvatarId);

    assistant.value.id = detail.id || aiAvatarId;
    assistant.value.name = detail.name || '';
    assistant.value.avatar = resolveAiAvatarImage(detail.avatarImgUrl);
    assistant.value.description = detail.description || '';
    assistant.value.status = detail.status;

    if (messages.value.length > 0 && messages.value[0]?.type === 'ai') {
      messages.value[0].content = `你好，我是${assistant.value.name}。${
        assistant.value.description || '有什么我可以帮助你的吗？'
      }`;
    }
  } catch {
    showToast('加载智慧体详情失败');
  }
};

const loadChatHistory = async () => {
  if (!sessionId.value) return;

  try {
    const response = await AiAvatarChatControllerService.getChatHistoryUsingGet(sessionId.value);

    if (response.code === 0 && response.data) {
      messages.value = response.data.map((msg) => ({
        id: msg.id || Date.now(),
        type: msg.messageType === 'user' ? 'user' : 'ai',
        content: msg.content || '',
        timestamp: msg.createTime ? new Date(msg.createTime).getTime() : Date.now(),
      }));
    } else {
      addWelcomeMessage();
    }
  } catch {
    addWelcomeMessage();
  }
};

const sendFullscreenMessage = () => {
  if (inputMessage.value.trim()) {
    sendMessage(inputMessage.value);
    showFullscreenInput.value = false;
  }
};

const selectEmoji = (emoji: string): void => {
  inputMessage.value += emoji;
  showEmojiPicker.value = false;
};

const uploadImage = (): void => {
  showToast('图片上传功能开发中');
};

const getSupportedAudioMimeType = () => {
  const candidates = ['audio/webm;codecs=opus', 'audio/webm', 'audio/mp4'];
  return candidates.find((type) => MediaRecorder.isTypeSupported(type)) || '';
};

const cleanupRecording = () => {
  recordingStream.value?.getTracks().forEach((track) => track.stop());
  recordingStream.value = null;
  mediaRecorder.value = null;
  recordedChunks.value = [];
  isRecording.value = false;
};

const uploadRecordedSpeech = async (audioBlob: Blob) => {
  const loading = showLoadingToast({
    message: '正在识别语音...',
    forbidClick: true,
    duration: 0,
  });

  try {
    const result = await recognizeSpeech(audioBlob);
    const text = result.text?.trim();
    if (!text) {
      showToast('未识别到有效文字');
      return;
    }
    inputMessage.value = text;
    showToast('识别完成，请确认后发送');
  } catch (error) {
    showToast(error instanceof Error ? error.message : '语音识别失败');
  } finally {
    loading.close();
  }
};

const startVoiceRecord = async (): Promise<void> => {
  if (isRecording.value) {
    if (mediaRecorder.value && mediaRecorder.value.state !== 'inactive') {
      mediaRecorder.value.stop();
    }
    return;
  }

  if (!navigator.mediaDevices?.getUserMedia || typeof MediaRecorder === 'undefined') {
    showToast('当前浏览器不支持录音');
    return;
  }

  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    const mimeType = getSupportedAudioMimeType();
    const recorder = mimeType ? new MediaRecorder(stream, { mimeType }) : new MediaRecorder(stream);

    recordingStream.value = stream;
    mediaRecorder.value = recorder;
    recordedChunks.value = [];

    recorder.ondataavailable = (event) => {
      if (event.data && event.data.size > 0) {
        recordedChunks.value.push(event.data);
      }
    };

    recorder.onstop = () => {
      const chunks = [...recordedChunks.value];
      const type = recorder.mimeType || mimeType || 'audio/webm';
      cleanupRecording();
      if (chunks.length === 0) {
        showToast('没有录到音频');
        return;
      }
      uploadRecordedSpeech(new Blob(chunks, { type }));
    };

    recorder.start();
    isRecording.value = true;
    showToast('正在录音，再次点击语音按钮结束');
  } catch (error) {
    cleanupRecording();
    showToast(error instanceof DOMException && error.name === 'NotAllowedError'
      ? '请允许浏览器使用麦克风'
      : '录音启动失败');
  }
};

const handleRegenerateResponse = (messageId: number) => {
  regenerateResponse(messageId);
};

onMounted(async () => {
  if (!userStore.userInfo) {
    await userStore.fetchCurrentUser();
  }

  if (userStore.userInfo) {
    userInfo.value = {
      id: userStore.userInfo.id || 0,
      name: userStore.userInfo.userName || '',
      avatar: userStore.userInfo.userAvatar || userStore.DEFAULT_USER_AVATAR,
    };
  }

  const routeSessionId = route.query.sessionId as string | undefined;

  await loadAiAvatarInfo();

  if (routeSessionId) {
    sessionId.value = routeSessionId;
    await loadChatHistory();
  } else {
    await initializeChat();
    addWelcomeMessage();
  }
});

onBeforeUnmount(() => {
  cleanupRecording();
  if (sessionId.value) {
    stopStreamingResponse();
  }
});
</script>

<style scoped>
.chat-detail {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  position: relative;
  background:
    radial-gradient(circle at 20% 8%, rgba(108, 99, 255, 0.2), transparent 30%),
    radial-gradient(circle at 88% 18%, rgba(96, 165, 250, 0.16), transparent 28%),
    linear-gradient(135deg, #c9d6ff, #e2e2e2, #fbc2eb);
}

.chat-detail::before {
  position: absolute;
  inset: 0;
  pointer-events: none;
  content: '';
  background:
    radial-gradient(circle at 50% 36%, rgba(255, 255, 255, 0.28), transparent 32%),
    radial-gradient(circle at 12% 78%, rgba(108, 99, 255, 0.12), transparent 24%);
  filter: blur(10px);
}

.message-container {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  margin-top: 72px;
  margin-bottom: 142px;
  padding: 0;
  width: 100%;
  box-sizing: border-box;
}

:deep(.message-list) {
  flex: 1;
  padding: 18px 16px 34px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  overflow-y: auto;
}

:deep(.message-item.ai) {
  width: 100%;
  margin-right: 0;
}

:deep(.message-item.ai .message-content) {
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(20px);
}

/* 思考块样式 */
:deep(.thinking-block) {
  margin-bottom: 8px;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  background: #f8fafc;
}

:deep(.thinking-block[open]) {
  background: #f1f5f9;
}

:deep(.thinking-summary) {
  cursor: pointer;
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  user-select: none;
  list-style: none;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: background-color 0.2s;
}

:deep(.thinking-summary::-webkit-details-marker) {
  display: none;
}

:deep(.thinking-summary::before) {
  content: '💭';
  font-size: 14px;
}

:deep(.thinking-summary:hover) {
  background-color: #e2e8f0;
}

:deep(.thinking-content) {
  padding: 0 12px 12px;
  font-size: 13px;
  line-height: 1.6;
  color: #475569;
  white-space: pre-wrap;
  word-break: break-word;
  border-top: 1px solid #e2e8f0;
  margin-top: 4px;
}

:deep(.thinking-content p) {
  margin: 6px 0;
}

/* 回答内容样式 */
:deep(.answer-content) {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
}

:deep(.answer-content p) {
  margin: 6px 0;
}

/* 回答占位符样式 */
:deep(.answer-placeholder) {
  font-size: 13px;
  color: #94a3b8;
  font-style: italic;
  padding: 8px 0;
}

/* KaTeX 块级公式样式 */
:deep(.katex-block) {
  margin: 12px 0;
  overflow-x: auto;
  text-align: center;
}

/* Markdown 样式 */
:deep(.markdown-body) {
  font-size: 14px;
  line-height: 1.6;
}

:deep(.markdown-body p) {
  margin: 6px 0;
}

:deep(.markdown-body code) {
  background-color: rgba(0, 0, 0, 0.05);
  padding: 2px 4px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 13px;
}

:deep(.markdown-body pre) {
  background-color: #f1f5f9;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 8px 0;
}

:deep(.markdown-body pre code) {
  background: none;
  padding: 0;
}

:deep(.markdown-body ul),
:deep(.markdown-body ol) {
  padding-left: 20px;
  margin: 8px 0;
}

:deep(.markdown-body blockquote) {
  border-left: 3px solid #cbd5e1;
  padding-left: 12px;
  margin: 8px 0;
  color: #64748b;
}
</style>
