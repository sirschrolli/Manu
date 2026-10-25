"""
Bemalt die Textur eines Mob-Modells (siehe geometry.py).

Jeder Quader hat ein "Material" (z. B. scales, belly, membrane, horn, eye). Für jedes Texel wird der
zugehörige Punkt im Modell berechnet; darauf beruhen durchgehende Muster über Quadergrenzen hinweg
(z. B. Schuppen als 3D-Voronoi-Zellen). Farben kommen aus benannten Farbverläufen (Rampen) je Variante.
"""
from __future__ import annotations

import colorsys
import math
import random
from typing import Callable

from PIL import Image

from .geometry import Cube, Model, face_rects, texel_to_local

RGBA = tuple[int, int, int, int]


# =============================================================================================
# Farben
# =============================================================================================

def hexc(s: str) -> RGBA:
    s = s.lstrip("#")
    return int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), 255


def ramp(dark: str, light: str, steps: int = 6, hue_shift: float = 0.04) -> list[RGBA]:
    """Farbverlauf dunkel -> hell mit leichter Farbton-Verschiebung (Schatten kühler, Lichter wärmer)."""
    a, b = hexc(dark), hexc(light)
    out = []
    for i in range(steps):
        t = i / (steps - 1)
        r = a[0] + (b[0] - a[0]) * t
        g = a[1] + (b[1] - a[1]) * t
        bl = a[2] + (b[2] - a[2]) * t
        h, l, s = colorsys.rgb_to_hls(r / 255, g / 255, bl / 255)
        h = (h + (t - 0.5) * -hue_shift) % 1.0
        r2, g2, b2 = colorsys.hls_to_rgb(h, l, s)
        out.append((round(r2 * 255), round(g2 * 255), round(b2 * 255), 255))
    return out


def pick(r: list[RGBA], v: float) -> RGBA:
    v = max(0.0, min(0.9999, v))
    return r[int(v * len(r))]


def mix(a: RGBA, b: RGBA, t: float) -> RGBA:
    t = max(0.0, min(1.0, t))
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def shade(c: RGBA, f: float) -> RGBA:
    return tuple(max(0, min(255, round(c[i] * f))) for i in range(3)) + (c[3],)


# =============================================================================================
# Muster
# =============================================================================================

class Voronoi3D:
    """Zellen-Muster im Raum: liefert (Zellnummer, Randnähe 0..1) für einen Punkt."""

    def __init__(self, cell: float, seed: int):
        self.cell = cell
        self.seed = seed
        self._cache: dict[tuple[int, int, int], tuple[float, float, float, float]] = {}

    def _point(self, ix: int, iy: int, iz: int):
        key = (ix, iy, iz)
        p = self._cache.get(key)
        if p is None:
            rng = random.Random(hash((ix, iy, iz, self.seed)) & 0xFFFFFFFF)
            p = ((ix + rng.random()) * self.cell, (iy + rng.random()) * self.cell, (iz + rng.random()) * self.cell, rng.random())
            self._cache[key] = p
        return p

    def __call__(self, x: float, y: float, z: float) -> tuple[float, float]:
        cx, cy, cz = int(math.floor(x / self.cell)), int(math.floor(y / self.cell)), int(math.floor(z / self.cell))
        best = (1e9, 0.0)
        second = 1e9
        for dx in (-1, 0, 1):
            for dy in (-1, 0, 1):
                for dz in (-1, 0, 1):
                    px, py, pz, val = self._point(cx + dx, cy + dy, cz + dz)
                    dist = (px - x) ** 2 + (py - y) ** 2 + (pz - z) ** 2
                    if dist < best[0]:
                        second = best[0]
                        best = (dist, val)
                    elif dist < second:
                        second = dist
        edge = math.sqrt(second) - math.sqrt(best[0])
        return best[1], max(0.0, min(1.0, edge / (self.cell * 0.35)))


def hash01(*args) -> float:
    return (hash(args) & 0xFFFF) / 0xFFFF


# =============================================================================================
# Bemalen
# =============================================================================================

Painter = Callable[["Ctx"], RGBA | None]


class Ctx:
    """Alles, was ein Material-Maler über ein Texel wissen muss."""
    __slots__ = ("cube", "face", "s", "t", "w", "h", "local", "world", "pal", "rng", "u", "v", "glow", "params")

    def __init__(self):
        self.glow: RGBA | None = None


def paint_model(model: Model, materials: dict[str, Painter], pal: dict, seed: int = 1,
                params: dict | None = None) -> tuple[Image.Image, Image.Image]:
    """Gibt (Textur, Leucht-Textur) zurück. Die Leucht-Textur enthält nur selbstleuchtende Texel."""
    img = Image.new("RGBA", (model.tex_w, model.tex_h), (0, 0, 0, 0))
    glow = Image.new("RGBA", (model.tex_w, model.tex_h), (0, 0, 0, 0))
    px = img.load()
    gx = glow.load()
    rng = random.Random(seed)
    for cube in model.all_cubes():
        wp = cube.bone.world_pivot()
        for face, (u0, v0, fw, fh) in face_rects(cube).items():
            if cube.faces and face not in cube.faces:
                continue
            mat = cube.material
            if isinstance(mat, dict):
                mat = mat.get(face, mat.get("*", "body"))
            painter = materials.get(mat)
            if painter is None:
                raise KeyError(f"Material '{mat}' fehlt (Modell {model.name})")
            for t in range(fh):
                for s in range(fw):
                    ctx = Ctx()
                    ctx.cube, ctx.face, ctx.s, ctx.t, ctx.w, ctx.h = cube, face, s, t, fw, fh
                    lx, ly, lz = texel_to_local(cube, face, s, t)
                    ctx.local = (lx, ly, lz)
                    ctx.world = (wp[0] + lx, wp[1] + ly, wp[2] + lz)
                    ctx.pal, ctx.rng, ctx.u, ctx.v = pal, rng, u0 + s, v0 + t
                    ctx.params = params or {}
                    color = painter(ctx)
                    if color is None:
                        continue
                    # leichte eingebaute Licht-Richtung: oben heller, unten dunkler
                    if face == "top":
                        color = shade(color, 1.06)
                    elif face == "bottom":
                        color = shade(color, 0.86)
                    px[u0 + s, v0 + t] = color
                    if ctx.glow is not None:
                        gx[u0 + s, v0 + t] = ctx.glow
    return img, glow


# =============================================================================================
# Fertige Material-Maler
# =============================================================================================

def rel_y(ctx: Ctx) -> float:
    """0 = Oberkante des Quaders, 1 = Unterkante."""
    oy = ctx.cube.origin[1]
    h = ctx.cube.size[1]
    return 0.0 if h <= 0 else (ctx.local[1] - oy) / h


def along(ctx: Ctx) -> float:
    """0..1 entlang der längsten Achse des Quaders (z. B. Horn-Basis -> Spitze bei negativer Richtung)."""
    w, h, d = ctx.cube.size
    ox, oy, oz = ctx.cube.origin
    axis = max(range(3), key=lambda i: (w, h, d)[i])
    lo = (ox, oy, oz)[axis]
    size = (w, h, d)[axis]
    return 0.0 if size <= 0 else (ctx.local[axis] - lo) / size


def scales_painter(cell: float = 2.2, seed: int = 7, back: str = "back", belly: str = "belly",
                   belly_from: float = 0.72, contrast: float = 0.55) -> Painter:
    vor = Voronoi3D(cell, seed)

    def paint(ctx: Ctx) -> RGBA:
        x, y, z = ctx.world
        val, edge = vor(x, y, z)
        if ctx.face == "bottom":
            return belly_plates(ctx, belly)
        ry = rel_y(ctx) if ctx.face not in ("top",) else 0.0
        base = 0.42 + (val - 0.5) * contrast * 0.6 + (1.0 - ry) * 0.18
        if edge < 0.35:
            base -= 0.28 * (1.0 - edge / 0.35)
        elif edge > 0.85:
            base += 0.08
        col = pick(ctx.pal[back], base)
        if ry > belly_from and ctx.face in ("west", "east", "front", "back"):
            t = (ry - belly_from) / (1.0 - belly_from)
            col = mix(col, pick(ctx.pal[belly], 0.55 + (val - 0.5) * 0.2), t * 0.85)
        return col

    return paint


def belly_plates(ctx: Ctx, belly: str = "belly", plate: float = 2.0) -> RGBA:
    z = ctx.world[2]
    k = (z / plate) % 1.0
    v = 0.62 + (0.12 if k < 0.5 else 0.0)
    if k > 0.86:
        v = 0.35
    x = ctx.local[0] - ctx.cube.origin[0]
    w = max(ctx.cube.size[0], 1e-3)
    edge = min(x, w - x) / w
    if edge < 0.12:
        v -= 0.15
    return pick(ctx.pal[belly], v)


def membrane_painter(key: str = "membrane", vein_key: str = "back", spacing: float = 7.0) -> Painter:
    def paint(ctx: Ctx) -> RGBA:
        x, y, z = ctx.world
        lx, ly, lz = ctx.local
        w, h, d = ctx.cube.size
        # Adern verlaufen vom Flügelarm (vordere Kante, kleines z) nach hinten
        vein_pos = (abs(lx) % spacing)
        v = 0.45 + 0.12 * math.sin(lz * 0.35 + lx * 0.15)
        rz = (lz - ctx.cube.origin[2]) / max(d, 1e-3)
        v += (1.0 - rz) * 0.1
        if vein_pos < 1.0:
            return pick(ctx.pal[vein_key], 0.32)
        if rz > 0.93:
            return pick(ctx.pal[key], 0.25)
        return pick(ctx.pal[key], v)

    return paint


def gradient_painter(key: str, start: float = 0.25, end: float = 0.85, rings: float = 0.0) -> Painter:
    def paint(ctx: Ctx) -> RGBA:
        a = along(ctx)
        v = start + (end - start) * a
        if rings and int(a * rings) % 2 == 0:
            v -= 0.08
        return pick(ctx.pal[key], v + (hash01(ctx.u, ctx.v) - 0.5) * 0.06)

    return paint


def flat_painter(key: str, value: float = 0.5, noise: float = 0.08) -> Painter:
    def paint(ctx: Ctx) -> RGBA:
        return pick(ctx.pal[key], value + (hash01(ctx.u, ctx.v, key) - 0.5) * noise)

    return paint


def eye_painter(iris: str = "eye", glow: bool = True) -> Painter:
    """Kleine Augen-Quader: Vorder-/Seitenflächen mit Iris + Pupille, Rest dunkel."""

    def paint(ctx: Ctx) -> RGBA:
        if ctx.face in ("top", "bottom", "back"):
            return pick(ctx.pal["back"], 0.15)
        cx = (ctx.w - 1) / 2
        is_pupil = ctx.w >= 2 and abs(ctx.s - cx) < 0.6 and ctx.t >= ctx.h // 2
        col = (20, 14, 12, 255) if is_pupil else pick(ctx.pal[iris], 0.75 if ctx.t == 0 else 0.55)
        if glow and not is_pupil:
            ctx.glow = col
        return col

    return paint


def transparent_painter() -> Painter:
    return lambda ctx: None


def stripes_painter(base: Painter, key: str, axis: int = 2, period: float = 6.0, width: float = 1.2,
                    glow: bool = False) -> Painter:
    """Legt Streifen (z. B. Blitzmuster) über ein anderes Material."""

    def paint(ctx: Ctx) -> RGBA:
        coord = ctx.world[axis] + 0.6 * ctx.world[(axis + 1) % 3]
        if (coord % period) < width and ctx.face in ("top", "west", "east"):
            col = pick(ctx.pal[key], 0.7)
            if glow:
                ctx.glow = col
            return col
        return base(ctx)

    return paint
