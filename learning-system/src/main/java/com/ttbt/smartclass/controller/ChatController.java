package com.ttbt.smartclass.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.model.entity.ChatMessage;
import com.ttbt.smartclass.model.entity.ChatSession;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.service.ChatMessageService;
import com.ttbt.smartclass.service.ChatSessionService;
import com.ttbt.smartclass.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 私聊接口控制器。
 * 负责私聊消息发送、会话消息查询、未读数统计和会话状态更新。
 */
@Slf4j
@RestController
@RequestMapping("/private-chat")
@Api(tags = "聊天接口")
public class ChatController {

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    private UserService userService;

    /**
     * 发送文本私聊消息。
     *
     * @param receiverId 接收者 ID
     * @param content 文本内容
     * @param request 当前 HTTP 请求
     * @return 已保存的聊天消息
     */
    @PostMapping("/send/text")
    @ApiOperation("发送文本消息")
    public BaseResponse<ChatMessage> sendTextMessage(
            @RequestParam Long receiverId,
            @RequestParam String content,
            HttpServletRequest request) {

        // 获取当前登录用户，发送方固定为当前用户
        User loginUser = userService.getLoginUser(request);
        // 保存文本消息，并由消息服务维护会话和未读状态
        ChatMessage message = chatMessageService.sendTextMessage(loginUser.getId(), receiverId, content);
        return ResultUtils.success(message);
    }

    /**
     * 发送图片私聊消息。
     *
     * @param receiverId 接收者 ID
     * @param mediaUrl 图片地址
     * @param mediaSize 图片大小
     * @param request 当前 HTTP 请求
     * @return 已保存的聊天消息
     */
    @PostMapping("/send/image")
    @ApiOperation("发送图片消息")
    public BaseResponse<ChatMessage> sendImageMessage(
            @RequestParam Long receiverId,
            @RequestParam String mediaUrl,
            @RequestParam(required = false) Long mediaSize,
            HttpServletRequest request) {

        // 获取当前登录用户，避免前端伪造发送者
        User loginUser = userService.getLoginUser(request);
        // 保存图片消息，并记录媒体地址和大小信息
        ChatMessage message = chatMessageService.sendImageMessage(loginUser.getId(), receiverId, mediaUrl, mediaSize);
        return ResultUtils.success(message);
    }

    /**
     * 发送视频私聊消息。
     *
     * @param receiverId 接收者 ID
     * @param mediaUrl 视频地址
     * @param mediaSize 视频大小
     * @param mediaDuration 视频时长
     * @param request 当前 HTTP 请求
     * @return 已保存的聊天消息
     */
    @PostMapping("/send/video")
    @ApiOperation("发送视频消息")
    public BaseResponse<ChatMessage> sendVideoMessage(
            @RequestParam Long receiverId,
            @RequestParam String mediaUrl,
            @RequestParam(required = false) Long mediaSize,
            @RequestParam(required = false) Integer mediaDuration,
            HttpServletRequest request) {

        // 获取当前登录用户，发送方由服务端登录态决定
        User loginUser = userService.getLoginUser(request);
        // 保存视频消息，并记录媒体地址、大小和时长
        ChatMessage message = chatMessageService.sendVideoMessage(loginUser.getId(), receiverId, mediaUrl, mediaSize, mediaDuration);
        return ResultUtils.success(message);
    }

    /**
     * 发送语音私聊消息。
     * 暂未实现，需要接接口
     * @param receiverId 接收者 ID
     * @param audioUrl 语音地址
     * @param audioDuration 语音时长
     * @param mediaSize 语音大小
     * @param request 当前 HTTP 请求
     * @return 已保存的聊天消息
     */
    @PostMapping("/send/audio")
    @ApiOperation("发送语音消息")
    public BaseResponse<ChatMessage> sendAudioMessage(
            @RequestParam Long receiverId,
            @RequestParam String audioUrl,
            @RequestParam(required = false) Integer audioDuration,
            @RequestParam(required = false) Long mediaSize,
            HttpServletRequest request) {

        // 获取当前登录用户，并发送语音类型消息
        User loginUser = userService.getLoginUser(request);
        ChatMessage message = chatMessageService.sendAudioMessage(loginUser.getId(), receiverId, audioUrl, audioDuration, mediaSize);
        return ResultUtils.success(message);
    }

    /**
     * 撤回当前用户发送的消息。
     * 前端暂无ui，未实现
     * @param messageId 消息 ID
     * @param request 当前 HTTP 请求
     * @return 是否撤回成功
     */
    @PostMapping("/revoke")
    @ApiOperation("撤回消息")
    public BaseResponse<Boolean> revokeMessage(
            @RequestParam Long messageId,
            HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        // 消息服务会校验当前用户是否为消息发送方并执行撤回
        chatMessageService.revokeMessage(messageId, loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * 将单条消息标记为当前用户已读。
     *
     * @param messageId 消息 ID
     * @param request 当前 HTTP 请求
     * @return 是否标记成功
     */
    @PostMapping("/read/message")
    @ApiOperation("标记消息为已读")
    public BaseResponse<Boolean> markMessageAsRead(
            @RequestParam Long messageId,
            HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        // 消息服务按当前用户校验接收者身份并更新已读状态
        chatMessageService.markAsRead(messageId, loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * 将指定会话内消息标记为当前用户已读。
     *
     * @param sessionId 会话 ID
     * @param request 当前 HTTP 请求
     * @return 是否标记成功
     */
    @PostMapping("/read/session")
    @ApiOperation("标记会话为已读")
    public BaseResponse<Boolean> markSessionAsRead(
            @RequestParam String sessionId,
            HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        // 标记会话已读前先确认当前用户是会话参与者
        if (!chatSessionService.hasSessionAccess(sessionId, loginUser.getId())) {
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
        }
        chatMessageService.markSessionAsRead(sessionId, loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * 通过路径变量将指定会话标记为已读。
     *
     * @param sessionId 会话 ID
     * @param request 当前 HTTP 请求
     * @return 是否标记成功
     */
    @PostMapping("/sessions/{sessionId}/read/all")
    @ApiOperation("标记会话为已读(路径变量)")
    public BaseResponse<Boolean> markSessionAsReadByPath(
            @PathVariable String sessionId,
            HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        // 兼容路径变量接口，同样需要校验会话访问权限
        if (!chatSessionService.hasSessionAccess(sessionId, loginUser.getId())) {
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
        }
        chatMessageService.markSessionAsRead(sessionId, loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * 分页查询指定会话的消息列表。
     *
     * @param sessionId 会话 ID
     * @param current 当前页码
     * @param pageSize 每页数量
     * @param request 当前 HTTP 请求
     * @return 会话消息分页数据
     */
    @GetMapping("/messages")
    @ApiOperation("获取会话消息列表")
    public BaseResponse<Page<ChatMessage>> getSessionMessages(
            @RequestParam String sessionId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long pageSize,
            HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        // 查询前校验当前用户是否属于该会话
        if (!chatSessionService.hasSessionAccess(sessionId, loginUser.getId())) {
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
        }
        // 分页读取会话消息，按消息服务既有排序返回
        Page<ChatMessage> messages = chatMessageService.getSessionMessages(sessionId, current, pageSize);
        return ResultUtils.success(messages);
    }

    /**
     * 通过会话记录 ID 分页查询消息列表。
     *
     * @param sessionId 会话记录 ID
     * @param current 当前页码
     * @param pageSize 每页数量
     * @param request 当前 HTTP 请求
     * @return 会话消息分页数据
     */
    @GetMapping("/sessions/{sessionId}/messages")
    @ApiOperation("获取会话消息列表(路径变量)")
    public BaseResponse<Page<ChatMessage>> getSessionMessagesByPath(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long pageSize,
            HttpServletRequest request) {

        // 先通过数据库主键加载会话，再取业务会话 ID 查询消息
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null) {
            return ResultUtils.error(ErrorCode.SESSION_NOT_FOUND);
        }

        User loginUser = userService.getLoginUser(request);
        // 当前用户必须是会话参与者才能查看消息
        if (!chatSessionService.hasSessionAccess(session.getSessionId(), loginUser.getId())) {
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
        }

        Page<ChatMessage> messages = chatMessageService.getSessionMessages(session.getSessionId(), current, pageSize);
        return ResultUtils.success(messages);
    }

    /**
     * 查询指定会话中当前用户的未读消息数。
     *
     * @param sessionId 会话记录 ID
     * @param request 当前 HTTP 请求
     * @return 未读消息数
     */
    @GetMapping("/sessions/{sessionId}/unread/count")
    @ApiOperation("获取会话未读消息数")
    public BaseResponse<Integer> getSessionUnreadCount(
            @PathVariable Long sessionId,
            HttpServletRequest request) {

        // 通过会话记录 ID 加载会话，避免无效会话统计未读数
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null) {
            return ResultUtils.error(ErrorCode.SESSION_NOT_FOUND);
        }

        User loginUser = userService.getLoginUser(request);
        // 未读数只允许会话参与者查询
        if (!chatSessionService.hasSessionAccess(session.getSessionId(), loginUser.getId())) {
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
        }

        // 按当前用户维度统计该会话未读消息数
        int count = chatSessionService.getSessionUnreadCount(session.getSessionId(), loginUser.getId());
        return ResultUtils.success(count);
    }

    /**
     * 分页查询当前用户的私聊会话列表。
     *
     * @param current 当前页码
     * @param pageSize 每页数量
     * @param request 当前 HTTP 请求
     * @return 会话分页数据
     */
    @GetMapping("/sessions/list")
    @ApiOperation("获取用户会话列表")
    public BaseResponse<Page<ChatSession>> getUserSessions(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long pageSize,
            HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        // 只查询当前登录用户参与的会话列表
        Page<ChatSession> sessions = chatSessionService.getUserSessions(loginUser.getId(), current, pageSize);
        return ResultUtils.success(sessions);
    }

    /**
     * 查询当前用户所有会话的未读数。
     *
     * @param request 当前 HTTP 请求
     * @return 会话 ID 到未读数的映射
     */
    @GetMapping("/sessions/unread/counts")
    @ApiOperation("获取用户所有会话未读数")
    public BaseResponse<Map<String, Integer>> getSessionUnreadCounts(HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        // 按会话聚合当前用户的未读消息数量
        Map<String, Integer> unreadCounts = chatSessionService.getAllSessionUnreadCounts(loginUser.getId());
        return ResultUtils.success(unreadCounts);
    }

    /**
     * 查询当前用户好友列表并附带未读数。
     *
     * @param request 当前 HTTP 请求
     * @return 好友和会话未读数列表
     */
    @GetMapping("/friends")
    @ApiOperation("获取用户好友列表")
    public BaseResponse<List<Map<String, Object>>> getUserFriends(HttpServletRequest request) {
        // 获取当前用户好友列表和所有会话未读数
        User loginUser = userService.getLoginUser(request);
        List<User> friends = userService.getFriendList(loginUser.getId());

        Map<String, Integer> unreadCounts = chatSessionService.getAllSessionUnreadCounts(loginUser.getId());

        // 根据双方用户 ID 生成稳定会话 ID，并补充对应未读数
        List<Map<String, Object>> result = new ArrayList<>();
        for (User friend : friends) {
            Map<String, Object> friendData = new HashMap<>();
            friendData.put("user", friend);

            String sessionId = loginUser.getId() < friend.getId()
                    ? loginUser.getId() + "_" + friend.getId()
                    : friend.getId() + "_" + loginUser.getId();

            Integer unreadCount = unreadCounts.get(sessionId);
            friendData.put("unreadCount", unreadCount == null ? 0 : unreadCount);

            result.add(friendData);
        }

        return ResultUtils.success(result);
    }

    /**
     * 查询当前用户总未读消息数。
     *
     * @param request 当前 HTTP 请求
     * @return 总未读数
     */
    @GetMapping("/unread/total")
    @ApiOperation("获取用户总未读数")
    public BaseResponse<Integer> getTotalUnreadCount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 汇总当前用户所有会话中的未读消息
        int totalCount = chatMessageService.getTotalUnreadCount(loginUser.getId());
        return ResultUtils.success(totalCount);
    }

    /**
     * 查询当前用户未读消息总数，兼容旧接口。
     *
     * @param request 当前 HTTP 请求
     * @return 未读消息总数
     */
    @GetMapping("/unread/count")
    @ApiOperation("获取未读消息总数(兼容旧接口)")
    public BaseResponse<Integer> getUnreadCount(HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        // 兼容旧接口，使用消息服务统计当前用户未读数
        int count = chatMessageService.getUserUnreadCount(loginUser.getId());
        return ResultUtils.success(count);
    }

    /**
     * 删除当前用户的指定私聊会话。
     *
     * @param sessionId 会话 ID
     * @param request 当前 HTTP 请求
     * @return 是否删除成功
     */
    @DeleteMapping("/session")
    @ApiOperation("删除会话")
    public BaseResponse<Boolean> deleteSession(
            @RequestParam String sessionId,
            HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        // 按当前用户维度删除会话，避免影响对方会话视图
        chatSessionService.deleteSession(sessionId, loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * 设置或取消当前用户的会话置顶状态。
     *
     * @param sessionId 会话 ID
     * @param isTop 是否置顶
     * @param request 当前 HTTP 请求
     * @return 是否操作成功
     */
    @PostMapping("/session/top")
    @ApiOperation("置顶/取消置顶会话")
    public BaseResponse<Boolean> toggleTop(
            @RequestParam String sessionId,
            @RequestParam Boolean isTop,
            HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        // 置顶状态只作用于当前用户自己的会话列表
        chatSessionService.toggleTop(sessionId, loginUser.getId(), isTop);
        return ResultUtils.success(true);
    }
}
