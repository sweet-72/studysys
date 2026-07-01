package com.ttbt.smartclass.controller;

import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.model.vo.*;
import com.ttbt.smartclass.service.AiAvatarStatsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * AI分身统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/ai-avatar-stats")
@Api(tags = "AI分身统计接口")
public class AiAvatarStatsController {

    @Resource
    private AiAvatarStatsService aiAvatarStatsService;

    /**
     * 获取统计总览
     */
    @GetMapping("/overview")
    @ApiOperation("获取AI分身统计总览")
    public BaseResponse<AiAvatarStatsOverviewVO> getStatsOverview() {
        AiAvatarStatsOverviewVO overview = aiAvatarStatsService.getStatsOverview();
        return ResultUtils.success(overview);
    }

    /**
     * 获取使用趋势
     */
    @GetMapping("/usage-trend")
    @ApiOperation("获取AI分身使用趋势")
    public BaseResponse<List<AiAvatarUsageTrendVO>> getUsageTrend(
            @ApiParam("时间范围天数：7=本周，30=本月，365=本年")
            @RequestParam(defaultValue = "7") Integer days) {
        List<AiAvatarUsageTrendVO> trendList = aiAvatarStatsService.getUsageTrend(days);
        return ResultUtils.success(trendList);
    }

    /**
     * 获取用户分布
     */
    @GetMapping("/user-distribution")
    @ApiOperation("获取用户分布")
    public BaseResponse<List<UserDistributionItemVO>> getUserDistribution() {
        List<UserDistributionItemVO> distribution = aiAvatarStatsService.getUserDistribution();
        return ResultUtils.success(distribution);
    }

    /**
     * 获取评分分布
     */
    @GetMapping("/rating-distribution")
    @ApiOperation("获取评分分布")
    public BaseResponse<List<RatingDistributionItemVO>> getRatingDistribution() {
        List<RatingDistributionItemVO> distribution = aiAvatarStatsService.getRatingDistribution();
        return ResultUtils.success(distribution);
    }

    /**
     * 获取对话时长分布
     */
    @GetMapping("/conversation-duration")
    @ApiOperation("获取对话时长分布")
    public BaseResponse<List<ConversationDurationVO>> getConversationDuration(
            @ApiParam("时间范围天数：7=本周，30=本月，365=本年")
            @RequestParam(defaultValue = "7") Integer days) {
        List<ConversationDurationVO> durationList = aiAvatarStatsService.getConversationDuration(days);
        return ResultUtils.success(durationList);
    }
}
