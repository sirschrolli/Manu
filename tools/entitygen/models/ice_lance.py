"""
Eislanze – das Geschoss des Eiszaubers: ein Speer aus Eiskristall, ca. 19 lang entlang Z, die Spitze vorn (-Z), die
Mitte im Ursprung (Geschoss, keine Boden-Regel). Ein einziger Knochen "lance" (Drehpunkt 0, 0, 0), den das Spiel in
die Flugrichtung dreht.

  * Spitze: ein geschliffener Kristall in Stufen – vom 3 × 3 breiten Gürtel über gut 5 Einheiten zur Nadelspitze,
    nach hinten kurz zum Schaft verjüngt
  * Schaft: schlankes Eis (1,6 dick) mit Reifring
  * hinten vier gepfeilte Kristallflossen (wie Befiederung) und ein kleiner Knaufkristall

Bemalung: blasses Cyan-Weiß, dunklere blaue Facetten (Unterseiten, Schattenseiten), helle Kanten, feine Sprünge im
Eis. Durchscheinend (Alpha ca. 190–240, Renderer mit durchscheinendem RenderType), der Kern leuchtet schwach cyan.
Innen liegende Flächen (von anderen Quadern verdeckt) bleiben durchsichtig, damit man im Eis keine doppelten Wände
sieht – siehe ``covered``, das auch das Eisgefängnis nutzt.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hexc, pick

PALETTES = {
    "ice_lance": {
        "ice": [hexc(c) for c in ("#0f2f5c", "#174a86", "#2668aa", "#3c88c8", "#5ea8de", "#86c6ee", "#b0e0f8", "#d8f2fe",
                                  "#f4fcff", "#ffffff")],
        "core": [hexc(c) for c in ("#0a3a5a", "#14607e", "#2a8eaa", "#4cbcd4", "#8ae4f0")],
    },
}

_NORMALS = {"west": (-1.0, 0.0, 0.0), "east": (1.0, 0.0, 0.0), "top": (0.0, -1.0, 0.0), "bottom": (0.0, 1.0, 0.0),
            "front": (0.0, 0.0, -1.0), "back": (0.0, 0.0, 1.0)}
_AXIS = {"west": 0, "east": 0, "top": 1, "bottom": 1, "front": 2, "back": 2}


# =============================================================================================
# Hilfen (auch für das Eisgefängnis)
# =============================================================================================

def covered(ctx: Ctx, group=None) -> bool:
    """True, wenn diese Stelle einer Fläche im Inneren liegt: knapp außerhalb der Fläche beginnt ein anderer Quader
    desselben Knochens (bei ``group``: nur Quader, für die group(cube) gleich ist). Liegen zwei Flächen gleicher
    Richtung in einer Ebene übereinander, malt nur der zuerst angelegte Quader (sonst flimmert es)."""
    c = ctx.cube
    face = ctx.lface or ctx.face
    n = _NORMALS[face]
    ax = _AXIS[face]
    p = ctx.local
    eps = 0.03
    q = (p[0] + n[0] * eps, p[1] + n[1] * eps, p[2] + n[2] * eps)
    key = group(c) if group else None
    cubes = c.bone.cubes
    my_index = cubes.index(c)
    for i, o in enumerate(cubes):
        if o is c or (group and group(o) != key):
            continue
        lo = o.origin
        hi = (lo[0] + o.size[0], lo[1] + o.size[1], lo[2] + o.size[2])
        if all(lo[k] + 1e-4 < q[k] < hi[k] - 1e-4 for k in range(3)):
            return True
        if i < my_index:
            # gleiche Ebene, gleiche Richtung: nur der frühere Quader malt
            plane = hi[ax] if n[ax] > 0 else lo[ax]
            if abs(p[ax] - plane) < 1e-3 and all(lo[k] - 1e-4 <= p[k] <= hi[k] + 1e-4 for k in range(3) if k != ax):
                return True
    return False


def with_alpha(col, alpha: float):
    return col[:3] + (max(0, min(255, round(alpha))),)


def face_uv(ctx: Ctx) -> tuple[float, float]:
    """Lage des Texels in seiner Fläche: (0..1 waagrecht, 0..1 senkrecht) bezogen auf die Pixelmitte."""
    return (ctx.s + 0.5) / ctx.w, (ctx.t + 0.5) / ctx.h


_LIGHT = {"top": 0.6, "front": 0.56, "west": 0.48, "east": 0.42, "back": 0.38, "bottom": 0.28}


def ice_shade(ctx: Ctx, base: float = 0.0, rim: float = 0.3, crack_seed: int = 3, crack: float = 0.14,
              alpha: float = 200.0, glow_core: float = 0.0, cell: float = 3.2):
    """Eis mit Facetten: Helligkeit je Flächenrichtung (Licht von oben), helle Kante oben/links an jeder Fläche (bei
    sehr schmalen Flächen nur oben), etwas dunklere Kante unten/rechts, eine schräge Facettenteilung und feine helle
    Sprünge. -> (Helligkeit, Alpha, Leuchten 0..1)."""
    face = ctx.lface or ctx.face
    v = _LIGHT[face] + base
    a = alpha
    g = glow_core
    u, w = face_uv(ctx)
    lit_edge = (ctx.t == 0 and ctx.h >= 3) or (ctx.s == 0 and ctx.w >= 3)
    dark_edge = (ctx.t == ctx.h - 1 and ctx.h >= 5) or (ctx.s == ctx.w - 1 and ctx.w >= 5)
    if lit_edge:
        v += rim
        a += 35
    elif dark_edge:
        v -= 0.08
        a += 20
    else:
        if u + w * 0.7 > 1.05:
            v -= 0.06                                                    # schräge Facettenteilung
        x, y, z = ctx.local
        k = ((x * 0.7 + y * 1.1 + z * 0.9) / cell + crack_seed * 0.37) % 1.0
        if k < crack * 0.5:
            v += 0.16                                                    # heller Sprung im Eis
            a += 25
            g = max(g, 0.45)
    return v, a, g


def ice_texel(ctx: Ctx, v: float, a: float, g: float, key: str = "ice", glow_key: str = "core"):
    """Farbe mit Alpha; ``g`` (0..1) legt das schwache Leuchten fest (0 = keins)."""
    col = pick(ctx.pal[key], v)
    if g > 0.05:
        ctx.glow = pick(ctx.pal[glow_key], g)
    return with_alpha(col, a)


# =============================================================================================
# Geometrie
# =============================================================================================

# Stufen der Spitze: (halbe Breite, z vorn, z hinten) – viele flache Stufen ergeben eine fein facettierte Spitze
HEAD = [(0.25, -9.9, -9.0), (0.45, -9.1, -8.2), (0.65, -8.3, -7.4), (0.85, -7.5, -6.6), (1.05, -6.7, -5.8),
        (1.25, -5.9, -5.0), (1.5, -5.1, -3.9), (1.2, -4.0, -3.1), (0.95, -3.2, -2.3)]
FIN_Z = (3.2, 6.6)          # Flossen: von z bis z
FIN_H = 2.3                 # Höhe über dem Schaft
SHAFT = 0.8                 # halbe Dicke des Schafts


def build() -> Model:
    m = Model("ice_lance", 64, 32)
    m.tex_res = 2
    m.bone("lance", pivot=(0, 0, 0))
    for h, z0, z1 in HEAD:
        m.cube("lance", (-h, -h, z0), (2 * h, 2 * h, z1 - z0), "head")
    m.cube("lance", (-SHAFT, -SHAFT, -2.4), (2 * SHAFT, 2 * SHAFT, 9.2), "shaft")
    m.cube("lance", (-1.1, -1.1, 1.6), (2.2, 2.2, 0.9), "ring")                 # Reifring
    z0, z1 = FIN_Z
    d = z1 - z0
    r0 = SHAFT - 0.1
    m.cube("lance", (-0.3, -r0 - FIN_H, z0), (0.6, FIN_H, d), "fin")            # oben
    m.cube("lance", (-0.3, r0, z0), (0.6, FIN_H, d), "fin")                     # unten
    m.cube("lance", (r0, -0.3, z0), (FIN_H, 0.6, d), "fin")                     # links
    m.cube("lance", (-r0 - FIN_H, -0.3, z0), (FIN_H, 0.6, d), "fin")           # rechts
    m.cube("lance", (-1.0, -1.0, 6.6), (2.0, 2.0, 1.2), "pommel")               # Knauf
    m.cube("lance", (-0.6, -0.6, 7.6), (1.2, 1.2, 1.0), "pommel")
    m.cube("lance", (-0.3, -0.3, 8.4), (0.6, 0.6, 0.9), "pommel")
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

def _head(ctx: Ctx):
    """Kristallspitze: die nach vorn zeigenden Stufenflächen glänzen wie Facetten, die Seiten werden zur Spitze hin
    heller, der Kern leuchtet."""
    x, y, z = ctx.local
    tip = max(0.0, min(1.0, (-z - 3.0) / 6.9))                          # 0 Gürtel … 1 Spitze
    face = ctx.lface
    if face in ("front", "back"):
        # Stufenflächen ganz bemalt (auch hinter der nächsten Stufe: eine Facettenebene im Eis, kein Loch);
        # nur Stirnflächen, die ganz in einer anderen Stufe stecken, bleiben leer
        if face == "back" and covered(ctx) and ctx.cube.size[0] < 2.0 * HEAD[-1][0] + 0.01:
            return None
        if face == "back" and covered(ctx) and z < -2.95:
            return None
        ring = max(abs(x), abs(y)) / max(ctx.cube.size[0] / 2, 1e-3)
        if face == "front":
            return ice_texel(ctx, 0.95 if ring > 0.7 else 0.66, 236 if ring > 0.7 else 170, 0.4 + 0.4 * tip)
        return ice_texel(ctx, 0.62 if ring > 0.7 else 0.5, 225 if ring > 0.7 else 170, 0.3)
    if covered(ctx):
        return None
    v, a, g = ice_shade(ctx, base=0.04 + 0.14 * tip, crack_seed=5)
    u, w = face_uv(ctx)
    if face != "back" and abs(u - 0.5) < 0.26 and abs(w - 0.5) < 0.26:
        g = max(g, 0.25 + 0.35 * tip)                                   # durchscheinender Kern
    return ice_texel(ctx, v, a, g)


def _shaft(ctx: Ctx):
    """Schaft: gedrehte Reifspirale, sonst klares Eis; der Kern leuchtet schwach."""
    if covered(ctx):
        return None
    x, y, z = ctx.local
    v, a, g = ice_shade(ctx, base=-0.04, crack=0.0)
    face = ctx.lface
    if face in ("top", "bottom", "west", "east"):
        per = {"top": 0.0, "east": 0.25, "bottom": 0.5, "west": 0.75}[face]
        u, w = face_uv(ctx)
        across = u if face in ("top", "bottom") else 1.0 - w
        spiral = (z / 2.6 + per + across * 0.25) % 1.0
        if spiral < 0.2:
            v += 0.2                                                     # Reifspirale
            a += 30
        elif 0.4 < spiral < 0.6:
            g = max(g, 0.3)
    return ice_texel(ctx, v, a, g)


def _ring(ctx: Ctx):
    if covered(ctx):
        return None
    v, a, g = ice_shade(ctx, base=0.12, crack=0.0, alpha=225)
    return ice_texel(ctx, v, a, max(g, 0.2))


def _fin(ctx: Ctx):
    """Kristallflosse wie ein Drachen (Raute): vorn flach ansteigend, bei drei Vierteln am höchsten, nach hinten
    wieder kürzer; heller Saum an den schrägen Kanten."""
    if covered(ctx):
        return None
    c = ctx.cube
    x, y, z = ctx.local
    ox, oy, oz = c.origin
    w, h, d = c.size
    # radiale Lage (Abstand vom Schaft) und Längsachse der Flosse
    if w > h:                                                            # waagrechte Flosse (links/rechts)
        r = (x - ox) if ox > 0 else (ox + w - x)
        flat_faces, edge_face = ("top", "bottom"), ("east" if ox > 0 else "west")
    else:
        r = (y - oy) if oy > 0 else (oy + h - y)
        flat_faces, edge_face = ("west", "east"), ("bottom" if oy > 0 else "top")
    r -= 0.1
    k = (z - oz) / d                                                     # 0 vorn … 1 hinten
    peak = 0.72
    if k < peak:
        r_max = FIN_H * (k / peak) ** 1.1
    else:
        r_max = FIN_H * (1.0 - 0.62 * (k - peak) / (1.0 - peak))
    face = ctx.lface
    if face in ("front", edge_face) or r > r_max + 0.05:
        return None
    v, a, g = ice_shade(ctx, base=0.06, crack=0.0)
    if face in flat_faces:
        if r > r_max - 0.42:
            v, a, g = 0.9, 235, 0.55                                     # Saum an den schrägen Kanten
        elif r < 0.45:
            v -= 0.06
    elif face == "back":
        v, a = 0.62, 220
    return ice_texel(ctx, v, a, g)


def _pommel(ctx: Ctx):
    if covered(ctx):
        return None
    x, y, z = ctx.local
    v, a, g = ice_shade(ctx, base=0.06, crack=0.0)
    if ctx.lface == "back" and ctx.cube.size[0] < 1.0:
        v, a, g = 0.95, 240, 0.8                                         # leuchtender Knauf-Splitter
    return ice_texel(ctx, v, a, max(g, 0.25))


def materials(variant: str) -> dict:
    return {"head": _head, "shaft": _shaft, "ring": _ring, "fin": _fin, "pommel": _pommel}
