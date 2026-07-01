package com.ttbt.smartclass.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * AI分身统计总览 VO
 */
@Data
@ApiModel(description = "AI分身统计总览")
public class AiAvatarStatsOverviewVO implements Serializable {

    @ApiModelProperty("总用户数")
    private Long totalUsers;

    @ApiModelProperty("总对话数")
    private Long totalConversations;

    @ApiModelProperty("平均对话时长（分钟）")
    private Double avgConversationDuration;

    @ApiModelProperty("平均评分")
    private BigDecimal avgRating;

    private static final long serialVersionUID = 1L;
}
