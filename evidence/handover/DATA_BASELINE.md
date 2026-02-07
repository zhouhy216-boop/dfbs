# DATA_BASELINE — Flyway migrations and key tables

**Facts only.** Location: `backend/dfbs-app/src/main/resources/db/migration/`. Format: `Vxxxx__<name>.sql`.

---

## Migration filenames (V0001–V0060)

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

(No V0056 in repo.)

---

## Key tables/entities by migration (from filenames)

- V0001: init (base schema).
- V0002–V0003: quote_version.
- V0004: masterdata init.
- V0005–V0017: quote_item, quote_header, payment, void, cc, downstream.
- V0018–V0019: shipment_workflow, shipment_panorama.
- V0020–V0021: damage_record, freight_bill.
- V0022: inventory.
- V0023: repair_ledger.
- V0024: permission_request.
- V0025: notification_center.
- V0026: account_statement.
- V0027–V0028: bom_versioning, quote_part_link.
- V0029: machine_iccid.
- V0030–V0042: quote_standardization, customer_merge, expense_claim, trip_request, payment_allocation, contract_pricing, carrier_freight_bill, correction, after_sales, masterdata_six_pack, bom_conflicts, quote_payment_created_at, warehouse_mvp.
- V0043–V0048: work_order (service_flow, cancellation_reason, refinement), app_user (username, seed_admin), smart_select_infrastructure.
- V0049–V0060: platform_org, platform_account_applications, refactor_platform_org, platform_application_source, relax_application_constraints, enforce_org_uniqueness, org_source_tracking, drop_data_governance_task, add_reject_reason, platform_org_status, create_md_platform.
