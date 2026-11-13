#!/usr/bin/env python3
"""
Trägt die Geräusche der Mod-Lebewesen in assets/kingdomsatwar/sounds.json ein.

Die Lebewesen nutzen Original-Geräusche aus Minecraft (Verweise wie "minecraft:mob/ravager/idle1"),
verändert durch Tonhöhe und Lautstärke. Es werden KEINE Minecraft-Dateien kopiert – das Spiel lädt die
Klänge zur Laufzeit aus seinen eigenen Dateien. Eigene Aufnahmen (Erzähler, Musik, Soldatenstimmen)
erzeugt tools/audio/build_audio.py; dessen Einträge bleiben hier unangetastet.

Aufruf (aus dem Projektordner):
    python3 tools/sound_events.py                    # sounds.json aktualisieren
    python3 tools/sound_events.py --check liste.txt  # zusätzlich prüfen, ob alle Vanilla-Dateien existieren
                                                     # (liste.txt: Pfade wie minecraft/sounds/mob/x.ogg)
"""
from __future__ import annotations

import argparse
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SOUNDS_JSON = ROOT / "src" / "main" / "resources" / "assets" / "kingdomsatwar" / "sounds.json"


def v(path: str, count: int = 1, *, pitch: float = 1.0, volume: float = 1.0, start: int = 1,
      attenuation: int | None = None, weight: int | None = None) -> list:
    """Vanilla-Klänge "path1".."pathN" (oder genau "path" bei count=0) mit Tonhöhe/Lautstärke."""
    names = [f"minecraft:{path}"] if count == 0 else [f"minecraft:{path}{i}" for i in range(start, start + count)]
    out = []
    for name in names:
        entry: dict = {"name": name}
        if pitch != 1.0:
            entry["pitch"] = pitch
        if volume != 1.0:
            entry["volume"] = volume
        if attenuation is not None:
            entry["attenuation_distance"] = attenuation
        if weight is not None:
            entry["weight"] = weight
        out.append(entry if len(entry) > 1 else name)
    return out


def event(subtitle: str, *groups: list) -> dict:
    sounds = [s for g in groups for s in g]
    return {"sounds": sounds, "subtitle": f"subtitles.kingdomsatwar.{subtitle}"}


# Die Tonhöhe hängt zusätzlich von der Wachstumsstufe ab (DragonEntity#getVoicePitch: 1,7 Schlüpfling …
# 0,8 Uralter Drache). Minecraft begrenzt die Tonhöhe auf 0,5–2,0.
EVENTS: dict[str, dict] = {
    # Eigene Klänge statt Verweisen: Drachen und Dracheneier in audio/dragon_sounds.py; Höhlentroll, Lindwurm, Hirsch, Gans,
    # Urelch, Moorschnapper, Flamingo (Schmerz, Tod) und Kommandostab in audio/creature_sounds.py
    # --- Soldaten und Banditen ---
    "entity.soldier.hurt": event(
        "entity.soldier.hurt",
        v("damage/hit", 3),
    ),
    "entity.soldier.death": event(
        "entity.soldier.death",
        v("damage/hit", 3, pitch=0.85),
    ),
    "entity.soldier.armor": event(
        "entity.soldier.armor",
        v("block/chain/step", 6, volume=0.6),
    ),
    "entity.bandit.ambient": event(
        "entity.bandit.ambient",
        v("mob/pillager/idle", 4, pitch=0.92),
    ),
    "entity.bandit.hurt": event(
        "entity.bandit.hurt",
        v("mob/pillager/hurt", 3, pitch=0.92),
    ),
    "entity.bandit.death": event(
        "entity.bandit.death",
        v("mob/pillager/death", 2, pitch=0.92),
    ),
    # --- Wilde Kreaturen ---
    "entity.wild_boar.ambient": event(
        "entity.wild_boar.ambient",
        v("mob/hoglin/idle", 6, pitch=1.25, volume=0.55),
        v("mob/pig/big/ambient", 4, pitch=0.75, volume=0.8),
    ),
    "entity.wild_boar.angry": event("entity.wild_boar.angry", v("mob/hoglin/angry", 6, pitch=1.2, volume=0.8)),
    "entity.wild_boar.hurt": event("entity.wild_boar.hurt", v("mob/hoglin/hurt", 4, pitch=1.25)),
    "entity.wild_boar.death": event("entity.wild_boar.death", v("mob/hoglin/death", 3, pitch=1.25)),
    "entity.wild_boar.step": event("entity.wild_boar.step", v("mob/hoglin/step", 6, pitch=1.3, volume=0.5)),
    # --- Tiere der eigenen Biome (0.85) ---
    # Schneeleopard: tiefe Katzenstimmen (Ozelot, Streuner, Königskatze), Fauchen und Schnurren tiefer gestimmt
    "entity.snow_leopard.ambient": event(
        "entity.snow_leopard.ambient",
        v("mob/cat/ocelot/idle", 4, pitch=0.62, volume=0.7),
        v("mob/cat/stray/idle", 4, pitch=0.55, volume=0.55),
    ),
    "entity.snow_leopard.purr": event(
        "entity.snow_leopard.purr",
        v("mob/cat/purr", 3, pitch=0.62, volume=0.8),
        v("mob/cat/purreow", 2, pitch=0.6, volume=0.5),
    ),
    "entity.snow_leopard.hiss": event(
        "entity.snow_leopard.hiss",
        v("mob/cat/hiss", 3, pitch=0.58, volume=0.9),
        v("mob/wolf/big/growl", 3, pitch=1.15, volume=0.45),
    ),
    "entity.snow_leopard.eat": event("entity.snow_leopard.eat", v("mob/cat/eat", 2, pitch=0.7, volume=0.8)),
    "entity.snow_leopard.hurt": event("entity.snow_leopard.hurt", v("mob/cat/royal/hurt", 3, pitch=0.66)),
    "entity.snow_leopard.death": event(
        "entity.snow_leopard.death",
        v("mob/cat/ocelot/death", 3, pitch=0.62),
        v("mob/cat/royal/death", 0, pitch=0.6),
    ),
    "entity.snow_leopard.step": event("entity.snow_leopard.step", v("mob/wolf/step", 5, pitch=0.9, volume=0.5)),
    # --- Wesen der eigenen Biome (0.86) ---
    # Glutsalamander: quiekende Molchlaute (tiefer), dazu leises Knistern; Fußtapsen wie ein Frosch
    "entity.ember_salamander.ambient": event(
        "entity.ember_salamander.ambient",
        v("mob/axolotl/idle_air", 5, pitch=0.78, volume=0.55),
        v("block/campfire/crackle", 6, pitch=1.25, volume=0.4),
    ),
    "entity.ember_salamander.hurt": event("entity.ember_salamander.hurt", v("mob/axolotl/hurt", 4, pitch=0.8, volume=0.8)),
    "entity.ember_salamander.death": event(
        "entity.ember_salamander.death",
        v("mob/axolotl/death", 2, pitch=0.78),
    ),
    "entity.ember_salamander.step": event("entity.ember_salamander.step", v("mob/frog/step", 4, pitch=1.3, volume=0.35)),
    "entity.ember_salamander.ignite": event("entity.ember_salamander.ignite", v("fire/ignite", 0, pitch=1.15, volume=0.8)),
    "entity.will_o_wisp.ambient": event(
        "entity.will_o_wisp.ambient",
        v("block/amethyst/resonate", 4, pitch=1.6, volume=0.35),
        v("block/amethyst/shimmer", 0, pitch=1.4, volume=0.4),
    ),
    "entity.will_o_wisp.hurt": event("entity.will_o_wisp.hurt", v("mob/allay/hurt", 2, pitch=1.4, volume=0.6)),
    "entity.will_o_wisp.death": event("entity.will_o_wisp.death", v("mob/allay/death", 2, pitch=1.3, volume=0.7)),
    "entity.will_o_wisp.vanish": event(
        "entity.will_o_wisp.vanish",
        v("block/amethyst/shimmer", 0, pitch=0.8, volume=0.6),
        v("random/glass", 3, pitch=1.8, volume=0.25),
    ),
}


def vanilla_files(event_data: dict) -> list[str]:
    files = []
    for s in event_data["sounds"]:
        name = s if isinstance(s, str) else s["name"]
        if name.startswith("minecraft:"):
            files.append("minecraft/sounds/" + name.split(":", 1)[1] + ".ogg")
    return files


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--check", type=Path, help="Liste der Vanilla-Klangdateien zum Prüfen")
    args = ap.parse_args()

    if args.check:
        known = set(args.check.read_text(encoding="utf-8").split())
        missing = [f for e in EVENTS.values() for f in vanilla_files(e) if f not in known]
        if missing:
            raise SystemExit("Fehlende Vanilla-Klänge:\n  " + "\n  ".join(missing))
        print(f"Alle {sum(len(vanilla_files(e)) for e in EVENTS.values())} Vanilla-Klänge gefunden.")

    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8")) if SOUNDS_JSON.exists() else {}
    data.update(EVENTS)
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"{SOUNDS_JSON.relative_to(ROOT)}: {len(data)} Ereignisse ({len(EVENTS)} aus diesem Skript)")


if __name__ == "__main__":
    main()
