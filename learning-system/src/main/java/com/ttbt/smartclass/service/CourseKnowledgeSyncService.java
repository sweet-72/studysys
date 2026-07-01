package com.ttbt.smartclass.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.model.entity.CourseKnowledgeSync;
import com.ttbt.smartclass.model.vo.CourseKnowledgeSyncStatusVO;
import java.util.Date;
import java.util.List;

/**
 * 课程知识同步服务
 */
public interface CourseKnowledgeSyncService {

    List<CourseKnowledgeSync> syncCourse(Long courseId);

    CourseKnowledgeSync syncCourseIntro(Long courseId);

    CourseKnowledgeSync syncChapter(Long chapterId);

    CourseKnowledgeSync syncSection(Long sectionId);

    CourseKnowledgeSync syncMaterial(Long materialId);

    CourseKnowledgeSync syncQuestion(Long questionId);

    CourseKnowledgeSync retrySync(Long syncId);

    CourseKnowledgeSyncStatusVO querySyncStatus(Long courseId);

    Page<CourseKnowledgeSync> pageSyncRecords(long current,
                                              long pageSize,
                                              Long courseId,
                                              String sourceType,
                                              String syncStatus,
                                              Date startTime,
                                              Date endTime,
                                              List<Long> allowedCourseIds);

    CourseKnowledgeSync refreshSyncStatus(Long syncId);
}
