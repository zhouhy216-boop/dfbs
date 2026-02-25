# PERM-260211-001-03.d — Dev-only minimal default role templates

**Ticket:** PERM-260211-001-03.d  
**Purpose:** Seed minimal role templates (e.g. 只读模板) so the Roles & Permissions page has at least one template to interact with on a fresh DB; no permissions required, works when modules tree is empty.

---

## How to enable (Windows PowerShell)

From `backend/dfbs-app`, start the backend with the seeder enabled (same style as other dev flags):

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Ddfbs.dev.seedPermDefaultRoleTemplates=true"
```

- **Gate:** `dfbs.dev.seedPermDefaultRoleTemplates=true` (default `false`). Seeder does **not** run when profile `prod` is active.
- **Idempotent:** Safe to restart; no duplicate roles.

---

## Verifying seeder ran (evidence)

After startup with the flag set, check backend logs for:

- `DevPermDefaultRoleTemplatesSeeder: flag=..., activeProfiles=...` — confirms the runner executed and logs the flag value and profiles.
- `DevPermDefaultRoleTemplatesSeeder: template_viewer=inserted|skipped (exists), template_editor=...` — confirms what was inserted or skipped.

If you see `skipping (prod profile active)`, the seeder did not insert (prod safety). If you do **not** see any `DevPermDefaultRoleTemplatesSeeder` line, the bean was not created: ensure the JVM argument is exactly `-Ddfbs.dev.seedPermDefaultRoleTemplates=true` and that no `prod` profile is active.

**API:** `GET /api/v1/admin/perm/roles?enabledOnly=true` returns only enabled roles (e.g. template_viewer). Omit the param or `enabledOnly=false` to get all roles (including template_editor when disabled).

---

## Expected UI effect

- **Default list** (enabled only): shows at least **只读模板** (`template_viewer`), enabled.
- **显示全部（含停用）**: checkbox triggers refetch with `enabledOnly=false`; list then includes **编辑模板** (`template_editor`), disabled.
- Both templates have **no permissions** by default; “Save to apply” still applies when you assign permissions later.
