#!/usr/bin/env python3
"""One-off migration: createharmonics -> createharmonics_unofficial."""

from __future__ import annotations

import json
import os
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OLD = "createharmonics"
NEW = "createharmonics_unofficial"

SKIP_DIR_PARTS = {".git", "build", "buildSrc", ".gradle", "__pycache__", ".vscode", "run"}
TEXT_SUFFIXES = {
    ".kt",
    ".java",
    ".json",
    ".kts",
    ".toml",
    ".properties",
    ".MD",
    ".md",
    ".py",
    ".yml",
    ".yaml",
    ".java",
}


def should_skip(path: Path) -> bool:
    return any(part in SKIP_DIR_PARTS for part in path.parts)


def replace_mod_id(text: str) -> str:
    # Order matters: longer / namespaced forms first.
    replacements = [
        (f"assets/{OLD}/", f"assets/{NEW}/"),
        (f"data/{OLD}/", f"data/{NEW}/"),
        (f"block.{OLD}.", f"block.{NEW}."),
        (f"item.{OLD}.", f"item.{NEW}."),
        (f"sounds.{OLD}.", f"sounds.{NEW}."),
        (f"{OLD}:", f"{NEW}:"),
        (f'"{OLD}.', f'"{NEW}.'),
        (f"{OLD}.common.mixins.json", f"{NEW}.common.mixins.json"),
        (f"{OLD}.mixins.json", f"{NEW}.mixins.json"),
        (f"{OLD}.refmap.json", f"{NEW}.refmap.json"),
    ]
    for old, new in replacements:
        text = text.replace(old, new)
    return text


def rename_paths() -> None:
    renames = [
        ROOT / "common/src/main/resources/assets/createharmonics",
        ROOT / "common/src/main/resources/data/createharmonics",
        ROOT / "neoforge/src/main/resources/assets/createharmonics",
        ROOT / "neoforge/src/main/resources/data/createharmonics",
        ROOT / "common/src/main/resources/createharmonics.common.mixins.json",
        ROOT / "neoforge/src/main/resources/createharmonics.mixins.json",
    ]
    for src in renames:
        if not src.exists():
            continue
        dest = src.parent / src.name.replace(OLD, NEW)
        if dest.exists():
            raise SystemExit(f"Destination already exists: {dest}")
        print(f"rename {src.relative_to(ROOT)} -> {dest.relative_to(ROOT)}")
        src.rename(dest)


def remove_brass_jukebox_texture() -> None:
    tex = (
        ROOT
        / "common/src/main/resources/assets"
        / NEW
        / "textures/block/brass_jukebox/brass_jukebox.png"
    )
    if tex.exists():
        tex.unlink()
        print(f"deleted {tex.relative_to(ROOT)}")
    folder = tex.parent
    if folder.exists() and not any(folder.iterdir()):
        folder.rmdir()
        print(f"removed empty {folder.relative_to(ROOT)}")


def strip_brass_jukebox_lang() -> None:
    lang_dir = ROOT / "common/src/main/resources/assets" / NEW / "lang"
    for path in lang_dir.rglob("*.json"):
        data = json.loads(path.read_text(encoding="utf-8"))
        keys = [k for k in data if "brass_jukebox" in k]
        if not keys:
            continue
        for k in keys:
            del data[k]
        path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(f"removed brass_jukebox keys from {path.relative_to(ROOT)}")


def patch_text_files() -> None:
    for path in ROOT.rglob("*"):
        if not path.is_file() or should_skip(path):
            continue
        if path.suffix not in TEXT_SUFFIXES and path.name not in {"gradlew", "gradlew.bat"}:
            continue
        if path.name == "migrate_mod_id.py":
            continue
        original = path.read_text(encoding="utf-8")
        updated = replace_mod_id(original)
        if updated != original:
            path.write_text(updated, encoding="utf-8")
            print(f"patched {path.relative_to(ROOT)}")


def patch_gradle_mod_id() -> None:
    props = ROOT / "gradle.properties"
    text = props.read_text(encoding="utf-8")
    text = re.sub(r"^mod_id=.*$", f"mod_id={NEW}", text, flags=re.MULTILINE)
    props.write_text(text, encoding="utf-8")
    print(f"set mod_id={NEW} in gradle.properties")


def patch_mod_id_constant() -> None:
    path = ROOT / "common/src/main/kotlin/io/github/qwer854645/createresonance/CreateResonanceMod.kt"
    text = path.read_text(encoding="utf-8")
    text = re.sub(
        r'const val MOD_ID = "createharmonics(_unofficial)?"',
        f'const val MOD_ID = "{NEW}"',
        text,
    )
    path.write_text(text, encoding="utf-8")
    print("updated CreateResonanceMod.MOD_ID")


def main() -> None:
    rename_paths()
    remove_brass_jukebox_texture()
    patch_gradle_mod_id()
    patch_mod_id_constant()
    patch_text_files()
    strip_brass_jukebox_lang()
    print("Migration complete.")


if __name__ == "__main__":
    main()
