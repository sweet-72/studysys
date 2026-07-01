package com.ttbt.smartclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ttbt.smartclass.mapper.*;
import com.ttbt.smartclass.model.entity.*;
import com.ttbt.smartclass.model.vo.DashboardOverviewVO;
import com.ttbt.smartclass.model.vo.UserActivityTrendItemVO;
import com.ttbt.smartclass.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据看板服务实现
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private PostMapper postMapper;

    @Resource
    private AiAvatarMapper aiAvatarMapper;

    @Resource
    private DailyWordMapper dailyWordMapper;

    @Resource
    private DailyArticleMapper dailyArticleMapper;

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Override
    public DashboardOverviewVO getDashboardOverview() {
        DashboardOverviewVO vo = new DashboardOverviewVO();

        // 总用户数
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getIsDelete, 0);
        long totalUsers = userMapper.selectCount(userQuery);
        vo.setTotalUsers(totalUsers);

        // 今日活跃用户（今天有登录或活动的用户）
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        
        // 通过聊天会话统计今日活跃用户
        LambdaQueryWrapper<ChatSession> chatQuery = new LambdaQueryWrapper<>();
        chatQuery.ge(ChatSession::getUpdateTime, todayStart)
                .lt(ChatSession::getUpdateTime, todayEnd);
        long todayActiveUsers = chatSessionMapper.selectCount(chatQuery);
        
        // 如果聊天会话数为0，则使用用户创建时间作为备选
        if (todayActiveUsers == 0) {
            LambdaQueryWrapper<User> todayUserQuery = new LambdaQueryWrapper<>();
            todayUserQuery.eq(User::getIsDelete, 0)
                    .ge(User::getUpdateTime, todayStart)
                    .lt(User::getUpdateTime, todayEnd);
            todayActiveUsers = userMapper.selectCount(todayUserQuery);
        }
        vo.setTodayActiveUsers(todayActiveUsers);

        // 总用户增长率（与昨天相比）
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        LocalDateTime yesterdayEnd = todayStart;
        
        LambdaQueryWrapper<User> yesterdayUserQuery = new LambdaQueryWrapper<>();
        yesterdayUserQuery.eq(User::getIsDelete, 0)
                .ge(User::getCreateTime, yesterdayStart)
                .lt(User::getCreateTime, yesterdayEnd);
        long yesterdayNewUsers = userMapper.selectCount(yesterdayUserQuery);
        
        LambdaQueryWrapper<User> todayNewUserQuery = new LambdaQueryWrapper<>();
        todayNewUserQuery.eq(User::getIsDelete, 0)
                .ge(User::getCreateTime, todayStart)
                .lt(User::getCreateTime, todayEnd);
        long todayNewUsers = userMapper.selectCount(todayNewUserQuery);
        
        String userGrowth = calculateGrowthRate(todayNewUsers, yesterdayNewUsers);
        vo.setTotalUsersGrowth(userGrowth);

        // 班级总数（这里用课程代替，因为系统可能没有独立的班级概念）
        LambdaQueryWrapper<Course> courseQuery = new LambdaQueryWrapper<>();
        courseQuery.eq(Course::getIsDelete, 0);
        long totalCourses = courseMapper.selectCount(courseQuery);
        vo.setTotalClasses(totalCourses);

        // 帖子总数
        LambdaQueryWrapper<Post> postQuery = new LambdaQueryWrapper<>();
        postQuery.eq(Post::getIsDelete, 0);
        long totalPosts = postMapper.selectCount(postQuery);
        vo.setTotalPosts(totalPosts);

        // 帖子增长率
        LambdaQueryWrapper<Post> yesterdayPostQuery = new LambdaQueryWrapper<>();
        yesterdayPostQuery.eq(Post::getIsDelete, 0)
                .ge(Post::getCreateTime, yesterdayStart)
                .lt(Post::getCreateTime, yesterdayEnd);
        long yesterdayNewPosts = postMapper.selectCount(yesterdayPostQuery);
        
        LambdaQueryWrapper<Post> todayPostQuery = new LambdaQueryWrapper<>();
        todayPostQuery.eq(Post::getIsDelete, 0)
                .ge(Post::getCreateTime, todayStart)
                .lt(Post::getCreateTime, todayEnd);
        long todayNewPosts = postMapper.selectCount(todayPostQuery);
        
        String postGrowth = calculateGrowthRate(todayNewPosts, yesterdayNewPosts);
        vo.setTotalPostsGrowth(postGrowth);

        // 课程总数
        vo.setTotalCourses(totalCourses);
        String courseGrowth = calculateGrowthRate(0, 0); // 需要更复杂的逻辑来计算课程增长
        vo.setTotalCoursesGrowth(courseGrowth);

        // AI分身数量
        LambdaQueryWrapper<AiAvatar> avatarQuery = new LambdaQueryWrapper<>();
        avatarQuery.eq(AiAvatar::getIsDelete, 0);
        long totalAiAvatars = aiAvatarMapper.selectCount(avatarQuery);
        vo.setTotalAiAvatars(totalAiAvatars);

        // 单词库数量
        LambdaQueryWrapper<DailyWord> wordQuery = new LambdaQueryWrapper<>();
        long totalWords = dailyWordMapper.selectCount(wordQuery);
        vo.setTotalWords(totalWords);

        // 单词库增长率
        LambdaQueryWrapper<DailyWord> yesterdayWordQuery = new LambdaQueryWrapper<>();
        yesterdayWordQuery.ge(DailyWord::getCreateTime, yesterdayStart)
                .lt(DailyWord::getCreateTime, yesterdayEnd);
        long yesterdayNewWords = dailyWordMapper.selectCount(yesterdayWordQuery);
        
        LambdaQueryWrapper<DailyWord> todayWordQuery = new LambdaQueryWrapper<>();
        todayWordQuery.ge(DailyWord::getCreateTime, todayStart)
                .lt(DailyWord::getCreateTime, todayEnd);
        long todayNewWords = dailyWordMapper.selectCount(todayWordQuery);
        
        String wordGrowth = calculateGrowthRate(todayNewWords, yesterdayNewWords);
        vo.setTotalWordsGrowth(wordGrowth);

        // 美文数量
        LambdaQueryWrapper<DailyArticle> articleQuery = new LambdaQueryWrapper<>();
        long totalArticles = dailyArticleMapper.selectCount(articleQuery);
        vo.setTotalArticles(totalArticles);

        return vo;
    }

    @Override
    public List<UserActivityTrendItemVO> getUserActivityTrend() {
        List<UserActivityTrendItemVO> trendList = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        // 获取近6个月的数据
        for (int i = 5; i >= 0; i--) {
            LocalDate firstDayOfMonth = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
            
            LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();
            LocalDateTime monthEnd = lastDayOfMonth.atTime(23, 59, 59);
            
            UserActivityTrendItemVO item = new UserActivityTrendItemVO();
            item.setMonth(firstDayOfMonth.format(monthFormatter));
            
            // 月活跃用户数（通过聊天会话统计）
            LambdaQueryWrapper<ChatSession> activeQuery = new LambdaQueryWrapper<>();
            activeQuery.ge(ChatSession::getUpdateTime, monthStart)
                    .le(ChatSession::getUpdateTime, monthEnd);
            long monthlyActiveUsers = chatSessionMapper.selectCount(activeQuery);
            item.setMonthlyActiveUsers(monthlyActiveUsers);
            
            // 新增用户数
            LambdaQueryWrapper<User> newUserQuery = new LambdaQueryWrapper<>();
            newUserQuery.eq(User::getIsDelete, 0)
                    .ge(User::getCreateTime, monthStart)
                    .le(User::getCreateTime, monthEnd);
            long newUsers = userMapper.selectCount(newUserQuery);
            item.setNewUsers(newUsers);
            
            // 登录次数（这里用聊天会话数近似表示活跃度）
            item.setLoginCount(monthlyActiveUsers);
            
            trendList.add(item);
        }
        
        return trendList;
    }

    /**
     * 计算增长率
     *
     * @param current 当前值
     * @param previous 之前的值
     * @return 增长率字符串（百分比）
     */
    private String calculateGrowthRate(long current, long previous) {
        if (previous == 0) {
            if (current == 0) {
                return "0.00%";
            } else {
                return "+100.00%";
            }
        }
        
        double growthRate = ((double) (current - previous) / previous) * 100;
        String sign = growthRate >= 0 ? "+" : "";
        return String.format("%s%.2f%%", sign, growthRate);
    }
}
