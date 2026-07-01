package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.CourseMaterialMapper;
import com.ttbt.smartclass.model.entity.Course;
import com.ttbt.smartclass.model.entity.CourseMaterial;
import com.ttbt.smartclass.service.CourseKnowledgeAutoSyncService;
import com.ttbt.smartclass.service.CourseMaterialService;
import com.ttbt.smartclass.service.CourseService;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class CourseMaterialServiceImpl extends ServiceImpl<CourseMaterialMapper, CourseMaterial>
        implements CourseMaterialService {

    private static final String COLUMN_COURSE_ID = "course_id";
    private static final String COLUMN_SORT = "sort";
    private static final String COLUMN_IS_DELETE = "is_delete";

    @Resource
    private CourseService courseService;

    @Resource
    private CourseKnowledgeAutoSyncService courseKnowledgeAutoSyncService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addCourseMaterial(CourseMaterial courseMaterial, Long adminId) {
        if (courseMaterial == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseMaterial is null");
        }
        if (adminId == null || adminId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid admin_id");
        }

        validCourseMaterial(courseMaterial, true);
        assertCourseExists(courseMaterial.getCourseId());

        courseMaterial.setAdminId(adminId);
        if (courseMaterial.getDownloadCount() == null) {
            courseMaterial.setDownloadCount(0);
        }
        if (courseMaterial.getIsDelete() == null) {
            courseMaterial.setIsDelete(0);
        }

        boolean result = this.save(courseMaterial);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "create material failed");
        }
        courseKnowledgeAutoSyncService.triggerMaterialSync(courseMaterial.getCourseId(), courseMaterial.getId());
        return courseMaterial.getId();
    }

    @Override
    public boolean updateById(CourseMaterial entity) {
        if (entity == null || entity.getId() == null) {
            return false;
        }
        CourseMaterial oldMaterial = this.getById(entity.getId());
        boolean result = super.updateById(entity);
        if (result) {
            Long courseId = entity.getCourseId() != null ? entity.getCourseId()
                    : oldMaterial == null ? null : oldMaterial.getCourseId();
            courseKnowledgeAutoSyncService.triggerMaterialSync(courseId, entity.getId());
        }
        return result;
    }

    @Override
    public List<CourseMaterial> getMaterialsByCourseId(Long courseId) {
        validateCourseId(courseId);
        assertCourseExists(courseId);

        QueryWrapper<CourseMaterial> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(COLUMN_COURSE_ID, courseId);
        queryWrapper.eq(COLUMN_IS_DELETE, 0);
        queryWrapper.orderByAsc(COLUMN_SORT);
        return this.list(queryWrapper);
    }

    @Override
    public Page<CourseMaterial> listMaterialsByPage(Long courseId, long current, long size) {
        validateCourseId(courseId);
        assertCourseExists(courseId);

        QueryWrapper<CourseMaterial> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(COLUMN_COURSE_ID, courseId);
        queryWrapper.eq(COLUMN_IS_DELETE, 0);
        queryWrapper.orderByAsc(COLUMN_SORT);
        return this.page(new Page<>(current, size), queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementDownloadCount(Long materialId) {
        if (materialId == null || materialId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid materialId");
        }

        CourseMaterial material = this.getById(materialId);
        if (material == null || (material.getIsDelete() != null && material.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "material not found");
        }

        material.setDownloadCount((material.getDownloadCount() == null ? 0 : material.getDownloadCount()) + 1);
        return super.updateById(material);
    }

    private void validCourseMaterial(CourseMaterial courseMaterial, boolean add) {
        if (courseMaterial == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long courseId = courseMaterial.getCourseId();
        String title = courseMaterial.getTitle();
        String fileUrl = courseMaterial.getFileUrl();

        if (add) {
            if (courseId == null || courseId <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid course_id");
            }
            if (StringUtils.isBlank(title)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "material title cannot be blank");
            }
            if (StringUtils.isBlank(fileUrl)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "file_url cannot be blank");
            }
        }

        if (StringUtils.isNotBlank(title) && title.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "material title too long");
        }
    }

    private void validateCourseId(Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid course_id");
        }
    }

    private void assertCourseExists(Long courseId) {
        Course course = courseService.getById(courseId);
        if (course == null || (course.getIsDelete() != null && course.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "course not found");
        }
    }
}
