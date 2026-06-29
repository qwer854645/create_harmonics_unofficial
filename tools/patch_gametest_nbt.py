#!/usr/bin/env python3
"""Replace upstream-identical GameTest structure NBT files with distinct filler blocks."""

from __future__ import annotations

import gzip
import io
from pathlib import Path

from nbtlib import File, String

ROOT = Path(__file__).resolve().parents[1]
STRUCTURE_DIR = ROOT / "common" / "src" / "main" / "resources" / "data" / "createharmonics" / "structure"

# Decorative swaps only; functional Create / mod blocks stay unchanged.
NAME_REPLACEMENTS = {
    "minecraft:snow_block": "minecraft:gray_concrete",
    "minecraft:pink_concrete": "minecraft:light_gray_concrete",
    "minecraft:pink_stained_glass_pane": "minecraft:light_gray_stained_glass_pane",
}


def patch_structure(path: Path) -> None:
    raw = gzip.decompress(path.read_bytes())
    nbt = File.from_fileobj(io.BytesIO(raw), byteorder="big")

    for state in nbt["palette"]:
        name = str(state["Name"])
        if name in NAME_REPLACEMENTS:
            state["Name"] = String(NAME_REPLACEMENTS[name])

    nbt.save(str(path), gzipped=True)
    print(f"Patched {path.relative_to(ROOT)}")


def main() -> None:
    for path in sorted(STRUCTURE_DIR.glob("*.nbt")):
        patch_structure(path)


if __name__ == "__main__":
    main()
