#!/usr/bin/env python3
"""
Kleines Pixelkunst-Werkzeug für die Texturen der Mod (16x16, Vanilla-Stil).

Grundsätze, an denen sich alle Texturen orientieren:
  * Kleine Paletten (4-8 Töne pro Material) statt freier Farbverläufe.
  * "Hue shifting": dunkle Töne wandern Richtung Blau/Violett, helle Töne Richtung Gelb.
  * Licht kommt immer von oben links: Kanten oben/links heller, unten/rechts dunkler.
  * Gegenstände bekommen einen 1-Pixel-Rand in einem dunklen Ton der eigenen Farbe (kein reines Schwarz).
  * Blocktexturen sind nahtlos kachelbar (alle Zufallsmuster rechnen "um die Ecke").

Die Funktionen sind bewusst einfach gehalten und werden von tools/generate_textures.py benutzt.
Alle Zufallswerte kommen aus einem festen Startwert (seed), damit jede Textur bei jedem Aufruf
exakt gleich erzeugt wird.

Benötigt: Pillow (pip install pillow)
"""
from __future__ import annotations

import colorsys
import math
import random
from typing import Callable, Iterable, Sequence

from PIL import Image

Color = tuple[int, int, int, int]
CLEAR: Color = (0, 0, 0, 0)
SIZE = 16


# =============================================================================
# Farben
# =============================================================================

def hexc(value: str, alpha: int = 255) -> Color:
    """'#rrggbb' -> (r, g, b, a)."""
    value = value.lstrip("#")
    return int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16), alpha


def to_hex(c: Color) -> str:
    return "#%02x%02x%02x" % c[:3]


def mix(a: Color, b: Color, t: float) -> Color:
    """Lineare Mischung zweier Farben (t=0 -> a, t=1 -> b)."""
    return (round(a[0] + (b[0] - a[0]) * t), round(a[1] + (b[1] - a[1]) * t),
            round(a[2] + (b[2] - a[2]) * t), round(a[3] + (b[3] - a[3]) * t))


def shift(c: Color, hue: float = 0.0, sat: float = 0.0, light: float = 0.0) -> Color:
    """Verschiebt eine Farbe im HSL-Raum (hue in Grad, sat/light als Anteil -1..1)."""
    h, l, s = colorsys.rgb_to_hls(c[0] / 255, c[1] / 255, c[2] / 255)
    h = (h + hue / 360.0) % 1.0
    l = min(1.0, max(0.0, l + light))
    s = min(1.0, max(0.0, s + sat))
    r, g, b = colorsys.hls_to_rgb(h, l, s)
    return round(r * 255), round(g * 255), round(b * 255), c[3]


def _hue_towards(h: float, target: float, amount: float) -> float:
    """Dreht den Farbton h (0..1) um 'amount' (0..1 = bis zu 360°) auf dem kürzesten Weg zu target."""
    d = ((target - h + 0.5) % 1.0) - 0.5
    step = math.copysign(min(abs(d), amount), d)
    return (h + step) % 1.0


def ramp(base: str | Color, steps: int = 5, contrast: float = 0.42, hue_shift: float = 16.0,
         sat_shift: float = 0.08, cool: float = 235.0, warm: float = 55.0) -> list[Color]:
    """
    Erzeugt eine Palette von dunkel nach hell rund um eine Grundfarbe.

    contrast  : gesamte Helligkeitsspanne (0..1)
    hue_shift : wie viele Grad dunkle Töne Richtung 'cool' bzw. helle Töne Richtung 'warm' wandern
    sat_shift : dunkle Töne etwas satter, helle etwas blasser (wie in klassischer Pixelkunst)
    """
    c = hexc(base) if isinstance(base, str) else base
    h, l, s = colorsys.rgb_to_hls(c[0] / 255, c[1] / 255, c[2] / 255)
    out: list[Color] = []
    for i in range(steps):
        t = (i / (steps - 1)) * 2 - 1 if steps > 1 else 0.0  # -1 (dunkel) .. +1 (hell)
        nl = min(0.97, max(0.03, l + t * contrast / 2))
        if t < 0:
            nh = _hue_towards(h, cool / 360.0, -t * hue_shift / 360.0)
            ns = min(1.0, s + (-t) * sat_shift)
        else:
            nh = _hue_towards(h, warm / 360.0, t * hue_shift / 360.0)
            ns = max(0.0, s - t * sat_shift)
        r, g, b = colorsys.hls_to_rgb(nh, nl, ns)
        out.append((round(r * 255), round(g * 255), round(b * 255), 255))
    return out


def palette(*hex_values: str) -> list[Color]:
    """Feste Palette aus Hex-Werten (dunkel -> hell)."""
    return [hexc(v) for v in hex_values]


def pick(pal: Sequence[Color], value: float) -> Color:
    """value 0..1 -> Farbe aus der Palette (dunkel -> hell)."""
    value = max(0.0, min(0.9999, value))
    return pal[int(value * len(pal))]


# Bayer-Matrix für geordnetes Dithering (sanfte Übergänge ohne Rauschen)
BAYER4 = [[0, 8, 2, 10], [12, 4, 14, 6], [3, 11, 1, 9], [15, 7, 13, 5]]


def pick_dither(pal: Sequence[Color], value: float, x: int, y: int, strength: float = 1.0) -> Color:
    """Wie pick(), aber mit geordnetem Dithering zwischen zwei Nachbartönen."""
    n = len(pal)
    v = max(0.0, min(0.9999, value)) * n
    threshold = (BAYER4[y % 4][x % 4] + 0.5) / 16.0
    frac = v - int(v)
    idx = int(v)
    if frac * strength > threshold and idx + 1 < n:
        idx += 1
    return pal[idx]


# =============================================================================
# Bilder
# =============================================================================

def new(fill: Color = CLEAR, w: int = SIZE, h: int = SIZE) -> Image.Image:
    return Image.new("RGBA", (w, h), fill)


def sprite(art: str | Sequence[str], colors: dict[str, Color], w: int = SIZE, h: int = SIZE) -> Image.Image:
    """
    ASCII-Sprite -> Bild. '.' und ' ' sind durchsichtig, jedes andere Zeichen wird über 'colors'
    nachgeschlagen. Zeilen dürfen kürzer als die Bildbreite sein (Rest durchsichtig).
    """
    rows = art.strip("\n").splitlines() if isinstance(art, str) else list(art)
    img = new(w=w, h=h)
    px = img.load()
    for y, row in enumerate(rows[:h]):
        for x, ch in enumerate(row[:w]):
            if ch in ". ":
                continue
            if ch not in colors:
                raise KeyError(f"Farbe für Zeichen {ch!r} fehlt (Zeile {y}: {row!r})")
            px[x, y] = colors[ch]
    return img


def stamp(img: Image.Image, art: str | Sequence[str], colors: dict[str, Color], x0: int, y0: int,
          wrap: bool = True) -> None:
    """Stempelt ein kleines ASCII-Muster in ein Bild (mit Umbruch an den Rändern für Kachelbarkeit)."""
    rows = art.strip("\n").splitlines() if isinstance(art, str) else list(art)
    px = img.load()
    w, h = img.size
    for dy, row in enumerate(rows):
        for dx, ch in enumerate(row):
            if ch in ". ":
                continue
            x, y = x0 + dx, y0 + dy
            if wrap:
                x, y = x % w, y % h
            elif not (0 <= x < w and 0 <= y < h):
                continue
            px[x, y] = colors[ch]


def opaque(img: Image.Image, x: int, y: int) -> bool:
    w, h = img.size
    return 0 <= x < w and 0 <= y < h and img.getpixel((x, y))[3] > 0


def outline(img: Image.Image, color: Color | Callable[[Color], Color], diagonal: bool = False) -> Image.Image:
    """
    Zeichnet einen 1-Pixel-Rand AUSSEN um alle sichtbaren Pixel.
    color kann eine feste Farbe oder eine Funktion (Nachbarfarbe -> Randfarbe) sein.
    """
    out = img.copy()
    px = out.load()
    w, h = img.size
    dirs = [(1, 0), (-1, 0), (0, 1), (0, -1)]
    if diagonal:
        dirs += [(1, 1), (1, -1), (-1, 1), (-1, -1)]
    for y in range(h):
        for x in range(w):
            if opaque(img, x, y):
                continue
            for dx, dy in dirs:
                if opaque(img, x + dx, y + dy):
                    src = img.getpixel((x + dx, y + dy))
                    px[x, y] = color(src) if callable(color) else color
                    break
    return out


def edge_shade(img: Image.Image, light: Callable[[Color], Color], dark: Callable[[Color], Color]) -> Image.Image:
    """
    Innenschattierung einer Form: Pixel mit freiem Nachbarn oben/links werden aufgehellt,
    Pixel mit freiem Nachbarn unten/rechts abgedunkelt (Licht von oben links).
    """
    out = img.copy()
    px = out.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            if not opaque(img, x, y):
                continue
            c = img.getpixel((x, y))
            if not opaque(img, x + 1, y) or not opaque(img, x, y + 1):
                px[x, y] = dark(c)
            elif not opaque(img, x - 1, y) or not opaque(img, x, y - 1):
                px[x, y] = light(c)
    return out


def recolor(img: Image.Image, mapping: dict[Color, Color]) -> Image.Image:
    """Ersetzt Farben 1:1 (z. B. um eine Form in einer anderen Palette zu verwenden)."""
    out = img.copy()
    px = out.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            c = px[x, y]
            if c in mapping:
                px[x, y] = mapping[c]
    return out


def flip_x(img: Image.Image) -> Image.Image:
    return img.transpose(Image.FLIP_LEFT_RIGHT)


def compose(*layers: Image.Image) -> Image.Image:
    """Legt mehrere Ebenen übereinander (erste Ebene unten)."""
    base = layers[0].copy()
    for layer in layers[1:]:
        base.alpha_composite(layer)
    return base


# =============================================================================
# Kachelbares Rauschen
# =============================================================================

def value_noise(seed: int, cell: int = 4, w: int = SIZE, h: int = SIZE) -> list[list[float]]:
    """Weiches, kachelbares Rauschen (0..1). cell muss w und h teilen."""
    rng = random.Random(seed)
    nx, ny = w // cell, h // cell
    grid = [[rng.random() for _ in range(nx)] for _ in range(ny)]
    out = [[0.0] * w for _ in range(h)]
    for y in range(h):
        for x in range(w):
            gx, gy = x / cell, y / cell
            x0, y0 = int(gx) % nx, int(gy) % ny
            x1, y1 = (x0 + 1) % nx, (y0 + 1) % ny
            tx, ty = gx - int(gx), gy - int(gy)
            tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
            a = grid[y0][x0] + (grid[y0][x1] - grid[y0][x0]) * tx
            b = grid[y1][x0] + (grid[y1][x1] - grid[y1][x0]) * tx
            out[y][x] = a + (b - a) * ty
    return out


def fractal_noise(seed: int, octaves: Iterable[tuple[int, float]] = ((8, 0.5), (4, 0.3), (2, 0.2)),
                  w: int = SIZE, h: int = SIZE) -> list[list[float]]:
    """Mehrere Rausch-Ebenen übereinander (cell, Gewicht), normiert auf 0..1."""
    total = [[0.0] * w for _ in range(h)]
    weight = 0.0
    for i, (cell, wgt) in enumerate(octaves):
        layer = value_noise(seed * 31 + i * 7919, cell, w, h)
        for y in range(h):
            for x in range(w):
                total[y][x] += layer[y][x] * wgt
        weight += wgt
    lo = min(min(r) for r in total)
    hi = max(max(r) for r in total)
    span = (hi - lo) or 1.0
    return [[(v - lo) / span for v in row] for row in total]


def voronoi(seed: int, points: int, w: int = SIZE, h: int = SIZE, jitter: float = 1.0,
            metric: str = "euclid") -> tuple[list[list[int]], list[list[float]], list[tuple[float, float]]]:
    """
    Kachelbare Voronoi-Zellen.
    Rückgabe: (Zellnummer je Pixel, Abstand zur nächsten Zellgrenze je Pixel, Zellmittelpunkte)
    """
    rng = random.Random(seed)
    pts = [(rng.random() * w, rng.random() * h) for _ in range(points)]
    cell = [[0] * w for _ in range(h)]
    border = [[0.0] * w for _ in range(h)]

    def dist(ax, ay, bx, by):
        dx = abs(ax - bx)
        dy = abs(ay - by)
        dx, dy = min(dx, w - dx), min(dy, h - dy)
        if metric == "manhattan":
            return dx + dy
        if metric == "cheby":
            return max(dx, dy)
        return math.hypot(dx, dy)

    for y in range(h):
        for x in range(w):
            ds = sorted((dist(x + 0.5, y + 0.5, px_, py_), i) for i, (px_, py_) in enumerate(pts))
            cell[y][x] = ds[0][1]
            border[y][x] = ds[1][0] - ds[0][0]
    return cell, border, pts


# =============================================================================
# Bausteine für Blocktexturen
# =============================================================================

def bricks(rows: Sequence[tuple[int, Sequence[int], int]], w: int = SIZE) -> list[list[tuple[int, int, int]]]:
    """
    Teilt die Fläche in Steine auf. rows = [(Zeilenhöhe, [Steinbreiten...], Versatz), ...]
    (Summe der Breiten je Zeile = w, Summe der Höhen = Bildhöhe).
    Rückgabe je Pixel: (Stein-ID, x im Stein, y im Stein) bzw. (-1, ...) für Fugen.
    Die Fuge liegt jeweils in der letzten Zeile/Spalte eines Steins, damit das Muster kachelt.
    """
    h = sum(r[0] for r in rows)
    grid = [[(-1, 0, 0)] * w for _ in range(h)]
    stone_id = 0
    y0 = 0
    for height, widths, offset in rows:
        x = offset
        for width in widths:
            for dy in range(height):
                for dx in range(width):
                    gx = (x + dx) % w
                    gy = y0 + dy
                    if dy == height - 1 or dx == width - 1:
                        grid[gy][gx] = (-1, dx, dy)
                    else:
                        grid[gy][gx] = (stone_id, dx, dy)
            stone_id += 1
            x += width
        y0 += height
    return grid


def stone_dims(grid: list[list[tuple[int, int, int]]]) -> dict[int, tuple[int, int]]:
    """Größe (Breite, Höhe) jedes Steins ohne Fuge."""
    dims: dict[int, tuple[int, int]] = {}
    for row in grid:
        for sid, dx, dy in row:
            if sid >= 0:
                w, h = dims.get(sid, (0, 0))
                dims[sid] = (max(w, dx + 1), max(h, dy + 1))
    return dims


# =============================================================================
# Vorschau-Hilfen
# =============================================================================

def zoom(img: Image.Image, factor: int) -> Image.Image:
    return img.resize((img.width * factor, img.height * factor), Image.NEAREST)


def tiled(img: Image.Image, n: int = 3) -> Image.Image:
    out = new(w=img.width * n, h=img.height * n)
    for y in range(n):
        for x in range(n):
            out.alpha_composite(img, (x * img.width, y * img.height))
    return out


def checker(w: int, h: int, a: Color = (120, 120, 120, 255), b: Color = (98, 98, 98, 255), cell: int = 1) -> Image.Image:
    img = new(w=w, h=h)
    px = img.load()
    for y in range(h):
        for x in range(w):
            px[x, y] = a if ((x // cell) + (y // cell)) % 2 else b
    return img
