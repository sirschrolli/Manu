#!/usr/bin/env python3
"""
Das Sternennetz und die Sternenweberin (0.99): Blöcke, Sternenbogen, Sternenpfeil.

  * Sternenseide – dicht gewebte Fäden aus Sternenlicht: ein Korbgeflecht aus hellen, blausilbernen Fadenbündeln (über
    und unter), darin glitzert es hier und da.
  * Sternengewebe – ein weitmaschiges, rautenförmiges Netz aus Sternenseide; die Maschen sind offen (man sieht ins Nichts),
    an den Knoten hängen Tautropfen aus Licht.
  * Sternennetz – Spinnweben aus Sternenlicht (über Kreuz gestellt wie Spinnweben): acht Speichen, drei Ringe, Tropfen.
  * Sternenkokon – längs umwickelte Sternenseide, von innen schwach erleuchtet; oben und unten die Enden.
  * Sternengelege – ein Bett aus wirrer Seide und Eier, in denen Sternenlicht glimmt (erloschen: grau).
  * Sternenbogen – zwei Arme aus Himmelseisen mit Silberbeschlägen, in der Mitte ein Kristall der Leere, die Sehne aus
    Sternenseide leuchtet; gespannt in drei Stufen mit einem Pfeil.
  * Sternenpfeil – Schaft aus Himmelseisen, Kristallspitze, Federn aus Sternenseide (Seitenansicht und Kreuz von hinten).

Wird von tools/generate_textures.py aufgerufen: build(put).
"""
from __future__ import annotations

import math
import random
from typing import Callable

from PIL import Image

import pixelart as pa
from pixelart import hexc

N = 16
CLEAR = (0, 0, 0, 0)

SILK = [hexc(c) for c in ("#1a2140", "#2b3866", "#465b94", "#6c84c0", "#9ab2e4", "#cadcf8", "#eef6ff")]
GLOW = [hexc(c) for c in ("#7fc6ff", "#b8e2ff", "#e8f6ff", "#ffffff")]
NAVY = [hexc(c) for c in ("#0a0d1c", "#11162c", "#1a2140", "#253058")]
IRON = [hexc(c) for c in ("#1c2238", "#2c3554", "#435078", "#64739e", "#8e9cc4", "#c2cde8")]
CRYSTAL = [hexc(c) for c in ("#2a1c64", "#4a33a0", "#6a52cc", "#9480ea", "#c4b6f8")]
COLD = [hexc(c) for c in ("#2a2e3c", "#3c4152", "#535a6e", "#6e7588", "#8a90a2")]


def _rng(seed: int) -> random.Random:
    return random.Random(seed)


def silk() -> Image.Image:
    """Ein Korbgeflecht: 4x4-Zellen abwechselnd mit waagerechten und senkrechten Fadenbündeln (nahtlos)."""
    img = Image.new("RGBA", (N, N))
    rnd = _rng(9901)
    for y in range(N):
        for x in range(N):
            cell = ((x // 4) + (y // 4)) % 2
            u, v = (x % 4, y % 4) if cell == 0 else (y % 4, x % 4)
            # u: Lage im Bündel quer (0 und 3 = Rand, dunkler), v: entlang des Fadens
            level = 4 if u in (1, 2) else 3
            if v in (0, 3) and u in (0, 3):
                level = 2
            if (u == 1 and v == 1) or (u == 2 and v == 2):
                level += 1                                   # Glanz auf dem Bündel
            if cell == 1:
                level -= 1                                   # die unteren Fäden liegen im Schatten
            img.putpixel((x, y), SILK[max(1, min(6, level))])
    for _ in range(5):
        x, y = rnd.randrange(N), rnd.randrange(N)
        img.putpixel((x, y), GLOW[2])
    return img


def weave() -> Image.Image:
    """Ein rautenförmiges Netz (Fäden auf (x+y) und (x-y) im Abstand 4), offene Maschen; an den Knoten Tropfen aus Licht."""
    img = Image.new("RGBA", (N, N), CLEAR)
    for y in range(N):
        for x in range(N):
            a = (x + y) % 4 == 0
            b = (x - y) % 4 == 0
            if a and b:
                img.putpixel((x, y), GLOW[1] if (x // 4 + y // 4) % 2 == 0 else SILK[6])
            elif a or b:
                # die Fäden: zur Mitte jeder Masche hin etwas dunkler (sie hängen durch)
                img.putpixel((x, y), SILK[5] if (x + 2 * y) % 3 else SILK[4])
    return img


def web() -> Image.Image:
    """Spinnweben aus Sternenlicht: acht Speichen von der Mitte, drei achteckige Ringe, ein paar Tropfen."""
    img = Image.new("RGBA", (N, N), CLEAR)
    cx = cy = 7.5
    for y in range(N):
        for x in range(N):
            dx, dy = x - cx, y - cy
            r = math.hypot(dx, dy)
            a = math.atan2(dy, dx)
            spoke = min(abs(math.sin(a - k * math.pi / 4.0)) for k in range(4)) * r < 0.5
            # achteckige Ringe: Abstand in der „Achteck-Norm“
            octo = max(abs(dx), abs(dy), (abs(dx) + abs(dy)) / 1.414)
            ring = any(abs(octo - rr) < 0.45 for rr in (2.5, 4.8, 7.1))
            if spoke and r < 7.8:
                img.putpixel((x, y), SILK[5] if r > 1.0 else GLOW[2])
            elif ring and r < 8.2:
                img.putpixel((x, y), SILK[6] if (x + y) % 2 else SILK[5])
    for x, y in ((5, 3), (11, 9), (3, 11), (9, 5), (12, 13)):
        if img.getpixel((x, y))[3] > 0:
            img.putpixel((x, y), GLOW[3])
    return img


def cocoon_side() -> Image.Image:
    """Der Kokon von der Seite: schräg gewickelte Bahnen, links im Licht; von innen schwach erleuchtet."""
    img = Image.new("RGBA", (N, N))
    for y in range(N):
        for x in range(N):
            band = (x * 2 + y * 3) % 7
            level = 4 if band < 4 else 3
            if band == 0:
                level = 5
            if x < 4:
                level += 1
            elif x > 11:
                level -= 1
            col = SILK[max(1, min(6, level))]
            # das Licht im Inneren schimmert durch die dünneren Stellen
            if band == 5 and 4 <= x <= 11 and 3 <= y <= 12:
                col = pa.mix(col, GLOW[0], 0.35)
            img.putpixel((x, y), col)
    return img


def cocoon_end() -> Image.Image:
    """Die Enden des Kokons: eine Spirale aus Seide."""
    img = Image.new("RGBA", (N, N))
    for y in range(N):
        for x in range(N):
            dx, dy = x - 7.5, y - 7.5
            r = math.hypot(dx, dy)
            a = math.atan2(dy, dx)
            s = (r - a * 0.9) % 2.4
            level = 5 if s < 0.8 else 4 if s < 1.6 else 3
            if r < 1.2:
                level = 6
            img.putpixel((x, y), SILK[level])
    return img


def nest_silk() -> Image.Image:
    """Das Bett des Geleges: wirre Fäden auf dunklem Grund (nahtlos)."""
    img = Image.new("RGBA", (N, N))
    rnd = _rng(4471)
    for y in range(N):
        for x in range(N):
            img.putpixel((x, y), NAVY[2 + ((x * 7 + y * 13) % 5 == 0)])
    for _ in range(22):
        x, y = rnd.uniform(0, N), rnd.uniform(0, N)
        a = rnd.uniform(0, math.pi)
        length = rnd.uniform(3, 7)
        col = SILK[rnd.choice((4, 5, 5, 6))]
        for i in range(int(length * 2)):
            px = int(x + math.cos(a) * i * 0.5) % N
            py = int(y + math.sin(a) * i * 0.5) % N
            img.putpixel((px, py), col)
    return img


def nest_egg(lit: bool) -> Image.Image:
    """Die Haut der Eier: perlmuttern, in der Mitte glimmt Sternenlicht (erloschen: grau und stumpf)."""
    img = Image.new("RGBA", (N, N))
    for y in range(N):
        for x in range(N):
            dx, dy = (x % 8) - 3.5, (y % 8) - 3.5
            r = math.hypot(dx, dy) / 4.9
            if lit:
                col = pa.mix(GLOW[3], GLOW[0], min(1.0, r * 1.2))
                if (x * 5 + y * 3) % 11 == 0:
                    col = pa.mix(col, SILK[4], 0.4)
            else:
                col = pa.mix(COLD[4], COLD[1], min(1.0, r * 1.1))
            img.putpixel((x, y), col)
    return img


# =============================================================================
# Der Sternenbogen (Symbole: ruhend und drei Stufen gespannt)
# =============================================================================

def _bow_limb() -> dict[tuple[int, int], tuple[float, float]]:
    """
    Die Arme des Bogens als Pixel -> (Lage t längs des Arms 0..1 von der oberen Spitze, Seite -1 innen .. +1 außen).
    Ein Kreisbogen von der Spitze oben rechts nach unten links, zur Ecke oben links gewölbt (wie beim Bogen von Minecraft –
    so passt die Haltung in der Hand), gut zwei Pixel stark, zu den Spitzen hin schlanker.
    """
    a, b, m = (13.6, 1.4), (1.4, 13.6), (5.0, 5.0)
    # Mittelpunkt auf der Diagonale x = y, gleich weit von Spitze und Scheitel
    k = ((a[0] ** 2 + a[1] ** 2) - 2 * m[0] ** 2) / (2 * (a[0] + a[1]) - 4 * m[0])
    radius_arc = math.hypot(a[0] - k, a[1] - k)
    ang_a = math.atan2(a[1] - k, a[0] - k)
    ang_b = math.atan2(b[1] - k, b[0] - k)
    # von A über den Scheitel (Richtung -135°) nach B
    if ang_b > ang_a:
        ang_b -= 2 * math.pi
    out: dict[tuple[int, int], tuple[float, float]] = {}
    for y in range(N):
        for x in range(N):
            cx, cy = x + 0.5, y + 0.5
            ang = math.atan2(cy - k, cx - k)
            while ang > ang_a:
                ang -= 2 * math.pi
            while ang < ang_b:
                ang += 2 * math.pi
            if not (ang_b - 0.02 <= ang <= ang_a + 0.02):
                continue
            t = (ang_a - ang) / (ang_a - ang_b)
            dist = math.hypot(cx - k, cy - k)
            side = dist - radius_arc  # außen (weiter vom Mittelpunkt) = zur Ecke oben links
            half = 1.0 if 0.12 < t < 0.88 else 0.75
            if abs(side) < half:
                out[(x, y)] = (min(1.0, max(0.0, t)), side / half)
    return out


def _line(x0: int, y0: int, x1: int, y1: int) -> list[tuple[int, int]]:
    """Bresenham: eine ein Pixel feine Linie."""
    pts = []
    dx, dy = abs(x1 - x0), -abs(y1 - y0)
    sx, sy = (1 if x0 < x1 else -1), (1 if y0 < y1 else -1)
    err = dx + dy
    while True:
        pts.append((x0, y0))
        if x0 == x1 and y0 == y1:
            return pts
        e2 = 2 * err
        if e2 >= dy:
            err += dy
            x0 += sx
        if e2 <= dx:
            err += dx
            y0 += sy


def bow_icon(pull: int) -> Image.Image:
    """
    Der Sternenbogen schräg wie der Bogen von Minecraft: die Arme aus Himmelseisen von oben rechts über die Ecke oben links
    nach unten links gewölbt (außen hell, innen im Schatten, die Spitzen mit Silber beschlagen), in der Mitte der Griff mit
    violetter Wicklung und einem Kristall der Leere. Die Sehne aus Sternenseide ist ein feiner, leuchtender Faden von
    Spitze zu Spitze; gespannt (Stufen 1–3) wandert ihre Mitte nach unten rechts, und der Pfeil – Kristallspitze vorn –
    wandert mit ihr zurück.
    """
    img = Image.new("RGBA", (N, N), CLEAR)
    limb = _bow_limb()
    for (x, y), (t, side) in limb.items():
        tip = t < 0.1 or t > 0.9
        grip = abs(t - 0.5) < 0.1
        if grip:
            col = CRYSTAL[3] if side > 0.35 else CRYSTAL[1] if side < -0.35 else CRYSTAL[2]
            if abs(t - 0.5) < 0.03 and abs(side) < 0.5:
                col = CRYSTAL[4]
        elif tip:
            col = IRON[5] if side > -0.2 else IRON[3]
        else:
            col = IRON[4] if side > 0.4 else IRON[1] if side < -0.4 else IRON[3]
            # feine Lichtpunkte im Eisen (Sternenstaub)
            if side > 0.4 and int(t * 40) % 7 == 3:
                col = IRON[5]
        img.putpixel((x, y), col)
    img = pa.outline(img, hexc("#0c0f1e"))

    # die Sehne: von Spitze zu Spitze, gespannt zur Mitte nach unten rechts gezogen
    top, bottom = (13, 2), (2, 13)
    nock = {0: None, 1: (9, 9), 2: (10, 10), 3: (11, 11)}[pull]
    if nock is None:
        string = _line(*top, *bottom)
    else:
        string = _line(*top, *nock) + _line(*nock, *bottom)
    for i, (x, y) in enumerate(string):
        img.putpixel((x, y), GLOW[1] if i % 3 else GLOW[0])
    for (x, y) in (top, bottom):
        img.putpixel((x, y), GLOW[0])
    if nock is not None:
        # der Pfeil liegt auf dem Griff: hinten am Faden die Federn, vorn die Kristallspitze
        length = 9
        nx, ny = nock
        hx, hy = nx - length, ny - length
        for k in range(1, length):
            x, y = nx - k, ny - k
            img.putpixel((x, y), CRYSTAL[4] if k % 2 else CRYSTAL[3])
        for (x, y) in ((nx - 2, ny - 1), (nx - 1, ny - 2)):
            img.putpixel((x, y), SILK[5])
        img.putpixel((nx, ny), GLOW[2])
        for (x, y, col) in ((hx, hy, GLOW[3]), (hx + 1, hy, GLOW[1]), (hx, hy + 1, GLOW[1])):
            if 0 <= x < N and 0 <= y < N:
                img.putpixel((x, y), col)
    return img


# =============================================================================
# Der Sternenpfeil (Textur wie die Pfeile von Minecraft: Seite oben, Kreuz von hinten darunter)
# =============================================================================

def star_arrow() -> Image.Image:
    img = Image.new("RGBA", (32, 32), CLEAR)
    side = Image.new("RGBA", (16, 4), CLEAR)
    for x in range(16):
        if 3 <= x <= 12:
            side.putpixel((x, 1), IRON[4])
            side.putpixel((x, 2), IRON[2])
    # die Spitze aus Kristall (rechts)
    for x, y, c in ((13, 1, GLOW[2]), (13, 2, CRYSTAL[3]), (14, 1, GLOW[3]), (14, 2, GLOW[1]), (15, 1, GLOW[2]), (12, 0, CRYSTAL[3]),
                    (12, 3, CRYSTAL[2]), (13, 0, GLOW[1]), (13, 3, CRYSTAL[3])):
        side.putpixel((x, y), c)
    # die Federn aus Sternenseide (links)
    for x in range(0, 5):
        for y in (0, 3):
            side.putpixel((x, y), SILK[6] if (x + y) % 2 == 0 else SILK[5])
        if x < 3:
            side.putpixel((x, 1), SILK[5])
            side.putpixel((x, 2), SILK[4])
    img.alpha_composite(side, (0, 0))
    img.alpha_composite(side, (16, 0))
    back = Image.new("RGBA", (5, 5), CLEAR)
    for i in range(5):
        back.putpixel((i, i), SILK[6])
        back.putpixel((4 - i, i), SILK[5])
    back.putpixel((2, 2), GLOW[2])
    img.alpha_composite(back, (0, 5))
    img.alpha_composite(back, (5, 5))
    return img


def build(put: Callable[[str, Image.Image], None]) -> None:
    put("block/star_silk", silk())
    put("block/star_weave", weave())
    w = web()
    put("block/star_web", w)
    # die Leuchtebene des Sternennetzes: die hellen Fäden und die Tropfen
    glow = Image.new("RGBA", (N, N), CLEAR)
    for y in range(N):
        for x in range(N):
            c = w.getpixel((x, y))
            if c[3] > 0 and (c in GLOW or c == SILK[6]):
                glow.putpixel((x, y), c)
    put("block/star_web_emissive", glow)
    put("block/star_cocoon_side", cocoon_side())
    put("block/star_cocoon_end", cocoon_end())
    put("block/weaver_nest_silk", nest_silk())
    put("block/weaver_nest_egg", nest_egg(True))
    put("block/weaver_nest_egg_cold", nest_egg(False))
    put("item/star_bow", bow_icon(0))
    for i in range(3):
        put(f"item/star_bow_pulling_{i}", bow_icon(i + 1))
    put("entity/projectiles/star_arrow", star_arrow())


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda k, i: out.__setitem__(k, i))
    keys = [k for k in out if not k.startswith("entity")]
    z = 8
    tile = N * 2 * z
    sheet = Image.new("RGBA", (len(keys) * (tile + 8) + 32 * 6 + 8, tile), (40, 44, 60, 255))
    for i, k in enumerate(keys):
        img = out[k]
        t = Image.new("RGBA", (N * 2, N * 2), (0, 0, 0, 0))
        if k.startswith("block"):
            for dx in (0, N):
                for dy in (0, N):
                    t.alpha_composite(img, (dx, dy))
        else:
            t.alpha_composite(img.resize((N * 2, N * 2), Image.NEAREST))
        sheet.alpha_composite(t.resize((tile, tile), Image.NEAREST), (i * (tile + 8), 0))
    arrow = out["entity/projectiles/star_arrow"].resize((32 * 6, 32 * 6), Image.NEAREST)
    sheet.alpha_composite(arrow, (len(keys) * (tile + 8), 0))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "weaver.png")
