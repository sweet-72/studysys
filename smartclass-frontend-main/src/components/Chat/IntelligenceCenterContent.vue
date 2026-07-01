<template>
  <div class="intelligence-center-content">
    <div
      v-if="!loading && !error && assistants.length > 0"
      class="assistant-bento"
    >
      <button
        v-for="(assistant, index) in assistants"
        :key="assistant.id"
        type="button"
        class="assistant-tile"
        :class="`tile-${index % 6}`"
        @click="handleAssistantSelect(assistant)"
      >
        <div class="tile-glow"></div>
        <div class="tile-top">
          <van-image
            :src="assistant.avatar"
            round
            width="54"
            height="54"
            fit="cover"
          />
          <van-icon name="chat-o" class="tile-icon" />
        </div>
        <div class="tile-body">
          <h3>{{ assistant.assistantName }}</h3>
          <p>{{ assistant.summary || '陪你拆解知识点和学习任务' }}</p>
        </div>
        <div class="ability-tags">
          <span
            v-for="tag in displayTags(assistant, index)"
            :key="tag"
          >
            {{ tag }}
          </span>
        </div>
      </button>
    </div>

    <div v-if="loading" class="loading-container">
      <van-loading type="spinner" size="32" color="#4f46e5" />
      <p>正在加载智慧体列表...</p>
    </div>

    <network-error
      v-if="error"
      :message="error"
      :loading="retryLoading"
      @retry="retryLoadData"
    />

    <div v-if="!loading && assistants.length === 0 && !error" class="empty-container">
      <van-empty description="暂无智慧体数据" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineEmits, onMounted, ref, watch } from 'vue';
import { showToast } from 'vant';
import { NetworkError } from '../Common';
import {
  queryAiAvatarPage,
  resolveAiAvatarImage,
  type AiAvatarInfo,
} from '../../api/aiAvatar';

interface Assistant {
  id: number;
  assistantId: number;
  assistantName: string;
  avatar: string;
  lastMessage: string;
  summary?: string;
  lastTime: string;
  online?: boolean;
  tags?: string[];
  type: number;
}

const props = withDefaults(
  defineProps<{
    keyword?: string;
  }>(),
  {
    keyword: '',
  },
);

const emit = defineEmits<{
  (e: 'select', assistantId: number): void;
}>();

const assistants = ref<Assistant[]>([]);
const loading = ref(false);
const retryLoading = ref(false);
const error = ref('');

const fallbackTagGroups = [
  ['阅读理解', '作文润色'],
  ['口语训练', '语法纠错'],
  ['解题思路', '公式推导'],
  ['力学分析', '实验梳理'],
  ['学习规划', '错题复盘'],
  ['知识问答', '陪练巩固'],
];

const buildTags = (avatar: AiAvatarInfo): string[] => {
  const tags = avatar.tags
    ? avatar.tags
        .split(',')
        .map((tag) => tag.trim())
        .filter(Boolean)
    : [];

  if (typeof avatar.rating === 'number') {
    tags.push(
      typeof avatar.ratingCount === 'number' && avatar.ratingCount > 0
        ? `评分 ${avatar.rating.toFixed(1)}`
        : `评分 ${avatar.rating.toFixed(1)}`,
    );
  }

  if (typeof avatar.usageCount === 'number') {
    tags.push(`使用 ${avatar.usageCount}`);
  }

  return tags;
};

const displayTags = (assistant: Assistant, index: number) => {
  const tags = assistant.tags?.filter(Boolean) || [];
  return (tags.length ? tags : fallbackTagGroups[index % fallbackTagGroups.length]).slice(0, 2);
};

const loadAiAvatars = async () => {
  loading.value = true;
  error.value = '';

  try {
    const page = await queryAiAvatarPage({
      current: 1,
      pageSize: 20,
      keyword: props.keyword,
    });

    assistants.value = (page.records || []).map((avatar) => ({
      id: avatar.id || 0,
      assistantId: avatar.id || 0,
      assistantName: avatar.name || '未命名智慧体',
      avatar: resolveAiAvatarImage(avatar.avatarImgUrl),
      lastMessage: '',
      summary: avatar.description || '',
      lastTime: '',
      tags: buildTags(avatar),
      type: avatar.status || 1,
    }));
  } catch (err) {
    error.value = '获取智慧体列表失败，请检查网络连接';
    showToast(err instanceof Error ? err.message : '获取智慧体列表失败');
  } finally {
    loading.value = false;
  }
};

const retryLoadData = () => {
  retryLoading.value = true;
  loadAiAvatars().finally(() => {
    retryLoading.value = false;
  });
};

const handleAssistantSelect = (assistant: Assistant) => {
  emit('select', assistant.assistantId);
};

onMounted(() => {
  loadAiAvatars();
});

watch(
  () => props.keyword,
  () => {
    loadAiAvatars();
  },
);
</script>

<style scoped>
.intelligence-center-content {
  position: relative;
  width: 100%;
  min-height: 200px;
}

.assistant-bento {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  animation: fadeInUp 0.28s ease both;
}

.assistant-tile {
  position: relative;
  min-height: 178px;
  padding: 14px;
  overflow: hidden;
  text-align: left;
  cursor: pointer;
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 20px;
  box-shadow: 0 16px 36px rgba(79, 70, 229, 0.18);
  backdrop-filter: blur(20px);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.assistant-tile:hover,
.assistant-tile:active {
  box-shadow: 0 18px 42px rgba(79, 70, 229, 0.24);
  transform: translateY(-3px);
}

.tile-0 {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.78), rgba(96, 165, 250, 0.66));
}

.tile-1 {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.72), rgba(96, 165, 250, 0.62));
}

.tile-2 {
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.74), rgba(167, 243, 208, 0.58));
}

.tile-3 {
  background: linear-gradient(135deg, rgba(96, 165, 250, 0.72), rgba(139, 92, 246, 0.64));
}

.tile-4 {
  background: linear-gradient(135deg, rgba(20, 184, 166, 0.66), rgba(99, 102, 241, 0.66));
}

.tile-5 {
  background: linear-gradient(135deg, rgba(129, 140, 248, 0.72), rgba(45, 212, 191, 0.58));
}

.tile-glow {
  position: absolute;
  top: -44px;
  right: -42px;
  width: 120px;
  height: 120px;
  pointer-events: none;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  filter: blur(12px);
}

.tile-top {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tile-top :deep(.van-image) {
  border: 2px solid rgba(255, 255, 255, 0.66);
  box-shadow: 0 10px 24px rgba(30, 41, 59, 0.16);
}

.tile-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  color: #fff;
  background: rgba(255, 255, 255, 0.18);
  border-radius: 999px;
}

.tile-body {
  position: relative;
  z-index: 1;
  margin-top: 14px;
}

.tile-body h3 {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: #fff;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.3;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  line-clamp: 1;
}

.tile-body p {
  display: -webkit-box;
  margin: 7px 0 0;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.78);
  font-size: 12px;
  line-height: 1.45;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
}

.ability-tags {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 13px;
}

.ability-tags span {
  padding: 4px 8px;
  color: rgba(255, 255, 255, 0.92);
  font-size: 11px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 999px;
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  margin-top: 20px;
}

.loading-container p {
  margin-top: 12px;
  color: #64748b;
  font-size: var(--font-size-md);
  font-family: 'Noto Sans SC', sans-serif;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
