package com.ttbt.smartclass.controller;

import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.model.dto.HomeworkSubmitRequest;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.ExerciseQuestionVO;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.HomeworkSubmissionService;
import com.ttbt.smartclass.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 课后习题接口
 */
@Slf4j
@RestController
@RequestMapping("/exercise")
public class ExerciseController {

    @Resource
    private CourseService courseService;

    @Resource
    private HomeworkSubmissionService homeworkSubmissionService;

    @Resource
    private UserService userService;

    /**
     * 查询指定小节下对学生公开的练习题。
     *
     * @param sectionId 小节 ID
     * @param request 当前 HTTP 请求
     * @return 练习题列表
     */
    @GetMapping("/{sectionId}")
    public BaseResponse<List<ExerciseQuestionVO>> listBySection(@PathVariable Long sectionId,
                                                                HttpServletRequest request) {
        // 练习题需要登录后访问，题目可见范围由课程服务限定
        userService.getLoginUser(request);
        return ResultUtils.success(courseService.listPublicExercisesBySection(sectionId));
    }

    /**
     * 提交主观作业答案并生成待批阅记录。
     *
     * @param request 作业提交请求
     * @param httpRequest 当前 HTTP 请求
     * @return 作业提交记录 ID
     */
    @PostMapping("/submit")
    public BaseResponse<Long> submit(@RequestBody HomeworkSubmitRequest request,
                                     HttpServletRequest httpRequest) {
        // 以当前登录用户作为提交人，服务层负责保存答案和提交状态
        User loginUser = userService.getLoginUser(httpRequest);
        Long id = homeworkSubmissionService.submitHomework(request, loginUser.getId());
        return ResultUtils.success(id);
    }
}
