#!/usr/bin/env python3
"""
Texturen zu Anni dem Troll (0.67), im Vanilla-Stil (siehe tools/item_style.py):

  * Annis Haut als fehlende Textur (für die Ticks, in denen er nur noch Magenta und Schwarz ist), der Ladekreis über seinem
    Kopf, das Zeilen-Abbild beim Rückruck, ein Pixel des Pixelregens, ein Feld des Fehlerfelds und die Zeile des
    Zeilenfehlers;
  * die Fehlerpixel (Partikel);
  * der Fehlerblock (fehlende Textur), das Bildrauschen (bewegt) und der Fehlerkern: ein zerbrochener Würfel mit einem
    flimmernden Kern aus Bildrauschen in Rot, Grün und Blau (wartend, laufend und erloschen);
  * der Bannhammer: Bild im Inventar und Atlas des 3D-Modells in der Hand (Baumstamm-Stiel mit Lappen, Steinklotz mit
    Eisenbändern, vorn fehlende Textur, schwebende Pixel);
  * die Fehlerflügel: Bild im Inventar (heil und zerrissen) und die getragenen Flügel – fehlende Textur mit Löchern und
    Farbsäumen;
  * der Fehlersplitter: ein gezackter Splitter, links magenta, rechts cyan.
"""
from __future__ import annotations

import math
import random
from pathlib import Path
from typing import Callable

from PIL import Image

import item_style as st

CLEAR = (0, 0, 0, 0)
MAGENTA = (248, 0, 248, 255)
BLACK = (0, 0, 0, 255)
ROOT = Path(__file__).resolve().parent.parent
SKIN = ROOT / "src/main/resources/assets/kingdomsatwar/textures/entity/anni_troll/anni_troll.png"

WOOD = st.ramp("#6a4628", dark=0.18, light=0.8, cool=40, warm=60)
STONE = st.ramp("#8a8a92", dark=0.2, light=0.92, cool=260)
IRON = st.ramp("#5a5a62", dark=0.12, light=0.78, cool=260)
RAG = st.ramp("#a89a80", dark=0.3, light=0.9, cool=60)
PINK = st.ramp("#f040c8", dark=0.25, light=0.95, cool=330)
CYAN = st.ramp("#22d8e8", dark=0.25, light=0.97, cool=200)
GUNMETAL = st.ramp("#3a3a46", dark=0.08, light=0.7, cool=260)


def _rng(seed: int) -> random.Random:
    return random.Random(seed)


def checker(w: int, h: int, cell: int) -> Image.Image:
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    for y in range(h):
        for x in range(w):
            px[x, y] = MAGENTA if ((x // cell) + (y // cell)) % 2 == 0 else BLACK
    return img


# =============================================================================
# Annis Bildfehler
# =============================================================================

def skin_missing() -> Image.Image:
    """Annis Haut als fehlende Textur: überall, wo die Haut Farbe hat, Magenta und Schwarz im Schachbrett (je 4 Texel)."""
    skin = Image.open(SKIN).convert("RGBA")
    img = Image.new("RGBA", skin.size, CLEAR)
    sp, px = skin.load(), img.load()
    for y in range(skin.height):
        for x in range(skin.width):
            if sp[x, y][3] > 0:
                px[x, y] = MAGENTA if ((x // 4) + (y // 4)) % 2 == 0 else BLACK
    return img


def loading() -> Image.Image:
    """Der Ladekreis: acht runde Punkte im Kreis, vom hellsten (oben) im Uhrzeigersinn immer blasser; mit dunklem Rand."""
    size = 32
    img = Image.new("RGBA", (size, size), CLEAR)
    px = img.load()
    for i in range(8):
        a = -math.pi / 2 + i * math.pi / 4
        cx, cy = 15.5 + math.cos(a) * 10.5, 15.5 + math.sin(a) * 10.5
        alpha = 1.0 - i * 0.11
        for y in range(size):
            for x in range(size):
                d = math.hypot(x + 0.5 - cx - 0.5, y + 0.5 - cy - 0.5)
                if d <= 3.4:
                    edge = d > 2.4
                    col = (40, 40, 52) if edge else (255, 255, 255)
                    px[x, y] = col + (int(255 * alpha),)
    return img


def ghost() -> Image.Image:
    """Zeilen für das flimmernde Abbild: hell und dunkel im Wechsel, ab und zu ein heller Fleck."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    r = _rng(41)
    for y in range(16):
        for x in range(16):
            a = 220 if y % 2 == 0 else 90
            if r.random() > 0.93:
                a = 255
            px[x, y] = (255, 255, 255, a)
    return img


def pixel() -> Image.Image:
    """Eine Seite eines Pixels: innen fast weiß, außen ein heller Rand und eine dunklere Kante unten rechts (Fase)."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            v = 225
            if x <= 1 or y <= 1:
                v = 255
            elif x >= 14 or y >= 14:
                v = 175
            px[x, y] = (v, v, v, 255)
    return img


def missing_cell() -> Image.Image:
    """Ein Feld des Fehlerfelds: weiß mit einer feinen, dunkleren Fuge (die Farbe kommt aus der Tönung)."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            v = 200 if x in (0, 15) or y in (0, 15) else 255
            px[x, y] = (v, v, v, 255)
    return img


def scanline() -> Image.Image:
    """Die Zeile: in der Mitte hell, zu den Rändern weich auslaufend."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        k = 1.0 - abs(y + 0.5 - 8.0) / 8.0
        for x in range(16):
            px[x, y] = (255, 255, 255, int(255 * min(1.0, k * 1.6) ** 0.8))
    return img


def glitch_pixel(kind: int) -> Image.Image:
    """Fehlerpixel (Partikel): ein volles Quadrat oder eines mit hellem Rand."""
    img = Image.new("RGBA", (8, 8), CLEAR)
    px = img.load()
    for y in range(8):
        for x in range(8):
            if kind == 0:
                px[x, y] = (255, 255, 255, 255)
            elif 1 <= x <= 6 and 1 <= y <= 6:
                px[x, y] = (255, 255, 255, 255) if (x, y) != (1, 1) else (255, 255, 255, 200)
    return img


# =============================================================================
# Blöcke
# =============================================================================

def missing_block() -> Image.Image:
    """Der Fehlerblock: die fehlende Textur – vier Felder, Magenta und Schwarz."""
    return checker(16, 16, 8)


def static_frames(frames: int, seed: int, colored: bool, dim: bool = False) -> Image.Image:
    """Bildrauschen (übereinander, für die Bewegung): Schnee in Grau, hier und da ein farbiges Pixel und ein heller
    Streifen, der durchs Bild läuft. Farbig: Rauschen in Rot, Grün und Blau (der Kern)."""
    img = Image.new("RGBA", (16, 16 * frames), CLEAR)
    px = img.load()
    r = _rng(seed)
    for f in range(frames):
        bar = (f * 5) % 16
        for y in range(16):
            for x in range(16):
                v = r.random()
                if colored:
                    k = r.randrange(3)
                    lv = int((0.35 + v * 0.65) * 255)
                    col = [int(lv * 0.25)] * 3
                    col[k] = lv
                    if v > 0.9:
                        col = [255, 255, 255]
                    if dim:
                        col = [int(c * 0.25 + 18) for c in col]
                    px[x, 16 * f + y] = tuple(col) + (255,)
                else:
                    g = int(60 + v * 170)
                    if abs(y - bar) <= 1:
                        g = min(255, g + 50)
                    col = (g, g, g, 255)
                    if v > 0.985:
                        col = (255, 40, 60, 255) if r.random() < 0.33 else (40, 255, 100, 255) if r.random() < 0.5 else (60, 120, 255, 255)
                    px[x, 16 * f + y] = col
    return img


def core_frame() -> Image.Image:
    """Der Rahmen des Fehlerkerns: dunkles Metall mit Fase, die Kanten mit Farbsaum (oben links magenta, unten rechts cyan)."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            v = 3
            if x == 0 or y == 0:
                col = PINK[4]
            elif x == 15 or y == 15:
                col = CYAN[4]
            else:
                if x == 1 or y == 1:
                    v = 5
                elif x == 14 or y == 14:
                    v = 1
                col = GUNMETAL[v]
                if (x + y * 3) % 7 == 0 and 2 <= x <= 13 and 2 <= y <= 13:
                    col = GUNMETAL[2]
            px[x, y] = col
    # Nieten in den Ecken
    for cx, cy in ((3, 3), (12, 3), (3, 12), (12, 12)):
        px[cx, cy] = GUNMETAL[6]
    return img


# =============================================================================
# Der Bannhammer
# =============================================================================

def bannhammer_icon() -> Image.Image:
    """Schräg (wie die Werkzeuge von Minecraft): ein Baumstamm-Stiel mit Lappen am Griff, oben rechts der Steinklotz mit
    Eisenband – seine vordere Hälfte fehlende Textur –, daneben zwei schwebende Pixel."""
    art = """
.........ccccc..
........ccsssss.
.......cmksssss.
.......mkmisss..
.......kmkissm..
........kmissmk.
.......w.mkmk...
......w..km.....
.....ww.........
....ww..........
...rw...........
..rrw...........
..rr............
.w..............
w...............
................
"""
    s = st.Spr.art(art, legend={"c": ("A", 5), "s": ("A", 3), "i": ("B", 4), "w": ("C", 3), "r": ("D", 4), "m": ("E", 4), "k": ("F", 1)})
    s.set(12, 7, ("G", 5))
    s.set(13, 9, ("H", 5))
    s.set(3, 7, ("G", 5))
    s = s.outline(prefer=("A", "C", "D", "B"))
    return s.image({"A": STONE, "B": IRON, "C": WOOD, "D": RAG, "E": st.fixed("#f800f8", "#f800f8", "#f800f8", "#f800f8", "#f800f8",
                                                                              "#f800f8", "#f800f8"),
                    "F": st.fixed("#000000", "#000000", "#000000", "#000000", "#000000", "#000000", "#000000"), "G": PINK, "H": CYAN})


def bannhammer_model() -> tuple[Image.Image, Image.Image]:
    """Atlas des 3D-Hammers (32 x 32, im Modell 16 x 16): Holz (0–8, 0–32), Lappen (8–14, 0–16), Stein (14–32, 0–18),
    Eisen (8–14, 16–32), fehlende Textur (14–32, 18–28), Pixel (14–32, 28–32: magenta, cyan, rot). Dazu die Leuchtmaske
    (Magenta der fehlenden Textur und die Pixel)."""
    img = Image.new("RGBA", (32, 32), CLEAR)
    glow = Image.new("RGBA", (32, 32), CLEAR)
    px, gp = img.load(), glow.load()
    for y in range(32):
        for x in range(8):
            grain = st.smooth_noise(301, x, y // 3, 2.0)
            v = 2 + round(grain * 2.4)
            if (y * 7 + x * 3) % 23 == 0:
                v = 1
            px[x, y] = WOOD[v]
    for y in range(16):
        for x in range(8, 14):
            px[x, y] = RAG[2 + ((y + x // 2) % 4)]
    for y in range(18):
        for x in range(14, 32):
            n = st.smooth_noise(311, x, y, 2.4)
            v = 2 + round(n * 3)
            if st.noise(317, x, y) > 0.92:
                v = 1
            px[x, y] = STONE[v]
    for y in range(16, 32):
        for x in range(8, 14):
            rivet = (x == 10 and y % 5 == 2)
            px[x, y] = IRON[6 if rivet else 3 + (1 if x == 8 else 0)]
    for y in range(18, 28):
        for x in range(14, 32):
            col = MAGENTA if ((x - 14) // 3 + (y - 18) // 3) % 2 == 0 else BLACK
            px[x, y] = col
            if col == MAGENTA:
                gp[x, y] = (200, 0, 200, 255)
    for i, ramp in enumerate((PINK, CYAN, st.ramp("#ff3040", dark=0.3, light=0.95, cool=20))):
        for y in range(28, 32):
            for x in range(14 + i * 6, 20 + i * 6):
                v = 6 if (x - 14 - i * 6 == 0 or y == 28) else 4
                px[x, y] = ramp[v]
                gp[x, y] = ramp[v]
    return img, glow


# =============================================================================
# Die Fehlerflügel
# =============================================================================

WINGS_LEFT = [
    "........",
    "........",
    "...EEEEk",
    "..Embmbk",
    ".Embmbmk",
    "Embm.mbk",
    "Ebmbmbmk",
    "ebmb.bmk",
    ".ebmbmbk",
    "..ebm.bk",
    "...ebmbk",
    "....eemk",
    "......ek",
    ".......k",
    "........",
    "........",
]

WINGS_BROKEN_LEFT = [
    "........",
    "........",
    "...EE,Ek",
    "..E,b,bk",
    ".Eb,m,mk",
    "Em,m.,bk",
    "E,mb,bmk",
    "e,,b.,mk",
    "..e,m,bk",
    "...e,.bk",
    "....e,mk",
    "......ek",
    "........",
    "........",
    "........",
    "........",
]


def glitch_wings_item(broken: bool) -> Image.Image:
    """Zwei Flügel aus fehlender Textur, mit Löchern; die Vorderkante links magenta, rechts cyan."""
    rows = WINGS_BROKEN_LEFT if broken else WINGS_LEFT
    full = [row + "".join(reversed(row)) for row in rows]
    s = st.Spr.art("\n".join(full), legend={"E": ("E", 4), "e": ("E", 2), "m": ("M", 3), "b": ("B", 1), "k": ("K", 3)})
    s = s.map(lambda x, y, p: ("C", p[1]) if x >= 8 and p[0] == "E" else p)
    s = s.outline(light=1, dark=0, prefer=("E", "C", "M"))
    mag = st.fixed("#5a005a", "#8a008a", "#c000c0", "#f800f8", "#ff60ff", "#ffa0ff", "#ffd8ff")
    if broken:
        mag = st.fixed("#3a1a3a", "#5a2a5a", "#7a3a7a", "#9a4a9a", "#b060b0", "#c880c8", "#e0a8e0")
    return s.image({"E": PINK, "C": CYAN, "M": mag, "B": st.fixed("#000000", "#0a000a", "#140014", "#1e001e", "#280028", "#320032", "#3c003c"),
                    "K": GUNMETAL})


def glitch_wings() -> Image.Image:
    """
    Die getragenen Flügel (Rückseite des Elytren-Quaders: 10 x 20 ab 36, 2) und ihre Kanten: ein Flügel aus fehlender
    Textur (Felder von je zwei Texeln), durch den Löcher gehen; die Vorderkante leuchtet magenta, die Hinterkante cyan, und
    ein paar Felder sind verrutscht.
    """
    img = Image.new("RGBA", (64, 32), CLEAR)
    px = img.load()
    x0, y0 = 36, 2

    def inside(x: float, y: float) -> bool:
        lead = x * 0.4
        trail = 19.5 - x * 0.9 - 1.6 * math.sin(math.pi * min(x, 9.5) / 9.5)
        return lead - 0.3 <= y <= trail and x <= 9.9

    shape = {(x, y): inside(x + 0.5, y + 0.5) for x in range(10) for y in range(20)}
    r = _rng(523)
    holes = {(r.randrange(2, 9), r.randrange(4, 17)) for _ in range(5)}
    for (x, y), ok in shape.items():
        if not ok:
            continue
        if any(abs(x - hx) + abs(y - hy) <= 0 for hx, hy in holes):
            continue
        lead_edge = not shape.get((x, y - 1), False)
        trail_edge = not shape.get((x, y + 1), False) or not shape.get((x + 1, y), True)
        shift = 1 if 8 <= y <= 9 else 0
        col = MAGENTA if (((x + shift) // 2) + (y // 2)) % 2 == 0 else (10, 0, 12, 255)
        if lead_edge:
            col = PINK[5] if x % 2 else PINK[4]
        elif trail_edge:
            col = CYAN[5] if (x + y) % 2 else CYAN[4]
        px[x0 + x, y0 + y] = col
    for x in range(10):
        px[24 + x, 0] = PINK[5]
        px[24 + x, 1] = PINK[4]
    for y in range(20):
        px[22, 2 + y] = (90, 0, 90, 255) if y < 14 else CLEAR
        px[34, 2 + y] = PINK[4] if y < 6 else CLEAR
        px[35, 2 + y] = PINK[4] if y < 6 else CLEAR
    return img


# =============================================================================
# Der Fehlersplitter
# =============================================================================

def glitch_shard_icon() -> Image.Image:
    """Ein gezackter Splitter, schräg: links magenta, rechts cyan, die Mitte weiß; zwei lose Pixel daneben."""
    art = """
................
..........c.....
.........cC.....
........mWCc....
.......mmWCC....
......mMmWCc....
.....mMmWWCc....
....mMmmWCCc....
....mmMWWCc.....
...mmmWWCCc.....
...mMmWCCc......
....mmWCc.......
.....mWc........
......w.........
................
................
"""
    s = st.Spr.art(art, legend={"m": ("A", 3), "M": ("A", 5), "W": ("W", 6), "w": ("W", 4), "c": ("B", 3), "C": ("B", 5)})
    s.set(2, 4, ("A", 5))
    s.set(13, 9, ("B", 5))
    s = s.outline(prefer=("A", "B", "W"))
    return s.image({"A": PINK, "B": CYAN, "W": st.ramp("#f4f0ff", dark=0.6, light=1.0, cool=270)})


def build(put: Callable[[str, Image.Image], None]) -> None:
    if SKIN.exists():
        put("entity/anni_troll/anni_troll_missing", skin_missing())
    put("entity/anni_troll/loading", loading())
    put("entity/anni_troll/ghost", ghost())
    put("entity/anni_troll/pixel", pixel())
    put("entity/anni_troll/missing_cell", missing_cell())
    put("entity/anni_troll/scanline", scanline())
    put("particle/glitch_pixel_0", glitch_pixel(0))
    put("particle/glitch_pixel_1", glitch_pixel(1))
    put("block/missing_block", missing_block())
    put("block/static_block", static_frames(8, 701, False))
    put("block/error_core_inner", static_frames(8, 709, True))
    put("block/error_core_inner_dead", static_frames(1, 719, True, dim=True))
    put("block/error_core_frame", core_frame())
    put("item/bannhammer", bannhammer_icon())
    model, model_glow = bannhammer_model()
    put("item/bannhammer_model", model)
    put("item/bannhammer_model_glow", model_glow)
    put("item/glitch_wings", glitch_wings_item(False))
    put("item/glitch_wings_broken", glitch_wings_item(True))
    put("entity/equipment/wings/glitch_wings", glitch_wings())
    put("item/glitch_shard", glitch_shard_icon())
