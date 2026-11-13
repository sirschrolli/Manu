"""
Greif der Felsschluchten (0.85): vorn ein Adler – weißer (beim Rostgreif ockerfarbener) Kopf mit gelbem, scharf
gebogenem Schnabel, dunkler Braue über bernsteinfarbenen Augen, nach hinten gestrichenen Federohren, ein Federkragen
am Hals, gefiederte Brust und Adlerläufe mit gelben, geschuppten Fängen und schwarzen Krallen –, hinten ein Löwe mit
lohfarbenem Fell, hellem Bauch, Pranken und einem Schwanz mit dunkler Quaste. Dazu große, dreigliedrige Schwingen:
goldbraune Deckfedern, dunkle, gebänderte Schwungfedern mit gezackter Hinterkante und gespreizten Handschwingen
("Fingern") an den Spitzen.

Knochen (für GriffinModel in Java): body (mit neck → head → jaw, tuft_l, tuft_r; wing_l → wing_l_mid → wing_l_tip,
wing_r → wing_r_mid → wing_r_tip; tail → tail2 → tail_tuft), die Läufe left_front_leg → left_front_shin →
left_front_foot, right_front_… (Adler) und left_hind_leg → left_hind_shin → left_hind_paw, right_hind_… (Löwe).
Ruhehaltung: Schwingen ausgebreitet (Gleitflug); zum Stehen faltet das Modell sie an den Flanken zusammen.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y

_COMMON = {
    "beak": [hexc(c) for c in ("#6e4a0e", "#9a6c16", "#c48e22", "#e2b034", "#f4cc5a", "#fde49a")],
    "horn": [hexc(c) for c in ("#0e0c0a", "#1c1814", "#2c2620", "#3e362e", "#52483e")],
    "scale": [hexc(c) for c in ("#7a5612", "#a07418", "#c69424", "#e0b236", "#f2cc56", "#fbe28a")],
    "eye": [hexc(c) for c in ("#120a04", "#5a2806", "#a0520e", "#d88418", "#f6b42e", "#ffd870")],
    "tuft": [hexc(c) for c in ("#160e08", "#22160c", "#301f12", "#402a18", "#523720")],
}

PALETTES = {
    "griffin": {**_COMMON,
                "head": [hexc(c) for c in ("#9a9282", "#b6ae9c", "#cec6b2", "#e2dcca", "#f0ebde", "#faf7ee", "#ffffff")],
                "brow": [hexc(c) for c in ("#5c5248", "#766a5e", "#908476", "#aa9e8e")],
                "ruff": [hexc(c) for c in ("#3e2610", "#5c3a18", "#7c5222", "#9c6c30", "#ba8840", "#d4a456", "#e8c278")],
                "fur": [hexc(c) for c in ("#5a3a1a", "#744c22", "#8e602c", "#a87438", "#be8a48", "#d2a05c", "#e2b878")],
                "belly": [hexc(c) for c in ("#a88a62", "#c0a27a", "#d4b892", "#e4ccaa", "#f0dec2")],
                "wing": [hexc(c) for c in ("#1a100a", "#28190f", "#382416", "#4a311e", "#5e4028", "#745034", "#8c6444")],
                "bar": [hexc(c) for c in ("#6c5640", "#86704f", "#a08a66", "#baa480")]},
    "griffin_rust": {**_COMMON,
                     "head": [hexc(c) for c in ("#7a4e30", "#946240", "#ac7852", "#c08e66", "#d2a47c", "#e0ba94", "#ecceac")],
                     "brow": [hexc(c) for c in ("#4a2a1a", "#623826", "#7a4832", "#925a40")],
                     "ruff": [hexc(c) for c in ("#401608", "#60220e", "#843014", "#a4421e", "#be5a2c", "#d4783e", "#e69858")],
                     "fur": [hexc(c) for c in ("#56301c", "#723e24", "#8c4e2e", "#a6623a", "#bc784a", "#d0905e", "#e0aa7a")],
                     "belly": [hexc(c) for c in ("#a8846a", "#be9a80", "#d2b098", "#e2c6b0", "#eed8c6")],
                     "wing": [hexc(c) for c in ("#1a0c06", "#28140a", "#381c10", "#4a2818", "#5e3420", "#74422a", "#8c5436")],
                     "bar": [hexc(c) for c in ("#6e4a36", "#886046", "#a27858", "#ba926e")]},
}

_LIT = {"top": 0.08, "front": 0.02, "west": 0.0, "east": -0.02, "back": -0.04, "bottom": -0.12}


def build() -> Model:
    m = Model("griffin", 128, 128)
    m.tex_res = 2
    m.world_faces = True
    # Leib des Löwen, vorn die gefiederte Brust (breiter, tiefer)
    m.bone("body", pivot=(0, 10.0, 2.0))
    m.cube("body", (-4.5, -4.5, -9.0), (9.0, 9.0, 18.0), "fur")
    m.cube("body", (-5.0, -5.6, -10.2), (10.0, 10.6, 8.4), "chest")
    # Hals, nach vorn geneigt, mit Federkragen; der Kopf sitzt waagerecht obenauf
    m.bone("neck", "body", pivot=(0, -3.4, -8.4), rotation=(22, 0, 0))
    m.cube("neck", (-3.0, -9.6, -3.2), (6.0, 10.4, 6.2), "neck")
    m.cube("neck", (-3.6, -3.4, -3.9), (7.2, 4.6, 7.4), "ruff")                 # der Federkragen am Halsansatz
    m.bone("head", "neck", pivot=(0, -9.0, -0.6), rotation=(-22, 0, 0))
    m.cube("head", (-3.0, -5.5, -5.4), (6.0, 6.0, 7.8), "head")
    m.cube("head", (-3.25, -5.4, -5.7), (6.5, 1.3, 3.3), "brow")                 # die finstere Braue über den Augen
    m.cube("head", (-1.65, -4.9, -6.2), (3.3, 2.6, 0.9), "cere")                 # die Wachshaut am Schnabelgrund
    m.cube("head", (-1.45, -4.75, -8.4), (2.9, 3.05, 3.0), "beak")
    m.cube("head", (-1.2, -4.45, -10.0), (2.4, 2.5, 1.7), "beak")
    m.cube("head", (-0.95, -2.35, -10.5), (1.9, 2.75, 1.4), "beak_tip")          # der Haken
    m.cube("head", (2.95, -3.9, -4.4), (0.16, 1.3, 1.5), "eye")
    m.cube("head", (-3.11, -3.9, -4.4), (0.16, 1.3, 1.5), "eye")
    m.bone("jaw", "head", pivot=(0, -1.7, -5.6))
    m.cube("jaw", (-1.2, 0, -3.5), (2.4, 1.0, 3.5), "beak_lower")
    for side, sx in (("l", 1), ("r", -1)):
        m.bone(f"tuft_{side}", "head", pivot=(1.9 * sx, -5.0, 1.6), rotation=(-26, 16 * sx, 0))
        m.cube(f"tuft_{side}", (-0.8, -0.7, 0), (1.6, 1.4, 3.8), "tuft_feather")
    # Schwingen: Arm, Unterarm, Hand – in der Ruhehaltung waagerecht ausgebreitet
    for side, sx in (("l", 1), ("r", -1)):
        lo = (lambda a, w: a if sx > 0 else -a - w)
        m.bone(f"wing_{side}", "body", pivot=(4.2 * sx, -4.0, -5.5))
        m.cube(f"wing_{side}", (lo(0, 9.5), -0.5, -1.2), (9.5, 1.0, 9.6), "wing_inner")
        m.bone(f"wing_{side}_mid", f"wing_{side}", pivot=(9.5 * sx, 0, 0))
        m.cube(f"wing_{side}_mid", (lo(0, 9.5), -0.4, -1.0), (9.5, 0.8, 9.4), "wing_mid")
        m.bone(f"wing_{side}_tip", f"wing_{side}_mid", pivot=(9.5 * sx, 0, 0))
        m.cube(f"wing_{side}_tip", (lo(0, 14.0), -0.4, -0.8), (14.0, 0.8, 8.8), "wing_tip")
    # Löwenschwanz mit Quaste
    m.bone("tail", "body", pivot=(0, -3.2, 8.6), rotation=(-50, 0, 0))
    m.cube("tail", (-0.8, -0.8, 0), (1.6, 1.6, 7.5), "tail")
    m.bone("tail2", "tail", pivot=(0, 0, 7.4), rotation=(38, 0, 0))
    m.cube("tail2", (-0.7, -0.7, 0), (1.4, 1.4, 6.5), "tail")
    m.bone("tail_tuft", "tail2", pivot=(0, 0, 6.4), rotation=(10, 0, 0))
    m.cube("tail_tuft", (-1.3, -1.3, -0.6), (2.6, 2.6, 4.2), "tail_tuft")
    # vorn Adlerläufe: befiederte "Hosen", geschuppte Läufe, Zehen mit Krallen
    for side, sx in (("left", 1), ("right", -1)):
        m.bone(f"{side}_front_leg", pivot=(3.0 * sx, 13.0, -4.6))
        m.cube(f"{side}_front_leg", (-1.9, -1.0, -2.0), (3.8, 6.6, 4.0), "trousers")
        m.bone(f"{side}_front_shin", f"{side}_front_leg", pivot=(0, 5.5, -0.2))
        m.cube(f"{side}_front_shin", (-1.2, 0, -1.2), (2.4, 4.1, 2.4), "tarsus")
        m.bone(f"{side}_front_foot", f"{side}_front_shin", pivot=(0, 4.0, 0))
        m.cube(f"{side}_front_foot", (-0.6, 0.55, -3.5), (1.2, 0.95, 3.8), "toe")
        m.cube(f"{side}_front_foot", (-1.75, 0.65, -2.7), (1.0, 0.85, 2.9), "toe")
        m.cube(f"{side}_front_foot", (0.75, 0.65, -2.7), (1.0, 0.85, 2.9), "toe")
        m.cube(f"{side}_front_foot", (-0.5, 0.65, 0.2), (1.0, 0.85, 1.8), "toe")
        m.cube(f"{side}_front_foot", (-0.42, 0.4, -4.6), (0.84, 1.2, 1.1), "talon")
        m.cube(f"{side}_front_foot", (-1.6, 0.5, -3.7), (0.7, 1.0, 1.0), "talon")
        m.cube(f"{side}_front_foot", (0.9, 0.5, -3.7), (0.7, 1.0, 1.0), "talon")
        m.cube(f"{side}_front_foot", (-0.38, 0.5, 2.0), (0.76, 1.0, 0.9), "talon")
    # hinten Löwenläufe mit Pranken
    for side, sx in (("left", 1), ("right", -1)):
        m.bone(f"{side}_hind_leg", pivot=(3.2 * sx, 10.5, 8.0))
        m.cube(f"{side}_hind_leg", (-2.1, -1.5, -2.6), (4.2, 8.5, 5.2), "thigh")
        m.bone(f"{side}_hind_shin", f"{side}_hind_leg", pivot=(0, 6.5, 1.6))
        m.cube(f"{side}_hind_shin", (-1.3, 0, -1.3), (2.6, 5.5, 2.6), "hind_shin")
        m.bone(f"{side}_hind_paw", f"{side}_hind_shin", pivot=(0, 5.5, 0))
        m.cube(f"{side}_hind_paw", (-1.7, 0, -2.6), (3.4, 1.5, 3.6), "paw")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _plane(ctx: Ctx) -> tuple[float, float]:
    """Zwei Koordinaten in der Fläche: (quer, abwärts) – abwärts heißt bei Seitenflächen nach unten, oben nach hinten."""
    x, y, z = ctx.world
    if ctx.face in ("west", "east"):
        return z, y
    if ctx.face in ("front", "back"):
        return x, y
    return x, z


def _scallop(ctx: Ctx, key: str, w: float = 1.8, h: float = 1.2, seed: int = 31, base: float = 0.55, var: float = 0.12,
             edge: float = 0.12) -> tuple:
    """Schuppig liegende Federn: Reihen runder Federspitzen, die nach unten (oben: nach hinten) zeigen."""
    a, b = _plane(ctx)
    row = math.floor(b / h)
    ap = a / w + (row % 2) * 0.5
    col = math.floor(ap)
    fa = ap - col
    fb = b / h - row
    tip = 0.55 + 0.45 * math.sqrt(max(0.0, 1.0 - (2.0 * fa - 1.0) ** 2))
    v = base + (hash01(col, row, seed) - 0.5) * var + fb * 0.04
    if fb > tip:
        v -= edge                                                            # der Schatten unter der Federspitze
    elif fb > tip - 0.2:
        v += 0.05                                                            # die helle Federkante
    return pick(ctx.pal[key], v + _LIT[ctx.face])


def _fur(ctx: Ctx) -> tuple:
    """Lohfarbenes Löwenfell: ruhige Flächen, feine Haarstriche, Rücken dunkler, Bauch hell."""
    x, y, z = ctx.world
    n = noise3(x * 0.3, y * 0.3, z * 0.3, 41)
    strand = hash01(int(math.floor(x)), int(math.floor(y * 0.5 + hash01(int(math.floor(x)), int(math.floor(z)), 42) * 2.0)),
                    int(math.floor(z)), 43)
    ry = rel_y(ctx) if ctx.face not in ("top", "bottom") else (0.0 if ctx.face == "top" else 1.0)
    v = 0.56 + (n - 0.5) * 0.24 + (strand - 0.5) * 0.08 - (1.0 - ry) * 0.12 + _LIT[ctx.face]
    if ctx.face != "top" and (ctx.s == 0 or ctx.s == ctx.w - 1 or ctx.t == ctx.h - 1):
        v -= 0.05
    col = pick(ctx.pal["fur"], v)
    if ctx.face == "bottom" or (ry > 0.62 and ctx.face in ("west", "east", "front", "back")):
        t = 1.0 if ctx.face == "bottom" else min(1.0, (ry - 0.62) / 0.38 * 1.5)
        col = mix(col, pick(ctx.pal["belly"], 0.55 + (n - 0.5) * 0.2), t)
    return col


def _chest(ctx: Ctx):
    """Gefiederte Brust: vorn und oben Federn, nach hinten (zum Fell) und unten (zum hellen Bauch) übergehend."""
    x, y, z = ctx.world
    col = _scallop(ctx, "ruff", w=1.6, h=1.1, seed=33, base=0.62, var=0.1, edge=0.1)
    if ctx.face == "bottom":
        return mix(col, pick(ctx.pal["belly"], 0.6), 0.6)
    # hinten (auf den Flanken) in das Fell auslaufen, in Zacken
    if ctx.face in ("west", "east", "top") and z > -2.6 + hash01(int(math.floor(y * 0.7)), 0, 34) * 2.2:
        return _fur(ctx)
    ry = rel_y(ctx)
    if ry > 0.78 and ctx.face in ("front", "west", "east"):
        return mix(col, pick(ctx.pal["belly"], 0.62), 0.55)
    return col


def _neck(ctx: Ctx):
    """Hals: oben weiß wie der Kopf, nach unten in die goldbraunen Federn des Kragens."""
    x, y, z = ctx.world
    ry = rel_y(ctx) if ctx.face not in ("top", "bottom") else (0.0 if ctx.face == "top" else 1.0)
    jag = hash01(int(math.floor(x * 1.2)), int(math.floor(z * 1.2)), 35) * 0.22
    if ry < 0.42 + jag:
        return _scallop(ctx, "head", w=1.6, h=1.0, seed=36, base=0.7, var=0.1, edge=0.1)
    return _scallop(ctx, "ruff", w=1.6, h=1.1, seed=37, base=0.56)


def _ruff(ctx: Ctx):
    """Der Federkragen: längere, gesträubte Federn, unten dunkler gezackt."""
    if ctx.face == "top":
        return _scallop(ctx, "ruff", w=1.6, h=1.1, seed=38, base=0.66)
    return _scallop(ctx, "ruff", w=1.5, h=2.2, seed=39, base=0.6, edge=0.2)


def _head(ctx: Ctx):
    """Weiße (ockerfarbene) Kopffedern mit feinen Strichen, hinter dem Auge ein dunklerer Streif."""
    x, y, z = ctx.world
    lx, ly, lz = ctx.local
    v = 0.72 + (hash01(int(math.floor(x * 2)), int(math.floor(y * 0.8)), int(math.floor(z * 2)), 51) - 0.5) * 0.1
    if ctx.face in ("west", "east") and -4.4 < ly < -3.2 and lz > -3.0:
        v -= 0.14 + (0.06 if lz > -1.0 else 0.0)                            # der Streif hinter dem Auge
    if ctx.lface == "bottom" or (ctx.face in ("west", "east", "front") and ly > -0.9):
        v -= 0.08                                                            # Kehle etwas grauer
    return pick(ctx.pal["head"], v + _LIT[ctx.face])


def _brow(ctx: Ctx):
    lx, ly, lz = ctx.local
    if ctx.lface == "top":
        return _head(ctx)
    v = 0.4 + (hash01(int(lx * 2), int(lz * 2), 52) - 0.5) * 0.12
    return pick(ctx.pal["brow"], v + _LIT[ctx.face])


def _cere(ctx: Ctx):
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    v = 0.78 + _LIT[ctx.face]
    if ctx.lface == "front" and abs(rs - 0.5) > 0.22 and abs(rs - 0.5) < 0.36 and ctx.t < ctx.h // 2:
        return pick(ctx.pal["horn"], 0.3)                                    # die Nasenlöcher
    return pick(ctx.pal["scale"], v)


def _beak(ctx: Ctx):
    """Oberschnabel: goldgelb, an der Firste heller, zur Spitze hin dunkler."""
    lz = ctx.local[2]
    t = min(1.0, max(0.0, (-lz - 6.2) / 3.6))                                # 0 am Grund, 1 an der Spitze
    v = 0.66 - t * 0.2 + _LIT[ctx.face]
    if ctx.lface == "top":
        v += 0.12
    if ctx.lface in ("west", "east") and ctx.t == ctx.h - 1:
        v -= 0.2                                                             # die Schnabelkante
    return pick(ctx.pal["beak"], v)


def _beak_tip(ctx: Ctx):
    ly = ctx.local[1]
    if ly > -0.8:
        return pick(ctx.pal["horn"], 0.5 + _LIT[ctx.face])                  # die dunkle Hakenspitze
    return pick(ctx.pal["beak"], 0.42 + _LIT[ctx.face])


def _beak_lower(ctx: Ctx):
    lz = ctx.local[2]
    v = 0.5 + _LIT[ctx.face] - (0.15 if lz < -2.6 else 0.0)
    return pick(ctx.pal["beak"], v)


def _eye(ctx: Ctx):
    if ctx.lface not in ("west", "east"):
        return pick(ctx.pal["brow"], 0.3)
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    rt = (ctx.t + 0.5) / max(ctx.h, 1)
    if abs(rs - 0.42) < 0.18 and abs(rt - 0.52) < 0.2:
        return pick(ctx.pal["eye"], 0.0)                                     # Pupille
    if rt < 0.2:
        return pick(ctx.pal["brow"], 0.15)                                   # Schatten der Braue
    if abs(rs - 0.5) > 0.4 or rt > 0.86:
        return pick(ctx.pal["brow"], 0.2)                                    # dunkler Lidrand
    if rs > 0.62 and rt < 0.42:
        return pick(ctx.pal["eye"], 1.0)                                     # Glanzpunkt
    return pick(ctx.pal["eye"], 0.62 + (rt - 0.5) * 0.2)


def _tuft_feather(ctx: Ctx):
    """Die Federohren: Kopffarbe, zur Spitze hin dunkel."""
    lz = ctx.local[2]
    if lz > 2.8 and ctx.lface != "back":
        return pick(ctx.pal["brow"], 0.55 + _LIT[ctx.face])
    return pick(ctx.pal["head"], 0.6 - lz * 0.04 + _LIT[ctx.face])


# ------------------------------------------------------------------------------------------- Schwingen

def _covert_rows(ctx: Ctx, span: float, chord: float, key: str, seed: int) -> tuple:
    """Deckfedern: kleine, rundliche Federn in Reihen quer zur Flügelspannweite (die Spitzen zeigen nach hinten)."""
    row = math.floor(chord / 1.5)
    ap = span / 1.9 + (row % 2) * 0.5
    col = math.floor(ap)
    fa = ap - col
    fb = chord / 1.5 - row
    tip = 0.6 + 0.4 * math.sqrt(max(0.0, 1.0 - (2.0 * fa - 1.0) ** 2))
    v = 0.58 + (hash01(col, row, seed) - 0.5) * 0.16 + fb * 0.08
    if fb > tip:
        v -= 0.2
    elif fb > tip - 0.22:
        v += 0.1
    return pick(ctx.pal[key], v + _LIT[ctx.face])


def _flight(ctx: Ctx, span: float, chord: float, width: float, seed: int, under: bool) -> tuple:
    """Schwungfedern: lange Federn in Richtung der Flügeltiefe, mit Schaftlinie und heller Bänderung."""
    k = span / width
    feather = math.floor(k)
    fk = k - feather
    v = 0.5 + (hash01(feather, seed) - 0.5) * 0.1
    if fk < 0.12:
        v -= 0.16                                                            # die Fuge zur Nachbarfeder
    elif abs(fk - 0.45) < 0.07:
        v += 0.06                                                            # der Schaft
    if under:
        # unten heller, mit dunklen Querbändern
        base = pick(ctx.pal["bar"], 0.55 + v * 0.3 + _LIT[ctx.face])
        if (chord + hash01(feather, seed + 1) * 0.6) % 1.6 < 0.5:
            return pick(ctx.pal["wing"], 0.42 + _LIT[ctx.face])
        return base
    col = pick(ctx.pal["wing"], v + 0.06 + _LIT[ctx.face])
    if (chord + hash01(feather, seed + 1) * 0.6) % 1.8 < 0.45:
        col = mix(col, pick(ctx.pal["bar"], 0.3), 0.45)                     # die helle Bänderung
    return col


def _wing_segment(ctx: Ctx, length: float, depth: float, z0: float, covert_to: float, width: float, seed: int, notch: bool):
    lx, ly, lz = ctx.local
    span = abs(lx)
    chord = lz - z0                                                          # 0 an der Vorderkante, depth an der Hinterkante
    face = ctx.lface
    # gezackte Hinterkante: zwischen den Federspitzen Lücken
    if notch and chord > depth - 0.55:
        fk = (span / width) % 1.0
        if fk < 0.22 and face in ("top", "bottom", "back"):
            return None
    if face == "front":
        return pick(ctx.pal["ruff"], 0.5 + _LIT["front"])                   # die Vorderkante: goldbraune Deckfedern
    if face in ("west", "east"):
        return pick(ctx.pal["wing"], 0.36 + (0.1 if chord < covert_to else 0.0))
    under = face == "bottom"
    if chord < covert_to:
        return _covert_rows(ctx, span, chord, "bar" if under else "ruff", seed)
    if chord < covert_to + 1.1 and not under:
        # die großen Deckfedern: eine Reihe dunklerer, längerer Federn
        return _covert_rows(ctx, span * 0.7, chord * 0.5, "wing", seed + 3)
    return _flight(ctx, span, chord, width, seed, under)


def _wing_inner(ctx: Ctx):
    return _wing_segment(ctx, 9.5, 9.6, -1.2, 4.2, 1.5, 61, True)


def _wing_mid(ctx: Ctx):
    return _wing_segment(ctx, 9.5, 9.4, -1.0, 3.2, 1.5, 62, True)


_FINGERS = 5
_TIP_LEN = 14.0
_TIP_COVERTS = 2.0


def _wing_tip(ctx: Ctx):
    """
    Handschwingen: die Federn liegen in Richtung der Spannweite (nach außen, angelegt nach hinten). Vorn die
    Handdecken, dahinter fünf lange Schwungfedern; außen gespreizt zu "Fingern" mit Lücken, verschieden lang, die
    Spitzen fast schwarz, darüber helle Bänder.
    """
    lx, ly, lz = ctx.local
    span = abs(lx)
    chord = lz + 0.8                                                         # 0..8.8 (Vorderkante .. Hinterkante)
    face = ctx.lface
    if chord < _TIP_COVERTS:
        if face in ("top", "bottom"):
            return _covert_rows(ctx, chord * 1.4, span * 0.75, "ruff" if face == "top" else "bar", 64)
        return pick(ctx.pal["ruff"], 0.48 + _LIT[ctx.face])
    fan = (chord - _TIP_COVERTS) / (8.8 - _TIP_COVERTS) * _FINGERS
    f = min(_FINGERS - 1, int(math.floor(fan)))
    ff = fan - f
    reach = _TIP_LEN - abs(f - 1.4) * 1.1                                    # die vorderen Finger sind die längsten
    spread = max(0.0, (span - 7.0) / (_TIP_LEN - 7.0))                       # 0 innen .. 1 an der Spitze
    gap = 0.1 + spread * 0.22
    rounded = 0.9 * (abs(ff - 0.55) / 0.45) ** 2                             # gerundete Federspitzen
    if span > 7.0 and (ff < gap or span > reach - rounded):
        return None
    if span > reach - rounded - 0.01:
        return None
    if face in ("front", "back", "west", "east"):
        return pick(ctx.pal["wing"], 0.22)
    v = 0.42 - spread * 0.22 + (hash01(f, 65) - 0.5) * 0.08
    if abs(ff - 0.55) < 0.07:
        v += 0.06                                                            # der Schaft
    if ff < gap + 0.08 and span <= 7.0:
        v -= 0.12                                                            # die Fuge zur Nachbarfeder
    band = (span + f * 0.35) % 1.9 < 0.5 and span < reach - 2.2
    if face == "bottom":
        return pick(ctx.pal["bar"], 0.5) if band else pick(ctx.pal["wing"], v + 0.06)
    col = pick(ctx.pal["wing"], v + _LIT[ctx.face])
    if band:
        col = mix(col, pick(ctx.pal["bar"], 0.3), 0.42)
    return col


# ------------------------------------------------------------------------------------------- Läufe und Schwanz

def _trousers(ctx: Ctx):
    """Befiederte Oberschenkel des Adlers."""
    return _scallop(ctx, "ruff", w=1.4, h=1.0, seed=71, base=0.54)


def _tarsus(ctx: Ctx):
    """Gelbe Läufe mit Schuppenringen."""
    ly = ctx.local[1]
    ring = (ly * 1.6) % 1.0
    v = 0.6 + (0.1 if ring < 0.5 else -0.04) + _LIT[ctx.face]
    if ring > 0.88:
        v -= 0.18
    return pick(ctx.pal["scale"], v)


def _toe(ctx: Ctx):
    lz = ctx.local[2]
    v = 0.6 + _LIT[ctx.face] - (0.14 if (lz * 2.2) % 1.0 > 0.82 else 0.0)
    return pick(ctx.pal["scale"], v)


def _talon(ctx: Ctx):
    rt = rel_y(ctx)
    return pick(ctx.pal["horn"], 0.55 - rt * 0.35 + _LIT[ctx.face])


def _thigh(ctx: Ctx):
    return _fur(ctx)


def _paw(ctx: Ctx):
    rs = (ctx.s + 0.5) / max(ctx.w, 1)
    if ctx.lface == "front" and ctx.t >= ctx.h - 2 and any(abs(rs - f) < 0.06 for f in (0.25, 0.5, 0.75)):
        return pick(ctx.pal["tuft"], 0.3)                                    # die Zehenfugen
    if ctx.lface == "bottom":
        return pick(ctx.pal["tuft"], 0.4)
    return _fur(ctx)


def _tail(ctx: Ctx):
    return _fur(ctx)


def _tail_tuft(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.5 + (hash01(int(math.floor(x * 2)), int(math.floor(y * 2)), int(math.floor(z * 2)), 81) - 0.5) * 0.3
    lz = ctx.local[2]
    if lz < 0.3:
        v -= 0.1
    return pick(ctx.pal["tuft"], v + _LIT[ctx.face])


def materials(variant: str) -> dict:
    return {"fur": _fur, "chest": _chest, "neck": _neck, "ruff": _ruff, "head": _head, "brow": _brow, "cere": _cere, "beak": _beak,
            "beak_tip": _beak_tip, "beak_lower": _beak_lower, "eye": _eye, "tuft_feather": _tuft_feather, "wing_inner": _wing_inner,
            "wing_mid": _wing_mid, "wing_tip": _wing_tip, "tail": _tail, "tail_tuft": _tail_tuft, "trousers": _trousers,
            "tarsus": _tarsus, "toe": _toe, "talon": _talon, "thigh": _thigh, "hind_shin": _fur, "paw": _paw}
