#!/usr/bin/env python3
"""Generate original replacement assets for Create: Resonance."""

from __future__ import annotations

import argparse
import math
import os
import subprocess
import struct
import tempfile
import wave
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
TEXTURES = ROOT / "common" / "src" / "main" / "resources" / "assets" / "create_resonance" / "textures"
SOUNDS = ROOT / "common" / "src" / "main" / "resources" / "assets" / "create_resonance" / "sounds"

# Create-inspired palette
ANDESITE = (110, 110, 110)
ANDESITE_DARK = (74, 74, 74)
ANDESITE_LIGHT = (148, 148, 148)
BRASS = (196, 152, 72)
BRASS_DARK = (140, 100, 40)
BRASS_LIGHT = (230, 196, 120)
PANEL_BG = (38, 38, 42)
PANEL_BORDER = (24, 24, 28)
PANEL_INNER = (56, 56, 62)
UI_ACCENT = (180, 140, 70)
WEB_BLUE = (30, 58, 95)
WEB_BLUE_LIGHT = (79, 195, 247)
WEB_BLUE_DARK = (18, 36, 58)
VINYL = (32, 34, 40)
VINYL_GROOVE = (48, 50, 58)

RECORD_COLORS = {
    "resonance_disc": ((120, 220, 255), (40, 160, 210)),
}


def ensure_dir(path: Path) -> None:
    path.mkdir(parents=True, exist_ok=True)


def put_pixel(img: Image.Image, x: int, y: int, color: tuple[int, int, int, int]) -> None:
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)


def fill_rect(img: Image.Image, x0: int, y0: int, x1: int, y1: int, color: tuple[int, int, int]) -> None:
    draw = ImageDraw.Draw(img)
    draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=color)


def draw_checker(img: Image.Image, x0: int, y0: int, w: int, h: int, c1, c2, size: int = 2) -> None:
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            color = c1 if ((x - x0) // size + (y - y0) // size) % 2 == 0 else c2
            put_pixel(img, x, y, (*color, 255))


def draw_vinyl(img: Image.Image, label: tuple[int, int, int], rim: tuple[int, int, int], broken: bool = False) -> None:
    cx, cy = img.width // 2, img.height // 2
    for y in range(img.height):
        for x in range(img.width):
            dx, dy = x - cx + 0.5, y - cy + 0.5
            dist = math.hypot(dx, dy)
            if dist > 7.5:
                color = (20, 20, 20)
            elif dist > 5.5:
                color = rim
            elif dist > 1.8:
                color = label
            else:
                color = (240, 240, 240)
            put_pixel(img, x, y, (*color, 255))

    if broken:
        draw = ImageDraw.Draw(img)
        draw.line((2, 2, 13, 13), fill=(230, 230, 230), width=1)
        draw.line((13, 3, 4, 12), fill=(180, 180, 180), width=1)


def draw_andesite_face(img: Image.Image, x0: int, y0: int, w: int, h: int, top: bool = False) -> None:
    fill_rect(img, x0, y0, x0 + w, y0 + h, ANDESITE)
    draw_checker(img, x0, y0, w, h, ANDESITE_LIGHT, ANDESITE_DARK, 4)
    draw = ImageDraw.Draw(img)
    draw.rectangle((x0, y0, x0 + w - 1, y0 + h - 1), outline=ANDESITE_DARK)
    if top:
        cx, cy = x0 + w // 2, y0 + h // 2
        draw.ellipse((cx - 4, cy - 4, cx + 3, cy + 3), outline=BRASS_DARK, width=1)
        draw.ellipse((cx - 2, cy - 2, cx + 1, cy + 1), fill=BRASS)


def make_andesite_resonator_texture() -> Image.Image:
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw_andesite_face(img, 0, 0, 16, 16, top=False)
    draw_andesite_face(img, 16, 0, 16, 16, top=True)
    draw_andesite_face(img, 32, 0, 16, 16, top=False)
    draw_andesite_face(img, 48, 0, 16, 16, top=False)

    for row in range(4):
        y = 16 + row * 12
        draw_andesite_face(img, 0, y, 16, 12, top=False)
        draw_andesite_face(img, 16, y, 16, 12, top=row == 0)
        draw_andesite_face(img, 32, y, 16, 12, top=False)
        draw_andesite_face(img, 48, y, 16, 12, top=False)

    draw = ImageDraw.Draw(img)
    draw.rectangle((18, 18, 30, 30), outline=BRASS, width=1)
    draw.line((24, 20, 24, 28), fill=BRASS_LIGHT)
    draw.line((20, 24, 28, 24), fill=BRASS_LIGHT)
    return img


def make_resonance_press_texture() -> Image.Image:
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw_andesite_face(img, 0, 0, 32, 32, top=True)
    draw_andesite_face(img, 32, 0, 32, 32, top=False)
    draw_andesite_face(img, 0, 32, 32, 32, top=False)
    draw_andesite_face(img, 32, 32, 32, 32, top=False)
    draw = ImageDraw.Draw(img)
    draw.rectangle((12, 12, 20, 20), fill=BRASS)
    draw.rectangle((13, 13, 19, 19), fill=BRASS_DARK)
    return img


def make_stamp_texture() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    fill_rect(img, 0, 0, 32, 32, BRASS_DARK)
    draw = ImageDraw.Draw(img)
    draw.ellipse((6, 6, 25, 25), fill=BRASS)
    draw.ellipse((10, 10, 21, 21), fill=BRASS_LIGHT)
    draw.line((16, 8, 16, 23), fill=BRASS_DARK, width=2)
    draw.line((8, 16, 23, 16), fill=BRASS_DARK, width=2)
    return img


def make_particle(color: tuple[int, int, int]) -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.ellipse((4, 4, 11, 11), fill=color)
    return img


def make_record_base_texture() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw_vinyl(img, (180, 180, 180), (60, 60, 60), broken=False)
    draw = ImageDraw.Draw(img)
    draw.rectangle((6, 6, 9, 9), fill=(255, 255, 255))
    return img


def make_icons_texture() -> Image.Image:
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))

    def icon_play(x: int, y: int) -> None:
        draw = ImageDraw.Draw(img)
        fill_rect(img, x, y, x + 16, y + 16, PANEL_INNER)
        draw.polygon([(x + 5, y + 4), (x + 5, y + 12), (x + 12, y + 8)], fill=UI_ACCENT)

    def icon_pause(x: int, y: int) -> None:
        draw = ImageDraw.Draw(img)
        fill_rect(img, x, y, x + 16, y + 16, PANEL_INNER)
        draw.rectangle((x + 5, y + 4, x + 7, y + 12), fill=UI_ACCENT)
        draw.rectangle((x + 9, y + 4, x + 11, y + 12), fill=UI_ACCENT)

    icon_play(0, 0)
    icon_pause(16, 0)
    return img


def make_logo_texture() -> Image.Image:
    """Webdisc logo: vinyl disc + URL link + Create brass gear (distinct from upstream Harmonics)."""
    size = 256
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    cx, cy = size // 2, size // 2

    # Outer badge
    draw.ellipse((20, 20, 236, 236), fill=WEB_BLUE_DARK, outline=BRASS_DARK, width=5)
    draw.ellipse((30, 30, 226, 226), outline=WEB_BLUE_LIGHT, width=2)

    # Vinyl disc
    draw.ellipse((52, 52, 204, 204), fill=VINYL, outline=BRASS, width=3)
    for r in (78, 92, 106, 120):
        draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=VINYL_GROOVE, width=1)
    draw.ellipse((108, 108, 148, 148), fill=(18, 18, 22), outline=BRASS_DARK, width=2)
    draw.ellipse((118, 118, 138, 138), fill=BRASS_LIGHT)

    # URL link arcs (top-right)
    draw.arc((138, 58, 198, 118), start=200, end=340, fill=WEB_BLUE_LIGHT, width=6)
    draw.arc((152, 72, 212, 132), start=20, end=160, fill=WEB_BLUE_LIGHT, width=6)

    # Audio wave (left)
    for i, h in enumerate((18, 28, 22, 34, 20)):
        x = 56 + i * 10
        draw.line((x, cy + h // 2, x, cy - h // 2), fill=WEB_BLUE_LIGHT, width=3)

    # Small Create-style gear (bottom-left)
    gear_cx, gear_cy = 78, 186
    gear_r = 18
    draw.ellipse(
        (gear_cx - gear_r, gear_cy - gear_r, gear_cx + gear_r, gear_cy + gear_r),
        fill=BRASS_DARK,
        outline=BRASS,
        width=2,
    )
    draw.ellipse(
        (gear_cx - 7, gear_cy - 7, gear_cx + 7, gear_cy + 7),
        fill=BRASS_LIGHT,
    )
    for angle_deg in range(0, 360, 45):
        rad = math.radians(angle_deg)
        x1 = gear_cx + math.cos(rad) * (gear_r - 2)
        y1 = gear_cy + math.sin(rad) * (gear_r - 2)
        x2 = gear_cx + math.cos(rad) * (gear_r + 5)
        y2 = gear_cy + math.sin(rad) * (gear_r + 5)
        draw.line((x1, y1, x2, y2), fill=BRASS, width=4)

    # Highlight arc on disc
    draw.arc((60, 60, 196, 196), start=240, end=310, fill=(255, 255, 255, 60), width=3)

    return img


def make_record_press_gui() -> Image.Image:
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Main window background
    fill_rect(img, 0, 0, 234, 176, PANEL_BG)
    draw.rectangle((0, 0, 233, 175), outline=PANEL_BORDER, width=2)
    draw.rectangle((8, 24, 226, 168), outline=UI_ACCENT, width=1)
    draw.rectangle((12, 30, 222, 70), fill=PANEL_INNER, outline=ANDESITE_DARK)
    draw.rectangle((12, 78, 222, 130), fill=(32, 32, 36), outline=ANDESITE_DARK)
    draw.rectangle((12, 136, 222, 164), fill=PANEL_INNER, outline=ANDESITE_DARK)

    def small_icon(x: int, y: int, kind: str) -> None:
        fill_rect(img, x, y, x + 16, y + 16, (48, 48, 54))
        d = ImageDraw.Draw(img)
        d.rectangle((x, y, x + 15, y + 15), outline=UI_ACCENT)
        if kind == "link":
            d.arc((x + 3, y + 5, x + 10, y + 12), 0, 180, fill=UI_ACCENT, width=2)
            d.arc((x + 6, y + 4, x + 13, y + 11), 180, 360, fill=UI_ACCENT, width=2)
        elif kind == "random":
            d.point([(x + 4, y + 4), (x + 10, y + 6), (x + 6, y + 11), (x + 12, y + 12)], fill=UI_ACCENT)
        elif kind == "sequential":
            for i in range(4):
                d.rectangle((x + 3 + i * 3, y + 5 + i, x + 5 + i * 3, y + 11), fill=UI_ACCENT)
        elif kind == "note":
            d.ellipse((x + 5, y + 4, x + 9, y + 8), fill=UI_ACCENT)
            d.line((x + 9, y + 6, x + 9, y + 12), fill=UI_ACCENT, width=1)
        elif kind == "arrow":
            d.polygon([(x + 3, y + 8), (x + 10, y + 4), (x + 10, y + 12)], fill=UI_ACCENT)
        elif kind == "percent":
            d.text((x + 2, y + 3), "%", fill=UI_ACCENT)

    small_icon(79, 239, "link")
    small_icon(224, 240, "random")
    small_icon(224, 224, "sequential")
    fill_rect(img, 13, 237, 22, 255, (40, 40, 46))
    draw.rectangle((13, 237, 21, 254), outline=UI_ACCENT)
    small_icon(112, 239, "arrow")
    small_icon(114, 221, "percent")

    # Gauge pointer
    fill_rect(img, 185, 239, 206, 255, (60, 60, 66))
    draw.polygon([(185, 247), (205, 239), (205, 255)], fill=BRASS)
    fill_rect(img, 171, 244, 184, 250, BRASS_DARK)

    return img


def save_png(path: Path, image: Image.Image) -> None:
    ensure_dir(path.parent)
    image.save(path, optimize=True)
    print(f"Wrote {path.relative_to(ROOT)}")


def generate_logo() -> None:
    save_png(TEXTURES / "gui/logo_small.png", make_logo_texture())


def generate_textures() -> None:
    # Hand-drawn assets (do not overwrite):
    # block/andesite_resonator/*, item/resonance_disc_blank/base.png, block/resonance_disc_visual/resonance_disc.png
    save_png(TEXTURES / "gui/icons.png", make_icons_texture())
    save_png(TEXTURES / "gui/logo_small.png", make_logo_texture())
    save_png(TEXTURES / "gui/record_press_base.png", make_record_press_gui())


def write_wav(path: Path, samples, sample_rate: int = 44100) -> None:
    with wave.open(str(path), "w") as wav:
        wav.setnchannels(1)
        wav.setsampwidth(2)
        wav.setframerate(sample_rate)
        frames = b"".join(struct.pack("<h", max(-32767, min(32767, int(s * 32767)))) for s in samples)
        wav.writeframes(frames)


def generate_sound(name: str, samples) -> None:
    import imageio_ffmpeg

    ensure_dir(SOUNDS)
    ffmpeg = imageio_ffmpeg.get_ffmpeg_exe()
    with tempfile.TemporaryDirectory() as tmp:
        wav_path = Path(tmp) / f"{name}.wav"
        ogg_path = SOUNDS / f"{name}.ogg"
        write_wav(wav_path, samples)
        subprocess.run(
            [ffmpeg, "-y", "-i", str(wav_path), "-c:a", "libvorbis", "-q:a", "4", str(ogg_path)],
            check=True,
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
        print(f"Wrote {ogg_path.relative_to(ROOT)}")


def make_sliding_stone_samples() -> list[float]:
    sample_rate = 44100
    duration = 1.2
    total = int(sample_rate * duration)
    samples = []
    for i in range(total):
        t = i / sample_rate
        env = min(1.0, t * 8) * max(0.0, 1 - (t - 0.8) * 3)
        noise = (hash(i) % 2000 - 1000) / 1000
        tone = math.sin(2 * math.pi * 90 * t) * 0.25
        scrape = math.sin(2 * math.pi * (180 + 20 * math.sin(t * 6)) * t) * 0.15
        samples.append((noise * 0.35 + tone + scrape) * env * 0.5)
    return samples


def make_glitter_samples() -> list[float]:
    sample_rate = 44100
    duration = 0.8
    total = int(sample_rate * duration)
    samples = []
    for i in range(total):
        t = i / sample_rate
        env = math.exp(-t * 4)
        sparkle = 0.0
        for freq in (1800, 2400, 3200, 4100):
            sparkle += math.sin(2 * math.pi * freq * t + i * 0.03) * 0.08
        ping = math.sin(2 * math.pi * 880 * t) * math.exp(-t * 10) * 0.25
        samples.append((sparkle + ping) * env * 0.45)
    return samples


def generate_sounds() -> None:
    generate_sound("sliding_stone", make_sliding_stone_samples())
    generate_sound("glitter", make_glitter_samples())


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate Create: Resonance replacement assets")
    parser.add_argument("--logo-only", action="store_true", help="Regenerate logo_small.png only")
    args = parser.parse_args()

    if args.logo_only:
        generate_logo()
        print("Logo generation complete.")
        return

    generate_textures()
    generate_sounds()
    print("Asset generation complete.")


if __name__ == "__main__":
    main()
