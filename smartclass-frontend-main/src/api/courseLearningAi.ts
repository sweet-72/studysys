import type { ChatMessageVO } from '@/services';
import { buildApiUrl } from '@/utils/api';

export interface BaseResponse<T> {
  code?: number;
  data?: T;
  message?: string;
}

export interface CourseLearningAiRequest {
  aiAvatarId: number;
  sessionId?: string;
  courseId: number;
  chapterId?: number;
  sectionId?: number;
  question: string;
  studentGoal?: string;
  endChat?: boolean;
}

export interface CourseLearningAiStreamMessage {
  event: 'message' | 'complete' | 'error';
  data: unknown;
  raw: string;
}

export interface CourseLearningAiStreamHandlers {
  onMessage?: (payload: {
    chunk: string;
    sessionId?: string;
    raw: unknown;
  }) => void;
  onComplete?: (payload: {
    sessionId?: string;
    raw: unknown;
  }) => void;
  onError?: (payload: {
    message: string;
    raw: unknown;
  }) => void;
}

export interface CourseLearningAiStreamOptions {
  signal?: AbortSignal;
}

const LOGIN_PATH = '/login';

const getToken = () => localStorage.getItem('token')?.trim() || '';

const getRedirectUrl = () =>
  `${window.location.pathname}${window.location.search}${window.location.hash}`;

const redirectToLogin = () => {
  const redirect = encodeURIComponent(getRedirectUrl());
  window.location.href = `${LOGIN_PATH}?redirect=${redirect}`;
};

export const clearAuthAndRedirect = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('userInfo');
  redirectToLogin();
};

const buildHeaders = (contentType = 'application/json'): Record<string, string> => {
  const token = getToken();
  const headers: Record<string, string> = {
    Accept: 'application/json',
  };

  if (contentType) {
    headers['Content-Type'] = contentType;
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return headers;
};

const handleUnauthorized = (): never => {
  clearAuthAndRedirect();
  throw new Error('登录已过期，请重新登录');
};

const unwrapResponse = async <T>(response: Response): Promise<BaseResponse<T>> => {
  if (response.status === 401) {
    return handleUnauthorized();
  }

  const contentType = response.headers.get('content-type') || '';
  if (!contentType.includes('application/json')) {
    if (!response.ok) {
      throw new Error(`请求失败：${response.status}`);
    }
    throw new Error('接口返回格式异常');
  }

  const payload = (await response.json()) as BaseResponse<T>;

  if (payload?.code === 401 || payload?.code === 40100) {
    return handleUnauthorized();
  }

  if (!response.ok) {
    throw new Error(payload?.message || `请求失败：${response.status}`);
  }

  if (payload?.code !== 0) {
    throw new Error(payload?.message || '接口调用失败');
  }

  return payload;
};

const buildSessionStorageKey = ({
  courseId,
  chapterId,
  sectionId,
  aiAvatarId,
}: Pick<CourseLearningAiRequest, 'courseId' | 'chapterId' | 'sectionId' | 'aiAvatarId'>) => {
  return [
    'course-learning-ai',
    String(courseId || 0),
    String(chapterId || 0),
    String(sectionId || 0),
    String(aiAvatarId || 0),
  ].join(':');
};

export const getCourseLearningAiSessionKey = buildSessionStorageKey;

export const getCachedCourseLearningAiSessionId = (
  keyParams: Pick<CourseLearningAiRequest, 'courseId' | 'chapterId' | 'sectionId' | 'aiAvatarId'>,
) => {
  const key = buildSessionStorageKey(keyParams);
  return sessionStorage.getItem(key) || '';
};

export const cacheCourseLearningAiSessionId = (
  keyParams: Pick<CourseLearningAiRequest, 'courseId' | 'chapterId' | 'sectionId' | 'aiAvatarId'>,
  sessionId?: string,
) => {
  const key = buildSessionStorageKey(keyParams);

  if (!sessionId) {
    sessionStorage.removeItem(key);
    return;
  }

  sessionStorage.setItem(key, sessionId);
};

export const clearCourseLearningAiSessionId = (
  keyParams: Pick<CourseLearningAiRequest, 'courseId' | 'chapterId' | 'sectionId' | 'aiAvatarId'>,
) => {
  const key = buildSessionStorageKey(keyParams);
  sessionStorage.removeItem(key);
};

export const askCourseLearningAi = async (
  request: CourseLearningAiRequest,
): Promise<ChatMessageVO> => {
  const response = await fetch(buildApiUrl('/api/course/ai/course-assistant/ask'), {
    method: 'POST',
    credentials: 'include',
    headers: buildHeaders(),
    body: JSON.stringify(request),
  });

  const payload = await unwrapResponse<ChatMessageVO>(response);
  const sessionId = payload.data?.sessionId;

  if (sessionId) {
    cacheCourseLearningAiSessionId(request, sessionId);
  }

  return payload.data || {};
};

const tryParseJson = (value: string): unknown => {
  try {
    return JSON.parse(value);
  } catch {
    return value;
  }
};

const getPayloadSessionId = (payload: unknown): string | undefined => {
  if (!payload || typeof payload !== 'object') {
    return undefined;
  }

  const raw = payload as Record<string, unknown>;
  const candidates = [raw.sessionId, raw.conversationId, raw.conversation_id];

  for (const item of candidates) {
    if (typeof item === 'string' && item.trim()) {
      return item.trim();
    }
  }

  return undefined;
};

const getPayloadErrorMessage = (payload: unknown): string => {
  if (!payload || typeof payload !== 'object') {
    return typeof payload === 'string' ? payload : '学习助手请求失败';
  }

  const raw = payload as Record<string, unknown>;
  const candidates = [raw.message, raw.error, raw.msg];

  for (const item of candidates) {
    if (typeof item === 'string' && item.trim()) {
      return item.trim();
    }
  }

  return '学习助手请求失败';
};

const extractMessageChunk = (payload: unknown): string => {
  if (typeof payload === 'string') {
    return payload;
  }

  if (!payload || typeof payload !== 'object') {
    return '';
  }

  const raw = payload as Record<string, unknown>;
  const directCandidates = [raw.chunk, raw.content, raw.answer, raw.message, raw.data];

  for (const item of directCandidates) {
    if (typeof item === 'string') {
      return item;
    }
  }

  const nestedData = raw.data;
  if (nestedData && typeof nestedData === 'object') {
    const nested = nestedData as Record<string, unknown>;
    const nestedCandidates = [nested.chunk, nested.content, nested.answer, nested.message];

    for (const item of nestedCandidates) {
      if (typeof item === 'string') {
        return item;
      }
    }
  }

  return '';
};

const parseSseEventBlock = (block: string): CourseLearningAiStreamMessage | null => {
  const lines = block
    .split(/\r?\n/)
    .map((line) => line.trimEnd())
    .filter(Boolean);

  if (lines.length === 0) {
    return null;
  }

  let eventName = 'message';
  const dataLines: string[] = [];

  for (const line of lines) {
    if (line.startsWith(':')) {
      continue;
    }

    if (line.startsWith('event:')) {
      eventName = line.slice(6).trim() || 'message';
      continue;
    }

    if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trim());
    }
  }

  const rawData = dataLines.join('\n').trim();
  if (!rawData) {
    return null;
  }

  const parsed = tryParseJson(rawData);
  if (!block.includes('event:') && parsed && typeof parsed === 'object') {
    const eventFromPayload = (parsed as Record<string, unknown>).event;
    if (typeof eventFromPayload === 'string' && eventFromPayload.trim()) {
      eventName = eventFromPayload.trim();
    }
  }

  if (rawData === '[DONE]') {
    eventName = 'complete';
  }

  if (eventName !== 'message' && eventName !== 'complete' && eventName !== 'error') {
    eventName = 'message';
  }

  return {
    event: eventName,
    data: parsed,
    raw: rawData,
  };
};

export const streamCourseLearningAi = async (
  request: CourseLearningAiRequest,
  handlers: CourseLearningAiStreamHandlers = {},
  options: CourseLearningAiStreamOptions = {},
): Promise<void> => {
  const response = await fetch(buildApiUrl('/api/course/ai/course-assistant/stream'), {
    method: 'POST',
    credentials: 'include',
    headers: {
      ...buildHeaders(),
      Accept: 'text/event-stream',
    },
    body: JSON.stringify(request),
    signal: options.signal,
  });

  if (response.status === 401) {
    return handleUnauthorized();
  }

  if (!response.ok) {
    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) {
      const payload = await unwrapResponse<unknown>(response);
      throw new Error(payload.message || '流式请求失败');
    }
    throw new Error(`流式请求失败：${response.status}`);
  }

  if (!response.body) {
    throw new Error('浏览器不支持流式响应');
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder('utf-8');
  let buffer = '';

  try {
    while (true) {
      const { value, done } = await reader.read();

      if (done) {
        break;
      }

      buffer += decoder.decode(value, { stream: true });
      const blocks = buffer.split(/\r?\n\r?\n/);
      buffer = blocks.pop() || '';

      for (const block of blocks) {
        const parsed = parseSseEventBlock(block);
        if (!parsed) {
          continue;
        }

        const sessionId = getPayloadSessionId(parsed.data);
        if (sessionId) {
          cacheCourseLearningAiSessionId(request, sessionId);
        }

        if (parsed.event === 'message') {
          const chunk = extractMessageChunk(parsed.data);
          if (chunk) {
            handlers.onMessage?.({
              chunk,
              sessionId,
              raw: parsed.data,
            });
          }
          continue;
        }

        if (parsed.event === 'complete') {
          handlers.onComplete?.({
            sessionId,
            raw: parsed.data,
          });
          continue;
        }

        if (parsed.event === 'error') {
          const message = getPayloadErrorMessage(parsed.data);
          handlers.onError?.({
            message,
            raw: parsed.data,
          });
          throw new Error(message);
        }
      }
    }

    if (buffer.trim()) {
      const parsed = parseSseEventBlock(buffer);
      if (parsed?.event === 'complete') {
        handlers.onComplete?.({
          sessionId: getPayloadSessionId(parsed.data),
          raw: parsed.data,
        });
      }
    }
  } finally {
    reader.releaseLock();
  }
};
