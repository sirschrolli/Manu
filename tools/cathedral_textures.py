#!/usr/bin/env python3
"""
Das Orgelpult der Stillen Kathedrale (seit 1.7): der Kasten des Spieltischs aus dunkler Eiche und das Notenpult darauf.

  * organ_desk_front – die Front des Spieltischs: ein Rahmen aus dunkler Eiche, darin zwei Reihen Registerknöpfe aus Bein mit
    dunklem Rand, dazwischen ein kleines Schild aus Kupfer mit einer Note;
  * organ_desk_side / organ_desk_top – Rahmen und Füllung aus dunkler Eiche, oben Bretter;
  * organ_desk_keys – das Manual: weiße Tasten aus Bein, hinten die schwarzen;
  * organ_desk_rack – das Notenpult von hinten: grün angelaufenes Kupfer;
  * organ_desk_sheet – das Notenblatt (Pergament, zwei Notenzeilen, Noten), in der Leuchtebene glimmen Linien und Noten türkis;
    organ_desk_sheet_silent – verstummt: grau, die Noten verblasst, Ascheflecken.

Wird von tools/generate_textures.py aufgerufen: build(put).
"""
from __future__ import annotations

from typing import Callable

from PIL import Image

N = 16
CLEAR = (0, 0, 0, 0)

#: dunkle Eiche: von der Fuge bis zur Kante im Licht
OAK = [(0x1e, 0x13, 0x09), (0x2b, 0x1c, 0x0e), (0x3a, 0x27, 0x14), (0x48, 0x31, 0x1a), (0x56, 0x3b, 0x21), (0x66, 0x47, 0x29)]
#: Bein (Tasten, Registerknöpfe)
IVORY = [(0x8a, 0x80, 0x6a), (0xb8, 0xae, 0x92), (0xd8, 0xd0, 0xb4), (0xee, 0xe8, 0xd2)]
EBONY = [(0x0c, 0x0b, 0x0e), (0x1c, 0x1a, 0x20), (0x2e, 0x2b, 0x34)]
#: grün angelaufenes Kupfer
VERDIGRIS = [(0x16, 0x3a, 0x33), (0x25, 0x59, 0x4b), (0x3b, 0x7d, 0x68), (0x5a, 0xa5, 0x8a), (0x86, 0xcd, 0xb0)]
COPPER = [(0x7a, 0x4a, 0x2c), (0xa8, 0x65, 0x3a), (0xd0, 0x8a, 0x52), (0xf2, 0xb9, 0x8a)]
#: Pergament
PARCHMENT = [(0x9c, 0x86, 0x5c), (0xb8, 0xa2, 0x74), (0xcc, 0xb8, 0x8a), (0xde, 0xcc, 0xa0), (0xec, 0xde, 0xb6)]
INK = (0x2a, 0x22, 0x1e)
GLOW = [(0x0b, 0x6f, 0x7c), (0x13, 0x9f, 0xac), (0x29, 0xdf, 0xeb), (0x8a, 0xf4, 0xfb), (0xe6, 0xff, 0xff)]
ASH = [(0x3a, 0x38, 0x36), (0x52, 0x4e, 0x4a), (0x6e, 0x6a, 0x64), (0x8a, 0x86, 0x7e), (0xa2, 0x9e, 0x96)]


def _h(*a: int) -> float:
    """Ein fester Zufall je Pixel (0–1)."""
    h = 2166136261
    for v in a:
        h = ((h ^ (v & 0xFFFFFFFF)) * 16777619) & 0xFFFFFFFF
    h ^= h >> 13
    h = (h * 0x5BD1E995) & 0xFFFFFFFF
    h ^= h >> 15
    return (h & 0xFFFF) / 65535.0


def _c(ramp: list, i: float) -> tuple:
    return ramp[max(0, min(len(ramp) - 1, int(round(i))))] + (255,)


def _img() -> Image.Image:
    return Image.new("RGBA", (N, N), CLEAR)


def _grain(x: int, y: int, seed: int, vertical: bool = True) -> float:
    """Maserung: Streifen entlang des Holzes, dazwischen ein wenig Rauschen."""
    along, across = (y, x) if vertical else (x, y)
    stripe = 0.6 if (across + int(_h(along // 5, across, seed) * 2)) % 3 == 0 else 0.0
    return stripe + (_h(x, y, seed) - 0.5) * 0.8


def panel(seed: int) -> Image.Image:
    """Rahmen und Füllung aus dunkler Eiche: außen ein Rahmen (oben und links im Licht), innen eine erhabene Füllung."""
    im = _img()
    px = im.load()
    for y in range(N):
        for x in range(N):
            if x == 0 or y == 0:
                level = 4.2
            elif x == N - 1 or y == N - 1:
                level = 1.0
            elif x in (1, 14) or y in (1, 14):
                level = 3.0 + (_h(x, y, seed) - 0.5)
            elif x == 2 or y == 2:
                level = 1.4                                                            # die Fuge um die Füllung
            elif x == 13 or y == 13:
                level = 3.6                                                            # ihre Kante im Licht
            else:
                level = 2.6 + _grain(x, y, seed)
            px[x, y] = _c(OAK, level)
    return im


def planks(seed: int) -> Image.Image:
    """Bretter aus dunkler Eiche (oben auf dem Kasten)."""
    im = _img()
    px = im.load()
    for y in range(N):
        for x in range(N):
            board = y // 4
            if y % 4 == 3:
                level = 1.0
            else:
                level = 2.6 + _grain(x + board * 7, y, seed, vertical=False) + (0.4 if y % 4 == 0 else 0.0)
                if (x + board * 5) % 16 == 0:
                    level = 1.2                                                        # Stoß zweier Bretter
            px[x, y] = _c(OAK, level)
    return im


def front() -> Image.Image:
    """Die Front: Rahmen, zwei Reihen Registerknöpfe aus Bein, in der Mitte ein Kupferschild mit einer Note."""
    im = panel(3101)
    px = im.load()
    for row_y in (4, 10):
        for kx in (3, 6, 9, 12):
            if row_y == 10 and kx in (6, 9):
                continue                                                               # dort sitzt das Schild
            # ein runder Knopf (2x2) mit dunklem Rand unten rechts und Glanz oben links
            px[kx, row_y] = _c(IVORY, 3)
            px[kx + 1, row_y] = _c(IVORY, 2)
            px[kx, row_y + 1] = _c(IVORY, 2)
            px[kx + 1, row_y + 1] = _c(IVORY, 1)
            px[kx + 2, row_y + 1] = _c(EBONY, 1)
            px[kx + 1, row_y + 2] = _c(EBONY, 1)
            px[kx, row_y + 2] = _c(EBONY, 0)
    # das Schild: Kupfer, darin eine Achtelnote
    for y in range(9, 13):
        for x in range(6, 11):
            edge = x in (6, 10) or y in (9, 12)
            px[x, y] = _c(COPPER, 1 if edge else 2 + (1 if (x + y) % 3 == 0 else 0))
    for (x, y) in ((8, 10), (8, 11), (7, 11), (9, 10)):
        px[x, y] = INK + (255,)
    return im


def keys() -> Image.Image:
    """Das Manual von oben: vorn (oben im Bild) die weißen Tasten, hinten die schwarzen. Nur die oberen acht Zeilen zählen."""
    im = planks(3121)
    px = im.load()
    black = {1, 2, 4, 5, 6}                                                            # je sieben Tasten: wo eine schwarze sitzt
    for y in range(0, 8):
        for x in range(N):
            if x % 2 == 1:
                px[x, y] = _c(EBONY, 0)                                                # die Fugen zwischen den Tasten
                continue
            key = x // 2
            if y >= 4 and key % 7 in black:
                px[x, y] = _c(EBONY, 2 if y == 4 else 1)
            else:
                px[x, y] = _c(IVORY, 3 if y == 0 else 2 if y < 3 else 1)
    return im


def rack() -> Image.Image:
    """Das Notenpult von hinten: grün angelaufenes Kupfer, mit einem Rand und schmalen Streben."""
    im = _img()
    px = im.load()
    for y in range(N):
        for x in range(N):
            if x in (0, N - 1) or y in (0, N - 1):
                level = 3.5 if y == 0 or x == 0 else 1.0
                px[x, y] = _c(VERDIGRIS, level)
                continue
            if x % 5 == 2:
                px[x, y] = _c(COPPER, 2 if _h(x, y, 3131) > 0.4 else 1)                # die Streben glänzen noch
                continue
            px[x, y] = _c(VERDIGRIS, 2.0 + (_h(x, y, 3132) - 0.5) * 1.6)
    return im


#: wo die Notenlinien liegen (zwei Zeilen zu je fünf Linien) und die Noten (x, Linie/2 ab der obersten Linie)
STAVES = (3, 10)
NOTES = ((3, 1), (5, 3), (7, 2), (9, 4), (11, 0), (13, 2), (4, 4), (6, 2), (8, 1), (11, 3), (13, 5))


def _sheet(base: list, ink, glow: bool, silent: bool) -> tuple[Image.Image, Image.Image]:
    im = _img()
    gl = _img()
    px = im.load()
    gx = gl.load()
    for y in range(N):
        for x in range(N):
            if x in (0, N - 1) or y in (0, N - 1):
                px[x, y] = _c(VERDIGRIS, 3 if y == 0 or x == 0 else 1)                 # der Rand des Pults
                continue
            level = 2.6 + (_h(x, y, 3141) - 0.5) * 1.2 - (0.8 if y == N - 2 else 0.0)
            if silent and _h(x // 2, y // 2, 3142) > 0.82:
                level -= 1.4                                                           # Asche
            px[x, y] = _c(base, level)
    for staff in STAVES:
        for line in range(5):
            y = staff + line if staff + line < N - 1 else None
            if y is None or line % 2 == 1:
                continue                                                               # nur drei Linien je Zeile (bei 16 Pixeln)
            for x in range(2, N - 2):
                if silent and _h(x, y, 3143) > 0.6:
                    continue                                                           # verblasst
                px[x, y] = ink + (255,)
                if glow:
                    gx[x, y] = _c(GLOW, 1)
    for i, (nx, pos) in enumerate(NOTES):
        staff = STAVES[0] if i < 6 else STAVES[1]
        ny = staff + pos
        if ny >= N - 1:
            continue
        if silent and i % 3 == 1:
            continue
        for (dx, dy) in ((0, 0), (1, 0), (1, -1), (1, -2)):
            x, y = nx + dx, ny + dy
            if 1 <= x < N - 1 and 1 <= y < N - 1:
                px[x, y] = ink + (255,)
                if glow:
                    gx[x, y] = _c(GLOW, 3 if dy == 0 else 2)
    return im, gl


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/organ_desk_front", front())
    put("block/organ_desk_side", panel(3111))
    put("block/organ_desk_top", planks(3112))
    put("block/organ_desk_keys", keys())
    put("block/organ_desk_rack", rack())
    sheet, glow = _sheet(PARCHMENT, INK, True, False)
    put("block/organ_desk_sheet", sheet)
    put("block/organ_desk_sheet_glow", glow)
    silent, _ = _sheet(ASH, (0x24, 0x22, 0x22), False, True)
    put("block/organ_desk_sheet_silent", silent)


if __name__ == "__main__":
    import sys
    target = sys.argv[1] if len(sys.argv) > 1 else "organ_desk_textures.png"
    tiles: list[tuple[str, Image.Image]] = []
    build(lambda name, img: tiles.append((name, img)))
    sheet = Image.new("RGBA", (len(tiles) * 136, 136), (40, 44, 52, 255))
    for i, (name, img) in enumerate(tiles):
        sheet.alpha_composite(img.resize((128, 128), Image.NEAREST), (i * 136 + 4, 4))
    sheet.save(target)
    print(target)
