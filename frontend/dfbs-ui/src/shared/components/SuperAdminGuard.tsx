import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/shared/stores/useAuthStore';

const SUPER_ADMIN_ROLE = 'SUPER_ADMIN';

export function useIsSuperAdmin(): boolean {
  const roles = useAuthStore((s) => s.userInfo?.roles);
  return Array.isArray(roles) && roles.includes(SUPER_ADMIN_ROLE);
}

/** Renders children only if current user is Super Admin; otherwise redirects to dashboard with 403 state. */
export function SuperAdminGuard({ children }: { children: React.ReactNode }) {
  const isSuperAdmin = useIsSuperAdmin();
  const location = useLocation();
  if (!isSuperAdmin) {
    return <Navigate to="/dashboard" state={ { from: location.pathname, reason: 'forbidden' } } replace />;
  }
  return <>{children}</>;
}
