#!/usr/bin/env python3
"""Die getragene Ausrüstung des Schöpfers (seit 0.78): Schwingen aus Federn, Heiligenschein und die Räder voller Augen.

Schreibt die Textur der 3D-Teile (Modelllage 128×64, gemalt in vierfacher Auflösung 512×256) mit ihrer Leuchtebene und die
Bilder der Gegenstände (16×16): Heiligenschein, Räder, Schwingen und den Speer des Schöpfers, dazu den Atlas des Speers in
der Hand (32×32).

Lage (Einheiten der Modelllage; siehe client/creator/CreatorGearLayer):
  Federflächen (Vorderseite = Unterseite des Flügels, Rückseite = Oberseite, gespiegelt):
    innen 7×10 bei (0,0)   Mitte 6×11 bei (0,11)   Spitze 10×17 bei (0,22)
    Deckfedern: innen 7×5 bei (20,0), Mitte 6×6 bei (20,6), Spitze 6×7 bei (20,13)
  Knochen: Oberarm (36,0) 16×2,5, Unterarm (36,3) 14×2,5, Hand (36,6) 10×2
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
UNDER = [(112, 104, 118), (146, 138, 150), (178, 170, 180), (204, 198, 204), (224, 218, 222), (238, 234, 236)]
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
# Federn
# =====================================================================================

def feather_field(w, h, feathers, under=False, seed=1, tip_gold=0.22, covert=False):
    """Eine Federfläche (w×h Einheiten): Federn hängen von oben herab – jede mit Kiel, heller Fahne, dunklem Saum und
    goldener Spitze; die äußeren liegen über den inneren.

    feathers: Liste von (x Wurzel, Länge, Breite, Neigung nach außen in Grad) in Einheiten; die Fläche ist oben am Knochen.
    """
    pw, ph = w * K, h * K
    img = Image.new("RGBA", (pw, ph), (0, 0, 0, 0))
    px = img.load()
    rnd = random.Random(seed)
    base = UNDER if under else WHITE
    for (fx, length, width, tilt) in feathers:
        a = math.radians(tilt)
        dx, dy = math.sin(a), math.cos(a)          # Richtung der Feder (nach unten, leicht nach außen)
        nx, ny = dy, -dx                            # quer dazu
        x0, y0 = fx * K, -0.6 * K
        L = length * K
        half = width * K / 2.0
        shade = rnd.uniform(-0.03, 0.03)
        for y in range(ph):
            for x in range(pw):
                rx, ry = x + 0.5 - x0, y + 0.5 - y0
                s = rx * dx + ry * dy               # entlang der Feder
                q = rx * nx + ry * ny               # quer (+: zur äußeren Seite)
                if s < 0 or s > L:
                    continue
                t = s / L
                # Umriss: schmale Wurzel, volle Fahne, die Außenfahne schmaler als die Innenfahne, rund zur Spitze
                full = half * (0.62 + 0.38 * math.sin(math.pi * min(1.0, t * 1.1 + 0.1)))
                side = full * (0.78 if q > 0 else 1.0)
                if t > 0.8:
                    side *= math.sqrt(max(0.0, 1.0 - ((t - 0.8) / 0.2) ** 2))
                if abs(q) > side:
                    continue
                edge = abs(q) / max(side, 0.01)
                # Fahne: oben hell, nach unten ein Hauch dunkler; feine schräge Äste
                barbs = 0.035 * math.sin(s * 0.9 - abs(q) * 1.4)
                v = 0.9 - 0.12 * t - 0.18 * edge ** 3 + barbs + shade - (0.06 if covert else 0.0)
                c = ramp(base, v)
                # der Kiel: eine helle Linie mit dunklem Rand
                if abs(q) < 0.55 and t < 0.9:
                    c = ramp(base, 0.97)
                elif abs(q) < 1.15 and t < 0.9:
                    c = lerp(c, base[3], 0.35)
                # goldene Spitze
                if t > 1.0 - tip_gold:
                    g = (t - (1.0 - tip_gold)) / tip_gold
                    c = lerp(c, ramp(GOLD, 0.62 + 0.36 * g - 0.3 * edge), min(1.0, g * 1.5))
                # Saum
                if edge > 0.84:
                    c = lerp(c, base[1], 0.55)
                px[x, y] = c + (255,)
    return img


def wing_planes():
    """Die Federflächen der Schwingen: (Oberseite, Unterseite) je Teil."""
    parts = {}
    # innen: Armschwingen, fünf Federn, nach außen kaum geneigt
    inner = [(0.6 + i * 1.35, 9.4 - i * 0.15, 2.4, -6 + i * 3) for i in range(5)]
    # Mitte: vier Armschwingen, länger
    mid = [(0.5 + i * 1.45, 10.6 - i * 0.2, 2.5, -2 + i * 4) for i in range(4)]
    # Spitze: sechs Handschwingen, gefächert, die äußeren lang und schräg
    tip = [(0.4 + i * 1.25, 12.0 + i * 0.9, 2.3 - i * 0.08, 6 + i * 9) for i in range(6)]
    parts["inner"] = (7, 10, inner, 0.18)
    parts["mid"] = (6, 11, mid, 0.2)
    parts["tip"] = (10, 17, tip, 0.26)
    # Deckfedern: zwei Reihen kurzer, runder Federn
    parts["inner_cov"] = (7, 5, [(0.3 + i * 1.3, 4.4 - (i % 2) * 0.6, 2.2, -4 + i * 2) for i in range(6)], 0.12)
    parts["mid_cov"] = (6, 6, [(0.3 + i * 1.3, 5.4 - (i % 2) * 0.7, 2.3, 0 + i * 3) for i in range(5)], 0.14)
    parts["tip_cov"] = (6, 7, [(0.3 + i * 1.2, 6.3 - (i % 2) * 0.6, 2.2, 5 + i * 6) for i in range(5)], 0.16)
    out = {}
    for name, (w, h, feathers, gold) in parts.items():
        covert = name.endswith("_cov")
        top = feather_field(w, h, feathers, under=False, seed=sum(map(ord, name)), tip_gold=gold, covert=covert)
        bottom = feather_field(w, h, feathers, under=True, seed=sum(map(ord, name)) + 7, tip_gold=gold * 0.8, covert=covert)
        out[name] = (top, bottom, w, h)
    return out


# =====================================================================================
# Die Textur der Teile
# =====================================================================================

def paste_plane(img, glow, at, top, bottom, w):
    """Vorderseite (Unterseite des Flügels) bei at, Rückseite (Oberseite, gespiegelt) daneben."""
    u, v = at
    img.alpha_composite(bottom, (u * K, v * K))
    img.alpha_composite(top.transpose(Image.FLIP_LEFT_RIGHT), ((u + w) * K, v * K))
    # Leuchtebene: die Federn schimmern sacht (heilige Schwingen bleiben auch im Schatten hell), die goldenen Spitzen glühen
    for src, ox in ((bottom, u), (top.transpose(Image.FLIP_LEFT_RIGHT), u + w)):
        sp = src.load()
        gp = glow.load()
        for y in range(src.height):
            for x in range(src.width):
                r, g, b, a = sp[x, y]
                if not a:
                    continue
                if r > 200 and g > 150 and b < 140:
                    gp[ox * K + x, v * K + y] = (min(255, r), min(255, g), min(255, b + 40), 150)
                else:
                    lum = (r + g + b) / 3
                    gp[ox * K + x, v * K + y] = (255, 254, 250, 70 if lum > 200 else 46)


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
    planes = wing_planes()
    spots = {"inner": (0, 0), "mid": (0, 11), "tip": (0, 22), "inner_cov": (20, 0), "mid_cov": (20, 6), "tip_cov": (20, 13)}
    for name, (top, bottom, w, h) in planes.items():
        paste_plane(img, glow, spots[name], top, bottom, w)
    # Knochen: weiß-goldene Vorderkante
    fill_region(img, 36, 0, 16, 2.5, [WHITE[3], WHITE[5], GOLD[4], GOLD[5]])
    fill_region(img, 36, 3, 14, 2.5, [WHITE[3], WHITE[5], GOLD[4], GOLD[5]])
    fill_region(img, 36, 6, 10, 2, [GOLD[3], GOLD[4], GOLD[5], GOLD[6]])
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
