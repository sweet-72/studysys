package com.ttbt.smartclass.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.annotation.AuthCheck;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.DeleteRequest;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.constant.UserConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.exception.ThrowUtils;
import com.ttbt.smartclass.model.dto.teacher.TeacherAddRequest;
import com.ttbt.smartclass.model.dto.teacher.TeacherQueryRequest;
import com.ttbt.smartclass.model.dto.teacher.TeacherUpdateRequest;
import com.ttbt.smartclass.model.entity.Teacher;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.CourseVO;
import com.ttbt.smartclass.model.vo.TeacherVO;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.TeacherService;
import com.ttbt.smartclass.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 讲师接口
 */
@RestController
@RequestMapping("/teacher")
@Slf4j
public class TeacherController {

    private static final long DEFAULT_CURRENT = 1L;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 20L;

    @Resource
    private TeacherService teacherService;

    @Resource
    private UserService userService;

    @Resource
    private CourseService courseService;

    /**
     * 管理员新增讲师信息。
     *
     * @param teacherAddRequest 讲师新增请求
     * @param request 当前 HTTP 请求
     * @return 新增讲师 ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addTeacher(@RequestBody TeacherAddRequest teacherAddRequest, HttpServletRequest request) {
        // 校验请求体不能为空，并转换为讲师实体进行字段校验
        ThrowUtils.throwIf(teacherAddRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(teacherAddRequest, teacher);
        teacherService.validTeacher(teacher, true);

        // 获取当前管理员用户，记录讲师创建操作人
        User loginUser = userService.getLoginUser(request);
        long teacherId = teacherService.addTeacher(teacher, loginUser.getId());
        return ResultUtils.success(teacherId);
    }

    /**
     * 管理员删除指定讲师。
     *
     * @param deleteRequest 讲师删除请求
     * @param request 当前 HTTP 请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteTeacher(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 校验讲师 id，并确认讲师存在
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR, "讲师 id 非法");

        long id = deleteRequest.getId();
        Teacher teacher = teacherService.getById(id);
        ThrowUtils.throwIf(teacher == null, ErrorCode.NOT_FOUND_ERROR, "讲师不存在");

        // 删除讲师记录，失败时返回操作错误
        boolean result = teacherService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除讲师失败");
        return ResultUtils.success(true);
    }

    /**
     * 管理员更新讲师信息。
     *
     * @param teacherUpdateRequest 讲师更新请求
     * @param request 当前 HTTP 请求
     * @return 是否更新成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateTeacher(@RequestBody TeacherUpdateRequest teacherUpdateRequest,
                                               HttpServletRequest request) {
        // 校验讲师 id，并确认待更新讲师存在
        ThrowUtils.throwIf(teacherUpdateRequest == null || teacherUpdateRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR, "讲师 id 非法");

        long id = teacherUpdateRequest.getId();
        Teacher oldTeacher = teacherService.getById(id);
        ThrowUtils.throwIf(oldTeacher == null, ErrorCode.NOT_FOUND_ERROR, "讲师不存在");

        // 将更新请求转换为讲师实体，并按更新场景校验字段
        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(teacherUpdateRequest, teacher);
        teacherService.validTeacher(teacher, false);

        // 执行讲师信息更新，失败时返回操作错误
        boolean result = teacherService.updateById(teacher);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新讲师失败");
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Teacher> getTeacherById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "讲师 id 非法");
        Teacher teacher = teacherService.getById(id);
        ThrowUtils.throwIf(teacher == null, ErrorCode.NOT_FOUND_ERROR, "讲师不存在");
        return ResultUtils.success(teacher);
    }

    @GetMapping("/get/vo")
    public BaseResponse<TeacherVO> getTeacherVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "讲师 id 非法");
        Teacher teacher = teacherService.getById(id);
        ThrowUtils.throwIf(teacher == null, ErrorCode.NOT_FOUND_ERROR, "讲师不存在");
        return ResultUtils.success(teacherService.getTeacherVO(teacher, request));
    }

    /**
     * 分页查询讲师基础信息。
     *
     * @param teacherQueryRequest 讲师查询条件
     * @return 讲师分页数据
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Teacher>> listTeacherByPage(@RequestBody(required = false) TeacherQueryRequest teacherQueryRequest) {
        // 请求为空时使用默认查询条件，并限制分页大小
        TeacherQueryRequest safeRequest = teacherQueryRequest == null ? new TeacherQueryRequest() : teacherQueryRequest;
        long current = safeRequest.getCurrent() > 0 ? safeRequest.getCurrent() : DEFAULT_CURRENT;
        long size = safeRequest.getPageSize() > 0 ? safeRequest.getPageSize() : DEFAULT_PAGE_SIZE;
        ThrowUtils.throwIf(size > MAX_PAGE_SIZE, ErrorCode.PARAMS_ERROR, "pageSize 不能超过 20");

        // 根据查询条件构造分页查询，返回讲师实体分页
        Page<Teacher> teacherPage = teacherService.page(new Page<>(current, size),
                teacherService.getQueryWrapper(safeRequest));
        return ResultUtils.success(teacherPage);
    }

    /**
     * 分页查询讲师展示信息。
     *
     * @param teacherQueryRequest 讲师查询条件
     * @param request 当前 HTTP 请求
     * @return 讲师展示分页数据
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<TeacherVO>> listTeacherVOByPage(@RequestBody(required = false) TeacherQueryRequest teacherQueryRequest,
                                                             HttpServletRequest request) {
        // 请求为空时使用默认查询条件，并限制分页大小
        TeacherQueryRequest safeRequest = teacherQueryRequest == null ? new TeacherQueryRequest() : teacherQueryRequest;
        long current = safeRequest.getCurrent() > 0 ? safeRequest.getCurrent() : DEFAULT_CURRENT;
        long size = safeRequest.getPageSize() > 0 ? safeRequest.getPageSize() : DEFAULT_PAGE_SIZE;
        ThrowUtils.throwIf(size > MAX_PAGE_SIZE, ErrorCode.PARAMS_ERROR, "pageSize 不能超过 20");

        // 分页查询讲师实体，再转换为包含展示扩展信息的 VO 分页
        Page<Teacher> teacherPage = teacherService.page(new Page<>(current, size),
                teacherService.getQueryWrapper(safeRequest));
        Page<TeacherVO> teacherVOPage = teacherService.getTeacherVOPage(teacherPage, request);
        return ResultUtils.success(teacherVOPage);
    }

    @GetMapping("/courses/{teacherId}")
    public BaseResponse<List<CourseVO>> getTeacherCourses(@PathVariable Long teacherId, HttpServletRequest request) {
        if (teacherId == null || teacherId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "讲师 id 非法");
        }

        User loginUser = userService.getLoginUser(request);
        List<CourseVO> courseVOList = courseService.getCoursesByTeacher(teacherId, loginUser);
        return ResultUtils.success(courseVOList);
    }

    @GetMapping("/recommend")
    public BaseResponse<List<TeacherVO>> getRecommendTeachers(String expertise, HttpServletRequest request) {
        int limit = 5;
        List<TeacherVO> teacherVOList = teacherService.getRecommendTeachers(expertise, limit, request);
        return ResultUtils.success(teacherVOList);
    }
}
