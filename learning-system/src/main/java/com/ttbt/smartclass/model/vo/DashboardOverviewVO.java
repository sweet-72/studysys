package com.ttbt.smartclass.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据看板总览 VO
 */
@Data
@ApiModel(description = "数据看板总览")
public class DashboardOverviewVO implements Serializable {

    @ApiModelProperty("总用户数")
    private Long totalUsers;

    @ApiModelProperty("今日活跃用户")
    private Long todayActiveUsers;

    @ApiModelProperty("总用户增长率（百分比）")
    private String totalUsersGrowth;

    @ApiModelProperty("班级总数")
    private Long totalClasses;

    @ApiModelProperty("帖子总数")
    private Long totalPosts;

    @ApiModelProperty("帖子增长率（百分比）")
    private String totalPostsGrowth;

    @ApiModelProperty("课程总数")
    private Long totalCourses;

    @ApiModelProperty("课程增长率（百分比）")
    private String totalCoursesGrowth;

    @ApiModelProperty("AI分身数量")
    private Long totalAiAvatars;

    @ApiModelProperty("单词库数量")
    private Long totalWords;

    @ApiModelProperty("单词库增长率（百分比）")
    private String totalWordsGrowth;

    @ApiModelProperty("美文数量")
    private Long totalArticles;

    private static final long serialVersionUID = 1L;
}
