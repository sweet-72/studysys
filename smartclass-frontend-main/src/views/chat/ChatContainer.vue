<template>
  <div class="chat-container has-tabbar">
    <!-- 固定头部区域 -->
    <div class="fixed-header">
      <span class="ambient ambient-one"></span>
      <span class="ambient ambient-two"></span>
      <!-- 页面标题区域 -->
      <div class="header">
        <van-image
          :src="useUserStore().userInfo?.userAvatar || useUserStore().DEFAULT_USER_AVATAR || '/default.jpg'"
          round
          width="46"
          height="46"
          fit="cover"
          class="header-avatar"
        />
        <div class="page-title">
          <span class="chat-title-text">AI学习空间</span>
          <van-icon name="chat-o" class="title-icon" />
          <span>聊天</span>
        </div>
        <div class="header-actions">
          <van-badge :content="friendRequestCount > 0 ? friendRequestCount : ''" :max="99" class="notification-badge">
            <van-icon name="bell" size="24" class="action-icon" @click="handleFriendRequests" />
          </van-badge>
        </div>
      </div>
    </div>

    <!-- 可滚动内容区域 -->
    <div class="scrollable-content">
      <section class="chat-workspace">
        <!-- 导航栏 -->
        <div class="nav-tabs">
          <div class="tab-glider" :style="{ transform: `translateX(${activeTabIndex * 100}%)` }"></div>
          <div
            :class="['nav-tab', { active: activeTab === 'history' }]"
            data-label="学习记录"
            @click="switchTab('history')"
          >
            历史对话
          </div>
          <div
            :class="['nav-tab', { active: activeTab === 'friends' }]"
            data-label="学习伙伴"
            @click="switchTab('friends')"
          >
            好友
          </div>
          <div
            :class="['nav-tab', { active: activeTab === 'intelligence' }]"
            data-label="AI老师"
            @click="switchTab('intelligence')"
          >
            智慧体中心
          </div>
        </div>

        <!-- 内容区域 -->
        <div class="tab-content">
        <!-- 历史对话内容 -->
        <div v-show="activeTab === 'history'" class="tab-pane">
          <div class="content-container">
            <chat-history-content @select="handleChatSelect" />
          </div>
        </div>
        
        <!-- 好友内容 -->
        <div v-show="activeTab === 'friends'" class="tab-pane">
          <div class="content-container">
            <div class="content-wrapper">
              <div v-if="friendsLoading" class="loading-container">
                <van-loading type="spinner" size="32" color="#1989fa" />
                <p>正在加载好友列表...</p>
              </div>
              <chat-list 
                v-else-if="friends.length > 0" 
                :chats="friends" 
                :show-status="true" 
                list-class="friend-flow"
                :show-partner-stats="true"
                @select="handleFriendSelect" 
              />
              <div v-else class="empty-container cat-empty">
                <img :src="learningCharacter" alt="" />
                <p>还没有学习伙伴，一起邀请朋友学习吧 🐣</p>
                <van-empty description="暂无好友数据" />
              </div>
            </div>
          </div>
        </div>

        <!-- 智慧体中心内容 -->
        <div v-show="activeTab === 'intelligence'" class="tab-pane">
          <div class="content-container">
            <intelligence-center-content @select="handleAssistantSelect" />
          </div>
        </div>
      </div>
      </section>
    </div>

    <!-- 新建对话按钮 -->
    <van-button
      v-show="activeTab === 'history'"
      class="new-chat-btn"
      type="primary"
      round
      icon="plus"
      @click="switchTab('intelligence')"
    >
      新建对话
    </van-button>

    <!-- 添加好友按钮 -->
    <van-button
      v-show="activeTab === 'friends'"
      class="new-chat-btn"
      type="primary"
      round
      icon="plus"
      @click="handleAddFriend"
    >
      添加好友
    </van-button>

    <!-- 与智能体对话按钮 -->
    <van-button
      v-show="activeTab === 'intelligence'"
      class="new-chat-btn"
      type="primary"
      round
      icon="comment-o"
      @click="handleChatWithIntelligence"
    >
      与智能体对话
    </van-button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showToast, showLoadingToast, closeToast } from 'vant';
import { useUserStore } from '../../stores/userStore';
import { ChatHistoryContent, IntelligenceCenterContent } from '../../components/Chat';
import { ChatList } from '../../components/Dialogue';
import { FriendRelationshipControllerService } from '../../services/services/FriendRelationshipControllerService';
import { ChatControllerService } from '../../services/services/ChatControllerService';
import { AiAvatarControllerService } from '../../services/services/AiAvatarControllerService';
import { AiAvatarChatControllerService } from '../../services/services/AiAvatarChatControllerService';
import { FriendRelationshipVO } from '../../services/models/FriendRelationshipVO';
import { PrivateMessageVO } from '../../services/models/PrivateMessageVO';
import { formatTimeAgo } from '../../utils/timeUtils';
import { FriendRequestControllerService } from '../../services/services/FriendRequestControllerService';
import learningCharacter from '../../assets/images/learning-character.png';

const router = useRouter();
const route = useRoute();
const activeTab = ref('history'); // 默认显示历史对话
const tabOrder = ['history', 'friends', 'intelligence'];
const activeTabIndex = computed(() => Math.max(tabOrder.indexOf(activeTab.value), 0));
const friendRequestCount = ref(0); // 好友请求数量
const friendsLoading = ref(false);
const recentMessagesLoading = ref(false);
const friendRelationships = ref<FriendRelationshipVO[]>([]);
const recentMessages = ref<PrivateMessageVO[]>([]);
const totalUnreadCount = ref(0); // 总未读消息数（用于全局红点）
const friendTotalUnread = ref(0); // 好友列表的总未读数（用于好友标签）
const friendUnreadMap = ref<Record<number, number>>({}); // 每个好友的未读数

// 轮询定时器
let unreadCountPollingInterval: number | null = null;
const POLLING_INTERVAL = 5000; // 5 秒轮询一次

// 好友列表数据
const friends = ref<any[]>([]);

// 获取好友列表
const fetchFriends = async () => {
  friendsLoading.value = true;
  try {
    // ✅ 使用新的接口：/api/private-chat/friends（后端已修改返回格式）
    const response = await FriendRelationshipControllerService.getFriendRelationshipUsingGet(
      undefined,
      undefined
    );
    
    console.log('=== 获取好友列表响应 ===');
    console.log('完整响应:', response);
    console.log('response.code:', response.code);
    console.log('response.data:', response.data);
    console.log('response.data 类型:', Array.isArray(response.data) ? 'Array' : typeof response.data);
    
    if (response.code === 0) {
      // ✅ 适配新的返回格式：[{ user: {...}, unreadCount: 2 }]
      let friendsData = response.data;
      
      // 如果 data 不是数组，尝试从 records 中获取（兼容分页格式）
      if (!Array.isArray(friendsData) && friendsData?.records) {
        friendsData = friendsData.records;
      }
      
      console.log('处理前的原始数据:', friendsData);
      console.log('好友数量:', friendsData?.length || 0);
      
      // ✅ 新增：适配新的数据结构，提取 user 和 unreadCount
      if (Array.isArray(friendsData)) {
        const processedFriends = friendsData.map((item: any, index: number) => {
          console.log(`好友 ${index + 1}:`, item);
          
          // ✅ 新格式：{ user: {...}, unreadCount: 2 }
          const user = item.user || item;
          const unreadCount = item.unreadCount || 0;
          
          console.log('  - 解析后的 user:', user);
          console.log('  - 未读数:', unreadCount);
          
          return {
            id: user.id || user.userId,
            assistantId: user.id || user.userId,
            assistantName: user.userName || user.name || '未命名用户',
            avatar: user.userAvatar || user.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
            userRole: user.userRole || user.role,
            tags: user.userRole ? [user.userRole] : [],
            lastMessage: '暂无消息',
            lastTime: '',
            type: 1, // 好友类型
            unreadCount: friendUnreadMap.value[user.id || user.userId] || 0, // ✅ 使用后端返回的未读数
            isLastMessageUnread: (friendUnreadMap.value[user.id || user.userId] || 0) > 0, // ✅ 根据未读数标记
          };
        });
        
        friends.value = processedFriends;
        
        // ✅ 计算好友列表的总未读数（从 friendUnreadMap 中获取）
        friendTotalUnread.value = Object.values(friendUnreadMap.value).reduce((sum, count) => {
          return sum + (count || 0);
        }, 0);
        
        console.log('处理后的好友列表:', friends.value);
        console.log('好友列表总未读数:', friendTotalUnread.value);
      } else {
        friends.value = [];
      }
      
      console.log('最终好友列表:', friends.value);
      console.log('========================');
    } else {
      showToast('获取好友列表失败：' + (response.message || '未知错误'));
    }
  } catch (error) {
    console.error('=== 获取好友列表错误 ===', error);
    showToast('获取好友列表出错');
  } finally {
    friendsLoading.value = false;
  }
};

// 获取最新消息（会话列表）
const fetchRecentMessages = async () => {
  recentMessagesLoading.value = true;
  try {
    // 调用会话列表接口
    const response = await ChatControllerService.listUserSessionsUsingGet();
    
    console.log('获取会话列表响应:', response);
    
    if (response.code === 0 && response.data) {
      // 后端返回的是分页对象，需要使用 .records 获取实际数组
      const chatSessions = response.data.records || [];
      console.log('会话列表数据:', chatSessions);
      
      const recentMessageList = chatSessions.map(session => session.lastMessage || {});
      recentMessages.value = recentMessageList;
      console.log('处理后的消息列表:', recentMessages.value);
      
      // ✅ 获取所有会话的未读消息数
      await loadAllUnreadCounts();
      
      // 直接使用会话列表生成好友列表
      processFriendsFromSessions(chatSessions);
      console.log('处理后的好友列表:', friends.value);
    } else {
      showToast('获取最新消息失败：' + (response.message || '未知错误'));
    }
  } catch (error) {
    console.error('获取最新消息出错：', error);
    showToast('获取最新消息出错');
  } finally {
    recentMessagesLoading.value = false;
  }
};

// 获取所有会话的未读消息数（使用新接口）
const loadAllUnreadCounts = async () => {
  try {
    const response = await ChatControllerService.getAllSessionsUnreadCountUsingGet();
    
    if (response.code === 0 && response.data) {
      const unreadCounts = response.data; // { "5_7": 3, "5_10": 1, ... }
      console.log('所有会话未读数:', unreadCounts);
      
      // 获取当前用户 ID
      const userStore = useUserStore();
      const currentUserId = userStore.userInfo?.id || 0;
      
      // 解析每个好友的未读数
      friendUnreadMap.value = {};
      let total = 0;
      
      Object.entries(unreadCounts).forEach(([sessionId, count]) => {
        if (typeof count === 'number') {
          total += count;
          
          // 从 sessionId 中提取好友 ID
          const [id1, id2] = sessionId.split('_');
          const friendId = id1 === currentUserId.toString() ? parseInt(id2) : parseInt(id1);
          
          // 更新该好友的未读数
          friendUnreadMap.value[friendId] = count;
          
          console.log(`  会话 ${sessionId} -> 好友 ${friendId}: ${count} 条未读`);
        }
      });
      
      totalUnreadCount.value = total;
      console.log('总未读消息数:', total);
      console.log('好友未读映射:', friendUnreadMap.value);
      
      // 更新标题栏红点
      if (totalUnreadCount.value > 0) {
        document.title = `(${totalUnreadCount.value}) AI 赋能的教育系统`;
      } else {
        document.title = 'AI 赋能的教育系统';
      }
    }
  } catch (error) {
    console.error('获取所有会话未读数失败:', error);
    // 不显示错误提示，避免影响用户体验
  }
};

// 是否有全局红点
const hasGlobalRedDot = computed(() => totalUnreadCount.value > 0);

// 启动未读数轮询
const startUnreadCountPolling = () => {
  // 如果已经在轮询，则不重复启动
  if (unreadCountPollingInterval !== null) return;
  
  console.log('启动未读数轮询，间隔:', POLLING_INTERVAL, 'ms');
  
  // 立即获取一次
  loadAllUnreadCounts();
  
  // 设置定时器定期获取
  unreadCountPollingInterval = window.setInterval(() => {
    loadAllUnreadCounts();
  }, POLLING_INTERVAL);
};

// 停止未读数轮询
const stopUnreadCountPolling = () => {
  if (unreadCountPollingInterval !== null) {
    window.clearInterval(unreadCountPollingInterval);
    unreadCountPollingInterval = null;
    console.log('已停止未读数轮询');
  }
};

// 直接从会话列表生成好友列表
const processFriendsFromSessions = (chatSessions: any[] = []) => {
  console.log('进入 processFriendsFromSessions 函数');
  console.log('会话列表:', chatSessions);
  
  if (!chatSessions.length) {
    console.warn('没有会话数据，跳过处理');
    return;
  }
  
  const friendsList = chatSessions.map((session, index) => {
    console.log(`处理第 ${index + 1} 个会话:`, session);
    
    // 使用 targetUser 作为好友信息
    const targetUser = session.targetUser;
    
    if (!targetUser) {
      console.warn(`第 ${index + 1} 个会话缺少 targetUser`);
      return null;
    }
    
    // 设置中文标签
    const userRoleTags = [];
    if (targetUser.userRole) {
      const roleMap: Record<string, string> = {
        'STUDENT': '学生',
        'TEACHER': '老师',
        'ADMIN': '管理员',
        'USER': '普通用户'
      };
      userRoleTags.push(roleMap[targetUser.userRole] || targetUser.userRole);
    }
    
    // 获取最后一条消息
    const lastMessage = session.lastMessage;
    
    const result = {
      id: session.id,
      assistantId: targetUser.id,
      assistantName: targetUser.userName || '未命名用户',
      avatar: targetUser.userAvatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
      // 使用最新消息或默认文本
      lastMessage: lastMessage?.content || '暂无消息',
      // 格式化消息时间
      lastTime: lastMessage?.createTime ? formatTimeAgo(new Date(lastMessage.createTime)) : '',
      // 角色标签
      tags: userRoleTags,
      type: 1, // 好友类型
      // ✅ 使用 friendUnreadMap 中的未读数据
      unreadCount: friendUnreadMap.value[targetUser.id] || 0,
      isLastMessageUnread: (friendUnreadMap.value[targetUser.id] || 0) > 0,
    };
    
    console.log('处理后的好友对象:', result);
    return result;
  }).filter(Boolean) as any[];
  
  console.log('最终的好友列表:', friendsList);
  friends.value = friendsList;
};

// 处理好友和消息数据（兼容两种数据格式）
const processFriendsAndMessages = (chatSessions: any[] = []) => {
  console.log('进入 processFriendsAndMessages 函数');
  console.log('好友关系列表:', friendRelationships.value);
  console.log('好友关系数量:', friendRelationships.value.length);
  
  if (!friendRelationships.value.length) {
    console.warn('好友关系列表为空，返回');
    return;
  }
  
  // 将好友关系和最新消息整合到一起
  const friendsList = friendRelationships.value.map((relationship, index) => {
    console.log(`\n处理第 ${index + 1} 个好友关系:`);
    console.log('relationship:', relationship);
    
    // ✅ 修复：兼容两种数据格式
    // 格式 1：后端返回嵌套对象 { friendUser: {...}, userId1: ..., userId2: ... }
    // 格式 2：后端返回扁平对象 { id: ..., userName: ..., userAvatar: ... }
    
    let friendUser;
    let friendId;
    let friendName;
    let friendAvatar;
    let friendRole;
    let userId1;
    let userId2;
    
    // 检查是否是嵌套格式
    if (relationship.friendUser) {
      // 格式 1：嵌套对象
      friendUser = relationship.friendUser;
      friendId = friendUser.id;
      friendName = friendUser.userName || '未命名用户';
      friendAvatar = friendUser.userAvatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg';
      friendRole = friendUser.userRole;
      userId1 = relationship.userId1;
      userId2 = relationship.userId2;
      console.log('检测到格式 1：嵌套对象');
    } else if (relationship.userName !== undefined) {
      // 格式 2：扁平对象（✅ 后端实际返回的格式）
      friendUser = relationship;
      friendId = relationship.id;
      friendName = relationship.userName || '未命名用户';
      friendAvatar = relationship.userAvatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg';
      friendRole = relationship.userRole;
      userId1 = relationship.userId1 || relationship.id;
      userId2 = relationship.userId2 || undefined;
      console.log('检测到格式 2：扁平对象（后端实际返回格式）✅');
    } else {
      // 未知格式
      console.warn(`第 ${index + 1} 个好友关系格式未知，跳过`);
      return null;
    }
    
    console.log('解析后的好友信息:', { friendId, friendName, friendAvatar, friendRole });
    
    // 查找与该好友相关的最新消息
    const lastMessage = recentMessages.value.find(
      msg => 
        (msg.senderId === friendId && msg.receiverId === userId1) || 
        (msg.receiverId === friendId && msg.senderId === userId1)
    );
    
    console.log('找到的最新消息:', lastMessage);
    
    // 设置中文标签
    const userRoleTags = [];
    if (friendRole) {
      const roleMap: Record<string, string> = {
        'STUDENT': '学生',
        'TEACHER': '老师',
        'ADMIN': '管理员',
        'USER': '普通用户',
        'student': '学生',
        'teacher': '老师',
        'admin': '管理员',
        'user': '普通用户'
      };
      userRoleTags.push(roleMap[friendRole] || friendRole);
    }
    
    // 查找当前会话信息，获取未读消息数量
    const session = chatSessions.find(
      session => 
        (session.userId1 === userId1 && session.userId2 === friendId) ||
        (session.userId2 === userId1 && session.userId1 === friendId)
    );
    
    const unreadCount = session?.unreadCount || 0;
    
    const result = {
      id: relationship.id || friendId,
      assistantId: friendId,
      assistantName: friendName,
      avatar: friendAvatar,
      // 使用最新消息或默认文本
      lastMessage: lastMessage?.content || '暂无消息',
      // 格式化消息时间
      lastTime: lastMessage?.createTime ? formatTimeAgo(new Date(lastMessage.createTime)) : '',
      // 角色标签
      tags: userRoleTags,
      type: 1, // 好友类型
      unreadCount: unreadCount, // 添加未读消息数量
      isLastMessageUnread: lastMessage?.senderId === friendId && lastMessage?.isRead === 0, // 最后一条消息是否未读
    };
    
    console.log('处理后的好友对象:', result);
    return result;
  }).filter(Boolean) as any[];
  
  console.log('\n处理完成，最终的好友列表:', friendsList);
  console.log('好友列表数量:', friendsList.length);
  friends.value = friendsList;
};

// 监听标签页变化，当切换到好友标签时加载数据
watch(activeTab, (newTab) => {
  if (newTab === 'friends') {
    // 每次都刷新好友列表，确保数据最新
    fetchFriends();
  }
});

// 更多功能
const handleMore = () => {
  showToast('更多功能开发中');
};

const handleAddFriend = () => {
  router.push('/friends/add');
};

// 处理好友请求
const handleFriendRequests = () => {
  router.push('/friends/requests');
};

// 处理与智能体对话
const handleChatWithIntelligence = async () => {
  const loadingToast = showLoadingToast({
    message: '正在创建会话...',
    duration: 0,
    forbidClick: true,
  });

  try {
    // 首先获取公开的 AI 助手列表
    const response = await AiAvatarControllerService.listAiAvatarByPageUsingGet(
      undefined, // abilities
      undefined, // adminId
      undefined, // avatarUrl
      undefined, // category
      undefined, // createTime
      undefined, // creatorId
      1, // current - 当前页码
      undefined, // description
      undefined, // id
      1, // isPublic - 只获取公开的智慧体
      undefined, // modelType
      undefined, // name
      50, // pageSize - 每页数量
      undefined, // personality
      undefined, // rating
      undefined, // sortField
      undefined, // sortOrder
      1, // status - 只获取正常状态的智慧体
      undefined, // tags
      undefined // usageCount
    );

    if (response.code === 0 && response.data?.records) {
      const aiAvatars = response.data.records;
      
      // 尝试查找"博学多才的熊猫老师"
      let targetAI = aiAvatars.find(ai => ai.name?.includes('熊猫'));
      
      // 如果没找到，使用第一个 AI
      if (!targetAI && aiAvatars.length > 0) {
        targetAI = aiAvatars[0];
      }
      
      if (targetAI && targetAI.id) {
        // 创建会话
        const sessionResponse = await AiAvatarChatControllerService.createSessionUsingPost(targetAI.id);
        
        if (sessionResponse.code === 0 && sessionResponse.data) {
          const sessionId = sessionResponse.data;
          
          // 跳转到聊天详情页面
          router.push({
            path: `/chat/detail/${targetAI.id}`,
            query: { 
              sessionId,
              tab: 'intelligence'
            }
          });
        } else {
          showToast('创建会话失败');
        }
      } else {
        showToast('暂无可用的智能体');
      }
    } else {
      showToast('获取智能体列表失败');
    }
  } catch (error) {
    console.error('创建会话出错：', error);
    showToast('创建会话失败，请稍后再试');
  } finally {
    closeToast();
  }
};

// 获取好友请求数量
const fetchFriendRequestCount = async () => {
  try {
    const response = await FriendRequestControllerService.getPendingRequestCountUsingGet();
    if (response.code === 0 && response.data !== undefined) {
      const count = Number(response.data);
      // ✅ 只有当有好友请求时才显示红点
      friendRequestCount.value = count > 0 ? count : 0;
      console.log('好友请求数量:', friendRequestCount.value);
    }
  } catch (error) {
    console.error('获取好友请求数量出错：', error);
  }
};

// 检查 URL 参数，决定默认显示哪个标签页
onMounted(() => {
  const tab = route.query.tab as string;
  if (tab === 'intelligence' || tab === 'history' || tab === 'friends') {
    activeTab.value = tab;
    if (tab === 'friends') {
      fetchFriends();
    }
  } else {
    // 如果 URL 没有有效的 tab 参数，设置为默认标签并更新 URL
    activeTab.value = 'history';
    router.replace({
      path: route.path,
      query: { ...route.query, tab: 'history' },
    });
  }
  
  // 获取好友请求数量
  fetchFriendRequestCount();
  
  // 监听好友列表刷新事件（来自好友请求页面）
  window.addEventListener('refresh-friends-list', handleRefreshFriends);
  
  // ✅ 启动未读数轮询
  startUnreadCountPolling();
});

// 处理好友列表刷新事件
const handleRefreshFriends = () => {
  console.log('收到刷新好友列表事件');
  fetchFriends();
};

// 离开页面时清理资源
onUnmounted(() => {
  // 移除事件监听
  window.removeEventListener('refresh-friends-list', handleRefreshFriends);
  
  // ✅ 停止未读数轮询
  stopUnreadCountPolling();
  
  // 停止所有定时器和轮询请求
  console.log('离开聊天页面，已清理所有定时器');
});

// 切换标签页
const switchTab = (tab: string) => {
  activeTab.value = tab;

  // 如果切换到好友标签，加载好友数据（每次都刷新，确保数据最新）
  if (tab === 'friends') {
    fetchFriends();
  }

  // 更新 URL 参数，但不触发页面刷新
  const query = { ...route.query };
  query.tab = tab; // 设置 tab 参数为当前标签页

  router.replace({
    path: route.path,
    query,
  });
};

// 处理好友选择
const handleFriendSelect = async (friend: any) => {
  console.log('选择好友:', friend);
  
  // ✅ 进入聊天页面时，清除该会话的未读数
  try {
    // ✅ 获取当前登录用户 ID
    const userStore = useUserStore();
    const currentUserId = userStore.userInfo?.id || 0;
    const friendId = friend.assistantId || friend.id;
    
    // ✅ 计算 sessionId（小的 ID 在前，字符串类型）
    let sessionId: string | null = null;
    
    if (currentUserId && friendId) {
      const id1 = Math.min(currentUserId, friendId);
      const id2 = Math.max(currentUserId, friendId);
      sessionId = `${id1}_${id2}`;
      
      console.log(`🔢 当前用户 ID: ${currentUserId}, 好友 ID: ${friendId}`);
      console.log(`🔢 计算 sessionId: ${id1}_${id2} = "${sessionId}"`);
    }
    
    // 如果有 sessionId，调用标记为已读接口
    if (sessionId) {
      console.log(`🔔 尝试标记会话 ${sessionId} 为已读`);
      
      // ✅ 使用 fetch 直接调用，避免 OpenAPI 生成的参数类型问题
      try {
        const response = await fetch(`/api/private-chat/sessions/${sessionId}/read/all`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include', // 携带 cookie
        });
        
        const result = await response.json();
        
        if (result.code === 0) {
          console.log(`✅ 成功清除会话 ${sessionId} 的未读消息`);
          
          // ✅ 立即刷新好友列表，确保数据同步
          await fetchFriends();
          
          // 重新获取所有未读数，更新红点
          await loadAllUnreadCounts();
        } else {
          console.error(`❌ 标记会话已读失败，响应码：${result.code}`);
        }
      } catch (error) {
        console.error(`❌ 标记会话已读异常:`, error);
      }
      
      // 重新获取所有未读数，更新红点
      await loadAllUnreadCounts();
    } else {
      console.error('❌ 无法计算 sessionId:', { currentUserId, friendId });
    }
  } catch (error) {
    console.error('❌ 清除未读消息失败:', error);
    // 不影响跳转，继续执行
  }
  
  // 跳转到聊天详情页
  router.push(`/userchat/${friend.assistantId}`);
};

// 处理对话选择
const handleChatSelect = async (messageId: string, assistantId: number) => {
  console.log('选择对话:', { messageId, assistantId });
  
  // ✅ 进入聊天页面时，清除该会话的未读数
  try {
    if (messageId) {
      // 调用标记为已读接口
      await ChatControllerService.markSessionMessagesAsReadUsingPost(Number(messageId));
      console.log(`已清除会话 ${messageId} 的未读消息`);
      
      // 重新获取所有未读数，更新红点
      await loadAllUnreadCounts();
    }
  } catch (error) {
    console.error('清除未读消息失败:', error);
    // 不影响跳转，继续执行
  }
  
  router.push(`/chat/detail/${assistantId}?sessionId=${messageId}`);
};

// 处理智能助手选择
const handleAssistantSelect = (assistantId: number) => {
  router.push(`/chat/detail/${assistantId}`);
};
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  padding-bottom: 130px; /* 减小底部间距，避免过多空白 */
  background-color: #f2f7fd;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden; /* 防止水平滚动条出现 */
}

.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background-color: #f2f7fd;
  padding: 16px 16px 0;
}

.scrollable-content {
  flex: 1;
  overflow-y: auto;
  padding: 0 16px;
  margin-top: 150px; /* 顶部边距，确保内容不会与导航栏重叠 */
  padding-bottom: 100px; /* 增加底部内边距，确保分页组件有足够空间 */
  position: relative;
  will-change: transform; /* 优化滚动性能 */
  overflow-x: hidden; /* 防止水平滚动条 */
  height: calc(100vh - 150px); /* 设置固定高度 */
  box-sizing: border-box;
}

.tab-content {
  min-height: 300px; /* 调整为更合理的高度 */
  position: relative;
  display: flex; 
  flex-direction: column;
}

.tab-pane {
  width: 100%;
  position: relative;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding: 12px 12px;
}

.page-title {
  display: flex;
  align-items: center;
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: #323233;
  font-family: 'Noto Sans SC', sans-serif;
}

.title-icon {
  margin-right: 6px;
  color: #1989fa;
  font-size: var(--font-size-xl);
}

.header-actions {
  display: flex;
  align-items: center;
}

.notification-badge {
  cursor: pointer;
  transition: all 0.3s;
}

.notification-badge:active {
  transform: scale(0.95);
}

.action-icon {
  font-size: 24px;
  color: #323233;
  margin-left: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  opacity: 0.85;
}

.action-icon:active {
  opacity: 0.6;
  transform: scale(0.95);
}

.nav-tabs {
  display: flex;
  margin-top: 0;
  margin-bottom: 16px;
  border-bottom: 1px solid #ebedf0;
  background-color: transparent;
  border-radius: 0;
  box-shadow: none;
  overflow: hidden;
}

.nav-tab {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-size: var(--font-size-md);
  font-weight: 500;
  color: #969799;
  position: relative;
  cursor: pointer;
}

.nav-badge {
  position: absolute;
  top: 5px;
  right: 15%;
  transform: translateX(50%);
}

.nav-tab.active {
  color: #1989fa;
  background-color: transparent;
}

.nav-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40%;
  height: 3px;
  background-color: #1989fa;
  border-radius: 3px 3px 0 0;
}

.new-chat-btn {
  position: fixed;
  right: 16px;
  bottom: 130px; /* 将按钮显示在分页组件上方 */
  z-index: 99;
}

.content-container {
  width: 100%;
  position: relative;
  min-height: calc(100vh - 250px);
  padding-bottom: 40px;
}

.content-wrapper {
  position: relative;
  min-height: 200px;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 0 4px;
  box-sizing: border-box;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  margin-top: 20px;
}

.loading-container p {
  margin-top: 12px;
  color: #666;
  font-size: var(--font-size-md);
  font-family: 'Noto Sans SC', sans-serif;
}

.empty-container {
  padding: 40px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-top: 20px;
}

.chat-container {
  min-height: 100vh;
  padding-bottom: 130px;
  overflow-x: hidden;
  background:
    radial-gradient(circle at 18% 8%, rgba(139, 92, 246, 0.28), transparent 32%),
    radial-gradient(circle at 86% 18%, rgba(96, 165, 250, 0.22), transparent 30%),
    radial-gradient(circle at 52% 48%, rgba(16, 185, 129, 0.11), transparent 34%),
    linear-gradient(160deg, #f3e8ff 0%, #eef2ff 46%, #dbeafe 100%);
}

.fixed-header {
  overflow: hidden;
  background:
    radial-gradient(circle at 22% 0%, rgba(139, 92, 246, 0.26), transparent 32%),
    linear-gradient(180deg, rgba(243, 232, 255, 0.92), rgba(238, 242, 255, 0.76));
  border: 1px solid rgba(255, 255, 255, 0.38);
  border-top: 0;
  backdrop-filter: blur(20px);
  box-shadow: 0 12px 36px rgba(79, 70, 229, 0.08);
}

.ambient {
  position: absolute;
  z-index: 0;
  pointer-events: none;
  border-radius: 50%;
  filter: blur(18px);
}

.ambient-one {
  top: -44px;
  left: 12%;
  width: 128px;
  height: 128px;
  background: rgba(139, 92, 246, 0.18);
}

.ambient-two {
  right: -26px;
  bottom: 8px;
  width: 118px;
  height: 118px;
  background: rgba(96, 165, 250, 0.18);
}

.header,
.nav-tabs,
.cat-prompt {
  position: relative;
  z-index: 1;
}

.header {
  gap: 12px;
  margin-top: 8px;
  padding: 8px 0 10px;
}

.header-avatar {
  flex: 0 0 auto;
  border: 2px solid rgba(255, 255, 255, 0.78);
  box-shadow: 0 10px 24px rgba(79, 70, 229, 0.12);
}

.page-title {
  flex: 1;
  min-width: 0;
  color: #1e293b;
}

.page-title > span:not(.chat-title-text),
.page-title .title-icon {
  display: none;
}

.chat-title-text {
  overflow: hidden;
  font-size: 20px;
  font-weight: 800;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-actions {
  flex: 0 0 auto;
  gap: 10px;
}

.action-icon {
  width: 38px;
  height: 38px;
  margin-left: 0;
  color: #4f46e5;
  background: rgba(255, 255, 255, 0.58);
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 999px;
  box-shadow: 0 8px 22px rgba(79, 70, 229, 0.12);
  backdrop-filter: blur(20px);
}

.nav-tabs {
  position: relative;
  gap: 8px;
  padding: 5px;
  margin: 0 0 12px;
  background: rgba(255, 255, 255, 0.42);
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-bottom: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 999px;
  box-shadow: 0 12px 30px rgba(79, 70, 229, 0.1);
  backdrop-filter: blur(20px);
}

.tab-glider {
  position: absolute;
  top: 5px;
  bottom: 5px;
  left: 5px;
  z-index: 0;
  width: calc((100% - 10px) / 3);
  border-radius: 999px;
  background: linear-gradient(135deg, #a5b4fc 0%, #8b5cf6 58%, #f0abfc 100%);
  box-shadow: 0 10px 24px rgba(79, 70, 229, 0.2);
  transition: transform 0.28s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.nav-tab {
  position: relative;
  z-index: 1;
  min-width: 0;
  padding: 10px 6px;
  overflow: hidden;
  color: transparent;
  font-size: 0;
  font-weight: 800;
  border-radius: 999px;
  transition: background 0.25s ease, transform 0.25s ease, box-shadow 0.25s ease;
}

.nav-tab::before {
  color: #64748b;
  font-size: 13px;
  content: attr(data-label);
}

.nav-tab.active {
  background: transparent;
  box-shadow: none;
  transform: translateY(-1px);
}

.nav-tab.active::before {
  color: #fff;
}

.nav-tab.active::after {
  display: none;
}

.nav-badge {
  top: 3px;
  right: 12%;
}

.cat-prompt {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  margin-bottom: 14px;
  background: rgba(255, 255, 255, 0.56);
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 20px;
  box-shadow: 0 14px 34px rgba(79, 70, 229, 0.1);
  backdrop-filter: blur(20px);
}

.cat-prompt-img {
  width: 58px;
  height: 58px;
  object-fit: contain;
  opacity: 0.94;
  animation: catFloat 3.6s ease-in-out infinite;
  filter: drop-shadow(0 10px 18px rgba(79, 70, 229, 0.16));
}

.cat-prompt-copy {
  min-width: 0;
}

.cat-prompt-copy span {
  display: block;
  color: #1e293b;
  font-size: 15px;
  font-weight: 800;
  line-height: 1.3;
}

.cat-prompt-copy small {
  display: -webkit-box;
  margin-top: 4px;
  overflow: hidden;
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  line-clamp: 1;
}

.scrollable-content {
  height: calc(100vh - 154px);
  padding: 0 16px 108px;
  margin-top: 154px;
}

.tab-pane {
  animation: tabFade 0.25s ease both;
}

.new-chat-btn {
  right: 18px;
  bottom: 84px;
  height: 46px;
  padding: 0 18px;
  color: #fff;
  background: linear-gradient(135deg, #60a5fa, #4f46e5 52%, #8b5cf6);
  border: 0;
  box-shadow: 0 8px 30px rgba(79, 70, 229, 0.4);
}

.loading-container,
.empty-container {
  padding: 28px 0;
  margin-top: 0;
  background: rgba(255, 255, 255, 0.56);
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 20px;
  box-shadow: 0 14px 34px rgba(79, 70, 229, 0.1);
  backdrop-filter: blur(20px);
}

.loading-container p {
  color: #64748b;
}

.cat-empty img {
  width: 104px;
  height: 104px;
  object-fit: contain;
  opacity: 0.9;
}

.cat-empty p {
  max-width: 260px;
  margin: 8px auto 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.5;
  text-align: center;
}

.cat-empty :deep(.van-empty) {
  display: none;
}

:deep(.van-button__text) {
  font-weight: 800;
}

@keyframes catFloat {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-5px);
  }
}

@keyframes tabFade {
  from {
    opacity: 0;
    transform: translateY(8px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (min-width: 769px) {
  .chat-container {
    border-right: 1px solid rgba(255, 255, 255, 0.36);
    border-left: 1px solid rgba(255, 255, 255, 0.36);
  }

  .fixed-header {
    left: 50%;
    right: auto;
    width: min(1100px, 100vw);
    transform: translateX(-50%);
  }

  .scrollable-content {
    width: 100%;
    max-width: 1100px;
    margin-right: auto;
    margin-left: auto;
  }

  .content-container,
  .content-wrapper {
    max-width: 960px;
  }
}

/* Final layout pass: keep header, tabs, list and pagination in one calm desktop/mobile flow. */
.chat-container {
  width: 100%;
  max-width: 1100px;
  min-height: calc(100vh - 96px);
  padding: 18px 18px 124px !important;
  margin: 0 auto;
  overflow-x: hidden;
  background:
    radial-gradient(circle at 14% 10%, rgba(196, 181, 253, 0.34), transparent 28%),
    radial-gradient(circle at 88% 16%, rgba(147, 197, 253, 0.28), transparent 26%),
    radial-gradient(circle at 52% 88%, rgba(251, 207, 232, 0.28), transparent 28%),
    linear-gradient(145deg, #f8fafc 0%, #eef2ff 48%, #fdf2f8 100%);
}

.fixed-header {
  position: relative;
  top: auto;
  right: auto;
  left: auto;
  width: 100%;
  padding: 18px 18px 16px;
  margin: 0 auto 14px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.45);
  border: 0;
  border-radius: 28px;
  box-shadow: 0 18px 44px rgba(120, 130, 180, 0.12);
  backdrop-filter: blur(18px);
  transform: none;
  box-sizing: border-box;
}

.header {
  min-height: 54px;
  padding: 0;
  margin: 0;
}

.chat-title-text {
  font-size: 22px;
  letter-spacing: 0;
}

.header-actions {
  margin-left: auto;
}

.action-icon {
  width: 42px;
  height: 42px;
  color: #4f46e5;
  background: rgba(255, 255, 255, 0.58);
  border: 1px solid rgba(255, 255, 255, 0.38);
  border-radius: 50%;
  box-shadow: 0 10px 28px rgba(120, 130, 180, 0.14);
  backdrop-filter: blur(16px);
}

.scrollable-content {
  display: flex;
  flex: 1;
  width: 100%;
  height: auto;
  min-height: 0;
  padding: 0;
  margin: 0;
  overflow: visible;
  box-sizing: border-box;
}

.chat-workspace {
  position: relative;
  display: flex;
  flex: 1;
  flex-direction: column;
  width: 100%;
  min-height: clamp(430px, calc(100vh - 260px), 760px);
  padding: 16px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.52);
  border: 1px solid rgba(255, 255, 255, 0.32);
  border-radius: 28px;
  box-shadow: 0 18px 46px rgba(120, 130, 180, 0.13);
  backdrop-filter: blur(20px);
}

.chat-workspace::before {
  position: absolute;
  top: -110px;
  right: 16%;
  width: 280px;
  height: 280px;
  pointer-events: none;
  content: '';
  background: radial-gradient(circle, rgba(196, 181, 253, 0.28), transparent 66%);
  filter: blur(8px);
}

.nav-tabs {
  position: relative;
  z-index: 1;
  flex-shrink: 0;
  gap: 8px;
  padding: 6px;
  margin: 0 auto 16px;
  width: min(520px, 100%);
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.36);
  border-radius: 999px;
  box-shadow: 0 12px 30px rgba(120, 130, 180, 0.12);
  backdrop-filter: blur(18px);
}

.tab-glider {
  top: 6px;
  bottom: 6px;
  left: 6px;
  width: calc((100% - 12px) / 3);
  background: linear-gradient(135deg, #93c5fd, #6366f1 56%, #c4b5fd);
  box-shadow: 0 10px 24px rgba(99, 102, 241, 0.22);
  transition: transform 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.nav-tab {
  padding: 11px 8px;
  color: transparent;
  border-radius: 999px;
}

.nav-tab::before {
  color: #475569;
  font-size: 14px;
  font-weight: 800;
}

.nav-tab.active {
  transform: none;
}

.nav-tab.active::before {
  color: #fff;
}

.tab-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.tab-pane {
  flex: 1;
  min-height: 0;
}

.content-container {
  width: 100%;
  min-height: 0;
  padding-bottom: 0;
}

.content-wrapper {
  width: min(90%, 900px);
  max-width: none;
  min-height: 0;
  padding: 0;
  margin: 0 auto;
}

.chat-workspace :deep(.chat-history-content),
.chat-workspace :deep(.history-container) {
  width: 100%;
  max-width: none;
  min-height: 0;
  padding: 0;
}

.chat-workspace :deep(.chat-list) {
  width: min(90%, 920px);
  padding: 0;
  margin: 0 auto;
}

.chat-workspace :deep(.chat-item) {
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid rgba(255, 255, 255, 0.34);
  border-radius: 22px;
  box-shadow: 0 10px 30px rgba(120, 130, 180, 0.12);
  backdrop-filter: blur(14px);
}

.chat-workspace :deep(.fixed-pagination) {
  width: min(90%, 920px);
  margin-right: auto;
  margin-left: auto;
}

.new-chat-btn {
  right: max(24px, calc((100vw - 1100px) / 2 + 24px));
  bottom: 112px;
  height: 46px;
  padding: 0 20px;
  color: #fff;
  background: linear-gradient(135deg, #93c5fd, #6366f1 56%, #c4b5fd) !important;
  border: 0;
  border-radius: 999px;
  box-shadow: 0 14px 32px rgba(99, 102, 241, 0.3);
}

@media (hover: hover) {
  .new-chat-btn:hover {
    transform: translateY(-3px);
  }
}

@media (min-width: 769px) {
  .fixed-header {
    left: auto;
    width: 100%;
    transform: none;
  }

  .scrollable-content {
    max-width: none;
  }
}

@media (max-width: 768px) {
  .chat-container {
    min-height: calc(100vh - 56px);
    padding: 12px 12px calc(112px + env(safe-area-inset-bottom)) !important;
  }

  .fixed-header {
    padding: 14px;
    margin-bottom: 10px;
    border-radius: 24px;
  }

  .header-avatar {
    width: 42px !important;
    height: 42px !important;
  }

  .chat-title-text {
    font-size: 19px;
  }

  .chat-workspace {
    min-height: calc(100vh - 210px);
    padding: 12px;
    border-radius: 24px;
  }

  .nav-tabs {
    margin-bottom: 12px;
  }

  .nav-tab::before {
    font-size: 13px;
  }

  .content-wrapper,
  .chat-workspace :deep(.chat-list),
  .chat-workspace :deep(.fixed-pagination) {
    width: 100%;
  }

  .new-chat-btn {
    right: 18px;
    bottom: calc(92px + env(safe-area-inset-bottom));
  }
}
</style>
