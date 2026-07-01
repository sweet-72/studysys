package com.ttbt.smartclass.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.annotation.AuthCheck;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.constant.UserConstant;
import com.ttbt.smartclass.model.dto.HomeworkGradeRequest;
import com.ttbt.smartclass.model.dto.HomeworkSubmitRequest;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.HomeworkSubmissionVO;
import com.ttbt.smartclass.service.HomeworkSubmissionService;
import com.ttbt.smartclass.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 作业提交控制器
 */
@Slf4j
@RestController
@RequestMapping("/homework")
public class HomeworkSubmissionController {

    @Resource
    private HomeworkSubmissionService homeworkSubmissionService;

    @Resource
    private UserService userService;

    /**
     * 提交当前用户的课程作业答案。
     *
     * @param request 作业提交请求
     * @param httpRequest 当前 HTTP 请求
     * @return 作业提交记录 ID
     */
    @PostMapping("/submit")
    public BaseResponse<Long> submitHomework(@RequestBody HomeworkSubmitRequest request,
                                             HttpServletRequest httpRequest) {
        // 获取当前登录用户，作业提交记录归属于该用户
        User loginUser = userService.getLoginUser(httpRequest);
        // 委托服务层校验题目归属、学习权限并保存或覆盖提交记录
        Long submissionId = homeworkSubmissionService.submitHomework(request, loginUser.getId());
        return ResultUtils.success(submissionId);
    }

    @GetMapping("/exercise/{sectionId}")
    public BaseResponse<List<HomeworkSubmissionVO>> getSectionExercises(@PathVariable Long sectionId,
                                                                        HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        List<HomeworkSubmissionVO> exercises = homeworkSubmissionService.getSectionExercises(sectionId, loginUser.getId());
        return ResultUtils.success(exercises);
    }

    @GetMapping("/my/list")
    public BaseResponse<Page<HomeworkSubmissionVO>> getMyHomeworkList(@RequestParam(defaultValue = "1") Long current,
                                                                      @RequestParam(defaultValue = "10") Long pageSize,
                                                                      @RequestParam(required = false) Integer status,
                                                                      HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        Page<HomeworkSubmissionVO> homeworkPage = homeworkSubmissionService.getMyHomeworkList(
                current, pageSize, loginUser.getId(), status);
        return ResultUtils.success(homeworkPage);
    }

    @GetMapping("/ungraded/list")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<Page<HomeworkSubmissionVO>> getUngradedHomeworkList(@RequestParam(defaultValue = "1") Long current,
                                                                            @RequestParam(defaultValue = "10") Long pageSize,
                                                                            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        Page<HomeworkSubmissionVO> homeworkPage = homeworkSubmissionService.getUngradedHomeworkList(
                current, pageSize, loginUser.getId());
        return ResultUtils.success(homeworkPage);
    }

    /**
     * 教师批改学生作业。
     *
     * @param request 作业批改请求
     * @param httpRequest 当前 HTTP 请求
     * @return 是否批改成功
     */
    @PostMapping("/grade")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<Boolean> gradeHomework(@RequestBody HomeworkGradeRequest request,
                                               HttpServletRequest httpRequest) {
        // 获取当前教师用户，服务层会校验该教师是否拥有作业所属课程
        User loginUser = userService.getLoginUser(httpRequest);
        homeworkSubmissionService.gradeHomework(request, loginUser.getId());
        return ResultUtils.success(true);
    }
}
