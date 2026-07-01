import { request } from '@umijs/max';

export type CourseKnowledgeSyncType =
  | 'course_intro'
  | 'chapter'
  | 'section'
  | 'material'
  | 'question';

interface BaseResponse<T> {
  code?: number;
  data?: T;
  message?: string;
}

export interface PageResult<T> {
  records?: T[];
  total?: number;
  current?: number;
  pageSize?: number;
  list?: T[];
}

export interface CourseKnowledgeSyncRequest {
  courseId: number;
  syncTypes: CourseKnowledgeSyncType[];
}

export interface CourseKnowledgeSyncStatusRequest {
  courseId: number;
}

export interface CourseKnowledgeSyncPageRequest {
  courseId: number;
  current?: number;
  pageSize?: number;
}

export interface CourseKnowledgeSyncRetryRequest {
  syncIds: number[];
}

export interface CourseKnowledgeSyncRefreshRequest {
  courseId: number;
}

export interface CourseKnowledgeSyncSummary {
  total?: number;
  completed?: number;
  indexing?: number;
  failed?: number;
}

export interface CourseKnowledgeSyncDetail {
  id?: number;
  syncId?: number;
  courseId?: number;
  syncType?: string;
  sourceType?: string;
  title?: string;
  sourceName?: string;
  syncMode?: string;
  status?: string | number;
  syncStatus?: string | number;
  retryCount?: number;
  errorMessage?: string;
  updateTime?: string;
  lastSyncTime?: string;
}

export interface CourseKnowledgeSyncStatusVO {
  summary?: CourseKnowledgeSyncSummary;
  details?: CourseKnowledgeSyncDetail[];
  records?: CourseKnowledgeSyncDetail[];
  list?: CourseKnowledgeSyncDetail[];
  total?: number;
  completed?: number;
  indexing?: number;
  failed?: number;
}

export async function triggerCourseKnowledgeSync(body: CourseKnowledgeSyncRequest) {
  return request<BaseResponse<boolean | CourseKnowledgeSyncStatusVO>>('/admin/course/knowledge/sync', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
  });
}

export async function getCourseKnowledgeSyncStatus(courseId: number) {
  return request<BaseResponse<CourseKnowledgeSyncStatusVO>>('/admin/course/knowledge/status', {
    method: 'GET',
    params: {
      courseId,
    },
  });
}

export async function listCourseKnowledgeSyncRecords(body: CourseKnowledgeSyncPageRequest) {
  return request<BaseResponse<PageResult<CourseKnowledgeSyncDetail> | CourseKnowledgeSyncDetail[]>>(
    '/admin/course/knowledge/list/page',
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      data: body,
    },
  );
}

export async function retryCourseKnowledgeSync(body: CourseKnowledgeSyncRetryRequest) {
  return request<BaseResponse<boolean>>('/admin/course/knowledge/retry', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
  });
}

export async function refreshCourseKnowledgeSync(body: CourseKnowledgeSyncRefreshRequest) {
  return request<BaseResponse<boolean | CourseKnowledgeSyncStatusVO>>('/admin/course/knowledge/refresh', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
  });
}

export const COURSE_KNOWLEDGE_SYNC_TYPES: Array<{
  label: string;
  value: CourseKnowledgeSyncType;
}> = [
  { label: '\u8bfe\u7a0b\u7b80\u4ecb', value: 'course_intro' },
  { label: '\u7ae0\u8282', value: 'chapter' },
  { label: '\u5c0f\u8282', value: 'section' },
  { label: '\u8d44\u6599', value: 'material' },
  { label: '\u9898\u76ee', value: 'question' },
];

export const normalizeCourseKnowledgeSyncStatus = (status?: string | number) => {
  if (typeof status === 'number') {
    if (status === 1) {
      return 'completed';
    }
    if (status === 2) {
      return 'indexing';
    }
    if (status === 3) {
      return 'failed';
    }
    return 'pending';
  }

  const normalized = String(status || '').trim().toLowerCase();
  if (!normalized) {
    return 'pending';
  }
  if (normalized.includes('fail') || normalized.includes('error') || normalized.includes('exception')) {
    return 'failed';
  }
  if (normalized.includes('upload')) {
    return 'uploading';
  }
  if (normalized.includes('index') || normalized.includes('process') || normalized.includes('running')) {
    return 'indexing';
  }
  if (normalized.includes('success') || normalized.includes('complete') || normalized.includes('done')) {
    return 'completed';
  }
  return normalized;
};
