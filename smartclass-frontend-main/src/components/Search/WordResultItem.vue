<template>
  <article class="word-result-item" @click="openDetail">
    <div class="word-info">
      <div class="word-title-row">
        <h3 class="word-title" v-html="highlightText(wordText)"></h3>
        <van-icon
          :name="isCollected ? 'star' : 'star-o'"
          :class="['collect-icon', { collected: isCollected }]"
          @click.stop="toggleCollect"
        />
      </div>
      <p v-if="phonetic" class="word-phonetic">/{{ phonetic }}/</p>
      <p class="word-translation" v-html="highlightText(translation)"></p>
      <p v-if="word.example" class="word-example" v-html="highlightText(word.example)"></p>
      <div class="word-meta">
        <span v-if="word.category">{{ word.category }}</span>
        <span v-if="difficultyText">{{ difficultyText }}</span>
        <span v-if="publishDate">{{ publishDate }}</span>
      </div>
    </div>
  </article>

  <van-popup
    v-model:show="showWordPopup"
    round
    position="bottom"
    :style="{ height: '70%' }"
  >
    <div class="word-detail" v-if="wordDetail">
      <div class="popup-header">
        <span>单词详情</span>
        <van-icon name="cross" @click="showWordPopup = false" />
      </div>
      <div class="detail-body">
        <div class="detail-title-row">
          <div>
            <h2>{{ detailWordText }}</h2>
            <p v-if="detailPhonetic">/{{ detailPhonetic }}/</p>
          </div>
          <div class="detail-actions">
            <van-icon
              v-if="wordDetail.audioUrl"
              name="volume-o"
              @click="playPronunciation(wordDetail.audioUrl)"
            />
            <van-icon
              :name="isCollected ? 'star' : 'star-o'"
              :class="['collect-icon', { collected: isCollected }]"
              @click="toggleCollect"
            />
          </div>
        </div>

        <section class="detail-card">
          <h3>释义</h3>
          <p>{{ wordDetail.translation || translation }}</p>
        </section>

        <section v-if="wordDetail.example" class="detail-card">
          <h3>例句</h3>
          <p>{{ wordDetail.example }}</p>
          <p v-if="wordDetail.exampleTranslation" class="example-translation">
            {{ wordDetail.exampleTranslation }}
          </p>
        </section>

        <section v-if="wordDetail.notes" class="detail-card">
          <h3>笔记</h3>
          <p>{{ wordDetail.notes }}</p>
        </section>
      </div>
    </div>
  </van-popup>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { showToast } from 'vant';
import { formatDate } from '../../utils/dateUtils';
import { DailyWordControllerService, UserWordBookControllerService } from '../../services';

const props = defineProps<{
  word: any;
  keyword?: string;
}>();

const showWordPopup = ref(false);
const wordDetail = ref<any>(null);
const isCollected = ref(Boolean(props.word?.isCollected));
const isCollecting = ref(false);

const wordId = computed(() => Number(props.word?.id || props.word?.wordId || 0));
const wordText = computed(() => props.word?.word || props.word?.text || '');
const phonetic = computed(() => props.word?.pronunciation || props.word?.phonetic || '');
const translation = computed(() => props.word?.translation || '');
const publishDate = computed(() => props.word?.publishDate ? formatDate(props.word.publishDate) : '');
const difficultyText = computed(() => getDifficultyTextById(props.word?.difficulty));
const detailWordText = computed(() => wordDetail.value?.word || wordDetail.value?.text || wordText.value);
const detailPhonetic = computed(() => wordDetail.value?.pronunciation || wordDetail.value?.phonetic || phonetic.value);

const getDifficultyTextById = (difficultyId?: number | string): string => {
  const map: Record<number, string> = {
    1: '初级',
    2: '中级',
    3: '高级',
  };
  return map[Number(difficultyId)] || '';
};

const checkWordCollectionStatus = async (): Promise<void> => {
  if (!wordId.value) return;

  try {
    const response = await UserWordBookControllerService.isWordInUserBookUsingGet(wordId.value);
    if (response.code === 0 && response.data !== undefined) {
      isCollected.value = response.data === true;
    }
  } catch (error) {
    // 状态检查失败不阻塞搜索结果展示
  }
};

const openDetail = async (): Promise<void> => {
  if (!wordId.value) return;

  showWordPopup.value = true;
  wordDetail.value = {
    ...props.word,
    isCollected: isCollected.value,
  };

  try {
    const response = await DailyWordControllerService.getDailyWordVoByIdUsingGet(wordId.value);
    if (response.code === 0 && response.data) {
      wordDetail.value = {
        ...response.data,
        isCollected: isCollected.value,
      };
    }
    await checkWordCollectionStatus();
  } catch (error) {
    showToast('获取单词详情失败，请稍后重试');
  }
};

const toggleCollect = async (): Promise<void> => {
  if (!wordId.value || isCollecting.value) return;
  isCollecting.value = true;

  try {
    const response = isCollected.value
      ? await UserWordBookControllerService.removeFromWordBookUsingDelete(wordId.value)
      : await UserWordBookControllerService.addToWordBookUsingPost({
        wordId: wordId.value,
        difficulty: Number(props.word?.difficulty) || undefined,
      });

    if (response.code === 0) {
      isCollected.value = !isCollected.value;
      if (wordDetail.value) {
        wordDetail.value.isCollected = isCollected.value;
      }
      showToast(isCollected.value ? '已加入生词本' : '已从生词本移除');
    } else {
      showToast(response.message || '操作失败，请稍后重试');
    }
  } catch (error) {
    showToast('操作失败，请稍后重试');
  } finally {
    isCollecting.value = false;
  }
};

const playPronunciation = (url: string): void => {
  const audio = new Audio(url);
  audio.play().catch(() => {
    showToast('音频播放失败');
  });
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

watch(
  () => wordId.value,
  () => {
    isCollected.value = Boolean(props.word?.isCollected);
    checkWordCollectionStatus();
  },
);

onMounted(() => {
  checkWordCollectionStatus();
});
</script>

<style scoped>
.word-result-item {
  padding: 16px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.52);
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
  transition: transform 0.18s ease, background 0.18s ease;
}

.word-result-item:active {
  background: rgba(238, 242, 255, 0.78);
  transform: scale(0.99);
}

.word-title-row {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: space-between;
}

.word-title {
  margin: 0;
  color: #1e293b;
  font-size: 22px;
  font-weight: 900;
}

.collect-icon {
  flex: 0 0 auto;
  color: #94a3b8;
  font-size: 22px;
}

.collect-icon.collected {
  color: #f6c453;
}

.word-phonetic {
  margin: 5px 0 8px;
  color: #8b5cf6;
  font-size: 13px;
  font-weight: 700;
}

.word-translation {
  margin: 0 0 10px;
  color: #334155;
  font-size: 15px;
  line-height: 1.6;
}

.word-example {
  margin: 0 0 12px;
  padding-left: 10px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
  border-left: 3px solid rgba(96, 165, 250, 0.62);
}

.word-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.word-meta span {
  padding: 4px 9px;
  color: #4f46e5;
  font-size: 12px;
  font-weight: 800;
  background: rgba(238, 242, 255, 0.88);
  border-radius: 999px;
}

.word-detail {
  height: 100%;
  overflow: hidden;
  background:
    radial-gradient(circle at 20% 0%, rgba(167, 243, 208, 0.22), transparent 30%),
    linear-gradient(180deg, #f8f7ff, #ffffff);
}

.popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  color: #1e293b;
  font-size: 16px;
  font-weight: 900;
  border-bottom: 1px solid rgba(148, 163, 184, 0.16);
}

.detail-body {
  height: calc(100% - 57px);
  padding: 18px 16px 28px;
  overflow-y: auto;
}

.detail-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
}

.detail-title-row h2 {
  margin: 0 0 6px;
  color: #1e293b;
  font-size: 28px;
}

.detail-title-row p {
  margin: 0;
  color: #8b5cf6;
  font-weight: 800;
}

.detail-actions {
  display: flex;
  gap: 12px;
  color: #6366f1;
  font-size: 22px;
}

.detail-card {
  padding: 14px;
  margin-bottom: 12px;
  background: rgba(255, 255, 255, 0.74);
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 18px;
}

.detail-card h3 {
  margin: 0 0 8px;
  color: #475569;
  font-size: 13px;
}

.detail-card p {
  margin: 0;
  color: #1e293b;
  font-size: 15px;
  line-height: 1.7;
}

.example-translation {
  margin-top: 8px !important;
  color: #64748b !important;
}

:deep(.highlight) {
  padding: 0 2px;
  color: #7c3aed;
  background: rgba(253, 224, 71, 0.45);
  border-radius: 5px;
}
</style>
