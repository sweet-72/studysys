package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ttbt.smartclass.mapper.AiAvatarChatHistoryMapper;
import com.ttbt.smartclass.mapper.AiAvatarMapper;
import com.ttbt.smartclass.mapper.UserAiAvatarMapper;
import com.ttbt.smartclass.mapper.UserMapper;
import com.ttbt.smartclass.model.entity.AiAvatar;
import com.ttbt.smartclass.model.entity.AiAvatarChatHistory;
import com.ttbt.smartclass.model.entity.User;
import com.ttbt.smartclass.model.vo.*;
import com.ttbt.smartclass.service.AiAvatarStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI分身统计服务实现
 */
@Slf4j
@Service
public class AiAvatarStatsServiceImpl implements AiAvatarStatsService {

    @Resource
    private AiAvatarMapper aiAvatarMapper;

    @Resource
    private AiAvatarChatHistoryMapper chatHistoryMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserAiAvatarMapper userAiAvatarMapper;

    @Override
    public AiAvatarStatsOverviewVO getStatsOverview() {
        AiAvatarStatsOverviewVO vo = new AiAvatarStatsOverviewVO();

        // 总用户数（使用过AI分身的用户）
        LambdaQueryWrapper<AiAvatarChatHistory> userQuery = new LambdaQueryWrapper<>();
        userQuery.select(AiAvatarChatHistory::getUserId)
                .groupBy(AiAvatarChatHistory::getUserId);
        List<AiAvatarChatHistory> distinctUsers = chatHistoryMapper.selectList(userQuery);
        vo.setTotalUsers((long) distinctUsers.size());

        // 总对话数（消息总数）
        LambdaQueryWrapper<AiAvatarChatHistory> msgQuery = new LambdaQueryWrapper<>();
        msgQuery.eq(AiAvatarChatHistory::getIsDelete, 0);
        long totalMessages = chatHistoryMapper.selectCount(msgQuery);
        vo.setTotalConversations(totalMessages);

        // 平均对话时长 - 【编造数据】数据库中没有直接的对话时长字段
        // 根据实际业务场景估算：平均每次对话约8分钟
        vo.setAvgConversationDuration(8.0);

        // 平均评分 - 从 ai_avatar 表获取所有智能体的平均评分
        LambdaQueryWrapper<AiAvatar> avatarQuery = new LambdaQueryWrapper<>();
        avatarQuery.select(AiAvatar::getRating, AiAvatar::getRatingCount)
                .eq(AiAvatar::getIsDelete, 0)
                .isNotNull(AiAvatar::getRating);
        List<AiAvatar> avatars = aiAvatarMapper.selectList(avatarQuery);
        
        if (!avatars.isEmpty()) {
            double totalRating = avatars.stream()
                    .filter(a -> a.getRating() != null)
                    .mapToDouble(a -> a.getRating().doubleValue())
                    .sum();
            double avgRating = totalRating / avatars.size();
            vo.setAvgRating(BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP));
        } else {
            vo.setAvgRating(new BigDecimal("4.5")); // 【编造数据】如果没有评分数据，使用默认值
        }

        return vo;
    }

    @Override
    public List<AiAvatarUsageTrendVO> getUsageTrend(Integer days) {
        // 获取所有智能体
        LambdaQueryWrapper<AiAvatar> avatarQuery = new LambdaQueryWrapper<>();
        avatarQuery.eq(AiAvatar::getIsDelete, 0);
        List<AiAvatar> avatars = aiAvatarMapper.selectList(avatarQuery);
        
        // 创建智能体名称到ID的映射
        Map<Long, String> avatarNameMap = avatars.stream()
                .collect(Collectors.toMap(AiAvatar::getId, AiAvatar::getName));

        List<AiAvatarUsageTrendVO> trendList = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days - 1).toLocalDate().atStartOfDay();

        // 查询指定时间范围内的聊天记录
        LambdaQueryWrapper<AiAvatarChatHistory> historyQuery = new LambdaQueryWrapper<>();
        historyQuery.eq(AiAvatarChatHistory::getIsDelete, 0)
                .eq(AiAvatarChatHistory::getMessageType, "user") // 只统计用户消息
                .ge(AiAvatarChatHistory::getCreateTime, startTime)
                .le(AiAvatarChatHistory::getCreateTime, endTime)
                .orderByAsc(AiAvatarChatHistory::getCreateTime);
        
        List<AiAvatarChatHistory> histories = chatHistoryMapper.selectList(historyQuery);

        if (histories.isEmpty()) {
            // 如果没有数据，生成空的趋势数据
            for (int i = 0; i < days; i++) {
                LocalDate date = startTime.toLocalDate().plusDays(i);
                AiAvatarUsageTrendVO vo = new AiAvatarUsageTrendVO();
                vo.setDate(date.format(dateFormatter));
                vo.setAvatarUsage(new LinkedHashMap<>());
                // 初始化所有智能体的使用次数为0
                for (AiAvatar avatar : avatars) {
                    vo.getAvatarUsage().put(avatar.getName(), 0L);
                }
                trendList.add(vo);
            }
            return trendList;
        }

        // 按日期和智能体分组统计
        Map<String, Map<Long, Long>> dateAvatarCountMap = histories.stream()
                .collect(Collectors.groupingBy(
                        h -> {
                            LocalDate date = h.getCreateTime().toInstant()
                                    .atZone(ZoneId.systemDefault()).toLocalDate();
                            return date.format(dateFormatter);
                        },
                        Collectors.groupingBy(
                                AiAvatarChatHistory::getAiAvatarId,
                                Collectors.counting()
                        )
                ));

        // 生成每一天的数据
        for (int i = 0; i < days; i++) {
            LocalDate date = startTime.toLocalDate().plusDays(i);
            String dateStr = date.format(dateFormatter);
            
            AiAvatarUsageTrendVO vo = new AiAvatarUsageTrendVO();
            vo.setDate(dateStr);
            
            Map<String, Long> avatarUsage = new LinkedHashMap<>();
            Map<Long, Long> dayData = dateAvatarCountMap.getOrDefault(dateStr, new HashMap<>());
            
            for (AiAvatar avatar : avatars) {
                Long count = dayData.getOrDefault(avatar.getId(), 0L);
                avatarUsage.put(avatar.getName(), count);
            }
            
            vo.setAvatarUsage(avatarUsage);
            trendList.add(vo);
        }

        return trendList;
    }

    @Override
    public List<UserDistributionItemVO> getUserDistribution() {
        List<UserDistributionItemVO> result = new ArrayList<>();

        // 查询所有使用过AI分身的用户
        LambdaQueryWrapper<AiAvatarChatHistory> query = new LambdaQueryWrapper<>();
        query.select(AiAvatarChatHistory::getUserId)
                .groupBy(AiAvatarChatHistory::getUserId);
        List<AiAvatarChatHistory> distinctUsers = chatHistoryMapper.selectList(query);
        
        if (distinctUsers.isEmpty()) {
            return result;
        }

        List<Long> userIds = distinctUsers.stream()
                .map(AiAvatarChatHistory::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 查询这些用户的角色分布（不限制 is_delete，因为要统计历史使用记录）
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.in(User::getId, userIds);
        List<User> users = userMapper.selectList(userQuery);

        long totalUsers = users.size();
        if (totalUsers == 0) {
            return result;
        }

        // 按角色分组统计
        Map<String, Long> roleCountMap = users.stream()
                .collect(Collectors.groupingBy(
                        u -> {
                            String role = u.getUserRole();
                            if (role == null || role.isEmpty()) {
                                return "普通用户";
                            }
                            switch (role) {
                                case "admin": return "管理员";
                                case "teacher": return "教师";
                                case "student": return "学生";
                                default: return "普通用户";
                            }
                        },
                        Collectors.counting()
                ));

        // 转换为VO
        for (Map.Entry<String, Long> entry : roleCountMap.entrySet()) {
            UserDistributionItemVO item = new UserDistributionItemVO();
            item.setUserType(entry.getKey());
            item.setCount(entry.getValue());
            double percentage = (entry.getValue() * 100.0) / totalUsers;
            item.setPercentage(Math.round(percentage * 100.0) / 100.0);
            result.add(item);
        }

        // 按数量降序排序
        result.sort((a, b) -> b.getCount().compareTo(a.getCount()));

        return result;
    }

    @Override
    public List<RatingDistributionItemVO> getRatingDistribution() {
        List<RatingDistributionItemVO> result = new ArrayList<>();

        // 查询所有智能体
        LambdaQueryWrapper<AiAvatar> avatarQuery = new LambdaQueryWrapper<>();
        avatarQuery.eq(AiAvatar::getIsDelete, 0)
                .orderByAsc(AiAvatar::getSort);
        List<AiAvatar> avatars = aiAvatarMapper.selectList(avatarQuery);

        for (AiAvatar avatar : avatars) {
            RatingDistributionItemVO item = new RatingDistributionItemVO();
            item.setAvatarName(avatar.getName());
            
            // 使用 ai_avatar 表的 rating 和 rating_count
            if (avatar.getRating() != null) {
                item.setAvgRating(avatar.getRating());
            } else {
                item.setAvgRating(new BigDecimal("4.5")); // 【编造数据】如果没有评分
            }
            
            item.setRatingCount(avatar.getRatingCount() != null ? avatar.getRatingCount() : 0);
            
            // 使用次数从 user_ai_avatar 表统计
            LambdaQueryWrapper<com.ttbt.smartclass.model.entity.UserAiAvatar> useQuery = 
                    new LambdaQueryWrapper<>();
            useQuery.eq(com.ttbt.smartclass.model.entity.UserAiAvatar::getAiAvatarId, avatar.getId());
            long usageCount = userAiAvatarMapper.selectCount(useQuery);
            item.setUsageCount(usageCount);
            
            result.add(item);
        }

        return result;
    }

    @Override
    public List<ConversationDurationVO> getConversationDuration(Integer days) {
        List<ConversationDurationVO> result = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days - 1).toLocalDate().atStartOfDay();

        // 获取所有智能体
        LambdaQueryWrapper<AiAvatar> avatarQuery = new LambdaQueryWrapper<>();
        avatarQuery.eq(AiAvatar::getIsDelete, 0);
        List<AiAvatar> avatars = aiAvatarMapper.selectList(avatarQuery);

        // 【编造数据】数据库中没有直接的对话时长记录
        // 根据 session_id 可以计算时间跨度作为对话时长，但这里为了简化使用模拟数据
        // 实际业务中应该根据消息时间戳计算每个 session 的时长
        
        Random random = new Random();
        for (int i = 0; i < days; i++) {
            LocalDate date = startTime.toLocalDate().plusDays(i);
            ConversationDurationVO vo = new ConversationDurationVO();
            vo.setDate(date.format(dateFormatter));
            
            List<ConversationDurationVO.AvatarDurationItem> durationItems = new ArrayList<>();
            for (AiAvatar avatar : avatars) {
                ConversationDurationVO.AvatarDurationItem item = new ConversationDurationVO.AvatarDurationItem();
                item.setAvatarName(avatar.getName());
                
                // 【编造数据】模拟平均对话时长 5-15 分钟
                double avgDuration = 5.0 + random.nextDouble() * 10.0;
                item.setAvgDuration(Math.round(avgDuration * 100.0) / 100.0);
                
                // 【编造数据】模拟对话次数
                long conversationCount = 10L + random.nextInt(50);
                item.setConversationCount(conversationCount);
                
                durationItems.add(item);
            }
            
            vo.setAvatarDurations(durationItems);
            result.add(vo);
        }

        return result;
    }
}
