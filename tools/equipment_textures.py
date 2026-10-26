#!/usr/bin/env python3
"""
Texturen für Werkzeuge, Waffen und Rüstungen der Mod (eigene Pixelkunst im Vanilla-Stil).

  * Gegenstände (16x16): Stahl-Werkzeuge, Draconium-Waffen, Rüstungen aus Stahl, Konterit und
    Drachenschuppen, Wyvernit-Gleiter, Edelstein-Krone, Drachenpanzer
  * getragene Rüstung (64x32, Schema von HumanoidModel): textures/entity/equipment/humanoid(_leggings)/
  * Gleiter-Flügel (64x32, Schema des Elytren-Modells): textures/entity/equipment/wings/

Die Rüstungs-Symbole folgen den üblichen Umrissen (Helm, Brustpanzer, Hose, Stiefel), damit die
Rüstungsbesatz-Muster (Trims) aus dem Schmiedetisch sauber darauf liegen.

Wird von tools/generate_textures.py aufgerufen: build(put) für die Gegenstände,
build_entity(save) für die Rüstungs-Ebenen.
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
KONTERIT = tones("#2a0c30", "#4f1958", "#76287e", "#a03ea6", "#c763cc", "#e59ae8", "#ffe0fd")
DRACONIUM = tones("#120406", "#33090d", "#5a1015", "#86191c", "#b42e22", "#e0582c", "#ffb052")
WYVERNIT = tones("#1d3550", "#34577a", "#5683ab", "#80acd0", "#abcfe9", "#d4ebfa", "#ffffff")
GOLD = {"g": hexc("#6e4a10"), "G": hexc("#c89432"), "y": hexc("#f6d978"), "Y": hexc("#fff4c4")}
BLUE = {"x": hexc("#14245a"), "X": hexc("#2c4ea8"), "z": hexc("#6a92e6")}
EMBER = {"x": hexc("#7a1400"), "X": hexc("#e2541a"), "z": hexc("#ffd36a")}
CRYSTAL = {"x": hexc("#5a1a62"), "X": hexc("#e070e8"), "z": hexc("#fff0ff")}
GEMS = {"r": hexc("#a0142a"), "R": hexc("#ff5a6a"), "v": hexc("#4a1d78"), "V": hexc("#b47ef0"),
        "n": hexc("#0f6a3a"), "N": hexc("#5ef0a0")}

# Drachenschuppen je Art: Umriss + 6 Töne, dazu Grat-/Akzentfarbe (x, X, z)
SCALES = {
    "fire": (tones("#2a0602", "#5a1206", "#8c2208", "#c23c10", "#ec6a20", "#ffa844", "#ffd88a"),
             {"x": hexc("#3a1a10"), "X": hexc("#ffc04a"), "z": hexc("#fff0b0")}),
    "ice": (tones("#1e3e5e", "#3f6f9a", "#6b9ac4", "#97c0e2", "#c2ddf4", "#e6f4ff", "#ffffff"),
            {"x": hexc("#2a5a8a"), "X": hexc("#bff0ff"), "z": hexc("#ffffff")}),
    "lightning": (tones("#07080b", "#15171d", "#24272f", "#363a45", "#4c5160", "#6a7082", "#9aa0b4"),
                  {"x": hexc("#6a5000"), "X": hexc("#ffd42a"), "z": hexc("#fff6b0")}),
    "nature": (tones("#0a240e", "#163d1b", "#245e2a", "#38823a", "#58a64c", "#86c868", "#c8ec8a"),
               {"x": hexc("#3a2a12"), "X": hexc("#c2e86a"), "z": hexc("#f0ffc0")}),
    "earth": (tones("#1c1208", "#3a2614", "#584024", "#7a5a36", "#9c7a4e", "#c0a070", "#e6d0a0"),
              {"x": hexc("#2e2f32"), "X": hexc("#a9adb2"), "z": hexc("#e4e6ea")}),
    "water": (tones("#031a30", "#08345c", "#0f5288", "#1c78b4", "#3aa2dc", "#7ccaf0", "#c4f2ff"),
              {"x": hexc("#0a3a4a"), "X": hexc("#7aeaff"), "z": hexc("#e0ffff")}),
}


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


def _ramp(colors: dict[str, Color]) -> list[Color]:
    return [colors[k] for k in "oabcdef"]


def _v(r: list[Color], value: float) -> Color:
    """0..1 -> Ton aus Umriss + 6 Stufen (0 = Umriss)."""
    return r[max(0, min(6, int(round(value * 6))))]


class Style:
    """Farben und Eigenheiten eines Rüstungs-Werkstoffs."""

    def __init__(self, metal: dict[str, Color], trim: dict[str, Color], accent: dict[str, Color], kind: str):
        self.m = _ramp(metal)
        self.trim = trim            # g (dunkel), G, y, Y (hell)
        self.accent = accent        # x (dunkel), X, z (hell)
        self.kind = kind            # "plate", "light", "scale"

    def metal(self, value: float, x: int = 0, y: int = 0, face: str = "front") -> Color:
        """Grundmetall; Schuppen-Werkstoffe bekommen versetzte Schuppenbögen."""
        if self.kind == "scale":
            row = y // 2
            local = (x + (row % 2) * 2) % 4
            if y % 2 == 1 and local in (0, 3):
                value -= 0.17
            elif y % 2 == 0 and local in (1, 2):
                value += 0.08
        n = (h01(x, y, face, self.kind) - 0.5) * 0.08
        return _v(self.m, value + n)


def side_shade(face: str, x: int, w: int) -> float:
    """Licht von vorne links: Vorderseite hell, Seiten mittel, Rücken dunkler; Ränder dunkler."""
    base = {"front": 0.0, "right": -0.06, "left": -0.1, "back": -0.12, "top": 0.06, "bottom": -0.25}[face]
    if face in SIDE_FACES and w > 2 and (x == 0 or x == w - 1):
        base -= 0.06
    return base


def paint_helmet(layer: ArmorLayer, st: Style) -> None:
    def head(face, x, y, w, h):
        v = 0.62 + side_shade(face, x, w) - y * 0.015
        if face == "top":
            if x in (3, 4):
                return st.trim["y"] if x == 3 else st.trim["G"]           # Kammgrat
            return st.metal(0.7 + (0.06 if y < 2 else 0.0), x, y, face)
        if face == "bottom":
            return None
        if st.kind == "light":
            # offener Helm: Gesicht frei, Wangenschutz und Stirnband mit Kristall
            if face == "front":
                if y <= 1:
                    if y == 1 and x in (3, 4):
                        return st.accent["X"] if x == 3 else st.accent["z"]
                    return st.trim["y"] if y == 1 else st.metal(0.7, x, y, face)
                if x in (0, 7) and y <= 6:
                    return st.metal(0.55 - y * 0.02, x, y, face)
                return None
            if y == 1:
                return st.trim["G"]
            if y >= 7 and face in ("right", "left"):
                return None
            return st.metal(v, x, y, face)
        if face == "front":
            if y == 2:
                return st.trim["y"] if x in (0, 7) or x % 2 == 1 else st.trim["G"]
            if y == 3:
                # Sehschlitz (durchsichtig: man sieht die Augen)
                if 1 <= x <= 6:
                    return None
                return st.metal(0.45, x, y, face)
            if st.kind == "scale":
                if y in (0, 1) and x in (1, 6):
                    return st.accent["X"]                                 # Hornansätze
                if y == 4 and x in (2, 5):
                    return st.accent["x"]                                 # Nüstern
            elif y >= 5 and x in (2, 5) and (y % 2 == 1):
                return st.m[1]                                            # Atemlöcher
            if x in (3, 4) and y >= 4:
                return st.metal(0.78 if x == 3 else 0.6, x, y, face)     # Mittelgrat
            return st.metal(v + 0.04, x, y, face)
        if y == 2:
            return st.trim["G"] if (x + y) % 3 else st.trim["y"]
        if y == h - 1:
            return st.metal(v - 0.18, x, y, face)
        if st.kind == "plate" and face in ("right", "left") and y == 5 and x in (2, 5):
            return st.trim["y"]                                           # Nieten
        return st.metal(v, x, y, face)

    layer.paint("head", ("top", "right", "front", "left", "back"), head)

    # Hut-Schicht: Federbusch (Stahl) bzw. Grat (Schuppen); leichte Rüstung ohne
    if st.kind == "plate":
        def plume(face, x, y, w, h):
            if face == "top" and x in (3, 4):
                return st.accent["z"] if x == 3 else st.accent["X"]
            if face == "back" and x in (3, 4) and y <= 2:
                return st.accent["X"] if x == 3 else st.accent["x"]
            return None
        layer.paint("hat", ("top", "back"), plume)
    elif st.kind == "scale":
        def crest(face, x, y, w, h):
            if face == "top" and x in (3, 4) and y % 2 == 0:
                return st.accent["X"] if x == 3 else st.accent["x"]
            if face == "back" and x in (3, 4) and y <= 1:
                return st.accent["X"]
            return None
        layer.paint("hat", ("top", "back"), crest)


def paint_chestplate(layer: ArmorLayer, st: Style) -> None:
    def body(face, x, y, w, h):
        v = 0.6 + side_shade(face, x, w)
        if face == "top":
            return st.metal(0.72, x, y, face)
        if face == "bottom":
            return st.metal(0.3, x, y, face)
        if st.kind == "light" and y >= 9:
            return None                                                    # nur Kürass, darunter Kleidung
        if y == 8:
            return st.trim["y"] if (face == "front" and x in (3, 4)) else st.trim["G"]   # Gürtel mit Schnalle
        if y > 8:
            # Panzerschurz aus überlappenden Schienen
            if st.kind == "scale":
                return st.metal(v - 0.04, x, y, face)
            return st.metal(v - (0.0 if y % 2 == 1 else 0.14), x, y, face)
        if face == "front":
            if st.kind == "scale" and 2 <= x <= 5:
                # helle Bauchplatten wie beim Drachen
                return _v(st.m, 0.82 - (0.12 if y % 2 == 1 else 0.0) - (0.06 if x in (2, 5) else 0.0))
            if st.kind == "light" and x in (3, 4) and y in (3, 4):
                return st.accent["z"] if (x + y) % 2 == 0 else st.accent["X"]   # Kristall-Einlage
            if x in (3, 4) and st.kind != "scale":
                return st.metal(0.86 if x == 3 else 0.66, x, y, face)        # Mittelgrat
            if st.kind == "plate" and y == 1 and x in (1, 6):
                return st.trim["y"]                                           # Nieten
            return st.metal(v + 0.06 - abs(x - 3.5) * 0.02 - y * 0.012, x, y, face)
        if face == "back" and x in (3, 4) and st.kind == "scale":
            return st.accent["X"] if y % 2 == 0 else st.accent["x"]           # Rückengrat
        return st.metal(v - y * 0.01, x, y, face)

    def arm(face, x, y, w, h):
        v = 0.58 + side_shade(face, x, w)
        if face == "top":
            return st.metal(0.74, x, y, face)
        if face == "bottom":
            return st.metal(0.3, x, y, face) if st.kind != "light" else None
        if y <= 3:
            # Schulterstück
            if y == 3:
                return st.trim["G"] if face != "back" else st.trim["g"]
            if st.kind == "scale" and y == 0 and face in ("right", "front", "back") and x % 2 == 0:
                return st.accent["X"]                                         # Schulterstacheln
            return st.metal(v + 0.12 - y * 0.03, x, y, face)
        if st.kind == "light":
            if 8 <= y <= 10:
                return st.trim["G"] if y in (8, 10) else st.metal(0.62, x, y, face)   # Armschiene
            return None
        if y == 6:
            return st.metal(0.8 if face == "front" else v + 0.08, x, y, face)        # Ellbogenkachel
        if y >= 10:
            return st.metal(v - 0.12, x, y, face)                                       # Handschuh
        if y == 9:
            return st.trim["g"]
        return st.metal(v - 0.02, x, y, face)

    layer.paint("body", ("top", "bottom") + SIDE_FACES, body)
    layer.paint("arm", ("top", "bottom") + SIDE_FACES, arm)


def paint_boots(layer: ArmorLayer, st: Style) -> None:
    def leg(face, x, y, w, h):
        if face == "top":
            return None
        if face == "bottom":
            return st.metal(0.22, x, y, face)
        start = 6 if st.kind != "light" else 7
        if y < start:
            return None
        v = 0.58 + side_shade(face, x, w)
        if y == start:
            return st.trim["G"] if face != "back" else st.trim["g"]
        if st.kind == "scale" and y == h - 1 and face == "front":
            return st.accent["z"] if x % 2 == 0 else st.accent["x"]           # Krallen
        if face == "front" and y >= h - 3 and st.kind == "plate":
            return st.metal(0.78 - (y - (h - 3)) * 0.12, x, y, face)          # Zehenkappe
        if y == h - 1:
            return st.metal(v - 0.2, x, y, face)
        return st.metal(v - (y - start) * 0.02, x, y, face)

    layer.paint("leg", ("top", "bottom") + SIDE_FACES, leg)


def paint_leggings(layer: ArmorLayer, st: Style) -> None:
    def body(face, x, y, w, h):
        if face in ("top", "bottom") or y < 8:
            return None
        if y == 8:
            return st.trim["y"] if (face == "front" and x in (3, 4)) else st.trim["G"]
        return st.metal(0.56 + side_shade(face, x, w) - (0.0 if y % 2 == 1 else 0.1), x, y, face)

    def leg(face, x, y, w, h):
        if face == "bottom":
            return None
        if face == "top":
            return st.metal(0.5, x, y, face)
        v = 0.58 + side_shade(face, x, w)
        if st.kind == "light":
            if y <= 4:
                return st.metal(v - (0.12 if y == 4 else 0.0), x, y, face) if face != "back" else None   # Beintaschen
            if y in (5, 6):
                return st.trim["G"] if y == 5 else st.metal(0.7, x, y, face)          # Kniekachel
            return None
        if y == 5:
            return st.trim["G"] if face != "front" else st.trim["y"]                  # Kniekachel-Rand
        if y == 6 and face == "front":
            return st.metal(0.84, x, y, face)
        if y <= 4:
            return st.metal(v + 0.04 - y * 0.02, x, y, face)                          # Beinröhre oben
        return st.metal(v - 0.04, x, y, face)                                          # Beinschiene

    layer.paint("body", ("top", "bottom") + SIDE_FACES, body)
    layer.paint("leg", ("top", "bottom") + SIDE_FACES, leg)


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


STEEL_STYLE = Style(STEEL, GOLD, BLUE, "plate")
KONTERIT_STYLE = Style(KONTERIT, {"g": STEEL["b"], "G": STEEL["d"], "y": STEEL["e"], "Y": STEEL["f"]}, CRYSTAL, "light")


def scale_style(kind: str) -> Style:
    pal, accent = SCALES[kind]
    return Style(pal, {"g": accent["x"], "G": accent["X"], "y": accent["z"], "Y": accent["z"]}, accent, "scale")


def armor_layers(style: Style) -> tuple[Image.Image, Image.Image]:
    outer = ArmorLayer()
    paint_helmet(outer, style)
    paint_chestplate(outer, style)
    paint_boots(outer, style)
    inner = ArmorLayer()
    paint_leggings(inner, style)
    return outer.img, inner.img


def build_entity(save: Callable[[str, Image.Image], None]) -> None:
    """Schreibt die Rüstungs-Ebenen (Pfade relativ zu textures/)."""
    sets = {"steel": STEEL_STYLE, "konterit": KONTERIT_STYLE}
    for kind in SCALES:
        sets[f"{kind}_dragon_scale"] = scale_style(kind)
    for name, style in sets.items():
        outer, inner = armor_layers(style)
        save(f"entity/equipment/humanoid/{name}", outer)
        save(f"entity/equipment/humanoid_leggings/{name}", inner)
    crown = ArmorLayer()
    paint_crown(crown)
    save("entity/equipment/humanoid/gemstone_crown", crown.img)
    wings = Image.new("RGBA", (64, 32), CLEAR)
    paint_glider(wings)
    save("entity/equipment/wings/wyvernit_glider", wings)
