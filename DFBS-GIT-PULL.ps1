# DFBS-GIT-PULL.ps1 - Safe git pull with clear progress and diagnostics (Win11 + PS 7.x)
param([switch]$NoPause)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

. "$PSScriptRoot\DFBS-UTILS.ps1"

$sw = [System.Diagnostics.Stopwatch]::StartNew()
$root = Resolve-RepoRoot
$logFile = Start-RunLog "dfbs-git-pull"

try {
    Write-RunHeader "GIT PULL (safe)" $logFile
    $step = 0
    $total = 6

    # [1/6] Preflight: git + git repo
    $step++; Write-Step "[$step/$total] Checking Git and repository..."
    Assert-Git
    Push-Location $root
    try {
        $null = & git rev-parse --is-inside-work-tree 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Fail "Current folder is not a Git repository."
            throw "Not a git repo."
        }
    } finally { Pop-Location }
    "Step $step : git + repo check" | Out-File -FilePath $logFile -Encoding UTF8 -Append
    Write-Ok "  [$step/$total] Git and repository OK"

    # [2/6] Working tree clean?
    $step++; Write-Step "[$step/$total] Checking for uncommitted changes..."
    $porcelain = & git -C $root status --porcelain 2>&1 | Out-String
    $porcelain | Out-File -FilePath $logFile -Encoding UTF8 -Append
    if ($porcelain.Trim()) {
        Write-Fail "Working tree has uncommitted changes. Commit or stash them before pulling."
        Write-Host "Summary of changes:"
        Write-Host $porcelain
        Write-Host "Log file: $logFile"
        if (-not $NoPause) { Read-Host "Press Enter to close" }
        exit 1
    }
    Write-Ok "  [$step/$total] Working tree clean"

    # [3/6] Show branch + remote
    $step++; Write-Step "[$step/$total] Current branch and remote..."
    Invoke-External -StepName "git status" -CommandLine "git status" -LogFile $logFile -WorkingDirectory $root
    Invoke-External -StepName "git remote -v" -CommandLine "git remote -v" -LogFile $logFile -WorkingDirectory $root
    Write-Ok "  [$step/$total] Branch and remote shown"

    # [4/6] Confirm
    $step++; Write-Step "[$step/$total] Confirmation..."
    $ans = Read-Host "Type YES to run 'git pull' (anything else = cancel)"
    if ($ans -ne "YES") {
        Write-Warn "Pull cancelled."
        $sw.Stop()
        Write-Host ""
        Write-Ok ("SUCCESS (cancelled). Elapsed: {0:N1}s. Log: {1}" -f $sw.Elapsed.TotalSeconds, $logFile)
        if (-not $NoPause) { Read-Host "Press Enter to close" }
        exit 0
    }
    Write-Ok "  [$step/$total] Confirmed"

    # [5/6] git pull
    $step++; Write-Step "[$step/$total] Running git pull..."
    Invoke-External -StepName "git pull" -CommandLine "git pull" -LogFile $logFile -WorkingDirectory $root
    Write-Ok "  [$step/$total] Pull completed"

    # [6/6] Final status
    $step++; Write-Step "[$step/$total] Final status..."
    Invoke-External -StepName "git status (final)" -CommandLine "git status" -LogFile $logFile -WorkingDirectory $root
    Write-Ok "  [$step/$total] Done"

    $sw.Stop()
    Write-Host ""
    Write-Ok ("SUCCESS. Elapsed: {0:N1}s. Log: {1}" -f $sw.Elapsed.TotalSeconds, $logFile)
}
catch {
    $sw.Stop()
    Write-Host ""
    Write-Fail ("FAILED at step $step. Log: {0}" -f $logFile)
    Write-Host $_.Exception.Message
    if (-not $NoPause) { Read-Host "Press Enter to close" }
    exit 1
}

if (-not $NoPause) { Read-Host "Press Enter to close" }
exit 0
