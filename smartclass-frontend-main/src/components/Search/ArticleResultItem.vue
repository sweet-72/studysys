<template>
  <article class="article-result-item" @click="navigateToDetail">
    <div class="article-info">
      <h3 class="article-title" v-html="highlightText(title)"></h3>
      <p class="article-summary" v-html="highlightText(summaryText)"></p>
      <div class="article-meta">
        <span v-if="article.category" class="category">{{ article.category }}</span>
        <span v-if="difficultyText" class="difficulty">{{ difficultyText }}</span>
        <span v-if="article.readTime">{{ article.readTime }}分钟</span>
        <span v-if="publishDate">{{ publishDate }}</span>
      </div>
    </div>
    <van-image
      v-if="cover"
      :src="cover"
      class="article-cover"
      fit="cover"
      radius="14px"
    />
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { formatDate } from '../../utils/dateUtils';

const props = defineProps<{
  article: any;
  keyword?: string;
}>();

const router = useRouter();

const title = computed(() => props.article.title || '未命名文章');
const summaryText = computed(() => {
  const content = props.article.summary || props.article.brief || props.article.content || '';
  return content.length > 90 ? `${content.slice(0, 90)}...` : content;
});
const cover = computed(() => props.article.coverUrl || props.article.coverImage || props.article.cover || '');
const publishDate = computed(() => props.article.publishDate ? formatDate(props.article.publishDate) : '');
const difficultyText = computed(() => {
  const map: Record<number, string> = {
    1: '初级',
    2: '中级',
    3: '高级',
  };
  return map[Number(props.article.difficulty)] || '';
});

const navigateToDetail = (): void => {
  if (props.article?.id) {
    router.push(`/daily/article/${props.article.id}`);
  }
};

const escapeRegExp = (value: string): string => value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
const escapeHtml = (value: string): string => value
  .replace(/&/g, '&amp;')
  .replace(/</g, '&lt;')
  .replace(/>/g, '&gt;')
  .replace(/"/g, '&quot;')
  .replace(/'/g, '&#39;');

const highlightText = (value?: string): string => {
  const text = escapeHtml(value || '');
  const keyword = props.keyword?.trim();
  if (!keyword) return text;
  const reg = new RegExp(`(${escapeRegExp(keyword)})`, 'gi');
  return text.replace(reg, '<span class="highlight">$1</span>');
};
</script>

<style scoped>
.article-result-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.52);
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
  transition: transform 0.18s ease, background 0.18s ease;
}

.article-result-item:active {
  background: rgba(238, 242, 255, 0.78);
  transform: scale(0.99);
}

.article-info {
  flex: 1;
  min-width: 0;
}

.article-title {
  margin: 0 0 8px;
  overflow: hidden;
  color: #1e293b;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-summary {
  display: -webkit-box;
  margin: 0 0 12px;
  overflow: hidden;
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  color: #94a3b8;
  font-size: 12px;
  font-weight: 700;
}

.category,
.difficulty {
  padding: 3px 9px;
  color: #4f46e5;
  background: rgba(238, 242, 255, 0.9);
  border-radius: 999px;
}

.difficulty {
  color: #0891b2;
  background: rgba(207, 250, 254, 0.78);
}

.article-cover {
  flex: 0 0 auto;
  width: 78px;
  height: 78px;
}

:deep(.highlight) {
  padding: 0 2px;
  color: #7c3aed;
  background: rgba(253, 224, 71, 0.45);
  border-radius: 5px;
}
</style>
