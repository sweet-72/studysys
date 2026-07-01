package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.exception.ThrowUtils;
import com.ttbt.smartclass.mapper.CourseReviewMapper;
import com.ttbt.smartclass.model.entity.Course;
import com.ttbt.smartclass.model.entity.CourseReview;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.CourseReviewVO;
import com.ttbt.smartclass.service.CourseReviewService;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.UserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class CourseReviewServiceImpl extends ServiceImpl<CourseReviewMapper, CourseReview> implements CourseReviewService {

    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_COURSE_ID = "course_id";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_IS_DELETE = "is_delete";
    private static final String COLUMN_CREATE_TIME = "create_time";
    private static final String COLUMN_ADMIN_REPLY = "admin_reply";
    private static final String COLUMN_ADMIN_REPLY_TIME = "admin_reply_time";
    private static final String SQL_LIKE_INCREMENT = "like_count = like_count + 1";

    @Resource
    private CourseService courseService;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addCourseReview(CourseReview courseReview, Long userId) {
        if (courseReview == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }

        Long courseId = courseReview.getCourseId();
        validateCourseId(courseId);
        validateReviewContent(courseReview.getContent());
        validateRating(courseReview.getRating());
        assertUserExists(userId);
        assertCourseExists(courseId);

        QueryWrapper<CourseReview> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(COLUMN_USER_ID, userId);
        queryWrapper.eq(COLUMN_COURSE_ID, courseId);
        queryWrapper.eq(COLUMN_IS_DELETE, 0);
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "您已经评价过该课程");
        }

        courseReview.setUserId(userId);
        courseReview.setLikeCount(0);
        courseReview.setReplyCount(0);
        courseReview.setAdminReply(null);
        courseReview.setAdminReplyTime(null);
        courseReview.setStatus(0);
        if (courseReview.getIsDelete() == null) {
            courseReview.setIsDelete(0);
        }

        boolean result = this.save(courseReview);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "新增评价失败");
        updateCourseRating(courseId);
        return courseReview.getId();
    }

    @Override
    public Page<CourseReviewVO> getReviewsByCourseId(Long courseId, long current, long pageSize) {
        validateCourseId(courseId);

        QueryWrapper<CourseReview> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(COLUMN_COURSE_ID, courseId);
        queryWrapper.eq(COLUMN_STATUS, 1);
        queryWrapper.eq(COLUMN_IS_DELETE, 0);
        queryWrapper.orderByDesc(COLUMN_CREATE_TIME);

        Page<CourseReview> reviewPage = this.page(new Page<>(current, pageSize), queryWrapper);
        Page<CourseReviewVO> reviewVOPage = new Page<>(reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
        reviewVOPage.setRecords(getReviewVO(reviewPage.getRecords()));
        return reviewVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeReview(Long reviewId) {
        CourseReview review = getReviewByIdOrThrow(reviewId);

        UpdateWrapper<CourseReview> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", review.getId());
        updateWrapper.setSql(SQL_LIKE_INCREMENT);
        return this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replyReview(Long reviewId, String replyContent, Long adminId) {
        CourseReview review = getReviewByIdOrThrow(reviewId);
        validateReplyContent(replyContent);
        if (adminId == null || adminId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "管理员ID不合法");
        }
        assertUserExists(adminId);

        UpdateWrapper<CourseReview> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", review.getId());
        updateWrapper.set(COLUMN_ADMIN_REPLY, replyContent);
        updateWrapper.set(COLUMN_ADMIN_REPLY_TIME, new Date());
        return this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateReviewStatus(Long reviewId, Integer status) {
        if (status == null || status < 0 || status > 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态值不合法");
        }

        CourseReview review = getReviewByIdOrThrow(reviewId);
        boolean needUpdateRating = (review.getStatus() == null || review.getStatus() != 1) && status == 1
                || review.getStatus() != null && review.getStatus() == 1 && status != 1;

        UpdateWrapper<CourseReview> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", review.getId());
        updateWrapper.set(COLUMN_STATUS, status);
        boolean result = this.update(updateWrapper);
        if (result && review.getCourseId() != null && needUpdateRating) {
            updateCourseRating(review.getCourseId());
        }
        return result;
    }

    @Override
    public QueryWrapper<CourseReview> getQueryWrapper(Long courseId, Long userId) {
        QueryWrapper<CourseReview> queryWrapper = new QueryWrapper<>();
        if (courseId != null && courseId > 0) {
            queryWrapper.eq(COLUMN_COURSE_ID, courseId);
        }
        if (userId != null && userId > 0) {
            queryWrapper.eq(COLUMN_USER_ID, userId);
        }
        queryWrapper.eq(COLUMN_IS_DELETE, 0);
        return queryWrapper;
    }

    @Override
    public Map<String, Object> getCourseRatingStats(Long courseId) {
        validateCourseId(courseId);

        QueryWrapper<CourseReview> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(COLUMN_COURSE_ID, courseId);
        queryWrapper.eq(COLUMN_STATUS, 1);
        queryWrapper.eq(COLUMN_IS_DELETE, 0);
        queryWrapper.select("COUNT(*) AS count", "AVG(rating) AS avgRating");

        Map<String, Object> statMap = this.getMap(queryWrapper);
        if (statMap == null || statMap.isEmpty()) {
            statMap = new HashMap<>();
            statMap.put("count", 0L);
            statMap.put("avgRating", 0D);
            return statMap;
        }

        Object countValue = statMap.get("count");
        Object avgRatingValue = statMap.get("avgRating");
        statMap.put("count", countValue == null ? 0L : ((Number) countValue).longValue());
        statMap.put("avgRating", avgRatingValue == null ? 0D : ((Number) avgRatingValue).doubleValue());
        return statMap;
    }

    @Override
    public CourseReviewVO getReviewVO(CourseReview courseReview) {
        if (courseReview == null) {
            return null;
        }

        CourseReviewVO reviewVO = new CourseReviewVO();
        reviewVO.setId(courseReview.getId());
        reviewVO.setUserId(courseReview.getUserId());
        reviewVO.setCourseId(courseReview.getCourseId());
        reviewVO.setContent(courseReview.getContent());
        reviewVO.setRating(courseReview.getRating());
        reviewVO.setLikeCount(courseReview.getLikeCount());
        reviewVO.setReplyCount(courseReview.getReplyCount());
        reviewVO.setAdminReply(courseReview.getAdminReply());
        reviewVO.setAdminReplyTime(courseReview.getAdminReplyTime());
        reviewVO.setStatus(courseReview.getStatus());
        reviewVO.setCreateTime(courseReview.getCreateTime());
        reviewVO.setUpdateTime(courseReview.getUpdateTime());

        User user = userService.getById(courseReview.getUserId());
        if (user != null && (user.getIsDelete() == null || user.getIsDelete() == 0)) {
            reviewVO.setUserName(user.getUserName());
            reviewVO.setUserAvatar(user.getUserAvatar());
        }

        Course course = courseService.getById(courseReview.getCourseId());
        if (course != null && (course.getIsDelete() == null || course.getIsDelete() == 0)) {
            reviewVO.setCourseTitle(course.getTitle());
        }
        return reviewVO;
    }

    @Override
    public List<CourseReviewVO> getReviewVO(List<CourseReview> reviewList) {
        if (reviewList == null || reviewList.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> userIds = reviewList.stream()
                .map(CourseReview::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMap = userService.listByIds(userIds).stream()
                    .filter(user -> user.getId() != null)
                    .collect(Collectors.toMap(User::getId, user -> user));
        }

        Set<Long> courseIds = reviewList.stream()
                .map(CourseReview::getCourseId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, Course> courseMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            courseMap = courseService.listByIds(courseIds).stream()
                    .filter(course -> course.getId() != null)
                    .collect(Collectors.toMap(Course::getId, course -> course));
        }

        Map<Long, User> finalUserMap = userMap;
        Map<Long, Course> finalCourseMap = courseMap;
        return reviewList.stream().map(review -> {
            CourseReviewVO reviewVO = new CourseReviewVO();
            reviewVO.setId(review.getId());
            reviewVO.setUserId(review.getUserId());
            reviewVO.setCourseId(review.getCourseId());
            reviewVO.setContent(review.getContent());
            reviewVO.setRating(review.getRating());
            reviewVO.setLikeCount(review.getLikeCount());
            reviewVO.setReplyCount(review.getReplyCount());
            reviewVO.setAdminReply(review.getAdminReply());
            reviewVO.setAdminReplyTime(review.getAdminReplyTime());
            reviewVO.setStatus(review.getStatus());
            reviewVO.setCreateTime(review.getCreateTime());
            reviewVO.setUpdateTime(review.getUpdateTime());

            User user = finalUserMap.get(review.getUserId());
            if (user != null && (user.getIsDelete() == null || user.getIsDelete() == 0)) {
                reviewVO.setUserName(user.getUserName());
                reviewVO.setUserAvatar(user.getUserAvatar());
            }

            Course course = finalCourseMap.get(review.getCourseId());
            if (course != null && (course.getIsDelete() == null || course.getIsDelete() == 0)) {
                reviewVO.setCourseTitle(course.getTitle());
            }
            return reviewVO;
        }).collect(Collectors.toList());
    }

    private void updateCourseRating(Long courseId) {
        Map<String, Object> ratingStats = getCourseRatingStats(courseId);
        long count = ((Number) ratingStats.get("count")).longValue();
        double avgRating = ((Number) ratingStats.get("avgRating")).doubleValue();
        BigDecimal ratingScore = BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP);
        courseService.updateCourseRating(courseId, ratingScore, (int) count);
    }

    private void validateCourseId(Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程ID不合法");
        }
    }

    private void validateReviewContent(String content) {
        if (StringUtils.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评价内容不能为空");
        }
        if (content.length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评价内容过长");
        }
    }

    private void validateReplyContent(String replyContent) {
        if (StringUtils.isBlank(replyContent)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "回复内容不能为空");
        }
        if (replyContent.length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "回复内容过长");
        }
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评分必须在 1 到 5 之间");
        }
    }

    private void assertUserExists(Long userId) {
        User user = userService.getById(userId);
        if (user == null || (user.getIsDelete() != null && user.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
    }

    private void assertCourseExists(Long courseId) {
        Course course = courseService.getById(courseId);
        if (course == null || (course.getIsDelete() != null && course.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "课程不存在");
        }
    }

    private CourseReview getReviewByIdOrThrow(Long reviewId) {
        if (reviewId == null || reviewId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评价ID不合法");
        }
        CourseReview review = this.getById(reviewId);
        if (review == null || (review.getIsDelete() != null && review.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评价不存在");
        }
        return review;
    }
}