@echo off
setlocal

echo SCRIPT_PATH=%~f0
echo SCRIPT_DIR=%~dp0
echo.

cd /d "%~dp0"

echo ==================================================
echo DFBS START - pull + infra + backend + healthz + test
echo ==================================================
echo.

REM 1) git pull
echo [1/5] git pull...
git pull
if errorlevel 1 (
  echo.
  echo FAIL: git pull
  echo.
  pause
  exit /b 1
)
echo OK: git pull
echo.

REM 2) start infra
echo [2/5] start infra containers...
set "COMPOSE_FILE=infra\docker-compose.yml"
if not exist "%COMPOSE_FILE%" (
  echo.
  echo FAIL: cannot find %COMPOSE_FILE%
  echo Please edit DFBS-START.bat and set COMPOSE_FILE correctly.
  echo.
  pause
  exit /b 1
)

docker compose -f "%COMPOSE_FILE%" up -d
if errorlevel 1 (
  echo.
  echo FAIL: docker compose up -d
  echo Please ensure Docker Desktop is running.
  echo.
  pause
  exit /b 1
)
echo OK: infra up
echo.

REM 3) start backend in new window
echo [3/5] start backend (new window)...
start "DFBS Backend" cmd /c "cd /d %~dp0backend\dfbs-app && mvnw.cmd spring-boot:run"
echo OK: backend starting
echo.

REM 4) wait healthz
echo [4/5] wait for healthz http://localhost:8080/api/healthz ...
set "HEALTH_URL=http://localhost:8080/api/healthz"
set "TRY=0"

:HEALTH_LOOP
set /a TRY=TRY+1
if %TRY% GTR 60 goto HEALTH_FAIL

curl -s "%HEALTH_URL%" | findstr /i "ok" >nul
if %ERRORLEVEL%==0 goto HEALTH_OK

timeout /t 1 /nobreak >nul
goto HEALTH_LOOP

:HEALTH_OK
echo OK: healthz ready
echo.

REM 5) run tests (directly)
echo [5/5] run tests...
cd /d "%~dp0backend\dfbs-app"

set "LOG=target\dfbs-test.log"
if exist "%LOG%" del /f /q "%LOG%"

call mvnw.cmd test > "%LOG%" 2>&1
set "TRC=%ERRORLEVEL%"

echo.
if "%TRC%"=="0" (
  echo TESTS PASS
  echo Log: backend\dfbs-app\%LOG%
  echo.
  echo ALL DONE: START OK
) else (
  echo TESTS FAIL
  echo Log: backend\dfbs-app\%LOG%
  echo.
  echo ALL DONE: START FAIL
)

echo.
pause
exit /b %TRC%

:HEALTH_FAIL
echo.
echo FAIL: healthz not ready after 60 seconds
echo Please check backend window logs.
echo.
pause
exit /b 1
