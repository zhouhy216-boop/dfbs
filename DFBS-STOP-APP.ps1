# DFBS-STOP-APP.ps1 - stop process that is LISTENING on 8080 (safe)
# - Only kills processes listening on TCP 8080
# - Shows PID and executable path before killing
param([switch]$NoPause)

$Root   = $PSScriptRoot
$LogDir = Join-Path $Root "logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
$LogFile = Join-Path $LogDir ("dfbs-stop-app_{0}.log" -f (Get-Date -Format "yyyyMMdd_HHmmss"))

function Log([string]$s) {
  $s | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  Write-Host $s
}

Log "=================================================="
Log "DFBS STOP APP - kill LISTENING TCP :8080"
Log ("Time: {0}" -f (Get-Date))
Log ("Log : {0}" -f $LogFile)
Log "=================================================="
Log ""

try {
  # Find lines that are LISTENING on 8080
  $lines = netstat -ano | findstr ":8080" | findstr "LISTENING"
  if (-not $lines) {
    Log "[OK] No LISTENING process on :8080"
    if (-not $NoPause) { Read-Host "Press Enter to close" }
    exit 0
  }

  Log "[INFO] Found LISTENING on :8080:"
  foreach ($l in $lines) { Log $l }

  # Extract ProcId(s) (last token of each line)
  $ProcIds = @()
  foreach ($l in $lines) {
    $tokens = ($l -split "\s+") | Where-Object { $_ -ne "" }
    if ($tokens.Count -ge 1) {
      $ProcIdText = $tokens[$tokens.Count - 1]
      if ($ProcIdText -match '^\d+$') { $ProcIds += [int]$ProcIdText }
    }
  }
  $ProcIds = $ProcIds | Sort-Object -Unique

  Log ""
  foreach ($ProcId in $ProcIds) {
    try {
      $proc = Get-Process -Id $ProcId -ErrorAction Stop

      $path = ""
      try {
        $path = (Get-Process -Id $ProcId -FileVersionInfo).FileName
      } catch { $path = "" }

      Log ("[INFO] ProcId {0} Name={1}" -f $ProcId, $proc.ProcessName)
      if ($path) { Log ("[INFO] Path {0}" -f $path) }

      Log ("[RUN] taskkill /PID {0} /F" -f $ProcId)
      cmd /c ("taskkill /PID {0} /F" -f $ProcId) 2>&1 | ForEach-Object { Log $_ }

      Log ("[OK] Killed ProcId {0}" -f $ProcId)
      Log ""
    }
    catch {
      Log ("[WARN] Failed to kill ProcId {0}: {1}" -f $ProcId, $_.Exception.Message)
      Log ""
    }
  }

  Log "[OK] STOP APP DONE"
}
catch {
  Log ("[FAILED] {0}" -f $_.Exception.Message)
  if (-not $NoPause) { Read-Host "Press Enter to close" }
  exit 1
}

if (-not $NoPause) { Read-Host "Press Enter to close" }
exit 0
