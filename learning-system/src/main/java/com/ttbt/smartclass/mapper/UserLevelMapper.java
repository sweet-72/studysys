package com.ttbt.smartclass.mapper;

import com.ttbt.smartclass.model.entity.UserLevel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户等级 Mapper
 */
@Mapper
public interface UserLevelMapper extends BaseMapper<UserLevel> {

    /**
     * 增加经验值（原子操作）
     */
    @Update("UPDATE user_level SET exp = exp + #{exp}, total_exp = total_exp + #{exp}, update_time = NOW() WHERE user_id = #{user_id}")
    int addExp(@Param("user_id") Long userId, @Param("exp") Integer exp);

    /**
     * 更新等级和下一级经验
     */
    @Update("UPDATE user_level SET level = #{level}, next_level_exp = #{next_level_exp}, level_up_time = NOW(), update_time = NOW() WHERE user_id = #{user_id}")
    int updateLevel(@Param("user_id") Long userId, @Param("level") Integer level, @Param("next_level_exp") Integer nextLevelExp);
}
