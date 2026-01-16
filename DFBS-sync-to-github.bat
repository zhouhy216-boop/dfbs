@echo off
setlocal

cd /d "%~dp0"

echo ===== git status =====
git status

echo.
echo ===== git add . =====
git add .

echo.
echo ===== git commit =====
git commit -m "sync"
if errorlevel 1 echo No changes to commit.

echo.
echo ===== git push =====
git push

echo.
echo Done.
pause
