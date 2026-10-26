#!/usr/bin/env python3
"""
Restliche Ausrüstungs-Texturen (eigene Pixelkunst im Vanilla-Stil):

  * Gegenstände (16x16): Wyvernit-Gleiter (heil und beschädigt), Königskrone, Drachenpanzer aus Stahl und Draconium
  * getragene Krone (64x32, Kopf-Schema von HumanoidModel): textures/entity/equipment/humanoid/gemstone_crown
  * Gleiter-Flügel (64x32, Schema des Elytren-Modells): textures/entity/equipment/wings/

Waffen, Werkzeuge und Rüstungs-Symbole: tools/items_gear.py und tools/items_armor.py,
getragene Rüstungen: tools/armor_worn.py.

Wird von tools/generate_textures.py aufgerufen: build(put) für die Gegenstände, build_entity(save) für Krone
und Gleiter-Flügel.
"""
from __future__ import annotations

import hashlib
from typing import Callable

from PIL import Image

import pixelart as pa
from pixelart import CLEAR, Color, hexc

# =============================================================================
# Farben (o = Umriss, a..f = dunkel -> hell)
# =============================================================================


def tones(*values: str) -> dict[str, Color]:
    keys = "oabcdef"
    return {k: hexc(v) for k, v in zip(keys, values)}


STEEL = tones("#1f242c", "#39404b", "#566070", "#7b8696", "#a2adbb", "#cad3dd", "#f1f5f9")
DRACONIUM = tones("#120406", "#33090d", "#5a1015", "#86191c", "#b42e22", "#e0582c", "#ffb052")
WYVERNIT = tones("#1d3550", "#34577a", "#5683ab", "#80acd0", "#abcfe9", "#d4ebfa", "#ffffff")
GOLD = {"g": hexc("#6e4a10"), "G": hexc("#c89432"), "y": hexc("#f6d978"), "Y": hexc("#fff4c4")}
EMBER = {"x": hexc("#7a1400"), "X": hexc("#e2541a"), "z": hexc("#ffd36a")}
GEMS = {"r": hexc("#a0142a"), "R": hexc("#ff5a6a"), "v": hexc("#4a1d78"), "V": hexc("#b47ef0"),
        "n": hexc("#0f6a3a"), "N": hexc("#5ef0a0")}

def h01(*args) -> float:
    return hashlib.md5(repr(args).encode()).digest()[0] / 255.0


# =============================================================================
# Werkzeuge und Waffen (16x16)
# =============================================================================

# =============================================================================
# Rüstungs-Symbole (Umrisse wie die üblichen Rüstungsteile)
# =============================================================================

# Edelstein-Krone
CROWN = [
    "................",
    "................",
    "................",
    "..o....o....o...",
    ".oYo..oYo..oYo..",
    ".oyo.oyRyo.oyo..",
    ".oyGooyrGoogyo..",
    ".oyGGyGGGGGGyo..",
    ".oyNGyVGGRGGyo..",
    ".oynGyvGGrGGgo..",
    ".oyGGGGGGGGGgo..",
    ".oYyyyyyyyyyGo..",
    "..oooooooooooo..",
    "................",
    "................",
    "................",
]

# Wyvernit-Gleiter: zwei Schwingen aus Flughaut mit Knochenstreben
GLIDER = [
    "................",
    "..ooo......ooo..",
    ".offeo....oeffo.",
    ".ofeddo..oddefo.",
    ".oeddcbooocddeo.",
    ".oedcbcoxocbcdo.",
    "..odcbcoxocbcdo.",
    "..odcbbo.obbcdo.",
    "..ocbbao.oabbco.",
    "..ocbaao.oaabco.",
    "...obaao.oaabo..",
    "...oba.o.o.abo..",
    "....oa..o..ao...",
    "....o.......o...",
    "................",
    "................",
]

GLIDER_BROKEN = [
    "................",
    "..ooo......ooo..",
    ".offeo....oeffo.",
    ".ofe.do..od.efo.",
    ".oed..booo..deo.",
    ".oed.ocoxoco.do.",
    "..od.o.oxo.o.do.",
    "..ocb.bo.ob.bco.",
    "..o.bb.o.o.bb.o.",
    "...o.aao.oaa.o..",
    "...ob..o.o..bo..",
    "....o..o.o..o...",
    "................",
    "................",
    "................",
    "................",
]

# Drachenpanzer: gepanzerter Drachenkopf im Profil (wie die Pferderüstung einen Pferdekopf zeigt)
DRAGON_ARMOR = [
    "................",
    "................",
    ".oo.............",
    ".oyoo...........",
    "..oyGo.ooo......",
    "...oGGoeffoo....",
    "....odfeeeeoooo.",
    "...odfeexXeeeedo",
    "...ofeedxxddddco",
    "...oeddddccccbao",
    "...odddcbbaaooo.",
    "...ocdcbbaao....",
    "...oyGGGGGyo....",
    "....oooooooo....",
    "................",
    "................",
]


def material_colors(base: dict[str, Color], *extra: dict[str, Color]) -> dict[str, Color]:
    colors = dict(base)
    for e in extra:
        colors.update(e)
    return colors


def build(put: Callable[[str, Image.Image], None]) -> None:
    # Waffen, Werkzeuge und Rüstungs-Symbole: siehe tools/items_gear.py und tools/items_armor.py
    # --- Gleiter, Krone, Drachenpanzer ---
    glider = material_colors(WYVERNIT, {"x": hexc("#e8dcc8")})
    put("item/wyvernit_glider", pa.sprite(GLIDER, glider))
    put("item/wyvernit_glider_broken", pa.sprite(GLIDER_BROKEN, glider))
    put("item/gemstone_crown", pa.sprite(CROWN, material_colors({"o": hexc("#4a2e08")}, GOLD, GEMS)))
    put("item/steel_dragon_armor", pa.sprite(DRAGON_ARMOR, material_colors(STEEL, GOLD, EMBER)))
    put("item/draconium_dragon_armor", pa.sprite(DRAGON_ARMOR, material_colors(DRACONIUM, GOLD, EMBER)))


if __name__ == "__main__":
    import sys

    sheet: dict[str, Image.Image] = {}
    build(lambda key, img: sheet.__setitem__(key, img))
    zoom, pad, cols = 8, 4, 8
    cell = 16 * zoom + pad
    rows = (len(sheet) + cols - 1) // cols
    out = Image.new("RGBA", (cols * cell + pad, rows * cell + pad), (60, 64, 70, 255))
    for i, (key, img) in enumerate(sheet.items()):
        tile = pa.checker(16, 16, (150, 150, 150, 255), (128, 128, 128, 255))
        tile.alpha_composite(img)
        out.alpha_composite(pa.zoom(tile, zoom), (pad + (i % cols) * cell, pad + (i // cols) * cell))
    target = sys.argv[1] if len(sys.argv) > 1 else "/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad/equipment_items.png"
    out.save(target)
    print(f"{len(sheet)} Symbole -> {target}")


# =============================================================================
# Getragene Rüstung (64x32, Schema von HumanoidModel)
# =============================================================================
#
#   Ebene 1 (humanoid):          Helm (Kopf + Hut-Schicht), Brustpanzer (Körper + Arme), Stiefel (Beine)
#   Ebene 2 (humanoid_leggings): Hose (Körper unten + Beine)

LAYER_PARTS = {
    "head": (0, 0, 8, 8, 8), "hat": (32, 0, 8, 8, 8),
    "body": (16, 16, 8, 12, 4), "arm": (40, 16, 4, 12, 4), "leg": (0, 16, 4, 12, 4),
}
SIDE_FACES = ("right", "front", "left", "back")


def layer_rect(part: str, face: str) -> tuple[int, int, int, int]:
    u, v, w, h, d = LAYER_PARTS[part]
    return {
        "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h), "back": (u + d + w + d, v + d, w, h),
    }[face]


class ArmorLayer:
    def __init__(self):
        self.img = Image.new("RGBA", (64, 32), CLEAR)
        self.px = self.img.load()

    def paint(self, part: str, faces, fn: Callable[[str, int, int, int, int], Color | None]):
        for face in faces:
            x0, y0, w, h = layer_rect(part, face)
            for y in range(h):
                for x in range(w):
                    c = fn(face, x, y, w, h)
                    if c is not None:
                        self.px[x0 + x, y0 + y] = c

    def clear(self, part: str, faces, rows: range | None = None):
        for face in faces:
            x0, y0, w, h = layer_rect(part, face)
            for y in (rows if rows is not None else range(h)):
                for x in range(w):
                    self.px[x0 + x, y0 + y] = CLEAR


def paint_crown(layer: ArmorLayer) -> None:
    g = GOLD

    def head(face, x, y, w, h):
        if face in ("top", "bottom") or y > 2:
            return None
        if y == 0:
            # Zacken: an den Ecken und in der Mitte jeder Seite
            if x in (0, w - 1) or x in (w // 2 - 1, w // 2):
                return g["Y"] if x in (0, w // 2 - 1) else g["y"]
            return None
        if y == 1:
            if face == "front" and x in (3, 4):
                return GEMS["R"] if x == 3 else GEMS["r"]                    # Rubin
            if face in ("right", "left") and x in (3, 4):
                return GEMS["V"] if x == 3 else GEMS["v"]                    # Amethyst
            if face == "back" and x in (3, 4):
                return GEMS["N"] if x == 3 else GEMS["n"]                    # Smaragd
            return g["y"] if x % 2 == 0 else g["G"]
        return g["G"] if x % 2 == 0 else g["g"]

    layer.paint("head", SIDE_FACES, head)


def paint_glider(img: Image.Image) -> None:
    """Flughaut-Schwinge auf der Rückseite des Elytren-Quaders (10x20 ab 36,2) plus Kanten."""
    px = img.load()
    w = WYVERNIT
    bone = [w["o"], w["c"], w["e"], w["f"]]
    membrane = [w["a"], w["b"], w["c"], w["d"]]
    x0, y0 = 36, 2
    for y in range(20):
        for x in range(10):
            # x = 0 innen (Schulter), x = 9 außen
            # Vorderkante: von der Schulter (0,0) schräg nach außen bis (9,5), dann außen herunter
            lead_y = x * 0.55
            if y < lead_y - 0.5:
                continue
            # Hinterkante: Bögen zwischen den Streben, innen kürzer
            reach = 19 - (9 - x) * 0.9
            scallop = 1.6 * abs(((x + 0.5) % 3.4) / 3.4 - 0.5) * 2
            if y > reach - scallop * 2.2:
                continue
            c = membrane[min(3, max(0, int(2.6 - (y - lead_y) * 0.09 + h01(x, y, "glider") * 0.8)))]
            if y - lead_y < 1.2:
                c = bone[2] if y - lead_y < 0.6 else bone[1]                  # Knochen der Vorderkante
            elif x == 9:
                c = bone[1]
            else:
                # Fingerknochen: von außen oben (9,5) fächerförmig nach innen unten
                for ang in (0.9, 1.6, 2.6):
                    if abs((9 - x) * ang - (y - 5)) < 0.7 and y > 5:
                        c = bone[1]
                        break
            px[x0 + x, y0 + y] = c
    # Oberkante (Deckfläche 24,0 bzw. 34,0) und Seitenflächen als Knochen
    for x in range(10):
        px[24 + x, 0] = bone[2]
        px[24 + x, 1] = bone[1]
    for y in range(20):
        px[22, 2 + y] = bone[1] if y < 14 else CLEAR
        px[34, 2 + y] = bone[1] if y < 6 else CLEAR
        px[35, 2 + y] = bone[1] if y < 6 else CLEAR


def build_entity(save: Callable[[str, Image.Image], None]) -> None:
    """Schreibt Krone und Gleiter-Flügel (Pfade relativ zu textures/); die Rüstungen kommen aus tools/armor_worn.py."""
    crown = ArmorLayer()
    paint_crown(crown)
    save("entity/equipment/humanoid/gemstone_crown", crown.img)
    wings = Image.new("RGBA", (64, 32), CLEAR)
    paint_glider(wings)
    save("entity/equipment/wings/wyvernit_glider", wings)
