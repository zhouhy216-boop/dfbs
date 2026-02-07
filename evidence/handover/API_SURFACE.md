# API_SURFACE — REST endpoints by controller

**Facts only.** Enumerated from `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/` (all *Controller.java). Base: frontend uses `baseURL: '/api'`.

---

## AuthController — `interfaces/auth/AuthController.java`

| Method | Path |
|--------|------|
| POST | /api/auth/login |

---

## PlatformOrgController — `interfaces/platformorg/PlatformOrgController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/platform-orgs |
| GET | /api/v1/platform-orgs/{id} |
| GET | /api/v1/platform-orgs/platform/{platform}/customer/{customerId} |
| POST | /api/v1/platform-orgs |
| PUT | /api/v1/platform-orgs/{id} |

---

## PlatformAccountApplicationController — `interfaces/platformaccount/PlatformAccountApplicationController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/platform-account-applications/create |
| GET | /api/v1/platform-account-applications/page |
| GET | /api/v1/platform-account-applications/{id} |
| GET | /api/v1/platform-account-applications/check-customer-name |
| POST | /api/v1/platform-account-applications/check-duplicates |
| GET | /api/v1/platform-account-applications/check-org-match |
| PUT | /api/v1/platform-account-applications/{id}/planner-submit |
| POST | /api/v1/platform-account-applications/{id}/approve |
| POST | /api/v1/platform-account-applications/{id}/reject |
| POST | /api/v1/platform-account-applications/{id}/close |

---

## PlatformConfigController — `interfaces/platformconfig/PlatformConfigController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/platform-configs/options |
| GET | /api/v1/platform-configs/{code}/rules |
| GET | /api/v1/platform-configs |
| GET | /api/v1/platform-configs/{id} |
| POST | /api/v1/platform-configs |
| PUT | /api/v1/platform-configs/{id} |

---

## QuoteController, QuoteItemController — `interfaces/quote/QuoteController.java`, QuoteItemController.java

| Method | Path |
|--------|------|
| POST | /api/v1/quotes |
| PUT | /api/v1/quotes/{id} |
| POST | /api/v1/quotes/{id}/confirm |
| POST | /api/v1/quotes/{id}/cancel |
| POST | /api/v1/quotes/{id}/submit |
| GET | /api/v1/quotes |
| GET | /api/v1/quotes/{id} |
| GET | /api/v1/quotes/my-pending |
| POST | /api/v1/quotes/{quoteId}/items |
| PUT | /api/v1/quotes/items/{itemId} |
| DELETE | /api/v1/quotes/items/{itemId} |
| GET | /api/v1/quotes/{quoteId}/items |

---

## QuoteWorkflowController — `interfaces/quote/workflow/QuoteWorkflowController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/quotes/workflow/{quoteId}/submit |
| POST | /api/v1/quotes/workflow/{quoteId}/audit |
| POST | /api/v1/quotes/workflow/{quoteId}/assign-collector |
| GET | /api/v1/quotes/workflow/{quoteId}/history |
| POST | /api/v1/quotes/workflow/fallback |

---

## QuoteVoidController — `interfaces/quote/void/QuoteVoidController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/quotes/void/apply |
| POST | /api/v1/quotes/void/audit |
| POST | /api/v1/quotes/void/direct, /api/v1/quotes/void/direct-void |
| GET | /api/v1/quotes/void/history/{quoteId} |
| GET | /api/v1/quotes/void/requests/{quoteId} |

---

## PaymentController (quote) — `interfaces/quote/payment/PaymentController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/payments/submit |
| POST | /api/v1/payments/{paymentId}/confirm |
| POST | /api/v1/payments/batch |
| GET | /api/v1/payments |
| GET | /api/v1/payments/quote/{quoteId} |
| GET | /api/v1/payments/{paymentId} |

---

## PermissionRequestController — `interfaces/permission/PermissionRequestController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/permission-requests/apply |
| POST | /api/v1/permission-requests/{id}/resubmit |
| POST | /api/v1/permission-requests/{id}/return |
| POST | /api/v1/permission-requests/{id}/reject |
| POST | /api/v1/permission-requests/{id}/approve |
| GET | /api/v1/permission-requests/my-requests |
| GET | /api/v1/permission-requests/pending |

---

## WorkOrderPublicController — `interfaces/workorder/WorkOrderPublicController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/public/work-orders/create |

---

## ExpenseStatsController — `interfaces/expense/ExpenseStatsController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/stats/expenses |
| GET | /api/v1/stats/expenses/export |

---

## SparePartController — `interfaces/masterdata/SparePartController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/masterdata/spare-parts |
| GET | /api/v1/masterdata/spare-parts/{id} |
| POST | /api/v1/masterdata/spare-parts |
| PUT | /api/v1/masterdata/spare-parts/{id} |
| POST | /api/v1/masterdata/spare-parts/{id}/disable |

---

## NotificationController — `interfaces/notification/NotificationController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/notifications |
| GET | /api/v1/notifications/my |
| GET | /api/v1/notifications/my/unread |
| GET | /api/v1/notifications/my/unread-count |
| POST | /api/v1/notifications/{id}/read |
| POST | /api/v1/notifications/read-all |
| PUT | /api/v1/notifications/preference |

---

## CorrectionController — `interfaces/correction/CorrectionController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/corrections |
| POST | /api/v1/corrections/{id}/submit |
| POST | /api/v1/corrections/{id}/approve |
| POST | /api/v1/corrections/{id}/reject |
| GET | /api/v1/corrections/{id} |

---

## ExpenseController — `interfaces/expense/ExpenseController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/expenses |
| PUT | /api/v1/expenses/{id} |
| POST | /api/v1/expenses/{id}/void |
| GET | /api/v1/expenses |

---

## InventoryController — `interfaces/inventory/InventoryController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/inventory/stock |
| POST | /api/v1/inventory/inbound |
| POST | /api/v1/inventory/return |
| GET | /api/v1/inventory/logs |

---

## TransferController — `interfaces/inventory/TransferController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/transfer/apply |
| POST | /api/v1/transfer/ship |
| POST | /api/v1/transfer/receive |

---

## CarrierController — `interfaces/carrier/CarrierController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/carriers |
| GET | /api/v1/carriers/recommend |
| GET | /api/v1/carriers/{id} |
| POST | /api/v1/carriers |
| PUT | /api/v1/carriers/{id} |
| DELETE | /api/v1/carriers/{id} |

---

## InvoiceApplicationController — `interfaces/invoice/InvoiceApplicationController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/invoice-applications/submit |
| POST | /api/v1/invoice-applications/audit |
| GET | /api/v1/invoice-applications/my-applications |
| GET | /api/v1/invoice-applications/{applicationId} |

---

## DamageController — `interfaces/damage/DamageController.java` (method-level paths)

| Method | Path |
|--------|------|
| POST | /api/v1/damages |
| GET | /api/v1/shipments/{shipmentId}/damages |
| PUT | /api/v1/damages/{id}/repair-stage |
| PUT | /api/v1/damages/{id}/compensation |
| GET | /api/v1/damages/config/types |
| GET | /api/v1/damages/config/treatments |

---

## IccidMasterDataController — `interfaces/iccid/IccidMasterDataController.java`

| Method | Path |
|--------|------|
| GET | /api/masterdata/iccid |
| POST | /api/masterdata/iccid |

---

## MachineModelController — `interfaces/masterdata/MachineModelController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/masterdata/machine-models |
| GET | /api/v1/masterdata/machine-models/{id} |
| POST | /api/v1/masterdata/machine-models |
| PUT | /api/v1/masterdata/machine-models/{id} |
| POST | /api/v1/masterdata/machine-models/{id}/disable |

---

## ContractPriceController — `interfaces/contractprice/ContractPriceController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/contract-prices |
| PUT | /api/v1/contract-prices/{id} |
| POST | /api/v1/contract-prices/{id}/deactivate |

---

## RepairRecordController — `interfaces/repair/RepairRecordController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/repair-records/import |
| GET | /api/v1/repair-records |
| GET | /api/v1/repair-records/template |

---

## OutboundController — `interfaces/inventory/OutboundController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/outbound/wo |
| POST | /api/v1/outbound/quote |
| POST | /api/v1/outbound/special/apply |
| POST | /api/v1/outbound/special/approve |
| POST | /api/v1/outbound/special/confirm |
| GET | /api/v1/outbound/validate-warehouse |

---

## PartController — `interfaces/masterdata/PartController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/parts |
| GET | /api/v1/parts/active |
| GET | /api/v1/parts/search |
| POST | /api/v1/parts/import |
| GET | /api/v1/parts/bom/{productId} |

---

## WhStockController — `interfaces/warehouse/WhStockController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/warehouse/warehouses |
| POST | /api/v1/warehouse/inbound |
| POST | /api/v1/warehouse/outbound |
| GET | /api/v1/warehouse/inventory |

---

## ContractController — `interfaces/masterdata/ContractController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/masterdata/contracts |
| GET | /api/v1/masterdata/contracts/{id} |
| POST | /api/v1/masterdata/contracts |
| PUT | /api/v1/masterdata/contracts/{id} |
| POST | /api/v1/masterdata/contracts/{id}/disable |

---

## ProductMasterDataController — `interfaces/product/ProductMasterDataController.java` (method-level paths)

| Method | Path |
|--------|------|
| POST | /api/masterdata/products |
| GET | /api/v1/products |

---

## BomController — `interfaces/bom/BomController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/bom/import |
| GET | /api/v1/bom/machine/{machineId}/active |
| GET | /api/v1/bom/machine/{machineId}/history |
| GET | /api/v1/bom/version/{versionId}/items |

---

## AfterSalesController — `interfaces/aftersales/AfterSalesController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/after-sales |
| PUT | /api/v1/after-sales/{id} |
| POST | /api/v1/after-sales/{id}/submit |
| POST | /api/v1/after-sales/{id}/receive |
| POST | /api/v1/after-sales/{id}/process |
| POST | /api/v1/after-sales/{id}/send-back |
| POST | /api/v1/after-sales/{id}/complete |
| GET | /api/v1/after-sales |
| GET | /api/v1/after-sales/{id} |

---

## ImportController — `interfaces/importdata/ImportController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/imports/customers (multipart/form-data) |
| POST | /api/v1/imports/customers/resolve |
| POST | /api/v1/imports/contracts (multipart/form-data) |
| POST | /api/v1/imports/contracts/resolve |
| POST | /api/v1/imports/models (multipart/form-data) |
| POST | /api/v1/imports/models/resolve |
| POST | /api/v1/imports/spare-parts (multipart/form-data) |
| POST | /api/v1/imports/spare-parts/resolve |
| POST | /api/v1/imports/machines (multipart/form-data) |
| POST | /api/v1/imports/machines/resolve |
| POST | /api/v1/imports/sim-cards (multipart/form-data) |
| POST | /api/v1/imports/sim-cards/resolve |
| POST | /api/v1/imports/model-part-lists (multipart/form-data) |
| POST | /api/v1/imports/model-part-lists/resolve |

---

## PaymentController (general) — `interfaces/payment/PaymentController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/general-payments |
| POST | /api/v1/general-payments/{id}/confirm |
| POST | /api/v1/general-payments/{id}/cancel |

---

## WhReplenishController — `interfaces/warehouse/WhReplenishController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/warehouse/replenish/create |
| POST | /api/v1/warehouse/replenish/approve-l1 |
| POST | /api/v1/warehouse/replenish/approve-l2 |
| GET | /api/v1/warehouse/replenish/my-pending |
| GET | /api/v1/warehouse/replenish/my-requests |

---

## TripRequestController — `interfaces/triprequest/TripRequestController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/trip-requests |
| PUT | /api/v1/trip-requests/{id} |
| POST | /api/v1/trip-requests/{id}/submit |
| POST | /api/v1/trip-requests/{id}/withdraw |
| POST | /api/v1/trip-requests/{id}/leader-approve |
| POST | /api/v1/trip-requests/{id}/finance-approve |
| POST | /api/v1/trip-requests/{id}/return |
| POST | /api/v1/trip-requests/{id}/reject |
| POST | /api/v1/trip-requests/{id}/cancel-request |
| POST | /api/v1/trip-requests/{id}/cancel-approve |
| POST | /api/v1/trip-requests/{id}/cancel-reject |
| GET | /api/v1/trip-requests |

---

## ShipmentController — `interfaces/shipment/ShipmentController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/shipments |
| POST | /api/v1/shipments/create-normal |
| POST | /api/v1/shipments/create-entrust |
| POST | /api/v1/shipments/{id}/machines |
| POST | /api/v1/shipments/parse-text |
| POST | /api/v1/shipments/infer-type |
| GET | /api/v1/shipments/{id}/export-ticket |
| GET | /api/v1/shipments/{id}/export-receipt |
| POST | /api/v1/shipments/create-from-quote |
| GET | /api/v1/shipments |
| GET | /api/v1/shipments/{id} |
| GET | /api/v1/shipments/{id}/machines |
| POST | /api/v1/shipments/{id}/accept |
| POST | /api/v1/shipments/{id}/ship |
| POST | /api/v1/shipments/{id}/complete |
| POST | /api/v1/shipments/{id}/exception |
| POST | /api/v1/shipments/{id}/cancel |

---

## AccountStatementController, StatementReconcileController — `interfaces/statement/`

| Method | Path |
|--------|------|
| POST | /api/v1/statements/generate |
| DELETE | /api/v1/statements/{id}/items/{quoteId} |
| GET | /api/v1/statements/{id}/export |
| GET | /api/v1/statements/list |
| GET | /api/v1/statements/{id} |
| POST | /api/v1/statements/{id}/bind-payments |

---

## FreightBillController — `interfaces/freightbill/FreightBillController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/freight-bills/available-shipments |
| POST | /api/v1/freight-bills/create |
| POST | /api/v1/freight-bills/create-by-period |
| POST | /api/v1/freight-bills/export |
| PUT | /api/v1/freight-bills/{id}/items |
| POST | /api/v1/freight-bills/{id}/remove-shipment/{shipmentId} |
| POST | /api/v1/freight-bills/{id}/confirm |
| POST | /api/v1/freight-bills/{id}/settle |
| GET | /api/v1/freight-bills/{id}/export |

---

## AttachmentController — `interfaces/attachment/AttachmentController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/attachments/upload |

---

## ModelPartListController — `interfaces/masterdata/ModelPartListController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/masterdata/model-part-lists |
| GET | /api/v1/masterdata/model-part-lists/{id} |
| POST | /api/v1/masterdata/model-part-lists |
| PUT | /api/v1/masterdata/model-part-lists/{id} |
| POST | /api/v1/masterdata/model-part-lists/{id}/disable |
| POST | /api/v1/masterdata/model-part-lists/draft |
| POST | /api/v1/masterdata/model-part-lists/{id}/publish |
| GET | /api/v1/masterdata/model-part-lists/{id}/conflicts |
| POST | /api/v1/masterdata/model-part-lists/conflicts/{conflictId}/resolve |

---

## SimCardController — `interfaces/masterdata/SimCardController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/masterdata/sim-cards |
| GET | /api/v1/masterdata/sim-cards/{id} |
| GET | /api/v1/masterdata/sim-cards/{id}/history |
| POST | /api/v1/masterdata/sim-cards |
| PUT | /api/v1/masterdata/sim-cards/{id} |
| POST | /api/v1/masterdata/sim-cards/{id}/disable |

---

## MachineController — `interfaces/masterdata/MachineController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/masterdata/machines |
| GET | /api/v1/masterdata/machines/{id} |
| GET | /api/v1/masterdata/machines/{id}/history |
| POST | /api/v1/masterdata/machines |
| PUT | /api/v1/masterdata/machines/{id} |
| POST | /api/v1/masterdata/machines/{id}/disable |

---

## CustomerMasterDataController — `interfaces/customer/CustomerMasterDataController.java` (method-level paths)

| Method | Path |
|--------|------|
| POST | /api/masterdata/customers |
| GET | /api/masterdata/customers/{id} |
| DELETE | /api/masterdata/customers/{id} |
| POST | /api/masterdata/customers/merge |
| POST | /api/masterdata/customers/merge/{logId}/undo |
| GET | /api/v1/customers |

---

## SmartSelectController — `interfaces/smartselect/SmartSelectController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/smart-select/search |
| POST | /api/v1/smart-select/get-or-create-temp |

---

## ConfirmationController — `interfaces/smartselect/ConfirmationController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/temp-pool |
| POST | /api/v1/temp-pool/confirm |
| POST | /api/v1/temp-pool/reject |

---

## WarehouseConfigController — `interfaces/settings/WarehouseConfigController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/warehouse-config/user-ids |
| PUT | /api/v1/warehouse-config/user-ids |

---

## BusinessLineController — `interfaces/settings/BusinessLineController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/business-lines |
| PUT | /api/v1/business-lines/{id} |
| GET | /api/v1/business-lines |
| GET | /api/v1/business-lines/active |
| GET | /api/v1/business-lines/{id} |

---

## PaymentMethodController — `interfaces/quote/payment/PaymentMethodController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/payment-methods |
| GET | /api/v1/payment-methods/active |

---

## DictionaryController — `interfaces/quote/dictionary/DictionaryController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/dictionary/categories |
| GET | /api/v1/dictionary/fee-types |
| GET | /api/v1/dictionary/fee-types/active |
| GET | /api/v1/dictionary/units |

---

## WorkOrderQuoteController — `interfaces/quote/WorkOrderQuoteController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/quotes/from-workorder |

---

## QuoteExportController — `interfaces/quote/QuoteExportController.java`

| Method | Path |
|--------|------|
| GET | /api/v1/quotes/{id}/export |

---

## QuoteVersionController — `interfaces/quote/QuoteVersionController.java`

| Method | Path |
|--------|------|
| POST | /api/quote-versions/activate |

---

## WorkOrderController — `interfaces/workorder/WorkOrderController.java`

| Method | Path |
|--------|------|
| POST | /api/v1/work-orders/create-from-quote |
| POST | /api/v1/work-orders/create |
| POST | /api/v1/work-orders/reject |
| POST | /api/v1/work-orders/accept-by-dispatcher |
| POST | /api/v1/work-orders/dispatch |
| POST | /api/v1/work-orders/accept |
| POST | /api/v1/work-orders/record |
| POST | /api/v1/work-orders/parts/add |
| POST | /api/v1/work-orders/parts/consume |
| POST | /api/v1/work-orders/sign |
| POST | /api/v1/work-orders/complete |
| GET | /api/v1/work-orders/pool |
| GET | /api/v1/work-orders/my-orders |
| GET | /api/v1/work-orders/{id} |

Controller file paths: `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/` + subpackages. Rescan when controllers change.
