# DEV-ENV — 开发环境与启动/构建命令

- **As-of:** 2025-02-24 14:00
- **Repo:** main
- **Commit:** 23467d7d
- **Verification method:** Inspected `frontend/dfbs-ui/package.json`, `vite.config.ts`, `backend/dfbs-app/pom.xml`, `backend/dfbs-app/src/main/resources/application.yml`.

**Facts only.** 本机事实仅供协作参考；不做环境推荐。

---

## Exact local startup/build commands

| Scope | Command | Working directory |
|-------|---------|-------------------|
| Backend run | `.\mvnw.cmd spring-boot:run` (Windows) or `./mvnw spring-boot:run` (Unix) | `backend/dfbs-app` |
| Backend compile | `.\mvnw.cmd -q clean compile -DskipTests` | `backend/dfbs-app` |
| Frontend dev | `npm run dev` | `frontend/dfbs-ui` |
| Frontend build | `npm run build` | `frontend/dfbs-ui` |

Required: Postgres at `spring.datasource.url` (default `jdbc:postgresql://localhost:5432/dfbs`); Node/npm for frontend. See TEST_BASELINE.md for test commands.

| Env / config | Where | Purpose |
|--------------|--------|---------|
| `VITE_API_TARGET` | `frontend/dfbs-ui/vite.config.ts` (loadEnv) | API proxy target for dev server; default `http://localhost:8080`. |

---

## Reality semantics

- **FE only:** `npm run dev` in `frontend/dfbs-ui`; needs backend or mock for API. Vite proxy may point to backend (see `vite.config.ts`).
- **BE only:** `.\mvnw.cmd spring-boot:run` in `backend/dfbs-app`; needs DB and migrations applied.
- **Both:** Start backend then frontend dev; frontend uses `/api` baseURL (proxy to backend).

---

## Not verified

- Full local run (FE + BE + DB) not executed in this handover. Toolchain versions below are from prior handover/machine notes.

---

## 电脑A（zhouhy-home）— Verified on 2026-02-07

- 仓库根路径：`D:\dfbs`
- 操作系统：Windows 11（来自 mvnw 输出）
- PowerShell：7.5.4
- Git：2.52.0.windows.1
- Docker（采集时已连通）：
  - Client Version 29.1.3，Context `desktop-linux`
  - Server：Docker Desktop 4.56.0 (214940)
  - Engine Version 29.1.3 (API 1.52)
  - containerd v2.2.1，runc 1.3.4，docker-init 0.19.0
- Docker Compose：v5.0.0-desktop.1
- Node：v24.13.0
- npm：11.6.2
- Java：25.0.1 LTS
- Maven（mvnw -v）：3.9.12
- WSL（已安装，Docker Desktop 使用）：
  - `wsl --status`：Default distro `docker-desktop`，Default version `2`
  - `wsl -l -v`：`docker-desktop` Running，Version 2

---

## Computer B (zhouhy-office) — Verified

**Machine**

| Item | Value |
|------|--------|
| Device name | zhouhy-office |
| OS | Windows 11 (64-bit, x64-based) |
| CPU | 12th Gen Intel(R) Core(TM) i9-12900H (2.50 GHz) |
| RAM | 32.0 GB (31.7 GB usable) |
| WSL | Ubuntu (Running, version 2); docker-desktop (Running, version 2) |

**Toolchain (PowerShell session)**

| Tool | Version |
|------|---------|
| PowerShell | 7.5.4 |
| Git | 2.52.0.windows.1 |
| Docker Desktop | 4.56.0 (214940) |
| Docker Client | 29.1.3 (API 1.52) |
| Docker Server/Engine | 29.1.3 (API 1.52) |
| Node | v25.5.0 |
| npm | 11.8.0 |
| Java | Eclipse Temurin OpenJDK 21.0.9+10 (LTS) |
| Maven (wrapper) | Apache Maven 3.9.12 (via `backend/dfbs-app/mvnw.cmd -v`) |
