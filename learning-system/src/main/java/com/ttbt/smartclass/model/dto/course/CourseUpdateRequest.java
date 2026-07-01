package com.ttbt.smartclass.model.dto.course;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 更新课程请求
 */
@Data
public class CourseUpdateRequest implements Serializable {
    /**
     * id
     */
    @NotNull(message = "课程ID不能为空")
    private Long id;

    /**
     * 课程标题
     */
    private String title;

    /**
     * 课程副标题
     */
    private String subtitle;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 课程价格
     */
    private BigDecimal price;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 课程类型：0-公开课，1-付费课，2-会员课
     */
    private Integer courseType;

    /**
     * 难度等级：1-入门，2-初级，3-中级，4-高级，5-专家
     */
    private Integer difficulty;

    /**
     * 状态：0-未发布，1-已发布，2-已下架
     */
    private Integer status;

    /**
     * 课程分类id
     */
    private Long categoryId;

    /**
     * 课程分类名称（当categoryId为空时可使用）
     */
    private String categoryName;

    /**
     * 讲师id
     */
    private Long teacherId;

    /**
     * 总时长(分钟)
     */
    private Integer totalDuration;

    /**
     * 总章节数
     */
    private Integer totalChapters;

    /**
     * 总小节数
     */
    private Integer totalSections;

    /**
     * 评分：1-5分
     */
    private BigDecimal ratingScore;

    /**
     * 评分人数
     */
    private Integer ratingCount;

    /**
     * 标签
     */
    private String tags;

    /**
     * 学习要求
     */
    private String requirements;

    /**
     * 学习目标
     */
    private String objectives;

    /**
     * 目标受众
     */
    private String targetAudience;

    /**
     * 课程专属 AI 知识
     */
    @NotBlank(message = "AI 知识不能为空")
    private String aiKnowleage;

    /**
     * 章节与小节结构；传入时会整体替换课程结构
     */
    @Valid
    @Size(min = 1, message = "章节不能为空")
    private List<CourseChapterSectionRequest.ChapterItem> chapters;

    private static final long serialVersionUID = 1L;
}
