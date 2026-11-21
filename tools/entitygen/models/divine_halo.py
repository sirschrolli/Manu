"""
Geworfener Heiligenschein (Heiligenschein-Klinge Schrollis des Schöpfers): ein waagrechtes Sonnenrad aus Gold und Licht
mit zwölf scharfen Strahlen, das sich wie ein Sägeblatt dreht (Knochen "disc" in der Mitte, dreht um Y).
"""
from __future__ import annotations

import math

from ..geometry import Model
from . import schrolli as base

PALETTES = {"divine_halo": dict(base._BASE)}
R = 9.0


def build() -> Model:
    m = Model("divine_halo", 64, 64)
    m.tex_res = 2
    m.bone("disc", pivot=(0, 0, 0))
    seg = 2 * math.pi * R / 16 + 0.5
    for k in range(16):
        a = 360.0 * k / 16
        r = math.radians(a)
        m.bone(f"seg_{k}", "disc", pivot=(math.cos(r) * R, 0, math.sin(r) * R), rotation=(0, -(a + 90.0), 0))
        m.cube(f"seg_{k}", (-seg / 2, -0.7, -0.8), (seg, 1.4, 1.6), "halo")
    for k in range(12):
        a = 360.0 * k / 12 + 15.0
        r = math.radians(a)
        length = 5.0 if k % 2 == 0 else 3.2
        # die Strahlen liegen waagrecht und zeigen nach außen (wie Sägezähne)
        m.bone(f"ray_{k}", "disc", pivot=(math.cos(r) * (R + 0.6), 0, math.sin(r) * (R + 0.6)), rotation=(0, -a, 0))
        m.cube(f"ray_{k}", (0, -0.3, -0.5), (length, 0.6, 1.0), "ray")
    inner = 5.4
    seg2 = 2 * math.pi * inner / 12 + 0.4
    for k in range(12):
        a = 30.0 * k
        r = math.radians(a)
        m.bone(f"inner_{k}", "disc", pivot=(math.cos(r) * inner, 0, math.sin(r) * inner), rotation=(0, -(a + 90.0), 0))
        m.cube(f"inner_{k}", (-seg2 / 2, -0.35, -0.35), (seg2, 0.7, 0.7), "halo_thin")
    return m


def materials(variant: str) -> dict:
    return base.materials("schrolli")
