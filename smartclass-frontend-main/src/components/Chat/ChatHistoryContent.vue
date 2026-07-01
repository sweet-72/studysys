<template>
  <div class="chat-history-content">
    <div class="history-container">
      <div class="content-wrapper">
        <div v-if="!loading && !error && transformedChatHistory.length > 0" class="chat-list-container">
          <chat-list
            :chats="transformedChatHistory"
            :show-status="false"
            list-class="history-flow"
            @select="handleChatSelect"
            @long-press="handleLongPress"
          />
        </div>

        <van-empty
          v-if="transformedChatHistory.length === 0 && !loading && !error"
          description="暂无对话记录"
          class="empty-state"
        />

        <div v-if="loading" class="loading-container">
          <van-loading type="spinner" size="32" color="#1989fa" />
          <p>正在加载对话记录...</p>
        </div>

        <network-error
          v-if="error"
          :message="error"
          :loading="retryLoading"
          @retry="retryLoadData"
        />
      </div>
    </div>

    <div v-if="transformedChatHistory.length > 0 && !loading && !error" class="fixed-pagination">
      <chat-pagination
        :total-items="total"
        :page-size="pageSize"
        :total-pages="totalPages"
        :initial-page="currentPage"
        @page-change="handlePageChange"
      />
    </div>

    <van-action-sheet
      v-model:show="showActionSheet"
      :actions="actionOptions"
      cancel-text="取消"
      close-on-click-action
      @select="handleActionSelect"
      @cancel="showActionSheet = false"
    />

    <van-dialog
      v-model:show="showDeleteConfirm"
      title="删除对话"
      :message="`确定要删除这条对话记录吗？该操作不可恢复。`"
      theme="round-button"
      confirm-button-color="#1989fa"
      cancel-button-color="#7d7e80"
      show-cancel-button
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { showSuccessToast, showToast } from 'vant';
import { ChatList, ChatPagination } from '../Dialogue';
import { NetworkError } from '../Common';
import { AiAvatarChatControllerService } from '../../services';
import type { ChatSessionVO } from '../../services/models/ChatSessionVO';
import { queryAiAvatarDetail, resolveAiAvatarImage } from '../../api/aiAvatar';

interface ChatItemType {
  id: number;
  sessionId?: string;
  assistantId: number;
  assistantName: string;
  avatar: string;
  lastMessage: string;
  summary?: string;
  lastTime: string;
  online?: boolean;
  tags?: string[];
  type: number;
}

interface ActionOption {
  name: string;
  color?: string;
  className?: string;
}

const emit = defineEmits(['select']);

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const totalPages = ref(0);
const loading = ref(false);
const retryLoading = ref(false);
const error = ref('');

const showActionSheet = ref(false);
const showDeleteConfirm = ref(false);
const chatToDelete = ref<ChatItemType | null>(null);
const actionOptions = [{ name: '删除对话', color: '#ee0a24' }];
const chatSessions = ref<ChatSessionVO[]>([]);
const publicAiAvatarIds = ref<Set<number>>(new Set());

const pagedSessions = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return chatSessions.value.slice(start, end);
});

const transformedChatHistory = computed<ChatItemType[]>(() =>
  pagedSessions.value
    .filter((session) => {
      // 过滤掉未公开的AI对话
      if (session.aiAvatarId) {
        return publicAiAvatarIds.value.has(session.aiAvatarId);
      }
      return false;
    })
    .map((session, index) => ({
      id: index + 1 + (currentPage.value - 1) * pageSize.value,
      sessionId: session.sessionId,
      assistantId: session.aiAvatarId || 0,
      assistantName: session.aiAvatarName || '未知智慧体',
      avatar: resolveAiAvatarImage(session.aiAvatarImgUrl),
      lastMessage: session.lastMessage || '',
      summary:
        typeof session.messageCount === 'number' ? `共 ${session.messageCount} 条消息` : '',
      lastTime: formatTime(session.lastMessageTime),
      tags: buildLearningTags(session.aiAvatarName || '', session.lastMessage || ''),
      type: 1,
    })),
);

const buildLearningTags = (name: string, message: string): string[] => {
  const text = `${name} ${message}`;
  if (/英语|口语|Emma/i.test(text)) return ['英语', '口语训练'];
  if (/物理|力学/i.test(text)) return ['物理', '解题'];
  if (/数学|公式|题/i.test(text)) return ['数学', '解题'];
  return ['课程', '知识点'];
};

const retryLoadData = () => {
  retryLoading.value = true;
  loadChatHistory().finally(() => {
    retryLoading.value = false;
  });
};

const formatTime = (timeStr?: string): string => {
  if (!timeStr) return '';

  try {
    const date = new Date(timeStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays === 0) {
      return `${date.getHours().toString().padStart(2, '0')}:${date
        .getMinutes()
        .toString()
        .padStart(2, '0')}`;
    }

    if (diffDays === 1) {
      return '昨天';
    }

    if (diffDays < 7) {
      const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
      return days[date.getDay()] || '';
    }

    return `${date.getMonth() + 1}月${date.getDate()}日`;
  } catch {
    return timeStr || '';
  }
};

const syncPagination = () => {
  total.value = chatSessions.value.length;
  totalPages.value = Math.max(1, Math.ceil(total.value / pageSize.value));

  if (currentPage.value > totalPages.value) {
    currentPage.value = totalPages.value;
  }
};

const loadPublicAiAvatarIds = async (sessions: ChatSessionVO[]) => {
  const aiAvatarIds = new Set<number>();
  sessions.forEach((session) => {
    if (session.aiAvatarId) {
      aiAvatarIds.add(session.aiAvatarId);
    }
  });

  if (aiAvatarIds.size === 0) {
    return;
  }

  try {
    const promises = Array.from(aiAvatarIds).map(async (id) => {
      try {
        const detail = await queryAiAvatarDetail(id);
        if (detail.isPublic === 1) {
          publicAiAvatarIds.value.add(id);
        }
      } catch {
        // 忽略单个查询失败，不影响其他查询
      }
    });

    await Promise.all(promises);
  } catch {
    // 批量查询失败时，默认为空，不显示任何会话
  }
};

const loadChatHistory = async () => {
  loading.value = true;
  error.value = '';
  publicAiAvatarIds.value = new Set();

  try {
    const response = await AiAvatarChatControllerService.getUserSessionsUsingGet();

    if (response.code === 0 && response.data) {
      const validSessions = response.data.filter((session) => !!session.sessionId);
      chatSessions.value = validSessions;

      // 加载公开AI智慧体ID列表
      await loadPublicAiAvatarIds(validSessions);

      syncPagination();
    } else {
      error.value = '获取对话记录失败，请检查网络连接';
      showToast(`获取对话记录失败: ${response.message || '未知错误'}`);
    }
  } catch {
    error.value = '网络连接失败，请检查网络设置后重试';
    showToast('加载对话记录出错');
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

const handleChatSelect = (chat: ChatItemType) => {
  emit('select', chat.sessionId || '', chat.assistantId);
};

const handleLongPress = (chat: ChatItemType) => {
  chatToDelete.value = chat;
  showActionSheet.value = true;
};

const handleActionSelect = (action: ActionOption) => {
  if (action.name === '删除对话') {
    showDeleteConfirm.value = true;
  }
};

const confirmDelete = async () => {
  if (!chatToDelete.value?.sessionId) return;

  loading.value = true;
  error.value = '';

  try {
    const response = await AiAvatarChatControllerService.deleteSessionUsingPost(
      chatToDelete.value.sessionId,
    );

    if (response.code === 0 && response.data) {
      showSuccessToast('删除成功');
      chatSessions.value = chatSessions.value.filter(
        (session) => session.sessionId !== chatToDelete.value?.sessionId,
      );
      syncPagination();
    } else {
      error.value = '删除失败，请稍后再试';
      showToast(`删除失败: ${response.message || '未知错误'}`);
    }
  } catch {
    error.value = '网络连接失败，请检查网络设置后重试';
    showToast('删除失败，请稍后再试');
  } finally {
    loading.value = false;
    chatToDelete.value = null;
  }
};

onMounted(() => {
  loadChatHistory();
});
</script>

<style scoped>
.chat-history-content {
  width: 100%;
  position: relative;
  min-height: 0;
  padding-bottom: 0;
}

.history-container {
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  padding: 0 8px;
  box-sizing: border-box;
  position: relative;
  z-index: 1;
}

.content-wrapper {
  position: relative;
  min-height: 0;
  padding-bottom: 8px;
  background-color: transparent;
}

.chat-list-container {
  width: 100%;
  position: relative;
  z-index: 2;
  display: block;
}

.fixed-pagination {
  position: static;
  width: 100%;
  margin-top: 8px;
  padding: 8px 0 0;
  background-color: transparent;
  box-shadow: none;
  backdrop-filter: none;
}

@media (max-width: 768px) {
  .history-container {
    padding: 0 4px;
  }

  .fixed-pagination {
    padding-top: 6px;
  }
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  width: 100%;
}

.loading-container p {
  margin-top: 12px;
  color: #64748b;
  font-size: 14px;
  font-family: 'Noto Sans SC', sans-serif;
}

.empty-state {
  padding: 40px 0;
}
</style>
