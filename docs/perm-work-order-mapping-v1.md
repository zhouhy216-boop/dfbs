# PERM Work Order — v1 Permission Mapping

**Purpose:** Deterministic v1 mapping for Step-07: Work Order menu/routes, UI actions, and backend endpoints → permission keys (`<moduleKey>:<actionKey>`). Evidence-derived from repo (impact check + `WorkOrderController`, `WorkOrder/Internal/index.tsx`, `Detail.tsx`). Enforcement is implemented in 07.c/07.d; this ticket is mapping-only.

**keyFormat:** `<moduleKey>:<actionKey>`. Reuses v1 default action set (VIEW, CREATE, EDIT, SUBMIT, APPROVE, REJECT, ASSIGN, CLOSE, DELETE, EXPORT) per `docs/perm-permission-keys.md`.

---

## ModuleKey taxonomy (v1, stable)

- **work_order** — Single module for internal Work Order (售后服务 → 工单管理). Consistent with `platform_application.*` style; no sub-key for v1. Public endpoint excluded (Section D).

---

## Section A: Menu / routes mapping (route → permission_key)

| Route | Menu label (BasicLayout) | Permission key (hide-only) |
|-------|---------------------------|----------------------------|
| `/work-orders` | 工单管理 (under 售后服务) | `work_order:VIEW` |
| `/work-orders/:id` | (detail, same menu entry) | `work_order:VIEW` |

Parent group “售后服务” (`/after-sales-service` redirects to `/work-orders`): v1 shows group if user has `work_order:VIEW`; otherwise hide “工单管理” entry (same pattern as platform-group in `BasicLayout.buildMenuRoutes`).

---

## Section B: UI actions mapping (button → permission_key)

**Source:** `frontend/dfbs-ui/src/pages/WorkOrder/Internal/index.tsx`, `Detail.tsx`.

### List page (`/work-orders`)

| UI action | Permission key | API (for reference) |
|-----------|----------------|---------------------|
| 待受理池 / 待派单池 / 我的工单 / 全部池 (tabs, read) | `work_order:VIEW` | GET /pool, /my-orders |
| 受理 | `work_order:ASSIGN` | POST /accept-by-dispatcher |
| 驳回 | `work_order:REJECT` | POST /reject |
| 派单 | `work_order:ASSIGN` | POST /dispatch |
| 新建工单 | `work_order:CREATE` | POST /create |

### Detail page (`/work-orders/:id`)

| UI action | Permission key | API (for reference) |
|-----------|----------------|---------------------|
| 查看详情 (read) | `work_order:VIEW` | GET /{id} |
| 派单 | `work_order:ASSIGN` | POST /dispatch |
| 接单 | `work_order:SUBMIT` | POST /accept |
| 开始处理 | `work_order:EDIT` | same as 添加记录 (record) |
| 添加记录 | `work_order:EDIT` | POST /record |
| 添加配件 | `work_order:EDIT` | POST /parts/add |
| 消耗配件 | `work_order:EDIT` | POST /parts/consume |
| 提交签字 | `work_order:APPROVE` | POST /sign |
| 完修 | `work_order:CLOSE` | POST /complete |

**Action key choices (v1, documented):** ASSIGN = 受理 + 派单; REJECT = 驳回; SUBMIT = 接单 (service manager accepts); EDIT = 记录/配件; APPROVE = 提交签字; CLOSE = 完修.

---

## Section C: Backend endpoints mapping (method + path → permission_key, critical?)

**Controller:** `WorkOrderController`, base path `/api/v1/work-orders` (`com.dfbs.app.interfaces.workorder.WorkOrderController`).

| Method | Path (suffix) | Permission key | Critical? | Notes |
|--------|----------------|----------------|-----------|--------|
| GET | `/api/v1/work-orders/pool` | `work_order:VIEW` | no | List pool |
| GET | `/api/v1/work-orders/my-orders` | `work_order:VIEW` | no | My orders |
| GET | `/api/v1/work-orders/{id}` | `work_order:VIEW` | no | Detail |
| POST | `/api/v1/work-orders/create-from-quote` | `work_order:CREATE` | **yes** | From quote |
| POST | `/api/v1/work-orders/create` | `work_order:CREATE` | **yes** | Internal create |
| POST | `/api/v1/work-orders/reject` | `work_order:REJECT` | **yes** | Reject |
| POST | `/api/v1/work-orders/accept-by-dispatcher` | `work_order:ASSIGN` | **yes** | 受理 |
| POST | `/api/v1/work-orders/dispatch` | `work_order:ASSIGN` | **yes** | 派单 |
| POST | `/api/v1/work-orders/accept` | `work_order:SUBMIT` | **yes** | 接单 |
| POST | `/api/v1/work-orders/record` | `work_order:EDIT` | **yes** | Add process record |
| POST | `/api/v1/work-orders/parts/add` | `work_order:EDIT` | **yes** | Add part |
| POST | `/api/v1/work-orders/parts/consume` | `work_order:EDIT` | **yes** | Consume part |
| POST | `/api/v1/work-orders/sign` | `work_order:APPROVE` | **yes** | Submit for signature |
| POST | `/api/v1/work-orders/complete` | `work_order:CLOSE` | **yes** | Complete |

---

## Section D: v1 exclusions (explicit)

- **Public endpoint:** `POST /api/v1/public/work-orders/create` (`WorkOrderPublicController`). **TBD** — explicitly excluded from v1 mapping; do NOT implement permission guard in this ticket. Decide in a later step whether it stays public or gets a dedicated key (e.g. `work_order.public:CREATE`).
- **Other domains:** Quote import from work order (`WorkOrderQuoteController`), outbound for work order (`OutboundController.outboundForWorkOrder`), expense/trip/repair references to work order: not in this mapping; no permission keys added here.
- **SIM管理 / 平台配置 / 其他系统菜单:** Unchanged; see platform and other mapping docs.

---

## Summary (v1 included)

- **Routes in scope:** `/work-orders`, `/work-orders/:id` (menu entry 工单管理 under 售后服务).
- **ModuleKey:** `work_order` (single key).
- **Action keys used:** VIEW, CREATE, EDIT, SUBMIT, APPROVE, REJECT, ASSIGN, CLOSE.
- **Critical server-side guards (must 403 if no permission):** All POST endpoints in Section C except read; read endpoints (GET pool, my-orders, {id}) guarded with VIEW.
