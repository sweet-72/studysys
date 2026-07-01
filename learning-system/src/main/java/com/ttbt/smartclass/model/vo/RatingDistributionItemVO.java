package com.ttbt.smartclass.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 评分分布项 VO
 */
@Data
@ApiModel(description = "评分分布项")
public class RatingDistributionItemVO implements Serializable {

    @ApiModelProperty("智能体名称")
    private String avatarName;

    @ApiModelProperty("平均评分")
    private BigDecimal avgRating;

    @ApiModelProperty("评分人数")
    private Integer ratingCount;

    @ApiModelProperty("使用次数")
    private Long usageCount;

    private static final long serialVersionUID = 1L;
}
