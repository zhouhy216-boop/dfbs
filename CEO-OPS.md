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
[DIRECT OPS TICKET — HANDOVER SYNC (FIXED)]

Goal:
- Refresh the handover pack so it matches CURRENT repo reality.
- This handover pack is the ONLY truth source for non-repo viewers. It MUST be detailed and accurate.
- Facts only. No new requirements. No planning. No guesses.

Scope (documentation only; overwrite/update in place):
Update these files under `evidence/handover/` (overwrite/update in place):
- STATE_SNAPSHOT.md
- UI_ENTRYPOINTS.md
- API_SURFACE.md
- DATA_BASELINE.md
- TEST_BASELINE.md
- REPO_MAP.md
- REUSABLE_BLOCKS.md
- REUSABLE_BLOCKS_ZH.md

Definition of Done (DoD):
- Every file above is updated to reflect CURRENT repo reality.
- Any statement conflicting with repo reality is deleted/rewritten to current facts (no legacy sections).
- Each file contains enough detail that a PM can understand CURRENT behavior without opening the repo.
- Each file includes concrete evidence pointers (paths/identifiers/commands) so facts are auditable.

Hard rules (anti-laziness):
- Repo is source of truth. You MUST inspect the repo to confirm each item.
- Be explicit & exhaustive within the repo’s current scope:
  - No hand-wavy language: remove `etc.`, `likely`, `maybe`, `approximately`, “similar”.
  - No placeholders like “TBD”, “TODO”, unless explicitly required; prefer “Not verified” if truly unverifiable.
- Evidence pointers are required:
  - For each key fact, include at least one of: file path, symbol name, route path, script name, command line, migration filename.
- If something cannot be verified from repo sources, write exactly: `Not verified` AND list it in a final “Not verified” section per file AND in the final receipt.
- Do NOT change any application code/behavior/scripts/infra. Docs only.
- Keep docs detailed but do not paste huge code blocks. Prefer concise tables/bullets + pointers.

Global header requirement (add/update at top of EACH file):
- As-of: <YYYY-MM-DD HH:MM>
- Repo: <branch name>
- Commit: <short SHA>
- Verification method: <commands used / key files inspected>

Per-file minimum requirements (MUST):
1) STATE_SNAPSHOT.md
- “What works now” list (user-visible capabilities) with pointers (routes/pages/modules).
- “Known limitations” list (facts only).
- “Feature flags / config gates” if present (name + where set).
- “Not verified” section if any.

2) UI_ENTRYPOINTS.md
- Enumerate ALL current UI entry points / major pages/flows (route or navigation path).
- For each entry:
  - Name, route/path (or navigation), prerequisites (auth/role/data), primary actions/outcomes.
  - Link to related APIs or modules (pointer only).
- “Not verified” section if any.

3) API_SURFACE.md
- Enumerate ALL current endpoints:
  - METHOD + PATH
  - Grouped by controller/module (pointer to file).
  - Include auth/role requirement if determinable from code/config.
  - Include request/response schema pointers (DTO/type names or files), not full dumps.
- “Not verified” section if any.

4) DATA_BASELINE.md
- List ALL Flyway migration filenames in order.
- For each migration:
  - Key tables/entities touched (names).
  - Brief intent (fact-based from migration content/comments).
  - Pointer: migration file path.
- “Not verified” section if any.

5) TEST_BASELINE.md
- Exact commands to run:
  - backend tests/build
  - frontend tests/build
  - full-suite build (what counts as BUILD SUCCESS)
- Required env vars/config (names + where referenced).
- Common failure modes observed in repo scripts/config (facts only, if evident).
- “Not verified” section if any.

6) REPO_MAP.md
- High-level directory map of the repo (key folders).
- Entry points for FE/BE (main files), build scripts, routing definitions, config roots.
- Where “evidence/handover” lives + intended usage.
- “Not verified” section if any.

7) REUSABLE_BLOCKS.md (EN)
- List reusable blocks/components/services judged reusable in CURRENT repo.
- For each block:
  - Name, purpose (1 line), location (file path), usage sites (list pages/flows), key interface (props/inputs/outputs names only).
- “Not verified” section if any.

8) REUSABLE_BLOCKS_ZH.md (ZH)
- Same list as EN, but usage sites must be described in page/flow wording (NO code paths).
- Still include the component name + location pointer, but do not explain via code structure.
- “Not verified” section if any.

Output (short receipt only; <=20 lines):
- `DONE: HANDOVER_SYNC (FIXED) — Run: <YYYY-MM-DD HH:MM>`
- 3–6 bullets: what changed (mention which files had major rewrites)
- `Not verified:` list (if any, aggregated)
- If anything blocked you from verifying: 1 blocker question max (only if truly blocking)

[/DIRECT OPS TICKET — HANDOVER SYNC (FIXED)]
```