#!/usr/bin/env python3
"""
Erzeugt die kleine Teststruktur für die automatischen GameTests:
eine 5x3x5 große Fläche mit Steinboden (unten) und Luft darüber.

Aufruf: python3 tools/generate_test_structure.py   (benötigt: pip install nbtlib)
"""
from pathlib import Path

import nbtlib
from nbtlib.tag import Compound, Int, List, String

ROOT = Path(__file__).resolve().parent.parent
TARGET = ROOT / "src" / "main" / "resources" / "data" / "kingdomsatwar" / "structure" / "test_platform.nbt"

DATA_VERSION = 5023  # Minecraft 26.3
SIZE_X, SIZE_Y, SIZE_Z = 5, 3, 5


def main() -> None:
    blocks = List[Compound]()
    for y in range(SIZE_Y):
        for z in range(SIZE_Z):
            for x in range(SIZE_X):
                blocks.append(Compound({
                    "pos": List[Int]([Int(x), Int(y), Int(z)]),
                    "state": Int(0 if y == 0 else 1),
                }))
    root = Compound({
        "DataVersion": Int(DATA_VERSION),
        "size": List[Int]([Int(SIZE_X), Int(SIZE_Y), Int(SIZE_Z)]),
        "palette": List[Compound]([
            Compound({"Name": String("minecraft:stone")}),
            Compound({"Name": String("minecraft:air")}),
        ]),
        "blocks": blocks,
        "entities": List[Compound](),
    })
    TARGET.parent.mkdir(parents=True, exist_ok=True)
    nbtlib.File(root, gzipped=True).save(TARGET)
    print(f"Geschrieben: {TARGET}")


if __name__ == "__main__":
    main()
