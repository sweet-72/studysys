package com.ttbt.smartclass.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 经验记录 VO
 */
@Data
public class UserExpRecordVO {

    /**
     * 记录 ID
     */
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 行为类型编码
     */
    private String actionType;

    /**
     * 行为类型描述
     */
    private String actionDescription;

    /**
     * 经验变化值
     */
    private Integer expChange;

    /**
     * 描述
     */
    private String description;

    /**
     * 关联业务 ID
     */
    private Long relatedId;

    /**
     * 关联类型
     */
    private String relatedType;

    /**
     * 创建时间
     */
    private Date createTime;
}
