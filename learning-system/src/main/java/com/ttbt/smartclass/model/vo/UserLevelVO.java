package com.ttbt.smartclass.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 用户等级信息 VO
 */
@Data
public class UserLevelVO {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 当前等级
     */
    private Integer level;

    /**
     * 等级名称
     */
    private String levelName;

    /**
     * 当前经验值
     */
    private Integer exp;

    /**
     * 下一级所需经验值
     */
    private Integer nextLevelExp;

    /**
     * 累计获得经验值
     */
    private Integer totalExp;

    /**
     * 升级进度百分比
     */
    private Double progressPercent;

    /**
     * 距离下一级还需经验值
     */
    private Integer expToNextLevel;

    /**
     * 连续登录天数
     */
    private Integer continuousLoginDays;

    /**
     * 上次升级时间
     */
    private Date levelUpTime;

    /**
     * 等级图标 URL
     */
    private String iconUrl;

    /**
     * 特权描述
     */
    private String privilegeDesc;
}
