import { create } from 'zustand';
import { getStoredToken, setStoredToken, clearStoredToken } from '@/shared/utils/request';

export interface UserInfo {
  id?: number;
  username?: string;
  roles?: string[];
  permissions?: string[];
}

interface AuthState {
  token: string | null;
  userInfo: UserInfo | null;
  login: (token: string, user?: UserInfo) => void;
  logout: () => void;
  hydrateFromStorage: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: getStoredToken(),
  userInfo: null,
  login: (token, user) => {
    setStoredToken(token);
    set({ token, userInfo: user ?? null });
  },
  logout: () => {
    clearStoredToken();
    set({ token: null, userInfo: null });
  },
  hydrateFromStorage: () => {
    const t = getStoredToken();
    if (t) set((s) => (s.token ? s : { ...s, token: t }));
  },
}));
