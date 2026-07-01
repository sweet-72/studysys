package com.ttbt.smartclass.service;

import java.util.Collection;

/**
 * 课程知识库自动同步触发服务。
 */
public interface CourseKnowledgeAutoSyncService {

    void triggerCourseIntroSync(Long courseId);

    void triggerCourseStructureSync(Long courseId, Collection<Long> chapterIds, Collection<Long> sectionIds);

    void triggerChapterSync(Long courseId, Long chapterId);

    void triggerSectionSync(Long courseId, Long sectionId);

    void triggerMaterialSync(Long courseId, Long materialId);

    void triggerQuestionSync(Long courseId, Long questionId);
}
