package com.ttbt.smartclass.model.dto;

import lombok.Data;

/**
 * 提交答案响应 DTO
 */
@Data
public class SubmitAnswerResponse {

    /**
     * 是否正确
     */
    private boolean correct;

    /**
     * 正确答案
     */
    private String correctAnswer;

    /**
     * 答案解析
     */
    private String explanation;

    /**
     * 得分
     */
    private int score;
}
