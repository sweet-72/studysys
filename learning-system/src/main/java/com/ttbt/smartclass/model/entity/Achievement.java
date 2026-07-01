package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 成就定义
 * @TableName achievement
 */
@TableName(value ="achievement")
@Data
public class Achievement implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 成就名称
     */
    private String name;

    /**
     * 成就描述
     */
    private String description;

    /**
     * 成就图标 URL
     */
    @TableField("icon_url")
    private String iconUrl;
    
    /**
     * 成就徽章 URL
     */
    @TableField("badge_url")
    private String badgeUrl;

    /**
     * 成就分类，如：学习、社交、活动等
     */
    @TableField("category")
    private String category;
    
    /**
     * 成就等级：1-普通，2-稀有，3-史诗，4-传说
     */
    @TableField("level")
    private Integer level;
    
    /**
     * 成就点数
     */
    @TableField("points")
    private Integer points;
    
    /**
     * 获取条件描述
     */
    @TableField("achievement_condition")
    private String achievementCondition;
    
    /**
     * 条件类型，如：course_complete, login_days, article_read 等
     */
    @TableField("condition_type")
    private String conditionType;
    
    /**
     * 条件值，如完成 10 门课程，登录 30 天等
     */
    @TableField("condition_value")
    private Integer conditionValue;
    
    /**
     * 是否隐藏成就：0-否，1-是，隐藏成就不会提前显示给用户
     */
    @TableField("is_hidden")
    private Integer isHidden;

    @TableField("sort")
    private Integer sort;
    
    /**
     * 创建管理员 id
     */
    @TableField("admin_id")
    private Long adminId;
    
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
    @TableLogic
    @TableField("is_delete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}