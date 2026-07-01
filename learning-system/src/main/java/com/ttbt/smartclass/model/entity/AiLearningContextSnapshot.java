package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * AI 学习上下文快照
 * @TableName ai_learning_context_snapshot
 */
@TableName(value = "ai_learning_context_snapshot")
@Data
public class AiLearningContextSnapshot implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 课程 ID
     */
    @TableField("course_id")
    private Long courseId;

    /**
     * 章节 ID
     */
    @TableField("chapter_id")
    private Long chapterId;

    /**
     * 小节 ID
     */
    @TableField("section_id")
    private Long sectionId;

    /**
     * 学习进度 JSON
     */
    @TableField("latest_progress_json")
    private String latestProgressJson;

    /**
     * 作业情况 JSON
     */
    @TableField("latest_homework_json")
    private String latestHomeworkJson;

    /**
     * 错题摘要 JSON
     */
    @TableField("latest_wrong_questions_json")
    private String latestWrongQuestionsJson;

    /**
     * 供大模型直接消费的摘要文本
     */
    @TableField("summary_text")
    private String summaryText;

    /**
     * 快照时间
     */
    @TableField("snapshot_time")
    private Date snapshotTime;

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
     * 逻辑删除
     */
    @TableLogic
    @TableField("is_delete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
