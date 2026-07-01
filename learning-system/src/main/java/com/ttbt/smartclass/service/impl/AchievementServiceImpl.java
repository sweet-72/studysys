package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ttbt.smartclass.mapper.AchievementMapper;
import com.ttbt.smartclass.mapper.UserAchievementMapper;
import com.ttbt.smartclass.model.entity.Achievement;
import com.ttbt.smartclass.model.entity.UserAchievement;
import com.ttbt.smartclass.model.enums.ActionTypeEnum;
import com.ttbt.smartclass.model.vo.AchievementStatsVO;
import com.ttbt.smartclass.service.AchievementService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 成就服务实现类
 */
@Slf4j
@Service
public class AchievementServiceImpl extends ServiceImpl<AchievementMapper, Achievement> 
        implements AchievementService {

    @Resource
    private AchievementMapper achievementMapper;

    @Resource
    private UserAchievementMapper userAchievementMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis Key 前缀
     */
    private static final String ACHIEVEMENT_PROGRESS_KEY_PREFIX = "achievement:progress:";
    private static final String ACHIEVEMENT_COMPLETED_KEY_PREFIX = "achievement:completed:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndUpdateUserAchievement(Long userId, String actionType, Long relatedId) {
        log.info("检查用户 {} 的成就，行为类型：{}", userId, actionType);

        // 1. 查询所有相关的成就
        LambdaQueryWrapper<Achievement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Achievement::getConditionType, actionType);
        List<Achievement> achievements = achievementMapper.selectList(queryWrapper);

        if (achievements.isEmpty()) {
            return;
        }

        // 2. 更新每个成就的进度
        for (Achievement achievement : achievements) {
            updateUserAchievementProgress(userId, achievement, actionType, relatedId);
        }
    }

    @Override
    public List<UserAchievement> getUserAchievements(Long userId) {
        LambdaQueryWrapper<UserAchievement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAchievement::getUserId, userId);
        return userAchievementMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean claimAchievementReward(Long userId, Long achievementId) {
        // 1. 查询用户成就
        UserAchievement userAchievement = getUserAchievement(userId, achievementId);
        if (userAchievement == null || userAchievement.getIsCompleted() != 1) {
            log.warn("用户 {} 成就 {} 未完成，无法领取奖励", userId, achievementId);
            return false;
        }

        // 2. 检查是否已领取
        if (userAchievement.getRewardClaimed() == 1) {
            log.warn("用户 {} 成就 {} 奖励已领取", userId, achievementId);
            return false;
        }

        // 3. 标记为已领取
        userAchievement.setRewardClaimed(1);
        userAchievementMapper.updateById(userAchievement);

        // 4. 发放奖励（此处可扩展：发放积分、优惠券等）
        grantReward(userId, achievementId);

        log.info("用户 {} 成功领取成就 {} 的奖励", userId, achievementId);
        return true;
    }

    @Override
    public AchievementStatsVO getAchievementStats(Long userId) {
        AchievementStatsVO stats = new AchievementStatsVO();

        // 查询用户所有成就
        List<UserAchievement> userAchievements = getUserAchievements(userId);

        int total = userAchievements.size();
        int completed = 0;
        int inProgress = 0;
        int claimed = 0;
        int totalPoints = 0;

        for (UserAchievement ua : userAchievements) {
            if (ua.getIsCompleted() == 1) {
                completed++;
                if (ua.getRewardClaimed() == 1) {
                    claimed++;
                }
                // 累加成就是点数
                Achievement achievement = achievementMapper.selectById(ua.getAchievementId());
                if (achievement != null) {
                    totalPoints += achievement.getPoints();
                }
            } else {
                inProgress++;
            }
        }

        stats.setTotalAchievements(total);
        stats.setCompletedAchievements(completed);
        stats.setInProgressAchievements(inProgress);
        stats.setCompletionRate(total > 0 ? (double) completed / total * 100 : 0.0);
        stats.setTotalPoints(totalPoints);
        stats.setClaimedCount(claimed);

        return stats;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 更新用户成就进度
     */
    private void updateUserAchievementProgress(Long userId, Achievement achievement, 
                                               String actionType, Long relatedId) {
        // 1. 查询或创建用户成就记录
        UserAchievement userAchievement = getOrCreateUserAchievement(userId, achievement.getId());

        // 2. 根据条件类型更新进度
        int newProgress = calculateProgress(userAchievement, achievement, actionType, relatedId);
        
        // 3. 更新进度
        userAchievement.setProgress(newProgress);
        userAchievement.setProgressMax(achievement.getConditionValue());

        // 4. 检查是否完成
        if (newProgress >= achievement.getConditionValue() && userAchievement.getIsCompleted() != 1) {
            userAchievement.setIsCompleted(1);
            userAchievement.setCompletedTime(new Date());
            log.info("🎉 用户 {} 完成成就：{}！", userId, achievement.getName());
            
            // 5. 发放经验奖励（集成成长体系）
            grantExpReward(userId, achievement);
        }

        userAchievementMapper.updateById(userAchievement);
    }

    /**
     * 获取或创建用户成就
     */
    private UserAchievement getOrCreateUserAchievement(Long userId, Long achievementId) {
        LambdaQueryWrapper<UserAchievement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAchievement::getUserId, userId)
                .eq(UserAchievement::getAchievementId, achievementId);
        
        UserAchievement userAchievement = userAchievementMapper.selectOne(queryWrapper);
        
        if (userAchievement == null) {
            userAchievement = new UserAchievement();
            userAchievement.setUserId(userId);
            userAchievement.setAchievementId(achievementId);
            userAchievement.setProgress(0);
            userAchievement.setProgressMax(1);
            userAchievement.setIsCompleted(0);
            userAchievement.setRewardClaimed(0);
            userAchievementMapper.insert(userAchievement);
        }
        
        return userAchievement;
    }

    /**
     * 计算新的进度值
     */
    private int calculateProgress(UserAchievement userAchievement, Achievement achievement,
                                  String actionType, Long relatedId) {
        // 根据不同行为类型计算进度
        switch (actionType) {
            case "course_section_complete":
                return userAchievement.getProgress() + 1;
            case "daily_login":
                return userAchievement.getProgress() + 1;
            case "post_create":
                return userAchievement.getProgress() + 1;
            default:
                return userAchievement.getProgress() + 1;
        }
    }

    /**
     * 发放经验奖励
     */
    private void grantExpReward(Long userId, Achievement achievement) {
        // TODO: 调用成长体系的 addExp 方法
        // ExpAddRequest request = new ExpAddRequest();
        // request.setUserId(userId);
        // request.setActionType(ActionTypeEnum.ACHIEVEMENT_UNLOCKED_BASIC.getCode());
        // userLevelService.addExp(request);
        log.info("用户 {} 获得成就奖励：{} 点经验", userId, achievement.getPoints());
    }

    /**
     * 发放其他奖励
     */
    private void grantReward(Long userId, Long achievementId) {
        // TODO: 扩展其他奖励类型（积分、优惠券等）
        log.info("用户 {} 领取成就 {} 的奖励", userId, achievementId);
    }

    /**
     * 获取用户成就
     */
    private UserAchievement getUserAchievement(Long userId, Long achievementId) {
        LambdaQueryWrapper<UserAchievement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAchievement::getUserId, userId)
                .eq(UserAchievement::getAchievementId, achievementId);
        return userAchievementMapper.selectOne(queryWrapper);
    }
}
