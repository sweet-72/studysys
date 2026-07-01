package com.ttbt.smartclass.service;

import com.ttbt.smartclass.model.vo.DashboardOverviewVO;
import com.ttbt.smartclass.model.vo.UserActivityTrendItemVO;

import java.util.List;

/**
 * 数据看板服务
 */
public interface DashboardService {

    /**
     * 获取数据看板总览
     */
    DashboardOverviewVO getDashboardOverview();

    /**
     * 获取近6个月用户活跃度趋势
     */
    List<UserActivityTrendItemVO> getUserActivityTrend();
}
