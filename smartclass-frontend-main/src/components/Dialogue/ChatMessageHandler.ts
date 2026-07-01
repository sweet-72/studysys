import { ref } from 'vue';
import { showToast } from 'vant';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import katex from 'katex';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { useUserStore } from '../../stores/userStore';
import { useRouter } from 'vue-router';
import { buildApiUrl } from '@/utils/api';

export interface UserInfo {
  id: number;
  name: string;
  avatar: string;
}

export interface Assistant {
  id: number;
  name: string;
  avatar: string;
  description: string;
  status?: number;
}

export interface Message {
  id: number;
  type: 'user' | 'ai';
  content: string;
  timestamp: number;
  isRead?: number;
}

interface LearningContext {
  recentCourseId?: number;
  recentCourseTitle?: string;
  sectionId?: number;
  progress?: number;
  wrongCount?: number;
  updatedAt?: string;
}

interface ChatMessageRequest {
  aiAvatarId: number;
  content: string;
  sessionId: string;
  messageType: string;
  regenerate?: boolean;
  learningContext?: LearningContext;
}

const getLearningContext = (): LearningContext | undefined => {
  const raw = localStorage.getItem('latest_learning_context');
  if (!raw) {
    return undefined;
  }

  try {
    return JSON.parse(raw) as LearningContext;
  } catch {
    return undefined;
  }
};

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

export function useChatMessages(assistant: Assistant) {
  const router = useRouter();
  const userStore = useUserStore();

  const messages = ref<Message[]>([]);
  const isAITyping = ref<boolean>(false);
  const currentAIMessageId = ref<number | null>(null);
  const sessionId = ref<string | undefined>(undefined);

  let currentStreamController: AbortController | null = null;

  const formatMessage = (content: string): string => {
    try {
      return formatMarkdownWithKatex(content);
    } catch {
      return content;
    }
  };

  const addWelcomeMessage = () => {
    if (!assistant.name) {
      return;
    }

    const welcomeMessage: Message = {
      id: Date.now(),
      type: 'ai',
      content: `你好！我是${assistant.name}。${assistant.description || '有什么我可以帮助你的吗？'}`,
      timestamp: Date.now(),
    };

    messages.value = [welcomeMessage];
  };

  const updateAIMessage = (content: string) => {
    const index = messages.value.findIndex((msg) => msg.id === currentAIMessageId.value);
    if (index === -1) {
      return;
    }

    const target = messages.value[index];
    if (!target) {
      return;
    }

    target.content = content;

    const messagesContainer = document.querySelector('.message-list');
    if (messagesContainer instanceof HTMLElement) {
      setTimeout(() => {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
      }, 0);
    }
  };

  const stopStreamingResponse = async () => {
    if (currentStreamController) {
      currentStreamController.abort();
      currentStreamController = null;
    }

    isAITyping.value = false;
    currentAIMessageId.value = null;
  };

  const sendMessage = async (text: string, regenerate = false) => {
    if (!text.trim() || isAITyping.value) {
      return;
    }

    if (currentStreamController) {
      await stopStreamingResponse();
    }

    if (!regenerate) {
      messages.value.push({
        id: Date.now(),
        type: 'user',
        content: text,
        timestamp: Date.now(),
      });
    }

    isAITyping.value = true;

    const aiMessageId = Date.now() + 1;
    currentAIMessageId.value = aiMessageId;
    messages.value.push({
      id: aiMessageId,
      type: 'ai',
      content: '',
      timestamp: Date.now(),
    });

    const messageRequest: ChatMessageRequest = {
      aiAvatarId: assistant.id,
      content: text,
      sessionId: sessionId.value || '',
      messageType: 'text',
    };

    if (regenerate) {
      messageRequest.regenerate = true;
    }

    const learningContext = getLearningContext();
    if (learningContext) {
      messageRequest.learningContext = learningContext;
    }

    const controller = new AbortController();
    currentStreamController = controller;

    let content = '';
    const apiUrl = buildApiUrl('/api/chat/message/stream');
    const token = localStorage.getItem('token')?.trim();

    try {
      await fetchEventSource(apiUrl, {
        method: 'POST',
        headers: {
          Accept: 'text/event-stream',
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: JSON.stringify(messageRequest),
        signal: controller.signal,
        credentials: 'include',
        async onopen(response) {
          const streamOk = response.ok && response.headers.get('content-type')?.includes('text/event-stream');
          if (streamOk) {
            return;
          }

          if (response.status === 401 || response.status === 403) {
            showToast('登录已过期，请重新登录');
            await userStore.logout();
            await router.push({
              path: '/login',
              query: { redirect: router.currentRoute.value.fullPath },
            });
            throw new Error(`未登录: ${response.status}`);
          }

          if (response.status === 404) {
            sessionId.value = undefined;
            throw new Error('会话不存在，请重新发送消息');
          }

          throw new Error(`SSE连接失败: ${response.status}`);
        },
        onmessage(event) {
          if (!event.data || !event.data.trim()) {
            return;
          }

          let data: Record<string, any> | null = null;
          try {
            data = JSON.parse(event.data) as Record<string, any>;
          } catch {
            return;
          }

          if (!data) {
            return;
          }

          if ((data.event === 'message_end' || data.event === 'tts_message_end') && data.conversation_id) {
            if (data.conversation_id !== sessionId.value) {
              sessionId.value = data.conversation_id;
            }

            if (currentStreamController) {
              currentStreamController.abort();
              currentStreamController = null;
            }

            isAITyping.value = false;
            currentAIMessageId.value = null;
            return;
          }

          if (data.event === 'message') {
            if (typeof data.answer === 'string') {
              content += data.answer;
            } else if (typeof data.content === 'string') {
              content += data.content;
            } else if (typeof data.data === 'string') {
              content += data.data;
            } else if (Array.isArray(data.choices) && data.choices.length > 0) {
              const first = data.choices[0];
              if (first?.delta?.content) {
                content += first.delta.content;
              } else if (first?.message?.content) {
                content += first.message.content;
              }
            }
          }

          if (content) {
            updateAIMessage(content);
          }
        },
        onclose() {
          // 主动断开连接，防止 fetchEventSource 自动重连
          if (currentStreamController) {
            currentStreamController.abort();
            currentStreamController = null;
          }
          isAITyping.value = false;
          currentAIMessageId.value = null;
        },
        onerror(error) {
          throw error;
        },
      });
    } catch (error) {
      console.error('发送消息失败', error);
      isAITyping.value = false;
      currentAIMessageId.value = null;
      showToast('发送消息失败，请重试');

      const lastMessage = messages.value.find((msg) => msg.id === aiMessageId);
      if (lastMessage) {
        lastMessage.content = '抱歉，发送消息时遇到问题，请重试。';
      }
    } finally {
      currentStreamController = null;
    }
  };

  const regenerateResponse = async (messageId: number) => {
    if (isAITyping.value) {
      await stopStreamingResponse();
    }

    const aiIndex = messages.value.findIndex((msg) => msg.id === messageId);
    if (aiIndex <= 0) {
      showToast('无法找到相关消息');
      return;
    }

    let userMessageIndex = -1;
    for (let index = aiIndex - 1; index >= 0; index -= 1) {
      if (messages.value[index]?.type === 'user') {
        userMessageIndex = index;
        break;
      }
    }

    if (userMessageIndex === -1) {
      showToast('无法找到相关问题');
      return;
    }

    const userMessage = messages.value[userMessageIndex];
    if (!userMessage) {
      showToast('无法找到相关问题');
      return;
    }

    messages.value.splice(aiIndex, 1);
    await sendMessage(userMessage.content, true);
  };

  return {
    messages,
    isAITyping,
    sessionId,
    addWelcomeMessage,
    stopStreamingResponse,
    sendMessage,
    regenerateResponse,
    formatMessage,
  };
}
