"""
Riffwächter (statt Wächter) und Uralter Riffwächter (statt Großer Wächter) auf dem Skelett des Vanilla-Wächters:
gleiche Knochen, Drehpunkte und Grundquader wie GuardianModel ("head" mit dem Leib, "spike0".."spike11" an den
Vanilla-Stellen und -Winkeln – das Modell schiebt sie jedes Bild aus den Vanilla-Konstanten heraus und wieder hinein –,
"eye" vorn, "tail0" -> "tail1" -> "tail2"). Der Uralte ist dieselbe Geometrie, im Renderer 2,35-fach vergrößert
(wie Vanilla createElderGuardianLayer).

Eigene Gestalt: ein gepanzerter Riff-Fisch mit einem einzigen großen Auge unter zornig gesenkten Brauenwülsten,
einem Maul mit Fangzähnen, Knochenplatten mit Fugen, gestreiften Seeigel-Stacheln (Schaft mit dünner Spitze),
Rückenkamm, Brustflossen und gegabelter Schwanzflosse.

  * riffwaechter: türkisgrüne Platten, korallenrote Flossen und Korallenpolypen, Stacheln elfenbein mit
    dunkelgrünen Ringen, bernsteingelb leuchtendes Auge mit Schlitzpupille
  * riffwaechter_alt: elfenbeingraue, ausgebleichte Platten mit dunklen Fugen, dicht mit Seepocken besetzt, Tang
    hängt herab, verblichene Flossen, türkis leuchtendes Auge
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, ramp, rel_y

PALETTES = {
    "riffwaechter": {
        "plate": ramp("#0a2224", "#9ce0bc", steps=9), "belly": ramp("#24504a", "#e2f2d2", steps=7),
        "seam": ramp("#030c0e", "#0e2626", steps=3), "fin": ramp("#6a1404", "#ffc08a", steps=7),
        "ray": ramp("#5a1000", "#e0602a", steps=5), "spine": ramp("#5e5240", "#fbf4e2", steps=6),
        "band": ramp("#0a2422", "#2e6a5a", steps=4), "eye": ramp("#7a2a00", "#ffec8a", steps=6),
        "pupil": ramp("#0e0400", "#2a1004", steps=3), "socket": ramp("#02080c", "#123038", steps=5),
        "tooth": ramp("#6a6048", "#fff8e4", steps=5), "growth": ramp("#7a1a1a", "#ffb08a", steps=6),
        "kelp": ramp("#14220c", "#7a9a3a", steps=5), "flesh": ramp("#2a0806", "#7a2a1e", steps=4),
    },
    "riffwaechter_alt": {
        "plate": ramp("#36322a", "#f2ecdc", steps=9), "belly": ramp("#4a463c", "#f8f4e8", steps=7),
        "seam": ramp("#0e0c0a", "#2a241e", steps=3), "fin": ramp("#28362e", "#a8c0a4", steps=7),
        "ray": ramp("#141e18", "#5a7060", steps=5), "spine": ramp("#5a5244", "#fffaee", steps=6),
        "band": ramp("#2a241e", "#7a6e5e", steps=4), "eye": ramp("#04403e", "#b4fff4", steps=6),
        "pupil": ramp("#010808", "#0a1e1e", steps=3), "socket": ramp("#020606", "#16262a", steps=5),
        "tooth": ramp("#5a5448", "#fffcf2", steps=5), "growth": ramp("#3e3a32", "#fdfaf0", steps=6),
        "kelp": ramp("#12200a", "#86a842", steps=6), "flesh": ramp("#1a0e0c", "#5a3a30", steps=4),
    },
}
for _p in PALETTES.values():
    _p["back"] = _p["plate"]

# Vanilla-Konstanten der Stacheln (GuardianModel): Lage am Rand des Leibs und Winkel in Vielfachen von Pi
_SPIKE_X_ROT = (1.75, 0.25, 0.0, 0.0, 0.5, 0.5, 0.5, 0.5, 1.25, 0.75, 0.0, 0.0)
_SPIKE_Y_ROT = (0.0, 0.0, 0.0, 0.0, 0.25, 1.75, 1.25, 0.75, 0.0, 0.0, 0.0, 0.0)
_SPIKE_Z_ROT = (0.0, 0.0, 0.25, 1.75, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.75, 1.25)
_SPIKE_X = (0.0, 0.0, 8.0, -8.0, -8.0, 8.0, 8.0, -8.0, 0.0, 0.0, 8.0, -8.0)
_SPIKE_Y = (-8.0, -8.0, -8.0, -8.0, 0.0, 0.0, 0.0, 0.0, 8.0, 8.0, 8.0, 8.0)
_SPIKE_Z = (8.0, -8.0, 0.0, 0.0, -8.0, -8.0, 8.0, 8.0, 8.0, -8.0, 0.0, 0.0)


def _spike_offset(i: int) -> float:
    # GuardianModel.getSpikeOffset(i, 0, 0): 1 + cos(i) * 0.01
    return 1.0 + math.cos(i) * 0.01


def build() -> Model:
    m = Model("guardian", 128, 128)
    # Leib (Vanilla-Grundquader): Rumpf, Wangenplatten, Rücken- und Bauchplatte
    m.bone("head")
    m.cube("head", (-6, 10, -8), (12, 12, 16), "body")
    m.cube("head", (-8, 10, -6), (2, 12, 12), "flank")
    m.cube("head", (6, 10, -6), (2, 12, 12), "flank")
    m.cube("head", (-6, 8, -6), (12, 2, 12), "dorsal")
    m.cube("head", (-6, 22, -6), (12, 2, 12), "ventral")

    # Stacheln: Vanilla-Lage und -Winkel; die Spitze zeigt bei allen zwölf nach lokal -Y (nach außen).
    # Schaft 2x5x2 (davon 2,5 sichtbar), dünne Spitze 1x3x1 – nach außen 1 länger als bei Vanilla.
    for i in range(12):
        off = _spike_offset(i)
        name = f"spike{i}"
        m.bone(name, "head", pivot=(_SPIKE_X[i] * off, 16.0 + _SPIKE_Y[i] * off, _SPIKE_Z[i] * off),
               rotation=(180.0 * _SPIKE_X_ROT[i], 180.0 * _SPIKE_Y_ROT[i], 180.0 * _SPIKE_Z_ROT[i]))
        m.cube(name, (-1, -2.5, -1), (2, 5, 2), "spine")
        m.cube(name, (-0.5, -5.5, -0.5), (1, 3, 1), "spine_tip")

    # Auge: 5x4 statt 2x2 (wandert wie bei Vanilla um bis zu 2 nach links/rechts und 1 nach unten)
    m.bone("eye", "head", pivot=(0, 0, -8.25))
    m.cube("eye", (-2.5, 14, 0), (5, 4, 1), "eye")

    # Schwanz wie Vanilla; die Flosse von tail2 als gegabelte Schwanzflosse bemalt
    m.bone("tail0", "head")
    m.cube("tail0", (-2, 14, 7), (4, 4, 8), "tail")
    m.bone("tail1", "tail0", pivot=(-1.5, 0.5, 14))
    m.cube("tail1", (0, 14, 0), (3, 3, 7), "tail")
    m.bone("tail2", "tail1", pivot=(0.5, 0.5, 6))
    m.cube("tail2", (0, 14, 0), (2, 2, 6), "tail")
    m.cube("tail2", (1, 10.5, 3), (1, 9, 9), "fluke")

    # Gesicht: zwei zornig gesenkte Brauenwülste und ein Maul mit Fangzähnen
    m.bone("brow_right", "head", pivot=(-0.3, 13.7, -8.2), rotation=(0, 0, 15))
    m.cube("brow_right", (-5.4, -1.0, -0.7), (5.4, 1.8, 1.3), "brow")
    m.bone("brow_left", "head", pivot=(0.3, 13.7, -8.2), rotation=(0, 0, -15))
    m.cube("brow_left", (0, -1.0, -0.7), (5.4, 1.8, 1.3), "brow")
    m.bone("jaw", "head")
    m.cube("jaw", (-4.5, 19.6, -8.7), (9, 2.2, 1), "jaw")

    # Flossen: Rückenkamm und zwei Brustflossen (feste Teile des Leibs)
    m.bone("crest", "head")
    m.cube("crest", (-0.5, 3.0, -4.5), (1, 5, 11), "crest")
    m.bone("fin_right", "head", pivot=(-8, 18.5, -3), rotation=(0, -22, 0))
    m.cube("fin_right", (-1, -2.5, 0), (1, 6, 8), "pectoral")
    m.bone("fin_left", "head", pivot=(8, 18.5, -3), rotation=(0, 22, 0))
    m.cube("fin_left", (0, -2.5, 0), (1, 6, 8), "pectoral")

    # Bewuchs: beim Riffwächter Korallenpolypen, beim Uralten Seepocken
    m.bone("growth", "head")
    for origin, size in (((2.5, 6.8, -2.5), (2, 1.2, 2)), ((-4.5, 7.0, 2.0), (1.5, 1.0, 1.5)), ((8, 12.5, 0.5), (1, 1.6, 1.6)),
                         ((-9, 16.5, -3.5), (1, 1.4, 1.4)), ((-2.0, 7.2, 4.6), (1.2, 0.8, 1.2))):
        m.cube("growth", origin, size, "growth")
    # nur beim Uralten: dichter Seepocken-Besatz und herabhängender Tang
    m.bone("crust", "head")
    for origin, size in (((-3.5, 6.8, -4.6), (2, 1.2, 2)), ((0.4, 7.1, 1.0), (1.4, 0.9, 1.4)), ((4.0, 6.9, 3.0), (1.6, 1.1, 1.6)),
                         ((8, 16.0, -4.5), (1, 1.6, 1.6)), ((8, 18.5, 2.5), (0.9, 1.2, 1.2)), ((-9, 12.0, 1.5), (1, 1.8, 1.8)),
                         ((-9, 19.0, 3.0), (0.9, 1.3, 1.3)), ((-5.0, 24, -2.5), (1.6, 0.9, 1.6)), ((2.5, 24, 3.0), (1.2, 0.8, 1.2)),
                         ((6.2, 14.0, 6.2), (1.2, 1.2, 1.0)), ((-6.6, 20.0, -7.4), (1.0, 1.0, 1.0))):
        m.cube("crust", origin, size, "barnacle")
    for origin, size in (((-4.5, 24, -3.5), (1, 7, 1)), ((3.5, 24, 1.5), (1, 9, 1)), ((-1.2, 24, 4.0), (1, 5, 1)),
                         ((6.4, 22, -4.0), (1, 6, 1)), ((-7.2, 22, 2.5), (1, 8, 1)), ((1.0, 24, -4.6), (1, 4, 1))):
        m.cube("crust", origin, size, "kelp")
    return m


def hidden(variant: str) -> list[str]:
    return [] if variant == "riffwaechter_alt" else ["crust"]


# =============================================================================================
# Malen
# =============================================================================================

def _c(r: list, i: float):
    return r[max(0, min(len(r) - 1, int(round(i))))]


def _alt(ctx: Ctx) -> bool:
    return ctx.params.get("variant") == "riffwaechter_alt"


def _grid(ctx: Ctx) -> tuple[float, float, float]:
    """Lage auf dem Plattenraster einer Fläche: (quer a, längs b, Überlappungsrichtung). Die Reihen laufen an den
    Seiten von oben nach unten (y ab 10, je 4 hoch), oben von vorn nach hinten (z ab -6) – wie Fischschuppen."""
    x, y, z = ctx.world
    face = ctx.face
    if face in ("west", "east"):
        return z, y - 10.0, 1.0
    if face in ("front", "back"):
        return x, y - 10.0, 1.0
    return x, z + 6.0, 1.0


def _plate(ctx: Ctx) -> tuple[int, int, float, float]:
    """(Reihe, Platte, Lage in der Platte quer 0..1, längs 0..1); Platten 4x4, jede zweite Reihe um 2 versetzt."""
    a, b, _ = _grid(ctx)
    row = int(math.floor(b / 4.0))
    a2 = a + (2.0 if row % 2 else 0.0) + 8.0
    col = int(math.floor(a2 / 4.0))
    return row, col, (a2 / 4.0) % 1.0, (b / 4.0) % 1.0


def _armor(ctx: Ctx, key: str = "plate", base: float = 4.6):
    """Knochenplatten im Schuppenraster: heller Oberrand, Schattenkante unten, feine Fuge links; Rücken dunkler,
    Flanken heller, zum Bauch hin hell (Gegenschattierung wie bei Fischen)."""
    x, y, z = ctx.world
    pal = ctx.pal
    face = ctx.face
    row, col, fa, fb = _plate(ctx)
    hy = max(0.0, min(1.0, (y - 8.0) / 16.0))                            # 0 = Rückenlinie, 1 = Bauchlinie
    var = (hash01(row, col, int(ctx.cube.origin[0] > 0), 4711) - 0.5) * 0.5
    i = base + var - (0.5 - hy) * 1.8
    if face == "top":
        i += 0.5
    elif face == "bottom":
        i -= 0.3
    # Schuppe 4x4: Oberrand im Licht, unten gerundet (Ecken dunkel), Schatten dort, wo die nächste Reihe überlappt
    edge = fa < 0.25 or fa >= 0.75
    if fb < 0.25:
        i += 1.5 if fa >= 0.25 else 0.4
    elif fb < 0.5:
        i += -0.5 if fa < 0.25 else 0.0
    elif fb < 0.75:
        i += -1.1 if edge else -0.25
    else:
        i += -1.6 if edge else -0.9
    alt = _alt(ctx)
    if alt:
        # Algen- und Kalkflecken je Platte (ganze Platten, kein Pixelrauschen)
        stain = hash01(row, col, 4723)
        if stain > 0.78:
            i -= 1.0
        elif stain < 0.16:
            i += 0.8
    out = _c(pal[key], i)
    if hy > 0.7 and face != "top":
        t = min(1.0, (hy - 0.7) / 0.3 * 1.25)
        j = 3.6 + var + (1.4 if fb < 0.25 else -1.0 if fb >= 0.75 else 0.0)
        out = mix(out, _c(pal["belly"], j), t)
    if face in ("west", "east") and 15.5 <= y < 16.5 and int(math.floor(z + 0.5)) % 2 == 0:
        out = _c(pal["belly"], 5.4)                                    # Seitenlinie: helle Poren
    if not alt and 0.25 <= fb < 0.75 and 0.25 <= fa < 0.75 and hash01(row, col, 4729) > 0.86 and hy < 0.7:
        out = _c(pal["growth"], 3.6)                                   # einzelne korallenrote Flecken
    return out


def _socket(ctx: Ctx):
    """Augenhöhle auf der Vorderseite: dunkel, nach innen noch dunkler, mit schwachem Schein um das Auge."""
    x, y, _ = ctx.world
    dx = x / 4.7
    dy = (y - 16.4) / 2.9
    r = math.hypot(dx, dy)
    if r > 1.0:
        return None
    pal = ctx.pal
    if r > 0.86:
        return _c(pal["socket"], 0)                                     # Rand der Höhle
    i = 3.8 - r * 3.0
    col = _c(pal["socket"], i)
    if r < 0.6:
        ctx.glow = mix(_c(pal["eye"], 1), (0, 0, 0, 255), 0.75)          # Widerschein des Auges
    return col


def _body(ctx: Ctx):
    if ctx.face == "front":
        col = _socket(ctx)
        if col is not None:
            return col
        return _face_plates(ctx)
    return _armor(ctx)


def _face_plates(ctx: Ctx):
    """Vorderseite um die Augenhöhle: ein erhabener Ring, außen das Schuppenraster."""
    x, y, _ = ctx.world
    r = math.hypot(x / 4.7, (y - 16.4) / 2.9)
    if r < 1.22:
        pal = ctx.pal
        top = y < 16.5
        return _c(pal["plate"], 6.2 if top else 4.6)                     # Ring: oben im Licht, unten matter
    return _armor(ctx, base=4.8)


def _flank(ctx: Ctx):
    return _armor(ctx, base=4.9)


def _dorsal(ctx: Ctx):
    if ctx.face == "bottom":
        return _c(ctx.pal["seam"], 1)
    return _armor(ctx, base=4.3)


def _ventral(ctx: Ctx):
    """Bauchplatte: helle Querschienen wie beim Fischbauch."""
    pal = ctx.pal
    if ctx.face == "top":
        return _c(pal["seam"], 1)
    x, y, z = ctx.world
    if ctx.face == "bottom":
        k = (z + 6.0) % 3.0
        i = 4.2 + (0.8 if k < 1.0 else 0.0) - abs(x) * 0.12
        if k >= 2.0:
            i = 2.0
        return _c(pal["belly"], i)
    return _armor(ctx, base=5.0)


def _spine(ctx: Ctx):
    """Seeigel-Stachel: geringelt, zum Leib hin dunkel (dort steckt er in der Haut)."""
    pal = ctx.pal
    u = -ctx.local[1]                                                    # Abstand vom Drehpunkt nach außen
    side = 0.0 if ctx.face in ("west", "front") else -0.5
    if ctx.face in ("top", "bottom"):
        side = -0.3
    if u < 0.0:
        return _c(pal["band"], 2.6 + side)                               # Ansatz (steckt meist im Leib)
    if 1.0 <= u < 2.0:
        return _c(pal["band"], 2.8 + side)
    return _c(pal["spine"], 3.0 + side + u * 0.35)


def _spine_tip(ctx: Ctx):
    pal = ctx.pal
    u = -ctx.local[1]
    side = 0.0 if ctx.face in ("west", "front") else -0.5
    if u > 4.6:
        return _c(pal["spine"], 5)                                     # helle Spitze
    if 3.6 < u <= 4.6:
        return _c(pal["band"], 3.0 + side)
    return _c(pal["spine"], 4.2 + side)


def _eye(ctx: Ctx):
    """Großes Auge 5x4: runde Ecken, goldene (bzw. türkise) Iris mit senkrechter Schlitzpupille und Glanzpunkt."""
    pal = ctx.pal
    if ctx.face != "front":
        return _c(pal["socket"], 1)
    s, t = ctx.s, ctx.t
    if (t in (0, 3)) and s in (0, 4):
        return None                                                      # runde Ecken (dahinter die Höhle)
    if s == 2:
        col = _c(pal["pupil"], 0 if t in (1, 2) else 1)
        return col
    if (s, t) == (1, 1):
        col = (255, 255, 240, 255)                                       # Glanzpunkt
        ctx.glow = col
        return col
    i = 5.0 - t * 0.9 - (0.6 if s in (0, 4) else 0.0)
    col = _c(pal["eye"], i)
    ctx.glow = col
    return col


def _tail(ctx: Ctx):
    """Schwanz: Ringe aus Platten (vorn hell abgesetzt, hinten etwas dunkler), oben dunkler, unten hell."""
    pal = ctx.pal
    x, y, z = ctx.world
    k = (z - 7.0) % 3.0
    ry = rel_y(ctx)
    i = 4.4 - (0.5 - ry) * 1.6
    if ctx.face == "top":
        i += 0.5
    elif ctx.face == "bottom":
        return _c(pal["belly"], 4.2 if k < 1.0 else 3.4)
    if k < 1.0:
        i += 1.3
    elif k >= 2.0:
        i -= 0.9
    col = _c(pal["plate"], i)
    if ry > 0.66:
        col = mix(col, _c(pal["belly"], 3.8 + (1.0 if k < 1.0 else 0.0)), 0.7)
    return col


def _fin_membrane(ctx: Ctx, ang: float, edge: float, n_rays: int = 5, base: float = 4.0):
    """Flossenhaut: ``ang`` 0..1 quer zu den Strahlen, ``edge`` 0..1 = Nähe zum Rand (dort heller).
    Dünne Strahlen, dazwischen klare, zum Rand hin hellere Haut; alte Flossen mit Rissen."""
    pal = ctx.pal
    if _alt(ctx):
        n = noise3(ctx.world[0] * 0.9, ctx.world[1] * 0.9, ctx.world[2] * 0.9, 4741)
        if n > 0.68 and edge > 0.35:
            return None                                                  # zerrissene alte Flossen
    k = ang * n_rays
    frac = k % 1.0
    i = base + edge * 1.8
    if frac < 0.18 and edge < 0.92:
        return _c(pal["ray"], 2.4 + edge * 1.6)
    if edge >= 0.92:
        i += 0.6                                                         # heller Saum
    return _c(pal["fin"], i)


def _fluke(ctx: Ctx):
    """Gegabelte Schwanzflosse (nur die beiden Seitenflächen; die dünnen Kanten bleiben frei)."""
    if ctx.face not in ("west", "east"):
        return None
    lx, ly, lz = ctx.local
    d = (lz - 3.0) / 9.0                                                # 0 = Ansatz, 1 = Hinterkante
    dy = ly - 15.0
    half = 0.9 + d * 3.7
    if abs(dy) > half:
        return None
    if d > 0.62 and abs(dy) < (d - 0.62) * 7.0:                          # Gabel
        return None
    ang = (math.atan2(dy, max(0.2, lz - 2.0)) / 1.2 + 1.0) / 2.0
    rim = max(abs(dy) / half, (d - 0.62) * 2.6 if abs(dy) < 3.0 else 0.0)
    edge = max(0.0, min(1.0, (max(d, rim) - 0.45) / 0.55))
    return _fin_membrane(ctx, ang, edge, 6, 3.6)


def _crest(ctx: Ctx):
    """Rückenkamm: Segel mit Strahlen, vorn am höchsten, mit leicht gewelltem oberem Rand."""
    if ctx.face not in ("west", "east"):
        return None
    lx, ly, lz = ctx.local
    f = (lz + 4.5) / 11.0                                                # 0 = vorn, 1 = hinten
    top = 8.0 - (0.8 + 4.2 * math.sin(math.pi * min(1.0, f * 1.25)) * (1.0 - 0.4 * f))
    if ly < top:
        return None
    h = max(0.0, min(1.0, (8.0 - ly) / max(0.5, 8.0 - top)))
    return _fin_membrane(ctx, f, h, 6, 3.6)


def _pectoral(ctx: Ctx):
    """Brustflosse (senkrecht, schräg nach hinten abstehend): vorn schmal am Leib, nach hinten breiter Fächer mit
    gerundeter, leicht eingekerbter Hinterkante."""
    if ctx.face not in ("west", "east"):
        return None
    lx, ly, lz = ctx.local
    f = lz / 8.0                                                         # 0 = Ansatz vorn, 1 = Hinterkante
    dy = (ly - 0.5) / 3.0                                                # -1 = oben, +1 = unten
    half = 0.25 + f * 0.85
    if abs(dy) > half:
        return None
    if f > 0.78 and (f - 0.78) * 4.5 > 1.0 - abs(dy) * 0.9:
        return None                                                      # gerundete Hinterkante
    if f > 0.86 and abs(dy) < 0.2:
        return None                                                      # kleine Kerbe
    ang = (math.atan2(dy, f + 0.2) / 1.3 + 1.0) / 2.0
    edge = max(0.0, min(1.0, max((f - 0.5) * 2.0, abs(dy) / max(half, 0.1) - 0.3)))
    return _fin_membrane(ctx, ang, edge, 5, 3.8)


def _brow(ctx: Ctx):
    """Brauenwulst: knochig, oben hell, darunter tiefer Schatten über dem Auge."""
    pal = ctx.pal
    if ctx.face == "bottom":
        return _c(pal["seam"], 0)
    if ctx.face == "top":
        return _c(pal["plate"], 6.2)
    if ctx.face == "front":
        s, t = ctx.s, ctx.t
        i = 5.6 - t * 1.4 + (hash01(s, 4751) - 0.5) * 0.6
        if _alt(ctx):
            i += 0.4
        col = _c(pal["plate"], i)
        if not _alt(ctx) and t == 0 and s % 2 == 0:
            col = _c(pal["growth"], 4)                                   # Korallenwarzen auf dem Wulst
        return col
    return _c(pal["plate"], 3.6)


def _jaw(ctx: Ctx):
    """Maul: dunkler Spalt mit Fangzähnen oben und unten."""
    pal = ctx.pal
    if ctx.face != "front":
        return _c(pal["plate"], 3.4) if ctx.face != "bottom" else _c(pal["belly"], 3)
    s, t = ctx.s, ctx.t
    if t == 0:
        # obere Zähne: spitz nach unten, ungleich lang
        if s in (1, 3, 5, 7):
            return _c(pal["tooth"], 4 if s in (3, 5) else 3)
        return _c(pal["plate"], 3.8)
    if t == 1:
        if s in (3, 5) or s in (2, 6) and _alt(ctx):
            return _c(pal["tooth"], 3)                                   # die langen Fangzähne reichen herunter
        if 1 <= s <= 7:
            return _c(pal["flesh"], 1 if s in (2, 4, 6) else 0)
        return _c(pal["plate"], 3.4)
    # Unterkiefer
    if s in (2, 4, 6):
        return _c(pal["tooth"], 3)
    return _c(pal["belly"], 3.6 - (0.6 if s in (0, 8) else 0.0))


def _growth(ctx: Ctx):
    """Korallenpolypen (Riffwächter) bzw. Seepocken (Uralter)."""
    pal = ctx.pal
    if _alt(ctx):
        return _barnacle(ctx)
    if ctx.face == "top":
        return _c(pal["growth"], 5 if hash01(ctx.u, ctx.v, 4761) > 0.5 else 4)
    ry = rel_y(ctx)
    return _c(pal["growth"], 4.2 - ry * 1.6 + (0.5 if (ctx.s + ctx.t) % 2 == 0 else 0.0))


def _barnacle(ctx: Ctx):
    """Seepocke: kalkweißer, gerippter Kegel; oben eine kleine graue Öffnung."""
    pal = ctx.pal
    if ctx.face == "top":
        if (ctx.s, ctx.t) == (ctx.w // 2, ctx.h // 2) or ctx.w <= 2 and (ctx.s, ctx.t) == (1, 1):
            return _c(pal["band"], 1)
        return _c(pal["growth"], 5 if (ctx.s + ctx.t) % 2 == 0 else 4)
    ry = rel_y(ctx)
    return _c(pal["growth"], 4.4 - ry * 2.2 + (0.6 if ctx.s % 2 == 0 else 0.0))


def _kelp(ctx: Ctx):
    """Tangstreifen: gewellt (einzelne Pixel fehlen), oben dunkel, zur Spitze heller."""
    pal = ctx.pal
    if ctx.face in ("top", "bottom"):
        return None
    ry = rel_y(ctx)
    wave = int(math.floor(ctx.local[1] * 1.0)) % 3
    if wave == 2 and ctx.face in ("west", "east"):
        return None
    if ry > 0.92 and hash01(ctx.u, 4771) > 0.5:
        return None
    return _c(pal["kelp"], 1.4 + ry * 3.0 + (0.6 if wave == 0 else 0.0))


def materials(variant: str) -> dict:
    return {
        "body": _body, "flank": _flank, "dorsal": _dorsal, "ventral": _ventral,
        "spine": _spine, "spine_tip": _spine_tip, "eye": _eye, "tail": _tail, "fluke": _fluke,
        "brow": _brow, "jaw": _jaw, "crest": _crest, "pectoral": _pectoral,
        "growth": _growth,
        # nur beim Uralten sichtbar (beim Riffwächter durchsichtig: gleiche Geometrie für beide)
        "barnacle": lambda ctx: _barnacle(ctx) if _alt(ctx) else None,
        "kelp": lambda ctx: _kelp(ctx) if _alt(ctx) else None,
    }
