package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ttbt.smartclass.mapper.UserProgressMapper;
import com.ttbt.smartclass.model.entity.UserProgress;
import com.ttbt.smartclass.service.UserProgressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 用户学习进度服务实现类
 */
@Slf4j
@Service
public class UserProgressServiceImpl extends ServiceImpl<UserProgressMapper, UserProgress> implements UserProgressService {

    @Resource
    private UserProgressMapper userProgressMapper;

    @Override
    public void updateLearningStatus(Long userId, Long courseId, Long sectionId) {
        // 查询进度记录
        LambdaQueryWrapper<UserProgress> query = new LambdaQueryWrapper<>();
        query.eq(UserProgress::getUserId, userId)
                .eq(UserProgress::getSectionId, sectionId);
        UserProgress progress = userProgressMapper.selectOne(query);

        if (progress == null) {
            // 创建新记录
            progress = new UserProgress();
            progress.setUserId(userId);
            progress.setCourseId(courseId);
            progress.setSectionId(sectionId);
            progress.setStatus(1); // 学习中
            progress.setLearnedTime(0);
            progress.setLastLearnTime(new Date());
            userProgressMapper.insert(progress);
        } else {
            // 更新状态
            if (progress.getStatus() == 0) { // 从未学习变为学习中
                progress.setStatus(1);
                progress.setLastLearnTime(new Date());
                userProgressMapper.updateById(progress);
            }
        }

        log.info("用户 {} 开始学习小节 {}", userId, sectionId);
    }
}
