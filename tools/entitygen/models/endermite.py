"""
Leermilbe (statt Endermite) auf dem Skelett der Vanilla-Endermite: vier Segmente (segment0 … segment3), die
EndermiteModel schlängeln lässt – deshalb heißen die Knochen wie bei Vanilla. Eigenes Aussehen:

  * ein Panzer aus dunklem Purpurkristall in Platten, die Fugen dazwischen glimmen violett
  * vorn ein Kopf mit vier glühenden Punktaugen, zwei langen Fühlern und Beißzangen
  * am großen Segment drei Beinpaare (dünne, geknickte Stelzen) und drei Kristalldornen auf dem Rücken
  * hinten läuft sie in einen leuchtenden Kristallstachel aus
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, ramp

PALETTES = {
    "leermilbe": {
        "shell": ramp("#0c0614", "#5a3a78", steps=8), "seam": [hexc(c) for c in ("#3a1460", "#7a2ab8", "#c060f0", "#f0b0ff")],
        "eye": [hexc(c) for c in ("#a02ad0", "#f080ff", "#ffffff")], "crystal": ramp("#2a1240", "#f0c8ff", steps=7),
        "leg": ramp("#08040c", "#3a2a4a"),
    },
}

# Lage der Segmente wie in EndermiteModel.createBodyLayer: (Breite, Höhe, Tiefe), Drehpunkt z
_SIZES = ((4, 3, 2), (6, 4, 5), (3, 3, 1), (1, 2, 1))


def build() -> Model:
    m = Model("endermite", 64, 32)
    m.tex_res = 2
    z = -3.5
    for i, (w, h, d) in enumerate(_SIZES):
        name = f"segment{i}"
        m.bone(name, pivot=(0, 24 - h, z))
        m.cube(name, (-w / 2, 0, -d / 2), (w, h, d), ("head", "shell", "shell", "tail")[i])
        if i < 3:
            z += (d + _SIZES[i + 1][2]) / 2
    # Kopf: Fühler und Beißzangen
    m.cube("segment0", (-1.6, -0.2, -3.6), (0.5, 0.5, 2.6), "feeler")
    m.cube("segment0", (1.1, -0.2, -3.6), (0.5, 0.5, 2.6), "feeler")
    m.cube("segment0", (-1.4, 2.2, -1.9), (0.8, 0.6, 1), "mandible")
    m.cube("segment0", (0.6, 2.2, -1.9), (0.8, 0.6, 1), "mandible")
    # Beine (drei Paare) und Rückendornen am großen Segment
    for k, lz in enumerate((-1.6, 0.1, 1.8)):
        m.cube("segment1", (-4.4, 2.6, lz - 0.25), (1.6, 0.5, 0.5), "leg")
        m.cube("segment1", (-4.6, 2.6, lz - 0.25), (0.5, 1.4, 0.5), "leg")
        m.cube("segment1", (2.8, 2.6, lz - 0.25), (1.6, 0.5, 0.5), "leg")
        m.cube("segment1", (4.1, 2.6, lz - 0.25), (0.5, 1.4, 0.5), "leg")
    for k, (sx, sz, sh) in enumerate(((-1.2, -1.2, 1.6), (0.6, 0.2, 2.2), (-0.4, 1.4, 1.4))):
        m.cube("segment1", (sx - 0.35, -sh, sz - 0.35), (0.7, sh, 0.7), "spine")
    m.cube("segment3", (-0.3, 0.6, 0.5), (0.6, 0.6, 1.8), "sting")
    return m


def _uv(ctx: Ctx) -> tuple[float, float]:
    return (ctx.s + 0.5) / max(ctx.w, 1), (ctx.t + 0.5) / max(ctx.h, 1)


def _lit(ctx: Ctx, key: str, v: float):
    pal = ctx.pal[key]
    col = pal[max(0, min(len(pal) - 1, int(v * len(pal))))]
    ctx.glow = col
    return col


def _plate(ctx: Ctx, base: float):
    x, y, z = ctx.world
    v = base + (noise3(x * 0.9, y * 0.9, z * 0.9, 1201) - 0.5) * 0.18 + (hash01(ctx.u, ctx.v, 1202) - 0.5) * 0.06
    if ctx.face == "top":
        v += 0.12
    elif ctx.face == "bottom":
        v -= 0.15
    return pick(ctx.pal["shell"], v)


def _shell(ctx: Ctx):
    """Panzerplatten mit glimmenden Fugen (quer über den Rücken und an den Rändern)."""
    u, v = _uv(ctx)
    if ctx.face == "bottom":
        return pick(ctx.pal["shell"], 0.12)
    z = ctx.local[2] - ctx.cube.origin[2]
    if ctx.face in ("top", "west", "east") and ctx.cube.size[2] > 2 and abs((z % 1.7) - 0.85) < 0.15:
        return _lit(ctx, "seam", 0.45 + hash01(ctx.u, ctx.v) * 0.3)
    if ctx.face == "top" and abs(u - 0.5) < 0.05:
        return _lit(ctx, "seam", 0.3)                                   # Mittelnaht
    if hash01(ctx.u, ctx.v, 1203) > 0.97:
        return _lit(ctx, "seam", 0.85)                                  # Funken im Kristall
    return _plate(ctx, 0.5)


def _head(ctx: Ctx):
    u, v = _uv(ctx)
    if ctx.face == "front":
        for cu, cv in ((0.25, 0.32), (0.75, 0.32), (0.38, 0.55), (0.62, 0.55)):     # vier Punktaugen
            if abs(u - cu) < 0.1 and abs(v - cv) < 0.14:
                return _lit(ctx, "eye", 0.95 if abs(u - cu) < 0.05 else 0.5)
        return _plate(ctx, 0.4)
    return _shell(ctx)


def _tail(ctx: Ctx):
    return _lit(ctx, "seam", 0.5) if ctx.face == "back" else _plate(ctx, 0.45)


def _feeler(ctx: Ctx):
    f = (ctx.local[2] - ctx.cube.origin[2]) / ctx.cube.size[2]            # 0 vorn (Spitze) … 1 am Kopf
    return _lit(ctx, "seam", 0.6) if f < 0.2 else pick(ctx.pal["leg"], 0.5)


def _mandible(ctx: Ctx):
    return pick(ctx.pal["shell"], 0.25)


def _leg(ctx: Ctx):
    return pick(ctx.pal["leg"], 0.35 + (hash01(ctx.u, ctx.v) - 0.5) * 0.1 + (0.15 if ctx.face == "top" else 0.0))


def _spine(ctx: Ctx):
    """Kristalldorn: läuft nach oben spitz zu."""
    f = (ctx.local[1] - ctx.cube.origin[1]) / ctx.cube.size[1]            # 0 Spitze … 1 Ansatz
    if ctx.face == "top":
        return None
    if ctx.face != "bottom":
        u, _ = _uv(ctx)
        if abs(u - 0.5) * 2.0 > 0.35 + f * 0.8:
            return None
    return _lit(ctx, "crystal", 0.92 - f * 0.45)


def _sting(ctx: Ctx):
    return _lit(ctx, "crystal", 0.8 if ctx.face != "back" else 0.98)


def materials(variant: str) -> dict:
    return {"head": _head, "shell": _shell, "tail": _tail, "feeler": _feeler, "mandible": _mandible, "leg": _leg,
            "spine": _spine, "sting": _sting}


def hidden(variant: str) -> list[str]:
    return []


__all__ = ["PALETTES", "build", "materials", "hidden", "mix"]
