# SHIPFLOW-260304-001-02 Impact Check (Facts Only)

**Request ID:** SHIPFLOW-260304-001-02-IMP  
**Scope:** Step-02 (detail-driven step actions + minimal required fields) — impact + regression checklist before implementation.

---

## 1) Likely impacted areas

**Pages/flows (frontend):**
- `/shipments` list + Drawer detail (`frontend/dfbs-ui/src/pages/Shipment/index.tsx`): add per-step forms in Drawer (no multi-page); existing “发起售后” button + 导出运单/回单 links in same Drawer.

**Modules/files (backend):**
- `ShipmentEntity` (`modules/shipment/ShipmentEntity.java`): existing fields (see below) — no schema change required unless Step-02 adds new required columns.
- Create/update DTOs: `NormalShipmentCreateRequest` (contractNo, salespersonName, packagingType, receiverName, receiverPhone, unloadService, deliveryAddress, remark); `CreateFromQuoteRequest` (quoteId, initiatorId, entrustMatter, shipDate, quantity, model, needPackaging, pickup*, receiver*, deliveryAddress, remark); no PATCH/PUT DTO for shipment today.
- Transition endpoints (all POST): `accept(id, operatorId)`; `ship(id, operatorId, ShipActionRequest)`; `complete(id, operatorId)`; `exception(id, operatorId, ReasonRequest)`; `cancel(id, operatorId?, ReasonRequest)`.

**APIs/contracts:**
- Existing: GET list, GET `/{id}`, GET `/{id}/workflow`; POST create/create-normal/create-from-quote; POST `/{id}/accept`, `/{id}/ship`, `/{id}/complete`, `/{id}/exception`, `/{id}/cancel`; POST `/{id}/machines`; GET export-ticket, export-receipt.
- **No** PATCH or PUT “update shipment info” endpoint today; only transition endpoints and `updateMachines`.

---

## 2) Facts to confirm (evidence)

**Data shipment already stores (reusable as Step-02 “minimal required”):**  
`ShipmentEntity`: id, quoteId, initiatorId, applicantId, type, approvalStatus, status; contractNo, salespersonName, packagingType, receiverName, unloadService; receiptUrl, ticketUrl; entrustMatter, shipDate, quantity, model, needPackaging; pickupContact, pickupPhone, needLoading, pickupAddress; receiverContact, receiverPhone, needUnloading, deliveryAddress; carrier, carrierId; isBillableToCustomer, logisticsNo, freightBillId, remark; exceptionReason, cancelReason; acceptedAt, shippedAt, completedAt, createdAt. (All in `ShipmentEntity.java`.)

**Per-step action vs existing endpoint/DTO:**

| Step (concept)            | Existing endpoint + DTO slot? | Smallest new shape if not |
|---------------------------|------------------------------|----------------------------|
| Sales request             | Yes: POST create-from-quote (CreateFromQuoteRequest), create-normal (NormalShipmentCreateRequest). |
| Biz review (接单)         | Yes: POST `/{id}/accept` (operatorId). |
| Production prepare       | No separate endpoint; state PENDING_SHIP; could add PATCH body or extend accept. |
| Logistics arrange         | Part of ship: POST `/{id}/ship` (ShipActionRequest: entrustMatter, shipDate, quantity, model, needPackaging, pickup*, receiver*, deliveryAddress, carrier, ticketUrl). |
| Warehouse outbound (发运) | Yes: POST `/{id}/ship` (ShipActionRequest). |
| Carrier tracking          | No endpoint; entity has `logisticsNo`; could add PATCH/POST `/{id}/tracking` or extend ship/complete. |

**Status history/audit:** Entity has `acceptedAt`, `shippedAt`, `completedAt` (timestamps only). No stored operatorId per transition; operatorId is request param only.

---

## 3) Regression watchlist

- `/shipments` list still loads; filters/pagination unchanged.
- Drawer detail still loads even if new step forms fail; workflow error message does not break detail.
- GET `/{id}/workflow` actions remain consistent with `ShipmentService.require()` rules.
- Export 运单/回单 and “发起售后” in Drawer not broken.
- No new required fields that block existing create flows: standalone create (POST with customerId/shipmentType), create-from-quote, create-normal.

---

## 4) Build/test status

- **Backend:** `./mvnw -q package -DskipTests` — run locally to confirm PASS.
- **Frontend:** `npm run build` — currently FAIL due to existing TS errors in other files (see TEST_BASELINE); `src/pages/Shipment/index.tsx` is clean.
- **Failing tests:** Not run; no test name list.
