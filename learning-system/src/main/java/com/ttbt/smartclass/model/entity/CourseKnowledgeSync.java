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
 * 课程知识同步记录
 * @TableName course_knowledge_sync
 */
@TableName(value = "course_knowledge_sync")
@Data
public class CourseKnowledgeSync implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程 ID
     */
    @TableField("course_id")
    private Long courseId;

    /**
     * 分类 / 学科 ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 来源类型：course_intro/chapter/section/material/question/explanation
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 来源记录 ID
     */
    @TableField("source_id")
    private Long sourceId;

    /**
     * 来源名称
     */
    @TableField("source_name")
    private String sourceName;

    /**
     * Dify 知识库 ID
     */
    @TableField("dify_dataset_id")
    private String difyDatasetId;

    /**
     * Dify 文档 ID
     */
    @TableField("dify_document_id")
    private String difyDocumentId;

    /**
     * Dify 批次 ID
     */
    @TableField("dify_batch_id")
    private String difyBatchId;

    /**
     * 同步模式：text/file
     */
    @TableField("sync_mode")
    private String syncMode;

    /**
     * 同步状态：pending/uploading/indexing/completed/failed
     */
    @TableField("sync_status")
    private String syncStatus;

    /**
     * 内容哈希，用于幂等和增量更新
     */
    @TableField("content_hash")
    private String contentHash;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 最近一次同步时间
     */
    @TableField("last_sync_time")
    private Date lastSyncTime;

    /**
     * 下次重试时间
     */
    @TableField("next_retry_time")
    private Date nextRetryTime;

    /**
     * 重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

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
