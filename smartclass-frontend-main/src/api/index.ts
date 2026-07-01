import axios, {
  AxiosInstance,
  InternalAxiosRequestConfig,
  AxiosResponse,
  AxiosError,
} from 'axios';
import { getApiBaseUrl } from '@/utils/api';

const apiClient: AxiosInstance = axios.create({
  baseURL: getApiBaseUrl() || undefined,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  },
);

const clearLoginState = () => {
  localStorage.removeItem('userInfo');
  localStorage.removeItem('token');
  window.location.href = '/login';
};

apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    if (response.data && response.data.code === 40100) {
      console.log('检测到未登录状态（40100），正在清除登录状态并跳转到登录页');
      clearLoginState();
    }
    return response;
  },
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      console.log('检测到未授权状态（401），正在清除登录状态并跳转到登录页');
      clearLoginState();
    }
    return Promise.reject(error);
  },
);

export default apiClient;