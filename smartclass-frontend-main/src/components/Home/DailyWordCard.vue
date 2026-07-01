<template>
  <div>
    <van-cell-group class="word-module">
      <van-cell title="每日单词">
        <template #icon>
          <svg class="icon svg-icon word-icon" aria-hidden="true">
            <use xlink:href="#icon-yingyu"></use>
          </svg>
        </template>
        <template #right-icon>
          <div class="right-actions">
            <div
              class="vocabulary-btn"
              @click.stop="showWordBookStats"
            >
              <van-icon name="bookmark-o" class="vocabulary-icon" />
              <span class="vocabulary-text">生词本</span>
              <span v-if="wordBookStats.length > 0" class="vocabulary-count">{{ wordBookStats[0] }}</span>
            </div>
            <span class="more-link" @click="$emit('more')">更多</span>
          </div>
        </template>
      </van-cell>

      <!-- 每日单词单卡片轮播 -->
      <div class="daily-words-container">
        <div 
          v-if="currentWord"
          :key="currentWord.id || currentWordIndex"
          class="daily-word-item active-word"
          @click="showWordDetail(currentWordIndex)"
        >
          <div class="word-card-top">
            <span>{{ currentWordIndex + 1 }} / {{ words.length }}</span>
          </div>
          <div class="word-content">
            <div class="word-header">
              <span class="word-text">{{ currentWord.text }}</span>
            </div>
            <div class="word-phonetic">
              <span class="phonetic-text">/{{ currentWord.phonetic }}/</span>
              <van-icon
                name="volume-o"
                class="audio-icon"
                @click.stop="playAudio(currentWord.audioUrl)"
                v-if="currentWord.audioUrl"
              />
            </div>
            <div class="word-translation">{{ currentWord.translation }}</div>
            <div class="word-info">
              <span class="word-category" v-if="currentWord.category">{{ currentWord.category }}</span>
              <span class="word-difficulty" v-if="currentWord.difficulty">{{ currentWord.difficulty }}</span>
            </div>
            <div class="word-example-wrapper">
              <div class="word-example">{{ currentWord.example }}</div>
              <div
                class="word-example-translation"
                v-if="currentWord.exampleTranslation"
              >
                {{ currentWord.exampleTranslation }}
              </div>
            </div>
            <div class="word-card-actions">
              <van-icon name="arrow-left" class="word-nav-btn" @click.stop="switchWord(-1)" />
              <div class="header-actions">
                <div class="thumb-action" :class="{ thumbed: currentWord.isThumbUp }">
                  <van-icon
                    :name="currentWord.isThumbUp ? 'good-job' : 'good-job-o'"
                    :class="['thumb-icon', { thumbed: currentWord.isThumbUp }]"
                    @click.stop="toggleThumbUp(currentWordIndex)"
                  />
                  <span
                    class="thumb-number"
                    :class="{ thumbed: currentWord.isThumbUp }"
                  >{{ currentWord.likeCount || 0 }}</span>
                </div>
                <div
                  class="collect-action"
                  :class="{ collected: currentWord.isCollected }"
                >
                  <van-icon
                    :name="currentWord.isCollected ? 'star' : 'star-o'"
                    :class="['collect-icon', { collected: currentWord.isCollected }]"
                    @click.stop="toggleCollect(currentWordIndex)"
                  />
                </div>
                <van-icon
                  v-if="currentWord.id"
                  name="success"
                  class="study-icon"
                  :class="{ studied: currentWord.isStudied }"
                  :loading="isMarkingStudied"
                  @click.stop="markAsStudied(currentWordIndex)"
                />
              </div>
              <van-icon name="arrow" class="word-nav-btn" @click.stop="switchWord(1)" />
            </div>
            <div v-if="isAutoSwitchActive" class="auto-progress">
              <span :key="progressKey"></span>
            </div>
          </div>
        </div>
      </div>
      
      <template v-if="!words || words.length === 0">
        <div class="empty-word">
          <van-empty description="暂无单词" />
        </div>
      </template>
    </van-cell-group>

    <!-- 单词详情弹出层 -->
    <van-popup
      v-model:show="showWordPopup"
      round
      position="bottom"
      :style="{ height: '70%' }"
    >
      <div class="word-detail">
        <div class="popup-header">
          <span class="title">单词详情</span>
          <van-icon name="cross" @click="showWordPopup = false" />
        </div>
        <div class="word-content" v-if="currentWord">
          <div class="word-main">
            <div class="word-title">
              <span class="word-text">{{ currentWord.text }}</span>
              <van-icon
                name="volume-o"
                class="audio-icon"
                @click="playAudio(currentWord.audioUrl)"
                v-if="currentWord.audioUrl"
              />
            </div>
            <div class="action-icons">
              <div class="thumb-action" :class="{ thumbed: currentWord.isThumbUp }">
                <van-icon
                  :name="currentWord.isThumbUp ? 'good-job' : 'good-job-o'"
                  :class="['thumb-icon', { thumbed: currentWord.isThumbUp }]"
                  @click="toggleThumbUp"
                />
                <span
                  class="thumb-number"
                  :class="{ thumbed: currentWord.isThumbUp }"
                >{{ currentWord.likeCount || 0 }}</span>
              </div>
              <div
                class="collect-action"
                :class="{ collected: currentWord.isCollected }"
              >
                <van-icon
                  :name="currentWord.isCollected ? 'star' : 'star-o'"
                  :class="['collect-icon', { collected: currentWord.isCollected }]"
                  @click="toggleCollect"
                />
              </div>
            </div>
          </div>
          <div class="word-phonetic">/{{ currentWord.phonetic }}/</div>
          <div class="word-info-detail">
            <span class="tag category-tag" v-if="currentWord.category">{{
              currentWord.category
            }}</span>
            <span class="tag difficulty-tag" v-if="currentWord.difficulty">{{
              currentWord.difficulty
            }}</span>
          </div>
          <div class="word-translation detail-item">
            <div class="item-label">释义</div>
            <div class="item-content">{{ currentWord.translation }}</div>
          </div>
          <div class="word-example-detail detail-item">
            <div class="item-label">例句</div>
            <div class="item-content">{{ currentWord.example }}</div>
            <div
              class="item-content example-translation"
              v-if="currentWord.exampleTranslation"
            >
              {{ currentWord.exampleTranslation }}
            </div>
          </div>

          <!-- 掌握程度设置 -->
          <div
            class="word-mastery detail-item"
            v-if="currentWord.isCollected && currentWord.id"
          >
            <div class="item-label">掌握程度</div>
            <div class="mastery-slider">
              <van-slider
                v-model="masteryLevel"
                :min="1"
                :max="3"
                :step="1"
                bar-height="4px"
                active-color="#1989fa"
                inactive-color="#e8eaec"
                @change="updateMasteryLevel"
              >
                <template #button>
                  <div class="mastery-button">{{ masteryLevel }}</div>
                </template>
              </van-slider>
              <div class="mastery-progress">
                <div
                  class="mastery-level"
                  :class="{ active: masteryLevel >= 1 }"
                >
                  生疏
                </div>
                <div
                  class="mastery-level"
                  :class="{ active: masteryLevel >= 2 }"
                >
                  一般
                </div>
                <div
                  class="mastery-level"
                  :class="{ active: masteryLevel >= 3 }"
                >
                  掌握
                </div>
              </div>
            </div>
          </div>

          <!-- 个人笔记 -->
          <div class="word-notes detail-item">
            <div class="item-label-with-action">
              <span>笔记</span>
              <van-button
                v-if="isEditingNote"
                size="mini"
                type="primary"
                plain
                @click="saveWordNote"
                :loading="isSavingNote"
              >
                保存
              </van-button>
              <van-icon
                v-else
                name="edit"
                class="edit-icon"
                @click="startEditingNote"
              />
            </div>
            <div v-if="isEditingNote" class="note-editor">
              <van-field
                v-model="noteContent"
                type="textarea"
                rows="3"
                placeholder="添加个人笔记..."
                class="note-textarea"
              />
            </div>
            <div v-else-if="currentWord.notes" class="item-content notes-content">
              {{ currentWord.notes }}
            </div>
            <div v-else class="empty-note" @click="startEditingNote">
              点击添加笔记
            </div>
          </div>

          <!-- 其他单词意思 -->
          <div
            class="word-meanings"
            v-if="currentWord.meanings && currentWord.meanings.length > 0"
          >
            <div
              class="meaning-item"
              v-for="(meaning, index) in currentWord.meanings"
              :key="index"
            >
              <div class="part-of-speech">{{ meaning.partOfSpeech }}</div>
              <div class="definition">{{ meaning.definition }}</div>
              <div class="example">{{ meaning.example }}</div>
            </div>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { showToast } from 'vant';
import {
  DailyWordLearningControllerService,
  DailyWordThumbControllerService,
  UserWordBookControllerService
} from '../../services';

interface WordMeaning {
  partOfSpeech: string;
  definition: string;
  example: string;
}

interface Word {
  id: number;
  text: string;
  phonetic: string;
  translation: string;
  example: string;
  isCollected: boolean;
  isThumbUp: boolean;
  thumbCount: number;
  likeCount: number;
  meanings: WordMeaning[];
  viewCount: number;
  collectCount: number;
  lastViewTime: string;
  difficulty: string;
  category: string;
  audioUrl?: string;
  exampleTranslation?: string;
  notes?: string;
  // UserWordBook相关属性
  learningStatus?: number;  // 学习状态
  isCollectedNumber?: number; // 后端返回的收藏状态（0或1）
  difficultyNumber?: number; // 后端返回的难度（数字）
  wordId?: number; // 单词ID
  pronunciation?: string; // 发音URL
  collectedTime?: string; // 收藏时间
  isStudied?: boolean; // 是否已学习
}

// 定义 props
const props = defineProps<{
  word: Word | Word[];
}>();

// 定义事件
const emit = defineEmits<{
  (e: 'update:word', word: Word | Word[]): void;
  (e: 'more'): void;
}>();

const showWordPopup = ref(false);

// 笔记编辑状态
const isEditingNote = ref(false);
const noteContent = ref('');
const isSavingNote = ref(false);

// 点赞状态
const isThumbUping = ref(false);

// 掌握程度
const masteryLevel = ref(1);
const isMarkingStudied = ref(false);
const isCollecting = ref(false);

// 生词本统计数据
const wordBookStats = ref<number[]>([]);

// 获取生词本统计信息
const getWordBookStats = async (): Promise<void> => {
  try {
    const response = await UserWordBookControllerService.getUserWordBookStatisticsUsingGet();
    if (response.code === 0 && response.data) {
      wordBookStats.value = response.data;
    }
  } catch (error) {
    console.error('获取生词本统计失败', error);
  }
};

// 显示生词本统计信息并跳转
const showWordBookStats = (): void => {
  // 如果有统计数据，显示提示
  if (wordBookStats.value.length > 0) {
    const totalWords = wordBookStats.value[0] || 0;
    const studiedWords = wordBookStats.value[1] || 0;
    const collectedWords = wordBookStats.value[2] || 0;
    
    showToast({
      message: `词汇量：${totalWords}，已学习：${studiedWords}，收藏：${collectedWords}`,
      position: 'bottom',
      duration: 2000,
    });
  }
  
  // 直接跳转到生词本页面
  emit('more');
};

// 将单个单词或单词数组统一处理为数组
const words = computed(() => {
  if (Array.isArray(props.word)) {
    return props.word;
  }
  return props.word ? [props.word] : [];
});

const currentWordIndex = ref(0);
const progressKey = ref(0);
const isAutoSwitchActive = ref(false);
let autoSwitchTimer: ReturnType<typeof window.setInterval> | null = null;

// 当前显示的单词
const currentWord = computed(() => {
  return words.value[currentWordIndex.value] || null;
});

const updateWordAt = (index: number, updatedWord: Word): void => {
  if (Array.isArray(props.word)) {
    const updatedWords = [...props.word];
    updatedWords[index] = updatedWord;
    emit('update:word', updatedWords);
    return;
  }

  emit('update:word', updatedWord);
};

const clearAutoSwitch = (): void => {
  if (autoSwitchTimer) {
    clearInterval(autoSwitchTimer);
    autoSwitchTimer = null;
  }
  isAutoSwitchActive.value = false;
};

const startAutoSwitch = (): void => {
  clearAutoSwitch();
  if (words.value.length <= 1) return;

  progressKey.value += 1;
  isAutoSwitchActive.value = true;
  autoSwitchTimer = window.setInterval(() => {
    currentWordIndex.value = (currentWordIndex.value + 1) % words.value.length;
    startAutoSwitch();
  }, 4000);
};

const pauseAutoSwitch = (): void => {
  clearAutoSwitch();
};

const switchWord = (step: number): void => {
  if (!words.value.length) return;
  currentWordIndex.value =
    (currentWordIndex.value + step + words.value.length) % words.value.length;
  startAutoSwitch();
};

// 显示单词详情
const showWordDetail = (index: number): void => {
  pauseAutoSwitch();
  currentWordIndex.value = index;
  showWordPopup.value = true;
};

// 检查单词是否已在生词本中
const checkWordFavourStatus = async (index = currentWordIndex.value): Promise<void> => {
  const wordItem = words.value[index];
  if (!wordItem?.id) return;

  try {
    const response = await UserWordBookControllerService.isWordInUserBookUsingGet(wordItem.id);

    if (response.code === 0 && response.data !== undefined && wordItem.isCollected !== response.data) {
      updateWordAt(index, {
        ...wordItem,
        isCollected: response.data,
      });
    }
  } catch (error) {
    console.error('检查单词收藏状态失败', error);
  }
};

const checkAllWordFavourStatus = async (): Promise<void> => {
  await Promise.all(words.value.map((_, index) => checkWordFavourStatus(index)));
};

// 检查单词是否已被点赞
const checkWordThumbStatus = async (index = currentWordIndex.value): Promise<void> => {
  const wordItem = words.value[index];
  if (!wordItem?.id) return;

  try {
    const response = await DailyWordThumbControllerService.isThumbWordUsingGet(
      wordItem.id,
    );

    if (response.code === 0 && response.data !== undefined) {
      // 如果当前点赞状态与后端不一致，更新本地状态
      if (wordItem.isThumbUp !== response.data) {
        const updatedWord: Word = {
          ...wordItem,
          isThumbUp: response.data,
          // 保持原有的likeCount
          likeCount: wordItem.likeCount || 0,
        };
        updateWordAt(index, updatedWord);
      }
    }
  } catch (error) {
    console.error('检查单词点赞状态失败', error);
  }
};

const checkAllWordThumbStatus = async (): Promise<void> => {
  await Promise.all(words.value.map((_, index) => checkWordThumbStatus(index)));
};

// 检查单词是否已被学习
const checkWordStudiedStatus = async (index = currentWordIndex.value): Promise<void> => {
  const wordItem = words.value[index];
  if (!wordItem?.id) return;

  try {
    // 从本地存储判断
    const studiedWords = JSON.parse(
      localStorage.getItem('studiedWords') || '{}',
    );
    const isStudied = studiedWords[wordItem.id] === true;

    // 如果当前学习状态与存储不一致，更新本地状态
    if (wordItem.isStudied !== isStudied) {
      const updatedWord: Word = {
        ...wordItem,
        isStudied: isStudied,
      };
      updateWordAt(index, updatedWord);
    }
  } catch (error) {
    console.error('检查单词学习状态失败', error);
  }
};

const checkAllWordStudiedStatus = async (): Promise<void> => {
  await Promise.all(words.value.map((_, index) => checkWordStudiedStatus(index)));
};

// 收藏/取消收藏单词
const toggleCollect = async (index: number = currentWordIndex.value): Promise<void> => {
  pauseAutoSwitch();
  // 获取当前点击的单词
  const wordItem = words.value[index];

  // 如果没有wordId，不能执行收藏操作
  if (!wordItem?.id) {
    showToast({
      message: '单词ID不存在，无法收藏',
      position: 'bottom',
    });
    return;
  }

  if (isCollecting.value) return;
  isCollecting.value = true;

  try {
    // 调用后端API进行收藏或取消收藏
    let response;
    if (!wordItem.isCollected) {
      // 添加到生词本
      const addRequest = {
        wordId: wordItem.id,
        difficulty: typeof wordItem.difficultyNumber === 'number'
          ? wordItem.difficultyNumber
          : undefined,
      };
      response =
        await UserWordBookControllerService.addToWordBookUsingPost(addRequest);
    } else {
      // 从生词本移除
      response =
        await UserWordBookControllerService.removeFromWordBookUsingDelete(
          wordItem.id,
        );
    }

    if (response.code === 0) {
      // API调用成功，更新本地状态
      const newCollectedStatus = !wordItem.isCollected;
      const updatedWord: Word = {
        ...wordItem,
        isCollected: newCollectedStatus,
        collectCount: newCollectedStatus
          ? (wordItem.collectCount || 0) + 1
          : Math.max((wordItem.collectCount || 0) - 1, 0),
      };

      updateWordAt(index, updatedWord);

      // 更新生词本统计数据
      await getWordBookStats();

      showToast({
        message: newCollectedStatus ? '已添加到生词本' : '已取消收藏',
        position: 'bottom',
      });
    } else {
      // API调用失败
      showToast({
        message: `操作失败: ${response.message || '未知错误'}`,
        position: 'bottom',
      });
    }
  } catch (error) {
    console.error('收藏/取消收藏单词失败', error);
    showToast({
      message: '操作失败，请稍后再试',
      position: 'bottom',
    });
  } finally {
    isCollecting.value = false;
  }
};



// 点赞/取消点赞单词
const toggleThumbUp = async (index: number = currentWordIndex.value): Promise<void> => {
  pauseAutoSwitch();
  const wordItem = words.value[index];

  if (!wordItem?.id) {
    showToast({
      message: '单词ID不存在，无法点赞',
      position: 'bottom',
    });
    return;
  }

  if (isThumbUping.value) return;
  isThumbUping.value = true;

  try {
    let response;
    if (wordItem.isThumbUp) {
      response = await DailyWordThumbControllerService.cancelThumbWordUsingDelete(wordItem.id);
    } else {
      response = await DailyWordThumbControllerService.thumbWordUsingPost(wordItem.id);
    }

    if (response && response.code === 0) {
      const newThumbStatus = !wordItem.isThumbUp;
      const currentCount = wordItem.likeCount || 0;
      const newLikeCount = newThumbStatus
        ? currentCount + 1
        : Math.max(currentCount - 1, 0);

      const updatedWord: Word = {
        ...wordItem,
        isThumbUp: newThumbStatus,
        likeCount: newLikeCount,
      };

      updateWordAt(index, updatedWord);

      showToast({
        message: newThumbStatus ? '点赞成功' : '已取消点赞',
        position: 'bottom',
      });
    }
  } catch (error) {
    console.error('点赞/取消点赞单词失败', error);
    showToast({
      message: '操作失败，请稍后再试',
      position: 'bottom',
    });
  } finally {
    isThumbUping.value = false;
  }
};


// 开始编辑笔记
const startEditingNote = (): void => {
  pauseAutoSwitch();
  noteContent.value = currentWord.value?.notes || '';
  isEditingNote.value = true;
};

// 保存单词笔记
const saveWordNote = async (): Promise<void> => {
  pauseAutoSwitch();
  const wordItem = currentWord.value;
  if (!wordItem?.id) {
    showToast({
      message: '单词ID不存在，无法保存笔记',
      position: 'bottom',
    });
    return;
  }

  isSavingNote.value = true;

  try {
    const response =
      await DailyWordLearningControllerService.saveWordNoteUsingPost(
        noteContent.value,
        wordItem.id,
      );

    if (response.code === 0 && response.data) {
      // 更新本地单词数据
      const updatedWord: Word = {
        ...wordItem,
        notes: noteContent.value,
      };

      updateWordAt(currentWordIndex.value, updatedWord);
      isEditingNote.value = false;
      showToast({
        message: '笔记保存成功',
        position: 'bottom',
      });
    } else {
      showToast({
        message: `保存失败: ${response.message || '未知错误'}`,
        position: 'bottom',
      });
    }
  } catch (error) {
    console.error('保存单词笔记失败', error);
    showToast({
      message: '保存失败，请稍后再试',
      position: 'bottom',
    });
  } finally {
    isSavingNote.value = false;
  }
};

// 更新掌握程度
const updateMasteryLevel = async (): Promise<void> => {
  pauseAutoSwitch();
  const wordItem = currentWord.value;
  if (!wordItem?.id) return;

  try {
    // 更新单词的难度
    const difficultyRequest = {
      wordId: wordItem.id,
      difficulty: masteryLevel.value,
    };
    const response = await UserWordBookControllerService.updateDifficultyUsingPut(difficultyRequest, wordItem.id);

    if (response.code === 0 && response.data) {
      showToast({
        message: '掌握程度已更新',
        position: 'bottom',
      });
    } else {
      showToast({
        message: `更新失败: ${response.message || '未知错误'}`,
        position: 'bottom',
      });
    }
  } catch (error) {
    console.error('更新单词掌握程度失败', error);
    showToast({
      message: '更新失败，请稍后再试',
      position: 'bottom',
    });
  }
};

// 标记单词为已学习
const markAsStudied = async (index: number = currentWordIndex.value): Promise<void> => {
  pauseAutoSwitch();
  const wordItem = words.value[index];
  if (!wordItem?.id) return;

  isMarkingStudied.value = true;

  try {
    const newStudiedStatus = !wordItem.isStudied;
    
    // 更新学习状态
    const updateStatusRequest = {
      wordId: wordItem.id,
      learningStatus: newStudiedStatus ? 1 : 0,
    };
    
    // 更新学习状态
    const response = await UserWordBookControllerService.updateLearningStatusUsingPut(
      updateStatusRequest,
      wordItem.id
    );

    if (response.code === 0) {
      // 更新本地状态
      const updatedWord: Word = {
        ...wordItem,
        isStudied: newStudiedStatus,
        learningStatus: newStudiedStatus ? 1 : 0,
      };
      updateWordAt(index, updatedWord);

      // 更新本地存储
      try {
        const studiedWords = JSON.parse(
          localStorage.getItem('studiedWords') || '{}'
        );
        studiedWords[wordItem.id] = newStudiedStatus;
        localStorage.setItem('studiedWords', JSON.stringify(studiedWords));
      } catch (error) {
        console.error('保存学习状态到本地存储失败', error);
      }

      showToast({
        message: newStudiedStatus ? '已标记为学习完成' : '已取消学习标记',
        position: 'bottom',
      });
    } else {
      showToast({
        message: `操作失败: ${response.message || '未知错误'}`,
        position: 'bottom',
      });
    }
  } catch (error) {
    console.error('标记单词为已学习失败', error);
    showToast({
      message: '操作失败，请稍后再试',
      position: 'bottom',
    });
  } finally {
    isMarkingStudied.value = false;
  }
};

// 组件挂载时检查单词收藏状态与初始化数据
onMounted(() => {
  checkAllWordFavourStatus();
  checkAllWordThumbStatus();
  checkAllWordStudiedStatus();
  noteContent.value = currentWord.value?.notes || '';
  masteryLevel.value = 1;
  
  // 获取生词本统计
  getWordBookStats();
  startAutoSwitch();
});

// 在props变化时也检查状态
watch(
  () => words.value.map((word) => word.id).join(','),
  (ids) => {
    if (ids) {
      checkAllWordFavourStatus();
      checkAllWordThumbStatus();
      checkAllWordStudiedStatus();
    }
  },
);

watch(
  () => words.value.length,
  () => {
    if (currentWordIndex.value >= words.value.length) {
      currentWordIndex.value = 0;
    }
    startAutoSwitch();
  },
);

onUnmounted(() => {
  clearAutoSwitch();
});

// 播放单词发音
const playAudio = (audioUrl?: string): void => {
  pauseAutoSwitch();
  if (audioUrl) {
    const audio = new Audio(audioUrl);
    audio.play().catch((error) => {
      console.error('播放音频失败', error);
      showToast({
        message: '音频播放失败',
        position: 'bottom',
      });
    });
  }
};
</script>

<style scoped>
.word-module {
  margin-bottom: 16px;
  background: transparent;
  border-radius: 0;
  overflow: visible;
  box-shadow: none;
}

.more-link {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.word-module :deep(.van-cell) {
  padding: 0 2px 12px !important;
  background: transparent;
}

.word-module :deep(.van-cell__title) {
  color: #1e293b;
  font-size: 16px;
  font-weight: 800 !important;
}

.word-module :deep(.van-cell__value),
.word-module :deep(.van-cell__right-icon) {
  flex: 0 0 auto;
  overflow: visible;
}

.daily-word {
  padding: 16px;
  cursor: pointer;
}

.word-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.word-text {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
}

.daily-words-container {
  padding: 2px 2px 4px;
}

.daily-word-item {
  padding: 14px;
  margin-bottom: 0;
  overflow: hidden;
  background:
    radial-gradient(circle at 92% 4%, rgba(96, 165, 250, 0.16), transparent 30%),
    linear-gradient(135deg, rgba(238, 242, 255, 0.86) 0%, rgba(219, 234, 254, 0.68) 100%);
  border: 1px solid rgba(255, 255, 255, 0.66);
  border-radius: 22px;
  box-shadow: 0 14px 32px rgba(79, 70, 229, 0.08);
  backdrop-filter: blur(12px);
}

.word-card-top {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
  font-size: 12px;
  font-weight: 700;
  color: #6366f1;
}

.word-card-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 14px;
}

.word-nav-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  color: #6366f1;
  background: rgba(99, 102, 241, 0.1);
  border-radius: 50%;
}

.auto-progress {
  height: 3px;
  margin-top: 14px;
  overflow: hidden;
  background: rgba(79, 70, 229, 0.12);
  border-radius: 999px;
}

.auto-progress span {
  display: block;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, #8b5cf6, #60a5fa, #a7f3d0);
  border-radius: inherit;
  transform-origin: left center;
  animation: word-progress 4s linear forwards;
}

@keyframes word-progress {
  from {
    transform: scaleX(0);
  }

  to {
    transform: scaleX(1);
  }
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  height: 32px;
}

.action-icons {
  display: flex;
  gap: 10px;
  align-items: center;
  height: 32px;
}

.thumb-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  background-color: #f8f8f8;
  padding: 2px 8px;
  border-radius: 12px;
  transition: all 0.3s ease;
  line-height: 1;
  height: 24px;
}

.thumb-action.thumbed {
  background-color: #ffebee;
}

.thumb-number {
  font-size: var(--font-size-sm);
  color: #969799;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  line-height: 1;
}

.thumb-number.thumbed {
  color: #ee0a24;
}

.thumb-icon {
  font-size: var(--font-size-md);
  color: #969799;
  transition: color 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  height: 24px;
}

.thumb-icon.thumbed {
  color: #ee0a24;
}

.collect-action {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f8f8;
  padding: 2px 8px;
  border-radius: 12px;
  transition: all 0.3s ease;
  line-height: 1;
  height: 24px;
  width: 32px;
}

.collect-action.collected {
  background-color: #fffbe5;
}

.collect-icon {
  font-size: var(--font-size-lg);
  color: #969799;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 24px;
  transition: color 0.3s ease;
}

.collect-icon.collected {
  color: #ffd21e;
}

.word-phonetic {
  font-size: var(--font-size-sm);
  color: #969799;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.phonetic-text {
  display: inline-block;
  line-height: 1;
}

.audio-icon {
  font-size: var(--font-size-md);
  color: #6366f1;
  margin-left: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  height: 24px;
  width: 24px;
}

.word-translation {
  font-size: var(--font-size-md);
  color: #1e293b;
  margin-bottom: 8px;
  font-weight: 500;
}

.word-example-wrapper {
  margin-top: 10px;
}

.word-example {
  font-size: var(--font-size-sm);
  color: #64748b;
  font-style: italic;
  margin-bottom: 4px;
}

.word-example-translation {
  font-size: var(--font-size-xs);
  color: #94a3b8;
  font-style: normal;
  margin-top: 2px;
}

.word-detail {
  padding: 16px;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebedf0;
}

.popup-header .title {
  font-size: var(--font-size-md);
  font-weight: 700;
}

.word-content {
  padding: 0 8px;
}

.word-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.meaning-item {
  margin-bottom: 16px;
}

.part-of-speech {
  font-size: var(--font-size-sm);
  color: #969799;
  margin-bottom: 4px;
}

.definition {
  font-size: var(--font-size-md);
  color: #323233;
  margin-bottom: 8px;
  font-weight: 500;
}

.example {
  font-size: 14px;
  color: #646566;
  font-style: italic;
}

:deep(.van-grid-item__text) {
  font-size: 14px;
  color: #323233;
  margin-top: 4px;
  font-weight: 700;
  font-family: 'Noto Sans SC', sans-serif;
}

:deep(.van-grid-item__icon) {
  font-size: 24px;
  color: #1989fa;
}

:deep(.van-grid-item__content) {
  padding: 16px 8px;
}

/* 强制覆盖组件标题样式 */
:deep(.van-cell) {
  position: relative;
  padding: 0 0 12px !important;
  transition: all 0.3s ease;
  border-radius: 0 !important;
  background-color: transparent !important;
  margin: 0 !important;
}

:deep(.van-cell:hover) {
  background-color: transparent !important;
}

:deep(.van-cell::after) {
  display: none !important;
}

:deep(.van-cell__title) {
  font-weight: 700 !important;
  font-family: 'Noto Sans SC', sans-serif !important;
}

.svg-icon {
  width: 1em;
  height: 1em;
  vertical-align: -0.15em;
  fill: currentColor;
  overflow: hidden;
}

.word-icon {
  font-size: 20px;
  margin-right: 4px;
  color: #6366f1;
  vertical-align: middle;
  display: flex;
  align-items: center;
  height: 24px;
}

.right-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  overflow: visible;
  flex-shrink: 0;
}

.vocabulary-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-width: 92px;
  background-color: rgba(99, 102, 241, 0.08);
  padding: 5px 18px 5px 10px;
  border-radius: 16px;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: visible;
}

.vocabulary-btn:hover {
  background-color: #e6f1ff;
}

.vocabulary-text {
  font-size: var(--font-size-sm);
  color: #6366f1;
  font-weight: 500;
  line-height: 1;
  white-space: nowrap;
}

.vocabulary-icon {
  font-size: 18px;
  color: #6366f1;
  vertical-align: middle;
}

.word-info {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.word-category,
.word-difficulty {
  font-size: var(--font-size-xs);
  padding: 2px 6px;
  border-radius: 10px;
  color: #fff;
}

.word-category {
  background-color: #60a5fa;
}

.word-difficulty {
  background-color: #8b5cf6;
}

.word-title {
  display: flex;
  align-items: center;
}

.word-info-detail {
  display: flex;
  gap: 8px;
  margin: 8px 0 16px;
}

.tag {
  font-size: var(--font-size-xs);
  padding: 2px 8px;
  border-radius: 10px;
  color: #fff;
}

.category-tag {
  background-color: #1989fa;
}

.difficulty-tag {
  background-color: #ff976a;
}

.detail-item {
  margin-bottom: 16px;
}

.item-label {
  font-size: var(--font-size-sm);
  color: #969799;
  margin-bottom: 4px;
}

.item-content {
  font-size: var(--font-size-md);
  color: #323233;
  line-height: 1.5;
}

.example-translation {
  color: #969799;
  font-size: var(--font-size-sm);
  margin-top: 4px;
  font-style: italic;
}

.notes-content {
  font-size: var(--font-size-sm);
  background-color: #f7f8fa;
  padding: 8px;
  border-radius: 4px;
  border-left: 3px solid #1989fa;
}

.word-mastery {
  margin-bottom: 20px;
  background-color: #f8fafc;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.mastery-slider {
  padding: 0 4px;
  margin-top: 12px;
}

.mastery-progress {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  position: relative;
}

.mastery-progress::before {
  content: '';
  position: absolute;
  top: 10px;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #e8eaec;
  z-index: 0;
}

.mastery-level {
  position: relative;
  z-index: 1;
  font-size: var(--font-size-xs);
  color: #969799;
  padding: 4px 8px;
  background-color: #fff;
  border-radius: 12px;
  border: 1px solid #ebedf0;
  transition: all 0.3s ease;
}

.mastery-level.active {
  color: #1989fa;
  background-color: #e6f7ff;
  border-color: #a7d0ff;
  font-weight: 500;
}

.mastery-button {
  width: 24px;
  height: 24px;
  background-color: #1989fa;
  border-radius: 50%;
  color: white;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(25, 137, 250, 0.3);
}

.item-label-with-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--font-size-sm);
  color: #969799;
  margin-bottom: 4px;
}

.edit-icon {
  color: #1989fa;
  font-size: 16px;
}

.note-editor {
  margin-top: 8px;
  margin-bottom: 10px;
}

.note-textarea {
  background-color: #f7f8fa;
  border-radius: 8px;
}

.empty-note {
  padding: 12px;
  background-color: #f7f8fa;
  border-radius: 8px;
  color: #c8c9cc;
  text-align: center;
  font-size: var(--font-size-sm);
  margin-top: 8px;
  cursor: pointer;
}

/* 添加学习图标样式 */
.study-icon {
  font-size: 20px;
  color: #c8c9cc;
  transition: all 0.3s ease;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f7f8fa;
  border: 1px solid #e8e8e8;
}

.study-icon.studied {
  color: #fff;
  background-color: #07c160;
  border-color: #07c160;
  box-shadow: 0 2px 4px rgba(7, 193, 96, 0.2);
}

.empty-word {
  padding: 30px 0;
  text-align: center;
}

.vocabulary-count {
  position: absolute;
  top: 2px;
  right: 4px;
  z-index: 2;
  background-color: #ee0a24;
  color: #fff;
  font-size: 9px;
  border-radius: 10px;
  min-width: 14px;
  height: 14px;
  line-height: 14px;
  text-align: center;
  padding: 0 3px;
  font-weight: bold;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.92);
}

@media (max-width: 375px) {
  .right-actions {
    gap: 6px;
  }

  .vocabulary-btn {
    min-width: 86px;
    padding-right: 16px;
  }

  .more-link {
    font-size: 12px;
  }
}
</style>
