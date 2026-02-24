# PERM-260211-001-02.e — admin2 dev helper + verification notes

**Ticket:** PERM-260211-001-02.e  
**Purpose:** Dev-only helper to create non-allowlist test user for 403 verification; no backend config edit, no direct DB.

---

## How to use (CEO / beginner)

1. **Start backend** from `backend/dfbs-app` so the seeder runs. **Windows PowerShell** — use one of:

   - **Simplest one-command (no profile needed):**  
     `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Ddfbs.dev.seedNonAllowlistUser=true"`

   - **Using dev profile** (loads `application-dev.yml`):  
     `.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev`  
     or: `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev"`  
     or: `$env:SPRING_PROFILES_ACTIVE="dev"; .\mvnw.cmd spring-boot:run`

   - **Do not** run: `.\mvnw.cmd spring-boot:run --spring.profiles.active=dev`  
     (Maven treats `--spring.profiles.active=dev` as a Maven option and fails with “Unrecognized option”.)

2. **Log in** with username **admin2**, password **anything** (ignored in MVP).
3. **Verify:** `GET /api/v1/admin/perm/permission-tree` → **403** “无权限”; menu “角色与权限” **hidden**; `/admin/roles-permissions` → **redirect to /dashboard**.

---

## File paths and env

- **Gate:** `dfbs.dev.seedNonAllowlistUser` (default `false`). Seeder runs when `true` and profile is not `prod`.
- **One-command:** Pass the property via JVM: `-Dspring-boot.run.jvmArguments=-Ddfbs.dev.seedNonAllowlistUser=true`.
- **Dev config:** `application-dev.yml` sets the property when profile `dev` is active.
- **Seeder:** `runner/DevNonAllowlistUserSeeder.java` (idempotent; skips when profile `prod` is active).

---

## What not to do

- Do **not** add admin2’s userId to `dfbs.perm.superAdminAllowlist`.
- Do **not** put `--spring.profiles.active=dev` as a separate argument after `spring-boot:run` (use `-Dspring-boot.run.profiles=dev` or `-Dspring-boot.run.arguments=...` instead).
