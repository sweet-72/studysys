package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 私聊消息实体类
 */
@TableName(value = "chat_message")
@Data
public class ChatMessage implements Serializable {

    /**
     * 消息 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送者 ID
     */
    @TableField("sender_id")
    private Long senderId;

    /**
     * 接收者 ID
     */
    @TableField("receiver_id")
    private Long receiverId;

    /**
     * 会话 ID（senderId_receiverId 排序组合）
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 消息类型：text-文本，image-图片，video-视频，audio-语音
     */
    @TableField("message_type")
    private String messageType;

    /**
     * 消息内容（文本或媒体 URL）
     */
    @TableField("content")
    private String content;

    /**
     * 语音文件 URL
     */
    @TableField("audio_url")
    private String audioUrl;

    /**
     * 语音时长（秒）
     */
    @TableField("audio_duration")
    private Integer audioDuration;

    /**
     * 语音识别的文字内容
     */
    @TableField("audio_text")
    private String audioText;

    /**
     * 图片/视频 URL
     */
    @TableField("media_url")
    private String mediaUrl;

    /**
     * 文件大小（字节）
     */
    @TableField("media_size")
    private Long mediaSize;

    /**
     * 视频/音频时长（秒）
     */
    @TableField("media_duration")
    private Integer mediaDuration;

    /**
     * 消息状态：0-未读，1-已读，2-已删除
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否撤回：0-否，1-是
     */
    @TableField("is_revoke")
    private Integer isRevoke;

    /**
     * 撤回时间
     */
    @TableField("revoke_time")
    private Date revokeTime;

    /**
     * 是否已发放经验：0-否，1-是
     */
    @TableField("exp_awarded")
    private Integer expAwarded;

    /**
     * 发送时间
     */
    @TableField("send_time")
    private Date sendTime;

    /**
     * 阅读时间
     */
    @TableField("read_time")
    private Date readTime;

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
