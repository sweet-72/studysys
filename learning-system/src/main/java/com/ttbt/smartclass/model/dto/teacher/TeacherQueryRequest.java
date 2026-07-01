package com.ttbt.smartclass.model.dto.teacher;

import com.ttbt.smartclass.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询讲师请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeacherQueryRequest extends PageRequest implements Serializable {

    private Long id;

    private String name;

    private String title;

    private String expertise;

    private Long userId;

    private Long adminId;

    private static final long serialVersionUID = 1L;
}