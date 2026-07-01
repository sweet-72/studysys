import type { AxiosResponse } from 'axios';
import apiClient from './index';

interface BaseResponse<T> {
  code?: number;
  data?: T;
  message?: string;
}

export interface DailyGoalItem {
  id: number;
  goalId: number;
  userId?: number;
  goalDate?: string;
  title: string;
  goalType: string;
  targetValue: number;
  currentValue: number;
  unit?: string;
  status: number;
  source: 'SYSTEM' | 'CARRY_OVER' | 'CUSTOM' | string;
  carryFromItemId?: number;
  completedTime?: string;
  createTime?: string;
  updateTime?: string;
}

export interface DailyGoalToday {
  id: number;
  userId: number;
  goalDate: string;
  totalGoals: number;
  completedGoals: number;
  progressPercent: number;
  isCompleted: number;
  completedTime?: string;
  items: DailyGoalItem[];
}

export interface DailyGoalItemAddPayload {
  title: string;
  goalType?: string;
  targetValue?: number;
  unit?: string;
}

const unwrap = <T>(response: AxiosResponse<BaseResponse<T>>, fallbackMessage: string): T => {
  const payload = response.data;
  if (payload?.code === 0 && payload.data !== undefined) {
    return payload.data;
  }
  throw new Error(payload?.message || fallbackMessage);
};

export const queryTodayDailyGoal = async (): Promise<DailyGoalToday> => {
  const response = await apiClient.get<BaseResponse<DailyGoalToday>>('/api/daily-goal/today');
  return unwrap(response, '今日学习目标获取失败');
};

export const completeDailyGoalItem = async (itemId: number): Promise<DailyGoalToday> => {
  const response = await apiClient.post<BaseResponse<DailyGoalToday>>('/api/daily-goal/item/complete', {
    itemId,
  });
  return unwrap(response, '目标完成失败');
};

export const cancelCompleteDailyGoalItem = async (itemId: number): Promise<DailyGoalToday> => {
  const response = await apiClient.post<BaseResponse<DailyGoalToday>>('/api/daily-goal/item/cancel-complete', {
    itemId,
  });
  return unwrap(response, '目标取消完成失败');
};

export const updateDailyGoalItemProgress = async (
  itemId: number,
  currentValue: number,
): Promise<DailyGoalToday> => {
  const response = await apiClient.post<BaseResponse<DailyGoalToday>>(
    '/api/daily-goal/item/update-progress',
    {
      itemId,
      currentValue,
    },
  );
  return unwrap(response, '目标进度更新失败');
};

export const addDailyGoalItem = async (
  payload: DailyGoalItemAddPayload,
): Promise<DailyGoalToday> => {
  const response = await apiClient.post<BaseResponse<DailyGoalToday>>('/api/daily-goal/item/add', payload);
  return unwrap(response, '目标添加失败');
};
