package com.ttbt.smartclass.model.dto.course;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 课程学习助手上下文
 */
@Data
public class CourseLearningAiContext implements Serializable {

    private Long userId;

    private Long courseId;

    private String courseTitle;

    private Long categoryId;

    private String categoryName;

    private Long chapterId;

    private String chapterTitle;

    private Long sectionId;

    private String sectionTitle;

    private String courseSummary;

    private String sectionSummary;

    private String aiKnowledgeContent;

    private String aiKnowledgeSource;

    private Integer courseProgressPercent;

    private Integer completedSections;

    private Integer totalSections;

    private String learningProgress;

    private String currentVideoProgress;

    private String recentHomeworkSummary;

    private String recentQuestionSummary;

    private String wrongQuestionSummary;

    private String studentGoal;

    private String summaryText;

    private Date snapshotTime;

    private static final long serialVersionUID = 1L;
}
