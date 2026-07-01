package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 每日文章
 * @TableName daily_article
 */
@TableName(value ="daily_article")
@Data
public class DailyArticle implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 封面图片 URL
     */
    @TableField("cover_image")
    private String coverImage;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 来源
     */
    private String source;
    
    /**
     * 原文链接
     */
    @TableField("source_url")
    private String sourceUrl;
    
    /**
     * 文章分类
     */
    private String category;
    
    /**
     * 标签，JSON 数组格式
     */
    private String tags;
    
    /**
     * 难度等级：1-简单，2-中等，3-困难
     */
    private Integer difficulty;
    
    /**
     * 预计阅读时间 (分钟)
     */
    private Integer readTime;
    
    /**
     * 发布日期
     */
    @TableField("publish_date")
    private Date publishDate;
    
    /**
     * 创建管理员 id
     */
    @TableField("admin_id")
    private Long adminId;
    
    /**
     * 查看次数
     */
    @TableField("view_count")
    private Integer viewCount;
    
    /**
     * 点赞次数
     */
    @TableField("like_count")
    private Integer likeCount;
    
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
    @TableField("is_delete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
