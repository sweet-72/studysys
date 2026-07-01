<template>
  <div class="circle has-tabbar">
    <div class="scene-blob blob-left"></div>
    <div class="scene-blob blob-right"></div>
    <span class="scene-cloud cloud-left"></span>
    <span class="scene-cloud cloud-right"></span>
    <span class="scene-planet"></span>
    <span class="scene-star star-one">✦</span>
    <span class="scene-star star-two">✧</span>
    <span class="scene-star star-three">✦</span>

    <header class="hero">
      <div class="hero-main">
        <div class="hero-copy">
          <h1>学习圈子</h1>
          <p>和同学一起交流问题、分享灵感、记录学习日常</p>
          <div class="hero-chips">
            <span>同学讨论</span>
            <span>问答互助</span>
            <span>学习分享</span>
          </div>
        </div>

        <div class="hero-actions">
          <button
            class="search-button"
            type="button"
            :aria-label="searchExpanded ? '收起搜索' : '搜索'"
            @click="toggleSearch"
          >
            <van-icon :name="searchExpanded ? 'cross' : 'search'" />
          </button>
        </div>
      </div>

      <form v-if="searchExpanded" class="search-panel" @submit.prevent="submitSearch">
        <van-icon name="search" />
        <input
          ref="searchInputRef"
          v-model.trim="searchText"
          type="search"
          placeholder="搜索帖子标题或内容"
          @keyup.esc="closeSearch"
        />
        <button v-if="searchText || submittedSearchText" type="button" @click="clearSearch">
          <van-icon name="cross" />
        </button>
      </form>
    </header>

    <nav class="nav-tabs" aria-label="圈子分类">
      <div class="tab-glider" :style="{ transform: `translateX(${activeTabIndex * 100}%)` }"></div>
      <button
        v-for="tab in tabs"
        :key="tab.value"
        type="button"
        :class="['nav-tab', { active: activeTab === tab.value }]"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </button>
    </nav>

    <main ref="scrollContent" class="main-content" @scroll="handleScroll">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <section class="feed-panel">
          <div class="panel-head">
            <div>
              <h2>
                <van-icon :name="isSearchMode ? 'search' : 'fire-o'" />
                {{ isSearchMode ? '搜索结果' : '热门讨论' }}
              </h2>
              <p>{{ isSearchMode ? searchResultTitle : '按互动热度筛选值得参与的话题' }}</p>
            </div>
            <button class="sort-button" type="button">
              最新
              <van-icon name="arrow-down" />
            </button>
          </div>

          <div v-if="currentLoading" class="loading-state">
            <van-loading size="22px" />
          </div>

          <div v-else-if="!currentPosts.length" class="empty-state">
            <van-empty :description="isSearchMode ? '没有找到相关帖子' : emptyDescription" />
          </div>

          <div v-else class="post-list">
            <article
              v-for="(post, index) in currentPosts"
              :key="`${isSearchMode ? 'search' : activeTab}-${post.id || index}`"
              class="post-card"
              @click="viewPostDetail(post)"
            >
              <div class="post-avatar" @click.stop="viewUserProfile(post.user?.id)">
                <img :src="post.user?.userAvatar || defaultAvatar" alt="用户头像" />
              </div>

              <div class="post-content-area">
                <div class="post-meta">
                  <div>
                    <strong>{{ post.user?.userName || '匿名同学' }}</strong>
                    <span>{{ formatDate(post.createTime) }}</span>
                  </div>
                  <button class="more-btn" type="button" @click.stop="showActionSheet(post)">···</button>
                </div>

                <div class="post-layout">
                  <div class="post-text">
                    <h3>{{ post.title || '未命名帖子' }}</h3>
                    <p>{{ getSummary(post.content) }}</p>
                    <div class="tag-row">
                      <span v-for="tag in getPostTags(post)" :key="tag">#{{ tag }}</span>
                    </div>
                  </div>

                  <div class="post-thumb">
                    <img v-if="getPostThumb(post)" :src="getPostThumb(post)" alt="帖子配图" />
                    <div v-else class="thumb-fallback"></div>
                  </div>
                </div>

                <div class="post-actions">
                  <button
                    type="button"
                    :class="['action-item', { active: post.hasThumb }]"
                    @click.stop="toggleLike(post)"
                  >
                    <van-icon :name="post.hasThumb ? 'good-job' : 'good-job-o'" />
                    {{ post.thumbNum || 0 }}
                  </button>
                  <button type="button" class="action-item" @click.stop="showCommentPopup(post)">
                    <van-icon name="chat-o" />
                    {{ post.commentNum || 0 }}
                  </button>
                  <button
                    type="button"
                    :class="['action-item', { active: post.hasFavour }]"
                    @click.stop="toggleFavour(post)"
                  >
                    <van-icon :name="post.hasFavour ? 'star' : 'star-o'" />
                    {{ post.favourNum || 0 }}
                  </button>
                </div>
              </div>
            </article>
          </div>
        </section>
      </van-pull-refresh>
    </main>

    <button class="publish-btn" type="button" @click="handlePublish" aria-label="发布帖子">
      <van-icon name="plus" />
    </button>

    <van-popup v-model:show="showComments" position="bottom" round :style="{ height: '60%' }">
      <div class="comment-popup">
        <div class="comment-header">
          <span>评论</span>
          <van-icon name="cross" @click="showComments = false" />
        </div>
        <div class="comment-list">
          <div v-if="currentComments.length === 0" class="empty-comment">
            <van-empty description="暂无评论" />
          </div>
          <div v-else v-for="comment in currentComments" :key="comment.id" class="comment-item">
            <van-image round width="32" height="32" :src="comment.avatar" class="comment-avatar" />
            <div class="comment-content">
              <div class="comment-user">{{ comment.username }}</div>
              <div class="comment-text">{{ comment.content }}</div>
              <div class="comment-time">{{ comment.time }}</div>
            </div>
          </div>
        </div>
        <div class="comment-input">
          <van-field v-model="commentText" placeholder="发表你的看法" :border="false">
            <template #button>
              <van-button size="small" type="primary" @click="addComment">发送</van-button>
            </template>
          </van-field>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { ActionSheet } from 'vant';
import { useUserStore } from '../../stores/userStore';
import { PostControllerService } from '../../services/services/PostControllerService';
import { PostFavourControllerService } from '../../services/services/PostFavourControllerService';
import { PostCommentControllerService } from '../../services/services/PostCommentControllerService';
import { PostThumbControllerService } from '../../services/services/PostThumbControllerService';
import type { PostVO } from '../../services/models/PostVO';
import type { PostFavourAddRequest } from '../../services/models/PostFavourAddRequest';
import type { PostCommentAddRequest } from '../../services/models/PostCommentAddRequest';
import type { PostCommentVO } from '../../services/models/PostCommentVO';
import type { PostThumbAddRequest } from '../../services/models/PostThumbAddRequest';
import { getClientIPWithRetry } from '../../utils/ipUtils';
import dayjs from 'dayjs';

interface ExtendedPostVO extends PostVO {
  images?: string[];
}

interface Comment {
  id: string;
  username: string;
  avatar: string;
  content: string;
  time: string;
}

type TabValue = 'recommend' | 'following' | 'hot' | 'questions';

const router = useRouter();
const userStore = useUserStore();
const activeTab = ref<TabValue>('recommend');
const refreshing = ref(false);
const showComments = ref(false);
const commentText = ref('');
const currentPostId = ref<number | null>(null);
const currentComments = ref<Comment[]>([]);
const posts = ref<ExtendedPostVO[]>([]);
const searchPosts = ref<ExtendedPostVO[]>([]);
const loading = ref(false);
const searching = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const hasMore = ref(true);
const searchExpanded = ref(false);
const searchText = ref('');
const submittedSearchText = ref('');
const scrollContent = ref<HTMLElement | null>(null);
const searchInputRef = ref<HTMLInputElement | null>(null);
const defaultAvatar = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg';

const tabs: Array<{ label: string; value: TabValue }> = [
  { label: '推荐', value: 'recommend' },
  { label: '关注', value: 'following' },
  { label: '热榜', value: 'hot' },
  { label: '问答', value: 'questions' },
];

const activeTabIndex = computed(() => Math.max(tabs.findIndex((tab) => tab.value === activeTab.value), 0));
const isSearchMode = computed(() => Boolean(submittedSearchText.value));
const searchResultTitle = computed(() => `关键词：${submittedSearchText.value}`);
const currentLoading = computed(() => searching.value || (loading.value && posts.value.length === 0));

const postHeat = (post: ExtendedPostVO) => {
  return (post.thumbNum || 0) * 2 + (post.commentNum || 0) * 3 + (post.favourNum || 0);
};

const getPostPlainText = (post: ExtendedPostVO) => {
  return `${post.title || ''} ${post.content || ''} ${(post.tagList || []).join(' ')}`;
};

const isQuestionPost = (post: ExtendedPostVO) => {
  return /问答|问题|求助|怎么|如何|为什么|作业|题|解题|\?/.test(getPostPlainText(post));
};

const tabPosts = computed(() => {
  if (activeTab.value === 'following') return [];
  if (activeTab.value === 'questions') return posts.value.filter((post) => isQuestionPost(post));
  if (activeTab.value === 'hot') return [...posts.value].sort((a, b) => postHeat(b) - postHeat(a));
  return [...posts.value].sort((a, b) => postHeat(b) - postHeat(a));
});

const currentPosts = computed(() => (isSearchMode.value ? searchPosts.value : tabPosts.value));

const emptyDescription = computed(() => {
  if (activeTab.value === 'following') return '关注的同学发布内容后会出现在这里';
  if (activeTab.value === 'questions') return '包含求助、问题、如何等关键词的帖子会出现在这里';
  return '暂无讨论，下拉刷新看看新的学习动态';
});

const formatDate = (dateString?: string) => {
  if (!dateString) return '刚刚';
  const date = dayjs(dateString);
  const now = dayjs();
  if (date.isSame(now, 'day')) return `${Math.max(now.diff(date, 'hour'), 1)} 小时前`;
  if (date.isSame(now.subtract(1, 'day'), 'day')) return '昨天';
  if (date.isAfter(now.subtract(7, 'day'))) return `${now.diff(date, 'day')} 天前`;
  return date.format('MM-DD');
};

const stripMarkdown = (content?: string) => {
  return (content || '')
    .replace(/!\[[^\]]*]\([^)]*\)/g, '')
    .replace(/\[[^\]]*]\([^)]*\)/g, '')
    .replace(/[#>*_`~-]/g, '')
    .replace(/\s+/g, ' ')
    .trim();
};

const getSummary = (content?: string) => stripMarkdown(content) || '还没有内容摘要';

const getPostTags = (post: ExtendedPostVO) => {
  const serverTags = (post.tagList || [])
    .filter(Boolean)
    .map((tag) => tag.replace(/^#/, '').trim())
    .filter(Boolean);
  if (serverTags.length > 0) return serverTags.slice(0, 3);

  const text = getPostPlainText(post);
  const tags: string[] = [];
  if (/数学|函数|公式|几何|代数|概率/.test(text)) tags.push('高中数学');
  if (/英语|单词|口语|阅读|作文/.test(text)) tags.push('英语学习');
  if (/物理|力学|电路|实验/.test(text)) tags.push('高中物理');
  if (/求助|问题|怎么|如何|为什么|题|解题/.test(text)) tags.push('求助');
  if (/笔记|经验|方法|复盘/.test(text)) tags.push('学习方法');
  return (tags.length > 0 ? tags : ['学习分享']).slice(0, 3);
};

const getPostThumb = (post: ExtendedPostVO) => {
  return post.images?.[0] || '';
};

const rankSearchResults = (records: ExtendedPostVO[], keyword: string) => {
  const words = keyword.toLowerCase().split(/\s+/).filter(Boolean);
  const score = (post: ExtendedPostVO) => {
    const title = (post.title || '').toLowerCase();
    const content = (post.content || '').toLowerCase();
    const tags = (post.tagList || []).join(' ').toLowerCase();
    return words.reduce((sum, word) => {
      let next = sum;
      if (title.includes(word)) next += 10;
      if (content.includes(word)) next += 4;
      if (tags.includes(word)) next += 3;
      return next;
    }, 0) + postHeat(post) / 100;
  };
  return [...records].sort((a, b) => score(b) - score(a));
};

const fetchPosts = async (reset = false) => {
  if (reset) {
    currentPage.value = 1;
    posts.value = [];
    hasMore.value = true;
  }
  if (!hasMore.value && !reset) return;

  loading.value = true;
  try {
    const response = await PostControllerService.listPostVoByPageUsingGet(
      undefined,
      currentPage.value,
      undefined,
      undefined,
      undefined,
      undefined,
      pageSize.value,
      undefined,
      'createTime',
      'descend',
      undefined,
      undefined,
      undefined,
    );

    if (response.code === 0 && response.data) {
      const records = (response.data.records || []) as ExtendedPostVO[];
      posts.value = reset ? records : [...posts.value, ...records];
      currentPage.value += 1;
      hasMore.value = records.length === pageSize.value;
    } else {
      showToast(response.message || '获取帖子失败');
    }
  } catch (error) {
    console.error('获取帖子失败:', error);
    showToast('获取帖子失败，请稍后重试');
  } finally {
    loading.value = false;
    refreshing.value = false;
  }
};

const submitSearch = async () => {
  const keyword = searchText.value.trim();
  if (!keyword) {
    clearSearch();
    return;
  }

  searching.value = true;
  submittedSearchText.value = keyword;
  try {
    const response = await PostControllerService.searchPostVoByPageUsingGet(keyword);
    if (response.code === 0 && response.data) {
      const records = (response.data.records || []) as ExtendedPostVO[];
      searchPosts.value = rankSearchResults(records, keyword);
    } else {
      searchPosts.value = [];
      showToast(response.message || '搜索失败');
    }
  } catch (error) {
    console.error('搜索帖子失败:', error);
    searchPosts.value = [];
    showToast('搜索失败，请稍后重试');
  } finally {
    searching.value = false;
  }
};

const toggleSearch = async () => {
  if (searchExpanded.value) {
    closeSearch();
    return;
  }
  searchExpanded.value = true;
  await nextTick();
  searchInputRef.value?.focus();
};

const closeSearch = () => {
  searchExpanded.value = false;
  if (!submittedSearchText.value) searchText.value = '';
};

const clearSearch = () => {
  searchText.value = '';
  submittedSearchText.value = '';
  searchPosts.value = [];
  searchExpanded.value = false;
};

const switchTab = (tab: TabValue) => {
  activeTab.value = tab;
};

const onRefresh = async () => {
  if (isSearchMode.value) {
    await submitSearch();
    refreshing.value = false;
    return;
  }
  await fetchPosts(true);
};

const handleScroll = () => {
  if (isSearchMode.value || loading.value || !hasMore.value || !scrollContent.value) return;
  const { scrollTop, clientHeight, scrollHeight } = scrollContent.value;
  if (scrollTop + clientHeight >= scrollHeight - 80) {
    fetchPosts(false);
  }
};

const handlePublish = () => {
  router.push('/circle/post/create');
};

const toggleLike = async (post: ExtendedPostVO) => {
  if (!post.id) return;
  if (!userStore.userInfo?.id) {
    showToast('请先登录');
    router.push('/login');
    return;
  }

  try {
    const response = post.hasThumb
      ? await PostThumbControllerService.cancelThumbUsingDelete(post.id)
      : await PostThumbControllerService.addThumbUsingPost({ postId: post.id } as PostThumbAddRequest);

    if (response.code === 0) {
      post.hasThumb = !post.hasThumb;
      post.thumbNum = post.hasThumb ? (post.thumbNum || 0) + 1 : Math.max((post.thumbNum || 0) - 1, 0);
      return;
    }
    showToast(response.message || '操作失败');
  } catch (error) {
    console.error('点赞失败:', error);
    showToast('操作失败，请稍后重试');
  }
};

const toggleFavour = async (post: ExtendedPostVO) => {
  if (!post.id) return;
  if (!userStore.userInfo?.id) {
    showToast('请先登录');
    router.push('/login');
    return;
  }

  try {
    const response = post.hasFavour
      ? await PostFavourControllerService.cancelFavourUsingDelete(post.id)
      : await PostFavourControllerService.addFavourUsingPost({ postId: post.id } as PostFavourAddRequest);

    if (response.code === 0) {
      post.hasFavour = !post.hasFavour;
      post.favourNum = post.hasFavour ? (post.favourNum || 0) + 1 : Math.max((post.favourNum || 0) - 1, 0);
      return;
    }
    showToast(response.message || '操作失败');
  } catch (error) {
    console.error('收藏失败:', error);
    showToast('操作失败，请稍后重试');
  }
};

const showCommentPopup = async (post: ExtendedPostVO) => {
  currentPostId.value = post.id || null;
  currentComments.value = [];
  showComments.value = true;
  if (!currentPostId.value) return;

  try {
    const response = await PostCommentControllerService.listPostCommentByPageUsingGet(
      undefined,
      1,
      10,
      currentPostId.value,
      'createTime',
      'desc',
      undefined,
    );

    if (response.code === 0 && response.data) {
      currentComments.value = response.data.records?.map((comment: PostCommentVO) => ({
        id: String(comment.id || ''),
        username: comment.userVO?.userName || '匿名用户',
        avatar: comment.userVO?.userAvatar || defaultAvatar,
        content: comment.content || '',
        time: formatDate(comment.createTime),
      })) || [];
    } else {
      showToast(response.message || '获取评论失败');
    }
  } catch (error) {
    console.error('获取评论失败:', error);
    showToast('获取评论失败，请稍后重试');
  }
};

const addComment = async () => {
  if (!commentText.value.trim() || !currentPostId.value) {
    showToast('评论不能为空');
    return;
  }

  try {
    const clientIp = await getClientIPWithRetry();
    const response = await PostCommentControllerService.addPostCommentUsingPost({
      content: commentText.value.trim(),
      postId: currentPostId.value,
      clientIp: clientIp || '127.0.0.1',
    } as PostCommentAddRequest);

    if (response.code === 0) {
      commentText.value = '';
      await showCommentPopup({ id: currentPostId.value } as ExtendedPostVO);
      const post = posts.value.find((item) => item.id === currentPostId.value);
      if (post) post.commentNum = (post.commentNum || 0) + 1;
      return;
    }
    showToast(response.message || '评论失败');
  } catch (error) {
    console.error('提交评论失败:', error);
    showToast('评论失败，请稍后重试');
  }
};

const showActionSheet = (post: ExtendedPostVO) => {
  ActionSheet.show({
    actions: [
      { name: '分享', color: '#7c5cff' },
      { name: post.hasFavour ? '取消收藏' : '收藏' },
      { name: '不感兴趣' },
      { name: '举报', color: '#ef4444' },
    ],
    cancel: '取消',
  }).then((action: { name: string }) => {
    if (action.name.includes('收藏')) {
      toggleFavour(post);
      return;
    }
    showToast(action.name);
  });
};

const viewPostDetail = (post: ExtendedPostVO) => {
  if (post.id) router.push(`/circle/post/${post.id}`);
};

const viewUserProfile = (userId: number | undefined) => {
  if (userId) router.push(`/users/${userId}`);
};

onMounted(() => {
  fetchPosts(true);
});
</script>

<style scoped>
.circle {
  --ink: #1f2a44;
  --muted: #8b93aa;
  --purple: #8158ff;
  --purple-soft: #ebe6ff;
  --blue-soft: #e7f0ff;
  --pink-soft: #fff0f8;
  position: relative;
  height: calc(100vh - 50px);
  min-height: 0;
  overflow: hidden;
  color: var(--ink);
  background:
    radial-gradient(circle at 8% 0%, rgba(161, 155, 255, 0.34), transparent 22%),
    radial-gradient(circle at 88% 8%, rgba(255, 219, 244, 0.72), transparent 24%),
    radial-gradient(circle at 62% 34%, rgba(255, 255, 255, 0.42), transparent 26%),
    linear-gradient(135deg, #e7ebff 0%, #f7f4ff 52%, #fff4fb 100%);
}

.scene-blob,
.scene-cloud,
.scene-planet,
.scene-star {
  position: fixed;
  pointer-events: none;
}

.scene-blob {
  z-index: 0;
  border-radius: 999px;
  filter: blur(28px);
}

.blob-left {
  top: -70px;
  left: -40px;
  width: 220px;
  height: 220px;
  background: rgba(160, 160, 255, 0.38);
}

.blob-right {
  right: -86px;
  top: 130px;
  width: 220px;
  height: 220px;
  background: rgba(255, 232, 247, 0.58);
}

.scene-cloud {
  z-index: 1;
  width: 112px;
  height: 38px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  filter: blur(1px);
}

.scene-cloud::before,
.scene-cloud::after {
  content: '';
  position: absolute;
  bottom: 8px;
  border-radius: 50%;
  background: inherit;
}

.scene-cloud::before {
  left: 18px;
  width: 42px;
  height: 42px;
}

.scene-cloud::after {
  right: 14px;
  width: 58px;
  height: 58px;
}

.cloud-left {
  left: 22px;
  top: 260px;
}

.cloud-right {
  right: 24px;
  bottom: 64px;
}

.scene-planet {
  z-index: 1;
  top: 116px;
  right: 31%;
  width: 74px;
  height: 74px;
  border-radius: 50%;
  background: radial-gradient(circle at 32% 28%, #fff8d7, #ffc7eb 48%, #c9b5ff);
  box-shadow: 0 0 34px rgba(255, 204, 235, 0.52);
}

.scene-planet::after {
  content: '';
  position: absolute;
  left: -18px;
  top: 30px;
  width: 112px;
  height: 18px;
  border: 4px solid rgba(255, 231, 195, 0.55);
  border-radius: 50%;
  transform: rotate(-22deg);
}

.scene-star {
  z-index: 1;
  color: rgba(255, 255, 255, 0.92);
  text-shadow: 0 8px 22px rgba(129, 88, 255, 0.3);
  animation: starFloat 3.4s ease-in-out infinite;
}

.star-one {
  top: 58px;
  left: 55%;
  font-size: 24px;
}

.star-two {
  top: 206px;
  left: 47%;
  font-size: 22px;
  animation-delay: 0.6s;
}

.star-three {
  right: 7%;
  top: 184px;
  font-size: 34px;
  color: rgba(255, 242, 221, 0.9);
  animation-delay: 1s;
}

@keyframes starFloat {
  0%,
  100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-9px) scale(1.08);
  }
}

.hero {
  position: relative;
  z-index: 2;
  width: min(1100px, calc(100% - 32px));
  margin: 0 auto;
  padding: 34px 0 0;
}

.hero-main {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.hero-copy h1 {
  margin: 0;
  color: #202944;
  font-size: 34px;
  font-weight: 900;
  letter-spacing: 0;
}

.hero-copy p {
  margin: 12px 0 0;
  color: #8a91ad;
  font-size: 16px;
  font-weight: 700;
}

.hero-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.hero-chips span {
  height: 28px;
  padding: 0 13px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  color: #747b96;
  font-size: 12px;
  font-weight: 800;
  background: rgba(255, 255, 255, 0.58);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.74);
  backdrop-filter: blur(14px);
}

.hero-actions {
  display: flex;
  gap: 12px;
}

.search-button {
  width: 50px;
  height: 50px;
  border: 0;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--purple);
  font-size: 20px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 14px 30px rgba(98, 87, 190, 0.12);
  backdrop-filter: blur(18px);
}

.search-panel {
  max-width: 520px;
  height: 46px;
  margin-top: 18px;
  padding: 0 14px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--purple);
  background: rgba(255, 255, 255, 0.62);
  box-shadow: 0 14px 30px rgba(98, 87, 190, 0.12);
  backdrop-filter: blur(18px);
}

.search-panel input {
  flex: 1;
  min-width: 0;
  border: 0;
  outline: 0;
  color: var(--ink);
  font-size: 14px;
  background: transparent;
}

.search-panel button {
  border: 0;
  color: var(--purple);
  background: transparent;
}

.nav-tabs {
  position: relative;
  z-index: 3;
  width: min(420px, calc(100% - 32px));
  margin: 36px auto 10px;
  padding: 6px;
  border-radius: 999px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background: rgba(255, 255, 255, 0.42);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72), 0 18px 36px rgba(99, 89, 180, 0.08);
  backdrop-filter: blur(18px);
}

.tab-glider {
  position: absolute;
  top: 6px;
  bottom: 6px;
  left: 6px;
  width: calc((100% - 12px) / 4);
  border-radius: 999px;
  background: #ffffff;
  box-shadow: 0 12px 24px rgba(98, 87, 190, 0.12);
  transition: transform 0.28s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.nav-tab {
  position: relative;
  z-index: 1;
  height: 42px;
  border: 0;
  color: #7d849d;
  font-size: 14px;
  font-weight: 900;
  background: transparent;
}

.nav-tab.active {
  color: var(--purple);
}

.main-content {
  position: relative;
  z-index: 2;
  height: calc(100vh - 250px);
  overflow-y: auto;
  padding: 0 16px 116px;
  -webkit-overflow-scrolling: touch;
}

.feed-panel {
  width: min(1100px, 100%);
  margin: 0 auto;
  padding: 28px;
  border: 1px solid rgba(255, 255, 255, 0.62);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.38);
  box-shadow: 0 24px 58px rgba(98, 87, 190, 0.12);
  backdrop-filter: blur(22px);
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}

.panel-head h2 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #27304e;
  font-size: 18px;
  font-weight: 900;
}

.panel-head h2 :deep(.van-icon) {
  color: var(--purple);
}

.panel-head p {
  margin: 6px 0 0;
  color: #8a91ad;
  font-size: 13px;
  font-weight: 700;
}

.sort-button {
  border: 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #8a91ad;
  font-size: 13px;
  font-weight: 900;
  background: transparent;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.post-card {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: 14px;
  padding: 22px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.62);
  box-shadow: 0 16px 34px rgba(98, 87, 190, 0.08);
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease, background 0.22s ease;
}

.post-card:hover {
  transform: translateY(-4px);
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 22px 42px rgba(98, 87, 190, 0.13);
}

.post-avatar img {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 8px 20px rgba(122, 100, 200, 0.12);
}

.post-content-area {
  min-width: 0;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.post-meta strong {
  display: block;
  color: #27304e;
  font-size: 14px;
  font-weight: 900;
}

.post-meta span {
  display: block;
  margin-top: 2px;
  color: #9aa1b7;
  font-size: 12px;
  font-weight: 700;
}

.more-btn {
  border: 0;
  color: #b2b8cb;
  font-size: 18px;
  background: transparent;
}

.post-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 260px;
  gap: 20px;
  align-items: center;
  margin-top: 10px;
}

.post-text h3 {
  margin: 0 0 10px;
  color: #202944;
  font-size: 17px;
  font-weight: 900;
  line-height: 1.45;
}

.post-text p {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: #7a829a;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.7;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.tag-row span {
  height: 26px;
  padding: 0 12px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  color: #777f9a;
  font-size: 12px;
  font-weight: 900;
  background: rgba(244, 235, 255, 0.74);
}

.tag-row span:nth-child(2n) {
  background: rgba(219, 247, 241, 0.78);
}

.tag-row span:nth-child(3n) {
  background: rgba(255, 236, 217, 0.78);
}

.post-thumb {
  height: 92px;
  border-radius: 14px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.5);
}

.post-thumb img,
.thumb-fallback {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.thumb-fallback {
  background:
    radial-gradient(circle at 28% 26%, rgba(255, 255, 255, 0.62), transparent 28%),
    linear-gradient(135deg, #d7d3ff, #f8c6df 52%, #cde8ff);
}

.post-actions {
  display: flex;
  justify-content: flex-end;
  gap: 26px;
  margin-top: 14px;
}

.action-item {
  border: 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #8991a8;
  font-size: 13px;
  font-weight: 800;
  background: transparent;
}

.action-item.active {
  color: var(--purple);
}

.publish-btn {
  position: fixed;
  right: max(22px, calc((100vw - 1100px) / 2 - 78px));
  bottom: 82px;
  z-index: 30;
  width: 64px;
  height: 64px;
  border: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 32px;
  background: linear-gradient(135deg, #db68ff 0%, #7f58ff 65%, #5d9cff 100%);
  box-shadow: 0 22px 42px rgba(127, 88, 255, 0.3);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.publish-btn::before {
  content: '';
  position: absolute;
  z-index: -1;
  width: 116px;
  height: 62px;
  bottom: -24px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  filter: blur(1px);
}

.publish-btn:active {
  transform: scale(0.95);
}

.empty-state,
.loading-state {
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.comment-popup {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(18px);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  font-size: 17px;
  font-weight: 900;
}

.comment-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 16px 16px;
}

.empty-comment {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.comment-item {
  display: flex;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid rgba(226, 232, 240, 0.7);
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-user {
  color: #27304e;
  font-size: 14px;
  font-weight: 900;
}

.comment-text {
  margin-top: 4px;
  color: #6f7890;
  font-size: 14px;
  line-height: 1.5;
}

.comment-time {
  margin-top: 4px;
  color: #9aa1b7;
  font-size: 12px;
}

.comment-input {
  padding: 10px 16px 16px;
  border-top: 1px solid rgba(226, 232, 240, 0.7);
}

.comment-input :deep(.van-field) {
  border-radius: 999px;
  background: rgba(248, 250, 252, 0.9);
}

@media (max-width: 900px) {
  .post-layout {
    grid-template-columns: minmax(0, 1fr);
  }

  .post-thumb {
    display: none;
  }

  .main-content {
    height: calc(100vh - 265px);
  }
}

@media (max-width: 520px) {
  .hero {
    width: calc(100% - 28px);
    padding-top: 26px;
  }

  .hero-copy h1 {
    font-size: 30px;
  }

  .hero-copy p {
    font-size: 14px;
  }

  .nav-tabs {
    margin-top: 24px;
  }

  .feed-panel {
    padding: 18px 12px;
    border-radius: 24px;
  }

  .post-card {
    grid-template-columns: 40px minmax(0, 1fr);
    padding: 16px;
  }

  .post-actions {
    justify-content: flex-start;
    gap: 18px;
  }
}
</style>
