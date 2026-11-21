"""
Gemeinsame Maler für die Nutz- und Wildtiere (E3): Scheckung, Zottelfell, Wolle, Hufe, Hörner, Mäuler.

Alle Maler arbeiten mit Rampen aus der Palette der jeweiligen Rasse (siehe paint.ramp) und mit Weltkoordinaten,
damit Muster über Quadergrenzen hinweg durchlaufen (eine Platte hört nicht an der Kante des Halses auf).
"""
from __future__ import annotations

import math

from ..paint import Ctx, hash01, mix, noise3, pick, rel_y, soft_fur_painter


def fur(back: str = "fur", belly: str | None = "belly", seed: int = 5, contrast: float = 0.24, top_dark: float = 0.12,
        belly_from: float = 0.66):
    """Ruhiges, vanilla-artiges Fell (Rücken dunkler, Bauch heller)."""
    return soft_fur_painter(back, belly, belly_from=belly_from, contrast=contrast, strokes=0.07, top_dark=top_dark, seed=seed)


def patched(base, patch_key: str, scale: float = 0.16, threshold: float = 0.56, seed: int = 11, soft: float = 0.04,
            mask=None):
    """
    Scheckung: große, unregelmäßige Platten in der Farbe {@code patch_key} über einem Grundmaler. Die Platten
    entstehen aus 3D-Rauschen mit gezackten Rändern; {@code mask(ctx)} kann Stellen ausnehmen (z. B. den Kopf).
    """

    def paint(ctx: Ctx):
        col = base(ctx)
        if col is None or (mask is not None and not mask(ctx)):
            return col
        x, y, z = ctx.world
        n = noise3(x * scale, y * scale * 1.3, z * scale, seed)
        n += (hash01(int(x * 2), int(y * 2), int(z * 2), seed) - 0.5) * 0.08
        if n > threshold + soft:
            shade = 0.45 + noise3(x * 0.5, y * 0.5, z * 0.5, seed + 3) * 0.25
            if ctx.face != "top" and rel_y(ctx) > 0.8:
                shade -= 0.08
            return pick(ctx.pal[patch_key], shade)
        if n > threshold:
            return mix(col, pick(ctx.pal[patch_key], 0.5), 0.5)
        return col

    return paint


def shaggy(key: str = "fur", seed: int = 17, length: float = 2.6, contrast: float = 0.32):
    """
    Zottelfell: lange, senkrechte Strähnen (je ein Texel breit) mit sanft wechselnder Helligkeit, dunkleren
    Spitzen am unteren Rand jedes Teils und weichen großen Flächen – für Hochlandrind, Wollschwein, Steinbock-Bart.
    """

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        across = z if ctx.face in ("west", "east") else x
        lane = math.floor(across * 1.0 + 0.001)
        off = hash01(lane, seed) * 6.28
        n = noise3(x * 0.18, y * 0.18, z * 0.18, seed)
        v = 0.52 + (n - 0.5) * contrast * 0.8 + math.sin(y / length * 3.1 + off) * 0.07
        v += (hash01(lane, seed + 1) - 0.5) * 0.12
        if ctx.face == "top":
            v = 0.56 + (noise3(x * 0.35, 0, z * 0.35, seed + 1) - 0.5) * 0.3 + (hash01(int(x), int(z), seed) - 0.5) * 0.08
        elif ctx.face == "bottom":
            v -= 0.14
        else:
            ry = rel_y(ctx)
            fringe = 0.86 - hash01(lane, seed + 2) * 0.12
            if ry > fringe:
                v -= 0.16                                   # dunklere Strähnenspitzen unten
            v -= (1.0 - ry) * 0.06
        return pick(ctx.pal[key], v)

    return paint


def wool(key: str = "wool", seed: int = 23):
    """
    Wolle: dichte, krause Locken (kleine helle Bögen mit dunklen Zwischenräumen). Hell gemalt, damit der Renderer
    sie mit der Farbe der Wolle einfärben kann.
    """

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        a, b = (x, y) if ctx.face in ("front", "back") else (z, y) if ctx.face in ("west", "east") else (x, z)
        cell_a = math.floor(a / 1.5)
        cell_b = math.floor(b / 1.5 + (cell_a % 2) * 0.5)
        fa = (a / 1.5) % 1.0
        fb = (b / 1.5 + (cell_a % 2) * 0.5) % 1.0
        d = math.hypot(fa - 0.5, fb - 0.5)
        v = 0.78 - d * 0.55 + (hash01(cell_a, cell_b, seed) - 0.5) * 0.18
        v += (noise3(x * 0.25, y * 0.25, z * 0.25, seed) - 0.5) * 0.2
        if ctx.face == "bottom":
            v -= 0.15
        elif ctx.face != "top" and rel_y(ctx) > 0.75:
            v -= 0.08
        return pick(ctx.pal[key], v)

    return paint


def leg(fur_key: str = "fur", hoof_key: str = "hoof", sock_key: str | None = None, sock_from: float = 0.6,
        hoof_from: float = 0.86, seed: int = 9, feather_key: str | None = None):
    """Bein mit Huf (unten), optional weißer Fessel ("Socke") und Behang (Fesselhaar) über dem Huf."""
    base = soft_fur_painter(fur_key, None, contrast=0.2, top_dark=0.0, seed=seed)

    def paint(ctx: Ctx):
        ry = rel_y(ctx)
        if ctx.face == "bottom" or ry > hoof_from:
            return pick(ctx.pal[hoof_key], 0.3 + hash01(ctx.u, ctx.v) * 0.2 + (0.15 if ctx.face == "front" else 0.0))
        if feather_key is not None and ry > hoof_from - 0.22:
            x, y, z = ctx.world
            return pick(ctx.pal[feather_key], 0.55 + (hash01(int(x * 2), int(z * 2), int(y), seed) - 0.5) * 0.3)
        if sock_key is not None and ry > sock_from:
            return pick(ctx.pal[sock_key], 0.7 + hash01(ctx.u, ctx.v) * 0.15)
        return base(ctx)

    return paint


def horn(key: str = "horn", tip_key: str | None = "horn_tip", rings: float = 1.4, seed: int = 31):
    """Horn: Ringe quer zur Längsachse, zur Spitze hin heller bzw. in der Farbe der Spitze."""

    def paint(ctx: Ctx):
        w, h, d = ctx.cube.size
        ox, oy, oz = ctx.cube.origin
        lx, ly, lz = ctx.local
        axis = max(range(3), key=lambda i: (w, h, d)[i])
        pos = ((lx, ly, lz)[axis] - (ox, oy, oz)[axis]) / max(1e-3, (w, h, d)[axis])
        tip = ctx.cube.tag == "tip"
        ring = math.sin((lx + ly + lz) * math.pi / rings) * 0.08
        v = 0.45 + ring + (pos - 0.5) * 0.2 + (0.12 if ctx.face in ("top", "west") else 0.0)
        if tip and tip_key is not None:
            return pick(ctx.pal[tip_key], 0.35 + v * 0.4)
        return pick(ctx.pal[key], v)

    return paint


def muzzle(skin_key: str = "nose", fur_key: str = "fur", ring_key: str | None = None, nostrils: bool = True):
    """Flotzmaul / Schnauze: dunkle, feuchte Haut vorne mit Nüstern, optional heller Maulring an den Seiten."""

    def paint(ctx: Ctx):
        if ctx.face == "front":
            v = 0.42 + hash01(ctx.u, ctx.v) * 0.12
            if nostrils and ctx.t == 1 and ctx.s in (1, ctx.w - 2):
                v = 0.08
            return pick(ctx.pal[skin_key], v)
        if ring_key is not None and ctx.face != "bottom":
            lx, ly, lz = ctx.local
            if lz - ctx.cube.origin[2] < 1.2:
                return pick(ctx.pal[ring_key], 0.6 + hash01(ctx.u, ctx.v) * 0.15)
        if ctx.face == "bottom":
            return pick(ctx.pal[skin_key], 0.5)
        return soft_fur_painter(fur_key, None, contrast=0.16, top_dark=0.0, seed=41)(ctx)

    return paint


def solid(key: str, value: float = 0.5, noise: float = 0.1):
    def paint(ctx: Ctx):
        return pick(ctx.pal[key], value + (hash01(ctx.u, ctx.v) - 0.5) * noise)

    return paint


def hidden():
    """Für Teile, die nur in manchen Varianten zu sehen sind und hier unbemalt (durchsichtig) bleiben."""
    return lambda ctx: None
