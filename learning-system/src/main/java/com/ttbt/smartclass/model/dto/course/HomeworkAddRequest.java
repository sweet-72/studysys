package com.ttbt.smartclass.model.dto.course;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.io.Serializable;

/**
 * 管理端新增小节作业请求
 */
@Data
public class HomeworkAddRequest implements Serializable {

    @NotNull(message = "sectionId 不能为空")
    @Positive(message = "sectionId 非法")
    private Long sectionId;

    @NotBlank(message = "作业内容不能为空")
    private String content;

    /**
     * 题目类型：single / multiple / judge / subjective
     */
    private String type;

    /**
     * 客观题选项（JSON 字符串）
     */
    private String options;

    /**
     * 客观题答案（主观题可为空）
     */
    private String answer;

    private String explanation;

    @PositiveOrZero(message = "分值不能小于 0")
    private Integer score;

    @PositiveOrZero(message = "难度不能小于 0")
    private Integer difficulty;

    @PositiveOrZero(message = "排序不能小于 0")
    private Integer sortOrder;

    private static final long serialVersionUID = 1L;
}
