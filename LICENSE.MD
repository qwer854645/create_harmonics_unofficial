# License Overview

This repository is an **independent addon** (**Create: Webdisc**) derived from MIT code in [bitmochibit/createharmonics](https://github.com/bitmochibit/createharmonics). It is **not** affiliated with the upstream author's brand or ARR assets.

---

## Assets License

The following paths in this fork contain **redistributable replacement content** that is **not** the All Rights Reserved (ARR) artwork from upstream:

- `common/src/main/resources/assets/create_webdisc/` — textures, sounds, models, ponder scenes
- `common/src/main/resources/data/create_webdisc/structure/` — GameTest structure files (regenerated)

Copyright © qwer854645 (Create: Webdisc)

You may use, modify, and redistribute these files under the same terms as the MIT code license below, provided you retain this notice.

### What was replaced

Upstream ARR assets were removed, including:

- Webdisc item and block textures
- Andesite jukebox and record press base art
- GUI textures and custom sounds
- Blockbench (`.bbmodel`) source files
- Upstream mod logo and translation-tool web assets under `docs/`

### Asset sources in this fork

| Category | Examples | Origin |
|----------|----------|--------|
| Custom art | Record textures, jukebox/press base textures | Authored for this derivative edition |
| Script-generated placeholders | `gui/logo_small.png`, `gui/icons.png`, some particles | `tools/generate_assets.py` |
| Regenerated data files | `ponder/*.nbt`, `data/.../structure/*.nbt` | `tools/generate_ponder_nbt.py`, `tools/patch_gametest_nbt.py` |
| Procedural sounds | `sounds/*.ogg` | `tools/generate_assets.py` |

### Regenerating assets

```bash
python tools/generate_assets.py
python tools/generate_ponder_nbt.py
python tools/patch_gametest_nbt.py
```

### Upstream asset notice

The original mod's assets remain **All Rights Reserved** in the upstream repository. Do not copy them into this fork or redistribute them under this project's asset terms.

### Runtime references (not bundled)

Some block models reference textures from the **Create** mod at runtime (for example `create:block/brass_casing`). Those assets are not included in this repository and remain subject to Create's license.

---

## Code License (MIT)

All non-asset source files are based on the upstream project and remain under the **MIT License**:

MIT License

Copyright © Leonardo Importuna

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Fork modifications by qwer854645 are also distributed under the MIT License.

---

## Third-party software

This mod may download or use external binaries at runtime (with user consent):

- **ffmpeg** — https://ffmpeg.org/
- **yt-dlp** — https://github.com/yt-dlp/yt-dlp (Unlicense)

See the mod's in-game library setup screen for installation details. A Bilibili compatibility patch for the bundled yt-dlp workflow can be reapplied with `tools/repatch-ytdlp-bilibili.ps1`.

### Shaded dependency

- **XZ for Java** (`org.tukaani:xz`) — Public Domain, relocated in the release JAR

See [NOTICE.MD](NOTICE.MD) for additional attribution.
