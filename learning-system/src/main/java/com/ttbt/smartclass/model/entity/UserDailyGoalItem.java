package com.ttbt.smartclass.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import lombok.Data;

/**
 * User daily learning goal item.
 */
@TableName(value = "user_daily_goal_item")
@Data
public class UserDailyGoalItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long goalId;

    private Long userId;

    private LocalDate goalDate;

    private String title;

    private String goalType;

    private Integer targetValue;

    private Integer currentValue;

    private String unit;

    private Integer status;

    private String source;

    private Long carryFromItemId;

    private Date completedTime;

    private Date createTime;

    private Date updateTime;

    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
