package com.ttbt.smartclass.service.impl;

import com.ttbt.smartclass.service.CourseKnowledgeAutoSyncService;
import com.ttbt.smartclass.service.CourseKnowledgeSyncService;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 课程知识库轻量异步同步触发实现类。
 */
@Service
@Slf4j
public class CourseKnowledgeAutoSyncServiceImpl implements CourseKnowledgeAutoSyncService {

    @Resource
    @Lazy
    private CourseKnowledgeSyncService courseKnowledgeSyncService;

    @Override
    public void triggerCourseIntroSync(Long courseId) {
        // TODO: 当前项目未实际接入 Dify 知识库 / RAG 检索，本段代码暂时屏蔽。
        // 当前课程 AI 真实链路为：课程上下文组装 -> Prompt 注入 -> Dify 调用。
        // 为避免论文与实际系统不一致，先保留代码但不再自动触发 Dify Dataset 同步。
        // dispatchAfterCommit("course_intro", courseId, courseId,
        //         () -> courseKnowledgeSyncService.syncCourseIntro(courseId));
    }

    @Override
    public void triggerCourseStructureSync(Long courseId, Collection<Long> chapterIds, Collection<Long> sectionIds) {
        Set<Long> uniqueChapterIds = normalizeIds(chapterIds);
        for (Long chapterId : uniqueChapterIds) {
            triggerChapterSync(courseId, chapterId);
        }
        Set<Long> uniqueSectionIds = normalizeIds(sectionIds);
        for (Long sectionId : uniqueSectionIds) {
            triggerSectionSync(courseId, sectionId);
        }
    }

    @Override
    public void triggerChapterSync(Long courseId, Long chapterId) {
        // TODO: 当前项目未实际接入 Dify 知识库 / RAG 检索，本段代码暂时屏蔽。
        // 当前课程 AI 真实链路为：课程上下文组装 -> Prompt 注入 -> Dify 调用。
        // 为避免论文与实际系统不一致，先保留代码但不再自动触发 Dify Dataset 同步。
        // dispatchAfterCommit("chapter", courseId, chapterId,
        //         () -> courseKnowledgeSyncService.syncChapter(chapterId));
    }

    @Override
    public void triggerSectionSync(Long courseId, Long sectionId) {
        // TODO: 当前项目未实际接入 Dify 知识库 / RAG 检索，本段代码暂时屏蔽。
        // 当前课程 AI 真实链路为：课程上下文组装 -> Prompt 注入 -> Dify 调用。
        // 为避免论文与实际系统不一致，先保留代码但不再自动触发 Dify Dataset 同步。
        // dispatchAfterCommit("section", courseId, sectionId,
        //         () -> courseKnowledgeSyncService.syncSection(sectionId));
    }

    @Override
    public void triggerMaterialSync(Long courseId, Long materialId) {
        // TODO: 当前项目未实际接入 Dify 知识库 / RAG 检索，本段代码暂时屏蔽。
        // 当前课程 AI 真实链路为：课程上下文组装 -> Prompt 注入 -> Dify 调用。
        // 为避免论文与实际系统不一致，先保留代码但不再自动触发 Dify Dataset 同步。
        // dispatchAfterCommit("material", courseId, materialId,
        //         () -> courseKnowledgeSyncService.syncMaterial(materialId));
    }

    @Override
    public void triggerQuestionSync(Long courseId, Long questionId) {
        // TODO: 当前项目未实际接入 Dify 知识库 / RAG 检索，本段代码暂时屏蔽。
        // 当前课程 AI 真实链路为：课程上下文组装 -> Prompt 注入 -> Dify 调用。
        // 为避免论文与实际系统不一致，先保留代码但不再自动触发 Dify Dataset 同步。
        // dispatchAfterCommit("question", courseId, questionId,
        //         () -> courseKnowledgeSyncService.syncQuestion(questionId));
    }

    private void dispatchAfterCommit(String syncType, Long courseId, Long targetId, Runnable action) {
        if (targetId == null || targetId <= 0) {
            return;
        }
        Runnable task = () -> CompletableFuture.runAsync(() -> safeExecute(syncType, courseId, targetId, action));
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
            return;
        }
        task.run();
    }

    private void safeExecute(String syncType, Long courseId, Long targetId, Runnable action) {
        try {
            log.info("trigger knowledge sync, course_id={}, type={}, targetId={}", courseId, syncType, targetId);
            action.run();
        } catch (Exception e) {
            log.error("knowledge auto sync failed, course_id={}, type={}, targetId={}",
                    courseId, syncType, targetId, e);
        }
    }

    private Set<Long> normalizeIds(Collection<Long> ids) {
        Set<Long> result = new LinkedHashSet<>();
        if (ids == null || ids.isEmpty()) {
            return result;
        }
        for (Long id : ids) {
            if (id != null && id > 0) {
                result.add(id);
            }
        }
        return result;
    }
}
