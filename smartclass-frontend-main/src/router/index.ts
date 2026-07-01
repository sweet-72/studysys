import {
  createRouter,
  createWebHistory,
  RouteRecordRaw,
  NavigationGuardNext,
  RouteLocationNormalized,
} from 'vue-router';
import Home from '../views/home/Home.vue';
import Courses from '../views/course/Courses.vue';
import Profile from '../views/my-profile/Profile.vue';
import Login from '../views/user/Login.vue';
import Register from '../views/user/Register.vue';
import Circle from '../views/circle/Circle.vue';
import { AIChatDetail, ChatContainer, UserChatDetail } from '../views/chat';
import AvatarCropper from '../views/my-profile/settings/AvatarCropper.vue';
import NoticeList from '../views/home/NoticeList.vue';
import PostDetail from '../views/circle/PostDetail.vue';
import ClassList from '../views/class/index.vue';
import UserProfile from '../views/user/UserProfile.vue';

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth: boolean;
  }
}

const routes: Array<RouteRecordRaw> = [
  // 闂備浇宕垫慨鎶芥⒔瀹ュ纾规繛鎴欏灪閸庡﹤顭块懜闈涘闁稿骸娴风槐鎺斺偓锝庝簽娴犮垻鎲搁幍顔尖枅闁诡喛顫夌粭鐔碱敍濮樺彉鍖栭梻?
  {
    path: '/login',
    name: 'login',
    component: Login,
    meta: {
      requiresAuth: false,
    },
  },
  {
    path: '/register',
    name: 'register',
    component: Register,
    meta: {
      requiresAuth: false,
    },
  },
  
  // 婵犵數鍋為崹鍫曞箰閹间讲鈧箓鎮滈挊澹┿儱顭块懜闈涘闁稿骸娴风槐鎺斺偓锝庝簽娴犮垻鎲搁幍顔尖枅闁诡喛顫夌粭鐔碱敍濮樺彉鍖栭梻?
  {
    path: '/',
    name: 'home',
    component: Home,
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/learning-space',
    name: 'LearningSpace',
    component: () => import('../views/learning-space/LearningSpace.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/search',
    name: 'search',
    component: () => import('../views/common').then(m => m.SearchPage),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/notices',
    name: 'notices',
    component: NoticeList,
    meta: {
      requiresAuth: true,
    },
  },
  
  // 闂傚倷鑳堕崢褍鐣烽鍕櫇闁靛牆顦婵犮垼鍩栭崝鏍磿鎼达絿纾奸悗锝庝簽娴犮垻鎲搁幍顔尖枅闁诡喛顫夌粭鐔碱敍濮樺彉鍖栭梻?
  {
    path: '/chat',
    name: 'chat',
    component: ChatContainer,
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/chat/detail',
    name: 'chat-detail',
    component: AIChatDetail,
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/chat/detail/:assistantId',
    name: 'chat-detail-with-assistant',
    component: AIChatDetail,
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/userchat/:userId',
    name: 'user-chat-detail',
    component: UserChatDetail,
    meta: {
      requiresAuth: true,
    },
    props: true,
  },
  {
    path: '/chat-history',
    redirect: '/chat',
  },
  
  // 婵犵數濞€濞佳囧磹閽樺）娑㈠礋椤愵偄娈ㄩ梺褰掓？閻掞箓宕曟惔锝囩＜閻庯綆浜炴禒銏㈡喐閹殿喖鈻堥柟顔款潐缁楃喖顢涘鍙樺寲闂?
  {
    path: '/friends/requests',
    name: 'friend-requests',
    component: () => import('../views/chat/friends/FriendRequests.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/friends/add',
    name: 'add-friend',
    component: () => import('../views/chat/friends/AddFriend.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/qr/scan',
    name: 'qr-scan',
    component: () => import('../views/chat/QRCodeScanPage.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/qr/display',
    name: 'qr-display',
    component: () => import('../views/chat/QRCodeDisplayPage.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  
  // 闂備浇宕垫慨鏉懨洪妶鍡樻珷濞寸姴顑呴悡婵嗏攽閸屾碍鍟為柛搴℃捣缁辨帞鈧綆浜炴禒銏㈡喐閹殿喖鈻堥柟顔款潐缁楃喖顢涘鍙樺寲闂?
  {
    path: '/courses',
    name: 'courses',
    component: Courses,
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/courses/schedule',
    name: 'course-schedule',
    component: () => import('../views/course/CourseSchedule.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/courses/popular',
    name: 'popular-courses',
    component: () => import('../views/course/PopularCourses.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/courses/task-plans',
    name: 'task-plans',
    component: () => import('../views/course/TaskPlans.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/courses/study/:id',
    name: 'course-study',
    component: () => import('../views/course/CourseStudy.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/courses/history',
    name: 'course-history',
    component: () => import('../views/course/HistoryPage.vue'),
    meta: {
      requiresAuth: true,
    },
  },

  // 婵犵數鍋為崹鍫曞箹閳哄倻顩叉繛鍡楄瑜庡鍕偓锝庡厴閺嬫牕顪冮妶鍡樺暗闁稿鍠愭穱濠囧炊椤掍胶鍘介梺鍐叉惈閸犳岸鎮炴ィ鍐╃厱婵炲懓顕ч幊蹇撶暦婢跺浜滈柡宥冨妿閻鏌?
  {
    path: '/profile',
    name: 'profile',
    component: Profile,
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/profile/level',
    name: 'profile-level',
    component: () => import('../views/my-profile/level/LevelPage.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/profile/achievements',
    name: 'achievements',
    component: () => import('../views/my-profile/achievements/AchievementsPage.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/profile/settings',
    name: 'settings',
    component: () => import('../views/my-profile/settings/Settings.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/profile/settings/info',
    name: 'settings-profile',
    component: () => import('../views/my-profile/settings/SettingsProfile.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/profile/settings/about',
    name: 'settings-about',
    component: () => import('../views/my-profile/settings/SettingsAbout.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/profile/settings/terms',
    name: 'settings-terms',
    component: () => import('../views/my-profile/settings/SettingsTerms.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/profile/settings/privacy',
    name: 'settings-privacy',
    component: () => import('../views/my-profile/settings/SettingsPrivacy.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/profile/settings/feedback',
    name: 'settings-feedback',
    component: () => import('../views/my-profile/settings/SettingsFeedback.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/profile/settings/avatar-cropper',
    name: 'avatar-cropper',
    component: AvatarCropper,
    meta: {
      requiresAuth: true,
      title: 'Avatar Cropper',
    },
  },
  
  // 闂備浇宕垫慨鏉懨洪妶鍥╃煋闁汇垹鎲￠崕鎴犳喐閻楀牆绗氶柛搴℃捣缁辨帞鈧綆浜炴禒銏㈡喐閹殿喖鈻堥柟顔款潐缁楃喖顢涘鍙樺寲闂?
  {
    path: '/vocabulary',
    name: 'vocabulary',
    component: () => import('../views/home/vocabulary/VocabularyList.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/vocabulary/collected',
    name: 'collected-words',
    component: () => import('../views/home/vocabulary/CollectedWords.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  
  // 闂傚倷绀侀幖顐﹀磹缁嬫５娲Ω閳哄倸浠ч梺缁樺灱婵倝宕曟惔锝囩＜閻庯綆浜炴禒銏㈡喐閹殿喖鈻堥柟顔款潐缁楃喖顢涘鍙樺寲闂?
  {
    path: '/articles',
    name: 'articles',
    component: () => import('../views/home/articles/ArticlesList.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/daily/article/:id',
    name: 'article-detail',
    component: () => import('../views/home/articles/ArticleDetailPage.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  
  // 闂傚倷绶氬鑽ょ不閹达附鍋嬮柛鈩冪⊕閸嬨倖绻涢崱妯诲鞍闁稿骸娴风槐鎺斺偓锝庝簽娴犮垻鎲搁幍顔尖枅闁诡喛顫夌粭鐔碱敍濮樺彉鍖栭梻?
  {
    path: '/circle',
    name: 'circle',
    component: Circle,
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/circle/post/:id',
    name: 'post-detail',
    component: PostDetail,
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/circle/post/create',
    name: 'post-create',
    component: () => import('../views/circle/PostCreate.vue'),
    meta: {
      requiresAuth: true,
    },
  },
  
  // 闂傚倷鐒﹀鍨焽閸ф绀夌€广儱顦弰銉︾箾閹存瑥鐏柛搴℃捣缁辨帞鈧綆浜炴禒銏㈡喐閹殿喖鈻堥柟顔款潐缁楃喖顢涘鍙樺寲闂?
  {
    path: '/users/:id',
    name: 'user-profile',
    component: UserProfile,
    meta: {
      requiresAuth: true,
    },
  },
  
  // 闂傚倷鑳剁划顖滄崲瀹ュ鍌ㄩ柣鎰靛墻濞堜粙鏌涢妷銏℃珖濞戞挸绉归弻銊モ槈濡警浠鹃梺?
  // {
  //   path: '/class',
  //   name: 'ClassList',
  //   component: () => import('../views/class/index.vue')
  // },
  // {
  //   path: '/class/:id',
  //   name: 'ClassDetail',
  //   component: () => import('../views/class/ClassDetail.vue')
  // },
  
  // 404婵犵绱曢崑鎴﹀磹濡ゅ懎鏋侀悹鍥ф▕閻?- 闂傚倷娴囬妴鈧柛瀣崌閺岀喖顢涢崱妤€顏柛姘煎亰濮婃椽宕崟顓烆暤闂佺顑嗛幑鍥蓟濞戞﹩娼╂い鎺戝閳ь剚顨婇幃妯跨疀閿濆懏鐝濋梺鍝勮嫰閿曘倝顢樻總绋垮耿婵°倕锕ュ▓褰掓⒒娴ｅ憡鎯堥柣妤€妫濊棟闁哄灏呴懓鎸庛亜閹惧崬鐏╃紒鐘劦閺岀喖顢涢崱妤€鏆熼柛妯诲浮濮婂搫效閸パ冾瀳闁诲孩鍑归崹鍫曞箚鐏炰勘鍋呴柛鎰ㄦ櫅娴?
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('../views/common/NotFound.vue'),
    meta: {
      requiresAuth: false,
      title: 'Not Found',
    },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      // 闂傚倷绶氬鑽ゆ嫻閻旂厧绀夐煫鍥ㄧ⊕閸庢螖閿濆懎鏆為柛瀣ㄥ姂閺屾稑螖閸愩劋绮堕梺璇查閵堟悂寮婚悢鑲╁祦闁割煈鍠氭禒鎯р攽閿涘嫬浠滈柛濠呭吹閸掓帡鍩￠崘锝呬壕闁挎繂绨肩花鑽ょ磼娓氬洤鏋熺紒缁樼箖缁绘繈宕橀鐑嗘濠电姵顔栭崰鎺楀疾閻樿尙鏆︽慨姗嗗幖椤曡鲸绻涢敐搴″幋闁稿鎸荤换婵嬪炊閵娧冨Ъ婵＄偑鍊ら崢鐣屾暜閻愮數鐭嗗璺侯儑缁犻箖鎮橀悙娈跨劸濠⒀冨级缁绘繈鍩€椤掍胶鐟归柍褜鍓熼幃浼搭敋閳ь剟骞栬ぐ鎺濇晝闁靛牆妫楅銈嗙節閻㈤潧孝闁挎洏鍎甸幊婵嬫倷椤掍礁寮块梺鍐叉惈閹冲酣寮告笟鈧弻娑㈩敃閵堝懏鐏佹繛?
      return { top: 0, behavior: 'auto' };
    }
  },
});

// 闂備浇宕垫慨宕囨媰閿曞倸鍨傞柟鎯版閺嬩線寮堕崼姘澒闁稿鎹囬幃鐑藉箥椤旂⒈鏆柣?
router.beforeEach(
  async (
    to: RouteLocationNormalized,
    from: RouteLocationNormalized,
    next: NavigationGuardNext,
  ) => {
    const { useUserStore } = await import('../stores/userStore');
    const userStore = useUserStore();

    let isLoggedIn = Boolean(localStorage.getItem('userInfo'));

    if (to.meta.requiresAuth || to.path === '/login') {
      isLoggedIn = await userStore.restoreSession();
    }

    if (to.meta.requiresAuth && !isLoggedIn) {
      next({
        path: '/login',
        query: { redirect: to.fullPath },
      });
      return;
    }

    if (to.path === '/login' && isLoggedIn) {
      next('/');
      return;
    }

    next();
  },
);

export default router;
