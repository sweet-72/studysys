package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.constant.PrivateChatSessionConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.PrivateMessageMapper;
import com.ttbt.smartclass.model.dto.privateMessage.PrivateMessageAddRequest;
import com.ttbt.smartclass.model.dto.privateMessage.PrivateMessageQueryRequest;
import com.ttbt.smartclass.model.entity.PrivateChatSession;
import com.ttbt.smartclass.model.entity.PrivateMessage;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.PrivateMessageVO;
import com.ttbt.smartclass.model.vo.UserVO;
import com.ttbt.smartclass.service.PrivateChatSessionService;
import com.ttbt.smartclass.service.PrivateMessageService;
import com.ttbt.smartclass.service.UserService;
import com.ttbt.smartclass.service.ChatSessionUtils;
import com.ttbt.smartclass.service.ChatSseService;
import com.ttbt.smartclass.service.ChatMessageService;
import com.ttbt.smartclass.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 私聊消息服务实现类
 */
@Service
@Slf4j
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage>
        implements PrivateMessageService {

    @Resource
    private UserService userService;

    @Resource
    private PrivateChatSessionService privateChatSessionService;

    @Resource
    private ChatSessionUtils chatSessionUtils;
    
    @Resource
    private ChatSseService chatSseService;
    
    @Resource
    private ChatMessageService chatMessageService;

    /**
     * 发送私聊消息并推送未读数变化。
     *
     * @param senderId 发送者 ID
     * @param request 私聊消息请求
     * @param httpRequest 当前 HTTP 请求
     * @return 新消息 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long sendPrivateMessage(Long senderId, PrivateMessageAddRequest request, HttpServletRequest httpRequest) {
        // 校验发送者、接收者和消息内容，禁止给自己发送私聊消息
        if (senderId == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        
        Long receiverId = request.getReceiverId();
        String content = request.getContent();
        
        if (receiverId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接收者ID不能为空");
        }
        
        if (StringUtils.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        }
        
        if (senderId.equals(receiverId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能给自己发送消息");
        }
        
        // 确保用户存在
        User sender = userService.getById(senderId);
        User receiver = userService.getById(receiverId);
        if (sender == null || receiver == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        
        // 获取或创建会话
        PrivateChatSession chatSession = privateChatSessionService.getOrCreateChatSession(senderId, receiverId);
        
        // 创建消息
        PrivateMessage message = new PrivateMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setIsRead(PrivateChatSessionConstant.UNREAD);
        
        boolean saveResult = save(message);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送消息失败");
        }
        
        // 更新会话的最后消息时间
        chatSession.setLastMessageTime(new Date());
        privateChatSessionService.updateById(chatSession);
        
        // 使用SSE实时推送消息
        chatSseService.sendChatMessage(receiverId, senderId, content, chatSession.getId().toString());
        
        // 更新接收者的未读消息数量
        int unreadCount = chatMessageService.getUserUnreadCount(receiverId);
        chatSseService.sendUnreadCountUpdate(receiverId, unreadCount);
        
        return message.getId();
    }

    /**
     * 查询指定用户相关的全部私聊消息。
     *
     * @param userId 用户 ID
     * @param request 当前 HTTP 请求
     * @return 私聊消息列表
     */
    @Override
    public List<PrivateMessageVO> listUserMessages(Long userId, HttpServletRequest request) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }
        
        // 验证用户是否有权限查看（只能查看自己的或管理员可查看所有）
        User loginUser = userService.getLoginUser(request);
        if (!userService.isAdmin(loginUser) && !loginUser.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权查看其他用户的消息");
        }
        
        // 查询用户相关的所有消息（发送或接收）
        QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sender_id", userId)
                .or()
                .eq("receiver_id", userId)
                .orderByDesc("create_time");
        
        List<PrivateMessage> messages = list(queryWrapper);
        
        // 转换为VO
        return getPrivateMessageVOList(messages);
    }

    /**
     * 分页查询两个用户之间的私聊消息。
     *
     * @param userId1 用户 1 ID
     * @param userId2 用户 2 ID
     * @param current 当前页码
     * @param size 每页数量
     * @param request 当前 HTTP 请求
     * @return 私聊消息分页数据
     */
    @Override
    public Page<PrivateMessageVO> listMessagesBetweenUsers(Long userId1, Long userId2, long current, long size, HttpServletRequest request) {
        if (userId1 == null || userId2 == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }
        
        // 验证用户是否有权限查看（只能查看自己相关的或管理员可查看所有）
        User loginUser = userService.getLoginUser(request);
        if (!userService.isAdmin(loginUser) && 
                !loginUser.getId().equals(userId1) && 
                !loginUser.getId().equals(userId2)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权查看其他用户之间的消息");
        }
        
        // 查询两个用户之间的所有消息
        QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.nested(q -> q.eq("sender_id", userId1)
                .eq("receiver_id", userId2))
                .or()
                .nested(q -> q.eq("sender_id", userId2)
                .eq("receiver_id", userId1))
                .orderByDesc("create_time");
        
        Page<PrivateMessage> messagePage = new Page<>(current, size);
        Page<PrivateMessage> pageResult = page(messagePage, queryWrapper);
        
        // 转换为VO
        Page<PrivateMessageVO> messageVOPage = new Page<>(
                pageResult.getCurrent(),
                pageResult.getSize(),
                pageResult.getTotal()
        );
        
        List<PrivateMessageVO> messageVOList = getPrivateMessageVOList(pageResult.getRecords());
        messageVOPage.setRecords(messageVOList);
        
        // 如果当前登录用户是接收者，则标记消息为已读
        if (loginUser.getId().equals(userId1) || loginUser.getId().equals(userId2)) {
            Long loginUserId = loginUser.getId();
            for (PrivateMessage message : pageResult.getRecords()) {
                if (loginUserId.equals(message.getReceiverId()) && 
                        PrivateChatSessionConstant.UNREAD == message.getIsRead()) {
                    message.setIsRead(PrivateChatSessionConstant.READ);
                    updateById(message);
                }
            }
        }
        
        return messageVOPage;
    }

    /**
     * 分页查询指定用户的未读私聊消息。
     *
     * @param userId 用户 ID
     * @param current 当前页码
     * @param size 每页数量
     * @param request 当前 HTTP 请求
     * @return 未读消息分页数据
     */
    @Override
    public Page<PrivateMessageVO> listUnreadMessages(Long userId, long current, long size, HttpServletRequest request) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }
        
        // 验证用户是否有权限查看（只能查看自己的或管理员可查看所有）
        User loginUser = userService.getLoginUser(request);
        if (!userService.isAdmin(loginUser) && !loginUser.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权查看其他用户的消息");
        }
        
        // 查询用户未读消息
        QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiver_id", userId)
                .eq("is_read", PrivateChatSessionConstant.UNREAD)
                .orderByDesc("create_time");
        
        Page<PrivateMessage> messagePage = new Page<>(current, size);
        Page<PrivateMessage> pageResult = page(messagePage, queryWrapper);
        
        // 转换为VO
        Page<PrivateMessageVO> messageVOPage = new Page<>(
                pageResult.getCurrent(),
                pageResult.getSize(),
                pageResult.getTotal()
        );
        
        List<PrivateMessageVO> messageVOList = getPrivateMessageVOList(pageResult.getRecords());
        messageVOPage.setRecords(messageVOList);
        
        return messageVOPage;
    }

    /**
     * 将单条私聊消息标记为已读。
     *
     * @param messageId 消息 ID
     * @param request 当前 HTTP 请求
     * @return 是否标记成功
     */
    @Override
    public boolean markMessageAsRead(Long messageId, HttpServletRequest request) {
        if (messageId == null || messageId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息ID不合法");
        }
        
        // 获取消息
        PrivateMessage message = getById(messageId);
        if (message == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "消息不存在");
        }
        
        // 验证权限（只有接收者可以标记消息为已读）
        User loginUser = userService.getLoginUser(request);
        if (!userService.isAdmin(loginUser) && !loginUser.getId().equals(message.getReceiverId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作");
        }
        
        // 使用ChatSessionUtils标记消息为已读
        chatSessionUtils.markMessageAsRead(messageId, loginUser.getId());
        
        // 找到消息所属的会话ID
        PrivateChatSession session = privateChatSessionService.getOrCreateChatSession(
                message.getSenderId(), message.getReceiverId());
        String sessionId = session.getId().toString();
        
        // 通过SSE推送已读状态更新
        chatSseService.sendReadStatusUpdate(loginUser.getId(), messageId, sessionId, true);
        
        // 更新未读消息数量并推送
        int unreadCount = chatMessageService.getUserUnreadCount(loginUser.getId());
        chatSseService.sendUnreadCountUpdate(loginUser.getId(), unreadCount);
        
        return true;
    }

    /**
     * 批量标记私聊消息为已读。
     *
     * @param messageIds 消息 ID 列表
     * @param request 当前 HTTP 请求
     * @return 是否标记成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markMessagesAsRead(List<Long> messageIds, HttpServletRequest request) {
        if (messageIds == null || messageIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息ID列表不能为空");
        }
        
        User loginUser = userService.getLoginUser(request);
        Long loginUserId = loginUser.getId();
        boolean isAdmin = userService.isAdmin(loginUser);
        
        // 验证消息是否存在并且当前用户是接收者
        List<PrivateMessage> messages = listByIds(messageIds);
        if (messages.size() != messageIds.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "部分消息不存在");
        }
        
        if (!isAdmin) {
            for (PrivateMessage message : messages) {
                if (!loginUserId.equals(message.getReceiverId())) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作他人的消息");
                }
            }
        }
        
        // 批量标记为已读
        UpdateWrapper<PrivateMessage> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", messageIds)
                .set("is_read", PrivateChatSessionConstant.READ);
        
        boolean result = update(updateWrapper);
        
        if (result) {
            // 找出第一条消息所属的会话ID
            // 注意：这里假设所有消息都属于同一个会话，实际使用中可能需要进一步优化
            if (!messages.isEmpty()) {
                PrivateMessage firstMessage = messages.get(0);
                PrivateChatSession session = privateChatSessionService.getOrCreateChatSession(
                        firstMessage.getSenderId(), firstMessage.getReceiverId());
                String sessionId = session.getId().toString();
                
                // 通过SSE推送批量已读状态更新
                chatSseService.sendBatchReadStatusUpdate(loginUserId, messageIds, sessionId, true);
                
                // 更新未读消息数量并推送
                int unreadCount = chatMessageService.getUserUnreadCount(loginUserId);
                chatSseService.sendUnreadCountUpdate(loginUserId, unreadCount);
            }
        }
        
        return result;
    }

    /**
     * 将指定用户的全部未读私聊消息标记为已读。
     *
     * @param userId 用户 ID
     * @return 是否标记成功
     */
    @Override
    public boolean markAllMessagesAsRead(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }
        
        // 先获取所有未读消息ID
        QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id")
                .eq("receiver_id", userId)
                .eq("is_read", PrivateChatSessionConstant.UNREAD);
        List<Object> messageIdObjs = listObjs(queryWrapper);
        List<Long> messageIds = messageIdObjs.stream()
                .map(obj -> Long.parseLong(obj.toString()))
                .collect(Collectors.toList());
        
        // 查找用户作为接收者的所有未读消息
        UpdateWrapper<PrivateMessage> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("receiver_id", userId)
                .eq("is_read", PrivateChatSessionConstant.UNREAD)
                .set("is_read", PrivateChatSessionConstant.READ);
        
        boolean result = update(updateWrapper);
        
        if (result && !messageIds.isEmpty()) {
            // 推送未读数量更新
            chatSseService.sendUnreadCountUpdate(userId, 0);
            
            // 对每个会话的消息进行分组处理
            Map<String, List<Long>> sessionMessageMap = new HashMap<>();
            
            // 获取这些消息所属的会话
            List<PrivateMessage> messages = listByIds(messageIds);
            for (PrivateMessage message : messages) {
                PrivateChatSession session = privateChatSessionService.getOrCreateChatSession(
                        message.getSenderId(), message.getReceiverId());
                String sessionId = session.getId().toString();
                
                sessionMessageMap.computeIfAbsent(sessionId, k -> new ArrayList<>())
                        .add(message.getId());
            }
            
            // 对每个会话发送批量已读状态更新
            for (Map.Entry<String, List<Long>> entry : sessionMessageMap.entrySet()) {
                String sessionId = entry.getKey();
                List<Long> sessionMessageIds = entry.getValue();
                
                chatSseService.sendBatchReadStatusUpdate(userId, sessionMessageIds, sessionId, true);
                // 也可以简单地发送会话全部已读更新
                chatSseService.sendSessionReadStatusUpdate(userId, sessionId);
            }
        }
        
        return result;
    }

    @Override
    public PrivateMessageVO getPrivateMessageVO(PrivateMessage message) {
        if (message == null) {
            return null;
        }
        
        PrivateMessageVO vo = new PrivateMessageVO();
        BeanUtils.copyProperties(message, vo);
        
        // 获取发送者和接收者信息
        UserVO senderVO = userService.getUserVOById(message.getSenderId());
        UserVO receiverVO = userService.getUserVOById(message.getReceiverId());
        vo.setSenderUser(senderVO);
        vo.setReceiverUser(receiverVO);
        
        return vo;
    }

    @Override
    public List<PrivateMessageVO> getPrivateMessageVOList(List<PrivateMessage> messageList) {
        if (messageList == null || messageList.isEmpty()) {
            return new ArrayList<>();
        }
        
        return messageList.stream()
                .map(this::getPrivateMessageVO)
                .collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<PrivateMessage> getQueryWrapper(PrivateMessageQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        
        Long id = request.getId();
        Long senderId = request.getSenderId();
        Long receiverId = request.getReceiverId();
        Long sessionId = request.getSessionId();
        String content = request.getContent();
        Integer isRead = request.getIsRead();
        Date startTime = request.getStartTime();
        Date endTime = request.getEndTime();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        
        QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        
        // 特殊处理：当senderId和receiverId相同时，表示查询与该用户相关的所有消息
        if (senderId != null && receiverId != null && senderId.equals(receiverId)) {
            queryWrapper.and(wrapper -> wrapper
                    .eq("sender_id", senderId)
                    .or()
                    .eq("receiver_id", receiverId)
            );
        } else {
            queryWrapper.eq(senderId != null, "sender_id", senderId);
            queryWrapper.eq(receiverId != null, "receiver_id", receiverId);
        }
        
        // 如果指定了会话ID，需要查询会话的两个用户之间的消息
        if (sessionId != null) {
            PrivateChatSession chatSession = privateChatSessionService.getById(sessionId);
            if (chatSession != null) {
                Long userId1 = chatSession.getUserId1();
                Long userId2 = chatSession.getUserId2();
                queryWrapper.and(wrapper -> wrapper
                        .nested(w -> w.eq("sender_id", userId1).eq("receiver_id", userId2))
                        .or()
                        .nested(w -> w.eq("sender_id", userId2).eq("receiver_id", userId1))
                );
            }
        }
        
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.eq(isRead != null, "is_read", isRead);
        queryWrapper.ge(startTime != null, "create_time", startTime);
        queryWrapper.le(endTime != null, "create_time", endTime);
        
        // 排序
        if (StringUtils.isNotBlank(sortField)) {
            queryWrapper.orderBy(true, "asc".equals(sortOrder), SqlUtils.normalizeSortField(sortField));
        } else {
            queryWrapper.orderByDesc("create_time");
        }
        
        return queryWrapper;
    }
}
