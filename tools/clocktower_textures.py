#!/usr/bin/env python3
"""
Der Uhrturm (1.01): die Blöcke des Uhrturms im Abgrund.

  * Uhrbronze – eine polierte Bronzeplatte: abgefaster Rand (oben/links im Licht, unten/rechts im Schatten), in den Ecken
    Nieten, dazwischen ruhige, gebürstete Bahnen mit einem Glanz; unten ein Hauch Grünspan.
  * Uhrwerk – ein Bronzerahmen, dahinter im Dunkel zwei Zahnräder, die ineinandergreifen und sich drehen (Bildfolge: das
    große Rad dreht sich um einen Zahn weiter, das kleine gegenläufig im richtigen Verhältnis – nahtlos).
  * Zifferblatt – elfenbeinfarbenes Email, fast glatt, mit einem feinen Guilloché aus Wellen und haarfeinen Rissen, nahtlos
    kachelbar (der Boden des großen Zifferblatts).
  * Uhrwerkherz – oben ein kleines Zifferblatt in einem Bronzering (zwölf Marken, eine Chronitnabe), die Seiten wie das
    Uhrwerk mit Messingrand; die Leuchtebenen: erstarrt ein schwaches Glimmen (die Zeiger auf eine Minute vor zwölf), wach
    laufen die Zeiger (Bildfolge), besiegt ein dunkles, gesprungenes Blatt mit herabgefallenen Zeigern.

Wird von tools/generate_textures.py aufgerufen: build(put); Bildfolgen stehen in ANIMATIONS.
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import item_style as st
import pixelart as pa
import vanilla_style as vs
from pixelart import hexc

N = 16
CLEAR = (0, 0, 0, 0)
ANIMATIONS: dict[str, tuple[int, int, bool]] = {}

#: Uhrbronze: warm, leicht rötlich in den Tiefen, golden im Licht
BRONZE = st.ramp("#b4793a", dark=0.24, light=0.92, cool=20, hue_dark=10, chroma_dark=1.15, warm=85, hue_light=10, chroma_light=0.55)
#: Messing (Säume, Nieten, Zahnräder)
BRASS = st.ramp("#d0a446", dark=0.3, light=0.97, cool=30, warm=95, chroma_light=0.45)
#: Grünspan
PATINA = st.fixed("#2f4a3c", "#3e6250", "#4f7a64", "#6a9a80", "#8ab89c", "#a8d0b4", "#c4e4cc")
#: das Dunkel im Uhrwerk
WORKS = st.fixed("#0a0705", "#130d08", "#1d140b", "#2a1d10", "#3a2915", "#4d381c", "#634a26")
#: Email (Zifferblatt)
ENAMEL = st.ramp("#e6dcc4", dark=0.55, light=0.99, cool=50, hue_dark=10, warm=90, chroma_light=0.3)
#: Tusche (Stundenmarken, Zeiger)
INK = st.fixed("#0c0d16", "#161a2a", "#212842", "#2e3a5e", "#40507e", "#58689a", "#7a8ab8")
#: das Leuchten (Chronit, Zeiger)
AMBER = st.fixed("#6a2c06", "#a2480c", "#d26a16", "#f08f24", "#ffb347", "#ffd27e", "#fff0c4")

GEAR_FRAMES = 8
HEART_FRAMES = 16


def _px(ramp, level: float):
    return ramp[max(0, min(len(ramp) - 1, int(round(level))))]


# =============================================================================
# Uhrbronze
# =============================================================================

def clock_bronze() -> Image.Image:
    img = Image.new("RGBA", (N, N), CLEAR)
    brushed = vs.layered(77311, ((16, 2, 0.6), (8, 1, 0.4)))
    for y in range(N):
        for x in range(N):
            level = 3.6 + (7.5 - y) * 0.06 + (brushed[y][x] - 0.5) * 0.9
            if 3 <= y <= 5 and 2 <= x <= 13:
                level += 0.8                                                  # ein Glanz quer über die Platte
            if x == 0 or y == 0:
                level = 5.4
            elif x == N - 1 or y == N - 1:
                level = 1.3
            elif x == 1 or y == 1:
                level = max(level, 4.6)
            elif x == N - 2 or y == N - 2:
                level = min(level, 2.4)
            img.putpixel((x, y), _px(BRONZE, level))
    for cx, cy in ((3, 3), (12, 3), (3, 12), (12, 12)):
        img.putpixel((cx, cy), _px(BRASS, 6))
        img.putpixel((cx + 1, cy), _px(BRASS, 3))
        img.putpixel((cx, cy + 1), _px(BRASS, 3))
        img.putpixel((cx + 1, cy + 1), _px(BRONZE, 1.5))
    img.putpixel((6, N - 3), _px(PATINA, 2))                                  # ein Hauch Grünspan unten
    img.putpixel((10, N - 3), _px(PATINA, 3))
    return img


# =============================================================================
# Uhrwerk: zwei Zahnräder hinter einem Rahmen
# =============================================================================

def _gear_level(x: float, y: float, cx: float, cy: float, r: float, teeth: int, angle: float) -> float | None:
    """Stufe eines Zahnrads an (x, y) – oder None, wenn dort keins ist (oder eine Lücke zwischen den Speichen).
    -1 = die Nabe (Chronit). Ein Zahnrad: Zähne außen, ein schmaler Kranz, drei Speichen, die Nabe."""
    dx, dy = x - cx, y - cy
    d = math.hypot(dx, dy)
    if d > r:
        return None
    a = math.atan2(dy, dx) - angle
    root = r - 1.25
    if d > root:
        k = (a / (2 * math.pi) * teeth) % 1.0
        if abs(k - 0.5) > 0.23:
            return None
    light = -(dx + dy) / max(0.5, d) * 0.55
    if d > root - 1.05:
        return 4.0 + light + (0.5 if d > root else 0.0)                    # Kranz und Zähne
    if d < 1.25:
        return -1.0                                                         # die Nabe
    if d < 1.9:
        return 3.6 + light                                                  # der Ring um die Nabe
    for i in range(3):
        sa = angle + i * 2 * math.pi / 3
        across = abs(dx * math.sin(sa) - dy * math.cos(sa))
        along = dx * math.cos(sa) + dy * math.sin(sa)
        if across < 0.55 and along > 0:
            return 3.3 + light                                              # eine Speiche
    return None


def clockwork_frame(f: int, frames: int = GEAR_FRAMES, border: bool = True) -> Image.Image:
    """Ein Bild des Uhrwerks; die Zahnräder werden je Pixel 4×4-fach abgetastet (saubere Kanten auch beim Drehen)."""
    img = Image.new("RGBA", (N, N), CLEAR)
    big = (6.8, 8.8, 4.9, 6)
    small = (11.9, 4.1, 2.6, 4)
    ang_big = f / frames * (2 * math.pi / big[3])
    ang_small = -ang_big * big[3] / small[3] + math.pi / small[3]
    for y in range(N):
        for x in range(N):
            if border and (x < 2 or y < 2 or x > N - 3 or y > N - 3):
                # der Rahmen: Bronze, abgefast
                if x == 0 or y == 0:
                    level = 5.2
                elif x == N - 1 or y == N - 1:
                    level = 1.2
                elif x == 1 or y == 1:
                    level = 4.2
                else:
                    level = 2.2
                img.putpixel((x, y), _px(BRONZE, level))
                continue
            hits_big, hits_small, hub, light = 0, 0, 0, 0.0
            for sy in range(4):
                for sx in range(4):
                    px, py = x + (sx + 0.5) / 4.0, y + (sy + 0.5) / 4.0
                    lv = _gear_level(px, py, *big[:3], big[3], ang_big)
                    if lv is not None:
                        if lv < 0:
                            hub += 1
                        else:
                            hits_big += 1
                            light += lv
                        continue
                    lv = _gear_level(px, py, *small[:3], small[3], ang_small)
                    if lv is not None:
                        if lv < 0:
                            hub += 1
                        else:
                            hits_small += 1
                            light += lv
            total = hits_big + hits_small
            if hub >= 8:
                img.putpixel((x, y), _px(AMBER, 4))
            elif total + hub >= 8:
                ring = BRASS if hits_big >= hits_small else BRONZE
                img.putpixel((x, y), _px(ring, light / max(1, total)))
            else:
                dark = 1.4 + 0.6 * math.sin(x * 0.7 + y * 0.3) + total * 0.15
                img.putpixel((x, y), _px(WORKS, dark))
    if border:
        for cx, cy in ((2, 2), (13, 2), (2, 13), (13, 13)):
            img.putpixel((cx, cy), _px(BRASS, 5))
    return img


def clockwork() -> Image.Image:
    sheet = Image.new("RGBA", (N, N * GEAR_FRAMES), CLEAR)
    for f in range(GEAR_FRAMES):
        sheet.alpha_composite(clockwork_frame(f), (0, f * N))
    return sheet


# =============================================================================
# Zifferblatt (Email)
# =============================================================================

def clock_face() -> Image.Image:
    """
    Altes Email: elfenbeinfarben, eine leise Wolke, hier und da ein heller Schimmer – und ein Netz haarfeiner Risse
    (Craquelé), das über die Kanten weiterläuft (nahtlos, ohne sichtbares Raster).
    """
    img = Image.new("RGBA", (N, N), CLEAR)
    cloud = vs.layered(51207, ((8, 8, 0.6), (4, 4, 0.4)))
    shine = vs.white(51209)
    cracks = [[0.0] * N for _ in range(N)]
    import random
    rnd = random.Random(51211)
    for _ in range(2):
        x, y = rnd.randrange(N), rnd.randrange(N)
        dx, dy = rnd.choice(((1, 0), (0, 1), (1, 1), (1, -1)))
        for _ in range(rnd.randrange(6, 10)):
            cracks[y % N][x % N] = 1.0
            # meist geradeaus, selten ein Knick
            if rnd.random() < 0.2:
                dx, dy = rnd.choice(((1, 0), (0, 1), (1, 1), (1, -1)))
            x += dx
            y += dy
    for y in range(N):
        for x in range(N):
            level = 3.75 + (cloud[y][x] - 0.5) * 0.9 + (0.55 if shine[y][x] > 0.95 else 0.0) - (0.45 if shine[y][x] < 0.03 else 0.0)
            level -= 0.45 * cracks[y][x]
            img.putpixel((x, y), _px(ENAMEL, level))
    return img


# =============================================================================
# Uhrwerkherz
# =============================================================================

C0 = 7.5
R_RING = 7.6


def _marks() -> list[tuple[int, int, bool]]:
    out = []
    for h in range(12):
        a = h * math.pi / 6.0 - math.pi / 2.0
        r = 5.3
        out.append((int(math.floor(C0 + 0.5 + math.cos(a) * r)), int(math.floor(C0 + 0.5 + math.sin(a) * r)), h % 3 == 0))
    return out


def heart_top(defeated: bool) -> Image.Image:
    img = Image.new("RGBA", (N, N), CLEAR)
    for y in range(N):
        for x in range(N):
            dx, dy = x + 0.5 - (C0 + 0.5), y + 0.5 - (C0 + 0.5)
            r = math.hypot(dx, dy)
            if r > R_RING - 0.4 or x in (0, N - 1) or y in (0, N - 1):
                # die Ecken: Bronze (wie die Uhrbronze, abgefast)
                level = 5.2 if x == 0 or y == 0 else 1.2 if x == N - 1 or y == N - 1 else 3.2 + (-dy - dx) * 0.06
                img.putpixel((x, y), _px(BRONZE, level))
            elif r > R_RING - 1.5:
                img.putpixel((x, y), _px(BRASS, 5.5 if dy < -1 else 4 if dy < 2 else 3))   # der Ring
            elif r > R_RING - 2.0:
                img.putpixel((x, y), _px(BRONZE, 1.5))
            else:
                base = 4.2 if not defeated else 2.6
                img.putpixel((x, y), _px(ENAMEL, base + (0.3 if dy < 0 else 0.0)))
    for x, y, main in _marks():
        img.putpixel((x, y), _px(INK, 1 if main else 3))
    for x, y in ((7, 7), (8, 7), (7, 8), (8, 8)):
        img.putpixel((x, y), _px(AMBER, 2 if defeated else 4))
    if defeated:
        # gesprungen, die Zeiger herabgefallen (halb sieben)
        for x, y in ((5, 3), (5, 4), (6, 5), (6, 6), (9, 10), (10, 11), (11, 11), (11, 12)):
            img.putpixel((x, y), _px(INK, 2))
        for y in range(9, 13):
            img.putpixel((8, y), _px(INK, 0))
        img.putpixel((7, 10), _px(INK, 1))
        img.putpixel((7, 11), _px(INK, 1))
    return img


def _hand_pixels(angle: float, length: float, width: float) -> list[tuple[int, int, float]]:
    """Pixel eines Zeigers (0 = zwölf Uhr, im Uhrzeigersinn) mit Helligkeit 0..1 (zur Spitze heller)."""
    out = []
    ux, uy = math.sin(angle), -math.cos(angle)
    for y in range(N):
        for x in range(N):
            dx, dy = x + 0.5 - (C0 + 1.0), y + 0.5 - (C0 + 1.0)
            along = dx * ux + dy * uy
            across = abs(dx * uy - dy * ux)
            if -0.6 < along < length and across < width:
                out.append((x, y, along / length))
    return out


def heart_glow(awake: bool) -> Image.Image:
    """Die Leuchtebene: Marken und Nabe glimmen; wach laufen die Zeiger (eine Umdrehung des großen je Bildfolge)."""
    frames = HEART_FRAMES if awake else 1
    sheet = Image.new("RGBA", (N, N * frames), CLEAR)
    for f in range(frames):
        frame = Image.new("RGBA", (N, N), CLEAR)
        if awake:
            minute = f / frames * 2 * math.pi
            hour = minute / 12.0 + 2 * math.pi * (11.0 / 12.0)
        else:
            minute = -math.pi / 30.0                                          # eine Minute vor zwölf
            hour = -math.pi / 360.0
        level = 5 if awake else 2
        for x, y, main in _marks():
            if main:
                frame.putpixel((x, y), AMBER[level])
        for x, y, k in _hand_pixels(hour, 3.6, 0.75):
            frame.putpixel((x, y), AMBER[max(1, level - 1)])
        for x, y, k in _hand_pixels(minute, 5.6, 0.55):
            frame.putpixel((x, y), AMBER[min(6, level + (1 if k > 0.6 else 0))])
        for x, y in ((7, 7), (8, 7), (7, 8), (8, 8)):
            frame.putpixel((x, y), AMBER[6 if awake else 3])
        sheet.alpha_composite(frame, (0, f * N))
    return sheet


def heart_side() -> Image.Image:
    img = clockwork_frame(0)
    for x in range(N):
        img.putpixel((x, 0), _px(BRASS, 6))
        img.putpixel((x, 1), _px(BRASS, 4))
    return img


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/clock_bronze", clock_bronze())
    put("block/clockwork", clockwork())
    ANIMATIONS["block/clockwork"] = (GEAR_FRAMES, 4, False)
    put("block/clock_face", clock_face())
    put("block/clock_heart_top", heart_top(False))
    put("block/clock_heart_top_defeated", heart_top(True))
    put("block/clock_heart_side", heart_side())
    put("block/clock_heart_glow_sleeping", heart_glow(False))
    put("block/clock_heart_glow_awake", heart_glow(True))
    ANIMATIONS["block/clock_heart_glow_awake"] = (HEART_FRAMES, 5, False)
    put("block/clock_heart_glow_defeated", Image.new("RGBA", (N, N), CLEAR))


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda k, i: out.__setitem__(k, i))
    z = 6
    tiles = []
    for key in ("block/clock_bronze", "block/clock_face"):
        t = Image.new("RGBA", (N * 3, N * 3))
        for dx in range(3):
            for dy in range(3):
                t.alpha_composite(out[key], (dx * N, dy * N))
        tiles.append(t)
    for f in (0, 3):
        t = Image.new("RGBA", (N * 3, N * 3))
        fr = out["block/clockwork"].crop((0, f * N, N, (f + 1) * N))
        for dx in range(3):
            for dy in range(3):
                t.alpha_composite(fr, (dx * N, dy * N))
        tiles.append(t)
    for key, glow, frame in (("block/clock_heart_top", "block/clock_heart_glow_sleeping", 0), ("block/clock_heart_top", "block/clock_heart_glow_awake", 3),
                             ("block/clock_heart_top_defeated", None, 0)):
        t = Image.new("RGBA", (N * 3, N * 3), (40, 40, 48, 255))
        base = out[key].copy()
        if glow:
            base.alpha_composite(out[glow].crop((0, frame * N, N, (frame + 1) * N)))
        t.alpha_composite(base.resize((N * 2, N * 2), Image.NEAREST), (N // 2, N // 2))
        tiles.append(t)
    sheet = Image.new("RGBA", (len(tiles) * (N * 3 * z + 10), N * 3 * z), (52, 50, 62, 255))
    for i, t in enumerate(tiles):
        sheet.alpha_composite(t.resize((N * 3 * z, N * 3 * z), Image.NEAREST), (i * (N * 3 * z + 10), 0))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "clocktower_preview.png")
