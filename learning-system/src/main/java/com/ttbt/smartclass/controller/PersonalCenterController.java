package com.ttbt.smartclass.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.PersonalCenterOverviewVO;
import com.ttbt.smartclass.model.vo.PersonalFavouriteCourseVO;
import com.ttbt.smartclass.model.vo.PersonalLearningRecordVO;
import com.ttbt.smartclass.model.vo.LearningHistoryItemVO;
import com.ttbt.smartclass.service.PersonalCenterService;
import com.ttbt.smartclass.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人中心聚合接口。
 */
@RestController
@RequestMapping("/personal")
public class PersonalCenterController {

    @Resource
    private PersonalCenterService personalCenterService;

    @Resource
    private UserService userService;

    @GetMapping("/overview")
    public BaseResponse<PersonalCenterOverviewVO> getOverview(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(personalCenterService.getOverview(loginUser.getId()));
    }

    @GetMapping("/course-favourites/page")
    public BaseResponse<Page<PersonalFavouriteCourseVO>> getFavouriteCoursePage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long pageSize,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(personalCenterService.getFavouriteCoursePage(loginUser.getId(), current, pageSize));
    }

    @GetMapping("/learning-records/page")
    public BaseResponse<Page<PersonalLearningRecordVO>> getLearningRecordPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long pageSize,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(personalCenterService.getLearningRecordPage(loginUser.getId(), current, pageSize));
    }

    @GetMapping("/learning-history/page")
    public BaseResponse<Page<LearningHistoryItemVO>> getLearningHistoryPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long pageSize,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(personalCenterService.getLearningHistoryPage(loginUser.getId(), current, pageSize));
    }
}
