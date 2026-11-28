#!/usr/bin/env python3
"""
3D-Modelle der Werkzeuge und Waffen, die bis 0.87 flach waren (0.88): die Sätze aus Stahl, Nachtstahl und Abyssit
(Schwert, Axt, Spitzhacke, Schaufel, Hacke), Draconium-Schwert und -Axt, die Mondsilberklinge, die Trollkeule, der
Kommandostab und der Vermessungsstab.

Gebaut wie die Waffenkammer (tools/armory_models.py, mit deren Bausteinen): senkrecht um x = z = 8, der Griff unten, die
Spitze oben; in der Hand legt MonsterGearModels das Modell schräg wie ein Schwert. Statt jedes Stück einzeln zu formen,
gibt es hier Vorlagen – Schwert, Axt, Spitzhacke, Schaufel, Hacke – mit Maßen je Werkstoff, und alle Stücke einer Vorlage
teilen dieselben Texturbereiche (tools/toolset_textures.py malt sie je Werkstoff aus).

Ausgabe: src/main/java/.../datagen/ToolsetShapes.java. Mit --preview DATEI zeichnet das Skript die Modelle (schräg wie in
der Hand und von der Seite), ohne Spiel.
"""
from __future__ import annotations

import sys
from dataclasses import dataclass
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import monster_gear_models as mg  # noqa: E402
from monster_gear_models import Shape, _sub  # noqa: E402
from armory_models import plate  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/io/github/sirschrolli/kingdomsatwar/datagen/ToolsetShapes.java"

UV = tuple[float, float, float, float]

# Texturbereiche (u0, v0, u1, v1) in UV-Einheiten (je zwei Pixel); oben im Bild ist oben am Gegenstand, Stränge: v0 = Spitze.
SWORD: dict[str, UV] = {
    "tip": (0, 0, 4, 2),            # die Spitze der Klinge von vorn
    "blade": (0, 2, 4, 16),         # die Klinge von vorn (beide Schneiden hell)
    "blade_edge": (4, 0, 5, 16),    # die schmalen Seiten der Klinge
    "fuller": (5, 0, 6, 16),        # die Hohlkehle (bei manchen leuchtend)
    "guard": (6, 0, 14, 2),         # die Parierstange von vorn
    "guard_side": (6, 2, 14, 3),
    "quillon": (6, 3, 8, 7),        # ihre Enden (Stränge)
    "quillon_edge": (8, 3, 9, 7),
    "grip": (9, 3, 11, 10),         # der umwickelte Griff
    "metal": (11, 3, 13, 5),        # Knauf und Zwinge
    "metal_top": (13, 3, 15, 5),
    "gem": (11, 5, 13, 7),          # Stein im Knauf (leuchtet)
    "glow": (13, 5, 14, 16),        # leuchtende Ader (Stränge)
    "glow_edge": (14, 5, 15, 16),
}

TOOL: dict[str, UV] = {
    "handle": (0, 0, 1, 16),        # der Stiel von vorn
    "handle_side": (1, 0, 2, 16),
    "wrap": (2, 0, 4, 5),           # Wicklung am Griff
    "metal": (2, 5, 4, 7),          # Zwingen, Kappen
    "metal_top": (2, 7, 4, 9),
    "head": (4, 0, 12, 7),          # Blatt von vorn (Axt, Schaufel, Hacke): oben im Bild = oben am Werkzeug
    "head_side": (4, 7, 12, 8),
    "edge": (12, 0, 13, 8),         # die helle Schneide
    "edge_side": (13, 0, 14, 8),
    "pick": (4, 8, 7, 16),          # die Arme der Spitzhacke: Spitze oben
    "pick_edge": (7, 8, 8, 16),
    "glow": (8, 8, 9, 16),          # leuchtende Ader (Stränge)
    "glow_edge": (9, 8, 10, 16),
    "socket": (10, 8, 14, 11),      # das Auge, das den Stiel hält
    "socket_top": (10, 11, 14, 13),
    "gem": (14, 0, 16, 2),          # Stein (leuchtet)
    "spike": (14, 2, 16, 8),        # Dornen, Nägel: Spitze oben
    "spike_edge": (14, 8, 15, 14),
}

# Kommandostab und Messlatte brauchen nur Stiel, Kappen und Stein – dafür einen hohen Bereich für ihre Bänder (der Bereich des
# Kopfs ist bei ihnen frei)
ROD: dict[str, UV] = dict(TOOL)
ROD["band"] = (4, 0, 6, 16)


@dataclass
class SwordSpec:
    blade_len: float = 14.5
    half: float = 1.25              # halbe Breite der Klinge
    depth: float = 0.6              # Dicke der Klinge
    guard: float = 3.6              # halbe Breite der Parierstange
    quillon: float = 30.0           # Winkel der Parierenden (0 = gerade zur Seite)
    grip: float = 4.8
    gem: bool = False
    glow_fuller: bool = False       # leuchtende Hohlkehle


def sword(spec: SwordSpec) -> Shape:
    """Ein Schwert: Knauf (vielleicht mit Stein), umwickelter Griff, Parierstange mit geschwungenen Enden, die gerade Klinge mit
    Hohlkehle, oben spitz zulaufend."""
    s = Shape(SWORD)
    g0 = -spec.grip / 2 - 0.2
    g1 = g0 + spec.grip
    s.box(7.05, g0 - 1.35, 7.05, 8.95, g0, 8.95, "metal", cap="metal_top")
    if spec.gem:
        s.box(7.45, g0 - 1.05, 6.85, 8.55, g0 - 0.3, 9.15, "gem", glow=True)
    s.box(7.4, g0, 7.4, 8.6, g1, 8.6, "grip", cap="metal_top")
    s.box(7.2, g1 - 0.15, 7.2, 8.8, g1 + 0.35, 8.8, "metal", cap="metal_top")
    # Parierstange und ihre Enden
    y0 = g1 + 0.35
    s.box(8 - spec.guard, y0, 7.3, 8 + spec.guard, y0 + 1.0, 8.7, "guard", side="guard_side", cap="guard_side")
    for sign in (1, -1):
        s.chain(8 - sign * (spec.guard - 0.15), y0 + 0.35, sign * (90 - spec.quillon), -sign * 18, [0.9, 0.8], (-0.42, -0.2),
                (0.42, 0.2), (1.2, 0.7), "quillon", "quillon_edge")
    # die Klinge: der gerade Teil und die Spitze
    b0 = y0 + 1.0
    b1 = b0 + spec.blade_len
    z0, z1 = 8 - spec.depth / 2, 8 + spec.depth / 2
    s.box(8 - spec.half, b0, z0, 8 + spec.half, b1, z1, "blade", side="blade_edge", cap="blade_edge")
    # die Spitze: ein um 45° gedrehtes Quadrat, dessen obere Ecke die Spitze bildet (die untere steckt in der Klinge)
    d = spec.half / 2 ** 0.5
    s.box(8 - d, b1 - d, z0 + 0.02, 8 + d, b1 + d, z1 - 0.02, "tip", side="blade_edge", cap="blade_edge", rot=(0, 0, 45), pivot=(8.0, b1, 8.0))
    # die Hohlkehle auf beiden Seiten
    fz0, fz1 = z0 - 0.06, z1 + 0.06
    s.box(7.78, b0 + 0.6, fz0, 8.22, b1 - 1.2, fz1, "glow" if spec.glow_fuller else "fuller", side="glow_edge" if spec.glow_fuller else "fuller",
          glow=spec.glow_fuller)
    return s


@dataclass
class ToolSpec:
    handle: float = 19.5            # Länge des Stiels (vom Fuß bei -3.4)
    glow: bool = False              # leuchtende Adern im Kopf
    gem: bool = False


def _handle(s: Shape, top: float) -> None:
    """Stiel mit Fußkappe und Wicklung (Fuß bei y = -3.4)."""
    s.box(7.32, -3.4, 7.32, 8.68, -2.6, 8.68, "metal", cap="metal_top")
    s.box(7.5, -2.6, 7.5, 8.5, top, 8.5, "handle", side="handle_side", cap="metal_top")
    s.box(7.36, -2.3, 7.36, 8.64, 2.1, 8.64, "wrap", cap="metal_top")


def axe(spec: ToolSpec) -> Shape:
    """Axt: Stiel mit Wicklung, oben das Auge; links das Blatt (zur Schneide hin breiter, halbmondförmig), rechts der Nacken."""
    s = Shape(TOOL)
    top = spec.handle - 3.4
    _handle(s, top)
    s.box(7.2, top - 4.0, 7.2, 8.8, top, 8.8, "socket", cap="socket_top")
    # das Blatt links, nach außen höher
    frame = (2.7, top - 6.2, 7.2, top + 0.8)
    for (x0, x1, y0, y1) in ((6.0, 7.2, top - 4.0, top - 0.2), (4.8, 6.0, top - 4.6, top + 0.2), (3.6, 4.8, top - 5.4, top + 0.5),
                             (3.1, 3.6, top - 5.9, top + 0.7)):
        plate(s, x0, y0, x1, y1, 7.72, 8.28, "head", frame, side="head_side", cap="head_side")
    plate(s, 2.6, top - 6.1, 3.1, top + 0.75, 7.8, 8.2, "edge", (2.6, top - 6.1, 3.1, top + 0.75), side="edge_side", cap="edge_side")
    if spec.glow:
        s.box(4.2, top - 4.6, 7.66, 4.6, top - 0.2, 8.34, "glow", side="glow_edge", glow=True)
    # der Nacken rechts
    s.box(8.8, top - 3.3, 7.45, 10.0, top - 0.6, 8.55, "socket", cap="socket_top")
    if spec.gem:
        s.box(7.55, top - 2.7, 7.1, 8.45, top - 1.8, 8.9, "gem", glow=True)
    return s


def pickaxe(spec: ToolSpec) -> Shape:
    """Spitzhacke: Stiel, oben das Auge und die beiden nach unten gebogenen, spitzen Arme."""
    s = Shape(TOOL)
    top = spec.handle - 3.4
    _handle(s, top)
    y = top - 1.2
    s.box(7.15, y - 1.2, 7.15, 8.85, y + 1.2, 8.85, "socket", cap="socket_top")
    for sign in (1, -1):
        s.chain(8.0, y - 0.05, sign * 90, sign * 13, [2.3, 2.1, 1.9, 1.5], (-0.95, -0.12), (0.95, 0.12), (1.3, 0.35), "pick", "pick_edge")
        if spec.glow:
            s.chain(8.0, y - 0.05, sign * 90, sign * 13, [2.3, 2.1, 1.7], (-0.2, -0.1), (0.2, 0.1), (1.38, 0.6), "glow", "glow_edge",
                    glow=True)
    if spec.gem:
        s.box(7.55, y - 0.45, 7.05, 8.45, y + 0.45, 8.95, "gem", glow=True)
    return s


def shovel(spec: ToolSpec) -> Shape:
    """Schaufel: Stiel, oben eine Tülle und das breite, oben gerundete Blatt."""
    s = Shape(TOOL)
    top = spec.handle - 3.4
    _handle(s, top)
    s.box(7.25, top - 1.6, 7.25, 8.75, top + 0.2, 8.75, "socket", cap="socket_top")
    frame = (5.2, top, 10.8, top + 6.6)
    for (x0, x1, y0, y1) in ((5.2, 10.8, top, top + 4.9), (5.6, 10.4, top + 4.9, top + 5.8), (6.4, 9.6, top + 5.8, top + 6.4)):
        plate(s, x0, y0, x1, y1, 7.72, 8.28, "head", frame, side="head_side", cap="head_side")
    # der Rücken des Blatts: etwas dicker in der Mitte
    s.box(7.4, top - 0.1, 7.55, 8.6, top + 3.8, 8.45, "socket", cap="socket_top")
    if spec.glow:
        for z0, z1 in ((7.64, 7.7), (8.3, 8.36)):
            s.box(7.8, top + 1.2, z0, 8.2, top + 5.6, z1, "glow", side="glow_edge", glow=True)
    return s


def hoe(spec: ToolSpec) -> Shape:
    """Hacke: Stiel, oben das Auge, ein kurzer Arm nach links und daran das nach unten zeigende Blatt."""
    s = Shape(TOOL)
    top = spec.handle - 3.4
    _handle(s, top)
    s.box(7.2, top - 2.2, 7.2, 8.8, top + 0.2, 8.8, "socket", cap="socket_top")
    s.box(4.4, top - 1.2, 7.55, 7.2, top + 0.05, 8.45, "socket", cap="socket_top")
    frame = (2.8, top - 5.6, 4.6, top + 0.4)
    plate(s, 2.8, top - 4.6, 4.6, top + 0.4, 7.7, 8.3, "head", frame, side="head_side", cap="head_side", rot=(0, 0, -8),
          pivot=(3.7, top, 8.0))
    plate(s, 2.9, top - 5.5, 4.5, top - 4.6, 7.78, 8.22, "edge", (2.9, top - 5.5, 4.5, top - 4.6), side="edge_side", cap="edge_side",
          rot=(0, 0, -8), pivot=(3.7, top, 8.0))
    if spec.glow:
        s.box(3.5, top - 4.0, 7.64, 3.9, top - 0.2, 8.36, "glow", side="glow_edge", glow=True, rot=(0, 0, -8), pivot=(3.7, top, 8.0))
    if spec.gem:
        s.box(7.55, top - 1.5, 7.1, 8.45, top - 0.6, 8.9, "gem", glow=True)
    return s


def troll_club() -> Shape:
    """Trollkeule: ein Griff mit Lederwicklung, dann ein knorriger, nach oben immer dickerer Holzprügel mit zwei Eisenbändern
    und eingeschlagenen Nägeln."""
    s = Shape(TOOL)
    s.box(7.3, -3.6, 7.3, 8.7, -2.6, 8.7, "metal", cap="metal_top")
    s.box(7.4, -2.6, 7.4, 8.6, 2.6, 8.6, "wrap", cap="metal_top")
    s.chain(8.0, 2.4, 0, [0, 3, 3, 2], [3.6, 3.6, 3.4, 3.0], (-0.75, -2.1), (0.75, 2.1), (1.5, 4.0), "head", "head_side")
    # Eisenbänder um den Prügel
    s.box(6.75, 6.6, 6.55, 9.25, 7.4, 9.45, "metal", cap="metal_top", rot=(0, 0, 2))
    s.box(5.9, 13.4, 5.95, 10.2, 14.3, 10.05, "metal", cap="metal_top", rot=(0, 0, 5))
    # Nägel: zur Seite und nach vorn/hinten
    for x, y, a in ((6.5, 10.2, 70), (9.6, 11.2, -72), (6.0, 14.6, 62), (10.0, 13.8, -66), (7.0, 15.4, 22), (8.9, 15.1, -26)):
        s.chain(x, y, a, 0, [1.3], (-0.22, -0.06), (0.22, 0.06), (0.44, 0.12), "spike", "spike_edge")
    for y, z0, z1 in ((10.8, 5.2, 6.0), (15.2, 10.4, 11.2), (12.8, 5.4, 6.2), (16.8, 9.9, 10.7)):
        s.box(7.8, y, z0, 8.2, y + 0.4, z1, "spike", side="spike_edge", cap="spike_edge")
    return s


def command_baton() -> Shape:
    """Kommandostab: ein blauer Stab mit goldenen Bändern, unten eine goldene Kappe, oben ein goldener Knauf mit Stein."""
    s = Shape(ROD)
    s.box(7.1, -3.4, 7.1, 8.9, -2.4, 8.9, "metal", cap="metal_top")
    s.box(7.35, -2.4, 7.35, 8.65, 12.6, 8.65, "band", side="band", cap="metal_top")
    for y in (1.6, 6.0, 10.4):
        s.box(7.25, y, 7.25, 8.75, y + 0.6, 8.75, "metal", cap="metal_top")
    s.box(7.1, 12.6, 7.1, 8.9, 13.6, 8.9, "metal", cap="metal_top")
    s.box(7.3, 13.6, 7.3, 8.7, 14.6, 8.7, "metal", cap="metal_top", rot=(0, 45, 0))
    s.box(7.5, 14.6, 7.5, 8.5, 15.6, 8.5, "gem", glow=True)
    s.box(7.7, 15.6, 7.7, 8.3, 16.3, 8.3, "metal", cap="metal_top")
    return s


def surveyors_rod() -> Shape:
    """Vermessungsstab: eine lange, rot-weiß gestreifte Latte mit Messingspitze unten und Messingkappe mit Ring oben."""
    s = Shape(ROD)
    s.chain(8.0, -3.0, 180, 0, [0.8, 0.7], (-0.42, -0.08), (0.42, 0.08), (0.84, 0.16), "spike", "spike_edge")
    s.box(7.4, -3.2, 7.4, 8.6, -2.2, 8.6, "metal", cap="metal_top")
    s.box(7.55, -2.2, 7.55, 8.45, 20.0, 8.45, "band", side="band", cap="metal_top")
    s.box(7.4, 20.0, 7.4, 8.6, 21.0, 8.6, "metal", cap="metal_top")
    s.box(7.85, 21.0, 7.25, 8.15, 22.4, 8.75, "metal", cap="metal_top")
    s.box(7.85, 21.0, 7.85, 8.15, 22.4, 8.15, "metal", cap="metal_top", rot=(0, 90, 0))
    return s


# Die Stücke: Name -> (Form, Vorlage der Texturbereiche)
ITEMS: dict[str, tuple] = {
    "STEEL_SWORD": ("steel_sword", lambda: sword(SwordSpec())),
    "STEEL_AXE": ("steel_axe", lambda: axe(ToolSpec())),
    "STEEL_PICKAXE": ("steel_pickaxe", lambda: pickaxe(ToolSpec())),
    "STEEL_SHOVEL": ("steel_shovel", lambda: shovel(ToolSpec(handle=18.5))),
    "STEEL_HOE": ("steel_hoe", lambda: hoe(ToolSpec())),
    "NIGHTSTEEL_SWORD": ("nightsteel_sword", lambda: sword(SwordSpec(blade_len=15.0, half=1.15, guard=3.9, quillon=40, gem=True))),
    "NIGHTSTEEL_AXE": ("nightsteel_axe", lambda: axe(ToolSpec(gem=True))),
    "NIGHTSTEEL_PICKAXE": ("nightsteel_pickaxe", lambda: pickaxe(ToolSpec(gem=True))),
    "NIGHTSTEEL_SHOVEL": ("nightsteel_shovel", lambda: shovel(ToolSpec(handle=18.5))),
    "NIGHTSTEEL_HOE": ("nightsteel_hoe", lambda: hoe(ToolSpec(gem=True))),
    "ABYSSITE_SWORD": ("abyssite_sword", lambda: sword(SwordSpec(blade_len=15.0, half=1.35, guard=4.0, quillon=-25, gem=True,
                                                                 glow_fuller=True))),
    "ABYSSITE_AXE": ("abyssite_axe", lambda: axe(ToolSpec(glow=True))),
    "ABYSSITE_PICKAXE": ("abyssite_pickaxe", lambda: pickaxe(ToolSpec(glow=True))),
    "ABYSSITE_SHOVEL": ("abyssite_shovel", lambda: shovel(ToolSpec(handle=18.5, glow=True))),
    "ABYSSITE_HOE": ("abyssite_hoe", lambda: hoe(ToolSpec(glow=True))),
    "DRACONIUM_SWORD": ("draconium_sword", lambda: sword(SwordSpec(blade_len=15.2, half=1.4, guard=4.3, quillon=50, gem=True,
                                                                   glow_fuller=True))),
    "DRACONIUM_AXE": ("draconium_axe", lambda: axe(ToolSpec(glow=True, gem=True))),
    "MOONSILVER_BLADE": ("moonsilver_blade", lambda: sword(SwordSpec(blade_len=16.4, half=0.95, depth=0.5, guard=2.6, quillon=10,
                                                                     grip=5.4, gem=True, glow_fuller=True))),
    "TROLL_CLUB": ("troll_club", troll_club),
    "COMMAND_BATON": ("command_baton", command_baton),
    "SURVEYORS_ROD": ("surveyors_rod", surveyors_rod),
}


def java() -> str:
    out = [
        "package io.github.sirschrolli.kingdomsatwar.datagen;",
        "",
        "/**",
        " * Die Teile der Werkzeuge und Waffen in 3D (0.88), senkrecht um x = z = 8 gebaut – Aufbau wie {@link MonsterGearShapes}.",
        " *",
        " * <p>ERZEUGT von tools/toolset_models.py – nicht von Hand ändern.",
        " */",
        "final class ToolsetShapes {",
        "    private ToolsetShapes() {}",
    ]
    for const, (name, fn) in ITEMS.items():
        out.append("")
        out.append(f"    /** {name} */")
        out.append(f"    static final float[][] {const} = {{")
        for p in fn().parts:
            vals = [*p.lo, *p.hi, *p.uv, *p.side, *p.cap, 1.0 if p.glow else 0.0, *p.rot, *p.pivot]
            out.append("            {" + ", ".join(mg._f(v) for v in vals) + "},")
        out.append("    };")
    out.append("}")
    return "\n".join(out) + "\n"


def preview(path: str) -> None:
    from PIL import Image
    import toolset_textures as tex
    views = [((0, 0), True), ((-35, 18), True), ((90, 0), False)]
    tiles = []
    for const, (name, fn) in ITEMS.items():
        atlas, glow = tex.atlas(name)
        tiles.append([mg.render(fn(), atlas, glow, v, size=300, scale=9.5, tilt=t) for v, t in views])
    w, h = tiles[0][0].size
    cols = 2
    per = len(views)
    rows = (len(tiles) + cols - 1) // cols
    sheet = Image.new("RGBA", (w * per * cols, h * rows), (40, 40, 40, 255))
    for j, row in enumerate(tiles):
        for i, t in enumerate(row):
            sheet.paste(t, (((j % cols) * per + i) * w, (j // cols) * h))
    sheet.save(path)


def main(argv: list[str]) -> None:
    if "--preview" in argv:
        preview(argv[argv.index("--preview") + 1])
        return
    JAVA.write_text(java(), encoding="utf-8")
    print(f"geschrieben: {JAVA.relative_to(ROOT)}")


if __name__ == "__main__":
    main(sys.argv[1:])
