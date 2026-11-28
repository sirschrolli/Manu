"""
Gemeinsame Bausteine der Belagerungswaffen (Katapult, Tribok, Balliste, Rammbock und ihre Geschosse): Kriegsgerät
aus warmem Eichenholz mit dunklen Eisenbeschlägen, gedrehten Seilen, vernähtem Leder und rauem Stein.

Bauen:
  * ``box`` setzt einen Quader zwischen zwei Ecken in Modell-Koordinaten (Y nach unten, y = 24 ist der Boden),
    ``lbox`` dasselbe in Knochen-Koordinaten (für schräg gestellte Hilfsknochen wie Streben und Dachflächen)
  * ``wheel`` baut ein Vollholzrad aus gestuften Scheiben (Pixelkreis, nichts überlappt, kein Flimmern)
  * ``band`` legt einen Eisenring um einen Balken, ``boulder`` einen Feldstein aus drei sich kreuzenden Quadern

Malen: Alle Muster rechnen im Raum des Knochens und relativ zum Quader (nicht nach Texel-Lage), daher
  * läuft die Maserung immer entlang der langen Achse eines Balkens und setzt sich über die Kanten fort,
  * zeigen Schnittflächen dunkleres Hirnholz mit Jahresringen, Mark und Trockenriss,
  * sehen Quader, die sich in einer Ebene überdecken (Räder, Steine, Unterlegscheiben), dort gleich aus.
Wenige Tonstufen je Material, ruhige Flächen, Kanten leicht dunkler, Licht von oben – wie bei Vanilla.
"""
from __future__ import annotations

import math

from ..geometry import Cube, Model
from ..paint import Ctx, Voronoi3D, hash01, mix, noise3, pick, ramp

PALETTE = {
    "oak": ramp("#4a2f18", "#dcae70", steps=9, hue_shift=-0.03),
    "end": ramp("#2a1a0d", "#a87c4a", steps=8, hue_shift=-0.03),
    "iron": ramp("#121316", "#b4b8bd", steps=9, hue_shift=0.02),
    "rust": ramp("#2e150a", "#a65c2c", steps=6, hue_shift=-0.02),
    "rope": ramp("#3a2610", "#e6c47e", steps=8, hue_shift=-0.03),
    "leather": ramp("#26150a", "#b07a48", steps=9, hue_shift=-0.03),
    "hide": ramp("#2a190e", "#c39566", steps=9, hue_shift=-0.03),
    "dye": ramp("#3a0e0a", "#b84e32", steps=6),
    "linen": ramp("#6e6250", "#efe6cc", steps=5),
    "stone": ramp("#34322e", "#d2ccbb", steps=9, hue_shift=0.0),
    "moss": ramp("#1e2e14", "#7a9444", steps=5),
    "straw": ramp("#6a5424", "#e8d084", steps=6),
}

FACE_AXIS = {"west": 0, "east": 0, "top": 1, "bottom": 1, "front": 2, "back": 2}
FACE_ID = {"west": 1, "east": 2, "top": 3, "bottom": 4, "front": 5, "back": 6}
HIGH_SIDE = ("east", "bottom", "back")
# Licht von oben (die Spiel-Beleuchtung kommt noch dazu, daher nur leicht)
LIT = {"top": 0.04, "front": 0.0, "back": -0.02, "west": -0.01, "east": 0.0, "bottom": -0.05}
LIT_IRON = {"top": 0.1, "front": 0.03, "back": -0.03, "west": 0.0, "east": -0.01, "bottom": -0.1}


# =============================================================================================
# Bauen
# =============================================================================================

def box(m: Model, bone: str, x0, y0, z0, x1, y1, z1, mat, inflate: float = 0.0, faces=None) -> Cube:
    """Quader zwischen zwei Ecken in Modell-Koordinaten (nur für Knochen ohne gedrehte Vorfahren)."""
    px, py, pz = m.bones[bone].world_pivot()
    xa, xb = sorted((x0, x1))
    ya, yb = sorted((y0, y1))
    za, zb = sorted((z0, z1))
    return m.cube(bone, (xa - px, ya - py, za - pz), (xb - xa, yb - ya, zb - za), mat, inflate, faces=faces)


def lbox(m: Model, bone: str, x0, y0, z0, x1, y1, z1, mat, inflate: float = 0.0, faces=None) -> Cube:
    """Quader zwischen zwei Ecken in Koordinaten des Knochens."""
    xa, xb = sorted((x0, x1))
    ya, yb = sorted((y0, y1))
    za, zb = sorted((z0, z1))
    return m.cube(bone, (xa, ya, za), (xb - xa, yb - ya, zb - za), mat, inflate, faces=faces)


def abs_to_local(m: Model, parent: str, x, y, z) -> tuple[float, float, float]:
    """Drehpunkt für einen neuen Knochen unter ``parent`` aus Modell-Koordinaten (ohne Drehungen)."""
    px, py, pz = m.bones[parent].world_pivot()
    return x - px, y - py, z - pz


def strut(m: Model, name: str, parent: str, a, b, thick: float, mat: str = "beam", over: float = 0.0) -> None:
    """
    Schräger Balken von Punkt ``a`` nach ``b`` (Modell-Koordinaten, beide in einer Ebene x = konstant oder
    z = konstant) als eigener, fest gedrehter Hilfsknochen. ``over`` verlängert ihn an beiden Enden (Zapfen).
    """
    ax, ay, az = a
    bx, by, bz = b
    dx, dy, dz = bx - ax, by - ay, bz - az
    length = math.sqrt(dx * dx + dy * dy + dz * dz)
    pivot = abs_to_local(m, parent, ax, ay, az)
    h = thick / 2
    if abs(dx) < 1e-6:
        # Drehung um X: lokale +Y-Achse (0, L, 0) -> (0, L cos, L sin)
        rot = (math.degrees(math.atan2(dz, dy)), 0.0, 0.0)
    elif abs(dz) < 1e-6:
        # Drehung um Z: (0, L, 0) -> (-L sin, L cos, 0)
        rot = (0.0, 0.0, math.degrees(math.atan2(-dx, dy)))
    else:
        raise ValueError("Strebe muss in einer Ebene x = konst. oder z = konst. liegen")
    m.bone(name, parent, pivot=pivot, rotation=rot)
    m.cube(name, (-h, -over, -h), (thick, length + 2 * over, thick), mat)


# Pixelkreise (halbe Breite je Reihe, von der Mitte nach außen) – in Zeilen und Spalten gleich
DISC = {12: (6, 6, 5, 5, 4, 2), 10: (5, 5, 4, 4, 2), 8: (4, 4, 3, 2), 6: (3, 3, 2)}


def disc_inside(d: int, a: float, b: float) -> bool:
    hw = DISC[d]
    k = int(math.floor(abs(a)))
    return k < len(hw) and abs(b) < hw[k]


def wheel(m: Model, bone: str, d: int, x0: float, x1: float, mat: str) -> None:
    """Vollrad mit Durchmesser ``d`` um die X-Achse des Knochens (Mitte = Drehpunkt), Dicke von x0 bis x1."""
    hw = DISC[d]
    rows = []
    start = 0
    for k in range(1, len(hw) + 1):
        if k == len(hw) or hw[k] != hw[start]:
            rows.append((start, k, hw[start]))
            start = k
    for a, b, w in rows:
        if a == 0:
            lbox(m, bone, x0, -b, -w, x1, b, w, mat)
        else:
            lbox(m, bone, x0, -b, -w, x1, -a, w, mat)
            lbox(m, bone, x0, a, -w, x1, b, w, mat)


def hub(m: Model, bone: str, out: int, x0: float, thick: float = 1.0, cap: float = 0.6) -> None:
    """Runde Nabe (Pixelkreis Ø4 aus zwei gekreuzten Quadern) außen am Rad ab x0, dazu eine Achskappe (Seite ``out``)."""
    xa, xb = x0 * out, (x0 + thick) * out
    lbox(m, bone, xa, -1, -2, xb, 1, 2, "hub")
    lbox(m, bone, xa, -2, -1, xb, 2, 1, "hub")
    if cap:
        lbox(m, bone, xb, -1, -1, (x0 + thick + cap) * out, 1, 1, "cap")


def band(m: Model, bone: str, axis: int, at: float, lo, hi, width: float = 1.0, grow: float = 0.3,
         mat: str = "band") -> Cube:
    """Eisenband um einen Balken: ``axis`` = Längsachse des Balkens, ``at`` = Lage darauf, lo/hi = Ecken des Querschnitts
    (Modell-Koordinaten, je 3 Werte; die Komponente der Längsachse wird ignoriert)."""
    a = [lo[i] - grow for i in range(3)]
    b = [hi[i] + grow for i in range(3)]
    a[axis] = at - width / 2
    b[axis] = at + width / 2
    return box(m, bone, a[0], a[1], a[2], b[0], b[1], b[2], mat)


BOLT_TIP, BOLT_END = -13.5, 13.0     # Ballistenbolzen: Spitze (-Z) und Ende (+Z) relativ zur Mitte


def bolt(m: Model, bone: str) -> None:
    """
    Schwerer Ballistenbolzen um den Drehpunkt von ``bone`` (Mitte), Spitze nach -Z: vierkantige Spitze in vier Stufen,
    eiserne Tülle, Schaft 1,5 × 1,5, hinten drei Leitflossen aus gefärbtem Leder im Abstand von 120° (eine nach unten,
    zwei schräg nach oben – liegt so mit der unteren Flosse im Schlitz der Gleitbahn) und eine Endkappe.
    Die Flossen sind fest gedrehte Hilfsknochen ``<bone>_fin_a/b/c``.
    """
    lbox(m, bone, -0.25, -0.25, BOLT_TIP, 0.25, 0.25, -12.5, "head")
    lbox(m, bone, -0.5, -0.5, -12.5, 0.5, 0.5, -11, "head")
    lbox(m, bone, -0.9, -0.9, -11, 0.9, 0.9, -9, "head")
    lbox(m, bone, -1.25, -1.25, -9, 1.25, 1.25, -7.5, "head")
    lbox(m, bone, -1, -1, -7.5, 1, 1, -5.5, "socket")
    lbox(m, bone, -0.75, -0.75, -5.5, 0.75, 0.75, 12, "shaft")
    lbox(m, bone, -0.9, -0.9, 12, 0.9, 0.9, BOLT_END, "socket")
    for name, ang in (("a", 0.0), ("b", 120.0), ("c", -120.0)):
        fin = f"{bone}_fin_{name}"
        m.bone(fin, bone, pivot=(0, 0, 0), rotation=(0, 0, ang))
        lbox(m, fin, -0.25, 0.7, 5.5, 0.25, 2.9, 11.5, "fletch")


def boulder(m: Model, bone: str, cx, cy, cz, sx, sy, sz, mat: str = "stone", local: bool = True) -> None:
    """Feldstein: drei sich kreuzende Quader (abgeschrägte Kanten) um die Mitte, Maße sx/sy/sz."""
    f = lbox if local else box
    f(m, bone, cx - sx / 2, cy - sy / 2 + 1, cz - sz / 2 + 1, cx + sx / 2, cy + sy / 2 - 1, cz + sz / 2 - 1, mat)
    f(m, bone, cx - sx / 2 + 1, cy - sy / 2, cz - sz / 2 + 1, cx + sx / 2 - 1, cy + sy / 2, cz + sz / 2 - 1, mat)
    f(m, bone, cx - sx / 2 + 1, cy - sy / 2 + 1, cz - sz / 2, cx + sx / 2 - 1, cy + sy / 2 - 1, cz + sz / 2, mat)


# =============================================================================================
# Hilfen fürs Malen
# =============================================================================================

_CID: dict[int, int] = {}


def cid(cube: Cube) -> int:
    """Fester Zufallswert je Quader (jeder Balken ein anderer Stamm)."""
    key = id(cube)
    v = _CID.get(key)
    if v is None:
        o, s = cube.origin, cube.size
        v = int(hash01(cube.bone.name, *(round(t * 4) for t in o + s)) * 65535)
        _CID[key] = v
    return v


def rel(ctx: Ctx) -> tuple[float, float, float]:
    o = ctx.cube.origin
    p = ctx.local
    return p[0] - o[0], p[1] - o[1], p[2] - o[2]


def long_axis(cube: Cube, prefer: int | None = None) -> int:
    if prefer is not None:
        return prefer
    s = cube.size
    return max(range(3), key=lambda i: (s[i], -i))


def perimeter(r, size, axis: int, face: str) -> float:
    """Lage rund um einen Stab (für Seile und Wicklungen, über die Kanten durchgehend)."""
    ua, va = [i for i in range(3) if i != axis]
    fa = FACE_AXIS[face]
    hi = face in HIGH_SIDE
    w, h = size[ua], size[va]
    u, v = r[ua], r[va]
    if fa == va and not hi:
        return u
    if fa == ua and hi:
        return w + v
    if fa == va and hi:
        return w + h + (w - u)
    return 2 * w + h + (h - v)


def face_edge(ctx: Ctx) -> tuple[float, float, float, float]:
    """Abstände des Texels zu den vier Rändern seiner Fläche (u-unten, u-oben, v-unten, v-oben) und Flächenmaße."""
    r = rel(ctx)
    size = ctx.cube.size
    fa = FACE_AXIS[ctx.face]
    ua, va = [i for i in range(3) if i != fa]
    return r[ua], size[ua] - r[ua], r[va], size[va] - r[va]


# =============================================================================================
# Holz
# =============================================================================================

def wood_value(ctx: Ctx, axis: int, base: float = 0.56, knots: bool = True, edges: bool = True,
               seed: int | None = None) -> float:
    """
    Tonwert einer Längsfläche, Maserung entlang ``axis`` wie bei Vanilla-Holz: jede Faserreihe (eine Texelzeile entlang
    des Balkens) hat ihre eigene Folge kurzer, eine Stufe dunklerer oder hellerer Läufe; dazu einzelne lange dunkle
    Spätholz-Linien, kaum sichtbare weiche Wolken und selten ein Ast. Oberkante senkrechter Flächen hell, die übrigen
    Kanten eine halbe Stufe dunkler (abgegriffen).
    """
    r = rel(ctx)
    size = ctx.cube.size
    fa = FACE_AXIS[ctx.face]
    ca = 3 - axis - fa
    p = ctx.local
    c = cid(ctx.cube) if seed is None else seed
    a = p[axis]
    v = base + ((c % 97) / 96 - 0.5) * 0.05                         # jeder Balken ein anderer Stamm
    q = [p[0] * 0.25, p[1] * 0.25, p[2] * 0.25]
    q[axis] = a * 0.045
    v += (noise3(q[0], q[1], q[2], c) - 0.5) * 0.09                # weiche Wolken
    row = math.floor(p[ca] + 1e-4)
    plane = round(p[fa] * 2)
    off = hash01(row, plane, c, 3) * 64.0
    n = noise3(a * 0.3 + off, row * 0.23, plane * 0.31, c + 1)
    if n < 0.33:
        v -= 0.09                                                   # dunklerer Lauf
    elif n > 0.73:
        v += 0.07                                                   # hellerer Lauf
    f = noise3(a * 0.055 + off * 0.1, row * 1.37, plane * 1.13, c + 2)
    if f > 0.71:
        v -= 0.15                                                   # lange Spätholz-Linie
    if knots and size[axis] >= 10 and size[ca] >= 2.5:
        v += _knot(ctx, r, size, axis, ca, c)
    if edges:
        lo, hi = r[ca] < 1.0, r[ca] > size[ca] - 1.0
        if lo or hi:
            if ca == 1 and lo and fa != 1:
                v += 0.05
            else:
                v -= 0.06
        if r[axis] < 0.8 or r[axis] > size[axis] - 0.8:
            v -= 0.04
    return v + LIT[ctx.face]


def _knot(ctx: Ctx, r, size, axis: int, ca: int, c: int) -> float:
    """Kleiner Ast: dunkler Kern (2 × 1 Texel), die Fasern davor und dahinter etwas dunkler (umfließend)."""
    slot = 11.0
    a = r[axis]
    k = math.floor(a / slot)
    fid = FACE_ID[ctx.face]
    out = 0.0
    for kk in (k - 1, k, k + 1):
        if kk < 0 or hash01(c, kk, fid, 5) < 0.66:
            continue
        ka = (kk + 0.25 + 0.5 * hash01(c, kk, 6)) * slot
        if ka < 2.5 or ka > size[axis] - 2.5:
            continue
        kc = math.floor(1.0 + (size[ca] - 2.0) * hash01(c, kk, fid, 7)) + 0.5
        da = a - ka
        dc = r[ca] - kc
        if abs(dc) < 0.5 and abs(da) < 1.0:
            out = min(out, -0.27)
        elif abs(dc) < 0.5 and abs(da) < 2.6:
            out = min(out, -0.1)
        elif abs(dc) < 1.5 and abs(da) < 1.6:
            out = min(out, -0.05)
    return out


def end_value(ctx: Ctx, axis: int, base: float = 0.5) -> float:
    """Hirnholz: Jahresringe um ein leicht versetztes Mark, dunkle Sägekante, ein Trockenriss."""
    r = rel(ctx)
    size = ctx.cube.size
    ua, va = [i for i in range(3) if i != axis]
    su, sv = size[ua], size[va]
    c = cid(ctx.cube)
    cu = su / 2 + (hash01(c, 11) - 0.5) * su * 0.3
    cv = sv / 2 + (hash01(c, 12) - 0.5) * sv * 0.3
    du, dv = r[ua] - cu, r[va] - cv
    rad = 0.55 * math.hypot(du, dv) + 0.45 * max(abs(du), abs(dv))
    rad += (noise3(r[ua] * 0.8, r[va] * 0.8, c * 0.01, 13) - 0.5) * 0.8
    v = base + ((c % 89) / 88 - 0.5) * 0.06
    if rad < 0.7:
        v -= 0.13
    elif int(rad / 1.2) % 2 == 1:
        v += 0.09
    edge = min(r[ua], su - r[ua], r[va], sv - r[va])
    if edge < 1.0 and min(su, sv) >= 2:
        v -= 0.08
    ang = math.atan2(dv, du)
    crack = hash01(c, 14) * 2 * math.pi
    dang = abs((ang - crack + math.pi) % (2 * math.pi) - math.pi)
    if min(su, sv) >= 3 and 0.9 < rad < max(su, sv) * 0.5 and dang * rad < 0.55:
        v -= 0.17
    return v + LIT[ctx.face] * 0.5


def wood(axis: int | None = None, base: float = 0.56, knots: bool = True):
    """Balken: Längsflächen mit Maserung, Schnittflächen als Hirnholz."""

    def paint(ctx: Ctx):
        ax = long_axis(ctx.cube, axis)
        if FACE_AXIS[ctx.face] == ax:
            return pick(PALETTE["end"], end_value(ctx, ax, base))
        return pick(PALETTE["oak"], wood_value(ctx, ax, base, knots))

    return paint


def planks(axis: int | None = None, width: float = 3.0, base: float = 0.58, nails: bool = True):
    """Bohlen/Bretter: Fasern entlang ``axis`` (sonst längste Achse), Fugen quer dazu alle ``width``, Nägel an den Enden."""

    def paint(ctx: Ctx):
        cube = ctx.cube
        size = cube.size
        r = rel(ctx)
        order = sorted(range(3), key=lambda i: size[i])
        thick = order[0]
        g = axis if axis is not None else order[2]
        wa = [i for i in range(3) if i not in (g, thick)][0] if g != thick else order[1]
        fa = FACE_AXIS[ctx.face]
        n = max(1, round(size[wa] / width))
        pw = size[wa] / n
        k = min(n - 1, int(r[wa] / pw))
        inb = r[wa] - k * pw
        c = cid(cube)
        shift = (hash01(c, k, 21) - 0.5) * 0.12
        if fa == g:
            v = end_value(ctx, g, base + shift)
            if fa != wa and (k < n - 1 and inb > pw - 1.0):
                v -= 0.12
            return pick(PALETTE["end"], v)
        v = wood_value(ctx, g, base + shift, knots=size[g] >= 12, seed=c + k * 7)
        if fa != wa:
            if k < n - 1 and inb > pw - 1.0:
                return pick(PALETTE["oak"], 0.1 + hash01(ctx.u, ctx.v, 22) * 0.06)
            if k > 0 and inb < 1.0:
                v += 0.04
        if nails and fa == thick and size[g] >= 5:
            ends = r[g] < 1.6 or r[g] > size[g] - 1.6
            mid = abs(inb - pw / 2) < 0.55 or (pw >= 4 and abs(inb - pw / 2) < 1.0 and inb < pw / 2)
            if ends and mid and (0.6 < r[g] < 1.6 or size[g] - 1.6 < r[g] < size[g] - 0.6):
                return pick(PALETTE["iron"], 0.6 if ctx.face != "bottom" else 0.4)
        return pick(PALETTE["oak"], v)

    return paint


# =============================================================================================
# Eisen
# =============================================================================================

def iron_value(ctx: Ctx, base: float = 0.42, bevel: bool = True) -> float:
    """Geschmiedetes, geschwärztes Eisen: Oberkante hell, Unterkante dunkel, flache Hammerschläge."""
    r = rel(ctx)
    size = ctx.cube.size
    v = base + LIT_IRON[ctx.face]
    if bevel and ctx.face not in ("top", "bottom") and size[1] >= 1.5:
        if r[1] < 0.75:
            v += 0.1
        elif r[1] > size[1] - 0.75:
            v -= 0.1
    p = ctx.local
    v += (noise3(p[0] * 0.5, p[1] * 0.5, p[2] * 0.5, 41) - 0.5) * 0.1
    return v


def _rust(ctx: Ctx, col, amount: float = 1.0):
    """Ein wenig Rost an Kanten und in Flecken."""
    p = ctx.local
    n = noise3(p[0] * 0.35 + 7, p[1] * 0.35, p[2] * 0.35, 43)
    if n > 0.74 - 0.04 * amount:
        t = min(1.0, (n - 0.7) * 3.0) * 0.55 * amount
        return mix(col, pick(PALETTE["rust"], 0.35 + (hash01(ctx.u, ctx.v, 44) - 0.5) * 0.2), t)
    return col


def rivet(ctx: Ctx, bright: float = 0.8):
    return pick(PALETTE["iron"], bright)


def iron(base: float = 0.42, corner_rivets: bool = True):
    """Eisenteile (Platten, Kappen, Winkel): ruhige Fläche, Niete in den Ecken größerer Flächen."""

    def paint(ctx: Ctx):
        u0, u1, v0, v1 = face_edge(ctx)
        su, sv = u0 + u1, v0 + v1
        if corner_rivets and su >= 3.5 and sv >= 3.5:
            inu = 0.6 < u0 < 1.6 or 0.6 < u1 < 1.6
            inv = 0.6 < v0 < 1.6 or 0.6 < v1 < 1.6
            if inu and inv:
                return rivet(ctx)
        v = iron_value(ctx, base)
        if min(u0, u1, v0, v1) < 0.6 and min(su, sv) >= 2:
            v -= 0.05
        return _rust(ctx, pick(PALETTE["iron"], v), 0.6)

    return paint


def band_paint(spacing: int = 3, base: float = 0.42):
    """Eisenband um einen Balken: schmale Achse = Breite, Niete in der Mitte alle ``spacing`` Texel."""

    def paint(ctx: Ctx):
        cube = ctx.cube
        size = cube.size
        r = rel(ctx)
        ba = min(range(3), key=lambda i: size[i])
        fa = FACE_AXIS[ctx.face]
        if fa == ba:
            return pick(PALETTE["iron"], 0.24 + LIT_IRON[ctx.face] * 0.5)
        la = 3 - ba - fa
        w, length = size[ba], size[la]
        v = base + LIT_IRON[ctx.face]
        if w >= 1.5 and (r[ba] < 0.6 or r[ba] > w - 0.6):
            v -= 0.07
        n = max(1, math.ceil(length - 1e-6))
        idx = min(n - 1, math.floor(r[la] / length * n))
        if abs(r[ba] - w / 2) < 0.55 and 1 <= idx <= n - 2 and (idx - (n - 1) // 2) % spacing == 0:
            return rivet(ctx)
        if idx == 0 or idx == n - 1:
            v -= 0.05
        p = ctx.local
        v += (noise3(p[0] * 0.6, p[1] * 0.6, p[2] * 0.6, 45) - 0.5) * 0.08
        return _rust(ctx, pick(PALETTE["iron"], v), 0.7)

    return paint


def axle_paint(ctx: Ctx):
    """Achsen und Bolzen: dunkles, glattes Rundeisen (Glanzlinie oben)."""
    r = rel(ctx)
    ax = long_axis(ctx.cube)
    fa = FACE_AXIS[ctx.face]
    if fa == ax:
        return pick(PALETTE["iron"], 0.5 if ctx.face != "bottom" else 0.3)
    v = 0.3 + LIT_IRON[ctx.face] * 1.2
    ca = 3 - ax - fa
    if ctx.face not in ("top", "bottom") and ca == 1 and r[1] < 0.6:
        v += 0.12
    return pick(PALETTE["iron"], v + (hash01(ctx.u, ctx.v, 46) - 0.5) * 0.04)


def chain_paint(ctx: Ctx):
    """Kette: Glieder abwechselnd von vorn (Ring mit Loch) und von der Kante (Steg) gesehen."""
    r = rel(ctx)
    ax = long_axis(ctx.cube)
    fa = FACE_AXIS[ctx.face]
    if fa == ax:
        return pick(PALETTE["iron"], 0.35)
    a = r[ax]
    k = math.floor(a / 2.5)
    pos = a - k * 2.5
    ua = [i for i in range(3) if i not in (ax,)][0]
    face_on = (k + (1 if fa == ua else 0)) % 2 == 0
    if face_on:
        if 0.8 < pos < 1.7:
            return pick(PALETTE["iron"], 0.1)
        return pick(PALETTE["iron"], 0.56 + LIT_IRON[ctx.face] - (0.08 if pos < 0.8 else 0.0))
    if pos < 0.5:
        return pick(PALETTE["iron"], 0.2)
    return pick(PALETTE["iron"], 0.44 + LIT_IRON[ctx.face])


# =============================================================================================
# Seil, Leder, Stein
# =============================================================================================

def rope_value(ctx: Ctx, axis: int, period: float = 3.0, base: float = 0.5) -> float:
    """Schräge Litzen: je ``period`` Texel eine dunkle Fuge, danach die helle Kuppe der Litze, dann ihr Schatten."""
    r = rel(ctx)
    size = ctx.cube.size
    per = perimeter(r, size, axis, ctx.face)
    t = (r[axis] + per) / period
    f = t % 1.0
    strand = math.floor(t)
    if f < 1.0 / period:
        v = base - 0.26
    elif f < 2.0 / period:
        v = base + 0.14
    elif f < 3.0 / period:
        v = base + 0.04
    else:
        v = base - 0.06
    v += (hash01(strand, cid(ctx.cube), 52) - 0.5) * 0.06
    return v + LIT[ctx.face]


def rope_paint(period: float = 3.0, base: float = 0.5, axis: int | None = None):
    """Gedrehtes Seil: schräge Litzen (hell in der Mitte, dunkle Fugen), Enden mit Fasern."""

    def paint(ctx: Ctx):
        ax = long_axis(ctx.cube, axis)
        if FACE_AXIS[ctx.face] == ax:
            v = 0.4 + hash01(ctx.u, ctx.v, 51) * 0.16
            u0, u1, v0, v1 = face_edge(ctx)
            if min(u0, u1, v0, v1) < 0.6:
                v -= 0.1
            return pick(PALETTE["rope"], v)
        return pick(PALETTE["rope"], rope_value(ctx, ax, period, base))

    return paint


def skein_paint(ctx: Ctx):
    """
    Dickes Torsionsbündel, durch das Spannen verdrillt: liest sich wie ein sehr dickes Seil – breite schräge Litzen
    (vier Texel je Windung) mit dunklen Fugen, hier und da eine helle lose Faser. Die Enden zeigen die Seilquerschnitte.
    """
    cube = ctx.cube
    ax = long_axis(cube)
    r = rel(ctx)
    if FACE_AXIS[ctx.face] == ax:
        u0, u1, v0, v1 = face_edge(ctx)
        v = 0.42 + hash01(math.floor(r[(ax + 1) % 3]), math.floor(r[(ax + 2) % 3]), 53) * 0.18
        if min(u0, u1, v0, v1) < 0.6:
            v -= 0.08
        return pick(PALETTE["rope"], v)
    v = rope_value(ctx, ax, 4.0, 0.5)
    if hash01(ctx.u, ctx.v, 54) > 0.965:
        v += 0.12
    return pick(PALETTE["rope"], v)


def leather_value(ctx: Ctx, base: float = 0.45, key_seed: int = 61) -> float:
    p = ctx.local
    n = noise3(p[0] * 0.38, p[1] * 0.38, p[2] * 0.38, key_seed)
    v = base + (n - 0.5) * 0.2 + LIT[ctx.face]
    v += (noise3(p[0] * 1.3, p[1] * 1.3, p[2] * 1.3, key_seed + 1) - 0.5) * 0.06
    return v


def leather_paint(base: float = 0.45, stitch: bool = True, key: str = "leather"):
    """Leder: weich gefleckt, Ränder dunkler, eine Naht aus hellem Zwirn einen Texel innen."""

    def paint(ctx: Ctx):
        u0, u1, v0, v1 = face_edge(ctx)
        su, sv = u0 + u1, v0 + v1
        v = leather_value(ctx, base)
        edge = min(u0, u1, v0, v1)
        if edge < 0.6 and min(su, sv) >= 2:
            v -= 0.08
        elif stitch and min(su, sv) >= 4 and 1.0 <= edge < 2.0:
            along = (u0 if min(v0, v1) < min(u0, u1) else v0)
            if int(math.floor(along)) % 2 == 0:
                return pick(PALETTE["linen"], 0.62)
            v -= 0.03
        return pick(PALETTE[key], v)

    return paint


_STONE = Voronoi3D(2.3, 77)
_CRACKS = Voronoi3D(3.6, 91)


def stone_value(ctx: Ctx, base: float = 0.5, seed: int = 0) -> float:
    """
    Rauer Feldstein: Körnung aus kleinen Flecken (zwei Texel), Facetten verschiedener Helligkeit, ein Netz feiner
    dunkler Risse, oben heller und unten dunkler, ein paar dunkle Poren und helle Glimmer – immer paarweise.
    """
    x, y, z = ctx.local
    x += seed * 13.7
    v = base + (noise3(x * 0.6, y * 0.6, z * 0.6, 78 + seed) - 0.5) * 0.3
    v += (noise3(x * 1.2, y * 1.2, z * 1.2, 88 + seed) - 0.5) * 0.12
    val, _ = _STONE(x, y, z)
    v += (val - 0.5) * 0.14
    _, edge = _CRACKS(x, y, z)
    if edge < 0.07:
        v -= 0.24
    elif edge < 0.13:
        v -= 0.07
    v += {"top": 0.09, "bottom": -0.12, "back": -0.03, "east": -0.02}.get(ctx.face, 0.0)
    g = hash01(math.floor(x * 0.5), math.floor(y), math.floor(z * 0.5), 79 + seed)
    if g > 0.94:
        v += 0.09
    elif g < 0.05:
        v -= 0.1
    return v


def stone_paint(base: float = 0.5, seed: int = 0, lichen: bool = False):
    def paint(ctx: Ctx):
        v = stone_value(ctx, base, seed)
        if lichen and ctx.face in ("top", "west", "front"):
            x, y, z = ctx.local
            n = noise3(x * 0.55 + seed, y * 0.55, z * 0.55, 81)
            if n > 0.77:
                return pick(PALETTE["moss"], 0.35 + (n - 0.77) * 2.4)
        return pick(PALETTE["stone"], v)

    return paint


# =============================================================================================
# Räder
# =============================================================================================

def wheel_paint(d: int, seams=(-1.5, 1.5)):
    """
    Vollholzrad (Knochen-Mitte = Radmitte, Achse X). Seiten: außen der eiserne Radreifen (Niete), darin ein Kranz aus
    dunklerem Holz mit Nagelköpfen, innen senkrechte Bohlen mit dunklen Fugen. Die Lauffläche ist ganz Eisen.
    Gemalt wird nur nach Lage, daher passen die gestuften Scheiben nahtlos aneinander.
    """

    def paint(ctx: Ctx):
        x, y, z = ctx.local
        fa = FACE_AXIS[ctx.face]
        if fa != 0:
            return tire_paint(ctx)
        neigh = ((1, 0), (-1, 0), (0, 1), (0, -1))
        ang = math.atan2(y, z)
        if not all(disc_inside(d, y + a, z + b) for a, b in neigh):
            k = int(round((ang + math.pi) / (2 * math.pi) * (d * 2.4)))
            v = 0.38 + LIT_IRON[ctx.face] + (0.1 if y < -d * 0.3 else (-0.07 if y > d * 0.3 else 0.0))
            if k % 3 == 0:
                return rivet(ctx, 0.72)
            return _rust(ctx, pick(PALETTE["iron"], v), 0.8)
        if d >= 8 and not all(disc_inside(d, y + 2 * a, z + 2 * b) for a, b in neigh):
            seg = (ang + math.pi) / (2 * math.pi) * 8 + 0.5
            if seg % 1.0 < 0.14 and d >= 10:
                return pick(PALETTE["oak"], 0.16)                     # Stoß zwischen zwei Felgen
            if d >= 10 and abs(seg % 1.0 - 0.55) < 0.09:
                return pick(PALETTE["iron"], 0.62)                    # Nagelkopf
            v = 0.42 + (hash01(math.floor(seg), d, 31) - 0.5) * 0.08 + (0.05 if y < 0 else -0.03)
            return pick(PALETTE["oak"], v + LIT[ctx.face])
        for s in seams:
            if abs(z - s) < 0.5:
                return pick(PALETTE["oak"], 0.18 + hash01(ctx.u, ctx.v, 33) * 0.05)
        plank = sum(1 for s in seams if z > s)
        v = wood_value(ctx, 1, 0.58 + (hash01(plank, d, 34) - 0.5) * 0.1, knots=False, edges=False,
                       seed=1000 + plank * 17 + d)
        if any(abs(z - (s + 1.0)) < 0.5 for s in seams):
            v += 0.04
        return pick(PALETTE["oak"], v)

    return paint


def tire_paint(ctx: Ctx):
    """Lauffläche: eiserner Radreifen mit einer Reihe Nagelköpfe in der Mitte."""
    r = rel(ctx)
    size = ctx.cube.size
    fa = FACE_AXIS[ctx.face]
    la = 3 - 0 - fa
    w = size[0]
    v = 0.4 + LIT_IRON[ctx.face]
    if r[0] < 0.6 or r[0] > w - 0.6:
        v -= 0.06
    if abs(r[0] - w / 2) < 0.55 and math.floor(ctx.local[la] + 50) % 3 == 1:
        return rivet(ctx, 0.66)
    return _rust(ctx, pick(PALETTE["iron"], v + (hash01(ctx.u, ctx.v, 35) - 0.5) * 0.05), 1.0)


def hub_paint(ctx: Ctx):
    """Runde Radnabe (zwei gekreuzte Quader, nur nach Lage gemalt): Hirnholz mit dunklem Ring, Eisenband rundum."""
    x, y, z = ctx.local
    if ctx.face in ("west", "east"):
        r = math.hypot(y, z)
        if r > 1.6:
            return pick(PALETTE["iron"], 0.44 + (0.08 if y < 0 else -0.04))
        return pick(PALETTE["end"], 0.52 + (0.08 if (y < 0) == (z < 0) else 0.0))
    return pick(PALETTE["iron"], 0.38 + LIT_IRON[ctx.face])


def cap_paint(ctx: Ctx):
    """Achskappe mit Splint."""
    if ctx.face in ("west", "east"):
        u0, u1, v0, v1 = face_edge(ctx)
        if min(u0, u1, v0, v1) < 0.6:
            return pick(PALETTE["iron"], 0.52 if v0 < 1.0 else 0.4)
        return pick(PALETTE["iron"], 0.18)
    return pick(PALETTE["iron"], 0.44 + LIT_IRON[ctx.face])


# =============================================================================================
# Sonstiges
# =============================================================================================

def cushion_paint(ctx: Ctx):
    """Prellpolster: mit Stroh gestopftes Leder, abgesteppt (Knopfheftung), Mitte der Felder gewölbt und hell."""
    u0, u1, v0, v1 = face_edge(ctx)
    su, sv = u0 + u1, v0 + v1
    v = leather_value(ctx, 0.5, 71)
    if min(u0, u1, v0, v1) < 0.6:
        v -= 0.12
    if su >= 4 and sv >= 3:
        fu = (u0 - 0.5) % 2.0
        fv = (v0 - 0.5) % 2.0
        if fu < 0.5 and fv < 0.5 and min(u0, u1, v0, v1) > 0.6:
            return pick(PALETTE["leather"], 0.18)
        if abs(fu - 1.0) < 0.6 and abs(fv - 1.0) < 0.6:
            v += 0.08
    if hash01(ctx.u, ctx.v, 72) > 0.95 and min(u0, u1, v0, v1) < 0.6:
        return pick(PALETTE["straw"], 0.7)
    return pick(PALETTE["leather"], v)


def fletch_paint(ctx: Ctx):
    """Leitflossen aus gefärbtem Leder: rote Mitte, dunkle Kante, heller Streifen."""
    r = rel(ctx)
    size = ctx.cube.size
    fa = FACE_AXIS[ctx.face]
    thin = min(range(3), key=lambda i: size[i])
    if fa != thin:
        return pick(PALETTE["leather"], 0.3)
    ax = 2
    a = r[ax] / max(size[ax], 1e-3)
    others = [i for i in range(3) if i not in (thin, ax)]
    h = r[others[0]] / max(size[others[0]], 1e-3)
    v = 0.5 + (hash01(ctx.u, ctx.v, 73) - 0.5) * 0.08
    if 0.35 < a < 0.55:
        return pick(PALETTE["linen"], 0.55 + (0.1 if h < 0.5 else 0.0))
    if a < 0.12 or a > 0.9:
        return pick(PALETTE["dye"], 0.25)
    return pick(PALETTE["dye"], v)


def head_paint(ctx: Ctx):
    """Geschmiedete Spitze: hell geschliffene Kanten, dunkle Tülle."""
    v = 0.5 + LIT_IRON[ctx.face]
    if ctx.face == "front":
        v += 0.18
    u0, u1, v0, v1 = face_edge(ctx)
    if min(u0, u1, v0, v1) < 0.6 and min(u0 + u1, v0 + v1) >= 2:
        v += 0.08
    v += (hash01(ctx.u, ctx.v, 74) - 0.5) * 0.05
    return pick(PALETTE["iron"], v)


def stones_fill(ctx: Ctx):
    """Aufgeschüttete Bruchsteine (Füllung des Gegengewichts): einzelne Steine mit dunklen Zwischenräumen."""
    x, y, z = ctx.local
    val, edge = _STONE(x * 0.7 + 3.1, y * 0.7, z * 0.7)
    if edge < 0.18:
        return pick(PALETTE["stone"], 0.12 + val * 0.08)
    v = 0.4 + (val - 0.5) * 0.4 + (edge - 0.5) * 0.18 + (0.06 if ctx.face == "top" else 0.0)
    return pick(PALETTE["stone"], v)


def coil_paint(ctx: Ctx):
    """Auf eine Winde gewickeltes Seil (Achse X): Windungen von zwei Texeln (Kuppe hell, Fuge dunkel), leicht schräg."""
    r = rel(ctx)
    if ctx.face in ("west", "east"):
        return pick(PALETTE["rope"], 0.34)
    per = perimeter(r, ctx.cube.size, 0, ctx.face)
    t = (r[0] + math.floor(per / 3.0)) / 2.0
    f = t % 1.0
    v = (0.62 if f < 0.5 else 0.36) + LIT[ctx.face] + (hash01(math.floor(t), 57) - 0.5) * 0.06
    return pick(PALETTE["rope"], v)


def crate_paint(post: float = 2.0, width: float = 3.0):
    """
    Kasten aus Bohlen (z. B. Gegengewicht): an den senkrechten Kanten Eckpfosten (senkrechte Maserung, dunkler), dazwischen
    waagerechte Bohlen mit Fugen und Nägeln an den Pfosten; oben ein Rand aus Hirnholz und darin Bruchsteine.
    """
    plank_x = planks(0, width, 0.56, nails=False)
    plank_z = planks(2, width, 0.56, nails=False)

    def paint(ctx: Ctx):
        r = rel(ctx)
        size = ctx.cube.size
        face = ctx.face
        if face == "top":
            if min(r[0], size[0] - r[0], r[2], size[2] - r[2]) < 1.0:
                return pick(PALETTE["end"], end_value(ctx, 1, 0.58))
            return stones_fill(ctx)
        if face == "bottom":
            return plank_x(ctx)
        ca = 2 if face in ("west", "east") else 0                     # waagerechte Achse der Fläche
        d = min(r[ca], size[ca] - r[ca])
        if d < post:
            v = wood_value(ctx, 1, 0.48, knots=False)
            if post - 1.0 <= d < post:
                v -= 0.06
            return pick(PALETTE["oak"], v)
        if d < post + 1.0 and math.floor(r[1]) % 3 == 1:
            return pick(PALETTE["iron"], 0.6)                         # Nagel am Pfosten
        return plank_z(ctx) if ca == 2 else plank_x(ctx)

    return paint


def materials() -> dict:
    """Alle gemeinsamen Materialien (Modelle ergänzen ihre eigenen)."""
    return {
        "beam": wood(),
        "beam_x": wood(0),
        "beam_y": wood(1),
        "beam_z": wood(2),
        "dark_beam": wood(base=0.46),
        "post": wood(1, base=0.53),
        "plank": planks(),
        "plank_x": planks(0),
        "plank_y": planks(1),
        "plank_z": planks(2),
        "iron": iron(),
        "iron_plain": iron(0.4, corner_rivets=False),
        "band": band_paint(),
        "axle": axle_paint,
        "chain": chain_paint,
        "rope": rope_paint(),
        "skein": skein_paint,
        "leather": leather_paint(),
        "leather_plain": leather_paint(stitch=False),
        "stone": stone_paint(),
        "wheel12": wheel_paint(12),
        "wheel10": wheel_paint(10),
        "wheel8": wheel_paint(8, seams=(-0.5,)),
        "wheel6": wheel_paint(6, seams=()),
        "hub": hub_paint,
        "cap": cap_paint,
        "cushion": cushion_paint,
        "fletch": fletch_paint,
        "head": head_paint,
        "stones_fill": stones_fill,
        "coil": coil_paint,
        "shaft": wood(2, base=0.62, knots=False),
        "socket": iron(0.36, corner_rivets=False),
        "crate": crate_paint(),
    }
