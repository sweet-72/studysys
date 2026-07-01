package com.ttbt.smartclass.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户分布项 VO
 */
@Data
@ApiModel(description = "用户分布项")
public class UserDistributionItemVO implements Serializable {

    @ApiModelProperty("用户类型")
    private String userType;

    @ApiModelProperty("用户数量")
    private Long count;

    @ApiModelProperty("占比（百分比）")
    private Double percentage;

    private static final long serialVersionUID = 1L;
}
