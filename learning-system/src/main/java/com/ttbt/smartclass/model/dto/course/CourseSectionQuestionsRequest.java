package com.ttbt.smartclass.model.dto.course;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 小节题目查询请求
 */
@Data
public class CourseSectionQuestionsRequest implements Serializable {

    @NotNull(message = "sectionId 不能为空")
    private Long sectionId;

    private static final long serialVersionUID = 1L;
}
