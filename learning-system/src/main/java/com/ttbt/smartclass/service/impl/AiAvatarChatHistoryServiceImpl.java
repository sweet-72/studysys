package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.mapper.AiAvatarChatHistoryMapper;
import com.ttbt.smartclass.mapper.AiAvatarMapper;
import com.ttbt.smartclass.mapper.UserMapper;
import com.ttbt.smartclass.model.entity.AiAvatar;
import com.ttbt.smartclass.model.entity.AiAvatarChatHistory;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.ChatMessageVO;
import com.ttbt.smartclass.model.vo.ChatSessionVO;
import com.ttbt.smartclass.service.AiAvatarChatHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * AI 数字人聊天历史服务实现类。
 */
@Service
public class AiAvatarChatHistoryServiceImpl extends ServiceImpl<AiAvatarChatHistoryMapper, AiAvatarChatHistory>
        implements AiAvatarChatHistoryService {

    private static final Logger log = LoggerFactory.getLogger(AiAvatarChatHistoryServiceImpl.class);

    @Resource
    private AiAvatarMapper aiAvatarMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ApplicationContext applicationContext;

    @Override
    @Transactional
    public boolean saveMessage(Long userId, Long aiAvatarId, String sessionId, String messageType, String content) {
        if (userId == null || aiAvatarId == null || !StringUtils.hasText(sessionId)
                || !StringUtils.hasText(messageType) || content == null) {
            return false;
        }

        // Ignore service bootstrap marker messages.
        if ("system".equals(messageType) && "SESSION_CREATED".equals(content)) {
            return true;
        }

        AiAvatarChatHistory chatHistory = new AiAvatarChatHistory();
        chatHistory.setUserId(userId);
        chatHistory.setAiAvatarId(aiAvatarId);
        chatHistory.setSessionId(sessionId);
        chatHistory.setMessageType(messageType);
        chatHistory.setContent(content);
        chatHistory.setCreateTime(new Date());
        chatHistory.setTokens(Math.max(1, content.length() / 4));
        return this.save(chatHistory);
    }

    @Override
    public String createNewSession(Long userId, Long aiAvatarId) {
        return UUID.randomUUID().toString();
    }

    @Override
    public String getLatestDifyConversationId(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return null;
        }
        AiAvatarChatHistory latestHistory = this.getOne(new LambdaQueryWrapper<AiAvatarChatHistory>()
                .select(AiAvatarChatHistory::getId, AiAvatarChatHistory::getDifyConversationId)
                .eq(AiAvatarChatHistory::getSessionId, sessionId)
                .and(wrapper -> wrapper.isNotNull(AiAvatarChatHistory::getDifyConversationId)
                        .ne(AiAvatarChatHistory::getDifyConversationId, ""))
                .orderByDesc(AiAvatarChatHistory::getId)
                .last("LIMIT 1"), false);
        return latestHistory == null ? null : latestHistory.getDifyConversationId();
    }

    @Override
    @Transactional
    public void bindDifyConversationId(String sessionId, Long messageId, String difyConversationId) {
        if (!StringUtils.hasText(sessionId) || !StringUtils.hasText(difyConversationId)) {
            return;
        }
        if (messageId != null) {
            AiAvatarChatHistory updateEntity = new AiAvatarChatHistory();
            updateEntity.setId(messageId);
            updateEntity.setDifyConversationId(difyConversationId);
            this.updateById(updateEntity);
        }
        this.update(new LambdaUpdateWrapper<AiAvatarChatHistory>()
                .eq(AiAvatarChatHistory::getSessionId, sessionId)
                .and(wrapper -> wrapper.isNull(AiAvatarChatHistory::getDifyConversationId)
                        .or()
                        .eq(AiAvatarChatHistory::getDifyConversationId, ""))
                .set(AiAvatarChatHistory::getDifyConversationId, difyConversationId));
    }

    @Override
    public List<ChatSessionVO> getUserSessions(Long userId, Long aiAvatarId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<AiAvatarChatHistory> queryWrapper = new LambdaQueryWrapper<AiAvatarChatHistory>()
                .eq(AiAvatarChatHistory::getUserId, userId)
                .orderByDesc(AiAvatarChatHistory::getCreateTime)
                .orderByDesc(AiAvatarChatHistory::getId);
        if (aiAvatarId != null) {
            queryWrapper.eq(AiAvatarChatHistory::getAiAvatarId, aiAvatarId);
        }

        List<AiAvatarChatHistory> records = this.list(queryWrapper);
        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, AiAvatar> avatarCache = new HashMap<>();
        Map<String, ChatSessionVO> sessionMap = new LinkedHashMap<>();

        for (AiAvatarChatHistory record : records) {
            if (!StringUtils.hasText(record.getSessionId())) {
                continue;
            }

            ChatSessionVO sessionVO = sessionMap.get(record.getSessionId());
            if (sessionVO == null) {
                sessionVO = new ChatSessionVO();
                sessionVO.setSessionId(record.getSessionId());
                sessionVO.setSessionName(record.getSessionName());
                sessionVO.setAiAvatarId(record.getAiAvatarId());
                sessionVO.setLastMessage(record.getContent());
                sessionVO.setLastMessageTime(record.getCreateTime());
                sessionVO.setMessageCount(0);

                if (record.getAiAvatarId() != null) {
                    AiAvatar aiAvatar = avatarCache.computeIfAbsent(record.getAiAvatarId(), aiAvatarMapper::selectById);
                    if (aiAvatar != null) {
                        sessionVO.setAiAvatarName(aiAvatar.getName());
                        sessionVO.setAiAvatarImgUrl(aiAvatar.getAvatarImgUrl());
                    }
                }
                sessionMap.put(record.getSessionId(), sessionVO);
            }

            sessionVO.setMessageCount(sessionVO.getMessageCount() + 1);
        }

        return new ArrayList<>(sessionMap.values());
    }

    @Override
    public List<AiAvatarChatHistory> getSessionHistory(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return Collections.emptyList();
        }

        return this.list(new LambdaQueryWrapper<AiAvatarChatHistory>()
                .eq(AiAvatarChatHistory::getSessionId, sessionId)
                .orderByAsc(AiAvatarChatHistory::getCreateTime)
                .orderByAsc(AiAvatarChatHistory::getId));
    }

    @Override
    public Page<AiAvatarChatHistory> getSessionHistoryPage(String sessionId, long current, long size) {
        Page<AiAvatarChatHistory> page = new Page<>(current, size);
        if (!StringUtils.hasText(sessionId)) {
            return page;
        }

        return this.page(page, new LambdaQueryWrapper<AiAvatarChatHistory>()
                .eq(AiAvatarChatHistory::getSessionId, sessionId)
                .orderByAsc(AiAvatarChatHistory::getCreateTime)
                .orderByAsc(AiAvatarChatHistory::getId));
    }

    @Override
    public Page<AiAvatarChatHistory> getUserHistoryPage(Long userId, Long aiAvatarId, long current, long size) {
        Page<AiAvatarChatHistory> page = new Page<>(current, size);
        if (userId == null) {
            return page;
        }

        LambdaQueryWrapper<AiAvatarChatHistory> queryWrapper = new LambdaQueryWrapper<AiAvatarChatHistory>()
                .eq(AiAvatarChatHistory::getUserId, userId)
                .orderByDesc(AiAvatarChatHistory::getCreateTime)
                .orderByDesc(AiAvatarChatHistory::getId);
        if (aiAvatarId != null && aiAvatarId > 0) {
            queryWrapper.eq(AiAvatarChatHistory::getAiAvatarId, aiAvatarId);
        }

        return this.page(page, queryWrapper);
    }

    @Override
    public Page<AiAvatarChatHistory> getUserLatestMessagesPage(Long userId, long current, long size) {
        Page<AiAvatarChatHistory> page = new Page<>(current, size);
        if (userId == null) {
            return page;
        }

        List<AiAvatarChatHistory> allMessages = this.list(new LambdaQueryWrapper<AiAvatarChatHistory>()
                .eq(AiAvatarChatHistory::getUserId, userId)
                .orderByDesc(AiAvatarChatHistory::getCreateTime)
                .orderByDesc(AiAvatarChatHistory::getId));

        LinkedHashMap<String, AiAvatarChatHistory> latestBySession = new LinkedHashMap<>();
        for (AiAvatarChatHistory message : allMessages) {
            if (StringUtils.hasText(message.getSessionId()) && !latestBySession.containsKey(message.getSessionId())) {
                latestBySession.put(message.getSessionId(), message);
            }
        }

        List<AiAvatarChatHistory> latestMessages = new ArrayList<>(latestBySession.values());
        int from = (int) Math.max(0, (current - 1) * size);
        int to = (int) Math.min(latestMessages.size(), from + size);

        if (from >= latestMessages.size()) {
            page.setRecords(Collections.emptyList());
        } else {
            page.setRecords(latestMessages.subList(from, to));
        }
        page.setTotal(latestMessages.size());
        return page;
    }

    @Override
    @Transactional
    public boolean updateSessionName(String sessionId, String sessionName) {
        if (!StringUtils.hasText(sessionId) || !StringUtils.hasText(sessionName)) {
            return false;
        }

        List<AiAvatarChatHistory> chatHistories = this.list(new LambdaQueryWrapper<AiAvatarChatHistory>()
                .eq(AiAvatarChatHistory::getSessionId, sessionId));
        if (chatHistories.isEmpty()) {
            return false;
        }

        for (AiAvatarChatHistory history : chatHistories) {
            history.setSessionName(sessionName);
        }
        return this.updateBatchById(chatHistories);
    }

    @Override
    public boolean updateSessionSummary(String sessionId, String summary) {
        return updateSessionName(sessionId, summary);
    }

    @Override
    @Transactional
    public boolean deleteSession(String sessionId, Long userId) {
        if (!StringUtils.hasText(sessionId) || userId == null) {
            return false;
        }

        if (!hasSessionAccess(sessionId, userId, null)) {
            return false;
        }

        return this.remove(new LambdaQueryWrapper<AiAvatarChatHistory>()
                .eq(AiAvatarChatHistory::getSessionId, sessionId));
    }

    @Override
    @Transactional
    public boolean deleteSessionCompletely(String sessionId, Long userId, String baseUrl, String avatarAuth) {
        if (!StringUtils.hasText(sessionId) || userId == null) {
            return false;
        }

        if (!hasSessionAccess(sessionId, userId, null)) {
            log.warn("deleteSessionCompletely rejected, session not owned: sessionId={}, user_id={}", sessionId, userId);
            return false;
        }

        if (StringUtils.hasText(baseUrl) && StringUtils.hasText(avatarAuth)) {
            try {
                Object difyServiceObj = applicationContext.getBean("difyServiceImpl");
                Class<?> clazz = difyServiceObj.getClass();
                clazz.getMethod("deleteConversation", Long.class, String.class, String.class, String.class)
                        .invoke(difyServiceObj, userId, sessionId, baseUrl, avatarAuth);
            } catch (Exception e) {
                // Remote deletion failure should not block local deletion.
                log.warn("delete dify conversation failed, continue local delete. sessionId={}", sessionId, e);
            }
        }

        return this.remove(new LambdaQueryWrapper<AiAvatarChatHistory>()
                .eq(AiAvatarChatHistory::getSessionId, sessionId));
    }

    @Override
    public List<ChatSessionVO> getRecentSessions(Long userId, int limit) {
        if (userId == null || limit <= 0) {
            return Collections.emptyList();
        }

        List<ChatSessionVO> sessions = getUserSessions(userId, null);
        if (sessions.size() <= limit) {
            return sessions;
        }
        return new ArrayList<>(sessions.subList(0, limit));
    }

    @Override
    public List<ChatMessageVO> getUserMessages(Long userId, Long aiAvatarId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<AiAvatarChatHistory> queryWrapper = new LambdaQueryWrapper<AiAvatarChatHistory>()
                .eq(AiAvatarChatHistory::getUserId, userId)
                .orderByDesc(AiAvatarChatHistory::getCreateTime)
                .orderByDesc(AiAvatarChatHistory::getId);
        if (aiAvatarId != null && aiAvatarId > 0) {
            queryWrapper.eq(AiAvatarChatHistory::getAiAvatarId, aiAvatarId);
        }

        List<AiAvatarChatHistory> messageList = this.list(queryWrapper);
        if (messageList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, AiAvatar> aiAvatarCache = new HashMap<>();
        Map<Long, User> userCache = new HashMap<>();
        List<ChatMessageVO> result = new ArrayList<>(messageList.size());

        for (AiAvatarChatHistory message : messageList) {
            ChatMessageVO chatMessageVO = new ChatMessageVO();
            BeanUtils.copyProperties(message, chatMessageVO);

            if (message.getAiAvatarId() != null) {
                AiAvatar aiAvatar = aiAvatarCache.computeIfAbsent(message.getAiAvatarId(), aiAvatarMapper::selectById);
                if (aiAvatar != null) {
                    chatMessageVO.setAiAvatarName(aiAvatar.getName());
                    chatMessageVO.setAiAvatarImgUrl(aiAvatar.getAvatarImgUrl());
                }
            }

            if (message.getUserId() != null) {
                User user = userCache.computeIfAbsent(message.getUserId(), userMapper::selectById);
                if (user != null) {
                    chatMessageVO.setUserName(user.getUserName());
                    chatMessageVO.setUserAvatar(user.getUserAvatar());
                }
            }

            result.add(chatMessageVO);
        }

        return result;
    }

    @Override
    public boolean hasSessionAccess(String sessionId, Long userId, Long aiAvatarId) {
        if (!StringUtils.hasText(sessionId) || userId == null) {
            return false;
        }

        AiAvatarChatHistory anySessionRecord = this.getOne(new LambdaQueryWrapper<AiAvatarChatHistory>()
                .select(AiAvatarChatHistory::getId)
                .eq(AiAvatarChatHistory::getSessionId, sessionId)
                .last("LIMIT 1"), false);

        // Allow brand new session id before first message is persisted.
        if (anySessionRecord == null) {
            return true;
        }

        LambdaQueryWrapper<AiAvatarChatHistory> ownerQuery = new LambdaQueryWrapper<AiAvatarChatHistory>()
                .select(AiAvatarChatHistory::getId)
                .eq(AiAvatarChatHistory::getSessionId, sessionId)
                .eq(AiAvatarChatHistory::getUserId, userId)
                .last("LIMIT 1");
        if (aiAvatarId != null) {
            ownerQuery.eq(AiAvatarChatHistory::getAiAvatarId, aiAvatarId);
        }

        return this.getOne(ownerQuery, false) != null;
    }

    @Override
    public List<ChatSessionVO> getHistoryDialogsList(Long userId, int limit, int offset) {
        if (userId == null || limit <= 0 || offset < 0) {
            return Collections.emptyList();
        }

        List<ChatSessionVO> allSessions = getUserSessions(userId, null);
        if (offset >= allSessions.size()) {
            return Collections.emptyList();
        }

        int end = Math.min(offset + limit, allSessions.size());
        return new ArrayList<>(allSessions.subList(offset, end));
    }
}
