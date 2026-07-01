package com.ttbt.smartclass.service;

import com.ttbt.smartclass.model.entity.Achievement;
import com.ttbt.smartclass.model.entity.UserAchievement;
import com.ttbt.smartclass.model.vo.AchievementStatsVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 成就服务接口
 */
public interface AchievementService extends IService<Achievement> {

    /**
     * 检查并更新用户成就
     * 
     * @param userId 用户 ID
     * @param actionType 行为类型
     * @param relatedId 关联 ID
     */
    void checkAndUpdateUserAchievement(Long userId, String actionType, Long relatedId);

    /**
     * 获取用户的所有成就
     * 
     * @param userId 用户 ID
     * @return 用户成就列表
     */
    List<UserAchievement> getUserAchievements(Long userId);

    /**
     * 领取成就奖励
     * 
     * @param userId 用户 ID
     * @param achievementId 成就 ID
     * @return 是否领取成功
     */
    boolean claimAchievementReward(Long userId, Long achievementId);

    /**
     * 获取用户的成就进度统计
     * 
     * @param userId 用户 ID
     * @return 成就统计信息
     */
    AchievementStatsVO getAchievementStats(Long userId);
}
