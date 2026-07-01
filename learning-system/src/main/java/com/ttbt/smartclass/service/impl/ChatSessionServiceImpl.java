package com.ttbt.smartclass.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.ChatSessionMapper;
import com.ttbt.smartclass.model.entity.ChatSession;
import com.ttbt.smartclass.service.ChatSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天会话服务实现类。
 */
@Slf4j
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SESSIONS_KEY = "chat:sessions:";
    private static final String UNREAD_COUNT_KEY = "chat:unread:";

    @Override
    public ChatSession getOrCreateSession(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "user_id cannot be null");
        }

        String sessionId = generateSessionId(userId1, userId2);
        ChatSession session = this.baseMapper.selectBySessionId(sessionId);
        if (session != null && !Integer.valueOf(1).equals(session.getIsDelete())) {
            return session;
        }

        ChatSession newSession = new ChatSession();
        newSession.setSessionId(sessionId);
        newSession.setUser1Id(Math.min(userId1, userId2));
        newSession.setUser2Id(Math.max(userId1, userId2));
        newSession.setUser1UnreadCount(0);
        newSession.setUser2UnreadCount(0);
        newSession.setIsUser1Deleted(0);
        newSession.setIsUser2Deleted(0);
        newSession.setIsUser1Top(0);
        newSession.setIsUser2Top(0);
        newSession.setIsDelete(0);
        newSession.setCreateTime(new Date());

        try {
            boolean saved = this.save(newSession);
            if (!saved) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "create session failed");
            }
        } catch (DuplicateKeyException duplicateKeyException) {
            ChatSession concurrentSession = this.baseMapper.selectBySessionId(sessionId);
            if (concurrentSession != null && !Integer.valueOf(1).equals(concurrentSession.getIsDelete())) {
                return concurrentSession;
            }
            throw duplicateKeyException;
        }

        log.info("Created chat session: {}", sessionId);
        return newSession;
    }

    @Override
    public Page<ChatSession> getUserSessions(Long userId, long current, long pageSize) {
        Page<ChatSession> page = new Page<>(current, pageSize);
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(ChatSession::getIsDelete, 0)
                .and(w -> w.nested(n -> n.eq(ChatSession::getUser1Id, userId)
                                        .eq(ChatSession::getIsUser1Deleted, 0))
                           .or(n -> n.eq(ChatSession::getUser2Id, userId)
                                     .eq(ChatSession::getIsUser2Deleted, 0)))
                .orderByDesc(ChatSession::getLastMessageTime)
                .orderByDesc(ChatSession::getUpdateTime)
                .orderByDesc(ChatSession::getId);

        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastMessage(String sessionId, Long messageId, String content, String messageType, Long targetUserId) {
        ChatSession session = this.baseMapper.selectBySessionId(sessionId);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "session not found");
        }

        this.baseMapper.updateLastMessage(
                sessionId,
                messageId,
                content,
                messageType,
                targetUserId,
                session.getUser1Id(),
                session.getUser2Id()
        );

        try {
            String sessionsKey = SESSIONS_KEY + targetUserId;
            redisTemplate.opsForZSet().add(sessionsKey, sessionId, System.currentTimeMillis());

            String unreadKey = UNREAD_COUNT_KEY + targetUserId;
            redisTemplate.opsForHash().increment(unreadKey, sessionId, 1);
        } catch (Exception e) {
            log.warn("update session cache failed, sessionId={}", sessionId, e);
        }
    }

    @Override
    public void clearUnreadCount(String sessionId, Long userId) {
        ChatSession session = this.baseMapper.selectBySessionId(sessionId);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "session not found");
        }
        if (!hasSessionAccess(sessionId, userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no auth for session");
        }

        this.baseMapper.clearUnreadCount(sessionId, userId, session.getUser1Id(), session.getUser2Id());

        try {
            String unreadKey = UNREAD_COUNT_KEY + userId;
            redisTemplate.opsForHash().put(unreadKey, sessionId, 0);
        } catch (Exception e) {
            log.warn("clear unread cache failed, sessionId={}, user_id={}", sessionId, userId, e);
        }
    }

    @Override
    public void decreaseUnreadCount(String sessionId, Long userId, int delta) {
        if (delta <= 0) {
            return;
        }
        ChatSession session = this.baseMapper.selectBySessionId(sessionId);
        if (session == null) {
            return;
        }
        if (!hasSessionAccess(sessionId, userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no auth for session");
        }

        this.baseMapper.decreaseUnreadCount(sessionId, userId, delta, session.getUser1Id(), session.getUser2Id());

        try {
            String unreadKey = UNREAD_COUNT_KEY + userId;
            Object countObj = redisTemplate.opsForHash().get(unreadKey, sessionId);
            int current = parseInt(countObj);
            int latest = Math.max(0, current - delta);
            redisTemplate.opsForHash().put(unreadKey, sessionId, latest);
        } catch (Exception e) {
            log.warn("decrease unread cache failed, sessionId={}, user_id={}, delta={}", sessionId, userId, delta, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(String sessionId, Long userId) {
        ChatSession session = this.baseMapper.selectBySessionId(sessionId);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "session not found");
        }

        if (userId.equals(session.getUser1Id())) {
            session.setIsUser1Deleted(1);
        } else if (userId.equals(session.getUser2Id())) {
            session.setIsUser2Deleted(1);
        } else {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no auth for session");
        }

        if (Integer.valueOf(1).equals(session.getIsUser1Deleted())
                && Integer.valueOf(1).equals(session.getIsUser2Deleted())) {
            this.removeById(session.getId());
        } else {
            this.updateById(session);
        }
    }

    @Override
    public void toggleTop(String sessionId, Long userId, boolean isTop) {
        ChatSession session = this.baseMapper.selectBySessionId(sessionId);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "session not found");
        }

        if (userId.equals(session.getUser1Id())) {
            session.setIsUser1Top(isTop ? 1 : 0);
        } else if (userId.equals(session.getUser2Id())) {
            session.setIsUser2Top(isTop ? 1 : 0);
        } else {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no auth for session");
        }

        this.updateById(session);
    }

    @Override
    public int getSessionUnreadCount(String sessionId, Long userId) {
        if (!hasSessionAccess(sessionId, userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no auth for session");
        }

        ChatSession session = this.baseMapper.selectBySessionId(sessionId);
        if (session == null) {
            return 0;
        }
        int dbCount = userId.equals(session.getUser1Id()) ? safeCount(session.getUser1UnreadCount()) : safeCount(session.getUser2UnreadCount());
        try {
            String unreadKey = UNREAD_COUNT_KEY + userId;
            redisTemplate.opsForHash().put(unreadKey, sessionId, dbCount);
        } catch (Exception e) {
            log.warn("sync session unread to redis failed, sessionId={}, user_id={}", sessionId, userId, e);
        }
        return dbCount;
    }

    @Override
    public Map<String, Integer> getAllSessionUnreadCounts(Long userId) {
        Map<String, Integer> result = new HashMap<>();
        try {
            LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChatSession::getIsDelete, 0)
                    .and(w -> w.eq(ChatSession::getUser1Id, userId).or().eq(ChatSession::getUser2Id, userId));

            List<ChatSession> sessions = this.list(wrapper);
            for (ChatSession session : sessions) {
                int count = userId.equals(session.getUser1Id())
                        ? safeCount(session.getUser1UnreadCount())
                        : safeCount(session.getUser2UnreadCount());
                if (count > 0) {
                    result.put(session.getSessionId(), count);
                }
            }
            syncSessionUnreadMapToRedis(userId, result);
            return result;
        } catch (Exception dbException) {
            log.warn("load unread from db failed, fallback redis. user_id={}", userId, dbException);
            String unreadKey = UNREAD_COUNT_KEY + userId;
            try {
                Map<Object, Object> unreadMap = redisTemplate.opsForHash().entries(unreadKey);
                if (unreadMap != null) {
                    for (Map.Entry<Object, Object> entry : unreadMap.entrySet()) {
                        if (entry.getKey() == null || entry.getValue() == null) {
                            continue;
                        }
                        int count = Math.max(0, parseInt(entry.getValue()));
                        if (count > 0) {
                            result.put(entry.getKey().toString(), count);
                        }
                    }
                }
            } catch (Exception redisException) {
                log.warn("fallback redis unread read failed, user_id={}", userId, redisException);
            }
            return result;
        }
    }

    @Override
    public boolean hasSessionAccess(String sessionId, Long userId) {
        if (StrUtil.isBlank(sessionId) || userId == null) {
            return false;
        }
        ChatSession session = this.baseMapper.selectBySessionId(sessionId);
        if (session == null || Integer.valueOf(1).equals(session.getIsDelete())) {
            return false;
        }
        return userId.equals(session.getUser1Id()) || userId.equals(session.getUser2Id());
    }

    private String generateSessionId(Long userId1, Long userId2) {
        return userId1 < userId2 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }

    private int safeCount(Integer count) {
        return count == null ? 0 : Math.max(0, count);
    }

    private void syncSessionUnreadMapToRedis(Long userId, Map<String, Integer> unreadMap) {
        String unreadKey = UNREAD_COUNT_KEY + userId;
        try {
            redisTemplate.delete(unreadKey);
            if (unreadMap == null || unreadMap.isEmpty()) {
                return;
            }
            Map<String, Object> payload = new HashMap<>();
            for (Map.Entry<String, Integer> entry : unreadMap.entrySet()) {
                payload.put(entry.getKey(), entry.getValue());
            }
            redisTemplate.opsForHash().putAll(unreadKey, payload);
        } catch (Exception e) {
            log.warn("sync unread map to redis failed, user_id={}", userId, e);
        }
    }

    private int parseInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }
}

