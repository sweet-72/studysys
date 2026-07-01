package com.ttbt.smartclass.model.dto.dailygoal;

import java.io.Serializable;
import lombok.Data;

@Data
public class DailyGoalItemProgressUpdateRequest implements Serializable {

    private Long itemId;

    private Integer currentValue;

    private static final long serialVersionUID = 1L;
}
