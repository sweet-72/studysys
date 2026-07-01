package com.ttbt.smartclass.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ttbt.smartclass.model.entity.UserCourse;
import com.ttbt.smartclass.model.vo.MyCourseVO;

/**
 * 用户选课 / 开始学习
 */
public interface UserCourseService extends IService<UserCourse> {

    /**
     * 开始学习：写入或更新 user_course
     *
     * @param userId 用户
     * @param courseId 课程
     */
    void startLearning(Long userId, Long courseId);

    /**
     * 当前登录用户的我的课程分页。
     *
     * @param userId 当前登录用户
     * @param current 当前页
     * @param pageSize 每页大小
     * @return 我的课程分页
     */
    Page<MyCourseVO> getMyCoursePage(Long userId, long current, long pageSize);
}
