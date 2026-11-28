"""
Der Felsbrocken des Steinhüters (0.96), wie er durch die Luft fliegt: derselbe Klotz aus Feenstein mit Erde, Moos und zwei
Mondsteinkristallen, den der Hüter beim Felswurf über dem Kopf hält (siehe stone_keeper.py) – hier mit der Mitte im
Ursprung, damit er sich im Flug um sich selbst überschlägt. Bemalt wie der Hüter.
"""
from __future__ import annotations

from ..geometry import Model
from . import stone_keeper

PALETTES = {"keeper_boulder": stone_keeper.PALETTES["stone_keeper"]}


def build() -> Model:
    m = Model("keeper_boulder", 128, 64)
    m.bone("boulder", pivot=(0.0, 0.0, 0.0))
    m.cube("boulder", (-6.8, -5.6, -6.2), (13.6, 11.2, 12.4), "boulder")
    m.cube("boulder", (-5.0, -7.2, -4.4), (10.0, 2.0, 8.8), "boulder")
    m.cube("boulder", (-5.4, 5.2, -4.6), (10.8, 1.8, 9.2), "boulder")
    stone_keeper._crystal(m, "boulder", "boulder_crystal_0", (3.4, -5.0, -2.0), (-20.0, 0.0, 30.0), 4.2, 2.0)
    stone_keeper._crystal(m, "boulder", "boulder_crystal_1", (-4.0, -4.8, 2.4), (24.0, 0.0, -26.0), 3.2, 1.6)
    return m


def materials(variant: str) -> dict:
    return stone_keeper.materials("stone_keeper")
