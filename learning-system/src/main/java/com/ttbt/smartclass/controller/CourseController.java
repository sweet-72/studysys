package com.ttbt.smartclass.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.common.DeleteRequest;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.constant.UserConstant;
import com.ttbt.smartclass.annotation.AuthCheck;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.CourseChapterMapper;
import com.ttbt.smartclass.mapper.SectionMapper;
import com.ttbt.smartclass.model.dto.HomeworkGradeRequest;
import com.ttbt.smartclass.model.dto.SubmitAnswerRequest;
import com.ttbt.smartclass.model.dto.SubmitAnswerResponse;
import com.ttbt.smartclass.model.dto.VideoProgressSaveRequest;
import com.ttbt.smartclass.model.dto.course.CourseAddRequest;
import com.ttbt.smartclass.model.dto.course.CourseManageCreateRequest;
import com.ttbt.smartclass.model.dto.course.HomeworkAddRequest;
import com.ttbt.smartclass.model.dto.course.CourseStartRequest;
import com.ttbt.smartclass.model.dto.course.CourseUpdateRequest;
import com.ttbt.smartclass.model.entity.Course;
import com.ttbt.smartclass.model.entity.CourseChapter;
import com.ttbt.smartclass.model.entity.Question;
import com.ttbt.smartclass.model.entity.Section;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.CourseCatalogVO;
import com.ttbt.smartclass.model.vo.CourseDetailVO;
import com.ttbt.smartclass.model.vo.CourseProgressVO;
import com.ttbt.smartclass.model.vo.CourseVO;
import com.ttbt.smartclass.model.vo.ExerciseQuestionVO;
import com.ttbt.smartclass.model.vo.SectionVO;
import com.ttbt.smartclass.service.CourseManageAdminService;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.QuestionService;
import com.ttbt.smartclass.service.SectionService;
import com.ttbt.smartclass.service.UserCourseService;
import com.ttbt.smartclass.service.UserService;
import com.ttbt.smartclass.service.VideoLearningRecordService;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 课程控制器
 */
@Slf4j
@RestController
@RequestMapping("/course")
public class CourseController {

    @Resource
    private CourseService courseService;

    @Resource
    private SectionMapper sectionMapper;

    @Resource
    private CourseChapterMapper courseChapterMapper;

    @Resource
    private SectionService sectionService;

    @Resource
    private UserCourseService userCourseService;

    @Resource
    private VideoLearningRecordService videoLearningRecordService;

    @Resource
    private UserService userService;

    @Resource
    private CourseManageAdminService courseManageAdminService;

    @Resource
    private QuestionService questionService;
    /**
     * 新增课程，兼容旧版课程添加接口。
     * POST /api/course/add
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Long> addCourse(
            @RequestBody CourseAddRequest courseAddRequest,
            HttpServletRequest request
    ) {
        User loginUser = userService.getLoginUser(request);
        Long courseId = courseService.createCourse(courseAddRequest, loginUser);
        return ResultUtils.success(courseId);
    }

    /**
     * 管理员或讲师创建课程，支持课程管理端提交完整课程信息。
     * POST /api/course/create
     */
    @PostMapping("/create")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Long> createCourse(
            @Valid @RequestBody CourseManageCreateRequest courseCreateRequest,
            HttpServletRequest request
    ) {
        User loginUser = userService.getLoginUser(request);
        Long courseId = courseManageAdminService.createCourse(courseCreateRequest, loginUser);
        return ResultUtils.success(courseId);
    }

    /**
     * 上传课程视频文件并返回访问地址，兼容旧版视频上传接口。
     * POST /api/course/upload
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<String> uploadVideo(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        User loginUser = userService.getLoginUser(request);
        String accessUrl = courseService.uploadVideo(file, loginUser);
        return ResultUtils.success(accessUrl);
    }

    /**
     * 上传课程视频文件或接收外部视频 URL，返回可用视频地址。
     * POST /api/course/upload/video
     */
    @PostMapping("/upload/video")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<String> uploadCourseVideo(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "videoUrl", required = false) String videoUrl,
            HttpServletRequest request
    ) {
        User loginUser = userService.getLoginUser(request);
        String source = courseManageAdminService.handleVideoUpload(file, videoUrl, loginUser);
        return ResultUtils.success(source);
    }

    /**
     * 分页查询课程列表，支持用户端和管理端共用。
     *
     * @return 课程分页数据
     */
    @GetMapping("/list/page/vo")
    public BaseResponse<Page<CourseVO>> getCourseListByPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer courseType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            HttpServletRequest request
    ) {
        // 解析当前登录用户，判断是否为管理员或讲师管理视角
        User loginUser = userService.getLoginUserPermitNull(request);
        boolean managerView = loginUser != null
                && (userService.isAdmin(loginUser) || UserConstant.TEACHER_ROLE.equals(loginUser.getUserRole()));

        // 根据标题、类型、难度、教师、标签和价格区间构造课程筛选条件
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getIsDelete, 0);
        queryWrapper.like(StringUtils.isNotBlank(title), Course::getTitle, title);
        queryWrapper.eq(courseType != null, Course::getCourseType, courseType);
        queryWrapper.eq(difficulty != null, Course::getDifficulty, difficulty);
        queryWrapper.eq(teacherId != null, Course::getTeacherId, teacherId);
        queryWrapper.like(StringUtils.isNotBlank(tags), Course::getTags, tags);
        queryWrapper.ge(minPrice != null, Course::getPrice, minPrice);
        queryWrapper.le(maxPrice != null, Course::getPrice, maxPrice);

        // 分类字段兼容 categoryId 和旧版字符串 category 两种存储方式
        if (categoryId != null) {
            queryWrapper.and(qw -> qw.eq(Course::getCategoryId, categoryId)
                    .or()
                    .eq(Course::getCategory, String.valueOf(categoryId)));
        }

        // 管理端可按传入状态筛选，普通用户只能查看已发布课程
        if (managerView) {
            queryWrapper.eq(status != null, Course::getStatus, status);
        } else {
            queryWrapper.eq(Course::getStatus, 1);
        }

        // 应用前端传入的排序字段，未传或不支持时按创建时间倒序
        applySort(queryWrapper, sortField, sortOrder);

        // 分页查询课程实体，并转换为前端展示 VO
        Page<Course> coursePage = courseService.page(new Page<>(current, pageSize), queryWrapper);
        Page<CourseVO> voPage = new Page<>(coursePage.getCurrent(), coursePage.getSize(), coursePage.getTotal());
        // 按当前用户角色控制 AI 知识库字段是否可见
        voPage.setRecords(coursePage.getRecords().stream()
                .map(course -> toCourseVO(course, shouldIncludeAiKnowleage(loginUser, course)))
                .toList());
        return ResultUtils.success(voPage);
    }

    /**
     * 查询课程列表，支持按标题、类型、难度、状态、分类、教师、价格和标签筛选，并返回分页课程信息。
     * GET /api/course/list
     */
    @GetMapping("/list")
    public BaseResponse<Page<CourseVO>> getCourseListRest(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer courseType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            HttpServletRequest request
    ) {
        return getCourseListByPage(current, pageSize, title, courseType, difficulty, status, categoryId,
                teacherId, minPrice, maxPrice, tags, sortField, sortOrder, request);
    }

    /**
     * 根据课程 ID 查询当前登录用户的课程学习进度。
     * GET /api/course/progress
     */
    @GetMapping("/progress")
    public BaseResponse<CourseProgressVO> getCourseProgressByParam(
            @RequestParam Long courseId,
            HttpServletRequest request
    ) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            return ResultUtils.error(401, "not login");
        }
        return ResultUtils.success(courseService.getCourseProgress(courseId, loginUser.getId()));
    }

    /**
     * 当前登录用户开始学习指定课程，创建或更新用户课程学习记录。
     * POST /api/course/start
     */
    @PostMapping("/start")
    public BaseResponse<Boolean> startCourse(
            @RequestBody CourseStartRequest body,
            HttpServletRequest request
    ) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            return ResultUtils.error(401, "not login");
        }
        userCourseService.startLearning(loginUser.getId(), body.getCourseId());
        return ResultUtils.success(true);
    }

    /**
     * 保存当前登录用户的视频学习进度，用于续播和课程进度统计。
     * POST /api/course/progress/save
     */
    @PostMapping("/progress/save")
    public BaseResponse<Boolean> saveCourseVideoProgress(
            @RequestBody VideoProgressSaveRequest request,
            HttpServletRequest httpRequest
    ) {
        User loginUser = userService.getLoginUser(httpRequest);
        if (loginUser == null) {
            return ResultUtils.error(401, "not login");
        }
        videoLearningRecordService.saveVideoProgress(request, loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * 查询课程小节详情，返回小节基础信息和学习内容。
     *
     * GET /api/section/{sectionId}/detail
     *
     * @param sectionId 小节 ID
     * @return 小节详情信息
     */
    @GetMapping("/section/{sectionId}/detail")
    public BaseResponse<SectionVO> getSectionDetail(@PathVariable Long sectionId) {
        SectionVO sectionVO = sectionService.getSectionDetail(sectionId);
        return ResultUtils.success(sectionVO);
    }

    /**
     * 查询课程目录，返回章节、小节以及用户学习状态。
     *
     * GET /api/course/{courseId}/catalog
     *
     * @param courseId 课程 ID
     * @param userId 用户 ID，可选；未传时按当前登录用户解析
     * @return 课程目录信息
     */
    @GetMapping("/{course_id}/catalog")
    public BaseResponse<CourseCatalogVO> getCourseCatalog(
            @PathVariable Long courseId,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request
    ) {
        Long resolvedUserId = resolveRequestedUserId(request, userId);
        CourseCatalogVO catalogVO = courseService.getCourseCatalog(courseId, resolvedUserId);
        return ResultUtils.success(catalogVO);
    }

    /**
     * 查询指定小节内容，并从课程目录中提取该小节的展示信息。
     *
     * GET /api/section/{sectionId}
     *
     * @param sectionId 小节 ID
     * @param userId 用户 ID
     * @return 小节内容信息
     */
    @GetMapping("/section/{sectionId}")
    public BaseResponse<CourseCatalogVO.SectionInfo> getSectionContent(
            @PathVariable Long sectionId,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request
    ) {
        Long resolvedUserId = resolveRequestedUserId(request, userId);
        // 先调用 Service 校验并加载小节内容。
        courseService.getSectionContent(sectionId, resolvedUserId);

        // Resolve section and chapter from sectionId.
        Section section = sectionMapper.selectById(sectionId);
        if (section == null) {
            return ResultUtils.error(404, "section not found");
        }

        CourseChapter chapter = courseChapterMapper.selectById(section.getChapterId());
        if (chapter == null) {
            return ResultUtils.error(404, "chapter not found");
        }

        CourseCatalogVO catalogVO = courseService.getCourseCatalog(chapter.getCourseId(), resolvedUserId);

        // 从课程目录中查找当前小节信息。
        CourseCatalogVO.SectionInfo sectionInfo = catalogVO.getChapters().stream()
            .flatMap(chapterItem -> chapterItem.getSections().stream())
            .filter(s -> s.getSectionId().equals(sectionId))
            .findFirst()
            .orElse(null);

        if (sectionInfo == null) {
            return ResultUtils.error(404, "section not found");
        }

        return ResultUtils.success(sectionInfo);
    }

    /**
     * 查询指定小节下的练习题列表。
     *
     * GET /api/section/{sectionId}/questions
     *
     * @param sectionId 小节 ID
     * @return 小节练习题列表
     */
    @GetMapping("/{sectionId}/questions")
    public BaseResponse<List<Question>> getSectionQuestions(@PathVariable Long sectionId) {
        List<Question> questions = courseService.getSectionQuestions(sectionId);
        return ResultUtils.success(questions);
    }

    /**
     * 提交用户练习答案并返回判题结果。
     *
     * POST /api/question/submit
     *
     * @param request 答题提交请求
     * @return 答题结果
     */
    @PostMapping("/question/submit")
    public BaseResponse<SubmitAnswerResponse> submitAnswer(@RequestBody SubmitAnswerRequest request,
                                                           HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        request.setUserId(loginUser.getId());
        SubmitAnswerResponse response = courseService.submitAnswer(request);
        return ResultUtils.success(response);
    }

    /**
     * 查询指定用户在指定课程中的学习进度。
     *
     * GET /api/course/{courseId}/progress
     *
     * @param courseId 课程 ID
     * @param userId 用户 ID
     * @return 课程学习进度
     */
    @GetMapping("/{course_id}/progress")
    public BaseResponse<CourseProgressVO> getCourseProgress(
            @PathVariable Long courseId,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request
    ) {
        Long resolvedUserId = resolveRequestedUserId(request, userId);
        if (resolvedUserId == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "please login first");
        }
        CourseProgressVO progressVO = courseService.getCourseProgress(courseId, resolvedUserId);
        return ResultUtils.success(progressVO);
    }

    /**
     * 查询课程详情，并结合用户学习记录返回课程与小节信息。
     * GET /api/course/{id}
     */
    @GetMapping("/{id}")
    public BaseResponse<CourseDetailVO> getCourseDetailRest(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request
    ) {
        Long uid = resolveRequestedUserId(request, userId);
        CourseDetailVO detailVO = courseService.getCourseDetail(id, uid);
        Course course = courseService.getById(id);
        if (course != null && shouldIncludeAiKnowleage(userService.getLoginUserPermitNull(request), course)) {
            detailVO.setAiKnowleage(course.getAiKnowleage());
        }
        return ResultUtils.success(detailVO);
    }

    /**
     * 根据课程 ID 查询课程基础信息。
     *
     * @param id 课程 ID
     * @param request 当前 HTTP 请求
     * @return 课程基础信息
     */
    @GetMapping("/get")
    public BaseResponse<Course> getCourseById(@RequestParam("id") Long id, HttpServletRequest request) {
        // 查询课程实体，并过滤不存在或已逻辑删除的课程
        Course course = courseService.getById(id);
        if (course == null || (course.getIsDelete() != null && course.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 复制课程对象作为返回值，避免直接修改数据库实体中的字段
        Course responseCourse = new Course();
        BeanUtils.copyProperties(course, responseCourse);
        // 非管理员且非课程所属讲师时，隐藏课程 AI 知识库内容
        if (!shouldIncludeAiKnowleage(userService.getLoginUserPermitNull(request), course)) {
            responseCourse.setAiKnowleage(null);
        }
        return ResultUtils.success(responseCourse);
    }

    /**
     * 根据课程 ID 查询课程展示信息。
     *
     * @param id 课程 ID
     * @param request 当前 HTTP 请求
     * @return 课程展示信息
     */
    @GetMapping("/get/vo")
    public BaseResponse<CourseVO> getCourseVOById(@RequestParam("id") Long id, HttpServletRequest request) {
        // 查询课程实体，并过滤不存在或已逻辑删除的课程
        Course course = courseService.getById(id);
        if (course == null || (course.getIsDelete() != null && course.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 转换为前端展示 VO，并按当前用户权限决定是否包含 AI 知识库内容
        return ResultUtils.success(toCourseVO(course, shouldIncludeAiKnowleage(userService.getLoginUserPermitNull(request), course)));
    }

    @GetMapping("/teacher/{teacherId}")
    public BaseResponse<List<CourseVO>> getCoursesByTeacher(@PathVariable Long teacherId,
                                                            HttpServletRequest request) {
        return ResultUtils.success(courseService.getCoursesByTeacher(teacherId, userService.getLoginUserPermitNull(request)));
    }

    /**
     * 分页查询当前管理用户可管理的课程列表。
     *
     * @param current 当前页码
     * @param pageSize 每页数量
     * @param request 当前 HTTP 请求
     * @return 当前用户可管理的课程分页数据
     */
    @GetMapping("/my/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Page<CourseVO>> listMyCourseVOByPage(@RequestParam(defaultValue = "1") Long current,
                                                             @RequestParam(defaultValue = "10") Long pageSize,
                                                             HttpServletRequest request) {
        // 获取当前登录用户，用于区分管理员视角和讲师视角
        User loginUser = userService.getLoginUser(request);
        // 管理员可查看全部课程，直接复用通用课程分页查询逻辑
        if (UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            return getCourseListByPage(current, pageSize, null, null, null, null, null, null, null, null, null, null, null, request);
        }
        // 讲师只能查看自己负责且未删除的课程
        Page<Course> page = courseService.page(new Page<>(current, pageSize),
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getTeacherId, loginUser.getId())
                        .eq(Course::getIsDelete, 0)
                        .orderByDesc(Course::getCreateTime));
        // 将课程实体转换为管理端需要的 VO，并保留 AI 知识库字段
        Page<CourseVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(course -> toCourseVO(course, true)).toList());
        return ResultUtils.success(voPage);
    }

    /**
     * 更新课程管理端提交的课程信息。
     *
     * @param courseUpdateRequest 课程更新请求
     * @param request 当前 HTTP 请求
     * @return 是否更新成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Boolean> updateCourse(@Valid @RequestBody CourseUpdateRequest courseUpdateRequest,
                                              HttpServletRequest request) {
        // 获取当前管理用户，由管理服务校验课程归属和可编辑权限
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(courseManageAdminService.updateCourse(courseUpdateRequest, loginUser));
    }

    /**
     * 删除管理员或讲师可管理的课程。
     *
     * @param deleteRequest 删除课程请求
     * @param request 当前 HTTP 请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Boolean> deleteCourse(@RequestBody DeleteRequest deleteRequest,
                                              HttpServletRequest request) {
        // 校验课程 id，避免删除请求缺少明确目标
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前管理用户，由管理服务判断是否允许删除该课程
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(courseManageAdminService.deleteCourse(deleteRequest.getId(), loginUser));
    }

    /**
     * 分页查询指定小节下的作业题目列表。
     *
     * @param sectionId 小节 ID
     * @param current 当前页码
     * @param pageSize 每页数量
     * @param httpRequest 当前 HTTP 请求
     * @return 作业题目分页数据
     */
    @GetMapping("/homework/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Page<ExerciseQuestionVO>> listHomeworkVOByPage(
            @RequestParam("sectionId") Long sectionId,
            @RequestParam(value = "current", defaultValue = "1") Long current,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize,
            HttpServletRequest httpRequest
    ) {
        // 校验小节 id 和分页参数，管理端单次最多查询 20 条作业题目
        if (sectionId == null || sectionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "sectionId is required");
        }
        long safeCurrent = current == null || current <= 0 ? 1L : current;
        long safePageSize = pageSize == null || pageSize <= 0 ? 10L : pageSize;
        if (safePageSize > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "pageSize cannot exceed 20");
        }

        // 获取当前管理用户，确认小节存在且用户有权管理其所属课程
        User loginUser = userService.getLoginUser(httpRequest);
        Section section = sectionService.getById(sectionId);
        if (section == null || (section.getIsDelete() != null && section.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "section not found");
        }
        Long courseId = resolveCourseIdBySectionId(sectionId);
        courseService.assertCanManageCourse(loginUser, courseId);

        // 按小节查询未删除题目，并按排序字段和题目 id 稳定排序
        Page<Question> questionPage = new Page<>(safeCurrent, safePageSize);
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getSectionId, sectionId)
                .eq(Question::getIsDelete, 0)
                .orderByAsc(Question::getSortOrder)
                .orderByAsc(Question::getId);
        Page<Question> pageResult = questionService.page(questionPage, queryWrapper);

        // 将题目实体转换为作业题目 VO，返回管理端需要展示的字段
        Page<ExerciseQuestionVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        List<ExerciseQuestionVO> voList = pageResult.getRecords().stream().map(question -> {
            ExerciseQuestionVO vo = new ExerciseQuestionVO();
            vo.setId(question.getId());
            vo.setSectionId(question.getSectionId());
            vo.setType(question.getType());
            vo.setContent(question.getContent());
            vo.setOptions(question.getOptions());
            vo.setScore(question.getScore());
            vo.setDifficulty(question.getDifficulty());
            vo.setSortOrder(question.getSortOrder());
            return vo;
        }).collect(java.util.stream.Collectors.toList());
        voPage.setRecords(voList);
        return ResultUtils.success(voPage);
    }

    /**
     * 为指定小节新增作业题目。
     *
     * @param request 作业题目新增请求
     * @param httpRequest 当前 HTTP 请求
     * @return 新增题目 id
     */
    @PostMapping("/homework/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Long> addHomework(@Valid @RequestBody HomeworkAddRequest request,
                                          HttpServletRequest httpRequest) {
        // 获取当前管理用户，并确认目标小节存在且未删除
        User loginUser = userService.getLoginUser(httpRequest);
        Section section = sectionService.getById(request.getSectionId());
        if (section == null || (section.getIsDelete() != null && section.getIsDelete() != 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "section not found");
        }
        // 根据小节反查课程，校验管理员或讲师是否有管理权限
        Long courseId = resolveCourseIdBySectionId(section.getId());
        courseService.assertCanManageCourse(loginUser, courseId);

        // 未指定排序时，取当前小节最后一道题的排序值并顺延
        Integer sortOrder = request.getSortOrder();
        if (sortOrder == null) {
            LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Question::getSectionId, request.getSectionId())
                    .orderByDesc(Question::getSortOrder)
                    .orderByDesc(Question::getId)
                    .last("limit 1");
            Question lastQuestion = questionService.getOne(queryWrapper, false);
            sortOrder = (lastQuestion == null || lastQuestion.getSortOrder() == null)
                    ? 1
                    : lastQuestion.getSortOrder() + 1;
        }

        // 将请求字段转换为题目实体，补齐题型、分值、难度等默认值后保存
        Question question = new Question();
        question.setSectionId(request.getSectionId());
        question.setType(StringUtils.isBlank(request.getType()) ? "subjective" : request.getType().trim());
        question.setContent(request.getContent().trim());
        question.setOptions(request.getOptions());
        question.setAnswer(StringUtils.defaultString(request.getAnswer()));
        question.setExplanation(request.getExplanation());
        question.setScore(request.getScore() == null ? 10 : request.getScore());
        question.setDifficulty(request.getDifficulty() == null ? 1 : request.getDifficulty());
        question.setSortOrder(sortOrder);
        questionService.save(question);
        return ResultUtils.success(question.getId());
    }

    /**
     * 删除指定作业题目。
     *
     * @param questionId 题目 id
     * @param httpRequest 当前 HTTP 请求
     * @return 是否删除成功
     */
    @DeleteMapping("/homework/delete/{questionId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Boolean> deleteHomework(@PathVariable Long questionId,
                                                HttpServletRequest httpRequest) {
        // 校验题目 id，避免无目标删除
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "questionId is required");
        }
        // 查询题目并确认可通过小节归属反查课程
        User loginUser = userService.getLoginUser(httpRequest);
        Question question = questionService.getById(questionId);
        if (question == null || question.getSectionId() == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "question not found");
        }
        // 校验当前管理用户是否有权限管理题目所属课程
        Long courseId = resolveCourseIdBySectionId(question.getSectionId());
        courseService.assertCanManageCourse(loginUser, courseId);
        return ResultUtils.success(questionService.removeById(questionId));
    }

    /**
     * 兼容旧版接口删除作业题目。
     *
     * @param questionId 题目 id
     * @param httpRequest 当前 HTTP 请求
     * @return 是否删除成功
     */
    @PostMapping("/homework/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Boolean> deleteHomeworkCompat(@RequestParam("questionId") Long questionId,
                                                      HttpServletRequest httpRequest) {
        // 复用 REST 删除接口的校验、权限判断和删除逻辑
        return deleteHomework(questionId, httpRequest);
    }

    private Long resolveCourseIdBySectionId(Long sectionId) {
        Section section = sectionService.getById(sectionId);
        if (section == null || section.getChapterId() == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "section not found");
        }
        CourseChapter chapter = courseChapterMapper.selectById(section.getChapterId());
        if (chapter == null || chapter.getCourseId() == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "chapter not found");
        }
        return chapter.getCourseId();
    }

    /**
     * 教师或管理员批改学生提交的作业。
     *
     * @param gradeRequest 作业批改请求
     * @param request 当前 HTTP 请求
     * @return 是否批改成功
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_OR_TEACHER_ROLES)
    public BaseResponse<Boolean> reviewHomework(
            @Valid @RequestBody HomeworkGradeRequest gradeRequest,
            HttpServletRequest request
    ) {
        // 获取当前批改人，由管理服务校验作业归属并写入分数与评语
        User loginUser = userService.getLoginUser(request);
        courseManageAdminService.reviewHomework(gradeRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 管理员更新课程状态。
     *
     * @param id 课程 ID
     * @param status 目标状态
     * @return 是否更新成功
     */
    @PostMapping("/status/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateCourseStatus(@RequestParam("id") Long id,
                                                    @RequestParam("status") Integer status) {
        // 校验课程 ID 和目标状态，避免构造无效更新对象
        if (id == null || status == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 只设置需要更新的状态字段，避免覆盖课程其他信息
        Course updateEntity = new Course();
        updateEntity.setId(id);
        updateEntity.setStatus(status);
        return ResultUtils.success(courseService.updateById(updateEntity));
    }

    @GetMapping("/recommend")
    public BaseResponse<List<CourseVO>> getRecommendCourses(
            @RequestParam(required = false) Integer limit,
            HttpServletRequest request
    ) {
        User loginUser = userService.getLoginUserPermitNull(request);
        Long userId = loginUser == null ? null : loginUser.getId();
        return ResultUtils.success(courseService.recommendCourses(userId, limit));
    }

    /**
     * 热门课程推荐，按点击人数、学习人数、评分、创建时间排序。
     * GET /api/course/recommend/hot
     */
    @GetMapping("/recommend/hot")
    public BaseResponse<List<CourseVO>> getHotRecommendCourses(
            @RequestParam(required = false, defaultValue = "6") Integer limit
    ) {
        return ResultUtils.success(courseService.listHotRecommendCourses(limit));
    }

    /**
     * 记录登录用户课程点击，游客访问不统计。
     * POST /api/course/{courseId}/view
     */
    @PostMapping("/{courseId}/view")
    public BaseResponse<Boolean> recordCourseView(
            @PathVariable Long courseId,
            HttpServletRequest request
    ) {
        User loginUser = userService.getLoginUserPermitNull(request);
        courseService.recordCourseView(courseId, loginUser == null ? null : loginUser.getId());
        return ResultUtils.success(true);
    }


    //把从数据库查出来的课程数据，转换成前端页面能直接用的格式
    private CourseVO toCourseVO(Course course) {
        return toCourseVO(course, false);
    }

    private CourseVO toCourseVO(Course course, boolean includeAiKnowleage) {
        CourseVO courseVO = new CourseVO();
        courseVO.setId(course.getId());
        courseVO.setTitle(course.getTitle());
        courseVO.setSubtitle(course.getSubtitle());
        courseVO.setDescription(course.getDescription());
        courseVO.setCoverImage(StringUtils.isNotBlank(course.getCoverUrl()) ? course.getCoverUrl() : course.getCoverImage());
        courseVO.setPrice(course.getPrice());
        courseVO.setOriginalPrice(course.getOriginalPrice());
        courseVO.setCourseType(course.getCourseType());
        courseVO.setDifficulty(course.getDifficulty());
        courseVO.setTeacherId(course.getTeacherId());
        courseVO.setStatus(course.getStatus());
        courseVO.setRatingScore(course.getRatingScore());
        courseVO.setRatingCount(course.getRatingCount());
        courseVO.setBuyCount(course.getBuyCount());
        courseVO.setStudyCount(course.getStudentCount());
        courseVO.setViewCount(course.getViewCount());
        courseVO.setTotalDuration(course.getTotalDuration());
        courseVO.setTotalChapters(course.getTotalChapters());
        courseVO.setTotalSections(course.getTotalSections());
        courseVO.setTags(course.getTags());
        courseVO.setRequirements(course.getRequirements());
        courseVO.setObjectives(course.getObjectives());
        courseVO.setTargetAudience(course.getTargetAudience());
        if (includeAiKnowleage) {
            courseVO.setAiKnowleage(course.getAiKnowleage());
        }
        courseVO.setCreateTime(course.getCreateTime());
        courseVO.setUpdateTime(course.getUpdateTime());

        Long categoryId = course.getCategoryId();
        if (categoryId == null && StringUtils.isNumeric(course.getCategory())) {
            categoryId = Long.valueOf(course.getCategory());
        }
        courseVO.setCategoryId(categoryId);

        if (course.getTeacherId() != null) {
            User teacher = userService.getById(course.getTeacherId());
            if (teacher != null) {
                courseVO.setTeacherName(teacher.getUserName());
                courseVO.setTeacherAvatar(teacher.getUserAvatar());
                courseVO.setTeacherVO(userService.getUserVO(teacher));
            }
        }

        return courseVO;
    }


    // 判断当前登录用户是否是课程的作者，或者管理员
    private boolean shouldIncludeAiKnowleage(User loginUser, Course course) {
        if (loginUser == null || course == null) {
            return false;
        }
        if (userService.isAdmin(loginUser)) {
            return true;
        }
        return UserConstant.TEACHER_ROLE.equals(loginUser.getUserRole())
                && loginUser.getId() != null
                && loginUser.getId().equals(course.getTeacherId());
    }


    // 排序
    private void applySort(LambdaQueryWrapper<Course> queryWrapper, String sortField, String sortOrder) {
        boolean isAsc = "ascend".equalsIgnoreCase(sortOrder) || "asc".equalsIgnoreCase(sortOrder);
        if (StringUtils.isBlank(sortField)) {
            queryWrapper.orderByDesc(Course::getCreateTime);
            return;
        }
        switch (sortField) {
            case "id":
                if (isAsc) {
                    queryWrapper.orderByAsc(Course::getId);
                } else {
                    queryWrapper.orderByDesc(Course::getId);
                }
                return;
            case "title":
                if (isAsc) {
                    queryWrapper.orderByAsc(Course::getTitle);
                } else {
                    queryWrapper.orderByDesc(Course::getTitle);
                }
                return;
            case "price":
                if (isAsc) {
                    queryWrapper.orderByAsc(Course::getPrice);
                } else {
                    queryWrapper.orderByDesc(Course::getPrice);
                }
                return;
            case "originalPrice":
                if (isAsc) {
                    queryWrapper.orderByAsc(Course::getOriginalPrice);
                } else {
                    queryWrapper.orderByDesc(Course::getOriginalPrice);
                }
                return;
            case "courseType":
                if (isAsc) {
                    queryWrapper.orderByAsc(Course::getCourseType);
                } else {
                    queryWrapper.orderByDesc(Course::getCourseType);
                }
                return;
            case "difficulty":
                if (isAsc) {
                    queryWrapper.orderByAsc(Course::getDifficulty);
                } else {
                    queryWrapper.orderByDesc(Course::getDifficulty);
                }
                return;
            case "status":
                if (isAsc) {
                    queryWrapper.orderByAsc(Course::getStatus);
                } else {
                    queryWrapper.orderByDesc(Course::getStatus);
                }
                return;
            case "rating":
            case "ratingScore":
                if (isAsc) {
                    queryWrapper.orderByAsc(Course::getRatingScore);
                } else {
                    queryWrapper.orderByDesc(Course::getRatingScore);
                }
                return;
            case "ratingCount":
                if (isAsc) {
                    queryWrapper.orderByAsc(Course::getRatingCount);
                } else {
                    queryWrapper.orderByDesc(Course::getRatingCount);
                }
                return;
            case "hot":
            case "studentCount":
                if (isAsc) {
                    queryWrapper.orderByAsc(Course::getStudentCount);
                } else {
                    queryWrapper.orderByDesc(Course::getStudentCount);
                }
                return;
            case "latest":
            case "newest":
            case "new":
            case "create_time":
                if (isAsc) {
                    queryWrapper.orderByAsc(Course::getCreateTime);
                } else {
                    queryWrapper.orderByDesc(Course::getCreateTime);
                }
                return;
            case "update_time":
                if (isAsc) {
                    queryWrapper.orderByAsc(Course::getUpdateTime);
                } else {
                    queryWrapper.orderByDesc(Course::getUpdateTime);
                }
                return;
            default:
                queryWrapper.orderByDesc(Course::getCreateTime);
        }
    }


    // 处理请求中的用户ID
    private Long resolveRequestedUserId(HttpServletRequest request, Long requestedUserId) {
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser == null) {
            return null;
        }
        if (UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            return requestedUserId != null ? requestedUserId : loginUser.getId();
        }
        return loginUser.getId();
    }
}




