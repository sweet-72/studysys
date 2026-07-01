package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 视频学习记录实体类
 */
@TableName(value = "video_learning_record")
@Data
public class VideoLearningRecord implements Serializable {

    /**
     * 记录 id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 小节 id
     */
    @TableField("section_id")
    private Long sectionId;

    /**
     * 视频总时长（秒）
     */
    @TableField("video_duration")
    private Integer videoDuration;

    /**
     * 已学习时长（秒）
     */
    @TableField("learned_time")
    private Integer learnedTime;

    /**
     * 学习进度百分比
     */
    private BigDecimal progress;

    /**
     * 最后观看位置（秒）
     */
    @TableField("last_watch_position")
    private Integer lastWatchPosition;

    /**
     * 是否完成：0-否，1-是
     */
    @TableField("is_completed")
    private Integer isCompleted;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
