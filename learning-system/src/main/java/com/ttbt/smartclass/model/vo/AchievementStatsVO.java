package com.ttbt.smartclass.model.vo;

import lombok.Data;

/**
 * 成就统计 VO
 */
@Data
public class AchievementStatsVO {

    /**
     * 总成就数
     */
    private Integer totalAchievements;

    /**
     * 已完成成就数
     */
    private Integer completedAchievements;

    /**
     * 进行中成就数
     */
    private Integer inProgressAchievements;

    /**
     * 成就完成度百分比
     */
    private Double completionRate;

    /**
     * 成就点数总和
     */
    private Integer totalPoints;

    /**
     * 已领取奖励的成就数
     */
    private Integer claimedCount;
}
