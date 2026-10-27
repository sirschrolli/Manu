#!/usr/bin/env python3
"""
Texturen der Magie: Zaubertafeln (Gegenstände), Zaubersymbole (24x24, für Zauberkreis und Magietisch), der Zauberkreis
(Radialmenü), der Magietisch (Block, Oberfläche, schwebende Runen) und die Geschosse der Erzmagier.

Zaubertafel: gewölbte Schiefertafel mit eingeritztem Rahmen, oben ein Edelstein des Elements, in der Mitte das
leuchtende Zeichen des Elements, darunter zwei Zeilen Schrift.

Zaubersymbole werden als Vektorbild in achtfacher Größe gezeichnet, verkleinert und bekommen einen dunklen Umriss –
so bleiben sie im Zauberkreis klar lesbar.

Aufruf:  python3 tools/magic_textures.py [vorschau.png]
"""
from __future__ import annotations

import math
import sys
from pathlib import Path
from typing import Callable

from PIL import Image, ImageDraw, ImageFilter

sys.path.insert(0, str(Path(__file__).resolve().parent))

import item_style as st  # noqa: E402
from items_materials import ELEMENT_GLOW, RUNE_GLYPHS  # noqa: E402
from pixelart import CLEAR, hexc, mix  # noqa: E402

ELEMENTS = ("fire", "ice", "lightning", "nature", "earth", "water")
SPELLS = ("aether_bolt",) + ELEMENTS

# =============================================================================================
# Zaubertafeln (16x16)
# =============================================================================================

TABLET_SLATE = st.ramp("#434a5c", dark=0.14, light=0.74, chroma_dark=1.5, cool=265)
TABLET_GOLD = st.ramp("#d9a53a", dark=0.25, light=0.97, chroma_light=0.6, hue_light=30)

TABLET = """
................
...effffffffe...
..ef66666665fd..
..f6555555554d..
..f6555555544d..
..f6555555544d..
..f6555555544d..
..f6555555544d..
..f6555555544d..
..f6555555544d..
..e6555555444c..
..e6555554444c..
..e5444444433c..
..dd44444333cb..
...dcccccccbb...
................
"""


def tablet(kind: str) -> st.Spr:
    """Schiefertafel im Goldrahmen: oben ein Edelstein des Elements, in der Mitte das leuchtende, eingeritzte Zeichen,
    darunter zwei Zeilen Schrift."""
    s = st.Spr.art(TABLET)
    # Edelstein im oberen Rahmen
    s.set(7, 1, ("D", 6))
    s.set(8, 1, ("D", 4))
    s.set(6, 1, ("B", 6))
    s.set(9, 1, ("B", 3))
    # Zeichen (aus den Runen, eine Zeile tiefer), mit dunkler Schnittkante unten rechts
    glyph = st.Spr.art(RUNE_GLYPHS[kind])
    marks: dict[tuple[int, int], tuple[str, int]] = {}
    for x, y, px in glyph.cells():
        ty = y + 1
        if 4 <= ty <= 11 and 4 <= x <= 11:
            marks[(x, ty)] = px
    for (x, y), px in marks.items():
        s.set(x, y, px)
    for (x, y) in marks:
        p = s.get(x + 1, y + 1)
        if p is not None and p[0] == "A" and (x + 1, y + 1) not in marks:
            s.set(x + 1, y + 1, ("A", 2))
    # zwei Zeilen Schrift unter dem Zeichen
    for x in (4, 5, 7, 8, 9, 11):
        if (x, 12) not in marks and s.get(x, 12) is not None and s.get(x, 12)[0] == "A":
            s.set(x, 12, ("A", 2))
    for x in (4, 6, 7, 9, 10):
        if s.get(x, 13) is not None and s.get(x, 13)[0] == "A":
            s.set(x, 13, ("A", 2))
    return s


def render_tablet(kind: str) -> Image.Image:
    glow = st.ramp(ELEMENT_GLOW[kind], dark=0.3, light=0.99, chroma_light=0.7)
    spr = tablet(kind)
    img = spr.outline(prefer=("B", "A"), override={"D": "B"}).image({"A": TABLET_SLATE, "B": TABLET_GOLD, "D": glow})
    # Lichtschein des Zeichens auf dem Schiefer ringsum
    glyph = {(x, y) for x, y, px in spr.cells() if px[0] == "D" and y >= 3}
    px = img.load()
    tint = hexc(ELEMENT_GLOW[kind])
    for y in range(16):
        for x in range(16):
            if (x, y) in glyph:
                continue
            cell = spr.get(x, y)
            if cell is None or cell[0] != "A":
                continue
            near = sum(1 for dx in (-1, 0, 1) for dy in (-1, 0, 1) if (x + dx, y + dy) in glyph)
            if near:
                px[x, y] = mix(px[x, y], tint, min(0.45, 0.18 + 0.09 * near))
    return img


# =============================================================================================
# Zaubersymbole (24x24, Vektor in achtfacher Größe)
# =============================================================================================

ICON = 24
SS = 8
BIG = ICON * SS


def rgba(value: str, alpha: int = 255) -> tuple[int, int, int, int]:
    return hexc(value, alpha)


def radial(size: int, cx: float, cy: float, r: float, inner: tuple, outer: tuple, power: float = 1.0) -> Image.Image:
    """Kreisförmiger Verlauf (innen -> außen), außerhalb des Radius durchsichtig."""
    img = Image.new("RGBA", (size, size), CLEAR)
    px = img.load()
    for y in range(size):
        for x in range(size):
            d = math.hypot(x + 0.5 - cx, y + 0.5 - cy) / r
            if d <= 1.0:
                px[x, y] = mix(inner, outer, d ** power)
    return img


def poly_mask(points: list[tuple[float, float]], size: int = BIG) -> Image.Image:
    m = Image.new("L", (size, size), 0)
    ImageDraw.Draw(m).polygon([(x * SS, y * SS) for x, y in points], fill=255)
    return m


def ellipse_mask(cx: float, cy: float, rx: float, ry: float, size: int = BIG) -> Image.Image:
    m = Image.new("L", (size, size), 0)
    ImageDraw.Draw(m).ellipse([(cx - rx) * SS, (cy - ry) * SS, (cx + rx) * SS, (cy + ry) * SS], fill=255)
    return m


def fill_mask(img: Image.Image, mask: Image.Image, color) -> None:
    """Füllt die Maske mit einer Farbe oder einem Bild (Verlauf)."""
    layer = color if isinstance(color, Image.Image) else Image.new("RGBA", img.size, color)
    clipped = Image.new("RGBA", img.size, CLEAR)
    clipped.paste(layer, (0, 0), mask)
    img.alpha_composite(clipped)


def vertical(size: int, top: tuple, bottom: tuple, y0: float = 0.0, y1: float | None = None) -> Image.Image:
    y1 = size if y1 is None else y1
    img = Image.new("RGBA", (size, size), CLEAR)
    px = img.load()
    for y in range(size):
        t = min(1.0, max(0.0, (y - y0) / max(1.0, y1 - y0)))
        c = mix(top, bottom, t)
        for x in range(size):
            px[x, y] = c
    return img


def glow_layer(mask: Image.Image, color: tuple, radius: float, strength: float = 1.0) -> Image.Image:
    blurred = mask.filter(ImageFilter.GaussianBlur(radius * SS))
    a = blurred.point(lambda v: min(255, int(v * strength)))
    layer = Image.new("RGBA", mask.size, color[:3] + (0,))
    layer.putalpha(a)
    return layer


def finish_icon(big: Image.Image, outline: tuple = (16, 10, 26, 255), glow: tuple | None = None) -> Image.Image:
    """Verkleinern, Alpha straffen, dunkler Umriss (4er-Nachbarschaft) und optional ein weicher Schein darunter."""
    small = big.resize((ICON, ICON), Image.LANCZOS)
    px = small.load()
    for y in range(ICON):
        for x in range(ICON):
            r, g, b, a = px[x, y]
            if a < 70:
                px[x, y] = (0, 0, 0, 0)
            else:
                px[x, y] = (r, g, b, 255)
    out = Image.new("RGBA", (ICON, ICON), CLEAR)
    if glow is not None:
        halo = Image.new("L", (ICON, ICON), 0)
        hp = halo.load()
        for y in range(ICON):
            for x in range(ICON):
                if px[x, y][3]:
                    hp[x, y] = 255
        halo = halo.filter(ImageFilter.GaussianBlur(1.6))
        hl = Image.new("RGBA", (ICON, ICON), glow[:3] + (0,))
        hl.putalpha(halo.point(lambda v: int(v * 0.55)))
        out.alpha_composite(hl)
    op = out.load()
    for y in range(ICON):
        for x in range(ICON):
            if px[x, y][3]:
                continue
            if any(0 <= x + dx < ICON and 0 <= y + dy < ICON and px[x + dx, y + dy][3]
                   for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                op[x, y] = outline
    for y in range(ICON):
        for x in range(ICON):
            if px[x, y][3]:
                op[x, y] = px[x, y]
    return out


def star_points(cx: float, cy: float, r_out: float, r_in: float, n: int, rot: float = -90.0) -> list[tuple[float, float]]:
    pts = []
    for i in range(n * 2):
        a = math.radians(rot + i * 180.0 / n)
        r = r_out if i % 2 == 0 else r_in
        pts.append((cx + math.cos(a) * r, cy + math.sin(a) * r))
    return pts


def icon_aether_bolt() -> Image.Image:
    img = Image.new("RGBA", (BIG, BIG), CLEAR)
    big_star = poly_mask(star_points(11.5, 12.5, 10.5, 2.6, 4))
    small_star = poly_mask(star_points(18.5, 5.5, 4.2, 1.1, 4))
    tiny = poly_mask(star_points(5.0, 19.5, 2.6, 0.8, 4))
    img.alpha_composite(glow_layer(big_star, rgba("#b070ff"), 1.4, 0.9))
    fill_mask(img, big_star, radial(BIG, 11.5 * SS, 12.5 * SS, 10.5 * SS, rgba("#ffffff"), rgba("#7a3ae0"), 0.7))
    fill_mask(img, poly_mask(star_points(11.5, 12.5, 5.0, 1.4, 4, rot=-45)), rgba("#e8d4ff", 200))
    fill_mask(img, small_star, radial(BIG, 18.5 * SS, 5.5 * SS, 4.2 * SS, rgba("#ffffff"), rgba("#a070ff"), 0.8))
    fill_mask(img, tiny, rgba("#d8c0ff"))
    fill_mask(img, ellipse_mask(11.5, 12.5, 1.6, 1.6), rgba("#ffffff"))
    return finish_icon(img, outline=(26, 10, 46, 255), glow=rgba("#a060ff"))


def icon_fire() -> Image.Image:
    img = Image.new("RGBA", (BIG, BIG), CLEAR)
    # Flammenschweif nach oben rechts
    tail = poly_mask([(4.5, 16.0), (9.0, 9.0), (14.0, 4.0), (21.5, 1.5), (17.0, 7.0), (20.5, 6.5), (15.5, 12.0),
                      (19.5, 12.5), (13.0, 18.5), (8.0, 21.0)])
    img.alpha_composite(glow_layer(tail, rgba("#ff5a10"), 1.2, 0.8))
    fill_mask(img, tail, radial(BIG, 8.5 * SS, 16.0 * SS, 15.0 * SS, rgba("#ffb030"), rgba("#c81e08"), 1.2))
    inner = poly_mask([(6.5, 15.5), (10.5, 10.5), (15.5, 6.0), (13.5, 10.5), (16.0, 10.5), (11.5, 16.5)])
    fill_mask(img, inner, rgba("#ffd25a"))
    core = ellipse_mask(8.6, 15.6, 5.2, 5.2)
    fill_mask(img, core, radial(BIG, 7.6 * SS, 14.6 * SS, 5.6 * SS, rgba("#fffbe8"), rgba("#ff9a20"), 1.3))
    fill_mask(img, ellipse_mask(7.4, 14.2, 1.8, 1.6), rgba("#ffffff"))
    for ex, ey, er in ((19.0, 15.5, 0.9), (17.0, 19.5, 0.7), (21.0, 10.0, 0.6)):
        fill_mask(img, ellipse_mask(ex, ey, er, er), rgba("#ffb84a"))
    return finish_icon(img, outline=(48, 10, 4, 255), glow=rgba("#ff6a1a"))


def icon_ice() -> Image.Image:
    img = Image.new("RGBA", (BIG, BIG), CLEAR)
    cx, cy = 12.0, 12.0
    arms = Image.new("L", (BIG, BIG), 0)
    d = ImageDraw.Draw(arms)

    def spike(x0: float, y0: float, ang: float, length: float, width: float) -> None:
        """Schmale, spitz zulaufende Eisnadel."""
        nx, ny = -math.sin(ang) * width / 2, math.cos(ang) * width / 2
        ex, ey = x0 + math.cos(ang) * length, y0 + math.sin(ang) * length
        d.polygon([((x0 + nx) * SS, (y0 + ny) * SS), ((x0 - nx) * SS, (y0 - ny) * SS),
                   ((ex - nx * 0.25) * SS, (ey - ny * 0.25) * SS), (ex * SS, ey * SS), ((ex + nx * 0.25) * SS, (ey + ny * 0.25) * SS)],
                  fill=255)

    for i in range(6):
        a = math.radians(i * 60 - 90)
        spike(cx, cy, a, 11.2, 2.3)
        for t, l in ((0.42, 3.8), (0.7, 2.9)):
            bx, by = cx + math.cos(a) * 11.2 * t, cy + math.sin(a) * 11.2 * t
            for side in (-1, 1):
                spike(bx, by, a + side * math.radians(52), l, 1.5)
    img.alpha_composite(glow_layer(arms, rgba("#6ad0ff"), 1.0, 0.8))
    fill_mask(img, arms, radial(BIG, cx * SS, cy * SS, 11.5 * SS, rgba("#ffffff"), rgba("#5ab4f0"), 0.8))
    hexagon = poly_mask([(cx + math.cos(math.radians(i * 60 - 90)) * 3.4, cy + math.sin(math.radians(i * 60 - 90)) * 3.4)
                         for i in range(6)])
    fill_mask(img, hexagon, rgba("#bfe8ff"))
    fill_mask(img, poly_mask([(cx + math.cos(math.radians(i * 60 - 90)) * 1.8, cy + math.sin(math.radians(i * 60 - 90)) * 1.8)
                              for i in range(6)]), rgba("#ffffff"))
    return finish_icon(img, outline=(8, 26, 48, 255), glow=rgba("#7fd8ff"))


def icon_lightning() -> Image.Image:
    img = Image.new("RGBA", (BIG, BIG), CLEAR)
    bolt = poly_mask([(13.5, 0.8), (5.0, 13.0), (10.8, 13.0), (7.5, 23.2), (19.0, 9.5), (13.0, 9.5), (17.5, 0.8)])
    img.alpha_composite(glow_layer(bolt, rgba("#ffd21a"), 1.4, 1.0))
    fill_mask(img, bolt, vertical(BIG, rgba("#fffbe0"), rgba("#ffb81a"), 0, BIG))
    inner = poly_mask([(14.2, 2.4), (8.0, 11.6), (12.6, 11.6), (10.4, 18.6), (16.6, 10.8), (11.6, 10.8), (15.8, 2.4)])
    fill_mask(img, inner, rgba("#ffffff"))
    for sx, sy in ((4.0, 6.0), (20.0, 16.0), (3.0, 18.5)):
        fill_mask(img, poly_mask(star_points(sx, sy, 1.9, 0.5, 4)), rgba("#fff3a0"))
    return finish_icon(img, outline=(52, 34, 4, 255), glow=rgba("#ffe14a"))


def icon_nature() -> Image.Image:
    img = Image.new("RGBA", (BIG, BIG), CLEAR)
    stem = Image.new("L", (BIG, BIG), 0)
    d = ImageDraw.Draw(stem)
    d.line([(12 * SS, 22.5 * SS), (12 * SS, 15 * SS), (11 * SS, 11 * SS)], fill=255, width=int(1.8 * SS), joint="curve")
    fill_mask(img, stem, rgba("#4a8a2a"))

    def leaf(base: tuple[float, float], tip: tuple[float, float], width: float, light: str, dark: str) -> None:
        bx, by = base
        tx, ty = tip
        mx, my = (bx + tx) / 2, (by + ty) / 2
        nx, ny = -(ty - by), tx - bx
        n = math.hypot(nx, ny)
        nx, ny = nx / n * width, ny / n * width
        pts = []
        for i in range(17):
            t = i / 16
            x = bx + (tx - bx) * t
            y = by + (ty - by) * t
            w = math.sin(t * math.pi) ** 0.8
            pts.append((x + nx * w, y + ny * w))
        for i in range(16, -1, -1):
            t = i / 16
            x = bx + (tx - bx) * t
            y = by + (ty - by) * t
            w = math.sin(t * math.pi) ** 0.8
            pts.append((x - nx * w * 0.9, y - ny * w * 0.9))
        m = poly_mask(pts)
        img.alpha_composite(glow_layer(m, rgba("#7ee05a"), 0.8, 0.6))
        fill_mask(img, m, radial(BIG, mx * SS, my * SS, math.hypot(tx - bx, ty - by) * 0.6 * SS, rgba(light), rgba(dark), 1.1))
        vein = Image.new("L", (BIG, BIG), 0)
        ImageDraw.Draw(vein).line([(bx * SS, by * SS), (tx * SS, ty * SS)], fill=255, width=int(0.7 * SS))
        fill_mask(img, vein, rgba("#2e6a1a", 220))

    leaf((11.5, 14.0), (2.0, 5.5), 3.6, "#b8f070", "#3a9a22")
    leaf((12.0, 12.0), (21.5, 3.0), 3.9, "#c8ff80", "#40a828")
    leaf((12.0, 18.0), (19.5, 15.5), 2.4, "#a8e060", "#358a1e")
    for sx, sy in ((5.0, 15.5), (19.0, 20.0), (16.5, 1.5)):
        fill_mask(img, poly_mask(star_points(sx, sy, 1.8, 0.5, 4)), rgba("#e8ffc0"))
    return finish_icon(img, outline=(10, 32, 8, 255), glow=rgba("#7ee05a"))


def icon_earth() -> Image.Image:
    img = Image.new("RGBA", (BIG, BIG), CLEAR)
    ground = poly_mask([(0.5, 19.0), (6.0, 17.6), (12.0, 18.4), (18.0, 17.4), (23.5, 18.6), (23.5, 23.5), (0.5, 23.5)])
    fill_mask(img, ground, vertical(BIG, rgba("#8a5a2a"), rgba("#4a2c14"), 17 * SS, 24 * SS))
    spikes = [
        ([(7.0, 19.0), (9.6, 4.0), (12.6, 19.0)], "#e8b878", "#8a5a2a"),
        ([(2.0, 19.5), (4.6, 10.5), (7.6, 19.5)], "#d8a060", "#7a4a22"),
        ([(12.0, 19.0), (16.4, 1.5), (19.6, 19.0)], "#f4c88a", "#9a6430"),
        ([(17.8, 19.5), (20.4, 11.0), (22.8, 19.5)], "#d09858", "#74461e"),
    ]
    for pts, light, dark in spikes:
        m = poly_mask(pts)
        img.alpha_composite(glow_layer(m, rgba("#e8a85a"), 0.6, 0.4))
        (x0, y0), (xt, yt), (x1, y1) = pts
        grad = Image.new("RGBA", (BIG, BIG), CLEAR)
        gp = ImageDraw.Draw(grad)
        gp.polygon([(x0 * SS, y0 * SS), (xt * SS, yt * SS), (x1 * SS, y1 * SS)], fill=rgba(dark))
        gp.polygon([(x0 * SS, y0 * SS), (xt * SS, yt * SS), ((x0 + x1) / 2 * SS, y0 * SS)], fill=rgba(light))
        fill_mask(img, m, grad)
    # Bernstein-Adern
    for gx, gy in ((16.0, 9.0), (9.6, 11.0)):
        fill_mask(img, ellipse_mask(gx, gy, 0.9, 1.2), rgba("#ffd27a"))
    for rx, ry, rr in ((3.0, 15.0, 1.0), (21.0, 14.0, 0.9), (13.0, 21.0, 0.8)):
        fill_mask(img, ellipse_mask(rx, ry, rr, rr * 0.8), rgba("#b07a44"))
    return finish_icon(img, outline=(34, 18, 6, 255), glow=rgba("#e8a85a"))


def icon_water() -> Image.Image:
    img = Image.new("RGBA", (BIG, BIG), CLEAR)
    pts = []
    # Wellenkamm: von links unten aufsteigend, oben eingerollt
    for i in range(41):
        t = i / 40
        x = 1.0 + t * 15.0
        y = 21.0 - math.sin(t * math.pi * 0.62) * 14.0
        pts.append((x, y))
    for i in range(25):
        a = math.radians(-100 + i * 9.5)
        pts.append((15.6 + math.cos(a) * 5.4, 9.0 + math.sin(a) * 5.4 + 0.5))
    pts += [(17.2, 11.8), (13.6, 11.2), (12.0, 14.0), (14.0, 18.0), (19.0, 19.5), (23.0, 18.5), (23.0, 23.0), (1.0, 23.0)]
    wave = poly_mask(pts)
    img.alpha_composite(glow_layer(wave, rgba("#3fb6ff"), 1.0, 0.7))
    fill_mask(img, wave, vertical(BIG, rgba("#7ae4ff"), rgba("#0a4a9a"), 4 * SS, 23 * SS))
    curl = Image.new("L", (BIG, BIG), 0)
    ImageDraw.Draw(curl).arc([10.4 * SS, 4.0 * SS, 21.2 * SS, 14.8 * SS], start=170, end=330, fill=255, width=int(1.4 * SS))
    fill_mask(img, curl, rgba("#ffffff"))
    foam = Image.new("L", (BIG, BIG), 0)
    fd = ImageDraw.Draw(foam)
    for fx, fy, fr in ((21.6, 9.6, 1.2), (22.5, 12.6, 0.8), (6.0, 15.0, 0.9), (9.0, 12.2, 0.7)):
        fd.ellipse([(fx - fr) * SS, (fy - fr) * SS, (fx + fr) * SS, (fy + fr) * SS], fill=255)
    fill_mask(img, foam, rgba("#e8fbff"))
    band = Image.new("L", (BIG, BIG), 0)
    ImageDraw.Draw(band).line([(3 * SS, 20.5 * SS), (9 * SS, 17.5 * SS), (14 * SS, 19.2 * SS), (21 * SS, 20.8 * SS)], fill=255,
                              width=int(1.0 * SS))
    fill_mask(img, band, rgba("#9ef0ff", 200))
    return finish_icon(img, outline=(4, 20, 44, 255), glow=rgba("#3fb6ff"))


# =============================================================================================
# Zauberkreis (Radialmenü)
# =============================================================================================
#
# spell_wheel/base.png   256x256, genutzt 200x200: dunkle Scheibe mit Goldringen, Siebenstern und Mittelfeld
# spell_wheel/parts.png  256x256:
#   (0, 0)    36x36  Fassung eines Zaubers (Gold, dunkle Mulde)
#   (40, 0)   36x36  Fassung, gesperrt (Silber, matt)
#   (80, 0)   52x52  Schein hinter der Fassung (weiß, wird eingefärbt)
#   (136, 0)  40x40  heller Ring um die Fassung (Auswahl unter der Maus)
#   (180, 0)  11x7   Marke "gewählt" (goldener Pfeil nach innen)
#   (192, 0)  10x12  Schloss
#   (204, 0)  5x5    Stufen-Edelstein leuchtend, (210, 0) dunkel
#   (216, 0)  7x7    Zeiger (Punkt am Rand des Mittelfelds)
#   (0, 56)   je 7x9 zwölf Runenzeichen nebeneinander (kreisen langsam um den Rand)
#   (0, 72)   28x28  kleine Fassung für die Anzeige an der Schnellleiste

GOLD_LIGHT = rgba("#f6dc96")
GOLD_MID = rgba("#d2a24e")
GOLD_DARK = rgba("#6e4a1c")
WELL = rgba("#0c0716")
VIOLET_DEEP = rgba("#120a22")
VIOLET = rgba("#2c1a4e")
ARCANE = rgba("#b886ff")


def _ss_canvas(w: int, h: int, k: int = 4) -> tuple[Image.Image, ImageDraw.ImageDraw, int]:
    img = Image.new("RGBA", (w * k, h * k), CLEAR)
    return img, ImageDraw.Draw(img), k


def _ring(draw: ImageDraw.ImageDraw, k: int, cx: float, cy: float, r: float, width: float, color) -> None:
    draw.ellipse([(cx - r) * k, (cy - r) * k, (cx + r) * k, (cy + r) * k], outline=color, width=max(1, round(width * k)))


def _disc(draw: ImageDraw.ImageDraw, k: int, cx: float, cy: float, r: float, color) -> None:
    draw.ellipse([(cx - r) * k, (cy - r) * k, (cx + r) * k, (cy + r) * k], fill=color)


def _down(img: Image.Image, w: int, h: int) -> Image.Image:
    return img.resize((w, h), Image.LANCZOS)


def wheel_base() -> Image.Image:
    size = 200
    c = size / 2
    img, d, k = _ss_canvas(size, size)
    # Grund: außen fast durchsichtig, nach innen dunkler (die Welt bleibt schwach sichtbar)
    for r in range(97, 0, -1):
        t = r / 97
        alpha = round(150 + 70 * (1 - t) ** 1.5)
        col = mix(VIOLET, VIOLET_DEEP, 1 - t)[:3] + (alpha,)
        _disc(d, k, c, c, r, col)
    # äußerer Rand: Goldband mit Kerben
    _ring(d, k, c, c, 98.0, 2.2, GOLD_DARK)
    _ring(d, k, c, c, 97.6, 1.2, GOLD_MID)
    _ring(d, k, c, c, 98.6, 0.6, GOLD_LIGHT[:3] + (180,))
    _ring(d, k, c, c, 86.0, 1.0, GOLD_DARK[:3] + (220,))
    _ring(d, k, c, c, 85.6, 0.5, GOLD_MID[:3] + (200,))
    for i in range(56):
        a = math.radians(i * 360 / 56 - 90)
        r0, r1 = (93.0, 97.5) if i % 8 == 0 else (95.5, 97.5)
        d.line([((c + math.cos(a) * r0) * k, (c + math.sin(a) * r0) * k), ((c + math.cos(a) * r1) * k, (c + math.sin(a) * r1) * k)],
               fill=GOLD_MID[:3] + (230 if i % 8 == 0 else 150,), width=max(1, round(0.8 * k)))
    # Siebenstern durch die sieben Fassungen (Abstand 74)
    pts = [(c + math.sin(math.radians(i * 360 / 7)) * 74, c - math.cos(math.radians(i * 360 / 7)) * 74) for i in range(7)]
    for step, alpha in ((3, 70), (2, 45)):
        for i in range(7):
            a, b = pts[i], pts[(i + step) % 7]
            d.line([(a[0] * k, a[1] * k), (b[0] * k, b[1] * k)], fill=ARCANE[:3] + (alpha,), width=max(1, round(0.9 * k)))
    _ring(d, k, c, c, 74.0, 0.8, ARCANE[:3] + (60,))
    # Mittelfeld
    for r in range(52, 0, -1):
        t = r / 52
        _disc(d, k, c, c, r, mix(rgba("#2a1848"), rgba("#08040f"), t ** 0.7)[:3] + (235,))
    _ring(d, k, c, c, 52.5, 2.0, GOLD_DARK)
    _ring(d, k, c, c, 52.2, 1.0, GOLD_MID)
    _ring(d, k, c, c, 50.0, 0.5, ARCANE[:3] + (120,))
    for i in range(7):
        a = math.radians(i * 360 / 7 - 90)
        x, y = c + math.cos(a) * 52.4, c + math.sin(a) * 52.4
        _disc(d, k, x, y, 2.0, GOLD_MID)
        _disc(d, k, x - 0.4, y - 0.4, 0.9, GOLD_LIGHT)
    out = Image.new("RGBA", (256, 256), CLEAR)
    out.alpha_composite(_down(img, size, size))
    return out


def _socket(locked: bool) -> Image.Image:
    size = 36
    img, d, k = _ss_canvas(size, size)
    c = size / 2
    rim_dark, rim_mid, rim_light = ((rgba("#3a3a46"), rgba("#7a7a8a"), rgba("#b8b8c8")) if locked
                                    else (GOLD_DARK, GOLD_MID, GOLD_LIGHT))
    _disc(d, k, c, c, 17.6, rim_dark)
    _disc(d, k, c, c, 16.6, rim_mid)
    # Glanz oben links auf dem Rand
    d.pieslice([(c - 16.6) * k, (c - 16.6) * k, (c + 16.6) * k, (c + 16.6) * k], 190, 280, fill=rim_light)
    _disc(d, k, c, c, 15.0, rim_dark)
    for r in range(14, 0, -1):
        t = r / 14
        _disc(d, k, c, c, r, mix(rgba("#24163c") if not locked else rgba("#1c1c24"), WELL, t ** 0.6))
    # vier Nieten
    for i in range(4):
        a = math.radians(45 + i * 90)
        x, y = c + math.cos(a) * 15.9, c + math.sin(a) * 15.9
        _disc(d, k, x, y, 1.1, rim_light)
    return _down(img, size, size)


def _halo() -> Image.Image:
    size = 52
    img = Image.new("RGBA", (size, size), CLEAR)
    px = img.load()
    for y in range(size):
        for x in range(size):
            dist = math.hypot(x + 0.5 - size / 2, y + 0.5 - size / 2) / (size / 2)
            if dist < 1.0:
                a = min(1.0, (1 - dist) ** 1.2 * 1.5)
                px[x, y] = (255, 255, 255, round(255 * a))
    return img


def _plate() -> Image.Image:
    """Kleine Fassung (28x28) für die Anzeige an der Schnellleiste."""
    size = 28
    img, d, k = _ss_canvas(size, size)
    c = size / 2
    _disc(d, k, c, c, 13.8, GOLD_DARK)
    _disc(d, k, c, c, 13.0, GOLD_MID)
    d.pieslice([(c - 13.0) * k, (c - 13.0) * k, (c + 13.0) * k, (c + 13.0) * k], 190, 280, fill=GOLD_LIGHT)
    _disc(d, k, c, c, 11.9, GOLD_DARK)
    for r in range(11, 0, -1):
        t = r / 11
        _disc(d, k, c, c, r + 0.4, mix(rgba("#2a1a46"), WELL, t ** 0.6)[:3] + (235,))
    return _down(img, size, size)


def _hover_ring() -> Image.Image:
    size = 40
    img, d, k = _ss_canvas(size, size)
    c = size / 2
    _ring(d, k, c, c, 19.0, 1.6, (255, 255, 255, 120))
    _ring(d, k, c, c, 18.6, 0.8, (255, 255, 255, 255))
    for i in range(8):
        a = math.radians(i * 45)
        x, y = c + math.cos(a) * 19.0, c + math.sin(a) * 19.0
        _disc(d, k, x, y, 0.9 if i % 2 else 1.4, (255, 255, 255, 255))
    return _down(img, size, size)


def _marker() -> Image.Image:
    img, d, k = _ss_canvas(11, 7)
    d.polygon([(0.5 * k, 0.5 * k), (10.5 * k, 0.5 * k), (5.5 * k, 6.5 * k)], fill=GOLD_DARK)
    d.polygon([(1.8 * k, 1.2 * k), (9.2 * k, 1.2 * k), (5.5 * k, 5.4 * k)], fill=GOLD_MID)
    d.polygon([(2.6 * k, 1.4 * k), (5.5 * k, 1.4 * k), (5.5 * k, 4.6 * k)], fill=GOLD_LIGHT)
    return _down(img, 11, 7)


LOCK = """
..........
...3333...
..3....3..
..3....3..
..3....3..
.66666665.
.65555554.
.6554.554.
.6555.554.
.65555554.
.54444443.
..........
"""


def _lock() -> Image.Image:
    return st.Spr.art(LOCK, w=10, h=12).image({"A": st.ramp("#8a8a9a", dark=0.2, light=0.92)})


def _gem(lit: bool) -> Image.Image:
    art = """
..6..
.655.
65543
.543.
..3..
"""
    ramp = st.ramp("#c99aff", dark=0.3, light=0.99, chroma_light=0.5) if lit else st.ramp("#4a4058", dark=0.15, light=0.5)
    return st.Spr.art(art, w=5, h=5).image({"A": ramp})


def _pointer() -> Image.Image:
    img, d, k = _ss_canvas(7, 7)
    _disc(d, k, 3.5, 3.5, 3.2, (255, 255, 255, 90))
    _disc(d, k, 3.5, 3.5, 1.9, (255, 255, 255, 255))
    return _down(img, 7, 7)


# zwölf Runenzeichen (7x9), an die Zeichen der Siegelsteine angelehnt
WHEEL_RUNES = [
    ["..#....", "..#....", "..###..", "..#..#.", "..#....", "..#....", "..#....", "..#....", "..#...."],
    ["#.....#", ".#...#.", "..#.#..", "...#...", "...#...", "...#...", "..#.#..", ".#...#.", "#.....#"],
    ["...#...", "..#.#..", ".#...#.", "#..#..#", "...#...", "...#...", "...#...", "..###..", "......."],
    ["#######", "......#", ".....#.", "....#..", "...#...", "..#....", ".#.....", "#......", "#######"],
    ["...#...", "...#...", "#######", "...#...", "...#...", "...#...", ".#.#.#.", "..###..", "...#..."],
    ["..###..", ".#...#.", ".#.....", "..##...", "....#..", ".....#.", ".#...#.", "..###..", "......."],
    ["#.....#", "##...##", "#.#.#.#", "#..#..#", "#.....#", "#.....#", "#.....#", "#.....#", "#.....#"],
    ["...#...", "..###..", ".#.#.#.", "#..#..#", "...#...", "...#...", "...#...", "..#.#..", ".#...#."],
    [".#####.", "#.....#", "#.....#", ".#...#.", "..#.#..", "...#...", "..#.#..", ".#...#.", "#.....#"],
    ["#......", "#......", "#..#...", "#.#.#..", "##...#.", "#.....#", "#......", "#......", "#......"],
    ["...#...", "...#...", "..#.#..", "..#.#..", ".#...#.", ".#...#.", "#.....#", "#######", "......."],
    ["#.#.#.#", ".......", "...#...", "..###..", ".#####.", "..###..", "...#...", ".......", "#.#.#.#"],
]


def _rune_strip() -> Image.Image:
    img = Image.new("RGBA", (12 * 8, 9), CLEAR)
    px = img.load()
    for i, rows in enumerate(WHEEL_RUNES):
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if ch == "#":
                    px[i * 8 + x, y] = (255, 255, 255, 255)
    return img


def wheel_parts() -> Image.Image:
    out = Image.new("RGBA", (256, 256), CLEAR)
    out.alpha_composite(_socket(False), (0, 0))
    out.alpha_composite(_socket(True), (40, 0))
    out.alpha_composite(_halo(), (80, 0))
    out.alpha_composite(_hover_ring(), (136, 0))
    out.alpha_composite(_marker(), (180, 0))
    out.alpha_composite(_lock(), (192, 0))
    out.alpha_composite(_gem(True), (204, 0))
    out.alpha_composite(_gem(False), (210, 0))
    out.alpha_composite(_pointer(), (216, 0))
    out.alpha_composite(_rune_strip(), (0, 56))
    out.alpha_composite(_plate(), (0, 72))
    return out


# =============================================================================================
# Magietisch (Block, 16x16)
# =============================================================================================

ASTONE = [hexc(c) for c in ("#120f1a", "#1b1726", "#252033", "#302941", "#3c334f", "#4a3f60", "#5b4f74")]
GOLDS = [hexc(c) for c in ("#4a2c0c", "#7a4e16", "#a8742a", "#d0a040", "#ecc866", "#fae6a4", "#fff8dc")]
GLOWS = [hexc(c) for c in ("#5a2ea8", "#8656e0", "#b48cff", "#dcc4ff", "#ffffff")]
CRYST = [hexc(c) for c in ("#0e3a52", "#14607e", "#1e8eae", "#2fb2d2", "#6cd8ee", "#b8f2fc", "#f2feff")]


def _noise16(seed: int) -> list[list[float]]:
    from pixelart import fractal_noise
    return fractal_noise(seed, ((8, 0.45), (4, 0.35), (2, 0.2)))


def _px(img: Image.Image) -> "PixelAccess":  # noqa: F821
    return img.load()


def table_base() -> Image.Image:
    """Arkanstein-Quader; die unteren vier Zeilen sind das Band des Sockels (nur sie sieht man am Tisch)."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    n = _noise16(701)
    courses = [(0, 4, 0), (4, 4, 5), (8, 4, 2)]
    for y in range(12):
        course = y // 4
        top, h, shift = courses[course]
        local = y - top
        for x in range(16):
            xx = (x + shift) % 16
            joint = xx % 8 == 7
            v = 0.42 + (n[y][x] - 0.5) * 0.35
            if local == 0:
                v += 0.18
            if local == h - 1 or joint:
                c = ASTONE[0] if local == h - 1 else ASTONE[1]
                px[x, y] = c
                continue
            if xx % 8 == 0:
                v += 0.08
            px[x, y] = pick(ASTONE, v)
    # Sockelband: helle Kante, Körper mit Rillen, dunkle Fußkante
    for x in range(16):
        px[x, 12] = ASTONE[0]
        px[x, 13] = pick(ASTONE, 0.78 + (n[13][x] - 0.5) * 0.1)
        px[x, 14] = pick(ASTONE, 0.48 + (n[14][x] - 0.5) * 0.2)
        px[x, 15] = ASTONE[1]
    for x in (2, 5, 10, 13):
        px[x, 14] = ASTONE[1]
    for x in (7, 8):
        px[x, 14] = GOLDS[3]
    return img


def pick(pal, v: float):
    v = max(0.0, min(0.9999, v))
    return pal[int(v * len(pal))]


def table_base_top() -> Image.Image:
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    n = _noise16(702)
    for y in range(16):
        for x in range(16):
            v = 0.5 + (n[y][x] - 0.5) * 0.25 + (0.08 if (x + y) % 9 == 0 else 0.0)
            px[x, y] = pick(ASTONE, v)
    return img


def table_bottom() -> Image.Image:
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    n = _noise16(703)
    for y in range(16):
        for x in range(16):
            px[x, y] = pick(ASTONE, 0.3 + (n[y][x] - 0.5) * 0.25)
    return img


def table_gold() -> Image.Image:
    """Goldband: glänzend, mit eingravierten Punkten und Linien."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    n = _noise16(704)
    for y in range(16):
        for x in range(16):
            v = 0.55 + (n[y][x] - 0.5) * 0.3 + (0.12 if (x - y) % 7 == 0 else 0.0)
            px[x, y] = pick(GOLDS, v)
    for i in range(16):
        if i % 4 == 1:
            for y in (2, 13):
                px[i, y] = GOLDS[1]
        px[i, 0] = GOLDS[5]
        px[i, 15] = GOLDS[1]
    for y in range(16):
        px[0, y] = GOLDS[5] if y < 15 else GOLDS[1]
        px[15, y] = GOLDS[1]
    return img


def table_column() -> Image.Image:
    """Säule (sichtbar u 4..12, v 6..12): Kannelur, Kapitell und Fuß, in der Mitte eine Rinne für die Runen."""
    img = table_base_top()
    px = img.load()
    n = _noise16(705)
    for y in range(16):
        for x in range(16):
            v = 0.5 + (n[y][x] - 0.5) * 0.25
            if x in (5, 10):
                v -= 0.18
            if x in (4, 11):
                v += 0.1
            px[x, y] = pick(ASTONE, v)
    for x in range(16):
        px[x, 6] = pick(ASTONE, 0.85)
        px[x, 7] = ASTONE[1]
        px[x, 11] = pick(ASTONE, 0.75)
    for y in range(8, 11):
        px[7, y] = ASTONE[0]
        px[8, y] = ASTONE[0]
    return img


def table_column_glow() -> Image.Image:
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y, v in ((8, 3), (9, 2), (10, 3)):
        px[7, y] = GLOWS[v]
        px[8, y] = GLOWS[v - 1]
    px[5, 9] = GLOWS[1]
    px[10, 9] = GLOWS[1]
    return img


def _table_top_layers() -> tuple[Image.Image, Image.Image]:
    """Platte: dunkler polierter Stein mit Goldrand, eingelegtem Goldkreis und Siebenstern; die Leuchtpunkte getrennt."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    glow = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    gp = glow.load()
    n = _noise16(706)
    for y in range(16):
        for x in range(16):
            v = 0.42 + (n[y][x] - 0.5) * 0.2 + (0.1 if (x + y) % 11 == 0 else 0.0)
            px[x, y] = pick(ASTONE, v)
    # Goldrand
    for i in range(16):
        px[i, 0] = GOLDS[5]
        px[0, i] = GOLDS[5]
        px[i, 15] = GOLDS[2]
        px[15, i] = GOLDS[2]
    for c in ((1, 1), (14, 1), (1, 14), (14, 14)):
        px[c] = GOLDS[4]
    # Goldkreis (Radius 5.5 um die Mitte)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x + 0.5 - 8, y + 0.5 - 8)
            if abs(d - 5.6) < 0.5:
                px[x, y] = GOLDS[4] if (x + y) % 3 else GOLDS[5]
            elif abs(d - 5.6) < 0.95 and d > 5.6:
                px[x, y] = ASTONE[0]
    # Innenfeld: tiefer und dunkler, mit einem feinen violetten Ring
    for y in range(16):
        for x in range(16):
            d = math.hypot(x + 0.5 - 8, y + 0.5 - 8)
            if d < 4.9:
                px[x, y] = pick(ASTONE, 0.22 + (n[y][x] - 0.5) * 0.15)
            if abs(d - 3.0) < 0.45:
                px[x, y] = mix(pick(ASTONE, 0.3), GLOWS[0], 0.6)
    pts = [(8 + math.sin(i * math.tau / 7) * 5.6, 8 - math.cos(i * math.tau / 7) * 5.6) for i in range(7)]
    # Leuchtpunkte an den Spitzen und in der Mitte
    for x, y in pts:
        gp[int(x), int(y)] = GLOWS[3]
    for x, y, c in ((7, 7, 4), (8, 8, 4), (7, 8, 3), (8, 7, 3)):
        gp[x, y] = GLOWS[c]
    return img, glow


def table_top() -> Image.Image:
    return _table_top_layers()[0]


def table_top_glow() -> Image.Image:
    return _table_top_layers()[1]


def table_top_side() -> Image.Image:
    """Seite der Platte (sichtbar Zeilen 3..5): Goldkante, Stein mit Runenkerben, Goldkante."""
    img = table_base_top()
    px = img.load()
    for x in range(16):
        px[x, 3] = GOLDS[5] if x % 5 else GOLDS[6]
        px[x, 4] = pick(ASTONE, 0.32)
        px[x, 5] = GOLDS[2]
    return img


def table_top_side_glow() -> Image.Image:
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for x in (2, 3, 7, 8, 12, 13):
        px[x, 4] = GLOWS[2] if x in (7, 8) else GLOWS[1]
    return img


def table_crystal() -> Image.Image:
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            facet = (x // 2) % 3
            v = (0.35, 0.6, 0.85)[facet] - y * 0.012
            if x % 2 == 0 and facet == 2:
                v += 0.1
            px[x, y] = pick(CRYST, v)
    return img


def table_glyphs() -> Image.Image:
    """Acht leuchtende Runen (je 16x16) für den Runenkreis über dem Tisch: weiße Striche mit weichem Schein."""
    out = Image.new("RGBA", (128, 16), CLEAR)
    k = 8
    strokes = [
        [((8, 2), (8, 14)), ((3, 8), (13, 8)), ((5, 5), (11, 11)), ((11, 5), (5, 11))],                 # Aether
        [((8, 2), (5, 9)), ((5, 9), (8, 14)), ((8, 14), (11, 9)), ((11, 9), (8, 2)), ((8, 8), (8, 12))],  # Feuer
        [((8, 2), (8, 14)), ((3, 5), (13, 11)), ((13, 5), (3, 11)), ((6, 2), (8, 4)), ((10, 2), (8, 4))],  # Eis
        [((10, 2), (5, 9)), ((5, 9), (11, 8)), ((11, 8), (6, 14))],                                       # Blitz
        [((8, 14), (8, 6)), ((8, 9), (4, 5)), ((8, 7), (12, 3)), ((4, 5), (4, 3)), ((12, 3), (13, 4))],   # Natur
        [((3, 13), (13, 13)), ((3, 13), (8, 3)), ((8, 3), (13, 13)), ((6, 9), (10, 9))],                   # Erde
        [((2, 6), (5, 4)), ((5, 4), (8, 6)), ((8, 6), (11, 4)), ((11, 4), (14, 6)),
         ((2, 11), (5, 9)), ((5, 9), (8, 11)), ((8, 11), (11, 9)), ((11, 9), (14, 11))],                   # Wasser
        [((4, 3), (12, 3)), ((8, 3), (8, 13)), ((5, 13), (11, 13)), ((5, 8), (11, 8))],                    # Siegel
    ]
    for i, lines in enumerate(strokes):
        core = Image.new("L", (16 * k, 16 * k), 0)
        d = ImageDraw.Draw(core)
        for (x0, y0), (x1, y1) in lines:
            d.line([(x0 * k, y0 * k), (x1 * k, y1 * k)], fill=255, width=int(1.6 * k))
            for (cx, cy) in ((x0, y0), (x1, y1)):
                d.ellipse([(cx - 0.8) * k, (cy - 0.8) * k, (cx + 0.8) * k, (cy + 0.8) * k], fill=255)
        halo = core.filter(ImageFilter.GaussianBlur(2.2 * k))
        core_s = core.resize((16, 16), Image.LANCZOS)
        halo_s = halo.resize((16, 16), Image.LANCZOS)
        glyph = Image.new("RGBA", (16, 16), CLEAR)
        gp = glyph.load()
        cp = core_s.load()
        hp = halo_s.load()
        for y in range(16):
            for x in range(16):
                a = max(cp[x, y], min(255, int(hp[x, y] * 1.6) // 2))
                gp[x, y] = (255, 255, 255, a)
        out.alpha_composite(glyph, (i * 16, 0))
    return out


def table_beam() -> Image.Image:
    """Lichtsäule: in der Mitte hell, zu den Seiten und nach oben ausgeblendet."""
    w, h = 32, 64
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    for y in range(h):
        up = y / (h - 1)          # 0 oben, 1 unten
        for x in range(w):
            side = 1.0 - abs(x + 0.5 - w / 2) / (w / 2)
            a = (side ** 2.2) * (up ** 1.3)
            core = side ** 8
            px[x, y] = (255, 255, 255, int(255 * min(1.0, a * 0.85 + core * 0.4 * up)))
    return img


# =============================================================================================
# Fenster des Magietischs (240x222 auf 256x256) und Bausteine (Knopf, Symbole, leere Fächer)
# =============================================================================================

PANEL_W, PANEL_H = 240, 222
CIRCLE = (63, 65)
SLOT_DARK = hexc("#0a0712")
SLOT_LIGHT = hexc("#5a4d72")
SLOT_FILL = hexc("#15111e")


def _slot(px, x: int, y: int) -> None:
    """Eingelassenes Fach (18x18, Inhalt bei x+1, y+1)."""
    for i in range(18):
        for j in range(18):
            if i == 0 or j == 0:
                c = SLOT_DARK
            elif i == 17 or j == 17:
                c = SLOT_LIGHT
            else:
                c = SLOT_FILL
            px[x + i, y + j] = c


def _ornate_slot(img: Image.Image, x: int, y: int) -> None:
    """Fach mit Goldfassung (für Stab, Tafel und Aetherit)."""
    d = ImageDraw.Draw(img)
    d.rectangle([x - 2, y - 2, x + 19, y + 19], outline=GOLDS[1])
    d.rectangle([x - 1, y - 1, x + 18, y + 18], outline=GOLDS[4])
    for cx, cy in ((x - 2, y - 2), (x + 19, y - 2), (x - 2, y + 19), (x + 19, y + 19)):
        d.point((cx, cy), fill=GOLDS[6])
    _slot(img.load(), x, y)


def magic_table_gui() -> Image.Image:
    k = 4
    img = Image.new("RGBA", (256, 256), CLEAR)
    px = img.load()
    from pixelart import fractal_noise
    noise = fractal_noise(911, ((16, 0.5), (8, 0.3), (4, 0.2)), w=256, h=256)
    cx, cy = CIRCLE
    for y in range(PANEL_H):
        for x in range(PANEL_W):
            v = 0.34 + (noise[y][x] - 0.5) * 0.12
            # leichte Vignette
            dx = (x - PANEL_W / 2) / (PANEL_W / 2)
            dy = (y - PANEL_H / 2) / (PANEL_H / 2)
            v -= 0.06 * (dx * dx + dy * dy)
            px[x, y] = pick(ASTONE, v)
    # rechtes Feld etwas heller und eingelassen
    for y in range(6, 124):
        for x in range(124, 234):
            r, g, b, a = px[x, y]
            px[x, y] = (min(255, r + 8), min(255, g + 6), min(255, b + 12), 255)
    for x in range(124, 234):
        px[x, 6] = ASTONE[0]
        px[x, 123] = ASTONE[5]
    for y in range(6, 124):
        px[124, y] = ASTONE[0]
        px[233, y] = ASTONE[5]
    # Kreis links (Ringe, Siebenstern) – fein gezeichnet
    big = Image.new("RGBA", (PANEL_W * k, PANEL_H * k), CLEAR)
    d = ImageDraw.Draw(big)

    def ring(r: float, width: float, color) -> None:
        d.ellipse([(cx - r) * k, (cy - r) * k, (cx + r) * k, (cy + r) * k], outline=color, width=max(1, round(width * k)))

    pts = [(cx + math.sin(i * math.tau / 7) * 41, cy - math.cos(i * math.tau / 7) * 41) for i in range(7)]
    for step, alpha in ((3, 110), (2, 60)):
        for i in range(7):
            a, b = pts[i], pts[(i + step) % 7]
            d.line([(a[0] * k, a[1] * k), (b[0] * k, b[1] * k)], fill=GLOWS[1][:3] + (alpha,), width=round(0.9 * k))
    ring(54.0, 1.6, GOLDS[1])
    ring(53.6, 0.8, GOLDS[4])
    ring(49.5, 0.7, ASTONE[0])
    ring(41.0, 0.7, GLOWS[1][:3] + (90,))
    ring(17.0, 1.4, GOLDS[1])
    ring(16.6, 0.7, GOLDS[4])
    for i in range(7):
        a = math.radians(i * 360 / 7 - 90 + 360 / 14)
        x0, y0 = cx + math.cos(a) * 49.5, cy + math.sin(a) * 49.5
        d.ellipse([(x0 - 1.6) * k, (y0 - 1.6) * k, (x0 + 1.6) * k, (y0 + 1.6) * k], fill=GOLDS[3])
    small = big.resize((PANEL_W, PANEL_H), Image.LANCZOS)
    img.alpha_composite(small)
    px = img.load()
    # Trennlinie zwischen Kreis und Feld
    for y in range(10, 120):
        px[121, y] = GOLDS[1]
        px[122, y] = GOLDS[3] if y % 6 else GOLDS[5]
    # Fächer mit Goldfassung: Stab (Mitte), Tafel, Aetherit
    _ornate_slot(img, cx - 9, cy - 9)
    _ornate_slot(img, 134, 21)
    _ornate_slot(img, 134, 44)
    px = img.load()
    # Zierlinie unter den Fächern
    for x in range(132, 226):
        px[x, 67] = GOLDS[2] if x % 4 else GOLDS[4]
    # Inventar: Trennlinie und Fächer
    for x in range(10, 230):
        px[x, 126] = GOLDS[1]
        px[x, 127] = GOLDS[3]
    for x0 in (8, 229):
        for dy in range(-2, 3):
            for dx in range(-2, 3):
                if abs(dx) + abs(dy) <= 2:
                    px[x0 + dx + 1, 126 + dy] = GOLDS[4] if abs(dx) + abs(dy) < 2 else GOLDS[1]
    for row in range(3):
        for col in range(9):
            _slot(px, 39 + col * 18, 139 + row * 18)
    for col in range(9):
        _slot(px, 39 + col * 18, 197)
    # Rahmen: dunkle Außenlinie, Goldleiste, dunkle Innenlinie, Zier an den Ecken
    for x in range(PANEL_W):
        for y in range(PANEL_H):
            edge = min(x, y, PANEL_W - 1 - x, PANEL_H - 1 - y)
            if edge == 0:
                px[x, y] = hexc("#07050c")
            elif edge == 1:
                px[x, y] = GOLDS[5] if (x < 3 or y < 3) else GOLDS[2]
            elif edge == 2:
                px[x, y] = GOLDS[3]
            elif edge == 3:
                px[x, y] = hexc("#0b0812")
    for (ox, oy) in ((0, 0), (PANEL_W - 9, 0), (0, PANEL_H - 9), (PANEL_W - 9, PANEL_H - 9)):
        for i in range(9):
            for j in range(9):
                dd = abs(i - 4) + abs(j - 4)
                if dd <= 4:
                    px[ox + i, oy + j] = GOLDS[5] if dd <= 1 else GOLDS[3] if dd <= 3 else GOLDS[1]
    # rechteckige Corners: entfernt (runde Ecken)
    for (x, y) in ((0, 0), (PANEL_W - 1, 0), (0, PANEL_H - 1), (PANEL_W - 1, PANEL_H - 1)):
        px[x, y] = CLEAR
    return img


def _button(state: str) -> Image.Image:
    w, h = 94, 16
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    if state == "normal":
        top, bottom, rim, rim_hi = hexc("#3a2a5c"), hexc("#1e1532"), GOLDS[3], GOLDS[5]
    elif state == "hover":
        top, bottom, rim, rim_hi = hexc("#5a3e8e"), hexc("#2c1d4a"), GOLDS[4], GOLDS[6]
    else:
        top, bottom, rim, rim_hi = hexc("#2a2533"), hexc("#18151e"), hexc("#4a4352"), hexc("#5e5668")
    for y in range(h):
        for x in range(w):
            t = y / (h - 1)
            c = mix(top, bottom, t)
            edge = min(x, y, w - 1 - x, h - 1 - y)
            if edge == 0:
                c = hexc("#07050c")
            elif edge == 1:
                c = rim_hi if y <= 1 or x <= 1 else rim
            elif edge == 2 and y == 2 and state != "disabled":
                c = mix(c, (255, 255, 255, 255), 0.18)
            px[x, y] = c
    for (x, y) in ((0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1)):
        px[x, y] = CLEAR
    if state == "hover":
        for x in range(3, w - 3):
            r, g, b, a = px[x, h // 2]
            px[x, h // 2] = (min(255, r + 10), min(255, g + 6), min(255, b + 18), a)
    return img


def _xp_icon() -> Image.Image:
    art = """
...666...
..65555..
.6555554.
.6555544.
.6555544.
.6554444.
..54443..
...444...
.........
"""
    return st.Spr.art(art, w=9, h=9).outline().image({"A": st.ramp("#7ed33a", dark=0.2, light=0.97, chroma_light=0.6)})


def _gem_icon() -> Image.Image:
    art = """
..6665...
.665554..
6655544.3
.555443..
..5443...
...43....
....3....
.........
.........
"""
    return st.Spr.art(art, w=9, h=9).outline().image({"A": st.ramp("#2fb2d2", dark=0.24, light=0.99, cool=255, chroma_light=0.3)})


EMPTY_STAFF = """
................
.............##.
............####
............####
.............##.
............#...
...........#....
..........#.....
.........#......
........#.......
.......#........
......#.........
.....#..........
....#...........
...#............
................
"""

EMPTY_TABLET = """
................
................
....########....
...#........#...
...#..####..#...
...#..#..#..#...
...#...##...#...
...#..#..#..#...
...#..####..#...
...#........#...
...#.######.#...
...#........#...
...#.####...#...
....########....
................
................
"""

EMPTY_GEM = """
................
................
................
.....######.....
....#......#....
...#..#..#..#...
..##########.#..
...#........#...
....#......#....
.....#....#.....
......#..#......
.......##.......
................
................
................
................
"""


def _empty_icon(art: str) -> Image.Image:
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    rows = [r for r in art.strip("\n").split("\n")]
    for y, row in enumerate(rows[:16]):
        for x, ch in enumerate(row[:16]):
            if ch == "#":
                px[x, y] = hexc("#6e6286")
    return img


ICONS: dict[str, Callable[[], Image.Image]] = {
    "aether_bolt": icon_aether_bolt,
    "fire": icon_fire,
    "ice": icon_ice,
    "lightning": icon_lightning,
    "nature": icon_nature,
    "earth": icon_earth,
    "water": icon_water,
}


def build(put: Callable[[str, Image.Image], None]) -> None:
    for kind in ELEMENTS:
        put(f"item/{kind}_spell_tablet", render_tablet(kind))
    for spell, fn in ICONS.items():
        put(f"gui/sprites/spell/{spell}", fn())
    put("gui/spell_wheel/base", wheel_base())
    put("gui/spell_wheel/parts", wheel_parts())
    put("block/magic_table_base", table_base())
    put("block/magic_table_base_top", table_base_top())
    put("block/magic_table_bottom", table_bottom())
    put("block/magic_table_gold", table_gold())
    put("block/magic_table_column", table_column())
    put("block/magic_table_column_glow", table_column_glow())
    put("block/magic_table_top", table_top())
    put("block/magic_table_top_glow", table_top_glow())
    put("block/magic_table_top_side", table_top_side())
    put("block/magic_table_top_side_glow", table_top_side_glow())
    put("block/magic_table_crystal", table_crystal())
    put("block/magic_table_glyphs", table_glyphs())
    put("block/magic_table_beam", table_beam())
    put("gui/magic_table", magic_table_gui())
    put("gui/sprites/magic_table/button", _button("normal"))
    put("gui/sprites/magic_table/button_highlighted", _button("hover"))
    put("gui/sprites/magic_table/button_disabled", _button("disabled"))
    put("gui/sprites/magic_table/experience", _xp_icon())
    put("gui/sprites/magic_table/aetherit", _gem_icon())
    put("gui/sprites/container/slot/staff", _empty_icon(EMPTY_STAFF))
    put("gui/sprites/container/slot/spell_tablet", _empty_icon(EMPTY_TABLET))
    put("gui/sprites/container/slot/aetherit", _empty_icon(EMPTY_GEM))


def preview(path: str) -> None:
    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key, img))
    zoom = 8
    cols = 7
    keys = list(sheet)
    cell = 24 * zoom + 12
    out = Image.new("RGBA", (cols * cell + 12, ((len(keys) + cols - 1) // cols) * cell + 12), (40, 36, 48, 255))
    for i, key in enumerate(keys):
        img = sheet[key]
        x = 12 + (i % cols) * cell
        y = 12 + (i // cols) * cell
        bg = Image.new("RGBA", img.size, (60, 52, 72, 255))
        bg.alpha_composite(img)
        out.alpha_composite(bg.resize((img.width * zoom, img.height * zoom), Image.NEAREST), (x, y))
    out.save(path)


if __name__ == "__main__":
    preview(sys.argv[1] if len(sys.argv) > 1 else "/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad/magic_preview.png")
