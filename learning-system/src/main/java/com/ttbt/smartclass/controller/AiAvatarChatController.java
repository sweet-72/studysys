package com.ttbt.smartclass.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.model.dto.chat.ChatMessageAddRequest;
import com.ttbt.smartclass.model.dto.chat.ChatSessionUpdateRequest;
import com.ttbt.smartclass.model.dto.chat.StopStreamingRequest;
import com.ttbt.smartclass.model.entity.AiAvatar;
import com.ttbt.smartclass.model.entity.AiAvatarChatHistory;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.ChatMessageVO;
import com.ttbt.smartclass.model.vo.ChatSessionVO;
import com.ttbt.smartclass.service.AiAvatarChatHistoryService;
import com.ttbt.smartclass.service.AiAvatarService;
import com.ttbt.smartclass.service.DifyService;
import com.ttbt.smartclass.service.UserAiAvatarService;
import com.ttbt.smartclass.service.UserService;
import com.ttbt.smartclass.utils.SseEmitterUtils;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 智能体聊天控制器。
 * 负责 AI 智能体会话创建、消息发送、流式回复和聊天历史查询等接口。
 */
@RestController
@RequestMapping("/chat")
@Slf4j
public class AiAvatarChatController {

    @Resource
    private AiAvatarChatHistoryService aiAvatarChatHistoryService;

    @Resource
    private AiAvatarService aiAvatarService;

    @Resource
    private UserAiAvatarService userAiAvatarService;

    @Resource
    private UserService userService;

    @Resource
    private DifyService difyService;

    /**
     * 创建当前用户与指定 AI 智能体的聊天会话。
     *
     * @param aiAvatarId AI 智能体 id
     * @param request 当前 HTTP 请求
     * @return 新创建的会话 id
     */
    @PostMapping("/session/create")
    public BaseResponse<String> createSession(@RequestParam Long aiAvatarId, HttpServletRequest request) {
        // 校验智能体 id，避免创建无效会话
        if (aiAvatarId == null || aiAvatarId <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前登录用户，并记录本次智能体使用次数
        User loginUser = userService.getLoginUser(request);
        userAiAvatarService.useAiAvatar(loginUser.getId(), aiAvatarId);

        // 为当前用户和智能体创建新的本地会话
        String sessionId = aiAvatarChatHistoryService.createNewSession(loginUser.getId(), aiAvatarId);
        return ResultUtils.success(sessionId);
    }

    /**
     * 向指定 AI 智能体发送普通聊天消息。
     *
     * @param req 聊天消息请求
     * @param request 当前 HTTP 请求
     * @return AI 回复消息
     */
    @PostMapping("/message/send")
    public BaseResponse<ChatMessageVO> sendMessage(@RequestBody ChatMessageAddRequest req,
                                                   HttpServletRequest request) {
        // 校验智能体 id 和消息内容，避免无效请求进入 Dify 调用
        if (req == null || req.getAiAvatarId() == null || req.getAiAvatarId() <= 0
                || StringUtils.isBlank(req.getContent())) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "Invalid request params");
        }

        // 获取当前登录用户，消息归属和会话权限都以该用户为准
        User loginUser = userService.getLoginUser(request);
        // 如果前端传入已有会话，先确认当前用户有权继续使用该会话
        if (StringUtils.isNotBlank(req.getSessionId())
                && !hasSessionAccess(req.getSessionId(), loginUser, req.getAiAvatarId())) {
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "No permission for this session");
        }

        try {
            // 委托 Dify 服务完成消息发送、聊天记录落库和回复 VO 构建
            ChatMessageVO data = difyService.handleSendMessageRequest(
                    loginUser.getId(),
                    req.getAiAvatarId(),
                    req.getSessionId(),
                    req.getContent(),
                    req.isEndChat(),
                    aiAvatarChatHistoryService,
                    aiAvatarService,
                    userService
            );
            return ResultUtils.success(data);
        } catch (Exception e) {
            // 捕获 Dify 调用或消息落库异常，统一转换为接口错误响应
            log.error("sendMessage failed", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "Send message failed: " + e.getMessage());
        }
    }

    /**
     * 发送 AI 智能体流式聊天消息。
     *
     * @param req 聊天消息请求
     * @param request 当前 HTTP 请求
     * @return SSE 流式响应对象
     */
    @PostMapping(value = "/message/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStream(@RequestBody ChatMessageAddRequest req, HttpServletRequest request) {
        try {
            // 校验 AI 智能体 ID 和用户输入内容，避免无效请求进入流式调用
            if (req == null || req.getAiAvatarId() == null || req.getAiAvatarId() <= 0
                    || StringUtils.isBlank(req.getContent())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid stream request params");
            }

            // 获取当前登录用户，用于记录聊天归属并校验会话权限
            User loginUser = userService.getLoginUser(request);
            // 如果前端传入已有会话，必须确认该会话属于当前用户或当前用户具备访问权限
            if (StringUtils.isNotBlank(req.getSessionId())
                    && !hasSessionAccess(req.getSessionId(), loginUser, req.getAiAvatarId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "No permission for this session");
            }

            // 调用 Dify 流式接口生成回复，返回 SSE 连接给前端持续接收内容
            return difyService.handleStreamMessageRequest(
                    loginUser.getId(),
                    req.getAiAvatarId(),
                    req.getSessionId(),
                    req.getContent(),
                    aiAvatarChatHistoryService,
                    aiAvatarService
            );
        } catch (Exception e) {
            log.error("sendMessageStream failed", e);
            // 流式调用失败时仍返回 SSE 对象，让前端能按统一方式接收错误信息
            return SseEmitterUtils.createErrorEmitter(e.getMessage());
        }
    }

    /**
     * 查询指定会话的聊天记录。
     *
     * @param sessionId 会话 id
     * @param request 当前 HTTP 请求
     * @return 会话内的消息列表
     */
    @GetMapping("/history")
    public BaseResponse<List<ChatMessageVO>> getChatHistory(@RequestParam String sessionId,
                                                            HttpServletRequest request) {
        // 会话 id 不能为空，避免查询无归属的聊天记录
        if (StringUtils.isBlank(sessionId)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }

        // 当前用户只能查看自己拥有或有权限访问的会话
        User loginUser = userService.getLoginUser(request);
        if (!hasSessionAccess(sessionId, loginUser, null)) {
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "No permission for this session");
        }

        // 拉取会话历史记录，空列表表示会话不存在或尚无消息
        List<AiAvatarChatHistory> historyList = aiAvatarChatHistoryService.getSessionHistory(sessionId);
        if (historyList.isEmpty()) {
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "Session not found");
        }

        // 将历史实体转换为前端展示所需的聊天消息 VO
        List<ChatMessageVO> vos = historyList.stream().map(item -> {
            ChatMessageVO vo = new ChatMessageVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());

        return ResultUtils.success(vos);
    }

    /**
     * 查询当前用户的 AI 聊天会话列表。
     *
     * @param aiAvatarId AI 智能体 id，可为空
     * @param request 当前 HTTP 请求
     * @return 当前用户的会话列表
     */
    @GetMapping("/sessions")
    public BaseResponse<List<ChatSessionVO>> getUserSessions(@RequestParam(required = false) Long aiAvatarId,
                                                             HttpServletRequest request) {
        // 以当前登录用户为查询条件，可按 AI 智能体过滤会话
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(aiAvatarChatHistoryService.getUserSessions(loginUser.getId(), aiAvatarId));
    }

    /**
     * 查询当前用户最近的 AI 聊天会话。
     *
     * @param limit 返回数量上限
     * @param request 当前 HTTP 请求
     * @return 最近会话列表
     */
    @GetMapping("/sessions/recent")
    public BaseResponse<List<ChatSessionVO>> getRecentSessions(@RequestParam(defaultValue = "10") int limit,
                                                               HttpServletRequest request) {
        // 按当前登录用户限制查询范围，并按传入数量返回最近会话
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(aiAvatarChatHistoryService.getRecentSessions(loginUser.getId(), limit));
    }

    /**
     * 修改当前用户指定会话的名称。
     *
     * @param req 会话名称更新请求
     * @param request 当前 HTTP 请求
     * @return 是否更新成功
     */
    @PostMapping("/session/update")
    public BaseResponse<Boolean> updateSessionName(@RequestBody ChatSessionUpdateRequest req,
                                                   HttpServletRequest request) {
        // 校验会话 id 和新名称，避免写入空会话名
        if (req == null || StringUtils.isBlank(req.getSessionId()) || StringUtils.isBlank(req.getSessionName())) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }

        // 只允许会话拥有者或管理员修改会话名称
        User loginUser = userService.getLoginUser(request);
        if (!hasSessionAccess(req.getSessionId(), loginUser, null)) {
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "No permission for this session");
        }

        // 先确认会话确实存在，再执行名称更新
        List<AiAvatarChatHistory> checkList = aiAvatarChatHistoryService.getSessionHistory(req.getSessionId());
        if (checkList.isEmpty()) {
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "Session not found");
        }

        return ResultUtils.success(aiAvatarChatHistoryService.updateSessionName(req.getSessionId(), req.getSessionName()));
    }

    /**
     * 删除当前用户指定的聊天会话。
     *
     * @param sessionId 会话 id
     * @param request 当前 HTTP 请求
     * @return 是否删除成功
     */
    @PostMapping("/session/delete")
    public BaseResponse<Boolean> deleteSession(@RequestParam String sessionId,
                                               HttpServletRequest request) {
        // 会话 id 不能为空，删除前需要明确目标会话
        if (StringUtils.isBlank(sessionId)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }

        // 校验当前用户是否有权访问该会话，防止越权删除
        User loginUser = userService.getLoginUser(request);
        if (!hasSessionAccess(sessionId, loginUser, null)) {
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "No permission for this session");
        }

        // 按用户维度删除会话，确保只影响当前用户的数据
        return ResultUtils.success(aiAvatarChatHistoryService.deleteSession(sessionId, loginUser.getId()));
    }

    /**
     * 查询当前用户的聊天消息记录。
     *
     * @param aiAvatarId AI 智能体 id，可为空
     * @param request 当前 HTTP 请求
     * @return 当前用户的聊天消息列表
     */
    @GetMapping("/messages/list")
    public BaseResponse<List<ChatMessageVO>> getUserChatMessages(@RequestParam(required = false) Long aiAvatarId,
                                                                 HttpServletRequest request) {
        // 获取当前登录用户，并按智能体条件查询该用户的消息记录
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(aiAvatarChatHistoryService.getUserMessages(loginUser.getId(), aiAvatarId));
    }

    /**
     * 停止指定 Dify 流式响应任务。
     *
     * @param req 停止流式响应请求
     * @param request 当前 HTTP 请求
     * @return 是否停止成功
     */
    @PostMapping("/message/stop")
    public BaseResponse<Boolean> stopStreamingResponse(@RequestBody StopStreamingRequest req,
                                                       HttpServletRequest request) {
        // 校验智能体 id 和 Dify task_id，缺少任一参数都无法定位流式任务
        if (req == null || req.getAiAvatarId() == null || req.getAiAvatarId() <= 0
                || StringUtils.isBlank(req.getTaskId())) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "Invalid request params");
        }

        // 获取当前用户，并加载智能体的 Dify 调用配置
        User loginUser = userService.getLoginUser(request);
        AiAvatar aiAvatar = aiAvatarService.getById(req.getAiAvatarId());
        if (aiAvatar == null || StringUtils.isAnyBlank(aiAvatar.getBaseUrl(), aiAvatar.getAvatarAuth())) {
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "AI avatar not found or invalid config");
        }

        // 调用 Dify 停止接口，中止当前用户发起的流式回复任务
        boolean ok = difyService.stopStreamingResponse(loginUser.getId(), req.getTaskId(),
                aiAvatar.getBaseUrl(), aiAvatar.getAvatarAuth());
        return ok ? ResultUtils.success(true) : ResultUtils.error(ErrorCode.OPERATION_ERROR, "Stop stream failed");
    }

    /**
     * 分页查询当前用户的 AI 聊天历史。
     *
     * @param current 当前页码
     * @param pageSize 每页数量
     * @param request 当前 HTTP 请求
     * @return 聊天历史分页数据
     */
    @GetMapping("/user/history")
    public BaseResponse<Page<ChatMessageVO>> getUserHistoryPage(@RequestParam(defaultValue = "1") long current,
                                                                @RequestParam(defaultValue = "10") long pageSize,
                                                                HttpServletRequest request) {
        // 限制单次查询数量，避免一次性拉取过多聊天历史影响性能
        if (pageSize > 100) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "pageSize too large");
        }

        // 获取当前登录用户，用于限定只能查询自己的 AI 聊天历史
        User loginUser = userService.getLoginUser(request);
        // 分页查询当前用户最近的 AI 聊天消息记录
        Page<AiAvatarChatHistory> historyPage = aiAvatarChatHistoryService.getUserLatestMessagesPage(
                loginUser.getId(), current, pageSize);

        // 创建返回给前端的分页对象，并沿用数据库查询得到的总记录数
        Page<ChatMessageVO> resultPage = new Page<>(current, pageSize, historyPage.getTotal());
        // 将数据库实体转换为前端展示所需的 VO，并补充 AI 智能体和用户基础信息
        List<ChatMessageVO> records = historyPage.getRecords().stream().map(history -> {
            ChatMessageVO vo = new ChatMessageVO();
            BeanUtils.copyProperties(history, vo);

            // 根据聊天记录中的 AI 智能体 ID 补充名称和头像
            AiAvatar aiAvatar = aiAvatarService.getById(history.getAiAvatarId());
            if (aiAvatar != null) {
                vo.setAiAvatarName(aiAvatar.getName());
                vo.setAiAvatarImgUrl(aiAvatar.getAvatarImgUrl());
            }

            // 根据聊天记录中的用户 ID 补充用户昵称和头像
            User user = userService.getById(history.getUserId());
            if (user != null) {
                vo.setUserName(user.getUserName());
                vo.setUserAvatar(user.getUserAvatar());
            }
            return vo;
        }).collect(Collectors.toList());

        resultPage.setRecords(records);
        // 手动计算总页数，保证返回分页信息完整
        resultPage.setPages((historyPage.getTotal() + pageSize - 1) / pageSize);
        return ResultUtils.success(resultPage);
    }

    private boolean hasSessionAccess(String sessionId, User loginUser, Long aiAvatarId) {
        if (loginUser == null || StringUtils.isBlank(sessionId)) {
            return false;
        }
        if (userService.isAdmin(loginUser)) {
            return true;
        }
        return aiAvatarChatHistoryService.hasSessionAccess(sessionId, loginUser.getId(), aiAvatarId);
    }
}
