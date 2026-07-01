package com.ttbt.smartclass.model.dto;

import lombok.Data;

/**
 * 提交答案请求 DTO
 */
@Data
public class SubmitAnswerRequest {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 题目 ID
     */
    private Long questionId;

    /**
     * 用户答案
     */
    private String answer;

    /**
     * 答题耗时（秒）
     */
    private Integer timeSpent;
}
