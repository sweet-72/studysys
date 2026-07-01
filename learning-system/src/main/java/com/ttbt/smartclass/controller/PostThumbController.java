package com.ttbt.smartclass.controller;

import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.model.dto.postthumb.PostThumbAddRequest;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.service.PostThumbService;
import com.ttbt.smartclass.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子点赞接口
*/
@RestController
@RequestMapping("/post-thumbs")
@Slf4j
public class PostThumbController {

    @Resource
    private PostThumbService postThumbService;

    @Resource
    private UserService userService;

    /**
     * 点赞帖子
     *
     * @param postThumbAddRequest 点赞请求
     * @param request 请求
     * @return 点赞结果
     */
    @PostMapping
    public BaseResponse<Boolean> addThumb(@RequestBody PostThumbAddRequest postThumbAddRequest,
            HttpServletRequest request) {
        if (postThumbAddRequest == null || postThumbAddRequest.getPostId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long postId = postThumbAddRequest.getPostId();
        boolean result = postThumbService.addPostThumb(postId, loginUser.getId());
        return ResultUtils.success(result);
    }
    
    /**
     * 取消点赞帖子
     *
     * @param postId 帖子ID
     * @param request 请求
     * @return 取消点赞结果
     */
    @DeleteMapping("/{post_id}")
    public BaseResponse<Boolean> cancelThumb(@PathVariable("post_id") Long postId,
            HttpServletRequest request) {
        if (postId == null || postId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能取消点赞
        final User loginUser = userService.getLoginUser(request);
        boolean result = postThumbService.cancelPostThumb(postId, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 判断当前登录用户是否已点赞
     * 
     * @param postId 帖子id
     * @param request HTTP请求
     * @return 是否已点赞
     */
    @GetMapping("/{post_id}")
    public BaseResponse<Boolean> hasThumb(@PathVariable("post_id") Long postId, HttpServletRequest request) {
        if (postId == null || postId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser == null) {
            return ResultUtils.success(false);
        }
        boolean result = postThumbService.hasThumb(postId, loginUser.getId());
        return ResultUtils.success(result);
    }
}
