package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户作答实体类
 */
@TableName(value = "user_answer")
@Data
public class UserAnswer implements Serializable {

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
     * 题目 id
     */
    @TableField("question_id")
    private Long questionId;

    /**
     * 用户答案
     */
    @TableField("user_answer")
    private String userAnswer;

    /**
     * 是否正确：0-否，1-是
     */
    @TableField("is_correct")
    private Integer isCorrect;

    /**
     * 得分
     */
    private Integer score;

    /**
     * 答题耗时（秒）
     */
    @TableField("time_spent")
    private Integer timeSpent;

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
