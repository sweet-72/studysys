package com.ttbt.smartclass.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ttbt.smartclass.model.entity.FriendRelationship;
import com.ttbt.smartclass.model.entity.User;

import java.util.List;

/**
 * 用户好友关系服务接口
 */
public interface UserFriendService {

    /**
     * 添加好友（双向关系）
     * 
     * @param userId1 用户 ID1
     * @param userId2 用户 ID2
     * @param status 关系状态
     * @return 关系 ID
     */
    long addFriend(Long userId1, Long userId2, String status);

    /**
     * 判断两个用户是否为好友
     * 
     * @param userId1 用户 ID1
     * @param userId2 用户 ID2
     * @return true-是好友，false-不是好友
     */
    boolean isFriend(Long userId1, Long userId2);

    /**
     * 获取用户的好友列表
     * 
     * @param userId 用户 ID
     * @return 好友列表
     */
    List<User> getFriendList(Long userId);

    /**
     * 获取用户的好友关系（单向）
     * 
     * @param userId1 用户 ID1
     * @param userId2 用户 ID2
     * @return 好友关系，不存在返回 null
     */
    FriendRelationship getFriendRelationship(Long userId1, Long userId2);

    /**
     * 检查用户是否已经是好友（双向检查）
     * 
     * @param userId1 用户 ID1
     * @param userId2 用户 ID2
     * @return true-已是好友，false-不是好友
     */
    boolean checkIsAlreadyFriends(Long userId1, Long userId2);

    /**
     * 删除好友关系（双向删除）
     * 
     * @param userId1 用户 ID1
     * @param userId2 用户 ID2
     */
    void deleteFriend(Long userId1, Long userId2);
}
