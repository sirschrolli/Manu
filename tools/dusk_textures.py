#!/usr/bin/env python3
"""
Dämmerquarz (0.98): der Edelstein, der Block und die Symbole der Dämmerrüstung.

  * Dämmerquarz – eine rauchige Quarzspitze, schräg aufrecht, mit einer kleineren an ihrem Fuß: sechseckige Säulen mit
    spitzer Endfläche, links im Licht, rechts im Schatten, ein heller Grat; im Inneren steht eine „Phantomspitze" (eine
    ältere Spitze, um die der Kristall weitergewachsen ist). Oben nachtviolett, zum Fuß hin glimmt ein Rest Abendrot.
  * Dämmerquarzblock – vier geschliffene Quarzsäulen (links die Lichtkante, rechts die Schattenkante), rauchviolett; in
    der Leuchtebene steigt durch jede Säule langsam ein Schimmer Abendrot auf (Bildfolge, nahtlos), dazwischen funkeln
    ein paar Sterne.
  * Die Rüstung der Schleicher: eine spitze Kapuze mit Halbmaske und Quarzschließe, ein Mantel mit Schulterumhang über
    einem Wams mit Quarzschuppen, Beinlinge mit Riemen und Knieschutz, weiche Stiefel mit Wickelriemen.

Wird von tools/generate_textures.py aufgerufen: build(put); Bildfolgen stehen in ANIMATIONS.
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import item_style as st
import items_armor as icons
import items_gear as gear
import pixelart as pa
import vanilla_style as vs
from pixelart import hexc

N = 16
CLEAR = (0, 0, 0, 0)
ANIMATIONS: dict[str, tuple[int, int, bool]] = {}

#: der Quarz: rauchiges Nachtviolett
QUARTZ = st.ramp("#5e4c8e", dark=0.17, light=0.93, cool=262, hue_dark=14, chroma_dark=1.15, warm=300, hue_light=10, chroma_light=0.45)
#: das Abendrot am Fuß der Spitzen
EMBER = st.ramp("#c97a74", dark=0.3, light=0.97, cool=330, hue_dark=12, chroma_dark=0.95, warm=60, hue_light=24, chroma_light=0.55)
#: Glanzlichter
GLINT = st.ramp("#e8dcff", dark=0.55, light=1.0, cool=270, warm=300, chroma_light=0.2)

#: Stoff der Rüstung (rauchiges Violettgrau), Leder, Riemenbeschläge
CLOTH = st.ramp("#47405e", dark=0.13, light=0.8, cool=262, hue_dark=10, chroma_dark=1.1, warm=300, chroma_light=0.35)
LEATHER = st.ramp("#5a4232", dark=0.14, light=0.78, cool=330, warm=60, chroma_light=0.4)
BUCKLE = gear.MOONSILVER

# --- Block ---
BLOCK_FRAMES = 16
#: das Licht der Dämmerung, das durch die Säulen steigt (dunkel -> hell)
DUSK_LIGHT = [hexc("#7a3e66"), hexc("#b0566a"), hexc("#e08a6e"), hexc("#ffc89a")]


# =============================================================================
# Der Edelstein
# =============================================================================

def _crystal(s: st.Spr, p0: tuple[float, float], p1: tuple[float, float], half: float, tip_len: float, ember_from: float,
             phantom: bool) -> None:
    """Eine Quarzspitze von p0 (Fuß) nach p1 (Spitze) – setzt Pixel in s (hinter vorhandenen nicht, darüber schon)."""
    ax, ay = p1[0] - p0[0], p1[1] - p0[1]
    length = math.hypot(ax, ay)
    ux, uy = ax / length, ay / length
    nx, ny = -uy, ux
    tip = length - tip_len
    for y in range(N):
        for x in range(N):
            px, py = x + 0.5 - p0[0], y + 0.5 - p0[1]
            a = px * ux + py * uy
            d = px * nx + py * ny
            hw = half if a < tip else half * max(0.0, (length - a) / tip_len) + 0.25
            if a < 0.0 or a > length or abs(d) > hw:
                continue
            if a >= tip:
                # die Endfläche: zwei Flächen, links im Licht, dazwischen der Grat
                if d < -0.45:
                    lv = ("A", 5)
                elif d < 0.2:
                    lv = ("D", 5)
                else:
                    lv = ("A", 2)
            elif d < -half * 0.42:
                lv = ("A", 5)
            elif d < -half * 0.15:
                lv = ("D", 4)                       # der helle Grat
            elif d <= half * 0.38:
                lv = ("A", 3)
            else:
                lv = ("A", 1)
            # die Phantomspitze: ein heller Winkel im Inneren
            if phantom and lv[0] == "A":
                pa_ = tip - 3.2 - abs(d) * 1.1
                if abs(a - pa_) < 0.55:
                    lv = ("A", min(5, lv[1] + 2))
            # zum Fuß hin das Abendrot
            if a < ember_from and lv[0] == "A":
                k = 1.0 - a / ember_from
                lv = ("B", max(1, min(5, lv[1] + (1 if k > 0.6 else 0))))
            s.set(x, y, lv)


def dusk_quartz_gem() -> Image.Image:
    s = st.Spr()
    # die kleine Spitze dahinter, nach links geneigt
    _crystal(s, (2.2, 15.2), (4.8, 6.8), 1.6, 2.4, 2.2, False)
    # die große Spitze, schräg aufrecht
    _crystal(s, (7.0, 15.4), (11.0, 1.0), 2.8, 4.4, 3.4, True)
    for x, y in ((9, 4), (9, 5)):
        if s.opaque(x, y):
            s.set(x, y, ("D", 6))
    return s.outline(prefer=("A", "B", "D"), override={"D": "A"}).image({"A": QUARTZ, "B": EMBER, "D": GLINT})


# =============================================================================
# Der Block
# =============================================================================

def _cells() -> list[tuple[float, float, float]]:
    """Die Facetten des Blocks: Mittelpunkte (nahtlos über die Ränder) und die Neigung jeder Fläche (0 dunkel .. 1 hell)."""
    pts = [(2.5, 3.0, 0.85), (8.5, 1.5, 0.55), (13.5, 4.5, 0.3), (5.0, 8.5, 0.6), (10.5, 8.0, 0.95), (1.5, 12.5, 0.4),
           (7.5, 13.5, 0.75), (13.0, 12.5, 0.5), (15.5, 8.5, 0.65)]
    return pts


def _nearest(x: float, y: float, pts: list[tuple[float, float, float]]) -> tuple[int, float, float]:
    """Nächste und zweitnächste Facette (Abstand nahtlos über die Ränder des Blocks)."""
    best, second, idx = 1e9, 1e9, 0
    for i, (px, py, _) in enumerate(pts):
        dx = min(abs(x - px), N - abs(x - px))
        dy = min(abs(y - py), N - abs(y - py))
        d = math.hypot(dx, dy)
        if d < best:
            second, best, idx = best, d, i
        elif d < second:
            second = d
    return idx, best, second


def dusk_quartz_block() -> tuple[Image.Image, Image.Image]:
    """Ein Gefüge aus Quarzfacetten (wie gewachsener Kristall), rauchviolett, an den Kanten dunkle Fugen und helle Grate;
    dazu die Leuchtebene (Bildfolge): in einigen Facetten glimmt das Abendrot auf und vergeht wieder, Sterne funkeln."""
    pts = _cells()
    cloud = vs.layered(98213, ((8, 8, 0.6), (4, 4, 0.4)))
    g = vs.grid(3)
    owner = [[0] * N for _ in range(N)]
    for y in range(N):
        for x in range(N):
            i, d1, d2 = _nearest(x + 0.5, y + 0.5, pts)
            owner[y][x] = i
            tilt = pts[i][2]
            tone = 2 + int(round(tilt * 3))                 # 2..5 je nach Neigung der Fläche
            if cloud[y][x] > 0.72:
                tone += 1
            elif cloud[y][x] < 0.22:
                tone -= 1
            edge = d2 - d1
            if edge < 0.55:
                tone = 1                                    # die Fuge zwischen zwei Facetten
            elif edge < 1.2 and tilt > 0.6:
                tone = 6                                    # ein heller Grat an der Lichtseite
            g[y][x] = max(1, min(6, tone))
    img = vs.paint(g, QUARTZ)
    for x, y in ((3, 2), (11, 7), (8, 13)):
        img.putpixel((x, y), pa.mix(img.getpixel((x, y)), hexc("#ffffff"), 0.5))

    frames = Image.new("RGBA", (N, N * BLOCK_FRAMES), CLEAR)
    # welche Facetten glimmen und wann (Phase in Bildern)
    glowing = {0: 0, 4: 6, 6: 11, 2: 3}
    stars = ((5, 4, 2), (12, 2, 9), (14, 14, 5), (2, 9, 13))
    for f in range(BLOCK_FRAMES):
        for y in range(N):
            for x in range(N):
                i = owner[y][x]
                if i not in glowing or g[y][x] <= 1:
                    continue
                k = 0.5 + 0.5 * math.cos(2.0 * math.pi * (f - glowing[i]) / BLOCK_FRAMES)
                if k < 0.35:
                    continue
                i2, d1, d2 = _nearest(x + 0.5, y + 0.5, pts)
                if d2 - d1 < 1.3:
                    continue                                # das Licht steht im Inneren der Facette, nicht an den Fugen
                level = min(3, int((k - 0.35) / 0.17))
                base = img.getpixel((x, y))
                frames.putpixel((x, f * N + y), pa.mix(base, DUSK_LIGHT[level], 0.2 + 0.09 * level))
        for (sx, sy, start) in stars:
            age = (f - start) % BLOCK_FRAMES
            if age < 3:
                frames.putpixel((sx, f * N + sy), hexc("#ffffff") if age == 1 else hexc("#ddd2ff"))
    return img, frames


# =============================================================================
# Die Symbole der Rüstung
# =============================================================================

HOOD = """
................
.......##.......
......####......
.....######.....
....########....
...##########...
...##########...
...###%%%%###...
...##%%%%%%##...
...##%%%%%%##...
...##%%%%%%##...
...###....###...
..####....####..
..###......###..
................
................
"""

SOFT_BOOTS = """
................
................
...###....###...
...###....###...
...###....###...
...###....###...
...###....###...
...####...####..
..#####...#####.
..######..######
..######..######
..######..######
...####....####.
................
................
................
"""


def dusk_icons(put: Callable[[str, Image.Image], None]) -> None:
    ramps = {"A": CLOTH, "B": LEATHER, "C": BUCKLE, "D": GLINT, "E": QUARTZ, "F": EMBER}

    # --- die Kapuze: spitz, mit Halbmaske (unten im Gesicht) und einer Quarzschließe am Hals
    s = icons.base(HOOD)
    for x in range(5, 11):                              # die Halbmaske über Mund und Nase
        s.set(x, 10, ("B", 3 if x < 8 else 2))
    for x in (5, 10):
        s.set(x, 9, ("B", 3))
    for x, y, px in ((7, 12, ("E", 5)), (8, 12, ("E", 3)), (7, 13, ("F", 3)), (8, 13, ("F", 2))):
        s.set(x, y, px)
    for x, y in ((6, 8), (9, 8)):                        # zwei Augen im Schatten der Kapuze
        s.set(x, y, ("F", 5))
    for y in range(3, 7):                               # die Naht über den Scheitel
        s.set(7, y, ("A", 2))
    put("item/dusk_hood", s.outline(prefer=("A", "B", "E"), override={"F": "A", "E": "A"}).image(ramps))

    # --- der Mantel: ein Wams mit Quarzschuppen, Lederriemen über der Brust, der Schulterumhang
    s = icons.base(icons.CHESTPLATE)
    for x, y, (sl, lv) in list(s.cells()):
        if 5 <= x <= 10 and 4 <= y <= 11 and sl == "A":
            # Quarzschuppen: 2er-Raster, oben hell
            row = (y - 4) // 2
            u = (x + row) % 2
            s.set(x, y, ("E", 4 if (y % 2 == 0 and u == 0) else 2 if u else 3))
    for i in range(9):                                   # der Riemen schräg über die Brust
        x, y = 3 + i, 2 + i
        if s.opaque(x, y):
            s.set(x, y, ("B", 4 if i % 3 else 5))
    s.set(7, 6, ("C", 5))
    for x in range(1, 15):                               # der Umhang über den Schultern
        for y in (1, 2):
            if s.opaque(x, y):
                s.set(x, y, ("A", 5 if y == 1 else 4))
    put("item/dusk_cloak", s.outline(prefer=("A", "B", "E"), override={"E": "A", "C": "B"}).image(ramps))

    # --- die Beinlinge: Gürtel mit Schnalle, Wickelriemen, Knieschutz aus Leder mit einem Quarz
    s = icons.base(icons.LEGGINGS)
    for x in range(2, 14):
        s.set(x, 2, ("B", 4 if x < 8 else 3))
    s.set(7, 2, ("C", 6))
    s.set(8, 2, ("C", 4))
    for x0 in (2, 10):
        for x in range(x0, x0 + 4):
            for y in (8, 9):
                s.set(x, y, ("B", 4 if y == 8 else 3))
        s.set(x0 + 1, 8, ("E", 5))
        for y in (11, 13):
            for x in range(x0, x0 + 4):
                if s.opaque(x, y):
                    s.set(x, y, ("B", 2))
    put("item/dusk_leggings", s.outline(prefer=("A", "B", "E"), override={"E": "A", "C": "B"}).image(ramps))

    # --- die Stiefel: weich, mit Wickelriemen und einer Quarzschließe
    s = icons.base(SOFT_BOOTS)
    for x, y, (sl, lv) in list(s.cells()):
        if y in (3, 5) or (y == 7 and (x < 7 or x > 9)):
            s.set(x, y, ("B", 3 if (x + y) % 2 else 2))
    for x, y in ((4, 9), (12, 9)):
        s.set(x, y, ("E", 5))
        s.set(x + 1, y, ("E", 3))
    put("item/dusk_boots", s.outline(prefer=("A", "B", "E"), override={"E": "A"}).image(ramps))


def build(put: Callable[[str, Image.Image], None]) -> None:
    block, glow = dusk_quartz_block()
    put("block/dusk_quartz_block", block)
    put("block/dusk_quartz_block_glow", glow)
    ANIMATIONS["block/dusk_quartz_block_glow"] = (BLOCK_FRAMES, 4, True)
    put("item/dusk_quartz", dusk_quartz_gem())
    dusk_icons(put)


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda k, i: out.__setitem__(k, i))
    tiles = []
    plain = Image.new("RGBA", (N * 2, N * 2))
    for dx in (0, N):
        for dy in (0, N):
            plain.alpha_composite(out["block/dusk_quartz_block"], (dx, dy))
    tiles.append(plain)
    for f in (0, 5, 10):
        fr = out["block/dusk_quartz_block_glow"].crop((0, f * N, N, (f + 1) * N))
        big = plain.copy()
        for dx in (0, N):
            for dy in (0, N):
                big.alpha_composite(fr, (dx, dy))
        tiles.append(big)
    for key in ("item/dusk_quartz", "item/dusk_hood", "item/dusk_cloak", "item/dusk_leggings", "item/dusk_boots"):
        t = Image.new("RGBA", (N * 2, N * 2), (0, 0, 0, 0))
        t.alpha_composite(out[key].resize((N * 2, N * 2), Image.NEAREST))
        tiles.append(t)
    z = 6
    sheet = Image.new("RGBA", (len(tiles) * (N * 2 * z + 10), N * 2 * z), (52, 50, 62, 255))
    for i, t in enumerate(tiles):
        sheet.alpha_composite(t.resize((N * 2 * z, N * 2 * z), Image.NEAREST), (i * (N * 2 * z + 10), 0))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "dusk.png")
