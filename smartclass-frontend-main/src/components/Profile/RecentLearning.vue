<template>
  <div class="recent-learning">
    <div class="learning-header">
      <h3 class="section-title">最近学习</h3>
    </div>
    <van-empty
      v-if="learningItems.length === 0"
      image-size="64"
      description="还没有最近学习记录，去课程页开始学习吧"
    />
    <div v-else class="learning-list">
      <div
        v-for="item in learningItems"
        :key="`${item.courseId}-${item.sectionId || 0}`"
        class="learning-item"
        @click="continueLearning(item)"
      >
        <div class="image-container">
          <van-icon name="play-circle-o" color="#1989fa" size="24" />
        </div>
        <div class="learning-info">
          <div class="learning-name">{{ item.courseTitle }}</div>
          <div class="learning-subtitle">
            <span>{{ item.sectionTitle || '课程学习' }}</span>
            <span>{{ formatTime(item.lastLearnTime) }}</span>
          </div>
          <van-progress
            :percentage="normalizeProgress(item.progressPercent)"
            size="small"
            :stroke-width="6"
            :pivot-text="`${normalizeProgress(item.progressPercent)}%`"
          />
        </div>
        <van-icon name="arrow" color="#969799" size="16" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

interface LearningItem {
  courseId: number;
  courseTitle: string;
  sectionId?: number;
  sectionTitle?: string;
  progressPercent?: number;
  lastLearnTime?: string;
}

defineProps<{
  learningItems: LearningItem[];
}>();

const router = useRouter();

const normalizeProgress = (value?: number) => {
  if (value === undefined || Number.isNaN(Number(value))) {
    return 0;
  }
  return Math.max(0, Math.min(100, Math.round(Number(value))));
};

const formatTime = (value?: string) => {
  if (!value) {
    return '暂无时间';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

const continueLearning = (item: LearningItem) => {
  const query = item.sectionId ? { sectionId: String(item.sectionId) } : {};
  router.push({
    path: `/courses/study/${item.courseId}`,
    query,
  });
};
</script>

<style scoped>
.recent-learning {
  margin-bottom: 12px;
  border-radius: 12px;
  overflow: hidden;
  background-color: #ffffff;
  box-shadow: 0 2px 12px rgba(100, 101, 102, 0.08);
  padding: 16px;
}

.learning-header {
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

.learning-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
}

.learning-item:last-child {
  border-bottom: none;
}

.image-container {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background-color: rgba(25, 137, 250, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.learning-info {
  flex: 1;
  min-width: 0;
}

.learning-name {
  font-size: var(--font-size-md);
  color: #323233;
  margin-bottom: 5px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.learning-subtitle {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 7px;
  font-size: 12px;
  color: #969799;
}

.learning-subtitle span:first-child {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.van-progress__pivot) {
  background-color: #1989fa;
  color: #ffffff;
  font-weight: 500;
  font-size: var(--font-size-sm);
}
</style>
