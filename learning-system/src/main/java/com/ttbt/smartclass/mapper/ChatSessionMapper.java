package com.ttbt.smartclass.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ttbt.smartclass.model.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 聊天会话数据访问接口。
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    /**
     * Update last message and increment unread count for target receiver.
     */
    @Update("<script>" +
            "UPDATE chat_session SET " +
            "last_message_id = #{lastMessageId}, " +
            "last_message_content = #{lastMessageContent}, " +
            "last_message_type = #{lastMessageType}, " +
            "last_message_time = NOW(), " +
            "user1_unread_count = CASE WHEN #{targetUserId} = #{user1Id} THEN user1_unread_count + 1 ELSE user1_unread_count END, " +
            "user2_unread_count = CASE WHEN #{targetUserId} = #{user2Id} THEN user2_unread_count + 1 ELSE user2_unread_count END " +
            "WHERE session_id = #{sessionId}" +
            "</script>")
    int updateLastMessage(@Param("sessionId") String sessionId,
                          @Param("lastMessageId") Long lastMessageId,
                          @Param("lastMessageContent") String lastMessageContent,
                          @Param("lastMessageType") String lastMessageType,
                          @Param("targetUserId") Long targetUserId,
                          @Param("user1Id") Long user1Id,
                          @Param("user2Id") Long user2Id);

    /**
     * Clear unread count for one participant in a session.
     */
    @Update("<script>" +
            "UPDATE chat_session SET " +
            "user1_unread_count = CASE WHEN #{user_id} = #{user1Id} THEN 0 ELSE user1_unread_count END, " +
            "user2_unread_count = CASE WHEN #{user_id} = #{user2Id} THEN 0 ELSE user2_unread_count END " +
            "WHERE session_id = #{sessionId}" +
            "</script>")
    int clearUnreadCount(@Param("sessionId") String sessionId,
                         @Param("user_id") Long userId,
                         @Param("user1Id") Long user1Id,
                         @Param("user2Id") Long user2Id);

    /**
     * Decrease unread count for one participant in a session.
     */
    @Update("<script>" +
            "UPDATE chat_session SET " +
            "user1_unread_count = CASE WHEN #{user_id} = #{user1Id} THEN GREATEST(user1_unread_count - #{delta}, 0) ELSE user1_unread_count END, " +
            "user2_unread_count = CASE WHEN #{user_id} = #{user2Id} THEN GREATEST(user2_unread_count - #{delta}, 0) ELSE user2_unread_count END " +
            "WHERE session_id = #{sessionId}" +
            "</script>")
    int decreaseUnreadCount(@Param("sessionId") String sessionId,
                            @Param("user_id") Long userId,
                            @Param("delta") Integer delta,
                            @Param("user1Id") Long user1Id,
                            @Param("user2Id") Long user2Id);

    /**
     * Query by business session id.
     */
    ChatSession selectBySessionId(@Param("sessionId") String sessionId);
}
