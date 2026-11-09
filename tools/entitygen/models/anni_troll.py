"""
Anni der Troll: eine riesige, bucklige Trollfrau, die in einer Fehlerzone der Oberwelt haust – dort, wo die Welt selbst
kaputtgegangen ist, und Anni gleich mit. Moosgraue, warzige Haut, ein gewaltiger Schädel mit Unterbiss, zwei Hauern und
einer Knollennase, Segelohren mit goldenen Ringen und – wie bei einem Trollpüppchen – ein hoch aufragender Haarschopf,
der genau in der Mitte die Farbe wechselt: links Magenta, rechts Cyan (ein Farbfehler, der an ihr hängen geblieben ist).
Über dem Schopf schweben drei Pixel als Krone (rot, grün, blau). Sie trägt einen Kittel aus Flicken mit einem Gürtel, auf
dessen Schnalle ein Warnzeichen sitzt; ein Flicken ist ein Stück „fehlender Textur“. Ihr linker Unterarm ist in drei
Brocken zerfallen, die mit kleinen Lücken übereinander schweben, ihr rechter Unterschenkel und Fuß sind nur noch fehlende
Textur, und ihr rechtes Auge ist ein flimmernder Bildschirm. In der rechten Hand schleift sie den Bannhammer: einen
Steinklotz mit Eisenbändern an einem Baumstamm-Stiel – dessen vordere Hälfte ebenfalls fehlt.

Im Spiel 1,2-fach vergrößert (gut vier Blöcke bis zur Haarspitze). Knochen-Namen werden in AnniTrollModel (Java) für die
Animationen verwendet.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, hexc, mix, noise3, pick, ramp, rel_y


def stops(*colors: str) -> list:
    return [hexc(c) for c in colors]


PALETTES = {
    "anni_troll": {
        "skin": ramp("#20302c", "#9fb49c", steps=8),
        "wart": ramp("#1a2622", "#6e8668", steps=5),
        "blush": ramp("#5a3a3a", "#d8968e", steps=5),
        "nose": ramp("#3a3a34", "#c8a49a", steps=6),
        "tusk": ramp("#5a4e3a", "#fff6dc", steps=6),
        "mouth": ramp("#0c0606", "#5a2424", steps=4),
        "eye": stops("#2a1400", "#7a3a00", "#d07a10", "#ffc030", "#fff0a0"),
        "screen": stops("#04141c", "#063a4a", "#0a7a90", "#20d0e8", "#a0fcff", "#ffffff"),
        "magenta": stops("#3a0630", "#7a0e66", "#c01a9c", "#f040c8", "#ff86e2", "#ffd0f4"),
        "cyan": stops("#03303a", "#06606e", "#0c9eb0", "#22d8e8", "#86f4ff", "#d8ffff"),
        "gold": ramp("#4a3008", "#ffe28a", steps=6),
        "copper": ramp("#3a1a0c", "#e8905a", steps=6),
        "leather": ramp("#160c06", "#6a4626", steps=6),
        "burlap": ramp("#2a1e10", "#a08458", steps=6),
        "mustard": ramp("#3a2a06", "#d8b040", steps=6),
        "denim": ramp("#141e30", "#6a84a8", steps=6),
        "olive": ramp("#1c2410", "#7a8a48", steps=6),
        "rust": ramp("#2a0e08", "#b0583a", steps=6),
        "thread": ramp("#6a5a40", "#f0e2b8", steps=4),
        "wood": ramp("#1a0e06", "#7a5232", steps=7),
        "rag": ramp("#2a2018", "#a89a80", steps=5),
        "stone": ramp("#1e1e22", "#9a9aa2", steps=8),
        "iron": ramp("#0c0c10", "#6a6a72", steps=6),
        "nail": ramp("#3a3428", "#c8bca0", steps=5),
        "red": stops("#3a0000", "#a00010", "#ff2030", "#ff8a8a"),
        "green": stops("#003a10", "#00a030", "#30ff60", "#a0ffb0"),
        "blue": stops("#00103a", "#0030b0", "#3070ff", "#a0c0ff"),
        "warn": stops("#3a2a00", "#c09000", "#ffd400", "#fff080"),
    },
}

# Pixel, die um Anni schweben: (Knochen, Elternknochen, Lage, Größe, Farbe)
PIXELS = [
    ("pixel_0", "body", (11.5, -21.0, -3.0), 1.6, "px_red"),
    ("pixel_1", "body", (-12.5, -10.0, 2.0), 1.3, "px_cyan"),
    ("pixel_2", "body", (10.0, -2.0, 5.0), 1.1, "px_green"),
    ("pixel_3", "body", (-9.0, -22.0, 4.0), 1.2, "px_magenta"),
    ("pixel_4", "body", (13.0, -12.0, -5.0), 0.9, "px_blue"),
    ("pixel_5", "body", (-13.0, 2.0, -3.0), 1.0, "px_yellow"),
]


def build() -> Model:
    m = Model("anni_troll", 256, 256)
    m.tex_res = 2
    # --- Rumpf: tief gebeugt, mit Bauch; der Flickenkittel reicht bis über die Knie
    m.bone("body", pivot=(0, 4, 0), rotation=(18, 0, 0))
    m.cube("body", (-8.5, -3, -5.5), (17, 7, 11), "tunic", tag="hips")
    m.cube("body", (-9.5, -17, -6), (19, 14, 12), "tunic", tag="torso")
    m.cube("body", (-7.5, -12, -8.2), (15, 9, 2.4), "tunic", tag="belly")
    m.cube("body", (-10.2, -19.5, -4.8), (20.4, 3.5, 9.6), "skin", tag="shoulders")
    m.cube("body", (-4.0, -20.5, -3.2), (8, 2, 6.4), "skin", tag="neck")
    m.cube("body", (1.6, -11.0, -8.8), (4.6, 4.6, 0.4), "glitch", tag="patch")
    m.cube("body", (-9.9, -4.4, -6.4), (19.8, 2.6, 12.8), "belt")
    m.cube("body", (-2.2, -5.2, -7.2), (4.4, 4.0, 0.8), "buckle")
    # Saum aus Flicken (schwingt beim Gehen)
    m.bone("skirt", "body", pivot=(0, 4, 0))
    m.cube("skirt", (-9.2, 0, -6.2), (18.4, 5.5, 12.4), "hem")

    # --- Kopf: großer Schädel, Stirnwulst, Knollennase, breites Grinsen über dem Unterkiefer mit zwei Hauern,
    #     spitze Ohren, der Haarschopf
    m.bone("head", "body", pivot=(0, -19, -5), rotation=(-24, 0, 0))
    m.cube("head", (-6.5, -12, -9.5), (13, 12, 11.5), "face", tag="skull")
    m.cube("head", (-6.9, -10.2, -10.4), (13.8, 2.2, 1.6), "brow")
    m.cube("head", (-5.2, -8.0, -9.95), (3.8, 3.0, 0.5), "eye")
    m.cube("head", (1.2, -8.4, -10.05), (4.2, 3.6, 0.6), "screen")
    m.cube("head", (-1.8, -7.4, -12.0), (3.6, 3.0, 2.6), "nose", tag="bridge")
    m.cube("head", (-2.7, -5.6, -12.9), (5.4, 3.0, 3.4), "nose", tag="bulb")
    m.cube("head", (-6.9, -13.4, -8.0), (13.8, 1.8, 12.2), "hair", tag="cap")
    m.cube("head", (-6.8, -12.0, 1.9), (13.6, 9.0, 1.4), "hair", tag="back")
    m.bone("jaw", "head", pivot=(0, -1.0, -5.0), rotation=(0, 0, 0))
    m.cube("jaw", (-6.0, 0, -5.2), (12.0, 3.4, 9.6), "jaw")
    m.cube("jaw", (-5.2, -3.4, -5.0), (1.8, 3.6, 1.6), "tusk")
    m.cube("jaw", (3.4, -3.4, -5.0), (1.8, 3.6, 1.6), "tusk")
    for side, sx in (("left", 1), ("right", -1)):
        ear = f"{side}_ear"
        m.bone(ear, "head", pivot=(6.3 * sx, -7.0, -3.5), rotation=(0, -35 * sx, -18 * sx))
        m.cube(ear, (0 if sx > 0 else -5.2, -1.8, -0.5), (5.2, 3.6, 1.0), "ear")
        m.cube(ear, (4.4 if sx > 0 else -7.0, -3.6, -0.45), (2.6, 2.4, 0.9), "ear", tag="tip")
        m.cube(ear, (6.2 if sx > 0 else -7.6, -5.2, -0.4), (1.4, 1.8, 0.8), "ear", tag="tip")
        m.cube(ear, (1.0 if sx > 0 else -2.5, 1.6, -0.7), (1.5, 1.7, 1.4), "earring")
    # der Haarschopf wie bei einem Trollpüppchen: hoch aufragend und nach hinten geschwungen, links Magenta, rechts Cyan
    m.bone("tuft", "head", pivot=(0, -13.2, -1.0), rotation=(-12, 0, 0))
    m.cube("tuft", (-4.4, -3.6, -4.0), (8.8, 3.8, 8.0), "tuft")
    m.cube("tuft", (-5.0, -8.6, -4.0), (10.0, 5.2, 8.6), "tuft", tag="puff")
    m.cube("tuft", (-3.8, -12.2, -2.6), (7.6, 3.8, 6.6), "tuft")
    m.cube("tuft", (-2.4, -14.8, -1.0), (4.8, 2.8, 4.6), "tuft")
    m.cube("tuft", (-1.2, -16.6, 0.8), (2.4, 2.0, 3.0), "tuft")
    m.cube("tuft", (-0.8, -17.0, 3.6), (1.6, 1.6, 2.0), "tuft")
    m.cube("tuft", (-6.8, -7.6, -1.2), (2.0, 2.4, 2.4), "tuft", tag="wisp")
    m.cube("tuft", (4.8, -9.4, 0.0), (2.2, 2.6, 2.2), "tuft", tag="wisp")
    m.cube("tuft", (-5.2, -1.2, -4.8), (10.4, 1.2, 9.6), "tie")
    # die Pixelkrone: drei Pixel kreisen um den Schopf
    m.bone("crown", "head", pivot=(0, -16.0, -1.0))
    for i, (mat, x, z) in enumerate((("px_red", 6.0, 0.0), ("px_green", -3.0, 5.2), ("px_blue", -3.0, -5.2))):
        m.bone(f"crown_{i}", "crown", pivot=(x, 0, z))
        m.cube(f"crown_{i}", (-1.1, -1.1, -1.1), (2.2, 2.2, 2.2), mat)

    # --- Arme: lang und kräftig; rechts eine Hand am Bannhammer, links ein zerfallener Unterarm
    for side, sx in (("left", 1), ("right", -1)):
        arm = f"{side}_arm"
        m.bone(arm, "body", pivot=(10.6 * sx, -16.0, -0.4), rotation=(-6, 0, -9 * sx))
        m.cube(arm, (-3.2, -2.6, -3.6), (6.4, 12.4, 7.2), "arm")
        fore = f"{side}_forearm"
        m.bone(fore, arm, pivot=(0, 9.6, 0), rotation=(-14, 0, 0))
        if sx < 0:
            m.cube(fore, (-3.6, 0, -3.8), (7.2, 10.2, 7.6), "arm")
            m.cube(fore, (-3.95, 6.4, -4.15), (7.9, 1.9, 8.3), "bracelet")
            m.bone(f"{side}_hand", fore, pivot=(0, 10.0, 0))
            m.cube(f"{side}_hand", (-4.2, 0, -4.4), (8.4, 6.0, 8.8), "hand")
        else:
            # zerfallen: drei Brocken mit Lücken (im Spiel schweben sie auseinander und zusammen)
            for i, (y0, h) in enumerate(((0.0, 3.2), (3.6, 3.0), (7.0, 3.2))):
                part = f"left_shard_{i}"
                m.bone(part, fore, pivot=(0, y0, 0))
                m.cube(part, (-3.6, 0, -3.8), (7.2, h, 7.6), "shard")
            m.bone(f"{side}_hand", fore, pivot=(0, 10.6, 0))
            m.cube(f"{side}_hand", (-4.2, 0, -4.4), (8.4, 6.0, 8.8), "hand")
    # der Bannhammer: Baumstamm-Stiel mit Lappen, Steinklotz mit Eisenbändern, die vordere Hälfte fehlt
    m.bone("hammer", "right_hand", pivot=(0, 3.0, 0), rotation=(0, 0, 0))
    m.cube("hammer", (-1.4, -5.0, -1.4), (2.8, 24.0, 2.8), "wood")
    m.cube("hammer", (-1.75, -2.5, -1.75), (3.5, 5.5, 3.5), "rag")
    m.cube("hammer", (-4.6, 18.0, -6.6), (9.2, 9.2, 13.2), "stone")
    m.cube("hammer", (-4.95, 17.6, -1.6), (9.9, 10.0, 1.6), "iron", tag="band")
    m.cube("hammer", (-4.95, 17.6, 2.6), (9.9, 10.0, 1.6), "iron", tag="band")
    m.cube("hammer", (-4.85, 17.75, -6.85), (9.7, 9.7, 5.4), "glitch", tag="hammer_face")
    for i, (x, y, z, s) in enumerate(((5.6, 20.0, -5.0, 1.4), (-6.4, 23.5, -6.2, 1.1), (1.0, 16.0, -8.4, 1.0))):
        m.bone(f"hammer_px_{i}", "hammer", pivot=(x, y, z))
        m.cube(f"hammer_px_{i}", (-s / 2, -s / 2, -s / 2), (s, s, s), ("px_magenta", "px_cyan", "px_red")[i])

    # --- Beine: kurze, stämmige Säulen mit riesigen, nackten Füßen; rechts nur noch fehlende Textur
    for side, sx in (("left", 1), ("right", -1)):
        leg = f"{side}_leg"
        m.bone(leg, pivot=(5.0 * sx, 7.0, 0.6))
        m.cube(leg, (-3.9, 0, -3.9), (7.8, 8.6, 7.8), "leg")
        shin = f"{side}_shin"
        m.bone(shin, leg, pivot=(0, 8.2, 0))
        glitched = sx < 0
        m.cube(shin, (-3.5, 0, -3.5), (7.0, 7.4, 7.0), "glitch" if glitched else "leg", tag="calf")
        m.cube(shin, (-4.1, 6.6, -7.4), (8.2, 2.2, 10.8), "glitch" if glitched else "foot", tag="foot")

    # --- Pixel, die sich von ihr gelöst haben und um sie schweben
    for name, parent, pos, size, mat in PIXELS:
        m.bone(name, parent, pivot=pos)
        h = size / 2
        m.cube(name, (-h, -h, -h), (size, size, size), mat)
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

_LIT = {"top": 0.1, "front": 0.04, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.16}
_WARTS = Voronoi3D(2.6, 811)
_PATCH = 4.6


def _n(ctx: Ctx, scale: float = 1.0, seed: int = 0) -> float:
    x, y, z = ctx.world
    return noise3(x * scale, y * scale, z * scale, seed) - 0.5


def _skin_value(ctx: Ctx, base: float = 0.56) -> float:
    """Moosgraue Trollhaut: große, weiche Flecken, oben heller, die Kanten der Quader dunkler (wirkt rund)."""
    v = base + _LIT[ctx.face] - rel_y(ctx) * 0.1 + _n(ctx, 0.18, 3) * 0.22 + _n(ctx, 0.6, 9) * 0.08
    edge = min(ctx.s, ctx.t, ctx.w - 1 - ctx.s, ctx.h - 1 - ctx.t)
    if edge == 0 and ctx.face not in ("top", "bottom"):
        v -= 0.06
    return v


def _skin(ctx: Ctx):
    x, y, z = ctx.world
    cell, edge = _WARTS(x, y, z)
    v = _skin_value(ctx)
    if cell > 0.9 and edge > 0.55:
        # Warze: ein dunklerer Hof um einen hellen Buckel
        return pick(ctx.pal["wart"], 0.55 + (edge - 0.55) * 0.9)
    return pick(ctx.pal["skin"], v)


def _arm(ctx: Ctx):
    # Knöchel und Ellbogen etwas dunkler, ein paar Narben
    if hash01(ctx.u, ctx.v, "scar") > 0.985 and ctx.face in ("front", "west", "east"):
        return pick(ctx.pal["nose"], 0.55)
    return _skin(ctx)


def _leg(ctx: Ctx):
    return _skin(ctx)


# Gesicht (Vorderseite des Schädels, 26 x 24): Stirnfalten, Augenhöhlen, Wangen mit Rouge und Warzen, das breite Grinsen
_GRIN_TEETH = {5, 6, 8, 9, 11, 12, 13, 14, 16, 17, 19, 20}


def _face(ctx: Ctx):
    if ctx.face != "front":
        if ctx.face in ("top", "back"):
            return _hair(ctx)
        return _skin(ctx)
    s, t = ctx.s, ctx.t
    v = _skin_value(ctx, 0.62)
    # Stirnfalten
    if t in (1, 3) and 6 <= s <= 19 and (s * 3 + t) % 7 != 0:
        v -= 0.1
    # Augenhöhlen (die Augen sitzen davor): ein dunkler Rand
    for cx, cy, rx, ry in ((6.6, 13.0, 4.8, 3.6), (18.8, 12.8, 5.2, 4.2)):
        d = ((s + 0.5 - cx) / rx) ** 2 + ((t + 0.5 - cy) / ry) ** 2
        if d < 1.0:
            v -= 0.16 * (1.0 - d)
    # das Grinsen: breit, die Mundwinkel hochgezogen, oben eine Reihe schiefer Zähne
    if t == 19 and s in (2, 3, 22, 23):
        return pick(ctx.pal["mouth"], 0.15)
    if t == 20 and 3 <= s <= 22:
        if s in _GRIN_TEETH:
            return pick(ctx.pal["tusk"], 0.8 - (0.12 if s in (8, 16) else 0.0))
        return pick(ctx.pal["mouth"], 0.1)
    if t >= 21 and 4 <= s <= 21:
        return pick(ctx.pal["mouth"], 0.3 if t == 21 else 0.15)
    if t >= 21:
        return pick(ctx.pal["skin"], v - 0.12)
    # Wangen: Rouge und je eine Warze
    for cx, cy in ((4.2, 17.0), (21.8, 17.0)):
        d = ((s + 0.5 - cx) / 3.4) ** 2 + ((t + 0.5 - cy) / 2.2) ** 2
        if d < 1.0:
            return mix(pick(ctx.pal["skin"], v), pick(ctx.pal["blush"], 0.72), 0.45 * (1.0 - d) + 0.12)
    if (s, t) in ((2, 15), (23, 16), (22, 15)):
        return pick(ctx.pal["wart"], 0.75)
    # Lachfalten neben der Nase, die Oberlippe
    if t in (16, 17, 18) and s in (7, 18):
        v -= 0.14
    if t == 18 and 4 <= s <= 21:
        v -= 0.06
    return pick(ctx.pal["skin"], v)


def _brow(ctx: Ctx):
    # der Stirnwulst mit buschigen Brauen (zweifarbig wie das Haar)
    if ctx.face in ("front", "top") and ctx.t <= (1 if ctx.face == "front" else 99):
        return _hair(ctx)
    if ctx.face == "front" and ctx.t == 2 and hash01(ctx.u, ctx.v, "brow") > 0.4:
        return _hair(ctx)
    return pick(ctx.pal["skin"], _skin_value(ctx, 0.6))


def _eye(ctx: Ctx):
    """Das gesunde Auge: gelb mit Schlitzpupille, glimmt im Dunkeln."""
    if ctx.face != "front":
        return pick(ctx.pal["skin"], 0.4)
    s, t, w, h = ctx.s, ctx.t, ctx.w, ctx.h
    if t == 0:
        return pick(ctx.pal["skin"], 0.3)                    # Lid
    if s == w // 2 or (s == w // 2 - 1 and 1 <= t <= h - 2):
        col = pick(ctx.pal["eye"], 0.05)                       # Schlitzpupille
        return col
    col = pick(ctx.pal["eye"], 0.85 - abs(s - w / 2) / w * 0.6 - (t / h) * 0.15)
    ctx.glow = mix((0, 0, 0, 255), col, 0.55)
    return col


def _screen(ctx: Ctx):
    """Das kaputte Auge: ein flimmernder Bildschirm mit Zeilen, einem Pixel als Pupille und Farbsäumen."""
    if ctx.face != "front":
        return pick(ctx.pal["iron"], 0.3)
    s, t, w, h = ctx.s, ctx.t, ctx.w, ctx.h
    if s == 0 or t == 0 or s == w - 1 or t == h - 1:
        return pick(ctx.pal["iron"], 0.25)                   # Rahmen
    if 2 <= s <= 3 and 2 <= t <= 3:
        col = pick(ctx.pal["screen"], 0.99)                    # Pupille
    elif s == 4 and 2 <= t <= 3:
        col = pick(ctx.pal["red"], 0.7)                        # Farbsaum rot
    elif s == 1 and 2 <= t <= 3:
        col = pick(ctx.pal["blue"], 0.7)                       # Farbsaum blau
    else:
        col = pick(ctx.pal["screen"], 0.35 + (0.2 if t % 2 == 0 else 0.0) + hash01(ctx.u, ctx.v, "noise") * 0.15)
    ctx.glow = col
    return col


def _nose(ctx: Ctx):
    """Die Knollennase: Trollhaut, zur Spitze hin rötlich, mit Poren; unten zwei Nasenlöcher."""
    v = 0.56 + _LIT[ctx.face] + _n(ctx, 0.5, 21) * 0.2 - rel_y(ctx) * 0.06
    if ctx.cube.tag == "bulb" and ctx.face == "bottom" and ctx.t in (2, 3, 4) and ctx.s in (2, 3, ctx.w - 3, ctx.w - 4):
        return pick(ctx.pal["mouth"], 0.2)                   # Nasenlöcher
    if hash01(ctx.u, ctx.v, "pore") > 0.93:
        v -= 0.15
    red = 0.45 if ctx.cube.tag == "bulb" else 0.25
    return mix(pick(ctx.pal["skin"], v), pick(ctx.pal["nose"], v), red)


def _jaw(ctx: Ctx):
    """Unterkiefer: oben die untere Zahnreihe (schief), darunter die Lippe und ein kräftiges Kinn."""
    if ctx.face == "front":
        s, t, w = ctx.s, ctx.t, ctx.w
        if t == 0:
            if 3 <= s <= w - 4 and s % 3 != 0:
                return pick(ctx.pal["tusk"], 0.66 + (s % 4) * 0.05)
            return pick(ctx.pal["mouth"], 0.15)
        if t == 1:
            return pick(ctx.pal["nose"], 0.42)                   # Unterlippe
        if t == 5 and 8 <= s <= w - 9:
            return pick(ctx.pal["skin"], 0.45)                   # Kinnfurche
    if ctx.face == "top":
        return pick(ctx.pal["mouth"], 0.35 if ctx.t > 2 else 0.2)
    return _skin(ctx)


def _tusk(ctx: Ctx):
    v = 0.4 + (1.0 - rel_y(ctx)) * 0.5 + _LIT[ctx.face]
    if ctx.face == "top":
        v = 0.95
    return pick(ctx.pal["tusk"], v)


def _ear(ctx: Ctx):
    if ctx.face in ("front", "back") and ctx.cube.size[1] > 3:
        # Ohrmuschel: innen rosig und dunkler
        edge = min(ctx.s, ctx.t, ctx.w - 1 - ctx.s, ctx.h - 1 - ctx.t)
        if edge >= 2:
            return mix(pick(ctx.pal["skin"], 0.4), pick(ctx.pal["blush"], 0.5), 0.4)
    return _skin(ctx)


def _earring(ctx: Ctx):
    # ein goldener Ring: außen hell, in der Mitte ein Loch
    if ctx.face in ("front", "back") and 1 <= ctx.s <= ctx.w - 2 and 1 <= ctx.t <= ctx.h - 2:
        return None
    return pick(ctx.pal["gold"], 0.6 + _LIT[ctx.face] * 2 + (0.2 if (ctx.s + ctx.t) % 3 == 0 else 0.0))


def _hair_ramp(ctx: Ctx) -> list:
    return ctx.pal["magenta"] if ctx.world[0] < 0 else ctx.pal["cyan"]


def _hair(ctx: Ctx):
    strand = hash01(ctx.s // 2 if ctx.face in ("front", "back", "west", "east") else ctx.s, ctx.face, "strand")
    v = 0.42 + strand * 0.24 + _LIT[ctx.face] * 1.5 - rel_y(ctx) * 0.1
    col = pick(_hair_ramp(ctx), v)
    if abs(ctx.world[0]) < 0.6:
        col = mix(col, pick(ctx.pal["magenta"] if ctx.world[0] >= 0 else ctx.pal["cyan"], v), 0.5)
    return col


def _tuft(ctx: Ctx):
    """Der Schopf: senkrechte Strähnen, zur Spitze heller; hier und da ein leuchtendes Pixel (Farbfehler)."""
    x, y, z = ctx.world
    strand = hash01(int((x + z) * 2.0), "tuft")
    v = 0.36 + strand * 0.26 + (1.0 - rel_y(ctx)) * 0.22 + _LIT[ctx.face] * 1.4
    if ctx.face == "top":
        v += 0.15
    col = pick(_hair_ramp(ctx), v)
    if abs(x) < 0.55:
        col = mix(pick(ctx.pal["magenta"], v), pick(ctx.pal["cyan"], v), 0.5)
    if hash01(ctx.u, ctx.v, "glitch") > 0.965:
        col = pick(_hair_ramp(ctx), 0.95)
        ctx.glow = col
    return col


def _tie(ctx: Ctx):
    # das Haarband: ein Ring aus goldenen und roten Perlen
    k = (ctx.s // 2) % 3
    return pick(ctx.pal["gold"], 0.75) if k != 1 else pick(ctx.pal["red"], 0.6)


def _patch_index(ctx: Ctx) -> tuple[int, float, float]:
    """In welchem Flicken ein Punkt liegt (Raster im Raum, gegeneinander versetzt) und wie nah am Rand."""
    x, y, z = ctx.world
    row = int((y + 40) // _PATCH)
    off = (row % 2) * _PATCH / 2
    col = int((x + z * 0.7 + 40 + off) // _PATCH)
    fx = ((x + z * 0.7 + 40 + off) % _PATCH) / _PATCH
    fy = ((y + 40) % _PATCH) / _PATCH
    return row * 31 + col, fx, fy


_FABRICS = ("burlap", "mustard", "denim", "olive", "rust", "burlap", "denim")


def _fabric(ctx: Ctx, base: float = 0.5):
    idx, fx, fy = _patch_index(ctx)
    fabric = _FABRICS[int(hash01(idx, "fabric") * len(_FABRICS))]
    # Stiche an den Rändern der Flicken
    edge = min(fx, fy, 1 - fx, 1 - fy)
    if edge < 0.07 and (ctx.u + ctx.v) % 3 == 0:
        return pick(ctx.pal["thread"], 0.55)
    if edge < 0.05:
        return pick(ctx.pal[fabric], 0.18)
    weave = 0.06 if (ctx.u + ctx.v) % 2 == 0 else -0.04
    v = base + _LIT[ctx.face] * 1.5 - rel_y(ctx) * 0.08 + weave + _n(ctx, 0.4, 41) * 0.12
    return pick(ctx.pal[fabric], v)


def _tunic(ctx: Ctx):
    if ctx.face == "top" and ctx.cube.tag == "torso":
        return _skin(ctx)                                     # Schultern und Hals schauen oben heraus
    return _fabric(ctx)


def _hem(ctx: Ctx):
    """Der Saum: ausgefranst – unten fehlen Fetzen."""
    if ctx.face in ("front", "back", "west", "east") and ctx.t >= ctx.h - 3:
        tear = hash01(ctx.s // 2, ctx.face, "tear")
        if ctx.t >= ctx.h - 1 - int(tear * 3):
            return None
    return _fabric(ctx, 0.46)


def _belt(ctx: Ctx):
    if ctx.face in ("front", "back", "west", "east") and ctx.t in (0, ctx.h - 1):
        return pick(ctx.pal["leather"], 0.25)
    if ctx.face in ("front", "back", "west", "east") and ctx.t in (1, ctx.h - 2) and ctx.s % 3 == 0:
        return pick(ctx.pal["thread"], 0.4)
    return pick(ctx.pal["leather"], 0.55 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v) - 0.5) * 0.1)


def _buckle(ctx: Ctx):
    """Die Gürtelschnalle: ein gelbes Warndreieck mit schwarzem Ausrufezeichen."""
    if ctx.face != "front":
        return pick(ctx.pal["iron"], 0.5)
    s, t, w, h = ctx.s, ctx.t, ctx.w, ctx.h
    # Dreieck: Spitze oben in der Mitte
    half = (t + 1) / h * (w / 2)
    dx = abs(s + 0.5 - w / 2)
    if dx > half + 0.2:
        return pick(ctx.pal["iron"], 0.35)
    if dx > half - 1.0 or t == h - 1:
        return pick(ctx.pal["mouth"], 0.05)                  # schwarzer Rand
    if abs(s + 0.5 - w / 2) < 0.6 and (2 <= t <= h - 4 or t == h - 2):
        return pick(ctx.pal["mouth"], 0.05)                  # das Ausrufezeichen
    col = pick(ctx.pal["warn"], 0.7)
    ctx.glow = mix((0, 0, 0, 255), col, 0.4)
    return col


def _bracelet(ctx: Ctx):
    if ctx.face == "front" and ctx.h >= 3 and 1 <= ctx.t <= ctx.h - 2 and ctx.w // 2 - 2 <= ctx.s <= ctx.w // 2 + 1:
        col = (pick(ctx.pal["red"], 0.8), pick(ctx.pal["green"], 0.8), pick(ctx.pal["blue"], 0.8), pick(ctx.pal["red"], 0.8))[
            ctx.s - (ctx.w // 2 - 2)]
        ctx.glow = col
        return col
    return pick(ctx.pal["copper"], 0.55 + _LIT[ctx.face] * 2 + (0.15 if ctx.t == 0 else 0.0))


def _hand(ctx: Ctx):
    # Finger als Rillen, unten die Nägel
    if ctx.face in ("front", "west", "east") and ctx.t >= ctx.h - 3:
        if ctx.face == "front" and ctx.s % 4 == 3:
            return pick(ctx.pal["skin"], 0.25)
        if ctx.t == ctx.h - 1:
            return pick(ctx.pal["nail"], 0.55 + (ctx.s % 4) * 0.05)
    if ctx.face == "front" and ctx.s % 4 == 3 and ctx.t >= ctx.h // 2:
        return pick(ctx.pal["skin"], 0.3)
    return _skin(ctx)


def _foot(ctx: Ctx):
    # große, nackte Füße: vorn fünf dicke Zehen mit Nägeln
    if ctx.face == "front":
        toe = ctx.s * 5 // ctx.w
        local = ctx.s - toe * ctx.w / 5
        if local < 0.8:
            return pick(ctx.pal["skin"], 0.25)
        if ctx.t <= 1:
            return pick(ctx.pal["nail"], 0.6)
    if ctx.face == "top" and ctx.t <= 4:
        if ctx.s * 5 // ctx.w != (ctx.s + 1) * 5 // ctx.w:
            return pick(ctx.pal["skin"], 0.3)
    return _skin(ctx)


def _shard(ctx: Ctx):
    """Ein Brocken des zerfallenen Unterarms: außen Haut, an den Bruchflächen leuchtende Farbsäume und Datenrauschen."""
    if ctx.face in ("top", "bottom"):
        k = hash01(ctx.u, ctx.v, "data")
        ramp_ = (ctx.pal["red"], ctx.pal["green"], ctx.pal["blue"], ctx.pal["magenta"], ctx.pal["cyan"])[int(k * 5)]
        col = pick(ramp_, 0.55 + hash01(ctx.v, ctx.u, "b") * 0.4)
        ctx.glow = col
        return col
    if ctx.t == 0 or ctx.t == ctx.h - 1:
        col = pick(ctx.pal["red"] if ctx.t == 0 else ctx.pal["cyan"], 0.7)
        ctx.glow = col
        return col
    return _skin(ctx)


def _glitch(ctx: Ctx):
    """Fehlende Textur: Magenta und Schwarz im Schachbrett (je ein Feld zwei Einheiten groß); Magenta leuchtet."""
    x, y, z = ctx.world
    if ctx.face in ("front", "back"):
        a, b = x, y
    elif ctx.face in ("west", "east"):
        a, b = z, y
    else:
        a, b = x, z
    cell = (int((a + 64) // 2) + int((b + 64) // 2)) % 2
    if cell == 0:
        col = (248, 0, 248, 255)
        ctx.glow = (200, 0, 200, 255)
        return col
    return (8, 0, 10, 255)


def _wood(ctx: Ctx):
    # der Stiel: ein entrindeter Stamm mit Maserung und Astlöchern
    x, y, z = ctx.world
    grain = noise3(x * 2.4, y * 0.2, z * 2.4, 61)
    v = 0.4 + grain * 0.35 + _LIT[ctx.face]
    if hash01(int(y), "knot") > 0.88 and ctx.face in ("front", "west") and ctx.s in (2, 3):
        v = 0.12
    return pick(ctx.pal["wood"], v)


def _rag(ctx: Ctx):
    # der umwickelte Griff
    band = (ctx.t + (ctx.s // 3)) % 4
    return pick(ctx.pal["rag"], 0.35 + band * 0.12 + _LIT[ctx.face])


def _stone(ctx: Ctx):
    x, y, z = ctx.world
    _, edge = _WARTS(x * 1.3, y * 1.3, z * 1.3)
    v = 0.42 + _n(ctx, 0.35, 77) * 0.3 + _LIT[ctx.face] * 1.5
    if edge < 0.08:
        v -= 0.22                                             # Risse
    return pick(ctx.pal["stone"], v)


def _iron(ctx: Ctx):
    rivet = ctx.face in ("west", "east", "top") and ctx.s % 5 == 2 and ctx.t % 6 == 2
    return pick(ctx.pal["iron"], (0.85 if rivet else 0.45) + _LIT[ctx.face] * 1.5 + (hash01(ctx.u, ctx.v) - 0.5) * 0.08)


def _pixel(name: str):
    def paint(ctx: Ctx):
        ramp_ = {"px_red": "red", "px_green": "green", "px_blue": "blue", "px_magenta": "magenta", "px_cyan": "cyan",
                 "px_yellow": "warn"}[name]
        r = ctx.pal[ramp_]
        # ein Pixel mit Fase: oben links hell, unten rechts dunkel
        v = 0.7
        if ctx.s == 0 or ctx.t == 0:
            v = 0.95
        elif ctx.s == ctx.w - 1 or ctx.t == ctx.h - 1:
            v = 0.45
        col = pick(r, v)
        ctx.glow = col
        return col
    return paint


def materials(variant: str) -> dict:
    mats = {
        "skin": _skin, "arm": _arm, "leg": _leg, "face": _face, "brow": _brow, "eye": _eye, "screen": _screen,
        "nose": _nose, "jaw": _jaw, "tusk": _tusk, "ear": _ear, "earring": _earring, "hair": _hair, "tuft": _tuft,
        "tie": _tie, "tunic": _tunic, "hem": _hem, "belt": _belt, "buckle": _buckle, "bracelet": _bracelet,
        "hand": _hand, "foot": _foot, "shard": _shard, "glitch": _glitch, "wood": _wood, "rag": _rag, "stone": _stone,
        "iron": _iron,
    }
    for name in ("px_red", "px_green", "px_blue", "px_magenta", "px_cyan", "px_yellow"):
        mats[name] = _pixel(name)
    return mats
