#!/usr/bin/env python3
"""
Der Seelenhirte (0.98): die Seelenglocke und seine Irrlichter.

  * Seelenglocke – eine schwere Glocke aus dunkler, grün angelaufener Bronze. Die Seitentextur ist ein Aufriss der ganzen
    Glocke (das Modell nimmt je Teil die passenden Zeilen): oben die Schulter mit einem Wulst, in der Mitte die Flanke mit
    einem Band eingravierter Seelenrunen (in den Rillen ein Hauch Türkis), unten der Schlagring; Grünspan läuft in Tropfen
    herab. Dazu die eiserne Krone, der Schlagring von unten, das dunkle Innere (von unten türkis beschienen) und der Mund:
    Seelenfeuer, das in der Glocke wirbelt (Bildfolge, nahtlos) – erloschen nur noch kalte Asche.
  * Irrlicht – eine kleine Flamme aus Seelenfeuer mit zwei dunklen Augen; dazu ein weicher, türkiser Schein.

Wird von tools/generate_textures.py aufgerufen: build(put); die Bildfolgen stehen in ANIMATIONS.
"""
from __future__ import annotations

import math
from typing import Callable

from PIL import Image

import pixelart as pa
import vanilla_style as vs
from pixelart import hexc

N = 16
CLEAR = (0, 0, 0, 0)

#: angelaufene Bronze (dunkel -> hell)
BRONZE = vs.tones("#6a5a38", 7, spread=0.6, cool=200, warm=60, hue=18, sat=0.1)
VERDIGRIS = [hexc("#1c463c"), hexc("#2c6656"), hexc("#46907a"), hexc("#78bea4")]
IRON = [hexc("#121316"), hexc("#1d1f24"), hexc("#2b2e35"), hexc("#3b3f48"), hexc("#50555f"), hexc("#6a707b"), hexc("#8a919c")]
SOUL = [hexc("#06302e"), hexc("#0c5a54"), hexc("#16908a"), hexc("#2ac8bc"), hexc("#6af0e0"), hexc("#b8fff2"), hexc("#f0fffc")]
ASH = [hexc("#0a0b0c"), hexc("#131518"), hexc("#1c1f23"), hexc("#272a2f"), hexc("#34383e")]
MOUTH_FRAMES = 12

ANIMATIONS: dict[str, tuple[int, int, bool]] = {}


def _noise(seed: int) -> list[list[float]]:
    return vs.layered(seed, ((8, 8, 0.55), (4, 4, 0.3), (2, 2, 0.15)))


# Seelenrunen: je Rune 3 × 2 Pixel (gesetzte Punkte), im Band nebeneinander
_RUNES = ({(0, 0), (1, 1), (2, 0)}, {(1, 0), (0, 1), (2, 1)}, {(0, 0), (0, 1), (2, 0)}, {(1, 0), (1, 1), (2, 1)},
          {(0, 1), (1, 0), (2, 0)})


def bell_side() -> Image.Image:
    """Der Aufriss der Glocke (Zeilen 3–4 Schulter, 5–11 Flanke mit dem Runenband in 7–8, 12–13 Schlagring)."""
    n = _noise(5201)
    img = Image.new("RGBA", (N, N), CLEAR)
    for y in range(N):
        for x in range(N):
            v = n[y][x]
            lv = 3 + (1 if v > 0.62 else 0) - (1 if v < 0.28 else 0)
            if x <= 1:
                lv += 1                                          # Licht von links
            elif x >= 14:
                lv -= 1
            if y in (3, 12):
                lv = 5                                           # die Wülste im Licht
            elif y in (4, 13):
                lv = 2
            elif y in (6, 9):
                lv = 2                                           # die Linien um das Runenband
            elif y == 11:
                lv -= 1
            img.putpixel((x, y), BRONZE[max(0, min(6, lv))])
    # das Runenband (Zeilen 7–8): eingraviert, in den Rillen ein Hauch Seelenlicht
    for y in (7, 8):
        for x in range(N):
            img.putpixel((x, y), BRONZE[2])
    for i in range(5):
        rune = _RUNES[(i * 3 + 1) % len(_RUNES)]
        x0 = 1 + i * 3
        for (dx, dy) in rune:
            x, y = x0 + dx, 7 + dy
            if x < N:
                img.putpixel((x, y), pa.mix(BRONZE[1], SOUL[3], 0.75))
    # Grünspan: Flecken unter den Wülsten, kurze Tropfen
    for (x, y0, length) in ((2, 4, 2), (11, 4, 3), (5, 10, 2), (9, 10, 1), (13, 13, 2), (6, 13, 1)):
        for i in range(length):
            if y0 + i < N:
                img.putpixel((x, y0 + i), VERDIGRIS[max(0, 3 - i)])
    return img


def bell_top() -> Image.Image:
    """Die Schulter von oben: ein heller Ring, zur Mitte dunkler (dort sitzt die Krone)."""
    n = _noise(5202)
    img = Image.new("RGBA", (N, N), CLEAR)
    for y in range(N):
        for x in range(N):
            d = max(abs(x - 7.5), abs(y - 7.5))
            lv = 4 if d > 5 else 3
            if d > 6.5:
                lv = 5
            if n[y][x] > 0.68:
                lv += 1
            if n[y][x] < 0.18 and d > 4:
                img.putpixel((x, y), VERDIGRIS[1])
                continue
            img.putpixel((x, y), BRONZE[max(0, min(6, lv))])
    return img


def bell_lip() -> Image.Image:
    """Der Schlagring: oben eine helle Kante, unten im Schatten, abgegriffen."""
    n = _noise(5203)
    img = Image.new("RGBA", (N, N), CLEAR)
    for y in range(N):
        for x in range(N):
            lv = 4 + (1 if n[y][x] > 0.6 else 0)
            if y % 4 == 0:
                lv = 5
            elif y % 4 == 3:
                lv = 2
            img.putpixel((x, y), BRONZE[max(0, min(6, lv))])
    return img


def bell_inside() -> Image.Image:
    """Das Innere: fast schwarz, unten vom Seelenfeuer türkis beschienen."""
    n = _noise(5204)
    img = Image.new("RGBA", (N, N), CLEAR)
    for y in range(N):
        for x in range(N):
            c = BRONZE[0 if n[y][x] < 0.5 else 1]
            k = max(0.0, (y - 8) / 8.0) * 0.5
            img.putpixel((x, y), pa.mix(c, SOUL[2], k))
    return img


def bell_crown() -> Image.Image:
    """Die eiserne Krone: geschmiedete Bänder mit Nieten."""
    n = _noise(5205)
    img = Image.new("RGBA", (N, N), CLEAR)
    for y in range(N):
        for x in range(N):
            lv = 3 + (1 if n[y][x] > 0.6 else 0) - (1 if n[y][x] < 0.3 else 0)
            if y % 5 == 0:
                lv = 5
            elif y % 5 == 4:
                lv = 1
            if y % 5 == 2 and x % 4 == 1:
                lv = 6
            img.putpixel((x, y), IRON[max(0, min(6, lv))])
    return img


def mouth_frames() -> Image.Image:
    """Seelenfeuer im Mund der Glocke, von unten gesehen: ein Wirbel aus Türkis um einen gleißenden Kern (nahtlos)."""
    frames = Image.new("RGBA", (N, N * MOUTH_FRAMES), CLEAR)
    tau = 2.0 * math.pi
    c = (N - 1) / 2.0
    for f in range(MOUTH_FRAMES):
        t = f / MOUTH_FRAMES
        for y in range(N):
            for x in range(N):
                dx, dy = x - c, y - c
                r = math.hypot(dx, dy) / (N / 2.0)
                ang = math.atan2(dy, dx)
                swirl = math.sin(ang * 3.0 - r * 6.0 + tau * t) * 0.5 + math.sin(ang * 5.0 + r * 4.0 - 2.0 * tau * t) * 0.3
                lv = 5.6 - r * 4.2 + swirl * 1.1
                frames.putpixel((x, f * N + y), SOUL[max(0, min(6, int(round(lv))))])
    return frames


def mouth_cold() -> Image.Image:
    """Erloschen: kalte Asche, ein paar letzte, dunkle Funken."""
    n = _noise(5206)
    img = Image.new("RGBA", (N, N), CLEAR)
    for y in range(N):
        for x in range(N):
            img.putpixel((x, y), ASH[min(4, int(n[y][x] * 5.0))])
    for (x, y) in ((5, 6), (10, 9), (7, 11)):
        img.putpixel((x, y), pa.mix(ASH[4], SOUL[1], 0.5))
    return img


def wisp() -> tuple[Image.Image, Image.Image]:
    """Das Irrlicht: eine Flamme (unten rund, oben spitz, leicht geschwungen) aus Seelenfeuer mit zwei dunklen Augen; der
    Schein ist eine weiche, runde Aura."""
    body = Image.new("RGBA", (N, N), CLEAR)
    halo = Image.new("RGBA", (N, N), CLEAR)
    for y in range(N):
        for x in range(N):
            # Flamme: Kreis unten (Mitte 7.5, 10), nach oben spitz zulaufend
            fy = (y - 2.0) / 12.0                              # 0 oben … 1 unten
            width = 5.2 * math.sin(min(1.0, max(0.0, fy)) * math.pi * 0.62) + 0.4
            sway = math.sin(fy * 5.0) * 0.8 * (1.0 - fy)
            dx = abs(x - 7.5 - sway)
            if 0.0 <= fy <= 1.0 and dx <= width:
                k = dx / max(0.5, width)
                lv = 6.0 - k * 2.6 - abs(fy - 0.7) * 2.2
                col = SOUL[max(1, min(6, int(round(lv))))]
                body.putpixel((x, y), col)
            d = math.hypot(x - 7.5, y - 9.0) / 8.0
            if d < 1.0:
                a = (1.0 - d) ** 1.6
                col = pa.mix(SOUL[3], SOUL[5], 1.0 - d)
                halo.putpixel((x, y), (col[0], col[1], col[2], int(235 * a)))
    for (x, y) in ((6, 9), (9, 9)):
        body.putpixel((x, y), hexc("#04201e"))
        body.putpixel((x, y + 1), hexc("#0a3a36"))
    return body, halo


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/soul_bell_side", bell_side())
    put("block/soul_bell_top", bell_top())
    put("block/soul_bell_lip", bell_lip())
    put("block/soul_bell_inside", bell_inside())
    put("block/soul_bell_crown", bell_crown())
    put("block/soul_bell_mouth", mouth_frames())
    ANIMATIONS["block/soul_bell_mouth"] = (MOUTH_FRAMES, 3, True)
    put("block/soul_bell_mouth_cold", mouth_cold())
    body, halo = wisp()
    put("entity/soul_shepherd/wisp", body)
    put("entity/soul_shepherd/wisp_glow", halo)


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda k, img: out.__setitem__(k, img))
    keys = list(out)
    scale = 12
    sheet = Image.new("RGBA", (len(keys) * (N * scale + 8), N * scale), (40, 44, 52, 255))
    for i, k in enumerate(keys):
        img = out[k].crop((0, 0, N, N)).resize((N * scale, N * scale), Image.NEAREST)
        sheet.alpha_composite(img, (i * (N * scale + 8), 0))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "shepherd_textures.png")
