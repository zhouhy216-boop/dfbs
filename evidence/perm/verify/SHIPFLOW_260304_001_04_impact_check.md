# SHIPFLOW-260304-001-04 Impact Check (Sign-off + Close) — Facts Only

**Request ID:** SHIPFLOW-260304-001-04-IMP  
**Scope:** Step-04 (sign-off + close) — impact + regression checklist before implementation.

---

## 1) Facts to confirm (evidence)

- **ShipmentStatus enum** (`modules/shipment/ShipmentStatus.java`): `CREATED`, `PENDING_SHIP`, `SHIPPED`, `COMPLETED`, `EXCEPTION`, `CANCELLED`. **No `CLOSED`**; `COMPLETED` is the terminal “delivered / signed” state per Javadoc.
- **Meaning of `complete()`** (`ShipmentService.java` ~541–552): Single transition **SHIPPED → COMPLETED**; sets `completedAt`; for type NORMAL, validates `receiptUrl` via `AttachmentRuleService` (COMPLETE_RECEIPT) before setting status. So it **is** “signed-off and closed” in one step; there is no separate “sign-off” then “close.”
- **Separate close/sign-off endpoint:** **None.** Only `POST /api/v1/shipments/{id}/complete` (`ShipmentController.java` ~219–222); no dedicated “close” or “sign-off” endpoint.
- **Fields usable as v1 prerequisites for closing:** `ShipmentEntity`: `shippedAt` (set on ship), `completedAt` (set in complete), `receiptUrl` (required for NORMAL in complete via ruleService), `logisticsNo` (optional). So v1 “can close” = status SHIPPED + (for NORMAL) receiptUrl present and valid; `completedAt` is the closure timestamp.

---

## 2) Likely impacted areas

- **Backend:** `ShipmentStatus` (only if adding CLOSED or renaming; currently COMPLETED = closed). `ShipmentService.complete()` and any new “close”/prerequisite logic; `ShipmentController` if new endpoint or change to complete; workflow step/action mapping (e.g. COMPLETED → “签收完成”/SIGN_CLOSE) in `ShipmentService` allowedActions/stepLabelCn.
- **Frontend:** `pages/Shipment/index.tsx` — “签收完成” button and any new close/sign-off action; status labels (valueEnum/stepLabelCn); permission keys if a new action is added (e.g. `shipment.shipments:COMPLETE` already exists).

---

## 3) Regression watchlist

- No breaking change to existing statuses/transitions used by Step-01/02 (CREATED→PENDING_SHIP→SHIPPED→COMPLETED; EXCEPTION/CANCELLED).
- Step-03 permission gating unchanged: VIEW + COMPLETE (and other action keys) still enforced; no new endpoint without corresponding perm.
- Ability to reach closure: ensure NORMAL still requires receiptUrl (or documented exception); no new blocking that prevents COMPLETED when prerequisites are met.

---

## 4) Build/test status

- **Backend:** `.\mvnw.cmd -q package -DskipTests` — run locally to confirm PASS.
- **Frontend:** `npm run build` — currently FAIL due to existing TS errors in other files; `Shipment/index.tsx` unaffected.
- **Tests affected:** `ShipmentProcessTest`, `ShipmentPanoramaTest`, `ShipmentControllerPermissionTest` (if complete/close logic or workflow changes); attachment/export tests if receipt/close rules change.
