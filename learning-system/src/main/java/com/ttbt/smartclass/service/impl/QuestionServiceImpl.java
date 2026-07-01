package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.mapper.QuestionMapper;
import com.ttbt.smartclass.model.entity.CourseChapter;
import com.ttbt.smartclass.model.entity.Question;
import com.ttbt.smartclass.model.entity.Section;
import com.ttbt.smartclass.service.CourseChapterService;
import com.ttbt.smartclass.service.CourseKnowledgeAutoSyncService;
import com.ttbt.smartclass.service.QuestionService;
import com.ttbt.smartclass.service.SectionService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 题目服务实现类。
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private SectionService sectionService;

    @Resource
    private CourseChapterService courseChapterService;

    @Resource
    private CourseKnowledgeAutoSyncService courseKnowledgeAutoSyncService;

    @Override
    public boolean save(Question entity) {
        boolean result = super.save(entity);
        if (result && entity != null && entity.getId() != null) {
            courseKnowledgeAutoSyncService.triggerQuestionSync(resolveCourseId(entity.getSectionId()), entity.getId());
        }
        return result;
    }

    @Override
    public boolean updateById(Question entity) {
        if (entity == null || entity.getId() == null) {
            return false;
        }
        Question oldQuestion = this.getById(entity.getId());
        boolean result = super.updateById(entity);
        if (result) {
            Long sectionId = entity.getSectionId() != null ? entity.getSectionId()
                    : oldQuestion == null ? null : oldQuestion.getSectionId();
            courseKnowledgeAutoSyncService.triggerQuestionSync(resolveCourseId(sectionId), entity.getId());
        }
        return result;
    }

    private Long resolveCourseId(Long sectionId) {
        if (sectionId == null) {
            return null;
        }
        Section section = sectionService.getById(sectionId);
        if (section == null || section.getChapterId() == null) {
            return null;
        }
        CourseChapter chapter = courseChapterService.getById(section.getChapterId());
        return chapter == null ? null : chapter.getCourseId();
    }
}
