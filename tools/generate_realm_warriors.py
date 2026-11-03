#!/usr/bin/env python3
"""
Texturen der Krieger der vier fremden Reiche (gleiches Schema wie die Soldaten, siehe tools/generate_soldiers.py):
Fußknecht, Bogenschütze, Armbrustschütze, Ritter, Hauptmann und der Herrscher – jeweils im Wappenrock ihres Reichs
(Feldfarbe), mit Besätzen in der Farbe des Wappenzeichens und dem Zeichen selbst auf der Brust.

  * entity/realm/<reich>/footman_1..3, archer_1..3, crossbowman_1..2, knight_1..2, captain_1, lord_1

Der Herrscher trägt eine Krone, einen Hermelinkragen und seinen eigenen Umhang; jedes Reich hat sein eigenes Gesicht
(König Harald der Graue, Herzogin Morwen, Fürst Kasimir, Graf Ansgar).

Aufruf: python3 tools/generate_realm_warriors.py [--preview]
"""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
import generate_soldiers as gs  # noqa: E402
from pixelart import hexc  # noqa: E402

TEX = gs.TEX / "realm"
pal = gs.pal

PURPLE = pal("#1e0f2e", "#2e1846", "#40225f", "#55307a", "#6c4396", "#8a5cb4")
ORANGE = pal("#4a1f05", "#6e320a", "#934710", "#b85e18", "#d47a26", "#e89a44")
GREEN = pal("#0f2414", "#17361d", "#204a27", "#2b6233", "#3a7c42", "#509855")
CRIMSON = pal("#3a0a0e", "#601217", "#8a1c20", "#b0282a", "#cf4438", "#e8735c")
SILVER = pal("#5a5f68", "#7e848e", "#a7adb6", "#cdd2d8", "#eef1f4")
IRON = pal("#0e0e10", "#1b1b1f", "#2a2a30", "#3c3c44", "#55555f")
GOLD = gs.GOLD

# Zeichen auf der Brust (6 × 6 auf dem Wappenrock, '#' in der Farbe des Wappenzeichens)
MARKS = {
    "tower": ["#.##.#", "######", ".####.", ".#..#.", ".####.", "######"],
    "raven": [".##...", "####..", ".####.", "..####", "...###", "...#.#"],
    "sun": ["#.##.#", ".####.", "######", "######", ".####.", "#.##.#"],
    "swan": ["#.....", ".#....", ".#..##", ".#####", "..####", "......"],
}

REALMS = {
    "eisenmark": {"tabard": PURPLE, "trim": SILVER, "mark": "tower", "charge": hexc("#eef1f4"),
                  "lord": ("light", "gray", "gray", "fullbeard", False)},
    "rabenstein": {"tabard": CRIMSON, "trim": IRON, "mark": "raven", "charge": hexc("#141418"),
                   "lord": ("light", "black", "green", "woman", True)},
    "dornfels": {"tabard": ORANGE, "trim": GOLD, "mark": "sun", "charge": hexc("#f7dc80"),
                 "lord": ("tan", "black", "brown", "mustache", False)},
    "silberau": {"tabard": GREEN, "trim": SILVER, "mark": "swan", "charge": hexc("#f4f6f8"),
                 "lord": ("light", "blond", "blue", "beard", False)},
}

FOOTMEN = [("light", "brown", "blue", "beard", False), ("tan", "black", "brown", "stubble", False), ("medium", "red", "green", "mustache", False)]
ARCHERS = [("medium", "blond", "blue", "plain", False), ("dark", "black", "brown", "plain", False), ("light", "chestnut", "green", "freckles", True)]
CROSSBOWMEN = [("light", "brown", "gray", "stubble", False), ("medium", "black", "hazel", "mustache", False)]
CAPTAINS = [("light", "brown", "blue", "fullbeard")]


class Colors:
    """Setzt die Farben eines Reichs in die Soldaten-Maler ein (und danach wieder zurück)."""

    def __init__(self, realm: dict):
        self.realm = realm

    def __enter__(self):
        self.saved = gs.BLUE, gs.GOLD, gs.RED, gs.FOREST
        gs.BLUE = self.realm["tabard"]
        gs.GOLD = self.realm["trim"]
        gs.RED = self.realm["tabard"]
        # die Kapuzen der Schützen in der Farbe des Reichs
        gs.FOREST = self.realm["tabard"]
        return self

    def __exit__(self, *exc):
        gs.BLUE, gs.GOLD, gs.RED, gs.FOREST = self.saved


def stamp(s: gs.Skin, realm: dict, rows: tuple[int, int] = (0, 6)) -> None:
    """Das Zeichen des Reichs auf Brust (und Rücken) des Wappenrocks; darunter einfarbiger Stoff."""
    tabard = realm["tabard"]
    mark = MARKS[realm["mark"]]
    for part in ("body", "jacket"):
        for face in ("front", "back"):
            x0, y0, w, h = gs.face_rect(part, face)
            for y in range(rows[0], rows[1] + 1):
                for x in range(1, w - 1):
                    if s.px[x0 + x, y0 + y][3] == 0:
                        continue
                    s.set(x0 + x, y0 + y, tabard[3] if (x + y) % 5 else tabard[2])
            for y, row in enumerate(mark):
                for x, ch in enumerate(row):
                    if ch == "#":
                        s.set(x0 + 1 + x, y0 + rows[0] + y, realm["charge"])


def footman(realm: dict, args) -> gs.Skin:
    with Colors(realm):
        s = gs.paint_swordsman(*args)
    stamp(s, realm)
    return s


def archer(realm: dict, args) -> gs.Skin:
    with Colors(realm):
        s = gs.paint_archer(*args)
    return s


def crossbowman(realm: dict, args) -> gs.Skin:
    with Colors(realm):
        s = gs.paint_swordsman(*args)
        # Eisenhut statt Normannenhelm: breite Krempe, kein Nasenschutz
        t = gs.tabard_codes()
        s.art("hat", "front", ["bcdeedcb", "abccccba", "        ", "        ", "        ", "        ", "        ", "        "], t)
        brim = ["cddeeddc", "abbccbba", "        ", "        ", "        ", "        ", "        ", "        "]
        for face in ("right", "left", "back"):
            s.art("hat", face, brim, t)
    stamp(s, realm)
    return s


def knight(realm: dict, variant: int) -> gs.Skin:
    with Colors(realm):
        s = gs.paint_knight(variant)
    stamp(s, realm)
    return s


def captain(realm: dict, args) -> gs.Skin:
    with Colors(realm):
        s = gs.paint_captain(*args)
    return s


def lord(realm: dict) -> gs.Skin:
    skin_name, hair_name, iris, face, feminine = realm["lord"]
    with Colors(realm):
        s = gs.paint_captain(skin_name, hair_name, iris, face if face != "woman" else "plain")
        skin, hair = gs.SKINS[skin_name], gs.HAIRS[hair_name]
        gs.paint_head(s, skin, hair, gs.IRISES[iris], face, long_hair=feminine)
        # Krone statt Helm: goldener Reif mit Zacken und einem roten Stein, sonst frei
        crown = {"G": GOLD[3], "g": GOLD[2], "y": GOLD[4], "o": GOLD[1], "r": hexc("#b0202a"), "R": hexc("#e04050"),
                 "b": hexc("#2a3fa8")}
        s.clear("hat")
        s.art("hat", "front", ["y.G..G.y", "GGgRrgGG", "oggggggo"], crown)
        for side in ("right", "left", "back"):
            s.art("hat", side, ["y..G...y", "GGgbggGG", "oggggggo"], crown)
        if feminine:
            # lange Haare über Schultern und Rücken
            codes = gs.face_codes(skin, hair, gs.IRISES[iris])
            s.art("head", "back", ["cccddccc", "ccdccdcc", "cccccccc", "bccccccb", "bcbccbcb", "bbbccbbb", "abbbbbba", "abbaabba"],
                  codes)
            for part in ("body", "jacket"):
                s.art(part, "back", ["cbbbbbbc", "bcbbbbcb", "abccccba", ".abbbba."], codes)
        # Hermelinkragen
        ermine = {"w": hexc("#f2f0ea"), "W": hexc("#d8d4ca"), "k": hexc("#16141a")}
        for part in ("body", "jacket"):
            s.art(part, "front", ["wWwkwWkw", "WkwWwWwW"], ermine)
            s.art(part, "back", ["wWkwWwkW"], ermine)
            s.art(part, "top", ["wwWwwkww", "WkwwWwwW", "wwWkwwWw", "kwwWwwkw"], ermine)
    # Zeichen des Reichs auf dem Umhang
    x0, y0, w, h = gs.face_rect("cape", "back")
    for y, row in enumerate(MARKS[realm["mark"]]):
        for x, ch in enumerate(row):
            if ch == "#":
                s.set(x0 + 2 + x, y0 + 4 + y, realm["charge"])
    return s


def all_skins() -> dict[str, Image.Image]:
    out: dict[str, Image.Image] = {}
    for rid, realm in REALMS.items():
        for i, args in enumerate(FOOTMEN):
            out[f"{rid}/footman_{i + 1}"] = footman(realm, args).img
        for i, args in enumerate(ARCHERS):
            out[f"{rid}/archer_{i + 1}"] = archer(realm, args).img
        for i, args in enumerate(CROSSBOWMEN):
            out[f"{rid}/crossbowman_{i + 1}"] = crossbowman(realm, args).img
        for i in range(2):
            out[f"{rid}/knight_{i + 1}"] = knight(realm, i).img
        for i, args in enumerate(CAPTAINS):
            out[f"{rid}/captain_{i + 1}"] = captain(realm, args).img
        out[f"{rid}/lord_1"] = lord(realm).img
    return out


def main() -> None:
    skins = all_skins()
    for key, img in skins.items():
        path = TEX / f"{key}.png"
        path.parent.mkdir(parents=True, exist_ok=True)
        img.save(path)
    print(f"{len(skins)} Texturen geschrieben nach {TEX}")
    if "--preview" in sys.argv:
        scale = 6
        keys = list(skins)
        cols = 12
        rows = (len(keys) + cols - 1) // cols
        sheet = Image.new("RGBA", (cols * 18 * scale, rows * 2 * 34 * scale), (90, 110, 130, 255))
        for i, key in enumerate(keys):
            c, r = i % cols, i // cols
            for j, back in enumerate((False, True)):
                v = gs.front_view(skins[key], back).resize((16 * scale, 32 * scale), Image.NEAREST)
                sheet.alpha_composite(v, (c * 18 * scale + scale, (r * 2 + j) * 34 * scale + scale))
        sheet.save(gs.SCRATCH / "realm_warriors_preview.png")
        print("Vorschau:", gs.SCRATCH / "realm_warriors_preview.png")


if __name__ == "__main__":
    main()
