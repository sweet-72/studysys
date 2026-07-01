package com.ttbt.smartclass.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * AI分身使用趋势 VO
 */
@Data
@ApiModel(description = "AI分身使用趋势")
public class AiAvatarUsageTrendVO implements Serializable {

    @ApiModelProperty("日期 (YYYY-MM-DD)")
    private String date;

    @ApiModelProperty("各智能体的使用次数，key为智能体名称，value为使用次数")
    private Map<String, Long> avatarUsage;

    private static final long serialVersionUID = 1L;
}
