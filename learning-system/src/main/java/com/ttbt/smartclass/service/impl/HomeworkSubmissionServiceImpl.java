package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.constant.UserConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.CourseChapterMapper;
import com.ttbt.smartclass.mapper.CourseMapper;
import com.ttbt.smartclass.mapper.HomeworkSubmissionMapper;
import com.ttbt.smartclass.mapper.SectionMapper;
import com.ttbt.smartclass.model.dto.HomeworkGradeRequest;
import com.ttbt.smartclass.model.dto.HomeworkSubmitRequest;
import com.ttbt.smartclass.model.entity.Course;
import com.ttbt.smartclass.model.entity.CourseChapter;
import com.ttbt.smartclass.model.entity.HomeworkSubmission;
import com.ttbt.smartclass.model.entity.Question;
import com.ttbt.smartclass.model.entity.Section;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.HomeworkSubmissionReviewDetailVO;
import com.ttbt.smartclass.model.vo.HomeworkSubmissionReviewPageVO;
import com.ttbt.smartclass.model.vo.HomeworkSubmissionVO;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.HomeworkSubmissionService;
import com.ttbt.smartclass.service.QuestionService;
import com.ttbt.smartclass.service.SectionService;
import com.ttbt.smartclass.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 作业提交服务实现类
 */
@Slf4j
@Service
public class HomeworkSubmissionServiceImpl extends ServiceImpl<HomeworkSubmissionMapper, HomeworkSubmission>
        implements HomeworkSubmissionService {

    @Resource
    private QuestionService questionService;

    @Resource
    private SectionService sectionService;

    @Resource
    private UserService userService;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private CourseChapterMapper courseChapterMapper;

    @Resource
    private SectionMapper sectionMapper;

    @Resource
    private CourseService courseService;

    /**
     * 提交或重新提交用户作业答案。
     *
     * @param request 作业提交请求
     * @param userId 用户 ID
     * @return 作业提交记录 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitHomework(HomeworkSubmitRequest request, Long userId) {
        // 校验小节存在，作业必须提交到有效课程小节下
        Section section = sectionService.getById(request.getSectionId());
        if (section == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "section not found");
        }

        // 校验题目存在且属于当前小节，防止跨小节提交答案
        Question question = questionService.getById(request.getQuestionId());
        if (question == null || !request.getSectionId().equals(question.getSectionId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "question not found in section");
        }
        // 校验用户是否满足学习顺序要求，未完成前置小节时不能提交作业
        courseService.assertSectionLearnable(userId, request.getSectionId());

        // 查询用户是否已经提交过该题，已提交则覆盖答案并重置为待批改
        LambdaQueryWrapper<HomeworkSubmission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HomeworkSubmission::getUserId, userId)
                .eq(HomeworkSubmission::getQuestionId, request.getQuestionId());
        HomeworkSubmission existingSubmission = this.getOne(queryWrapper);

        Long submissionId;
        if (existingSubmission != null) {
            existingSubmission.setAnswer(request.getAnswer());
            existingSubmission.setStatus(0);
            existingSubmission.setScore(0);
            existingSubmission.setFeedback(null);
            existingSubmission.setGradedBy(null);
            existingSubmission.setGradedTime(null);
            this.updateById(existingSubmission);
            submissionId = existingSubmission.getId();
        } else {
            // 首次提交时创建新的待批改记录
            HomeworkSubmission submission = new HomeworkSubmission();
            submission.setUserId(userId);
            submission.setSectionId(request.getSectionId());
            submission.setQuestionId(request.getQuestionId());
            submission.setAnswer(request.getAnswer());
            submission.setStatus(0);
            submission.setScore(0);
            this.save(submission);
            submissionId = submission.getId();
        }
        // 提交作业后尝试完成当前小节，主观题提交完成会参与小节完成判断
        courseService.tryCompleteSection(userId, request.getSectionId());
        return submissionId;
    }

    @Override
    public List<HomeworkSubmissionVO> getSectionExercises(Long sectionId, Long userId) {
        LambdaQueryWrapper<Question> questionQuery = new LambdaQueryWrapper<>();
        questionQuery.eq(Question::getSectionId, sectionId)
                .orderByAsc(Question::getSortOrder);
        List<Question> questions = questionService.list(questionQuery);

        if (questions.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        LambdaQueryWrapper<HomeworkSubmission> submissionQuery = new LambdaQueryWrapper<>();
        submissionQuery.in(HomeworkSubmission::getQuestionId, questionIds)
                .eq(HomeworkSubmission::getUserId, userId);
        Map<Long, HomeworkSubmission> submissionMap = this.list(submissionQuery).stream()
                .collect(Collectors.toMap(HomeworkSubmission::getQuestionId, s -> s));

        List<HomeworkSubmissionVO> result = new ArrayList<>();
        for (Question question : questions) {
            HomeworkSubmissionVO vo = new HomeworkSubmissionVO();
            vo.setQuestionId(question.getId());
            vo.setSectionId(sectionId);
            vo.setQuestionContent(question.getContent());
            vo.setQuestionType(question.getType());

            HomeworkSubmission submission = submissionMap.get(question.getId());
            if (submission != null) {
                vo.setId(submission.getId());
                vo.setAnswer(submission.getAnswer());
                vo.setStatus(submission.getStatus());
                vo.setScore(submission.getScore());
                vo.setFeedback(submission.getFeedback());
            }

            result.add(vo);
        }

        return result;
    }

    @Override
    public Page<HomeworkSubmissionVO> getUngradedHomeworkList(Long current, Long pageSize, Long teacherId) {
        LambdaQueryWrapper<Course> cq = new LambdaQueryWrapper<>();
        cq.eq(Course::getTeacherId, teacherId).eq(Course::getIsDelete, 0);
        List<Course> courses = courseMapper.selectList(cq);
        if (courses.isEmpty()) {
            Page<HomeworkSubmissionVO> empty = new Page<>(current, pageSize, 0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }
        List<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());
        LambdaQueryWrapper<CourseChapter> chq = new LambdaQueryWrapper<>();
        chq.in(CourseChapter::getCourseId, courseIds).eq(CourseChapter::getIsDelete, 0);
        List<CourseChapter> chapters = courseChapterMapper.selectList(chq);
        if (chapters.isEmpty()) {
            Page<HomeworkSubmissionVO> empty = new Page<>(current, pageSize, 0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }
        List<Long> chapterIds = chapters.stream().map(CourseChapter::getId).collect(Collectors.toList());
        LambdaQueryWrapper<Section> sq = new LambdaQueryWrapper<>();
        sq.in(Section::getChapterId, chapterIds);
        List<Section> sections = sectionMapper.selectList(sq);
        if (sections.isEmpty()) {
            Page<HomeworkSubmissionVO> empty = new Page<>(current, pageSize, 0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }
        List<Long> sectionIds = sections.stream().map(Section::getId).collect(Collectors.toList());

        Page<HomeworkSubmission> submissionPage = new Page<>(current, pageSize);
        LambdaQueryWrapper<HomeworkSubmission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HomeworkSubmission::getStatus, 0)
                .in(HomeworkSubmission::getSectionId, sectionIds)
                .orderByDesc(HomeworkSubmission::getCreateTime);

        Page<HomeworkSubmission> page = this.page(submissionPage, queryWrapper);
        return convertToVO(page);
    }

    /**
     * 教师批改学生作业提交记录。
     *
     * @param request 作业批改请求
     * @param teacherId 教师用户 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void gradeHomework(HomeworkGradeRequest request, Long teacherId) {
        // 查询提交记录，记录不存在时无法批改
        HomeworkSubmission submission = this.getById(request.getSubmissionId());
        if (submission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "submission not found");
        }

        // 已批改的作业不允许重复批改
        if (submission.getStatus() != null && submission.getStatus() > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "submission already reviewed");
        }

        // 校验当前教师是否拥有该作业所属课程的小节
        assertTeacherOwnsSubmissionSection(submission.getSectionId(), teacherId);

        // 写入批改状态、分数、反馈、批改人和批改时间
        submission.setStatus(request.getStatus());
        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setGradedBy(teacherId);
        submission.setGradedTime(new Date());
        this.updateById(submission);

        // 批改完成后记录日志，便于排查作业批改流转
        User user = userService.getById(submission.getUserId());
        if (user != null) {
            log.info("homework submission reviewed for user {}", user.getUserName());
        }
    }

    @Override
    public Page<HomeworkSubmissionVO> getMyHomeworkList(Long current, Long pageSize, Long userId, Integer status) {
        Page<HomeworkSubmission> submissionPage = new Page<>(current, pageSize);
        LambdaQueryWrapper<HomeworkSubmission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HomeworkSubmission::getUserId, userId);
        if (status != null) {
            queryWrapper.eq(HomeworkSubmission::getStatus, status);
        }
        queryWrapper.orderByDesc(HomeworkSubmission::getCreateTime);

        Page<HomeworkSubmission> page = this.page(submissionPage, queryWrapper);
        return convertToVO(page);
    }


    @Override
    public Page<HomeworkSubmissionReviewPageVO> getSubmissionReviewPage(Long current,
                                                                         Long pageSize,
                                                                         Long courseId,
                                                                         Long sectionId,
                                                                         Long homeworkId,
                                                                         Integer reviewStatus,
                                                                         User loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        long safeCurrent = current == null || current <= 0 ? 1L : current;
        long safePageSize = pageSize == null || pageSize <= 0 ? 10L : pageSize;
        if (safePageSize > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "pageSize cannot exceed 20");
        }

        List<Long> allowedSectionIds = null;
        if (!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            List<Long> managedCourseIds = getTeacherManagedCourseIds(loginUser.getId());
            if (managedCourseIds.isEmpty()) {
                return emptyReviewPage(safeCurrent, safePageSize);
            }
            if (courseId != null && !managedCourseIds.contains(courseId)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no permission to manage this course");
            }
            List<Long> targetCourseIds = courseId == null
                    ? managedCourseIds
                    : Collections.singletonList(courseId);
            allowedSectionIds = getSectionIdsByCourseIds(targetCourseIds);
            if (allowedSectionIds.isEmpty()) {
                return emptyReviewPage(safeCurrent, safePageSize);
            }
        } else if (courseId != null) {
            allowedSectionIds = getSectionIdsByCourseIds(Collections.singletonList(courseId));
            if (allowedSectionIds.isEmpty()) {
                return emptyReviewPage(safeCurrent, safePageSize);
            }
        }

        if (sectionId != null && allowedSectionIds != null && !allowedSectionIds.contains(sectionId)) {
            if (UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
                return emptyReviewPage(safeCurrent, safePageSize);
            }
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no permission to manage this section");
        }

        Page<HomeworkSubmission> submissionPage = new Page<>(safeCurrent, safePageSize);
        LambdaQueryWrapper<HomeworkSubmission> queryWrapper = new LambdaQueryWrapper<>();
        if (allowedSectionIds != null) {
            queryWrapper.in(HomeworkSubmission::getSectionId, allowedSectionIds);
        }
        if (sectionId != null) {
            queryWrapper.eq(HomeworkSubmission::getSectionId, sectionId);
        }
        if (homeworkId != null) {
            queryWrapper.eq(HomeworkSubmission::getQuestionId, homeworkId);
        }
        if (reviewStatus != null) {
            queryWrapper.eq(HomeworkSubmission::getStatus, reviewStatus);
        }
        queryWrapper.orderByDesc(HomeworkSubmission::getCreateTime);

        Page<HomeworkSubmission> page = this.page(submissionPage, queryWrapper);
        return convertToReviewPageVO(page);
    }

    @Override
    public HomeworkSubmissionReviewDetailVO getSubmissionReviewDetail(Long submissionId, User loginUser) {
        if (submissionId == null || submissionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id is required");
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        HomeworkSubmission submission = this.getById(submissionId);
        if (submission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "submission not found");
        }

        Long courseIdOfSubmission = getCourseIdBySectionId(submission.getSectionId());
        courseService.assertCanManageCourse(loginUser, courseIdOfSubmission);

        HomeworkSubmissionReviewDetailVO detailVO = new HomeworkSubmissionReviewDetailVO();
        detailVO.setId(submission.getId());
        detailVO.setSubmissionId(submission.getId());
        detailVO.setAnswerContent(submission.getAnswer());
        detailVO.setAnswerAttachmentUrl(null);
        detailVO.setReviewComment(submission.getFeedback());
        detailVO.setComment(submission.getFeedback());
        detailVO.setReviewerId(submission.getGradedBy());
        detailVO.setReviewTime(submission.getGradedTime());

        if (submission.getGradedBy() != null) {
            User reviewer = userService.getById(submission.getGradedBy());
            if (reviewer != null) {
                detailVO.setReviewerName(reviewer.getUserName());
            }
        }

        return detailVO;
    }

    /**
     * 管理端批改指定作业提交记录。
     *
     * @param submissionId 作业提交记录 ID
     * @param score 批改分数
     * @param comment 批改评语
     * @param loginUser 当前登录用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewSubmission(Long submissionId, Integer score, String comment, User loginUser) {
        // 校验提交记录、分数和登录用户，分数必须在 0 到 100 之间
        if (submissionId == null || submissionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "submissionId is required");
        }
        if (score == null || score < 0 || score > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "score must be between 0 and 100");
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 查询提交记录，并确认当前用户有权管理该作业所属课程
        HomeworkSubmission submission = this.getById(submissionId);
        if (submission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "submission not found");
        }

        Long courseId = getCourseIdBySectionId(submission.getSectionId());
        courseService.assertCanManageCourse(loginUser, courseId);

        // 组装通用批改请求，普通教师复用 gradeHomework 的课程归属校验
        HomeworkGradeRequest request = new HomeworkGradeRequest();
        request.setSubmissionId(submissionId);
        request.setStatus(1);
        request.setScore(score);
        request.setFeedback(comment);

        // 管理员已通过课程管理权限校验，直接写入批改结果
        if (UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            submission.setStatus(1);
            submission.setScore(score);
            submission.setFeedback(comment);
            submission.setGradedBy(loginUser.getId());
            submission.setGradedTime(new Date());
            this.updateById(submission);
            return;
        }

        // 非管理员按教师身份继续校验课程归属并批改
        this.gradeHomework(request, loginUser.getId());
    }

    private Page<HomeworkSubmissionReviewPageVO> convertToReviewPageVO(Page<HomeworkSubmission> page) {
        List<HomeworkSubmission> submissions = page.getRecords();
        Page<HomeworkSubmissionReviewPageVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (submissions == null || submissions.isEmpty()) {
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }

        List<Long> userIds = submissions.stream()
                .map(HomeworkSubmission::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));

        List<Long> questionIds = submissions.stream()
                .map(HomeworkSubmission::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Question> questionMap = questionIds.isEmpty()
                ? Collections.emptyMap()
                : questionService.listByIds(questionIds).stream().collect(Collectors.toMap(Question::getId, q -> q));

        List<Long> sectionIds = submissions.stream()
                .map(HomeworkSubmission::getSectionId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Section> sectionMap = sectionIds.isEmpty()
                ? Collections.emptyMap()
                : sectionService.listByIds(sectionIds).stream().collect(Collectors.toMap(Section::getId, s -> s));

        List<Long> chapterIds = sectionMap.values().stream()
                .map(Section::getChapterId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, CourseChapter> chapterMap = chapterIds.isEmpty()
                ? Collections.emptyMap()
                : courseChapterMapper.selectBatchIds(chapterIds).stream()
                .collect(Collectors.toMap(CourseChapter::getId, c -> c));

        List<HomeworkSubmissionReviewPageVO> voList = submissions.stream().map(submission -> {
            HomeworkSubmissionReviewPageVO vo = new HomeworkSubmissionReviewPageVO();
            vo.setId(submission.getId());
            vo.setHomeworkId(submission.getQuestionId());
            vo.setSectionId(submission.getSectionId());
            vo.setStudentId(submission.getUserId());
            vo.setSubmitTime(submission.getCreateTime());
            vo.setReviewStatus(submission.getStatus());
            vo.setReviewScore(submission.getScore());
            vo.setScore(submission.getScore());

            User student = userMap.get(submission.getUserId());
            if (student != null) {
                vo.setStudentName(student.getUserName());
            }

            Question question = questionMap.get(submission.getQuestionId());
            if (question != null) {
                vo.setHomeworkTitle(question.getContent());
            }

            Section section = sectionMap.get(submission.getSectionId());
            if (section != null && section.getChapterId() != null) {
                CourseChapter chapter = chapterMap.get(section.getChapterId());
                if (chapter != null) {
                    vo.setCourseId(chapter.getCourseId());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    private List<Long> getTeacherManagedCourseIds(Long teacherId) {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getTeacherId, teacherId)
                .eq(Course::getIsDelete, 0);
        List<Course> courses = courseMapper.selectList(queryWrapper);
        if (courses == null || courses.isEmpty()) {
            return Collections.emptyList();
        }
        return courses.stream().map(Course::getId).collect(Collectors.toList());
    }

    private List<Long> getSectionIdsByCourseIds(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<CourseChapter> chapterQuery = new LambdaQueryWrapper<>();
        chapterQuery.in(CourseChapter::getCourseId, courseIds)
                .eq(CourseChapter::getIsDelete, 0);
        List<CourseChapter> chapters = courseChapterMapper.selectList(chapterQuery);
        if (chapters == null || chapters.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> chapterIds = chapters.stream().map(CourseChapter::getId).collect(Collectors.toList());
        LambdaQueryWrapper<Section> sectionQuery = new LambdaQueryWrapper<>();
        sectionQuery.in(Section::getChapterId, chapterIds)
                .eq(Section::getIsDelete, 0);
        List<Section> sections = sectionMapper.selectList(sectionQuery);
        if (sections == null || sections.isEmpty()) {
            return Collections.emptyList();
        }
        return sections.stream().map(Section::getId).collect(Collectors.toList());
    }

    private Long getCourseIdBySectionId(Long sectionId) {
        Section section = sectionMapper.selectById(sectionId);
        if (section == null || section.getChapterId() == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "section not found");
        }
        CourseChapter chapter = courseChapterMapper.selectById(section.getChapterId());
        if (chapter == null || chapter.getCourseId() == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "chapter not found");
        }
        return chapter.getCourseId();
    }

    private Page<HomeworkSubmissionReviewPageVO> emptyReviewPage(Long current, Long pageSize) {
        Page<HomeworkSubmissionReviewPageVO> page = new Page<>(current, pageSize, 0);
        page.setRecords(Collections.emptyList());
        return page;
    }
    private void assertTeacherOwnsSubmissionSection(Long sectionId, Long teacherId) {
        Section section = sectionMapper.selectById(sectionId);
        if (section == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "section not found");
        }
        CourseChapter chapter = courseChapterMapper.selectById(section.getChapterId());
        if (chapter == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "chapter not found");
        }
        Course course = courseMapper.selectById(chapter.getCourseId());
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "course not found");
        }
        if (!teacherId.equals(course.getTeacherId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no permission to review this submission");
        }
    }

    private Page<HomeworkSubmissionVO> convertToVO(Page<HomeworkSubmission> page) {
        List<HomeworkSubmission> submissions = page.getRecords();
        if (submissions.isEmpty()) {
            return new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        }

        List<Long> userIds = submissions.stream().map(HomeworkSubmission::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<Long> sectionIds = submissions.stream().map(HomeworkSubmission::getSectionId).distinct().collect(Collectors.toList());
        Map<Long, Section> sectionMap = sectionService.listByIds(sectionIds).stream()
                .collect(Collectors.toMap(Section::getId, s -> s));

        List<Long> questionIds = submissions.stream().map(HomeworkSubmission::getQuestionId).distinct().collect(Collectors.toList());

        Map<Long, Question> questionMap;
        if (!questionIds.isEmpty()) {
            questionMap = questionService.listByIds(questionIds).stream()
                    .collect(Collectors.toMap(Question::getId, q -> q));
        } else {
            questionMap = Collections.emptyMap();
        }

        List<Long> gradedByIds = submissions.stream()
                .filter(s -> s.getGradedBy() != null)
                .map(HomeworkSubmission::getGradedBy)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> gradedByMap;
        if (!gradedByIds.isEmpty()) {
            gradedByMap = userService.listByIds(gradedByIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
        } else {
            gradedByMap = Collections.emptyMap();
        }

        List<HomeworkSubmissionVO> voList = submissions.stream().map(submission -> {
            HomeworkSubmissionVO vo = new HomeworkSubmissionVO();
            BeanUtils.copyProperties(submission, vo);

            User user = userMap.get(submission.getUserId());
            if (user != null) {
                vo.setUserName(user.getUserName());
                vo.setUserAvatar(user.getUserAvatar());
            }

            Section section = sectionMap.get(submission.getSectionId());
            if (section != null) {
                vo.setSectionTitle(section.getTitle());
            }

            Question question = questionMap.get(submission.getQuestionId());
            if (question != null) {
                vo.setQuestionContent(question.getContent());
                vo.setQuestionType(question.getType());
            }

            Long gradedById = submission.getGradedBy();
            if (gradedById != null) {
                User gradedBy = gradedByMap.get(gradedById);
                if (gradedBy != null) {
                    vo.setGradedByName(gradedBy.getUserName());
                }
            }

            return vo;
        }).collect(Collectors.toList());

        Page<HomeworkSubmissionVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }
}



