package com.ttbt.smartclass.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ttbt.smartclass.model.dto.teacher.TeacherQueryRequest;
import com.ttbt.smartclass.model.entity.Teacher;
import com.ttbt.smartclass.model.vo.TeacherVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 讲师服务
 */
public interface TeacherService extends IService<Teacher> {

    /**
     * 校验讲师信息
     *
     * @param teacher 讲师实体
     * @param add     是否为新增校验
     */
    void validTeacher(Teacher teacher, boolean add);

    /**
     * 获取查询条件
     *
     * @param teacherQueryRequest 查询请求
     * @return 查询包装器
     */
    QueryWrapper<Teacher> getQueryWrapper(TeacherQueryRequest teacherQueryRequest);

    /**
     * 获取讲师视图对象
     *
     * @param teacher 讲师实体
     * @param request 请求上下文
     * @return 脱敏后的讲师视图
     */
    TeacherVO getTeacherVO(Teacher teacher, HttpServletRequest request);

    /**
     * 获取讲师视图列表
     *
     * @param teacherList 讲师列表
     * @param request     请求上下文
     * @return 讲师视图列表
     */
    List<TeacherVO> getTeacherVO(List<Teacher> teacherList, HttpServletRequest request);

    /**
     * 获取讲师分页视图
     *
     * @param teacherPage 讲师分页
     * @param request     请求上下文
     * @return 讲师视图分页
     */
    Page<TeacherVO> getTeacherVOPage(Page<Teacher> teacherPage, HttpServletRequest request);

    /**
     * 新增讲师
     *
     * @param teacher  讲师实体
     * @param adminId  管理员 id
     * @return 新增后的讲师 id
     */
    long addTeacher(Teacher teacher, Long adminId);

    /**
     * 获取推荐讲师列表
     *
     * @param expertise 专业领域关键字
     * @param limit     返回数量
     * @param request   请求上下文
     * @return 推荐讲师列表
     */
    List<TeacherVO> getRecommendTeachers(String expertise, int limit, HttpServletRequest request);
}