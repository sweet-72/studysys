<template>
  <div class="popular-courses-module">
    <div class="module-header">
      <div>
        <h2>好课推荐</h2>
      </div>
      <span class="more-link" @click="$emit('more')">更多 <van-icon name="arrow" /></span>
    </div>

    <div v-if="loading" class="course-loading">
      <van-skeleton title :row="2" />
    </div>

    <div
      v-else-if="courses.length > 0"
      ref="courseTrackRef"
      class="course-list"
      @scroll.passive="handleManualScroll"
      @wheel="handleWheel"
      @pointerdown="handlePointerDown"
      @pointermove="handlePointerMove"
      @pointerup="handlePointerUp"
      @pointerleave="handlePointerUp"
      @pointercancel="handlePointerUp"
    >
      <div
        v-for="(course, index) in courses"
        :key="course.id"
        class="course-card"
        @click="handleSelect(course, index)"
      >
        <div class="course-cover">
          <van-image :src="course.cover" fit="cover" radius="0" />
        </div>
        <div class="course-info">
          <div class="course-copy">
            <h3 class="course-title">{{ course.title }}</h3>
            <p class="course-brief">{{ course.brief }}</p>
          </div>
          <div class="course-meta">
            <span class="level-pill">{{ course.level }}</span>
            <span v-if="course.studentsCount">{{ course.studentsCount }} 人在学</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-courses">暂无推荐课程</div>

    <div class="carousel-dots" v-if="courses.length > 1">
      <span
        v-for="(_, index) in courses"
        :key="index"
        :class="{ active: activeIndex === index }"
      ></span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { Course } from '../../api/mock';

const props = defineProps<{
  courses: Course[];
  loading?: boolean;
}>();

const emit = defineEmits<{
  (e: 'select', course: Course): void;
  (e: 'more'): void;
}>();

const courseTrackRef = ref<HTMLElement | null>(null);
const activeIndex = ref(0);
const isDragging = ref(false);
const hasDragged = ref(false);
const dragStartX = ref(0);
const dragStartScrollLeft = ref(0);
let autoTimer: ReturnType<typeof window.setInterval> | null = null;
let resumeTimer: ReturnType<typeof window.setTimeout> | null = null;
let scrollTimer: ReturnType<typeof window.setTimeout> | null = null;

const clearAutoTimer = () => {
  if (autoTimer) {
    clearInterval(autoTimer);
    autoTimer = null;
  }
};

const clearResumeTimer = () => {
  if (resumeTimer) {
    clearTimeout(resumeTimer);
    resumeTimer = null;
  }
};

const scrollToCourse = (index: number) => {
  const track = courseTrackRef.value;
  if (!track || props.courses.length === 0) return;

  const normalizedIndex = (index + props.courses.length) % props.courses.length;
  const firstCard = track.querySelector<HTMLElement>('.course-card');
  if (!firstCard) return;

  const gap = 12;
  track.scrollTo({
    left: normalizedIndex * (firstCard.offsetWidth + gap),
    behavior: 'smooth',
  });
  activeIndex.value = normalizedIndex;
};

const startAutoPlay = () => {
  clearAutoTimer();
  if (props.courses.length <= 1) return;

  autoTimer = window.setInterval(() => {
    scrollToCourse(activeIndex.value + 1);
  }, 3000);
};

const pauseAutoPlay = () => {
  clearAutoTimer();
  clearResumeTimer();
  resumeTimer = window.setTimeout(startAutoPlay, 6500);
};

const handleManualScroll = () => {
  pauseAutoPlay();

  if (scrollTimer) {
    clearTimeout(scrollTimer);
  }

  scrollTimer = window.setTimeout(() => {
    const track = courseTrackRef.value;
    const firstCard = track?.querySelector<HTMLElement>('.course-card');
    if (!track || !firstCard) return;

    const gap = 12;
    const nextIndex = Math.round(track.scrollLeft / (firstCard.offsetWidth + gap));
    activeIndex.value = Math.min(Math.max(nextIndex, 0), props.courses.length - 1);
  }, 120);
};

const handleWheel = (event: WheelEvent) => {
  const track = courseTrackRef.value;
  if (!track) return;

  const canScrollHorizontally = track.scrollWidth > track.clientWidth;
  if (!canScrollHorizontally) return;

  const horizontalDelta =
    Math.abs(event.deltaX) > Math.abs(event.deltaY) ? event.deltaX : event.deltaY;

  if (horizontalDelta === 0) return;

  event.preventDefault();
  track.scrollLeft += horizontalDelta;
};

const handlePointerDown = (event: PointerEvent) => {
  const track = courseTrackRef.value;
  if (!track || event.button !== 0) return;

  pauseAutoPlay();
  isDragging.value = true;
  hasDragged.value = false;
  dragStartX.value = event.clientX;
  dragStartScrollLeft.value = track.scrollLeft;
  track.setPointerCapture?.(event.pointerId);
};

const handlePointerMove = (event: PointerEvent) => {
  const track = courseTrackRef.value;
  if (!track || !isDragging.value) return;

  const distance = event.clientX - dragStartX.value;
  if (Math.abs(distance) > 4) {
    hasDragged.value = true;
  }

  track.scrollLeft = dragStartScrollLeft.value - distance;
};

const handlePointerUp = (event: PointerEvent) => {
  const track = courseTrackRef.value;
  if (!track || !isDragging.value) return;

  isDragging.value = false;
  track.releasePointerCapture?.(event.pointerId);
};

const handleSelect = (course: Course, index: number) => {
  if (hasDragged.value) {
    hasDragged.value = false;
    return;
  }

  pauseAutoPlay();
  activeIndex.value = index;
  emit('select', course);
};

onMounted(async () => {
  await nextTick();
  startAutoPlay();
});

watch(
  () => props.courses.length,
  async () => {
    activeIndex.value = 0;
    await nextTick();
    scrollToCourse(0);
    startAutoPlay();
  },
);

onUnmounted(() => {
  clearAutoTimer();
  clearResumeTimer();
  if (scrollTimer) {
    clearTimeout(scrollTimer);
  }
});
</script>

<style scoped>
.popular-courses-module {
  margin-bottom: 0;
  overflow: visible;
  background: transparent;
  border: none;
  border-radius: 0;
  box-shadow: none;
}

.module-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.module-header h2 {
  margin: 0;
  color: #1e293b;
  font-size: 20px !important;
  font-weight: 800;
  line-height: 1.2;
}

.more-link {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.course-list {
  display: flex;
  flex-wrap: nowrap;
  gap: 12px;
  padding: 0 18px 8px;
  margin: 0 -18px;
  overflow-x: auto;
  overflow-y: hidden;
  cursor: grab;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior-x: contain;
  scrollbar-width: none;
  scroll-snap-type: x mandatory;
  touch-action: pan-x;
  user-select: none;
}

.course-list:active {
  cursor: grabbing;
}

.course-list::-webkit-scrollbar {
  display: none;
}

.course-loading {
  padding: 0 2px 10px;
}

.empty-courses {
  padding: 22px 0 26px;
  color: #94a3b8;
  font-size: 13px;
  font-weight: 600;
  text-align: center;
}

.course-card {
  display: flex;
  flex: 0 0 clamp(168px, 38vw, 236px);
  max-width: 236px;
  min-width: 168px;
  min-height: 188px;
  flex-direction: column;
  overflow: hidden;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 19px;
  box-shadow: 0 14px 32px rgba(79, 70, 229, 0.08);
  backdrop-filter: blur(16px);
  scroll-snap-align: start;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.course-card:active {
  box-shadow: 0 10px 24px rgba(79, 70, 229, 0.1);
  transform: scale(0.98);
}

.course-cover {
  position: relative;
  flex: 0 0 auto;
  width: 100%;
  height: 90px;
  overflow: hidden;
}

.course-cover :deep(.van-image) {
  width: 100%;
  height: 100%;
}

.course-cover :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-info {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  justify-content: space-between;
  padding: 10px 10px 11px;
}

.course-title {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: #1e293b;
  font-size: 13px !important;
  font-weight: 800 !important;
  line-height: 1.32;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
}

.course-brief {
  display: none;
  margin: 7px 0 0;
  overflow: hidden;
  color: #94a3b8;
  font-size: 12px;
  line-height: 1.45;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  line-clamp: 1;
}

.course-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  overflow: hidden;
  color: #94a3b8;
  font-size: 11px !important;
  line-height: 1.2;
  white-space: nowrap;
}

.level-pill {
  flex-shrink: 0;
  padding: 4px 7px;
  color: #6366f1;
  font-size: 11px;
  font-weight: 800;
  background: rgba(99, 102, 241, 0.09);
  border-radius: 999px;
}

.carousel-dots {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 3px;
}

.carousel-dots span {
  width: 6px;
  height: 6px;
  background: rgba(148, 163, 184, 0.48);
  border-radius: 999px;
  transition: all 0.2s ease;
}

.carousel-dots span.active {
  width: 18px;
  background: #6366f1;
}

@media (max-width: 375px) {
  .course-card {
    flex-basis: 162px;
    max-width: 162px;
    min-width: 162px;
  }

  .course-cover {
    height: 84px;
  }
}
</style>
