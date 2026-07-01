package com.ttbt.smartclass.model.dto.course;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 课程页 AI 提问请求
 */
@Data
public class CourseAiAskRequest implements Serializable {

    /**
     * AI 分身 ID
     */
    @NotNull(message = "aiAvatarId 不能为空")
    private Long aiAvatarId;

    /**
     * 会话 ID（为空时自动创建）
     */
    private String sessionId;

    /**
     * 课程 ID（可选，传入后会附带课程上下文）
     */
    private Long courseId;

    /**
     * 用户问题
     */
    @NotBlank(message = "question 不能为空")
    private String question;

    /**
     * 是否结束会话
     */
    private boolean endChat;

    private static final long serialVersionUID = 1L;
}
