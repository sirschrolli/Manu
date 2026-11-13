"""
Eisgefängnis – Zauber des Eiselements: ein Käfig aus Eiskristallen, der einen stehenden Spieler einschließt (innen
frei: bis zur Hüfte mindestens 9, in Schulterhöhe 8,3 und in Kopfhöhe 7,9 Einheiten Abstand zur Mitte). Boden-Regel:
y = 24 ist der Boden, oben ist -y.

Knochen:
  * shard0 … shard7   je ein Kristall (mit ein, zwei kleinen Nebenkristallen am Fuß) im Ring um die Mitte, Drehpunkt
                      am Fuß AUF dem Boden (y = 24). Die vier hohen (34–40, an den Achsen) neigen sich nach innen, so
                      dass ihre Spitzen über den Kopf ragen, die vier niedrigen (22–28, auf den Diagonalen) stehen
                      steiler oder kippen leicht nach außen. Jeder Kristall ist für sich vollständig – das Spiel lässt
                      sie nacheinander aus dem Boden wachsen (Skalierung 0 -> 1 um den Fuß).
                      Die Drehung jedes Knochens ist (Neigung, Ausrichtung, 0): lokal -Z zeigt zur Mitte.
  * cap               Kristallknoten über dem Kopf (Drehpunkt 0, -14, 0): vier waagrechte Kristalle zu den Spitzen der
                      hohen Kristalle, einer nach oben, ein kurzer Eiszapfen nach unten.

Jeder Kristall ist ein sechseckig wirkendes Prisma aus drei überlagerten Quadern; die Spitze aus drei kleiner werdenden
Stufen, deren Seiten spitz zulaufend bemalt sind (der Rest durchsichtig). Bemalung: durchscheinendes blasses Blau
(Alpha ca. 180–235, Renderer mit durchscheinendem RenderType), Facetten je nach Lage heller/dunkler, weiß bereifte
Kanten und Raureif am Fuß, im Kern ein schwaches cyanfarbenes Leuchten.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hexc, noise3, pick
from .ice_lance import covered, with_alpha

PALETTES = {
    "ice_prison": {
        "ice": [hexc(c) for c in ("#0e2a52", "#143e72", "#1d5893", "#2b74b0", "#4593cc", "#68b2e2", "#90cdf0", "#b8e2f8",
                                  "#dcf2fd", "#f6fdff")],
        "frost": [hexc(c) for c in ("#a8c8e0", "#cce2f2", "#e8f6fe", "#ffffff")],
        "core": [hexc(c) for c in ("#06283c", "#0c4058", "#145e78", "#22829a", "#3eacbe")],
    },
}

# (Winkel um die Mitte in Grad, Abstand, Höhe, Dicke, Neigung nach innen in Grad,
#  Nebenkristalle: (Winkel um den Kristall – 90 = nach außen –, Höhe, Dicke))
SHARDS = [
    (0, 14.0, 40, 7.0, 6.0, ((35, 12, 2.8), (150, 8, 2.2))),
    (45, 12.0, 26, 5.0, -4.0, ((70, 9, 2.2),)),
    (90, 13.5, 35, 6.5, 5.5, ((125, 10, 2.6),)),
    (135, 11.5, 22, 4.5, 2.0, ((40, 7, 2.0), (140, 6, 1.8))),
    (180, 14.0, 38, 7.5, 6.0, ((55, 12, 2.8),)),
    (225, 12.0, 28, 5.5, -3.0, ((115, 9, 2.2),)),
    (270, 13.5, 33, 6.5, 5.0, ((30, 9, 2.4), (145, 7, 2.0))),
    (315, 11.5, 24, 4.5, 2.0, ((95, 8, 2.0),)),
]
CAP_Y = -14.0
_HEX = ((1.0, 0.5), (0.5, 0.866))                        # halbe Ausdehnung der zwei Quader (x, z) je Radius
_TIP = ((0.8, 0.26), (0.6, 0.24), (0.4, 0.2), (0.24, 0.17), (0.11, 0.13))   # Spitzenstufen (Radius, Längenanteil)
_TAPER_FROM = 3                                          # ab dieser Stufe laufen die Seiten gemalt spitz zu
_LIGHT_DIR = (-0.8, -0.6)                                # Licht von vorn links (waagrechter Anteil, Modell-Raum)


def _hex(m: Model, bone: str, cx: float, cz: float, r: float, y_bottom: float, y_top: float, mat: str,
         taper_to: float | None = None, step: int = -1) -> None:
    """Sechseckig wirkendes Prisma (zwei überlagerte Quader) von y_bottom bis y_top (y_top < y_bottom, oben = -y).
    ``taper_to``: Radius oben – die Seiten werden dann spitz zulaufend bemalt. Tag: "c:cx:cz:r:step|tp:r0:r1:y0:y1"."""
    tag = f"c:{cx:.3f}:{cz:.3f}:{r:.3f}:{step}"
    if taper_to is not None:
        tag += f"|tp:{r:.3f}:{taper_to:.3f}:{y_bottom:.3f}:{y_top:.3f}"
    for i, (fx, fz) in enumerate(_HEX):
        inset = 0.05 * i                                     # Stirnflächen nicht in einer Ebene (Flimmern)
        m.cube(bone, (cx - r * fx, y_top + inset, cz - r * fz), (2 * r * fx, y_bottom - y_top - 2 * inset, 2 * r * fz),
               mat, tag=tag)


def crystal(m: Model, bone: str, cx: float, cz: float, height: float, thick: float, mat: str = "shard",
            sink: float = 1.2) -> None:
    """Kristall: Prisma und Spitze aus fünf kleiner werdenden Stufen (die letzten laufen gemalt spitz zu), Fuß
    ``sink`` tief im Boden (y = 0 ist der Boden des Knochens)."""
    r = thick / 2
    tip_len = min(height * 0.42, r * 3.0)
    y_body = -(height - tip_len)
    _hex(m, bone, cx, cz, r, sink, y_body, mat)
    y = y_body
    for i, (frac, part) in enumerate(_TIP):
        y_next = y - tip_len * part
        rr = r * frac
        if i >= _TAPER_FROM:
            nxt = r * _TIP[i + 1][0] if i + 1 < len(_TIP) else 0.0
            _hex(m, bone, cx, cz, rr, y + 0.1, y_next, mat, taper_to=nxt, step=i)
        else:
            _hex(m, bone, cx, cz, rr, y + 0.1, y_next, mat, step=i)
        y = y_next


def build() -> Model:
    m = Model("ice_prison", 256, 112)
    m.tex_res = 2
    for i, (ang, dist, height, thick, lean, small) in enumerate(SHARDS):
        a = math.radians(ang)
        px, pz = dist * math.cos(a), dist * math.sin(a)
        # lokal -Z zeigt zur Mitte: Ry(psi) * (0, 0, -1) = Richtung zur Mitte
        dx, dz = -math.cos(a), -math.sin(a)
        psi = math.degrees(math.atan2(-dx, -dz))
        name = f"shard{i}"
        m.bone(name, pivot=(round(px, 3), 24, round(pz, 3)), rotation=(lean, round(psi, 3), 0))
        crystal(m, name, 0, 0, height, thick)
        for beta, h, t in small:
            b = math.radians(beta)
            d = thick * 0.42
            crystal(m, name, round(d * math.cos(b), 3), round(d * math.sin(b), 3), h, t, sink=1.0)

    # Kristallknoten über dem Kopf: Knoten, vier gestufte Arme zu den Spitzen der hohen Kristalle, Krone, Zapfen
    m.bone("cap", pivot=(0, CAP_Y, 0))
    _hex(m, "cap", 0, 0, 3.2, 2.2, -2.4, "cap")
    for sx, sz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        _arm(m, sx, sz)
    crystal_up(m, 0.0, 0.0, -2.2, 8.5, 3.4)
    crystal_up(m, 1.6, 1.4, -1.8, 5.0, 2.0)
    crystal_up(m, -1.7, -1.2, -1.8, 4.2, 1.8)
    _hex(m, "cap", 0, 0, 1.7, 4.4, 2.0, "cap", taper_to=0.9)                   # Zapfen nach unten
    _hex(m, "cap", 0, 0, 0.9, 6.0, 4.3, "cap", taper_to=0.0)
    return m


def crystal_up(m: Model, cx: float, cz: float, y0: float, height: float, thick: float) -> None:
    """Kristall der Krone (nach oben, ab y0)."""
    r = thick / 2
    _hex(m, "cap", cx, cz, r, y0 + 0.5, y0 - height * 0.45, "cap")
    y = y0 - height * 0.45
    for i, (frac, part) in enumerate(((0.7, 0.2), (0.45, 0.18), (0.25, 0.17))):
        y_next = y - height * part
        nxt = r * (0.45, 0.25, 0.0)[i]
        _hex(m, "cap", cx, cz, r * frac, y + 0.1, y_next, "cap", taper_to=nxt if i >= 1 else None, step=i)
        y = y_next


_ARM = ((1.7, 0.0, 3.0), (1.3, 2.8, 5.0), (0.9, 4.8, 6.8), (0.5, 6.6, 8.4))   # (halbe Dicke, von, bis) nach außen


def _arm(m: Model, sx: int, sz: int) -> None:
    """Waagrechter Kristall vom Knoten nach außen, in Stufen dünner."""
    for i, (r, a0, a1) in enumerate(_ARM):
        a0 += 2.0
        a1 += 2.0
        if sx:
            x0 = a0 if sx > 0 else -a1
            m.cube("cap", (x0, -r, -r), (a1 - a0, 2 * r, 2 * r), "cap_arm", tag=f"arm:{sx}:{sz}:{i}")
        else:
            z0 = a0 if sz > 0 else -a1
            m.cube("cap", (-r, -r, z0), (2 * r, 2 * r, a1 - a0), "cap_arm", tag=f"arm:{sx}:{sz}:{i}")


# =============================================================================================
# Bemalen
# =============================================================================================

def _bone_yaw(ctx: Ctx) -> float:
    return math.radians(ctx.cube.bone.rotation[1])


def _tag(ctx: Ctx) -> tuple[float, float, float, int, tuple | None]:
    head, _, taper = ctx.cube.tag.partition("|")
    _, cx, cz, r, step = head.split(":")
    tp = None
    if taper:
        tp = tuple(float(v) for v in taper.split(":")[1:])
    return float(cx), float(cz), float(r), int(step), tp


def _taper_ok(ctx: Ctx, tp) -> bool:
    """Gemalte Spitze (nur die dünnen letzten Stufen): Seiten als Trapez, das zur Spitze schmaler wird."""
    if tp is None:
        return True
    r0, r1, y0, y1 = tp
    x, y, z = ctx.local
    f = max(0.0, min(1.0, (y0 - y) / max(y0 - y1, 1e-3)))              # 0 unten … 1 oben
    scale = (r0 + (r1 - r0) * f) / max(r0, 1e-3)
    face = ctx.lface
    if face == "top":
        return False
    ox, oy, oz = ctx.cube.origin
    w, h, d = ctx.cube.size
    if face in ("west", "east"):
        return abs(z - (oz + d / 2)) <= d / 2 * scale + 1e-3
    if face in ("front", "back"):
        return abs(x - (ox + w / 2)) <= w / 2 * scale + 1e-3
    return True


def _facet(ctx: Ctx, cx: float, cz: float) -> tuple[float, bool]:
    """Helligkeit der Sechseck-Facette an dieser Stelle (-1 … 1, Licht von vorn links) und ob hier ein Grat liegt."""
    x, y, z = ctx.local
    phi = math.atan2(z - cz, x - cx)
    sector = round((phi - math.pi / 6) / (math.pi / 3))
    n_ang = math.pi / 6 + sector * math.pi / 3                         # Richtung der Facette
    yaw = _bone_yaw(ctx)
    lx, lz = _LIGHT_DIR
    llx = lx * math.cos(yaw) - lz * math.sin(yaw)                       # Licht im gedrehten Knochen-System
    llz = lx * math.sin(yaw) + lz * math.cos(yaw)
    lit = math.cos(n_ang) * llx + math.sin(n_ang) * llz
    vert = abs(((phi / (math.pi / 3)) + 0.5) % 1.0 - 0.5) * (math.pi / 3)
    ridge = vert * math.hypot(x - cx, z - cz) < 0.3
    return lit, ridge


def _ice(ctx: Ctx, cx: float, cz: float, hf: float, step: int, base: float = 0.0, frost_foot: float = 1.0,
         glow: float = 0.3):
    """Gemeinsame Eis-Farbe: blasses Blau, Facetten nach Lage um die Achse (Sechseck), helle Grate, bereifte
    Stufenkanten, Raureif am Fuß, zur Spitze heller, schwach leuchtender Kern."""
    x, y, z = ctx.local
    face = ctx.lface
    if face == "top":
        # Stufenkante: bereift (weiß), innen hellblau
        col = pick(ctx.pal["frost"], 0.55 + 0.4 * noise3(x * 1.4, y, z * 1.4, 33))
        return with_alpha(col, 240)
    if face == "bottom":
        return with_alpha(pick(ctx.pal["ice"], 0.34 + base), 200)
    lit, ridge = _facet(ctx, cx, cz)
    if ridge:
        col = pick(ctx.pal["frost"], 0.6 + 0.35 * hf)
        ctx.glow = pick(ctx.pal["core"], glow * 0.5)
        return with_alpha(col, 232)
    v = 0.48 + 0.13 * lit + 0.32 * hf + base
    a = 184
    g = glow
    # Oberkante jeder Fläche (unter der Stufe): bereift
    if ctx.t == 0 and step >= 0:
        return with_alpha(pick(ctx.pal["frost"], 0.7), 236)
    # Kern: zur Mitte der Facette etwas heller und leuchtend
    phi = math.atan2(z - cz, x - cx)
    mid = abs(((phi - math.pi / 6) / (math.pi / 3) + 0.5) % 1.0 - 0.5)
    if mid < 0.16:
        v += 0.04
        g = glow + 0.2
    # wenige feine Sprünge im Eis: schräge, helle Linien mit dunklem Saum darunter
    seed = cx * 7.3 + cz * 3.1 + _bone_yaw(ctx) * 5.0
    k = (y * 0.42 + (x - cx) * 0.9 - (z - cz) * 0.6 + seed) / 5.5
    frac = k - math.floor(k)
    if math.floor(k) % 3 == 0 and hf > 0.1:
        if frac < 0.045:
            v += 0.2
            a += 30
        elif frac < 0.075:
            v -= 0.06
    # Raureif am Fuß: weiß, nach oben ausfransend
    if frost_foot > 0 and y > -2.6 * frost_foot + (noise3(x * 0.9, 0, z * 0.9, 31) - 0.5) * 2.0:
        col = pick(ctx.pal["frost"], 0.45 + 0.55 * noise3(x * 1.3, y * 1.3, z * 1.3, 32))
        return with_alpha(col, 246)
    col = pick(ctx.pal["ice"], v)
    ctx.glow = pick(ctx.pal["core"], min(0.99, g))
    return with_alpha(col, a)


_HEIGHTS: dict[str, float] = {}


def _shard(ctx: Ctx):
    cx, cz, r, step, tp = _tag(ctx)
    if covered(ctx) or not _taper_ok(ctx, tp):
        return None
    bone = ctx.cube.bone
    total = _HEIGHTS.get(bone.name)
    if total is None:
        total = _HEIGHTS[bone.name] = -min(c.origin[1] for c in bone.cubes)
    hf = max(0.0, min(1.0, -ctx.local[1] / max(total, 1.0)))
    return _ice(ctx, cx, cz, hf, step)


def _cap(ctx: Ctx):
    cx, cz, r, step, tp = _tag(ctx)
    if covered(ctx) or not _taper_ok(ctx, tp):
        return None
    hf = max(0.0, min(1.0, 0.6 - ctx.local[1] / 18.0))
    return _ice(ctx, cx, cz, hf, step, base=0.02, frost_foot=0.0, glow=0.35)


def _cap_arm(ctx: Ctx):
    """Waagrechte Kristalle des Knotens: Stufen mit bereiften Kanten, oben hell, unten dunkler, die Spitze offen."""
    if covered(ctx):
        return None
    _, sx, sz, step = ctx.cube.tag.split(":")
    sx, sz, step = int(sx), int(sz), int(step)
    face = ctx.lface
    outward = ("east" if sx > 0 else "west") if sx else ("back" if sz > 0 else "front")
    if face == outward:
        col = pick(ctx.pal["frost"], 0.8)                               # Stufenfläche nach außen: bereift
        return with_alpha(col, 238)
    x, y, z = ctx.local
    along = abs(x) if sx else abs(z)
    f = max(0.0, min(1.0, (along - 2.0) / 8.4))
    v = {"top": 0.66, "bottom": 0.36}.get(face, 0.5) + 0.18 * f
    if (face in ("top", "bottom") and (ctx.s == 0 or ctx.s == ctx.w - 1)) or \
            (face not in ("top", "bottom") and ctx.t == 0):
        return with_alpha(pick(ctx.pal["frost"], 0.6), 232)              # helle Grate
    ctx.glow = pick(ctx.pal["core"], 0.35)
    return with_alpha(pick(ctx.pal["ice"], v), 190)


def materials(variant: str) -> dict:
    return {"shard": _shard, "cap": _cap, "cap_arm": _cap_arm}
