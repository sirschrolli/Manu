#!/usr/bin/env python3
"""
Texturen der Drachen-Bosse: eigene Bossleisten je Element (verzierter Rahmen mit Drachenkopf-Medaillon,
Füllung mit Element-Muster, Phasen-Markierungen) und die Geschosse (Feuerball, Meteor).

Wird von tools/generate_textures.py aufgerufen (build(put)).

Die Bossleisten selbst entstehen in bossbar_textures.py (Aufbau der Textur siehe dort und BossBarRenderer.java).
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import pixelart as pa
from pixelart import CLEAR, hexc

ELEMENTS = {
    #           Akzent      hell        dunkel      Metall dunkel / hell
    "fire": ("#ff6a1a", "#ffd27a", "#3a0c06", "#2a2220", "#8a7a70"),
    "ice": ("#5cc8f4", "#e8fbff", "#0e2a3a", "#26323c", "#a8c4d4"),
    "lightning": ("#ffd21a", "#fff6b0", "#1a1830", "#24222e", "#9a96aa"),
    "nature": ("#56c832", "#d0ff9a", "#0e2410", "#2a2418", "#9a8a62"),
    "earth": ("#d8963c", "#f4d8a0", "#2a1a0c", "#2c2620", "#9c8c7a"),
    "water": ("#28b8f0", "#b8f2ff", "#062438", "#1c2a34", "#88a8b8"),
}

FRAME_W, FRAME_H = 232, 30
BAR_X, BAR_Y, BAR_W, BAR_H = 25, 15, 182, 7

# Drachenkopf im Profil (nach rechts blickend), 16 x 14
HEAD = [
    "......xx........",
    ".....xxxx.......",
    "....xxxxxx..x...",
    "...xxxxxxxxxx...",
    "..xxxxxexxxxxx..",
    "..xxxxxxxxxxxxx.",
    ".xxxxxxxxxxxxxxx",
    ".xxxxxxx........",
    "xxxxxxxx.ww.w...",
    "xxxxxxxxxxxxxx..",
    ".xxxxxxxxxxxx...",
    "..xxxxxxxx......",
    "...xxxxx........",
    "....xx..........",
]


def lerp(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def pattern(element: str, x: int, y: int) -> float:
    """Helligkeits-Muster 0..1 der Füllung je Element."""
    if element == "fire":
        return 0.5 + 0.5 * math.sin(x * 0.55 + math.sin(y * 1.3 + x * 0.21) * 2.2)
    if element == "ice":
        return 0.6 if ((x + y * 2) // 3) % 4 == 0 else 0.25
    if element == "lightning":
        zig = (x + (y if (x // 4) % 2 == 0 else 3 - y)) % 9
        return 0.9 if zig == 0 else 0.3
    if element == "nature":
        return 0.5 + 0.5 * math.sin(x * 0.4) * math.cos(y * 1.1 + x * 0.15)
    if element == "earth":
        return ((x * 7 + y * 13) % 11) / 11.0
    if element == "water":
        return 0.5 + 0.5 * math.sin(x * 0.35 - y * 0.9)
    return 0.5


def fill_strip(element: str, empty: bool = False) -> Image.Image:
    acc, light, dark, _, _ = (hexc(c) for c in ELEMENTS[element])
    img = Image.new("RGBA", (BAR_W, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        for x in range(BAR_W):
            if empty:
                v = 0.12 + 0.05 * pattern(element, x, y)
                px[x, y] = lerp((10, 10, 12), dark, v * 2.2)
                continue
            top = 1.0 - y / (BAR_H - 1)
            base = lerp(lerp(dark, acc, 0.55 + 0.45 * top), light, 0.0)
            v = pattern(element, x, y)
            col = lerp(base, light, max(0.0, (v - 0.55)) * 0.9 + (0.35 if y == 1 else 0.0))
            if y == BAR_H - 1:
                col = lerp(col, dark, 0.45)
            px[x, y] = col
    return img


def shine_strip() -> Image.Image:
    img = Image.new("RGBA", (12, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        for x in range(12):
            d = abs((x - y * 0.6) - 4.0)
            if d < 3.0:
                a = int(255 * (1 - d / 3.0) * 0.85)
                px[x, y] = (255, 255, 255, a)
    return img


def frame(element: str) -> Image.Image:
    acc, light, dark, mdark, mlight = (hexc(c) for c in ELEMENTS[element])
    img = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    px = img.load()
    x0, x1 = BAR_X - 3, BAR_X + BAR_W + 2       # äußerer Metallrahmen der Leiste
    y0, y1 = BAR_Y - 3, BAR_Y + BAR_H + 2
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            inner = BAR_X <= x < BAR_X + BAR_W and BAR_Y <= y < BAR_Y + BAR_H
            if inner:
                continue
            edge = x in (x0, x1) or y in (y0, y1)
            ring2 = x in (x0 + 1, x1 - 1) or y in (y0 + 1, y1 - 1)
            if edge:
                px[x, y] = (12, 10, 10, 255)
            elif ring2:
                px[x, y] = lerp(mlight, mdark, 0.25 if y < BAR_Y else 0.65)
            else:
                px[x, y] = lerp(mdark, (0, 0, 0), 0.3)
    # Phasen-Markierungen (60 % und 30 %)
    for frac in (0.6, 0.3):
        mx = BAR_X + round(BAR_W * frac)
        for y in range(y0, y0 + 3):
            px[mx, y] = light
        px[mx, y1] = light
        px[mx, y1 - 1] = acc
    # Zierspitzen oben auf dem Rahmen
    for i, sx in enumerate(range(x0 + 20, x1 - 10, 24)):
        h = 3 if i % 2 == 0 else 2
        for k in range(h):
            for w in range(-1 + (k > 0), 2 - (k > 0)):
                px[sx + w, y0 - 1 - k] = mlight if k == h - 1 else lerp(mlight, mdark, 0.5)
    # rechte Schwanzspitze
    for k in range(9):
        y = (y0 + y1) // 2
        for dy in range(-(4 - k // 2), 5 - k // 2):
            if 0 <= y + dy < FRAME_H:
                px[x1 + 1 + k, y + dy] = (12, 10, 10, 255) if abs(dy) == 4 - k // 2 else lerp(mdark, acc, 0.25 + 0.05 * k)
    # linkes Medaillon mit Drachenkopf
    cx, cy, r = 13, 15, 12.5
    for y in range(FRAME_H):
        for x in range(0, 28):
            d = math.hypot(x - cx, y - cy)
            if d <= r:
                if d > r - 1.2:
                    px[x, y] = (12, 10, 10, 255)
                elif d > r - 3.0:
                    t = 0.5 + 0.5 * math.cos(math.atan2(y - cy, x - cx) + 2.2)
                    px[x, y] = lerp(mdark, mlight, t * 0.8)
                else:
                    px[x, y] = lerp(dark, (0, 0, 0), 0.25 + d / r * 0.3)
    for hy, row in enumerate(HEAD):
        for hx, ch in enumerate(row):
            x, y = cx - 8 + hx, cy - 7 + hy
            if ch == "x":
                shade = 0.65 + 0.35 * (1 - hy / len(HEAD))
                px[x, y] = lerp(dark, acc, shade)
            elif ch == "e":
                px[x, y] = light
            elif ch == "w":
                px[x, y] = (236, 230, 214, 255)
    return img


def bossbar(element: str) -> Image.Image:
    img = Image.new("RGBA", (256, 64), CLEAR)
    img.alpha_composite(frame(element), (0, 0))
    img.alpha_composite(fill_strip(element), (0, 32))
    img.alpha_composite(shine_strip(), (200, 32))
    img.alpha_composite(fill_strip(element, empty=True), (0, 40))
    return img


def orb(core: str, mid: str, rim: str, flames: bool, rough: bool, seed: int) -> Image.Image:
    """Leuchtende Kugel (Billboard) – Feuerball oder glühender Felsbrocken."""
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    noise = pa.value_noise(seed, 3)
    c_core, c_mid, c_rim = hexc(core), hexc(mid), hexc(rim)
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            d = math.hypot(dx, dy) / 7.5
            n = noise[y][x]
            limit = 0.95 + (0.12 * math.sin(math.atan2(dy, dx) * 5 + n * 3) if flames else 0.0) - (0.15 * n if rough else 0)
            if d > limit:
                continue
            if rough:
                # dunkler Fels mit glühenden Rissen und brennendem Rand
                crack = abs(n - 0.5) < 0.07
                if d > limit - 0.18:
                    px[x, y] = lerp(c_mid, c_core, 0.3)
                elif crack:
                    px[x, y] = lerp(c_mid, c_core, 0.7)
                else:
                    shade = 0.25 + 0.35 * (1 - d) - (0.15 if dx + dy > 2 else 0)
                    px[x, y] = lerp(c_rim, (110, 80, 66, 255), max(0.0, min(1.0, shade)))
            else:
                t = max(0.0, min(1.0, (1 - d) * 1.4 + (n - 0.5) * 0.3))
                px[x, y] = lerp(c_rim, c_mid, min(1.0, t * 1.6)) if t < 0.6 else lerp(c_mid, c_core, (t - 0.6) / 0.4)
    return img


def soul_bolt() -> Image.Image:
    """Seelenpfeil (Nyxar): violette Kugel mit hellem Kern und zwei Schlieren, die sich um sie winden."""
    img = orb("#fbf2ff", "#a65cff", "#2a0e52", flames=True, rough=False, seed=77)
    px = img.load()
    for i in range(22):
        a = i * 0.42
        for arm in (0, math.pi):
            r = 2.0 + i * 0.25
            x = int(7.5 + math.cos(a + arm) * r)
            y = int(7.5 + math.sin(a + arm) * r)
            if 0 <= x < 16 and 0 <= y < 16 and px[x, y][3]:
                px[x, y] = hexc("#e6ccff") if i < 10 else hexc("#7a3ad0")
    return img


# =============================================================================================
# Odem-Partikel (je 8 Bilder, 16 x 16): von hell und klein zu groß und verblassend
# =============================================================================================

PARTICLE_RAMPS = {
    "flame": ["#fffbe0", "#ffe88a", "#ffb43a", "#ff7a1a", "#d8420e", "#8a2a12", "#4a2a24", "#2e2624"],
    "frost": ["#ffffff", "#f0fcff", "#d8f4ff", "#bfeaff", "#a6dcf4", "#90cbe6", "#82b8d6", "#7aa8c6"],
    "spark": ["#ffffff", "#fffde0", "#fff6a0", "#ffe85a", "#ffd21a", "#e8b010", "#c08a10", "#8a6a20"],
    "spore": ["#e8ffb0", "#c8f07a", "#a6d85a", "#8ac24a", "#7aa848", "#8a7aa8", "#7a6a96", "#6a5a80"],
    "dust": ["#e8d8b8", "#d4c09a", "#c0a880", "#a88e6a", "#90785a", "#7a664e", "#665644", "#56483a"],
    "water": ["#ffffff", "#e8fbff", "#c0f0ff", "#90e0f8", "#60c8f0", "#3aaee0", "#2a90c8", "#2478b0"],
    "shadow": ["#f2e0ff", "#d2a4ff", "#b06aff", "#8642d6", "#5c2a9a", "#3c1c68", "#281444", "#1a0e2c"],
}


def particle_frame(kind: str, frame: int) -> Image.Image:
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    ramp = [hexc(c) for c in PARTICLE_RAMPS[kind]]
    t = frame / 7.0
    seed = sum(map(ord, kind)) * 13 + frame * 7
    noise = pa.value_noise(seed, 3)
    if kind == "spark":
        # gezackter Funke: ein paar leuchtende Linien vom Mittelpunkt
        import random
        rng = random.Random(seed)
        for arm in range(3 + frame % 2):
            x, y = 7.5, 7.5
            ang = rng.random() * math.tau
            for step in range(int(3 + 4 * (1 - t))):
                ang += (rng.random() - 0.5) * 1.6
                x += math.cos(ang) * 1.2
                y += math.sin(ang) * 1.2
                if 0 <= int(x) < 16 and 0 <= int(y) < 16:
                    px[int(x), int(y)] = ramp[min(7, step + frame // 2)]
        px[7, 7] = px[8, 8] = px[7, 8] = px[8, 7] = ramp[min(7, frame // 2)]
        return img
    radius = 0.45 + 0.5 * t
    for y in range(16):
        for x in range(16):
            dx, dy = (x - 7.5) / 7.5, (y - 7.5) / 7.5
            d = math.hypot(dx, dy)
            n = noise[y][x]
            edge = radius * (0.85 + 0.3 * n)
            if d > edge:
                continue
            inner = 1.0 - d / max(edge, 1e-3)
            v = (1.0 - inner) * 0.55 + t * 0.6 + (n - 0.5) * 0.3
            col = ramp[max(0, min(7, int(v * 8)))]
            if kind in ("frost", "water") and n > 0.78 and inner > 0.3:
                col = ramp[0]                       # Glitzern / Gischt
            if kind == "dust" and n > 0.8 and frame < 5:
                col = hexc("#4a3a2a")               # kleine Steinchen
            if kind == "shadow":
                # Schattenrauch: weich auslaufend und löchrig, nur im Kern ein glimmender Seelenfunke
                if n < 0.3 + 0.3 * t and inner < 0.6:
                    continue
                if n > 0.86 and inner > 0.45 and frame < 5:
                    col = ramp[1]
                a = int(255 * min(1.0, 0.3 + 0.9 * inner) * (1.0 - 0.35 * t))
                col = (col[0], col[1], col[2], max(60, a))
            if kind == "spore" and n > 0.82:
                col = hexc("#f4ffd0")
            px[x, y] = col
    return img


def build(put: Callable[[str, Image.Image], None]) -> None:
    import bossbar_textures
    for element in bossbar_textures.ELEMENTS:
        put(f"gui/boss_bar/{element}", bossbar_textures.bossbar(element))
    for element in ("fire", "ice", "lightning", "nature", "earth", "water"):
        put(f"gui/boss_bar/mage_{element}", bossbar_textures.mage_bossbar(element))
    put("entity/boss/projectile_fireball", orb("#fff6c0", "#ffa42a", "#c42a08", flames=True, rough=False, seed=11))
    for kind in PARTICLE_RAMPS:
        for frame in range(8):
            put(f"particle/boss_{kind}_{frame}", particle_frame(kind, frame))
    put("entity/boss/projectile_soul_bolt", soul_bolt())
    put("entity/boss/projectile_meteor", orb("#ffe27a", "#e0520c", "#2a1410", flames=False, rough=True, seed=23))


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key, img))
    out = Image.new("RGBA", (256 * 3, 64 * 3 * 2 + 40), (40, 44, 50, 255))
    for i, element in enumerate(ELEMENTS):
        img = sheet[f"gui/boss_bar/{element}"].resize((256 * 3 // 2, 64 * 3 // 2), Image.NEAREST)
        out.alpha_composite(img, ((i % 2) * 384, (i // 2) * 100))
    out.alpha_composite(pa.zoom(sheet["entity/boss/projectile_fireball"], 6), (0, 300))
    out.alpha_composite(pa.zoom(sheet["entity/boss/projectile_meteor"], 6), (110, 300))
    target = sys.argv[1] if len(sys.argv) > 1 else "/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad/boss_tex.png"
    out.save(target)
    print(target)
