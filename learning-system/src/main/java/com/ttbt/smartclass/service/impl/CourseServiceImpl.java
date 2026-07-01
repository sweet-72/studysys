package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.constant.UserConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.CourseChapterMapper;
import com.ttbt.smartclass.mapper.CourseMapper;
import com.ttbt.smartclass.mapper.CourseRatingMapper;
import com.ttbt.smartclass.mapper.CourseViewRecordMapper;
import com.ttbt.smartclass.mapper.HomeworkSubmissionMapper;
import com.ttbt.smartclass.mapper.QuestionMapper;
import com.ttbt.smartclass.mapper.SectionMapper;
import com.ttbt.smartclass.mapper.UserAnswerMapper;
import com.ttbt.smartclass.mapper.UserCourseMapper;
import com.ttbt.smartclass.mapper.UserProgressMapper;
import com.ttbt.smartclass.mapper.VideoLearningRecordMapper;
import com.ttbt.smartclass.model.dto.SubmitAnswerRequest;
import com.ttbt.smartclass.model.dto.SubmitAnswerResponse;
import com.ttbt.smartclass.model.dto.course.CourseAddRequest;
import com.ttbt.smartclass.model.entity.Course;
import com.ttbt.smartclass.model.entity.CourseChapter;
import com.ttbt.smartclass.model.entity.CourseRating;
import com.ttbt.smartclass.model.entity.CourseViewRecord;
import com.ttbt.smartclass.model.entity.HomeworkSubmission;
import com.ttbt.smartclass.model.entity.Question;
import com.ttbt.smartclass.model.entity.Section;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.entity.UserAnswer;
import com.ttbt.smartclass.model.entity.UserCourse;
import com.ttbt.smartclass.model.entity.UserProgress;
import com.ttbt.smartclass.model.entity.VideoLearningRecord;
import com.ttbt.smartclass.model.vo.CourseCatalogVO;
import com.ttbt.smartclass.model.vo.CourseDetailVO;
import com.ttbt.smartclass.model.vo.CourseProgressVO;
import com.ttbt.smartclass.model.vo.CourseVO;
import com.ttbt.smartclass.model.vo.ExerciseQuestionVO;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.UserProgressService;
import com.ttbt.smartclass.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.dao.DuplicateKeyException;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 课程核心服务实现类。
 * 负责课程目录、学习进度、答题判分、课程推荐和课程管理基础能力。
 */
@Slf4j
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private CourseChapterMapper courseChapterMapper;

    @Resource
    private SectionMapper sectionMapper;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private UserAnswerMapper userAnswerMapper;

    @Resource
    private HomeworkSubmissionMapper homeworkSubmissionMapper;

    @Resource
    private UserProgressMapper userProgressMapper;

    @Resource
    private UserCourseMapper userCourseMapper;

    @Resource
    private VideoLearningRecordMapper videoLearningRecordMapper;

    @Resource
    private CourseRatingMapper courseRatingMapper;

    @Resource
    private CourseViewRecordMapper courseViewRecordMapper;

    @Resource
    private UserProgressService userProgressService;

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String COURSE_CATALOG_CACHE_KEY = "course:catalog:";
    private static final String USER_SUBMIT_KEY = "user:submit:question:";
    private static final BigDecimal VIDEO_COMPLETION_THRESHOLD = BigDecimal.valueOf(90);
    private static final Set<String> OBJECTIVE_QUESTION_TYPES = new HashSet<>(Arrays.asList("single", "multiple", "judge", "choice", "objective"));

    @Override
    public CourseCatalogVO getCourseCatalog(Long courseId, Long userId) {
        boolean usePublicCache = userId == null;
        String cacheKey = COURSE_CATALOG_CACHE_KEY + courseId;
        if (usePublicCache) {
            try {
                CourseCatalogVO cached = (CourseCatalogVO) redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    return cached;
                }
            } catch (Exception e) {
                log.warn("read course catalog cache failed, course_id={}", courseId, e);
            }
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null || isDeleted(course.getIsDelete())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "course not found");
        }

        LambdaQueryWrapper<CourseChapter> chapterQuery = new LambdaQueryWrapper<>();
        chapterQuery.eq(CourseChapter::getCourseId, courseId)
                .eq(CourseChapter::getIsDelete, 0)
                .orderByAsc(CourseChapter::getSort);
        List<CourseChapter> chapters = courseChapterMapper.selectList(chapterQuery);

        Set<Long> learnedSectionIds = new HashSet<>();
        if (userId != null) {
            LambdaQueryWrapper<UserProgress> progressQuery = new LambdaQueryWrapper<>();
            progressQuery.eq(UserProgress::getUserId, userId)
                    .eq(UserProgress::getCourseId, courseId)
                    .eq(UserProgress::getStatus, 2);
            List<UserProgress> progressList = userProgressMapper.selectList(progressQuery);
            learnedSectionIds = progressList.stream()
                    .map(UserProgress::getSectionId)
                    .filter(id -> id != null)
                    .collect(Collectors.toSet());
        }

        CourseCatalogVO catalogVO = new CourseCatalogVO();
        catalogVO.setCourseId(courseId);
        catalogVO.setCourseTitle(course.getTitle());
        catalogVO.setTotalSections(safeInt(course.getTotalSections()));

        List<CourseCatalogVO.ChapterInfo> chapterInfoList = new ArrayList<>();
        for (CourseChapter chapter : chapters) {
            CourseCatalogVO.ChapterInfo chapterInfo = new CourseCatalogVO.ChapterInfo();
            chapterInfo.setChapterId(chapter.getId());
            chapterInfo.setChapterTitle(chapter.getTitle());
            // 兼容旧版前端字段，补充通用章节标题
            chapterInfo.setTitle(chapter.getTitle());

            LambdaQueryWrapper<Section> sectionQuery = new LambdaQueryWrapper<>();
            sectionQuery.eq(Section::getChapterId, chapter.getId())
                    .eq(Section::getIsDelete, 0)
                    .orderByAsc(Section::getSortOrder);
            List<Section> sections = sectionMapper.selectList(sectionQuery);

            List<CourseCatalogVO.SectionInfo> sectionInfoList = new ArrayList<>();
            for (Section section : sections) {
                CourseCatalogVO.SectionInfo sectionInfo = new CourseCatalogVO.SectionInfo();
                sectionInfo.setSectionId(section.getId());
                sectionInfo.setSectionTitle(section.getTitle());
                // 兼容旧版前端字段，补充通用小节标题
                sectionInfo.setTitle(section.getTitle());
                sectionInfo.setType(section.getType());
                sectionInfo.setVideoUrl(resolveSectionVideoUrl(section));
                sectionInfo.setContent(section.getContent());
                sectionInfo.setIsFree(section.getIsFree());
                boolean learned = learnedSectionIds.contains(section.getId());
                sectionInfo.setIsLearned(learned);
                sectionInfo.setLearnStatus(learned ? "COMPLETED" : "NOT_STARTED");
                sectionInfoList.add(sectionInfo);
            }

            chapterInfo.setSections(sectionInfoList);
            chapterInfoList.add(chapterInfo);
        }

        catalogVO.setChapters(chapterInfoList);

        if (usePublicCache) {
            try {
                redisTemplate.opsForValue().set(cacheKey, catalogVO, 10, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("write course catalog cache failed, course_id={}", courseId, e);
            }
        }

        return catalogVO;
    }

    @Override
    public CourseDetailVO getCourseDetail(Long courseId, Long userId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || isDeleted(course.getIsDelete())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "course not found");
        }

        CourseCatalogVO catalog = getCourseCatalog(courseId, userId);
        CourseDetailVO vo = new CourseDetailVO();
        vo.setId(course.getId());
        vo.setTitle(course.getTitle());
        vo.setSubtitle(course.getSubtitle());
        vo.setDescription(course.getDescription());
        vo.setCoverUrl(course.getCoverUrl());
        vo.setTeacherId(course.getTeacherId());
        vo.setCourseType(course.getCourseType());
        vo.setDifficulty(course.getDifficulty());
        vo.setTotalSections(course.getTotalSections());
        vo.setTotalQuestions(course.getTotalQuestions());
        vo.setStatus(course.getStatus());
        vo.setRatingScore(course.getRatingScore());
        vo.setRatingCount(course.getRatingCount());
        vo.setChapters(catalog.getChapters());

        if (course.getTeacherId() != null) {
            User teacher = userService.getById(course.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getUserName());
                vo.setTeacherAvatar(teacher.getUserAvatar());
            }
        }
        return vo;
    }

    @Override
    public List<ExerciseQuestionVO> listPublicExercisesBySection(Long sectionId) {
        List<Question> questions = getSectionQuestions(sectionId);
        List<ExerciseQuestionVO> result = new ArrayList<>();
        for (Question q : questions) {
            ExerciseQuestionVO vo = new ExerciseQuestionVO();
            vo.setId(q.getId());
            vo.setSectionId(q.getSectionId());
            vo.setType(q.getType());
            vo.setContent(q.getContent());
            vo.setOptions(q.getOptions());
            vo.setScore(q.getScore());
            vo.setDifficulty(q.getDifficulty());
            vo.setSortOrder(q.getSortOrder());
            result.add(vo);
        }
        return result;
    }

    @Override
    public Course getSectionContent(Long sectionId, Long userId) {
        Section section = sectionMapper.selectById(sectionId);
        if (section == null || isDeleted(section.getIsDelete())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "section not found");
        }

        CourseChapter chapter = courseChapterMapper.selectById(section.getChapterId());
        if (chapter == null || isDeleted(chapter.getIsDelete())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "chapter not found");
        }

        Long courseId = chapter.getCourseId();
        if (userId != null) {
            assertSectionLearnable(userId, sectionId);
            userProgressService.updateLearningStatus(userId, courseId, sectionId);
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null || isDeleted(course.getIsDelete())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "course not found");
        }
        return course;
    }

    @Override
    public List<Question> getSectionQuestions(Long sectionId) {
        LambdaQueryWrapper<Question> query = new LambdaQueryWrapper<>();
        query.eq(Question::getSectionId, sectionId)
                .eq(Question::getIsDelete, 0)
                .orderByAsc(Question::getSortOrder);
        return questionMapper.selectList(query);
    }

    /**
     * 提交小节练习答案并返回自动判题结果。
     *
     * @param request 答题提交请求
     * @return 判题结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmitAnswerResponse submitAnswer(SubmitAnswerRequest request) {
        // 提取用户、题目和答案参数，用户和题目缺失时无法判题
        Long userId = request.getUserId();
        Long questionId = request.getQuestionId();
        String userAnswer = request.getAnswer();

        if (userId == null || questionId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Please complete the previous section before continuing");
        }

        // 使用 Redis 短锁防止同一用户短时间重复提交同一题
        String submitKey = USER_SUBMIT_KEY + userId + ":" + questionId;
        boolean redisLockAcquired = false;
        try {
            Boolean lock = redisTemplate.opsForValue().setIfAbsent(submitKey, true, 30, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(lock)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "duplicate submission");
            }
            redisLockAcquired = Boolean.TRUE.equals(lock);
        } catch (BusinessException businessException) {
            throw businessException;
        } catch (Exception redisException) {
            log.warn("redis lock failed, continue without lock. user_id={}, questionId={}", userId, questionId, redisException);
        }

        try {
            // 数据库层再次检查是否已经提交，避免绕过 Redis 锁造成重复答题
            LambdaQueryWrapper<UserAnswer> existsQuery = new LambdaQueryWrapper<>();
            existsQuery.eq(UserAnswer::getUserId, userId)
                    .eq(UserAnswer::getQuestionId, questionId)
                    .last("LIMIT 1");
            if (userAnswerMapper.selectOne(existsQuery) != null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "question already submitted");
            }

            // 查询题目并校验题目所属小节是否满足学习顺序要求
            Question question = questionMapper.selectById(questionId);
            if (question == null || isDeleted(question.getIsDelete())) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "question not found");
            }

            assertSectionLearnable(userId, question.getSectionId());

            // 根据题型执行自动判题，并按题目分值计算得分
            boolean isCorrect = judgeAnswer(question, userAnswer);
            int score = isCorrect ? safeInt(question.getScore()) : 0;

            // 保存用户答题记录，包含答案、是否正确、得分和耗时
            UserAnswer userAnswerEntity = new UserAnswer();
            userAnswerEntity.setUserId(userId);
            userAnswerEntity.setQuestionId(questionId);
            userAnswerEntity.setUserAnswer(userAnswer);
            userAnswerEntity.setIsCorrect(isCorrect ? 1 : 0);
            userAnswerEntity.setScore(score);
            userAnswerEntity.setTimeSpent(request.getTimeSpent());
            userAnswerMapper.insert(userAnswerEntity);
            // 答题后尝试完成当前小节，客观题全部完成且条件满足时会刷新学习进度
            tryCompleteSection(userId, question.getSectionId());

            // 返回前端判题结果、正确答案、解析和本题得分
            SubmitAnswerResponse response = new SubmitAnswerResponse();
            response.setCorrect(isCorrect);
            response.setCorrectAnswer(question.getAnswer());
            response.setExplanation(question.getExplanation());
            response.setScore(score);
            return response;
        } finally {
            if (redisLockAcquired) {
                try {
                    // 释放 Redis 提交锁，避免影响后续正常提交
                    redisTemplate.delete(submitKey);
                } catch (Exception e) {
                    log.warn("release submit lock failed, submitKey={}", submitKey, e);
                }
            }
        }
    }

    @Override
    public CourseProgressVO getCourseProgress(Long courseId, Long userId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || isDeleted(course.getIsDelete())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "course not found");
        }

        int totalSections = safeInt(course.getTotalSections());
        LambdaQueryWrapper<UserProgress> query = new LambdaQueryWrapper<>();
        query.eq(UserProgress::getUserId, userId)
                .eq(UserProgress::getCourseId, courseId)
                .eq(UserProgress::getStatus, 2);
        Long completedCount = userProgressMapper.selectCount(query);

        double progress = totalSections > 0 ? (double) completedCount / totalSections * 100 : 0;

        CourseProgressVO progressVO = new CourseProgressVO();
        progressVO.setCourseId(courseId);
        progressVO.setCompletedSections(completedCount.intValue());
        progressVO.setTotalSections(totalSections);
        progressVO.setProgress((int) Math.round(progress));
        return progressVO;
    }

    /**
     * 尝试自动完成指定小节学习。
     *
     * @param userId 用户 ID
     * @param sectionId 小节 ID
     * @return 是否已完成或成功完成
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean tryCompleteSection(Long userId, Long sectionId) {
        // 自动完成场景不会放宽空内容小节，需要由内部逻辑判断完成条件
        return tryCompleteSectionInternal(userId, sectionId, false);
    }

    private boolean tryCompleteSectionInternal(Long userId, Long sectionId, boolean explicitCompleteAction) {
        if (userId == null || sectionId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Please complete the previous section before continuing");
        }
        SectionCompletionCheckResult checkResult = evaluateSectionCompletion(userId, sectionId, explicitCompleteAction);
        if (checkResult.isCompleted()) {
            return true;
        }
        if (!checkResult.isAllowed()) {
            return false;
        }
        markSectionCompleted(userId, checkResult.getSection(), checkResult.getChapter());
        return true;
    }

    private SectionCompletionCheckResult evaluateSectionCompletion(Long userId, Long sectionId, boolean explicitCompleteAction) {
        assertSectionLearnable(userId, sectionId);

        Section section = sectionMapper.selectById(sectionId);
        if (section == null || isDeleted(section.getIsDelete())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "section not found");
        }
        CourseChapter chapter = courseChapterMapper.selectById(section.getChapterId());
        if (chapter == null || isDeleted(chapter.getIsDelete())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "chapter not found");
        }

        userProgressService.updateLearningStatus(userId, chapter.getCourseId(), sectionId);
        if (isPersistedSectionCompleted(userId, sectionId)) {
            return SectionCompletionCheckResult.completed(section, chapter);
        }

        List<Question> questions = getSectionQuestions(sectionId);
        boolean requiresVideo = requiresVideoCompletion(section);
        boolean hasQuestions = !questions.isEmpty();
        boolean videoCompleted = !requiresVideo || isVideoRequirementSatisfied(userId, section);

        List<Long> objectiveQuestionIds = questions.stream()
                .filter(this::isObjectiveQuestion)
                .map(Question::getId)
                .collect(Collectors.toList());
        List<Long> subjectiveQuestionIds = questions.stream()
                .filter(question -> !isObjectiveQuestion(question))
                .map(Question::getId)
                .collect(Collectors.toList());

        boolean objectiveCompleted = objectiveQuestionIds.isEmpty()
                || countAnsweredObjectiveQuestions(userId, objectiveQuestionIds) >= objectiveQuestionIds.size();
        boolean subjectiveCompleted = subjectiveQuestionIds.isEmpty()
                || countSubmittedSubjectiveQuestions(userId, subjectiveQuestionIds) >= subjectiveQuestionIds.size();

        if (!requiresVideo && !hasQuestions) {
            if (explicitCompleteAction) {
                return SectionCompletionCheckResult.allowed(section, chapter);
            }
            return SectionCompletionCheckResult.blocked(section, chapter,
                    "this section requires an explicit completion action");
        }

        List<String> unmetRequirements = new ArrayList<>();
        if (!videoCompleted) {
            unmetRequirements.add("video progress has not reached 90%");
        }
        if (!objectiveCompleted) {
            unmetRequirements.add("please finish all objective questions");
        }
        if (!subjectiveCompleted) {
            unmetRequirements.add("please submit all subjective homework");
        }
        if (!unmetRequirements.isEmpty()) {
            return SectionCompletionCheckResult.blocked(section, chapter, String.join("; ", unmetRequirements));
        }
        return SectionCompletionCheckResult.allowed(section, chapter);
    }

    private void markSectionCompleted(Long userId, Section section, CourseChapter chapter) {
        LambdaQueryWrapper<UserProgress> query = new LambdaQueryWrapper<>();
        query.eq(UserProgress::getUserId, userId)
                .eq(UserProgress::getSectionId, section.getId())
                .last("LIMIT 1");
        UserProgress progress = userProgressMapper.selectOne(query);

        if (progress == null) {
            progress = new UserProgress();
            progress.setUserId(userId);
            progress.setCourseId(chapter.getCourseId());
            progress.setSectionId(section.getId());
            progress.setStatus(2);
            progress.setLearnedTime(0);
            progress.setLastLearnTime(new Date());
            userProgressMapper.insert(progress);
        } else {
            progress.setStatus(2);
            progress.setLastLearnTime(new Date());
            userProgressMapper.updateById(progress);
        }

        refreshUserCourseProgress(userId, chapter.getCourseId());

        String cacheKey = COURSE_CATALOG_CACHE_KEY + chapter.getCourseId();
        try {
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.warn("delete course catalog cache failed, course_id={}", chapter.getCourseId(), e);
        }
    }

    private boolean requiresVideoCompletion(Section section) {
        if (section == null) {
            return false;
        }
        String type = normalizeLower(section.getType());
        String contentType = normalizeLower(section.getContentType());
        return "video".equals(type) || "video".equals(contentType) || !isBlank(section.getVideoUrl());
    }

    private boolean isVideoRequirementSatisfied(Long userId, Section section) {
        LambdaQueryWrapper<VideoLearningRecord> query = new LambdaQueryWrapper<>();
        query.eq(VideoLearningRecord::getUserId, userId)
                .eq(VideoLearningRecord::getSectionId, section.getId())
                .last("LIMIT 1");
        VideoLearningRecord record = videoLearningRecordMapper.selectOne(query);
        if (record == null) {
            return false;
        }
        if (record.getIsCompleted() != null && record.getIsCompleted() == 1) {
            return true;
        }
        BigDecimal progress = record.getProgress();
        if (progress == null && section.getVideoDuration() != null && section.getVideoDuration() > 0) {
            int lastWatchPosition = record.getLastWatchPosition() == null ? 0 : record.getLastWatchPosition();
            progress = BigDecimal.valueOf(lastWatchPosition)
                    .divide(BigDecimal.valueOf(section.getVideoDuration()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        return progress != null && progress.compareTo(VIDEO_COMPLETION_THRESHOLD) >= 0;
    }

    private int countAnsweredObjectiveQuestions(Long userId, List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return 0;
        }
        LambdaQueryWrapper<UserAnswer> query = new LambdaQueryWrapper<>();
        query.eq(UserAnswer::getUserId, userId)
                .in(UserAnswer::getQuestionId, questionIds);
        List<UserAnswer> answers = userAnswerMapper.selectList(query);
        return (int) answers.stream()
                .map(UserAnswer::getQuestionId)
                .filter(id -> id != null)
                .distinct()
                .count();
    }

    private int countSubmittedSubjectiveQuestions(Long userId, List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return 0;
        }
        LambdaQueryWrapper<HomeworkSubmission> query = new LambdaQueryWrapper<>();
        query.eq(HomeworkSubmission::getUserId, userId)
                .in(HomeworkSubmission::getQuestionId, questionIds);
        List<HomeworkSubmission> submissions = homeworkSubmissionMapper.selectList(query);
        return (int) submissions.stream()
                .map(HomeworkSubmission::getQuestionId)
                .filter(id -> id != null)
                .distinct()
                .count();
    }

    private boolean isObjectiveQuestion(Question question) {
        return question != null && OBJECTIVE_QUESTION_TYPES.contains(normalizeLower(question.getType()));
    }

    private boolean isPersistedSectionCompleted(Long userId, Long sectionId) {
        LambdaQueryWrapper<UserProgress> progressQuery = new LambdaQueryWrapper<>();
        progressQuery.eq(UserProgress::getUserId, userId)
                .eq(UserProgress::getSectionId, sectionId)
                .eq(UserProgress::getStatus, 2)
                .last("LIMIT 1");
        return userProgressMapper.selectOne(progressQuery) != null;
    }

    private String normalizeLower(String value) {
        return isBlank(value) ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean updateCourseRating(Long courseId, BigDecimal score, int count) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || count <= 0) {
            return false;
        }

        BigDecimal currentScore = course.getRatingScore() == null ? BigDecimal.ZERO : course.getRatingScore();
        int currentCount = course.getRatingCount() == null ? 0 : course.getRatingCount();

        BigDecimal totalScore = currentScore.multiply(BigDecimal.valueOf(currentCount)).add(score == null ? BigDecimal.ZERO : score);
        BigDecimal newAvgScore = totalScore.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);

        UpdateWrapper<Course> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", courseId);
        updateWrapper.set("rating_score", newAvgScore);
        updateWrapper.set("rating_count", count);
        return this.update(updateWrapper);
    }

    @Override
    public List<CourseVO> getCoursesByTeacher(Long teacherId, User currentUser) {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getTeacherId, teacherId)
                .eq(Course::getStatus, 1)
                .eq(Course::getIsDelete, 0)
                .orderByDesc(Course::getCreateTime);

        List<Course> courseList = this.list(queryWrapper);
        return courseList.stream().map(this::toCourseVO).collect(Collectors.toList());
    }

    @Override
    public Page<CourseVO> getCourseListByPage(Long current, Long pageSize) {
        Page<Course> coursePage = new Page<>(current, pageSize);
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getStatus, 1)
                .eq(Course::getIsDelete, 0)
                .orderByDesc(Course::getCreateTime);

        Page<Course> pageResult = this.page(coursePage, queryWrapper);
        Page<CourseVO> voPage = new Page<>(current, pageSize, pageResult.getTotal());
        voPage.setRecords(pageResult.getRecords().stream().map(this::toCourseVO).collect(Collectors.toList()));
        return voPage;
    }

    /**
     * 创建课程并补齐课程默认配置。
     *
     * @param courseAddRequest 课程新增请求
     * @param loginUser 当前登录用户
     * @return 新建课程 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCourse(CourseAddRequest courseAddRequest, User loginUser) {
        // 创建课程必须有登录用户，讲师只能创建归属自己的课程
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 根据角色确定课程讲师归属，非管理员和非讲师不能创建课程
        String role = loginUser.getUserRole();
        if (UserConstant.TEACHER_ROLE.equals(role)) {
            courseAddRequest.setTeacherId(loginUser.getId());
        } else if (!UserConstant.ADMIN_ROLE.equals(role)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        if (courseAddRequest.getTeacherId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Please complete the previous section before continuing");
        }

        // 将新增请求复制为课程实体，并兼容封面、分类和 AI 知识库字段
        Course course = new Course();
        BeanUtils.copyProperties(courseAddRequest, course);
        course.setAiKnowleage(courseAddRequest.getAiKnowleage().trim());
        if (isBlank(course.getCoverUrl()) && !isBlank(courseAddRequest.getCoverImage())) {
            course.setCoverUrl(courseAddRequest.getCoverImage());
        }
        if (isBlank(course.getCategory()) && courseAddRequest.getCategoryId() != null) {
            course.setCategory(String.valueOf(courseAddRequest.getCategoryId()));
        }

        // 补齐课程状态、评分、统计数量、价格和难度等默认值
        if (course.getStatus() == null) {
            course.setStatus(1);
        }
        if (course.getRatingScore() == null) {
            course.setRatingScore(BigDecimal.ZERO);
        }
        if (course.getRatingCount() == null) {
            course.setRatingCount(0);
        }
        if (course.getStudentCount() == null) {
            course.setStudentCount(0);
        }
        if (course.getBuyCount() == null) {
            course.setBuyCount(0);
        }
        if (course.getTotalSections() == null) {
            course.setTotalSections(0);
        }
        if (course.getTotalChapters() == null) {
            course.setTotalChapters(0);
        }
        if (course.getTotalDuration() == null) {
            course.setTotalDuration(0);
        }
        if (course.getTotalQuestions() == null) {
            course.setTotalQuestions(0);
        }
        if (course.getPrice() == null) {
            course.setPrice(BigDecimal.ZERO);
        }
        if (course.getOriginalPrice() == null) {
            course.setOriginalPrice(course.getPrice());
        }
        if (course.getCourseType() == null) {
            course.setCourseType(1);
        }
        if (course.getDifficulty() == null) {
            course.setDifficulty(1);
        }

        // 保存课程实体，失败时抛出业务异常并回滚事务
        if (!this.save(course)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "create course failed");
        }

        log.info("course created, operator={}, course_id={}", loginUser.getId(), course.getId());
        return course.getId();
    }

    /**
     * 校验当前用户是否可以管理指定课程。
     *
     * @param loginUser 当前登录用户
     * @param courseId 课程 ID
     */
    @Override
    public void assertCanManageCourse(User loginUser, Long courseId) {
        // 用户和课程 id 都必须存在，否则无法判断权限
        if (loginUser == null || courseId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 查询课程并排除已删除课程
        Course course = courseMapper.selectById(courseId);
        if (course == null || isDeleted(course.getIsDelete())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "course not found");
        }

        // 管理员可管理全部课程，讲师只能管理自己负责的课程
        String role = loginUser.getUserRole();
        if (UserConstant.ADMIN_ROLE.equals(role)) {
            return;
        }
        if (UserConstant.TEACHER_ROLE.equals(role) && loginUser.getId().equals(course.getTeacherId())) {
            return;
        }
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no permission to manage course");
    }

    /**
     * 校验用户是否可以学习指定小节。
     *
     * @param userId 用户 ID
     * @param sectionId 小节 ID
     */
    @Override
    public void assertSectionLearnable(Long userId, Long sectionId) {
        // 未传用户或小节时跳过顺序校验，兼容匿名或非学习状态查询场景
        if (userId == null || sectionId == null) {
            return;
        }
        // 查询当前小节及所属章节，确保小节属于有效课程结构
        Section currentSection = sectionMapper.selectById(sectionId);
        if (currentSection == null || isDeleted(currentSection.getIsDelete())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "section not found");
        }
        CourseChapter currentChapter = courseChapterMapper.selectById(currentSection.getChapterId());
        if (currentChapter == null || isDeleted(currentChapter.getIsDelete())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "chapter not found");
        }

        // 按章节排序和小节排序拼出课程完整学习顺序
        LambdaQueryWrapper<CourseChapter> chapterQuery = new LambdaQueryWrapper<>();
        chapterQuery.eq(CourseChapter::getCourseId, currentChapter.getCourseId())
                .eq(CourseChapter::getIsDelete, 0)
                .orderByAsc(CourseChapter::getSort)
                .orderByAsc(CourseChapter::getId);
        List<CourseChapter> chapterList = courseChapterMapper.selectList(chapterQuery);
        if (chapterList.isEmpty()) {
            return;
        }

        List<Long> orderedSectionIds = new ArrayList<>();
        for (CourseChapter chapter : chapterList) {
            LambdaQueryWrapper<Section> sectionQuery = new LambdaQueryWrapper<>();
            sectionQuery.eq(Section::getChapterId, chapter.getId())
                    .eq(Section::getIsDelete, 0)
                    .orderByAsc(Section::getSortOrder)
                    .orderByAsc(Section::getId);
            List<Section> sections = sectionMapper.selectList(sectionQuery);
            for (Section section : sections) {
                orderedSectionIds.add(section.getId());
            }
        }
        if (orderedSectionIds.isEmpty()) {
            return;
        }

        // 第一个小节可直接学习，其余小节必须先完成前一个小节
        int currentIndex = orderedSectionIds.indexOf(sectionId);
        if (currentIndex < 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "section not found in course");
        }
        if (currentIndex == 0) {
            return;
        }

        Long previousSectionId = orderedSectionIds.get(currentIndex - 1);
        if (!isSectionCompletedForUser(userId, previousSectionId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Please complete the previous section before continuing");
        }
    }

    /**
     * 根据用户学习历史推荐课程。
     *
     * @param userId 用户 ID，可为空
     * @param limit 推荐数量上限
     * @return 推荐课程列表
     */
    @Override
    public List<CourseVO> recommendCourses(Long userId, Integer limit) {
        // 归一化推荐数量，限制单次最多返回 50 门课程
        int recommendLimit = normalizeRecommendLimit(limit);
        Set<Long> learnedCourseIds = new HashSet<>();

        // 登录用户优先收集学习、评分和视频观看涉及过的课程作为历史偏好
        if (userId != null) {
            LambdaQueryWrapper<UserCourse> userCourseQuery = new LambdaQueryWrapper<>();
            userCourseQuery.eq(UserCourse::getUserId, userId)
                    .orderByDesc(UserCourse::getLastLearnTime)
                    .orderByDesc(UserCourse::getUpdateTime);
            List<UserCourse> userCourseList = userCourseMapper.selectList(userCourseQuery);
            for (UserCourse userCourse : userCourseList) {
                if (userCourse.getCourseId() != null) {
                    learnedCourseIds.add(userCourse.getCourseId());
                }
            }

            LambdaQueryWrapper<CourseRating> ratingQuery = new LambdaQueryWrapper<>();
            ratingQuery.eq(CourseRating::getUserId, userId)
                    .eq(CourseRating::getIsDelete, 0);
            List<CourseRating> ratingList = courseRatingMapper.selectList(ratingQuery);
            for (CourseRating rating : ratingList) {
                if (rating.getCourseId() != null) {
                    learnedCourseIds.add(rating.getCourseId());
                }
            }

            LambdaQueryWrapper<VideoLearningRecord> recordQuery = new LambdaQueryWrapper<>();
            recordQuery.eq(VideoLearningRecord::getUserId, userId)
                    .orderByDesc(VideoLearningRecord::getUpdateTime);
            List<VideoLearningRecord> recordList = videoLearningRecordMapper.selectList(recordQuery);
            Set<Long> sectionIds = recordList.stream()
                    .map(VideoLearningRecord::getSectionId)
                    .filter(id -> id != null)
                    .collect(Collectors.toSet());
            if (!sectionIds.isEmpty()) {
                List<Section> sections = sectionMapper.selectBatchIds(sectionIds);
                Set<Long> chapterIds = sections.stream()
                        .map(Section::getChapterId)
                        .filter(id -> id != null)
                        .collect(Collectors.toSet());
                if (!chapterIds.isEmpty()) {
                    List<CourseChapter> chapterList = courseChapterMapper.selectBatchIds(chapterIds);
                    for (CourseChapter chapter : chapterList) {
                        if (chapter != null && chapter.getCourseId() != null) {
                            learnedCourseIds.add(chapter.getCourseId());
                        }
                    }
                }
            }
        }

        // 没有历史偏好时，直接返回热门已发布课程
        if (learnedCourseIds.isEmpty()) {
            return listHotPublishedCourses(recommendLimit, Collections.emptySet())
                    .stream().map(this::toCourseVO).collect(Collectors.toList());
        }

        // 从历史课程中提取分类、难度、课程类型和标签偏好
        List<Course> historyCourses = courseMapper.selectBatchIds(learnedCourseIds);
        Set<Long> preferredCategoryIds = new HashSet<>();
        Map<Integer, Integer> difficultyCounter = new HashMap<>();
        Map<Integer, Integer> typeCounter = new HashMap<>();
        Set<String> preferredTags = new HashSet<>();

        for (Course historyCourse : historyCourses) {
            if (historyCourse == null || isDeleted(historyCourse.getIsDelete())) {
                continue;
            }
            if (historyCourse.getCategoryId() != null) {
                preferredCategoryIds.add(historyCourse.getCategoryId());
            }
            if (historyCourse.getDifficulty() != null) {
                difficultyCounter.merge(historyCourse.getDifficulty(), 1, Integer::sum);
            }
            if (historyCourse.getCourseType() != null) {
                typeCounter.merge(historyCourse.getCourseType(), 1, Integer::sum);
            }
            preferredTags.addAll(parseTagSet(historyCourse.getTags()));
        }

        Integer preferredDifficulty = findTopPreference(difficultyCounter);
        Integer preferredType = findTopPreference(typeCounter);

        // 查询未学习过的已发布课程作为候选集，先按热度和评分粗排
        LambdaQueryWrapper<Course> candidateQuery = new LambdaQueryWrapper<>();
        candidateQuery.eq(Course::getStatus, 1)
                .eq(Course::getIsDelete, 0)
                .notIn(!learnedCourseIds.isEmpty(), Course::getId, learnedCourseIds)
                .orderByDesc(Course::getViewCount)
                .orderByDesc(Course::getStudentCount)
                .orderByDesc(Course::getRatingScore)
                .orderByDesc(Course::getRatingCount)
                .orderByDesc(Course::getCreateTime);
        List<Course> candidates = this.page(new Page<>(1, 200), candidateQuery).getRecords();

        // 候选集为空时回退到热门课程推荐
        if (candidates.isEmpty()) {
            return listHotPublishedCourses(recommendLimit, learnedCourseIds)
                    .stream().map(this::toCourseVO).collect(Collectors.toList());
        }

        // 根据热度、评分、分类、难度、类型和标签重合度计算推荐分
        Map<Long, Double> scoreMap = new HashMap<>();
        for (Course candidate : candidates) {
            double score = 0.0D;
            score += safeInt(candidate.getViewCount()) * 0.08D;
            score += safeInt(candidate.getStudentCount()) * 0.05D;
            score += safeInt(candidate.getRatingCount()) * 0.02D;
            score += (candidate.getRatingScore() == null ? BigDecimal.ZERO : candidate.getRatingScore()).doubleValue() * 2.0D;

            if (candidate.getCategoryId() != null && preferredCategoryIds.contains(candidate.getCategoryId())) {
                score += 3.0D;
            }
            if (preferredDifficulty != null && preferredDifficulty.equals(candidate.getDifficulty())) {
                score += 1.5D;
            }
            if (preferredType != null && preferredType.equals(candidate.getCourseType())) {
                score += 1.0D;
            }
            if (!preferredTags.isEmpty()) {
                Set<String> tags = parseTagSet(candidate.getTags());
                int overlap = 0;
                for (String tag : tags) {
                    if (preferredTags.contains(tag)) {
                        overlap++;
                    }
                }
                score += Math.min(overlap, 4) * 0.6D;
            }
            scoreMap.put(candidate.getId(), score);
        }

        // 按推荐分从高到低排序，选取指定数量课程
        candidates.sort((a, b) -> Double.compare(
                scoreMap.getOrDefault(b.getId(), 0.0D),
                scoreMap.getOrDefault(a.getId(), 0.0D)
        ));

        LinkedHashSet<Long> selectedCourseIds = new LinkedHashSet<>();
        for (Course candidate : candidates) {
            selectedCourseIds.add(candidate.getId());
            if (selectedCourseIds.size() >= recommendLimit) {
                break;
            }
        }

        // 推荐数量不足时，用热门课程补齐，并避免与已学习和已选课程重复
        Set<Long> fallbackExclude = new HashSet<>(learnedCourseIds);
        fallbackExclude.addAll(selectedCourseIds);
        if (selectedCourseIds.size() < recommendLimit) {
            List<Course> hotCourses = listHotPublishedCourses(recommendLimit * 2, fallbackExclude);
            for (Course hotCourse : hotCourses) {
                selectedCourseIds.add(hotCourse.getId());
                if (selectedCourseIds.size() >= recommendLimit) {
                    break;
                }
            }
        }

        // 将最终课程 id 映射回课程实体，并转换为前端展示 VO
        Map<Long, Course> courseMap = new HashMap<>();
        for (Course course : candidates) {
            courseMap.put(course.getId(), course);
        }
        if (selectedCourseIds.size() > candidates.size()) {
            List<Course> supplement = listHotPublishedCourses(recommendLimit * 2, learnedCourseIds);
            for (Course course : supplement) {
                courseMap.putIfAbsent(course.getId(), course);
            }
        }

        List<CourseVO> result = new ArrayList<>();
        for (Long courseId : selectedCourseIds) {
            Course course = courseMap.get(courseId);
            if (course != null) {
                result.add(toCourseVO(course));
            }
            if (result.size() >= recommendLimit) {
                break;
            }
        }
        return result;
    }

    @Override
    public List<CourseVO> listHotRecommendCourses(Integer limit) {
        int recommendLimit = normalizeRecommendLimit(limit);
        return courseMapper.selectHotRecommendCourses(recommendLimit)
                .stream()
                .map(this::toCourseVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordCourseView(Long courseId, Long userId) {
        if (courseId == null) {
            return;
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null || isDeleted(course.getIsDelete()) || !Integer.valueOf(1).equals(course.getStatus())) {
            return;
        }

        UpdateWrapper<Course> courseUpdateWrapper = new UpdateWrapper<>();
        courseUpdateWrapper.eq("id", courseId)
                .eq("status", 1)
                .eq("is_delete", 0)
                .setSql("view_count = view_count + 1");
        courseMapper.update(null, courseUpdateWrapper);

        if (userId == null) {
            return;
        }

        LambdaQueryWrapper<CourseViewRecord> query = new LambdaQueryWrapper<>();
        query.eq(CourseViewRecord::getCourseId, courseId)
                .eq(CourseViewRecord::getUserId, userId)
                .last("LIMIT 1");
        CourseViewRecord record = courseViewRecordMapper.selectOne(query);
        Date now = new Date();
        if (record == null) {
            record = new CourseViewRecord();
            record.setCourseId(courseId);
            record.setUserId(userId);
            record.setCreateTime(now);
            record.setUpdateTime(now);
            record.setIsDelete(0);
            try {
                courseViewRecordMapper.insert(record);
            } catch (DuplicateKeyException e) {
                updateCourseViewRecordTime(courseId, userId, now);
            }
            return;
        }

        record.setUpdateTime(now);
        record.setIsDelete(0);
        courseViewRecordMapper.updateById(record);
    }

    /**
     * 记录用户开始学习指定课程小节。
     *
     * @param userId 用户 ID
     * @param courseId 课程 ID
     * @param sectionId 小节 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startSectionLearning(Long userId, Long courseId, Long sectionId) {
        // 校验用户、课程和小节参数，开始学习必须明确三者关系
        if (userId == null || courseId == null || sectionId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Please complete the previous section before continuing");
        }

        // 查询小节和章节，并确认小节确实属于传入课程
        Section section = sectionMapper.selectById(sectionId);
        if (section == null || isDeleted(section.getIsDelete())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "section not found");
        }
        CourseChapter chapter = courseChapterMapper.selectById(section.getChapterId());
        if (chapter == null || isDeleted(chapter.getIsDelete())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "chapter not found");
        }
        if (!courseId.equals(chapter.getCourseId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Please complete the previous section before continuing");
        }

        // 校验学习顺序，未完成前置小节时不允许开始后续小节
        assertSectionLearnable(userId, sectionId);

        // 查询用户课程关系，存在则刷新最后学习时间，不存在则创建学习关系
        LambdaQueryWrapper<UserCourse> userCourseQuery = new LambdaQueryWrapper<>();
        userCourseQuery.eq(UserCourse::getUserId, userId)
                .eq(UserCourse::getCourseId, courseId)
                .last("LIMIT 1");
        UserCourse userCourse = userCourseMapper.selectOne(userCourseQuery);
        Date now = new Date();
        if (userCourse == null) {
            userCourse = new UserCourse();
            userCourse.setUserId(userId);
            userCourse.setCourseId(courseId);
            userCourse.setStatus(0);
            userCourse.setProgress(BigDecimal.ZERO);
            userCourse.setStartTime(now);
            userCourse.setLastLearnTime(now);
            userCourseMapper.insert(userCourse);
        } else {
            userCourse.setLastLearnTime(now);
            if (userCourse.getStatus() == null || userCourse.getStatus() == 2) {
                userCourse.setStatus(0);
            }
            userCourseMapper.updateById(userCourse);
        }

        // 更新当前小节为学习中状态
        userProgressService.updateLearningStatus(userId, courseId, sectionId);
    }

    /**
     * 完成用户指定小节的学习。
     *
     * @param userId 用户 ID
     * @param sectionId 小节 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeSectionLearning(Long userId, Long sectionId) {
        // 校验用户和小节参数，完成学习必须明确当前用户和目标小节
        if (userId == null || sectionId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Please complete the previous section before continuing");
        }
        // 显式完成时会检查视频进度、客观题答案和主观题提交情况
        SectionCompletionCheckResult checkResult = evaluateSectionCompletion(userId, sectionId, true);
        if (checkResult.isCompleted()) {
            return;
        }
        if (!checkResult.isAllowed()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, checkResult.getReason());
        }
        // 满足完成条件后标记小节完成，并刷新课程整体进度和目录缓存
        markSectionCompleted(userId, checkResult.getSection(), checkResult.getChapter());
    }

    private void refreshUserCourseProgress(Long userId, Long courseId) {
        if (userId == null || courseId == null) {
            return;
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null || isDeleted(course.getIsDelete())) {
            return;
        }

        Integer configuredTotalSections = course.getTotalSections();
        int totalSections = configuredTotalSections == null ? 0 : configuredTotalSections;
        if (totalSections <= 0) {
            LambdaQueryWrapper<CourseChapter> chapterQuery = new LambdaQueryWrapper<>();
            chapterQuery.eq(CourseChapter::getCourseId, courseId)
                    .eq(CourseChapter::getIsDelete, 0);
            List<CourseChapter> chapterList = courseChapterMapper.selectList(chapterQuery);
            if (!chapterList.isEmpty()) {
                List<Long> chapterIds = chapterList.stream().map(CourseChapter::getId).collect(Collectors.toList());
                LambdaQueryWrapper<Section> sectionQuery = new LambdaQueryWrapper<>();
                sectionQuery.in(Section::getChapterId, chapterIds)
                        .eq(Section::getIsDelete, 0);
                totalSections = sectionMapper.selectCount(sectionQuery).intValue();
            }
        }

        LambdaQueryWrapper<UserProgress> completedQuery = new LambdaQueryWrapper<>();
        completedQuery.eq(UserProgress::getUserId, userId)
                .eq(UserProgress::getCourseId, courseId)
                .eq(UserProgress::getStatus, 2);
        long completedCount = userProgressMapper.selectCount(completedQuery);

        BigDecimal progressPercent;
        if (totalSections <= 0) {
            progressPercent = BigDecimal.ZERO;
        } else {
            progressPercent = BigDecimal.valueOf(completedCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalSections), 2, RoundingMode.HALF_UP)
                    .min(BigDecimal.valueOf(100));
        }

        LambdaQueryWrapper<UserCourse> relationQuery = new LambdaQueryWrapper<>();
        relationQuery.eq(UserCourse::getUserId, userId)
                .eq(UserCourse::getCourseId, courseId)
                .last("LIMIT 1");
        UserCourse relation = userCourseMapper.selectOne(relationQuery);
        Date now = new Date();

        int status = (totalSections > 0 && completedCount >= totalSections) ? 1 : 0;
        Date completeTime = status == 1 ? now : null;

        if (relation == null) {
            relation = new UserCourse();
            relation.setUserId(userId);
            relation.setCourseId(courseId);
            relation.setStatus(status);
            relation.setProgress(progressPercent);
            relation.setStartTime(now);
            relation.setCompleteTime(completeTime);
            relation.setLastLearnTime(now);
            userCourseMapper.insert(relation);
            return;
        }

        relation.setStatus(status);
        relation.setProgress(progressPercent);
        relation.setLastLearnTime(now);
        relation.setCompleteTime(completeTime);
        userCourseMapper.updateById(relation);
    }

    private List<Course> listHotPublishedCourses(int limit, Set<Long> excludeCourseIds) {
        LambdaQueryWrapper<Course> query = new LambdaQueryWrapper<>();
        query.eq(Course::getStatus, 1)
                .eq(Course::getIsDelete, 0)
                .notIn(excludeCourseIds != null && !excludeCourseIds.isEmpty(), Course::getId, excludeCourseIds)
                .orderByDesc(Course::getViewCount)
                .orderByDesc(Course::getStudentCount)
                .orderByDesc(Course::getRatingScore)
                .orderByDesc(Course::getRatingCount)
                .orderByDesc(Course::getCreateTime);
        return this.page(new Page<>(1, limit), query).getRecords();
    }

    private void updateCourseViewRecordTime(Long courseId, Long userId, Date updateTime) {
        UpdateWrapper<CourseViewRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("course_id", courseId)
                .eq("user_id", userId)
                .set("update_time", updateTime)
                .set("is_delete", 0);
        courseViewRecordMapper.update(null, updateWrapper);
    }

    private Integer findTopPreference(Map<Integer, Integer> counter) {
        Integer target = null;
        int max = 0;
        for (Map.Entry<Integer, Integer> entry : counter.entrySet()) {
            if (entry.getValue() != null && entry.getValue() > max) {
                max = entry.getValue();
                target = entry.getKey();
            }
        }
        return target;
    }

    private Set<String> parseTagSet(String tags) {
        if (isBlank(tags)) {
            return Collections.emptySet();
        }
        String normalized = tags.replace("[", "")
                .replace("]", "")
                .replace("\"", "");
        Set<String> result = new HashSet<>();
        String[] parts = normalized.split("[,\\uFF0C;|\\s]+");
        for (String part : parts) {
            if (!isBlank(part)) {
                result.add(part.trim().toLowerCase());
            }
        }
        return result;
    }

    private String resolveSectionVideoUrl(Section section) {
        if (section == null) {
            return null;
        }
        String url = !isBlank(section.getVideoUrl()) ? section.getVideoUrl() : section.getResourceUrl();
        if (isBlank(url)) {
            return url;
        }
        String lower = url.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("/api/")) {
            return url;
        }
        if (url.startsWith("/upload/")) {
            return "/api" + url;
        }
        return url;
    }

    private boolean isSectionCompletedForUser(Long userId, Long sectionId) {
        return isPersistedSectionCompleted(userId, sectionId);
    }

    private static final class SectionCompletionCheckResult {
        private final Section section;
        private final CourseChapter chapter;
        private final boolean allowed;
        private final boolean completed;
        private final String reason;

        private SectionCompletionCheckResult(Section section, CourseChapter chapter,
                                             boolean allowed, boolean completed, String reason) {
            this.section = section;
            this.chapter = chapter;
            this.allowed = allowed;
            this.completed = completed;
            this.reason = reason;
        }

        private static SectionCompletionCheckResult completed(Section section, CourseChapter chapter) {
            return new SectionCompletionCheckResult(section, chapter, true, true, null);
        }

        private static SectionCompletionCheckResult allowed(Section section, CourseChapter chapter) {
            return new SectionCompletionCheckResult(section, chapter, true, false, null);
        }

        private static SectionCompletionCheckResult blocked(Section section, CourseChapter chapter, String reason) {
            return new SectionCompletionCheckResult(section, chapter, false, false, reason);
        }

        private Section getSection() {
            return section;
        }

        private CourseChapter getChapter() {
            return chapter;
        }

        private boolean isAllowed() {
            return allowed;
        }

        private boolean isCompleted() {
            return completed;
        }

        private String getReason() {
            return reason;
        }
    }

    private int normalizeRecommendLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 10;
        }
        return Math.min(limit, 50);
    }


    /**
     * 上传课程视频文件并返回访问地址。
     *
     * @param file 视频文件
     * @param loginUser 当前登录用户
     * @return 视频访问地址
     */
    @Override
    public String uploadVideo(MultipartFile file, User loginUser) {
        // 校验文件不能为空，视频上传必须包含实际文件内容
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Please complete the previous section before continuing");
        }

        // 校验文件扩展名，只允许常见视频格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().matches(".*\\.(mp4|webm|mov|flv|wmv)$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Please complete the previous section before continuing");
        }

        // 限制单个视频最大 500MB，避免占用过多本地存储
        long maxSize = 500L * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Please complete the previous section before continuing");
        }

        try {
            // 使用 UUID 生成文件名，避免不同用户上传同名视频互相覆盖
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String fileName = "video_" + uuid + extension;

            // 确保本地视频上传目录存在
            String uploadDir = System.getProperty("user.dir") + "/upload/video/";
            File dir = new File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "failed to create upload directory");
            }

            // 保存文件并返回前端可访问的接口路径
            String filePath = uploadDir + fileName;
            file.transferTo(new File(filePath));

            String accessUrl = "/api/upload/video/" + fileName;
            log.info("video uploaded, user_id={}, url={}", loginUser == null ? null : loginUser.getId(), accessUrl);
            return accessUrl;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("upload video failed", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "upload video failed: " + e.getMessage());
        }
    }

    private CourseVO toCourseVO(Course course) {
        CourseVO vo = new CourseVO();
        BeanUtils.copyProperties(course, vo);
        vo.setCoverImage(course.getCoverUrl());
        vo.setStudyCount(course.getStudentCount());
        vo.setViewCount(course.getViewCount());
        return vo;
    }

    private boolean judgeAnswer(Question question, String userAnswer) {
        if (question == null || userAnswer == null) {
            return false;
        }

        String correctAnswer = question.getAnswer();
        String questionType = question.getType();
        if (correctAnswer == null || questionType == null) {
            return false;
        }

        switch (questionType) {
            case "single":
            case "judge":
                return correctAnswer.trim().equals(userAnswer.trim());
            case "multiple":
                List<String> correctList = Arrays.stream(correctAnswer.split(","))
                        .map(String::trim)
                        .sorted()
                        .collect(Collectors.toList());
                List<String> userList = Arrays.stream(userAnswer.split(","))
                        .map(String::trim)
                        .sorted()
                        .collect(Collectors.toList());
                return correctList.equals(userList);
            default:
                return false;
        }
    }

    private boolean isDeleted(Integer isDelete) {
        return isDelete != null && isDelete != 0;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}














