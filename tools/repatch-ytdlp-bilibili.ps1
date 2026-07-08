# Re-applies local Bilibili workarounds to the dev yt-dlp venv.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$ytdlpDir = Join-Path $root 'neoforge\run\audio_providers\yt-dlp'
$bilibili = Join-Path $ytdlpDir 'venv\Lib\site-packages\yt_dlp\extractor\bilibili.py'

if (-not (Test-Path $bilibili)) {
    $python = @(
        "$env:LOCALAPPDATA\Programs\Python\Python312\python.exe",
        'python'
    ) | Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1

    if (-not $python) {
        throw "Missing bilibili.py and no Python found to create venv at $ytdlpDir\venv"
    }

    if (-not (Test-Path (Join-Path $ytdlpDir 'venv'))) {
        & $python -m venv (Join-Path $ytdlpDir 'venv')
        & (Join-Path $ytdlpDir 'venv\Scripts\pip.exe') install 'yt-dlp==2026.06.09'
    } else {
        throw "Missing bilibili.py at $bilibili"
    }
}

$utf8NoBom = New-Object System.Text.UTF8Encoding $false
$content = [System.IO.File]::ReadAllText($bilibili, $utf8NoBom)

if ($content -notmatch '_download_playinfo') {
    throw 'Expected patch anchor _download_playinfo not found; bilibili.py layout may have changed.'
}

$patched = $content -replace "https://api\.bilibili\.com/x/player/wbi/playurl", 'https://api.bilibili.com/x/player/playurl'

if ($patched -eq $content) {
    Write-Host 'Bilibili playurl endpoint already patched (or pattern missing).'
} else {
    $content = $patched
}

[System.IO.File]::WriteAllText($bilibili, $content, $utf8NoBom)

if (-not (Test-Path (Join-Path $ytdlpDir 'yt-dlp.exe.official'))) {
    Copy-Item (Join-Path $ytdlpDir 'yt-dlp.exe') (Join-Path $ytdlpDir 'yt-dlp.exe.official') -ErrorAction SilentlyContinue
}
Copy-Item (Join-Path $ytdlpDir 'venv\Scripts\yt-dlp.exe') (Join-Path $ytdlpDir 'yt-dlp.exe') -Force

Write-Host 'yt-dlp Bilibili patches applied.'
