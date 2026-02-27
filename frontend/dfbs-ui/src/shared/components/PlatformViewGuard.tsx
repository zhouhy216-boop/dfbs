import { Navigate, useLocation } from 'react-router-dom';
import { useEffectivePermissions } from '@/shared/hooks/useEffectivePermissions';

interface PlatformViewGuardProps {
  requiredPermission: string;
  children: React.ReactNode;
}

/** Renders children only when user has required VIEW permission; otherwise redirects to /dashboard. Deny-by-default until loaded. */
export function PlatformViewGuard({ requiredPermission, children }: PlatformViewGuardProps) {
  const { has, loading } = useEffectivePermissions();
  const location = useLocation();

  if (loading) {
    return <div style={{ padding: 24 }}>加载中...</div>;
  }
  if (!has(requiredPermission)) {
    return <Navigate to="/dashboard" state={{ from: location.pathname, reason: 'forbidden' }} replace />;
  }
  return <>{children}</>;
}
