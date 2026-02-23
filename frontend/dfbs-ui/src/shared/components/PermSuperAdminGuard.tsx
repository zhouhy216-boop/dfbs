import { useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import request from '@/shared/utils/request';

interface PermMeResponse {
  allowed?: boolean;
}

let cachedAllowed: boolean | null = null;

export function useIsPermSuperAdmin(): { allowed: boolean; loading: boolean } {
  const [allowed, setAllowed] = useState(cachedAllowed ?? false);
  const [loading, setLoading] = useState(cachedAllowed === null);

  useEffect(() => {
    if (cachedAllowed !== null) {
      setAllowed(cachedAllowed);
      setLoading(false);
      return;
    }
    request
      .get<PermMeResponse>('/v1/admin/perm/super-admin/me')
      .then((res) => {
        const value = res.data?.allowed === true;
        cachedAllowed = value;
        setAllowed(value);
      })
      .catch(() => {
        cachedAllowed = false;
        setAllowed(false);
      })
      .finally(() => setLoading(false));
  }, []);

  return { allowed, loading };
}

/** Renders children only when user is in PERM super-admin allowlist; otherwise redirects to /dashboard. */
export function PermSuperAdminGuard({ children }: { children: React.ReactNode }) {
  const { allowed, loading } = useIsPermSuperAdmin();
  const location = useLocation();

  if (loading) {
    return <div style={{ padding: 24 }}>加载中...</div>;
  }
  if (!allowed) {
    return <Navigate to="/dashboard" state={{ from: location.pathname, reason: 'forbidden' }} replace />;
  }
  return <>{children}</>;
}
