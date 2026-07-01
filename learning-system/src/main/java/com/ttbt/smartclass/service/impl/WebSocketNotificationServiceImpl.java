package com.ttbt.smartclass.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ttbt.smartclass.constant.UserConstant;
import com.ttbt.smartclass.constant.WebSocketMessageType;
import com.ttbt.smartclass.event.UserStatusEvent;
import com.ttbt.smartclass.event.WebSocketMessageEvent;
import com.ttbt.smartclass.model.dto.websocket.WebSocketMessage;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.enums.UserRoleEnum;
import com.ttbt.smartclass.netty.ChannelManager;
import com.ttbt.smartclass.service.UserService;
import com.ttbt.smartclass.service.WebSocketNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WebSocket通知服务实现类
 * 用于实时推送系统通知和管理员监控功能
 */
@Service
@Slf4j
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private UserService userService;
    
    // 仅用于获取在线状态和用户信息，不直接发送消息
    @Autowired
    private ChannelManager channelManager;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 监听用户状态事件
     */
    @EventListener
    public void handleUserStatusEvent(UserStatusEvent event) {
        if (event.isOnline()) {
            handleUserOnline(event.getUserId());
        } else {
            handleUserOffline(event.getUserId());
        }
    }

    /**
     * 发送系统通知给指定用户
     */
    @Override
    public void sendSystemNotification(Long userId, String content, Object data) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType(WebSocketMessageType.SYSTEM);
        message.setContent(content);
        message.setReceiverId(userId);
        message.setTimestamp(System.currentTimeMillis());
        message.setData(data);
        
        eventPublisher.publishEvent(new WebSocketMessageEvent(userId, JSON.toJSONString(message)));
    }

    /**
     * 发送系统通知给所有用户
     */
    @Override
    public void broadcastSystemNotification(String content, Object data) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType(WebSocketMessageType.SYSTEM);
        message.setContent(content);
        message.setTimestamp(System.currentTimeMillis());
        message.setData(data);
        
        eventPublisher.publishEvent(new WebSocketMessageEvent(JSON.toJSONString(message), true));
    }

    /**
     * 发送系统通知给所有管理员
     */
    @Override
    public void notifyAdmins(String content, Object data) {
        try {
            // 查询所有管理员用户
            List<User> adminUsers = userService.getAllAdmins();
            
            WebSocketMessage message = new WebSocketMessage();
            message.setType(WebSocketMessageType.SYSTEM);
            message.setContent(content);
            message.setTimestamp(System.currentTimeMillis());
            message.setData(data);
            
            String messageJson = JSON.toJSONString(message);
            
            // 向在线管理员发送消息
            for (User admin : adminUsers) {
                if (channelManager.isUserOnline(admin.getId())) {
                    eventPublisher.publishEvent(new WebSocketMessageEvent(admin.getId(), messageJson));
                }
            }
        } catch (Exception e) {
            log.error("向管理员推送消息失败", e);
        }
    }

    /**
     * 向管理员推送聊天监控数据
     */
    @Override
    public void pushChatMonitorData(String sessionId, WebSocketMessage chatMessage) {
        try {
            // 查询所有管理员用户
            List<User> adminUsers = userService.getAllAdmins();
            
            // 创建监控消息
            WebSocketMessage monitorMessage = new WebSocketMessage();
            monitorMessage.setType("chat_monitor");
            monitorMessage.setSessionId(sessionId);
            monitorMessage.setTimestamp(System.currentTimeMillis());
            
            // 添加监控数据
            Map<String, Object> monitorData = new HashMap<>();
            monitorData.put("originalMessage", chatMessage);
            monitorData.put("sessionId", sessionId);
            monitorData.put("sender_id", chatMessage.getSenderId());
            monitorData.put("receiver_id", chatMessage.getReceiverId());
            monitorData.put("monitorTime", System.currentTimeMillis());
            
            monitorMessage.setData(monitorData);
            
            String messageJson = JSON.toJSONString(monitorMessage);
            
            // 向所有在线管理员推送监控数据
            for (User admin : adminUsers) {
                if (channelManager.isUserOnline(admin.getId())) {
                    eventPublisher.publishEvent(new WebSocketMessageEvent(admin.getId(), messageJson));
                }
            }
        } catch (Exception e) {
            log.error("推送聊天监控数据失败", e);
        }
    }

    /**
     * 定时推送在线用户状态给管理员
     * 每60秒执行一次
     */
    @Scheduled(fixedRate = 60000)
    public void pushOnlineUserStats() {
        try {
            // 获取在线用户信息
            int onlineCount = channelManager.getOnlineUserCount();
            List<Long> onlineUserIds = channelManager.getOnlineUserIds();
            
            Map<String, Object> statsData = new HashMap<>();
            statsData.put("onlineCount", onlineCount);
            statsData.put("onlineUsers", onlineUserIds);
            statsData.put("timestamp", System.currentTimeMillis());
            
            // 查询所有管理员
            List<User> adminUsers = userService.getAllAdmins();
            
            // 创建监控消息
            WebSocketMessage statsMessage = new WebSocketMessage();
            statsMessage.setType("online_stats");
            statsMessage.setContent("在线用户统计");
            statsMessage.setData(statsData);
            statsMessage.setTimestamp(System.currentTimeMillis());
            
            String messageJson = JSON.toJSONString(statsMessage);
            
            // 推送给所有在线管理员
            for (User admin : adminUsers) {
                if (channelManager.isUserOnline(admin.getId())) {
                    eventPublisher.publishEvent(new WebSocketMessageEvent(admin.getId(), messageJson));
                }
            }
        } catch (Exception e) {
            log.error("推送在线用户统计失败", e);
        }
    }
    
    /**
     * 用户上线处理
     */
    private void handleUserOnline(Long userId) {
        try {
            // 设置 Redis 在线状态
            redisTemplate.opsForValue().set("chat:online:" + userId, true);
            log.info("用户 {} 已上线", userId);
            
            // 获取用户信息
            User user = userService.getById(userId);
            if (user == null) {
                return;
            }
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("user_id", userId);
            userData.put("userName", user.getUserName());
            userData.put("onlineTime", System.currentTimeMillis());
            
            // 创建通知消息
            WebSocketMessage notification = new WebSocketMessage();
            notification.setType("user_online");
            notification.setContent("用户上线");
            notification.setData(userData);
            notification.setTimestamp(System.currentTimeMillis());
            
            String messageJson = JSON.toJSONString(notification);
            
            // 获取所有管理员用户
            List<User> adminUsers = userService.getAllAdmins();
            
            // 只通知管理员
            for (User admin : adminUsers) {
                if (!admin.getId().equals(userId) && channelManager.isUserOnline(admin.getId())) {
                    eventPublisher.publishEvent(new WebSocketMessageEvent(admin.getId(), messageJson));
                }
            }
        } catch (Exception e) {
            log.error("推送用户上线通知失败", e);
        }
    }
    
    /**
     * 用户下线处理
     */
    private void handleUserOffline(Long userId) {
        try {
            // 删除 Redis 在线状态
            redisTemplate.delete("chat:online:" + userId);
            log.info("用户 {} 已下线", userId);
            
            // 获取用户信息
            User user = userService.getById(userId);
            if (user == null) {
                return;
            }
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("user_id", userId);
            userData.put("userName", user.getUserName());
            userData.put("offlineTime", System.currentTimeMillis());
            
            // 创建通知消息
            WebSocketMessage notification = new WebSocketMessage();
            notification.setType("user_offline");
            notification.setContent("用户下线");
            notification.setData(userData);
            notification.setTimestamp(System.currentTimeMillis());
            
            String messageJson = JSON.toJSONString(notification);
            
            // 获取所有管理员用户
            List<User> adminUsers = userService.getAllAdmins();
            
            // 只通知管理员
            for (User admin : adminUsers) {
                if (channelManager.isUserOnline(admin.getId())) {
                    eventPublisher.publishEvent(new WebSocketMessageEvent(admin.getId(), messageJson));
                }
            }
        } catch (Exception e) {
            log.error("推送用户下线通知失败", e);
        }
    }
    
    /**
     * 当用户上线时通知管理员
     * 此方法被UserStatusEvent事件监听器调用
     */
    @Override
    public void notifyUserOnline(Long userId) {
        handleUserOnline(userId);
    }
    
    /**
     * 当用户下线时通知管理员
     * 此方法被 UserStatusEvent 事件监听器调用
     */
    @Override
    public void notifyUserOffline(Long userId) {
        handleUserOffline(userId);
    }
    
    /**
     * 发送好友申请通知
     */
    @Override
    public void sendFriendRequestNotification(Long receiverId, Long senderId) {
        try {
            // 获取发送者信息
            User sender = userService.getById(senderId);
            String senderName = sender != null ? sender.getUserName() : "未知用户";
            
            // 创建通知消息
            WebSocketMessage notification = new WebSocketMessage();
            notification.setType("friend_request");
            notification.setContent("收到一条好友申请");
            notification.setReceiverId(receiverId);
            notification.setTimestamp(System.currentTimeMillis());
            
            // 附加数据
            Map<String, Object> data = new HashMap<>();
            data.put("sender_id", senderId);
            data.put("senderName", senderName);
            notification.setData(data);
            
            String messageJson = JSON.toJSONString(notification);
            
            // 发送给接收者
            if (channelManager.isUserOnline(receiverId)) {
                eventPublisher.publishEvent(new WebSocketMessageEvent(receiverId, messageJson));
                log.info("发送好友申请通知给 receiver_id={}, sender_id={}", receiverId, senderId);
            } else {
                log.info("用户不在线，跳过通知 receiver_id={}, sender_id={}", receiverId, senderId);
            }
        } catch (Exception e) {
            log.error("发送好友申请通知失败", e);
        }
    }
}