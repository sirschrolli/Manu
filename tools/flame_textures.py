#!/usr/bin/env python3
"""
Feueropal (seit 1.1.6): der Edelstein der Unterwelt, sein Block und die Symbole der Glutwandler-Rüstung.

  * Feueropal – ein glatt geschliffener, ovaler Stein (schräg gestellt): durchscheinend, am Rand tiefrot, nach innen
    orange bis goldgelb, oben links ein heller Glanz; unten rechts glüht das Licht, das hineinfällt, im Stein nach. Darin
    das Farbenspiel des Opals: kleine grüne, türkise und violette Blitze.
  * Feueropalblock – geschliffene Feueropale, wie Fliesen in Schwarzgold gefasst (zwei große, zwei kleine): in jedem wogt
    Feuer, dazwischen blitzt das Farbenspiel. In der Leuchtebene steigen Flammenzungen langsam durch die Steine.
  * Die Glutwandler-Rüstung: Platten aus geschwärztem Stahl mit glühenden Rissen wie erkaltende Lava, Säume aus
    Schwarzgold, Feueropale an Stirn, Brust, Knien und Knöcheln.

Wird von tools/generate_textures.py aufgerufen: build(put); Bildfolgen stehen in ANIMATIONS.
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import item_style as st
import items_armor as icons
import pixelart as pa
from pixelart import hexc

N = 16
CLEAR = (0, 0, 0, 0)
ANIMATIONS: dict[str, tuple[int, int, bool]] = {}

#: der Stein: am Rand tiefrot, innen orange, im Licht goldgelb bis fast weiß
OPAL = st.ramp("#f26a1a", dark=0.3, light=0.93, cool=22, hue_dark=28, chroma_dark=1.15, warm=96, hue_light=40, chroma_light=0.72)
#: das Farbenspiel: grüne, türkise und violette Blitze
FLASH_GREEN = st.ramp("#3ed67c", dark=0.42, light=0.98, cool=160, warm=120, chroma_light=0.4)
FLASH_TEAL = st.ramp("#2fc6dc", dark=0.42, light=0.98, cool=240, warm=190, chroma_light=0.4)
FLASH_VIOLET = st.ramp("#b25cf0", dark=0.4, light=0.97, cool=290, warm=330, chroma_light=0.4)
#: Glanzlicht
SHINE = st.fixed("#fff2c8", "#fff6dc", "#fffaee", "#ffffff", "#ffffff", "#ffffff", "#ffffff")
#: geschwärzter Stahl (die Platten der Rüstung) und Schwarzgold (Säume, Fassungen)
BLACKSTEEL = st.ramp("#3a3036", dark=0.12, light=0.7, cool=280, hue_dark=10, warm=40, hue_light=10, chroma_light=0.6)
DARK_GOLD = st.ramp("#b8862e", dark=0.22, light=0.93, cool=30, hue_dark=12, chroma_dark=1.1, warm=90, hue_light=10, chroma_light=0.5)
#: das Glühen in den Rissen
EMBER = st.ramp("#ff7a1e", dark=0.42, light=0.98, cool=25, hue_dark=20, warm=95, hue_light=30, chroma_light=0.35)

BLOCK_FRAMES = 16


# =============================================================================
# Der Stein
# =============================================================================

def _oval(cx: float, cy: float, rx: float, ry: float, angle: float) -> set[tuple[int, int]]:
    """Pixel in einer gedrehten Ellipse (Winkel in Grad, gegen den Uhrzeigersinn auf dem Bild)."""
    a = math.radians(angle)
    ca, sa = math.cos(a), math.sin(a)
    out = set()
    for y in range(N):
        for x in range(N):
            px, py = x + 0.5 - cx, y + 0.5 - cy
            u = px * ca - py * sa
            v = px * sa + py * ca
            if (u / rx) ** 2 + (v / ry) ** 2 <= 1.0:
                out.add((x, y))
    return out


def _edge_distance(mask: set[tuple[int, int]]) -> dict[tuple[int, int], int]:
    """Abstand jedes Pixels zum Rand der Form (1 = am Rand)."""
    out = {}
    frontier = [p for p in mask if any((p[0] + dx, p[1] + dy) not in mask for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))]
    for p in frontier:
        out[p] = 1
    d = 1
    while frontier:
        nxt = []
        for x, y in frontier:
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                q = (x + dx, y + dy)
                if q in mask and q not in out:
                    out[q] = d + 1
                    nxt.append(q)
        frontier = nxt
        d += 1
    return out


LIGHT = (-0.52, -0.62, 0.59)


def _cabochon(u: float, v: float) -> tuple[float, float, float]:
    """Ein gewölbter Stein in Ellipsen-Koordinaten (u, v in −1…1): (Licht, Glanz, Glühen im Innern)."""
    r2 = min(1.0, u * u + v * v)
    nz = math.sqrt(max(0.0, 1.0 - r2)) * 1.25            # etwas flacher als eine Halbkugel
    nx, ny = u, v
    nn = math.sqrt(nx * nx + ny * ny + nz * nz)
    nx, ny, nz = nx / nn, ny / nn, nz / nn
    lx, ly, lz = LIGHT
    ln = math.sqrt(lx * lx + ly * ly + lz * lz)
    lx, ly, lz = lx / ln, ly / ln, lz / ln
    diffuse = max(0.0, nx * lx + ny * ly + nz * lz)
    hx, hy, hz = lx, ly, lz + 1.0
    hn = math.sqrt(hx * hx + hy * hy + hz * hz)
    spec = max(0.0, (nx * hx + ny * hy + nz * hz) / hn) ** 40
    # durchscheinend: das Licht, das oben links hineinfällt, sammelt sich unten rechts im Innern
    glow = math.exp(-((u - 0.28) ** 2 + (v - 0.34) ** 2) / 0.16)
    return diffuse, spec, glow


def _ellipse_uv(x: int, y: int, cx: float, cy: float, rx: float, ry: float, angle: float) -> tuple[float, float]:
    a = math.radians(angle)
    px, py = x + 0.5 - cx, y + 0.5 - cy
    return (px * math.cos(a) - py * math.sin(a)) / rx, (px * math.sin(a) + py * math.cos(a)) / ry


#: der Feueropal, von Hand schattiert: 1..6 = Stein (dunkel -> hell), W/w = Glanz, G/g = grüner Blitz, T/t = türkis, V = violett
OPAL_ART = """
................
................
................
.......34443....
.....34455543...
....34WW554443..
...34Ww554T432..
..345w54444332..
..344544445432..
..344gG4V5542...
..23gG4455532...
..2234455432....
...2233322......
................
................
................
"""


def opal_gem_art() -> Image.Image:
    """Der Feueropal nach der Zeichnung: gewölbt, oben links der Glanz, unten rechts das Glühen im Innern."""
    legend = {str(i): ("O", i) for i in range(7)}
    legend.update({"W": ("W", 0), "w": ("W", 3), "G": ("G", 5), "g": ("G", 3), "T": ("T", 5), "t": ("T", 3), "V": ("V", 4)})
    s = st.Spr.art(OPAL_ART, legend)
    s = s.outline(prefer=("O",), override={"G": "O", "T": "O", "V": "O", "W": "O"}, light=1, dark=0)
    return s.image({"O": OPAL, "G": FLASH_GREEN, "T": FLASH_TEAL, "V": FLASH_VIOLET, "W": SHINE})


# =============================================================================
# Der Block: Feueropale in Schwarzgold gefasst
# =============================================================================

#: das Feld des Steins im Block (einschließlich): ringsum ein Rahmen aus Schwarzgold, zwei Pixel breit
PANEL = (2, 2, 13, 13)
#: das Farbenspiel im Block: kurze Blitze (x, y, Slot)
BLOCK_FLASHES = ((5, 6, "G"), (6, 5, "G"), (10, 4, "T"), (11, 4, "T"), (9, 10, "V"), (5, 11, "T"), (6, 11, "T"), (12, 8, "G"),
                 (12, 9, "G"))


def _fire(x: float, y: float, phase: float) -> float:
    """Wogendes Feuer im Stein (0..1): geschwungene Bänder schräg von unten links nach oben rechts."""
    w = math.sin(x * 0.55 + y * 0.8 + phase + 1.3 * math.sin(y * 0.38 - x * 0.24 + 0.6))
    w += 0.4 * math.sin(x * -0.36 + y * 0.6 - phase * 0.5 + 2.0)
    return max(0.0, min(1.0, 0.5 + 0.36 * w))


def opal_block() -> tuple[Image.Image, Image.Image]:
    """Der Block und seine Leuchtebene (Bildfolge: das Feuer wogt langsam durch den Stein)."""
    x0, y0, x1, y1 = PANEL
    s = st.Spr()
    for y in range(N):
        for x in range(N):
            inside = x0 <= x <= x1 and y0 <= y <= y1
            if not inside:
                # der Rahmen: Schwarzgold, abgeschrägt – außen oben/links im Licht, innen unten/rechts im Licht
                outer = x == 0 or y == 0 or x == N - 1 or y == N - 1
                if outer:
                    tone = 4 if (x == 0 or y == 0) and not (x == N - 1 or y == N - 1) else 1
                else:
                    tone = 2 if (x == 1 or y == 1) and not (x == N - 2 or y == N - 2) else 4
                if (x, y) in ((1, 1), (N - 2, 1), (1, N - 2), (N - 2, N - 2)):
                    tone = 5                                 # Nieten in den Ecken
                s.set(x, y, ("B", tone))
                continue
            # der Stein: gewölbt geschliffen, darin wogendes Feuer
            u = (x + 0.5 - (x0 + x1 + 1) / 2) / ((x1 - x0 + 1) / 2)
            v = (y + 0.5 - (y0 + y1 + 1) / 2) / ((y1 - y0 + 1) / 2)
            diffuse, spec, glow = _cabochon(u * 0.82, v * 0.82)
            f = _fire(x, y, 0.0)
            lvl = 0.9 + diffuse * 1.3 + f * 3.3
            if x == x0 or y == y0:
                lvl -= 0.8                                   # die Fuge oben/links im Schatten des Rahmens
            s.set(x, y, ("O", st.clamp(int(round(lvl)), 1, 6)))
    for x, y, slot in BLOCK_FLASHES:
        s.set(x, y, (slot, 4))
    for x, y, lvl in ((4, 3, 2), (3, 4, 2), (5, 3, 4)):
        s.set(x, y, ("W", lvl))                              # der Glanz oben links
    img = s.image({"O": OPAL, "B": DARK_GOLD, "G": FLASH_GREEN, "T": FLASH_TEAL, "V": FLASH_VIOLET, "W": SHINE})

    frames = Image.new("RGBA", (N, N * BLOCK_FRAMES), CLEAR)
    for fr in range(BLOCK_FRAMES):
        phase = fr / BLOCK_FRAMES * math.tau
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                # die hellen Bänder wogen (gleiche Form, eine Umdrehung je Bildfolge)
                f = _fire(x, y, phase)
                if f < 0.68:
                    continue
                k = min(1.0, (f - 0.68) / 0.22)
                col = OPAL[5] if k > 0.6 else OPAL[4]
                frames.putpixel((x, fr * N + y), (col[0], col[1], col[2], int(110 + 100 * k)))
        for i, (x, y, slot) in enumerate(BLOCK_FLASHES):
            # das Farbenspiel blitzt reihum auf
            if (fr - i * 2) % BLOCK_FRAMES < 3:
                ramp = {"G": FLASH_GREEN, "T": FLASH_TEAL, "V": FLASH_VIOLET}[slot]
                frames.putpixel((x, fr * N + y), ramp[5])
    return img, frames


# =============================================================================
# Die Glutkruste: erstarrte Lava unter den Stiefeln der Glutwandler
# =============================================================================

#: die Kruste: fast schwarzer, warmer Basalt
CRUST = st.ramp("#3a2a26", dark=0.13, light=0.55, cool=300, hue_dark=10, warm=40, hue_light=10, chroma_light=0.8)
#: Keimpunkte der Schollen (kachelbar)
CRUST_SEEDS = ((3.0, 5.5), (11.0, 3.0), (7.0, 10.5), (14.5, 8.5), (2.0, 14.0), (11.5, 14.5), (6.5, 1.0))


def _plates(x: float, y: float) -> tuple[float, float, int]:
    """Abstand zur nächsten und zweitnächsten Scholle (kachelbar) und die Nummer der nächsten."""
    best = [(1e9, -1), (1e9, -1)]
    for i, (sx, sy) in enumerate(CRUST_SEEDS):
        for ox in (-N, 0, N):
            for oy in (-N, 0, N):
                d = math.hypot(x - sx - ox, y - sy - oy)
                if d < best[0][0]:
                    best[1] = best[0]
                    best[0] = (d, i)
                elif d < best[1][0] and i != best[0][1]:
                    best[1] = (d, i)
    return best[0][0], best[1][0], best[0][1]


def lava_crust(age: int) -> tuple[Image.Image, Image.Image]:
    """Die Kruste in Stufe {@code age} (0–3) und ihre Leuchtebene: Schollen aus Basalt, dazwischen glühende Risse, die mit
    jeder Stufe breiter und heller werden."""
    width = (0.5, 0.75, 1.05, 1.4)[age]
    img = Image.new("RGBA", (N, N), CLEAR)
    glow = Image.new("RGBA", (N, N), CLEAR)
    for y in range(N):
        for x in range(N):
            d1, d2, plate = _plates(x + 0.5, y + 0.5)
            edge = d2 - d1
            if edge < width:
                # ein Riss: innen am hellsten
                k = 1.0 - edge / width
                lv = min(5, 1 + age + int(k * 1.7))
                col = EMBER[lv]
                img.putpixel((x, y), col)
                if lv >= 2:
                    glow.putpixel((x, y), (col[0], col[1], col[2], 120 + 35 * age))
                continue
            # eine Scholle: zur Mitte hin etwas heller, oben links ein Lichtrand, Körnung
            lv = 3 if d1 < 2.2 else 2
            if edge < width + 0.8:
                lv = 4 if (x + y) % 2 == 0 and d1 > d2 * 0.4 else lv
            n = st.noise(41 + plate, x, y)
            if n > 0.55:
                lv += 1
            elif n < -0.6:
                lv -= 1
            img.putpixel((x, y), CRUST[st.clamp(lv, 0, 6)])
    return img, glow


# =============================================================================
# Die Glutwandler-Rüstung
# =============================================================================

def emberwalker_icons(put: Callable[[str, Image.Image], None]) -> None:
    ramps = {"A": BLACKSTEEL, "B": DARK_GOLD, "D": EMBER, "E": OPAL, "C": SHINE}
    override = {"D": "A", "C": "A", "E": "A"}

    # --- der Helm: geschlossen, ein T-Sehschlitz voller Glut, ein Feueropal über der Stirn, oben ein Kamm aus Flammen
    s = icons.base(icons.CLOSED_HELMET)
    for x in range(4, 12):
        s.set(x, 7, ("D", 5 if 5 <= x <= 10 else 3))            # der Sehschlitz
    for y in (8, 9):
        s.set(7, y, ("D", 4))
        s.set(8, y, ("D", 3))
    for x in range(3, 13):
        if s.opaque(x, 5):
            s.set(x, 5, ("B", 5 if x < 8 else 4))               # der goldene Stirnreif
    s.set(7, 5, ("E", 6))
    s.set(8, 5, ("E", 4))                                       # der Feueropal
    s.set(7, 4, ("E", 5))
    s.set(8, 4, ("E", 3))
    for x, y, lv in ((8, 0, 2), (7, 1, 4), (8, 1, 5), (6, 2, 3), (7, 2, 6), (8, 2, 5), (9, 2, 3), (7, 3, 5), (8, 3, 4)):
        s.set(x, y, ("D", lv))                                  # der Flammenkamm
    for x, y in ((4, 9), (5, 10), (11, 9), (10, 10)):
        s.set(x, y, ("D", 2))                                   # Glutrisse an den Wangen
    put("item/emberwalker_helmet", s.outline(prefer=("A", "B"), override=override).image(ramps))

    # --- der Harnisch: Schultern in Schwarzgold, auf der Brust ein großer Feueropal, von dem Glutrisse ausgehen
    s = icons.base(icons.CHESTPLATE)
    for x in range(2, 14):
        for y in (2, 3):
            if s.opaque(x, y) and (x <= 5 or x >= 10):
                s.set(x, y, ("B", 5 if y == 2 else 3))
    for x, y, lv in ((7, 6, 6), (8, 6, 5), (7, 7, 5), (8, 7, 3)):
        s.set(x, y, ("E", lv))
    for x, y in ((6, 6), (9, 6), (6, 7), (9, 7), (7, 5), (8, 5), (7, 8), (8, 8)):
        s.set(x, y, ("B", 4 if y <= 6 else 2))                  # die Fassung
    for x, y, lv in ((5, 9, 3), (4, 10, 2), (10, 9, 3), (11, 10, 2), (7, 10, 3), (8, 11, 2), (6, 12, 2)):
        if s.opaque(x, y):
            s.set(x, y, ("D", lv))                              # Glutrisse
    for x in range(5, 11):
        if s.opaque(x, 13):
            s.set(x, 13, ("B", 3))
    put("item/emberwalker_chestplate", s.outline(prefer=("A", "B"), override=override).image(ramps))

    # --- die Beinschienen: ein goldener Gürtel mit Opalschnalle, Feueropale an den Knien, Glutrisse an den Schienbeinen
    s = icons.base(icons.LEGGINGS)
    for x in range(2, 14):
        s.set(x, 2, ("B", 5 if x < 8 else 4))
        s.set(x, 3, ("B", 3))
    s.set(7, 2, ("E", 6))
    s.set(8, 2, ("E", 4))
    s.set(7, 3, ("E", 4))
    s.set(8, 3, ("E", 2))
    for x0 in (2, 10):
        s.set(x0 + 1, 8, ("E", 6))
        s.set(x0 + 2, 8, ("E", 4))
        s.set(x0 + 1, 7, ("B", 4))
        s.set(x0 + 2, 7, ("B", 4))
        s.set(x0 + 1, 10, ("D", 3))
        s.set(x0 + 2, 11, ("D", 2))
    put("item/emberwalker_leggings", s.outline(prefer=("A", "B"), override=override).image(ramps))

    # --- die Stiefel: Sohlen voller Glut, ein goldenes Band, Feueropale an den Knöcheln
    s = icons.base(icons.BOOTS)
    for x in list(range(1, 6)) + list(range(10, 15)):
        if s.opaque(x, 11):
            s.set(x, 11, ("D", 5 if x in (2, 3, 11, 12) else 4))
        if s.opaque(x, 9):
            s.set(x, 9, ("B", 5 if x < 8 else 4))
    for x0 in (4, 11):
        s.set(x0, 6, ("E", 6))
        s.set(x0 - 1, 6, ("E", 4))
        s.set(x0, 7, ("D", 3))
    put("item/emberwalker_boots", s.outline(prefer=("A", "B"), override=override).image(ramps))


# =============================================================================
# Ausgabe
# =============================================================================

# =============================================================================
# Das Flammenheiligtum (seit 1.2): der Flammenaltar und die Flammenpeitsche
# =============================================================================

#: polierter Schwarzstein des Altars (dunkles Grau mit einem Hauch Violett)
ALTAR_STONE = st.ramp("#3c3442", dark=0.13, light=0.64, cool=290, hue_dark=10, warm=40, hue_light=8, chroma_light=0.7)
#: Tempelgold (heller und wärmer als das Schwarzgold der Rüstung)
TEMPLE_GOLD = st.ramp("#dca23a", dark=0.3, light=0.96, cool=30, hue_dark=14, chroma_dark=1.1, warm=95, hue_light=12,
                      chroma_light=0.5)
#: kalte Asche im erloschenen Becken
ASH = st.ramp("#625a58", dark=0.15, light=0.8, cool=280, hue_dark=8, warm=60, hue_light=6, chroma_light=0.6)
#: die Peitschenschnur: karminrotes, geflochtenes Leder; der Griff: dunkles Leder
LASH_LEATHER = st.ramp("#962a1c", dark=0.17, light=0.8, cool=10, hue_dark=10, warm=60, hue_light=20, chroma_light=0.6)
GRIP_LEATHER = st.ramp("#4e2c20", dark=0.12, light=0.7, cool=20, hue_dark=8, warm=60, hue_light=10, chroma_light=0.6)
#: die Flammen des Altars (von dunkelrot bis fast weiß)
FIRE = st.fixed("#6e1206", "#b02a0a", "#e8480e", "#ff7a1a", "#ffa630", "#ffd46a", "#fff4c8")
FIRE_FRAMES = 32


def _altar_texture(fn) -> Image.Image:
    s = st.Spr()
    for y in range(N):
        for x in range(N):
            px = fn(x, y)
            if px is not None:
                s.set(x, y, px)
    return s.image({"S": ALTAR_STONE, "G": TEMPLE_GOLD, "O": OPAL, "A": ASH, "E": EMBER, "F": FLASH_GREEN, "T": FLASH_TEAL})


#: das Flammenzeichen auf den Seiten des Schafts (die Fläche dort ist 10 x 6 Pixel: u 3–13, v 5–11)
ALTAR_SIGIL = {
    (8, 5): 5, (7, 6): 4, (8, 6): 5, (9, 6): 3, (7, 7): 4, (8, 7): 2, (9, 7): 4, (6, 8): 4, (7, 8): 2, (8, 8): 1, (9, 8): 3,
    (10, 8): 3, (6, 9): 4, (7, 9): 2, (8, 9): 1, (9, 9): 2, (10, 9): 2, (7, 10): 3, (8, 10): 3, (9, 10): 2,
}


def altar_stone() -> Image.Image:
    """Polierter Schwarzstein für den Schaft: feine, wolkige Maserung, oben eine helle, unten eine dunkle Kante; in der Mitte
    ein eingelegtes Flammenzeichen aus Tempelgold, darin ein glimmender Feueropal."""
    def fn(x, y):
        if (x, y) in ALTAR_SIGIL:
            return ("G", ALTAR_SIGIL[(x, y)])
        if (x, y) in ((8, 8), (8, 9)):
            return ("O", 4)
        n = st.smooth_noise(71, x, y, 3.0)
        lv = 2 + (1 if n > 0.3 else 0) - (1 if n < -0.5 else 0)
        if y == 5:
            lv += 1
        if y == 10 and (x, y) not in ALTAR_SIGIL:
            lv -= 1
        if x in (3, 12):
            lv += 1 if x == 3 else -1
        return ("S", st.clamp(lv, 0, 6))
    img = _altar_texture(fn)
    # der Opal im Zeichen: ein grüner Blitz
    img.putpixel((8, 8), OPAL[5])
    img.putpixel((8, 9), FLASH_GREEN[4])
    return img


def altar_base() -> Image.Image:
    """Der Sockel: Ziegel aus poliertem Schwarzstein (je vier Pixel hoch, versetzt), mit feinen, dunklen Fugen; die Seiten
    des Sockels zeigen die unteren drei Reihen (eine helle Kante oben)."""
    def fn(x, y):
        row = y // 4
        joint_x = (x + (4 if row % 2 else 0)) % 8 == 7
        if y % 4 == 3 or joint_x:
            return ("S", 0)
        lv = 2
        if y % 4 == 0:
            lv = 3                                 # die obere Kante jedes Ziegels im Licht
        n = st.noise(83, x, y)
        if n > 0.6:
            lv += 1
        elif n < -0.65:
            lv -= 1
        if y == 13:
            lv = 4                                 # die Oberkante des Sockels
        return ("S", st.clamp(lv, 0, 6))
    return _altar_texture(fn)


def altar_gold() -> Image.Image:
    """Tempelgold: poliert, mit einer eingravierten Flammenwelle; oben eine helle, unten eine dunkle Kante (für Stufe, Kragen,
    Becken und Rand – jede Reihe ist für sich ein Band)."""
    def fn(x, y):
        lv = 4
        if y == 0:
            lv = 5
        elif y in (1, 2, 3):
            wave = round(2 + math.sin((x + 0.5) * math.tau / 8) * 1.0)
            lv = 2 if y == wave else (5 if y == 1 else 4)
            if y == 3:
                lv = 3
        else:
            lv = 4 if (x + y) % 5 else 5
            if y % 4 == 3:
                lv = 3
        return ("G", lv)
    return _altar_texture(fn)


def altar_coals(hot: bool) -> Image.Image:
    """Das Becken von oben: brennt das Feuer, liegt dort Glut (Schollen mit hellen Rissen); erloschen nur Asche mit ein paar
    schwarzen Kohlen."""
    def fn(x, y):
        d1, d2, plate = _plates(x + 0.5, y + 0.5)
        edge = d2 - d1
        if hot:
            if edge < 0.9:
                return ("E", 5 if edge < 0.45 else 4)
            n = st.noise(97 + plate, x, y)
            return ("E", 2 if n < 0.3 else 3)
        if edge < 0.7:
            return ("A", 1)
        n = st.noise(101 + plate, x, y)
        return ("A", 3 if n > 0.4 else (2 if n > -0.5 else 4))
    return _altar_texture(fn)


def altar_opal() -> Image.Image:
    """Der Feueropal vorn am Schaft (4 x 4, bei u 6–10, v 6–10), drumherum Tempelgold."""
    art = {
        (6, 6): 3, (7, 6): 5, (8, 6): 5, (9, 6): 4,
        (6, 7): 4, (7, 7): 6, (8, 7): 5, (9, 7): 4,
        (6, 8): 4, (7, 8): 5, (8, 8): 4, (9, 8): 3,
        (6, 9): 3, (7, 9): 4, (8, 9): 3, (9, 9): 2,
    }

    def fn(x, y):
        if (x, y) in art:
            return ("O", art[(x, y)])
        if 5 <= x <= 10 and 5 <= y <= 10:
            return ("G", 5 if x == 5 or y == 5 else 3 if x == 10 or y == 10 else 4)
        return ("G", 4)
    img = _altar_texture(fn)
    img.putpixel((7, 7), SHINE[2])
    img.putpixel((8, 8), FLASH_GREEN[4])
    img.putpixel((9, 7), FLASH_TEAL[4])
    return img


def altar_fire() -> Image.Image:
    """Die Flammen über dem Altar (Bildfolge, 32 Bilder): ein Feuer, das sich von unten nährt – jede Zelle steigt auf, kühlt
    ein wenig ab und weht zufällig zur Seite; in der Mitte brennt es am höchsten. Wo es zu kalt ist, bleibt das Bild leer."""
    import random
    rnd = random.Random(1201)
    w, h = N, N + 4
    fire = [[0.0] * w for _ in range(h)]
    hottest = 36.0

    def source():
        for x in range(w):
            cx = abs(x + 0.5 - w / 2) / (w / 2)
            fire[h - 1][x] = hottest * max(0.0, 1.0 - cx ** 1.6) * (0.85 + 0.15 * rnd.random())

    def step():
        source()
        for y in range(1, h):
            for x in range(w):
                r = rnd.randint(0, 2)
                dst = min(w - 1, max(0, x + r - 1))
                edge = abs(dst + 0.5 - w / 2) / (w / 2)
                cool = rnd.random() * (1.5 + 3.0 * edge ** 1.5) + 0.6
                fire[y - 1][dst] = max(0.0, fire[y][x] - cool)

    for _ in range(120):
        step()
    out = Image.new("RGBA", (N, N * FIRE_FRAMES), CLEAR)
    for fr in range(FIRE_FRAMES):
        step()
        cells = {}
        for y in range(N):
            for x in range(N):
                v = fire[y + 4][x] / hottest
                if v >= 0.14:
                    cells[(x, y)] = min(6, int((v - 0.14) / 0.86 * 7.0))
        for (x, y), lv in cells.items():
            # einzelne Funken ohne Nachbarn fallen weg (sonst flimmert es)
            if sum((x + dx, y + dy) in cells for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1))) < 2:
                continue
            out.putpixel((x, fr * N + y), FIRE[lv])
    return out


# --- die Flammenpeitsche ------------------------------------------------------

def _whip_lash_points() -> list[tuple[float, float]]:
    """Die Schnur im Bild: vom Griff (unten links) in einem weiten Bogen nach oben und zur Spitze oben rechts."""
    pts = []
    p0, p1, p2, p3 = (6.2, 9.6), (2.0, 2.0), (12.0, 11.0), (14.2, 1.6)
    for i in range(120):
        t = i / 119
        a = (1 - t) ** 3
        b = 3 * (1 - t) ** 2 * t
        c = 3 * (1 - t) * t ** 2
        d = t ** 3
        pts.append((a * p0[0] + b * p1[0] + c * p2[0] + d * p3[0], a * p0[1] + b * p1[1] + c * p2[1] + d * p3[1]))
    return pts


def flame_whip_icon() -> Image.Image:
    """Die Flammenpeitsche im Inventar: unten links der Griff (Feueropal als Knauf, dunkles Leder, Goldringe); die karminrote
    Schnur schwingt in einer Welle nach oben rechts – erst nach links hinauf, über einen Bogen und wieder hinauf zur Spitze,
    die glüht und brennt."""
    s = st.Spr()
    lash = [(6, 7), (5, 6), (4, 5), (4, 4), (4, 3), (5, 2), (6, 2), (7, 2), (8, 2), (9, 3), (10, 4), (10, 5), (11, 6), (12, 6),
            (13, 5), (13, 4), (14, 3), (14, 2)]
    for i, (x, y) in enumerate(lash):
        if i >= len(lash) - 4:
            s.set(x, y, ("E", 2 + (i - (len(lash) - 4))))          # das Ende glüht
        else:
            s.set(x, y, ("L", 4 if y <= 3 or x <= 4 else 3))       # oben und links im Licht
    for (x, y, lv) in ((15, 1, 6), (15, 0, 5), (14, 1, 5), (15, 2, 4)):
        s.set(x, y, ("F", lv))                                     # die Flamme an der Spitze
    for (x, y, lv) in ((5, 9, 3), (4, 10, 3), (3, 11, 3), (5, 10, 1), (4, 11, 1), (3, 12, 1)):
        s.set(x, y, ("H", lv))                                     # die Lederwicklung des Griffs
    for (x, y, lv) in ((6, 8, 5), (6, 9, 3), (5, 8, 4), (2, 12, 4), (2, 11, 5)):
        s.set(x, y, ("G", lv))                                     # Goldringe oben und unten
    for (x, y, lv) in ((1, 13, 5), (2, 13, 4), (1, 14, 3), (2, 14, 2)):
        s.set(x, y, ("O", lv))                                     # der Feueropal als Knauf
    s = s.outline(prefer=("G", "H", "L", "O", "E"), override={"E": "L", "F": "L", "O": "G"}, light=0, dark=0)
    img = s.image({"L": LASH_LEATHER, "H": GRIP_LEATHER, "G": TEMPLE_GOLD, "O": OPAL, "E": EMBER, "F": FIRE})
    img.putpixel((1, 13), SHINE[3])
    return img


def flame_whip_model() -> tuple[Image.Image, Image.Image]:
    """Die Textur des 3D-Modells (16 x 16) und die seiner leuchtenden Teile:
      Gold (u 0–2, v 0–2), Lederwicklung des Griffs (u 2–4, v 0–6), die geflochtene Schnur (u 4–5, v 0–16, nach oben heller);
      leuchtend: der Opalknauf (u 0–2, v 0–2), die glühende Schnur (u 4–5, v 0–16), die Flamme (u 6–8, v 0–4)."""
    base = Image.new("RGBA", (N, N), CLEAR)
    glow = Image.new("RGBA", (N, N), CLEAR)
    for y in range(2):
        for x in range(2):
            base.putpixel((x, y), TEMPLE_GOLD[5 if (x + y) == 0 else 4])
            glow.putpixel((x, y), OPAL[5 if (x, y) == (0, 0) else 4])
    glow.putpixel((1, 1), FLASH_GREEN[4])
    for y in range(6):
        for x in range(2, 4):
            base.putpixel((x, y), GRIP_LEATHER[3 if (x + y) % 3 else 1])
    for y in range(N):
        base.putpixel((4, y), LASH_LEATHER[2 + (y % 3 == 0) + (1 if y > 8 else 0)])
        glow.putpixel((4, y), EMBER[3 + min(2, y // 6)])
    for y in range(4):
        for x in range(6, 8):
            glow.putpixel((x, y), FIRE[6 - y] if x == 6 else FIRE[5 - y])
    return base, glow


def build(put: Callable[[str, Image.Image], None]) -> None:
    block, glow = opal_block()
    put("block/fire_opal_block", block)
    put("block/fire_opal_block_glow", glow)
    ANIMATIONS["block/fire_opal_block_glow"] = (BLOCK_FRAMES, 3, True)
    put("item/fire_opal", opal_gem_art())
    for age in range(4):
        crust, crust_glow = lava_crust(age)
        put(f"block/lava_crust_{age}", crust)
        put(f"block/lava_crust_{age}_glow", crust_glow)
    emberwalker_icons(put)
    # das Flammenheiligtum (1.2)
    put("block/flame_altar_stone", altar_stone())
    put("block/flame_altar_base", altar_base())
    put("block/flame_altar_gold", altar_gold())
    put("block/flame_altar_coals", altar_coals(True))
    put("block/flame_altar_ash", altar_coals(False))
    put("block/flame_altar_opal", altar_opal())
    put("block/flame_altar_fire", altar_fire())
    ANIMATIONS["block/flame_altar_fire"] = (FIRE_FRAMES, 1, False)
    put("item/flame_whip", flame_whip_icon())
    whip, whip_glow = flame_whip_model()
    put("item/flame_whip_model", whip)
    put("item/flame_whip_model_glow", whip_glow)


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda k, i: out.__setitem__(k, i))
    tiles = []
    plain = Image.new("RGBA", (N * 2, N * 2))
    for dx in (0, N):
        for dy in (0, N):
            plain.alpha_composite(out["block/fire_opal_block"], (dx, dy))
    tiles.append(plain)
    for f in (0, 5, 10):
        fr = out["block/fire_opal_block_glow"].crop((0, f * N, N, (f + 1) * N))
        big = plain.copy()
        for dx in (0, N):
            for dy in (0, N):
                big.alpha_composite(fr, (dx, dy))
        tiles.append(big)
    for key in sorted(k for k in out if k.startswith("item/")):
        t = Image.new("RGBA", (N * 2, N * 2), (0, 0, 0, 0))
        t.alpha_composite(out[key].resize((N * 2, N * 2), Image.NEAREST))
        tiles.append(t)
    z = 5
    sheet = Image.new("RGBA", (len(tiles) * (N * 2 * z + 10), N * 2 * z), (52, 50, 62, 255))
    for i, t in enumerate(tiles):
        sheet.alpha_composite(t.resize((N * 2 * z, N * 2 * z), Image.NEAREST), (i * (N * 2 * z + 10), 0))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "fire_opal.png")
