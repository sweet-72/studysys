export const AUTH_TOKEN_KEY = 'token';

const TOKEN_CANDIDATE_KEYS = [
  'token',
  'accessToken',
  'jwtToken',
  'bearerToken',
  'authorization',
  'Authorization',
  'x-access-token',
  'X-Access-Token',
];

const normalizeToken = (token?: string) => {
  if (!token) {
    return undefined;
  }

  return token.replace(/^Bearer\s+/i, '').trim() || undefined;
};

const readTokenFromHeaders = (headers: any): string | undefined => {
  if (!headers || typeof headers !== 'object') {
    return undefined;
  }

  for (const key of TOKEN_CANDIDATE_KEYS) {
    const headerValue = headers[key] || headers[String(key).toLowerCase()];
    const token = normalizeToken(typeof headerValue === 'string' ? headerValue : undefined);
    if (token) {
      return token;
    }
  }

  return undefined;
};

const readTokenFromObject = (value: any, depth = 0): string | undefined => {
  if (!value || typeof value !== 'object' || depth > 3) {
    return undefined;
  }

  for (const key of TOKEN_CANDIDATE_KEYS) {
    const token = normalizeToken(value[key]);
    if (token) {
      return token;
    }
  }

  for (const [key, fieldValue] of Object.entries(value)) {
    if (/token|authorization/i.test(key)) {
      const token = normalizeToken(typeof fieldValue === 'string' ? fieldValue : undefined);
      if (token) {
        return token;
      }
    }

    if (fieldValue && typeof fieldValue === 'object') {
      const nestedToken = readTokenFromObject(fieldValue, depth + 1);
      if (nestedToken) {
        return nestedToken;
      }
    }
  }

  return undefined;
};

export const getAuthToken = () => {
  if (typeof window === 'undefined') {
    return undefined;
  }

  return normalizeToken(window.localStorage.getItem(AUTH_TOKEN_KEY) || undefined);
};

export const saveAuthToken = (token?: string) => {
  if (typeof window === 'undefined') {
    return;
  }

  const normalizedToken = normalizeToken(token);
  if (!normalizedToken) {
    return;
  }

  window.localStorage.setItem(AUTH_TOKEN_KEY, normalizedToken);
};

export const clearAuthToken = () => {
  if (typeof window === 'undefined') {
    return;
  }

  window.localStorage.removeItem(AUTH_TOKEN_KEY);
};

export const extractTokenFromLoginResponse = (response: any): string | undefined => {
  return (
    readTokenFromHeaders(response?.headers) ||
    readTokenFromObject(response) ||
    readTokenFromObject(response?.data)
  );
};
