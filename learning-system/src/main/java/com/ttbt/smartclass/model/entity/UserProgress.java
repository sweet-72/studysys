package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户学习进度实体类
 */
@TableName(value = "user_progress")
@Data
public class UserProgress implements Serializable {

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
     * 小节 id
     */
    @TableField("section_id")
    private Long sectionId;

    /**
     * 状态：0-未学习，1-学习中，2-已完成
     */
    private Integer status;

    /**
     * 学习时长（秒）
     */
    @TableField("learned_time")
    private Integer learnedTime;

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
