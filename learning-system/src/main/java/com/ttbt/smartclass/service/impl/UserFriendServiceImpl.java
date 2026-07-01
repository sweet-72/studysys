package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ttbt.smartclass.common.ErrorCode;
import com.ttbt.smartclass.exception.BusinessException;
import com.ttbt.smartclass.mapper.FriendRelationshipMapper;
import com.ttbt.smartclass.model.entity.FriendRelationship;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.service.UserFriendService;
import com.ttbt.smartclass.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户好友关系服务实现类。
 */
@Service
@Slf4j
public class UserFriendServiceImpl implements UserFriendService {

    @Resource
    private FriendRelationshipMapper friendRelationshipMapper;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addFriend(Long userId1, Long userId2, String status) {
        if (userId1 == null || userId2 == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "User id must not be null");
        }
        if (userId1.equals(userId2)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Cannot add yourself as friend");
        }

        User user1 = userService.getById(userId1);
        User user2 = userService.getById(userId2);
        if (user1 == null || user2 == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "User not found");
        }

        FriendRelationship existRelationship = getFriendRelationship(userId1, userId2);
        if (existRelationship != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Friend relationship already exists");
        }

        FriendRelationship relationship1 = new FriendRelationship();
        relationship1.setUserId1(userId1);
        relationship1.setUserId2(userId2);
        relationship1.setStatus(status);

        FriendRelationship relationship2 = new FriendRelationship();
        relationship2.setUserId1(userId2);
        relationship2.setUserId2(userId1);
        relationship2.setStatus(status);

        boolean saveResult1 = friendRelationshipMapper.insert(relationship1) > 0;
        boolean saveResult2 = friendRelationshipMapper.insert(relationship2) > 0;
        if (!saveResult1 || !saveResult2) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Failed to create friend relationship");
        }

        log.info("create friend relationship, user_id1={}, user_id2={}, status={}", userId1, userId2, status);
        return relationship1.getId();
    }

    @Override
    public boolean isFriend(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null) {
            return false;
        }
        FriendRelationship relationship = getFriendRelationship(userId1, userId2);
        return relationship != null && "accepted".equalsIgnoreCase(relationship.getStatus());
    }

    @Override
    public List<User> getFriendList(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "User id must not be null");
        }

        LambdaQueryWrapper<FriendRelationship> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(qw -> qw
                .eq(FriendRelationship::getUserId1, userId)
                .or()
                .eq(FriendRelationship::getUserId2, userId));

        List<FriendRelationship> relationships = friendRelationshipMapper.selectList(queryWrapper);
        if (relationships.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> friendIds = relationships.stream()
                .map(rel -> userId.equals(rel.getUserId1()) ? rel.getUserId2() : rel.getUserId1())
                .distinct()
                .collect(Collectors.toList());

        return userService.listByIds(friendIds);
    }

    @Override
    public FriendRelationship getFriendRelationship(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null) {
            return null;
        }

        LambdaQueryWrapper<FriendRelationship> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(qw -> qw
                .eq(FriendRelationship::getUserId1, userId1)
                .eq(FriendRelationship::getUserId2, userId2)
                .or()
                .eq(FriendRelationship::getUserId1, userId2)
                .eq(FriendRelationship::getUserId2, userId1));

        List<FriendRelationship> relationships = friendRelationshipMapper.selectList(queryWrapper);
        return relationships.isEmpty() ? null : relationships.get(0);
    }

    @Override
    public boolean checkIsAlreadyFriends(Long userId1, Long userId2) {
        return getFriendRelationship(userId1, userId2) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "User id must not be null");
        }

        LambdaQueryWrapper<FriendRelationship> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(FriendRelationship::getUserId1, userId1)
                .eq(FriendRelationship::getUserId2, userId2);
        friendRelationshipMapper.delete(queryWrapper1);

        LambdaQueryWrapper<FriendRelationship> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(FriendRelationship::getUserId1, userId2)
                .eq(FriendRelationship::getUserId2, userId1);
        friendRelationshipMapper.delete(queryWrapper2);

        log.info("delete friend relationship, user_id1={}, user_id2={}", userId1, userId2);
    }
}
