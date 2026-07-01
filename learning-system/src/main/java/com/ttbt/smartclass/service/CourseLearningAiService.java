package com.ttbt.smartclass.service;

import com.ttbt.smartclass.model.dto.course.CourseLearningAiAskRequest;
import com.ttbt.smartclass.model.dto.course.CourseLearningAiContext;
import com.ttbt.smartclass.model.entity.AiAvatar;
import com.ttbt.smartclass.model.vo.ChatMessageVO;
import java.util.Map;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 课程学习助手服务
 */
public interface CourseLearningAiService {

    ChatMessageVO askCourseLearningAssistant(CourseLearningAiAskRequest request, Long userId);

    SseEmitter askCourseLearningAssistantStream(CourseLearningAiAskRequest request, Long userId);

    ChatMessageVO askCourseExclusiveAssistant(CourseLearningAiAskRequest request, Long userId);

    SseEmitter askCourseExclusiveAssistantStream(CourseLearningAiAskRequest request, Long userId);

    AiAvatar resolveAssistant(CourseLearningAiContext context, Long preferredAiAvatarId);

    Map<String, Object> buildDifyInputs(CourseLearningAiContext context, String question);
}
