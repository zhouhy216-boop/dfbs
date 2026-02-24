import { useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import request from '@/shared/utils/request';
import { getPermAllowedCache, setPermAllowedCache, clearPermAllowedCache } from '@/shared/permAllowedCache';

export { clearPermAllowedCache };

interface PermMeResponse {
  allowed?: boolean;
}

export function useIsPermSuperAdmin(): { allowed: boolean; loading: boolean } {
  const cached = getPermAllowedCache();
  const [allowed, setAllowed] = useState(cached ?? false);
  const [loading, setLoading] = useState(cached === null);

  useEffect(() => {
    if (cached !== null) {
      setAllowed(cached);
      setLoading(false);
      return;
    }
    request
      .get<PermMeResponse>('/v1/admin/perm/super-admin/me')
      .then((res) => {
        const value = res.data?.allowed === true;
        setPermAllowedCache(value);
        setAllowed(value);
      })
      .catch(() => {
        setPermAllowedCache(false);
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
