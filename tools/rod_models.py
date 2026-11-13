#!/usr/bin/env python3
"""
Die Angeln in 3D (0.90): die Angel von Minecraft und die sechs Angeln der Mod (Eisen, Gold, Drache, Schatten, Glut, Sterne),
je ruhend (mit Schnur, Schwimmer oder Köder und Haken) und ausgeworfen (ohne Schnur – die zeichnet das Spiel bis zum
Schwimmer –, die Spitze stärker gebogen).

Gebaut wie die Waffen (tools/monster_gear_models.py, dessen Bausteine hier verwendet werden): senkrecht um x = z = 8, der Griff
unten, die Spitze oben; RodModels legt das Modell schräg (-45° um Z) wie das Bild der Angel von Minecraft und hält es wie
diese. Die Rute ist ein Strang aus vier sich verjüngenden Gliedern, an den Gelenken Ringe mit Wicklung; unten am Rollenhalter
hängt die Rolle (eine Scheibe aus zwei verdrehten Quadern) mit Kurbel. Was an der Schnur hängt (Schnur, Schwimmer, Köder,
Haken), hängt senkrecht nach unten: Diese Teile werden in einem „hängenden“ Rahmen unter der Spitze gebaut und um +45°
gedreht, damit sie nach dem Schräglegen lotrecht hängen.

Alle Angeln teilen dieselben Texturbereiche; tools/rod_textures.py malt sie je Angel aus.

Ausgabe: src/main/java/.../datagen/RodShapes.java. Mit --preview DATEI zeichnet das Skript die Modelle (schräg wie in der Hand
und von der Seite), ohne Spiel.
"""
from __future__ import annotations

import math
import sys
from dataclasses import dataclass, field
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import monster_gear_models as mg  # noqa: E402
from monster_gear_models import Shape  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/io/github/sirschrolli/kingdomsatwar/datagen/RodShapes.java"

UV = tuple[float, float, float, float]

# Texturbereiche (u0, v0, u1, v1) in UV-Einheiten (je zwei Pixel). Stränge: v0 = Spitze (oben), v1 = Fuß.
REGIONS: dict[str, UV] = {
    "butt": (0, 0, 2, 2),           # Kappe am Griffende (vorn)
    "butt_top": (2, 0, 4, 2),       # Kappe von unten / Deckel
    "grip": (0, 2, 2, 10),          # der Griff (Kork, Leder, Wicklung): oben im Bild = oben am Griff
    "grip_side": (2, 2, 3, 10),
    "seat": (3, 2, 5, 4),           # der Rollenhalter (Metall)
    "seat_top": (3, 4, 5, 5),
    "fore": (3, 5, 5, 8),           # der Vordergriff
    "blank": (5, 0, 6, 16),         # die Rute: Spitze oben
    "blank_side": (6, 0, 7, 16),
    "wrap": (7, 0, 8, 4),           # Wicklungen an den Ringen
    "guide": (7, 4, 8, 6),          # die Ringe
    "spool": (8, 0, 11, 3),         # die Rolle von vorn (Scheibe mit aufgewickelter Schnur)
    "spool_side": (8, 3, 11, 4),
    "reel": (11, 0, 13, 2),         # Rollenfuß
    "reel_side": (11, 2, 13, 3),
    "crank": (13, 0, 14, 3),        # Kurbel
    "knob": (14, 0, 16, 2),         # Kurbelknauf
    "knob_top": (14, 2, 16, 3),
    "line": (8, 4, 9, 16),          # die Schnur
    "hook": (9, 4, 10, 8),          # der Haken
    "lure": (10, 4, 13, 7),         # Schwimmer oder Köder von vorn
    "lure_top": (10, 7, 13, 8),
    "gem": (13, 3, 15, 5),          # Stein (am Griffende)
    "accent": (13, 5, 16, 8),       # Zierde (Schuppen, Platten)
    "glow": (9, 8, 10, 16),         # leuchtende Ader (Strang)
    "glow_edge": (10, 8, 11, 16),
    "spike": (11, 8, 13, 16),       # Dornen, Hörner, Flossen: Spitze oben
    "spike_edge": (13, 8, 14, 16),
    "tip": (14, 8, 16, 11),         # Zier an der Spitze (Stern)
    "tip_side": (14, 11, 16, 12),
}


@dataclass
class Spec:
    """Wie eine Angel gebaut ist."""
    name: str                       # Name der Texturen und Modelle
    lure: str = "bobber"            # bobber, spoon, crystal, ember, star, scale
    grip_half: float = 0.72         # halbe Breite des Griffs
    blank_base: float = 0.42        # halbe Breite der Rute am Fuß
    blank_tip: float = 0.16         # … und an der Spitze
    seat: bool = True               # Rollenhalter aus Metall (die Angel von Minecraft hat keinen)
    gem: bool = False               # Stein im Griffende
    horns: bool = False             # zwei Hörner am Griffende (Drache)
    fins: bool = False              # Flossenplatten am Vordergriff (Drache, Glut)
    star_tip: bool = False          # ein Stern an der Spitze
    glow_blank: bool = False        # die Rute leuchtet (Lohenstab der Glutangel)
    glow_wraps: bool = False        # die Wicklungen leuchten (Schatten)
    glow_line: bool = False         # die Schnur leuchtet (Sterne, Glut)
    glow_lure: bool = False         # der Köder leuchtet
    glow_gem: bool = True
    suckers: bool = False           # Saugnäpfe an der Unterseite der Rute (Kraken)
    bend_rest: list[float] = field(default_factory=lambda: [0.0, -1.5, -2.5, -4.0])
    bend_cast: list[float] = field(default_factory=lambda: [-1.0, -6.0, -9.0, -12.0])


SPECS: dict[str, Spec] = {
    "FISHING_ROD": Spec("fishing_rod", lure="bobber", grip_half=0.66, blank_base=0.4, blank_tip=0.2, seat=False),
    "IRON_FISHING_ROD": Spec("iron_fishing_rod", lure="bobber"),
    "GOLD_FISHING_ROD": Spec("gold_fishing_rod", lure="spoon", gem=True, glow_gem=False),
    "DRAGON_FISHING_ROD": Spec("dragon_fishing_rod", lure="scale", grip_half=0.78, blank_base=0.46, gem=True, horns=True, fins=True,
                               glow_lure=True),
    "SHADOW_FISHING_ROD": Spec("shadow_fishing_rod", lure="crystal", gem=True, glow_wraps=True, glow_lure=True),
    "GLUT_FISHING_ROD": Spec("glut_fishing_rod", lure="ember", blank_base=0.44, fins=True, glow_blank=True, glow_line=True, glow_lure=True),
    "STAR_FISHING_ROD": Spec("star_fishing_rod", lure="star", gem=True, star_tip=True, glow_line=True, glow_lure=True),
    "KRAKEN_FISHING_ROD": Spec("kraken_fishing_rod", lure="eye", grip_half=0.76, blank_base=0.46, blank_tip=0.18, gem=True, suckers=True,
                               glow_line=True, glow_lure=True),
}

X, Z = 8.0, 8.0
BUTT = -1.6
GRIP_TOP = 4.2
SEAT_TOP = 5.8
FORE_TOP = 6.9
BLANK = [3.0, 2.8, 2.6, 2.3]
LINE = 8.6


def _rot(x: float, y: float, a: float) -> tuple[float, float]:
    r = math.radians(a)
    return x * math.cos(r) - y * math.sin(r), x * math.sin(r) + y * math.cos(r)


def joints(x: float, y: float, a0: float, bends: list[float], lengths: list[float]) -> list[tuple[float, float, float]]:
    """Die Gelenke eines Strangs (wie Shape.chain): Anfang, Ende jedes Glieds, je mit dem Winkel des Glieds davor."""
    out = [(x, y, a0)]
    angle = a0
    for i, length in enumerate(lengths):
        if i > 0:
            angle += bends[i]
        x -= length * math.sin(math.radians(angle))
        y += length * math.cos(math.radians(angle))
        out.append((x, y, angle))
    return out


def hang(s: Shape, tip: tuple[float, float], cx: float, cy: float, w: float, h: float, d: float, region: str,
         side: str | None = None, cap: str | None = None, glow: bool = False, turn: float = 0.0) -> None:
    """
    Ein Teil, das an der Schnur hängt: (cx, cy) ist seine Mitte im hängenden Rahmen – dort hängt die Schnur von der Spitze
    lotrecht nach unten. Nach dem Schräglegen (-45°) hängt es wirklich lotrecht; {@code turn} dreht es zusätzlich.
    """
    tx, ty = tip
    dx, dy = _rot(cx - tx, cy - ty, 45.0)
    px, py = tx + dx, ty + dy
    s.box(px - w / 2, py - h / 2, Z - d / 2, px + w / 2, py + h / 2, Z + d / 2, region, side=side, cap=cap, glow=glow,
          rot=(0.0, 0.0, 45.0 + turn), pivot=(px, py, Z))


def rod(spec: Spec, cast: bool) -> Shape:
    s = Shape(REGIONS)
    g = spec.grip_half
    # --- Griffende und Griff ---
    s.box(X - g - 0.08, BUTT, Z - g - 0.08, X + g + 0.08, BUTT + 0.8, Z + g + 0.08, "butt", cap="butt_top")
    if spec.gem:
        s.box(X - 0.42, BUTT - 0.3, Z - 0.42, X + 0.42, BUTT + 0.15, Z + 0.42, "gem", cap="gem", glow=spec.glow_gem)
    if spec.horns:
        # zwei kleine Hörner, nach unten auseinander
        s.chain(X - g + 0.1, BUTT + 0.5, 150, 14, [0.9, 0.8], (-0.24, -0.08), (0.24, 0.08), (0.45, 0.2), "spike", "spike_edge")
        s.chain(X + g - 0.1, BUTT + 0.5, -150, -14, [0.9, 0.8], (-0.24, -0.08), (0.24, 0.08), (0.45, 0.2), "spike", "spike_edge")
    s.box(X - g, BUTT + 0.8, Z - g, X + g, GRIP_TOP, Z + g, "grip", side="grip_side", cap="butt_top")
    # --- Rollenhalter und Vordergriff ---
    if spec.seat:
        s.box(X - 0.56, GRIP_TOP, Z - 0.56, X + 0.56, SEAT_TOP, Z + 0.56, "seat", cap="seat_top")
        s.box(X - 0.6, GRIP_TOP - 0.05, Z - 0.6, X + 0.6, GRIP_TOP + 0.35, Z + 0.6, "seat_top", cap="seat_top")
        s.box(X - 0.6, SEAT_TOP - 0.35, Z - 0.6, X + 0.6, SEAT_TOP + 0.05, Z + 0.6, "seat_top", cap="seat_top")
        s.box(X - 0.52, SEAT_TOP, Z - 0.52, X + 0.52, FORE_TOP, Z + 0.52, "fore", cap="seat_top")
    else:
        # die Angel von Minecraft: ein Stock, an dem eine Spule festgebunden ist
        s.box(X - 0.5, GRIP_TOP, Z - 0.5, X + 0.5, FORE_TOP, Z + 0.5, "fore", cap="seat_top")
    if spec.fins:
        # Flossenplatten am Vordergriff, nach unten gerichtet
        s.chain(X - 0.5, SEAT_TOP + 0.9, 168, 10, [1.1, 0.8], (-0.32, -0.1), (0.32, 0.1), (0.3, 0.14), "spike", "spike_edge")
        s.chain(X + 0.5, SEAT_TOP + 0.9, -168, -10, [1.1, 0.8], (-0.32, -0.1), (0.32, 0.1), (0.3, 0.14), "spike", "spike_edge")
    # --- die Rolle: hängt an der Unterseite (+x; nach dem Schräglegen unten) ---
    cy = (GRIP_TOP + SEAT_TOP) / 2
    s.box(X + 0.5, cy - 0.32, Z - 0.26, X + 1.25, cy + 0.32, Z + 0.26, "reel", side="reel_side", cap="reel_side")
    rx = X + 2.15
    s.box(rx - 0.98, cy - 0.98, Z - 0.6, rx + 0.98, cy + 0.98, Z + 0.6, "spool", side="spool_side", cap="spool_side")
    s.box(rx - 0.98, cy - 0.98, Z - 0.55, rx + 0.98, cy + 0.98, Z + 0.55, "spool", side="spool_side", cap="spool_side",
          rot=(0.0, 0.0, 45.0), pivot=(rx, cy, Z))
    # die Kurbel (vorn) mit ihrem Knauf
    s.box(rx - 0.12, cy - 0.12, Z + 0.6, rx + 1.25, cy + 0.12, Z + 0.82, "crank", cap="crank")
    s.box(rx + 0.95, cy - 0.28, Z + 0.82, rx + 1.45, cy + 0.28, Z + 1.42, "knob", cap="knob_top")
    # --- die Rute: vier sich verjüngende Glieder ---
    bends = spec.bend_cast if cast else spec.bend_rest
    b0, b1 = spec.blank_base, spec.blank_tip
    s.chain(X, FORE_TOP - 0.05, 0.0, bends, BLANK, (-b0, -b1), (b0, b1), (2 * b0, 2 * b1), "blank", "blank_side",
            glow=spec.glow_blank)
    pts = joints(X, FORE_TOP - 0.05, 0.0, bends, BLANK)
    total = sum(BLANK)
    # Ringe mit Wicklung an den Gelenken und an der Spitze
    for i, (jx, jy, a) in enumerate(pts[1:], start=1):
        f = sum(BLANK[:i]) / total
        w = b0 + (b1 - b0) * f
        tip = i == len(pts) - 1
        if not tip:
            s.box(jx - w - 0.09, jy - 0.24, Z - w - 0.09, jx + w + 0.09, jy + 0.24, Z + w + 0.09, "wrap", cap="wrap",
                  glow=spec.glow_wraps, rot=(0.0, 0.0, a), pivot=(jx, jy, Z))
            ring = 0.62 - 0.12 * f
            s.box(jx + w, jy - 0.24, Z - 0.26, jx + w + ring, jy + 0.24, Z + 0.26, "guide", cap="guide",
                  rot=(0.0, 0.0, a), pivot=(jx, jy, Z))
        else:
            s.box(jx - 0.24, jy - 0.3, Z - 0.24, jx + 0.24, jy + 0.22, Z + 0.24, "guide", cap="guide", rot=(0.0, 0.0, a), pivot=(jx, jy, Z))
    if spec.suckers:
        # Saugnäpfe: kleine helle Polster an der Unterseite, zwischen den Ringen, zur Spitze hin kleiner
        for i in range(len(pts) - 1):
            x0, y0, a0 = pts[i]
            x1, y1, a1 = pts[i + 1]
            for f in (0.3, 0.62):
                jx, jy = x0 + (x1 - x0) * f, y0 + (y1 - y0) * f
                k = (sum(BLANK[:i]) + BLANK[i] * f) / total
                w = b0 + (b1 - b0) * k
                size = 0.42 - 0.18 * k
                s.box(jx + w - 0.08, jy - size / 2, Z - size / 2, jx + w + size * 0.55, jy + size / 2, Z + size / 2, "accent", cap="accent",
                      rot=(0.0, 0.0, a1), pivot=(jx, jy, Z))
    # der erste Ring gleich über dem Vordergriff
    jx, jy, a = pts[0]
    s.box(jx - b0 - 0.09, jy + 0.6, Z - b0 - 0.09, jx + b0 + 0.09, jy + 1.0, Z + b0 + 0.09, "wrap", cap="wrap", glow=spec.glow_wraps)
    s.box(jx + b0, jy + 0.6, Z - 0.3, jx + b0 + 0.75, jy + 1.0, Z + 0.3, "guide", cap="guide")
    tx, ty, ta = pts[-1]
    if spec.star_tip:
        # ein kleiner Stern über der Spitze (zwei gekreuzte Quader)
        sx, sy = tx - 0.45 * math.sin(math.radians(ta)), ty + 0.45 * math.cos(math.radians(ta))
        s.box(sx - 0.42, sy - 0.42, Z - 0.16, sx + 0.42, sy + 0.42, Z + 0.16, "tip", side="tip_side", cap="tip_side", glow=True)
        s.box(sx - 0.42, sy - 0.42, Z - 0.12, sx + 0.42, sy + 0.42, Z + 0.12, "tip", side="tip_side", cap="tip_side", glow=True,
              rot=(0.0, 0.0, 45.0), pivot=(sx, sy, Z))
    if cast:
        return s
    # --- ruhend: die Schnur hängt von der Spitze, daran Schwimmer oder Köder und der Haken ---
    tip = (tx, ty)
    hang(s, tip, tx, ty - LINE / 2 + 0.1, 0.26, LINE + 0.2, 0.26, "line", cap="line", glow=spec.glow_line)
    bottom = ty - LINE
    if spec.lure == "bobber":
        # ein Schwimmer auf der Schnur, oben rot, unten weiß
        hang(s, tip, tx, ty - LINE * 0.48, 1.0, 1.4, 1.0, "lure", cap="lure_top")
        hang(s, tip, tx, ty - LINE * 0.48 + 0.95, 0.3, 0.5, 0.3, "lure_top", cap="lure_top")
    elif spec.lure == "spoon":
        # ein goldener Löffelköder über dem Haken
        hang(s, tip, tx, bottom + 0.75, 0.9, 1.5, 0.3, "lure", side="lure_top", cap="lure_top", glow=spec.glow_lure)
    elif spec.lure == "scale":
        # eine Drachenschuppe als Köder, leicht schräg
        hang(s, tip, tx, bottom + 0.8, 1.0, 1.4, 0.32, "lure", side="lure_top", cap="lure_top", glow=spec.glow_lure, turn=12.0)
    elif spec.lure == "crystal":
        # ein Schattenkristall (auf der Spitze stehend)
        hang(s, tip, tx, bottom + 0.9, 0.85, 0.85, 0.5, "lure", side="lure_top", cap="lure_top", glow=spec.glow_lure, turn=45.0)
    elif spec.lure == "ember":
        # eine glühende Perle mit einer kleinen Flamme darüber
        hang(s, tip, tx, bottom + 0.7, 0.85, 0.85, 0.85, "lure", side="lure_top", cap="lure_top", glow=spec.glow_lure)
        hang(s, tip, tx, bottom + 1.35, 0.42, 0.5, 0.42, "lure_top", cap="lure_top", glow=spec.glow_lure)
    elif spec.lure == "eye":
        # ein leuchtendes Krakenauge mit schmaler Pupille, darüber ein Wirbel aus Bronze
        hang(s, tip, tx, bottom + 0.8, 0.95, 0.95, 0.95, "lure", side="lure_top", cap="lure_top", glow=spec.glow_lure)
        hang(s, tip, tx, bottom + 1.45, 0.36, 0.36, 0.36, "guide", cap="guide")
    elif spec.lure == "star":
        hang(s, tip, tx, bottom + 0.85, 0.95, 0.95, 0.3, "lure", side="lure_top", cap="lure_top", glow=spec.glow_lure)
        hang(s, tip, tx, bottom + 0.85, 0.95, 0.95, 0.26, "lure", side="lure_top", cap="lure_top", glow=spec.glow_lure, turn=45.0)
    # der Haken: Schenkel, Bogen, Spitze mit Widerhaken
    hang(s, tip, tx, bottom - 0.45, 0.22, 1.1, 0.22, "hook", cap="hook")
    hang(s, tip, tx + 0.3, bottom - 1.0, 0.82, 0.22, 0.22, "hook", cap="hook")
    hang(s, tip, tx + 0.6, bottom - 0.68, 0.22, 0.6, 0.22, "hook", cap="hook")
    hang(s, tip, tx + 0.48, bottom - 0.42, 0.18, 0.22, 0.2, "hook", cap="hook")
    return s


def shapes() -> dict[str, Shape]:
    out = {}
    for const, spec in SPECS.items():
        out[const] = rod(spec, False)
        out[const + "_CAST"] = rod(spec, True)
    return out


# =============================================================================
# Java und Vorschau
# =============================================================================

def java() -> str:
    out = [
        "package io.github.sirschrolli.kingdomsatwar.datagen;",
        "",
        "/**",
        " * Die Teile der Angeln in 3D (0.90), senkrecht um x = z = 8 gebaut, je ruhend und ausgeworfen ({@code _CAST}). Je Teil:",
        " * von x, y, z, bis x, y, z; UV vorn/hinten (u0, v0, u1, v1), UV der Seiten, UV oben/unten; Leuchten (0/1); eigene Drehung",
        " * (Grad um X, Y, Z) und ihr Drehpunkt (x, y, z).",
        " *",
        " * <p>Jede Form liegt in einer eigenen kleinen Klasse – zusammen wären die Tabellen zu groß für eine (64 KB je Methode).",
        " *",
        " * <p>ERZEUGT von tools/rod_models.py – nicht von Hand ändern.",
        " */",
        "final class RodShapes {",
        "    private RodShapes() {}",
    ]
    all_shapes = shapes()
    out.append("")
    for const in all_shapes:
        holder = "".join(w.capitalize() for w in const.lower().split("_"))
        out.append(f"    static final float[][] {const} = {holder}.PARTS;")
    for const, shape in all_shapes.items():
        holder = "".join(w.capitalize() for w in const.lower().split("_"))
        out.append("")
        out.append(f"    private static final class {holder} {{")
        out.append(f"        static final float[][] PARTS = {{")
        for p in shape.parts:
            vals = [*p.lo, *p.hi, *p.uv, *p.side, *p.cap, 1.0 if p.glow else 0.0, *p.rot, *p.pivot]
            out.append("                {" + ", ".join(mg._f(v) for v in vals) + "},")
        out.append("        };")
        out.append("    }")
    out.append("}")
    return "\n".join(out) + "\n"


def debug_atlas():
    from PIL import Image, ImageDraw
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    glow = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d, gd = ImageDraw.Draw(img), ImageDraw.Draw(glow)
    palette = [(200, 60, 60), (60, 160, 60), (60, 90, 200), (210, 170, 50), (160, 70, 190), (60, 180, 180), (230, 120, 40),
               (150, 150, 150), (240, 240, 240), (100, 60, 30), (230, 90, 160), (120, 200, 90), (40, 40, 60), (250, 220, 150)]
    for i, (key, (u0, v0, u1, v1)) in enumerate(REGIONS.items()):
        col = palette[i % len(palette)]
        box = (round(u0 * 2), round(v0 * 2), round(u1 * 2) - 1, round(v1 * 2) - 1)
        d.rectangle(box, fill=col + (255,))
        gd.rectangle(box, fill=col + (255,))
    return img, glow


def preview(path: str, debug: bool) -> None:
    from PIL import Image
    if not debug:
        import rod_textures as tex
    views = [((0, 0), True), ((-35, 18), True), ((60, 10), True), ((90, 0), False)]
    tiles = []
    for const, spec in SPECS.items():
        atlas, glow = debug_atlas() if debug else tex.atlas(spec.name)
        for cast in (False, True):
            shape = rod(spec, cast)
            tiles.append([mg.render(shape, atlas, glow, v, size=300, scale=12.0, tilt=t) for v, t in views])
    w, h = tiles[0][0].size
    cols = len(views) * 2
    rows = (len(tiles) + 1) // 2
    sheet = Image.new("RGBA", (w * cols, h * rows), (40, 40, 40, 255))
    for j, row in enumerate(tiles):
        for i, t in enumerate(row):
            sheet.paste(t, ((j % 2) * len(views) * w + i * w, (j // 2) * h))
    sheet.save(path)
    print("Vorschau:", path)


def main(argv: list[str]) -> None:
    if "--preview" in argv:
        preview(argv[argv.index("--preview") + 1], "--debug" in argv)
        return
    JAVA.write_text(java(), encoding="utf-8")
    print(f"geschrieben: {JAVA.relative_to(ROOT)}")


if __name__ == "__main__":
    main(sys.argv[1:])
