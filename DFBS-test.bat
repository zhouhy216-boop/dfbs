@echo off
setlocal

echo SCRIPT_PATH=%~f0
echo SCRIPT_DIR=%~dp0

echo.
echo ===========================================
echo DFBS - Running backend tests (mvnw test)...
echo ===========================================
echo.

cd /d "%~dp0"
cd backend\dfbs-app

set "LOG=target\dfbs-test.log"
if exist "%LOG%" del /f /q "%LOG%"

call mvnw.cmd test > "%LOG%" 2>&1
set "RC=%ERRORLEVEL%"

echo.
if not "%RC%"=="0" goto FAIL

echo TESTS PASS
echo Log: backend\dfbs-app\%LOG%
goto END

:FAIL
echo TESTS FAIL (exit code=%RC%)
echo Log: backend\dfbs-app\%LOG%
echo.
echo Please open the log file above and copy the failure part here.

:END
echo.
pause
exit /b %RC%
'@ | Set-Content -Encoding ASCII "D:\dfbs\DFBS-验收测试.bat"
