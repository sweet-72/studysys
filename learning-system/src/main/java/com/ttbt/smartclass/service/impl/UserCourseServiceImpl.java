package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.CourseMapper;
import com.ttbt.smartclass.mapper.UserCourseMapper;
import com.ttbt.smartclass.mapper.UserProgressMapper;
import com.ttbt.smartclass.model.entity.Course;
import com.ttbt.smartclass.model.entity.UserCourse;
import com.ttbt.smartclass.model.entity.UserProgress;
import com.ttbt.smartclass.model.vo.MyCourseVO;
import com.ttbt.smartclass.service.UserCourseService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 用户课程关系服务实现类
 */
@Service
public class UserCourseServiceImpl extends ServiceImpl<UserCourseMapper, UserCourse> implements UserCourseService {

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private UserProgressMapper userProgressMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startLearning(Long userId, Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || (course.getIsDelete() != null && course.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        }
        if (course.getStatus() == null || course.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程未上架");
        }

        LambdaQueryWrapper<UserCourse> qw = new LambdaQueryWrapper<>();
        qw.eq(UserCourse::getUserId, userId).eq(UserCourse::getCourseId, courseId);
        UserCourse existing = this.getOne(qw);
        Date now = new Date();
        if (existing == null) {
            UserCourse uc = new UserCourse();
            uc.setUserId(userId);
            uc.setCourseId(courseId);
            uc.setStatus(0);
            uc.setProgress(BigDecimal.ZERO);
            uc.setStartTime(now);
            uc.setLastLearnTime(now);
            this.save(uc);
        } else {
            existing.setLastLearnTime(now);
            if (existing.getStatus() == null || existing.getStatus() == 2) {
                existing.setStatus(0);
            }
            this.updateById(existing);
        }
    }

    @Override
    public Page<MyCourseVO> getMyCoursePage(Long userId, long current, long pageSize) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "please login first");
        }
        long safeCurrent = current <= 0 ? 1 : current;
        long safePageSize = pageSize <= 0 ? 10 : Math.min(pageSize, 50);

        LambdaQueryWrapper<UserCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCourse::getUserId, userId)
                .orderByDesc(UserCourse::getLastLearnTime)
                .orderByDesc(UserCourse::getUpdateTime)
                .orderByDesc(UserCourse::getCreateTime);
        Page<UserCourse> userCoursePage = this.page(new Page<>(safeCurrent, safePageSize), queryWrapper);

        Page<MyCourseVO> resultPage = new Page<>(userCoursePage.getCurrent(), userCoursePage.getSize(), userCoursePage.getTotal());
        List<UserCourse> records = userCoursePage.getRecords();
        if (records == null || records.isEmpty()) {
            resultPage.setRecords(Collections.emptyList());
            return resultPage;
        }

        Set<Long> courseIds = records.stream()
                .map(UserCourse::getCourseId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Long, Course> courseMap = courseIds.isEmpty()
                ? Collections.emptyMap()
                : courseMapper.selectBatchIds(courseIds).stream()
                .filter(course -> course != null && (course.getIsDelete() == null || course.getIsDelete() == 0))
                .collect(Collectors.toMap(Course::getId, Function.identity()));

        Map<Long, Integer> completedSectionsMap = queryCompletedSections(userId, courseIds);

        List<MyCourseVO> voList = records.stream()
                .map(userCourse -> toMyCourseVO(userCourse, courseMap.get(userCourse.getCourseId()), completedSectionsMap))
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
        resultPage.setRecords(voList);
        return resultPage;
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

    private MyCourseVO toMyCourseVO(UserCourse userCourse, Course course, Map<Long, Integer> completedSectionsMap) {
        if (userCourse == null || course == null) {
            return null;
        }
        MyCourseVO vo = new MyCourseVO();
        vo.setCourseId(course.getId());
        vo.setCourseTitle(course.getTitle());
        vo.setCourseCover(resolveCourseCover(course));
        vo.setProgressPercent(userCourse.getProgress() == null ? BigDecimal.ZERO : userCourse.getProgress());
        vo.setCompletedSections(completedSectionsMap.getOrDefault(course.getId(), 0));
        vo.setTotalSections(course.getTotalSections() == null ? 0 : course.getTotalSections());
        vo.setLastLearnTime(userCourse.getLastLearnTime());
        vo.setLearningStatus(resolveLearningStatus(userCourse.getStatus()));
        return vo;
    }

    private String resolveCourseCover(Course course) {
        if (course == null) {
            return null;
        }
        return StringUtils.isNotBlank(course.getCoverUrl()) ? course.getCoverUrl() : course.getCoverImage();
    }

    private String resolveLearningStatus(Integer status) {
        if (status == null) {
            return "NOT_STARTED";
        }
        switch (status) {
            case 1:
                return "COMPLETED";
            case 2:
                return "DROPPED";
            case 0:
            default:
                return "LEARNING";
        }
    }
}
