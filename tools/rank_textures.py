#!/usr/bin/env python3
"""
Rangabzeichen der Soldaten (128 x 64, Aufteilung wie die Spielerhaut): eine Ebene, die etwas größer als der Soldat über
ihm liegt (siehe SoldierRankLayer).

  * veteran  – purpurne Schärpe mit Silbersaum, ein silberner Ärmelstreifen
  * elite    – purpurne Schärpe mit Goldsaum, zwei goldene Ärmelstreifen, goldene Schulterstücke, Ehrenmedaille
  * meister  – karmesinrote Schärpe mit Goldsaum, drei Ärmelstreifen, Schulterstücke, zwei Orden
  * legende  – königsblaue Schärpe mit Goldsaum, drei Ärmelstreifen, Schulterstücke, ein goldener Stern auf der Brust
               und ein goldener Lorbeerkranz um den Kopf

    python3 tools/rank_textures.py            # schreibt nach src/main/resources/assets/kingdomsatwar/textures/entity/soldier/rank
"""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image

OUT = Path(__file__).resolve().parent.parent / "src/main/resources/assets/kingdomsatwar/textures/entity/soldier/rank"


def hexc(h: str, a: int = 255) -> tuple[int, int, int, int]:
    h = h.lstrip("#")
    return int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a


SASHES = {
    "purple": (hexc("8540AD"), hexc("6A2C8C"), hexc("4A1D64")),
    "crimson": (hexc("D0464A"), hexc("A82A30"), hexc("74161C")),
    "royal": (hexc("4A72D6"), hexc("2E52B0"), hexc("1D3478")),
}
SILVER = (hexc("D3D7DD"), hexc("9EA4AD"))
GOLD = (hexc("E8B83A"), hexc("A9791C"))
BRIGHT_GOLD = (hexc("F6CF52"), hexc("B8862A"))


def make(rank: int) -> Image.Image:
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
    px = img.load()
    light, mid, dark = SASHES["purple" if rank <= 2 else "crimson" if rank == 3 else "royal"]
    trim, trim_dark = SILVER if rank == 1 else BRIGHT_GOLD if rank == 4 else GOLD

    def sash_face(u0: int, v0: int, reverse: bool) -> None:
        # 8 x 12: Schärpe von der einen Schulter zur anderen Hüfte
        for r in range(12):
            c = 7.0 - r * 7.0 / 11.0
            if reverse:
                c = 7.0 - c
            for col in range(8):
                d = col - c
                ad = abs(d)
                if ad < 1.25:
                    color = light if r < 3 else mid
                    if r % 4 == 3:
                        color = dark
                    px[u0 + col, v0 + r] = color
                elif ad < 2.05:
                    px[u0 + col, v0 + r] = trim if (d < 0) == (not reverse) else trim_dark

    # Körper vorn (20..27, 20..31) und hinten (32..39)
    sash_face(20, 20, False)
    sash_face(32, 20, True)
    # über die linke Schulter
    for v in range(16, 20):
        px[25, v] = trim_dark
        px[26, v] = light
        px[27, v] = trim
    # um die rechte Hüfte
    for u in range(16, 20):
        px[u, 29] = trim
        px[u, 30] = mid
        px[u, 31] = dark
    # Knoten mit Quasten
    px[20, 29] = dark
    px[21, 30] = trim
    px[21, 31] = trim_dark

    def arm_bands(top_u: int, face_v: int, rows: list[int]) -> None:
        # vier Seiten nebeneinander ab top_u - 4 (außen, vorn, innen, hinten), 16 Pixel breit
        u0 = top_u - 4
        for row in rows:
            for k in range(16):
                px[u0 + k, face_v + row] = trim if k % 4 != 3 else trim_dark

    rows = {1: [3], 2: [3, 5]}.get(rank, [3, 5, 7])
    arm_bands(44, 20, rows)      # rechter Arm
    arm_bands(36, 52, rows)      # linker Arm

    if rank >= 2:
        # Schulterstücke: Oberseite golden mit dunklem Rand, oben am Arm eine Fransenreihe
        for (tu, tv, fv) in ((44, 16, 20), (36, 48, 52)):
            for du in range(4):
                for dv in range(4):
                    edge = du in (0, 3) or dv in (0, 3)
                    px[tu + du, tv + dv] = trim_dark if edge else trim
            u0 = tu - 4
            for k in range(16):
                px[u0 + k, fv] = trim
                if k % 2 == 0:
                    px[u0 + k, fv + 1] = trim_dark
    if rank in (2, 3):
        # Ehrenmedaille an rotem Band (vorn auf der rechten Brust, links im Bild)
        px[21, 21] = hexc("B3262E")
        px[22, 21] = hexc("E04A44")
        px[21, 22] = hexc("B3262E")
        px[22, 22] = hexc("E04A44")
        px[21, 23] = hexc("F2CB57")
        px[22, 23] = hexc("E8B83A")
        px[21, 24] = hexc("C9932A")
        px[22, 24] = hexc("F2CB57")
    if rank == 3:
        # zweiter Orden: Silberstern an blauem Band daneben
        px[23, 21] = hexc("2E52B0")
        px[23, 22] = hexc("4A72D6")
        px[23, 23] = hexc("E8ECF2")
        px[23, 24] = hexc("AEB6C2")
    if rank == 4:
        # ein goldener Stern auf der Brust (vorn links im Bild)
        for (u, v, c) in ((22, 21, "FFF0B0"), (21, 22, "F6CF52"), (22, 22, "FFE58A"), (23, 22, "F6CF52"), (22, 23, "E8B83A"),
                          (21, 24, "B8862A"), (23, 24, "B8862A")):
            px[u, v] = hexc(c)
        # Lorbeerkranz: rund um den Kopf (Seiten 0..31, Zeilen 8..15) ein goldenes Band mit Blättern darüber
        for u in range(32):
            px[u, 10] = trim_dark if u % 4 == 3 else trim
            if u % 2 == 0:
                px[u, 9] = trim
            if u % 4 == 1:
                px[u, 8] = trim_dark
        # vorn über der Stirn treffen sich die Zweige: ein kleines Juwel
        px[11, 9] = hexc("E04A44")
        px[12, 9] = hexc("E04A44")
    return img


def main() -> None:
    out = Path(sys.argv[1]) if len(sys.argv) > 1 else OUT
    out.mkdir(parents=True, exist_ok=True)
    for rank, name in ((1, "veteran"), (2, "elite"), (3, "meister"), (4, "legende")):
        make(rank).save(out / f"{name}.png")
    print("Rangabzeichen geschrieben nach", out)


if __name__ == "__main__":
    main()
