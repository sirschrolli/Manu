#!/usr/bin/env python3
"""
Texturen der Himmelswacht der Bosse (seit 0.82): das Siegel, das über dem Boss lädt (ein Runenkreis mit durchgestrichener
Schwinge), der Bannpfeil, die Linie für Ziellinie und Schweif, das Symbol des Flugbanns (18×18, auch in der Warnanzeige)
und der Richtungspfeil der Warnanzeige.

Siegel, Pfeil, Linie und Lichthof sind reines Weiß, ihre Helligkeit steckt in der Deckkraft: Gezeichnet werden sie in
der Farbe des Bosses – einmal deckend-durchscheinend (damit sie auch vor hellem Himmel Farbe haben) und einmal als Licht
obenauf.

Aufruf (aus dem Projektordner):
    python3 tools/sky_textures.py --preview /pfad/vorschau.png
Sonst über tools/generate_textures.py.
"""
from __future__ import annotations

import math
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

sys.path.insert(0, str(Path(__file__).resolve().parent))

import item_style as st  # noqa: E402

SS = 4  # Überabtastung beim Zeichnen der weichen Leuchtbilder


def _gray(img: Image.Image) -> Image.Image:
    """Graustufenbild → Leuchtbild: reines Weiß, die Helligkeit steckt in der Deckkraft (die Farbe gibt der Zeichner)."""
    g = img.convert("L")
    white = Image.new("L", g.size, 255)
    return Image.merge("RGBA", (white, white, white, g))


def _glow(sharp: Image.Image, radius: float, gain: float) -> Image.Image:
    """Legt einen weichen Schein unter die scharfen Linien."""
    blur = sharp.filter(ImageFilter.GaussianBlur(radius))
    out = Image.new("L", sharp.size, 0)
    px, pb, po = sharp.load(), blur.load(), out.load()
    for y in range(sharp.size[1]):
        for x in range(sharp.size[0]):
            po[x, y] = min(255, int(px[x, y] + pb[x, y] * gain))
    return out


WING_SHAPE = [(0.0, 0.0), (0.10, -0.26), (0.30, -0.50), (0.62, -0.62), (1.0, -0.50),
              (1.04, -0.12), (0.88, -0.22), (0.86, 0.12), (0.71, -0.02), (0.67, 0.28), (0.54, 0.12), (0.48, 0.38), (0.37, 0.20),
              (0.29, 0.40), (0.19, 0.24), (0.10, 0.34), (0.0, 0.16)]


def _wing(d: ImageDraw.ImageDraw, cx: float, cy: float, s: float, side: int, fill: int) -> None:
    """Eine gefiederte Schwinge (side: −1 links, +1 rechts), Schulter bei (cx, cy), Spannweite s."""
    d.polygon([(cx + side * s * x, cy + s * y) for x, y in WING_SHAPE], fill=fill)
    # dunkle Kiele zwischen den Schwungfedern
    for (nx, ny), (ax, ay) in (((0.88, -0.22), (0.66, -0.50)), ((0.71, -0.02), (0.52, -0.46)), ((0.54, 0.12), (0.38, -0.38)),
                               ((0.37, 0.20), (0.24, -0.28)), ((0.19, 0.24), (0.12, -0.14))):
        d.line([(cx + side * s * nx, cy + s * ny), (cx + side * s * ax, cy + s * ay)], fill=0, width=max(1, int(s * 0.035)))


def sigil() -> Image.Image:
    """Das Siegel: zwei Ringe, dazwischen Runen und Punkte, in der Mitte die durchgestrichene Schwinge."""
    n = 128
    big = n * SS
    img = Image.new("L", (big, big), 0)
    d = ImageDraw.Draw(img)
    c = big / 2.0

    def ring(r: float, w: float, fill: int = 255) -> None:
        d.ellipse([c - r * SS, c - r * SS, c + r * SS, c + r * SS], outline=fill, width=int(w * SS))

    ring(60, 3.0)
    ring(54, 1.0, 190)
    ring(41, 1.6, 230)
    # Runen zwischen den Ringen: acht verschiedene Zeichen aus Strichen
    runes = [
        [(-2, -4, -2, 4), (-2, -4, 2, -1), (2, -1, -2, 1)],
        [(0, -4, 0, 4), (-3, -1, 0, -4), (3, -1, 0, -4)],
        [(-2, -4, 2, 4), (2, -4, -2, 4)],
        [(-2, -4, -2, 4), (-2, 0, 2, -4), (-2, 0, 2, 4)],
        [(0, -4, 0, 4), (-3, 1, 0, 4), (3, 1, 0, 4)],
        [(-2, 4, -2, -4), (-2, -4, 2, -4), (2, -4, 2, 0), (2, 0, -2, 0)],
        [(-3, -4, 0, 4), (0, 4, 3, -4), (-1.5, 0, 1.5, 0)],
        [(-2, -4, 2, -4), (0, -4, 0, 4), (-2, 4, 2, 4)],
    ]
    for i, strokes in enumerate(runes):
        a = math.radians(i * 45.0 + 22.5)
        rx, ry = c + math.cos(a) * 47.5 * SS, c + math.sin(a) * 47.5 * SS
        # die Rune steht mit dem Fuß zur Mitte
        ca, sa = math.cos(a + math.pi / 2), math.sin(a + math.pi / 2)
        for x0, y0, x1, y1 in strokes:
            p0 = (rx + (x0 * ca - y0 * sa) * SS, ry + (x0 * sa + y0 * ca) * SS)
            p1 = (rx + (x1 * ca - y1 * sa) * SS, ry + (x1 * sa + y1 * ca) * SS)
            d.line([p0, p1], fill=255, width=int(1.3 * SS))
    for i in range(8):
        a = math.radians(i * 45.0)
        px, py = c + math.cos(a) * 47.5 * SS, c + math.sin(a) * 47.5 * SS
        r = 1.7 * SS
        d.polygon([(px, py - r * 1.4), (px + r, py), (px, py + r * 1.4), (px - r, py)], fill=255)
    for i in range(32):
        a = math.radians(i * 11.25)
        d.line([(c + math.cos(a) * 55.5 * SS, c + math.sin(a) * 55.5 * SS), (c + math.cos(a) * 58.5 * SS, c + math.sin(a) * 58.5 * SS)],
               fill=200, width=int(0.9 * SS))
    # die Schwingen in der Mitte …
    _wing(d, c - 2 * SS, c + 5 * SS, 31 * SS, -1, 255)
    _wing(d, c + 2 * SS, c + 5 * SS, 31 * SS, 1, 255)
    # … und der Strich hindurch (von einem dunklen Saum freigestellt)
    a0 = (c - 29 * SS, c + 25 * SS)
    a1 = (c + 29 * SS, c - 25 * SS)
    d.line([a0, a1], fill=0, width=int(9.0 * SS))
    d.line([a0, a1], fill=255, width=int(3.6 * SS))
    small = img.resize((n, n), Image.LANCZOS)
    return _gray(_glow(small, 2.6, 0.75))


def bolt() -> Image.Image:
    """Der Bannpfeil (Spitze rechts): heller Kern, rautenförmige Spitze, zwei Paar Widerhaken – ein Pfeil aus Licht."""
    w, h = 96, 32
    img = Image.new("L", (w * SS, h * SS), 0)
    d = ImageDraw.Draw(img)
    cy = h * SS / 2.0

    def p(x: float, y: float) -> tuple[float, float]:
        return (x * SS, cy + y * SS)

    # Schaft: läuft nach hinten dünn aus
    d.polygon([p(6, 0), p(60, -2.2), p(74, 0), p(60, 2.2)], fill=235)
    # Spitze
    d.polygon([p(92, 0), p(72, -7.5), p(66, 0), p(72, 7.5)], fill=255)
    # Widerhaken
    for bx, reach in ((54, 9.5), (40, 7.0)):
        d.polygon([p(bx + 10, -1.5), p(bx - 6, -reach), p(bx - 1, -1.5)], fill=225)
        d.polygon([p(bx + 10, 1.5), p(bx - 6, reach), p(bx - 1, 1.5)], fill=225)
    # Runenkerben im Schaft
    for x in (22, 30, 38):
        d.line([p(x, -1.6), p(x + 3, 1.6)], fill=90, width=int(0.9 * SS))
    small = img.resize((w, h), Image.LANCZOS)
    return _gray(_glow(small, 2.2, 0.9))


def line() -> Image.Image:
    """Querschnitt eines Lichtbands (für Ziellinie und Schweif): in der Mitte hell, zu den Rändern weich auslaufend."""
    w, h = 16, 16
    img = Image.new("L", (w, h), 0)
    px = img.load()
    for y in range(h):
        for x in range(w):
            t = abs((x + 0.5) / w * 2.0 - 1.0)
            core = max(0.0, 1.0 - t * 3.2)
            halo = max(0.0, 1.0 - t) ** 2.2
            px[x, y] = min(255, int(255 * min(1.0, core * 0.9 + halo * 0.55)))
    return _gray(img)


ARROW = """
................
.......oo.......
......owwo......
......owwo......
.....owwwwo.....
.....owwwwo.....
....owwllwwo....
....owwllwwo....
...owwllllwwo...
...owwllllwwo...
..owwlloollwwo..
..owwloooolwwo..
.owwloo..oolwwo.
.owwoo....oowwo.
.ooo........ooo.
................
"""


def _art(art: str, size: int, colors: dict[str, str]) -> Image.Image:
    rows = (art[1:] if art.startswith("\n") else art).rstrip("\n").split("\n")
    assert len(rows) == size and all(len(r) == size for r in rows), [(i, len(r)) for i, r in enumerate(rows)]
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != ".":
                img.putpixel((x, y), st.hexc(colors[ch]))
    return img


def glow() -> Image.Image:
    """Ein weicher, runder Lichthof."""
    n = 64
    img = Image.new("L", (n, n), 0)
    px = img.load()
    for y in range(n):
        for x in range(n):
            r = math.hypot(x + 0.5 - n / 2, y + 0.5 - n / 2) / (n / 2)
            v = max(0.0, 1.0 - r)
            px[x, y] = min(255, int(255 * (v ** 2.4 * 0.75 + max(0.0, 1.0 - r * 3.2) ** 1.5 * 0.5)))
    return _gray(img)


# linke Hälfte der Schwingen (8 breit); die rechte ist ihr Spiegelbild
ELYTRA_HALF = """
....ooo.
..oowwwo
.owwwllo
owwlllmo
owlllmmo
owllmmo.
owllmmo.
owlmmo..
owlmmo..
owlmo...
owlmo...
owmo....
owmo....
.oo.....
"""


def flight_ban_icon() -> Image.Image:
    """Symbol des Flugbanns (18×18): ein Paar Schwingen, rot durchgestrichen."""
    n = 18
    colors = {"o": st.hexc("#23273f"), "w": st.hexc("#f2f6ff"), "l": st.hexc("#b3c0e6"), "m": st.hexc("#7684b8")}
    rows = ELYTRA_HALF.strip("\n").split("\n")
    assert all(len(r) == 8 for r in rows), [len(r) for r in rows]
    img = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != ".":
                img.putpixel((1 + x, 2 + y), colors[ch])
                img.putpixel((16 - x, 2 + y), colors[ch])
    # der rote Strich von links unten nach rechts oben, dunkel gesäumt
    red, red_light, red_dark = st.hexc("#e8402e"), st.hexc("#ff8a66"), st.hexc("#5e0f14")
    core = [(1 + i, 16 - i) for i in range(16)]
    for x, y in core:
        for dx, dy in ((0, -1), (1, 0), (-1, 0), (0, 1), (-1, -1), (-2, 0)):
            if 0 <= x + dx < n and 0 <= y + dy < n:
                img.putpixel((x + dx, y + dy), red_dark)
    for x, y in core:
        img.putpixel((x, y), red)
    for x, y in core[1:]:
        img.putpixel((x - 1, y), red_light)
    return img


def arrow() -> Image.Image:
    """Richtungspfeil der Warnanzeige (weiß, wird in der Farbe des Bosses getönt)."""
    return _art(ARROW, 16, {"o": "#1a1c28", "w": "#ffffff", "l": "#d0d4e4"})


def build_all() -> dict[str, Image.Image]:
    return {
        "entity/sky_ward/sigil": sigil(),
        "entity/sky_ward/bolt": bolt(),
        "entity/sky_ward/line": line(),
        "entity/sky_ward/glow": glow(),
        "mob_effect/flight_ban": flight_ban_icon(),
        "gui/sky_ward/arrow": arrow(),
    }


def build(put) -> None:
    """Für tools/generate_textures.py."""
    for key, img in build_all().items():
        put(key, img)


def preview(target: str) -> None:
    imgs = build_all()
    sheet = Image.new("RGBA", (1180, 560), (24, 26, 36, 255))
    tints = [(255, 122, 42), (159, 232, 255), (154, 108, 255), (255, 210, 74), (32, 232, 255)]

    def tinted(img: Image.Image, rgb: tuple[int, int, int], scale: int) -> Image.Image:
        r, g, b, a = img.split()
        out = Image.merge("RGBA", (r.point(lambda v: v * rgb[0] // 255), g.point(lambda v: v * rgb[1] // 255), b.point(lambda v: v * rgb[2] // 255), a))
        return out.resize((img.size[0] * scale, img.size[1] * scale), Image.NEAREST)

    x = 10
    for rgb in tints[:3]:
        sheet.alpha_composite(tinted(imgs["entity/sky_ward/sigil"], rgb, 3), (x, 10))
        x += 390
    x = 10
    for rgb in tints:
        sheet.alpha_composite(tinted(imgs["entity/sky_ward/bolt"], rgb, 2), (x, 410))
        x += 200
    sheet.alpha_composite(imgs["mob_effect/flight_ban"].resize((108, 108), Image.NEAREST), (1040, 410))
    sheet.alpha_composite(imgs["gui/sky_ward/arrow"].resize((64, 64), Image.NEAREST), (960, 480))
    sheet.alpha_composite(tinted(imgs["entity/sky_ward/line"], (255, 255, 255), 4), (960, 410))
    sheet.convert("RGB").save(target)


if __name__ == "__main__":
    if len(sys.argv) >= 3 and sys.argv[1] == "--preview":
        preview(sys.argv[2])
    else:
        print(__doc__)
