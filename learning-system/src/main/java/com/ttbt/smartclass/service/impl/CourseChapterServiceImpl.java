package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.exception.ThrowUtils;
import com.ttbt.smartclass.mapper.CourseChapterMapper;
import com.ttbt.smartclass.model.entity.CourseChapter;
import com.ttbt.smartclass.service.CourseChapterService;
import com.ttbt.smartclass.service.CourseKnowledgeAutoSyncService;
import com.ttbt.smartclass.service.CourseService;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CourseChapterServiceImpl extends ServiceImpl<CourseChapterMapper, CourseChapter> implements CourseChapterService {

    private static final String COLUMN_COURSE_ID = "course_id";
    private static final String COLUMN_SORT_ORDER = "sort_order";
    private static final String COLUMN_IS_DELETE = "is_delete";

    @Resource
    private CourseService courseService;

    @Resource
    private CourseKnowledgeAutoSyncService courseKnowledgeAutoSyncService;

    @Override
    public List<CourseChapter> getChaptersByCourseId(Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid course_id");
        }
        QueryWrapper<CourseChapter> queryWrapper = getQueryWrapper(courseId);
        return this.list(queryWrapper);
    }

    @Override
    public long addCourseChapter(CourseChapter courseChapter, Long adminId) {
        if (courseChapter == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseChapter is null");
        }
        if (adminId == null || adminId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid admin_id");
        }

        validCourseChapter(courseChapter, true);

        Long courseId = courseChapter.getCourseId();
        if (courseService.getById(courseId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "course not found");
        }

        courseChapter.setAdminId(adminId);
        if (courseChapter.getIsDelete() == null) {
            courseChapter.setIsDelete(0);
        }

        if (courseChapter.getSort() == null) {
            QueryWrapper<CourseChapter> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(COLUMN_COURSE_ID, courseId);
            queryWrapper.eq(COLUMN_IS_DELETE, 0);
            queryWrapper.orderByDesc(COLUMN_SORT_ORDER);
            queryWrapper.last("limit 1");

            CourseChapter lastChapter = this.getOne(queryWrapper);
            courseChapter.setSort(lastChapter == null || lastChapter.getSort() == null ? 1 : lastChapter.getSort() + 1);
        }

        boolean result = this.save(courseChapter);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "create chapter failed");
        courseKnowledgeAutoSyncService.triggerChapterSync(courseId, courseChapter.getId());
        return courseChapter.getId();
    }

    @Override
    public boolean updateById(CourseChapter entity) {
        if (entity == null || entity.getId() == null) {
            return false;
        }
        CourseChapter oldChapter = this.getById(entity.getId());
        boolean result = super.updateById(entity);
        if (result) {
            Long courseId = entity.getCourseId() != null ? entity.getCourseId()
                    : oldChapter == null ? null : oldChapter.getCourseId();
            courseKnowledgeAutoSyncService.triggerChapterSync(courseId, entity.getId());
        }
        return result;
    }

    @Override
    public void validCourseChapter(CourseChapter courseChapter, boolean add) {
        if (courseChapter == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String title = courseChapter.getTitle();
        Long courseId = courseChapter.getCourseId();

        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR, "chapter title cannot be blank");
            ThrowUtils.throwIf(courseId == null || courseId <= 0, ErrorCode.PARAMS_ERROR, "invalid course_id");
        }

        if (StringUtils.isNotBlank(title) && title.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "chapter title too long");
        }
    }

    @Override
    public QueryWrapper<CourseChapter> getQueryWrapper(Long courseId) {
        QueryWrapper<CourseChapter> queryWrapper = new QueryWrapper<>();
        if (courseId != null && courseId > 0) {
            queryWrapper.eq(COLUMN_COURSE_ID, courseId);
        }
        queryWrapper.eq(COLUMN_IS_DELETE, 0);
        queryWrapper.orderByAsc(COLUMN_SORT_ORDER);
        return queryWrapper;
    }
}