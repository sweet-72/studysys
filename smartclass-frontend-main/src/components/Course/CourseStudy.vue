<template>
  <div class="course-study">
    <learning-workspace
      :course-id="courseId"
      :course="course"
      :open-assistant-on-load="props.openAssistantOnLoad"
      :initial-section-id="props.initialSectionId"
    />
  </div>
</template>

<script setup lang="ts">
import LearningWorkspace from './LearningWorkspace.vue';

interface CourseBrief {
  id: number;
  title?: string;
  description?: string;
  cover?: string;
}

const props = defineProps<{
  course: CourseBrief | null;
  courseId?: number;
  openAssistantOnLoad?: boolean;
  initialSectionId?: number;
}>();

const courseId = Number(props.courseId || props.course?.id || 0);
</script>

<style scoped>
.course-study {
  height: 100%;
}
</style>
