package com.ttbt.smartclass.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.model.dto.CourseRatingRequest;
import com.ttbt.smartclass.model.entity.CourseRating;
import com.ttbt.smartclass.model.vo.CourseRatingStatsVO;
import com.ttbt.smartclass.model.vo.CourseRatingVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 课程评分服务接口
 */
public interface CourseRatingService extends IService<CourseRating> {

    /**
     * 对课程进行评分
     *
     * @param request 评分请求
     * @param userId 用户 ID
     */
    void rateCourse(CourseRatingRequest request, Long userId);

    /**
     * 获取课程评分列表
     *
     * @param courseId 课程 ID
     * @param current 当前页
     * @param pageSize 每页大小
     * @return 分页评分列表
     */
    Page<CourseRatingVO> getCourseRatings(Long courseId, Long current, Long pageSize);

    /**
     * 获取课程评分统计
     *
     * @param courseId 课程 ID
     * @return 评分统计信息
     */
    CourseRatingStatsVO getCourseRatingStats(Long courseId);

    /**
     * 用户是否已评分
     *
     * @param courseId 课程 ID
     * @param userId 用户 ID
     * @return true-已评分，false-未评分
     */
    boolean hasRated(Long courseId, Long userId);

    /**
     * 点赞/取消点赞评价
     *
     * @param ratingId 评分 ID
     * @param userId 用户 ID
     */
    void toggleHelpful(Long ratingId, Long userId);
}
