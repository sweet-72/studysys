package com.ttbt.smartclass.model.dto.dailyword;

import java.io.Serializable;
import lombok.Data;

/**
 * 每日单词复习结果更新请求。
 */
@Data
public class DailyWordReviewUpdateRequest implements Serializable {

    /**
     * Optional new mastery level, valid values: 1-5.
     */
    private Integer masteryLevel;

    /**
     * Whether to remove the word from the wrong-word set after review.
     */
    private Boolean removeWrong;

    private static final long serialVersionUID = 1L;
}
