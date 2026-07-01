<template>
  <div class="course-study-page">
    <header class="study-header">
      <button class="study-back" type="button" @click="router.back()">
        <van-icon name="arrow-left" />
        <span>课程学习</span>
      </button>
      <div class="study-actions">
        <button type="button">
          <van-icon name="star-o" />
          <span>收藏</span>
        </button>
        <button type="button">
          <van-icon name="notes-o" />
          <span>笔记</span>
        </button>
        <button type="button">
          <van-icon name="share-o" />
          <span>分享</span>
        </button>
      </div>
    </header>
    <course-study
      :course="course"
      :course-id="courseId"
      :open-assistant-on-load="openAssistantOnLoad"
      :initial-section-id="initialSectionId"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { CourseStudy } from '@/components/Course';

interface CourseBrief {
  id: number;
  title?: string;
  description?: string;
  cover?: string;
}

const route = useRoute();
const router = useRouter();
const courseId = computed<number>(() => Number(route.params.id) || 0);
const openAssistantOnLoad = computed(() => route.query.assistant === '1');
const initialSectionId = computed<number | undefined>(() => {
  const raw = Number(route.query.sectionId || 0);
  return Number.isFinite(raw) && raw > 0 ? raw : undefined;
});
const course = ref<CourseBrief | null>(null);

const loadCourseCache = () => {
  const cache = sessionStorage.getItem(`course_${courseId.value}`);
  if (!cache) {
    course.value = null;
    return;
  }

  try {
    course.value = JSON.parse(cache) as CourseBrief;
  } catch (error) {
    console.error('解析课程缓存失败', error);
    course.value = null;
  }
};

watch(courseId, () => {
  loadCourseCache();
}, { immediate: true });
</script>

<style scoped>
.course-study-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(circle at 10% 6%, rgba(232, 224, 255, 0.72), transparent 24%),
    linear-gradient(135deg, #f8f5ff, #f2f8ff 48%, #fff7fc);
}

:deep(.course-study) {
  flex: 1;
  overflow: hidden;
}

.study-header {
  height: 76px;
  margin: 8px 18px 0;
  padding: 0 22px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.58);
  box-shadow: 0 16px 40px rgba(129, 140, 248, 0.12);
  backdrop-filter: blur(20px);
}

.study-back,
.study-actions button {
  border: 0;
  display: inline-flex;
  align-items: center;
  color: #25304d;
  background: transparent;
  cursor: pointer;
}

.study-back {
  gap: 10px;
  font-size: 17px;
  font-weight: 900;
}

.study-back :deep(.van-icon) {
  color: #7b61ff;
  font-size: 22px;
}

.study-actions {
  display: flex;
  align-items: center;
  gap: 22px;
}

.study-actions button {
  flex-direction: column;
  gap: 4px;
  color: #69738f;
  font-size: 12px;
  font-weight: 900;
}

.study-actions :deep(.van-icon) {
  color: #8b6cff;
  font-size: 23px;
}

@media (max-width: 640px) {
  .study-header {
    height: 64px;
    margin: 8px 12px 0;
    padding: 0 14px;
  }

  .study-actions {
    gap: 12px;
  }
}
</style>
