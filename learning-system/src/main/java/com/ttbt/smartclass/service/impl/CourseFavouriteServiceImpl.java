package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.CourseFavouriteMapper;
import com.ttbt.smartclass.model.entity.Course;
import com.ttbt.smartclass.model.entity.CourseFavourite;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.service.CourseFavouriteService;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.UserService;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class CourseFavouriteServiceImpl extends ServiceImpl<CourseFavouriteMapper, CourseFavourite>
        implements CourseFavouriteService {

    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_COURSE_ID = "course_id";

    @Resource
    private CourseService courseService;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long favourCourse(Long userId, Long courseId) {
        validateUserAndCourseIds(userId, courseId);
        assertUserExists(userId);
        assertCourseExists(courseId);

        if (hasFavoured(userId, courseId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "已经收藏过该课程");
        }

        CourseFavourite courseFavourite = new CourseFavourite();
        courseFavourite.setUserId(userId);
        courseFavourite.setCourseId(courseId);

        boolean result = this.save(courseFavourite);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "收藏失败");
        }
        return courseFavourite.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfavourCourse(Long userId, Long courseId) {
        validateUserAndCourseIds(userId, courseId);

        CourseFavourite favourite = getFavourite(userId, courseId);
        if (favourite == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未收藏该课程");
        }
        return this.removeById(favourite.getId());
    }

    @Override
    public boolean hasFavoured(Long userId, Long courseId) {
        if (userId == null || userId <= 0 || courseId == null || courseId <= 0) {
            return false;
        }
        return getFavourite(userId, courseId) != null;
    }

    @Override
    public List<Long> getUserFavouriteCourseIds(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }

        QueryWrapper<CourseFavourite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(COLUMN_USER_ID, userId);
        List<CourseFavourite> favourites = this.list(queryWrapper);
        return favourites.stream()
                .map(CourseFavourite::getCourseId)
                .collect(Collectors.toList());
    }

    @Override
    public long getUserFavouriteCount(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }

        QueryWrapper<CourseFavourite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(COLUMN_USER_ID, userId);
        return this.count(queryWrapper);
    }

    private void validateUserAndCourseIds(Long userId, Long courseId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程ID不合法");
        }
    }

    private void assertUserExists(Long userId) {
        User user = userService.getById(userId);
        if (user == null || (user.getIsDelete() != null && user.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
    }

    private void assertCourseExists(Long courseId) {
        Course course = courseService.getById(courseId);
        if (course == null || (course.getIsDelete() != null && course.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        }
    }

    private CourseFavourite getFavourite(Long userId, Long courseId) {
        QueryWrapper<CourseFavourite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(COLUMN_USER_ID, userId);
        queryWrapper.eq(COLUMN_COURSE_ID, courseId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }
}