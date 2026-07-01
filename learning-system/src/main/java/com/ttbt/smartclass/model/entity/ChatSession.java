package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 私聊会话实体类
 */
@TableName(value = "chat_session")
@Data
public class ChatSession implements Serializable {

    /**
     * 会话 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话 ID（两个用户 ID 排序后组合）
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 参与者 1 ID（较小 ID）
     */
    @TableField("user1_id")
    private Long user1Id;

    /**
     * 参与者 2 ID（较大 ID）
     */
    @TableField("user2_id")
    private Long user2Id;

    /**
     * 最后一条消息 ID
     */
    @TableField("last_message_id")
    private Long lastMessageId;

    /**
     * 最后一条消息内容
     */
    @TableField("last_message_content")
    private String lastMessageContent;

    /**
     * 最后一条消息类型
     */
    @TableField("last_message_type")
    private String lastMessageType;

    /**
     * 最后一条消息时间
     */
    @TableField("last_message_time")
    private Date lastMessageTime;

    /**
     * 用户 1 未读消息数
     */
    @TableField("user1_unread_count")
    private Integer user1UnreadCount;

    /**
     * 用户 2 未读消息数
     */
    @TableField("user2_unread_count")
    private Integer user2UnreadCount;

    /**
     * 用户 1 是否删除会话
     */
    @TableField("is_user1_deleted")
    private Integer isUser1Deleted;

    /**
     * 用户 2 是否删除会话
     */
    @TableField("is_user2_deleted")
    private Integer isUser2Deleted;

    /**
     * 用户 1 是否置顶
     */
    @TableField("is_user1_top")
    private Integer isUser1Top;

    /**
     * 用户 2 是否置顶
     */
    @TableField("is_user2_top")
    private Integer isUser2Top;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableField("is_delete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
