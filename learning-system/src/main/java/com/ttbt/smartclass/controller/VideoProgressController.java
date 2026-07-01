package com.ttbt.smartclass.controller;

import com.ttbt.smartclass.common.BaseResponse;
import com.ttbt.smartclass.common.ResultUtils;
import com.ttbt.smartclass.model.dto.VideoProgressSaveRequest;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.entity.VideoLearningRecord;
import com.ttbt.smartclass.service.UserService;
import com.ttbt.smartclass.service.VideoLearningRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 视频学习记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/video/progress")
public class VideoProgressController {

    @Resource
    private VideoLearningRecordService videoLearningRecordService;

    @Resource
    private UserService userService;

    /**
     * 保存当前用户的视频学习进度。
     *
     * @param request 视频进度保存请求
     * @param httpRequest 当前 HTTP 请求
     * @return 是否保存成功
     */
    @PostMapping("/save")
    public BaseResponse<Boolean> saveVideoProgress(@RequestBody VideoProgressSaveRequest request,
                                                   HttpServletRequest httpRequest) {
        // 以当前登录用户为准记录视频进度，避免写入其他用户学习记录
        User loginUser = userService.getLoginUser(httpRequest);
        videoLearningRecordService.saveVideoProgress(request, loginUser.getId());
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<VideoLearningRecord> getVideoProgress(@RequestParam Long sectionId,
                                                              HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        VideoLearningRecord record = videoLearningRecordService.getVideoProgress(sectionId, loginUser.getId());
        return ResultUtils.success(record);
    }
}
