import { request } from '@umijs/max';

/**
 * 获取智能体统计总览
 */
export async function getAiAvatarOverview() {
  return request<API.ResponseStructure<{
    totalUsers: number;
    totalConversations: number;
    avgConversationDuration: number;
    avgRating: number;
  }>>('/api/ai-avatar-stats/overview', {
    method: 'GET',
  });
}

/**
 * 获取智能体使用趋势
 * @param days 天数：7本周，30本月，365本年
 */
export async function getAiAvatarUsageTrend(days: number = 7) {
  return request<API.ResponseStructure<{
    date: string;
    avatarName: string;
    usageCount: number;
  }[]>>('/api/ai-avatar-stats/usage-trend', {
    method: 'GET',
    params: { days },
  });
}

/**
 * 获取用户分布
 */
export async function getAiAvatarUserDistribution() {
  return request<API.ResponseStructure<{
    userRole: string;
    count: number;
  }[]>>('/api/ai-avatar-stats/user-distribution', {
    method: 'GET',
  });
}

/**
 * 获取评分分布
 */
export async function getAiAvatarRatingDistribution() {
  return request<API.ResponseStructure<{
    avatarName: string;
    avgRating: number;
    ratingCount: number;
    usageCount: number;
  }[]>>('/api/ai-avatar-stats/rating-distribution', {
    method: 'GET',
  });
}

/**
 * 获取对话时长分布
 * @param days 天数
 */
export async function getAiAvatarConversationDuration(days: number = 7) {
  return request<API.ResponseStructure<{
    date: string;
    avatarName: string;
    avgDuration: number;
  }[]>>('/api/ai-avatar-stats/conversation-duration', {
    method: 'GET',
    params: { days },
  });
}
