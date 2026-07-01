package com.ttbt.smartclass.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 作业提交批阅详情视图。
 */
@Data
public class HomeworkSubmissionReviewDetailVO {

    private Long id;

    private Long submissionId;

    private String answerContent;

    private String answerAttachmentUrl;

    private String reviewComment;

    /**
     * Frontend compatibility alias.
     */
    private String comment;

    private Long reviewerId;

    private String reviewerName;

    private Date reviewTime;
}
