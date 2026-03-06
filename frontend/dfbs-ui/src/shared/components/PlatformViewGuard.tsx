import { Navigate, useLocation } from 'react-router-dom';
import { useEffectivePermissions } from '@/shared/hooks/useEffectivePermissions';
import { useIsAdminOrSuperAdmin } from '@/shared/components/AdminOrSuperAdminGuard';

interface PlatformViewGuardProps {
  requiredPermission: string;
  children: React.ReactNode;
}

/** Renders children when user has required VIEW permission or is admin/super-admin; otherwise redirects to /dashboard. */
export function PlatformViewGuard({ requiredPermission, children }: PlatformViewGuardProps) {
  const { has, loading } = useEffectivePermissions();
  const isAdminOrSuperAdmin = useIsAdminOrSuperAdmin();
  const location = useLocation();

  if (loading) {
    return <div style={{ padding: 24 }}>加载中...</div>;
  }
  if (!has(requiredPermission) && !isAdminOrSuperAdmin) {
    return <Navigate to="/dashboard" state={{ from: location.pathname, reason: 'forbidden' }} replace />;
  }
  return <>{children}</>;
}
