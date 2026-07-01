<template>
  <van-cell-group inset class="chat-list" :class="listClass">
    <div
      v-for="chat in chats"
      :key="chat.id"
      class="chat-item"
      @click="$emit('select', chat)"
      @touchstart="startTouch(chat)"
      @touchend="endTouch"
      @touchmove="cancelTouch"
      @touchcancel="cancelTouch"
    >
      <div class="chat-avatar">
        <van-image 
          :src="getAvatarUrl(chat.avatar)" 
          round 
          width="50" 
          height="50"
          :lazy-load="false"
          @error="handleAvatarError($event)"
        />
        <div
          v-if="showStatus && chat.online !== undefined"
          class="online-status"
          :class="{ online: chat.online }"
        ></div>
        <van-badge
          v-if="chat.unreadCount && chat.unreadCount > 0"
          :content="chat.unreadCount"
          :max="99"
          class="unread-badge"
        />
      </div>
      <div class="chat-info">
        <div class="chat-header">
          <span class="assistant-name">{{ chat.assistantName }}</span>
          <!-- 未读数显示在名字后面 -->
          <span 
            v-if="chat.unreadCount && chat.unreadCount > 0" 
            class="name-unread-badge"
          >
            {{ chat.unreadCount > 99 ? '99+' : chat.unreadCount }}
          </span>
          <span class="chat-time">{{ chat.lastTime }}</span>
        </div>
        <div 
          v-if="chat.lastMessage" 
          class="chat-last-message"
          :class="{ 'unread': chat.isLastMessageUnread }"
        >
          {{ chat.lastMessage }}
        </div>
        <div v-else-if="chat.summary" class="chat-summary">{{ chat.summary }}</div>
        <div v-if="showPartnerStats" class="partner-stats">
          <span>🔥 连续 {{ getStudyDays(chat) }} 天</span>
          <span>📈 今日 {{ getStudyMinutes(chat) }} 分钟</span>
        </div>
        <div
          v-if="chat.tags && chat.tags.length > 0"
          class="chat-tags"
        >
          <span v-for="tag in chat.tags" :key="tag" class="tag">{{ tag }}</span>
        </div>
      </div>
    </div>
  </van-cell-group>
</template>

<script setup lang="ts">
import { ref } from 'vue';

interface Chat {
  id: number;
  sessionId?: string;
  assistantId: number;
  assistantName: string;
  avatar: string;
  lastMessage: string;
  summary?: string;
  lastTime: string;
  online?: boolean;
  tags?: string[];
  type: number;
  unreadCount?: number;
  isLastMessageUnread?: boolean;
}

// 定义props并设置默认值
const { chats, showStatus = true } = defineProps<{
  chats: Chat[];
  showStatus?: boolean;
  listClass?: string;
  showPartnerStats?: boolean;
}>();

// 定义事件
const emit = defineEmits<{
  (e: 'select', chat: Chat): void;
  (e: 'long-press', chat: Chat): void;
}>();

// 长按处理相关变量
const touchTimeout = ref<number | null>(null);
const touchedChat = ref<Chat | null>(null);
const longPressDuration = 600; // 长按判定时间（毫秒）

// 触摸开始事件
const startTouch = (chat: Chat) => {
  touchedChat.value = chat;

  // 清除可能存在的旧定时器
  if (touchTimeout.value !== null) {
    clearTimeout(touchTimeout.value);
  }

  // 设置新的定时器
  touchTimeout.value = window.setTimeout(() => {
    if (touchedChat.value) {
      emit('long-press', touchedChat.value);
    }
    endTouch(); // 触发长按后清理状态
  }, longPressDuration);
};

// 触摸结束事件
const endTouch = () => {
  if (touchTimeout.value !== null) {
    clearTimeout(touchTimeout.value);
    touchTimeout.value = null;
  }
  touchedChat.value = null;
};

// 触摸移动或取消时取消长按
const cancelTouch = () => {
  endTouch();
};

// 获取头像 URL，如果原始 URL 无效则使用默认头像
const defaultAvatarUrl = '/avatar-default.png';
const getAvatarUrl = (avatar: string): string => {
  if (!avatar) {
    return defaultAvatarUrl;
  }
  // 如果是外部链接，直接使用
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar;
  }
  // 本地链接直接返回
  return avatar;
};

// 处理头像加载错误
const handleAvatarError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  // 设置为默认头像
  img.src = defaultAvatarUrl;
};

const getStudyDays = (chat: Chat) => ((chat.assistantId || chat.id) % 6) + 2;
const getStudyMinutes = (chat: Chat) => ((chat.assistantId || chat.id) % 5 + 1) * 18;
</script>

<style scoped>
.chat-list {
  margin: 0;
  background-color: transparent;
  padding: 0 4px;
  box-sizing: border-box;
  display: block;
  width: 100%;
  position: relative;
  min-height: 50px;
}

.chat-item {
  display: flex;
  padding: 13px 14px;
  margin-bottom: 10px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  cursor: pointer;
  background: rgba(255, 255, 255, 0.68);
  border-radius: 20px;
  transition: all 0.3s ease;
  -webkit-tap-highlight-color: transparent; /* 避免移动端点击出现蓝色背景 */
  user-select: none; /* 防止选中文本 */
  touch-action: pan-y; /* 允许垂直滚动，但阻止其他默认行为 */
  position: relative;
  z-index: 1;
  box-shadow: 0 12px 32px rgba(79, 70, 229, 0.12);
  backdrop-filter: blur(20px);
}

.chat-item:active {
  background-color: rgba(255, 255, 255, 0.78);
}

.chat-item:hover {
  background-color: rgba(255, 255, 255, 0.82);
  transform: translateY(-2px);
  box-shadow: 0 16px 34px rgba(79, 70, 229, 0.18);
}

.chat-item:last-child {
  border-bottom: none;
}

.chat-avatar {
  position: relative;
  margin-right: 12px;
}

.chat-avatar :deep(.van-image) {
  border: 2px solid rgba(255, 255, 255, 0.74);
  box-shadow: 0 8px 18px rgba(79, 70, 229, 0.14);
}

.online-status {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #969799;
  border: 2px solid #fff;
}

.online-status.online {
  background-color: #10b981;
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
}

.chat-info {
  flex: 1;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.assistant-name {
  font-size: var(--font-size-md, 16px);
  font-weight: 700;
  color: #1e293b;
  font-family: 'Noto Sans SC', sans-serif;
}

/* 名字后面的未读数红点 */
.name-unread-badge {
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  color: white;
  border-radius: 9px;
  font-size: 12px;
  padding: 0 6px;
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(255, 77, 79, 0.3);
  display: inline-block;
  white-space: nowrap;
}

.chat-time {
  font-size: var(--font-size-sm, 12px);
  color: rgba(100, 116, 139, 0.62);
  font-family: 'Noto Sans SC', sans-serif;
}

.chat-last-message {
  font-size: var(--font-size-sm, 12px);
  color: #64748b;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: 'Noto Sans SC', sans-serif;
}

.chat-last-message.unread {
  font-weight: 700;
  color: #1e293b;
}

.chat-summary {
  font-size: var(--font-size-sm, 12px);
  color: #64748b;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: 'Noto Sans SC', sans-serif;
  font-style: italic;
}

.chat-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag {
  font-size: var(--font-size-sm, 10px);
  color: #4f46e5;
  background-color: rgba(79, 70, 229, 0.08);
  padding: 3px 7px;
  border-radius: 999px;
  font-family: 'Noto Sans SC', sans-serif;
}

.chat-list.history-flow .chat-item {
  align-items: center;
  min-height: 76px;
  border-radius: 22px 22px 22px 8px;
}

.chat-list.history-flow .chat-item:nth-child(2n) {
  border-radius: 22px 22px 8px 22px;
}

.chat-list.history-flow .tag:nth-child(1)::before {
  content: '📘 ';
}

.chat-list.history-flow .tag:nth-child(2)::before {
  content: '🧠 ';
}

.chat-list.history-flow .tag:nth-child(3)::before {
  content: '🗣 ';
}

.chat-list.friend-flow .chat-item {
  align-items: center;
}

.partner-stats {
  display: flex;
  gap: 8px;
  margin: 7px 0 5px;
  overflow-x: auto;
  color: #64748b;
  font-size: 11px;
  white-space: nowrap;
  scrollbar-width: none;
}

.partner-stats::-webkit-scrollbar {
  display: none;
}
</style>
