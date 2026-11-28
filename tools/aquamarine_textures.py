#!/usr/bin/env python3
"""
Aquamarin (0.97): der Aquamarinblock und der Aquamarin selbst.

  * Aquamarinblock – ein großer Stein im Treppenschliff (wie ein Smaragdschliff von oben): ein abgefaster Rahmen, zwei
    Stufen ringsum (oben hell, unten dunkel) und die Tafel in der Mitte, in der sich die Stufen der Unterseite gespiegelt
    zeigen (heller und dunkler im Wechsel – der „Spiegelsaal" eines Treppenschliffs). Darüber zieht ein sanftes
    Lichtnetz wie Sonnenlicht durch Wasser (Leuchtebene, bewegt; nahtlos – nebeneinander gesetzte Blöcke spielen als
    eine Fläche).
  * Aquamarin – derselbe Schliff als hochkant geschliffener Edelstein, meerblau mit hellem Glanz.

Wird von tools/urmeer_textures.py aufgerufen: build(put, animations).
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

#: der Stein: klares Meerblau, in den Tiefen ein Hauch Tintenblau, in den Lichtern fast Weiß
STONE = st.ramp("#4fb2d2", dark=0.36, light=0.97, cool=255, hue_dark=18, chroma_dark=1.25, warm=185, hue_light=10, chroma_light=0.3)
#: das Lichtnetz (dunkel -> hell)
CAUSTIC = [hexc("#8fe4f2"), hexc("#b4f2fa"), hexc("#d8fbff"), hexc("#f4ffff")]
#: Bilder im Lauf des Lichtnetzes (je 4 Ticks)
CAUSTIC_FRAMES = 16


def _ring(x: int, y: int, x0: int, y0: int, x1: int, y1: int) -> int:
    """Wie weit (in Pixeln) liegt (x, y) innerhalb des Rechtecks x0..x1, y0..y1? 0 = auf dem Rand."""
    return min(x - x0, x1 - x, y - y0, y1 - y)


def _side(x: int, y: int, x0: int, y0: int, x1: int, y1: int) -> str:
    """Zu welcher Seite des Rechtecks gehört (x, y)? Auf den Ecken-Diagonalen 'tl', 'tr', 'bl', 'br'."""
    top, bottom, left, right = y - y0, y1 - y, x - x0, x1 - x
    m = min(top, bottom, left, right)
    if top == m and left == m:
        return "tl"
    if top == m and right == m:
        return "tr"
    if bottom == m and left == m:
        return "bl"
    if bottom == m and right == m:
        return "br"
    return {top: "t", bottom: "b", left: "l", right: "r"}[m]


#: Stufen ringsum: (Ring von außen, Licht je Seite). Die Tafel spiegelt die Unterseite: dort ist das Licht umgekehrt.
BLOCK_RINGS = [
    {"t": 4, "l": 4, "r": 1, "b": 0, "tl": 5, "tr": 3, "bl": 2, "br": 0},       # Fase am Rand
    {"t": 6, "l": 5, "r": 3, "b": 2, "tl": 6, "tr": 5, "bl": 4, "br": 2},       # erste Stufe
    {"t": 5, "l": 5, "r": 3, "b": 2, "tl": 6, "tr": 4, "bl": 3, "br": 1},
    {"t": 3, "l": 3, "r": 2, "b": 1, "tl": 4, "tr": 3, "bl": 2, "br": 1},       # zweite Stufe (tiefer)
    {"t": 1, "l": 2, "r": 4, "b": 5, "tl": 1, "tr": 3, "bl": 3, "br": 6},       # Tafel: die gespiegelte Unterseite
    {"t": 3, "l": 3, "r": 4, "b": 4, "tl": 3, "tr": 4, "bl": 4, "br": 5},
    {"t": 5, "l": 5, "r": 4, "b": 4, "tl": 6, "tr": 4, "bl": 4, "br": 3},
    {"t": 4, "l": 4, "r": 4, "b": 4, "tl": 4, "tr": 4, "bl": 4, "br": 4},
]


def aquamarine_block() -> tuple[Image.Image, Image.Image]:
    """Der Block und sein Lichtnetz (Bildfolge, senkrecht gestapelt)."""
    cloud = vs.layered(70417, ((8, 8, 0.55), (4, 4, 0.3), (2, 2, 0.15)))
    g = vs.grid(3)
    for y in range(N):
        for x in range(N):
            r = _ring(x, y, 0, 0, N - 1, N - 1)
            side = _side(x, y, 0, 0, N - 1, N - 1)
            tone = BLOCK_RINGS[min(r, len(BLOCK_RINGS) - 1)][side]
            # ein wenig Wolke im Stein (nur innerhalb der Flächen, nie auf den Graten)
            if side in ("t", "b", "l", "r") and 0 < r:
                if cloud[y][x] > 0.74:
                    tone = min(6, tone + 1)
                elif cloud[y][x] < 0.2:
                    tone = max(1, tone - 1)
            g[y][x] = tone
    # zwei Einschlüsse (feine Nadeln, wie sie im Aquamarin liegen) und ein Glanz in der Tafel
    for x, y in ((6, 9), (7, 10)):
        g[y][x] = 2
    for x, y in ((6, 6), (7, 6), (6, 7)):
        g[y][x] = 6
    img = vs.paint(g, STONE)
    for x, y in ((1, 1), (2, 1), (1, 2), (6, 6)):
        img.putpixel((x, y), pa.mix(img.getpixel((x, y)), hexc("#ffffff"), 0.55))

    # das Lichtnetz: helle, sich verschlingende Linien, die langsam wandern; auf allen Rändern nahtlos (Wellen mit ganzen
    # Wellenzahlen je Block) und über die Bildfolge geschlossen (ganze Umläufe je Schleife)
    frames = Image.new("RGBA", (N, N * CAUSTIC_FRAMES), CLEAR)
    tau = 2.0 * math.pi
    for f in range(CAUSTIC_FRAMES):
        t = f / CAUSTIC_FRAMES
        for y in range(N):
            for x in range(N):
                u, v = x / N, y / N
                a = math.sin(tau * (u + t)) + math.sin(tau * (v - t))
                b = math.sin(tau * (u + v + 2 * t)) + math.sin(tau * (2 * u - v - t))
                c = math.sin(tau * (u - 2 * v + t)) + 0.6 * math.sin(tau * (2 * u + 2 * v - t))
                # Kaustik: helle Linien dort, wo sich die Wellen gerade aufheben
                k = 1.0 - min(1.0, abs(a + b + c) / 1.25)
                k = k ** 2.4
                if k < 0.34:
                    continue
                level = min(len(CAUSTIC) - 1, int((k - 0.34) / 0.17))
                base = img.getpixel((x, y))
                frames.putpixel((x, f * N + y), pa.mix(base, CAUSTIC[level], 0.45 + 0.12 * level))
    return img, frames


# =============================================================================
# Der Aquamarin (Edelstein, hochkant im Treppenschliff)
# =============================================================================

GEM = st.ramp("#5fbcd8", dark=0.3, light=0.98, cool=258, hue_dark=22, chroma_dark=1.35, warm=180, hue_light=8, chroma_light=0.35)
GEM_GLINT = st.ramp("#bdf4ff", dark=0.5, light=1.0, cool=230, warm=190, chroma_light=0.2)

def aquamarine_gem() -> Image.Image:
    """
    Ein Aquamarinkristall: eine sechseckige Säule, schräg von unten links nach oben rechts, oben in zwei Flächen spitz
    zulaufend, unten gebrochen. Drei Längsflächen: oben links im Licht, die mittlere, unten rechts im Schatten; zwischen
    der hellen und der mittleren ein heller Grat, quer durch den Stein ein dunkler Spiegel (wie in klarem Glas).
    """
    p0 = (2.6, 13.9)          # unten links (Bruchkante)
    p1 = (13.2, 2.8)          # oben rechts (Spitze)
    ax, ay = p1[0] - p0[0], p1[1] - p0[1]
    length = math.hypot(ax, ay)
    ux, uy = ax / length, ay / length           # entlang der Säule
    nx, ny = -uy, ux                            # quer: negative Werte oben links
    s = st.Spr()
    tip = length - 3.4
    for y in range(N):
        for x in range(N):
            px, py = x + 0.5 - p0[0], y + 0.5 - p0[1]
            a = px * ux + py * uy
            d = px * nx + py * ny
            hw = 3.05 if a < tip else 3.05 * max(0.0, (length - a) / 3.4) + 0.3
            # gebrochene Unterseite: schräg und ein wenig zackig
            base = 0.4 + 0.55 * (d + 3.05) / 3.05 + (0.6 if (x * 7 + y * 3) % 5 == 0 else 0.0)
            if a < base or a > length or abs(d) > hw:
                continue
            if a >= tip:
                lv = ("D", 6) if d < -0.3 else ("A", 4)
            elif d < -1.15:
                lv = ("A", 5)
            elif d < -0.55:
                lv = ("D", 5)                       # der helle Grat
            elif d <= 1.0:
                lv = ("A", 3)
            else:
                lv = ("A", 1)
            # quer durch den Stein ein dunkler Spiegel, auf der Lichtseite ein heller
            if tip - 4.2 < a < tip - 2.9 and lv[0] == "A":
                lv = ("A", max(1, lv[1] - 1)) if d > -1.15 else ("D", 6)
            if a < base + 0.9 and lv[0] == "A":
                lv = ("A", max(1, lv[1] - 1))       # die Bruchkante dunkler
            s.set(x, y, lv)
    return s.outline(prefer=("A", "D"), override={"D": "A"}).image({"A": GEM, "D": GEM_GLINT})


def build(put: Callable[[str, Image.Image], None], animations: dict) -> None:
    block, caustic = aquamarine_block()
    put("block/aquamarine_block", block)
    put("block/aquamarine_block_glow", caustic)
    animations["block/aquamarine_block_glow"] = (CAUSTIC_FRAMES, 4, True)
    put("item/aquamarine", aquamarine_gem())


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda k, i: out.__setitem__(k, i), {})
    frames = [out["block/aquamarine_block_glow"].crop((0, f * N, N, (f + 1) * N)) for f in (0, 5, 10)]
    tiles = []
    for fr in frames:
        big = Image.new("RGBA", (N * 2, N * 2))
        for dx in (0, N):
            for dy in (0, N):
                big.alpha_composite(out["block/aquamarine_block"], (dx, dy))
                big.alpha_composite(fr, (dx, dy))
        tiles.append(big)
    plain = Image.new("RGBA", (N * 2, N * 2))
    for dx in (0, N):
        for dy in (0, N):
            plain.alpha_composite(out["block/aquamarine_block"], (dx, dy))
    gem = Image.new("RGBA", (N * 2, N * 2), (0, 0, 0, 0))
    gem.alpha_composite(out["item/aquamarine"].resize((N * 2, N * 2), Image.NEAREST))
    tiles = [plain] + tiles + [gem]
    z = 8
    sheet = Image.new("RGBA", (len(tiles) * (N * 2 * z + 10), N * 2 * z), (40, 40, 48, 255))
    for i, t in enumerate(tiles):
        sheet.alpha_composite(t.resize((N * 2 * z, N * 2 * z), Image.NEAREST), (i * (N * 2 * z + 10), 0))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "aquamarine.png")
