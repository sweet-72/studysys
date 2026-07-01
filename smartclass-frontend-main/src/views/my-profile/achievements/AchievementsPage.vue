<template>
  <div class="achievements-page">
    <!-- 返回按钮 -->
    <back-button title="我的成就" />

    <!-- 成就统计信息 -->
    <div class="stats-card">
      <div class="stats-item">
        <div class="stats-value">{{ stats.completedAchievements || 0 }}</div>
        <div class="stats-label">已获得成就</div>
      </div>
      <div class="stats-item">
        <div class="stats-value">{{ stats.totalAchievements || 0 }}</div>
        <div class="stats-label">总成就数</div>
      </div>
      <div class="stats-item">
        <div class="stats-value">{{ stats.completionRate || 0 }}%</div>
        <div class="stats-label">完成度</div>
      </div>
    </div>

    <!-- 分类标签页 -->
    <div class="tab-container">
      <van-tabs v-model:active="activeTab" class="custom-tabs">
        <van-tab title="全部" name="all"></van-tab>
        <van-tab title="已完成" name="completed"></van-tab>
        <van-tab title="进行中" name="inProgress"></van-tab>
      </van-tabs>
    </div>

    <!-- 成就列表 -->
    <div class="achievement-grid">
      <van-empty v-if="loading" description="加载中..." />
      <div v-else-if="filteredAchievements.length === 0" class="empty-state">
        <van-empty description="暂无成就数据" />
      </div>
      <div
        v-for="achievement in filteredAchievements"
        :key="achievement.achievementId"
        class="achievement-item"
        @click="showAchievementDetail(achievement)"
      >
        <div class="badge-icon-wrapper" :class="getBgClass(achievement.category)">
          <van-icon :name="getIconByCategory(achievement.category)" :color="getColorByCategory(achievement.category)" size="24" />
        </div>
        <div class="badge-details">
          <span class="badge-name">{{ achievement.achievementName }}</span>
          <span class="badge-description">{{ achievement.description }}</span>
          
          <!-- 进度条（进行中的显示） -->
          <template v-if="!achievement.isCompleted && achievement.progressMax > 0">
            <div class="progress-bar">
              <div 
                class="progress-fill" 
                :style="{ width: getProgressPercent(achievement) + '%' }"
              ></div>
            </div>
            <span class="progress-text">
              {{ achievement.progress || 0 }} / {{ achievement.progressMax }}
            </span>
          </template>
          
          <!-- 已领取标签 -->
          <template v-else-if="achievement.isCompleted">
            <span class="completed-tag">
              <van-icon name="checked" /> 已领取
            </span>
          </template>
          
          <span class="badge-date" v-if="achievement.completedTime">
            获得于 {{ formatAchievementDate(achievement.completedTime) }}
          </span>
        </div>
        
        <!-- 领取奖励按钮 -->
        <van-button 
          v-if="achievement.isCompleted && !achievement.isClaimed" 
          type="primary" 
          size="small"
          @click.stop="claimReward(achievement)"
        >
          领取奖励
        </van-button>
      </div>
    </div>

    <!-- 成就弹窗详情 -->
    <van-popup
      v-model:show="showDetail"
      round
      position="bottom"
      :style="{ height: '60%' }"
    >
      <div class="badge-detail-popup" v-if="selectedBadge">
        <div class="popup-header">
          <van-icon
            name="cross"
            @click="showDetail = false"
            class="close-icon"
          />
          <h3 class="popup-title">成就详情</h3>
        </div>

      <div class="badge-detail-content">
        <div class="badge-icon-large" :class="selectedAchievement.bgClass || getBgClass(selectedAchievement.category)">
          <van-icon
            :name="getIconByCategory(selectedAchievement.category)"
            :color="getColorByCategory(selectedAchievement.category)"
            size="64"
          />
        </div>

        <h2 class="badge-title">{{ selectedAchievement.achievementName }}</h2>
        <p class="badge-full-description">{{ selectedAchievement.description }}</p>
        <p class="badge-earn-date" v-if="selectedAchievement.completedTime">
          获得于 {{ formatAchievementDate(selectedAchievement.completedTime) }}
        </p>

        <div class="badge-criteria">
          <h4>获得条件</h4>
          <p>{{ selectedAchievement.condition }}</p>
        </div>

        <div class="badge-reward" v-if="selectedAchievement.reward">
          <h4>成就奖励</h4>
          <p>{{ selectedAchievement.reward }}</p>
        </div>

        <van-button 
          v-if="selectedAchievement.isCompleted && !selectedAchievement.isClaimed" 
          type="primary" 
          block
          @click="claimReward(selectedAchievement)"
        >
          领取奖励
        </van-button>
      </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showLoadingToast, showSuccessToast } from 'vant';
import { BackButton } from '../../../components/Common';
import { useUserStore } from '../../../stores/userStore';
import { UserAchievementControllerService } from '../../../services';

interface Achievement {
  achievementId: number;
  achievementName: string;
  description: string;
  condition: string;
  reward?: string;
  category: string;
  progress: number;
  progressMax: number;
  isCompleted: boolean;
  isClaimed: boolean;
  completedTime?: string;
  bgClass?: string;
}

interface AchievementStats {
  totalAchievements: number;
  completedAchievements: number;
  completionRate: number;
  totalPoints: number;
}

const router = useRouter();
const userStore = useUserStore();
const showDetail = ref(false);
const selectedAchievement = ref<Achievement | null>(null);
const activeTab = ref('all');
const loading = ref(false);

// 成就统计
const stats = ref<AchievementStats>({
  totalAchievements: 0,
  completedAchievements: 0,
  completionRate: 0,
  totalPoints: 0,
});

// 成就列表
const achievements = ref<Achievement[]>([]);

// 根据筛选条件过滤成就
const filteredAchievements = computed(() => {
  let result = [...achievements.value];

  // 根据标签页过滤
  if (activeTab.value === 'completed') {
    return result.filter(a => a.isCompleted);
  } else if (activeTab.value === 'inProgress') {
    return result.filter(a => !a.isCompleted);
  }

  return result;
});

// 显示成就详情
const showAchievementDetail = (achievement: Achievement) => {
  selectedAchievement.value = achievement;
  showDetail.value = true;
};

// 获取进度百分比
const getProgressPercent = (achievement: Achievement): number => {
  if (!achievement.progressMax || achievement.progressMax === 0) return 0;
  const percent = ((achievement.progress || 0) / achievement.progressMax) * 100;
  return Math.min(Math.round(percent), 100);
};

// 格式化成就日期
const formatAchievementDate = (dateStr: string): string => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// 根据分类获取背景类名
const getBgClass = (category?: string): string => {
  const classMap: Record<string, string> = {
    'skill': 'bg-blue',
    'persistence': 'bg-orange',
    'learning': 'bg-yellow',
    'social': 'bg-green',
  };
  return classMap[category || ''] || 'bg-blue';
};

// 根据分类获取图标
const getIconByCategory = (category?: string): string => {
  const iconMap: Record<string, string> = {
    'skill': 'award-o',
    'persistence': 'fire-o',
    'learning': 'star-o',
    'social': 'friends-o',
  };
  return iconMap[category || ''] || 'award-o';
};

// 根据分类获取颜色
const getColorByCategory = (category?: string): string => {
  const colorMap: Record<string, string> = {
    'skill': '#1989fa',
    'persistence': '#ff976a',
    'learning': '#ffcd32',
    'social': '#07c160',
  };
  return colorMap[category || ''] || '#1989fa';
};

// 领取成就奖励
const claimReward = async (achievement: Achievement) => {
  try {
    const userId = userStore.userInfo?.id;
    if (!userId) {
      showToast('请先登录');
      setTimeout(() => {
        router.push('/login');
      }, 1500);
      return;
    }

    const loading = showLoadingToast({
      message: '领取中...',
      forbidClick: true,
    });

    const response = await UserAchievementControllerService.claimAchievementRewardUsingPost(
      userId,
      achievement.achievementId
    );

    loading.clear();

    if (response.code === 0 && response.data) {
      showSuccessToast('奖励已领取');
      // 更新成就状态
      achievement.isClaimed = true;
      // 重新加载成就列表
      fetchAchievements();
      fetchAchievementStats();
    } else {
      showToast(response.message || '领取失败');
    }
  } catch (error) {
    console.error('领取奖励失败:', error);
    showToast('领取奖励失败');
  }
};

// 获取成就列表
const fetchAchievements = async () => {
  try {
    const userId = userStore.userInfo?.id;
    if (!userId) {
      showToast('请先登录');
      setTimeout(() => {
        router.push('/login');
      }, 1500);
      return;
    }

    loading.value = true;
    const response = await UserAchievementControllerService.getUserAchievementsUsingGet(userId);
    
    if (response.code === 0 && response.data) {
      // 这里需要根据实际返回的数据结构进行调整
      achievements.value = response.data as unknown as Achievement[];
    } else {
      showToast(response.message || '获取成就列表失败');
    }
  } catch (error) {
    console.error('获取成就列表失败:', error);
    showToast('获取成就列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取成就统计
const fetchAchievementStats = async () => {
  try {
    const userId = userStore.userInfo?.id;
    if (!userId) return;

    const response = await UserAchievementControllerService.getUserAchievementStatsUsingGet(userId);
    
    if (response.code === 0 && response.data) {
      stats.value = response.data as unknown as AchievementStats;
    }
  } catch (error) {
    console.error('获取成就统计失败:', error);
  }
};

// 页面加载时
onMounted(() => {
  fetchAchievements();
  fetchAchievementStats();
});
</script>

<style scoped>
.achievements-page {
  padding: 0 0 16px 0;
  background-color: #f2f7fd;
  min-height: 100vh;
}

.stats-card {
  display: flex;
  justify-content: space-between;
  padding: 16px;
  background-color: #ffffff;
  border-radius: 0px;
  margin: 0 0 8px 0;
}

.stats-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #323233;
  margin-bottom: 4px;
}

.stats-label {
  font-size: 14px;
  color: #969799;
}

.tab-container {
  background-color: #ffffff;
  margin-bottom: 8px;
}

.custom-tabs :deep(.van-tabs__wrap) {
  padding: 0 16px;
}

.custom-tabs :deep(.van-tabs__line) {
  background-color: #1989fa;
}

.achievement-grid {
  padding: 0 16px;
}

.achievement-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #ffffff;
  border-radius: 12px;
  margin-bottom: 8px;
  box-shadow: 0 2px 8px rgba(100, 101, 102, 0.05);
}

.badge-icon-wrapper {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  margin-right: 16px;
  flex-shrink: 0;
}

.badge-details {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.badge-name {
  font-size: 16px;
  font-weight: bold;
  color: #323233;
  margin-bottom: 4px;
}

.badge-description {
  font-size: 14px;
  color: #646566;
  margin-bottom: 4px;
}

.badge-date {
  font-size: 12px;
  color: #969799;
}

.badge-detail-popup {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.popup-header {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-bottom: 24px;
}

.close-icon {
  position: absolute;
  left: 0;
  font-size: 18px;
}

.popup-title {
  font-size: 18px;
  font-weight: bold;
  margin: 0;
}

.badge-detail-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.badge-icon-large {
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 24px;
  margin-bottom: 20px;
}

.badge-title {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 8px;
  text-align: center;
}

.badge-full-description {
  font-size: 16px;
  color: #323233;
  text-align: center;
  margin-bottom: 8px;
}

.badge-earn-date {
  font-size: 14px;
  color: #969799;
  margin-bottom: 24px;
}

.badge-criteria {
  width: 100%;
  background-color: #f7f8fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.badge-criteria h4 {
  font-size: 16px;
  margin-top: 0;
  margin-bottom: 8px;
  color: #323233;
}

.badge-criteria p {
  font-size: 14px;
  color: #646566;
  margin: 0;
}

.share-button {
  width: 100%;
  margin-top: auto;
}

/* 成就背景颜色 */
.bg-blue {
  background-color: rgba(25, 137, 250, 0.1);
}

.bg-orange {
  background-color: rgba(255, 151, 106, 0.1);
}

.bg-green {
  background-color: rgba(7, 193, 96, 0.1);
}

.bg-purple {
  background-color: rgba(114, 50, 221, 0.1);
}

.bg-red {
  background-color: rgba(238, 10, 36, 0.1);
}

.bg-yellow {
  background-color: rgba(255, 205, 50, 0.1);
}

.bg-cyan {
  background-color: rgba(0, 206, 209, 0.1);
}

.bg-pink {
  background-color: rgba(255, 105, 180, 0.1);
}
</style>
