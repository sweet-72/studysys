package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.exception.ThrowUtils;
import com.ttbt.smartclass.mapper.CourseSectionMapper;
import com.ttbt.smartclass.model.entity.CourseChapter;
import com.ttbt.smartclass.model.entity.CourseSection;
import com.ttbt.smartclass.service.CourseChapterService;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.CourseSectionService;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CourseSectionServiceImpl extends ServiceImpl<CourseSectionMapper, CourseSection> implements CourseSectionService {

    private static final String COLUMN_CHAPTER_ID = "chapter_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_VIDEO_URL = "video_url";
    private static final String COLUMN_VIDEO_DURATION = "video_duration";
    private static final String COLUMN_SORT_ORDER = "sort_order";
    private static final String COLUMN_IS_DELETE = "is_delete";

    @Resource
    private CourseService courseService;

    @Resource
    private CourseChapterService courseChapterService;

    @Override
    public List<CourseSection> getSectionsByCourseId(Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程ID不合法");
        }
        QueryWrapper<CourseSection> queryWrapper = getQueryWrapper(courseId, null);
        return this.list(queryWrapper);
    }

    @Override
    public List<CourseSection> getSectionsByChapterId(Long chapterId) {
        if (chapterId == null || chapterId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "章节ID不合法");
        }

        QueryWrapper<CourseSection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(COLUMN_CHAPTER_ID, chapterId);
        queryWrapper.eq(COLUMN_IS_DELETE, 0);
        queryWrapper.orderByAsc(COLUMN_SORT_ORDER);
        return this.list(queryWrapper);
    }

    @Override
    public long addCourseSection(CourseSection courseSection, Long adminId) {
        if (courseSection == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (adminId == null || adminId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "管理员ID不合法");
        }

        validCourseSection(courseSection, true);

        Long courseId = courseSection.getCourseId();
        Long chapterId = courseSection.getChapterId();
        if (courseService.getById(courseId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程不存在");
        }

        CourseChapter chapter = courseChapterService.getById(chapterId);
        if (chapter == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "章节不存在");
        }
        if (!courseId.equals(chapter.getCourseId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "章节不属于该课程");
        }

        if (courseSection.getSort() == null) {
            QueryWrapper<CourseSection> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(COLUMN_CHAPTER_ID, chapterId);
            queryWrapper.eq(COLUMN_IS_DELETE, 0);
            queryWrapper.orderByDesc(COLUMN_SORT_ORDER);
            queryWrapper.last("limit 1");
            CourseSection lastSection = this.getOne(queryWrapper);
            courseSection.setSort(lastSection == null || lastSection.getSort() == null ? 1 : lastSection.getSort() + 1);
        }

        // Keep the legacy entity compatible with the current table schema.
        courseSection.setCourseId(null);
        courseSection.setDescription(null);
        courseSection.setAdminId(null);
        if (courseSection.getIsDelete() == null) {
            courseSection.setIsDelete(0);
        }

        boolean result = this.save(courseSection);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "新增小节失败");
        return courseSection.getId();
    }

    @Override
    public void validCourseSection(CourseSection courseSection, boolean add) {
        if (courseSection == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String title = courseSection.getTitle();
        Long courseId = courseSection.getCourseId();
        Long chapterId = courseSection.getChapterId();
        String videoUrl = courseSection.getVideoUrl();
        Integer duration = courseSection.getDuration();

        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR, "小节标题不能为空");
            ThrowUtils.throwIf(courseId == null || courseId <= 0, ErrorCode.PARAMS_ERROR, "课程ID不合法");
            ThrowUtils.throwIf(chapterId == null || chapterId <= 0, ErrorCode.PARAMS_ERROR, "章节ID不合法");
            ThrowUtils.throwIf(StringUtils.isBlank(videoUrl), ErrorCode.PARAMS_ERROR, "视频地址不能为空");
        }

        if (StringUtils.isNotBlank(title) && title.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "小节标题过长");
        }
        if (StringUtils.isNotBlank(videoUrl) && videoUrl.length() > 255) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "视频地址过长");
        }
        if (duration != null && duration < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "视频时长不能为负数");
        }
    }

    @Override
    public QueryWrapper<CourseSection> getQueryWrapper(Long courseId, Long chapterId) {
        QueryWrapper<CourseSection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(COLUMN_IS_DELETE, 0);

        if (chapterId != null && chapterId > 0) {
            queryWrapper.eq(COLUMN_CHAPTER_ID, chapterId);
        } else if (courseId != null && courseId > 0) {
            List<Long> chapterIds = listChapterIdsByCourseId(courseId);
            if (chapterIds.isEmpty()) {
                queryWrapper.apply("1 = 0");
                return queryWrapper;
            }
            queryWrapper.in(COLUMN_CHAPTER_ID, chapterIds);
        }

        queryWrapper.orderByAsc(COLUMN_CHAPTER_ID, COLUMN_SORT_ORDER);
        return queryWrapper;
    }

    @Override
    public int getTotalDuration(Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程ID不合法");
        }
        return getSectionsByCourseId(courseId).stream()
                .map(CourseSection::getDuration)
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .sum();
    }

    @Override
    public int countSections(Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程ID不合法");
        }
        QueryWrapper<CourseSection> queryWrapper = getQueryWrapper(courseId, null);
        return Math.toIntExact(this.count(queryWrapper));
    }

    private List<Long> listChapterIdsByCourseId(Long courseId) {
        List<CourseChapter> chapters = courseChapterService.getChaptersByCourseId(courseId);
        if (chapters == null || chapters.isEmpty()) {
            return Collections.emptyList();
        }
        return chapters.stream()
                .map(CourseChapter::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }
}