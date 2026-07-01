package com.ttbt.smartclass.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 小节学习状态返回
 */
@Data
public class CourseLearnStatusVO implements Serializable {

    private Long courseId;

    private Long sectionId;

    /**
     * UNLEARNED / LEARNING / COMPLETED
     */
    private String status;

    private static final long serialVersionUID = 1L;
}
