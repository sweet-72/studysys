<template>
  <div class="learning-history">
    <div class="history-header">
      <h3 class="section-title">学习历史记录</h3>
      <span class="more-link" @click="onViewAll">查看全部</span>
    </div>
    <van-empty v-if="historyItems.length === 0" image-size="64" description="暂无学习历史" />
    <div v-else class="history-list">
      <div v-for="item in historyItems" :key="item.id" class="history-item">
        <div class="history-date">{{ formatDate(item.actionTime) }}</div>
        <div class="history-content">
          <div class="icon-wrapper">
            <van-icon :name="iconByType(item.actionType)" class="history-icon" />
          </div>
          <div class="history-info">
            <div class="history-title">{{ item.title }}</div>
            <div class="history-desc">{{ item.description || '学习记录' }}</div>
          </div>
          <div class="history-time">{{ formatClock(item.actionTime) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface HistoryItem {
  id: string;
  actionType: string;
  title: string;
  description?: string;
  actionTime?: string;
}

defineProps<{
  historyItems: HistoryItem[];
}>();

const emit = defineEmits<{
  (e: 'view-all'): void;
}>();

const onViewAll = (): void => {
  emit('view-all');
};

const toDate = (value?: string) => {
  if (!value) {
    return null;
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
};

const formatDate = (value?: string) => {
  const date = toDate(value);
  if (!date) {
    return '';
  }
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

const formatClock = (value?: string) => {
  const date = toDate(value);
  if (!date) {
    return '';
  }
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

const iconByType = (type: string) => {
  if (type === 'DAILY_WORD') {
    return 'records-o';
  }
  if (type === 'DAILY_ARTICLE') {
    return 'description-o';
  }
  if (type === 'HOMEWORK') {
    return 'edit';
  }
  if (type === 'SECTION_COMPLETE') {
    return 'passed';
  }
  return 'play-circle-o';
};
</script>

<style scoped>
.learning-history {
  margin-bottom: 12px;
  border-radius: 12px;
  overflow: hidden;
  background-color: #ffffff;
  box-shadow: 0 2px 12px rgba(100, 101, 102, 0.08);
  padding: 16px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-weight: 600;
  font-family: 'Noto Sans SC', sans-serif;
  font-size: var(--font-size-md);
  color: #323233;
  margin: 0;
}

.more-link {
  color: #1989fa;
  font-size: var(--font-size-sm);
  font-weight: 500;
  padding: 3px 6px;
  border-radius: 4px;
  background-color: rgba(25, 137, 250, 0.1);
}

.history-item {
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.history-item:last-child {
  border-bottom: none;
}

.history-date {
  font-size: var(--font-size-sm);
  color: #969799;
  margin-bottom: 8px;
  font-family: 'Noto Sans SC', sans-serif;
  font-weight: 500;
}

.history-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: rgba(25, 137, 250, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.history-icon {
  font-size: 20px;
  color: #1989fa;
}

.history-info {
  flex: 1;
  min-width: 0;
}

.history-title {
  font-size: var(--font-size-md);
  color: #323233;
  margin-bottom: 4px;
  font-weight: 600;
  font-family: 'Noto Sans SC', sans-serif;
}

.history-desc {
  font-size: var(--font-size-sm);
  color: #646566;
  font-family: 'Noto Sans SC', sans-serif;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-time {
  font-size: var(--font-size-sm);
  color: #969799;
  font-family: 'Noto Sans SC', sans-serif;
  background-color: #f7f8fa;
  padding: 4px 8px;
  border-radius: 4px;
  flex-shrink: 0;
  white-space: nowrap;
}
</style>
