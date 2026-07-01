package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.SubjectAiBindingMapper;
import com.ttbt.smartclass.model.dto.course.CourseLearningAiAskRequest;
import com.ttbt.smartclass.model.dto.course.CourseLearningAiContext;
import com.ttbt.smartclass.model.entity.AiAvatar;
import com.ttbt.smartclass.model.entity.Course;
import com.ttbt.smartclass.model.entity.Section;
import com.ttbt.smartclass.model.entity.SubjectAiBinding;
import com.ttbt.smartclass.model.vo.ChatMessageVO;
import com.ttbt.smartclass.service.AiAvatarChatHistoryService;
import com.ttbt.smartclass.service.AiAvatarService;
import com.ttbt.smartclass.service.CourseLearningAiService;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.DifyService;
import com.ttbt.smartclass.service.LearningContextAssembler;
import com.ttbt.smartclass.service.SectionService;
import com.ttbt.smartclass.service.UserService;
import com.ttbt.smartclass.utils.SseEmitterUtils;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 课程学习 AI 服务实现。
 */
@Service
@Slf4j
public class CourseLearningAiServiceImpl implements CourseLearningAiService {

    private static final String SECTION_REQUIRED_MESSAGE = "请先选择具体小节后再使用学习助手";
    private static final String KNOWLEDGE_MISSING_MESSAGE = "当前小节和课程均未配置 AI 学习助手知识内容";
    private static final String KNOWLEDGE_SOURCE_SECTION = "section";
    private static final String KNOWLEDGE_SOURCE_COURSE = "course";

    @Resource
    private LearningContextAssembler learningContextAssembler;

    @Resource
    private SubjectAiBindingMapper subjectAiBindingMapper;

    @Resource
    private AiAvatarService aiAvatarService;

    @Resource
    private AiAvatarChatHistoryService aiAvatarChatHistoryService;

    @Resource
    private DifyService difyService;

    @Resource
    private CourseService courseService;

    @Resource
    private SectionService sectionService;

    @Resource
    private UserService userService;

    @Override
    public ChatMessageVO askCourseLearningAssistant(CourseLearningAiAskRequest request, Long userId) {
        return runWorkflow(request, userId);
    }

    @Override
    public SseEmitter askCourseLearningAssistantStream(CourseLearningAiAskRequest request, Long userId) {
        return runWorkflowStream(request, userId);
    }

    @Override
    public ChatMessageVO askCourseExclusiveAssistant(CourseLearningAiAskRequest request, Long userId) {
        validateRequest(request, userId);
        KnowledgeResolution knowledge = resolveKnowledge(request);
        if (!knowledge.canCallAi()) {
            return buildTipMessage(userId, request, knowledge.getTipMessage());
        }

        Course course = loadCourseOrThrow(request.getCourseId());
        AiAvatar assistant = resolveCourseExclusiveAssistant(request.getAiAvatarId());
        String prompt = buildCourseExclusivePrompt(course, knowledge, request.getQuestion());
        return difyService.handleSendMessageRequest(
                userId,
                assistant.getId(),
                request.getSessionId(),
                prompt,
                request.isEndChat(),
                aiAvatarChatHistoryService,
                aiAvatarService,
                userService
        );
    }

    @Override
    public SseEmitter askCourseExclusiveAssistantStream(CourseLearningAiAskRequest request, Long userId) {
        validateRequest(request, userId);
        KnowledgeResolution knowledge = resolveKnowledge(request);
        if (!knowledge.canCallAi()) {
            return SseEmitterUtils.createErrorEmitter(knowledge.getTipMessage());
        }

        Course course = loadCourseOrThrow(request.getCourseId());
        AiAvatar assistant = resolveCourseExclusiveAssistant(1000L);
        String prompt = buildCourseExclusivePrompt(course, knowledge, request.getQuestion());
        return difyService.handleStreamMessageRequest(
                userId,
                assistant.getId(),
                request.getSessionId(),
                prompt,
                aiAvatarChatHistoryService,
                aiAvatarService
        );
    }

    private ChatMessageVO runWorkflow(CourseLearningAiAskRequest request, Long userId) {
        validateRequest(request, userId);
        KnowledgeResolution knowledge = resolveKnowledge(request);
        if (!knowledge.canCallAi()) {
            return buildTipMessage(userId, request, knowledge.getTipMessage());
        }

        CourseLearningAiContext context = learningContextAssembler.assemble(userId, request);
        applyKnowledgeToContext(context, knowledge);
        SubjectAiBinding workflowBinding = resolveWorkflowBinding(context);
        AiAvatar assistant = resolveWorkflowAssistant(workflowBinding, context, request.getAiAvatarId());
        Map<String, Object> difyInputs = buildWorkflowInputs(context, request, assistant);

        return difyService.handleWorkflowRunRequest(
                userId,
                assistant.getId(),
                request.getSessionId(),
                request.getQuestion(),
                difyInputs,
                request.isEndChat(),
                workflowBinding.getDifyWorkflowBaseUrl(),
                workflowBinding.getDifyWorkflowAppKey(),
                aiAvatarChatHistoryService,
                aiAvatarService,
                userService
        );
    }

    private SseEmitter runWorkflowStream(CourseLearningAiAskRequest request, Long userId) {
        validateRequest(request, userId);
        KnowledgeResolution knowledge = resolveKnowledge(request);
        if (!knowledge.canCallAi()) {
            return SseEmitterUtils.createErrorEmitter(knowledge.getTipMessage());
        }

        CourseLearningAiContext context = learningContextAssembler.assemble(userId, request);
        applyKnowledgeToContext(context, knowledge);
        SubjectAiBinding workflowBinding = resolveWorkflowBinding(context);
        AiAvatar assistant = resolveWorkflowAssistant(workflowBinding, context, request.getAiAvatarId());
        Map<String, Object> difyInputs = buildWorkflowInputs(context, request, assistant);

        return difyService.handleWorkflowStreamRequest(
                userId,
                assistant.getId(),
                request.getSessionId(),
                request.getQuestion(),
                difyInputs,
                request.isEndChat(),
                workflowBinding.getDifyWorkflowBaseUrl(),
                workflowBinding.getDifyWorkflowAppKey(),
                aiAvatarChatHistoryService,
                aiAvatarService
        );
    }

    @Override
    public AiAvatar resolveAssistant(CourseLearningAiContext context, Long preferredAiAvatarId) {
        if (preferredAiAvatarId != null) {
            AiAvatar preferred = aiAvatarService.getById(preferredAiAvatarId);
            if (isAvailableAssistant(preferred)) {
                return preferred;
            }
        }

        if (context.getCategoryId() != null) {
            LambdaQueryWrapper<SubjectAiBinding> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SubjectAiBinding::getCategoryId, context.getCategoryId())
                    .eq(SubjectAiBinding::getStatus, 1)
                    .eq(SubjectAiBinding::getIsDelete, 0)
                    .last("limit 1");
            SubjectAiBinding binding = subjectAiBindingMapper.selectOne(wrapper);
            if (binding != null && binding.getAiAvatarId() != null) {
                AiAvatar avatar = aiAvatarService.getById(binding.getAiAvatarId());
                if (isAvailableAssistant(avatar)) {
                    return avatar;
                }
            }
        }

        LambdaQueryWrapper<AiAvatar> fallbackWrapper = new LambdaQueryWrapper<>();
        fallbackWrapper.eq(AiAvatar::getStatus, 1)
                .eq(AiAvatar::getIsDelete, 0)
                .orderByAsc(AiAvatar::getSort)
                .orderByDesc(AiAvatar::getUsageCount)
                .last("limit 1");
        AiAvatar fallback = aiAvatarService.getOne(fallbackWrapper);
        if (!isAvailableAssistant(fallback)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "no available learning assistant found");
        }
        return fallback;
    }

    @Override
    public Map<String, Object> buildDifyInputs(CourseLearningAiContext context, String question) {
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("question", defaultText(question));
        inputs.put("user_id", context.getUserId());
        inputs.put("course_id", context.getCourseId());
        inputs.put("course_title", defaultText(context.getCourseTitle()));
        inputs.put("category_id", context.getCategoryId());
        inputs.put("category_name", defaultText(context.getCategoryName()));
        inputs.put("chapter_id", context.getChapterId());
        inputs.put("chapter_title", defaultText(context.getChapterTitle()));
        inputs.put("section_id", context.getSectionId());
        inputs.put("section_title", defaultText(context.getSectionTitle()));
        inputs.put("ai_knowledge_source", defaultText(context.getAiKnowledgeSource()));
        inputs.put("ai_knowledge_content", defaultText(context.getAiKnowledgeContent()));
        inputs.put("course_summary", defaultText(context.getCourseSummary()));
        inputs.put("section_summary", defaultText(context.getSectionSummary()));
        inputs.put("learning_progress", defaultText(context.getLearningProgress()));
        inputs.put("current_video_progress", defaultText(context.getCurrentVideoProgress()));
        inputs.put("recent_homework_summary", defaultText(context.getRecentHomeworkSummary()));
        inputs.put("recent_question_summary", defaultText(context.getRecentQuestionSummary()));
        inputs.put("wrong_question_summary", defaultText(context.getWrongQuestionSummary()));
        inputs.put("student_goal", defaultText(context.getStudentGoal()));
        inputs.put("summary_text", defaultText(context.getSummaryText()));
        inputs.put("context_summary", defaultText(context.getSummaryText()));
        return inputs;
    }

    private Map<String, Object> buildWorkflowInputs(CourseLearningAiContext context,
                                                    CourseLearningAiAskRequest request,
                                                    AiAvatar assistant) {
        Map<String, Object> inputs = buildDifyInputs(context, request.getQuestion());
        inputs.put("session_id", defaultText(request.getSessionId()));
        inputs.put("end_chat", request.isEndChat());
        inputs.put("assistant_id", assistant == null ? null : assistant.getId());
        inputs.put("assistant_name", assistant == null ? "" : defaultText(assistant.getName()));
        return inputs;
    }

    private SubjectAiBinding resolveWorkflowBinding(CourseLearningAiContext context) {
        if (context == null || context.getCategoryId() == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "course category is missing, workflow binding cannot be resolved");
        }
        LambdaQueryWrapper<SubjectAiBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectAiBinding::getCategoryId, context.getCategoryId())
                .eq(SubjectAiBinding::getStatus, 1)
                .eq(SubjectAiBinding::getIsDelete, 0)
                .last("limit 1");
        SubjectAiBinding binding = subjectAiBindingMapper.selectOne(wrapper);
        if (binding == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,
                    "course workflow binding not found, categoryId=" + context.getCategoryId());
        }
        if (!StringUtils.hasText(binding.getDifyWorkflowAppKey())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "course workflow app key is blank, categoryId=" + context.getCategoryId());
        }
        return binding;
    }

    private AiAvatar resolveWorkflowAssistant(SubjectAiBinding binding,
                                              CourseLearningAiContext context,
                                              Long preferredAiAvatarId) {
        if (binding != null && binding.getAiAvatarId() != null) {
            AiAvatar bindingAvatar = aiAvatarService.getById(binding.getAiAvatarId());
            if (isAvailableAssistant(bindingAvatar)) {
                return bindingAvatar;
            }
        }
        return resolveAssistant(context, preferredAiAvatarId);
    }

    private void validateRequest(CourseLearningAiAskRequest request, Long userId) {
        if (userId == null || request == null || request.getCourseId() == null || !StringUtils.hasText(request.getQuestion())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "course learning assistant request is invalid");
        }
    }

    private Course loadCourseOrThrow(Long courseId) {
        if (courseId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId is required");
        }
        Course course = courseService.getById(courseId);
        if (course == null || (course.getIsDelete() != null && course.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "course not found");
        }
        return course;
    }

    private AiAvatar resolveCourseExclusiveAssistant(Long preferredAiAvatarId) {
        if (preferredAiAvatarId != null) {
            AiAvatar preferred = aiAvatarService.getById(preferredAiAvatarId);
            if (isAvailableAssistant(preferred)) {
                return preferred;
            }
        }
        LambdaQueryWrapper<AiAvatar> fallbackWrapper = new LambdaQueryWrapper<>();
        fallbackWrapper.eq(AiAvatar::getStatus, 1)
                .eq(AiAvatar::getIsDelete, 0)
                .orderByAsc(AiAvatar::getSort)
                .orderByDesc(AiAvatar::getUsageCount)
                .last("limit 1");
        AiAvatar fallback = aiAvatarService.getOne(fallbackWrapper);
        if (!isAvailableAssistant(fallback)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "no available course assistant found");
        }
        return fallback;
    }

    private String buildCourseExclusivePrompt(Course course, KnowledgeResolution knowledge, String question) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你现在是课程学习助手，请严格基于下面提供的 AI 学习助手知识内容回答用户问题。");
        prompt.append("如果知识内容不足，请明确说明知识库未覆盖，不要用课程标题、简介或无关内容编造答案。\n");
        prompt.append("当前课程名称：").append(defaultText(course.getTitle())).append("\n");
        prompt.append("知识来源：").append(KNOWLEDGE_SOURCE_SECTION.equals(knowledge.getSource()) ? "当前小节" : "课程级兜底").append("\n");
        prompt.append("AI 学习助手知识内容：\n").append(knowledge.getContent()).append("\n\n");
        prompt.append("用户问题：\n").append(defaultText(question));
        return prompt.toString();
    }

    private KnowledgeResolution resolveKnowledge(CourseLearningAiAskRequest request) {
        if (request == null || request.getSectionId() == null) {
            return KnowledgeResolution.tip(SECTION_REQUIRED_MESSAGE);
        }

        Section section = sectionService.getById(request.getSectionId());
        if (section == null || (section.getIsDelete() != null && section.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "section not found");
        }

        if (StringUtils.hasText(section.getAiKnowleage())) {
            log.info("AI使用小节级知识 sectionId={}", request.getSectionId());
            return KnowledgeResolution.knowledge(KNOWLEDGE_SOURCE_SECTION, section.getAiKnowleage().trim());
        }

        Course course = loadCourseOrThrow(request.getCourseId());
        if (StringUtils.hasText(course.getAiKnowleage())) {
            log.warn("AI使用课程级兜底 courseId={} sectionId={}（小节未配置）",
                    request.getCourseId(), request.getSectionId());
            return KnowledgeResolution.knowledge(KNOWLEDGE_SOURCE_COURSE, course.getAiKnowleage().trim());
        }

        log.error("AI知识缺失 courseId={} sectionId={}", request.getCourseId(), request.getSectionId());
        return KnowledgeResolution.tip(KNOWLEDGE_MISSING_MESSAGE);
    }

    private void applyKnowledgeToContext(CourseLearningAiContext context, KnowledgeResolution knowledge) {
        context.setAiKnowledgeSource(knowledge.getSource());
        context.setAiKnowledgeContent(knowledge.getContent());
        context.setSummaryText(learningContextAssembler.buildSummaryText(context));
    }

    private ChatMessageVO buildTipMessage(Long userId, CourseLearningAiAskRequest request, String content) {
        ChatMessageVO message = new ChatMessageVO();
        message.setUserId(userId);
        message.setAiAvatarId(request == null ? null : request.getAiAvatarId());
        message.setSessionId(request == null ? null : request.getSessionId());
        message.setMessageType("ai");
        message.setContent(content);
        message.setTokens(0);
        message.setCreateTime(new Date());
        return message;
    }

    private boolean isAvailableAssistant(AiAvatar avatar) {
        return avatar != null
                && (avatar.getIsDelete() == null || avatar.getIsDelete() == 0)
                && avatar.getStatus() != null
                && avatar.getStatus() == 1;
    }

    private String defaultText(String value) {
        return value == null ? "" : value.trim();
    }

    private static class KnowledgeResolution {

        private final String source;
        private final String content;
        private final String tipMessage;

        private KnowledgeResolution(String source, String content, String tipMessage) {
            this.source = source;
            this.content = content;
            this.tipMessage = tipMessage;
        }

        static KnowledgeResolution knowledge(String source, String content) {
            return new KnowledgeResolution(source, content, null);
        }

        static KnowledgeResolution tip(String tipMessage) {
            return new KnowledgeResolution(null, null, tipMessage);
        }

        boolean canCallAi() {
            return StringUtils.hasText(content);
        }

        String getSource() {
            return source;
        }

        String getContent() {
            return content;
        }

        String getTipMessage() {
            return tipMessage;
        }
    }
}
