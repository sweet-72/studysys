package com.ttbt.smartclass.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.constant.UserConstant;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.model.dto.SubmitAnswerRequest;
import com.ttbt.smartclass.model.dto.SubmitAnswerResponse;
import com.ttbt.smartclass.model.dto.VideoProgressSaveRequest;
import com.ttbt.smartclass.model.entity.Question;
import com.ttbt.smartclass.model.entity.Section;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.entity.VideoLearningRecord;
import com.ttbt.smartclass.model.vo.CourseCatalogVO;
import com.ttbt.smartclass.model.vo.SectionVO;
import com.ttbt.smartclass.service.CourseService;
import com.ttbt.smartclass.service.SectionService;
import com.ttbt.smartclass.service.UserService;
import com.ttbt.smartclass.service.VideoLearningRecordService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户端学习接口兼容层。
 * 保留 /section、/question、/learning/record 历史路径，兼容现有前端调用。
 */
@RestController
public class CourseLearningCompatibilityController {

    @Resource
    private CourseController courseController;

    @Resource
    private CourseService courseService;

    @Resource
    private SectionService sectionService;

    @Resource
    private UserService userService;

    @Resource
    private VideoLearningRecordService videoLearningRecordService;

    /**
     * 查询课程小节内容，兼容旧版小节内容路径。
     *
     * @param sectionId 小节 ID
     * @param userId 用户 ID，可选
     * @param request 当前 HTTP 请求
     * @return 小节内容信息
     */
    @GetMapping("/section/{sectionId}")
    public BaseResponse<CourseCatalogVO.SectionInfo> getSectionContent(@PathVariable Long sectionId,
                                                                       @RequestParam(required = false) Long userId,
                                                                       HttpServletRequest request) {
        // 复用课程控制器的小节内容查询逻辑，保持兼容路径和新版接口返回一致
        return courseController.getSectionContent(sectionId, userId, request);
    }

    /**
     * 查询课程小节详情，兼容旧版小节详情路径。
     *
     * @param sectionId 小节 ID
     * @return 小节详情信息
     */
    @GetMapping("/section/{sectionId}/detail")
    public BaseResponse<SectionVO> getSectionDetail(@PathVariable Long sectionId) {
        // 直接复用小节服务查询详情，保持历史路径返回结构与新版接口一致
        return ResultUtils.success(sectionService.getSectionDetail(sectionId));
    }

    @GetMapping("/section/{sectionId}/questions")
    public BaseResponse<List<Question>> getSectionQuestions(@PathVariable Long sectionId) {
        return ResultUtils.success(courseService.getSectionQuestions(sectionId));
    }

    /**
     * 提交小节题目答案，兼容旧版答题路径。
     *
     * @param request 答题提交请求
     * @param httpRequest 当前 HTTP 请求
     * @return 答题结果
     */
    @PostMapping("/question/submit")
    public BaseResponse<SubmitAnswerResponse> submitAnswer(@RequestBody SubmitAnswerRequest request,
                                                           HttpServletRequest httpRequest) {
        // 以当前登录用户为准写入提交用户，避免前端传入他人用户 ID
        User loginUser = userService.getLoginUser(httpRequest);
        request.setUserId(loginUser.getId());
        return ResultUtils.success(courseService.submitAnswer(request));
    }

    /**
     * 兼容用户端：GET /api/learning/record?userId=&courseId=
     *
     * @param userId 用户 ID，管理员可指定
     * @param courseId 课程 ID
     * @param request 当前 HTTP 请求
     * @return 旧版前端需要的学习记录数据
     */
    @GetMapping("/learning/record")
    public BaseResponse<Map<String, Object>> getLearningRecord(@RequestParam(required = false) Long userId,
                                                                @RequestParam(required = false) Long courseId,
                                                                HttpServletRequest request) {
        // 兼容旧前端未登录访问，未登录时返回空记录而不是抛出异常
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser == null) {
            return ResultUtils.success(null);
        }
        // 管理员可以按参数查询指定用户，普通用户只能查询自己的学习记录
        Long resolvedUserId = resolveUserId(loginUser, userId);
        if (courseId == null) {
            return ResultUtils.success(null);
        }

        // 先查询课程目录，从章节中提取课程下全部小节 ID
        CourseCatalogVO catalog = courseService.getCourseCatalog(courseId, resolvedUserId);
        List<CourseCatalogVO.ChapterInfo> chapterList = catalog.getChapters() == null ? List.of() : catalog.getChapters();
        List<Long> sectionIds = chapterList.stream()
                .flatMap(chapter -> (chapter.getSections() == null ? List.<CourseCatalogVO.SectionInfo>of() : chapter.getSections()).stream())
                .map(CourseCatalogVO.SectionInfo::getSectionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (sectionIds.isEmpty()) {
            return ResultUtils.success(null);
        }

        // 查询这些小节对应的视频学习记录，按更新时间倒序取最近一次学习进度
        List<VideoLearningRecord> records = videoLearningRecordService.list(new LambdaQueryWrapper<VideoLearningRecord>()
                .eq(VideoLearningRecord::getUserId, resolvedUserId)
                .in(VideoLearningRecord::getSectionId, sectionIds)
                .orderByDesc(VideoLearningRecord::getUpdateTime)
                .orderByDesc(VideoLearningRecord::getId));
        if (records == null || records.isEmpty()) {
            return ResultUtils.success(null);
        }

        VideoLearningRecord latest = records.get(0);
        int progress = toIntPercent(latest.getProgress());

        // 组装旧版前端需要的 Map 结构，保留 progress、videoProgress 和历史小节列表字段
        Map<String, Object> result = new HashMap<>();
        result.put("user_id", resolvedUserId);
        result.put("course_id", courseId);
        result.put("sectionId", latest.getSectionId());
        result.put("progress", progress);
        result.put("videoProgress", progress);
        result.put("wrongCount", 0);
        result.put("historySectionIds", records.stream()
                .map(VideoLearningRecord::getSectionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList()));
        result.put("updatedAt", latest.getUpdateTime());
        return ResultUtils.success(result);
    }

    /**
     * 兼容用户端：POST /api/learning/record
     *
     * @param request 学习记录保存请求
     * @param httpRequest 当前 HTTP 请求
     * @return 是否保存成功
     */
    @PostMapping("/learning/record")
    public BaseResponse<Boolean> saveLearningRecord(@RequestBody LearningRecordRequest request,
                                                    HttpServletRequest httpRequest) {
        // 校验旧版学习记录必须包含小节 ID，否则无法换算视频进度
        if (request == null || request.getSectionId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "sectionId is required");
        }

        // 获取当前登录用户，并按管理员/普通用户规则解析最终写入的用户 ID
        User loginUser = userService.getLoginUser(httpRequest);
        Long resolvedUserId = resolveUserId(loginUser, request.getUserId());

        // 查询小节信息，用小节视频时长将百分比进度换算为播放位置
        Section section = sectionService.getById(request.getSectionId());
        if (section == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "section not /api/course/chapter/listfound");
        }

        // 兼容 videoProgress 和 progress 两个旧字段，并将进度限制在 0 到 100
        int percent = normalizePercent(request.getVideoProgress() != null ? request.getVideoProgress() : request.getProgress());
        int duration = section.getVideoDuration() == null ? 0 : Math.max(section.getVideoDuration(), 0);
        int position = duration > 0 ? (int) Math.round(duration * (percent / 100.0D)) : percent;

        // 转换为当前视频进度保存请求，复用统一的视频学习记录服务
        VideoProgressSaveRequest saveRequest = new VideoProgressSaveRequest();
        saveRequest.setSectionId(request.getSectionId());
        saveRequest.setLearnedTime(position);
        saveRequest.setLastWatchPosition(position);
        saveRequest.setIsCompleted(percent >= 100 ? 1 : 0);

        videoLearningRecordService.saveVideoProgress(saveRequest, resolvedUserId);
        return ResultUtils.success(true);
    }

    private Long resolveUserId(User loginUser, Long requestedUserId) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "please login first");
        }
        if (UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()) && requestedUserId != null) {
            return requestedUserId;
        }
        return loginUser.getId();
    }

    private int toIntPercent(BigDecimal value) {
        if (value == null) {
            return 0;
        }
        int percent = value.intValue();
        return normalizePercent(percent);
    }

    private int normalizePercent(Integer value) {
        if (value == null) {
            return 0;
        }
        if (value < 0) {
            return 0;
        }
        return Math.min(value, 100);
    }

    @Data
    private static class LearningRecordRequest {
        private Long userId;
        private Long courseId;
        private Long chapterId;
        private Long sectionId;
        private Integer progress;
        private Integer videoProgress;
        private List<Long> historySectionIds;
        private Integer wrongCount;
        private String updatedAt;
    }
}


