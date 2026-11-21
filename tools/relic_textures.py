#!/usr/bin/env python3
"""
Totems und Äpfel der Macht (0.80) im Vanilla-Stil (siehe tools/item_style.py) – und das Bild des neuen Zustands
(Zweites Leben).

Jedes Totem ist ein kleines Götzenbild: Kopf mit leuchtenden Augen, Schultern und Arme, Rumpf mit Sinnbild, Sockel. Die
Gestalt unterscheidet sich je Totem (Flammenkrone, Eiszacken, Blitzhörner, Blätterkranz, Muschelfächer, Kapuze mit
Sichelhörnern, schwebende Bruchstücke, Heiligenschein und Schwingen), nicht nur die Farbe.

Die Flächen werden als Bereiche gezeichnet und gewölbt schattiert (Licht von oben links); Leuchtendes steht mit fester
Stufe darin. Zeichen:
    '#' Körper (Tonleiter A)     '%' Zier (B)      '&' drittes Material (E)
    'v' 'w' 'x' 'y' 'X' Leuchten (C), dunkel -> hell     'p' 'o' 'O' Stein/Perle (D), dunkel -> hell
    '.' durchsichtig
"""
from __future__ import annotations

import os
import sys
from typing import Callable

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import item_style as st  # noqa: E402

EXPLICIT = {"k": ("B", 1), "v": ("C", 2), "w": ("C", 3), "x": ("C", 4), "y": ("C", 5), "X": ("C", 6), "p": ("D", 2), "o": ("D", 4), "O": ("D", 6)}
REGIONS = {"#": "A", "%": "B", "&": "E"}


def paint(art: str, cuts: dict[str, tuple[float, ...]] | None = None, grain: dict[str, float] | None = None, seed: int = 1,
          outline: tuple[str, ...] = ("A", "B", "E"), prefer: tuple[str, ...] = ("A", "B", "E")) -> st.Spr:
    """Bereiche gewölbt schattieren, Leuchtendes fest einsetzen, außen der Umriss."""
    rows = (art[1:] if art.startswith("\n") else art).rstrip("\n").split("\n")
    assert len(rows) == 16 and all(len(r) == 16 for r in rows), [(i, len(r)) for i, r in enumerate(rows) if len(r) != 16] or len(rows)
    s = st.Spr()
    for ch, slot in REGIONS.items():
        mask = {(x, y) for y, row in enumerate(rows) for x, c in enumerate(row) if c == ch}
        if mask:
            part = st.shaded(mask, slot, cuts=(cuts or {}).get(slot, (0.05, 0.42, 0.72, 0.93)), grain=(grain or {}).get(slot, 0.0),
                             seed=seed, cell=2.0)
            s.over(part)
    for y, row in enumerate(rows):
        for x, c in enumerate(row):
            if c in EXPLICIT:
                s.set(x, y, EXPLICIT[c])
            elif c not in REGIONS and c != ".":
                raise KeyError(c)
    return s.outline(slots=outline, prefer=prefer)


# =============================================================================
# Tonleitern
# =============================================================================

BASALT = st.ramp("#5a5560", dark=0.16, light=0.8, cool=280, warm=40, chroma_light=0.6)
EMBER_ROCK = st.ramp("#7a3a2a", dark=0.16, light=0.78, cool=340, warm=40)
LAVA = st.fixed("#5a1408", "#8c1f0a", "#c43a0c", "#ee6a12", "#ff9a1e", "#ffc93c", "#fff3a8")
ICE = st.ramp("#78b4e4", dark=0.26, light=0.985, cool=258, warm=200, chroma_light=0.3)
ICE_DEEP = st.ramp("#4f86c6", dark=0.22, light=0.9, cool=265, warm=210)
FROST_GLOW = st.fixed("#2c5a9a", "#3f78c0", "#5a9ee0", "#86c6f6", "#b6e4ff", "#dcf4ff", "#ffffff")
STORM = st.ramp("#5c6c8a", dark=0.17, light=0.86, cool=265, warm=220, chroma_light=0.6)
STORM_TRIM = st.ramp("#3c4460", dark=0.14, light=0.74, cool=270, warm=230)
BOLT = st.fixed("#6a5a08", "#9a8410", "#ccb21c", "#f0d42c", "#ffe95a", "#fff59a", "#ffffe6")
STONE = st.ramp("#8a8a80", dark=0.2, light=0.9, cool=250, warm=80, chroma_light=0.6)
MOSS = st.ramp("#5a8a3a", dark=0.2, light=0.84, cool=160, warm=100)
AMBER = st.fixed("#5a2e06", "#8a4a0a", "#b86a10", "#de8e1c", "#f6b53a", "#ffd870", "#fff2c0")
WOOD = st.ramp("#8a5f38", dark=0.18, light=0.84, cool=20, warm=70)
LEAF = st.ramp("#58a83c", dark=0.2, light=0.9, cool=165, warm=105, chroma_light=0.65)
BLOOM = st.fixed("#1c5a2a", "#2a8038", "#42a84a", "#74cc5c", "#a8e878", "#d6f8a4", "#f6ffdc")
PETAL = st.fixed("#7a1c50", "#a82e6c", "#d04a8c", "#ec74ac", "#f8a4c8", "#ffd0e2", "#fff0f6")
TIDE = st.ramp("#2f9aa6", dark=0.18, light=0.9, cool=235, warm=170, chroma_light=0.55)
CORAL = st.ramp("#e8705a", dark=0.24, light=0.94, cool=350, warm=50, chroma_light=0.6)
PEARL = st.fixed("#5a6a8a", "#8a9ab8", "#b4c4dc", "#d4e0f0", "#e8f0fa", "#f6faff", "#ffffff")
AQUA = st.fixed("#0c4a5a", "#12707e", "#1a9aa4", "#3cc4c8", "#74e4e0", "#b0f6f0", "#e8fffc")
SHADOW = st.ramp("#3a2c52", dark=0.1, light=0.7, cool=285, warm=300, chroma_light=0.7)
SHADOW_TRIM = st.ramp("#5a4a78", dark=0.14, light=0.8, cool=280, warm=300)
VIOLET = st.fixed("#2c0c50", "#4a1680", "#7226b4", "#9c44e0", "#c074f8", "#deaaff", "#f6e6ff")
VOID = st.ramp("#262a4a", dark=0.09, light=0.66, cool=265, warm=230, chroma_light=0.8)
VOID_TRIM = st.ramp("#4a4f86", dark=0.14, light=0.82, cool=265, warm=220)
STAR = st.fixed("#12406a", "#1a6496", "#2a90c2", "#4cbce6", "#86dcfa", "#c2f0ff", "#ffffff")
GOLD = st.ramp("#e8b83a", dark=0.3, light=0.98, cool=35, hue_dark=14, warm=100, hue_light=10, chroma_light=0.5)
IVORY = st.ramp("#efe6d2", dark=0.42, light=0.995, cool=45, hue_dark=10, warm=95, chroma_light=0.4)
SUN = st.fixed("#8a5a08", "#c08410", "#eaaa1c", "#ffd040", "#ffe57a", "#fff3b4", "#ffffff")

# =============================================================================
# Totems
# =============================================================================

EMBER = """
.......y........
......xXy.......
....x.yXy.x.....
....yxyXyyx.....
....wyyyyyw.....
....########....
....#yy##yy#....
.%%.########.%%.
.%%..#wyyw#..%%.
.%%%########%%%.
..%%%######%%%..
.....##y###.....
.....###y##.....
.....##y###.....
....%%%%%%%%....
....%%%%%%%%....
"""

FROST = """
.......%........
...%...%...%....
...%%..%..%%....
....%%.%.%%.....
....%%%%%%%.....
....#%%##%%#....
....#kX##kX#....
....########....
..##.##yy##.##..
..###.####.###..
..##..####..##..
..x...#yy#...x..
......####......
.....######.....
....%%%%%%%%....
....%%%%%%%%....
"""

STORM_ART = """
..y..........y..
..yy........yy..
...yx......xy...
..xyy.####.yyx..
...y.######.y...
....########....
....#XX##XX#....
....########....
.%%%.######.%%%.
.%%%%######%%%%.
.....##yy##.....
.....#yy###.....
.....##yy##.....
.....###y##.....
....%%%%%%%%....
....%%%%%%%%....
"""

STONE_ART = """
................
................
...##########...
...##########...
...#yy####yy#...
...##########...
....###ww###....
..############..
.&############&.
.##.########.##.
.##.###oo###.##.
.##.###oO###.##.
....########....
....########....
...&#########&..
...##########...
"""

GROVE = """
......%.........
.....%%..o......
....%%%.oOo.....
....%%%%.o%%....
.....%%%%%%%....
....########....
....#yy##yy#....
....########....
.%%..######..%%.
.%%%.######.%%%.
..%##########%..
.....##yy##.....
.....#yXXy#.....
.....##yy##.....
....########....
...%########%...
"""

TIDE_ART = """
................
.....%.%.%.%....
....%%%%%%%%....
....%%%%%%%%....
.....%%%%%%.....
....########....
....#yy##yy#....
....########....
.%%..######..%%.
%%.%.######.%.%%
%%%%.##oo##.%%%%
.%%..#oOOo#..%%.
.....##oo##.....
.....######.....
....########....
...##########...
"""

SHADOW_ART = """
................
..%........%....
..%%......%%....
..%%%....%%%....
...%%####%%.....
...########.....
...#XX##XX#.....
...########.....
....######......
..##########....
..##.####.##....
..#..#yy#..#....
.....#yy#.......
....######......
...##.##.##.....
...#..#...#.....
"""

VOID_ART = """
................
.....######.....
....########....
....#XX##XX#....
....########....
.....######.....
................
..%..######..%..
.%%.########.%%.
..%.###yy###.%..
....##yXXy##....
.x..###yy###..x.
.....######.....
................
....%%%%%%%%....
.....%%%%%%.....
"""

CREATOR = """
.....yyyyyy.....
....y......y....
.....yyyyyy.....
.....######.....
....########....
....#vv##vv#....
....###ww###....
%%...######...%%
%%%.########.%%%
%%%%###yy###%%%%
.%%%##yXXy##%%%.
..%%###yy###%%..
...%.######.%...
.....######.....
....%%%%%%%%....
...%%%%%%%%%%...
"""

TOTEMS: dict[str, tuple[str, dict[str, st.Ramp], dict]] = {
    "totem_ember": (EMBER, {"A": BASALT, "B": EMBER_ROCK, "C": LAVA}, {"grain": {"A": 0.1}}),
    "totem_frost": (FROST, {"A": ICE, "B": ICE_DEEP, "C": FROST_GLOW}, {}),
    "totem_storm": (STORM_ART, {"A": STORM, "B": STORM_TRIM, "C": BOLT}, {}),
    "totem_stone": (STONE_ART, {"A": STONE, "E": MOSS, "C": AMBER, "D": AMBER}, {"grain": {"A": 0.14}}),
    "totem_grove": (GROVE, {"A": WOOD, "B": LEAF, "C": BLOOM, "D": PETAL}, {"grain": {"A": 0.1}}),
    "totem_tide": (TIDE_ART, {"A": TIDE, "B": CORAL, "C": AQUA, "D": PEARL}, {}),
    "totem_shadow": (SHADOW_ART, {"A": SHADOW, "B": SHADOW_TRIM, "C": VIOLET}, {}),
    "totem_void": (VOID_ART, {"A": VOID, "B": VOID_TRIM, "C": STAR}, {}),
    "totem_creator": (CREATOR, {"A": IVORY, "B": GOLD, "C": SUN}, {}),
}


def totem(name: str) -> Image.Image:
    art, ramps, opts = TOTEMS[name]
    s = paint(art, grain=opts.get("grain"), seed=sum(map(ord, name)))
    if name == "totem_void":
        # Sternenpunkte im Körper der Leere
        for x, y in ((5, 2), (10, 4), (4, 8), (10, 12), (7, 14)):
            if s.get(x, y) and s.get(x, y)[0] in ("A", "B"):
                s.set(x, y, ("C", 5))
    return s.image(ramps)


# =============================================================================
# Äpfel der Macht
# =============================================================================

APPLE_BODY = """
................
................
................
....####.####...
...###########..
..#############.
..#############.
..#############.
..#############.
..#############.
..#############.
...###########..
...###########..
....####.####...
................
................
"""

DRAGON_RED = st.ramp("#c8301e", dark=0.18, light=0.9, cool=345, hue_dark=14, warm=50, hue_light=16, chroma_light=0.7)
DRAGON_GOLD = st.fixed("#6a3a06", "#9a5a0a", "#c88212", "#eeb02a", "#ffd65a", "#ffeb9c", "#fffbe0")
HORN = st.ramp("#d8c8a0", dark=0.28, light=0.96, cool=40, warm=90)
NIGHT = st.ramp("#2c3a8a", dark=0.12, light=0.8, cool=275, warm=215, chroma_light=0.7)
SILVER = st.ramp("#b8c4d8", dark=0.3, light=0.98, cool=250, warm=210, chroma_light=0.4)
DUSK = st.ramp("#3a2450", dark=0.09, light=0.66, cool=290, warm=310, chroma_light=0.7)
THORN = st.ramp("#4a3a5c", dark=0.14, light=0.7, cool=285)
RADIANT = st.ramp("#f6e6a8", dark=0.5, light=0.995, cool=40, hue_dark=12, warm=95, chroma_light=0.35)
WING = st.ramp("#f4f0e6", dark=0.5, light=0.995, cool=230, warm=80, chroma_light=0.4)


def _mask(text: str) -> set[tuple[int, int]]:
    rows = (text[1:] if text.startswith("\n") else text).rstrip().split("\n")
    return {(x, y) for y, row in enumerate(rows) for x, c in enumerate(row) if c == "#"}


def _apple(slot_cuts=(0.0, 0.36, 0.68, 0.92), grain=0.0, seed=3) -> st.Spr:
    return st.shaded(_mask(APPLE_BODY), "A", cuts=slot_cuts, grain=grain, seed=seed, cell=2.0)


DRAGON_TOP = """
.........b......
........cb.de...
.......cb.dfe...
"""

DRAGON_SCALES = (  # Schuppenbögen: (x, y) dunkler Bogenrand
    (4, 6), (5, 7), (6, 6), (8, 6), (9, 7), (10, 6), (12, 6),
    (3, 9), (4, 10), (5, 9), (7, 9), (8, 10), (9, 9), (11, 9), (12, 10), (13, 9),
    (5, 12), (6, 12), (9, 12), (10, 12),
)


def dragon_apple() -> Image.Image:
    s = _apple()
    for x, y in DRAGON_SCALES:
        p = s.get(x, y)
        if p:
            s.set(x, y, ("A", max(1, p[1] - 2)))
    # goldene Schuppenspitzen und der Glanz
    for x, y in ((5, 6), (9, 6), (4, 9), (8, 9), (12, 9)):
        if s.get(x, y):
            s.set(x, y, ("C", 4))
    for x, y in ((4, 5), (5, 4), (4, 4)):
        if s.get(x, y):
            s.set(x, y, ("C", 6))
    s.over(st.Spr.art(DRAGON_TOP, legend={"b": ("B", 3), "c": ("B", 5), "d": ("C", 3), "e": ("C", 5), "f": ("C", 6)}))
    return s.outline(prefer=("A", "B", "C")).image({"A": DRAGON_RED, "B": HORN, "C": DRAGON_GOLD})


STAR_TOP = """
........b.......
.......cb.......
.......b........
"""


def star_apple() -> Image.Image:
    s = _apple(slot_cuts=(-0.1, 0.3, 0.64, 0.9))
    for x, y in ((6, 6), (11, 8), (4, 9), (9, 11), (12, 5), (7, 9), (5, 12)):
        if s.get(x, y):
            s.set(x, y, ("C", 4))
    # ein heller Stern mit Strahlen
    for (x, y), lvl in {(9, 6): 6, (8, 6): 5, (10, 6): 5, (9, 5): 5, (9, 7): 5, (4, 5): 6}.items():
        if s.get(x, y):
            s.set(x, y, ("C", lvl))
    s.over(st.Spr.art(STAR_TOP, legend={"b": ("B", 3), "c": ("B", 5)}))
    return s.outline(prefer=("A", "B")).image({"A": NIGHT, "B": SILVER, "C": STAR})


SHADOW_TOP = """
.........b......
........bb......
.......bc.......
"""


def shadow_apple() -> Image.Image:
    s = _apple(slot_cuts=(0.1, 0.5, 0.8, 0.96))
    # violette Risse, aus denen es leuchtet
    crack = {(7, 4): 4, (7, 5): 5, (6, 6): 5, (6, 7): 6, (7, 8): 5, (8, 9): 6, (8, 10): 5, (7, 11): 4, (9, 8): 4, (10, 7): 4, (5, 8): 4,
             (4, 9): 3, (11, 6): 3}
    for (x, y), lvl in crack.items():
        if s.get(x, y):
            s.set(x, y, ("C", lvl))
    s.over(st.Spr.art(SHADOW_TOP, legend={"b": ("B", 2), "c": ("B", 4)}))
    return s.outline(prefer=("A", "B"), slots=("A", "B")).image({"A": DUSK, "B": THORN, "C": VIOLET})


IMMORTAL_TOP = """
.....hhhhhh.....
....h...b..h....
.....hhhbhh.....
"""

OPAL = st.fixed("#5a4a8c", "#7c6cb4", "#a096d8", "#c4c2f0", "#e2eafc", "#f4faff", "#ffffff")


def immortal_apple() -> Image.Image:
    s = _apple(slot_cuts=(-0.15, 0.25, 0.6, 0.88))
    # ein Lichtkreuz auf der Schale, rosa und türkise Schimmer
    for (x, y), px in {(8, 7): ("C", 6), (8, 6): ("C", 5), (8, 8): ("C", 5), (7, 7): ("C", 5), (9, 7): ("C", 5), (8, 5): ("C", 4),
                       (8, 9): ("C", 4), (6, 7): ("C", 4), (10, 7): ("C", 4), (4, 5): ("A", 6), (5, 4): ("A", 6),
                       (11, 10): ("D", 4), (12, 9): ("D", 5), (4, 10): ("E", 4), (5, 11): ("E", 5), (11, 5): ("E", 5)}.items():
        if s.get(x, y):
            s.set(x, y, px)
    s.over(st.Spr.art(IMMORTAL_TOP, legend={"b": ("B", 3), "h": ("C", 4)}))
    return s.outline(prefer=("A", "B"), slots=("A", "B")).image({"A": OPAL, "B": GOLD, "C": SUN, "D": PETAL, "E": AQUA})


APPLES: dict[str, Callable[[], Image.Image]] = {
    "dragon_apple": dragon_apple,
    "star_apple": star_apple,
    "shadow_apple": shadow_apple,
    "immortal_apple": immortal_apple,
}

# =============================================================================
# Zustände (18 x 18)
# =============================================================================

SECOND_LIFE_ICON = """
..................
.....hhhhhhhh.....
....h........h....
.....hhhhhhhh.....
..................
...ggg....ggg.....
..gfffg..gfffg....
.gfeeefggfeeefg...
.gfedeeffeeeefg...
.gfeddeeeeeeefg...
..gfeeeeeeeefg....
...gfeeeeeefg.....
....gfeeeefg......
.....gfeefg.......
......gffg........
.......gg.........
..................
..................
"""


def icon(art: str, colors: dict[str, str]) -> Image.Image:
    rows = (art[1:] if art.startswith("\n") else art).rstrip("\n").split("\n")
    assert len(rows) == 18 and all(len(r) == 18 for r in rows), [(i, len(r)) for i, r in enumerate(rows)]
    img = Image.new("RGBA", (18, 18), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, c in enumerate(row):
            if c != ".":
                img.putpixel((x, y), st.hexc(colors[c]))
    return img


def build_all() -> dict[str, Image.Image]:
    out: dict[str, Image.Image] = {}
    for name in TOTEMS:
        out[f"item/{name}"] = totem(name)
    for name, make in APPLES.items():
        out[f"item/{name}"] = make()
    out["mob_effect/second_life"] = icon(SECOND_LIFE_ICON, {"h": "#ffe9a0", "g": "#7a4a08", "f": "#d89a1c", "e": "#ffd24a", "d": "#fff6c8"})
    return out


def build(put) -> None:
    """Für tools/generate_textures.py."""
    for key, img in build_all().items():
        put(key, img)


def preview(target: str) -> None:
    imgs = build_all()
    names = list(imgs)
    cols = 7
    zoom = 10
    cell = 18 * zoom + 14
    rows = (len(names) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * cell + 14, rows * cell * 2 + 14), (40, 44, 52, 255))
    for half, bg in enumerate(((198, 178, 138, 255), (46, 52, 70, 255))):
        for i, n in enumerate(names):
            x, y = 14 + (i % cols) * cell, 14 + (i // cols) * cell + half * rows * cell
            img = imgs[n]
            tile = Image.new("RGBA", (18 * zoom, 18 * zoom), bg)
            tile.alpha_composite(img.resize((img.width * zoom, img.height * zoom), Image.NEAREST), ((18 - img.width) * zoom // 2,) * 2)
            sheet.alpha_composite(tile, (x, y))
    sheet.save(target)
    print(target, len(names), "Bilder")


def main() -> None:
    if "--preview" in sys.argv:
        preview(sys.argv[sys.argv.index("--preview") + 1])
        return
    tex = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src/main/resources/assets/kingdomsatwar/textures")
    for key, img in build_all().items():
        path = os.path.join(tex, key + ".png")
        os.makedirs(os.path.dirname(path), exist_ok=True)
        img.save(path)
    print(len(build_all()), "Texturen geschrieben")


if __name__ == "__main__":
    main()
