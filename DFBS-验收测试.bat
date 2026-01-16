@echo off
setlocal enabledelayedexpansion

echo.
echo ===========================================
echo DFBS - Running backend tests (mvnw test)...
echo ===========================================
echo.

REM Always run from this bat's directory (project root)
cd /d "%~dp0"

REM Go to backend app module
cd backend\dfbs-app

REM Run tests, and write a log file for later viewing
set LOG=target\dfbs-test.log

call mvnw.cmd test > "%LOG%" 2>&1
set RC=%ERRORLEVEL%

echo.
if "%RC%"=="0" (
  echo ✅ TESTS PASS
  echo Log: backend\dfbs-app\%LOG%
) else (
  echo ❌ TESTS FAIL (exit code=%RC%)
  echo Log: backend\dfbs-app\%LOG%
  echo.
  echo Please open the log file above and copy the failure part here.
)

echo.
pause
exit /b %RC%