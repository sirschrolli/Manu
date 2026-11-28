"""
Quetzara, die Gefiederte Schlange (seit 1.10): die Hüterin des Schlangentempels. Eine Riesenschlange, im Spiel 1,5-fach
vergrößert (der Leib knapp anderthalb Blöcke dick, mit Kopf und Schwanzfedern gut zwanzig Blöcke lang):

* **Der Kopf** – ein breiter, dreieckiger Schädel wie bei einer Viper, die Wangen gewölbt, die Schnauze spitz zulaufend; über
  den Augen vorspringende Brauenschilde, die Augen golden mit senkrechtem Schlitz. Der Unterkiefer klappt weit auf, innen
  rosiges Fleisch, oben zwei lange Giftzähne (sie liegen bei geschlossenem Maul zurückgeklappt am Gaumen), die gespaltene
  Zunge züngelt. Auf dem Kopf ein **Kamm** aus fünf langen Quetzalfedern, um den Hals eine **Krause** aus dreizehn Federn,
  die angelegt nach hinten liegt und sich gespreizt wie die Haube einer Kobra aufstellt. Lider decken die Augen im Schlaf.
* **Ein Glied des Leibs** – achteckig im Schnitt, smaragdgrüne Schuppen mit türkisem Schimmer, auf dem Rücken eine Kette
  dunkler Rauten mit goldenem Saum, unten goldene Bauchschilde; auf dem Rückgrat ein Büschel aus zwei Federn (so trägt sie
  einen Federkamm vom Hals bis zum Schwanz).
* **Der Schwanz** – läuft in drei Stücken spitz zu und endet in einem Federbusch: drei lange, schillernde Schleppfedern wie
  beim Quetzal und ein Fächer aus kürzeren Federn.

Drei Kleider: **quetzara** (wie sie schläft und kämpft), **molted** (nach der Häutung: leuchtend, der goldene Saum glüht) und
**shed** (die abgestreifte Haut: blass, papieren, halb durchscheinend, mit leeren Augen).

Knochen-Namen werden in QuetzaraModel (Java) für die Bewegungen verwendet (jaw, fangs, tongue, crest_*, ruff_*, lids).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, hexc, mix, noise3, pick


def stops(*colors: str) -> list:
    return [hexc(c) for c in colors]


_BASE = {
    # Schuppen: tiefes Smaragdgrün, zum Licht hin heller
    "scale": stops("#06180f", "#0a2617", "#0e3520", "#13462a", "#195a35", "#217042", "#2b8851", "#3aa163", "#52bb78", "#74d394"),
    # türkiser Schimmer auf den Flanken
    "sheen": stops("#0f4a48", "#17676a", "#22888a", "#33a8a6", "#55c8c0", "#86e2d6"),
    # die Rauten auf dem Rücken
    "diamond": stops("#020805", "#05120b", "#091d12", "#0e2a1a", "#143822"),
    # Gold: Saum der Rauten, Bauch, Federkiele
    "gold": stops("#3e2804", "#634008", "#8c5c10", "#b27c1a", "#d29e2c", "#e8bc46", "#f6d870", "#fff0a8"),
    "belly": stops("#5e4618", "#80621e", "#a2802a", "#c29c3a", "#d8b450", "#e8c96c", "#f4dc92", "#fbecbe"),
    # Federn: Smaragd mit türkisem Glanz, Spitzen in Rot und Gold (wie beim Quetzal)
    "feather": stops("#082a1c", "#0d3d28", "#125334", "#186b40", "#1f854c", "#299e5a", "#38b56a", "#4fca7c", "#74dc98"),
    "feather_sheen": stops("#0c4646", "#126262", "#1a8080", "#26a09c", "#3cbeb4", "#66d8cc"),
    "crimson": stops("#3e0608", "#640c10", "#8e1418", "#b41e1e", "#d4362a", "#ec5a38", "#fa8a4e"),
    "eye": stops("#6a3a00", "#a86200", "#e09200", "#ffc030", "#ffe27a", "#fff6c8"),
    "mouth": stops("#2e060c", "#4e0c14", "#721822", "#962a32", "#b84448", "#d4686a"),
    "fang": stops("#6a6250", "#9a907a", "#c6bca2", "#e4dcc4", "#f6f0e0", "#fffcf4"),
    "tongue": stops("#3a040c", "#600812", "#8a101c", "#b41e28", "#d23a3a"),
    "dark": stops("#020403", "#06100a", "#0a1a10"),
}

_MOLTED = dict(_BASE)
_MOLTED.update({
    "scale": stops("#08261a", "#0c3a24", "#11502f", "#17683b", "#1e8248", "#279e57", "#33b866", "#47d07a", "#66e292", "#92f0b2"),
    "sheen": stops("#12605c", "#1a8482", "#26a8a4", "#3acac0", "#62e2d6", "#9af4ea"),
    "gold": stops("#5a3a06", "#86580c", "#b07a16", "#d49c24", "#f0bc38", "#ffd858", "#ffec8c", "#fff8cc"),
    "feather": stops("#0c3a28", "#125236", "#196c44", "#218852", "#2ca462", "#3cc074", "#56d68a", "#7ce6a6", "#a8f2c4"),
    "crimson": stops("#5a080c", "#861016", "#b01a1e", "#d42c26", "#f04a32", "#ff7446", "#ffa266"),
})

# die abgestreifte Haut: blass, papieren, das Muster nur noch als Schatten
_SHED = {k: v for k, v in _BASE.items()}
_SHED.update({
    "scale": stops("#5e5848", "#736c5a", "#888068", "#9c9478", "#b0a88a", "#c2ba9c", "#d2caae", "#ded8c0", "#e8e2ce", "#f2eedc"),
    "sheen": stops("#7c7a6a", "#908e7e", "#a4a292", "#b8b6a6", "#cccabc", "#dedcd0"),
    "diamond": stops("#4c4638", "#5a5444", "#686050", "#766e5c", "#847a68"),
    "gold": stops("#6e6650", "#827a62", "#968e74", "#aaa286", "#bcb498", "#ccc4aa", "#dad4bc", "#e8e2ce"),
    "belly": stops("#7a7258", "#8c846a", "#9e967c", "#b0a88e", "#c2baa0", "#d2cab2", "#e0dac4", "#ece8d6"),
    "feather": stops("#5c5a4c", "#6e6c5e", "#807e70", "#929082", "#a4a294", "#b4b2a6", "#c4c2b6", "#d2d0c6", "#e0ded6"),
    "feather_sheen": stops("#7a7a70", "#8e8e84", "#a2a298", "#b6b6ac", "#cacac0", "#dcdcd4"),
    "crimson": stops("#6e5e54", "#827064", "#968474", "#aa9886", "#bcac98", "#ccbeac", "#dacebe"),
    "eye": stops("#2a2620", "#3a352c", "#4a4438", "#5a5444", "#6a6450", "#7a745e"),
})

PALETTES = {"quetzara": _BASE, "quetzara_molted": _MOLTED, "quetzara_shed": _SHED}

#: für das Bildnis auf der Bossleiste: die Krause gespreizt, das Maul halb offen
FOLD_POSE = {**{f"ruff_{k}": (68, 0, 0) for k in range(13)}, "jaw": (24, 0, 0), "crest": (-10, 0, 0)}

_LIT = {"top": 0.1, "front": 0.03, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.16}
_SCALES = Voronoi3D(1.35, 401)
_BIG = Voronoi3D(2.6, 402)

#: Länge eines Glieds (Modell-Einheiten) – etwas länger als der Abstand der Glieder, damit sie sich in Kurven überlappen
SEG_LEN = 16.0
#: halbe Dicke eines Glieds der Dicke 1
SEG_R = 7.0


# =============================================================================================
# Aufbau
# =============================================================================================

def _feather(m: Model, bone: str, parent: str, pivot, rotation, width: float, length: float, material: str) -> None:
    """Eine flache Feder (Kiel unten am Drehpunkt, Spitze nach −y), im Knochen {bone} unter {parent}."""
    m.bone(bone, parent, pivot=pivot, rotation=rotation)
    m.cube(bone, (-width / 2, -length, -0.25), (width, length, 0.5), material)


def build() -> Model:
    """Der Kopf (Drehpunkt: der Nacken; die Schnauze zeigt nach −z)."""
    m = Model("quetzara", 128, 128)
    m.tex_res = 2
    m.world_faces = True
    m.bone("head", pivot=(0, 0, 0))
    # der Hals hinter dem Kopf (geht in das erste Glied über), achteckig
    m.cube("head", (-6, -6, -2), (12, 12, 8), "scale")
    m.bone("neck_oct", "head", pivot=(0, 0, 0), rotation=(0, 0, 45))
    m.cube("neck_oct", (-5.2, -5.2, -1.5), (10.4, 10.4, 7), "scale")
    # der Schädel: hinten breit, mit gewölbten Wangen, nach vorn schmaler – dreieckig wie bei einer Viper
    m.cube("head", (-8, -6, -12), (16, 10, 11), {"bottom": "throat", "*": "scale"})
    for sx in (1, -1):
        m.cube("head", (7.6 if sx > 0 else -9.2, -3.6, -10.5), (1.6, 7.0, 8.0), "scale")
    m.cube("head", (-6.6, -5.8, -16.2), (13.2, 5.8, 4.4), {"bottom": "mouth", "*": "snout"})
    # die Schnauze, nach vorn schmaler und flacher, innen das Fleisch des Gaumens
    m.cube("head", (-5.0, -5.2, -21.2), (10, 5.2, 5.2), {"bottom": "mouth", "*": "snout"})
    m.cube("head", (-3.6, -4.4, -23.8), (7.2, 4.4, 2.8), {"bottom": "mouth", "*": "snout"})
    # Brauenschilde über den Augen (schräg, grimmig)
    for sx in (1, -1):
        brow = f"brow_{'l' if sx > 0 else 'r'}"
        m.bone(brow, "head", pivot=(5.2 * sx, -6.0, -13.0), rotation=(-7, 0, -18 * sx))
        m.cube(brow, (-2.8, -1.5, -3.5), (5.6, 1.7, 10.0), "brow")
    # Schuppenwülste auf dem Scheitel
    m.cube("head", (-3.5, -6.8, -11), (7, 0.8, 9), "crown")
    # die Augen (golden, mit senkrechtem Schlitz) und die Lider, die sie im Schlaf decken
    for sx in (1, -1):
        m.cube("head", (6.0 if sx > 0 else -7.4, -5.0, -15.6), (1.4, 2.6, 3.6), "eye")
    m.bone("lids", "head", pivot=(0, 0, 0))
    for sx in (1, -1):
        m.cube("lids", (5.9 if sx > 0 else -7.65, -5.25, -15.85), (1.75, 3.1, 4.1), "lid")
    # die Nüstern
    for sx in (1, -1):
        m.cube("head", (1.3 if sx > 0 else -2.3, -4.7, -23.5), (1.0, 0.5, 1.0), "nostril")
    # die Giftzähne (klappen mit dem Maul herab)
    m.bone("fangs", "head", pivot=(0, -0.2, -19.6))
    for sx in (1, -1):
        m.cube("fangs", (2.4 if sx > 0 else -3.4, 0, -0.5), (1.0, 3.6, 1.0), "fang")
        m.cube("fangs", (2.6 if sx > 0 else -3.2, 3.6, -0.3), (0.6, 1.3, 0.6), "fang")
    # der Unterkiefer (Gelenk hinten im Maul), innen Fleisch, unten helle Kehlschuppen, kleine Zähne am Rand
    m.bone("jaw", "head", pivot=(0, 0, -4))
    m.cube("jaw", (-4.6, 0, -19.6), (9.2, 2.4, 9.0), {"top": "mouth", "bottom": "throat", "*": "snout"})
    m.cube("jaw", (-6.2, 0, -10.8), (12.4, 3.0, 5.0), {"top": "mouth", "bottom": "throat", "*": "snout"})
    m.cube("jaw", (-7.4, 0, -6.0), (14.8, 3.8, 6.0), {"top": "mouth", "bottom": "throat", "*": "scale"})
    for z in (-18.0, -15.0, -12.0):
        for sx in (1, -1):
            m.cube("jaw", (3.6 if sx > 0 else -4.3, -0.8, z), (0.7, 0.8, 0.7), "fang")
    # die gespaltene Zunge
    m.bone("tongue", "jaw", pivot=(0, -0.3, -8))
    m.cube("tongue", (-0.6, -0.2, -10), (1.2, 0.4, 10), "tongue")
    for sx in (1, -1):
        _side = f"tongue_{'l' if sx > 0 else 'r'}"
        m.bone(_side, "tongue", pivot=(0.25 * sx, 0, -10), rotation=(0, -20 * sx, 0))
        m.cube(_side, (-0.3, -0.2, -3.2), (0.6, 0.4, 3.2), "tongue")
    # der Kamm: fünf lange Quetzalfedern, schräg nach hinten
    m.bone("crest", "head", pivot=(0, -6.6, -7.5))
    for i, (x, length, splay, pitch) in enumerate(((0.0, 17.0, 0, -48), (2.2, 14.0, -13, -53), (-2.2, 14.0, 13, -53),
                                                   (4.0, 11.0, -27, -58), (-4.0, 11.0, 27, -58))):
        _feather(m, f"crest_{i}", "crest", (x, 0, 1.2 * abs(x)), (pitch, 0, splay), 2.8 if i == 0 else 2.5, length, "plume")
    # die Krause: dreizehn Federn rund um den Hals (unten frei); angelegt liegen sie nach hinten, gespreizt stehen sie ab
    m.bone("ruff", "head", pivot=(0, 0, 1.5))
    for k, theta in enumerate(range(-120, 121, 20)):
        side = abs(theta)
        length = 9.0 + 4.5 * math.sin(math.radians(min(side, 90))) - (2.0 if side > 100 else 0.0)
        base = f"ruff_{k}_base"
        m.bone(base, "ruff", pivot=(0, 0, 0), rotation=(0, 0, theta))
        _feather(m, f"ruff_{k}", base, (0, -5.4, 0), (-75, 0, 0), 3.4, length, "ruff")
    return m


def build_segment() -> Model:
    """Ein Glied des Leibs (Mitte am Drehpunkt, vorn −z), achteckig, mit Rautenkette, Bauchschilden und Federbüschel."""
    m = Model("quetzara_segment", 128, 128)
    m.tex_res = 2
    m.world_faces = True
    h = SEG_LEN / 2
    m.bone("body", pivot=(0, 0, 0))
    m.cube("body", (-SEG_R, -SEG_R, -h), (2 * SEG_R, 2 * SEG_R, SEG_LEN), {"bottom": "belly", "*": "scale"})
    m.bone("body_oct", "body", pivot=(0, 0, 0), rotation=(0, 0, 45))
    m.cube("body_oct", (-6.3, -6.3, -h + 0.5), (12.6, 12.6, SEG_LEN - 1), "scale")
    m.cube("body", (-5.2, SEG_R - 0.2, -h + 0.4), (10.4, 0.9, SEG_LEN - 0.8), "belly")
    m.cube("body", (-1.4, -SEG_R - 0.8, -h + 0.5), (2.8, 0.9, SEG_LEN - 1), "ridge")
    # das Federbüschel auf dem Rückgrat
    _feather(m, "tuft", "body", (0, -SEG_R - 0.5, -2.5), (-64, 0, 0), 2.8, 8.5, "tuft")
    _feather(m, "tuft2", "body", (0, -SEG_R - 0.5, 2.5), (-72, 0, 0), 2.2, 6.0, "tuft")
    return m


def build_tail() -> Model:
    """Der Schwanz (Drehpunkt: Mitte des letzten Glieds, nach hinten +z): drei spitz zulaufende Stücke und der Federbusch."""
    m = Model("quetzara_tail", 128, 128)
    m.tex_res = 2
    m.world_faces = True
    m.bone("tail", pivot=(0, 0, 0))
    m.cube("tail", (-3.6, -3.6, -4), (7.2, 7.2, 12), {"bottom": "belly", "*": "scale"})
    m.cube("tail", (-0.8, -4.3, -3.5), (1.6, 0.8, 11), "ridge")
    m.bone("tail2", "tail", pivot=(0, 0, 8), rotation=(3, 0, 0))
    m.cube("tail2", (-2.6, -2.6, 0), (5.2, 5.2, 10), {"bottom": "belly", "*": "scale"})
    m.bone("tail3", "tail2", pivot=(0, 0, 10), rotation=(4, 0, 0))
    m.cube("tail3", (-1.7, -1.7, 0), (3.4, 3.4, 8), {"bottom": "belly", "*": "scale"})
    m.cube("tail3", (-2.2, -2.2, 6), (4.4, 4.4, 2.6), "gold_band")
    # der Federbusch: drei lange Schleppfedern, ein Fächer aus kürzeren
    m.bone("plume", "tail3", pivot=(0, 0, 8))
    for i, (yaw, length, width) in enumerate(((0, 38.0, 4.2), (-8, 33.0, 3.8), (8, 33.0, 3.8))):
        _feather(m, f"streamer_{i}", "plume", (0, -0.2 * i, 0), (-90 + 3 * (i == 0), yaw, 0), width, length, "streamer")
    for i, yaw in enumerate((-56, -38, -20, 20, 38, 56)):
        _feather(m, f"fan_{i}", "plume", (0, -0.5, 0), (-80, yaw, 0), 3.2, 17.0 - abs(yaw) * 0.09, "plume")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

def _shed(ctx: Ctx) -> bool:
    return ctx.params.get("variant", "").endswith("shed")


def _molted(ctx: Ctx) -> bool:
    return ctx.params.get("variant", "").endswith("molted")


def _alpha(ctx: Ctx, col, a: int):
    """Die abgestreifte Haut ist halb durchscheinend."""
    if _shed(ctx):
        return col[0], col[1], col[2], a
    return col


def _rs_rt(ctx: Ctx) -> tuple[float, float]:
    return (ctx.s + 0.5) / max(ctx.w, 1), (ctx.t + 0.5) / max(ctx.h, 1)


def _scale(ctx: Ctx):
    """
    Schuppen: kleine Zellen mit feinen dunklen Fugen; der Rücken dunkler mit einer Kette von Rauten (dunkel, goldener Saum),
    die Flanken heller mit einem weichen türkisen Schimmer und einer Reihe goldener Punkte, unten der goldene Bauch.
    """
    x, y, z = ctx.world
    d, edge = _SCALES(x, y, z)
    n = noise3(x * 0.18, y * 0.18, z * 0.18, 403)
    # Höhe im Querschnitt: -1 oben … +1 unten (y zeigt nach unten)
    up = max(-1.0, min(1.0, y / SEG_R))
    v = 0.56 + _LIT.get(ctx.face, 0.0) * 0.6 + (d - 0.5) * 0.08 + (n - 0.5) * 0.08 - max(0.0, -up) * 0.16 + max(0.0, up) * 0.04
    if edge < 0.22:
        v -= 0.13
    col = pick(ctx.pal["scale"], v)
    if ctx.face == "top" or up < -0.72:
        # Rauten auf dem Rücken (eine je Glied, längs)
        zz = ((z + SEG_LEN / 2) % SEG_LEN) - SEG_LEN / 2
        dm = abs(x) / 5.2 + abs(zz) / 7.6
        if dm < 0.76:
            col = pick(ctx.pal["diamond"], 0.45 + (d - 0.5) * 0.25 + (0.15 if edge > 0.6 else 0.0))
        elif dm < 0.98:
            col = pick(ctx.pal["gold"], 0.6 + _LIT.get(ctx.face, 0.0) * 0.5 + (d - 0.5) * 0.15)
            if _molted(ctx):
                ctx.glow = mix((0, 0, 0, 255), col, 0.75)
    elif up < 0.62:
        # Flanken: ein weicher türkiser Schimmer oben, eine Reihe goldener Punkte in der Mitte
        sheen = max(0.0, 1.0 - abs(up + 0.35) / 0.35)
        if sheen > 0.0 and edge > 0.22:
            col = mix(col, pick(ctx.pal["sheen"], 0.5 + (d - 0.5) * 0.2), sheen * 0.32)
        if abs(up - 0.12) < 0.09 and d > 0.7 and edge > 0.3:
            col = pick(ctx.pal["gold"], 0.58)
            if _molted(ctx):
                ctx.glow = mix((0, 0, 0, 255), col, 0.6)
    else:
        # unten der Übergang zum Bauch: helle Schilde
        col = pick(ctx.pal["belly"], 0.6 + (d - 0.5) * 0.15 - (0.14 if edge < 0.22 else 0.0))
    return _alpha(ctx, col, 205)


def _snout(ctx: Ctx):
    """Die Schnauze: Schuppen, oben große Schilde, unten eine helle Lippenkante, ein dunkler Streif vom Auge zum Maul."""
    x, y, z = ctx.world
    if ctx.face in ("west", "east", "front"):
        if -1.2 < y < 0.9:
            return _alpha(ctx, pick(ctx.pal["belly"], 0.66 + (hash01(ctx.u, ctx.v, 405) - 0.5) * 0.1), 205)
        if abs(y + 3.0 + (z + 14.0) * 0.12) < 0.9 and z > -20:
            return _alpha(ctx, pick(ctx.pal["diamond"], 0.6), 205)
    if ctx.face == "top":
        d, edge = _BIG(x, y, z)
        v = 0.5 + _LIT["top"] + (d - 0.5) * 0.18
        if edge < 0.25:
            v -= 0.22
        return _alpha(ctx, pick(ctx.pal["scale"], v), 205)
    return _scale(ctx)


def _crown(ctx: Ctx):
    """Scheitel: große Schilde mit goldenem Muster (ein Pfeil, der zur Schnauze weist)."""
    x, y, z = ctx.world
    d, edge = _BIG(x, y * 2, z)
    v = 0.48 + _LIT.get(ctx.face, 0.0) + (d - 0.5) * 0.2
    col = pick(ctx.pal["scale"], v - (0.22 if edge < 0.25 else 0.0))
    if abs(abs(x) * 1.2 - (z + 11.0) * 0.55) < 0.6 or abs(x) < 0.5 and z > -9:
        col = pick(ctx.pal["gold"], 0.6)
        if _molted(ctx):
            ctx.glow = mix((0, 0, 0, 255), col, 0.8)
    return _alpha(ctx, col, 205)


def _brow(ctx: Ctx):
    rs, rt = _rs_rt(ctx)
    v = 0.4 + _LIT.get(ctx.face, 0.0) + (hash01(ctx.u // 2, ctx.v // 2, 406) - 0.5) * 0.1
    if ctx.face == "top" and (int(rt * 5) % 2 == 0):
        v += 0.08
    return _alpha(ctx, pick(ctx.pal["scale"], v), 205)


def _throat(ctx: Ctx):
    """Unter dem Kopf: helle, quer gereihte Kehlschilde."""
    x, y, z = ctx.world
    band = (z * 0.5) % 1.0
    v = 0.6 + (noise3(x * 0.6, y, z * 0.6, 407) - 0.5) * 0.12 - (0.2 if band < 0.14 else 0.0)
    return _alpha(ctx, pick(ctx.pal["belly"], v), 205)


def _belly(ctx: Ctx):
    """Bauchschilde: breite, quer liegende goldene Platten mit dunkler Fuge."""
    x, y, z = ctx.world
    band = ((z + 0.5) * 0.36) % 1.0
    v = 0.56 + (noise3(x * 0.5, y * 0.5, z * 0.5, 408) - 0.5) * 0.14
    if band < 0.13:
        v -= 0.3
    elif band > 0.78:
        v += 0.1
    if abs(x) > 4.6:
        v -= 0.12
    return _alpha(ctx, pick(ctx.pal["belly"], v), 205)


def _ridge(ctx: Ctx):
    """Die Schuppenleiste auf dem Rückgrat: dunkel, mit goldenen Kielen."""
    x, y, z = ctx.world
    k = (z * 0.5) % 1.0
    col = pick(ctx.pal["diamond"], 0.6 + _LIT.get(ctx.face, 0.0))
    if k < 0.3:
        col = pick(ctx.pal["gold"], 0.5 + _LIT.get(ctx.face, 0.0))
        if _molted(ctx):
            ctx.glow = mix((0, 0, 0, 255), col, 0.6)
    return _alpha(ctx, col, 205)


def _gold_band(ctx: Ctx):
    rs, rt = _rs_rt(ctx)
    v = 0.6 + _LIT.get(ctx.face, 0.0) + (0.15 if abs(rt - 0.5) < 0.2 else -0.05)
    col = pick(ctx.pal["gold"], v)
    if _molted(ctx):
        ctx.glow = mix((0, 0, 0, 255), col, 0.7)
    return _alpha(ctx, col, 205)


def _eye(ctx: Ctx):
    """Golden mit senkrechtem Schlitz (die leere Haut: trübe Schalen, kein Glanz)."""
    rs, rt = _rs_rt(ctx)
    if ctx.lface in ("top", "bottom", "back"):
        return _alpha(ctx, pick(ctx.pal["dark"], 0.6), 205)
    slit = abs(rs - 0.5) < 0.13 and 0.12 < rt < 0.88
    if _shed(ctx):
        return _alpha(ctx, pick(ctx.pal["eye"], 0.6 - abs(rs - 0.5) * 0.4), 140)
    if slit:
        return pick(ctx.pal["dark"], 0.1)
    d = math.hypot(rs - 0.5, rt - 0.5) * 2.0
    col = pick(ctx.pal["eye"], 0.95 - d * 0.55 + (0.08 if rt < 0.3 else 0.0))
    ctx.glow = col
    return col


def _lid(ctx: Ctx):
    rs, rt = _rs_rt(ctx)
    v = 0.42 + _LIT.get(ctx.face, 0.0) + (0.08 if abs(rt - 0.5) < 0.1 else 0.0)
    return _alpha(ctx, pick(ctx.pal["scale"], v), 205)


def _nostril(ctx: Ctx):
    return _alpha(ctx, pick(ctx.pal["dark"], 0.3), 205)


def _mouth(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.5 + (noise3(x * 0.8, y * 0.8, z * 0.8, 409) - 0.5) * 0.25
    if abs(x) < 0.8:
        v -= 0.15                                      # die Rinne in der Mitte
    return _alpha(ctx, pick(ctx.pal["mouth"], v), 205)


def _fang(ctx: Ctx):
    rs, rt = _rs_rt(ctx)
    return _alpha(ctx, pick(ctx.pal["fang"], 0.75 + _LIT.get(ctx.face, 0.0) - rt * 0.15 + (hash01(ctx.u, ctx.v, 410) - 0.5) * 0.08), 205)


def _tongue(ctx: Ctx):
    rs, rt = _rs_rt(ctx)
    return _alpha(ctx, pick(ctx.pal["tongue"], 0.6 + _LIT.get(ctx.face, 0.0) + (hash01(ctx.u, ctx.v, 411) - 0.5) * 0.1), 205)


def _feather_painter(kind: str):
    """
    Eine Feder: außerhalb ihres Umrisses durchsichtig (die Fahne ist in der Mitte breit, zur Spitze und zum Kiel schmal), in
    der Mitte der goldene Kiel, schräge Äste, die Farbe vom Kiel (dunkles Smaragd) zur Spitze (Türkis); an der Spitze ein
    Band in Rot und Gold – je nach Art der Feder.
    """

    def paint(ctx: Ctx):
        rs, rt = _rs_rt(ctx)                         # rt: 0 = Spitze, 1 = Kiel
        if ctx.lface not in ("front", "back"):
            # die schmalen Seiten: nur am Kiel zu sehen
            if ctx.lface == "bottom" and abs(rs - 0.5) < 0.25:
                return _alpha(ctx, pick(ctx.pal["gold"], 0.5), 180)
            return None
        dx = abs(rs - 0.5) * 2.0
        if kind == "streamer":
            prof = math.sin(math.pi * min(1.0, rt * 1.05)) ** 0.35 * (0.55 + 0.45 * rt)
        elif kind == "ruff":
            prof = math.sin(math.pi * min(1.0, 0.08 + rt * 0.95)) ** 0.5
        else:
            prof = math.sin(math.pi * min(1.0, 0.04 + rt)) ** 0.55
        quill = rt > 0.88
        if quill:
            prof = 0.18
        if dx > prof + 0.04:
            return None
        if dx < 0.12:
            col = pick(ctx.pal["gold"], 0.62 - rt * 0.2)    # der Kiel
            return _alpha(ctx, col, 200)
        # die Äste: schräg vom Kiel nach außen zur Spitze
        barb = ((rt * ctx.h * 0.55 + dx * 3.0) % 1.0) < 0.35
        base = 0.35 + (1.0 - rt) * 0.45 - dx * 0.12 + (0.05 if ctx.lface == "front" else -0.04)
        if barb:
            base -= 0.08
        col = pick(ctx.pal["feather"], base + (hash01(ctx.u, ctx.v, 412) - 0.5) * 0.06)
        # türkiser Glanz zur Spitze hin
        if rt < 0.55:
            col = mix(col, pick(ctx.pal["feather_sheen"], 0.5 + (0.55 - rt)), (0.55 - rt) * 1.1)
        # das Band an der Spitze
        tip_band = {"plume": 0.16, "ruff": 0.2, "tuft": 0.0, "streamer": 0.1}[kind]
        if rt < tip_band:
            if kind == "ruff" and rt < tip_band * 0.45:
                col = pick(ctx.pal["gold"], 0.7)
            else:
                col = pick(ctx.pal["crimson"], 0.5 + (tip_band - rt) * 2.0 - dx * 0.2)
            if _molted(ctx):
                ctx.glow = mix((0, 0, 0, 255), col, 0.65)
        # ein Auge auf den Schleppfedern (wie beim Pfau, nur dezent)
        if kind == "streamer" and abs(rt - 0.22) < 0.05 and dx < prof * 0.6:
            col = pick(ctx.pal["gold"], 0.75)
        if dx > prof - 0.06:
            col = mix(col, pick(ctx.pal["feather"], 0.2), 0.4)  # dunkler Rand
        return _alpha(ctx, col, 170)

    return paint


def materials(variant: str) -> dict:
    return {"scale": _scale, "snout": _snout, "crown": _crown, "brow": _brow, "throat": _throat, "belly": _belly, "ridge": _ridge,
            "gold_band": _gold_band, "eye": _eye, "lid": _lid, "nostril": _nostril, "mouth": _mouth, "fang": _fang,
            "tongue": _tongue, "plume": _feather_painter("plume"), "ruff": _feather_painter("ruff"), "tuft": _feather_painter("tuft"),
            "streamer": _feather_painter("streamer")}
