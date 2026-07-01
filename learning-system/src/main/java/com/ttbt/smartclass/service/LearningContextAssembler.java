package com.ttbt.smartclass.service;

import com.ttbt.smartclass.model.dto.course.CourseLearningAiAskRequest;
import com.ttbt.smartclass.model.dto.course.CourseLearningAiContext;

/**
 * 学习上下文组装器
 */
public interface LearningContextAssembler {

    CourseLearningAiContext assemble(Long userId, CourseLearningAiAskRequest request);

    String buildSummaryText(CourseLearningAiContext context);

    void saveSnapshotIfNecessary(CourseLearningAiContext context);
}