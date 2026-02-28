import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/shared/stores/useAuthStore';

/** Role strings: backend AuthController returns roles with ROLE_ prefix stripped (ADMIN, SUPER_ADMIN). Align with SuperAdminGuard. */
const ADMIN_ROLE = 'ADMIN';
const SUPER_ADMIN_ROLE = 'SUPER_ADMIN';

export function useIsAdminOrSuperAdmin(): boolean {
  const roles = useAuthStore((s) => s.userInfo?.roles);
  if (!Array.isArray(roles)) return false;
  return roles.includes(ADMIN_ROLE) || roles.includes(SUPER_ADMIN_ROLE);
}

/** Renders children only when user has ROLE_ADMIN or SUPER_ADMIN; otherwise redirects to /dashboard. Deny-by-default until userInfo is present. */
export function AdminOrSuperAdminGuard({ children }: { children: React.ReactNode }) {
  const userInfo = useAuthStore((s) => s.userInfo);
  const isAllowed = useIsAdminOrSuperAdmin();
  const location = useLocation();

  if (!userInfo) {
    return <div style={{ padding: 24 }}>加载中...</div>;
  }
  if (!isAllowed) {
    return <Navigate to="/dashboard" state={{ from: location.pathname, reason: 'forbidden' }} replace />;
  }
  return <>{children}</>;
}
