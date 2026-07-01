package com.ttbt.smartclass.model.dto.course;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 小节开始学习请求
 */
@Data
public class CourseLearnStartRequest implements Serializable {

    @NotNull(message = "course_id 不能为空")
    private Long courseId;

    @NotNull(message = "sectionId 不能为空")
    private Long sectionId;

    private static final long serialVersionUID = 1L;
}
