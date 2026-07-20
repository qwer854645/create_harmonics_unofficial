"""Extract original record-player / disc textures from the built JAR back into source."""
from __future__ import annotations

import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEST = ROOT / "common/src/main/resources/assets/create_resonance"
JAR = ROOT / "neoforge/build/libs/create_resonance-neoforge-1.21.1-1.0.0.jar"

# Map jar entry -> source path under assets/create_resonance/
# Prefer original names from the JAR (built before bad regenerations).
EXTRACT = [
    # Resonators
    "textures/block/andesite_resonator/gearbox_bottom.png",
    "textures/block/andesite_resonator/gearbox_top.png",
    "textures/block/andesite_resonator/jukebox_casing.png",
    "textures/block/andesite_resonator/particle.png",
    "textures/block/brass_resonator/gearbox_bottom.png",
    "textures/block/brass_resonator/gearbox_top.png",
    "textures/block/brass_resonator/jukebox_casing.png",
    "textures/block/brass_resonator/particle.png",
    # Press
    "textures/block/resonance_press/base_side.png",
    "textures/block/resonance_press/base_top.png",
    # Disc item + visual (jar still has webdisc.png name for visual)
    "textures/item/resonance_disc_blank/base.png",
    "textures/block/resonance_disc_visual/webdisc.png",
    # GUI used by press / players
    "textures/gui/icons.png",
    "textures/gui/logo_small.png",
    "textures/gui/record_press_base.png",
]


def main() -> None:
    if not JAR.exists():
        raise SystemExit(f"JAR not found: {JAR}")

    with zipfile.ZipFile(JAR) as z:
        for rel in EXTRACT:
            entry = f"assets/create_resonance/{rel}"
            if entry not in z.namelist():
                print("MISSING in jar:", entry)
                continue
            data = z.read(entry)
            # visual: jar name webdisc.png -> source resonance_disc.png (code expects this)
            if rel.endswith("resonance_disc_visual/webdisc.png"):
                out = DEST / "textures/block/resonance_disc_visual/resonance_disc.png"
            else:
                out = DEST / rel
            out.parent.mkdir(parents=True, exist_ok=True)
            out.write_bytes(data)
            print(f"extracted {entry} ({len(data)} bytes) -> {out.relative_to(ROOT)}")

        # remove stale webdisc.png in source if present
        stale = DEST / "textures/block/resonance_disc_visual/webdisc.png"
        if stale.exists():
            stale.unlink()
            print("removed stale", stale.relative_to(ROOT))

    print("done")


if __name__ == "__main__":
    main()
