package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目实体类
 */
@TableName(value = "question")
@Data
public class Question implements Serializable {

    /**
     * 题目 id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 小节 id
     */
    @TableField("section_id")
    private Long sectionId;

    /**
     * 题目类型：single-单选，multiple-多选，judge-判断
     */
    private String type;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 选项（JSON 数组）
     */
    private String options;

    /**
     * 正确答案
     */
    private String answer;

    /**
     * 答案解析
     */
    private String explanation;

    /**
     * 题目分值
     */
    private Integer score;

    /**
     * 难度：1-简单，2-中等，3-困难
     */
    private Integer difficulty;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

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
