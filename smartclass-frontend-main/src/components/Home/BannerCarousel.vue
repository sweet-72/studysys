<template>
  <div class="banner-carousel">
    <van-swipe
      class="swipe"
      :autoplay="3000"
      indicator-color="rgba(255, 255, 255, 0.5)"
      :show-indicators="true"
      :loop="true"
      @change="onSwiperChange"
    >
      <van-swipe-item
        v-for="banner in banners"
        :key="banner.id"
        @click="onBannerClick(banner)"
      >
        <div class="banner-item">
          <img
            :src="banner.imageUrl"
            :alt="banner.title"
            class="banner-image"
          />
          <div class="banner-overlay">
            <div class="banner-content">
              <div class="ai-label">课程助手</div>
              <h2 class="hero-title">继续学习</h2>
              <p class="hero-subtitle">从推荐课程开始，让 AI 帮你保持节奏</p>
              <div class="hero-actions">
                <span>问 AI</span>
                <span>课程</span>
                <span>单词</span>
              </div>
              <h3 class="banner-title">{{ banner.title }}</h3>
              <p class="banner-description">{{ banner.description }}</p>
              <div class="banner-tag">
                <span class="tag" :style="{ backgroundColor: banner.tagColor }">
                  {{ banner.tag }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </van-swipe-item>
    </van-swipe>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { mockPopularCourses } from '../../api/mock';

interface Banner {
  id: number;
  title: string;
  description: string;
  imageUrl: string;
  tag: string;
  tagColor: string;
}

const router = useRouter();
const banners = ref<Banner[]>([]);

const onBannerClick = (banner: Banner) => {
  router.push('/courses');
};

const onSwiperChange = (index: number) => {
  // Reserved for analytics.
};

const generateBannersFromCourses = () => {
  const selectedCourses = mockPopularCourses
    .sort((a, b) => b.studentsCount - a.studentsCount)
    .slice(0, 5);

  banners.value = selectedCourses.map((course) => ({
    id: course.id,
    title: course.title,
    description: course.brief,
    imageUrl: course.cover,
    tag: course.tag,
    tagColor: course.tagColor,
  }));
};

onMounted(() => {
  generateBannersFromCourses();
});
</script>

<style scoped>
.banner-carousel {
  position: relative;
  margin: 0;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 20px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.12);
  backdrop-filter: blur(12px);
}

.swipe {
  height: 154px;
  border-radius: 20px;
}

:deep(.van-swipe__track) {
  height: 100%;
  align-items: stretch;
}

:deep(.van-swipe-item) {
  position: relative;
  height: 100%;
}

.banner-item {
  position: relative;
  display: block;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.banner-image {
  position: absolute;
  inset: 0;
  z-index: 0;
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.14;
  transition: transform 0.3s ease;
}

.banner-item:hover .banner-image {
  transform: scale(1.05);
}

.banner-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  display: flex;
  align-items: center;
  padding: 12px;
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.9), rgba(124, 58, 237, 0.76));
}

.banner-content {
  position: relative;
  z-index: 1;
  width: 100%;
  color: #fff;
}

.banner-content::after {
  position: absolute;
  top: -22px;
  right: -18px;
  display: flex;
  width: 58px;
  height: 58px;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.24);
  content: 'AI';
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 20px;
  font-size: 22px;
  font-weight: 800;
  transform: rotate(12deg);
}

.ai-label {
  display: inline-flex;
  height: 20px;
  align-items: center;
  padding: 0 8px;
  margin-bottom: 6px;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 999px;
  backdrop-filter: blur(8px);
}

.hero-title {
  margin: 0;
  color: #fff;
  font-size: 18px !important;
  line-height: 1.18;
  text-shadow: 0 2px 8px rgba(31, 42, 68, 0.18);
}

.hero-subtitle {
  display: -webkit-box;
  margin: 5px 0 8px;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.9);
  font-size: 11px !important;
  line-height: 1.35;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-bottom: 8px;
}

.hero-actions span {
  display: inline-flex;
  height: 24px;
  align-items: center;
  padding: 0 8px;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 999px;
  backdrop-filter: blur(8px);
}

.banner-title {
  display: -webkit-box;
  margin: 0 0 3px;
  overflow: hidden;
  font-size: 11px;
  font-weight: 600;
  line-height: 1.3;
  text-shadow: none;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
}

.banner-description {
  display: none;
}

.banner-tag {
  display: flex;
  align-items: center;
}

.tag {
  padding: 3px 8px;
  color: #fff;
  font-size: 11px;
  font-weight: 500;
  background-color: rgba(255, 255, 255, 0.22) !important;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

:deep(.van-swipe__indicator) {
  width: 6px;
  height: 6px;
  margin: 0 3px;
  background-color: rgba(255, 255, 255, 0.5);
  border-radius: 3px;
  transition: all 0.3s ease;
}

:deep(.van-swipe__indicator--active) {
  width: 18px;
  background-color: #fff;
  border-radius: 3px;
}

:deep(.van-swipe__indicators) {
  bottom: 8px;
}

@media (max-width: 375px) {
  .swipe {
    height: 148px;
  }

  .banner-overlay {
    padding: 10px;
  }

  .hero-title {
    font-size: 17px !important;
  }

  .hero-subtitle {
    font-size: 11px !important;
  }
}

@media (min-width: 414px) {
  .swipe {
    height: 158px;
  }
}
</style>
