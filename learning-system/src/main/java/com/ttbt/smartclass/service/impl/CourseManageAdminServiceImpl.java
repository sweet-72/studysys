package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.constant.UserConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.CourseChapterMapper;
import com.ttbt.smartclass.mapper.CourseMapper;
import com.ttbt.smartclass.mapper.SectionMapper;
import com.ttbt.smartclass.model.dto.HomeworkGradeRequest;
import com.ttbt.smartclass.model.dto.course.CourseChapterSectionRequest;
import com.ttbt.smartclass.model.dto.course.CourseManageCreateRequest;
import com.ttbt.smartclass.model.dto.course.CourseUpdateRequest;
import com.ttbt.smartclass.model.entity.Course;
import com.ttbt.smartclass.model.entity.CourseChapter;
import com.ttbt.smartclass.model.entity.Section;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.service.CourseKnowledgeAutoSyncService;
import com.ttbt.smartclass.service.CourseManageAdminService;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.HomeworkSubmissionService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 课程管理端服务实现类。
 */
@Service
@Slf4j
public class CourseManageAdminServiceImpl implements CourseManageAdminService {

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private CourseChapterMapper courseChapterMapper;

    @Resource
    private SectionMapper sectionMapper;

    @Resource
    private CourseService courseService;

    @Resource
    private HomeworkSubmissionService homeworkSubmissionService;

    @Resource
    private CourseKnowledgeAutoSyncService courseKnowledgeAutoSyncService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCourse(CourseManageCreateRequest request, User loginUser) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "request is null");
        }
        List<CourseChapterSectionRequest.ChapterItem> chapters = request.getChapters();

        Long teacherId = resolveCreateTeacherId(request.getTeacherId(), loginUser);

        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setSubtitle(request.getSubtitle());
        course.setDescription(request.getDescription());
        course.setTeacherId(teacherId);
        course.setCourseType(request.getCourseType() == null ? 1 : request.getCourseType());
        course.setDifficulty(request.getDifficulty() == null ? 1 : request.getDifficulty());
        course.setStatus(request.getStatus() == null ? 0 : request.getStatus());
        course.setPrice(request.getPrice() == null ? BigDecimal.ZERO : request.getPrice());
        course.setOriginalPrice(request.getOriginalPrice() == null ? course.getPrice() : request.getOriginalPrice());
        course.setTags(request.getTags());
        course.setRequirements(request.getRequirements());
        course.setObjectives(request.getObjectives());
        course.setTargetAudience(request.getTargetAudience());
        course.setAiKnowleage(StringUtils.trim(request.getAiKnowleage()));
        if (request.getCategoryId() != null) {
            course.setCategoryId(request.getCategoryId());
            course.setCategory(String.valueOf(request.getCategoryId()));
        } else if (StringUtils.isNotBlank(request.getCategoryName())) {
            course.setCategory(request.getCategoryName().trim());
        }

        courseMapper.insert(course);
        if (course.getId() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "create course failed");
        }

        OutlineStat stat = new OutlineStat();
        if (chapters != null && !chapters.isEmpty()) {
            stat = rebuildOutline(course.getId(), chapters, loginUser.getId(), false);
        }
        updateCourseOutlineStat(course.getId(), stat);
        triggerKnowledgeSyncAfterCourseChange(course.getId(), stat);
        return course.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCourse(CourseUpdateRequest request, User loginUser) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "course_id is required");
        }

        courseService.assertCanManageCourse(loginUser, request.getId());

        Course oldCourse = courseMapper.selectById(request.getId());
        if (oldCourse == null || (oldCourse.getIsDelete() != null && oldCourse.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "course not found");
        }

        Course updateEntity = new Course();
        updateEntity.setId(request.getId());
        updateEntity.setTitle(request.getTitle());
        updateEntity.setSubtitle(request.getSubtitle());
        updateEntity.setDescription(request.getDescription());
        updateEntity.setCoverUrl(request.getCoverImage());
        updateEntity.setPrice(request.getPrice());
        updateEntity.setOriginalPrice(request.getOriginalPrice());
        updateEntity.setCourseType(request.getCourseType());
        updateEntity.setDifficulty(request.getDifficulty());
        updateEntity.setTags(request.getTags());
        updateEntity.setRequirements(request.getRequirements());
        updateEntity.setObjectives(request.getObjectives());
        updateEntity.setTargetAudience(request.getTargetAudience());
        updateEntity.setAiKnowleage(StringUtils.trim(request.getAiKnowleage()));

        if (request.getCategoryId() != null) {
            updateEntity.setCategoryId(request.getCategoryId());
            updateEntity.setCategory(String.valueOf(request.getCategoryId()));
        } else if (StringUtils.isNotBlank(request.getCategoryName())) {
            updateEntity.setCategory(request.getCategoryName().trim());
        }

        if (request.getStatus() != null) {
            updateEntity.setStatus(request.getStatus());
        }

        if (UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()) && request.getTeacherId() != null) {
            updateEntity.setTeacherId(request.getTeacherId());
        }

        courseMapper.updateById(updateEntity);

        OutlineStat stat = new OutlineStat();
        if (request.getChapters() != null) {
            if (request.getChapters().isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "chapters cannot be empty");
            }
            stat = rebuildOutline(request.getId(), request.getChapters(), loginUser.getId(), true);
            updateCourseOutlineStat(request.getId(), stat);
        }

        triggerKnowledgeSyncAfterCourseChange(request.getId(), stat);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCourse(Long courseId, User loginUser) {
        if (courseId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "course_id is required");
        }
        courseService.assertCanManageCourse(loginUser, courseId);

        Course updateCourse = new Course();
        updateCourse.setId(courseId);
        updateCourse.setIsDelete(1);
        courseMapper.updateById(updateCourse);

        List<Long> chapterIds = getActiveChapterIds(courseId);
        if (!chapterIds.isEmpty()) {
            UpdateWrapper<Section> sectionDeleteWrapper = new UpdateWrapper<>();
            sectionDeleteWrapper.in("chapter_id", chapterIds)
                    .eq("is_delete", 0)
                    .set("is_delete", 1);
            sectionMapper.update(null, sectionDeleteWrapper);
        }

        UpdateWrapper<CourseChapter> chapterDeleteWrapper = new UpdateWrapper<>();
        chapterDeleteWrapper.eq("course_id", courseId)
                .eq("is_delete", 0)
                .set("is_delete", 1);
        courseChapterMapper.update(null, chapterDeleteWrapper);
        return true;
    }

    @Override
    public String handleVideoUpload(MultipartFile file, String videoUrl, User loginUser) {
        boolean hasFile = file != null && !file.isEmpty();
        boolean hasUrl = StringUtils.isNotBlank(videoUrl);
        if (!hasFile && !hasUrl) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "videoUrl or file is required");
        }

        if (hasFile) {
            return courseService.uploadVideo(file, loginUser);
        }
        return videoUrl.trim();
    }

    @Override
    public void reviewHomework(HomeworkGradeRequest request, User loginUser) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "grade request is null");
        }
        homeworkSubmissionService.gradeHomework(request, loginUser.getId());
    }

    private void triggerKnowledgeSyncAfterCourseChange(Long courseId, OutlineStat stat) {
        courseKnowledgeAutoSyncService.triggerCourseIntroSync(courseId);
        if (stat != null) {
            courseKnowledgeAutoSyncService.triggerCourseStructureSync(courseId, stat.chapterIds, stat.sectionIds);
        }
    }

    private Long resolveCreateTeacherId(Long requestTeacherId, User loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "login required");
        }

        String role = loginUser.getUserRole();
        if (UserConstant.ADMIN_ROLE.equals(role)) {
            if (requestTeacherId == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "teacherId is required");
            }
            return requestTeacherId;
        }
        if (UserConstant.TEACHER_ROLE.equals(role)) {
            if (requestTeacherId != null && !requestTeacherId.equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "teacher can only create own course");
            }
            return loginUser.getId();
        }
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "no permission to manage course");
    }

    private OutlineStat rebuildOutline(Long courseId,
                                      List<CourseChapterSectionRequest.ChapterItem> chapters,
                                      Long operatorId,
                                      boolean clearOldOutline) {
        if (chapters == null || chapters.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "chapters cannot be empty");
        }

        if (clearOldOutline) {
            List<Long> chapterIds = getActiveChapterIds(courseId);
            if (!chapterIds.isEmpty()) {
                UpdateWrapper<Section> sectionDeleteWrapper = new UpdateWrapper<>();
                sectionDeleteWrapper.in("chapter_id", chapterIds)
                        .eq("is_delete", 0)
                        .set("is_delete", 1);
                sectionMapper.update(null, sectionDeleteWrapper);
            }

            UpdateWrapper<CourseChapter> chapterDeleteWrapper = new UpdateWrapper<>();
            chapterDeleteWrapper.eq("course_id", courseId)
                    .eq("is_delete", 0)
                    .set("is_delete", 1);
            courseChapterMapper.update(null, chapterDeleteWrapper);
        }

        OutlineStat stat = new OutlineStat();
        int chapterOrder = 1;
        for (CourseChapterSectionRequest.ChapterItem chapterItem : chapters) {
            if (chapterItem == null || StringUtils.isBlank(chapterItem.getTitle())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "chapter title cannot be blank");
            }
            if (chapterItem.getSections() == null || chapterItem.getSections().isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "section list cannot be empty");
            }

            CourseChapter chapter = new CourseChapter();
            chapter.setCourseId(courseId);
            chapter.setTitle(chapterItem.getTitle().trim());
            chapter.setDescription(chapterItem.getDescription());
            chapter.setSort(chapterItem.getSortOrder() == null ? chapterOrder : chapterItem.getSortOrder());
            chapter.setAdminId(operatorId);
            courseChapterMapper.insert(chapter);
            stat.chapterIds.add(chapter.getId());

            int sectionOrder = 1;
            for (CourseChapterSectionRequest.SectionItem sectionItem : chapterItem.getSections()) {
                if (sectionItem == null || StringUtils.isBlank(sectionItem.getTitle())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "section title cannot be blank");
                }
                String source = pickVideoSource(sectionItem);

                Section section = new Section();
                section.setChapterId(chapter.getId());
                section.setTitle(sectionItem.getTitle().trim());
                section.setAiKnowleage(StringUtils.trim(sectionItem.getAiKnowleage()));
                section.setType("video");
                section.setContentType("VIDEO");
                section.setVideoUrl(source);
                section.setResourceUrl(source);
                section.setResourceType(isHttpUrl(source) ? "URL" : "FILE");
                section.setVideoDuration(sectionItem.getVideoDuration() == null ? 0 : sectionItem.getVideoDuration());
                section.setSortOrder(sectionItem.getSortOrder() == null ? sectionOrder : sectionItem.getSortOrder());
                section.setIsFree(sectionItem.getIsFree() == null ? 0 : sectionItem.getIsFree());
                sectionMapper.insert(section);
                stat.sectionIds.add(section.getId());

                stat.totalSections += 1;
                stat.totalDuration += section.getVideoDuration() == null ? 0 : section.getVideoDuration();
                sectionOrder++;
            }
            stat.totalChapters += 1;
            chapterOrder++;
        }
        return stat;
    }

    private List<Long> getActiveChapterIds(Long courseId) {
        LambdaQueryWrapper<CourseChapter> chapterQuery = new LambdaQueryWrapper<>();
        chapterQuery.eq(CourseChapter::getCourseId, courseId)
                .eq(CourseChapter::getIsDelete, 0);
        List<CourseChapter> chapters = courseChapterMapper.selectList(chapterQuery);
        List<Long> chapterIds = new ArrayList<>();
        for (CourseChapter chapter : chapters) {
            chapterIds.add(chapter.getId());
        }
        return chapterIds;
    }

    private void updateCourseOutlineStat(Long courseId, OutlineStat stat) {
        Course update = new Course();
        update.setId(courseId);
        update.setTotalChapters(stat.totalChapters);
        update.setTotalSections(stat.totalSections);
        update.setTotalDuration(stat.totalDuration);
        courseMapper.updateById(update);
    }

    private String pickVideoSource(CourseChapterSectionRequest.SectionItem sectionItem) {
        String url = StringUtils.trimToEmpty(sectionItem.getVideoUrl());
        String localPath = StringUtils.trimToEmpty(sectionItem.getLocalVideoPath());
        if (StringUtils.isBlank(url) && StringUtils.isBlank(localPath)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "videoUrl or localVideoPath is required");
        }
        return StringUtils.isNotBlank(url) ? url : localPath;
    }

    private boolean isHttpUrl(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    private static final class OutlineStat {
        private int totalChapters;
        private int totalSections;
        private int totalDuration;
        private final List<Long> chapterIds = new ArrayList<>();
        private final List<Long> sectionIds = new ArrayList<>();
    }
}
