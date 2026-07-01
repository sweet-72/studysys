package com.ttbt.smartclass.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 保存视频学习进度请求 DTO
 */
@Data
public class VideoProgressSaveRequest {

    /**
     * 小节 ID
     */
    @NotNull(message = "小节 ID 不能为空")
    private Long sectionId;

    /**
     * 已学习时长（秒）
     */
    @NotNull(message = "学习时长不能为空")
    private Integer learnedTime;

    /**
     * 最后观看位置（秒）
     */
    @NotNull(message = "观看位置不能为空")
    private Integer lastWatchPosition;

    /**
     * 是否完成：0-否，1-是
     */
    private Integer isCompleted = 0;
}
