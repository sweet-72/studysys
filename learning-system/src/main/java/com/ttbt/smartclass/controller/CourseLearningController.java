package com.ttbt.smartclass.controller;

import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.model.dto.HomeworkSubmitRequest;
import com.ttbt.smartclass.model.dto.SubmitAnswerRequest;
import com.ttbt.smartclass.model.dto.SubmitAnswerResponse;
import com.ttbt.smartclass.model.dto.course.CourseAiAskRequest;
import com.ttbt.smartclass.model.dto.course.CourseDetailRequest;
import com.ttbt.smartclass.model.dto.course.CourseLearnCompleteRequest;
import com.ttbt.smartclass.model.dto.course.CourseLearnStartRequest;
import com.ttbt.smartclass.model.dto.course.CourseLearningAiAskRequest;
import com.ttbt.smartclass.model.dto.course.CourseSectionQuestionsRequest;
import com.ttbt.smartclass.model.dto.course.CourseSubmitRequest;
import com.ttbt.smartclass.model.entity.CourseChapter;
import com.ttbt.smartclass.model.entity.Question;
import com.ttbt.smartclass.model.entity.Section;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.ChatMessageVO;
import com.ttbt.smartclass.model.vo.CourseDetailVO;
import com.ttbt.smartclass.model.vo.CourseLearnStatusVO;
import com.ttbt.smartclass.model.vo.CourseSubmitResultVO;
import com.ttbt.smartclass.model.vo.ExerciseQuestionVO;
import com.ttbt.smartclass.service.AiAvatarChatHistoryService;
import com.ttbt.smartclass.service.AiAvatarService;
import com.ttbt.smartclass.service.CourseChapterService;
import com.ttbt.smartclass.service.CourseLearningAiService;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.DifyService;
import com.ttbt.smartclass.service.HomeworkSubmissionService;
import com.ttbt.smartclass.service.QuestionService;
import com.ttbt.smartclass.service.SectionService;
import com.ttbt.smartclass.service.UserService;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.ttbt.smartclass.utils.SseEmitterUtils;

/**
 * 课程学习控制器。
 * 负责课程详情、学习进度、练习提交和课程 AI 助手相关接口。
 */
@RestController
@RequestMapping("/course")
public class CourseLearningController {

    private static final Set<String> OBJECTIVE_TYPES = Set.of("single", "multiple", "judge", "choice", "objective");

    @Resource
    private CourseService courseService;

    @Resource
    private UserService userService;

    @Resource
    private QuestionService questionService;

    @Resource
    private HomeworkSubmissionService homeworkSubmissionService;

    @Resource
    private SectionService sectionService;

    @Resource
    private CourseChapterService courseChapterService;

    @Resource
    private DifyService difyService;

    @Resource
    private AiAvatarService aiAvatarService;

    @Resource
    private AiAvatarChatHistoryService aiAvatarChatHistoryService;

    @Resource
    private CourseLearningAiService courseLearningAiService;

    @GetMapping("/detail")
    public BaseResponse<CourseDetailVO> getCourseDetail(@RequestParam("course_id") Long courseId,
                                                         HttpServletRequest httpRequest) {
        if (courseId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "course_id is required");
        }
        User loginUser = userService.getLoginUserPermitNull(httpRequest);
        Long userId = loginUser == null ? null : loginUser.getId();
        return ResultUtils.success(courseService.getCourseDetail(courseId, userId));
    }

    /**
     * 开始学习指定课程小节。
     *
     * @param request 课程小节学习开始请求
     * @param httpRequest 当前 HTTP 请求
     * @return 当前小节学习状态
     */
    @PostMapping("/learn/start")
    public BaseResponse<CourseLearnStatusVO> startLearning(@RequestBody CourseLearnStartRequest request,
                                                           HttpServletRequest httpRequest) {
        // 校验课程和小节参数，开始学习必须明确具体小节
        if (request == null || request.getCourseId() == null || request.getSectionId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "course_id and sectionId are required");
        }
        // 获取当前用户并创建或更新课程、小节学习进度
        User loginUser = userService.getLoginUser(httpRequest);
        courseService.startSectionLearning(loginUser.getId(), request.getCourseId(), request.getSectionId());

        // 返回前端当前小节已进入学习中的状态
        CourseLearnStatusVO vo = new CourseLearnStatusVO();
        vo.setCourseId(request.getCourseId());
        vo.setSectionId(request.getSectionId());
        vo.setStatus("LEARNING");
        return ResultUtils.success(vo);
    }

    /**
     * 完成当前用户指定小节的学习。
     *
     * @param request 小节学习完成请求
     * @param httpRequest 当前 HTTP 请求
     * @return 当前小节完成状态
     */
    @PostMapping("/learn/complete")
    public BaseResponse<CourseLearnStatusVO> completeLearning(@RequestBody CourseLearnCompleteRequest request,
                                                              HttpServletRequest httpRequest) {
        // 校验小节参数，完成学习需要定位具体小节
        if (request == null || request.getSectionId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "sectionId is required");
        }
        // 获取当前用户并根据小节反查课程，用于返回完整学习状态
        User loginUser = userService.getLoginUser(httpRequest);
        Long courseId = resolveCourseIdBySection(request.getSectionId());
        // 调用服务层校验学习条件，满足后标记小节完成并刷新课程进度
        courseService.completeSectionLearning(loginUser.getId(), request.getSectionId());

        // 返回前端当前小节已完成的状态
        CourseLearnStatusVO vo = new CourseLearnStatusVO();
        vo.setCourseId(courseId);
        vo.setSectionId(request.getSectionId());
        vo.setStatus("COMPLETED");
        return ResultUtils.success(vo);
    }

    @GetMapping("/section/questions")
    public BaseResponse<List<ExerciseQuestionVO>> getSectionQuestions(@ModelAttribute CourseSectionQuestionsRequest request,
                                                                      HttpServletRequest httpRequest) {
        if (request == null || request.getSectionId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "sectionId is required");
        }
        User loginUser = userService.getLoginUser(httpRequest);
        courseService.assertSectionLearnable(loginUser.getId(), request.getSectionId());
        return ResultUtils.success(courseService.listPublicExercisesBySection(request.getSectionId()));
    }

    /**
     * 提交课程小节练习答案，并根据题型决定自动判题或转入教师批改。
     *
     * @param request 答案提交请求
     * @param httpRequest 当前 HTTP 请求
     * @return 答题结果或作业提交结果
     */
    @PostMapping("/submit")
    public BaseResponse<CourseSubmitResultVO> submitAnswer(@RequestBody CourseSubmitRequest request,
                                                           HttpServletRequest httpRequest) {
        // 提交答案必须明确小节和题目，缺少任一参数都无法判断归属关系
        if (request == null || request.getQuestionId() == null || request.getSectionId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "sectionId and questionId are required");
        }
        // 获取当前登录用户，并校验题目确实属于本次提交的小节
        User loginUser = userService.getLoginUser(httpRequest);
        Question question = questionService.getById(request.getQuestionId());
        if (question == null || question.getSectionId() == null || !question.getSectionId().equals(request.getSectionId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "question does not belong to section");
        }

        // 校验用户是否满足课程学习顺序，未解锁的小节不能提交答案
        courseService.assertSectionLearnable(loginUser.getId(), request.getSectionId());

        // 客观题走自动判题流程，立即返回正确性、得分、正确答案和解析
        String type = question.getType() == null ? "" : question.getType().trim().toLowerCase(Locale.ROOT);
        if (OBJECTIVE_TYPES.contains(type)) {
            SubmitAnswerRequest submitRequest = new SubmitAnswerRequest();
            submitRequest.setUserId(loginUser.getId());
            submitRequest.setQuestionId(request.getQuestionId());
            submitRequest.setAnswer(request.getAnswer());
            submitRequest.setTimeSpent(request.getTimeSpent());
            SubmitAnswerResponse submitResponse = courseService.submitAnswer(submitRequest);

            CourseSubmitResultVO vo = new CourseSubmitResultVO();
            vo.setSubmitType("objective");
            vo.setAutoGraded(true);
            vo.setCorrect(submitResponse.isCorrect());
            vo.setScore(submitResponse.getScore());
            vo.setCorrectAnswer(submitResponse.getCorrectAnswer());
            vo.setExplanation(submitResponse.getExplanation());
            vo.setMessage("submitted and auto graded");
            return ResultUtils.success(vo);
        }

        // 主观题创建作业提交记录，等待教师在管理端批阅
        HomeworkSubmitRequest homeworkSubmitRequest = new HomeworkSubmitRequest();
        homeworkSubmitRequest.setSectionId(request.getSectionId());
        homeworkSubmitRequest.setQuestionId(request.getQuestionId());
        homeworkSubmitRequest.setAnswer(request.getAnswer());
        Long submissionId = homeworkSubmissionService.submitHomework(homeworkSubmitRequest, loginUser.getId());

        CourseSubmitResultVO vo = new CourseSubmitResultVO();
        vo.setSubmitType("subjective");
        vo.setAutoGraded(false);
        vo.setSubmissionId(submissionId);
        vo.setReviewStatus(0);
        vo.setMessage("submitted, waiting teacher review");
        return ResultUtils.success(vo);
    }

    /**
     * 向课程 AI 助手提问，并在存在课程 ID 时自动拼接课程上下文。
     * 方法会把课程标题、简介、难度等信息加入 prompt，使 AI 回复更贴合当前课程学习场景。
     * 最终通过 DifyService 发起非流式对话，并写入 AI 聊天历史。
     *
     * 处理流程：
     * 1. 校验 AI 数字人 ID 和问题内容。
     * 2. 获取当前登录用户。
     * 3. 查询课程详情并构造课程上下文。
     * 4. 调用 Dify 对话服务生成回复。
     *
     * @param request 课程 AI 提问请求
     * @param httpRequest 当前 HTTP 请求
     * @return AI 回复消息
     */
    @PostMapping("/ai/ask")
    public BaseResponse<ChatMessageVO> askCourseAi(@RequestBody CourseAiAskRequest request,
                                                   HttpServletRequest httpRequest) {
        if (request == null || request.getAiAvatarId() == null || request.getQuestion() == null
                || request.getQuestion().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "aiAvatarId and question are required");
        }

        User loginUser = userService.getLoginUser(httpRequest);
        String prompt = request.getQuestion().trim();

        if (request.getCourseId() != null) {
            CourseDetailVO detail = courseService.getCourseDetail(request.getCourseId(), loginUser.getId());
            String courseContext = "当前课程：" + safe(detail.getTitle())
                    + "；简介：" + safe(detail.getDescription())
                    + "；难度：" + safe(detail.getDifficulty())
                    + "；请结合该课程上下文回答用户问题。";
            prompt = courseContext + "\n用户问题：" + prompt;
        }

        ChatMessageVO messageVO = difyService.handleSendMessageRequest(
                loginUser.getId(),
                request.getAiAvatarId(),
                request.getSessionId(),
                prompt,
                request.isEndChat(),
                aiAvatarChatHistoryService,
                aiAvatarService,
                userService
        );
        return ResultUtils.success(messageVO);
    }

    /**
     * 向课程学习 AI 助手发起非流式提问。
     *
     * @param request 课程学习 AI 提问请求
     * @param httpRequest 当前 HTTP 请求
     * @return AI 回复消息
     */
    @PostMapping("/ai/learn/ask")
    public BaseResponse<ChatMessageVO> askCourseLearningAssistant(
            @Valid @RequestBody CourseLearningAiAskRequest request,
            HttpServletRequest httpRequest) {
        // 获取当前登录用户，将用户维度交给服务层组装学习上下文
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(courseLearningAiService.askCourseLearningAssistant(request, loginUser.getId()));
    }

    /**
     * 向课程学习 AI 助手发起流式提问。
     *
     * @param request 课程学习 AI 提问请求
     * @param httpRequest 当前 HTTP 请求
     * @return SSE 流式响应对象
     */
    @PostMapping(value = "/ai/learn/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter askCourseLearningAssistantStream(
            @Valid @RequestBody CourseLearningAiAskRequest request,
            HttpServletRequest httpRequest) {
        try {
            // 获取当前登录用户，并由服务层通过 Dify Workflow 持续返回增量回复
            User loginUser = userService.getLoginUser(httpRequest);
            return courseLearningAiService.askCourseLearningAssistantStream(request, loginUser.getId());
        } catch (Exception e) {
            // 流式接口失败时仍返回 SSE 错误对象，便于前端统一处理
            return SseEmitterUtils.createErrorEmitter(e.getMessage());
        }
    }

    /**
     * 向课程专属 AI 助手发起非流式提问。
     *
     * @param request 课程学习 AI 提问请求
     * @param httpRequest 当前 HTTP 请求
     * @return AI 回复消息
     */
    @PostMapping("/ai/course-assistant/ask")
    public BaseResponse<ChatMessageVO> askCourseExclusiveAssistant(
            @Valid @RequestBody CourseLearningAiAskRequest request,
            HttpServletRequest httpRequest) {
        // 获取当前登录用户，由服务层加载课程专属知识并调用对应智能体
        User loginUser = userService.getLoginUser(httpRequest);
        return ResultUtils.success(courseLearningAiService.askCourseExclusiveAssistant(request, loginUser.getId()));
    }

    /**
     * 向课程专属 AI 助手发起流式提问。
     *
     * @param request 课程学习 AI 提问请求
     * @param httpRequest 当前 HTTP 请求
     * @return SSE 流式响应对象
     */
    @PostMapping(value = "/ai/course-assistant/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter askCourseExclusiveAssistantStream(
            @Valid @RequestBody CourseLearningAiAskRequest request,
            HttpServletRequest httpRequest) {
        try {
            // 获取当前登录用户，并通过课程专属助手流式返回 Dify 回复
            User loginUser = userService.getLoginUser(httpRequest);
            return courseLearningAiService.askCourseExclusiveAssistantStream(request, loginUser.getId());
        } catch (Exception e) {
            // 流式接口失败时构造 SSE 错误响应，避免前端连接直接中断无提示
            return SseEmitterUtils.createErrorEmitter(e.getMessage());
        }
    }

    private Long resolveCourseIdBySection(Long sectionId) {
        Section section = sectionService.getById(sectionId);
        if (section == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "section not found");
        }
        CourseChapter chapter = courseChapterService.getById(section.getChapterId());
        if (chapter == null || chapter.getCourseId() == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "chapter not found");
        }
        return chapter.getCourseId();
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
