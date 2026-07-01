<template>
  <div class="user-chat-detail">
    <!-- 头部导航 -->
    <chat-header :title="targetUser.name || '学习伙伴'" :custom-back-path="'/chat'" />

    <!-- 消息列表区域 -->
    <div class="message-container">
      <friend-message-list
        :messages="messages"
        :assistant-avatar="targetUser.avatar"
        :user-avatar="userInfo?.avatar || ''"
        :loading="isSending"
        @update-read-status="handleReadStatusUpdate"
      />
      
      <!-- SSE 连接状态调试信息 -->
      <div v-if="false" class="debug-panel">
        <p>连接模式：轮询模式（已禁用 SSE）</p>
        <p>轮询间隔：{{ pollingInterval ? POLLING_INTERVAL + 'ms' : '已停止' }}</p>
        <p>会话 ID: {{ sessionId }}</p>
        <div class="debug-buttons">
          <button @click="togglePolling">{{ pollingInterval ? '停止轮询' : '启动轮询' }}</button>
          <button @click="checkMessageReadStatus">检查已读状态</button>
        </div>
      </div>
    </div>

    <!-- 底部输入框 -->
    <chat-input-area
      v-model="inputMessage"
      :is-loading="isSending"
      @send="sendMessage"
      @emoji="showEmojiPicker = true"
      @image="uploadImage"
      @voice="startVoiceRecord"
      @fullscreen="showFullscreenInput = true"
    />

    <!-- 表情选择器 -->
    <emoji-picker 
      v-model:show="showEmojiPicker" 
      @select="selectEmoji" 
    />

    <!-- 全屏输入框 -->
    <fullscreen-input
      v-model:show="showFullscreenInput"
      v-model="inputMessage"
      :is-loading="isSending"
      @send="sendFullscreenMessage"
      @emoji="showEmojiPicker = true"
      @image="uploadImage"
      @voice="startVoiceRecord"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast } from 'vant';
import { 
  FriendMessageList, 
  ChatHeader, 
  EmojiPicker, 
  FullscreenInput, 
  ChatInputArea
} from '../../components/Dialogue';
import { useUserStore } from '../../stores/userStore';
import type { UserInfo, Message } from '../../components/Dialogue/ChatMessageHandler';
import { ChatControllerService } from '../../services/services/ChatControllerService';
// 移除 SSE 相关的 import（已禁用）
// import { fetchEventSource } from '@microsoft/fetch-event-source';
import { OpenAPI } from '../../services/core/OpenAPI.ts';

// 生成UUID函数
function generateUUID(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const inputMessage = ref('');
const showEmojiPicker = ref(false);
const showFullscreenInput = ref(false);
const isSending = ref(false);
const currentPage = ref(1);
const pageSize = ref(20);
const hasMoreMessages = ref(true);
const loadingUserInfo = ref(false);
const DEFAULT_USER_AVATAR = userStore.DEFAULT_USER_AVATAR;
const lastMessageTime = ref<string | null>(null);

// 开发环境标志
const isDevelopment = process.env.NODE_ENV === 'development';

// SSE 连接相关变量（已禁用）
// let sseEventSource: AbortController | null = null;
const sseConnected = ref(false); // 保留变量但不再使用

// 消息列表
const messages = ref<Message[]>([]);

// 用户信息
const userInfo = ref<UserInfo>({
  id: 0,
  name: '',
  avatar: '',
});

// 目标用户（对话对象）信息
const targetUser = ref<UserInfo>({
  id: Number(route.params.userId) || 0,
  name: '',
  avatar: '',
});

// 会话ID
const sessionId = ref<number | null>(null);

// 在组件挂载时设置定时检查已读状态
let readStatusCheckInterval: number | null = null;
let healthCheckInterval: number | null = null;

// 手动重连 SSE（已禁用）
// const reconnectSSE = () => {
//   stopSSEConnection();
//   setTimeout(() => {
//     startSSEConnection();
//   }, 100);
// };

// 切换轮询状态
const togglePolling = () => {
  if (pollingInterval) {
    stopPolling();
  } else {
    startPolling();
  }
};

// 替换滚动到底部方法
const scrollToBottom = () => {
  setTimeout(() => {
    const messageList = document.querySelector('.message-list');
    if (messageList instanceof HTMLElement) {
      messageList.scrollTop = messageList.scrollHeight;
    }
  }, 100);
};

/**
 * 初始化对话
 */
const initializeChat = async () => {
  // 更新目标用户 ID
  targetUser.value.id = Number(route.params.userId) || 0;
  
  if (!targetUser.value.id) {
    showToast('无效的用户 ID');
    return;
  }
  
  // 先设置一个临时名称，避免页面显示空白
  targetUser.value.name = `用户${targetUser.value.id}`;
  targetUser.value.avatar = DEFAULT_USER_AVATAR;
  
  // 加载历史消息 (同时会获取对方用户信息)
  try {
    await loadChatHistory();
  } catch (error) {
    // 错误已在函数内部处理
  }
  
  // ✅ 进入聊天页面时，清除该会话的未读数
  if (sessionId.value) {
    try {
      console.log(`🔔 进入聊天页面，标记会话 ${sessionId.value} 为已读`);
      await ChatControllerService.markSessionMessagesAsReadUsingPost(sessionId.value);
      console.log(`✅ 已清除会话 ${sessionId.value} 的未读消息`);
    } catch (error) {
      console.error('❌ 标记会话已读失败:', error);
      // 不影响聊天页面正常使用
    }
  }
  
  // ✅ 使用轮询方式，不再使用 SSE（后端没有 SSE 接口）
  console.log('使用轮询方式获取新消息（SSE 已禁用）');
  startPolling();
};

// 播放消息提示音
const playMessageTone = () => {
  try {
    // 使用在线音频文件（如果本地文件不存在）
    const audio = new Audio('https://www.soundjay.com/button/beep-07.wav');
    audio.volume = 0.5;
    audio.play().catch(e => console.log('无法播放提示音:', e));
  } catch (e) {
    console.log('提示音播放失败:', e);
  }
};

// 显示桌面通知
const showDesktopNotification = (messageContent: string) => {
  // 检查浏览器是否支持 Notification API
  if (!('Notification' in window)) {
    console.log('浏览器不支持桌面通知');
    return;
  }
  
  // 检查用户是否授权
  if (Notification.permission === 'granted') {
    // 已授权，直接显示通知
    new Notification('新消息', {
      body: messageContent.length > 50 ? messageContent.substring(0, 50) + '...' : messageContent,
      icon: targetUser.value.avatar || '/avatar-default.png',
      tag: 'new-message', // 使用相同的 tag 避免重复通知
      requireInteraction: false, // 不强制用户交互
      silent: true, // 不播放声音（我们自己控制）
    });
  } else if (Notification.permission !== 'denied') {
    // 未授权但未被拒绝，请求授权
    Notification.requestPermission().then(permission => {
      if (permission === 'granted') {
        new Notification('新消息', {
          body: messageContent.length > 50 ? messageContent.substring(0, 50) + '...' : messageContent,
          icon: targetUser.value.avatar || '/avatar-default.png',
          tag: 'new-message',
          requireInteraction: false,
          silent: true,
        });
      }
    });
  }
};

// 更新页面标题（显示新消息提示）
const updateTitleWithNewMessage = (showNotification: boolean = true) => {
  if (showNotification) {
    document.title = '(新消息) AI 赋能的教育系统';
    
    // 3 秒后恢复原标题
    setTimeout(() => {
      document.title = 'AI 赋能的教育系统';
    }, 3000);
  } else {
    document.title = 'AI 赋能的教育系统';
  }
};

// 震动提示（移动端）
const vibrateDevice = () => {
  if ('vibrate' in navigator) {
    try {
      navigator.vibrate([200, 100, 200]); // 震动模式：震 200ms，停 100ms，再震 200ms
    } catch (e) {
      console.log('设备不支持震动:', e);
    }
  }
};

// 标记消息为已读时添加验证
const markMessageAsRead = async (messageId?: number) => {
  // 如果没有提供有效的消息ID，直接返回
  if (!messageId || typeof messageId !== 'number' || !sessionId.value) {
    console.error('无效的消息ID或会话ID', { messageId, sessionId: sessionId.value });
    return;
  }
  
  try {
    // 验证这是对方发送的消息，只有对方消息才能被标记为已读
    const message = messages.value.find(msg => msg.id === messageId);
    if (!message || message.type !== 'ai') {
      console.log(`尝试标记自己的消息为已读，被拒绝: ${messageId}`);
      return;
    }
    
    console.log(`尝试标记消息 ${messageId} 为已读，会话ID: ${sessionId.value}`);
    const response = await ChatControllerService.markMessageAsReadUsingPost(messageId, sessionId.value.toString());
    
    if (response && response.code === 0) {
      console.log(`成功标记消息 ${messageId} 为已读`);
      // 更新本地已读状态
      updateMessageReadStatus(messageId, 1);
    } else {
      console.error('标记消息已读API返回错误:', response);
    }
  } catch (error) {
    // 记录详细错误
    console.error('标记消息已读失败:', error);
  }
};

// 批量标记消息为已读
const markMessagesAsRead = async (messageIds: number[]) => {
  if (!messageIds || messageIds.length === 0 || !sessionId.value) {
    console.error('无效的消息ID数组或会话ID', { messageIds, sessionId: sessionId.value });
    return;
  }
  
  try {
    console.log(`尝试批量标记消息为已读: ${messageIds.join(',')}, 会话ID: ${sessionId.value}`);
    const response = await ChatControllerService.markMessagesAsReadUsingPost(messageIds, sessionId.value.toString());
    
    if (response && response.code === 0) {
      console.log(`成功批量标记消息为已读: ${messageIds.join(',')}`);
      // 更新本地已读状态
      updateMessagesReadStatus(messageIds, 1);
    } else {
      console.error('批量标记消息已读API返回错误:', response);
    }
  } catch (error) {
    console.error('批量标记消息已读失败:', error);
  }
};

// 优化检查消息已读状态函数，增加服务器验证
const checkMessageReadStatus = async () => {
  if (!sessionId.value || messages.value.length === 0) return;
  
  // 获取当前未读的用户消息ID
  const unreadUserMessageIds = messages.value
    .filter(msg => msg.type === 'user' && msg.isRead === 0)
    .map(msg => msg.id);
  
  if (unreadUserMessageIds.length === 0) return;
  
  console.log(`主动检查以下消息的已读状态: ${unreadUserMessageIds.join(',')}`);
  
  try {
    // 从服务器获取最新消息状态
    const response = await ChatControllerService.listSessionMessagesUsingGet(
      sessionId.value,
      1,
      50 // 获取更多消息以确保覆盖未读消息
    );
    
    if (response.code === 0 && response.data && response.data.records) {
      // 检查这些消息在服务器上是否已读
      for (const localMsgId of unreadUserMessageIds) {
        const serverMsg = response.data.records.find(msg => msg.id === localMsgId);
        
        // 只有服务器消息存在且已读时才更新本地状态 - 这是关键条件
        if (serverMsg && serverMsg.isRead === 1) {
          console.log(`服务器确认消息已读: ${localMsgId}`);
          updateMessageReadStatus(localMsgId, 1);
        }
      }
    }
  } catch (error) {
    console.error('主动检查消息已读状态失败:', error);
  }
};

// 增强更新消息已读状态函数，确保UI更新，并添加额外验证
const updateMessageReadStatus = (messageId: number, isRead: number) => {
  const messageIndex = messages.value.findIndex(msg => msg.id === messageId);
  if (messageIndex !== -1) {
    console.log(`更新消息 ${messageId} 已读状态为: ${isRead}`);
    
    // 安全地获取消息对象
    const oldMessage = messages.value[messageIndex];
    if (!oldMessage) return;
    
    // 如果状态已经是最新的，不重复更新
    if (oldMessage.isRead === isRead) return;
    
    // 执行用户消息到已读状态的额外验证 - 确保发送方消息只有在服务器确认已读时才显示已读
    if (oldMessage.type === 'user' && isRead === 1) {
      console.log(`用户消息 ${messageId} 设置为已读，当前状态: ${oldMessage.isRead}`);
      // 可以添加额外的服务器验证逻辑
    }
    
    // 创建新消息对象，保留所有原始字段并更新isRead
    const updatedMessage: Message = {
      ...oldMessage,
      isRead: isRead
    };
    
    // 更新消息数组，强制Vue重新渲染
    const newMessages = [...messages.value];
    newMessages[messageIndex] = updatedMessage;
    messages.value = newMessages;
    
    // 通知UI更新已读状态 (使用nextTick确保在下一个渲染周期更新)
    nextTick(() => {
      const messageElements = document.querySelectorAll('.message-item.user');
      if (messageElements && messageElements.length > 0) {
        console.log('强制更新消息已读状态UI');
      }
    });
  }
};

// 增强批量更新消息已读状态函数
const updateMessagesReadStatus = (messageIds: number[], isRead: number) => {
  if (!messageIds || messageIds.length === 0) return;
  
  console.log(`批量更新消息已读状态: ${messageIds.join(',')} -> ${isRead}`);
  
  let hasUpdates = false;
  messageIds.forEach(id => {
    const index = messages.value.findIndex(msg => msg.id === id);
    if (index !== -1 && messages.value[index] && messages.value[index].isRead !== isRead) {
      hasUpdates = true;
    }
    updateMessageReadStatus(id, isRead);
  });
  
  // 如果有更新，强制刷新界面
  if (hasUpdates) {
    nextTick(() => {
      console.log('批量更新后强制刷新界面');
    });
  }
};

// 标记整个会话已读
const markSessionAsRead = async () => {
  if (!sessionId.value) {
    console.error('无法标记会话已读：会话ID不存在');
    return;
  }
  
  try {
    console.log(`尝试标记会话 ${sessionId.value} 中的所有消息为已读`);
    const response = await ChatControllerService.markSessionMessagesAsReadUsingPost(sessionId.value);
    
    if (response && response.code === 0) {
      console.log(`成功标记会话 ${sessionId.value} 中的所有消息为已读`);
      
      // 更新本地消息的已读状态
      const targetUserMessageIds = messages.value
        .filter(msg => msg.type === 'ai' && msg.isRead === 0)
        .map(msg => msg.id);
      
      if (targetUserMessageIds.length > 0) {
        updateMessagesReadStatus(targetUserMessageIds, 1);
      }
    } else {
      console.error('标记会话所有消息已读API返回错误:', response);
    }
  } catch (error) {
    console.error('标记会话所有消息已读失败:', error);
  }
};

// 加载历史消息
const loadChatHistory = async () => {
  if (!targetUser.value.id) return;

  try {
    // ✅ 不需要预先获取会话，直接获取消息列表
    // 后端会在发送第一条消息时自动创建会话
    
    // 如果没有会话 ID，先尝试获取会话列表
    if (!sessionId.value) {
      try {
        const sessionsResponse = await ChatControllerService.listUserSessionsUsingGet();
        if (sessionsResponse.code === 0 && sessionsResponse.data) {
          console.log('📋 初始化时获取会话列表响应:', JSON.parse(JSON.stringify(sessionsResponse.data)));
          
          // ✅ 兼容后端返回的数据格式：可能是对象包含 list 数组，也可能是直接数组
          const sessionsList = Array.isArray(sessionsResponse.data) 
            ? sessionsResponse.data 
            : (sessionsResponse.data.list || sessionsResponse.data.records || []);
          
          console.log('📋 解析后的会话列表数量:', sessionsList.length);
          console.log('🎯 目标用户 ID:', targetUser.value.id);
          console.log('👤 当前用户 ID:', userInfo.value.id);
          
          if (sessionsList.length > 0) {
            console.log('📋 第一个会话的完整数据:', JSON.parse(JSON.stringify(sessionsList[0])));
          }
          
          // ✅ 查找与目标用户的会话 - 后端字段名是 user1Id/user2Id，值是字符串
          const session = sessionsList.find(
            (s: any) => {
              // ✅ 后端返回的字段名是 user1Id 和 user2Id，值是字符串类型（如 '5'、'7'）
              const userId1 = Number(s.user1Id || s.userId1 || 0);
              const userId2 = Number(s.user2Id || s.userId2 || 0);
              // ✅ 确保当前用户 ID 和目标用户 ID 也是数字类型
              const currentUserId = Number(userInfo.value.id);
              const targetUserId = Number(targetUser.value.id);
              
              console.log(`🔍 检查会话：user1Id=${s.user1Id}(${typeof s.user1Id}), user2Id=${s.user2Id}(${typeof s.user2Id})`);
              console.log(`   转换后：userId1=${userId1}, userId2=${userId2}`);
              console.log(`   当前用户：${currentUserId}(${typeof currentUserId}), 目标用户：${targetUserId}(${typeof targetUserId})`);
              console.log(`   原始数据字段：`, Object.keys(s));
              
              // ✅ 使用数字比较，兼容两种顺序
              const isMatch = (userId1 === currentUserId && userId2 === targetUserId) ||
                             (userId2 === currentUserId && userId1 === targetUserId);
              
              console.log(`   匹配结果：${isMatch}`);
              return isMatch;
            }
          );
          
          if (session) {
            sessionId.value = session.id || null;
            console.log('✅ 找到会话，sessionId:', sessionId.value);
            console.log('✅ 会话完整数据:', session);
          } else {
            console.log('❌ 未找到与目标用户的会话');
            console.log('❌ 会话列表中的所有会话:', sessionsList.map((s: any) => ({
              id: s.id,
              user1Id: s.user1Id,
              user2Id: s.user2Id,
              userId1: s.userId1,
              userId2: s.userId2,
            })));
          }
        }
      } catch (error) {
        console.log('获取会话列表失败，将在发送消息时自动创建会话');
      }
    }

    // 如果没有会话 ID，说明还没有历史消息，直接返回
    if (!sessionId.value) {
      console.log('暂无历史消息，等待发送第一条消息创建会话');
      return;
    }

    // 有了会话ID后，获取消息列表
    const response = await ChatControllerService.listSessionMessagesUsingGet(
      sessionId.value,
      currentPage.value,
      pageSize.value
    );

    if (response.code === 0 && response.data) {
      // 检查是否有更多消息
      hasMoreMessages.value = response.data.records && response.data.records.length >= pageSize.value ? true : false;
      
      // 转换消息格式
      const historyMessages: Message[] = [];
      
      response.data.records?.forEach(msg => {
        if (!msg) return;
        
        historyMessages.push({
          id: msg.id || Date.now(),
          type: msg.senderId === userInfo.value.id ? 'user' : 'ai', // 'ai'类型用于对方消息
          content: msg.content || '',
          timestamp: msg.createTime ? new Date(msg.createTime).getTime() : Date.now(),
          isRead: msg.isRead, // 添加已读状态
        });
        
        // 记录最后一条消息的时间
        if (msg.createTime) {
          lastMessageTime.value = msg.createTime;
        }
      });
      
      // 第一页时直接替换，否则添加到现有消息前面
      if (currentPage.value === 1) {
        messages.value = historyMessages.reverse(); // 逆序排列，最新的在最下面
      } else {
        messages.value = [...historyMessages.reverse(), ...messages.value];
      }
      
      // 替换原来的单条消息标记逻辑，直接标记整个会话已读
      const hasUnreadMessages = response.data.records?.some(msg => 
        msg && msg.isRead === 0 && msg.senderId === targetUser.value.id
      );
      
      if (hasUnreadMessages && sessionId.value) {
        // 标记整个会话为已读
        await markSessionAsRead();
      }
      
      // 滚动到底部
      scrollToBottom();
    }
  } catch (error) {
    showToast('获取历史消息失败');
    console.error('加载历史消息失败:', error);
  }
};

// 加载更多历史消息
const loadMoreHistory = async () => {
  if (!hasMoreMessages.value) return;
  
  currentPage.value++;
  await loadChatHistory();
};

// 停止 SSE 连接（已禁用）
// const stopSSEConnection = () => {
//   if (sseEventSource) {
//     sseEventSource.abort();
//     sseEventSource = null;
//   }
//   sseConnected.value = false;
// };

// 轮询相关变量
let pollingInterval: number | null = null;
const POLLING_INTERVAL = 5000; // 5 秒轮询一次

// ✅ SSE 连接函数已禁用，改用轮询方式（后端没有 SSE 接口）

// 修改轮询函数，也处理已读状态
const startPolling = () => {
  // 如果已经在轮询，则不重复启动
  if (pollingInterval !== null) return;
  
  console.log('启动消息轮询备选方案，间隔:', POLLING_INTERVAL, 'ms');
  
  // 设置定时器定期获取新消息
  pollingInterval = window.setInterval(async () => {
    if (!sessionId.value || !targetUser.value.id) {
      console.log('轮询暂停：缺少 sessionId 或 targetUser');
      return;
    }
    
    try {
      // 获取最新消息
      const response = await ChatControllerService.listSessionMessagesUsingGet(
        sessionId.value,
        1, // 只获取第一页
        20 // 获取更多消息以确保覆盖
      );
      
      if (response.code === 0 && response.data && response.data.records) {
        console.log('🔄 轮询获取到', response.data.records.length, '条消息');
        
        // 更新已读状态 - 检查用户发送的消息是否被标记为已读
        if (response.data.records) {
          // 获取本地未读的用户消息
          const localUnreadUserMessages = messages.value.filter(
            msg => msg.type === 'user' && msg.isRead === 0
          );
          
          if (localUnreadUserMessages.length > 0) {
            // 检查这些消息在服务器上是否已读
            for (const localMsg of localUnreadUserMessages) {
              const serverMsg = response.data.records.find(
                msg => msg.id === localMsg.id
              );
              
              // 如果服务器上消息已读，更新本地状态
              if (serverMsg && serverMsg.isRead === 1) {
                console.log(`轮询发现消息已读状态更新：${localMsg.id}`);
                updateMessageReadStatus(localMsg.id, 1);
              }
            }
          }
        }
        
        // 过滤出尚未显示的对方消息
        const newMessageRecords = response.data.records.filter(msg => 
          msg && 
          msg.senderId === targetUser.value.id && 
          !messages.value.some(existingMsg => existingMsg.id === msg.id)
        );
        
        console.log('📨 发现', newMessageRecords.length, '条新消息');
        
        // 转换为前端消息格式
        if (newMessageRecords.length > 0) {
          const newMessages: Message[] = newMessageRecords.map(msg => ({
            id: msg.id || Date.now(),
            type: 'ai', // 使用字面量类型
            content: msg.content || '',
            timestamp: msg.createTime ? new Date(msg.createTime).getTime() : Date.now(),
            isRead: 1, // 自动标记为已读
          }));
          
          // 按时间顺序添加新消息
          messages.value.push(...newMessages.sort((a, b) => a.timestamp - b.timestamp));
          
          console.log('✅ 新消息已添加到列表，当前消息总数:', messages.value.length);
          
          // 📢 新消息提示（多种方式）
          // 1. 播放提示音
          playMessageTone();
          
          // 2. 显示桌面通知（只在页面不可见时显示）
          if (document.hidden) {
            showDesktopNotification(newMessages[0].content);
          }
          
          // 3. 更新页面标题
          updateTitleWithNewMessage(true);
          
          // 4. 震动提示（移动端）
          if (!document.hidden) {
            vibrateDevice();
          }
          
          // 滚动到底部
          scrollToBottom();
          
          // 标记会话为已读，而不是单条消息
          if (sessionId.value) {
            try {
              console.log(`准备标记会话 ${sessionId.value} 为已读`);
              markSessionAsRead();
            } catch (error) {
              console.error('标记轮询消息为已读失败:', error);
            }
          }
        }
        
        // 检查会话未读消息数
        try {
          const unreadCountResponse = await ChatControllerService.getSessionUnreadCountUsingGet(sessionId.value);
          if (unreadCountResponse.code === 0 && unreadCountResponse.data === 0) {
            // 如果没有未读消息，可能所有消息都已读，更新本地状态
            const userMessageIds = messages.value
              .filter(msg => msg.type === 'user' && msg.isRead === 0)
              .map(msg => msg.id);
            
            if (userMessageIds.length > 0) {
              console.log('轮询发现会话所有消息已读，更新本地状态');
              updateMessagesReadStatus(userMessageIds, 1);
            }
          }
        } catch (error) {
          console.error('获取会话未读消息数失败:', error);
        }
      }
    } catch (error) {
      console.error('轮询获取消息失败:', error);
    }
  }, POLLING_INTERVAL);
};

// 停止轮询
const stopPolling = () => {
  if (pollingInterval !== null) {
    window.clearInterval(pollingInterval);
    pollingInterval = null;
    console.log('已停止消息轮询');
  }
};

// 发送消息
const sendMessage = async (text: string) => {
  if (!text.trim() || isSending.value) return;
  
  // 确保目标用户ID存在
  if (!targetUser.value.id) {
    showToast('无法发送消息：未指定接收用户');
    return;
  }
  
  // 生成临时消息ID
  const tempMessageId = Date.now();
  
  // 添加用户消息到列表
  const userMessage: Message = {
    id: tempMessageId,
    type: 'user',
    content: text,
    timestamp: Date.now(),
    isRead: 0, // 默认为未读状态
  };
  messages.value.push(userMessage);
  
  // 清空输入框
  inputMessage.value = '';
  
  // 设置发送状态
  isSending.value = true;
  
  try {
    // ✅ 记录发送时间
    const sendTime = new Date();
    
    // ✅ 使用正确的接口：/api/private-chat/send/text
    // ✅ 确保参数类型正确：receiverId 是数字，content 是字符串
    const receiverId = Number(targetUser.value.id);
    const content = String(text);
    
    console.log('🚀 开始发送消息:', { 
      receiverId, 
      receiverIdType: typeof receiverId, 
      content, 
      contentType: typeof content 
    });
    
    console.log('📡 调用 API，参数:', receiverId, content);
    
    const response = await ChatControllerService.sendTextMessageUsingPost(receiverId, content);
    
    console.log('✅ API 响应:', response);
    
    if (response.code === 0) {
      // ✅ 获取实际的消息 ID - 兼容后端返回的格式
      let realMessageId: number;
      if (typeof response.data === 'object') {
        // 如果返回的是对象，尝试获取 id 字段
        realMessageId = response.data.id || response.data.messageId || Date.now();
      } else {
        // 如果直接返回数字
        realMessageId = Number(response.data) || Date.now();
      }
          
      console.log(`消息发送成功，服务器返回 ID: ${realMessageId}，临时 ID: ${tempMessageId}`);
          
      // ✅ 不更新临时消息，保持显示
      // 临时消息已经显示在列表中，只需要确保它不会被移除
          
      // 更新最后消息时间
      if (sendTime > new Date(lastMessageTime.value || 0)) {
        lastMessageTime.value = sendTime.toISOString();
      }
      
      // 发送成功后，会话应该已经自动创建
      // 不需要再调用获取会话的接口
      // 如果 sessionId 不存在，尝试从会话列表中获取
      if (!sessionId.value) {
        console.log('⚠️ 消息发送成功但 sessionId 为空，开始获取会话列表');
        try {
          const sessionsResponse = await ChatControllerService.listUserSessionsUsingGet();
          if (sessionsResponse.code === 0 && sessionsResponse.data) {
            console.log('📋 发送消息后获取会话列表响应:', JSON.parse(JSON.stringify(sessionsResponse.data)));
            
            // ✅ 兼容后端返回的数据格式
            const sessionsList = Array.isArray(sessionsResponse.data) 
              ? sessionsResponse.data 
              : (sessionsResponse.data.list || sessionsResponse.data.records || []);
            
            console.log('📋 解析后的会话列表数量:', sessionsList.length);
            console.log('🎯 目标用户 ID:', targetUser.value.id);
            console.log('👤 当前用户 ID:', userInfo.value.id);
            
            if (sessionsList.length > 0) {
              console.log('📋 第一个会话的完整数据:', JSON.parse(JSON.stringify(sessionsList[0])));
              console.log('📋 第一个会话的字段名:', Object.keys(sessionsList[0]));
            }
            
            // ✅ 查找匹配的会话 - 后端字段名是 user1Id/user2Id，值是字符串
            const session = sessionsList.find(
              (s: any) => {
                // ✅ 后端返回的字段名是 user1Id 和 user2Id，值是字符串类型（如 '5'、'7'）
                const userId1 = Number(s.user1Id || s.userId1 || 0);
                const userId2 = Number(s.user2Id || s.userId2 || 0);
                // ✅ 确保当前用户 ID 和目标用户 ID 也是数字类型
                const currentUserId = Number(userInfo.value.id);
                const targetUserId = Number(targetUser.value.id);
                
                console.log(`🔍 检查会话：user1Id=${s.user1Id}(${typeof s.user1Id}), user2Id=${s.user2Id}(${typeof s.user2Id})`);
                console.log(`   转换后：userId1=${userId1}, userId2=${userId2}`);
                console.log(`   当前用户：${currentUserId}(${typeof currentUserId}), 目标用户：${targetUserId}(${typeof targetUserId})`);
                console.log(`   原始数据字段：`, Object.keys(s));
                
                // ✅ 使用数字比较，兼容两种顺序
                const isMatch = (userId1 === currentUserId && userId2 === targetUserId) ||
                               (userId2 === currentUserId && userId1 === targetUserId);
                
                console.log(`   匹配结果：${isMatch}`);
                return isMatch;
              }
            );
            if (session) {
              sessionId.value = session.id || null;
              console.log('✅ 发送消息后获取到会话 ID:', sessionId.value);
              console.log('✅ 会话完整数据:', session);
              
              // ✅ 重要：如果轮询已停止，重新启动轮询以获取对方回复
              if (!pollingInterval) {
                console.log('🔄 重新启动轮询以获取新消息');
                startPolling();
              }
            } else {
              console.log('❌ 发送消息后仍未找到会话');
              console.log('❌ 会话列表中的所有会话:', sessionsList.map((s: any) => ({
                id: s.id,
                user1Id: s.user1Id,
                user2Id: s.user2Id,
                userId1: s.userId1,
                userId2: s.userId2,
              })));
            }
          }
        } catch (error) {
          console.error('获取会话列表失败:', error);
        }
      }
      
      // 发送成功后，开始定期检查该消息的已读状态，但不要自动标记为已读
      const checkThisMessageInterval = setInterval(() => {
        // 检查消息是否存在且未读
        const sentMessage = messages.value.find(msg => msg.id === realMessageId);
        if (sentMessage && sentMessage.isRead === 0) {
          // 调用检查函数，但不自动标记为已读
          checkMessageReadStatus();
        } else {
          // 如果消息已标记为已读或不存在，停止检查
          clearInterval(checkThisMessageInterval);
        }
      }, 2000); // 每2秒检查一次
      
      // 30 秒后无论如何停止检查，避免无限检查
      setTimeout(() => {
        clearInterval(checkThisMessageInterval);
      }, 30000);
    } else {
      throw new Error(response.message || '未知错误');
    }
        
    // ✅ 已禁用 SSE，不需要检查连接
    // if (!sseConnected.value) {
    //   startSSEConnection();
    // }
          
    // 滚动到底部
    scrollToBottom();
  } catch (error) {
    console.error('❌ 消息发送过程出错:', error);
    showToast(`发送失败：${error instanceof Error ? error.message : '未知错误'}`);
    // 移除失败的消息
    const failedIndex = messages.value.findIndex(msg => msg.id === tempMessageId);
    if (failedIndex !== -1) {
      messages.value.splice(failedIndex, 1);
    }
  } finally {
    isSending.value = false;
  }
};

// 发送全屏输入框消息
const sendFullscreenMessage = () => {
  if (inputMessage.value.trim()) {
    sendMessage(inputMessage.value);
    showFullscreenInput.value = false;
  }
};

// 选择表情
const selectEmoji = (emoji: string): void => {
  inputMessage.value += emoji;
  showEmojiPicker.value = false;
};

// 上传图片
const uploadImage = (): void => {
  showToast('图片上传功能开发中');
};

// 开始语音录制
const startVoiceRecord = (): void => {
  showToast('语音录制功能开发中');
};

// 监听消息容器滚动，实现上拉加载更多历史消息
const setupScrollListener = () => {
  const messageList = document.querySelector('.message-list');
  if (!messageList) return;
  
  messageList.addEventListener('scroll', () => {
    if ((messageList as HTMLElement).scrollTop <= 50 && hasMoreMessages.value && !isSending.value) {
      loadMoreHistory();
    }
  });
};

// 监听输入框回车事件
const handleKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    sendMessage(inputMessage.value);
  }
};

// 点击发送按钮
const handleSend = () => {
  sendMessage(inputMessage.value);
};

// 发送表情符号
const handleEmojiSelect = (emoji: string) => {
  inputMessage.value += emoji;
  showEmojiPicker.value = false;
};

// 输入框获取焦点时
const handleInputFocus = () => {
  // 关闭表情选择器
  showEmojiPicker.value = false;
};

// 切换表情选择器
const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value;
};

// 切换全屏输入模式
const toggleFullscreenInput = () => {
  showFullscreenInput.value = !showFullscreenInput.value;
};

// 发送全屏输入框消息
const handleFullscreenSubmit = (text: string) => {
  showFullscreenInput.value = false;
  if (text.trim()) {
    sendMessage(text);
  }
};

// 添加处理已读状态更新的方法
const handleReadStatusUpdate = (messageId: number, isRead: number) => {
  console.log(`UI触发更新消息 ${messageId} 的已读状态为: ${isRead}`);
  if (isRead === 1) {
    // 如果消息变为已读，调用API标记为已读
    markMessageAsRead(messageId);
  }
};

// 在组件挂载时添加额外的状态检查
onMounted(async () => {
  // 设置当前用户信息
  if (userStore.userInfo) {
    userInfo.value = {
      id: userStore.userInfo.id || 0,
      name: userStore.userInfo.userName || '',
      avatar: userStore.userInfo.userAvatar || DEFAULT_USER_AVATAR,
    };
  } else {
    // 用户未登录，尝试重新获取用户信息
    try {
      const currentUser = await userStore.fetchCurrentUser();
      if (currentUser) {
        userInfo.value = {
          id: currentUser.id || 0,
          name: currentUser.userName || '',
          avatar: currentUser.userAvatar || DEFAULT_USER_AVATAR,
        };
      } else {
        showToast('请先登录');
        return;
      }
    } catch (error) {
      console.error('获取当前用户信息失败:', error);
      showToast('请先登录');
      return;
    }
  }
  
  console.log('组件挂载，初始化对话，当前用户ID:', userInfo.value.id);
  
  // 初始化对话
  await initializeChat();
  
  // 设置消息滚动监听
  setupScrollListener();
  
  // ✅ 已禁用 SSE，不再尝试连接
  console.log('SSE 已禁用，使用轮询方式');
  
  // 设置定时检查消息已读状态
  readStatusCheckInterval = window.setInterval(checkMessageReadStatus, 3000); // 每 3 秒检查一次
  
  // 初始化后立即检查一次
  setTimeout(checkMessageReadStatus, 1000);
});

// 当路由参数改变时重新初始化对话
watch(() => route.params.userId, async (newUserId) => {
  if (newUserId && Number(newUserId) !== targetUser.value.id) {
    targetUser.value.id = Number(newUserId);
    messages.value = [];
    currentPage.value = 1;
    lastMessageTime.value = null;
    sessionId.value = null; // 重置会话 ID
    // ✅ 已禁用 SSE，只需重新初始化
    await initializeChat();
  }
});

// 组件销毁前清理资源
onBeforeUnmount(() => {
  // ✅ 已禁用 SSE，不需要清理
  // stopSSEConnection();
  
  // 停止轮询
  stopPolling();
  
  // ✅ 已禁用 SSE，不需要断开连接
  // try {
  //   ChatControllerService.disconnectUsingPost();
  // } catch (error) {
  //   console.error('断开 SSE 连接失败:', error);
  // }
  
  // 清除定时检查已读状态的定时器
  if (readStatusCheckInterval !== null) {
    window.clearInterval(readStatusCheckInterval);
    readStatusCheckInterval = null;
  }
  
  // ✅ 已禁用 SSE，不需要清理健康检查
  // if (healthCheckInterval !== null) {
  //   window.clearInterval(healthCheckInterval);
  //   healthCheckInterval = null;
  // }
  
  // 恢复页面标题
  updateTitleWithNewMessage(false);
});

// 监听页面可见性变化，恢复标题
watch(() => document.hidden, (isHidden) => {
  if (!isHidden) {
    // 页面变为可见，恢复标题
    updateTitleWithNewMessage(false);
  }
});
</script>

<style scoped>
.user-chat-detail {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  position: relative;
  background:
    radial-gradient(circle at 20% 8%, rgba(108, 99, 255, 0.2), transparent 30%),
    radial-gradient(circle at 88% 18%, rgba(96, 165, 250, 0.16), transparent 28%),
    linear-gradient(135deg, #c9d6ff, #e2e2e2, #fbc2eb);
}

.user-chat-detail::before {
  position: absolute;
  inset: 0;
  pointer-events: none;
  content: '';
  background:
    radial-gradient(circle at 50% 36%, rgba(255, 255, 255, 0.28), transparent 32%),
    radial-gradient(circle at 12% 78%, rgba(108, 99, 255, 0.12), transparent 24%);
  filter: blur(10px);
}

.message-container {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  margin-top: 72px;
  margin-bottom: 142px;
  padding: 0;
  width: 100%;
  box-sizing: border-box;
}

/* 确保消息列表占据整个容器 */
:deep(.message-list) {
  flex: 1;
  padding: 18px 16px 60px;
  padding-bottom: 60px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  overflow-y: auto;
}

/* 调整收到的消息样式 */
:deep(.message-item.ai) {
  width: 100%;
  margin-right: 0;
}

:deep(.message-item.ai .message-content) {
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(20px);
}

/* 调试面板样式 */
.debug-panel {
  position: fixed;
  bottom: 130px;
  right: 10px;
  background-color: rgba(0, 0, 0, 0.7);
  color: #fff;
  border-radius: 8px;
  padding: 10px;
  font-size: 12px;
  z-index: 1000;
  max-width: 200px;
}

.debug-panel p {
  margin: 5px 0;
  word-break: break-all;
}

.debug-buttons {
  display: flex;
  gap: 5px;
  margin-top: 8px;
}

.debug-buttons button {
  background-color: #1989fa;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 5px 8px;
  font-size: 12px;
  cursor: pointer;
}

.debug-buttons button:active {
  background-color: #0e71d8;
}
</style> 
