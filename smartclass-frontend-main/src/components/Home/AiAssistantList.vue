<template>
  <div class="ai-assistant-module">
    <div class="module-header">
      <div>
        <h2>AI小助手</h2>
      </div>
      <span class="more-link" @click="$emit('more')">更多</span>
    </div>

    <div class="assistant-list">
      <template v-if="rankedAssistants.length > 0">
        <div
          v-for="assistant in rankedAssistants"
          :key="assistant.id"
          class="assistant-card"
          :style="getCardStyle(assistant.rank)"
          @click="$emit('chat', assistant)"
        >
          <van-image
            :src="assistant.avatar"
            round
            width="48"
            height="48"
            :lazy-load="false"
            @error="handleImageError($event)"
          />
          <div class="assistant-info">
            <div class="assistant-name">{{ assistant.name }}</div>
            <div class="assistant-desc">
              {{ assistant.description || '今天也可以陪你拆解知识点。' }}
            </div>
          </div>
          <div class="chat-button">
            <van-icon name="arrow" />
          </div>
        </div>
        <div v-if="showLoadMore && assistants.length > displayLimit" class="load-more">
          <van-button size="small" type="default" @click="loadMore">查看更多</van-button>
        </div>
      </template>
      <template v-else>
        <div class="empty-assistant">
          <van-empty description="暂无智慧体" />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';

interface Assistant {
  id: number;
  name: string;
  description: string;
  avatar: string;
  meta?: string[];
}

type RankedAssistant = Assistant & {
  rank: number;
  rankLabel: string;
};

const props = defineProps<{
  assistants: Assistant[];
  showLoadMore?: boolean;
}>();

defineEmits<{
  (e: 'chat', assistant: Assistant): void;
  (e: 'more'): void;
  (e: 'loadMore'): void;
}>();

const displayLimit = ref(4);
const showAll = ref(false);

const displayAssistants = computed(() => {
  if (showAll.value) {
    return props.assistants;
  }

  return props.assistants.slice(0, displayLimit.value);
});

const rankedAssistants = computed<RankedAssistant[]>(() =>
  displayAssistants.value.map((assistant, index) => ({
    ...assistant,
    rank: index,
    rankLabel: index === 0 ? '高频' : index === 1 ? '常用' : '轻用',
  })),
);

const loadMore = () => {
  showAll.value = true;
};

const getCardStyle = (rank: number) => {
  const accents = ['#c7d2fe', '#fbcfe8', '#bbf7d0', '#dbeafe'];
  const accent = accents[rank % accents.length];

  return {
    '--card-accent': accent,
  };
};

const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement;
  target.src = '/avatar-default.png';
};
</script>

<style scoped>
.ai-assistant-module {
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
  margin-bottom: 12px;
}

.module-header h2 {
  margin: 0;
  color: #1e293b;
  font-size: 20px !important;
  font-weight: 800;
  line-height: 1.25;
}

.more-link {
  flex-shrink: 0;
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.assistant-list {
  display: flex;
  gap: 12px;
  padding: 0 18px 4px;
  margin: 0 -18px;
  overflow-x: auto;
  scrollbar-width: none;
  scroll-snap-type: x mandatory;
}

.assistant-list::-webkit-scrollbar {
  display: none;
}

.assistant-card {
  position: relative;
  display: flex;
  flex: 0 0 46%;
  min-width: 160px;
  min-height: 112px;
  align-items: center;
  gap: 12px;
  padding: 14px 12px;
  overflow: hidden;
  cursor: pointer;
  background:
    radial-gradient(circle at 20% 4%, var(--card-accent), transparent 62%),
    rgba(255, 255, 255, 0.58);
  border: 1px solid rgba(255, 255, 255, 0.66);
  border-radius: 22px;
  box-shadow: 0 14px 32px rgba(79, 70, 229, 0.08);
  backdrop-filter: blur(12px);
  scroll-snap-align: start;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.assistant-card:active {
  box-shadow: 0 10px 24px rgba(79, 70, 229, 0.1);
  transform: scale(0.98);
}

.assistant-card :deep(.van-image) {
  flex: 0 0 auto;
  border: 3px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 10px 18px rgba(79, 70, 229, 0.14);
}

.assistant-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.assistant-name {
  margin-bottom: 5px;
  color: #1e293b;
  font-size: 15px;
  font-weight: 800;
}

.assistant-desc {
  display: -webkit-box;
  overflow: hidden;
  color: #94a3b8;
  font-size: 12px;
  line-height: 1.45;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  line-clamp: 1;
}

.chat-button {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  color: #6366f1;
  background: rgba(255, 255, 255, 0.74);
  border-radius: 999px;
  box-shadow: 0 8px 18px rgba(79, 70, 229, 0.18);
}

.load-more {
  display: flex;
  justify-content: center;
  margin-top: 2px;
}

.empty-assistant {
  padding: 24px 0;
  text-align: center;
}
</style>
