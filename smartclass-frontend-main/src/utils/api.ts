const RAW_API_BASE_URL = String(import.meta.env.VITE_API_BASE_URL ?? '').trim();
const TOKEN_STORAGE_KEY = 'token';

const trimTrailingSlash = (value: string): string => value.replace(/\/+$/, '');

const normalizeApiBaseUrl = (value: string): string => {
  const base = trimTrailingSlash(value.trim());

  if (!base || base === '/api') {
    return '';
  }

  return base;
};

const normalizePath = (path: string): string => {
  if (!path) {
    return '';
  }

  return path.startsWith('/') ? path : `/${path}`;
};

const isAbsoluteHttpUrl = (value: string): boolean => /^https?:\/\//i.test(value);

export const getApiBaseUrl = (): string => normalizeApiBaseUrl(RAW_API_BASE_URL);

export const getStoredToken = (): string => {
  if (typeof localStorage === 'undefined') {
    return '';
  }

  return localStorage.getItem(TOKEN_STORAGE_KEY)?.trim() || '';
};

export const buildApiUrl = (path: string): string => {
  const normalizedPath = normalizePath(path);
  const base = getApiBaseUrl();

  if (!base) {
    return normalizedPath;
  }

  if (base.endsWith('/api') && normalizedPath === '/api') {
    return base;
  }

  if (base.endsWith('/api') && normalizedPath.startsWith('/api/')) {
    return `${base}${normalizedPath.slice('/api'.length)}`;
  }

  return `${base}${normalizedPath}`;
};

export const buildApiHeaders = (
  headers: Record<string, string> = {},
): Record<string, string> => {
  const token = getStoredToken();

  if (!token) {
    return { ...headers };
  }

  return {
    ...headers,
    Authorization: `Bearer ${token}`,
  };
};

export const getBrowserOriginBase = (): string => {
  const base = getApiBaseUrl();
  return isAbsoluteHttpUrl(base) ? base : window.location.origin;
};
