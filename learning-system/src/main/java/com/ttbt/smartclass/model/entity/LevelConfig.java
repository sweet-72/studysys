package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 等级配置实体类
 */
@TableName(value = "level_config")
@Data
public class LevelConfig implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 升级所需经验值
     */
    @TableField("required_exp")
    private Integer requiredExp;

    /**
     * 特权描述
     */
    @TableField("privilege_desc")
    private String privilegeDesc;

    /**
     * 等级图标 URL
     */
    @TableField("icon_url")
    private String iconUrl;

    /**
     * 等级名称
     */
    @TableField("level_name")
    private String levelName;

    /**
     * 排序权重
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 是否启用
     */
    @TableField("is_enabled")
    private Integer isEnabled;

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

    /**
     * 是否删除
     */
    @TableLogic
    @TableField("is_delete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
