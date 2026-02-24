# 前端开发代理（Vite）

本地开发时，前端（默认 5173）将 `/api` 请求代理到后端。

- **默认后端地址：** `http://localhost:8080`
- **覆盖代理目标：** 设置环境变量 `VITE_API_TARGET` 后重启 Vite。  
  例如使用 8081 端口验证时：`VITE_API_TARGET=http://localhost:8081`（Windows PowerShell：`$env:VITE_API_TARGET="http://localhost:8081"`）。

未设置时使用默认 8080，可避免后端未在 8081 启动时出现登录 ECONNREFUSED。
