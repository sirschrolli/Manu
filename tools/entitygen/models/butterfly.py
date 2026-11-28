"""
Schmetterlinge der Königsauen (0.86): sechs Arten auf einem Modell – Tagpfauenauge, Zitronenfalter, Schwalbenschwanz,
Hauhechel-Bläuling, Admiral und Monarch. Ein schlanker, pelziger Leib mit Kopf und Fühlern mit Kolben, je zwei
Vorder- und Hinterflügel als dünne Flächen; die Form der Flügel (abgerundet, spitz, mit Schwänzen) steckt in der Textur
(was nicht Flügel ist, bleibt durchsichtig). Oben die Zeichnung der Art, unten die schlichtere Unterseite.

Knochen (für ButterflyModel in Java): body (mit head → antenna_l, antenna_r; wing_l, wing_r (Vorderflügel),
hind_l, hind_r (Hinterflügel)). Ruhehaltung: Flügel flach ausgebreitet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, pick

# Grundfarben je Art: Ober- und Unterseite, Zeichnung, Leib
_SPECIES = {
    "peacock": {"base": "#a8281c", "base2": "#c8402a", "under": "#2c2420", "under2": "#4a3a30", "body": "#2a1c16"},
    "brimstone": {"base": "#f2e04a", "base2": "#f8ec84", "under": "#d8dc8a", "under2": "#e8eab0", "body": "#6a6a3a"},
    "swallowtail": {"base": "#f2d860", "base2": "#f8e898", "under": "#e8d888", "under2": "#f2e4b0", "body": "#1a1612"},
    "blue": {"base": "#5a8ef0", "base2": "#8ab2f8", "under": "#b8b0a0", "under2": "#d4ccbc", "body": "#3a3a48"},
    "admiral": {"base": "#16121a", "base2": "#2a2430", "under": "#3a3036", "under2": "#5a4a50", "body": "#1a1416"},
    "monarch": {"base": "#e8781a", "base2": "#f49a3a", "under": "#e8a050", "under2": "#f2bc78", "body": "#1a1412"},
}

VARIANTS = list(_SPECIES)


def _pal(spec: dict) -> dict:
    def ramp(c1: str, c2: str) -> list:
        a, b = hexc(c1), hexc(c2)
        dark = tuple(int(v * 0.55) for v in a[:3]) + (255,)
        light = tuple(min(255, int(v + (255 - v) * 0.35)) for v in b[:3]) + (255,)
        return [dark, mix(dark, a, 0.5), a, mix(a, b, 0.5), b, light]

    return {"base": ramp(spec["base"], spec["base2"]), "under": ramp(spec["under"], spec["under2"]),
            "body": ramp(spec["body"], spec["body"]),
            "black": [hexc(c) for c in ("#060508", "#100e12", "#1c181e", "#2a242c")],
            "white": [hexc(c) for c in ("#c8c4c0", "#e0dcd8", "#f2f0ec", "#ffffff")],
            "red": [hexc(c) for c in ("#a01818", "#c82a20", "#e84a30", "#f86a48")],
            "blue": [hexc(c) for c in ("#1a3a8a", "#2a5ac0", "#4a82e8", "#86b4ff")],
            "yellow": [hexc(c) for c in ("#c89a20", "#e8c040", "#f8dc70", "#fff0b0")],
            "orange": [hexc(c) for c in ("#c05a10", "#e07a20", "#f49a3a", "#ffc070")]}


PALETTES = {name: _pal(spec) for name, spec in _SPECIES.items()}


def build() -> Model:
    m = Model("butterfly", 32, 32)
    m.tex_res = 8
    m.bone("body", pivot=(0, 22.0, 0))
    m.cube("body", (-0.3, -0.3, -1.0), (0.6, 0.6, 1.4), "thorax")
    m.cube("body", (-0.22, -0.22, 0.4), (0.44, 0.44, 2.2), "abdomen")
    m.bone("head", "body", pivot=(0, 0, -1.0))
    m.cube("head", (-0.32, -0.32, -0.55), (0.64, 0.6, 0.55), "head")
    for side, sx in (("l", 1), ("r", -1)):
        m.bone(f"antenna_{side}", "head", pivot=(0.15 * sx, -0.25, -0.4), rotation=(-28, 22 * sx, 0))
        m.cube(f"antenna_{side}", (-0.05, -0.05, -2.0), (0.1, 0.1, 2.0), "antenna")
        m.cube(f"antenna_{side}", (-0.11, -0.11, -2.25), (0.22, 0.22, 0.32), "club")
        lo = (lambda a, w: a if sx > 0 else -a - w)
        # Vorderflügel (vorn, länger), Hinterflügel (hinten, runder) – flache Flächen
        m.bone(f"wing_{side}", "body", pivot=(0.25 * sx, -0.2, -0.5), rotation=(0, 16 * sx, 0))
        m.cube(f"wing_{side}", (lo(0, 4.0), 0, -1.9), (4.0, 0, 2.4), "fore")
        m.bone(f"hind_{side}", "body", pivot=(0.22 * sx, -0.18, 0.2), rotation=(0, -8 * sx, 0))
        m.cube(f"hind_{side}", (lo(0, 3.0), 0, -0.2), (3.0, 0, 2.8), "hind")
    return m


# =============================================================================================
# Bemalung: Umriss (durchsichtig außerhalb) und Zeichnung je Art
# =============================================================================================

def _uv(ctx: Ctx) -> tuple[float, float]:
    """(Abstand vom Leib 0..1, Lage von vorn nach hinten 0..1) auf dem Flügel."""
    c = ctx.cube
    lx, _, lz = ctx.local
    span = abs(lx) / c.size[0]
    along = (lz - c.origin[2]) / c.size[2]
    return span, along


def _fore_shape(variant: str, s: float, a: float) -> bool:
    """Vorderflügel: vorn gerade, außen schräg abgeschnitten, hinten gerundet; Zitronenfalter mit Spitze."""
    if s < 0.0 or s > 1.0 or a < 0.0 or a > 1.0:
        return False
    tip = 1.0 - a * (0.45 if variant != "brimstone" else 0.3)                # Außenrand schräg nach innen-hinten
    if variant == "brimstone" and a < 0.25:
        tip = 1.0 - (0.25 - a) * 0.6                                          # die Flügelspitze
    if s > tip:
        return False
    # hinten (zum Hinterflügel hin) gerundet
    if a > 0.72 and (a - 0.72) / 0.28 > math.sqrt(max(0.0, 1.0 - (s / max(tip, 1e-3)) ** 2)) + 0.12:
        return False
    return not (a < 0.06 and s > 0.85)


def _hind_shape(variant: str, s: float, a: float) -> bool:
    """Hinterflügel: rund; der Schwalbenschwanz mit Schwanz, der Monarch etwas eckiger."""
    if s < 0.0 or a < 0.0 or a > 1.0:
        return False
    if variant == "swallowtail" and 0.42 < s < 0.6 and a > 0.62:
        return True                                                          # der Schwanz
    lim = 0.82 if variant == "swallowtail" else 1.0
    if a > lim:
        return False
    r = math.hypot(s / 1.0, (a - 0.05) / (lim - 0.05) * 1.05)
    return r <= 1.0 and s <= 1.0


def _body(ctx: Ctx):
    x, y, z = ctx.world
    v = 0.45 + (hash01(int(z * 8), int(x * 8), 911) - 0.5) * 0.2
    return pick(ctx.pal["body"], v)


def _antenna(ctx: Ctx):
    return pick(ctx.pal["black"], 0.4)


def _club(ctx: Ctx):
    return pick(ctx.pal["white"], 0.4) if ctx.params.get("variant") == "admiral" else pick(ctx.pal["black"], 0.6)


def _wing_common(ctx: Ctx, fore: bool):
    variant = ctx.params.get("variant", "peacock")
    s, a = _uv(ctx)
    if not (_fore_shape(variant, s, a) if fore else _hind_shape(variant, s, a)):
        return None
    under = ctx.lface == "bottom"
    if ctx.lface not in ("top", "bottom"):
        return None
    if under:
        return _under(ctx, variant, s, a, fore)
    return _upper(ctx, variant, s, a, fore)


def _veins(s: float, a: float, n: int = 5) -> bool:
    """Flügeladern: Strahlen vom Leib nach außen."""
    ang = math.atan2(a - 0.5, s + 0.15)
    return abs((ang * n / math.pi * 2.0) % 1.0 - 0.5) > 0.45


def _upper(ctx: Ctx, variant: str, s: float, a: float, fore: bool):
    p = ctx.pal
    edge = (s > 0.88 - a * 0.4 if fore else math.hypot(s, a) > 0.9)          # der Außensaum
    if variant == "peacock":
        # rotbraun, das große Augenfleck an der Vorderflügelspitze und auf dem Hinterflügel, dunkler Saum
        if fore:
            ex, ea = 0.68, 0.22
            d = math.hypot((s - ex) / 0.17, (a - ea) / 0.2)
            if d < 1.0:
                if d < 0.35:
                    return pick(p["black"], 0.3)
                if d < 0.55:
                    return pick(p["blue"], 0.7)
                if d < 0.75:
                    return pick(p["yellow"], 0.6)
                return pick(p["black"], 0.5)
            if 0.2 < s < 0.5 and a < 0.18:
                return pick(p["black"], 0.4)                                 # dunkle Flecken am Vorderrand
        else:
            d = math.hypot((s - 0.55) / 0.24, (a - 0.45) / 0.28)
            if d < 1.0:
                if d < 0.45:
                    return pick(p["blue"], 0.55 + (0.3 if d < 0.2 else 0.0))
                return pick(p["black"], 0.35)
        if edge:
            return pick(p["under"], 0.3)
        return pick(p["base"], 0.5 + (1.0 - s) * 0.2)
    if variant == "brimstone":
        # zitronengelb, ein kleiner orangener Punkt in der Mitte jedes Flügels
        if math.hypot(s - 0.5, a - 0.5) < 0.08:
            return pick(p["orange"], 0.5)
        return pick(p["base"], 0.55 + (0.1 if _veins(s, a) else 0.0))
    if variant == "swallowtail":
        # gelb mit schwarzen Bändern und Adern, am Hinterflügel blaue Monde und ein rotes Auge
        if fore:
            if a < 0.16 or edge or (0.18 < s < 0.3) or (0.42 < s < 0.5 and a < 0.6):
                return pick(p["black"], 0.35)
            if _veins(s, a, 7):
                return pick(p["black"], 0.4)
            return pick(p["base"], 0.6)
        if 0.42 < s < 0.6 and a > 0.62:
            return pick(p["black"], 0.35)                                    # der Schwanz
        r = math.hypot(s, a)
        if r > 0.62:
            if 0.68 < r < 0.82 and (math.atan2(a, s) * 6.0) % 1.0 < 0.55:
                return pick(p["blue"], 0.6)                                  # die blauen Monde
            return pick(p["black"], 0.3)
        if math.hypot(s - 0.12, a - 0.78) < 0.1:
            return pick(p["red"], 0.6)                                       # das rote Auge
        return pick(p["base"], 0.6 + (0.0 if not _veins(s, a, 7) else -0.3))
    if variant == "blue":
        # leuchtend blau, schmaler dunkler Saum, weiße Fransen
        if (s > 0.94 - a * 0.42) if fore else (math.hypot(s, a) > 0.94):
            return pick(p["white"], 0.6)
        if edge:
            return pick(p["black"], 0.5)
        return pick(p["base"], 0.45 + (1.0 - s) * 0.25 + (hash01(int(s * 20), int(a * 20), 912) - 0.5) * 0.06)
    if variant == "admiral":
        # schwarz mit leuchtend rotem Band und weißen Flecken an der Spitze
        if fore:
            band = abs((s - 0.25) * 0.8 - (a - 0.1) * 0.9 + 0.05) < 0.1 and 0.35 < s + a * 0.3 < 0.9
            if band:
                return pick(p["red"], 0.65)
            if any(math.hypot(s - cs, a - ca) < 0.07 for cs, ca in ((0.75, 0.12), (0.6, 0.18), (0.82, 0.3))):
                return pick(p["white"], 0.8)
            return pick(p["base"], 0.4)
        if a > 0.72 and math.hypot(s, a) > 0.7:
            return pick(p["red"], 0.6)                                       # das rote Band am Hinterrand
        return pick(p["base"], 0.42)
    # monarch: orange mit schwarzen Adern, schwarzer Saum mit weißen Punkten
    if edge:
        if (s * 23 + a * 17) % 1.0 < 0.3:
            return pick(p["white"], 0.75)
        return pick(p["black"], 0.3)
    if _veins(s, a, 6) or (fore and a < 0.1):
        return pick(p["black"], 0.35)
    return pick(p["base"], 0.5 + (1.0 - s) * 0.15)


def _under(ctx: Ctx, variant: str, s: float, a: float, fore: bool):
    p = ctx.pal
    v = 0.45 + (hash01(int(s * 24), int(a * 24), 913) - 0.5) * 0.2
    if variant == "peacock":
        return pick(p["under"], v - 0.1)                                     # fast schwarz, wie ein welkes Blatt
    if variant == "blue":
        if any(math.hypot(s - cs, a - ca) < 0.05 for cs, ca in ((0.3, 0.3), (0.55, 0.4), (0.45, 0.65), (0.7, 0.6))):
            return pick(p["black"], 0.5)
        if not fore and math.hypot(s, a) > 0.75 and (math.atan2(a, s) * 7) % 1.0 < 0.5:
            return pick(p["orange"], 0.6)
        return pick(p["under"], v)
    if variant == "admiral":
        if fore and abs((s - 0.25) * 0.8 - (a - 0.1) * 0.9 + 0.05) < 0.08:
            return pick(p["red"], 0.45)
        return pick(p["under"], v)
    if variant == "monarch" and _veins(s, a, 6):
        return pick(p["black"], 0.4)
    return pick(p["under"], v + 0.1)


def materials(variant: str) -> dict:
    return {"thorax": _body, "abdomen": _body, "head": _body, "antenna": _antenna, "club": _club,
            "fore": lambda ctx: _wing_common(ctx, True), "hind": lambda ctx: _wing_common(ctx, False)}
