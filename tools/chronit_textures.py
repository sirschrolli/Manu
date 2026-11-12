#!/usr/bin/env python3
"""
Chronit (1.01): der Kristall aus geronnener Zeit, sein Block und die Symbole der Rüstung der Zeitwächter.

  * Chronit – ein bernsteinfarbener Kristall, an beiden Enden zugespitzt (schräg gestellt), drei Längsflächen im Licht,
    im Schatten und dazwischen; innen hängen erstarrte Funken (helle Punkte, als stünde Staub in einem Sonnenstrahl).
  * Chronitblock – ein geschliffenes Zifferblatt: ein abgefaster Rahmen, in den Ecken Facetten, in der Mitte eine runde
    Scheibe mit zwölf Stundenmarken (die vier Hauptstunden heller) und einer Nabe. In der Leuchtebene wandert ein
    Lichtzeiger reihum (eine Umdrehung je Bildfolge, mit einem nachglühenden Schweif), die Marken glimmen.
  * Die Rüstung der Zeitwächter: ein geschlossener Helm aus Uhrbronze mit runder Sichtscheibe (zwei Bernsteinaugen) und
    einem Zeigerkamm, ein Harnisch mit einem Zifferblatt auf der Brust, Beinschienen mit Zahnradschnalle und Kniesteinen,
    Stiefel mit Messingkappen und Bernstein an den Knöcheln.

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

#: der Kristall: warmes Bernstein, in den Tiefen rotbraun, in den Lichtern fast Weiß
CHRONIT = st.ramp("#e39a34", dark=0.3, light=0.97, cool=25, hue_dark=12, chroma_dark=1.25, warm=95, hue_light=12, chroma_light=0.35)
#: erstarrte Funken und Glanzlichter
SPARK = st.ramp("#fff0bf", dark=0.72, light=1.0, cool=70, warm=100, chroma_light=0.12)
#: Uhrbronze (die Platten der Rüstung)
BRONZE = st.ramp("#8c5d33", dark=0.17, light=0.84, cool=15, hue_dark=10, chroma_dark=1.15, warm=80, hue_light=10, chroma_light=0.5)
#: Säume und Zahnräder: Messing
TRIM = gear.BRASS
#: das Leuchten (Augen, Zeiger, Stundenmarken)
AMBER = st.ramp("#ffb63c", dark=0.45, light=1.0, cool=40, hue_dark=8, warm=95, chroma_light=0.3)

BLOCK_FRAMES = 16
#: der Lichtzeiger (schwach -> stark)
HAND = [hexc("#c8741c"), hexc("#ec9c34"), hexc("#ffc864"), hexc("#fff2c4")]


# =============================================================================
# Der Kristall
# =============================================================================

def _crystal(s: st.Spr, bx: float, by: float, height: float, width: float, tilt: float, tip: float, spark: bool) -> None:
    """Eine sechseckige Kristallspitze: Fuß bei (bx, by), nach oben (um tilt Grad nach rechts geneigt); drei sichtbare
    Flächen (links im Licht, Mitte, rechts im Schatten), oben eine Spitze aus zwei Facetten."""
    t = math.radians(tilt)
    ux, uy = math.sin(t), -math.cos(t)          # entlang der Achse (nach oben)
    vx, vy = math.cos(t), math.sin(t)           # quer (nach rechts)
    half = width / 2.0
    for y in range(N):
        for x in range(N):
            px, py = x + 0.5 - bx, y + 0.5 - by
            u = px * ux + py * uy
            v = px * vx + py * vy
            if u < 0.0 or u > height:
                continue
            hw = half if u < height - tip else half * (height - u) / tip + 0.2
            if abs(v) > hw:
                continue
            if u > height - tip:
                lv = ("D", 5) if v < -0.2 else ("A", 4) if v < 0.9 else ("A", 2)
            elif v < -half * 0.45:
                lv = ("A", 5)
            elif v < -half * 0.2:
                lv = ("D", 4)                    # der helle Grat zwischen Licht- und Mittelfläche
            elif v < half * 0.4:
                lv = ("A", 3)
            else:
                lv = ("A", 1) if v > half * 0.75 else ("A", 2)
            if u < 0.9:
                lv = ("A", max(1, lv[1] - 1)) if lv[0] == "A" else ("A", 3)   # der Fuß ist dunkler
            s.set(x, y, lv)
    if spark:
        for du, dv, lv in ((height * 0.45, -0.3, 6), (height * 0.62, 0.6, 5), (height * 0.3, 0.8, 4)):
            x = int(math.floor(bx + ux * du + vx * dv))
            y = int(math.floor(by + uy * du + vy * dv))
            if s.opaque(x, y):
                s.set(x, y, ("C", lv))


def chronit_gem() -> Image.Image:
    """Ein Chronitkristall: eine große, leicht geneigte Spitze, daneben eine kleine; innen erstarrte Funken."""
    s = st.Spr()
    _crystal(s, 11.2, 14.8, 7.2, 4.0, 30.0, 2.4, False)
    _crystal(s, 6.9, 15.2, 13.6, 6.2, -6.0, 3.9, True)
    return s.outline(prefer=("A", "D"), override={"D": "A", "C": "A"}).image({"A": CHRONIT, "D": SPARK, "C": SPARK})


# =============================================================================
# Der Block: ein geschliffenes Zifferblatt
# =============================================================================

C0 = 7.5
R_DIAL = 6.3


def _hour_marks() -> list[tuple[int, int, bool]]:
    """Die zwölf Stundenmarken auf der Scheibe: (x, y, Hauptstunde)."""
    out = []
    for h in range(12):
        a = h * math.pi / 6.0 - math.pi / 2.0
        r = 3.9
        out.append((int(math.floor(C0 + 0.5 + math.cos(a) * r)), int(math.floor(C0 + 0.5 + math.sin(a) * r)), h % 3 == 0))
    return out


def chronit_block() -> tuple[Image.Image, Image.Image]:
    """Der Block und seine Leuchtebene (Bildfolge: der Lichtzeiger läuft reihum)."""
    cloud = vs.layered(90311, ((8, 8, 0.55), (4, 4, 0.3), (2, 2, 0.15)))
    g = vs.grid(3)
    for y in range(N):
        for x in range(N):
            dx, dy = x + 0.5 - (C0 + 0.5), y + 0.5 - (C0 + 0.5)
            r = math.hypot(dx, dy)
            if x == 0 or y == 0:
                tone = 5 if not (x == N - 1 or y == N - 1) else 3        # der Rahmen oben/links im Licht
            elif x == N - 1 or y == N - 1:
                tone = 1                                                  # unten/rechts im Schatten
            elif r > R_DIAL + 0.4:
                # die Ecken: Facetten, schräg geschliffen (abwechselnd hell und dunkel)
                tone = 4 if (x - y) % 4 in (0, 1) else 3
                if dx > 0 and dy > 0:
                    tone -= 1
            elif r > R_DIAL - 0.7:
                tone = 6 if dx + dy < -2.0 else 5 if dx + dy < 2.0 else 3   # die Lünette: hell, gewölbt
            elif r > R_DIAL - 1.5:
                tone = 1                                                  # die dunkle Fuge unter der Lünette
            else:
                # das Zifferblatt: tief bernsteinbraun, zur Mitte etwas heller, ein wenig Wolke im Kristall
                tone = 2 if r > 2.6 else 3
                if cloud[y][x] > 0.78:
                    tone += 1
            g[y][x] = max(1, min(6, tone))
    for x, y, main in _hour_marks():
        g[y][x] = 6 if main else 5
    for x, y in ((7, 7), (8, 7), (7, 8), (8, 8)):
        g[y][x] = 6 if (x, y) in ((7, 7), (8, 8)) else 5               # die Nabe
    img = vs.paint(g, CHRONIT)
    for x, y in ((1, 1), (2, 1), (1, 2)):
        img.putpixel((x, y), pa.mix(img.getpixel((x, y)), hexc("#ffffff"), 0.5))

    frames = Image.new("RGBA", (N, N * BLOCK_FRAMES), CLEAR)
    marks = _hour_marks()
    for f in range(BLOCK_FRAMES):
        angle = f / BLOCK_FRAMES * math.tau - math.pi / 2.0
        for y in range(N):
            for x in range(N):
                dx, dy = x + 0.5 - (C0 + 0.5), y + 0.5 - (C0 + 0.5)
                r = math.hypot(dx, dy)
                if r > R_DIAL - 1.6 or r < 0.9:
                    continue
                a = math.atan2(dy, dx)
                behind = (angle - a) % math.tau            # wie weit hinter dem Zeiger (im Uhrzeigersinn)
                if behind < 0.2 or behind > math.tau - 0.1:
                    level = 3 if r < 3.4 else 2
                elif behind < 0.6:
                    level = 1
                elif behind < 1.1:
                    level = 0
                else:
                    continue
                frames.putpixel((x, f * N + y), HAND[level])
        for x, y, main in marks:
            frames.putpixel((x, f * N + y), HAND[3 if main else 2])
        for x, y in ((7, 7), (8, 7), (7, 8), (8, 8)):
            frames.putpixel((x, f * N + y), HAND[3 if (x, y) in ((7, 7), (8, 8)) else 2])
    return img, frames


# =============================================================================
# Die Rüstung der Zeitwächter
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


def timewarden_icons(put: Callable[[str, Image.Image], None]) -> None:
    ramps = {"A": BRONZE, "B": TRIM, "D": AMBER, "E": CHRONIT, "C": SPARK}

    # --- der Helm: geschlossen, eine runde Sichtscheibe in Messing mit zwei Bernsteinaugen, darüber ein Zeigerkamm
    s = icons.base(HELMET)
    ring = [(6, 6), (7, 5), (8, 5), (9, 6), (10, 7), (10, 8), (9, 9), (8, 10), (7, 10), (6, 9), (5, 8), (5, 7)]
    for x, y in ring:
        s.set(x, y, ("B", 5 if y < 8 else 3))
    for x, y in ((6, 7), (7, 6), (8, 6), (9, 7), (6, 8), (7, 9), (8, 9), (9, 8), (7, 7), (8, 7), (7, 8), (8, 8)):
        s.set(x, y, ("A", 1))                               # das dunkle Glas
    s.set(7, 7, ("D", 5))
    s.set(8, 7, ("D", 5))                                   # die Augen
    for x, y, lv in ((7, 2, 5), (8, 2, 4), (7, 1, 6), (7, 3, 4), (8, 3, 3), (7, 4, 5)):
        s.set(x, y, ("B", lv))                              # der Zeigerkamm (Spitze oben)
    for x in range(3, 13):
        if s.opaque(x, 10) and not (5 <= x <= 10):
            s.set(x, 10, ("B", 4 if x < 8 else 3))
    put("item/timewarden_helmet", s.outline(prefer=("A", "B"), override={"D": "A", "C": "A", "E": "A"}).image(ramps))

    # --- der Harnisch: Messing an den Schultern, auf der Brust ein Zifferblatt mit Zeigern und einer Bernsteinnabe
    s = icons.base(icons.CHESTPLATE)
    for x in range(2, 14):
        for y in (2, 3):
            if s.opaque(x, y) and (x <= 5 or x >= 10):
                s.set(x, y, ("B", 5 if y == 2 else 4))
    dial = [(6, 5), (7, 5), (8, 5), (9, 5), (5, 6), (10, 6), (5, 7), (10, 7), (5, 8), (10, 8), (5, 9), (10, 9), (6, 10), (7, 10),
            (8, 10), (9, 10)]
    for x, y in dial:
        s.set(x, y, ("B", 5 if y < 8 else 3))
    for x in range(6, 10):
        for y in range(6, 10):
            s.set(x, y, ("C", 3 if (x + y) % 2 else 4))       # das helle Zifferblatt
    s.set(7, 6, ("A", 1))
    s.set(7, 7, ("A", 1))                                   # der große Zeiger nach oben
    s.set(8, 8, ("A", 2))                                   # der kleine nach unten rechts
    s.set(8, 7, ("D", 6))                                   # die Nabe
    for x, y in ((7, 12), (8, 12)):
        s.set(x, y, ("E", 5))                               # das Pendelgewicht unten
    put("item/timewarden_chestplate", s.outline(prefer=("A", "B"), override={"D": "A", "C": "A", "E": "A"}).image(ramps))

    # --- die Beinschienen: Messinggürtel mit Zahnradschnalle (Bernstein in der Mitte), Kniesteine, Messingbänder
    s = icons.base(icons.LEGGINGS)
    for x in range(2, 14):
        s.set(x, 2, ("B", 5 if x < 8 else 4))
        s.set(x, 3, ("B", 3 if x not in (7, 8) else 4))
    s.set(7, 2, ("E", 6))
    s.set(8, 2, ("E", 4))
    for x0 in (2, 10):
        s.set(x0 + 1, 8, ("E", 5))
        s.set(x0 + 2, 8, ("D", 6))
        for x in range(x0, x0 + 4):
            if s.opaque(x, 11):
                s.set(x, 11, ("B", 3))
    put("item/timewarden_leggings", s.outline(prefer=("A", "B"), override={"D": "A", "C": "A", "E": "B"}).image(ramps))

    # --- die Stiefel: Messingkappen, an den Knöcheln kleine Zahnräder mit Bernstein
    s = icons.base(icons.BOOTS)
    for x in list(range(1, 6)) + list(range(10, 15)):
        if s.opaque(x, 11):
            s.set(x, 11, ("B", 4 if x < 8 else 3))
        if s.opaque(x, 10):
            s.set(x, 10, ("B", 5 if x < 8 else 4))
    for x0 in (3, 12):
        s.set(x0, 6, ("D", 6))
        s.set(x0 - 1, 6, ("B", 4))
        s.set(x0 + 1, 6, ("B", 3))
        s.set(x0, 5, ("B", 5))
        s.set(x0, 7, ("B", 3))
    put("item/timewarden_boots", s.outline(prefer=("A", "B"), override={"D": "A", "C": "A", "E": "A"}).image(ramps))


def build(put: Callable[[str, Image.Image], None]) -> None:
    block, glow = chronit_block()
    put("block/chronit_block", block)
    put("block/chronit_block_glow", glow)
    ANIMATIONS["block/chronit_block_glow"] = (BLOCK_FRAMES, 4, True)
    put("item/chronit", chronit_gem())
    timewarden_icons(put)


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda k, i: out.__setitem__(k, i))
    tiles = []
    plain = Image.new("RGBA", (N * 2, N * 2))
    for dx in (0, N):
        for dy in (0, N):
            plain.alpha_composite(out["block/chronit_block"], (dx, dy))
    tiles.append(plain)
    for f in (0, 4, 10):
        fr = out["block/chronit_block_glow"].crop((0, f * N, N, (f + 1) * N))
        big = plain.copy()
        for dx in (0, N):
            for dy in (0, N):
                big.alpha_composite(fr, (dx, dy))
        tiles.append(big)
    for key in ("item/chronit", "item/timewarden_helmet", "item/timewarden_chestplate", "item/timewarden_leggings", "item/timewarden_boots"):
        t = Image.new("RGBA", (N * 2, N * 2), (0, 0, 0, 0))
        t.alpha_composite(out[key].resize((N * 2, N * 2), Image.NEAREST))
        tiles.append(t)
    z = 5
    sheet = Image.new("RGBA", (len(tiles) * (N * 2 * z + 10), N * 2 * z), (52, 50, 62, 255))
    for i, t in enumerate(tiles):
        sheet.alpha_composite(t.resize((N * 2 * z, N * 2 * z), Image.NEAREST), (i * (N * 2 * z + 10), 0))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "chronit.png")
