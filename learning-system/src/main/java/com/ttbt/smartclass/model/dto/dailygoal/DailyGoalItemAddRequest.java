package com.ttbt.smartclass.model.dto.dailygoal;

import java.io.Serializable;
import lombok.Data;

@Data
public class DailyGoalItemAddRequest implements Serializable {

    private String title;

    private String goalType;

    private Integer targetValue;

    private String unit;

    private static final long serialVersionUID = 1L;
}
