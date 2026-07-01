package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.CourseRatingMapper;
import com.ttbt.smartclass.model.dto.CourseRatingRequest;
import com.ttbt.smartclass.model.entity.Course;
import com.ttbt.smartclass.model.entity.CourseRating;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.CourseRatingStatsVO;
import com.ttbt.smartclass.model.vo.CourseRatingVO;
import com.ttbt.smartclass.service.CourseRatingService;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 课程评分服务实现类
 */
@Slf4j
@Service
public class CourseRatingServiceImpl extends ServiceImpl<CourseRatingMapper, CourseRating> 
        implements CourseRatingService {

    @Resource
    private CourseService courseService;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rateCourse(CourseRatingRequest request, Long userId) {
        // 1. 检查课程是否存在
        Course course = courseService.getById(request.getCourseId());
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        }

        // 2. 检查是否已评分
        LambdaQueryWrapper<CourseRating> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseRating::getUserId, userId)
                .eq(CourseRating::getCourseId, request.getCourseId());
        CourseRating existingRating = this.getOne(queryWrapper);

        if (existingRating != null) {
            // 更新已有评分
            existingRating.setRating(request.getRating());
            existingRating.setContent(request.getContent());
            existingRating.setAnonymous(request.getAnonymous());
            this.updateById(existingRating);
        } else {
            // 创建新评分
            CourseRating rating = new CourseRating();
            rating.setUserId(userId);
            rating.setCourseId(request.getCourseId());
            rating.setRating(request.getRating());
            rating.setContent(request.getContent());
            rating.setAnonymous(request.getAnonymous());
            rating.setHelpfulCount(0);
            this.save(rating);
        }

        // 3. 更新课程评分统计
        updateCourseRatingStats(request.getCourseId());
    }

    @Override
    public Page<CourseRatingVO> getCourseRatings(Long courseId, Long current, Long pageSize) {
        // 1. 分页查询评分
        Page<CourseRating> ratingPage = new Page<>(current, pageSize);
        LambdaQueryWrapper<CourseRating> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseRating::getCourseId, courseId)
                .eq(CourseRating::getIsDelete, 0)
                .orderByDesc(CourseRating::getCreateTime);
        
        Page<CourseRating> page = this.page(ratingPage, queryWrapper);

        // 2. 转换为 VO
        List<CourseRating> ratings = page.getRecords();
        if (ratings.isEmpty()) {
            return new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        }

        // 批量查询用户信息
        List<Long> userIds = ratings.stream().map(CourseRating::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<CourseRatingVO> voList = ratings.stream().map(rating -> {
            CourseRatingVO vo = new CourseRatingVO();
            BeanUtils.copyProperties(rating, vo);
            vo.setAnonymous(rating.getAnonymous() == 1);
            
            User user = userMap.get(rating.getUserId());
            if (user != null) {
                if (rating.getAnonymous() == 1) {
                    vo.setUserName("匿名用户");
                    vo.setUserAvatar(null);
                } else {
                    vo.setUserName(user.getUserName());
                    vo.setUserAvatar(user.getUserAvatar());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());

        Page<CourseRatingVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public CourseRatingStatsVO getCourseRatingStats(Long courseId) {
        // 1. 查询该课程的所有评分
        LambdaQueryWrapper<CourseRating> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseRating::getCourseId, courseId)
                .eq(CourseRating::getIsDelete, 0);
        List<CourseRating> ratings = this.list(queryWrapper);

        CourseRatingStatsVO statsVO = new CourseRatingStatsVO();
        statsVO.setCourseId(courseId);
        
        if (ratings.isEmpty()) {
            statsVO.setTotalCount(0L);
            statsVO.setAvgScore(BigDecimal.ZERO);
            statsVO.setScore1Count(0);
            statsVO.setScore2Count(0);
            statsVO.setScore3Count(0);
            statsVO.setScore4Count(0);
            statsVO.setScore5Count(0);
            return statsVO;
        }

        // 2. 统计数据
        long totalCount = ratings.size();
        double avgRating = ratings.stream().mapToInt(CourseRating::getRating).average().orElse(0.0);
        
        Map<Integer, Long> scoreCountMap = ratings.stream()
                .collect(Collectors.groupingBy(CourseRating::getRating, Collectors.counting()));

        statsVO.setTotalCount(totalCount);
        statsVO.setAvgScore(BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP));
        statsVO.setScore1Count(scoreCountMap.getOrDefault(1, 0L).intValue());
        statsVO.setScore2Count(scoreCountMap.getOrDefault(2, 0L).intValue());
        statsVO.setScore3Count(scoreCountMap.getOrDefault(3, 0L).intValue());
        statsVO.setScore4Count(scoreCountMap.getOrDefault(4, 0L).intValue());
        statsVO.setScore5Count(scoreCountMap.getOrDefault(5, 0L).intValue());

        return statsVO;
    }

    @Override
    public boolean hasRated(Long courseId, Long userId) {
        LambdaQueryWrapper<CourseRating> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseRating::getUserId, userId)
                .eq(CourseRating::getCourseId, courseId);
        return this.count(queryWrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleHelpful(Long ratingId, Long userId) {
        // TODO: 实现点赞功能，需要额外的表记录用户点赞状态
        CourseRating rating = this.getById(ratingId);
        if (rating == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评分不存在");
        }

        // 简单实现：直接增加有用次数
        Integer helpfulCount = rating.getHelpfulCount() == null ? 0 : rating.getHelpfulCount();
        rating.setHelpfulCount(helpfulCount + 1);
        this.updateById(rating);
    }

    /**
     * 更新课程评分统计
     */
    private void updateCourseRatingStats(Long courseId) {
        CourseRatingStatsVO statsVO = getCourseRatingStats(courseId);
        
        // 更新课程表的评分字段
        Course course = courseService.getById(courseId);
        if (course != null) {
            course.setRatingScore(statsVO.getAvgScore().setScale(1, RoundingMode.HALF_UP));
            course.setRatingCount(statsVO.getTotalCount().intValue());
            courseService.updateById(course);
        }
    }
}
