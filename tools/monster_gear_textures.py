#!/usr/bin/env python3
"""
Texturen der Waffen und Rüstungen aus Monsterteilen (0.70), im Vanilla-Stil (siehe tools/item_style.py):

  * die Teile: Werwolfspelz, Werwolfsklaue, Moorwurzel, Moorherz, Glutwurmschuppe, Glutzahn, Seelenasche, Aschenschleier;
  * die Waffen: Bild im Inventar (schräg wie ein Schwert) und je ein Atlas (32x32) für das 3D-Modell in der Hand, dazu die
    Leuchtmasken (Pilze der Moorkeule, Adern des Glutzahns, die Klinge der Seelensense);
  * die Rüstungsbilder im Inventar (getragen: tools/armor_art.py) und die Texturen der 3D-Anbauten
    (entity/equipment_parts: Ohren, Schnauze, Pelzkragen; Hörner, Kamm, Rückenstacheln mit Leuchtmaske).

Atlas der Waffen (UV-Einheiten 0..16, je Einheit zwei Pixel): A (0..2) Schaft, B (2..4) Wicklung, C (4..6) Metall/Knochen,
D (6..10) Klinge/Kopf, E (10..12) Leuchten, F (12..14) Pelz/Moos/Schuppen, G (14..16) Klauen/Schneide.
"""
from __future__ import annotations

from typing import Callable

from PIL import Image

import item_style as st
import items_armor as armor
from sea_textures import _diagonal_art

# Tonleitern
FUR = st.ramp("#5c5047", dark=0.12, light=0.86, cool=290, chroma_light=0.35)
LEATHER = st.ramp("#6a4428", dark=0.15, light=0.8, cool=20)
BONE = st.ramp("#e2d8c0", dark=0.35, light=0.98)
IRON = st.ramp("#9aa2ac", dark=0.28, light=0.96, chroma_dark=1.3)
ROOT = st.ramp("#5a3e26", dark=0.12, light=0.78, cool=20)
MOSS = st.ramp("#5d7a34", dark=0.18, light=0.88)
MUD = st.ramp("#4a4330", dark=0.12, light=0.72, cool=300)
GLOWCAP = st.ramp("#6ee8c0", dark=0.4, light=1.0, cool=170)
BASALT = st.ramp("#3c3232", dark=0.1, light=0.66, cool=300, chroma_dark=1.0)
EMBER = st.ramp("#ff7a1a", dark=0.35, light=1.0, cool=20, warm=90)
IVORY = st.ramp("#e8d6b0", dark=0.32, light=0.99, warm=70)
BLAZE = st.ramp("#c8781e", dark=0.25, light=0.95, cool=20, warm=80)
ASH = st.ramp("#8c857d", dark=0.14, light=0.92, cool=260)
ASHWOOD = st.ramp("#5e5650", dark=0.12, light=0.8, cool=280)
SOUL = st.ramp("#4fd6ec", dark=0.35, light=1.0, cool=200, chroma_light=0.5)
SOUL_DARK = st.ramp("#1c2a3a", dark=0.06, light=0.5, cool=240)


def render(art: str | st.Spr, ramps: dict[str, st.Ramp], prefer: tuple[str, ...] = ("A", "B", "C", "D", "E"),
           override: dict[str, str] | None = None) -> Image.Image:
    s = st.Spr.art(art) if isinstance(art, str) else art
    return s.outline(prefer=prefer, override=override).image(ramps)


# =============================================================================
# Teile
# =============================================================================

def werewolf_pelt() -> Image.Image:
    """Ein abgezogener, gefalteter Pelz: dunkles Fell mit hellen Strähnen, an einer Ecke die Ohren."""
    art = """
................
................
..3......3......
..43....34......
..4543.3454.....
...45444554.....
...34554545.....
..3455454554....
..4545455545....
..34544545543...
...3454545543...
...34545.4543...
..3443....4443..
..343......343..
...3........3...
................
"""
    return render(art, {"A": FUR})


def werewolf_claw() -> Image.Image:
    """Eine lange, sichelförmig gebogene Klaue: an der Wurzel dunkel und dick, zur hakenförmigen Spitze elfenbeinhell."""
    art = """
................
................
..........jkl...
........ijkllm..
.......ijkl..mn.
......ijkl....n.
.....hijk.......
....hijk........
...3hij.........
..34hi..........
..344h..........
..343...........
...3............
................
................
................
"""
    return render(art, {"A": FUR, "C": BONE}, prefer=("C", "A"))


def bog_root() -> Image.Image:
    """Ein knorriges Wurzelstück, nass glänzend, mit Moos und abzweigenden Fasern."""
    art = """
................
................
............3...
...........34...
..........344...
....3.....45....
.....3...45.....
......345k4.....
.....3454k......
....34544.......
...3454.k.......
..345.k.........
..34..k.........
..3.............
.........k......
................
"""
    return render(art, {"A": ROOT, "C": MOSS}, prefer=("A", "C"))


def bog_heart() -> Image.Image:
    """Ein Herz aus Schlamm und Wurzeln, in dem ein grünes Licht glimmt."""
    art = """
................
................
.....k....k.....
....2k3..3k2....
...234433442....
..23455555432...
..2345nnm5432...
..2345nmm5432...
...234mm5432....
....2345432.....
.....23432......
......232.......
.......2........
................
................
................
"""
    return render(art, {"A": MUD, "C": GLOWCAP}, prefer=("C", "A"))


def ember_scale() -> Image.Image:
    """Eine Glutwurmschuppe: dunkler Basalt in Schildform, am Rand glühend."""
    art = """
................
................
.....rssssr.....
....rs3443sr....
...rs344443sr...
...s34455443s...
...s34555443s...
...s34554432s...
...r3445443.r...
....s34432.s....
....r34432.r....
.....s343.s.....
......s3.s......
.......rr.......
................
................
"""
    return render(art, {"A": BASALT, "D": EMBER}, prefer=("D", "A"))


def ember_fang() -> Image.Image:
    """Ein gebogener Fangzahn, elfenbeinhell, mit glühender Ader und dunkler Wurzel."""
    art = """
................
..........ef....
.........efg....
........deg.....
.......dfg......
......dfs.......
.....dft........
....def.........
...dds..........
...dt...........
..23............
.233............
.23.............
................
................
................
"""
    return render(art, {"A": BASALT, "B": IVORY, "D": EMBER}, prefer=("B", "D", "A"))


def soul_ash() -> Image.Image:
    """Ein Häufchen graue Asche, darin blaue Funken."""
    art = """
................
................
................
................
................
.......s........
......3.........
.....34s3.......
....345443......
...34555443.....
...3455s5443....
..345555s5443...
..3444444443....
................
................
................
"""
    return render(art, {"A": ASH, "D": SOUL}, prefer=("D", "A"))


def ash_veil() -> Image.Image:
    """Ein zerrissener, aschgrauer Stofffetzen mit einem blauen Schimmer."""
    art = """
................
................
...34444443.....
...45555554.....
...4555s5543....
...45555s554....
...355555554....
...45555s55.....
...35555554.....
...4555455......
...355.4554.....
...35..3.54.....
...4...3..3.....
...3............
................
................
"""
    return render(art, {"A": ASH, "D": SOUL}, prefer=("A", "D"))


# =============================================================================
# Waffen im Inventar (schräg wie die Schwerter)
# =============================================================================

def claw_blade_icon() -> Image.Image:
    """Wie das 3D-Modell: eiserner Knauf mit Hakenkralle, Lederwicklung, Pelzkragen, die Faustplatte quer dazu und darüber
    drei Werwolfsklauen – gefächert, an der Wurzel dunkles Horn, zur Spitze elfenbeinhell, alle nach oben gekrümmt."""
    s = st.Spr()
    put = s.set
    # Knauf und Griff
    put(0, 15, ("G", 3))
    put(1, 14, ("B", 3)); put(2, 14, ("B", 2)); put(1, 13, ("B", 5))
    for i, (x, y) in enumerate(((2, 13), (3, 12), (4, 11))):
        put(x, y, ("C", 3 if i % 2 == 0 else 2))
    # Pelzkragen
    for x, y, lv in ((4, 10, 4), (5, 11, 3), (5, 10, 5), (3, 10, 3), (5, 12, 2), (6, 10, 4)):
        put(x, y, ("A", lv))
    # Faustplatte (quer)
    for i, (x, y) in enumerate(((4, 7), (5, 8), (6, 9), (7, 10), (8, 11))):
        put(x, y, ("B", (5, 5, 4, 4, 3)[i]))
    for i, (x, y) in enumerate(((5, 7), (6, 8), (7, 9), (8, 10))):
        put(x, y, ("B", (4, 3, 3, 2)[i]))
    # drei Klauen
    claws = (
        ((5, 6), (5, 5), (6, 4), (6, 3), (7, 2), (7, 1)),
        ((7, 8), (8, 7), (9, 6), (10, 5), (10, 4), (11, 3), (11, 2)),
        ((9, 10), (10, 10), (11, 9), (12, 8), (13, 7), (13, 6), (14, 5)),
    )
    for claw in claws:
        n = len(claw)
        for i, (x, y) in enumerate(claw):
            t = i / (n - 1)
            px = ("E", 2) if t < 0.2 else ("G", min(6, 3 + round((t - 0.2) / 0.8 * 3)))
            put(x, y, px)
    return render(s, {"A": FUR, "B": IRON, "C": LEATHER, "E": HORN, "G": IVORY}, prefer=("G", "E", "B", "A", "C"))


def bog_club_icon() -> Image.Image:
    """Dünner Wurzelgriff mit Rankenband, darüber der knorrige Kopf mit Moospolster, Knochendornen und dem grün glühenden
    Moorherz."""
    def profile(t: float, off: float):
        if t < 0.07:
            return [(1.0, "3")]
        if t < 0.42:
            if 0.17 < t < 0.27:
                return [(0.85, "l" if off < 0 else "j")]
            return [(0.7, "4" if off < 0 else "2")]
        if t < 0.55:
            return [(1.3 + (t - 0.42) * 6, "4" if off < 0 else "2")]
        if 0.62 < t < 0.78 and abs(off) < 1.1:
            return [(1.1, "E" if abs(off) < 0.55 and 0.66 < t < 0.74 else "D")]
        if 0.6 < t < 0.66 and off > 1.7:
            return [(3.0, "R")]
        if 0.8 < t < 0.86 and off < -1.7:
            return [(3.0, "R")]
        if t > 0.9:
            return [(2.0, "l" if (int(t * 40) + int(off * 2)) % 2 else "k")]
        return [(2.15, ("4" if off < 0.4 else "3") if (int(t * 30) + int(off * 2)) % 3 else ("5" if off < -0.6 else "2"))]
    return render(_diagonal_art(profile), {"A": ROOT, "C": MOSS, "E": HEART, "G": BONE}, prefer=("G", "C", "A"),
                  override={"E": "A"})


def ember_fang_icon() -> Image.Image:
    """Basaltknauf mit Glutstein, Griff, Parierstange aus Schuppen, darüber die gebogene Fangklinge mit glühender Ader."""
    def profile(t: float, off: float):
        if t < 0.06:
            return [(0.95, "s" if abs(off) < 0.4 else "3")]
        if t < 0.3:
            return [(0.72, "c" if int(t * 40) % 2 else "e")]
        if t < 0.36:
            return [(2.7, "4" if off < 0 else "2")]
        u = (t - 0.36) / 0.64
        center = -0.9 * u * u                     # die Klinge biegt sich zur Seite
        w = 1.25 * (1.0 - u) ** 0.7 + 0.12
        o = off - center
        if abs(o) <= 0.38 and u < 0.85:
            return [(9.0, "t")]
        if abs(o) <= w:
            return [(9.0, "j" if o < 0 else "l")]
        return []
    return render(_diagonal_art(profile), {"A": BASALT, "B": BLAZE, "C": IVORY, "D": EMBER}, prefer=("D", "C", "A", "B"))


def soul_scythe_icon() -> Image.Image:
    """Ein langer Aschenholzschaft mit Wicklungen, oben eine Knochenfassung und die weit ausladende Klinge aus Seelenfeuer."""
    art = """
................
.....tttts......
...tts....rs....
..ts.......jl...
.ts.......jkl...
.s.......jkl....
.r......k3......
.......43.......
......c3........
.....43.........
....43..........
...43...........
..c3............
.43.............
.k..............
................
"""
    return render(art, {"A": ASHWOOD, "B": ASH, "C": BONE, "D": SOUL}, prefer=("D", "C", "A", "B"))


# =============================================================================
# Atlanten der 3D-Modelle (32x32, zwei Pixel je UV-Einheit)
# =============================================================================

def _atlas() -> tuple[Image.Image, Image.Image]:
    return Image.new("RGBA", (32, 32), (0, 0, 0, 0)), Image.new("RGBA", (32, 32), (0, 0, 0, 0))


def _paint(img: Image.Image, uv: tuple[float, float, float, float], color_at: Callable[[int, int, int, int], tuple]) -> None:
    """Malt einen Texturbereich (UV-Einheiten, je zwei Pixel); color_at bekommt Pixel (x, y) im Bereich und seine Größe."""
    px = img.load()
    x0, y0, x1, y1 = (round(c * 2) for c in uv)
    w, h = x1 - x0, y1 - y0
    for y in range(h):
        for x in range(w):
            c = color_at(x, y, w, h)
            if c is not None:
                px[x0 + x, y0 + y] = c


def _lv(ramp: st.Ramp, level: float) -> tuple:
    return ramp[max(0, min(len(ramp) - 1, int(round(level))))]


HORN = st.ramp("#6a5a4c", dark=0.16, light=0.82, cool=290)
SPECTRAL = st.ramp("#5f8f9c", dark=0.14, light=0.94, cool=230, chroma_light=0.5)
HEART = st.fixed("#0f3a22", "#1d6a3c", "#2fa860", "#4fd88a", "#8ff5b4", "#c8ffd9", "#f0fff4")
STEM = st.ramp("#bfe0cc", dark=0.4, light=0.97, cool=170)
HOT = st.fixed("#5a1606", "#8a2a08", "#c8400c", "#ff6a14", "#ff9a2e", "#ffc860", "#fff0b8")


def _claw_color(t: float, lateral: int, x: int, y: int) -> tuple:
    """Klauenfarbe: t = 0 an der Spitze (hell), 1 an der Wurzel (dunkles Horn); lateral = Stufenverschiebung quer."""
    if t < 0.66:
        level = 6 - t * 4.0 + lateral
        if (y % 7 == 5) and t > 0.2:
            level -= 1                                                     # Wachstumsrillen
        return _lv(IVORY, level)
    if t < 0.78 and (x + y) % 2 == 0:
        return _lv(IVORY, 3 + lateral)                                     # gesprenkelter Übergang
    return _lv(HORN, 4.4 - (t - 0.66) * 5 + lateral)


def claw_blade_atlas() -> tuple[Image.Image, Image.Image]:
    import monster_gear_models as models
    R = models.REGIONS["claw_blade"]
    img, glow = _atlas()
    rnd = st.seeded(11)

    def wrap(x, y, w, h):
        k = (y + x // 2) % 4
        if k == 0:
            return LEATHER[1]
        if k == 1:
            return LEATHER[4] if 0 < x < w - 1 else LEATHER[3]
        return LEATHER[2] if x == w - 1 else LEATHER[3]
    _paint(img, R["wrap"], wrap)
    _paint(img, R["pommel"], lambda x, y, w, h: IRON[5] if y == 0 else (IRON[2] if y == h - 1 else (IRON[4] if x % 3 else IRON[3])))
    _paint(img, R["iron"], lambda x, y, w, h: IRON[5] if (x + y) % 3 == 0 else IRON[4])
    cols = {x: rnd.choice((-1, 0, 0, 1)) for x in range(8)}

    def fur(x, y, w, h):
        v = 4 + cols[x] + (1 if y == 0 else 0)
        if y == h - 1:
            v = 2 if x % 2 else 3                                          # fransiger Saum
        elif y == h - 2 and x % 3 == 1:
            v -= 1
        return _lv(FUR, v)
    _paint(img, R["fur"], fur)
    _paint(img, R["fur_top"], lambda x, y, w, h: _lv(FUR, 4 + ((x * 3 + y * 5) % 4 == 0) - ((x + 2 * y) % 5 == 0)))

    def claw(x, y, w, h):
        lateral = (-1, 1, 0, -1)[min(x, 3)]
        return _claw_color(y / (h - 1), lateral, x, y)
    _paint(img, R["claw"], claw)
    _paint(img, R["claw_edge"], lambda x, y, w, h: _claw_color(y / (h - 1), 1 if x == 0 else 0, x, y))

    def plate(x, y, w, h):
        if (x, y) in ((1, 1), (w - 2, 1), (w // 2 - 3, 2), (w // 2 + 2, 2)):
            return IRON[6]                                                 # Nieten
        if (x, y) in ((1, 2), (w - 2, 2), (w // 2 - 3, 3), (w // 2 + 2, 3)):
            return IRON[1]
        if y == 0:
            return IRON[5]
        if y == h - 1:
            return IRON[2]
        return IRON[4] if (x * 2 + y) % 7 else IRON[3]
    _paint(img, R["plate"], plate)
    _paint(img, R["plate_side"], lambda x, y, w, h: IRON[5] if y == 0 else (IRON[2] if y == h - 1 else IRON[3]))
    return img, glow


def ember_fang_atlas() -> tuple[Image.Image, Image.Image]:
    import monster_gear_models as models
    R = models.REGIONS["ember_fang_blade"]
    img, glow = _atlas()

    def wrap(x, y, w, h):
        k = (y + x) % 5                                                    # eine glutrote Kordel, schräg herumgewickelt
        if k == 0:
            return BLAZE[4] if x in (1, 2) else BLAZE[3]
        if k == 1:
            return BLAZE[2]
        return BASALT[3] if k == 2 else BASALT[2]
    _paint(img, R["wrap"], wrap)

    def scales(x, y, w, h):
        row = y // 2
        sx = (x + (2 if row % 2 else 0)) % 4
        if y % 2 == 0:
            return BASALT[4] if sx in (1, 2) else BASALT[3]
        if sx == 0 and (x + row) % 3 == 0:
            return HOT[1]                                                  # Glut zwischen den Schuppen
        return BASALT[2] if sx in (0, 3) else BASALT[3]
    _paint(img, R["basalt"], scales)

    def wing(x, y, w, h):
        t = y / (h - 1)                                                    # 0 = Spitze
        if t < 0.3:
            return _lv(HOT, 1 + (1 - t / 0.3) * 1.5 + (x == 1))
        if y % 3 == 0:
            return BASALT[2]
        return BASALT[4] if x == 1 else BASALT[3]
    _paint(img, R["wing"], wing)
    _paint(img, R["wing_edge"], lambda x, y, w, h: wing(1, y, 4, h))

    def root(x, y, w, h):
        if y == 0:
            return IVORY[2] if x % 3 else HOT[1]
        return HOT[1] if (x * 3 + y * 5) % 11 == 0 else (BASALT[2] if (x + y) % 2 else BASALT[1])
    _paint(img, R["root"], root)

    cracks = set()
    for start in (2, 5):
        cx = start
        for y in range(31, 16, -1):
            cracks.add((cx, y))
            if y % 3 == 0:
                cx += 1 if (y + start) % 2 else -1
                cx = max(1, min(6, cx))

    def fang(x, y, w, h):
        t = y / (h - 1)                                                    # 0 = Spitze
        if t < 0.08:
            return IVORY[6]
        base = 5.6 - t * 1.6
        lateral = (1.2, 0.5, 0.2, 0.0, 0.0, -0.3, -0.9, -1.6)[x]           # links die Schneide, rechts der Rücken
        level = base + lateral
        if y % 7 == 3 and 1 < x < 6:
            level -= 0.8
        if (x, y) in cracks:
            return HOT[2] if y > 24 else HOT[1]
        if t > 0.78:
            level -= (t - 0.78) * 6
        return _lv(IVORY, level)
    _paint(img, R["fang"], fang)
    _paint(img, R["fang_edge"], lambda x, y, w, h: _lv(IVORY, 6 - y / (h - 1) * 3))

    def vein(x, y, w, h):
        t = y / (h - 1)
        core = x in (1, 2)
        if t < 0.12:
            return HOT[3] if core else HOT[2]
        if core:
            return HOT[6] if (y // 3) % 3 else HOT[5]
        return HOT[4] if (y + x) % 3 else HOT[3]
    _paint(img, R["vein"], vein)
    _paint(glow, R["vein"], vein)
    _paint(img, R["vein_edge"], lambda x, y, w, h: HOT[4])
    _paint(glow, R["vein_edge"], lambda x, y, w, h: HOT[4])

    def gem(x, y, w, h):
        d = abs(x - 1.5) + abs(y - 1.5)
        return HOT[6] if d < 1.1 else (HOT[5] if d < 2.1 else HOT[3])
    for target in (img, glow):
        _paint(target, R["gem"], gem)
        _paint(target, R["hot"], lambda x, y, w, h: _lv(HOT, 6 - y / (h - 1) * 2.5 - (x in (0, w - 1))))
    return img, glow


def bog_club_atlas() -> tuple[Image.Image, Image.Image]:
    import monster_gear_models as models
    R = models.REGIONS["bog_club"]
    img, glow = _atlas()
    rnd = st.seeded(23)

    def root(x, y, w, h):
        if (x + y // 4) % 4 == 0:
            return ROOT[2]
        if (x * 2 + y // 3) % 5 == 2:
            return ROOT[4]
        return ROOT[3]
    _paint(img, R["root"], root)

    def vine(x, y, w, h):
        k = (y - x) % 5
        if k in (0, 1):
            return MOSS[4] if k == 0 else MOSS[3]
        if k == 2 and x == 1:
            return MOSS[5]                                                 # ein Blatt
        return ROOT[2]
    _paint(img, R["vine"], vine)

    def burl(x, y, w, h):
        cx, cy = (w - 1) / 2, h * 0.55
        d = ((x - cx) ** 2 + ((y - cy) * 0.7) ** 2) ** 0.5
        if y < 2:
            return MOSS[4] if (x + y) % 2 else MOSS[3]
        if d < 0.8:
            return ROOT[1]
        if d < 1.6:
            return ROOT[4]
        return ROOT[2] if (x + y) % 3 == 0 else ROOT[3]
    _paint(img, R["burl"], burl)
    _paint(img, R["band"], lambda x, y, w, h: ROOT[3] if y % 2 == 0 and x % 3 else ROOT[1] if (x + y) % 4 == 0 else ROOT[2])

    fissures = set()
    for fx in (1, 4, 7):
        x = fx
        for y in range(14):
            fissures.add((x, y))
            if rnd.random() < 0.35:
                x = max(0, min(9, x + rnd.choice((-1, 1))))
    knot = (6, 9)

    def bark(x, y, w, h):
        dk = abs(x - knot[0]) + abs(y - knot[1])
        if dk == 0:
            return ROOT[1]
        if dk == 1:
            return ROOT[4]
        if (x, y) in fissures:
            return MOSS[3] if y < 3 and x % 2 else ROOT[1]
        if y < 2 and (x * 3 + y) % 4 != 0:
            return MOSS[4] if (x + y) % 2 else MOSS[3]                     # Moos sickert über den Rand
        if (x * 5 + y * 3) % 13 == 0:
            return ROOT[5]                                                 # nasser Glanz
        return ROOT[3] if (x + y // 2) % 3 else ROOT[4]
    _paint(img, R["bark"], bark)

    def end_grain(x, y, w, h):
        cx, cy = (w - 1) / 2, (h - 1) / 2
        d = max(abs(x - cx) * 0.95, abs(y - cy) * 1.1)
        if d >= min(cx, cy) - 0.2:
            return ROOT[2]
        ring = int(d * 1.4) % 2
        return ROOT[4] if ring else ROOT[3]
    _paint(img, R["bark_top"], end_grain)

    def moss(x, y, w, h):
        if y == h - 1:
            return MOSS[2] if x % 2 else None if x % 4 == 1 else MOSS[3]
        return _lv(MOSS, 4 + ((x * 3 + y) % 5 == 0) - ((x + y * 2) % 4 == 0))
    _paint(img, R["moss"], lambda x, y, w, h: moss(x, y, w, h) or MOSS[2])
    _paint(img, R["moss_top"], lambda x, y, w, h: _lv(MOSS, 4 + ((x * 7 + y * 3) % 6 == 0) * 2 - ((x + y) % 5 == 0)))
    _paint(img, R["bone"], lambda x, y, w, h: _lv(BONE, 6 - y / (h - 1) * 3 - (x == 1)))
    _paint(img, R["bone_edge"], lambda x, y, w, h: _lv(BONE, 5 - y / (h - 1) * 2))
    _paint(img, R["strand"], lambda x, y, w, h: _lv(MOSS, 2 + y / (h - 1) * 2.4 + (x == 0)))
    _paint(img, R["strand_edge"], lambda x, y, w, h: _lv(MOSS, 2 + y / (h - 1) * 2))

    def heart(x, y, w, h):
        cx, cy = (w - 1) / 2, (h - 1) / 2 - 0.3
        d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
        if (x in (0, w - 1) and y in (0, h - 1)) or (y == h - 1 and x in (1, w - 2)):
            return HEART[0]                                                # die Ecken: dunkles Gewebe
        if (x == int(cx) and y > cy + 1) or (y == int(cy) and x == 0):
            return HEART[1]                                                # Adern
        if d < 1.0:
            return HEART[6]
        if d < 1.9:
            return HEART[5] if (x + y) % 2 else HEART[4]
        return HEART[3] if (x + y) % 3 else HEART[2]
    for target in (img, glow):
        _paint(target, R["heart"], heart)
        _paint(target, R["heart_side"], lambda x, y, w, h: HEART[2] if y in (0, h - 1) else HEART[3])

        def cap(x, y, w, h):
            if (x, y) in ((1, 1), (2, 2)):
                return GLOWCAP[6]
            return GLOWCAP[5] if y < h // 2 else GLOWCAP[3]
        _paint(target, R["cap"], cap)
        _paint(target, R["stem"], lambda x, y, w, h: STEM[5] if x == 0 else STEM[4])
    return img, glow


def soul_scythe_atlas() -> tuple[Image.Image, Image.Image]:
    import monster_gear_models as models
    R = models.REGIONS["soul_scythe"]
    img, glow = _atlas()
    rnd = st.seeded(31)
    grain = {x: rnd.choice((-1, 0, 0, 1)) for x in range(4)}
    knots = {(1, 9), (2, 23)}

    def shaft(x, y, w, h):
        if (x, y) in knots:
            return ASHWOOD[1]
        if any(abs(x - kx) + abs(y - ky) == 1 for kx, ky in knots):
            return ASHWOOD[4]
        v = 3 + grain[x] + (1 if (y * 3 + x) % 11 == 0 else 0)
        return _lv(ASHWOOD, v)
    _paint(img, R["shaft"], shaft)

    def wrap(x, y, w, h):
        if y % 3 == 2:
            return ASH[2]
        if y == 3:
            return SOUL[3]                                                 # ein bläulicher Faden
        return ASH[5] if y % 3 == 0 and 0 < x < w - 1 else ASH[4]
    _paint(img, R["wrap"], wrap)

    def ribbon(x, y, w, h):
        t = y / (h - 1)                                                    # 0 = das freie Ende
        if t < 0.15:
            return SPECTRAL[2] if (x + y) % 2 else ASH[2]                  # ausgefranst, bläulich
        return _lv(ASH, 4.2 - (1 - t) * 1.2 - (x == w - 1)) if (x + y) % 5 else SPECTRAL[3]
    _paint(img, R["ribbon"], ribbon)
    _paint(img, R["ribbon_edge"], lambda x, y, w, h: ASH[3])

    def bone(x, y, w, h):
        if (x, y) in ((1, 2), (2, 3), (2, 4)):
            return BONE[2]                                                 # Riss
        return BONE[5] if y == 0 else (BONE[3] if y == h - 1 else BONE[4])
    _paint(img, R["bone"], bone)
    _paint(img, R["spike"], lambda x, y, w, h: _lv(BONE, 6 - y / (h - 1) * 3 - (x == w - 1)))
    _paint(img, R["spike_edge"], lambda x, y, w, h: _lv(BONE, 5 - y / (h - 1) * 2))

    def blade(x, y, w, h):
        t = y / (h - 1)                                                    # 0 = Spitze
        if x >= w - 2:
            return SOUL_DARK[3] if x == w - 2 else SOUL_DARK[2]            # der dunkle Rücken
        ripple = (x + int(round(1.5 * __import__("math").sin(y / 2.5)))) % 3 == 0
        level = (5.0, 4.4, 3.8, 3.6, 3.6, 3.2)[x] + (0.6 if ripple else 0.0) + (0.8 if t < 0.12 else 0.0)
        if x in (3, 4) and y % 8 == 4:
            return SOUL[4]                                                 # eingeätzte Runen
        return _lv(SPECTRAL, level)
    _paint(img, R["blade"], blade)
    _paint(img, R["blade_edge"], lambda x, y, w, h: SOUL_DARK[4] if (x + y) % 3 else SOUL_DARK[3])
    for target in (img, glow):
        _paint(target, R["edge"], lambda x, y, w, h: SOUL[6] if x == 0 else SOUL[5])
        _paint(target, R["edge_side"], lambda x, y, w, h: SOUL[6])
        _paint(target, R["eyes"], lambda x, y, w, h: SOUL[6])
        _paint(target, R["flame"], lambda x, y, w, h: _lv(SOUL, 6 - y / (h - 1) * 2.4 - (x in (0, w - 1)) * 0.8))

    def skull(x, y, w, h):
        # 6x6: Stirn, zwei Augenhöhlen, Nasenloch, Zahnreihe
        sockets = {(1, 2), (1, 3), (4, 2), (4, 3)}
        if (x, y) in sockets:
            return SOUL_DARK[1]
        if (x, y) in ((2, 4), (3, 4)):
            return BONE[2]                                                 # Nasenloch
        if y == 5:
            return BONE[5] if x % 2 else BONE[2]
        if y == 0:
            return BONE[6] if 0 < x < w - 1 else BONE[5]
        return BONE[5] if (x + y) % 4 else BONE[4]
    _paint(img, R["skull"], skull)
    _paint(img, R["skull_side"], lambda x, y, w, h: BONE[2] if (x, y) in ((2, 1), (3, 2), (3, 3)) else (BONE[5] if y < 2 else BONE[4]))
    return img, glow


# =============================================================================
# Rüstungen im Inventar
# =============================================================================

PELT_HELMET = """
..2..........2..
..32........23..
..3............3
................
................
................
................
................
................
................
"""

PELT_CHEST = """
................
................
................
................
....jjjjjjjj....
.....j....j.....
......j..j......
.......lm.......
"""

PELT_LEGS = """
................
................
..jjjjjjjjjjjj..
"""

PELT_BOOTS = """
................
................
................
................
................
................
................
................
................
..2222....2222..
.233332..233332.
"""

EMBER_HELMET = """
................
.n............l.
.mn..........lk.
..ml........lk..
...ll......kk...
................
.......ts.......
.......sr.......
"""

EMBER_CHEST = """
................
................
................
................
................
.......ts.......
.......sr.......
......s..r......
.......sr.......
.......sr.......
.......sr.......
.......rq.......
"""

EMBER_LEGS = """
................
................
..rrssttssrrqq..
"""

EMBER_BOOTS = """
................
................
................
................
...rss....srq...
"""


def _fur_pattern(s: st.Spr) -> st.Spr:
    """Fell im Inventarbild: senkrechte Strähnen (jede zweite Spalte eine Stufe heller oder dunkler)."""
    for x, y, (sl, lv) in list(s.cells()):
        if sl == "A" and 1 < lv < 6:
            s.set(x, y, ("A", st.clamp(lv + (1 if (x * 3 + y // 2) % 4 == 0 else (-1 if (x + y) % 5 == 0 else 0)), 1, 6)))
    return s


def armor_icons(put: Callable[[str, Image.Image], None]) -> None:
    pieces = (("helmet", armor.OPEN_HELMET), ("chestplate", armor.CHESTPLATE), ("leggings", armor.LEGGINGS), ("boots", armor.BOOTS))
    for (piece, shape), overlay in zip(pieces, (PELT_HELMET, PELT_CHEST, PELT_LEGS, PELT_BOOTS)):
        s = _fur_pattern(armor.base(shape))
        put(f"item/werewolf_pelt_{piece}", armor.finish(s, overlay, {"A": FUR_SOFT, "B": LEATHER, "C": BONE}, prefer=("A", "C", "B")))
    for (piece, shape), overlay in zip(pieces, (EMBER_HELMET, EMBER_CHEST, EMBER_LEGS, EMBER_BOOTS)):
        s = armor.scale_pattern(armor.base(shape))
        put(f"item/ember_scale_{piece}", armor.finish(s, overlay, {"A": BASALT, "C": BASALT, "D": EMBER}, prefer=("A", "C", "D")))


# =============================================================================
# Die 3D-Anbauten der Rüstungen (64x32, Kastenabwicklung wie in MonsterArmorLayer)
# =============================================================================

def _box(img: Image.Image, u: int, v: int, w: int, h: int, d: int, color_at: Callable[[int, int, str, int, int], tuple | None]) -> None:
    """
    Füllt die Kastenabwicklung eines Quaders wie im Spiel (oben/unten in der ersten Reihe, darunter West, Nord = vorn, Ost,
    Süd = hinten). color_at bekommt Pixel und Seite samt Seitengröße; None bleibt durchsichtig. Bei den Seiten liegt
    Zeile 0 am Ende mit dem kleinsten y (im Modell oben bzw. bei Hörnern und Stacheln an der Spitze).
    """
    px = img.load()
    faces = {
        "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
        "west": (u, v + d, d, h), "north": (u + d, v + d, w, h), "east": (u + d + w, v + d, d, h), "south": (u + 2 * d + w, v + d, w, h),
    }
    for face, (x0, y0, fw, fh) in faces.items():
        for y in range(y0, y0 + fh):
            for x in range(x0, x0 + fw):
                if 0 <= x < img.width and 0 <= y < img.height:
                    c = color_at(x - x0, y - y0, face, fw, fh)
                    if c is not None:
                        px[x, y] = c


FUR_LIGHT = st.ramp("#9a8c7c", dark=0.24, light=0.92, cool=290, chroma_light=0.35)
AMBER = st.fixed("#3a2406", "#6a3c08", "#a8600c", "#e08a16", "#ffb02a", "#ffd060", "#fff0b0")
SIDES = ("west", "north", "east", "south")


FUR_SOFT = st.ramp("#5c5047", dark=0.16, light=0.68, cool=290, chroma_light=0.35)
FUR_SOFT_LIGHT = st.ramp("#8a7c6c", dark=0.24, light=0.8, cool=290, chroma_light=0.35)


def _fur_px(seed: int, light: bool = False, ragged: bool = False, stripe: tuple[int, int] | None = None):
    """
    Pelz wie auf der getragenen Rüstung: jede Spalte eine Folge von Strähnen (Läufe von 2–4 Pixeln, meist Grundton, öfter
    dunkler, selten heller), oben ein Glanz; unten ausgefranst (durchsichtige Lücken); optional ein dunkler Rückenstrich.
    """
    rnd = st.seeded(seed)
    runs: dict[tuple[str, int], list[int]] = {}
    ramp = FUR_SOFT_LIGHT if light else FUR_SOFT

    def column(face: str, x: int, fh: int) -> list[int]:
        key = (face, x)
        if key not in runs:
            col: list[int] = []
            while len(col) < fh:
                col += [rnd.choices((0, -1, 1), weights=(6, 3, 1))[0]] * rnd.choice((2, 3, 3, 4))
            runs[key] = col
        return runs[key]

    def at(x: int, y: int, face: str, fw: int, fh: int):
        if face in ("top", "bottom"):
            mod = column(face, x, fw * 4)[y * 3 % (fw * 4)]
            return _lv(ramp, 4 + mod - (1 if face == "bottom" else 0))
        level = 4 + column(face, x, fh)[y]
        if y == 0:
            level += 1
        if stripe and face == "south" and stripe[0] <= x <= stripe[1]:
            level -= 1                                                     # dunkler Rückenstrich
        if ragged and y == fh - 1 and (x * 7 + seed) % 3 == 0:
            return None                                                    # Fransen
        if ragged and y >= fh - 2:
            level -= 1
        return _lv(ramp, level)
    return at


def pelt_parts() -> tuple[Image.Image, Image.Image]:
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    glow = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    fur = _fur_px(70)
    lip, mouth, nose = st.hexc("#2a1f1c"), st.hexc("#5a2a28"), st.hexc("#151112")

    def muzzle(x, y, face, fw, fh):
        if face == "top":
            return _lv(FUR_SOFT, 4.6 if 1 <= x <= 2 else 3.8)              # heller Nasenrücken
        if face == "bottom":
            if y == 0 or x in (0, fw - 1):
                return BONE[6] if (x + y) % 2 else BONE[5]                 # Zähne am Rand
            return mouth
        if face == "north":
            return lip if y == fh - 1 else _lv(FUR_SOFT, 4.2 - y * 0.4)
        if face in ("west", "east"):
            if y == fh - 1:
                return BONE[6] if x % 2 else lip                           # Zahnreihe an den Lefzen
            return _lv(FUR_SOFT_LIGHT if y == 1 else FUR_SOFT, 3.6 + (x % 2) * 0.5)
        return fur(x, y, face, fw, fh)
    _box(img, 0, 0, 4, 3, 4, muzzle)
    _box(img, 16, 0, 2, 1, 1, lambda x, y, f, fw, fh: st.hexc("#3a3236") if f == "top" and x == 0 else nose)
    _box(img, 24, 0, 9, 1, 1, lambda x, y, f, fw, fh: _lv(FUR_SOFT, 4.4 if f == "top" else 3.2 + (x % 2) * 0.6))

    def ear(x, y, face, fw, fh):
        if face == "north" and x == 1:
            return st.hexc("#8a5a52") if y > 0 else st.hexc("#6a4440")     # das Innere des Ohrs
        return _lv(FUR_SOFT, 3.6 + (0.8 if face == "top" else 0) - (0.5 if x == fw - 1 else 0))

    def ear_tip(x, y, face, fw, fh):
        return _lv(FUR_SOFT, 3.0 if face != "top" else 3.4)
    for u in (0, 14):
        _box(img, u, 8, 3, 2, 1, ear)
        _box(img, u + 8, 8, 2, 1, 1, ear_tip)
    for u in (28, 34):
        _box(img, u, 8, 1, 2, 1, lambda x, y, f, fw, fh: BONE[6] if y == fh - 1 or f == "bottom" else BONE[5])
    for v in (8, 10):
        for target, color in ((img, AMBER[4]), (glow, AMBER[5])):
            target.putpixel((40, v), color)
            target.putpixel((41, v), color)
    _box(img, 0, 12, 10, 3, 6, _fur_px(71, ragged=True))
    _box(img, 0, 22, 6, 2, 6, _fur_px(72, ragged=True))
    _box(img, 24, 22, 6, 2, 6, _fur_px(73, light=False, ragged=True))
    def cape(seed: int, w: int, h: int, ragged: bool):
        fur = _fur_px(seed, ragged=ragged, stripe=(w // 2 - 1, w // 2 + 1))

        def at(x, y, face, fw, fh):
            if face == "north":                                            # innen: Leder
                if ragged and y == fh - 1 and (x * 7) % 3 == 0:
                    return None
                return LEATHER[2] if (x + y) % 5 else LEATHER[1]
            return fur(x, y, face, fw, fh)
        _box(img, 0, 32 if seed == 74 else 40, w, h, 1, at)
    cape(74, 10, 5, False)
    cape(79, 9, 6, True)
    _box(img, 24, 32, 6, 2, 6, _fur_px(75, ragged=True))
    _box(img, 48, 0, 2, 3, 2, _fur_px(76))
    _box(img, 48, 6, 2, 3, 2, _fur_px(77))
    tip = _fur_px(78, light=True)
    _box(img, 48, 12, 2, 2, 2, lambda x, y, f, fw, fh: _lv(FUR_SOFT_LIGHT, 5) if f == "bottom" or y == fh - 1 else tip(x, y, f, fw, fh))
    return img, glow


def ember_parts() -> tuple[Image.Image, Image.Image]:
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    glow = Image.new("RGBA", (64, 64), (0, 0, 0, 0))

    def both(u, v, w, h, d, base, lit=None):
        _box(img, u, v, w, h, d, base)
        if lit:
            _box(glow, u, v, w, h, d, lit)

    def horn(heat: float):
        def at(x, y, face, fw, fh):
            if face in ("top", "bottom"):
                return BASALT[3]
            ring = y % 2 == 0
            level = 3.3 + (0.7 if x == 0 else 0) - (0.8 if ring else 0)
            if heat > 0 and (x + y) % 3 == 0:
                return HOT[1]
            return _lv(BASALT, level)
        return at
    both(0, 0, 2, 3, 2, horn(0.0))
    both(8, 0, 2, 3, 2, horn(0.5))
    both(16, 0, 1, 2, 1, lambda x, y, f, fw, fh: HOT[1] if y == 0 or f == "top" else BASALT[2])
    both(20, 0, 1, 2, 1, lambda x, y, f, fw, fh: HOT[2] if y == 0 or f == "top" else HOT[1],
         lambda x, y, f, fw, fh: HOT[4] if y == 0 or f == "top" else HOT[3])
    both(24, 0, 1, 2, 2, lambda x, y, f, fw, fh: HOT[2] if (y == 0 or f == "top") else BASALT[2 + (x % 2)],
         lambda x, y, f, fw, fh: HOT[3] if (y == 0 or f == "top") else None)
    both(32, 0, 9, 1, 1, lambda x, y, f, fw, fh: (HOT[2] if f == "north" and x % 3 == 1 else BASALT[4 if f == "top" else 3]),
         lambda x, y, f, fw, fh: HOT[3] if f == "north" and x % 3 == 1 else None)

    def spike(x, y, face, fw, fh):
        if face == "south":
            return BASALT[2]
        return BASALT[3] if (x + y) % 2 else BASALT[2]
    both(0, 8, 2, 2, 3, spike)
    both(10, 8, 1, 1, 2, lambda x, y, f, fw, fh: HOT[2], lambda x, y, f, fw, fh: HOT[3] if f != "south" else HOT[4])

    def plate(x, y, face, fw, fh):
        if face == "top":
            row = y // 2
            sx = (x + (2 if row % 2 else 0)) % 4
            if y % 2 == 1 and sx == 0:
                return HOT[1]
            return BASALT[4] if (y % 2 == 0 and sx in (1, 2)) else BASALT[3]
        if face == "bottom":
            return BASALT[1]
        if y == 0:
            return BASALT[4]
        return HOT[2] if (y == fh - 1 and x % 3 == 1) else BASALT[2]
    both(0, 16, 7, 2, 7, plate, lambda x, y, f, fw, fh: HOT[3] if f in SIDES and y == fh - 1 and x % 3 == 1 else None)
    both(28, 16, 1, 3, 1, lambda x, y, f, fw, fh: HOT[2] if (y == 0 or f == "top") else BASALT[3 - (y == fh - 1)],
         lambda x, y, f, fw, fh: HOT[4] if (y == 0 or f == "top") else None)
    return img, glow


# =============================================================================
# Ausgabe
# =============================================================================

def build(put: Callable[[str, Image.Image], None]) -> None:
    for name, fn in (("werewolf_pelt", werewolf_pelt), ("werewolf_claw", werewolf_claw), ("bog_root", bog_root), ("bog_heart", bog_heart),
                     ("ember_scale", ember_scale), ("ember_fang", ember_fang), ("soul_ash", soul_ash), ("ash_veil", ash_veil)):
        put(f"item/{name}", fn())
    put("item/claw_blade", claw_blade_icon())
    put("item/bog_club", bog_club_icon())
    put("item/ember_fang_blade", ember_fang_icon())
    put("item/soul_scythe", soul_scythe_icon())
    for name, fn in (("claw_blade", claw_blade_atlas), ("bog_club", bog_club_atlas), ("ember_fang_blade", ember_fang_atlas),
                     ("soul_scythe", soul_scythe_atlas)):
        img, glow = fn()
        put(f"item/{name}_model", img)
        put(f"item/{name}_model_glow", glow)
    armor_icons(put)
    parts, glow = pelt_parts()
    put("entity/equipment_parts/werewolf_pelt", parts)
    put("entity/equipment_parts/werewolf_pelt_glow", glow)
    parts, glow = ember_parts()
    put("entity/equipment_parts/ember_scale", parts)
    put("entity/equipment_parts/ember_scale_glow", glow)


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key.split("/")[-1], img))
    st.preview({k: v for k, v in sheet.items() if v.size == (16, 16)}, sys.argv[1] if len(sys.argv) > 1 else "monster_gear.png", zoom=8,
               cols=10)
