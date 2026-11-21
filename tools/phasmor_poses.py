#!/usr/bin/env python3
"""
Haltungen und Abläufe von Phasmor, dem Leerwandlerkönig (1.5) – erzeugt PhasmorPoses.java.

Jede Haltung gibt je Knochen Zusatzwinkel (Grad) zur Grundhaltung des Modells an (Arme hängen, die Klinge zeigt nach vorn),
dazu (optional) Verschiebungen in Pixeln; negatives rx hebt einen Arm nach vorn, ein negatives rx am Unterarm beugt den
Ellbogen. Die Werte stammen aus einer kleinen Rechnung (die Fäuste dorthin, wo sie hingehören: beim Wachen beide Hände auf
dem Knauf, die Klinge senkrecht im Boden; beim Hieb die Klinge über der Schulter …), die Ellbogen beugen sich nur nach vorn.
Die Abläufe (je Angriff) reihen Haltungen an Ticks auf; dazwischen wird weich überblendet (beim Zuschlagen beschleunigend).

Aufruf (aus dem Projektordner):
  python3 tools/phasmor_poses.py                 # schreibt die Java-Klasse
  python3 tools/phasmor_poses.py --preview a.png  # zeigt alle Haltungen
"""
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TARGET = ROOT / "src/main/java/io/github/sirschrolli/kingdomsatwar/client/renderer/end/PhasmorPoses.java"

BONES = ["body", "head", "left_arm", "left_forearm", "right_arm", "right_forearm", "blade", "left_leg", "left_shin", "right_leg",
         "right_shin"]

# Haltungen: je Knochen [rx, ry, rz, dx, dy, dz] (fehlende = 0) ---------------------------------------------------------
# kneel: die Wache – auf ein Knie gestützt, beide Hände auf dem Knauf, die Klinge senkrecht im Boden, das Haupt gesenkt
# ready: kampfbereit – die Klinge schräg vor sich, die Spitze nach vorn oben, die Linke mit gespreizten Fingern
# rise/raise: das Erwachen – er steht auf und zieht die Klinge aus dem Boden, dann reckt er sie empor
# slash_wind/slash_strike: der Klingenhieb – über die rechte Schulter ausholen, schräg hinab nach links
# thrust: der Phasenstoß – ein weiter Ausfallschritt, die Klinge nach vorn
# cast: die Trugbilder – die Linke erhoben, die Klinge gesenkt
# lift/throw: der Blockwurf – beide Arme empor (die Brocken schweben), dann wirft die Linke
# plunge: die Leerenrisse – er stößt die Klinge in den Boden
# storm: der Phasensturm – geduckt, die Klinge hinter sich, zum Sprung bereit
# roar: die Krönung – das Haupt in den Nacken, die Arme ausgebreitet, der Kiefer offen
# stagger: Wasser! – gekrümmt, die Linke vor dem Gesicht
# death: er sinkt auf die Knie, das Haupt fällt nach vorn
POSES: dict[str, dict[str, list[float]]] = {
    "kneel": {"body": [6, 0, 0, 0, 11.5, 0], "head": [26, 0, 0], "left_arm": [17.6, 0, 9], "left_forearm": [-139.7, 25.9, 0], "right_arm": [20.2, 0, 6.8], "right_forearm": [-128.7, -30, 0], "blade": [193.8, -3.6, -14], "left_leg": [-84, 0, -4], "left_shin": [86, 0, 0], "right_leg": [4, 0, 4], "right_shin": [86, 0, 0]},
    "ready": {"body": [4, 0, 0], "head": [4, 0, 0], "left_arm": [25.3, 0, 24.5], "left_forearm": [-92, -39.4, 0], "right_arm": [26.8, 0, 19], "right_forearm": [-107.3, -24.3, 0], "blade": [24.7, 6.8, 9.5], "left_leg": [-6, 0, -3], "left_shin": [8, 0, 0], "right_leg": [6, 0, 3], "right_shin": [6, 0, 0]},
    "rise": {"body": [14, 0, 0, 0, 5, 0], "head": [6, 0, 0], "left_arm": [19.4, 0, -3.7], "left_forearm": [-132.4, 26.3, 0], "right_arm": [20.5, 0, -55.3], "right_forearm": [-122.2, 24.4, 0], "blade": [134.7, -4.2, 26.8], "left_leg": [-50, 0, -4], "left_shin": [50, 0, 0], "right_leg": [-10, 0, 4], "right_shin": [40, 0, 0]},
    "raise": {"body": [-6, 0, 0], "head": [-26, 0, 0], "left_arm": [28.7, 0, 22.8], "left_forearm": [-138.9, -60, 0], "right_arm": [-102.9, 0, 5.2], "right_forearm": [-106.5, -0.4, 0], "blade": [133.6, -1, 6.5], "left_leg": [-4, 0, -4], "right_leg": [4, 0, 4]},
    "slash_wind": {"body": [-4, 26, 0], "head": [-4, -18, 0], "left_arm": [22.4, 0, 32.4], "left_forearm": [-111.1, -52, 0], "right_arm": [-92.4, 0, -16.8], "right_forearm": [-150, 4.1, 0], "blade": [88.9, 0, 0.5], "left_leg": [-14, 0, -4], "left_shin": [10, 0, 0], "right_leg": [10, 0, 4]},
    "slash_strike": {"body": [12, -30, 0], "head": [4, 22, 0], "left_arm": [24.7, 0, 29.7], "left_forearm": [-97.2, -60, 0], "right_arm": [9.8, 0, -13.5], "right_forearm": [-81.6, -6, 0], "blade": [90.6, 0, -5.4], "left_leg": [-20, 0, -4], "left_shin": [16, 0, 0], "right_leg": [14, 0, 4]},
    "thrust": {"body": [16, -8, 0, 0, 2, 0], "head": [-10, 6, 0], "left_arm": [45, 0, 34.1], "left_forearm": [-107.5, -60, 0], "right_arm": [-12.1, 0, -10.4], "right_forearm": [-113, 0.3, 0], "blade": [110.5, -0.4, 5.4], "left_leg": [24, 0, -4], "left_shin": [20, 0, 0], "right_leg": [-34, 0, 4], "right_shin": [30, 0, 0]},
    "cast": {"body": [2, -10, 0], "head": [-8, -8, 0], "left_arm": [-61.6, 0, -9.5], "left_forearm": [-129.5, 4, 0], "right_arm": [27.7, 0, -2], "right_forearm": [-62.5, 15.3, 0], "blade": [79, 0.3, 5.2], "left_leg": [-6, 0, -3], "right_leg": [6, 0, 3]},
    "lift": {"body": [-8, 0, 0, 0, -1, 0], "head": [-30, 0, 0], "left_arm": [-92.9, 0, 8.7], "left_forearm": [-125.2, -2.1, 0], "right_arm": [-92.9, 0, -8.7], "right_forearm": [-125.2, 2.1, 0], "blade": [135.7, -0.1, 0.8], "left_leg": [-4, 0, -6], "right_leg": [4, 0, 6]},
    "throw": {"body": [14, -18, 0], "head": [0, 12, 0], "left_arm": [-1.3, 0, 32.1], "left_forearm": [-108.7, 13.4, 0], "right_arm": [-156.6, 0, -23.6], "right_forearm": [-132.1, -3.9, 0], "blade": [171, 6.9, -25], "left_leg": [-22, 0, -4], "left_shin": [16, 0, 0], "right_leg": [14, 0, 4]},
    "plunge": {"body": [30, 0, 0, 0, 3, 0], "head": [20, 0, 0], "left_arm": [13.8, 0, 18.7], "left_forearm": [-113.7, 6.7, 0], "right_arm": [7.5, 0, -32], "right_forearm": [-98.6, 8.7, 0], "blade": [137, -4.2, 25.8], "left_leg": [-26, 0, -5], "left_shin": [30, 0, 0], "right_leg": [-10, 0, 5], "right_shin": [24, 0, 0]},
    "storm": {"body": [24, 10, 0, 0, 3, 0], "head": [-18, -8, 0], "left_arm": [14.6, 0, -6.5], "left_forearm": [-130.3, -3.3, 0], "right_arm": [45, 0, -25.3], "right_forearm": [-97.1, 46.4, 0], "blade": [139.4, -9.6, 55.6], "left_leg": [-34, 0, -6], "left_shin": [40, 0, 0], "right_leg": [16, 0, 6], "right_shin": [36, 0, 0]},
    "roar": {"body": [-10, 0, 0], "head": [-36, 0, 0], "left_arm": [28.3, 0, 14.8], "left_forearm": [-111.1, -60, 0], "right_arm": [28.3, 0, 84.5], "right_forearm": [-111, -60, 0], "blade": [139.6, 1.7, -9.9], "left_leg": [-4, 0, -8], "right_leg": [4, 0, 8]},
    "stagger": {"body": [26, 6, 0, 0, 2, 0], "head": [30, -10, 0], "left_arm": [-78.9, 0, -20.4], "left_forearm": [-150, 5.6, 0], "right_arm": [5.2, 0, 3.2], "right_forearm": [-63.8, -1.6, 0], "blade": [69.3, -0.4, -3.9], "left_leg": [-16, 0, -6], "left_shin": [22, 0, 0], "right_leg": [8, 0, 6], "right_shin": [12, 0, 0]},
    "death": {"body": [28, 0, 0, 0, 11.5, 0], "head": [44, 0, 8], "left_arm": [22.5, 0, 42], "left_forearm": [-117, -43.9, 0], "right_arm": [25.8, 0, 34.2], "right_forearm": [-121.2, -29.1, 0], "blade": [92.1, -0.2, 19.8], "left_leg": [-84, 0, -4], "left_shin": [86, 0, 0], "right_leg": [4, 0, 4], "right_shin": [86, 0, 0]},
}

# Abläufe: (Tick, Haltung, Übergang dorthin: 0 weich, 1 beschleunigend, 2 abbremsend) ----------------------------------
TIMELINES: dict[str, list[tuple[float, str, int]]] = {
    "AWAKEN": [(0, "kneel", 0), (16, "kneel", 0), (34, "rise", 2), (46, "raise", 2), (60, "raise", 0), (68, "slash_strike", 1),
               (80, "ready", 0)],
    "SLASH": [(0, "ready", 0), (10, "slash_wind", 2), (15, "slash_strike", 1), (22, "slash_strike", 0), (32, "ready", 0)],
    "BLINK": [(0, "ready", 0), (7, "storm", 2), (14, "slash_wind", 0), (19, "slash_strike", 1), (28, "slash_strike", 0),
              (40, "ready", 0)],
    "ILLUSIONS": [(0, "ready", 0), (10, "cast", 2), (30, "cast", 0), (44, "ready", 0)],
    "HURL": [(0, "ready", 0), (10, "lift", 2), (22, "lift", 0), (26, "throw", 1), (31, "lift", 2), (36, "throw", 1), (41, "lift", 2),
             (46, "throw", 1), (54, "throw", 0), (64, "ready", 0)],
    "RIFTS": [(0, "ready", 0), (8, "raise", 2), (14, "plunge", 1), (40, "plunge", 0), (46, "rise", 0), (56, "ready", 0)],
    "STORM": [(0, "ready", 0), (4, "storm", 2), (10, "slash_strike", 1), (18, "storm", 2), (24, "slash_strike", 1), (32, "storm", 2),
              (38, "slash_strike", 1), (46, "storm", 2), (52, "slash_strike", 1), (60, "storm", 2), (66, "slash_strike", 1),
              (74, "slash_strike", 0), (84, "ready", 0)],
    "CROWN": [(0, "ready", 0), (10, "roar", 2), (50, "roar", 0), (60, "raise", 2), (72, "ready", 0)],
    "STAGGER": [(0, "stagger", 1), (40, "stagger", 0), (50, "ready", 0)],
}


def bones(p: dict) -> dict[str, list[float]]:
    out = {}
    for b in BONES:
        v = list(p.get(b, []))
        out[b] = (v + [0.0] * 6)[:6]
    return out


def fmt(v: float) -> str:
    s = f"{v:.2f}".rstrip("0").rstrip(".")
    return (s if s not in ("-0", "") else "0") + "f"


def java() -> str:
    names = list(POSES)
    lines = [
        "package io.github.sirschrolli.kingdomsatwar.client.renderer.end;",
        "",
        "/**",
        " * Haltungen und Abläufe von Phasmor, dem Leerwandlerkönig: je Haltung Zusatzwinkel (Grad) und Verschiebungen (Pixel) der",
        " * Knochen, je Angriff eine Folge von Haltungen an Ticks (dazwischen wird überblendet).",
        " *",
        " * <p>ERZEUGT von tools/phasmor_poses.py – nicht von Hand ändern.",
        " */",
        "final class PhasmorPoses {",
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
    lines.append("    private PhasmorPoses() {}")
    lines.append("}")
    return "\n".join(lines) + "\n"


def preview(target: str) -> None:
    sys.path.insert(0, str(ROOT / "tools"))
    from entitygen.models import phasmor as mod
    from entitygen.paint import paint_model
    from entitygen.preview import render
    from PIL import Image, ImageDraw

    m = mod.build()
    m.pack()
    img, glow = paint_model(m, mod.materials("phasmor"), mod.PALETTES["phasmor"], seed=100)
    size = 260
    names = list(POSES)
    cols = 5
    rows = (len(names) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * size, rows * (size + 20)), (30, 32, 36, 255))
    d = ImageDraw.Draw(sheet)
    for i, n in enumerate(names):
        b = bones(POSES[n])
        pose = {k: (v[0], v[1], v[2], 1, 1, 1, v[3], v[4], v[5]) for k, v in b.items()}
        im = render(m, img, glow, -30, 8, size, pose)
        x, y = (i % cols) * size, (i // cols) * (size + 20)
        sheet.paste(im, (x, y + 20))
        d.text((x + 6, y + 4), n, fill=(160, 240, 220, 255))
    sheet.save(target)


if __name__ == "__main__":
    if "--preview" in sys.argv:
        preview(sys.argv[sys.argv.index("--preview") + 1])
    else:
        TARGET.parent.mkdir(parents=True, exist_ok=True)
        TARGET.write_text(java(), encoding="utf-8")
        print(TARGET)
