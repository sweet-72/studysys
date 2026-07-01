package com.ttbt.smartclass.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 课程评分统计 VO
 */
@Data
public class CourseRatingStatsVO {

    /**
     * 课程 ID
     */
    private Long courseId;

    /**
     * 总评分数
     */
    private Long totalCount;

    /**
     * 平均评分
     */
    private BigDecimal avgScore;

    /**
     * 1 星数量
     */
    private Integer score1Count;

    /**
     * 2 星数量
     */
    private Integer score2Count;

    /**
     * 3 星数量
     */
    private Integer score3Count;

    /**
     * 4 星数量
     */
    private Integer score4Count;

    /**
     * 5 星数量
     */
    private Integer score5Count;
}
