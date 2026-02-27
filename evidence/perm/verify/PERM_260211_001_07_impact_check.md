# Step-07 Impact Check — Work Order Enforcement (FACTS ONLY)

**Request ID:** PERM-260211-001-07-IMP | **DO NOT MODIFY**

**1) Impacted areas**  
- **Frontend:** Menu 售后服务 → 工单管理 (`/work-orders`); routes `/work-orders`, `/work-orders/:id`; list/detail pages `WorkOrder/Internal/index.tsx`, `Detail.tsx`. Menu currently not filtered by `hasPermission` (only platform-group is in `BasicLayout.buildMenuRoutes`). Buttons: 派单/驳回/受理/新建, detail 派单/接单/记录/配件/签字/完成.  
- **Backend:** `WorkOrderController.java` — no `PermEnforcementService`; add `requirePermission` at controller. `WorkOrderService` unchanged.  
- **APIs:** Read: `GET /pool`, `GET /my-orders`, `GET /{id}`. Critical: `POST /create-from-quote`, `/create`, `/reject`, `/accept-by-dispatcher`, `/dispatch`, `/accept`, `/record`, `/parts/add`, `/parts/consume`, `/sign`, `/complete`. Public `POST /api/v1/public/work-orders/create` — TBD if guarded.  
- **Reuse:** `PermEnforcementService.requirePermission`, `PermMeController`, frontend `useEffectivePermissions()`; no new framework.

**2) Regression watchlist**  
Step-05 platform enforcement unchanged; effective-perms/cache and Role-Vision no leakage; Step-01~06 allowlist/templates/test utils/audit unchanged; quote/shipment/expense/inventory/trip/repair/finance unaffected unless they call work-order APIs.

**3) Build/test status**  
Build: not executed. Tests: not executed. (Backend: `mvn -q compile`; frontend: `npm run build`.)
