package com.ttbt.smartclass.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.constant.CourseLearningAiConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.AiLearningContextSnapshotMapper;
import com.ttbt.smartclass.mapper.UserAnswerMapper;
import com.ttbt.smartclass.model.dto.course.CourseLearningAiAskRequest;
import com.ttbt.smartclass.model.dto.course.CourseLearningAiContext;
import com.ttbt.smartclass.model.entity.AiLearningContextSnapshot;
import com.ttbt.smartclass.model.entity.Course;
import com.ttbt.smartclass.model.entity.CourseCategory;
import com.ttbt.smartclass.model.entity.CourseChapter;
import com.ttbt.smartclass.model.entity.Question;
import com.ttbt.smartclass.model.entity.Section;
import com.ttbt.smartclass.model.entity.UserAnswer;
import com.ttbt.smartclass.model.entity.VideoLearningRecord;
import com.ttbt.smartclass.model.vo.CourseDetailVO;
import com.ttbt.smartclass.model.vo.CourseProgressVO;
import com.ttbt.smartclass.model.vo.HomeworkSubmissionVO;
import com.ttbt.smartclass.service.CourseCategoryService;
import com.ttbt.smartclass.service.CourseChapterService;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.HomeworkSubmissionService;
import com.ttbt.smartclass.service.LearningContextAssembler;
import com.ttbt.smartclass.service.QuestionService;
import com.ttbt.smartclass.service.SectionService;
import com.ttbt.smartclass.service.VideoLearningRecordService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 课程学习 AI 上下文组装服务实现类。
 */
@Service
@Slf4j
public class LearningContextAssemblerImpl implements LearningContextAssembler {

    private static final int RECENT_HOMEWORK_LIMIT = 5;
    private static final int RECENT_QUESTION_LIMIT = 5;
    private static final int WRONG_QUESTION_LIMIT = 5;

    @Resource
    private CourseService courseService;

    @Resource
    private CourseChapterService courseChapterService;

    @Resource
    private SectionService sectionService;

    @Resource
    private CourseCategoryService courseCategoryService;

    @Resource
    private VideoLearningRecordService videoLearningRecordService;

    @Resource
    private HomeworkSubmissionService homeworkSubmissionService;

    @Resource
    private QuestionService questionService;

    @Resource
    private UserAnswerMapper userAnswerMapper;

    @Resource
    private AiLearningContextSnapshotMapper aiLearningContextSnapshotMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public CourseLearningAiContext assemble(Long userId, CourseLearningAiAskRequest request) {
        if (userId == null || request == null || request.getCourseId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程学习上下文参数不完整");
        }

        String cacheKey = buildCacheKey(userId, request.getCourseId(), request.getChapterId(), request.getSectionId());
        CourseLearningAiContext cached = getCachedContext(cacheKey);
        if (cached != null) {
            cached.setStudentGoal(resolveStudentGoal(request.getStudentGoal()));
            cached.setSnapshotTime(new Date());
            return cached;
        }

        CourseLearningAiContext context = new CourseLearningAiContext();
        context.setUserId(userId);
        context.setCourseId(request.getCourseId());
        context.setChapterId(request.getChapterId());
        context.setSectionId(request.getSectionId());
        context.setStudentGoal(resolveStudentGoal(request.getStudentGoal()));
        context.setSnapshotTime(new Date());

        fillCourseContext(context, userId);
        fillChapterAndSectionContext(context);
        fillProgressContext(context, userId);
        fillHomeworkContext(context, userId);
        fillQuestionContext(context, userId);
        context.setSummaryText(buildSummaryText(context));

        cacheContext(cacheKey, context);
        saveSnapshotIfNecessary(context);
        return context;
    }

    @Override
    public String buildSummaryText(CourseLearningAiContext context) {
        StringBuilder sb = new StringBuilder();
        appendLine(sb, "学生ID", context.getUserId());
        appendLine(sb, "课程ID", context.getCourseId());
        appendLine(sb, "课程标题", context.getCourseTitle());
        appendLine(sb, "学科", context.getCategoryName());
        appendLine(sb, "章节", context.getChapterTitle());
        appendLine(sb, "小节", context.getSectionTitle());
        appendLine(sb, "课程摘要", context.getCourseSummary());
        appendLine(sb, "当前小节摘要", context.getSectionSummary());
        appendLine(sb, "AI知识来源", context.getAiKnowledgeSource());
        appendLine(sb, "AI学习助手知识内容", context.getAiKnowledgeContent());
        appendLine(sb, "学习进度", context.getLearningProgress());
        appendLine(sb, "当前视频进度", context.getCurrentVideoProgress());
        appendLine(sb, "近期作业情况", context.getRecentHomeworkSummary());
        appendLine(sb, "当前题目概览", context.getRecentQuestionSummary());
        appendLine(sb, "近期错题摘要", context.getWrongQuestionSummary());
        appendLine(sb, "本轮学习目标", context.getStudentGoal());
        return sb.toString().trim();
    }

    @Override
    public void saveSnapshotIfNecessary(CourseLearningAiContext context) {
        if (context == null || context.getUserId() == null || context.getCourseId() == null) {
            return;
        }
        try {
            LambdaQueryWrapper<AiLearningContextSnapshot> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiLearningContextSnapshot::getUserId, context.getUserId())
                    .eq(AiLearningContextSnapshot::getCourseId, context.getCourseId())
                    .eq(context.getChapterId() != null, AiLearningContextSnapshot::getChapterId, context.getChapterId())
                    .isNull(context.getChapterId() == null, AiLearningContextSnapshot::getChapterId)
                    .eq(context.getSectionId() != null, AiLearningContextSnapshot::getSectionId, context.getSectionId())
                    .isNull(context.getSectionId() == null, AiLearningContextSnapshot::getSectionId)
                    .orderByDesc(AiLearningContextSnapshot::getSnapshotTime)
                    .last("limit 1");
            AiLearningContextSnapshot snapshot = aiLearningContextSnapshotMapper.selectOne(wrapper);
            if (snapshot == null) {
                snapshot = new AiLearningContextSnapshot();
                snapshot.setUserId(context.getUserId());
                snapshot.setCourseId(context.getCourseId());
                snapshot.setChapterId(context.getChapterId());
                snapshot.setSectionId(context.getSectionId());
            }
            snapshot.setLatestProgressJson(JSONUtil.toJsonStr(buildProgressPayload(context)));
            snapshot.setLatestHomeworkJson(JSONUtil.toJsonStr(buildHomeworkPayload(context)));
            snapshot.setLatestWrongQuestionsJson(JSONUtil.toJsonStr(buildWrongQuestionPayload(context)));
            snapshot.setSummaryText(context.getSummaryText());
            snapshot.setSnapshotTime(new Date());
            if (snapshot.getId() == null) {
                aiLearningContextSnapshotMapper.insert(snapshot);
            } else {
                aiLearningContextSnapshotMapper.updateById(snapshot);
            }
        } catch (Exception e) {
            log.warn("save learning context snapshot failed, course_id={}, sectionId={}",
                    context.getCourseId(), context.getSectionId(), e);
        }
    }

    private void fillCourseContext(CourseLearningAiContext context, Long userId) {
        Course course = courseService.getById(context.getCourseId());
        if (course == null || isDeleted(course.getIsDelete())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        }
        CourseDetailVO detailVO = courseService.getCourseDetail(context.getCourseId(), userId);
        context.setCourseTitle(detailVO.getTitle());
        context.setCourseSummary(defaultText(detailVO.getDescription(), course.getDescription()));
        context.setCategoryId(course.getCategoryId());
        if (course.getCategoryId() != null) {
            CourseCategory category = courseCategoryService.getById(course.getCategoryId());
            if (category != null && !isDeleted(category.getIsDelete())) {
                context.setCategoryName(category.getName());
            }
        }
    }

    private void fillChapterAndSectionContext(CourseLearningAiContext context) {
        if (context.getSectionId() != null) {
            Section section = sectionService.getById(context.getSectionId());
            if (section == null || isDeleted(section.getIsDelete())) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "小节不存在");
            }
            context.setSectionTitle(section.getTitle());
            context.setSectionSummary(buildSectionSummary(section));
            context.setChapterId(section.getChapterId());
        }

        if (context.getChapterId() != null) {
            CourseChapter chapter = courseChapterService.getById(context.getChapterId());
            if (chapter == null || isDeleted(chapter.getIsDelete())) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "章节不存在");
            }
            context.setChapterTitle(chapter.getTitle());
        }

        if (!StringUtils.hasText(context.getSectionSummary())) {
            context.setSectionSummary("当前小节暂无普通内容摘要。");
        }
    }

    private void fillProgressContext(CourseLearningAiContext context, Long userId) {
        try {
            CourseProgressVO progressVO = courseService.getCourseProgress(context.getCourseId(), userId);
            context.setCourseProgressPercent(progressVO.getProgress());
            context.setCompletedSections(progressVO.getCompletedSections());
            context.setTotalSections(progressVO.getTotalSections());
            context.setLearningProgress(String.format("已完成 %s/%s 小节，课程进度约 %s%%。",
                    safeInt(progressVO.getCompletedSections()),
                    safeInt(progressVO.getTotalSections()),
                    safeInt(progressVO.getProgress())));
        } catch (Exception e) {
            log.warn("build course progress context failed, course_id={}, user_id={}", context.getCourseId(), userId, e);
            context.setLearningProgress("暂未获取到课程进度数据。");
        }

        if (context.getSectionId() == null) {
            context.setCurrentVideoProgress("当前未指定视频小节。");
            return;
        }

        try {
            VideoLearningRecord record = videoLearningRecordService.getVideoProgress(context.getSectionId(), userId);
            if (record == null) {
                context.setCurrentVideoProgress("当前小节暂无视频学习记录。");
                return;
            }
            String completedText = record.getIsCompleted() != null && record.getIsCompleted() == 1 ? "已完成" : "学习中";
            context.setCurrentVideoProgress(String.format("视频进度 %s%%，最后观看到 %s 秒，累计学习 %s 秒，状态：%s。",
                    safeBigDecimalText(record.getProgress()),
                    safeInt(record.getLastWatchPosition()),
                    safeInt(record.getLearnedTime()),
                    completedText));
        } catch (Exception e) {
            log.warn("build video progress context failed, sectionId={}, user_id={}", context.getSectionId(), userId, e);
            context.setCurrentVideoProgress("暂未获取到当前视频进度。");
        }
    }

    private void fillHomeworkContext(CourseLearningAiContext context, Long userId) {
        try {
            List<HomeworkSubmissionVO> submissions;
            if (context.getSectionId() != null) {
                submissions = homeworkSubmissionService.getSectionExercises(context.getSectionId(), userId);
            } else {
                submissions = homeworkSubmissionService.getMyHomeworkList(1L, (long) RECENT_HOMEWORK_LIMIT, userId, null)
                        .getRecords();
            }
            context.setRecentHomeworkSummary(buildHomeworkSummary(submissions));
        } catch (Exception e) {
            log.warn("build homework context failed, course_id={}, sectionId={}, user_id={}",
                    context.getCourseId(), context.getSectionId(), userId, e);
            context.setRecentHomeworkSummary("暂未获取到近期作业情况。");
        }
    }

    private void fillQuestionContext(CourseLearningAiContext context, Long userId) {
        try {
            List<Question> sectionQuestions = Collections.emptyList();
            if (context.getSectionId() != null) {
                sectionQuestions = courseService.getSectionQuestions(context.getSectionId());
            }
            context.setRecentQuestionSummary(buildQuestionSummary(sectionQuestions));
            context.setWrongQuestionSummary(buildWrongQuestionSummary(userId, sectionQuestions));
        } catch (Exception e) {
            log.warn("build question context failed, course_id={}, sectionId={}, user_id={}",
                    context.getCourseId(), context.getSectionId(), userId, e);
            context.setRecentQuestionSummary("暂未获取到题目概览。");
            context.setWrongQuestionSummary("暂未获取到错题摘要。");
        }
    }

    private String buildHomeworkSummary(List<HomeworkSubmissionVO> submissions) {
        if (submissions == null || submissions.isEmpty()) {
            return "当前范围内暂无作业提交记录。";
        }
        int total = submissions.size();
        long submitted = submissions.stream().filter(item -> StringUtils.hasText(item.getAnswer())).count();
        long graded = submissions.stream().filter(item -> item.getStatus() != null && item.getStatus() > 0).count();
        long pending = submissions.stream().filter(item -> item.getStatus() != null && item.getStatus() == 0).count();
        String titles = submissions.stream()
                .map(HomeworkSubmissionVO::getQuestionContent)
                .filter(StringUtils::hasText)
                .limit(3)
                .collect(Collectors.joining("；"));
        return String.format("共 %s 道作业/练习题，已作答 %s 道，已批改 %s 道，待批改 %s 道。%s",
                total, submitted, graded, pending,
                StringUtils.hasText(titles) ? "代表题目：" + trimText(titles, 120) : "");
    }

    private String buildQuestionSummary(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return "当前范围内暂无题目概览。";
        }
        List<Question> sampleList = questions.stream().limit(RECENT_QUESTION_LIMIT).collect(Collectors.toList());
        String summary = sampleList.stream()
                .map(question -> String.format("[%s] %s",
                        defaultText(question.getType(), "unknown"),
                        trimText(question.getContent(), 40)))
                .collect(Collectors.joining("；"));
        return String.format("当前范围共 %s 道题，示例：%s", questions.size(), summary);
    }

    private String buildWrongQuestionSummary(Long userId, List<Question> sectionQuestions) {
        LambdaQueryWrapper<UserAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAnswer::getUserId, userId)
                .eq(UserAnswer::getIsCorrect, 0)
                .orderByDesc(UserAnswer::getUpdateTime)
                .last("limit " + WRONG_QUESTION_LIMIT);

        Set<Long> sectionQuestionIds = sectionQuestions == null ? Collections.emptySet()
                : sectionQuestions.stream().map(Question::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (!sectionQuestionIds.isEmpty()) {
            wrapper.in(UserAnswer::getQuestionId, sectionQuestionIds);
        }

        List<UserAnswer> wrongAnswers = userAnswerMapper.selectList(wrapper);
        if (wrongAnswers == null || wrongAnswers.isEmpty()) {
            return "当前范围内暂无近期错题。";
        }

        List<Long> questionIds = wrongAnswers.stream()
                .map(UserAnswer::getQuestionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<Question> questions = questionIds.isEmpty() ? Collections.emptyList() : questionService.listByIds(questionIds);
        if (questions.isEmpty()) {
            return String.format("近期有 %s 道错题，但暂未获取到题目详情。", wrongAnswers.size());
        }

        List<String> items = new ArrayList<>();
        for (Question question : questions.stream().limit(WRONG_QUESTION_LIMIT).collect(Collectors.toList())) {
            String explanation = trimText(defaultText(question.getExplanation(), "建议回看解析与课程讲解。"), 60);
            items.add(trimText(question.getContent(), 36) + "，解析提示：" + explanation);
        }
        return "近期重点错题：" + String.join("；", items);
    }

    private String buildSectionSummary(Section section) {
        if (section == null) {
            return null;
        }
        if (StringUtils.hasText(section.getContent())) {
            return trimText(section.getContent(), 240);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("当前小节标题：").append(defaultText(section.getTitle(), "未命名小节")).append("。");
        if (StringUtils.hasText(section.getType())) {
            sb.append("类型：").append(section.getType()).append("。");
        }
        if (section.getVideoDuration() != null && section.getVideoDuration() > 0) {
            sb.append("视频时长约 ").append(section.getVideoDuration()).append(" 秒。");
        }
        if (StringUtils.hasText(section.getResourceUrl())) {
            sb.append("包含课程资源。");
        }
        return sb.toString();
    }

    private Object buildProgressPayload(CourseLearningAiContext context) {
        return JSONUtil.createObj()
                .set("courseProgressPercent", context.getCourseProgressPercent())
                .set("completedSections", context.getCompletedSections())
                .set("totalSections", context.getTotalSections())
                .set("learningProgress", context.getLearningProgress())
                .set("currentVideoProgress", context.getCurrentVideoProgress());
    }

    private Object buildHomeworkPayload(CourseLearningAiContext context) {
        return JSONUtil.createObj()
                .set("recentHomeworkSummary", context.getRecentHomeworkSummary())
                .set("recentQuestionSummary", context.getRecentQuestionSummary());
    }

    private Object buildWrongQuestionPayload(CourseLearningAiContext context) {
        return JSONUtil.createObj()
                .set("wrongQuestionSummary", context.getWrongQuestionSummary());
    }

    private CourseLearningAiContext getCachedContext(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached == null) {
                return null;
            }
            return JSONUtil.toBean(String.valueOf(cached), CourseLearningAiContext.class);
        } catch (Exception e) {
            log.warn("read learning context cache failed, cacheKey={}", cacheKey, e);
            return null;
        }
    }

    private void cacheContext(String cacheKey, CourseLearningAiContext context) {
        try {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    JSONUtil.toJsonStr(context),
                    CourseLearningAiConstant.CONTEXT_CACHE_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            log.warn("write learning context cache failed, cacheKey={}", cacheKey, e);
        }
    }

    private String buildCacheKey(Long userId, Long courseId, Long chapterId, Long sectionId) {
        return CourseLearningAiConstant.CONTEXT_CACHE_KEY_PREFIX
                + userId + ":" + courseId + ":" + safeKey(chapterId) + ":" + safeKey(sectionId);
    }

    private String resolveStudentGoal(String studentGoal) {
        return StringUtils.hasText(studentGoal) ? studentGoal.trim() : CourseLearningAiConstant.DEFAULT_STUDENT_GOAL;
    }

    private String safeKey(Long value) {
        return value == null ? "none" : String.valueOf(value);
    }

    private void appendLine(StringBuilder sb, String label, Object value) {
        if (value == null) {
            return;
        }
        String text = String.valueOf(value).trim();
        if (!text.isEmpty()) {
            sb.append(label).append("：").append(text).append("\n");
        }
    }

    private String defaultText(String primary, String fallback) {
        return StringUtils.hasText(primary) ? primary.trim() : (StringUtils.hasText(fallback) ? fallback.trim() : "");
    }

    private String trimText(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
    }

    private boolean isDeleted(Integer isDelete) {
        return isDelete != null && isDelete != 0;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String safeBigDecimalText(Object value) {
        return value == null ? "0" : String.valueOf(value);
    }
}
