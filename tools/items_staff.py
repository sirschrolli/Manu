#!/usr/bin/env python3
"""
Aetherit-Stab in vier Rängen der Meisterschaft (Lehrling, Adept, Magister, Erzmagier) und der Aetherkern.

Jeder Rang hat zwei Ebenen: den Stab (Holz, Beschläge) und den Kristall in Graustufen – das Spiel färbt ihn in der
Farbe des gewählten Zaubers (custom_model_data). Magister- und Erzmagier-Kristall funkeln (Animation).

  Lehrling   krummer Ast, ein kleiner Splitter mit Bastschnur festgebunden
  Adept      Dämmerholz mit Silberbändern, Kristall in einer Silberklaue
  Magister   Dämmerholz mit Goldbändern, größerer Kristall in einer goldenen Sichel
  Erzmagier  Ebenholz mit leuchtenden Aetherit-Einlagen, Kristall in einem silbernen Ring mit Goldnieten

Aufruf:  python3 tools/items_staff.py [vorschau.png]   (schreibt die Texturen auch direkt)
"""
from __future__ import annotations

import json
from pathlib import Path
from typing import Callable

from PIL import Image

import item_style as st

OAK = st.ramp("#8a6236", dark=0.2, light=0.82, chroma_dark=0.9)
TWINE = st.ramp("#c9b07a", dark=0.3, light=0.95, chroma_dark=0.8)
DUSKWOOD = st.ramp("#5a3f5e", dark=0.18, light=0.72, chroma_dark=0.9, cool=300)
EBONY = st.ramp("#3a2a3e", dark=0.1, light=0.6, chroma_dark=1.0, cool=300)
SILVER = st.ramp("#b8c2e0", dark=0.3, light=0.99, chroma_dark=1.4, cool=280)
GOLD = st.ramp("#d6a838", dark=0.3, light=0.97, cool=30, hue_dark=25, warm=100)
AETHER_GLOW = st.fixed("#3a1460", "#5a2490", "#7a3cc0", "#a060e8", "#c890ff", "#e4c4ff", "#ffffff")
# Kristall: neutrales Grau, das Spiel färbt es (dunkle Stufen = Umriss und Schattenseite)
CRYSTAL = st.fixed("#3a3a3a", "#5c5c5c", "#828282", "#a8a8a8", "#cccccc", "#ebebeb", "#ffffff")
STEEL = st.ramp("#a9b2bc", dark=0.25, light=0.97, chroma_dark=1.6, hue_dark=10)
CORE = st.fixed("#2a0c4a", "#4a1a80", "#7030b8", "#9a58e8", "#c08cff", "#e6ccff", "#ffffff")

# A = Holz, B = Beschlag, C = zweites Metall, D = Kristall (eigene Ebene), E = leuchtende Einlage

APPRENTICE = """
................
................
............u...
...........uts..
...........tsr..
..........ceq...
.........43b....
........43......
.......53.......
......43........
.....43.........
....43..........
...5.3..........
..43............
.43.............
.3..............
"""

ADEPT = """
............u...
...........uts..
..........utsrq.
.........dtsrq..
.........ddrq...
........kdbd....
.......43.......
......43........
.....43.........
.....d..........
....43..........
...43...........
..ec............
.43.............
.3..............
................
"""

MAGISTER = """
..........g.u...
.........f.utt..
.........ettsrq.
.........e.srq.g
..........d.q.f.
.........edcde..
........31......
.......31.......
......31........
.....fc.........
....31..........
...31...........
..fc............
.31.............
.1..............
................
"""

ARCHMAGE = """
..........nngn..
.........n.ut.m.
........n.utsr.l
........g.tsrq.f
........m..rq..k
........m......k
.........e....k.
.......3f.kkkk..
......E1........
.....31.........
....fc..........
...D1...........
..31............
.E1.............
.1..............
................
"""


def _split(art: str, prefer: tuple[str, ...]) -> tuple[st.Spr, st.Spr]:
    """Ganzes Bild mit Umriss, dann aufgeteilt in Stab (ohne Kristall) und Kristall (nur Slot D)."""
    full = st.Spr.art(art).outline(prefer=prefer)
    staff = full.copy().map(lambda x, y, p: None if p[0] == "D" else p)
    crystal = full.copy().map(lambda x, y, p: p if p[0] == "D" else None)
    return staff, crystal


def _sparkle(crystal: st.Spr, frame: int, frames: int) -> st.Spr:
    """Funkeln: ein heller Glanzpunkt wandert über den Kristall, der Rest pulsiert sanft."""
    cells = sorted(((x, y) for x, y, p in crystal.cells() if p[1] >= 3), key=lambda c: (c[1] - c[0], c[0]))
    out = crystal.copy()
    if not cells:
        return out
    pulse = (0, 0, 1, 1, 1, 0, 0, -1)[frame % 8]
    out.map(lambda x, y, p: (p[0], st.clamp(p[1] + pulse, 1, 6)) if p[1] >= 2 else p)
    hx, hy = cells[(frame * len(cells)) // frames]
    out.set(hx, hy, ("D", 6))
    return out


def _strip(frames: list[Image.Image]) -> Image.Image:
    img = Image.new("RGBA", (16, 16 * len(frames)), (0, 0, 0, 0))
    for i, f in enumerate(frames):
        img.alpha_composite(f, (0, 16 * i))
    return img


ANIMATED = {"magister": 3, "archmage": 2}


def build(put: Callable[[str, Image.Image], None]) -> None:
    tiers = {
        "apprentice": (APPRENTICE, {"A": OAK, "B": TWINE, "C": TWINE, "D": CRYSTAL, "E": OAK}),
        "adept": (ADEPT, {"A": DUSKWOOD, "B": SILVER, "C": DUSKWOOD, "D": CRYSTAL, "E": SILVER}),
        "magister": (MAGISTER, {"A": DUSKWOOD, "B": GOLD, "C": SILVER, "D": CRYSTAL, "E": GOLD}),
        "archmage": (ARCHMAGE, {"A": EBONY, "B": GOLD, "C": SILVER, "D": CRYSTAL, "E": AETHER_GLOW}),
    }
    for name, (art, ramps) in tiers.items():
        staff, crystal = _split(art, prefer=("D", "B", "E", "C", "A"))
        put(f"item/aetherit_staff_{name}", staff.image(ramps))
        if name in ANIMATED:
            frames = [_sparkle(crystal, i, 8).image(ramps) for i in range(8)]
            put(f"item/aetherit_staff_{name}_crystal", _strip(frames))
        else:
            put(f"item/aetherit_staff_{name}_crystal", crystal.image(ramps))
    put("item/aether_core", aether_core())


def mcmeta() -> dict[str, dict]:
    """Animationen der funkelnden Kristalle (Dateiname -> Inhalt der .mcmeta)."""
    return {f"item/aetherit_staff_{name}_crystal": {"animation": {"frametime": t, "interpolate": True}}
            for name, t in ANIMATED.items()}


# Aetherkern: violett glühende Kugel in einer Fassung aus Stahl mit vier Krallen und Nieten
AETHER_CORE = """
................
.......b........
......cdc.......
....b.bdb.b.....
...cd.TUT.dc....
....dTUUUTd.....
...bTUUUUTTb....
..cdTUUUTTSdc...
...bTTTTTSSb....
....dTSSSSRd....
...cd.SRR.dc....
....b.bdb.b.....
......cdc.......
.......b........
................
................
"""


def aether_core() -> Image.Image:
    return st.Spr.art(AETHER_CORE).outline(prefer=("B", "G")).image({"B": STEEL, "G": CORE})


def main() -> None:
    import sys

    root = Path(__file__).resolve().parent.parent / "src/main/resources/assets/kingdomsatwar/textures"
    images: dict[str, Image.Image] = {}
    build(lambda key, img: images.__setitem__(key, img))
    for key, img in images.items():
        target = root / f"{key}.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        img.save(target)
    for key, meta in mcmeta().items():
        (root / f"{key}.png.mcmeta").write_text(json.dumps(meta, indent=2) + "\n", encoding="utf-8")
    print(f"{len(images)} Texturen geschrieben")
    if len(sys.argv) > 1:
        sheet: dict[str, Image.Image] = {}
        for key, img in images.items():
            sheet[key.split("/")[-1]] = img.crop((0, 0, 16, 16))
        # zusammengesetzt, in Aether-Violett und in Feuer-Orange getönt
        for tint_name, tint in (("aether", (199, 155, 255)), ("feuer", (255, 106, 26))):
            for name in ("apprentice", "adept", "magister", "archmage"):
                base = images[f"item/aetherit_staff_{name}"].copy()
                crystal = images[f"item/aetherit_staff_{name}_crystal"].crop((0, 0, 16, 16))
                px = crystal.load()
                for x in range(16):
                    for y in range(16):
                        r, g, b, a = px[x, y]
                        px[x, y] = (r * tint[0] // 255, g * tint[1] // 255, b * tint[2] // 255, a)
                base.alpha_composite(crystal)
                sheet[f"{name}_{tint_name}"] = base
        st.preview(sheet, sys.argv[1], zoom=10, cols=6)
        print(f"Vorschau: {sys.argv[1]}")


if __name__ == "__main__":
    main()
