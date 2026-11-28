#!/usr/bin/env python3
"""
Haltungen und Abläufe von Kuno von Rabenfels, dem Raubritter (1.8) – erzeugt RobberKnightPoses.java.

Jede Haltung gibt je Knochen Zusatzwinkel (Grad) zur Grundhaltung des Modells an (der Reiter steht, die Arme hängen, Lanze
und Morgenstern zeigen nach vorn), dazu (optional) Verschiebungen in Pixeln. Negatives rx hebt einen Arm oder ein Bein nach
vorn; positives rz führt den rechten Arm (bei −x) nach außen, negatives den linken.

Zu Ross sitzt der Reiter im Sattel: Das Java-Modell setzt ihn dorthin, wohin der Sattel gerade zeigt (auch wenn das Ross
steigt) – die Haltungen „ride_*“ geben nur seine Glieder an und am Ross dessen Drehung (das Steigen um die Hinterhand).
Zu Fuß („foot_*“) gibt der Knochen „knight“ die Verschiebung von der Sattelhöhe auf den Boden an (dy = FOOT_DY).

Aufruf (aus dem Projektordner):
  python3 tools/robber_knight_poses.py                       # schreibt die Java-Klasse
  python3 tools/robber_knight_poses.py --preview a.png [n,m] [yaw]   # zeigt Haltungen
"""
from __future__ import annotations

import math
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TARGET = ROOT / "src/main/java/io/github/sirschrolli/kingdomsatwar/client/renderer/castle/RobberKnightPoses.java"

BONES = ["knight", "torso", "helm", "arm_r", "forearm_r", "hand_r", "arm_l", "forearm_l", "hand_l", "thigh_r", "shin_r", "thigh_l",
         "shin_l", "lance", "mace", "shield", "tabard", "tabard_back", "cape", "horse", "neck", "head", "leg_fl", "shin_fl", "leg_fr",
         "shin_fr", "leg_hl", "shin_hl", "leg_hr", "shin_hr", "tail"]

#: zu Fuß: so weit liegt die Hüfte tiefer als im Sattel (Pixel)
FOOT_DY = 14.7
#: Drehpunkt des Rosses und Sitz des Reiters (Modellkoordinaten)
HORSE_PIVOT = (0.0, 5.0, 0.0)
SEAT = (0.0, -3.0, 0.6)

# Die Beine im Sattel: die Schenkel umfassen den Rumpf des Rosses, die Unterschenkel hängen an seinen Flanken
_SADDLE_LEGS = {"thigh_r": [-46, 0, 26, -3.0, 0, 0], "thigh_l": [-46, 0, -26, 3.0, 0, 0], "shin_r": [52, 0, -20],
                "shin_l": [52, 0, 20], "tabard": [-64, 0, 0], "tabard_back": [42, 0, 0], "cape": [34, 0, 0]}


def _ride(**bones) -> dict:
    out = {k: list(v) for k, v in _SADDLE_LEGS.items()}
    out.update({k: list(v) for k, v in bones.items()})
    return out


def _foot(**bones) -> dict:
    out = {"knight": [0, 0, 0, 0, FOOT_DY, 0], "cape": [6, 0, 0]}
    for k, v in bones.items():
        if k == "knight":
            v = list(v) + [0] * (6 - len(v))
            v[4] += FOOT_DY
        out[k] = list(v)
    return out


# Haltungen: je Knochen [rx, ry, rz, dx, dy, dz] (fehlende = 0) ---------------------------------------------------------
# ride_guard: er hält Hof – die Lanze aufrecht, der Schild an der Flanke, das Ross steht
# ride_ready: kampfbereit – die Lanze eingelegt (waagerecht nach vorn, ein wenig nach links über den Hals des Rosses)
# ride_charge: der Ansturm – tief über den Hals gebeugt, die Lanze gesenkt, der Schild vor der Brust
# ride_thrust_back / ride_thrust: der Lanzenstich – zurückziehen, zustoßen
# ride_rear: das Ross steigt (die Vorderhand hoch, die Vorderbeine angewinkelt), er reckt die Lanze empor
# ride_stomp: das Ross schlägt mit der Vorderhand auf
# ride_horn: das Horn an den Helm (die Lanze aufrecht in der Rechten)
# ride_point: er weist mit der Lanze auf das Ziel (die Armbrustschützen schießen)
# ride_stagger: die Lanze ist am Schild gebrochen – er wankt im Sattel
# ride_fall: das Ross wirft ihn ab: er stürzt rücklings
# foot_ground: er liegt auf dem Rücken; foot_kneel: er rafft sich auf (ein Knie am Boden)
# foot_ready: zu Fuß – der Schild vorn, der Morgenstern an der Schulter
# foot_smash_wind / foot_smash: der Morgensternschlag – hoch über den Kopf, dann auf den Boden vor ihm
# foot_whirl: der Wirbel – die Arme weit, er dreht sich (die Drehung gibt das Java-Modell dazu)
# foot_bash_crouch / foot_bash: der Schildstoß – geduckt hinter dem Schild, dann vorgestoßen
# foot_wall: der Schildwall – der Schild hoch vor dem Leib, geduckt, der Morgenstern zurückgenommen
# foot_horn, foot_point: wie zu Ross
# death: er sinkt in die Knie, der Morgenstern fällt
POSES: dict[str, dict[str, list[float]]] = {
    "ride_guard": _ride(torso=[2, 0, 0], arm_r=[-8, 0, 10], forearm_r=[-82, 0, 0], hand_r=[0, 0, 0], arm_l=[-20, 0, -16],
                        forearm_l=[-48, 0, 18], helm=[0, -8, 0], neck=[-4, 0, 0], head=[6, 0, 0]),
    "ride_ready": _ride(torso=[4, -8, 0], arm_r=[-18, 0, 14], forearm_r=[-42, 0, 0], hand_r=[58, 0, 0], lance=[0, 0, 0, 0, 0, 0],
                        arm_l=[-42, 0, -10], forearm_l=[-50, 0, 30], helm=[4, 6, 0], neck=[-8, 0, 0], head=[8, 0, 0]),
    "ride_charge": _ride(torso=[22, -10, 0], arm_r=[-12, 0, 16], forearm_r=[-48, 0, 0], hand_r=[40, 0, -10], arm_l=[-55, 0, -6],
                         forearm_l=[-40, 0, 34], helm=[16, 8, 0], neck=[-22, 0, 0], head=[12, 0, 0], cape=[46, 0, 0],
                         tabard_back=[60, 0, 0]),
    "ride_thrust_back": _ride(torso=[-4, 28, 0], arm_r=[24, 0, 22], forearm_r=[-60, 0, 0], hand_r=[42, 0, 0], arm_l=[-40, 0, -12],
                              forearm_l=[-50, 0, 30], helm=[2, -12, 0]),
    "ride_thrust": _ride(torso=[12, -26, 0], arm_r=[-60, 0, 8], forearm_r=[-14, 0, 0], hand_r=[74, 0, 0], arm_l=[-30, 0, -20],
                         forearm_l=[-40, 0, 30], helm=[8, 14, 0], neck=[-10, 0, 0]),
    "ride_rear": _ride(torso=[24, 0, 0], arm_r=[-70, 0, 18], forearm_r=[-10, 0, 0], hand_r=[2, 0, 0], arm_l=[-60, 0, -30],
                       forearm_l=[-40, 0, 30], helm=[-10, 0, 0], horse=[-36, 0, 0, 0, -4.5, 3.0], neck=[-30, 0, 0], head=[-8, 0, 0],
                       leg_fl=[-70, 0, 0], shin_fl=[96, 0, 0], leg_fr=[-52, 0, 0], shin_fr=[84, 0, 0], leg_hl=[30, 0, 0],
                       shin_hl=[10, 0, 0], leg_hr=[34, 0, 0], shin_hr=[6, 0, 0], tail=[30, 0, 0], cape=[10, 0, 0]),
    "ride_stomp": _ride(torso=[-8, 0, 0], arm_r=[-30, 0, 14], forearm_r=[-60, 0, 0], arm_l=[-40, 0, -14], forearm_l=[-50, 0, 24],
                        horse=[6, 0, 0, 0, 0.6, 0], neck=[6, 0, 0], head=[14, 0, 0], leg_fl=[-22, 0, 0], shin_fl=[8, 0, 0],
                        leg_fr=[-14, 0, 0], shin_fr=[4, 0, 0], leg_hl=[-8, 0, 0], leg_hr=[-6, 0, 0], tail=[-10, 0, 0]),
    "ride_horn": _ride(torso=[-6, 10, 0], arm_r=[-8, 0, 12], forearm_r=[-82, 0, 0], arm_l=[-128, 0, 12], forearm_l=[-62, 0, 0],
                       hand_l=[0, 0, 0], helm=[-14, 0, 0], neck=[-14, 0, 0], head=[-6, 0, 0]),
    "ride_point": _ride(torso=[-8, -16, 0], arm_r=[-120, 0, 6], forearm_r=[-10, 0, 0], hand_r=[93, 0, 0], arm_l=[-24, 0, -18],
                        forearm_l=[-46, 0, 22], helm=[-4, 12, 0]),
    "ride_stagger": _ride(torso=[-24, 0, 8], arm_r=[-40, 0, 48], forearm_r=[-30, 0, 0], arm_l=[-30, 0, -50], forearm_l=[-30, 0, 20],
                          helm=[18, 0, -10], neck=[-24, 0, 0], head=[-10, 0, 0], horse=[-8, 0, 0], tail=[18, 0, 0]),
    "ride_fall": {"knight": [-74, 0, 10, 0, 6.0, 9.0], "torso": [10, 0, 0], "arm_r": [-160, 0, 30], "forearm_r": [-20, 0, 0],
                  "arm_l": [-150, 0, -30], "forearm_l": [-20, 0, 0], "thigh_r": [-80, 0, 16], "thigh_l": [-70, 0, -16],
                  "shin_r": [40, 0, 0], "shin_l": [30, 0, 0], "helm": [-20, 0, 0], "cape": [-40, 0, 0],
                  "horse": [-30, 0, 0, 0, -3.5, 2.5], "neck": [-26, 0, 0], "leg_fl": [-60, 0, 0], "shin_fl": [90, 0, 0],
                  "leg_fr": [-48, 0, 0], "shin_fr": [80, 0, 0], "leg_hl": [26, 0, 0], "leg_hr": [30, 0, 0]},
    "foot_ground": _foot(knight=[-90, 0, 0, 0, 9.6, 6.0], arm_r=[-150, 0, 30], forearm_r=[-10, 0, 0], arm_l=[-160, 0, -36],
                         forearm_l=[-10, 0, 0], thigh_r=[-10, 0, 8], thigh_l=[-4, 0, -10], shin_r=[20, 0, 0], shin_l=[8, 0, 0],
                         helm=[-30, 0, 0], cape=[0, 0, 0]),
    "foot_kneel": _foot(knight=[0, 0, 0, 0, 4.8, 0], torso=[20, 0, 0], thigh_r=[-88, 0, 0], shin_r=[88, 0, 0], thigh_l=[-2, 0, 0],
                        shin_l=[86, 0, 0], arm_r=[-30, 0, 20], forearm_r=[-20, 0, 0], arm_l=[-40, 0, -10],
                        forearm_l=[-40, 0, 20], helm=[16, 0, 0]),
    "foot_ready": _foot(torso=[6, -12, 0], arm_r=[-58, 0, 26], forearm_r=[-62, 0, 0], hand_r=[-30, 0, 0], arm_l=[-46, 0, -6],
                        forearm_l=[-52, 0, 34], thigh_r=[-14, 0, 6], shin_r=[16, 0, 0], thigh_l=[12, 0, -6], shin_l=[6, 0, 0],
                        helm=[2, 10, 0]),
    "foot_smash_wind": _foot(torso=[-14, 6, 0], arm_r=[-176, 0, 6], forearm_r=[-36, 0, 0], hand_r=[-20, 0, 0], arm_l=[-24, 0, -20],
                             forearm_l=[-30, 0, 30], thigh_r=[-18, 0, 6], shin_r=[18, 0, 0], thigh_l=[16, 0, -6], helm=[-10, 0, 0],
                             cape=[-8, 0, 0]),
    "foot_smash": _foot(knight=[0, 0, 0, 0, 2.0, 0], torso=[34, -4, 0], arm_r=[-72, 0, 4], forearm_r=[-8, 0, 0], hand_r=[40, 0, 0],
                        arm_l=[-30, 0, -24], forearm_l=[-40, 0, 30], thigh_r=[-46, 0, 6], shin_r=[56, 0, 0], thigh_l=[24, 0, -6],
                        shin_l=[30, 0, 0], helm=[-20, 0, 0], cape=[30, 0, 0]),
    "foot_whirl": _foot(torso=[10, 0, 0], arm_r=[-10, 0, 92], forearm_r=[-10, 0, 0], hand_r=[0, 0, -90], arm_l=[-10, 0, -84],
                        forearm_l=[-20, 0, 0], thigh_r=[-16, 0, 10], shin_r=[20, 0, 0], thigh_l=[10, 0, -10], shin_l=[10, 0, 0],
                        cape=[50, 0, 0]),
    "foot_bash_crouch": _foot(knight=[0, 0, 0, 0, 1.6, 0], torso=[22, 20, 0], arm_l=[-70, 0, 6], forearm_l=[-60, 0, 40],
                              arm_r=[30, 0, 20], forearm_r=[-50, 0, 0], thigh_r=[-34, 0, 6], shin_r=[44, 0, 0], thigh_l=[-6, 0, -6],
                              shin_l=[26, 0, 0], helm=[-14, -12, 0]),
    "foot_bash": _foot(torso=[16, -18, 0], arm_l=[-86, 0, 0], forearm_l=[-24, 0, 50], arm_r=[24, 0, 20], forearm_r=[-50, 0, 0],
                       thigh_r=[18, 0, 6], shin_r=[10, 0, 0], thigh_l=[-42, 0, -6], shin_l=[30, 0, 0], helm=[-8, 10, 0]),
    "foot_wall": _foot(knight=[0, 0, 0, 0, 1.4, 0], torso=[16, 8, 0], arm_l=[-88, 0, 12], forearm_l=[-44, 0, 58],
                       arm_r=[10, 0, 24], forearm_r=[-70, 0, 0], hand_r=[-20, 0, 0], thigh_r=[-28, 0, 8], shin_r=[38, 0, 0],
                       thigh_l=[-4, 0, -8], shin_l=[22, 0, 0], helm=[-10, -6, 0]),
    "foot_horn": _foot(torso=[-4, 10, 0], arm_r=[-30, 0, 20], forearm_r=[-50, 0, 0], arm_l=[-128, 0, 12], forearm_l=[-62, 0, 0],
                       helm=[-14, 0, 0], thigh_r=[-6, 0, 4], thigh_l=[6, 0, -4]),
    "foot_point": _foot(torso=[-4, -14, 0], arm_r=[-130, 0, 8], forearm_r=[-20, 0, 0], hand_r=[109, 0, 0], arm_l=[-40, 0, -14],
                        forearm_l=[-50, 0, 30], helm=[-6, 10, 0]),
    "death": _foot(knight=[0, 0, 0, 0, 5.0, 0], torso=[46, 0, 0], thigh_r=[-90, 0, 0], shin_r=[90, 0, 0], thigh_l=[-84, 0, 0],
                   shin_l=[84, 0, 0], arm_r=[-20, 0, 10], forearm_r=[-10, 0, 0], arm_l=[-10, 0, -20], forearm_l=[-20, 0, 0],
                   helm=[36, 0, 10], cape=[-30, 0, 0]),
}

# Abläufe: (Tick, Haltung, Übergang dorthin: 0 weich, 1 beschleunigend, 2 abbremsend) ----------------------------------
TIMELINES: dict[str, list[tuple[float, str, int]]] = {
    "AWAKEN": [(0, "ride_guard", 0), (14, "ride_guard", 0), (26, "ride_rear", 2), (38, "ride_rear", 0), (44, "ride_stomp", 1),
               (54, "ride_ready", 2), (70, "ride_ready", 0)],
    "CHARGE": [(0, "ride_ready", 0), (12, "ride_charge", 2), (200, "ride_charge", 0)],
    "HALT": [(0, "ride_charge", 0), (6, "ride_rear", 2), (14, "ride_rear", 0), (20, "ride_stomp", 1), (30, "ride_ready", 2)],
    "THRUST": [(0, "ride_ready", 0), (8, "ride_thrust_back", 2), (12, "ride_thrust", 1), (18, "ride_thrust", 0), (26, "ride_ready", 0)],
    "TRAMPLE": [(0, "ride_ready", 0), (12, "ride_rear", 2), (19, "ride_rear", 0), (22, "ride_stomp", 1), (32, "ride_stomp", 0),
                (40, "ride_ready", 0)],
    "HORN_RIDE": [(0, "ride_ready", 0), (8, "ride_horn", 2), (34, "ride_horn", 0), (42, "ride_ready", 0)],
    "VOLLEY_RIDE": [(0, "ride_ready", 0), (10, "ride_point", 2), (40, "ride_point", 0), (50, "ride_ready", 0)],
    "STAGGER": [(0, "ride_stagger", 1), (48, "ride_stagger", 0), (60, "ride_ready", 0)],
    "UNHORSE": [(0, "ride_ready", 0), (10, "ride_rear", 2), (16, "ride_fall", 1), (22, "foot_ground", 1), (36, "foot_ground", 0),
                (46, "foot_kneel", 2), (56, "foot_ready", 2), (64, "foot_ready", 0)],
    "SMASH": [(0, "foot_ready", 0), (12, "foot_smash_wind", 2), (17, "foot_smash_wind", 0), (20, "foot_smash", 1), (30, "foot_smash", 0),
              (38, "foot_ready", 0)],
    "WHIRL": [(0, "foot_ready", 0), (8, "foot_whirl", 2), (40, "foot_whirl", 0), (48, "foot_ready", 0)],
    "BASH": [(0, "foot_ready", 0), (8, "foot_bash_crouch", 2), (12, "foot_bash", 1), (20, "foot_bash", 0), (28, "foot_ready", 0)],
    "WALL": [(0, "foot_ready", 0), (8, "foot_wall", 2), (62, "foot_wall", 0), (70, "foot_ready", 0)],
    "HORN_FOOT": [(0, "foot_ready", 0), (8, "foot_horn", 2), (34, "foot_horn", 0), (42, "foot_ready", 0)],
    "VOLLEY_FOOT": [(0, "foot_ready", 0), (10, "foot_point", 2), (40, "foot_point", 0), (50, "foot_ready", 0)],
}


def bones(p: dict) -> dict[str, list[float]]:
    out = {}
    for b in BONES:
        v = list(p.get(b, []))
        out[b] = (v + [0.0] * 6)[:6]
    return out


def mounted(name: str) -> bool:
    return name.startswith("ride_")


def fmt(v: float) -> str:
    s = f"{v:.2f}".rstrip("0").rstrip(".")
    return (s if s not in ("-0", "") else "0") + "f"


def java() -> str:
    names = list(POSES)
    lines = [
        "package io.github.sirschrolli.kingdomsatwar.client.renderer.castle;",
        "",
        "/**",
        " * Haltungen und Abläufe von Kuno von Rabenfels, dem Raubritter: je Haltung Zusatzwinkel (Grad) und Verschiebungen (Pixel)",
        " * der Knochen, je Angriff eine Folge von Haltungen an Ticks (dazwischen wird überblendet). Zu Ross setzt das Modell den",
        " * Reiter in den Sattel (auch wenn das Ross steigt); zu Fuß verschiebt der Knochen „knight“ ihn auf den Boden.",
        " *",
        " * <p>ERZEUGT von tools/robber_knight_poses.py – nicht von Hand ändern.",
        " */",
        "final class RobberKnightPoses {",
    ]
    for i, b in enumerate(BONES):
        lines.append(f"    static final int {b.upper()} = {i};")
    lines.append(f"    static final int BONES = {len(BONES)};")
    lines.append(f"    static final float FOOT_DY = {fmt(FOOT_DY)};")
    lines.append(f"    static final float HORSE_PIVOT_Y = {fmt(HORSE_PIVOT[1])};")
    lines.append(f"    static final float SEAT_Y = {fmt(SEAT[1])};")
    lines.append(f"    static final float SEAT_Z = {fmt(SEAT[2])};")
    lines.append("")
    for i, n in enumerate(names):
        lines.append(f"    static final int P_{n.upper()} = {i};")
    lines.append("")
    lines.append("    /** Je Haltung: sitzt er im Sattel? */")
    lines.append("    static final boolean[] MOUNTED = {" + ", ".join("true" if mounted(n) else "false" for n in names) + "};")
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
    lines.append("    private RobberKnightPoses() {}")
    lines.append("}")
    return "\n".join(lines) + "\n"


def _seat(pose: dict) -> dict:
    """Wie im Java-Modell: Im Sattel folgt der Reiter dem Ross (Drehung um X am Drehpunkt des Rosses plus dessen
    Verschiebung)."""
    out = {k: list(v) for k, v in pose.items()}
    h = out["horse"]
    a = math.radians(h[0])
    sy, sz = SEAT[1] - HORSE_PIVOT[1], SEAT[2] - HORSE_PIVOT[2]
    ny = sy * math.cos(a) - sz * math.sin(a)
    nz = sy * math.sin(a) + sz * math.cos(a)
    k = out["knight"]
    k[0] += h[0]
    k[3] += h[3]
    k[4] += (ny - sy) + h[4]
    k[5] += (nz - sz) + h[5]
    return out


def preview(target: str, names: list[str] | None = None, yaw: float = -30, pitch: float = 8, size: int = 300) -> None:
    sys.path.insert(0, str(ROOT / "tools"))
    from entitygen.models import robber_knight as mod
    from entitygen.paint import paint_model
    from entitygen.preview import render
    from PIL import Image, ImageDraw

    m = mod.build()
    m.pack()
    img, glow = paint_model(m, mod.materials("robber_knight"), mod.PALETTES["robber_knight"], seed=100)
    names = names or list(POSES)
    cols = min(5, len(names))
    rows = (len(names) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * size, rows * (size + 20)), (30, 32, 36, 255))
    d = ImageDraw.Draw(sheet)
    hide = (0, 0, 0, 0, 0, 0, 0, 0, 0)
    for i, n in enumerate(names):
        b = bones(POSES[n])
        ride = mounted(n) or n == "ride_fall"
        if mounted(n):
            b = _seat(b)
        pose = {k: (v[0], v[1], v[2], 1, 1, 1, v[3], v[4], v[5]) for k, v in b.items()}
        if not ride:
            pose["horse"] = hide
            pose["lance"] = hide
        else:
            pose["mace"] = hide
        pose["horn_hand"] = hide if "horn" not in n else (0, 0, 0, 1, 1, 1, 0, 0, 0)
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
