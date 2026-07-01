import { request } from '@umijs/max';

export interface ApiResponse<T> {
  code?: number;
  data?: T;
  message?: string;
}

export interface PageResult<T> {
  records?: T[];
  total?: number;
  current?: number;
  pageSize?: number;
}

export type CourseStatus = 0 | 1 | 2 | string;

export interface CourseSectionItem {
  sectionId?: number;
  title: string;
  description?: string;
  assistantKnowledge?: string;
  aiKnowledgeContent?: string;
  aiKnowledge?: string;
  aiContent?: string;
  knowledgeContent?: string;
  sort?: number;
  duration?: number;
  videoUrl?: string;
  localVideoPath?: string;
  assignmentId?: number;
  assignmentTitle?: string;
  assignmentStatus?: string;
}

export interface CourseChapterItem {
  chapterId?: number;
  title: string;
  description?: string;
  sort?: number;
  sections: CourseSectionItem[];
}

export interface CourseItem {
  courseId?: number;
  title?: string;
  aiKnowleage?: string;
  category?: string;
  categoryName?: string;
  categoryId?: number;
  difficulty?: string;
  status?: CourseStatus;
  teacherId?: number;
  teacherName?: string;
  description?: string;
  updateTime?: string;
  chapters?: CourseChapterItem[];
}

export interface CourseListQuery {
  current?: number;
  pageSize?: number;
  title?: string;
  category?: string;
  categoryId?: number;
  difficulty?: string;
  status?: CourseStatus;
  teacher?: string;
}

export interface CourseUpsertPayload {
  courseId?: number;
  title: string;
  aiKnowleage: string;
  category?: string;
  categoryId?: number;
  difficulty?: string;
  status?: CourseStatus;
  teacherId: number;
  teacherName?: string;
  description?: string;
  chapters: CourseChapterItem[];
}

export interface DeleteCoursePayload {
  courseId: number;
}

export interface UploadVideoPayload {
  file?: File;
  videoUrl?: string;
  courseId?: number;
  chapterId?: number;
  sectionId?: number;
}

export interface UploadVideoResult {
  localVideoPath?: string;
  videoUrl?: string;
  url?: string;
  path?: string;
}

export interface CourseDetailQuery {
  courseId: number;
}

export interface CourseReviewPayload {
  reviewId?: number;
  assignmentId?: number;
  courseId: number;
  chapterId?: number;
  sectionId?: number;
  score: number;
  comment: string;
}

const removeUndefined = <T extends Record<string, any>>(params: T) =>
  Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as T;

export async function createCourse(body: CourseUpsertPayload) {
  return request<ApiResponse<number>>('/api/course/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
  });
}

export async function updateCourse(body: CourseUpsertPayload) {
  return request<ApiResponse<boolean>>('/api/course/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
  });
}

export async function deleteCourse(body: DeleteCoursePayload) {
  return request<ApiResponse<boolean>>('/api/course/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
  });
}

export async function uploadCourseVideo(payload: UploadVideoPayload) {
  if (!payload.file && !payload.videoUrl) {
    throw new Error('请上传本地视频或输入视频 URL');
  }

  const formData = new FormData();
  if (payload.file) {
    formData.append('file', payload.file);
  }
  if (payload.videoUrl) {
    formData.append('videoUrl', payload.videoUrl);
  }
  if (payload.courseId !== undefined) {
    formData.append('courseId', String(payload.courseId));
  }
  if (payload.chapterId !== undefined) {
    formData.append('chapterId', String(payload.chapterId));
  }
  if (payload.sectionId !== undefined) {
    formData.append('sectionId', String(payload.sectionId));
  }

  return request<ApiResponse<UploadVideoResult | string>>('/api/course/upload/video', {
    method: 'POST',
    data: formData,
  });
}
export async function listCourses(params: CourseListQuery) {
  return request<ApiResponse<PageResult<CourseItem> | CourseItem[]>>('/api/course/list', {
    method: 'GET',
    params: removeUndefined(params),
  });
}

export async function getCourseDetail(params: CourseDetailQuery) {
  return request<ApiResponse<CourseItem>>('/api/course/detail', {
    method: 'GET',
    params,
  });
}

export async function reviewCourse(body: CourseReviewPayload) {
  return request<ApiResponse<boolean>>('/api/course/review', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
  });
}

export const resolveVideoUploadPath = (
  response?: ApiResponse<UploadVideoResult | string>,
): string | undefined => {
  const raw = response?.data;
  if (!raw) {
    return undefined;
  }
  if (typeof raw === 'string') {
    return raw;
  }
  return raw.localVideoPath || raw.videoUrl || raw.path || raw.url;
};

export const resolvePageResult = (
  response?: ApiResponse<PageResult<CourseItem> | CourseItem[]>,
): {
  records: CourseItem[];
  total: number;
  current: number;
  pageSize: number;
} => {
  const raw = response?.data;
  if (!raw) {
    return { records: [], total: 0, current: 1, pageSize: 10 };
  }

  if (Array.isArray(raw)) {
    return {
      records: raw,
      total: raw.length,
      current: 1,
      pageSize: raw.length || 10,
    };
  }

  const records = Array.isArray(raw.records) ? raw.records : [];
  return {
    records,
    total: raw.total ?? records.length,
    current: raw.current ?? 1,
    pageSize: raw.pageSize ?? 10,
  };
};
