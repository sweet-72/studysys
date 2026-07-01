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
 * 学科助手与 Dify 知识库绑定
 * @TableName subject_ai_binding
 */
@TableName(value = "subject_ai_binding")
@Data
public class SubjectAiBinding implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程分类 / 学科 ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 本地 AI 助手 ID
     */
    @TableField("ai_avatar_id")
    private Long aiAvatarId;

    /**
     * Dify 知识库 ID
     */
    @TableField("dify_dataset_id")
    private String difyDatasetId;

    /**
     * Dify 工作流 App Key
     */
    @TableField("dify_workflow_app_key")
    private String difyWorkflowAppKey;

    /**
     * Dify 工作流基础地址
     */
    @TableField("dify_workflow_base_url")
    private String difyWorkflowBaseUrl;

    /**
     * 状态：1-启用，0-停用
     */
    private Integer status;

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
