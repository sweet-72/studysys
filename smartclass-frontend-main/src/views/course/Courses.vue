<template>
  <div class="courses has-tabbar">
    <span class="dream-cloud cloud-left"></span>
    <span class="dream-cloud cloud-right"></span>
    <span class="dream-star star-one">✦</span>
    <span class="dream-star star-two">✧</span>
    <span class="dream-star star-three">✦</span>

    <section class="course-hero" :class="{ 'search-active': searchExpanded }">
      <div class="hero-glow hero-glow-purple"></div>
      <div class="hero-glow hero-glow-blue"></div>
      <span class="hero-moon"></span>
      <span class="hero-planet"></span>

      <div class="hero-actions" aria-label="课程快捷操作">
        <button
          v-if="!searchExpanded"
          class="hero-icon-action"
          type="button"
          aria-label="搜索课程"
          @click="toggleCourseSearch"
        >
          <van-icon name="search" />
        </button>
        <button
          class="hero-icon-action"
          :class="{ active: recordHintVisible }"
          type="button"
          aria-label="学习记录"
          @click="handleLearningRecordClick"
        >
          <van-icon name="clock-o" />
        </button>
      </div>

      <form
        v-if="searchExpanded"
        class="course-search-popover"
        @submit.prevent="submitCourseSearch"
      >
        <van-icon name="search" />
        <input
          ref="searchInputRef"
          v-model="courseSearchText"
          type="search"
          placeholder="搜索课程"
          @input="submitCourseSearch"
          @keyup.enter="submitCourseSearch"
        />
        <button type="button" aria-label="关闭搜索" @click="closeCourseSearch">
          <van-icon name="cross" />
        </button>
      </form>

      <div v-if="recordHintVisible" class="record-hint">点击查看学习记录</div>

      <div class="hero-stage">
        <div class="hero-copy">
          <span class="hero-kicker">Course Space</span>
          <h1>课程学习空间</h1>
          <p>精选优质课程，开启你的学习之旅</p>
          <div class="hero-badges">
            <span>优质课程</span>
            <span>专业讲解</span>
            <span>轻松学习</span>
          </div>
        </div>

        <div class="hero-visual">
          <img :src="learningImg" alt="课程学习空间" class="hero-learning" />
        </div>
      </div>
    </section>

    <div class="scrollable-content">
      <course-categories
        :categories="categories"
        :active-category="activeCategory"
        @select="selectCategory"
      />

      <section class="course-glass-panel">
        <div class="panel-head">
          <div>
            <span class="panel-kicker">Course Collection</span>
            <h2>{{ isSearching ? '搜索结果' : activeCategory === 0 ? '推荐课程' : activeCategoryName }}</h2>
            <p v-if="isSearching" class="search-count">共找到 {{ searchResultCourses.length }} 门课程</p>
          </div>

          <div v-if="!isSearching && activeCategory !== 0" class="grade-selector-container">
            <div class="custom-dropdown">
              <div class="dropdown-trigger" @click="toggleDropdown">
                <span>{{ currentGradeText }}</span>
                <span class="dropdown-arrow"></span>
              </div>
              <div v-if="showDropdown" class="dropdown-content">
                <div
                  v-for="option in gradeOptions"
                  :key="option.value"
                  class="dropdown-option"
                  :class="{ 'dropdown-option-active': gradeValue === option.value }"
                  @click="selectGrade(option.value)"
                >
                  {{ option.text }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <course-list
          v-if="isSearching"
          title="搜索结果"
          :courses="searchResultCourses"
          class-name="search-results"
          empty-title="没有找到相关课程，换个关键词试试吧"
          @select="showCourseDetail"
        />

        <course-list
          v-else-if="activeCategory === 0"
          title="为你挑选"
          :courses="recommendedCourses"
          class-name="recommended"
          @select="showCourseDetail"
        />

        <course-list
          v-else
          title="课程列表"
          :courses="filteredCourses"
          class-name="subject-courses"
          @select="showCourseDetail"
        />
      </section>
    </div>

    <course-detail
      v-model="showDetailPopup"
      :course="selectedCourse"
      @close="showDetailPopup = false"
      @start="startLearning"
      @assistant="enterLearningAssistant"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import { CourseDetail as CourseDetailResponse, queryCourseDetail } from '@/api/course';
import {
  BaseResponse_List_CourseCategory_,
  BaseResponse_Page_CourseVO_,
  CourseCategory,
  CourseCategoryControllerService,
  CourseControllerService,
} from '@/services';
import {
  CourseCategories,
  CourseDetail,
  CourseList,
} from '@/components/Course';
import learningImg from '@/assets/images/learning.png';

interface CourseHighlight {
  icon: string;
  color: string;
  text: string;
}

interface ChapterPreview {
  id: number;
  title: string;
  sections: Array<{
    id: number;
    title: string;
  }>;
}

interface EnhancedCourse {
  id: number;
  title: string;
  brief: string;
  cover: string;
  tag: string;
  tagColor: string;
  level: string;
  duration: number;
  studentsCount: number;
  subject: string;
  categoryId?: number;
  categoryName?: string;
  tags?: string[];
  grade?: string;
  description?: string;
  teacherName?: string;
  highlights?: CourseHighlight[];
  detailDescription?: string;
  detailChapters?: ChapterPreview[];
}

interface Category {
  id: number;
  name: string;
  icon: string;
  path: string;
}

const router = useRouter();
const route = useRoute();

const showDetailPopup = ref(false);
const selectedCourse = ref<EnhancedCourse | null>(null);
const activeCategory = ref(0);
const gradeValue = ref(0);
const showDropdown = ref(false);
const recommendedCourses = ref<EnhancedCourse[]>([]);
const searchExpanded = ref(false);
const courseSearchText = ref('');
const submittedCourseSearchText = ref('');
const searchInputRef = ref<HTMLInputElement | null>(null);
const recordHintVisible = ref(false);
const searchDebounceTimer = ref<number | undefined>();

const categories = ref<Category[]>([
  { id: 0, name: '推荐', icon: 'star', path: '/courses/recommended' },
]);

const gradeOptions = [
  { text: '全部学段', value: 0 },
  { text: '小学', value: 1 },
  { text: '初中', value: 2 },
  { text: '高中', value: 3 },
  { text: '大学', value: 4 },
];

const currentGradeText = computed(() => {
  const option = gradeOptions.find((item) => item.value === gradeValue.value);
  return option?.text || '全部学段';
});

const activeCategoryName = computed(() => {
  const category = categories.value.find((item) => item.id === activeCategory.value);
  return category ? `${category.name}课程` : '课程';
});

const filteredCourses = computed(() => {
  return recommendedCourses.value.filter((course) => {
    const passGrade =
      gradeValue.value === 0 ||
      course.grade === getGradeText(gradeValue.value);

    return passGrade;
  });
});

const normalizedSearchText = computed(() => submittedCourseSearchText.value.trim().toLowerCase());
const isSearching = computed(() => normalizedSearchText.value.length > 0);

const courseMatchesSearch = (course: EnhancedCourse): boolean => {
  const keyword = normalizedSearchText.value;
  if (!keyword) {
    return true;
  }

  return [
    course.title,
    course.brief,
    course.description,
    course.subject,
    course.grade,
    course.level,
    course.teacherName,
    course.tag,
    ...(course.tags || []),
  ].some((value) => String(value || '').toLowerCase().includes(keyword));
};

const searchResultCourses = computed(() =>
  filteredCourses.value.filter((course) => courseMatchesSearch(course)),
);

const getDifficultyText = (difficulty?: number): string => {
  switch (difficulty) {
    case 1:
      return '初级';
    case 2:
      return '中级';
    case 3:
      return '高级';
    default:
      return '中级';
  }
};

const getGradeText = (difficulty?: number): string => {
  switch (difficulty) {
    case 1:
      return '小学';
    case 2:
      return '初中';
    case 3:
      return '高中';
    case 4:
      return '大学';
    default:
      return '高中';
  }
};

const parseTags = (rawTags?: string): string[] => {
  if (!rawTags) {
    return [];
  }

  try {
    const parsed = JSON.parse(rawTags) as unknown;
    if (Array.isArray(parsed)) {
      return parsed.filter((item): item is string => typeof item === 'string');
    }
    return [];
  } catch {
    return [];
  }
};

const mapCourse = (course: Record<string, unknown>): EnhancedCourse => {
  const tags = parseTags(course.tags as string | undefined);
  const categoryId = Number(course.categoryId || 0) || undefined;
  const categoryName =
    String(course.categoryName || '').trim() ||
    categories.value.find((item) => item.id === categoryId)?.name ||
    '';
  const subject = categoryName || tags[0] || '综合';

  const difficulty = Number(course.difficulty || 0);
  const durationSeconds = Number(course.totalDuration || 0);

  return {
    id: Number(course.id || 0),
    title: String(course.title || ''),
    brief: String(course.description || ''),
    cover: String(course.coverImage || ''),
    tag: subject,
    tags,
    tagColor: '#1989fa',
    level: getDifficultyText(difficulty),
    duration: Math.max(1, Math.floor(durationSeconds / 60) || 30),
    studentsCount: Number(course.studyCount || 0),
    subject,
    categoryId,
    categoryName,
    grade: getGradeText(difficulty),
    description: String(course.description || ''),
    teacherName: String(course.teacherName || ''),
    highlights: [],
    detailDescription: String(course.description || ''),
    detailChapters: [],
  };
};

const mapCategoryIcon = (icon?: string): string => {
  const iconText = String(icon || '').trim();
  const subjectIconMap: Record<string, string> = {
    语文: 'yuwen',
    数学: 'shuxue',
    英语: 'yingyu1',
    物理: 'wuli',
    化学: 'huaxue',
    政治: 'zhengzhi',
    历史: 'lishi',
    生物: 'shengwu',
    地理: 'dili-',
  };

  return subjectIconMap[iconText] || iconText || 'star';
};

const loadCourseCategories = async () => {
  try {
    const response: BaseResponse_List_CourseCategory_ =
      await CourseCategoryControllerService.getTopCategoriesUsingGet();
    const backendCategories = (response.data || [])
      .filter((item: CourseCategory) => item.id !== undefined && item.id !== null)
      .map((item: CourseCategory) => ({
        id: Number(item.id),
        name: item.name || `分类-${item.id}`,
        icon: mapCategoryIcon(item.icon || item.name),
        path: `/courses/category/${item.id}`,
      }));

    categories.value = [
      { id: 0, name: '推荐', icon: 'star', path: '/courses/recommended' },
      ...backendCategories,
    ];
  } catch (error) {
    console.error('加载课程分类失败', error);
    categories.value = [{ id: 0, name: '推荐', icon: 'star', path: '/courses/recommended' }];
  }
};

const toChapterPreview = (detail: CourseDetailResponse): ChapterPreview[] => {
  return (detail.chapters || []).map((chapter) => ({
    id: chapter.id,
    title: chapter.title,
    sections: (chapter.sections || []).map((section) => ({
      id: section.id,
      title: section.title,
    })),
  }));
};

const cacheSelectedCourse = (course: EnhancedCourse) => {
  sessionStorage.setItem(`course_${course.id}`, JSON.stringify(course));
};

const navigateToCourseStudy = (options?: { assistant?: boolean; sectionId?: number }) => {
  if (!selectedCourse.value) {
    return;
  }

  cacheSelectedCourse(selectedCourse.value);
  showDetailPopup.value = false;

  const query: Record<string, string> = {};
  if (options?.assistant) {
    query.assistant = '1';
  }
  if (options?.sectionId) {
    query.sectionId = String(options.sectionId);
  }

  router.push({
    path: `/courses/study/${selectedCourse.value.id}`,
    query,
  });
};

const loadRecommendedCourses = async () => {
  try {
    const response: BaseResponse_Page_CourseVO_ = await CourseControllerService.listCourseVoByPageUsingGet(
      activeCategory.value === 0 ? undefined : activeCategory.value,
      undefined,
      undefined,
      1,
      undefined,
      undefined,
      undefined,
      undefined,
      60,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
    );

    recommendedCourses.value = (response.data?.records || []).map((item) =>
      mapCourse(item as unknown as Record<string, unknown>),
    );
  } catch (error) {
    console.error('加载课程列表失败', error);
    showToast('加载课程列表失败');
    recommendedCourses.value = [];
  }
};

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};

const toggleCourseSearch = async () => {
  searchExpanded.value = true;
  recordHintVisible.value = false;

  if (searchExpanded.value) {
    await nextTick();
    searchInputRef.value?.focus();
  }
};

const submitCourseSearch = () => {
  window.clearTimeout(searchDebounceTimer.value);
  searchDebounceTimer.value = window.setTimeout(() => {
    submittedCourseSearchText.value = courseSearchText.value.trim();
  }, 300);
};

const clearCourseSearch = () => {
  window.clearTimeout(searchDebounceTimer.value);
  courseSearchText.value = '';
  submittedCourseSearchText.value = '';
  searchInputRef.value?.focus();
};

const closeCourseSearch = () => {
  window.clearTimeout(searchDebounceTimer.value);
  courseSearchText.value = '';
  submittedCourseSearchText.value = '';
  searchExpanded.value = false;
};

const handleLearningRecordClick = () => {
  if (!recordHintVisible.value) {
    searchExpanded.value = false;
    recordHintVisible.value = true;
    return;
  }

  router.push('/courses/history');
};

const selectCategory = (category: Category) => {
  activeCategory.value = category.id;
  const query: Record<string, string> = {
    ...(route.query as Record<string, string>),
    category: String(category.id),
  };

  if (category.id === 0) {
    delete query.grade;
    gradeValue.value = 0;
  }

  router.replace({
    path: route.path,
    query,
  });

  loadRecommendedCourses();
};

const selectGrade = (value: number) => {
  gradeValue.value = value;
  showDropdown.value = false;

  const query: Record<string, string> = {
    ...(route.query as Record<string, string>),
  };

  if (value === 0) {
    delete query.grade;
  } else {
    query.grade = String(value);
  }

  router.replace({
    path: route.path,
    query,
  });
};

const closeDropdownOnClickOutside = (event: MouseEvent) => {
  const dropdown = document.querySelector('.custom-dropdown');
  if (dropdown && !dropdown.contains(event.target as Node)) {
    showDropdown.value = false;
  }
};

const showCourseDetail = async (course: EnhancedCourse) => {
  selectedCourse.value = {
    ...course,
    detailDescription: course.description,
    detailChapters: [],
  };
  showDetailPopup.value = true;
  cacheSelectedCourse(selectedCourse.value);

  try {
    const detail = await queryCourseDetail(course.id);
    if (!selectedCourse.value || selectedCourse.value.id !== course.id) {
      return;
    }

    const mergedCourse: EnhancedCourse = {
      ...selectedCourse.value,
      teacherName: detail.teacherName || selectedCourse.value.teacherName,
      detailDescription: detail.content || detail.description || selectedCourse.value.detailDescription,
      detailChapters: toChapterPreview(detail),
    };

    selectedCourse.value = mergedCourse;
    cacheSelectedCourse(mergedCourse);
  } catch (error) {
    showToast(error instanceof Error ? error.message : '课程详情加载失败');
  }
};

const startLearning = () => {
  navigateToCourseStudy();
};

const enterLearningAssistant = () => {
  const firstSectionId = selectedCourse.value?.detailChapters?.[0]?.sections?.[0]?.id;
  navigateToCourseStudy({
    assistant: true,
    sectionId: firstSectionId,
  });
};

onMounted(async () => {
  window.addEventListener('click', closeDropdownOnClickOutside);
  await loadCourseCategories();

  const categoryId = Number(route.query.category || 0);
  const grade = Number(route.query.grade || 0);

  if (categoryId > 0 && categories.value.some((item) => item.id === categoryId)) {
    activeCategory.value = categoryId;
  }

  if (grade > 0 && gradeOptions.some((item) => item.value === grade)) {
    gradeValue.value = grade;
  }

  await loadRecommendedCourses();
});

onUnmounted(() => {
  window.clearTimeout(searchDebounceTimer.value);
  window.removeEventListener('click', closeDropdownOnClickOutside);
});
</script>

<style scoped>
.courses {
  --course-text: #1e293b;
  --course-muted: #64748b;
  --course-soft: #94a3b8;
  --course-primary: #4f46e5;
  --course-melon: #e0f0b0;
  --course-dew: #d1e5d0;
  --course-coral: #ffa7a6;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  padding-bottom: 84px;
  position: relative;
  overflow: hidden;
  color: var(--course-text);
  background:
    radial-gradient(circle at 18% 8%, rgba(224, 240, 176, 0.42), transparent 26%),
    radial-gradient(circle at 84% 4%, rgba(255, 255, 255, 0.58), transparent 28%),
    linear-gradient(
      180deg,
      rgba(224, 240, 176, 0.45) 0%,
      rgba(209, 229, 208, 0.38) 45%,
      rgba(255, 167, 166, 0.28) 100%
    );
}

.course-glow {
  position: fixed;
  z-index: 0;
  width: 190px;
  height: 190px;
  border-radius: 999px;
  filter: blur(34px);
  opacity: 0.28;
  pointer-events: none;
}

.glow-melon {
  top: 72px;
  left: -74px;
  background: #e0f0b0;
}

.glow-coral {
  right: -86px;
  bottom: 120px;
  background: #ffa7a6;
}

.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  padding: 16px 16px 10px;
  background:
    radial-gradient(circle at 15% 12%, rgba(224, 240, 176, 0.52), transparent 36%),
    radial-gradient(circle at 88% 0%, rgba(255, 167, 166, 0.18), transparent 32%),
    linear-gradient(180deg, rgba(248, 250, 252, 0.76), rgba(248, 250, 252, 0.36));
  border-bottom: 1px solid rgba(255, 255, 255, 0.38);
  backdrop-filter: blur(20px);
  box-shadow: 0 12px 30px rgba(80, 100, 120, 0.06);
}

.scrollable-content {
  position: relative;
  z-index: 1;
  flex: 1;
  overflow-y: auto;
  padding: 0 16px 28px;
  margin-top: 150px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding: 8px 0 12px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--course-text);
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 0;
}

.title-icon {
  width: 38px;
  height: 38px;
  margin-right: 0;
  border-radius: 15px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--course-primary);
  font-size: 22px;
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid rgba(255, 255, 255, 0.72);
  box-shadow: 0 10px 24px rgba(80, 100, 120, 0.08);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.action-button {
  width: 40px;
  height: 40px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #475569;
  background: rgba(255, 255, 255, 0.62);
  box-shadow: 0 10px 24px rgba(80, 100, 120, 0.08);
  backdrop-filter: blur(16px);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.action-button:active {
  transform: scale(0.94);
}

.action-icon {
  margin-left: 0;
  color: inherit;
  font-size: 21px;
  opacity: 0.9;
}

.grade-selector-container {
  margin: 2px 0 16px;
  display: flex;
  justify-content: flex-end;
  position: relative;
}

.custom-dropdown {
  width: 122px;
  position: relative;
}

.dropdown-trigger {
  height: 36px;
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 999px;
  padding: 0 12px 0 14px;
  background: rgba(255, 255, 255, 0.64);
  backdrop-filter: blur(16px);
  box-shadow: 0 8px 20px rgba(80, 100, 120, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
  color: #475569;
}

.dropdown-arrow {
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 5px 5px 0 5px;
  border-color: #94a3b8 transparent transparent transparent;
  margin-left: 6px;
}

.dropdown-content {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  width: 100%;
  z-index: 100;
  overflow: hidden;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(18px);
  box-shadow: 0 14px 32px rgba(80, 100, 120, 0.1);
}

.dropdown-option {
  padding: 11px 0;
  text-align: center;
  border-bottom: 0;
  font-size: 13px;
  color: #64748b;
  cursor: pointer;
}

.dropdown-option-active {
  color: var(--course-primary);
  font-weight: 800;
  background: rgba(224, 240, 176, 0.32);
}

.recommended,
.subject-courses {
  margin-top: 16px;
  margin-bottom: 18px;
}

.explore-panel {
  margin: 18px 0 18px;
  padding: 18px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.56);
  border: 1px solid rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(16px);
  box-shadow: 0 14px 32px rgba(80, 100, 120, 0.08);
}

.explore-copy h3 {
  margin: 0 0 14px;
  color: var(--course-text);
  font-size: 17px;
  font-weight: 800;
}

.explore-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 9px;
}

.explore-chip {
  height: 34px;
  padding: 0 13px;
  border: 1px solid rgba(255, 255, 255, 0.68);
  border-radius: 999px;
  color: #475569;
  font-size: 13px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 18px rgba(80, 100, 120, 0.06);
}

@media (max-width: 360px) {
  .fixed-header,
  .scrollable-content {
    padding-left: 12px;
    padding-right: 12px;
  }

  .page-title {
    font-size: 21px;
  }

  .action-button {
    width: 38px;
    height: 38px;
  }
}

@media (min-width: 769px) {
  .courses {
    border-right: 1px solid rgba(255, 255, 255, 0.38);
    border-left: 1px solid rgba(255, 255, 255, 0.38);
  }

  .fixed-header {
    left: 50%;
    right: auto;
    width: min(1100px, 100vw);
    transform: translateX(-50%);
  }

  .scrollable-content {
    max-width: 1100px;
    margin-right: auto;
    margin-left: auto;
    padding-right: 24px;
    padding-left: 24px;
  }
}
</style>

<style scoped>
.courses {
  --course-ink: #24304f;
  --course-muted: #6f7a96;
  --course-purple: #b8a7ff;
  --course-blue: #9bd7ff;
  --course-pink: #ffd3e6;
  --course-mint: #d5f5e8;
  min-height: 100vh;
  padding-bottom: 92px;
  overflow-x: hidden;
  overflow-y: auto;
  color: var(--course-ink);
  background:
    linear-gradient(180deg, rgba(248, 245, 255, 0.92) 0%, rgba(239, 248, 255, 0.88) 46%, rgba(255, 246, 250, 0.9) 100%);
  animation: courseFadeIn 0.38s ease both;
}

@keyframes courseFadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dream-cloud,
.dream-star {
  position: fixed;
  z-index: 0;
  pointer-events: none;
}

.dream-cloud {
  width: 98px;
  height: 34px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.62);
  filter: blur(0.2px);
}

.dream-cloud::before,
.dream-cloud::after {
  content: '';
  position: absolute;
  bottom: 8px;
  border-radius: 50%;
  background: inherit;
}

.dream-cloud::before {
  left: 18px;
  width: 38px;
  height: 38px;
}

.dream-cloud::after {
  right: 14px;
  width: 50px;
  height: 50px;
}

.cloud-left {
  top: 90px;
  left: -24px;
  opacity: 0.55;
}

.cloud-right {
  top: 220px;
  right: -34px;
  opacity: 0.42;
}

.dream-star {
  color: rgba(255, 255, 255, 0.92);
  text-shadow: 0 6px 20px rgba(139, 92, 246, 0.26);
  animation: floatSpark 3.4s ease-in-out infinite;
}

.star-one {
  top: 88px;
  left: 18%;
  font-size: 20px;
}

.star-two {
  top: 168px;
  right: 20%;
  font-size: 18px;
  animation-delay: 0.6s;
}

.star-three {
  top: 286px;
  left: 10%;
  font-size: 15px;
  animation-delay: 1.1s;
}

@keyframes floatSpark {
  0%,
  100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-8px) rotate(8deg);
  }
}

.course-hero {
  position: relative;
  z-index: 1;
  min-height: 336px;
  margin: 14px 14px 0;
  padding: 18px 16px 22px;
  border-radius: 30px;
  overflow: hidden;
  background:
    linear-gradient(135deg, rgba(190, 180, 255, 0.78) 0%, rgba(155, 215, 255, 0.72) 52%, rgba(255, 211, 230, 0.76) 100%);
  box-shadow: 0 24px 56px rgba(129, 140, 248, 0.24);
}

.course-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 18% 18%, rgba(255, 255, 255, 0.42), transparent 26%),
    radial-gradient(circle at 80% 16%, rgba(255, 255, 255, 0.36), transparent 24%);
  pointer-events: none;
}

.hero-glow {
  position: absolute;
  width: 190px;
  height: 190px;
  border-radius: 50%;
  filter: blur(42px);
  opacity: 0.38;
}

.hero-glow-purple {
  left: -60px;
  top: 34px;
  background: #a78bfa;
}

.hero-glow-blue {
  right: -42px;
  bottom: 20px;
  background: #7dd3fc;
}

.hero-moon {
  position: absolute;
  right: 22px;
  top: 24px;
  width: 46px;
  opacity: 0.72;
  animation: floatSpark 4s ease-in-out infinite;
}

.hero-stage {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 210px;
  align-items: center;
  gap: 10px;
  min-height: 280px;
}

.hero-kicker,
.panel-kicker {
  display: inline-flex;
  color: rgba(79, 70, 229, 0.78);
  font-size: 12px;
  font-weight: 900;
}

.hero-copy h1 {
  margin: 8px 0 10px;
  color: #ffffff;
  font-size: 34px;
  font-weight: 900;
  line-height: 1.08;
  text-shadow: 0 8px 22px rgba(91, 33, 182, 0.16);
}

.hero-copy p {
  max-width: 300px;
  margin: 0;
  color: rgba(255, 255, 255, 0.88);
  font-size: 14px;
  line-height: 1.7;
}

.mascot-wrap {
  position: relative;
  height: 240px;
}

.cloud-seat {
  position: absolute;
  left: 50%;
  bottom: 36px;
  width: 176px;
  height: 58px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.7);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.86), 0 16px 30px rgba(148, 163, 184, 0.18);
  transform: translateX(-50%);
}

.cloud-seat::before,
.cloud-seat::after {
  content: '';
  position: absolute;
  bottom: 18px;
  border-radius: 50%;
  background: inherit;
}

.cloud-seat::before {
  left: 28px;
  width: 66px;
  height: 66px;
}

.cloud-seat::after {
  right: 24px;
  width: 76px;
  height: 76px;
}

.hero-maomaoe {
  position: absolute;
  left: 50%;
  bottom: 58px;
  z-index: 2;
  width: 128px;
  transform: translateX(-50%);
  filter: drop-shadow(0 18px 28px rgba(91, 33, 182, 0.18));
  animation: mascotFloat 3.6s ease-in-out infinite;
}

@keyframes mascotFloat {
  0%,
  100% {
    transform: translateX(-50%) translateY(0);
  }
  50% {
    transform: translateX(-50%) translateY(-8px);
  }
}

.orbit-action {
  position: absolute;
  z-index: 4;
  min-width: 76px;
  height: 56px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 999px;
  padding: 0 12px;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: #4f46e5;
  font-size: 11px;
  font-weight: 900;
  background: rgba(255, 255, 255, 0.52);
  box-shadow: 0 14px 28px rgba(99, 102, 241, 0.14);
  backdrop-filter: blur(18px);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.orbit-action:active {
  transform: scale(0.95);
}

.orbit-action :deep(.van-icon) {
  font-size: 18px;
}

.record-action {
  top: 16px;
  right: 16px;
}

.orbit-left {
  left: -10px;
  top: 86px;
}

.orbit-right {
  right: -2px;
  top: 86px;
}

.orbit-bottom {
  left: 50%;
  bottom: 2px;
  transform: translateX(-50%);
}

.orbit-bottom:active {
  transform: translateX(-50%) scale(0.95);
}

.scrollable-content {
  position: relative;
  z-index: 2;
  margin-top: -14px;
  padding: 0 16px 28px;
  overflow: visible;
}

.course-glass-panel {
  margin-top: 16px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.48);
  box-shadow: 0 18px 46px rgba(99, 102, 241, 0.12);
  backdrop-filter: blur(20px);
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.panel-head h2 {
  margin: 4px 0 0;
  color: var(--course-ink);
  font-size: 22px;
  font-weight: 900;
}

.grade-selector-container {
  margin: 0;
  flex-shrink: 0;
}

.custom-dropdown {
  width: 126px;
  position: relative;
}

.dropdown-trigger {
  height: 38px;
  border: 1px solid rgba(255, 255, 255, 0.68);
  border-radius: 999px;
  padding: 0 13px;
  background: rgba(255, 255, 255, 0.58);
  backdrop-filter: blur(16px);
  box-shadow: 0 10px 22px rgba(99, 102, 241, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  color: #5b647c;
  font-size: 13px;
  font-weight: 800;
}

.dropdown-arrow {
  width: 0;
  height: 0;
  margin-left: 8px;
  border-style: solid;
  border-width: 5px 5px 0;
  border-color: #8b8fa8 transparent transparent;
}

.dropdown-content {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  z-index: 20;
  width: 100%;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.74);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: 0 18px 36px rgba(99, 102, 241, 0.12);
  backdrop-filter: blur(18px);
}

.dropdown-option {
  padding: 11px 0;
  color: #687089;
  font-size: 13px;
  font-weight: 700;
  text-align: center;
}

.dropdown-option-active {
  color: #4f46e5;
  background: rgba(232, 224, 255, 0.58);
}

.fixed-header,
.header,
.explore-panel,
.course-glow {
  display: none;
}

@media (hover: hover) {
  .orbit-action:hover {
    transform: translateY(-3px);
    background: rgba(255, 255, 255, 0.68);
    box-shadow: 0 18px 36px rgba(99, 102, 241, 0.18);
  }

  .orbit-bottom:hover {
    transform: translateX(-50%) translateY(-3px);
  }
}

@media (max-width: 720px) {
  .course-hero {
    min-height: 430px;
  }

  .hero-stage {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .hero-copy {
    padding-top: 18px;
    text-align: left;
  }

  .hero-copy h1 {
    font-size: 31px;
  }

  .mascot-wrap {
    margin-top: -4px;
    height: 230px;
  }
}

@media (max-width: 380px) {
  .course-hero {
    margin-right: 10px;
    margin-left: 10px;
    padding-right: 12px;
    padding-left: 12px;
  }

  .scrollable-content {
    padding-right: 12px;
    padding-left: 12px;
  }

  .orbit-action {
    min-width: 68px;
    height: 52px;
    font-size: 10px;
  }
}

@media (min-width: 769px) {
  .course-hero,
  .scrollable-content {
    max-width: 1100px;
    margin-right: auto;
    margin-left: auto;
  }
}

.course-hero {
  min-height: 390px;
  padding: 42px 48px 36px;
  background:
    radial-gradient(circle at 13% 12%, rgba(255, 255, 255, 0.42), transparent 22%),
    radial-gradient(circle at 82% 20%, rgba(255, 255, 255, 0.38), transparent 24%),
    linear-gradient(135deg, rgba(205, 194, 255, 0.9) 0%, rgba(190, 218, 255, 0.82) 50%, rgba(255, 220, 238, 0.86) 100%);
}

.hero-stage {
  grid-template-columns: minmax(280px, 0.92fr) minmax(360px, 1.08fr);
  min-height: 310px;
}

.hero-copy {
  position: relative;
  z-index: 3;
}

.hero-kicker {
  color: #7868f2;
  font-size: 15px;
}

.hero-copy h1 {
  margin: 14px 0 16px;
  color: #26314f;
  font-size: 46px;
  letter-spacing: 0;
  text-shadow: none;
}

.hero-copy p {
  max-width: 360px;
  color: #727b99;
  font-size: 17px;
}

.hero-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 26px;
}

.hero-badges span {
  height: 34px;
  padding: 0 16px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  color: #7868f2;
  font-size: 13px;
  font-weight: 900;
  background: rgba(255, 255, 255, 0.42);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.68);
  backdrop-filter: blur(14px);
}

.hero-visual {
  position: relative;
  z-index: 2;
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-learning {
  width: min(460px, 100%);
  max-height: 310px;
  object-fit: contain;
  filter: drop-shadow(0 24px 34px rgba(106, 88, 205, 0.16));
  animation: mascotFloat 3.8s ease-in-out infinite;
}

.hero-actions {
  position: absolute;
  top: 28px;
  right: 32px;
  z-index: 5;
  display: flex;
  gap: 18px;
}

.hero-icon-action {
  width: 56px;
  height: 56px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #715ef2;
  font-size: 25px;
  background: rgba(255, 255, 255, 0.66);
  box-shadow: 0 16px 34px rgba(99, 102, 241, 0.14);
  backdrop-filter: blur(18px);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.hero-icon-action.active {
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 18px 38px rgba(99, 102, 241, 0.22);
}

.hero-icon-action:active {
  transform: scale(0.95);
}

.course-search-popover {
  position: absolute;
  top: 96px;
  right: 118px;
  z-index: 6;
  width: min(330px, calc(100% - 64px));
  height: 48px;
  padding: 0 12px 0 16px;
  border: 1px solid rgba(255, 255, 255, 0.76);
  border-radius: 999px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #7566e8;
  background: rgba(255, 255, 255, 0.52);
  box-shadow: 0 18px 38px rgba(99, 102, 241, 0.16);
  backdrop-filter: blur(20px);
}

.course-search-popover input {
  width: 100%;
  min-width: 0;
  border: 0;
  outline: none;
  color: #26314f;
  font-size: 15px;
  font-weight: 700;
  background: transparent;
}

.course-search-popover input::placeholder {
  color: rgba(111, 122, 150, 0.76);
}

.course-search-popover button {
  width: 28px;
  height: 28px;
  border: 0;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #8a82ac;
  background: rgba(255, 255, 255, 0.58);
}

.record-hint {
  position: absolute;
  top: 100px;
  right: 22px;
  z-index: 6;
  width: 110px;
  color: #7566e8;
  font-size: 13px;
  font-weight: 900;
  line-height: 1.45;
  text-align: center;
}

.record-hint::before {
  content: '↑';
  display: block;
  margin-bottom: 4px;
  font-size: 26px;
  line-height: 1;
}

.hero-moon {
  right: 270px;
  top: 72px;
  width: 54px;
  height: 54px;
  border-radius: 50%;
  background: #fff2a8;
  box-shadow: inset -15px 1px 0 rgba(255, 255, 255, 0.72), 0 10px 28px rgba(255, 242, 168, 0.3);
}

.hero-planet {
  position: absolute;
  right: 160px;
  bottom: 116px;
  z-index: 1;
  width: 74px;
  height: 74px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 222, 236, 0.9), rgba(189, 178, 255, 0.88));
  box-shadow: 0 14px 34px rgba(189, 178, 255, 0.22);
}

.hero-planet::after {
  content: '';
  position: absolute;
  left: -16px;
  top: 32px;
  width: 106px;
  height: 12px;
  border: 4px solid rgba(255, 255, 255, 0.42);
  border-top-color: rgba(255, 240, 184, 0.58);
  border-radius: 50%;
  transform: rotate(-18deg);
}

@media (hover: hover) {
  .hero-icon-action:hover {
    transform: translateY(-3px);
    background: rgba(255, 255, 255, 0.78);
    box-shadow: 0 20px 42px rgba(99, 102, 241, 0.2);
  }
}

@media (max-width: 720px) {
  .course-hero {
    min-height: 520px;
    padding: 74px 24px 26px;
  }

  .hero-actions {
    top: 20px;
    right: 22px;
    gap: 12px;
  }

  .hero-icon-action {
    width: 48px;
    height: 48px;
    font-size: 22px;
  }

  .course-search-popover {
    top: 82px;
    right: 18px;
    width: calc(100% - 36px);
  }

  .record-hint {
    top: 82px;
    right: 14px;
  }

  .hero-stage {
    grid-template-columns: 1fr;
  }

  .hero-copy h1 {
    font-size: 36px;
  }

  .hero-visual {
    min-height: 250px;
  }

  .hero-learning {
    max-height: 260px;
  }

  .hero-moon {
    right: 120px;
    top: 110px;
  }

  .hero-planet {
    right: 28px;
    bottom: 150px;
    width: 58px;
    height: 58px;
  }
}

.course-hero {
  max-width: 980px;
  min-height: 318px;
  margin-top: 10px;
  padding: 30px 42px 26px;
  border-radius: 26px;
}

.hero-stage {
  min-height: 260px;
  grid-template-columns: minmax(260px, 0.92fr) minmax(300px, 1.08fr);
  align-items: start;
}

.hero-copy {
  padding-top: 28px;
}

.hero-copy h1 {
  margin: 10px 0 12px;
  font-size: 40px;
}

.hero-copy p {
  font-size: 15px;
}

.hero-badges {
  margin-top: 20px;
}

.hero-visual {
  min-height: 250px;
  align-items: flex-start;
}

.hero-learning {
  width: min(380px, 100%);
  max-height: 250px;
  margin-top: -4px;
}

.hero-actions {
  top: 26px;
  right: 30px;
  gap: 14px;
}

.hero-icon-action {
  width: 46px;
  height: 46px;
  font-size: 21px;
}

.course-search-popover {
  top: 82px;
  right: 88px;
  height: 42px;
}

.record-hint {
  top: 82px;
  right: 12px;
}

.hero-moon {
  right: 268px;
  top: 60px;
  width: 46px;
  height: 46px;
}

.hero-planet {
  right: 150px;
  bottom: 74px;
  width: 62px;
  height: 62px;
}

.scrollable-content {
  margin-top: -24px;
  padding-bottom: 20px;
}

.course-glass-panel {
  margin-top: 10px;
  padding: 14px;
  border-radius: 24px;
}

@media (min-width: 769px) {
  .course-hero,
  .scrollable-content {
    max-width: 980px;
  }
}

@media (max-width: 720px) {
  .course-hero {
    min-height: 444px;
    padding: 62px 22px 22px;
  }

  .hero-copy {
    padding-top: 0;
  }

  .hero-copy h1 {
    font-size: 32px;
  }

  .hero-visual {
    min-height: 210px;
  }

  .hero-learning {
    max-height: 220px;
  }

  .hero-icon-action {
    width: 42px;
    height: 42px;
    font-size: 20px;
  }
}

.course-hero.search-active .hero-copy {
  padding-top: 66px;
}

.course-hero.search-active .hero-stage {
  grid-template-columns: minmax(260px, 0.88fr) minmax(320px, 1.12fr);
}

.course-search-popover {
  top: 24px;
  left: 42px;
  right: auto;
  z-index: 4;
  width: 260px;
  max-width: calc(100% - 120px);
  height: 42px;
  padding: 0 8px 0 14px;
}

.course-search-popover button {
  flex-shrink: 0;
  cursor: pointer;
}

.scrollable-content {
  margin-top: -36px;
  padding-bottom: 92px;
}

.course-glass-panel {
  margin-top: 4px;
}

.search-count {
  margin: 4px 0 0;
  color: #7a84a3;
  font-size: 13px;
  font-weight: 800;
}

.search-results {
  margin-top: 4px;
}

@media (max-width: 720px) {
  .course-search-popover {
    top: 16px;
    left: 18px;
    width: min(260px, calc(100% - 84px));
  }

  .course-hero.search-active .hero-copy {
    padding-top: 46px;
  }

  .scrollable-content {
    margin-top: -30px;
    padding-bottom: 96px;
  }
}
</style>
