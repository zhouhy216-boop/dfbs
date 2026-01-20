# DFBS-GIT-PULL.ps1 - SAFE git pull (with manual confirmation)
param([switch]$NoPause)

$Root   = $PSScriptRoot
$LogDir = Join-Path $Root "logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
$LogFile = Join-Path $LogDir ("dfbs-git-pull_{0}.log" -f (Get-Date -Format "yyyyMMdd_HHmmss"))

function Log([string]$s) {
  $s | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  Write-Host $s
}

Log "=================================================="
Log "DFBS GIT PULL (SAFE)"
Log ("Time: {0}" -f (Get-Date))
Log ("Root: {0}" -f $Root)
Log ("Log : {0}" -f $LogFile)
Log "=================================================="
Log ""

# Show current branch + status first
Log "[RUN] git status"
cmd /c "git status" 2>&1 | ForEach-Object { Log $_ }

Log ""
Log "[RUN] git remote -v"
cmd /c "git remote -v" 2>&1 | ForEach-Object { Log $_ }

Log ""
$ans = Read-Host "Type YES to run 'git pull' (anything else = cancel)"
if ($ans -ne "YES") {
  Log "[CANCEL] git pull not executed."
  if (-not $NoPause) { Read-Host "Press Enter to close" }
  exit 0
}

Log ""
Log "[RUN] git pull"
cmd /c "git pull" 2>&1 | ForEach-Object { Log $_ }

Log ""
Log "[RUN] git status"
cmd /c "git status" 2>&1 | ForEach-Object { Log $_ }

Log ""
Log "[OK] GIT PULL DONE"
if (-not $NoPause) { Read-Host "Press Enter to close" }
