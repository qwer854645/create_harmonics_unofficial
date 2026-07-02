#!/usr/bin/env python3
"""Migrate createharmonics_unofficial -> create_webdisc (text, paths, registry IDs)."""

from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OLD_MOD = "createharmonics_unofficial"
NEW_MOD = "create_webdisc"

SKIP_DIR_PARTS = {
    ".git",
    "build",
    "buildSrc",
    ".gradle",
    "__pycache__",
    ".vscode",
    "run",
    "venv",
    "agent-tools",
}

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
    ".txt",
}

SKIP_FILES = {
    "migrate_mod_id.py",
    "migrate_to_webdisc.py",
}

# Path segment renames (relative paths under assets/data/generated)
PATH_SEGMENT_RENAMES = [
    (f"assets/{OLD_MOD}", f"assets/{NEW_MOD}"),
    (f"data/{OLD_MOD}", f"data/{NEW_MOD}"),
    (f"{OLD_MOD}.common.mixins.json", f"{NEW_MOD}.common.mixins.json"),
    (f"{OLD_MOD}.mixins.json", f"{NEW_MOD}.mixins.json"),
    ("models/block/andesite_jukebox", "models/block/andesite_web_player"),
    ("models/block/record_press_base", "models/block/webdisc_imprinter"),
    ("models/block/ethereal_record_visual", "models/block/webdisc_visual"),
    ("models/item/ethereal_record_base.json", "models/item/webdisc_blank.json"),
    ("models/item/record_press_base.json", "models/item/webdisc_imprinter.json"),
    ("textures/block/andesite_jukebox", "textures/block/andesite_web_player"),
    ("textures/block/record_press_base", "textures/block/webdisc_imprinter"),
    ("textures/block/ethereal_record_visual", "textures/block/webdisc_visual"),
    ("textures/item/ethereal_record_base", "textures/item/webdisc_blank"),
    ("textures/item/ethereal_record", "textures/item/webdisc"),
    ("ponder/andesite_jukebox.nbt", "ponder/andesite_web_player.nbt"),
    ("ponder/record_press_base.nbt", "ponder/webdisc_imprinter.nbt"),
    ("blockstates/ethereal_record_block.json", "blockstates/webdisc_block.json"),
    ("recipe/base_record.json", "recipe/webdisc_blank.json"),
    ("recipe/andesite_jukebox.json", "recipe/andesite_web_player.json"),
    ("recipe/mechanical_crafting/record_press_base.json", "recipe/mechanical_crafting/webdisc_imprinter.json"),
    ("recipe/pressing/ethereal_record", "recipe/pressing/webdisc"),
    ("recipe/deploying/ethereal_record", "recipe/deploying/webdisc"),
    ("loot_table/blocks/record_press_base.json", "loot_table/blocks/webdisc_imprinter.json"),
    ("loot_table/blocks/andesite_jukebox.json", "loot_table/blocks/andesite_web_player.json"),
    ("structure/andesite_jukebox", "structure/andesite_web_player"),
    ("structure/record_press_base", "structure/record_press_base"),  # noop anchor
]

RECORD_TYPES = ("stone", "brass", "gold", "diamond", "emerald", "netherite", "creative")


def should_skip(path: Path) -> bool:
    return any(part in SKIP_DIR_PARTS for part in path.parts)


def replace_registry_ids(text: str) -> str:
    replacements = [
        (OLD_MOD, NEW_MOD),
        (f"block.{OLD_MOD}.", f"block.{NEW_MOD}."),
        (f"item.{OLD_MOD}.", f"item.{NEW_MOD}."),
        (f"sounds.{OLD_MOD}.", f"sounds.{NEW_MOD}."),
        *[
            (f"broken_{mat}_ethereal_record", f"broken_{mat}_webdisc")
            for mat in RECORD_TYPES
            if mat != "creative"
        ],
        *[(f"{mat}_ethereal_record", f"{mat}_webdisc") for mat in RECORD_TYPES],
        ("ethereal_record_base", "webdisc_blank"),
        ("ethereal_record_visual", "webdisc_visual"),
        ("ethereal_record_block", "webdisc_block"),
        ("record_press_base", "webdisc_imprinter"),
        ("andesite_jukebox", "andesite_web_player"),
        ("recipe/pressing/ethereal_record", "recipe/pressing/webdisc"),
        ("recipe/deploying/ethereal_record", "recipe/deploying/webdisc"),
        ("ethereal_record/", "webdisc/"),
        ("base_record", "webdisc_blank"),
        ("HarmonicsMenuScreen", "WebdiscMenuScreen"),
        ("assertFromHarmonics", "assertFromWebdisc"),
        ("Non-Harmonics", "Non-Webdisc"),
        ("Harmonics'", "Webdisc's"),
        ("CreateHarmonics blocks", "Webdisc blocks"),
        ("Create: Harmonics (Unofficial)", "Create: Webdisc"),
        ("Create Harmonics (Unofficial)", "Create: Webdisc"),
        ("Create: Harmonics", "Create: Webdisc"),
        ("create_harmonics_unofficial", "create_webdisc"),
        ("[CreateHarmonics]", "[Webdisc]"),
        ("Ethereal Record", "Webdisc"),
        ("Ethereal Records", "Webdiscs"),
        ("Ethereal source", "Web URL"),
        ("Ethereal Webdisc", "Webdisc"),
        ("（非官方版）", ""),
        ("机械动力：和声学", "机械动力：网联唱片"),
        ("和声学", "网联唱片"),
        ("空灵唱片", "网联唱片"),
        ("空灵音源", "网络音源"),
        ("安山唱片机", "安山网播机"),
        ("唱片压印台", "网碟压印台"),
        ("唱片压印", "网碟压印"),
        ("空灵", "网联"),
    ]
    for old, new in replacements:
        text = text.replace(old, new)
    return text


def patch_text_file(path: Path) -> bool:
    original = path.read_text(encoding="utf-8")
    updated = replace_registry_ids(original)
    if updated != original:
        path.write_text(updated, encoding="utf-8")
        return True
    return False


def rename_path_if_needed(path: Path) -> None:
    rel = path.relative_to(ROOT).as_posix()
    new_rel = rel
    for old, new in PATH_SEGMENT_RENAMES:
        if old == new:
            continue
        if old in new_rel:
            new_rel = new_rel.replace(old, new)
    # item model files: *_ethereal_record.json -> *_webdisc.json
    new_rel = re.sub(r"([a-z]+)_ethereal_record\.json$", r"\1_webdisc.json", new_rel)
    if new_rel != rel:
        dest = ROOT / new_rel
        if dest.exists() and dest != path:
            return
        dest.parent.mkdir(parents=True, exist_ok=True)
        print(f"rename {rel} -> {new_rel}")
        shutil.move(str(path), str(dest))


def collect_paths_for_rename() -> list[Path]:
    paths: list[Path] = []
    for path in ROOT.rglob("*"):
        if not path.is_file() or should_skip(path):
            continue
        rel = path.relative_to(ROOT).as_posix()
        if OLD_MOD in rel or "andesite_jukebox" in rel or "record_press_base" in rel:
            paths.append(path)
        elif "ethereal_record" in rel:
            paths.append(path)
        elif re.search(r"[a-z]+_ethereal_record\.json$", rel):
            paths.append(path)
        elif path.name in {f"{OLD_MOD}.mixins.json", f"{OLD_MOD}.common.mixins.json"}:
            paths.append(path)
    # deepest paths first
    paths.sort(key=lambda p: len(p.parts), reverse=True)
    return paths


def rename_kotlin_gui_file() -> None:
    src = ROOT / "common/src/main/kotlin/me/mochibit/createharmonics/gui/HarmonicsMenuScreen.kt"
    dest = ROOT / "common/src/main/kotlin/me/mochibit/createharmonics/gui/WebdiscMenuScreen.kt"
    if src.exists() and not dest.exists():
        src.rename(dest)
        print(f"rename {src.relative_to(ROOT)} -> {dest.relative_to(ROOT)}")
    elif src.exists() and dest.exists():
        src.unlink()


def patch_lang_json(path: Path) -> None:
    if path.suffix != ".json" or "/lang/" not in path.as_posix().replace("\\", "/"):
        return
    data = json.loads(path.read_text(encoding="utf-8"))
    new_data: dict[str, str] = {}
    for key, value in data.items():
        new_key = replace_registry_ids(key)
        new_value = replace_registry_ids(value)
        new_data[new_key] = new_value
    path.write_text(json.dumps(new_data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def patch_gradle_properties() -> None:
    props = ROOT / "gradle.properties"
    text = props.read_text(encoding="utf-8")
    text = re.sub(r"^mod_id=.*$", f"mod_id={NEW_MOD}", text, flags=re.MULTILINE)
    text = re.sub(r"^mod_name=.*$", "mod_name=Create: Webdisc", text, flags=re.MULTILINE)
    text = re.sub(r"^version_major=.*$", "version_major=2", text, flags=re.MULTILINE)
    text = re.sub(r"^version_minor=.*$", "version_minor=0", text, flags=re.MULTILINE)
    text = re.sub(r"^version_patch=.*$", "version_patch=0", text, flags=re.MULTILINE)
    text = re.sub(
        r"^mod_group_id=.*$",
        "mod_group_id=io.github.qwer854645",
        text,
        flags=re.MULTILINE,
    )
    desc = (
        "Create addon: craft Webdiscs, bind URL audio, play on Andesite Web Players. "
        "Independent MIT fork with original art — not affiliated with Create: Harmonics upstream."
    )
    text = re.sub(r"^mod_description=.*$", f"mod_description={desc}", text, flags=re.MULTILINE)
    props.write_text(text, encoding="utf-8")
    print("updated gradle.properties")


def patch_mod_id_constant() -> None:
    path = ROOT / "common/src/main/kotlin/me/mochibit/createharmonics/CreateHarmonicsMod.kt"
    text = path.read_text(encoding="utf-8")
    text = re.sub(
        r'const val MOD_ID = "[^"]+"',
        f'const val MOD_ID = "{NEW_MOD}"',
        text,
    )
    path.write_text(text, encoding="utf-8")


def patch_settings() -> None:
    path = ROOT / "settings.gradle.kts"
    text = path.read_text(encoding="utf-8")
    text = text.replace('rootProject.name = "createharmonics"', 'rootProject.name = "create_webdisc"')
    path.write_text(text, encoding="utf-8")


def patch_binary_nbt(path: Path) -> None:
    data = path.read_bytes()
    updated = data
    for old, new in [
        (f"{OLD_MOD}:andesite_jukebox".encode(), f"{NEW_MOD}:andesite_web_player".encode()),
        (f"{OLD_MOD}:record_press_base".encode(), f"{NEW_MOD}:webdisc_imprinter".encode()),
        (OLD_MOD.encode(), NEW_MOD.encode()),
        (b"andesite_jukebox", b"andesite_web_player"),
        (b"record_press_base", b"webdisc_imprinter"),
    ]:
        if len(new) <= len(old):
            # only replace when same length or pad - skip unsafe
            updated = updated.replace(old, new)
    if updated != data:
        path.write_bytes(updated)
        print(f"patched binary {path.relative_to(ROOT)}")


def patch_mod_items_lang_calls() -> None:
    path = ROOT / "common/src/main/kotlin/me/mochibit/createharmonics/foundation/registry/ModItems.kt"
    text = path.read_text(encoding="utf-8")
    text = text.replace('lang("Broken $it Webdisc")', 'lang("Broken $it Webdisc")')
    text = text.replace('lang("Broken $it Ethereal Record")', 'lang("Broken $it Webdisc")')
    text = text.replace('lang("$it Ethereal Record")', 'lang("$it Webdisc")')
    text = text.replace("etherealRecord", "webdisc")
    text = text.replace("ETHEREAL_RECORDS", "WEBDISCS")
    text = text.replace("BROKEN_ETHEREAL_RECORDS", "BROKEN_WEBDISCS")
    text = text.replace("registerBrokenEtherealRecordVariant", "registerBrokenWebdiscVariant")
    text = text.replace("registerEtherealRecordVariant", "registerWebdiscVariant")
    text = text.replace("getEtherealRecordItem", "getWebdiscItem")
    text = text.replace("getBrokenEtherealRecordItem", "getBrokenWebdiscItem")
    path.write_text(text, encoding="utf-8")


def main() -> None:
    # 1) text patches before renames (content in place)
    for path in ROOT.rglob("*"):
        if not path.is_file() or should_skip(path):
            continue
        if path.name in SKIP_FILES:
            continue
        if path.suffix not in TEXT_SUFFIXES:
            continue
        if patch_text_file(path):
            print(f"patched {path.relative_to(ROOT)}")

    patch_gradle_properties()
    patch_mod_id_constant()
    patch_settings()
    patch_mod_items_lang_calls()

    # 2) path renames (deepest first)
    for path in collect_paths_for_rename():
        rename_path_if_needed(path)

    # 3) lang json key rebuild (ensure keys match)
    for path in ROOT.rglob("**/lang/**/*.json"):
        if should_skip(path):
            continue
        if NEW_MOD in path.as_posix() or OLD_MOD in path.as_posix():
            patch_lang_json(path)

    # 4) kotlin file rename
    rename_kotlin_gui_file()

    # 5) nbt binary patches
    for path in ROOT.rglob("**/*.nbt"):
        if should_skip(path):
            continue
        patch_binary_nbt(path)

    print("Migration to create_webdisc complete.")


if __name__ == "__main__":
    main()
