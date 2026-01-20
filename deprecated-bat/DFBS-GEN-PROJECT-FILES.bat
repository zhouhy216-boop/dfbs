@echo off
setlocal

cd /d "%~dp0"

echo ===========================================
echo DFBS - Generate PROJECT_FILES.md
echo ===========================================
echo.

python .\gen_project_files.py
if errorlevel 1 (
  echo.
  echo FAIL: gen_project_files.py
  pause
  exit /b 1
)

echo.
if exist PROJECT_FILES.md (
  for %%A in (PROJECT_FILES.md) do (
    echo OK: PROJECT_FILES.md written
    echo Size: %%~zA bytes
    echo Time: %%~tA
  )
) else (
  echo FAIL: PROJECT_FILES.md not found
  pause
  exit /b 1
)

echo.
echo ===== git status (to confirm changes) =====
git status

echo.
pause
exit /b 0
