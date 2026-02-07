# DFBS-END.ps1 - git add + commit + push with clear progress (Win11 + PS 7.x)
# Commit may "fail" when there are no changes; push still runs.
# By default does NOT exit the shell (returns to prompt); use -ExitOnFinish for CI/automation.
param([switch]$NoPause, [switch]$ExitOnFinish)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

. "$PSScriptRoot\DFBS-UTILS.ps1"

$sw = [System.Diagnostics.Stopwatch]::StartNew()
$root = Resolve-RepoRoot
$logFile = Start-RunLog "dfbs-end"
$step = 0
$total = 5

try {
    Write-RunHeader "END (add + commit + push)" $logFile

    # [1/5] Preflight: git + repo
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
    Write-Ok "  [$step/$total] Git and repository OK"

    # [2/5] What will be committed / pushed (branch + remote; full status only in log)
    $step++; Write-Step "[$step/$total] What will be committed and pushed..."
    Invoke-External -StepName "git status" -CommandLine "git status" -LogFile $logFile -WorkingDirectory $root
    Invoke-External -StepName "git remote -v" -CommandLine "git remote -v" -LogFile $logFile -WorkingDirectory $root
    Write-Ok "  [$step/$total] Branch and remote recorded (details in log)"

    # [3/5] Staging: 3.0 quick view, 3A tracked, 3B new files (safe)
    $step++; Write-Step "[$step/$total] Staging changes (tracked + new files safely)..."
    $summary = Get-RepoStatusSummary -RepoRoot $root
    Write-Host "  Tracked changes: $($summary.TrackedCount)"
    Write-Host "  New files (untracked): $($summary.UntrackedCount)"
    if ($summary.TopUntrackedFolders -and $summary.TopUntrackedFolders.Count -gt 0) {
        Write-Host "  Top new folders:"
        foreach ($t in $summary.TopUntrackedFolders) { Write-Host "    $($t.Name): $($t.Count)" }
    }
    $includeNewFiles = $true
    if ($summary.HighRisk) {
        Write-Warn "  Large new content detected (likely dependencies/build output). Recommended: do NOT include new files now."
        $safeCmd = "git add -u"
        $forceCmd = "git add ."
        Write-Host "  Safe command (tracked only): $safeCmd"
        Write-Host "  Force include all new files: $forceCmd"
        try { Set-Clipboard -Value $safeCmd } catch { }
        $ans = Read-Host "  Type YES to include new files anyway (otherwise only tracked changes will be staged)"
        if ($ans -ne "YES") {
            $includeNewFiles = $false
            Write-Host "  Proceeding with tracked changes only. New files were NOT included; they are still on disk; nothing was deleted." -ForegroundColor Yellow
        }
    }
    $statusCounts = @{ Tracked = $summary.TrackedCount; Untracked = $summary.UntrackedCount }
    Write-Host "  [3A] Stage tracked (git add -u)..."
    Invoke-External -StepName "git add -u" -CommandLine "git add -u" -LogFile $logFile -WorkingDirectory $root -HeartbeatSec 10 -TimeoutSec 120 -StatusCounts $statusCounts
    Write-Ok "  [3A] Tracked staged"
    if ($includeNewFiles -and $summary.UntrackedCount -gt 0) {
        Write-Host "  [3B] Stage new files (excluding build/deps/junk)..."
        $pathListFile = Get-SafeUntrackedPathListFile -RepoRoot $root
        if ($pathListFile) {
            $addCmd = "git add --pathspec-from-file=`"$pathListFile`""
            Invoke-External -StepName "git add new files" -CommandLine $addCmd -LogFile $logFile -WorkingDirectory $root -HeartbeatSec 10 -TimeoutSec 120 -StatusCounts $statusCounts
            Write-Ok "  [3B] New files staged (safe list)"
        } else {
            Write-Host "  [3B] No new files to add (after exclusions)." -ForegroundColor Gray
        }
    } elseif (-not $includeNewFiles -and $summary.UntrackedCount -gt 0) {
        Write-Host "  [3B] Skipped (per your choice). New files remain on disk; nothing was deleted." -ForegroundColor Gray
    }
    Write-Ok "  [$step/$total] Staging done"

    # [4/5] git commit -m "sync" (allow failure when nothing to commit)
    $step++; Write-Step "[$step/$total] Committing (git commit -m ""sync"")..."
    $commitSucceeded = $false
    try {
        Invoke-External -StepName "git commit -m sync" -CommandLine "git commit -m ""sync""" -LogFile $logFile -WorkingDirectory $root
        $commitSucceeded = $true
    } catch {
        Write-Warn "  [$step/$total] Commit skipped (no changes or other reason). Push will still run."
    }
    if ($commitSucceeded) { Write-Ok "  [$step/$total] Committed" }

    # [5/5] git push (interactive so credential/passphrase prompts are visible)
    $step++; Write-Step "[$step/$total] Pushing to remote (git push --progress)..."
    Invoke-External -StepName "git push" -CommandLine "git push --progress" -LogFile $logFile -WorkingDirectory $root -InteractiveConsole -HeartbeatSec 10 -TimeoutSec 600
    Write-Ok "  [$step/$total] Pushed"

    $sw.Stop()
    Write-Host ""
    Write-Ok ("SUCCESS. Elapsed: {0:N1}s. Log: {1}" -f $sw.Elapsed.TotalSeconds, $logFile)
    $global:LASTEXITCODE = 0
}
catch {
    $sw.Stop()
    Write-Host ""
    Write-Fail ("FAILED at step $step. Log: {0}" -f $logFile)
    Write-Host $_.Exception.Message
    $global:LASTEXITCODE = 1
    if (-not $NoPause) { Read-Host "Press Enter to close" }
    if ($ExitOnFinish) { exit 1 }
    return
}

if (-not $NoPause) { Read-Host "Press Enter to close" }
if ($ExitOnFinish) { exit $global:LASTEXITCODE }
return
