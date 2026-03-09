# CEO 操作速查（复制即得两行）

每个操作一个代码块，块内两行：先 `cd` 再命令。点一次「复制」可复制整块。注释用大白话说明「属于哪一步、干了啥、清空啥」。

---

## 【开工】确认本机 Docker 已就绪

```powershell
cd .   # 【开工】在仓库根
docker version   # 【开工】检查是否装了 Docker、能否连上；若看到连接失败=本机 Docker Desktop 未运行或未就绪
```

```powershell
cd .   # 【开工】在仓库根
docker compose version   # 【开工】检查本机是否有 Compose
```

---

## 【脚本-首选】拉取远程代码

```powershell
cd .   # 【脚本-首选】在仓库根，准备拉取
.\DFBS-GIT-PULL.ps1 -NoPause   # 【脚本-首选】拉取远程代码（无暂停）
```

---

## 【脚本-首选】启动本机数据库和中间件（Postgres/Redis/RabbitMQ/MinIO）

```powershell
cd .   # 【脚本-首选】在仓库根，准备启动
.\DFBS-INFRA-UP.ps1 -NoPause   # 【脚本-首选】启动本机数据库和中间件（无暂停）
```

---

## 【脚本-首选】下班前：暂存 + 提交 + 推送到远程

```powershell
cd .   # 【脚本-首选】在仓库根，准备收尾
.\DFBS-END.ps1 -NoPause   # 【脚本-首选】暂存、提交、推送（无暂停、不关窗口）
```

---

## 【换电脑前】用脚本收尾（首选）

```powershell
cd .   # 【换电脑前】在仓库根
.\DFBS-END.ps1 -NoPause   # 【换电脑前】把当前改动推上去，换电脑后能拉下来
```

---

## 【换电脑后】用脚本拉取（首选）

```powershell
cd .   # 【换电脑后】在仓库根
.\DFBS-GIT-PULL.ps1 -NoPause   # 【换电脑后】把远程最新代码拉下来
```

---

## 【脚本坏了-手动等价】拉取：看状态、看远程、拉取

```powershell
cd .   # 【拉取-手动】在仓库根
git status   # 【拉取-手动】看当前有哪些改动
```

```powershell
cd .   # 【拉取-手动】在仓库根
git remote -v   # 【拉取-手动】看远程地址
```

```powershell
cd .   # 【拉取-手动】在仓库根
git pull   # 【拉取-手动】从远程拉最新代码
```

---

## 【脚本坏了-手动等价】启动本机服务：看版本、启动、看状态、看日志、停止

```powershell
cd .   # 【启动-手动】在仓库根
docker version   # 【启动-手动】检查 Docker 是否可用；若连接失败=本机 Docker Desktop 未运行/未就绪
```

```powershell
cd .   # 【启动-手动】在仓库根
docker compose version   # 【启动-手动】检查 Compose 是否可用
```

```powershell
cd .   # 【启动-手动】在仓库根
docker compose -f ".\infra\docker-compose.yml" up -d   # 【启动-手动】启动本机数据库和中间件（后台跑）
```

```powershell
cd .   # 【启动-手动】在仓库根
docker compose -f ".\infra\docker-compose.yml" ps   # 【启动-手动】看哪些服务在跑
```

```powershell
cd .   # 【启动-手动】在仓库根
docker compose -f ".\infra\docker-compose.yml" logs postgres   # 【启动-手动】看数据库服务日志
```

```powershell
cd .   # 【启动-手动】在仓库根
docker compose -f ".\infra\docker-compose.yml" logs redis   # 【启动-手动】看 Redis 服务日志
```

```powershell
cd .   # 【启动-手动】在仓库根
docker compose -f ".\infra\docker-compose.yml" logs rabbitmq   # 【启动-手动】看 RabbitMQ 服务日志
```

```powershell
cd .   # 【启动-手动】在仓库根
docker compose -f ".\infra\docker-compose.yml" logs minio   # 【启动-手动】看 MinIO 服务日志
```

```powershell
cd .   # 【启动-手动】在仓库根
docker compose -f ".\infra\docker-compose.yml" down   # 【启动-手动】停止所有服务（不删本机数据）
```

---

## 【脚本坏了-手动等价】收尾：暂存、看有没有要提交的、提交、推送

```powershell
cd .   # 【收尾-手动】在仓库根
git status   # 【收尾-手动】看当前有哪些改动
```

```powershell
cd .   # 【收尾-手动】在仓库根
git add -u   # 【收尾-手动】只把已跟踪文件的修改放进「待提交」
```

```powershell
cd .   # 【收尾-手动】在仓库根
git diff --cached --quiet   # 【收尾-手动】看有没有待提交的（0=没有，1=有）
```

```powershell
cd .   # 【收尾-手动】在仓库根
git commit -m "sync"   # 【收尾-手动】把待提交的打成一条提交
```

```powershell
cd .   # 【收尾-手动】在仓库根
git push --progress   # 【收尾-手动】推送到远程（会显示进度；要输密码时在本窗口输）
```

---

## 【启动后端】在后端目录启动 Spring Boot

```powershell
cd .\backend\dfbs-app   # 【启动后端】进入后端目录
.\mvnw.cmd spring-boot:run   # 【启动后端】启动本机后端服务（停用请在本窗口按 Ctrl+C）
```

---

## 【启动前端】在前端目录装依赖、启动开发服务器

```powershell
cd .\frontend\dfbs-ui   # 【启动前端】进入前端目录（仓库根无 package.json，必须进这里）
npm install   # 【启动前端】安装依赖（首次或依赖改过时执行）
```

```powershell
cd .\frontend\dfbs-ui   # 【启动前端】进入前端目录
npm run dev   # 【启动前端】启动本机前端开发服务器
```

---

## 【清库】方法一：全清本机数据（从零开始，不影响 GitHub 代码）

```powershell
cd .   # 【清库-方法一】在仓库根
docker compose -f ".\infra\docker-compose.yml" down -v   # 【清库-方法一】清空本机数据库等所有本地数据（只影响这台电脑，不影响 GitHub 代码）
```

```powershell
cd .   # 【清库-方法一】在仓库根
docker compose -f ".\infra\docker-compose.yml" up -d   # 【清库-方法一】重新启动本机服务（库是空的，从零开始）
```

---

## 【清库】方法二：只清业务数据，不改库结构

清空这些表里的数据：work_order（含级联子表）、platform_account_applications、platform_org、md_customer、contracts（来自 truncate-business-data.sql）。

```powershell
cd .   # 【清库-方法二】在仓库根
docker cp .\backend\dfbs-app\truncate-business-data.sql dfbs-postgres:/tmp/truncate-business-data.sql   # 【清库-方法二】把 SQL 拷进本机数据库所在进程
```

```powershell
cd .   # 【清库-方法二】在仓库根
docker compose -f ".\infra\docker-compose.yml" exec -T postgres psql -U dfbs -d dfbs -f /tmp/truncate-business-data.sql   # 【清库-方法二】执行 SQL，只清业务表数据
```

---

## 【清库】方法三：清空本机数据库里 public 下所有表和数据（从零开始）

执行 wipe-schema.sql：删掉并重建 public，等于本机数据库 public 下全部清空、从零开始。

```powershell
cd .   # 【清库-方法三】在仓库根
docker cp .\backend\dfbs-app\scripts\wipe-schema.sql dfbs-postgres:/tmp/wipe-schema.sql   # 【清库-方法三】把 SQL 拷进本机数据库所在进程
```

```powershell
cd .   # 【清库-方法三】在仓库根
docker compose -f ".\infra\docker-compose.yml" exec -T postgres psql -U dfbs -d dfbs -f /tmp/wipe-schema.sql   # 【清库-方法三】执行 SQL，清空 public 下所有表和数据
```

---

## 【环境检查】每项两行一块；注释说明检查什么

```powershell
cd .   # 【环境检查】在仓库根
pwsh -v   # 【环境检查】看 PowerShell 版本
```

```powershell
cd .   # 【环境检查】在仓库根
git --version   # 【环境检查】看 Git 是否装好
```

```powershell
cd .   # 【环境检查】在仓库根
docker version   # 【环境检查】看 Docker 是否装好、能否连上；若看到连接失败=本机 Docker Desktop 未运行/未就绪
```

```powershell
cd .   # 【环境检查】在仓库根
docker compose version   # 【环境检查】看 Compose 是否可用
```

```powershell
cd .   # 【环境检查】在仓库根
node -v   # 【环境检查】看 Node 版本
```

```powershell
cd .   # 【环境检查】在仓库根
npm -v   # 【环境检查】看 npm 版本
```

```powershell
cd .   # 【环境检查】在仓库根
java -version   # 【环境检查】看 Java 是否装好
```

```powershell
cd .\backend\dfbs-app   # 【环境检查】必须在后端目录看 Maven 版本
.\mvnw.cmd -v   # 【环境检查】看 Maven（mvnw）版本
```

```powershell
cd .   # 【环境检查】在仓库根（仅当已装 WSL）
wsl --status   # 【环境检查】看 WSL 状态
```

```powershell
cd .   # 【环境检查】在仓库根（仅当已装 WSL）
wsl -l -v   # 【环境检查】看 WSL 里的发行版
```


##  阶段对齐工单
```
[DIRECT OPS TICKET — STAGE BASELINE REBUILD (FIXED)]

Goal:
- Rebuild the stage baseline so it is detailed enough for decision-making, not just for rough orientation.
- Refresh the handover pack to match CURRENT repo reality.
- Audit the product alignment pack against CURRENT repo reality, without rewriting product intent into repo-fact language.
- Facts only. No new requirements. No planning. No guesses.

Scope (documentation only; overwrite/update in place where allowed):
A) Update these files under `evidence/handover/` (overwrite/update in place):
- STATE_SNAPSHOT.md
- UI_ENTRYPOINTS.md
- API_SURFACE.md
- DATA_BASELINE.md
- TEST_BASELINE.md
- REPO_MAP.md
- REUSABLE_BLOCKS.md
- REUSABLE_BLOCKS_ZH.md
- DEV-ENV.md

B) Audit these files under `docs/product/` against CURRENT repo reality:
- MODULE_ROUTE_ANCHORS_v0.1.md
- BUSINESS_MAP_v0.1.md
- PROCESS_MAP_v0.1.md
- OBJECT_MAP_v0.1.md

Rules for B:
- These are product-alignment docs, not pure repo-fact docs.
- Do NOT rewrite product intent/speculation into repo-truth language.
- ONLY add/update a short factual section per file such as:
  - `Repo reality check`
  - `Conflicts with current repo reality`
  - `Anchor gaps / not yet present in repo`
- Preserve the original product intent text unless it is an obvious factual route/path/symbol typo.

Definition of Done (DoD):
- Every handover file above reflects CURRENT repo reality.
- Any statement conflicting with repo reality is deleted/rewritten in handover docs (no legacy sections).
- Product docs are audited and annotated with repo-reality conflicts/gaps where needed.
- Each file is detailed enough that a PM can make safer execution decisions without opening the repo.
- Each key fact includes concrete evidence pointers (paths / symbols / routes / migrations / scripts / commands).

Hard rules (anti-laziness):
- Repo is source of truth. You MUST inspect the repo to confirm each item.
- Be explicit & exhaustive within the repo’s current scope:
  - No hand-wavy language: remove `etc.`, `likely`, `maybe`, `approximately`, `similar`.
  - No placeholders like `TBD`, `TODO`, unless explicitly required.
  - If something cannot be verified from repo sources, write exactly: `Not verified`.
- Evidence pointers are required:
  - For each key fact, include at least one of:
    - file path
    - symbol/class/component/controller name
    - route path
    - script/command
    - migration filename
- Do NOT change any application code / behavior / scripts / infra. Docs only.
- Keep docs detailed but do not paste huge code blocks. Prefer concise tables/bullets + pointers.

Global header requirement (add/update at top of EACH touched file):
- As-of: <YYYY-MM-DD HH:MM>
- Repo: <branch name>
- Commit: <short SHA>
- Verification method: <commands used / key files inspected>

Global section requirements (apply where relevant in EACH handover file):
- `Reality semantics`
  - Explain what the current capability REALLY means in repo terms (not just that a page/endpoint exists).
- `Reuse status`
  - For each key capability/module in scope, classify as one of:
    - Reusable as-is
    - Reusable with small patch
    - Exists but incomplete
    - Not present
- `Decision-risk notes`
  - Facts that are easy to misread and could mislead planning/execution if interpreted loosely.
- `Not verified`
  - Mandatory section if anything remains unverifiable.

Per-file minimum requirements (MUST):

1) STATE_SNAPSHOT.md
- “What works now” list (user-visible capabilities) with pointers (routes/pages/modules/files).
- “Known limitations” list (facts only).
- “Reality semantics” for key foundation areas:
  - login/auth
  - organization/person
  - account management
  - permissions/bypass behavior
  - role simulator / mock account related current state if present
- “Reuse status” for major foundation capabilities.
- “Decision-risk notes” for facts that can easily cause wrong planning assumptions.
- “Feature flags / config gates” if present.
- “Not verified” section if any.

2) UI_ENTRYPOINTS.md
- Enumerate ALL current UI entry points / major pages/flows (route or navigation path).
- For each entry:
  - Name
  - route/path or navigation path
  - prerequisites (auth/role/data) if determinable
  - primary actions/outcomes
  - related APIs/modules (pointer only)
  - reuse status
  - current real semantic note if the page is only partial / shell / gated / misleading
- Include “decision-risk notes” for any entry whose existence could be misread as “feature complete”.
- “Not verified” section if any.

3) API_SURFACE.md
- Enumerate ALL current endpoints:
  - METHOD + PATH
  - grouped by controller/module (pointer to file)
  - auth/role requirement if determinable from code/config
  - request/response schema pointers (DTO/type names or files), not full dumps
- For auth-sensitive areas, explicitly note real semantics if admin/super-admin bypass is partial / whitelist-based / not global.
- Add reuse status for major API groups.
- Add “decision-risk notes” where endpoint existence does NOT mean end-to-end feature readiness.
- “Not verified” section if any.

4) DATA_BASELINE.md
- List ALL Flyway migration filenames in order.
- For each migration:
  - key tables/entities touched
  - brief intent (fact-based from migration content/comments)
  - pointer: migration file path
- Highlight migrations relevant to:
  - accounts / users / people / org
  - permissions / role-like structures
  - business ownership fields if present
- “Decision-risk notes” for tables/entities easy to over-assume from naming alone.
- “Not verified” section if any.

5) TEST_BASELINE.md
- Exact commands to run:
  - backend tests/build
  - frontend tests/build
  - full-suite build (what counts as BUILD SUCCESS)
- Required env vars/config (names + where referenced)
- Common failure modes visible from repo scripts/config (facts only)
- Reality semantics:
  - what “build success” really proves / does not prove
- “Not verified” section if any.

6) REPO_MAP.md
- High-level directory map of the repo (key folders)
- Entry points for FE/BE (main files), build scripts, routing definitions, config roots
- Where `evidence/handover` lives + intended usage
- Where `docs/product` lives + intended usage
- Reality semantics:
  - which directories/pages are actively wired vs shells / partial / legacy
- “Not verified” section if any.

7) REUSABLE_BLOCKS.md (EN)
- List reusable blocks/components/services judged reusable in CURRENT repo
- For each block:
  - name
  - purpose (1 line)
  - location (file path)
  - usage sites (list pages/flows)
  - key interface (props/inputs/outputs names only)
  - reuse status
- Mark if reusable in name only but not safely reusable yet.
- “Not verified” section if any.

8) REUSABLE_BLOCKS_ZH.md (ZH)
- Same list as EN, but usage sites described in page/flow wording (NO code-structure explanation)
- Still include component/service name + location pointer
- Add reuse status in Chinese wording
- “Not verified” section if any.

9) DEV-ENV.md
- Exact local startup/build commands and required toolchain versions if determinable
- Required config/env files or env vars
- Any known local setup gates visible from repo/config
- Reality semantics:
  - what is sufficient to run FE only / BE only / both
- “Not verified” section if any.

Product pack audit requirements (for docs/product files):
For EACH of the 4 product files, add a short factual section:
- `Repo reality check`
  - Which anchors/claims already map to current repo reality
- `Conflicts with current repo reality`
  - Only factual conflicts (wrong route, missing page, wrong module/path, etc.)
- `Anchor gaps / not yet present in repo`
  - Items that are valid product discussion anchors but do not yet exist in code
- Do NOT delete product intent because the repo has not implemented it yet.
- Do NOT silently “upgrade” product docs into implementation docs.

Special focus areas (must verify carefully; these are currently high decision-risk):
- Whether org structure already contains usable person records and how they are surfaced
- Whether account creation/binding already depends on person records
- What “account & permissions” can truly do now vs what is shell/incomplete
- Real meaning of admin / super-admin behavior:
  - full permission?
  - whitelist-based bypass?
  - page-only access restored vs action-level blocked?
- Whether route existence equals usable business feature, or only partial page wiring
- Current status of role simulator / mock account related code, if any exists
- Whether current business pages already support “my / pending / my-orders / assigned to me” semantics, and at what real level

Output (short receipt only; <=20 lines):
- `DONE: STAGE_BASELINE_REBUILD (FIXED) — Run: <YYYY-MM-DD HH:MM>`
- 3–6 bullets: what changed (mention major rewrites and any product-doc conflict annotations)
- `Not verified:` list (aggregated)
- `Highest decision-risk facts found:` list (aggregated, very short)
- 1 blocker question max (only if truly blocking)

[/DIRECT OPS TICKET — STAGE BASELINE REBUILD (FIXED)]
```