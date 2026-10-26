#!/usr/bin/env python3
"""
Erzeugt die Teststrukturen für die automatischen GameTests (Steinboden unten, Luft darüber):
  - test_platform: 5x3x5 für Block-Tests
  - test_arena:    9x6x9 für Tests mit Drachen und anderen großen Lebewesen
  - test_village:  14x32x14 für Tests, die ein ganzes Gebäude eines Umbrer-Dorfs bauen

Aufruf: python3 tools/generate_test_structure.py   (benötigt: pip install nbtlib)
"""
from pathlib import Path

import nbtlib
from nbtlib.tag import Compound, Int, List, String

ROOT = Path(__file__).resolve().parent.parent
STRUCTURES = ROOT / "src" / "main" / "resources" / "data" / "kingdomsatwar" / "structure"

DATA_VERSION = 5023  # Minecraft 26.3
PLATFORMS = {"test_platform": (5, 3, 5), "test_arena": (9, 6, 9), "test_village": (14, 32, 14)}


def write_platform(name: str, size_x: int, size_y: int, size_z: int) -> None:
    blocks = List[Compound]()
    for y in range(size_y):
        for z in range(size_z):
            for x in range(size_x):
                blocks.append(Compound({
                    "pos": List[Int]([Int(x), Int(y), Int(z)]),
                    "state": Int(0 if y == 0 else 1),
                }))
    root = Compound({
        "DataVersion": Int(DATA_VERSION),
        "size": List[Int]([Int(size_x), Int(size_y), Int(size_z)]),
        "palette": List[Compound]([
            Compound({"Name": String("minecraft:stone")}),
            Compound({"Name": String("minecraft:air")}),
        ]),
        "blocks": blocks,
        "entities": List[Compound](),
    })
    target = STRUCTURES / f"{name}.nbt"
    target.parent.mkdir(parents=True, exist_ok=True)
    nbtlib.File(root, gzipped=True).save(target)
    print(f"Geschrieben: {target}")


def main() -> None:
    for name, size in PLATFORMS.items():
        write_platform(name, *size)


if __name__ == "__main__":
    main()
