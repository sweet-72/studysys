package com.ttbt.smartclass.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.exception.ThrowUtils;
import com.ttbt.smartclass.model.dto.post.PostQueryRequest;
import com.ttbt.smartclass.model.dto.postfavour.PostFavourAddRequest;
import com.ttbt.smartclass.model.entity.Post;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.PostVO;
import com.ttbt.smartclass.service.PostFavourService;
import com.ttbt.smartclass.service.PostService;
import com.ttbt.smartclass.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子收藏接口
 */
@RestController
@RequestMapping("/post-favours")
@Slf4j
public class PostFavourController {

    @Resource
    private PostFavourService postFavourService;

    @Resource
    private PostService postService;

    @Resource
    private UserService userService;

    /**
     * 收藏帖子
     *
     * @param postFavourAddRequest 收藏请求
     * @param request              HTTP请求
     * @return 收藏结果
     */
    @PostMapping
    public BaseResponse<Boolean> addFavour(@RequestBody PostFavourAddRequest postFavourAddRequest,
                                          HttpServletRequest request) {
        if (postFavourAddRequest == null || postFavourAddRequest.getPostId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能收藏
        final User loginUser = userService.getLoginUser(request);
        long postId = postFavourAddRequest.getPostId();
        boolean result = postFavourService.addPostFavour(postId, loginUser.getId());
        return ResultUtils.success(result);
    }
    
    /**
     * 取消收藏帖子
     *
     * @param postId 帖子ID
     * @param request HTTP请求
     * @return 取消收藏结果
     */
    @DeleteMapping("/{post_id}")
    public BaseResponse<Boolean> cancelFavour(@PathVariable("post_id") Long postId,
                                             HttpServletRequest request) {
        if (postId == null || postId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能取消收藏
        final User loginUser = userService.getLoginUser(request);
        boolean result = postFavourService.cancelPostFavour(postId, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 获取我收藏的帖子列表
     *
     * @param postQueryRequest 查询请求
     * @param request          HTTP请求
     * @return 帖子列表
     */
    @GetMapping("/me/page")
    public BaseResponse<Page<PostVO>> listMyFavourPostByPage(PostQueryRequest postQueryRequest,
                                                         HttpServletRequest request) {
        if (postQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Post> postPage = postFavourService.listFavourPostByPage(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest), loginUser.getId());
        return ResultUtils.success(postService.getPostVOPage(postPage, request));
    }
    
    /**
     * 判断当前登录用户是否已收藏
     * 
     * @param postId 帖子id
     * @param request HTTP请求
     * @return 是否已收藏
     */
    @GetMapping("/{post_id}")
    public BaseResponse<Boolean> hasFavour(@PathVariable("post_id") Long postId, HttpServletRequest request) {
        if (postId == null || postId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser == null) {
            return ResultUtils.success(false);
        }
        boolean result = postFavourService.hasFavour(postId, loginUser.getId());
        return ResultUtils.success(result);
    }
}
