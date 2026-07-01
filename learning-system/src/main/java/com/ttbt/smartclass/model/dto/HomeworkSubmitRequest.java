package com.ttbt.smartclass.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 提交作业请求 DTO
 */
@Data
public class HomeworkSubmitRequest {

    /**
     * 小节 ID
     */
    @NotNull(message = "小节 ID 不能为空")
    private Long sectionId;

    /**
     * 题目 ID
     */
    @NotNull(message = "题目 ID 不能为空")
    private Long questionId;

    /**
     * 用户答案
     */
    @NotBlank(message = "答案不能为空")
    private String answer;
}
