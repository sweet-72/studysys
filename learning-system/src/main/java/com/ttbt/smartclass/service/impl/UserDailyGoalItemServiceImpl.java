package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.mapper.UserDailyGoalItemMapper;
import com.ttbt.smartclass.model.entity.UserDailyGoalItem;
import com.ttbt.smartclass.service.UserDailyGoalItemService;
import org.springframework.stereotype.Service;

@Service
public class UserDailyGoalItemServiceImpl extends ServiceImpl<UserDailyGoalItemMapper, UserDailyGoalItem>
        implements UserDailyGoalItemService {
}
