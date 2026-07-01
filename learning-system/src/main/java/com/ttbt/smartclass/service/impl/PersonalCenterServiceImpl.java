package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.CourseChapterMapper;
import com.ttbt.smartclass.mapper.CourseMapper;
import com.ttbt.smartclass.mapper.DailyArticleMapper;
import com.ttbt.smartclass.mapper.DailyWordMapper;
import com.ttbt.smartclass.mapper.SectionMapper;
import com.ttbt.smartclass.mapper.UserDailyArticleMapper;
import com.ttbt.smartclass.mapper.UserDailyWordMapper;
import com.ttbt.smartclass.mapper.UserProgressMapper;
import com.ttbt.smartclass.mapper.VideoLearningRecordMapper;
import com.ttbt.smartclass.model.entity.Course;
import com.ttbt.smartclass.model.entity.CourseChapter;
import com.ttbt.smartclass.model.entity.DailyArticle;
import com.ttbt.smartclass.model.entity.DailyWord;
import com.ttbt.smartclass.model.entity.CourseFavourite;
import com.ttbt.smartclass.model.entity.HomeworkSubmission;
import com.ttbt.smartclass.model.entity.PostFavour;
import com.ttbt.smartclass.model.entity.Section;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.entity.UserCourse;
import com.ttbt.smartclass.model.entity.UserDailyArticle;
import com.ttbt.smartclass.model.entity.UserDailyWord;
import com.ttbt.smartclass.model.entity.UserProgress;
import com.ttbt.smartclass.model.entity.UserWordBook;
import com.ttbt.smartclass.model.entity.VideoLearningRecord;
import com.ttbt.smartclass.model.vo.ChatSessionVO;
import com.ttbt.smartclass.model.vo.LearningHistoryItemVO;
import com.ttbt.smartclass.model.vo.MyCourseVO;
import com.ttbt.smartclass.model.vo.PersonalCenterOverviewVO;
import com.ttbt.smartclass.model.vo.PersonalFavouriteCourseVO;
import com.ttbt.smartclass.model.vo.PersonalLearningRecordVO;
import com.ttbt.smartclass.service.AiAvatarChatHistoryService;
import com.ttbt.smartclass.service.CourseFavouriteService;
import com.ttbt.smartclass.service.HomeworkSubmissionService;
import com.ttbt.smartclass.service.PersonalCenterService;
import com.ttbt.smartclass.service.PostFavourService;
import com.ttbt.smartclass.service.UserCourseService;
import com.ttbt.smartclass.service.UserService;
import com.ttbt.smartclass.service.UserWordBookService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 个人中心聚合服务实现类。
 */
@Service
public class PersonalCenterServiceImpl implements PersonalCenterService {

    private static final long OVERVIEW_COURSE_LIMIT = 3L;
    private static final int OVERVIEW_AI_SESSION_LIMIT = 3;

    @Resource
    private UserService userService;

    @Resource
    private UserCourseService userCourseService;

    @Resource
    private AiAvatarChatHistoryService aiAvatarChatHistoryService;

    @Resource
    private CourseFavouriteService courseFavouriteService;

    @Resource
    private PostFavourService postFavourService;

    @Resource
    private UserWordBookService userWordBookService;

    @Resource
    private HomeworkSubmissionService homeworkSubmissionService;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private UserProgressMapper userProgressMapper;

    @Resource
    private CourseChapterMapper courseChapterMapper;

    @Resource
    private SectionMapper sectionMapper;

    @Resource
    private VideoLearningRecordMapper videoLearningRecordMapper;

    @Resource
    private UserDailyWordMapper userDailyWordMapper;

    @Resource
    private UserDailyArticleMapper userDailyArticleMapper;

    @Resource
    private DailyWordMapper dailyWordMapper;

    @Resource
    private DailyArticleMapper dailyArticleMapper;

    @Override
    public PersonalCenterOverviewVO getOverview(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "please login first");
        }
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "user not found");
        }

        PersonalCenterOverviewVO overview = new PersonalCenterOverviewVO();
        overview.setUserInfo(userService.getUserVO(user));

        fillCourseOverview(overview, userId);
        fillAiOverview(overview, userId);
        fillFavourOverview(overview, userId);
        fillWordBookOverview(overview, userId);
        fillHomeworkOverview(overview, userId);
        return overview;
    }

    @Override
    public Page<PersonalFavouriteCourseVO> getFavouriteCoursePage(Long userId, long current, long pageSize) {
        assertLoginUser(userId);
        long safeCurrent = normalizeCurrent(current);
        long safePageSize = normalizePageSize(pageSize);

        LambdaQueryWrapper<CourseFavourite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseFavourite::getUserId, userId)
                .orderByDesc(CourseFavourite::getCreateTime)
                .orderByDesc(CourseFavourite::getId);
        Page<CourseFavourite> favouritePage = courseFavouriteService.page(new Page<>(safeCurrent, safePageSize), queryWrapper);

        Page<PersonalFavouriteCourseVO> resultPage = new Page<>(favouritePage.getCurrent(), favouritePage.getSize(), favouritePage.getTotal());
        List<CourseFavourite> favourites = favouritePage.getRecords();
        if (favourites == null || favourites.isEmpty()) {
            resultPage.setRecords(Collections.emptyList());
            return resultPage;
        }

        Set<Long> courseIds = favourites.stream()
                .map(CourseFavourite::getCourseId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Long, Course> courseMap = loadCourseMap(courseIds);
        Map<Long, String> teacherNameMap = loadTeacherNameMap(courseMap.values());

        List<PersonalFavouriteCourseVO> records = favourites.stream()
                .map(favourite -> toPersonalFavouriteCourseVO(favourite, courseMap.get(favourite.getCourseId()), teacherNameMap))
                .filter(item -> item != null)
                .collect(Collectors.toList());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public Page<PersonalLearningRecordVO> getLearningRecordPage(Long userId, long current, long pageSize) {
        assertLoginUser(userId);
        long safeCurrent = normalizeCurrent(current);
        long safePageSize = normalizePageSize(pageSize);

        LambdaQueryWrapper<UserCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCourse::getUserId, userId)
                .orderByDesc(UserCourse::getLastLearnTime)
                .orderByDesc(UserCourse::getUpdateTime)
                .orderByDesc(UserCourse::getCreateTime);
        Page<UserCourse> userCoursePage = userCourseService.page(new Page<>(safeCurrent, safePageSize), queryWrapper);

        Page<PersonalLearningRecordVO> resultPage = new Page<>(userCoursePage.getCurrent(), userCoursePage.getSize(), userCoursePage.getTotal());
        List<UserCourse> userCourses = userCoursePage.getRecords();
        if (userCourses == null || userCourses.isEmpty()) {
            resultPage.setRecords(Collections.emptyList());
            return resultPage;
        }

        Set<Long> courseIds = userCourses.stream()
                .map(UserCourse::getCourseId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Long, Course> courseMap = loadCourseMap(courseIds);
        Map<Long, Integer> completedSectionsMap = queryCompletedSections(userId, courseIds);
        Map<Long, UserProgress> latestProgressMap = queryLatestProgressMap(userId, courseIds);
        Map<Long, Section> latestSectionMap = queryLatestSectionMap(latestProgressMap.values());

        List<PersonalLearningRecordVO> records = userCourses.stream()
                .map(userCourse -> toPersonalLearningRecordVO(userCourse,
                        courseMap.get(userCourse.getCourseId()),
                        latestProgressMap.get(userCourse.getCourseId()),
                        latestSectionMap,
                        completedSectionsMap))
                .filter(item -> item != null)
                .collect(Collectors.toList());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public Page<LearningHistoryItemVO> getLearningHistoryPage(Long userId, long current, long pageSize) {
        assertLoginUser(userId);
        long safeCurrent = normalizeCurrent(current);
        long safePageSize = normalizePageSize(pageSize);
        int sourceLimit = (int) Math.min(100, safeCurrent * safePageSize + 20);

        List<LearningHistoryItemVO> records = new ArrayList<>();
        records.addAll(listVideoHistory(userId, sourceLimit));
        records.addAll(listCompletedSectionHistory(userId, sourceLimit));
        records.addAll(listDailyWordHistory(userId, sourceLimit));
        records.addAll(listDailyArticleHistory(userId, sourceLimit));
        records.addAll(listHomeworkHistory(userId, sourceLimit));

        records = records.stream()
                .filter(item -> item.getActionTime() != null)
                .sorted(Comparator.comparing(LearningHistoryItemVO::getActionTime).reversed())
                .collect(Collectors.toList());

        Page<LearningHistoryItemVO> resultPage = new Page<>(safeCurrent, safePageSize, records.size());
        int fromIndex = (int) Math.min((safeCurrent - 1) * safePageSize, records.size());
        int toIndex = (int) Math.min(fromIndex + safePageSize, records.size());
        resultPage.setRecords(fromIndex >= toIndex ? Collections.emptyList() : records.subList(fromIndex, toIndex));
        return resultPage;
    }

    private void fillCourseOverview(PersonalCenterOverviewVO overview, Long userId) {
        LambdaQueryWrapper<UserCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCourse::getUserId, userId);
        overview.setMyCourseCount(userCourseService.count(queryWrapper));

        Page<MyCourseVO> recentCoursePage = userCourseService.getMyCoursePage(userId, 1, OVERVIEW_COURSE_LIMIT);
        overview.setRecentCourses(recentCoursePage == null || recentCoursePage.getRecords() == null
                ? Collections.emptyList()
                : recentCoursePage.getRecords());
    }

    private void fillAiOverview(PersonalCenterOverviewVO overview, Long userId) {
        List<ChatSessionVO> allSessions = aiAvatarChatHistoryService.getUserSessions(userId, null);
        overview.setAiSessionCount((long) (allSessions == null ? 0 : allSessions.size()));

        List<ChatSessionVO> recentSessions = aiAvatarChatHistoryService.getRecentSessions(userId, OVERVIEW_AI_SESSION_LIMIT);
        overview.setRecentAiSessions(recentSessions == null ? Collections.emptyList() : recentSessions);
    }

    private void fillFavourOverview(PersonalCenterOverviewVO overview, Long userId) {
        overview.setCourseFavouriteCount(courseFavouriteService.getUserFavouriteCount(userId));

        LambdaQueryWrapper<PostFavour> postFavourQuery = new LambdaQueryWrapper<>();
        postFavourQuery.eq(PostFavour::getUserId, userId);
        overview.setPostFavouriteCount(postFavourService.count(postFavourQuery));
    }

    private void fillWordBookOverview(PersonalCenterOverviewVO overview, Long userId) {
        LambdaQueryWrapper<UserWordBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserWordBook::getUserId, userId)
                .eq(UserWordBook::getIsDeleted, 0);
        overview.setWordBookCount(userWordBookService.count(queryWrapper));

        int[] statistics = userWordBookService.getUserWordBookStatistics(userId);
        overview.setWordBookLearnedCount(statistics.length > 1 ? statistics[1] : 0);
        overview.setWordBookReviewCount(statistics.length > 2 ? statistics[2] : 0);
    }

    private void fillHomeworkOverview(PersonalCenterOverviewVO overview, Long userId) {
        LambdaQueryWrapper<HomeworkSubmission> totalQuery = new LambdaQueryWrapper<>();
        totalQuery.eq(HomeworkSubmission::getUserId, userId);
        overview.setHomeworkCount(homeworkSubmissionService.count(totalQuery));

        LambdaQueryWrapper<HomeworkSubmission> pendingQuery = new LambdaQueryWrapper<>();
        pendingQuery.eq(HomeworkSubmission::getUserId, userId)
                .eq(HomeworkSubmission::getStatus, 0);
        overview.setPendingHomeworkCount(homeworkSubmissionService.count(pendingQuery));
    }

    private Map<Long, Course> loadCourseMap(Set<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return courseMapper.selectBatchIds(courseIds).stream()
                .filter(course -> course != null && (course.getIsDelete() == null || course.getIsDelete() == 0))
                .collect(Collectors.toMap(Course::getId, Function.identity()));
    }

    private Map<Long, String> loadTeacherNameMap(java.util.Collection<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> teacherIds = courses.stream()
                .map(Course::getTeacherId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (teacherIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userService.listByIds(teacherIds).stream()
                .filter(user -> user != null && user.getId() != null)
                .collect(Collectors.toMap(User::getId, user -> StringUtils.defaultString(user.getUserName())));
    }

    private Map<Long, Integer> queryCompletedSections(Long userId, Set<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<UserProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserProgress::getUserId, userId)
                .eq(UserProgress::getStatus, 2)
                .in(UserProgress::getCourseId, courseIds);
        List<UserProgress> progressList = userProgressMapper.selectList(queryWrapper);
        if (progressList == null || progressList.isEmpty()) {
            return Collections.emptyMap();
        }
        return progressList.stream()
                .filter(progress -> progress.getCourseId() != null)
                .collect(Collectors.groupingBy(UserProgress::getCourseId,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }

    private Map<Long, UserProgress> queryLatestProgressMap(Long userId, Set<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<UserProgress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserProgress::getUserId, userId)
                .in(UserProgress::getCourseId, courseIds)
                .orderByDesc(UserProgress::getLastLearnTime)
                .orderByDesc(UserProgress::getUpdateTime)
                .orderByDesc(UserProgress::getId);
        List<UserProgress> progressList = userProgressMapper.selectList(queryWrapper);
        if (progressList == null || progressList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, UserProgress> latestProgressMap = new HashMap<>();
        for (UserProgress progress : progressList) {
            if (progress.getCourseId() != null && !latestProgressMap.containsKey(progress.getCourseId())) {
                latestProgressMap.put(progress.getCourseId(), progress);
            }
        }
        return latestProgressMap;
    }

    private Map<Long, Section> queryLatestSectionMap(java.util.Collection<UserProgress> progressList) {
        if (progressList == null || progressList.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> sectionIds = progressList.stream()
                .map(UserProgress::getSectionId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (sectionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return sectionMapper.selectBatchIds(sectionIds).stream()
                .filter(section -> section != null && section.getId() != null && (section.getIsDelete() == null || section.getIsDelete() == 0))
                .collect(Collectors.toMap(Section::getId, Function.identity()));
    }

    private PersonalFavouriteCourseVO toPersonalFavouriteCourseVO(CourseFavourite favourite,
                                                                  Course course,
                                                                  Map<Long, String> teacherNameMap) {
        if (favourite == null || course == null) {
            return null;
        }
        PersonalFavouriteCourseVO vo = new PersonalFavouriteCourseVO();
        vo.setCourseId(course.getId());
        vo.setCourseTitle(course.getTitle());
        vo.setCourseCover(resolveCourseCover(course));
        vo.setCourseDescription(course.getDescription());
        vo.setTeacherName(teacherNameMap.get(course.getTeacherId()));
        vo.setTotalSections(course.getTotalSections() == null ? 0 : course.getTotalSections());
        vo.setDifficulty(course.getDifficulty());
        vo.setCategoryId(course.getCategoryId());
        vo.setFavourTime(favourite.getCreateTime());
        return vo;
    }

    private PersonalLearningRecordVO toPersonalLearningRecordVO(UserCourse userCourse,
                                                                Course course,
                                                                UserProgress latestProgress,
                                                                Map<Long, Section> latestSectionMap,
                                                                Map<Long, Integer> completedSectionsMap) {
        if (userCourse == null || course == null) {
            return null;
        }
        PersonalLearningRecordVO vo = new PersonalLearningRecordVO();
        vo.setCourseId(course.getId());
        vo.setCourseTitle(course.getTitle());
        if (latestProgress != null) {
            vo.setSectionId(latestProgress.getSectionId());
            Section latestSection = latestSectionMap.get(latestProgress.getSectionId());
            if (latestSection != null) {
                vo.setSectionTitle(latestSection.getTitle());
            }
        }
        vo.setProgressPercent(userCourse.getProgress() == null ? BigDecimal.ZERO : userCourse.getProgress());
        vo.setLastLearnTime(resolveLastLearnTime(userCourse, latestProgress));
        vo.setCompletedSections(completedSectionsMap.getOrDefault(course.getId(), 0));
        vo.setTotalSections(course.getTotalSections() == null ? 0 : course.getTotalSections());
        return vo;
    }

    private List<LearningHistoryItemVO> listVideoHistory(Long userId, int limit) {
        LambdaQueryWrapper<VideoLearningRecord> query = new LambdaQueryWrapper<>();
        query.eq(VideoLearningRecord::getUserId, userId)
                .orderByDesc(VideoLearningRecord::getUpdateTime)
                .orderByDesc(VideoLearningRecord::getId)
                .last("limit " + limit);
        List<VideoLearningRecord> videoRecords = videoLearningRecordMapper.selectList(query);
        if (videoRecords == null || videoRecords.isEmpty()) {
            return Collections.emptyList();
        }
        LearningContext context = buildLearningContext(videoRecords.stream()
                .map(VideoLearningRecord::getSectionId)
                .collect(Collectors.toSet()));
        return videoRecords.stream().map(record -> {
            Section section = context.sectionMap.get(record.getSectionId());
            Course course = context.courseMap.get(context.sectionCourseMap.get(record.getSectionId()));
            LearningHistoryItemVO vo = baseHistory("VIDEO:" + record.getId(), "VIDEO", record.getUpdateTime());
            vo.setTitle("观看视频课程");
            vo.setDescription(joinTitle(course == null ? null : course.getTitle(), section == null ? null : section.getTitle()));
            vo.setCourseId(course == null ? null : course.getId());
            vo.setSectionId(record.getSectionId());
            vo.setTargetId(record.getId());
            vo.setProgressPercent(record.getProgress() == null ? 0 : record.getProgress().intValue());
            return vo;
        }).collect(Collectors.toList());
    }

    private List<LearningHistoryItemVO> listCompletedSectionHistory(Long userId, int limit) {
        LambdaQueryWrapper<UserProgress> query = new LambdaQueryWrapper<>();
        query.eq(UserProgress::getUserId, userId)
                .eq(UserProgress::getStatus, 2)
                .orderByDesc(UserProgress::getLastLearnTime)
                .orderByDesc(UserProgress::getUpdateTime)
                .orderByDesc(UserProgress::getId)
                .last("limit " + limit);
        List<UserProgress> progressList = userProgressMapper.selectList(query);
        if (progressList == null || progressList.isEmpty()) {
            return Collections.emptyList();
        }
        LearningContext context = buildLearningContext(progressList.stream()
                .map(UserProgress::getSectionId)
                .collect(Collectors.toSet()));
        return progressList.stream().map(progress -> {
            Section section = context.sectionMap.get(progress.getSectionId());
            Course course = context.courseMap.get(progress.getCourseId());
            LearningHistoryItemVO vo = baseHistory("SECTION:" + progress.getId(), "SECTION_COMPLETE",
                    progress.getLastLearnTime() == null ? progress.getUpdateTime() : progress.getLastLearnTime());
            vo.setTitle("完成课程小节");
            vo.setDescription(joinTitle(course == null ? null : course.getTitle(), section == null ? null : section.getTitle()));
            vo.setCourseId(progress.getCourseId());
            vo.setSectionId(progress.getSectionId());
            vo.setTargetId(progress.getId());
            vo.setProgressPercent(100);
            return vo;
        }).collect(Collectors.toList());
    }

    private List<LearningHistoryItemVO> listDailyWordHistory(Long userId, int limit) {
        LambdaQueryWrapper<UserDailyWord> query = new LambdaQueryWrapper<>();
        query.eq(UserDailyWord::getUserId, userId)
                .eq(UserDailyWord::getIsStudied, 1)
                .orderByDesc(UserDailyWord::getStudyTime)
                .orderByDesc(UserDailyWord::getUpdateTime)
                .orderByDesc(UserDailyWord::getId)
                .last("limit " + limit);
        List<UserDailyWord> records = userDailyWordMapper.selectList(query);
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> wordIds = records.stream()
                .map(UserDailyWord::getWordId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Long, DailyWord> wordMap = wordIds.isEmpty() ? Collections.emptyMap() : dailyWordMapper.selectBatchIds(wordIds)
                .stream().collect(Collectors.toMap(DailyWord::getId, Function.identity(), (left, right) -> left));
        return records.stream().map(record -> {
            DailyWord word = wordMap.get(record.getWordId());
            LearningHistoryItemVO vo = baseHistory("WORD:" + record.getId(), "DAILY_WORD",
                    record.getStudyTime() == null ? record.getUpdateTime() : record.getStudyTime());
            vo.setTitle("学习每日单词");
            vo.setDescription(word == null ? "每日单词" : word.getWord());
            vo.setTargetId(record.getWordId());
            vo.setProgressPercent(100);
            return vo;
        }).collect(Collectors.toList());
    }

    private List<LearningHistoryItemVO> listDailyArticleHistory(Long userId, int limit) {
        LambdaQueryWrapper<UserDailyArticle> query = new LambdaQueryWrapper<>();
        query.eq(UserDailyArticle::getUserId, userId)
                .eq(UserDailyArticle::getIsRead, 1)
                .orderByDesc(UserDailyArticle::getReadTime)
                .orderByDesc(UserDailyArticle::getUpdateTime)
                .orderByDesc(UserDailyArticle::getId)
                .last("limit " + limit);
        List<UserDailyArticle> records = userDailyArticleMapper.selectList(query);
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> articleIds = records.stream()
                .map(UserDailyArticle::getArticleId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Long, DailyArticle> articleMap = articleIds.isEmpty() ? Collections.emptyMap() : dailyArticleMapper.selectBatchIds(articleIds)
                .stream().collect(Collectors.toMap(DailyArticle::getId, Function.identity(), (left, right) -> left));
        return records.stream().map(record -> {
            DailyArticle article = articleMap.get(record.getArticleId());
            LearningHistoryItemVO vo = baseHistory("ARTICLE:" + record.getId(), "DAILY_ARTICLE",
                    record.getReadTime() == null ? record.getUpdateTime() : record.getReadTime());
            vo.setTitle("阅读每日美文");
            vo.setDescription(article == null ? "每日美文" : article.getTitle());
            vo.setTargetId(record.getArticleId());
            vo.setProgressPercent(100);
            return vo;
        }).collect(Collectors.toList());
    }

    private List<LearningHistoryItemVO> listHomeworkHistory(Long userId, int limit) {
        LambdaQueryWrapper<HomeworkSubmission> query = new LambdaQueryWrapper<>();
        query.eq(HomeworkSubmission::getUserId, userId)
                .orderByDesc(HomeworkSubmission::getCreateTime)
                .orderByDesc(HomeworkSubmission::getId)
                .last("limit " + limit);
        List<HomeworkSubmission> submissions = homeworkSubmissionService.list(query);
        if (submissions == null || submissions.isEmpty()) {
            return Collections.emptyList();
        }
        LearningContext context = buildLearningContext(submissions.stream()
                .map(HomeworkSubmission::getSectionId)
                .collect(Collectors.toSet()));
        return submissions.stream().map(submission -> {
            Section section = context.sectionMap.get(submission.getSectionId());
            Long courseId = context.sectionCourseMap.get(submission.getSectionId());
            Course course = context.courseMap.get(courseId);
            LearningHistoryItemVO vo = baseHistory("HOMEWORK:" + submission.getId(), "HOMEWORK", submission.getCreateTime());
            vo.setTitle("完成作业/练习");
            vo.setDescription(joinTitle(course == null ? null : course.getTitle(), section == null ? null : section.getTitle()));
            vo.setCourseId(courseId);
            vo.setSectionId(submission.getSectionId());
            vo.setTargetId(submission.getId());
            vo.setProgressPercent(submission.getScore());
            return vo;
        }).collect(Collectors.toList());
    }

    private LearningContext buildLearningContext(Set<Long> sectionIds) {
        if (sectionIds == null || sectionIds.isEmpty()) {
            return new LearningContext(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
        }
        Map<Long, Section> sectionMap = sectionMapper.selectBatchIds(sectionIds).stream()
                .filter(section -> section != null && section.getId() != null)
                .collect(Collectors.toMap(Section::getId, Function.identity(), (left, right) -> left));
        Set<Long> chapterIds = sectionMap.values().stream()
                .map(Section::getChapterId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Long, CourseChapter> chapterMap = chapterIds.isEmpty() ? Collections.emptyMap()
                : courseChapterMapper.selectBatchIds(chapterIds).stream()
                .filter(chapter -> chapter != null && chapter.getId() != null)
                .collect(Collectors.toMap(CourseChapter::getId, Function.identity(), (left, right) -> left));
        Map<Long, Long> sectionCourseMap = new HashMap<>();
        for (Section section : sectionMap.values()) {
            CourseChapter chapter = chapterMap.get(section.getChapterId());
            if (chapter != null && chapter.getCourseId() != null) {
                sectionCourseMap.put(section.getId(), chapter.getCourseId());
            }
        }
        Map<Long, Course> courseMap = loadCourseMap(new HashSet<>(sectionCourseMap.values()));
        return new LearningContext(sectionMap, sectionCourseMap, courseMap);
    }

    private LearningHistoryItemVO baseHistory(String id, String actionType, Date actionTime) {
        LearningHistoryItemVO vo = new LearningHistoryItemVO();
        vo.setId(id);
        vo.setActionType(actionType);
        vo.setActionTime(actionTime);
        return vo;
    }

    private String joinTitle(String courseTitle, String sectionTitle) {
        if (StringUtils.isNotBlank(courseTitle) && StringUtils.isNotBlank(sectionTitle)) {
            return courseTitle + " - " + sectionTitle;
        }
        if (StringUtils.isNotBlank(sectionTitle)) {
            return sectionTitle;
        }
        return StringUtils.defaultIfBlank(courseTitle, "学习记录");
    }

    private Date resolveLastLearnTime(UserCourse userCourse, UserProgress latestProgress) {
        List<Date> candidates = new ArrayList<>();
        if (userCourse != null && userCourse.getLastLearnTime() != null) {
            candidates.add(userCourse.getLastLearnTime());
        }
        if (latestProgress != null && latestProgress.getLastLearnTime() != null) {
            candidates.add(latestProgress.getLastLearnTime());
        }
        return candidates.stream().max(Comparator.naturalOrder()).orElse(null);
    }

    private String resolveCourseCover(Course course) {
        if (course == null) {
            return null;
        }
        return StringUtils.isNotBlank(course.getCoverUrl()) ? course.getCoverUrl() : course.getCoverImage();
    }

    private void assertLoginUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "please login first");
        }
    }

    private long normalizeCurrent(long current) {
        return current <= 0 ? 1 : current;
    }

    private long normalizePageSize(long pageSize) {
        if (pageSize <= 0) {
            return 10;
        }
        return Math.min(pageSize, 50);
    }

    private static final class LearningContext {
        private final Map<Long, Section> sectionMap;
        private final Map<Long, Long> sectionCourseMap;
        private final Map<Long, Course> courseMap;

        private LearningContext(Map<Long, Section> sectionMap,
                                Map<Long, Long> sectionCourseMap,
                                Map<Long, Course> courseMap) {
            this.sectionMap = sectionMap;
            this.sectionCourseMap = sectionCourseMap;
            this.courseMap = courseMap;
        }
    }
}
