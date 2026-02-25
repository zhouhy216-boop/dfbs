# PERM-260211-001-02.c-fix1 — Dev-only demo perm modules (CEO UI verification)

**Ticket:** PERM-260211-001-02.c-fix1  
**Purpose:** In dev/local, auto-create a small demo module tree so the Roles & Permissions UI shows checkboxes; CEO can verify draft/save/reset (steps 5–7) purely via UI, no curl/DB.

---

## How to enable (Windows PowerShell)

From `backend/dfbs-app`, start the backend with the dev seeder enabled (same style as admin2):

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Ddfbs.dev.seedPermDemoModules=true"
```

- **Gate:** `dfbs.dev.seedPermDemoModules=true` (default `false`). Seeder does **not** run when profile `prod` is active.
- **Idempotent:** Safe to restart; no duplicate modules/actions.

---

## Expected UI effect

- **Permission tree** (right panel on 角色与权限): shows demo modules — root **平台应用** (`platform_application`) and child **订单** (`platform_application.orders`) with actions VIEW/CREATE/EDIT.
- **Role binding checkboxes** become testable: select a role, tick/untick permissions, **Save** and **Reset** to verify steps 5–7.

No role bindings are seeded; only modules and module–action links for display and testing.
