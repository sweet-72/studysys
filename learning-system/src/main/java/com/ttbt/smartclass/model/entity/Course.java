package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 课程实体。
 */
@TableName(value = "course")
@Data
public class Course implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String subtitle;

    private String description;

    @TableField("teacher_id")
    private Long teacherId;

    @TableField("cover_url")
    private String coverUrl;

    /**
     * Legacy category string.
     */
    private String category;

    @TableField("category_id")
    private Long categoryId;

    @TableField("course_type")
    private Integer courseType;

    private Integer difficulty;

    private BigDecimal price;

    @TableField("original_price")
    private BigDecimal originalPrice;

    @TableField("total_sections")
    private Integer totalSections;

    @TableField("total_chapters")
    private Integer totalChapters;

    @TableField("total_duration")
    private Integer totalDuration;

    @TableField("total_questions")
    private Integer totalQuestions;

    private Integer status;

    @TableField("rating_score")
    private BigDecimal ratingScore;

    @TableField("rating_count")
    private Integer ratingCount;

    @TableField("student_count")
    private Integer studentCount;

    @TableField("buy_count")
    private Integer buyCount;

    @TableField("view_count")
    private Integer viewCount;

    private String tags;

    private String requirements;

    private String objectives;

    @TableField("target_audience")
    private String targetAudience;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("ai_knowleage")
    private String aiKnowleage;

    @TableLogic
    @TableField("is_delete")
    private Integer isDelete;

    /**
     * VO-only field for compatibility with frontend naming.
     */
    @TableField(exist = false)
    private String coverImage;

    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private String teacherAvatar;

    @TableField(exist = false)
    private Integer studyCount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
