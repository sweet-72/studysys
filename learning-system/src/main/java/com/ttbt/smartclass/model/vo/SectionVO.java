package com.ttbt.smartclass.model.vo;

import lombok.Data;

/**
 * 小节信息 VO（用户端）
 */
@Data
public class SectionVO {

    /**
     * 小节 id
     */
    private Long id;

    /**
     * 小节标题
     */
    private String title;

    /**
     * 小节类型：video-视频，article-文章，exercise-练习
     */
    private String type;

    /**
     * 资源类型：URL-外链，FILE-本地文件
     */
    private String resourceType;

    /**
     * 处理后的可播放地址
     */
    private String playUrl;

    /**
     * 内容类型：VIDEO-视频，ARTICLE-文章
     */
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
