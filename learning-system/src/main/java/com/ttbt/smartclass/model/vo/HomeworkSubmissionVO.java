package com.ttbt.smartclass.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 作业提交信息 VO
 */
@Data
public class HomeworkSubmissionVO {

    /**
     * 提交 ID
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
     * 小节 ID
     */
    private Long sectionId;

    /**
     * 小节标题
     */
    private String sectionTitle;

    /**
     * 题目 ID
     */
    private Long questionId;

    /**
     * 题目内容
     */
    private String questionContent;

    /**
     * 题目类型
     */
    private String questionType;

    /**
     * 用户答案
     */
    private String answer;

    /**
     * 状态：0-未批改，1-已批改，2-需复查
     */
    private Integer status;

    /**
     * 得分
     */
    private Integer score;

    /**
     * 教师评语
     */
    private String feedback;

    /**
     * 批改教师 ID
     */
    private Long gradedBy;

    /**
     * 批改教师名称
     */
    private String gradedByName;

    /**
     * 批改时间
     */
    private Date gradedTime;

    /**
     * 提交时间
     */
    private Date createTime;
}
