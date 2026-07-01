package com.ttbt.smartclass.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class LearningHistoryItemVO implements Serializable {

    private String id;

    private String actionType;

    private String title;

    private String description;

    private Date actionTime;

    private Long courseId;

    private Long sectionId;

    private Long targetId;

    private Integer progressPercent;

    private static final long serialVersionUID = 1L;
}
