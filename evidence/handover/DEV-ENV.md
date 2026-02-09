# 开发环境记录（本机事实）

仅供协作参考；不做环境推荐。记录「在这台机器上实际跑出来的版本/路径」。

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
