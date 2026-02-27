/**
 * In-memory cache for current user effective permission keys (GET /perm/me/effective-keys or test/me/effective-keys).
 * Keyed by userId + visionSignature; cleared on logout so next user gets fresh result. Deny-by-default until loaded.
 */
function cacheKey(userId: string, visionKey?: string): string {
  return userId + '|' + (visionKey ?? '');
}

let cache: { key: string; keys: Set<string> } | null = null;

export function getEffectiveKeysCache(userId: string | null, visionKey?: string): Set<string> | null {
  if (userId == null || userId === '') return null;
  const key = cacheKey(userId, visionKey);
  if (cache?.key !== key) return null;
  return cache.keys;
}

export function setEffectiveKeysCache(userId: string, keys: Set<string>, visionKey?: string): void {
  cache = { key: cacheKey(userId, visionKey), keys };
}

export function clearEffectiveKeysCache(): void {
  cache = null;
}
