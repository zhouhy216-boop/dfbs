# SHIPFLOW-260304-001-01 Impact Check — Shipment workflow skeleton on /shipments

**Ticket:** SHIPFLOW-260304-001-01.a (facts-only; no implementation).

---

## Completed?

Yes.

---

## Full-suite build

- **Backend:** Not run for this ticket. (Previous handover: `.\mvnw.cmd -q -DskipTests package` passes.)
- **Frontend:** Not run. (Previous handover: `npm run build` fails due to existing TS errors elsewhere.)

---

## Findings

### Pages/flows

- **Route:** `path="shipments"` → `<Shipment />`. Evidence: `frontend/dfbs-ui/src/App.tsx` L70.
- **List:** Single page `frontend/dfbs-ui/src/pages/Shipment/index.tsx` — ProTable, columns 发货单号 / 客户 / 状态 / 创建时间 / 操作(详情, 导出运单, 导出回单). Request: GET `/v1/shipments` with params (status, page, size). Toolbar: “New Shipment” ModalForm → POST `/v1/shipments`.
- **Detail:** Same file — Drawer opened when row “详情” clicked; load via GET `/v1/shipments/{id}`. Content: one button “发起售后 (Apply After-Sales)”, then Descriptions (ID, 报价单ID, 类型, 状态, 承运商, 发货日期, 收货人, 电话, 地址, 创建时间), then 附件 (AttachmentList). No “available next actions” block today; workflow actions (accept/ship/complete/exception/cancel) are **not** called from this page.
- **Status display:** List: `dataIndex: 'status'` with `valueEnum` (CREATED, PENDING_SHIP, SHIPPED, COMPLETED, EXCEPTION, CANCELLED + frontend-only PENDING, PARTIAL_SHIPPED, DELIVERED). Detail: `<Descriptions.Item label="状态">{detail.status}</Descriptions.Item>` (raw string). Insert point for “available next actions”: in Drawer, after line 197 (current “发起售后” block) or before Descriptions; safe to add a dedicated block without moving existing nodes.

### Modules/files

- **Backend:** `modules/shipment/ShipmentEntity.java` (status: `ShipmentStatus`, approvalStatus: `ApprovalStatus`); `modules/shipment/ShipmentStatus.java` (CREATED, PENDING_SHIP, SHIPPED, COMPLETED, EXCEPTION, CANCELLED); `application/shipment/ShipmentService.java` (listWithCustomerName, getDetail, accept, ship, complete, handleException, cancel — transitions enforced with `require()` in service); `application/shipment/ShipmentListDto.java` (id, shipmentNo, customerName, status, createdAt); `interfaces/shipment/ShipmentController.java` (GET /, GET /{id}, POST /{id}/accept, /{id}/ship, /{id}/complete, /{id}/exception, /{id}/cancel).
- **Frontend:** `pages/Shipment/index.tsx` (single component for list + detail Drawer; no separate detail route).

### APIs/contracts

- **List:** GET `/api/v1/shipments` — params: status (ShipmentStatus), quoteId, initiatorId, page, size. Returns: `Page<ShipmentListDto>` (Spring Page). Frontend uses `toProTableResult(data)`; expects `data.content`, pagination.
- **Detail:** GET `/api/v1/shipments/{id}` — returns `ShipmentEntity` (JSON). Frontend expects `ShipmentDetail` (id, quoteId, type, status, contractNo, shipDate, deliveryAddress, receiverName, receiverPhone, carrierId, carrier, createdAt).
- **Workflow (existing, not used by Shipment page):** POST `/api/v1/shipments/{id}/accept`, `/ship`, `/complete`, `/exception`, `/cancel` — all return updated `ShipmentEntity`. Service rules: accept CREATED→PENDING_SHIP; ship PENDING_SHIP→SHIPPED; complete SHIPPED→COMPLETED; exception any except COMPLETED/CANCELLED→EXCEPTION; cancel→CANCELLED.

---

## Regression watchlist

1. **List:** GET `/v1/shipments` response shape (content, total, etc.) and ProTable request/params — do not change contract or adapter usage.
2. **Detail:** GET `/v1/shipments/{id}` response fields consumed by Drawer (status, type, quoteId, etc.) — preserve; adding a “next actions” block must not remove or rename existing Descriptions items.
3. **Status display:** List valueEnum and detail `detail.status` — skeleton must not change existing status values or column keys.
4. **Create flows:** POST `/v1/shipments` (standalone) and Quote page “Create Shipment” (create-from-quote) — unchanged.
5. **Export:** GET `/v1/shipments/{id}/export-ticket`, `/export-receipt` — unchanged.
6. **After-sales:** “发起售后” button and POST `/v1/after-sales` with sourceShipmentId — unchanged.
7. **Backend transition rules:** accept/ship/complete/exception/cancel — any new “skeleton” (e.g. allowed-actions API) must not break existing require() checks or response type.

---

## Not verified

- Full backend test run and frontend build not executed for this ticket. Shipment tests: `ShipmentProcessTest.java`, `ShipmentPanoramaTest.java` (names only; not run).
