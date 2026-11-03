"""
Rauchqualle (statt Ghast) auf dem Skelett des Vanilla-Ghasts: derselbe Körperwürfel (16 Einheiten, im Spiel ×4,5) und
dieselben neun Fangarme an denselben Stellen wie GhastModel (Fangarme am Wurzelknochen), damit das Wehen der Fangarme
greift. Eigenes Aussehen:

  * ein düsterer Ballen aus Qualm mit doppelter Texturauflösung: die Rauchballen sind als Kugeln im Raum gedacht und
    werden über die Kanten hinweg schattiert – oben fahl, unten vom Feuer des Nethers angeglüht, dazwischen
    Rauchschlieren; hier und da glimmt ein Funke im Qualm
  * oben eine Haube und unten ein Kranz aus Rauchballen, deren Umriss die Ballen nachzeichnet (der Würfel wirkt rund)
  * die Fangarme sind Rauchfäden, die nach unten dünner werden und in einer glimmenden Spitze enden
  * ein trauriges Gesicht: geschlossene, schwere Lider, aus denen glühende Tränen rinnen, ein bekümmerter Mund – beim
    Feuerspeien reißt sie die glühenden Augen auf und das Maul steht offen voller Feuer (eigene Textur „_feuer“)

Drei Gestalten: Aschenqualle (aschgrau, Glut im Qualm), Glutqualle (kohlschwarz mit glühenden Rissen, in den
Basaltdeltas) und Seelenqualle (blaugrau mit Seelenfeuer, im Seelensandtal).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, hexc, mix, noise3

_EMBER = (hexc("#c2410f"), hexc("#ff7a1c"), hexc("#ffb347"), hexc("#ffe7a3"))
_SOUL = (hexc("#147b80"), hexc("#2fc9c1"), hexc("#7ff2e6"), hexc("#dcfffa"))

PALETTES = {
    "asche": {
        "tones": [hexc(c) for c in ("#2c2827", "#3a3533", "#4a4442", "#5c5552", "#706864", "#857c77", "#9b918b", "#b2a8a0",
                                    "#c8beb5")],
        "under": hexc("#b4552a"), "lid": hexc("#1d1918"), "fire": _EMBER, "embers": 0.004, "cracks": False},
    "glut": {
        "tones": [hexc(c) for c in ("#151110", "#1f1918", "#2a2220", "#362c28", "#433630", "#52423a", "#634f44", "#765d4f",
                                    "#8a6c5a")],
        "under": hexc("#d4621f"), "lid": hexc("#0d0a09"), "fire": _EMBER, "embers": 0.003, "cracks": True},
    "seele": {
        "tones": [hexc(c) for c in ("#1f2a30", "#29373e", "#35464e", "#435760", "#536a73", "#647e86", "#78939a", "#8ea9ae",
                                    "#a6c0c3")],
        "under": hexc("#2d939a"), "lid": hexc("#121a1e"), "fire": _SOUL, "embers": 0.004, "cracks": False},
}
PALETTES.update({f"{k}_feuer": v for k, v in list(PALETTES.items())})

LOOKS = ("asche", "glut", "seele")

# (x, z, Länge) der neun Fangarme: Lage wie bei Vanilla, die Längen eigene
_TENTACLES = ((-3.75, -5.0, 9), (1.25, -5.0, 12), (6.25, -5.0, 8), (-6.25, 0.0, 11), (-1.25, 0.0, 13), (3.75, 0.0, 10),
              (-3.75, 5.0, 12), (1.25, 5.0, 9), (6.25, 5.0, 11))
_BODY_Y = 17.6
_TENTACLE_Y = 24.6


def build() -> Model:
    m = Model("ghast", 128, 128)
    m.tex_res = 2
    m.bone("body", pivot=(0, _BODY_Y, 0))
    m.cube("body", (-8, -8, -8), (16, 16, 16), "smoke")
    m.cube("body", (-9.5, 1.0, -9.5), (19.0, 8.75, 19.0), "puff")              # Kranz aus Rauchballen unten
    m.cube("body", (-9.25, -9.25, -9.25), (18.5, 5.5, 18.5), "cap")           # Haube oben
    for i, (x, z, length) in enumerate(_TENTACLES):
        name = f"tentacle{i}"
        m.bone(name, pivot=(x, _TENTACLE_Y, z))
        m.cube(name, (-1, 0, -1), (2, length, 2), "wisp")
    return m


# =============================================================================================
# Rauchballen als Kugeln im Raum (Körper-Koordinaten: Würfel -8..8, y nach unten)
# =============================================================================================

def _spheres() -> list[tuple[float, float, float, float, int]]:
    """
    Vier Lagen Rauchballen (x, y, z, Radius, Lage): hinten große Ballen als Kern, davor mittlere, die als runde Ballen
    hervortreten, kleine Bäusche, die den Qualm zerfasern, und ein Saum unten und oben, der über den Rand quillt.
    """
    import random
    rnd = random.Random(1660)
    out: list[tuple[float, float, float, float, int]] = []

    def on_cube(a, y_range=None, faces=6):
        face = rnd.randrange(faces)
        u, v = rnd.uniform(-a, a), rnd.uniform(-a, a)
        if y_range is not None and face < 4:
            v = rnd.uniform(*y_range)
        return [(a, v, u), (-a, v, u), (u, v, a), (u, v, -a), (u, a, v), (u, -a, v)][face]

    def place(count, a, r_range, layer, spacing, y_range=None, faces=6, keep_face=False):
        added = 0
        for _ in range(9000):
            if added >= count:
                break
            x, y, z = on_cube(a, y_range, faces)
            if keep_face and z < -a + 0.5 and -4.0 < x < 4.0 and -5.0 < y < 5.0:
                continue                                                  # das Gesicht bleibt frei von kleinen Bäuschen
            r = rnd.uniform(*r_range)
            if any(lay == layer and (x - ox) ** 2 + (y - oy) ** 2 + (z - oz) ** 2 < (spacing * (r + orr)) ** 2
                   for ox, oy, oz, orr, lay in out):
                continue
            out.append((x, y, z, r, layer))
            added += 1

    for sx in (-1, 1):                                                    # Kern: acht große Ballen
        for sy in (-1, 1):
            for sz in (-1, 1):
                out.append((3.0 * sx, 3.0 * sy, 3.0 * sz, 7.3, 0))
    place(34, 6.0, (3.6, 4.8), 1, 0.6)                                    # mittlere Ballen
    place(26, 8.2, (1.8, 2.8), 2, 0.85, keep_face=True)                   # kleine Bäusche
    place(26, 8.2, (2.8, 3.6), 3, 0.62, y_range=(5.0, 7.6), faces=4)      # Saum unten
    place(10, 8.0, (2.5, 3.2), 3, 0.62, faces=1)                          # … und unter dem Bauch
    place(18, 7.9, (2.6, 3.4), 3, 0.62, faces=6, y_range=(-7.9, -5.6))    # Haube oben
    return out


_SPHERES = _spheres()
_SKY = (lambda v: tuple(c / math.sqrt(sum(k * k for k in v)) for c in v))((-0.35, -1.0, -0.55))
_LAVA = (lambda v: tuple(c / math.sqrt(sum(k * k for k in v)) for c in v))((0.25, 1.0, -0.35))
_AXES = {"east": (0, 1.0), "west": (0, -1.0), "bottom": (1, 1.0), "top": (1, -1.0), "back": (2, 1.0), "front": (2, -1.0)}
_CRACKS = Voronoi3D(4.2, 77)


def _billow(p: tuple[float, float, float], face: str):
    """Welcher Ballen ist von außen gesehen zuoberst? (Höhe, Normale, Umrisslinie 0/1/2) wie bei der Wolkenqualle."""
    axis, sign = _AXES[face]
    best = second = -1e9
    owner = other = None
    for s in _SPHERES:
        c = s[:3]
        r = s[3]
        d2 = sum((p[i] - c[i]) ** 2 for i in range(3) if i != axis)
        if d2 >= r * r:
            continue
        h = c[axis] * sign + math.sqrt(r * r - d2)
        if h > best:
            second, other = best, owner
            best, owner = h, s
        elif h > second:
            second, other = h, s
    if owner is None:
        return -1e9, (0.0, -1.0, 0.0), 0, None
    c = owner[:3]
    q = list(p)
    q[axis] = best * sign
    normal = tuple((q[i] - c[i]) / owner[3] for i in range(3))
    line = 0
    if other is not None and other[4] > owner[4]:
        gap = best - second
        below = p[1] > other[1] + other[3] * 0.2
        if gap < (0.8 if below else 0.35):
            line = 2 if below else 1
    return best, normal, line, tuple(q)


def _body_pos(ctx: Ctx) -> tuple[float, float, float]:
    x, y, z = ctx.world
    return x, y - _BODY_Y, z


def _tone(ctx: Ctx, b: float):
    tones = ctx.pal["tones"]
    b = max(0.0, min(0.999, b))
    return tones[int(b * len(tones))]


def _shooting(ctx: Ctx) -> bool:
    return ctx.params.get("variant", "").endswith("_feuer")


def _fire(ctx: Ctx, k: float):
    """Feuer der Gestalt: 0 = dunkle Glut … 1 = weißglühend."""
    f = ctx.pal["fire"]
    k = max(0.0, min(0.999, k))
    return f[int(k * len(f))]


def _smoke_color(ctx: Ctx, p, n, line: int, q, face_zone: bool = False):
    sky = n[0] * _SKY[0] + n[1] * _SKY[1] + n[2] * _SKY[2]
    lava = n[0] * _LAVA[0] + n[1] * _LAVA[1] + n[2] * _LAVA[2]
    b = 0.54 + 0.17 * sky - p[1] * 0.011
    # Schlieren: zähe, helle Rauchfäden, die sich um die Ballen winden, dazwischen dunklere Täler
    flow = math.sin(p[0] * 0.42 + p[2] * 0.3 + p[1] * 0.8 + noise3(p[0] * 0.22, p[1] * 0.22, p[2] * 0.22, 11) * 6.5)
    if flow > 0.82:
        b += 0.07
    elif flow > 0.6:
        b += 0.03
    elif flow < -0.75:
        b -= 0.04
    b += (noise3(p[0] * 0.5, p[1] * 0.5, p[2] * 0.5, 5) - 0.5) * 0.05
    if line and not face_zone:
        b -= 0.07 if line == 2 else 0.035
    if ctx.face == "top":
        b += 0.05
    col = _tone(ctx, b)
    # von unten angeglüht: das Feuer des Nethers auf der Unterseite der Ballen
    low = max(0.0, lava) * max(0.0, min(1.0, (p[1] + 3.0) / 12.0))
    if ctx.face == "bottom":
        low = max(low, 0.6)
    if low > 0:
        col = mix(col, ctx.pal["under"], min(0.5, low * 0.55))
    # Glutnester (Glutqualle): im Qualm der unteren Hälfte glimmt es von innen
    if ctx.pal.get("cracks") and not face_zone and ctx.face != "top" and p[1] > 0.0:
        heat = noise3(p[0] * 0.7, p[1] * 0.7, p[2] * 0.7, 23) * min(1.0, p[1] / 7.0 + 0.35)
        if heat > 0.6:
            k = min(1.0, (heat - 0.6) / 0.2)
            c = _fire(ctx, 0.05 + k * 0.6)
            if k > 0.35:
                ctx.glow = c
                return c
            return mix(col, c, 0.5)
    # Funken im Qualm
    if not face_zone and hash01(round(p[0] * 2), round(p[1] * 2), round(p[2] * 2), ctx.face, "funke") < ctx.pal["embers"]:
        c = _fire(ctx, 0.55 + hash01(round(p[0] * 2), round(p[2] * 2), "hell") * 0.4)
        ctx.glow = c
        return c
    return col


# =============================================================================================
# Gesicht (Vorderseite, 32×32 Pixel)
# =============================================================================================

def _face(ctx: Ctx):
    """Lider, Tränen und Mund (ruhig) bzw. aufgerissene Augen und Feuermaul (beim Feuerspeien); None: kein Gesicht."""
    s, t = ctx.s, ctx.t
    pal = ctx.pal
    lid = pal["lid"]
    rim = pal["tones"][6]
    shooting = _shooting(ctx)
    for side, (left, right) in enumerate(((4, 12), (19, 27))):
        if not left <= s <= right:
            continue
        rel = (s - left) / (right - left)                                 # 0 = links, 1 = rechts
        outer = 1.0 - rel if side == 0 else rel                           # 1 = äußerer Winkel
        if shooting:
            # zornige Brauen (zur Mitte hin abfallend) …
            brow = 7 + round((1.0 - outer) * 2.0)
            if t in (brow, brow + 1) and 0.05 < outer < 0.98:
                return lid
            # … und weit aufgerissene, glühende Augen mit schmaler Pupille
            cx = (left + right) / 2.0
            dx = (s + 0.5 - (cx + 0.5)) / 3.8
            dy = (t + 0.5 - 13.5) / 3.2
            d = dx * dx + dy * dy
            if d <= 1.0:
                if abs(s + 0.5 - (cx + 0.5)) < 0.8 and abs(t + 0.5 - 13.5) < 2.4:
                    return lid                                            # Pupille
                c = _fire(ctx, 0.95 - d * 0.6)
                ctx.glow = c
                return c
            if d <= 1.45:
                return lid
            # Tränenspuren, erloschen
            for ts in ((left + 2, left + 6) if side == 0 else (right - 6, right - 2)):
                if s == ts and 17 <= t <= 21:
                    return mix(lid, pal["tones"][3], 0.45)
            continue
        # fest zugekniffene Augen (zwei Bögen) …
        if side == 0 and not 5 <= s <= 11 or side == 1 and not 20 <= s <= 26:
            continue
        col = s - (5 if side == 0 else 20)                                # 0..6
        arc = 11 + (2, 1, 0, 0, 0, 1, 2)[col]
        if arc <= t <= arc + 1:
            return lid
        if t == arc - 1 and 1 <= col <= 5:
            return rim                                                    # Licht über dem Lid
        # … aus denen glühende Tränen in zwei Bächen über die Wangen rinnen
        stream = (1, 2) if side == 0 else (4, 5)
        if col in stream and 14 <= t <= 21:
            k = (t - 14) / 7.0
            if col == stream[1 if side == 0 else 0] and t > 18:
                continue                                                  # der Bach wird unten schmaler
            c = _fire(ctx, 0.9 - k * 0.45)
            ctx.glow = c
            return c
        if col == (1 if side == 0 else 5) and t == 22:
            c = _fire(ctx, 0.5)                                           # ein Tropfen am Ende
            ctx.glow = c
            return c
    # der Mund
    if shooting:
        dx = (s + 0.5 - 16.0) / 6.4
        dy = (t + 0.5 - 23.0) / 3.8
        d = dx * dx + dy * dy
        if d <= 1.0:
            c = _fire(ctx, 0.98 - d * 0.75)
            ctx.glow = c
            return c
        if d <= 1.32:
            return lid
    else:
        # ein klagender, kleiner offener Mund
        dx = (s + 0.5 - 16.0) / 2.6
        dy = (t + 0.5 - 22.5) / 2.0
        d = dx * dx + dy * dy
        if d <= 0.55:
            return mix(lid, ctx.pal["under"], 0.25)
        if d <= 1.0:
            return lid
    return None


# =============================================================================================
# Materialien
# =============================================================================================

def _smoke(ctx: Ctx):
    p = _body_pos(ctx)
    depth, n, line, q = _billow(p, ctx.face)
    face_zone = ctx.face == "front" and 3 <= ctx.s <= 28 and 5 <= ctx.t <= 27
    if ctx.face == "front":
        f = _face(ctx)
        if f is not None:
            return f
    return _smoke_color(ctx, p, n, line, q, face_zone)


def _puff(ctx: Ctx):
    """Kranz unten: nur, wo eine Rauchkugel hinreicht – so zeichnet der Umriss die Ballen nach."""
    if ctx.face == "top":
        return None
    p = _body_pos(ctx)
    height, n, line, q = _billow(p, ctx.face)
    axis, sign = _AXES[ctx.face]
    if height < p[axis] * sign:
        return None
    if ctx.face == "front" and abs(p[0]) < 6.0 and p[1] < 5.5:
        return None                                                       # vor dem Mund kein Ballen
    return _smoke_color(ctx, p, n, line, q)


def _cap(ctx: Ctx):
    """Haube oben: wie der Kranz, die Unterseite bleibt offen."""
    if ctx.face == "bottom":
        return None
    p = _body_pos(ctx)
    height, n, line, q = _billow(p, ctx.face)
    axis, sign = _AXES[ctx.face]
    if height < p[axis] * sign:
        return None
    if ctx.face == "front" and abs(p[0]) < 7.0 and p[1] > -6.5:
        return None                                                       # über den Augen kein Ballen
    return _smoke_color(ctx, p, n, line, q)


def _wisp(ctx: Ctx):
    """
    Rauchfaden: oben so breit wie der Arm und in der Farbe der angeglühten Unterseite, darunter ein schmaler, gewellter
    Faden, der sich nach unten auflöst und in einer glimmenden Spitze endet.
    """
    face = ctx.face
    s, t, w, h = ctx.s, ctx.t, ctx.w, ctx.h
    name = ctx.cube.bone.name
    seed = int(name.removeprefix("tentacle")) if name.startswith("tentacle") else 0
    tones = ctx.pal["tones"]
    under = ctx.pal["under"]
    if face == "top":
        return mix(tones[2], under, 0.3)
    if face == "bottom":
        c = _fire(ctx, 0.6)
        ctx.glow = c
        return c
    k = (t + 0.5) / h
    if k < 0.28:
        b = 4 + (1 if s in (1, 2) else 0) - (1 if k > 0.18 else 0)
        return mix(tones[b], under, 0.15 + k * 0.6)
    wave = 1 if math.sin(t * 0.5 + seed * 1.9) > 0.3 else 0
    left = wave
    if t >= h - 2:
        if s == left or s == left + 1:
            c = _fire(ctx, 0.45 + (t - (h - 2)) * 0.3)                    # glimmende Spitze
            ctx.glow = c
            return c
        return None
    if k > 0.8:
        return mix(tones[2], under, 0.5) if s == left + 1 - wave else None
    if s not in (left, left + 1):
        return None
    b = 3 if s == left else 4
    return mix(tones[b], under, 0.2 + (k - 0.28) * 0.5)


def materials(variant: str) -> dict:
    return {"smoke": _smoke, "puff": _puff, "cap": _cap, "wisp": _wisp}


def hidden(variant: str) -> list[str]:
    return []
