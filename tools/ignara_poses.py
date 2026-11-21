#!/usr/bin/env python3
"""
Haltungen und Abläufe von Ignara, der Flammenpriesterin (1.2) – erzeugt IgnaraPoses.java.

Jede Haltung gibt je Knochen Zusatzwinkel (Grad) zur Grundhaltung des Modells an (Arme leicht vom Körper, Unterarme
angewinkelt, die Peitsche zeigt nach vorn): negatives rx hebt einen Arm nach vorn, rz spreizt (links negativ, rechts
positiv), beim Rumpf neigt positives rx nach vorn. Die Abläufe (je Angriff) reihen Haltungen an Ticks auf; dazwischen
wird weich überblendet (beim Zuschlagen beschleunigend).

Aufruf (aus dem Projektordner):
  python3 tools/ignara_poses.py                 # schreibt die Java-Klasse
  python3 tools/ignara_poses.py --preview a.png  # zeigt alle Haltungen
"""
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TARGET = ROOT / "src/main/java/io/github/sirschrolli/kingdomsatwar/client/renderer/nether/IgnaraPoses.java"

BONES = ["body", "head", "left_arm", "left_forearm", "right_arm", "right_forearm", "whip"]

# Haltungen: je Knochen [rx, ry, rz, dx, dy, dz] (fehlende = 0) ---------------------------------------------------------
POSES: dict[str, dict[str, list[float]]] = {
    # kampfbereit: die Feuerkugel vor der Brust, die Peitsche locker nach vorn
    "ready": {"left_arm": [-28, 0, 4], "left_forearm": [-22, 0, 0], "right_arm": [-16, 0, 0], "right_forearm": [-8, 0, 0],
              "whip": [30, 0, 0]},
    # Gebet: das Haupt gesenkt, die Hände vor der Brust zusammen, die Peitsche hängt
    "pray": {"body": [8, 0, 0, 0, 0.5, 0], "head": [28, 0, 0], "left_arm": [-14, 0, 22], "left_forearm": [-66, 0, 0],
             "right_arm": [-14, 0, -22], "right_forearm": [-66, 0, 0], "whip": [150, 0, 0]},
    # Erwachen: aufblicken, die Arme ausbreiten
    "awaken_look": {"body": [-4, 0, 0], "head": [-22, 0, 0], "left_arm": [-24, 0, -20], "left_forearm": [-20, 0, 0],
                    "right_arm": [-24, 0, 20], "right_forearm": [-20, 0, 0], "whip": [60, 0, 0]},
    "awaken_spread": {"body": [-10, 0, 0], "head": [-30, 0, 0], "left_arm": [-30, 0, -78], "left_forearm": [-6, 0, 0],
                      "right_arm": [-30, 0, 78], "right_forearm": [-6, 0, 0], "whip": [20, 0, 0]},
    # Peitschenhieb: hoch und hinter die Schulter, dann quer nach vorn (und zurück, mit dem Handrücken)
    "lash_wind": {"body": [-4, 22, 0], "head": [-6, -16, 0], "left_arm": [-30, 0, -24], "left_forearm": [-20, 0, 0],
                  "right_arm": [-150, 0, 26], "right_forearm": [-24, 0, 0], "whip": [-20, 0, 0]},
    "lash_strike": {"body": [10, -26, 0], "head": [4, 20, 0], "left_arm": [-20, 0, -18], "left_forearm": [-30, 0, 0],
                    "right_arm": [-74, 0, -22], "right_forearm": [-6, 0, 0], "whip": [40, 0, 0]},
    "lash_back": {"body": [6, 24, 0], "head": [0, -18, 0], "left_arm": [-20, 0, -18], "left_forearm": [-30, 0, 0],
                  "right_arm": [-80, 0, 50], "right_forearm": [-4, 0, 0], "whip": [30, 0, 0]},
    # Flammenschlinge: die Peitsche kreist über dem Kopf, schnellt nach vorn, reißt heran
    "snare_spin": {"body": [-6, 0, 0], "head": [-10, 0, 0], "left_arm": [-30, 0, -20], "left_forearm": [-24, 0, 0],
                   "right_arm": [-172, 0, 8], "right_forearm": [-6, 0, 0], "whip": [-10, 0, 0]},
    "snare_throw": {"body": [12, 0, 0], "head": [2, 0, 0], "left_arm": [-20, 0, -26], "left_forearm": [-20, 0, 0],
                    "right_arm": [-96, 0, 0], "right_forearm": [0, 0, 0], "whip": [-6, 0, 0]},
    "snare_pull": {"body": [-14, 10, 0], "head": [-6, -8, 0], "left_arm": [-34, 0, -18], "left_forearm": [-30, 0, 0],
                   "right_arm": [-36, 0, 18], "right_forearm": [-40, 0, 0], "whip": [-30, 0, 0]},
    # Lavawoge: beide Arme empor, dann die Feuerkugel auf den Boden
    "wave_raise": {"body": [-10, 0, 0, 0, -1, 0], "head": [-18, 0, 0], "left_arm": [-170, 0, -14], "left_forearm": [-6, 0, 0],
                   "right_arm": [-170, 0, 14], "right_forearm": [-6, 0, 0], "whip": [60, 0, 0]},
    "wave_slam": {"body": [28, 0, 0, 0, 1.5, 0], "head": [12, 0, 0], "left_arm": [-44, 0, 6], "left_forearm": [-4, 0, 0],
                  "right_arm": [-44, 0, -6], "right_forearm": [-4, 0, 0], "whip": [70, 0, 0]},
    # Feuersäulen: die Linke mit der Feuerkugel nach vorn gestreckt, die Rechte weit zur Seite
    "pillars_cast": {"body": [4, 14, 0], "head": [6, -10, 0], "left_arm": [-104, 0, 6], "left_forearm": [4, 0, 0],
                     "right_arm": [-26, 0, 36], "right_forearm": [-20, 0, 0], "whip": [50, 0, 0]},
    # Opalregen: die Peitsche hoch empor, die Linke zur Seite, der Blick nach oben
    "rain_raise": {"body": [-8, 0, 0], "head": [-32, 0, 0], "left_arm": [-36, 0, -66], "left_forearm": [-10, 0, 0],
                   "right_arm": [-176, 0, -4], "right_forearm": [0, 0, 0], "whip": [-40, 0, 0]},
    # Ruf der Glutgeister: die Arme weit ausgebreitet
    "call": {"body": [-6, 0, 0], "head": [-20, 0, 0], "left_arm": [-44, 0, -86], "left_forearm": [-12, 0, 0],
             "right_arm": [-44, 0, 86], "right_forearm": [-12, 0, 0], "whip": [40, 0, 0]},
    # Phönixgestalt: das Feuer sammeln (Arme vor der Brust gekreuzt), dann auffahren
    "phoenix_gather": {"body": [14, 0, 0, 0, 1, 0], "head": [24, 0, 0], "left_arm": [-62, 0, 40], "left_forearm": [-80, 0, 0],
                       "right_arm": [-62, 0, -40], "right_forearm": [-80, 0, 0], "whip": [80, 0, 0]},
    "phoenix_burst": {"body": [-16, 0, 0, 0, -1, 0], "head": [-32, 0, 0], "left_arm": [-40, 0, -112], "left_forearm": [-4, 0, 0],
                      "right_arm": [-40, 0, 112], "right_forearm": [-4, 0, 0], "whip": [10, 0, 0]},
    # Phönixsturz: waagerecht nach vorn, die Arme zurückgelegt
    "dive": {"body": [62, 0, 0, 0, 2, 0], "head": [-46, 0, 0], "left_arm": [36, 0, -26], "left_forearm": [10, 0, 0],
             "right_arm": [36, 0, 26], "right_forearm": [10, 0, 0], "whip": [80, 0, 0]},
    # Tod: sie sinkt in sich zusammen, die Arme fallen
    "death": {"body": [30, 0, 0, 0, 3, 0], "head": [44, 0, 8], "left_arm": [16, 0, 6], "left_forearm": [10, 0, 0],
              "right_arm": [16, 0, -6], "right_forearm": [10, 0, 0], "whip": [90, 0, 0]},
}

# Abläufe: (Tick, Haltung, Übergang dorthin: 0 weich, 1 beschleunigend, 2 abbremsend) ----------------------------------
TIMELINES: dict[str, list[tuple[float, str, int]]] = {
    "AWAKEN": [(0, "pray", 0), (14, "pray", 0), (26, "awaken_look", 0), (38, "awaken_spread", 2), (40, "lash_wind", 0),
               (44, "lash_strike", 1), (54, "lash_strike", 0), (70, "ready", 0)],
    "LASH": [(0, "ready", 0), (9, "lash_wind", 2), (12, "lash_strike", 1), (18, "lash_strike", 0), (22, "lash_back", 1),
             (26, "lash_back", 0), (32, "ready", 0)],
    "SNARE": [(0, "ready", 0), (6, "snare_spin", 0), (14, "snare_spin", 0), (16, "snare_throw", 1), (20, "snare_pull", 0),
              (31, "snare_pull", 0), (34, "lash_strike", 1), (42, "lash_strike", 0), (56, "ready", 0)],
    "WAVE": [(0, "ready", 0), (14, "wave_raise", 2), (18, "wave_slam", 1), (24, "wave_raise", 2), (30, "wave_slam", 1),
             (40, "wave_slam", 0), (50, "ready", 0)],
    "PILLARS": [(0, "ready", 0), (8, "pillars_cast", 2), (28, "pillars_cast", 0), (32, "wave_slam", 1), (40, "pillars_cast", 0),
                (60, "pillars_cast", 0), (62, "ready", 0)],
    "OPAL_RAIN": [(0, "ready", 0), (10, "rain_raise", 2), (56, "rain_raise", 0), (66, "ready", 0)],
    "CALL": [(0, "ready", 0), (8, "call", 2), (18, "call", 0), (36, "ready", 0)],
    "PHOENIX": [(0, "ready", 0), (20, "phoenix_gather", 0), (26, "phoenix_burst", 1), (42, "phoenix_burst", 0), (70, "ready", 0)],
    "DIVE": [(0, "ready", 0), (10, "rain_raise", 2), (13, "dive", 1), (26, "dive", 0), (34, "ready", 0), (46, "ready", 0)],
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
        "package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;",
        "",
        "/**",
        " * Haltungen und Abläufe von Ignara, der Flammenpriesterin: je Haltung Zusatzwinkel (Grad) und Verschiebungen (Pixel) der",
        " * Knochen, je Angriff eine Folge von Haltungen an Ticks (dazwischen wird überblendet).",
        " *",
        " * <p>ERZEUGT von tools/ignara_poses.py – nicht von Hand ändern.",
        " */",
        "final class IgnaraPoses {",
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
    lines.append("    private IgnaraPoses() {}")
    lines.append("}")
    return "\n".join(lines) + "\n"


def preview(target: str) -> None:
    sys.path.insert(0, str(ROOT / "tools"))
    from entitygen.models import ignara as mod
    from entitygen.paint import paint_model
    from entitygen.preview import render
    from PIL import Image, ImageDraw

    m = mod.build()
    m.pack()
    img, glow = paint_model(m, mod.materials("ignara"), mod.PALETTES["ignara"], seed=100)
    size = 300
    names = list(POSES)
    cols = 5
    rows = (len(names) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * size, rows * (size + 20)), (30, 32, 36, 255))
    d = ImageDraw.Draw(sheet)
    for i, n in enumerate(names):
        b = bones(POSES[n])
        pose = {k: (v[0], v[1], v[2], 1, 1, 1, v[3], v[4], v[5]) for k, v in b.items()}
        pose["left_wing"] = (0, 0, 0, 0.001, 0.001, 0.001, 0, 0, 0)
        pose["right_wing"] = (0, 0, 0, 0.001, 0.001, 0.001, 0, 0, 0)
        im = render(m, img, glow, -30, 8, size, pose)
        x, y = (i % cols) * size, (i // cols) * (size + 20)
        sheet.paste(im, (x, y + 20))
        d.text((x + 6, y + 4), n, fill=(240, 220, 160, 255))
    sheet.save(target)


if __name__ == "__main__":
    if "--preview" in sys.argv:
        preview(sys.argv[sys.argv.index("--preview") + 1])
    else:
        TARGET.write_text(java(), encoding="utf-8")
        print(TARGET)
