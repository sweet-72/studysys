<template>
  <van-button
    v-if="show"
    class="stop-response-btn"
    size="small"
    type="default"
    @click="handleStop"
  >
    <van-icon name="pause-circle-o" style="margin-right: 4px; vertical-align: -2px;" />
    停止响应
  </van-button>
</template>

<script setup lang="ts">
// 定义props
defineProps<{
  show: boolean;
}>();

// 定义emit
const emit = defineEmits<{
  (e: 'stop'): void;
}>();

// 处理停止按钮点击
const handleStop = () => {
  emit('stop');
};
</script>

<style scoped>
.stop-response-btn {
  position: fixed;
  left: 50%;
  transform: translateX(-50%);
  bottom: 158px;
  z-index: 999;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border-radius: 999px;
  padding: 0 12px;
  height: 32px;
  background: rgba(255, 255, 255, 0.7);
  color: #4f46e5;
  border: 1px solid rgba(255, 255, 255, 0.2);
  font-size: 14px;
  transition: all 0.3s;
  backdrop-filter: blur(20px);
}

.stop-response-btn:active {
  background-color: #f2f3f5;
}
</style> 
