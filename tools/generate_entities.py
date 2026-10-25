#!/usr/bin/env python3
"""
Erzeugt Modell-Geometrie (Java) und Texturen aller Mobs der Mod.

Aufruf (aus dem Projektordner):  python3 tools/generate_entities.py [--preview]
Ergebnis:
  src/main/java/.../client/model/geom/<Name>Geometry.java   (nicht von Hand ändern)
  src/main/resources/assets/kingdomsatwar/textures/entity/<mob>/<variante>.png (+ _glow.png)
"""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from entitygen.models import dragon  # noqa: E402
from entitygen.paint import paint_model  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/io/github/sirschrolli/kingdomsatwar/client/model/geom"
TEX = ROOT / "src/main/resources/assets/kingdomsatwar/textures/entity"
PACKAGE = "io.github.sirschrolli.kingdomsatwar.client.model.geom"


def write_model(module, class_name: str, doc: str, folder: str, variants: dict, preview: bool):
    model = module.build()
    model.pack()
    JAVA.mkdir(parents=True, exist_ok=True)
    (JAVA / f"{class_name}.java").write_text(model.java_class(PACKAGE, class_name, doc), encoding="utf-8")
    out = TEX / folder
    out.mkdir(parents=True, exist_ok=True)
    for i, (variant, pal) in enumerate(variants.items()):
        img, glow = paint_model(model, module.materials(variant), pal, seed=100 + i, params={"variant": variant})
        img.save(out / f"{variant}.png")
        if glow.getbbox() is not None:
            glow.save(out / f"{variant}_glow.png")
        if preview:
            img.resize((img.width * 3, img.height * 3)).save(f"/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad/ent_{folder}_{variant}.png")
    print(f"{class_name}: {len(model.all_cubes())} Quader, {len(variants)} Varianten")


def main():
    preview = "--preview" in sys.argv
    write_model(dragon, "DragonGeometry", "Geometrie des Drachen-Modells (ausgewachsene Größe).", "dragon",
                dragon.PALETTES, preview)


if __name__ == "__main__":
    main()
