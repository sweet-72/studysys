package com.ttbt.smartclass.model.vo;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import lombok.Data;

/**
 * 个人中心总览视图。
 */
@Data
public class PersonalCenterOverviewVO implements Serializable {

    private UserVO userInfo;

    private Long myCourseCount;

    private List<MyCourseVO> recentCourses = Collections.emptyList();

    private Long aiSessionCount;

    private List<ChatSessionVO> recentAiSessions = Collections.emptyList();

    private Long courseFavouriteCount;

    private Long postFavouriteCount;

    private Long wordBookCount;

    private Integer wordBookLearnedCount;

    private Integer wordBookReviewCount;

    private Long homeworkCount;

    private Long pendingHomeworkCount;

    private static final long serialVersionUID = 1L;
}
