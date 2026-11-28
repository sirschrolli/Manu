#!/usr/bin/env python3
"""
3D-Modelle der Waffen aus Monsterteilen (0.70), wie sie in der Hand gezeigt werden.

Jede Waffe wird senkrecht um x = z = 8 gebaut (Griff unten, Spitze oben) – aus Quadern und gebogenen, sich verjüngenden
Strängen (Krallen, Klingen, Dornen, Fasern). Ein Strang besteht aus Gliedern; jedes Glied ist ein Quader, um seinen Fuß
gedreht, das nächste setzt an seinem Ende an und ist um den Biegewinkel weiter gedreht. So entstehen echte Bögen statt
Treppen. Die Texturbereiche (UV, Einheiten 0..16 = 32 Pixel) werden hier festgelegt; tools/monster_gear_textures.py malt
sie aus (REGIONS).

Ausgabe: src/main/java/.../datagen/MonsterGearShapes.java (die Teile als Zahlenreihen; MonsterGearModels legt sie wie ein
Schwert schräg). Mit --preview DATEI zeichnet das Skript die Modelle (schräg wie in der Hand und von der Seite), ohne Spiel.
"""
from __future__ import annotations

import math
import sys
from dataclasses import dataclass, field
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/io/github/sirschrolli/kingdomsatwar/datagen/MonsterGearShapes.java"

UV = tuple[float, float, float, float]

# Texturbereiche je Waffe (u0, v0, u1, v1) in UV-Einheiten (je zwei Pixel). Stränge: v0 = Spitze, v1 = Fuß; quer läuft u
# von der linken (-x) zur rechten (+x) Kante des Strangs.
REGIONS: dict[str, dict[str, UV]] = {
    "claw_blade": {
        "wrap": (0, 0, 2, 11),          # Lederwicklung des Griffs
        "pommel": (2, 0, 4, 2),         # eiserner Knauf
        "iron": (2, 2, 4, 4),           # Eisen (Deckel, Kanten)
        "fur": (4, 0, 7, 4),            # Pelzkragen (Seiten)
        "fur_top": (4, 4, 7, 7),        # Pelz von oben
        "claw": (7, 0, 9, 14),          # Klauen: Spitze oben (hell), Wurzel unten (dunkel)
        "claw_edge": (9, 0, 10, 14),    # die schmalen Seiten der Klauen
        "plate": (0, 12, 7, 14),        # Faustplatte vorn/hinten (mit Nieten)
        "plate_side": (7, 14, 9, 16),   # Faustplatte an den Seiten
    },
    "ember_fang_blade": {
        "wrap": (0, 0, 2, 12),          # Griff: Schuppenleder mit glutroter Kordel
        "basalt": (2, 0, 6, 3),         # Basaltschuppen (Knauf, Parierstück)
        "wing": (2, 3, 4, 9),           # Flügel der Parierstange
        "wing_edge": (4, 3, 5, 9),
        "root": (0, 12, 6, 14),         # Zahnwurzel
        "fang": (6, 0, 10, 16),         # die Fangklinge: Spitze oben
        "fang_edge": (10, 0, 11, 16),
        "vein": (11, 0, 13, 16),        # die glühende Ader (leuchtet)
        "vein_edge": (13, 0, 14, 16),
        "gem": (14, 0, 16, 2),          # Glutstein im Knauf (leuchtet)
        "hot": (14, 2, 16, 5),          # glühende Spitzen (leuchtet)
    },
    "bog_club": {
        "root": (0, 0, 2, 16),          # Wurzelholz (Griff, Schaft)
        "vine": (2, 0, 4, 5),           # Rankenwicklung
        "burl": (2, 5, 4, 11),          # Knorren
        "band": (2, 11, 4, 16),         # Wurzelbänder um den Kopf
        "bark": (4, 0, 9, 7),           # Rinde des Kopfes
        "bark_top": (4, 7, 9, 12),      # der Kopf von oben/unten
        "moss": (4, 12, 9, 14),         # Moospolster
        "moss_top": (4, 14, 9, 16),
        "bone": (9, 0, 10, 4),          # Knochendornen
        "bone_edge": (10, 0, 11, 4),
        "strand": (9, 4, 10, 12),       # hängende Moosfasern
        "strand_edge": (10, 4, 11, 12),
        "heart": (11, 0, 14, 3),        # das Moorherz (leuchtet)
        "heart_side": (14, 0, 15, 3),
        "cap": (11, 4, 13, 6),          # Pilzhüte (leuchten)
        "stem": (13, 4, 14, 6),
    },
    "soul_scythe": {
        "shaft": (0, 0, 2, 16),         # Aschenholz
        "wrap": (2, 0, 4, 4),           # Wicklungen aus Aschenschleier
        "ribbon": (2, 4, 4, 10),        # ein Fetzen Schleier
        "ribbon_edge": (4, 8, 5, 14),
        "bone": (4, 0, 6, 4),           # Knochen (Kappe, Fassung)
        "spike": (4, 4, 6, 8),          # Knochendornen
        "spike_edge": (5, 8, 6, 14),
        "blade": (6, 0, 10, 16),        # Klinge aus Seelenstahl: links die Schneide, rechts der Rücken
        "blade_edge": (10, 0, 11, 16),
        "edge": (11, 0, 12, 16),        # die glühende Schneide (leuchtet)
        "edge_side": (12, 0, 13, 6),
        "skull": (12, 6, 15, 9),        # der Schädel vorn/hinten
        "skull_side": (12, 9, 15, 12),
        "eyes": (13, 0, 14, 1),         # glühende Augen (leuchten)
        "flame": (14, 0, 16, 4),        # Seelenflamme (leuchtet)
    },
}


# =============================================================================
# Teile und Stränge
# =============================================================================

@dataclass
class Part:
    lo: tuple[float, float, float]
    hi: tuple[float, float, float]
    uv: UV
    side: UV
    cap: UV
    glow: bool = False
    rot: tuple[float, float, float] = (0.0, 0.0, 0.0)          # Grad um X, Y, Z (Z·Y·X, wie "rotation" im Modell)
    pivot: tuple[float, float, float] = (8.0, 8.0, 8.0)


def _cap_of(uv: UV) -> UV:
    u0, v0, u1, v1 = uv
    return (u0, v0, min(u1, u0 + 1.5), min(v1, v0 + 1.5))


def _sub(uv: UV, fu0: float, fv0: float, fu1: float, fv1: float) -> UV:
    """Teilbereich (Anteile 0..1) eines Bereichs."""
    u0, v0, u1, v1 = uv
    return (u0 + (u1 - u0) * fu0, v0 + (v1 - v0) * fv0, u0 + (u1 - u0) * fu1, v0 + (v1 - v0) * fv1)


@dataclass
class Shape:
    regions: dict[str, UV]
    parts: list[Part] = field(default_factory=list)

    def r(self, key: str) -> UV:
        return self.regions[key]

    def box(self, x0: float, y0: float, z0: float, x1: float, y1: float, z1: float, uv: str | UV, side: str | UV | None = None,
            cap: str | UV | None = None, glow: bool = False, rot: tuple[float, float, float] = (0.0, 0.0, 0.0),
            pivot: tuple[float, float, float] | None = None) -> "Shape":
        main = self.r(uv) if isinstance(uv, str) else uv
        s = main if side is None else (self.r(side) if isinstance(side, str) else side)
        c = _cap_of(s if side is not None else main) if cap is None else (self.r(cap) if isinstance(cap, str) else cap)
        if pivot is None:
            pivot = ((x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2)
        self.parts.append(Part((x0, y0, z0), (x1, y1, z1), main, s, c, glow, rot, pivot))
        return self

    def chain(self, x: float, y: float, a0: float, bend: float | list[float], lengths: list[float],
              lo: tuple[float, float], hi: tuple[float, float], depth: tuple[float, float], region: str,
              edge: str | None = None, glow: bool = False, z: float = 8.0) -> tuple[float, float, float]:
        """
        Ein gebogener, sich verjüngender Strang: Fuß bei (x, y) in der Ebene z; das erste Glied steht unter dem Winkel a0
        (Grad; 0 = nach oben, positiv = nach links), jedes weitere ist um {@code bend} weiter gedreht (oder je Glied aus
        einer Liste). Quer zur Achse reicht ein Glied von lo bis hi (vom Fuß lo[0]/hi[0] zur Spitze lo[1]/hi[1]), in der
        Tiefe von depth[0] zu depth[1]. Gibt das Ende (x, y, Winkel) zurück.
        """
        n = len(lengths)
        bends = bend if isinstance(bend, list) else [bend] * n
        total = sum(lengths)
        u0, v0, u1, v1 = self.r(region)
        eu0, _, eu1, _ = self.r(edge) if edge else (u0, v0, u0 + 1.0, v1)
        def v_at(c: float) -> float:
            return v1 + (v0 - v1) * min(1.0, c / total)

        angle = a0
        c = 0.0
        for i, length in enumerate(lengths):
            if i > 0:
                angle += bends[i]
            f = i / (n - 1) if n > 1 else 0.0
            a = lo[0] + (lo[1] - lo[0]) * f
            b = hi[0] + (hi[1] - hi[0]) * f
            d = depth[0] + (depth[1] - depth[0]) * f
            last = i == n - 1
            nxt = bends[i + 1] if not last else 0.0
            overlap = 0.0 if last else 0.12 + (b - a) * abs(math.sin(math.radians(nxt))) * 0.6
            vt, vb = v_at(c + length + overlap), v_at(c)
            uv = (u0, vt, u1, vb)                                       # quer immer der ganze Bereich (Schneide bis zur Spitze)
            side = (eu0, vt, eu1, vb)
            cap = (eu0, vt, eu1, min(vb, vt + 0.6))
            self.parts.append(Part((x + a, y, z - d / 2), (x + b, y + length + overlap, z + d / 2), uv, side, cap, glow,
                                   (0.0, 0.0, angle), (x, y, z)))
            x -= length * math.sin(math.radians(angle))
            y += length * math.cos(math.radians(angle))
            c += length
        return x, y, angle


# =============================================================================
# Die Waffen
# =============================================================================

def claw_blade() -> Shape:
    """Krallenklinge: Lederwicklung mit eisernem Knauf (daran eine kleine Hakenkralle), ein Pelzkragen, die eiserne Faustplatte
    und darüber drei Werwolfsklauen – zur Seite gefächert und alle in dieselbe Richtung gekrümmt, die mittlere die längste."""
    s = Shape(REGIONS["claw_blade"])
    s.box(7.05, -3.6, 7.05, 8.95, -2.35, 8.95, "pommel", cap="iron")
    s.chain(8.0, -3.4, 196, -18, [0.9, 0.75], (-0.42, -0.14), (0.42, 0.14), (0.8, 0.35), "claw", "claw_edge")
    s.box(7.35, -2.4, 7.35, 8.65, 2.5, 8.65, "wrap", cap="iron")
    s.box(6.75, 2.2, 6.75, 9.25, 2.9, 9.25, "fur", cap="fur_top")
    s.box(6.35, 2.75, 6.35, 9.65, 4.35, 9.65, "fur", cap="fur_top")
    s.box(4.85, 4.2, 6.9, 11.15, 5.7, 9.1, _sub(s.r("plate"), 0, 0, 6.3 / 7, 1), side="plate_side", cap="plate_side")
    # Pelzbüschel zwischen den Klauen
    s.box(6.55, 5.6, 7.3, 7.35, 6.3, 8.7, "fur", cap="fur_top", rot=(0, 0, 10))
    s.box(8.65, 5.6, 7.3, 9.45, 6.3, 8.7, "fur", cap="fur_top", rot=(0, 0, -10))
    # die drei Klauen
    s.chain(8.0, 5.3, 0, 7, [3.4, 3.2, 3.0, 2.6], (-0.85, -0.18), (0.85, 0.18), (1.1, 0.4), "claw", "claw_edge")
    s.chain(5.75, 5.2, 15, 8, [3.0, 2.8, 2.6, 2.2], (-0.75, -0.16), (0.75, 0.16), (1.0, 0.38), "claw", "claw_edge")
    s.chain(10.25, 5.2, -13, 8, [3.0, 2.8, 2.6, 2.2], (-0.75, -0.16), (0.75, 0.16), (1.0, 0.38), "claw", "claw_edge")
    return s


def ember_fang_blade() -> Shape:
    """Glutzahn-Klinge: Basaltknauf mit Glutstein, ein Griff aus Schuppenleder mit glutroter Kordel, eine Parierstange aus
    Basaltschuppen mit zwei geschwungenen, an den Spitzen glühenden Flügeln, die dunkle Zahnwurzel und darüber der große,
    gekrümmte Fangzahn – elfenbeinhell, mit einer glühenden Ader."""
    s = Shape(REGIONS["ember_fang_blade"])
    basalt = s.r("basalt")
    s.box(7.1, -3.8, 7.1, 8.9, -2.4, 8.9, _sub(basalt, 0, 0, 0.45, 1))
    s.box(7.45, -3.45, 6.75, 8.55, -2.75, 9.25, "gem", glow=True)
    s.box(7.4, -2.4, 7.4, 8.6, 3.4, 8.6, "wrap", cap=_sub(basalt, 0, 0, 0.3, 0.4))
    s.box(6.2, 3.3, 6.9, 9.8, 4.75, 9.1, basalt)
    for sign in (1, -1):
        x, y, a = s.chain(8.0 - sign * 1.5, 3.95, sign * 80, -sign * 26, [1.5, 1.3, 1.1], (-0.62, -0.3), (0.62, 0.3), (1.6, 0.9),
                          "wing", "wing_edge")
        s.chain(x, y, a - sign * 20, 0, [0.85], (-0.26, -0.26), (0.26, 0.26), (0.75, 0.75), "hot", "hot", glow=True)
    s.box(6.65, 4.6, 7.15, 9.35, 5.6, 8.85, "root")
    bend = 6.5
    s.chain(8.0, 4.8, 0, bend, [3.4, 3.3, 3.1, 2.9, 2.6], (-1.35, -0.2), (1.35, 0.2), (1.15, 0.45), "fang", "fang_edge")
    s.chain(8.0, 5.7, 0, bend, [2.5, 3.3, 3.1, 2.2], (-0.27, -0.12), (0.27, 0.12), (1.3, 0.76), "vein", "vein_edge", glow=True)
    return s


def bog_club() -> Shape:
    """Moorkeule: ein Wurzelknoten als Knauf, ein dünner, mit einer Ranke umwickelter Wurzelgriff, der Schaft wird knorrig
    dick; der Kopf aus Moorholz mit Knorren und Moospolster, von Wurzeln umschlungen, in ihm das grün glühende Moorherz;
    Knochendornen ragen heraus, Moosfasern hängen herab, leuchtende Pilze wachsen darauf."""
    s = Shape(REGIONS["bog_club"])
    root = s.r("root")
    s.box(7.2, -3.4, 7.2, 8.8, -2.0, 8.8, _sub(root, 0, 0.85, 0.8, 1.0))
    s.chain(7.6, -3.25, 150, 28, [0.9, 0.8], (-0.24, -0.14), (0.24, 0.14), (0.5, 0.3), "strand", "strand_edge")
    s.box(7.45, -2.0, 7.45, 8.55, 4.3, 8.55, _sub(root, 0, 0, 0.55, 0.45))
    s.box(7.3, 0.2, 7.3, 8.7, 2.5, 8.7, "vine")
    s.chain(8.0, 4.0, 2.5, -5, [2.3, 2.2], (-0.68, -0.95), (0.68, 0.95), (1.36, 1.9), "root")
    # der Kopf: unten schmaler, oben ein Moospolster, das über den Rand hängt
    s.box(6.35, 8.0, 6.35, 9.65, 10.2, 9.65, _sub(s.r("bark"), 0, 0.7, 0.66, 1.0), cap="bark_top")
    s.box(5.95, 10.0, 5.95, 10.05, 14.5, 10.05, _sub(s.r("bark"), 0, 0, 0.82, 0.64), cap="bark_top")
    s.box(6.4, 14.4, 6.4, 9.6, 15.4, 9.6, _sub(s.r("bark"), 0.1, 0.1, 0.74, 0.25), cap="moss_top", rot=(0, 14, 0))
    s.box(5.7, 14.15, 5.7, 10.3, 14.75, 10.3, "moss", cap="moss_top")
    s.box(6.6, 15.3, 6.6, 9.4, 15.85, 9.4, "moss", cap="moss_top", rot=(0, 14, 0))
    # Knorren
    s.box(5.15, 9.1, 6.8, 6.05, 11.7, 9.0, "burl")
    s.box(9.95, 11.0, 6.7, 10.85, 13.4, 9.1, "burl")
    s.box(6.9, 12.0, 9.95, 9.1, 13.9, 10.7, "burl")
    s.box(7.1, 8.6, 5.55, 8.9, 9.9, 6.45, "burl")
    # das Moorherz, vorn und hinten im Holz, darüber die Wurzeln, die es halten
    s.box(6.95, 10.15, 5.55, 9.05, 12.45, 6.15, "heart", side="heart_side", glow=True)
    s.box(6.95, 10.15, 9.85, 9.05, 12.45, 10.45, "heart", side="heart_side", glow=True)
    s.box(5.6, 11.05, 5.4, 10.4, 11.6, 10.6, "band", rot=(0, 0, 17))
    s.box(5.65, 13.15, 5.6, 10.35, 13.65, 10.4, "band", rot=(0, 0, -12))
    s.box(5.7, 9.3, 5.65, 10.3, 9.75, 10.35, "band", rot=(0, 0, 6))
    # Knochendornen
    for x, y, a in ((6.0, 13.5, 58), (6.0, 9.6, 102), (10.0, 10.3, -78), (10.0, 13.9, -42), (8.7, 15.6, -14)):
        s.chain(x, y, a, -8 if a > 0 else 8, [1.3, 1.1], (-0.44, -0.14), (0.44, 0.14), (0.9, 0.34), "bone", "bone_edge")
    # hängende Moosfasern
    for x, z, a, lengths in ((5.85, 7.1, 182, [1.0, 0.9, 0.7]), (10.15, 8.7, 176, [1.1, 0.8]), (7.4, 5.75, 180, [0.9, 0.8, 0.5]),
                             (8.9, 10.25, 178, [1.0, 0.7])):
        s.chain(x, 14.3, a, 9 if a < 180 else -9, lengths, (-0.24, -0.12), (0.24, 0.12), (0.3, 0.2), "strand", "strand_edge", z=z)
    # leuchtende Pilze
    for x, y, z, w in ((10.4, 13.4, 7.9, 1.0), (7.15, 15.8, 7.1, 1.1), (5.35, 11.7, 8.2, 0.8)):
        s.box(x - 0.15, y, z - 0.15, x + 0.15, y + 0.5, z + 0.15, "stem", glow=True)
        s.box(x - w / 2, y + 0.45, z - w / 2, x + w / 2, y + 0.85, z + w / 2, "cap", glow=True)
    return s


def soul_scythe() -> Shape:
    """Seelensense: ein langer Schaft aus Aschenholz mit Knochenkappe und Dorn, Wicklungen aus Aschenschleier (ein Fetzen weht
    davon), oben eine Knochenfassung mit einem kleinen Schädel, dessen Augen und Flamme blau glühen; aus ihm wächst die weit
    geschwungene Klinge aus Seelenstahl mit glühender Schneide, auf der anderen Seite ein Gegendorn."""
    s = Shape(REGIONS["soul_scythe"])
    s.box(7.15, -4.0, 7.15, 8.85, -2.9, 8.85, "bone")
    s.chain(8.0, -3.9, 180, 0, [0.9, 0.7], (-0.42, -0.14), (0.42, 0.14), (0.85, 0.3), "spike", "spike_edge")
    s.box(7.5, -2.9, 7.5, 8.5, 17.2, 8.5, "shaft", cap="bone")
    s.box(7.3, 0.2, 7.3, 8.7, 2.8, 8.7, "wrap")
    s.box(7.3, 9.6, 7.3, 8.7, 11.0, 8.7, _sub(s.r("wrap"), 0, 0, 1, 0.55))
    s.chain(8.55, 10.6, 196, -12, [1.5, 1.3, 1.1], (-0.5, -0.36), (0.5, 0.36), (0.16, 0.12), "ribbon", "ribbon_edge")
    s.box(7.05, 17.0, 7.05, 8.95, 18.05, 8.95, "bone")
    skull = s.r("skull")
    s.box(6.8, 18.0, 6.8, 9.2, 20.4, 9.2, _sub(skull, 0.1, 0.1, 0.9, 0.9), side=_sub(s.r("skull_side"), 0.1, 0.1, 0.9, 0.9))
    for z0, z1 in ((6.68, 6.82), (9.18, 9.32)):
        for x in (7.05, 8.55):
            s.box(x, 18.98, z0, x + 0.4, 19.42, z1, "eyes", glow=True)
    s.box(7.55, 20.35, 7.55, 8.45, 21.15, 8.45, _sub(s.r("flame"), 0, 0.45, 1, 1), glow=True)
    s.box(7.75, 21.1, 7.75, 8.25, 21.85, 8.25, _sub(s.r("flame"), 0.25, 0, 0.75, 0.45), glow=True)
    lengths = [2.6, 2.6, 2.5, 2.3, 2.0, 1.6]
    s.chain(7.1, 20.0, 84, 15, lengths, (-2.2, -0.35), (0.0, 0.0), (0.7, 0.34), "blade", "blade_edge")
    s.chain(7.1, 20.0, 84, 15, lengths, (-2.62, -0.6), (-2.2, -0.35), (0.42, 0.2), "edge", "edge_side", glow=True)
    s.chain(9.1, 19.6, -80, -14, [1.1, 0.9], (-0.44, -0.14), (0.44, 0.14), (0.8, 0.3), "spike", "spike_edge")
    return s


WEAPONS = {
    "CLAW_BLADE": ("claw_blade", claw_blade),
    "EMBER_FANG": ("ember_fang_blade", ember_fang_blade),
    "BOG_CLUB": ("bog_club", bog_club),
    "SOUL_SCYTHE": ("soul_scythe", soul_scythe),
}


# =============================================================================
# Java
# =============================================================================

def _f(v: float) -> str:
    s = f"{v:.4f}".rstrip("0").rstrip(".")
    if s in ("-0", ""):
        s = "0"
    return s + "f"


def java() -> str:
    out = [
        "package io.github.sirschrolli.kingdomsatwar.datagen;",
        "",
        "/**",
        " * Die Teile der Waffenmodelle aus Monsterteilen (0.70), senkrecht um x = z = 8 gebaut. Je Teil: von x, y, z, bis x, y, z;",
        " * UV vorn/hinten (u0, v0, u1, v1), UV der Seiten, UV oben/unten; Leuchten (0/1); eigene Drehung (Grad um X, Y, Z) und ihr",
        " * Drehpunkt (x, y, z).",
        " *",
        " * <p>ERZEUGT von tools/monster_gear_models.py – nicht von Hand ändern.",
        " */",
        "final class MonsterGearShapes {",
        "    private MonsterGearShapes() {}",
    ]
    for const, (name, fn) in WEAPONS.items():
        out.append("")
        out.append(f"    /** {name} */")
        out.append(f"    static final float[][] {const} = {{")
        for p in fn().parts:
            vals = [*p.lo, *p.hi, *p.uv, *p.side, *p.cap, 1.0 if p.glow else 0.0, *p.rot, *p.pivot]
            out.append("            {" + ", ".join(_f(v) for v in vals) + "},")
        out.append("    };")
    out.append("}")
    return "\n".join(out) + "\n"


# =============================================================================
# Vorschau (ohne Spiel): Quader drehen, schräglegen, mit Textur und einfachem Licht zeichnen
# =============================================================================

def _rot_matrix(rx: float, ry: float, rz: float):
    import numpy as np
    ax, ay, az = (math.radians(a) for a in (rx, ry, rz))
    Rx = np.array([[1, 0, 0], [0, math.cos(ax), -math.sin(ax)], [0, math.sin(ax), math.cos(ax)]])
    Ry = np.array([[math.cos(ay), 0, math.sin(ay)], [0, 1, 0], [-math.sin(ay), 0, math.cos(ay)]])
    Rz = np.array([[math.cos(az), -math.sin(az), 0], [math.sin(az), math.cos(az), 0], [0, 0, 1]])
    return Rz @ Ry @ Rx


def _faces(p: Part):
    """Die sechs Flächen: (Ecken in Bildreihenfolge oben-links, oben-rechts, unten-rechts, unten-links), UV, Glühen."""
    (x0, y0, z0), (x1, y1, z1) = p.lo, p.hi
    u = p.uv
    sd = p.side
    cp = p.cap
    return [
        ([(x0, y1, z1), (x1, y1, z1), (x1, y0, z1), (x0, y0, z1)], u, (0, 0, 1)),            # Süden
        ([(x0, y1, z0), (x1, y1, z0), (x1, y0, z0), (x0, y0, z0)], u, (0, 0, -1)),           # Norden (gespiegelt wie im Spiel)
        ([(x1, y1, z1), (x1, y1, z0), (x1, y0, z0), (x1, y0, z1)], sd, (1, 0, 0)),           # Osten
        ([(x0, y1, z0), (x0, y1, z1), (x0, y0, z1), (x0, y0, z0)], sd, (-1, 0, 0)),          # Westen
        ([(x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)], cp, (0, 1, 0)),           # oben
        ([(x0, y0, z1), (x1, y0, z1), (x1, y0, z0), (x0, y0, z0)], cp, (0, -1, 0)),          # unten
    ]


def render(shape: Shape, atlas, glow_atlas, view: tuple[float, float], size: int = 420, scale: float = 13.0, tilt: bool = True):
    import numpy as np
    from PIL import Image
    tex = np.asarray(atlas.convert("RGBA"), dtype=np.float32)
    gtex = np.asarray(glow_atlas.convert("RGBA"), dtype=np.float32)
    img = np.zeros((size, size, 4), dtype=np.float32)
    zbuf = np.full((size, size), -1e9, dtype=np.float32)
    G = _rot_matrix(0, 0, -45 if tilt else 0)
    V = _rot_matrix(view[1], view[0], 0)
    C = np.array([8.0, 8.0, 8.0])
    light = np.array([-0.35, 0.75, 0.55])
    light /= np.linalg.norm(light)
    for p in shape.parts:
        L = _rot_matrix(*p.rot)
        P = np.array(p.pivot)
        src = gtex if p.glow else tex
        for corners, (u0, v0, u1, v1), nrm in _faces(p):
            pts = []
            for c in corners:
                q = P + L @ (np.array(c) - P)
                q = C + G @ (q - C)
                q = C + V @ (q - C)
                pts.append(q)
            a, b, _, d = pts
            normal = V @ (G @ (L @ np.array(nrm, dtype=float)))
            if normal[2] <= 1e-6:
                continue
            shade = 1.0 if p.glow else 0.5 + 0.5 * max(0.0, float(normal @ light))
            # Bildkoordinaten
            def scr(q):
                return np.array([size / 2 + (q[0] - 8) * scale, size * 0.55 - (q[1] - 8) * scale])
            A, B, D = scr(a), scr(b), scr(d)
            E1, E2 = B - A, D - A
            det = E1[0] * E2[1] - E1[1] * E2[0]
            if abs(det) < 1e-6:
                continue
            xs = [A[0], B[0], D[0], (B + E2)[0]]
            ys = [A[1], B[1], D[1], (B + E2)[1]]
            bx0, bx1 = max(0, int(min(xs))), min(size - 1, int(max(xs)) + 1)
            by0, by1 = max(0, int(min(ys))), min(size - 1, int(max(ys)) + 1)
            if bx1 < bx0 or by1 < by0:
                continue
            gx, gy = np.meshgrid(np.arange(bx0, bx1 + 1) + 0.5, np.arange(by0, by1 + 1) + 0.5)
            rx, ry = gx - A[0], gy - A[1]
            s = (rx * E2[1] - ry * E2[0]) / det
            t = (E1[0] * ry - E1[1] * rx) / det
            m = (s >= 0) & (s <= 1) & (t >= 0) & (t <= 1)
            if not m.any():
                continue
            depth = a[2] + s * (b[2] - a[2]) + t * (d[2] - a[2])
            uu = (u0 + s * (u1 - u0)) * 2
            vv = (v0 + t * (v1 - v0)) * 2
            ui = np.clip(np.floor(np.minimum(uu, np.maximum(u0, u1) * 2 - 1e-3)).astype(int), 0, src.shape[1] - 1)
            vi = np.clip(np.floor(np.minimum(vv, np.maximum(v0, v1) * 2 - 1e-3)).astype(int), 0, src.shape[0] - 1)
            col = src[vi, ui]
            sub = (slice(by0, by1 + 1), slice(bx0, bx1 + 1))
            ok = m & (col[..., 3] > 8) & (depth > zbuf[sub])
            zb = zbuf[sub]
            zb[ok] = depth[ok]
            im = img[sub]
            im[ok, :3] = col[ok, :3] * shade
            im[ok, 3] = 255
    out = Image.fromarray(np.clip(img, 0, 255).astype("uint8"), "RGBA")
    bg = Image.new("RGBA", out.size, (58, 62, 70, 255))
    bg.alpha_composite(out)
    return bg


def preview(path: str) -> None:
    from PIL import Image
    import monster_gear_textures as tex
    atlases = {"claw_blade": tex.claw_blade_atlas, "ember_fang_blade": tex.ember_fang_atlas, "bog_club": tex.bog_club_atlas,
               "soul_scythe": tex.soul_scythe_atlas}
    views = [((0, 0), True), ((-35, 18), True), ((90, 0), False), ((30, -20), False)]
    tiles = []
    for const, (name, fn) in WEAPONS.items():
        atlas, glow = atlases[name]()
        row = [render(fn(), atlas, glow, v, tilt=t) for v, t in views]
        tiles.append(row)
    w, h = tiles[0][0].size
    sheet = Image.new("RGBA", (w * len(views), h * len(tiles)), (40, 40, 40, 255))
    for j, row in enumerate(tiles):
        for i, t in enumerate(row):
            sheet.paste(t, (i * w, j * h))
    sheet.save(path)


def main(argv: list[str]) -> None:
    if "--preview" in argv:
        preview(argv[argv.index("--preview") + 1])
        return
    JAVA.write_text(java(), encoding="utf-8")
    print(f"geschrieben: {JAVA.relative_to(ROOT)}")


if __name__ == "__main__":
    main(sys.argv[1:])
