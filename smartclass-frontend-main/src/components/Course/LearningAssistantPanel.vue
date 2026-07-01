<template>
  <div class="learning-assistant-panel">
    <div class="panel-header">
      <div>
        <div class="panel-title">学习助手</div>
        <div class="panel-subtitle">
          <template v-if="sectionId">
            当前上下文：课程 {{ courseId }} / 章节 {{ chapterId || '-' }} / 小节 {{ sectionId }}
          </template>
          <template v-else>
            请选择当前学习小节后再开始提问
          </template>
        </div>
      </div>
      <van-tag v-if="currentSessionId" type="primary" plain>会话已缓存</van-tag>
    </div>

    <div ref="messageContainerRef" class="message-container">
      <van-empty
        v-if="messages.length === 0"
        description="可以围绕当前课程内容直接提问"
        image-size="72"
      />

      <div
        v-for="message in messages"
        :key="message.id"
        class="message-item"
        :class="message.role"
      >
        <div class="message-bubble">
          <template v-if="message.role === 'assistant'">
            <details
              v-if="getAssistantMessageParts(message.content).thinkingContent"
              class="thinking-block"
              :open="!getAssistantMessageParts(message.content).answerContent"
            >
              <summary>
                {{ getAssistantMessageParts(message.content).answerContent ? '查看思考过程' : '正在思考...' }}
              </summary>
              <div class="thinking-content">
                {{ getAssistantMessageParts(message.content).thinkingContent }}
              </div>
            </details>

            <div
              v-if="getAssistantMessageParts(message.content).answerContent"
              class="answer-content"
            >
              {{ getAssistantMessageParts(message.content).answerContent }}
            </div>

            <div
              v-else-if="getAssistantMessageParts(message.content).thinkingContent"
              class="answer-placeholder"
            >
              正在整理最终回答...
            </div>
          </template>
          <template v-else>
            {{ message.content }}
          </template>
        </div>
      </div>

      <div v-if="isStreaming" class="streaming-status">正在生成回答...</div>
    </div>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <div class="input-area">
      <van-field
        v-model="question"
        type="textarea"
        rows="2"
        autosize
        maxlength="500"
        placeholder="输入你对当前小节的疑问"
        :disabled="isStreaming || !sectionId"
        show-word-limit
        @keydown.enter.exact.prevent="handleSubmit"
      />

      <div class="action-row">
        <van-button
          size="small"
          plain
          :disabled="isStreaming"
          @click="clearCurrentContextSession"
        >
          清空会话
        </van-button>
        <van-button
          v-if="isStreaming"
          size="small"
          plain
          type="warning"
          @click="stopStreaming"
        >
          停止
        </van-button>
        <van-button
          v-else
          size="small"
          type="primary"
          :disabled="!canSend"
          @click="handleSubmit"
        >
          发送
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue';
import { showToast } from 'vant';
import {
  askCourseLearningAi,
  cacheCourseLearningAiSessionId,
  clearCourseLearningAiSessionId,
  type CourseLearningAiRequest,
  getCachedCourseLearningAiSessionId,
  streamCourseLearningAi,
} from '@/api/courseLearningAi';

interface ChatMessageItem {
  id: number;
  role: 'user' | 'assistant';
  content: string;
}

const props = withDefaults(
  defineProps<{
    courseId: number;
    chapterId?: number;
    sectionId?: number;
    aiAvatarId?: number;
  }>(),
  {
    chapterId: undefined,
    sectionId: undefined,
    aiAvatarId: 1,
  },
);

const question = ref('');
const messages = ref<ChatMessageItem[]>([]);
const isStreaming = ref(false);
const errorMessage = ref('');
const currentSessionId = ref('');
const messageContainerRef = ref<HTMLElement | null>(null);
let currentStreamController: AbortController | null = null;

const THINK_OPEN_TAG = '<think>';
const THINK_CLOSE_TAG = '</think>';

const canSend = computed(() => {
  return Boolean(question.value.trim() && props.courseId && props.sectionId && !isStreaming.value);
});

const contextKeyParams = computed(() => ({
  courseId: props.courseId,
  chapterId: props.chapterId,
  sectionId: props.sectionId,
  aiAvatarId: props.aiAvatarId,
}));

const normalizeMessageBlock = (value: string) => value.replace(/\n{3,}/g, '\n\n').trim();

const getAssistantMessageParts = (content: string) => {
  if (!content.includes(THINK_OPEN_TAG)) {
    return {
      thinkingContent: '',
      answerContent: content,
    };
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

const scrollToBottom = async () => {
  await nextTick();
  if (!messageContainerRef.value) {
    return;
  }
  messageContainerRef.value.scrollTop = messageContainerRef.value.scrollHeight;
};

const stopStreaming = () => {
  if (currentStreamController) {
    currentStreamController.abort();
    currentStreamController = null;
  }
  isStreaming.value = false;
};

const syncSessionFromCache = () => {
  stopStreaming();

  if (!props.sectionId) {
    currentSessionId.value = '';
    messages.value = [];
    errorMessage.value = '';
    return;
  }

  currentSessionId.value = getCachedCourseLearningAiSessionId(contextKeyParams.value);
  messages.value = [];
  errorMessage.value = '';
};

watch(
  () => [props.courseId, props.chapterId, props.sectionId, props.aiAvatarId],
  () => {
    syncSessionFromCache();
  },
  { immediate: true },
);

const updateSessionId = (sessionId?: string) => {
  if (!sessionId) {
    return;
  }
  currentSessionId.value = sessionId;
  cacheCourseLearningAiSessionId(contextKeyParams.value, sessionId);
};

const buildRequestPayload = (): CourseLearningAiRequest => {
  return {
    aiAvatarId: props.aiAvatarId,
    sessionId: currentSessionId.value || undefined,
    courseId: props.courseId,
    chapterId: props.chapterId,
    sectionId: props.sectionId,
    question: question.value.trim(),
    endChat: false,
  };
};

const replaceAssistantMessage = (messageId: number, content: string) => {
  const target = messages.value.find((item) => item.id === messageId);
  if (target) {
    target.content = content;
  }
};

const appendAssistantMessageChunk = (messageId: number, chunk: string) => {
  const target = messages.value.find((item) => item.id === messageId);
  if (target) {
    target.content += chunk;
  }
};

const sendByFallback = async (payload: CourseLearningAiRequest, messageId: number) => {
  const response = await askCourseLearningAi(payload);
  updateSessionId(response.sessionId);
  replaceAssistantMessage(messageId, response.content || '学习助手暂时没有返回内容');
};

const handleSubmit = async () => {
  if (!canSend.value) {
    if (!props.sectionId) {
      showToast('请先选择当前学习小节');
    }
    return;
  }

  const userQuestion = question.value.trim();
  const requestPayload = buildRequestPayload();
  const userMessageId = Date.now();
  const assistantMessageId = userMessageId + 1;
  let receivedChunk = false;

  errorMessage.value = '';
  messages.value.push({
    id: userMessageId,
    role: 'user',
    content: userQuestion,
  });
  messages.value.push({
    id: assistantMessageId,
    role: 'assistant',
    content: '',
  });

  question.value = '';
  isStreaming.value = true;
  currentStreamController = new AbortController();
  await scrollToBottom();

  try {
    await streamCourseLearningAi(
      requestPayload,
      {
        onMessage: ({ chunk, sessionId }) => {
          receivedChunk = true;
          updateSessionId(sessionId);
          appendAssistantMessageChunk(assistantMessageId, chunk);
          void scrollToBottom();
        },
        onComplete: ({ sessionId, raw }) => {
          updateSessionId(sessionId);
          if (!receivedChunk) {
            const completeMessage =
              raw && typeof raw === 'object' && typeof (raw as Record<string, unknown>).message === 'string'
                ? String((raw as Record<string, unknown>).message)
                : '回答完成';
            replaceAssistantMessage(assistantMessageId, completeMessage);
          }
        },
        onError: ({ message }) => {
          errorMessage.value = message;
        },
      },
      {
        signal: currentStreamController.signal,
      },
    );
  } catch (error) {
    const isAbortError = error instanceof DOMException && error.name === 'AbortError';
    const isAuthExpired = error instanceof Error && error.message.includes('登录已过期');

    if (isAbortError) {
      if (!receivedChunk) {
        replaceAssistantMessage(assistantMessageId, '已停止本次回答');
      }
      return;
    }

    if (!receivedChunk && !isAuthExpired) {
      try {
        await sendByFallback(requestPayload, assistantMessageId);
        errorMessage.value = '';
      } catch (fallbackError) {
        const finalMessage =
          fallbackError instanceof Error
            ? fallbackError.message
            : error instanceof Error
              ? error.message
              : '学习助手请求失败';
        errorMessage.value = finalMessage;
        replaceAssistantMessage(assistantMessageId, '抱歉，本次提问失败，请稍后重试。');
      }
    } else if (isAuthExpired) {
      errorMessage.value = '登录已过期，请重新登录';
    } else {
      errorMessage.value = error instanceof Error ? error.message : '流式回答中断';
    }
  } finally {
    currentStreamController = null;
    isStreaming.value = false;
    await scrollToBottom();
  }
};

const clearCurrentContextSession = () => {
  stopStreaming();
  clearCourseLearningAiSessionId(contextKeyParams.value);
  currentSessionId.value = '';
  messages.value = [];
  errorMessage.value = '';
};

onBeforeUnmount(() => {
  stopStreaming();
});
</script>

<style scoped>
.learning-assistant-panel {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #edf0f4;
  padding: 12px;
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.panel-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
}

.message-container {
  max-height: 260px;
  min-height: 180px;
  overflow-y: auto;
  border: 1px solid #edf0f4;
  border-radius: 12px;
  padding: 10px;
  background: #f9fbff;
}

.message-item {
  display: flex;
  margin-bottom: 10px;
}

.message-item:last-child {
  margin-bottom: 0;
}

.message-item.user {
  justify-content: flex-end;
}

.message-item.assistant {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 85%;
  padding: 10px 12px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-item.user .message-bubble {
  background: #1989fa;
  color: #fff;
}

.message-item.assistant .message-bubble {
  background: #fff;
  color: #1f2937;
  border: 1px solid #e5e7eb;
}

.thinking-block {
  margin-bottom: 8px;
  border: 1px dashed #cbd5e1;
  border-radius: 10px;
  background: #f8fafc;
}

.thinking-block summary {
  cursor: pointer;
  padding: 8px 10px;
  font-size: 12px;
  color: #64748b;
  user-select: none;
}

.thinking-content {
  padding: 0 10px 10px;
  font-size: 12px;
  line-height: 1.6;
  color: #475569;
  white-space: pre-wrap;
}

.answer-content {
  white-space: pre-wrap;
}

.answer-placeholder {
  font-size: 12px;
  color: #64748b;
}

.streaming-status {
  margin-top: 8px;
  font-size: 12px;
  color: #1989fa;
}

.error-message {
  margin-top: 10px;
  border-radius: 8px;
  padding: 8px 10px;
  background: #fff1f0;
  color: #cf1322;
  font-size: 12px;
}

.input-area {
  margin-top: 12px;
}

.action-row {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
