package com.ttbt.smartclass.model.dto.course;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 后台创建课程请求
 */
@Data
public class CourseManageCreateRequest implements Serializable {

    @NotBlank(message = "课程标题不能为空")
    private String title;

    private String subtitle;

    @NotBlank(message = "课程简介不能为空")
    private String description;

    private Long categoryId;

    private String categoryName;

    private Integer courseType;

    private Integer difficulty;
    private Long teacherId;

    private Integer status;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private String tags;

    private String requirements;

    private String objectives;

    private String targetAudience;

    @NotBlank(message = "AI 知识不能为空")
    private String aiKnowleage;

    @Valid
    private List<CourseChapterSectionRequest.ChapterItem> chapters;

    private static final long serialVersionUID = 1L;
}
