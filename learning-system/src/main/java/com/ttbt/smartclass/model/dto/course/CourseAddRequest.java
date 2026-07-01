package com.ttbt.smartclass.model.dto.course;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 课程新增请求。
 */
@Data
public class CourseAddRequest {

    @NotBlank(message = "课程标题不能为空")
    private String title;

    private String subtitle;

    private String description;

    private String coverUrl;

    private String coverImage;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer courseType;

    private Integer difficulty;

    private Integer status;

    private String category;

    private Long categoryId;

    @NotNull(message = "教师 ID 不能为空")
    private Long teacherId;

    private Integer totalDuration;

    private Integer totalChapters;

    private Integer totalSections;

    private String tags;

    private String requirements;

    private String objectives;

    private String targetAudience;

    @NotBlank(message = "AI 知识不能为空")
    private String aiKnowleage;
}
