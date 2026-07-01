<template>
  <div :class="['course-list-container', className]">
    <div class="course-section-head">
      <div>
        <h2>{{ title }}</h2>
      </div>
      <button v-if="showMore" class="more-link" type="button" @click="emit('more')">更多</button>
      <slot v-else name="right-icon"></slot>
    </div>

    <div v-if="courses.length" class="course-list">
      <course-item
        v-for="course in courses"
        :key="course.id"
        :course="course"
        @click="emit('select', course)"
      />
    </div>

    <div v-else class="course-empty">
      <div class="empty-icon">✦</div>
      <div class="empty-title">{{ emptyTitle || '暂无课程' }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import CourseItem from './CourseItem.vue';

interface EnhancedCourse {
  id: number;
  title: string;
  brief: string;
  cover: string;
  tag: string;
  tagColor: string;
  level: string;
  duration: number;
  studentsCount: number;
  grade?: string;
}

defineProps<{
  title: string;
  courses: EnhancedCourse[];
  showMore?: boolean;
  className?: string;
  emptyTitle?: string;
}>();

const emit = defineEmits<{
  (e: 'select', course: EnhancedCourse): void;
  (e: 'more'): void;
}>();
</script>

<style scoped>
.course-list-container {
  margin-bottom: 18px;
  background: transparent;
}

.course-section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  padding: 2px 2px 14px;
}

.course-section-head h2 {
  margin: 0;
  color: #1e293b;
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0;
}

.course-list {
  padding: 0;
}

.more-link {
  flex-shrink: 0;
  height: 34px;
  padding: 0 13px;
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.58);
  box-shadow: 0 8px 18px rgba(80, 100, 120, 0.06);
  font-size: 14px;
  color: #4f46e5;
  font-weight: 800;
  cursor: pointer;
}

.course-empty {
  min-height: 188px;
  padding: 24px 18px;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  background: rgba(255, 255, 255, 0.58);
  border: 1px solid rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(16px);
  box-shadow: 0 14px 32px rgba(80, 100, 120, 0.08);
}

.empty-icon {
  width: 50px;
  height: 50px;
  margin-bottom: 12px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4f46e5;
  font-size: 24px;
  background: rgba(224, 240, 176, 0.42);
}

.empty-title {
  color: #1e293b;
  font-size: 16px;
  font-weight: 800;
}

</style>

<style scoped>
.course-list-container {
  margin: 0;
  background: transparent;
}

.course-section-head {
  padding: 4px 2px 14px;
}

.course-section-head h2 {
  color: #24304f;
  font-size: 18px;
  font-weight: 900;
}

.course-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}

.more-link {
  display: none;
}

.course-empty {
  min-height: 220px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.48);
  border: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.76), 0 16px 36px rgba(99, 102, 241, 0.08);
}

.empty-icon {
  color: #8b5cf6;
  background: rgba(232, 224, 255, 0.62);
}

@media (max-width: 560px) {
  .course-list {
    grid-template-columns: 1fr;
  }
}
</style>
