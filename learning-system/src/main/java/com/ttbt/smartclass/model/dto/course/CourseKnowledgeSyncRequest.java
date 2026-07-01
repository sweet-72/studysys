package com.ttbt.smartclass.model.dto.course;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 课程知识库同步触发请求。
 */
@Data
public class CourseKnowledgeSyncRequest implements Serializable {

    @NotNull(message = "course_id 不能为空")
    private Long courseId;

    /**
     * Optional source types: course_intro/chapter/section/material/question.
     * Empty means syncing the whole course knowledge scope.
     */
    private List<String> syncTypes;

    private static final long serialVersionUID = 1L;
}
