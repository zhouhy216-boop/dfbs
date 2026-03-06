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
import { clearEffectiveKeysCache } from '@/shared/hooks/useEffectivePermissions';
import { useVisionStore } from '@/shared/stores/useVisionStore';

export interface UserInfo {
  id?: number;
  username?: string;
  roles?: string[];
  permissions?: string[];
}

interface AuthState {
  token: string | null;
  userInfo: UserInfo | null;
  /** True only after login() or setUserInfoFromMe(); false after load from storage until /auth/me succeeds. Used to avoid showing role-dependent UI (e.g. simulator) on stale storage. */
  userInfoRefreshedFromServer: boolean;
  login: (token: string, user?: UserInfo) => void;
  logout: () => void;
  hydrateFromStorage: () => void;
  setUserInfoFromMe: (user: UserInfo | null) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: getStoredToken(),
  userInfo: getStoredUserInfo(),
  userInfoRefreshedFromServer: false,
  login: (token, user) => {
    clearPermAllowedCache();
    clearEffectiveKeysCache();
    setStoredToken(token);
    setStoredUserId(user?.id);
    setStoredUserInfo(user ?? null);
    set({ token, userInfo: user ?? null, userInfoRefreshedFromServer: true });
  },
  logout: () => {
    useVisionStore.getState().clearVision();
    clearStoredToken();
    set({ token: null, userInfo: null, userInfoRefreshedFromServer: false });
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
  setUserInfoFromMe: (user) => {
    if (user != null) {
      setStoredUserInfo(user);
      set({ userInfo: user, userInfoRefreshedFromServer: true });
    } else {
      set({ userInfo: null, userInfoRefreshedFromServer: false });
    }
  },
}));
