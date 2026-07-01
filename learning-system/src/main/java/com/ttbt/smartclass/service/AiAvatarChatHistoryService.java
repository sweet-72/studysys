package com.ttbt.smartclass.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ttbt.smartclass.model.entity.AiAvatarChatHistory;
import com.ttbt.smartclass.model.vo.ChatMessageVO;
import com.ttbt.smartclass.model.vo.ChatSessionVO;
import java.util.List;

/**
* @author mudong
* @description 针对表【ai_avatar_chat_history(AI分身对话历史)】的数据库操作Service
* @createDate 2025-03-24 21:35:44
*/
public interface AiAvatarChatHistoryService extends IService<AiAvatarChatHistory> {

    boolean saveMessage(Long userId, Long aiAvatarId, String sessionId, String messageType, String content);

    String createNewSession(Long userId, Long aiAvatarId);

    String getLatestDifyConversationId(String sessionId);

    void bindDifyConversationId(String sessionId, Long messageId, String difyConversationId);

    List<ChatSessionVO> getUserSessions(Long userId, Long aiAvatarId);

    List<AiAvatarChatHistory> getSessionHistory(String sessionId);

    Page<AiAvatarChatHistory> getSessionHistoryPage(String sessionId, long current, long size);

    Page<AiAvatarChatHistory> getUserHistoryPage(Long userId, Long aiAvatarId, long current, long size);

    Page<AiAvatarChatHistory> getUserLatestMessagesPage(Long userId, long current, long size);

    boolean updateSessionName(String sessionId, String sessionName);

    boolean deleteSession(String sessionId, Long userId);

    boolean deleteSessionCompletely(String sessionId, Long userId, String baseUrl, String avatarAuth);

    List<ChatSessionVO> getRecentSessions(Long userId, int limit);

    List<ChatMessageVO> getUserMessages(Long userId, Long aiAvatarId);

    boolean updateSessionSummary(String sessionId, String summary);

    List<ChatSessionVO> getHistoryDialogsList(Long userId, int limit, int offset);

    boolean hasSessionAccess(String sessionId, Long userId, Long aiAvatarId);
}