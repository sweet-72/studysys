<template>
  <div class="message-list" ref="messageList">
    <div
      v-for="message in messages"
      :key="message.id"
      class="message-item-container"
    >
      <div
        v-if="
          message.type === 'user' ||
          (message.type === 'ai' && message.content.trim())
        "
        :class="['message-item', message.type]"
      >
        <div v-if="message.type === 'user'" class="message-content">
          <div>{{ message.content }}</div>
        </div>

        <div v-else class="ai-card">
          <div class="ai-card-header">
            <van-image :src="assistantAvatar" round width="34" height="34" />
            <span>{{ assistantName || 'AI助手' }}</span>
          </div>
          <div
            v-html="
              customFormatMessage
                ? customFormatMessage(message.content)
                : defaultFormatMessage(message.content)
            "
            class="markdown-body"
          ></div>
          <div class="message-actions">
            <button class="action-button" type="button" @click="likeMessage">
              <van-icon name="good-job-o" />
            </button>
            <button class="action-button" type="button" @click="regenerateResponse(message.id)">
              <van-icon name="replay" />
            </button>
            <button class="action-button" type="button" @click="copyMessage(message.content)">
              <van-icon name="description" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="thinking-card">
      <div class="thinking-orb"></div>
      <span>AI正在思考中...</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUpdated, ref, watch } from 'vue';
import { showToast } from 'vant';

interface Message {
  id: number;
  type: 'ai' | 'user';
  content: string;
  timestamp: number;
}

const props = defineProps<{
  messages: Message[];
  assistantAvatar: string;
  assistantName?: string;
  userAvatar: string;
  customFormatMessage?: (content: string) => string;
  loading?: boolean;
}>();

const emit = defineEmits<{
  (e: 'regenerate', messageId: number): void;
}>();

const messageList = ref<HTMLElement | null>(null);

const defaultFormatMessage = (content: string): string => content;

const copyMessage = (content: string): void => {
  const tempDiv = document.createElement('div');
  tempDiv.innerHTML = content;
  const plainText = tempDiv.textContent || tempDiv.innerText || '';

  navigator.clipboard
    .writeText(plainText)
    .then(() => {
      showToast('已复制到剪贴板');
    })
    .catch(() => {
      showToast('复制失败，请手动复制');
    });
};

const likeMessage = (): void => {
  showToast('已收到反馈');
};

const regenerateResponse = (messageId: number): void => {
  emit('regenerate', messageId);
};

const scrollToBottom = (): void => {
  if (messageList.value) {
    messageList.value.scrollTop = messageList.value.scrollHeight;
  }
};

watch(
  () => props.messages.length,
  () => {
    setTimeout(scrollToBottom, 100);
  },
);

watch(
  () => props.loading,
  () => {
    setTimeout(scrollToBottom, 80);
  },
);

onMounted(scrollToBottom);
onUpdated(scrollToBottom);
</script>

<style scoped>
.message-list {
  flex: 1;
  padding: 20px 16px 34px;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.message-item-container {
  margin-bottom: 18px;
}

.message-item {
  display: flex;
  width: 100%;
  animation: messageEnter 0.28s ease both;
}

.message-item.user {
  justify-content: flex-end;
}

.message-item.ai {
  justify-content: center;
}

.message-content {
  box-sizing: border-box;
  display: inline-block;
  width: fit-content;
  max-width: 78%;
  min-height: 20px;
  padding: 12px 14px;
  color: #fff;
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
  background: linear-gradient(135deg, #6c63ff, #60a5fa);
  border-radius: 16px 16px 4px 16px;
  box-shadow: 0 10px 24px rgba(79, 70, 229, 0.26);
}

.ai-card {
  box-sizing: border-box;
  width: min(100%, 680px);
  padding: 18px;
  color: #1e293b;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(20px);
  transition: transform 0.24s ease, box-shadow 0.24s ease;
}

.ai-card:hover {
  box-shadow: 0 16px 38px rgba(79, 70, 229, 0.16);
  transform: translateY(-2px);
}

.ai-card-header {
  display: flex;
  align-items: center;
  gap: 9px;
  margin-bottom: 12px;
  color: #4f46e5;
  font-size: 13px;
  font-weight: 800;
}

.ai-card-header :deep(.van-image) {
  border: 2px solid rgba(255, 255, 255, 0.74);
  box-shadow: 0 8px 18px rgba(79, 70, 229, 0.16);
}

.message-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 14px;
}

.action-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  color: #64748b;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.58);
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 999px;
  backdrop-filter: blur(20px);
  transition: transform 0.2s ease, color 0.2s ease;
}

.action-button:hover {
  color: #6c63ff;
  transform: translateY(-1px);
}

.thinking-card {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  color: #64748b;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(20px);
  animation: messageEnter 0.28s ease both;
}

.thinking-orb {
  width: 10px;
  height: 10px;
  background: #6c63ff;
  border-radius: 50%;
  box-shadow: 0 0 0 0 rgba(108, 99, 255, 0.3);
  animation: pulse 1.4s infinite;
}

.markdown-body {
  width: 100%;
  margin: 0;
  font-size: 14px;
  line-height: 1.65;
  word-break: break-word;
}

.markdown-body p {
  margin: 6px 0;
}

.markdown-body code {
  padding: 2px 4px;
  font-family: monospace;
  font-size: 13px;
  white-space: pre-wrap;
  background-color: rgba(108, 99, 255, 0.08);
  border-radius: 4px;
}

.markdown-body pre {
  box-sizing: border-box;
  width: 100%;
  padding: 10px;
  margin: 8px 0;
  overflow-x: auto;
  background-color: rgba(15, 23, 42, 0.06);
  border-radius: 12px;
}

.markdown-body pre code {
  display: block;
  width: 100%;
  padding: 0;
  background: none;
}

@keyframes messageEnter {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(108, 99, 255, 0.35);
  }

  70% {
    box-shadow: 0 0 0 10px rgba(108, 99, 255, 0);
  }

  100% {
    box-shadow: 0 0 0 0 rgba(108, 99, 255, 0);
  }
}
</style>
