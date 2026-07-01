package com.ttbt.smartclass.service.impl;

import com.ttbt.smartclass.model.entity.UserExpRecord;
import com.ttbt.smartclass.mapper.UserExpRecordMapper;
import com.ttbt.smartclass.service.UserExpRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 经验记录服务实现类
 */
@Service
public class UserExpRecordServiceImpl extends ServiceImpl<UserExpRecordMapper, UserExpRecord> 
        implements UserExpRecordService {
}
