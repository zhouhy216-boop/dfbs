# 开发环境记录（本机事实）

仅供协作参考；不做环境推荐。记录「在这台机器上实际跑出来的版本/路径」。

---

## 电脑A（zhouhy-home）— Verified on 2026-02-07

| 项 | 值 |
|----|-----|
| 仓库根路径 | D:\dfbs |
| 操作系统 | Windows 11 |
| PowerShell | 7.5.4 |
| Git | 2.52.0.windows.1 |
| Node | v24.13.0 |
| npm | 11.6.2 |
| Java | 25.0.1 LTS |
| Maven（mvnw -v） | 3.9.12 |
| Docker Client | 29.1.3 |
| Docker Compose | v5.0.0-desktop.1 |
| Docker 引擎 | 采集时连接失败（failed to connect … dockerDesktopLinuxEngine … file specified）；仅作事实记录。 |

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
