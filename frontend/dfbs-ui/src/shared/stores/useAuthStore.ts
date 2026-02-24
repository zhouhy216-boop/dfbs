import { create } from 'zustand';
import {
  getStoredToken,
  setStoredToken,
  clearStoredToken,
  setStoredUserId,
  getStoredUserInfo,
  setStoredUserInfo,
} from '@/shared/utils/request';
import { clearPermAllowedCache } from '@/shared/permAllowedCache';

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
  userInfo: getStoredUserInfo(),
  login: (token, user) => {
    clearPermAllowedCache();
    setStoredToken(token);
    setStoredUserId(user?.id);
    setStoredUserInfo(user ?? null);
    set({ token, userInfo: user ?? null });
  },
  logout: () => {
    clearStoredToken();
    set({ token: null, userInfo: null });
  },
  hydrateFromStorage: () => {
    const t = getStoredToken();
    const stored = getStoredUserInfo();
    if (t) {
      set((s) => {
        const next = { ...s, token: s.token || t };
        if (stored && !s.userInfo) next.userInfo = stored;
        return next;
      });
    }
  },
}));
