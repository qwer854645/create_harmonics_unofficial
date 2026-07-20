"""Extract record-player assets from createharmonics JAR + F:\\web_player."""
from __future__ import annotations

import shutil
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEST = ROOT / "common/src/main/resources/assets/create_resonance"
JAR = Path(r"F:\others\createharmonics-neoforge-1.21.1-1.2.0.jar")
WEB_PLAYER = Path(r"F:\web_player")
USER_DISC = Path(r"F:\others\1111.png")

# JAR createharmonics path -> create_resonance relative path under assets/
JAR_MAP = {
    # Andesite resonator from official andesite_jukebox
    "textures/block/andesite_jukebox/gearbox_bottom.png": "textures/block/andesite_resonator/gearbox_bottom.png",
    "textures/block/andesite_jukebox/gearbox_top.png": "textures/block/andesite_resonator/gearbox_top.png",
    "textures/block/andesite_jukebox/jukebox_casing.png": "textures/block/andesite_resonator/jukebox_casing.png",
    "textures/block/andesite_jukebox/particle.png": "textures/block/andesite_resonator/particle.png",
    # Press
    "textures/block/record_press_base/base_side.png": "textures/block/resonance_press/base_side.png",
    "textures/block/record_press_base/base_top.png": "textures/block/resonance_press/base_top.png",
    # Disc blank item
    "textures/item/ethereal_record_base/base.png": "textures/item/resonance_disc_blank/base.png",
    # GUI (press / icons) — skip logo (keep Resonance branding)
    "textures/gui/icons.png": "textures/gui/icons.png",
    "textures/gui/record_press_base.png": "textures/gui/record_press_base.png",
    # Sounds
    "sounds/glitter.ogg": "sounds/glitter.ogg",
    "sounds/sliding_stone.ogg": "sounds/sliding_stone.ogg",
}


def main() -> None:
    if not JAR.exists():
        raise SystemExit(f"JAR missing: {JAR}")
    if not WEB_PLAYER.is_dir():
        raise SystemExit(f"web_player missing: {WEB_PLAYER}")

    # 1) Brass resonator from F:\web_player
    brass = DEST / "textures/block/brass_resonator"
    brass.mkdir(parents=True, exist_ok=True)
    for name in ("gearbox_bottom.png", "gearbox_top.png", "jukebox_casing.png", "particle.png"):
        src = WEB_PLAYER / name
        if not src.exists():
            print("MISSING web_player:", name)
            continue
        shutil.copy2(src, brass / name)
        print(f"web_player -> brass_resonator/{name} ({src.stat().st_size} bytes)")

    # 2) Everything else from harmonics JAR
    with zipfile.ZipFile(JAR) as z:
        for jar_rel, dest_rel in JAR_MAP.items():
            entry = f"assets/createharmonics/{jar_rel}"
            if entry not in z.namelist():
                print("MISSING in jar:", entry)
                continue
            data = z.read(entry)
            out = DEST / dest_rel
            out.parent.mkdir(parents=True, exist_ok=True)
            out.write_bytes(data)
            print(f"jar -> {dest_rel} ({len(data)} bytes)")

    # 3) Disc visual: prefer user 1111.png, else item base
    visual = DEST / "textures/block/resonance_disc_visual/resonance_disc.png"
    visual.parent.mkdir(parents=True, exist_ok=True)
    if USER_DISC.exists():
        shutil.copy2(USER_DISC, visual)
        print(f"1111.png -> resonance_disc_visual/resonance_disc.png ({USER_DISC.stat().st_size} bytes)")
    else:
        base = DEST / "textures/item/resonance_disc_blank/base.png"
        shutil.copy2(base, visual)
        print("copied blank base -> resonance_disc visual")

    stale = DEST / "textures/block/resonance_disc_visual/webdisc.png"
    if stale.exists():
        stale.unlink()
        print("removed stale webdisc.png")

    print("done")


if __name__ == "__main__":
    main()
