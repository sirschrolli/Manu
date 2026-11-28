#!/usr/bin/env python3
"""
Schreibt nur die Texturen einzelner Generator-Module (statt aller, wie tools/generate_textures.py) – so bleiben die übrigen
Bilddateien unangetastet (Pillow kodiert PNGs nicht immer byte-gleich).

Aufruf (aus dem Projektordner):  python3 tools/write_textures.py items_cuisine [weitere Module ...]
Jedes Modul braucht eine Funktion build(put), die put("block/name" | "item/name", Bild) aufruft.
"""
from __future__ import annotations

import importlib
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

ROOT = Path(__file__).resolve().parent.parent
TEXTURES = ROOT / "src" / "main" / "resources" / "assets" / "kingdomsatwar" / "textures"


def main() -> None:
    written = 0
    for name in sys.argv[1:]:
        module = importlib.import_module(name)
        seen: set[str] = set()

        def put(key: str, img) -> None:
            nonlocal written
            if key in seen:
                raise ValueError(f"Textur doppelt erzeugt: {key}")
            seen.add(key)
            target = TEXTURES / f"{key}.png"
            target.parent.mkdir(parents=True, exist_ok=True)
            img.save(target)
            written += 1

        module.build(put)
    print(f"{written} Texturen geschrieben nach {TEXTURES}")


if __name__ == "__main__":
    main()
