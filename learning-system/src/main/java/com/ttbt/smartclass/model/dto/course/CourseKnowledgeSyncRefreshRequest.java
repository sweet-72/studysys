package com.ttbt.smartclass.model.dto.course;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 课程知识库索引状态刷新请求。
 */
@Data
public class CourseKnowledgeSyncRefreshRequest implements Serializable {

    @NotEmpty(message = "syncIds 不能为空")
    private List<Long> syncIds;

    private static final long serialVersionUID = 1L;
}
