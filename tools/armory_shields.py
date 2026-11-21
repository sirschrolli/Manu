#!/usr/bin/env python3
"""
Die Schilde der Waffenkammer (0.84): Form und Bemalung.

Jeder Schild ist aus Quadern gebaut, die um den Ursprung liegen wie der Schild von Minecraft (Platte vorn bei z = 1..2,
der Griff dahinter bei z = -5..1, Mitte bei x = y = 0) – so passen die Haltungen des Vanilla-Schilds (in der Hand, beim
Blocken, im Inventar). Die Vorderseite jedes Quaders zeigt den passenden Ausschnitt EINES Bildes der ganzen Schildfront
(1 Pixel je Einheit), die Rückseite den Ausschnitt des Rückseitenbilds (gespiegelt, wie man sie von hinten sieht); Kanten,
Griff und Seiten der Aufsätze nehmen kleine Farbfelder. Leuchtende Teile (Augen, Runen, Edelsteine) nutzen die Leuchtmaske
mit derselben Aufteilung und leuchten mit Lichtstärke 15.

Atlas je Schild: 64 x 64 Pixel, UV-Einheiten 0..16 (je 4 Pixel). Vorderbild ab (0, 0), Rückseite ab (32, 0), Farbfelder
unten (ab Zeile 40).

Ausgabe: src/main/java/.../datagen/ArmoryShieldShapes.java; Texturen über build(put) (tools/generate_textures.py).
Mit --preview DATEI zeichnet das Skript die Schilde (vorn, schräg, hinten, von der Seite).
"""
from __future__ import annotations

import math
import os
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Callable

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import item_style as st  # noqa: E402
import armory_textures as tx  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/io/github/sirschrolli/kingdomsatwar/datagen/ArmoryShieldShapes.java"
PPU = 4
SIZE = 64
FRONT_AT = (0, 0)
BACK_AT = (32, 0)

Rect = tuple[int, int, int, int]

# Farbfelder (Pixel) – für alle Schilde gleich angeordnet
REGIONS: dict[str, Rect] = {
    "rim": (0, 40, 4, 44),           # Kanten der Platte, des Rands
    "rim_dark": (4, 40, 8, 44),
    "boss": (8, 40, 12, 44),         # Seiten der Aufsätze
    "accent": (12, 40, 16, 44),      # Seiten der Zierstücke (Gold, Mondsilber …)
    "handle": (16, 40, 20, 44),      # Lederriemen des Griffs
    "wood": (20, 40, 24, 44),        # Holz (Kanten der Rückseite)
    "glow": (24, 40, 28, 44),        # Seiten leuchtender Teile (in der Leuchtmaske)
    "dark": (28, 40, 32, 44),
}


@dataclass
class Box:
    lo: tuple[float, float, float]
    hi: tuple[float, float, float]
    front: str = "front"             # Süden: "front" = Ausschnitt des Vorderbilds, sonst ein Farbfeld
    back: str = "back"               # Norden: "back" = Ausschnitt der Rückseite, sonst ein Farbfeld
    side: str = "rim"                # Osten/Westen
    cap: str | None = None           # oben/unten (sonst wie die Seiten)
    glow: bool = False


@dataclass
class Shield:
    name: str
    xr: tuple[int, int]              # Ausmaß des Vorderbilds (Einheiten)
    yr: tuple[int, int]
    boxes: list[Box] = field(default_factory=list)

    @property
    def w(self) -> int:
        return self.xr[1] - self.xr[0]

    @property
    def h(self) -> int:
        return self.yr[1] - self.yr[0]

    def add(self, *args, **kw) -> "Shield":
        self.boxes.append(Box(*args, **kw))
        return self


# =============================================================================
# Formen
# =============================================================================

def rows_of(half_width: Callable[[int], float], y0: int, y1: int) -> list[tuple[int, int, int]]:
    """Zeilen [y, y+1) mit ganzzahliger halber Breite; gleiche Breiten werden zusammengefasst: (y_von, y_bis, halbe Breite)."""
    out: list[tuple[int, int, int]] = []
    for y in range(y0, y1):
        hw = int(round(half_width(y)))
        if hw <= 0:
            continue
        if out and out[-1][1] == y and out[-1][2] == hw:
            out[-1] = (out[-1][0], y + 1, hw)
        else:
            out.append((y, y + 1, hw))
    return out


def disc(r: float) -> Callable[[int], float]:
    return lambda y: math.sqrt(max(0.0, r * r - (y + 0.5) ** 2))


def ring_segments(outer: Callable[[int], float], inner: Callable[[int], float], y0: int, y1: int):
    """Die Randstücke je Zeile zwischen äußerer und innerer Form, zusammengefasst: (y_von, y_bis, x_von, x_bis)."""
    segs: list[tuple[int, int, int, int]] = []
    for y in range(y0, y1):
        ho = int(round(outer(y)))
        hi = int(round(inner(y)))
        if ho <= 0:
            continue
        pieces = [(-ho, ho)] if hi <= 0 else [(-ho, -hi), (hi, ho)]
        for a, b in pieces:
            if b <= a:
                continue
            for i, (sy0, sy1, sa, sb) in enumerate(segs):
                if sy1 == y and sa == a and sb == b:
                    segs[i] = (sy0, y + 1, a, b)
                    break
            else:
                segs.append((y, y + 1, a, b))
    return segs


def handle(sh: Shield) -> None:
    sh.add((-1, -3, -5), (1, 3, 1), front="handle", back="handle", side="handle")


def steel_buckler() -> Shield:
    """Stahl-Rundschild: klein und rund, ein Stahlrand mit Nieten, ein Buckel in der Mitte."""
    sh = Shield("steel_buckler", (-7, 7), (-7, 7))
    for y0, y1, hw in rows_of(disc(7.0), -7, 7):
        sh.add((-hw, y0, 1), (hw, y1, 2), side="rim")
    for y0, y1, a, b in ring_segments(disc(7.0), disc(6.0), -7, 7):
        sh.add((a, y0, 2), (b, y1, 2.5), back="rim", side="rim")
    # Nieten auf dem Rand
    for k in range(8):
        a = math.radians(22.5 + 45 * k)
        cx, cy = math.cos(a) * 6.3, math.sin(a) * 6.3
        sh.add((cx - 0.45, cy - 0.45, 2.5), (cx + 0.45, cy + 0.45, 2.85), back="boss", side="boss")
    # Buckel
    sh.add((-2.5, -2.5, 2), (2.5, 2.5, 2.8), back="boss", side="boss")
    sh.add((-1.5, -1.5, 2.8), (1.5, 1.5, 3.5), back="boss", side="boss")
    sh.add((-0.5, -0.5, 3.5), (0.5, 0.5, 3.9), back="boss", side="boss")
    handle(sh)
    return sh


def heater(y: int) -> float:
    """Halbe Breite des Drachenschilds: oben gerade, ab y = -2 zur Spitze hin schmaler."""
    if y >= -2:
        return 7.0
    return max(0.6, 1.0 + 6.0 * (y + 13) / 11.0)


def dragon_shield() -> Shield:
    """Drachenschild: ein Wappenschild aus Draconium mit Goldrand, Hörnern oben und einem Drachenkopf mit glühenden Augen."""
    sh = Shield("dragon_shield", (-8, 8), (-13, 14))
    for y0, y1, hw in rows_of(heater, -13, 11):
        sh.add((-hw, y0, 1), (hw, y1, 2), side="rim")
    inner = lambda y: heater(y) - 1.0 if y < 10 else 0.0  # noqa: E731
    for y0, y1, a, b in ring_segments(heater, inner, -13, 11):
        sh.add((a, y0, 2), (b, y1, 2.5), back="rim", side="accent")
    # Hörner an den oberen Ecken
    for s in (-1, 1):
        x0, x1 = (-8, -6) if s < 0 else (6, 8)
        sh.add((x0, 10, 1), (x1, 12, 2.5), back="rim", side="accent")
        tx0, tx1 = (-8, -7) if s < 0 else (7, 8)
        sh.add((tx0, 12, 1.25), (tx1, 14, 2.25), back="rim", side="accent")
    # der Drachenkopf (erhaben): Stirn, Schnauze, Hörner, Kiefer
    sh.add((-3, -1, 2), (3, 5, 3), back="boss", side="boss")
    sh.add((-2, -6, 2), (2, -1, 2.8), back="boss", side="boss")
    sh.add((-1.5, -8, 2), (1.5, -6, 2.5), back="boss", side="boss")
    sh.add((-5, 4, 2), (-3, 7, 2.7), back="boss", side="accent")
    sh.add((3, 4, 2), (5, 7, 2.7), back="boss", side="accent")
    sh.add((-6, 6, 2), (-4, 8, 2.5), back="boss", side="accent")
    sh.add((4, 6, 2), (6, 8, 2.5), back="boss", side="accent")
    # glühende Augen
    sh.add((-2.5, 1, 3), (-0.5, 2, 3.25), back="glow", side="glow", glow=True)
    sh.add((0.5, 1, 3), (2.5, 2, 3.25), back="glow", side="glow", glow=True)
    handle(sh)
    return sh


def tower_shield() -> Shield:
    """Turmschild: groß und gewölbt (drei Bahnen), Nachtstahl mit Mondsilberbändern, Sehschlitz und Mondsichel."""
    sh = Shield("tower_shield", (-8, 8), (-13, 13))
    sh.add((-5, -13, 1.5), (5, 13, 2.5), side="rim")
    sh.add((-8, -12, 1), (-5, 12, 2), side="rim")
    sh.add((5, -12, 1), (8, 12, 2), side="rim")
    # Bänder aus Mondsilber (vorn erhaben)
    for y in (-10, -1, 8):
        sh.add((-5, y, 2.5), (5, y + 1.5, 2.8), back="accent", side="accent")
        sh.add((-8, y + 0.25, 2), (-5, y + 1.25, 2.3), back="accent", side="accent")
        sh.add((5, y + 0.25, 2), (8, y + 1.25, 2.3), back="accent", side="accent")
    # Mittelgrat
    sh.add((-0.5, -12.5, 2.5), (0.5, 12.5, 3), back="accent", side="accent")
    # die Mondsichel (leuchtet): je Zeile die zusammenhängenden Pixel als ein flacher Quader
    for y, x0, x1 in crescent_runs():
        sh.add((x0, y, 2.8), (x1, y + 1, 3.05), back="glow", side="glow", glow=True)
    handle(sh)
    return sh


CRESCENT_OUTER = ((-0.4, 4.6), 3.1)
CRESCENT_INNER = ((0.9, 5.1), 2.5)


def in_crescent(x: float, y: float) -> bool:
    (ox, oy), orad = CRESCENT_OUTER
    (ix, iy), irad = CRESCENT_INNER
    return math.hypot(x - ox, y - oy) <= orad and math.hypot(x - ix, y - iy) > irad


def crescent_runs() -> list[tuple[int, int, int]]:
    """Die Pixel der Mondsichel (Zellenmitten), je Zeile zu Läufen zusammengefasst: (y, x_von, x_bis)."""
    runs = []
    for y in range(0, 9):
        x = -5
        while x <= 4:
            if in_crescent(x + 0.5, y + 0.5):
                x0 = x
                while x <= 4 and in_crescent(x + 0.5, y + 0.5):
                    x += 1
                runs.append((y, x0, x))
            else:
                x += 1
    return runs


def mirror_shield() -> Shield:
    """Spiegelschild: rund, ein dunkler Abyssitrand mit glühenden Runen, ein Spiegel aus poliertem Abyssit, ein Edelstein."""
    sh = Shield("mirror_shield", (-8, 8), (-8, 8))
    for y0, y1, hw in rows_of(disc(8.0), -8, 8):
        sh.add((-hw, y0, 1), (hw, y1, 2), side="rim_dark")
    for y0, y1, a, b in ring_segments(disc(8.0), disc(6.5), -8, 8):
        sh.add((a, y0, 2), (b, y1, 2.6), back="rim_dark", side="rim_dark")
    for y0, y1, hw in rows_of(disc(6.5), -7, 7):
        sh.add((-hw, y0, 2), (hw, y1, 2.2), back="accent", side="accent")
    # Runen auf dem Rand (leuchten)
    for k in range(8):
        a = math.radians(45 * k)
        cx, cy = math.cos(a) * 7.25, math.sin(a) * 7.25
        sh.add((cx - 0.4, cy - 0.4, 2.6), (cx + 0.4, cy + 0.4, 2.75), back="glow", side="glow", glow=True)
    # der Edelstein in der Mitte (leuchtet)
    sh.add((-1, -1, 2.2), (1, 1, 2.9), back="glow", side="glow", glow=True)
    handle(sh)
    return sh


SHIELDS: dict[str, Callable[[], Shield]] = {
    "STEEL_BUCKLER": steel_buckler,
    "DRAGON_SHIELD": dragon_shield,
    "TOWER_SHIELD": tower_shield,
    "MIRROR_SHIELD": mirror_shield,
}


# =============================================================================
# UV
# =============================================================================

def _region(name: str) -> tuple[float, float, float, float]:
    u0, v0, u1, v1 = REGIONS[name]
    return (u0 / PPU, v0 / PPU, u1 / PPU, v1 / PPU)


def face_uvs(sh: Shield, b: Box) -> list[tuple[float, float, float, float]]:
    """UV der sechs Seiten in der Reihenfolge Norden, Süden, Osten, Westen, oben, unten."""
    (x0, y0, _), (x1, y1, _) = b.lo, b.hi
    xmin, xmax = sh.xr
    ymin, ymax = sh.yr

    def proj(at: tuple[int, int], mirror: bool):
        fx, fy = at
        if mirror:
            u0, u1 = fx + (xmax - x1), fx + (xmax - x0)
        else:
            u0, u1 = fx + (x0 - xmin), fx + (x1 - xmin)
        v0, v1 = fy + (ymax - y1), fy + (ymax - y0)
        return (u0 / PPU, v0 / PPU, u1 / PPU, v1 / PPU)

    south = proj(FRONT_AT, False) if b.front == "front" else _region(b.front)
    north = proj(BACK_AT, True) if b.back == "back" else _region(b.back)
    side = _region(b.side)
    cap = _region(b.cap or b.side)
    return [north, south, side, side, cap, cap]


# =============================================================================
# Bemalung
# =============================================================================

def _lv(ramp, level: float):
    return ramp[max(0, min(len(ramp) - 1, int(round(level))))]


def _h(*a) -> float:
    return tx.h01(*a)


def _blank():
    return Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0)), Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))


def _paint_image(img: Image.Image, at: tuple[int, int], w: int, h: int, fn: Callable[[float, float, int, int], tuple | None]) -> None:
    """Malt ein Bild der Schildfläche: fn(x, y, px, py) bekommt die Mitte des Pixels in Schild-Einheiten (y nach oben)."""
    px = img.load()
    for py in range(h):
        for pxx in range(w):
            c = fn(pxx, py, w, h)
            if c is not None:
                px[at[0] + pxx, at[1] + py] = c


def _fill(img: Image.Image, name: str, fn: Callable[[int, int, int, int], tuple]) -> None:
    u0, v0, u1, v1 = REGIONS[name]
    px = img.load()
    for y in range(v0, v1):
        for x in range(u0, u1):
            px[x, y] = fn(x - u0, y - v0, u1 - u0, v1 - v0)


def _std_regions(img, glow, rim, rim_dark, boss, accent, glow_ramp, wood=tx.OAK):
    _fill(img, "rim", lambda x, y, w, h: _lv(rim, 4 if (x + y) % 3 else 3))
    _fill(img, "rim_dark", lambda x, y, w, h: _lv(rim_dark, 3 if (x + y) % 3 else 2))
    _fill(img, "boss", lambda x, y, w, h: _lv(boss, 4 if (x + y) % 2 else 3))
    _fill(img, "accent", lambda x, y, w, h: _lv(accent, 5 if (x + y) % 3 else 4))
    _fill(img, "handle", lambda x, y, w, h: _lv(tx.LEATHER, 4 if (y // 2) % 2 else 3))
    _fill(img, "wood", lambda x, y, w, h: _lv(wood, 4 if x % 3 else 3))
    _fill(img, "dark", lambda x, y, w, h: _lv(rim_dark, 1))
    _fill(glow, "glow", lambda x, y, w, h: _lv(glow_ramp, 5 if (x + y) % 2 else 4))


def _light(x: float, y: float, r: float) -> float:
    """Licht von oben links: +1 oben links, -1 unten rechts (auf den Radius bezogen)."""
    return max(-1.0, min(1.0, (-x + y) / (r * 1.4)))


def _back_planks(img, sh: Shield, inside: Callable[[float, float], bool], rim_test: Callable[[float, float], bool], rim_ramp,
                 wood=tx.OAK, strap=True) -> None:
    """Rückseite: senkrechte Planken, ein Lederriemen quer, außen der Rand (von hinten gesehen)."""
    xmin, xmax = sh.xr
    ymin, ymax = sh.yr

    def fn(pxx, py, w, h):
        x = xmax - (pxx + 0.5)                      # von hinten gesehen: links im Bild ist +x
        y = ymax - (py + 0.5)
        if not inside(x, y):
            return None
        if rim_test(x, y):
            return _lv(rim_ramp, 3 + _light(-x, y, 8) * 1.2)
        plank = int(math.floor(x + 100)) // 3
        level = 3.6 + (0.8 if (int(math.floor(x + 100)) % 3) == 0 else 0) - (0.5 if _h(plank, int(y // 4)) < 0.3 else 0)
        if strap and abs(y) < 1.6:
            return _lv(tx.LEATHER, 4 if abs(y) < 0.6 else 3)
        return _lv(wood, level)
    _paint_image(img, BACK_AT, sh.w, sh.h, fn)


def buckler_textures() -> tuple[Image.Image, Image.Image]:
    sh = steel_buckler()
    img, glow = _blank()
    blue = st.ramp("#3a5aa8", dark=0.15, light=0.8, cool=260)
    xmin, xmax = sh.xr
    ymin, ymax = sh.yr

    def front(pxx, py, w, h):
        x, y = xmin + pxx + 0.5, ymax - (py + 0.5)
        if int(round(disc(7.0)(int(math.floor(y))))) < abs(x) + 0.5 - 1e-6 and abs(x) > int(round(disc(7.0)(int(math.floor(y))))):
            return None
        d = math.hypot(x, y)
        lit = _light(x, y, 7)
        if d > 6.0:                                         # Stahlrand
            return _lv(tx.STEEL, 4 + lit * 1.6)
        if d > 5.4:                                         # dunkle Fuge innen am Rand
            return _lv(tx.STEEL, 2)
        if d < 2.6:                                         # Buckel
            return _lv(tx.STEEL, 4 + lit * 2.0 + (1 if d < 1.0 else 0))
        if d < 3.2:                                         # Messingring
            return _lv(tx.BRASS, 4 + lit * 1.4)
        # Feld: blaue Emaille, vier Stahlspeichen
        if abs(x) < 0.75 or abs(y) < 0.75:
            return _lv(tx.STEEL, 4 + lit * 1.2)
        return _lv(blue, 4 + lit * 1.0 - (0.6 if _h(int(x * 2), int(y * 2)) < 0.12 else 0))
    _paint_image(img, FRONT_AT, sh.w, sh.h, front)
    _back_planks(img, sh, lambda x, y: math.hypot(x, y) <= 7.3, lambda x, y: math.hypot(x, y) > 6.0, tx.STEEL)
    _std_regions(img, glow, tx.STEEL, tx.STEEL, tx.STEEL, tx.BRASS, tx.MOON)
    return img, glow


def dragon_textures() -> tuple[Image.Image, Image.Image]:
    sh = dragon_shield()
    img, glow = _blank()
    xmin, xmax = sh.xr
    ymin, ymax = sh.yr

    def inside(x, y):
        if y < 11:
            return abs(x) <= int(round(heater(int(math.floor(y))))) + 1e-6
        # Hörner
        return 6 <= abs(x) <= 8 and y < 12 or 7 <= abs(x) <= 8 and y < 14

    def rim(x, y):
        hw = int(round(heater(int(math.floor(y))))) if y < 11 else 8
        return y >= 10 or abs(x) > hw - 1.0

    def front(pxx, py, w, h):
        x, y = xmin + pxx + 0.5, ymax - (py + 0.5)
        if not inside(x, y):
            return None
        lit = _light(x, y + 1, 9)
        if y >= 11 or (6 <= abs(x) and y >= 10):           # Hörner: Gold, zur Spitze heller
            return _lv(tx.GOLD, 3.5 + (y - 10) * 0.6 + lit)
        if rim(x, y):
            return _lv(tx.GOLD, 4 + lit * 1.5)
        # die Drachenmaske (erhaben, Gold): Hörner, Stirn mit Brauenwülsten, Schnauze, Kiefer
        head = -3 <= x <= 3 and -1 <= y <= 5
        snout = -2 <= x <= 2 and -6 <= y <= -1
        jaw = -1.5 <= x <= 1.5 and -8 <= y <= -6
        horn = (3 <= abs(x) <= 5 and 4 <= y <= 7) or (4 <= abs(x) <= 6 and 6 <= y <= 8)
        g_lit = _light(x, y - 1, 6)
        if horn:
            tip = (abs(x) > 5 and y > 7)
            return _lv(tx.GOLD, 4.2 + g_lit * 1.2 + (1.0 if tip else 0) - (1.0 if abs(x) < 3.5 or (abs(x) < 4.5 and y > 6.5) else 0))
        if head or snout or jaw:
            ax = abs(x)
            if 1 <= y <= 2 and 0.5 <= ax <= 2.5:
                return _lv(tx.DRACONIUM, 0)                     # Augenhöhlen (darüber die Glut)
            if 2 <= y <= 3 and 0.5 <= ax <= 3:
                return _lv(tx.GOLD, 5.6)                        # Brauenwülste, im Licht
            if snout and -5 <= y <= -4 and 0.4 <= ax <= 1.4:
                return _lv(tx.DRACONIUM, 0)                     # Nüstern
            if -6.5 <= y <= -6 and ax <= 1.5:
                return _lv(tx.DRACONIUM, 1)                     # das Maul
            if jaw and y < -7.4:
                return _lv(tx.GOLD, 2.4)
            edge_r = (head and (x > 2.5 or y < -0.5)) or (snout and x > 1.5) or (jaw and x > 1.0)
            edge_l = (head and (x < -2.5 or y > 4.5)) or (snout and x < -1.5) or (jaw and x < -1.0)
            if edge_r:
                return _lv(tx.GOLD, 2.6)
            if edge_l:
                return _lv(tx.GOLD, 5.4)
            ridge = ax < 0.5 and (y > 2.5 or y < -1.5)
            return _lv(tx.GOLD, 4.0 + g_lit * 0.9 + (0.8 if ridge else 0) - (0.6 if (int(x + 10) + int(y + 20)) % 4 == 0 else 0))
        # Feld: Draconium mit Schuppen, um die Maske ein dunkler Schatten
        if (-4 <= x <= 4 and -2 <= y <= 6) or (-3 <= x <= 3 and -9 <= y <= -1) or ((3 <= abs(x) <= 7 and 3 <= y <= 9)):
            near = True
        else:
            near = False
        row = int(math.floor(y + 20))
        col = int(math.floor((x + 20) / 2 + (row % 2) * 0.5))
        sx = (x + 20) / 2 + (row % 2) * 0.5 - col
        scale_edge = sx < 0.25 or (y - math.floor(y)) > 0.75
        level = 4 + lit * 0.6 - (1.0 if scale_edge else 0) - (0.7 if near else 0)
        return _lv(tx.DRACONIUM, level)
    _paint_image(img, FRONT_AT, sh.w, sh.h, front)

    def gfront(pxx, py, w, h):
        x, y = xmin + pxx + 0.5, ymax - (py + 0.5)
        if 1 <= y <= 2 and 0.5 <= abs(x) <= 2.5:
            return _lv(tx.EMBER, 6 if abs(x) < 1.5 else 5)
        return None
    _paint_image(glow, FRONT_AT, sh.w, sh.h, gfront)
    _back_planks(img, sh, inside, rim, tx.GOLD, wood=tx.CHARRED)
    _std_regions(img, glow, tx.GOLD, tx.DRACONIUM, tx.DRACONIUM, tx.GOLD, tx.EMBER, wood=tx.CHARRED)
    return img, glow


def tower_textures() -> tuple[Image.Image, Image.Image]:
    sh = tower_shield()
    img, glow = _blank()
    xmin, xmax = sh.xr
    ymin, ymax = sh.yr
    moon = tx.MOON

    def inside(x, y):
        return abs(x) <= 5 or abs(y) <= 12

    def front(pxx, py, w, h):
        x, y = xmin + pxx + 0.5, ymax - (py + 0.5)
        if not inside(x, y):
            return None
        lit = _light(x, y, 13)
        band = any(b <= y <= b + 1.5 for b in (-10, -1, 8))
        side = abs(x) > 5
        if band:
            return _lv(moon, 4 + lit * 1.2 + (0.6 if y > -1 and y < 0 else 0))
        if abs(x) < 0.5:                                    # Mittelgrat
            return _lv(moon, 4 + lit)
        if 10.2 <= y <= 11.2 and 1.5 <= abs(x) <= 4:       # Sehschlitze
            return _lv(tx.NIGHTSTEEL, 0)
        if abs(y) > 12.4 or (side and abs(y) > 11.4) or abs(abs(x) - 5) < 0.5 or abs(x) > 7.5:
            return _lv(tx.NIGHTSTEEL, 2.5 + lit)           # Ränder und Fugen der Bahnen
        # Mondsichel (die leuchtenden Teile malt die Leuchtmaske), darum ein dunkler Hof
        if in_crescent(x, y):
            return _lv(tx.NIGHTSTEEL, 1)
        if math.hypot(x + 0.4, y - 4.6) <= 4.0:
            return _lv(tx.NIGHTSTEEL, 2.4 + lit * 0.5)
        rivet = (abs(x) in (3.5,) and abs(y) in (6.5, 4.5)) or (side and abs(abs(x) - 6.5) < 0.6 and abs((y + 13) % 4 - 2) < 0.6)
        if rivet:
            return _lv(moon, 5)
        level = (3.8 if not side else 3.2) + lit * 1.0 - (0.5 if _h(int(x + 20), int(y + 20) // 3) < 0.15 else 0)
        return _lv(tx.NIGHTSTEEL, level)
    _paint_image(img, FRONT_AT, sh.w, sh.h, front)

    def gfront(pxx, py, w, h):
        x, y = xmin + pxx + 0.5, ymax - (py + 0.5)
        if in_crescent(x, y):
            return _lv(moon, 6 if x < -1.0 else (5 if x < 0.5 else 4))
        return None
    _paint_image(glow, FRONT_AT, sh.w, sh.h, gfront)
    _back_planks(img, sh, inside, lambda x, y: abs(y) > 12 or abs(x) > 7, tx.NIGHTSTEEL, wood=tx.OAK)
    _std_regions(img, glow, tx.NIGHTSTEEL, tx.NIGHTSTEEL, tx.NIGHTSTEEL, moon, moon)
    return img, glow


def mirror_textures() -> tuple[Image.Image, Image.Image]:
    sh = mirror_shield()
    img, glow = _blank()
    xmin, xmax = sh.xr
    ymin, ymax = sh.yr
    mirror = st.fixed("#2a2440", "#4a4270", "#7a72a8", "#a8a2d0", "#cfcaf0", "#ebe8ff", "#ffffff")

    def inside(x, y):
        return abs(x) <= int(round(disc(8.0)(int(math.floor(y))))) + 1e-6

    def front(pxx, py, w, h):
        x, y = xmin + pxx + 0.5, ymax - (py + 0.5)
        if not inside(x, y):
            return None
        d = math.hypot(x, y)
        lit = _light(x, y, 8)
        if d > 6.5:                                         # Abyssitrand
            return _lv(tx.ABYSS, 3 + lit * 1.6)
        if d > 6.0:
            return _lv(tx.ABYSS, 1)
        # der Spiegel: Verlauf von oben links, zwei helle Streifen der Spiegelung
        t = (-x + y) / 9.0
        streak = abs((x + y) - 2.5) < 0.9 or abs((x + y) + 2.0) < 0.45
        level = 3.6 + t * 1.6 + (1.6 if streak else 0)
        return _lv(mirror, level)
    _paint_image(img, FRONT_AT, sh.w, sh.h, front)

    def gfront(pxx, py, w, h):
        x, y = xmin + pxx + 0.5, ymax - (py + 0.5)
        if math.hypot(x, y) < 1.0:
            return _lv(tx.MAGENTA, 6 if (x < 0 and y > 0) else 5)
        for k in range(8):
            a = math.radians(45 * k)
            if abs(x - math.cos(a) * 7.25) < 0.45 and abs(y - math.sin(a) * 7.25) < 0.45:
                return _lv(tx.MAGENTA, 5)
        return None
    _paint_image(glow, FRONT_AT, sh.w, sh.h, gfront)
    _back_planks(img, sh, inside, lambda x, y: math.hypot(x, y) > 6.5, tx.ABYSS, wood=tx.CHARRED)
    _std_regions(img, glow, tx.ABYSS, tx.ABYSS, tx.ABYSS, mirror, tx.MAGENTA, wood=tx.CHARRED)
    return img, glow


TEXTURES: dict[str, Callable[[], tuple[Image.Image, Image.Image]]] = {
    "steel_buckler": buckler_textures,
    "dragon_shield": dragon_textures,
    "tower_shield": tower_textures,
    "mirror_shield": mirror_textures,
}


def build(put) -> None:
    """Für tools/generate_textures.py."""
    for name, fn in TEXTURES.items():
        img, glow = fn()
        put("item/" + name + "_model", img)
        put("item/" + name + "_model_glow", glow)


# =============================================================================
# Java
# =============================================================================

def _f(v: float) -> str:
    s = f"{v:.4f}".rstrip("0").rstrip(".")
    if s in ("-0", ""):
        s = "0"
    return s + "f"


def java() -> str:
    out = [
        "package io.github.sirschrolli.kingdomsatwar.datagen;",
        "",
        "/**",
        " * Die Teile der Schilde der Waffenkammer (0.84), um den Ursprung gebaut wie der Schild von Minecraft (Platte vorn bei",
        " * z = 1..2, Griff dahinter). Je Teil: von x, y, z, bis x, y, z; Leuchten (0/1); dann die UV der sechs Seiten in der",
        " * Reihenfolge Norden, Süden, Osten, Westen, oben, unten (je u0, v0, u1, v1).",
        " *",
        " * <p>ERZEUGT von tools/armory_shields.py – nicht von Hand ändern.",
        " */",
        "final class ArmoryShieldShapes {",
        "    private ArmoryShieldShapes() {}",
    ]
    for const, fn in SHIELDS.items():
        sh = fn()
        out.append("")
        out.append(f"    /** {sh.name} */")
        out.append(f"    static final float[][] {const} = {{")
        for b in sh.boxes:
            vals = [*b.lo, *b.hi, 1.0 if b.glow else 0.0]
            for uv in face_uvs(sh, b):
                vals.extend(uv)
            out.append("            {" + ", ".join(_f(v) for v in vals) + "},")
        out.append("    };")
    out.append("}")
    return "\n".join(out) + "\n"


# =============================================================================
# Vorschau
# =============================================================================

def _rot(rx: float, ry: float, rz: float):
    import numpy as np
    ax, ay, az = (math.radians(a) for a in (rx, ry, rz))
    Rx = np.array([[1, 0, 0], [0, math.cos(ax), -math.sin(ax)], [0, math.sin(ax), math.cos(ax)]])
    Ry = np.array([[math.cos(ay), 0, math.sin(ay)], [0, 1, 0], [-math.sin(ay), 0, math.cos(ay)]])
    Rz = np.array([[math.cos(az), -math.sin(az), 0], [math.sin(az), math.cos(az), 0], [0, 0, 1]])
    return Rz @ Ry @ Rx


def render(sh: Shield, atlas: Image.Image, glow_atlas: Image.Image, view: tuple[float, float], size: int = 420, scale: float = 11.0):
    """Zeichnet den Schild (Blick: Gieren, Nicken in Grad; 0/0 = von vorn)."""
    import numpy as np
    tex = np.asarray(atlas.convert("RGBA"), dtype=np.float32)
    gtex = np.asarray(glow_atlas.convert("RGBA"), dtype=np.float32)
    img = np.zeros((size, size, 4), dtype=np.float32)
    zbuf = np.full((size, size), -1e9, dtype=np.float32)
    V = _rot(view[1], view[0], 0)
    light = np.array([-0.35, 0.75, 0.55])
    light /= np.linalg.norm(light)
    cy = (sh.yr[0] + sh.yr[1]) / 2.0
    for b in sh.boxes:
        (x0, y0, z0), (x1, y1, z1) = b.lo, b.hi
        uvs = face_uvs(sh, b)
        faces = [
            ([(x1, y1, z0), (x0, y1, z0), (x0, y0, z0), (x1, y0, z0)], uvs[0], (0, 0, -1)),   # Norden (von hinten: links = +x)
            ([(x0, y1, z1), (x1, y1, z1), (x1, y0, z1), (x0, y0, z1)], uvs[1], (0, 0, 1)),    # Süden
            ([(x1, y1, z1), (x1, y1, z0), (x1, y0, z0), (x1, y0, z1)], uvs[2], (1, 0, 0)),    # Osten
            ([(x0, y1, z0), (x0, y1, z1), (x0, y0, z1), (x0, y0, z0)], uvs[3], (-1, 0, 0)),   # Westen
            ([(x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)], uvs[4], (0, 1, 0)),    # oben
            ([(x0, y0, z1), (x1, y0, z1), (x1, y0, z0), (x0, y0, z0)], uvs[5], (0, -1, 0)),   # unten
        ]
        src = gtex if b.glow else tex
        for corners, (u0, v0, u1, v1), nrm in faces:
            pts = [V @ (np.array(c, dtype=float) - np.array([0.0, cy, 0.0])) for c in corners]
            normal = V @ np.array(nrm, dtype=float)
            if normal[2] <= 1e-6:
                continue
            shade = 1.0 if b.glow else 0.55 + 0.45 * max(0.0, float(normal @ light))

            def scr(q):
                return np.array([size / 2 + q[0] * scale, size / 2 - q[1] * scale])
            A, B, D = scr(pts[0]), scr(pts[1]), scr(pts[3])
            E1, E2 = B - A, D - A
            det = E1[0] * E2[1] - E1[1] * E2[0]
            if abs(det) < 1e-9:
                continue
            xs = [A[0], B[0], D[0], (B + E2)[0]]
            ys = [A[1], B[1], D[1], (B + E2)[1]]
            bx0, bx1 = max(0, int(min(xs))), min(size - 1, int(max(xs)) + 1)
            by0, by1 = max(0, int(min(ys))), min(size - 1, int(max(ys)) + 1)
            if bx1 < bx0 or by1 < by0:
                continue
            gx, gy = np.meshgrid(np.arange(bx0, bx1 + 1) + 0.5, np.arange(by0, by1 + 1) + 0.5)
            rx, ry = gx - A[0], gy - A[1]
            s = (rx * E2[1] - ry * E2[0]) / det
            t = (E1[0] * ry - E1[1] * rx) / det
            m = (s >= 0) & (s <= 1) & (t >= 0) & (t <= 1)
            if not m.any():
                continue
            depth = pts[0][2] + s * (pts[1][2] - pts[0][2]) + t * (pts[3][2] - pts[0][2])
            uu = (u0 + s * (u1 - u0)) * PPU
            vv = (v0 + t * (v1 - v0)) * PPU
            ui = np.clip(np.floor(np.minimum(uu, max(u0, u1) * PPU - 1e-3)).astype(int), 0, SIZE - 1)
            vi = np.clip(np.floor(np.minimum(vv, max(v0, v1) * PPU - 1e-3)).astype(int), 0, SIZE - 1)
            col = src[vi, ui]
            sub = (slice(by0, by1 + 1), slice(bx0, bx1 + 1))
            ok = m & (col[..., 3] > 8) & (depth > zbuf[sub])
            zb = zbuf[sub]
            zb[ok] = depth[ok]
            im = img[sub]
            im[ok, :3] = col[ok, :3] * shade
            im[ok, 3] = 255
    out = Image.fromarray(np.clip(img, 0, 255).astype("uint8"), "RGBA")
    bg = Image.new("RGBA", out.size, (58, 62, 70, 255))
    bg.alpha_composite(out)
    return bg


def preview(path: str) -> None:
    views = [(0, 0), (-35, 15), (180, 0), (90, 0), (35, -20)]
    tiles = []
    for const, fn in SHIELDS.items():
        sh = fn()
        atlas, glow = TEXTURES[sh.name]()
        tiles.append([render(sh, atlas, glow, v) for v in views])
    w, h = tiles[0][0].size
    sheet = Image.new("RGBA", (w * len(views), h * len(tiles)), (40, 40, 40, 255))
    for j, row in enumerate(tiles):
        for i, t in enumerate(row):
            sheet.paste(t, (i * w, j * h))
    sheet.save(path)
    print("Vorschau:", path)


def main(argv: list[str]) -> None:
    if "--preview" in argv:
        preview(argv[argv.index("--preview") + 1])
        return
    if "--atlas" in argv:
        out = argv[argv.index("--atlas") + 1]
        sheet = Image.new("RGBA", (SIZE * 4 * len(TEXTURES), SIZE * 4 * 2), (40, 40, 40, 255))
        for i, (name, fn) in enumerate(TEXTURES.items()):
            img, glow = fn()
            sheet.alpha_composite(img.resize((SIZE * 4, SIZE * 4), Image.NEAREST), (i * SIZE * 4, 0))
            sheet.alpha_composite(glow.resize((SIZE * 4, SIZE * 4), Image.NEAREST), (i * SIZE * 4, SIZE * 4))
        sheet.save(out)
        return
    JAVA.write_text(java(), encoding="utf-8")
    print(f"geschrieben: {JAVA.relative_to(ROOT)}")


if __name__ == "__main__":
    main(sys.argv[1:])
