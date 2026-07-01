package com.ttbt.smartclass.controller;

import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.model.dto.dailygoal.DailyGoalItemAddRequest;
import com.ttbt.smartclass.model.dto.dailygoal.DailyGoalItemCompleteRequest;
import com.ttbt.smartclass.model.dto.dailygoal.DailyGoalItemProgressUpdateRequest;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.DailyGoalTodayVO;
import com.ttbt.smartclass.service.UserDailyGoalService;
import com.ttbt.smartclass.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/daily-goal")
public class DailyGoalController {

    @Resource
    private UserDailyGoalService userDailyGoalService;

    @Resource
    private UserService userService;

    @GetMapping("/today")
    public BaseResponse<DailyGoalTodayVO> getTodayGoal(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userDailyGoalService.getOrCreateTodayGoal(loginUser.getId()));
    }

    @PostMapping("/item/update-progress")
    public BaseResponse<DailyGoalTodayVO> updateItemProgress(@RequestBody DailyGoalItemProgressUpdateRequest updateRequest,
                                                             HttpServletRequest request) {
        if (updateRequest == null || updateRequest.getItemId() == null || updateRequest.getCurrentValue() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userDailyGoalService.updateItemProgress(
                loginUser.getId(),
                updateRequest.getItemId(),
                updateRequest.getCurrentValue()));
    }

    @PostMapping("/item/complete")
    public BaseResponse<DailyGoalTodayVO> completeItem(@RequestBody DailyGoalItemCompleteRequest completeRequest,
                                                       HttpServletRequest request) {
        if (completeRequest == null || completeRequest.getItemId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userDailyGoalService.completeItem(loginUser.getId(), completeRequest.getItemId()));
    }

    @PostMapping("/item/cancel-complete")
    public BaseResponse<DailyGoalTodayVO> cancelCompleteItem(@RequestBody DailyGoalItemCompleteRequest completeRequest,
                                                             HttpServletRequest request) {
        if (completeRequest == null || completeRequest.getItemId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userDailyGoalService.cancelCompleteItem(loginUser.getId(), completeRequest.getItemId()));
    }

    @PostMapping("/item/add")
    public BaseResponse<DailyGoalTodayVO> addItem(@RequestBody DailyGoalItemAddRequest addRequest,
                                                  HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userDailyGoalService.addCustomItem(loginUser.getId(), addRequest));
    }
}
