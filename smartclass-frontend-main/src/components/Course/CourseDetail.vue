<template>
  <van-popup
    :show="modelValue"
    class="course-detail-popup"
    overlay-class="course-detail-overlay"
    position="bottom"
    close-on-click-overlay
    lock-scroll
    teleport="body"
    :duration="0.3"
    :style="{ height: '88vh' }"
    @click-overlay="emit('close')"
    @update:show="handlePopupUpdate"
  >
    <div v-if="course" class="course-detail-drawer">
      <div class="drawer-handle"></div>

      <header class="drawer-header">
        <button class="back-button" type="button" @click="emit('close')">
          <van-icon name="arrow-left" />
          <span>返回</span>
        </button>

        <div class="header-title">
          <span>{{ course.subject || course.tag || '课程' }}</span>
          <em>{{ course.level }}</em>
        </div>

        <div class="duration-pill">
          <van-icon name="clock-o" />
          <span>{{ course.duration }} 分钟</span>
        </div>
      </header>

      <main class="drawer-body">
        <section class="detail-main">
          <div class="cover-panel">
            <van-image :src="course.cover" fit="cover" width="100%" height="100%" />
            <button class="play-button" type="button" aria-label="开始学习" @click="emit('start')">
              <van-icon name="play" />
            </button>
          </div>

          <section class="info-panel">
            <h2>{{ course.title }}</h2>

            <div class="detail-meta">
              <span v-if="course.grade" class="soft-tag blue">{{ course.grade }}</span>
              <span class="soft-tag purple">{{ course.level }}</span>
              <span v-if="course.studentsCount" class="learn-count">
                <van-icon name="friends-o" />
                {{ formatStudyCount(course.studentsCount) }}人学习
              </span>
            </div>

            <p class="course-description">
              {{ course.detailDescription || course.description || course.brief || '暂无课程介绍' }}
            </p>

            <div v-if="course.teacherName" class="teacher-line">
              <span>讲师</span>
              <strong>{{ course.teacherName }}</strong>
            </div>

            <div class="chapter-block">
              <div class="section-title">
                <van-icon name="notes-o" />
                <span>章节列表</span>
              </div>

              <div v-if="chapterRows.length > 0" class="chapter-list">
                <button
                  v-for="(chapter, index) in chapterRows"
                  :key="chapter.id"
                  class="chapter-item"
                  :class="{ active: index === 0, locked: index > 0 }"
                  type="button"
                  @click="emit('start')"
                >
                  <span class="chapter-status">
                    <van-icon :name="index === 0 ? 'play' : 'lock'" />
                  </span>
                  <span class="chapter-main">
                    <strong>{{ chapter.title }}</strong>
                    <small>{{ chapter.sectionText }}</small>
                  </span>
                  <span class="chapter-duration">{{ chapter.durationText }}</span>
                  <van-icon name="arrow" class="chapter-arrow" />
                </button>
              </div>

              <div v-else class="chapter-empty">暂无章节信息</div>
            </div>
          </section>
        </section>
      </main>

      <footer class="drawer-actions">
        <button class="assistant-action" type="button" @click="emit('assistant')">
          <span class="assistant-avatar">
            <van-icon name="service-o" />
          </span>
          <span>
            <strong>进入学习助手</strong>
            <small>AI 互动学习，智能答疑解惑</small>
          </span>
          <van-icon name="arrow" class="action-arrow" />
        </button>

        <button class="start-action" type="button" @click="emit('start')">
          <span class="book-icon">
            <van-icon name="bookmark-o" />
          </span>
          <span>
            <strong>开始学习</strong>
            <small>从第一章开始学习之旅</small>
          </span>
          <van-icon name="arrow" class="action-arrow" />
        </button>
      </footer>
    </div>
  </van-popup>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface ChapterSection {
  id: number;
  title: string;
  duration?: number;
  totalDuration?: number;
}

interface ChapterItem {
  id: number;
  title: string;
  duration?: number;
  totalDuration?: number;
  sections: ChapterSection[];
}

interface CourseForDetail {
  id: number;
  title: string;
  brief: string;
  cover: string;
  level: string;
  duration: number;
  grade?: string;
  tag?: string;
  subject?: string;
  studentsCount?: number;
  description?: string;
  teacherName?: string;
  detailDescription?: string;
  detailChapters?: ChapterItem[];
}

const props = defineProps<{
  modelValue: boolean;
  course: CourseForDetail | null;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'start'): void;
  (e: 'assistant'): void;
  (e: 'update:modelValue', value: boolean): void;
}>();

const handlePopupUpdate = (value: boolean) => {
  emit('update:modelValue', value);
  if (!value) {
    emit('close');
  }
};

const formatStudyCount = (count: number) => {
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}万`;
  }
  return String(count);
};

const formatMinutes = (minutes: number) => `${String(Math.max(1, Math.round(minutes))).padStart(2, '0')} 分钟`;

const getChapterDuration = (chapter: ChapterItem) => {
  const directDuration = Number(chapter.duration || chapter.totalDuration || 0);
  if (directDuration > 0) {
    return directDuration > 180 ? Math.ceil(directDuration / 60) : directDuration;
  }

  const sectionDuration = (chapter.sections || []).reduce((total, section) => {
    const duration = Number(section.duration || section.totalDuration || 0);
    return total + (duration > 180 ? Math.ceil(duration / 60) : duration);
  }, 0);

  if (sectionDuration > 0) {
    return sectionDuration;
  }

  const chapterCount = props.course?.detailChapters?.length || 1;
  return Math.max(1, Math.ceil((props.course?.duration || 30) / chapterCount));
};

const chapterRows = computed(() =>
  (props.course?.detailChapters || []).map((chapter) => {
    const sectionCount = chapter.sections?.length || 0;
    return {
      id: chapter.id,
      title: chapter.title,
      sectionText: sectionCount > 0 ? `${sectionCount} 个小节` : '暂无小节',
      durationText: formatMinutes(getChapterDuration(chapter)),
    };
  }),
);
</script>

<style scoped>
:global(.course-detail-overlay) {
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  opacity: 0;
  transition: opacity 0.3s ease-out;
}

:global(.van-overlay.course-detail-overlay) {
  opacity: 1;
}

:global(.course-detail-popup) {
  left: 50%;
  right: auto;
  bottom: 0;
  width: min(1120px, 100vw);
  max-width: 100vw;
  overflow: visible;
  border-radius: 28px 28px 0 0;
  background: transparent;
  box-shadow: none;
  transform: translate3d(-50%, 100%, 0);
}

:global(.course-detail-popup.van-popup--bottom) {
  transform: translate3d(-50%, 0, 0);
  transition-timing-function: ease-out;
}

.course-detail-drawer {
  position: relative;
  height: 100%;
  padding: 12px 22px 138px;
  overflow: hidden;
  color: #25304d;
  border-radius: 28px 28px 0 0;
  background:
    radial-gradient(circle at 5% 96%, rgba(216, 185, 255, 0.45), transparent 18%),
    radial-gradient(circle at 92% 10%, rgba(190, 218, 255, 0.45), transparent 20%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.82), rgba(245, 249, 255, 0.82) 48%, rgba(255, 248, 252, 0.84));
  box-shadow: 0 -24px 60px rgba(76, 69, 132, 0.22), inset 0 1px 0 rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(24px);
}

.drawer-handle {
  width: 48px;
  height: 5px;
  margin: 0 auto 10px;
  border-radius: 999px;
  background: rgba(127, 111, 199, 0.26);
}

.drawer-header {
  position: sticky;
  top: 0;
  z-index: 5;
  height: 70px;
  margin-bottom: 16px;
  padding: 0 22px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 22px;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  background: rgba(255, 255, 255, 0.58);
  box-shadow: 0 12px 30px rgba(129, 140, 248, 0.1);
  backdrop-filter: blur(18px);
}

.back-button,
.duration-pill,
.header-title {
  display: inline-flex;
  align-items: center;
}

.back-button {
  border: 0;
  gap: 8px;
  justify-self: start;
  color: #25304d;
  font-size: 16px;
  font-weight: 900;
  background: transparent;
  cursor: pointer;
}

.back-button :deep(.van-icon) {
  color: #7b61ff;
  font-size: 24px;
}

.header-title {
  gap: 10px;
  justify-self: center;
  font-size: 22px;
  font-weight: 900;
}

.header-title em {
  padding: 5px 10px;
  border-radius: 10px;
  color: #6b5cff;
  font-style: normal;
  font-size: 16px;
  background: rgba(232, 224, 255, 0.82);
}

.duration-pill {
  justify-self: end;
  gap: 8px;
  color: #25304d;
  font-size: 16px;
  font-weight: 800;
}

.duration-pill :deep(.van-icon) {
  color: #6b5cff;
  font-size: 22px;
}

.drawer-body {
  height: calc(100% - 86px);
  overflow-y: auto;
  overscroll-behavior: contain;
  padding: 0 4px 18px;
}

.drawer-body::-webkit-scrollbar {
  width: 0;
}

.detail-main {
  display: grid;
  grid-template-columns: minmax(300px, 0.95fr) minmax(360px, 1.05fr);
  gap: 22px 26px;
}

.cover-panel,
.info-panel,
.assistant-action,
.start-action {
  border: 1px solid rgba(255, 255, 255, 0.74);
  background: rgba(255, 255, 255, 0.58);
  box-shadow: 0 18px 42px rgba(129, 140, 248, 0.14);
  backdrop-filter: blur(18px);
}

.cover-panel {
  position: sticky;
  top: 0;
  min-height: 470px;
  overflow: hidden;
  border-radius: 24px;
}

.cover-panel :deep(.van-image) {
  height: 100%;
}

.play-button {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 82px;
  height: 82px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 42px;
  background: rgba(137, 112, 235, 0.54);
  box-shadow: 0 12px 28px rgba(137, 112, 235, 0.24);
  backdrop-filter: blur(12px);
  transform: translate(-50%, -50%);
  cursor: pointer;
}

.info-panel {
  min-height: 470px;
  padding: 28px;
  border-radius: 24px;
}

.info-panel h2 {
  margin: 0 0 18px;
  color: #25304d;
  font-size: 30px;
  font-weight: 900;
  line-height: 1.2;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 22px;
}

.soft-tag {
  min-width: 56px;
  height: 34px;
  padding: 0 14px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
}

.soft-tag.blue {
  color: #4387ff;
  background: rgba(222, 239, 255, 0.9);
}

.soft-tag.purple {
  color: #725cff;
  background: rgba(234, 226, 255, 0.9);
}

.learn-count {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #828ba8;
  font-weight: 800;
}

.course-description {
  margin: 0 0 22px;
  color: #69738f;
  font-size: 16px;
  line-height: 1.8;
}

.teacher-line {
  margin-bottom: 22px;
  padding: 12px 14px;
  border-radius: 16px;
  display: flex;
  justify-content: space-between;
  color: #69738f;
  background: rgba(255, 255, 255, 0.48);
}

.teacher-line strong {
  color: #25304d;
}

.section-title {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #25304d;
  font-size: 19px;
  font-weight: 900;
}

.section-title :deep(.van-icon) {
  color: #8a6cff;
}

.chapter-list {
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.chapter-item {
  min-height: 58px;
  padding: 0 14px 0 16px;
  border: 1px solid rgba(220, 226, 242, 0.84);
  border-radius: 16px;
  display: grid;
  grid-template-columns: 34px 1fr auto 20px;
  align-items: center;
  gap: 10px;
  color: #25304d;
  background: rgba(255, 255, 255, 0.46);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.chapter-item.active {
  border-color: rgba(178, 124, 255, 0.78);
  box-shadow: 0 10px 22px rgba(178, 124, 255, 0.14);
}

.chapter-item.locked {
  color: #8a92ad;
  background: rgba(255, 255, 255, 0.32);
}

.chapter-status {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, #7b61ff, #e78bff);
}

.chapter-item.locked .chapter-status {
  color: #a4abc2;
  background: rgba(231, 235, 247, 0.92);
}

.chapter-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
  text-align: left;
}

.chapter-main strong,
.chapter-main small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chapter-main strong {
  font-size: 15px;
}

.chapter-main small,
.chapter-duration,
.chapter-arrow {
  color: #8a92ad;
  font-size: 13px;
  font-weight: 800;
}

.chapter-empty {
  padding: 18px;
  border-radius: 16px;
  color: #8a92ad;
  text-align: center;
  background: rgba(255, 255, 255, 0.5);
}

.drawer-actions {
  position: absolute;
  left: 22px;
  right: 22px;
  bottom: 18px;
  z-index: 8;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 22px;
}

.assistant-action,
.start-action {
  min-height: 90px;
  padding: 14px 20px;
  border-radius: 22px;
  display: grid;
  grid-template-columns: 58px 1fr 38px;
  align-items: center;
  gap: 14px;
  cursor: pointer;
}

.assistant-avatar,
.book-icon {
  width: 54px;
  height: 54px;
  border-radius: 19px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 29px;
}

.assistant-avatar {
  color: #6b5cff;
  background: rgba(232, 224, 255, 0.74);
}

.assistant-action {
  color: #25304d;
  text-align: left;
}

.assistant-action strong,
.start-action strong {
  display: block;
  margin-bottom: 6px;
  font-size: 19px;
}

.assistant-action small,
.start-action small {
  color: #69738f;
  font-size: 14px;
}

.start-action {
  border: 0;
  color: #fff;
  text-align: left;
  background: linear-gradient(135deg, #5f7cff, #8f63ff 48%, #eba0f5);
  box-shadow: 0 20px 42px rgba(143, 99, 255, 0.28);
}

.book-icon {
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
}

.start-action small {
  color: rgba(255, 255, 255, 0.82);
}

.action-arrow {
  justify-self: end;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #7b61ff;
  background: rgba(255, 255, 255, 0.78);
}

.start-action .action-arrow {
  color: #8f63ff;
}

@media (hover: hover) {
  .chapter-item:hover,
  .assistant-action:hover,
  .start-action:hover {
    transform: translateY(-3px);
  }
}

@media (max-width: 820px) {
  :global(.course-detail-popup) {
    width: 100vw;
  }

  .course-detail-drawer {
    padding: 10px 14px 196px;
  }

  .drawer-header {
    height: auto;
    min-height: 66px;
    padding: 12px 14px;
    grid-template-columns: 1fr;
    justify-items: start;
    gap: 8px;
  }

  .header-title,
  .duration-pill {
    justify-self: start;
  }

  .detail-main {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .cover-panel,
  .info-panel {
    min-height: auto;
  }

  .cover-panel {
    position: relative;
    height: 260px;
  }

  .info-panel {
    padding: 20px;
  }

  .info-panel h2 {
    font-size: 24px;
  }

  .learn-count {
    margin-left: 0;
  }

  .drawer-actions {
    left: 14px;
    right: 14px;
    bottom: 12px;
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .assistant-action,
  .start-action {
    min-height: 78px;
  }
}
</style>
