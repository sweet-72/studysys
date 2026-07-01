import { request } from '@umijs/max';

interface BaseResponse<T> {
  code?: number;
  data?: T;
  message?: string;
}

interface PageResult<T> {
  records?: T[];
  total?: number;
  current?: number;
  pageSize?: number;
}

export interface CourseHomeworkVO {
  id?: number;
  courseId?: number;
  sectionId?: number;
  title?: string;
  content?: string;
  standardAnswer?: string;
  analysis?: string;
  score?: number;
  sort?: number;
  status?: number;
  createTime?: string;
  updateTime?: string;
}

export interface CourseHomeworkListParams {
  sectionId: number;
  current?: number;
  pageSize?: number;
}

export interface CourseHomeworkAddRequest {
  courseId: number;
  sectionId: number;
  title: string;
  content: string;
  standardAnswer?: string;
  analysis?: string;
  score?: number;
  sort?: number;
  status?: number;
}

export interface CourseHomeworkUpdateRequest extends CourseHomeworkAddRequest {
  id: number;
}

const PAGE_SIZE_LIMIT = 20;

const removeUndefined = <T extends Record<string, any>>(params: T) =>
  Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as T;

export async function listCourseHomeworksByPage(params: CourseHomeworkListParams) {
  const safePageSize = Math.min(Number(params.pageSize || 10), PAGE_SIZE_LIMIT);
  return request<BaseResponse<PageResult<CourseHomeworkVO>>>('/api/course/homework/list/page/vo', {
    method: 'GET',
    params: removeUndefined({
      ...params,
      pageSize: safePageSize,
    }),
  });
}

export async function getCourseHomeworkById(id: number) {
  return request<BaseResponse<CourseHomeworkVO>>('/api/course/homework/get/vo', {
    method: 'GET',
    params: { id },
  });
}

export async function addCourseHomework(body: CourseHomeworkAddRequest) {
  return request<BaseResponse<number>>('/api/course/homework/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
  });
}

export async function updateCourseHomework(body: CourseHomeworkUpdateRequest) {
  return request<BaseResponse<boolean>>('/api/course/homework/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
  });
}

export async function deleteCourseHomework(questionId: number) {
  return request<BaseResponse<boolean>>(`/api/course/homework/delete/${questionId}`, {
    method: 'DELETE',
  });
}

export async function deleteCourseHomeworkCompat(questionId: number) {
  return request<BaseResponse<boolean>>('/api/course/homework/delete', {
    method: 'POST',
    params: {
      questionId,
    },
  });
}

export const COURSE_HOMEWORK_STATUS_OPTIONS = [
  { label: '草稿', value: 0, color: 'default' },
  { label: '启用', value: 1, color: 'success' },
  { label: '停用', value: 2, color: 'warning' },
];

export const resolveCourseHomeworkStatus = (status?: number) => {
  return COURSE_HOMEWORK_STATUS_OPTIONS.find((item) => item.value === status) || COURSE_HOMEWORK_STATUS_OPTIONS[0];
};
