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

## 电脑B — TBD

请在本机执行 **CEO-OPS.md** 里【环境检查】那一节的命令，把各命令的输出贴到下面（或按上表格式填好），并注明采集日期。

- `pwsh -v`
- `git --version`
- `docker version`
- `docker compose version`
- `node -v`
- `npm -v`
- `java -version`
- 在 `backend\dfbs-app` 下执行：`.\mvnw.cmd -v`

（可选）若已装 WSL：`wsl --status`、`wsl -l -v`
