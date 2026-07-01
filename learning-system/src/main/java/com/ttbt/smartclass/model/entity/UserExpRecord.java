package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 经验记录实体类
 */
@TableName(value = "user_exp_record")
@Data
public class UserExpRecord implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 行为类型
     */
    @TableField("action_type")
    private String actionType;

    /**
     * 经验变化值
     */
    @TableField("exp_change")
    private Integer expChange;

    /**
     * 描述
     */
    private String description;

    /**
     * 关联业务 ID
     */
    @TableField("related_id")
    private Long relatedId;

    /**
     * 关联类型
     */
    @TableField("related_type")
    private String relatedType;

    /**
     * 操作 IP 地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 设备信息
     */
    @TableField("device_info")
    private String deviceInfo;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
