import axios, { type AxiosError } from 'axios';
import { message } from 'antd';
import { clearPermAllowedCache } from '@/shared/permAllowedCache';
import { clearEffectiveKeysCache } from '@/shared/effectiveKeysCache';

const AUTH_TOKEN_KEY = 'dfbs_token';
const AUTH_USER_ID_KEY = 'dfbs_user_id';
const AUTH_USER_INFO_KEY = 'dfbs_user_info';

export function getStoredToken(): string | null {
  return localStorage.getItem(AUTH_TOKEN_KEY);
}

export function setStoredToken(token: string): void {
  localStorage.setItem(AUTH_TOKEN_KEY, token);
}

export function getStoredUserId(): string | null {
  return localStorage.getItem(AUTH_USER_ID_KEY);
}

export function setStoredUserId(userId: number | undefined | null): void {
  if (userId != null) {
    localStorage.setItem(AUTH_USER_ID_KEY, String(userId));
  } else {
    localStorage.removeItem(AUTH_USER_ID_KEY);
  }
}

export interface StoredUserInfo {
  id?: number;
  username?: string;
  roles?: string[];
}

export function getStoredUserInfo(): StoredUserInfo | null {
  try {
    const raw = localStorage.getItem(AUTH_USER_INFO_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as StoredUserInfo;
    return parsed && typeof parsed === 'object' ? parsed : null;
  } catch {
    return null;
  }
}

export function setStoredUserInfo(info: StoredUserInfo | null | undefined): void {
  if (info != null && typeof info === 'object') {
    localStorage.setItem(AUTH_USER_INFO_KEY, JSON.stringify({ id: info.id, username: info.username, roles: info.roles }));
  } else {
    localStorage.removeItem(AUTH_USER_INFO_KEY);
  }
}

export function clearStoredToken(): void {
  localStorage.removeItem(AUTH_TOKEN_KEY);
  localStorage.removeItem(AUTH_USER_ID_KEY);
  localStorage.removeItem(AUTH_USER_INFO_KEY);
  clearPermAllowedCache();
  clearEffectiveKeysCache();
}

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
});

request.interceptors.request.use((config) => {
  const token = getStoredToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  const userId = getStoredUserId();
  if (userId) {
    config.headers['X-User-Id'] = userId;
  }
  return config;
});

request.interceptors.response.use(
  (res) => res,
  (err: AxiosError<{ message?: string; machineCode?: string }>) => {
    if (err.response?.status === 401) {
      clearStoredToken();
      window.location.href = '/login';
      return Promise.reject(err);
    }
    // 409/400: let caller show custom message (e.g. customers, dictionary-types, dictionary-items)
    if (err.response?.status === 409 || err.response?.status === 400) {
      const url = err.config?.url ?? '';
      const method = err.config?.method?.toLowerCase();
      if (url.includes('customers') && method === 'post') {
        return Promise.reject(err);
      }
      if (url.includes('dictionary-types') && (method === 'post' || method === 'put')) {
        return Promise.reject(err);
      }
      if (url.includes('/items') && url.includes('dictionary-types') && (method === 'post' || method === 'patch')) {
        return Promise.reject(err);
      }
      if (url.includes('dictionary-items') && (method === 'put' || method === 'patch' || method === 'delete')) {
        return Promise.reject(err);
      }
      if (url.includes('dictionary-types') && method === 'delete') {
        return Promise.reject(err);
      }
    }
    // Read-only dictionaries API: let caller show Chinese message (字典类型不存在 / 加载失败，请重试)
    const url = err.config?.url ?? '';
    if (url.includes('/v1/dictionaries/') && !url.includes('/admin/')) {
      return Promise.reject(err);
    }
    const msg = err.response?.data?.message ?? err.message ?? '请求失败';
    message.error(msg);
    return Promise.reject(err);
  }
);

export default request;
