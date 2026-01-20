# DFBS-GEN-PROJECT-FILES.ps1 - regenerate PROJECT_FILES.md (safe)
# - Uses python gen_project_files.py
# - Always write FULL output log to logs\
# - Keep window open (Press Enter to close)
param([switch]$NoPause)

$Root   = $PSScriptRoot
$LogDir = Join-Path $Root "logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

$Ts = Get-Date -Format "yyyyMMdd_HHmmss"
$LogFile = Join-Path $LogDir ("dfbs-gen-project-files_{0}.log" -f $Ts)

function Write-LogHeader {
  "==================================================" | Out-File -FilePath $LogFile -Encoding UTF8
  "DFBS GEN PROJECT FILES" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  ("Time: {0}" -f (Get-Date)) | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  ("Root: {0}" -f $Root) | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  "==================================================" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  "" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
}

function Run-Cmd {
  param(
    [Parameter(Mandatory=$true)][string]$WorkingDirectory,
    [Parameter(Mandatory=$true)][string]$CommandLine
  )

  $psi = New-Object System.Diagnostics.ProcessStartInfo
  $psi.FileName = "cmd.exe"
  $psi.Arguments = "/c " + $CommandLine
  $psi.WorkingDirectory = $WorkingDirectory
  $psi.UseShellExecute = $false
  $psi.RedirectStandardOutput = $true
  $psi.RedirectStandardError  = $true
  $psi.CreateNoWindow = $true

  $p = New-Object System.Diagnostics.Process
  $p.StartInfo = $psi
  [void]$p.Start()

  while (-not $p.HasExited) {
    while (-not $p.StandardOutput.EndOfStream) {
      $line = $p.StandardOutput.ReadLine()
      $line | Out-File -FilePath $LogFile -Encoding UTF8 -Append
      Write-Host $line
    }
    while (-not $p.StandardError.EndOfStream) {
      $line = $p.StandardError.ReadLine()
      $line | Out-File -FilePath $LogFile -Encoding UTF8 -Append
      Write-Host $line
    }
    Start-Sleep -Milliseconds 50
  }

  while (-not $p.StandardOutput.EndOfStream) {
    $line = $p.StandardOutput.ReadLine()
    $line | Out-File -FilePath $LogFile -Encoding UTF8 -Append
    Write-Host $line
  }
  while (-not $p.StandardError.EndOfStream) {
    $line = $p.StandardError.ReadLine()
    $line | Out-File -FilePath $LogFile -Encoding UTF8 -Append
    Write-Host $line
  }

  return $p.ExitCode
}

try {
  Write-Host "=================================================="
  Write-Host "DFBS GEN PROJECT FILES"
  Write-Host "Time: $(Get-Date)"
  Write-Host "Root: $Root"
  Write-Host "Log : $LogFile"
  Write-Host "=================================================="
  Write-Host ""

  Write-LogHeader

  # 这里保持你们既有做法：python gen_project_files.py
  "[RUN] python .\gen_project_files.py" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  Write-Host "[RUN] python .\gen_project_files.py"
  $ec = Run-Cmd -WorkingDirectory $Root -CommandLine "python .\gen_project_files.py"
  if ($ec -ne 0) { throw "gen_project_files.py failed (exit code=$ec)" }

  "" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  "[OK] GEN PROJECT FILES OK" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  Write-Host ""
  Write-Host "[OK] GEN PROJECT FILES OK"
}
catch {
  "" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  ("[FAILED] {0}" -f $_.Exception.Message) | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  Write-Host ""
  Write-Host "[FAILED] $($_.Exception.Message)"
  Write-Host "Please open log: $LogFile"
  if (-not $NoPause) { Read-Host "Press Enter to close" }
  exit 1
}

if (-not $NoPause) { Read-Host "Press Enter to close" }
exit 0
