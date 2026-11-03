"""
Schwerer Ballistenbolzen (Geschoss): vierkantige, geschmiedete Spitze in vier Stufen, eiserne Tülle, Eschenschaft,
hinten drei Leitflossen aus rot gefärbtem Leder im Abstand von 120° (eine nach unten) und eine eiserne Endkappe.
Gleich gebaut wie der eingelegte Bolzen der Balliste (siege_common.bolt), etwa 26 lang.

Knochen (Vertrag mit dem Java-Renderer): ``bolt`` – Drehpunkt in der Mitte (0, 0, 0), Spitze nach -Z, keine
Boden-Lage (Geschoss). Hilfsknochen bolt_fin_a/b/c sind fest gedreht (nicht animieren).
"""
from __future__ import annotations

from ..geometry import Model
from . import siege_common as sc

PALETTES = {"ballista_bolt": sc.PALETTE}


def build() -> Model:
    m = Model("ballista_bolt", 64, 64)
    m.bone("bolt", pivot=(0, 0, 0))
    sc.bolt(m, "bolt")
    return m


def materials(variant: str) -> dict:
    return sc.materials()
