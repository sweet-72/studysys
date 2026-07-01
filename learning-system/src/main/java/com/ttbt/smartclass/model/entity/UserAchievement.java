package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户成就
 */
@TableName(value = "user_achievement")
@Data
public class UserAchievement implements Serializable {

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
     * 成就 id
     */
    @TableField("achievement_id")
    private Long achievementId;

    /**
     * 当前进度值
     */
    private Integer progress;

    /**
     * 目标进度值
     */
    @TableField("progress_max")
    private Integer progressMax;

    /**
     * 是否完成
     */
    @TableField("is_completed")
    private Integer isCompleted;

    /**
     * 完成时间
     */
    @TableField("completed_time")
    private Date completedTime;

    /**
     * 是否已领取奖励
     */
    @TableField("reward_claimed")
    private Integer rewardClaimed;

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
