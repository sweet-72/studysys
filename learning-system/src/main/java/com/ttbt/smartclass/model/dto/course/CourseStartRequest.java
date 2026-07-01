package com.ttbt.smartclass.model.dto.course;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 开始学习课程
 */
@Data
public class CourseStartRequest {

    @NotNull(message = "课程 ID 不能为空")
    private Long courseId;
}
