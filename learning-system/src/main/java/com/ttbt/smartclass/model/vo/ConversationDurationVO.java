package com.ttbt.smartclass.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 对话时长分布 VO
 */
@Data
@ApiModel(description = "对话时长分布")
public class ConversationDurationVO implements Serializable {

    @ApiModelProperty("日期 (YYYY-MM-DD)")
    private String date;

    @ApiModelProperty("各智能体的平均对话时长（分钟）")
    private List<AvatarDurationItem> avatarDurations;

    @Data
    public static class AvatarDurationItem implements Serializable {
        @ApiModelProperty("智能体名称")
        private String avatarName;

        @ApiModelProperty("平均对话时长（分钟）")
        private Double avgDuration;

        @ApiModelProperty("对话次数")
        private Long conversationCount;

        private static final long serialVersionUID = 1L;
    }

    private static final long serialVersionUID = 1L;
}
