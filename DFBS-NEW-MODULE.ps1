# DFBS-NEW-MODULE.ps1 - generate a new module skeleton (safe)
# - Uses python tools\new_module.py <module>
# - Always write FULL output log to logs\
# - Keep window open (Press Enter to close)
param([switch]$NoPause)

$Root   = $PSScriptRoot
$LogDir = Join-Path $Root "logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

$Ts = Get-Date -Format "yyyyMMdd_HHmmss"
$LogFile = Join-Path $LogDir ("dfbs-new-module_{0}.log" -f $Ts)

function Write-LogHeader {
  "==================================================" | Out-File -FilePath $LogFile -Encoding UTF8
  "DFBS NEW MODULE" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
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
  Write-Host "DFBS NEW MODULE"
  Write-Host "Time: $(Get-Date)"
  Write-Host "Root: $Root"
  Write-Host "Log : $LogFile"
  Write-Host "=================================================="
  Write-Host ""

  Write-LogHeader

  $Module = Read-Host "Enter module name (e.g. quote, shipping, repair)"
  if ([string]::IsNullOrWhiteSpace($Module)) {
    throw "Module name is empty."
  }

  # 基础校验：只允许小写字母/数字/下划线，避免生成奇怪路径
  if ($Module -notmatch '^[a-z0-9_]+$') {
    throw "Invalid module name: '$Module'. Only [a-z0-9_] allowed."
  }

  $tool = ".\tools\new_module.py"
  if (!(Test-Path (Join-Path $Root $tool))) {
    throw "tool not found: $tool"
  }

  "" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  ("[RUN] python {0} {1}" -f $tool, $Module) | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  Write-Host ("[RUN] python {0} {1}" -f $tool, $Module)

  $ec = Run-Cmd -WorkingDirectory $Root -CommandLine ("python {0} {1}" -f $tool, $Module)
  if ($ec -ne 0) { throw "new_module.py failed (exit code=$ec)" }

  "" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  "[OK] NEW MODULE OK" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  Write-Host ""
  Write-Host "[OK] NEW MODULE OK"
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
