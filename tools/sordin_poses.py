#!/usr/bin/env python3
"""
Haltungen und Abläufe von Sordin, dem Kantor der Stille (1.7) – erzeugt SordinPoses.java.

Jede Haltung gibt je Knochen Zusatzwinkel (Grad) zur Grundhaltung des Modells an (Arme hängen, die Stimmgabel zeigt nach
vorn), dazu (optional) Verschiebungen in Pixeln; negatives rx hebt einen Arm nach vorn, ein negatives rx am Unterarm beugt
den Ellbogen. Die Abläufe (je Angriff) reihen Haltungen an Ticks auf; dazwischen wird weich überblendet (beim Zuschlagen
beschleunigend).

Aufruf (aus dem Projektordner):
  python3 tools/sordin_poses.py                 # schreibt die Java-Klasse
  python3 tools/sordin_poses.py --preview a.png  # zeigt alle Haltungen
"""
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TARGET = ROOT / "src/main/java/io/github/sirschrolli/kingdomsatwar/client/renderer/deep/SordinPoses.java"

BONES = ["body", "head", "left_arm", "left_forearm", "left_hand", "right_arm", "right_forearm", "right_hand", "fork", "left_leg",
         "left_shin", "right_leg", "right_shin", "skirt", "skirt_low", "pipes"]

# Haltungen: je Knochen [rx, ry, rz, dx, dy, dz] (fehlende = 0) ---------------------------------------------------------
# play: an der Orgel – vorgebeugt, beide Hände vorn auf dem Spieltisch, der Kopf zu den Pfeifen erhoben
# listen: er hört etwas – der Kopf neigt sich zur Seite, die Linke hebt sich zur Kapuze
# ready: kampfbereit – die Stimmgabel schräg vor sich, die Zinken nach oben vorn, die Linke offen
# raise: die Gabel empor (beim Erwachen)
# swing_wind/swing_strike: der Hieb mit der Gabel – über die rechte Schulter ausholen, schräg hinab
# strike: die Gabel senkrecht vor sich, die Linke schlägt an die Zinken (die Gabel klingt)
# aim: die Schallwelle – die Gabel weist auf den Gegner, die Linke am Mund
# conduct_up/conduct_down: der Akkord – er dirigiert: beide Arme empor, dann hinab und auseinander
# sing: der Notenschwarm – das Haupt in den Nacken, die Arme weit, die Pfeifen blasen
# hush: die Stille – der Zeigefinger der Linken vor dem Mund
# fortissimo: unter halber Kraft – die Arme empor und auseinander, das Haupt zurück
# stagger: jemand spielt auf der Orgel! – gekrümmt, die Hände an der Kapuze (er hält sich die Ohren zu)
# death: er sinkt auf die Knie, das Haupt fällt nach vorn
POSES: dict[str, dict[str, list[float]]] = {
    "play": {"body": [12, 0, 0], "head": [-22, 0, 0], "left_arm": [-8.9, 4.1, -1.7], "left_forearm": [-94.5, 1.6, 0],
             "left_hand": [46.9, 0, -1.7], "right_arm": [-8.9, -4.1, 1.7], "right_forearm": [-94.5, -1.6, 0], "right_hand": [46.9, 0, 1.7],
             "left_leg": [-8, 0, -3], "left_shin": [6, 0, 0], "right_leg": [-4, 0, 3], "right_shin": [4, 0, 0], "skirt": [-8, 0, 0],
             "skirt_low": [4, 0, 0]},
    "listen": {"body": [4, 0, 0], "head": [-6, 18, 22], "left_arm": [3.2, 6.9, -18.3], "left_forearm": [-150, 4.6, 0],
               "left_hand": [-44.9, 0, -38.2], "right_arm": [-20, 0, 8], "right_forearm": [-30, 0, 0], "skirt": [-4, 0, 0]},
    "ready": {"body": [4, 0, 0], "head": [6, 0, 0], "left_arm": [-20, 0, -18], "left_forearm": [-30, 0, 0], "left_hand": [-10, 0, 0],
              "right_arm": [-28, 0, 6], "right_forearm": [-58, 0, 0], "right_hand": [-4, 0, 0], "fork": [-62, 0, 0],
              "left_leg": [-6, 0, -3], "left_shin": [6, 0, 0], "right_leg": [6, 0, 3], "right_shin": [4, 0, 0]},
    "raise": {"body": [-8, 0, 0], "head": [-24, 0, 0], "left_arm": [-24, 0, -40], "left_forearm": [-20, 0, 0],
              "right_arm": [-168, 0, 4], "right_forearm": [-8, 0, 0], "fork": [90, 0, 0], "skirt": [6, 0, 0]},
    "swing_wind": {"body": [-6, 24, 0], "head": [-6, -16, 0], "left_arm": [-40, 0, -24], "left_forearm": [-40, 0, 0],
                   "right_arm": [-160, 0, 12], "right_forearm": [-40, 0, 0], "fork": [60, 0, 0], "left_leg": [-12, 0, -3],
                   "right_leg": [10, 0, 3]},
    "swing_strike": {"body": [16, -28, 0, 0, 1, 0], "head": [8, 20, 0], "left_arm": [-10, 0, -30], "left_forearm": [-20, 0, 0],
                     "right_arm": [-50, 0, 6], "right_forearm": [-12, 0, 0], "fork": [90, 0, 0], "left_leg": [-20, 0, -3],
                     "left_shin": [14, 0, 0], "right_leg": [14, 0, 3], "skirt": [-8, 0, 0]},
    "strike": {"body": [2, 0, 0], "head": [10, 0, 0], "left_arm": [-79.1, -1.3, -25.5], "left_forearm": [-104.9, 3.6, 0],
               "left_hand": [49.6, 0, 39], "right_arm": [-30, 0, 8], "right_forearm": [-70, 0, 0], "fork": [0, 0, 0]},
    "aim": {"body": [6, -10, 0], "head": [2, 8, 0], "left_arm": [1.3, -1.3, -3.6], "left_forearm": [-138.6, 30, 0],
            "left_hand": [-51.4, 0, 0.1], "right_arm": [-88, 0, 2], "right_forearm": [-4, 0, 0], "fork": [90, 0, 0],
            "left_leg": [-14, 0, -3], "left_shin": [10, 0, 0], "right_leg": [12, 0, 3]},
    "conduct_up": {"body": [-6, 0, 0], "head": [-20, 0, 0], "left_arm": [-165, 0, 16], "left_forearm": [-25, 0, 0],
                   "left_hand": [-15, 0, 0], "right_arm": [-165, 0, -16], "right_forearm": [-25, 0, 0], "fork": [90, 0, 0],
                   "skirt": [4, 0, 0]},
    "conduct_down": {"body": [12, 0, 0, 0, 1, 0], "head": [10, 0, 0], "left_arm": [-45, 0, -45], "left_forearm": [-15, 0, 0],
                     "left_hand": [20, 0, 0], "right_arm": [-45, 0, 45], "right_forearm": [-15, 0, 0], "fork": [90, 0, 0],
                     "left_leg": [-8, 0, -4], "right_leg": [8, 0, 4], "skirt": [-6, 0, 0]},
    "sing": {"body": [-12, 0, 0], "head": [-36, 0, 0], "left_arm": [-30, 0, -78], "left_forearm": [-14, 0, 0], "left_hand": [-20, 0, 0],
             "right_arm": [-30, 0, 78], "right_forearm": [-14, 0, 0], "fork": [90, 0, 0], "pipes": [-8, 0, 0], "skirt": [8, 0, 0],
             "skirt_low": [4, 0, 0]},
    "hush": {"body": [10, 0, 0], "head": [10, 0, 0], "left_arm": [-3.5, 7.4, -1.5], "left_forearm": [-131.2, 30, 0],
             "left_hand": [-55.4, 0, 7.5], "right_arm": [-16, 0, 10], "right_forearm": [-30, 0, 0], "fork": [-50, 0, 0],
             "skirt": [-4, 0, 0]},
    "fortissimo": {"body": [-14, 0, 0], "head": [-40, 0, 0], "left_arm": [-160, 0, 40], "left_forearm": [-10, 0, 0],
                   "left_hand": [-20, 0, 0], "right_arm": [-160, 0, -40], "right_forearm": [-10, 0, 0], "fork": [90, 0, 0],
                   "pipes": [-12, 0, 0], "left_leg": [-4, 0, -6], "right_leg": [4, 0, 6], "skirt": [10, 0, 0], "skirt_low": [6, 0, 0]},
    "stagger": {"body": [28, 6, 0, 0, 2, 0], "head": [24, -10, 0], "left_arm": [-16.8, 0.8, -1.3], "left_forearm": [-150, 0.8, 0],
                "left_hand": [-44.9, 0, -0.5], "right_arm": [-18.3, -13.4, 1.8], "right_forearm": [-143, 5.1, 0],
                "right_hand": [-49.1, 0, -5.5], "fork": [-30, 0, 0], "left_leg": [-18, 0, -6], "left_shin": [22, 0, 0],
                "right_leg": [8, 0, 6], "right_shin": [12, 0, 0], "skirt": [-16, 0, 0]},
    "death": {"body": [30, 0, 0, 0, 11, 0], "head": [40, 0, 8], "left_arm": [20, 0, -30], "left_forearm": [-30, 0, 0],
              "right_arm": [24, 0, 26], "right_forearm": [-30, 0, 0], "fork": [-10, 0, 0], "left_leg": [-84, 0, -4], "left_shin": [86, 0, 0],
              "right_leg": [-80, 0, 4], "right_shin": [84, 0, 0], "skirt": [-40, 0, 0], "skirt_low": [30, 0, 0]},
}

# Abläufe: (Tick, Haltung, Übergang dorthin: 0 weich, 1 beschleunigend, 2 abbremsend) ----------------------------------
TIMELINES: dict[str, list[tuple[float, str, int]]] = {
    "AWAKEN": [(0, "play", 0), (14, "listen", 2), (34, "listen", 0), (50, "raise", 2), (66, "raise", 0), (72, "swing_strike", 1),
               (80, "ready", 0)],
    "SWING": [(0, "ready", 0), (9, "swing_wind", 2), (14, "swing_strike", 1), (20, "swing_strike", 0), (30, "ready", 0)],
    "SONIC": [(0, "ready", 0), (8, "strike", 2), (12, "strike", 0), (22, "aim", 2), (40, "aim", 0), (50, "ready", 0)],
    "CHORD": [(0, "ready", 0), (8, "conduct_up", 2), (14, "conduct_down", 1), (22, "conduct_up", 2), (28, "conduct_down", 1),
              (36, "conduct_up", 2), (42, "conduct_down", 1), (52, "conduct_down", 0), (60, "ready", 0)],
    "NOTES": [(0, "ready", 0), (12, "sing", 2), (50, "sing", 0), (60, "ready", 0)],
    # er bedeutet Stille (Finger an den Mund), verschwindet (12) – und taucht hinter dem Ziel schon ausholend auf (44), Hieb (54)
    "SILENCE": [(0, "ready", 0), (8, "hush", 2), (36, "hush", 0), (44, "swing_wind", 2), (54, "swing_strike", 1), (60, "swing_strike", 0),
                (70, "ready", 0)],
    "FORTISSIMO": [(0, "ready", 0), (12, "fortissimo", 2), (56, "fortissimo", 0), (64, "conduct_down", 1), (76, "ready", 0)],
    "STAGGER": [(0, "stagger", 1), (50, "stagger", 0), (60, "ready", 0)],
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
        "package io.github.sirschrolli.kingdomsatwar.client.renderer.deep;",
        "",
        "/**",
        " * Haltungen und Abläufe von Sordin, dem Kantor der Stille: je Haltung Zusatzwinkel (Grad) und Verschiebungen (Pixel) der",
        " * Knochen, je Angriff eine Folge von Haltungen an Ticks (dazwischen wird überblendet).",
        " *",
        " * <p>ERZEUGT von tools/sordin_poses.py – nicht von Hand ändern.",
        " */",
        "final class SordinPoses {",
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
    lines.append("    private SordinPoses() {}")
    lines.append("}")
    return "\n".join(lines) + "\n"


def preview(target: str, names: list[str] | None = None, yaw: float = -30, pitch: float = 8) -> None:
    sys.path.insert(0, str(ROOT / "tools"))
    from entitygen.models import sordin as mod
    from entitygen.paint import paint_model
    from entitygen.preview import render
    from PIL import Image, ImageDraw

    m = mod.build()
    m.pack()
    img, glow = paint_model(m, mod.materials("sordin"), mod.PALETTES["sordin"], seed=100)
    size = 300
    names = names or list(POSES)
    cols = 5
    rows = (len(names) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * size, rows * (size + 20)), (30, 32, 36, 255))
    d = ImageDraw.Draw(sheet)
    for i, n in enumerate(names):
        b = bones(POSES[n])
        pose = {k: (v[0], v[1], v[2], 1, 1, 1, v[3], v[4], v[5]) for k, v in b.items()}
        im = render(m, img, glow, yaw, pitch, size, pose)
        x, y = (i % cols) * size, (i // cols) * (size + 20)
        sheet.paste(im, (x, y + 20))
        d.text((x + 6, y + 4), n, fill=(160, 240, 220, 255))
    sheet.save(target)


if __name__ == "__main__":
    if "--preview" in sys.argv:
        args = sys.argv[sys.argv.index("--preview") + 1:]
        names = args[1].split(",") if len(args) > 1 and args[1] != "-" else None
        yaw = float(args[2]) if len(args) > 2 else -30
        preview(args[0], names, yaw)
    else:
        TARGET.parent.mkdir(parents=True, exist_ok=True)
        TARGET.write_text(java(), encoding="utf-8")
        print(TARGET)
