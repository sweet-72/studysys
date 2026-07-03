import { defineStore } from 'pinia';
import { ref } from 'vue';
import { UserControllerService } from '../services';
import type { LoginUserVO } from '../services';

export const DEFAULT_USER_AVATAR = '/default.jpg';
const TOKEN_STORAGE_KEY = 'token';
const USER_INFO_STORAGE_KEY = 'userInfo';

type LoginPayload = LoginUserVO & Record<string, unknown>;

type LoginResult = {
  success: boolean;
  data?: LoginUserVO;
  message?: string;
};

const normalizeUserInfo = (user: LoginUserVO): LoginUserVO => {
  if (!user.userAvatar) {
    user.userAvatar = DEFAULT_USER_AVATAR;
  }
  return user;
};

const saveUserInfo = (user: LoginUserVO) => {
  localStorage.setItem(USER_INFO_STORAGE_KEY, JSON.stringify(user));
};

const removeStoredAuth = () => {
  localStorage.removeItem(USER_INFO_STORAGE_KEY);
  localStorage.removeItem(TOKEN_STORAGE_KEY);
};

const pickTokenValue = (source: Record<string, unknown> | null | undefined): string => {
  if (!source) {
    return '';
  }

  const directCandidates = [
    source.token,
    source.accessToken,
    source.jwtToken,
    source.authToken,
    source.authorization,
    source.Authorization,
  ];

  for (const item of directCandidates) {
    if (typeof item === 'string' && item.trim()) {
      return item.replace(/^Bearer\s+/i, '').trim();
    }
  }

  const nestedCandidates = [source.tokenInfo, source.auth, source.extra, source.meta];
  for (const item of nestedCandidates) {
    if (item && typeof item === 'object') {
      const nestedToken = pickTokenValue(item as Record<string, unknown>);
      if (nestedToken) {
        return nestedToken;
      }
    }
  }

  return '';
};

const pickTokenFromHeaders = (headers: Headers): string => {
  const candidates = [
    'authorization',
    'Authorization',
    'x-access-token',
    'X-Access-Token',
    'token',
    'Token',
  ];

  for (const name of candidates) {
    const value = headers.get(name);
    if (value?.trim()) {
      return value.replace(/^Bearer\s+/i, '').trim();
    }
  }

  return '';
};

const syncToken = (payload?: unknown, headers?: Headers) => {
  const tokenFromPayload =
    payload && typeof payload === 'object'
      ? pickTokenValue(payload as Record<string, unknown>)
      : '';
  const tokenFromHeaders = headers ? pickTokenFromHeaders(headers) : '';
  const token = tokenFromPayload || tokenFromHeaders;

  if (token) {
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
  }
};

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<LoginUserVO | null>(null);
  let restoreSessionPromise: Promise<boolean> | null = null;

  try {
    const storedUserInfo = localStorage.getItem(USER_INFO_STORAGE_KEY);
    if (storedUserInfo) {
      userInfo.value = JSON.parse(storedUserInfo);
    }
  } catch (error) {
    console.error('Failed to parse user info from localStorage', error);
  }

  const setCurrentUser = (user: LoginUserVO | null) => {
    userInfo.value = user;
    if (user) {
      saveUserInfo(user);
      return;
    }
    localStorage.removeItem(USER_INFO_STORAGE_KEY);
  };

  const restoreSession = async (): Promise<boolean> => {
    if (restoreSessionPromise) {
      return restoreSessionPromise;
    }

    restoreSessionPromise = (async () => {
      try {
        const response = await UserControllerService.getLoginUserUsingGet();

        if (response.code === 0 && response.data) {
          const normalizedUser = normalizeUserInfo(response.data);
          setCurrentUser(normalizedUser);
          return true;
        }

        setCurrentUser(null);
        removeStoredAuth();
        return false;
      } catch (error) {
        console.error('Failed to restore user session', error);
        setCurrentUser(null);
        removeStoredAuth();
        return false;
      } finally {
        restoreSessionPromise = null;
      }
    })();

    return restoreSessionPromise;
  };

  const fetchCurrentUser = async () => {
    try {
      const response = await UserControllerService.getLoginUserUsingGet();
      if (response.code === 0 && response.data) {
        const normalizedUser = normalizeUserInfo(response.data);
        setCurrentUser(normalizedUser);
        return normalizedUser;
      }
      return userInfo.value;
    } catch (error) {
      console.error('Failed to fetch current user', error);
      return userInfo.value;
    }
  };

  const login = async (userAccount: string, userPassword: string): Promise<LoginResult> => {
    try {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      const response = await UserControllerService.userLoginUsingPost({
        userAccount,
        userPassword,
      });

      if (response.code === 0 && response.data) {
        const normalizedUser = normalizeUserInfo(response.data);
        syncToken(response.data as LoginPayload);
        setCurrentUser(normalizedUser);
        return { success: true, data: normalizedUser };
      }

      removeStoredAuth();
      return { success: false, message: response.message || '登录失败' };
    } catch (error: unknown) {
      const errorMessage =
        error instanceof Error ? error.message : '登录失败，请检查网络连接';
      removeStoredAuth();
      return {
        success: false,
        message: errorMessage,
      };
    }
  };

  const loginByPhone = async (userPhone: string, userPassword: string): Promise<LoginResult> => {
    try {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      const response = await UserControllerService.userLoginByPhoneUsingPost({
        userPhone,
        userPassword,
      });

      if (response.code === 0 && response.data) {
        const normalizedUser = normalizeUserInfo(response.data);
        syncToken(response.data as LoginPayload);
        setCurrentUser(normalizedUser);
        return { success: true, data: normalizedUser };
      }

      removeStoredAuth();
      return { success: false, message: response.message || '登录失败' };
    } catch (error: unknown) {
      const errorMessage =
        error instanceof Error ? error.message : '登录失败，请检查网络连接';
      removeStoredAuth();
      return {
        success: false,
        message: errorMessage,
      };
    }
  };

  const logout = async () => {
    try {
      await UserControllerService.userLogoutUsingPost();
    } catch (error) {
      console.error('Logout API call failed', error);
    } finally {
      setCurrentUser(null);
      removeStoredAuth();
    }
  };

  const getUserAvatar = () => {
    return userInfo.value?.userAvatar || DEFAULT_USER_AVATAR;
  };

  return {
    userInfo,
    login,
    loginByPhone,
    logout,
    fetchCurrentUser,
    restoreSession,
    getUserAvatar,
    DEFAULT_USER_AVATAR,
  };
});
