# Changelog — Create: Webdisc

Fork changelog for **qwer854645/create_webdisc** (mod ID `create_webdisc`).  
Upstream release history for the official Create: Harmonics mod is not reproduced here.

## 2.0.0

### Rebrand & compliance
- Rebranded from `createharmonics` to `create_webdisc`
- Removed upstream All Rights Reserved artwork; added script-generated replacement assets
- Rewrote README, LICENSE, and NOTICE for derivative-work compliance
- Bundle LICENSE.MD in release JAR (`META-INF/`)

### Features (this fork)
- **Brass Web Player** — side-face seek GUI with live duration, precise time input, and restart-from-beginning
- Chinese (`zh_cn`) tooltips and Ponder translations
- Bilibili yt-dlp patch script (`tools/repatch-ytdlp-bilibili.ps1`)

### Fixes
- Seek GUI rendering and sync
- Restart-from-beginning when client audio player is stopped
- Real-time duration sync from client audio stream to server
