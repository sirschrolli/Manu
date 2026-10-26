#!/usr/bin/env python3
"""
Blocktexturen im Stil von Vanilla-Minecraft.

Was Mojangs Texturen ausmacht (und was diese Werkzeuge nachbilden):
  * Sehr wenige Töne je Material (Stein 4, Ziegel und Holz 6-7) mit enger Helligkeitsspanne (etwa ±15 %).
  * Muster aus kurzen, meist waagerechten Strichen und kleinen Flecken – kein Pixelrauschen, keine einzelnen
    hellen Punkte (allein stehende Pixel werden von ihren Nachbarn aufgenommen).
  * Licht von oben links: Ziegel und Bohlen haben oben eine helle Kante, unten eine dunkle Fuge.
  * Leichtes Farbton-Wandern: dunkle Töne kühler und etwas satter, helle Töne wärmer und blasser.
  * Alles ist nahtlos kachelbar (jedes Zufallsmuster rechnet über den Rand hinweg weiter).

Alle Muster entstehen zunächst als Raster von Tonstufen (0 = dunkelste) und werden erst am Ende mit einer
Palette eingefärbt – so lässt sich dieselbe Form in vielen Materialien verwenden.
"""
from __future__ import annotations

import colorsys
import math
import random
from typing import Callable, Sequence

from PIL import Image

from pixelart import Color, hexc

N = 16
Grid = list[list[int]]
Field = list[list[float]]


# =============================================================================
# Farben
# =============================================================================

def tones(base: str | Color, n: int = 4, spread: float = 0.16, cool: float = 235.0, warm: float = 45.0,
          hue: float = 8.0, sat: float = 0.05, curve: float = 1.15) -> list[Color]:
    """
    Enge Tonleiter um die Grundfarbe (dunkel -> hell): Helligkeit von (1 - spread) bis (1 + spread) mal der Grundhelligkeit.
    Dunkle Stufen wandern bis zu {@code hue} Grad Richtung {@code cool} und werden etwas satter, helle Richtung {@code warm}
    und etwas blasser. {@code curve} > 1 macht die hellen Stufen etwas weiter auseinander (wie bei Vanilla-Stein).
    """
    c = hexc(base) if isinstance(base, str) else base
    h, l, s = colorsys.rgb_to_hls(c[0] / 255, c[1] / 255, c[2] / 255)
    out: list[Color] = []
    for i in range(n):
        t = (i / (n - 1)) * 2 - 1 if n > 1 else 0.0
        t = math.copysign(abs(t) ** (1 / curve if t > 0 else curve), t)
        nl = min(0.97, max(0.02, l * (1 + spread * t)))
        target = cool if t < 0 else warm
        d = ((target / 360.0 - h + 0.5) % 1.0) - 0.5
        nh = (h + math.copysign(min(abs(d), abs(t) * hue / 360.0), d)) % 1.0
        ns = min(1.0, max(0.0, s + (-t) * sat))
        r, g, b = colorsys.hls_to_rgb(nh, nl, ns)
        out.append((round(r * 255), round(g * 255), round(b * 255), 255))
    return out


def paint(grid: Grid, pal: Sequence[Color], alpha: Grid | None = None) -> Image.Image:
    """Färbt ein Stufenraster mit der Palette ein (Stufe < 0 = durchsichtig)."""
    h, w = len(grid), len(grid[0])
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    for y in range(h):
        for x in range(w):
            v = grid[y][x]
            if v >= 0 and (alpha is None or alpha[y][x]):
                img.putpixel((x, y), pal[max(0, min(len(pal) - 1, v))])
    return img


# =============================================================================
# Raster und Rauschen
# =============================================================================

def grid(fill: int = 0, w: int = N, h: int = N) -> Grid:
    return [[fill] * w for _ in range(h)]


def copy(g: Grid) -> Grid:
    return [row[:] for row in g]


def _smooth(t: float) -> float:
    return t * t * (3 - 2 * t)


def noise(seed: int, cx: int, cy: int, w: int = N, h: int = N) -> Field:
    """Kachelbares Wertrauschen mit Zellen von cx × cy Pixeln (verschieden breit und hoch = gestreckte Flecken)."""
    rnd = random.Random(seed)
    gw, gh = max(1, w // cx), max(1, h // cy)
    pts = [[rnd.random() for _ in range(gw)] for _ in range(gh)]
    out = [[0.0] * w for _ in range(h)]
    for y in range(h):
        for x in range(w):
            fx, fy = x / cx, y / cy
            x0, y0 = int(fx) % gw, int(fy) % gh
            x1, y1 = (x0 + 1) % gw, (y0 + 1) % gh
            tx, ty = _smooth(fx - int(fx)), _smooth(fy - int(fy))
            a = pts[y0][x0] + (pts[y0][x1] - pts[y0][x0]) * tx
            b = pts[y1][x0] + (pts[y1][x1] - pts[y1][x0]) * tx
            out[y][x] = a + (b - a) * ty
    return out


def layered(seed: int, octaves: Sequence[tuple[int, int, float]], w: int = N, h: int = N) -> Field:
    """Mehrere Rauschlagen (cx, cy, Gewicht) übereinander."""
    out = [[0.0] * w for _ in range(h)]
    for i, (cx, cy, weight) in enumerate(octaves):
        f = noise(seed * 31 + i * 7919, cx, cy, w, h)
        for y in range(h):
            for x in range(w):
                out[y][x] += f[y][x] * weight
    return out


def white(seed: int, w: int = N, h: int = N) -> Field:
    rnd = random.Random(seed)
    return [[rnd.random() for _ in range(w)] for _ in range(h)]


def strokes(seed: int, horizontal: tuple[float, ...] = (1, 2, 2, 1), vertical: tuple[float, ...] = (1,), w: int = N,
            h: int = N) -> Field:
    """
    Feines Strichmuster: weißes Rauschen, waagerecht (bzw. senkrecht) weichgezeichnet – benachbarte Pixel in
    Strichrichtung ähneln sich, so entstehen kurze Striche von 2-4 Pixeln wie bei Vanilla-Stein.
    """
    base = white(seed, w, h)
    out = [[0.0] * w for _ in range(h)]
    kh = len(horizontal) // 2
    kv = len(vertical) // 2
    total = sum(horizontal) * sum(vertical)
    for y in range(h):
        for x in range(w):
            acc = 0.0
            for j, wv in enumerate(vertical):
                for i, wh in enumerate(horizontal):
                    acc += base[(y + j - kv) % h][(x + i - kh) % w] * wh * wv
            out[y][x] = acc / total
    return out


def mix_fields(*parts: tuple[Field, float]) -> Field:
    h, w = len(parts[0][0]), len(parts[0][0][0])
    out = [[0.0] * w for _ in range(h)]
    for f, weight in parts:
        for y in range(h):
            for x in range(w):
                out[y][x] += f[y][x] * weight
    return out


def quantize(field: Field, shares: Sequence[float]) -> Grid:
    """Teilt die Werte nach Anteilen in Stufen (shares: Anteil je Stufe von dunkel nach hell)."""
    h, w = len(field), len(field[0])
    values = sorted(v for row in field for v in row)
    total = sum(shares)
    cuts = []
    acc = 0.0
    for share in shares[:-1]:
        acc += share / total
        cuts.append(values[min(len(values) - 1, int(acc * len(values)))])
    out = grid(0, w, h)
    for y in range(h):
        for x in range(w):
            v = field[y][x]
            level = 0
            while level < len(cuts) and v >= cuts[level]:
                level += 1
            out[y][x] = level
    return out


def despeckle(g: Grid, keep: Callable[[int, int], bool] | None = None, passes: int = 2) -> Grid:
    """Einzelne Pixel, die keinem Nachbarn (oben/unten/links/rechts) gleichen, übernehmen den häufigsten Nachbarton."""
    h, w = len(g), len(g[0])
    for _ in range(passes):
        out = copy(g)
        changed = False
        for y in range(h):
            for x in range(w):
                if keep is not None and keep(x, y):
                    continue
                v = g[y][x]
                nb = [g[(y - 1) % h][x], g[(y + 1) % h][x], g[y][(x - 1) % w], g[y][(x + 1) % w]]
                if v in nb:
                    continue
                counts: dict[int, int] = {}
                for n in nb:
                    counts[n] = counts.get(n, 0) + 1
                best = max(counts.items(), key=lambda kv: (kv[1], -abs(kv[0] - v)))[0]
                out[y][x] = best
                changed = True
        g = out
        if not changed:
            break
    return g


def streaks(g: Grid, rnd: random.Random, chance: float = 0.6, vertical: bool = False) -> Grid:
    """Pixel, die waagerecht (bzw. senkrecht) allein stehen, schließen sich oft einem Nachbarn an – das gibt Striche."""
    h, w = len(g), len(g[0])
    out = copy(g)
    for y in range(h):
        for x in range(w):
            if vertical:
                a, b = g[(y - 1) % h][x], g[(y + 1) % h][x]
            else:
                a, b = g[y][(x - 1) % w], g[y][(x + 1) % w]
            v = g[y][x]
            if v != a and v != b and rnd.random() < chance:
                out[y][x] = a if rnd.random() < 0.5 else b
    return out


# =============================================================================
# Stein
# =============================================================================

def stone(seed: int, shares: Sequence[float] = (0.13, 0.32, 0.37, 0.18), grain: float = 0.7) -> Grid:
    """
    Fels wie Vanilla-Stein: vier Stufen aus kurzen waagerechten Strichen (2-4 Pixel) und kleinen Flecken, darunter
    eine leichte großflächige Schwankung. Allein stehende Pixel werden entfernt.
    """
    rnd = random.Random(seed)
    f = mix_fields((strokes(seed), grain), (layered(seed + 1, ((8, 4, 0.6), (4, 4, 0.4))), 1.0 - grain))
    g = quantize(f, shares)
    g = streaks(g, rnd, 0.5)
    return despeckle(g)


def layered_stone(seed: int, shares: Sequence[float] = (0.14, 0.3, 0.36, 0.2)) -> Grid:
    """Geschichteter Fels (wie Tiefenschiefer): lange waagerechte Striche in Bändern, dazu ein paar dunkle Fugen."""
    rnd = random.Random(seed)
    f = mix_fields((strokes(seed, (1, 2, 3, 3, 2, 1)), 0.6), (layered(seed + 1, ((16, 4, 0.6), (8, 2, 0.4))), 0.4))
    g = quantize(f, shares)
    g = streaks(g, rnd, 0.8)
    for _ in range(3):
        y = rnd.randrange(N)
        x = rnd.randrange(N)
        for i in range(rnd.randint(3, 6)):
            g[y][(x + i) % N] = 0
    return despeckle(g)


def cobble(seed: int, stones: int = 9) -> Grid:
    """
    Pflaster aus runden Steinen (wie Bruchstein): jeder Stein hell oben links und dunkel unten rechts, dazwischen
    dunkle Fugen. Stufen 0..5.
    """
    rnd = random.Random(seed)
    pts = [(rnd.uniform(0, N), rnd.uniform(0, N), rnd.random()) for _ in range(stones)]
    g = grid(0)
    for y in range(N):
        for x in range(N):
            best, second, owner = 1e9, 1e9, None
            for p in pts:
                dx = min(abs(x + 0.5 - p[0]), N - abs(x + 0.5 - p[0]))
                dy = min(abs(y + 0.5 - p[1]), N - abs(y + 0.5 - p[1]))
                d = math.hypot(dx * 0.9, dy * 1.1)
                if d < best:
                    best, second, owner = d, best, p
                elif d < second:
                    second = d
            edge = second - best
            if edge < 0.7:
                g[y][x] = 0
                continue
            # Lage im Stein: oben links hell, unten rechts dunkel
            ox = ((x + 0.5 - owner[0] + N / 2) % N) - N / 2
            oy = ((y + 0.5 - owner[1] + N / 2) % N) - N / 2
            light = -(ox + oy) * 0.22 + owner[2] * 0.8
            level = 3 + (1 if light > 0.5 else 0) + (1 if light > 1.3 else 0) - (1 if light < -0.4 else 0)
            if edge < 1.6:
                level = min(level, 2)
            g[y][x] = max(1, min(5, level))
    return despeckle(g)


# =============================================================================
# Ziegel, polierte Platten, gemeißelte Steine
# =============================================================================

def bricks(seed: int, rows: Sequence[tuple[int, Sequence[int]]], levels: int = 7, body: Sequence[float] = (0.25, 0.45, 0.30),
           chips: int = 3) -> Grid:
    """
    Ziegelmauer. rows: (Höhe einer Ziegelreihe inkl. Fuge, Fugen-x-Positionen). Jede Ziegelreihe hat oben eine helle
    Kante, unten eine dunkle Schattenzeile und darunter die Fuge; links neben einer Fuge ist der Ziegel heller, rechts
    am Ende dunkler. Stufen: 0 Fuge, 1 Fuge hell, 2 Schatten, 3-5 Fläche, 6 Lichtkante.
    """
    rnd = random.Random(seed)
    face = mix_fields((strokes(seed + 5), 0.65), (layered(seed + 6, ((8, 4, 0.6), (4, 2, 0.4))), 0.35))
    fq = despeckle(quantize(face, body))
    g = grid(4)
    y0 = 0
    for height, joints in rows:
        js = sorted(j % N for j in joints)
        for yy in range(height):
            y = y0 + yy
            for x in range(N):
                at_joint = x in js
                left_edge = ((x - 1) % N) in js
                right_edge = ((x + 1) % N) in js
                if yy == height - 1:
                    g[y][x] = 0 if rnd.random() < 0.75 else 1          # waagerechte Fuge
                elif at_joint:
                    g[y][x] = 0 if yy > 0 else 1                       # senkrechte Fuge
                elif yy == height - 2:
                    g[y][x] = 2                                        # Schatten unter dem Ziegel
                elif yy == 0:
                    g[y][x] = 6 if not right_edge else 5               # Lichtkante oben
                elif left_edge:
                    g[y][x] = 6 if yy < height - 3 else 5               # Lichtkante links
                elif right_edge:
                    g[y][x] = 2 if yy > 1 else 3                        # Schatten rechts
                else:
                    g[y][x] = 3 + fq[y][x]
        y0 += height
    # ein paar abgeschlagene Ecken / dunkle Kerben in den Flächen
    for _ in range(chips):
        x, y = rnd.randrange(N), rnd.randrange(N)
        if 3 <= g[y][x] <= 5:
            g[y][x] = 2
            if rnd.random() < 0.6 and 3 <= g[y][(x + 1) % N] <= 5:
                g[y][(x + 1) % N] = 2
    return g


def masonry(layout: Sequence[str], seed: int, vary: float = 0.35, body: Sequence[float] = (0.25, 0.45, 0.30),
            rounded: bool = False) -> Grid:
    """
    Mauerwerk nach einem Zeichenraster (gleiches Zeichen = derselbe Stein, auch über den Rand hinweg; '.' = Fuge).
    Schattierung wie bei Vanilla-Steinziegeln: Lichtkante oben (6) und links (5), Schatten unten (2) und rechts (3),
    Fugen 0-1; die Fläche jedes Steins aus kurzen Strichen (3-5), einzelne Steine eine Stufe heller oder dunkler.
    {@code rounded}: Steinecken ausgebrochen (für verwitterte Mauern).
    """
    rows = [r.strip() for r in layout if r.strip()]
    h, w = len(rows), len(rows[0])
    rnd = random.Random(seed)
    face = mix_fields((strokes(seed + 5), 0.65), (layered(seed + 6, ((8, 4, 0.6), (4, 2, 0.4))), 0.35))
    fq = despeckle(quantize(face, body))

    def sid(x: int, y: int) -> str:
        return rows[y % h][x % w]

    stones = sorted({c for r in rows for c in r if c != "."})
    shift = {s: (rnd.choice((-1, 1)) if rnd.random() < vary else 0) for s in stones}
    g = grid(0, w, h)
    for y in range(h):
        for x in range(w):
            s = sid(x, y)
            if s == ".":
                g[y][x] = 0 if rnd.random() < 0.75 else 1
                continue
            up, down = sid(x, y - 1) != s, sid(x, y + 1) != s
            left, right = sid(x - 1, y) != s, sid(x + 1, y) != s
            if rounded and ((up or down) and (left or right)):
                g[y][x] = 1
            elif down:
                g[y][x] = 2
            elif up:
                g[y][x] = 5 if right else 6
            elif left:
                g[y][x] = 5
            elif right:
                g[y][x] = 3
            else:
                g[y][x] = max(3, min(5, 3 + fq[y][x] + shift[s]))
    return g


def crack(g: Grid, seed: int, count: int = 3, length: tuple[int, int] = (4, 9), level: int = 0,
          only: Callable[[int], bool] = lambda v: v >= 2, edge: int | None = None) -> Grid:
    """
    Risse: dünne, zackige dunkle Linien, die über Ziegelflächen laufen; mit {@code edge} bekommt jeder Riss rechts
    daneben eine helle Bruchkante.
    """
    rnd = random.Random(seed)
    g = copy(g)
    for _ in range(count):
        x, y = rnd.randrange(N), rnd.randrange(N)
        dx = rnd.choice((-1, 1))
        for _ in range(rnd.randint(*length)):
            if only(g[y % N][x % N]):
                g[y % N][x % N] = level
                if edge is not None and only(g[y % N][(x + 1) % N]) and g[y % N][(x + 1) % N] != level:
                    g[y % N][(x + 1) % N] = edge
            if rnd.random() < 0.55:
                x += dx
            else:
                y += 1
            if rnd.random() < 0.15:
                dx = -dx
    return g


def polished(seed: int, shares: Sequence[float] = (0.2, 0.45, 0.35)) -> Grid:
    """
    Polierte Platte: eine Fläche mit ruhigem, leicht gestricheltem Muster (Stufen 2-4), oben und links eine helle
    Kante (5), unten und rechts eine dunkle (1), die Ecke unten rechts am dunkelsten (0).
    """
    rnd = random.Random(seed)
    f = mix_fields((strokes(seed, (1, 2, 3, 2, 1)), 0.7), (layered(seed + 1, ((8, 4, 0.6), (4, 2, 0.4))), 0.3))
    q = quantize(f, shares)
    q = streaks(q, rnd, 0.7)
    q = despeckle(q)
    g = grid(0)
    for y in range(N):
        for x in range(N):
            if y == 0 or x == 0:
                g[y][x] = 5 if not (x == N - 1 or y == N - 1) else 3
            elif y == N - 1 or x == N - 1:
                g[y][x] = 1
            else:
                g[y][x] = 2 + q[y][x]
    g[N - 1][N - 1] = 0
    return g


def bevel_frame(g: Grid, x0: int, y0: int, x1: int, y1: int, light: int, dark: int, depth: int = 1) -> None:
    """Rahmen (eingelassen): oben/links dunkel, unten/rechts hell – wie eine gemeißelte Vertiefung."""
    for d in range(depth):
        for x in range(x0 + d, x1 - d + 1):
            g[y0 + d][x] = dark
            g[y1 - d][x] = light
        for y in range(y0 + d, y1 - d + 1):
            g[y][x0 + d] = dark
            g[y][x1 - d] = light


# =============================================================================
# Holz
# =============================================================================

def planks(seed: int, boards: int = 4, joints: Sequence[int | None] = (None, 9, None, 4)) -> Grid:
    """
    Bohlen wie Vanilla: vier Bretter zu je vier Pixeln. Oberste Zeile eines Bretts hell (5-6), darunter Maserung aus
    waagerechten Strichen (3-5), unterste Zeile die dunkle Fuge (0-1). Einige Bretter enden in einem Stoß (senkrechte
    dunkle Linie). Stufen 0..6.
    """
    rnd = random.Random(seed)
    h = N // boards
    grain = mix_fields((strokes(seed + 3, (1, 2, 3, 3, 2, 1)), 0.65), (layered(seed + 4, ((16, 1, 0.5), (8, 1, 0.5))), 0.35))
    gq = quantize(grain, (0.24, 0.46, 0.30))
    g = grid(4)
    for b in range(boards):
        top = b * h
        joint = joints[b % len(joints)] if joints else None
        for yy in range(h):
            y = top + yy
            for x in range(N):
                if yy == h - 1:
                    g[y][x] = 0 if rnd.random() < 0.7 else 1
                elif yy == 0:
                    g[y][x] = 5 + (1 if gq[y][x] == 2 else 0)
                else:
                    g[y][x] = 3 + gq[y][x]
                if joint is not None and x == joint and yy < h - 1:
                    g[y][x] = 1 if yy > 0 else 2
                if joint is not None and x == (joint + 1) % N and yy < h - 1:
                    g[y][x] = max(g[y][x], 5)
    return despeckle(g, keep=lambda x, y: (y % h) in (0, h - 1))


def bark(seed: int, grooves: int = 5) -> Grid:
    """
    Rinde wie Vanilla-Stämme: senkrechte Striche (Stufen 1-4) mit einigen durchgehenden dunklen Furchen (0) und
    hellen Graten (5). Stufen 0..5.
    """
    rnd = random.Random(seed)
    f = layered(seed, ((2, 8, 0.45), (1, 4, 0.35), (1, 2, 0.2)))
    g = quantize(f, (0.2, 0.3, 0.3, 0.2))
    g = [[v + 1 for v in row] for row in g]
    g = streaks(g, rnd, 0.8, vertical=True)
    for _ in range(grooves):
        x = rnd.randrange(N)
        y = rnd.randrange(N)
        for i in range(rnd.randint(5, 12)):
            g[(y + i) % N][x] = 0
            if rnd.random() < 0.2:
                x = (x + rnd.choice((-1, 1))) % N
        # Grat neben der Furche
        gx = (x + 1) % N
        for i in range(rnd.randint(2, 5)):
            yy = (y + rnd.randrange(8)) % N
            if g[yy][gx] >= 3:
                g[yy][gx] = 5
    return despeckle(g)


def log_top(seed: int, bark_width: int = 1) -> Grid:
    """
    Stirnseite eines Stamms: Rinde am Rand (Stufen 0-1, hier und da zwei Pixel tief), darin Jahresringe als leicht
    unregelmäßige, abgerundete Quadrate (abwechselnd 3 und 4, ab und zu 5), in der Mitte ein dunkler Kern (2).
    Stufen 0..5.
    """
    rnd = random.Random(seed)
    wobble = noise(seed + 1, 4, 4)
    g = grid(4)
    c = (N - 1) / 2
    for y in range(N):
        for x in range(N):
            edge = min(x, y, N - 1 - x, N - 1 - y)
            if edge < bark_width or (edge == bark_width and rnd.random() < 0.18):
                g[y][x] = 0 if rnd.random() < 0.55 else 1
                continue
            dx, dy = abs(x - c), abs(y - c)
            r = max(dx, dy) * 0.82 + math.hypot(dx, dy) * 0.18 + (wobble[y][x] - 0.5) * 1.1
            if r < 1.3:
                g[y][x] = 2
                continue
            ring = int(r * 0.95)
            g[y][x] = 3 if ring % 2 == 0 else (5 if rnd.random() < 0.25 else 4)
    return despeckle(g, keep=lambda x, y: min(x, y, N - 1 - x, N - 1 - y) == 0)


def stripped(seed: int) -> Grid:
    """Geschälter Stamm: feine senkrechte Fasern, ruhiger als Rinde. Stufen 0..4."""
    rnd = random.Random(seed)
    f = layered(seed, ((2, 16, 0.4), (1, 8, 0.4), (1, 4, 0.2)))
    g = quantize(f, (0.12, 0.3, 0.38, 0.2))
    g = streaks(g, rnd, 0.9, vertical=True)
    g = [[v + (1 if v > 0 else 0) for v in row] for row in g]
    return despeckle(g)


# =============================================================================
# Böden
# =============================================================================

def soil(seed: int, pebbles: int = 6) -> Grid:
    """
    Erde wie Vanilla: körnig aus Klümpchen von 1-2 Pixeln (Stufen 0-4), dazu ein paar Steinchen (Stufe -2 als Marke,
    beim Einfärben mit einer zweiten Palette). Gibt Stufen 0..4 und für Steinchen 5..6 zurück.
    """
    rnd = random.Random(seed)
    f = layered(seed, ((4, 4, 0.25), (2, 2, 0.35), (1, 1, 0.4)))
    g = quantize(f, (0.12, 0.25, 0.33, 0.2, 0.1))
    # helle Spitzen paarweise, damit keine einzelnen Lichtpunkte entstehen
    for y in range(N):
        for x in range(N):
            if g[y][x] == 4 and g[y][(x + 1) % N] < 3 and g[(y + 1) % N][x] < 3:
                g[y][x] = 3
    for _ in range(pebbles):
        x, y = rnd.randrange(N), rnd.randrange(N)
        g[y][x] = 6
        if rnd.random() < 0.6:
            g[y][(x + 1) % N] = 5
    return g


def sand(seed: int) -> Grid:
    """Sand: feines, kontrastarmes Korn (Stufen 0-3)."""
    f = layered(seed, ((4, 4, 0.3), (2, 2, 0.3), (1, 1, 0.4)))
    return quantize(f, (0.15, 0.4, 0.33, 0.12))


def grass_top(seed: int) -> Grid:
    """Grasdecke (wird vom Biom eingefärbt): kleine Halmbüschel, Stufen 0..4."""
    rnd = random.Random(seed)
    f = layered(seed, ((4, 4, 0.3), (2, 2, 0.3), (1, 1, 0.4)))
    g = quantize(f, (0.1, 0.25, 0.35, 0.2, 0.1))
    return streaks(g, rnd, 0.3, vertical=True)


def leaves(seed: int, holes: float = 0.3) -> Grid:
    """
    Laub wie Vanilla: Büschel aus Blättern mit hellen Spitzen oben links und dunkler Unterseite, dazwischen
    unregelmäßige Lücken (-1 = durchsichtig, etwa {@code holes} der Fläche). Stufen 0..3.
    """
    f = mix_fields((white(seed), 0.5), (layered(seed + 1, ((2, 2, 0.6), (4, 4, 0.4))), 0.5))
    q = quantize(f, (holes, 0.2, 0.25, 0.25 - holes / 3, 0.3 - holes * 2 / 3))
    g = grid(-1)
    for y in range(N):
        for x in range(N):
            g[y][x] = q[y][x] - 1
    out = copy(g)
    for y in range(N):
        for x in range(N):
            v = g[y][x]
            if v < 0:
                continue
            if g[(y - 1) % N][x] < 0 or g[y][(x - 1) % N] < 0:
                v = min(3, v + 1)
            elif g[(y + 1) % N][x] < 0:
                v = max(0, v - 1)
            out[y][x] = v
    # einzelne Löcher schließen, einzelne Blätter entfernen
    for y in range(N):
        for x in range(N):
            nb = [out[(y - 1) % N][x], out[(y + 1) % N][x], out[y][(x - 1) % N], out[y][(x + 1) % N]]
            if out[y][x] < 0 and all(n >= 0 for n in nb):
                out[y][x] = 0
            elif out[y][x] >= 0 and all(n < 0 for n in nb):
                out[y][x] = -1
    return out


# =============================================================================
# Erz, Metall, Kristall
# =============================================================================

GEM_SHAPES = (
    (".h.", "hmd", ".d."),
    ("h.", "md", ".d"),
    (".h.", "hmm", "mmd", ".d."),
    (".hh.", "hmmd", ".dd."),
    ("hh", "md"),
    ("h..", "mm.", ".md"),
)


def gem_blobs(seed: int, count: int = 5) -> Grid:
    """
    Edelsteine im Gestein (wie Vanilla-Smaragderz): kleine Kristalle verschiedener Form an unregelmäßigen Stellen.
    Stufen wie bei ore_blobs: 0 Schattenkante, 1 dunkel, 2 Mitte, 3 hell, 4 Funkeln.
    """
    rnd = random.Random(seed)
    g = grid(-1)
    placed: list[tuple[int, int]] = []
    tries = 0
    while len(placed) < count and tries < 400:
        tries += 1
        cx, cy = rnd.randrange(N), rnd.randrange(N)
        if any(min(abs(cx - px), N - abs(cx - px)) + min(abs(cy - py), N - abs(cy - py)) < 6 for px, py in placed):
            continue
        shape = rnd.choice(GEM_SHAPES)
        if rnd.random() < 0.5:
            shape = tuple(row[::-1].replace("h", "H").replace("d", "h").replace("H", "d") if False else row[::-1] for row in shape)
        cells = {}
        for dy, row in enumerate(shape):
            for dx, ch in enumerate(row):
                if ch != ".":
                    cells[((cx + dx) % N, (cy + dy) % N)] = {"h": 3, "m": 2, "d": 1}[ch]
        if any(g[y][x] != -1 for x, y in cells):
            continue
        placed.append((cx, cy))
        for (x, y), v in cells.items():
            g[y][x] = v
        top = min((c for c, v in cells.items() if v == 3), key=lambda c: (c[1], c[0]), default=None)
        if top is not None:
            g[top[1]][top[0]] = 4
        for (x, y) in cells:
            for sx, sy in ((x + 1, y + 1), (x + 1, y), (x, y + 1)):
                sx, sy = sx % N, sy % N
                if g[sy][sx] == -1 and (sx, sy) not in cells and rnd.random() < 0.6:
                    g[sy][sx] = 0
    return g


ORE_SHAPES = (
    ((0, 0), (1, 0), (0, 1), (1, 1), (2, 1)),
    ((1, 0), (0, 1), (1, 1), (2, 1), (1, 2)),
    ((0, 0), (1, 0), (2, 0), (0, 1), (1, 1), (1, 2)),
    ((1, 0), (2, 0), (0, 1), (1, 1), (2, 1), (1, 2), (2, 2)),
    ((0, 0), (1, 0), (0, 1), (1, 1), (2, 1), (2, 2), (3, 2)),
    ((1, 0), (2, 0), (0, 1), (1, 1), (2, 1), (3, 1), (1, 2), (2, 2)),
    ((0, 0), (1, 0), (1, 1), (2, 1), (1, 2)),
    ((1, 0), (2, 0), (0, 1), (1, 1), (2, 1), (3, 1), (0, 2), (1, 2), (2, 2)),
    ((0, 0), (1, 0), (2, 0), (0, 1), (1, 1), (2, 1), (3, 1), (1, 2), (2, 2), (3, 2)),
)


def ore_blobs(seed: int, count: int = 5, size: tuple[int, int] = (5, 8), margin: int = 0) -> Grid:
    """
    Erzflecken als Maske: -1 = kein Erz, 0 = Schattenkante (dunkler Stein unten rechts am Fleck), 1 = Erz dunkel,
    2 = Erz, 3 = Erz hell, 4 = Glanzpunkt. Die Flecken sind kompakte Klümpchen (5-8 Pixel) mit Licht von oben links.
    """
    rnd = random.Random(seed)
    g = grid(-1)
    placed: list[tuple[int, int]] = []
    shapes = [s for s in ORE_SHAPES if size[0] <= len(s) <= size[1]] or list(ORE_SHAPES)
    tries = 0
    while len(placed) < count and tries < 400:
        tries += 1
        cx, cy = rnd.randrange(N), rnd.randrange(N)
        if any(min(abs(cx - px), N - abs(cx - px)) + min(abs(cy - py), N - abs(cy - py)) < 6 for px, py in placed):
            continue
        shape = rnd.choice(shapes)
        if rnd.random() < 0.5:
            shape = tuple((-dx, dy) for dx, dy in shape)
        cells = {((cx + dx) % N, (cy + dy) % N) for dx, dy in shape}
        if any(g[y][x] != -1 for x, y in cells):
            continue
        placed.append((cx, cy))
        for x, y in cells:
            up = (x, (y - 1) % N) not in cells
            left = ((x - 1) % N, y) not in cells
            down = (x, (y + 1) % N) not in cells
            right = ((x + 1) % N, y) not in cells
            if (up or left) and not (down and right):
                g[y][x] = 3
            elif down or right:
                g[y][x] = 1
            else:
                g[y][x] = 2
        top = min(cells, key=lambda c: (c[1], c[0]))
        g[top[1]][top[0]] = 4
        for x, y in cells:
            for sx, sy in ((x + 1, y + 1), (x + 1, y), (x, y + 1)):
                sx, sy = sx % N, sy % N
                if g[sy][sx] == -1 and (sx, sy) not in cells and rnd.random() < 0.7:
                    g[sy][sx] = 0
    return g


def metal_block(seed: int, panels: int = 1) -> Grid:
    """
    Metallblock wie Vanilla-Eisenblock: Rahmen mit Lichtkante oben/links (5) und Schatten unten/rechts (1),
    innen ruhige waagerechte Bahnen (2-4). Stufen 0..5.
    """
    rnd = random.Random(seed)
    g = grid(3)
    for y in range(N):
        band = 3 if (y // 2) % 3 != 2 else 4
        for x in range(N):
            g[y][x] = band if rnd.random() > 0.08 else band - 1
    for i in range(N):
        g[0][i] = 5
        g[i][0] = 5
        g[N - 1][i] = 1
        g[i][N - 1] = 1
        g[1][i] = max(g[1][i], 4) if 0 < i < N - 1 else g[1][i]
        g[N - 2][i] = 2 if 0 < i < N - 1 else g[N - 2][i]
    g[0][N - 1] = 3
    g[N - 1][0] = 3
    g[N - 1][N - 1] = 0
    if panels == 4:
        for i in range(N):
            g[7][i] = 1
            g[8][i] = 5 if 0 < i < N - 1 else g[8][i]
            g[i][7] = 1
            g[i][8] = 5 if 0 < i < N - 1 else g[i][8]
    return g


def crystal_block(seed: int, facets: int = 7) -> Grid:
    """
    Kristallblock: Facetten (Voronoi-Zellen) mit je eigener Grundhelligkeit, an den oberen/linken Zellkanten eine
    helle Linie, an den unteren/rechten eine dunkle. Stufen 0..5.
    """
    rnd = random.Random(seed)
    pts = [(rnd.uniform(0, N), rnd.uniform(0, N), rnd.choice((2, 2, 3, 3, 3, 4))) for _ in range(facets)]

    def owner(x: float, y: float) -> int:
        best, idx = 1e9, 0
        for i, p in enumerate(pts):
            dx = min(abs(x - p[0]), N - abs(x - p[0]))
            dy = min(abs(y - p[1]), N - abs(y - p[1]))
            d = dx * dx + dy * dy
            if d < best:
                best, idx = d, i
        return idx

    own = [[owner(x + 0.5, y + 0.5) for x in range(N)] for y in range(N)]
    g = grid(0)
    for y in range(N):
        for x in range(N):
            o = own[y][x]
            v = pts[o][2]
            if own[(y - 1) % N][x] != o or own[y][(x - 1) % N] != o:
                v = 5 if v >= 3 else 4
            elif own[(y + 1) % N][x] != o or own[y][(x + 1) % N] != o:
                v = 1
            g[y][x] = v
    return g


# =============================================================================
# Vorschau
# =============================================================================

def preview(images: dict[str, Image.Image], path: str, zoom: int = 8, tile: int = 3, cols: int = 4) -> None:
    """Bogen mit jeder Textur einmal groß und 3×3 gekachelt (um Nähte und Wiederholungen zu sehen)."""
    from PIL import ImageDraw
    cell_w = 16 * zoom + 16 * (zoom // 2) * tile + 24
    cell_h = max(16 * zoom, 16 * (zoom // 2) * tile) + 22
    names = list(images)
    rows = (len(names) + cols - 1) // cols
    out = Image.new("RGB", (cols * cell_w, rows * cell_h), (38, 38, 44))
    d = ImageDraw.Draw(out)
    for i, name in enumerate(names):
        img = images[name]
        x0 = (i % cols) * cell_w + 8
        y0 = (i // cols) * cell_h + 4
        bg = Image.new("RGBA", (16, 16), (72, 72, 84, 255))
        bg.alpha_composite(img.crop((0, 0, 16, 16)))
        out.paste(bg.resize((16 * zoom, 16 * zoom), Image.NEAREST).convert("RGB"), (x0, y0))
        t = Image.new("RGBA", (16 * tile, 16 * tile), (72, 72, 84, 255))
        for tx in range(tile):
            for ty in range(tile):
                t.alpha_composite(img.crop((0, 0, 16, 16)), (tx * 16, ty * 16))
        z2 = zoom // 2
        out.paste(t.resize((16 * tile * z2, 16 * tile * z2), Image.NEAREST).convert("RGB"), (x0 + 16 * zoom + 8, y0))
        d.text((x0, y0 + max(16 * zoom, 16 * z2 * tile) + 2), name, fill=(225, 225, 230))
    out.save(path)
