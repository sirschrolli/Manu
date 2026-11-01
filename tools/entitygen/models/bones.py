"""
Gebeine auf dem Skelett der Vanilla-Skelette (HumanoidModel mit dünnen Gliedern wie in
SkeletonModel.createDefaultSkeletonMesh – gleiche Teile und Drehpunkte, damit Bogen, Rüstung, Köpfe und alle
Bewegungen passen). Ein Modell für die ganze Familie:

  * Brustkorb mit Rippen und Lücken (durchsichtig), eigene Wirbelsäule und eigenes Becken, Unterkiefer, Knie- und
    Ellbogenknöchel, Fußknochen
  * Zubehör in eigenen Knochen, auf den Bildern der anderen Arten durchsichtig: Gewand (Wappenrock, Köchergurt,
    Pelzumhang, Moosbehang, Wüstenbinden – alles auf derselben Hülle), Kragen (Kettenkragen, Kapuzenkragen, Pelzkragen,
    Moospolster, Schal), Köcher mit Pfeilen, Pilze (nur Moorgebein; "mushrooms" wie beim Vanilla-Moorskelett, damit
    das Scheren sie ausblendet)

  * Gerippe (statt Skelett): alte vergilbte Knochen, Ritter mit rostigem Nasalhelm und zerfetztem Wappenrock,
    Jäger mit zerschlissener Kapuze und Köcher
  * Frostgebein (statt Eiswanderer): Reif und Eiskrusten, zerfetzter Pelzumhang, eisblaue Augen
  * Moorgebein (statt Sumpfskelett): Moos, Schlamm, Ranken, rote und braune Pilze, Baumpilze
  * Dörrgebein (statt Ausgedörrtem): sonnengebleicht, sandfarbene Wüstenbinden, vertrocknete Sehnen
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y

_SOCKET = ramp("#050404", "#2a2420")
_EMBER = ramp("#2a0904", "#b04a20")          # schwaches Glimmen der Gerippe

_GERIPPE = {"bone": ramp("#6a5a40", "#f6eed6", steps=8), "stain": ramp("#5a4426", "#a88a5a"), "socket": _SOCKET,
            "eye": _EMBER}


def _pal(base: dict, **extra) -> dict:
    pal = dict(base)
    for key, value in extra.items():
        pal[key] = ramp(*value) if isinstance(value, tuple) else value
    return pal


PALETTES = {
    "gerippe_knochen": _pal(_GERIPPE),
    "gerippe_ritter": _pal(_GERIPPE, metal=("#24221f", "#8e8a82"), rust=("#3e1a06", "#c0682a"), cloth=("#4a0e0c", "#c8483a"),
                           trim=("#7a6a3a", "#f0dca0"), mail=("#262422", "#9a958c"), leather=("#24160c", "#7a5232"),
                           brass=("#4a3410", "#d8b050")),
    "gerippe_jaeger": _pal(_GERIPPE, cloth=("#16200f", "#6e7c44"), leather=("#24160c", "#8a5e36"), brass=("#4a3410", "#d8b050"),
                           wood=("#3a2614", "#a07848"), feather=("#6a6a66", "#f0eee6"), fletch=("#5a1010", "#c84030")),
    "frostgebein": _pal({"bone": ramp("#8ca6b6", "#ffffff", steps=8), "stain": ramp("#5a7486", "#a6c4d4"), "socket": _SOCKET,
                         "eye": ramp("#04304a", "#a8f4ff")},
                        frost=("#9ac4dc", "#ffffff"), ice=("#4a8cbc", "#d8f6ff"), fur=("#1c1610", "#8e7c64"),
                        hide=("#2a2018", "#7a6248"), leather=("#1e140c", "#6a4a2e")),
    "moorgebein": _pal({"bone": ramp("#545440", "#e0dcba", steps=8), "stain": ramp("#3a4020", "#7a8048"), "socket": _SOCKET,
                        "eye": ramp("#1a3a02", "#d0ff60")},
                       moss=("#183010", "#8cbc44"), mud=("#1e160c", "#6a5230"), vine=("#0e1e08", "#557e2c"),
                       mush_red=("#5a0808", "#e83a2a"), mush_spot=("#c8c0b0", "#ffffff"), mush_brown=("#3a2414", "#b08a5a"),
                       stem=("#8a8070", "#efe8d6"), shelf=("#3a2410", "#c89a5a")),
    "doerrgebein": _pal({"bone": ramp("#a29478", "#fffdf6", steps=8), "stain": ramp("#8a7656", "#d8c49a"), "socket": _SOCKET,
                         "eye": ramp("#4a2002", "#ffc860")},
                        sand=("#8a6a3a", "#f6e6c0"), stripe=("#6a2c16", "#c4683a"), sinew=("#3a1a0e", "#8a4a2a")),
}

# Zubehör je Bild (Knochen-Namen); was nicht dabei ist, bleibt durchsichtig
GEAR = {
    "gerippe_knochen": set(),
    "gerippe_ritter": {"garb", "collar"},
    "gerippe_jaeger": {"garb", "collar", "quiver"},
    "frostgebein": {"garb", "collar"},
    "moorgebein": {"garb", "collar", "mushrooms"},
    "doerrgebein": {"garb", "collar"},
}
ALL_GEAR = {"garb", "collar", "quiver", "mushrooms"}


def build() -> Model:
    m = Model("bones", 128, 64)
    # --- Kopf: Schädel, Überlage (Helm, Kapuze, Reif, Moos, Kopftuch), Unterkiefer, Pilze
    m.bone("head")
    m.cube("head", (-4, -8, -4), (8, 8, 8), "skull")
    m.bone("hat", "head")
    m.cube("hat", (-4, -8, -4), (8, 8, 8), "hat", inflate=0.5)
    m.bone("jaw", "head", pivot=(0, -0.2, -2.2), rotation=(10, 0, 0))
    m.cube("jaw", (-3, 0, -2), (6, 2, 2), "jaw")
    m.bone("mushrooms", "head")
    m.bone("red_mushroom_1", "mushrooms", pivot=(2, -8, 1.5), rotation=(0, 45, 0))
    m.cube("red_mushroom_1", (-3, -4, 0), (6, 4, 0), "mush_red")
    m.bone("red_mushroom_2", "mushrooms", pivot=(2, -8, 1.5), rotation=(0, 135, 0))
    m.cube("red_mushroom_2", (-3, -4, 0), (6, 4, 0), "mush_red")
    m.bone("brown_mushroom_1", "mushrooms", pivot=(-2.5, -8, -1.5), rotation=(0, 45, 0))
    m.cube("brown_mushroom_1", (-2.5, -3, 0), (5, 3, 0), "mush_brown")
    m.bone("brown_mushroom_2", "mushrooms", pivot=(-2.5, -8, -1.5), rotation=(0, 135, 0))
    m.cube("brown_mushroom_2", (-2.5, -3, 0), (5, 3, 0), "mush_brown")
    m.bone("shelf_fungus", "mushrooms", pivot=(4, -3, 1))                    # Baumpilze an der Schläfe
    m.cube("shelf_fungus", (0, -1, -1.5), (2, 1, 3), "shelf")
    m.cube("shelf_fungus", (0, 0.6, -0.5), (1.4, 1, 2), "shelf")

    # --- Rumpf: Brustkorb, Wirbelsäule, Becken; Gewand, Kragen, Köcher
    m.bone("body")
    m.cube("body", (-4, 0, -2), (8, 8, 4), "ribs")
    m.cube("body", (-1, 0, 0.3), (2, 10.5, 1.5), "spine")
    m.cube("body", (-3.5, 10, -1.5), (7, 2, 3), "pelvis")
    m.bone("garb", "body")
    m.cube("garb", (-4.5, -0.5, -2.5), (9, 16, 5), "garb")
    m.bone("collar", "body")
    m.cube("collar", (-5, -1, -3), (10, 3, 6), "collar")
    m.bone("quiver", "body", pivot=(-3, -0.5, 2.7), rotation=(0, 0, -26))
    m.cube("quiver", (-1.5, 0, 0), (3, 10, 2), "quiver")
    m.bone("arrows", "quiver")
    m.cube("arrows", (-1.1, -2.5, 0.5), (1, 3, 1), "arrow")
    m.cube("arrows", (0.1, -3.2, 0.6), (1, 4, 1), "arrow")

    # --- Glieder (dünn wie bei Vanilla) mit Knöcheln und Füßen
    m.bone("right_arm", pivot=(-5, 2, 0))
    m.cube("right_arm", (-1, -2, -1), (2, 12, 2), "arm")
    m.cube("right_arm", (-1, 3.5, -1), (2, 1, 2), "knob", inflate=0.25)
    m.bone("left_arm", pivot=(5, 2, 0))
    m.cube("left_arm", (-1, -2, -1), (2, 12, 2), "arm")
    m.cube("left_arm", (-1, 3.5, -1), (2, 1, 2), "knob", inflate=0.25)
    m.bone("right_leg", pivot=(-2, 12, 0))
    m.cube("right_leg", (-1, 0, -1), (2, 12, 2), "leg")
    m.cube("right_leg", (-1, 5, -1), (2, 1, 2), "knob", inflate=0.3)
    m.cube("right_leg", (-1, 11, -2.8), (2, 1, 2), "foot")
    m.bone("left_leg", pivot=(2, 12, 0))
    m.cube("left_leg", (-1, 0, -1), (2, 12, 2), "leg")
    m.cube("left_leg", (-1, 5, -1), (2, 1, 2), "knob", inflate=0.3)
    m.cube("left_leg", (-1, 11, -2.8), (2, 1, 2), "foot")
    return m


# =============================================================================================
# Hilfen
# =============================================================================================

def _variant(ctx: Ctx) -> str:
    return ctx.params.get("variant")


def _kind(ctx: Ctx) -> str:
    v = _variant(ctx)
    return "gerippe" if v.startswith("gerippe") else v


def _hidden(ctx: Ctx) -> bool:
    bone = ctx.cube.bone
    while bone is not None:
        if bone.name in ALL_GEAR:
            return bone.name not in GEAR[_variant(ctx)]
        bone = bone.parent
    return False


def _bone(ctx: Ctx, base: float = 0.66):
    """Knochen: matt, leicht fleckig, mit Altersflecken; je Art Reif, Moos oder Bleiche obendrauf."""
    x, y, z = ctx.world
    n = noise3(x * 0.45, y * 0.45, z * 0.45, 501)
    v = base + (n - 0.5) * 0.09 + (hash01(ctx.u, ctx.v, 502) - 0.5) * 0.05
    if ctx.face == "top":
        v += 0.07
    elif ctx.face == "bottom":
        v -= 0.14
    col = pick(ctx.pal["bone"], v)
    stain = noise3(x * 0.28 + 11, y * 0.28, z * 0.28, 503)
    if stain > 0.7:
        col = mix(col, pick(ctx.pal["stain"], 0.6), min(0.3, (stain - 0.7) * 2.0))
    kind = _kind(ctx)
    if kind == "frostgebein":
        # Reif nur, wo er liegen bleibt (Oberseiten), dazu vereinzelte Eiskristalle
        if ctx.face == "top":
            return pick(ctx.pal["frost"], 0.6 + hash01(ctx.u, ctx.v, 505) * 0.4)
        if hash01(ctx.u, ctx.v, 504) > 0.94:
            return pick(ctx.pal["frost"], 0.85)
    elif kind == "moorgebein":
        moss = noise3(x * 0.4 + 5, y * 0.4, z * 0.4, 506)
        if (ctx.face == "top" and moss > 0.35) or moss > 0.74:
            return pick(ctx.pal["moss"], 0.35 + hash01(ctx.u, ctx.v, 507) * 0.4)
        if moss > 0.68:
            return mix(col, pick(ctx.pal["moss"], 0.4), 0.45)
    return col


def _crack(ctx: Ctx, col, seed: int):
    """Feine Risse im Knochen (bei allen Arten selten, beim Dörrgebein häufiger)."""
    x, y, z = ctx.world
    rate = 0.07 if _kind(ctx) == "doerrgebein" else 0.035
    line = abs(math.sin(x * 2.1 + y * 1.3 - z * 1.7 + noise3(x * 0.3, y * 0.3, z * 0.3, seed) * 7.0))
    if line < rate and hash01(ctx.u, ctx.v, seed + 1) > 0.25:
        return mix(col, pick(ctx.pal["bone"], 0.08), 0.65)
    return col


def _cloth(ctx: Ctx, key: str, base: float = 0.45, seed: int = 601, holes: float = 0.0):
    """Grobes, verblichenes Tuch mit Webrichtung, Falten und (bei holes > 0) Löchern."""
    x, y, z = ctx.world
    n = noise3(x * 0.45, y * 0.3, z * 0.45, seed)
    v = base + math.sin(x * 1.3 + n * 3.0) * 0.06 + (n - 0.5) * 0.14 + (hash01(ctx.u, ctx.v, seed) - 0.5) * 0.08
    if ctx.face == "top":
        v += 0.06
    if holes > 0:
        h = noise3(x * 0.6 + 17, y * 0.6, z * 0.6, seed + 1)
        if h > 1.0 - holes:
            return None
        if h > 1.0 - holes - 0.05:
            v -= 0.16
    return pick(ctx.pal[key], v)


def _ragged(ctx: Ctx, base_len: int, spread: int, seed: int) -> int:
    """Länge einer ausgefransten Kante je Spalte (in Zeilen)."""
    return base_len + int(hash01(ctx.s, ctx.face, seed) * (spread + 1))


def _mail(ctx: Ctx):
    """Kettengeflecht: versetzte Ringreihen, rostig."""
    s, t = ctx.s, ctx.t
    ring = (s + t) % 2 == 0
    v = (0.66 if ring else 0.3) + (hash01(ctx.u, ctx.v, 611) - 0.5) * 0.08
    if t % 2 == 1:
        v -= 0.06
    col = pick(ctx.pal["mail"], v)
    x, y, z = ctx.world
    if noise3(x * 0.5, y * 0.5, z * 0.5, 612) > 0.58:
        col = mix(col, pick(ctx.pal["rust"], 0.4 + hash01(ctx.u, ctx.v) * 0.25), 0.65)
    return col


def _rusty(ctx: Ctx, base: float = 0.5):
    """Rostiges Eisen: matte Fläche, Rostflecken und Rostläufer."""
    x, y, z = ctx.world
    v = base + (noise3(x * 0.9, y * 0.9, z * 0.9, 621) - 0.5) * 0.12 + (hash01(ctx.u, ctx.v, 622) - 0.5) * 0.06
    if ctx.face == "top":
        v += 0.14
    elif ctx.t == 0:
        v += 0.1                                                                               # Glanzkante
    col = pick(ctx.pal["metal"], v)
    r = noise3(x * 0.75, y * 0.75, z * 0.75, 623)
    if r > 0.66:
        col = mix(col, pick(ctx.pal["rust"], 0.4 + (r - 0.66) * 1.5), 0.8)
    elif ctx.face != "top" and hash01(ctx.s, ctx.face, 624) > 0.72 and ctx.t >= 1:
        col = mix(col, pick(ctx.pal["rust"], 0.45), 0.45)                                     # Rostläufer
    return col


# =============================================================================================
# Kopf
# =============================================================================================

def _eye_glow(ctx: Ctx):
    level = 0.55 if _kind(ctx) == "gerippe" else 0.88
    col = pick(ctx.pal["eye"], level)
    ctx.glow = col
    return col


def _skull(ctx: Ctx):
    kind = _kind(ctx)
    face, s, t = ctx.face, ctx.s, ctx.t
    col = _bone(ctx, 0.7)
    if face == "front":
        if t in (3, 4) and s in (1, 2, 5, 6):                                            # Augenhöhlen
            if t == 4 and s in (2, 5):
                return _eye_glow(ctx)
            return pick(ctx.pal["socket"], 0.15 if t == 3 else 0.3)
        if t == 2 and s in (1, 2, 5, 6):
            return pick(ctx.pal["bone"], 0.38)                                            # Brauenwulst wirft Schatten
        if t == 5 and s in (3, 4):
            return pick(ctx.pal["socket"], 0.25)                                          # Nasenöffnung
        if t == 5 and s in (1, 6):
            return pick(ctx.pal["bone"], 0.82)                                            # Jochbein
        if t == 6:
            if s in (0, 7):
                return pick(ctx.pal["bone"], 0.4)
            return pick(ctx.pal["bone"], 0.8 if s % 2 == 0 else 0.62)                     # obere Zähne
        if t == 7:
            if s in (0, 7):
                return pick(ctx.pal["bone"], 0.45)
            return pick(ctx.pal["socket"], 0.1)                                           # offener Mund
        return _crack(ctx, col, 631)
    if face == "top":
        # Schädelnähte: Pfeilnaht längs, Kranznaht quer
        if (s == 3 + (t % 2) and t <= 5) or (t == 5 and 1 <= s <= 6 and (s + t) % 3 != 0):
            col = mix(col, pick(ctx.pal["bone"], 0.25), 0.55)
        return col
    if face in ("west", "east"):
        front_col = ctx.w - 1 if face == "west" else 0
        if t in (3, 4, 5) and abs(ctx.s - front_col) == 1:
            return mix(col, pick(ctx.pal["bone"], 0.35), 0.5)                              # Schläfengrube
        if t == 7:
            return pick(ctx.pal["bone"], 0.4)
        return _crack(ctx, col, 632)
    if face == "back":
        if t == 7 and 2 <= s <= 5:
            return pick(ctx.pal["bone"], 0.38)
        return _crack(ctx, col, 633)
    if face == "bottom":
        if 3 <= s <= 4 and 3 <= t <= 4:
            return pick(ctx.pal["socket"], 0.3)                                           # Hinterhauptsloch
        return pick(ctx.pal["bone"], 0.32 + hash01(ctx.u, ctx.v) * 0.08)
    return col


def _jaw(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if t == 0:
            if s in (0, 5):
                return pick(ctx.pal["bone"], 0.5)
            return pick(ctx.pal["bone"], 0.82 if s % 2 == 1 else 0.6)                     # untere Zähne
        return _bone(ctx, 0.58) if s not in (0, 5) else pick(ctx.pal["bone"], 0.48)
    if face == "top":
        if t == ctx.h - 1:
            return pick(ctx.pal["bone"], 0.78 if s % 2 == 1 else 0.6)
        return pick(ctx.pal["socket"], 0.2)                                               # Mundboden
    if face == "bottom":
        return pick(ctx.pal["bone"], 0.36)
    return _bone(ctx, 0.52)


def _hat(ctx: Ctx):
    """Überlage des Kopfes (wie Vanilla 'hat'): je Art Helm, Kapuze, Reif, Moos oder Kopftuch."""
    v = _variant(ctx)
    face, s, t, w, h = ctx.face, ctx.s, ctx.t, ctx.w, ctx.h
    if v == "gerippe_ritter":
        # rostiger Nasalhelm: Glocke bis über die Brauen, Nasenschiene, Nietenrand
        if face == "top":
            return _rusty(ctx, 0.55)
        if face == "bottom":
            return None
        rim = 2 if face == "front" else 3 if face in ("west", "east") else 4
        if t < rim:
            return _rusty(ctx, 0.5)
        if t == rim:
            col = _rusty(ctx, 0.36)
            if s % 3 == 1:
                col = pick(ctx.pal["metal"], 0.8)                                         # Nieten
            return col
        if face == "front" and s in (3, 4) and t <= 5:
            return _rusty(ctx, 0.46 if s == 3 else 0.38)                                  # Nasenschiene
        return None
    if v == "gerippe_jaeger":
        # zerschlissene Kapuze: rahmt das Gesicht, unten ausgefranst
        if face == "bottom":
            return None
        if face == "front":
            if t == 0:
                return _cloth(ctx, "cloth", 0.46, seed=641)
            if t == 1 and 1 <= s <= 6:
                return _cloth(ctx, "cloth", 0.26, seed=641)                                 # Schatten am Kapuzenrand
            if s in (0, 7) and t <= 6:
                return _cloth(ctx, "cloth", 0.3 if t >= 2 else 0.4, seed=641)
            return None
        if face == "top":
            col = _cloth(ctx, "cloth", 0.54, seed=642, holes=0.06)
            return col if not (s in (3, 4) and t >= 4) else _cloth(ctx, "cloth", 0.4, seed=642)  # Naht
        if t >= h - 1 - int(hash01(s, face, 643) * 2):
            return None
        if face == "back" and s in (3, 4):
            return _cloth(ctx, "cloth", 0.32, seed=644)                                     # Mittelnaht
        return _cloth(ctx, "cloth", 0.44 - t * 0.015, seed=644, holes=0.08)
    if v == "frostgebein":
        # Reifkruste mit Eiszapfen an Schläfen und Hinterkopf
        if face == "bottom":
            return None
        if face == "top":
            return pick(ctx.pal["frost"], 0.55 + hash01(ctx.u, ctx.v, 651) * 0.45) if hash01(ctx.u, ctx.v, 652) > 0.12 else None
        if face == "front":
            if t == 0 and hash01(s, 653) > 0.25:
                return pick(ctx.pal["frost"], 0.75)
            return None
        crust = 1 + int(hash01(s, face, 654) * 2)
        if t < crust:
            return pick(ctx.pal["frost"], 0.6 + hash01(ctx.u, ctx.v, 655) * 0.35)
        back_half = face == "back" or (face == "west" and s < w // 2) or (face == "east" and s >= w // 2)
        icicle = crust + int(hash01(s, face, 656) * 5) if back_half and hash01(s, face, 657) > 0.45 else crust
        if t < icicle:
            return pick(ctx.pal["ice"], 0.4 + (t - crust) * 0.12)                          # Eiszapfen, unten heller
        return None
    if v == "moorgebein":
        # Moospolster mit herabhängenden Strähnen
        if face == "bottom":
            return None
        if face == "top":
            return pick(ctx.pal["moss"], 0.35 + hash01(ctx.u, ctx.v, 661) * 0.4) if hash01(ctx.u, ctx.v, 662) > 0.1 else None
        if face == "front":
            if t == 0 or (s in (0, 7) and t <= int(hash01(s, 663) * 3) + 1):
                return pick(ctx.pal["moss"], 0.45 + hash01(ctx.u, ctx.v) * 0.2)
            return None
        cover = 2 + int(hash01(s, face, 664) * 2)
        if t < cover:
            return pick(ctx.pal["moss"], 0.35 + hash01(ctx.u, ctx.v, 665) * 0.4)
        strand = cover + int(hash01(s, face, 666) * 5) if hash01(s, face, 667) > 0.4 else cover
        if t < strand:
            return pick(ctx.pal["vine"], 0.3 + hash01(ctx.u, ctx.v, 668) * 0.4)
        return None
    if v == "doerrgebein":
        # Kopftuch: Stirnband mit Webmuster, Tuch über Scheitel und Nacken, Zipfel hinten
        if face == "bottom":
            return None
        stripe = t == 1 and face != "top"
        if face == "front":
            if t <= 2 or (s in (0, 7) and t <= 4):
                return pick(ctx.pal["stripe"], 0.5 + (0.15 if s % 2 else 0.0)) if stripe else _cloth(ctx, "sand", 0.5, seed=671)
            return None
        if face == "top":
            return _cloth(ctx, "sand", 0.58, seed=672)
        if face == "back":
            tail = 7 if 3 <= s <= 4 else 5 + int(hash01(s, 673) * 2)
            if t <= tail:
                return pick(ctx.pal["stripe"], 0.55) if stripe else _cloth(ctx, "sand", 0.5, seed=674, holes=0.06)
            return None
        if t <= 4 - int(hash01(s, face, 675) * 2):
            return pick(ctx.pal["stripe"], 0.55) if stripe else _cloth(ctx, "sand", 0.5, seed=676)
        return None
    return None


# --- Pilze (nur Moorgebein) --------------------------------------------------------------------

_RED_MUSHROOM = ["..rr..",
                 ".RwRR.",
                 "RRRwRr",
                 "..ss.."]
_BROWN_MUSHROOM = [".bBb.",
                   "bBBBb",
                   "..s.."]


def _sprite(ctx: Ctx, rows: list[str], width: float):
    """Pilz-Bild auf einer Ebene: nach Lage im Raum gemalt, damit Vorder- und Rückseite zusammenpassen."""
    lx, ly, _ = ctx.local
    ox, oy, _ = ctx.cube.origin
    col_i = int((lx - ox) / width * len(rows[0]))
    row_i = int((ly - oy) / ctx.cube.size[1] * len(rows))
    col_i = max(0, min(len(rows[0]) - 1, col_i))
    row_i = max(0, min(len(rows) - 1, row_i))
    return rows[row_i][col_i]


def _mush_red(ctx: Ctx):
    if _hidden(ctx):
        return None
    c = _sprite(ctx, _RED_MUSHROOM, 6)
    return {"r": pick(ctx.pal["mush_red"], 0.55), "R": pick(ctx.pal["mush_red"], 0.85), "w": pick(ctx.pal["mush_spot"], 0.8),
            "s": pick(ctx.pal["stem"], 0.7)}.get(c)


def _mush_brown(ctx: Ctx):
    if _hidden(ctx):
        return None
    c = _sprite(ctx, _BROWN_MUSHROOM, 5)
    return {"b": pick(ctx.pal["mush_brown"], 0.45), "B": pick(ctx.pal["mush_brown"], 0.75), "s": pick(ctx.pal["stem"], 0.65)}.get(c)


def _shelf(ctx: Ctx):
    if _hidden(ctx):
        return None
    if ctx.face == "top":
        ring = (ctx.s + ctx.t) % 2
        return pick(ctx.pal["shelf"], 0.62 + ring * 0.15)
    if ctx.face == "bottom":
        return pick(ctx.pal["stem"], 0.55)                                                  # helle Poren unten
    return pick(ctx.pal["shelf"], 0.4)


# =============================================================================================
# Rumpf
# =============================================================================================

def _ribs(ctx: Ctx):
    """Brustkorb: Schlüsselbeine, Brustbein, Rippen mit Lücken, hinten Wirbelsäule und Schulterblätter."""
    face, s, t, w = ctx.face, ctx.s, ctx.t, ctx.w
    if face == "front":
        if t == 0:
            return _bone(ctx, 0.66)                                                        # Schlüsselbeine
        if s in (3, 4) and t <= 6:
            return _bone(ctx, 0.7 if t <= 5 else 0.55)                                     # Brustbein
        if t == 7:
            return _bone(ctx, 0.55) if s in (0, 1, 6, 7) else None                         # Rippenbogen
        if t % 2 == 1 or (t == 6 and s in (0, 7)):
            return _crack(ctx, _bone(ctx, 0.62 - (0.06 if s in (0, 7) else 0.0)), 701)
        return None
    if face in ("west", "east"):
        if t == 0 or t % 2 == 1:
            return _bone(ctx, 0.55)
        return None
    if face == "back":
        if s in (3, 4):
            return _bone(ctx, 0.68 if t % 2 == 0 else 0.5)                                 # Wirbel
        if t <= 3 and s in (1, 2, 5, 6) and not (t == 3 and s in (1, 6)):
            return _bone(ctx, 0.6)                                                         # Schulterblätter
        if t % 2 == 1:
            return _bone(ctx, 0.56)
        return None
    if face == "top":
        if t in (0, ctx.h - 1) or s in (0, w - 1):
            return _bone(ctx, 0.66)
        if t == 1 and s in (3, 4):
            return _bone(ctx, 0.5)
        return None
    if face == "bottom":
        return _bone(ctx, 0.4) if t == 0 and s in (3, 4) else None
    return None


def _spine(ctx: Ctx):
    if ctx.t % 2 == 1 and _kind(ctx) == "doerrgebein" and ctx.t >= 7:
        return pick(ctx.pal["sinew"], 0.4 + hash01(ctx.u, ctx.v) * 0.15)                     # vertrocknete Bänder
    v = 0.68 if ctx.t % 2 == 0 else 0.46
    if ctx.face in ("west", "east") and ctx.t % 2 == 0 and ctx.t >= 8:
        v = 0.74                                                                           # Querfortsätze
    return _bone(ctx, v)


def _pelvis(ctx: Ctx):
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "front":
        if t == 0:
            return pick(ctx.pal["bone"], 0.4) if s in (1, 5) else _bone(ctx, 0.66)        # Hüftlöcher
        return None if s == 3 else _bone(ctx, 0.55)                                         # Schambogen
    if face == "back":
        if s == 3:
            return _bone(ctx, 0.72)                                                        # Kreuzbein
        return _bone(ctx, 0.55)
    if face == "top":
        return _bone(ctx, 0.7) if s in (0, 1, 5, 6) or t == 0 else _bone(ctx, 0.5)         # Beckenschaufeln
    if face == "bottom":
        return _bone(ctx, 0.35)
    return _bone(ctx, 0.58)


def _garb(ctx: Ctx):
    """Gewand auf der Hülle um den Rumpf – je Art etwas anderes, sonst durchsichtig."""
    if _hidden(ctx):
        return None
    v = _variant(ctx)
    face, s, t, w, h = ctx.face, ctx.s, ctx.t, ctx.w, ctx.h
    if v == "gerippe_ritter":
        # zerfetzter Wappenrock: Bahn vorn und hinten, Schulterstreifen, Gürtel, verblichenes Kreuz
        if face == "top":
            return _cloth(ctx, "cloth", 0.4, seed=711) if s in (2, 3, 5, 6) else None
        if face == "bottom":
            return None
        if t == 10:
            if face in ("west", "east") or 1 <= s <= 7:
                return pick(ctx.pal["brass"], 0.7) if face == "front" and s == 4 else pick(ctx.pal["leather"], 0.4 + (s % 2) * 0.12)
        if face in ("west", "east"):
            return None
        if not 1 <= s <= 7 or t == 0:
            return None
        if t >= 12 + int(hash01(s, face, 712) * 4):
            return None                                                                     # zerfetzter Saum
        col = _cloth(ctx, "cloth", 0.5, seed=713, holes=0.1)
        if col is None:
            return None
        if face == "front" and (s == 4 and 2 <= t <= 8 or t == 4 and 2 <= s <= 6):
            col = mix(pick(ctx.pal["trim"], 0.55), col, 0.25)                               # verblichenes Kreuz
        if s in (1, 7):
            col = pick(ctx.pal["trim"], 0.4)                                                # Borte
        return col
    if v == "gerippe_jaeger":
        # Köchergurt quer über Brust und Rücken, Gürtel mit Messer
        if face == "top":
            return pick(ctx.pal["leather"], 0.5) if s in (1, 2) else None
        if face == "bottom":
            return None
        if t == 10:
            if face == "west" and ctx.s in (1, 2):
                return pick(ctx.pal["leather"], 0.25)                                        # Messerscheide
            return pick(ctx.pal["brass"], 0.7) if face == "front" and s == 4 else pick(ctx.pal["leather"], 0.42 + (s % 2) * 0.1)
        if face == "west" and ctx.s in (1, 2) and 11 <= t <= 12:
            return pick(ctx.pal["leather"], 0.2 + (t - 11) * 0.1)
        if face in ("front", "back") and 1 <= t <= 9:
            line = 1 + (t - 1) * 7 / 8 if face == "front" else 7 - (t - 1) * 7 / 8
            if abs(s - line) < 1.0:
                if face == "front" and t == 5:
                    return pick(ctx.pal["brass"], 0.65)
                return pick(ctx.pal["leather"], 0.5 + (0.1 if (s + t) % 2 else 0.0))
        return None
    if v == "frostgebein":
        # zerfetzter Pelzumhang über Schultern und Rücken, unten vereist
        if face == "top":
            return _fur(ctx, 0.62) if t <= 2 else None
        if face == "bottom" or face == "front":
            return None
        if face in ("west", "east"):
            back_cols = (0, 1) if face == "west" else (w - 2, w - 1)
            if s not in back_cols or t > 11:
                return None
            return _fur(ctx, 0.5)
        end = 12 + int(hash01(s, 721) * 4)
        if t > end:
            return None
        if t >= end - 1:
            return pick(ctx.pal["ice"], 0.5 + hash01(ctx.u, ctx.v) * 0.3)                    # Eis am Saum
        if hash01(ctx.s, ctx.t, 722) > 0.93:
            return None
        return _fur(ctx, 0.48)
    if v == "moorgebein":
        # Moos und Ranken hängen von den Schultern
        if face == "bottom":
            return None
        if face == "top":
            return pick(ctx.pal["moss"], 0.35 + hash01(ctx.u, ctx.v) * 0.35) if s in (0, 1, 2, 6, 7, 8) else None
        cover = int(hash01(s, face, 731) * 2)
        if t <= cover:
            return pick(ctx.pal["moss"], 0.3 + hash01(ctx.u, ctx.v, 732) * 0.4)
        if hash01(s, face, 733) > 0.62:
            length = cover + 2 + int(hash01(s, face, 734) * 7)
            if t <= length:
                if hash01(ctx.u, ctx.v, 735) > 0.8:
                    return pick(ctx.pal["moss"], 0.75)                                        # Blättchen
                return pick(ctx.pal["vine"], 0.25 + hash01(ctx.u, ctx.v, 736) * 0.35)
        return None
    if v == "doerrgebein":
        # Wüstenbinden um den Brustkorb, Schärpe und Lendentuch
        if face in ("top", "bottom"):
            return None
        band = t in (2, 3, 6) or t in (10, 11)
        if band:
            if hash01(ctx.s, ctx.t, face, 741) > 0.86:
                return None
            if t in (10, 11):
                return pick(ctx.pal["stripe"], 0.45 + (0.12 if s % 2 else 0.0)) if t == 10 else _cloth(ctx, "sand", 0.5, seed=742)
            return _cloth(ctx, "sand", 0.55 if t != 3 else 0.42, seed=743)
        if t >= 12 and face in ("front", "back") and 2 <= s <= 6:
            if t >= 14 + int(hash01(s, face, 744) * 2):
                return None
            return _cloth(ctx, "sand", 0.46, seed=745)
        return None
    return None


def _fur(ctx: Ctx, base: float):
    """Zottiges Fell: Strähnen nach unten, Reif an den Spitzen."""
    x, y, z = ctx.world
    strand = noise3(x * 1.4, y * 0.35, z * 1.4, 751)
    v = base + (strand - 0.5) * 0.35 + (hash01(ctx.u, ctx.v, 752) - 0.5) * 0.08
    col = pick(ctx.pal["fur"], v)
    if hash01(ctx.u, ctx.v, 753) > 0.94 or ctx.face == "top" and hash01(ctx.u, ctx.v, 754) > 0.5:
        col = mix(col, pick(ctx.pal["frost"], 0.85), 0.6)
    return col


def _collar(ctx: Ctx):
    """Kragen auf den Schultern; vorn in der Mitte offen (Hals und Unterkiefer bleiben frei)."""
    if _hidden(ctx):
        return None
    v = _variant(ctx)
    face, s, t, w, h = ctx.face, ctx.s, ctx.t, ctx.w, ctx.h
    if face == "bottom":
        return None
    if face == "front" and 3 <= s <= 6:
        return None
    if face == "top" and 2 <= s <= 7 and 1 <= t <= 4:
        return None                                                                          # Halsausschnitt
    last = t == h - 1
    if v == "gerippe_ritter":
        if last and hash01(ctx.s, face, 761) > 0.6:
            return None
        return _mail(ctx)
    if v == "gerippe_jaeger":
        if last and hash01(ctx.s, face, 762) > 0.45:
            return None
        return _cloth(ctx, "cloth", 0.38, seed=763, holes=0.06)
    if v == "frostgebein":
        if last and hash01(ctx.s, face, 764) > 0.5:
            return None
        return _fur(ctx, 0.6)
    if v == "moorgebein":
        # Moospolster nur oben auf den Schultern, an den Seiten hängen einzelne Strähnen
        if face == "top":
            return pick(ctx.pal["moss"], 0.4 + hash01(ctx.u, ctx.v, 767) * 0.4) if hash01(ctx.u, ctx.v, 765) > 0.12 else None
        if t == 0 or (t <= 1 + int(hash01(s, face, 766) * 2) and hash01(s, face, 768) > 0.5):
            return pick(ctx.pal["moss"] if t == 0 else ctx.pal["vine"], 0.35 + hash01(ctx.u, ctx.v, 767) * 0.35)
        return None
    if v == "doerrgebein":
        if last and hash01(ctx.s, face, 768) > 0.5:
            return None
        if t == 1 and face != "top":
            return pick(ctx.pal["stripe"], 0.5 + (0.12 if s % 2 else 0.0))
        return _cloth(ctx, "sand", 0.52, seed=769)
    return None


def _quiver(ctx: Ctx):
    if _hidden(ctx):
        return None
    face, s, t = ctx.face, ctx.s, ctx.t
    if face == "top":
        return pick(ctx.pal["leather"], 0.1)                                                 # Öffnung
    col = pick(ctx.pal["leather"], 0.42 + (hash01(ctx.u, ctx.v, 771) - 0.5) * 0.1)
    if t in (0, ctx.h - 1):
        return pick(ctx.pal["leather"], 0.28)                                                # Randnaht
    if t in (2, 7) and face in ("front", "back"):
        return pick(ctx.pal["brass"], 0.6) if s == 1 else pick(ctx.pal["leather"], 0.62)     # Bänder
    return col


def _arrow(ctx: Ctx):
    if _hidden(ctx):
        return None
    r = rel_y(ctx)
    if ctx.face == "top":
        return pick(ctx.pal["fletch"], 0.6)
    if r < 0.6:
        return pick(ctx.pal["feather"], 0.55 + (0.25 if ctx.s % 2 == 0 else 0.0)) if ctx.t != 1 else pick(ctx.pal["fletch"], 0.65)
    return pick(ctx.pal["wood"], 0.5)


# =============================================================================================
# Glieder
# =============================================================================================

def _arm(ctx: Ctx):
    kind = _kind(ctx)
    r = rel_y(ctx)
    t, h = ctx.t, ctx.h
    if ctx.face == "bottom":
        return pick(ctx.pal["bone"], 0.4)
    if t >= h - 2:
        if ctx.face == "front" or ctx.face == "back":
            return _bone(ctx, 0.72 if ctx.s % 2 == 0 else 0.45) if t == h - 1 else _bone(ctx, 0.6)   # Fingerknochen
        return _bone(ctx, 0.55)
    if kind == "doerrgebein" and 7 <= t <= 9:
        return _cloth(ctx, "sand", 0.5 + (0.1 if t == 8 else 0.0), seed=781)                  # Armbinden
    col = _bone(ctx, 0.64 if r < 0.45 else 0.58)
    return _crack(ctx, col, 782)


def _leg(ctx: Ctx):
    kind = _kind(ctx)
    t, h = ctx.t, ctx.h
    if ctx.face == "bottom":
        return pick(ctx.pal["bone"], 0.38)
    if kind == "doerrgebein" and 8 <= t <= 10:
        return _cloth(ctx, "sand", 0.48 + (0.1 if t == 9 else 0.0), seed=791)                # Wadenbinden
    col = _bone(ctx, 0.64 if t < 6 else 0.58)
    if kind == "moorgebein" and t >= 8:
        mud = (t - 8) / 4
        col = mix(col, pick(ctx.pal["mud"], 0.4 + hash01(ctx.u, ctx.v) * 0.2), min(0.9, 0.35 + mud * 0.6))
    return _crack(ctx, col, 792)


def _knob(ctx: Ctx):
    col = _bone(ctx, 0.7)
    if _kind(ctx) == "doerrgebein" and ctx.face == "back":
        return pick(ctx.pal["sinew"], 0.42 + (0.12 if ctx.s % 2 else 0.0))                  # Sehnen hinter Knie und Ellbogen
    return col


def _foot(ctx: Ctx):
    kind = _kind(ctx)
    if ctx.face == "front":
        col = _bone(ctx, 0.72 if ctx.s % 2 == 0 else 0.48)                                   # Zehen
    elif ctx.face == "bottom":
        col = pick(ctx.pal["bone"], 0.35)
    else:
        col = _bone(ctx, 0.6)
    if kind == "moorgebein":
        col = mix(col, pick(ctx.pal["mud"], 0.4), 0.8)
    return col


def materials(variant: str) -> dict:
    return {
        "skull": _skull,
        "hat": _hat,
        "jaw": _jaw,
        "mush_red": _mush_red,
        "mush_brown": _mush_brown,
        "shelf": _shelf,
        "ribs": _ribs,
        "spine": _spine,
        "pelvis": _pelvis,
        "garb": _garb,
        "collar": _collar,
        "quiver": _quiver,
        "arrow": _arrow,
        "arm": _arm,
        "leg": _leg,
        "knob": _knob,
        "foot": _foot,
    }


def hidden(variant: str) -> list[str]:
    return sorted(ALL_GEAR - GEAR[variant])
