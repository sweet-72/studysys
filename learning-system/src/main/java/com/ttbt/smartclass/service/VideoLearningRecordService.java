package com.ttbt.smartclass.service;

import com.ttbt.smartclass.model.dto.VideoProgressSaveRequest;
import com.ttbt.smartclass.model.entity.VideoLearningRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 视频学习记录服务接口
 */
public interface VideoLearningRecordService extends IService<VideoLearningRecord> {

    /**
     * 保存视频学习进度
     *
     * @param request 进度保存请求
     * @param userId 用户 ID
     */
    void saveVideoProgress(VideoProgressSaveRequest request, Long userId);

    /**
     * 获取视频学习进度
     *
     * @param sectionId 小节 ID
     * @param userId 用户 ID
     * @return 学习记录
     */
    VideoLearningRecord getVideoProgress(Long sectionId, Long userId);
}
