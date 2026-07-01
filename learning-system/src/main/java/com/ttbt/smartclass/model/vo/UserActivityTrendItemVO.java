package com.ttbt.smartclass.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户活跃度趋势数据项 VO
 */
@Data
@ApiModel(description = "用户活跃度趋势数据项")
public class UserActivityTrendItemVO implements Serializable {

    @ApiModelProperty("日期 (YYYY-MM)")
    private String month;

    @ApiModelProperty("月活跃用户数")
    private Long monthlyActiveUsers;

    @ApiModelProperty("新增用户数")
    private Long newUsers;

    @ApiModelProperty("登录次数")
    private Long loginCount;

    private static final long serialVersionUID = 1L;
}
