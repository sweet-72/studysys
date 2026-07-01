package com.ttbt.smartclass.service;

import com.ttbt.smartclass.model.entity.UserProgress;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户学习进度服务接口
 */
public interface UserProgressService extends IService<UserProgress> {

    /**
     * 更新学习状态
     * 
     * @param userId 用户 ID
     * @param courseId 课程 ID
     * @param sectionId 小节 ID
     */
    void updateLearningStatus(Long userId, Long courseId, Long sectionId);
}
