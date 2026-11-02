"""
Glutschleim (statt Magmawürfel) auf dem Skelett des Vanilla-Magmawürfels: acht Scheiben (cube0 … cube7), die
MagmaCubeModel beim Springen auseinanderzieht, und ein Kern (inside_cube). Eigenes Aussehen:

  * außen eine Kruste aus erkalteten Schollen mit glühenden Fugen dazwischen; hier und da steht eine Scholle vor
  * zwischen den Scheiben ist er flüssig: springt er, leuchten die Schichten dazwischen hell auf
  * vorn ein Gesicht aus Glut: zwei gleißende Augen unter einer Krustenbraue und ein breites, glühendes Maul
  * innen ein weißglühendes Herz, unten tropft Lava

Zwei Gestalten: Glutschleim (schwarze Kruste, orange Glut) und Basaltschleim (graue Basaltkruste, tiefrote Glut, in
den Basaltdeltas).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, hexc, noise3

PALETTES = {
    "glut": {
        "crust": [hexc(c) for c in ("#120c0a", "#1d1410", "#2a1d16", "#3a281d", "#4d3525", "#62432d")],
        "fire": [hexc(c) for c in ("#5c1404", "#9a2a07", "#d4490f", "#f8761d", "#ffa936", "#ffd970", "#fff3c4")]},
    "basalt": {
        "crust": [hexc(c) for c in ("#1b1b1e", "#28282c", "#37373c", "#47474d", "#5a5a61", "#6e6e75")],
        "fire": [hexc(c) for c in ("#3f0606", "#6e0d0a", "#a3170f", "#d42b16", "#f2522a", "#ff8a4a", "#ffc79a")]},
}

_PLATES = Voronoi3D(2.3, 41)


def build() -> Model:
    m = Model("magma_cube", 128, 64)
    m.tex_res = 2
    for i in range(8):
        name = f"cube{i}"
        m.bone(name, pivot=(0, 0, 0))
        m.cube(name, (-4, 16 + i, -4), (8, 1, 8), "slice")
    # ein paar vorstehende Schollen (sie gehören zu ihrer Scheibe und springen mit)
    m.cube("cube1", (-4.5, 17, -2), (0.5, 1, 3), "nub")
    m.cube("cube3", (4, 19, 0), (0.5, 1, 3), "nub")
    m.cube("cube6", (-1, 22, 4), (3, 1, 0.5), "nub")
    m.cube("cube0", (-2.5, 15.5, -2), (4, 0.5, 3), "nub")
    m.cube("cube7", (-1.5, 24, -1.5), (1, 1.5, 1), "drip")
    m.cube("cube7", (2, 24, 1), (1, 1, 1), "drip")
    m.bone("inside_cube", pivot=(0, 0, 0))
    m.cube("inside_cube", (-2, 18, -2), (4, 4, 4), "heart")
    return m


def _fire(ctx: Ctx, k: float):
    f = ctx.pal["fire"]
    k = max(0.0, min(0.999, k))
    return f[int(k * len(f))]


def _crust(ctx: Ctx, k: float):
    c = ctx.pal["crust"]
    k = max(0.0, min(0.999, k))
    return c[int(k * len(c))]


def _glow(ctx: Ctx, c):
    ctx.glow = c
    return c


def _face(ctx: Ctx, s: int, t: int):
    """Gesicht auf der Vorderseite (16 × 16 Pixel über alle acht Scheiben): Augen, Braue, Maul; None: kein Gesicht."""
    for left in (2, 10):
        es, et = s - left, t - 5
        if 0 <= es < 4 and 0 <= et < 3:
            if (es, et) in ((0, 2), (3, 2)):
                return _crust(ctx, 0.05)                                  # zornige Ecken
            return _glow(ctx, _fire(ctx, 0.97 if et == 1 and es in (1, 2) else 0.82))
        if 0 <= es < 4 and et == -1:
            return _crust(ctx, 0.78)                                      # Krustenbraue
        if 0 <= es < 4 and et == -2:
            return _crust(ctx, 0.05)
    # breites Maul mit glühendem Schlund und Zähnen aus Kruste
    if 4 <= s <= 11 and 10 <= t <= 12:
        if t == 10 and s in (4, 11) or t == 12 and s in (4, 5, 10, 11):
            return _crust(ctx, 0.1)
        if t == 10 and s in (6, 9):
            return _crust(ctx, 0.6)                                       # Zähne
        return _glow(ctx, _fire(ctx, 0.72 + (0.18 if t == 11 else 0.0)))
    return None


def _slice(ctx: Ctx):
    """Kruste außen (Schollen mit glühenden Fugen), flüssige Glut auf den inneren Schnittflächen."""
    x, y, z = ctx.world
    i = int(ctx.cube.bone.name.removeprefix("cube"))
    face = ctx.face
    inner = face == "top" and i > 0 or face == "bottom" and i < 7
    if inner:
        # die Schicht zwischen zwei Scheiben: flüssig, in der Mitte am hellsten
        r = max(abs(x), abs(z)) / 4.0
        return _glow(ctx, _fire(ctx, 0.95 - r * 0.55 + (noise3(x, i, z, 3) - 0.5) * 0.2))
    if face == "bottom":
        return _glow(ctx, _fire(ctx, 0.55 + noise3(x * 0.8, 0, z * 0.8, 4) * 0.3))   # Unterseite glüht
    if face == "top":
        # Deckel: Schollen mit glühenden Fugen
        cell, edge = _PLATES(x, 0.0, z)
        if edge < 0.2:
            return _glow(ctx, _fire(ctx, 0.35 + (1.0 - edge / 0.2) * 0.4))
        return _crust(ctx, 0.45 + cell * 0.35)
    # Seiten: jede Scheibe ist eine Lage Schollen, versetzt wie Mauerwerk, mit glühenden Fugen
    u = ctx.s                                                             # 0..15 entlang der Seite
    row = ctx.t                                                           # 0 oben, 1 unten in der Scheibe
    full_t = i * 2 + row
    if face == "front":
        f = _face(ctx, u, full_t)
        if f is not None:
            return f
        if 1 <= u <= 14 and 3 <= full_t <= 13:
            return _crust(ctx, 0.35 + noise3(u * 0.5, full_t * 0.5, 0, 2) * 0.25)   # ruhige Fläche ums Gesicht
    seams = [round(hash01(i, face, k, "fuge") * 15) for k in range(2)]
    if u in seams or (u + 1) % 16 in seams and hash01(i, face, u, "breit") < 0.3:
        return _glow(ctx, _fire(ctx, 0.45 + hash01(i, u, face) * 0.35))
    if row == 1 and i % 2 == 0 and hash01(i, face, u // 3, "lage") < 0.45:
        return _glow(ctx, _fire(ctx, 0.3))                                # Fuge zur nächsten Lage
    k = 0.35 + noise3(x * 0.9, y * 0.9, z * 0.9, 6) * 0.35 + (0.12 if row == 0 else 0.0) - i * 0.02
    if hash01(round(x * 2), round(y * 2), round(z * 2), "pore") < 0.05:
        k -= 0.2
    return _crust(ctx, k)


def _nub(ctx: Ctx):
    x, y, z = ctx.world
    if ctx.face == "bottom":
        return _glow(ctx, _fire(ctx, 0.4))
    return _crust(ctx, 0.55 + noise3(x * 2, y * 2, z * 2, 8) * 0.3)


def _drip(ctx: Ctx):
    """Lavatropfen unter dem Würfel."""
    x, y, z = ctx.world
    if ctx.face == "top":
        return _glow(ctx, _fire(ctx, 0.7))
    return _glow(ctx, _fire(ctx, 0.85 - (y - 24.0) * 0.3))


def _heart(ctx: Ctx):
    """Weißglühendes Herz mit dunkleren Adern."""
    x, y, z = ctx.local
    v = noise3(x * 1.4, y * 1.4, z * 1.4, 21)
    if 0.48 < v < 0.53:
        return _glow(ctx, _fire(ctx, 0.6))
    return _glow(ctx, _fire(ctx, 0.99 - abs(v - 0.5) * 0.4))


def materials(variant: str) -> dict:
    return {"slice": _slice, "nub": _nub, "drip": _drip, "heart": _heart}


def hidden(variant: str) -> list[str]:
    return []
