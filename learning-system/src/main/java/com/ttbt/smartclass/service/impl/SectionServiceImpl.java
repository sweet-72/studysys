package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.SectionMapper;
import com.ttbt.smartclass.model.dto.section.SectionAddRequest;
import com.ttbt.smartclass.model.entity.CourseChapter;
import com.ttbt.smartclass.model.entity.Section;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.SectionVO;
import com.ttbt.smartclass.service.CourseChapterService;
import com.ttbt.smartclass.service.CourseKnowledgeAutoSyncService;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.SectionService;
import com.ttbt.smartclass.utils.VideoParseUtil;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 课程小节服务实现类。
 */
@Slf4j
@Service
public class SectionServiceImpl extends ServiceImpl<SectionMapper, Section> implements SectionService {

    @Resource
    private CourseChapterService courseChapterService;

    @Resource
    private CourseService courseService;

    @Resource
    private CourseKnowledgeAutoSyncService courseKnowledgeAutoSyncService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSection(SectionAddRequest sectionAddRequest, User loginUser) {
        if (sectionAddRequest == null || sectionAddRequest.getChapterId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "chapterId is required");
        }

        CourseChapter courseChapter = courseChapterService.getById(sectionAddRequest.getChapterId());
        if (courseChapter == null || isDeleted(courseChapter.getIsDelete())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "chapter not found");
        }

        courseService.assertCanManageCourse(loginUser, courseChapter.getCourseId());

        if (!"URL".equals(sectionAddRequest.getResourceType())
                && !"FILE".equals(sectionAddRequest.getResourceType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "resourceType must be URL or FILE");
        }

        if ("URL".equals(sectionAddRequest.getResourceType())
                && (sectionAddRequest.getResourceUrl() == null || sectionAddRequest.getResourceUrl().trim().isEmpty())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "resourceUrl is required for URL resource");
        }

        Section section = new Section();
        BeanUtils.copyProperties(sectionAddRequest, section);
        section.setSortOrder(sectionAddRequest.getSortOrder() != null ? sectionAddRequest.getSortOrder() : 0);
        section.setIsFree(sectionAddRequest.getIsFree() != null ? sectionAddRequest.getIsFree() : 0);

        if (isBlank(section.getVideoUrl())
                && !isBlank(section.getResourceUrl())
                && ("VIDEO".equalsIgnoreCase(section.getContentType()) || "video".equalsIgnoreCase(section.getType()))) {
            section.setVideoUrl(section.getResourceUrl());
        }

        boolean result = this.save(section);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "create section failed");
        }

        courseKnowledgeAutoSyncService.triggerSectionSync(courseChapter.getCourseId(), section.getId());
        log.info("create section success, operator={}, chapterId={}, sectionId={}",
                loginUser == null ? null : loginUser.getId(), section.getChapterId(), section.getId());
        return section.getId();
    }

    @Override
    public boolean updateById(Section entity) {
        if (entity == null || entity.getId() == null) {
            return false;
        }
        Section oldSection = this.getById(entity.getId());
        boolean result = super.updateById(entity);
        if (result) {
            Long chapterId = entity.getChapterId() != null ? entity.getChapterId()
                    : oldSection == null ? null : oldSection.getChapterId();
            Long courseId = resolveCourseId(chapterId);
            courseKnowledgeAutoSyncService.triggerSectionSync(courseId, entity.getId());
        }
        return result;
    }

    @Override
    public SectionVO getSectionDetail(Long sectionId) {
        Section section = this.getById(sectionId);
        if (section == null || isDeleted(section.getIsDelete())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "section not found");
        }

        SectionVO sectionVO = new SectionVO();
        BeanUtils.copyProperties(section, sectionVO);

        if ("URL".equals(section.getResourceType())) {
            sectionVO.setPlayUrl(VideoParseUtil.parse(section.getResourceUrl()));
        } else if ("FILE".equals(section.getResourceType())) {
            sectionVO.setPlayUrl(section.getResourceUrl());
        }

        return sectionVO;
    }

    private Long resolveCourseId(Long chapterId) {
        if (chapterId == null) {
            return null;
        }
        CourseChapter chapter = courseChapterService.getById(chapterId);
        return chapter == null ? null : chapter.getCourseId();
    }

    private boolean isDeleted(Integer isDelete) {
        return isDelete != null && isDelete != 0;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
