package com.ttbt.smartclass.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ttbt.smartclass.model.dto.dailygoal.DailyGoalItemAddRequest;
import com.ttbt.smartclass.model.entity.UserDailyGoal;
import com.ttbt.smartclass.model.vo.DailyGoalTodayVO;

public interface UserDailyGoalService extends IService<UserDailyGoal> {

    DailyGoalTodayVO getOrCreateTodayGoal(long userId);

    DailyGoalTodayVO updateItemProgress(long userId, long itemId, int currentValue);

    DailyGoalTodayVO completeItem(long userId, long itemId);

    DailyGoalTodayVO cancelCompleteItem(long userId, long itemId);

    DailyGoalTodayVO addCustomItem(long userId, DailyGoalItemAddRequest request);
}
