#!/usr/bin/env python3
"""
Werkzeugkasten für Gegenstands-Sprites (16x16) im Stil der Vanilla-Gegenstände.

Grundregeln (aus dem Studium der Vanilla-Gegenstände abgeleitet, keine Pixel übernommen):
- Licht kommt von oben links: Glanzlichter an oberen/linken Kanten, Schatten unten/rechts.
- Der Umriss ist KEIN Schwarz, sondern die dunkelste Stufe des angrenzenden Materials: oben/links etwas heller
  (Stufe 1), unten/rechts am dunkelsten (Stufe 0). Umriss nur über die 4 direkten Nachbarn -> weiche Ecken.
- Jedes Material hat eine Tonleiter mit 7 Stufen (0 = Umriss dunkel ... 6 = Glanzlicht). Dunkle Stufen wandern im
  Farbton Richtung Blau/Violett und werden satter, helle Stufen Richtung Gelb und blasser.
- Wenige, klare Flächen statt Rauschen; keine einzelnen hellen Pixel mitten in dunklen Flächen.

Ein Sprite ist ein Raster aus (Slot, Stufe). Als ASCII gezeichnet:
    Ziffern 0-6 -> Slot "A",  a-g -> "B",  h-n -> "C",  o-u -> "D",
    A-G -> "E",  H-N -> "F",  O-U -> "G"
    '.' = durchsichtig (hier darf ein Umriss entstehen), ',' = durchsichtig und bleibt es.
Weitere Zeichen können über ein eigenes legend-Wörterbuch belegt werden.
"""
from __future__ import annotations

import math
import random
from typing import Callable, Iterable, Sequence

from PIL import Image

Color = tuple[int, int, int, int]
Px = tuple[str, int]
N = 16

SLOT_CHARS = {
    "A": "0123456",
    "B": "abcdefg",
    "C": "hijklmn",
    "D": "opqrstu",
    "E": "ABCDEFG",
    "F": "HIJKLMN",
    "G": "OPQRSTU",
}
CHAR_TO_PX: dict[str, Px] = {ch: (slot, i) for slot, chars in SLOT_CHARS.items() for i, ch in enumerate(chars)}


# =============================================================================
# Farben (OKLab / OKLCH)
# =============================================================================

def hexc(value: str, alpha: int = 255) -> Color:
    value = value.lstrip("#")
    return int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16), alpha


def _lin(c: float) -> float:
    c /= 255.0
    return c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4


def _srgb(c: float) -> int:
    v = 12.92 * c if c <= 0.0031308 else 1.055 * (max(c, 0.0) ** (1 / 2.4)) - 0.055
    return round(max(0.0, min(1.0, v)) * 255)


def _cbrt(v: float) -> float:
    return math.copysign(abs(v) ** (1 / 3), v)


def to_oklch(c: Color | str) -> tuple[float, float, float]:
    if isinstance(c, str):
        c = hexc(c)
    r, g, b = _lin(c[0]), _lin(c[1]), _lin(c[2])
    l_ = _cbrt(0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b)
    m_ = _cbrt(0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b)
    s_ = _cbrt(0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b)
    L = 0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_
    A = 1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_
    B = 0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_
    return L, math.hypot(A, B), math.degrees(math.atan2(B, A)) % 360


def from_oklch(L: float, C: float, h: float, alpha: int = 255) -> Color:
    A, B = C * math.cos(math.radians(h)), C * math.sin(math.radians(h))
    # Chroma so weit verringern, bis die Farbe darstellbar ist
    for _ in range(24):
        l_ = (L + 0.3963377774 * A + 0.2158037573 * B) ** 3
        m_ = (L - 0.1055613458 * A - 0.0638541728 * B) ** 3
        s_ = (L - 0.0894841775 * A - 1.2914855480 * B) ** 3
        r = 4.0767416621 * l_ - 3.3077115913 * m_ + 0.2309699292 * s_
        g = -1.2684380046 * l_ + 2.6097574011 * m_ - 0.3413193965 * s_
        b = -0.0041960863 * l_ - 0.7034186147 * m_ + 1.7076147010 * s_
        if min(r, g, b) >= -0.002 and max(r, g, b) <= 1.002:
            break
        A *= 0.9
        B *= 0.9
    return _srgb(r), _srgb(g), _srgb(b), alpha


def _toward(h: float, target: float, amount: float) -> float:
    d = ((target - h + 180) % 360) - 180
    return (h + math.copysign(min(abs(d), amount), d)) % 360


Ramp = list[Color]


def ramp(base: str | Color, dark: float = 0.22, light: float = 0.93, base_level: int = 4, cool: float = 270.0,
         warm: float = 85.0, hue_dark: float = 18.0, hue_light: float = 14.0, chroma_dark: float = 1.0,
         chroma_light: float = 0.45, levels: int = 7) -> Ramp:
    """
    Tonleiter mit {@code levels} Stufen; Stufe {@code base_level} ist die Grundfarbe. Stufe 0 hat die Helligkeit
    {@code dark}, die letzte {@code light} (OKLab-L). Dunkle Stufen drehen bis {@code hue_dark} Grad Richtung
    {@code cool}, helle bis {@code hue_light} Grad Richtung {@code warm}. Die Sättigung fällt zu den Enden ab.
    """
    L0, C0, h0 = to_oklch(base)
    out: Ramp = []
    for i in range(levels):
        if i <= base_level:
            t = (base_level - i) / max(1, base_level)  # 0 an der Grundfarbe, 1 ganz dunkel
            L = L0 + (dark - L0) * t
            C = C0 * (1 + (chroma_dark - 1) * t)
            h = _toward(h0, cool, hue_dark * t)
        else:
            t = (i - base_level) / max(1, levels - 1 - base_level)
            L = L0 + (light - L0) * t
            C = C0 * (1 + (chroma_light - 1) * t)
            h = _toward(h0, warm, hue_light * t)
        out.append(from_oklch(L, C, h))
    return out


def fixed(*hex_values: str) -> Ramp:
    """Handverlesene Tonleiter (dunkel -> hell)."""
    return [hexc(v) for v in hex_values]


def blend(a: Color, b: Color, t: float) -> Color:
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(4))  # type: ignore[return-value]


# =============================================================================
# Sprite-Raster
# =============================================================================

class Spr:
    """16x16-Raster aus (Slot, Stufe) bzw. None (durchsichtig)."""

    def __init__(self, w: int = N, h: int = N):
        self.w, self.h = w, h
        self.g: list[list[Px | None]] = [[None] * w for _ in range(h)]
        self.blocked: set[tuple[int, int]] = set()

    # ---- Aufbau ----------------------------------------------------------------------------------------------------

    @classmethod
    def art(cls, text: str, legend: dict[str, Px] | None = None, w: int = N, h: int = N) -> "Spr":
        s = cls(w, h)
        s.draw(text, legend=legend)
        return s

    def draw(self, text: str, dx: int = 0, dy: int = 0, legend: dict[str, Px] | None = None) -> "Spr":
        # nur EINEN führenden Zeilenumbruch entfernen (leere Zeilen am Anfang verschieben die Zeichnung nach unten)
        rows = (text[1:] if text.startswith("\n") else text).rstrip().split("\n")
        for y, row in enumerate(rows):
            for x, ch in enumerate(row.rstrip()):
                X, Y = x + dx, y + dy
                if not (0 <= X < self.w and 0 <= Y < self.h) or ch in ". ":
                    continue
                if ch == ",":
                    self.g[Y][X] = None
                    self.blocked.add((X, Y))
                    continue
                px = (legend or {}).get(ch) or CHAR_TO_PX.get(ch)
                if px is None:
                    raise KeyError(f"Unbekanntes Zeichen {ch!r} in Zeile {y}: {row!r}")
                self.g[Y][X] = px
                self.blocked.discard((X, Y))
        return self

    def copy(self) -> "Spr":
        s = Spr(self.w, self.h)
        s.g = [row[:] for row in self.g]
        s.blocked = set(self.blocked)
        return s

    def get(self, x: int, y: int) -> Px | None:
        return self.g[y][x] if 0 <= x < self.w and 0 <= y < self.h else None

    def set(self, x: int, y: int, px: Px | None) -> None:
        if 0 <= x < self.w and 0 <= y < self.h:
            self.g[y][x] = px

    def opaque(self, x: int, y: int) -> bool:
        return self.get(x, y) is not None

    def cells(self) -> Iterable[tuple[int, int, Px]]:
        for y in range(self.h):
            for x in range(self.w):
                if self.g[y][x] is not None:
                    yield x, y, self.g[y][x]  # type: ignore[misc]

    def mask(self) -> set[tuple[int, int]]:
        return {(x, y) for x, y, _ in self.cells()}

    def over(self, other: "Spr", dx: int = 0, dy: int = 0) -> "Spr":
        """Legt ein anderes Sprite darüber (seine durchsichtigen Pixel lassen dieses durch)."""
        for x, y, px in other.cells():
            self.set(x + dx, y + dy, px)
        for x, y in other.blocked:
            self.blocked.add((x + dx, y + dy))
        return self

    def shifted(self, dx: int, dy: int) -> "Spr":
        s = Spr(self.w, self.h)
        return s.over(self, dx, dy)

    def flipped(self) -> "Spr":
        s = Spr(self.w, self.h)
        for x, y, px in self.cells():
            s.g[y][self.w - 1 - x] = px
        s.blocked = {(self.w - 1 - x, y) for x, y in self.blocked}
        return s

    def map(self, fn: Callable[[int, int, Px], Px | None]) -> "Spr":
        for x, y, px in list(self.cells()):
            self.g[y][x] = fn(x, y, px)
        return self

    def slot(self, src: str, dst: str, shift: int = 0) -> "Spr":
        """Ersetzt einen Slot durch einen anderen (optional um Stufen verschoben)."""
        return self.map(lambda x, y, p: (dst, clamp(p[1] + shift)) if p[0] == src else p)

    def fill(self, pts: Iterable[tuple[int, int]], px: Px) -> "Spr":
        for x, y in pts:
            self.set(x, y, px)
        return self

    # ---- Umriss ------------------------------------------------------------------------------------------------------

    def outline(self, light: int = 1, dark: int = 0, slots: Sequence[str] | None = None,
                prefer: Sequence[str] = (), override: dict[str, str] | None = None) -> "Spr":
        """
        Umriss außen um alle sichtbaren Pixel (4er-Nachbarschaft). Liegt das Sprite links/oberhalb des Randpixels
        (Randpixel unten/rechts), bekommt es Stufe {@code dark}, sonst {@code light}. Das Material kommt vom Nachbarn;
        {@code prefer} legt fest, welche Slots bei mehreren Nachbarn gewinnen; {@code override} lenkt einen Slot auf
        einen anderen Umriss-Slot um (z. B. leuchtende Kristalle mit dunklem Fassungsumriss).
        """
        out = self.copy()
        for y in range(self.h):
            for x in range(self.w):
                if self.g[y][x] is not None or (x, y) in self.blocked:
                    continue
                cand: list[tuple[int, str]] = []
                shadow_side = False
                for dx, dy in ((-1, 0), (0, -1), (1, 0), (0, 1)):
                    p = self.get(x + dx, y + dy)
                    if p is None or (slots is not None and p[0] not in slots):
                        continue
                    if dx == -1 or dy == -1:
                        shadow_side = True
                    rank = prefer.index(p[0]) if p[0] in prefer else len(prefer)
                    cand.append((rank, p[0]))
                if not cand:
                    continue
                cand.sort()
                slot = cand[0][1]
                if override and slot in override:
                    slot = override[slot]
                out.g[y][x] = (slot, dark if shadow_side else light)
        return out

    def inline(self, light: int = 1, dark: int = 0, slots: Sequence[str] | None = None) -> "Spr":
        """Wie outline(), aber die Randpixel werden INNEN gesetzt (die äußerste Pixelreihe wird zum Umriss)."""
        out = self.copy()
        for x, y, p in self.cells():
            if slots is not None and p[0] not in slots:
                continue
            free = [(dx, dy) for dx, dy in ((-1, 0), (0, -1), (1, 0), (0, 1)) if not self.opaque(x + dx, y + dy)]
            if free:
                shadow_side = any(dx == 1 or dy == 1 for dx, dy in free)
                out.g[y][x] = (p[0], dark if shadow_side else light)
        return out

    # ---- Ausgabe -------------------------------------------------------------------------------------------------

    def image(self, ramps: dict[str, Ramp], alpha: dict[str, int] | None = None) -> Image.Image:
        img = Image.new("RGBA", (self.w, self.h), (0, 0, 0, 0))
        px = img.load()
        for x, y, (slot, lvl) in self.cells():
            if slot not in ramps:
                raise KeyError(f"Keine Tonleiter für Slot {slot!r}")
            r = ramps[slot]
            c = r[max(0, min(len(r) - 1, lvl))]
            if alpha and slot in alpha:
                c = (c[0], c[1], c[2], alpha[slot])
            px[x, y] = c
        return img


def clamp(v: int, lo: int = 0, hi: int = 6) -> int:
    return max(lo, min(hi, v))


# =============================================================================
# Formen
# =============================================================================

def ellipse(cx: float, cy: float, rx: float, ry: float) -> set[tuple[int, int]]:
    return {(x, y) for y in range(N) for x in range(N) if ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2 <= 1.0}


def egg_shape(cx: float = 8.0, top: float = 0.5, bottom: float = 15.5, half_width: float = 6.5,
              point: float = 0.32) -> set[tuple[int, int]]:
    """Eiform: unten breiter als oben ({@code point} = wie spitz das obere Ende ist)."""
    pts = set()
    height = bottom - top
    for y in range(N):
        for x in range(N):
            t = (y + 0.5 - top) / height  # 0 oben, 1 unten
            if not 0 <= t <= 1:
                continue
            # Profil: Halbellipse, oben schmaler
            w = math.sqrt(max(0.0, 1 - (2 * t - 1) ** 2))
            w *= 1 - point * (1 - t) ** 2
            if abs(x + 0.5 - cx) <= half_width * w + 0.15:
                pts.add((x, y))
    return pts


def polygon(points: Sequence[tuple[float, float]]) -> set[tuple[int, int]]:
    """Pixel, deren Mittelpunkt im Polygon liegt."""
    out = set()
    n = len(points)
    for y in range(N):
        for x in range(N):
            px, py = x + 0.5, y + 0.5
            inside = False
            j = n - 1
            for i in range(n):
                xi, yi = points[i]
                xj, yj = points[j]
                if (yi > py) != (yj > py) and px < (xj - xi) * (py - yi) / (yj - yi) + xi:
                    inside = not inside
                j = i
            if inside:
                out.add((x, y))
    return out


def line(x0: int, y0: int, x1: int, y1: int) -> list[tuple[int, int]]:
    """Bresenham-Linie."""
    pts = []
    dx, dy = abs(x1 - x0), -abs(y1 - y0)
    sx, sy = (1 if x0 < x1 else -1), (1 if y0 < y1 else -1)
    err = dx + dy
    while True:
        pts.append((x0, y0))
        if x0 == x1 and y0 == y1:
            break
        e2 = 2 * err
        if e2 >= dy:
            err += dy
            x0 += sx
        if e2 <= dx:
            err += dx
            y0 += sy
    return pts


# =============================================================================
# Schattierung runder Formen (gerichtetes Licht, keine "Kissen"-Schattierung)
# =============================================================================

LIGHT = (-0.55, -0.7, 0.75)


def _distance(mask: set[tuple[int, int]]) -> dict[tuple[int, int], float]:
    """Abstand jedes Pixels zum nächsten Pixel außerhalb der Form (Chamfer 3-4)."""
    INF = 10 ** 6
    d = {p: INF for p in mask}
    for p in mask:
        x, y = p
        if any((x + dx, y + dy) not in mask for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
            d[p] = 3
    for _ in range(N):
        changed = False
        for (x, y) in mask:
            best = d[(x, y)]
            for dx, dy, c in ((1, 0, 3), (-1, 0, 3), (0, 1, 3), (0, -1, 3), (1, 1, 4), (-1, -1, 4), (1, -1, 4), (-1, 1, 4)):
                q = (x + dx, y + dy)
                if q in d and d[q] + c < best:
                    best = d[q] + c
            if best < d[(x, y)]:
                d[(x, y)] = best
                changed = True
        if not changed:
            break
    return {p: v / 3.0 for p, v in d.items()}


def light_field(mask: set[tuple[int, int]], light: tuple[float, float, float] = LIGHT, roundness: float = 1.0,
                radius: float | None = None) -> dict[tuple[int, int], float]:
    """
    Helligkeit (-1..1) jedes Pixels einer gewölbten Form, die von {@code light} beleuchtet wird. Die Wölbung folgt dem
    Abstand zum Rand (Querschnitt kreisförmig bis {@code radius}, innen flach).
    """
    d = _distance(mask)
    R = radius if radius is not None else max(d.values(), default=1.0)
    R = max(R, 1.0)
    hgt = {}
    for p, v in d.items():
        t = min(v, R) / R
        hgt[p] = math.sqrt(max(0.0, 1 - (1 - t) ** 2)) * R * roundness
    lx, ly, lz = light
    ln = math.sqrt(lx * lx + ly * ly + lz * lz)
    out = {}
    for (x, y) in mask:
        def h(px: int, py: int) -> float:
            return hgt.get((px, py), -0.35 * R * roundness)
        gx = (h(x + 1, y) - h(x - 1, y)) / 2
        gy = (h(x, y + 1) - h(x, y - 1)) / 2
        nx, ny, nz = -gx, -gy, 1.0
        nn = math.sqrt(nx * nx + ny * ny + nz * nz)
        out[(x, y)] = (nx * lx + ny * ly + nz * lz) / (nn * ln)
    return out


def levels(field: dict[tuple[int, int], float], cuts: Sequence[float] = (0.25, 0.55, 0.8, 0.95),
           low: int = 2) -> dict[tuple[int, int], int]:
    """Helligkeit -> Stufen: unter cuts[0] = low, darüber jeweils eine Stufe mehr."""
    out = {}
    for p, v in field.items():
        lvl = low
        for c in cuts:
            if v >= c:
                lvl += 1
        out[p] = lvl
    return out


def rank_levels(field: dict[tuple[int, int], float], shares: Sequence[float], low: int = 2) -> dict[tuple[int, int], int]:
    """Wie levels(), aber über Anteile: shares[i] = Anteil der Pixel auf Stufe low+i (dunkel -> hell)."""
    order = sorted(field, key=lambda p: (field[p], -p[0] - p[1]))
    out = {}
    total = sum(shares)
    n = len(order)
    acc = 0.0
    bounds = []
    for s in shares:
        acc += s / total
        bounds.append(round(acc * n))
    i = 0
    for k, p in enumerate(order):
        while i < len(bounds) - 1 and k >= bounds[i]:
            i += 1
        out[p] = low + i
    return out


def noise(seed: int, x: int, y: int) -> float:
    """Deterministisches Rauschen -1..1 je Pixel."""
    h = (x * 374761393 + y * 668265263 + seed * 2147483647) & 0xFFFFFFFF
    h = ((h ^ (h >> 13)) * 1274126177) & 0xFFFFFFFF
    return ((h ^ (h >> 16)) & 0xFFFF) / 32767.5 - 1.0


def smooth_noise(seed: int, x: int, y: int, cell: float = 3.0) -> float:
    fx, fy = x / cell, y / cell
    x0, y0 = math.floor(fx), math.floor(fy)
    tx, ty = fx - x0, fy - y0
    tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
    a, b = noise(seed, x0, y0), noise(seed, x0 + 1, y0)
    c, d = noise(seed, x0, y0 + 1), noise(seed, x0 + 1, y0 + 1)
    return (a + (b - a) * tx) * (1 - ty) + (c + (d - c) * tx) * ty


def shaded(mask: set[tuple[int, int]], slot: str, cuts: Sequence[float] = (0.25, 0.55, 0.8, 0.95), low: int = 2,
           light: tuple[float, float, float] = LIGHT, radius: float | None = None, grain: float = 0.0,
           seed: int = 0, cell: float = 2.0) -> Spr:
    """Gewölbte Form in einem Material; {@code grain} streut die Helligkeit (organische Oberflächen)."""
    f = light_field(mask, light, radius=radius)
    if grain:
        f = {p: v + grain * smooth_noise(seed, p[0], p[1], cell) for p, v in f.items()}
    lv = levels(f, cuts, low)
    s = Spr()
    for (x, y), l in lv.items():
        s.set(x, y, (slot, clamp(l)))
    return s


def despeckle(s: Spr, slots: Sequence[str] | None = None, passes: int = 1) -> Spr:
    """Entfernt einzelne Pixel, deren Stufe sich von allen 4 Nachbarn (gleiches Material) unterscheidet."""
    for _ in range(passes):
        out = s.copy()
        for x, y, (slot, lvl) in s.cells():
            if slots is not None and slot not in slots:
                continue
            ns = [s.get(x + dx, y + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))]
            same = [p[1] for p in ns if p is not None and p[0] == slot]
            if len(same) >= 3 and all(v != lvl for v in same):
                # auf den häufigsten Nachbarwert setzen
                out.g[y][x] = (slot, max(set(same), key=same.count))
        s = out
    return s


# =============================================================================
# Vorschau
# =============================================================================

def preview(images: dict[str, Image.Image], path: str, zoom: int = 8, cols: int = 8,
            background: tuple[int, int, int] = (139, 139, 139)) -> None:
    from PIL import ImageDraw

    cell = 16 * zoom + 10
    rows = (len(images) + cols - 1) // cols
    sheet = Image.new("RGB", (cols * cell, rows * (cell + 12)), background)
    d = ImageDraw.Draw(sheet)
    for i, (name, img) in enumerate(images.items()):
        x, y = (i % cols) * cell, (i // cols) * (cell + 12)
        big = img.resize((img.width * zoom, img.height * zoom), Image.NEAREST)
        sheet.paste(big, (x + 5, y + 5), big)
        d.text((x + 4, y + cell - 2), name[: cell // 6], fill=(25, 25, 25))
    sheet.save(path)


def seeded(seed: int) -> random.Random:
    return random.Random(seed)
