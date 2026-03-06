import { useEffectivePermissions } from '@/shared/hooks/useEffectivePermissions';
import { Navigate, useLocation } from 'react-router-dom';
import { useIsAdminOrSuperAdmin } from '@/shared/components/AdminOrSuperAdminGuard';

const WORK_ORDER_VIEW = 'work_order:VIEW';

/** Renders children when user has work_order:VIEW or is admin/super-admin; otherwise redirects to /dashboard. */
export function WorkOrderViewGuard({ children }: { children: React.ReactNode }) {
  const { has, loading } = useEffectivePermissions();
  const isAdminOrSuperAdmin = useIsAdminOrSuperAdmin();
  const location = useLocation();

  if (loading) {
    return <div style={{ padding: 24 }}>加载中...</div>;
  }
  if (!has(WORK_ORDER_VIEW) && !isAdminOrSuperAdmin) {
    return <Navigate to="/dashboard" state={{ from: location.pathname, reason: 'forbidden' }} replace />;
  }
  return <>{children}</>;
}
