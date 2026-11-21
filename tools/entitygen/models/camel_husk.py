"""
Untote Kamele auf dem Kamel-Skelett der Mod (camel.py – gleiche Knochen wie das Vanilla-Kamel, damit Sitzen, Aufstehen,
Sprint und Sattel passen), ein Höcker:

  * doerrkamel    – vertrocknete, über die Rippen gespannte Haut, eingefallener Höcker, Knochen schauen hervor,
                    bernsteinfarben glimmende Augen, Fetzen der Halsmähne
  * mumienkamel   – in Leinenbinden gewickelte Beine und Hals, Reste einer Satteldecke, Goldschmuck am Zaum
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp, rel_y
from . import camel, farm

PALETTES = {
    "doerrkamel": {"fur": ramp("#5a4a32", "#dcc8a0"), "belly": ramp("#6a5a40", "#e4d2ae"), "dark": ramp("#2a1e12", "#7a6244"),
                   "bone": ramp("#8a7a60", "#f0e6cc"), "eye": ramp("#4a2a02", "#ffc04a"), "socket": ramp("#080604", "#2a1e12"),
                   "hoof": ramp("#2a2420", "#7a6a5e"), "shag": ramp("#2a2014", "#7a6446")},
    "mumienkamel": {"fur": ramp("#4a3c2a", "#bca482"), "belly": ramp("#5a4a36", "#c8b290"), "dark": ramp("#22160c", "#664e36"),
                    "bone": ramp("#8a7a60", "#f0e6cc"), "eye": ramp("#4a2a02", "#ffc04a"), "socket": ramp("#080604", "#2a1e12"),
                    "hoof": ramp("#2a2420", "#7a6a5e"), "shag": ramp("#2a2014", "#7a6446"), "linen": ramp("#5e4e36", "#efe4c8"),
                    "blanket": ramp("#3a0e0a", "#b04a2a"), "gold": ramp("#5a3a08", "#f0c850")},
}
for _p in PALETTES.values():
    _p["back"] = _p["fur"]


def build() -> Model:
    m = camel.build()
    m.name = "camel_husk"
    return m


def _variant(ctx: Ctx) -> str:
    return ctx.params.get("variant")


_HIDE = farm.fur("fur", "belly", seed=191, contrast=0.2, belly_from=0.8, top_dark=0.06)


def _hide(ctx: Ctx):
    """Vertrocknete Haut: rissig, fleckig, an manchen Stellen aufgeplatzt (Knochen darunter)."""
    col = _HIDE(ctx)
    x, y, z = ctx.world
    crack = abs(math.sin(x * 1.3 + z * 0.7 + noise3(x * 0.2, y * 0.2, z * 0.2, 801) * 7.0))
    if crack < 0.07:
        col = mix(col, pick(ctx.pal["dark"], 0.3), 0.6)                                   # Risse im Leder
    blot = noise3(x * 0.25, y * 0.25, z * 0.25, 802)
    if blot > 0.68:
        col = mix(col, pick(ctx.pal["dark"], 0.45), 0.35)
    tear = noise3(x * 0.4 + 5, y * 0.4, z * 0.4, 803)
    if tear > 0.84:
        return pick(ctx.pal["bone"], 0.55 + hash01(ctx.u, ctx.v) * 0.15) if tear > 0.865 else pick(ctx.pal["dark"], 0.2)
    return col


def _bandage(ctx: Ctx):
    x, y, z = ctx.world
    across = x if ctx.face in ("front", "back") else z if ctx.face in ("west", "east") else x + z
    band = y + across * 0.3
    idx = math.floor(band / 2.0)
    frac = band - idx * 2.0
    v = 0.64 + (hash01(idx, 811) - 0.5) * 0.16 - (0.24 if frac > 1.45 else 0.0)
    col = pick(ctx.pal["linen"], v + (hash01(ctx.u, ctx.v, 812) - 0.5) * 0.05)
    if noise3(x * 0.3, y * 0.3, z * 0.3, 813) > 0.66:
        col = mix(col, pick(ctx.pal["dark"], 0.6), 0.35)
    return col


def _coat(ctx: Ctx):
    lx, ly, lz = ctx.local
    if _variant(ctx) == "mumienkamel" and ctx.cube.bone.name == "body" and ctx.face in ("west", "east", "top"):
        # Reste einer Satteldecke mit Goldfransen
        if -17.0 < lz < -2.0 and (ctx.face == "top" or ly < -6.0):
            if ctx.face != "top" and ly > -7.0:
                return pick(ctx.pal["gold"], 0.7) if math.floor(lz) % 2 == 0 else None    # Goldfransen
            if ctx.face != "top" and ly > -8.0:
                return pick(ctx.pal["gold"], 0.5)                                         # Borte
            stripe = math.floor(lz / 2.0) % 3 == 0
            col = pick(ctx.pal["blanket"], (0.55 if stripe else 0.4) + (hash01(ctx.u, ctx.v) - 0.5) * 0.06)
            return col if noise3(lx * 0.4, ly * 0.4, lz * 0.4, 814) < 0.72 else _hide(ctx)
    if ctx.cube.bone.name == "body" and ctx.face in ("west", "east") and -19.0 < lz < -3.0 and ly > -10.5:
        # die Haut spannt sich über die Rippen: helle Rippenbögen, dunkle Mulden dazwischen
        rib = math.floor((lz + (ly + 10.0) * 0.3) / 1.6) % 2
        col = _hide(ctx)
        return mix(col, pick(ctx.pal["fur"], 0.82), 0.4) if rib == 0 else mix(col, pick(ctx.pal["dark"], 0.3), 0.55)
    if ctx.cube.bone.name == "body" and ctx.face == "top" and abs(lx) < 1.0:
        return pick(ctx.pal["bone"], 0.6 if math.floor(lz) % 2 == 0 else 0.42)            # Rückgrat sticht hervor
    return _hide(ctx)


def _hump(ctx: Ctx):
    # eingefallener Höcker: tiefe Falten
    col = _hide(ctx)
    x, y, z = ctx.world
    if math.floor(z * 0.8 + math.sin(x * 0.9) * 0.6) % 3 == 0:
        col = mix(col, pick(ctx.pal["dark"], 0.3), 0.5)
    return col


def _neck(ctx: Ctx):
    if _variant(ctx) == "mumienkamel" and ctx.face != "top":
        return _bandage(ctx)
    if ctx.face == "top":
        return pick(ctx.pal["bone"], 0.58 if math.floor(ctx.world[2] * 0.7) % 2 == 0 else 0.42)   # Halswirbel
    return _hide(ctx)


def _neck_up(ctx: Ctx):
    if _variant(ctx) == "mumienkamel" and ctx.face != "top":
        return _bandage(ctx)
    if ctx.face == "back":
        return pick(ctx.pal["bone"], 0.58 if math.floor(ctx.world[1] * 0.7) % 2 == 0 else 0.42)
    return _hide(ctx)


def _face(ctx: Ctx):
    s, t, face = ctx.s, ctx.t, ctx.face
    if face in ("west", "east") and t in (0, 1, 2) and s in (2, 3, 4):
        # eingefallene Augenhöhle mit Glimmen
        if t == 1 and s == 3:
            glow = pick(ctx.pal["eye"], 0.85)
            ctx.glow = glow
            return glow
        return pick(ctx.pal["socket"], 0.35)
    if face == "front":
        if t == 3 and s in (1, 3):
            return pick(ctx.pal["socket"], 0.2)                                           # Nüstern
        if t == 4:
            return pick(ctx.pal["bone"], 0.7 if s % 2 else 0.5)                           # Zähne hinter den Lippen
        if _variant(ctx) == "mumienkamel" and t == 2:
            return pick(ctx.pal["gold"], 0.7 if s % 2 else 0.5)                           # Goldspange am Zaum
    if face in ("west", "east") and t == 4 and s < 3:
        return pick(ctx.pal["bone"], 0.6 if s % 2 else 0.45)
    return _hide(ctx)


def _shag(ctx: Ctx):
    # nur noch Fetzen der Halsmähne
    if ctx.face in ("west", "east", "front", "back"):
        lane = ctx.s
        if rel_y(ctx) > 0.35 + hash01(lane, ctx.face, 821) * 0.6 or hash01(lane, 822) < 0.35:
            return None
    elif hash01(ctx.u, ctx.v, 823) < 0.5:
        return None
    return pick(ctx.pal["shag"], 0.35 + hash01(ctx.u, ctx.v, 824) * 0.3)


def _leg(ctx: Ctx):
    ry = rel_y(ctx)
    if ctx.face == "bottom" or ry > 0.92:
        return pick(ctx.pal["hoof"], 0.4 + hash01(ctx.u, ctx.v) * 0.15)
    if _variant(ctx) == "mumienkamel" and 0.35 < ry < 0.9:
        return _bandage(ctx)
    if 0.42 < ry < 0.56:
        return pick(ctx.pal["bone"], 0.55 + (0.1 if ctx.face == "front" else 0.0))       # Knie: Knochen unter dünner Haut
    col = _hide(ctx)
    if ctx.face != "top" and (ctx.s == 0 or ctx.s == ctx.w - 1):
        col = mix(col, pick(ctx.pal["dark"], 0.3), 0.35)                                 # magere, kantige Beine
    return col


def _tail(ctx: Ctx):
    if ctx.t > ctx.h - 4:
        return pick(ctx.pal["shag"], 0.3 + hash01(ctx.u, ctx.v) * 0.2) if ctx.s % 2 == 0 and hash01(ctx.u, 831) > 0.3 else None
    return pick(ctx.pal["bone"], 0.5 if ctx.t % 2 else 0.4) if ctx.s == 1 else None


def materials(variant: str) -> dict:
    return {
        "coat": _coat, "hump": _hump, "tail": _tail, "neck": _neck, "neck_up": _neck_up, "face": _face,
        "shag": _shag, "ear": farm.solid("dark", 0.45, 0.1), "leg": _leg,
    }


def hidden(variant: str) -> list[str]:
    return ["hump2"]
