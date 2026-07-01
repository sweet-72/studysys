package com.ttbt.smartclass.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 课程详情（含章节、小节）
 */
@Data
public class CourseDetailVO implements Serializable {

    private Long id;

    private String title;

    private String subtitle;

    private String description;

    private String coverUrl;

    private Long teacherId;

    private String teacherName;

    private String teacherAvatar;

    private Integer courseType;

    private Integer difficulty;

    private Integer totalSections;

    private Integer totalQuestions;

    private Integer status;

    private BigDecimal ratingScore;

    private Integer ratingCount;

    /**
     * 后台课程管理查看用，前台普通接口不返回。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String aiKnowleage;

    private List<CourseCatalogVO.ChapterInfo> chapters;

    private static final long serialVersionUID = 1L;
}
