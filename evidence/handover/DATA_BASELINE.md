# DATA_BASELINE — DB migrations and core entities

**Facts only.** Unknowns marked "Unknown (not verified)".

---

## DB migrations list (ids / Flyway filenames / location)

Location: `backend/dfbs-app/src/main/resources/db/migration/`. Filenames are as on disk (Flyway format `Vxxxx__<name>.sql`).

| Id | Flyway filename |
|----|-----------------|
| V0001 | V0001__init.sql |
| V0002 | V0002__quote_version.sql |
| V0003 | V0003__quote_version_only_one_active.sql |
| V0004 | V0004__masterdata_init.sql |
| V0005 | V0005__quote_item.sql |
| V0006 | V0006__quote_header_and_sequence.sql |
| V0007 | V0007__quote_item_mvp.sql |
| V0008 | V0008__quote_workorder_fields.sql |
| V0009 | V0009__fee_dictionary_and_bom.sql |
| V0010 | V0010__payment_record.sql |
| V0011 | V0011__quote_void_process.sql |
| V0012 | V0012__quote_cc_leadership.sql |
| V0013 | V0013__quote_cc_warehouse.sql |
| V0014 | V0014__quote_payment_workflow.sql |
| V0015 | V0015__invoice_workflow.sql |
| V0016 | V0016__quote_void_process.sql |
| V0017 | V0017__quote_downstream.sql |
| V0018 | V0018__shipment_workflow.sql |
| V0019 | V0019__shipment_panorama.sql |
| V0020 | V0020__damage_record.sql |
| V0021 | V0021__freight_bill.sql |
| V0022 | V0022__inventory_mvp.sql |
| V0023 | V0023__repair_ledger.sql |
| V0024 | V0024__permission_request.sql |
| V0025 | V0025__notification_center.sql |
| V0026 | V0026__account_statement.sql |
| V0027 | V0027__bom_versioning.sql |
| V0028 | V0028__quote_part_link.sql |
| V0029 | V0029__machine_iccid_list_support.sql |
| V0030 | V0030__quote_standardization.sql |
| V0031 | V0031__customer_merge_support.sql |
| V0032 | V0032__expense_claim_mvp.sql |
| V0033 | V0033__trip_request_mvp.sql |
| V0034 | V0034__payment_allocation_mvp.sql |
| V0035 | V0035__contract_pricing_mvp.sql |
| V0036 | V0036__carrier_freight_bill_mvp.sql |
| V0037 | V0037__correction_mvp.sql |
| V0038 | V0038__after_sales_mvp.sql |
| V0039 | V0039__masterdata_six_pack.sql |
| V0040 | V0040__bom_conflicts_and_spare_part_aliases.sql |
| V0041 | V0041__quote_payment_created_at.sql |
| V0042 | V0042__warehouse_mvp.sql |
| V0043 | V0043__work_order_service_flow.sql |
| V0044 | V0044__work_order_cancellation_reason.sql |
| V0045 | V0045__work_order_refinement.sql |
| V0046 | V0046__app_user_username.sql |
| V0047 | V0047__app_user_seed_admin.sql |
| V0048 | V0048__smart_select_infrastructure.sql |
| V0049 | V0049__platform_org_master.sql |
| V0050 | V0050__platform_account_applications.sql |
| V0051 | V0051__refactor_platform_org.sql |
| V0052 | V0052__platform_application_source_and_customer_name.sql |
| V0053 | V0053__relax_application_constraints.sql |
| V0054 | V0054__enforce_org_uniqueness.sql |
| V0055 | V0055__org_source_tracking.sql |
| V0057 | V0057__drop_data_governance_task.sql |
| V0058 | V0058__add_reject_reason.sql |
| V0059 | V0059__platform_org_status.sql |
| V0060 | V0060__create_md_platform.sql |

(No V0056 in repo — gap present. Verify by listing `backend/dfbs-app/src/main/resources/db/migration/`.)

---

## Core entities/tables overview

Derived from module package names and migration names; key fields/relations not fully enumerated here.

- **Quote**: quote version, quote header, quote item, quote workflow, payment, void, part link, downstream, cc (leadership/warehouse).
- **Shipment**: shipment workflow, panorama, damage record.
- **Master data**: customers, contracts, machines, machine models, spare parts, sim cards, model-part-lists, BOM, ICCID, products.
- **Platform**: platform_org, platform_account_applications, platform config (code/options).
- **Warehouse**: inventory, stock records, replenish requests, outbound.
- **Work order**: work order, work order part, work order record.
- **Finance/ops**: payment record, invoice workflow, account statement, freight bill, expense/claim, trip request, correction.
- **Other**: permission_request, notification_center, attachment (Unknown (not verified) table name), app_user.

Exact table names and key columns: see migration SQL files and `com.dfbs.app.modules.*` entities.
