"""
Ventor, der Gladiator der Winde – Wind-Ebene (seit 1.9): dasselbe Knochengerüst wie ventor.py (gleiche Namen, Drehpunkte
und Winkel, damit VentorModel beide Netze backen und gleich bewegen kann), aber nur die vier Ringe des Wirbels tragen Quader:
je zwei ineinander geschachtelte Kästen voll durchscheinendem Dunst.

Das Bild rollt im Spiel waagrecht (RenderTypes.breezeWind verschiebt U jedes Bild) – es ist dasselbe nahtlose Windbild wie
beim Windgeist (breeze_wind.py), nur kühler und heller, mit Sandkörnern und kleinen Kieseln statt Blättern.
"""
from __future__ import annotations

from PIL import Image

from . import breeze_wind, ventor
from ..geometry import Model
from ..paint import Ctx, hexc

W, H = breeze_wind.W, breeze_wind.H

PALETTES = {
    "wind": {
        "haze": hexc("#bfe6ee"), "edge": hexc("#9cdcea"), "core": hexc("#f4feff"),
        "dust": hexc("#b49a62"), "leaf": hexc("#d4bc84"), "leaf_dark": hexc("#927a4a"),
        "leaf2": hexc("#a89a86"), "leaf2_dark": hexc("#6e6252"),
    },
}
for _p in PALETTES.values():
    _p["back"] = [_p["haze"]]

#: je Ring zwei Kästen: (halbe Breite außen, halbe Breite innen)
BOXES = ((4.9, 3.1), (3.9, 2.4), (2.9, 1.7), (1.9, 0.9))


def build() -> Model:
    base = ventor.build()
    m = Model("ventor_wind", W, H)

    def copy(b, parent: str):
        for ch in b.children:
            m.bone(ch.name, parent, pivot=ch.pivot, rotation=ch.rotation)
            copy(ch, ch.name)

    copy(base.root, "root")
    for i, ((outer, inner), (_, height)) in enumerate(zip(BOXES, ventor.RINGS)):
        name = f"ring_{i}"
        m.cube(name, (-outer, 0, -outer), (2 * outer, height, 2 * outer), "wind")
        m.cube(name, (-inner, 0, -inner), (2 * inner, height, 2 * inner), "wind")
    return m


def full_texture(variant: str = "wind") -> tuple[Image.Image, Image.Image]:
    """Das ganze Windbild (W x H), nahtlos – als Textur der Wind-Ebene (sie rollt)."""
    pal = PALETTES[variant]
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    px = img.load()
    for v in range(H):
        for u in range(W):
            c = breeze_wind._speck_at(u, v, pal) or breeze_wind.wind_rgba(u, v, pal)
            if c is not None:
                px[u, v] = c
    return img, Image.new("RGBA", (W, H), (0, 0, 0, 0))


def _wind(ctx: Ctx):
    return breeze_wind._speck_at(ctx.u, ctx.v, ctx.pal) or breeze_wind.wind_rgba(ctx.u, ctx.v, ctx.pal)


def materials(variant: str) -> dict:
    return {"wind": _wind}


def hidden(variant: str) -> list[str]:
    return []
