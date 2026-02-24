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

## Dev-only: non-allowlist test user (admin2)

To verify 403 / menu hidden / redirect **without** editing backend config or touching the DB:

1. **Start backend** so the dev seeder runs (pick one):
   - **One-command (Windows PowerShell, from backend folder):**  
     `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Ddfbs.dev.seedNonAllowlistUser=true"`  
     (Seeder runs when property is true and profile is not `prod`.)
   - **Using dev profile (loads application-dev.yml):**  
     `.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev`  
     or: `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev"`  
     or set env `SPRING_PROFILES_ACTIVE=dev` then run `.\mvnw.cmd spring-boot:run`.
   - **Do not** put `--spring.profiles.active=dev` directly after `spring-boot:run` (e.g. `.\mvnw.cmd spring-boot:run --spring.profiles.active=dev`). Maven does not accept that; you get “Unrecognized option”.
2. **Log in as admin2:** Username **admin2**, password **anything** (ignored in MVP).
3. **Verify:** `GET /api/v1/admin/perm/permission-tree` → 403 “无权限”; sidebar no “角色与权限”; `/admin/roles-permissions` → redirect to /dashboard.

**Files:** `application-dev.yml` (profile dev), `runner/DevNonAllowlistUserSeeder.java`. Do **not** add admin2’s userId to `dfbs.perm.superAdminAllowlist`.
