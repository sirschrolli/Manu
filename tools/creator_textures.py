#!/usr/bin/env python3
"""
Texturen zu Schrolli dem Schöpfer (0.75):

* der Himmel der Schöpfung (textures/environment/creation_sky.png, 4×4 Felder je 256 Texel, additiv): Sonnenrad, zwei
  Nebel, eine Galaxie, drei ferne Welten, Strahlenbündel, ein Riss aus Licht, das Auge der Schöpfung, Sternhaufen und der
  Lichthof des Sonnenrads;
* Lichthof und vierzackiger Stern der Geschosse, die weißen Leuchtbilder Schrollis (aus seinen Texturen abgeleitet);
* die Teilchen „Göttlicher Funke“ und „Goldene Feder“;
* der Altar des Schöpfers (Sockel, Säule, Sonnenscheibe, Funke);
* Schöpferfunke, Zepter (Bild und 3D-Atlas), Schwingen (Bild und getragen).
"""
from __future__ import annotations

import math
import random
from pathlib import Path
from typing import Callable

import numpy as np
from PIL import Image, ImageFilter

import item_style as st

ROOT = Path(__file__).resolve().parent.parent
ENTITY = ROOT / "src/main/resources/assets/kingdomsatwar/textures/entity/schrolli"
CLEAR = (0, 0, 0, 0)

META = {"environment/creation_sky": {"texture": {"blur": True, "clamp": True}}}

GOLD = st.ramp("#e8b23a", dark=0.22, light=1.0, warm=40)
WHITE = st.ramp("#f2eefa", dark=0.42, light=1.0, cool=270)
LIGHT = st.fixed("#c8902a", "#ecb846", "#ffd870", "#ffe8a0", "#fff4cc", "#fffbea", "#ffffff")
SKY_BLUE = st.ramp("#4a8ae8", dark=0.25, light=1.0)


def _rng(seed: int) -> random.Random:
    return random.Random(seed)


# =============================================================================
# Hilfen für weiche, leuchtende Bilder (numpy, additiv: Schwarz = durchsichtig)
# =============================================================================

def _grid(n: int):
    y, x = np.mgrid[0:n, 0:n].astype(np.float32)
    c = (n - 1) / 2.0
    return (x - c) / c, (y - c) / c


def _noise(n: int, scale: float, seed: int, octaves: int = 4) -> np.ndarray:
    """Glattes Wertrauschen 0..1 (mehrere Oktaven)."""
    rng = np.random.default_rng(seed)
    out = np.zeros((n, n), np.float32)
    amp = 1.0
    total = 0.0
    for o in range(octaves):
        cells = max(2, int(scale * (2 ** o)))
        g = rng.random((cells + 1, cells + 1)).astype(np.float32)
        img = Image.fromarray((g * 255).astype(np.uint8), "L").resize((n, n), Image.BICUBIC)
        out += np.asarray(img, np.float32) / 255.0 * amp
        total += amp
        amp *= 0.5
    return out / total


def _rgb(arr: np.ndarray) -> Image.Image:
    """float RGB (0..1+) -> Bild (additiv: Alpha = Helligkeit, für Blending mit Schwarz unwichtig)."""
    a = np.clip(np.nan_to_num(arr, nan=0.0), 0.0, 1.0)
    img = (a * 255).astype(np.uint8)
    return Image.fromarray(img, "RGB").convert("RGBA")


def _col(hexs: str) -> np.ndarray:
    hexs = hexs.lstrip("#")
    return np.array([int(hexs[i:i + 2], 16) / 255.0 for i in (0, 2, 4)], np.float32)


def _cell_fade(x: np.ndarray, y: np.ndarray, start: float = 0.82) -> np.ndarray:
    """Zum Rand des Felds hin auf Schwarz (keine Kanten am Himmel)."""
    r = np.maximum(np.abs(x), np.abs(y))
    return np.clip((1.0 - r) / (1.0 - start), 0.0, 1.0)


# =============================================================================
# Der Himmel der Schöpfung
# =============================================================================

def sky_sunwheel(n: int) -> np.ndarray:
    """Das Sonnenrad: ein goldener Kranz mit 24 Strahlen, Zeichen im Ring, innen eine weiße Sonne."""
    x, y = _grid(n)
    r = np.sqrt(x * x + y * y)
    a = np.arctan2(y, x)
    out = np.zeros((n, n, 3), np.float32)
    gold = _col("#ffc84a")
    white = _col("#fff8e0")
    # Sonne
    core = np.exp(-(r / 0.16) ** 2) * 1.3
    out += core[..., None] * white
    # Kranz
    for rad, w, k in ((0.42, 0.035, 1.0), (0.5, 0.012, 0.7), (0.34, 0.01, 0.6)):
        ring = np.exp(-((r - rad) / w) ** 2) * k
        out += ring[..., None] * gold
    # Zeichen im Ring (kurze Striche)
    marks = (np.abs(np.sin(a * 36.0)) > 0.92) & (np.abs(r - 0.46) < 0.03)
    out += marks[..., None] * gold * 0.8
    # Strahlen: lang und kurz im Wechsel
    rays = np.abs(np.cos(a * 12.0)) ** 40 * np.clip(1.0 - (r - 0.5) / 0.45, 0, 1) * (r > 0.5)
    rays2 = np.abs(np.cos(a * 12.0 + math.pi / 2)) ** 60 * np.clip(1.0 - (r - 0.5) / 0.25, 0, 1) * (r > 0.5)
    out += (rays * 0.9 + rays2 * 0.6)[..., None] * gold
    # weicher Schein
    out += (np.exp(-(r / 0.6) ** 2) * 0.25)[..., None] * gold
    return out * _cell_fade(x, y, 0.9)[..., None]


def sky_glow(n: int, color: str, power: float = 2.0) -> np.ndarray:
    x, y = _grid(n)
    r = np.sqrt(x * x + y * y)
    g = np.clip(1.0 - r, 0.0, 1.0) ** power
    return g[..., None] * _col(color)


def sky_nebula(n: int, seed: int, colors: tuple[str, str, str]) -> np.ndarray:
    x, y = _grid(n)
    r = np.sqrt(x * x * 0.8 + y * y * 1.2)
    n1 = _noise(n, 3.0, seed)
    n2 = _noise(n, 6.0, seed + 1)
    shape = np.clip(1.0 - r, 0.0, 1.0) ** 1.4
    dens = np.clip((n1 * 0.7 + n2 * 0.5 - 0.42) * 2.2, 0.0, 1.0) * shape
    c1, c2, c3 = (_col(c) for c in colors)
    mixk = np.clip(n2 * 1.4 - 0.2, 0, 1)[..., None]
    col = c1 * (1 - mixk) + c2 * mixk
    out = dens[..., None] * col
    # Sterne darin
    rng = np.random.default_rng(seed + 7)
    stars = rng.random((n, n)) > 0.9975
    out += (stars * shape * 1.2)[..., None] * _col("#ffffff")
    out += (np.clip(dens - 0.6, 0, 1) * 0.8)[..., None] * c3
    return out * _cell_fade(x, y)[..., None]


def sky_galaxy(n: int) -> np.ndarray:
    x, y = _grid(n)
    r = np.sqrt(x * x + y * y)
    a = np.arctan2(y, x)
    arms = 0.5 + 0.5 * np.cos(2.0 * (a - np.log(r + 0.05) * 3.2))
    dens = arms ** 3 * np.exp(-(r / 0.55) ** 2)
    core = np.exp(-(r / 0.1) ** 2) * 1.4
    noise = _noise(n, 8.0, 77)
    out = (dens * (0.6 + noise * 0.6))[..., None] * _col("#b8a0ff") + core[..., None] * _col("#fff0d0")
    rng = np.random.default_rng(78)
    stars = (rng.random((n, n)) > 0.996) * np.exp(-(r / 0.8) ** 2)
    out += stars[..., None] * 1.0
    return out * _cell_fade(x, y)[..., None]


def sky_world(n: int, base: str, light: str, seed: int, ring: bool = False) -> np.ndarray:
    x, y = _grid(n)
    r = np.sqrt(x * x + y * y)
    rad = 0.5
    inside = r < rad
    z = np.sqrt(np.clip(rad * rad - r * r, 0, None)) / rad
    land = _noise(n, 5.0, seed) > 0.52
    shade = np.clip(0.25 + 0.75 * (x * -0.5 + y * -0.5 + z * 0.8), 0.05, 1.2)
    col = np.where(land[..., None], _col(light), _col(base))
    out = (inside * shade)[..., None] * col
    # Atmosphäre
    rim = np.exp(-((r - rad) / 0.05) ** 2) * 0.8 + np.exp(-((r - rad) / 0.2) ** 2) * 0.3
    out += rim[..., None] * _col(light)
    if ring:
        rr = np.sqrt((x / 1.0) ** 2 + (y / 0.28) ** 2)
        band = np.exp(-((rr - 0.8) / 0.06) ** 2) * ((r > rad) | (y > 0))
        out += band[..., None] * _col("#ffe8b0") * 0.8
    return out * _cell_fade(x, y, 0.9)[..., None]


def sky_rays(n: int) -> np.ndarray:
    """Ein Fächer aus Lichtstrahlen, der aus dem Horizont aufsteigt."""
    x, y = _grid(n)
    out = np.zeros((n, n, 3), np.float32)
    origin_y = 1.1
    a = np.arctan2(x, origin_y - y)
    d = np.sqrt(x * x + (origin_y - y) ** 2)
    rng = np.random.default_rng(91)
    for k in range(13):
        ang = -0.9 + 1.8 * k / 12 + rng.normal(0, 0.03)
        w = 0.02 + rng.random() * 0.03
        beam = np.exp(-((a - ang) / w) ** 2) * np.clip(1.0 - d / 2.0, 0, 1) * (0.5 + rng.random() * 0.6)
        out += beam[..., None] * _col("#ffe0a0")
    fade = np.clip((y + 1.0) / 0.4, 0, 1)
    return out * fade[..., None] * _cell_fade(x, y, 0.85)[..., None]


def sky_rift(n: int) -> np.ndarray:
    """Ein Riss aus weißem Licht mit violettem Rand."""
    x, y = _grid(n)
    rng = np.random.default_rng(55)
    pts = [(-0.85, -0.2)]
    for i in range(10):
        pts.append((-0.85 + 1.7 * (i + 1) / 10, pts[-1][1] * 0.6 + rng.normal(0, 0.18)))
    dist = np.full((n, n), 9.0, np.float32)
    for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
        dx, dy = x1 - x0, y1 - y0
        t = np.clip(((x - x0) * dx + (y - y0) * dy) / (dx * dx + dy * dy), 0, 1)
        dist = np.minimum(dist, np.sqrt((x - x0 - t * dx) ** 2 + (y - y0 - t * dy) ** 2))
    width = 0.03 * (1.0 - np.abs(x) * 0.8)
    core = np.exp(-(dist / np.maximum(width, 0.004)) ** 2) * 1.4
    halo = np.exp(-(dist / 0.12) ** 2) * 0.6
    out = core[..., None] * _col("#ffffff") + halo[..., None] * _col("#c080ff")
    return out * _cell_fade(x, y)[..., None]


def sky_eye(n: int) -> np.ndarray:
    """Das Auge der Schöpfung: eine Iris aus Sternen und Gold um eine Pupille aus tiefem All, Wimpern aus Licht."""
    x, y = _grid(n)
    r = np.sqrt(x * x + y * y)
    a = np.arctan2(y, x)
    # Lid (Mandelform)
    cosx = np.clip(np.cos(np.clip(x, -1, 1) * math.pi / 2), 0.0, 1.0)
    lid = (np.abs(y) < 0.55 * cosx ** 0.8)
    out = np.zeros((n, n, 3), np.float32)
    iris = (r < 0.42) & lid
    streak = 0.5 + 0.5 * np.cos(a * 40.0 + _noise(n, 10.0, 31) * 6.0)
    irisc = np.where((r < 0.12)[..., None], np.array([0.02, 0.0, 0.05], np.float32),
                     (_col("#ffd060") * (0.4 + 0.6 * streak[..., None]) * np.clip(1.0 - r / 0.42, 0.2, 1.0)[..., None]
                      + _col("#a060ff") * (r / 0.42)[..., None] * 0.8))
    out += iris[..., None] * irisc
    white = lid & (r >= 0.42)
    out += white[..., None] * _col("#e8e0ff") * 0.35 * np.clip(1.0 - np.abs(x), 0, 1)[..., None]
    # Lidrand und Wimpern aus Licht
    edge_d = np.abs(np.abs(y) - 0.55 * cosx ** 0.8)
    out += (np.exp(-(edge_d / 0.02) ** 2) * (np.abs(x) < 0.98))[..., None] * _col("#fff4d0")
    lashes = (np.abs(np.sin(x * 28.0)) > 0.9) & (np.abs(y) > 0.5 * cosx ** 0.8) & (np.abs(y) < 0.75 * cosx ** 0.6)
    out += lashes[..., None] * _col("#ffd890") * 0.6
    out += (np.exp(-(r / 0.08) ** 2) * 0.6)[..., None] * _col("#ffffff")
    out += (np.exp(-((r - 0.12) / 0.015) ** 2) * 0.9)[..., None] * _col("#ffffff")
    return out * _cell_fade(x, y, 0.92)[..., None]


def sky_stars(n: int, seed: int) -> np.ndarray:
    x, y = _grid(n)
    rng = np.random.default_rng(seed)
    out = np.zeros((n, n, 3), np.float32)
    r = np.sqrt(x * x + y * y)
    dens = np.exp(-(r / 0.6) ** 2)
    for _ in range(260):
        px, py = rng.normal(0, 0.4, 2)
        if abs(px) > 0.9 or abs(py) > 0.9:
            continue
        s = 0.006 + rng.random() * 0.012
        b = 0.4 + rng.random() * 0.8
        col = _col(rng.choice(["#ffffff", "#fff0c0", "#c8d8ff", "#ffd8f0"]))
        out += (np.exp(-((x - px) ** 2 + (y - py) ** 2) / (s * s)) * b)[..., None] * col
    out += (dens * 0.12)[..., None] * _col("#b090ff")
    return out * _cell_fade(x, y)[..., None]


def creation_sky() -> Image.Image:
    n = 256
    cells = {
        0: sky_sunwheel(n),
        1: sky_nebula(n, 11, ("#8a2ab0", "#e04aa8", "#ffd0f0")) * 1.8,
        2: sky_nebula(n, 23, ("#2a5ad0", "#30c8e8", "#d0f8ff")) * 1.8,
        3: sky_galaxy(n),
        4: sky_world(n, "#1a3a8a", "#ffd870", 41, ring=True),
        5: sky_world(n, "#5a1408", "#ff8a30", 42),
        6: sky_world(n, "#103a7a", "#5ac85a", 43),
        7: sky_rays(n),
        8: sky_rift(n),
        9: sky_eye(n),
        10: sky_rift(n)[:, ::-1],
        11: sky_stars(n, 61),
        12: sky_glow(n, "#ffc860", 2.4) * 0.9,
    }
    atlas = np.zeros((n * 4, n * 4, 3), np.float32)
    for i, arr in cells.items():
        cx, cy = (i % 4) * n, (i // 4) * n
        atlas[cy:cy + n, cx:cx + n] = arr
    return _rgb(atlas)


# =============================================================================
# Lichthof, Stern, Leuchtbilder
# =============================================================================

def glow_disc() -> Image.Image:
    n = 64
    x, y = _grid(n)
    r = np.sqrt(x * x + y * y)
    g = np.clip(1.0 - r, 0, 1) ** 2.2
    arr = g[..., None] * _col("#ffffff")
    img = _rgb(arr)
    a = (np.clip(g, 0, 1) * 255).astype(np.uint8)
    img.putalpha(Image.fromarray(a, "L"))
    return img


def star4() -> Image.Image:
    n = 64
    x, y = _grid(n)
    r = np.sqrt(x * x + y * y)
    spikes = np.exp(-(np.abs(x) / 0.06) ** 1.2) * np.clip(1 - np.abs(y), 0, 1) ** 2 \
        + np.exp(-(np.abs(y) / 0.06) ** 1.2) * np.clip(1 - np.abs(x), 0, 1) ** 2
    diag = (np.exp(-(np.abs(x - y) / 0.05) ** 1.2) + np.exp(-(np.abs(x + y) / 0.05) ** 1.2)) * np.clip(1 - r, 0, 1) ** 3 * 0.45
    core = np.exp(-(r / 0.18) ** 2)
    v = np.clip(spikes + diag + core, 0, 1.5)
    arr = v[..., None] * _col("#fff6d8")
    img = _rgb(arr)
    img.putalpha(Image.fromarray((np.clip(v, 0, 1) * 255).astype(np.uint8), "L"))
    return img


def flash_of(name: str) -> Image.Image | None:
    """Ein weißes Leuchtbild aus einer Textur: jedes sichtbare Texel weiß (mit seiner Deckkraft)."""
    src = ENTITY / f"{name}.png"
    if not src.exists():
        return None
    img = Image.open(src).convert("RGBA")
    a = img.getchannel("A")
    out = Image.new("RGBA", img.size, (255, 255, 255, 0))
    out.putalpha(a)
    return out


# =============================================================================
# Teilchen
# =============================================================================

def spark(frame: int) -> Image.Image:
    """Göttlicher Funke: ein vierzackiger goldener Stern (vier Bilder: groß -> klein)."""
    n = 16
    img = Image.new("RGBA", (n, n), CLEAR)
    px = img.load()
    arm = [6, 5, 3, 2][frame]
    c = 7.5
    for yy in range(n):
        for xx in range(n):
            dx, dy = abs(xx - c), abs(yy - c)
            d = min(dx, dy)
            m = max(dx, dy)
            if d < 0.6 and m <= arm + 0.5 or dx + dy < 2.2 - frame * 0.3:
                v = 1.0 - m / (arm + 1.5)
                col = LIGHT[min(6, 3 + int(v * 4))]
                px[xx, yy] = col
    return img


def feather(frame: int) -> Image.Image:
    """Goldene Feder: weiß mit goldenem Kiel und goldener Spitze, schräg (vier Bilder, leicht gedreht)."""
    art = """
................
...........gG...
..........gwG...
.........gwwG...
........gwwwg...
.......gwwwg....
......gwwwg.....
.....gwwwg......
....gwwwg.......
...gwwwg........
...gwwg.........
..gwg...........
..k.............
.k..............
................
................
"""
    s = st.Spr.art(art, legend={"w": ("A", 6), "g": ("B", 4), "G": ("B", 6), "k": ("B", 2)})
    s = s.outline(prefer=("B", "A"))
    img = s.image({"A": WHITE, "B": GOLD})
    return img.rotate(frame * 25, resample=Image.NEAREST)


# =============================================================================
# Der Altar des Schöpfers
# =============================================================================

def _quartz(x: int, y: int, seed: int) -> tuple:
    r = _rng(seed * 1000 + y * 16 + x)
    v = 0.86 + r.random() * 0.08
    base = st.hexc("#f2eef6")
    return tuple(int(base[i] * v) for i in range(3)) + (255,)


def altar_side() -> Image.Image:
    """Der Sockel: weißer Quarz, unten ein Goldband, darüber eine eingelegte goldene Sonne."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = _quartz(x, y, 1)
    for x in range(16):
        px[x, 15] = GOLD[2]
        px[x, 14] = GOLD[4]
        px[x, 13] = GOLD[5] if x % 3 else GOLD[6]
        px[x, 12] = GOLD[3]
        px[x, 0] = GOLD[5]
    # eine kleine Sonne mittig
    for y in range(3, 11):
        for x in range(4, 12):
            dx, dy = x - 7.5, y - 6.5
            r = math.hypot(dx, dy)
            if r < 1.6:
                px[x, y] = LIGHT[5]
            elif r < 2.4:
                px[x, y] = GOLD[5]
            elif r < 3.8 and (abs(dx) < 0.6 or abs(dy) < 0.6 or abs(abs(dx) - abs(dy)) < 0.6):
                px[x, y] = GOLD[4]
    return img


def altar_top() -> Image.Image:
    """Die Sonnenscheibe obenauf: Gold mit zwölf Strahlen, die Mitte hell."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            r = math.hypot(dx, dy)
            a = math.atan2(dy, dx)
            ray = abs(math.cos(a * 6.0)) > 0.8
            if r < 2.2:
                c = LIGHT[6] if r < 1.2 else LIGHT[5]
            elif r < 3.0:
                c = GOLD[6]
            elif r < 7.2 and ray:
                c = GOLD[5]
            elif r < 7.6:
                c = GOLD[3]
            else:
                c = GOLD[2] if (x + y) % 2 else GOLD[3]
            px[x, y] = c
    for i in range(16):
        px[i, 0] = GOLD[4]
        px[i, 15] = GOLD[1]
        px[0, i] = GOLD[4]
        px[15, i] = GOLD[1]
    return img


def altar_column() -> Image.Image:
    """Die Säule: weißer Quarz mit Kanneluren und goldenen Linien."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            c = _quartz(x, y, 2)
            if x % 4 == 0:
                c = tuple(int(v * 0.86) for v in c[:3]) + (255,)
            if x % 8 == 2:
                c = GOLD[4]
            px[x, y] = c
    return img


def altar_bottom() -> Image.Image:
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = _quartz(x, y, 3)
    return img


def altar_spark() -> Image.Image:
    """Der Funke im Altar: ein gleißender Kristall aus goldenem Licht."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            r = math.hypot(dx, dy)
            v = 1.0 - r / 11.0
            facet = (x + y) % 5 == 0 or (x - y) % 7 == 0
            px[x, y] = LIGHT[min(6, max(1, int(v * 7) + (1 if facet else 0)))]
    return img


# =============================================================================
# Gegenstände
# =============================================================================

def spark_item() -> Image.Image:
    """Der Schöpferfunke: ein gleißender, vierzackiger Stern aus goldenem Licht, darum kleine Funken."""
    art = """
................
.......7........
.......7........
......676.......
......676.......
.....56765......
.775566766557...
77766677766677..
.775566766557...
.....56765......
......676.......
......676.......
.......7........
.......7........
................
................
"""
    leg = {"5": ("G", 3), "6": ("G", 5), "7": ("G", 6)}
    s = st.Spr.art(art, legend=leg)
    s.set(3, 3, ("G", 4))
    s.set(12, 12, ("G", 4))
    s.set(12, 2, ("G", 3))
    s = s.outline(prefer=("G",))
    return s.image({"G": LIGHT})


def scepter_item() -> Image.Image:
    """Das Zepter des Schöpfers: ein Goldstab schräg, oben ein Sonnenstern mit blauem Stein."""
    art = """
...........L....
..........LgL...
.......L.LgSgL..
........LgSSSgL.
........gSbbSg..
.......LgSbbSgL.
........LgSSgL..
.......d.LgL.L..
......dD...L....
.....dD.........
....dD..........
...wD...........
..dD............
.dD.............
.D..............
................
"""
    leg = {"L": ("G", 6), "g": ("B", 6), "S": ("B", 5), "b": ("C", 5), "d": ("B", 3), "D": ("B", 5), "w": ("A", 6)}
    s = st.Spr.art(art, legend=leg)
    s = s.outline(prefer=("B", "G", "C", "A"))
    return s.image({"A": WHITE, "B": GOLD, "C": SKY_BLUE, "G": LIGHT})


def wings_item() -> Image.Image:
    """Die Schwingen des Schöpfers: zwei weiße Flügel, oben verbunden, mit Federreihen und goldenen Spitzen."""
    art = """
................
......gGGg......
.....gwwwwg.....
....gwwgGwwg....
...gwwg..gwwg...
..gwwWg..gWwwg..
..gwwwg..gwwwg..
.gwWwwg..gwwWwg.
.gwwwWg..gWwwwg.
.gwWwwg..gwwWwg.
.gwwwg....gwwwg.
.gwwgG....Ggwwg.
..gGG......GGg..
..G..........G..
................
................
"""
    leg = {"w": ("A", 6), "W": ("A", 4), "g": ("B", 4), "G": ("B", 6)}
    s = st.Spr.art(art, legend=leg)
    s = s.outline(prefer=("A", "B"))
    return s.image({"A": WHITE, "B": GOLD})


def scepter_model() -> tuple[Image.Image, Image.Image]:
    """Atlas (32×32) des Zepters in der Hand: Gold (0–8), weiße Bänder (8–12), Licht (12–24), blauer Stein (24–32)."""
    img = Image.new("RGBA", (32, 32), CLEAR)
    glow = Image.new("RGBA", (32, 32), CLEAR)
    px = img.load()
    gx = glow.load()
    for y in range(32):
        for x in range(32):
            if x < 8:
                c = GOLD[3 + (x + y) % 3]
            elif x < 12:
                c = WHITE[5 + (y % 2)]
            elif x < 24:
                c = LIGHT[4 + (x + y) % 3]
                gx[x, y] = c
            else:
                c = SKY_BLUE[3 + (x + y) % 4]
                gx[x, y] = c
            px[x, y] = c
    return img, glow


def creator_wings() -> Image.Image:
    """Die getragenen Schwingen (Rückseite des Elytren-Quaders: 10 × 20 ab 36, 2): weiße Federn in Reihen, goldene Spitzen."""
    img = Image.new("RGBA", (64, 32), CLEAR)
    px = img.load()
    x0, y0 = 36, 2

    def inside(x: float, y: float) -> bool:
        lead = x * 0.35
        trail = 19.5 - x * 0.85 - 1.2 * math.sin(math.pi * min(x, 9.5) / 9.5)
        return lead - 0.3 <= y <= trail and x <= 9.9

    for x in range(10):
        for y in range(20):
            if not inside(x + 0.5, y + 0.5):
                continue
            # Federn: Reihen schräg, nach unten länger; die Spitzen golden
            trail = 19.5 - (x + 0.5) * 0.85 - 1.2 * math.sin(math.pi * min(x + 0.5, 9.5) / 9.5)
            tip = trail - (y + 0.5) < 2.2
            row = (y + x // 2) % 4
            c = WHITE[6] if row else WHITE[4]
            if x == 0 or (y + 0.5) - (x + 0.5) * 0.35 < 1.0:
                c = GOLD[5]                  # Vorderkante
            if tip:
                c = GOLD[6] if (x + y) % 2 else GOLD[4]
            px[x0 + x, y0 + y] = c
    for x in range(10):
        px[24 + x, 0] = GOLD[5]
        px[24 + x, 1] = GOLD[4]
    for y in range(20):
        px[22, 2 + y] = WHITE[4] if y < 14 else CLEAR
        px[34, 2 + y] = GOLD[4] if y < 6 else CLEAR
        px[35, 2 + y] = GOLD[4] if y < 6 else CLEAR
    return img


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("environment/creation_sky", creation_sky())
    put("entity/schrolli/glow_disc", glow_disc())
    put("entity/schrolli/star4", star4())
    for name in ("schrolli", "schrolli_true", "schrolli_almighty"):
        img = flash_of(name)
        if img is not None:
            put(f"entity/schrolli/{name}_flash", img)
    for i in range(4):
        put(f"particle/divine_spark_{i}", spark(i))
        put(f"particle/divine_feather_{i}", feather(i))
    put("block/creator_altar_side", altar_side())
    put("block/creator_altar_top", altar_top())
    put("block/creator_altar_column", altar_column())
    put("block/creator_altar_bottom", altar_bottom())
    put("block/creator_altar_spark", altar_spark())
    put("item/creator_spark", spark_item())
    # Speer, Schwingen, Heiligenschein und Räder malt seit 0.78 tools/creator_gear.py


if __name__ == "__main__":
    import json
    import sys

    out = ROOT / "src/main/resources/assets/kingdomsatwar/textures"

    def _put(key: str, img: Image.Image) -> None:
        target = out / f"{key}.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        img.save(target)

    build(_put)
    for key, meta in META.items():
        (out / f"{key}.png.mcmeta").write_text(json.dumps(meta, indent=2) + "\n", encoding="utf-8")
    print("Texturen der Schöpfung geschrieben")
