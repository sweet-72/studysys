package com.ttbt.smartclass.controller;

import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.model.vo.DashboardOverviewVO;
import com.ttbt.smartclass.model.vo.UserActivityTrendItemVO;
import com.ttbt.smartclass.service.DashboardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 数据看板控制器
 */
@Slf4j
@RestController
@RequestMapping("/dashboard")
@Api(tags = "数据看板接口")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    /**
     * 获取数据看板总览
     */
    @GetMapping("/overview")
    @ApiOperation("获取数据看板总览")
    public BaseResponse<DashboardOverviewVO> getDashboardOverview() {
        DashboardOverviewVO overview = dashboardService.getDashboardOverview();
        return ResultUtils.success(overview);
    }

    /**
     * 获取用户活跃度趋势
     */
    @GetMapping("/user-activity-trend")
    @ApiOperation("获取近6个月用户活跃度趋势")
    public BaseResponse<List<UserActivityTrendItemVO>> getUserActivityTrend() {
        List<UserActivityTrendItemVO> trendList = dashboardService.getUserActivityTrend();
        return ResultUtils.success(trendList);
    }
}
