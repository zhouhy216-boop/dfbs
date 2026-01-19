@echo off
setlocal enabledelayedexpansion

REM ==================================================
REM DFBS TEST - run backend tests only (SAFE)
REM - NO git pull
REM - NO docker compose
REM - NO app start
REM ==================================================

set "ROOT=%~dp0"
set "APP=%ROOT%backend\dfbs-app"

echo ==================================================
echo DFBS TEST - mvnw clean test
echo ==================================================

if not exist "%APP%\mvnw.cmd" (
  echo [ERROR] mvnw.cmd not found: "%APP%\mvnw.cmd"
  exit /b 1
)

pushd "%APP%" >nul

echo [RUN] mvnw clean test
call "%APP%\mvnw.cmd" clean test
set "EC=%ERRORLEVEL%"

popd >nul

if not "%EC%"=="0" (
  echo.
  echo [FAILED] mvnw clean test (exit code=%EC%)
  echo Hint: check log file: backend\dfbs-app\target\dfbs-test.log (if you have it)
  exit /b %EC%
)

echo.
echo [OK] TESTS PASS
exit /b 0
