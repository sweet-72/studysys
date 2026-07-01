<template>
  <div class="search-page">
    <div class="search-shell">
      <header class="search-header">
        <button class="back-btn" type="button" aria-label="返回" @click="handleBack">
          <van-icon name="arrow-left" />
        </button>

        <form class="search-field" @submit.prevent="submitSearch">
          <van-icon name="search" class="search-icon" />
          <input
            v-model="searchValue"
            class="search-control"
            :placeholder="placeholder"
            type="search"
            @keyup.enter="submitSearch"
          />
          <button
            v-if="searchValue"
            class="clear-btn"
            type="button"
            aria-label="清空"
            @click="handleClear"
          >
            <van-icon name="cross" />
          </button>
          <button class="submit-btn" type="submit" aria-label="搜索">
            <van-icon name="search" />
          </button>
        </form>
      </header>

      <section class="assist-panel">
        <div class="assist-block">
          <h3>热门搜索</h3>
          <div class="tag-list">
            <button
              v-for="tag in randomHotWords"
              :key="tag"
              class="hot-tag"
              type="button"
              @click="searchByText(tag)"
            >
              {{ tag }}
            </button>
          </div>
        </div>

        <div v-if="filteredRecommendations.length && searchValue" class="assist-block">
          <h3>搜索建议</h3>
          <div class="tag-list">
            <button
              v-for="tag in filteredRecommendations"
              :key="tag"
              class="hot-tag"
              type="button"
              @click="searchByText(tag)"
            >
              {{ tag }}
            </button>
          </div>
        </div>

        <div v-if="searchStore.searchHistory.length && !searchValue" class="assist-block">
          <h3>搜索历史</h3>
          <div class="history-list">
            <button
              v-for="(record, index) in processedHistory"
              :key="`${record}-${index}`"
              class="history-item"
              type="button"
              @click="searchByText(record)"
            >
              <van-icon name="clock-o" />
              <span>{{ record }}</span>
              <van-icon name="cross" @click.stop="deleteRecord(index)" />
            </button>
          </div>
        </div>
      </section>

      <nav v-if="searchExecuted" class="category-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          type="button"
          :class="['tab-pill', { active: activeTab === tab.value }]"
          @click="activeTab = tab.value"
        >
          {{ tab.label }}
        </button>
      </nav>

      <main class="search-results">
        <div v-if="loading" class="loading-card">
          <van-loading type="spinner" color="#7c3aed" />
          <span>搜索中...</span>
        </div>

        <template v-else-if="searchExecuted">
          <div v-if="serviceWarning" class="service-warning">
            {{ serviceWarning }}
          </div>

          <div v-if="activeTab === 'all' && mixedResults.length" class="mixed-list">
            <article
              v-for="result in mixedResults"
              :key="result.key"
              class="mixed-card"
              @click="openMixedResult(result)"
            >
              <span :class="['type-badge', result.type]">{{ result.label }}</span>
              <van-image
                v-if="result.cover"
                :src="result.cover"
                class="mixed-cover"
                fit="cover"
                radius="14px"
              />
              <div class="mixed-content">
                <h3 v-html="highlightText(result.title)"></h3>
                <p v-if="result.summary" v-html="highlightText(result.summary)"></p>
                <div class="mixed-meta">
                  <span v-for="meta in result.meta" :key="meta">{{ meta }}</span>
                </div>
              </div>
            </article>
          </div>

          <div v-else-if="activeTab === 'posts'" class="result-stack">
            <post-result-item
              v-for="item in postResults.list"
              :key="`post-${item.id}`"
              :post="item"
              :keyword="currentKeyword"
              @click="navigateToPost(item.id)"
            />
            <empty-card v-if="!postResults.list.length" text="没有找到相关帖子" />
          </div>

          <div v-else-if="activeTab === 'articles'" class="result-stack">
            <article-result-item
              v-for="item in articleResults.list"
              :key="`article-${item.id}`"
              :article="item"
              :keyword="currentKeyword"
            />
            <empty-card v-if="!articleResults.list.length" text="没有找到相关文章" />
          </div>

          <div v-else-if="activeTab === 'words'" class="result-stack">
            <word-result-item
              v-for="item in wordResults.list"
              :key="`word-${item.id}`"
              :word="item"
              :keyword="currentKeyword"
            />
            <empty-card v-if="!wordResults.list.length" text="没有找到相关单词" />
          </div>

          <empty-card v-else text="没有找到相关内容，试试换个关键词～" />
        </template>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import { mockRecommendations } from '../../api/mock.ts';
import { queryAiAvatarPage, resolveAiAvatarImage } from '../../api/aiAvatar';
import { useSearchStore } from '../../stores/searchStore.ts';
import { PostResultItem, ArticleResultItem, WordResultItem } from '../../components/Search';
import { PostControllerService } from '../../services/services/PostControllerService';
import { DailyArticleControllerService } from '../../services/services/DailyArticleControllerService';
import { DailyWordControllerService } from '../../services/services/DailyWordControllerService';
import { CourseControllerService } from '../../services/services/CourseControllerService';

type TabValue = 'all' | 'posts' | 'articles' | 'words';
type ResultType = 'course' | 'ai' | 'post' | 'article' | 'word';

interface PageState {
  list: any[];
  total: number;
  pageSize: number;
  current: number;
}

interface SafeResult {
  ok: boolean;
  data?: any;
  error?: unknown;
}

interface MixedResult {
  key: string;
  type: ResultType;
  label: string;
  priority: number;
  item: any;
  title: string;
  summary: string;
  cover: string;
  meta: string[];
}

const EmptyCard = defineComponent({
  props: {
    text: { type: String, required: true },
  },
  setup(props) {
    return () => h('div', { class: 'empty-card' }, [
      h('div', { class: 'empty-mark' }, '⌕'),
      h('h3', props.text),
      h('p', '没有找到相关内容，试试换个关键词～'),
    ]);
  },
});

const router = useRouter();
const route = useRoute();
const searchStore = useSearchStore();

const placeholder = '请输入搜索内容';
const searchValue = ref('');
const currentKeyword = ref('');
const loading = ref(false);
const searchExecuted = ref(false);
const serviceWarning = ref('');
const activeTab = ref<TabValue>('all');
const randomHotWords = ref<string[]>([]);

const tabs: Array<{ label: string; value: TabValue }> = [
  { label: '全部', value: 'all' },
  { label: '帖子', value: 'posts' },
  { label: '文章', value: 'articles' },
  { label: '单词', value: 'words' },
];

const createPageState = (): PageState => ({
  list: [],
  total: 0,
  pageSize: 10,
  current: 1,
});

const postResults = ref<PageState>(createPageState());
const articleResults = ref<PageState>(createPageState());
const wordResults = ref<PageState>(createPageState());
const courseResults = ref<PageState>(createPageState());
const aiResults = ref<PageState>(createPageState());

const filteredRecommendations = computed(() => {
  const keyword = searchValue.value.trim().toLowerCase();
  if (!keyword) return [];
  return mockRecommendations
    .filter((item) => item.toLowerCase().includes(keyword))
    .slice(0, 8);
});

const processedHistory = computed(() => searchStore.searchHistory.slice(0, 7));

const mixedResults = computed<MixedResult[]>(() => {
  const courses = courseResults.value.list.map((item) => toMixedResult('course', item));
  const ai = aiResults.value.list.map((item) => toMixedResult('ai', item));
  const posts = postResults.value.list.map((item) => toMixedResult('post', item));
  const articles = articleResults.value.list.map((item) => toMixedResult('article', item));
  const words = wordResults.value.list.map((item) => toMixedResult('word', item));
  return [...courses, ...ai, ...posts, ...articles, ...words]
    .filter((item): item is MixedResult => Boolean(item))
    .sort((a, b) => a.priority - b.priority);
});

const getQueryKeyword = (): string => {
  const value = route.query.keyword;
  return typeof value === 'string' ? value.trim() : '';
};

const resetResults = (): void => {
  postResults.value = createPageState();
  articleResults.value = createPageState();
  wordResults.value = createPageState();
  courseResults.value = createPageState();
  aiResults.value = createPageState();
  serviceWarning.value = '';
};

const normalizePage = (data: any): PageState => ({
  list: Array.isArray(data?.records) ? data.records : Array.isArray(data) ? data : [],
  total: Number(data?.total || data?.records?.length || 0),
  pageSize: Number(data?.size || data?.pageSize || 10),
  current: Number(data?.current || 1),
});

const safeSearch = async (label: string, request: Promise<any>): Promise<SafeResult> => {
  try {
    const response = await request;
    console.log(`${label} res`, response);
    return { ok: true, data: response?.data ?? response };
  } catch (error) {
    console.log(`${label} res`, error);
    return { ok: false, error };
  }
};

const fetchCourseResults = (keyword: string) =>
  CourseControllerService.listCourseVoByPageUsingGet(
    undefined,
    undefined,
    undefined,
    1,
    undefined,
    undefined,
    undefined,
    undefined,
    10,
    undefined,
    undefined,
    undefined,
    undefined,
    undefined,
    keyword,
  );

const runSearch = async (keyword: string): Promise<void> => {
  const query = keyword.trim();
  if (!query) {
    showToast('请输入搜索内容');
    return;
  }

  resetResults();
  loading.value = true;
  searchExecuted.value = true;
  currentKeyword.value = query;
  searchStore.addSearchHistory(query);

  console.log('[search-debug] keyword=', query);
  console.log('[search-debug] activeTab=', activeTab.value);

  const [postRes, articleRes, wordRes, courseRes, aiRes] = await Promise.all([
    safeSearch('post', PostControllerService.searchPostVoByPageUsingGet(query)),
    safeSearch('article', DailyArticleControllerService.searchDailyArticleUsingGet(query)),
    safeSearch('word', DailyWordControllerService.searchDailyWordUsingGet(query)),
    safeSearch('course', fetchCourseResults(query)),
    safeSearch('ai', queryAiAvatarPage({ current: 1, pageSize: 10, keyword: query })),
  ]);

  console.log('[search-debug] post raw=', postRes);
  console.log('[search-debug] article raw=', articleRes);
  console.log('[search-debug] word raw=', wordRes);

  postResults.value = postRes.ok ? normalizePage(postRes.data) : createPageState();
  articleResults.value = articleRes.ok ? normalizePage(articleRes.data) : createPageState();
  wordResults.value = wordRes.ok ? normalizePage(wordRes.data) : createPageState();
  courseResults.value = courseRes.ok ? normalizePage(courseRes.data) : createPageState();
  aiResults.value = aiRes.ok ? normalizePage(aiRes.data) : createPageState();

  console.log('[search-debug] post list length=', postResults.value.list.length);
  console.log('[search-debug] article list length=', articleResults.value.list.length);
  console.log('[search-debug] word list length=', wordResults.value.list.length);
  console.log('[search-debug] course list length=', courseResults.value.list.length);
  console.log('[search-debug] ai list length=', aiResults.value.list.length);
  console.log('[search-debug] combined length=', mixedResults.value.length);

  const coreSearchFailed = !postRes.ok || !articleRes.ok || !wordRes.ok;
  if (coreSearchFailed) {
    serviceWarning.value = '搜索服务暂不可用，部分结果可能没有加载出来。';
    showToast('搜索服务暂不可用');
  }

  loading.value = false;
};

const submitSearch = (): void => {
  const keyword = searchValue.value.trim();
  if (!keyword) {
    showToast('请输入搜索内容');
    return;
  }

  router.push({
    path: '/search',
    query: { keyword },
  });
};

const searchByText = (text: string): void => {
  searchValue.value = text;
  submitSearch();
};

const handleClear = (): void => {
  searchValue.value = '';
  currentKeyword.value = '';
  searchExecuted.value = false;
  activeTab.value = 'all';
  resetResults();
};

const deleteRecord = (index: number): void => {
  searchStore.deleteSearchRecord(index);
};

const handleBack = (): void => {
  if (window.history.state?.back) {
    router.go(-1);
    return;
  }
  router.replace('/');
};

const navigateToPost = (id?: number): void => {
  if (id) router.push(`/circle/post/${id}`);
};

const navigateToCourse = (course: any): void => {
  if (!course?.id) return;
  router.push({
    path: '/courses/popular',
    query: { showDetail: 'true', courseId: course.id },
  });
};

const navigateToAssistant = (id?: number): void => {
  if (id) router.push(`/chat/detail/${id}`);
};

const openMixedResult = (result: MixedResult): void => {
  if (result.type === 'course') {
    navigateToCourse(result.item);
  } else if (result.type === 'ai') {
    navigateToAssistant(result.item.id);
  } else if (result.type === 'post') {
    navigateToPost(result.item.id);
  }
};

const getCourseCover = (course: any): string => (
  course.coverImage ||
  course.coverUrl ||
  course.imageUrl ||
  course.courseCover ||
  ''
);

const truncate = (value: string, max = 96): string => (
  value.length > max ? `${value.slice(0, max)}...` : value
);

const toMixedResult = (type: ResultType, item: any): MixedResult | null => {
  const config: Record<ResultType, { label: string; priority: number }> = {
    course: { label: '课程', priority: 1 },
    ai: { label: 'AI助手', priority: 2 },
    post: { label: '帖子', priority: 3 },
    article: { label: '文章', priority: 4 },
    word: { label: '单词', priority: 5 },
  };

  const titleMap: Record<ResultType, string> = {
    course: item.title || item.courseName || '未命名课程',
    ai: item.name || item.title || 'AI助手',
    post: item.title || '未命名帖子',
    article: item.title || '未命名文章',
    word: item.word || item.text || '',
  };
  const summaryMap: Record<ResultType, string> = {
    course: item.description || item.summary || item.content || '暂无课程简介',
    ai: item.description || item.abilities || '暂无简介',
    post: item.content || item.summary || '',
    article: item.summary || item.brief || item.content || '',
    word: item.translation || item.example || '',
  };

  const title = titleMap[type];
  if (!title) return null;

  const metaMap: Record<ResultType, string[]> = {
    course: [item.categoryName, item.teacherName].filter(Boolean),
    ai: [item.category, item.modelType].filter(Boolean),
    post: [
      item.userVO?.userName || item.userName || item.author,
      `点赞 ${item.thumbNum ?? item.likeCount ?? 0}`,
      `评论 ${item.commentNum ?? item.commentsNum ?? item.favourNum ?? 0}`,
    ].filter(Boolean),
    article: [item.category, item.publishDate].filter(Boolean),
    word: [item.pronunciation || item.phonetic, item.category].filter(Boolean),
  };

  return {
    key: `${type}-${item.id || title}`,
    type,
    label: config[type].label,
    priority: config[type].priority,
    item,
    title,
    summary: truncate(summaryMap[type] || ''),
    cover: type === 'course'
      ? getCourseCover(item)
      : type === 'ai'
        ? resolveAiAvatarImage(item.avatarImgUrl)
        : item.coverImage || item.coverUrl || '',
    meta: metaMap[type],
  };
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
  const keyword = currentKeyword.value.trim();
  if (!keyword) return text;
  const reg = new RegExp(`(${escapeRegExp(keyword)})`, 'gi');
  return text.replace(reg, '<span class="highlight">$1</span>');
};

const generateRandomHotWords = (): void => {
  randomHotWords.value = [...mockRecommendations]
    .sort(() => Math.random() - 0.5)
    .slice(0, 8);
};

watch(
  () => route.query.keyword,
  async () => {
    const keyword = getQueryKeyword();
    searchValue.value = keyword;
    activeTab.value = 'all';

    if (keyword) {
      await runSearch(keyword);
    } else {
      handleClear();
    }
  },
);

onMounted(() => {
  generateRandomHotWords();
  const keyword = getQueryKeyword();
  if (keyword) {
    searchValue.value = keyword;
    runSearch(keyword);
  }
});
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  padding: 18px 16px 112px;
  background:
    radial-gradient(circle at 14% 10%, rgba(167, 243, 208, 0.28), transparent 28%),
    radial-gradient(circle at 86% 12%, rgba(244, 114, 182, 0.18), transparent 30%),
    radial-gradient(circle at 52% 0%, rgba(139, 92, 246, 0.18), transparent 34%),
    linear-gradient(180deg, #f3e8ff 0%, #eef2ff 44%, #f8fafc 100%);
}

.search-shell {
  width: min(1100px, 100%);
  margin: 0 auto;
}

.search-header {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 8px 0 14px;
  backdrop-filter: blur(18px);
}

.back-btn,
.clear-btn,
.submit-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  cursor: pointer;
}

.back-btn {
  flex: 0 0 auto;
  width: 44px;
  height: 44px;
  color: #4f46e5;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 999px;
  box-shadow: 0 12px 26px rgba(99, 102, 241, 0.12);
  backdrop-filter: blur(14px);
}

.search-field {
  display: flex;
  flex: 1;
  align-items: center;
  min-width: 0;
  height: 44px;
  gap: 8px;
  padding: 0 8px 0 14px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(255, 255, 255, 0.88);
  border-radius: 999px;
  box-shadow: 0 12px 30px rgba(79, 70, 229, 0.1);
  backdrop-filter: blur(16px);
}

.search-icon,
.submit-btn {
  flex: 0 0 auto;
  color: #6366f1;
  font-size: 18px;
  line-height: 1;
}

.search-control {
  flex: 1;
  min-width: 0;
  height: 100%;
  padding: 0;
  color: #1e293b;
  font-size: 14px;
  line-height: 44px;
  background: transparent;
  border: 0;
  outline: none;
}

.search-control::placeholder {
  color: #94a3b8;
}

.search-control::-webkit-search-cancel-button {
  display: none;
}

.clear-btn,
.submit-btn {
  width: 30px;
  height: 30px;
  background: rgba(238, 242, 255, 0.7);
  border-radius: 999px;
}

.clear-btn {
  color: #94a3b8;
}

.assist-panel {
  display: grid;
  gap: 12px;
  margin-top: 8px;
}

.assist-block,
.service-warning,
.mixed-card,
.loading-card,
.empty-card {
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid rgba(255, 255, 255, 0.82);
  border-radius: 22px;
  box-shadow: 0 16px 36px rgba(99, 102, 241, 0.08);
  backdrop-filter: blur(16px);
}

.assist-block {
  padding: 16px;
}

.assist-block h3 {
  margin: 0;
  color: #1e293b;
  font-size: 16px;
  font-weight: 800;
}

.tag-list,
.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

.hot-tag,
.history-item,
.tab-pill {
  border: 0;
  cursor: pointer;
}

.hot-tag {
  padding: 8px 13px;
  color: #4f46e5;
  font-size: 13px;
  font-weight: 700;
  background: rgba(238, 242, 255, 0.86);
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 999px;
}

.history-item {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  max-width: 100%;
  padding: 8px 12px;
  color: #64748b;
  background: rgba(255, 255, 255, 0.68);
  border-radius: 999px;
}

.history-item span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-tabs {
  display: flex;
  gap: 8px;
  padding: 8px;
  margin-top: 16px;
  overflow-x: auto;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.76);
  border-radius: 999px;
  box-shadow: 0 12px 30px rgba(99, 102, 241, 0.08);
  backdrop-filter: blur(16px);
}

.tab-pill {
  flex: 1;
  min-width: 72px;
  padding: 9px 16px;
  color: #64748b;
  font-size: 14px;
  font-weight: 800;
  background: rgba(255, 255, 255, 0.36);
  border-radius: 999px;
}

.tab-pill.active {
  color: #fff;
  background: linear-gradient(135deg, #8b5cf6, #60a5fa);
  box-shadow: 0 10px 20px rgba(99, 102, 241, 0.2);
}

.search-results {
  margin-top: 16px;
}

.service-warning {
  padding: 12px 14px;
  margin-bottom: 12px;
  color: #a16207;
  font-size: 13px;
  font-weight: 700;
  background: rgba(254, 249, 195, 0.68);
}

.mixed-list,
.result-stack {
  display: grid;
  gap: 12px;
}

.mixed-card {
  position: relative;
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 16px;
  cursor: pointer;
  transition: transform 0.18s ease, background 0.18s ease;
}

.mixed-card:active {
  background: rgba(238, 242, 255, 0.78);
  transform: scale(0.99);
}

.type-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 9px;
  color: #fff;
  font-size: 11px;
  font-weight: 900;
  border-radius: 999px;
}

.type-badge.course {
  background: linear-gradient(135deg, #60a5fa, #6366f1);
}

.type-badge.ai {
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
}

.type-badge.post {
  background: linear-gradient(135deg, #34d399, #22c55e);
}

.type-badge.article {
  background: linear-gradient(135deg, #f59e0b, #fb7185);
}

.type-badge.word {
  background: linear-gradient(135deg, #06b6d4, #60a5fa);
}

.mixed-cover {
  flex: 0 0 auto;
  width: 72px;
  height: 72px;
}

.mixed-content {
  min-width: 0;
  padding-right: 58px;
}

.mixed-content h3 {
  margin: 0 0 8px;
  overflow: hidden;
  color: #1e293b;
  font-size: 16px;
  font-weight: 900;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mixed-content p {
  display: -webkit-box;
  margin: 0 0 10px;
  overflow: hidden;
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
}

.mixed-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.mixed-meta span {
  color: #94a3b8;
  font-size: 12px;
  font-weight: 700;
}

.loading-card,
.empty-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 180px;
  padding: 28px 18px;
  text-align: center;
  border-radius: 24px;
}

.loading-card span {
  margin-top: 12px;
  color: #64748b;
  font-weight: 700;
}

.empty-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  color: #8b5cf6;
  font-size: 30px;
  font-weight: 900;
  background: linear-gradient(135deg, rgba(238, 242, 255, 0.92), rgba(219, 234, 254, 0.72));
  border-radius: 20px;
}

.empty-card h3 {
  margin: 14px 0 6px;
  color: #1e293b;
  font-size: 17px;
}

.empty-card p {
  margin: 0;
  color: #94a3b8;
  font-size: 13px;
}

:deep(.highlight) {
  padding: 0 2px;
  color: #7c3aed;
  background: rgba(253, 224, 71, 0.45);
  border-radius: 5px;
}

@media (max-width: 520px) {
  .search-page {
    padding: 12px 12px 104px;
  }

  .search-header {
    gap: 8px;
  }

  .back-btn {
    width: 42px;
    height: 42px;
  }

  .tab-pill {
    flex: 0 0 auto;
  }

  .mixed-cover {
    width: 64px;
    height: 64px;
  }
}
</style>
