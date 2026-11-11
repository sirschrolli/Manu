"""
Eiszapfen – Glaciaras Geschoss, das vom Himmel stürzt: ein dicker, unregelmäßiger Zapfen aus altem Gletschereis,
ca. 22 lang entlang Z, die Spitze vorn (-Z, sie zeigt in Fallrichtung), oben der abgebrochene, breite Ansatz mit
einer rauen Bruchkante. Ein einziger Knochen "icicle" (Drehpunkt 0, 0, 0), den das Spiel in die Flugrichtung dreht.

  * Körper: acht Stufen, nach vorn immer schmaler, jede ein wenig gegen die vorige versetzt (gewachsen, nicht gedrechselt)
  * zwei kleine Nebenzapfen an der Seite, nahe am Ansatz, die mit nach vorn zeigen
  * Ansatz: breiter, schräg abgebrochener Eisbrocken mit Schnee obenauf

Bemalung wie die Eislanze (``ice_lance.ice_shade``): Facetten je Flächenrichtung, helle Kanten, feine Sprünge; zum
Ansatz hin trüber und weißer (Reif, eingeschlossene Luft), zur Spitze klarer und blauer, der Kern leuchtet schwach.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, hexc, pick
from .ice_lance import covered, face_uv, ice_shade, ice_texel, with_alpha

PALETTES = {
    "icicle": {
        "ice": [hexc(c) for c in ("#0f2f5c", "#174a86", "#2668aa", "#3c88c8", "#5ea8de", "#86c6ee", "#b0e0f8", "#d8f2fe",
                                  "#f4fcff", "#ffffff")],
        "core": [hexc(c) for c in ("#0a3a5a", "#14607e", "#2a8eaa", "#4cbcd4", "#8ae4f0")],
        "snow": [hexc(c) for c in ("#a8bccc", "#c4d4e0", "#dce8f0", "#eef5fa", "#ffffff")],
    },
}

# Stufen: (halbe Breite x, halbe Breite y, z vorn, z hinten, Versatz x, Versatz y) – viele flache Stufen, zur Spitze
# hin immer schlanker (wie gewachsen, nicht gedrechselt: jede Stufe ein wenig versetzt)
_N = 13
_Z_TIP, _Z_BASE = -11.2, 8.4
_JITTER = [(0.1, -0.05), (0.08, 0.0), (0.05, 0.04), (0.0, 0.06), (-0.04, 0.05), (-0.07, 0.02), (-0.08, -0.02),
           (-0.05, -0.05), (0.0, -0.06), (0.04, -0.04), (0.06, 0.0), (0.04, 0.03), (0.0, 0.0)]
BODY = []
for _i in range(_N):
    _t = (_i + 0.5) / _N                                                # 0 Spitze … 1 Ansatz
    _hw = 0.22 + 2.3 * _t ** 1.45
    _z0 = _Z_TIP + (_Z_BASE - _Z_TIP) * _i / _N
    _z1 = _Z_TIP + (_Z_BASE - _Z_TIP) * (_i + 1) / _N + 0.35
    BODY.append((_hw, _hw * 0.96, round(_z0, 3), round(min(_z1, _Z_BASE), 3)) + _JITTER[_i])


def build() -> Model:
    m = Model("icicle", 64, 32)
    m.tex_res = 2
    m.bone("icicle", pivot=(0, 0, 0))
    for hx, hy, z0, z1, dx, dy in BODY:
        m.cube("icicle", (dx - hx, dy - hy, z0), (2 * hx, 2 * hy, z1 - z0), "body")
    # Nebenzapfen (zeigen mit nach vorn)
    m.cube("icicle", (1.8, -0.5, 0.4), (1.0, 1.0, 3.4), "drip")
    m.cube("icicle", (2.0, -0.3, -1.4), (0.6, 0.6, 2.0), "drip")
    m.cube("icicle", (-1.0, 1.6, 2.6), (1.2, 1.1, 3.0), "drip")
    m.cube("icicle", (-0.8, 2.0, 1.0), (0.7, 0.7, 1.8), "drip")
    # Ansatz: breiter Brocken, schräg abgebrochen, Schnee obenauf
    m.cube("icicle", (-2.9, -2.8, 8.2), (5.8, 5.6, 1.8), "base")
    m.cube("icicle", (-2.4, -2.6, 9.8), (4.4, 4.6, 1.0), "base")
    m.cube("icicle", (-1.6, -2.2, 10.6), (2.4, 3.0, 0.8), "base")
    return m


def _tipness(z: float) -> float:
    """0 am Ansatz … 1 an der Spitze."""
    return max(0.0, min(1.0, (8.4 - z) / 19.4))


def _body(ctx: Ctx):
    if covered(ctx):
        return None
    x, y, z = ctx.local
    tip = _tipness(z)
    v, a, g = ice_shade(ctx, base=-0.06 + 0.1 * tip, crack_seed=7, crack=0.18, alpha=205 + 25 * (1.0 - tip))
    face = ctx.lface
    u, w = face_uv(ctx)
    if face in ("front", "back"):
        # Stufenflächen: ringförmig, außen heller (Facette), innen klarer
        ring = max(abs(u - 0.5), abs(w - 0.5)) * 2.0
        v = 0.86 if ring > 0.7 else 0.6
        a = 232 if ring > 0.7 else 175
    else:
        # Längsrillen: geschmolzen und wieder gefroren
        groove = (u * 3.0 + z * 0.13) % 1.0
        if groove < 0.18:
            v += 0.12
            a += 20
        # zum Ansatz hin trüber (Reif, Luftbläschen)
        if tip < 0.45:
            v += (0.45 - tip) * 0.35
            a += (0.45 - tip) * 60
        if abs(u - 0.5) < 0.22 and tip > 0.3:
            g = max(g, 0.18 + 0.4 * tip)                                    # klarer Kern
    return ice_texel(ctx, v, a, g)


def _drip(ctx: Ctx):
    if covered(ctx):
        return None
    if ctx.lface == "back":
        return None                                                         # steckt im Zapfen
    v, a, g = ice_shade(ctx, base=0.08, crack=0.0, alpha=215)
    return ice_texel(ctx, v, a, max(g, 0.22))


def _base(ctx: Ctx):
    """Bruchkante: rau, trüb und weiß; obenauf (hinten, +Z) Schnee."""
    if covered(ctx):
        return None
    x, y, z = ctx.local
    face = ctx.lface
    if face == "back" or face == "top" and z > 9.6:
        k = (x * 1.7 + y * 2.3) % 1.0
        return with_alpha(pick(ctx.pal["snow"], 0.55 + 0.4 * k), 255)
    v, a, g = ice_shade(ctx, base=0.18, crack_seed=11, crack=0.22, alpha=238)
    u, w = face_uv(ctx)
    if (u * 5.0 + w * 3.0) % 1.0 < 0.2:
        v -= 0.12                                                           # Bruchrillen
    return ice_texel(ctx, v, a, g * 0.5)


def materials(variant: str) -> dict:
    return {"body": _body, "drip": _drip, "base": _base}
