<template>
  <div class="chat-header">
    <back-button :custom-path="customBackPath" class="back-button" />
    <div class="assistant-meta">
      <div class="assistant-title">{{ title || '猫猫鹅AI助手' }}</div>
      <div class="assistant-status">在线 · 智能陪伴</div>
    </div>
    <button class="more-button" type="button" aria-label="更多功能">
      <van-icon name="ellipsis" size="22" />
    </button>
  </div>
</template>

<script setup lang="ts">
import { BackButton } from '../Common';

// 定义props
defineProps<{
  title: string;
  customBackPath?: string;
}>();
</script>

<style scoped>
.chat-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 10;
  display: grid;
  grid-template-columns: 48px 1fr 44px;
  align-items: center;
  min-height: 68px;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.58);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(20px);
  width: 100%;
  box-sizing: border-box;
}

.back-button {
  min-width: 0;
}

:deep(.back-button) {
  position: static;
  padding: 0;
  background: transparent;
  border: 0;
  box-shadow: none;
  backdrop-filter: none;
}

:deep(.icon-wrapper) {
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  color: #4f46e5;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 999px;
}

.assistant-meta {
  min-width: 0;
  text-align: center;
}

.assistant-title {
  overflow: hidden;
  color: #1e293b;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.assistant-status {
  margin-top: 3px;
  color: #10b981;
  font-size: 12px;
  line-height: 1.2;
}

.more-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  color: #4f46e5;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 999px;
}

@media (min-width: 769px) {
  .chat-header {
    left: 50%;
    right: auto;
    width: min(1100px, 100vw);
    border-right: 1px solid rgba(255, 255, 255, 0.2);
    border-left: 1px solid rgba(255, 255, 255, 0.2);
    transform: translateX(-50%);
  }
}
</style> 
