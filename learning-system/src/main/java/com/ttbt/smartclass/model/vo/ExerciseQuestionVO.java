package com.ttbt.smartclass.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 习题展示（不含标准答案与解析，避免前端泄题）
 */
@Data
public class ExerciseQuestionVO implements Serializable {

    private Long id;

    private Long sectionId;

    private String type;

    private String content;

    private String options;

    private Integer score;

    private Integer difficulty;

    private Integer sortOrder;

    private static final long serialVersionUID = 1L;
}
