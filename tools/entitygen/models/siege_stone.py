"""
Steinkugel für Katapult und Tribok: ein grob behauener, klumpiger Feldstein (etwa 10 × 10 × 10) aus drei sich
kreuzenden Quadern mit abgeschlagenen Kanten und ein paar Buckeln; grauer, rauer Stein mit Rissen und etwas Flechte.

Knochen (Vertrag mit dem Java-Renderer): ``stone`` – Drehpunkt in der Mitte (0, 0, 0), keine Boden-Lage (Geschoss).
"""
from __future__ import annotations

from ..geometry import Model
from . import siege_common as sc
from .siege_common import lbox

PALETTES = {"siege_stone": sc.PALETTE}


def build() -> Model:
    m = Model("siege_stone", 64, 64)
    m.bone("stone", pivot=(0, 0, 0))
    s = "stone"
    lbox(m, s, -5, -4, -4, 5, 4, 4.5, "rock")
    lbox(m, s, -4, -5, -4, 4, 5, 4, "rock")
    lbox(m, s, -4, -4, -5, 4.5, 3.5, 5, "rock")
    lbox(m, s, -2, -6, -2.5, 1.5, -4.5, 1, "rock")                         # Buckel oben
    lbox(m, s, 4.5, -1.5, -2, 6, 2, 1.5, "rock")                           # Buckel seitlich
    lbox(m, s, -3, 4.5, 0, 0, 6, 3, "rock")                                # Buckel unten
    lbox(m, s, -6, -2.5, 1, -4.5, 1, 3.5, "rock")
    return m


def materials(variant: str) -> dict:
    mats = sc.materials()
    mats["rock"] = sc.stone_paint(0.54, seed=3, lichen=True)
    return mats
