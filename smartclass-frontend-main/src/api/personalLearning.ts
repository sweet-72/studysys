import type { AxiosResponse } from 'axios';
import apiClient from './index';

interface BaseResponse<T> {
  code?: number;
  data?: T;
  message?: string;
}

interface PageResponse<T> {
  records?: T[];
  total?: number;
  current?: number;
  size?: number;
  pages?: number;
}

export interface RecentLearningRecord {
  courseId: number;
  courseTitle: string;
  sectionId?: number;
  sectionTitle?: string;
  progressPercent?: number;
  lastLearnTime?: string;
  completedSections?: number;
  totalSections?: number;
}

export interface LearningHistoryRecord {
  id: string;
  actionType: string;
  title: string;
  description?: string;
  actionTime?: string;
  courseId?: number;
  sectionId?: number;
  targetId?: number;
  progressPercent?: number;
}

const unwrap = <T>(response: AxiosResponse<BaseResponse<T>>, fallbackMessage: string): T => {
  const payload = response.data;
  if (payload?.code === 0 && payload.data !== undefined) {
    return payload.data;
  }
  throw new Error(payload?.message || fallbackMessage);
};

export const queryRecentLearning = async (
  pageSize = 3,
): Promise<PageResponse<RecentLearningRecord>> => {
  const response = await apiClient.get<BaseResponse<PageResponse<RecentLearningRecord>>>(
    '/api/personal/learning-records/page',
    {
      params: {
        current: 1,
        pageSize,
      },
    },
  );
  return unwrap(response, '最近学习记录获取失败');
};

export const queryLearningHistory = async (
  pageSize = 5,
): Promise<PageResponse<LearningHistoryRecord>> => {
  const response = await apiClient.get<BaseResponse<PageResponse<LearningHistoryRecord>>>(
    '/api/personal/learning-history/page',
    {
      params: {
        current: 1,
        pageSize,
      },
    },
  );
  return unwrap(response, '学习历史获取失败');
};
