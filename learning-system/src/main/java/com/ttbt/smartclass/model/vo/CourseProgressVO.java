package com.ttbt.smartclass.model.vo;

import lombok.Data;

/**
 * 课程学习进度 VO
 */
@Data
public class CourseProgressVO {

    /**
     * 课程 ID
     */
    private Long courseId;

    /**
     * 已完成小节数
     */
    private Integer completedSections;

    /**
     * 总小节数
     */
    private Integer totalSections;

    /**
     * 进度百分比
     */
    private Integer progress;
}
