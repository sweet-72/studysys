package com.ttbt.smartclass.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 课程评分信息 VO
 */
@Data
public class CourseRatingVO {

    /**
     * 评分 ID
     */
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 评分：1-5 分
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 是否匿名
     */
    private Boolean anonymous;

    /**
     * 有用次数
     */
    private Integer helpfulCount;

    /**
     * 评分时间
     */
    private Date createTime;

    /**
     * 用户是否点赞
     */
    private Boolean userHelpful;
}
