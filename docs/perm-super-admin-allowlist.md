# PERM 角色与权限 — Super-admin allowlist

Only users listed in the allowlist can see and use the “角色与权限” admin entry and PERM admin endpoints.

## Config key

- **Name:** `dfbs.perm.superAdminAllowlist`
- **Format:** Comma-separated list of identity keys (no spaces, or spaces trimmed).
- **Example:** `1,2` or `1, 2`

## Identity key used

- **Field:** `userId` (Long), from the current authenticated user.
- **Comparison:** Stored and compared as **string** in the allowlist (e.g. `"1"`, `"2"`).
- **Source:** Resolved server-side via `CurrentUserIdResolver.getCurrentUserId()` (same as other admin features). The login/context must provide a way to resolve to a numeric userId; that value (as string) is what you put in the allowlist.

## Example (local dev)

In `application.yml`:

```yaml
dfbs:
  perm:
    superAdminAllowlist: "1"
```

Or via environment variable (if supported by your config binding):

```bash
DFBS_PERM_SUPERADMINALLOWLIST=1,2
```

(Spring Boot maps `dfbs.perm.superAdminAllowlist` from `DFBS_PERM_SUPERADMINALLOWLIST` when using relaxed binding.)

## Behaviour

- **Empty or unset:** No one is allowlisted; “角色与权限” is hidden and PERM admin endpoints return 403.
- **Server:** PERM admin endpoints (other than the visibility check `GET /api/v1/admin/perm/super-admin/me`) must call `PermSuperAdminGuard.requirePermSuperAdmin()`; if the current user’s userId (as string) is not in the list, the server returns 403 with message “无权限”.
- **Frontend:** The “角色与权限” menu entry and page are only shown when `GET .../super-admin/me` returns `allowed: true`.
