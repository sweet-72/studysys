package com.ttbt.smartclass.model.vo;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * 管理端课程知识库同步状态视图。
 */
@Data
public class CourseKnowledgeSyncAdminStatusVO implements Serializable {

    private Long courseId;

    private Integer totalCount;

    private Integer completedCount;

    private Integer indexingCount;

    private Integer failedCount;

    private List<CourseKnowledgeSyncItemVO> records;

    private static final long serialVersionUID = 1L;
}
