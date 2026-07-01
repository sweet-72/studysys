package com.ttbt.smartclass.model.dto.course;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ttbt.smartclass.common.PageRequest;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 课程知识库同步记录查询请求。
 */
@Data
public class CourseKnowledgeSyncQueryRequest extends PageRequest implements Serializable {

    private Long courseId;

    private String sourceType;

    private String syncStatus;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    private static final long serialVersionUID = 1L;
}
