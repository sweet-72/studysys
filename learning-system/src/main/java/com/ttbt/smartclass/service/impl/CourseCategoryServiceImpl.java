package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.CourseCategoryMapper;
import com.ttbt.smartclass.model.entity.CourseCategory;
import com.ttbt.smartclass.service.CourseCategoryService;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 课程分类服务实现类
 */
@Service
@Slf4j
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory>
        implements CourseCategoryService {

    @Override
    public long addCourseCategory(CourseCategory courseCategory, Long adminId) {
        if (courseCategory == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        validCourseCategory(courseCategory, true);
        courseCategory.setAdminId(adminId);
        if (courseCategory.getParentId() == null) {
            courseCategory.setParentId(0L);
        }
        if (courseCategory.getSort() == null) {
            courseCategory.setSort(0);
        }

        boolean result = this.save(courseCategory);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "添加失败");
        }
        return courseCategory.getId();
    }

    @Override
    public List<CourseCategory> getTopCategories() {
        LambdaQueryWrapper<CourseCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseCategory::getParentId, 0L);
        queryWrapper.eq(CourseCategory::getIsDelete, 0);
        queryWrapper.orderByDesc(CourseCategory::getSort);
        return this.list(queryWrapper);
    }

    @Override
    public List<CourseCategory> getSubCategories(Long parentId) {
        if (parentId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "父分类ID不能为空");
        }

        LambdaQueryWrapper<CourseCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseCategory::getParentId, parentId);
        queryWrapper.eq(CourseCategory::getIsDelete, 0);
        queryWrapper.orderByDesc(CourseCategory::getSort);
        return this.list(queryWrapper);
    }

    @Override
    public List<CourseCategory> getCategoryWithChildren(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类ID不能为空");
        }

        CourseCategory category = this.getById(categoryId);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "分类不存在");
        }

        List<CourseCategory> children = getSubCategories(categoryId);
        List<CourseCategory> result = new ArrayList<>();
        result.add(category);
        result.addAll(children);
        return result;
    }

    private void validCourseCategory(CourseCategory courseCategory, boolean add) {
        if (courseCategory == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String name = courseCategory.getName();
        if (add && StringUtils.isBlank(name)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类名称不能为空");
        }
        if (StringUtils.isNotBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类名称过长");
        }
    }
}