<template>
  <div class="level-page">
    <back-button title="我的等级" />

    <div class="hero-card">
      <div class="hero-top">
        <div class="hero-main">
          <div class="hero-label">当前等级</div>
          <div class="hero-value">Lv.{{ currentLevel }}</div>
          <div class="hero-name">{{ currentLevelName }}</div>
        </div>

        <div class="hero-icon">
          <img
            v-if="resolvedIconUrl && !iconLoadFailed"
            :src="resolvedIconUrl"
            alt="level icon"
            class="hero-icon-image"
            @error="iconLoadFailed = true"
          />
          <van-icon
            v-else
            name="star-o"
            size="34"
            color="#fff"
          />
        </div>
      </div>

      <div class="progress-row">
        <span>当前经验 {{ currentExp }}</span>
        <span>升级阈值 {{ nextLevelExp }}</span>
      </div>

      <van-progress
        :percentage="progressPercentage"
        :show-pivot="false"
        stroke-width="10"
        color="linear-gradient(90deg, #1d4ed8, #60a5fa)"
        track-color="rgba(255,255,255,0.24)"
      />

      <div class="hero-tip">
        <template v-if="expToNextLevel > 0">
          距离下一级还差 {{ expToNextLevel }} 经验
        </template>
        <template v-else>
          当前等级已满或已达到最高等级
        </template>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stats-item">
        <div class="stats-value">{{ totalExp }}</div>
        <div class="stats-label">累计经验</div>
      </div>
      <div class="stats-item">
        <div class="stats-value">{{ continuousLoginDays }}</div>
        <div class="stats-label">连续登录</div>
      </div>
      <div class="stats-item">
        <div class="stats-value">{{ progressPercentage }}%</div>
        <div class="stats-label">升级进度</div>
      </div>
    </div>

    <div class="section-card">
      <div class="section-title">等级特权</div>
      <div v-if="privilegeList.length" class="privilege-list">
        <div
          v-for="item in privilegeList"
          :key="item"
          class="privilege-item"
        >
          <van-icon name="passed" color="#2563eb" />
          <span>{{ item }}</span>
        </div>
      </div>
      <div
        v-else
        class="section-description muted"
      >
        当前等级暂未配置特权说明
      </div>
    </div>

    <div class="section-card">
      <div class="section-title">等级信息</div>
      <div class="info-list">
        <div class="info-row">
          <span class="info-label">用户 ID</span>
          <span class="info-value">{{ userIdDisplay }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">等级名称</span>
          <span class="info-value">{{ currentLevelName }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">当前经验</span>
          <span class="info-value">{{ currentExp }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">累计经验</span>
          <span class="info-value">{{ totalExp }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">距离升级</span>
          <span class="info-value">{{ expToNextLevel }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">上次升级时间</span>
          <span class="info-value">{{ levelUpTimeText }}</span>
        </div>
      </div>
    </div>

    <van-loading
      v-if="loading"
      class="page-loading"
      size="24px"
      vertical
    >
      加载中...
    </van-loading>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { showToast } from 'vant';
import { BackButton } from '@/components/Common';
import { queryUserExpLevel, type UserExpLevelInfo } from '@/api/userExp';
import { useUserStore } from '@/stores/userStore';
import { buildApiUrl } from '@/utils/api';

const userStore = useUserStore();
const loading = ref(false);
const levelInfo = ref<UserExpLevelInfo | null>(null);
const iconLoadFailed = ref(false);

const currentLevel = computed(() => levelInfo.value?.level || 1);
const currentLevelName = computed(() => levelInfo.value?.levelName || `等级 ${currentLevel.value}`);
const currentExp = computed(() => levelInfo.value?.exp || 0);
const nextLevelExp = computed(() => levelInfo.value?.nextLevelExp || 0);
const totalExp = computed(() => levelInfo.value?.totalExp || currentExp.value);
const progressPercentage = computed(() => {
  const progress = levelInfo.value?.progressPercent;
  if (typeof progress === 'number' && Number.isFinite(progress)) {
    return Math.max(0, Math.min(100, Math.round(progress)));
  }
  if (!nextLevelExp.value) {
    return 0;
  }
  return Math.max(0, Math.min(100, Math.round((currentExp.value / nextLevelExp.value) * 100)));
});
const expToNextLevel = computed(() => levelInfo.value?.expToNextLevel || 0);
const continuousLoginDays = computed(() => levelInfo.value?.continuousLoginDays || 0);
const userIdDisplay = computed(() => levelInfo.value?.userId || userStore.userInfo?.id || '--');
const resolvedIconUrl = computed(() => {
  const raw = String(levelInfo.value?.iconUrl || '').trim();
  if (!raw) {
    return '';
  }
  if (/^(https?:)?\/\//i.test(raw) || raw.startsWith('data:') || raw.startsWith('blob:')) {
    return raw;
  }
  return buildApiUrl(raw.startsWith('/') ? raw : `/${raw}`);
});

const privilegeList = computed(() => {
  const raw = levelInfo.value?.privilegeDesc || '';
  return raw
    .split(/[\n,;；、]/)
    .map((item) => item.trim())
    .filter(Boolean);
});

const levelUpTimeText = computed(() => {
  const raw = levelInfo.value?.levelUpTime;
  if (!raw) {
    return '--';
  }

  const date = new Date(raw);
  if (Number.isNaN(date.getTime())) {
    return raw;
  }

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
});

const loadLevelInfo = async () => {
  loading.value = true;

  try {
    const currentUser = userStore.userInfo || (await userStore.fetchCurrentUser());
    const userId = Number(currentUser?.id || 0);

    if (!Number.isFinite(userId) || userId <= 0) {
      showToast('未获取到当前用户信息');
      return;
    }

    levelInfo.value = await queryUserExpLevel(userId);
  } catch (error) {
    console.error('Failed to load user exp level', error);
    showToast(error instanceof Error ? error.message : '等级信息加载失败');
  } finally {
    loading.value = false;
  }
};

watch(
  resolvedIconUrl,
  () => {
    iconLoadFailed.value = false;
  },
  { immediate: true },
);

onMounted(() => {
  loadLevelInfo();
});
</script>

<style scoped>
.level-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(96, 165, 250, 0.28), transparent 28%),
    linear-gradient(180deg, #eef5ff 0%, #f8fbff 34%, #f5f7fb 100%);
  padding-bottom: 24px;
}

.hero-card {
  margin: 0 16px 12px;
  padding: 18px;
  border-radius: 20px;
  background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 48%, #60a5fa 100%);
  color: #fff;
  box-shadow: 0 14px 32px rgba(37, 99, 235, 0.24);
}

.hero-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.hero-main {
  min-width: 0;
}

.hero-label {
  font-size: 13px;
  opacity: 0.88;
}

.hero-value {
  margin-top: 6px;
  font-size: 34px;
  font-weight: 700;
  line-height: 1;
}

.hero-name {
  margin-top: 8px;
  font-size: 14px;
  opacity: 0.92;
}

.hero-icon {
  width: 62px;
  height: 62px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(8px);
  flex-shrink: 0;
}

.hero-icon-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.progress-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
  margin-bottom: 10px;
  font-size: 12px;
  opacity: 0.92;
}

.hero-tip {
  margin-top: 10px;
  font-size: 12px;
  opacity: 0.94;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin: 0 16px 12px;
}

.stats-item {
  border-radius: 16px;
  background: #fff;
  padding: 16px 12px;
  text-align: center;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.05);
}

.stats-value {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.stats-label {
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
}

.section-card {
  margin: 0 16px 12px;
  padding: 16px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.05);
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}

.section-description {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.7;
  color: #334155;
}

.section-description.muted {
  color: #94a3b8;
}

.privilege-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.privilege-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: #1e293b;
}

.info-list {
  margin-top: 10px;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #eef2f7;
}

.info-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.info-label {
  font-size: 13px;
  color: #64748b;
}

.info-value {
  font-size: 13px;
  color: #0f172a;
  text-align: right;
  word-break: break-word;
}

.page-loading {
  margin-top: 18px;
}
</style>
