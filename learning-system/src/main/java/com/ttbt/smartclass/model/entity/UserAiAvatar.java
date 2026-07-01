package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 用户 AI 分身关联
 * @TableName user_ai_avatar
 */
@TableName(value = "user_ai_avatar")
@Data
public class UserAiAvatar implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * AI 分身 id
     */
    @TableField("ai_avatar_id")
    private Long aiAvatarId;

    /**
     * 是否收藏：0-否，1-是
     */
    @TableField("is_favorite")
    private Integer isFavorite;

    /**
     * 最后使用时间
     */
    @TableField("last_use_time")
    private Date lastUseTime;

    /**
     * 使用次数
     */
    @TableField("use_count")
    private Integer useCount;

    /**
     * 用户评分：1-5 分
     */
    @TableField("user_rating")
    private BigDecimal userRating;

    /**
     * 用户反馈
     */
    @TableField("user_feedback")
    private String userFeedback;

    /**
     * 用户自定义设置，JSON 格式
     */
    @TableField("custom_settings")
    private String customSettings;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
