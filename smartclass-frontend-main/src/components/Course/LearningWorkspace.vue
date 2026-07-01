<template>
  <div class="learning-workspace">
    <div v-if="loadingCourse" class="loading-wrap">
      <van-loading type="spinner" size="24px">课程加载中...</van-loading>
    </div>

    <template v-else>
      <section class="learning-top-grid">
        <div class="video-panel">
          <course-video-player
            ref="videoPlayerRef"
            :video-url="rawVideoCandidate"
            @loadedmetadata="handleVideoLoadedMetadata"
            @ended="handleVideoEnded"
            @timeupdate="handleVideoTimeUpdate"
            @pause="handleVideoPause"
            @error="handleVideoError"
          />

          <div class="video-meta">
            <div>
              <div class="course-name">{{ displayCourseTitle }}</div>
              <div class="section-name">{{ activeSection?.title || '请选择章节进行学习' }}</div>
            </div>
            <div class="meta-row">
              <span>学习进度：{{ currentProgress }}%</span>
              <van-tag type="primary" plain>{{ currentStatusText }}</van-tag>
            </div>
          </div>
        </div>

        <aside class="chapter-sidebar">
          <div class="chapter-side-head">
            <div>
              <van-icon name="bookmark-o" />
              <span>章节列表</span>
            </div>
            <label>
              只看未学
              <van-switch v-model="onlyUnlearned" size="18px" active-color="#8b6cff" />
            </label>
          </div>

          <div v-if="!hasValidSections" class="side-empty">当前课程暂无可学习小节</div>
          <div v-else class="side-chapter-list">
            <template v-for="(chapter, chapterIndex) in visibleChapters" :key="getChapterId(chapter) || chapterIndex">
              <button
                v-for="(section, sectionIndex) in chapter.sections"
                :key="getSectionId(section) || `${chapterIndex}-${sectionIndex}`"
                class="side-chapter-item"
                :class="{
                  active: activeSectionId === getSectionId(section),
                  completed: section.learnStatus === 'COMPLETED',
                  locked: section.learnStatus !== 'COMPLETED' && activeSectionId !== getSectionId(section) && sectionIndex > 0,
                }"
                type="button"
                @click="handleSectionSelect({ chapter, section })"
              >
                <span class="chapter-state">
                  <van-icon
                    :name="
                      activeSectionId === getSectionId(section)
                        ? 'bar-chart-o'
                        : section.learnStatus === 'COMPLETED'
                          ? 'success'
                          : sectionIndex > 0
                            ? 'lock'
                            : 'play'
                    "
                  />
                </span>
                <span class="chapter-copy">
                  <strong>{{ chapter.title }}</strong>
                  <small>{{ section.title || `第 ${sectionIndex + 1} 节` }}</small>
                  <i v-if="activeSectionId === getSectionId(section)" class="mini-progress">
                    <b :style="{ width: `${currentProgress}%` }"></b>
                  </i>
                </span>
                <span class="chapter-time">{{ getSectionDurationText(section) }}</span>
              </button>
            </template>
          </div>
        </aside>
      </section>

      <div class="tabs-panel">
        <van-tabs v-model:active="activeTab" animated>
          <van-tab title="详情" name="detail">
            <div class="tab-content">
              <div class="info-card">
                <div class="info-row"><span>课程：</span>{{ displayCourseTitle }}</div>
                <div class="info-row"><span>章节：</span>{{ activeChapter?.title || '-' }}</div>
                <div class="info-row"><span>小节：</span>{{ activeSection?.title || '-' }}</div>
                <div class="info-row"><span>视频类型：</span>{{ videoTypeText }}</div>
              </div>
            </div>
          </van-tab>

          <van-tab title="章节" name="chapter">
            <div class="tab-content">
              <van-empty
                v-if="!hasValidSections"
                description="当前课程暂无可学习小节"
                image-size="80"
              />
              <chapter-section-list
                v-else
                :chapters="chapters"
                :active-section-id="activeSectionId"
                @select="handleSectionSelect"
              />
            </div>
          </van-tab>

          <van-tab title="笔记" name="note">
            <div class="tab-content">
              <div class="note-card">
                <textarea v-model="localNote" placeholder="记录这一节课的重点、疑问或灵感..." />
              </div>
            </div>
          </van-tab>

          <van-tab title="作业" name="homework">
            <div class="tab-content">
              <van-empty
                v-if="!hasValidSections"
                description="当前课程暂无可学习小节"
                image-size="80"
              />
              <homework-panel
                v-else
                :questions="questions"
                :answers="answers"
                :submitting-question-id="submittingQuestionId"
                :results="questionResults"
                @answer-change="handleAnswerChange"
                @submit="handleQuestionSubmit"
              />
            </div>
          </van-tab>

          <van-tab title="学习助手" name="assistant">
            <div class="assistant-layout">
              <div class="assistant-guide">
                <img :src="maomaoeImg" alt="猫猫鹅学习助手" />
                <div class="guide-bubble">
                  <strong>Hi~ 我是你的学习助手</strong>
                  <span>有什么问题都可以问我哦~</span>
                </div>
                <button type="button">这一章节的重点是什么？</button>
                <button type="button">能举个生活中的例子吗？</button>
                <button type="button">知识点太难了，能简单讲一下吗？</button>
                <button type="button">还有其他问题？换个问法试试~</button>
              </div>
              <div class="assistant-chat-shell">
                <learning-assistant-panel
                  :course-id="props.courseId"
                  :chapter-id="activeChapterId"
                  :section-id="activeSectionId"
                />
              </div>
            </div>
          </van-tab>
        </van-tabs>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { showToast } from 'vant';
import { useUserStore } from '@/stores/userStore';
import {
  completeCourseSection,
  CourseChapter,
  CourseDetail,
  CourseQuestion,
  CourseSection,
  LearningRecordPayload,
  queryCourseDetail,
  queryLearningRecord,
  querySectionQuestions,
  saveLearningRecord,
  startCourseSection,
  submitHomework,
} from '@/api/course';
import maomaoeImg from '@/assets/images/maomaoe.png';
import ChapterSectionList from './ChapterSectionList.vue';
import CourseVideoPlayer from './CourseVideoPlayer.vue';
import HomeworkPanel from './HomeworkPanel.vue';
import LearningAssistantPanel from './LearningAssistantPanel.vue';

interface CourseBrief {
  id: number;
  title?: string;
  description?: string;
  cover?: string;
}

interface LearningContextPayload {
  recentCourseId: number;
  recentCourseTitle: string;
  sectionId: number;
  progress: number;
  wrongCount: number;
  updatedAt: string;
}

const LEARNING_CONTEXT_KEY = 'latest_learning_context';

type CourseVideoPlayerExpose = {
  videoElement: HTMLVideoElement | null;
  renderMode: 'direct' | 'bilibili' | 'external' | 'empty';
};

const props = defineProps<{
  courseId: number;
  course?: CourseBrief | null;
  openAssistantOnLoad?: boolean;
  initialSectionId?: number;
}>();

const userStore = useUserStore();
const videoPlayerRef = ref<CourseVideoPlayerExpose | null>(null);

const loadingCourse = ref(false);
const courseDetail = ref<CourseDetail | null>(null);

const chapters = ref<CourseChapter[]>([]);
const activeChapter = ref<CourseChapter | null>(null);
const activeSection = ref<CourseSection | null>(null);
const activeTab = ref<'detail' | 'chapter' | 'note' | 'homework' | 'assistant'>(
  props.openAssistantOnLoad ? 'assistant' : 'assistant',
);
const onlyUnlearned = ref(false);
const localNote = ref('');

const questions = ref<CourseQuestion[]>([]);
const answers = ref<Record<number, string>>({});
const questionResults = ref<Record<number, string>>({});
const submittingQuestionId = ref<number | null>(null);

const currentProgress = ref(0);
const videoProgress = ref(0);
const historySectionIds = ref<number[]>([]);
const wrongCount = ref(0);

const displayCourseTitle = computed(() => {
  return courseDetail.value?.title || props.course?.title || `课程 #${props.courseId}`;
});

const asStringOrEmpty = (value: unknown): string => {
  if (typeof value === 'string') {
    return value.trim();
  }
  return '';
};

const toValidPositiveId = (value: unknown): number | undefined => {
  const num =
    typeof value === 'string'
      ? Number.parseInt(value, 10)
      : typeof value === 'number'
        ? value
        : Number(value);
  return Number.isFinite(num) && num > 0 ? num : undefined;
};

const getChapterId = (chapter: CourseChapter | null | undefined): number | undefined => {
  if (!chapter) {
    return undefined;
  }
  const record = chapter as unknown as Record<string, unknown>;
  return toValidPositiveId(record.id) ?? toValidPositiveId(record.chapterId) ?? toValidPositiveId(record.chapter_id);
};

const getSectionId = (section: CourseSection | null | undefined): number | undefined => {
  if (!section) {
    return undefined;
  }
  const record = section as unknown as Record<string, unknown>;
  return toValidPositiveId(record.id) ?? toValidPositiveId(record.sectionId) ?? toValidPositiveId(record.section_id);
};

const activeChapterId = computed(() => getChapterId(activeChapter.value));
const activeSectionId = computed(() => getSectionId(activeSection.value));

const visibleChapters = computed(() => {
  if (!onlyUnlearned.value) {
    return chapters.value;
  }

  return chapters.value
    .map((chapter) => ({
      ...chapter,
      sections: (chapter.sections || []).filter((section) => section.learnStatus !== 'COMPLETED'),
    }))
    .filter((chapter) => chapter.sections.length > 0);
});

const normalizeDetailChapters = (detail: CourseDetail): CourseChapter[] => {
  return (detail.chapters || []).map((chapter) => {
    const chapterRecord = chapter as unknown as Record<string, unknown>;
    const chapterId = toValidPositiveId(chapterRecord.id) ?? toValidPositiveId(chapterRecord.chapterId) ?? 0;
    const chapterTitle = asStringOrEmpty(chapterRecord.title) || asStringOrEmpty(chapterRecord.chapterTitle) || `章节 ${chapterId || ''}`;

    const sectionsRaw = Array.isArray(chapterRecord.sections) ? chapterRecord.sections : [];
    const sections = sectionsRaw
      .map((sectionRaw) => {
        const sectionRecord = (sectionRaw || {}) as Record<string, unknown>;
        const sectionId =
          toValidPositiveId(sectionRecord.id) ?? toValidPositiveId(sectionRecord.sectionId) ?? toValidPositiveId(sectionRecord.section_id);
        if (!sectionId) {
          return null;
        }

        const normalizedSection: CourseSection = {
          ...(sectionRecord as unknown as CourseSection),
          id: sectionId,
          title:
            asStringOrEmpty(sectionRecord.title) ||
            asStringOrEmpty(sectionRecord.sectionTitle) ||
            `小节 ${sectionId}`,
          videoUrl:
            asStringOrEmpty(sectionRecord.videoUrl) ||
            asStringOrEmpty(sectionRecord.video_url) ||
            asStringOrEmpty(sectionRecord.resourceUrl) ||
            asStringOrEmpty(sectionRecord.resource_url) ||
            undefined,
        };
        return normalizedSection;
      })
      .filter((section): section is CourseSection => section !== null);

    return {
      ...(chapterRecord as unknown as CourseChapter),
      id: chapterId,
      title: chapterTitle,
      sections,
    };
  });
};

const getSectionField = (section: CourseSection | null, keys: string[]): string => {
  if (!section) {
    return '';
  }
  const record = section as unknown as Record<string, unknown>;
  for (const key of keys) {
    const value = asStringOrEmpty(record[key]);
    if (value) {
      return value;
    }
  }
  return '';
};

const getNumericSectionField = (section: CourseSection | null, keys: string[]): number => {
  if (!section) {
    return 0;
  }
  const record = section as unknown as Record<string, unknown>;
  for (const key of keys) {
    const value = Number(record[key] || 0);
    if (Number.isFinite(value) && value > 0) {
      return value;
    }
  }
  return 0;
};

const getSectionDurationText = (section: CourseSection | null) => {
  const rawDuration = getNumericSectionField(section, ['duration', 'totalDuration', 'total_duration']);
  const minutes = rawDuration > 180 ? Math.ceil(rawDuration / 60) : rawDuration;
  return `${Math.max(1, Math.round(minutes || 15))} 分钟`;
};

const rawVideoCandidate = computed(() => {
  return getSectionField(activeSection.value, [
    'videoUrl',
    'video_url',
    'resourceUrl',
    'resource_url',
    'mediaUrl',
    'media_url',
    'url',
  ]);
});

const videoTypeText = computed(() => {
  const renderMode = videoPlayerRef.value?.renderMode;
  if (renderMode === 'direct') {
    return '站内视频';
  }
  if (renderMode === 'bilibili') {
    return 'B站内嵌视频';
  }
  if (renderMode === 'external') {
    return '外部网页视频';
  }
  return '暂无视频';
});

const currentStatusText = computed(() => {
  if (activeSection.value?.learnStatus === 'COMPLETED') {
    return '已完成';
  }
  if (activeSection.value?.learnStatus === 'LEARNING') {
    return '学习中';
  }
  return '未学习';
});

const hasValidSections = computed(() => {
  return chapters.value.some((chapter) => (chapter.sections || []).some((section) => getSectionId(section) !== undefined));
});

const userId = computed(() => userStore.userInfo?.id);

const sanitizeHistorySectionIds = (ids: unknown[]): number[] => {
  return ids
    .map((id) => toValidPositiveId(id))
    .filter((id): id is number => id !== undefined);
};

const getErrorMessage = (error: unknown, fallback = '操作失败，请稍后重试'): string => {
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
};

const updateLearningContext = () => {
  const sectionId = activeSectionId.value;
  const courseId = toValidPositiveId(props.courseId);
  if (!sectionId || !courseId) {
    return;
  }

  const payload: LearningContextPayload = {
    recentCourseId: courseId,
    recentCourseTitle: displayCourseTitle.value,
    sectionId,
    progress: currentProgress.value,
    wrongCount: wrongCount.value,
    updatedAt: new Date().toISOString(),
  };

  localStorage.setItem(LEARNING_CONTEXT_KEY, JSON.stringify(payload));
};

const persistLearningRecord = async () => {
  const sectionId = activeSectionId.value;
  const courseId = toValidPositiveId(props.courseId);
  if (!sectionId || !courseId) {
    return;
  }

  const payload: LearningRecordPayload = {
    courseId,
    sectionId,
    progress: currentProgress.value,
    videoProgress: videoProgress.value,
    historySectionIds: sanitizeHistorySectionIds(historySectionIds.value),
    wrongCount: wrongCount.value,
    updatedAt: new Date().toISOString(),
  };

  const validUserId = toValidPositiveId(userId.value);
  const validChapterId = activeChapterId.value;
  if (validUserId) {
    payload.userId = validUserId;
  }
  if (validChapterId) {
    payload.chapterId = validChapterId;
  }

  historySectionIds.value = payload.historySectionIds || [];

  try {
    await saveLearningRecord(payload);
    updateLearningContext();
  } catch (error) {
    console.error('保存学习记录失败', error);
  }
};

const loadSectionQuestions = async (sectionId: number) => {
  if (!toValidPositiveId(sectionId)) {
    questions.value = [];
    answers.value = {};
    questionResults.value = {};
    return;
  }

  try {
    questions.value = await querySectionQuestions(sectionId);
    answers.value = {};
    questionResults.value = {};
  } catch (error) {
    questions.value = [];
    showToast(getErrorMessage(error, '习题加载失败'));
  }
};

const markHistory = (sectionId: number) => {
  const validSectionId = toValidPositiveId(sectionId);
  if (!validSectionId) {
    return;
  }

  const set = new Set(sanitizeHistorySectionIds(historySectionIds.value));
  set.add(validSectionId);
  historySectionIds.value = Array.from(set);
};

const setActiveSection = async (chapter: CourseChapter, section: CourseSection, checkOrder: boolean) => {
  const validSectionId = getSectionId(section);
  const validCourseId = toValidPositiveId(props.courseId);
  if (!validSectionId || !validCourseId) {
    showToast('当前小节数据异常，请切换其他小节');
    return;
  }

  if (checkOrder) {
    try {
      await startCourseSection(validCourseId, validSectionId);
    } catch (error) {
      showToast(getErrorMessage(error, '当前章节暂不可学习'));
      return;
    }
  }

  activeChapter.value = chapter;
  activeSection.value = section;
  section.learnStatus = section.learnStatus === 'COMPLETED' ? 'COMPLETED' : 'LEARNING';
  markHistory(validSectionId);

  await loadSectionQuestions(validSectionId);

  currentProgress.value = section.learnStatus === 'COMPLETED' ? 100 : Math.max(currentProgress.value, 1);
  await persistLearningRecord();

  await nextTick();
  const videoElement = videoPlayerRef.value?.videoElement;
  if (videoElement) {
    videoElement.load();
  }
};

const findSectionById = (sectionId: number) => {
  const validSectionId = toValidPositiveId(sectionId);
  if (!validSectionId) {
    return null;
  }

  for (const chapter of chapters.value) {
    const section = chapter.sections.find((item) => getSectionId(item) === validSectionId);
    if (section) {
      return { chapter, section };
    }
  }
  return null;
};

const getFirstSection = () => {
  for (const chapter of chapters.value) {
    const section = chapter.sections.find((item) => getSectionId(item) !== undefined);
    if (section) {
      return { chapter, section };
    }
  }
  return null;
};

const normalizeRecord = (record: unknown): Record<string, unknown> | null => {
  if (Array.isArray(record)) {
    return (record[0] as Record<string, unknown>) || null;
  }
  if (record && typeof record === 'object') {
    return record as Record<string, unknown>;
  }
  return null;
};

const loadCourseData = async () => {
  loadingCourse.value = true;
  try {
    const detail = await queryCourseDetail(props.courseId);
    courseDetail.value = detail;
    chapters.value = normalizeDetailChapters(detail);

    if (!hasValidSections.value) {
      activeChapter.value = null;
      activeSection.value = null;
      questions.value = [];
      answers.value = {};
      questionResults.value = {};
      showToast('当前课程暂无可学习小节');
      return;
    }

    const initialMatched = props.initialSectionId && props.initialSectionId > 0 ? findSectionById(props.initialSectionId) : null;

    if (initialMatched) {
      await setActiveSection(initialMatched.chapter, initialMatched.section, false);
    }

    if (!activeSection.value) {
      const recordParams: { userId?: number; courseId: number } = { courseId: props.courseId };
      const validUserId = toValidPositiveId(userId.value);
      if (validUserId) {
        recordParams.userId = validUserId;
      }

      const recordRaw = await queryLearningRecord(recordParams).catch(() => null);
      const record = normalizeRecord(recordRaw);
      if (record) {
        const sectionId = toValidPositiveId(record.sectionId);
        const progress = Number(record.progress || 0);
        const wrong = Number(record.wrongCount || 0);
        const history = Array.isArray(record.historySectionIds) ? sanitizeHistorySectionIds(record.historySectionIds) : [];

        currentProgress.value = Number.isFinite(progress) ? progress : 0;
        wrongCount.value = Number.isFinite(wrong) ? wrong : 0;
        historySectionIds.value = history;

        const matched = sectionId ? findSectionById(sectionId) : null;
        if (matched) {
          await setActiveSection(matched.chapter, matched.section, false);
        }
      }
    }

    if (!activeSection.value) {
      const first = getFirstSection();
      if (first) {
        await setActiveSection(first.chapter, first.section, false);
      }
    }
  } catch (error) {
    showToast(getErrorMessage(error, '课程详情加载失败'));
  } finally {
    loadingCourse.value = false;
  }
};

const handleSectionSelect = async (payload: { chapter: CourseChapter; section: CourseSection }) => {
  const selectedSectionId = getSectionId(payload.section);
  if (!selectedSectionId) {
    showToast('该小节数据异常，请选择其他小节');
    return;
  }

  if (activeSectionId.value === selectedSectionId) {
    return;
  }
  await setActiveSection(payload.chapter, payload.section, true);
};

const handleAnswerChange = (payload: { questionId: number; answer: string }) => {
  answers.value[payload.questionId] = payload.answer;
};

const getQuestionId = (question: CourseQuestion): number | undefined => {
  const record = question as unknown as Record<string, unknown>;
  return (
    toValidPositiveId(record.id) ??
    toValidPositiveId(record.questionId) ??
    toValidPositiveId(record.question_id)
  );
};

const handleQuestionSubmit = async (question: CourseQuestion) => {
  const validSectionId = activeSectionId.value;
  const validQuestionId = getQuestionId(question);
  if (!validSectionId || !validQuestionId) {
    showToast('当前题目或小节数据异常，请刷新后重试');
    return;
  }

  const answer = (answers.value[question.id] || '').trim();
  if (!answer) {
    showToast('请先填写答案');
    return;
  }

  try {
    submittingQuestionId.value = question.id;
    const result = await submitHomework({
      sectionId: validSectionId,
      questionId: validQuestionId,
      answer,
    });

    const isCorrect = typeof result.correct === 'boolean' ? result.correct : undefined;
    if (isCorrect === false) {
      wrongCount.value += 1;
    }

    questionResults.value[question.id] = result.message || (isCorrect ? '提交成功，回答正确' : '提交成功');
    await persistLearningRecord();
  } catch (error) {
    showToast(getErrorMessage(error, '作业提交失败'));
  } finally {
    submittingQuestionId.value = null;
  }
};

const handleVideoLoadedMetadata = () => {
  const videoElement = videoPlayerRef.value?.videoElement;
  if (!videoElement) {
    return;
  }
  if (Number.isFinite(videoElement.duration) && videoElement.duration > 0 && videoProgress.value > 0) {
    const seekTo = Math.max(0, Math.min(videoElement.duration, (videoElement.duration * videoProgress.value) / 100));
    videoElement.currentTime = seekTo;
  }
};

const handleVideoTimeUpdate = () => {
  const videoElement = videoPlayerRef.value?.videoElement;
  if (!videoElement) {
    return;
  }

  const duration = videoElement.duration;
  const currentTime = videoElement.currentTime;
  if (!Number.isFinite(duration) || duration <= 0) {
    return;
  }

  const progress = Math.min(100, Math.floor((currentTime / duration) * 100));
  currentProgress.value = progress;
  videoProgress.value = progress;
};

const handleVideoPause = async () => {
  await persistLearningRecord();
};

const handleVideoError = () => {
  showToast('视频加载失败，请检查视频地址是否可访问');
};

const handleVideoEnded = async () => {
  const validSectionId = activeSectionId.value;
  const validCourseId = toValidPositiveId(props.courseId);
  if (!validSectionId || !validCourseId || !activeSection.value) {
    return;
  }

  try {
    await completeCourseSection(validCourseId, validSectionId);
    activeSection.value.learnStatus = 'COMPLETED';
    currentProgress.value = 100;
    videoProgress.value = 100;
    showToast('当前小节学习完成');
    await persistLearningRecord();
  } catch (error) {
    showToast(getErrorMessage(error, '更新学习进度失败'));
  }
};

onMounted(async () => {
  await loadCourseData();
});

onBeforeUnmount(async () => {
  await persistLearningRecord();
});
</script>

<style scoped>
.learning-workspace {
  height: 100%;
  padding: 18px;
  box-sizing: border-box;
  overflow-y: auto;
  color: #25304d;
  background:
    radial-gradient(circle at 8% 4%, rgba(232, 224, 255, 0.76), transparent 24%),
    radial-gradient(circle at 92% 12%, rgba(220, 236, 255, 0.72), transparent 24%),
    linear-gradient(135deg, rgba(248, 245, 255, 0.96), rgba(242, 248, 255, 0.94) 48%, rgba(255, 247, 252, 0.95));
}

.loading-wrap {
  min-height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.learning-top-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 16px;
}

.video-panel,
.chapter-sidebar,
.tabs-panel {
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.58);
  box-shadow: 0 18px 46px rgba(129, 140, 248, 0.12);
  backdrop-filter: blur(20px);
}

.video-panel {
  position: relative;
  overflow: hidden;
}

.video-meta {
  padding: 14px 18px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.course-name {
  color: #25304d;
  font-size: 18px;
  font-weight: 900;
}

.section-name {
  margin-top: 5px;
  color: #69738f;
  font-size: 13px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #69738f;
  font-size: 13px;
  white-space: nowrap;
}

.chapter-sidebar {
  padding: 16px;
  min-height: 430px;
}

.chapter-side-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 14px;
}

.chapter-side-head > div,
.chapter-side-head label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.chapter-side-head > div {
  color: #25304d;
  font-size: 17px;
  font-weight: 900;
}

.chapter-side-head :deep(.van-icon) {
  color: #8b6cff;
}

.chapter-side-head label {
  color: #69738f;
  font-size: 12px;
  font-weight: 800;
}

.side-empty {
  padding: 28px 12px;
  color: #8a92ad;
  text-align: center;
}

.side-chapter-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 390px;
  overflow-y: auto;
  padding-right: 2px;
}

.side-chapter-list::-webkit-scrollbar {
  width: 0;
}

.side-chapter-item {
  min-height: 72px;
  padding: 10px 12px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 16px;
  display: grid;
  grid-template-columns: 38px 1fr auto;
  align-items: center;
  gap: 10px;
  color: #25304d;
  text-align: left;
  background: rgba(255, 255, 255, 0.56);
  box-shadow: 0 10px 24px rgba(129, 140, 248, 0.08);
  cursor: pointer;
}

.side-chapter-item.active {
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 14px 30px rgba(139, 108, 255, 0.18);
}

.side-chapter-item.locked {
  color: #8a92ad;
}

.chapter-state {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, #7b61ff, #e78bff);
}

.side-chapter-item.locked .chapter-state {
  color: #a4abc2;
  background: rgba(232, 235, 248, 0.9);
}

.chapter-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.chapter-copy strong,
.chapter-copy small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chapter-copy strong {
  font-size: 14px;
  font-weight: 900;
}

.chapter-copy small,
.chapter-time {
  color: #8a92ad;
  font-size: 12px;
  font-weight: 800;
}

.mini-progress {
  width: 100%;
  height: 4px;
  margin-top: 4px;
  border-radius: 999px;
  overflow: hidden;
  background: rgba(139, 108, 255, 0.12);
}

.mini-progress b {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #7b61ff, #e78bff);
}

.tabs-panel {
  margin-top: 16px;
  overflow: hidden;
}

.tab-content {
  padding: 14px 16px 18px;
}

.info-card,
.note-card {
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.52);
  padding: 16px;
}

.info-row {
  color: #25304d;
  font-size: 14px;
  line-height: 1.9;
}

.info-row span {
  color: #69738f;
}

.note-card textarea {
  width: 100%;
  min-height: 160px;
  border: 0;
  outline: none;
  resize: vertical;
  color: #25304d;
  font-size: 14px;
  line-height: 1.7;
  background: transparent;
}

.assistant-layout {
  display: grid;
  grid-template-columns: minmax(240px, 0.42fr) minmax(320px, 0.58fr);
  gap: 18px;
  padding: 22px;
}

.assistant-guide {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.assistant-guide img {
  width: 128px;
  align-self: center;
  filter: drop-shadow(0 18px 26px rgba(139, 108, 255, 0.18));
}

.guide-bubble {
  align-self: center;
  padding: 14px 18px;
  border-radius: 18px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #25304d;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 12px 28px rgba(129, 140, 248, 0.12);
}

.guide-bubble span {
  color: #69738f;
  font-size: 13px;
}

.assistant-guide button {
  width: 100%;
  min-height: 36px;
  border: 0;
  border-radius: 999px;
  padding: 0 14px;
  color: #7b61ff;
  text-align: left;
  font-weight: 800;
  background: rgba(239, 233, 255, 0.72);
}

.assistant-chat-shell {
  min-height: 280px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.48);
}

:deep(.van-tabs__wrap) {
  padding: 0 18px;
}

:deep(.van-tabs__nav) {
  background: transparent;
}

:deep(.van-tab) {
  color: #69738f;
  font-weight: 900;
}

:deep(.van-tab--active) {
  color: #7b61ff;
}

:deep(.van-tabs__line) {
  height: 3px;
  background: linear-gradient(90deg, #7b61ff, #e78bff);
}

@media (max-width: 980px) {
  .learning-top-grid,
  .assistant-layout {
    grid-template-columns: 1fr;
  }

  .chapter-sidebar {
    min-height: auto;
  }

  .side-chapter-list {
    max-height: none;
  }
}

@media (max-width: 640px) {
  .learning-workspace {
    padding: 12px;
  }

  .video-meta {
    align-items: flex-start;
    flex-direction: column;
  }

  .assistant-layout {
    padding: 16px;
  }
}
</style>
