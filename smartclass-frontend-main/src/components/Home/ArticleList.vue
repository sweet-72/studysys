<template>
  <div class="article-module">
    <div class="module-header">
      <div>
        <h3>每日美文</h3>
      </div>
      <span class="more-link" @click="$emit('more')">更多</span>
    </div>

    <div class="article-list">
      <div
        v-for="article in articles"
        :key="article.id"
        class="article-item"
        @click="showArticleDetail(article)"
      >
        <div class="article-cover">
          <van-image :src="article.cover" fit="cover" radius="0" />
        </div>
        <div class="article-info">
          <h3 class="article-title">{{ article.title }}</h3>
          <p class="article-brief">{{ article.brief }}</p>
          <div class="article-meta">
            <span>{{ article.readTime }} 分钟</span>
            <span>{{ convertDifficultyToText(article.difficulty) }}</span>
            <span v-if="article.author">{{ article.author }}</span>
          </div>
        </div>
        <van-icon name="arrow" class="article-arrow" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

interface Article {
  id: number;
  title: string;
  brief: string;
  cover: string;
  category: string;
  readTime: number;
  difficulty: number | string;
  content: string;
  tags?: string[];
  author?: string;
  source?: string;
  publishDate?: string;
  viewCount?: number;
  likeCount?: number;
}

defineProps<{
  articles: Article[];
}>();

defineEmits<{
  (e: 'more'): void;
}>();

const router = useRouter();

const showArticleDetail = (article: Article): void => {
  if (article?.id) {
    router.push(`/daily/article/${article.id}`);
  }
};

const convertDifficultyToText = (difficulty: number | string): string => {
  if (typeof difficulty === 'string') {
    return difficulty;
  }

  const difficultyMap: Record<number, string> = {
    1: '初级',
    2: '中级',
    3: '高级',
  };

  return difficultyMap[difficulty] || '未知';
};
</script>

<style scoped>
.article-module {
  margin-bottom: 0;
  overflow: visible;
  background: transparent;
  border: none;
  border-radius: 0;
  box-shadow: none;
}

.module-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.module-header h3 {
  margin: 0;
  color: #1e293b;
  font-size: 16px;
  line-height: 1.25;
}

.more-link {
  color: #6366f1;
  font-size: 13px;
  font-weight: 700;
}

.article-list {
  padding: 0;
}

.article-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 92px;
  padding: 10px 38px 10px 10px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(255, 255, 255, 0.62);
  border-radius: 22px;
  box-shadow: 0 12px 28px rgba(79, 70, 229, 0.06);
  backdrop-filter: blur(12px);
}

.article-item:active {
  transform: scale(0.98);
}

.article-cover {
  position: relative;
  flex: 0 0 86px;
  width: 86px;
  height: 70px;
  overflow: hidden;
  border-radius: 16px;
}

.article-cover .van-image {
  width: 100%;
  height: 100%;
}

.article-info {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
}

.article-title {
  display: -webkit-box;
  margin: 0 0 4px;
  overflow: hidden;
  color: #1e293b;
  font-size: 15px;
  font-weight: 800;
  line-height: 1.35;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  line-clamp: 1;
}

.article-brief {
  display: -webkit-box;
  margin: 0 0 6px;
  overflow: hidden;
  color: #94a3b8;
  font-size: 12px;
  line-height: 1.45;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  line-clamp: 1;
}

.article-meta {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  color: #94a3b8;
  font-size: 12px;
  white-space: nowrap;
  scrollbar-width: none;
}

.article-meta::-webkit-scrollbar {
  display: none;
}

.article-arrow {
  position: absolute;
  right: 12px;
  color: #94a3b8;
}
</style>
