package com.ttbt.smartclass.service;

import com.ttbt.smartclass.model.dto.ExpAddRequest;
import com.ttbt.smartclass.model.entity.UserLevel;
import com.ttbt.smartclass.model.vo.UserExpRecordVO;
import com.ttbt.smartclass.model.vo.UserLevelVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户等级服务接口
 */
public interface UserLevelService extends IService<UserLevel> {

    /**
     * 增加经验值（核心方法）
     * 
     * @param request 经验值请求
     * @return 实际获得的经验值
     */
    int addExp(ExpAddRequest request);

    /**
     * 获取用户等级信息
     * 
     * @param userId 用户 ID
     * @return 用户等级信息
     */
    UserLevelVO getUserLevel(Long userId);

    /**
     * 查询经验记录列表
     * 
     * @param userId 用户 ID
     * @param current 当前页
     * @param pageSize 每页大小
     * @return 经验记录列表
     */
    List<UserExpRecordVO> getExpRecords(Long userId, long current, long pageSize);

    /**
     * 处理每日登录奖励
     * 
     * @param userId 用户 ID
     */
    void handleDailyLogin(Long userId);

    /**
     * 检查并处理连续登录奖励
     * 
     * @param userId 用户 ID
     */
    void checkContinuousLogin(Long userId);
}
