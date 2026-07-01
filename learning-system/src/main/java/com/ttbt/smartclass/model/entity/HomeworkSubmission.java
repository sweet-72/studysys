package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 作业提交实体类
 */
@TableName(value = "homework_submission")
@Data
public class HomeworkSubmission implements Serializable {

    /**
     * 提交 id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 小节 id
     */
    @TableField("section_id")
    private Long sectionId;

    /**
     * 题目 id
     */
    @TableField("question_id")
    private Long questionId;

    /**
     * 用户答案
     */
    private String answer;

    /**
     * 状态：0-未批改，1-已批改，2-需复查
     */
    private Integer status;

    /**
     * 得分
     */
    private Integer score;

    /**
     * 教师评语
     */
    private String feedback;

    /**
     * 批改教师 id
     */
    @TableField("graded_by")
    private Long gradedBy;

    /**
     * 批改时间
     */
    @TableField("graded_time")
    private Date gradedTime;

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
