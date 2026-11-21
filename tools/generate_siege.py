#!/usr/bin/env python3
"""
Erzeugt Modell-Geometrie (Java) und Texturen der Belagerungswaffen und ihrer Geschosse: Katapult, Tribok, Balliste,
Rammbock, Steinkugel und Ballistenbolzen. Nutzt write_model aus generate_entities.py (gleiche Ziele und Konventionen).

Aufruf (aus dem Projektordner):
  python3 tools/generate_siege.py [--preview] [--only CatapultGeometry,TrebuchetGeometry] [--views ORDNER]
Ergebnis:
  src/main/java/.../client/model/geom/<Name>Geometry.java          (nicht von Hand ändern)
  src/main/resources/assets/kingdomsatwar/textures/entity/siege/<id>/<id>.png
``--views ORDNER`` legt zusätzlich Vorschaubilder (mehrere Blickwinkel und Stellungen) als siege_preview_<id>.png ab.
"""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

import generate_entities  # noqa: E402
from entitygen.models import ballista, ballista_bolt, battering_ram, catapult, siege_stone, siege_tower, trebuchet, wagon  # noqa: E402
from entitygen.paint import paint_model  # noqa: E402
from entitygen.preview import render  # noqa: E402
from generate_entities import write_model  # noqa: E402

# (Modul, Java-Klasse, Beschreibung, id)
MODELS = (
    (catapult, "CatapultGeometry",
     "Geometrie des Katapults (Onager auf vier Rädern; Wurfarm senkrecht = abgefeuert, Becher, Stein und Winde als eigene "
     "Knochen; erzeugt von tools/generate_siege.py).", "catapult"),
    (trebuchet, "TrebuchetGeometry",
     "Geometrie des Triboks (Wurfbalken waagerecht gebaut, Gegengewicht und Schlinge hängen, Winde hinten; erzeugt von "
     "tools/generate_siege.py).", "trebuchet"),
    (ballista, "BallistaGeometry",
     "Geometrie der Balliste (Fahrwagen mit Drehscheibe, Geschütz neigt um X, Bogenarme und Sehnen gespannt gebaut; "
     "erzeugt von tools/generate_siege.py).", "ballista"),
    (battering_ram, "BatteringRamGeometry",
     "Geometrie des Rammbocks (fahrbares Schutzdach mit Häuten, Widder als Pendel an zwei Ketten; erzeugt von "
     "tools/generate_siege.py).", "battering_ram"),
    (siege_tower, "SiegeTowerGeometry",
     "Geometrie des Belagerungsturms (Unterbau und zwei aufsetzbare Stockwerke: 7, 10 oder 13 Blöcke bis zur Plattform; "
     "vorn Häute, seitlich Bretter und Streben, hinten die Leiter; Fallbrücke hochgezogen gebaut, dreht um X nach vorn; "
     "erzeugt von tools/generate_siege.py).", "siege_tower"),
    (wagon, "WagonGeometry",
     "Geometrie des Planwagens (Kasten auf vier Rädern, Plane über drei Bögen, Kutschbock, Deichsel; Ladung als eigener "
     "Knochen; erzeugt von tools/generate_siege.py).", "wagon"),
    (siege_stone, "SiegeStoneGeometry",
     "Geometrie der Steinkugel von Katapult und Tribok (Drehpunkt in der Mitte; erzeugt von tools/generate_siege.py).",
     "siege_stone"),
    (ballista_bolt, "BallistaBoltGeometry",
     "Geometrie des Ballistenbolzens (Drehpunkt in der Mitte, Spitze nach -Z; erzeugt von tools/generate_siege.py).",
     "ballista_bolt"),
)

# Vorschau: Blickwinkel (Drehung, Neigung) und Stellungen je Modell
VIEWS = {
    "catapult": [((35, 22), None), ((145, 22), None), ((88, 3), None), ((60, 20), "gespannt"),
                 ((120, 25), "gespannt"), ((2, 70), None)],
    "trebuchet": [((35, 18), None), ((145, 18), None), ((88, 3), None), ((60, 15), "gespannt"),
                  ((70, 10), "abgefeuert"), ((-40, 30), None)],
    "ballista": [((35, 20), None), ((180, 35), None), ((88, 3), None), ((150, 25), "abgefeuert"),
                 ((-30, 60), None), ((60, 15), "abgefeuert")],
    "battering_ram": [((35, 20), None), ((145, 22), None), ((88, 3), None), ((70, 8), "vor"),
                      ((110, 8), "zurück"), ((-30, 45), None)],
    "siege_tower": [((35, 15), None), ((145, 15), None), ((88, 3), "unten"), ((60, 12), "zehn"),
                    ((-30, 20), "sieben"), ((200, 10), "zehn_unten")],
    "wagon": [((35, 20), None), ((145, 20), None), ((88, 3), None), ((160, 25), "leer"),
              ((-30, 45), None), ((0, 10), None)],
    "siege_stone": [((35, 25), None), ((150, -20), None), ((90, 0), None), ((0, 90), None)],
    "ballista_bolt": [((35, 25), None), ((160, 20), None), ((90, 0), None), ((180, 5), None)],
}
POSES = {
    ("catapult", "gespannt"): {"arm": (-80, 0, 0), "winch": (40, 0, 0), "wheel_fl": (30, 0, 0), "wheel_fr": (30, 0, 0),
                               "wheel_bl": (30, 0, 0), "wheel_br": (30, 0, 0)},
    ("trebuchet", "gespannt"): {"arm": (-60, 0, 0), "counterweight": (60, 0, 0), "sling": (60, 0, 0)},
    ("trebuchet", "abgefeuert"): {"arm": (115, 0, 0), "counterweight": (-115, 0, 0), "sling": (-60, 0, 0)},
    ("ballista", "abgefeuert"): {"left_arm": (0, 25, 0), "right_arm": (0, -25, 0), "string_left": (0, -30, 0),
                                 "string_right": (0, 30, 0), "bow": (-12, 0, 0)},
    ("battering_ram", "vor"): {"log": (-16, 0, 0)},
    ("battering_ram", "zurück"): {"log": (14, 0, 0)},
    ("siege_tower", "unten"): {"bridge": (90, 0, 0)},
    ("siege_tower", "zehn"): {"storey_b": (0, 0, 0, 0, 0, 0), "top": (0, 0, 0, 1, 1, 1, 0, 48, 0)},
    ("siege_tower", "zehn_unten"): {"storey_b": (0, 0, 0, 0, 0, 0), "top": (0, 0, 0, 1, 1, 1, 0, 48, 0), "bridge": (90, 0, 0)},
    ("siege_tower", "sieben"): {"storey_a": (0, 0, 0, 0, 0, 0), "storey_b": (0, 0, 0, 0, 0, 0), "top": (0, 0, 0, 1, 1, 1, 0, 96, 0)},
}


def views(module, ident: str, target: Path) -> None:
    """Vorschaubild mit mehreren Blickwinkeln und Stellungen (je Kachel 640 × 640)."""
    from PIL import Image
    model = module.build()
    model.pack()
    pal = module.PALETTES[ident]
    img, glow = paint_model(model, module.materials(ident), pal, seed=100, params={"variant": ident})
    tiles = []
    for (yaw, pitch), pose in VIEWS[ident]:
        tiles.append(render(model, img, glow if glow.getbbox() else None, yaw, pitch, 640,
                            POSES.get((ident, pose)) if pose else None))
    cols = 3 if len(tiles) > 4 else 2
    rows = (len(tiles) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * 640, rows * 640), (30, 32, 36, 255))
    for i, tile in enumerate(tiles):
        sheet.paste(tile, ((i % cols) * 640, (i // cols) * 640))
    sheet.save(target / f"siege_preview_{ident}.png")


def main():
    preview = "--preview" in sys.argv
    if "--only" in sys.argv:
        generate_entities.ONLY.update(sys.argv[sys.argv.index("--only") + 1].split(","))
    target = Path(sys.argv[sys.argv.index("--views") + 1]) if "--views" in sys.argv else None
    for module, class_name, doc, ident in MODELS:
        write_model(module, class_name, doc, f"siege/{ident}", module.PALETTES, preview)
        if target is not None and (not generate_entities.ONLY or class_name in generate_entities.ONLY):
            target.mkdir(parents=True, exist_ok=True)
            views(module, ident, target)


if __name__ == "__main__":
    main()
