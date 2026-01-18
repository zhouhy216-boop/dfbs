@echo off
setlocal

echo SCRIPT_PATH=%~f0
echo SCRIPT_DIR=%~dp0
echo.

cd /d "%~dp0"

echo ===========================================
echo DFBS - Update (git pull) and Run Tests
echo ===========================================
echo.

echo ===== git pull =====
git pull

REM If git pull failed, ERRORLEVEL will be >= 1
if errorlevel 1 (
  echo.
  echo UPDATE FAIL
  echo Please fix git pull first, then re-run this script.
  echo.
  pause
  exit /b 1
)

echo.
echo ===== run tests =====
call "%~dp0DFBS-test.bat"

REM Preserve tests exit code
set "TRC=%ERRORLEVEL%"

echo.
if "%TRC%"=="0" (
  echo ALL DONE: UPDATE OK + TESTS PASS
) else (
  echo ALL DONE: UPDATE OK + TESTS FAIL
)

echo.
pause
exit /b %TRC%