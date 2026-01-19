@echo off
setlocal enabledelayedexpansion

REM ==============================
REM DFBS - New Module Generator
REM Usage: double click, then input module key + ModuleName
REM ==============================

set SCRIPT_DIR=%~dp0

echo ==================================================
echo DFBS NEW MODULE - generate module skeleton
echo ==================================================

set /p MODULE_KEY=Enter module_key (lowercase, e.g. shipment): 
set /p MODULE_NAME=Enter ModuleName (PascalCase, e.g. Shipment): 

if "%MODULE_KEY%"=="" (
  echo ERROR: module_key is empty
  pause
  exit /b 1
)

if "%MODULE_NAME%"=="" (
  echo ERROR: ModuleName is empty
  pause
  exit /b 1
)

python "%SCRIPT_DIR%tools\new_module.py" "%MODULE_KEY%" "%MODULE_NAME%"
if errorlevel 1 (
  echo ERROR: generator failed
  pause
  exit /b 1
)

echo OK: module skeleton generated.
echo Tip: run DFBS-GEN-PROJECT-FILES.bat to refresh PROJECT_FILES.md if needed.
pause