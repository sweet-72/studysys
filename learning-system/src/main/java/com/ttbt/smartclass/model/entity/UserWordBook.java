package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户生词本
 * @TableName user_word_book
 */
@TableName(value = "user_word_book")
@Data
public class UserWordBook implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 id，关联到 user 表
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 单词 id，关联到 daily_word 表
     */
    @TableField("word_id")
    private Long wordId;

    /**
     * 学习状态：0-未学习，1-已学习，2-已掌握
     */
    @TableField("learning_status")
    private Integer learningStatus;

    /**
     * 是否收藏：0-否，1-是
     */
    @TableField("is_collected")
    private Integer isCollected;

    /**
     * 收藏时间
     */
    @TableField("collected_time")
    private Date collectedTime;

    /**
     * 难度等级：1-简单，2-中等，3-困难
     */
    private Integer difficulty;

    /**
     * 是否删除：0-否，1-是
     */
    @TableField("is_deleted")
    private Integer isDeleted;

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
