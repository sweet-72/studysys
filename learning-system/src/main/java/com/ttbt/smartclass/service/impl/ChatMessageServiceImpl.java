package com.ttbt.smartclass.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.ChatMessageMapper;
import com.ttbt.smartclass.model.dto.ExpAddRequest;
import com.ttbt.smartclass.model.entity.ChatMessage;
import com.ttbt.smartclass.netty.ChannelManager;
import com.ttbt.smartclass.service.ChatMessageService;
import com.ttbt.smartclass.service.ChatSessionService;
import com.ttbt.smartclass.service.SpeechRecognitionService;
import com.ttbt.smartclass.service.UserLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 聊天消息服务实现类。
 */
@Slf4j
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    private SpeechRecognitionService speechRecognitionService;

    @Resource
    private UserLevelService userLevelService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ChannelManager channelManager;

    private static final String UNREAD_TOTAL_KEY = "chat:unread:total:";
    private static final String EXP_LIMIT_KEY = "chat:exp:limit:";
    private static final String OFFLINE_MESSAGE_KEY = "chat:offline:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage sendTextMessage(Long senderId, Long receiverId, String content) {
        if (StrUtil.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "message content is blank");
        }

        String sessionId = generateSessionId(senderId, receiverId);
        chatSessionService.getOrCreateSession(senderId, receiverId);

        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setSessionId(sessionId);
        message.setMessageType("text");
        message.setContent(content);
        message.setStatus(0);
        message.setSendTime(new Date());
        message.setExpAwarded(0);

        if (!this.save(message)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "send text failed");
        }

        chatSessionService.updateLastMessage(sessionId, message.getId(), StrUtil.sub(content, 0, 200), "text", receiverId);
        increaseTotalUnreadCount(receiverId);

        addExpWithLimit(senderId, "text", message.getId());
        pushMessageToReceiver(message);
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage sendImageMessage(Long senderId, Long receiverId, String mediaUrl, Long mediaSize) {
        if (StrUtil.isBlank(mediaUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "image url is blank");
        }

        String sessionId = generateSessionId(senderId, receiverId);
        chatSessionService.getOrCreateSession(senderId, receiverId);

        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setSessionId(sessionId);
        message.setMessageType("image");
        message.setContent("[image]");
        message.setMediaUrl(mediaUrl);
        message.setMediaSize(mediaSize);
        message.setStatus(0);
        message.setSendTime(new Date());
        message.setExpAwarded(0);

        if (!this.save(message)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "send image failed");
        }

        chatSessionService.updateLastMessage(sessionId, message.getId(), "[image]", "image", receiverId);
        increaseTotalUnreadCount(receiverId);

        addExpWithLimit(senderId, "image", message.getId());
        pushMessageToReceiver(message);
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage sendVideoMessage(Long senderId, Long receiverId, String mediaUrl, Long mediaSize, Integer mediaDuration) {
        if (StrUtil.isBlank(mediaUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "video url is blank");
        }

        String sessionId = generateSessionId(senderId, receiverId);
        chatSessionService.getOrCreateSession(senderId, receiverId);

        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setSessionId(sessionId);
        message.setMessageType("video");
        message.setContent("[video]");
        message.setMediaUrl(mediaUrl);
        message.setMediaSize(mediaSize);
        message.setMediaDuration(mediaDuration);
        message.setStatus(0);
        message.setSendTime(new Date());
        message.setExpAwarded(0);

        if (!this.save(message)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "send video failed");
        }

        chatSessionService.updateLastMessage(sessionId, message.getId(), "[video]", "video", receiverId);
        increaseTotalUnreadCount(receiverId);

        addExpWithLimit(senderId, "video", message.getId());
        pushMessageToReceiver(message);
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage sendAudioMessage(Long senderId, Long receiverId, String audioUrl, Integer audioDuration, Long mediaSize) {
        if (StrUtil.isBlank(audioUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "audio url is blank");
        }

        String sessionId = generateSessionId(senderId, receiverId);
        chatSessionService.getOrCreateSession(senderId, receiverId);

        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setSessionId(sessionId);
        message.setMessageType("audio");
        message.setContent("[audio]");
        message.setAudioUrl(audioUrl);
        message.setAudioDuration(audioDuration);
        message.setMediaSize(mediaSize);
        message.setStatus(0);
        message.setSendTime(new Date());
        message.setExpAwarded(0);

        if (!this.save(message)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "send audio failed");
        }

        chatSessionService.updateLastMessage(sessionId, message.getId(), "[audio]", "audio", receiverId);
        increaseTotalUnreadCount(receiverId);

        recognizeSpeechAsync(message);
        addExpWithLimit(senderId, "audio", message.getId());
        pushMessageToReceiver(message);
        return message;
    }

    @Override
    public void revokeMessage(Long messageId, Long userId) {
        ChatMessage message = this.getById(messageId);
        if (message == null || !Integer.valueOf(0).equals(message.getIsDelete())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "message not found");
        }

        if (!userId.equals(message.getSenderId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "only sender can revoke");
        }

        Date createTime = message.getCreateTime() != null ? message.getCreateTime() : message.getSendTime();
        if (createTime == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "message time missing");
        }

        long diff = System.currentTimeMillis() - createTime.getTime();
        if (diff > 2 * 60 * 1000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "revoke window exceeded");
        }

        message.setIsRevoke(1);
        message.setRevokeTime(new Date());
        message.setContent("message revoked");
        this.updateById(message);

        channelManager.sendMessage(message.getReceiverId(), JSON.toJSONString(message));
    }

    @Override
    public void markAsRead(Long messageId, Long userId) {
        ChatMessage message = this.getById(messageId);
        if (message == null || !userId.equals(message.getReceiverId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "message not found");
        }

        int changed = this.baseMapper.markAsRead(messageId);
        if (changed <= 0) {
            return;
        }

        chatSessionService.decreaseUnreadCount(message.getSessionId(), userId, 1);
        decreaseTotalUnreadCountBy(userId, 1L);
        syncTotalUnreadCountFromDb(userId);
    }

    @Override
    public void markSessionAsRead(String sessionId, Long userId) {
        int changed = this.baseMapper.markSessionMessagesAsRead(sessionId, userId);
        chatSessionService.clearUnreadCount(sessionId, userId);

        if (changed > 0) {
            decreaseTotalUnreadCountBy(userId, (long) changed);
        }
        syncTotalUnreadCountFromDb(userId);
    }

    @Override
    public Page<ChatMessage> getSessionMessages(String sessionId, long current, long pageSize) {
        Page<ChatMessage> page = new Page<>(current, pageSize);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .orderByDesc(ChatMessage::getSendTime);
        return this.page(page, wrapper);
    }

    @Override
    public int getUserUnreadCount(Long userId) {
        return getAccurateTotalUnreadCount(userId);
    }

    @Override
    public Map<String, Integer> getAllSessionUnreadCounts(Long userId) {
        return chatSessionService.getAllSessionUnreadCounts(userId);
    }

    @Override
    public int getTotalUnreadCount(Long userId) {
        return getAccurateTotalUnreadCount(userId);
    }

    private int getAccurateTotalUnreadCount(Long userId) {
        int dbTotal = countUnreadFromDb(userId);
        syncTotalUnreadCountToRedis(userId, dbTotal);
        return dbTotal;
    }

    private int countUnreadFromDb(Long userId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getReceiverId, userId)
                .eq(ChatMessage::getStatus, 0);
        return Math.toIntExact(this.count(wrapper));
    }

    private void syncTotalUnreadCountFromDb(Long userId) {
        int latestTotal = countUnreadFromDb(userId);
        syncTotalUnreadCountToRedis(userId, latestTotal);
    }

    private void syncTotalUnreadCountToRedis(Long userId, int total) {
        String redisKey = UNREAD_TOTAL_KEY + userId;
        try {
            redisTemplate.opsForValue().set(redisKey, Math.max(0, total));
        } catch (Exception e) {
            log.warn("sync total unread to redis failed, user_id={}, total={}", userId, total, e);
        }
    }

    private String generateSessionId(Long userId1, Long userId2) {
        return userId1 < userId2 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }

    private void addExpWithLimit(Long userId, String messageType, Long messageId) {
        try {
            String limitKey = EXP_LIMIT_KEY + userId + ":" + System.currentTimeMillis() / 60000;
            Long count = redisTemplate.opsForValue().increment(limitKey);
            if (count == null) {
                count = 1L;
            }
            if (count == 1) {
                redisTemplate.expire(limitKey, 60, TimeUnit.SECONDS);
            }
            if (count > 5) {
                return;
            }

            ExpAddRequest request = new ExpAddRequest();
            request.setUserId(userId);
            request.setActionType(getActionTypeByMessageType(messageType));
            request.setRelatedId(messageId);
            request.setRelatedType("chat_message");
            request.setDescription("send " + getMessageTypeDescription(messageType) + " message");
            userLevelService.addExp(request);

            ChatMessage message = this.getById(messageId);
            if (message != null) {
                message.setExpAwarded(1);
                this.updateById(message);
            }
        } catch (Exception e) {
            log.error("add exp failed", e);
        }
    }

    private String getActionTypeByMessageType(String messageType) {
        switch (messageType) {
            case "image":
                return "chat_send_image";
            case "video":
                return "chat_send_video";
            case "audio":
                return "chat_send_audio";
            case "text":
            default:
                return "chat_send_text";
        }
    }

    private String getMessageTypeDescription(String messageType) {
        switch (messageType) {
            case "image":
                return "image";
            case "video":
                return "video";
            case "audio":
                return "audio";
            case "text":
                return "text";
            default:
                return "unknown";
        }
    }

    private void recognizeSpeechAsync(ChatMessage message) {
        CompletableFuture.runAsync(() -> {
            try {
                String text = speechRecognitionService.recognizeSpeech(message.getAudioUrl());
                if (StrUtil.isNotBlank(text)) {
                    message.setAudioText(text);
                    this.updateById(message);
                }
            } catch (Exception e) {
                log.error("speech recognition failed", e);
            }
        });
    }

    private void pushMessageToReceiver(ChatMessage message) {
        try {
            String onlineKey = "chat:online:" + message.getReceiverId();
            Boolean isOnline = (Boolean) redisTemplate.opsForValue().get(onlineKey);

            if (Boolean.TRUE.equals(isOnline)) {
                channelManager.sendMessage(message.getReceiverId(), JSON.toJSONString(message));
                return;
            }

            String offlineKey = OFFLINE_MESSAGE_KEY + message.getReceiverId();
            redisTemplate.opsForList().leftPush(offlineKey, message);
            redisTemplate.expire(offlineKey, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("push message failed, fallback to direct push", e);
            try {
                channelManager.sendMessage(message.getReceiverId(), JSON.toJSONString(message));
            } catch (Exception ignored) {
                // no-op
            }
        }
    }

    private void increaseTotalUnreadCount(Long userId) {
        String redisKey = UNREAD_TOTAL_KEY + userId;
        try {
            redisTemplate.opsForValue().increment(redisKey);
        } catch (Exception e) {
            log.warn("increase total unread failed, user_id={}", userId, e);
        }
    }

    private void decreaseTotalUnreadCountBy(Long userId, Long count) {
        if (count == null || count <= 0) {
            return;
        }

        String redisKey = UNREAD_TOTAL_KEY + userId;
        try {
            redisTemplate.opsForValue().decrement(redisKey, count);
            Integer current = getUnreadTotalFromRedis(userId);
            if (current != null && current < 0) {
                redisTemplate.opsForValue().set(redisKey, 0);
            }
        } catch (Exception e) {
            log.warn("decrease total unread failed, user_id={}, count={}", userId, count, e);
        }
    }

    private Integer getUnreadTotalFromRedis(Long userId) {
        String redisKey = UNREAD_TOTAL_KEY + userId;
        try {
            Object totalObj = redisTemplate.opsForValue().get(redisKey);
            if (totalObj == null) {
                return null;
            }
            if (totalObj instanceof Integer) {
                return (Integer) totalObj;
            }
            if (totalObj instanceof Long) {
                return ((Long) totalObj).intValue();
            }
            return Integer.parseInt(String.valueOf(totalObj));
        } catch (Exception e) {
            log.warn("read total unread from redis failed, user_id={}", userId, e);
            return null;
        }
    }
}
