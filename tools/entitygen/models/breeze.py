"""
Windgeist (statt Breeze) – Körper-Ebene: Knochen, Drehpunkte und Grundquader wie BreezeModel (ein Netz für drei
Ebenen: Körper = "head" + "rods", Wind = "wind_body" …, Augen = "eyes"). In diesem Modul tragen nur Kopf, Stäbe und
Augen Quader; der Wind steht in breeze_wind.py (gleiche Knochen, eigenes Bild, das im Spiel waagrecht wandert).

Eigene Gestalt: eine verwitterte, blasse Steinmaske mit zornig gesenkter Stirn und herabgezogenem Mund, ein
Sehschlitz-Band mit zwei hell leuchtenden Augen; darüber eine windzerzauste Kapuze aus schieferblauem Tuch mit
silbernem Saum, nach hinten wehende Bänder und zwei Federbüsche an den Seiten. Die drei Stäbe sind schwebende,
geschnitzte Runensteine mit Lederbindung und leuchtender Kristallspitze; die Runen glimmen türkis.

Augen: "eyes" doppelt den Kopf (Gesichtsquader und Sehschlitz-Band) wie bei Vanilla. Im Hauptbild sind diese Quader
völlig durchsichtig (Alpha 0); ihr Leuchten (die Augen) steht im Leuchtbild "<variante>_glow.png", das deshalb auch als
Augen-Textur der Augen-Ebene taugt (gleiche UVs, weil dieselbe Geometrie).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, mix, noise3, ramp, rel_y

PALETTES = {
    "windgeist": {
        "stone": ramp("#3a3e46", "#f2f0e8", steps=9), "crack": ramp("#10141a", "#3a4048", steps=4),
        "cloth": ramp("#141c2c", "#8eaccc", steps=8), "trim": ramp("#5a6878", "#f0f8ff", steps=6),
        "feather": ramp("#56606e", "#ffffff", steps=7), "tip": ramp("#1a6e8a", "#a8f4ff", steps=5),
        "rune": ramp("#0e5a78", "#d2fcff", steps=6), "eye": ramp("#3ab8e0", "#ffffff", steps=5),
        "leather": ramp("#24180e", "#8a6040", steps=5), "crystal": ramp("#2a8ab0", "#e6fdff", steps=6),
    },
}
for _p in PALETTES.values():
    _p["back"] = _p["stone"]

_D = math.degrees


def build() -> Model:
    m = Model("breeze", 128, 64)
    m.bone("body")
    m.bone("rods", "body", pivot=(0, 8, 0))
    for name, pivot, rot in (("rod_1", (2.5981, -3, 1.5), (_D(-2.7489), _D(-1.0472), _D(3.1416))),
                             ("rod_2", (-2.5981, -3, 1.5), (_D(-2.7489), _D(1.0472), _D(3.1416))),
                             ("rod_3", (0, -3, -3), (_D(0.3927), 0, 0))):
        # alle drei Stäbe haben dasselbe lokale Gerüst (um 120° versetzt): -Z zeigt nach außen, +Y am Stab entlang
        m.bone(name, "rods", pivot=pivot, rotation=rot)
        m.cube(name, (-1.5, 0.4, -3), (3, 6, 1.6), "runestone")           # Runenstein (Tafel, Rune nach außen)
        m.cube(name, (-0.5, 6.4, -2.7), (1, 2, 1), "crystal")            # leuchtende Kristallspitze

    m.bone("head", "body", pivot=(0, 4, 0))
    m.cube("head", (-5, -5, -4.2), (10, 3, 4), "band")                  # Sehschlitz-Band (Vanilla-Quader)
    m.cube("head", (-4, -8, -4), (8, 8, 8), "mask")                     # Steinmaske (Vanilla-Quader)
    m.bone("eyes", "head")                                              # Vanilla: dieselben Quader noch einmal
    m.cube("eyes", (-5, -5, -4.2), (10, 3, 4), "eyes")
    m.cube("eyes", (-4, -8, -4), (8, 8, 8), "eyes")

    # Kapuze: liegt eng um Scheitel, Hinterkopf und Seiten (bis zum Sehschlitz); nach hinten oben ein Zipfel
    m.bone("hood", "head")
    m.cube("hood", (-5.2, -8.9, -3.3), (10.4, 6.7, 8.1), "hood")
    m.cube("hood", (-3.5, -9.9, -1.5), (7, 1.0, 6.2), "hood_top")
    m.cube("hood", (-4.7, -9.3, -4.7), (9.4, 1.3, 1.6), "hood_brim")     # vorstehender Rand über der Stirn
    m.bone("hood_tip", "hood", pivot=(0, -9.0, 4.4), rotation=(-18, 0, 0))
    m.cube("hood_tip", (-2, -1.2, 0), (4, 2.2, 3.5), "hood_tip")
    m.cube("hood_tip", (-1, -0.8, 3.5), (2, 1.4, 3), "hood_tip")
    # wehende Bänder hinten
    m.bone("streamer_right", "hood", pivot=(-2.2, -5.0, 4.8), rotation=(16, -18, 0))
    m.cube("streamer_right", (-1, -0.5, 0), (2, 1, 9), "streamer")
    m.bone("streamer_left", "hood", pivot=(2.2, -5.0, 4.8), rotation=(10, 20, 0))
    m.cube("streamer_left", (-1, -0.5, 0), (2, 1, 7), "streamer")
    # Federbüsche an den Seiten, nach hinten oben gestrichen
    m.bone("plume_right", "hood", pivot=(-5.2, -6.2, 0.5), rotation=(-16, -30, 0))
    m.cube("plume_right", (-1, -2, 0), (1, 4, 11), "plume")
    m.bone("plume_left", "hood", pivot=(5.2, -6.2, 0.5), rotation=(-16, 30, 0))
    m.cube("plume_left", (0, -2, 0), (1, 4, 11), "plume")

    # Wind (nur die Knochen; die Quader stehen in breeze_wind.py)
    m.bone("wind_body")
    m.bone("wind_bottom", "wind_body", pivot=(0, 24, 0))
    m.bone("wind_mid", "wind_bottom", pivot=(0, -7, 0))
    m.bone("wind_top", "wind_mid", pivot=(0, -6, 0))
    return m


# =============================================================================================
# Malen
# =============================================================================================

def _c(r: list, i: float):
    return r[max(0, min(len(r) - 1, int(round(i))))]


def _stone(ctx: Ctx, base: float = 5.0):
    """Verwitterter heller Stein: große ruhige Flächen, wenige feine Risse, oben heller, unten kühler."""
    x, y, z = ctx.world
    pal = ctx.pal
    n = noise3(x * 0.35, y * 0.35, z * 0.35, 6101)
    i = base + (n - 0.5) * 1.2
    if ctx.face == "top":
        i += 0.8
    elif ctx.face == "bottom":
        i -= 1.4
    elif ctx.face in ("west", "east", "back"):
        i -= 0.6
    return _c(pal["stone"], i)


def _mask(ctx: Ctx):
    """Gesicht (Vorderseite 8x8): Zeile 0-2 Stirn mit zornig zur Mitte gesenkten Brauen, 3-5 liegt das Sehschlitz-Band
    davor, 6-7 der herabgezogene Mund."""
    pal = ctx.pal
    if ctx.face != "front":
        return _stone(ctx, 4.6)
    s, t = ctx.s, ctx.t
    if t == 0:
        return _c(pal["stone"], 6.4 if 1 <= s <= 6 else 5.6)
    if t == 1:
        if s in (0, 1, 6, 7):
            return _c(pal["stone"], 7.6)                               # Brauen außen (höher)
        return _c(pal["stone"], 5.8)
    if t == 2:
        if s in (2, 3, 4, 5):
            return _c(pal["stone"], 7.2)                               # Brauen innen (tiefer)
        return _c(pal["stone"], 4.0)                                   # Schatten unter den äußeren Brauen
    if t == 6:
        return _c(pal["crack"], 1) if 2 <= s <= 5 else _c(pal["stone"], 4.8)      # Mund
    if t == 7:
        return _c(pal["crack"], 2) if s in (1, 6) else _c(pal["stone"], 4.0)      # Mundwinkel hängen herab
    return _c(pal["stone"], 5.4)


def _band(ctx: Ctx):
    """Sehschlitz-Band: dunkler Schlitz mit zwei leuchtenden Augen, darüber und darunter heller Steinrand."""
    pal = ctx.pal
    if ctx.face == "top":
        return _c(pal["stone"], 7)
    if ctx.face == "bottom":
        return _c(pal["crack"], 2)
    if ctx.face != "front":
        return _stone(ctx, 4.4)
    s, t = ctx.s, ctx.t
    if t == 0:
        return _c(pal["stone"], 6.6 if s not in (0, 9) else 5.2)
    if t == 2:
        return _c(pal["stone"], 4.8 if s not in (0, 9) else 4.0)
    # mittlere Zeile: Schlitz mit Augen (die Augen selbst leuchten über die "eyes"-Quader)
    if s in (2, 3, 6, 7):
        return _c(pal["eye"], 1)
    return _c(pal["crack"], 0)


def _eyes(ctx: Ctx):
    """Doppel der Kopfquader: im Hauptbild unsichtbar, im Leuchtbild die beiden Augen (und ihr Schein)."""
    if ctx.face == "front" and ctx.cube.size[0] == 10:
        s, t = ctx.s, ctx.t
        if t == 1 and s in (2, 3, 6, 7):
            ctx.glow = _c(ctx.pal["eye"], 4 if s in (3, 6) else 3)       # innen am hellsten
        elif t == 1 and s in (1, 8):
            ctx.glow = mix(_c(ctx.pal["eye"], 0), (0, 0, 0, 255), 0.5)   # schwacher Schein im Schlitz
    return (0, 0, 0, 0)


def _cloth(ctx: Ctx, base: float = 3.8):
    """Tuch: weiche Falten (in Windrichtung nach hinten), oben im Licht, Säume heller."""
    x, y, z = ctx.world
    pal = ctx.pal
    n = noise3(x * 0.3, y * 0.5, z * 0.3, 6111)
    fold = math.sin((x if ctx.face in ("front", "back", "top") else z) * 1.3 + n * 2.4)
    i = base + fold * 0.7 + (n - 0.5) * 0.6
    if ctx.face == "top":
        i += 1.0
    elif ctx.face == "bottom":
        i -= 1.6
    return _c(pal["cloth"], i)


def _hood(ctx: Ctx):
    pal = ctx.pal
    face = ctx.face
    if face == "front":
        # nur der Rand um das Gesicht ist zu sehen: innen tiefer Schatten, außen ein silberner Saum
        lx, ly, _ = ctx.local
        if abs(lx) > 4.8:
            return _c(pal["cloth"], 2)                                 # Kante der Kapuze
        return _c(pal["cloth"], 0)                                     # tiefer Schatten in der Öffnung
    if face == "bottom":
        return _c(pal["cloth"], 1)
    col = _cloth(ctx)
    if face in ("west", "east") and rel_y(ctx) > 0.86:
        return _c(pal["trim"], 3.2)                                    # Saum unten
    return col


def _hood_top(ctx: Ctx):
    if ctx.face == "bottom":
        return None
    return _cloth(ctx, 4.6)


def _hood_brim(ctx: Ctx):
    """Vorstehender Kapuzenrand: oben Tuch, vorn ein silberner Saum, unten Schatten auf die Stirn."""
    pal = ctx.pal
    if ctx.face == "bottom":
        return _c(pal["cloth"], 0)
    if ctx.face == "front":
        return _c(pal["trim"], 4 if ctx.t == 0 else 2.6)
    return _cloth(ctx, 4.4)


def _hood_tip(ctx: Ctx):
    """Zipfel der Kapuze: nach hinten oben gezogen, zum Ende heller (Licht im Wind)."""
    if ctx.face == "bottom":
        return _c(ctx.pal["cloth"], 1)
    return _cloth(ctx, 4.0 + ctx.local[2] * 0.25)


def _streamer(ctx: Ctx):
    """Wehendes Band: zum Ende hin heller und ausgefranst."""
    pal = ctx.pal
    lz = ctx.local[2]
    d = lz / max(ctx.cube.size[2], 1.0)
    if d > 0.82 and hash01(ctx.u, ctx.v, 6121) > 0.55 - (d - 0.82) * 2:
        return None
    if ctx.face in ("west", "east"):
        return _c(pal["trim"], 2.6 + d * 1.6)
    i = 3.4 + d * 2.2 + (0.8 if ctx.face == "top" else -0.6)
    if int(math.floor(lz)) % 4 == 3:
        i -= 0.8                                                       # Knitterfalten
    return _c(pal["cloth"], i)


def _plume(ctx: Ctx):
    """Eine große, nach hinten gestrichene Feder: heller Kiel in der Mitte, weiße Fahne, türkise Spitze."""
    if ctx.face not in ("west", "east"):
        return None
    pal = ctx.pal
    lx, ly, lz = ctx.local
    f = lz / 11.0                                                       # 0 = Kiel am Ansatz, 1 = Spitze
    half = 2.05 * math.sin(math.pi * min(1.0, f)) ** 0.85                 # spitz an Ansatz und Spitze
    dy = ly + (f - 0.5) * 0.6                                           # leicht geschwungen
    if abs(dy) > half:
        return None
    if abs(dy) < 0.5:
        return _c(pal["feather"], 3.2 + f)                             # Kiel
    if f > 0.78:
        return _c(pal["tip"], 2.4 + (f - 0.78) * 9)                    # türkise Spitze
    # Fahne: zum Rand hin heller, schräge Äste (jeder dritte Pixel etwas dunkler)
    barb = int(math.floor(lz - abs(dy) * 1.2)) % 3 == 0
    return _c(pal["feather"], 5.8 - (0.7 if barb else 0.0) - (0.4 if dy > 0 else 0.0))


_GLYPH_OUT = {(1, 1), (0, 2), (1, 2), (2, 2), (1, 3), (1, 4)}              # Kreuz-Rune nach außen
_GLYPH_IN = {(2, 1), (1, 2), (0, 3), (1, 4), (2, 5)}                       # Winkel-Rune nach innen


def _runestone(ctx: Ctx):
    """Runenstein-Tafel: heller Stein mit dunklem Rand; auf der Außen- und Innenseite je eine leuchtende Rune."""
    pal = ctx.pal
    s, t = ctx.s, ctx.t
    if ctx.face in ("top", "bottom"):
        return _c(pal["stone"], 4.2 if ctx.face == "top" else 3.0)
    if ctx.face in ("front", "back"):
        glyph = _GLYPH_OUT if ctx.face == "front" else _GLYPH_IN
        if (s, t) in glyph:
            g = _c(pal["rune"], 3.6 if t <= 2 else 3.0)
            ctx.glow = g
            return g
        if t == ctx.h - 1:
            return _c(pal["stone"], 3.4)
        return _c(pal["stone"], 4.4 if ctx.face == "front" else 3.6)
    return _c(pal["stone"], 3.6 - rel_y(ctx) * 0.8)


def _crystal(ctx: Ctx):
    pal = ctx.pal
    i = 4.6 - rel_y(ctx) * 1.6 + (0.6 if ctx.face in ("front", "west") else 0.0)
    col = _c(pal["crystal"], i)
    ctx.glow = mix(col, (0, 0, 0, 255), 0.2)
    return col


def materials(variant: str) -> dict:
    return {
        "mask": _mask, "band": _band, "eyes": _eyes, "hood": _hood, "hood_top": _hood_top,
        "hood_brim": _hood_brim, "hood_tip": _hood_tip, "streamer": _streamer, "plume": _plume, "runestone": _runestone,
        "crystal": _crystal,
    }


def hidden(variant: str) -> list[str]:
    return []
