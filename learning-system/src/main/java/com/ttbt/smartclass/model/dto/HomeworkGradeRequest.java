package com.ttbt.smartclass.model.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 批改作业请求 DTO
 */
@Data
public class HomeworkGradeRequest {

    /**
     * 提交 ID
     */
    @NotNull(message = "提交 ID 不能为空")
    private Long submissionId;

    /**
     * 得分
     */
    @NotNull(message = "得分不能为空")
    @Min(value = 0, message = "得分不能小于 0")
    private Integer score;

    /**
     * 教师评语
     */
    private String feedback;

    /**
     * 状态：1-已批改，2-需复查
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 1, message = "状态必须大于等于 1")
    @Max(value = 2, message = "状态必须小于等于 2")
    private Integer status;
}
