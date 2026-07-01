package com.ttbt.smartclass.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 个人中心收藏课程详情视图。
 */
@Data
public class PersonalFavouriteCourseVO implements Serializable {

    private Long courseId;

    private String courseTitle;

    private String courseCover;

    private String courseDescription;

    private String teacherName;

    private Integer totalSections;

    private Integer difficulty;

    private Long categoryId;

    private Date favourTime;

    private static final long serialVersionUID = 1L;
}
