package com.ttbt.smartclass.controller;

import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.model.dto.ExpAddRequest;
import com.ttbt.smartclass.model.vo.UserExpRecordVO;
import com.ttbt.smartclass.model.vo.UserLevelVO;
import com.ttbt.smartclass.service.UserLevelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 用户成长体系 Controller
 */
@Slf4j
@RestController
@RequestMapping("/user/exp")
@Api(tags = "用户成长体系接口")
public class UserExpController {

    @Resource
    private UserLevelService userLevelService;

    /**
     * 增加经验值（内部调用）
     */
    @PostMapping("/add")
    @ApiOperation("增加经验值")
    public BaseResponse<Integer> addExp(@Valid @RequestBody ExpAddRequest request) {
        log.info("收到增加经验请求：user_id={}, actionType={}", 
                request.getUserId(), request.getActionType());
        
        int expAdded = userLevelService.addExp(request);
        return ResultUtils.success(expAdded);
    }

    /**
     * 查询用户等级信息
     */
    @GetMapping("/level/{userId}")
    @ApiOperation("查询用户等级")
    public BaseResponse<UserLevelVO> getUserLevel(@PathVariable Long userId) {
        UserLevelVO userLevelVO = userLevelService.getUserLevel(userId);
        return ResultUtils.success(userLevelVO);
    }

    /**
     * 查询经验记录列表
     */
    @GetMapping("/records")
    @ApiOperation("查询经验记录")
    public BaseResponse<List<UserExpRecordVO>> getExpRecords(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long pageSize) {
        
        List<UserExpRecordVO> records = userLevelService.getExpRecords(userId, current, pageSize);
        return ResultUtils.success(records);
    }

    /**
     * 处理每日登录（可在登录时调用）
     */
    @PostMapping("/login/{userId}")
    @ApiOperation("处理每日登录")
    public BaseResponse<Void> handleDailyLogin(@PathVariable Long userId) {
        userLevelService.handleDailyLogin(userId);
        return ResultUtils.success(null);
    }
}
