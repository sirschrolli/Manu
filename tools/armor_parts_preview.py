#!/usr/bin/env python3
"""Vorschau der 3D-Rüstungsteile (entitygen/models/armor_parts.py) auf einer Puppe in Rüstungsgröße.

Aufruf: python3 tools/armor_parts_preview.py <satz>[,<satz>…] <ausgabe.png> [slots=head,chest,legs,feet]
Jeder Satz wird von vorn schräg, von der Seite und von hinten gezeigt (eine Zeile je Satz).
"""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from PIL import Image  # noqa: E402

from entitygen.geometry import Model  # noqa: E402
from entitygen.models import armor_parts  # noqa: E402
from entitygen.paint import paint_model, pick, ramp  # noqa: E402
from entitygen.preview import render  # noqa: E402

PUPPET = {"head": ((0, 0, 0), [(-5, -9, -5, 10, 10, 10)]), "body": ((0, 0, 0), [(-5, -1, -3, 10, 14, 6)]),
          "left_arm": ((5, 2, 0), [(-2, -3, -3, 6, 14, 6)]), "right_arm": ((-5, 2, 0), [(-4, -3, -3, 6, 14, 6)]),
          "left_leg": ((1.9, 12, 0), [(-2.9, -0.9, -2.9, 5.8, 13.8, 5.8)]),
          "right_leg": ((-1.9, 12, 0), [(-2.9, -0.9, -2.9, 5.8, 13.8, 5.8)])}


def puppet_with(set_name: str, slots: list[str]) -> Model:
    src = armor_parts.build()
    m = Model("puppet", 256, 384)
    m.tex_res = 2
    for bone, (pivot, boxes) in PUPPET.items():
        m.bone(bone, "root", pivot)
        for (x, y, z, w, h, d) in boxes:
            m.cube(bone, (x, y, z), (w, h, d), "base")

    def copy(b, parent: str, name: str | None = None):
        nb = name or b.name
        m.bone(nb, parent, b.pivot, b.rotation)
        for c in b.cubes:
            m.cube(nb, c.origin, c.size, c.material, c.inflate, c.tag, c.faces)
        for ch in b.children:
            copy(ch, nb)

    for slot in slots:
        for bone in armor_parts.SLOTS[slot]:
            name = f"{set_name}_{slot}_{bone}"
            copy(src.bones[name], bone, f"{name}__p")
    return m


def main():
    sets = sys.argv[1].split(",")
    target = sys.argv[2]
    slots = ["head", "chest", "legs", "feet"]
    zoom, focus = 0.95, None
    for arg in sys.argv[3:]:
        if arg.startswith("slots="):
            slots = arg[6:].split(",")
        elif arg.startswith("zoom="):
            zoom = float(arg[5:])
        elif arg.startswith("focus="):
            focus = arg[6:].split(",")
    pal = dict(armor_parts.PALETTES["armor_parts"])
    pal["base"] = ramp("#40444c", "#9aa0aa", 6)
    mats = armor_parts.materials("armor_parts")
    mats["base"] = lambda ctx: pick(ctx.pal["base"], 0.45 + (0.1 if ctx.face == "top" else 0.0))
    size = 360
    views = ((-35, 12), (-90, 6), (150, 12))
    sheet = Image.new("RGBA", (size * len(views), size * len(sets)), (40, 44, 52, 255))
    for row, s in enumerate(sets):
        m = puppet_with(s, slots)
        m.pack()
        tex, glow = paint_model(m, mats, pal, seed=100)
        for col, (yaw, pitch) in enumerate(views):
            img = render(m, tex, glow, yaw, pitch, size, background=(40, 44, 52, 255), zoom=zoom, focus=focus)
            sheet.paste(img, (col * size, row * size))
    sheet.save(target)
    print(target)


if __name__ == "__main__":
    main()
