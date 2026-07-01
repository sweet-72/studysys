package com.ttbt.smartclass.model.dto.course;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 课程详情查询请求
 */
@Data
public class CourseDetailRequest implements Serializable {

    @NotNull(message = "course_id 不能为空")
    private Long courseId;

    private static final long serialVersionUID = 1L;
}
