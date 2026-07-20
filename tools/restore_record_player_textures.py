"""Restore record-player textures from Create references + user disc art."""
from __future__ import annotations

import shutil
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageEnhance

ROOT = Path(__file__).resolve().parents[1]
TEX = ROOT / "common/src/main/resources/assets/create_resonance/textures"
MODELS = ROOT / "common/src/main/resources/assets/create_resonance/models"
BS = ROOT / "common/src/main/resources/assets/create_resonance/blockstates"
REF = ROOT / "art/create_ref"

CREATE_JAR = Path(
    r"C:\Users\Administrator\.gradle\caches\modules-2\files-2.1\com.simibubi.create"
    r"\create-1.21.1\6.0.10-280\92471e8fc5ed4e3c2279001d77ebcec6fd38bcab"
    r"\create-1.21.1-6.0.10-280-slim.jar"
)

EXTRACT = {
    "andesite_casing.png": "assets/create/textures/block/andesite_casing.png",
    "brass_casing.png": "assets/create/textures/block/brass_casing.png",
    "gearbox.png": "assets/create/textures/block/gearbox.png",
    "gearbox_top.png": "assets/create/textures/block/gearbox_top.png",
    "brass_gearbox.png": "assets/create/textures/block/brass_gearbox.png",
    "axis_top.png": "assets/create/textures/block/axis_top.png",
}


def extract_refs() -> None:
    REF.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(CREATE_JAR) as z:
        for name, entry in EXTRACT.items():
            (REF / name).write_bytes(z.read(entry))
            print("ref", name)


def tint(img: Image.Image, rgb: tuple[int, int, int], strength: float = 0.25) -> Image.Image:
    base = img.convert("RGBA")
    overlay = Image.new("RGBA", base.size, (*rgb, int(255 * strength)))
    return Image.alpha_composite(base, overlay)


def add_window(casing: Image.Image, accent: tuple[int, int, int]) -> Image.Image:
    """Side casing with a small viewing window for the spinning disc."""
    img = casing.copy().convert("RGBA")
    d = ImageDraw.Draw(img)
    # Small recessed window (keep most of Create casing visible)
    d.rectangle((5, 5, 10, 10), fill=(18, 20, 26, 255), outline=accent)
    d.ellipse((6, 6, 9, 9), fill=(90, 190, 230, 255))
    return img


def make_top(gearbox_top: Image.Image, accent: tuple[int, int, int]) -> Image.Image:
    img = gearbox_top.copy().convert("RGBA")
    d = ImageDraw.Draw(img)
    # Accent ring suggesting audio resonator
    d.ellipse((3, 3, 12, 12), outline=accent)
    return img


def write_resonator(kind: str, casing: Image.Image, top: Image.Image, bottom: Image.Image) -> None:
    dest = TEX / f"block/{kind}_resonator"
    dest.mkdir(parents=True, exist_ok=True)
    add_window(casing, (180, 150, 80) if kind == "brass" else (120, 200, 230)).save(dest / "jukebox_casing.png")
    make_top(top, (196, 152, 72) if kind == "brass" else (79, 195, 247)).save(dest / "gearbox_top.png")
    bottom.convert("RGBA").save(dest / "gearbox_bottom.png")
    # particle = average of casing center
    particle = casing.crop((6, 6, 10, 10)).resize((16, 16), Image.Resampling.NEAREST)
    particle.save(dest / "particle.png")
    print("wrote", dest)


def restore_disc() -> None:
    src = Path(r"F:\others\1111.png")
    item = TEX / "item/resonance_disc_blank/base.png"
    visual = TEX / "block/resonance_disc_visual/resonance_disc.png"
    item.parent.mkdir(parents=True, exist_ok=True)
    visual.parent.mkdir(parents=True, exist_ok=True)
    if src.exists():
        im = Image.open(src).convert("RGBA")
        # ensure 16x16
        if im.size != (16, 16):
            im = im.resize((16, 16), Image.Resampling.NEAREST)
        im.save(item)
        im.save(visual)
        print("restored disc from 1111.png")
    else:
        print("1111.png missing, keeping current disc")


def write_blockstates() -> None:
    BS.mkdir(parents=True, exist_ok=True)
    for name in ("andesite_resonator", "brass_resonator"):
        model = f"create_resonance:block/{name}/block"
        # Single model for all variants (facing/powered/disc handled in code/renderer)
        variants = {}
        for facing in ("north", "south", "east", "west", "up", "down"):
            for has_disc in ("true", "false"):
                for powered in ("true", "false"):
                    key = f"facing={facing},has_resonance_disc={has_disc},powered={powered}"
                    variants[key] = {"model": model}
        import json

        path = BS / f"{name}.json"
        path.write_text(json.dumps({"variants": variants}, indent=2) + "\n", encoding="utf-8")
        print("wrote", path.relative_to(ROOT))

    # resonance_press — horizontal facing
    press_model = "create_resonance:block/resonance_press/block"
    press_variants = {}
    for facing, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        press_variants[f"facing={facing}"] = {"model": press_model, "y": y}
    (BS / "resonance_press.json").write_text(
        __import__("json").dumps({"variants": press_variants}, indent=2) + "\n",
        encoding="utf-8",
    )
    print("wrote blockstates/resonance_press.json")


def fix_press() -> None:
    casing = Image.open(REF / "andesite_casing.png").convert("RGBA")
    brass = Image.open(REF / "brass_casing.png").convert("RGBA")
    dest = TEX / "block/resonance_press"
    dest.mkdir(parents=True, exist_ok=True)
    # top: andesite with brass inset plate
    top = casing.copy()
    d = ImageDraw.Draw(top)
    d.rectangle((2, 2, 13, 13), outline=(196, 152, 72, 255))
    d.rectangle((4, 4, 11, 11), fill=(140, 100, 40, 255))
    d.rectangle((5, 5, 10, 10), fill=(196, 152, 72, 255))
    top.save(dest / "base_top.png")
    # side
    side = casing.copy()
    d = ImageDraw.Draw(side)
    d.rectangle((0, 0, 15, 4), fill=(74, 74, 74, 255))
    d.line((0, 5, 15, 5), fill=(196, 152, 72, 255))
    side.save(dest / "base_side.png")
    print("wrote resonance_press textures")


def main() -> None:
    extract_refs()
    andesite = Image.open(REF / "andesite_casing.png")
    brass = Image.open(REF / "brass_casing.png")
    gtop = Image.open(REF / "gearbox_top.png")
    gside = Image.open(REF / "gearbox.png")
    # Prefer Create brass_gearbox if present for brass top
    brass_top = Image.open(REF / "brass_gearbox.png") if (REF / "brass_gearbox.png").exists() else gtop

    write_resonator("andesite", andesite, gtop, gside)
    write_resonator("brass", brass, brass_top, gside)
    fix_press()
    restore_disc()
    write_blockstates()
    print("done")


if __name__ == "__main__":
    main()
