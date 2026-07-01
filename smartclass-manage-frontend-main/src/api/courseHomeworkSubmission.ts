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

const PAGE_SIZE_LIMIT = 20;

export interface CourseHomeworkSubmissionVO {
  id?: number;
  homeworkId?: number;
  homeworkTitle?: string;
  courseId?: number;
  sectionId?: number;
  studentId?: number;
  studentName?: string;
  submitTime?: string;
  reviewStatus?: number | string;
  reviewScore?: number;
  score?: number;
}

export interface CourseHomeworkSubmissionDetailVO extends CourseHomeworkSubmissionVO {
  answerContent?: string;
  answerAttachmentUrl?: string;
  reviewComment?: string;
  comment?: string;
  reviewerId?: number;
  reviewerName?: string;
  reviewTime?: string;
}

export interface CourseHomeworkSubmissionListParams {
  courseId?: number;
  sectionId?: number;
  homeworkId?: number;
  reviewStatus?: number;
  current?: number;
  pageSize?: number;
}

export interface ReviewCourseHomeworkSubmissionRequest {
  submissionId: number;
  score: number;
  comment: string;
}

const removeUndefined = <T extends Record<string, any>>(params: T) =>
  Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as T;

export async function listCourseHomeworkSubmissionsByPage(params: CourseHomeworkSubmissionListParams) {
  const safePageSize = Math.min(Number(params.pageSize || 10), PAGE_SIZE_LIMIT);
  return request<BaseResponse<PageResult<CourseHomeworkSubmissionVO>>>(
    '/api/course/homework/submission/list/page/vo',
    {
      method: 'GET',
      params: removeUndefined({
        ...params,
        pageSize: safePageSize,
      }),
    },
  );
}

export async function getCourseHomeworkSubmissionDetail(id: number) {
  return request<BaseResponse<CourseHomeworkSubmissionDetailVO>>(
    '/api/course/homework/submission/get/vo',
    {
      method: 'GET',
      params: { id },
    },
  );
}

export async function reviewCourseHomeworkSubmission(body: ReviewCourseHomeworkSubmissionRequest) {
  return request<BaseResponse<boolean>>('/api/course/homework/submission/review', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
  });
}

export const COURSE_HOMEWORK_REVIEW_STATUS_OPTIONS = [
  { label: String.fromCharCode(0x5f85, 0x6279, 0x6539), value: 0, color: 'processing' },
  { label: String.fromCharCode(0x5df2, 0x6279, 0x6539), value: 1, color: 'success' },
];

export const resolveCourseHomeworkReviewStatus = (status?: number | string) => {
  const normalizedStatus = Number(status);
  return (
    COURSE_HOMEWORK_REVIEW_STATUS_OPTIONS.find((item) => item.value === normalizedStatus) ||
    COURSE_HOMEWORK_REVIEW_STATUS_OPTIONS[0]
  );
};