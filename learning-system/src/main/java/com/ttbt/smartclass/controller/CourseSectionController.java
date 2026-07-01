package com.ttbt.smartclass.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.annotation.AuthCheck;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.DeleteRequest;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.constant.UserConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.model.dto.file.UploadVideoRequest;
import com.ttbt.smartclass.model.dto.section.SectionAddRequest;
import com.ttbt.smartclass.model.entity.CourseChapter;
import com.ttbt.smartclass.model.entity.CourseSection;
import com.ttbt.smartclass.model.entity.Section;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.service.CourseChapterService;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.SectionService;
import com.ttbt.smartclass.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 课程小节管理控制器。
 * 负责课程小节的新增、上传、查询、统计和旧版接口兼容。
 */
@RestController
@RequestMapping("/course/section")
@Slf4j
public class CourseSectionController {

    @Resource
    private CourseChapterService courseChapterService;

    @Resource
    private CourseService courseService;

    @Resource
    private UserService userService;

    @Resource
    private SectionService sectionService;

    @Resource
    private FileController fileController;

    /**
     * 新增课程小节，兼容旧版前端字段。
     *
     * @param payload 小节创建参数
     * @param request 当前 HTTP 请求
     * @return 新建小节 ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Long> addSection(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        // 校验旧版 Map 请求体不能为空，否则无法转换为标准小节创建请求
        if (payload == null || payload.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前登录用户，并将旧版字段转换为 SectionAddRequest
        User loginUser = userService.getLoginUser(request);
        SectionAddRequest sectionAddRequest = buildSectionAddRequest(payload);
        // 如果前端同时传入 course_id，需要校验章节确实属于该课程
        Long courseId = toLong(payload.get("course_id"));
        CourseChapter chapter = getChapterById(sectionAddRequest.getChapterId());
        if (courseId != null && !courseId.equals(chapter.getCourseId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "章节不属于该课程");
        }
        // 校验当前管理员或讲师是否具备目标课程的管理权限
        courseService.assertCanManageCourse(loginUser, chapter.getCourseId());

        // 创建课程小节，并返回新生成的小节 ID
        Long sectionId = sectionService.createSection(sectionAddRequest, loginUser);
        return ResultUtils.success(sectionId);
    }

    /**
     * 上传视频并创建对应课程小节。
     *
     * @param file 视频文件
     * @param uploadVideoRequest 视频小节创建参数
     * @param request 当前 HTTP 请求
     * @return 新建小节 ID
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Long> uploadVideoAndAddSection(
            @RequestParam("file") MultipartFile file,
            UploadVideoRequest uploadVideoRequest,
            HttpServletRequest request) {
        // 校验上传请求体，避免缺少课程和章节等必要信息
        if (uploadVideoRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 校验课程和章节参数，并确认章节确实属于该课程
        Long courseId = uploadVideoRequest.getCourseId();
        Long chapterId = uploadVideoRequest.getChapterId();
        if (courseId == null || courseId <= 0 || chapterId == null || chapterId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程或章节参数不合法");
        }
        if (courseService.getById(courseId) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程不存在");
        }
        CourseChapter chapter = getChapterById(chapterId);
        if (!courseId.equals(chapter.getCourseId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "章节不属于该课程");
        }

        // 校验当前管理员或讲师是否具备目标课程管理权限
        User loginUser = userService.getLoginUser(request);
        courseService.assertCanManageCourse(loginUser, courseId);

        // 先上传视频文件获取访问地址，再作为小节资源地址保存
        BaseResponse<String> uploadResponse = fileController.uploadVideo(file, request);
        String videoUrl = uploadResponse.getData();

        // 组装标准小节创建请求，兼容旧版上传接口默认创建视频小节
        SectionAddRequest sectionAddRequest = new SectionAddRequest();
        sectionAddRequest.setChapterId(chapterId);
        sectionAddRequest.setTitle(uploadVideoRequest.getTitle());
        sectionAddRequest.setType("video");
        sectionAddRequest.setResourceType("FILE");
        sectionAddRequest.setResourceUrl(videoUrl);
        sectionAddRequest.setContentType("VIDEO");
        sectionAddRequest.setVideoDuration(300);
        sectionAddRequest.setSortOrder(uploadVideoRequest.getSort());
        sectionAddRequest.setIsFree(0);

        long sectionId = sectionService.createSection(sectionAddRequest, loginUser);
        return ResultUtils.success(sectionId);
    }

    /**
     * 删除指定课程小节。
     *
     * @param deleteRequest 小节删除请求
     * @param request 当前 HTTP 请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Boolean> deleteSection(@RequestBody DeleteRequest deleteRequest,
                                               HttpServletRequest request) {
        // 校验小节 id，避免删除请求没有明确目标
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 查询旧小节并通过章节反查课程，校验当前用户管理权限
        Long id = deleteRequest.getId();
        Section oldSection = getSectionByIdOrThrow(id);
        User loginUser = userService.getLoginUser(request);
        courseService.assertCanManageCourse(loginUser, getCourseIdByChapterId(oldSection.getChapterId()));
        // 删除小节记录，具体删除策略沿用 SectionService 配置
        boolean result = sectionService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 更新课程小节基础信息和资源信息。
     *
     * @param courseSection 旧版课程小节更新参数
     * @param request 当前 HTTP 请求
     * @return 是否更新成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Boolean> updateSection(@RequestBody CourseSection courseSection,
                                               HttpServletRequest request) {
        // 校验小节 id，更新必须先定位已有小节
        if (courseSection == null || courseSection.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取原小节，并根据原章节或新章节计算目标课程用于权限校验
        Section oldSection = getSectionByIdOrThrow(courseSection.getId());
        User loginUser = userService.getLoginUser(request);
        Long targetCourseId = getCourseIdByChapterId(oldSection.getChapterId());
        if (courseSection.getChapterId() != null) {
            targetCourseId = getCourseIdByChapterId(courseSection.getChapterId());
        }
        courseService.assertCanManageCourse(loginUser, targetCourseId);

        // 只设置允许更新的字段，未传 chapterId 时保持原章节归属
        Section updateSection = new Section();
        updateSection.setId(courseSection.getId());
        updateSection.setChapterId(courseSection.getChapterId() != null ? courseSection.getChapterId() : oldSection.getChapterId());
        updateSection.setTitle(courseSection.getTitle());
        updateSection.setVideoUrl(courseSection.getVideoUrl());
        updateSection.setVideoDuration(courseSection.getDuration());
        updateSection.setResourceUrl(courseSection.getResourceUrl());
        updateSection.setSortOrder(courseSection.getSort());
        updateSection.setIsFree(courseSection.getIsFree());
        // 如果更新了视频或资源地址，同步标记为视频类型资源
        if (courseSection.getVideoUrl() != null || courseSection.getResourceUrl() != null) {
            updateSection.setType("video");
            updateSection.setContentType("VIDEO");
            updateSection.setResourceType(courseSection.getResourceUrl() != null ? "FILE" : oldSection.getResourceType());
        }
        boolean result = sectionService.updateById(updateSection);
        return ResultUtils.success(result);
    }

    /**
     * 替换指定小节的视频文件。
     *
     * @param file 新的视频文件
     * @param sectionId 小节 ID
     * @param request 当前 HTTP 请求
     * @return 是否更新成功
     */
    @PostMapping("/update/video")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Boolean> updateSectionVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sectionId") Long sectionId,
            HttpServletRequest request) {
        // 查询小节并校验当前用户是否能管理该小节所属课程
        Section oldSection = getSectionByIdOrThrow(sectionId);
        User loginUser = userService.getLoginUser(request);
        courseService.assertCanManageCourse(loginUser, getCourseIdByChapterId(oldSection.getChapterId()));

        // 上传新视频并取得可访问地址
        BaseResponse<String> uploadResponse = fileController.uploadVideo(file, request);
        String videoUrl = uploadResponse.getData();

        // 仅更新视频相关字段，保留小节其他基础信息
        Section updatedSection = new Section();
        updatedSection.setId(sectionId);
        updatedSection.setVideoUrl(videoUrl);
        updatedSection.setResourceUrl(videoUrl);
        updatedSection.setResourceType("FILE");
        updatedSection.setContentType("VIDEO");
        updatedSection.setType("video");
        updatedSection.setVideoDuration(300);

        boolean result = sectionService.updateById(updatedSection);
        return ResultUtils.success(result);
    }

    @GetMapping("/get")
    public BaseResponse<CourseSection> getSectionById(@RequestParam("id") Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Section section = getSectionByIdOrThrow(id);
        return ResultUtils.success(toLegacyCourseSection(section));
    }

    /**
     * 查询指定课程下的全部小节。
     *
     * @param courseId 课程 ID
     * @return 课程小节列表
     */
    @GetMapping("/list/course")
    public BaseResponse<List<CourseSection>> listSectionsByCourseId(@RequestParam("course_id") Long courseId) {
        // 校验课程 ID，避免查询无效课程的小节数据
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 先查询课程下所有章节 ID，小节数据通过章节归属进行关联
        List<Long> chapterIds = listChapterIdsByCourseId(courseId);
        if (chapterIds.isEmpty()) {
            return ResultUtils.success(List.of());
        }

        // 查询未删除小节并按排序字段升序返回，同时转换为旧版 CourseSection 结构
        List<CourseSection> sectionList = sectionService.list(new LambdaQueryWrapper<Section>()
                        .in(Section::getChapterId, chapterIds)
                        .eq(Section::getIsDelete, 0)
                        .orderByAsc(Section::getSortOrder))
                .stream()
                .map(this::toLegacyCourseSection)
                .collect(Collectors.toList());
        return ResultUtils.success(sectionList);
    }

    /**
     * 查询指定章节下的全部小节。
     *
     * @param chapterId 章节 ID
     * @return 章节小节列表
     */
    @GetMapping("/list/chapter")
    public BaseResponse<List<CourseSection>> listSectionsByChapterId(@RequestParam("chapterId") Long chapterId) {
        // 校验章节 ID，避免查询无效章节
        if (chapterId == null || chapterId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 查询章节下未删除小节，并转换为旧版 CourseSection 返回结构
        List<CourseSection> sectionList = sectionService.list(new LambdaQueryWrapper<Section>()
                        .eq(Section::getChapterId, chapterId)
                        .eq(Section::getIsDelete, 0)
                        .orderByAsc(Section::getSortOrder))
                .stream()
                .map(this::toLegacyCourseSection)
                .collect(Collectors.toList());
        return ResultUtils.success(sectionList);
    }

    /**
     * 分页查询课程或章节下的小节列表。
     *
     * @param courseId 课程 ID，可为空
     * @param chapterId 章节 ID，可为空
     * @param current 当前页码
     * @param pageSize 每页数量
     * @return 小节分页数据
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<CourseSection>> listSectionsByPage(
            @RequestParam(value = "course_id", required = false) Long courseId,
            @RequestParam(value = "chapterId", required = false) Long chapterId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 课程 ID 和章节 ID 至少传一个，用于限定小节查询范围
        if ((courseId == null || courseId <= 0) && (chapterId == null || chapterId <= 0)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "课程ID和章节ID不能同时为空");
        }

        // 优先按章节查询；未传章节时先查课程下的章节集合，再按章节集合分页查询小节
        LambdaQueryWrapper<Section> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Section::getIsDelete, 0).orderByAsc(Section::getSortOrder);
        if (chapterId != null && chapterId > 0) {
            wrapper.eq(Section::getChapterId, chapterId);
        } else {
            List<Long> chapterIds = listChapterIdsByCourseId(courseId);
            if (chapterIds.isEmpty()) {
                return ResultUtils.success(new Page<>(current, pageSize, 0));
            }
            wrapper.in(Section::getChapterId, chapterIds);
        }

        // 分页查询小节实体，并转换为旧版 CourseSection 分页结构
        Page<Section> sectionPage = sectionService.page(new Page<>(current, pageSize), wrapper);
        Page<CourseSection> legacyPage = new Page<>(sectionPage.getCurrent(), sectionPage.getSize(), sectionPage.getTotal());
        legacyPage.setRecords(sectionPage.getRecords().stream().map(this::toLegacyCourseSection).collect(Collectors.toList()));
        return ResultUtils.success(legacyPage);
    }

    /**
     * 统计指定课程下全部小节的视频总时长。
     *
     * @param courseId 课程 ID
     * @return 视频总时长
     */
    @GetMapping("/duration")
    public BaseResponse<Integer> getTotalDuration(@RequestParam("course_id") Long courseId) {
        // 校验课程 ID，避免统计无效课程
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 复用课程小节列表接口，汇总每个小节的 duration 字段
        List<CourseSection> sectionList = listSectionsByCourseId(courseId).getData();
        int totalDuration = sectionList.stream()
                .map(CourseSection::getDuration)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        return ResultUtils.success(totalDuration);
    }

    /**
     * 统计指定课程下的有效小节数量。
     *
     * @param courseId 课程 ID
     * @return 小节数量
     */
    @GetMapping("/count")
    public BaseResponse<Integer> getSectionCount(@RequestParam("course_id") Long courseId) {
        // 校验课程 ID，避免统计无效课程
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 先查出课程下的章节 ID，没有章节时小节数量直接为 0
        List<Long> chapterIds = listChapterIdsByCourseId(courseId);
        if (chapterIds.isEmpty()) {
            return ResultUtils.success(0);
        }

        // 统计这些章节下未删除的小节数量
        int count = Math.toIntExact(sectionService.count(new LambdaQueryWrapper<Section>()
                .in(Section::getChapterId, chapterIds)
                .eq(Section::getIsDelete, 0)));
        return ResultUtils.success(count);
    }

    private Section getSectionByIdOrThrow(Long sectionId) {
        Section section = sectionService.getById(sectionId);
        if (section == null || (section.getIsDelete() != null && section.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "小节不存在");
        }
        return section;
    }

    private CourseChapter getChapterById(Long chapterId) {
        if (chapterId == null || chapterId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "章节ID不合法");
        }
        CourseChapter chapter = courseChapterService.getById(chapterId);
        if (chapter == null || (chapter.getIsDelete() != null && chapter.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "章节不存在");
        }
        return chapter;
    }

    private List<Long> listChapterIdsByCourseId(Long courseId) {
        List<CourseChapter> chapters = courseChapterService.getChaptersByCourseId(courseId);
        return chapters.stream()
                .map(CourseChapter::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Long getCourseIdByChapterId(Long chapterId) {
        return getChapterById(chapterId).getCourseId();
    }

    private CourseSection toLegacyCourseSection(Section section) {
        CourseSection legacy = new CourseSection();
        legacy.setId(section.getId());
        legacy.setChapterId(section.getChapterId());
        legacy.setCourseId(getCourseIdByChapterId(section.getChapterId()));
        legacy.setTitle(section.getTitle());
        legacy.setVideoUrl(section.getVideoUrl());
        legacy.setDuration(section.getVideoDuration());
        legacy.setSort(section.getSortOrder());
        legacy.setIsFree(section.getIsFree());
        legacy.setResourceUrl(section.getResourceUrl());
        legacy.setResourceType("VIDEO".equalsIgnoreCase(section.getContentType()) ? 1 : 3);
        legacy.setCreateTime(section.getCreateTime());
        legacy.setUpdateTime(section.getUpdateTime());
        legacy.setIsDelete(section.getIsDelete());
        return legacy;
    }

    /**
     * 将旧版小节参数 Map 转换为标准 SectionAddRequest。
     *
     * @param payload 旧版小节参数
     * @return 标准小节创建请求
     */
    private SectionAddRequest buildSectionAddRequest(Map<String, Object> payload) {
        // 读取章节、标题、内容、时长、排序和免费状态等基础字段
        SectionAddRequest request = new SectionAddRequest();
        request.setChapterId(toLong(payload.get("chapterId")));
        request.setTitle(toStringValue(payload.get("title")));
        request.setContent(toStringValue(payload.get("content")));
        request.setVideoDuration(toInteger(firstNonNull(payload.get("videoDuration"), payload.get("duration")), 300));
        request.setSortOrder(toInteger(firstNonNull(payload.get("sortOrder"), payload.get("sort")), 0));
        request.setIsFree(toInteger(payload.get("isFree"), 0));

        // 兼容 resourceUrl/videoUrl、type/contentType、resourceType 等旧版字段
        String resourceUrl = toStringValue(payload.get("resourceUrl"));
        String videoUrl = toStringValue(payload.get("videoUrl"));
        String type = toStringValue(payload.get("type"));
        String contentType = toStringValue(payload.get("contentType"));
        Integer resourceType = toInteger(payload.get("resourceType"), 1);

        // 未传资源地址时使用视频地址兜底，兼容旧版只传 videoUrl 的请求
        if (resourceUrl == null || resourceUrl.isBlank()) {
            resourceUrl = videoUrl;
        }

        // 根据资源地址和类型字段推断小节资源类型、展示类型和内容类型
        request.setResourceUrl(resourceUrl);
        request.setResourceType(resolveResourceType(resourceType, resourceUrl));
        request.setType(resolveSectionType(type, contentType, resourceUrl));
        request.setContentType(resolveContentType(contentType, type, resourceUrl));
        return request;
    }

    private String resolveResourceType(Integer resourceType, String resourceUrl) {
        if (resourceType != null && resourceType == 1) {
            return "URL";
        }
        if (resourceUrl != null && (resourceUrl.startsWith("http://") || resourceUrl.startsWith("https://"))) {
            return "URL";
        }
        return "FILE";
    }

    private String resolveSectionType(String type, String contentType, String resourceUrl) {
        if (type != null && !type.isBlank()) {
            return type;
        }
        if ("ARTICLE".equalsIgnoreCase(contentType)) {
            return "article";
        }
        if ("EXERCISE".equalsIgnoreCase(contentType)) {
            return "exercise";
        }
        if (resourceUrl == null || resourceUrl.isBlank()) {
            return "article";
        }
        return "video";
    }

    private String resolveContentType(String contentType, String type, String resourceUrl) {
        if (contentType != null && !contentType.isBlank()) {
            return contentType.toUpperCase();
        }
        if ("article".equalsIgnoreCase(type)) {
            return "ARTICLE";
        }
        if ("exercise".equalsIgnoreCase(type)) {
            return "EXERCISE";
        }
        if (resourceUrl == null || resourceUrl.isBlank()) {
            return "ARTICLE";
        }
        return "VIDEO";
    }

    private Object firstNonNull(Object first, Object second) {
        return first != null ? first : second;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Integer toInteger(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return defaultValue;
        }
        return Integer.parseInt(text);
    }

    private String toStringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }
}
