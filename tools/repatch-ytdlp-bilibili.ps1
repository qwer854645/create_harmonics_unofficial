# Re-applies local Bilibili workarounds to the dev yt-dlp venv.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$ytdlpDir = Join-Path $root 'neoforge\run\audio_providers\yt-dlp'
$bilibili = Join-Path $ytdlpDir 'venv\Lib\site-packages\yt_dlp\extractor\bilibili.py'

if (-not (Test-Path $bilibili)) {
    if (-not (Test-Path (Join-Path $ytdlpDir 'venv'))) {
        python -m venv (Join-Path $ytdlpDir 'venv')
        & (Join-Path $ytdlpDir 'venv\Scripts\pip.exe') install 'yt-dlp==2026.06.09'
    } else {
        throw "Missing bilibili.py at $bilibili"
    }
}

$content = Get-Content $bilibili -Raw

$content = $content -replace "https://api\.bilibili\.com/x/player/wbi/playurl", 'https://api.bilibili.com/x/player/playurl'

if ($content -notmatch "_extract_from_view_api") {
    throw 'Expected patch anchor _extract_from_view_api not found; bilibili.py layout may have changed.'
}

Set-Content -Path $bilibili -Value $content -NoNewline

if (-not (Test-Path (Join-Path $ytdlpDir 'yt-dlp.exe.official'))) {
    Copy-Item (Join-Path $ytdlpDir 'yt-dlp.exe') (Join-Path $ytdlpDir 'yt-dlp.exe.official') -ErrorAction SilentlyContinue
}
Copy-Item (Join-Path $ytdlpDir 'venv\Scripts\yt-dlp.exe') (Join-Path $ytdlpDir 'yt-dlp.exe') -Force

Write-Host 'yt-dlp Bilibili patches applied.'
