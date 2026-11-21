#!/usr/bin/env python3
"""
Haltungen und Abläufe des Steinhüters (0.96) – erzeugt StoneKeeperPoses.java.

Wie bei Malleus (tools/glutschmied_poses.py) wird jede Haltung in Weltwinkeln beschrieben (Grad; 0 = senkrecht nach unten,
90 = waagerecht nach vorn, 180 = nach oben): je Arm Oberarm und Unterarm, je Bein Schenkel und Schienbein. Daraus entstehen
die Zusatzwinkel je Knochen, die das Modell im Spiel zu seiner Grundhaltung addiert. Die Abläufe (je Angriff) reihen
Haltungen an Ticks auf; dazwischen wird weich überblendet (beim Zuschlagen beschleunigend).

Aufruf (aus dem Projektordner):
  python3 tools/stone_keeper_poses.py                 # schreibt die Java-Klasse
  python3 tools/stone_keeper_poses.py --preview a.png  # zeigt alle Haltungen

Felder einer Haltung: lean (Rumpf nach vorn, zusätzlich zur Grundneigung von 10°), drop (Rumpf und Hüfte tiefer, Pixel),
twist (Rumpf-Drehung), head [x, y, z], jaw, l/r = [Oberarm, Unterarm] (Weltwinkel), lz/rz (Arme zur Seite hinaus, Grad),
legs = [links, rechts] und knees = [links, rechts] (Weltwinkel von Schenkel und Schienbein), legz (Spreizung),
boulder = [dx, dy, dz] (der Felsbrocken über dem Kopf, im Raum des Rumpfs).
"""
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TARGET = ROOT / "src/main/java/io/github/sirschrolli/kingdomsatwar/client/renderer/fae/StoneKeeperPoses.java"

BONES = ["body", "head", "jaw", "left_arm", "left_forearm", "right_arm", "right_forearm", "left_leg", "left_shin", "right_leg",
         "right_shin", "boulder"]
BODY_TILT = 10.0
FOREARM_BASE = -14.0

# Haltungen ---------------------------------------------------------------------------------------------------------
POSES: dict[str, dict] = {
    # kampfbereit: gebeugt, die Fäuste hängen schwer vor den Knien, die Knie leicht gebeugt
    "ready": {"lean": 6, "drop": 0.6, "head": [-10, 0, 0], "l": [8, 26], "lz": 4, "r": [8, 26], "rz": 4,
              "legs": [10, -4], "knees": [-2, -8], "legz": 3},
    # der Schlaf: auf ein Knie gesunken, die Fäuste auf dem Boden, der Kopf gesenkt
    "dormant": {"lean": 30, "drop": 8.5, "head": [34, 0, 0], "l": [36, 4], "lz": 8, "r": [44, 2], "rz": 6,
                "legs": [78, -16], "knees": [4, -96], "legz": 6},
    # Erwachen: der Kopf hebt sich, er stemmt sich hoch, richtet sich auf und brüllt mit erhobenen Fäusten
    "wake_look": {"lean": 26, "drop": 8.5, "head": [-6, 0, 0], "l": [36, 4], "lz": 8, "r": [44, 2], "rz": 6,
                  "legs": [78, -16], "knees": [4, -96], "legz": 6},
    "wake_push": {"lean": 22, "drop": 4.5, "head": [-14, 0, 0], "l": [26, 6], "lz": 10, "r": [30, 8], "rz": 10,
                  "legs": [54, -6], "knees": [-6, -44], "legz": 5},
    "wake_roar": {"lean": -16, "drop": 0, "head": [-34, 0, 0], "jaw": 34, "l": [118, 160], "lz": 34, "r": [118, 160], "rz": 34,
                  "legs": [12, -12], "knees": [0, -6], "legz": 7},
    # Faustschlag: beide Fäuste hoch über den Kopf, dann vor sich auf den Boden
    "smash_raise": {"lean": -14, "head": [-20, 0, 0], "jaw": 10, "l": [168, 196], "lz": -6, "r": [168, 196], "rz": -6,
                    "legs": [14, -10], "knees": [0, -6], "legz": 5},
    "smash_hit": {"lean": 42, "drop": 5.5, "head": [6, 0, 0], "jaw": 18, "l": [50, 62], "lz": -10, "r": [50, 62], "rz": -10,
                  "legs": [40, -20], "knees": [-10, -42], "legz": 6},
    # Stampfer: das rechte Bein hoch, Arme zur Seite – und mit aller Wucht hinab
    "stomp_lift": {"lean": -6, "twist": 6, "head": [-14, 0, 0], "l": [30, 52], "lz": 30, "r": [40, 70], "rz": 26,
                   "legs": [-8, 74], "knees": [-6, -2], "legz": 4},
    "stomp_hit": {"lean": 22, "drop": 3.0, "head": [10, 0, 0], "jaw": 16, "l": [16, 36], "lz": 22, "r": [22, 40], "rz": 22,
                  "legs": [-14, 30], "knees": [-30, -6], "legz": 6},
    # Felswurf: tief in die Knie, eine Faust gräbt sich in den Boden – den Brocken über den Kopf – und schleudern
    "boulder_dig": {"lean": 34, "drop": 5.0, "head": [14, 0, 0], "l": [30, 10], "lz": 14, "r": [62, 26], "rz": 2,
                    "legs": [42, -10], "knees": [-8, -46], "legz": 6, "boulder": [0, 30, -14]},
    "boulder_lift": {"lean": -14, "head": [-22, 0, 0], "jaw": 12, "l": [158, 188], "lz": -14, "r": [158, 188], "rz": -14,
                     "legs": [12, -10], "knees": [0, -6], "legz": 6, "boulder": [0, 0, 0]},
    "boulder_throw": {"lean": 30, "drop": 2.0, "head": [-4, 0, 0], "jaw": 22, "l": [98, 116], "lz": -8, "r": [98, 116], "rz": -8,
                      "legs": [30, -24], "knees": [-6, -30], "legz": 6, "boulder": [0, 0, 0]},
    # Kristallspur: beide Fäuste senkrecht in den Boden
    "crystals_slam": {"lean": 44, "drop": 6.0, "head": [2, 0, 0], "jaw": 20, "l": [40, 6], "lz": 2, "r": [40, 6], "rz": 2,
                      "legs": [46, -18], "knees": [-6, -50], "legz": 7},
    # Mondstrahl: die Brust heraus, die Arme weit zurück – das Herz öffnet sich
    "beam_charge": {"lean": -18, "head": [-24, 0, 0], "jaw": 8, "l": [-22, -6], "lz": 36, "r": [-22, -6], "rz": 36,
                    "legs": [14, -12], "knees": [0, -8], "legz": 7},
    "beam_fire": {"lean": 2, "drop": 1.5, "head": [-12, 0, 0], "jaw": 26, "l": [-28, -12], "lz": 40, "r": [-28, -12], "rz": 40,
                  "legs": [22, -18], "knees": [-6, -22], "legz": 8},
    # Felssprung: tief in die Knie, Arme zurück – in der Luft die Fäuste hoch, die Beine angezogen – Landung mit Wucht
    "leap_crouch": {"lean": 34, "drop": 6.5, "head": [-12, 0, 0], "l": [-34, -14], "lz": 10, "r": [-34, -14], "rz": 10,
                    "legs": [56, 50], "knees": [-30, -36], "legz": 6},
    "leap_air": {"lean": 4, "head": [-10, 0, 0], "jaw": 14, "l": [150, 178], "lz": 18, "r": [150, 178], "rz": 18,
                 "legs": [60, 46], "knees": [-24, -40], "legz": 8},
    "leap_land": {"lean": 40, "drop": 6.0, "head": [8, 0, 0], "jaw": 22, "l": [46, 58], "lz": 24, "r": [46, 58], "rz": 24,
                  "legs": [48, -16], "knees": [-10, -54], "legz": 8},
    # Kristallpanzer: er ballt die Fäuste vor der Brust, brüllt – und Kristalle wachsen über ihn
    "armor_flex": {"lean": -6, "head": [-28, 0, 0], "jaw": 30, "l": [62, 150], "lz": 40, "r": [62, 150], "rz": 40,
                   "legs": [14, -12], "knees": [0, -8], "legz": 8},
    # benommen (wenn der Kristallpanzer zerspringt): er sinkt halb auf ein Knie, der Kopf hängt
    "stagger": {"lean": 24, "drop": 5.0, "head": [30, 0, 10], "jaw": 6, "l": [18, 6], "lz": 12, "r": [26, 10], "rz": 8,
                "legs": [52, -12], "knees": [-8, -56], "legz": 6},
    # Tod: er sinkt auf beide Knie und sackt in sich zusammen
    "death": {"lean": 40, "drop": 9.5, "head": [42, 0, 14], "l": [12, 0], "lz": 18, "r": [16, 4], "rz": 16,
              "legs": [12, 4], "knees": [-84, -88], "legz": 7},
}

# Abläufe: (Tick, Haltung, Übergang dorthin: 0 weich, 1 beschleunigend, 2 abbremsend) --------------------------------
TIMELINES: dict[str, list[tuple[float, str, int]]] = {
    "DORMANT": [(0, "dormant", 0)],
    "AWAKEN": [(0, "dormant", 0), (10, "wake_look", 0), (26, "wake_push", 0), (38, "wake_roar", 2), (56, "wake_roar", 0), (70, "ready", 0)],
    "SMASH": [(0, "ready", 0), (12, "smash_raise", 2), (16, "smash_hit", 1), (26, "smash_hit", 0), (36, "ready", 0)],
    "STOMP": [(0, "ready", 0), (12, "stomp_lift", 2), (16, "stomp_hit", 1), (26, "stomp_hit", 0), (36, "ready", 0)],
    "HURL": [(0, "ready", 0), (10, "boulder_dig", 0), (16, "boulder_dig", 0), (26, "boulder_lift", 2), (32, "boulder_lift", 0),
                (36, "boulder_throw", 1), (44, "boulder_throw", 0), (54, "ready", 0)],
    "CRYSTALS": [(0, "ready", 0), (12, "smash_raise", 2), (16, "crystals_slam", 1), (40, "crystals_slam", 0), (52, "ready", 0)],
    "BEAM": [(0, "ready", 0), (16, "beam_charge", 2), (30, "beam_charge", 0), (34, "beam_fire", 1), (70, "beam_fire", 0), (82, "ready", 0)],
    "LEAP": [(0, "ready", 0), (12, "leap_crouch", 2), (16, "leap_air", 1), (40, "leap_air", 0)],
    "LAND": [(0, "leap_air", 0), (3, "leap_land", 1), (14, "leap_land", 0), (26, "ready", 0)],
    "ARMOR": [(0, "ready", 0), (12, "armor_flex", 2), (40, "armor_flex", 0), (52, "ready", 0)],
    "STAGGER": [(0, "ready", 0), (6, "stagger", 1), (54, "stagger", 0), (64, "ready", 0)],
}


def bones(p: dict) -> dict[str, list[float]]:
    """Zusatzwinkel (Grad) und Verschiebungen (Pixel) je Knochen: [rx, ry, rz, dx, dy, dz]."""
    lean = p.get("lean", 0.0)
    tilt = BODY_TILT + lean
    drop = p.get("drop", 0.0)
    l1, l2 = p.get("l", [0, 14])
    r1, r2 = p.get("r", [0, 14])
    lz = p.get("lz", 0.0)
    rz = p.get("rz", 0.0)
    legs = p.get("legs", [0, 0])
    knees = p.get("knees", [0, 0])
    legz = p.get("legz", 0.0)
    head = p.get("head", [0, 0, 0])
    boulder = p.get("boulder", [0, 0, 0])
    return {
        "body": [lean, p.get("twist", 0.0), 0, 0, drop, 0],
        "head": [head[0], head[1], head[2], 0, 0, 0],
        "jaw": [p.get("jaw", 0.0), 0, 0, 0, 0, 0],
        "left_arm": [-(l1 + tilt), 0, -lz, 0, 0, 0],
        "left_forearm": [(l1 - l2) - FOREARM_BASE, 0, 0, 0, 0, 0],
        "right_arm": [-(r1 + tilt), 0, rz, 0, 0, 0],
        "right_forearm": [(r1 - r2) - FOREARM_BASE, 0, 0, 0, 0, 0],
        "left_leg": [-legs[0], 0, -legz, 0, drop, 0],
        "left_shin": [legs[0] - knees[0], 0, 0, 0, 0, 0],
        "right_leg": [-legs[1], 0, legz, 0, drop, 0],
        "right_shin": [legs[1] - knees[1], 0, 0, 0, 0, 0],
        "boulder": [0, 0, 0, boulder[0], boulder[1], boulder[2]],
    }


def fmt(v: float) -> str:
    s = f"{v:.2f}".rstrip("0").rstrip(".")
    return (s if s not in ("-0", "") else "0") + "f"


def java() -> str:
    names = list(POSES)
    lines = [
        "package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;",
        "",
        "/**",
        " * Haltungen und Abläufe des Steinhüters: je Haltung Zusatzwinkel (Grad) und Verschiebungen (Pixel) der Knochen, je",
        " * Angriff eine Folge von Haltungen an Ticks (dazwischen wird überblendet).",
        " *",
        " * <p>ERZEUGT von tools/stone_keeper_poses.py – nicht von Hand ändern.",
        " */",
        "final class StoneKeeperPoses {",
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
    lines.append("    private StoneKeeperPoses() {}")
    lines.append("}")
    return "\n".join(lines) + "\n"


def preview(target: str, only: list[str] | None = None, yaw: float = 60.0) -> None:
    sys.path.insert(0, str(ROOT / "tools"))
    from entitygen.models import stone_keeper as mod
    from entitygen.paint import paint_model
    from entitygen.preview import render
    from PIL import Image, ImageDraw

    m = mod.build()
    m.pack()
    img, glow = paint_model(m, mod.materials("stone_keeper"), mod.PALETTES["stone_keeper"], seed=100)
    size = 300
    names = only or list(POSES)
    cols = 4
    rows = (len(names) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * size, rows * (size + 20)), (30, 32, 36, 255))
    d = ImageDraw.Draw(sheet)
    hidden = [b for b in m.bones if b in ("armor_chest", "armor_head", "armor_left_arm", "armor_right_arm", "armor_left_leg",
                                          "armor_right_leg")]
    for i, n in enumerate(names):
        b = bones(POSES[n])
        pose = {k: (v[0], v[1], v[2], 1, 1, 1, v[3], v[4], v[5]) for k, v in b.items()}
        for h in hidden:
            pose[h] = (0, 0, 0, 0.001, 0.001, 0.001)
        if "boulder" not in POSES[n] and "boulder" in m.bones:
            pose["boulder"] = (0, 0, 0, 0.001, 0.001, 0.001)
        im = render(m, img, glow, yaw, 10, size, pose)
        x, y = (i % cols) * size, (i // cols) * (size + 20)
        sheet.paste(im, (x, y + 20))
        d.text((x + 6, y + 4), n, fill=(200, 220, 255, 255))
    sheet.save(target)


if __name__ == "__main__":
    if len(sys.argv) > 2 and sys.argv[1] == "--preview":
        only = sys.argv[3].split(",") if len(sys.argv) > 3 and sys.argv[3] else None
        yaw = float(sys.argv[4]) if len(sys.argv) > 4 else 60.0
        preview(sys.argv[2], only, yaw)
    else:
        TARGET.parent.mkdir(parents=True, exist_ok=True)
        TARGET.write_text(java(), encoding="utf-8")
        print(f"{TARGET.name}: {len(POSES)} Haltungen, {len(TIMELINES)} Abläufe")
