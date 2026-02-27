import { useEffect, useMemo, useState } from 'react';
import request from '@/shared/utils/request';
import { getStoredUserId } from '@/shared/utils/request';
import {
  getEffectiveKeysCache,
  setEffectiveKeysCache,
  clearEffectiveKeysCache,
} from '@/shared/effectiveKeysCache';
import { useVisionStore } from '@/shared/stores/useVisionStore';

export { clearEffectiveKeysCache };

export interface EffectivePermissionsResult {
  /** Effective permission keys (empty until loaded; deny-by-default). */
  keys: Set<string>;
  loading: boolean;
  has: (key: string) => boolean;
}

interface EffectiveKeysResponse {
  effectiveKeys?: string[];
}

export function useEffectivePermissions(): EffectivePermissionsResult {
  const userId = getStoredUserId();
  const vision = useVisionStore((s) => s.vision);
  const visionVersion = useVisionStore((s) => s.version);
  const visionKey = vision?.mode === 'USER' && vision.userId != null ? `vision:${vision.userId}` : undefined;
  const cached = userId ? getEffectiveKeysCache(userId, visionKey) : null;
  const [keys, setKeys] = useState<Set<string>>(cached ?? new Set());
  const [loading, setLoading] = useState(cached === null && !!userId);

  useEffect(() => {
    if (!userId) {
      setKeys(new Set());
      setLoading(false);
      return;
    }
    const sig = vision?.mode === 'USER' && vision.userId != null ? `vision:${vision.userId}` : undefined;
    const c = getEffectiveKeysCache(userId, sig);
    if (c !== null) {
      setKeys(c);
      setLoading(false);
      return;
    }
    setLoading(true);
    if (sig != null) {
      request
        .get<EffectiveKeysResponse>('/v1/admin/perm/test/me/effective-keys')
        .then((res) => {
          const list = res.data?.effectiveKeys ?? [];
          const set = new Set(list);
          setEffectiveKeysCache(userId, set, sig);
          setKeys(set);
        })
        .catch(() => setKeys(new Set()))
        .finally(() => setLoading(false));
    } else {
      request
        .get<EffectiveKeysResponse>('/v1/perm/me/effective-keys')
        .then((res) => {
          const list = res.data?.effectiveKeys ?? [];
          const set = new Set(list);
          setEffectiveKeysCache(userId, set);
          setKeys(set);
        })
        .catch(() => setKeys(new Set()))
        .finally(() => setLoading(false));
    }
  }, [userId, vision?.mode, vision?.userId, visionVersion]);

  const has = useMemo(
    () => (key: string) => (key ? keys.has(key.trim()) : false),
    [keys]
  );

  return { keys, loading, has };
}
