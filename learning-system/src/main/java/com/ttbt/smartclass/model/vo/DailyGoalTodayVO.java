package com.ttbt.smartclass.model.vo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class DailyGoalTodayVO implements Serializable {

    private Long id;

    private Long userId;

    private LocalDate goalDate;

    private Integer totalGoals;

    private Integer completedGoals;

    private Integer progressPercent;

    private Integer isCompleted;

    private Date completedTime;

    private List<DailyGoalItemVO> items;

    private static final long serialVersionUID = 1L;
}
