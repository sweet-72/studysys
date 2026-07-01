package com.ttbt.smartclass.model.vo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import lombok.Data;

@Data
public class DailyGoalItemVO implements Serializable {

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

    private static final long serialVersionUID = 1L;
}
