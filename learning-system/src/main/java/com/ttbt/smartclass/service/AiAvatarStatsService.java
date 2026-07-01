package com.ttbt.smartclass.service;

import com.ttbt.smartclass.model.vo.*;

import java.util.List;

/**
 * AI分身统计服务
 */
public interface AiAvatarStatsService {

    /**
     * 获取AI分身统计总览
     */
    AiAvatarStatsOverviewVO getStatsOverview();

    /**
     * 获取使用趋势（按日期和智能体分组）
     * @param days 天数（7=本周，30=本月，365=本年）
     */
    List<AiAvatarUsageTrendVO> getUsageTrend(Integer days);

    /**
     * 获取用户分布
     */
    List<UserDistributionItemVO> getUserDistribution();

    /**
     * 获取评分分布
     */
    List<RatingDistributionItemVO> getRatingDistribution();

    /**
     * 获取对话时长分布
     * @param days 天数
     */
    List<ConversationDurationVO> getConversationDuration(Integer days);
}
