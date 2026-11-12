#!/usr/bin/env python3
"""
Die Meerhexe (0.97): der Hexenkessel und ihre Tintenkugeln.

  * Hexenkessel – ein schwerer Kessel aus geschwärzter Bronze, grün angelaufen, mit Nietenreihen und Seepocken; der Rand ist
    von schwarzer Koralle überwuchert (mit leuchtenden Polypen und Knochennieten); innen dunkel, oben vom Trank beschienen.
  * Der Trank – meergrün leuchtend, wirbelnd, mit aufsteigenden Blasen (Bildfolge, nahtlos); erkaltet eine trübe, dunkle Brühe.
  * Tintenkugel – ein dunkler, wirbelnder Kern aus Hexentinte mit weichem Rand, darüber ein leuchtender, meergrüner Saum.

Wird von tools/urmeer_textures.py aufgerufen: build(put, animations).
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

#: geschwärzte Bronze (dunkel -> hell)
BRONZE = vs.tones("#33403a", 7, spread=0.55, cool=200, warm=60, hue=14, sat=0.06)
#: grüner Belag (Grünspan)
VERDIGRIS = [hexc("#1f4a3e"), hexc("#2f6a56"), hexc("#4a8e74"), hexc("#7ab89a")]
#: schwarze Koralle
CORAL = [hexc("#0b070b"), hexc("#1c121a"), hexc("#2e1f2a"), hexc("#43303d"), hexc("#5a4252")]
POLYP = [hexc("#c04a9a"), hexc("#ff8ad0"), hexc("#ffd4f0")]
BONE = [hexc("#8a7c66"), hexc("#c8bba0"), hexc("#f2ead8")]
BARNACLE = [hexc("#5e6862"), hexc("#9aa49c"), hexc("#d4dad2")]
#: der Trank
BREW = [hexc("#06302a"), hexc("#0c5a48"), hexc("#14886a"), hexc("#2ab890"), hexc("#5ae8b8"), hexc("#a8ffe0"), hexc("#ecfff8")]
BREW_COLD = [hexc("#070b0a"), hexc("#0e1714"), hexc("#16231e"), hexc("#203229"), hexc("#2c4236")]
BREW_FRAMES = 16


def _noise(seed: int) -> list[list[float]]:
    return vs.layered(seed, ((8, 8, 0.55), (4, 4, 0.3), (2, 2, 0.15)))


def cauldron_side() -> Image.Image:
    """Außenwand: zwei Reihen gewölbter Bronzeplatten mit Nieten, darunter Grünspan, der in Tropfen hinabläuft, ein paar
    Seepocken; unten der dunkle Boden und die Füße."""
    n = _noise(5101)
    img = Image.new("RGBA", (N, N), CLEAR)
    for y in range(N):
        for x in range(N):
            v = n[y][x]
            if y < 11:
                row_top = 2 if y < 6 else 6
                k = y - row_top                                   # 0 oben an der Platte … 3 unten
                lv = 4 - (1 if k >= 2 else 0) + (1 if v > 0.66 else 0) - (1 if v < 0.25 else 0)
                if k == 0:
                    lv = 5                                        # die obere Kante im Licht
                if k == 3 or y == 10:
                    lv = 1                                        # die Fuge darunter
                if (x + (2 if row_top == 6 else 0)) % 8 == 0 and k < 3:
                    lv = min(lv, 2)                               # Stoß zwischen zwei Platten
                if k == 1 and (x + (2 if row_top == 6 else 0)) % 8 in (2, 6):
                    lv = 6                                        # Nieten
            else:
                lv = 2 + (1 if v > 0.5 else 0)
                if y == 11:
                    lv = 4
            img.putpixel((x, y), BRONZE[max(0, min(6, lv))])
    # Grünspan: an den Plattenkanten, in Tropfen hinab
    for (x, y0, length) in ((1, 2, 3), (5, 2, 2), (10, 2, 3), (13, 2, 1), (3, 6, 3), (8, 6, 2), (12, 6, 4), (15, 6, 2)):
        for i in range(length):
            img.putpixel((x, y0 + i), VERDIGRIS[max(0, 3 - i)])
        if length > 2:
            img.putpixel(((x + 1) % N, y0), VERDIGRIS[2])
    for (x, y) in ((6, 4), (14, 8), (2, 8)):
        img.putpixel((x, y), BARNACLE[1])
        img.putpixel((x, y - 1), BARNACLE[2])
        img.putpixel(((x + 1) % N, y), BARNACLE[0])
    return img


def cauldron_top() -> Image.Image:
    """Die Oberkante der Wände: blanke Bronze, innen dunkler."""
    img = Image.new("RGBA", (N, N), CLEAR)
    n = _noise(5102)
    for y in range(N):
        for x in range(N):
            lv = 4 + (1 if n[y][x] > 0.6 else 0)
            if min(x, y, N - 1 - x, N - 1 - y) >= 2:
                lv = 2
            img.putpixel((x, y), BRONZE[lv])
    return img


def cauldron_bottom() -> Image.Image:
    img = Image.new("RGBA", (N, N), CLEAR)
    n = _noise(5103)
    for y in range(N):
        for x in range(N):
            img.putpixel((x, y), BRONZE[1 + (1 if n[y][x] > 0.55 else 0)])
    return img


def cauldron_inside() -> Image.Image:
    """Innen: fast schwarz, oben vom Trank meergrün beschienen."""
    img = Image.new("RGBA", (N, N), CLEAR)
    n = _noise(5104)
    for y in range(N):
        for x in range(N):
            c = BRONZE[0 if n[y][x] < 0.5 else 1]
            k = max(0.0, 1.0 - y / 9.0) * 0.45
            img.putpixel((x, y), pa.mix(c, BREW[2], k))
    return img


def cauldron_rim() -> Image.Image:
    """Der Rand: von schwarzer Koralle überwuchert, mit leuchtenden Polypen und Knochennieten."""
    img = Image.new("RGBA", (N, N), CLEAR)
    n = _noise(5105)
    for y in range(N):
        for x in range(N):
            v = n[y][x]
            img.putpixel((x, y), CORAL[min(4, int(v * 5.0))])
    for (x, y) in ((2, 0), (9, 1), (13, 0), (5, 1), (6, 9), (12, 12), (3, 6), (14, 7), (8, 14), (1, 12), (10, 5)):
        img.putpixel((x, y), POLYP[1])
        if y + 1 < N:
            img.putpixel((x, y + 1), POLYP[0])
    for (x, y) in ((0, 1), (4, 0), (8, 0), (12, 1), (15, 0)):
        img.putpixel((x, y), BONE[2])
    return img


def brew_frames() -> Image.Image:
    """Der Trank: meergrüne Schlieren drehen sich langsam, Blasen steigen auf und platzen (nahtlos, geschlossene Folge)."""
    frames = Image.new("RGBA", (N, N * BREW_FRAMES), CLEAR)
    tau = 2.0 * math.pi
    bubbles = [(3, 4, 0), (11, 9, 5), (7, 12, 9), (12, 3, 12), (5, 9, 3), (9, 6, 14)]
    for f in range(BREW_FRAMES):
        t = f / BREW_FRAMES
        for y in range(N):
            for x in range(N):
                u, v = x / N, y / N
                a = math.sin(tau * (u + v + t)) + math.sin(tau * (2 * u - v - 2 * t)) * 0.6 + math.sin(tau * (u - 2 * v + t)) * 0.5
                lv = 3 + a * 0.9
                frames.putpixel((x, f * N + y), BREW[max(1, min(5, int(round(lv))))])
        for (bx, by, start) in bubbles:
            age = (f - start) % BREW_FRAMES
            if age < 5:
                r = 0 if age < 2 else 1
                c = BREW[6] if age < 4 else BREW[5]
                for dx in range(-r, r + 1):
                    for dy in range(-r, r + 1):
                        if abs(dx) + abs(dy) <= r:
                            frames.putpixel(((bx + dx) % N, f * N + (by + dy) % N), c)
    return frames


def brew_cold() -> Image.Image:
    img = Image.new("RGBA", (N, N), CLEAR)
    n = _noise(5106)
    for y in range(N):
        for x in range(N):
            img.putpixel((x, y), BREW_COLD[min(4, int(n[y][x] * 5.0))])
    for (x, y) in ((4, 5), (10, 11)):
        img.putpixel((x, y), BREW_COLD[4])
    return img


def ink_bolt() -> tuple[Image.Image, Image.Image]:
    """Die Tintenkugel: dunkler Wirbel mit weichem Rand, darüber der leuchtende Saum (eigene Leuchtschicht)."""
    ink = Image.new("RGBA", (N, N), CLEAR)
    glow = Image.new("RGBA", (N, N), CLEAR)
    c = (N - 1) / 2.0
    for y in range(N):
        for x in range(N):
            dx, dy = x - c, y - c
            r = math.hypot(dx, dy) / (N / 2.0)
            ang = math.atan2(dy, dx)
            swirl = math.sin(ang * 3.0 + r * 7.0)
            if r < 0.78:
                k = 0.18 + 0.12 * swirl + 0.1 * (1.0 - r)
                alpha = 255 if r < 0.6 else int(255 * (0.78 - r) / 0.18)
                base = pa.mix(hexc("#05080c"), hexc("#1c3a40"), max(0.0, min(1.0, k)))
                ink.putpixel((x, y), (base[0], base[1], base[2], alpha))
            if 0.5 < r < 0.95:
                ring = 1.0 - abs(r - 0.72) / 0.23
                spark = 0.5 + 0.5 * math.sin(ang * 5.0 + 1.3)
                a = max(0.0, ring) * (0.55 + 0.45 * spark)
                if a > 0.15:
                    col = pa.mix(hexc("#2ad8b0"), hexc("#d8fff2"), max(0.0, min(1.0, a - 0.3)))
                    glow.putpixel((x, y), (col[0], col[1], col[2], int(255 * min(1.0, a))))
    for (x, y) in ((5, 5), (6, 5)):
        glow.putpixel((x, y), (220, 255, 245, 230))
    return ink, glow


def build(put: Callable[[str, Image.Image], None], animations: dict) -> None:
    put("block/witch_cauldron_side", cauldron_side())
    put("block/witch_cauldron_top", cauldron_top())
    put("block/witch_cauldron_bottom", cauldron_bottom())
    put("block/witch_cauldron_inside", cauldron_inside())
    put("block/witch_cauldron_rim", cauldron_rim())
    put("block/witch_cauldron_brew", brew_frames())
    animations["block/witch_cauldron_brew"] = (BREW_FRAMES, 3, True)
    put("block/witch_cauldron_brew_cold", brew_cold())
    ink, glow = ink_bolt()
    put("entity/sea_witch/ink_bolt", ink)
    put("entity/sea_witch/ink_bolt_glow", glow)


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda k, i: out.__setitem__(k, i), {})
    keys = ["block/witch_cauldron_side", "block/witch_cauldron_top", "block/witch_cauldron_inside", "block/witch_cauldron_rim",
            "block/witch_cauldron_brew", "block/witch_cauldron_brew_cold", "entity/sea_witch/ink_bolt", "entity/sea_witch/ink_bolt_glow"]
    z = 10
    sheet = Image.new("RGBA", (len(keys) * (N * z + 8), N * z), (60, 60, 70, 255))
    for i, k in enumerate(keys):
        img = out[k].crop((0, 0, N, N))
        sheet.alpha_composite(img.resize((N * z, N * z), Image.NEAREST), (i * (N * z + 8), 0))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "witch.png")
