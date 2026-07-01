import type { AxiosResponse } from 'axios';
import apiClient from './index';

interface BaseResponse<T> {
  code?: number;
  data?: T;
  message?: string;
}

export interface UserExpLevelInfo {
  userId?: number;
  level?: number;
  levelName?: string;
  exp?: number;
  nextLevelExp?: number;
  totalExp?: number;
  progressPercent?: number;
  expToNextLevel?: number;
  continuousLoginDays?: number;
  levelUpTime?: string;
  iconUrl?: string;
  privilegeDesc?: string;
}

const unwrap = <T>(response: AxiosResponse<BaseResponse<T>>): T => {
  const payload = response.data;
  if (payload?.code === 0 && payload.data !== undefined) {
    return payload.data;
  }
  throw new Error(payload?.message || '等级信息获取失败');
};

export const queryUserExpLevel = async (userId: number): Promise<UserExpLevelInfo> => {
  const response = await apiClient.get<BaseResponse<UserExpLevelInfo>>(`/api/user/exp/level/${userId}`);
  return unwrap(response);
};
