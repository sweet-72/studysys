package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 章节实体类
 */
@TableName(value = "chapter")
@Data
public class Chapter implements Serializable {

    /**
     * 章节 id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程 id
     */
    @TableField("course_id")
    private Long courseId;

    /**
     * 章节标题
     */
    private String title;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 章节描述
     */
    private String description;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField("is_delete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
