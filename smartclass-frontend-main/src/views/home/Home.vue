<template>
  <div class="home has-tabbar" @click="collapseSearch">
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <section class="stage-area" :style="{ backgroundImage: `url(${backImg})` }">
        <div class="learning-stage">
          <div class="home-user-row">
            <div class="user-profile">
              <van-image
                :src="userAvatar"
                round
                width="48"
                height="48"
                fit="cover"
                class="user-avatar"
              />
              <div class="user-greeting">
                <span>晚上好，{{ userNickname }}</span>
                <em v-if="isVipUser">VIP</em>
              </div>
            </div>
            <div class="top-actions" @click.stop>
              <div
                class="search-pill"
                :class="{ expanded: searchExpanded }"
                @click="expandSearch"
              >
                <input
                  ref="searchInputRef"
                  v-model="searchText"
                  :readonly="!searchExpanded"
                  placeholder="click me"
                  @keyup.enter="onSearch(searchText)"
                />
                <van-icon
                  name="search"
                  size="18"
                  class="search-inline-icon"
                  @click.stop="searchExpanded ? onSearch(searchText) : expandSearch()"
                />
              </div>
              <button
                class="top-plus"
                type="button"
                @click="showActionSheet = true"
              >
                <van-icon name="plus" size="20" />
              </button>
            </div>
          </div>

          <div class="character-stage">
            <img
              :src="maomaoe"
              alt=""
              class="hero-maomaoe"
              @click.stop="openLearningSpace"
            />
            <function-grid />
          </div>
        </div>
      </section>

      <section class="content-panel">
        <!-- 鐑棬璇剧▼妯″潡 - 鎳掑姞杞?-->
        <div ref="popularCoursesRef" class="lazy-load-container">
          <component
            v-if="showPopularCourses"
            :is="PopularCoursesRaw"
            :courses="filteredPopularCourses"
            :loading="popularCoursesLoading"
            @select="viewCourseDetail"
            @more="router.push('/courses/popular')"
          />
          <div v-else class="lazy-load-placeholder">
            <van-skeleton title :row="2" />
          </div>
        </div>

        <!-- 鍏憡鍗＄墖 - 鎳掑姞杞?-->
        <div ref="aiAssistantRef" class="lazy-load-container">
          <ai-assistant-list
            v-if="showAiAssistant"
            :assistants="aiAssistants"
            @chat="startChat"
            @more="router.push('/chat?tab=intelligence')"
          />
          <div v-else class="lazy-load-placeholder">
            <van-skeleton title :row="3" />
          </div>
        </div>

        <!-- 姣忔棩鍗曡瘝妯″潡 - 鎳掑姞杞?-->
        <section class="daily-learning-bento">
          <div class="section-heading">
            <div>
              <h2>每日学习</h2>
            </div>
          </div>
        <div ref="dailyWordRef" class="lazy-load-container">
          <daily-word-card
            v-if="showDailyWord"
            :word="dailyWord as any"
            @update:word="dailyWord = $event"
            @more="router.push('/vocabulary/collected')"
          />
          <div v-else class="lazy-load-placeholder">
            <van-skeleton title :row="1" />
          </div>
        </div>

        <!-- 绮剧編鏂囩珷妯″潡 - 鎳掑姞杞?-->
        <div ref="articleListRef" class="lazy-load-container">
          <article-list
            v-if="showArticleList"
            :articles="articles"
            @more="router.push('/articles')"
          />
          <div v-else class="lazy-load-placeholder">
            <van-skeleton title :row="2" />
          </div>
        </div>
        </section>

        <div ref="noticeCardRef" class="lazy-load-container notice-bottom">
          <notice-card
            v-if="showNoticeCard"
            :notices="notices"
          />
          <div v-else class="lazy-load-placeholder">
            <van-skeleton title :row="2" />
          </div>
        </div>
      </section>

      <!-- AI鍔╂墜鍒楄〃 - 鎳掑姞杞?-->
    </van-pull-refresh>

    <!-- 娴姩鍔犲彿鎸夐挳 -->
    <div class="float-button">
      <van-icon name="plus" size="24" @click="showActionSheet = true" />
    </div>

    <!-- 蹇嵎鎿嶄綔闈㈡澘 -->
    <van-action-sheet
      v-model:show="showActionSheet"
      :actions="actions"
      cancel-text="取消"
      close-on-click-action
      @select="onActionSelect"
    />

  </div>
</template>

<script setup lang="ts">
import { ref, markRaw, onMounted, computed, onUnmounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import {
  NoticeCard,
  AiAssistantList,
  PopularCourses,
  DailyWordCard,
  ArticleList,
  FunctionGrid,
} from '../../components/Home';
import {
  type Course,
  type Notice,
  type Article,
} from '../../api/mock.ts';
import { queryHotRecommendCourses, queryRecommendCourses, recordCourseView, type CourseItem } from '../../api/course';
import { useUserStore } from '../../stores/userStore.ts';
import {
  AiAvatarControllerService,
  DailyWordControllerService,
  DailyArticleControllerService,
  UserWordBookControllerService,
} from '../../services';
import { AnnouncementControllerService } from '../../services/services/AnnouncementControllerService.ts';
import { AnnouncementVO } from '../../services/models/AnnouncementVO.ts';
import { resolveAiAvatarImage } from '../../api/aiAvatar';
import backImg from '@/assets/images/back.png';
import maomaoe from '@/assets/images/maomaoe.png';

// 瀹氫箟绫诲瀷
interface Assistant {
  id: number;
  name: string;
  description: string;
  avatar: string;
}

interface Action {
  name: string;
  icon: string;
  color: string;
}

// 瀹氫箟WordMeaning绫诲瀷
interface WordMeaning {
  partOfSpeech: string;
  definition: string;
  example: string;
}

// 瀹氫箟Word绫诲瀷鏉ュ尮閰岲ailyWordCard缁勪欢鏈熸湜鐨勭被鍨?
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
  isStudied?: boolean;
}

// 浣跨敤markRaw鍖呰缁勪欢锛岄槻姝㈣杞崲涓哄搷搴斿紡瀵硅薄
const PopularCoursesRaw = markRaw(PopularCourses);

const router = useRouter();
const searchText = ref('');
const showActionSheet = ref(false);
const searchExpanded = ref(false);
const searchInputRef = ref<HTMLInputElement | null>(null);
const refreshing = ref(false);
const userStore = useUserStore();

const userAvatar = computed(() => {
  const user = userStore.userInfo as Record<string, string | undefined> | null;
  return user?.userAvatar || userStore.DEFAULT_USER_AVATAR || '/default.jpg';
});

const userNickname = computed(() => {
  const user = userStore.userInfo as Record<string, string | undefined> | null;
  return user?.userName || user?.userAccount || '同学';
});

const isVipUser = computed(() => {
  const user = userStore.userInfo as Record<string, string | undefined> | null;
  return user?.userRole === 'admin' || user?.userRole === 'vip';
});

const expandSearch = async () => {
  if (!searchExpanded.value) {
    searchExpanded.value = true;
    await nextTick();
    searchInputRef.value?.focus();
  }
};

const collapseSearch = () => {
  searchExpanded.value = false;
};

const openLearningSpace = () => {
  router.push('/learning-space');
};

// 鎳掑姞杞界姸鎬佸彉閲?
const showNoticeCard = ref(false);
const showAiAssistant = ref(false);
const showPopularCourses = ref(false);
const showDailyWord = ref(false);
const showArticleList = ref(false);

// 鎳掑姞杞藉鍣ㄥ紩鐢?
const noticeCardRef = ref<HTMLElement | null>(null);
const aiAssistantRef = ref<HTMLElement | null>(null);
const popularCoursesRef = ref<HTMLElement | null>(null);
const dailyWordRef = ref<HTMLElement | null>(null);
const articleListRef = ref<HTMLElement | null>(null);

// Intersection Observer瀹炰緥
let observer: IntersectionObserver | null = null;

// 璁剧疆鍏憡鏁版嵁
const notices = ref<Notice[]>([]);

// 灏嗗悗绔叕鍛婃暟鎹浆鎹负缁勪欢浣跨敤鐨勬牸寮?
const convertAnnouncementToNotice = (announcement: AnnouncementVO): Notice => {
  return {
    id: announcement.id || 0,
    title: announcement.title || '',
    content: announcement.content || '',
    date: announcement.createTime
      ? new Date(announcement.createTime).toLocaleDateString()
      : '',
  };
};

// 鑾峰彇鍏憡鏁版嵁
const fetchNotices = async () => {
  try {
    // 璋冪敤list/page/vo鎺ュ彛锛屽彧浼犲叆蹇呰鐨勫弬鏁?
    const response =
      await AnnouncementControllerService.listAnnouncementVoByPageUsingGet(
        undefined, // adminId
        undefined, // content
        undefined, // coverImage
        undefined, // createTime
        1, // current
        undefined, // endTime
        undefined, // id
        undefined, // isValid
        3, // pageSize
        undefined, // priority
        'createTime', // sortField
        'desc', // sortOrder
        undefined, // startTime
        undefined, // status
        undefined // title
      );

    if (
      response.code === 0 &&
      response.data &&
      response.data.records &&
      response.data.records.length > 0
    ) {
      // 鐩存帴浣跨敤杩斿洖鐨勮褰曡浆鎹负 Notice 鏍煎紡
      notices.value = response.data.records.map(convertAnnouncementToNotice);
    } else {
      // 濡傛灉 API 璇锋眰澶辫触鎴栨病鏈夋暟鎹紝浣跨敤榛樿鍏憡
      notices.value = [
        {
          id: 1,
          title: '系统升级维护通知',
          content: '系统将于今晚 23:00-24:00 进行升级维护。',
          date: new Date().toLocaleDateString(),
        },
        {
          id: 2,
          title: '学习打卡活动开启',
          content: '系统已优化部分已知问题',
          date: new Date().toLocaleDateString(),
        },
      ];
    }
  } catch (error) {
    console.error('检查单词收藏状态失败', error);
    // 鍑洪敊鏃舵樉绀洪粯璁ゅ叕鍛?
    notices.value = [
      {
        id: 1,
        title: '系统升级维护通知',
        content: '系统将于今晚 23:00-24:00 进行升级维护。',
        date: new Date().toLocaleDateString(),
      },
      {
        id: 2,
        title: '学习打卡活动开启',
        content: '参与每日打卡，赢取精美学习礼品。',
        date: new Date().toLocaleDateString(),
      },
    ];
  }
};

// 璁剧疆鐑棬璇剧▼鏁版嵁
const popularCourses = ref<Course[]>([]);
const popularCoursesLoading = ref(false);

const filteredPopularCourses = computed<Course[]>(() => {
  return popularCourses.value;
});

const difficultyTextMap: Record<number, string> = {
  1: '初级',
  2: '中级',
  3: '高级',
  4: '专家',
};

const getCourseCover = (course: CourseItem): string => {
  return course.coverImage || course.coverUrl || '/logo.svg';
};

const convertCourseToCard = (course: CourseItem): Course => {
  const difficulty =
    typeof course.difficulty === 'number'
      ? difficultyTextMap[course.difficulty] || '课程'
      : course.difficulty || '课程';

  return {
    id: course.id,
    title: course.title || '',
    brief: course.subtitle || course.description || '',
    cover: getCourseCover(course),
    tag: '热门',
    tagColor: '#ff8c00',
    level: difficulty,
    duration: course.totalDuration || 0,
    studentsCount: course.studyCount || course.studentCount || 0,
    subject: course.teacherName || '',
  };
};

const fetchPopularCourses = async () => {
  popularCoursesLoading.value = true;
  try {
    const courses = await queryRecommendCourses(6);
    const fallbackCourses =
      courses.length < 6
        ? await queryHotRecommendCourses(6 - courses.length)
        : [];
    const courseMap = new Map<number, CourseItem>();

    [...courses, ...fallbackCourses].forEach((course) => {
      if (course.id && !courseMap.has(course.id)) {
        courseMap.set(course.id, course);
      }
    });

    popularCourses.value = Array.from(courseMap.values()).slice(0, 6).map(convertCourseToCard);
  } catch (error) {
    console.error('获取热门推荐课程失败', error);
    popularCourses.value = [];
  } finally {
    popularCoursesLoading.value = false;
  }
};

// 鏋勫缓涓€涓┖鐨勫崟璇嶅璞′綔涓哄垵濮嬪€?
const emptyWord: Word = {
  id: 0,
  text: '',
  phonetic: '',
  translation: '',
  example: '',
  isCollected: false,
  isThumbUp: false,
  thumbCount: 0,
  likeCount: 0,
  meanings: [],
  viewCount: 0,
  collectCount: 0,
  lastViewTime: new Date().toISOString(),
  difficulty: '',
  category: ''
};

const dailyWord = ref<Word[]>([]);

// 浠庡悗绔幏鍙栦粖鏃ュ崟璇嶏紙淇敼涓鸿幏鍙栨渶杩?3 涓崟璇嶏級
const fetchDailyWord = async () => {
  try {
    // 浣跨敤鍒嗛〉鎺ュ彛鑾峰彇鏈€杩?3 澶╃殑鍗曡瘝
    const response = await DailyWordControllerService.listDailyWordVoByPageUsingGet(
      undefined, // adminId
      undefined, // category
      undefined, // createTime
      1, // current - 绗?1 椤?
      undefined, // difficulty
      undefined, // id
      3, // pageSize - 鑾峰彇 3 涓崟璇?
      undefined, // publishDateEnd
      undefined, // publishDateStart
      'publishDate', // sortField - 鎸夊彂甯冩棩鏈熸帓搴?
      'DESC', // sortOrder - 闄嶅簭鎺掑垪
      undefined, // translation
      undefined, // word
    );

    if (response.code === 0 && response.data && response.data.records) {
      // 鑾峰彇鏈€杩戠殑鍗曡瘝锛堟渶澶?3 涓級
      const recentWords = response.data.records.slice(0, 3);
      
      if (recentWords.length > 0) {
        // 杞崲 API 杩斿洖鐨?DailyWordVO 鏍煎紡涓虹粍浠朵娇鐢ㄧ殑 Word 鏍煎紡
        dailyWord.value = recentWords.map(todayWord => ({
          id: todayWord.id || 0,
          text: todayWord.word || '',
          phonetic: todayWord.pronunciation || '',
          translation: todayWord.translation || '',
          example: todayWord.example || '',
          isCollected: false, // 榛樿鏈敹钘忥紝灏嗗湪 checkCollectedStatus 涓鏌?
          isThumbUp: false, // 榛樿鏈偣璧烇紝灏嗗湪鍚庣画 API 涓鏌?
          thumbCount: todayWord.likeCount || 0,
          likeCount: todayWord.likeCount || 0,
          meanings: [
            {
              partOfSpeech: todayWord.category || '',
              definition: todayWord.translation || '',
              example: todayWord.example || '',
            },
          ],
          viewCount: 0,
          collectCount: 0,
          lastViewTime: new Date().toISOString(),
          difficulty: convertDifficultyToText(todayWord.difficulty),
          category: todayWord.category || '',
          audioUrl: todayWord.audioUrl,
          exampleTranslation: todayWord.exampleTranslation || '',
          notes: todayWord.notes,
        }));
        
        // 妫€鏌ョ涓€涓崟璇嶇殑鏀惰棌鐘舵€?
        if (dailyWord.value[0]?.id) {
          checkCollectedStatus();
        }
      } else {
        console.error('后端返回的单词数据为空');
        showToast('暂无今日单词，请稍后再试');
      }
    } else {
      console.error('鑾峰彇鍗曡瘝 API 璋冪敤澶辫触', response);
      showToast('获取单词数据失败，请稍后再试');
    }
  } catch (error) {
    console.error('检查单词收藏状态失败', error);
    showToast('获取单词数据失败，请稍后再试');
  }
};

// 灏嗘暟瀛楅毦搴﹁浆鎹负鏂囨湰鎻忚堪
const convertDifficultyToText = (difficulty?: number): string => {
  if (!difficulty) return '';

  switch (difficulty) {
    case 1:
      return '初级';
    case 2:
      return '中级';
    case 3:
      return '高级';
    default:
      return '';
  }
};

// 妫€鏌ュ崟璇嶆槸鍚﹀凡鏀惰棌锛堜慨鏀逛负妫€鏌ョ涓€涓崟璇嶏級
const checkCollectedStatus = async () => {
  if (!dailyWord.value.length || !dailyWord.value[0].id) {
    return;
  }

  try {
    // 浣跨敤 UserWordBookControllerService 妫€鏌ュ崟璇嶆槸鍚﹀湪鐢熻瘝鏈腑
    const response = await UserWordBookControllerService.isWordInUserBookUsingGet(
      dailyWord.value[0].id
    );

    if (response.code === 0 && response.data !== undefined) {
      // 鏇存柊绗竴涓崟璇嶇殑鏀惰棌鐘舵€?
      dailyWord.value[0].isCollected = response.data;
    }
  } catch (error) {
    console.error('检查单词收藏状态失败', error);
  }
};

// 璁剧疆缇庢枃鏁版嵁
const articles = ref<Article[]>([]);

// 鑾峰彇浠婃棩缇庢枃鏁版嵁
const fetchTodayArticles = async () => {
  try {
    const response = await DailyArticleControllerService.getTodayArticleUsingGet();

    if (response.code === 0 && response.data) {
      // 灏嗗悗绔暟鎹浆鎹负鍓嶇鎵€闇€鏍煎紡
      const article = response.data;
      
      // 澶勭悊鏍囩瀛楃涓诧紝灏嗛€楀彿鍒嗛殧鐨勬爣绛捐浆鎹负鏁扮粍
      const tagsList = article.tags
        ? article.tags.split(',').map((tag) => tag.trim())
        : [];

      const formattedArticle = {
        id: article.id || 0,
        title: article.title || '',
        brief: article.summary || '',
        cover: article.coverImage || '',
        category: article.category || '',
        readTime: article.readTime || 0,
        // 灏嗘暟瀛楅毦搴﹁浆鎹负瀛楃涓?
        difficulty: article.difficulty ? String(article.difficulty) : '',
        content: article.content || '',
        publishDate: article.publishDate || '',
        viewCount: article.viewCount || 0,
        likeCount: article.likeCount || 0,
        tags: tagsList,
        author: article.author || '', 
        source: article.source || '',
      };

      // 鏀惧叆鏁扮粍涓互渚跨粍浠舵覆鏌撳苟浣跨敤绫诲瀷鏂█
      articles.value = [formattedArticle as Article];
    } else {
      console.error('鑾峰彇浠婃棩缇庢枃鏁版嵁澶辫触', response);
      articles.value = [];
      showToast('获取美文数据失败');
    }
  } catch (error) {
    console.error('检查单词收藏状态失败', error);
    articles.value = [];
    showToast('获取美文数据失败');
  }
};

// AI鍔╂墜鏁版嵁
const aiAssistants = ref<Assistant[]>([]);

// 浠庡悗绔幏鍙栨櫤鎱т綋鏁版嵁
const fetchAiAssistants = async () => {
  try {
    // 浣跨敤鏅€氱敤鎴锋帴鍙ｈ幏鍙朅I鍒嗚韩鍒楄〃锛屾浛鎹㈢鐞嗗憳鎺ュ彛
    const response = await AiAvatarControllerService.listAiAvatarByPageUsingGet(
      undefined, // abilities
      undefined, // adminId
      undefined, // avatarUrl
      undefined, // category
      undefined, // createTime
      undefined, // creatorId
      1, // current - 榛樿绗竴椤?
      undefined, // description
      undefined, // id
      1, // isPublic - 鍙幏鍙栧叕寮€鐨勬櫤鎱т綋
      undefined, // modelType
      undefined, // name
      10, // pageSize - 榛樿鑾峰彇10鏉?
      undefined, // personality
      undefined, // rating
      undefined, // sortField
      undefined, // sortOrder
      1, // status - 鍙幏鍙栨甯哥姸鎬佺殑鏅烘収浣?
      undefined, // tags
      undefined  // usageCount
    );

    if (response.code === 0 && response.data && response.data.records) {
      // 灏嗗悗绔暟鎹浆鎹负鍓嶇闇€瑕佺殑鏍煎紡
      const defaultAvatar = '/avatar-default.png';
      aiAssistants.value = response.data.records.map((avatar) => {
        return {
          id: avatar.id || 0,
          name: avatar.name || '',
          description: avatar.description || '',
          avatar: resolveAiAvatarImage(avatar.avatarImgUrl) || defaultAvatar,
        };
      });
    } else {
      // 浣跨敤榛樿鏁版嵁浣滀负澶囩敤
      setDefaultAiAssistants();
    }
  } catch (error) {
    // 浣跨敤榛樿鏁版嵁浣滀负澶囩敤
    setDefaultAiAssistants();
  }
};

// 璁剧疆榛樿鐨勬櫤鎱т綋鏁版嵁锛堝綋API璇锋眰澶辫触鏃朵娇鐢級
const setDefaultAiAssistants = () => {
  // 娓呯┖榛樿鏁版嵁锛屽彧淇濈暀绌烘暟缁?
  aiAssistants.value = [];
};

// 蹇嵎鎿嶄綔鍒楄〃
const actions = ref<Action[]>([
  { name: '添加生词', icon: 'edit', color: '#1989fa' },
  { name: '上传笔记', icon: 'notes-o', color: '#07c160' },
  { name: '发起对话', icon: 'chat-o', color: '#ff976a' },
  { name: '发布帖子', icon: 'share-o', color: '#ee0a24' },
]);

// 鎼滅储澶勭悊
const onSearch = (text: string) => {
  const keyword = text.trim();
  if (!keyword) {
    showToast('请输入搜索内容');
    return;
  }

  router.push({
    path: '/search',
    query: { keyword },
  });
};

// 寮€濮嬪璇?
const startChat = (assistant: Assistant) => {
  router.push(`/chat/detail/${assistant.id}`);
};

// 鏌ョ湅璇剧▼璇︽儏
const viewCourseDetail = (course: any) => {
  recordCourseView(course.id).catch((error) => {
    console.error('记录课程点击失败', error);
  });
  router.push({
    path: '/courses/popular',
    query: { showDetail: 'true', courseId: course.id },
  });
};

// 澶勭悊蹇嵎鎿嶄綔閫夋嫨
const onActionSelect = (action: Action) => {
  switch (action.name) {
    case '添加生词':
      router.push('/vocabulary');
      break;
    case '上传笔记':
      showToast('笔记功能开发中');
      break;
    case '发起对话':
      router.push('/chat?tab=intelligence');
      break;
    case '发布帖子':
      router.push('/circle/post/create');
      break;
  }
};

// 涓嬫媺鍒锋柊
const onRefresh = async () => {
  try {
    // 渚濇鍒锋柊鎵€鏈夋暟鎹紝閬垮厤 Promise.all 鐨勯敊璇?
    await fetchAiAssistants();
    await fetchNotices(); // 鍒锋柊鍏憡鏁版嵁
    await fetchPopularCourses();
    await fetchDailyWord(); // 鍒锋柊浠婃棩鍗曡瘝鏁版嵁
    await fetchTodayArticles(); // 鍒锋柊浠婃棩缇庢枃鏁版嵁

    // 閲嶇疆鎳掑姞杞界姸鎬侊紝璁╃粍浠堕噸鏂版噿鍔犺浇
    showNoticeCard.value = false;
    showAiAssistant.value = false;
    showPopularCourses.value = false;
    showDailyWord.value = false;
    showArticleList.value = false;

    // 閲嶆柊鍒濆鍖栨噿鍔犺浇
    await nextTick();
    initLazyLoading();

    showToast('刷新成功');
  } catch (error) {
    showToast('刷新失败');
  } finally {
    refreshing.value = false;
  }
};

// 鍒濆鍖栨噿鍔犺浇
const initLazyLoading = () => {
  // 鍒涘缓Intersection Observer
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          const target = entry.target as HTMLElement;
          
          // 鏍规嵁鐩爣鍏冪礌鏄剧ず瀵瑰簲鐨勭粍浠?
          if (target === noticeCardRef.value) {
            showNoticeCard.value = true;
          } else if (target === aiAssistantRef.value) {
            showAiAssistant.value = true;
          } else if (target === popularCoursesRef.value) {
            showPopularCourses.value = true;
          } else if (target === dailyWordRef.value) {
            showDailyWord.value = true;
          } else if (target === articleListRef.value) {
            showArticleList.value = true;
          }
          
          // 涓€鏃︽樉绀哄氨涓嶅啀瑙傚療
          observer?.unobserve(target);
        }
      });
    },
    {
      rootMargin: '50px', // 鎻愬墠50px寮€濮嬪姞杞?
      threshold: 0.1, // 10%鍙鏃惰Е鍙?
    }
  );

  // 寤惰繜瑙傚療锛岀‘淇滵OM鍏冪礌宸茬粡娓叉煋
  setTimeout(() => {
    const containers = [
      noticeCardRef.value,
      aiAssistantRef.value,
      popularCoursesRef.value,
      dailyWordRef.value,
      articleListRef.value,
    ];

    containers.forEach((container) => {
      if (container) {
        observer?.observe(container);
      }
    });
  }, 100);
};

// 娓呯悊鎳掑姞杞借瀵熷櫒
const cleanupLazyLoading = () => {
  if (observer) {
    observer.disconnect();
    observer = null;
  }
};

// 椤甸潰鍔犺浇鏃惰幏鍙栨暟鎹?
onMounted(async () => {
  checkCollectedStatus();
  fetchAiAssistants();
  fetchNotices(); // 鑾峰彇鍏憡鏁版嵁
  fetchPopularCourses();
  fetchDailyWord(); // 鑾峰彇浠婃棩鍗曡瘝鏁版嵁
  fetchTodayArticles(); // 鑾峰彇浠婃棩缇庢枃鏁版嵁
  
  // 绛夊緟DOM娓叉煋瀹屾垚鍚庡垵濮嬪寲鎳掑姞杞?
  await nextTick();
  initLazyLoading();
});

// 缁勪欢鍗歌浇鏃舵竻鐞?
onUnmounted(() => {
  cleanupLazyLoading();
});
</script>

<style scoped>
.home {
  min-height: 100vh;
  padding: 0 0 108px;
  overflow-x: hidden;
  background:
    radial-gradient(circle at 50% 8%, rgba(139, 92, 246, 0.12), transparent 30%),
    linear-gradient(180deg, #f3e8ff 0%, #eef2ff 42%, #f8fafc 100%);
}

.stage-area {
  position: relative;
  box-sizing: border-box;
  width: 100%;
  padding: 20px 20px 34px;
  overflow: hidden;
  background:
    radial-gradient(circle at 50% 42%, rgba(139, 92, 246, 0.18), transparent 32%),
    radial-gradient(circle at 18% 18%, rgba(96, 165, 250, 0.14), transparent 28%),
    radial-gradient(circle at 82% 22%, rgba(16, 185, 129, 0.1), transparent 26%),
    linear-gradient(180deg, #f3e8ff 0%, #eef2ff 55%, #f8fafc 100%);
}

.stage-prop {
  position: absolute;
  z-index: 0;
  color: #6366f1;
  pointer-events: none;
  opacity: 0.12;
}

.prop-star {
  font-size: 30px;
}

.prop-star-1 {
  top: 98px;
  left: 26px;
}

.prop-star-2 {
  top: 142px;
  right: 28px;
  color: #10b981;
}

.prop-dot {
  width: 82px;
  height: 82px;
  border: 10px solid currentColor;
  border-radius: 50%;
}

.prop-dot-1 {
  top: 72px;
  right: -34px;
  color: #60a5fa;
}

.prop-dot-2 {
  bottom: 56px;
  left: -36px;
  width: 70px;
  height: 70px;
  color: #8b5cf6;
}

.prop-book,
.prop-pencil {
  font-size: 42px;
}

.prop-book {
  right: 58px;
  bottom: 98px;
  color: #6366f1;
  transform: rotate(-14deg);
}

.prop-pencil {
  bottom: 122px;
  left: 54px;
  color: #60a5fa;
  transform: rotate(24deg);
}

.learning-stage,
.home-user-row {
  position: relative;
  z-index: 1;
}

.learning-stage {
  box-sizing: border-box;
  min-height: 440px;
  overflow: visible;
}

.home-user-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
}

.user-profile {
  display: flex;
  align-items: center;
  min-width: 0;
}

.user-avatar {
  flex: 0 0 auto;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.08);
}

.top-actions {
  position: relative;
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  min-width: 0;
  max-width: calc(100% - 60px);
}

.top-plus {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  padding: 0;
  color: #fff;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: 0;
  border-radius: 999px;
  box-shadow: 0 10px 22px rgba(79, 70, 229, 0.22);
}

.search-pill {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  width: 44px;
  height: 44px;
  gap: 8px;
  padding: 0 13px;
  overflow: hidden;
  color: #6366f1;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 999px;
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.06);
  transition: width 0.28s ease, background 0.28s ease, border-color 0.28s ease;
  backdrop-filter: blur(14px);
}

.search-pill.expanded {
  width: min(176px, calc(100vw - 132px));
  padding: 0 14px;
}

.search-pill input {
  flex: 1;
  width: 0;
  min-width: 0;
  padding: 0;
  color: #1e293b;
  font-size: 14px;
  line-height: 1;
  background: transparent;
  border: 0;
  outline: none;
  opacity: 0;
  transition: opacity 0.18s ease;
}

.search-pill.expanded input {
  opacity: 1;
}

.search-pill input::placeholder {
  color: rgba(100, 116, 139, 0.78);
}

.search-inline-icon {
  flex: 0 0 auto;
}

.top-plus:active,
.search-pill:active {
  transform: scale(0.96);
}

.guide-body {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  margin: 12px 0 0;
  overflow: visible;
}

.guide-body::before {
  position: absolute;
  z-index: 0;
  width: 214px;
  height: 214px;
  content: '';
  background: radial-gradient(circle, rgba(255, 255, 255, 0.48), rgba(139, 92, 246, 0.08) 46%, rgba(255, 255, 255, 0) 72%);
  border-radius: 50%;
}

.learning-character {
  position: relative;
  z-index: 3;
  display: block;
  width: min(224px, 61vw);
  max-height: 236px;
  object-fit: contain;
  filter: drop-shadow(0 14px 24px rgba(79, 70, 229, 0.16));
}

.learning-stage :deep(.function-grid) {
  padding: 0;
  margin: 0;
}

.content-panel {
  position: relative;
  z-index: 2;
  margin-top: -18px;
  padding: 24px 16px 116px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.78), rgba(248, 250, 252, 0.92));
  border-radius: 28px 28px 0 0;
  box-shadow: 0 -12px 34px rgba(79, 70, 229, 0.08);
  backdrop-filter: blur(18px);
}

.float-button {
  position: fixed;
  right: 16px;
  bottom: 82px;
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  color: #fff;
  background: linear-gradient(135deg, #60a5fa, #6366f1 52%, #8b5cf6);
  border-radius: 50%;
  box-shadow: 0 12px 24px rgba(79, 70, 229, 0.25);
}

:deep(.van-pull-refresh) {
  min-height: calc(100vh - 32px);
}

:deep(.van-pull-refresh__track) {
  padding-bottom: 16px;
}

:deep(.van-cell-group) {
  transition: all 0.3s ease;
}

.lazy-load-container {
  min-height: 0;
  margin-bottom: 26px;
  opacity: 1;
  transition: opacity 0.3s ease-in-out;
}

.lazy-load-container:not(.loaded) {
  opacity: 0.8;
}

.lazy-load-placeholder {
  padding: 16px;
  margin-bottom: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.daily-learning-bento {
  margin-bottom: 26px;
}

.section-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin: 0 2px 12px;
}

.section-heading h2 {
  margin: 0;
  color: #1e293b;
  font-size: 18px !important;
  line-height: 1.25;
}

.section-heading span {
  display: block;
  margin-top: 5px;
  color: #94a3b8;
  font-size: 12px;
}

.notice-bottom {
  margin-bottom: 0;
  padding-bottom: 8px;
}

.home {
  min-height: 100vh;
  padding: 0 0 96px;
  overflow-x: hidden;
  background: #f8fafc;
}

.stage-area {
  position: relative;
  box-sizing: border-box;
  width: 100%;
  min-height: 510px;
  padding: 24px 20px 28px;
  overflow: visible;
  background-color: #eef2ff;
  background-repeat: no-repeat;
  background-position: top center;
  background-size: cover;
}

.learning-stage {
  position: relative;
  z-index: 1;
  box-sizing: border-box;
  min-height: 430px;
  overflow: visible;
}

.home-user-row {
  position: relative;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 0;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.user-avatar {
  flex: 0 0 auto;
  overflow: hidden;
  border: 3px solid rgba(255, 255, 255, 0.78);
  box-shadow: 0 10px 24px rgba(99, 102, 241, 0.12);
}

.user-greeting {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  color: #1e293b;
  font-size: 16px;
  font-weight: 800;
  line-height: 1;
}

.user-greeting span {
  display: block;
  max-width: min(156px, 40vw);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-greeting em {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 9px;
  border-radius: 999px;
  color: #ffffff;
  font-size: 11px;
  font-style: normal;
  font-weight: 800;
  background: linear-gradient(135deg, #f6c177, #d99b55);
  box-shadow: 0 8px 18px rgba(217, 155, 85, 0.18);
}

.top-actions {
  position: relative;
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  min-width: 0;
  max-width: calc(100% - 60px);
}

.top-plus,
.search-pill {
  width: 48px;
  height: 48px;
  color: #0f172a;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.74);
  border-radius: 50%;
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.12);
  backdrop-filter: blur(14px);
}

.top-plus {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.search-pill {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  padding: 0 14px;
  overflow: hidden;
  transition: width 0.28s ease, border-radius 0.28s ease, background 0.28s ease;
}

.search-pill.expanded {
  width: min(178px, calc(100vw - 156px));
  border-radius: 999px;
}

.search-pill input {
  flex: 1;
  width: 0;
  min-width: 0;
  padding: 0;
  color: #1e293b;
  font-size: 14px;
  line-height: 1;
  background: transparent;
  border: 0;
  outline: none;
  opacity: 0;
  transition: opacity 0.18s ease;
}

.search-pill.expanded input {
  opacity: 1;
}

.search-pill input::placeholder {
  color: #94a3b8;
}

.search-inline-icon {
  flex: 0 0 auto;
  color: #0f172a;
}

.top-plus:active,
.search-pill:active {
  transform: scale(0.96);
}

.character-stage {
  position: relative;
  z-index: 2;
  display: block;
  height: 420px;
  margin: 12px 0 0;
  overflow: visible;
}

.character-stage::before {
  display: none;
}

.hero-maomaoe {
  position: absolute;
  top: -226px;
  left: 50%;
  z-index: 3;
  display: block;
  width: 420px;
  max-width: 88vw;
  max-height: none;
  margin: 0;
  object-fit: contain;
  cursor: pointer;
  pointer-events: auto;
  filter: drop-shadow(0 18px 32px rgba(99, 102, 241, 0.16));
  transform: translateX(-50%);
}

.learning-character {
  display: none;
}

.learning-stage :deep(.function-grid) {
  padding: 0;
  margin: 0;
}

.content-panel {
  position: relative;
  z-index: 6;
  margin: -128px 12px 0;
  padding: 28px 16px 104px;
  overflow: hidden;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.88));
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 34px 34px 0 0;
  box-shadow:
    0 -18px 42px rgba(99, 102, 241, 0.13),
    0 12px 30px rgba(148, 163, 184, 0.08);
  backdrop-filter: blur(20px);
}

.content-panel::before {
  position: absolute;
  top: -12px;
  left: 50%;
  width: 132px;
  height: 28px;
  content: '';
  background:
    radial-gradient(circle at 22% 50%, rgba(167, 243, 208, 0.42), transparent 42%),
    linear-gradient(90deg, rgba(139, 92, 246, 0), rgba(99, 102, 241, 0.36), rgba(96, 165, 250, 0));
  border-radius: 999px;
  filter: blur(1px);
  opacity: 0.9;
  transform: translateX(-50%);
  animation: panelGlow 3.2s ease-in-out infinite;
}

.content-panel::after {
  position: absolute;
  top: 18px;
  right: 18px;
  left: 18px;
  height: 86px;
  pointer-events: none;
  content: '';
  background: radial-gradient(circle at 36% 0%, rgba(99, 102, 241, 0.09), transparent 58%);
}

.content-panel > * {
  position: relative;
  z-index: 1;
}

@keyframes panelGlow {
  0%,
  100% {
    transform: translateX(-50%) translateY(0);
    opacity: 0.72;
  }

  50% {
    transform: translateX(-50%) translateY(4px);
    opacity: 1;
  }
}

.float-button {
  display: none;
}

.lazy-load-container {
  min-height: 0;
  margin-bottom: 24px;
  opacity: 1;
  transition: opacity 0.3s ease-in-out;
}

.daily-learning-bento {
  margin-bottom: 24px;
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 2px 14px;
}

.section-heading h2 {
  margin: 0;
  color: #1e293b;
  font-size: 20px !important;
  font-weight: 800;
  line-height: 1.25;
}

.section-heading span {
  display: none;
}

:deep(.van-cell-group) {
  background: transparent;
}

:deep(.van-cell) {
  background: transparent;
}

@media (max-width: 375px) {
  .stage-area {
    min-height: 492px;
    padding: 20px 16px 26px;
  }

  .learning-stage {
    min-height: 422px;
  }

  .character-stage {
    height: 410px;
    margin-top: 10px;
  }

  .hero-maomaoe {
    top: -202px;
    width: 390px;
    max-width: 90vw;
  }

  .search-pill.expanded {
    width: min(156px, calc(100vw - 144px));
  }

  .user-greeting {
    font-size: 15px;
  }
}

@media (min-width: 768px) {
  .home {
    background:
      radial-gradient(circle at 50% 12%, rgba(139, 92, 246, 0.12), transparent 32%),
      linear-gradient(180deg, #eef2ff 0%, #f8fafc 58%, #ffffff 100%);
  }

  .stage-area {
    max-width: min(1100px, calc(100vw - 64px));
    min-height: 540px;
    margin: 24px auto 0;
    padding: 28px 36px 34px;
    border-radius: 32px 32px 0 0;
  }

  .learning-stage {
    min-height: 462px;
  }

  .home-user-row {
    max-width: 1040px;
    margin-right: auto;
    margin-left: auto;
  }

  .character-stage {
    height: 430px;
  }

  .hero-maomaoe {
    top: -220px;
    width: 440px;
    max-width: 44vw;
  }

  .content-panel {
    max-width: min(1100px, calc(100vw - 64px));
    margin: -128px auto 0;
    padding: 34px 28px 120px;
    border-radius: 36px;
  }
}
</style>
