<template>
  <div class="popular-courses-page">
    <!-- 返回按钮 -->
    <back-button title="热门课程" />

    <!-- 主要内容区域 -->
    <div class="content-container">
      <!-- 课程分类标签 -->
      <van-tabs v-model:active="activeCategoryId" sticky swipeable>
        <van-tab title="全部" :name="0">
          <van-loading v-if="loading" class="course-loading" size="24px" vertical>加载中...</van-loading>
          <course-list
            v-else
            title="推荐课程"
            :courses="displayedCourses"
            class-name="recommended"
            @select="showCourseDetail"
          />
        </van-tab>
        <van-tab
          v-for="category in categories"
          :key="category.id"
          :title="category.name"
          :name="category.id"
        >
          <van-loading v-if="loading" class="course-loading" size="24px" vertical>加载中...</van-loading>
          <course-list
            v-else
            :title="category.name + '课程'"
            :courses="displayedCourses"
            class-name="category-courses"
            @select="showCourseDetail"
          />
        </van-tab>
      </van-tabs>
    </div>

    <!-- 课程详情弹出层 -->
    <course-detail
      v-model="showDetailPopup"
      :course="selectedCourse"
      @close="showDetailPopup = false"
      @start="startLearning"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { CourseList, CourseDetail } from '../../components/Course';
import { BackButton } from '../../components/Common';
import { queryHotRecommendCourses, queryRecommendCourses, type CourseItem } from '../../api/course';
import {
  CourseCategoryControllerService,
  type CourseCategory,
} from '../../services';

// 定义类型
interface CourseHighlight {
  icon: string;
  color: string;
  text: string;
}

// 基本课程类型
interface BaseCourse {
  id: number;
  title: string;
  brief: string;
  cover: string;
  tag: string;
  tagColor: string;
  level: string;
  duration: number;
  studentsCount: number;
  subject?: string;
  categoryId?: number;
  categoryName?: string;
  grade?: string;
  teacherName?: string;
  description?: string;
  highlights?: CourseHighlight[];
}

// 扩展课程类型
interface EnhancedCourse extends BaseCourse {
  description?: string;
  highlights?: CourseHighlight[];
}

interface Category {
  id: number;
  name: string;
  icon: string;
}

const router = useRouter();
const showDetailPopup = ref(false);
const selectedCourse = ref<EnhancedCourse | null>(null);
const activeCategoryId = ref(0);
const allCourses = ref<EnhancedCourse[]>([]);
const categories = ref<Category[]>([]);
const loading = ref(true);
const requestSeq = ref(0);

const RECOMMEND_LIMIT = 60;
const MIN_DISPLAY_COUNT = 8;

const displayedCourses = computed(() => allCourses.value);

const currentCategory = computed(() =>
  activeCategoryId.value === 0
    ? undefined
    : categories.value.find((category) => category.id === activeCategoryId.value),
);

const categoryNameMap = computed(() =>
  categories.value.reduce<Record<number, string>>((map, category) => {
    map[category.id] = category.name;
    return map;
  }, {}),
);

// 显示课程详情
const showCourseDetail = (course: EnhancedCourse) => {
  selectedCourse.value = course;
  showDetailPopup.value = true;
};

// 开始学习
const startLearning = () => {
  showDetailPopup.value = false;
  if (selectedCourse.value) {
    router.push(`/courses/study/${selectedCourse.value.id}`);
  }
};

const difficultyTextMap: Record<number, string> = {
  1: '初级',
  2: '中级',
  3: '高级',
  4: '专家',
};

const getDifficultyText = (difficulty?: string | number): string => {
  if (typeof difficulty === 'number') {
    return difficultyTextMap[difficulty] || '课程';
  }
  return difficulty || '课程';
};

const getCourseCover = (course: CourseItem): string => {
  return course.coverImage || course.coverUrl || '/logo.svg';
};

const getCourseDuration = (course: CourseItem): number => {
  const duration = Number(course.totalDuration || 0);
  if (duration <= 0) {
    return 30;
  }
  return duration > 180 ? Math.ceil(duration / 60) : duration;
};

const convertCourseToCard = (course: CourseItem, category?: Category): BaseCourse => {
  const backendCategoryName =
    course.categoryName ||
    (course.categoryId ? categoryNameMap.value[Number(course.categoryId)] : '');
  const subject = backendCategoryName || category?.name || '热门';
  const level = getDifficultyText(course.difficulty);

  return {
    id: course.id,
    title: course.title || '',
    brief: course.subtitle || course.description || '',
    cover: getCourseCover(course),
    tag: subject,
    tagColor: '#ff8c00',
    level,
    duration: getCourseDuration(course),
    studentsCount: course.studyCount || course.studentCount || 0,
    subject,
    categoryId: course.categoryId || category?.id,
    categoryName: subject,
    teacherName: course.teacherName || '',
    description: course.description || course.subtitle || '',
  };
};

const fetchFallbackCourses = async (missingCount: number, category?: Category): Promise<CourseItem[]> => {
  if (missingCount <= 0) {
    return [];
  }

  const limit = Math.max(missingCount, MIN_DISPLAY_COUNT);

  try {
    return await queryHotRecommendCourses(limit, category?.id);
  } catch (error) {
    console.warn('热门课程补充失败，改用默认推荐课程补充:', error);
    return queryRecommendCourses({ limit, categoryId: category?.id });
  }
};

const isSameCategoryCourse = (course: CourseItem, category: Category): boolean => {
  const courseCategoryId = Number(course.categoryId || 0);
  if (courseCategoryId > 0) {
    return courseCategoryId === category.id;
  }

  const courseCategoryName = String(course.categoryName || '').trim();
  return courseCategoryName !== '' && courseCategoryName === category.name;
};

// 为课程添加描述和亮点
const enhanceCourse = (course: BaseCourse): EnhancedCourse => {
  return {
    ...course,
    // 如果没有描述，则创建一个标准描述
    description:
      course.description ||
      `本课程是${course.subject || ''}学科的${course.level}课程，${course.brief}`,
    // 如果没有亮点，则根据课程信息创建标准亮点
    highlights: course.highlights || [
      {
        icon:
          course.level === '初级'
            ? 'smile-o'
            : course.level === '中级'
              ? 'bulb-o'
              : 'certificate',
        color: course.tagColor,
        text: `${course.level}级别`,
      },
      { icon: 'clock-o', color: '#1989fa', text: `${course.duration}分钟` },
      {
        icon: 'friends-o',
        color: '#07c160',
        text: `${course.studentsCount}人学习`,
      },
    ],
  };
};

// 加载课程数据
const loadCourses = async (category?: Category) => {
  const currentRequestSeq = requestSeq.value + 1;
  requestSeq.value = currentRequestSeq;
  loading.value = true;

  try {
    const coursesData = await queryRecommendCourses({
      limit: RECOMMEND_LIMIT,
      categoryId: category?.id,
    });
    const visibleCourses = category
      ? coursesData.filter((course) => isSameCategoryCourse(course, category))
      : coursesData;
    const fallbackCourses =
      visibleCourses.length < MIN_DISPLAY_COUNT
        ? await fetchFallbackCourses(MIN_DISPLAY_COUNT - visibleCourses.length, category)
        : [];

    if (currentRequestSeq !== requestSeq.value) {
      return;
    }

    const primaryIds = new Set(visibleCourses.map((course) => course.id));
    const primaryCourseCards = visibleCourses.map((course) =>
      enhanceCourse(convertCourseToCard(course, category)),
    );
    const fallbackCourseCards = fallbackCourses
      .filter((course) => !primaryIds.has(course.id))
      .filter((course) => !category || isSameCategoryCourse(course, category))
      .map((course) => enhanceCourse(convertCourseToCard(course)));

    allCourses.value = [...primaryCourseCards, ...fallbackCourseCards];
  } catch (error) {
    console.error('加载课程数据失败:', error);
    showToast('数据加载失败，请重试');
    allCourses.value = [];
  } finally {
    loading.value = false;
  }
};

const loadCategories = async () => {
  try {
    const response = await CourseCategoryControllerService.getTopCategoriesUsingGet();
    categories.value = (response.data || [])
      .filter((category: CourseCategory) => category.id !== undefined && category.id !== null)
      .map((category: CourseCategory) => ({
        id: Number(category.id),
        name: category.name || `分类-${category.id}`,
        icon: category.icon || '',
      }));
  } catch (error) {
    console.error('加载课程分类失败:', error);
    categories.value = [];
  }
};

const openDetailFromQuery = () => {
  const searchParams = new URLSearchParams(location.search);
  const showDetail = searchParams.get('showDetail');
  const courseId = searchParams.get('courseId');

  if (showDetail === 'true' && courseId) {
    const courseIdNum = parseInt(courseId, 10);
    const course = allCourses.value.find((c) => c.id === courseIdNum);
    if (course) {
      showCourseDetail(course);
    }
  }
};

watch(activeCategoryId, () => {
  loadCourses(currentCategory.value);
});

// 组件挂载时加载数据
onMounted(async () => {
  await loadCategories();
  await loadCourses();
  openDetailFromQuery();
});
</script>

<style scoped>
.popular-courses-page {
  min-height: 100vh;
  background-color: #f7f8fa;
  display: flex;
  flex-direction: column;
}

.content-container {
  flex: 1;
  margin-top: 46px; /* 为返回按钮留出空间 */
  padding-bottom: 20px;
}

:deep(.van-tabs) {
  background: #fff;
}

:deep(.van-sticky--fixed) {
  z-index: 999;
}

:deep(.van-tabs__line) {
  background-color: #1989fa;
}

:deep(.van-tab) {
  font-size: 14px;
  color: #323233;
}

:deep(.van-tab--active) {
  font-weight: 700;
  color: #1989fa;
}

:deep(.van-tab__pane) {
  padding: 12px 0;
}

.course-loading {
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
