<template>
  <div class="course-item" @click="emit('click', course)">
    <div class="course-cover">
      <van-image :src="course.cover" fit="cover" radius="8" />
      <div class="cover-wash"></div>
    </div>

    <div class="course-info">
      <h3 class="course-title">{{ course.title }}</h3>
      <p class="course-brief">{{ course.brief }}</p>

      <div class="course-meta">
        <span v-if="course.grade" class="grade">{{ course.grade }}</span>
        <span class="difficulty">{{ course.level }}</span>
        <span class="duration">{{ course.duration }} 分钟</span>
        <span v-if="course.studentsCount" class="students">{{ course.studentsCount }} 人在学</span>
        <span class="course-tag">{{ course.tag }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
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
  course: EnhancedCourse;
}>();

const emit = defineEmits<{
  (e: 'click', course: EnhancedCourse): void;
}>();
</script>

<style scoped>
.course-item {
  margin-bottom: 18px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(16px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.65);
  overflow: hidden;
  box-shadow: 0 14px 32px rgba(80, 100, 120, 0.08);
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

@media (hover: hover) {
  .course-item:hover {
    transform: translateY(-4px);
    box-shadow: 0 18px 36px rgba(80, 100, 120, 0.12);
  }
}

.course-cover {
  position: relative;
  width: 100%;
  height: 190px;
  overflow: hidden;
}

.course-cover :deep(.van-image) {
  width: 100%;
  height: 100%;
  display: block;
}

.course-cover :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-wash {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.04), rgba(255, 255, 255, 0.18)),
    rgba(255, 255, 255, 0.12);
}

.course-info {
  padding: 16px 16px 17px;
}

.course-title {
  margin: 0 0 8px;
  font-size: 17px;
  color: #1e293b;
  font-weight: 800;
  line-height: 1.4;
}

.course-brief {
  margin: 0 0 12px;
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.course-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
  align-items: center;
}

.course-meta span {
  display: inline-flex;
  align-items: center;
  height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  font-weight: 700;
}

.difficulty {
  color: #2563eb;
  background: rgba(191, 219, 254, 0.52);
  border: 1px solid rgba(191, 219, 254, 0.62);
}

.grade {
  color: #3f6212;
  background: rgba(224, 240, 176, 0.62);
  border: 1px solid rgba(224, 240, 176, 0.72);
}

.duration,
.students {
  color: #64748b;
  background: rgba(241, 245, 249, 0.76);
  border: 1px solid rgba(226, 232, 240, 0.72);
}

.course-tag {
  margin-left: auto;
  color: #6d28d9;
  background: rgba(233, 213, 255, 0.52);
  border: 1px solid rgba(216, 180, 254, 0.58);
}

@media (max-width: 360px) {
  .course-cover {
    height: 172px;
  }

  .course-tag {
    margin-left: 0;
  }
}
</style>

<style scoped>
.course-item {
  height: 100%;
  margin-bottom: 0;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.56);
  border: 1px solid rgba(255, 255, 255, 0.74);
  box-shadow: 0 16px 36px rgba(99, 102, 241, 0.1);
  backdrop-filter: blur(18px);
  transform-origin: center;
  transition:
    transform 0.24s ease,
    box-shadow 0.24s ease,
    background 0.24s ease;
}

@media (hover: hover) {
  .course-item:hover {
    transform: translateY(-5px) scale(1.012);
    background: rgba(255, 255, 255, 0.68);
    box-shadow: 0 24px 48px rgba(99, 102, 241, 0.17);
  }
}

.course-item:active {
  transform: scale(0.98);
}

.course-cover {
  height: 188px;
  margin: 12px 12px 0;
  width: auto;
  border-radius: 22px;
  box-shadow: 0 14px 28px rgba(30, 41, 59, 0.12);
}

.course-cover :deep(.van-image),
.course-cover :deep(img) {
  border-radius: 22px;
}

.cover-wash {
  border-radius: 22px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.04) 20%, rgba(91, 33, 182, 0.28) 100%),
    linear-gradient(135deg, rgba(184, 167, 255, 0.14), rgba(155, 215, 255, 0.1));
}

.course-info {
  padding: 15px 16px 18px;
}

.course-title {
  color: #24304f;
  font-size: 17px;
  font-weight: 900;
}

.course-brief {
  color: #6f7a96;
  line-height: 1.65;
}

.course-meta {
  gap: 7px;
}

.course-meta span {
  height: 27px;
  padding: 0 10px;
  border: 0;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

.grade {
  color: #5b21b6;
  background: rgba(232, 224, 255, 0.72);
}

.difficulty {
  color: #1d4ed8;
  background: rgba(219, 234, 254, 0.78);
}

.duration {
  color: #0f766e;
  background: rgba(204, 251, 241, 0.72);
}

.students {
  color: #be185d;
  background: rgba(252, 231, 243, 0.72);
}

.course-tag {
  margin-left: 0;
  color: #7c2d12;
  background: rgba(255, 237, 213, 0.76);
}

@media (max-width: 360px) {
  .course-cover {
    height: 168px;
  }
}
</style>
