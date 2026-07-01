package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户每日单词学习关系实体。
 * @TableName user_daily_word
 */
@TableName(value = "user_daily_word")
@Data
public class UserDailyWord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long wordId;

    private Integer isStudied;

    private Integer isLiked;

    private Date likeTime;

    private Date studyTime;

    private String noteContent;

    private Date noteTime;

    private Integer masteryLevel;

    /**
     * Whether the word is currently marked as wrong for the user: 0-no, 1-yes.
     */
    private Integer isWrong;

    /**
     * The latest time the word was marked as wrong.
     */
    private Date wrongTime;

    /**
     * The latest time the user completed a review action for the word.
     */
    private Date reviewTime;

    private Date createTime;

    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
