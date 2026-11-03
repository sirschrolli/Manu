"""
Kaulquappe (statt Vanilla-Kaulquappe) auf dem Skelett von TadpoleModel: Knochen "body" (0, 22, -3) und "tail"
(0, 22, 0); TadpoleModel dreht nur "tail" (yRot, Schwimmschlag).

Rundlicher, fast schwarzer olivfarbener Leib (Kern wie Vanilla plus Kappen oben, unten und vorn), bronzener Bauch,
feine Goldsprenkel, seitliche Knopfaugen mit Goldrand und ein heller Mundrand vorn. Der Schwanz hat einen dunklen
Muskelstrang und einen hohen Flossensaum oben und unten, der zur Spitze ausläuft – mit dunklen Pünktchen, helleren
Rändern und Alpha im Saum (durchscheinend, sobald mit einem durchscheinenden RenderType gezeichnet wird; mit dem
Vanilla-RenderType entityCutout erscheint er deckend in denselben Farben).

TadpoleModel zeichnet mit Rückseiten-Culling, darum sind beide Seiten der Flossenfläche bemalt. Alle Quader haben
ganzzahlige Maße (UVs passen in Minecraft genau).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, ramp

PALETTES = {
    "kaulquappe": {
        "skin": ramp("#0c0c06", "#5e5c2c"),
        "belly": ramp("#2a2416", "#a8945c"),
        "gold": ramp("#9a741c", "#f8de80"),
        "fin": ramp("#4a4628", "#d6cc94"),
        "back": ramp("#0c0c06", "#5e5c2c"),
    },
}


def build() -> Model:
    m = Model("tadpole", 32, 32)
    m.bone("body", pivot=(0, 22, -3))
    m.cube("body", (-1.5, -1, 0), (3, 2, 3), "body")                 # Kern wie Vanilla
    m.cube("body", (-1, -1.5, 0.5), (2, 1, 2), "body_cap")           # Rundung oben
    m.cube("body", (-1, 0.5, 0.5), (2, 1, 2), "belly_cap")           # Rundung unten
    m.cube("body", (-1, -0.5, -0.5), (2, 1, 1), "snout")             # Schnauze mit Mundrand
    m.cube("body", (-2, -0.9, 0.5), (1, 1, 1), "eye")                # rechtes Auge
    m.cube("body", (1, -0.9, 0.5), (1, 1, 1), "eye")                 # linkes Auge
    m.bone("tail", pivot=(0, 22, 0))
    m.cube("tail", (-0.5, -0.5, -0.5), (1, 1, 5), "tail_core")       # Muskelstrang
    m.cube("tail", (0, -2, 0), (0, 4, 8), "fin", faces=("west", "east"))
    return m


# =============================================================================================
# Malen
# =============================================================================================

def _fleck(ctx: Ctx, col: tuple, chance: float = 0.86) -> tuple:
    """Goldsprenkel: meist matt bronzen, selten ein heller Glanzpunkt."""
    x, y, z = ctx.world
    h = hash01(round(x * 2), round(y * 2), round(z * 2), 61)
    if h > chance:
        return pick(ctx.pal["gold"], 0.8) if h > 0.975 else mix(col, pick(ctx.pal["gold"], 0.5), 0.6)
    return col


def _skin(ctx: Ctx, base: float) -> tuple:
    x, y, z = ctx.world
    tone = base + (noise3(x * 0.8, y * 0.8, z * 0.8, 62) - 0.5) * 0.12
    if ctx.face == "top":
        tone += 0.06
    elif ctx.face in ("west", "back"):
        tone -= 0.03
    return pick(ctx.pal["skin"], tone)


def _body(ctx: Ctx):
    face, t, h = ctx.face, ctx.t, ctx.h
    if face == "bottom":
        return pick(ctx.pal["belly"], 0.5 + (noise3(*ctx.world, 63) - 0.5) * 0.12)
    if face in ("west", "east", "front", "back") and t == h - 1:
        return mix(_skin(ctx, 0.36), pick(ctx.pal["belly"], 0.45), 0.55)   # Übergang zum Bauch
    return _fleck(ctx, _skin(ctx, 0.4 if face == "top" else 0.34))


def _body_cap(ctx: Ctx):
    if ctx.face == "top":
        return _fleck(ctx, _skin(ctx, 0.5), 0.82)                         # Rundung mit Glanz
    return _fleck(ctx, _skin(ctx, 0.42))


def _belly_cap(ctx: Ctx):
    return pick(ctx.pal["belly"], 0.56 if ctx.face == "bottom" else 0.5)


def _snout(ctx: Ctx):
    if ctx.face == "front":
        return pick(ctx.pal["belly"], 0.7)                                # heller Mundrand
    if ctx.face == "bottom":
        return pick(ctx.pal["belly"], 0.5)
    return _skin(ctx, 0.38)


def _eye(ctx: Ctx):
    """Knopfauge: außen die schwarze Pupille, ringsum der Goldrand."""
    x = ctx.local[0]
    outer = (ctx.face == "west" and x < 0) or (ctx.face == "east" and x > 0)
    if outer:
        return (10, 9, 6, 255)
    if ctx.face in ("back", "bottom"):
        return _skin(ctx, 0.36)
    return pick(ctx.pal["gold"], 0.52 if ctx.face == "top" else 0.4)


def _tail_core(ctx: Ctx):
    z = ctx.world[2]
    k = max(0.0, min(1.0, z / 4.5))
    if ctx.face == "bottom":
        return pick(ctx.pal["belly"], 0.42)
    return _fleck(ctx, _skin(ctx, 0.36 + k * 0.08), 0.88)


_FIN = (        # Zeilen y -2..2 (oben -> unten), Spalten z 0..8 (vom Leib -> Spitze)
    ".eeeee..",
    "mmmmmfft",
    "mmmmmff.",
    ".eeee...",
)


def _fin(ctx: Ctx):
    y, z = ctx.local[1], ctx.local[2]
    r, c = int(y + 2), int(z)
    if not (0 <= r < 4 and 0 <= c < 8):
        return None
    k = _FIN[r][c]
    if k == ".":
        return None
    shade, alpha = {"e": (0.72, 150), "f": (0.56, 175), "m": (0.34, 230), "t": (0.64, 150)}[k]
    if k == "e" and c % 2 == 1:
        shade -= 0.08                                                     # feine Flossenstrahlen
    if k != "m" and hash01(c, r, 7, 64) > 0.82:
        shade -= 0.22                                                     # dunkle Pünktchen im Saum
    col = pick(ctx.pal["fin"], shade)
    return (col[0], col[1], col[2], alpha)


def materials(variant: str) -> dict:
    return {"body": _body, "body_cap": _body_cap, "belly_cap": _belly_cap, "snout": _snout, "eye": _eye,
            "tail_core": _tail_core, "fin": _fin}
