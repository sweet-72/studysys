<template>
  <div class="function-grid feature-orbit">
    <div ref="featurePagesRef" class="feature-pages" @scroll.passive="onFeatureScroll">
      <div
        v-for="(page, pageIndex) in featurePages"
        :key="pageIndex"
        class="feature-page"
      >
        <button
          v-for="item in page"
          :key="item.path"
          type="button"
          class="dock-item"
          :class="{ active: selectedPath === item.path }"
          @click="handleFeatureClick(item)"
        >
          <span class="feature-bubble">
            <van-icon :name="item.icon" size="24" />
          </span>
          <span v-if="selectedPath === item.path" class="feature-name-pop">
            {{ item.text }}
          </span>
        </button>
      </div>
    </div>
    <div class="feature-dots">
      <span
        v-for="(_, index) in featurePages"
        :key="index"
        :class="{ active: currentPage === index }"
        @click="goToPage(index)"
      ></span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';

type FeatureItem = {
  text: string;
  icon: string;
  path: string;
};

const router = useRouter();
const currentPage = ref(0);
const selectedPath = ref('');
const featurePagesRef = ref<HTMLElement | null>(null);

const features: FeatureItem[] = [
  { text: 'AI对话', icon: 'chat-o', path: '/chat?tab=intelligence' },
  { text: '课程学习', icon: 'play-circle-o', path: '/courses' },
  { text: '社交圈子', icon: 'friends-o', path: '/circle' },
  { text: '个人中心', icon: 'user-o', path: '/profile' },
  { text: '单词记忆', icon: 'bookmark-o', path: '/vocabulary/collected' },
  { text: '精品文章', icon: 'notes-o', path: '/articles' },
  { text: '课程表', icon: 'calendar-o', path: '/courses/schedule' },
  { text: '学习历史', icon: 'records', path: '/profile?tab=history' },
];

const featurePages = computed(() => {
  const pages: FeatureItem[][] = [];
  for (let i = 0; i < features.length; i += 4) {
    pages.push(features.slice(i, i + 4));
  }
  return pages;
});

const handleFeatureClick = (item: FeatureItem) => {
  if (selectedPath.value === item.path) {
    router.push(item.path);
    return;
  }
  selectedPath.value = item.path;
};

const goToPage = (index: number) => {
  selectedPath.value = '';
  currentPage.value = index;
  featurePagesRef.value?.scrollTo({
    left: featurePagesRef.value.clientWidth * index,
    behavior: 'smooth',
  });
};

const onFeatureScroll = (event: Event) => {
  const target = event.target as HTMLElement;
  const nextPage = Math.round(target.scrollLeft / target.clientWidth);
  if (nextPage !== currentPage.value) {
    selectedPath.value = '';
  }
  currentPage.value = nextPage;
};
</script>

<style scoped>
.function-grid {
  position: absolute;
  left: 50%;
  bottom: 104px;
  transform: translateX(-50%);
  z-index: 5;
  width: min(360px, calc(100vw - 64px));
  height: 112px;
  padding: 0;
  margin: 0;
  overflow: visible;
  pointer-events: auto;
  background: transparent;
  border: 0;
  box-shadow: none;
}

.feature-pages {
  display: flex;
  width: 100%;
  height: 112px;
  overflow-x: auto;
  overflow-y: visible;
  pointer-events: auto;
  scrollbar-width: none;
  scroll-snap-type: x mandatory;
}

.feature-pages::-webkit-scrollbar {
  display: none;
}

.feature-page {
  position: relative;
  flex: 0 0 100%;
  min-height: 100%;
  padding: 0 2px;
  box-sizing: border-box;
  scroll-snap-align: start;
}

.dock-item {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  width: 50px;
  min-width: 50px;
  height: 78px;
  gap: 0;
  padding: 0;
  color: #6366f1;
  cursor: pointer;
  background: transparent;
  border: 0;
  transition: transform 0.22s ease;
}

.dock-item:active {
  transform: scale(0.96);
}

.dock-item:nth-child(1) {
  left: 0;
  bottom: 34px;
}

.dock-item:nth-child(2) {
  left: 105px;
  bottom: 0;
}

.dock-item:nth-child(3) {
  right: 105px;
  bottom: 0;
}

.dock-item:nth-child(4) {
  right: 0;
  bottom: 34px;
}

.feature-bubble {
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  width: 50px;
  height: 50px;
  overflow: visible;
  color: #6366f1;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 50%;
  box-shadow: 0 12px 24px rgba(99, 102, 241, 0.12);
  backdrop-filter: blur(14px);
  transition: transform 0.22s ease, box-shadow 0.22s ease, background 0.22s ease, color 0.22s ease;
}

.feature-bubble :deep(.van-icon) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  overflow: visible;
  line-height: 1;
}

.feature-bubble :deep(.van-icon::before) {
  display: block;
  line-height: 1;
}

.dock-item.active .feature-bubble {
  color: #ffffff;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  box-shadow: 0 14px 28px rgba(99, 102, 241, 0.24);
  transform: scale(1.08);
}

.feature-name-pop {
  position: absolute;
  top: 56px;
  left: 50%;
  z-index: 10;
  padding: 5px 10px;
  color: #ffffff;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  white-space: nowrap;
  background: rgba(30, 41, 59, 0.86);
  border-radius: 999px;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.14);
  transform: translateX(-50%);
}

.dock-item:nth-child(1) .feature-name-pop {
  left: 0;
  transform: translateX(0);
}

.dock-item:nth-child(4) .feature-name-pop {
  right: 0;
  left: auto;
  transform: translateX(0);
}

.feature-dots {
  position: absolute;
  right: 0;
  bottom: -8px;
  left: 0;
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 0;
  pointer-events: auto;
}

.feature-dots span {
  width: 6px;
  height: 6px;
  background: rgba(148, 163, 184, 0.58);
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.feature-dots span.active {
  width: 18px;
  background: #6366f1;
}

@media (max-width: 375px) {
  .function-grid {
    bottom: 88px;
    width: min(330px, calc(100vw - 56px));
  }

  .dock-item {
    width: 50px;
    min-width: 50px;
    height: 84px;
  }

  .feature-bubble {
    width: 50px;
    height: 50px;
  }

  .dock-item:nth-child(1) {
    bottom: 32px;
  }

  .dock-item:nth-child(2) {
    left: 82px;
  }

  .dock-item:nth-child(3) {
    right: 82px;
  }

  .dock-item:nth-child(4) {
    bottom: 32px;
  }
}
</style>
