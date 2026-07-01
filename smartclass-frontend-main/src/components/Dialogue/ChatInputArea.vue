<template>
  <div class="input-container">
    <div class="console-row">
      <button class="plus-button" type="button" @click="$emit('image')">
        <van-icon name="plus" size="22" />
      </button>

      <div class="input-wrapper">
        <textarea
          v-model="inputValue"
          :disabled="isLoading"
          placeholder="Ask anything..."
          class="textarea"
          rows="2"
          @keypress.enter.prevent="handleEnterPress"
        ></textarea>
      </div>

      <button
        class="send-button"
        type="button"
        :disabled="isLoading || !inputValue.trim()"
        @click="sendMessage"
      >
        <van-icon v-if="!isLoading" name="arrow-up" size="20" />
        <van-loading v-else size="18" color="#fff" />
      </button>
    </div>

    <div class="toolbar">
      <button class="quick-chip" type="button" @click="$emit('fullscreen')">
        <van-icon name="bulb-o" size="14" />
        Idea Boost
      </button>
      <button class="quick-chip" type="button" @click="$emit('emoji')">
        <van-icon name="search" size="14" />
        Deep Search
      </button>
      <button class="quick-chip" type="button" @click="$emit('voice')">
        <van-icon name="records" size="14" />
        语音输入
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

const props = defineProps<{
  modelValue: string;
  isLoading?: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'send', message: string): void;
  (e: 'emoji'): void;
  (e: 'image'): void;
  (e: 'voice'): void;
  (e: 'fullscreen'): void;
}>();

const inputValue = ref(props.modelValue);

watch(
  () => props.modelValue,
  (newVal) => {
    inputValue.value = newVal;
  },
);

watch(inputValue, (newVal) => {
  emit('update:modelValue', newVal);
});

const handleEnterPress = (event: KeyboardEvent): void => {
  if (event.shiftKey) {
    return;
  }

  sendMessage();
};

const sendMessage = (): void => {
  if (inputValue.value.trim() && !props.isLoading) {
    emit('send', inputValue.value);
    inputValue.value = '';
  }
};
</script>

<style scoped>
.input-container {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 10;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  padding: 12px 16px 14px;
  background: rgba(255, 255, 255, 0.56);
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 -10px 30px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(20px);
}

.console-row {
  display: grid;
  grid-template-columns: 42px 1fr 46px;
  align-items: end;
  gap: 10px;
}

.input-wrapper {
  position: relative;
  width: 100%;
}

.textarea {
  box-sizing: border-box;
  width: 100%;
  min-height: 46px;
  max-height: 120px;
  padding: 12px 14px;
  overflow-y: auto;
  color: #1e293b;
  font-size: 15px;
  line-height: 1.5;
  resize: none;
  background: rgba(255, 255, 255, 0.64);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  outline: none;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(20px);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.textarea:focus {
  background: rgba(255, 255, 255, 0.78);
  border-color: rgba(108, 99, 255, 0.5);
  box-shadow: 0 0 0 4px rgba(108, 99, 255, 0.12), 0 10px 30px rgba(0, 0, 0, 0.1);
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  padding-top: 10px;
}

.send-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  color: #fff;
  background: linear-gradient(135deg, #6c63ff, #60a5fa);
  border: 0;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(79, 70, 229, 0.4);
}

.send-button:disabled {
  opacity: 0.55;
}

.plus-button,
.quick-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #4f46e5;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(20px);
}

.plus-button {
  width: 42px;
  height: 42px;
  margin-bottom: 2px;
  border-radius: 16px;
}

.quick-chip {
  gap: 5px;
  min-height: 30px;
  padding: 0 10px;
  color: #64748b;
  font-size: 12px;
  border-radius: 999px;
}
</style>
