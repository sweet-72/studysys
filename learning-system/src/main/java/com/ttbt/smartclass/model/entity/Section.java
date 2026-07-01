package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 小节实体类
 */
@TableName(value = "section")
@Data
public class Section implements Serializable {

    /**
     * 小节 id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 章节 id
     */
    @TableField("chapter_id")
    private Long chapterId;

    /**
     * 小节标题
     */
    private String title;

    /**
     * 小节类型：video-视频，article-文章，exercise-练习
     */
    private String type;

    /**
     * 图文内容（HTML）
     */
    private String content;

    /**
     * 小节级 AI 学习助手知识内容。
     */
    @TableField("ai_knowleage")
    private String aiKnowleage;

    /**
     * 视频 URL
     */
    @TableField("video_url")
    private String videoUrl;

    /**
     * 视频时长（秒）
     */
    @TableField("video_duration")
    private Integer videoDuration;

    /**
     * 资源类型：URL-外链，FILE-本地文件
     */
    @TableField("resource_type")
    private String resourceType;

    /**
     * 资源地址（URL 或文件路径）
     */
    @TableField("resource_url")
    private String resourceUrl;

    /**
     * 内容类型：VIDEO-视频，ARTICLE-文章
     */
    @TableField("content_type")
    private String contentType;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 是否免费试看
     */
    @TableField("is_free")
    private Integer isFree;

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
