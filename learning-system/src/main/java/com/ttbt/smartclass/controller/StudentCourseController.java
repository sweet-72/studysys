package com.ttbt.smartclass.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.MyCourseVO;
import com.ttbt.smartclass.service.UserCourseService;
import com.ttbt.smartclass.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生端课程接口。
 */
@Slf4j
@RestController
@RequestMapping("/course")
public class StudentCourseController {

    @Resource
    private UserCourseService userCourseService;

    @Resource
    private UserService userService;

    @GetMapping("/my/page/vo")
    public BaseResponse<Page<MyCourseVO>> getMyCoursePage(@RequestParam(defaultValue = "1") long current,
                                                          @RequestParam(defaultValue = "10") long pageSize,
                                                          HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userCourseService.getMyCoursePage(loginUser.getId(), current, pageSize));
    }
}
