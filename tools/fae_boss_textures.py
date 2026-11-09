#!/usr/bin/env python3
"""
Texturen rund um Noctua, die Mottenkönigin (eigene Pixelkunst im Vanilla-Stil):

  * Mottenseide (Block, mit Glanzpunkten als Leuchtebene) und Seidenfäden (hängende Kreuzpflanze, Tautropfen leuchten)
  * der Kokon (Seitenfläche für das spindelförmige Modell, darunter pulsierendes Licht als Bildfolge) und sein Symbol
  * die Feen-Elytren: Gegenstand (heil und zerfetzt) und die getragenen Flügel (64x32, Schema des Elytren-Modells)

Wird von tools/fae_textures.py aufgerufen: build(put, animations).
"""
from __future__ import annotations

import math
import random
from typing import Callable

from PIL import Image

import item_style as st
import pixelart as pa
from pixelart import Color, hexc

N = 16
CLEAR = (0, 0, 0, 0)
TAU = math.tau

SILK = [hexc("#9c8ab8"), hexc("#b8a8d2"), hexc("#d2c6e6"), hexc("#e8e0f4"), hexc("#f6f2fc"), hexc("#fffdff")]
SILK_SHEEN = [hexc("#ffe2f4"), hexc("#dff6ff"), hexc("#fff4dc")]
GLINT = [hexc("#ffffff"), hexc("#ffd8f2"), hexc("#d8faff")]
INNER = [hexc("#4a1a40"), hexc("#7a2a64"), hexc("#b04a8e"), hexc("#e070b8"), hexc("#ff9ad6"), hexc("#ffd4ee")]


def _mix(a: Color, b: Color, t: float) -> Color:
    t = max(0.0, min(1.0, t))
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(4))  # type: ignore[return-value]


def _pick(pal: list[Color], v: float) -> Color:
    v = max(0.0, min(0.999, v))
    return pal[int(v * len(pal))]


# =============================================================================
# Mottenseide
# =============================================================================

def moth_silk() -> tuple[Image.Image, Image.Image]:
    """
    Mottenseide: dicht gesponnene, weiche Fäden, die in flachen Wellen quer laufen und sich mit einer zweiten, steileren
    Lage kreuzen; dazwischen schattige Mulden, auf den Fäden ein perlmuttfarbener Schimmer. Nahtlos kachelbar. Wo sich
    die hellsten Fäden kreuzen, glitzern ein paar Punkte (Leuchtebene).
    """
    img = Image.new("RGBA", (N, N))
    glow = Image.new("RGBA", (N, N), CLEAR)
    rnd = random.Random(71301)
    phases = [rnd.uniform(0, TAU) for _ in range(8)]
    field = [[0.0] * N for _ in range(N)]
    for y in range(N):
        for x in range(N):
            u = x / N * TAU
            v = y / N * TAU
            # flache Wellen quer (4 Fäden je Kachel), steilere Lage darüber (3 Fäden)
            a = math.sin(v * 4 + math.sin(u + phases[0]) * 1.4 + math.sin(u * 2 + phases[1]) * 0.5)
            b = math.sin(u * 3 + v * 1 + math.sin(v * 2 + phases[2]) * 0.9)
            fa = (a * 0.5 + 0.5) ** 3
            fb = (b * 0.5 + 0.5) ** 4
            f = 0.25 + 0.55 * fa + 0.35 * fb + 0.06 * math.sin(u * 5 + v * 3 + phases[3])
            field[y][x] = f
    for y in range(N):
        for x in range(N):
            f = field[y][x]
            c = _pick(SILK, 0.1 + f * 0.82)
            sheen = (math.sin((x + y * 0.5) / N * TAU * 2 + phases[4]) * 0.5 + 0.5) * max(0.0, f - 0.55) * 1.6
            c = _mix(c, SILK_SHEEN[(x // 5 + y // 6) % 3], sheen * 0.45)
            img.putpixel((x, y), c)
    # Glanzpunkte auf den hellsten Kreuzungen (nicht nebeneinander)
    spots = sorted(((field[y][x], x, y) for y in range(N) for x in range(N)), reverse=True)
    placed: list[tuple[int, int]] = []
    for _, x, y in spots:
        if len(placed) >= 6:
            break
        if any(abs(x - px) + abs(y - py) < 5 for px, py in placed):
            continue
        c = GLINT[len(placed) % 3]
        img.putpixel((x, y), c)
        glow.putpixel((x, y), c)
        placed.append((x, y))
    return img, glow


def silk_strands() -> tuple[Image.Image, Image.Image]:
    """
    Seidenfäden: drei, vier feine Fäden, die fast senkrecht hängen und sacht wehen (oben und unten treffen sie sich
    mit dem Block darüber und darunter), mit kleinen Flocken und leuchtenden Tautropfen.
    """
    img = Image.new("RGBA", (N, N), CLEAR)
    glow = Image.new("RGBA", (N, N), CLEAR)
    rnd = random.Random(71302)
    for k, x0 in enumerate((3, 7, 10, 13)):
        phase = rnd.uniform(0, TAU)
        for y in range(N):
            x = int(round(x0 + math.sin(y / N * TAU + phase) * 0.55))
            c = SILK[4] if (y + k) % 3 else SILK[3]
            if (y * 7 + k * 3) % 11 == 0:
                c = SILK_SHEEN[k % 3]
            img.putpixel((x, y), c)
            # Flocken: ein zarter Bausch neben dem Faden
            if (y * 5 + k * 7) % 13 == 0:
                img.putpixel((x + (1 if k % 2 else -1), y), SILK[2])
        for j in range(2):
            y = (3 + k * 4 + j * 7) % (N - 2) + 1
            x = int(round(x0 + math.sin(y / N * TAU + phase) * 0.55))
            c = GLINT[(k + j) % 3]
            img.putpixel((x, y), c)
            glow.putpixel((x, y), c)
    return img, glow


# =============================================================================
# Der Kokon
# =============================================================================

def _cocoon_cell(x: int, y: int) -> tuple[str, float]:
    """
    Wickelbahnen des Kokons: breite Seidenbahnen laufen schräg um ihn herum (sechs Pixel breit), jede in sich gewölbt
    (oben hell, unten im Schatten der nächsten Bahn). Zwischen manchen Bahnen klafft ein schmaler Spalt, durch den das
    rosa Licht scheint; lose Fäden einer zweiten Lage kreuzen flach darüber. Nahtlos kachelbar.
    Gibt ("silk", Helligkeit) oder ("gap", Stärke) zurück.
    """
    d = (x + y) % 16
    band = d // 6 if d < 12 else 2
    p = d % 6 if d < 12 else (d - 12) * 6 // 4
    if p == 0 and band != 1 and (x * 5 + y * 3) % 8 < 5:
        return "gap", 0.6 + 0.4 * ((x + 2 * y) % 3) / 2
    light = (0.3, 0.62, 0.92, 0.98, 0.8, 0.55)[p]
    if (2 * x - y) % 11 == 0:
        light = min(1.0, light + 0.2)
    return "silk", light


def moth_cocoon() -> Image.Image:
    """
    Seitenfläche des Kokons (das Modell staucht sie auf seine volle Höhe): schräg gewickelte, gewölbte Seidenbahnen in
    Weiß und Flieder; in den schmalen Spalten dazwischen schimmert dunkles Rosa – dort pulsiert das Licht.
    """
    img = Image.new("RGBA", (N, N))
    for y in range(N):
        for x in range(N):
            kind, v = _cocoon_cell(x, y)
            if kind == "gap":
                c = _pick(INNER, 0.2 + v * 0.25)
            else:
                c = _pick(SILK, 0.05 + v * 0.9)
                if v > 0.9:
                    c = _mix(c, SILK_SHEEN[(x // 3 + y) % 3], 0.3)
            img.putpixel((x, y), c)
    return img


#: Herzschlag des Kokons: zwei schnelle Schläge, dann Ruhe (Helligkeit je Bild)
PULSE = (0.30, 0.55, 1.00, 0.72, 0.48, 0.80, 0.96, 0.62, 0.42, 0.34, 0.31, 0.30, 0.29, 0.29, 0.30, 0.30)


def moth_cocoon_glow() -> Image.Image:
    """Das Licht in den Spalten als Bildfolge (16 Bilder): ein langsamer Herzschlag in Rosa."""
    sheet = Image.new("RGBA", (N, N * len(PULSE)), CLEAR)
    for f, beat in enumerate(PULSE):
        for y in range(N):
            for x in range(N):
                kind, v = _cocoon_cell(x, y)
                if kind != "gap":
                    continue
                sheet.putpixel((x, f * N + y), _pick(INNER, 0.2 + beat * 0.78 * (0.75 + 0.25 * v)))
    return sheet


COCOON_ITEM = """
.......O........
.......O........
......ded.......
.....defed......
....defgfed.....
....efpfpfe.....
...defgfgfed....
...efpfgfpfe....
...defgfgfdc....
...efgfpfgec....
...defgfgedc....
....efpfgec.....
....defged......
.....dfed.......
......ded.......
.......O........
"""


def moth_cocoon_item() -> Image.Image:
    """Symbol des Kokons: eine Seidenspindel, durch deren Spalten rosa Licht schimmert, oben und unten ein Faden."""
    s = st.Spr.art(COCOON_ITEM, legend={"p": ("D", 5), "O": ("A", 4)})
    s = s.outline(light=2, dark=1, prefer=("B",))
    return s.image({"A": st.ramp("#d8cce8", dark=0.5, light=0.98, cool=280), "B": st.ramp("#e2d6f0", dark=0.42, light=0.99, cool=285),
                    "D": st.ramp("#e070b8", dark=0.3, light=0.97, cool=320)})


# =============================================================================
# Feen-Elytren
# =============================================================================

ELYTRA_LEFT = [
    # 8 Spalten (außen -> zur Mitte), die Mitte selbst (Spalte 7) ist der Leib
    "........",
    ".....EEk",
    "...EEffk",
    ".EEfffek",
    "Efffeeek",
    "Efesueek",
    "Efeqseek",
    ".feeeedk",
    "..edddd.",
    "...eddc.",
    "...ddcc.",
    "....dc..",
    "....dc..",
    "...dc...",
    "...c....",
    "........",
]

ELYTRA_BROKEN_LEFT = [
    "........",
    ".....EEk",
    "...E,ffk",
    ".EEf,,ek",
    "E,ffeeek",
    "Efes.eek",
    ".fe,seek",
    ".f,eeedk",
    "..e,ddd.",
    "...ed,c.",
    "....dcc.",
    "....d...",
    "........",
    "........",
    "........",
    "........",
]


def _elytra_sprite(left: list[str]) -> st.Spr:
    """Beide Flügel: links wie gezeichnet, rechts gespiegelt (eine Stufe dunkler – das Licht kommt von links)."""
    rows = []
    for row in left:
        right = []
        for ch in reversed(row):
            right.append(ch)
        rows.append(row + "".join(right))
    s = st.Spr.art("\n".join(rows), legend={"q": ("D", 2), "s": ("D", 4), "u": ("D", 6), "E": ("E", 4), "k": ("C", 4)})
    s = s.map(lambda x, y, p: (p[0], st.clamp(p[1] - 1)) if x >= 8 and p[0] in ("B", "E") else p)
    return s.outline(light=1, dark=0, prefer=("E", "B"))


def fae_elytra_item(broken: bool) -> Image.Image:
    s = _elytra_sprite(ELYTRA_BROKEN_LEFT if broken else ELYTRA_LEFT)
    ramps = {"B": st.ramp("#92e2c4", dark=0.3, light=0.97, cool=200, warm=120),
             "C": st.ramp("#ece4f6", dark=0.45, light=0.99, cool=280),
             "D": st.ramp("#ff7ec0", dark=0.32, light=0.98, cool=320, warm=60),
             "E": st.ramp("#b48ae0", dark=0.3, light=0.95, cool=285)}
    if broken:
        ramps["B"] = st.ramp("#6a9a8c", dark=0.25, light=0.85, cool=220)
        ramps["E"] = st.ramp("#8a76a0", dark=0.25, light=0.82, cool=280)
        ramps["D"] = st.ramp("#a0607e", dark=0.25, light=0.8, cool=320)
    return s.image(ramps)


WING_GREEN = [hexc("#2e6e64"), hexc("#3f8f7c"), hexc("#5cb294"), hexc("#7fd0ac"), hexc("#a4e8c6"), hexc("#ccf8e0"), hexc("#effff6")]
WING_EDGE = [hexc("#4a2a62"), hexc("#6a3e8a"), hexc("#9064b4"), hexc("#b892d6")]
WING_VEIN = hexc("#3a7a6a")
SPOT = {"rim": hexc("#3a1e4e"), "gold": hexc("#ffd878"), "pink": hexc("#ff86c4"), "core": hexc("#fff0f8")}


def _wing_mask(x: int, y: int) -> str | None:
    """
    Umriss eines Mondfalter-Flügels in der 10x20-Fläche des Elytren-Quaders (x = 0 an der Schulter, 9 außen):
    oben der breite, dreieckige Vorderflügel mit gerundeter Spitze außen, darunter der Hinterflügel, der in einen
    langen, nach außen geschwungenen Schwanz ausläuft. Gibt "fore", "hind", "tail" oder None zurück.
    """
    cx, cy = x + 0.5, y + 0.5
    lead = 0.28 * cx
    trail = 11.6 - 0.46 * cx
    if lead - 0.2 <= cy <= trail:
        mid = (lead + trail) / 2
        half = (trail - lead) / 2
        if cx < 8.4 or abs(cy - mid) <= half - (cx - 8.4) * 0.9:
            return "fore"
    if ((cx - 3.8) / 4.4) ** 2 + ((cy - 12.2) / 3.6) ** 2 <= 1.0:
        return "hind"
    if 14.5 <= cy <= 19.9:
        t = (cy - 14.5) / 5.4
        tail_x = 3.8 + t * 2.4 + t * t * 1.2
        if abs(cx - tail_x) <= 1.2 - t * 0.6:
            return "tail"
    return None


def paint_fae_wings(img: Image.Image) -> None:
    """
    Die getragenen Flügel (Rückseite des Elytren-Quaders: 10x20 ab 36,2) plus Kanten: mondgrün mit hellem Schimmer zum
    Rand hin, eine violette Vorderkante, feine Adern von der Schulter aus, je ein Augenfleck (dunkler Ring, Goldsaum,
    rosa Kern) auf Vorder- und Hinterflügel und ein heller Saum mit kleinen Goldpunkten.
    """
    px = img.load()
    x0, y0 = 36, 2
    shape = {(x, y): _wing_mask(x, y) for x in range(10) for y in range(20)}
    for (x, y), part in shape.items():
        if part is None:
            continue
        if part == "fore":
            edge = any(shape.get((x + dx, y + dy)) != "fore" for dx, dy in ((1, 0), (0, 1), (0, -1)) if 0 <= x + dx < 10)
        else:
            edge = any(shape.get((x + dx, y + dy)) is None for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)) if 0 <= x + dx < 10)
        # Grundton: zum Rand und nach unten heller
        v = 0.34 + x * 0.045 + (0.08 if part != "fore" else 0.0) + 0.03 * math.sin(x * 1.7 + y * 0.9)
        c = _pick(WING_GREEN, v)
        # Adern: fächerförmig von der Schulter (0, 0)
        ang = math.atan2(y + 0.5, x + 0.5)
        if part == "fore" and x > 1 and abs(math.sin(ang * 5.0)) < 0.16:
            c = WING_VEIN
        if part == "hind" and x > 0 and abs(math.sin(ang * 4.0 + 0.6)) < 0.14:
            c = WING_VEIN
        # Vorderkante violett
        if part == "fore" and y < 0.35 * x + 1.2:
            c = WING_EDGE[2] if y < 0.35 * x + 0.6 else WING_EDGE[1]
        elif edge:
            c = WING_GREEN[5] if (x + y) % 4 else SPOT["gold"]
            if part == "tail":
                c = WING_GREEN[4]
        px[x0 + x, y0 + y] = c
    # Augenflecken
    for (sx, sy, r) in ((6.0, 5.6, 1.9), (3.8, 12.8, 1.6)):
        for y in range(20):
            for x in range(10):
                if shape.get((x, y)) is None:
                    continue
                d = math.hypot(x + 0.5 - sx, y + 0.5 - sy)
                if d <= r * 0.45:
                    px[x0 + x, y0 + y] = SPOT["core"] if d < r * 0.2 else SPOT["pink"]
                elif d <= r * 0.75:
                    px[x0 + x, y0 + y] = SPOT["gold"]
                elif d <= r:
                    px[x0 + x, y0 + y] = SPOT["rim"]
    # Oberkante (Deckfläche 24,0) und Seitenflächen: die violette Vorderkante
    for x in range(10):
        px[24 + x, 0] = WING_EDGE[3]
        px[24 + x, 1] = WING_EDGE[2]
    for y in range(20):
        px[22, 2 + y] = WING_EDGE[1] if y < 12 else CLEAR
        px[34, 2 + y] = WING_EDGE[2] if y < 5 else CLEAR
        px[35, 2 + y] = WING_EDGE[2] if y < 5 else CLEAR


def fae_wings() -> Image.Image:
    img = Image.new("RGBA", (64, 32), CLEAR)
    paint_fae_wings(img)
    return img


# =============================================================================
# Ausgabe
# =============================================================================

def build(put: Callable[[str, Image.Image], None], animations: dict) -> None:
    silk, silk_glow = moth_silk()
    put("block/moth_silk", silk)
    put("block/moth_silk_glow", silk_glow)
    strands, strands_glow = silk_strands()
    put("block/silk_strands", strands)
    put("block/silk_strands_emissive", strands_glow)
    put("block/moth_cocoon", moth_cocoon())
    put("block/moth_cocoon_glow", moth_cocoon_glow())
    animations["block/moth_cocoon_glow"] = (len(PULSE), 3, True)
    put("item/moth_cocoon", moth_cocoon_item())
    put("item/fae_elytra", fae_elytra_item(False))
    put("item/fae_elytra_broken", fae_elytra_item(True))
    put("entity/equipment/wings/fae_elytra", fae_wings())


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda name, img: out.__setitem__(name, img), {})
    zoom = 12
    tiles = []
    for name, img in out.items():
        frame = img.crop((0, 0, min(img.width, 64), min(img.height, 32 if img.width == 64 else 16)))
        bg = Image.new("RGBA", frame.size, (60, 50, 80, 255))
        bg.alpha_composite(frame)
        tiles.append(bg.resize((frame.width * zoom, frame.height * zoom), Image.NEAREST))
    w = sum(t.width for t in tiles) + 10 * len(tiles)
    h = max(t.height for t in tiles)
    sheet = Image.new("RGBA", (w, h), (30, 26, 40, 255))
    xo = 0
    for t in tiles:
        sheet.alpha_composite(t, (xo, 0))
        xo += t.width + 10
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "fae_boss_preview.png")
