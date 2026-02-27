import { useEffectivePermissions } from '@/shared/hooks/useEffectivePermissions';
import { Navigate, useLocation } from 'react-router-dom';

const WORK_ORDER_VIEW = 'work_order:VIEW';

/** Renders children only when user has work_order:VIEW; otherwise redirects to /dashboard. Deny-by-default until loaded. */
export function WorkOrderViewGuard({ children }: { children: React.ReactNode }) {
  const { has, loading } = useEffectivePermissions();
  const location = useLocation();

  if (loading) {
    return <div style={{ padding: 24 }}>加载中...</div>;
  }
  if (!has(WORK_ORDER_VIEW)) {
    return <Navigate to="/dashboard" state={{ from: location.pathname, reason: 'forbidden' }} replace />;
  }
  return <>{children}</>;
}
