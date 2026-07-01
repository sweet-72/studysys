package com.ttbt.smartclass.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 增加经验值请求 DTO
 */
@Data
public class ExpAddRequest {

    /**
     * 用户 ID
     */
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    /**
     * 行为类型（枚举编码）
     */
    @NotBlank(message = "行为类型不能为空")
    private String actionType;

    /**
     * 关联业务 ID（可选）
     */
    private Long relatedId;

    /**
     * 关联类型（可选）
     */
    private String relatedType;

    /**
     * 描述（可选，为空则使用默认描述）
     */
    private String description;

    /**
     * IP 地址（可选）
     */
    private String ipAddress;

    /**
     * 设备信息（可选）
     */
    private String deviceInfo;
}
