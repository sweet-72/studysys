package com.ttbt.smartclass.model.dto.course;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.AssertTrue;
import java.io.Serializable;
import java.util.List;

/**
 * 课程章节与小节请求结构
 */
@Data
public class CourseChapterSectionRequest implements Serializable {

    @Data
    public static class ChapterItem implements Serializable {

        private Long id;

        @NotBlank(message = "章节标题不能为空")
        private String title;

        private String description;

        @PositiveOrZero(message = "章节排序不能小于0")
        private Integer sortOrder;

        @Valid
        @NotEmpty(message = "小节不能为空")
        private List<SectionItem> sections;

        private static final long serialVersionUID = 1L;
    }

    @Data
    public static class SectionItem implements Serializable {

        private Long id;

        @NotBlank(message = "小节标题不能为空")
        private String title;

        /**
         * 小节级 AI 学习助手知识内容。
         */
        private String aiKnowleage;

        /**
         * 视频URL
         */
        private String videoUrl;

        /**
         * 本地上传后的视频路径
         */
        private String localVideoPath;

        @PositiveOrZero(message = "视频时长不能小于0")
        private Integer videoDuration;

        @PositiveOrZero(message = "小节排序不能小于0")
        private Integer sortOrder;

        @PositiveOrZero(message = "是否免费参数非法")
        private Integer isFree;

        /**
         * 校验小节至少提供外部视频地址或本地上传视频路径。
         *
         * @return 是否存在可用视频来源
         */
        @AssertTrue(message = "视频URL和本地上传文件路径至少填写一个")
        public boolean hasVideoSource() {
            return StringUtils.isNotBlank(videoUrl) || StringUtils.isNotBlank(localVideoPath);
        }

        private static final long serialVersionUID = 1L;
    }

    private static final long serialVersionUID = 1L;
}
