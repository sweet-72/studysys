package com.ttbt.smartclass.service;

import com.ttbt.smartclass.model.entity.LevelConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 等级配置服务接口
 */
public interface LevelConfigService extends IService<LevelConfig> {

    /**
     * 根据等级获取配置
     * 
     * @param level 等级
     * @return 等级配置
     */
    LevelConfig getByLevel(Integer level);

    /**
     * 获取所有启用的等级配置
     * 
     * @return 等级配置列表
     */
    List<LevelConfig> getEnabledConfigs();
}
