package com.ttbt.smartclass.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 管理端课程知识库同步记录视图。
 */
@Data
public class CourseKnowledgeSyncItemVO implements Serializable {

    private Long id;

    private Long courseId;

    private String sourceType;

    private Long sourceId;

    private String sourceName;

    private String syncMode;

    private String syncStatus;

    private Integer retryCount;

    private String errorMessage;

    private String documentId;

    private String batchId;

    private Date lastSyncTime;

    private Date nextRetryTime;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
