@echo off
setlocal enabledelayedexpansion

REM ==================================================
REM DFBS TEST - run backend tests only (SAFE)
REM - NO git pull
REM - NO docker compose
REM - NO app start
REM - ALWAYS write log to: logs\dfbs-test.bat.log
REM - PAUSE on failure (so window won't flash close)
REM ==================================================

set "ROOT=%~dp0"
set "APP=%ROOT%backend\dfbs-app"
set "LOGDIR=%ROOT%logs"
set "LOGFILE=%LOGDIR%\dfbs-test.bat.log"

if not exist "%LOGDIR%" mkdir "%LOGDIR%" >nul 2>&1

echo ================================================== > "%LOGFILE%"
echo DFBS TEST - mvnw clean test >> "%LOGFILE%"
echo Time: %date% %time% >> "%LOGFILE%"
echo Root: %ROOT% >> "%LOGFILE%"
echo App : %APP% >> "%LOGFILE%"
echo ================================================== >> "%LOGFILE%"
echo.

echo ==================================================
echo DFBS TEST - mvnw clean test
echo Log: %LOGFILE%
echo ==================================================

if not exist "%APP%\mvnw.cmd" (
  echo [ERROR] mvnw.cmd not found: "%APP%\mvnw.cmd" >> "%LOGFILE%"
  echo [ERROR] mvnw.cmd not found: "%APP%\mvnw.cmd"
  echo.
  echo [FAILED] Please open log: %LOGFILE%
  pause
  exit /b 1
)

pushd "%APP%" >nul

echo [RUN] mvnw clean test >> "%LOGFILE%"
echo [RUN] mvnw clean test

call "%APP%\mvnw.cmd" clean test >> "%LOGFILE%" 2>&1
set "EC=%ERRORLEVEL%"

popd >nul

if not "%EC%"=="0" (
  echo. >> "%LOGFILE%"
  echo [FAILED] mvnw clean test (exit code=%EC%) >> "%LOGFILE%"
  echo.
  echo [FAILED] mvnw clean test (exit code=%EC%)
  echo Please open log: %LOGFILE%
  echo.
  pause
  exit /b %EC%
)

echo. >> "%LOGFILE%"
echo [OK] TESTS PASS >> "%LOGFILE%"

echo.
echo [OK] TESTS PASS
exit /b 0