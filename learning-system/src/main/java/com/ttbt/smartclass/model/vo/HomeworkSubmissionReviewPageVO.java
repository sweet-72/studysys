package com.ttbt.smartclass.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 作业提交批阅列表项视图。
 */
@Data
public class HomeworkSubmissionReviewPageVO {

    private Long id;

    private Long homeworkId;

    private String homeworkTitle;

    private Long courseId;

    private Long sectionId;

    private Long studentId;

    private String studentName;

    private Date submitTime;

    private Integer reviewStatus;

    private Integer reviewScore;

    /**
     * Frontend compatibility alias.
     */
    private Integer score;
}
