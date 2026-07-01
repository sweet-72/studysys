package com.ttbt.smartclass.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.model.entity.ChatMessage;
import com.ttbt.smartclass.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 私聊消息服务接口
 */
public interface ChatMessageService extends IService<ChatMessage> {

    /**
     * 发送文本消息
     * 
     * @param senderId 发送者 ID
     * @param receiverId 接收者 ID
     * @param content 消息内容
     * @return 消息对象
     */
    ChatMessage sendTextMessage(Long senderId, Long receiverId, String content);

    /**
     * 发送图片消息
     * 
     * @param senderId 发送者 ID
     * @param receiverId 接收者 ID
     * @param mediaUrl 图片 URL
     * @param mediaSize 文件大小
     * @return 消息对象
     */
    ChatMessage sendImageMessage(Long senderId, Long receiverId, String mediaUrl, Long mediaSize);

    /**
     * 发送视频消息
     * 
     * @param senderId 发送者 ID
     * @param receiverId 接收者 ID
     * @param mediaUrl 视频 URL
     * @param mediaSize 文件大小
     * @param mediaDuration 视频时长
     * @return 消息对象
     */
    ChatMessage sendVideoMessage(Long senderId, Long receiverId, String mediaUrl, Long mediaSize, Integer mediaDuration);

    /**
     * 发送语音消息
     * 
     * @param senderId 发送者 ID
     * @param receiverId 接收者 ID
     * @param audioUrl 语音 URL
     * @param audioDuration 语音时长
     * @param mediaSize 文件大小
     * @return 消息对象
     */
    ChatMessage sendAudioMessage(Long senderId, Long receiverId, String audioUrl, Integer audioDuration, Long mediaSize);

    /**
     * 撤回消息
     * 
     * @param messageId 消息 ID
     * @param userId 用户 ID
     */
    void revokeMessage(Long messageId, Long userId);

    /**
     * 标记消息为已读
     * 
     * @param messageId 消息 ID
     * @param userId 用户 ID
     */
    void markAsRead(Long messageId, Long userId);

    /**
     * 标记整个会话为已读
     * 
     * @param sessionId 会话 ID
     * @param userId 用户 ID
     */
    void markSessionAsRead(String sessionId, Long userId);

    /**
     * 获取会话消息列表
     * 
     * @param sessionId 会话 ID
     * @param current 当前页
     * @param pageSize 每页大小
     * @return 消息分页列表
     */
    Page<ChatMessage> getSessionMessages(String sessionId, long current, long pageSize);

    /**
     * 获取用户的未读消息数
     * 
     * @param userId 用户 ID
     * @return 未读消息总数
     */
    int getUserUnreadCount(Long userId);
    
    /**
     * 获取用户的所有会话未读数（按会话维度）
     * 
     * @param userId 用户 ID
     * @return 会话未读数 Map<sessionId, count>
     */
    java.util.Map<String, Integer> getAllSessionUnreadCounts(Long userId);
    
    /**
     * 获取用户的总未读数（用于全局红点）
     * 
     * @param userId 用户 ID
     * @return 总未读数
     */
    int getTotalUnreadCount(Long userId);
}
