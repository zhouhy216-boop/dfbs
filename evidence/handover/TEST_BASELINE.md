# TEST_BASELINE — How to run tests; what counts as BUILD SUCCESS

**Facts only.** Source: `backend/dfbs-app/pom.xml`, `frontend/dfbs-ui/package.json`, test dirs.

---

## Backend tests

- **Command**: From `backend/dfbs-app/`, run `.\mvnw.cmd test` (Windows) or `./mvnw test` (Unix).
- **Runner**: Maven Surefire; JUnit tests in `src/test/java`.
- **BUILD SUCCESS**: Exit 0, log line `[INFO] BUILD SUCCESS`, and tests summary with Failures: 0, Errors: 0.
- **Not verified**: Full test run not executed in this handover; run locally to confirm.

---

## Backend test layout (facts from repo)

- **Application (service) tests**: `src/test/java/com/dfbs/app/application/` — e.g. QuoteItemTest, QuoteStateTest, QuoteVoidTest, ShipmentPanoramaTest, ShipmentProcessTest, RepairRecordTest, PaymentRecordTest, QuotePaymentWorkflowTest, AccountStatementTest, ExpenseClaimTest, TripRequestTest, CorrectionTest, FreightBillTest, InventoryTest, InvoiceApplicationTest, CustomerMergeTest, DamageRecordTest, BomServiceTest, CarrierTest, PriceBookTest, NotificationTest, PermissionRequestTest, PaymentTest, DictionaryLogicTest, DictionaryQuoteTest, QuoteCcTest, QuoteDownstreamTest, QuoteExportTest, QuoteNumberingTest, QuotePartLinkTest, QuoteStandardizationTest, QuoteWarehouseTest, WorkOrderQuoteTest, ExpenseStatsTest, AttachmentRuleTest, MasterDataListTest.
- **Interface (controller) tests**: `src/test/java/com/dfbs/app/interfaces/` — e.g. ContractMasterDataCreateTest, CustomerMasterDataSearchTest, AccountStatementControllerTest, ContractPriceControllerTest, MachineMasterDataCreateTest, ProductMasterDataCreateTest, ProductMasterDataSearchTest, IccidMasterDataCreateTest, QuoteVersionActivateTest; orgstructure: OrgLevelControllerTest, OrgLevelReorderControllerTest, OrgChangeLogControllerTest, OrgNodeControllerTest, OrgStructureResetAllControllerTest.
- **Application (orgstructure) tests**: `src/test/java/com/dfbs/app/application/orgstructure/` — OrgLevelServiceTest, OrgStructureDevResetServiceTest.
- **Other**: DfbsAppApplicationTests.java, ArchitectureRulesTest.java, MasterDataReadOnlyRulesTest.java, SwaggerTest.java (under infra/).

---

## Frontend scripts (from package.json)

Source: `frontend/dfbs-ui/package.json`.

| Script | Command |
|--------|---------|
| dev | vite |
| build | tsc -b && vite build |
| lint | eslint . |
| preview | vite preview |

There is no `test` script in package.json. Frontend gate (e.g. for PR): Not verified. Candidates: `npm run build`, `npm run lint`.

---

## Frontend BUILD SUCCESS

- **Production build**: `cd frontend/dfbs-ui` then `npm run build`. Success = command completes without error and emits output (e.g. to `dist/`). Not verified in this handover.
