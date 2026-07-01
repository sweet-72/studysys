package com.ttbt.smartclass.mapper;

import com.ttbt.smartclass.model.entity.LevelConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 等级配置 Mapper
 */
@Mapper
public interface LevelConfigMapper extends BaseMapper<LevelConfig> {

    /**
     * 查询所有启用的等级配置（按等级升序）
     */
    @Select("SELECT * FROM level_config WHERE is_enabled = 1 ORDER BY level ASC")
    List<LevelConfig> selectEnabledConfigs();
}
