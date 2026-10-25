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
    # --- Drachen ---
    "entity.dragon.ambient": event(
        "entity.dragon.ambient",
        v("mob/enderdragon/growl", 4, pitch=1.15, volume=0.35),
        v("mob/ravager/idle", 4, pitch=0.7, volume=0.9),
    ),
    "entity.dragon.baby_ambient": event(
        "entity.dragon.baby_ambient",
        v("mob/sniffer/happy", 5, pitch=0.95, volume=0.55),
        v("mob/hoglin/idle", 3, pitch=1.1, volume=0.45, start=2),
    ),
    "entity.dragon.hurt": event(
        "entity.dragon.hurt",
        v("mob/enderdragon/hit", 4, pitch=1.15, volume=0.55),
        v("mob/ravager/hurt", 4, pitch=0.85, volume=0.8),
    ),
    "entity.dragon.death": event(
        "entity.dragon.death",
        v("mob/ravager/death", 3, pitch=0.7),
    ),
    "entity.dragon.roar": event(
        "entity.dragon.roar",
        v("mob/ravager/roar", 4, pitch=0.8, attenuation=48),
        v("mob/enderdragon/growl", 2, pitch=0.95, volume=0.7, attenuation=48),
    ),
    "entity.dragon.flap": event(
        "entity.dragon.flap",
        v("mob/enderdragon/wings", 6, pitch=1.2, volume=0.45),
    ),
    "entity.dragon.step": event(
        "entity.dragon.step",
        v("mob/sniffer/step", 6, volume=0.7),
        v("mob/ravager/step", 5, pitch=0.9, volume=0.5),
    ),
    "entity.dragon.eat": event(
        "entity.dragon.eat",
        v("mob/sniffer/eat", 3, pitch=1.1, volume=0.8),
        v("random/eat", 3, pitch=0.8, volume=0.6),
    ),
    "entity.dragon.bite": event(
        "entity.dragon.bite",
        v("mob/ravager/bite", 3, pitch=1.05),
        v("mob/hoglin/attack", 2, pitch=0.9, volume=0.8),
    ),
    "entity.dragon.grow": event(
        "entity.dragon.grow",
        v("random/levelup", 0, pitch=0.75, volume=0.7),
    ),
    "entity.dragon.breath.fire": event(
        "entity.dragon.breath.fire",
        v("mob/blaze/breathe", 4, pitch=0.55),
        v("mob/ghast/fireball4", 0, pitch=0.6, volume=0.8),
    ),
    "entity.dragon.breath.ice": event(
        "entity.dragon.breath.ice",
        v("mob/breeze/wind_burst", 3, pitch=0.8),
        v("block/powder_snow/break", 3, pitch=0.6),
    ),
    "entity.dragon.breath.lightning": event(
        "entity.dragon.breath.lightning",
        v("mob/warden/sonic_charge", 4, pitch=1.7, volume=0.8),
        v("item/trident/thunder", 2, pitch=1.3, volume=0.5),
    ),
    "entity.dragon.breath.nature": event(
        "entity.dragon.breath.nature",
        v("mob/breeze/wind_burst", 3, pitch=1.0, volume=0.8),
        v("block/chorus_flower/grow", 4, pitch=0.7),
    ),
    "entity.dragon.breath.earth": event(
        "entity.dragon.breath.earth",
        v("dig/gravel", 4, pitch=0.5),
        v("mob/goat/impact", 3, pitch=0.6, volume=0.8),
    ),
    "entity.dragon.breath.water": event(
        "entity.dragon.breath.water",
        v("liquid/heavy_splash", 0, pitch=1.1),
        v("mob/glow_squid/squirt", 3, pitch=0.6),
    ),
    # --- Dracheneier ---
    "block.dragon_egg.crack": event(
        "block.dragon_egg.crack",
        v("mob/turtle/egg/egg_crack", 5, pitch=0.7),
    ),
    "block.dragon_egg.hatch": event(
        "block.dragon_egg.hatch",
        v("mob/turtle/egg/egg_break", 2, pitch=0.65),
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
