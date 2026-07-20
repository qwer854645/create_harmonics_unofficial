"""Write missing item/block models and blockstates so textures actually show."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "common/src/main/resources/assets/create_resonance"


def write(path: Path, obj) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")
    print("wrote", path.relative_to(ROOT))


def item_parent(parent: str) -> dict:
    return {"parent": parent}


def main() -> None:
    items = ASSETS / "models/item"
    blocks = ASSETS / "models/block"
    bs = ASSETS / "blockstates"

    # --- Item models (Minecraft looks here; Create customItemModel would generate these via runData) ---
    write(items / "andesite_resonator.json", item_parent("create_resonance:block/andesite_resonator/item"))
    write(items / "brass_resonator.json", item_parent("create_resonance:block/brass_resonator/item"))
    write(items / "resonance_press.json", item_parent("create_resonance:block/resonance_press/item"))
    write(
        items / "resonance_disc.json",
        {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": "create_resonance:item/resonance_disc_blank/base"},
        },
    )

    # Music boxes + conductor: block models + item + blockstates
    for name, tex_prefix in (
        ("andesite_music_box", "andesite_music_box"),
        ("kinetic_music_box", "kinetic_music_box"),
        ("music_conductor", "music_conductor"),
    ):
        # Prefer multi-face if present, else cube_all single
        side = ASSETS / f"textures/block/{tex_prefix}_side.png"
        top = ASSETS / f"textures/block/{tex_prefix}_top.png"
        bottom = ASSETS / f"textures/block/{tex_prefix}_bottom.png"
        single = ASSETS / f"textures/block/{tex_prefix}.png"
        if side.exists() and top.exists() and bottom.exists():
            model = {
                "parent": "minecraft:block/cube_bottom_top",
                "textures": {
                    "side": f"create_resonance:block/{tex_prefix}_side",
                    "top": f"create_resonance:block/{tex_prefix}_top",
                    "bottom": f"create_resonance:block/{tex_prefix}_bottom",
                    "particle": f"create_resonance:block/{tex_prefix}_side",
                },
            }
        else:
            model = {
                "parent": "minecraft:block/cube_all",
                "textures": {"all": f"create_resonance:block/{tex_prefix}"},
            }
        write(blocks / f"{name}.json", model)
        write(items / f"{name}.json", item_parent(f"create_resonance:block/{name}"))

        variants = {}
        for facing in ("north", "south", "east", "west", "up", "down"):
            # DirectionalKineticBlock: rotate model by facing
            entry: dict = {"model": f"create_resonance:block/{name}"}
            # Approximate facing rotations for cube
            if facing == "down":
                entry["x"] = 180
            elif facing == "east":
                entry["x"] = 90
                entry["y"] = 90
            elif facing == "west":
                entry["x"] = 90
                entry["y"] = 270
            elif facing == "south":
                entry["x"] = 90
                entry["y"] = 180
            elif facing == "north":
                entry["x"] = 90
            # up: default
            variants[f"facing={facing}"] = entry
        write(bs / f"{name}.json", {"variants": variants})

    print("done")


if __name__ == "__main__":
    main()
