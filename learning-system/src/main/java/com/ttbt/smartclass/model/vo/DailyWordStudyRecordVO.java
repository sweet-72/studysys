package com.ttbt.smartclass.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户每日单词学习记录视图。
 */
@Data
public class DailyWordStudyRecordVO implements Serializable {

    private Long wordId;

    private String word;

    private String phonetic;

    private String translation;

    private Integer isStudied;

    private Integer masteryLevel;

    private Date studyTime;

    private Date updateTime;

    private String note;

    private Integer isLiked;

    private Integer isCollected;

    private static final long serialVersionUID = 1L;
}
