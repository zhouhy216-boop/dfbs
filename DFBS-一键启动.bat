@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM =========================================================
REM DFBS 一键启动（单文件版）
REM - 启动 docker compose（postgres/redis/rabbitmq/minio）
REM - 用 PowerShell 新窗口启动 Spring Boot（mvnw spring-boot:run）
REM - 等待 /api/healthz 返回 ok
REM =========================================================

REM 以 bat 所在目录作为项目根目录
set "ROOT=%~dp0"
REM 去掉末尾反斜杠（可选）
if "%ROOT:~-1%"=="\" set "ROOT=%ROOT:~0,-1%"

set "COMPOSE=%ROOT%\infra\docker-compose.yml"
set "APPDIR=%ROOT%\backend\dfbs-app"
set "HEALTHZ=http://localhost:8080/api/healthz"
set "WAIT_SECONDS=120"

echo.
echo [1/3] 启动基础容器（Postgres/Redis/RabbitMQ/MinIO）...
docker compose -f "%COMPOSE%" up -d
if errorlevel 1 (
  echo ❌ docker compose 启动失败，请检查 Docker Desktop 是否运行、compose 文件路径是否正确。
  echo    %COMPOSE%
  exit /b 1
)

echo ✅ docker compose 已启动（如已有在运行会保持不变）
echo.

echo [2/3] 在 PowerShell 新窗口启动 Spring Boot（日志会显示在新窗口）...
REM 使用 start 强制开新窗口；PowerShell 保持不自动关闭，便于看日志
start "DFBS-App" powershell -NoExit -Command ^
  "cd '%APPDIR%'; Write-Host '🚀 DFBS Spring Boot 启动中...' -ForegroundColor Green; .\mvnw.cmd spring-boot:run"

echo.
echo [3/3] 等待 /api/healthz 就绪（最多 %WAIT_SECONDS%s）...
set "OK=0"

for /l %%i in (1,1,%WAIT_SECONDS%) do (
  powershell -NoProfile -Command ^
    "try { $r=Invoke-WebRequest -UseBasicParsing -TimeoutSec 1 '%HEALTHZ%'; if($r.StatusCode -eq 200 -and $r.Content -match 'ok'){ exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>&1

  if not errorlevel 1 (
    set "OK=1"
    goto :HEALTHZ_OK
  )

  REM 每 5 秒提示一次等待中
  set /a mod=%%i %% 5
  if "!mod!"=="0" echo ⏳ 等待中... %%i/%WAIT_SECONDS%

  timeout /t 1 >nul
)

:HEALTHZ_OK
if "%OK%"=="1" (
  echo ✅ healthz OK: %HEALTHZ%
  echo.
  echo [OK] DFBS 已启动完成。
  echo.
  pause
  exit /b 0
) else (
  echo ❌ healthz 超时未就绪（>%WAIT_SECONDS%s）：%HEALTHZ%
  echo 👉 请看 PowerShell 新窗口 DFBS-App 的启动日志（一般是启动报错/端口占用）。
  echo.
  pause
  exit /b 1
)
