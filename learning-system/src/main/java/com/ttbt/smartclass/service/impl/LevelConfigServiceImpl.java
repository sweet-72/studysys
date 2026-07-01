package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ttbt.smartclass.model.entity.LevelConfig;
import com.ttbt.smartclass.mapper.LevelConfigMapper;
import com.ttbt.smartclass.service.LevelConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 等级配置服务实现类
 */
@Service
public class LevelConfigServiceImpl extends ServiceImpl<LevelConfigMapper, LevelConfig> 
        implements LevelConfigService {

    @Resource
    private LevelConfigMapper levelConfigMapper;

    @Override
    public LevelConfig getByLevel(Integer level) {
        LambdaQueryWrapper<LevelConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LevelConfig::getLevel, level)
                .eq(LevelConfig::getIsEnabled, 1);
        return levelConfigMapper.selectOne(queryWrapper);
    }

    @Override
    public List<LevelConfig> getEnabledConfigs() {
        return levelConfigMapper.selectEnabledConfigs();
    }
}
