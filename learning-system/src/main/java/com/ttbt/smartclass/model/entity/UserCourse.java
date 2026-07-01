package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户课程关系实体类
 */
@TableName(value = "user_course")
@Data
public class UserCourse implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 课程 id
     */
    @TableField("course_id")
    private Long courseId;

    /**
     * 状态：0-学习中，1-已完成，2-已放弃
     */
    private Integer status;

    /**
     * 课程总进度百分比
     */
    private BigDecimal progress;

    /**
     * 开始学习时间
     */
    @TableField("start_time")
    private Date startTime;

    /**
     * 完成时间
     */
    @TableField("complete_time")
    private Date completeTime;

    /**
     * 最后学习时间
     */
    @TableField("last_learn_time")
    private Date lastLearnTime;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
