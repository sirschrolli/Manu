"""
Dornenranke – Zauber des Naturelements: eine peitschende Ranke voller Dornen bricht aus dem Boden (ca. 49 hoch).
Boden-Regel: y = 24 ist der Boden, oben ist -y.

Knochen (eine Kette, jedes Glied Kind des vorigen, Drehpunkt am Fuß des Glieds, alle ohne Grunddrehung):
  * seg0   Drehpunkt (0, 24, 0) auf dem Boden, 6 dick, 10 hoch
  * seg1 … seg5   Drehpunkt oben auf dem vorigen Glied (0, -Länge, 0); jedes dünner (6 -> 2) und etwas kürzer;
                  seg5 endet in einer scharfen Spitze
Alle Quader eines Glieds beginnen an seinem Drehpunkt und reichen nach oben (-y); nur der Stamm jedes Glieds reicht
0,6 nach unten in das vorige hinein, damit beim Biegen (xRot/zRot) keine Lücke an den Gelenken aufklafft.

Jedes Glied trägt zwei bis drei Hakendornen (nach außen, Spitze nach oben gebogen), seg1–seg3 je zwei Blätter
(senkrechte Flächen mit gemaltem Umriss, schräg nach oben außen).

Bemalung: dunkle grünbraune, gedrehte Borke mit helleren Graten, in einer Furche ein schwach giftgrün leuchtender
Saftstrang (Leuchtbild); Dornen dunkelrot mit knochenweißer Spitze; Blätter sattgrün mit heller Mittelrippe, bei
einigen leuchten die Adern.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hexc, mix, noise3, pick

PALETTES = {
    "thorn_vine": {
        "bark": [hexc(c) for c in ("#141209", "#1f1d10", "#2a2916", "#36361d", "#434424", "#52532b", "#636434", "#77773f",
                                   "#8c8b4c")],
        "thorn": [hexc(c) for c in ("#2e0a08", "#4c120e", "#6e1c14", "#8e3424", "#ae5e48", "#cc9478", "#e6c6aa",
                                    "#f6ecdc")],
        "leaf": [hexc(c) for c in ("#0c240c", "#133612", "#1b4c18", "#25641e", "#307c26", "#3f9430", "#56ac3e",
                                   "#74c652", "#98dc6c")],
        "venom": [hexc(c) for c in ("#1c3604", "#345c08", "#56860e", "#80b018", "#acd832", "#d4f468")],
    },
}

# Glieder: (Länge, Dicke)
SEGMENTS = [(10.0, 6.0), (9.0, 5.2), (8.5, 4.4), (8.0, 3.6), (7.0, 2.8), (6.0, 2.0)]
OVERLAP = 0.6


def build() -> Model:
    m = Model("thorn_vine", 128, 80)
    m.tex_res = 2
    parent, pivot = "root", (0.0, 24.0, 0.0)
    for i, (length, thick) in enumerate(SEGMENTS):
        name = f"seg{i}"
        m.bone(name, parent, pivot=pivot)
        last = i == len(SEGMENTS) - 1
        body = length - (3.0 if last else 0.0)
        _stem(m, name, thick, body, i)
        if i == 0:
            _roots(m, name)
        else:
            # Knoten am Gelenk: etwas dicker, verdeckt beim Biegen die Fuge
            k = thick + 0.9
            m.cube(name, (-k / 2, -1.1, -k / 2), (k, 1.7, k), "knot", tag=f"seg:{i}")
        if last:
            # Spitze: zwei schmale Stufen, die letzte läuft gemalt spitz zu
            m.cube(name, (-0.65, -body - 1.6, -0.65), (1.3, 1.8, 1.3), "bark", tag=f"seg:{i}")
            m.cube(name, (-0.4, -length - 0.6, -0.4), (0.8, 2.2, 0.8), "tip")
        _thorns(m, name, i, length, thick)
        if 1 <= i <= 3:
            _leaves(m, name, i, length, thick)
        parent, pivot = name, (0.0, -length, 0.0)
    return m


def _stem(m: Model, bone: str, t: float, length: float, i: int) -> None:
    """Stamm eines Glieds: zwei überlagerte Quader (achteckiger Querschnitt), unten OVERLAP ins vorige Glied."""
    b = max(0.3, t * 0.15)
    m.cube(bone, (-t / 2, -length, -t / 2 + b), (t, length + OVERLAP, t - 2 * b), "bark", tag=f"seg:{i}")
    m.cube(bone, (-t / 2 + b, -length + 0.05, -t / 2), (t - 2 * b, length + OVERLAP - 0.1, t), "bark", tag=f"seg:{i}")


def _roots(m: Model, bone: str) -> None:
    """Wurzeln am Fuß: vier Stränge spreizen sich in den Boden (Fuß des ersten Glieds)."""
    for dx, dz, ln in ((1, 0, 4.2), (0, 1, 3.6), (-1, 0, 3.8), (0, -1, 4.4)):
        if dx:
            x0 = 2.4 if dx > 0 else -2.4 - ln
            m.cube(bone, (x0, -1.8, -1.1 + 0.4 * dx), (ln, 2.4, 2.2), "root", tag=f"root:{dx}:{dz}:{ln}")
        else:
            z0 = 2.4 if dz > 0 else -2.4 - ln
            m.cube(bone, (-1.1 - 0.4 * dz, -1.8, z0), (2.2, 2.4, ln), "root", tag=f"root:{dx}:{dz}:{ln}")
    m.cube(bone, (-3.9, -2.6, -3.9), (7.8, 3.2, 7.8), "knot", tag="seg:0")         # verdickter Fuß


# Richtungen (x, z) der Dornen der Reihe nach
_DIRS = [(1, 0), (0, -1), (-1, 0), (0, 1)]


def _thorns(m: Model, bone: str, i: int, length: float, t: float) -> None:
    """Hakendornen: ein kleiner Sockel am Stamm und zwei gekreuzte flache Flächen (senkrecht und waagrecht) mit
    gemaltem Umriss – breit am Stamm, spitz, die Spitze nach oben gebogen wie ein Rosendorn."""
    count = 3 if i < 4 else 2
    s = 0.7 + 0.3 * t / 6.0                                              # Größe nach Dicke
    for k in range(count):
        dx, dz = _DIRS[(i + k * (2 if count == 2 else 1) + (k == 2)) % 4]
        y = -length * (0.3 + 0.48 * k / max(1, count - 1)) - (0.6 if i % 2 else 0.0)
        r0 = t / 2 - 0.5
        side = (k % 2 * 2 - 1) * 0.5 * s                                 # etwas seitlich versetzt
        ln, wd, curve = 4.0 * s, 2.2 * s, 0.32
        base = 1.3 * s
        info = f"{dx}:{dz}:{r0:.3f}:{ln:.3f}:{wd:.3f}:{curve}:{y:.3f}:{side:.3f}"
        # Sockel
        if dx:
            x0 = r0 if dx > 0 else -r0 - 1.0 * s
            m.cube(bone, (x0, y - base / 2, side - base / 2), (1.0 * s, base, base), "thorn_base", tag=f"tb:{info}")
        else:
            z0 = r0 if dz > 0 else -r0 - 1.0 * s
            m.cube(bone, (side - base / 2, y - base / 2, z0), (base, base, 1.0 * s), "thorn_base", tag=f"tb:{info}")
        # gekreuzte Flächen: senkrecht (mit Haken nach oben) und waagrecht
        up = ln * curve + wd / 2 + 0.2
        if dx:
            x0 = r0 if dx > 0 else -r0 - ln
            m.cube(bone, (x0, y - up, side), (ln, up + wd / 2, 0), "thorn", tag=f"tv:{info}")
            m.cube(bone, (x0, y, side - wd / 2), (ln, 0, wd), "thorn", tag=f"th:{info}")
        else:
            z0 = r0 if dz > 0 else -r0 - ln
            m.cube(bone, (side, y - up, z0), (0, up + wd / 2, ln), "thorn", tag=f"tv:{info}")
            m.cube(bone, (side - wd / 2, y, z0), (wd, 0, ln), "thorn", tag=f"th:{info}")


def _leaves(m: Model, bone: str, i: int, length: float, t: float) -> None:
    """Zwei Blätter je Glied: senkrechte Flächen, eines quer (XY), eines längs (ZY), schräg nach oben außen."""
    for k, (dx, dz) in enumerate(((1, 0), (0, -1)) if i % 2 else ((-1, 0), (0, 1))):
        y = -length * (0.45 + 0.3 * k)
        r0 = t / 2 - 0.4
        if dx:
            x0 = r0 if dx > 0 else -r0 - 6.4
            m.cube(bone, (x0, y - 5.2, 0.15 * dx), (6.4, 5.8, 0), "leaf", tag=f"leaf:{dx}:{dz}:{r0:.3f}:{y:.3f}:{i}")
        else:
            z0 = r0 if dz > 0 else -r0 - 6.4
            m.cube(bone, (0.15, y - 5.2, z0), (0, 5.8, 6.4), "leaf", tag=f"leaf:{dx}:{dz}:{r0:.3f}:{y:.3f}:{i}")


# =============================================================================================
# Bemalen
# =============================================================================================

def _perimeter(ctx: Ctx) -> float:
    """Lage um den Stamm herum (0..1), aus dem Winkel um die Achse."""
    x, y, z = ctx.local
    return (math.atan2(z, x) / (2 * math.pi)) % 1.0


def _bark(ctx: Ctx):
    """Gedrehte Borke: schräg umlaufende Grate (hell, oben mit Lichtkante) und Furchen (dunkel); in einer Furche ein
    schwach leuchtender Saftstrang; Knoten; zur Spitze hin etwas heller und grüner."""
    x, y, z = ctx.local
    wx, wy, wz = ctx.world
    face = ctx.lface
    seg = int(ctx.cube.tag.split(":")[1])
    if face in ("top", "bottom"):
        return pick(ctx.pal["bark"], 0.42 if face == "top" else 0.2)
    p = _perimeter(ctx)
    twist = (p * 5.0 + wy / 7.0) % 1.0                                     # fünf Grate, eine Drehung je 35 Einheiten
    v = 0.4 + 0.06 * seg / 5.0
    if twist < 0.38:
        v += 0.2 + 0.07 * math.sin(twist / 0.38 * math.pi)                   # Grat
        if twist < 0.07:
            v += 0.1                                                         # Lichtkante
    elif twist > 0.86:
        v -= 0.16                                                            # Furche
    v += (noise3(wx * 0.4, wy * 0.15, wz * 0.4, 301) - 0.5) * 0.14
    # Saftstrang in einer Furche (nur jede fünfte), leuchtet schwach
    strand = math.floor(p * 5.0 + wy / 7.0) % 5
    if strand == 2 and 0.62 < twist < 0.8:
        col = pick(ctx.pal["venom"], 0.45 + 0.25 * noise3(wx, wy * 0.5, wz, 303))
        ctx.glow = pick(ctx.pal["venom"], 0.3)
        return col
    # Kanten der Fläche: unten dunkler
    if ctx.t == ctx.h - 1:
        v -= 0.05
    col = pick(ctx.pal["bark"], v)
    # Moosgrün in den Furchen nach oben hin
    if twist > 0.8 and seg >= 3:
        col = mix(col, pick(ctx.pal["leaf"], 0.3), 0.35)
    return col


def _tip(ctx: Ctx):
    """Scharfe Spitze (gemalt spitz zulaufend), dunkelrot wie die Dornen."""
    x, y, z = ctx.local
    w, h, d = ctx.cube.size
    ox, oy, oz = ctx.cube.origin
    f = max(0.0, min(1.0, (oy + h - y) / h))
    face = ctx.lface
    if face == "top":
        return None
    if face in ("west", "east"):
        across = abs((z - oz) / d - 0.5) * 2
    elif face in ("front", "back"):
        across = abs((x - ox) / w - 0.5) * 2
    else:
        across = 0.0
    if across > (1.0 - f) ** 0.8 + 0.05:
        return None
    return pick(ctx.pal["thorn"], 0.25 + 0.65 * f)


def _thorn_info(ctx: Ctx):
    kind, dx, dz, r0, ln, wd, curve, y0, side = ctx.cube.tag.split(":")
    return kind, int(dx), int(dz), float(r0), float(ln), float(wd), float(curve), float(y0), float(side)


def _thorn(ctx: Ctx):
    """Dorn (flache Fläche): breit am Stamm, spitz, in der senkrechten Fläche mit nach oben gebogener Spitze;
    dunkelrot am Stamm, zur Spitze knochenweiß, oben eine helle Kante."""
    kind, dx, dz, r0, ln, wd, curve, y0, side = _thorn_info(ctx)
    x, y, z = ctx.local
    a = (x * dx + z * dz) - r0                                            # nach außen
    f = a / ln
    if f < 0.0 or f > 1.0:
        return None
    if kind == "tv":
        b = (y0 - y) - curve * ln * f * f                                 # nach oben, um die Biegung verschoben
    else:
        b = (z - side) if dx else (x - side)
    half = wd / 2 * (1.0 - f) ** 1.15
    if abs(b) > half + 0.12:
        return None
    v = 0.14 + 0.84 * f ** 1.2
    if kind == "tv" and b > half - 0.35:
        v += 0.1                                                          # Lichtkante oben
    elif half - abs(b) < 0.3:
        v -= 0.06
    return pick(ctx.pal["thorn"], v)


def _thorn_base(ctx: Ctx):
    """Sockel des Dorns: dunkles Rot, zum Stamm hin in die Borke übergehend."""
    v = 0.16 + (0.06 if ctx.lface == "top" else 0.0)
    return pick(ctx.pal["thorn"], v)


_FACE_AXIS = {"west": 0, "east": 0, "top": 1, "bottom": 1, "front": 2, "back": 2}


def _leaf(ctx: Ctx):
    """Blatt (senkrechte Fläche): spitzes Oval vom Stamm schräg nach oben außen, helle Mittelrippe, schräge
    Seitenadern, dunkler Saum; bei jedem zweiten Glied leuchten die Adern giftgrün."""
    _, dx, dz, r0, y0, seg = ctx.cube.tag.split(":")
    dx, dz, r0, y0, seg = int(dx), int(dz), float(r0), float(y0), int(seg)
    x, y, z = ctx.local
    out = (x * dx + z * dz) - r0                                         # nach außen
    up = y0 - y                                                          # nach oben
    ang = math.radians(38)
    along = out * math.cos(ang) + up * math.sin(ang)
    across = -out * math.sin(ang) + up * math.cos(ang)
    length, width = 6.3, 3.1
    t = along / length
    if t < 0.0 or t > 1.0:
        return None
    across -= 0.5 * t * t                                                # Spitze biegt sich leicht nach unten
    half = width / 2 * math.sin(math.pi * min(1.0, t * 1.08)) ** 0.75 * (1.0 - 0.25 * t)
    if t < 0.12:
        half = max(half, 0.3)                                            # Blattstiel
    if abs(across) > half:
        return None
    s = across / max(half, 1e-3)
    v = 0.5 + 0.12 * (1 - abs(s)) - 0.08 * t
    glow_veins = seg % 2 == 1
    if abs(across) < 0.22:
        v = 0.78                                                         # Mittelrippe
        if glow_veins:
            col = pick(ctx.pal["venom"], 0.6)
            ctx.glow = pick(ctx.pal["venom"], 0.35)
            return col
    else:
        k = (along * 1.3 - abs(across) * 1.1) % 1.6
        if k < 0.22 and t < 0.9:
            if glow_veins:
                ctx.glow = pick(ctx.pal["venom"], 0.2)
                return pick(ctx.pal["venom"], 0.45)
            v += 0.12                                                    # Seitenader
    if half - abs(across) < 0.3:
        v -= 0.16                                                        # Saum
    return pick(ctx.pal["leaf"], v)


def _knot(ctx: Ctx):
    """Knoten am Gelenk: Borke mit hellem Wulst oben und dunkler Fuge unten."""
    col = _bark(ctx)
    if ctx.lface in ("top",):
        return pick(ctx.pal["bark"], 0.5)
    if ctx.lface not in ("bottom",) and ctx.t == 0:
        return pick(ctx.pal["bark"], 0.62)
    return col


def _root(ctx: Ctx):
    """Wurzelstrang: Borke, nach außen dunkler (verschwindet im Boden)."""
    _, dx, dz, ln = ctx.cube.tag.split(":")
    dx, dz, ln = int(dx), int(dz), float(ln)
    x, y, z = ctx.local
    out = max(0.0, min(1.0, ((x * dx + z * dz) - 2.4) / ln))
    v = 0.42 - 0.22 * out + (0.08 if ctx.lface == "top" else 0.0)
    if ctx.lface not in ("top", "bottom") and ctx.t == 0:
        v += 0.08
    return pick(ctx.pal["bark"], v + (noise3(x * 0.6, y * 0.6, z * 0.6, 305) - 0.5) * 0.12)


def materials(variant: str) -> dict:
    return {"bark": _bark, "knot": _knot, "root": _root, "tip": _tip, "thorn": _thorn, "thorn_base": _thorn_base,
            "leaf": _leaf}
