package com.ttbt.smartclass.model.dto.section;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建小节请求 DTO
 */
@Data
public class SectionAddRequest {

    /**
     * 章节 ID
     */
    @NotNull(message = "章节 ID 不能为空")
    private Long chapterId;

    /**
     * 小节标题
     */
    @NotBlank(message = "小节标题不能为空")
    private String title;

    /**
     * 小节类型：video-视频，article-文章，exercise-练习
     */
    @NotBlank(message = "小节类型不能为空")
    private String type;

    /**
     * 资源类型：URL-外链，FILE-本地文件
     */
    @NotBlank(message = "资源类型不能为空")
    private String resourceType;

    /**
     * 资源地址（URL 或文件路径）
     */
    private String resourceUrl;

    /**
     * 内容类型：VIDEO-视频，ARTICLE-文章
     */
    @NotBlank(message = "内容类型不能为空")
    private String contentType;

    /**
     * 图文内容（HTML）
     */
    private String content;

    /**
     * 视频时长（秒）
     */
    private Integer videoDuration;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 是否免费试看：0-否，1-是
     */
    private Integer isFree;
}
