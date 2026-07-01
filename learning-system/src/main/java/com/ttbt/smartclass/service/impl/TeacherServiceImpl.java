package com.ttbt.smartclass.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.constant.CommonConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.TeacherMapper;
import com.ttbt.smartclass.model.dto.teacher.TeacherQueryRequest;
import com.ttbt.smartclass.model.entity.Course;
import com.ttbt.smartclass.model.entity.Teacher;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.TeacherVO;
import com.ttbt.smartclass.model.vo.UserVO;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.TeacherService;
import com.ttbt.smartclass.service.UserService;
import com.ttbt.smartclass.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 讲师服务实现
 */
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    private static final int DEFAULT_RECOMMEND_LIMIT = 5;
    private static final int MAX_RECOMMEND_LIMIT = 20;

    @Resource
    private UserService userService;

    @Resource
    private CourseService courseService;

    @Override
    public void validTeacher(Teacher teacher, boolean add) {
        if (teacher == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "讲师信息不能为空");
        }

        String name = StringUtils.trimToNull(teacher.getName());
        String avatar = StringUtils.trimToNull(teacher.getAvatar());
        String title = StringUtils.trimToNull(teacher.getTitle());
        String introduction = StringUtils.trimToNull(teacher.getIntroduction());
        String expertise = StringUtils.trimToNull(teacher.getExpertise());
        Long userId = teacher.getUserId();

        if (add && StringUtils.isBlank(name)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "讲师姓名不能为空");
        }
        if (StringUtils.isNotBlank(name) && name.length() > 128) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "讲师姓名过长");
        }
        if (StringUtils.isNotBlank(avatar) && avatar.length() > 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "讲师头像链接过长");
        }
        if (StringUtils.isNotBlank(title) && title.length() > 128) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "讲师职称过长");
        }
        if (StringUtils.isNotBlank(introduction) && introduction.length() > 4096) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "讲师简介过长");
        }
        if (StringUtils.isNotBlank(expertise) && expertise.length() > 2048) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "讲师专业领域信息过长");
        }

        if (userId != null) {
            User linkedUser = userService.getById(userId);
            if (linkedUser == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "关联用户不存在");
            }
            LambdaQueryWrapper<Teacher> duplicateWrapper = new LambdaQueryWrapper<>();
            duplicateWrapper.eq(Teacher::getUserId, userId)
                    .eq(Teacher::getIsDelete, 0)
                    .ne(teacher.getId() != null && teacher.getId() > 0, Teacher::getId, teacher.getId());
            if (this.count(duplicateWrapper) > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户已绑定其他讲师");
            }
        }
    }

    @Override
    public QueryWrapper<Teacher> getQueryWrapper(TeacherQueryRequest teacherQueryRequest) {
        QueryWrapper<Teacher> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<Teacher> lambdaQueryWrapper = queryWrapper.lambda();
        if (teacherQueryRequest == null) {
            lambdaQueryWrapper.eq(Teacher::getIsDelete, 0)
                    .orderByDesc(Teacher::getId);
            return queryWrapper;
        }

        Long id = teacherQueryRequest.getId();
        String name = StringUtils.trimToNull(teacherQueryRequest.getName());
        String title = StringUtils.trimToNull(teacherQueryRequest.getTitle());
        String expertise = StringUtils.trimToNull(teacherQueryRequest.getExpertise());
        Long userId = teacherQueryRequest.getUserId();
        Long adminId = teacherQueryRequest.getAdminId();
        String sortField = StringUtils.trimToNull(teacherQueryRequest.getSortField());
        String sortOrder = StringUtils.trimToNull(teacherQueryRequest.getSortOrder());

        lambdaQueryWrapper.eq(id != null && id > 0, Teacher::getId, id)
                .like(StringUtils.isNotBlank(name), Teacher::getName, name)
                .like(StringUtils.isNotBlank(title), Teacher::getTitle, title)
                .like(StringUtils.isNotBlank(expertise), Teacher::getExpertise, expertise)
                .eq(ObjectUtils.isNotEmpty(userId), Teacher::getUserId, userId)
                .eq(ObjectUtils.isNotEmpty(adminId), Teacher::getAdminId, adminId)
                .eq(Teacher::getIsDelete, 0);

        if (SqlUtils.validSortField(sortField)) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equalsIgnoreCase(
                    StringUtils.defaultIfBlank(sortOrder, CommonConstant.SORT_ORDER_ASC));
            queryWrapper.orderBy(true, isAsc, SqlUtils.normalizeSortField(sortField));
        } else {
            lambdaQueryWrapper.orderByDesc(Teacher::getId);
        }
        return queryWrapper;
    }

    @Override
    public TeacherVO getTeacherVO(Teacher teacher, HttpServletRequest request) {
        if (teacher == null) {
            return null;
        }

        TeacherVO teacherVO = new TeacherVO();
        BeanUtils.copyProperties(teacher, teacherVO);

        Long userId = teacher.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) {
                UserVO userVO = userService.getUserVO(user);
                teacherVO.setUserVO(userVO);
            }
        }

        Long teacherId = teacher.getId();
        LambdaQueryWrapper<Course> courseQueryWrapper = new LambdaQueryWrapper<>();
        courseQueryWrapper.eq(Course::getTeacherId, teacherId)
                .eq(Course::getIsDelete, 0)
                .eq(Course::getStatus, 1);

        List<Course> courseList = courseService.list(courseQueryWrapper);
        fillTeacherStats(teacherVO, courseList);
        return teacherVO;
    }

    @Override
    public List<TeacherVO> getTeacherVO(List<Teacher> teacherList, HttpServletRequest request) {
        if (CollUtil.isEmpty(teacherList)) {
            return new ArrayList<>();
        }

        Set<Long> userIdSet = teacherList.stream()
                .map(Teacher::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, List<User>> userIdUserListMap = new HashMap<>();
        if (CollUtil.isNotEmpty(userIdSet)) {
            List<User> userList = userService.listByIds(userIdSet);
            userIdUserListMap = userList.stream().collect(Collectors.groupingBy(User::getId));
        }

        Set<Long> teacherIdSet = teacherList.stream()
                .map(Teacher::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, List<Course>> teacherCourseMap = new HashMap<>();
        if (CollUtil.isNotEmpty(teacherIdSet)) {
            LambdaQueryWrapper<Course> courseQueryWrapper = new LambdaQueryWrapper<>();
            courseQueryWrapper.in(Course::getTeacherId, teacherIdSet)
                    .eq(Course::getIsDelete, 0)
                    .eq(Course::getStatus, 1);
            List<Course> allCourseList = courseService.list(courseQueryWrapper);
            teacherCourseMap = allCourseList.stream().collect(Collectors.groupingBy(Course::getTeacherId));
        }

        Map<Long, List<User>> finalUserIdUserListMap = userIdUserListMap;
        Map<Long, List<Course>> finalTeacherCourseMap = teacherCourseMap;
        return teacherList.stream().map(teacher -> {
            TeacherVO teacherVO = new TeacherVO();
            BeanUtils.copyProperties(teacher, teacherVO);

            Long userId = teacher.getUserId();
            if (userId != null && finalUserIdUserListMap.containsKey(userId)) {
                User user = finalUserIdUserListMap.get(userId).get(0);
                teacherVO.setUserVO(userService.getUserVO(user));
            }

            List<Course> courseList = finalTeacherCourseMap.getOrDefault(teacher.getId(), new ArrayList<>());
            fillTeacherStats(teacherVO, courseList);
            return teacherVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<TeacherVO> getTeacherVOPage(Page<Teacher> teacherPage, HttpServletRequest request) {
        List<Teacher> teacherList = teacherPage.getRecords();
        Page<TeacherVO> teacherVOPage = new Page<>(teacherPage.getCurrent(), teacherPage.getSize(), teacherPage.getTotal());
        teacherVOPage.setRecords(getTeacherVO(teacherList, request));
        return teacherVOPage;
    }

    @Override
    public long addTeacher(Teacher teacher, Long adminId) {
        if (teacher == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "讲师信息不能为空");
        }
        validTeacher(teacher, true);
        teacher.setAdminId(adminId);
        boolean result = this.save(teacher);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "新增讲师失败");
        }
        return teacher.getId();
    }

    @Override
    public List<TeacherVO> getRecommendTeachers(String expertise, int limit, HttpServletRequest request) {
        int safeLimit = limit <= 0 ? DEFAULT_RECOMMEND_LIMIT : Math.min(limit, MAX_RECOMMEND_LIMIT);
        QueryWrapper<Teacher> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<Teacher> lambdaQueryWrapper = queryWrapper.lambda();
        lambdaQueryWrapper.eq(Teacher::getIsDelete, 0)
                .like(StringUtils.isNotBlank(expertise), Teacher::getExpertise, StringUtils.trimToNull(expertise))
                .orderByDesc(Teacher::getId);
        queryWrapper.last("LIMIT " + safeLimit);
        List<Teacher> teacherList = this.list(queryWrapper);
        return this.getTeacherVO(teacherList, request);
    }

    private void fillTeacherStats(TeacherVO teacherVO, List<Course> courseList) {
        if (teacherVO == null) {
            return;
        }
        if (CollUtil.isEmpty(courseList)) {
            teacherVO.setCourseCount(0);
            teacherVO.setStudentCount(0);
            teacherVO.setAverageRating(0.0);
            return;
        }

        teacherVO.setCourseCount(courseList.size());
        int totalStudentCount = courseList.stream()
                .map(Course::getStudentCount)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        teacherVO.setStudentCount(totalStudentCount);

        double totalRating = courseList.stream()
                .filter(course -> course.getRatingCount() != null
                        && course.getRatingCount() > 0
                        && course.getRatingScore() != null)
                .mapToDouble(course -> course.getRatingScore().doubleValue())
                .sum();
        long ratedCourseCount = courseList.stream()
                .filter(course -> course.getRatingCount() != null
                        && course.getRatingCount() > 0
                        && course.getRatingScore() != null)
                .count();
        if (ratedCourseCount > 0) {
            teacherVO.setAverageRating(BigDecimal.valueOf(totalRating / ratedCourseCount)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue());
        } else {
            teacherVO.setAverageRating(0.0);
        }
    }
}