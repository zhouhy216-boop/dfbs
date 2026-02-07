# REPO_MAP — Navigation map of the repo

**Facts only.** Paths relative to repo root.

---

## Key folders

| Path | Purpose |
|------|--------|
| `backend/dfbs-app/` | Main Spring Boot application (single module). |
| `backend/dfbs-app/src/main/java/com/dfbs/app/` | Java source: `DfbsAppApplication.java`, `config/`, `interfaces/` (REST), `application/` (services), `modules/` (entities/repos). |
| `backend/dfbs-app/src/main/resources/` | `application*.yml`, `db/migration/`, `templates/`. |
| `backend/dfbs-app/src/test/java/com/dfbs/app/` | Tests: `application/`, `interfaces/`, `DfbsAppApplicationTests.java`, `ArchitectureRulesTest`, `MasterDataReadOnlyRulesTest`, `SwaggerTest`. |
| `frontend/dfbs-ui/` | Vite + React + Ant Design Pro frontend. |
| `frontend/dfbs-ui/src/` | `main.tsx`, `App.tsx`, `pages/`, `components/`, `layouts/`, `utils/`, `stores/`, `services/`. |
| `evidence/` | Handover and evidence (this pack). |
| `legacy/` | Quarantined legacy docs; not authoritative. |
| `infra/` | Contains `docker-compose.yml`. |

---

## Entry points

- **Frontend routing / app entry**: `frontend/dfbs-ui/src/main.tsx` → `App.tsx` (routes defined in `App.tsx`).
- **Frontend layout (sidebar/menu)**: `frontend/dfbs-ui/src/layouts/BasicLayout.tsx`.
- **Major pages**: `frontend/dfbs-ui/src/pages/` (Dashboard, Customer, Quote, Shipment, Finance, AfterSales, WorkOrder, Warehouse, ImportCenter, MasterData/*, Platform/*, Admin, System).
- **Key shared components**: `frontend/dfbs-ui/src/components/` (SmartInput, SmartReferenceSelect, Business/DuplicateCheckModal, Business/HitAnalysisPanel, AttachmentList, Access).
- **Backend REST entry**: `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/` — one or more controller classes per domain (e.g. `platformaccount/PlatformAccountApplicationController.java`).
- **Backend application entry**: `backend/dfbs-app/src/main/java/com/dfbs/app/DfbsAppApplication.java`.
- **Tests entry**: `backend/dfbs-app/src/test/java/com/dfbs/app/` — `DfbsAppApplicationTests.java`, `application/*`, `interfaces/*`.

---

## Operational script (non-authoritative)

- `DFBS-INFRA-UP.ps1` exists at repo root. It is an operational convenience script for bringing up infra; not part of the authoritative source of truth for requirements or build. Verify presence at repo root; if missing, check `legacy/scripts_ps1/` (moved during legacy quarantine).
