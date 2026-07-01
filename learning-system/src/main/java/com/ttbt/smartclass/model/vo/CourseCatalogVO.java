package com.ttbt.smartclass.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 课程目录 VO（树形结构）
 */
@Data
public class CourseCatalogVO {

    /**
     * 课程 ID
     */
    private Long courseId;

    /**
     * 课程标题
     */
    private String courseTitle;

    /**
     * 总小节数
     */
    private Integer totalSections;

    /**
     * 章节列表
     */
    private List<ChapterInfo> chapters;

    /**
     * 章节信息
     */
    @Data
    public static class ChapterInfo {

        /**
         * 章节 ID
         */
        private Long chapterId;

        /**
         * 章节标题
         */
        private String chapterTitle;

        /**
         * 兼容字段：用户端前端使用 id/title
         */
        private Long id;
        private String title;

        /**
         * 小节列表
         */
        private List<SectionInfo> sections;
    }

    /**
     * 小节信息
     */
    @Data
    public static class SectionInfo {

        /**
         * 小节 ID
         */
        private Long sectionId;

        /**
         * 小节标题
         */
        private String sectionTitle;

        /**
         * 兼容字段：用户端前端使用 id/title
         */
        private Long id;
        private String title;

        /**
         * 小节类型
         */
        private String type;

        /**
         * 视频 URL
         */
        private String videoUrl;

        /**
         * 图文内容
         */
        private String content;

        /**
         * 是否免费
         */
        private Integer isFree;

        /**
         * 是否已学习
         */
        private Boolean isLearned;

        /**
         * 兼容字段：学习状态
         */
        private String learnStatus;
    }
}
