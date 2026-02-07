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

```
阶段对齐工单
[TICKET]
ID: VNX-YYYYMMDD-###
Title: Handover Sync — Refresh evidence/handover snapshot to CURRENT reality
Priority: P2
Risk: LOW
Status: CURRENT
Owner: Cursor
Language: EN (Questions to Delivery PM only)

1) Goal
- Refresh the handover pack so PM/Gemini/Cursor can align on CURRENT repo reality.
- Facts only. No new requirements. No planning. No guesses.

2) Scope (documentation only)
Update these files under evidence/handover/ (overwrite/update in place):
- STATE_SNAPSHOT.md
- UI_ENTRYPOINTS.md
- API_SURFACE.md
- DATA_BASELINE.md
- TEST_BASELINE.md
- REPO_MAP.md
- REUSABLE_BLOCKS.md
- REUSABLE_BLOCKS_ZH.md

3) Hard rules
- Facts only: derive from repo code, routes, controllers, migrations, package.json, etc.
- If not verified, write "Not verified" (do NOT invent).
- Remove vague words like "etc.", "likely", "maybe".
- REUSABLE_BLOCKS_ZH.md must describe “current usage location” in page/flow wording (no code paths).

4) What to include (minimum)
- STATE_SNAPSHOT: what works now / known limitations (facts)
- UI_ENTRYPOINTS: main pages/flows and where to enter them (facts)
- API_SURFACE: enumerate existing endpoints (method + path) with controller grouping (facts)
- DATA_BASELINE: list Flyway filenames and key tables/entities touched by migrations (facts)
- TEST_BASELINE: how to run tests/build (backend + frontend) and what counts as BUILD SUCCESS (facts)
- REPO_MAP: key folders and entry points (facts)
- REUSABLE_BLOCKS (EN/ZH): list reusable blocks and their current usage sites (facts)

5) Output to Delivery PM when done (short)
- "DONE: <Ticket ID> — Handover Sync"
- 3–6 bullets: what changed in handover pack + any "Not verified" items needing PM follow-up

[/TICKET]
```