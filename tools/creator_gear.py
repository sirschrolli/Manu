#!/usr/bin/env python3
"""Die getragene Ausrüstung des Schöpfers (seit 0.78): Schwingen aus Federn, Heiligenschein und die Räder voller Augen.

Schreibt die Textur der 3D-Teile (Modelllage 128×64, gemalt in vierfacher Auflösung 512×256) mit ihrer Leuchtebene und die
Bilder der Gegenstände (16×16): Heiligenschein, Räder, Schwingen und den Speer des Schöpfers, dazu den Atlas des Speers in
der Hand (32×32).

Lage (Einheiten der Modelllage; siehe client/creator/CreatorGearLayer):
  Federn, jede einzeln (Vorderseite = Unterseite des Flügels, Rückseite = Oberseite gespiegelt daneben; Aufbau des Flügels in
  tools/creator_wings.py): Handschwingen 3×16 bei (0,0), 3×14 bei (6,0), 3×12 bei (12,0); Armschwinge 3×12 bei (18,0);
  Schirmfeder 3×10 bei (24,0); Deckfedern 3×7 bei (30,0) und 2×4 bei (36,0)
  Arme der Glieder: Oberarm (0,20) 18×4, Unterarm (0,24) 16×4, Hand (0,28) 10×2
  Heiligenschein: Glied (64,0) 6×2, Stein (72,0) 4×2, innerer Lichtring (78,0) 4×1
  Räder: Glied (64,4), Auge (72,4), Rune (80,4) je 8×3

Aufruf: python3 tools/creator_gear.py
"""
import math
import os
import random

from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src/main/resources/assets/kingdomsatwar/textures")
K = 4  # Pixel je Einheit
W, H = 128 * K, 64 * K

WHITE = [(118, 116, 140), (152, 150, 172), (186, 186, 204), (214, 214, 228), (234, 234, 244), (246, 246, 252), (255, 255, 255)]
UNDER = [(136, 128, 144), (168, 160, 174), (198, 190, 202), (220, 214, 222), (236, 230, 236), (247, 243, 246)]
GOLD = [(90, 58, 16), (138, 94, 28), (184, 134, 42), (220, 174, 62), (242, 207, 98), (255, 232, 160), (255, 248, 220)]
LIGHT = [(255, 214, 120), (255, 232, 160), (255, 244, 200), (255, 252, 236)]
EYE = [(30, 80, 160), (60, 140, 230), (140, 210, 255), (230, 248, 255)]


def lerp(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def ramp(colors, t):
    t = max(0.0, min(1.0, t))
    f = t * (len(colors) - 1)
    i = min(int(f), len(colors) - 2)
    return lerp(colors[i], colors[i + 1], f - i)


# =====================================================================================
# Federn (seit 0.78.2 jede einzeln – Aufbau des Flügels in tools/creator_wings.py)
# =====================================================================================

def feather_sprite(w, length, kind, under):
    """Eine einzelne Feder (w×length Einheiten), die Wurzel oben, die Spitze unten: schmaler Kiel, helle Fahne mit feinen
    Ästen, weicher dunkler Saum, die Außenfahne (rechts) schmaler als die Innenfahne; Handschwingen laufen spitz zu und
    tragen eine goldene Spitze, Arm- und Schirmfedern sind rund mit goldenem Saum, Deckfedern klein und weich.

    under: die Unterseite des Flügels (matter, wärmer, weniger Gold).
    """
    pw, ph = w * K, length * K
    img = Image.new("RGBA", (pw, ph), (0, 0, 0, 0))
    px = img.load()
    base = UNDER if under else WHITE
    family = kind[0]
    gold = {"P": 0.2, "S": 0.11, "T": 0.07, "C": 0.0}[family] * (0.75 if under else 1.0)
    rachis = pw * (0.58 if family in "PS" else 0.52)
    for y in range(ph):
        t = (y + 0.5) / ph
        if family == "P":
            f = min(1.0, 0.5 + 0.5 * t / 0.1) if t < 0.5 else max(0.0, 1.0 - ((t - 0.5) / 0.5) ** 2.3) ** 0.75
        elif family == "C":
            f = min(1.0, 0.62 + 0.38 * t / 0.2) if t < 0.58 else math.sqrt(max(0.0, 1.0 - ((t - 0.58) / 0.42) ** 2))
        else:
            f = min(1.0, 0.5 + 0.5 * t / 0.1) if t < 0.72 else math.sqrt(max(0.0, 1.0 - ((t - 0.72) / 0.28) ** 2))
        right = (pw - rachis - 0.4) * f
        left = (rachis - 0.4) * f
        for x in range(pw):
            dx = x + 0.5 - rachis
            side = right if dx > 0 else left
            if side <= 0.3 or abs(dx) > side:
                continue
            edge = abs(dx) / side
            # die Fahne: zur Außenkante (rechts) heller – sie liegt frei –, zur Innenkante im Schatten der Nachbarfeder
            barbs = 0.03 * math.sin(y * 0.85 - abs(dx) * 1.5)
            v = 0.93 - 0.11 * t - 0.16 * edge ** 2.5 + barbs + (0.03 if dx > 0 else -0.05 * edge) - (0.05 if family == "C" else 0.0)
            c = ramp(base, v)
            # der Kiel: eine feine helle Linie mit weichem Schatten daneben
            if abs(dx) < 0.55 and t < 0.9:
                c = ramp(base, 1.0)
            elif -1.6 < dx <= -0.55 and t < 0.9:
                c = lerp(c, base[2], 0.4)
            # goldene Spitze bzw. goldener Saum
            if gold > 0.0 and t > 1.0 - gold:
                g = (t - (1.0 - gold)) / gold
                c = lerp(c, ramp(GOLD, 0.58 + 0.4 * g - 0.22 * edge), min(1.0, g * 1.7))
            # Saum
            if edge > 0.8 or (side - abs(dx)) < 0.9:
                c = lerp(c, base[1] if t < 1.0 - gold * 0.6 or gold == 0.0 else GOLD[2], 0.5)
            px[x, y] = c + (255,)
    return img


def paste_feather(img, glow, kind):
    """Vorderseite (Unterseite des Flügels) bei (u, v), Rückseite (Oberseite, gespiegelt) gleich daneben; dazu die
    Leuchtebene: Die Federn schimmern sacht (heilige Schwingen bleiben auch im Schatten hell), das Gold glüht."""
    from creator_wings import KINDS
    w, length, u, v = KINDS[kind]
    bottom = feather_sprite(w, length, kind, True)
    top = feather_sprite(w, length, kind, False).transpose(Image.FLIP_LEFT_RIGHT)
    for src, ox in ((bottom, u), (top, u + w)):
        img.alpha_composite(src, (ox * K, v * K))
        sp = src.load()
        gp = glow.load()
        for y in range(src.height):
            for x in range(src.width):
                r, g, b, a = sp[x, y]
                if not a:
                    continue
                if r > 200 and g > 150 and b < 150:
                    gp[ox * K + x, v * K + y] = (min(255, r), min(255, g), min(255, b + 40), 150)
                else:
                    lum = (r + g + b) / 3
                    gp[ox * K + x, v * K + y] = (255, 254, 250, 104 if lum > 200 else 76)


def arm_region(img, x0, y0, w, h):
    """Der Arm eines Glieds: dicht mit kleinen, runden Federn besetzt – versetzte Reihen von Bögen, jeder oben hell und am
    unteren Rand weich abgesetzt, wie Schindeln."""
    px = img.load()
    for y in range(int(y0 * K), int((y0 + h) * K)):
        for x in range(int(x0 * K), int((x0 + w) * K)):
            ly, lx = y - int(y0 * K), x - int(x0 * K)
            best = None
            # die Feder, deren Bogen diesen Punkt bedeckt: erst die eigene Reihe, sonst die darüber
            for row in (ly // 4, ly // 4 - 1):
                cx = ((lx + (row % 2) * 4) % 8) - 3.5
                cy = ly - row * 4
                if cy <= 6.4 - 0.26 * cx * cx:
                    best = (cx, cy)
                    break
            if best is None:
                best = (0.0, 0.0)
            cx, cy = best
            rim = cy > 5.2 - 0.26 * cx * cx
            v = 0.97 - 0.035 * cy - 0.012 * abs(cx) - (0.2 if rim else 0.0)
            px[x, y] = ramp(WHITE, v) + (255,)


def fill_region(img, x0, y0, w, h, colors, pattern="band", glow=None, glow_alpha=0):
    px = img.load()
    for y in range(int(y0 * K), int((y0 + h) * K)):
        for x in range(int(x0 * K), int((x0 + w) * K)):
            t = (y - y0 * K) / max(1, h * K - 1)
            if pattern == "band":
                c = ramp(colors, 0.75 - 0.5 * abs(t - 0.4) + 0.06 * math.sin(x * 0.9))
            else:
                c = ramp(colors, 0.6 + 0.3 * math.sin(x * 0.7 + y * 1.3))
            px[x, y] = c + (255,)
            if glow is not None:
                glow.load()[x, y] = c + (glow_alpha,)


def eye_cell(img, glow, x0, y0, w, h):
    """Ein Glied mit Auge: Gold, in der Mitte jeder Seite ein offenes Auge (leuchtend)."""
    fill_region(img, x0, y0, w, h, GOLD)
    px = img.load()
    gp = glow.load()
    # auf jeder Seitenfläche (Breite 2,5 bzw. 1,5) ein Auge: Mandel, Iris, Pupille
    for cx in (1.5 + 1.25, 1.5 + 2.5 + 1.5 + 1.25):
        cxp, cyp = (x0 + cx) * K, (y0 + 1.5 + 0.75) * K
        for y in range(int(y0 * K), int((y0 + h) * K)):
            for x in range(int(x0 * K), int((x0 + w) * K)):
                ex, ey = (x + 0.5 - cxp) / 2.3, (y + 0.5 - cyp) / 1.25
                if ex * ex + ey * ey <= 1.0:
                    d = math.sqrt(ex * ex + ey * ey)
                    c = (250, 250, 255) if d > 0.62 else EYE[2] if d > 0.32 else (16, 20, 40)
                    if d < 0.16:
                        c = (255, 255, 255)
                    px[x, y] = c + (255,)
                    gp[x, y] = c + (220,)


def rune_cell(img, glow, x0, y0, w, h):
    fill_region(img, x0, y0, w, h, GOLD)
    px = img.load()
    gp = glow.load()
    for y in range(int(y0 * K), int((y0 + h) * K)):
        for x in range(int(x0 * K), int((x0 + w) * K)):
            lx, ly = x - x0 * K, y - y0 * K
            if (lx * 3 + ly * 5) % 9 == 0 and 1 <= ly <= h * K - 2:
                px[x, y] = LIGHT[2] + (255,)
                gp[x, y] = LIGHT[3] + (200,)


def gear_texture():
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    import sys
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    from creator_wings import ARMS, KINDS
    for kind in KINDS:
        paste_feather(img, glow, kind)
    # die Arme der drei Glieder: weich befiedert (Lage wie ein Quader: 2·(Tiefe + Länge) breit, Tiefe + Höhe hoch)
    for (length, thick, depth, u, v) in ARMS:
        arm_region(img, u, v, 2 * (depth + length), depth + thick)
    # Heiligenschein: Gold, das leuchtet; Steine; innerer Lichtring
    fill_region(img, 64, 0, 6, 2, [GOLD[4], GOLD[5], GOLD[6], LIGHT[3]], glow=glow, glow_alpha=235)
    fill_region(img, 72, 0, 4, 2, [EYE[1], EYE[2], EYE[3]], pattern="gem", glow=glow, glow_alpha=255)
    fill_region(img, 78, 0, 4, 1, [LIGHT[2], LIGHT[3], (255, 255, 255)], glow=glow, glow_alpha=255)
    # Räder: Glied, Auge, Rune
    fill_region(img, 64, 4, 8, 3, GOLD)
    eye_cell(img, glow, 72, 4, 8, 3)
    rune_cell(img, glow, 80, 4, 8, 3)
    return img, glow


# =====================================================================================
# Bilder der Gegenstände (16×16)
# =====================================================================================

def sprite(art, legend):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y, row in enumerate(art.strip("\n").split("\n")):
        for x, ch in enumerate(row):
            if ch in legend:
                px[x, y] = legend[ch] + (255,)
    return img


def outline(img, color=(44, 30, 14)):
    out = img.copy()
    src = img.load()
    px = out.load()
    for y in range(16):
        for x in range(16):
            if src[x, y][3]:
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < 16 and 0 <= ny < 16 and src[nx, ny][3]:
                    px[x, y] = color + (255,)
                    break
    return out


def halo_item():
    art = """
................
................
................
.....abccba.....
...abcddddcba...
..bcd......dcb..
.bcd........dcb.
.cd....eE....dc.
.bc....EE....cb.
..bcd......dcb..
...bccddddccb...
.....bccccb.....
................
................
................
................
"""
    leg = {"a": GOLD[3], "b": GOLD[4], "c": GOLD[5], "d": LIGHT[3], "e": LIGHT[1], "E": LIGHT[2]}
    img = outline(sprite(art, leg), (96, 62, 18))
    # ein Funkeln oben rechts
    px = img.load()
    for x, y in ((13, 2), (12, 1), (14, 1), (13, 0)):
        px[x, y] = (255, 252, 230, 255) if (x, y) == (13, 1) else (255, 236, 170, 200)
    return img


def rings_item():
    art = """
................
.....aabbaa.....
...ab......ba...
..a....cc....a..
.a...ccEEcc...a.
.b..c..ww..c..b.
.b.cE.wPPw.Ec.b.
.a.c..wPPw..c.a.
.a.cE..ww..Ec.a.
.b..c......c..b.
.b...ccEEcc...b.
..a....cc....a..
...ab......ba...
.....aabbaa.....
................
................
"""
    leg = {"a": GOLD[3], "b": GOLD[5], "c": GOLD[4], "E": EYE[2], "w": (250, 250, 255), "P": EYE[1]}
    return outline(sprite(art, leg), (70, 46, 14))


def wings_item():
    art = """
................
.gG..........Gg.
.wGg........gGw.
.wwGg......gGww.
.wwwGg....gGwww.
.WwwwGg..gGwwwW.
.wWwwwG..GwwwWw.
..wWwwg..gwwWw..
..wwWwg..gwWww..
...wwWg..gWww...
...wwwg..gwww...
....wwG..Gww....
....wG....Gw....
.....G....G.....
................
................
"""
    leg = {"w": WHITE[5], "W": WHITE[3], "g": GOLD[4], "G": GOLD[5]}
    return outline(sprite(art, leg), (90, 86, 110))


def spear_item():
    art = """
............LLL.
...........LwwL.
..........LwSwL.
.........gLSSL..
........gGgLL...
.......gGg......
......gDg.......
.....dDd........
....dDd.........
...wDw..........
..dDd...........
.dDd............
.Dd.............
................
................
................
"""
    leg = {"L": LIGHT[2], "w": (255, 255, 255), "S": LIGHT[1], "g": GOLD[4], "G": GOLD[6], "d": GOLD[3], "D": GOLD[5]}
    img = outline(sprite(art, leg), (80, 52, 14))
    px = img.load()
    for x, y in ((11, 4), (13, 5)):
        px[x, y] = (255, 236, 170, 220)
    return img


def spear_model():
    """Atlas (32×32) des Speers in der Hand (seit 0.78 neu geordnet): Gold (0–8 oben), Griff (0–8 unten), Weiß (8–12),
    Licht der Klinge (12–16), leuchtendes Gold (16–20), der Ring (20–24), blauer Stein (24–32)."""
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    glow = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    px = img.load()
    gx = glow.load()
    shaft = [2, 3, 4, 5, 5, 4, 3, 2]
    for y in range(32):
        for x in range(32):
            lit = False
            if x < 8 and y < 16:
                c = GOLD[min(6, shaft[x] + (1 if (y + x) % 7 == 0 else 0))]
            elif x < 8:
                band = ((x + y) // 2) % 3
                c = (GOLD[2], GOLD[3], GOLD[4])[band] if band else GOLD[1]
                if band == 2 and (x + y) % 2 == 0:
                    c = GOLD[5]
            elif x < 12:
                c = (WHITE[4], WHITE[6], WHITE[6], WHITE[5])[x - 8]
            elif x < 16:
                c = ((255, 236, 176), (255, 252, 236), (255, 255, 255), (255, 244, 206))[x - 12]
                lit = True
            elif x < 20:
                c = (GOLD[5], GOLD[6], GOLD[6], GOLD[5])[x - 16] if (x + y) % 5 else LIGHT[2]
                lit = True
            elif x < 24:
                c = ((196, 236, 255), (236, 250, 255), (255, 255, 255), (214, 242, 255))[x - 20]
                lit = True
            else:
                f = abs((x - 28) + 0.5) + abs(((y % 16) - 8) + 0.5) * 0.5
                c = EYE[3] if f < 1.5 else EYE[2] if f < 3.0 else EYE[1]
                if (x + y) % 6 == 0:
                    c = (255, 255, 255)
                lit = True
            px[x, y] = c + (255,)
            if lit:
                gx[x, y] = c + (255,)
    return img, glow


def main():
    gear, glow = gear_texture()
    out = os.path.join(TEX, "entity/equipment_parts")
    os.makedirs(out, exist_ok=True)
    gear.save(os.path.join(out, "creator_gear.png"))
    glow.save(os.path.join(out, "creator_gear_glow.png"))
    items = os.path.join(TEX, "item")
    halo_item().save(os.path.join(items, "creator_halo.png"))
    rings_item().save(os.path.join(items, "creator_rings.png"))
    wings_item().save(os.path.join(items, "creator_wings.png"))
    spear_item().save(os.path.join(items, "creator_scepter.png"))
    model, model_glow = spear_model()
    model.save(os.path.join(items, "creator_scepter_model.png"))
    model_glow.save(os.path.join(items, "creator_scepter_model_glow.png"))
    print("Ausrüstung des Schöpfers geschrieben")


if __name__ == "__main__":
    main()
