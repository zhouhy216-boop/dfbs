# API_SURFACE — Important API endpoints

**Facts only.** METHOD PATH → controller (class path). Base path for backend: `/api` (frontend `request` uses `baseURL: '/api'`). No guesswork; where not verified: "Unknown (not verified)" and path to controller file.

---

## Coverage boundary

- **How endpoints were enumerated**: Scanned all `*Controller.java` under `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/` (and subpackages) for `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`; combined class-level path with method-level path to produce METHOD + PATH.
- **Controller classes scanned**: 56 controller classes (as of enumeration).
- **Date of enumeration**: 2026-01-28.
- **Rule**: If new controllers or endpoints are added, this file must be updated via Evidence Update Ticket (after CEO acceptance + CEO BUILD SUCCESS). Do not assume this document is complete without re-scanning the interfaces tree.

---

## Auth

| Method | Path | Controller |
|--------|------|------------|
| POST | `/api/auth/login` | AuthController — `com.dfbs.app.interfaces.auth.AuthController` |

Verify: `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/auth/AuthController.java`

---

## Platform (orgs, applications, config)

| Method | Path | Controller |
|--------|------|------------|
| GET | `/api/v1/platform-orgs` | PlatformOrgController |
| GET | `/api/v1/platform-orgs/{id}` | PlatformOrgController |
| GET | `/api/v1/platform-orgs/platform/{platform}/customer/{customerId}` | PlatformOrgController |
| POST | `/api/v1/platform-orgs` | PlatformOrgController |
| PUT | `/api/v1/platform-orgs/{id}` | PlatformOrgController |
| POST | `/api/v1/platform-account-applications/create` | PlatformAccountApplicationController |
| GET | `/api/v1/platform-account-applications/page` | PlatformAccountApplicationController |
| GET | `/api/v1/platform-account-applications/{id}` | PlatformAccountApplicationController |
| GET | `/api/v1/platform-account-applications/check-customer-name` | PlatformAccountApplicationController |
| POST | `/api/v1/platform-account-applications/check-duplicates` | PlatformAccountApplicationController |
| GET | `/api/v1/platform-account-applications/check-org-match` | PlatformAccountApplicationController |
| PUT | `/api/v1/platform-account-applications/{id}/planner-submit` | PlatformAccountApplicationController |
| POST | `/api/v1/platform-account-applications/{id}/approve` | PlatformAccountApplicationController |
| POST | `/api/v1/platform-account-applications/{id}/reject` | PlatformAccountApplicationController |
| POST | `/api/v1/platform-account-applications/{id}/close` | PlatformAccountApplicationController |
| GET | `/api/v1/platform-configs/options` | PlatformConfigController |
| GET | `/api/v1/platform-configs/{code}/rules` | PlatformConfigController |
| GET | `/api/v1/platform-configs` | PlatformConfigController |
| GET | `/api/v1/platform-configs/{id}` | PlatformConfigController |
| POST | `/api/v1/platform-configs` | PlatformConfigController |
| PUT | `/api/v1/platform-configs/{id}` | PlatformConfigController |
| PATCH | `/api/v1/platform-configs/{id}/toggle` | PlatformConfigController |

Controller paths: `interfaces/platformorg/PlatformOrgController.java`, `interfaces/platformaccount/PlatformAccountApplicationController.java`, `interfaces/platformconfig/PlatformConfigController.java`.

---

## Quotes (CRUD, workflow, items, payment, void, export, dictionary)

| Method | Path | Controller |
|--------|------|------------|
| POST | `/api/v1/quotes` | QuoteController |
| PUT | `/api/v1/quotes/{id}` | QuoteController |
| POST | `/api/v1/quotes/{id}/confirm` | QuoteController |
| POST | `/api/v1/quotes/{id}/cancel` | QuoteController |
| POST | `/api/v1/quotes/{id}/submit` | QuoteController |
| GET | `/api/v1/quotes` | QuoteController |
| GET | `/api/v1/quotes/{id}` | QuoteController |
| GET | `/api/v1/quotes/my-pending` | QuoteController |
| POST | `/api/v1/quotes/{quoteId}/items` | QuoteItemController |
| PUT | `/api/v1/quotes/items/{itemId}` | QuoteItemController |
| DELETE | `/api/v1/quotes/items/{itemId}` | QuoteItemController |
| GET | `/api/v1/quotes/{quoteId}/items` | QuoteItemController |
| POST | `/api/v1/quotes/workflow/{quoteId}/submit` | QuoteWorkflowController |
| POST | `/api/v1/quotes/workflow/{quoteId}/audit` | QuoteWorkflowController |
| POST | `/api/v1/quotes/workflow/{quoteId}/assign-collector` | QuoteWorkflowController |
| GET | `/api/v1/quotes/workflow/{quoteId}/history` | QuoteWorkflowController |
| POST | `/api/v1/quotes/workflow/fallback` | QuoteWorkflowController |
| POST | `/api/v1/quotes/void/apply` | QuoteVoidController |
| POST | `/api/v1/quotes/void/audit` | QuoteVoidController |
| POST | `/api/v1/quotes/void/direct` | QuoteVoidController |
| POST | `/api/v1/quotes/void/direct-void` | QuoteVoidController |
| GET | `/api/v1/quotes/void/history/{quoteId}` | QuoteVoidController |
| GET | `/api/v1/quotes/void/requests/{quoteId}` | QuoteVoidController |
| POST | `/api/v1/payments/submit` | quote.payment.PaymentController |
| POST | `/api/v1/payments/{paymentId}/confirm` | quote.payment.PaymentController |
| POST | `/api/v1/payments/batch` | quote.payment.PaymentController |
| GET | `/api/v1/payments` | quote.payment.PaymentController |
| GET | `/api/v1/payments/quote/{quoteId}` | quote.payment.PaymentController |
| GET | `/api/v1/payments/{paymentId}` | quote.payment.PaymentController |
| GET | `/api/v1/dictionary` | DictionaryController |

More quote-related endpoints: see `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/quote/` (QuoteExportController, QuoteVersionController, WorkOrderQuoteController, payment/PaymentMethodController).

---

## Shipments

| Method | Path | Controller |
|--------|------|------------|
| POST | `/api/v1/shipments` | ShipmentController |
| POST | `/api/v1/shipments/create-normal` | ShipmentController |
| POST | `/api/v1/shipments/create-entrust` | ShipmentController |
| POST | `/api/v1/shipments/{id}/machines` | ShipmentController |
| POST | `/api/v1/shipments/parse-text` | ShipmentController |
| POST | `/api/v1/shipments/infer-type` | ShipmentController |
| GET | `/api/v1/shipments/{id}/export-ticket` | ShipmentController |
| GET | `/api/v1/shipments/{id}/export-receipt` | ShipmentController |
| POST | `/api/v1/shipments/create-from-quote` | ShipmentController |
| GET | `/api/v1/shipments` | ShipmentController |
| GET | `/api/v1/shipments/{id}` | ShipmentController |
| GET | `/api/v1/shipments/{id}/machines` | ShipmentController |
| POST | `/api/v1/shipments/{id}/accept` | ShipmentController |
| POST | `/api/v1/shipments/{id}/ship` | ShipmentController |
| POST | `/api/v1/shipments/{id}/complete` | ShipmentController |
| POST | `/api/v1/shipments/{id}/exception` | ShipmentController |
| POST | `/api/v1/shipments/{id}/cancel` | ShipmentController |

DamageController (no class-level RequestMapping; full path on each method):

| Method | Path | Controller |
|--------|------|------------|
| POST | `/api/v1/damages` | DamageController |
| GET | `/api/v1/shipments/{shipmentId}/damages` | DamageController |
| PUT | `/api/v1/damages/{id}/repair-stage` | DamageController |
| PUT | `/api/v1/damages/{id}/compensation` | DamageController |
| GET | `/api/v1/damages/config/types` | DamageController |
| GET | `/api/v1/damages/config/treatments` | DamageController |

Verify: `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/shipment/ShipmentController.java`, `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/damage/DamageController.java`.

---

## After-sales, work orders (internal + public)

| Method | Path | Controller |
|--------|------|------------|
| POST | `/api/v1/after-sales` | AfterSalesController |
| PUT | `/api/v1/after-sales/{id}` | AfterSalesController |
| POST | `/api/v1/after-sales/{id}/submit` | AfterSalesController |
| POST | `/api/v1/after-sales/{id}/receive` | AfterSalesController |
| POST | `/api/v1/after-sales/{id}/process` | AfterSalesController |
| POST | `/api/v1/after-sales/{id}/send-back` | AfterSalesController |
| POST | `/api/v1/after-sales/{id}/complete` | AfterSalesController |
| GET | `/api/v1/after-sales` | AfterSalesController |
| GET | `/api/v1/after-sales/{id}` | AfterSalesController |
| POST | `/api/v1/work-orders/create-from-quote` | WorkOrderController |
| POST | `/api/v1/work-orders/create` | WorkOrderController |
| POST | `/api/v1/work-orders/reject` | WorkOrderController |
| POST | `/api/v1/work-orders/accept-by-dispatcher` | WorkOrderController |
| POST | `/api/v1/work-orders/dispatch` | WorkOrderController |
| POST | `/api/v1/work-orders/accept` | WorkOrderController |
| POST | `/api/v1/work-orders/record` | WorkOrderController |
| POST | `/api/v1/work-orders/parts/add` | WorkOrderController |
| POST | `/api/v1/work-orders/parts/consume` | WorkOrderController |
| POST | `/api/v1/work-orders/sign` | WorkOrderController |
| POST | `/api/v1/work-orders/complete` | WorkOrderController |
| GET | `/api/v1/work-orders/pool` | WorkOrderController |
| GET | `/api/v1/work-orders/my-orders` | WorkOrderController |
| GET | `/api/v1/work-orders/{id}` | WorkOrderController |
| POST | `/api/v1/public/work-orders/create` | WorkOrderPublicController |

Verify: `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/aftersales/AfterSalesController.java`, `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/workorder/WorkOrderController.java`, `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/workorder/WorkOrderPublicController.java`.

---

## Master data (contracts, machines, models, spare parts, sim cards, model-part-lists, parts, BOM)

| Method | Path | Controller |
|--------|------|------------|
| GET | `/api/v1/masterdata/contracts` | ContractController |
| GET | `/api/v1/masterdata/contracts/{id}` | ContractController |
| POST | `/api/v1/masterdata/contracts` | ContractController |
| PUT | `/api/v1/masterdata/contracts/{id}` | ContractController |
| POST | `/api/v1/masterdata/contracts/{id}/disable` | ContractController |
| GET | `/api/v1/masterdata/machine-models` | MachineModelController |
| GET | `/api/v1/masterdata/machine-models/{id}` | MachineModelController |
| POST | `/api/v1/masterdata/machine-models` | MachineModelController |
| PUT | `/api/v1/masterdata/machine-models/{id}` | MachineModelController |
| POST | `/api/v1/masterdata/machine-models/{id}/disable` | MachineModelController |
| GET | `/api/v1/masterdata/spare-parts` | SparePartController |
| GET | `/api/v1/masterdata/spare-parts/{id}` | SparePartController |
| POST | `/api/v1/masterdata/spare-parts` | SparePartController |
| PUT | `/api/v1/masterdata/spare-parts/{id}` | SparePartController |
| POST | `/api/v1/masterdata/spare-parts/{id}/disable` | SparePartController |
| GET | `/api/v1/masterdata/machines` | MachineController |
| GET | `/api/v1/masterdata/machines/{id}` | MachineController |
| GET | `/api/v1/masterdata/machines/{id}/history` | MachineController |
| POST | `/api/v1/masterdata/machines` | MachineController |
| PUT | `/api/v1/masterdata/machines/{id}` | MachineController |
| POST | `/api/v1/masterdata/machines/{id}/disable` | MachineController |
| GET | `/api/v1/masterdata/sim-cards` | SimCardController |
| GET | `/api/v1/masterdata/sim-cards/{id}` | SimCardController |
| GET | `/api/v1/masterdata/sim-cards/{id}/history` | SimCardController |
| POST | `/api/v1/masterdata/sim-cards` | SimCardController |
| PUT | `/api/v1/masterdata/sim-cards/{id}` | SimCardController |
| POST | `/api/v1/masterdata/sim-cards/{id}/disable` | SimCardController |
| GET | `/api/v1/masterdata/model-part-lists` | ModelPartListController |
| GET | `/api/v1/masterdata/model-part-lists/{id}` | ModelPartListController |
| POST | `/api/v1/masterdata/model-part-lists` | ModelPartListController |
| PUT | `/api/v1/masterdata/model-part-lists/{id}` | ModelPartListController |
| POST | `/api/v1/masterdata/model-part-lists/{id}/disable` | ModelPartListController |
| POST | `/api/v1/masterdata/model-part-lists/draft` | ModelPartListController |
| POST | `/api/v1/masterdata/model-part-lists/{id}/publish` | ModelPartListController |
| GET | `/api/v1/masterdata/model-part-lists/{id}/conflicts` | ModelPartListController |
| POST | `/api/v1/masterdata/model-part-lists/conflicts/{conflictId}/resolve` | ModelPartListController |
| GET | `/api/v1/parts` | PartController |
| GET | `/api/v1/parts/active` | PartController |
| GET | `/api/v1/parts/search` | PartController |
| POST | `/api/v1/parts/import` | PartController |
| GET | `/api/v1/parts/bom/{productId}` | PartController |
| POST | `/api/v1/bom/import` | BomController |
| GET | `/api/v1/bom/machine/{machineId}/active` | BomController |
| GET | `/api/v1/bom/machine/{machineId}/history` | BomController |
| GET | `/api/v1/bom/version/{versionId}/items` | BomController |

Customer/product/ICCID (different base paths):

| Method | Path | Controller |
|--------|------|------------|
| POST | `/api/masterdata/customers` | CustomerMasterDataController |
| GET | `/api/masterdata/customers/{id}` | CustomerMasterDataController |
| PATCH | `/api/masterdata/customers/{id}` | CustomerMasterDataController |
| DELETE | `/api/masterdata/customers/{id}` | CustomerMasterDataController |
| POST | `/api/masterdata/customers/merge` | CustomerMasterDataController |
| POST | `/api/masterdata/customers/merge/{logId}/undo` | CustomerMasterDataController |
| GET | `/api/v1/customers` | CustomerMasterDataController |
| GET | `/api/masterdata/iccid` | IccidMasterDataController |
| POST | `/api/masterdata/iccid` | IccidMasterDataController |
| POST | `/api/masterdata/products` | ProductMasterDataController |
| GET | `/api/v1/products` | ProductMasterDataController |

Verify: `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/masterdata/*.java`, `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/bom/BomController.java`, `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/customer/CustomerMasterDataController.java`, `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/iccid/IccidMasterDataController.java`, `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/product/ProductMasterDataController.java`.

---

## Smart-select and temp-pool (confirmation)

| Method | Path | Controller |
|--------|------|------------|
| GET | `/api/v1/smart-select/search` | SmartSelectController |
| POST | `/api/v1/smart-select/get-or-create-temp` | SmartSelectController |
| GET | `/api/v1/temp-pool` | ConfirmationController |
| POST | `/api/v1/temp-pool/confirm` | ConfirmationController |
| POST | `/api/v1/temp-pool/reject` | ConfirmationController |

Verify: `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/smartselect/SmartSelectController.java`, `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/smartselect/ConfirmationController.java`.

---

## Imports (bulk upload + resolve)

| Method | Path | Controller |
|--------|------|------------|
| POST | `/api/v1/imports/customers` (multipart/form-data) | ImportController |
| POST | `/api/v1/imports/customers/resolve` | ImportController |
| POST | `/api/v1/imports/contracts` (multipart/form-data) | ImportController |
| POST | `/api/v1/imports/contracts/resolve` | ImportController |
| POST | `/api/v1/imports/models` (multipart/form-data) | ImportController |
| POST | `/api/v1/imports/models/resolve` | ImportController |
| POST | `/api/v1/imports/spare-parts` (multipart/form-data) | ImportController |
| POST | `/api/v1/imports/spare-parts/resolve` | ImportController |
| POST | `/api/v1/imports/machines` (multipart/form-data) | ImportController |
| POST | `/api/v1/imports/machines/resolve` | ImportController |
| POST | `/api/v1/imports/sim-cards` (multipart/form-data) | ImportController |
| POST | `/api/v1/imports/sim-cards/resolve` | ImportController |
| POST | `/api/v1/imports/model-part-lists` (multipart/form-data) | ImportController |
| POST | `/api/v1/imports/model-part-lists/resolve` | ImportController |

Verify: `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/importdata/ImportController.java`.

---

## Inventory, warehouse, outbound, transfer, replenish

| Method | Path | Controller |
|--------|------|------------|
| GET | `/api/v1/inventory/stock` | InventoryController |
| POST | `/api/v1/inventory/inbound` | InventoryController |
| POST | `/api/v1/inventory/return` | InventoryController |
| GET | `/api/v1/inventory/logs` | InventoryController |
| GET | `/api/v1/warehouse/warehouses` | WhStockController |
| POST | `/api/v1/warehouse/inbound` | WhStockController |
| POST | `/api/v1/warehouse/outbound` | WhStockController |
| GET | `/api/v1/warehouse/inventory` | WhStockController |
| POST | `/api/v1/outbound/wo` | OutboundController |
| POST | `/api/v1/outbound/quote` | OutboundController |
| POST | `/api/v1/outbound/special/apply` | OutboundController |
| POST | `/api/v1/outbound/special/approve` | OutboundController |
| POST | `/api/v1/outbound/special/confirm` | OutboundController |
| GET | `/api/v1/outbound/validate-warehouse` | OutboundController |
| POST | `/api/v1/transfer/apply` | TransferController |
| POST | `/api/v1/transfer/ship` | TransferController |
| POST | `/api/v1/transfer/receive` | TransferController |
| POST | `/api/v1/warehouse/replenish/create` | WhReplenishController |
| POST | `/api/v1/warehouse/replenish/approve-l1` | WhReplenishController |
| POST | `/api/v1/warehouse/replenish/approve-l2` | WhReplenishController |
| GET | `/api/v1/warehouse/replenish/my-pending` | WhReplenishController |
| GET | `/api/v1/warehouse/replenish/my-requests` | WhReplenishController |

Verify: `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/inventory/*.java`, `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/warehouse/*.java`.

---

## Payments (general), statements, freight, expenses, claims, trip requests, corrections

| Method | Path | Controller |
|--------|------|------------|
| POST | `/api/v1/general-payments` | payment.PaymentController |
| POST | `/api/v1/general-payments/{id}/confirm` | payment.PaymentController |
| POST | `/api/v1/general-payments/{id}/cancel` | payment.PaymentController |
| POST | `/api/v1/statements/generate` | AccountStatementController |
| DELETE | `/api/v1/statements/{id}/items/{quoteId}` | AccountStatementController |
| GET | `/api/v1/statements/{id}/export` | AccountStatementController |
| GET | `/api/v1/statements/list` | AccountStatementController |
| GET | `/api/v1/statements/{id}` | AccountStatementController |
| POST | `/api/v1/statements/{id}/bind-payments` | StatementReconcileController |
| GET | `/api/v1/freight-bills/available-shipments` | FreightBillController |
| POST | `/api/v1/freight-bills/create` | FreightBillController |
| POST | `/api/v1/freight-bills/create-by-period` | FreightBillController |
| POST | `/api/v1/freight-bills/export` | FreightBillController |
| PUT | `/api/v1/freight-bills/{id}/items` | FreightBillController |
| POST | `/api/v1/freight-bills/{id}/remove-shipment/{shipmentId}` | FreightBillController |
| POST | `/api/v1/freight-bills/{id}/confirm` | FreightBillController |
| POST | `/api/v1/freight-bills/{id}/settle` | FreightBillController |
| GET | `/api/v1/freight-bills/{id}/export` | FreightBillController |
| POST | `/api/v1/expenses` | ExpenseController |
| PUT | `/api/v1/expenses/{id}` | ExpenseController |
| POST | `/api/v1/expenses/{id}/void` | ExpenseController |
| GET | `/api/v1/expenses` | ExpenseController |
| POST | `/api/v1/stats/expenses` | ExpenseStatsController |
| GET | `/api/v1/stats/expenses/export` | ExpenseStatsController |
| POST | `/api/v1/claims` | ClaimController |
| POST | `/api/v1/claims/{id}/submit` | ClaimController |
| POST | `/api/v1/claims/{id}/return` | ClaimController |
| POST | `/api/v1/claims/{id}/reject` | ClaimController |
| POST | `/api/v1/claims/{id}/approve` | ClaimController |
| POST | `/api/v1/claims/{id}/pay` | ClaimController |
| GET | `/api/v1/claims` | ClaimController |
| POST | `/api/v1/trip-requests` | TripRequestController |
| PUT | `/api/v1/trip-requests/{id}` | TripRequestController |
| POST | `/api/v1/trip-requests/{id}/submit` | TripRequestController |
| POST | `/api/v1/trip-requests/{id}/withdraw` | TripRequestController |
| POST | `/api/v1/trip-requests/{id}/leader-approve` | TripRequestController |
| POST | `/api/v1/trip-requests/{id}/finance-approve` | TripRequestController |
| POST | `/api/v1/trip-requests/{id}/return` | TripRequestController |
| POST | `/api/v1/trip-requests/{id}/reject` | TripRequestController |
| POST | `/api/v1/trip-requests/{id}/cancel-request` | TripRequestController |
| POST | `/api/v1/trip-requests/{id}/cancel-approve` | TripRequestController |
| POST | `/api/v1/trip-requests/{id}/cancel-reject` | TripRequestController |
| GET | `/api/v1/trip-requests` | TripRequestController |
| POST | `/api/v1/corrections` | CorrectionController |
| POST | `/api/v1/corrections/{id}/submit` | CorrectionController |
| POST | `/api/v1/corrections/{id}/approve` | CorrectionController |
| POST | `/api/v1/corrections/{id}/reject` | CorrectionController |
| GET | `/api/v1/corrections/{id}` | CorrectionController |

Verify: `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/payment/PaymentController.java`, `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/statement/*.java`, `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/freightbill/FreightBillController.java`, `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/expense/*.java`, `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/triprequest/TripRequestController.java`, `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/correction/CorrectionController.java`.

---

## Attachments, notifications, permission-requests, carriers, contract-prices, invoice-applications, repair, warehouse-config, business-lines, payment-methods

| Method | Path | Controller |
|--------|------|------------|
| POST | `/api/v1/attachments/upload` | AttachmentController |
| GET | `/api/v1/notifications` | NotificationController |
| GET | `/api/v1/notifications/my` | NotificationController |
| GET | `/api/v1/notifications/my/unread` | NotificationController |
| GET | `/api/v1/notifications/my/unread-count` | NotificationController |
| POST | `/api/v1/notifications/{id}/read` | NotificationController |
| POST | `/api/v1/notifications/read-all` | NotificationController |
| PUT | `/api/v1/notifications/preference` | NotificationController |
| POST | `/api/v1/permission-requests/apply` | PermissionRequestController |
| POST | `/api/v1/permission-requests/{id}/resubmit` | PermissionRequestController |
| POST | `/api/v1/permission-requests/{id}/return` | PermissionRequestController |
| POST | `/api/v1/permission-requests/{id}/reject` | PermissionRequestController |
| POST | `/api/v1/permission-requests/{id}/approve` | PermissionRequestController |
| GET | `/api/v1/permission-requests/my-requests` | PermissionRequestController |
| GET | `/api/v1/permission-requests/pending` | PermissionRequestController |
| GET | `/api/v1/carriers` | CarrierController |
| GET | `/api/v1/carriers/recommend` | CarrierController |
| GET | `/api/v1/carriers/{id}` | CarrierController |
| POST | `/api/v1/carriers` | CarrierController |
| PUT | `/api/v1/carriers/{id}` | CarrierController |
| DELETE | `/api/v1/carriers/{id}` | CarrierController |
| POST | `/api/v1/contract-prices` | ContractPriceController |
| PUT | `/api/v1/contract-prices/{id}` | ContractPriceController |
| POST | `/api/v1/contract-prices/{id}/deactivate` | ContractPriceController |
| POST | `/api/v1/invoice-applications/submit` | InvoiceApplicationController |
| POST | `/api/v1/invoice-applications/audit` | InvoiceApplicationController |
| GET | `/api/v1/invoice-applications/my-applications` | InvoiceApplicationController |
| GET | `/api/v1/invoice-applications/{applicationId}` | InvoiceApplicationController |
| POST | `/api/v1/repair-records/import` | RepairRecordController |
| GET | `/api/v1/repair-records` | RepairRecordController |
| GET | `/api/v1/repair-records/template` | RepairRecordController |
| GET | `/api/v1/warehouse-config/user-ids` | WarehouseConfigController |
| PUT | `/api/v1/warehouse-config/user-ids` | WarehouseConfigController |
| POST | `/api/v1/business-lines` | BusinessLineController |
| PUT | `/api/v1/business-lines/{id}` | BusinessLineController |
| GET | `/api/v1/business-lines` | BusinessLineController |
| GET | `/api/v1/business-lines/active` | BusinessLineController |
| GET | `/api/v1/business-lines/{id}` | BusinessLineController |
| GET | `/api/v1/payment-methods` | PaymentMethodController |
| GET | `/api/v1/payment-methods/active` | PaymentMethodController |

Full list of controller classes: `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/` (enumerate .java files there).
