<template>
  <div class="chat-pagination" v-if="totalPages > 0">
    <van-pagination
      v-model="currentPage"
      :total-items="totalItems"
      :items-per-page="pageSize"
      :show-page-size="5"
      force-ellipses
      @change="handlePageChange"
      class="custom-pagination"
    >
      <template #prev-text>
        <van-icon name="arrow-left" />
      </template>
      <template #next-text>
        <van-icon name="arrow" />
      </template>
      <template #page-desc>
        <span class="page-desc-text"
          >{{ currentPage }}/{{ totalPages }} 页</span
        >
      </template>
    </van-pagination>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

const props = defineProps<{
  totalItems: number;
  pageSize: number;
  totalPages: number;
  initialPage?: number;
}>();

const emit = defineEmits(['page-change']);

const currentPage = ref(props.initialPage || 1);

// 监听当前页码变化
watch(
  () => props.initialPage,
  (newVal) => {
    if (newVal) {
      currentPage.value = newVal;
    }
  },
);

// 处理页码变化
const handlePageChange = (page: number) => {
  currentPage.value = page;
  emit('page-change', page);
};
</script>

<style scoped>
.chat-pagination {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: 4px 0;
  box-sizing: border-box;
}

/* 自定义分页样式 */
:deep(.custom-pagination) {
  --van-pagination-height: 34px;
  --van-pagination-item-width: 34px;
  --van-pagination-item-margin: 0 3px;
  --van-pagination-item-default-color: #6366f1;
  --van-pagination-item-font-size: 14px;
  --van-pagination-item-border-width: 0;
  --van-pagination-background-color: transparent;
  width: 100%;
  display: flex;
  justify-content: center;
  margin: 0;
}

:deep(.custom-pagination .van-pagination__item) {
  color: #64748b;
  background: rgba(255, 255, 255, 0.58);
  border-radius: 999px;
  box-shadow: 0 6px 16px rgba(120, 130, 180, 0.1);
  backdrop-filter: blur(12px);
}

:deep(.custom-pagination .van-pagination__item--active) {
  color: #fff;
  background: linear-gradient(135deg, #93c5fd, #6366f1 58%, #c4b5fd);
  box-shadow: 0 10px 22px rgba(99, 102, 241, 0.22);
}

.page-desc-text {
  font-family: 'Noto Sans SC', sans-serif;
  color: #64748b;
  font-weight: 700;
}
</style>
