package com.ttbt.smartclass.model.vo;

import com.ttbt.smartclass.model.entity.CourseKnowledgeSync;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * 课程知识同步状态视图
 */
@Data
public class CourseKnowledgeSyncStatusVO implements Serializable {

    private Long courseId;

    private Integer totalCount;

    private Integer completedCount;

    private Integer indexingCount;

    private Integer failedCount;

    private List<CourseKnowledgeSync> records;

    private static final long serialVersionUID = 1L;
}