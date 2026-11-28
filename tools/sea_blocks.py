#!/usr/bin/env python3
"""
Blöcke des Meeresgrunds (0.62, im Vanilla-Stil, siehe tools/vanilla_style.py):

  * Seeanemonen in vier Farben: Fuß und Mundscheibe (_body) und ein Kranz wiegender Fangarme (_tentacles, bewegt,
    mit leuchtenden Spitzen)
  * Muscheln: Herzmuschel, Kammmuschel und Wellhornschnecke – je ein kleiner Atlas für die 3D-Modelle
  * Röhrenwürmer: weiße, geringelte Röhren mit roten Federkronen
  * Schlotgestein (dunkel, porös, rostig), Schwefelkruste und der heiße Schlot (oben eine glühende Öffnung, bewegt)
  * Leuchtalgen: ein dünner Belag aus Fäden mit leuchtenden Punkten
  * Riesentang: goldbraune Wedel mit Schwimmblasen (bewegt), Spitze und Stängel

Wird von tools/generate_textures.py aufgerufen (build(put)). Bewegte Texturen bekommen ihre .mcmeta von dort
(siehe ANIMATIONS).
"""
from __future__ import annotations

import math
import random
from typing import Callable

from PIL import Image

import pixelart as pa
import vanilla_style as vs
from pixelart import Color, hexc

N = 16

# Bildfolgen: Name -> (Bilder, Bilddauer in Ticks, weich überblenden)
ANIMATIONS: dict[str, tuple[int, int, bool]] = {}


def _sheet(frames: list[Image.Image]) -> Image.Image:
    sheet = Image.new("RGBA", (N, N * len(frames)))
    for i, f in enumerate(frames):
        sheet.paste(f, (0, i * N))
    return sheet


# =============================================================================
# Seeanemonen
# =============================================================================

# Farbe: Fuß (dunkel -> hell), Fangarme (dunkel -> hell), Spitzen (leuchtend), Mund
ANEMONES: dict[str, dict[str, list[Color] | Color]] = {
    "purple": {"body": vs.tones("#5c2f72", 4, spread=0.3, cool=260, warm=320, hue=10),
               "arm": [hexc("#5e2a86"), hexc("#8a3cb4"), hexc("#b25ad8"), hexc("#d48cf0")],
               "tip": [hexc("#f0b8ff"), hexc("#fff0ff")], "mouth": hexc("#2a1236")},
    "red": {"body": vs.tones("#7a1f28", 4, spread=0.3, cool=340, warm=20, hue=8),
            "arm": [hexc("#8a1c26"), hexc("#c22e3a"), hexc("#e8504e"), hexc("#ff8a78")],
            "tip": [hexc("#ffc2a8"), hexc("#fff2e8")], "mouth": hexc("#3a0c12")},
    "green": {"body": vs.tones("#2e5e44", 4, spread=0.3, cool=170, warm=100, hue=8),
              "arm": [hexc("#1e6a46"), hexc("#2e9a5e"), hexc("#4cc878"), hexc("#8ef0a4")],
              "tip": [hexc("#c8ffd2"), hexc("#f2fff4")], "mouth": hexc("#10301e")},
    "orange": {"body": vs.tones("#8a4c1e", 4, spread=0.3, cool=15, warm=45, hue=8),
               "arm": [hexc("#a8501a"), hexc("#d87422"), hexc("#f49a2e"), hexc("#ffc45a")],
               "tip": [hexc("#ffe49a"), hexc("#fffbe2")], "mouth": hexc("#3a1c08")},
}

ANEMONE_FRAMES = 10


def anemone_body(color: str) -> Image.Image:
    """
    Fuß und Mundscheibe (ein Atlas): oben links (0..8, 0..8) die Mundscheibe von oben – Ringe von Fangarm-Ansätzen um
    den dunklen Mund; unten (0..16, 8..16) die Seite des Fußes mit senkrechten Furchen und hellen Warzen.
    """
    pal = ANEMONES[color]
    body: list[Color] = pal["body"]  # type: ignore[assignment]
    arm: list[Color] = pal["arm"]  # type: ignore[assignment]
    img = pa.new()
    px = img.load()
    rnd = random.Random(sum(map(ord, color)) * 17 % 9973)
    # Mundscheibe 8x8
    for y in range(8):
        for x in range(8):
            dx, dy = x - 3.5, y - 3.5
            r = math.hypot(dx, dy)
            a = math.atan2(dy, dx)
            if r < 1.2:
                c = pal["mouth"]  # type: ignore[assignment]
            elif r < 2.2:
                c = body[3]
            else:
                ray = (math.sin(a * 6) + 1) / 2
                c = arm[2] if ray > 0.55 else arm[1]
                if r > 3.4:
                    c = arm[0] if ray < 0.5 else arm[1]
            px[x, y] = c
    # Rand der Mundscheibe als Seite (8..16, 0..2): Fangarm-Ansätze
    for x in range(8, 16):
        for y in range(0, 2):
            px[x, y] = arm[1] if (x + y) % 2 else arm[2]
    # Fuß: senkrechte Furchen, unten dunkler, ein paar helle Warzen (Saugwarzen der Pferdeaktinie)
    for y in range(8, 16):
        for x in range(16):
            furrow = (x % 3 == 0)
            level = 2 if not furrow else 1
            if y >= 14:
                level -= 1
            if y == 8:
                level = 3
            px[x, y] = body[max(0, min(3, level))]
    for _ in range(6):
        x, y = rnd.randrange(16), rnd.randrange(9, 14)
        px[x, y] = pa.mix(body[3], arm[3], 0.5)
    return img


def anemone_tentacles(color: str) -> tuple[Image.Image, Image.Image]:
    """
    Ein dichter Kranz Fangarme (Bildfolge): 13 Arme wachsen von unten (Zeile 15) nach oben und außen, unten zwei Pixel
    dick, jeder mit einer hellen, leuchtenden Kugelspitze, und alle wiegen sich langsam – oben weiter als unten. Die
    hinteren Arme sind dunkler (Tiefe). Dazu die Leuchtmaske (Spitzen und der Glanz der oberen Arme).
    """
    pal = ANEMONES[color]
    arm: list[Color] = pal["arm"]  # type: ignore[assignment]
    tip: list[Color] = pal["tip"]  # type: ignore[assignment]
    rnd = random.Random(sum(map(ord, color)) * 31)
    arms = []
    for i in range(13):
        base_x = 2.2 + i * (11.6 / 12) + rnd.uniform(-0.25, 0.25)
        edge = abs(base_x - 8.0) / 6.0
        height = rnd.uniform(9.5, 12.5) * (1.0 - 0.35 * edge * edge)
        lean = (base_x - 8.0) * 0.55 + rnd.uniform(-0.6, 0.6)
        phase = rnd.uniform(0, 2 * math.pi)
        back = i % 2 == 1
        arms.append((base_x, height, lean, phase, rnd.uniform(0.7, 1.2), back))
    # hintere Arme zuerst, damit die vorderen sie überdecken
    arms.sort(key=lambda a: not a[5])
    frames, glows = [], []
    for f in range(ANEMONE_FRAMES):
        t = f / ANEMONE_FRAMES * 2 * math.pi
        img = pa.new()
        glow = pa.new()
        px, gp = img.load(), glow.load()
        for base_x, height, lean, phase, amp, back in arms:
            steps = int(height * 3)
            for s in range(steps + 1):
                u = s / steps
                y = 15.6 - u * height
                sway = math.sin(t + phase + u * 1.6) * amp * u ** 1.3
                x = base_x + lean * u ** 1.5 + sway
                ix, iy = int(math.floor(x)), int(math.floor(y))
                if not (0 <= iy < N):
                    continue
                if u > 0.9:
                    c = tip[1] if u > 0.97 else tip[0]
                elif u > 0.62:
                    c = arm[3]
                elif u > 0.3:
                    c = arm[2]
                else:
                    c = arm[1]
                if back:
                    c = pa.mix(c, arm[0], 0.45)
                cells = [(ix, iy)]
                if u < 0.45:
                    cells.append((ix + 1, iy))
                for cx, cy in cells:
                    if 0 <= cx < N:
                        px[cx, cy] = c
                        if u > 0.9:
                            gp[cx, cy] = c
                        elif u > 0.62 and not back:
                            gp[cx, cy] = pa.mix(c, (0, 0, 0, 255), 0.45)
            # die Kugelspitze
            u = 1.0
            x = base_x + lean + math.sin(t + phase + 1.6) * amp
            y = 15.6 - height
            ix, iy = int(math.floor(x)), int(math.floor(y))
            for cx, cy in ((ix, iy), (ix, iy - 1)):
                if 0 <= cx < N and 0 <= cy < N:
                    c = tip[1] if cy == iy - 1 else tip[0]
                    if back:
                        c = pa.mix(c, arm[2], 0.4)
                    px[cx, cy] = c
                    gp[cx, cy] = c
        frames.append(img)
        glows.append(glow)
    return _sheet(frames), _sheet(glows)


# =============================================================================
# Muscheln
# =============================================================================

COCKLE = [hexc("#7a6248"), hexc("#a88a66"), hexc("#d2b890"), hexc("#efe2c4"), hexc("#fbf5e6")]
COCKLE_BAND = [hexc("#8a5a34"), hexc("#b07848")]
SCALLOP = [hexc("#9a3a2a"), hexc("#d0603e"), hexc("#ee8c62"), hexc("#ffb894"), hexc("#ffe2cc")]
WHELK = [hexc("#5e4632"), hexc("#86684a"), hexc("#b0926c"), hexc("#d6bc96"), hexc("#f2e2c4")]
WHELK_BAND = [hexc("#6a3e24"), hexc("#8e5434")]
NACRE = [hexc("#d8b4c8"), hexc("#f0d8e4"), hexc("#fff4fa")]


def cockle() -> Image.Image:
    """
    Herzmuschel (Atlas): oben links (0..8, 0..8) die Schale von oben – ein Oval (außen durchsichtig), Rippen strahlen
    vom Wirbel (oben Mitte) aus, unten ein brauner Wachstumssaum; rechts (8..12, 0..4) das Innere (Perlmutt).
    """
    img = pa.new()
    px = img.load()
    for y in range(8):
        for x in range(8):
            dx, dy = (x - 3.5) / 3.9, (y - 3.8) / 3.6
            if dx * dx + dy * dy > 1.0:
                continue
            a = math.atan2(x - 3.5, y + 2.0)
            rib = math.cos(a * 16) > 0.0
            v = 3 if rib else 2
            if dy < -0.55:
                v += 1
            if dx * dx + dy * dy > 0.7:
                v -= 1
            c = COCKLE[max(0, min(4, v))]
            if y in (5, 6) and dx * dx + dy * dy > 0.35:
                c = COCKLE_BAND[1] if rib else COCKLE_BAND[0]
            px[x, y] = c
    for y in range(4):
        for x in range(8, 12):
            px[x, y] = NACRE[(x + y) % 3]
    return img


def scallop() -> Image.Image:
    """
    Kammmuschel (Atlas): oben (0..10, 0..9) der Fächer von oben – Rippen strahlen vom Schloss (unten Mitte) aus, der
    Rand ist gewellt, zwei „Ohren“ am Schloss (außerhalb des Fächers durchsichtig); unten (0..10, 10..11) der Rand.
    """
    img = pa.new()
    px = img.load()
    cx, cy = 4.5, 8.6
    for y in range(9):
        for x in range(10):
            dx, dy = x - cx, cy - y
            r = math.hypot(dx, dy)
            a = math.atan2(dx, dy)
            edge = 8.0 + 0.45 * math.cos(a * 9)
            inside = r < edge and abs(a) < 1.15
            ears = y >= 7 and abs(dx) < 3.6
            if not (inside or ears):
                continue
            rib = math.cos(a * 9) > 0.05
            v = 3 if rib else 2
            if r > edge - 1.2:
                v = 4 if rib else 3
            if r < 2.5:
                v = 1
            if ears and not inside:
                v = 2
            px[x, y] = SCALLOP[v]
    for x in range(10):
        px[x, 10] = SCALLOP[1] if x % 2 else SCALLOP[2]
    return img


def whelk() -> Image.Image:
    """
    Wellhornschnecke (Atlas): (0..16, 0..4) die Seite der Windungen – schräge Spiralbänder; (0..4, 5..9) die Mündung
    (Perlmutt, rosig, dunkler Rand); (5..9, 5..9) die Spitze.
    """
    img = pa.new()
    px = img.load()
    for y in range(4):
        for x in range(16):
            band = (x + y * 2) % 5
            v = [2, 3, 4, 4, 3][band]
            c = WHELK[v]
            if band == 0:
                c = WHELK_BAND[0] if y % 2 else WHELK_BAND[1]
            if y == 0:
                c = pa.mix(c, WHELK[4], 0.3)
            if y == 3:
                c = pa.mix(c, WHELK[0], 0.3)
            px[x, y] = c
    for y in range(5, 9):
        for x in range(4):
            d = abs(x - 1.5) + abs(y - 6.5)
            px[x, y] = WHELK[1] if d > 2.2 else NACRE[1] if d > 1 else hexc("#e8a8b8")
    for y in range(5, 9):
        for x in range(5, 9):
            px[x, y] = WHELK[2] if (x + y) % 2 else WHELK[3]
    return img


# =============================================================================
# Röhrenwürmer
# =============================================================================

TUBE = [hexc("#8c8478"), hexc("#b8b0a2"), hexc("#dcd6ca"), hexc("#f4f0e6")]
PLUME = [hexc("#6e0e16"), hexc("#a8161e"), hexc("#d82a26"), hexc("#ff5e3a"), hexc("#ff9a6a")]


def tube_worms() -> Image.Image:
    """
    Atlas: (0..4, 0..16) die Seite einer Röhre – weiße Ringe mit grauen Fugen, unten etwas Belag; (4..12, 0..8) die rote
    Federkrone von der Seite (für die Kreuzflächen, Rest durchsichtig); (12..16, 0..4) die Röhre von oben (Öffnung).
    """
    img = pa.new()
    px = img.load()
    rnd = random.Random(62101)
    for y in range(16):
        ring = y % 3
        for x in range(4):
            v = [3, 2, 1][ring]
            if x == 0:
                v = max(0, v - 1)
            if x == 3:
                v = max(0, v - 1)
            c = TUBE[v]
            if y >= 13:
                c = pa.mix(c, hexc("#5a4a3a"), 0.35 + 0.1 * (y - 13))
            px[x, y] = c
    # Federkrone: zwei Reihen gefiederter Fäden fächern von unten Mitte (8, 7) nach oben und außen auseinander
    for k in range(11):
        a = -1.05 + k * 0.21
        length = 7.4 - abs(a) * 1.6 + rnd.uniform(-0.4, 0.4)
        for s in range(14):
            u = s / 13
            x = 8 + math.sin(a) * length * u
            y = 7.8 - math.cos(a) * length * u
            ix, iy = int(math.floor(x)), int(math.floor(y))
            if 4 <= ix < 12 and 0 <= iy < 8:
                c = PLUME[1] if u < 0.35 else PLUME[2] if u < 0.7 else PLUME[3]
                if s % 4 == 3:
                    c = PLUME[4]
                px[ix, iy] = c
    for y in range(6, 8):
        for x in range(7, 9):
            px[x, y] = PLUME[0]
    for y in range(4):
        for x in range(12, 16):
            d = math.hypot(x - 13.5, y - 1.5)
            px[x, y] = PLUME[0] if d < 1.0 else TUBE[2] if d < 1.8 else TUBE[1]
    return img


# =============================================================================
# Schlotgestein, Schwefelkruste und heißer Schlot
# =============================================================================

VENT_ROCK = vs.tones("#3a3532", 5, spread=0.42, cool=230, warm=25, hue=10, sat=0.04)
RUST = [hexc("#6a3418"), hexc("#94501e"), hexc("#bc7428")]
SULFUR = [hexc("#8a7a3a"), hexc("#b8a248"), hexc("#d8c45c"), hexc("#ecdc84"), hexc("#f8f2c4")]
WHITE_CRUST = [hexc("#b8b4a8"), hexc("#d8d4c8"), hexc("#f0ece2")]
EMBER = [hexc("#5a1a0a"), hexc("#b03a0e"), hexc("#ee6a12"), hexc("#ffa82e"), hexc("#ffe08a")]


def _pits(img: Image.Image, seed: int, count: int, dark: Color) -> None:
    """Poren: kleine dunkle Löcher mit hellem Rand oben."""
    rnd = random.Random(seed)
    px = img.load()
    for _ in range(count):
        x, y = rnd.randrange(N), rnd.randrange(N)
        px[x, y] = dark
        if rnd.random() < 0.5:
            px[(x + 1) % N, y] = pa.mix(dark, px[(x + 1) % N, y], 0.5)
        px[x, (y - 1) % N] = pa.mix(px[x, (y - 1) % N], hexc("#8a807a"), 0.35)


def vent_rock() -> Image.Image:
    """Schlotgestein: fast schwarzes, poröses Gestein, rostige Flecken (Eisen) und ein paar weiße Kristallkörner."""
    g = vs.stone(62201, shares=(0.16, 0.3, 0.32, 0.16, 0.06), grain=0.6)
    img = vs.paint(g, VENT_ROCK)
    px = img.load()
    rust = vs.layered(62202, ((8, 8, 0.6), (4, 4, 0.4)))
    for y in range(N):
        for x in range(N):
            if rust[y][x] > 0.66:
                px[x, y] = pa.mix(px[x, y], RUST[1 if rust[y][x] < 0.74 else 2], 0.45)
    _pits(img, 62203, 9, hexc("#141210"))
    rnd = random.Random(62204)
    for _ in range(4):
        px[rnd.randrange(N), rnd.randrange(N)] = WHITE_CRUST[rnd.randrange(3)]
    return img


def sulfur_crust() -> Image.Image:
    """Schwefelkruste: körnige Krusten in Gelb und Weiß, dazwischen rostige Rinnen."""
    f = vs.layered(62301, ((4, 4, 0.45), (2, 2, 0.35), (1, 1, 0.2)))
    g = vs.quantize(f, (0.1, 0.2, 0.3, 0.25, 0.15))
    img = pa.new()
    px = img.load()
    streak = vs.layered(62302, ((16, 4, 0.7), (8, 2, 0.3)))
    for y in range(N):
        for x in range(N):
            v = g[y][x]
            c = SULFUR[v]
            if streak[y][x] > 0.62:
                c = WHITE_CRUST[min(2, v // 2)]
            if streak[y][x] < 0.3 and v <= 1:
                c = RUST[v]
            px[x, y] = c
    return img


def vent_side() -> Image.Image:
    """Seite des Schlots: Schlotgestein mit senkrechten Krusten-Rinnen in Weiß, Gelb und Rost, die nach unten laufen."""
    img = vent_rock()
    px = img.load()
    rnd = random.Random(62401)
    for col in (2, 7, 12):
        x = col + rnd.randint(-1, 1)
        length = rnd.randint(6, 12)
        for y in range(length):
            c = SULFUR[3] if y < 2 else WHITE_CRUST[2 - min(2, y // 4)] if y < 7 else RUST[2]
            px[x % N, y] = c
            if rnd.random() < 0.4:
                px[(x + 1) % N, y] = pa.mix(c, px[(x + 1) % N, y], 0.5)
            if rnd.random() < 0.25:
                x += rnd.choice((-1, 1))
    return img


VENT_FRAMES = 6


def vent_top() -> tuple[Image.Image, Image.Image]:
    """
    Oberseite des Schlots (Bildfolge): ein Kragen aus Kruste um eine runde Öffnung, in der es rot und gelb glüht und
    flackert. Dazu die Leuchtmaske (nur die Glut).
    """
    frames, glows = [], []
    for f in range(VENT_FRAMES):
        t = f / VENT_FRAMES * 2 * math.pi
        img = vent_rock()
        glow = pa.new()
        px, gp = img.load(), glow.load()
        for y in range(N):
            for x in range(N):
                dx, dy = x - 7.5, y - 7.5
                r = math.hypot(dx, dy)
                a = math.atan2(dy, dx)
                if r < 6.6 and r >= 4.6:
                    v = 3 if math.sin(a * 5 + 1.0) > 0.2 else 2
                    if r > 6.0:
                        v -= 1
                    px[x, y] = SULFUR[v] if math.sin(a * 3) > -0.3 else WHITE_CRUST[1]
                elif r < 4.6:
                    heat = 1.0 - r / 4.6
                    flicker = 0.18 * math.sin(t + a * 2 + r) + 0.1 * math.sin(2 * t - a * 3)
                    v = heat + flicker
                    if r > 3.8:
                        c = EMBER[0]
                    elif v > 0.72:
                        c = EMBER[4]
                    elif v > 0.48:
                        c = EMBER[3]
                    elif v > 0.24:
                        c = EMBER[2]
                    else:
                        c = EMBER[1]
                    px[x, y] = c
                    if c != EMBER[0]:
                        gp[x, y] = c
        frames.append(img)
        glows.append(glow)
    return _sheet(frames), _sheet(glows)


# =============================================================================
# Leuchtalgen
# =============================================================================

ALGAE = [hexc("#0e3a40"), hexc("#16585c"), hexc("#227a78")]
ALGAE_STRANDS = 20
ALGAE_LIGHT = [hexc("#3ad2c8"), hexc("#8af8ec"), hexc("#e2fffa")]


def glow_algae() -> tuple[Image.Image, Image.Image]:
    """
    Leuchtalgen: ein lockerer Belag aus feinen, verzweigten Fäden (Lücken durchsichtig), an deren Enden und Knoten
    leuchtende Pünktchen sitzen. Dazu die Leuchtmaske (nur die Pünktchen und ein Hauch der Fäden).
    """
    img = pa.new()
    glow = pa.new()
    px, gp = img.load(), glow.load()
    rnd = random.Random(62501)
    for _ in range(ALGAE_STRANDS):
        x, y = rnd.uniform(0, N), rnd.uniform(0, N)
        a = rnd.uniform(0, 2 * math.pi)
        length = rnd.randint(5, 10)
        for s in range(length):
            ix, iy = int(x) % N, int(y) % N
            px[ix, iy] = ALGAE[1 + (s % 2)] if s < length - 1 else ALGAE_LIGHT[0]
            if s == length - 1:
                gp[ix, iy] = ALGAE_LIGHT[0]
            elif s % 3 == 0:
                gp[ix, iy] = pa.mix(ALGAE[2], (0, 0, 0, 255), 0.4)
            a += rnd.uniform(-0.7, 0.7)
            x += math.cos(a)
            y += math.sin(a)
            if rnd.random() < 0.18:
                # ein kurzer Seitenzweig
                bx, by = ix + rnd.choice((-1, 1)), iy + rnd.choice((-1, 1))
                px[bx % N, by % N] = ALGAE[0]
    for _ in range(26):
        x, y = rnd.randrange(N), rnd.randrange(N)
        if px[x, y][3] == 0 and rnd.random() < 0.6:
            continue
        c = ALGAE_LIGHT[rnd.choice((0, 1, 1, 2))]
        px[x, y] = c
        gp[x, y] = c
    return img, glow


# =============================================================================
# Riesentang
# =============================================================================

KELP = [hexc("#4a3212"), hexc("#7a5618"), hexc("#a8801e"), hexc("#ceaa34"), hexc("#ead266")]
KELP_STIPE = [hexc("#3a2a10"), hexc("#5e4418")]
BLADDER = [hexc("#8a6a1e"), hexc("#c8a43a"), hexc("#f0d878")]
KELP_FRAMES = 16


def _kelp_frame(t: float, plant: bool, seed: int) -> Image.Image:
    """
    Ein Bild des Riesentangs: der dunkle Stängel windet sich leicht; an ihm sitzen wechselständig breite, gewellte
    Wedel (drei Pixel breit, dunkler Rand, helle Mittelrippe), jeder an einer runden, glänzenden Schwimmblase. Die
    Spitze ({@code plant}=False) endet in einem jungen, eingerollten Wedel.
    """
    img = pa.new()
    px = img.load()
    rnd = random.Random(seed)
    top = 0 if plant else 5

    def stem_x(y: float) -> float:
        return 7.5 + math.sin(t + y * 0.35) * 0.8

    def put(x: float, y: float, c: Color) -> None:
        xx, yy = int(math.floor(x)), int(math.floor(y))
        if 0 <= xx < N and 0 <= yy < N:
            px[xx, yy] = c

    # Wedel zuerst (hinter dem Stängel)
    blades = [(14.5, -1), (11.0, 1), (7.0, -1), (3.0, 1)] if plant else [(14.0, 1), (10.0, -1), (7.0, 1)]
    for y0, side in blades:
        bx = stem_x(y0) + side * 1.6
        length = 8.5 + rnd.uniform(-1.0, 1.0)
        phase = rnd.uniform(0, 6.28)
        for s in range(40):
            u = s / 39
            cx = bx + side * (0.6 + u * length * 0.62)
            cy = y0 - 0.8 - u * length * 0.78 + math.sin(t + phase + u * 2.6) * 1.2 * u
            width = 2.6 * math.sin(math.pi * min(1.0, u * 1.0 + 0.08))
            for w in (-1.0, -0.66, -0.33, 0.0, 0.33, 0.66, 1.0):
                if abs(w) * 2.6 > width + 0.3:
                    continue
                edge = abs(w) * 2.6 > width - 0.6
                ripple = int((u * 11 + w * 2) * 1.5) % 3 == 0
                if edge:
                    c = KELP[1] if w > 0 else KELP[2]
                elif abs(w) < 0.2:
                    c = KELP[4] if u > 0.45 else KELP[3]
                else:
                    c = KELP[3] if not ripple else KELP[2]
                put(cx + w * 0.55 * side, cy + w * 1.25, c)
        # Schwimmblase am Fuß des Wedels
        for dx, dy, c in ((0, 0, BLADDER[1]), (side, 0, BLADDER[0]), (0, -1, BLADDER[2]), (side, -1, BLADDER[1])):
            put(bx + dx, y0 + dy - 0.5, c)
    for y in range(top, N):
        x = stem_x(y)
        put(x - 0.5, y, KELP_STIPE[1])
        put(x + 0.5, y, KELP_STIPE[0])
    if not plant:
        sx = stem_x(4)
        for dx, dy, c in ((0, 0, KELP[3]), (1, 0, KELP[2]), (1, -1, KELP[4]), (0, -1, KELP[3]), (-1, -1, KELP[2]), (-1, -2, KELP[3]),
                          (0, -2, KELP[4]), (0, -3, KELP[3]), (0, 1, KELP_STIPE[1])):
            put(sx + dx - 0.5, 4 + dy, c)
    return img


def giant_kelp(plant: bool) -> Image.Image:
    frames = [_kelp_frame(f / KELP_FRAMES * 2 * math.pi, plant, 62601 if plant else 62602) for f in range(KELP_FRAMES)]
    return _sheet(frames)


# =============================================================================
# Partikel: Schlotrauch und Meeresleuchten
# =============================================================================

SMOKE = [hexc("#1a1614"), hexc("#26201c"), hexc("#342c26"), hexc("#463c34"), hexc("#5a5048")]


def vent_smoke(frame: int) -> Image.Image:
    """
    Schwarzer Rauch eines heißen Schlots (8 Bilder): ein dichter, dunkler Ballen, der sich ausbreitet, löchrig wird und
    verblasst; ganz am Anfang glimmt im Kern noch ein Funke Glut.
    """
    img = pa.new()
    px = img.load()
    t = frame / 7.0
    noise = pa.value_noise(62701 + frame * 13, 3)
    radius = 0.5 + 0.45 * t
    for y in range(N):
        for x in range(N):
            dx, dy = (x - 7.5) / 7.5, (y - 7.5) / 7.5
            d = math.hypot(dx, dy)
            n = noise[y][x]
            edge = radius * (0.8 + 0.35 * n)
            if d > edge:
                continue
            inner = 1.0 - d / max(edge, 1e-3)
            if n < 0.18 + 0.42 * t and inner < 0.55:
                continue
            v = inner * 0.6 + (n - 0.5) * 0.4 + t * 0.25
            c = SMOKE[max(0, min(4, int(v * 5)))]
            if frame < 2 and inner > 0.7 and n > 0.6:
                c = EMBER[2] if frame == 0 else EMBER[1]
            a = int(255 * min(1.0, 0.35 + 0.9 * inner) * (1.0 - 0.45 * t))
            px[x, y] = (c[0], c[1], c[2], max(50, a))
    return img


GLOW_DOT = [hexc("#2ab8c8"), hexc("#5ae8e8"), hexc("#a8fff6"), hexc("#f0fffc")]


def sea_glow(frame: int) -> Image.Image:
    """Meeresleuchten (4 Bilder): ein winziger, türkis leuchtender Punkt mit weichem Schein, der langsam erlischt."""
    img = pa.new()
    px = img.load()
    fade = 1.0 - frame / 4.0
    for y in range(N):
        for x in range(N):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 1.6:
                c = GLOW_DOT[3] if frame < 2 else GLOW_DOT[2]
                a = 255
            elif d < 3.6:
                c = GLOW_DOT[2] if d < 2.6 else GLOW_DOT[1]
                a = int(230 * fade)
            elif d < 6.5:
                c = GLOW_DOT[0]
                a = int(150 * fade * (1.0 - (d - 3.6) / 2.9))
            else:
                continue
            if a > 8:
                px[x, y] = (c[0], c[1], c[2], a)
    return img


# =============================================================================
# Ausgabe
# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    for color in ANEMONES:
        put(f"block/{color}_sea_anemone", anemone_body(color))
        arms, glow = anemone_tentacles(color)
        put(f"block/{color}_sea_anemone_tentacles", arms)
        put(f"block/{color}_sea_anemone_tentacles_glow", glow)
        ANIMATIONS[f"block/{color}_sea_anemone_tentacles"] = (ANEMONE_FRAMES, 4, True)
        ANIMATIONS[f"block/{color}_sea_anemone_tentacles_glow"] = (ANEMONE_FRAMES, 4, True)
    put("block/cockle_shells", cockle())
    put("block/scallop_shells", scallop())
    put("block/whelk_shells", whelk())
    put("block/tube_worms", tube_worms())
    put("block/vent_rock", vent_rock())
    put("block/sulfur_crust", sulfur_crust())
    put("block/hydrothermal_vent_side", vent_side())
    put("block/hydrothermal_vent_bottom", vent_rock())
    top, top_glow = vent_top()
    put("block/hydrothermal_vent_top", top)
    put("block/hydrothermal_vent_top_glow", top_glow)
    ANIMATIONS["block/hydrothermal_vent_top"] = (VENT_FRAMES, 5, True)
    ANIMATIONS["block/hydrothermal_vent_top_glow"] = (VENT_FRAMES, 5, True)
    algae, algae_glow = glow_algae()
    put("block/glow_algae", algae)
    put("block/glow_algae_glow", algae_glow)
    put("block/giant_kelp", giant_kelp(False))
    put("block/giant_kelp_plant", giant_kelp(True))
    ANIMATIONS["block/giant_kelp"] = (KELP_FRAMES, 3, True)
    ANIMATIONS["block/giant_kelp_plant"] = (KELP_FRAMES, 3, True)
    for frame in range(8):
        put(f"particle/vent_smoke_{frame}", vent_smoke(frame))
    for frame in range(4):
        put(f"particle/sea_glow_{frame}", sea_glow(frame))


if __name__ == "__main__":
    import sys
    out: dict[str, Image.Image] = {}
    build(lambda name, img: out.__setitem__(name, img))
    tiles = []
    for name, img in out.items():
        frame = img.crop((0, 0, N, N))
        bg = Image.new("RGBA", (N, N), (38, 70, 96, 255))
        bg.alpha_composite(frame)
        tiles.append(bg.resize((N * 8, N * 8), Image.NEAREST))
    cols = 6
    rows = (len(tiles) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * 136, rows * 136), (16, 16, 16, 255))
    for i, t in enumerate(tiles):
        sheet.paste(t, ((i % cols) * 136, (i // cols) * 136))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "sea_blocks_preview.png")
    print("\n".join(f"{i}: {n}" for i, n in enumerate(out)))
