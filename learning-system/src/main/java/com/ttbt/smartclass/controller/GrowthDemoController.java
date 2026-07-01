package com.ttbt.smartclass.controller;

import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.model.dto.ExpAddRequest;
import com.ttbt.smartclass.model.enums.ActionTypeEnum;
import com.ttbt.smartclass.service.UserLevelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 用户成长演示 Controller（用于展示如何调用成长系统）
 */
@Slf4j
@RestController
@RequestMapping("/demo/growth")
@Api(tags = "成长系统演示接口")
public class GrowthDemoController {

    @Resource
    private UserLevelService userLevelService;

    /**
     * 示例 1：完成课程小节
     */
    @PostMapping("/course/complete")
    @ApiOperation("示例：完成课程小节")
    public BaseResponse<Integer> completeCourseSection(
            @RequestParam Long userId,
            @RequestParam Long sectionId) {
        
        ExpAddRequest request = new ExpAddRequest();
        request.setUserId(userId);
        request.setActionType(ActionTypeEnum.COURSE_SECTION_COMPLETE.getCode());
        request.setRelatedId(sectionId);
        request.setRelatedType("course_section");
        request.setDescription("完成课程小节：" + sectionId);

        int exp = userLevelService.addExp(request);
        return ResultUtils.success(exp);
    }

    /**
     * 示例 2：发布帖子
     */
    @PostMapping("/post/create")
    @ApiOperation("示例：发布帖子")
    public BaseResponse<Integer> createPost(
            @RequestParam Long userId,
            @RequestParam Long postId) {
        
        ExpAddRequest request = new ExpAddRequest();
        request.setUserId(userId);
        request.setActionType(ActionTypeEnum.POST_CREATE.getCode());
        request.setRelatedId(postId);
        request.setRelatedType("post");
        request.setDescription("发布帖子：" + postId);

        int exp = userLevelService.addExp(request);
        return ResultUtils.success(exp);
    }

    /**
     * 示例 3：点赞帖子
     */
    @PostMapping("/like/post")
    @ApiOperation("示例：点赞帖子")
    public BaseResponse<Integer> likePost(
            @RequestParam Long userId,      // 点赞者 ID
            @RequestParam Long targetUserId, // 被点赞者 ID
            @RequestParam Long postId) {
        
        // 1. 点赞者获得经验
        ExpAddRequest giveRequest = new ExpAddRequest();
        giveRequest.setUserId(userId);
        giveRequest.setActionType(ActionTypeEnum.LIKE_GIVE.getCode());
        giveRequest.setRelatedId(postId);
        giveRequest.setRelatedType("post");
        giveRequest.setDescription("点赞帖子");
        userLevelService.addExp(giveRequest);

        // 2. 被点赞者获得经验
        ExpAddRequest receiveRequest = new ExpAddRequest();
        receiveRequest.setUserId(targetUserId);
        receiveRequest.setActionType(ActionTypeEnum.LIKE_RECEIVED.getCode());
        receiveRequest.setRelatedId(postId);
        receiveRequest.setRelatedType("post");
        receiveRequest.setDescription("帖子被点赞");
        int exp = userLevelService.addExp(receiveRequest);

        return ResultUtils.success(exp);
    }

    /**
     * 示例 4：用户登录时调用
     */
    @PostMapping("/user/login")
    @ApiOperation("示例：用户登录奖励")
    public BaseResponse<Void> userLogin(@RequestParam Long userId) {
        // 处理每日登录和连续登录奖励
        userLevelService.handleDailyLogin(userId);
        return ResultUtils.success(null);
    }

    /**
     * 示例 5：完成练习题
     */
    @PostMapping("/exercise/complete")
    @ApiOperation("示例：完成练习题")
    public BaseResponse<Integer> completeExercise(
            @RequestParam Long userId,
            @RequestParam Long exerciseId) {
        
        ExpAddRequest request = new ExpAddRequest();
        request.setUserId(userId);
        request.setActionType(ActionTypeEnum.EXERCISE_COMPLETE.getCode());
        request.setRelatedId(exerciseId);
        request.setRelatedType("exercise");
        request.setDescription("完成练习题");

        int exp = userLevelService.addExp(request);
        return ResultUtils.success(exp);
    }
}
