package com.ttbt.smartclass.mapper;

import com.ttbt.smartclass.model.entity.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 私聊消息 Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 更新消息状态为已读
     */
    @Update("UPDATE chat_message SET status = 1, read_time = NOW() WHERE id = #{messageId} AND status = 0")
    int markAsRead(@Param("messageId") Long messageId);

    /**
     * 批量更新会话消息为已读
     */
    @Update("UPDATE chat_message SET status = 1, read_time = NOW() WHERE session_id = #{sessionId} AND receiver_id = #{receiver_id} AND status = 0")
    int markSessionMessagesAsRead(@Param("sessionId") String sessionId, @Param("receiver_id") Long receiverId);

    /**
     * 统计未读消息数
     */
    int countUnreadMessages(@Param("user_id") Long userId, @Param("sessionId") String sessionId);
}
