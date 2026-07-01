package com.ttbt.smartclass.service;

import com.ttbt.smartclass.model.entity.AiAvatarChatHistory;
import com.ttbt.smartclass.model.vo.ChatMessageVO;
import java.util.Map;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Dify 对话与工作流服务。
 */
public interface DifyService {

    AiAvatarChatHistory sendChatMessage(Long userId, Long aiAvatarId, String sessionId, String content,
                                       String baseUrl, String avatarAuth);

    AiAvatarChatHistory sendChatMessageStreaming(Long userId, Long aiAvatarId, String sessionId, String content,
                                                String baseUrl, String avatarAuth, DifyStreamCallback callback);

    ChatMessageVO handleSendMessageRequest(Long userId, Long aiAvatarId, String sessionId, String content,
                                           boolean endChat, AiAvatarChatHistoryService chatHistoryService,
                                           AiAvatarService aiAvatarService, UserService userService);

    SseEmitter handleStreamMessageRequest(Long userId, Long aiAvatarId, String sessionId, String content,
                                          AiAvatarChatHistoryService chatHistoryService, AiAvatarService aiAvatarService);

    ChatMessageVO handleWorkflowRunRequest(Long userId, Long aiAvatarId, String sessionId, String content,
                                           Map<String, Object> inputs, boolean endChat, String workflowBaseUrl,
                                           String workflowAppKey, AiAvatarChatHistoryService chatHistoryService,
                                           AiAvatarService aiAvatarService, UserService userService);

    SseEmitter handleWorkflowStreamRequest(Long userId, Long aiAvatarId, String sessionId, String content,
                                           Map<String, Object> inputs, boolean endChat, String workflowBaseUrl,
                                           String workflowAppKey, AiAvatarChatHistoryService chatHistoryService,
                                           AiAvatarService aiAvatarService);

    String getSessionSummary(String sessionId, String baseUrl, String avatarAuth);

    boolean deleteConversation(Long userId, String sessionId, String baseUrl, String avatarAuth);

    boolean stopStreamingResponse(Long userId, String taskId, String baseUrl, String avatarAuth);

    interface DifyStreamCallback {

        void onMessage(String chunk);

        void onComplete(String fullResponse);

        void onError(Throwable error);
    }
}
