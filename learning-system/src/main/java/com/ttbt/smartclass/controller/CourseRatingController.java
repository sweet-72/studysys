package com.ttbt.smartclass.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.model.dto.CourseRatingRequest;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.CourseRatingStatsVO;
import com.ttbt.smartclass.model.vo.CourseRatingVO;
import com.ttbt.smartclass.service.CourseRatingService;
import com.ttbt.smartclass.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 未实现
 * 课程评分控制器
 */
@Slf4j
@RestController
@RequestMapping("/course/rating")
public class CourseRatingController {

    @Resource
    private CourseRatingService courseRatingService;

    @Resource
    private UserService userService;

    /**
     * 提交或更新当前用户对课程的评分。
     *
     * @param request 课程评分请求
     * @param httpRequest 当前 HTTP 请求
     * @return 是否评分成功
     */
    @PostMapping
    public BaseResponse<Boolean> rateCourse(@RequestBody CourseRatingRequest request,
                                            HttpServletRequest httpRequest) {
        // 以当前登录用户为评分人，避免前端伪造用户身份
        User loginUser = userService.getLoginUser(httpRequest);
        courseRatingService.rateCourse(request, loginUser.getId());
        return ResultUtils.success(true);
    }

    @GetMapping("/list")
    public BaseResponse<Page<CourseRatingVO>> getCourseRatings(@RequestParam Long courseId,
                                                               @RequestParam(defaultValue = "1") Long current,
                                                               @RequestParam(defaultValue = "10") Long pageSize) {
        Page<CourseRatingVO> ratingPage = courseRatingService.getCourseRatings(courseId, current, pageSize);
        return ResultUtils.success(ratingPage);
    }

    @GetMapping("/stats")
    public BaseResponse<CourseRatingStatsVO> getCourseRatingStats(@RequestParam Long courseId) {
        CourseRatingStatsVO statsVO = courseRatingService.getCourseRatingStats(courseId);
        return ResultUtils.success(statsVO);
    }

    /**
     * 判断当前用户是否已经评价过指定课程。
     *
     * @param courseId 课程 ID
     * @param httpRequest 当前 HTTP 请求
     * @return 是否已评分
     */
    @GetMapping("/has-rated")
    public BaseResponse<Boolean> hasRated(@RequestParam Long courseId,
                                          HttpServletRequest httpRequest) {
        // 仅查询当前登录用户自己的评分状态
        User loginUser = userService.getLoginUser(httpRequest);
        boolean rated = courseRatingService.hasRated(courseId, loginUser.getId());
        return ResultUtils.success(rated);
    }

    /**
     * 切换当前用户对课程评价的有用标记。
     *
     * @param ratingId 课程评价 ID
     * @param httpRequest 当前 HTTP 请求
     * @return 是否操作成功
     */
    @PostMapping("/helpful")
    public BaseResponse<Boolean> toggleHelpful(@RequestParam Long ratingId,
                                               HttpServletRequest httpRequest) {
        // 以当前登录用户为操作人，服务层负责处理点赞状态切换
        User loginUser = userService.getLoginUser(httpRequest);
        courseRatingService.toggleHelpful(ratingId, loginUser.getId());
        return ResultUtils.success(true);
    }
}
