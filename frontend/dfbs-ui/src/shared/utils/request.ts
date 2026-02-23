import axios, { type AxiosError } from 'axios';
import { message } from 'antd';

const AUTH_TOKEN_KEY = 'dfbs_token';

export function getStoredToken(): string | null {
  return localStorage.getItem(AUTH_TOKEN_KEY);
}

export function setStoredToken(token: string): void {
  localStorage.setItem(AUTH_TOKEN_KEY, token);
}

export function clearStoredToken(): void {
  localStorage.removeItem(AUTH_TOKEN_KEY);
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
