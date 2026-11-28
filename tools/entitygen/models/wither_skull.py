"""
Seelenschädel (statt Witherschädel-Geschoss): ein verkohlter Schädel mit Unterkiefer wie die Köpfe des Aschenkönigs,
Augen und Rachen glühen, hinten zieht ein Schweif aus Seelenfeuer her (eine senkrechte und eine waagrechte Fläche, die
sich nach hinten verjüngen und in Zungen auslaufen). Das Spiel dreht den Knochen "head" in die Flugrichtung (wie beim
Vanilla-Schädel), das Gesicht zeigt nach vorn (-z), der Schweif nach hinten.

Zwei Bilder: der Seelenschädel (türkis) und der Zornschädel (der "gefährliche", der auch harte Blöcke sprengt: weißblau
lodernd, gebleichter Schädel).
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, noise3, pick, ramp
from . import wither

PALETTES = {
    "seelenschaedel": {
        "bone": ramp("#120e0d", "#7e766f", steps=8), "ash": ramp("#3a3533", "#b4ada6"), "socket": ramp("#020101", "#18110f"),
        "soul": [hexc(c) for c in ("#062c2a", "#0b4c48", "#127a72", "#1ca498", "#33cdc0", "#6aece0", "#b4fff6", "#f0fffd")],
        "horn": ramp("#16121c", "#8a7f96"),
    },
    "seelenschaedel_zorn": {
        "bone": ramp("#2a3040", "#d6e0f4", steps=8), "ash": ramp("#5a6478", "#eef2ff"), "socket": ramp("#080a1e", "#1e2a5a"),
        "soul": [hexc(c) for c in ("#120e4a", "#1e2280", "#2c3cbc", "#4060ea", "#6e92ff", "#a6c0ff", "#dce6ff", "#ffffff")],
        "horn": ramp("#1a1e30", "#8a96b8"),
    },
}

_LENGTH = 12.0


def build() -> Model:
    m = Model("wither_skull", 64, 64)
    m.tex_res = 2
    m.bone("head")
    m.cube("head", (-3.5, -7.5, -3.5), (7, 7, 7), "skull")
    m.cube("head", (-3.5, -5.6, -3.85), (7, 0.9, 0.35), "brow")
    m.cube("head", (-2.4, -0.5, -3.4), (4.8, 1.6, 3.5), "jaw")
    m.cube("head", (0, -10, -0.5), (0, 10, _LENGTH), "trail")                # senkrechter Schweif
    m.cube("head", (-5.5, -4, -0.5), (11, 0, _LENGTH), "trail")              # waagrechter Schweif
    return m


def _trail(ctx: Ctx):
    """Flammenschweif: am Schädel breit und hell, nach hinten schmaler, in Zungen auslaufend."""
    x, y, z = ctx.local
    t = (z + 0.5) / _LENGTH                                               # 0 am Schädel … 1 am Ende
    across = abs(x) if ctx.cube.size[0] > 0 else abs(y + 4.0)            # Abstand von der Mittellinie
    half = (6.0 if ctx.cube.size[0] == 0 else 5.5) * (1.0 - t) ** 0.75
    # Zungen: der Rand züngelt
    half *= 0.78 + 0.32 * (0.5 + 0.5 * math.sin(z * 1.7 + across * 0.9 + hash01(int(z * 2), 951) * 2.0))
    half += (noise3(x * 0.8, y * 0.8, z * 0.8, 952) - 0.5) * 1.2
    if half <= 0.2 or across > half:
        return None
    k = 1.0 - across / half
    return wither._soul(ctx, 0.12 + k * 0.62 + (1.0 - t) * 0.28)


def materials(variant: str) -> dict:
    return {"skull": wither._skull, "brow": wither._brow, "jaw": wither._jaw, "trail": _trail}


def hidden(variant: str) -> list[str]:
    return []
