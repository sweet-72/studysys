package com.ttbt.smartclass.controller;

import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.service.DailyWordThumbService;
import com.ttbt.smartclass.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 每日单词点赞接口
 */
@RestController
@RequestMapping("/daily-word-thumbs")
@Slf4j
public class DailyWordThumbController {

    @Resource
    private DailyWordThumbService dailyWordThumbService;

    @Resource
    private UserService userService;

    /**
     * 点赞单词
     *
     * @param wordId 单词ID
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return 1-点赞成功；0-操作失败
     */
    @PostMapping("/{word_id}")
    public BaseResponse<Integer> thumbWord(@PathVariable("word_id") long wordId,
                                               HttpServletRequest request) {
        if (wordId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 执行点赞操作
        int result = dailyWordThumbService.thumbWord(wordId, loginUser);
        return ResultUtils.success(result);
    }
    
    /**
     * 取消点赞单词
     *
     * @param wordId 单词ID
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return 1-取消点赞成功；0-操作失败
     */
    @DeleteMapping("/{word_id}")
    public BaseResponse<Integer> cancelThumbWord(@PathVariable("word_id") long wordId,
                                               HttpServletRequest request) {
        if (wordId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 执行取消点赞操作
        int result = dailyWordThumbService.cancelThumbWord(wordId, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 查询当前用户是否点赞了单词
     *
     * @param wordId 单词ID
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return 是否点赞
     */
    @GetMapping("/{word_id}/status")
    public BaseResponse<Boolean> isThumbWord(@PathVariable("word_id") long wordId,
                                              HttpServletRequest request) {
        if (wordId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 查询是否点赞
        boolean result = dailyWordThumbService.isThumbWord(wordId, loginUser.getId());
        return ResultUtils.success(result);
    }
} 