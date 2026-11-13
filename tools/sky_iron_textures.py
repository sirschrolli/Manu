#!/usr/bin/env python3
"""
Himmelseisen (0.99): Rohstoff, Barren, Block und die Symbole der Rüstung der Sternwandler.

  * Rohes Himmelseisen – drei Brocken mit schwarzer, warm schimmernder Schmelzkruste und Daumendellen; an den
    Bruchstellen schaut blankes, kühles Eisen heraus.
  * Himmelseisenbarren – ein liegender Barren aus blaustichigem Eisen, auf der Oberseite das Muster der Sterne: feine,
    sich kreuzende Bänder (wie geätztes Meteoreisen), dazu ein Glanzpunkt.
  * Himmelseisenblock – eine geschliffene Platte aus Kristallkörnern; in jedem Korn laufen die Bänder in eine andere
    Richtung (drei Richtungen, wie die Figuren im Meteoreisen), zwischen den Körnern feine dunkle Säume. In der Leuchtebene
    zieht langsam ein kühler Schimmer schräg über die Platte (Bildfolge, nahtlos), dazwischen funkeln ein paar Sterne.
  * Die Rüstung der Sternwandler: ein geschlossener Helm mit leuchtendem Sehschlitz und einem Stern über der Stirn, ein
    Harnisch mit einem Sternbild auf der Brust, Beinschienen mit Sternnieten und Stiefel mit Kometenflossen.

Wird von tools/generate_textures.py aufgerufen: build(put); Bildfolgen stehen in ANIMATIONS.
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import item_style as st
import items_armor as icons
import items_gear as gear
import items_materials as mats
import pixelart as pa
import vanilla_style as vs
from pixelart import hexc

N = 16
CLEAR = (0, 0, 0, 0)
ANIMATIONS: dict[str, tuple[int, int, bool]] = {}

#: das Eisen: kühles Blaugrau mit einem Hauch Violett in den Schatten
SKY_IRON = st.ramp("#6b7898", dark=0.16, light=0.86, cool=280, hue_dark=16, chroma_dark=1.3, warm=230, hue_light=6, chroma_light=0.35)
#: die Platten der Rüstung: etwas dunkler, blauer
PLATE = st.ramp("#56628a", dark=0.14, light=0.74, cool=282, hue_dark=18, chroma_dark=1.4, warm=230, hue_light=6, chroma_light=0.4)
#: die Schmelzkruste: fast schwarz, warm
CRUST = st.ramp("#3e3230", dark=0.12, light=0.62, cool=320, hue_dark=10, chroma_dark=1.1, warm=50, chroma_light=0.6)
#: Glanzlichter und Sterne
GLINT = st.ramp("#dfeaff", dark=0.55, light=1.0, cool=260, warm=230, chroma_light=0.2)
#: das Leuchten der Leere (Sehschlitz, Sterne der Rüstung)
STARLIGHT = st.ramp("#8fd3ff", dark=0.42, light=1.0, cool=270, hue_dark=20, warm=220, chroma_light=0.35)
#: Kristall der Leere (violett)
CRYSTAL = st.ramp("#8a6ad8", dark=0.2, light=0.95, cool=285, warm=250, hue_light=12, chroma_light=0.4)
#: Säume: Sternsilber
TRIM = gear.MOONSILVER

BLOCK_FRAMES = 16
#: der Schimmer, der über die Platte zieht (schwach -> stark)
SHEEN = [hexc("#6f86b8"), hexc("#9ab2e2"), hexc("#c8dcff"), hexc("#f2f8ff")]


# =============================================================================
# Rohstoff und Barren
# =============================================================================

def raw_sky_iron() -> st.Spr:
    """Drei Brocken (wie Roheisen), aber mit schwarzer Kruste und blankem Eisen an den Bruchstellen."""
    s = mats.raw_chunk()
    # Daumendellen in der Kruste: kleine, dunkle Mulden mit hellem Rand oben links
    for x, y in ((6, 4), (9, 6), (3, 7), (11, 10), (8, 3)):
        if s.opaque(x, y) and s.get(x, y)[0] == "A":
            s.set(x, y, ("A", 1))
            if s.opaque(x - 1, y - 1) and s.get(x - 1, y - 1)[0] == "A":
                s.set(x - 1, y - 1, ("A", 5))
    return s


INGOT_STARS = """
................
................
................
................
........6665....
......665555554.
....66555555544.
..6655555555443.
.5665555555433..
.44466555543332.
.444446654333...
..3444433332....
...33333222.....
................
................
................
"""


def sky_iron_ingot() -> st.Spr:
    """Der Barren: auf der Oberseite das Muster der Sterne (zwei Scharen feiner Bänder, die sich kreuzen)."""
    s = st.Spr.art(INGOT_STARS)
    for x, y, (sl, lv) in list(s.cells()):
        if lv != 5:
            continue
        a = (2 * x + y) % 7            # die eine Schar
        b = (2 * x - y) % 8            # die andere, sie kreuzt
        if a == 0:
            s.set(x, y, ("A", 6))
        elif b == 0:
            s.set(x, y, ("A", 4))
    # ein Glanzpunkt an der Lichtkante und ein winziger Stern
    s.set(9, 5, ("D", 6))
    s.set(5, 7, ("D", 5))
    return s


# =============================================================================
# Der Block
# =============================================================================

#: die Bänder der Platte (wie im geätzten Meteoreisen): waagerecht ("h") und in zwei steilen Richtungen ("p": Steigung 2,
#: "m": Steigung -2) – Versatz und Breite; die späteren liegen oben. Alle Richtungen sind über 16 Pixel kachelbar.
BARS = [("h", 3.0, 3.0), ("p", 2.0, 3.0), ("m", 11.0, 3.0), ("h", 11.0, 2.5), ("p", 10.0, 2.5)]


def _bar_offset(kind: str, x: float, y: float, off: float) -> float:
    if kind == "h":
        s = y - off
    elif kind == "p":
        s = x - y / 2.0 - off
    else:
        s = x + y / 2.0 - off
    s %= N
    return s - N if s > N / 2 else s


def sky_iron_block() -> tuple[Image.Image, Image.Image]:
    """Die Platte mit dem Muster der Sterne und die Leuchtebene (der wandernde Schimmer, funkelnde Sterne)."""
    cloud = vs.layered(51177, ((8, 8, 0.6), (4, 4, 0.4)))
    speck = vs.layered(51180, ((2, 2, 1.0),))
    g = vs.grid(3)
    on_bar = [[False] * N for _ in range(N)]
    for y in range(N):
        for x in range(N):
            # dazwischen: feinkörniges, dunkleres Gefüge
            tone = 3 if cloud[y][x] > 0.45 else 2
            if speck[y][x] > 0.82:
                tone += 1
            for kind, off, width in BARS:
                s = _bar_offset(kind, x + 0.5, y + 0.5, off)
                if abs(s) <= width / 2.0:
                    edge = s / (width / 2.0)
                    tone = 5 if edge < -0.5 else 3 if edge > 0.6 else 4
                    on_bar[y][x] = True
            g[y][x] = max(1, min(6, tone))
    img = vs.paint(g, SKY_IRON)
    for x, y in ((4, 2), (12, 7), (7, 12)):
        img.putpixel((x, y), pa.mix(img.getpixel((x, y)), hexc("#ffffff"), 0.5))

    frames = Image.new("RGBA", (N, N * BLOCK_FRAMES), CLEAR)
    stars = ((2, 6, 1), (11, 3, 7), (14, 13, 4), (6, 11, 12))
    for f in range(BLOCK_FRAMES):
        # der Schimmer: ein breites, weiches Band, das schräg (von oben links nach unten rechts) über die Bänder wandert
        center = f * 2.0
        for y in range(N):
            for x in range(N):
                if not on_bar[y][x]:
                    continue
                t = ((x + y) - center) % 32.0
                d = min(t, 32.0 - t)
                if d > 3.5:
                    continue
                k = 1.0 - d / 3.5
                level = min(3, int(k * 3.2 + (0.6 if g[y][x] >= 5 else 0.0)))
                base = img.getpixel((x, y))
                frames.putpixel((x, f * N + y), pa.mix(base, SHEEN[level], 0.25 + 0.12 * level))
        for (sx, sy, start) in stars:
            age = (f - start) % BLOCK_FRAMES
            if age < 3:
                frames.putpixel((sx, f * N + sy), hexc("#ffffff") if age == 1 else hexc("#cfe0ff"))
    return img, frames


# =============================================================================
# Die Symbole der Rüstung
# =============================================================================

HELMET = """
................
................
................
.....######.....
....########....
...##########...
...##########...
...##########...
...##########...
...##########...
...##########...
...###....###...
...##......##...
................
................
................
"""

def starwalker_icons(put: Callable[[str, Image.Image], None]) -> None:
    ramps = {"A": PLATE, "B": TRIM, "D": STARLIGHT, "E": CRYSTAL, "C": GLINT}

    # --- der Helm: geschlossen, ein leuchtender Sehschlitz, über der Stirn ein Stern, seitlich Kometenflossen
    s = icons.base(HELMET)
    for x in range(4, 12):
        s.set(x, 8, ("D", 4 if 5 <= x <= 10 else 3))           # der Sehschlitz
    s.set(7, 9, ("A", 2))
    s.set(8, 9, ("A", 2))
    for x, y, px in ((7, 4, ("C", 6)), (7, 3, ("D", 5)), (6, 4, ("D", 4)), (8, 4, ("D", 4)), (7, 5, ("D", 3))):
        s.set(x, y, px)                                        # der Stern über der Stirn
    for x, y in ((3, 6), (2, 7), (1, 8)):                      # die Kometenflosse (links, nach hinten gezogen)
        s.set(x, y, ("B", 5 if x == 3 else 4))
    for x in range(4, 12):
        s.set(x, 10, ("B", 4 if x < 8 else 3))                  # der Saum unten
    put("item/starwalker_helmet", s.outline(prefer=("A", "B"), override={"D": "A", "C": "A", "E": "A"}).image(ramps))

    # --- der Harnisch: Silbersäume an den Schultern, auf der Brust ein Sternbild aus vier Sternen und ein großer Stern
    s = icons.base(icons.CHESTPLATE)
    for x in range(2, 14):
        for y in (2, 3):
            if s.opaque(x, y) and (x <= 5 or x >= 10):
                s.set(x, y, ("B", 5 if y == 2 else 4))
    big = ((7, 6, ("C", 6)), (8, 6, ("D", 5)), (7, 5, ("D", 4)), (7, 7, ("D", 4)), (6, 6, ("D", 3)), (8, 7, ("D", 3)),
           (8, 5, ("D", 3)))
    for x, y, px in big:
        s.set(x, y, px)
    for x, y in ((5, 10), (10, 9), (8, 12)):                  # das Sternbild: drei Sterne, schwach verbunden
        s.set(x, y, ("D", 5))
    for x, y in ((6, 10), (7, 10), (8, 10), (9, 10), (9, 11)):
        if s.opaque(x, y):
            s.set(x, y, ("A", 5))
    put("item/starwalker_chestplate", s.outline(prefer=("A", "B"), override={"D": "A", "C": "A", "E": "A"}).image(ramps))

    # --- die Beinschienen: Gürtel mit Kristallschnalle, Knie mit Sternnieten, Bänder in Silber
    s = icons.base(icons.LEGGINGS)
    for x in range(2, 14):
        s.set(x, 2, ("B", 5 if x < 8 else 4))
    s.set(7, 2, ("E", 6))
    s.set(8, 2, ("E", 4))
    for x0 in (2, 10):
        s.set(x0 + 1, 8, ("D", 5))
        s.set(x0 + 2, 8, ("C", 6))
        for x in range(x0, x0 + 4):
            if s.opaque(x, 11):
                s.set(x, 11, ("B", 3))
    put("item/starwalker_leggings", s.outline(prefer=("A", "B"), override={"D": "A", "C": "A", "E": "B"}).image(ramps))

    # --- die Stiefel: Kometenflossen an den Knöcheln (silbern, wie kleine Flügel nach innen-oben), ein Stern auf der Spitze
    s = icons.base(icons.BOOTS)
    for (x, y, lv) in ((6, 6, 5), (6, 5, 4), (7, 5, 5), (7, 4, 4)):
        s.set(x, y, ("B", lv))
        s.set(15 - x, y, ("B", lv - 1))
    for x0 in (2, 13):
        s.set(x0, 10, ("D", 5))
    for x in list(range(3, 6)) + list(range(10, 13)):
        s.set(x, 8, ("B", 3))
    put("item/starwalker_boots", s.outline(prefer=("A", "B"), override={"D": "A", "C": "A", "E": "A"}).image(ramps))


def build(put: Callable[[str, Image.Image], None]) -> None:
    block, glow = sky_iron_block()
    put("block/sky_iron_block", block)
    put("block/sky_iron_block_glow", glow)
    ANIMATIONS["block/sky_iron_block_glow"] = (BLOCK_FRAMES, 5, True)
    put("item/raw_sky_iron", mats.render(raw_sky_iron(), {"A": CRUST, "B": SKY_IRON}, prefer=("A",)))
    put("item/sky_iron_ingot", mats.render(sky_iron_ingot(), {"A": SKY_IRON, "D": GLINT}, override={"D": "A"}))
    starwalker_icons(put)


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda k, i: out.__setitem__(k, i))
    tiles = []
    plain = Image.new("RGBA", (N * 2, N * 2))
    for dx in (0, N):
        for dy in (0, N):
            plain.alpha_composite(out["block/sky_iron_block"], (dx, dy))
    tiles.append(plain)
    for f in (0, 5, 10):
        fr = out["block/sky_iron_block_glow"].crop((0, f * N, N, (f + 1) * N))
        big = plain.copy()
        for dx in (0, N):
            for dy in (0, N):
                big.alpha_composite(fr, (dx, dy))
        tiles.append(big)
    for key in ("item/raw_sky_iron", "item/sky_iron_ingot", "item/starwalker_helmet", "item/starwalker_chestplate",
                "item/starwalker_leggings", "item/starwalker_boots"):
        t = Image.new("RGBA", (N * 2, N * 2), (0, 0, 0, 0))
        t.alpha_composite(out[key].resize((N * 2, N * 2), Image.NEAREST))
        tiles.append(t)
    z = 5
    sheet = Image.new("RGBA", (len(tiles) * (N * 2 * z + 10), N * 2 * z), (52, 50, 62, 255))
    for i, t in enumerate(tiles):
        sheet.alpha_composite(t.resize((N * 2 * z, N * 2 * z), Image.NEAREST), (i * (N * 2 * z + 10), 0))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "sky_iron.png")
