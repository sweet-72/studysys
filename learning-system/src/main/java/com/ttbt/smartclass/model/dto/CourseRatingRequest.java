package com.ttbt.smartclass.model.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 课程评分请求 DTO
 */
@Data
public class CourseRatingRequest {

    /**
     * 课程 ID
     */
    @NotNull(message = "课程 ID 不能为空")
    private Long courseId;

    /**
     * 评分：1-5 分
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为 1")
    @Max(value = 5, message = "评分最大为 5")
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 是否匿名：0-否，1-是
     */
    private Integer anonymous = 0;
}
