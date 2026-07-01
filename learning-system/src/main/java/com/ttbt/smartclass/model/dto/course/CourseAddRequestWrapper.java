package com.ttbt.smartclass.model.dto.course;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 添加课程请求
 */
@Data
public class CourseAddRequestWrapper implements Serializable {


    /**
     * 课程添加请求数据
     */
    private CourseAddRequest data;


}