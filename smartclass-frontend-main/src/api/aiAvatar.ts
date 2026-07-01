import type { AxiosResponse } from 'axios';
import apiClient from './index';
import { buildApiUrl } from '@/utils/api';

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

export interface AiAvatarInfo {
  id?: number;
  name?: string;
  baseUrl?: string;
  description?: string;
  avatarImgUrl?: string;
  tags?: string;
  personality?: string;
  abilities?: string;
  isPublic?: number;
  status?: number;
  usageCount?: number;
  rating?: number;
  ratingCount?: number;
  creatorId?: number;
  sort?: number;
  createTime?: string;
  updateTime?: string;
}

export interface AiAvatarPageQuery {
  current?: number;
  pageSize?: number;
  keyword?: string;
}

const unwrap = <T>(response: AxiosResponse<BaseResponse<T>>, fallbackMessage: string): T => {
  const payload = response.data;
  if (payload?.code === 0 && payload.data !== undefined) {
    return payload.data;
  }

  throw new Error(payload?.message || fallbackMessage);
};

export const queryAiAvatarPage = async (
  query: AiAvatarPageQuery = {},
): Promise<PageResponse<AiAvatarInfo>> => {
  const params: Record<string, string | number> = {
    current: query.current ?? 1,
    pageSize: query.pageSize ?? 20,
  };

  const keyword = query.keyword?.trim();
  if (keyword) {
    params.keyword = keyword;
  }

  const response = await apiClient.get<BaseResponse<PageResponse<AiAvatarInfo>>>(
    '/api/ai-avatars/page',
    { params },
  );

  return unwrap(response, '智慧体列表获取失败');
};

export const queryAiAvatarDetail = async (id: number): Promise<AiAvatarInfo> => {
  const response = await apiClient.get<BaseResponse<AiAvatarInfo>>(`/api/ai-avatars/${id}`);
  return unwrap(response, '智慧体详情获取失败');
};

export const resolveAiAvatarImage = (avatarUrl?: string): string => {
  const value = avatarUrl?.trim();
  if (!value) {
    return '';
  }

  if (
    value.startsWith('http://') ||
    value.startsWith('https://') ||
    value.startsWith('//') ||
    value.startsWith('data:') ||
    value.startsWith('blob:')
  ) {
    return value;
  }

  return buildApiUrl(value.startsWith('/') ? value : `/${value}`);
};
