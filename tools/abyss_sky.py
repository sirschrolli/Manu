#!/usr/bin/env python3
"""
Die Risse am Himmel des Abgrunds (textures/environment/abyss_rifts.png, vier Varianten im 2×2-Raster).

Jede Variante ist ein Netz zackiger Sprünge: ein weißglühender Kern, darum ein orangefarbenes Glühen, ein roter Saum und
ein weiter, karminroter Schein. Das Bild wird im Spiel <b>additiv</b> über den Himmel gelegt (Pipeline „celestial“): Die
Farbe mal Deckkraft ist das Licht, das der Riss abgibt; wo nichts reißt, ist das Bild durchsichtig.

    0  der große Riss – ein langer, gezackter Sprung quer über das Feld, aus dem Äste brechen
    1  der Einschlag – Sprünge, die sternförmig von einem Punkt ausgehen, mit Querrissen wie in Glas
    2  das Auge – ein klaffender, linsenförmiger Spalt mit ausgefransten Rändern
    3  der Blitzriss – ein schräger, mehrfach gegabelter Sprung

Aufruf zur Vorschau: python3 tools/abyss_sky.py vorschau.png
"""
from __future__ import annotations

import math
import random

import numpy as np
from PIL import Image, ImageDraw
from scipy import ndimage

CELL = 512


def _jagged(p0, p1, depth: int, rough: float, rnd: random.Random) -> list[tuple[float, float]]:
    """Ein zackiger Pfad von p0 nach p1 (Mittelpunktverschiebung)."""
    pts = [p0, p1]
    for level in range(depth):
        out = [pts[0]]
        r = rough * (0.68 ** level)
        for a, b in zip(pts, pts[1:]):
            dx, dy = b[0] - a[0], b[1] - a[1]
            length = math.hypot(dx, dy) or 1.0
            nx, ny = -dy / length, dx / length
            off = rnd.uniform(-1.0, 1.0) * length * r
            along = rnd.uniform(-0.12, 0.12) * length
            mx = (a[0] + b[0]) / 2 + nx * off + dx / length * along
            my = (a[1] + b[1]) / 2 + ny * off + dy / length * along
            out += [(mx, my), b]
        pts = out
    return pts


class _Net:
    """Mittellinien der Sprünge mit ihrer Breite (in Bildpunkten)."""

    def __init__(self) -> None:
        self.paths: list[tuple[list[tuple[float, float]], list[float]]] = []

    def add(self, pts, w0: float, w1: float, bulge: float = 0.0, power: float = 0.8) -> None:
        """Breite läuft von w0 nach w1; {@code bulge} wölbt sie in der Mitte auf (klaffender Spalt)."""
        n = len(pts) - 1
        widths = []
        for i in range(len(pts)):
            t = i / max(1, n)
            w = w0 + (w1 - w0) * t + bulge * math.sin(math.pi * t) ** power
            widths.append(max(0.6, w))
        self.paths.append((pts, widths))

    def branch(self, rnd: random.Random, pts, widths, count: int, length: tuple[float, float], spread: tuple[float, float],
               rough: float, depth: int, sub: int = 0, scale: float = 0.55) -> None:
        """Äste, die aus einem Pfad brechen (und, wenn {@code sub} > 0, selbst wieder verzweigen)."""
        n = len(pts)
        for _ in range(count):
            i = rnd.randint(int(n * 0.12), int(n * 0.88))
            j = min(n - 1, i + 2)
            dx, dy = pts[j][0] - pts[i][0], pts[j][1] - pts[i][1]
            base = math.atan2(dy, dx)
            ang = base + rnd.choice((-1, 1)) * math.radians(rnd.uniform(*spread))
            ln = rnd.uniform(*length)
            p0 = pts[i]
            p1 = (p0[0] + math.cos(ang) * ln, p0[1] + math.sin(ang) * ln)
            path = _jagged(p0, p1, depth, rough, rnd)
            w0 = widths[i] * scale
            self.add(path, w0, 0.6)
            if sub > 0:
                self.branch(rnd, path, [w0 * (1 - k / len(path)) + 0.6 for k in range(len(path))], sub, (ln * 0.3, ln * 0.55),
                            (25, 60), rough, max(3, depth - 1), 0, scale)


def _stamp(draw: ImageDraw.ImageDraw, pts, widths, ss: int, fill: int = 255) -> None:
    """Zeichnet einen Pfad mit wechselnder Breite (Kreisstempel, ss-fach überabgetastet)."""
    for (a, b), wa, wb in zip(zip(pts, pts[1:]), widths, widths[1:]):
        steps = max(1, int(math.hypot(b[0] - a[0], b[1] - a[1]) * ss / 1.5))
        for k in range(steps + 1):
            t = k / steps
            x = (a[0] + (b[0] - a[0]) * t) * ss
            y = (a[1] + (b[1] - a[1]) * t) * ss
            r = max(0.5, (wa + (wb - wa) * t) * 0.5 * ss)
            draw.ellipse((x - r, y - r, x + r, y + r), fill=fill)


# Glutfarben von kalt nach heiß (Hitze, Farbe): tiefes Karmesin – Rot – Orange – Gelb – Weiß
RAMP = [(0.0, (0.0, 0.0, 0.0)), (0.06, (0.20, 0.01, 0.07)), (0.16, (0.48, 0.03, 0.12)), (0.32, (0.86, 0.12, 0.08)),
        (0.52, (1.0, 0.42, 0.10)), (0.72, (1.0, 0.70, 0.32)), (0.88, (1.0, 0.90, 0.66)), (1.0, (1.0, 0.98, 0.93))]


def _ramp(heat: np.ndarray) -> np.ndarray:
    xs = [p[0] for p in RAMP]
    out = np.zeros(heat.shape + (3,))
    h = np.clip(heat, 0.0, 1.0)
    for c in range(3):
        out[..., c] = np.interp(h, xs, [p[1][c] for p in RAMP])
    return out


def _render(net: "_Net", rnd: random.Random, size: int = CELL, inner=None, halo: float = 1.0) -> np.ndarray:
    """
    Lichtbild (RGB, 0…1) eines Netzes: Die Sprünge werden als Hitzefeld gezeichnet (Kern plus mehrere weiche
    Schleier), das eine Glutfarbskala in Licht übersetzt – so glüht ein dicker Sprung weiß, ein dünner rot.
    """
    ss = 2
    img = Image.new("L", (size * ss, size * ss), 0)
    draw = ImageDraw.Draw(img)
    for pts, widths in net.paths:
        _stamp(draw, pts, widths, ss)
    core = np.asarray(img.resize((size, size), Image.LANCZOS), dtype=np.float64) / 255.0
    core = np.clip(core, 0.0, 1.0)

    flicker = ndimage.gaussian_filter(np.random.default_rng(rnd.randint(0, 1 << 30)).standard_normal((size, size)), 5.0)
    flicker = 0.8 + 0.2 * np.tanh(flicker / (flicker.std() + 1e-9))

    heat = (core * 0.95
            + ndimage.gaussian_filter(core, 1.6) * 0.55
            + ndimage.gaussian_filter(core, 4.5) * 0.9
            + ndimage.gaussian_filter(core, 13.0) * 1.5 * halo
            + ndimage.gaussian_filter(core, 36.0) * 1.6 * halo) * flicker
    stars = None
    if inner is not None:
        glow_in, stars = inner
        heat = heat + glow_in
    heat = 1.0 - np.exp(-heat * 1.25)
    e = _ramp(heat)
    if stars is not None:
        e += stars[..., None] * np.array([0.82, 0.88, 1.0])

    # Funken: kleine Glutpunkte in der Nähe der Sprünge
    near = ndimage.gaussian_filter(core, 10.0)
    ys, xs = np.nonzero((near > 0.02) & (core < 0.05))
    if len(xs):
        gen = np.random.default_rng(rnd.randint(0, 1 << 30))
        pick = gen.choice(len(xs), size=min(len(xs), 120), replace=False)
        sparks = np.zeros((size, size))
        for p in pick:
            sparks[ys[p], xs[p]] = gen.uniform(0.4, 1.0)
        sparks = np.clip(ndimage.gaussian_filter(sparks, 0.8) * 5.0, 0, 1)
        e += sparks[..., None] * np.array([1.0, 0.70, 0.36]) * 0.75

    # zum Rand des Feldes hin ganz ausblenden (keine sichtbaren Kanten am Himmel)
    yy, xx = np.mgrid[0:size, 0:size] / (size - 1)
    edge = np.minimum(np.minimum(xx, 1 - xx), np.minimum(yy, 1 - yy))
    e *= np.clip(edge / 0.16, 0, 1)[..., None] ** 1.5
    return np.clip(e, 0.0, 1.0)


def _great(rnd: random.Random, s: int) -> _Net:
    net = _Net()
    p0 = (s * 0.07, s * rnd.uniform(0.38, 0.5))
    p1 = (s * 0.93, s * rnd.uniform(0.5, 0.62))
    trunk = _jagged(p0, p1, 7, 0.24, rnd)
    net.add(trunk, 1.0, 1.0, bulge=s * 0.022, power=0.7)
    pts, widths = net.paths[-1]
    net.branch(rnd, pts, widths, 7, (s * 0.1, s * 0.26), (28, 72), 0.26, 5, sub=1)
    return net


def _impact(rnd: random.Random, s: int) -> _Net:
    net = _Net()
    c = (s * rnd.uniform(0.45, 0.55), s * rnd.uniform(0.45, 0.55))
    rays = rnd.randint(6, 8)
    ends = []
    for k in range(rays):
        ang = 2 * math.pi * k / rays + rnd.uniform(-0.3, 0.3)
        ln = s * rnd.uniform(0.24, 0.42)
        end = (c[0] + math.cos(ang) * ln, c[1] + math.sin(ang) * ln)
        ends.append((ang, ln))
        ray = _jagged(c, end, 6, 0.2, rnd)
        net.add(ray, s * 0.02, 0.7)
        pts, widths = net.paths[-1]
        net.branch(rnd, pts, widths, 2, (s * 0.05, s * 0.12), (30, 70), 0.25, 4)
    # Querrisse wie in gesprungenem Glas: Bögen zwischen benachbarten Strahlen
    for ring in (0.45, 0.75):
        for k in range(rays):
            if rnd.random() < 0.55:
                a0, l0 = ends[k]
                a1, l1 = ends[(k + 1) % rays]
                if a1 < a0:
                    a1 += 2 * math.pi
                q0 = (c[0] + math.cos(a0) * l0 * ring, c[1] + math.sin(a0) * l0 * ring)
                q1 = (c[0] + math.cos(a1) * l1 * ring, c[1] + math.sin(a1) * l1 * ring)
                net.add(_jagged(q0, q1, 4, 0.18, rnd), 2.2 - ring, 1.0)
    return net


def _eye(rnd: random.Random, s: int):
    """Ein klaffender Spalt: zwei glühende, zackige Lippen, dazwischen schimmert dunkelrot das Jenseits mit fremden Sternen."""
    net = _Net()
    y0 = s * rnd.uniform(0.47, 0.53)
    p0 = (s * 0.1, y0)
    p1 = (s * 0.9, y0 + s * rnd.uniform(-0.04, 0.04))
    gape = s * 0.12
    lips = []
    for sign in (-1, 1):
        n = 9
        ctrl = [p0]
        for k in range(1, n):
            t = k / n
            x = p0[0] + (p1[0] - p0[0]) * t
            y = p0[1] + (p1[1] - p0[1]) * t + sign * gape * math.sin(math.pi * t) ** 0.8 * rnd.uniform(0.75, 1.15)
            ctrl.append((x, y))
        ctrl.append(p1)
        path = [ctrl[0]]
        for a, b in zip(ctrl, ctrl[1:]):
            path += _jagged(a, b, 3, 0.22, rnd)[1:]
        net.add(path, 1.2, 1.2, bulge=s * 0.012, power=0.6)
        lips.append(path)
        pts, widths = net.paths[-1]
        # ausgefranste Ränder: kurze Risse, die vom Spalt weg ins Dunkel laufen
        for _ in range(9):
            i = rnd.randint(int(len(pts) * 0.1), int(len(pts) * 0.9))
            ang = math.atan2(sign, 0) + rnd.uniform(-0.7, 0.7)
            ln = s * rnd.uniform(0.04, 0.13)
            q = (pts[i][0] + math.cos(ang) * ln, pts[i][1] + math.sin(ang) * ln)
            net.add(_jagged(pts[i], q, 4, 0.3, rnd), widths[i] * 0.45, 0.6)
    # das Innere: ein schwacher, roter Schein und fremde Sterne
    poly = Image.new("L", (s, s), 0)
    ImageDraw.Draw(poly).polygon(lips[0] + lips[1][::-1], fill=255)
    inside = ndimage.gaussian_filter(np.asarray(poly, dtype=np.float64) / 255.0, 3.0)
    gen = np.random.default_rng(rnd.randint(0, 1 << 30))
    stars = (gen.random((s, s)) < 0.004) * gen.uniform(0.3, 1.0, (s, s))
    stars = ndimage.gaussian_filter(stars, 0.6) * 4.0
    return net, (inside * 0.05, np.clip(stars, 0, 1) * inside)


def _fork(rnd: random.Random, s: int) -> _Net:
    net = _Net()
    p0 = (s * rnd.uniform(0.15, 0.3), s * 0.08)
    p1 = (s * rnd.uniform(0.6, 0.8), s * 0.92)
    trunk = _jagged(p0, p1, 7, 0.22, rnd)
    net.add(trunk, s * 0.008, 0.8, bulge=s * 0.016, power=0.6)
    pts, widths = net.paths[-1]
    net.branch(rnd, pts, widths, 4, (s * 0.2, s * 0.36), (18, 40), 0.24, 6, sub=2, scale=0.7)
    return net


def rifts(size: int = CELL) -> Image.Image:
    """Alle vier Varianten im 2×2-Raster als RGBA (Farbe mal Deckkraft = Licht)."""
    sheet = np.zeros((size * 2, size * 2, 4))
    for v, fn in enumerate((_great, _impact, _eye, _fork)):
        rnd = random.Random(72400 + v * 31)
        made = fn(rnd, size)
        net, inner = made if isinstance(made, tuple) else (made, None)
        e = _render(net, rnd, size, inner, 0.45 if inner is not None else 1.0)
        alpha = e.max(axis=2)
        rgb = np.where(alpha[..., None] > 1e-6, e / np.maximum(alpha[..., None], 1e-6), 0.0)
        y0, x0 = (v // 2) * size, (v % 2) * size
        sheet[y0:y0 + size, x0:x0 + size, :3] = rgb
        sheet[y0:y0 + size, x0:x0 + size, 3] = alpha
    data = np.clip(np.round(sheet * 255), 0, 255).astype(np.uint8)
    return Image.fromarray(data, "RGBA")


if __name__ == "__main__":
    import sys
    img = rifts()
    out = sys.argv[1] if len(sys.argv) > 1 else "abyss_rifts.png"
    # Vorschau: additiv über einen dunklen Himmel gelegt
    bg = np.zeros((img.height, img.width, 3))
    bg[..., 0], bg[..., 1], bg[..., 2] = 0x1A / 255, 0x06 / 255, 0x10 / 255
    a = np.asarray(img, dtype=np.float64) / 255
    comp = np.clip(bg + a[..., :3] * a[..., 3:4], 0, 1)
    Image.fromarray((comp * 255).astype(np.uint8), "RGB").save(out)
    img.save(out.replace(".png", "_rgba.png"))
