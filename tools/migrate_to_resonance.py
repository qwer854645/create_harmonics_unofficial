#!/usr/bin/env python3
"""Migrate create_resonance / io.github.qwer854645.createresonance -> create_resonance."""

from __future__ import annotations

import os
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

REPLACEMENTS: list[tuple[str, str]] = [
    # Packages / namespaces (order matters: longer first)
    ("io.github.qwer854645.createresonance", "io.github.qwer854645.createresonance"),
    ("io/github/qwer854645/createresonance", "io/github/qwer854645/createresonance"),
    ("create_resonance", "create_resonance"),
    ("Create: Resonance", "Create: Resonance"),
    ("Create Resonance", "Create Resonance"),
    ("createresonance", "createresonance"),
    ("CreateResonanceClientMod", "CreateResonanceClientMod"),
    ("CreateResonanceMod", "CreateResonanceMod"),
    ("ResonanceDiscItem", "ResonanceDiscItem"),
    ("ResonanceDisc", "ResonanceDisc"),
    ("resonance_disc", "resonance_disc"),
    ("RESONANCE_DISC", "RESONANCE_DISC"),
    # Block IDs / names in code strings
    ("andesite_resonator", "andesite_resonator"),
    ("brass_resonator", "brass_resonator"),
    ("resonance_press", "resonance_press"),
    ("resonance_disc_visual", "resonance_disc_visual"),
    ("resonance_disc_blank", "resonance_disc_blank"),
    ("ResonanceMenuScreen", "ResonanceMenuScreen"),
    ("resonance_disc", "resonance_disc"),  # careful — after more specific
]

SKIP_DIRS = {
    ".git",
    ".gradle",
    "build",
    ".kotlin",
    "run",
    "neoforge/run",
    "neoforge/build",
    "common/build",
    "buildSrc/build",
    "__pycache__",
    "node_modules",
}

TEXT_EXTS = {
    ".kt",
    ".java",
    ".kts",
    ".gradle",
    ".properties",
    ".toml",
    ".json",
    ".md",
    ".MD",
    ".txt",
    ".cfg",
    ".lang",
    ".ps1",
    ".py",
    ".xml",
    ".accesswidener",
}


def should_skip(path: Path) -> bool:
    parts = set(path.parts)
    if parts & SKIP_DIRS:
        return True
    rel = path.relative_to(ROOT).as_posix()
    for d in SKIP_DIRS:
        if rel.startswith(d + "/") or rel == d:
            return True
    return False


def rewrite_file(path: Path) -> bool:
    if path.suffix not in TEXT_EXTS and path.name not in {"gradlew", "gradlew.bat"}:
        return False
    try:
        text = path.read_text(encoding="utf-8")
    except (UnicodeDecodeError, OSError):
        return False
    original = text
    for old, new in REPLACEMENTS:
        text = text.replace(old, new)
    if text != original:
        path.write_text(text, encoding="utf-8")
        return True
    return False


def move_package_tree() -> None:
    mappings = [
        (
            ROOT / "common/src/main/kotlin/io/github/qwer854645/createresonance",
            ROOT / "common/src/main/kotlin/io/github/qwer854645/createresonance",
        ),
        (
            ROOT / "common/src/main/java/io/github/qwer854645/createresonance",
            ROOT / "common/src/main/java/io/github/qwer854645/createresonance",
        ),
        (
            ROOT / "neoforge/src/main/kotlin/io/github/qwer854645/createresonance",
            ROOT / "neoforge/src/main/kotlin/io/github/qwer854645/createresonance",
        ),
        (
            ROOT / "neoforge/src/main/java/io/github/qwer854645/createresonance",
            ROOT / "neoforge/src/main/java/io/github/qwer854645/createresonance",
        ),
        (
            ROOT / "common/src/main/resources/assets/create_resonance",
            ROOT / "common/src/main/resources/assets/create_resonance",
        ),
        (
            ROOT / "common/src/main/resources/data/create_resonance",
            ROOT / "common/src/main/resources/data/create_resonance",
        ),
        (
            ROOT / "neoforge/src/main/resources/assets/create_resonance",
            ROOT / "neoforge/src/main/resources/assets/create_resonance",
        ),
        (
            ROOT / "neoforge/src/main/resources/data/create_resonance",
            ROOT / "neoforge/src/main/resources/data/create_resonance",
        ),
    ]
    for src, dst in mappings:
        if not src.exists():
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        if dst.exists():
            shutil.rmtree(dst)
        shutil.move(str(src), str(dst))
        print(f"Moved {src.relative_to(ROOT)} -> {dst.relative_to(ROOT)}")
        # clean empty parents
        parent = src.parent
        while parent != ROOT and parent.exists() and not any(parent.iterdir()):
            parent.rmdir()
            parent = parent.parent


def rename_key_files() -> None:
    renames = [
        (
            "common/src/main/kotlin/io/github/qwer854645/createresonance/CreateResonanceMod.kt",
            "common/src/main/kotlin/io/github/qwer854645/createresonance/CreateResonanceMod.kt",
        ),
        (
            "common/src/main/kotlin/io/github/qwer854645/createresonance/CreateResonanceClientMod.kt",
            "common/src/main/kotlin/io/github/qwer854645/createresonance/CreateResonanceClientMod.kt",
        ),
        (
            "common/src/main/kotlin/io/github/qwer854645/createresonance/content/records/ResonanceDiscItem.kt",
            "common/src/main/kotlin/io/github/qwer854645/createresonance/content/records/ResonanceDiscItem.kt",
        ),
        (
            "common/src/main/kotlin/io/github/qwer854645/createresonance/gui/ResonanceMenuScreen.kt",
            "common/src/main/kotlin/io/github/qwer854645/createresonance/gui/ResonanceMenuScreen.kt",
        ),
        (
            "common/src/main/resources/create_resonance.common.mixins.json",
            "common/src/main/resources/create_resonance.common.mixins.json",
        ),
        (
            "neoforge/src/main/resources/create_resonance.mixins.json",
            "neoforge/src/main/resources/create_resonance.mixins.json",
        ),
        (
            "neoforge/src/main/java/io/github/qwer854645/createresonance/mixin/content/record/ResonanceDiscItemMixin.java",
            "neoforge/src/main/java/io/github/qwer854645/createresonance/mixin/content/record/ResonanceDiscItemMixin.java",
        ),
    ]
    for old_s, new_s in renames:
        old, new = ROOT / old_s, ROOT / new_s
        if old.exists():
            new.parent.mkdir(parents=True, exist_ok=True)
            old.rename(new)
            print(f"Renamed {old.name} -> {new.name}")


def rename_asset_dirs() -> None:
    # After assets moved to create_resonance, rename nested folders
    assets = ROOT / "common/src/main/resources/assets/create_resonance"
    if not assets.exists():
        return
    dir_renames = [
        ("models/block/andesite_resonator", "models/block/andesite_resonator"),
        ("models/block/brass_resonator", "models/block/brass_resonator"),
        ("models/block/resonance_press", "models/block/resonance_press"),
        ("models/block/resonance_disc_visual", "models/block/resonance_disc_visual"),
        ("blockstates/andesite_resonator.json", "blockstates/andesite_resonator.json"),
        ("blockstates/brass_resonator.json", "blockstates/brass_resonator.json"),
        ("blockstates/resonance_press.json", "blockstates/resonance_press.json"),
        ("textures/block/andesite_resonator", "textures/block/andesite_resonator"),
        ("textures/block/brass_resonator", "textures/block/brass_resonator"),
        ("textures/block/resonance_press", "textures/block/resonance_press"),
        ("textures/block/resonance_disc_visual", "textures/block/resonance_disc_visual"),
        ("textures/item/resonance_disc", "textures/item/resonance_disc"),
        ("ponder/andesite_resonator.nbt", "ponder/andesite_resonator.nbt"),
        ("ponder/resonance_press.nbt", "ponder/resonance_press.nbt"),
    ]
    for old_s, new_s in dir_renames:
        old, new = assets / old_s, assets / new_s
        if old.exists():
            new.parent.mkdir(parents=True, exist_ok=True)
            if new.exists():
                if new.is_dir():
                    shutil.rmtree(new)
                else:
                    new.unlink()
            old.rename(new)
            print(f"Asset rename {old_s} -> {new_s}")


def main() -> None:
    print("Step 1: move package/asset trees")
    move_package_tree()
    print("Step 2: rename key files")
    rename_key_files()
    print("Step 3: rename asset subdirs")
    rename_asset_dirs()
    print("Step 4: rewrite file contents")
    changed = 0
    for path in ROOT.rglob("*"):
        if not path.is_file() or should_skip(path):
            continue
        if rewrite_file(path):
            changed += 1
    print(f"Rewrote {changed} files")
    print("Done.")


if __name__ == "__main__":
    main()
