package com.ttbt.smartclass.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一提交结果（客观题自动判分 / 主观题待教师批改）
 */
@Data
public class CourseSubmitResultVO implements Serializable {

    /**
     * objective / subjective
     */
    private String submitType;

    /**
     * 是否自动判分
     */
    private Boolean autoGraded;

    /**
     * 自动判分是否正确（主观题为空）
     */
    private Boolean correct;

    /**
     * 自动判分得分（主观题为空）
     */
    private Integer score;

    /**
     * 正确答案（客观题）
     */
    private String correctAnswer;

    /**
     * 解析（客观题）
     */
    private String explanation;

    /**
     * 主观题提交记录 ID
     */
    private Long submissionId;

    /**
     * 主观题批改状态：0-待批改，1-已批改，2-需复查
     */
    private Integer reviewStatus;

    /**
     * 提示信息
     */
    private String message;

    private static final long serialVersionUID = 1L;
}
