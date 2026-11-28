#!/usr/bin/env python3
"""
Texturen zu Asteria, der Sternenreiterin (0.65), im Vanilla-Stil (siehe tools/item_style.py):

  * die Sternenlanze: Bild im Inventar (schräg, wie die Speere von Minecraft) und der Atlas für das 3D-Modell in der Hand
    (silberner Schaft mit goldener Wendel, violette Griffwicklung, Brechscheibe, Goldkragen, ein Wimpel mit dem Stern des
    Ordens und die Spitze aus einem leuchtenden, vierzackigen Sternkristall);
  * die Sternenschwingen: Bild im Inventar (heil und zerrissen) und die getragenen Schwingen – Rochenflügel aus
    Nachthimmel mit Sternen und Sternbildern, violette Vorderkante;
  * der Sternsplitter: ein vierzackiger Kristallsplitter, innen weiß, außen eisblau;
  * der Sternstein: Sockel aus Quarz mit Goldsaum und eingelegtem Stern, darauf der Sternkristall (leuchtend und
    erloschen), dazu die Leuchtmaske;
  * der fallende Stern: ein vierzackiger Lichtstern und sein bläulicher Lichthof (für das Geschoss).
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import item_style as st
from sea_textures import _diagonal_art

SILVER = st.ramp("#b8c0d8", dark=0.3, light=0.99, cool=265, warm=240, chroma_light=0.3)
GOLD = st.ramp("#e0b84e", dark=0.3, light=0.98, cool=40, warm=75, chroma_light=0.6)
CRYSTAL = st.ramp("#7ab0ff", dark=0.4, light=1.0, cool=255, warm=210, chroma_light=0.25)
VIOLET = st.ramp("#764096", dark=0.22, light=0.82, cool=290)
NIGHT = st.ramp("#1c2458", dark=0.1, light=0.6, cool=250)
STARLIGHT = st.ramp("#cfe6ff", dark=0.6, light=1.0, cool=230)
QUARTZ = st.ramp("#e8e0d4", dark=0.55, light=0.99, cool=260, warm=70, chroma_light=0.4)
CLEAR = (0, 0, 0, 0)


def render(art: str | st.Spr, ramps: dict[str, st.Ramp], prefer: tuple[str, ...] = ("A", "B", "C", "D", "E"),
           override: dict[str, str] | None = None) -> Image.Image:
    s = st.Spr.art(art) if isinstance(art, str) else art
    return s.outline(prefer=prefer, override=override).image(ramps)


# =============================================================================
# Die Sternenlanze
# =============================================================================

def star_lance_icon() -> Image.Image:
    """Schräg von unten links nach oben rechts: goldener Knauf, violette Griffwicklung, Brechscheibe, silberner Schaft mit
    goldener Wendel, Goldkragen, darüber die Spitze aus einem leuchtenden Sternkristall mit vier Zacken."""
    def profile(t: float, off: float):
        if t < 0.05:
            return [(0.95, "d" if off < 0 else "b")]
        if t < 0.17:
            band = int(t * 70) % 2 == 0
            return [(0.85, "s" if band else "q")]
        if t < 0.235:
            w = 2.4 - (t - 0.17) / 0.065 * 1.5
            if abs(off) > w - 0.7:
                return [(w, "e" if off < 0 else "c")]
            return [(w, "5" if off < 0 else "3")]
        if t < 0.70:
            if (t * 14.0) % 1.0 < 0.2:
                return [(0.72, "e" if off < 0 else "c")]
            return [(0.72, "5" if off < 0 else "3")]
        if t < 0.745:
            return [(1.05, "f" if off < 0 else "c")]
        u = (t - 0.745) / 0.255
        if u < 0.18:
            w = 1.1
        elif u < 0.32:
            w = max(1.15, 2.8 - abs(u - 0.25) * 22.0)
        else:
            w = 1.05 * (1.0 - (u - 0.32) / 0.68) ** 0.9 + 0.14
        if abs(off) < 0.5 and u < 0.95:
            return [(w, "n")]
        return [(w, "l" if off < 0 else "j")]
    art = _diagonal_art(profile)
    return render(st.Spr.art(art), {"A": SILVER, "B": GOLD, "C": CRYSTAL, "D": VIOLET}, prefer=("C", "B", "A", "D"))


def star_lance_model() -> tuple[Image.Image, Image.Image]:
    """
    Atlas für das 3D-Modell: (0..2, 0..16) silberner Schaft mit goldener Wendel, (2..4, 0..16) violette Griffwicklung,
    (4..6, 0..8) Gold, (6..10, 0..12) Sternkristall (eisblau, zur Mitte weiß), (10..12, 0..12) weißglühender Kern,
    (12..14, 0..4) Edelstein, (14..16, 0..8) Silber der Brechscheibe mit Goldsaum, (12..16, 8..12) der Wimpel (nachtblau,
    Goldsaum, weißer Stern). Dazu die Leuchtmaske.
    """
    img = Image.new("RGBA", (16, 16), CLEAR)
    glow = Image.new("RGBA", (16, 16), CLEAR)
    px, gp = img.load(), glow.load()
    for y in range(16):
        for x in range(2):
            px[x, y] = GOLD[4 if x == 0 else 3] if (y + x * 2) % 5 == 0 else SILVER[5 if x == 0 else 3]
        for x in range(2, 4):
            px[x, y] = VIOLET[2] if y % 3 == 0 else VIOLET[4 if x == 2 else 3]
    for y in range(8):
        for x in range(4, 6):
            px[x, y] = GOLD[5 if x == 4 and y % 3 != 2 else 3]
    for y in range(12):
        for x in range(6, 10):
            edge = x in (6, 9)
            col = CRYSTAL[3 if edge else 5] if y > 1 else CRYSTAL[4 if edge else 6]
            px[x, y] = col
            gp[x, y] = col if not edge else (int(col[0] * 0.75), int(col[1] * 0.75), int(col[2] * 0.75), 255)
        for x in range(10, 12):
            col = CRYSTAL[6] if (x + y) % 3 else st.hexc("#ffffff")
            px[x, y] = col
            gp[x, y] = col
    for y in range(4):
        for x in range(12, 14):
            col = CRYSTAL[5 if (x + y) % 2 == 0 else 3]
            px[x, y] = col
            gp[x, y] = col
    for y in range(8):
        for x in range(14, 16):
            px[x, y] = GOLD[4] if y in (0, 7) else SILVER[5 if x == 14 else 3]
    # der Wimpel: nachtblau, oben und unten Goldsaum, in der Mitte der weiße Stern
    for y in range(8, 12):
        for x in range(12, 16):
            if y in (8, 11):
                px[x, y] = GOLD[4]
            else:
                px[x, y] = NIGHT[3 if (x + y) % 2 else 2]
    for x, y in ((13, 9), (14, 9), (13, 10), (14, 10)):
        px[x, y] = STARLIGHT[6]
        gp[x, y] = STARLIGHT[6]
    return img, glow


# =============================================================================
# Die Sternenschwingen
# =============================================================================

WINGS_LEFT = [
    # ein Sternenrochen von oben: 8 Spalten (außen -> zur Mitte), die Mitte selbst (Spalte 7) ist der Leib mit dem Schwanz
    "........",
    "........",
    "........",
    ".....EEk",
    "...EEbbk",
    ".EEbbcbk",
    "Ebbbbbbk",
    ".ebcbbbk",
    "..ebbbbk",
    "...eebck",
    ".....eek",
    ".......k",
    ".......k",
    ".......c",
    "........",
    "........",
]

WINGS_BROKEN_LEFT = [
    "........",
    "........",
    "........",
    ".....EEk",
    "...E,bbk",
    ".E,bb,bk",
    "Eb,b,bbk",
    ".e,,bbbk",
    "...e,bbk",
    "....e,ck",
    ".....e.k",
    ".......k",
    "........",
    "........",
    "........",
    "........",
]


def _wings_sprite(left: list[str]) -> st.Spr:
    """Beide Schwingen: links wie gezeichnet, rechts gespiegelt (eine Stufe dunkler – das Licht kommt von links)."""
    rows = [row + "".join(reversed(row)) for row in left]
    s = st.Spr.art("\n".join(rows), legend={"E": ("E", 4), "k": ("B", 4), "b": ("B", 2), "c": ("D", 5), "e": ("E", 2)})
    s = s.map(lambda x, y, p: (p[0], st.clamp(p[1] - 1)) if x >= 8 and p[0] in ("B", "E") else p)
    return s.outline(light=1, dark=0, prefer=("E", "B"))


def star_wings_item(broken: bool) -> Image.Image:
    s = _wings_sprite(WINGS_BROKEN_LEFT if broken else WINGS_LEFT)
    ramps = {"B": NIGHT, "C": SILVER, "D": STARLIGHT, "E": st.ramp("#8a5cc8", dark=0.25, light=0.92, cool=280)}
    if broken:
        ramps["B"] = st.ramp("#262a40", dark=0.1, light=0.5, cool=250)
        ramps["E"] = st.ramp("#6a5a86", dark=0.22, light=0.75, cool=280)
        ramps["D"] = st.ramp("#8a9ab4", dark=0.4, light=0.85)
    return s.image(ramps)


def _hash(x: int, y: int, seed: int) -> float:
    return st.noise(seed, x, y)


def star_wings() -> Image.Image:
    """
    Die getragenen Schwingen (Rückseite des Elytren-Quaders: 10x20 ab 36,2) plus Kanten: ein breiter Rochenflügel – die
    Vorderkante läuft von der Schulter schräg zur Spitze außen, die Hinterkante geschwungen zurück zur Mitte. Nachthimmel
    mit funkelnden Sternen und einem Sternbild, violette Vorderkante, zur Spitze hin ein Schleier aus violettem Nebel.
    """
    img = Image.new("RGBA", (64, 32), CLEAR)
    px = img.load()
    x0, y0 = 36, 2
    nebula = [st.hexc(c) for c in ("#2a1a6a", "#4a2a92", "#7a3ab4", "#a85ad0")]

    def inside(x: float, y: float) -> bool:
        lead = x * 0.55
        trail = 19.6 - x * 1.38 - 1.3 * math.sin(math.pi * min(x, 9.5) / 9.5)
        return lead - 0.3 <= y <= trail and x <= 9.9

    shape = {(x, y): inside(x + 0.5, y + 0.5) for x in range(10) for y in range(20)}
    for (x, y), ok in shape.items():
        if not ok:
            continue
        lead_edge = not shape.get((x, y - 1), False)
        trail_edge = not shape.get((x, y + 1), False) or not shape.get((x + 1, y), True)
        n = st.smooth_noise(907, x, y, 3.0)
        c = NIGHT[1 + round(n * 2.2)]
        neb = st.smooth_noise(911, x, y, 2.6) + x * 0.05
        if neb > 0.72:
            c = st.blend(c, nebula[min(3, int((neb - 0.72) * 10))], 0.55)
        if _hash(x, y, 913) > 0.9:
            c = STARLIGHT[4 + int(_hash(x, y, 917) * 3)]
        if lead_edge:
            c = st.hexc("#a878e0") if y < 2 or x % 2 else st.hexc("#8a5cc8")
        elif trail_edge:
            c = nebula[3] if (x + y) % 3 else STARLIGHT[3]
        px[x0 + x, y0 + y] = c
    # ein Sternbild (fünf Sterne, durch feine Linien verbunden)
    stars = [(2, 4), (4, 6), (6, 6), (3, 9), (5, 11)]
    for (ax, ay), (bx, by) in zip(stars, stars[1:]):
        for x, y in st.line(ax, ay, bx, by):
            if shape.get((x, y)):
                px[x0 + x, y0 + y] = st.blend(px[x0 + x, y0 + y], st.hexc("#5a7ac8"), 0.7)
    for x, y in stars:
        if shape.get((x, y)):
            px[x0 + x, y0 + y] = STARLIGHT[6]
    # Oberkante (Deckfläche 24,0) und Seitenflächen: die violette Vorderkante
    for x in range(10):
        px[24 + x, 0] = st.hexc("#a878e0")
        px[24 + x, 1] = st.hexc("#8a5cc8")
    for y in range(20):
        px[22, 2 + y] = st.hexc("#6a3e8a") if y < 14 else CLEAR
        px[34, 2 + y] = st.hexc("#8a5cc8") if y < 6 else CLEAR
        px[35, 2 + y] = st.hexc("#8a5cc8") if y < 6 else CLEAR
    return img


# =============================================================================
# Der Sternsplitter
# =============================================================================

def star_shard_icon() -> Image.Image:
    """Ein vierzackiger Kristallsplitter, leicht geneigt: innen weiß glühend, nach außen eisblau, zwei kleine Funken."""
    s = st.Spr()
    cx, cy = 7.5, 8.0
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            # leicht gedreht
            rx = dx * 0.96 - dy * 0.28
            ry = dx * 0.28 + dy * 0.96
            ax, ay = abs(rx), abs(ry)
            vert = ax <= max(0.0, 1.45 - ay * 0.22) and ay <= 6.6
            horz = ay <= max(0.0, 1.25 - ax * 0.26) and ax <= 5.0
            core = ax + ay <= 2.6
            if not (vert or horz or core):
                continue
            d = math.hypot(rx, ry)
            lvl = 6 if d < 1.3 else 5 if d < 2.6 else 4 if d < 4.0 else 3
            if rx > 0.4 and ry > -0.5 and lvl < 6:
                lvl -= 1
            s.set(x, y, ("C", lvl))
    for x, y in ((13, 3), (2, 12)):
        s.set(x, y, ("D", 6))
    return render(s, {"C": CRYSTAL, "D": STARLIGHT}, prefer=("C", "D"))


# =============================================================================
# Der Sternstein
# =============================================================================

def _star(cx: float, cy: float, x: int, y: int, arm: float, width: float) -> float:
    """Vierzackiger Stern um (cx, cy): 1 in der Mitte, 0 außen."""
    dx, dy = abs(x + 0.5 - cx), abs(y + 0.5 - cy)
    along = max(0.0, 1.0 - dx / arm) if dy <= width * max(0.0, 1.0 - dx / arm) + 0.3 else 0.0
    across = max(0.0, 1.0 - dy / arm) if dx <= width * max(0.0, 1.0 - dy / arm) + 0.3 else 0.0
    return max(along, across)


def starstone_base(top: bool) -> Image.Image:
    """Quarz mit Goldsaum. Oben: ein achtzackiger Stern aus Gold mit blauem Kern; an den Seiten: ein kleiner Stern."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    px = img.load()
    for y in range(16):
        for x in range(16):
            n = st.smooth_noise(613, x, y, 3.2)
            v = 3 + round(n * 2.0)
            px[x, y] = QUARTZ[st.clamp(v)]
            if top:
                if x in (0, 15) or y in (0, 15):
                    px[x, y] = GOLD[4 if (x == 0 or y == 0) else 2]
                k = _star(8, 8, x, y, 7.2, 1.0)
                # Diagonalzacken
                dx, dy = x + 0.5 - 8, y + 0.5 - 8
                diag = abs(abs(dx) - abs(dy)) < 0.75 and math.hypot(dx, dy) < 5.6
                if k > 0.05 or diag:
                    px[x, y] = GOLD[5 if k > 0.6 else 4]
                if math.hypot(dx, dy) < 1.6:
                    px[x, y] = CRYSTAL[5]
            else:
                if y <= 1:
                    px[x, y] = GOLD[5 if y == 0 else 3]
                elif y == 15:
                    px[x, y] = QUARTZ[1]
                elif y == 14:
                    px[x, y] = GOLD[2]
                k = _star(8, 8.5, x, y, 4.2, 0.8)
                if k > 0.05:
                    px[x, y] = CRYSTAL[3 if k < 0.5 else 5]
    return img


def starstone_crystal(lit: bool) -> tuple[Image.Image, Image.Image]:
    """Facetten eines Sternkristalls: unten tiefblau, nach oben weiß; schräge Kanten zwischen den Facetten."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    glow = Image.new("RGBA", (16, 16), CLEAR)
    px, gp = img.load(), glow.load()
    ramp = CRYSTAL if lit else st.ramp("#6a7894", dark=0.25, light=0.78, cool=250)
    for y in range(16):
        for x in range(16):
            v = 2 + (15 - y) / 15 * 3.6
            facet = (x + y // 2) % 6
            if facet == 0:
                v += 1.2
            elif facet == 3:
                v -= 0.6
            if st.noise(619, x, y) > 0.93:
                v += 1.5
            col = ramp[st.clamp(round(v))]
            px[x, y] = col
            if lit:
                k = 0.55 + (15 - y) / 15 * 0.45
                gp[x, y] = (round(col[0] * k), round(col[1] * k), round(col[2] * k), 255)
    return img, glow


# =============================================================================
# Der fallende Stern
# =============================================================================

def starfall_star() -> Image.Image:
    """Ein vierzackiger Lichtstern (32x32): weißer Kern, lange senkrechte und waagrechte Strahlen, kurze schräge."""
    img = Image.new("RGBA", (32, 32), CLEAR)
    px = img.load()
    c = 15.5
    for y in range(32):
        for x in range(32):
            dx, dy = x + 0.5 - 16, y + 0.5 - 16
            r = math.hypot(dx, dy)
            core = math.exp(-(r / 3.2) ** 2)
            spike = max(math.exp(-(dy / 0.9) ** 2) * max(0.0, 1.0 - abs(dx) / c),
                        math.exp(-(dx / 0.9) ** 2) * max(0.0, 1.0 - abs(dy) / c))
            u, v = (dx + dy) / 1.4142, (dx - dy) / 1.4142
            diag = max(math.exp(-(v / 0.8) ** 2) * max(0.0, 1.0 - abs(u) / 7.0),
                       math.exp(-(u / 0.8) ** 2) * max(0.0, 1.0 - abs(v) / 7.0)) * 0.6
            k = min(1.0, core + spike ** 1.4 + diag)
            if k < 0.03:
                continue
            white = min(1.0, core * 1.4 + spike * 0.3)
            col = st.blend(st.hexc("#7ab8ff"), st.hexc("#ffffff"), white)
            px[x, y] = (round(col[0] * k), round(col[1] * k), round(col[2] * k), round(255 * k))
    return img


def starfall_halo() -> Image.Image:
    """Ein weicher, bläulicher Lichthof (32x32)."""
    img = Image.new("RGBA", (32, 32), CLEAR)
    px = img.load()
    for y in range(32):
        for x in range(32):
            r = math.hypot(x + 0.5 - 16, y + 0.5 - 16)
            k = math.exp(-(r / 8.0) ** 2) * 0.75
            if k < 0.02:
                continue
            col = st.hexc("#6aa8ff")
            px[x, y] = (round(col[0] * k), round(col[1] * k), round(col[2] * k), round(255 * k))
    return img


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("item/star_lance", star_lance_icon())
    model, model_glow = star_lance_model()
    put("item/star_lance_model", model)
    put("item/star_lance_model_glow", model_glow)
    put("item/star_wings", star_wings_item(False))
    put("item/star_wings_broken", star_wings_item(True))
    put("entity/equipment/wings/star_wings", star_wings())
    put("item/star_shard", star_shard_icon())
    put("block/starstone_base", starstone_base(False))
    put("block/starstone_base_top", starstone_base(True))
    lit, _ = starstone_crystal(True)
    put("block/starstone_crystal", lit)
    dim, _ = starstone_crystal(False)
    put("block/starstone_crystal_dim", dim)
    put("entity/starfall/star", starfall_star())
    put("entity/starfall/halo", starfall_halo())
