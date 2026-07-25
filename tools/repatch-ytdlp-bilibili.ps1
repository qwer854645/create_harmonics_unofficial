# Re-applies Bilibili workarounds for Create: Resonance / Harmonics WITHOUT needing a new jar.
#
# What it does:
# 1) Ensures a local yt-dlp venv exists and patches bilibili.py (wbi/playurl -> playurl)
# 2) Installs a small yt-dlp.exe wrapper that runs: venv\Scripts\python.exe -m yt_dlp
#    Stock jars only launch yt-dlp.exe; this makes the venv patches take effect.
#    (Does NOT use the fragile 100KB venv Scripts launcher — breaks on paths with '&'.)
#
# Usage:
#   .\tools\repatch-ytdlp-bilibili.ps1
#   .\tools\repatch-ytdlp-bilibili.ps1 -YtdlpDir "F:\MC\ser\.minecraft\versions\Create_Craft&Quiet\audio_providers\yt-dlp"

param(
    [string]$YtdlpDir = ""
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

if (-not $YtdlpDir) {
    $YtdlpDir = Join-Path $root 'neoforge\run\audio_providers\yt-dlp'
}

New-Item -ItemType Directory -Path $YtdlpDir -Force | Out-Null
$bilibili = Join-Path $YtdlpDir 'venv\Lib\site-packages\yt_dlp\extractor\bilibili.py'
$pythonCandidates = @(
    "$env:LOCALAPPDATA\Programs\Python\Python312\python.exe",
    "$env:LOCALAPPDATA\Programs\Python\Python313\python.exe",
    'C:\Users\Administrator\AppData\Local\Programs\Python\Python312\python.exe'
)

function Get-HostPython {
    foreach ($p in $pythonCandidates) {
        if ($p -and (Test-Path -LiteralPath $p)) { return $p }
    }
    $cmd = Get-Command python -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    return $null
}

# Recreate venv if missing or broken (pyvenv.cfg home points elsewhere)
$needVenv = -not (Test-Path -LiteralPath $bilibili)
$venvPython = Join-Path $YtdlpDir 'venv\Scripts\python.exe'
if (-not $needVenv) {
    $cfg = Join-Path $YtdlpDir 'venv\pyvenv.cfg'
    if (Test-Path -LiteralPath $cfg) {
        $homeLine = (Get-Content -LiteralPath $cfg) | Where-Object { $_ -match '^\s*home\s*=' } | Select-Object -First 1
        if ($homeLine) {
            $homePath = ($homeLine -split '=', 2)[1].Trim()
            if ($homePath -and -not (Test-Path -LiteralPath $homePath)) { $needVenv = $true }
        }
    }
    $probe = Start-Process -FilePath $venvPython -ArgumentList @('-c', 'print(1)') -Wait -PassThru -WindowStyle Hidden -ErrorAction SilentlyContinue
    if (-not $probe -or $probe.ExitCode -ne 0) { $needVenv = $true }
}

if ($needVenv) {
    $hostPy = Get-HostPython
    if (-not $hostPy) { throw 'No host Python found to create yt-dlp venv.' }
    if (Test-Path -LiteralPath (Join-Path $YtdlpDir 'venv')) {
        Remove-Item -LiteralPath (Join-Path $YtdlpDir 'venv') -Recurse -Force
    }
    Write-Host "Creating venv with $hostPy ..."
    & $hostPy -m venv (Join-Path $YtdlpDir 'venv')
    & (Join-Path $YtdlpDir 'venv\Scripts\pip.exe') install --disable-pip-version-check 'yt-dlp==2026.06.09'
}

if (-not (Test-Path -LiteralPath $bilibili)) {
    throw "Missing bilibili.py at $bilibili"
}

$utf8NoBom = New-Object System.Text.UTF8Encoding $false
$content = [System.IO.File]::ReadAllText((Resolve-Path -LiteralPath $bilibili), $utf8NoBom)
if ($content -notmatch '_download_playinfo') {
    throw 'Expected patch anchor _download_playinfo not found; bilibili.py layout may have changed.'
}

$patched = $content -replace 'https://api\.bilibili\.com/x/player/wbi/playurl', 'https://api.bilibili.com/x/player/playurl'
if ($patched -eq $content) {
    Write-Host 'Bilibili playurl endpoint already OK (non-wbi).'
} else {
    Write-Host 'Patched wbi/playurl -> playurl.'
    $content = $patched
}
[System.IO.File]::WriteAllText((Resolve-Path -LiteralPath $bilibili), $content, $utf8NoBom)

# Preserve full standalone binary as fallback / backup
$exe = Join-Path $YtdlpDir 'yt-dlp.exe'
$official = Join-Path $YtdlpDir 'yt-dlp.exe.official'
if ((Test-Path -LiteralPath $exe) -and -not (Test-Path -LiteralPath $official) -and ((Get-Item -LiteralPath $exe).Length -gt 1MB)) {
    Copy-Item -LiteralPath $exe -Destination $official -Force
    Write-Host 'Saved yt-dlp.exe.official backup.'
}

# Build / install wrapper as yt-dlp.exe (stock jar entrypoint)
$csc = Join-Path $env:WINDIR 'Microsoft.NET\Framework64\v4.0.30319\csc.exe'
$wrapperSrc = Join-Path $PSScriptRoot 'yt_dlp_venv_wrapper.cs'
$wrapperOut = Join-Path $env:TEMP 'create_resonance_yt_dlp_wrapper.exe'
if (-not (Test-Path -LiteralPath $csc)) { throw "csc.exe not found at $csc" }
if (-not (Test-Path -LiteralPath $wrapperSrc)) { throw "Missing $wrapperSrc" }

& $csc /nologo /optimize+ /target:exe /out:$wrapperOut $wrapperSrc
if ($LASTEXITCODE -ne 0) { throw 'Failed to compile yt-dlp venv wrapper.' }

Copy-Item -LiteralPath $wrapperOut -Destination $exe -Force
Write-Host "Installed yt-dlp.exe wrapper -> $exe"

& $exe --version
Write-Host 'Done. Stock Create: Resonance jars will pick this up (no jar change required).'
Write-Host 'Restart the game and retry Bilibili / network discs.'
