package com.ttbt.smartclass.model.dto.dailygoal;

import java.io.Serializable;
import lombok.Data;

@Data
public class DailyGoalItemCompleteRequest implements Serializable {

    private Long itemId;

    private static final long serialVersionUID = 1L;
}
