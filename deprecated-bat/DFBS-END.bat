@echo off
setlocal

echo SCRIPT_PATH=%~f0
echo SCRIPT_DIR=%~dp0
echo.

cd /d "%~dp0"

echo ==================================================
echo DFBS END - git add + commit + push (if needed)
echo ==================================================
echo.

echo ===== git status =====
git status

echo.
echo ===== git add . =====
git add .

echo.
echo ===== git commit =====
git commit -m "sync"
if errorlevel 1 (
  echo No changes to commit.
)

echo.
echo ===== git push =====
git push
if errorlevel 1 (
  echo.
  echo FAIL: git push
  echo Please check network/login.
  echo.
  pause
  exit /b 1
)

echo.
echo ALL DONE: END OK
echo.
pause
exit /b 0
