package com.ttbt.smartclass.model.dto.course;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 课程统一提交（客观题/主观题）请求
 */
@Data
public class CourseSubmitRequest implements Serializable {

    @NotNull(message = "sectionId 不能为空")
    private Long sectionId;

    @NotNull(message = "questionId 不能为空")
    private Long questionId;

    @NotBlank(message = "answer 不能为空")
    private String answer;

    /**
     * 答题耗时（秒）
     */
    private Integer timeSpent;

    private static final long serialVersionUID = 1L;
}
