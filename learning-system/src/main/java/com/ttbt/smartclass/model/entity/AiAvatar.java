package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * AI 数字人实体。
 */
@TableName(value = "ai_avatar")
@Data
public class AiAvatar implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    @TableField("base_url")
    private String baseUrl;

    private String description;

    @TableField("avatar_img_url")
    private String avatarImgUrl;

    @TableField("avatar_auth")
    private String avatarAuth;

    private String tags;

    private String personality;

    private String abilities;

    @TableField("is_public")
    private Integer isPublic;

    private Integer status;

    @TableField("usage_count")
    private Integer usageCount;

    private BigDecimal rating;

    @TableField("rating_count")
    private Integer ratingCount;

    @TableField("creator_id")
    private Long creatorId;

    private Integer sort;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("is_delete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
