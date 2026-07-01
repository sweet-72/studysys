package com.ttbt.smartclass.model.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 个人中心跨课程学习记录视图。
 */
@Data
public class PersonalLearningRecordVO implements Serializable {

    private Long courseId;

    private String courseTitle;

    private Long sectionId;

    private String sectionTitle;

    private BigDecimal progressPercent;

    private Date lastLearnTime;

    private Integer completedSections;

    private Integer totalSections;

    private static final long serialVersionUID = 1L;
}
