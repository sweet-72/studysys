package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 好友关系实体。
 */
@TableName(value = "friend_relationship")
@Data
public class FriendRelationship implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id1")
    private Long userId1;

    @TableField("user_id2")
    private Long userId2;

    private String status;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
