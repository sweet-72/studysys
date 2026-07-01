package com.ttbt.smartclass.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.constant.PrivateChatSessionConstant;
import com.ttbt.smartclass.event.WebSocketMessageEvent;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.PrivateChatSessionMapper;
import com.ttbt.smartclass.mapper.PrivateMessageMapper;
import com.ttbt.smartclass.model.dto.privatechatsession.PrivateChatSessionQueryRequest;
import com.ttbt.smartclass.model.dto.privateMessage.PrivateMessageAddRequest;
import com.ttbt.smartclass.model.entity.PrivateChatSession;
import com.ttbt.smartclass.model.entity.PrivateMessage;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.PrivateChatSessionVO;
import com.ttbt.smartclass.model.vo.PrivateMessageVO;
import com.ttbt.smartclass.model.vo.UserVO;
import com.ttbt.smartclass.model.websocket.WebSocketMessage;
import com.ttbt.smartclass.service.ChatSessionUtils;
import com.ttbt.smartclass.service.PrivateChatSessionService;
import com.ttbt.smartclass.service.UserService;
import com.ttbt.smartclass.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 私聊会话服务实现类
 */
@Service
@Slf4j
public class PrivateChatSessionServiceImpl extends ServiceImpl<PrivateChatSessionMapper, PrivateChatSession>
        implements PrivateChatSessionService {

    @Resource
    private PrivateMessageMapper privateMessageMapper;
    
    @Resource
    private UserService userService;
    
    @Autowired
    private ChatSessionUtils chatSessionUtils;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Redis Key前缀
    private static final String SESSION_KEY = "chat:session:";
    private static final String SESSION_USER_KEY = "chat:session:user:";
    private static final int SESSION_EXPIRE_DAYS = 30; // 会话缓存过期时间，30天

    /**
     * 创建或获取两个用户之间的私聊会话。
     *
     * @param userId1 用户 1 ID
     * @param userId2 用户 2 ID
     * @return 会话记录 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long createChatSession(Long userId1, Long userId2) {
        // 通过工具类保证两个用户之间只存在一个稳定会话
        PrivateChatSession chatSession = chatSessionUtils.getOrCreateChatSession(userId1, userId2);
        return chatSession.getId();
    }

    /**
     * 查询用户私聊会话列表。
     *
     * @param userId 用户 ID
     * @param request 当前 HTTP 请求
     * @return 会话展示列表
     */
    @Override
    public List<PrivateChatSessionVO> listUserChatSessions(Long userId, HttpServletRequest request) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }
        
        // 验证用户是否有权限查看（只能查看自己的或管理员可查看所有）
        User loginUser = userService.getLoginUser(request);
        if (!userService.isAdmin(loginUser) && !loginUser.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权查看其他用户的聊天会话");
        }
        
        // 尝试从Redis获取用户的会话ID列表
        Set<Object> sessionIds = redisTemplate.opsForSet().members(SESSION_USER_KEY + userId);
        List<PrivateChatSession> chatSessions = new ArrayList<>();
        
        if (sessionIds != null && !sessionIds.isEmpty()) {
            // 从Redis获取会话详情
            for (Object sessionId : sessionIds) {
                PrivateChatSession session = getById(Long.parseLong(sessionId.toString()));
                if (session != null) {
                    chatSessions.add(session);
                }
            }
        }
        
        // 如果Redis中没有数据，从数据库查询
        if (chatSessions.isEmpty()) {
            QueryWrapper<PrivateChatSession> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id1", userId)
                    .or()
                    .eq("user_id2", userId)
                    .orderByDesc("last_message_time");
            
            chatSessions = list(queryWrapper);
            
            // 将会话信息缓存到Redis
            for (PrivateChatSession session : chatSessions) {
                Long userId1 = session.getUserId1();
                Long userId2 = session.getUserId2();
                String sessionKey = String.format("%s%d_%d", SESSION_KEY, userId1, userId2);
                redisTemplate.opsForValue().set(sessionKey, session, SESSION_EXPIRE_DAYS, TimeUnit.DAYS);
                
                // 缓存用户的会话列表
                redisTemplate.opsForSet().add(SESSION_USER_KEY + userId1, session.getId());
                redisTemplate.opsForSet().add(SESSION_USER_KEY + userId2, session.getId());
            }
        }
        
        // 按最后消息时间排序
        chatSessions.sort((s1, s2) -> s2.getLastMessageTime().compareTo(s1.getLastMessageTime()));
        
        // 转换为VO
        return chatSessions.stream()
                .map(session -> getPrivateChatSessionVO(session, userId))
                .collect(Collectors.toList());
    }

    @Override
    public PrivateChatSession getOrCreateChatSession(Long userId1, Long userId2) {
        return chatSessionUtils.getOrCreateChatSession(userId1, userId2);
    }

    /**
     * 发送私聊消息并通过 WebSocket 事件实时推送。
     *
     * @param senderId 发送者 ID
     * @param request 私聊消息请求
     * @param httpRequest 当前 HTTP 请求
     * @return 新消息 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long sendPrivateMessage(Long senderId, PrivateMessageAddRequest request, HttpServletRequest httpRequest) {
        if (senderId == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        
        Long receiverId = request.getReceiverId();
        String content = request.getContent();
        
        // 参数校验
        if (receiverId == null || StringUtils.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接收者ID和消息内容不能为空");
        }
        
        // 保存消息到数据库
        PrivateMessage message = new PrivateMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setIsRead(PrivateChatSessionConstant.UNREAD);
        message.setCreateTime(new Date());
        privateMessageMapper.insert(message);
        
        // 更新或创建聊天会话
        PrivateChatSession chatSession = chatSessionUtils.getOrCreateChatSession(senderId, receiverId);
        chatSessionUtils.updateSessionLastMessageTime(chatSession);
        
        // 缓存消息ID
        String messageIdKey = String.format("chat:last_message:%s:%s", senderId, receiverId);
        redisTemplate.opsForValue().set(messageIdKey, message.getId(), 1, TimeUnit.DAYS);
        
        // 添加到接收者的未读消息列表
        redisTemplate.opsForList().rightPush("chat:unread:" + receiverId, message.getId().toString());
        
        // 尝试实时推送消息 - 使用事件发布而不是直接调用channelManager
        WebSocketMessage webSocketMessage = new WebSocketMessage();
        webSocketMessage.setType(1); // 私聊消息
        webSocketMessage.setMessageId(message.getId().toString());
        webSocketMessage.setSenderId(senderId);
        webSocketMessage.setReceiverId(receiverId);
        webSocketMessage.setContent(content);
        webSocketMessage.setSendTime(new Date());
        
        eventPublisher.publishEvent(new WebSocketMessageEvent(receiverId, JSON.toJSONString(webSocketMessage)));
        
        return message.getId();
    }

    /**
     * 分页查询指定私聊会话的消息列表。
     *
     * @param sessionId 会话 ID
     * @param current 当前页码
     * @param size 每页数量
     * @param request 当前 HTTP 请求
     * @return 会话消息分页数据
     */
    @Override
    public Page<PrivateMessageVO> listSessionMessages(Long sessionId, long current, long size, HttpServletRequest request) {
        if (sessionId == null || sessionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话ID不合法");
        }
        
        // 获取会话
        PrivateChatSession chatSession = getById(sessionId);
        if (chatSession == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "聊天会话不存在");
        }
        
        // 检查权限
        User loginUser = userService.getLoginUser(request);
        Long loginUserId = loginUser.getId();
        if (!userService.isAdmin(loginUser) && 
                !loginUserId.equals(chatSession.getUserId1()) && 
                !loginUserId.equals(chatSession.getUserId2())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权查看该聊天会话的消息");
        }
        
        // 查询消息
        QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sender_id", chatSession.getUserId1())
                .eq("receiver_id", chatSession.getUserId2())
                .or()
                .eq("sender_id", chatSession.getUserId2())
                .eq("receiver_id", chatSession.getUserId1())
                .orderByDesc("create_time");
        
        Page<PrivateMessage> messagePage = new Page<>(current, size);
        Page<PrivateMessage> pageResult = privateMessageMapper.selectPage(messagePage, queryWrapper);
        
        // 转换为VO
        Page<PrivateMessageVO> messageVOPage = new Page<>(
                pageResult.getCurrent(),
                pageResult.getSize(),
                pageResult.getTotal()
        );
        
        List<PrivateMessageVO> messageVOList = pageResult.getRecords().stream()
                .map(this::getPrivateMessageVO)
                .collect(Collectors.toList());
        
        messageVOPage.setRecords(messageVOList);
        
        // 标记接收到的消息为已读
        markAllMessagesAsRead(sessionId, loginUserId);
        
        return messageVOPage;
    }

    /**
     * 将指定消息标记为用户已读。
     *
     * @param messageId 消息 ID
     * @param userId 用户 ID
     */
    @Override
    public void markMessageAsRead(Long messageId, Long userId) {
        if (messageId == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        
        // 委托会话工具按接收者维度更新消息已读状态
        chatSessionUtils.markMessageAsRead(messageId, userId);
    }

    /**
     * 将指定会话中当前用户接收的消息全部标记为已读。
     *
     * @param sessionId 会话 ID
     * @param userId 用户 ID
     * @return 是否标记成功
     */
    @Override
    public boolean markAllMessagesAsRead(Long sessionId, Long userId) {
        if (sessionId == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        
        // 获取会话
        PrivateChatSession chatSession = getById(sessionId);
        if (chatSession == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "聊天会话不存在");
        }
        
        // 确保用户是会话的参与者
        if (!userId.equals(chatSession.getUserId1()) && !userId.equals(chatSession.getUserId2())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户不是该会话的参与者");
        }
        
        // 查询会话中接收者为当前用户且未读的消息
        QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiver_id", userId)
                .eq("is_read", PrivateChatSessionConstant.UNREAD)
                .and(wrapper -> wrapper
                        .eq("sender_id", chatSession.getUserId1().equals(userId) ? chatSession.getUserId2() : chatSession.getUserId1()));
        
        List<PrivateMessage> unreadMessages = privateMessageMapper.selectList(queryWrapper);
        
        // 批量标记为已读
        for (PrivateMessage message : unreadMessages) {
            chatSessionUtils.markMessageAsRead(message.getId(), userId);
        }
        
        return true;
    }

    @Override
    public int getUnreadMessageCount(Long sessionId, Long userId) {
        if (sessionId == null || userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        
        // 获取会话
        PrivateChatSession chatSession = getById(sessionId);
        if (chatSession == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "聊天会话不存在");
        }
        
        // 确保用户是会话的参与者
        if (!userId.equals(chatSession.getUserId1()) && !userId.equals(chatSession.getUserId2())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户不是该会话的参与者");
        }
        
        // 查询会话中接收者为当前用户且未读的消息数量
        QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiver_id", userId)
                .eq("is_read", PrivateChatSessionConstant.UNREAD)
                .and(wrapper -> wrapper
                        .eq("sender_id", chatSession.getUserId1().equals(userId) ? chatSession.getUserId2() : chatSession.getUserId1()));
        
        return Math.toIntExact(privateMessageMapper.selectCount(queryWrapper));
    }

    @Override
    public int getTotalUnreadMessageCount(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }
        
        // 使用ChatSessionUtils获取未读消息数量
        return Math.toIntExact(chatSessionUtils.getUnreadMessageCount(userId));
    }

    @Override
    public PrivateChatSession getChatSession(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }
        
        // 确保ID顺序，保证一致性
        if (userId1 > userId2) {
            Long temp = userId1;
            userId1 = userId2;
            userId2 = temp;
        }
        
        // 优先从Redis获取会话
        String sessionKey = String.format("%s%d_%d", SESSION_KEY, userId1, userId2);
        Object sessionObj = redisTemplate.opsForValue().get(sessionKey);
        
        if (sessionObj != null) {
            if (sessionObj instanceof PrivateChatSession) {
                return (PrivateChatSession) sessionObj;
            } else {
                // 处理可能的类型转换问题
                PrivateChatSession session = JSON.parseObject(JSON.toJSONString(sessionObj), PrivateChatSession.class);
                return session;
            }
        }
        
        // Redis中没有，查询数据库
        QueryWrapper<PrivateChatSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id1", userId1).eq("user_id2", userId2);
        
        PrivateChatSession chatSession = getOne(queryWrapper);
        if (chatSession != null) {
            // 缓存会话信息
            redisTemplate.opsForValue().set(sessionKey, chatSession, SESSION_EXPIRE_DAYS, TimeUnit.DAYS);
        }
        
        return chatSession;
    }

    @Override
    public PrivateChatSessionVO getPrivateChatSessionVO(PrivateChatSession chatSession, Long currentUserId) {
        if (chatSession == null) {
            return null;
        }
        
        PrivateChatSessionVO sessionVO = new PrivateChatSessionVO();
        BeanUtils.copyProperties(chatSession, sessionVO);
        
        Long userId1 = chatSession.getUserId1();
        Long userId2 = chatSession.getUserId2();
        
        // 确定对方用户ID
        Long partnerId = userId1.equals(currentUserId) ? userId2 : userId1;
        
        // 获取对方用户信息
        UserVO partnerVO = userService.getUserVOById(partnerId);
        sessionVO.setTargetUser(partnerVO);
        
        // 获取未读消息数量
        int unreadCount = getUnreadMessageCount(chatSession.getId(), currentUserId);
        sessionVO.setUnreadCount(unreadCount);
        
        // 获取最新消息
        QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.nested(q -> q.eq("sender_id", userId1).eq("receiver_id", userId2))
                .or()
                .nested(q -> q.eq("sender_id", userId2).eq("receiver_id", userId1))
                .orderByDesc("create_time")
                .last("LIMIT 1");
        
        PrivateMessage latestMessage = privateMessageMapper.selectOne(queryWrapper);
        if (latestMessage != null) {
            PrivateMessageVO latestMessageVO = getPrivateMessageVO(latestMessage);
            sessionVO.setLastMessage(latestMessageVO);
        }
        
        return sessionVO;
    }

    @Override
    public PrivateMessageVO getPrivateMessageVO(PrivateMessage message) {
        if (message == null) {
            return null;
        }
        
        PrivateMessageVO messageVO = new PrivateMessageVO();
        BeanUtils.copyProperties(message, messageVO);
        
        // 获取用户信息
        UserVO senderVO = userService.getUserVOById(message.getSenderId());
        UserVO receiverVO = userService.getUserVOById(message.getReceiverId());
        
        messageVO.setSenderUser(senderVO);
        messageVO.setReceiverUser(receiverVO);
        
        return messageVO;
    }
    
    @Override
    public QueryWrapper<PrivateChatSession> getQueryWrapper(PrivateChatSessionQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        
        Long userId = request.getUserId();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        
        QueryWrapper<PrivateChatSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(userId != null, "user_id1", userId)
                .or()
                .eq(userId != null, "user_id2", userId);
        
        // 排序
        if (StringUtils.isNotBlank(sortField)) {
            queryWrapper.orderBy(true, "asc".equals(sortOrder), SqlUtils.normalizeSortField(sortField));
        } else {
            queryWrapper.orderByDesc("last_message_time");
        }
        
        return queryWrapper;
    }
}
