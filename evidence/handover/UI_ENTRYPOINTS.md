# UI_ENTRYPOINTS — Page/route list and key user flows

**Facts only.** Paths relative to `frontend/dfbs-ui/src/`.

**Authoritative source for routes:** If routes drift from this document, the authoritative source of truth for routes is the route config in `frontend/dfbs-ui/src/App.tsx`.

---

## Page/route list

| Route | Component path | Description |
|-------|----------------|-------------|
| `/login` | `pages/Login/index.tsx` | Login; token stored; redirect to dashboard. |
| `/public/repair` | `pages/WorkOrder/Public/index.tsx` | Public repair work order (no auth). |
| `/` | (layout) | Redirect to `/dashboard`. |
| `/dashboard` | `pages/Dashboard/index.tsx` | Dashboard. |
| `/customers` | `pages/Customer/index.tsx` | Customer list. |
| `/quotes` | `pages/Quote/index.tsx` | Quote list and actions. |
| `/shipments` | `pages/Shipment/index.tsx` | Shipment list and actions. |
| `/after-sales` | `pages/AfterSales/index.tsx` | After-sales list. |
| `/after-sales/:id` | `pages/AfterSales/Detail.tsx` | After-sales detail. |
| `/work-orders` | `pages/WorkOrder/Internal/index.tsx` | Internal work order list. |
| `/work-orders/:id` | `pages/WorkOrder/Internal/Detail.tsx` | Work order detail. |
| `/finance` | `pages/Finance/index.tsx` | Finance. |
| `/warehouse/inventory` | `pages/Warehouse/Inventory/index.tsx` | Warehouse inventory. |
| `/warehouse/replenish` | `pages/Warehouse/Replenish/index.tsx` | Replenish requests. |
| `/import-center` | `pages/ImportCenter/index.tsx` | Bulk import. |
| `/master-data/contracts` | `pages/MasterData/Contract/index.tsx` | Contract master. |
| `/master-data/machine-models` | `pages/MasterData/MachineModel/index.tsx` | Machine models. |
| `/master-data/machine-models/:id` | `pages/MasterData/MachineModel/Detail.tsx` | Machine model detail. |
| `/master-data/spare-parts` | `pages/MasterData/SparePart/index.tsx` | Spare parts. |
| `/master-data/machines` | `pages/MasterData/Machine/index.tsx` | Machines. |
| `/master-data/machines/:id` | `pages/MasterData/Machine/Detail.tsx` | Machine detail. |
| `/master-data/sim-cards` | `pages/MasterData/SimCard/index.tsx` | Sim cards. |
| `/master-data/sim-cards/:id` | `pages/MasterData/SimCard/Detail.tsx` | Sim card detail. |
| `/master-data/model-part-lists` | `pages/MasterData/ModelPartList/index.tsx` | Model part lists. |
| `/platform/orgs` | `pages/Platform/Org/index.tsx` | Platform orgs. |
| `/platform/applications` | `pages/Platform/Application/index.tsx` | Platform account applications. |
| `/platform/sim-applications` | `pages/Platform/SimApplication/index.tsx` | Sim applications. |
| `/admin/confirmation-center` | `pages/Admin/ConfirmationCenter/index.tsx` | Confirmation center. |
| `/system/platform-config` | `pages/System/PlatformConfig/index.tsx` | Platform config. |

Redirects: `/logistics` → `/shipments`; `/after-sales-service` → `/work-orders`; `/master-data` → `/master-data/contracts`; `/platform` → `/platform/applications`; `/admin` → `/admin/confirmation-center`.

---

## Key user flows (facts: click steps/behaviors)

- **Login**: Open `/login` → enter credentials → submit → token stored → redirect to `/dashboard`.
- **Platform application**: Go to `/platform/applications` → create (sales/service) or open row → planner confirm or admin approve/reject; duplicate check modal may show; org code validation by platform (INHAND/JINGPIN vs HENDONG).
- **Quote**: `/quotes` → create/edit quote, add items, submit workflow, payment/void as per UI.
- **Shipment**: `/shipments` → create, add machines, ship, complete; export ticket/receipt.

(Other flows follow from route and page responsibilities above.)

---

## Where UI logic lives

- **Routes**: `App.tsx`.
- **Layout/menu**: `layouts/BasicLayout.tsx`.
- **Pages**: `pages/<Area>/<Page>/index.tsx` or `Detail.tsx`.
- **Shared components**: `components/SmartInput`, `components/SmartReferenceSelect`, `components/Business/DuplicateCheckModal`, `components/Business/HitAnalysisPanel`, `components/AttachmentList`, `components/Access`.
- **API calls**: `utils/request.ts` (axios instance with baseURL `/api`); `utils/adapters.ts` (toProTableResult); `stores/useAuthStore.ts`; `services/platformConfig.ts`.
