<template>
  <div class="notice-card">
    <div class="module-header">
      <h2>最新公告</h2>
      <span class="more-link" @click="goToNoticeList">更多</span>
    </div>

    <div class="notice-preview">
      <template v-if="notices && notices.length > 0">
        <div
          v-for="notice in displayNotices"
          :key="notice.id"
          class="notice-item"
          @click="showNoticeDetail(notice)"
        >
          <div class="notice-icon">
            <van-icon name="volume-o" />
          </div>
          <div class="notice-copy">
            <h4>{{ notice.title }}</h4>
            <p class="notice-brief">{{ notice.content }}</p>
          </div>
          <div class="notice-side">
            <span class="notice-date">{{ notice.date }}</span>
            <van-icon name="arrow" />
          </div>
        </div>

        <div
          v-if="notices.length > displayLimit && !showAll"
          class="expand-button"
          @click="loadMore"
        >
          <div class="expand-button-content">
            <van-icon name="arrow-down" />
            <span>展开更多公告</span>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="empty-notice">
          <van-empty description="暂无公告" />
        </div>
      </template>
    </div>

    <van-popup
      v-model:show="showDetail"
      round
      position="bottom"
      :style="{ height: '60%' }"
    >
      <div class="notice-detail">
        <div class="notice-popup-header">
          <span class="title">公告详情</span>
          <van-icon name="cross" @click="showDetail = false" />
        </div>
        <div class="notice-content" v-if="selectedNotice">
          <h3>{{ selectedNotice.title }}</h3>
          <p class="notice-date">{{ selectedNotice.date }}</p>
          <div class="notice-text">{{ selectedNotice.content }}</div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { AnnouncementControllerService } from '../../services/services/AnnouncementControllerService.ts';
import { AnnouncementVO } from '../../services/models/AnnouncementVO.ts';

interface Notice {
  id: number;
  title: string;
  date: string;
  content: string;
}

const props = defineProps<{
  notices?: Notice[];
  showLoadMore?: boolean;
}>();

const emit = defineEmits<{
  (e: 'loadMore'): void;
}>();

const router = useRouter();
const showDetail = ref(false);
const selectedNotice = ref<Notice | null>(null);
const localNotices = ref<Notice[]>([]);
const displayLimit = ref(1);
const showAll = ref(false);

const notices = computed(() => props.notices || localNotices.value);

const displayNotices = computed(() => {
  if (showAll.value) {
    return notices.value.slice(0, 3);
  }
  return notices.value.slice(0, displayLimit.value);
});

const loadMore = () => {
  showAll.value = true;
  emit('loadMore');
};

const convertAnnouncementToNotice = (announcement: AnnouncementVO): Notice => ({
  id: announcement.id || 0,
  title: announcement.title || '未命名公告',
  content: announcement.content || '',
  date: announcement.createTime
    ? new Date(announcement.createTime).toLocaleDateString()
    : '',
});

const fetchNotices = async () => {
  try {
    const response =
      await AnnouncementControllerService.listAnnouncementVoByPageUsingGet(
        undefined,
        undefined,
        undefined,
        undefined,
        1,
        undefined,
        undefined,
        undefined,
        3,
        undefined,
        'createTime',
        'desc',
        undefined,
        undefined,
        undefined,
      );

    if (response.code === 0 && response.data?.records?.length) {
      localNotices.value = response.data.records.map(convertAnnouncementToNotice);
    } else {
      localNotices.value = [
        {
          id: 1,
          title: '系统优化',
          date: new Date().toLocaleDateString(),
          content: '系统已优化部分已知问题',
        },
      ];
    }
  } catch (error) {
    showToast('获取公告数据失败');
    localNotices.value = [
      {
        id: 1,
        title: '系统优化',
        date: new Date().toLocaleDateString(),
        content: '系统已优化部分已知问题',
      },
    ];
  }
};

const goToNoticeList = (): void => {
  router.push('/notices');
};

const showNoticeDetail = (notice: Notice): void => {
  selectedNotice.value = notice;
  showDetail.value = true;

  if (notice.id) {
    markNoticeAsRead(notice.id);
  }
};

const markNoticeAsRead = async (id: number) => {
  try {
    await AnnouncementControllerService.readAnnouncementUsingPost(id);
  } catch (error) {
    // 忽略已读标记失败，不影响公告浏览。
  }
};

onMounted(() => {
  if (!props.notices) {
    fetchNotices();
  }
});
</script>

<style scoped>
.notice-card {
  margin-bottom: 0;
  overflow: hidden;
  background: transparent;
  border: none;
  border-radius: 0;
  box-shadow: none;
}

.module-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.module-header h2 {
  margin: 0;
  color: #1e293b;
  font-size: 20px !important;
  font-weight: 800;
  line-height: 1.25;
}

.more-link {
  color: #94a3b8;
  font-size: 13px;
  font-weight: 700;
}

.notice-preview {
  padding: 0 2px 4px;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 62px;
  padding: 10px 12px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid rgba(255, 255, 255, 0.62);
  border-radius: 18px;
  box-shadow: none;
  backdrop-filter: blur(12px);
}

.notice-item:not(:last-child) {
  margin-bottom: 10px;
}

.notice-icon {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  color: #6366f1;
  background: rgba(238, 242, 255, 0.72);
  border-radius: 14px;
}

.notice-copy {
  flex: 1;
  min-width: 0;
}

.notice-preview h4 {
  margin: 0 0 4px;
  color: #1e293b;
  font-size: 14px;
  font-weight: 800;
}

.notice-brief {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: #94a3b8;
  font-size: var(--font-size-sm);
  line-height: 1.5;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
}

.notice-side {
  display: flex;
  align-items: center;
  flex: 0 0 auto;
  justify-content: space-between;
  gap: 8px;
  color: #94a3b8;
  font-size: var(--font-size-sm);
}

.notice-date {
  margin: 0;
  color: #94a3b8;
  font-size: var(--font-size-sm);
}

.notice-detail {
  padding: 16px;
}

.notice-popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px 16px;
  border-bottom: 1px solid #ebedf0;
}

.notice-popup-header .title {
  font-size: var(--font-size-md);
  font-weight: 700;
}

.notice-content {
  padding: 16px 4px;
}

.notice-content h3 {
  margin: 0 0 8px;
  color: #1e293b;
  font-size: var(--font-size-lg);
  font-weight: 700;
}

.notice-text {
  color: #475569;
  font-size: var(--font-size-base, 14px);
  line-height: 1.6;
}

.empty-notice {
  padding: 20px 0;
  color: #94a3b8;
  text-align: center;
}

.expand-button {
  padding: 0;
  margin-top: 4px;
  cursor: pointer;
  text-align: center;
  border-top: 0;
}

.expand-button-content {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 20px;
  padding: 5px 0;
  color: #94a3b8;
  font-size: var(--font-size-sm);
  line-height: 1;
}

.expand-button-content span {
  display: inline-block;
  margin-left: 4px;
  line-height: 20px;
  vertical-align: middle;
}

.expand-button-content .van-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 20px;
  font-size: 14px;
}
</style>
