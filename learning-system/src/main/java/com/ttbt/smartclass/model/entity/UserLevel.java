package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户等级实体类
 */
@TableName(value = "user_level")
@Data
public class UserLevel implements Serializable {

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
     * 当前等级
     */
    private Integer level;

    /**
     * 当前经验值
     */
    private Integer exp;

    /**
     * 下一级所需经验值
     */
    @TableField("next_level_exp")
    private Integer nextLevelExp;

    /**
     * 累计获得经验值
     */
    @TableField("total_exp")
    private Integer totalExp;

    /**
     * 上次升级时间
     */
    @TableField("level_up_time")
    private Date levelUpTime;

    /**
     * 连续登录天数
     */
    @TableField("continuous_login_days")
    private Integer continuousLoginDays;

    /**
     * 最后登录日期
     */
    @TableField("last_login_date")
    private Date lastLoginDate;

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
