import { request } from '@umijs/max';

/**
 * 获取数据看板总览
 */
export async function getDashboardOverview() {
  return request<API.ResponseStructure<{
    totalUsers: number;
    todayActiveUsers: number;
    totalUsersGrowth: number;
    totalClasses: number;
    totalPosts: number;
    totalPostsGrowth: number;
    totalCourses: number;
    totalCoursesGrowth: number;
    totalAiAvatars: number;
    totalWords: number;
    totalWordsGrowth: number;
    totalArticles: number;
  }>>('/api/dashboard/overview', {
    method: 'GET',
  });
}

/**
 * 获取用户活跃度趋势
 */
export async function getUserActivityTrend() {
  return request<API.ResponseStructure<{
    month: string;
    monthlyActiveUsers: number;
    newUsers: number;
    loginCount: number;
  }[]>>('/api/dashboard/user-activity-trend', {
    method: 'GET',
  });
}
