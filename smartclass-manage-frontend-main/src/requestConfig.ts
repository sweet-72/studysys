import { BACKEND_HOST_LOCAL, BACKEND_HOST_PROD } from '@/constants';
import { clearAuthToken, getAuthToken } from '@/utils/auth';
import type { RequestOptions } from '@@/plugin-request/request';
import type { RequestConfig } from '@umijs/max';

interface ResponseStructure {
  code?: number;
  data?: any;
  message?: string;
}

const isDev = process.env.NODE_ENV === 'development';
const loginPath = '/user/login';

const redirectToLogin = () => {
  clearAuthToken();
  if (window.location.pathname.includes(loginPath)) {
    return;
  }
  window.location.href = `${loginPath}?redirect=${encodeURIComponent(window.location.href)}`;
};

const isUnauthorizedResponse = (status?: number, code?: number) => {
  return status === 401 || code === 40100;
};

export const requestConfig: RequestConfig = {
  baseURL: isDev ? BACKEND_HOST_LOCAL : BACKEND_HOST_PROD,
  withCredentials: true,
  requestInterceptors: [
    (config: RequestOptions) => {
      const token = getAuthToken();
      config.headers = config.headers || {};
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
  ],
  responseInterceptors: [
    (response: any) => {
      const requestPath: string = response?.config?.url ?? '';
      const payload = response?.data as ResponseStructure | undefined;
      const status = Number(response?.status);

      if (isUnauthorizedResponse(status, Number(payload?.code))) {
        const isLoginCheck = requestPath.includes('user/get/login');
        const isLoginPage = window.location.pathname.includes(loginPath);
        if (!isLoginCheck && !isLoginPage) {
          redirectToLogin();
          throw new Error('请先登录');
        }
      }

      if (!payload) {
        throw new Error('服务异常，请稍后重试');
      }

      if (Number(payload.code) !== 0) {
        throw new Error(payload.message || '服务器错误');
      }

      return response;
    },
  ],
  errorConfig: {
    errorHandler: (error: any) => {
      const status = Number(error?.response?.status);
      const code = Number(error?.response?.data?.code);
      if (isUnauthorizedResponse(status, code)) {
        redirectToLogin();
        throw new Error('请先登录');
      }
      throw error;
    },
  },
};
