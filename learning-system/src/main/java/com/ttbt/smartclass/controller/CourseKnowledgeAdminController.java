package com.ttbt.smartclass.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.annotation.AuthCheck;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.constant.UserConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.CourseKnowledgeSyncMapper;
import com.ttbt.smartclass.model.dto.course.CourseKnowledgeSyncQueryRequest;
import com.ttbt.smartclass.model.dto.course.CourseKnowledgeSyncRefreshRequest;
import com.ttbt.smartclass.model.dto.course.CourseKnowledgeSyncRequest;
import com.ttbt.smartclass.model.dto.course.CourseKnowledgeSyncRetryRequest;
import com.ttbt.smartclass.model.entity.Course;
import com.ttbt.smartclass.model.entity.CourseChapter;
import com.ttbt.smartclass.model.entity.CourseKnowledgeSync;
import com.ttbt.smartclass.model.entity.CourseMaterial;
import com.ttbt.smartclass.model.entity.Question;
import com.ttbt.smartclass.model.entity.Section;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.enums.KnowledgeSyncSourceTypeEnum;
import com.ttbt.smartclass.model.enums.KnowledgeSyncStatusEnum;
import com.ttbt.smartclass.model.vo.CourseKnowledgeSyncAdminStatusVO;
import com.ttbt.smartclass.model.vo.CourseKnowledgeSyncItemVO;
import com.ttbt.smartclass.model.vo.CourseKnowledgeSyncStatusVO;
import com.ttbt.smartclass.service.CourseChapterService;
import com.ttbt.smartclass.service.CourseKnowledgeSyncService;
import com.ttbt.smartclass.service.CourseMaterialService;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.SectionService;
import com.ttbt.smartclass.service.UserService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 未实现
 * 课程知识库同步管理接口。
 * 负责课程知识库同步、失败重试、同步记录查询和索引状态刷新等管理操作。
 */
@RestController
@RequestMapping("/admin/course/knowledge")
public class CourseKnowledgeAdminController {

    @Resource
    private CourseKnowledgeSyncService courseKnowledgeSyncService;

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
    private UserService userService;

    /**
     * 触发指定课程的知识库同步任务。
     *
     * @param syncRequest 同步请求，包含课程 ID 和同步类型
     * @param request 当前 HTTP 请求
     * @return 课程知识库同步状态
     */
    @PostMapping("/sync")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<CourseKnowledgeSyncAdminStatusVO> syncCourseKnowledge(
            @Valid @RequestBody CourseKnowledgeSyncRequest syncRequest,
            HttpServletRequest request) {
        // 获取当前登录用户，并校验其是否有权限管理该课程
        User loginUser = userService.getLoginUser(request);
        Long courseId = syncRequest.getCourseId();
        courseService.assertCanManageCourse(loginUser, courseId);

        // TODO: 当前项目未实际接入 Dify 知识库 / RAG 检索，本段代码暂时屏蔽。
        // 当前课程 AI 真实链路为：课程上下文组装 -> Prompt 注入 -> Dify 调用。
        // 为避免论文与实际系统不一致，先保留同步查询代码但不再触发 Dify Dataset 写入。
        Set<String> syncTypes = normalizeSyncTypes(syncRequest.getSyncTypes());
        // triggerSync(courseId, syncTypes);
        return ResultUtils.success(buildAdminStatusVO(courseKnowledgeSyncService.querySyncStatus(courseId)));
    }

    @GetMapping("/status")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<CourseKnowledgeSyncAdminStatusVO> getCourseKnowledgeSyncStatus(
            @RequestParam("course_id") Long courseId,
            HttpServletRequest request) {
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "course_id is invalid");
        }
        User loginUser = userService.getLoginUser(request);
        courseService.assertCanManageCourse(loginUser, courseId);
        return ResultUtils.success(buildAdminStatusVO(courseKnowledgeSyncService.querySyncStatus(courseId)));
    }

    /**
     * 重试指定的课程知识库同步记录。
     *
     * @param retryRequest 重试请求，包含同步记录 ID 列表
     * @param request 当前 HTTP 请求
     * @return 重试后的同步记录列表
     */
    @PostMapping("/retry")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<List<CourseKnowledgeSyncItemVO>> retryCourseKnowledgeSync(
            @Valid @RequestBody CourseKnowledgeSyncRetryRequest retryRequest,
            HttpServletRequest request) {
        // 查询待重试记录，并确保请求中的记录全部存在
        User loginUser = userService.getLoginUser(request);
        List<Long> syncIds = retryRequest.getSyncIds().stream().distinct().collect(Collectors.toList());
        List<CourseKnowledgeSync> existingRecords = courseKnowledgeSyncMapper.selectBatchIds(syncIds);
        if (existingRecords == null || existingRecords.size() != syncIds.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "sync record not found");
        }

        // 每条同步记录都按所属课程校验当前用户的管理权限
        for (CourseKnowledgeSync record : existingRecords) {
            courseService.assertCanManageCourse(loginUser, record.getCourseId());
        }

        // TODO: 当前项目未实际接入 Dify 知识库 / RAG 检索，本段代码暂时屏蔽。
        // 当前课程 AI 真实链路为：课程上下文组装 -> Prompt 注入 -> Dify 调用。
        // 为避免论文与实际系统不一致，先保留接口返回现有记录，但不再调用 Dify Dataset 重试同步。
        List<CourseKnowledgeSyncItemVO> result = new ArrayList<>();
        // for (Long syncId : syncIds) {
        //     CourseKnowledgeSync retried = courseKnowledgeSyncService.retrySync(syncId);
        //     result.add(toItemVO(retried));
        // }
        for (CourseKnowledgeSync record : existingRecords) {
            result.add(toItemVO(record));
        }
        return ResultUtils.success(result);
    }

    /**
     * 分页查询课程知识库同步记录。
     *
     * @param queryRequest 查询条件，包含课程、来源类型、同步状态和时间范围
     * @param request 当前 HTTP 请求
     * @return 同步记录分页数据
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Page<CourseKnowledgeSyncItemVO>> listCourseKnowledgeSyncRecords(
            @RequestBody CourseKnowledgeSyncQueryRequest queryRequest,
            HttpServletRequest request) {
        // 校验分页参数和时间范围，避免无效查询进入同步记录检索
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "queryRequest is required");
        }
        if (queryRequest.getCurrent() <= 0 || queryRequest.getPageSize() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "current and pageSize must be greater than 0");
        }
        if (queryRequest.getPageSize() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "pageSize must not exceed 100");
        }
        if (queryRequest.getStartTime() != null && queryRequest.getEndTime() != null
                && queryRequest.getStartTime().after(queryRequest.getEndTime())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "start_time must be earlier than end_time");
        }

        // 根据当前用户角色和课程参数计算可查询的课程范围
        User loginUser = userService.getLoginUser(request);
        Long courseId = queryRequest.getCourseId();
        List<Long> allowedCourseIds = buildAllowedCourseIds(loginUser, courseId);
        if (allowedCourseIds != null && allowedCourseIds.isEmpty()) {
            return ResultUtils.success(new Page<>(queryRequest.getCurrent(), queryRequest.getPageSize(), 0));
        }

        // 规范化筛选条件后分页查询同步记录
        String sourceType = normalizeSourceTypeForQuery(queryRequest.getSourceType());
        String syncStatus = normalizeSyncStatus(queryRequest.getSyncStatus());
        Page<CourseKnowledgeSync> recordPage = courseKnowledgeSyncService.pageSyncRecords(
                queryRequest.getCurrent(),
                queryRequest.getPageSize(),
                courseId,
                sourceType,
                syncStatus,
                queryRequest.getStartTime(),
                queryRequest.getEndTime(),
                allowedCourseIds);

        // 将同步记录实体转换为管理端 VO，屏蔽内部字段
        Page<CourseKnowledgeSyncItemVO> voPage = new Page<>(
                recordPage.getCurrent(),
                recordPage.getSize(),
                recordPage.getTotal());
        voPage.setRecords(recordPage.getRecords().stream().map(this::toItemVO).collect(Collectors.toList()));
        return ResultUtils.success(voPage);
    }

    /**
     * 刷新指定同步记录的外部索引状态。
     *
     * @param refreshRequest 刷新请求，包含同步记录 ID 列表
     * @param request 当前 HTTP 请求
     * @return 刷新后的同步记录列表
     */
    @PostMapping("/refresh")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<List<CourseKnowledgeSyncItemVO>> refreshCourseKnowledgeSyncStatus(
            @Valid @RequestBody CourseKnowledgeSyncRefreshRequest refreshRequest,
            HttpServletRequest request) {
        // 查询待刷新记录，并确保所有记录都存在
        User loginUser = userService.getLoginUser(request);
        List<Long> syncIds = refreshRequest.getSyncIds().stream().distinct().collect(Collectors.toList());
        List<CourseKnowledgeSync> existingRecords = courseKnowledgeSyncMapper.selectBatchIds(syncIds);
        if (existingRecords == null || existingRecords.size() != syncIds.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "sync record not found");
        }

        // 刷新前逐条校验当前用户对记录所属课程的管理权限
        for (CourseKnowledgeSync record : existingRecords) {
            courseService.assertCanManageCourse(loginUser, record.getCourseId());
        }

        // TODO: 当前项目未实际接入 Dify 知识库 / RAG 检索，本段代码暂时屏蔽。
        // 当前课程 AI 真实链路为：课程上下文组装 -> Prompt 注入 -> Dify 调用。
        // 为避免论文与实际系统不一致，先保留接口返回现有记录，但不再刷新 Dify Dataset 索引状态。
        List<CourseKnowledgeSyncItemVO> result = new ArrayList<>();
        // for (Long syncId : syncIds) {
        //     CourseKnowledgeSync refreshed = courseKnowledgeSyncService.refreshSyncStatus(syncId);
        //     result.add(toItemVO(refreshed));
        // }
        for (CourseKnowledgeSync record : existingRecords) {
            result.add(toItemVO(record));
        }
        return ResultUtils.success(result);
    }

    private void triggerSync(Long courseId, Set<String> syncTypes) {
        if (syncTypes.isEmpty()) {
            courseKnowledgeSyncService.syncCourse(courseId);
            return;
        }

        List<CourseChapter> chapters = null;
        List<Section> sections = null;
        if (syncTypes.contains(KnowledgeSyncSourceTypeEnum.CHAPTER.getValue())
                || syncTypes.contains(KnowledgeSyncSourceTypeEnum.SECTION.getValue())
                || syncTypes.contains(KnowledgeSyncSourceTypeEnum.QUESTION.getValue())) {
            chapters = courseChapterService.getChaptersByCourseId(courseId);
        }
        if (chapters != null && !chapters.isEmpty()
                && (syncTypes.contains(KnowledgeSyncSourceTypeEnum.SECTION.getValue())
                || syncTypes.contains(KnowledgeSyncSourceTypeEnum.QUESTION.getValue()))) {
            List<Long> chapterIds = chapters.stream().map(CourseChapter::getId).collect(Collectors.toList());
            LambdaQueryWrapper<Section> sectionQuery = new LambdaQueryWrapper<>();
            sectionQuery.in(Section::getChapterId, chapterIds)
                    .eq(Section::getIsDelete, 0)
                    .orderByAsc(Section::getChapterId)
                    .orderByAsc(Section::getSortOrder);
            sections = sectionService.list(sectionQuery);
        }

        if (syncTypes.contains(KnowledgeSyncSourceTypeEnum.COURSE_INTRO.getValue())) {
            courseKnowledgeSyncService.syncCourseIntro(courseId);
        }
        if (chapters != null && syncTypes.contains(KnowledgeSyncSourceTypeEnum.CHAPTER.getValue())) {
            for (CourseChapter chapter : chapters) {
                courseKnowledgeSyncService.syncChapter(chapter.getId());
            }
        }
        if (sections != null && syncTypes.contains(KnowledgeSyncSourceTypeEnum.SECTION.getValue())) {
            for (Section section : sections) {
                courseKnowledgeSyncService.syncSection(section.getId());
            }
        }
        if (syncTypes.contains(KnowledgeSyncSourceTypeEnum.MATERIAL.getValue())) {
            List<CourseMaterial> materials = courseMaterialService.getMaterialsByCourseId(courseId);
            for (CourseMaterial material : materials) {
                courseKnowledgeSyncService.syncMaterial(material.getId());
            }
        }
        if (sections != null && syncTypes.contains(KnowledgeSyncSourceTypeEnum.QUESTION.getValue())) {
            for (Section section : sections) {
                List<Question> questions = courseService.getSectionQuestions(section.getId());
                for (Question question : questions) {
                    courseKnowledgeSyncService.syncQuestion(question.getId());
                }
            }
        }
    }

    private Set<String> normalizeSyncTypes(List<String> syncTypes) {
        if (syncTypes == null || syncTypes.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<String> result = new LinkedHashSet<>();
        for (String syncType : syncTypes) {
            if (syncType == null || syncType.trim().isEmpty()) {
                continue;
            }
            String normalized = syncType.trim().toLowerCase();
            KnowledgeSyncSourceTypeEnum sourceTypeEnum = KnowledgeSyncSourceTypeEnum.getEnumByValue(normalized);
            if (sourceTypeEnum == null || KnowledgeSyncSourceTypeEnum.EXPLANATION == sourceTypeEnum) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "unsupported sync type: " + syncType);
            }
            result.add(sourceTypeEnum.getValue());
        }
        return result;
    }

    private String normalizeSourceTypeForQuery(String sourceType) {
        if (sourceType == null || sourceType.trim().isEmpty()) {
            return null;
        }
        String normalized = sourceType.trim().toLowerCase();
        KnowledgeSyncSourceTypeEnum sourceTypeEnum = KnowledgeSyncSourceTypeEnum.getEnumByValue(normalized);
        if (sourceTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "unsupported sourceType: " + sourceType);
        }
        return sourceTypeEnum.getValue();
    }

    private String normalizeSyncStatus(String syncStatus) {
        if (syncStatus == null || syncStatus.trim().isEmpty()) {
            return null;
        }
        String normalized = syncStatus.trim().toLowerCase();
        KnowledgeSyncStatusEnum statusEnum = KnowledgeSyncStatusEnum.getEnumByValue(normalized);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "unsupported syncStatus: " + syncStatus);
        }
        return statusEnum.getValue();
    }

    private List<Long> buildAllowedCourseIds(User loginUser, Long courseId) {
        if (courseId != null) {
            courseService.assertCanManageCourse(loginUser, courseId);
            return Collections.singletonList(courseId);
        }
        if (UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            return null;
        }
        if (UserConstant.TEACHER_ROLE.equals(loginUser.getUserRole())) {
            List<Course> courseList = courseService.list(new LambdaQueryWrapper<Course>()
                    .eq(Course::getTeacherId, loginUser.getId())
                    .eq(Course::getIsDelete, 0));
            if (courseList == null || courseList.isEmpty()) {
                return Collections.emptyList();
            }
            return courseList.stream().map(Course::getId).collect(Collectors.toList());
        }
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    private CourseKnowledgeSyncAdminStatusVO buildAdminStatusVO(CourseKnowledgeSyncStatusVO rawStatus) {
        CourseKnowledgeSyncAdminStatusVO vo = new CourseKnowledgeSyncAdminStatusVO();
        vo.setCourseId(rawStatus.getCourseId());
        vo.setTotalCount(rawStatus.getTotalCount());
        vo.setCompletedCount(rawStatus.getCompletedCount());
        vo.setIndexingCount(rawStatus.getIndexingCount());
        vo.setFailedCount(rawStatus.getFailedCount());
        List<CourseKnowledgeSyncItemVO> records = rawStatus.getRecords() == null
                ? List.of()
                : rawStatus.getRecords().stream().map(this::toItemVO).collect(Collectors.toList());
        vo.setRecords(records);
        return vo;
    }

    private CourseKnowledgeSyncItemVO toItemVO(CourseKnowledgeSync record) {
        CourseKnowledgeSyncItemVO itemVO = new CourseKnowledgeSyncItemVO();
        itemVO.setId(record.getId());
        itemVO.setCourseId(record.getCourseId());
        itemVO.setSourceType(record.getSourceType());
        itemVO.setSourceId(record.getSourceId());
        itemVO.setSourceName(record.getSourceName());
        itemVO.setSyncMode(record.getSyncMode());
        itemVO.setSyncStatus(record.getSyncStatus());
        itemVO.setRetryCount(record.getRetryCount());
        itemVO.setErrorMessage(record.getErrorMessage());
        itemVO.setDocumentId(record.getDifyDocumentId());
        itemVO.setBatchId(record.getDifyBatchId());
        itemVO.setLastSyncTime(record.getLastSyncTime());
        itemVO.setNextRetryTime(record.getNextRetryTime());
        itemVO.setCreateTime(record.getCreateTime());
        itemVO.setUpdateTime(record.getUpdateTime());
        return itemVO;
    }
}
