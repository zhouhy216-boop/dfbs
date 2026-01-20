# DFBS-END.ps1 - git add + commit + push (safe)
# - Always write FULL output log to logs\
# - Keep window open (Press Enter to close)
param([switch]$NoPause)

$Root   = $PSScriptRoot
$LogDir = Join-Path $Root "logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

$Ts = Get-Date -Format "yyyyMMdd_HHmmss"
$LogFile = Join-Path $LogDir ("dfbs-end_{0}.log" -f $Ts)

function Write-LogHeader {
  "==================================================" | Out-File -FilePath $LogFile -Encoding UTF8
  "DFBS END - git add + commit + push" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
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
  Write-Host "DFBS END - git add + commit + push"
  Write-Host "Time: $(Get-Date)"
  Write-Host "Root: $Root"
  Write-Host "Log : $LogFile"
  Write-Host "=================================================="
  Write-Host ""

  Write-LogHeader

  # 1) git status
  "[RUN] git status" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  Write-Host "[RUN] git status"
  $ec = Run-Cmd -WorkingDirectory $Root -CommandLine "git status"
  if ($ec -ne 0) { throw "git status failed (exit code=$ec)" }

  # 2) git add .
  "" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  "[RUN] git add ." | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  Write-Host ""
  Write-Host "[RUN] git add ."
  $ec = Run-Cmd -WorkingDirectory $Root -CommandLine "git add ."
  if ($ec -ne 0) { throw "git add failed (exit code=$ec)" }

  # 3) git commit -m "sync"（无改动时允许失败）
  "" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  "[RUN] git commit -m ""sync""" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  Write-Host ""
  Write-Host "[RUN] git commit -m ""sync"""
  $ecCommit = Run-Cmd -WorkingDirectory $Root -CommandLine "git commit -m ""sync"""
  # 不强制要求 commit 成功：没变更会返回非 0，这是正常情况

  # 4) git push（无论 commit 是否成功都执行 push；没提交就会提示 up-to-date）
  "" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  "[RUN] git push" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  Write-Host ""
  Write-Host "[RUN] git push"
  $ec = Run-Cmd -WorkingDirectory $Root -CommandLine "git push"
  if ($ec -ne 0) { throw "git push failed (exit code=$ec)" }

  "" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  "[OK] END OK" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  Write-Host ""
  Write-Host "[OK] END OK"
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
