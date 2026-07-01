package com.ttbt.smartclass.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ttbt.smartclass.model.entity.ChatSession;

import java.util.Map;

/**
 * 聊天会话服务。
 */
public interface ChatSessionService extends IService<ChatSession> {

    ChatSession getOrCreateSession(Long userId1, Long userId2);

    Page<ChatSession> getUserSessions(Long userId, long current, long pageSize);

    void updateLastMessage(String sessionId, Long messageId, String content, String messageType, Long targetUserId);

    void clearUnreadCount(String sessionId, Long userId);

    /**
     * Decrease unread count for one session by delta.
     */
    void decreaseUnreadCount(String sessionId, Long userId, int delta);

    void deleteSession(String sessionId, Long userId);

    void toggleTop(String sessionId, Long userId, boolean isTop);

    int getSessionUnreadCount(String sessionId, Long userId);

    Map<String, Integer> getAllSessionUnreadCounts(Long userId);

    /**
     * Validate whether the given user can access this chat session.
     */
    boolean hasSessionAccess(String sessionId, Long userId);
}
