"""
Neuer, sauberer Stil der Drachen-Bosse (Version 2): runde Querschnitte statt kantiger Klötze, spitz zulaufende
Stacheln und Hörner, einzelne Zähne – und eine ruhigere Bemalung: weiche Schuppen mit Licht von oben, helle
Bauchplatten, dunkle Kanten und Leuchten nur dort, wo es hingehört (Augen, Maul, Adern, Kristalle).

Die Knochen heißen wie bisher, damit Animation und Trefferzonen weiter passen.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, pick, shade


# =============================================================================================
# Geometrie
# =============================================================================================

def rbox(m: Model, bone: str, origin, size, mat="scales", bevel: float = 1.0, axis: int | None = None) -> None:
    """Quader mit abgeschrägten Längskanten (zwei überlappende Quader ergeben einen achteckigen Querschnitt).

    Die Längsachse ist die längste Seite (oder ``axis``); entlang der beiden anderen wird ``bevel`` abgeschrägt."""
    ox, oy, oz = origin
    w, h, d = size
    dims = [w, h, d]
    if axis is None:
        axis = max(range(3), key=lambda i: dims[i])
    a, b = [i for i in range(3) if i != axis]
    if bevel <= 0 or dims[a] <= 2 * bevel + 0.5 or dims[b] <= 2 * bevel + 0.5:
        m.cube(bone, origin, size, mat)
        return
    tag = f"rb:{oy:.3f}:{h:.3f}"
    o1 = [ox, oy, oz]
    s1 = list(dims)
    o1[b] += bevel
    s1[b] -= 2 * bevel
    m.cube(bone, tuple(o1), tuple(s1), mat, tag=tag)
    # der zweite Quader ist an den Enden minimal kürzer: sonst lägen die Stirnflächen in einer Ebene (Flimmern)
    inset = min(0.08, dims[axis] * 0.02)
    o2 = [ox, oy, oz]
    s2 = list(dims)
    o2[a] += bevel
    s2[a] -= 2 * bevel
    o2[axis] += inset
    s2[axis] -= 2 * inset
    m.cube(bone, tuple(o2), tuple(s2), mat, tag=tag)


def spike(m: Model, parent: str, name: str, pos, height: float, base: float, lean: float = -25.0, mat: str = "spike",
          yaw: float = 0.0, roll: float = 0.0, depth: float | None = None, steps: int = 3) -> None:
    """Spitz zulaufender Stachel (Spitze nach oben, -Y), auf eigenem Knochen um ``lean`` Grad nach hinten geneigt."""
    m.bone(name, parent, pivot=pos, rotation=(lean, yaw, roll))
    dep = base * 1.25 if depth is None else depth
    y = 0.6
    for i in range(steps):
        f = i / max(1, steps - 1)
        seg_h = (height + 0.6) * (0.42 if i == 0 else 0.58 / (steps - 1))
        bw = max(0.5, base * (1.0 - 0.7 * f))
        bd = max(0.5, dep * (1.0 - 0.7 * f))
        m.cube(name, (-bw / 2, y - seg_h - 0.25, -bd / 2), (bw, seg_h + 0.25, bd), mat, tag=f"sp:{height + 0.6:.3f}")
        y -= seg_h


def horn(m: Model, parent: str, prefix: str, pivot, segments: list[tuple[float, float, tuple[float, float, float]]],
         mat: str = "horn", taper_to: float = 0.45) -> list[str]:
    """Horn aus Gliedern (Länge, Dicke, Drehung), jedes Glied auf eigenem Knochen; zeigt entlang -Z (nach vorn) bzw. so,
    wie die Drehungen es biegen. Die Dicke nimmt innerhalb jedes Glieds etwas ab (zwei Stufen)."""
    names = []
    p, pv = parent, pivot
    for i, (length, thick, rot) in enumerate(segments, start=1):
        name = f"{prefix}{i}"
        m.bone(name, p, pivot=pv, rotation=rot)
        t2 = max(0.6, thick * (1.0 - (1.0 - taper_to) * 0.5))
        tag = f"hn:{i}:{len(segments)}:{length:.3f}:-1"
        m.cube(name, (-thick / 2, -thick / 2, -length * 0.55), (thick, thick, length * 0.55 + 0.4), mat, tag=tag)
        m.cube(name, (-t2 / 2, -t2 / 2, -length - 0.2), (t2, t2, length * 0.5 + 0.2), mat, tag=tag)
        names.append(name)
        p, pv = name, (0, 0, -length)
    return names


def horn_back(m: Model, parent: str, prefix: str, pivot, segments: list[tuple[float, float, tuple[float, float, float]]],
              mat: str = "horn", taper_to: float = 0.62) -> list[str]:
    """Horn nach hinten (+Z): Glieder (Länge, Dicke, Drehung) auf eigenen Knochen. Positive X-Drehung biegt nach oben,
    positive Y-Drehung nach links (+X). Jedes Glied verjüngt sich in zwei Stufen."""
    names = []
    p, pv = parent, pivot
    for i, (length, thick, rot) in enumerate(segments, start=1):
        name = f"{prefix}{i}"
        m.bone(name, p, pivot=pv, rotation=rot)
        t2 = max(0.5, thick * taper_to)
        tag = f"hn:{i}:{len(segments)}:{length:.3f}:1"
        m.cube(name, (-thick / 2, -thick / 2, -0.4), (thick, thick, length * 0.55 + 0.4), mat, tag=tag)
        m.cube(name, (-t2 / 2, -t2 / 2, length * 0.5), (t2, t2, length * 0.5 + 0.25), mat, tag=tag)
        names.append(name)
        p, pv = name, (0, 0, length)
    return names


def teeth(m: Model, bone: str, x: float, y: float, z0: float, z1: float, count: int, height: float = 1.6, down: bool = True,
          size: float = 0.9, mat: str = "teeth") -> None:
    """Eine Reihe einzelner Zähne an einer Kieferkante (von z0 nach z1, bei x; nach unten oder oben gerichtet)."""
    for i in range(count):
        z = z0 + (z1 - z0) * (i + 0.5) / count
        h = height * (1.0 + (0.35 if i in (0, count - 1) and count > 3 else 0.0))
        oy = y if down else y - h
        m.cube(bone, (x - size / 2, oy, z - size / 2), (size, h, size), mat)


def toes(m: Model, bone: str, width: float, y: float, z: float, count: int = 3, length: float = 3.5, thick: float = 1.8,
         claw: float = 2.0, mat: str = "scales", claw_mat: str = "claw") -> None:
    """Zehen mit gebogenen Krallen vorn am Fuß (Fuß zeigt nach -Z)."""
    spacing = width / count
    for t in range(count):
        cx = -width / 2 + spacing * (t + 0.5)
        m.cube(bone, (cx - thick / 2, y, z - length), (thick, thick, length + 0.5), mat)
        m.cube(bone, (cx - thick * 0.35, y + thick * 0.25, z - length - claw), (thick * 0.7, thick * 0.9, claw + 0.3), claw_mat,
               tag="cl")


# =============================================================================================
# Bemalung
# =============================================================================================

def edge_ao(ctx: Ctx, col, strength: float = 0.12):
    """Dunkle Kanten am Rand jeder Fläche: so lesen sich die Formen klar, auch aus der Ferne."""
    d = min(ctx.s, ctx.w - 1 - ctx.s, ctx.t, ctx.h - 1 - ctx.t)
    if ctx.w <= 2 or ctx.h <= 2:
        return col
    if d == 0:
        return shade(col, 1.0 - strength)
    return col


def facing_down(ctx: Ctx) -> float:
    """0 = Oberseite/oben am Quader, 1 = Unterseite/unten (bei abgeschrägten Quadern bezogen auf die ganze Form)."""
    tag = ctx.cube.tag
    if tag.startswith("rb:"):
        _, oy, h = tag.split(":")
        oy, h = float(oy), float(h)
        if ctx.face == "top" and ctx.local[1] <= oy + 0.01:
            return 0.0
        if ctx.face == "bottom" and ctx.local[1] >= oy + h - 0.01:
            return 1.0
        return max(0.0, min(1.0, (ctx.local[1] - oy) / h)) if h > 0 else 0.5
    if ctx.face == "top":
        return 0.0
    if ctx.face == "bottom":
        return 1.0
    oy, h = ctx.cube.origin[1], ctx.cube.size[1]
    return 0.5 if h <= 0 else (ctx.local[1] - oy) / h


def pick_smooth(r, v: float):
    """Wie pick(), aber zwischen zwei Stufen der Rampe gemischt (weiche Verläufe)."""
    v = max(0.0, min(0.9999, v)) * (len(r) - 1)
    i = int(v)
    if i >= len(r) - 1:
        return r[-1]
    return mix(r[i], r[i + 1], v - i)


def plate_color(ctx: Ctx, key: str = "belly", plate: float = 2.4):
    """Querliegende Bauchplatten: helle Platten mit dunkler Fuge."""
    z = ctx.world[2]
    k = (z / plate) % 1.0
    v = 0.58 + 0.12 * (1.0 - k)
    if k > 0.82:
        v = 0.3
    return pick(ctx.pal[key], v)


def plates(key: str = "belly", glow=None, glow_key: str = "glow", ao: float = 0.1) -> callable:
    def paint(ctx: Ctx):
        if glow is not None:
            g = glow(ctx)
            if g > 0:
                col = mix(plate_color(ctx, key), pick(ctx.pal[glow_key], 0.55 + 0.4 * g), 0.6 + 0.4 * g)
                ctx.glow = pick(ctx.pal[glow_key], 0.45 + 0.5 * g)
                return col
        return edge_ao(ctx, plate_color(ctx, key), ao)

    return paint


def solid(key: str, value: float = 0.5, noise: float = 0.05, ao: float = 0.1, glow: bool = False) -> callable:
    def paint(ctx: Ctx):
        col = pick(ctx.pal[key], value + (hash01(ctx.u, ctx.v, key) - 0.5) * noise)
        if glow:
            ctx.glow = col
            return col
        return edge_ao(ctx, col, ao)

    return paint


def eye(key: str = "eye", slit: bool = True, dark: str = "back") -> callable:
    """Reptilienauge auf der Vorder- und Außenseite: leuchtende Iris (innen hell, zum Rand dunkler), senkrechte
    Schlitzpupille (ein Pixel breit), dunkles Lid oben; die übrigen Seiten in Hautfarbe."""

    def paint(ctx: Ctx):
        if ctx.face in ("top", "bottom", "back"):
            return pick_smooth(ctx.pal[dark], 0.3)
        cx = (ctx.w - 1) / 2
        cy = (ctx.h - 1) / 2
        if ctx.t == 0 and ctx.h > 2:
            return pick_smooth(ctx.pal[dark], 0.14)      # Lid
        if slit and ctx.w >= 6 and ctx.s == int(cx + 0.5) and 0 < ctx.t < ctx.h - 1:
            return 16, 8, 6, 255
        r = max(abs(ctx.s - cx) / max(cx, 1), abs(ctx.t - cy) / max(cy, 1))
        col = pick_smooth(ctx.pal[key], 0.95 - 0.5 * r)
        ctx.glow = col
        return col

    return paint


def mouth(key: str = "mouth", glow: bool = False) -> callable:
    def paint(ctx: Ctx):
        col = pick(ctx.pal[key], 0.3 + 0.35 * hash01(ctx.u, ctx.v, "m"))
        if glow:
            ctx.glow = shade(col, 0.8)
        return col

    return paint


def membrane_fill(ctx: Ctx, key: str, rel: float, vein: bool, vein_key: str, glow_veins: bool):
    """Farbe der Flughaut: zum Rand hin dunkler, Adern in der Aderfarbe."""
    v = 0.5 + 0.08 * math.sin(ctx.local[2] * 0.22 + ctx.local[0] * 0.09) - rel * 0.14
    col = pick(ctx.pal[key], v)
    if vein:
        col = pick(ctx.pal[vein_key], 0.5)
        if glow_veins:
            ctx.glow = col
    return col


def finger_web(key: str = "membrane", vein_key: str = "vein", tatter: float = 0.0, glow_veins: bool = False,
               seed: int = 3, edge_key: str | None = None) -> callable:
    """Flughaut zwischen zwei Fingern (Form aus dem Tag des Quaders, siehe boss_common.wing)."""

    def paint(ctx: Ctx):
        lx, ly, lz = ctx.local
        _, idx, count, gap, length, nxt, sag, z_off = ctx.cube.tag.split(":")
        idx = int(idx)
        gap, length, nxt, sag, z_off = (float(v) for v in (gap, length, nxt, sag, z_off))
        x = abs(lx)
        z = lz - z_off
        a = math.degrees(math.atan2(z, x))
        if a < 0 or a > gap:
            return None
        k = a / gap
        r_edge = (length + (nxt - length) * k) * (1 - sag * math.sin(math.pi * k))
        r = math.hypot(x, z)
        if r > r_edge:
            return None
        rel = r / max(r_edge, 1e-3)
        if tatter > 0 and rel > 0.88 and noise3(lx * 0.4, lz * 0.4, idx * 3.1, seed) < tatter:
            return None
        veins = max(2, int(gap / 15))
        fan = k * veins
        vein = abs(fan - round(fan)) < 0.04 + 0.06 * (1 - rel) and rel > 0.1 and 0 < round(fan) < veins
        col = membrane_fill(ctx, key, rel, vein, vein_key, glow_veins)
        if rel > 0.955:
            col = pick(ctx.pal[edge_key or key], 0.18)
        return col

    return paint


def arm_web(key: str = "membrane", vein_key: str = "vein", fore: bool = False, glow_veins: bool = False) -> callable:
    """Flughaut am Ober-/Unterarm."""

    def paint(ctx: Ctx):
        lx, ly, lz = ctx.local
        ox, oy, oz = ctx.cube.origin
        w, h, d = ctx.cube.size
        span = (lx - ox) / w if ox >= 0 else ((ox + w) - lx) / w
        chord = (lz - oz) / max(d, 1e-3)
        limit = (0.98 - 0.12 * span) if not fore else (0.95 - 0.35 * span ** 1.3)
        limit -= 0.05 * abs(math.sin(span * math.pi * 1.5))
        if chord > limit:
            return None
        vein = False
        for vs in ((0.38, 0.62) if not fore else (0.3, 0.55, 0.8)):
            if abs(span - (vs + 0.12 * chord * chord)) < 0.016 and chord < limit - 0.06:
                vein = True
        col = membrane_fill(ctx, key, chord / max(limit, 1e-3), vein, vein_key, glow_veins)
        if chord > limit - 0.035:
            col = pick(ctx.pal[key], 0.18)
        return col

    return paint


# =============================================================================================
# Körperteile (Version 2)
# =============================================================================================

def chain2(m: Model, prefix: str, parent: str, start, sizes: list[tuple[float, float, float]], rots: list[float],
           direction: int = 1, mat: str = "scales", belly: str | None = "plates", bevel: float = 0.28,
           spikes: list[float] | None = None, spike_mat: str = "spike", spike_lean: float = -32.0,
           yaw_rots: list[float] | None = None, overlap: float = 1.2) -> list[str]:
    """Hals oder Schwanz: Glieder mit rundem Querschnitt, die sich überlappen (keine Lücken beim Biegen), mit
    Bauchplatten und spitz zulaufenden Rückenstacheln. ``bevel`` ist der Anteil der Abschrägung an der kleineren
    Seite; der Drehpunkt liegt am Anfang jedes Glieds (Schwanz +Z, Hals -Z)."""
    names = []
    pivot = start
    for i, (w, h, length) in enumerate(sizes):
        name = f"{prefix}{i + 1}"
        yaw = yaw_rots[i] if yaw_rots else 0.0
        m.bone(name, parent, pivot=pivot, rotation=(rots[i], yaw, 0))
        z0 = -overlap * 0.5 if direction > 0 else -length - overlap * 0.5
        b = max(0.6, min(w, h) * bevel)
        rbox(m, name, (-w / 2, -h / 2, z0), (w, h, length + overlap), mat, bevel=b, axis=2)
        if belly:
            m.cube(name, (-w / 2 + b, h / 2 - 1.0, z0 + 0.6), (w - 2 * b, 1.6, length + overlap - 1.2), belly)
        if spikes and i < len(spikes) and spikes[i] > 0:
            s = spikes[i]
            zc = length * 0.45 * direction
            spike(m, name, f"{name}_spike", (0, -h / 2 + 0.4, zc), s, max(1.4, min(3.2, w * 0.24)), lean=spike_lean, mat=spike_mat)
        names.append(name)
        parent = name
        pivot = (0, 0, length * direction)
    return names


def leg2(m: Model, side: str, sx: int, kind: str, parent: str, hip, segs: list[tuple], toe_count: int = 3,
         toe_len: float = 3.5, claw: float = 2.2, mat: str = "scales", claw_mat: str = "claw",
         knee_spike: float = 0.0, spike_mat: str = "spike") -> None:
    """Bein wie boss_common.leg (gleiche Knochen), aber mit runden Gliedern, einem Muskelansatz am Oberschenkel und
    Zehen mit Krallen. segs = [(breite, länge, tiefe, winkel_x), (…), (fuß_breite, fuß_höhe, fuß_tiefe, winkel_x)]."""
    (w1, l1, d1, r1), (w2, l2, d2, r2), (w3, h3, d3, r3) = segs
    up = f"{kind}_leg_{side}"
    shin = f"{kind}_shin_{side}"
    foot = f"{kind}_foot_{side}"
    m.bone(up, parent, pivot=(hip[0] * sx, hip[1], hip[2]), rotation=(r1, 0, 0))
    rbox(m, up, (-w1 / 2, -w1 * 0.4, -d1 / 2), (w1, l1 + w1 * 0.4, d1), mat, bevel=min(w1, d1) * 0.22, axis=1)
    # Muskel oben am Oberschenkel (geht in den Rumpf über)
    rbox(m, up, (-w1 / 2 - 0.6, -w1 * 0.55, -d1 / 2 - 0.6), (w1 + 1.2, w1 * 0.75, d1 + 1.2), mat, bevel=min(w1, d1) * 0.3, axis=1)
    m.bone(shin, up, pivot=(0, l1 - 0.6, 0), rotation=(r2, 0, 0))
    rbox(m, shin, (-w2 / 2, -1.4, -d2 / 2), (w2, l2 + 1.6, d2), mat, bevel=min(w2, d2) * 0.22, axis=1)
    if knee_spike > 0:
        spike(m, shin, f"{shin}_spike", (0, 0.5, d2 / 2 if kind == "hind" else -d2 / 2), knee_spike, 1.6,
              lean=110.0 if kind == "hind" else -110.0, mat=spike_mat)
    m.bone(foot, shin, pivot=(0, l2 - 0.6, 0), rotation=(r3, 0, 0))
    rbox(m, foot, (-w3 / 2, 0, -d3 * 0.7), (w3, h3, d3), mat, bevel=min(1.2, h3 * 0.3), axis=2)
    toes(m, foot, w3 + 0.6, h3 - 1.9, -d3 * 0.7 + 0.6, toe_count, toe_len, 1.9, claw, mat, claw_mat)


def _span(sx: int, a: float, b: float) -> tuple[float, float]:
    """Abschnitt [a, b] nach außen (links +X, rechts -X) als (Anfang, Länge) eines Quaders."""
    return (a, b - a) if sx > 0 else (-b, b - a)


def wing2(m: Model, side: str, sx: int, parent: str, shoulder, arm: float, fore: float,
          fingers: list[tuple[float, float]], arm_th: float = 4.5, inner_chord: float = 30.0,
          fore_chord: float = 30.0, mats: dict | None = None, thumb: bool = True, sag: float = 0.16,
          web_bones: bool = False, finger_th: float = 2.4) -> None:
    """Flügel mit denselben Knochen wie boss_common.wing, aber mit runden, zum Ende hin dünneren Knochen, einem
    Knöchel am Handgelenk und einer Daumenkralle. Die Flughäute (und ihre Tags) bleiben gleich."""
    mats = mats or {}
    bone_mat = mats.get("bone", "scales")
    claw_mat = mats.get("claw", "claw")
    w = f"wing_{side}"
    m.bone(w, parent, pivot=(shoulder[0] * sx, shoulder[1], shoulder[2]))
    t0, t1 = arm_th + 1.0, arm_th * 0.8
    x, length = _span(sx, -1.0, arm * 0.55)
    rbox(m, w, (x, -t0 / 2, -t0 / 2), (length, t0, t0), bone_mat, bevel=t0 * 0.28, axis=0)
    x, length = _span(sx, arm * 0.5, arm + 0.8)
    rbox(m, w, (x, -t1 / 2, -t1 / 2), (length, t1, t1), bone_mat, bevel=t1 * 0.28, axis=0)
    web = w
    if web_bones:
        web = f"{w}_web"
        m.bone(web, w, pivot=(0, 0, 0))
    x, length = _span(sx, 0, arm)
    m.cube(web, (x, 0, t1 / 2 - 0.5), (length, 0, inner_chord), mats.get("inner", "membrane_inner"))

    f = f"{w}_fore"
    m.bone(f, w, pivot=(arm * sx, 0, 0))
    t2, t3 = arm_th * 0.78, arm_th * 0.55
    x, length = _span(sx, -0.6, fore * 0.5)
    rbox(m, f, (x, -t2 / 2, -t2 / 2), (length, t2, t2), bone_mat, bevel=t2 * 0.28, axis=0)
    x, length = _span(sx, fore * 0.45, fore + 0.6)
    rbox(m, f, (x, -t3 / 2, -t3 / 2), (length, t3, t3), bone_mat, bevel=t3 * 0.28, axis=0)
    fweb = f
    if web_bones:
        fweb = f"{f}_web"
        m.bone(fweb, f, pivot=(0, 0, 0))
    x, length = _span(sx, 0, fore)
    m.cube(fweb, (x, 0, t3 / 2 - 0.5), (length, 0, fore_chord), mats.get("fore", "membrane_fore"))

    h = f"{w}_hand"
    m.bone(h, f, pivot=(fore * sx, 0, 0))
    k = arm_th * 0.95
    rbox(m, h, (-k / 2, -k / 2, -k / 2), (k, k, k), bone_mat, bevel=k * 0.25, axis=0)
    if thumb:
        m.cube(h, (-0.9, -0.9, -k / 2 - 3.2), (1.8, 1.8, 3.4), bone_mat)
        m.cube(h, (-0.6, -0.5, -k / 2 - 5.4), (1.2, 1.5, 2.4), claw_mat)
    for i, (length, ang) in enumerate(fingers, start=1):
        fn = f"{w}_f{i}"
        m.bone(fn, h, pivot=(0, 0, 0), rotation=(0, -ang * sx, 0))
        fth = max(1.0, finger_th - (i - 1) * 0.3)
        ft2 = max(0.7, fth * 0.6)
        x, ln = _span(sx, 0, length * 0.52)
        m.cube(fn, (x, -fth / 2, -fth / 2), (ln, fth, fth), bone_mat)
        x, ln = _span(sx, length * 0.5, length)
        m.cube(fn, (x, -ft2 / 2, -ft2 / 2), (ln, ft2, ft2), bone_mat)
        if i < len(fingers):
            gap = fingers[i][1] - ang
            nxt = fingers[i][0]
        else:
            gap = min(90.0, 175.0 - ang)
            nxt = fore_chord * 1.05
        gap = max(8.0, gap)
        xmax = zmax = 0.0
        for kk in range(41):
            a = math.radians(gap * kk / 40)
            r = (length + (nxt - length) * kk / 40) * (1 - sag * math.sin(math.pi * kk / 40))
            xmax = max(xmax, r * math.cos(a))
            zmax = max(zmax, r * math.sin(a))
        z_off = ft2 / 2 - 0.3
        x0 = 0 if sx > 0 else -xmax
        cube = m.cube(fn, (x0, 0, z_off), (math.ceil(xmax), 0, math.ceil(zmax) + 1), mats.get("outer", "membrane_outer"))
        cube.tag = f"finger:{i}:{len(fingers)}:{gap:.2f}:{length:.2f}:{nxt:.2f}:{sag:.3f}:{z_off:.2f}"


def keratin(key: str = "horn", tip_key: str | None = None, rings: float = 0.0, base: float = 0.22, span: float = 0.62,
            ao: float = 0.12, seed: int = 4) -> callable:
    """Horn, Stachel, Kralle: von dunkler Basis zur hellen Spitze (Verlauf über die ganze Länge, auch über mehrere
    Glieder), optional mit feinen Querrillen und andersfarbiger Spitze."""

    def paint(ctx: Ctx):
        prog = _tag_progress(ctx)
        v = base + span * prog + (hash01(ctx.u, ctx.v, seed) - 0.5) * 0.04
        if rings and (prog * rings) % 1.0 < 0.22:
            v -= 0.07
        col = pick_smooth(ctx.pal[key], v)
        if tip_key and prog > 0.72:
            col = mix(col, pick_smooth(ctx.pal[tip_key], 0.55 + 0.4 * prog), (prog - 0.72) / 0.28)
        return edge_ao(ctx, col, ao)

    return paint


def fold_pose(fingers: list[tuple[float, float]], finger_len: float = 0.6) -> dict:
    """Haltung der angelegten Flügel wie im Spiel (DragonBossModel.foldWing) – für die Vorschau."""
    pose = {}
    deg = math.degrees
    for side, sx in (("l", 1), ("r", -1)):
        pose[f"wing_{side}"] = (deg(-1.3), deg(-1.3) * sx, deg(-0.35) * sx, 1, 1, 0.6)
        pose[f"wing_{side}_fore"] = (0, 0, deg(2.85) * sx, 1, 1, 0.6)
        pose[f"wing_{side}_hand"] = (deg(1.57), 0, deg(-2.97) * sx)
        for i, (_, ang) in enumerate(fingers, start=1):
            base = -ang * sx
            pose[f"wing_{side}_f{i}"] = (0, -0.92 * base, 0, finger_len, 1, 0.2)
    return pose


# =============================================================================================
# Schuppen (Version 3): plastisch, mit Lichtkante – auch im Spiel aus der Entfernung lesbar
# =============================================================================================

class ScaleCells:
    """Zellen-Muster wie Voronoi3D, aber mit Lage des Punktes in seiner Zelle (für Wölbung und Lichtkante).

    ``stretch`` streckt die Zellen entlang Z (Körperachse): Schuppen sind länger als breit."""

    def __init__(self, cell: float, seed: int, stretch: float = 1.35):
        self.cell = cell
        self.seed = seed
        self.stretch = stretch
        self._cache: dict = {}

    def _point(self, ix: int, iy: int, iz: int):
        key = (ix, iy, iz)
        p = self._cache.get(key)
        if p is None:
            h1 = hash01(ix, iy, iz, self.seed)
            h2 = hash01(iy, iz, ix, self.seed + 1)
            h3 = hash01(iz, ix, iy, self.seed + 2)
            h4 = hash01(ix, iz, iy, self.seed + 3)
            p = ((ix + 0.15 + 0.7 * h1) * self.cell, (iy + 0.15 + 0.7 * h2) * self.cell, (iz + 0.15 + 0.7 * h3) * self.cell, h4)
            self._cache[key] = p
        return p

    def __call__(self, x: float, y: float, z: float):
        """-> (Zellwert, Randnähe 0..1, dx, dy, dz) mit d = Punkt - Zellmitte (in Zellgrößen)."""
        z = z / self.stretch
        c = self.cell
        cx, cy, cz = int(math.floor(x / c)), int(math.floor(y / c)), int(math.floor(z / c))
        best = 1e9
        second = 1e9
        bp = None
        for dx in (-1, 0, 1):
            for dy in (-1, 0, 1):
                for dz in (-1, 0, 1):
                    p = self._point(cx + dx, cy + dy, cz + dz)
                    dist = (p[0] - x) ** 2 + (p[1] - y) ** 2 + (p[2] - z) ** 2
                    if dist < best:
                        second = best
                        best = dist
                        bp = p
                    elif dist < second:
                        second = dist
        edge = max(0.0, min(1.0, (math.sqrt(second) - math.sqrt(best)) / (c * 0.4)))
        return bp[3], edge, (x - bp[0]) / c, (y - bp[1]) / c, (z - bp[2]) / c


def scales3(back: str = "back", belly: str = "belly", cell: float = 2.6, seed: int = 11, belly_from: float = 0.7,
            top_light: float = 0.12, var: float = 0.1, relief: float = 0.16, line: float = 0.14, ao: float = 0.1,
            stretch: float = 1.35, hot: tuple[float, float] | None = None, hot_key: str = "glow", foot_dark: float = 0.0,
            belly_world_y: float | None = None, bias: float = 0.0, line_depth: float = 0.16) -> callable:
    """Schuppen mit Wölbung: jede Schuppe ist oben/hinten heller und hat eine feine dunkle Fuge; leicht verschiedene
    Töne je Schuppe. Zur Unterseite hin Übergang in die Bauchplatten.

    ``hot=(ab, breite)``: ab dieser Höhe (0 oben … 1 unten) glühen die Fugen zwischen den Schuppen, nach unten breiter
    (Lava unter der Kruste). ``foot_dark``: Beine zu den Füßen hin dunkler."""
    cells = ScaleCells(cell, seed, stretch)

    def paint(ctx: Ctx):
        x, y, z = ctx.world
        down = facing_down(ctx)
        val, edge, dx, dy, dz = cells(x, y, z)
        # Wölbung: Licht von oben und leicht von hinten (Schuppen liegen wie Dachziegel nach hinten)
        if ctx.face in ("top", "bottom"):
            shade_dir = dz * 0.9 - dx * 0.2
        elif ctx.face in ("west", "east"):
            shade_dir = -dy * 0.8 + dz * 0.5
        else:
            shade_dir = -dy * 0.9
        v = 0.42 + bias + (val - 0.5) * var + (1.0 - down) * top_light + max(-0.6, min(0.6, shade_dir)) * relief
        v += 0.05 * min(1.0, edge * 2.0)
        is_line = edge < line
        if is_line:
            v -= line_depth * (1.0 - edge / line)
        if foot_dark > 0:
            v -= foot_dark * max(0.0, min(1.0, (y - 4.0) / 20.0))
        if hot is not None and ctx.face != "top":
            start, width = hot
            t = (down - start) / max(1e-3, 1.0 - start)
            if t > 0 and edge < width * min(1.0, t * 1.4):
                g = 1.0 - edge / (width * min(1.0, t * 1.4))
                col = pick_smooth(ctx.pal[hot_key], 0.3 + 0.55 * g * min(1.0, t * 2))
                ctx.glow = col
                return col
        col = pick_smooth(ctx.pal[back], v)
        is_belly = ctx.face == "bottom" or (down > belly_from and ctx.face in ("west", "east", "front", "back"))
        if belly_world_y is not None:
            is_belly = is_belly and y > belly_world_y
        if is_belly:
            t = 1.0 if ctx.face == "bottom" else min(1.0, (down - belly_from) / max(1e-3, 1.0 - belly_from) * 1.5)
            col = mix(col, plate_color(ctx, belly), t)
        return edge_ao(ctx, col, ao)

    return paint


def _tag_progress(ctx: Ctx) -> float:
    """Fortschritt Basis -> Spitze aus dem Tag eines Stachels/Horns (sonst entlang der längsten Achse)."""
    tag = ctx.cube.tag
    lx, ly, lz = ctx.local
    if tag.startswith("sp:"):
        return max(0.0, min(1.0, (0.6 - ly) / float(tag[3:])))
    if tag.startswith("hn:"):
        _, i, n, length, direction = tag.split(":")
        a = max(0.0, min(1.0, lz * float(direction) / float(length)))
        return (int(i) - 1 + a) / int(n)
    if tag == "cl":
        oz, d = ctx.cube.origin[2], ctx.cube.size[2]
        return max(0.0, min(1.0, (oz + d - lz) / d))
    w, h, d = ctx.cube.size
    axis = max(range(3), key=lambda k: (w, h, d)[k])
    o = ctx.cube.origin[axis]
    size = (w, h, d)[axis] or 1.0
    return max(0.0, min(1.0, (ctx.local[axis] - o) / size))


def crystal2(key: str = "crystal", glow: float = 0.55, facet: float = 2.2, base: float = 0.32, span: float = 0.45) -> callable:
    """Kristall: schräge Facetten (abwechselnd hell/dunkel), helle Kanten, zur Spitze heller; leuchtet gedämpft
    (``glow`` = Anteil der Farbe in der Leucht-Textur, 0 = gar nicht)."""

    def paint(ctx: Ctx):
        prog = _tag_progress(ctx)
        lx, ly, lz = ctx.local
        k = ((lx * 0.8 + lz * 0.6 - ly * 0.45) / facet) % 1.0
        v = base + span * prog + (0.1 if k < 0.45 else -0.05)
        if ctx.s == 0 or ctx.s == ctx.w - 1 or ctx.t == 0:
            v += 0.16
        col = pick_smooth(ctx.pal[key], v)
        if glow > 0:
            ctx.glow = shade(col, glow)
        return col

    return paint


def seam_plates(key: str = "belly", glow_key: str = "glow", seam: float = 0.72, front_glow: bool = True,
                tint: float = 0.3) -> callable:
    """Brustplatten mit leuchtenden Fugen (Feuer-, Frost-, Blitzsack); von vorn scheint das Leuchten durch."""

    def paint(ctx: Ctx):
        col = plate_color(ctx, key)
        ox, w = ctx.cube.origin[0], ctx.cube.size[0]
        xr = abs((ctx.local[0] - ox) / w * 2 - 1)
        heat = max(0.0, 1.0 - xr * 1.1)
        if ctx.face in ("bottom", "west", "east"):
            k = (ctx.world[2] / 2.4) % 1.0
            if k > seam and heat > 0.08:
                mid = (1.0 + seam) / 2
                g = pick_smooth(ctx.pal[glow_key], 0.35 + 0.6 * heat * (1 - abs(k - mid) / (1.0 - mid)))
                ctx.glow = g
                return g
        if front_glow and ctx.face == "front":
            ry = (ctx.local[1] - ctx.cube.origin[1]) / max(ctx.cube.size[1], 1e-3)
            g = heat * (0.4 + 0.6 * ry)
            if g > 0.25:
                c = pick_smooth(ctx.pal[glow_key], 0.2 + 0.6 * g)
                ctx.glow = c
                return c
        return mix(col, pick_smooth(ctx.pal[glow_key], 0.1), tint * (1 + heat))

    return paint
