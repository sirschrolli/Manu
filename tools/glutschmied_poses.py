#!/usr/bin/env python3
"""
Haltungen und Abläufe von Malleus, dem Glutschmied – erzeugt GlutschmiedPoses.java.

Jede Haltung wird in Weltwinkeln beschrieben (Grad; 0 = senkrecht nach unten, 90 = waagerecht nach vorn, 180 = nach oben),
je Glied: Oberarm, Unterarm und Werkzeug (Hammerstiel bzw. Speer). Daraus entstehen die Zusatzwinkel je Knochen, die das
Modell im Spiel zu seiner Grundhaltung addiert. Die Abläufe (je Angriff) reihen Haltungen an Ticks auf; dazwischen wird
weich überblendet (beim Zuschlagen beschleunigend).

Aufruf (aus dem Projektordner):
  python3 tools/glutschmied_poses.py                 # schreibt die Java-Klasse
  python3 tools/glutschmied_poses.py --preview a.png  # zeigt alle Haltungen (mit Amboss als Maßstab)

Felder einer Haltung: lean (Rumpf nach vorn, zusätzlich zur Grundneigung von 6°), drop (Rumpf und Hüfte tiefer, Pixel),
twist (Rumpf-Drehung), head [x, y, z], jaw, l = [Oberarm, Unterarm, Hammerstiel], r = [Oberarm, Unterarm, Speer],
lz/rz (Arme zur Mitte hin, Grad), legs = [links, rechts] (Weltwinkel), legz (Spreizung), spear_y (Speer quer drehen).
"""
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TARGET = ROOT / "src/main/java/io/github/sirschrolli/kingdomsatwar/client/renderer/nether/GlutschmiedPoses.java"

BONES = ["body", "head", "jaw", "left_arm", "left_forearm", "hammer", "right_arm", "right_forearm", "spear", "left_leg", "right_leg"]

# Haltungen ---------------------------------------------------------------------------------------------------------
POSES: dict[str, dict] = {
    # kampfbereit: leicht geduckt, der Speer zeigt nach vorn, der Hammer hängt schwer in der Linken
    "ready": {"lean": 8, "drop": 1.5, "head": [-8, 0, 0], "l": [8, 40, 30], "lz": 4, "r": [14, 60, 100], "rz": 6,
              "legs": [14, -14], "legz": 5},
    # am Amboss: tief im Ausfallschritt, die Rechte hält das Werkstück, der Hammer schlägt
    "forge_impact": {"lean": 14, "drop": 5.5, "head": [16, 0, 0], "l": [-20, 62, 90], "lz": 15, "r": [25, 60, 0], "rz": 26,
                     "legs": [-55, 40]},
    "forge_lift": {"lean": 13, "drop": 5.5, "head": [14, 0, 0], "l": [-8, 80, 118], "lz": 14, "r": [25, 60, 0], "rz": 26,
                   "legs": [-55, 40]},
    "forge_raised": {"lean": 10, "drop": 5.5, "head": [8, 0, 0], "l": [174, 214, 254], "lz": 10, "r": [25, 60, 0], "rz": 26,
                     "legs": [-55, 40]},
    # Erwachen: aufblicken, aufrichten, den Speer vom Amboss nehmen, die Waffen recken, brüllen
    "awaken_look": {"lean": 14, "drop": 5.5, "head": [-22, 0, 0], "l": [-20, 62, 90], "lz": 15, "r": [25, 60, 0], "rz": 26,
                    "legs": [-55, 40]},
    "awaken_rise": {"lean": 4, "drop": 1, "head": [-10, 0, 0], "l": [4, 30, 16], "lz": 4, "r": [8, 24, 0], "rz": 6,
                    "legs": [12, -12], "legz": 4},
    "awaken_reach": {"lean": 30, "drop": 3, "head": [10, 0, 0], "l": [10, 40, 30], "lz": 4, "r": [52, 74, 90], "rz": 24,
                     "spear_y": 70, "legs": [24, -26], "legz": 4},
    "awaken_raise": {"lean": -8, "head": [-18, 0, 0], "l": [150, 172, 205], "lz": 2, "r": [150, 118, 180], "rz": -8,
                     "legs": [12, -12], "legz": 6},
    "awaken_roar": {"lean": -14, "head": [-32, 0, 0], "jaw": 36, "l": [130, 150, 190], "lz": -26, "r": [130, 100, 172], "rz": -26,
                    "legs": [14, -14], "legz": 8},
    # Speerstoß: ausholen (Speer an der Schulter) und zustoßen
    "thrust_draw": {"lean": 2, "drop": 1.5, "twist": -18, "head": [-6, 14, 0], "l": [10, 40, 30], "lz": 8, "r": [-40, 40, 92],
                    "rz": 12, "legs": [18, -18], "legz": 5},
    "thrust": {"lean": 18, "drop": 2.5, "twist": 22, "head": [-14, -18, 0], "l": [-30, 10, 10], "lz": 14, "r": [62, 84, 88],
               "legs": [30, -24], "legz": 5},
    # Hammerschlag: hoch über den Kopf, dann auf das Dach
    "slam_raise": {"lean": -12, "head": [-16, 0, 0], "l": [196, 230, 270], "lz": 6, "r": [20, 50, 110], "rz": 24,
                   "legs": [10, -10], "legz": 5},
    "slam_hit": {"lean": 34, "drop": 4, "head": [8, 0, 0], "jaw": 14, "l": [24, 44, 62], "lz": 10, "r": [-24, -12, 110], "rz": 40,
                 "legs": [36, -40], "legz": 6},
    # Funkenflug: Hammer auf Klinge
    "sparks_up": {"lean": 6, "drop": 1.5, "head": [6, 0, 0], "l": [120, 165, 205], "lz": 18, "r": [40, 78, 96], "rz": 14,
                  "spear_y": -28, "legs": [14, -14], "legz": 5},
    "sparks_hit": {"lean": 14, "drop": 2, "head": [12, 0, 0], "l": [58, 76, 96], "lz": 26, "r": [40, 78, 96], "rz": 14,
                   "spear_y": -28, "legs": [14, -14], "legz": 5},
    # Glutwurf: der Hammer schwingt von hinten unten nach vorn oben
    "globs_back": {"lean": 4, "drop": 2, "twist": 14, "head": [-6, -8, 0], "l": [-45, -30, -20], "lz": 2, "r": [10, 40, 120],
                   "rz": 8, "legs": [20, -16], "legz": 5},
    "globs_fling": {"lean": 10, "drop": 1, "twist": -16, "head": [-10, 10, 0], "l": [120, 140, 165], "lz": 10, "r": [-10, 20, 120],
                    "rz": 8, "legs": [-10, 18], "legz": 5},
    # Ruf der Gesellen: den Speer emporrecken, dann auf das Dach stoßen
    "call_raise": {"lean": -10, "head": [-24, 0, 0], "jaw": 30, "l": [30, 60, 40], "lz": -20, "r": [120, 100, 180], "rz": -10,
                   "legs": [10, -10], "legz": 6},
    "call_high": {"lean": -12, "head": [-28, 0, 0], "jaw": 34, "l": [30, 60, 40], "lz": -22, "r": [140, 120, 180], "rz": -10,
                  "legs": [10, -10], "legz": 6},
    "call_stamp": {"lean": 10, "drop": 2.5, "head": [2, 0, 0], "jaw": 10, "l": [20, 50, 40], "lz": -10, "r": [60, 40, 180], "rz": 4,
                   "legs": [16, -16], "legz": 7},
    # Esse anfachen: den Speer hoch erheben und ins Dach stoßen
    "stoke_raise": {"lean": -8, "head": [-18, 0, 0], "l": [40, 70, 60], "lz": -8, "r": [160, 175, 8], "rz": 6,
                    "legs": [10, -10], "legz": 6},
    "stoke_plant": {"lean": 30, "drop": 4.5, "head": [-26, 0, 0], "jaw": 32, "l": [150, 170, 200], "lz": -20, "r": [70, 40, 22],
                    "rz": 4, "legs": [30, -34], "legz": 7},
    # Tod: er sinkt auf die Knie, die Waffen gleiten ihm aus den Händen
    "death": {"lean": 40, "drop": 6.5, "head": [30, 0, 8], "jaw": 20, "l": [-10, 10, 0], "lz": 4, "r": [-6, 8, 40], "rz": 4,
              "legs": [-60, 45], "legz": 4},
}

# Abläufe: (Tick, Haltung, Übergang dorthin: 0 weich, 1 beschleunigend, 2 abbremsend) --------------------------------
TIMELINES: dict[str, list[tuple[float, str, int]]] = {
    "FORGE": [(0, "forge_impact", 0), (4, "forge_lift", 2), (12, "forge_raised", 0), (15, "forge_impact", 1), (26, "forge_impact", 0)],
    "AWAKEN": [(0, "forge_impact", 0), (8, "awaken_look", 0), (22, "awaken_rise", 0), (30, "awaken_reach", 0),
               (34, "awaken_reach", 0), (42, "awaken_raise", 2), (48, "awaken_roar", 0), (56, "awaken_roar", 0), (60, "ready", 0)],
    "THRUST": [(0, "ready", 0), (6, "thrust_draw", 0), (9, "thrust", 1), (13, "thrust", 0), (16, "thrust_draw", 0), (19, "thrust", 1),
               (23, "thrust", 0), (26, "thrust_draw", 0), (29, "thrust", 1), (34, "thrust", 0), (40, "ready", 0)],
    "SLAM": [(0, "ready", 0), (13, "slam_raise", 2), (17, "slam_hit", 1), (25, "slam_hit", 0), (36, "ready", 0)],
    "SPARKS": [(0, "ready", 0), (6, "sparks_up", 0), (10, "sparks_hit", 1), (16, "sparks_up", 2), (22, "sparks_hit", 1),
               (28, "sparks_up", 2), (34, "sparks_hit", 1), (40, "sparks_hit", 0), (50, "ready", 0)],
    "GLOBS": [(0, "ready", 0), (8, "globs_back", 0), (12, "globs_fling", 1), (16, "globs_back", 0), (20, "globs_fling", 1),
              (24, "globs_back", 0), (28, "globs_fling", 1), (32, "globs_back", 0), (36, "globs_fling", 1), (42, "ready", 0)],
    "CALL": [(0, "ready", 0), (8, "call_raise", 0), (12, "call_high", 0), (15, "call_stamp", 1), (22, "call_stamp", 0), (30, "ready", 0)],
    "STOKE": [(0, "ready", 0), (12, "stoke_raise", 2), (18, "stoke_plant", 1), (40, "stoke_plant", 0), (50, "ready", 0)],
}


def bones(p: dict) -> dict[str, list[float]]:
    """Zusatzwinkel (Grad) und Verschiebungen (Pixel) je Knochen: [rx, ry, rz, dx, dy, dz]."""
    lean = p.get("lean", 0.0)
    tilt = 6.0 + lean
    drop = p.get("drop", 0.0)
    l1, l2, l3 = p.get("l", [0, 12, 12])
    r1, r2, r3 = p.get("r", [0, 12, 102])
    lz = p.get("lz", 0.0)
    rz = p.get("rz", 0.0)
    legs = p.get("legs", [0, 0])
    legz = p.get("legz", 0.0)
    head = p.get("head", [0, 0, 0])
    return {
        "body": [lean, p.get("twist", 0.0), 0, 0, drop, 0],
        "head": [head[0], head[1], head[2], 0, 0, 0],
        "jaw": [p.get("jaw", 0.0), 0, 0, 0, 0, 0],
        # Grundhaltung der Arme: Oberarm 0°, Unterarm −12°, Hammer 0°, Speer −90° (zeigt nach vorn)
        "left_arm": [-(l1 + tilt), 0, lz, 0, 0, 0],
        "left_forearm": [(l1 - l2) + 12, 0, 0, 0, 0, 0],
        "hammer": [l2 - l3, 0, 0, 0, 0, 0],
        "right_arm": [-(r1 + tilt), 0, -rz, 0, 0, 0],
        "right_forearm": [(r1 - r2) + 12, 0, 0, 0, 0, 0],
        "spear": [r2 - r3 + 90, p.get("spear_y", 0.0), 0, 0, 0, 0],
        "left_leg": [-legs[0], 0, -legz, 0, drop, 0],
        "right_leg": [-legs[1], 0, legz, 0, drop, 0],
    }


def fmt(v: float) -> str:
    s = f"{v:.2f}".rstrip("0").rstrip(".")
    return (s if s not in ("-0", "") else "0") + "f"


def java() -> str:
    names = list(POSES)
    lines = [
        "package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;",
        "",
        "/**",
        " * Haltungen und Abläufe von Malleus, dem Glutschmied: je Haltung Zusatzwinkel (Grad) und Verschiebungen (Pixel) der",
        " * Knochen, je Angriff eine Folge von Haltungen an Ticks (dazwischen wird überblendet).",
        " *",
        " * <p>ERZEUGT von tools/glutschmied_poses.py – nicht von Hand ändern.",
        " */",
        "final class GlutschmiedPoses {",
    ]
    for i, b in enumerate(BONES):
        lines.append(f"    static final int {b.upper()} = {i};")
    lines.append(f"    static final int BONES = {len(BONES)};")
    lines.append("")
    for i, n in enumerate(names):
        lines.append(f"    static final int P_{n.upper()} = {i};")
    lines.append("")
    lines.append("    /** Je Haltung und Knochen: rx, ry, rz (Grad), dx, dy, dz (Pixel). */")
    lines.append("    static final float[][] POSES = {")
    for n in names:
        b = bones(POSES[n])
        vals = ", ".join(", ".join(fmt(v) for v in b[bone]) for bone in BONES)
        lines.append(f"            // {n}")
        lines.append(f"            {{{vals}}},")
    lines.append("    };")
    lines.append("")
    lines.append("    /** Abläufe: je Schlüssel {Tick, Haltung, Übergang (0 weich, 1 beschleunigend, 2 abbremsend)}. */")
    for t, keys in TIMELINES.items():
        body = ", ".join(f"{{{int(k[0])}, P_{k[1].upper()}, {k[2]}}}" for k in keys)
        lines.append(f"    static final int[][] {t} = {{{body}}};")
    lines.append("")
    lines.append("    private GlutschmiedPoses() {}")
    lines.append("}")
    return "\n".join(lines) + "\n"


def preview(target: str) -> None:
    sys.path.insert(0, str(ROOT / "tools"))
    from entitygen.models import glutschmied as mod
    from entitygen.paint import paint_model
    from entitygen.preview import render
    from PIL import Image, ImageDraw

    m = mod.build()
    m.pack()
    img, glow = paint_model(m, mod.materials("glutschmied"), mod.PALETTES["glutschmied"], seed=100)
    size = 300
    names = list(POSES)
    cols = 4
    rows = (len(names) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * size, rows * (size + 20)), (30, 32, 36, 255))
    d = ImageDraw.Draw(sheet)
    for i, n in enumerate(names):
        b = bones(POSES[n])
        pose = {k: (v[0], v[1], v[2], 1, 1, 1, v[3], v[4], v[5]) for k, v in b.items()}
        im = render(m, img, glow, 60, 10, size, pose)
        x, y = (i % cols) * size, (i // cols) * (size + 20)
        sheet.paste(im, (x, y + 20))
        d.text((x + 6, y + 4), n, fill=(240, 220, 160, 255))
    sheet.save(target)


if __name__ == "__main__":
    if len(sys.argv) > 2 and sys.argv[1] == "--preview":
        preview(sys.argv[2])
    else:
        TARGET.write_text(java(), encoding="utf-8")
        print(f"{TARGET.name}: {len(POSES)} Haltungen, {len(TIMELINES)} Abläufe")
