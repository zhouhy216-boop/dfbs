# PERM Platform Application — v1 Permission Mapping

**Purpose:** Deterministic mapping for Step-05 enforcement: routes, UI actions, and backend endpoints → permission keys (`<moduleKey>:<actionKey>`). Evidence-derived from repo; v1 minimal but end-to-end.

**keyFormat:** `<moduleKey>:<actionKey>`. Default actionKey set: VIEW, CREATE, EDIT, SUBMIT, APPROVE, REJECT, CLOSE, DELETE, ASSIGN, EXPORT (from `perm_action` / V0071).

---

## ModuleKey taxonomy (v1, stable)

- **platform_application** — Root (e.g. menu group “平台&网卡管理” visibility if needed later).
- **platform_application.orgs** — Platform Org (平台管理): `/platform/orgs`.
- **platform_application.applications** — Platform Application (申请管理): `/platform/applications`.

Same dotted-child scheme as demo (`platform_application`, `platform_application.orders`). No change to keyFormat.

---

## Section A: Routes / menus mapping

| Route | Menu label (BasicLayout) | Permission key (menu/entry visibility) |
|-------|--------------------------|----------------------------------------|
| `/platform/orgs` | 平台管理 | `platform_application.orgs:VIEW` |
| `/platform/applications` | 申请管理 | `platform_application.applications:VIEW` |
| `/platform/sim-applications` | SIM管理 | *(v1 excluded — see Section D)* |

Parent group “平台&网卡管理” (`/platform`) visibility: v1 can be “show if user has VIEW on any of the child routes above” or leave group always visible and only hide child entries by the keys above.

---

## Section B: UI actions mapping (button → permission_key)

### Platform Org (`/platform/orgs`)

| UI action | Permission key | Notes |
|-----------|----------------|-------|
| View list / detail | `platform_application.orgs:VIEW` | Page view |
| Create org | `platform_application.orgs:CREATE` | Button “新建” / create flow |
| Edit org | `platform_application.orgs:EDIT` | In-row edit / update |
| Delete (soft) | `platform_application.orgs:DELETE` | Set status DELETED via PUT |

### Platform Application (`/platform/applications`)

| UI action | Permission key | Notes |
|-----------|----------------|-------|
| View list / detail | `platform_application.applications:VIEW` | Page view, table, get-by-id |
| Create application | `platform_application.applications:CREATE` | 开户申请 / create flow (includes check-duplicates in flow) |
| Planner submit | `platform_application.applications:SUBMIT` | 提交至管理员审核 |
| Approve | `platform_application.applications:APPROVE` | 通过 |
| Reject | `platform_application.applications:REJECT` | 驳回 |
| Close | `platform_application.applications:CLOSE` | 关闭申请 |

---

## Section C: Backend endpoints mapping (method + path → permission_key, critical?)

### Platform Org — `PlatformOrgController` (`/api/v1/platform-orgs`)

| Method | Path | Permission key | Critical? | Notes |
|--------|------|----------------|-----------|--------|
| GET | `/api/v1/platform-orgs` | `platform_application.orgs:VIEW` | no | List; guard with VIEW |
| GET | `/api/v1/platform-orgs/{id}` | `platform_application.orgs:VIEW` | no | Detail |
| GET | `/api/v1/platform-orgs/platform/{platform}/customer/{customerId}` | `platform_application.orgs:VIEW` | no | List by platform+customer |
| POST | `/api/v1/platform-orgs` | `platform_application.orgs:CREATE` | **yes** | Create |
| PUT | `/api/v1/platform-orgs/{id}` | `platform_application.orgs:EDIT` or `DELETE` | **yes** | Update; if body sets status=DELETED → treat as DELETE |

### Platform Application — `PlatformAccountApplicationController` (`/api/v1/platform-account-applications`)

| Method | Path | Permission key | Critical? | Notes |
|--------|------|----------------|-----------|--------|
| GET | `/api/v1/platform-account-applications/page` | `platform_application.applications:VIEW` | no | List |
| GET | `/api/v1/platform-account-applications/{id}` | `platform_application.applications:VIEW` | no | Detail |
| GET | `/api/v1/platform-account-applications/check-customer-name` | `platform_application.applications:VIEW` | no | Helper for create flow |
| POST | `/api/v1/platform-account-applications/check-duplicates` | `platform_application.applications:VIEW` | no | Used in create flow; VIEW sufficient for v1 |
| GET | `/api/v1/platform-account-applications/check-org-match` | `platform_application.applications:VIEW` | no | Helper |
| POST | `/api/v1/platform-account-applications/create` | `platform_application.applications:CREATE` | **yes** | Create |
| PUT | `/api/v1/platform-account-applications/{id}/planner-submit` | `platform_application.applications:SUBMIT` | **yes** | Planner submit |
| POST | `/api/v1/platform-account-applications/{id}/approve` | `platform_application.applications:APPROVE` | **yes** | Approve |
| POST | `/api/v1/platform-account-applications/{id}/reject` | `platform_application.applications:REJECT` | **yes** | Reject |
| POST | `/api/v1/platform-account-applications/{id}/close` | `platform_application.applications:CLOSE` | **yes** | Close |

---

## Section D: v1 exclusions (not covered yet)

- **SIM管理** (`/platform/sim-applications`): route, menu, and all related endpoints excluded from v1 mapping; include in a later iteration if needed.
- **平台配置** (`/system/platform-config`): under “系统” menu; excluded from v1.
- **Other platform routes:** `/platform/apply`, `/platform/applications/history`, `/platform/applications/reuse`, `verification`, `sim-activation`, `enterprise-direct`: not enumerated in this v1 table; can be added later (e.g. map to `platform_application.applications:VIEW` or dedicated keys).
- **Masterdata / other domains:** No permission keys in this doc; only Platform Org and Platform Application as above.

---

## Summary (v1 included)

- **Routes in scope:** `/platform/orgs`, `/platform/applications`.
- **ModuleKeys:** `platform_application.orgs`, `platform_application.applications`.
- **Critical server-side guards (must 403 if no permission):** POST/PUT create/update/delete for orgs; POST create, PUT planner-submit, POST approve/reject/close for applications.
