package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * AI 数字人聊天历史实体。
 */
@TableName(value = "ai_avatar_chat_history")
@Data
public class AiAvatarChatHistory implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("ai_avatar_id")
    private Long aiAvatarId;

    @TableField("session_id")
    private String sessionId;

    @TableField("dify_conversation_id")
    private String difyConversationId;

    @TableField("session_name")
    private String sessionName;

    @TableField("message_type")
    private String messageType;

    private String content;

    private Integer tokens;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("is_delete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
