<p align="center">
    <img src="common/src/main/resources/assets/createharmonics/textures/gui/logo_small.png" alt="Logo">
</p>
<h1 align="center">Create: Harmonics (Unofficial)<br><br>
    <a href="https://github.com/qwer854645/create_harmonics_unofficial"><img alt="GitHub" src="https://img.shields.io/badge/GitHub-Unofficial_Fork-blue?logo=github&style=for-the-badge"></a>
</h1>

> **Unofficial fork** of [bitmochibit/createharmonics](https://github.com/bitmochibit/createharmonics).  
> Gameplay code is based on the upstream MIT project; **textures, sounds, models, and Blockbench sources were removed and replaced** so this edition can be redistributed. See [LICENSE.MD](LICENSE.MD).

<h4 align="center">Required dependencies<br><br>
    <a href="https://modrinth.com/mod/kotlin-for-forge">
        <img alt="Kotlin For Forge" src="https://img.shields.io/badge/KOTLIN%20FOR%20FORGE-REQUIRED-7f52ff?logo=kotlin&labelColor=white&style=for-the-badge">
    </a>
    <a href="https://modrinth.com/mod/create">
        <img alt="Create Mod" src="https://img.shields.io/badge/Create%20Mod-REQUIRED-ecaf89?logo=Modrinth&labelColor=white&style=for-the-badge">
    </a>
</h4>

<hr>

## About this fork

This repository is **not** the official Create: Harmonics release. It is maintained separately and includes:

- Replacement art assets (records, jukebox, record press, GUI, sounds)
- Removal of upstream ARR files (including `.bbmodel` sources and the upstream mod logo)
- Regenerated Ponder and GameTest structure files
- Simplified project layout (the upstream `docs/` translation website is not included)
- Additional `zh_cn` translations for mod tooltips and Ponder scenes
- A yt-dlp patch script for Bilibili URL playback (`tools/repatch-ytdlp-bilibili.ps1`)

For original feature documentation, the upstream [wiki](https://github.com/bitmochibit/createharmonics/wiki) is still a useful reference. Bug reports and contributions for **this fork** should go to [this repository's issues](https://github.com/qwer854645/create_harmonics_unofficial/issues).

## What the mod does

Create: Harmonics lets you play audio from URLs in Minecraft, integrated with **Create**:

- Craft **Ethereal Records** and bind them to online audio sources
- Use the **Andesite Jukebox** and **Record Press Base** with rotational power
- Play audio on contraptions; pitch and playback follow machine speed and redstone logic

Audio is streamed **client-side** from the original source (not relayed through the game server). The mod uses **yt-dlp** and **ffmpeg** locally; you can install them automatically in-game or provide your own binaries.

> [!IMPORTANT]
> Respect the terms of service of any platform you pull audio from. This mod does not host audio content.

## Development

**Requirements:** JDK 21, Gradle (wrapper included)

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
cd create_harmonics_unofficial
.\gradlew.bat :neoforge:runClient
```

Default run directory: `neoforge/run`

### Regenerating placeholder assets

```bash
python tools/generate_assets.py
python tools/generate_ponder_nbt.py
python tools/patch_gametest_nbt.py
```

## Differences from upstream

| Item | Upstream | This fork |
|------|----------|-----------|
| Record / block textures | ARR | Replaced |
| Sounds | ARR | Replaced (procedural) |
| Blockbench sources | Included | Removed |
| Mod logo (`logo.png`) | ARR | Removed |
| `docs/` translation tool | Included | Removed |
| Ponder / GameTest NBT | Upstream layouts | Regenerated |
| License for `assets/` | ARR | MIT (replacement art) |

## Legal

- Upstream **code**: MIT (Leonardo Importuna / bitmochibit)
- This fork's **assets**: MIT — see [LICENSE.MD](LICENSE.MD)
- Upstream **original art**: All Rights Reserved — not included here

This unofficial edition is provided as-is, without warranty. The fork maintainer is not affiliated with the upstream author, Create, YouTube, Bilibili, or other audio platforms.

## Credits

### Software

- [Create](https://github.com/Creators-of-Create/Create) — MIT
- [yt-dlp](https://github.com/yt-dlp/yt-dlp) — Unlicense
- [ffmpeg](https://ffmpeg.org/) — LGPL/GPL depending on build

### Upstream project

- Original mod: [bitmochibit/createharmonics](https://github.com/bitmochibit/createharmonics) by Leonardo Importuna (mochibit)
