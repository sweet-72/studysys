package com.ttbt.smartclass.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ttbt.smartclass.config.DifyConfig;
import com.ttbt.smartclass.model.dto.dify.DifyChatRequest;
import com.ttbt.smartclass.model.dto.dify.DifyChatResponse;
import com.ttbt.smartclass.model.dto.dify.DifyStreamChunk;
import com.ttbt.smartclass.model.dto.dify.DifyWorkflowRunRequest;
import com.ttbt.smartclass.model.dto.dify.DifyWorkflowRunResponse;
import com.ttbt.smartclass.model.dto.dify.DifyWorkflowStreamChunk;
import com.ttbt.smartclass.model.entity.AiAvatar;
import com.ttbt.smartclass.model.entity.AiAvatarChatHistory;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.ChatMessageVO;
import com.ttbt.smartclass.service.AiAvatarChatHistoryService;
import com.ttbt.smartclass.service.AiAvatarService;
import com.ttbt.smartclass.service.DifyService;
import com.ttbt.smartclass.service.UserService;
import com.ttbt.smartclass.utils.ChatMessageHelper;
import com.ttbt.smartclass.utils.OkHttpUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Dify 对话与工作流服务实现类。
 * 负责封装 Dify 聊天、流式响应、工作流执行和会话控制等调用。
 */
@Service
@Slf4j
public class DifyServiceImpl implements DifyService {

    private static final long STREAM_TIMEOUT_MILLIS = 300000L;

    @Resource
    private DifyConfig difyConfig;

    @Resource
    private AiAvatarChatHistoryService aiAvatarChatHistoryService;

    @Resource
    private ChatMessageHelper chatMessageHelper;

    @Resource
    private OkHttpUtils okHttpUtils;

    @Resource
    private AiAvatarService aiAvatarService;

    @Resource
    private UserService userService;

    /**
     * 发送普通 Dify 聊天消息并保存问答记录。
     *
     * @param userId 当前用户 id
     * @param aiAvatarId AI 智能体 id
     * @param sessionId 本地会话 id
     * @param content 用户消息内容
     * @param baseUrl Dify 应用地址
     * @param avatarAuth Dify 应用密钥
     * @return 已保存的 AI 回复记录
     */
    @Override
    @Transactional
    public AiAvatarChatHistory sendChatMessage(Long userId, Long aiAvatarId, String sessionId, String content,
                                               String baseUrl, String avatarAuth) {
        // 校验用户、智能体、消息内容和 Dify 配置，避免无效请求进入外部调用
        validateSendParams(userId, aiAvatarId, content, baseUrl, avatarAuth);

        // 没有传入会话时生成本地会话 id，传入会话时校验访问权限
        String localSessionId = StringUtils.hasText(sessionId) ? sessionId : UUID.randomUUID().toString();
        if (StringUtils.hasText(localSessionId)
                && !aiAvatarChatHistoryService.hasSessionAccess(localSessionId, userId, aiAvatarId)) {
            throw new RuntimeException("Session access denied");
        }

        // 读取最近绑定的 Dify conversation_id，并先落库用户消息
        String difyConversationId = aiAvatarChatHistoryService.getLatestDifyConversationId(localSessionId);
        AiAvatarChatHistory userMessage = chatMessageHelper.createUserMessage(userId, aiAvatarId, localSessionId, content);
        userMessage.setDifyConversationId(difyConversationId);
        if (!aiAvatarChatHistoryService.save(userMessage)) {
            throw new RuntimeException("Save user message failed");
        }

        // 将智能体范围信息拼入问题后调用 Dify，必要时在内部重试新的会话
        String difyQuery = buildAvatarScopedQuery(aiAvatarId, content);
        return sendChatMessageWithRetry(userId, aiAvatarId, localSessionId, difyConversationId, difyQuery,
                baseUrl, avatarAuth, userMessage, false);
    }

    private AiAvatarChatHistory sendChatMessageWithRetry(Long userId, Long aiAvatarId, String localSessionId,
                                                         String difyConversationId, String content,
                                                         String baseUrl, String avatarAuth,
                                                         AiAvatarChatHistory userMessage,
                                                         boolean retriedWithNewConversation) {
        DifyChatRequest chatRequest = buildChatRequest(userId, difyConversationId, content);
        String url = appendApiPath(baseUrl, difyConfig.getChatMessagesPath());
        String requestJson = JSONUtil.toJsonStr(chatRequest);

        Response response = okHttpUtils.postJson(url, requestJson, buildAuthHeaders(avatarAuth));
        try {
            if (!response.isSuccessful()) {
                String body = readBodySafely(response.body());
                if (StringUtils.hasText(difyConversationId)
                        && shouldRetryWithNewConversation(response.code(), body, retriedWithNewConversation)) {
                    log.warn("Dify conversation not found, retry with empty conversation_id. localSessionId={}, difyConversationId={}",
                            localSessionId, difyConversationId);
                    return sendChatMessageWithRetry(userId, aiAvatarId, localSessionId, null, content,
                            baseUrl, avatarAuth, userMessage, true);
                }
                throw new RuntimeException("Dify request failed: " + response.code() + ", body=" + body);
            }

            String body = readBodySafely(response.body());
            DifyChatResponse chatResponse = JSONUtil.toBean(body, DifyChatResponse.class);
            String resolvedDifyConversationId = resolveDifyConversationId(difyConversationId,
                    chatResponse == null ? null : chatResponse.getConversation_id());
            if (StringUtils.hasText(resolvedDifyConversationId)) {
                aiAvatarChatHistoryService.bindDifyConversationId(localSessionId, userMessage.getId(), resolvedDifyConversationId);
            }

            AiAvatarChatHistory aiResponse = chatMessageHelper.createAiResponse(userId, aiAvatarId, localSessionId, chatResponse);
            aiResponse.setDifyConversationId(resolvedDifyConversationId);
            if (!aiAvatarChatHistoryService.save(aiResponse)) {
                throw new RuntimeException("Save AI response failed");
            }
            return aiResponse;
        } finally {
            response.close();
        }
    }

    /**
     * 发送 Dify 流式聊天消息并实时回调响应片段。
     *
     * @param userId 当前用户 id
     * @param aiAvatarId AI 智能体 id
     * @param sessionId 本地会话 id
     * @param content 用户消息内容
     * @param baseUrl Dify 应用地址
     * @param avatarAuth Dify 应用密钥
     * @param callback 流式响应回调
     * @return 已保存的 AI 回复记录
     */
    @Override
    public AiAvatarChatHistory sendChatMessageStreaming(Long userId, Long aiAvatarId, String sessionId,
                                                        String content, String baseUrl, String avatarAuth,
                                                        DifyStreamCallback callback) {
        log.info("sendChatMessageStreaming called: session={}, user={}, avatar={}, url={}", 
                sessionId, userId, aiAvatarId, baseUrl);
        if (userId == null || aiAvatarId == null || !StringUtils.hasText(sessionId) 
                || !StringUtils.hasText(content) || !StringUtils.hasText(baseUrl) || !StringUtils.hasText(avatarAuth)) {
            throw new RuntimeException("Invalid streaming chat params");
        }

        // 保存用户消息
        AiAvatarChatHistory userMessage = chatMessageHelper.createUserMessage(userId, aiAvatarId, sessionId, content);
        if (!aiAvatarChatHistoryService.save(userMessage)) {
            throw new RuntimeException("Save user message failed");
        }

        // 获取或创建 Dify conversation_id
        String difyConversationId = aiAvatarChatHistoryService.getLatestDifyConversationId(sessionId);
        String difyQuery = buildAvatarScopedQuery(aiAvatarId, content);
        
        // 构建流式请求
        DifyChatRequest chatRequest = buildChatRequest(userId, difyConversationId, difyQuery);
        chatRequest.setResponse_mode("streaming"); // 关键：设置为流式模式
        
        String url = appendApiPath(baseUrl, difyConfig.getChatMessagesPath());
        String requestJson = JSONUtil.toJsonStr(chatRequest);
        Map<String, String> headers = buildAuthHeaders(avatarAuth);
        
        log.info("Calling Dify streaming API:");
        log.info("  URL: {}", url);
        log.info("  Method: POST");
        log.info("  Headers: {}", headers);
        log.info("  Body: {}", requestJson);
        
        StringBuilder fullAnswer = new StringBuilder();
        String[] finalConversationId = {difyConversationId};
        int[] tokenCount = {0};
        int[] chunkCount = {0};
        
        try {
            // 使用流式 HTTP 客户端
            log.info("Executing postJsonStream...");
            Response response = okHttpUtils.postJsonStream(url, requestJson, buildAuthHeaders(avatarAuth));
            log.info("postJsonStream returned, response code: {}", response.code());
            
            if (!response.isSuccessful()) {
                String body = readBodySafely(response.body());
                response.close();
                throw new RuntimeException("Dify streaming request failed: " + response.code() + ", body=" + body);
            }
            
            // 读取流式响应
            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    throw new RuntimeException("Empty response body");
                }
                
                log.info("Start reading Dify stream response for session: {}", sessionId);
                log.info("Response content-type: {}", response.header("Content-Type"));
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
                String line;
                int lineCount = 0;
                int emptyLineCount = 0;
                
                while ((line = reader.readLine()) != null) {
                    lineCount++;
                    
                    // 记录前 5 行和空行
                    if (lineCount <= 5) {
                        log.info("Stream line {}: [{}]", lineCount, line.isEmpty() ? "(empty)" : line);
                    }
                    
                    if (line.isEmpty()) {
                        emptyLineCount++;
                        continue; // SSE 格式中事件之间有空行
                    }
                    
                    if (!line.startsWith("data:")) {
                        // 可能是 event: 或其他 SSE 字段
                        log.debug("Non-data line: {}", line);
                        continue;
                    }
                    
                    String data = line.substring(5).trim();
                    if ("[DONE]".equals(data)) {
                        log.info("Received [DONE] marker for session: {}, total_lines={}", sessionId, lineCount);
                        break;
                    }
                    
                    log.debug("Processing stream chunk, event data length: {}", data.length());
                    
                    try {
                        DifyStreamChunk chunk = JSONUtil.toBean(data, DifyStreamChunk.class);
                        String event = chunk.getEvent();
                        log.debug("Received event: {}, answer_length: {}", event, 
                                chunk.getAnswer() == null ? 0 : chunk.getAnswer().length());
                        
                        if ("message".equals(event) || "agent_message".equals(event)) {
                            String answer = chunk.getAnswer();
                            if (StringUtils.hasText(answer)) {
                                fullAnswer.append(answer);
                                chunkCount[0]++;
                                
                                // 实时推送给前端
                                if (callback != null) {
                                    Map<String, Object> messageChunk = new HashMap<>();
                                    messageChunk.put("event", event);
                                    messageChunk.put("session_id", sessionId);
                                    messageChunk.put("answer", answer);
                                    callback.onMessage(JSONUtil.toJsonStr(messageChunk));
                                }
                            }
                        } else if ("message_end".equals(event)) {
                            // 消息结束，获取元数据
                            if (chunk.getMetadata() != null) {
                                Object usage = chunk.getMetadata().get("usage");
                                if (usage instanceof Map) {
                                    Map<String, Object> usageMap = (Map<String, Object>) usage;
                                    Object totalTokens = usageMap.get("total_tokens");
                                    if (totalTokens instanceof Number) {
                                        tokenCount[0] = ((Number) totalTokens).intValue();
                                    }
                                }
                            }
                        } else if ("message_replace".equals(event)) {
                            // 消息替换（较少见）
                            if (StringUtils.hasText(chunk.getAnswer())) {
                                fullAnswer.setLength(0);
                                fullAnswer.append(chunk.getAnswer());
                            }
                        }
                        
                        // 更新 conversation_id
                        if (StringUtils.hasText(chunk.getConversation_id())) {
                            finalConversationId[0] = chunk.getConversation_id();
                        }
                        
                    } catch (Exception e) {
                        log.warn("Parse stream chunk failed: {}", data, e);
                    }
                }
            }
            
            response.close();
            log.info("Dify stream finished, session={}, chunks={}, total_length={}", 
                    sessionId, chunkCount[0], fullAnswer.length());
            
            // 绑定 Dify conversation_id
            if (StringUtils.hasText(finalConversationId[0])) {
                aiAvatarChatHistoryService.bindDifyConversationId(sessionId, userMessage.getId(), finalConversationId[0]);
            }
            
            // 保存 AI 回复
            String answerText = fullAnswer.toString();
            AiAvatarChatHistory aiResponse = new AiAvatarChatHistory();
            aiResponse.setUserId(userId);
            aiResponse.setAiAvatarId(aiAvatarId);
            aiResponse.setSessionId(sessionId);
            aiResponse.setMessageType("ai");
            aiResponse.setContent(answerText);
            aiResponse.setTokens(tokenCount[0]);
            aiResponse.setDifyConversationId(finalConversationId[0]);
            aiResponse.setCreateTime(new java.util.Date());
            
            if (!aiAvatarChatHistoryService.save(aiResponse)) {
                throw new RuntimeException("Save AI response failed");
            }
            
            // 完成回调
            if (callback != null) {
                callback.onComplete(answerText);
            }
            
            return aiResponse;
            
        } catch (Exception e) {
            log.error("Dify streaming chat failed, session={}", sessionId, e);
            if (callback != null) {
                callback.onError(e);
            }
            throw new RuntimeException("Streaming chat failed: " + e.getMessage(), e);
        } finally {
            log.info("sendChatMessageStreaming finished for session: {}", sessionId);
        }
    }

    /**
     * 处理普通聊天请求并返回前端消息 VO。
     *
     * @param userId 当前用户 id
     * @param aiAvatarId AI 智能体 id
     * @param sessionId 本地会话 id
     * @param content 用户消息内容
     * @param endChat 是否结束会话并生成摘要
     * @param chatHistoryService 聊天历史服务
     * @param aiAvatarService AI 智能体服务
     * @param userService 用户服务
     * @return AI 回复消息 VO
     */
    @Override
    public ChatMessageVO handleSendMessageRequest(Long userId, Long aiAvatarId, String sessionId, String content,
                                                  boolean endChat, AiAvatarChatHistoryService chatHistoryService,
                                                  AiAvatarService aiAvatarService, UserService userService) {
        // 校验用户、智能体和消息内容，缺少核心参数时直接终止
        if (userId == null || aiAvatarId == null || !StringUtils.hasText(content)) {
            throw new RuntimeException("Invalid request params");
        }

        // 没有传入会话时创建新会话，传入会话时校验用户是否有访问权限
        String effectiveSessionId = StringUtils.hasText(sessionId) ? sessionId : chatHistoryService.createNewSession(userId, aiAvatarId);
        if (!chatHistoryService.hasSessionAccess(effectiveSessionId, userId, aiAvatarId)) {
            throw new RuntimeException("Session access denied");
        }

        // 加载智能体并校验 Dify 调用配置是否完整
        AiAvatar aiAvatar = aiAvatarService.getById(aiAvatarId);
        if (aiAvatar == null) {
            throw new RuntimeException("AI avatar not found");
        }
        if (!StringUtils.hasText(aiAvatar.getBaseUrl()) || !StringUtils.hasText(aiAvatar.getAvatarAuth())) {
            throw new RuntimeException("AI avatar config is incomplete");
        }

        // 发送消息到 Dify，并将 AI 回复转换为前端展示对象
        AiAvatarChatHistory response = sendChatMessage(userId, aiAvatarId, effectiveSessionId, content,
                aiAvatar.getBaseUrl(), aiAvatar.getAvatarAuth());
        ChatMessageVO vo = buildChatMessageVO(response, aiAvatar, userService.getById(userId));

        // 会话结束时异步生成并更新会话摘要
        if (endChat) {
            String finalSessionId = StringUtils.hasText(response.getSessionId()) ? response.getSessionId() : effectiveSessionId;
            processSessionSummaryAsync(finalSessionId, aiAvatar.getBaseUrl(), aiAvatar.getAvatarAuth());
        }
        return vo;
    }

    /**
     * 处理普通聊天的流式请求并返回 SSE 连接。
     *
     * @param userId 当前用户 id
     * @param aiAvatarId AI 智能体 id
     * @param sessionId 本地会话 id
     * @param content 用户消息内容
     * @param chatHistoryService 聊天历史服务
     * @param aiAvatarService AI 智能体服务
     * @return SSE 流式响应对象
     */
    @Override
    public SseEmitter handleStreamMessageRequest(Long userId, Long aiAvatarId, String sessionId, String content,
                                                 AiAvatarChatHistoryService chatHistoryService,
                                                 AiAvatarService aiAvatarService) {
        // 校验流式聊天必要参数，防止创建无效 SSE 连接
        if (userId == null || aiAvatarId == null || !StringUtils.hasText(content)) {
            throw new RuntimeException("Invalid stream request params");
        }

        // 创建或复用本地会话，并校验当前用户是否可访问该会话
        String effectiveSessionId = StringUtils.hasText(sessionId) ? sessionId : chatHistoryService.createNewSession(userId, aiAvatarId);
        if (!chatHistoryService.hasSessionAccess(effectiveSessionId, userId, aiAvatarId)) {
            throw new RuntimeException("Session access denied");
        }

        // 加载智能体配置，确保后续可以调用 Dify 流式接口
        AiAvatar aiAvatar = aiAvatarService.getById(aiAvatarId);
        if (aiAvatar == null || !StringUtils.hasText(aiAvatar.getBaseUrl()) || !StringUtils.hasText(aiAvatar.getAvatarAuth())) {
            throw new RuntimeException("AI avatar not found or invalid config");
        }

        // 创建 SSE 连接，并在异步线程中读取 Dify 流式响应
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MILLIS);
        log.info("Created SSE emitter for session: {}, starting async stream", effectiveSessionId);
        CompletableFuture.runAsync(() -> {
            log.info("Async thread started for session: {}", effectiveSessionId);
            try {
                sendChatMessageStreaming(userId, aiAvatarId, effectiveSessionId, content,
                        aiAvatar.getBaseUrl(), aiAvatar.getAvatarAuth(), new DifyStreamCallback() {
                            @Override
                            public void onMessage(String chunk) {
                                safeSendEvent(emitter, "message", chunk);
                            }

                            @Override
                            public void onComplete(String fullResponse) {
                                log.info("Stream complete, session_id={}, answer_length={}", effectiveSessionId, 
                                        fullResponse == null ? 0 : fullResponse.length());
                                safeSendEvent(emitter, "complete", "{\"event\":\"complete\",\"session_id\":\"" + effectiveSessionId + "\"}");
                                safeCompleteEmitter(emitter, null);
                                log.info("SSE emitter completed for session: {}", effectiveSessionId);
                            }

                            @Override
                            public void onError(Throwable error) {
                                String msg = error == null ? "stream error" : error.getMessage();
                                safeSendEvent(emitter, "error", buildErrorPayload(msg));
                                safeCompleteEmitter(emitter, null);
                            }
                        });
            } catch (Exception e) {
                safeSendEvent(emitter, "error", buildErrorPayload(e.getMessage()));
                safeCompleteEmitter(emitter, null);
            }
        });

        // 前端连接超时或异常时关闭 SSE，避免连接资源泄漏
        emitter.onTimeout(() -> safeCompleteEmitter(emitter, null));
        emitter.onError(t -> safeCompleteEmitter(emitter, null));
        return emitter;
    }

    /**
     * 执行 Dify 工作流并保存问答记录。
     *
     * @param userId 当前用户 id
     * @param aiAvatarId AI 智能体 id
     * @param sessionId 本地会话 id
     * @param content 用户问题
     * @param inputs 工作流输入参数
     * @param endChat 是否结束会话并更新摘要
     * @param workflowBaseUrl Dify 工作流地址
     * @param workflowAppKey Dify 工作流密钥
     * @param chatHistoryService 聊天历史服务
     * @param aiAvatarService AI 智能体服务
     * @param userService 用户服务
     * @return AI 回复消息 VO
     */
    @Override
    @Transactional
    public ChatMessageVO handleWorkflowRunRequest(Long userId, Long aiAvatarId, String sessionId, String content,
                                                  Map<String, Object> inputs, boolean endChat, String workflowBaseUrl,
                                                  String workflowAppKey, AiAvatarChatHistoryService chatHistoryService,
                                                  AiAvatarService aiAvatarService, UserService userService) {
        // 校验工作流调用参数和应用密钥，确保 inputs 可用于 Dify Workflow
        validateWorkflowHandleParams(userId, aiAvatarId, content, inputs, workflowAppKey);

        // 创建或复用本地会话，并校验当前用户是否有权写入该会话
        String effectiveSessionId = StringUtils.hasText(sessionId) ? sessionId : chatHistoryService.createNewSession(userId, aiAvatarId);
        if (!chatHistoryService.hasSessionAccess(effectiveSessionId, userId, aiAvatarId)) {
            throw new RuntimeException("Session access denied");
        }

        // 加载智能体基础信息，用于最终组装返回给前端的消息 VO
        AiAvatar aiAvatar = aiAvatarService.getById(aiAvatarId);
        if (aiAvatar == null) {
            throw new RuntimeException("AI avatar not found");
        }

        // 先保存用户问题，保证工作流调用前聊天记录已经落库
        AiAvatarChatHistory userMessage = chatMessageHelper.createUserMessage(userId, aiAvatarId, effectiveSessionId, content);
        if (!chatHistoryService.save(userMessage)) {
            throw new RuntimeException("Save user workflow message failed");
        }

        // 将本地会话 id 写入工作流变量，便于 Dify 侧串联上下文
        Map<String, Object> workflowInputs = new HashMap<>(inputs);
        workflowInputs.put("session_id", effectiveSessionId);
        WorkflowExecutionResult workflowResult = runWorkflow(userId, workflowInputs, workflowBaseUrl, workflowAppKey);

        // 将工作流输出转换为 AI 回复记录并保存
        AiAvatarChatHistory aiResponse = createWorkflowAiResponse(userId, aiAvatarId, effectiveSessionId,
                workflowResult.getAnswer(), workflowResult.getTokens());
        if (!chatHistoryService.save(aiResponse)) {
            throw new RuntimeException("Save workflow AI response failed");
        }

        // 结束会话时用本轮问答内容更新会话摘要
        if (endChat) {
            chatHistoryService.updateSessionSummary(effectiveSessionId,
                    buildWorkflowSessionSummary(content, workflowResult.getAnswer()));
        }

        return buildChatMessageVO(aiResponse, aiAvatar, userService.getById(userId));
    }

    /**
     * 执行 Dify 工作流流式请求并返回 SSE 连接。
     *
     * @param userId 当前用户 id
     * @param aiAvatarId AI 智能体 id
     * @param sessionId 本地会话 id
     * @param content 用户问题
     * @param inputs 工作流输入参数
     * @param endChat 是否结束会话并更新摘要
     * @param workflowBaseUrl Dify 工作流地址
     * @param workflowAppKey Dify 工作流密钥
     * @param chatHistoryService 聊天历史服务
     * @param aiAvatarService AI 智能体服务
     * @return SSE 流式响应对象
     */
    @Override
    public SseEmitter handleWorkflowStreamRequest(Long userId, Long aiAvatarId, String sessionId, String content,
                                                  Map<String, Object> inputs, boolean endChat,
                                                  String workflowBaseUrl, String workflowAppKey,
                                                  AiAvatarChatHistoryService chatHistoryService,
                                                  AiAvatarService aiAvatarService) {
        // 校验工作流流式调用参数，确保可以创建有效的异步任务
        validateWorkflowHandleParams(userId, aiAvatarId, content, inputs, workflowAppKey);

        // 创建或复用本地会话，并校验当前用户是否有权访问
        String effectiveSessionId = StringUtils.hasText(sessionId) ? sessionId : chatHistoryService.createNewSession(userId, aiAvatarId);
        if (!chatHistoryService.hasSessionAccess(effectiveSessionId, userId, aiAvatarId)) {
            throw new RuntimeException("Session access denied");
        }

        // 加载智能体，确保本轮回复可以归属到有效智能体
        AiAvatar aiAvatar = aiAvatarService.getById(aiAvatarId);
        if (aiAvatar == null) {
            throw new RuntimeException("AI avatar not found");
        }

        // 流式工作流启动前先保存用户问题
        AiAvatarChatHistory userMessage = chatMessageHelper.createUserMessage(userId, aiAvatarId, effectiveSessionId, content);
        if (!chatHistoryService.save(userMessage)) {
            throw new RuntimeException("Save user workflow message failed");
        }

        // 将会话 id 放入工作流输入，供 Dify 侧识别当前上下文
        Map<String, Object> workflowInputs = new HashMap<>(inputs);
        workflowInputs.put("session_id", effectiveSessionId);

        // 创建 SSE 连接，并在异步线程中消费 Dify Workflow 的流式输出
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MILLIS);
        CompletableFuture.runAsync(() -> {
            try {
                WorkflowExecutionResult result = sendWorkflowStreaming(
                        userId,
                        workflowInputs,
                        workflowBaseUrl,
                        workflowAppKey,
                        effectiveSessionId,
                        new DifyStreamCallback() {
                            @Override
                            public void onMessage(String chunk) {
                                safeSendEvent(emitter, "message", chunk);
                            }

                            @Override
                            public void onComplete(String fullResponse) {
                                safeSendEvent(emitter, "complete", buildWorkflowCompletePayload(effectiveSessionId, fullResponse));
                            }

                            @Override
                            public void onError(Throwable error) {
                                String msg = error == null ? "workflow stream error" : error.getMessage();
                                safeSendEvent(emitter, "error", buildErrorPayload(msg));
                            }
                        }
                );

                // 流式结束后保存完整 AI 回复，并按需更新会话摘要
                AiAvatarChatHistory aiResponse = createWorkflowAiResponse(userId, aiAvatarId, effectiveSessionId,
                        result.getAnswer(), result.getTokens());
                if (!chatHistoryService.save(aiResponse)) {
                    throw new RuntimeException("Save workflow AI response failed");
                }
                if (endChat) {
                    chatHistoryService.updateSessionSummary(effectiveSessionId,
                            buildWorkflowSessionSummary(content, result.getAnswer()));
                }
            } catch (Exception e) {
                log.error("Workflow stream async task failed for session: {}", effectiveSessionId, e);
                safeSendEvent(emitter, "error", buildErrorPayload(e.getMessage()));
            } finally {
                safeCompleteEmitter(emitter, null);
            }
        });

        // 前端连接超时或异常时关闭 SSE，避免异步连接残留
        emitter.onTimeout(() -> safeCompleteEmitter(emitter, null));
        emitter.onError(t -> safeCompleteEmitter(emitter, t));
        return emitter;
    }

    @Override
    public String getSessionSummary(String sessionId, String baseUrl, String avatarAuth) {
        if (!StringUtils.hasText(sessionId) || !StringUtils.hasText(baseUrl) || !StringUtils.hasText(avatarAuth)) {
            throw new RuntimeException("Invalid summary params");
        }

        String difyConversationId = aiAvatarChatHistoryService.getLatestDifyConversationId(sessionId);
        if (!StringUtils.hasText(difyConversationId)) {
            return "Chat summary";
        }

        String apiUrl = appendApiPath(baseUrl, "/chat-messages/summarize");
        JSONObject payload = new JSONObject();
        payload.set("conversation_id", difyConversationId);

        Response response = okHttpUtils.postJson(apiUrl, payload.toString(), buildAuthHeaders(avatarAuth));
        try {
            if (!response.isSuccessful()) {
                String body = readBodySafely(response.body());
                throw new RuntimeException("Get summary failed: " + response.code() + ", body=" + body);
            }
            String body = readBodySafely(response.body());
            JSONObject json = JSONUtil.parseObj(body);
            String summary = json.getStr("summary");
            return StringUtils.hasText(summary) ? summary : "Chat summary";
        } finally {
            response.close();
        }
    }

    /**
     * 删除 Dify 侧与本地会话绑定的 conversation。
     *
     * @param userId 当前用户 id
     * @param sessionId 本地会话 id
     * @param baseUrl Dify 应用地址
     * @param avatarAuth Dify 应用密钥
     * @return 是否删除成功
     */
    @Override
    public boolean deleteConversation(Long userId, String sessionId, String baseUrl, String avatarAuth) {
        // 参数不完整时无法定位 Dify 会话，直接返回失败
        if (userId == null || !StringUtils.hasText(sessionId)
                || !StringUtils.hasText(baseUrl) || !StringUtils.hasText(avatarAuth)) {
            return false;
        }

        // 本地没有绑定 Dify conversation_id 时视为无需删除
        String difyConversationId = aiAvatarChatHistoryService.getLatestDifyConversationId(sessionId);
        if (!StringUtils.hasText(difyConversationId)) {
            log.info("Skip deleteConversation because no dify conversation mapping exists. localSessionId={}", sessionId);
            return true;
        }

        // 调用 Dify 删除接口，并携带用户标识满足 Dify API 要求
        String apiUrl = appendApiPath(baseUrl, "/conversations/" + difyConversationId);
        JSONObject payload = new JSONObject();
        payload.set("user", difyConfig.getUserPrefix() + userId);

        Response response = okHttpUtils.delete(apiUrl, payload.toString(), buildAuthHeaders(avatarAuth));
        try {
            if (response.code() == 404) {
                return true;
            }
            if (!response.isSuccessful()) {
                String body = readBodySafely(response.body());
                log.warn("Delete conversation failed. code={}, body={}", response.code(), body);
                return false;
            }
            String body = readBodySafely(response.body());
            if (!StringUtils.hasText(body)) {
                return true;
            }
            JSONObject json = JSONUtil.parseObj(body);
            String result = json.getStr("result");
            return !StringUtils.hasText(result) || "success".equalsIgnoreCase(result);
        } finally {
            response.close();
        }
    }

    /**
     * 停止 Dify 正在生成的流式回复任务。
     *
     * @param userId 当前用户 id
     * @param taskId Dify 流式任务 id
     * @param baseUrl Dify 应用地址
     * @param avatarAuth Dify 应用密钥
     * @return 是否停止成功
     */
    @Override
    public boolean stopStreamingResponse(Long userId, String taskId, String baseUrl, String avatarAuth) {
        // 参数不完整时无法定位 Dify 流式任务，直接返回失败
        if (userId == null || !StringUtils.hasText(taskId)
                || !StringUtils.hasText(baseUrl) || !StringUtils.hasText(avatarAuth)) {
            return false;
        }

        // 调用 Dify stop 接口，并通过 result 字段判断是否成功
        String apiUrl = appendApiPath(baseUrl, "/chat-messages/" + taskId + "/stop");
        Map<String, Object> payload = new HashMap<>();
        payload.put("user", difyConfig.getUserPrefix() + userId);

        Response response = okHttpUtils.postJson(apiUrl, JSONUtil.toJsonStr(payload), buildAuthHeaders(avatarAuth));
        try {
            if (!response.isSuccessful()) {
                return false;
            }
            String body = readBodySafely(response.body());
            if (!StringUtils.hasText(body)) {
                return true;
            }
            JSONObject json = JSONUtil.parseObj(body);
            String result = json.getStr("result");
            return !StringUtils.hasText(result) || "success".equalsIgnoreCase(result);
        } finally {
            response.close();
        }
    }
    private WorkflowExecutionResult runWorkflow(Long userId, Map<String, Object> inputs,
                                                String workflowBaseUrl, String workflowAppKey) {
        validateWorkflowParams(userId, inputs, workflowAppKey);
        DifyWorkflowRunRequest request = buildWorkflowRequest(userId, inputs, "blocking");
        String apiUrl = appendApiPath(workflowBaseUrl, difyConfig.getWorkflowRunPath());
        Response response = okHttpUtils.postJson(apiUrl, JSONUtil.toJsonStr(request), buildAuthHeaders(workflowAppKey));
        try {
            if (!response.isSuccessful()) {
                String body = readBodySafely(response.body());
                throw new RuntimeException("Dify workflow request failed: " + response.code() + ", body=" + body);
            }
            String body = readBodySafely(response.body());
            DifyWorkflowRunResponse workflowResponse = JSONUtil.toBean(body, DifyWorkflowRunResponse.class);
            if (workflowResponse == null || workflowResponse.getData() == null) {
                throw new RuntimeException("Dify workflow response is empty");
            }
            if (StringUtils.hasText(workflowResponse.getData().getError())) {
                throw new RuntimeException("Dify workflow failed: " + workflowResponse.getData().getError());
            }
            String answer = extractWorkflowAnswer(workflowResponse.getData().getOutputs());
            WorkflowExecutionResult result = new WorkflowExecutionResult();
            result.setAnswer(answer);
            result.setTokens(defaultInteger(workflowResponse.getData().getTotal_tokens()));
            return result;
        } finally {
            response.close();
        }
    }

    private WorkflowExecutionResult sendWorkflowStreaming(Long userId, Map<String, Object> inputs,
                                                          String workflowBaseUrl, String workflowAppKey,
                                                          String sessionId, DifyStreamCallback callback) {
        try {
            return runWorkflowStreamingInternal(userId, inputs, workflowBaseUrl, workflowAppKey, sessionId, callback);
        } catch (Exception e) {
            if (callback != null) {
                callback.onError(e);
            }
            throw e;
        }
    }

    private WorkflowExecutionResult runWorkflowStreamingInternal(Long userId, Map<String, Object> inputs,
                                                                 String workflowBaseUrl, String workflowAppKey,
                                                                 String sessionId, DifyStreamCallback callback) {
        validateWorkflowParams(userId, inputs, workflowAppKey);
        DifyWorkflowRunRequest request = buildWorkflowRequest(userId, inputs, "streaming");
        String apiUrl = appendApiPath(workflowBaseUrl, difyConfig.getWorkflowRunPath());
        Response response = okHttpUtils.postJsonStream(apiUrl, JSONUtil.toJsonStr(request), buildAuthHeaders(workflowAppKey));
        try {
            if (!response.isSuccessful()) {
                String body = readBodySafely(response.body());
                throw new RuntimeException("Dify workflow stream request failed: " + response.code() + ", body=" + body);
            }
            return consumeWorkflowStream(response.body(), callback, sessionId);
        } finally {
            response.close();
        }
    }

    private WorkflowExecutionResult consumeWorkflowStream(ResponseBody body,
                                                          DifyStreamCallback callback,
                                                          String sessionId) {
        if (body == null) {
            throw new RuntimeException("Dify workflow stream body is empty");
        }
        WorkflowExecutionResult result = new WorkflowExecutionResult();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(body.byteStream(), StandardCharsets.UTF_8))) {
            String line;
            StringBuilder payloadBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data:")) {
                    if (payloadBuilder.length() > 0) {
                        payloadBuilder.append('\n');
                    }
                    payloadBuilder.append(line.substring(5).trim());
                    continue;
                }
                if (line.isEmpty()) {
                    processWorkflowStreamPayload(payloadBuilder.toString(), callback, sessionId, result);
                    payloadBuilder.setLength(0);
                }
            }
            if (payloadBuilder.length() > 0) {
                processWorkflowStreamPayload(payloadBuilder.toString(), callback, sessionId, result);
            }
        } catch (IOException e) {
            throw new RuntimeException("Read Dify workflow stream failed", e);
        }
        return result;
    }

    private void processWorkflowStreamPayload(String payload,
                                              DifyStreamCallback callback,
                                              String sessionId,
                                              WorkflowExecutionResult result) {
        if (!StringUtils.hasText(payload) || "[DONE]".equalsIgnoreCase(payload.trim())) {
            return;
        }
        DifyWorkflowStreamChunk chunk = JSONUtil.toBean(payload, DifyWorkflowStreamChunk.class);
        if (chunk == null) {
            return;
        }
        if ("error".equalsIgnoreCase(chunk.getEvent())) {
            throw new RuntimeException(StringUtils.hasText(chunk.getMessage()) ? chunk.getMessage() : "workflow stream error");
        }

        String answer = extractWorkflowAnswerFromChunk(chunk);
        if (StringUtils.hasText(answer) && !answer.equals(result.getLastEmittedAnswer())) {
            result.setLastEmittedAnswer(answer);
            if (callback != null) {
                callback.onMessage(buildWorkflowStreamMessagePayload(chunk, sessionId, answer));
            }
        }

        if ("workflow_finished".equalsIgnoreCase(chunk.getEvent())) {
            if (!StringUtils.hasText(answer)) {
                answer = extractWorkflowAnswer(toMap(chunk.getData() == null ? null : chunk.getData().get("outputs")));
            }
            if (!StringUtils.hasText(answer)) {
                answer = result.getLastEmittedAnswer();
            }
            result.setAnswer(defaultText(answer));
            result.setTokens(defaultInteger(toInteger(chunk.getData() == null ? null : chunk.getData().get("total_tokens"))));
            if (callback != null) {
                callback.onComplete(result.getAnswer());
            }
        }
    }

    private DifyChatRequest buildChatRequest(Long userId, String difyConversationId, String content) {
        DifyChatRequest request = new DifyChatRequest();
        request.setQuery(content);
        request.setInputs(new HashMap<>());
        request.setResponse_mode("blocking");
        request.setUser(difyConfig.getUserPrefix() + userId);
        request.setConversation_id(StringUtils.hasText(difyConversationId) ? difyConversationId : null);
        request.setAuto_generate_name(true);
        return request;
    }

    private String buildAvatarScopedQuery(Long aiAvatarId, String userQuestion) {
        if (!StringUtils.hasText(userQuestion) || aiAvatarId == null) {
            return userQuestion;
        }
        AiAvatar aiAvatar = aiAvatarService.getById(aiAvatarId);
        if (aiAvatar == null || !StringUtils.hasText(aiAvatar.getDescription())) {
            return userQuestion;
        }
        String avatarName = StringUtils.hasText(aiAvatar.getName()) ? aiAvatar.getName().trim() : "当前智能体";
        String avatarDescription = aiAvatar.getDescription().trim();
        return "你当前扮演的智能体信息如下：\n"
                + "智能体名称：" + avatarName + "\n"
                + "智能体描述：" + avatarDescription + "\n"
                + "上述描述是该智能体的角色设定和回答关键词，请严格围绕该设定回答用户问题。\n\n"
                + "用户问题：\n" + userQuestion.trim();
    }

    private DifyWorkflowRunRequest buildWorkflowRequest(Long userId, Map<String, Object> inputs, String responseMode) {
        DifyWorkflowRunRequest request = new DifyWorkflowRunRequest();
        request.setInputs(inputs == null ? new HashMap<>() : new HashMap<>(inputs));
        request.setResponse_mode(responseMode);
        request.setUser(difyConfig.getUserPrefix() + userId);
        return request;
    }

    private boolean shouldRetryWithNewConversation(int httpCode,
                                                   String responseBody,
                                                   boolean retriedWithNewConversation) {
        if (retriedWithNewConversation || httpCode != 404) {
            return false;
        }
        return responseBody != null && responseBody.toLowerCase().contains("conversation not exists");
    }

    private void validateSendParams(Long userId, Long aiAvatarId, String content,
                                    String baseUrl, String avatarAuth) {
        if (userId == null || aiAvatarId == null || !StringUtils.hasText(content)
                || !StringUtils.hasText(baseUrl) || !StringUtils.hasText(avatarAuth)) {
            throw new RuntimeException("Invalid chat params");
        }
    }

    private void validateWorkflowHandleParams(Long userId, Long aiAvatarId, String content,
                                              Map<String, Object> inputs, String workflowAppKey) {
        if (userId == null || aiAvatarId == null || !StringUtils.hasText(content)) {
            throw new RuntimeException("Invalid workflow request params");
        }
        validateWorkflowParams(userId, inputs, workflowAppKey);
    }

    private void validateWorkflowParams(Long userId, Map<String, Object> inputs, String workflowAppKey) {
        if (userId == null || inputs == null || inputs.isEmpty() || !StringUtils.hasText(workflowAppKey)) {
            throw new RuntimeException("Invalid workflow params");
        }
    }

    private String resolveDifyConversationId(String requestConversationId, String responseConversationId) {
        if (StringUtils.hasText(responseConversationId)) {
            return responseConversationId;
        }
        return StringUtils.hasText(requestConversationId) ? requestConversationId : null;
    }
    private ChatMessageVO buildChatMessageVO(AiAvatarChatHistory response, AiAvatar aiAvatar, User user) {
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(response, vo);
        if (aiAvatar != null) {
            vo.setAiAvatarName(aiAvatar.getName());
            vo.setAiAvatarImgUrl(aiAvatar.getAvatarImgUrl());
        }
        if (user != null) {
            vo.setUserName(user.getUserName());
            vo.setUserAvatar(user.getUserAvatar());
        }
        return vo;
    }

    private AiAvatarChatHistory createWorkflowAiResponse(Long userId, Long aiAvatarId, String sessionId,
                                                         String answer, Integer tokens) {
        AiAvatarChatHistory aiMessage = new AiAvatarChatHistory();
        aiMessage.setUserId(userId);
        aiMessage.setAiAvatarId(aiAvatarId);
        aiMessage.setSessionId(sessionId);
        aiMessage.setMessageType("ai");
        aiMessage.setContent(defaultText(answer));
        aiMessage.setTokens(defaultInteger(tokens));
        return aiMessage;
    }

    private String buildWorkflowSessionSummary(String question, String answer) {
        String source = StringUtils.hasText(question) ? question.trim() : defaultText(answer);
        if (!StringUtils.hasText(source)) {
            return "Course learning assistant session";
        }
        return source.length() <= 50 ? source : source.substring(0, 50);
    }

    private Map<String, String> buildAuthHeaders(String apiKey) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + apiKey);
        return headers;
    }

    private String appendApiPath(String baseUrl, String path) {
        String effectiveBaseUrl = StringUtils.hasText(baseUrl) ? baseUrl.trim() : difyConfig.getBaseUrl();
        if (!StringUtils.hasText(effectiveBaseUrl)) {
            throw new RuntimeException("Dify baseUrl is blank");
        }
        if (effectiveBaseUrl.endsWith("/")) {
            effectiveBaseUrl = effectiveBaseUrl.substring(0, effectiveBaseUrl.length() - 1);
        }
        String effectivePath = StringUtils.hasText(path) ? path.trim() : "";
        if (!effectivePath.startsWith("/")) {
            effectivePath = "/" + effectivePath;
        }
        return effectiveBaseUrl + effectivePath;
    }

    private String readBodySafely(ResponseBody body) {
        if (body == null) {
            return "";
        }
        try {
            return body.string();
        } catch (IOException e) {
            throw new RuntimeException("Read response body failed", e);
        }
    }

    private String extractWorkflowAnswerFromChunk(DifyWorkflowStreamChunk chunk) {
        if (chunk == null) {
            return "";
        }
        return extractWorkflowAnswer(toMap(chunk.getData() == null ? null : chunk.getData().get("outputs")));
    }

    private String extractWorkflowAnswer(Map<String, Object> outputs) {
        if (outputs == null || outputs.isEmpty()) {
            return "";
        }
        List<String> priorityKeys = new ArrayList<>();
        priorityKeys.add("answer");
        priorityKeys.add("reply");
        priorityKeys.add("result");
        priorityKeys.add("output");
        priorityKeys.add("text");
        priorityKeys.add("content");
        priorityKeys.add("final_answer");
        priorityKeys.add("response");
        for (String key : priorityKeys) {
            String value = objectToText(outputs.get(key));
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        for (Object value : outputs.values()) {
            String text = objectToText(value);
            if (StringUtils.hasText(text)) {
                return text;
            }
        }
        return JSONUtil.toJsonStr(outputs);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object value) {
        if (value == null) {
            return new HashMap<>();
        }
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        if (value instanceof JSONObject) {
            return ((JSONObject) value);
        }
        return JSONUtil.parseObj(value);
    }

    @SuppressWarnings("unchecked")
    private String objectToText(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String) {
            return ((String) value).trim();
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Map) {
            return extractWorkflowAnswer((Map<String, Object>) value);
        }
        if (value instanceof Iterable) {
            StringBuilder builder = new StringBuilder();
            for (Object item : (Iterable<?>) value) {
                String text = objectToText(item);
                if (!StringUtils.hasText(text)) {
                    continue;
                }
                if (builder.length() > 0) {
                    builder.append('\n');
                }
                builder.append(text);
            }
            return builder.toString();
        }
        return JSONUtil.toJsonStr(value);
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String && StringUtils.hasText((String) value)) {
            try {
                return Integer.parseInt(((String) value).trim());
            } catch (NumberFormatException e) {
                log.warn("parse workflow token failed, value={}", value);
            }
        }
        return 0;
    }

    private Integer defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private String defaultText(String value) {
        return value == null ? "" : value.trim();
    }

    private String buildWorkflowStreamMessagePayload(DifyWorkflowStreamChunk chunk,
                                                     String sessionId,
                                                     String answer) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", chunk.getEvent());
        payload.put("task_id", chunk.getTask_id());
        payload.put("workflow_run_id", chunk.getWorkflow_run_id());
        payload.put("conversation_id", sessionId);
        payload.put("answer", defaultText(answer));
        return JSONUtil.toJsonStr(payload);
    }

    private String buildWorkflowCompletePayload(String sessionId, String answer) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "complete");
        payload.put("conversation_id", sessionId);
        payload.put("answer", defaultText(answer));
        return JSONUtil.toJsonStr(payload);
    }

    private String buildErrorPayload(String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "error");
        payload.put("message", defaultText(message));
        return JSONUtil.toJsonStr(payload);
    }

    private void processSessionSummaryAsync(String sessionId, String baseUrl, String avatarAuth) {
        CompletableFuture.runAsync(() -> {
            try {
                String summary = getSessionSummary(sessionId, baseUrl, avatarAuth);
                aiAvatarChatHistoryService.updateSessionSummary(sessionId, summary);
            } catch (Exception e) {
                log.warn("Process session summary failed, sessionId={}", sessionId, e);
            }
        });
    }

    private void safeSendEvent(SseEmitter emitter, String name, String data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(name)
                    .id(name + "-" + System.currentTimeMillis())
                    .data(data));
        } catch (Exception ignored) {
            // no-op
        }
    }

    private void safeCompleteEmitter(SseEmitter emitter, Throwable error) {
        if (error != null) {
            log.warn("complete sse emitter with previous error: {}", error.getMessage());
        }
        try {
            emitter.complete();
        } catch (Exception ignored) {
            // no-op
        }
    }
    private static class WorkflowExecutionResult {

        private String answer;

        private Integer tokens;

        private String lastEmittedAnswer;

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public Integer getTokens() {
            return tokens;
        }

        public void setTokens(Integer tokens) {
            this.tokens = tokens;
        }

        public String getLastEmittedAnswer() {
            return lastEmittedAnswer;
        }

        public void setLastEmittedAnswer(String lastEmittedAnswer) {
            this.lastEmittedAnswer = lastEmittedAnswer;
        }
    }
}
