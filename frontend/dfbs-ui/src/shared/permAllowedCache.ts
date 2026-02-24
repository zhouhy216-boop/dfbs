/**
 * In-memory cache for PERM super-admin /me result. Cleared on logout/401 so next user gets fresh result.
 */
let cached: boolean | null = null;

export function getPermAllowedCache(): boolean | null {
  return cached;
}

export function setPermAllowedCache(value: boolean): void {
  cached = value;
}

export function clearPermAllowedCache(): void {
  cached = null;
}
