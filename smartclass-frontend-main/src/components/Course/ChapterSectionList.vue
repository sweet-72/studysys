<template>
  <div class="chapter-section-list">
    <div
      v-for="chapter in chapters"
      :key="chapter.id"
      class="chapter-card"
    >
      <div class="chapter-header">
        <span class="chapter-title">{{ chapter.title }}</span>
        <span class="chapter-count">{{ chapter.sections.length }} 节</span>
      </div>
      <div class="section-list">
        <button
          v-for="section in chapter.sections"
          :key="section.id"
          type="button"
          class="section-item"
          :class="{
            active: activeSectionId === section.id,
            completed: section.learnStatus === 'COMPLETED',
            learning: section.learnStatus === 'LEARNING',
          }"
          @click="emit('select', { chapter, section })"
        >
          <span class="section-title">{{ section.title }}</span>
          <span class="section-status">{{ formatStatus(section.learnStatus) }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { CourseChapter, CourseSection } from '@/api/course';

defineProps<{
  chapters: CourseChapter[];
  activeSectionId?: number;
}>();

const emit = defineEmits<{
  (
    e: 'select',
    payload: {
      chapter: CourseChapter;
      section: CourseSection;
    },
  ): void;
}>();

const formatStatus = (status?: CourseSection['learnStatus']): string => {
  if (status === 'COMPLETED') {
    return '已完成';
  }
  if (status === 'LEARNING') {
    return '学习中';
  }
  return '未学习';
};
</script>

<style scoped>
.chapter-section-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chapter-card {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  border: 1px solid #edf0f4;
}

.chapter-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.chapter-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.chapter-count {
  font-size: 12px;
  color: #667085;
}

.section-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-item {
  width: 100%;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  border-radius: 10px;
  padding: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  text-align: left;
  cursor: pointer;
}

.section-item.active {
  border-color: #1989fa;
  background: #edf5ff;
}

.section-item.completed {
  border-color: #16a34a;
}

.section-item.learning {
  border-color: #f59e0b;
}

.section-title {
  font-size: 13px;
  color: #111827;
}

.section-status {
  font-size: 12px;
  color: #6b7280;
}
</style>
