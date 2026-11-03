#!/usr/bin/env python3
"""
Texturen der Zauberwirkungen (textures/entity/spell/): weiße Vorlagen, die das Spiel in der Farbe des Elements färbt.

  ring.png     Warnkreis am Boden: Doppelring mit Runenstrichen und vier Rauten
  fill.png     weiche Füllung des Warnkreises (wächst bis zum Einschlag)
  beam.png     Lichtstrahl: heller Kern, weicher Schein, fließende Schlieren (kachelbar in Längsrichtung)
  pillar.png   Säule aus Feuer/Wasser/Licht: aufsteigende Flammenzungen (in beide Richtungen kachelbar)
  wave.png     Flutwelle: tiefes Blau unten, Gischtkamm oben (kachelbar in der Breite)
  bubble.png   Wasserblase: klarer Kern, glänzender Rand, Lichtpunkt oben links

Aufruf (aus dem Projektordner):  python3 tools/spell_fx_textures.py [--preview datei.png]
"""
from __future__ import annotations

import json
import math
import sys
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/textures/entity/spell"


def _noise(w: int, h: int, cell_x: int, cell_y: int, seed: int) -> np.ndarray:
    """Weiches, in beide Richtungen kachelbares Rauschen 0..1 (Wertrauschen auf einem periodischen Gitter)."""
    rng = np.random.default_rng(seed)
    gx, gy = max(1, w // cell_x), max(1, h // cell_y)
    grid = rng.random((gy, gx))
    ys = np.arange(h) / h * gy
    xs = np.arange(w) / w * gx
    y0 = np.floor(ys).astype(int)
    x0 = np.floor(xs).astype(int)
    fy = ys - y0
    fx = xs - x0
    fy = fy * fy * (3 - 2 * fy)
    fx = fx * fx * (3 - 2 * fx)
    y1 = (y0 + 1) % gy
    x1 = (x0 + 1) % gx
    a = grid[y0][:, x0]
    b = grid[y0][:, x1]
    c = grid[y1][:, x0]
    d = grid[y1][:, x1]
    top = a + (b - a) * fx[None, :]
    bot = c + (d - c) * fx[None, :]
    return top + (bot - top) * fy[:, None]


def _fbm(w: int, h: int, cells: list[tuple[int, int]], seed: int) -> np.ndarray:
    out = np.zeros((h, w))
    amp = 1.0
    total = 0.0
    for i, (cx, cy) in enumerate(cells):
        out += _noise(w, h, cx, cy, seed + i * 17) * amp
        total += amp
        amp *= 0.55
    return out / total


def _supersample(fn, size: int, ss: int = 4) -> np.ndarray:
    """Zeichnet mit {ss}-facher Auflösung und mittelt (weiche Kanten)."""
    n = size * ss
    ys, xs = np.mgrid[0:n, 0:n]
    u = (xs + 0.5) / n * 2 - 1
    v = (ys + 0.5) / n * 2 - 1
    a = fn(u, v)
    return a.reshape(size, ss, size, ss).mean(axis=(1, 3))


def ring() -> Image.Image:
    size = 128

    def fn(u, v):
        r = np.sqrt(u * u + v * v)
        ang = np.arctan2(v, u)
        a = np.zeros_like(r)
        a = np.maximum(a, ((r > 0.90) & (r < 0.97)).astype(float))                  # äußerer Ring
        a = np.maximum(a, ((r > 0.78) & (r < 0.805)).astype(float) * 0.9)           # innerer Ring
        # Runenstriche zwischen den Ringen
        k = (ang / (2 * math.pi) * 36) % 1.0
        ticks = ((k < 0.16) & (r > 0.81) & (r < 0.89)).astype(float) * 0.85
        a = np.maximum(a, ticks)
        # vier Rauten auf dem äußeren Ring
        for q in range(4):
            cx, cy = math.cos(q * math.pi / 2 + math.pi / 4) * 0.84, math.sin(q * math.pi / 2 + math.pi / 4) * 0.84
            d = np.abs(u - cx) + np.abs(v - cy)
            a = np.maximum(a, (d < 0.085).astype(float))
            a = np.where(d < 0.04, 0.35, a)
        # zarter Innenkreis mit Kreuzlinien (Zielmarke)
        a = np.maximum(a, ((r > 0.25) & (r < 0.265)).astype(float) * 0.55)
        cross = ((np.abs(u) < 0.012) | (np.abs(v) < 0.012)) & (r > 0.3) & (r < 0.72)
        a = np.maximum(a, cross.astype(float) * 0.35)
        return a

    alpha = _supersample(fn, size)
    img = np.zeros((size, size, 4), dtype=np.uint8)
    img[..., 0:3] = 255
    img[..., 3] = np.clip(alpha * 255, 0, 255).astype(np.uint8)
    return Image.fromarray(img, "RGBA")


def fill() -> Image.Image:
    size = 64

    def fn(u, v):
        r = np.sqrt(u * u + v * v)
        a = 0.18 + 0.62 * np.clip(r / 0.96, 0, 1) ** 2.2
        return np.where(r < 0.97, a, 0.0)

    alpha = _supersample(fn, size)
    img = np.zeros((size, size, 4), dtype=np.uint8)
    img[..., 0:3] = 255
    img[..., 3] = np.clip(alpha * 255, 0, 255).astype(np.uint8)
    return Image.fromarray(img, "RGBA")


def beam() -> Image.Image:
    w, h = 32, 64
    n = _fbm(w, h, [(32, 8), (16, 4), (8, 2)], 31)
    x = (np.arange(w) + 0.5) / w * 2 - 1
    core = np.exp(-(x / 0.16) ** 2)
    glow = np.exp(-(x / 0.55) ** 2)
    streak = 0.65 + 0.7 * n
    val = core[None, :] * 1.0 + glow[None, :] * 0.55 * streak
    val = np.clip(val, 0, 1)
    img = np.zeros((h, w, 4), dtype=np.uint8)
    rgb = np.clip(val * 255, 0, 255).astype(np.uint8)
    img[..., 0] = rgb
    img[..., 1] = rgb
    img[..., 2] = rgb
    img[..., 3] = 255
    return Image.fromarray(img, "RGBA")


def pillar() -> Image.Image:
    w, h = 64, 64
    n = _fbm(w, h, [(4, 32), (2, 16), (8, 32)], 57)
    m = _fbm(w, h, [(16, 64), (8, 32)], 91)
    # aufsteigende Zungen: stark senkrecht gestreckt, scharfe helle Kanten, dazwischen dunkle Lücken
    tongues = np.clip((n - 0.42) * 3.0, 0, 1) ** 1.2
    val = 0.12 + 0.88 * tongues * (0.65 + 0.6 * m)
    val = np.clip(val, 0, 1)
    img = np.zeros((h, w, 4), dtype=np.uint8)
    rgb = np.clip(val * 255, 0, 255).astype(np.uint8)
    img[..., 0] = rgb
    img[..., 1] = rgb
    img[..., 2] = rgb
    img[..., 3] = 255
    return Image.fromarray(img, "RGBA")


def wave() -> Image.Image:
    w, h = 64, 32
    n = _fbm(w, h, [(8, 8), (4, 4)], 77)
    y = (np.arange(h) + 0.5) / h                      # 0 oben (Kamm) … 1 unten
    deep = np.array([18, 74, 150], dtype=float)
    mid = np.array([40, 150, 210], dtype=float)
    light = np.array([150, 225, 245], dtype=float)
    foam = np.array([245, 252, 255], dtype=float)
    img = np.zeros((h, w, 4), dtype=float)
    for yi in range(h):
        t = y[yi]
        if t < 0.5:
            col = light + (mid - light) * (t / 0.5)
        else:
            col = mid + (deep - mid) * ((t - 0.5) / 0.5)
        img[yi, :, 0:3] = col
        img[yi, :, 3] = 150 + 70 * (1 - t)
    # Gischtkamm oben, fransig
    crest = 0.16 + 0.12 * n[0]
    for xi in range(w):
        for yi in range(h):
            t = y[yi]
            if t < crest[xi]:
                img[yi, xi, 0:3] = foam
                img[yi, xi, 3] = 235
            elif t < crest[xi] + 0.08 and n[yi, xi] > 0.5:
                img[yi, xi, 0:3] = foam * 0.85 + img[yi, xi, 0:3] * 0.15
                img[yi, xi, 3] = 220
    # helle Strömungslinien
    lines = (np.sin((np.arange(w)[None, :] / w * 4 + y[:, None] * 3 + n * 2) * math.pi * 2) > 0.92) & (y[:, None] > 0.3)
    img[..., 0:3] = np.where(lines[..., None], img[..., 0:3] * 0.5 + light * 0.5, img[..., 0:3])
    return Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), "RGBA")


def bubble() -> Image.Image:
    size = 64

    def alpha_fn(u, v):
        r = np.sqrt(u * u + v * v)
        a = 0.10 + 0.55 * np.clip((r - 0.55) / 0.4, 0, 1) ** 2
        rim = np.exp(-((r - 0.93) / 0.035) ** 2) * 0.6
        spot = np.exp(-(((u + 0.38) ** 2 + (v + 0.42) ** 2) / 0.012)) * 0.9
        spot2 = np.exp(-(((u - 0.35) ** 2 + (v - 0.45) ** 2) / 0.004)) * 0.5
        a = a + rim + spot + spot2
        return np.where(r < 0.985, np.clip(a, 0, 1), 0.0)

    def white_fn(u, v):
        r = np.sqrt(u * u + v * v)
        spot = np.exp(-(((u + 0.38) ** 2 + (v + 0.42) ** 2) / 0.012))
        rim = np.exp(-((r - 0.93) / 0.05) ** 2)
        return np.clip(spot + rim * 0.6, 0, 1)

    alpha = _supersample(alpha_fn, size)
    white = _supersample(white_fn, size)
    base = np.array([160, 225, 250], dtype=float)
    img = np.zeros((size, size, 4), dtype=float)
    img[..., 0:3] = base[None, None, :] * (1 - white[..., None]) + 255 * white[..., None]
    img[..., 3] = alpha * 255
    return Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), "RGBA")


BLUR = {"fill", "beam", "pillar", "bubble"}


def build() -> dict[str, Image.Image]:
    return {"ring": ring(), "fill": fill(), "beam": beam(), "pillar": pillar(), "wave": wave(), "bubble": bubble()}


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    images = build()
    for name, img in images.items():
        img.save(OUT / f"{name}.png")
        meta = OUT / f"{name}.png.mcmeta"
        if name in BLUR:
            meta.write_text(json.dumps({"texture": {"blur": True}}, indent=2) + "\n", encoding="utf-8")
        elif meta.exists():
            meta.unlink()
    print(f"{len(images)} Texturen geschrieben nach {OUT}")
    if "--preview" in sys.argv:
        target = Path(sys.argv[sys.argv.index("--preview") + 1])
        tiles = []
        for name, img in images.items():
            bg = Image.new("RGBA", (192, 192), (40, 30, 60, 255))
            scaled = img.resize((img.width * 192 // max(img.size), img.height * 192 // max(img.size)), Image.NEAREST)
            bg.alpha_composite(scaled, (0, 0))
            tiles.append(bg)
        sheet = Image.new("RGBA", (192 * len(tiles) + 8 * (len(tiles) - 1), 192), (20, 20, 20, 255))
        for i, t in enumerate(tiles):
            sheet.alpha_composite(t, (i * 200, 0))
        sheet.save(target)
        print(f"Vorschau: {target}")


if __name__ == "__main__":
    main()
