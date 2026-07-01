import type { AxiosResponse } from 'axios';
import apiClient from './index';

export interface BaseResponse<T> {
  code: number;
  data: T;
  message: string;
}

export interface CourseListParams {
  current: number;
  pageSize: number;
  title?: string;
  courseType?: string;
  difficulty?: string;
  sortField?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface CourseItem {
  id: number;
  title: string;
  subtitle?: string;
  description?: string;
  coverUrl?: string;
  coverImage?: string;
  categoryId?: number;
  categoryName?: string;
  courseType?: string | number;
  difficulty?: string | number;
  teacherName?: string;
  rating?: number;
  ratingScore?: number;
  buyCount?: number;
  studyCount?: number;
  studentCount?: number;
  totalDuration?: number;
  viewCount?: number;
  hotScore?: number;
  createTime?: string;
  tags?: string;
}

export interface RecommendCourseParams {
  limit?: number;
  categoryId?: number;
  difficulty?: string | number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  pageSize: number;
}

export type LearnStatus = 'NOT_STARTED' | 'LEARNING' | 'COMPLETED';

export interface CourseSection {
  id: number;
  title: string;
  videoUrl?: string;
  learnStatus?: LearnStatus;
  isLearned?: boolean;
  isLearning?: boolean;
  sort?: number;
}

export interface CourseChapter {
  id: number;
  title: string;
  sort?: number;
  sections: CourseSection[];
}

export interface CourseDetail {
  id: number;
  title: string;
  description?: string;
  content?: string;
  coverUrl?: string;
  teacherName?: string;
  teacherAvatar?: string;
  teacherIntro?: string;
  teacherTitle?: string;
  chapters: CourseChapter[];
}

export interface CourseQuestion {
  id: number;
  title: string;
  questionType: string;
  options?: Array<{
    label: string;
    value: string;
  }>;
}

export interface SubmitQuestionPayload {
  questionId: number;
  sectionId: number;
  answer: string | string[];
}

export interface SubmitQuestionResult {
  correct: boolean;
  message?: string;
  score?: number;
  rightAnswer?: string | string[];
}

export interface HomeworkSubmitPayload {
  sectionId: number;
  questionId: number;
  answer: string;
}

export interface HomeworkSubmitResult {
  correct?: boolean;
  message?: string;
  score?: number;
  data?: unknown;
  [key: string]: unknown;
}

interface HomeworkExerciseRaw {
  id?: number | string;
  questionId?: number | string;
  title?: string;
  questionTitle?: string;
  content?: string;
  questionContent?: string;
  questionType?: string;
  type?: string;
  options?: unknown;
  optionList?: unknown;
  choices?: unknown;
  [key: string]: unknown;
}

export interface LearningRecordQueryParams {
  userId?: number;
  courseId?: number;
}

export interface LearningRecordPayload {
  userId?: number;
  courseId: number;
  chapterId?: number;
  sectionId: number;
  progress?: number;
  videoProgress?: number;
  historySectionIds?: number[];
  wrongCount?: number;
  updatedAt?: string;
}

export interface LearningRecordVO {
  userId?: number;
  courseId?: number;
  chapterId?: number;
  sectionId?: number;
  progress?: number;
  videoProgress?: number;
  historySectionIds?: number[];
  wrongCount?: number;
  updatedAt?: string;
  [key: string]: unknown;
}

const unwrap = <T>(response: AxiosResponse<BaseResponse<T>>): T => {
  const payload = response.data;
  if (payload?.code === 0) {
    return payload.data;
  }
  throw new Error(payload?.message || '课程接口调用失败');
};

const toValidPositiveNumber = (value: unknown): number | undefined => {
  const num =
    typeof value === 'string'
      ? Number.parseInt(value, 10)
      : typeof value === 'number'
        ? value
        : Number(value);
  return Number.isFinite(num) && num > 0 ? num : undefined;
};

const normalizeHomeworkOptions = (optionsRaw: unknown): Array<{ label: string; value: string }> | undefined => {
  if (!Array.isArray(optionsRaw)) {
    return undefined;
  }

  const options = optionsRaw
    .map((optionItem, optionIndex) => {
      if (typeof optionItem === 'string') {
        const label = String.fromCharCode(65 + optionIndex);
        return { label, value: optionItem };
      }

      if (optionItem && typeof optionItem === 'object') {
        const optionObj = optionItem as Record<string, unknown>;
        const labelRaw = optionObj.label ?? optionObj.key ?? optionObj.code;
        const valueRaw = optionObj.value ?? optionObj.content ?? optionObj.text ?? optionObj.label;
        if (valueRaw === undefined || valueRaw === null) {
          return null;
        }

        const label =
          labelRaw === undefined || labelRaw === null
            ? String.fromCharCode(65 + optionIndex)
            : String(labelRaw);
        return { label, value: String(valueRaw) };
      }

      return null;
    })
    .filter((option): option is { label: string; value: string } => option !== null);

  return options.length > 0 ? options : undefined;
};

const normalizeHomeworkExercise = (raw: HomeworkExerciseRaw | HomeworkExerciseRaw[], sectionId: number): CourseQuestion[] => {
  const list = Array.isArray(raw) ? raw : raw ? [raw] : [];

  return list.map((item, index) => {
    const questionIdRaw = item.questionId ?? item.id;
    const parsedQuestionId =
      typeof questionIdRaw === 'string' ? Number.parseInt(questionIdRaw, 10) : Number(questionIdRaw);
    const fallbackId = sectionId * 1000 + index + 1;
    const id = Number.isFinite(parsedQuestionId) && parsedQuestionId > 0 ? parsedQuestionId : fallbackId;

    const options = normalizeHomeworkOptions(item.options ?? item.optionList ?? item.choices);
    const title = String(item.title ?? item.questionTitle ?? item.content ?? item.questionContent ?? '').trim();
    const questionType = String(item.questionType ?? item.type ?? (options ? 'SINGLE_CHOICE' : 'TEXT'));

    return {
      id,
      title: title || `第 ${index + 1} 题`,
      questionType,
      options,
    };
  });
};

export const queryCourseList = async (params: CourseListParams): Promise<PageResult<CourseItem>> => {
  const response = await apiClient.get<BaseResponse<PageResult<CourseItem>>>('/api/course/list', {
    params,
  });
  return unwrap(response);
};

export const queryCourseDetail = async (courseId: number): Promise<CourseDetail> => {
  const response = await apiClient.get<BaseResponse<CourseDetail>>('/api/course/detail', {
    params: { course_id: courseId },
  });
  return unwrap(response);
};

export const startCourseSection = async (courseId: number, sectionId: number): Promise<boolean> => {
  const response = await apiClient.post<BaseResponse<boolean>>('/api/course/learn/start', {
    courseId,
    sectionId,
  });
  return unwrap(response);
};

export const completeCourseSection = async (courseId: number, sectionId: number): Promise<boolean> => {
  const response = await apiClient.post<BaseResponse<boolean>>('/api/course/learn/complete', {
    courseId,
    sectionId,
  });
  return unwrap(response);
};

export const querySectionQuestions = async (sectionId: number): Promise<CourseQuestion[]> => {
  const response = await apiClient.get<BaseResponse<HomeworkExerciseRaw | HomeworkExerciseRaw[]>>(
    `/api/homework/exercise/${sectionId}`,
  );
  const raw = unwrap(response);
  return normalizeHomeworkExercise(raw, sectionId);
};

export const submitCourseQuestion = async (
  payload: SubmitQuestionPayload,
): Promise<SubmitQuestionResult> => {
  const response = await apiClient.post<BaseResponse<SubmitQuestionResult>>(
    '/api/course/question/submit',
    payload,
  );
  return unwrap(response);
};

export const queryRecommendCourses = async (
  paramsOrLimit: number | RecommendCourseParams = 6,
  categoryId?: number,
): Promise<CourseItem[]> => {
  const params =
    typeof paramsOrLimit === 'number'
      ? { limit: paramsOrLimit, categoryId }
      : paramsOrLimit;

  const response = await apiClient.get<BaseResponse<CourseItem[]>>('/api/course/recommend', {
    params,
  });
  return unwrap(response);
};

export const queryHotRecommendCourses = async (limit = 6, categoryId?: number): Promise<CourseItem[]> => {
  const response = await apiClient.get<BaseResponse<CourseItem[]>>('/api/course/recommend/hot', {
    params: { limit, categoryId },
  });
  return unwrap(response);
};

export const recordCourseView = async (courseId: number): Promise<boolean> => {
  const response = await apiClient.post<BaseResponse<boolean>>(`/api/course/${courseId}/view`);
  return unwrap(response);
};

export const submitHomework = async (
  payload: HomeworkSubmitPayload,
): Promise<HomeworkSubmitResult> => {
  const response = await apiClient.post<BaseResponse<HomeworkSubmitResult>>(
    '/api/homework/submit',
    payload,
  );
  return unwrap(response);
};

export const queryLearningRecord = async (
  params: LearningRecordQueryParams,
): Promise<LearningRecordVO | LearningRecordVO[] | null> => {
  const queryParams: Record<string, number> = {};
  const userId = toValidPositiveNumber(params.userId);
  const courseId = toValidPositiveNumber(params.courseId);
  if (userId !== undefined) {
    queryParams.userId = userId;
  }
  if (courseId !== undefined) {
    queryParams.courseId = courseId;
  }

  const response = await apiClient.get<BaseResponse<LearningRecordVO | LearningRecordVO[] | null>>(
    '/api/learning/record',
    { params: queryParams },
  );
  return unwrap(response);
};

export const saveLearningRecord = async (payload: LearningRecordPayload): Promise<boolean> => {
  const body: Record<string, unknown> = {
    courseId: payload.courseId,
    sectionId: payload.sectionId,
  };

  const userId = toValidPositiveNumber(payload.userId);
  const chapterId = toValidPositiveNumber(payload.chapterId);
  if (userId !== undefined) {
    body.userId = userId;
  }
  if (chapterId !== undefined) {
    body.chapterId = chapterId;
  }
  if (payload.progress !== undefined) {
    body.progress = payload.progress;
  }
  if (payload.videoProgress !== undefined) {
    body.videoProgress = payload.videoProgress;
  }
  if (payload.wrongCount !== undefined) {
    body.wrongCount = payload.wrongCount;
  }
  if (payload.updatedAt !== undefined) {
    body.updatedAt = payload.updatedAt;
  }
  if (Array.isArray(payload.historySectionIds)) {
    body.historySectionIds = payload.historySectionIds.filter((id) => toValidPositiveNumber(id) !== undefined);
  }

  const response = await apiClient.post<BaseResponse<boolean>>('/api/learning/record', body);
  return unwrap(response);
};

