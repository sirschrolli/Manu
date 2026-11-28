"""
Tropenfische (Rifffische) in den zwei Vanilla-Formen, mit dem Färbe-System von Vanilla: eine helle GRAUE
Grundtextur je Form wird mit der Grundfarbe (baseColor) eingefärbt, darüber liegt eines von sechs grauen Mustern je
Form (Alpha-Maske plus Grau-Schattierung), eingefärbt mit der Musterfarbe (patternColor). Die Augen sind in der
Grundtextur dunkel und in keinem Muster bedeckt.

  * Form A (TropicalFishSmallModel – Knochen body, tail, right_fin, left_fin, top_fin; die Klasse liest "tail"):
    schlanker Riffbarsch – Schnauze, Rückenflosse mit Bogen, kleine Afterflosse, leicht gegabelter Schwanzfächer
  * Form B (TropicalFishLargeModel – zusätzlich bottom_fin; die Klasse liest "tail"): hochrückiger Scheibenfisch –
    abgerundeter Leib (Kern mit Stirn- und Hinterkappe), kleines Maul, nach hinten hochgezogene Rücken- und
    Afterflosse, Halbmond-Schwanz

Alle Vanilla-Knochen bleiben mit Namen, Eltern und Drehpunkten erhalten; neue Teile sind Quader der Vanilla-Knochen.

Muster (Nummer = Reihenfolge der Vanilla-Muster in TropicalFish.Pattern):
  A: 1 Kob – Querbinden, 2 Sunstreak – Längsstreif, 3 Snooper – Kapuze (Gesicht, Rücken, Rückenflosse),
     4 Dasher – Doppelstreif (oben und unten), 5 Brinely – Flossen und Schwanzende, 6 Spotty – Punkte
  B: 1 Flopper – Halbmond (Bogen hinter dem Kopf, Schwanzspitzen), 2 Stripey – Bänder (eines durchs Auge),
     3 Glitter – Glitzertupfen, 4 Blockfish – Oberseite, 5 Betty – Schrägstreifen,
     6 Clayfish – Flossensaum und Augenfleck

Texturen (alle 32x32): rifffisch_a, rifffisch_a_muster_1 … _6, rifffisch_b, rifffisch_b_muster_1 … _6
(write_textures(ordner) schreibt alle 14). Muster-Geometrie: build_small(pattern=True) / build_large(pattern=True)
– jeder Quader um 0.008 aufgebläht wie Vanillas FISH_PATTERN_DEFORMATION; die UVs sind dieselben.
"""
from __future__ import annotations

import math
import os
from types import SimpleNamespace

from ..geometry import Model
from ..paint import Ctx, paint_model

PATTERN_INFLATE = 0.008
PATTERN_NAMES = {
    "a": ("kob", "sunstreak", "snooper", "dasher", "brinely", "spotty"),
    "b": ("flopper", "stripey", "glitter", "blockfish", "betty", "clayfish"),
}


def _names(shape: str) -> list[str]:
    return [f"rifffisch_{shape}"] + [f"rifffisch_{shape}_muster_{i}" for i in range(1, 7)]


# Die Texturen sind grau; die Paletten tragen nur die Form und die Musternummer (0 = Grundtextur).
PALETTES_A = {n: {"shape": "a", "pattern": i} for i, n in enumerate(_names("a"))}
PALETTES_B = {n: {"shape": "b", "pattern": i} for i, n in enumerate(_names("b"))}
PALETTES = PALETTES_A


# =============================================================================================
# Geometrie
# =============================================================================================

def build_small(pattern: bool = False) -> Model:
    """Form A – für TropicalFishSmallModel (und mit pattern=True für dessen Muster-Schicht)."""
    g = PATTERN_INFLATE if pattern else 0.0
    m = Model("fish_tropical_a" + ("_pattern" if pattern else ""), 32, 32)
    m.bone("body", pivot=(0, 22, 0))
    m.cube("body", (-1, -1.5, -3), (2, 3, 6), "body", inflate=g)
    m.cube("body", (-0.5, -0.5, -4), (1, 2, 1), "snout", inflate=g)     # spitze Schnauze (untere zwei Reihen)
    m.cube("body", (0, 1.5, 0), (0, 1, 3), "anal", inflate=g)           # Afterflosse
    m.bone("tail", pivot=(0, 22, 3))
    m.cube("tail", (0, -2.5, 0), (0, 5, 4), "tail", inflate=g)
    m.bone("right_fin", pivot=(-1, 22.5, 0), rotation=(0, 45, 0))
    m.cube("right_fin", (-2, -1, 0), (2, 2, 0), "pec", inflate=g)
    m.bone("left_fin", pivot=(1, 22.5, 0), rotation=(0, -45, 0))
    m.cube("left_fin", (0, -1, 0), (2, 2, 0), "pec", inflate=g)
    m.bone("top_fin", pivot=(0, 20.5, -3))
    m.cube("top_fin", (0, -2, 1), (0, 2, 5), "dorsal", inflate=g)
    return m


def build_large(pattern: bool = False) -> Model:
    """Form B – für TropicalFishLargeModel (und mit pattern=True für dessen Muster-Schicht)."""
    g = PATTERN_INFLATE if pattern else 0.0
    m = Model("fish_tropical_b" + ("_pattern" if pattern else ""), 32, 32)
    m.bone("body", pivot=(0, 19, 0))
    m.cube("body", (-1, -3, -2), (2, 6, 4), "body", inflate=g)          # Kern
    m.cube("body", (-1, -2, -3), (2, 4, 1), "body", inflate=g)          # Stirn
    m.cube("body", (-1, -2, 2), (2, 4, 1), "body", inflate=g)           # Hinterkappe
    m.cube("body", (-0.5, 0, -4), (1, 1, 1), "snout", inflate=g)        # kleines Maul
    m.bone("tail", pivot=(0, 19, 3))
    m.cube("tail", (0, -3, 0), (0, 6, 4), "tail", inflate=g)
    m.bone("right_fin", pivot=(-1, 20, 0), rotation=(0, 45, 0))
    m.cube("right_fin", (-2, 0, 0), (2, 2, 0), "pec", inflate=g)
    m.bone("left_fin", pivot=(1, 20, 0), rotation=(0, -45, 0))
    m.cube("left_fin", (0, 0, 0), (2, 2, 0), "pec", inflate=g)
    m.bone("top_fin", pivot=(0, 16, -3))
    m.cube("top_fin", (0, -4, 0), (0, 5, 6), "dorsal", inflate=g)       # unterste Reihe füllt die Lücke über der Kappe
    m.bone("bottom_fin", pivot=(0, 22, -3))
    m.cube("bottom_fin", (0, -1, 0), (0, 5, 6), "anal", inflate=g)
    return m


def build() -> Model:
    return build_small()


def shape_module(shape: str, pattern: bool = False) -> SimpleNamespace:
    """Modul-Objekt für write_model: Grundgeometrie mit allen sieben Texturen der Form, Muster-Geometrie ohne
    Texturen (PALETTES leer – nur Java; die UVs sind dieselben wie bei der Grundgeometrie)."""
    builder = build_small if shape == "a" else build_large
    pals = {} if pattern else (PALETTES_A if shape == "a" else PALETTES_B)
    return SimpleNamespace(PALETTES=pals, build=lambda: builder(pattern), materials=materials, hidden=hidden,
                           preview_pose=preview_pose)


def write_textures(out_dir: str) -> list[str]:
    """Schreibt alle 14 Texturen (rifffisch_a*.png, rifffisch_b*.png) nach ``out_dir``; gibt die Pfade zurück."""
    os.makedirs(out_dir, exist_ok=True)
    paths = []
    for builder, pals in ((build_small, PALETTES_A), (build_large, PALETTES_B)):
        model = builder()
        model.pack()
        for i, (name, pal) in enumerate(pals.items()):
            img, _ = paint_model(model, materials(name), pal, seed=100 + i, params={"variant": name})
            path = os.path.join(out_dir, f"{name}.png")
            img.save(path)
            paths.append(path)
    return paths


def hidden(variant: str) -> list[str]:
    return []


def preview_pose(variant: str) -> dict:
    return {"tail": (0, 12, 0)}


# =============================================================================================
# Raster: Spalte c (entlang z, 0 = vorderste Leibspalte) und Reihe r (0 = oberste Leibreihe) je Texel
# =============================================================================================

_GRID = {"a": SimpleNamespace(row0=20.5, rows=3, eye=(0, 0)),
         "b": SimpleNamespace(row0=16.0, rows=6, eye=(0, 2))}

# Flossenformen: erlaubte (Spalte, Reihe)
_SHAPES = {
    "a": {
        "dorsal": {(c, -1) for c in range(1, 6)} | {(c, -2) for c in range(2, 5)},
        "anal": {(c, 3) for c in range(3, 6)},
        "tail": ({(6, r) for r in range(0, 3)} | {(7, r) for r in range(-1, 4)} | {(8, r) for r in range(-1, 4)}
                 | {(9, -1), (9, 0), (9, 2), (9, 3)}),
    },
    "b": {
        "dorsal": {(c, r) for c, top in ((1, -1), (2, -2), (3, -3), (4, -4), (5, -4)) for r in range(top, 1)},
        "anal": {(c, r) for c, bottom in ((1, 6), (2, 7), (3, 8), (4, 9), (5, 9)) for r in range(5, bottom + 1)},
        "tail": ({(6, r) for r in range(1, 5)} | {(7, r) for r in range(0, 6)} | {(8, 0), (8, 1), (8, 4), (8, 5)}
                 | {(9, 0), (9, 5)}),
    },
}


def _shape(ctx: Ctx) -> str:
    return ctx.pal.get("shape", "a")


def _cell(ctx: Ctx, part: str) -> tuple[int, int]:
    """(Spalte, Reihe) des Texels; Ober-/Unter-/Stirn-/Rückseiten zählen zur angrenzenden Randspalte bzw. -reihe
    (die Brustflossen sind dünne Flächen – beide Seiten gleich)."""
    g = _GRID[_shape(ctx)]
    _, y, z = ctx.world
    if part == "pec":
        pass
    elif ctx.face == "top":
        y += 0.5
    elif ctx.face == "bottom":
        y -= 0.5
    elif ctx.face == "front":
        z += 0.5
    elif ctx.face == "back":
        z -= 0.5
    return int(math.floor(z + 3 + 1e-6)), int(math.floor(y - g.row0 + 1e-6))


def _rel(ctx: Ctx, r: int) -> float:
    g = _GRID[_shape(ctx)]
    return min(1.0, max(0.0, r / (g.rows - 1)))


def _edge(shape_set: set, c: int, r: int) -> bool:
    return any((c + dc, r + dr) not in shape_set for dc, dr in ((1, 0), (-1, 0), (0, 1), (0, -1)))


def _gray(v: float) -> tuple:
    v = int(max(0, min(255, round(v))))
    return v, v, v, 255


EYE = (28, 28, 32, 255)
MOUTH = (120, 120, 118, 255)


# =============================================================================================
# Muster
# =============================================================================================

def _pattern_hit(shape: str, n: int, part: str, face: str, c: int, r: int, ix: int) -> bool:
    if shape == "a":
        if n == 1:                                                      # Kob – Querbinden
            return c in (1, 3, 6) and part != "snout"
        if n == 2:                                                      # Sunstreak – Längsstreif
            if part == "dorsal":
                return r == -2
            return r == 1 and c <= 8 and face not in ("top", "bottom") and part != "pec"
        if n == 3:                                                      # Snooper – Kapuze
            if part in ("snout", "dorsal"):
                return True
            if part == "tail":
                return r <= 0
            return part == "body" and (r == 0 or face == "top")
        if n == 4:                                                      # Dasher – Doppelstreif oben und unten
            if part not in ("body", "snout", "tail") or face in ("top", "bottom"):
                return False
            return r in (0, 2)
        if n == 5:                                                      # Brinely – Flossen und Schwanzende
            return part in ("dorsal", "anal", "tail", "pec") or (part == "body" and c >= 4)
        if n == 6:                                                      # Spotty – Punkte
            if part in ("snout", "pec", "anal"):
                return False
            if face == "top":
                return (c + ix) % 4 == 1
            if face in ("bottom", "front", "back"):
                return False
            return (c + 2 * r) % 4 == 1
        return False
    # Form B
    if n == 1:                                                          # Flopper – Halbmond
        if part == "tail":
            return c >= 8
        if part in ("dorsal", "anal"):
            return c == 1
        if part != "body":
            return False
        if face == "top" or face == "bottom":
            return c == 1
        return (c == 2 and 1 <= r <= 4) or (c == 1 and r in (0, 5))
    if n == 2:                                                          # Stripey – Bänder
        if part == "snout":
            return False
        return c in (0, 3, 6)
    if n == 3:                                                          # Glitter – Glitzertupfen
        if part in ("snout", "pec"):
            return False
        if face in ("top", "bottom"):
            return (c + 2 * ix) % 3 == 0
        return (c + 3 * r) % 5 == 0
    if n == 4:                                                          # Blockfish – Oberseite
        if part in ("dorsal",):
            return True
        if part in ("anal", "snout", "pec"):
            return False
        return r <= 2 or face == "top"
    if n == 5:                                                          # Betty – Schrägstreifen (Leib und Schwanz)
        if part not in ("body", "tail"):
            return False
        return (c + r) % 4 == 0
    if n == 6:                                                          # Clayfish – Flossensaum und Augenfleck
        if part in ("dorsal", "anal", "tail"):
            return _edge(_SHAPES["b"][part], c, r)
        if part != "body" or face not in ("west", "east"):
            return False
        return 3 <= c <= 5 and 1 <= r <= 3 and (c, r) != (4, 2)
    return False


# =============================================================================================
# Malen
# =============================================================================================

def _painter(part: str):
    def paint(ctx: Ctx):
        shape = _shape(ctx)
        n = ctx.pal.get("pattern", 0)
        c, r = _cell(ctx, part)
        g = _GRID[shape]
        shapes = _SHAPES[shape]
        fin_set = shapes.get(part)
        if fin_set is not None and (c, r) not in fin_set:
            return None                                                 # außerhalb der Flossenform
        side = ctx.face in ("west", "east")
        is_eye = part == "body" and side and (c, r) == g.eye
        is_mouth = shape == "b" and part == "snout" and ctx.face == "front"
        if n:
            if is_eye or is_mouth:
                return None
            ix = int(math.floor(ctx.world[0] + 1))
            if not _pattern_hit(shape, n, part, ctx.face, c, r, ix):
                return None
            lift = 1.0
        else:
            if is_eye:
                return EYE
            if is_mouth:
                return MOUTH
            lift = 0.0
        # Grau-Schattierung: Rücken dunkler, Bauch heller (Gegenschattierung); Muster heller als der Grund
        if part in ("body", "snout"):
            if ctx.face == "top":
                v = 206
            elif ctx.face == "bottom":
                v = 252
            else:
                v = 214 + 38 * _rel(ctx, r)
                if ctx.face in ("front", "back"):
                    v -= 8
                elif c == 1 and part == "body" and 0 <= r < g.rows:
                    v -= 10                                             # Kiemendeckel-Rand
            return _gray(v + lift * (255 - v) * 0.6)
        if part == "pec":
            return _gray(232 + lift * 14)
        # Flossen: Strahlen und hellerer Saum
        ray = (c % 2 == 1) if part in ("dorsal", "anal") else (r % 2 == 1)
        v = 226 - (8 if ray else 0)
        if _edge(fin_set, c, r):
            v = 240
        return _gray(v + lift * (255 - v) * 0.6)

    return paint


def materials(variant: str) -> dict:
    return {part: _painter(part) for part in ("body", "snout", "tail", "dorsal", "anal", "pec")}
