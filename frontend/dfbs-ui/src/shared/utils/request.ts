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
    // 409/400 on customer create: caller shows "Client name exists"
    if (err.response?.status === 409 || err.response?.status === 400) {
      const url = err.config?.url ?? '';
      if (url.includes('customers') && err.config?.method?.toLowerCase() === 'post') {
        return Promise.reject(err);
      }
    }
    const msg = err.response?.data?.message ?? err.message ?? '请求失败';
    message.error(msg);
    return Promise.reject(err);
  }
);

export default request;
