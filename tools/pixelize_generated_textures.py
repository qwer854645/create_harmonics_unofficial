from PIL import Image
from pathlib import Path

src = Path(r"C:\Users\Administrator\.cursor\projects\f-others\assets")
block_dst = Path(
    r"f:\others\create_harmonics_unofficial\common\src\main\resources\assets\create_resonance\textures\block"
)
gui_dst = Path(
    r"f:\others\create_harmonics_unofficial\common\src\main\resources\assets\create_resonance\textures\gui"
)
hires = Path(r"f:\others\create_harmonics_unofficial\art\texture_gen_hires")
hires.mkdir(parents=True, exist_ok=True)
(gui_dst / "icons_gen").mkdir(parents=True, exist_ok=True)


def is_checker(px):
    r, g, b, a = px
    if a < 200:
        return False
    if abs(r - g) < 12 and abs(g - b) < 12:
        if 150 <= r <= 210 or 90 <= r <= 140:
            return True
    return False


def remove_checkerboard(im: Image.Image) -> Image.Image:
    im = im.convert("RGBA")
    w, h = im.size
    pix = im.load()
    corners = [pix[0, 0], pix[w - 1, 0], pix[0, h - 1], pix[w - 1, h - 1]]
    greyish = sum(
        1
        for c in corners
        if abs(c[0] - c[1]) < 15 and abs(c[1] - c[2]) < 15 and 80 <= c[0] <= 220
    )
    if greyish < 2:
        return im
    out = im.copy()
    op = out.load()
    for y in range(h):
        for x in range(w):
            if is_checker(op[x, y]):
                op[x, y] = (0, 0, 0, 0)
    return out


def to_pixel(im: Image.Image, size=16, quantize=True) -> Image.Image:
    im = im.convert("RGBA")
    w, h = im.size
    side = min(w, h)
    left = (w - side) // 2
    top = (h - side) // 2
    im = im.crop((left, top, left + side, top + side))
    im = im.resize((size, size), Image.Resampling.NEAREST)
    if quantize:
        alpha = im.split()[-1]
        rgb = im.convert("RGB").quantize(colors=24, method=Image.Quantize.MEDIANCUT).convert("RGB")
        im = rgb.convert("RGBA")
        im.putalpha(alpha)
    return im


block_map = {
    "gen_andesite_music_box_side.png": ["andesite_music_box_side.png", "andesite_music_box.png"],
    "gen_andesite_music_box_top.png": ["andesite_music_box_top.png"],
    "gen_andesite_music_box_bottom.png": ["andesite_music_box_bottom.png"],
    "gen_kinetic_music_box_side.png": ["kinetic_music_box_side.png", "kinetic_music_box.png"],
    "gen_kinetic_music_box_top.png": ["kinetic_music_box_top.png"],
    "gen_kinetic_music_box_bottom.png": ["kinetic_music_box_bottom.png"],
    "gen_music_conductor_side.png": ["music_conductor_side.png", "music_conductor.png"],
    "gen_music_conductor_top.png": ["music_conductor_top.png"],
    "gen_music_conductor_bottom.png": ["music_conductor_bottom.png"],
}

icon_map = {
    "gen_icon_music_box.png": "icon_music_box.png",
    "gen_icon_conductor.png": "icon_conductor.png",
    "gen_icon_note.png": "icon_note.png",
    "gen_icon_bookmark.png": "icon_bookmark.png",
    "gen_icon_seek.png": "icon_seek.png",
    "gen_icon_channel.png": "icon_channel.png",
    "gen_icon_section.png": "icon_section.png",
    "gen_icon_solo.png": "icon_solo.png",
}

report = []
for src_name, outs in block_map.items():
    p = src / src_name
    if not p.exists():
        report.append(f"MISSING {src_name}")
        continue
    raw = Image.open(p)
    raw.save(hires / src_name)
    pix = to_pixel(raw, 16, quantize=True)
    for out_name in outs:
        pix.save(block_dst / out_name)
        report.append(f"OK block/{out_name} from {src_name} {raw.size}->16")

preview_dir = hires / "preview_64"
preview_dir.mkdir(exist_ok=True)
for src_name in block_map:
    p = src / src_name
    if p.exists():
        to_pixel(Image.open(p), 64, quantize=False).save(
            preview_dir / src_name.replace("gen_", "p64_")
        )

for src_name, out_name in icon_map.items():
    p = src / src_name
    if not p.exists():
        report.append(f"MISSING {src_name}")
        continue
    raw = Image.open(p)
    raw.save(hires / src_name)
    cleaned = remove_checkerboard(raw)
    pix = to_pixel(cleaned, 16, quantize=False)
    pix = remove_checkerboard(pix)
    pix.save(gui_dst / "icons_gen" / out_name)
    report.append(f"OK gui/icons_gen/{out_name}")

atlas = Image.new("RGBA", (128, 16), (0, 0, 0, 0))
for i, out_name in enumerate(icon_map.values()):
    ip = gui_dst / "icons_gen" / out_name
    if ip.exists():
        atlas.paste(Image.open(ip), (i * 16, 0))
atlas.save(gui_dst / "icons_gen" / "icons_row.png")
report.append("OK gui/icons_gen/icons_row.png")

print("\n".join(report))
print("done")
