package com.ttbt.smartclass.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 每日单词复习项视图。
 */
@Data
public class DailyWordReviewItemVO implements Serializable {

    private Long wordId;

    private String word;

    private String phonetic;

    private String translation;

    private Integer masteryLevel;

    private Integer isCollected;

    private Integer isWrong;

    private Date lastStudyTime;

    private Date reviewTime;

    private Date updateTime;

    private String note;

    private static final long serialVersionUID = 1L;
}
