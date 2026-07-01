package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户实体。
 */
@TableName("user")
@Data
public class User implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_account")
    private String userAccount;

    @TableField("user_password")
    private String userPassword;

    @TableField("user_gender")
    private Integer userGender;

    @TableField("phone")
    private String userPhone;

    @TableField("union_id")
    private String unionId;

    @TableField("mp_open_id")
    private String mpOpenId;

    @TableField("user_name")
    private String userName;

    @TableField("user_avatar")
    private String userAvatar;

    @TableField("user_profile")
    private String userProfile;

    @TableField("user_role")
    private String userRole;

    @TableField("email")
    private String userEmail;

    @TableField("wechat_id")
    private String wechatId;

    private String province;

    private String city;

    private String district;

    private Date birthday;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableLogic
    @TableField("is_delete")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
