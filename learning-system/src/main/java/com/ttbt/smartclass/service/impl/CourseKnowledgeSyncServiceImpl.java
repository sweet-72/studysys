package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.config.DifyConfig;
import com.ttbt.smartclass.constant.DifyKnowledgeConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.CourseKnowledgeSyncMapper;
import com.ttbt.smartclass.mapper.SubjectAiBindingMapper;
import com.ttbt.smartclass.model.dto.dify.DifyDocumentCreateByFileRequest;
import com.ttbt.smartclass.model.dto.dify.DifyDocumentCreateByTextRequest;
import com.ttbt.smartclass.model.dto.dify.DifyDocumentIndexStatusResponse;
import com.ttbt.smartclass.model.dto.dify.DifyDocumentOperationResponse;
import com.ttbt.smartclass.model.entity.Course;
import com.ttbt.smartclass.model.entity.CourseChapter;
import com.ttbt.smartclass.model.entity.CourseKnowledgeSync;
import com.ttbt.smartclass.model.entity.CourseMaterial;
import com.ttbt.smartclass.model.entity.Question;
import com.ttbt.smartclass.model.entity.Section;
import com.ttbt.smartclass.model.entity.SubjectAiBinding;
import com.ttbt.smartclass.model.enums.DifyDocumentSyncModeEnum;
import com.ttbt.smartclass.model.enums.KnowledgeSyncSourceTypeEnum;
import com.ttbt.smartclass.model.enums.KnowledgeSyncStatusEnum;
import com.ttbt.smartclass.model.vo.CourseKnowledgeSyncStatusVO;
import com.ttbt.smartclass.service.CourseChapterService;
import com.ttbt.smartclass.service.CourseKnowledgeSyncService;
import com.ttbt.smartclass.service.CourseMaterialService;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.DifyKnowledgeService;
import com.ttbt.smartclass.service.QuestionService;
import com.ttbt.smartclass.service.SectionService;
import com.ttbt.smartclass.utils.ContentHashUtils;
import com.ttbt.smartclass.utils.MaterialFileStreamResolver;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 课程知识库同步服务实现类。
 */
@Service
@Slf4j
public class CourseKnowledgeSyncServiceImpl implements CourseKnowledgeSyncService {

    @Resource
    private SubjectAiBindingMapper subjectAiBindingMapper;

    @Resource
    private CourseKnowledgeSyncMapper courseKnowledgeSyncMapper;

    @Resource
    private CourseService courseService;

    @Resource
    private CourseChapterService courseChapterService;

    @Resource
    private SectionService sectionService;

    @Resource
    private CourseMaterialService courseMaterialService;

    @Resource
    private QuestionService questionService;

    @Resource
    private DifyKnowledgeService difyKnowledgeService;

    @Resource
    private DifyConfig difyConfig;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private MaterialFileStreamResolver materialFileStreamResolver;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CourseKnowledgeSync> syncCourse(Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "course_id is invalid");
        }

        List<CourseKnowledgeSync> results = new ArrayList<>();
        results.add(syncCourseIntro(courseId));

        List<CourseChapter> chapters = courseChapterService.getChaptersByCourseId(courseId);
        for (CourseChapter chapter : chapters) {
            results.add(syncChapter(chapter.getId()));

            List<Section> sections = listSectionsByChapterId(chapter.getId());
            for (Section section : sections) {
                results.add(syncSection(section.getId()));
                for (Question question : listQuestionsBySectionId(section.getId())) {
                    results.add(syncQuestion(question.getId()));
                }
            }
        }

        List<CourseMaterial> materials = courseMaterialService.getMaterialsByCourseId(courseId);
        for (CourseMaterial material : materials) {
            results.add(syncMaterial(material.getId()));
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseKnowledgeSync syncCourseIntro(Long courseId) {
        Course course = getCourseOrThrow(courseId);
        String content = buildCourseIntroText(course);
        return syncTextSource(course, KnowledgeSyncSourceTypeEnum.COURSE_INTRO, course.getId(), course.getTitle(), content, buildCourseMetadata(course));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseKnowledgeSync syncChapter(Long chapterId) {
        CourseChapter chapter = getChapterOrThrow(chapterId);
        Course course = getCourseOrThrow(chapter.getCourseId());
        String content = buildChapterText(course, chapter);
        Map<String, Object> metadata = buildCourseMetadata(course);
        metadata.put("chapter_id", chapter.getId());
        metadata.put("chapter_title", chapter.getTitle());
        return syncTextSource(course, KnowledgeSyncSourceTypeEnum.CHAPTER, chapter.getId(), chapter.getTitle(), content, metadata);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseKnowledgeSync syncSection(Long sectionId) {
        Section section = getSectionOrThrow(sectionId);
        CourseChapter chapter = getChapterOrThrow(section.getChapterId());
        Course course = getCourseOrThrow(chapter.getCourseId());
        String content = buildSectionText(course, chapter, section);
        Map<String, Object> metadata = buildCourseMetadata(course);
        metadata.put("chapter_id", chapter.getId());
        metadata.put("chapter_title", chapter.getTitle());
        metadata.put("section_id", section.getId());
        metadata.put("section_title", section.getTitle());
        metadata.put("content_type", section.getContentType());
        return syncTextSource(course, KnowledgeSyncSourceTypeEnum.SECTION, section.getId(), section.getTitle(), content, metadata);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseKnowledgeSync syncMaterial(Long materialId) {
        CourseMaterial material = getMaterialOrThrow(materialId);
        Course course = getCourseOrThrow(material.getCourseId());
        SubjectAiBinding binding = getBindingByCourse(course);
        String lockKey = buildLockKey(KnowledgeSyncSourceTypeEnum.MATERIAL.getValue(), materialId);

        return executeWithOptionalLock(lockKey, () -> {
            CourseKnowledgeSync record = getOrCreateSyncRecord(course, KnowledgeSyncSourceTypeEnum.MATERIAL, material.getId(), material.getTitle(), DifyDocumentSyncModeEnum.FILE.getValue(), material.getFileUrl());
            markUploading(record);
            try {
                DifyDocumentOperationResponse operationResponse = callFileSync(record, binding, material);
                updateSyncRecordAfterOperation(record, operationResponse, KnowledgeSyncStatusEnum.INDEXING.getValue());
                return record;
            } catch (Exception e) {
                markFailed(record, e);
                throw e;
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseKnowledgeSync syncQuestion(Long questionId) {
        Question question = getQuestionOrThrow(questionId);
        Section section = getSectionOrThrow(question.getSectionId());
        CourseChapter chapter = getChapterOrThrow(section.getChapterId());
        Course course = getCourseOrThrow(chapter.getCourseId());
        String content = buildQuestionText(course, chapter, section, question);
        Map<String, Object> metadata = buildCourseMetadata(course);
        metadata.put("chapter_id", chapter.getId());
        metadata.put("chapter_title", chapter.getTitle());
        metadata.put("section_id", section.getId());
        metadata.put("section_title", section.getTitle());
        metadata.put("question_id", question.getId());
        metadata.put("difficulty", question.getDifficulty());
        return syncTextSource(course, KnowledgeSyncSourceTypeEnum.QUESTION, question.getId(), "question-" + question.getId(), content, metadata);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseKnowledgeSync retrySync(Long syncId) {
        CourseKnowledgeSync syncRecord = courseKnowledgeSyncMapper.selectById(syncId);
        if (syncRecord == null || (syncRecord.getIsDelete() != null && syncRecord.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "sync record not found");
        }
        KnowledgeSyncSourceTypeEnum sourceTypeEnum = KnowledgeSyncSourceTypeEnum.getEnumByValue(syncRecord.getSourceType());
        if (sourceTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "unknown sync source type");
        }
        switch (sourceTypeEnum) {
            case COURSE_INTRO:
                return syncCourseIntro(syncRecord.getSourceId());
            case CHAPTER:
                return syncChapter(syncRecord.getSourceId());
            case SECTION:
                return syncSection(syncRecord.getSourceId());
            case MATERIAL:
                return syncMaterial(syncRecord.getSourceId());
            case QUESTION:
            case EXPLANATION:
                return syncQuestion(syncRecord.getSourceId());
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "unsupported sync source type");
        }
    }

    @Override
    public CourseKnowledgeSyncStatusVO querySyncStatus(Long courseId) {
        getCourseOrThrow(courseId);
        List<CourseKnowledgeSync> records = courseKnowledgeSyncMapper.selectList(new LambdaQueryWrapper<CourseKnowledgeSync>()
                .eq(CourseKnowledgeSync::getCourseId, courseId)
                .eq(CourseKnowledgeSync::getIsDelete, 0)
                .orderByDesc(CourseKnowledgeSync::getUpdateTime)
                .orderByDesc(CourseKnowledgeSync::getId));

        CourseKnowledgeSyncStatusVO result = new CourseKnowledgeSyncStatusVO();
        result.setCourseId(courseId);
        result.setTotalCount(records.size());
        result.setCompletedCount((int) records.stream().filter(item -> KnowledgeSyncStatusEnum.COMPLETED.getValue().equals(item.getSyncStatus())).count());
        result.setIndexingCount((int) records.stream().filter(item -> KnowledgeSyncStatusEnum.INDEXING.getValue().equals(item.getSyncStatus()) || KnowledgeSyncStatusEnum.UPLOADING.getValue().equals(item.getSyncStatus())).count());
        result.setFailedCount((int) records.stream().filter(item -> KnowledgeSyncStatusEnum.FAILED.getValue().equals(item.getSyncStatus())).count());
        result.setRecords(records);
        return result;
    }

    @Override
    public Page<CourseKnowledgeSync> pageSyncRecords(long current,
                                                     long pageSize,
                                                     Long courseId,
                                                     String sourceType,
                                                     String syncStatus,
                                                     Date startTime,
                                                     Date endTime,
                                                     List<Long> allowedCourseIds) {
        LambdaQueryWrapper<CourseKnowledgeSync> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseKnowledgeSync::getIsDelete, 0)
                .eq(courseId != null, CourseKnowledgeSync::getCourseId, courseId)
                .eq(StringUtils.isNotBlank(sourceType), CourseKnowledgeSync::getSourceType, sourceType)
                .eq(StringUtils.isNotBlank(syncStatus), CourseKnowledgeSync::getSyncStatus, syncStatus)
                .ge(startTime != null, CourseKnowledgeSync::getUpdateTime, startTime)
                .le(endTime != null, CourseKnowledgeSync::getUpdateTime, endTime)
                .in(allowedCourseIds != null, CourseKnowledgeSync::getCourseId, allowedCourseIds)
                .orderByDesc(CourseKnowledgeSync::getUpdateTime)
                .orderByDesc(CourseKnowledgeSync::getId);
        return courseKnowledgeSyncMapper.selectPage(new Page<>(current, pageSize), queryWrapper);
    }

    @Override
    public CourseKnowledgeSync refreshSyncStatus(Long syncId) {
        CourseKnowledgeSync record = courseKnowledgeSyncMapper.selectById(syncId);
        if (record == null || StringUtils.isAnyBlank(record.getDifyDatasetId(), record.getDifyBatchId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "sync record missing dify batch info");
        }
        SubjectAiBinding binding = subjectAiBindingMapper.selectOne(new LambdaQueryWrapper<SubjectAiBinding>()
                .eq(SubjectAiBinding::getCategoryId, record.getCategoryId())
                .eq(SubjectAiBinding::getStatus, 1)
                .eq(SubjectAiBinding::getIsDelete, 0)
                .last("limit 1"));
        if (binding == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "subject ai binding not found");
        }
        DifyDocumentIndexStatusResponse response = difyKnowledgeService.getDocumentIndexingStatus(
                resolveDatasetBaseUrl(binding),
                binding.getDifyWorkflowAppKey(),
                record.getDifyDatasetId(),
                record.getDifyBatchId()
        );
        if (KnowledgeSyncStatusEnum.COMPLETED.getValue().equalsIgnoreCase(response.getIndexingStatus())) {
            record.setSyncStatus(KnowledgeSyncStatusEnum.COMPLETED.getValue());
            record.setLastSyncTime(new Date());
            record.setErrorMessage(null);
            courseKnowledgeSyncMapper.updateById(record);
        } else if (KnowledgeSyncStatusEnum.FAILED.getValue().equalsIgnoreCase(response.getIndexingStatus())) {
            markFailed(record, new RuntimeException(StringUtils.defaultIfBlank(response.getErrorMessage(), "Dify indexing failed")));
        } else {
            record.setSyncStatus(KnowledgeSyncStatusEnum.INDEXING.getValue());
            courseKnowledgeSyncMapper.updateById(record);
        }
        return courseKnowledgeSyncMapper.selectById(syncId);
    }

    private CourseKnowledgeSync syncTextSource(Course course,
                                               KnowledgeSyncSourceTypeEnum sourceType,
                                               Long sourceId,
                                               String sourceName,
                                               String content,
                                               Map<String, Object> metadata) {
        SubjectAiBinding binding = getBindingByCourse(course);
        String lockKey = buildLockKey(sourceType.getValue(), sourceId);
        return executeWithOptionalLock(lockKey, () -> {
            String contentHash = ContentHashUtils.sha256(content);
            CourseKnowledgeSync record = getOrCreateSyncRecord(course, sourceType, sourceId, sourceName, DifyDocumentSyncModeEnum.TEXT.getValue(), contentHash);
            if (isSameContentAndCompleted(record, contentHash)) {
                return record;
            }

            markUploading(record);
            try {
                DifyDocumentOperationResponse operationResponse = callTextSync(record, binding, sourceName, content, metadata);
                updateSyncRecordAfterOperation(record, operationResponse, KnowledgeSyncStatusEnum.INDEXING.getValue());
                record.setContentHash(contentHash);
                courseKnowledgeSyncMapper.updateById(record);
                return record;
            } catch (Exception e) {
                markFailed(record, e);
                throw e;
            }
        });
    }

    private DifyDocumentOperationResponse callTextSync(CourseKnowledgeSync record,
                                                       SubjectAiBinding binding,
                                                       String sourceName,
                                                       String content,
                                                       Map<String, Object> metadata) {
        DifyDocumentCreateByTextRequest request = new DifyDocumentCreateByTextRequest();
        request.setBaseUrl(resolveDatasetBaseUrl(binding));
        request.setApiKey(binding.getDifyWorkflowAppKey());
        request.setDatasetId(binding.getDifyDatasetId());
        request.setName(sourceName);
        request.setText(content);
        request.setMetadata(metadata);

        if (StringUtils.isBlank(record.getDifyDocumentId())) {
            return difyKnowledgeService.createDocumentByText(request);
        }
        return difyKnowledgeService.updateDocumentByText(record.getDifyDocumentId(), request);
    }

    private DifyDocumentOperationResponse callFileSync(CourseKnowledgeSync record,
                                                       SubjectAiBinding binding,
                                                       CourseMaterial material) {
        try (MaterialFileStreamResolver.ResolvedMaterialFile resolvedFile = materialFileStreamResolver.resolve(material)) {
            DifyDocumentCreateByFileRequest request = new DifyDocumentCreateByFileRequest();
            request.setBaseUrl(resolveDatasetBaseUrl(binding));
            request.setApiKey(binding.getDifyWorkflowAppKey());
            request.setDatasetId(binding.getDifyDatasetId());
            request.setName(material.getTitle());
            request.setFilename(StringUtils.defaultIfBlank(resolvedFile.getFilename(), buildMaterialFilename(material)));
            request.setContentType(StringUtils.defaultIfBlank(resolvedFile.getContentType(),
                    StringUtils.defaultIfBlank(material.getFileType(), "application/octet-stream")));
            request.setInputStream(resolvedFile.getInputStream());
            request.setMetadata(buildMaterialMetadata(material, record.getCourseId()));

            log.info("sync course material as file document, materialId={}, file_url={}, resolvedSource={}, filename={}, contentType={}, file_size={}",
                    material.getId(), material.getFileUrl(), resolvedFile.getSourceDescription(),
                    request.getFilename(), request.getContentType(), resolvedFile.getContentLength());

            if (StringUtils.isBlank(record.getDifyDocumentId())) {
                return difyKnowledgeService.createDocumentByFile(request);
            }
            return difyKnowledgeService.updateDocumentByFile(record.getDifyDocumentId(), request);
        } catch (BusinessException e) {
            log.error("sync material file stream resolve failed, materialId={}, file_url={}",
                    material.getId(), material.getFileUrl(), e);
            throw e;
        } catch (Exception e) {
            log.error("sync material file upload failed, materialId={}, file_url={}",
                    material.getId(), material.getFileUrl(), e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "read material file stream failed, materialId=" + material.getId()
                            + ", file_url=" + material.getFileUrl()
                            + ", reason=" + StringUtils.defaultIfBlank(e.getMessage(), "unknown error"));
        }
    }

    private <T> T executeWithOptionalLock(String lockKey, SyncSupplier<T> supplier) {
        Boolean locked = null;
        try {
            locked = redisTemplate.opsForValue().setIfAbsent(lockKey, 1, DifyKnowledgeConstant.REDIS_SYNC_LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("acquire sync lock failed, lockKey={}", lockKey, e);
        }

        if (Boolean.FALSE.equals(locked)) {
            log.info("skip duplicate sync request, lockKey={}", lockKey);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "sync task is already running, please retry later");
        }

        try {
            return supplier.get();
        } finally {
            if (Boolean.TRUE.equals(locked)) {
                try {
                    redisTemplate.delete(lockKey);
                } catch (Exception e) {
                    log.warn("release sync lock failed, lockKey={}", lockKey, e);
                }
            }
        }
    }

    private String resolveDatasetBaseUrl(SubjectAiBinding binding) {
        if (binding != null && StringUtils.isNotBlank(binding.getDifyWorkflowBaseUrl())) {
            return binding.getDifyWorkflowBaseUrl();
        }
        return difyConfig.getBaseUrl();
    }

    private SubjectAiBinding getBindingByCourse(Course course) {
        if (course.getCategoryId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "course category is missing, cannot sync knowledge to Dify");
        }
        SubjectAiBinding binding = subjectAiBindingMapper.selectOne(new LambdaQueryWrapper<SubjectAiBinding>()
                .eq(SubjectAiBinding::getCategoryId, course.getCategoryId())
                .eq(SubjectAiBinding::getStatus, 1)
                .eq(SubjectAiBinding::getIsDelete, 0)
                .last("limit 1"));
        if (binding == null || StringUtils.isBlank(binding.getDifyDatasetId()) || StringUtils.isBlank(binding.getDifyWorkflowAppKey())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Dify subject binding or dataset config not found");
        }
        return binding;
    }

    private CourseKnowledgeSync getOrCreateSyncRecord(Course course,
                                                      KnowledgeSyncSourceTypeEnum sourceType,
                                                      Long sourceId,
                                                      String sourceName,
                                                      String syncMode,
                                                      String contentHash) {
        CourseKnowledgeSync existing = courseKnowledgeSyncMapper.selectOne(new LambdaQueryWrapper<CourseKnowledgeSync>()
                .eq(CourseKnowledgeSync::getSourceType, sourceType.getValue())
                .eq(CourseKnowledgeSync::getSourceId, sourceId)
                .eq(CourseKnowledgeSync::getIsDelete, 0)
                .last("limit 1"));
        if (existing != null) {
            existing.setCourseId(course.getId());
            existing.setCategoryId(course.getCategoryId());
            existing.setSourceName(sourceName);
            existing.setSyncMode(syncMode);
            if (StringUtils.isNotBlank(contentHash)) {
                existing.setContentHash(contentHash);
            }
            courseKnowledgeSyncMapper.updateById(existing);
            return existing;
        }

        CourseKnowledgeSync record = new CourseKnowledgeSync();
        record.setCourseId(course.getId());
        record.setCategoryId(course.getCategoryId());
        record.setSourceType(sourceType.getValue());
        record.setSourceId(sourceId);
        record.setSourceName(sourceName);
        record.setSyncMode(syncMode);
        record.setSyncStatus(KnowledgeSyncStatusEnum.PENDING.getValue());
        record.setContentHash(contentHash);
        record.setRetryCount(0);
        record.setIsDelete(0);
        courseKnowledgeSyncMapper.insert(record);
        return record;
    }

    private boolean isSameContentAndCompleted(CourseKnowledgeSync record, String contentHash) {
        return record != null
                && StringUtils.isNotBlank(record.getContentHash())
                && StringUtils.equals(record.getContentHash(), contentHash)
                && KnowledgeSyncStatusEnum.COMPLETED.getValue().equals(record.getSyncStatus())
                && StringUtils.isNotBlank(record.getDifyDocumentId());
    }

    private void markUploading(CourseKnowledgeSync record) {
        record.setSyncStatus(KnowledgeSyncStatusEnum.UPLOADING.getValue());
        record.setErrorMessage(null);
        courseKnowledgeSyncMapper.updateById(record);
    }

    private void updateSyncRecordAfterOperation(CourseKnowledgeSync record,
                                                DifyDocumentOperationResponse operationResponse,
                                                String syncStatus) {
        record.setDifyDatasetId(operationResponse.getDatasetId());
        if (StringUtils.isNotBlank(operationResponse.getDocumentId())) {
            record.setDifyDocumentId(operationResponse.getDocumentId());
        }
        record.setDifyBatchId(operationResponse.getBatchId());
        record.setSyncStatus(syncStatus);
        record.setLastSyncTime(new Date());
        record.setErrorMessage(null);
        courseKnowledgeSyncMapper.updateById(record);
    }

    private void markFailed(CourseKnowledgeSync record, Exception e) {
        record.setSyncStatus(KnowledgeSyncStatusEnum.FAILED.getValue());
        record.setRetryCount(record.getRetryCount() == null ? 1 : record.getRetryCount() + 1);
        record.setNextRetryTime(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)));
        record.setErrorMessage(StringUtils.left(StringUtils.defaultString(e.getMessage(), "unknown error"), 1000));
        courseKnowledgeSyncMapper.updateById(record);
        log.error("course knowledge sync failed, syncId={}", record.getId(), e);
    }

    private List<Section> listSectionsByChapterId(Long chapterId) {
        return sectionService.list(new LambdaQueryWrapper<Section>()
                .eq(Section::getChapterId, chapterId)
                .eq(Section::getIsDelete, 0)
                .orderByAsc(Section::getSortOrder));
    }

    private List<Question> listQuestionsBySectionId(Long sectionId) {
        return questionService.list(new LambdaQueryWrapper<Question>()
                .eq(Question::getSectionId, sectionId)
                .eq(Question::getIsDelete, 0)
                .orderByAsc(Question::getSortOrder));
    }

    private Map<String, Object> buildCourseMetadata(Course course) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("course_id", course.getId());
        metadata.put("course_title", course.getTitle());
        metadata.put("category_id", course.getCategoryId());
        metadata.put("tags", course.getTags());
        metadata.put("difficulty", course.getDifficulty());
        return metadata;
    }

    private Map<String, Object> buildMaterialMetadata(CourseMaterial material, Long courseId) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("course_id", courseId);
        metadata.put("material_id", material.getId());
        metadata.put("doc_type", KnowledgeSyncSourceTypeEnum.MATERIAL.getValue());
        metadata.put("file_type", material.getFileType());
        metadata.put("title", material.getTitle());
        return metadata;
    }

    private String buildCourseIntroText(Course course) {
        return new StringBuilder()
                .append("Course Title: ").append(StringUtils.defaultString(course.getTitle())).append('\n')
                .append("Course Subtitle: ").append(StringUtils.defaultString(course.getSubtitle())).append('\n')
                .append("Course Description: ").append(StringUtils.defaultString(course.getDescription())).append('\n')
                .append("Requirements: ").append(StringUtils.defaultString(course.getRequirements())).append('\n')
                .append("Objectives: ").append(StringUtils.defaultString(course.getObjectives())).append('\n')
                .append("Target Audience: ").append(StringUtils.defaultString(course.getTargetAudience())).append('\n')
                .append("Tags: ").append(StringUtils.defaultString(course.getTags()))
                .toString();
    }

    private String buildChapterText(Course course, CourseChapter chapter) {
        return new StringBuilder()
                .append("Course Title: ").append(StringUtils.defaultString(course.getTitle())).append('\n')
                .append("Chapter Title: ").append(StringUtils.defaultString(chapter.getTitle())).append('\n')
                .append("Chapter Description: ").append(StringUtils.defaultString(chapter.getDescription()))
                .toString();
    }

    private String buildSectionText(Course course, CourseChapter chapter, Section section) {
        return new StringBuilder()
                .append("Course Title: ").append(StringUtils.defaultString(course.getTitle())).append('\n')
                .append("Chapter Title: ").append(StringUtils.defaultString(chapter.getTitle())).append('\n')
                .append("Section Title: ").append(StringUtils.defaultString(section.getTitle())).append('\n')
                .append("Section Type: ").append(StringUtils.defaultString(section.getType())).append('\n')
                .append("Content Type: ").append(StringUtils.defaultString(section.getContentType())).append('\n')
                .append("Section Content: ").append(StringUtils.defaultString(section.getContent()))
                .toString();
    }

    private String buildQuestionText(Course course, CourseChapter chapter, Section section, Question question) {
        return new StringBuilder()
                .append("Course Title: ").append(StringUtils.defaultString(course.getTitle())).append('\n')
                .append("Chapter Title: ").append(StringUtils.defaultString(chapter.getTitle())).append('\n')
                .append("Section Title: ").append(StringUtils.defaultString(section.getTitle())).append('\n')
                .append("Question Type: ").append(StringUtils.defaultString(question.getType())).append('\n')
                .append("Question Content: ").append(StringUtils.defaultString(question.getContent())).append('\n')
                .append("Question Options: ").append(StringUtils.defaultString(question.getOptions())).append('\n')
                .append("Correct Answer: ").append(StringUtils.defaultString(question.getAnswer())).append('\n')
                .append("Question Explanation: ").append(StringUtils.defaultString(question.getExplanation()))
                .toString();
    }

    private String buildMaterialFilename(CourseMaterial material) {
        if (StringUtils.isNotBlank(material.getTitle()) && StringUtils.isNotBlank(material.getFileType())) {
            return material.getTitle() + normalizeMaterialSuffix(material.getFileType());
        }
        return "material-" + material.getId() + normalizeMaterialSuffix(material.getFileType());
    }

    private String normalizeMaterialSuffix(String fileType) {
        if (StringUtils.isBlank(fileType)) {
            return ".bin";
        }
        if (fileType.contains("pdf")) {
            return ".pdf";
        }
        if (fileType.contains("word")) {
            return ".docx";
        }
        if (fileType.contains("presentation")) {
            return ".pptx";
        }
        if (fileType.contains("sheet") || fileType.contains("excel")) {
            return ".xlsx";
        }
        if (fileType.contains("text")) {
            return ".txt";
        }
        return ".bin";
    }

    private String buildLockKey(String sourceType, Long sourceId) {
        return DifyKnowledgeConstant.REDIS_SYNC_LOCK_KEY + sourceType + ":" + sourceId;
    }

    private Course getCourseOrThrow(Long courseId) {
        Course course = courseService.getById(courseId);
        if (course == null || (course.getIsDelete() != null && course.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "course not found");
        }
        return course;
    }

    private CourseChapter getChapterOrThrow(Long chapterId) {
        CourseChapter chapter = courseChapterService.getById(chapterId);
        if (chapter == null || (chapter.getIsDelete() != null && chapter.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "chapter not found");
        }
        return chapter;
    }

    private Section getSectionOrThrow(Long sectionId) {
        Section section = sectionService.getById(sectionId);
        if (section == null || (section.getIsDelete() != null && section.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "section not found");
        }
        return section;
    }

    private CourseMaterial getMaterialOrThrow(Long materialId) {
        CourseMaterial material = courseMaterialService.getById(materialId);
        if (material == null || (material.getIsDelete() != null && material.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "course material not found");
        }
        return material;
    }

    private Question getQuestionOrThrow(Long questionId) {
        Question question = questionService.getById(questionId);
        if (question == null || (question.getIsDelete() != null && question.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "question not found");
        }
        return question;
    }

    @FunctionalInterface
    private interface SyncSupplier<T> {
        T get();
    }
}

