<template>
  <div class="profile has-tabbar">
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <!-- 用户基本信息卡片 -->
      <component
        :is="UserInfoCardRaw"
        :user-info="userInfo"
        @settings="router.push('/profile/settings')"
      />

      <!-- 成长体系快速入口 -->
      <div class="quick-access">
        <div class="access-item" @click="router.push('/profile/level')">
          <div class="access-icon bg-blue">
            <van-icon name="star-o" size="24" color="#fff" />
          </div>
          <div class="access-info">
            <div class="access-title">我的等级</div>
            <div class="access-subtitle">Lv.{{ userInfo.level }} {{ userInfo.levelName || '' }}</div>
          </div>
          <van-icon name="arrow" size="16" color="#969799" />
        </div>
        <div class="access-divider"></div>
        <div class="access-item" @click="router.push('/profile/achievements')">
          <div class="access-icon bg-orange">
            <van-icon name="award-o" size="24" color="#fff" />
          </div>
          <div class="access-info">
            <div class="access-title">我的成就</div>
            <div class="access-subtitle">已获取 {{ userStats.badges }} 个徽章</div>
          </div>
          <van-icon name="arrow" size="16" color="#969799" />
        </div>
      </div>

      <!-- 学习数据展示 -->
      <component :is="StudyStatsGridRaw" :stats="userStats" />

      <!-- 今日学习目标 -->
      <component
        :is="TodayGoalsRaw"
        :progress="todayProgress"
        :completed-goals="todayCompletedGoals"
        :total-goals="todayTotalGoals"
        :goals="todayGoals"
        @add-goal="addGoal"
        @toggle-goal="toggleGoalStatus"
      />

      <!-- 我的成就墙 -->
      <component
        :is="AchievementWallRaw"
        :badges="recentBadges"
        @view-all="router.push('/profile/achievements')"
      />

      <!-- 最近学习 -->
      <component :is="RecentLearningRaw" :learning-items="recentLearning" />

      <!-- 学习历史记录 -->
      <component
        :is="LearningHistoryRaw"
        :history-items="learningHistory"
        @view-all="router.push('/courses/history')"
      />

      <!-- 内容导航栏组件 -->
      <div class="profile-section">
        <div class="section-header" :style="sectionHeaderStyle">
          <h3>我的内容</h3>
          <van-icon name="arrow" />
        </div>
        <component :is="ContentTabsRaw" />
      </div>
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { ref, markRaw, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showSuccessToast } from 'vant';
import {
  UserInfoCard,
  StudyStatsGrid,
  TodayGoals,
  AchievementWall,
  RecentLearning,
  LearningHistory,
  ContentTabs,
} from '../../components/Profile';
import { queryUserExpLevel } from '@/api/userExp';
import {
  addDailyGoalItem,
  cancelCompleteDailyGoalItem,
  completeDailyGoalItem,
  queryTodayDailyGoal,
  type DailyGoalToday,
} from '@/api/dailyGoal';
import {
  queryLearningHistory,
  queryRecentLearning,
  type LearningHistoryRecord,
  type RecentLearningRecord,
} from '@/api/personalLearning';

// 添加样式定义
const sectionHeaderStyle = `display: flex; justify-content: space-between; align-items: center; padding: 16px; border-bottom: 1px solid #f5f5f5;`;
import { useUserStore } from '../../stores/userStore.ts';
import { UserControllerService } from '../../services';

// 组件引用
const UserInfoCardRaw = markRaw(UserInfoCard);
const StudyStatsGridRaw = markRaw(StudyStatsGrid);
const TodayGoalsRaw = markRaw(TodayGoals);
const AchievementWallRaw = markRaw(AchievementWall);
const RecentLearningRaw = markRaw(RecentLearning);
const LearningHistoryRaw = markRaw(LearningHistory);
const ContentTabsRaw = markRaw(ContentTabs);

const router = useRouter();
const userStore = useUserStore();
const refreshing = ref(false);

// 数据定义
const userInfo = ref({
  username: '',
  nickname: '',
  phone: '',
  avatar: userStore.DEFAULT_USER_AVATAR,
  level: 1,
  levelName: '',
  nextLevelExp: 100,
  userId: '' as string | number,
});

const userStats = ref({
  daysLearned: 0,
  streakDays: 0,
  stars: 0,
  badges: 0,
});

const todayProgress = ref(0);
const todayCompletedGoals = ref(0);
const todayTotalGoals = ref(0);
const todayGoals = ref<any[]>([]);

const recentBadges = ref([
  {
    id: 1,
    name: '单词达人',
    icon: 'award',
    color: '#1989fa',
    bgClass: 'bg-blue',
  },
  {
    id: 2,
    name: '坚持不懈',
    icon: 'fire',
    color: '#ff976a',
    bgClass: 'bg-orange',
  },
  {
    id: 3,
    name: '听力小子',
    icon: 'music',
    color: '#07c160',
    bgClass: 'bg-green',
  },
  {
    id: 4,
    name: '初级达成',
    icon: 'star',
    color: '#ffcd32',
    bgClass: 'bg-yellow',
  },
]);

const recentLearning = ref<RecentLearningRecord[]>([]);
const learningHistory = ref<LearningHistoryRecord[]>([]);

// 方法定义
const fetchUserData = async () => {
  refreshing.value = true;

  try {
    await userStore.fetchCurrentUser();

    if (userStore.userInfo) {
      if (userStore.userInfo.id) {
        // 尝试从API获取最新的用户数据
        const response = await UserControllerService.getUserByIdUsingGet(
          userStore.userInfo.id,
        );

        if (response.code === 0 && response.data) {
          const userData = response.data;
          let levelInfo = null;

          try {
            levelInfo = await queryUserExpLevel(Number(userData.id));
          } catch (error) {
            console.error('获取用户等级信息失败:', error);
          }

          userInfo.value = {
            username: userData.userName || '',
            nickname: userData.userName || '',
            phone: userData.userPhone || '',
            avatar: userData.userAvatar || userStore.DEFAULT_USER_AVATAR,
            level: levelInfo?.level || 1,
            levelName: levelInfo?.levelName || '',
            nextLevelExp: levelInfo?.nextLevelExp || 100,
            userId: userData.id || '',
          };

          userStats.value = {
            daysLearned: 15,
            streakDays: levelInfo?.continuousLoginDays || 7,
            stars: 128,
            badges: 8,
          };

          await fetchTodayGoal();
          await fetchLearningModules();
        } else {
          showToast(response.message || '获取用户详细信息失败');
        }
      }
    } else {
      showToast('请先登录');
      setTimeout(() => {
        router.push('/login');
      }, 1500);
    }
  } catch (error) {
    console.error('获取用户数据失败:', error);
    showToast('获取用户数据失败，请重试');
  } finally {
    refreshing.value = false;
  }
};

const onRefresh = () => {
  fetchUserData();
};

const applyTodayGoal = (goal: DailyGoalToday) => {
  todayProgress.value = goal.progressPercent || 0;
  todayCompletedGoals.value = goal.completedGoals || 0;
  todayTotalGoals.value = goal.totalGoals || 0;
  todayGoals.value = (goal.items || []).map((item) => ({
    id: item.id,
    title: item.title,
    completed: item.status === 1,
    source: item.source,
    goalType: item.goalType,
    targetValue: item.targetValue || 1,
    currentValue: item.currentValue || 0,
    unit: item.unit,
  }));
};

const fetchTodayGoal = async () => {
  try {
    applyTodayGoal(await queryTodayDailyGoal());
  } catch (error) {
    console.error('获取今日学习目标失败:', error);
    showToast('获取今日学习目标失败');
  }
};

const fetchLearningModules = async () => {
  try {
    const [recentPage, historyPage] = await Promise.all([
      queryRecentLearning(3),
      queryLearningHistory(5),
    ]);
    recentLearning.value = recentPage.records || [];
    learningHistory.value = historyPage.records || [];
  } catch (error) {
    console.error('获取学习记录失败:', error);
    showToast('获取学习记录失败');
  }
};

const addGoal = async (payload: { title: string; targetValue: number; unit?: string }) => {
  try {
    applyTodayGoal(await addDailyGoalItem({
      title: payload.title,
      goalType: 'CUSTOM',
      targetValue: payload.targetValue,
      unit: payload.unit,
    }));
    showSuccessToast('添加成功');
  } catch (error) {
    console.error('添加学习目标失败:', error);
    showToast('添加学习目标失败');
  }
};

const toggleGoalStatus = async (goalId: number) => {
  const goal = todayGoals.value.find((item) => item.id === goalId);
  try {
    applyTodayGoal(goal?.completed
      ? await cancelCompleteDailyGoalItem(goalId)
      : await completeDailyGoalItem(goalId));
    showSuccessToast(goal?.completed ? '已取消完成' : '已完成');
  } catch (error) {
    console.error('更新学习目标状态失败:', error);
    showToast('更新学习目标状态失败');
  }
};

// 生命周期
onMounted(() => {
  fetchUserData();
});
</script>

<style scoped>
.profile {
  padding-bottom: 66px;
  min-height: 100vh;
  overflow-x: hidden;
  background:
    radial-gradient(circle at 18% 8%, rgba(139, 92, 246, 0.16), transparent 30%),
    radial-gradient(circle at 86% 12%, rgba(96, 165, 250, 0.14), transparent 28%),
    linear-gradient(180deg, #f3e8ff 0%, #eef2ff 48%, #fdf2f8 100%);
}

:deep(.van-pull-refresh) {
  min-height: calc(100vh - 50px);
  padding: 12px 16px 0;
}

:deep(.van-pull-refresh__track) {
  padding-bottom: 16px;
}

/* 快速入口样式 */
.quick-access {
  margin: 12px 16px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(255, 255, 255, 0.62);
  border-radius: 22px;
  overflow: hidden;
  box-shadow: 0 14px 32px rgba(99, 102, 241, 0.08);
  backdrop-filter: blur(18px);
}

.access-item {
  display: flex;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.access-item:active {
  background: #f7f8fa;
}

.access-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.access-icon.bg-blue {
  background: linear-gradient(135deg, #93c5fd 0%, #818cf8 100%);
}

.access-icon.bg-orange {
  background: linear-gradient(135deg, #fbcfe8 0%, #c4b5fd 100%);
}

.access-info {
  flex: 1;
}

.access-title {
  font-size: 15px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 4px;
}

.access-subtitle {
  font-size: 13px;
  color: #969799;
}

.access-divider {
  height: 1px;
  background: rgba(226, 232, 240, 0.5);
  margin: 0 16px;
}

.profile-section {
  margin: 12px 16px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.66);
  border: 1px solid rgba(255, 255, 255, 0.62);
  border-radius: 22px;
  box-shadow: 0 14px 32px rgba(99, 102, 241, 0.08);
  backdrop-filter: blur(18px);
}

:deep(.user-info-card),
:deep(.stats-grid),
:deep(.today-goals),
:deep(.achievement-wall),
:deep(.recent-learning),
:deep(.learning-history),
:deep(.content-tabs),
:deep(.van-cell-group) {
  background: rgba(255, 255, 255, 0.66) !important;
  border: 1px solid rgba(255, 255, 255, 0.62);
  border-radius: 22px;
  box-shadow: 0 14px 32px rgba(99, 102, 241, 0.08);
  backdrop-filter: blur(18px);
}

:deep(.van-cell) {
  background: transparent !important;
}

@media (hover: hover) {
  .quick-access,
  .profile-section,
  :deep(.user-info-card),
  :deep(.stats-grid),
  :deep(.today-goals),
  :deep(.achievement-wall),
  :deep(.recent-learning),
  :deep(.learning-history),
  :deep(.content-tabs) {
    transition: transform 0.22s ease, box-shadow 0.22s ease;
  }

  .quick-access:hover,
  .profile-section:hover,
  :deep(.today-goals:hover),
  :deep(.achievement-wall:hover),
  :deep(.recent-learning:hover),
  :deep(.learning-history:hover),
  :deep(.content-tabs:hover) {
    box-shadow: 0 18px 38px rgba(99, 102, 241, 0.12);
    transform: translateY(-2px);
  }
}

@media (min-width: 769px) {
  .profile {
    border-right: 1px solid rgba(255, 255, 255, 0.38);
    border-left: 1px solid rgba(255, 255, 255, 0.38);
  }

  :deep(.van-pull-refresh) {
    max-width: 1100px;
    margin: 0 auto;
    padding-right: 24px;
    padding-left: 24px;
  }

  .quick-access,
  .profile-section {
    margin-right: 0;
    margin-left: 0;
  }
}
</style>
