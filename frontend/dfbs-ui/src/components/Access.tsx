import type { ReactNode } from 'react';
import { useAuthStore } from '@/stores/useAuthStore';

interface AccessProps {
  accessible: boolean;
  children?: ReactNode;
  fallback?: ReactNode;
}

/**
 * MVP: Hides children when accessible is false. Later can check permissions from store.
 */
export function Access({ accessible, children, fallback = null }: AccessProps) {
  if (!accessible) return <>{fallback}</>;
  return <>{children}</>;
}

export function useAccess(permission?: string): boolean {
  const { userInfo } = useAuthStore();
  if (!permission) return true;
  const perms = userInfo?.permissions ?? userInfo?.roles ?? [];
  return perms.includes(permission);
}
