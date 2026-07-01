package com.ttbt.smartclass.model.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 作业提交批阅请求。
 */
@Data
public class HomeworkSubmissionReviewRequest {

    @NotNull(message = "submissionId must not be null")
    private Long submissionId;

    @NotNull(message = "score must not be null")
    @Min(value = 0, message = "score must be >= 0")
    @Max(value = 100, message = "score must be <= 100")
    private Integer score;

    private String comment;
}
