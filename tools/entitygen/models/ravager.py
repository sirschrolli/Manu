"""
Verwüster (statt Verwüster/Ravager) auf dem Skelett des Vanilla-Verwüsters: dieselben Teile und Drehpunkte wie
RavagerModel (Hals → Kopf → Maul, Leib um 90° gedreht, vier Beine), damit Laufen, Brüllen, Rammen, Betäubtsein und die
Reiter bei Überfällen passen. Zubehör hängt als zusätzliche Kinder-Knochen an diesen Teilen.

Ein gewaltiges Kriegsrind: wuchtiger Schädel mit eiserner Stirnplatte und Dorn, kleine glühende Augen unter dem
Stirnwulst, ein Nasenring, lange Hörner, ein zotteliger Buckel über Hals und Schultern, eine Satteldecke mit
Beschlägen auf dem Rücken (dort sitzen die Reiter), lederne Brust- und Bauchgurte mit Nieten, schwere Hufe, ein
Schwanz mit Quaste.

  * verwuester_auerochse: dunkelbrauner Auerochse, schwarzer Zottelbuckel, lange nach vorn geschwungene helle Hörner
    mit dunklen Spitzen, rote Satteldecke mit Goldborte und Eisenbeschlägen, rot glühende Augen
  * verwuester_steppe: grau-ockerfarbenes Steppenrind mit langem, bis über die Knie hängendem Fell, schwere
    eingerollte Hörner, blaue Satteldecke mit Bronzebeschlägen, bernsteinfarben glühende Augen

Teile, die nur eine Art trägt (die Hörner der anderen Art, das lange Hängefell), bleiben in der Textur der anderen
Art durchsichtig – eine Geometrie für beide.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y


def _r(dark: str, light: str, steps: int = 6):
    return ramp(dark, light, steps)


_COMMON = dict(muzzle=_r("#0e0b0a", "#5a4c44"), hoof=_r("#0c0a08", "#45403a"), teeth=_r("#7a705e", "#f2ecdc"),
               tongue=_r("#3a0e12", "#b0484e"), socket=_r("#060404", "#2a1e18"))

PALETTES = {
    "verwuester_auerochse": dict(_COMMON,
                                 hide=_r("#140c07", "#7a5236", 8), fur=_r("#0a0705", "#46301f", 7),
                                 belly=_r("#2a1a10", "#8a6646", 7),
                                 horn=_r("#8c7c62", "#f8f0dc"), horn_tip=_r("#121010", "#4e463c"),
                                 eye=_r("#5a0606", "#ff6a3a"), cloth=_r("#2e0806", "#bc3a2e", 7), trim=_r("#5a3e0c", "#f0c860"),
                                 leather=_r("#1a1009", "#74502e"), metal=_r("#202126", "#d6dae2", 7),
                                 ring=_r("#6e6458", "#e8e0d0")),
    "verwuester_steppe": dict(_COMMON,
                              hide=_r("#2a2418", "#b4a27e", 8), fur=_r("#1c1810", "#a8966e", 8),
                              belly=_r("#3a3222", "#c0b08c", 7),
                              horn=_r("#2a241c", "#b8a888"), horn_tip=_r("#121010", "#3a3228"),
                              eye=_r("#5a3204", "#ffcc52"), cloth=_r("#081430", "#4274bc", 7), trim=_r("#4a2a0e", "#e09c52"),
                              leather=_r("#1c140c", "#745638"), metal=_r("#3a2410", "#eab274", 7),
                              ring=_r("#5e5648", "#d8d0c0")),
}

STEPPE_ONLY = ("horn_s_r1", "horn_s_r2", "horn_s_r3", "horn_s_l1", "horn_s_l2", "horn_s_l3", "skirt")
AUEROCHSE_ONLY = ("horn_a_r1", "horn_a_r2", "horn_a_r3", "horn_a_l1", "horn_a_l2", "horn_a_l3")


def _segment_rotation(d) -> tuple[float, float, float]:
    """Drehung (Grad), die einen entlang -Z gebauten Quader in Richtung d zeigen lässt."""
    n = math.sqrt(sum(v * v for v in d))
    dx, dy, dz = (v / n for v in d)
    rx = math.degrees(math.asin(max(-1.0, min(1.0, dy))))
    ry = math.degrees(math.atan2(-dx, -dz))
    return rx, ry, 0.0


def _build_horn(m: Model, parent: str, prefix: str, side: int, segments, start=(0.0, 0.0, 0.0)) -> None:
    """Horn aus geraden Stücken: jedes Stück ein eigener Knochen am Hornansatz, Richtung und Länge frei."""
    px, py, pz = start
    for i, (d, length, width) in enumerate(segments, 1):
        d = (d[0] * side, d[1], d[2])
        name = f"{prefix}{i}"
        m.bone(name, parent, pivot=(px, py, pz), rotation=_segment_rotation(d))
        w = width
        m.cube(name, (-w / 2, -w / 2, -length), (w, w, length + w * 0.4), "horn",            # reicht ins Stück davor
               tag="tip" if i == len(segments) else "")
        n = math.sqrt(sum(v * v for v in d))
        px, py, pz = px + d[0] / n * length, py + d[1] / n * length, pz + d[2] / n * length


def build() -> Model:
    m = Model("ravager", 256, 256)
    m.world_faces = True

    # ---------------------------------------------------------------- Hals, Kopf, Maul (Drehpunkte wie Vanilla)
    m.bone("neck", pivot=(0, -7, 5.5))
    m.cube("neck", (-5, -1, -18), (10, 10, 18), "neck")
    m.bone("mane", "neck")                                                      # Zottelkamm auf dem Hals
    m.cube("mane", (-6, -4.5, -17), (12, 5, 15), "mane")
    m.bone("dewlap", "neck")                                                    # Wamme unter dem Hals
    m.cube("dewlap", (-3.5, 8.5, -16), (7, 3.5, 12), "dewlap")

    m.bone("head", "neck", pivot=(0, 16, -17))
    m.cube("head", (-7, -19, -13), (14, 12, 13), "skull")
    m.cube("head", (-5.5, -10, -19), (11, 8, 6), "muzzle")
    m.cube("head", (-6, -7, -13), (12, 5, 11), "throat")
    m.bone("forelock", "head")                                                  # krause Stirnlocke zwischen den Hörnern
    m.cube("forelock", (-4.5, -20.4, -12.5), (9, 2.4, 7), "forelock")
    m.bone("chanfron", "head")                                                  # eiserne Stirnplatte mit Dorn
    m.cube("chanfron", (-4, -17.6, -13.8), (8, 9, 0.8), "chanfron")
    m.bone("spike", "chanfron", pivot=(0, -13.1, -13.8), rotation=(-10, 0, 0))
    m.cube("spike", (-1.1, -1.1, -2.2), (2.2, 2.2, 2.2), "spike")
    m.cube("spike", (-0.55, -0.55, -3.6), (1.1, 1.1, 1.4), "spike")
    m.cube("chanfron", (-1.6, -10.5, -18.2), (3.2, 0.6, 5.4), "noseguard")       # Nasenschiene bis zum Maul
    m.bone("nose_ring", "head")                                                 # Nasenring
    m.cube("nose_ring", (-1.9, -4.6, -19.6), (0.5, 2.8, 0.5), "ring")
    m.cube("nose_ring", (1.4, -4.6, -19.6), (0.5, 2.8, 0.5), "ring")
    m.cube("nose_ring", (-1.9, -2.3, -19.6), (3.8, 0.5, 0.5), "ring")
    for side, name in ((-1, "right_ear"), (1, "left_ear")):
        m.bone(name, "head", pivot=(7.0 * side, -13.5, -4.5), rotation=(0, 20 * side, -28 * side))
        m.cube(name, (0 if side > 0 else -3.5, -1, -1), (3.5, 2, 1), "ear")

    # Hörner: die Vanilla-Knochen right_horn/left_horn tragen je zwei Formen (eine je Art)
    for side, name in ((-1, "right_horn"), (1, "left_horn")):
        sfx = "r" if side < 0 else "l"
        m.bone(name, "head", pivot=(6.2 * side, -17.2, -7.0))
        # Auerochse: seitlich hinaus und leicht hoch, dann weit nach vorn, die Spitzen nach oben und innen
        _build_horn(m, name, f"horn_a_{sfx}", side, [((1.0, -0.25, -0.12), 6.5, 3.0), ((0.55, -0.45, -1.0), 7.5, 2.3),
                                                ((-0.35, -1.0, -0.45), 5.5, 1.5)])
        # Steppe: dick seitlich und abwärts, dann aufwärts gerollt, die Spitze nach vorn und innen
        _build_horn(m, name, f"horn_s_{sfx}", side, [((1.0, 0.45, 0.15), 5.5, 3.8), ((0.45, -1.0, 0.1), 5.5, 3.0),
                                                ((-0.25, -0.45, -1.0), 4.5, 2.1)], start=(0.0, 0.6, 0.0))

    m.bone("mouth", "head", pivot=(0, -2, 2))
    m.cube("mouth", (-5, 0, -21), (10, 3, 21), "jaw")
    m.bone("beard", "mouth")                                                    # Kinnbart (bei der Steppenart lang)
    m.cube("beard", (-3.5, 3, -18), (7, 4.5, 10), "beard")

    # ---------------------------------------------------------------- Leib (wie Vanilla um 90° gekippt)
    m.bone("body", pivot=(0, 1, 2), rotation=(90, 0, 0))
    m.cube("body", (-7, -10, -7), (14, 16, 20), "hide")
    m.cube("body", (-6, 6, -7), (12, 13, 18), "hide")
    # Zubehör am Leib in Weltlage (die Drehung des Leibs wird zurückgenommen): Koordinaten = Welt - (0, 1, 2)
    # Zottelpelz über den Schultern (ragt kaum über den Rücken: dort liegen die Beine der Reiter)
    m.bone("hump", "body", rotation=(-90, 0, 0))
    m.cube("hump", (-7.9, -13.4, -11.2), (15.8, 10.4, 5.6), "hump")
    m.bone("caparison", "body", rotation=(-90, 0, 0))                           # Satteldecke mit Beschlägen
    m.cube("caparison", (-7.6, -13.6, -6), (15.2, 11, 13), "cloth")
    m.bone("skirt", "body", rotation=(-90, 0, 0))                               # langes Hängefell der Steppenart
    m.cube("skirt", (-7.4, 2.5, -10.5), (14.8, 10, 29.5), "skirt")
    m.bone("tail", "body", pivot=(0, 19, 9), rotation=(-70, 0, 0))              # Schwanz mit Quaste
    m.cube("tail", (-1, 0, -1), (2, 13, 2), "tail")
    m.bone("tuft", "tail", pivot=(0, 12.5, 0))
    m.cube("tuft", (-1.6, 0, -1.6), (3.2, 5, 3.2), "tuft")

    # ---------------------------------------------------------------- Beine (Drehpunkte wie Vanilla) mit Fesselbehang
    # Der Quader beginnt 4 Einheiten unter dem Drehpunkt: die Schultern ragen nicht mehr über den Rücken.
    for name, x, z in (("right_hind_leg", -8, 18), ("left_hind_leg", 8, 18), ("right_front_leg", -8, -5),
                       ("left_front_leg", 8, -5)):
        m.bone(name, pivot=(x, -13, z))
        m.cube(name, (-4, 4, -4), (8, 33, 8), "leg")
        m.bone(name + "_fur", name)
        m.cube(name + "_fur", (-4.6, 24, -4.6), (9.2, 8, 9.2), "legfur")
    return m


def _steppe(ctx: Ctx) -> bool:
    return ctx.params.get("variant") == "verwuester_steppe"


# ------------------------------------------------------------------------------------------------- Fell

def _lane(ctx: Ctx, scale: float = 1.0) -> int:
    x, y, z = ctx.world
    return math.floor((z if ctx.face in ("west", "east") else x) * scale + 0.001)


def _fur_v(ctx: Ctx, seed: int, broad: float = 0.12, strand: float = 0.07, top_dark: float = 0.1) -> float:
    """Helligkeit fürs Fell: große, weiche Flächen und kurze senkrechte Haarstriche (je eine Stufe)."""
    x, y, z = ctx.world
    n = noise3(x * 0.11, y * 0.11, z * 0.11, seed)
    lane = _lane(ctx)
    stroke = hash01(lane, math.floor(y * 0.5 + hash01(lane, seed) * 2.0), seed)
    v = 0.52 + (n - 0.5) * broad * 2 + (stroke - 0.5) * strand
    if ctx.face == "top":
        v -= top_dark * 0.5
    elif ctx.face == "bottom":
        v -= 0.12
    else:
        v -= (1.0 - rel_y(ctx)) * top_dark
        # gerundet: Flächenmitte etwas heller, Kanten und Unterkante dunkler
        across = (ctx.s + 0.5) / max(1, ctx.w)
        v += (0.5 - abs(across - 0.5)) * 0.1 - 0.025
        if ctx.s in (0, ctx.w - 1) or ctx.t == ctx.h - 1:
            v -= 0.05
    return v


def _hide_col(ctx: Ctx, seed: int = 811):
    """Kurzes Fell am Leib; die Steppenart strähniger und heller am Bauch."""
    x, y, z = ctx.world
    v = _fur_v(ctx, seed, strand=0.09 if _steppe(ctx) else 0.06)
    col = pick(ctx.pal["hide"], v)
    if ctx.face == "bottom" or (ctx.face != "top" and y > 4):
        t = 1.0 if ctx.face == "bottom" else min(1.0, (y - 4) / 5)
        col = mix(col, pick(ctx.pal["belly"], 0.5 + (v - 0.52) * 0.6), t * 0.7)
    return col


def _shag_col(ctx: Ctx, key: str = "fur", seed: int = 813, base: float = 0.5):
    """
    Zottelfell: jede Spalte eine Strähne mit eigener, nur langsam wechselnder Helligkeit (drei Stufen), nach unten
    dunkler; oben weiche Büschel. Bewusst ruhig, damit keine Tarnfleck-Unruhe entsteht.
    """
    x, y, z = ctx.world
    lane = _lane(ctx)
    tone = (-0.07, 0.0, 0.07)[int(hash01(lane, seed) * 2.999)]
    v = base + tone + (noise3(x * 0.08, y * 0.06, z * 0.08, seed) - 0.5) * 0.12
    if hash01(lane, math.floor(y / 3.0), seed + 1) > 0.86:
        v += 0.08                                                                 # einzelne helle Strähnenstücke
    if ctx.face == "top":
        v = base + 0.05 + (noise3(x * 0.25, 0, z * 0.25, seed) - 0.5) * 0.16
    elif ctx.face == "bottom":
        v -= 0.16
    else:
        v -= rel_y(ctx) * 0.1
    return pick(ctx.pal[key], v)


def _hide(ctx: Ctx):
    """Leib: Fell, darauf die ledernen Gurte (Brustgurt vorn, Bauchgurt hinter den Vorderbeinen) mit Nieten."""
    x, y, z = ctx.world
    col = _hide_col(ctx)
    if not _steppe(ctx) and ctx.face != "bottom" and y < -5 and z < 3:
        col = _shag_col(ctx, "fur", 813, 0.55)                                    # schwarzer Schulterpelz
    if not _steppe(ctx) and ctx.face == "top" and abs(x) < 1.6 and z > 6:
        col = pick(ctx.pal["belly"], 0.62 if abs(x) < 0.8 else 0.48)              # heller Aalstrich auf dem Rücken
    # Bauchgurt: breiter Ledergurt rund um den Leib, mit Nietenreihe
    if -2.4 <= z <= 0.6 and ctx.face in ("west", "east", "bottom"):
        mid = abs(z + 0.9) < 0.55
        col = pick(ctx.pal["leather"], 0.48 if mid else 0.32)
        if ctx.face != "bottom" and mid and math.floor(y) % 3 == 0:
            col = pick(ctx.pal["metal"], 0.85)
        return col
    # Brustgurt quer über die Brust mit Buckel in der Mitte
    if ctx.face == "front" and -7.6 <= y <= -4.4:
        mid = -7.0 < y < -5.0
        col = pick(ctx.pal["leather"], 0.48 if mid else 0.3)
        if abs(x) < 1.8 and -7.4 < y < -4.6:
            col = pick(ctx.pal["metal"], 0.8 if abs(x) < 0.9 and -6.6 < y < -5.4 else 0.5)
        elif mid and math.floor(x + 8) % 3 == 0:
            col = pick(ctx.pal["metal"], 0.85)
        return col
    return col


def _ragged(ctx: Ctx, seed: int, limit: float, spread: float = 0.3) -> bool:
    """True, wenn das Texel unterhalb der (je Strähne verschieden langen) Unterkante liegt."""
    return rel_y(ctx) > limit - hash01(_lane(ctx), seed) * spread


def _neck(ctx: Ctx):
    if _steppe(ctx) or ctx.world[1] < -4:
        return _shag_col(ctx, "fur", 815, 0.5)
    return _hide_col(ctx, 816)


def _mane(ctx: Ctx):
    if ctx.face == "bottom":
        return None
    if ctx.face != "top" and _ragged(ctx, 821, 1.0, 0.4):
        return None
    return _shag_col(ctx, "fur", 822, 0.5)


def _hump(ctx: Ctx):
    if ctx.face == "bottom":
        return None
    if ctx.face != "top" and _ragged(ctx, 823, 1.0, 0.35):
        return None
    return _shag_col(ctx, "fur", 824, 0.52)


def _dewlap(ctx: Ctx):
    if ctx.face == "top":
        return None
    if ctx.face != "bottom" and _ragged(ctx, 825, 1.0, 0.45):
        return None
    return _shag_col(ctx, "fur" if _steppe(ctx) else "hide", 826, 0.45)


def _skirt(ctx: Ctx):
    """Langes Hängefell der Steppenart: Strähnen, unten ungleich lang ausfransend; oben und unten offen."""
    if not _steppe(ctx) or ctx.face in ("top", "bottom"):
        return None
    if _ragged(ctx, 831, 1.0, 0.42):
        return None
    return _shag_col(ctx, "fur", 832, 0.5)


# ------------------------------------------------------------------------------------------------- Kopf

def _skull(ctx: Ctx):
    face = ctx.face
    x, y, z = ctx.world
    if face == "front":
        # kleine, glühende Augen seitlich unter dem Stirnwulst (neben der Stirnplatte)
        if -5.0 <= y <= -4.0 and 4.6 <= abs(x) <= 6.4:
            if 5.0 <= abs(x) <= 6.0:
                col = pick(ctx.pal["eye"], 0.7)
                ctx.glow = col
                return col
            return pick(ctx.pal["socket"], 0.25)
        if -6.0 <= y < -5.0 and 4.0 <= abs(x) <= 7.0:
            return pick(ctx.pal["fur"], 0.15)                                     # finsterer Stirnwulst
        if -4.0 < y <= -3.0 and 4.6 <= abs(x) <= 6.4:
            return pick(ctx.pal["hide"], 0.2)                                     # Schatten unter dem Auge
    if face == "top" or y < -7.5:
        return _shag_col(ctx, "fur", 841, 0.5)                                    # Schopf auf dem Schädel
    return _hide_col(ctx, 842)


def _forelock(ctx: Ctx):
    """Krause Stirnlocke zwischen den Hörnern."""
    if ctx.face == "bottom":
        return None
    x, y, z = ctx.world
    if ctx.face != "top" and rel_y(ctx) > 0.45 and hash01(ctx.s, ctx.face, 852) > 0.5:
        return None
    # krause Büschel: weiche Flecken, jeder mit hellem Scheitel
    n = noise3(x * 0.6, y * 0.6, z * 0.6, 851)
    v = 0.36 + n * 0.28 + (0.08 if ctx.face == "top" else 0.0)
    return pick(ctx.pal["fur"], v)


def _muzzle(ctx: Ctx):
    """
    Flotzmaul: oben der behaarte Nasenrücken, darunter die dunkle, feuchte Nase mit zwei großen, schräg stehenden
    Nüstern und Glanzlichtern, unten und an den Seiten der helle Maulring (wie beim Auerochsen).
    """
    face, s, t = ctx.face, ctx.s, ctx.t
    x, y, z = ctx.world
    if face == "front":
        w = ctx.w
        edge = min(s, w - 1 - s)
        if t <= 1:
            return _hide_col(ctx, 860)                                            # Nasenrücken
        if t >= ctx.h - 2 or (edge == 0 and t >= 3):
            v = 0.55 + (hash01(ctx.u, ctx.v, 861) - 0.5) * 0.08
            if t == ctx.h - 1:
                v -= 0.14 if 2 <= s <= w - 3 else 0.05                            # Maulspalt
            return pick(ctx.pal["ring"], v)
        v = 0.42 - abs(s - (w - 1) / 2) * 0.025
        nostril = {(3, 2): 0.2, (3, 3): 0.05, (4, 2): 0.1, (4, 3): 0.05}
        key = (t, min(s, w - 1 - s))
        if key in nostril:
            v = nostril[key]                                                      # Nüstern
        elif t == 2 and edge == 1:
            v = 0.82                                                              # feuchtes Glanzlicht
        elif t == 2:
            v = 0.3
        return pick(ctx.pal["muzzle"], v)
    if face == "bottom":
        # Oberkiefer: vorn die Zahnplatte, dahinter dunkler Gaumen (sichtbar, wenn das Maul offen steht)
        if t >= ctx.h - 1:
            return pick(ctx.pal["teeth"], 0.62 if s % 2 else 0.82)
        return pick(ctx.pal["tongue"], 0.18)
    col = _hide_col(ctx, 863)
    if face in ("west", "east"):
        if y > 4.2 or (z < -28.0 and y > 0.5):
            return pick(ctx.pal["ring"], 0.42 + (hash01(ctx.u, ctx.v, 864) - 0.5) * 0.08)   # heller Maulring
        if -27.0 <= z <= -25.8:
            return pick(ctx.pal["leather"], 0.42)                                 # Riemen des Halfters
    elif face == "top" and -27.0 <= z <= -25.8:
        return pick(ctx.pal["leather"], 0.5)
    return col


def _throat(ctx: Ctx):
    if ctx.face == "top":
        return pick(ctx.pal["hide"], 0.2)
    if _steppe(ctx):
        return _shag_col(ctx, "fur", 871, 0.48)
    return mix(_hide_col(ctx, 872), pick(ctx.pal["belly"], 0.4), 0.25)


def _jaw(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    x, y, z = ctx.world
    if face == "top":
        if z > -13.6:
            return _shag_col(ctx, "fur", 882, 0.35)                               # hinten unter dem Hals: Fell
        # Unterkiefer von oben: vorn Schneidezähne, dahinter Zunge, an den Seiten Backenzähne
        if t == ctx.h - 1:
            return pick(ctx.pal["teeth"], 0.72 + (0.14 if s % 2 else 0.0))
        if s in (0, ctx.w - 1):
            return pick(ctx.pal["teeth"], 0.42 + (0.18 if t % 2 else 0.0))
        return pick(ctx.pal["tongue"], 0.45 + (0.08 if (s + t) % 3 == 0 else 0.0))
    if face == "front":
        return pick(ctx.pal["muzzle"], 0.28 if t == 0 else 0.36)
    if face == "bottom":
        return _shag_col(ctx, "fur", 883, 0.4)
    if z < -26.5:
        return pick(ctx.pal["ring"], 0.36 if t == 0 else 0.46)                    # heller Maulring am Kinn
    if t == 0 and z < -22.0:
        return pick(ctx.pal["muzzle"], 0.28)                                      # Lippe
    return _shag_col(ctx, "fur", 884, 0.4) if _steppe(ctx) else _hide_col(ctx, 884)


def _beard(ctx: Ctx):
    """Kinnbart: beim Auerochsen kurz, bei der Steppenart lang und strähnig."""
    if ctx.face == "top" or ctx.face == "bottom" and not _steppe(ctx):
        return None
    if ctx.face != "bottom" and _ragged(ctx, 891, 1.0 if _steppe(ctx) else 0.45, 0.35):
        return None
    return _shag_col(ctx, "fur", 892, 0.42)


def _ear(ctx: Ctx):
    if ctx.face == "front":
        return pick(ctx.pal["tongue"], 0.22) if 0 < ctx.s < ctx.w - 1 else pick(ctx.pal["hide"], 0.35)
    return _shag_col(ctx, "fur", 901, 0.45)


def _chanfron(ctx: Ctx):
    """Eiserne Stirnplatte: gewölbt (heller Grat in der Mitte, zu den Seiten dunkler), dunkler Rand, Nieten in den Ecken."""
    face, s, t = ctx.face, ctx.s, ctx.t
    if face != "front":
        return pick(ctx.pal["metal"], 0.32)
    w, h = ctx.w, ctx.h
    corner = s in (0, w - 1) and t in (0, h - 1)
    if corner:
        return pick(ctx.pal["metal"], 0.9)
    if s in (0, w - 1) or t in (0, h - 1):
        return pick(ctx.pal["metal"], 0.28)
    v = 0.66 - abs(s + 0.5 - w / 2) * 0.08 - t * 0.02
    if s in (w // 2 - 1, w // 2):
        v += 0.1                                                                  # Grat in der Mitte
    return pick(ctx.pal["metal"], v)


def _noseguard(ctx: Ctx):
    if ctx.face == "top":
        return pick(ctx.pal["metal"], 0.75 if ctx.s == 1 else 0.55)
    if ctx.face == "front":
        return pick(ctx.pal["metal"], 0.6)
    return pick(ctx.pal["metal"], 0.3)


def _spike(ctx: Ctx):
    v = 0.66 if ctx.face in ("top", "west", "front") else 0.4
    if ctx.cube.size[2] < 2:
        v += 0.14
    return pick(ctx.pal["metal"], v)


def _ring(ctx: Ctx):
    return pick(ctx.pal["metal"], 0.62 if ctx.face in ("top", "front") else 0.36)


def _horn(ctx: Ctx):
    """Horn: quer verlaufende Wülste, zur Spitze heller bzw. dunkel auslaufend; jede Art trägt nur ihre Form."""
    name = ctx.cube.bone.name
    if name.startswith("horn_s") != _steppe(ctx):
        return None
    lx, ly, lz = ctx.local
    length = ctx.cube.size[2]
    pos = (-lz) / max(1e-3, length)                                               # 0 = Ansatz, 1 = Ende des Stücks
    seg = int(name[-1])
    along = (seg - 1 + pos) / 3.0                                                  # 0 = Schädel, 1 = Spitze
    ring = math.sin(lz * (2.6 if _steppe(ctx) else 1.8)) * (0.1 if _steppe(ctx) else 0.06)
    v = 0.45 + ring + (0.12 if ctx.face in ("top", "west") else -0.04 if ctx.face == "bottom" else 0.0)
    if _steppe(ctx):
        v += 0.08 + along * 0.22
        return pick(ctx.pal["horn"], v)
    if along > 0.78:
        return pick(ctx.pal["horn_tip"], 0.3 + (1.0 - along) * 1.2 + (v - 0.45))
    return pick(ctx.pal["horn"], v + along * 0.12)


# ------------------------------------------------------------------------------------------------- Satteldecke

def _cloth(ctx: Ctx):
    """
    Satteldecke: oben Tuch mit Borte und Rautenmuster (Sitz der Reiter), an den Seiten Tuch mit einer Metallplatte samt
    Buckel und einer Nietenreihe, unten eine gezackte Borte. Vorn und hinten nur die Kanten des Tuchs.
    """
    face, s, t = ctx.face, ctx.s, ctx.t
    x, y, z = ctx.world
    if face == "bottom":
        return None
    if face in ("front", "back"):
        return pick(ctx.pal["cloth"], 0.3) if t <= 1 else None
    if face == "top":
        border = s in (0, ctx.w - 1) or t in (0, ctx.h - 1)
        inner = s in (1, ctx.w - 2) or t in (1, ctx.h - 2)
        if border:
            return pick(ctx.pal["trim"], 0.42)
        if inner:
            return pick(ctx.pal["trim"], 0.72 if (s + t) % 2 else 0.58)
        diamond = (abs(x) + abs(z - 2.5)) % 4.0
        v = 0.52 + (0.14 if diamond < 1.0 else 0.0) + (noise3(x * 0.3, 0, z * 0.3, 931) - 0.5) * 0.08
        return pick(ctx.pal["cloth"], v)
    # Seiten
    ry = rel_y(ctx)
    lane = math.floor(z + 0.001)
    scallop = 0.88 + (0.1 if lane % 3 == 1 else 0.0)
    if ry > scallop:
        return None
    if ry > scallop - 0.11:
        return pick(ctx.pal["trim"], 0.5 + (0.14 if lane % 2 else 0.0))           # gezackte Borte
    if ry < 0.09:
        return pick(ctx.pal["cloth"], 0.62)                                       # Umschlag an der Rückenkante
    # Metallplatte mit Buckel auf der Flanke
    pz, py = z - 2.5, y + 7.6
    if abs(pz) <= 3.0 and abs(py) <= 2.6:
        if abs(pz) <= 0.9 and abs(py) <= 0.9:
            return pick(ctx.pal["metal"], 0.92)
        if abs(pz) > 2.3 and abs(py) > 1.9:
            return pick(ctx.pal["metal"], 0.88)                                   # Eckniete
        if abs(pz) > 2.3 or abs(py) > 1.9:
            return pick(ctx.pal["metal"], 0.32)
        return pick(ctx.pal["metal"], 0.62 - py * 0.04 - abs(pz) * 0.02)
    v = 0.45 + (noise3(x * 0.3, y * 0.4, z * 0.3, 933) - 0.5) * 0.1 + math.sin(z * 1.2) * 0.05
    if abs(y + 3.6) < 0.5 and math.floor(z + 20) % 2 == 0:
        return pick(ctx.pal["metal"], 0.82)                                       # Nietenreihe über der Borte
    return pick(ctx.pal["cloth"], v)


# ------------------------------------------------------------------------------------------------- Beine, Schwanz

def _leg(ctx: Ctx):
    x, y, z = ctx.world
    if ctx.face == "bottom":
        return pick(ctx.pal["hoof"], 0.2)
    if y > 20.0:
        v = 0.34 + (0.14 if ctx.face == "front" else 0.0) - (y - 20) * 0.02
        if ctx.face == "front" and ctx.s in (3, 4):
            v = 0.08                                                              # Spalt der Klauen
        if ctx.face == "back" and ctx.s in (1, 6) and y < 21.5:
            v = 0.5                                                               # Afterklauen
        return pick(ctx.pal["hoof"], v)
    if ctx.face == "top":
        return _shag_col(ctx, "fur", 940, 0.45)
    if y < 6:
        # oben: Schulter und Keule gehen in den Leib über
        if not _steppe(ctx) and y < -3 and z < 3:
            return _shag_col(ctx, "fur", 813, 0.55)
        return _hide_col(ctx, 941)
    col = _hide_col(ctx, 942)
    return mix(col, pick(ctx.pal["hide"], 0.18), min(1.0, (y - 6) / 14) * 0.55)    # Unterbein dunkler


def _legfur(ctx: Ctx):
    """Fesselbehang über den Hufen: bei der Steppenart lang und dicht, beim Auerochsen nur ein Kranz."""
    if ctx.face in ("top", "bottom"):
        return None
    if _ragged(ctx, 951, 0.98 if _steppe(ctx) else 0.45, 0.32):
        return None
    return _shag_col(ctx, "fur", 952, 0.42)


def _tail(ctx: Ctx):
    return _shag_col(ctx, "hide", 961, 0.4)


def _tuft(ctx: Ctx):
    if ctx.face == "top":
        return pick(ctx.pal["fur"], 0.3)
    if ctx.face != "bottom" and _ragged(ctx, 962, 1.0, 0.35):
        return None
    return _shag_col(ctx, "fur", 963, 0.35)


def materials(variant: str) -> dict:
    return {
        "neck": _neck, "mane": _mane, "dewlap": _dewlap, "skull": _skull, "muzzle": _muzzle, "throat": _throat,
        "forelock": _forelock, "chanfron": _chanfron, "noseguard": _noseguard, "spike": _spike, "ring": _ring, "ear": _ear, "horn": _horn,
        "jaw": _jaw, "beard": _beard, "hide": _hide, "hump": _hump, "cloth": _cloth, "skirt": _skirt,
        "tail": _tail, "tuft": _tuft, "leg": _leg, "legfur": _legfur,
    }


def hidden(variant: str) -> list[str]:
    """Nur für die Vorschau (im Spiel sind diese Teile in der Textur durchsichtig)."""
    return list(STEPPE_ONLY if variant == "verwuester_auerochse" else AUEROCHSE_ONLY)
