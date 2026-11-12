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

def bow_icon(pull: int) -> Image.Image:
    """Der Bogen schräg (Spitzen unten links und oben rechts), der Rücken nach oben links gewölbt, rechts die leuchtende
    Sehne; gespannt wandert ihre Mitte nach unten rechts, und ein Pfeil liegt auf."""
    img = Image.new("RGBA", (N, N), CLEAR)
    t0 = (1.5, 13.5)
    t1 = (13.5, 1.5)
    bulge = 4.6 + pull * 0.5
    # die Arme: eine Bogenlinie durch die Spitzen, nach oben links gewölbt
    pts = []
    for i in range(41):
        s = i / 40.0
        x = t0[0] + (t1[0] - t0[0]) * s
        y = t0[1] + (t1[1] - t0[1]) * s
        k = math.sin(s * math.pi)
        x -= k * bulge * 0.707
        y -= k * bulge * 0.707
        pts.append((x, y, s))
    for x, y, s in pts:
        px, py = int(round(x)), int(round(y))
        mid = abs(s - 0.5) < 0.12
        col = CRYSTAL[3] if mid else IRON[4] if (s < 0.08 or s > 0.92) else IRON[3]
        img.putpixel((px, py), col)
        # die Dicke: ein Pixel nach unten rechts im Schatten
        qx, qy = px + 1, py + 1
        if 0 <= qx < N and 0 <= qy < N and img.getpixel((qx, qy))[3] == 0:
            img.putpixel((qx, qy), CRYSTAL[1] if mid else IRON[1])
    # Silberbeschläge an den Spitzen
    for (x, y) in (t0, t1):
        img.putpixel((int(round(x)), int(round(y))), IRON[5])
    # die Sehne: von Spitze zu Spitze, beim Spannen zur Mitte hin nach unten rechts gezogen
    draw = pull * 1.4
    nock = (7.5 + draw * 0.707, 7.5 + draw * 0.707)
    for seg in ((t0, nock), (nock, t1)):
        (ax, ay), (bx, by) = seg
        steps = 24
        for i in range(steps + 1):
            s = i / steps
            x = ax + (bx - ax) * s
            y = ay + (by - ay) * s
            px, py = int(round(x)), int(round(y))
            if img.getpixel((px, py))[3] == 0:
                img.putpixel((px, py), GLOW[1])
    if pull > 0:
        # der Pfeil: von der Sehne nach oben links, Kristallspitze
        length = 7.0 + pull * 0.5
        for i in range(int(length * 2)):
            s = i * 0.5
            px = int(round(nock[0] - s * 0.707))
            py = int(round(nock[1] - s * 0.707))
            if 0 <= px < N and 0 <= py < N:
                img.putpixel((px, py), GLOW[2] if s > length - 1.6 else IRON[4])
    return pa.outline(img, hexc("#0c0f1e"))


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
