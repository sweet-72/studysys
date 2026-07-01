package com.ttbt.smartclass.model.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 学生端我的课程列表项视图。
 */
@Data
public class MyCourseVO implements Serializable {

    private Long courseId;

    private String courseTitle;

    private String courseCover;

    private BigDecimal progressPercent;

    private Integer completedSections;

    private Integer totalSections;

    private Date lastLearnTime;

    private String learningStatus;

    private static final long serialVersionUID = 1L;
}
