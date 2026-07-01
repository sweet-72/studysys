package com.ttbt.smartclass.controller;

import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.model.entity.Achievement;
import com.ttbt.smartclass.model.entity.UserAchievement;
import com.ttbt.smartclass.model.vo.AchievementStatsVO;
import com.ttbt.smartclass.service.AchievementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 成就系统 Controller
 */
@Slf4j
@RestController
@RequestMapping("/achievement")
@Api(tags = "成就系统接口")
public class AchievementController {

    @Resource
    private AchievementService achievementService;

    /**
     * 查询所有可用成就（管理员）
     */
    @GetMapping("/list")
    @ApiOperation("查询所有成就")
    public BaseResponse<List<Achievement>> listAchievements() {
        List<Achievement> achievements = achievementService.list();
        return ResultUtils.success(achievements);
    }

    /**
     * 获取用户的所有成就
     */
    @GetMapping("/user/{userId}")
    @ApiOperation("获取用户成就")
    public BaseResponse<List<UserAchievement>> getUserAchievements(@PathVariable Long userId) {
        List<UserAchievement> userAchievements = achievementService.getUserAchievements(userId);
        return ResultUtils.success(userAchievements);
    }

    /**
     * 获取用户成就统计
     */
    @GetMapping("/user/{userId}/stats")
    @ApiOperation("获取用户成就统计")
    public BaseResponse<AchievementStatsVO> getAchievementStats(@PathVariable Long userId) {
        AchievementStatsVO stats = achievementService.getAchievementStats(userId);
        return ResultUtils.success(stats);
    }

    /**
     * 领取成就奖励
     */
    @PostMapping("/user/{userId}/claim/{achievementId}")
    @ApiOperation("领取成就奖励")
    public BaseResponse<Boolean> claimAchievementReward(
            @PathVariable Long userId,
            @PathVariable Long achievementId) {
        boolean success = achievementService.claimAchievementReward(userId, achievementId);
        return ResultUtils.success(success);
    }

    /**
     * 检查并更新用户成就进度（内部调用）
     */
    @PostMapping("/check")
    @ApiOperation("检查成就进度")
    public BaseResponse<Void> checkAchievement(
            @RequestParam Long userId,
            @RequestParam String actionType,
            @RequestParam(required = false) Long relatedId) {
        achievementService.checkAndUpdateUserAchievement(userId, actionType, relatedId);
        return ResultUtils.success(null);
    }
}
