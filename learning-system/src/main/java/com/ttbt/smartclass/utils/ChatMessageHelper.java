package com.ttbt.smartclass.utils;

import com.ttbt.smartclass.model.dto.dify.DifyChatResponse;
import com.ttbt.smartclass.model.entity.AiAvatarChatHistory;
import java.util.Date;
import org.springframework.stereotype.Component;

/**
 * AI 聊天消息构造工具。
 * 负责统一创建用户消息和 AI 回复历史记录。
 */
@Component
public class ChatMessageHelper {

    /**
     * 创建用户聊天消息记录。
     *
     * @param userId 用户 ID
     * @param aiAvatarId AI 智能体 ID
     * @param sessionId 会话 ID
     * @param content 用户消息内容
     * @return 用户消息记录
     */
    public AiAvatarChatHistory createUserMessage(Long userId, Long aiAvatarId, String sessionId, String content) {
        // 设置消息归属、消息类型和内容，并按内容长度粗略估算 token 数
        AiAvatarChatHistory userMessage = new AiAvatarChatHistory();
        userMessage.setUserId(userId);
        userMessage.setAiAvatarId(aiAvatarId);
        userMessage.setSessionId(sessionId);
        userMessage.setMessageType("user");
        userMessage.setContent(content);
        userMessage.setCreateTime(new Date());
        userMessage.setTokens(Math.max(1, content.length() / 4));
        return userMessage;
    }

    /**
     * 根据 Dify 阻塞响应创建 AI 回复记录。
     *
     * @param userId 用户 ID
     * @param aiAvatarId AI 智能体 ID
     * @param sessionId 会话 ID
     * @param chatResponse Dify 聊天响应
     * @return AI 回复记录
     */
    public AiAvatarChatHistory createAiResponse(Long userId, Long aiAvatarId, String sessionId, DifyChatResponse chatResponse) {
        // 从 Dify metadata 中读取回复 token 数，缺失时默认为 0
        int tokens = 0;
        if (chatResponse != null && chatResponse.getMetadata() != null && chatResponse.getMetadata().getUsage() != null) {
            tokens = chatResponse.getMetadata().getUsage().getCompletion_tokens();
        }

        // 保存 Dify conversation_id，便于后续会话上下文续聊
        AiAvatarChatHistory aiMessage = new AiAvatarChatHistory();
        aiMessage.setUserId(userId);
        aiMessage.setAiAvatarId(aiAvatarId);
        aiMessage.setSessionId(sessionId);
        aiMessage.setDifyConversationId(chatResponse == null ? null : chatResponse.getConversation_id());
        aiMessage.setMessageType("ai");
        aiMessage.setContent(chatResponse == null ? "" : chatResponse.getAnswer());
        aiMessage.setTokens(tokens);
        aiMessage.setCreateTime(new Date());
        return aiMessage;
    }

    /**
     * 根据纯文本内容创建 AI 回复记录。
     *
     * @param userId 用户 ID
     * @param aiAvatarId AI 智能体 ID
     * @param sessionId 会话 ID
     * @param content AI 回复文本
     * @param tokens token 数
     * @return AI 回复记录
     */
    public AiAvatarChatHistory createAiResponseFromText(Long userId, Long aiAvatarId, String sessionId, 
                                                         String content, int tokens) {
        // 用于流式或工作流场景，将最终文本直接落为 AI 回复消息
        AiAvatarChatHistory aiMessage = new AiAvatarChatHistory();
        aiMessage.setUserId(userId);
        aiMessage.setAiAvatarId(aiAvatarId);
        aiMessage.setSessionId(sessionId);
        aiMessage.setMessageType("ai");
        aiMessage.setContent(content == null ? "" : content);
        aiMessage.setTokens(tokens);
        aiMessage.setCreateTime(new Date());
        return aiMessage;
    }

    /**
     * 创建空的 AI 回复记录。
     *
     * @param userId 用户 ID
     * @param aiAvatarId AI 智能体 ID
     * @param sessionId 会话 ID
     * @return 空 AI 回复记录
     */
    public AiAvatarChatHistory createEmptyAiResponse(Long userId, Long aiAvatarId, String sessionId) {
        // 用于占位或异常兜底场景，内容为空但保留会话归属
        AiAvatarChatHistory aiResponse = new AiAvatarChatHistory();
        aiResponse.setUserId(userId);
        aiResponse.setAiAvatarId(aiAvatarId);
        aiResponse.setSessionId(sessionId);
        aiResponse.setMessageType("ai");
        aiResponse.setContent("");
        aiResponse.setCreateTime(new Date());
        return aiResponse;
    }
}
