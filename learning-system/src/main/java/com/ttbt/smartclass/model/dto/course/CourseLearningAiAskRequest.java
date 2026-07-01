package com.ttbt.smartclass.model.dto.course;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 课程学习助手提问请求
 */
@Data
public class CourseLearningAiAskRequest implements Serializable {

    /**
     * AI 助手 ID，可选，未传时根据课程分类自动匹配
     */
    private Long aiAvatarId;

    /**
     * 会话 ID，可为空，空时自动创建
     */
    private String sessionId;

    /**
     * 课程 ID
     */
    @NotNull(message = "course_id 不能为空")
    private Long courseId;

    /**
     * 章节 ID，可选
     */
    private Long chapterId;

    /**
     * 小节 ID，可选
     */
    private Long sectionId;

    /**
     * 学生问题
     */
    @NotBlank(message = "question 不能为空")
    private String question;

    /**
     * 学生本轮目标，可选
     */
    private String studentGoal;

    /**
     * 是否结束会话
     */
    private boolean endChat;

    private static final long serialVersionUID = 1L;
}