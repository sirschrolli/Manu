"""
Untote Pferde auf dem Pferde-Skelett der Mod (equine.py, Knochen wie beim Vanilla-Pferd, damit Sattel und Rossharnisch
passen):

  * zombiepferd_grau   – fahles Grau-Grün, kahle Stellen, an den Flanken liegen die Rippen frei, zottige Mähne
  * zombiepferd_rappe  – schwarzes, verwesendes Ross mit zerfetzter Mähne und offenen Wunden
  * skelettpferd_bleich – gebleichte Knochen: Rippen, Wirbel, Schädel mit leuchtenden Augenhöhlen, Reste von Mähne
  * skelettpferd_alt   – uralte, braun verfärbte Knochen mit Moos und Flechten

Die Augen leuchten (Leucht-Textur); Mähne und Schweif der Skelettpferde sind nur noch Fetzen (durchsichtige Lücken).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y
from . import equine, farm

_ZOMBIE = {"skin": ramp("#26302a", "#7e927a"), "flesh": ramp("#3a1210", "#a04a3c"), "bone": ramp("#8a8270", "#ece6d2"),
           "eye": ramp("#3a0402", "#ff6a3a"), "hoof": ramp("#141210", "#4a443c"), "socket": ramp("#0a0806", "#2a2420")}

PALETTES = {
    "zombiepferd_grau": {**_ZOMBIE, "fur": ramp("#3a4636", "#9aa88a"), "belly": ramp("#4a5444", "#aab49a"),
                    "mane": ramp("#121410", "#4e5244")},
    "zombiepferd_rappe": {**_ZOMBIE, "fur": ramp("#0e0e10", "#4a4a50"), "belly": ramp("#16161a", "#5a5a60"),
                          "mane": ramp("#060608", "#34343a"), "skin": ramp("#22261e", "#6a7262")},
    "skelettpferd_bleich": {"bone": ramp("#6a6656", "#f4f0e2"), "gap": ramp("#0a0a0c", "#2a2826"), "eye": ramp("#04303a", "#8af4ff"),
                     "hoof": ramp("#3a362e", "#a8a08a"), "mane": ramp("#2a2a28", "#8a8a80"), "moss": ramp("#1a2a10", "#6a8a3a")},
    "skelettpferd_alt": {"bone": ramp("#4a3a24", "#d8c8a0"), "gap": ramp("#0a0806", "#2a2018"), "eye": ramp("#2a4a04", "#c8ff6a"),
                         "hoof": ramp("#2a2014", "#8a7a5a"), "mane": ramp("#1a1610", "#6a5e48"), "moss": ramp("#1a2a10", "#7a9a3a")},
}
for _p in PALETTES.values():
    _p["back"] = _p.get("fur", _p["bone"])


def build() -> Model:
    m = Model("undead_horse", 128, 128)
    equine.skeleton(m, donkey=False)
    return m


def _variant(ctx: Ctx) -> str:
    return ctx.params.get("variant")


def _skeletal(ctx: Ctx) -> bool:
    return _variant(ctx).startswith("skelett")


# =============================================================================================
# Zombiepferde
# =============================================================================================

_FUR = farm.fur("fur", "belly", seed=81, contrast=0.2, top_dark=0.06, belly_from=0.8)


def _rot(ctx: Ctx, col):
    """Kahle, graue Hautstellen und wenige offene Wunden."""
    x, y, z = ctx.world
    bald = noise3(x * 0.22, y * 0.22, z * 0.22, 701)
    if bald > 0.62:
        col = mix(col, pick(ctx.pal["skin"], 0.45 + hash01(ctx.u, ctx.v) * 0.1), min(1.0, (bald - 0.62) * 6.0))
    wound = noise3(x * 0.35 + 11, y * 0.35, z * 0.35, 702)
    if wound > 0.8:
        col = pick(ctx.pal["flesh"], 0.3 + (wound - 0.8) * 3.0)
        if wound > 0.86 and hash01(ctx.u, ctx.v, 703) > 0.5:
            col = pick(ctx.pal["bone"], 0.6)
    return col


def _zombie_coat(ctx: Ctx):
    col = _FUR(ctx)
    lx, ly, lz = ctx.local
    if ctx.cube.bone.name == "body" and ctx.face == "east":
        # an der linken Flanke ist das Fell aufgerissen: Rippen über einer dunklen Höhle, ausgefranster Wundrand
        d = math.hypot((lz + 8.0) / 4.2, (ly + 3.6) / 2.4) + (noise3(lz * 0.5, ly * 0.5, 3.0, 704) - 0.5) * 0.5
        if d < 0.8:
            if math.floor(lz + (ly + 6.0) * 0.3) % 3 != 2:
                return pick(ctx.pal["bone"], 0.5 + hash01(ctx.u, ctx.v) * 0.15)
            return pick(ctx.pal["socket"], 0.2)
        if d < 1.0:
            return pick(ctx.pal["flesh"], 0.25 + hash01(ctx.u, ctx.v) * 0.2)                # Wundrand
    return _rot(ctx, col)


def _zombie_head(ctx: Ctx):
    lx, ly, lz = ctx.local
    if ctx.face in ("west", "east") and 1.0 <= lz < 3.0 and -10.0 <= ly < -9.0:
        if lz >= 2.0:
            glow = pick(ctx.pal["eye"], 0.85)
            ctx.glow = glow
            return glow
        return pick(ctx.pal["socket"], 0.3)
    if ctx.face in ("west", "east") and 0.0 <= lz < 4.0 and -11.0 <= ly < -8.0:
        return pick(ctx.pal["socket"], 0.45)                                              # eingefallene Augenhöhle
    if ctx.face == "west" and lz < 0.5 and -8.0 <= ly < -6.0 and _variant(ctx) == "zombiepferd_grau":
        return pick(ctx.pal["bone"], 0.6 if ctx.s % 2 else 0.45)                          # freiliegender Kieferknochen
    return _rot(ctx, _FUR(ctx))


def _zombie_mouth(ctx: Ctx):
    lx, ly, lz = ctx.local
    if ctx.face == "front":
        if -10.0 <= ly < -9.0 and (ctx.s == 0 or ctx.s == ctx.w - 1):
            return pick(ctx.pal["socket"], 0.1)                                           # Nüstern
        if -8.0 <= ly < -7.0:
            return pick(ctx.pal["bone"], 0.65 if ctx.s % 2 else 0.5)                      # Zähne zwischen den Lippen
        return pick(ctx.pal["skin"], 0.35 + hash01(ctx.u, ctx.v) * 0.1)
    if ctx.face in ("west", "east") and -8.0 <= ly < -7.0 and lz < -3.0:
        return pick(ctx.pal["bone"], 0.6 if ctx.s % 2 else 0.4)
    if lz < -5.5 and ctx.face != "bottom":
        return mix(_FUR(ctx), pick(ctx.pal["skin"], 0.35), 0.7)
    return _rot(ctx, _FUR(ctx))


def _zombie_leg(ctx: Ctx):
    ry = rel_y(ctx)
    if ctx.face == "bottom" or ry > 0.885:
        return pick(ctx.pal["hoof"], 0.35 + hash01(ctx.u, ctx.v) * 0.15 + (0.08 if ctx.s % 2 == 0 else 0.0))
    col = _FUR(ctx)
    if ry > 0.55:
        col = mix(col, pick(ctx.pal["skin"], 0.3), 0.35)                                  # verschmutzte Fesseln
    return _rot(ctx, col)


def _ragged(key: str, seed: int, gaps: float):
    """Zerfetzte Mähne bzw. zerfetzter Schweif: Strähnen mit Lücken und ungleich langen Enden."""
    sh = equine.hair(key, seed=seed, highlights=False)

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        across = z if ctx.face in ("west", "east") else x
        lane = math.floor(across + 0.001)
        if ctx.face in ("west", "east", "front", "back"):
            end = 0.55 + hash01(lane, seed) * 0.45
            if rel_y(ctx) > end or hash01(lane, ctx.cube.bone.name, seed + 1) < gaps:
                return None
        elif hash01(ctx.u, ctx.v, seed + 2) < gaps:
            return None
        return sh(ctx)

    return paint


# =============================================================================================
# Skelettpferde
# =============================================================================================

def _bone(ctx: Ctx, base: float = 0.62):
    x, y, z = ctx.world
    v = base + (noise3(x * 0.4, y * 0.4, z * 0.4, 711) - 0.5) * 0.16 + (hash01(ctx.u, ctx.v, 712) - 0.5) * 0.05
    if ctx.face == "top":
        v += 0.06
    elif ctx.face == "bottom":
        v -= 0.12
    col = pick(ctx.pal["bone"], v)
    if _variant(ctx) == "skelettpferd_alt" and noise3(x * 0.3, y * 0.3, z * 0.3, 713) > 0.64 and ctx.face != "bottom":
        col = mix(col, pick(ctx.pal["moss"], 0.45 + hash01(ctx.u, ctx.v) * 0.2), 0.75)          # Moos und Flechten
    return col


def _skel_coat(ctx: Ctx):
    lx, ly, lz = ctx.local
    name = ctx.cube.bone.name
    if name == "body":
        if ctx.face == "top":
            # Wirbelsäule mit Dornfortsätzen, daneben die Rippenbögen
            if abs(lx) < 1.0:
                return _bone(ctx, 0.78 if math.floor(lz) % 2 == 0 else 0.62)
            if -13.0 < lz < -1.0:
                return _bone(ctx, 0.6) if math.floor(lz) % 3 != 2 else pick(ctx.pal["gap"], 0.45)
            return _bone(ctx, 0.56)
        if ctx.face in ("west", "east"):
            if -13.0 < lz < -1.0:
                # Rippen: schräg nach hinten gebogene Bögen (zwei Pixel Knochen, ein Pixel Schatten), oben am Rückgrat
                if ly < -6.5:
                    return _bone(ctx, 0.68)
                slant = (ly + 6.5) * 0.35
                rib = math.floor(lz + slant) % 3
                if rib == 2:
                    return pick(ctx.pal["gap"], 0.42 - (ly + 6.5) * 0.015)
                return _bone(ctx, 0.64 if rib == 0 else 0.52)
            # Becken bzw. Schulterblatt: breite Knochenplatten mit dunklem Rand zu den Rippen
            if -13.6 < lz < -13.0 or -1.0 <= lz < -0.4:
                return pick(ctx.pal["gap"], 0.5)
            return _bone(ctx, 0.64 - (0.06 if ly > 0.0 else 0.0))
        if ctx.face == "bottom":
            return _bone(ctx, 0.48) if math.floor(lz) % 3 != 2 and -13.0 < lz < -1.0 else pick(ctx.pal["gap"], 0.3)
        if ctx.face == "front":
            # Brustbein mit den Rippenenden
            if abs(lx) < 1.5 or ly < -5.0:
                return _bone(ctx, 0.66)
            return _bone(ctx, 0.56) if math.floor(ly) % 3 != 2 else pick(ctx.pal["gap"], 0.45)
        # hinten: Becken mit Schwanzansatz
        return _bone(ctx, 0.6) if ly < -2.0 or abs(lx) > 3.5 else pick(ctx.pal["gap"], 0.5)
    return _bone(ctx)


def _skel_neck(ctx: Ctx):
    lx, ly, lz = ctx.local
    # Halswirbel: Kette aus Wirbelkörpern mit schmalen Fugen
    seg = math.floor(ly * 0.5) % 2 == 0
    fuge = (ly * 0.5) % 1.0 < 0.22
    if ctx.face in ("west", "east"):
        if fuge:
            return pick(ctx.pal["gap"], 0.45)
        return _bone(ctx, (0.64 if seg else 0.56) - (0.08 if lz < 1.0 else 0.0))
    if ctx.face == "front":
        return pick(ctx.pal["gap"], 0.45) if fuge else _bone(ctx, 0.52)
    return _bone(ctx, 0.66 if seg else 0.56)


def _skel_head(ctx: Ctx):
    lx, ly, lz = ctx.local
    if ctx.face in ("west", "east") and 0.0 <= lz < 4.0 and -11.0 <= ly < -8.0:
        # große Augenhöhle mit Glimmen
        if 1.0 <= lz < 3.0 and -10.0 <= ly < -9.0:
            glow = pick(ctx.pal["eye"], 0.85 if lz >= 2.0 else 0.6)
            ctx.glow = glow
            return glow
        return pick(ctx.pal["gap"], 0.2)
    if ctx.face == "top" and abs(lx) < 0.6:
        return _bone(ctx, 0.5)                                                             # Schädelnaht
    return _bone(ctx, 0.66)


def _skel_mouth(ctx: Ctx):
    lx, ly, lz = ctx.local
    if ctx.face == "front":
        if -10.0 <= ly < -8.5 and abs(lx) < 1.2:
            return pick(ctx.pal["gap"], 0.15)                                             # Nasenöffnung
        if -8.0 <= ly < -7.0:
            return pick(ctx.pal["gap"], 0.2) if ctx.s in (0, ctx.w - 1) else _bone(ctx, 0.8 if ctx.s % 2 else 0.62)
        return _bone(ctx, 0.62)
    if ctx.face in ("west", "east"):
        if -8.0 <= ly < -7.0:
            return _bone(ctx, 0.78) if ctx.s % 2 == 0 else pick(ctx.pal["gap"], 0.2)       # Zahnreihe
        if -10.0 <= ly < -8.0 and lz > -4.0:
            return pick(ctx.pal["gap"], 0.25)                                             # Jochbogen-Lücke
    return _bone(ctx, 0.62)


def _skel_leg(ctx: Ctx):
    ry = rel_y(ctx)
    if ctx.face == "bottom" or ry > 0.885:
        return pick(ctx.pal["hoof"], 0.35 + hash01(ctx.u, ctx.v) * 0.15 + (0.08 if ctx.s % 2 == 0 else 0.0))
    # Röhrenknochen: rund schattiert, verdickte Gelenke als helle Wülste mit Fuge
    if 0.4 < ry < 0.46:
        return pick(ctx.pal["gap"], 0.45)
    joint = 0.34 < ry < 0.52
    edge = ctx.face != "top" and (ctx.s == 0 or ctx.s == ctx.w - 1)
    return _bone(ctx, (0.74 if joint else 0.62) - (0.1 if edge else 0.0))


def _skel_ear(ctx: Ctx):
    return None                                                                            # Ohren sind längst zerfallen


def materials(variant: str) -> dict:
    if variant.startswith("skelett"):
        return {
            "coat": _skel_coat, "neck": _skel_neck, "head": _skel_head, "mouth": _skel_mouth, "leg": _skel_leg,
            "ear": _skel_ear, "mane": _ragged("mane", 91, 0.55), "tail": _ragged("mane", 93, 0.45),
            "feather": lambda ctx: None,
        }
    return {
        "coat": _zombie_coat, "neck": _zombie_coat, "head": _zombie_head, "mouth": _zombie_mouth, "leg": _zombie_leg,
        "ear": equine.ear({}), "mane": _ragged("mane", 91, 0.25), "tail": _ragged("mane", 93, 0.2),
        "feather": lambda ctx: None,
    }


def hidden(variant: str) -> list[str]:
    return ["mane_drape", "forelock_big"] + ["feather_" + n for n in equine.LEGS]
