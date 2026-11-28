#!/usr/bin/env python3
"""Aufbau der Schwingen des Schöpfers aus einzelnen Federn (seit 0.78.2) – die eine Quelle für Textur, Vorschau und Modell.

Ein Flügel hat drei Glieder (Oberarm 7, Unterarm 6, Hand 4 Einheiten lang, jeweils entlang +x des Glieds; die Federn hängen
nach +y, +z ist hinten/oben). An jedem Glied sitzen feste Federn: Schirm- und Armschwingen, darüber auf der Oberseite große
und kleine Deckfedern, auf der Unterseite Unterflügeldecken. Die sieben Handschwingen sitzen an der Hand und fächern sich je
nach Haltung auf (zugeklappt fast gleich gerichtet, im Gleitflug weit gespreizt).

Jede Feder ist eine Fläche ohne Tiefe: Vorderseite (nach -z, die Unterseite des Flügels) aus (u, v), Rückseite (die
Oberseite) gleich daneben aus (u + Breite, v). Nachbarn überlappen wie Dachschindeln: Jede Feder ist um ihre Längsachse
geneigt (Außenkante nach hinten), und nach außen hin liegt jede ein wenig tiefer – so durchdringen sie sich nie und flimmern
nicht.

Aufruf: python3 tools/creator_wings.py   → gibt die Tabellen für CreatorGearLayer (Java) aus.
"""

# Arten der Federn: Breite, Länge, u, v (Einheiten der Modelllage 128×64)
KINDS = {
    "P16": (3, 16, 0, 0),
    "P14": (3, 14, 6, 0),
    "P12": (3, 12, 12, 0),
    "S12": (3, 12, 18, 0),
    "T10": (3, 10, 24, 0),
    "C7": (3, 7, 30, 0),
    "C4": (2, 4, 36, 0),
}
KIND_ORDER = list(KINDS)
# die Arme der Glieder: Länge, Höhe (y), Tiefe (z), u, v – weich befiedert, von Deckfedern fast verdeckt
ARMS = [(7, 2, 2, 0, 20), (6, 2, 2, 0, 24), (4, 1, 1, 0, 28)]
# Lage der Arme im Glied: y von, z von
ARM_AT = [(-1.0, 0.1), (-1.0, 0.1), (-0.5, 0.6)]
TILT = -8.0     # Neigung um die Längsachse (Außenkante nach hinten)


def fixed():
    """Feste Federn: (Glied, x entlang des Knochens, y, z, Art, Drehung z, Drehung y, Drehung x) – Grad, linker Flügel."""
    rows = []
    # Schulterfedern, dicht am Leib
    rows.append((0, 0.3, 0.2, 1.30, "T10", 46.0, TILT, 0.0))
    rows.append((0, 0.9, 0.2, 1.25, "T10", 41.0, TILT, 0.0))
    # Schirmfedern am Oberarm
    for i in range(5):
        rows.append((0, 1.6 + i * 1.2, 0.2, 1.20 - i * 0.05, "T10" if i < 2 else "S12", 36.0 - i * 4.0, TILT, 0.0))
    # Armschwingen am Unterarm
    for i in range(6):
        rows.append((1, 0.4 + i * 1.0, 0.2, 0.95 - i * 0.05, "S12", 4.0 - i * 2.4, TILT, 0.0))
    # Oberseite: große Deckfedern, sie lehnen sich auf die Schwungfedern
    for i in range(5):
        rows.append((0, 0.9 + i * 1.3, -0.3, 2.42 - i * 0.04, "C7", 30.0 - i * 4.0, TILT, -9.0))
    for i in range(6):
        rows.append((1, 0.4 + i * 1.0, -0.3, 2.42 - i * 0.04, "C7", 3.0 - i * 2.0, TILT, -9.0))
    for i in range(4):
        rows.append((2, 0.3 + i * 1.1, -0.2, 1.90 - i * 0.04, "C7", -8.0 - i * 10.0, TILT, -8.0))
    # Oberseite: kleine Deckfedern über dem Arm
    for i in range(5):
        rows.append((0, 0.7 + i * 1.4, -1.0, 2.62 - i * 0.03, "C4", 26.0 - i * 4.0, -6.0, -7.0))
    for i in range(5):
        rows.append((1, 0.5 + i * 1.25, -1.0, 2.62 - i * 0.03, "C4", 2.0 - i * 2.0, -6.0, -7.0))
    for i in range(3):
        rows.append((2, 0.5 + i * 1.4, -0.6, 2.06 - i * 0.03, "C4", -10.0 - i * 10.0, -6.0, -6.0))
    # Unterseite: Unterflügeldecken
    for i in range(4):
        rows.append((0, 1.2 + i * 1.5, -0.2, -0.02 - i * 0.04, "C7", 28.0 - i * 5.0, TILT, 9.0))
    for i in range(5):
        rows.append((1, 0.6 + i * 1.2, -0.2, -0.02 - i * 0.04, "C7", 2.0 - i * 2.0, TILT, 9.0))
    for i in range(3):
        rows.append((2, 0.4 + i * 1.4, -0.1, 0.48 - i * 0.04, "C7", -8.0 - i * 12.0, TILT, 8.0))
    # der Daumenfittich am Handgelenk
    rows.append((2, 0.2, -0.4, 1.72, "C7", -72.0, TILT, 0.0))
    return rows


def primaries():
    """Die Handschwingen: (x entlang der Hand, z, Art, Winkel ausgebreitet in Grad; negativ = zur Flügelspitze hin)."""
    kinds = ["P14", "P16", "P16", "P16", "P16", "P14", "P12"]
    xs = [0.1, 0.75, 1.4, 2.05, 2.7, 3.35, 3.9]
    fans = [-10.0, -21.0, -32.0, -44.0, -56.0, -68.0, -80.0]
    return [(xs[i], 1.05 - i * 0.05, kinds[i], fans[i]) for i in range(7)]


def java():
    out = []
    out.append("    private static final float[][] KINDS = {")
    out.append("            " + ", ".join("{%d, %d, %d, %d}" % KINDS[k] for k in KIND_ORDER))
    out.append("    };")
    out.append("    private static final float[][] FIXED = {")
    rows = fixed()
    line = "            "
    for r in rows:
        cell = "{%d, %sf, %sf, %sf, %d, %sf, %sf, %sf}, " % (r[0], fmt(r[1]), fmt(r[2]), fmt(r[3]), KIND_ORDER.index(r[4]), fmt(r[5]),
                                                               fmt(r[6]), fmt(r[7]))
        if len(line) + len(cell) > 132:
            out.append(line.rstrip())
            line = "            "
        line += cell
    out.append(line.rstrip().rstrip(","))
    out.append("    };")
    out.append("    private static final float[][] PRIMARIES = {")
    out.append("            " + ", ".join("{%sf, %sf, %d, %sf}" % (fmt(p[0]), fmt(p[1]), KIND_ORDER.index(p[2]), fmt(p[3])) for p in primaries()))
    out.append("    };")
    return "\n".join(out)


def fmt(v):
    s = ("%.2f" % v).rstrip("0").rstrip(".")
    return s if s not in ("-0", "") else "0"


if __name__ == "__main__":
    print(java())
