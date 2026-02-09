# REPO_MAP — Key folders and entry points

**Facts only.** Paths relative to repo root.

---

## Key folders

| Path | Purpose |
|------|--------|
| `backend/dfbs-app/` | Single Spring Boot application module. |
| `backend/dfbs-app/src/main/java/com/dfbs/app/` | Java source: `DfbsAppApplication.java`, `config/`, `interfaces/` (REST), `application/` (services), `modules/` (entities/repos). |
| `backend/dfbs-app/src/main/resources/` | `application*.yml`, `db/migration/`, `templates/`. |
| `backend/dfbs-app/src/test/java/com/dfbs/app/` | Tests: `application/`, `interfaces/`, `DfbsAppApplicationTests.java`, `ArchitectureRulesTest`, `MasterDataReadOnlyRulesTest`, `SwaggerTest`. |
| `frontend/dfbs-ui/` | Vite + React + Ant Design Pro frontend. Entry: `src/main.tsx`, `package.json` (scripts: dev, build, lint, preview). |
| `frontend/dfbs-ui/src/` | `main.tsx`, `App.tsx`, `pages/`, `components/`, `layouts/`, `utils/`, `stores/`, `services/`. |
| `evidence/handover/` | Handover pack (this set of docs). |
| `legacy/` | Quarantined legacy docs; not authoritative. |
| `infra/` | `docker-compose.yml` (postgres, redis, rabbitmq, minio). |

---

## Entry points

- **Frontend routing**: `frontend/dfbs-ui/src/main.tsx` → `App.tsx` (routes in `App.tsx`).
- **Frontend layout/sidebar**: `frontend/dfbs-ui/src/layouts/BasicLayout.tsx`.
- **Pages**: `frontend/dfbs-ui/src/pages/` (Dashboard, Customer, Quote, Shipment, Finance, AfterSales, WorkOrder, Warehouse, ImportCenter, MasterData/*, Platform/*, Admin [ConfirmationCenter, OrgLevelConfig, OrgTree, OrgChangeLog], System).
- **Backend REST**: `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/` — controller classes per domain (auth, platformorg, platformaccount, platformconfig, quote, workorder, expense, orgstructure, masterdata, shipment, etc.).
- **Backend application**: `backend/dfbs-app/src/main/java/com/dfbs/app/DfbsAppApplication.java`.
- **Tests**: `backend/dfbs-app/src/test/java/com/dfbs/app/`.

---

## Root scripts (operational)

- `DFBS-GIT-PULL.ps1`, `DFBS-INFRA-UP.ps1`, `DFBS-END.ps1`, `DFBS-UTILS.ps1` at repo root. See `CEO-OPS.md` for usage.
