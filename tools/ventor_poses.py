#!/usr/bin/env python3
"""
Haltungen und Abläufe von Ventor, dem Gladiator der Winde (1.9) – erzeugt VentorPoses.java.

Jede Haltung gibt je Knochen Zusatzwinkel (Grad) zur Grundhaltung des Modells an (die Arme hängen, der Sturmdreizack zeigt
nach vorn, das Netz hängt aus der Linken), dazu (optional) Verschiebungen in Pixeln. Negatives rx hebt einen Arm nach vorn;
positives rz führt den rechten Arm (bei −x) nach außen, negatives den linken.

Für den Dreizack zählt die Summe der rx von Oberarm, Unterarm und Hand: 0 – er zeigt waagerecht nach vorn, −90 – senkrecht
nach oben, +90 – nach unten.

Aufruf (aus dem Projektordner):
  python3 tools/ventor_poses.py                              # schreibt die Java-Klasse
  python3 tools/ventor_poses.py --preview a.png [n,m] [yaw]  # zeigt Haltungen (mit dem Dunst des Wirbels)
"""
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TARGET = ROOT / "src/main/java/io/github/sirschrolli/kingdomsatwar/client/renderer/arena/VentorPoses.java"

BONES = ["wind", "body", "head", "arm_r", "forearm_r", "hand_r", "arm_l", "forearm_l", "hand_l", "trident", "net", "loin_front",
         "loin_back", "crest_tail"]

# Haltungen: je Knochen [rx, ry, rz, dx, dy, dz] (fehlende = 0) ---------------------------------------------------------
# idle: kampfbereit – der Dreizack schräg nach vorn oben, das Netz an der Hüfte, leicht seitlich
# rise: er formt sich im Wirbel – die Arme vor der Brust gekreuzt, der Kopf gesenkt
# spread: er breitet die Arme aus, den Kopf im Nacken
# salute: der Gruß an die Ränge – der Dreizack hoch über dem Kopf, die Linke als Faust vor der Brust
# gust_wind / gust: die Böe – die Linke holt quer vor dem Leib aus, dann fegt sie weit nach außen
# thrust_back / thrust: der Dreizackstoß – zurücknehmen, dann vorgestoßen (der Leib neigt sich mit)
# net_wind / net_throw: das Netz – über dem Kopf geschwungen, dann nach vorn geworfen
# throw_wind / throw: der Dreizackwurf – über der Schulter, die Linke zielt; dann geschleudert
# point: der Dreizack zielt waagerecht nach vorn (aus den Zinken fliegen die Windkugeln)
# summon: beide Arme hoch, den Kopf im Nacken (die Windhosen, der entfesselte Sturm)
# dive_rise / dive_hover / dive / dive_land: der Sturzflug – hinauf (die Arme zurück wie Schwingen), schweben (der Dreizack
#   über dem Kopf, die Zinken nach unten), hinab, der Aufschlag (geduckt, die Zinken im Boden)
# cyclone: der Zyklon – die Arme weit ausgestreckt (die Drehung gibt das Java-Modell dazu)
# death_sink / death_fall: der Wind erlahmt – er sinkt in sich zusammen, dann stürzt das Standbild vornüber
POSES: dict[str, dict[str, list[float]]] = {
    "idle": {"body": [4, -14, 0], "head": [4, 12, 0], "arm_r": [-24, 0, 14], "forearm_r": [-58, 0, 0], "hand_r": [36, 0, 0],
             "arm_l": [-16, 0, -18], "forearm_l": [-34, 0, 0], "hand_l": [0, 0, 0], "loin_front": [-6, 0, 0], "loin_back": [10, 0, 0],
             "crest_tail": [6, 0, 0]},
    "rise": {"body": [14, 0, 0, 0, 1.0, 0], "head": [30, 0, 0], "arm_r": [-8, 0, 6], "forearm_r": [-10, 0, 0], "hand_r": [96, 0, 0],
             "arm_l": [-6, 0, -6], "forearm_l": [-8, 0, 0], "loin_front": [-4, 0, 0], "loin_back": [6, 0, 0]},
    "spread": {"body": [-8, 0, 0], "head": [-16, 0, 0], "arm_r": [-24, 0, 72], "forearm_r": [-18, 0, 0], "hand_r": [-20, 0, 0],
               "arm_l": [-24, 0, -72], "forearm_l": [-18, 0, 0], "loin_front": [-12, 0, 0], "loin_back": [14, 0, 0],
               "crest_tail": [20, 0, 0]},
    "salute": {"body": [-6, 8, 0], "head": [-14, -6, 0], "arm_r": [-172, 0, 8], "forearm_r": [-6, 0, 0], "hand_r": [86, 0, 0],
               "arm_l": [-42, 0, 26], "forearm_l": [-92, 0, 0], "loin_front": [-8, 0, 0], "loin_back": [10, 0, 0]},
    "gust_wind": {"body": [6, -30, 0], "head": [2, 22, 0], "arm_r": [18, 0, 16], "forearm_r": [-40, 0, 0], "hand_r": [22, 0, 0],
                  "arm_l": [-78, 52, 0], "forearm_l": [-34, 0, 0], "loin_front": [-4, 0, 0], "loin_back": [8, 0, 0]},
    "gust": {"body": [10, 26, 0], "head": [6, -20, 0], "arm_r": [24, 0, 20], "forearm_r": [-36, 0, 0], "hand_r": [12, 0, 0],
             "arm_l": [-86, -64, 0], "forearm_l": [-6, 0, 0], "hand_l": [0, 0, -20], "loin_front": [-14, 0, 0], "loin_back": [16, 0, 0],
             "crest_tail": [22, 0, 0]},
    "thrust_back": {"wind": [6, 0, 0], "body": [-4, 28, 0], "head": [0, -20, 0], "arm_r": [26, 0, 16], "forearm_r": [-62, 0, 0],
                    "hand_r": [36, 0, 0], "arm_l": [-48, 0, -22], "forearm_l": [-30, 0, 0], "loin_front": [-4, 0, 0]},
    "thrust": {"wind": [-12, 0, 0], "body": [18, -24, 0, 0, 0, -1.5], "head": [-14, 18, 0], "arm_r": [-82, 0, 6], "forearm_r": [-6, 0, 0],
               "hand_r": [88, 0, 0], "arm_l": [30, 0, -26], "forearm_l": [-20, 0, 0], "loin_front": [-16, 0, 0], "loin_back": [20, 0, 0],
               "crest_tail": [26, 0, 0]},
    "net_wind": {"body": [-8, 18, 0], "head": [-10, -10, 0], "arm_r": [14, 0, 16], "forearm_r": [-44, 0, 0], "hand_r": [30, 0, 0],
                 "arm_l": [-164, 0, -24], "forearm_l": [-18, 0, 0], "net": [30, 0, 0], "loin_back": [8, 0, 0]},
    "net_throw": {"body": [12, -22, 0], "head": [-4, 14, 0], "arm_r": [20, 0, 16], "forearm_r": [-40, 0, 0], "hand_r": [20, 0, 0],
                  "arm_l": [-78, 0, -8], "forearm_l": [-8, 0, 0], "loin_front": [-12, 0, 0], "loin_back": [14, 0, 0]},
    "throw_wind": {"body": [-10, 32, 0], "head": [0, -26, 0], "arm_r": [-158, 0, 18], "forearm_r": [58, 0, 0], "hand_r": [90, 0, 0],
                   "arm_l": [-84, 0, -14], "forearm_l": [-6, 0, 0], "loin_back": [8, 0, 0]},
    "throw": {"wind": [-8, 0, 0], "body": [16, -26, 0], "head": [-10, 20, 0], "arm_r": [-70, 0, 10], "forearm_r": [-8, 0, 0],
              "hand_r": [70, 0, 0], "arm_l": [24, 0, -22], "forearm_l": [-20, 0, 0], "loin_front": [-12, 0, 0], "loin_back": [16, 0, 0],
              "crest_tail": [20, 0, 0]},
    "point": {"body": [2, -14, 0], "head": [0, 14, 0], "arm_r": [-88, 0, 6], "forearm_r": [-4, 0, 0], "hand_r": [92, 0, 0],
              "arm_l": [-22, 0, -24], "forearm_l": [-30, 0, 0], "loin_front": [-6, 0, 0]},
    "summon": {"body": [-10, 0, 0], "head": [-26, 0, 0], "arm_r": [-164, 0, 24], "forearm_r": [-8, 0, 0], "hand_r": [82, 0, 0],
               "arm_l": [-164, 0, -24], "forearm_l": [-8, 0, 0], "net": [10, 0, 0], "loin_front": [-14, 0, 0], "loin_back": [16, 0, 0],
               "crest_tail": [28, 0, 0]},
    "dive_rise": {"wind": [18, 0, 0], "body": [30, 0, 0], "head": [-26, 0, 0], "arm_r": [48, 0, 28], "forearm_r": [-10, 0, 0],
                  "hand_r": [-30, 0, 0], "arm_l": [48, 0, -28], "forearm_l": [-10, 0, 0], "loin_front": [-30, 0, 0],
                  "loin_back": [30, 0, 0], "crest_tail": [40, 0, 0]},
    "dive_hover": {"body": [-14, 0, 0], "head": [10, 0, 0], "arm_r": [-160, 0, 16], "forearm_r": [-10, 0, 0], "hand_r": [80, 0, 0],
                   "arm_l": [-28, 0, -58], "forearm_l": [-20, 0, 0], "loin_front": [-8, 0, 0], "loin_back": [8, 0, 0]},
    "dive": {"wind": [-20, 0, 0], "body": [34, 0, 0], "head": [-30, 0, 0], "arm_r": [-46, 0, 8], "forearm_r": [-10, 0, 0],
             "hand_r": [146, 0, 0], "arm_l": [24, 0, -40], "forearm_l": [-10, 0, 0], "loin_front": [-30, 0, 0], "loin_back": [34, 0, 0],
             "crest_tail": [-30, 0, 0]},
    "dive_land": {"body": [40, 0, 0, 0, 3.0, 0], "head": [-28, 0, 0], "arm_r": [-34, 0, 10], "forearm_r": [-16, 0, 0],
                  "hand_r": [110, 0, 0], "arm_l": [10, 0, -34], "forearm_l": [-24, 0, 0], "loin_front": [-34, 0, 0],
                  "loin_back": [10, 0, 0]},
    "cyclone": {"body": [6, 0, 0], "head": [-4, 0, 0], "arm_r": [-8, 0, 88], "forearm_r": [-6, 0, 0], "hand_r": [0, 0, 0],
                "arm_l": [-8, 0, -88], "forearm_l": [-6, 0, 0], "net": [0, 0, -30], "loin_front": [-20, 0, 0], "loin_back": [20, 0, 0],
                "crest_tail": [36, 0, 0]},
    "death_sink": {"body": [24, 0, 0, 0, 9.0, 0], "head": [26, 0, 0], "arm_r": [8, 0, 10], "forearm_r": [-10, 0, 0],
                   "arm_l": [8, 0, -10], "forearm_l": [-10, 0, 0], "loin_front": [10, 0, 0]},
    "death_fall": {"body": [82, 0, 0, 0, 18.0, -6.0], "head": [-10, 0, 0], "arm_r": [-150, 0, 20], "forearm_r": [-10, 0, 0],
                   "hand_r": [60, 0, 0], "arm_l": [-150, 0, -20], "forearm_l": [-10, 0, 0], "loin_front": [20, 0, 0]},
}

# Abläufe: (Tick, Haltung, Übergang dorthin: 0 weich, 1 beschleunigend, 2 abbremsend) ----------------------------------
TIMELINES: dict[str, list[tuple[float, str, int]]] = {
    "AWAKEN": [(0, "rise", 0), (30, "rise", 0), (42, "spread", 2), (52, "spread", 0), (60, "salute", 2), (72, "salute", 0),
               (80, "idle", 0)],
    "RAGE": [(0, "idle", 0), (8, "summon", 2), (34, "summon", 0), (40, "idle", 0)],
    "GUST": [(0, "idle", 0), (10, "gust_wind", 2), (14, "gust", 1), (22, "gust", 0), (30, "idle", 0)],
    "THRUST": [(0, "idle", 0), (9, "thrust_back", 2), (12, "thrust", 1), (18, "thrust", 0), (26, "idle", 0)],
    "NET_THROW": [(0, "idle", 0), (12, "net_wind", 2), (18, "net_wind", 0), (21, "net_throw", 1), (28, "net_throw", 0), (36, "idle", 0)],
    "THROW": [(0, "idle", 0), (14, "throw_wind", 2), (18, "throw", 1), (26, "throw", 0), (34, "idle", 0)],
    "CHARGES": [(0, "idle", 0), (10, "point", 2), (32, "point", 0), (40, "idle", 0)],
    "VORTEX": [(0, "idle", 0), (14, "summon", 2), (40, "summon", 0), (50, "idle", 0)],
    "DIVE": [(0, "idle", 0), (10, "dive_rise", 2), (26, "dive_hover", 2), (42, "dive_hover", 0), (48, "dive", 1), (52, "dive_land", 1),
             (64, "dive_land", 0), (72, "idle", 0)],
    "CYCLONE": [(0, "idle", 0), (10, "cyclone", 2), (62, "cyclone", 0), (70, "idle", 0)],
    "RETREAT": [(0, "idle", 0), (12, "rise", 2), (40, "rise", 0)],
    "DEATH": [(0, "idle", 0), (24, "death_sink", 2), (40, "death_sink", 0), (52, "death_fall", 1)],
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
        "package io.github.sirschrolli.kingdomsatwar.client.renderer.arena;",
        "",
        "/**",
        " * Haltungen und Abläufe von Ventor, dem Gladiator der Winde: je Haltung Zusatzwinkel (Grad) und Verschiebungen (Pixel) der",
        " * Knochen, je Angriff eine Folge von Haltungen an Ticks (dazwischen wird überblendet).",
        " *",
        " * <p>ERZEUGT von tools/ventor_poses.py – nicht von Hand ändern.",
        " */",
        "final class VentorPoses {",
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
    lines.append("    private VentorPoses() {}")
    lines.append("}")
    return "\n".join(lines) + "\n"


def combo_model():
    """Leib und Wind-Ebene in einem Modell (für die Vorschau; das Windbild liegt unten im Atlas)."""
    sys.path.insert(0, str(ROOT / "tools"))
    from entitygen.geometry import Model
    from entitygen.models import ventor as mod
    from entitygen.models import ventor_wind as wmod
    from entitygen.paint import paint_model
    from PIL import Image

    m = mod.build()
    m.pack()
    img, glow = paint_model(m, mod.materials("ventor"), mod.PALETTES["ventor"], seed=100)
    w = wmod.build()
    w.pack()
    wimg, _ = wmod.full_texture()
    combo = Model("combo", 128, 256)
    combo.tex_res = 2

    def copy_bones(src, parent):
        for ch in src.children:
            combo.bone(ch.name, parent, pivot=ch.pivot, rotation=ch.rotation)
            copy_bones(ch, ch.name)

    copy_bones(m.root, "root")
    atlas = Image.new("RGBA", (256, 512), (0, 0, 0, 0))
    atlas.paste(img, (0, 0))
    gl = Image.new("RGBA", (256, 512), (0, 0, 0, 0))
    gl.paste(glow, (0, 0))
    atlas.paste(wimg.resize((256, 256), Image.NEAREST), (0, 256))
    for c in m.all_cubes():
        combo.cube(c.bone.name, c.origin, c.size, c.material, c.inflate, c.tag, c.faces).uv = c.uv
    for c in w.all_cubes():
        combo.cube(c.bone.name, c.origin, c.size, c.material, c.inflate, c.tag, c.faces).uv = (c.uv[0], c.uv[1] + 128)
    return combo, atlas, gl


def preview(target: str, names: list[str] | None = None, yaw: float = -30, pitch: float = 8, size: int = 300) -> None:
    from entitygen.preview import render
    from PIL import Image, ImageDraw

    m, img, glow = combo_model()
    names = names or list(POSES)
    cols = min(5, len(names))
    rows = (len(names) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * size, rows * (size + 20)), (30, 32, 36, 255))
    d = ImageDraw.Draw(sheet)
    for i, n in enumerate(names):
        b = bones(POSES[n])
        pose = {k: (v[0], v[1], v[2], 1, 1, 1, v[3], v[4], v[5]) for k, v in b.items()}
        im = render(m, img, glow, yaw, pitch, size, pose)
        x, y = (i % cols) * size, (i // cols) * (size + 20)
        sheet.paste(im, (x, y + 20))
        d.text((x + 6, y + 4), n, fill=(240, 220, 120, 255))
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
