<template>
  <article class="post-result-item" @click="$emit('click')">
    <div class="post-info">
      <h3 class="post-title" v-html="highlightText(title)"></h3>
      <p class="post-content" v-html="highlightText(summary)"></p>
      <div class="post-meta">
        <span class="author">{{ author }}</span>
        <span v-if="formatTime">{{ formatTime }}</span>
        <span><van-icon name="good-job-o" /> {{ thumbCount }}</span>
        <span><van-icon name="comment-o" /> {{ commentCount }}</span>
      </div>
    </div>
    <van-image
      v-if="cover"
      :src="cover"
      class="post-cover"
      fit="cover"
      radius="14px"
    />
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { formatDate } from '../../utils/dateUtils';

const props = defineProps<{
  post: any;
  keyword?: string;
}>();

defineEmits<{
  (e: 'click'): void;
}>();

const title = computed(() => props.post.title || '未命名帖子');
const summary = computed(() => {
  const content = props.post.content || props.post.summary || props.post.description || '';
  return content.length > 90 ? `${content.slice(0, 90)}...` : content;
});
const author = computed(() => props.post.userVO?.userName || props.post.userName || props.post.author || '匿名用户');
const thumbCount = computed(() => props.post.thumbNum ?? props.post.likeCount ?? props.post.likeNum ?? 0);
const commentCount = computed(() => props.post.commentNum ?? props.post.commentsNum ?? props.post.favourNum ?? 0);
const cover = computed(() => props.post.coverUrl || props.post.coverImage || props.post.imageUrl || '');
const formatTime = computed(() => props.post.createTime ? formatDate(props.post.createTime) : '');

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
.post-result-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.52);
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
  transition: transform 0.18s ease, background 0.18s ease;
}

.post-result-item:active {
  background: rgba(238, 242, 255, 0.78);
  transform: scale(0.99);
}

.post-info {
  flex: 1;
  min-width: 0;
}

.post-title {
  margin: 0 0 8px;
  overflow: hidden;
  color: #1e293b;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-content {
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

.post-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  align-items: center;
  color: #94a3b8;
  font-size: 12px;
  font-weight: 700;
}

.post-meta span {
  display: inline-flex;
  gap: 4px;
  align-items: center;
}

.author {
  color: #6366f1;
}

.post-cover {
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
