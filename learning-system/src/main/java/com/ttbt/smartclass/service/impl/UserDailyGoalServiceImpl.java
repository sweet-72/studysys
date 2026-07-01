package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.exception.ThrowUtils;
import com.ttbt.smartclass.mapper.UserDailyGoalMapper;
import com.ttbt.smartclass.model.dto.dailygoal.DailyGoalItemAddRequest;
import com.ttbt.smartclass.model.entity.UserDailyGoal;
import com.ttbt.smartclass.model.entity.UserDailyGoalItem;
import com.ttbt.smartclass.model.vo.DailyGoalItemVO;
import com.ttbt.smartclass.model.vo.DailyGoalTodayVO;
import com.ttbt.smartclass.service.UserDailyGoalItemService;
import com.ttbt.smartclass.service.UserDailyGoalService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDailyGoalServiceImpl extends ServiceImpl<UserDailyGoalMapper, UserDailyGoal>
        implements UserDailyGoalService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_COMPLETED = 1;
    private static final String SOURCE_SYSTEM = "SYSTEM";
    private static final String SOURCE_CARRY_OVER = "CARRY_OVER";
    private static final String SOURCE_CUSTOM = "CUSTOM";
    private static final String TYPE_CUSTOM = "CUSTOM";

    @Resource
    private UserDailyGoalItemService userDailyGoalItemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyGoalTodayVO getOrCreateTodayGoal(long userId) {
        LocalDate today = LocalDate.now();
        UserDailyGoal todayGoal = getByUserAndDate(userId, today);
        if (todayGoal == null) {
            todayGoal = createTodayGoal(userId, today);
        }
        refreshSummary(todayGoal.getId());
        return buildTodayVO(todayGoal.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyGoalTodayVO updateItemProgress(long userId, long itemId, int currentValue) {
        ThrowUtils.throwIf(currentValue < 0, ErrorCode.PARAMS_ERROR, "currentValue must be greater than or equal to 0");
        UserDailyGoalItem item = getOwnedItem(userId, itemId);
        Date now = new Date();
        item.setCurrentValue(currentValue);
        if (currentValue >= defaultOne(item.getTargetValue())) {
            item.setStatus(STATUS_COMPLETED);
            if (item.getCompletedTime() == null) {
                item.setCompletedTime(now);
            }
        } else {
            item.setStatus(STATUS_PENDING);
            item.setCompletedTime(null);
        }
        item.setUpdateTime(now);
        userDailyGoalItemService.updateById(item);
        refreshSummary(item.getGoalId());
        return buildTodayVO(item.getGoalId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyGoalTodayVO completeItem(long userId, long itemId) {
        UserDailyGoalItem item = getOwnedItem(userId, itemId);
        Date now = new Date();
        item.setCurrentValue(defaultOne(item.getTargetValue()));
        item.setStatus(STATUS_COMPLETED);
        item.setCompletedTime(now);
        item.setUpdateTime(now);
        userDailyGoalItemService.updateById(item);
        refreshSummary(item.getGoalId());
        return buildTodayVO(item.getGoalId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyGoalTodayVO cancelCompleteItem(long userId, long itemId) {
        UserDailyGoalItem item = getOwnedItem(userId, itemId);
        item.setStatus(STATUS_PENDING);
        item.setCompletedTime(null);
        item.setUpdateTime(new Date());
        userDailyGoalItemService.updateById(item);
        refreshSummary(item.getGoalId());
        return buildTodayVO(item.getGoalId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyGoalTodayVO addCustomItem(long userId, DailyGoalItemAddRequest request) {
        if (request == null || StringUtils.isBlank(request.getTitle())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "title is required");
        }
        UserDailyGoal todayGoal = getOrCreateTodayGoalEntity(userId);
        UserDailyGoalItem item = new UserDailyGoalItem();
        item.setGoalId(todayGoal.getId());
        item.setUserId(userId);
        item.setGoalDate(todayGoal.getGoalDate());
        item.setTitle(request.getTitle().trim());
        item.setGoalType(StringUtils.defaultIfBlank(request.getGoalType(), TYPE_CUSTOM));
        item.setTargetValue(defaultOne(request.getTargetValue()));
        item.setCurrentValue(0);
        item.setUnit(StringUtils.trimToNull(request.getUnit()));
        item.setStatus(STATUS_PENDING);
        item.setSource(SOURCE_CUSTOM);
        item.setIsDelete(0);
        Date now = new Date();
        item.setCreateTime(now);
        item.setUpdateTime(now);
        userDailyGoalItemService.save(item);
        refreshSummary(todayGoal.getId());
        return buildTodayVO(todayGoal.getId());
    }

    private UserDailyGoal getOrCreateTodayGoalEntity(long userId) {
        LocalDate today = LocalDate.now();
        UserDailyGoal todayGoal = getByUserAndDate(userId, today);
        if (todayGoal == null) {
            todayGoal = createTodayGoal(userId, today);
        }
        return todayGoal;
    }

    private UserDailyGoal createTodayGoal(long userId, LocalDate today) {
        UserDailyGoal goal = new UserDailyGoal();
        goal.setUserId(userId);
        goal.setGoalDate(today);
        goal.setTotalGoals(0);
        goal.setCompletedGoals(0);
        goal.setProgressPercent(0);
        goal.setIsCompleted(0);
        Date now = new Date();
        goal.setCreateTime(now);
        goal.setUpdateTime(now);
        try {
            this.save(goal);
        } catch (DuplicateKeyException e) {
            UserDailyGoal existed = getByUserAndDate(userId, today);
            if (existed == null) {
                throw e;
            }
            return existed;
        }

        List<UserDailyGoalItem> items = buildCarryOverItems(userId, today.minusDays(1), goal);
        if (!items.isEmpty()) {
            userDailyGoalItemService.saveBatch(items);
        }
        return goal;
    }

    private List<UserDailyGoalItem> buildCarryOverItems(long userId, LocalDate yesterday, UserDailyGoal todayGoal) {
        List<UserDailyGoalItem> yesterdayItems = userDailyGoalItemService.list(new QueryWrapper<UserDailyGoalItem>()
                .eq("user_id", userId)
                .eq("goal_date", yesterday)
                .eq("status", STATUS_PENDING)
                .eq("is_delete", 0)
                .orderByAsc("id"));
        Date now = new Date();
        return yesterdayItems.stream().map(oldItem -> {
            UserDailyGoalItem item = new UserDailyGoalItem();
            BeanUtils.copyProperties(oldItem, item);
            item.setId(null);
            item.setGoalId(todayGoal.getId());
            item.setUserId(userId);
            item.setGoalDate(todayGoal.getGoalDate());
            item.setStatus(STATUS_PENDING);
            item.setSource(SOURCE_CARRY_OVER);
            item.setCarryFromItemId(oldItem.getId());
            item.setCompletedTime(null);
            item.setCreateTime(now);
            item.setUpdateTime(now);
            item.setIsDelete(0);
            return item;
        }).collect(Collectors.toList());
    }

    private UserDailyGoal getByUserAndDate(long userId, LocalDate goalDate) {
        return this.getOne(new QueryWrapper<UserDailyGoal>()
                .eq("user_id", userId)
                .eq("goal_date", goalDate)
                .last("limit 1"), false);
    }

    private UserDailyGoalItem getOwnedItem(long userId, long itemId) {
        ThrowUtils.throwIf(itemId <= 0, ErrorCode.PARAMS_ERROR);
        UserDailyGoalItem item = userDailyGoalItemService.getOne(new QueryWrapper<UserDailyGoalItem>()
                .eq("id", itemId)
                .eq("user_id", userId)
                .eq("is_delete", 0)
                .last("limit 1"), false);
        if (item == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "daily goal item not found");
        }
        return item;
    }

    private void refreshSummary(long goalId) {
        UserDailyGoal goal = this.getById(goalId);
        if (goal == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "daily goal not found");
        }
        List<UserDailyGoalItem> items = listItems(goalId);
        int total = items.size();
        int completed = (int) items.stream()
                .filter(item -> Integer.valueOf(STATUS_COMPLETED).equals(item.getStatus()))
                .count();
        int percent = total == 0 ? 0 : Math.round(completed * 100F / total);
        boolean allCompleted = total > 0 && completed == total;

        goal.setTotalGoals(total);
        goal.setCompletedGoals(completed);
        goal.setProgressPercent(percent);
        goal.setIsCompleted(allCompleted ? 1 : 0);
        goal.setCompletedTime(allCompleted
                ? (goal.getCompletedTime() == null ? new Date() : goal.getCompletedTime())
                : null);
        goal.setUpdateTime(new Date());
        this.updateById(goal);
    }

    private DailyGoalTodayVO buildTodayVO(long goalId) {
        UserDailyGoal goal = this.getById(goalId);
        DailyGoalTodayVO vo = new DailyGoalTodayVO();
        BeanUtils.copyProperties(goal, vo);
        vo.setItems(listItems(goalId).stream().map(this::toItemVO).collect(Collectors.toList()));
        return vo;
    }

    private List<UserDailyGoalItem> listItems(long goalId) {
        return userDailyGoalItemService.list(new QueryWrapper<UserDailyGoalItem>()
                .eq("goal_id", goalId)
                .eq("is_delete", 0)
                .orderByAsc("status")
                .orderByAsc("id"));
    }

    private DailyGoalItemVO toItemVO(UserDailyGoalItem item) {
        DailyGoalItemVO vo = new DailyGoalItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }

    private int defaultOne(Integer value) {
        return value == null || value <= 0 ? 1 : value;
    }
}
