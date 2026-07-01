#!/usr/bin/env python3
"""Regenerate Ponder scene structure NBT files for Create Harmonics Unofficial.

These files describe block layouts for in-game tutorial scenes. They are rebuilt
from functional coordinates required by PonderScenes.kt, not copied from upstream.
"""

from __future__ import annotations

from pathlib import Path
from nbtlib import Compound, File, Int, List, String

ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "common" / "src" / "main" / "resources" / "assets" / "createharmonics_unofficial" / "ponder"

# Base plate uses a distinct checker pattern (not upstream snow / white concrete).
BASE_A = "minecraft:gray_concrete"
BASE_B = "minecraft:light_gray_concrete"
DATA_VERSION = 3955

RECORD_PRESS_BLOCKS = [
    {"pos": [2, 1, 4], "name": "create:belt", "props": {"casing": "false", "waterlogged": "false", "part": "start", "facing": "east", "slope": "horizontal"}},
    {"pos": [3, 1, 4], "name": "create:belt", "props": {"casing": "false", "waterlogged": "false", "part": "end", "facing": "east", "slope": "horizontal"}},
    {"pos": [3, 1, 5], "name": "create:gearbox", "props": {"axis": "y"}},
    {"pos": [4, 1, 4], "name": "createharmonics_unofficial:record_press_base", "props": {"waterlogged": "false", "facing": "north"}},
    {"pos": [4, 1, 5], "name": "create:gearbox", "props": {"axis": "y"}},
    {"pos": [4, 1, 6], "name": "create:gearbox", "props": {"axis": "x"}},
    {"pos": [4, 1, 7], "name": "create:creative_motor", "props": {"facing": "north"}},
    {"pos": [5, 1, 4], "name": "create:belt", "props": {"casing": "false", "waterlogged": "false", "part": "end", "facing": "west", "slope": "horizontal"}},
    {"pos": [5, 1, 5], "name": "create:gearbox", "props": {"axis": "y"}},
    {"pos": [6, 1, 4], "name": "create:belt", "props": {"casing": "false", "waterlogged": "false", "part": "start", "facing": "west", "slope": "horizontal"}},
    {"pos": [7, 1, 4], "name": "create:weighted_ejector", "props": {"waterlogged": "false", "facing": "west"}},
    {"pos": [7, 1, 5], "name": "create:shaft", "props": {"waterlogged": "false", "axis": "z"}},
    {"pos": [7, 1, 6], "name": "create:shaft", "props": {"waterlogged": "false", "axis": "z"}},
    {"pos": [7, 1, 7], "name": "create:creative_motor", "props": {"facing": "north"}},
    {"pos": [4, 2, 6], "name": "create:shaft", "props": {"waterlogged": "false", "axis": "y"}},
    {"pos": [4, 3, 4], "name": "create:mechanical_press", "props": {"facing": "north"}},
    {"pos": [4, 3, 5], "name": "create:shaft", "props": {"waterlogged": "false", "axis": "z"}},
    {"pos": [4, 3, 6], "name": "create:gearbox", "props": {"axis": "x"}},
]

ANDESITE_JUKEBOX_BLOCKS = [
    {"pos": [16, 1, 15], "name": "create:andesite_casing", "props": {}},
    {"pos": [18, 1, 15], "name": "create:andesite_casing", "props": {}},
    {"pos": [18, 1, 16], "name": "create:andesite_casing", "props": {}},
    {"pos": [18, 1, 17], "name": "create:andesite_casing", "props": {}},
    {"pos": [15, 3, 15], "name": "create:andesite_casing", "props": {}},
    {"pos": [15, 3, 16], "name": "create:andesite_casing", "props": {}},
    {"pos": [16, 2, 15], "name": "minecraft:lever", "props": {"face": "floor", "powered": "false", "facing": "east"}},
    {"pos": [0, 1, 0], "name": "minecraft:oak_sign", "props": {"waterlogged": "false", "rotation": "8"}},
    {"pos": [16, 1, 16], "name": "create:gearbox", "props": {"axis": "x"}},
    {"pos": [16, 1, 17], "name": "create:shaft", "props": {"waterlogged": "false", "axis": "z"}},
    {"pos": [16, 1, 18], "name": "create:andesite_encased_cogwheel", "props": {"top_shaft": "true", "bottom_shaft": "true", "axis": "z"}},
    {"pos": [16, 1, 19], "name": "create:shaft", "props": {"waterlogged": "false", "axis": "z"}},
    {"pos": [16, 1, 20], "name": "create:shaft", "props": {"waterlogged": "false", "axis": "z"}},
    {"pos": [16, 1, 21], "name": "create:shaft", "props": {"waterlogged": "false", "axis": "z"}},
    {"pos": [16, 1, 22], "name": "create:shaft", "props": {"waterlogged": "false", "axis": "z"}},
    {"pos": [16, 1, 23], "name": "create:shaft", "props": {"waterlogged": "false", "axis": "z"}},
    {"pos": [16, 1, 24], "name": "create:creative_motor", "props": {"facing": "north"}},
    {"pos": [19, 1, 16], "name": "create:creative_motor", "props": {"facing": "up"}},
    {"pos": [16, 2, 16], "name": "createharmonics_unofficial:andesite_jukebox", "props": {"has_record": "false", "facing": "up"}},
    {"pos": [16, 2, 17], "name": "create:speedometer", "props": {"facing": "up", "axis_along_first": "false"}},
    {"pos": [16, 2, 18], "name": "create:andesite_encased_cogwheel", "props": {"top_shaft": "false", "bottom_shaft": "true", "axis": "z"}},
    {"pos": [18, 2, 15], "name": "create:mechanical_arm", "props": {"ceiling": "false"}},
    {"pos": [18, 2, 16], "name": "create:depot", "props": {"waterlogged": "false"}},
    {"pos": [18, 2, 17], "name": "create:mechanical_arm", "props": {"ceiling": "false"}},
    {"pos": [19, 2, 15], "name": "create:cogwheel", "props": {"waterlogged": "false", "axis": "y"}},
    {"pos": [19, 2, 16], "name": "create:cogwheel", "props": {"waterlogged": "false", "axis": "y"}},
    {"pos": [19, 2, 17], "name": "create:cogwheel", "props": {"waterlogged": "false", "axis": "y"}},
    {"pos": [15, 3, 14], "name": "createharmonics_unofficial:andesite_jukebox", "props": {"has_record": "true", "facing": "west"}},
    {"pos": [16, 3, 16], "name": "create:mechanical_bearing", "props": {"facing": "west"}},
]


def block_state(name: str, props: dict[str, str] | None = None) -> Compound:
    state = Compound({"Name": String(name)})
    if props:
        state["Properties"] = Compound({String(k): String(v) for k, v in props.items()})
    return state


def build_structure(size: tuple[int, int, int], placements: list[dict]) -> Compound:
    occupied: dict[tuple[int, int, int], Compound] = {}

    sx, sy, sz = size
    for x in range(sx):
        for z in range(sz):
            name = BASE_A if (x + z) % 2 == 0 else BASE_B
            occupied[(x, 0, z)] = block_state(name)

    for entry in placements:
        x, y, z = entry["pos"]
        occupied[(x, y, z)] = block_state(entry["name"], entry.get("props"))

    palette: list[Compound] = []
    palette_index: dict[tuple[str, tuple[tuple[str, str], ...]], int] = {}

    def palette_key(state: Compound) -> tuple[str, tuple[tuple[str, str], ...]]:
        props = state.get("Properties")
        if props is None:
            return (str(state["Name"]), ())
        return (str(state["Name"]), tuple(sorted((str(k), str(v)) for k, v in props.items())))

    block_entries: list[Compound] = []
    for (x, y, z), state in sorted(occupied.items(), key=lambda item: (item[0][1], item[0][2], item[0][0])):
        key = palette_key(state)
        if key not in palette_index:
            palette_index[key] = len(palette)
            palette.append(state)
        block_entries.append(
            Compound(
                {
                    "pos": List[Int]([Int(x), Int(y), Int(z)]),
                    "state": Int(palette_index[key]),
                }
            )
        )

    return Compound(
        {
            "size": List[Int]([Int(sx), Int(sy), Int(sz)]),
            "entities": List[Compound]([]),
            "blocks": List[Compound](block_entries),
            "palette": List[Compound](palette),
            "DataVersion": Int(DATA_VERSION),
        }
    )


def write_structure(path: Path, root: Compound) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    File(root).save(str(path), gzipped=True)


def main() -> None:
    write_structure(OUT_DIR / "record_press_base.nbt", build_structure((9, 6, 9), RECORD_PRESS_BLOCKS))
    write_structure(OUT_DIR / "andesite_jukebox.nbt", build_structure((33, 7, 33), ANDESITE_JUKEBOX_BLOCKS))
    print(f"Wrote ponder structures to {OUT_DIR.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
