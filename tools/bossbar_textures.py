#!/usr/bin/env python3
"""
Bossleisten der Drachen-Bosse (Version 2): je Element ein eigener, verzierter Rahmen.

  * links ein Medaillon mit dem Bildnis des Bosses (aus dem 3D-Modell gerendert, doppelte Auflösung)
  * die Leiste selbst (182 x 9) in einem Metallrahmen mit Nieten, darüber zwei Fassungen für die Phasen-Edelsteine
    (60 % und 30 %), rechts eine Spitze in Form des Elements (Flamme, Kristall, Blitz, Blatt, Fels, Flosse)
  * Verzierungen oben und unten: Flammen, Eiskristalle und Eiszapfen, Blitze, Ranken mit Blättern, Felszacken mit
    Bernstein, Wellenkämme mit Gischt
  * Füllung mit wanderndem Muster (wird im Spiel verschoben), leere Leiste, Spur für erlittenen Schaden, Glanz,
    leuchtende Aura für die letzte Phase, Edelsteine (leuchtend / zersprungen)

Aufbau einer Textur (256 x 256), siehe BossBarRenderer.java:
  (0, 0)     240 x 44  Rahmen; Fenster der Leiste bei (48, 18), 182 x 9; Medaillon-Mitte (21, 22)
  (0, 48)    256 x 9   Füllung (nahtlos kachelbar, wird verschoben)
  (0, 58)    182 x 9   leere Leiste
  (0, 68)    182 x 9   Schadensspur
  (184, 58)   16 x 9   Glanz
  (204, 58)    9 x 9   Edelstein leuchtend      (214, 58) Edelstein zersprungen
  (0, 80)    240 x 44  Aura (letzte Phase)
  (0, 128)    64 x 64  Bildnis (wird auf 32 x 32 gezeichnet)
"""
from __future__ import annotations

import math
import sys
from pathlib import Path

from PIL import Image, ImageEnhance

sys.path.insert(0, str(Path(__file__).resolve().parent))

from pixelart import CLEAR, hexc, mix  # noqa: E402

FRAME_W, FRAME_H = 240, 44
BAR_X, BAR_Y, BAR_W, BAR_H = 48, 18, 182, 9
EMB_X, EMB_Y, EMB_R = 21, 22, 20.0
GEMS = (0.6, 0.3)

# Metall dunkel / mittel / hell, Akzent, Leuchten, dunkler Grund
ELEMENTS = {
    "fire": dict(m0="#1c1412", m1="#4a3a34", m2="#9a8478", acc="#ff6a1a", glow="#ffd27a", deep="#2a0806",
                 f0="#5a0c04", f1="#d8360a", f2="#ff9a2a", f3="#ffe9a0"),
    "ice": dict(m0="#1a2430", m1="#4e6478", m2="#b8d0e0", acc="#6ad0f8", glow="#e8fbff", deep="#0a1c2a",
                f0="#0e3a66", f1="#2a86d0", f2="#7ad0f8", f3="#eafcff"),
    "lightning": dict(m0="#16141e", m1="#3c3a4c", m2="#a8a4bc", acc="#ffd21a", glow="#fff6b0", deep="#100e20",
                      f0="#5a4206", f1="#c89a10", f2="#ffd84a", f3="#fffbe0"),
    "nature": dict(m0="#1e160e", m1="#5a4428", m2="#a88e60", acc="#5ac832", glow="#d8ffa0", deep="#0c1c0a",
                   f0="#14460e", f1="#2e8a1e", f2="#6cd03a", f3="#d4ff9a"),
    "earth": dict(m0="#1e1a16", m1="#4e463c", m2="#a49684", acc="#e09a3a", glow="#ffe2a0", deep="#20140a",
                  f0="#5a2e08", f1="#b0661a", f2="#e8a440", f3="#fff0c0"),
    "water": dict(m0="#0e1a22", m1="#2e4a5a", m2="#8ab4c4", acc="#28c0f0", glow="#c8f6ff", deep="#04182a",
                  f0="#063a5e", f1="#0e78b8", f2="#3cc4ec", f3="#d8faff"),
    "shadow": dict(m0="#0c0812", m1="#2e2442", m2="#8a7ca8", acc="#b46cff", glow="#f0e0ff", deep="#12061e",
                   f0="#2a0a4e", f1="#6420b0", f2="#a660ff", f3="#f2e4ff"),
    # der Zerbrochene König (Boss der Leere): Sternenmarmor mit Goldsaum, Füllung wie ein Sternennebel
    "void": dict(m0="#16121f", m1="#4a4262", m2="#d2cae6", acc="#d6a23c", glow="#f4ecff", deep="#0a0618",
                 f0="#1a0c40", f1="#4c26a0", f2="#9a6ae8", f3="#f4eeff", gold="#e2b04a", gold_dark="#7a5420"),
    # Karkinos, der Riffkoloss: ausgebleichter Muschelkalk mit Goldsaum, darauf ein Korallenriff; Füllung wie das
    # Licht einer Lagune (Wellennetz und Blasen)
    "reef": dict(m0="#2a2219", m1="#7a6850", m2="#eadfc4", acc="#2ad8c8", glow="#d4fff4", deep="#03222a",
                 f0="#043e48", f1="#0a8486", f2="#38d4c4", f3="#e4fff6", gold="#e8b84a", gold_dark="#7a5a1e",
                 coral="#ee5a40", coral_dark="#7e2420", coral_light="#ffb08a", fan="#c25ab8", fan_dark="#5e2058",
                 sponge="#f0a428", sponge_dark="#7a4a10", brain="#d8c060", brain_dark="#7a6420",
                 anemone="#7ae8a8", anemone_dark="#1e6a4c", star="#ff8a3a"),
    # Noctua, die Mottenkönigin: Mondsilber mit Goldsaum, darauf Mottenflügel, Seidenfäden und Mondsicheln; Füllung wie
    # schimmernder Flügelstaub (Schuppenreihen, Glitzer)
    "moth": dict(m0="#1a1428", m1="#5a4e78", m2="#e2dcf2", acc="#ff8ad0", glow="#fff0fa", deep="#140c24",
                 f0="#3a1458", f1="#8a3aa8", f2="#e47ac8", f3="#fff0fa", gold="#ffd878", gold_dark="#8a6420",
                 wing="#9ee8c8", wing_dark="#3a8a74", wing_light="#e4fff2", spot="#ff7ab8", spot_dark="#5a1e4e",
                 silk="#f4eefc", silk_dark="#a898c4"),
    # Malleus, der Glutschmied: geschwärztes Eisen mit Nieten und glühenden Nähten, dahinter Hammer und Speer gekreuzt;
    # Füllung wie fließendes, glühendes Metall mit hellen Rissen
    "forge": dict(m0="#141012", m1="#4a4246", m2="#a89c98", acc="#ff7a1a", glow="#fff0c0", deep="#1c0804",
                  f0="#3e0a02", f1="#a8300a", f2="#ff8a24", f3="#fff2b8", gold="#e6ac48", gold_dark="#7a5218",
                  iron="#2e2c32", iron_light="#7c7880", ember="#ff9a30", ember_dark="#8a2406", wood="#4a2e1e"),
    # Asteria, die Sternenreiterin: poliertes Silber mit Goldsaum, dahinter die Schwingen ihres Sternenrochens und die
    # Sternenlanze; Füllung wie der Nachthimmel mit Nebelschleiern und funkelnden Sternen
    "star": dict(m0="#121628", m1="#5a6488", m2="#dce4f6", acc="#9ad8ff", glow="#f2f8ff", deep="#060a22",
                 f0="#0b1236", f1="#2a3d8a", f2="#7ab0ff", f3="#eef6ff", gold="#e8c25a", gold_dark="#8a6018",
                 night="#141c48", night_dark="#070b22", nebula="#7a3ab4", nebula_light="#c08ae8", silver="#c8d0e4",
                 silver_dark="#5a6488", crystal="#9ad0ff"),
    # MissMystik, die Feen-Zauberkünstlerin: violetter Samt mit Goldsaum, dahinter ein Fächer aus Zauberkarten und ihr
    # Zauberstab; Füllung wie ein Harlekin-Rautenmuster aus Samt, in dem Pailletten funkeln
    "mystic": dict(m0="#140c1e", m1="#4e3470", m2="#dcc8f4", acc="#ff7ac8", glow="#fff0fa", deep="#10061a",
                   f0="#2e0a44", f1="#7a2496", f2="#e46ad0", f3="#fff0fa", gold="#ffd86a", gold_dark="#8a6020",
                   card="#fbf7ff", card_dark="#a898c0", red="#e8344a", black="#1c1428", wand="#121016",
                   confetti="#4ae0e8"),
    # Anni der Troll: dunkles Metall mit Farbsäumen (magenta und cyan), hinter dem Medaillon der Bannhammer mit fehlender
    # Textur, ringsum lose Pixel; Füllung wie ein kaputtes Bild: Zeilen, verrutschte Blöcke, Farbrauschen
    "anni": dict(m0="#121218", m1="#3e3e4c", m2="#b8b8c8", acc="#f800f8", glow="#c8fcff", deep="#0a040e",
                 f0="#3a0040", f1="#8a0a8a", f2="#f040c8", f3="#d8ffff", cyan="#22d8e8", cyan_dark="#0a5a66",
                 red="#ff3040", green="#40ff70", blue="#4080ff", yellow="#ffd400", wood="#5a3a20", wood_dark="#2a1a0c",
                 stone="#8a8a92", stone_dark="#4a4a52", iron="#2e2e36"),
    # Anni der Troll 2.0 (aus der Sicherung wiederhergestellt): derselbe Rahmen, aber Säume, Füllung und Schein in Alarmrot
    "anni2": dict(m0="#160c0e", m1="#4a2e34", m2="#d0b0b8", acc="#ff2040", glow="#ffd0d4", deep="#120406",
                  f0="#3a0008", f1="#8a0a18", f2="#ff3a48", f3="#ffe0e0", cyan="#ff8a30", cyan_dark="#6a2a0a",
                  red="#ff3040", green="#40ff70", blue="#4080ff", yellow="#ffd400", wood="#5a3a20", wood_dark="#2a1a0c",
                  stone="#8a8a92", stone_dark="#4a4a52", iron="#2e2e36"),
    # Schrolli der Schöpfer: Elfenbein und Gold, hinter dem Medaillon zwei Schwingen aus weißen Federn mit goldenen
    # Spitzen, oben ein Kranz aus Strahlen, unten goldene Tropfen, als Spitze der achtstrahlige Zepterstern; Füllung aus
    # strömendem goldenem Licht mit Strahlen und Funkeln
    "schrolli": dict(m0="#3a2808", m1="#a8802a", m2="#fff4cc", acc="#ffc83a", glow="#fff8dc", deep="#2a1a06",
                     f0="#7a4a06", f1="#e0a01c", f2="#ffe070", f3="#ffffff", gold="#ffd24a", gold_dark="#8a5e14",
                     feather="#fbf8f0", feather_mid="#d8d4e4", feather_dark="#8c88a4", tip="#f2c44a", gem_blue="#3a94e8",
                     gem_light="#c8ecff", ray="#fff2b0"),
    # Schrolli der Schöpfer – Wahre Gestalt: dasselbe in den Farben des Weltalls – violettes Metall mit Goldsaum, Schwingen
    # wie ein Sternenhimmel mit schillernden Spitzen, ringsum Augen und Sterne; Füllung aus Nebeln und Sternen
    "schrolli_true": dict(m0="#140a2a", m1="#5a3c9a", m2="#f0e2ff", acc="#c080ff", glow="#f8f0ff", deep="#0a0418",
                          f0="#1a0838", f1="#5a2a9a", f2="#c870f0", f3="#ffffff", gold="#ffd86a", gold_dark="#8a6020",
                          feather="#4a2a92", feather_mid="#22125e", feather_dark="#0a0630", tip="#a0f0ff", gem_blue="#ffe07a",
                          gem_light="#fff8dc", ray="#e8d0ff", prism1="#ffa0a8", prism2="#e890ff", prism3="#80e0ff"),
    # Schrolli der Allmächtige (dritte Gestalt): Weißgold mit Glutsaum; hinter dem Medaillon die Schwingen aus weißen Federn,
    # deren Spitzen in Flammen stehen, auf dem Rahmen eine Sonnenkrone aus Flammenzungen und Strahlen, darunter tropft
    # flüssiges Gold, als Spitze die Sonne; auf dem Ring des Medaillons sieben Sonnensteine; Füllung wie das Plasma einer
    # Sonne (Körnung, Bögen, Funken)
    "schrolli_almighty": dict(m0="#3a1406", m1="#b8601c", m2="#fff0c4", acc="#ff7a1a", glow="#fff2c8", deep="#2c0a02",
                              f0="#a82206", f1="#f8600e", f2="#ffc040", f3="#fffbea", gold="#ffd24a", gold_dark="#8a4a10",
                              feather="#fffaf0", feather_mid="#e8d4b4", feather_dark="#9a7656", ray="#fff2b0",
                              flame0="#5a0a04", flame1="#b82408", flame2="#f0600e", flame3="#ffa024", flame4="#ffd65a",
                              flame5="#fff8d8", sun_dark="#a8280a", sun="#ff8a1e", sun_light="#ffe27a"),
    # der Weltenfresser: Chaosgestein in Platten mit glühenden Fugen; auf dem Rahmen Stücke verschlungener Welten (Wiese mit
    # Baum, Netherrack mit Magma, Endstein mit Chorus, Eis mit Zacken), darunter sein Gebiss, hinter dem Medaillon drei
    # Kronensplitter und ein Hauer, als Spitze die Kristallflosse seines Schwanzes; Füllung wie die Glut seines Schlunds,
    # durch die violette Risse ziehen
    "world_eater": dict(m0="#140f13", m1="#54444e", m2="#ab97a2", acc="#ff5a14", glow="#ffe0a0", deep="#220604",
                        f0="#2e0204", f1="#a01c08", f2="#ff7418", f3="#fff0c0",
                        seam0="#5a0a04", seam1="#e8541a", seam2="#ffd88a",
                        crystal0="#2a1052", crystal1="#6a3cc0", crystal2="#a47cf0", crystal3="#f0e6ff",
                        gold="#e8b84a", gold_dark="#7a5218", tooth="#ddd0b4", tooth_light="#fbf4e4", tooth_dark="#8a7a64",
                        gum="#7a1418", gum_dark="#3a0608",
                        grass="#5a8a32", grass_light="#94c45c", dirt="#6a482e", dirt_dark="#3e2818", wood="#6a4a2c",
                        wood_dark="#3a2614", leaves="#2e5a1e", leaves_light="#5c9a3e", netherrack="#7e2222",
                        netherrack_dark="#481010", magma="#ff8a2a", magma_light="#ffe08a", fungus="#d8342a",
                        fungus_light="#ff7a5a", endstone="#e0daa4", endstone_dark="#a49e66", chorus="#7a4a86",
                        chorus_light="#d0a8d8", ice="#a8ccf4", ice_dark="#5a86c4", ice_light="#f0f8ff"),
}

OUTLINE = (12, 10, 12, 255)


def c(pal: dict, key: str):
    return hexc(pal[key])


def put(px, img, x, y, col):
    if 0 <= x < img.width and 0 <= y < img.height:
        px[x, y] = col


def hash01(*a) -> float:
    h = 2166136261
    for v in a:
        h = ((h ^ (int(v) & 0xFFFFFFFF)) * 16777619) & 0xFFFFFFFF
    h ^= h >> 13
    h = (h * 1274126177) & 0xFFFFFFFF
    return (h & 0xFFFF) / 0xFFFF


# =============================================================================================
# Rahmen
# =============================================================================================

def frame_border(img: Image.Image, pal: dict) -> None:
    """Metallrahmen um die Leiste: dunkle Außenlinie, Fase (oben/links hell, unten/rechts dunkel), Innenschatten."""
    px = img.load()
    m0, m1, m2 = c(pal, "m0"), c(pal, "m1"), c(pal, "m2")
    x0, x1, y0, y1 = BAR_X - 3, BAR_X + BAR_W + 2, BAR_Y - 3, BAR_Y + BAR_H + 2
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if BAR_X <= x < BAR_X + BAR_W and BAR_Y <= y < BAR_Y + BAR_H:
                continue
            ring = min(x - x0, x1 - x, y - y0, y1 - y)
            if ring == 0:
                col = OUTLINE
            elif ring == 1:
                col = mix(m2, m1, 0.15) if (y - y0 <= 1 or x - x0 <= 1) and not (y1 - y <= 1) else mix(m1, m0, 0.35)
            else:
                col = mix(m0, OUTLINE, 0.3)
            px[x, y] = col
    # Nieten oben und unten
    for x in range(x0 + 9, x1 - 4, 22):
        if any(abs(x - (BAR_X + round(BAR_W * f))) < 7 for f in GEMS):
            continue
        for y in (y0 + 1, y1 - 1):
            put(px, img, x, y, m2)
            put(px, img, x + 1, y, mix(m1, m0, 0.4))
    # Fassungen der Edelsteine (oberhalb des Rahmens)
    for f in GEMS:
        gx = BAR_X + round(BAR_W * f)
        for dy in range(-6, 1):
            half = 5 - abs(dy + 3) + 1
            for dx in range(-half, half + 1):
                x, y = gx + dx, y0 + dy
                edge = abs(dx) == half or dy in (-6, 0)
                put(px, img, x, y, OUTLINE if edge else (mix(m1, m0, 0.2) if dx < 0 else mix(m0, OUTLINE, 0.2)))


def emblem(img: Image.Image, pal: dict) -> None:
    """Medaillon: Ring aus Metall (Licht von oben links), dunkler Grund mit Schimmer in der Farbe des Elements."""
    px = img.load()
    m0, m1, m2 = c(pal, "m0"), c(pal, "m1"), c(pal, "m2")
    deep, acc = c(pal, "deep"), c(pal, "acc")
    # Brücke zum Rahmen
    for y in range(BAR_Y - 1, BAR_Y + BAR_H + 1):
        for x in range(int(EMB_X + EMB_R) - 2, BAR_X - 2):
            edge = y in (BAR_Y - 1, BAR_Y + BAR_H)
            px[x, y] = OUTLINE if edge else mix(m1, m0, 0.3 + 0.4 * (y - BAR_Y) / BAR_H)
    for y in range(FRAME_H):
        for x in range(0, int(EMB_X + EMB_R) + 2):
            d = math.hypot(x - EMB_X + 0.5, y - EMB_Y + 0.5)
            if d > EMB_R:
                continue
            ang = math.atan2(y - EMB_Y, x - EMB_X)
            if d > EMB_R - 1.0:
                px[x, y] = OUTLINE
            elif d > EMB_R - 4.0:
                lit = 0.5 + 0.5 * math.cos(ang + 2.35)          # oben links hell
                col = mix(m0, m2, 0.15 + 0.7 * lit)
                if d > EMB_R - 1.8:
                    col = mix(col, m0, 0.35)
                px[x, y] = col
            elif d > EMB_R - 5.0:
                px[x, y] = OUTLINE
            else:
                t = d / (EMB_R - 5.0)
                px[x, y] = mix(mix(acc, deep, 0.25), mix(deep, OUTLINE, 0.2), t ** 1.3)


def gem(pal: dict, broken: bool) -> Image.Image:
    img = Image.new("RGBA", (9, 9), CLEAR)
    px = img.load()
    acc, glow = c(pal, "acc"), c(pal, "glow")
    for y in range(9):
        for x in range(9):
            d = abs(x - 4) + abs(y - 4)
            if d > 4:
                continue
            if d == 4:
                px[x, y] = OUTLINE
                continue
            if broken:
                col = (58, 54, 60, 255) if (x + y) % 3 else (40, 38, 44, 255)
                if (x == 3 and y in (2, 3)) or (x == 4 and y in (4, 5)) or (x == 5 and y == 6):
                    col = (20, 18, 22, 255)
            else:
                t = (x + y) / 8
                col = mix(glow, acc, min(1.0, t * 1.2))
                if (x, y) in ((3, 2), (2, 3), (3, 3)):
                    col = (255, 255, 255, 255)
            px[x, y] = col
    return img


# =============================================================================================
# Verzierungen je Element
# =============================================================================================

def deco_fire(img: Image.Image, pal: dict) -> None:
    """Flammenzungen oben auf dem Rahmen (am Medaillon und am Ende höher), Glutfunken darunter, Flamme als Spitze."""
    px = img.load()
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    top = BAR_Y - 3

    def flame(cx: int, base: int, h: int, w: int):
        for k in range(h):
            t = k / max(1, h - 1)
            half = max(0, round((w / 2) * (1 - t) ** 0.7 * (0.9 + 0.2 * math.sin(k * 1.7 + cx))))
            sway = round(math.sin(k * 0.8 + cx * 0.3) * t * 1.2)
            for dx in range(-half, half + 1):
                inner = abs(dx) < half
                col = f1 if t < 0.35 else (f2 if t < 0.7 else f3)
                if not inner:
                    col = mix(f0, f1, 0.4) if t < 0.5 else f1
                elif abs(dx) <= max(0, half - 2) and t < 0.6:
                    col = f2 if t < 0.3 else f3
                put(px, img, cx + dx + sway, base - k, col)
    for x in range(BAR_X - 2, BAR_X + BAR_W + 2, 5):
        rel = (x - BAR_X) / BAR_W
        hgt = 3 + round(4 * (abs(rel - 0.5) * 2) ** 2 + 2 * hash01(x, 7))
        flame(x, top - 1, hgt, 4)
    # Krone aus Flammen auf dem Medaillon
    for k, ang in enumerate(range(-150, -20, 26)):
        a = math.radians(ang)
        x = round(EMB_X + math.cos(a) * (EMB_R - 1))
        y = round(EMB_Y + math.sin(a) * (EMB_R - 1))
        flame(x, y, 4 + (k % 2) * 2, 4)
    # Glutfunken unterhalb
    for x in range(BAR_X + 4, BAR_X + BAR_W, 9):
        if hash01(x, 3) > 0.55:
            put(px, img, x, BAR_Y + BAR_H + 4 + round(hash01(x, 5) * 4), f2 if hash01(x, 9) > 0.5 else f1)
    # Spitze: liegende Flamme nach rechts
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(10):
        half = round(5 * (1 - k / 10) ** 0.8)
        for dy in range(-half, half + 1):
            col = OUTLINE if abs(dy) == half else (f1 if abs(dy) > half - 2 else (f2 if k < 6 else f3))
            put(px, img, ex + k, ey + dy + round(math.sin(k * 0.9) * k / 8), col)


def deco_ice(img: Image.Image, pal: dict) -> None:
    """Eiskristalle oben, Eiszapfen unten, Kristallspitzen am Medaillon, Kristall als Spitze."""
    px = img.load()
    f1, f2, f3 = c(pal, "f1"), c(pal, "f2"), c(pal, "f3")
    m0 = c(pal, "m0")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def crystal(cx: int, base: int, h: int, w: int, up: bool = True):
        for k in range(h):
            half = max(0, round((w / 2) * (1 - k / h)))
            for dx in range(-half, half + 1):
                if abs(dx) == half:
                    col = mix(f1, m0, 0.3)
                else:
                    col = f3 if dx < 0 else (f2 if dx == 0 else f1)
                put(px, img, cx + dx, base - k if up else base + k, col)
    for x in range(BAR_X + 3, BAR_X + BAR_W, 11):
        h = 4 + round(5 * hash01(x, 2))
        crystal(x, top - 1, h, 4)
        if hash01(x, 4) > 0.4:
            crystal(x + 3, top - 1, max(2, h - 3), 2)
    for x in range(BAR_X + 6, BAR_X + BAR_W - 2, 7):
        crystal(x, bottom + 1, 3 + round(6 * hash01(x, 6)), 2, up=False)
    for ang in (-90, -135, -45, 180):
        a = math.radians(ang)
        crystal(round(EMB_X + math.cos(a) * (EMB_R - 2)), round(EMB_Y + math.sin(a) * (EMB_R - 2)), 6, 4,
                up=ang != 180)
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(10):
        half = round(4 * (1 - abs(k - 3) / 7)) if k > 3 else k + 1
        for dy in range(-half, half + 1):
            col = OUTLINE if abs(dy) == half else (f3 if dy < 0 else f1)
            put(px, img, ex + k, ey + dy, col)


def deco_lightning(img: Image.Image, pal: dict) -> None:
    """Goldene Zickzack-Einlage im Rahmen, kleine Blitze oben, Funken, Blitz als Spitze."""
    px = img.load()
    f2, f3, acc = c(pal, "f2"), c(pal, "f3"), c(pal, "acc")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2
    for x in range(BAR_X - 2, BAR_X + BAR_W + 2):
        zig = (x // 3) % 2
        put(px, img, x, top + 1 if zig else top + 2, acc)
        put(px, img, x, bottom - 1 if not zig else bottom - 2, mix(acc, (0, 0, 0, 255), 0.35))

    def bolt(x: int, y: int, length: int, seed: int):
        cx = x
        for k in range(length):
            if k % 3 == 2:
                cx += 1 if hash01(seed, k) > 0.5 else -1
            put(px, img, cx, y - k, f3 if k % 2 else f2)
            put(px, img, cx + 1, y - k, OUTLINE)
    for i, x in enumerate(range(BAR_X + 10, BAR_X + BAR_W, 26)):
        bolt(x, top - 1, 4 + (i % 3) * 2, x)
    for x in range(BAR_X, BAR_X + BAR_W, 6):
        if hash01(x, 11) > 0.7:
            put(px, img, x, top - 3 - round(hash01(x, 12) * 5), f3)
        if hash01(x, 13) > 0.75:
            put(px, img, x, bottom + 2 + round(hash01(x, 14) * 4), f2)
    for ang in range(-160, 170, 40):
        a = math.radians(ang)
        x = round(EMB_X + math.cos(a) * (EMB_R - 2.5))
        y = round(EMB_Y + math.sin(a) * (EMB_R - 2.5))
        put(px, img, x, y, f3)
    # Blitz nach rechts
    ex, ey = BAR_X + BAR_W + 3, BAR_Y - 1
    shape = ["xx.......", ".xxx.....", "..xxxx...", "...xxxxx.", "....xxx..", "...xxxx..", "....xxxxx", ".....xxx.",
             "......xx.", ".......x."]
    for dy, row in enumerate(shape):
        for dx, ch in enumerate(row):
            if ch == "x":
                put(px, img, ex + dx, ey + dy, f3 if dx < 4 else f2)
    for dy, row in enumerate(shape):
        for dx, ch in enumerate(row):
            if ch == "x":
                for ox, oy in ((1, 0), (0, 1), (-1, 0), (0, -1)):
                    nx, ny = dx + ox, dy + oy
                    inside = 0 <= ny < len(shape) and 0 <= nx < len(shape[ny]) and shape[ny][nx] == "x"
                    tx, ty = ex + nx, ey + ny
                    if not inside and 0 <= tx < img.width and 0 <= ty < img.height and px[tx, ty][3] == 0:
                        put(px, img, tx, ty, OUTLINE)


def deco_nature(img: Image.Image, pal: dict) -> None:
    """Ranken winden sich um den Rahmen, mit Blättern und ein paar Blüten; ein Blatt als Spitze."""
    px = img.load()
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    blossom = hexc("#f07ab0")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def leaf(x: int, y: int, d: int):
        cells = [(0, 0), (d, 0), (d, -1), (2 * d, -1), (d, 1), (0, -1)]
        for i, (dx, dy) in enumerate(cells):
            put(px, img, x + dx, y + dy, f2 if i < 3 else f1)
        put(px, img, x + 2 * d, y - 1, f3)
    for x in range(BAR_X - 3, BAR_X + BAR_W + 3):
        yt = top - 1 + round(1.6 * math.sin(x * 0.21))
        yb = bottom + 1 + round(1.6 * math.sin(x * 0.21 + 2.0))
        put(px, img, x, yt, f0)
        put(px, img, x, yt - 1, f1)
        put(px, img, x, yb, f0)
        put(px, img, x, yb + 1, mix(f0, f1, 0.4))
        if x % 9 == 0:
            leaf(x, yt - 2, 1 if (x // 9) % 2 else -1)
        if x % 11 == 5:
            leaf(x, yb + 2, -1 if (x // 11) % 2 else 1)
        if x % 31 == 13:
            put(px, img, x, yt - 3, blossom)
            put(px, img, x + 1, yt - 3, hexc("#ffd0e4"))
    for ang in range(-170, 180, 34):
        a = math.radians(ang)
        leaf(round(EMB_X + math.cos(a) * (EMB_R + 0.5)), round(EMB_Y + math.sin(a) * (EMB_R + 0.5)), 1 if ang < 0 else -1)
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(10):
        half = round(4.5 * math.sin(math.pi * (k + 0.5) / 10))
        for dy in range(-half, half + 1):
            col = OUTLINE if abs(dy) == half else (f3 if dy == 0 else (f2 if dy < 0 else f1))
            put(px, img, ex + k, ey + dy, col)


def deco_earth(img: Image.Image, pal: dict) -> None:
    """Zackige Felsen auf dem Rahmen, Bernsteinkristalle, Geröll darunter, Felsspitze am Ende."""
    px = img.load()
    m0, m1, m2 = c(pal, "m0"), c(pal, "m1"), c(pal, "m2")
    f2, f3 = c(pal, "f2"), c(pal, "f3")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2
    x = BAR_X - 3
    while x < BAR_X + BAR_W + 2:
        w = 5 + round(5 * hash01(x, 1))
        h = 2 + round(4 * hash01(x, 2))
        for dx in range(w):
            t = dx / max(1, w - 1)
            colh = round(h * (1 - abs(t - 0.4) * 1.4))
            for k in range(max(1, colh)):
                col = m2 if dx < w * 0.4 else (m1 if dx < w * 0.8 else mix(m1, m0, 0.5))
                if k == colh - 1:
                    col = mix(col, m2, 0.5)
                put(px, img, x + dx, top - 1 - k, col)
        x += w
    for i, cx in enumerate((BAR_X + 30, BAR_X + 85, BAR_X + 140)):
        for j, (dx, h) in enumerate(((0, 6), (2, 4), (-2, 3))):
            for k in range(h):
                put(px, img, cx + dx, top - 2 - k, f3 if k > h - 3 else f2)
                put(px, img, cx + dx + 1, top - 2 - k, mix(f2, m0, 0.4))
    for gx in range(BAR_X + 3, BAR_X + BAR_W, 7):
        if hash01(gx, 5) > 0.5:
            put(px, img, gx, bottom + 1, m1)
            put(px, img, gx + 1, bottom + 1, m0)
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(9):
        half = round(5 * (1 - k / 9))
        for dy in range(-half, half + 1):
            col = OUTLINE if abs(dy) == half else (m2 if dy < 0 else m1)
            put(px, img, ex + k, ey + dy, col)


def deco_water(img: Image.Image, pal: dict) -> None:
    """Wellenkämme mit Gischt oben, Luftblasen unten, Flosse als Spitze."""
    px = img.load()
    f1, f2, f3 = c(pal, "f1"), c(pal, "f2"), c(pal, "f3")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2
    period = 14
    for x in range(BAR_X - 3, BAR_X + BAR_W + 3):
        u = ((x - BAR_X) % period) / period
        crest = round(4 * math.sin(math.pi * u) ** 3)
        for k in range(crest + 1):
            col = f1 if k < crest - 1 else f2
            put(px, img, x, top - 1 - k, col)
        if crest >= 3:
            put(px, img, x, top - 2 - crest, f3)
        if 0.6 < u < 0.75:
            put(px, img, x, top - 1 - crest, f3)
    for x in range(BAR_X + 5, BAR_X + BAR_W, 13):
        y = bottom + 3 + round(hash01(x, 2) * 4)
        r = 1 if hash01(x, 3) > 0.5 else 0
        for dx in range(-r, r + 1):
            for dy in range(-r, r + 1):
                put(px, img, x + dx, y + dy, f2 if (dx, dy) != (-r, -r) else f3)
        put(px, img, x - r, y - r, f3)
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(10):
        up = round(6 * (k / 9) ** 1.4)
        down = round(4 * (k / 9) ** 1.4)
        for dy in range(-up, down + 1):
            if k < 9 and abs(dy) < 2 + k // 3:
                col = OUTLINE if dy in (-up, down) else (f3 if dy < 0 else f1)
                put(px, img, ex + k, ey + dy, col)


def deco_shadow(img: Image.Image, pal: dict) -> None:
    """Dornige Nachtkristalle oben (zum Medaillon hin höher), Seelenfunken darunter, eine Krone aus Kristallen am
    Medaillon und ein wachendes Auge als Spitze."""
    px = img.load()
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    m0 = c(pal, "m0")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def shard(cx: int, base: int, h: int, w: int, lean: float, up: bool = True):
        for k in range(h):
            t = k / max(1, h - 1)
            half = max(0, round((w / 2) * (1 - t)))
            off = round(lean * k)
            for dx in range(-half, half + 1):
                if abs(dx) == half:
                    col = mix(f0, m0, 0.35)
                else:
                    col = f3 if (dx < 0 and t > 0.4) else (f2 if dx <= 0 else f1)
                put(px, img, cx + dx + off, base - k if up else base + k, col)
    for x in range(BAR_X - 1, BAR_X + BAR_W + 1, 6):
        rel = (x - BAR_X) / BAR_W
        h = 3 + round(5 * (1 - rel) ** 2 + 3 * hash01(x, 11))
        shard(x, top - 1, h, 3 if h < 6 else 4, (hash01(x, 13) - 0.5) * 0.5)
    # Seelenfunken unter der Leiste, wie aufsteigende Irrlichter
    for x in range(BAR_X + 3, BAR_X + BAR_W, 8):
        if hash01(x, 17) > 0.45:
            y = bottom + 2 + round(hash01(x, 19) * 5)
            put(px, img, x, y, f3)
            put(px, img, x, y + 1, f2)
            put(px, img, x + (1 if hash01(x, 23) > 0.5 else -1), y + 2, f1)
    # Kristallkrone am Medaillon
    for k, ang in enumerate(range(-160, -10, 22)):
        a = math.radians(ang)
        x = round(EMB_X + math.cos(a) * (EMB_R - 1))
        y = round(EMB_Y + math.sin(a) * (EMB_R - 1))
        shard(x, y, 5 + (k % 2) * 3, 3, math.cos(a) * 0.5)
    # Spitze: ein wachendes Auge (Mandelform, leuchtende Pupille)
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(11):
        half = round(4.2 * math.sin(math.pi * k / 10))
        for dy in range(-half, half + 1):
            if abs(dy) == half:
                col = OUTLINE
            elif abs(k - 5) <= 1 and abs(dy) <= 1:
                col = f3 if (k, dy) == (5, 0) else f2
            else:
                col = mix(m0, f0, 0.6)
            put(px, img, ex + k, ey + dy, col)


def deco_void(img: Image.Image, pal: dict) -> None:
    """Schwebende Bruchstücke aus Sternenmarmor über der Leiste (am Medaillon dichter), dazwischen Sterne; darunter
    Sternenstaub; am Medaillon die Krone aus sieben Kristallsplittern des Königs; als Spitze sein zersprungenes Herz."""
    px = img.load()
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    m0, m1, m2 = c(pal, "m0"), c(pal, "m1"), c(pal, "m2")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def chunk(cx: int, cy: int, w: int, h: int, seed: int):
        """Ein kleines Bruchstück: unregelmäßiger Umriss, oben hell, eine goldene Kante."""
        for dy in range(h):
            for dx in range(w):
                corner = (dx in (0, w - 1)) and (dy in (0, h - 1)) and hash01(seed, dx, dy) > 0.35
                if corner:
                    continue
                edge = dx in (0, w - 1) or dy in (0, h - 1)
                if edge:
                    col = OUTLINE
                elif dy == 1:
                    col = m2
                elif dy == h - 2 and hash01(seed, 3) > 0.4:
                    col = gold
                else:
                    col = mix(m1, m2, 0.35 + 0.3 * hash01(seed, dx, dy))
                put(px, img, cx + dx, cy + dy, col)

    def star(x: int, y: int, big: bool):
        put(px, img, x, y, f3)
        if big:
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                put(px, img, x + dx, y + dy, f2)

    # ein zerbrochener Fries: am Medaillon liegen die Stücke noch auf dem Rahmen, nach rechts lösen sie sich, steigen
    # und werden kleiner, bis nur noch Sterne treiben
    x = BAR_X + 1
    while x < BAR_X + BAR_W - 6:
        rel = (x - BAR_X) / BAR_W
        w = max(3, round(7 - 4 * rel + hash01(x, 31)))
        h = 4 if rel < 0.55 else 3
        lift = round(rel * 8 + (hash01(x, 41) - 0.5) * 2.0 * min(1.0, rel * 3))
        near_gem = any(abs(x + w // 2 - (BAR_X + round(BAR_W * f))) < 8 for f in GEMS)
        if not near_gem and rel < 0.9:
            chunk(x, top - lift - h + 1, w, h, x)
            if hash01(x, 89) > 0.55 and w >= 4:
                put(px, img, x + 1 + round(hash01(x, 97) * (w - 3)), top - lift - h + 3, f2)   # leuchtender Riss
        gap = 2 + round(rel * 6 + hash01(x, 59) * 2)
        if rel > 0.25 and hash01(x, 43) > 0.4:
            star(x + w + gap // 2, top - lift - 3 - round(hash01(x, 47) * 5), hash01(x, 53) > 0.75)
        x += w + gap
    # Sternenstaub unter der Leiste
    for x in range(BAR_X + 2, BAR_X + BAR_W, 6):
        if hash01(x, 61) > 0.6:
            y = bottom + 2 + round(hash01(x, 67) * 4)
            put(px, img, x, y, f3 if hash01(x, 71) > 0.5 else f2)
            if hash01(x, 73) > 0.8:
                put(px, img, x + 1, y + 1, f1)
    # die Krone aus sieben Kristallsplittern über dem Medaillon (die mittlere am höchsten)

    def shard(cx: int, base: int, h: int, w: int, lean: float):
        for k2 in range(h):
            t = k2 / max(1, h - 1)
            half = max(0, round((w / 2) * (1 - t)))
            off = round(lean * k2)
            for dx in range(-half, half + 1):
                if abs(dx) == half:
                    col = mix(f0, m0, 0.35)
                else:
                    col = f3 if (dx < 0 and t > 0.35) else (f2 if dx <= 0 else f1)
                put(px, img, cx + dx + off, base - k2, col)
    angles = [-90 + (i - 3) * 22 for i in range(7)]
    for i, ang in enumerate(angles):
        a = math.radians(ang)
        x = round(EMB_X + math.cos(a) * (EMB_R - 1))
        y = round(EMB_Y + math.sin(a) * (EMB_R - 1))
        h = 10 if i == 3 else (8 if i in (2, 4) else 6)
        shard(x, y, h, 3 if i != 3 else 4, math.cos(a) * 0.55)
    # goldener Reif um das Medaillon (oben)
    for ang in range(-170, -9, 4):
        a = math.radians(ang)
        put(px, img, round(EMB_X + math.cos(a) * (EMB_R + 0.2)), round(EMB_Y + math.sin(a) * (EMB_R + 0.2)),
            gold if ang < -90 else gold_dark)
    # Spitze: das zersprungene Herz – zwei Kristallhälften, dazwischen ein leuchtender Spalt
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for kx in range(12):
        half = round(4.6 * math.sin(math.pi * (kx + 0.5) / 12) + (1.2 if kx in (2, 3, 8, 9) else 0.0))
        for dy in range(-half, half + 1):
            gap = abs(kx - 6 + (1 if dy > 0 else 0) * (dy % 2)) <= 0
            if abs(dy) == half:
                col = OUTLINE
            elif gap:
                col = f3
            else:
                t = (dy + half) / max(1, 2 * half)
                col = mix(f2, f0, t) if kx < 6 else mix(f1, f0, t * 0.8)
                if kx in (2, 3) and dy == -half + 1:
                    col = f3
            put(px, img, ex + kx, ey + dy, col)


def deco_reef(img: Image.Image, pal: dict) -> None:
    """Ein Korallenriff wächst auf dem Rahmen (am Medaillon hoch und dicht, nach rechts niedriger, am Ende nur noch
    Muscheln und ein Seestern): Geweihkorallen, Gorgonienfächer, Tischkorallen, Hirnkorallen, Röhrenschwämme, Seegras
    und Anemonen, dazwischen zwei kleine Fische. Unter der Leiste hängen Seepocken und Rotalgen, Blasen steigen auf.
    Um das Medaillon ein Kranz aus Korallenästen mit Goldreif, als Spitze die geöffnete Schere des Karkinos."""
    px = img.load()
    f1, f2, f3 = (c(pal, k) for k in ("f1", "f2", "f3"))
    m0, m1, m2 = c(pal, "m0"), c(pal, "m1"), c(pal, "m2")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    coral, coral_dark, coral_light = c(pal, "coral"), c(pal, "coral_dark"), c(pal, "coral_light")
    fan, fan_dark = c(pal, "fan"), c(pal, "fan_dark")
    sponge, sponge_dark = c(pal, "sponge"), c(pal, "sponge_dark")
    brain, brain_dark = c(pal, "brain"), c(pal, "brain_dark")
    anemone, anemone_dark = c(pal, "anemone"), c(pal, "anemone_dark")
    star_col = c(pal, "star")
    white = (255, 250, 236, 255)
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2
    base = top - 1                                   # unterste Zeile über dem Rahmen

    def line(x0: float, y0: float, x1: float, y1: float, col0, col1, width: int = 1):
        n = max(1, round(max(abs(x1 - x0), abs(y1 - y0))))
        for k in range(n + 1):
            t = k / n
            x, y = round(x0 + (x1 - x0) * t), round(y0 + (y1 - y0) * t)
            col = mix(col0, col1, t)
            put(px, img, x, y, col)
            if width > 1:
                put(px, img, x + 1, y, mix(col, OUTLINE, 0.35))

    def staghorn(x: float, y: float, ang: float, length: float, depth: int, seed: int, palette=None):
        """Geweihkoralle: dicker Stamm, Äste verzweigen sich, an den Spitzen helle Polypen."""
        dark, mid, light = palette or (coral_dark, coral, coral_light)
        x1, y1 = x + math.cos(ang) * length, y + math.sin(ang) * length
        line(x, y, x1, y1, mix(dark, mid, 0.4) if depth >= 2 else mid, mid if depth else light, 2 if depth >= 2 else 1)
        if depth == 0:
            put(px, img, round(x1), round(y1), light)
            put(px, img, round(x1), round(y1) - 1 if math.sin(ang) < 0 else round(y1) + 1, white)
            return
        spread = 0.5 + 0.25 * hash01(seed, 1)
        staghorn(x1, y1, ang - spread, length * 0.7, depth - 1, seed * 3 + 1, palette)
        staghorn(x1, y1, ang + spread * 0.9, length * 0.62, depth - 1, seed * 3 + 2, palette)
        if depth >= 2 and hash01(seed, 4) > 0.4:
            staghorn((x + x1) / 2, (y + y1) / 2, ang + (spread + 0.3) * (1 if hash01(seed, 5) > 0.5 else -1),
                     length * 0.45, 0, seed * 3 + 3, palette)

    def gorgonian(cx: int, r: int, seed: int):
        """Gorgonienfächer, von vorn gesehen: fünf feine Adern strahlen vom Fuß aus und verzweigen sich, am Rand
        hellere Polypen – dazwischen bleibt der Fächer offen."""
        for k in range(5):
            a = math.radians(-150 + k * 30 + (hash01(seed, k) - 0.5) * 10)
            mx, my = cx + math.cos(a) * r * 0.55, base + math.sin(a) * r * 0.5
            line(cx, base, mx, my, fan_dark, mix(fan_dark, fan, 0.6))
            for side in (-0.28, 0.28):
                ex, ey = cx + math.cos(a + side) * r, base + math.sin(a + side) * r * 0.92
                line(mx, my, ex, ey, mix(fan_dark, fan, 0.6), fan)
                put(px, img, round(ex), round(ey), mix(fan, white, 0.5))
        put(px, img, cx, base, fan_dark)

    def table_coral(cx: int, h: int, w: int, seed: int):
        """Tischkoralle: dünner Fuß, oben eine flache, gewellte Platte."""
        for k in range(h):
            put(px, img, cx, base - k, mix(f1, OUTLINE, 0.35))
        for dx in range(-w, w + 1):
            y = base - h - (1 if abs(dx) < w - 1 and hash01(seed, dx) > 0.7 else 0)
            put(px, img, cx + dx, y, mix(f2, white, 0.25) if dx < 0 else f2)
            put(px, img, cx + dx, y + 1, mix(f1, OUTLINE, 0.25) if abs(dx) < w else OUTLINE)
        put(px, img, cx - w, base - h - 1, mix(f2, white, 0.5))

    def brain_coral(cx: int, w: int, h: int, seed: int):
        """Hirnkoralle: Kuppel mit gewundenen Furchen, oben links im Licht."""
        for dy in range(h + 1):
            for dx in range(-w, w + 1):
                e = (dx / (w + 0.5)) ** 2 + (dy / (h + 0.4)) ** 2
                if e > 1.0:
                    continue
                if e > 0.78:
                    col = mix(brain_dark, OUTLINE, 0.25)
                elif (dx + round(1.6 * math.sin(dy * 1.9 + seed))) % 3 == 0:
                    col = brain_dark
                else:
                    col = mix(brain, white, 0.35) if (dx < 0 and dy > h // 2) else brain
                put(px, img, cx + dx, base - dy, col)

    def sponge_tubes(cx: int, heights):
        """Röhrenschwämme: je zwei Pixel breit, oben eine dunkle Öffnung."""
        for i, h in enumerate(heights):
            x = cx + i * 3
            for k in range(h):
                t = k / max(1, h - 1)
                put(px, img, x, base - k, mix(sponge, (255, 236, 170, 255), 0.4 * t))
                put(px, img, x + 1, base - k, sponge_dark if k < h - 1 else sponge)
            put(px, img, x, base - h, mix(sponge, white, 0.4))
            put(px, img, x + 1, base - h, sponge)
            put(px, img, x, base - h + 1, OUTLINE)
            put(px, img, x + 1, base - h + 1, mix(sponge_dark, OUTLINE, 0.5))

    def anemone_tuft(cx: int, h: int, seed: int, pink: bool = False):
        """Anemone: kurzer Fuß, darüber ein Büschel geschwungener Tentakel mit leuchtenden Spitzen."""
        light = (255, 150, 200, 255) if pink else anemone
        dark = (120, 30, 80, 255) if pink else anemone_dark
        put(px, img, cx, base, dark)
        put(px, img, cx + 1, base, dark)
        put(px, img, cx, base - 1, mix(dark, light, 0.4))
        put(px, img, cx + 1, base - 1, mix(dark, light, 0.4))
        for i, lean in enumerate((-1.4, -0.8, -0.25, 0.25, 0.8, 1.4)):
            x = cx + 0.5 + lean * 0.5
            hh = h - abs(round(lean))
            for k in range(hh):
                x += lean * (0.18 + 0.12 * k) + 0.3 * math.sin(seed + k * 1.3 + i)
                put(px, img, round(x), base - 2 - k, mix(dark, light, 0.3 + 0.7 * k / max(1, hh - 1)))
            put(px, img, round(x), base - 2 - hh, f3)

    def seagrass(cx: int, h: int, seed: int):
        """Seegras: zwei, drei schmale Halme, die in der Strömung schwanken."""
        for b in range(2 + (1 if hash01(seed, 9) > 0.5 else 0)):
            x = cx + b * 2
            for k in range(h - b * 2):
                sway = round(1.2 * math.sin(k * 0.7 + seed + b))
                col = mix((40, 96, 40, 255), (150, 210, 90, 255), k / max(1, h - 1))
                put(px, img, x + sway, base - k, col)

    def scallop(cx: int):
        for dy in range(3):
            half = 2 if dy < 2 else 1
            for dx in range(-half, half + 1):
                col = mix(m2, coral_light, 0.4) if dx % 2 == 0 else mix(m1, coral, 0.35)
                put(px, img, cx + dx, base - dy, col)
        put(px, img, cx - 2, base - 2, OUTLINE if False else mix(m2, coral_light, 0.2))

    def starfish(cx: int, cy: int):
        for k in range(5):
            a = -math.pi / 2 + k * math.tau / 5
            for r in range(1, 3):
                put(px, img, round(cx + math.cos(a) * r), round(cy + math.sin(a) * r), star_col if r < 2 else coral)
        put(px, img, cx, cy, mix(star_col, (255, 230, 160, 255), 0.5))

    def fish(x: int, y: int, body, stripe, facing: int):
        """Ein kleiner Rifffisch (4 x 3 Pixel) mit Schwanzflosse."""
        for dx in range(4):
            for dy in (-1, 0, 1):
                if abs(dy) == 1 and dx in (0, 3):
                    continue
                col = stripe if dx == 2 else body
                put(px, img, x + dx * facing, y + dy, col)
        put(px, img, x + (3 if facing > 0 else -3), y - 1 if False else y, OUTLINE)        # Auge vorn
        put(px, img, x - facing, y - 1, body)
        put(px, img, x - facing, y + 1, body)

    # Fuß des Riffs: Geröll aus Kalk und Korallenbruch auf dem Rahmen (Lücken zwischen den Gruppen)
    def rubble(x0: int, x1: int):
        for x in range(x0, x1):
            col = mix(m1, m2, 0.25 + 0.5 * hash01(x, 3)) if hash01(x, 2) > 0.25 else mix(coral_dark, coral, 0.5)
            put(px, img, x, base, col)
            if hash01(x, 4) > 0.55:
                put(px, img, x, base - 1, mix(m1, m2, 0.5 * hash01(x, 6)))

    # das Riff: Gruppen von zwei, drei Korallen, nach rechts niedriger und lichter
    groups = []
    x = BAR_X + 1
    i = 0
    while x < BAR_X + BAR_W - 6:
        rel = (x - BAR_X) / BAR_W
        width = 13 + round(hash01(x, 1) * 6) - round(rel * 5)
        if any(abs(x + width / 2 - (BAR_X + round(BAR_W * f))) < width / 2 + 6 for f in GEMS):
            x += 4
            continue
        groups.append((x, width, rel, i))
        x += width + 2 + round(rel * 6)
        i += 1
    for gx, width, rel, i in groups:
        if rel < 0.86:
            rubble(gx, gx + width)
    for gx, width, rel, i in groups:
        scale = 1.0 - 0.55 * rel
        kind = i % 5
        seed = gx * 7 + i
        if rel >= 0.86:
            scallop(gx + 2)
            if i % 2 == 0:
                anemone_tuft(gx + 7, 4, seed, pink=True)
            continue
        if kind == 0:
            staghorn(gx + 4, base, -math.pi / 2 - 0.15, 4.2 * scale + 1.0, 2, seed)
            anemone_tuft(gx + 9, max(3, round(5 * scale)), seed)
        elif kind == 1:
            gorgonian(gx + 6, max(4, round(7 * scale)), seed)
            seagrass(gx + 12, max(4, round(8 * scale)), seed)
        elif kind == 2:
            brain_coral(gx + 4, 4 if scale > 0.7 else 3, 3, seed)
            sponge_tubes(gx + 9, [max(2, round(h * scale)) for h in (7, 9, 5)][: 3 if scale > 0.6 else 2])
        elif kind == 3:
            table_coral(gx + 5, max(3, round(6 * scale)), 5 if scale > 0.7 else 4, seed)
            staghorn(gx + 12, base, -math.pi / 2 + 0.2, 3.2 * scale + 1.0, 1, seed, (fan_dark, fan, (255, 190, 240, 255)))
        else:
            anemone_tuft(gx + 3, max(3, round(6 * scale)), seed, pink=True)
            seagrass(gx + 8, max(4, round(9 * scale)), seed)
            brain_coral(gx + 13, 2, 2, seed)
    starfish(BAR_X + BAR_W - 14, base - 2)
    # zwei kleine Fische: ein Anemonenfisch nahe dem Medaillon, ein blauer Doktorfisch weiter rechts
    fish(BAR_X + round(BAR_W * 0.42), top - 11, (255, 128, 30, 255), white, 1)
    fish(BAR_X + round(BAR_W * 0.74), top - 8, (60, 120, 230, 255), (255, 214, 60, 255), -1)
    # Seepocken und Rotalgen unter der Leiste, aufsteigende Blasen
    for x in range(BAR_X + 3, BAR_X + BAR_W - 3, 7):
        h = hash01(x, 19)
        if h > 0.62:
            for dx in range(-1, 2):
                put(px, img, x + dx, bottom + 1, mix(m2, m1, 0.2 + 0.25 * (dx + 1)))
            put(px, img, x, bottom + 2, mix(m1, m0, 0.3))
            put(px, img, x - 1, bottom + 2, mix(m2, m1, 0.5))
            put(px, img, x, bottom + 1, mix(m0, OUTLINE, 0.5))                 # Öffnung
        elif h > 0.4:
            length = 3 + round(hash01(x, 21) * 4)
            for k in range(length):
                sway = round(0.8 * math.sin(k * 0.9 + x))
                put(px, img, x + sway, bottom + 1 + k, mix(coral_dark, coral, k / max(1, length - 1)))
    for x in range(BAR_X + 9, BAR_X + BAR_W, 14):
        y = bottom + 6 + round(hash01(x, 23) * 6)
        if hash01(x, 29) > 0.5:
            for dx, dy in ((0, -1), (-1, 0), (1, 0), (0, 1)):
                put(px, img, x + dx, y + dy, f2)
            put(px, img, x - 1, y - 1, f3)
            put(px, img, x + 2, y - 4, f2)
        else:
            put(px, img, x, y, f2)
            put(px, img, x + 1, y - 2, f3)
    # Kranz aus Korallenästen um das Medaillon, darüber ein Goldreif
    for k, ang in enumerate((-158, -136, -112, -68, -44, -22)):
        a = math.radians(ang)
        bx = EMB_X + math.cos(a) * (EMB_R - 0.5)
        by = EMB_Y + math.sin(a) * (EMB_R - 0.5)
        staghorn(bx, by, a, 3.6, 1, 50 + k)
    for ang in range(-178, -1, 3):
        a = math.radians(ang)
        put(px, img, round(EMB_X + math.cos(a) * (EMB_R + 0.3)), round(EMB_Y + math.sin(a) * (EMB_R + 0.3)),
            gold if ang < -90 else gold_dark)
    # Spitze: die geöffnete Schere des Karkinos
    claw = ["OO.....",
            "LLOO...",
            "HLLLOO.",
            "LLLLLLO",
            "CCCCCLO",
            "CCOOOWO",
            "CO.....",
            "CCOOWO.",
            "DCCCCO.",
            "DDDDO..",
            "OOOO..."]
    cols = {"O": OUTLINE, "L": coral_light, "H": white, "C": coral, "D": coral_dark, "W": (60, 30, 28, 255)}
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2 - 6
    for dy, row in enumerate(claw):
        for dx, ch in enumerate(row):
            if ch != ".":
                put(px, img, ex + dx, ey + dy, cols[ch])


def deco_moth(img: Image.Image, pal: dict) -> None:
    """Hinter dem Medaillon breitet die Mottenkönigin ihre Flügel aus (Vorderflügel nach oben, Hinterflügel mit langen
    Schwänzen nach unten, mit Augenflecken); oben auf dem Rahmen sitzen gefiederte Fühler und kleine Mondsicheln,
    unter der Leiste hängen Seidenfäden mit Tautropfen; zwei kleine Falter schwirren über der Leiste. Als Spitze eine
    liegende Mondsichel mit Stern."""
    px = img.load()
    f2, f3 = c(pal, "f2"), c(pal, "f3")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    wing, wing_dark, wing_light = c(pal, "wing"), c(pal, "wing_dark"), c(pal, "wing_light")
    spot, spot_dark = c(pal, "spot"), c(pal, "spot_dark")
    silk, silk_dark = c(pal, "silk"), c(pal, "silk_dark")
    m1 = c(pal, "m1")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def wing_shape(cx: float, cy: float, rx: float, ry: float, ang: float, tail: float):
        """Ein Flügel als gedrehte Ellipse, der Rand hell, innen Adern, ein Augenfleck; optional ein Schwanz."""
        ca, sa = math.cos(ang), math.sin(ang)
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                u = (dx * ca + dy * sa) / rx
                v = (-dx * sa + dy * ca) / ry
                d = u * u + v * v
                if d > 1.0 or u < 0.0:
                    continue
                col = mix(wing_dark, wing, 0.35 + 0.65 * u)
                if d > 0.72:
                    col = wing_light if d < 0.86 else mix(wing_dark, OUTLINE, 0.3)
                elif abs(math.sin(math.atan2(v, u) * 5.0)) < 0.12:
                    col = mix(wing_dark, wing, 0.3)
                es = (u - 0.62) ** 2 + (v * 1.2) ** 2
                if es < 0.012:
                    col = f3
                elif es < 0.03:
                    col = spot
                elif es < 0.05:
                    col = gold
                elif es < 0.07:
                    col = spot_dark
                put(px, img, x, y, col)
        if tail > 0:
            tx, ty = cx + ca * rx * 0.9, cy + sa * rx * 0.9
            for k in range(int(tail)):
                t = k / max(1, tail - 1)
                put(px, img, round(tx + ca * k * 0.8 + t * t * 3.0), round(ty + sa * k * 0.8), wing if k < tail - 1 else wing_light)

    # Flügel, die hinter dem Medaillon hervorkommen: oben der Vorderflügel, unten der Hinterflügel mit Schwanz
    wing_shape(EMB_X + 13, EMB_Y - 8, 27, 8.5, math.radians(-17), 0)
    wing_shape(EMB_X + 12, EMB_Y + 8, 19, 6.0, math.radians(20), 8)
    # gefiederte Fühler über dem Medaillon
    for side in (-1, 1):
        x0, y0 = EMB_X + side * 4, EMB_Y - EMB_R + 1
        for k in range(9):
            x = round(x0 + side * k * 0.6)
            y = y0 - k
            put(px, img, x, y, m1 if k < 8 else gold)
            if 1 < k < 8 and k % 2 == 0:
                put(px, img, x - side, y, mix(m1, silk, 0.4))
                put(px, img, x + side, y + 1, mix(m1, silk, 0.25))
    # kleine Mondsicheln auf dem Rahmen (nach rechts offen), daneben ein Stern
    crescent = (".gg.", "g..o", "g...", "g..o", ".gg.")
    for x0 in range(BAR_X + 34, BAR_X + BAR_W - 10, 38):
        for dy, row in enumerate(crescent):
            for dx, ch in enumerate(row):
                if ch == "g":
                    put(px, img, x0 + dx, top - 7 + dy, gold if dy < 3 else gold_dark)
                elif ch == "o":
                    put(px, img, x0 + dx, top - 7 + dy, mix(gold, f3, 0.5))
        put(px, img, x0 + 6, top - 8, f3)
    # Seidenfäden mit Tautropfen unter der Leiste
    for x in range(BAR_X + 11, BAR_X + BAR_W - 4, 17):
        length = 3 + round(hash01(x, 41) * 7)
        for k in range(length):
            put(px, img, x, bottom + 1 + k, silk if k % 4 else silk_dark)
        put(px, img, x, bottom + 1 + length, f3)
        put(px, img, x, bottom + 2 + length, f2)
    # zwei kleine Falter über der Leiste
    for (mx, my) in ((BAR_X + 70, top - 9), (BAR_X + 140, top - 7)):
        for dx, dy, col in ((-2, -1, wing), (-1, -1, wing_light), (-1, 0, wing), (0, 0, silk_dark), (1, 0, wing), (1, -1, wing_light),
                            (2, -1, wing), (-1, 1, wing_dark), (1, 1, wing_dark), (0, -1, silk_dark)):
            put(px, img, mx + dx, my + dy, col)
    # Spitze: eine liegende Mondsichel mit Stern
    ex, ey = BAR_X + BAR_W + 2, BAR_Y + BAR_H // 2
    for k in range(9):
        a = math.radians(-120 + k * 30)
        put(px, img, round(ex + 3 + math.cos(a) * 4), round(ey + math.sin(a) * 4), gold if 1 <= k <= 7 else gold_dark)
        put(px, img, round(ex + 3 + math.cos(a) * 3), round(ey + math.sin(a) * 3), mix(gold, f3, 0.4) if 2 <= k <= 6 else gold_dark)
    put(px, img, ex + 7, ey - 1, f3)
    put(px, img, ex + 7, ey, f2)


def deco_forge(img: Image.Image, pal: dict) -> None:
    """Hinter dem Medaillon gekreuzt: der Schmiedehammer (Kopf oben links) und der Glutspeer (glühende Klinge oben
    rechts); oben auf dem Rahmen kleine Glutzungen mit aufstiebenden Funken, unter der Leiste tropft geschmolzenes Metall
    (die Tropfen glühen); als Spitze die Klinge des Glutspeers."""
    px = img.load()
    f1, f2, f3 = c(pal, "f1"), c(pal, "f2"), c(pal, "f3")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    iron, iron_light = c(pal, "iron"), c(pal, "iron_light")
    ember, ember_dark, wood = c(pal, "ember"), c(pal, "ember_dark"), c(pal, "wood")
    m1 = c(pal, "m1")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def stick(x0: float, y0: float, x1: float, y1: float, width: float, col_fn):
        length = math.hypot(x1 - x0, y1 - y0)
        ax, ay = (x1 - x0) / length, (y1 - y0) / length
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                dx, dy = x + 0.5 - x0, y + 0.5 - y0
                t = dx * ax + dy * ay
                off = -dx * ay + dy * ax
                if 0 <= t <= length:
                    w = width(t / length) if callable(width) else width
                    if abs(off) <= w:
                        put(px, img, x, y, col_fn(t / length, off, w))

    # der Speer: von unten links nach oben rechts, die Klinge glüht
    def spear_col(t, off, w):
        if t < 0.72:
            return OUTLINE if abs(off) > w - 0.6 else (wood if off < 0 else mix(wood, OUTLINE, 0.4))
        if t < 0.76:
            return gold if off < 0 else gold_dark
        if abs(off) > w - 0.6:
            return ember_dark
        return f3 if abs(off) < 0.6 else (f2 if off < 0 else ember)
    stick(EMB_X - 16, EMB_Y + 22, EMB_X + 40, EMB_Y - 21, lambda u: 1.1 if u < 0.72 else (2.6 if u < 0.76 else 2.6 * (1 - (u - 0.76) / 0.24) ** 0.8 + 0.3),
          spear_col)
    # der Hammer: von unten rechts nach oben links, oben der Kopf quer
    def handle_col(t, off, w):
        return OUTLINE if abs(off) > w - 0.6 else (mix(wood, iron_light, 0.15) if off < 0 else wood)
    stick(EMB_X + 17, EMB_Y + 21, EMB_X - 13, EMB_Y - 17, 1.1, handle_col)
    hx, hy = EMB_X - 13, EMB_Y - 17
    for y in range(-6, 7):
        for x in range(-6, 7):
            u = (x - y) / 1.414
            v = (x + y) / 1.414
            if abs(u) <= 5.2 and abs(v) <= 2.6:
                edge = abs(u) > 4.4 or abs(v) > 1.9
                face = abs(u) > 4.0
                col = OUTLINE if edge and not face else (ember if face else (iron_light if v < -0.6 else iron))
                put(px, img, hx + x, hy + y, col)
    # Glutzungen und Funken oben auf dem Rahmen
    for x in range(BAR_X + 6, BAR_X + BAR_W - 2, 11):
        hgt = 2 + round(hash01(x, 13) * 3)
        for k in range(hgt):
            t = k / max(1, hgt - 1)
            col = f1 if t < 0.4 else (f2 if t < 0.8 else f3)
            put(px, img, x, top - 1 - k, col)
            if k == 0:
                put(px, img, x - 1, top - 1, ember_dark)
                put(px, img, x + 1, top - 1, ember_dark)
        if hash01(x, 17) > 0.45:
            put(px, img, x + round(hash01(x, 19) * 4) - 2, top - hgt - 2 - round(hash01(x, 23) * 3), f2 if hash01(x, 29) > 0.5 else f3)
    # geschmolzenes Metall tropft von der Leiste
    for x in range(BAR_X + 9, BAR_X + BAR_W - 4, 13):
        length = 2 + round(hash01(x, 31) * 4)
        for k in range(length):
            put(px, img, x, bottom + 1 + k, ember_dark if k < length - 2 else ember)
        put(px, img, x, bottom + 1 + length, f2)
        put(px, img, x, bottom + 2 + length, f3)
        put(px, img, x + 1, bottom + 1, ember_dark)
    # Spitze: die Klinge des Glutspeers nach rechts
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(12):
        half = round(4.2 * (1 - k / 12) ** 0.75) if k > 1 else 2
        for dy in range(-half, half + 1):
            if abs(dy) == half:
                col = OUTLINE if k > 1 else gold_dark
            elif k <= 1:
                col = gold
            else:
                col = f3 if dy == 0 else (f2 if dy < 0 else ember)
            put(px, img, ex + k, ey + dy, col)
    put(px, img, ex + 12, ey, ember_dark)
    _ = m1


def deco_star(img: Image.Image, pal: dict) -> None:
    """Hinter dem Medaillon breitet der Sternenrochen seine Schwingen aus (Nachthimmel mit Sternen, violette Vorderkante,
    nach außen spitz zulaufend); schräg dahinter die Sternenlanze mit leuchtender Kristallspitze. Oben auf dem Rahmen
    funkeln kleine vierzackige Sterne, unter der Leiste fallen Sternschnuppen; als Spitze ein Sternkristall."""
    px = img.load()
    f2, f3 = c(pal, "f2"), c(pal, "f3")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    night, night_dark = c(pal, "night"), c(pal, "night_dark")
    nebula, nebula_light = c(pal, "nebula"), c(pal, "nebula_light")
    silver, silver_dark, crystal = c(pal, "silver"), c(pal, "silver_dark"), c(pal, "crystal")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def wing(cx: float, cy: float, length: float, lead: float, trail: float, ang: float):
        """Ein Rochenflügel ab (cx, cy) nach rechts: vorn die Vorderkante (Neigung lead), hinten geschwungen zur Spitze."""
        ca, sa = math.cos(ang), math.sin(ang)
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                u = dx * ca + dy * sa
                v = -dx * sa + dy * ca
                if u < 0.0 or u > length:
                    continue
                k = u / length
                v_lead = -lead * (1.0 - k)
                v_trail = trail * (1.0 - k) ** 1.4 - 1.2 * math.sin(math.pi * k)
                if v < v_lead or v > v_trail:
                    continue
                edge = v < v_lead + 1.0
                rim = v > v_trail - 0.9 or u > length - 1.2
                col = mix(night_dark, night, 0.4 + 0.6 * (1.0 - k))
                if hash01(x, y, 131) > 0.93:
                    col = f3 if hash01(x, y, 137) > 0.5 else crystal
                if edge:
                    col = nebula_light if v < v_lead + 0.5 else nebula
                elif rim:
                    col = mix(nebula, OUTLINE, 0.35)
                put(px, img, x, y, col)

    wing(EMB_X + 10, EMB_Y - 9, 34, 6.0, 9.0, math.radians(-24))
    # die Lanze: von unten links nach oben rechts hinter dem Medaillon, die Kristallspitze leuchtet
    x0, y0, x1, y1 = EMB_X - 17, EMB_Y + 20, EMB_X + 30, EMB_Y - 21
    length = math.hypot(x1 - x0, y1 - y0)
    ax, ay = (x1 - x0) / length, (y1 - y0) / length
    for y in range(FRAME_H):
        for x in range(FRAME_W):
            dx, dy = x + 0.5 - x0, y + 0.5 - y0
            t = (dx * ax + dy * ay) / length
            off = -dx * ay + dy * ax
            if not 0.0 <= t <= 1.0:
                continue
            if t < 0.8:
                w = 1.1
                if abs(off) > w:
                    continue
                col = OUTLINE if abs(off) > w - 0.55 else ((gold if off < 0 else gold_dark) if (t * 26) % 1.0 < 0.22 else
                                                          (silver if off < 0 else silver_dark))
            elif t < 0.84:
                w = 2.7
                if abs(off) > w:
                    continue
                col = OUTLINE if abs(off) > w - 0.6 else (gold if off < 0 else gold_dark)
            else:
                u = (t - 0.84) / 0.16
                w = 2.4 * max(0.0, 1.0 - u) ** 0.8 + 0.3
                if abs(off) > w:
                    continue
                col = mix(crystal, OUTLINE, 0.4) if abs(off) > w - 0.6 else (f3 if abs(off) < 0.7 else crystal)
            put(px, img, x, y, col)

    def star(sx: int, sy: int, r: int, col, core):
        for k in range(-r, r + 1):
            shade = col if abs(k) < r else mix(col, OUTLINE, 0.3)
            put(px, img, sx + k, sy, shade)
            put(px, img, sx, sy + k, shade)
        put(px, img, sx, sy, core)

    # kleine Sterne oben auf dem Rahmen
    for x in range(BAR_X + 14, BAR_X + BAR_W - 6, 27):
        big = hash01(x, 151) > 0.5
        star(x, top - 4 - round(hash01(x, 157) * 2), 2 if big else 1, crystal, f3)
    # Sternschnuppen unter der Leiste (schräg fallend, mit Schweif)
    for x in range(BAR_X + 18, BAR_X + BAR_W - 6, 31):
        length = 4 + round(hash01(x, 163) * 4)
        for k in range(length):
            t = k / max(1, length - 1)
            put(px, img, x - k, bottom + 2 + k // 2, mix(f2, nebula, t * 0.6) if k else f3)
        put(px, img, x + 1, bottom + 2, crystal)
    # Spitze: ein Sternkristall nach rechts
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(11):
        half = round(3.6 * (1 - k / 11) ** 0.75) if k > 1 else 2
        for dy in range(-half, half + 1):
            if abs(dy) == half:
                col = OUTLINE if k > 1 else gold_dark
            elif k <= 1:
                col = gold
            else:
                col = f3 if dy == 0 else (crystal if dy < 0 else f2)
            put(px, img, ex + k, ey + dy, col)
    for dy in (-5, -4, 4, 5):
        put(px, img, ex + 4, ey + dy, crystal if abs(dy) == 4 else OUTLINE)
    put(px, img, ex + 11, ey, f2)


def deco_mystic(img: Image.Image, pal: dict) -> None:
    """Hinter dem Medaillon ein Fächer aus fünf Zauberkarten (weiß mit Goldrand, rote und schwarze Farben), schräg dahinter
    ihr Zauberstab (schwarz mit weißen Spitzen); oben auf dem Rahmen die vier Kartenfarben, unter der Leiste rieselt
    Konfetti. Als Spitze die weiße Spitze des Zauberstabs mit einem Funkelstern."""
    px = img.load()
    f2, f3 = c(pal, "f2"), c(pal, "f3")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    card, card_dark = c(pal, "card"), c(pal, "card_dark")
    red, black, wand = c(pal, "red"), c(pal, "black"), c(pal, "wand")
    confetti = [c(pal, "acc"), gold, c(pal, "confetti"), f2, card]
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2
    pips = {"heart": (".#.#.", "#####", "#####", ".###.", "..#.."),
            "spade": ("..#..", ".###.", "#####", "#####", "..#.."),
            "diamond": ("..#..", ".###.", "#####", ".###.", "..#.."),
            "club": (".###.", ".###.", "#####", "#.#.#", "..#..")}

    # der Zauberstab: von unten links nach oben rechts hinter dem Medaillon
    x0, y0, x1, y1 = EMB_X - 15, EMB_Y + 19, EMB_X + 34, EMB_Y - 20
    length = math.hypot(x1 - x0, y1 - y0)
    for k in range(int(length) + 1):
        t = k / length
        x = x0 + (x1 - x0) * t
        y = y0 + (y1 - y0) * t
        tip = t < 0.08 or t > 0.9
        for o in (-1, 0, 1):
            col = OUTLINE if o != 0 else (card if tip else wand)
            put(px, img, round(x + o * 0.7), round(y + o * 0.7), col)

    def card_at(cx: float, cy: float, ang: float, suit: str):
        """Eine kleine Karte (8 x 11) um (cx, cy) gedreht: Goldrand, weiße Fläche, die Farbe in der Mitte – nie über der
        Leiste (die bleibt frei)."""
        ca, sa = math.cos(ang), math.sin(ang)
        w, h = 4.0, 5.6
        red_suit = suit in ("heart", "diamond")
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                if x >= BAR_X - 3 and y >= BAR_Y - 3:
                    continue
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                u = dx * ca + dy * sa
                v = -dx * sa + dy * ca
                if abs(u) > w or abs(v) > h:
                    continue
                if abs(u) > w - 0.9 or abs(v) > h - 0.9:
                    col = OUTLINE
                elif abs(u) > w - 1.8 or abs(v) > h - 1.8:
                    col = gold if (u + v) < 0 else gold_dark
                else:
                    col = mix(card, card_dark, 0.1 + 0.25 * (v + h) / (2 * h))
                    gx, gy = int(math.floor(u + 1.5)), int(math.floor(v + 1.5))
                    small_pip = {"heart": ("#.#", "###", ".#."), "spade": (".#.", "###", "#.#"),
                                 "diamond": (".#.", "###", ".#."), "club": (".#.", "###", ".#.")}[suit]
                    if 0 <= gx < 3 and 0 <= gy < 3 and small_pip[gy][gx] == "#":
                        col = red if red_suit else black
                put(px, img, x, y, col)

    # ein Fächer aus vier Karten steigt hinter dem Medaillon nach rechts oben auf
    for i, suit in enumerate(("club", "diamond", "spade", "heart")):
        ang = math.radians(-35 + i * 20)
        card_at(EMB_X + 17 + i * 6.0, EMB_Y - 12 + abs(i - 1.5) * 1.2, ang, suit)

    # die vier Kartenfarben auf dem Rahmen
    small = {"heart": ("#.#", "###", ".#."), "spade": (".#.", "###", "#.#"), "diamond": (".#.", "###", ".#."),
             "club": (".#.", "###", ".#.")}
    order = ("spade", "heart", "club", "diamond")
    for k, x in enumerate(range(BAR_X + 12, BAR_X + BAR_W - 6, 22)):
        suit = order[k % 4]
        col = red if suit in ("heart", "diamond") else card
        for gy, row in enumerate(small[suit]):
            for gx, ch in enumerate(row):
                if ch == "#":
                    put(px, img, x + gx - 1, top - 3 + gy, col)
        for gx, gy in ((-2, -1), (2, -1), (-2, 3), (2, 3)):
            put(px, img, x + gx, top - 3 + gy, gold_dark)
    # Konfetti unter der Leiste
    for x in range(BAR_X + 4, BAR_X + BAR_W - 4):
        h = hash01(x, 977)
        if h > 0.82:
            y = bottom + 1 + int(hash01(x, 979) * 5)
            put(px, img, x, y, confetti[int(hash01(x, 983) * len(confetti)) % len(confetti)])
            if h > 0.93:
                put(px, img, x + 1, y, confetti[int(hash01(x, 991) * len(confetti)) % len(confetti)])
    # Spitze: die weiße Spitze des Zauberstabs mit Funkelstern
    ex, ey = BAR_X + BAR_W + 2, BAR_Y + BAR_H // 2
    for k in range(9):
        for dy in (-1, 0, 1):
            col = OUTLINE if abs(dy) == 1 else (card if k > 4 else wand)
            put(px, img, ex + k, ey + dy, col)
    sx, sy = ex + 11, ey
    for d in range(-3, 4):
        put(px, img, sx + d, sy, f3 if abs(d) < 2 else gold)
        put(px, img, sx, sy + d, f3 if abs(d) < 2 else gold)
    put(px, img, sx + 1, sy + 1, gold)
    put(px, img, sx - 1, sy - 1, gold)
    put(px, img, sx + 1, sy - 1, gold)
    put(px, img, sx - 1, sy + 1, gold)


def deco_anni(img: Image.Image, pal: dict) -> None:
    """Hinter dem Medaillon schräg der Bannhammer (Baumstamm-Stiel, Steinklotz, dessen vordere Hälfte fehlende Textur ist);
    um den Rahmen schweben lose Pixel in Rot, Grün, Blau, Magenta und Cyan, der Rahmen hat Farbsäume (oben magenta, unten
    cyan), und an der Spitze sitzt ein gelbes Warndreieck."""
    px = img.load()
    acc, cyan = c(pal, "acc"), c(pal, "cyan")
    wood, wood_dark = c(pal, "wood"), c(pal, "wood_dark")
    stone, stone_dark, iron = c(pal, "stone"), c(pal, "stone_dark"), c(pal, "iron")
    pixels = [c(pal, k) for k in ("red", "green", "blue", "acc", "cyan", "yellow")]
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    # der Stiel: von unten links nach oben rechts hinter dem Medaillon, der Kopf rechts oben über dem Rahmen
    x0, y0, x1, y1 = EMB_X - 16, EMB_Y + 18, EMB_X + 27, EMB_Y - 14
    length = math.hypot(x1 - x0, y1 - y0)
    for k in range(int(length) + 1):
        t = k / length
        x = x0 + (x1 - x0) * t
        y = y0 + (y1 - y0) * t
        for o in (-1, 0, 1):
            col = OUTLINE if o != 0 else (wood if (k // 3) % 2 else wood_dark)
            put(px, img, round(x + o * 0.7), round(y + o * 0.7), col)
    # der Kopf: ein schräger Klotz um das obere Ende, die vordere Hälfte fehlende Textur
    hx, hy = x1 + 2, y1 + 1
    ang = math.atan2(y1 - y0, x1 - x0)
    ca, sa = math.cos(ang), math.sin(ang)
    for y in range(FRAME_H):
        for x in range(FRAME_W):
            if x >= BAR_X - 3 and y >= BAR_Y - 3:
                continue
            dx, dy = x + 0.5 - hx, y + 0.5 - hy
            u = dx * ca + dy * sa            # entlang des Stiels
            v = -dx * sa + dy * ca           # quer
            if abs(u) > 4.2 or abs(v) > 7.2:
                continue
            if abs(u) > 3.4 or abs(v) > 6.4:
                col = OUTLINE
            elif v < 0:
                col = acc if (int(math.floor(u + 8)) // 2 + int(math.floor(v + 8)) // 2) % 2 == 0 else (8, 0, 10, 255)
            else:
                col = mix(stone, stone_dark, 0.2 + 0.5 * hash01(x, y, 5)) if abs(v - 2.5) > 0.7 else iron
            put(px, img, x, y, col)
    # Farbsäume am Rahmen: oben magenta, unten cyan (verrutscht)
    for x in range(BAR_X - 2, BAR_X + BAR_W + 2):
        if hash01(x // 6, 81) > 0.45:
            put(px, img, x, top - 1, acc)
        if hash01(x // 5, 83) > 0.5:
            put(px, img, x + 1, bottom + 1, cyan)
    # lose Pixel um den Rahmen (je 2 x 2)
    for i in range(26):
        x = BAR_X + int(hash01(i, 91) * (BAR_W - 4))
        above = hash01(i, 93) > 0.5
        y = top - 4 - int(hash01(i, 95) * 6) if above else bottom + 2 + int(hash01(i, 97) * 4)
        col = pixels[int(hash01(i, 99) * len(pixels)) % len(pixels)]
        for gx in range(2):
            for gy in range(2):
                if 0 <= y + gy < FRAME_H:
                    put(px, img, x + gx, y + gy, col)
    # Spitze: ein gelbes Warndreieck mit schwarzem Ausrufezeichen (am rechten Ende des Rahmens)
    yellow = c(pal, "yellow")
    ex, ey = 234, BAR_Y + BAR_H // 2
    for y in range(-6, 6):
        half = (y + 6) * 0.45
        for x in range(-6, 7):
            if abs(x) > half + 0.3:
                continue
            edge = abs(x) > half - 1.0 or y == 5
            col = OUTLINE if edge else yellow
            if x == 0 and (-2 <= y <= 1 or y == 3):
                col = OUTLINE
            put(px, img, ex + x, ey + y, col)


def deco_schrolli(img: Image.Image, pal: dict) -> None:
    """Hinter dem Medaillon zwei Schwingen (eine nach oben rechts erhoben, eine nach unten rechts gesenkt), jede aus fünf
    gefächerten Federn mit Schaft und Spitze; oben auf dem Rahmen ein Kranz aus Strahlen mit Funkeln dazwischen, unter der
    Leiste goldene Tropfen; als Spitze der achtstrahlige Zepterstern um einen Himmelsstein. In der Wahren Gestalt sind die
    Federn ein Sternenhimmel mit schillernden Spitzen, die Strahlen Sterne und die Tropfen Sternenstaub."""
    px = img.load()
    truly = "prism1" in pal
    f3 = c(pal, "f3")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    feather, feather_mid, feather_dark = c(pal, "feather"), c(pal, "feather_mid"), c(pal, "feather_dark")
    tip, ray = c(pal, "tip"), c(pal, "ray")
    gem_blue, gem_light = c(pal, "gem_blue"), c(pal, "gem_light")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def quill(x0: float, y0: float, ang: float, length: float, width: float, seed: int):
        """Eine Feder ab (x0, y0) in Richtung ang: spitz zulaufend, mit Schaft, Fahne und farbiger Spitze."""
        ca, sa = math.cos(ang), math.sin(ang)
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                dx, dy = x + 0.5 - x0, y + 0.5 - y0
                u = dx * ca + dy * sa
                v = -dx * sa + dy * ca
                if u < 0.0 or u > length:
                    continue
                k = u / length
                half = width * (0.55 + 0.45 * math.sin(math.pi * min(1.0, k * 1.15))) * (1.0 - max(0.0, k - 0.82) * 3.2)
                if half <= 0.2 or abs(v) > half:
                    continue
                edge = abs(v) > half - 0.75
                if edge:
                    col = OUTLINE
                elif abs(v) < 0.45 and k < 0.9:
                    col = mix(feather_mid, feather_dark, 0.35)                       # Schaft
                else:
                    lit = 0.35 + 0.65 * (1.0 - (v + half) / (2 * half))          # Licht von oben
                    col = mix(feather_mid, feather, lit)
                    if truly and hash01(x, y, seed, 211) > 0.9:
                        col = f3 if hash01(x, y, seed, 213) > 0.5 else c(pal, "prism3")
                    if k > 0.68:
                        t = (k - 0.68) / 0.32
                        tipcol = tip
                        if truly:
                            tipcol = mix(c(pal, "prism1"), c(pal, "prism3"), hash01(seed, 217)) if t > 0.5 else c(pal, "prism2")
                        col = mix(col, tipcol, min(1.0, t * 1.6))
                    elif not truly and (u * 1.3 + seed) % 5.0 < 0.6:
                        col = mix(col, feather_mid, 0.6)                             # Rippen der Fahne
                put(px, img, x, y, col)

    # die erhobene Schwinge: fünf Federn, nach außen länger und flacher
    for j in range(5):
        k = j / 4
        quill(EMB_X + 12 + 3 * j, EMB_Y - 9 - j * 0.6, math.radians(-46 + 22 * k), 17 + 13 * k, 3.1 - 0.3 * k, j)
    # die gesenkte Schwinge: vier Federn
    for j in range(4):
        k = j / 3
        quill(EMB_X + 12 + 3 * j, EMB_Y + 8 + j * 0.5, math.radians(38 - 20 * k), 14 + 9 * k, 2.8 - 0.3 * k, 20 + j)

    def spark(sx: int, sy: int, r: int, col, core):
        for k in range(-r, r + 1):
            shade = col if abs(k) < r else mix(col, OUTLINE, 0.25)
            put(px, img, sx + k, sy, shade)
            put(px, img, sx, sy + k, shade)
        put(px, img, sx, sy, core)

    # der Kranz aus Strahlen auf dem Rahmen (nicht über den Fassungen der Edelsteine)
    gems = [BAR_X + round(BAR_W * f) for f in GEMS]
    for i, x in enumerate(range(BAR_X + 10, BAR_X + BAR_W - 4, 9)):
        if any(abs(x - g) < 9 for g in gems):
            continue
        h = 3 + (2 if i % 2 == 0 else 0) + (1 if i % 4 == 0 else 0)
        if truly:
            spark(x, top - h, 1 if i % 2 else 2, ray, f3)
            continue
        for dy in range(h):
            half = 1 if dy < h - 2 else 0
            for dx in range(-half, half + 1):
                col = OUTLINE if abs(dx) == half and half > 0 else (ray if dx <= 0 else gold)
                put(px, img, x + dx, top - 1 - dy, col)
        put(px, img, x, top - h, f3)
        if i % 3 == 1:
            spark(x + 4, top - 6, 1, ray, f3)
    # unter der Leiste: goldene Tropfen (in der Wahren Gestalt Sternenstaub)
    for i, x in enumerate(range(BAR_X + 6, BAR_X + BAR_W - 2, 12)):
        length = 2 + (i % 3)
        for dy in range(length):
            col = gold if dy < length - 1 else (f3 if not truly else ray)
            if truly:
                col = mix(ray, f3, dy / max(1, length - 1)) if hash01(x, dy, 223) > 0.3 else c(pal, "prism2")
            put(px, img, x, bottom + 1 + dy, col)
            if not truly:
                put(px, img, x + 1, bottom + 1 + dy, gold_dark if dy < length - 1 else OUTLINE)
        if truly and i % 2 == 0:
            spark(x + 6, bottom + 4, 1, ray, f3)
    # Spitze: der achtstrahlige Zepterstern um einen Himmelsstein
    ex, ey = BAR_X + BAR_W + 4, BAR_Y + BAR_H // 2
    for ang in range(0, 360, 45):
        r = 4 if ang % 90 == 0 else 3
        ca, sa = math.cos(math.radians(ang)), math.sin(math.radians(ang))
        for k in range(1, r + 1):
            put(px, img, round(ex + ca * k), round(ey + sa * k), gold if k < r else ray)
    for dx in (-1, 0, 1):
        for dy in (-1, 0, 1):
            put(px, img, ex + dx, ey + dy, gem_light if dx == -1 and dy == -1 else gem_blue)
    put(px, img, ex, ey, gem_light)


def post_schrolli(img: Image.Image, pal: dict) -> None:
    """Nach dem Medaillon: in der Wahren Gestalt sieben Augen auf seinem Ring (wie auf den Rädern im Rad)."""
    if "prism1" not in pal:
        return
    px = img.load()
    for k in range(7):
        a = math.radians(-150 + k * 50)
        x = round(EMB_X - 0.5 + math.cos(a) * (EMB_R - 2.5))
        y = round(EMB_Y - 0.5 + math.sin(a) * (EMB_R - 2.5))
        put(px, img, x - 1, y, c(pal, "f3"))
        put(px, img, x + 1, y, c(pal, "f3"))
        put(px, img, x, y - 1, c(pal, "gold"))
        put(px, img, x, y + 1, c(pal, "gold_dark"))
        put(px, img, x, y, c(pal, "tip"))


# =============================================================================================
# Schrolli der Allmächtige (dritte Gestalt) und der Weltenfresser
# =============================================================================================

def _ramp(cols, t: float):
    """Verlauf über mehrere Stützfarben (t von 0 bis 1)."""
    t = max(0.0, min(1.0, t)) * (len(cols) - 1)
    i = min(len(cols) - 2, int(t))
    return mix(cols[i], cols[i + 1], t - i)


def _outline(layer: Image.Image, col) -> None:
    """Freie Pixel, die (4er-Nachbarschaft) an die Form auf der Ebene grenzen, bekommen die Kontur."""
    src = layer.load()
    w, h = layer.size
    edge = []
    for y in range(h):
        for x in range(w):
            if src[x, y][3]:
                continue
            for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
                if 0 <= nx < w and 0 <= ny < h and src[nx, ny][3]:
                    edge.append((x, y))
                    break
    for x, y in edge:
        src[x, y] = col


def _worley(x: float, y: float, cell: float, period: int, seed: int) -> tuple[float, float]:
    """Abstand zum nächsten und zweitnächsten Zellpunkt; waagrecht nahtlos (period Zellen × cell = 256)."""
    cx, cy = int(math.floor(x / cell)), int(math.floor(y / cell))
    d1 = d2 = 99.0
    for gy in range(cy - 1, cy + 2):
        for gx in range(cx - 1, cx + 2):
            wx = gx % period
            fx = (gx + 0.15 + 0.7 * hash01(wx, gy, seed)) * cell
            fy = (gy + 0.15 + 0.7 * hash01(wx, gy, seed + 1)) * cell
            d = math.hypot(x + 0.5 - fx, y + 0.5 - fy)
            if d < d1:
                d1, d2 = d, d1
            elif d < d2:
                d2 = d
    return d1, d2


def _gem_sockets(px, img: Image.Image, pal: dict, fill0, fill1) -> None:
    """Fassungen der Phasen-Edelsteine über dem Rahmen (wie in frame_border)."""
    y0 = BAR_Y - 3
    for f in GEMS:
        gx = BAR_X + round(BAR_W * f)
        for dy in range(-6, 1):
            half = 5 - abs(dy + 3) + 1
            for dx in range(-half, half + 1):
                x, y = gx + dx, y0 + dy
                edge = abs(dx) == half or dy in (-6, 0)
                put(px, img, x, y, OUTLINE if edge else (fill0 if dx < 0 else fill1))


def deco_almighty(img: Image.Image, pal: dict) -> None:
    """Schrolli der Allmächtige: hinter dem Medaillon die beiden Schwingen wie in den ersten Gestalten, doch die Spitzen der
    Federn stehen in Flammen (sie züngeln und teilen sich); auf dem Rahmen eine Sonnenkrone – Flammenzungen im Wechsel mit
    goldenen Strahlen –, unter der Leiste tropft flüssiges Gold, dazwischen fallen Funken; als Spitze die Sonne selbst."""
    px = img.load()
    fire = [c(pal, f"flame{k}") for k in range(6)]
    feather, feather_mid, feather_dark = c(pal, "feather"), c(pal, "feather_mid"), c(pal, "feather_dark")
    gold, gold_dark, ray = c(pal, "gold"), c(pal, "gold_dark"), c(pal, "ray")
    sun, sun_dark, sun_light = c(pal, "sun"), c(pal, "sun_dark"), c(pal, "sun_light")
    ember_line = mix(OUTLINE, fire[0], 0.6)
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    wing_fire = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    wf = wing_fire.load()
    tips = []

    def flame_quill(x0: float, y0: float, ang: float, length: float, width: float, seed: int):
        """Eine Feder wie in deco_schrolli, deren Spitze brennt: ab 56 % der Länge schlägt eine Flamme aus ihr, breiter als
        die Feder, die über sie hinaus züngelt (die Flammen aller Federn kommen auf eine eigene Ebene mit Glutkontur)."""
        ca, sa = math.cos(ang), math.sin(ang)
        u_fire, u_end = length * 0.6, length * 1.42
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                dx, dy = x + 0.5 - x0, y + 0.5 - y0
                u = dx * ca + dy * sa
                v = -dx * sa + dy * ca
                if u < 0.0 or u > u_end:
                    continue
                k = u / length
                if u < u_fire + 1.2:
                    half = width * (0.55 + 0.45 * math.sin(math.pi * min(1.0, k * 1.15)))
                    if abs(v) <= half:
                        if abs(v) > half - 0.75:
                            col = OUTLINE
                        elif abs(v) < 0.45:
                            col = mix(feather_mid, feather_dark, 0.3)
                        else:
                            lit = 0.35 + 0.65 * (1.0 - (v + half) / (2 * half))
                            col = mix(feather_mid, feather, lit)
                            if (u * 1.3 + seed) % 5.0 < 0.6:
                                col = mix(col, feather_mid, 0.5)
                        put(px, img, x, y, col)
                if u >= u_fire:
                    t = (u - u_fire) / (u_end - u_fire)
                    vv = v - 1.5 * math.sin(t * 4.6 + seed * 1.9) * t
                    half = width * 1.02 * (1.0 - t) ** 0.8 * (0.86 + 0.24 * math.sin(t * 8.0 + seed))
                    if abs(vv) <= half and half > 0.3 and not (t > 0.5 and abs(vv + 0.4) < (t - 0.5) * half * 1.7):
                        core = 1.0 - abs(vv) / half
                        wf[x, y] = _ramp(fire[1:], 0.92 - 0.72 * t + 0.4 * core)
        tips.append((x0 + ca * u_end, y0 + sa * u_end))

    # die erhobene Schwinge: fünf Federn (flacher als in den ersten Gestalten, damit die Flammen Platz haben)
    for j in range(5):
        k = j / 4
        flame_quill(EMB_X + 12 + 3 * j, EMB_Y - 9 - j * 0.6, math.radians(-40 + 20 * k), 12.5 + 9.5 * k, 3.1 - 0.3 * k, j)
    # die gesenkte Schwinge: vier Federn
    for j in range(4):
        k = j / 3
        flame_quill(EMB_X + 12 + 3 * j, EMB_Y + 8 + j * 0.5, math.radians(36 - 18 * k), 11.5 + 7.5 * k, 2.8 - 0.3 * k, 20 + j)
    _outline(wing_fire, ember_line)
    img.alpha_composite(wing_fire)
    px = img.load()
    # Funken, die von den brennenden Spitzen stieben
    for i, (tx, ty) in enumerate(tips):
        for n in range(2):
            sx = round(tx + 1.5 + 3.0 * hash01(i, n, 341))
            sy = round(ty - 1.0 - 3.0 * hash01(i, n, 343))
            if 0 <= sy < FRAME_H and not (BAR_X - 3 <= sx and BAR_Y - 3 <= sy <= BAR_Y + BAR_H + 2):
                put(px, img, sx, sy, fire[5] if n == 0 else fire[3])

    # die Sonnenkrone auf dem Rahmen: Flammenzungen im Wechsel mit goldenen Strahlen (nicht über den Fassungen)
    gems = [BAR_X + round(BAR_W * f) for f in GEMS]
    flames = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    fp = flames.load()

    def tongue(cx: float, base: int, h: int, w: float, lean: float, seed: int):
        for k in range(h):
            t = k / max(1, h - 1)
            half = (w / 2) * (1.0 - t) ** 0.65 * (0.92 + 0.16 * math.sin(k * 1.9 + seed))
            mid = cx + lean * t ** 1.4 + 0.5 * math.sin(k * 0.9 + seed) * t
            for x in range(round(mid - half), round(mid + half) + 1):
                d = abs(x - mid) / max(0.6, half + 0.5)
                heat = 1.12 - t * 0.8 - d * 0.5
                y = base - k
                if 0 <= x < FRAME_W and 0 <= y < FRAME_H:
                    fp[x, y] = _ramp(fire[1:], heat)

    rays = []
    for i, x in enumerate(range(BAR_X + 22, BAR_X + BAR_W - 2, 5)):
        if any(abs(x - g) < 8 for g in gems):
            continue
        if i % 2 == 0:
            tongue(x, top - 1, 4 + (2 if i % 4 == 0 else 0) + round(hash01(x, 301)), 4.6, (hash01(x, 303) - 0.5) * 1.6, i)
        else:
            rays.append((x, 7 + (2 if i % 4 == 1 else 0) + round(hash01(x, 305))))
    for x, h in rays:
        for dy in range(h):
            half = 1 if dy < h - 3 else 0
            for dx in range(-half, half + 1):
                col = OUTLINE if abs(dx) == half and half > 0 else (ray if dx <= 0 else gold)
                if dy >= h - 2:
                    col = c(pal, "f3")
                put(px, img, x + dx, top - 1 - dy, col)
    _outline(flames, ember_line)
    img.alpha_composite(flames)
    px = img.load()
    # kleine Funken über der Krone
    for x, h in rays:
        if hash01(x, 307) > 0.55:
            sx, sy = x + (3 if hash01(x, 309) > 0.5 else -3), top - h - 1
            put(px, img, sx, sy, fire[5])
            if hash01(x, 311) > 0.5:
                for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    put(px, img, sx + ox, sy + oy, fire[3])

    # unter der Leiste: flüssiges Gold, das in glühenden Tropfen abreißt, dazwischen Funken
    for i, x in enumerate(range(BAR_X + 4, BAR_X + BAR_W - 2, 9)):
        length = 2 + round(3 * hash01(x, 321))
        for dy in range(length):
            t = dy / max(1, length - 1)
            put(px, img, x, bottom + 1 + dy, _ramp((gold, fire[4], fire[5]), t))
            put(px, img, x + 1, bottom + 1 + dy, gold_dark if t < 0.7 else fire[3])
        if hash01(x, 323) > 0.4:
            dy = length + 1 + round(2 * hash01(x, 325))
            put(px, img, x, bottom + 1 + dy, fire[5])
            put(px, img, x + 1, bottom + 1 + dy, fire[3])
            put(px, img, x, bottom + 2 + dy, fire[3])
            put(px, img, x + 1, bottom + 2 + dy, fire[2])
        if hash01(x, 327) > 0.6:
            put(px, img, x + 4, bottom + 4 + round(5 * hash01(x, 329)), fire[4] if hash01(x, 331) > 0.5 else fire[3])

    # Spitze: die Sonne – eine glühende Scheibe mit einem Kranz aus langen und kurzen Strahlen
    ex, ey = BAR_X + BAR_W + 4, BAR_Y + BAR_H // 2
    for k in range(16):
        a = math.radians(k * 22.5)
        r = 5.6 if k % 4 == 0 else (4.8 if k % 2 == 0 else 4.2)
        ca, sa = math.cos(a), math.sin(a)
        steps = int(r * 3)
        for s in range(steps + 1):
            rr = 2.6 + (r - 2.6) * s / steps
            put(px, img, round(ex + ca * rr), round(ey + sa * rr), fire[3] if rr < r - 0.8 else fire[4])
    for y in range(ey - 4, ey + 5):
        for x in range(ex - 4, ex + 5):
            d = math.hypot(x - ex, y - ey)
            if d <= 3.0:
                col = _ramp((fire[5], sun_light, sun, sun_dark), d / 3.0)
                if d > 2.4:
                    col = mix(col, ember_line, 0.55)
                put(px, img, x, y, col)
    put(px, img, ex - 1, ey - 1, (255, 255, 255, 255))


def post_almighty(img: Image.Image, pal: dict) -> None:
    """Nach dem Medaillon: sieben Sonnensteine auf seinem Ring (wo in der Wahren Gestalt die Augen waren)."""
    px = img.load()
    for k in range(7):
        a = math.radians(-150 + k * 50)
        x = round(EMB_X - 0.5 + math.cos(a) * (EMB_R - 2.5))
        y = round(EMB_Y - 0.5 + math.sin(a) * (EMB_R - 2.5))
        put(px, img, x - 1, y, c(pal, "flame3"))
        put(px, img, x + 1, y, c(pal, "sun_dark"))
        put(px, img, x, y - 1, c(pal, "flame4"))
        put(px, img, x, y + 1, c(pal, "sun_dark"))
        put(px, img, x, y, c(pal, "flame5"))


def frame_border_stone(img: Image.Image, pal: dict) -> None:
    """Rahmen des Weltenfressers: Platten aus Chaosgestein (jede an ihrem Anfang heller, wie gewölbt), dazwischen glühende
    Fugen wie die Nähte seiner Glieder, ein paar glimmende Risse; dazu die Fassungen der Edelsteine."""
    px = img.load()
    m0, m1, m2 = c(pal, "m0"), c(pal, "m1"), c(pal, "m2")
    seam0, seam1, seam2 = c(pal, "seam0"), c(pal, "seam1"), c(pal, "seam2")
    x0, x1, y0, y1 = BAR_X - 3, BAR_X + BAR_W + 2, BAR_Y - 3, BAR_Y + BAR_H + 2
    joints = [x for x in range(x0 + 15, x1 - 5, 16) if not any(abs(x - (BAR_X + round(BAR_W * f))) < 6 for f in GEMS)]
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if BAR_X <= x < BAR_X + BAR_W and BAR_Y <= y < BAR_Y + BAR_H:
                continue
            ring = min(x - x0, x1 - x, y - y0, y1 - y)
            prev = max([j for j in joints if j <= x], default=x0)
            along = (x - prev) / 16.0                                   # 0 am Anfang einer Platte
            n = hash01(x // 2, y, 401)
            if ring == 0:
                col = OUTLINE
            elif ring == 1:
                upper = (y - y0 <= 1 or x - x0 <= 1) and not (y1 - y <= 1)
                col = mix(m2, m1, 0.25 + 0.5 * along + 0.2 * n) if upper else mix(m1, m0, 0.3 + 0.4 * along + 0.2 * n)
            else:
                col = mix(m0, OUTLINE, 0.25 + 0.2 * n)
            if x in joints and ring >= 1:
                col = seam2 if ring == 2 else seam1
            elif ring >= 1 and (x - 1 in joints or x + 1 in joints):
                col = mix(col, seam0, 0.5)
            px[x, y] = col
    # glimmende Risse in der oberen und unteren Kante
    for i, x in enumerate(range(x0 + 7, x1 - 4, 23)):
        if hash01(x, 403) < 0.5:
            continue
        y = y0 + 1 if i % 2 == 0 else y1 - 1
        for k in range(3):
            put(px, img, x + k, y + (1 if (k == 1 and y == y0 + 1) else (-1 if k == 1 else 0)), seam1 if k != 1 else seam2)
    _gem_sockets(px, img, pal, mix(m1, m0, 0.2), mix(m0, OUTLINE, 0.2))


def deco_world_eater(img: Image.Image, pal: dict) -> None:
    """Der Weltenfresser: hinter dem Medaillon oben drei seiner Kronensplitter (violette Kristalle in goldener Fassung),
    unten ein gebogener Hauer; auf dem Rahmen – seinem Rücken – Stücke verschlungener Welten (Wiese mit Eiche, Netherrack mit
    Magma und Karmesinpilz, Endstein mit Chorus, Packeis mit Zacken), dazwischen Glut; unter der Leiste sein Gebiss (Zahnfleisch
    und Zähne, die Fänge zur Mitte gebogen); als Spitze die Kristallflosse seines Schwanzes."""
    px = img.load()
    g = {k: c(pal, k) for k in pal}
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    # die Kronensplitter: violette Kristalle, wo sie hinter dem Ring hervortreten in Gold gefasst
    shards = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    sp = shards.load()

    def shard(x0: float, y0: float, ang: float, length: float, width: float, seed: int):
        ca, sa = math.cos(ang), math.sin(ang)
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                dx, dy = x + 0.5 - x0, y + 0.5 - y0
                u = dx * ca + dy * sa
                v = -dx * sa + dy * ca
                if u < 0.0 or u > length:
                    continue
                k = u / length
                half = width if k < 0.7 else width * (1.0 - (k - 0.7) / 0.3) + 0.15
                if abs(v) > half:
                    continue
                ring = math.hypot(x + 0.5 - EMB_X, y + 0.5 - EMB_Y)
                if EMB_R - 0.5 <= ring <= EMB_R + 2.2:
                    col = g["gold"] if v < 0.3 else g["gold_dark"]
                    if abs(v) > half - 0.8 or abs(ring - (EMB_R + 2.2)) < 0.6:
                        col = mix(g["gold_dark"], OUTLINE, 0.4)
                elif v < -half * 0.2:
                    col = g["crystal2"]
                elif v < half * 0.15:
                    col = g["crystal3"] if k > 0.25 else g["crystal2"]
                else:
                    col = g["crystal1"]
                if abs(v) > half - 0.7 and not (EMB_R - 0.5 <= ring <= EMB_R + 2.2):
                    col = mix(col, g["crystal0"], 0.55)
                if k > 0.8 and v < 0.5:
                    col = mix(col, g["crystal3"], 0.5)
                if hash01(x, y, seed, 411) > 0.94 and abs(v) < half - 0.8:
                    col = g["crystal3"]
                sp[x, y] = col

    shard(29.0, 12.0, math.radians(-60), 14.0, 2.4, 1)
    shard(33.0, 12.5, math.radians(-40), 19.0, 2.8, 2)
    shard(36.5, 14.0, math.radians(-22), 21.0, 2.4, 3)
    _outline(shards, OUTLINE)
    img.alpha_composite(shards)
    px = img.load()

    # der Hauer: von hinter dem Medaillon nach unten rechts gebogen, die Spitze wieder nach oben
    tusk = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    tp = tusk.load()
    p0, p1, p2 = (30.0, 33.0), (45.0, 46.5), (61.0, 34.5)
    samples = []
    for i in range(241):
        t = i / 240
        bx = (1 - t) ** 2 * p0[0] + 2 * (1 - t) * t * p1[0] + t * t * p2[0]
        by = (1 - t) ** 2 * p0[1] + 2 * (1 - t) * t * p1[1] + t * t * p2[1]
        tx = 2 * (1 - t) * (p1[0] - p0[0]) + 2 * t * (p2[0] - p1[0])
        ty = 2 * (1 - t) * (p1[1] - p0[1]) + 2 * t * (p2[1] - p1[1])
        n = math.hypot(tx, ty)
        samples.append((t, bx, by, -ty / n, tx / n, 2.9 * (1.0 - t) ** 0.8 + 0.25))
    for y in range(26, FRAME_H):
        for x in range(24, 66):
            best = None
            for (t, bx, by, nx, ny, r) in samples:
                d = math.hypot(x + 0.5 - bx, y + 0.5 - by)
                if d <= r and (best is None or d - r < best[0]):
                    side = (x + 0.5 - bx) * nx + (y + 0.5 - by) * ny
                    best = (d - r, t, side / r)
            if best is None:
                continue
            _, t, side = best
            col = _ramp((g["tooth_light"], g["tooth"], g["tooth_dark"]), 0.5 + 0.55 * side)
            if any(abs(t - b) < 0.012 for b in (0.3, 0.46)):
                col = mix(col, g["tooth_dark"], 0.6)
            if t < 0.12:
                col = mix(col, g["gum_dark"], 0.5)
            tp[x, y] = col
    _outline(tusk, OUTLINE)
    img.alpha_composite(tusk)
    px = img.load()

    # die verschlungenen Welten auf seinem Rücken
    worlds = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    wp = worlds.load()
    base = top - 1

    def wput(x, y, col):
        if 0 <= x < FRAME_W and 0 <= y < FRAME_H:
            wp[x, y] = col

    def chunk(cx: int, w: int, h: int, surface, surface_light, body, body_dark, seed: int):
        """Ein Brocken Welt auf dem Rücken: oben die Oberfläche (zwei Reihen), darunter Gestein, unregelmäßig gebrochen."""
        x0 = cx - w // 2
        for x in range(x0, x0 + w):
            rel = (x - x0) / max(1, w - 1)
            hh = h - (1 if (rel < 0.12 or rel > 0.88) else 0) - (1 if hash01(x, seed, 421) > 0.8 else 0)
            for k in range(hh):
                y = base - k
                if k >= hh - 1:
                    col = surface_light if hash01(x, seed, 423) > 0.45 else surface
                elif k >= hh - 2:
                    col = surface if hash01(x, k, seed, 425) > 0.25 else body
                else:
                    col = body if hash01(x, k, seed, 427) > 0.3 else body_dark
                    if k == 0:
                        col = mix(col, body_dark, 0.5)
                wput(x, y, col)
        return x0

    def surface_y(x: int, cx: int, w: int, h: int, seed: int) -> int:
        x0 = cx - w // 2
        rel = (x - x0) / max(1, w - 1)
        hh = h - (1 if (rel < 0.12 or rel > 0.88) else 0) - (1 if hash01(x, seed, 421) > 0.8 else 0)
        return base - hh

    # Wiese mit Eiche und Blume
    cx, w, h, s = 75, 17, 4, 1
    chunk(cx, w, h, g["grass"], g["grass_light"], g["dirt"], g["dirt_dark"], s)
    tx = cx - 1
    ty = surface_y(tx, cx, w, h, s)
    for k in range(4):
        wput(tx, ty - k, g["wood"])
        wput(tx + 1, ty - k, g["wood_dark"])
    for y in range(ty - 11, ty - 3):
        for x in range(tx - 5, tx + 7):
            d = math.hypot((x - tx - 0.5) / 5.3, (y - (ty - 6.8)) / 3.7)
            if d > 1.0 or (d > 0.86 and hash01(x, y, 431) > 0.5):
                continue
            lit = (x - tx) < 0 and y < ty - 6
            col = g["leaves_light"] if (lit and hash01(x, y, 433) > 0.3) else g["leaves"]
            if hash01(x, y, 435) > 0.82:
                col = mix(col, g["leaves_light"], 0.6)
            wput(x, y, col)
    fx = cx + 4
    fy = surface_y(fx, cx, w, h, s)
    wput(fx, fy, g["grass_light"])
    wput(fx, fy - 1, (230, 56, 48, 255))
    wput(cx - 6, surface_y(cx - 6, cx, w, h, s), g["grass_light"])
    wput(cx + 6, surface_y(cx + 6, cx, w, h, s), (255, 214, 64, 255))

    # Netherrack mit einem Becken aus Magma, einem Karmesinpilz und Flämmchen
    cx, w, h, s = 131, 17, 4, 2
    chunk(cx, w, h, g["netherrack"], g["fungus"], g["netherrack"], g["netherrack_dark"], s)
    for x in range(cx - 1, cx + 4):
        y = surface_y(x, cx, w, h, s) + 1
        wput(x, y, g["magma_light"] if x in (cx, cx + 1) else g["magma"])
        wput(x, y + 1, g["magma"] if x in (cx, cx + 2) else g["netherrack_dark"])
    px_ = cx - 4
    py_ = surface_y(px_, cx, w, h, s)
    for k in range(3):
        wput(px_, py_ - k, (226, 196, 150, 255))
    for x in range(px_ - 3, px_ + 4):
        wput(x, py_ - 3, g["fungus"])
    for x in range(px_ - 2, px_ + 3):
        wput(x, py_ - 4, g["fungus_light"] if x < px_ else g["fungus"])
    wput(px_ - 2, py_ - 3, (255, 222, 120, 255))
    wput(px_ + 2, py_ - 4, (255, 222, 120, 255))
    for fxp, fh in ((cx + 5, 3), (cx + 6, 2)):
        fy = surface_y(fxp, cx, w, h, s)
        for k in range(fh):
            wput(fxp, fy - k, (255, 236, 140, 255) if k == 0 else (255, 150, 40, 255))

    # Endstein mit einer Chorus-Pflanze
    cx, w, h, s = 186, 16, 4, 3
    chunk(cx, w, h, g["endstone"], g["endstone"], g["endstone"], g["endstone_dark"], s)
    sx = cx
    sy = surface_y(sx, cx, w, h, s)
    stem = [(0, 0), (0, -1), (0, -2), (0, -3), (0, -4), (-1, -4), (-2, -4), (-2, -5), (-2, -6), (1, -2), (2, -2), (2, -3),
            (2, -4), (2, -5), (2, -6), (2, -7), (0, -5), (0, -6), (0, -7), (0, -8)]
    for ox, oy in stem:
        wput(sx + ox, sy + oy, g["chorus"])
    for ox, oy in ((-2, -7), (2, -8), (0, -9)):
        for bx, by in ((0, 0), (1, 0), (0, -1), (1, -1)):
            wput(sx + ox + bx - (1 if ox < 0 else 0), sy + oy + by, g["chorus_light"] if (bx, by) != (1, 0) else g["chorus"])
    # Packeis mit Eiszacken
    cx, w, h, s = 215, 16, 4, 4
    chunk(cx, w, h, g["ice_light"], g["ice_light"], g["ice"], g["ice_dark"], s)
    for ox, sh in ((-5, 4), (-2, 6), (0, 9), (2, 5), (5, 3)):
        x = cx + ox
        y = surface_y(x, cx, w, h, s)
        for k in range(sh):
            wput(x, y - k, g["ice_light"] if k > sh - 3 else g["ice"])
            if k < sh - 3:
                wput(x + 1, y - k, g["ice_dark"])
    _outline(worlds, OUTLINE)
    img.alpha_composite(worlds)
    px = img.load()
    # Glut zwischen den Welten
    for i, x in enumerate(range(BAR_X + 6, BAR_X + BAR_W - 4, 7)):
        if hash01(x, 441) > 0.62 and not any(abs(x - X) < 11 for X in (75, 131, 186, 215, 103, 157)):
            y = top - 2 - round(4 * hash01(x, 443))
            put(px, img, x, y, g["seam2"] if hash01(x, 445) > 0.5 else g["seam1"])

    # das Gebiss unter der Leiste: Zahnfleisch, daraus Zähne; die großen Fänge zur Mitte gebogen
    teeth = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    tq = teeth.load()
    for x in range(BAR_X - 1, BAR_X + BAR_W + 1):
        tq[x, bottom + 1] = g["gum"] if hash01(x, 451) > 0.2 else g["gum_dark"]
    mid = BAR_X + BAR_W / 2
    for i, x in enumerate(range(BAR_X + 4, BAR_X + BAR_W - 2, 6)):
        big = i % 3 == 1
        width = 4.6 if big else 3.0
        length = (7 if big else 3) + (1 if hash01(x, 453) > 0.6 else 0)
        bend = (1.6 if big else 0.4) * (1 if x < mid else -1)
        for k in range(length):
            t = k / max(1, length - 1)
            half = (width / 2) * (1.0 - t) ** 0.85
            cxk = x + bend * t * t
            for xx in range(round(cxk - half), round(cxk + half) + 1):
                d = (xx - cxk) / max(0.5, half + 0.5)
                col = _ramp((g["tooth_light"], g["tooth"], g["tooth_dark"]), 0.45 + 0.6 * d)
                if k == 0:
                    col = mix(col, g["gum"], 0.35)
                tq[xx, bottom + 2 + k] = col
    _outline(teeth, OUTLINE)
    img.alpha_composite(teeth)
    px = img.load()

    # Spitze: die Kristallflosse am Schwanz – zwei Schwingen aus Kristall, dazwischen ein Dorn
    fin = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    fq = fin.load()
    ex, ey = BAR_X + BAR_W + 2, BAR_Y + BAR_H // 2
    for y in range(ey - 12, ey + 13):
        for x in range(ex, FRAME_W - 1):
            rx = x - ex
            dy = y + 0.5 - (ey + 0.5)
            reach = 3.2 + rx * 1.55
            if abs(dy) > reach or abs(dy) < rx * 0.95 - 1.2:
                continue
            if rx > 5 and abs(dy) < reach - 3.0:
                continue
            outer = abs(dy) / max(1.0, reach)
            col = _ramp((g["crystal1"], g["crystal2"], g["crystal3"]), 0.2 + 0.7 * outer)
            if dy > 0:
                col = mix(col, g["crystal0"], 0.3)
            fq[x, y] = col
    for x in range(ex, ex + 6):
        fq[x, ey] = g["crystal3"] if x > ex + 2 else g["crystal2"]
    _outline(fin, OUTLINE)
    img.alpha_composite(fin)


def post_world_eater(img: Image.Image, pal: dict) -> None:
    """Nach dem Medaillon: der Ring ist sein Rachen – acht Lippen aus Chaosgestein mit glühenden Fugen dazwischen."""
    px = img.load()
    seam1, seam2 = c(pal, "seam1"), c(pal, "seam2")
    for y in range(FRAME_H):
        for x in range(0, int(EMB_X + EMB_R) + 2):
            d = math.hypot(x - EMB_X + 0.5, y - EMB_Y + 0.5)
            if not (EMB_R - 4.0 < d <= EMB_R - 1.0):
                continue
            ang = math.degrees(math.atan2(y - EMB_Y + 0.5, x - EMB_X + 0.5)) % 45.0
            if ang < 3.2 or ang > 45.0 - 3.2:
                px[x, y] = seam2 if EMB_R - 3.0 < d <= EMB_R - 2.0 else seam1


POST = {"schrolli": post_schrolli, "schrolli_true": post_schrolli, "schrolli_almighty": post_almighty,
        "world_eater": post_world_eater}
BORDERS = {"world_eater": frame_border_stone}


DECOS = {"schrolli": deco_schrolli, "schrolli_true": deco_schrolli, "schrolli_almighty": deco_almighty,
         "world_eater": deco_world_eater, "anni": deco_anni, "anni2": deco_anni, "mystic": deco_mystic, "star": deco_star, "forge": deco_forge, "moth": deco_moth, "void": deco_void, "reef": deco_reef, "shadow": deco_shadow, "fire": deco_fire, "ice": deco_ice, "lightning": deco_lightning, "nature": deco_nature, "earth": deco_earth,
         "water": deco_water}


def frame(element: str) -> Image.Image:
    pal = ELEMENTS[element]
    img = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    DECOS[element](img, pal)
    BORDERS.get(element, frame_border)(img, pal)
    emblem(img, pal)
    if element in POST:
        POST[element](img, pal)
    return img


# =============================================================================================
# Füllung, leere Leiste, Spur, Glanz, Aura
# =============================================================================================

def fill_value(element: str, x: int, y: int) -> float:
    tau = math.tau
    if element == "fire":
        return 0.5 + 0.5 * math.sin(tau * x / 32 + 1.6 * math.sin(tau * y / 9 + tau * x / 64))
    if element == "ice":
        band = ((x + 2 * y) // 6) % 4
        return (0.75 if band == 0 else 0.3) + 0.15 * math.sin(tau * x / 64)
    if element == "lightning":
        u = (x / 8.0) % 2.0
        tri = u if u < 1.0 else 2.0 - u                     # Zickzack-Bogen quer durch die Leiste
        arc = 1.5 + 5.0 * tri + (1.0 if (x // 16) % 2 else 0.0)
        d = abs(y - arc)
        return 1.0 if d < 0.6 else (0.72 if d < 1.6 else 0.32 + 0.08 * math.sin(tau * x / 32))
    if element == "nature":
        vein = ((x - 2 * y) % 16) < 2
        return 0.75 if vein else 0.4 + 0.15 * math.sin(tau * x / 32) * math.cos(tau * y / 9)
    if element == "earth":
        return 0.35 + 0.4 * hash01(x // 2, y // 2, 7) * (0.5 + 0.5 * math.sin(tau * x / 64))
    if element == "water":
        return 0.5 + 0.5 * math.sin(tau * (x / 32 - y / 9))
    if element == "void":
        # ein Sternennebel: weiche Schleier, darin einzelne helle Sterne
        neb = 0.4 + 0.2 * math.sin(tau * x / 64 + 1.4 * math.sin(tau * y / 9)) + 0.12 * math.sin(tau * x / 32 - tau * y / 18)
        return 1.0 if hash01(x, y, 83) > 0.965 else neb
    if element == "reef":
        # Licht einer Lagune: ein schwankendes Netz heller Linien, dazwischen einzelne Blasen
        line1 = abs(math.sin(math.pi * (x / 16 + 0.35 * math.sin(tau * (y / 18 + x / 64)))))
        line2 = abs(math.sin(math.pi * (-x / 16 + y / 4.5 + 0.3 * math.sin(tau * x / 32))))
        near = min(line1, line2)
        if hash01(x, y, 91) > 0.975:
            return 1.0
        return 0.92 if near < 0.14 else (0.66 if near < 0.3 else 0.36 + 0.1 * math.sin(tau * x / 64))
    if element == "star":
        # Nachthimmel: Nebelschleier, die langsam ziehen, darin funkelnde Sterne und ab und zu ein heller Stern
        neb = 0.32 + 0.18 * math.sin(tau * x / 64 + 1.2 * math.sin(tau * y / 9)) + 0.1 * math.sin(tau * x / 32 + tau * y / 18)
        h = hash01(x, y, 59)
        if h > 0.97:
            return 1.0
        if h > 0.94:
            return 0.8
        return neb
    if element == "forge":
        # fließendes, glühendes Metall: langsame Wellen, darin helle Risse und einzelne Funken
        flow = 0.46 + 0.2 * math.sin(tau * x / 64 + 1.3 * math.sin(tau * y / 9 + tau * x / 128))
        crack = abs(math.sin(math.pi * (x / 13 + 0.45 * math.sin(tau * (y / 9 + x / 48)))))
        if hash01(x, y, 47) > 0.975:
            return 1.0
        return 0.9 if crack < 0.1 else (0.7 if crack < 0.22 else flow)
    if element == "moth":
        # Flügelstaub: versetzte Reihen kleiner Schuppen (Bögen), dazwischen Glitzer
        row = y // 3
        sx = (x + (row % 2) * 3) % 6
        arc = abs(sx - 2.5) / 2.5
        scale = 0.42 + 0.3 * (1.0 - arc) * (1.0 if y % 3 else 0.6)
        if hash01(x, y, 61) > 0.97:
            return 1.0
        return scale + 0.08 * math.sin(tau * x / 64)
    if element == "mystic":
        # Harlekin-Rauten aus Samt im Wechsel hell und dunkel, darin funkelnde Pailletten
        a = (x / 8.0 + y / 4.5) % 2.0
        b = (x / 8.0 - y / 4.5) % 2.0
        cell = (int(a) + int(b)) % 2
        edge = min(a % 1.0, 1.0 - a % 1.0, b % 1.0, 1.0 - b % 1.0)
        if hash01(x, y, 67) > 0.972:
            return 1.0
        if edge < 0.07:
            return 0.82
        return 0.62 if cell else 0.36
    if element in ("anni", "anni2"):
        # ein kaputtes Bild: Zeilen, verrutschte Blöcke (4 x 1), dazwischen helle Fehlerpixel
        block = hash01(x // 4 + (y % 3) * 7, y, 71)
        if hash01(x, y, 73) > 0.975:
            return 1.0
        if y % 3 == 0:
            return 0.3
        return 0.4 + block * 0.45
    if element == "schrolli":
        # strömendes goldenes Licht: weiche Wellen, schräge Strahlen und Funkeln
        flow = 0.48 + 0.16 * math.sin(tau * x / 64 + 1.2 * math.sin(tau * y / 9)) + 0.08 * math.sin(tau * x / 32 - tau * y / 18)
        beam = ((x + 2 * y) % 32) < 2
        if hash01(x, y, 227) > 0.972:
            return 1.0
        return 0.88 if beam else flow
    if element == "schrolli_true":
        # das Weltall: violette Nebel, die ziehen, Sterne und ab und zu ein heller Stern
        neb = 0.36 + 0.2 * math.sin(tau * x / 64 + 1.5 * math.sin(tau * y / 9)) + 0.12 * math.sin(tau * x / 32 + tau * y / 9)
        h = hash01(x, y, 229)
        if h > 0.968:
            return 1.0
        if h > 0.94:
            return 0.78
        return neb
    if element == "schrolli_almighty":
        # das Plasma einer Sonne: wogende Glut mit feiner Körnung, darin Bögen aus Licht und einzelne Funken
        flow = 0.5 + 0.5 * math.sin(tau * x / 64 + 1.5 * math.sin(tau * y / 9 + tau * x / 128))
        d1, d2 = _worley(x, y * 1.2, 4.0, 64, 431)
        grain = min(1.0, (d2 - d1) / 2.0)
        arc = abs(y - (4.0 + 3.0 * math.sin(tau * x / 64 + 0.7)))
        if arc < 0.6 and math.sin(tau * x / 128) > 0.2:
            return 1.0
        if hash01(x, y, 433) > 0.978:
            return 1.0
        return 0.36 + 0.28 * flow + 0.14 * grain + (0.12 if arc < 1.5 and math.sin(tau * x / 128) > 0.2 else 0.0)
    if element == "world_eater":
        # Krusten auf Glut: zwischen den Schollen fließt es hell
        d1, d2 = _worley(x, y * 1.35, 256 / 24, 24, 451)
        crack = d2 - d1
        return 0.95 if crack < 0.75 else (0.7 if crack < 1.9 else 0.3 + 0.1 * math.sin(tau * x / 64) + 0.06 * hash01(x, y, 453))
    if element == "shadow":
        # wabernder Rauch, durch den einzelne Seelen ziehen
        smoke = 0.42 + 0.22 * math.sin(tau * x / 64 + 2.0 * math.sin(tau * y / 9 + tau * x / 32))
        return 1.0 if hash01(x // 2, y // 3, 29) > 0.93 else smoke
    return 0.5


def fill_strip_world_eater() -> Image.Image:
    """Füllung des Weltenfressers: die Glut seines Schlunds – dunkle Krusten, zwischen denen es hell fließt, und ein violetter
    Riss, der sich hindurchzieht und funkelt (wie der Riss in seinem Rachen)."""
    pal = ELEMENTS["world_eater"]
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    cr1, cr2, cr3 = c(pal, "crystal1"), c(pal, "crystal2"), c(pal, "crystal3")
    tau = math.tau
    img = Image.new("RGBA", (256, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        top = 1.0 - y / (BAR_H - 1)
        for x in range(256):
            v = fill_value("world_eater", x, y)
            flow = 0.5 + 0.5 * math.sin(tau * x / 64 + 1.3 * math.sin(tau * y / 9 + tau * x / 128))
            if v > 0.9:
                col = mix(f2, f3, 0.45 + 0.3 * flow)
            elif v > 0.6:
                col = mix(f1, f2, 0.5 + 0.3 * flow)
            else:
                col = mix(f0, f1, 0.2 + 0.4 * top + 0.25 * flow)
            gate = math.sin(tau * x / 128 + 0.3)
            if gate > 0.2:
                # der Riss: ein gezackter Spalt, aus dem violettes Licht dringt
                ry = 4.0 + 2.2 * math.sin(tau * x / 64 + 0.8) + (hash01(x // 3, 471) - 0.5) * 1.8
                d = abs(y - ry)
                if d < 0.6:
                    col = cr3 if hash01(x, 461) > 0.55 else cr2
                elif d < 1.6:
                    col = mix(col, cr1, 0.6 * min(1.0, (gate - 0.2) * 4))
                elif d < 2.6 and hash01(x, y, 463) > 0.9:
                    col = cr2
            if y == 1:
                col = mix(col, f3, 0.25)
            if y == BAR_H - 1:
                col = mix(col, f0, 0.5)
            px[x, y] = col
    return img


def fill_strip(element: str) -> Image.Image:
    if element == "world_eater":
        return fill_strip_world_eater()
    pal = ELEMENTS[element]
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    img = Image.new("RGBA", (256, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        top = 1.0 - y / (BAR_H - 1)
        for x in range(256):
            v = fill_value(element, x, y)
            base = mix(f0, f1, 0.35 + 0.65 * top)
            col = mix(base, f2, max(0.0, v - 0.35) * 1.2)
            if v > 0.85:
                col = mix(col, f3, (v - 0.85) / 0.15 * 0.8)
            if y == 1:
                col = mix(col, f3, 0.35)
            if y == BAR_H - 1:
                col = mix(col, f0, 0.5)
            px[x, y] = col
    return img


def empty_strip(element: str) -> Image.Image:
    pal = ELEMENTS[element]
    deep = c(pal, "deep")
    img = Image.new("RGBA", (BAR_W, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        for x in range(BAR_W):
            v = 0.25 + 0.1 * fill_value(element, x, y)
            col = mix(OUTLINE, deep, v * 1.6)
            if y == 0:
                col = mix(col, OUTLINE, 0.6)
            elif y == BAR_H - 1:
                col = mix(col, deep, 0.5)
            px[x, y] = col
    return img


def trail_strip(element: str) -> Image.Image:
    pal = ELEMENTS[element]
    glow = c(pal, "glow")
    img = Image.new("RGBA", (BAR_W, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        for x in range(BAR_W):
            a = 210 if 0 < y < BAR_H - 1 else 150
            px[x, y] = glow[:3] + (a,)
    return img


def shine_strip() -> Image.Image:
    img = Image.new("RGBA", (16, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        for x in range(16):
            d = abs((x - y * 0.7) - 6.0)
            if d < 3.5:
                px[x, y] = (255, 255, 255, int(255 * (1 - d / 3.5) * 0.8))
    return img


def aura(element: str, frame_img: Image.Image) -> Image.Image:
    """Weicher Schein rund um Rahmen und Medaillon (für die letzte Phase)."""
    pal = ELEMENTS[element]
    glow, acc = c(pal, "glow"), c(pal, "acc")
    src = frame_img.load()
    img = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    px = img.load()
    solid = [[src[x, y][3] > 0 for y in range(FRAME_H)] for x in range(FRAME_W)]
    for y in range(FRAME_H):
        for x in range(FRAME_W):
            if solid[x][y]:
                continue
            best = 99.0
            for dy in range(-4, 5):
                for dx in range(-4, 5):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < FRAME_W and 0 <= ny < FRAME_H and solid[nx][ny]:
                        best = min(best, math.hypot(dx, dy))
            if best < 4.5:
                t = 1.0 - best / 4.5
                px[x, y] = mix(acc, glow, t)[:3] + (int(230 * t ** 1.5),)
    return img


# =============================================================================================
# Bildnis aus dem 3D-Modell
# =============================================================================================

def _sun_behind(head: Image.Image) -> Image.Image:
    """Bildnis des Allmächtigen: hinter seinem Kopf lodert eine Sonne aus Flammenzungen; der Kopf bekommt eine dunkle Kontur,
    damit sich Krone und Haar von den Flammen abheben."""
    pal = ELEMENTS["schrolli_almighty"]
    fire = [c(pal, f"flame{k}") for k in range(6)]
    out = Image.new("RGBA", (64, 64), CLEAR)
    op = out.load()
    hp = head.load()
    cx, cy = 32.0, 27.0
    for y in range(64):
        for x in range(64):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            r = math.hypot(dx, dy)
            a = math.atan2(dy, dx)
            tongue = 0.5 + 0.5 * math.cos(14 * a + 1.4 * math.sin(3 * a + 0.5))
            reach = 19.0 + 12.0 * tongue ** 1.5 + 2.0 * math.sin(5 * a + 0.4)
            if r > reach:
                continue
            t = r / reach
            col = _ramp((fire[1], fire[2], fire[3], fire[4], fire[5]), 1.05 - 0.95 * t)
            if t > 0.9:
                col = mix(col, fire[0], 0.35)
            op[x, y] = col
    solid = [[hp[x, y][3] > 140 for y in range(64)] for x in range(64)]
    for y in range(64):
        for x in range(64):
            if solid[x][y]:
                op[x, y] = hp[x, y][:3] + (255,)
            elif any(0 <= x + dx < 64 and 0 <= y + dy < 64 and solid[x + dx][y + dy]
                     for dx in (-2, -1, 0, 1, 2) for dy in (-2, -1, 0, 1, 2) if abs(dx) + abs(dy) <= 2):
                op[x, y] = (40, 8, 4, 255)
    return out


def portrait(element: str) -> Image.Image:
    """Kopf des Bosses schräg von vorn, aus dem Modell gerendert und auf 64 x 64 verkleinert, kreisrund beschnitten,
    mit dunkler Kontur (2 Pixel = 1 Pixel im Spiel), damit er sich vom Grund des Medaillons abhebt."""
    from entitygen.models import (asteria, boss_earth, boss_fire, boss_ice, boss_lightning, boss_nature, boss_shadow, boss_water,
                                  glutschmied, miss_mystik, moth_queen, reef_colossus, shattered_king, anni_troll, schrolli,
                                  world_eater)
    from entitygen.paint import paint_model
    from entitygen.preview import render
    mod = {"fire": boss_fire, "ice": boss_ice, "lightning": boss_lightning, "nature": boss_nature, "earth": boss_earth,
           "water": boss_water, "shadow": boss_shadow, "void": shattered_king, "reef": reef_colossus, "moth": moth_queen,
           "forge": glutschmied, "star": asteria, "mystic": miss_mystik, "anni": anni_troll, "anni2": anni_troll,
           "schrolli": schrolli, "schrolli_true": schrolli, "schrolli_almighty": schrolli, "world_eater": world_eater}[element]
    m = mod.build()
    m.pack()
    variant = "anni_troll_v2" if element == "anni2" else element if element.startswith("schrolli") else next(iter(mod.PALETTES))
    tex, glow = paint_model(m, mod.materials(variant), mod.PALETTES[variant], seed=100, params={"variant": variant})
    if element.startswith("schrolli"):
        # Kopf mit Haar, Krone und Heiligenschein (in der Wahren Gestalt mit dem Strahlenkranz), die Schultern
        def strip_schrolli(bone):
            if bone.name.startswith(("wing_", "left_", "right_", "scepter", "orb", "robe", "true_rings", "halo", "true_corona",
                                     "hair_tip", "almighty", "blade")):
                bone.cubes.clear()
            for child in bone.children:
                strip_schrolli(child)
        strip_schrolli(m.root)
    if element == "void":
        # nur Kopf, Bart und Krone (Schultern, Hände und Trümmer würden das kleine Bildnis überladen)
        keep = {"body", "head", "beard", "beard_tip", "crown"} | {f"crown_{k}" for k in range(7)}

        def strip(bone):
            if bone.name not in keep:
                bone.cubes.clear()
            for child in bone.children:
                strip(child)
        strip(m.root)
    if element == "reef":
        # Karkinos von vorn: Panzer mit Riff, Stielaugen, Stirn (ohne Beine, Tangsträhnen und Speer)
        def strip_extra(bone):
            if bone.name.startswith(("kelp", "spear")) or "leg" in bone.name:
                bone.cubes.clear()
            for child in bone.children:
                strip_extra(child)
        strip_extra(m.root)
    if element == "moth":
        # nur Kopf mit Krone und Fühlern, Brust und der Ansatz der Flügel
        def strip_moth(bone):
            if bone.name.startswith(("left_leg", "right_leg", "abdomen", "tail", "left_wing", "right_wing", "left_hind", "right_hind")):
                bone.cubes.clear()
            for child in bone.children:
                strip_moth(child)
        strip_moth(m.root)
    if element == "forge":
        # der Widderschädel mit Hörnern, Flammen und Kiefer (ohne Arme, Waffen und Beine)
        def strip_forge(bone):
            if bone.name.startswith(("left_arm", "right_arm", "left_forearm", "right_forearm", "hammer", "spear", "left_leg",
                                     "right_leg", "apron")):
                bone.cubes.clear()
            for child in bone.children:
                strip_forge(child)
        strip_forge(m.root)
    if element == "star":
        # Asterias Helm mit Krone und Schweif, die Schultern (ohne Rochen, Lanze, Schild und Umhang)
        def strip_star(bone):
            if bone.name not in ("torso", "helm", "crown", "plume", "plume_mid", "plume_tip", "rider"):
                bone.cubes.clear()
            for child in bone.children:
                strip_star(child)
        strip_star(m.root)
    if element == "mystic":
        # Kopf mit Maske, Zylinder, Feder und Pferdeschwanz (ohne Leib, Arme und Flügel)
        def strip_mystic(bone):
            if bone.name not in ("head", "hat", "feather", "ponytail", "ponytail_tip"):
                bone.cubes.clear()
            for child in bone.children:
                strip_mystic(child)
        strip_mystic(m.root)
    if element in ("anni", "anni2"):
        # Kopf mit Grinsen, Bildschirmauge, Ohren und Haarschopf (ohne Leib, Arme und Hammer)
        def strip_anni(bone):
            if bone.name not in ("head", "jaw", "left_ear", "right_ear", "tuft"):
                bone.cubes.clear()
            if bone.name == "tuft":
                # nur der Ansatz des Schopfs (sonst wäre das Gesicht im Bildnis zu klein)
                del bone.cubes[2:]
            for child in bone.children:
                strip_anni(child)
        strip_anni(m.root)
    zoom = {"schrolli": 1.0, "schrolli_true": 1.0, "schrolli_almighty": 1.0, "world_eater": 1.35, "nature": 0.82, "earth": 1.15, "water": 1.1, "void": 1.05, "reef": 1.25, "moth": 0.8, "forge": 0.86, "star": 0.92, "mystic": 0.95,
            "anni": 1.0, "anni2": 1.0}.get(element, 1.2)
    yaw = {"schrolli": -22, "schrolli_true": -22, "schrolli_almighty": -22, "world_eater": -6, "water": -50, "earth": -38, "void": -26, "reef": -20, "moth": -8, "forge": -14, "star": -24, "mystic": -22, "anni": -24,
           "anni2": -24}.get(element, -42)
    pitch = {"schrolli": 4, "schrolli_true": 4, "schrolli_almighty": 4, "world_eater": 4, "reef": 8, "moth": 4, "forge": 4, "star": 4, "mystic": 2, "anni": 2, "anni2": 2}.get(element, 6)
    big = render(m, tex, glow, yaw, pitch, 448, pose=getattr(mod, "FOLD_POSE", None), background=(0, 0, 0, 0), zoom=zoom,
                 focus=["body"] if element == "reef" else ["helm"] if element == "star" else ["head"])
    img = big.resize((64, 64), Image.LANCZOS)
    img = ImageEnhance.Brightness(img).enhance({"fire": 1.9, "earth": 1.5, "lightning": 1.9, "nature": 1.6, "shadow": 2.4,
                                                 "void": 1.0, "reef": 1.35, "moth": 1.25, "forge": 1.65, "star": 1.15, "mystic": 1.2,
                                                 "anni": 1.45, "anni2": 1.45, "schrolli": 1.5, "schrolli_true": 1.5,
                                                 "schrolli_almighty": 1.45, "world_eater": 2.0}.get(element, 1.4))
    img = ImageEnhance.Color(img).enhance(1.15)
    img = ImageEnhance.Contrast(img).enhance(1.12)
    if element == "schrolli_almighty":
        img = _sun_behind(img)
    px = img.load()
    solid = [[False] * 64 for _ in range(64)]
    for y in range(64):
        for x in range(64):
            r, g, b, a = px[x, y]
            d = math.hypot(x - 31.5, y - 31.5)
            on = a > 140 and d <= 29.5
            solid[x][y] = on
            px[x, y] = (r, g, b, 255) if on else (0, 0, 0, 0)
    out = Image.new("RGBA", (64, 64), CLEAR)
    op = out.load()
    for y in range(64):
        for x in range(64):
            if solid[x][y]:
                op[x, y] = px[x, y]
                continue
            near = any(0 <= x + dx < 64 and 0 <= y + dy < 64 and solid[x + dx][y + dy]
                       for dx in (-2, -1, 0, 1, 2) for dy in (-2, -1, 0, 1, 2) if abs(dx) + abs(dy) <= 3)
            if near and math.hypot(x - 31.5, y - 31.5) <= 31.5:
                op[x, y] = (10, 8, 12, 255)
    return out


def mage_portrait(element: str) -> Image.Image:
    """Bildnis eines Erzmagiers: Kopf mit Kopfschmuck schräg von vorn (Zubehör anderer Elemente entfernt)."""
    from entitygen.models import mage
    from entitygen.paint import paint_model
    from entitygen.preview import render
    m = mage.build()
    m.pack()
    hidden = (mage.ALL_GEAR - mage.GEAR[element]) | {"left_arm", "right_arm"}

    def strip(bone):
        if bone.name in hidden:
            bone.cubes.clear()
            for child in bone.children:
                strip_all(child)
            return
        for child in bone.children:
            strip(child)

    def strip_all(bone):
        bone.cubes.clear()
        for child in bone.children:
            strip_all(child)

    tex, glow = paint_model(m, mage.materials(element), mage.PALETTES[element], seed=100 + mage.ELEMENTS.index(element),
                            params={"variant": element})
    strip(m.root)
    big = render(m, tex, glow, -28, 4, 448, background=(0, 0, 0, 0), zoom=1.08, focus=["head"])
    img = big.resize((64, 64), Image.LANCZOS)
    img = ImageEnhance.Brightness(img).enhance({"fire": 1.2, "earth": 1.25, "lightning": 1.5}.get(element, 1.4))
    img = ImageEnhance.Color(img).enhance(1.15)
    img = ImageEnhance.Contrast(img).enhance(1.12)
    px = img.load()
    solid = [[False] * 64 for _ in range(64)]
    for y in range(64):
        for x in range(64):
            r, g, b, a = px[x, y]
            d = math.hypot(x - 31.5, y - 31.5)
            on = a > 140 and d <= 29.5
            solid[x][y] = on
            px[x, y] = (r, g, b, 255) if on else (0, 0, 0, 0)
    out = Image.new("RGBA", (64, 64), CLEAR)
    op = out.load()
    for y in range(64):
        for x in range(64):
            if solid[x][y]:
                op[x, y] = px[x, y]
                continue
            near = any(0 <= x + dx < 64 and 0 <= y + dy < 64 and solid[x + dx][y + dy]
                       for dx in (-2, -1, 0, 1, 2) for dy in (-2, -1, 0, 1, 2) if abs(dx) + abs(dy) <= 3)
            if near and math.hypot(x - 31.5, y - 31.5) <= 31.5:
                op[x, y] = (10, 8, 12, 255)
    return out


def mage_bossbar(element: str) -> Image.Image:
    """Bossleiste eines Erzmagiers: Rahmen und Füllung seines Elements, im Medaillon sein Bildnis."""
    img = bossbar(element)
    img.paste((0, 0, 0, 0), (0, 128, 64, 192))
    img.alpha_composite(mage_portrait(element), (0, 128))
    return img


def bossbar(element: str) -> Image.Image:
    img = Image.new("RGBA", (256, 256), CLEAR)
    fr = frame(element)
    img.alpha_composite(fr, (0, 0))
    img.alpha_composite(fill_strip(element), (0, 48))
    img.alpha_composite(empty_strip(element), (0, 58))
    img.alpha_composite(trail_strip(element), (0, 68))
    img.alpha_composite(shine_strip(), (184, 58))
    img.alpha_composite(gem(ELEMENTS[element], False), (204, 58))
    img.alpha_composite(gem(ELEMENTS[element], True), (214, 58))
    img.alpha_composite(aura(element, fr), (0, 80))
    img.alpha_composite(portrait(element), (0, 128))
    return img


def preview(target: str) -> None:
    """Vorschau: alle Leisten zusammengesetzt (wie im Spiel, bei 70 % und 25 % Leben), dreifach vergrößert."""
    out = Image.new("RGBA", (FRAME_W * 3 * 2 + 30, len(ELEMENTS) * 3 * (FRAME_H + 10) + 20), (46, 52, 60, 255))
    for i, element in enumerate(ELEMENTS):
        tex = bossbar(element)
        for j, hp in enumerate((0.7, 0.25)):
            scene = Image.new("RGBA", (FRAME_W, FRAME_H + 8), (0, 0, 0, 0))
            scene.alpha_composite(tex.crop((0, 58, BAR_W, 58 + BAR_H)), (BAR_X, BAR_Y + 8))
            fill = round(BAR_W * hp)
            trail = min(BAR_W, fill + 18)
            scene.alpha_composite(tex.crop((0, 68, trail, 68 + BAR_H)), (BAR_X, BAR_Y + 8))
            scene.alpha_composite(tex.crop((20, 48, 20 + fill, 48 + BAR_H)), (BAR_X, BAR_Y + 8))
            if hp < 0.3:
                scene.alpha_composite(tex.crop((0, 80, FRAME_W, 80 + FRAME_H)), (0, 8))
            scene.alpha_composite(tex.crop((0, 0, FRAME_W, FRAME_H)), (0, 8))
            p = tex.crop((0, 128, 64, 192)).resize((32, 32), Image.NEAREST)
            scene.alpha_composite(p, (EMB_X - 16, EMB_Y - 16 + 8))
            for f in GEMS:
                gx = BAR_X + round(BAR_W * f)
                g = tex.crop((214, 58, 223, 67)) if hp < f else tex.crop((204, 58, 213, 67))
                scene.alpha_composite(g, (gx - 4, BAR_Y - 3 - 7 + 8))
            big = scene.resize((scene.width * 3, scene.height * 3), Image.NEAREST)
            out.alpha_composite(big, (10 + j * (FRAME_W * 3 + 10), 10 + i * 3 * (FRAME_H + 10)))
    out.save(target)


if __name__ == "__main__":
    preview(sys.argv[1] if len(sys.argv) > 1 else "/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad/v2/bars_new.png")
