<p align="center">
    <img src="common/src/main/resources/assets/createharmonics_unofficial/textures/gui/logo_small.png" alt="Logo">
</p>
<h1 align="center">Create: Harmonics (Unofficial)<br><br>
    <a href="https://github.com/qwer854645/create_harmonics_unofficial"><img alt="GitHub" src="https://img.shields.io/badge/GitHub-Unofficial_Fork-blue?logo=github&style=for-the-badge"></a>
</h1>

> **This is not the official Create: Harmonics mod.**  
> It is an independent redistribution fork with **replaced art assets** and a **different mod ID**.  
> Official releases: [bitmochibit/createharmonics](https://github.com/bitmochibit/createharmonics) (upstream assets are All Rights Reserved).

<h4 align="center">Required dependencies<br><br>
    <a href="https://modrinth.com/mod/kotlin-for-forge">
        <img alt="Kotlin For Forge" src="https://img.shields.io/badge/KOTLIN%20FOR%20FORGE-REQUIRED-7f52ff?logo=kotlin&labelColor=white&style=for-the-badge">
    </a>
    <a href="https://modrinth.com/mod/create">
        <img alt="Create Mod" src="https://img.shields.io/badge/Create%20Mod-REQUIRED-ecaf89?logo=Modrinth&labelColor=white&style=for-the-badge">
    </a>
</h4>

<hr>

## Quick facts

| | Official upstream | **This fork** |
|---|---|---|
| Mod ID | `createharmonics` | **`createharmonics_unofficial`** |
| Display name | Create: Harmonics | Create Harmonics (Unofficial) |
| Maintainer | Leonardo Importuna (mochibit) | qwer854645 |
| `assets/` license | All Rights Reserved | MIT (replacement art only) |
| Code license | MIT | MIT (based on upstream) |
| Can swap JARs in a save? | — | **No** — block/item IDs differ |

Do **not** install this alongside the official mod expecting the same namespace. Worlds that used `createharmonics:*` blocks will **not** migrate automatically; treat this as a separate mod.

---

## What this fork is

An unofficial NeoForge 1.21.1 edition of [Create: Harmonics](https://github.com/bitmochibit/createharmonics), kept so the project can be **built and redistributed without upstream ARR artwork**.

Gameplay is largely the same (URL audio, Ethereal Records, Andesite Jukebox, Record Press Base, Create contraption integration). Feature docs: upstream [wiki](https://github.com/bitmochibit/createharmonics/wiki) (reference only).

**Report bugs for this fork here:** [Issues](https://github.com/qwer854645/create_harmonics_unofficial/issues) — not on the upstream repo.

---

## How this fork differs from upstream

### Removed (upstream ARR or unused)

- Upstream mod logo (`logo.png`) and Blockbench (`.bbmodel`) sources
- Upstream `docs/` translation website and its image assets
- **Brass Jukebox** block art (block was already disabled in code; texture removed from this fork)

### Replaced (safe to redistribute under this fork's asset terms)

- Record item / 3D record textures
- Andesite Jukebox and Record Press Base textures
- GUI textures (`logo_small`, icons, record press screen)
- Custom sounds (procedural OGG)
- Ponder scene NBT and GameTest structure NBT (regenerated, not byte-copied from upstream)

### Added in this fork

- `zh_cn` tooltips and Ponder translations
- Bilibili yt-dlp patch script: `tools/repatch-ytdlp-bilibili.ps1`
- Clear **Unofficial** branding in mod list, creative tab, and in-game menus

### Not bundled, but referenced at runtime

- **Create** textures (e.g. `create:block/brass_casing` on the record press model) — loaded from the Create mod, not shipped in this JAR
- **ffmpeg** / **yt-dlp** — downloaded locally with user consent, or placed manually under `audio_providers/`

See [LICENSE.MD](LICENSE.MD) for the full asset/code split.

---

## What the mod does

- Craft **Ethereal Records** and bind them to online audio URLs
- Power an **Andesite Jukebox** or **Record Press Base** with Create rotation
- Play audio on contraptions; pitch and playback follow speed and redstone

Audio is streamed **client-side** from the original source (not relayed server → client). The mod may download **ffmpeg** and **yt-dlp** after asking permission, or you can supply your own binaries.

> [!IMPORTANT]
> Respect the terms of service of any site you pull audio from. This mod does not host audio.

---

## Installation

**Minecraft:** 1.21.1 · **Loader:** NeoForge · **Requires:** Create + Kotlin for Forge

1. Download **`createharmonics_unofficial-neoforge-1.21.1-*.jar`** from [Releases](https://github.com/qwer854645/create_harmonics_unofficial/releases) or build locally (below).
2. Put the JAR in your `mods` folder.
3. **Do not** use the official `createharmonics-*.jar` — mod ID and assets differ.
4. On first launch, accept the in-game prompt to install ffmpeg/yt-dlp, or install them manually (path shown in the library setup screen).

For Bilibili URLs after a fresh yt-dlp install, run:

```powershell
.\tools\repatch-ytdlp-bilibili.ps1
```

---

## Build from source

**Requirements:** JDK 21

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
cd create_harmonics_unofficial
.\gradlew.bat :neoforge:build
```

Output JAR:

```text
neoforge/build/libs/createharmonics_unofficial-neoforge-1.21.1-1.2.0.jar
```

Run client (dev):

```powershell
.\gradlew.bat :neoforge:runClient
```

Default game directory: `neoforge/run`

### Regenerate assets (optional)

```bash
python tools/generate_assets.py
python tools/generate_ponder_nbt.py
python tools/patch_gametest_nbt.py
```

Asset root: `common/src/main/resources/assets/createharmonics_unofficial/`

---

## Legal summary

| Part | License | Notes |
|------|---------|--------|
| Upstream **code** (MIT) | MIT | © Leonardo Importuna |
| This fork's **code changes** | MIT | Same as upstream base |
| This fork's **assets** | MIT | Replacement art only — see [LICENSE.MD](LICENSE.MD) |
| Upstream **original art** | All Rights Reserved | **Not included** in this repository |

This project is **not affiliated** with the upstream author, the Create team, YouTube, Bilibili, or other audio platforms. Provided as-is, without warranty.

---

## Credits

- Original mod: [bitmochibit/createharmonics](https://github.com/bitmochibit/createharmonics) by Leonardo Importuna (mochibit)
- [Create](https://github.com/Creators-of-Create/Create) — MIT
- [yt-dlp](https://github.com/yt-dlp/yt-dlp) — Unlicense (source); official `.exe` builds may be GPLv3+
- [ffmpeg](https://ffmpeg.org/) — LGPL/GPL depending on build
