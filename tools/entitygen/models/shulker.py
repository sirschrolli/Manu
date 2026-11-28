"""
Schreinling (statt Shulker) auf dem Skelett des Vanilla-Shulkers (Deckel "lid", Sockel "base", Kopf "head" mit den
Drehpunkten von ShulkerModel, damit Öffnen, Spähen, Kopfdrehen und das Anheften an jede Seite passen). Eigenes Aussehen:

  * die Schale ist ein geschnitzter Reliquienschrein aus Stein: auf den Seiten des Deckels je eine Bogennische mit
    einer glimmenden Rune, darunter ein Fries aus Rauten, an den Kanten Pilaster; oben ein achtzackiger Stern mit einem
    Medaillon samt Seelenstein in der Mitte, an den Ecken Beschläge aus angelaufener Bronze
  * unter dem Deckelrand sitzen Zähne – geschlossen verborgen im Sockel, beim Öffnen blecken sie
  * der Sockel ist ein Fuß mit Gesims und Runenpunkten, innen glimmt ein Ring, wo das Wesen ruht
  * das Wesen selbst ist bleich und leuchtet von innen, hat ein einziges großes Auge mit Schlitzpupille und zwei
    Fühler mit glühenden Spitzen

Siebzehn Bilder: Purpur (ungefärbt) und die sechzehn Farben, mit denen man Shulker färben kann.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, ramp

# Grundfarben der Farbstoffe (wie DyeColor.textureDiffuseColor), dazu Purpur für den ungefärbten
_DYES = {
    "white": "#f9fffe", "orange": "#f9801d", "magenta": "#c74ebd", "light_blue": "#3ab3da", "yellow": "#fed83d",
    "lime": "#80c71f", "pink": "#f38baa", "gray": "#474f52", "light_gray": "#9d9d97", "cyan": "#169c9c",
    "purple": "#8932b8", "blue": "#3c44aa", "brown": "#835432", "green": "#5e7c16", "red": "#b02e26", "black": "#1d1d21",
}


def _stone(base: str) -> list:
    """Stein-Verlauf zu einer Farbe: dunkel und etwas entsättigt bis hell."""
    c = hexc(base)
    grey = sum(c[:3]) / 3
    muted = tuple(round(c[i] * 0.78 + grey * 0.22) for i in range(3))
    dark = "#%02x%02x%02x" % tuple(max(0, round(v * 0.28)) for v in muted)
    light = "#%02x%02x%02x" % tuple(min(255, round(v * 0.7 + 255 * 0.3)) for v in muted)
    return ramp(dark, light, steps=8)


def _palette(stone: str) -> dict:
    return {
        "stone": _stone(stone), "bronze": ramp("#2a1c0c", "#d0a050", steps=6),
        "rune": [hexc(c) for c in ("#3a1460", "#7a2ab8", "#c070f0", "#f0c0ff", "#ffffff")],
        "inner": ramp("#0c0612", "#3a1a4a"), "skin": ramp("#7a6a96", "#fbf4ff", steps=7),
        "eye": [hexc(c) for c in ("#4a1070", "#a030e0", "#e090ff", "#ffffff")], "tooth": ramp("#6a6070", "#f2eee6"),
    }


PALETTES = {"schreinling": _palette("#a679a6")}
for _name, _hex in _DYES.items():
    PALETTES[f"schreinling_{_name}"] = _palette(_hex)


def build() -> Model:
    m = Model("shulker", 128, 128)
    m.tex_res = 2
    # Deckel: Schale, Medaillon, Eckbeschläge, Zähne unter dem Rand
    m.bone("lid", pivot=(0, 24, 0))
    m.cube("lid", (-8, -16, -8), (16, 12, 16), "lid")
    m.cube("lid", (-3, -17, -3), (6, 1, 6), "medallion")
    for cx in (-8.5, 6.5):
        for cz in (-8.5, 6.5):
            m.cube("lid", (cx, -16.5, cz), (2, 2.5, 2), "fitting")
    for k in range(4):
        x = -6 + k * 3.6
        m.cube("lid", (x, -4, -7.6), (1, 1.6, 0.5), "tooth")
        m.cube("lid", (x, -4, 7.1), (1, 1.6, 0.5), "tooth")
        m.cube("lid", (-7.6, -4, x), (0.5, 1.6, 1), "tooth")
        m.cube("lid", (7.1, -4, x), (0.5, 1.6, 1), "tooth")
    # Sockel mit Fuß
    m.bone("base", pivot=(0, 24, 0))
    m.cube("base", (-8, -8, -8), (16, 8, 16), "base")
    m.cube("base", (-8.5, -1.5, -8.5), (17, 1.5, 17), "plinth")
    # das Wesen: Kopf mit einem großen Auge und zwei Fühlern
    m.bone("head", pivot=(0, 12, 0))
    m.cube("head", (-3, 0, -3), (6, 6, 6), "head")
    m.cube("head", (-2.2, -2.4, -0.5), (0.7, 2.6, 0.7), "feeler")
    m.cube("head", (1.5, -2.4, -0.5), (0.7, 2.6, 0.7), "feeler")
    return m


def _uv(ctx: Ctx) -> tuple[float, float]:
    return (ctx.s + 0.5) / max(ctx.w, 1), (ctx.t + 0.5) / max(ctx.h, 1)


def _lit(ctx: Ctx, key: str, v: float):
    pal = ctx.pal[key]
    col = pal[max(0, min(len(pal) - 1, int(v * len(pal))))]
    ctx.glow = col
    return col


def _carved(ctx: Ctx, base: float):
    """Behauener Stein: matt, leicht fleckig, oben heller."""
    x, y, z = ctx.world
    v = base + (noise3(x * 0.5, y * 0.5, z * 0.5, 1301) - 0.5) * 0.12 + (hash01(ctx.u, ctx.v, 1302) - 0.5) * 0.06
    if ctx.face == "top":
        v += 0.08
    elif ctx.face == "bottom":
        v -= 0.12
    return pick(ctx.pal["stone"], v)


def _star(u: float, v: float) -> float:
    """Achtzackiger Stern: < 1 innen."""
    du, dv = u - 0.5, v - 0.5
    r = math.hypot(du, dv)
    a = math.atan2(dv, du)
    return r / (0.2 + 0.12 * abs(math.cos(4 * a)))


def _lid(ctx: Ctx):
    u, v = _uv(ctx)
    face = ctx.face
    if face == "bottom":                                                # innen (beim Öffnen zu sehen)
        r = math.hypot(u - 0.5, v - 0.5)
        if hash01(ctx.u, ctx.v, 1303) > 0.9:
            return _lit(ctx, "rune", 0.3)
        return pick(ctx.pal["inner"], 0.6 - r * 0.6)
    if face == "top":
        if u < 0.06 or u > 0.94 or v < 0.06 or v > 0.94:
            return _carved(ctx, 0.75)                                   # Kante
        if u < 0.12 or u > 0.88 or v < 0.12 or v > 0.88:
            return _carved(ctx, 0.38)                                   # Rille
        k = _star(u, v)
        if k < 1.0:
            return _carved(ctx, 0.7 - k * 0.2)
        if k < 1.15:
            return _carved(ctx, 0.3)
        return _carved(ctx, 0.52)
    # Seiten: oben Kante, Pilaster, Bogennische mit Rune, Rautenfries, unten Rand
    if v < 0.07:
        return _carved(ctx, 0.78)
    if v > 0.9:
        return _carved(ctx, 0.62 if v < 0.95 else 0.4)
    if u < 0.1 or u > 0.9:
        return _carved(ctx, 0.66 if (u < 0.05 or u > 0.95) else 0.5)   # Pilaster
    if 0.74 < v < 0.86:
        k = (u * 8.0) % 1.0
        if abs(k - 0.5) + abs((v - 0.8) / 0.06 - 0) * 0.25 < 0.3:
            return _carved(ctx, 0.72)                                   # Rauten
        return _carved(ctx, 0.36)
    # Bogennische: Rechteck mit rundem Bogen oben
    nu, nv = abs(u - 0.5) / 0.22, v
    inside = nu < 1.0 and 0.2 < nv < 0.68 and (nv > 0.34 or math.hypot(nu, (0.34 - nv) / 0.14) < 1.0)
    if inside:
        # Rune: ein senkrechter Strich mit zwei Haken
        ru, rv = u - 0.5, v - 0.46
        if (abs(ru) < 0.025 and abs(rv) < 0.15) or (abs(rv - (abs(ru) * 1.2 - 0.06)) < 0.025 and abs(ru) < 0.1):
            return _lit(ctx, "rune", 0.55 + hash01(ctx.u, ctx.v) * 0.2)
        return pick(ctx.pal["stone"], 0.18 + (noise3(*ctx.world, 1304) - 0.5) * 0.06)
    if nu < 1.18 and 0.16 < nv < 0.7:
        return _carved(ctx, 0.7)                                        # Rahmen der Nische
    return _carved(ctx, 0.5)


def _medallion(ctx: Ctx):
    u, v = _uv(ctx)
    if ctx.face == "top":
        r = math.hypot(u - 0.5, v - 0.5)
        if r < 0.18:
            return _lit(ctx, "rune", 0.95 - r * 2.0)                    # Seelenstein
        if r < 0.26:
            return pick(ctx.pal["bronze"], 0.85)
        return pick(ctx.pal["bronze"], 0.5 + (noise3(*ctx.world, 1305) - 0.5) * 0.2)
    return pick(ctx.pal["bronze"], 0.35)


def _fitting(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.5 + (noise3(x, y, z, 1306) - 0.5) * 0.3 + (0.2 if ctx.face == "top" else 0.0)
    if noise3(x * 1.3 + 9, y * 1.3, z * 1.3, 1307) > 0.68:
        return mix(pick(ctx.pal["bronze"], v), hexc("#3a6a5a"), 0.5)       # Grünspan
    return pick(ctx.pal["bronze"], v)


def _tooth(ctx: Ctx):
    f = (ctx.local[1] - ctx.cube.origin[1]) / ctx.cube.size[1]            # 0 Ansatz … 1 Spitze
    if ctx.face == "bottom":
        return None
    if ctx.face not in ("top",):
        u, _ = _uv(ctx)
        if abs(u - 0.5) * 2.0 > 1.05 - f:
            return None
    return pick(ctx.pal["tooth"], 0.75 - f * 0.25)


def _base(ctx: Ctx):
    u, v = _uv(ctx)
    if ctx.face == "top":                                               # innen: der Ring, auf dem das Wesen ruht
        r = math.hypot(u - 0.5, v - 0.5)
        if 0.22 < r < 0.28:
            return _lit(ctx, "rune", 0.5)
        return pick(ctx.pal["inner"], 0.45 - r * 0.5)
    if ctx.face == "bottom":
        return _carved(ctx, 0.3)
    if v < 0.12:
        return _carved(ctx, 0.7)                                        # Gesims
    if v < 0.2:
        return _carved(ctx, 0.36)
    if abs(v - 0.55) < 0.07 and int(u * 8) % 2 == 0 and abs((u * 8) % 1.0 - 0.5) < 0.12:
        return _lit(ctx, "rune", 0.45)                                  # Runenpunkte
    if int(u * 4 + (0.5 if v > 0.55 else 0.0)) % 1 == 0 and abs((u * 4 + (0.5 if v > 0.55 else 0.0)) % 1.0) < 0.03:
        return _carved(ctx, 0.3)                                        # Fugen
    return _carved(ctx, 0.48)


def _plinth(ctx: Ctx):
    if ctx.face == "top":
        return _carved(ctx, 0.66)
    return _carved(ctx, 0.42 if ctx.face != "bottom" else 0.3)


def _head(ctx: Ctx):
    """Das Wesen: bleich, von innen leuchtend, ein großes Auge mit Schlitzpupille."""
    u, v = _uv(ctx)
    x, y, z = ctx.world
    if ctx.face == "front":
        r = math.hypot((u - 0.5) * 1.0, (v - 0.45) * 1.1)
        if r < 0.33:
            if abs(u - 0.5) < 0.05 and abs(v - 0.45) < 0.26:
                return pick(ctx.pal["inner"], 0.05)                     # Schlitzpupille
            return _lit(ctx, "eye", 0.95 - r * 2.2)
        if r < 0.4:
            return pick(ctx.pal["skin"], 0.25)                          # Lidrand
        if v > 0.82 and 0.3 < u < 0.7:
            return pick(ctx.pal["skin"], 0.3)                           # kleiner Mund
    col = pick(ctx.pal["skin"], 0.74 + (noise3(x * 1.1, y * 1.1, z * 1.1, 1308) - 0.5) * 0.25)
    if hash01(ctx.u, ctx.v, 1309) > 0.965:
        return _lit(ctx, "rune", 0.6)                                   # leuchtende Flecken
    ctx.glow = mix(col, (0, 0, 0, 255), 0.6)                            # schimmert schwach von innen
    return col


def _feeler(ctx: Ctx):
    f = (ctx.local[1] - ctx.cube.origin[1]) / ctx.cube.size[1]            # 0 Spitze … 1 Ansatz
    if f < 0.3:
        return _lit(ctx, "eye", 0.7)
    return pick(ctx.pal["skin"], 0.55)


def materials(variant: str) -> dict:
    return {"lid": _lid, "medallion": _medallion, "fitting": _fitting, "tooth": _tooth, "base": _base, "plinth": _plinth,
            "head": _head, "feeler": _feeler}


def hidden(variant: str) -> list[str]:
    return []
