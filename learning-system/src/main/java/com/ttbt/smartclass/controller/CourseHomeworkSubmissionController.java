package com.ttbt.smartclass.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.annotation.AuthCheck;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.constant.UserConstant;
import com.ttbt.smartclass.model.dto.HomeworkSubmissionReviewRequest;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.HomeworkSubmissionReviewDetailVO;
import com.ttbt.smartclass.model.vo.HomeworkSubmissionReviewPageVO;
import com.ttbt.smartclass.service.HomeworkSubmissionService;
import com.ttbt.smartclass.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 课程作业提交审核接口。
 * 负责教师和管理员分页查看作业提交记录、查看详情并完成批阅。
 */
@RestController
@RequestMapping("/course/homework/submission")
public class CourseHomeworkSubmissionController {

    @Resource
    private HomeworkSubmissionService homeworkSubmissionService;

    @Resource
    private UserService userService;

    /**
     * 分页查询课程作业提交审核列表。
     *
     * @param courseId 课程 ID，可选
     * @param sectionId 小节 ID，可选
     * @param homeworkId 作业题目 ID，可选
     * @param reviewStatus 批阅状态，可选
     * @param current 当前页码
     * @param pageSize 每页数量
     * @param httpRequest 当前 HTTP 请求
     * @return 作业提交审核分页数据
     */
    @GetMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Page<HomeworkSubmissionReviewPageVO>> listSubmissionVOByPage(
            @RequestParam(value = "course_id", required = false) Long courseId,
            @RequestParam(value = "sectionId", required = false) Long sectionId,
            @RequestParam(value = "homeworkId", required = false) Long homeworkId,
            @RequestParam(value = "reviewStatus", required = false) Integer reviewStatus,
            @RequestParam(value = "current", defaultValue = "1") Long current,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize,
            HttpServletRequest httpRequest
    ) {
        // 获取当前审核人，用于服务层校验课程管理权限并限定可见范围
        User loginUser = userService.getLoginUser(httpRequest);
        Page<HomeworkSubmissionReviewPageVO> page = homeworkSubmissionService.getSubmissionReviewPage(
                current,
                pageSize,
                courseId,
                sectionId,
                homeworkId,
                reviewStatus,
                loginUser
        );
        return ResultUtils.success(page);
    }

    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<HomeworkSubmissionReviewDetailVO> getSubmissionVO(
            @RequestParam("id") Long id,
            HttpServletRequest httpRequest
    ) {
        User loginUser = userService.getLoginUser(httpRequest);
        HomeworkSubmissionReviewDetailVO detail = homeworkSubmissionService.getSubmissionReviewDetail(id, loginUser);
        return ResultUtils.success(detail);
    }

    /**
     * 批阅指定的作业提交记录。
     *
     * @param request 批阅请求，包含提交记录、分数和评语
     * @param httpRequest 当前 HTTP 请求
     * @return 是否批阅成功
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Boolean> reviewSubmission(
            @Valid @RequestBody HomeworkSubmissionReviewRequest request,
            HttpServletRequest httpRequest
    ) {
        // 获取当前批阅人，并由服务层完成权限校验、分数写入和审核状态更新
        User loginUser = userService.getLoginUser(httpRequest);
        homeworkSubmissionService.reviewSubmission(
                request.getSubmissionId(),
                request.getScore(),
                request.getComment(),
                loginUser
        );
        return ResultUtils.success(true);
    }
}
