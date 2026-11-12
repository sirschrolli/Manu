#!/usr/bin/env python3
"""
Die Klänge der Rüstung der Zeitwächter (1.01): Chronit ist geronnene Zeit.

  * Zurückspulen – ein Uhrwerk, das rückwärts rast: Ticks, die immer dichter werden, darunter ein Sog, der nach oben
    gleitet (wie ein rückwärts abgespielter Schwall), am Ende ein heller Doppelschlag einer kleinen Uhrglocke;
  * Die Zeit stockt – ein einzelnes, schweres Tick-Tack eines Uhrwerks, das kurz hängenbleibt, mit metallischem Nachklang;
  * Die Uhr tickt wieder – zwei leise Glockentöne (eine Quinte), dazu drei ruhige Ticks.

Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py, sea_creatures.py); es werden keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/chronit_sounds.py [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/item/timewarden_<klang><n>.ogg
    Einträge "item.timewarden.<klang>" in assets/kingdomsatwar/sounds.json (die übrigen bleiben)
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, bell, env_ad, env_asr, filt, fit_level, resonator, rng, room, soft_clip, tone,  # noqa: E402
                          trim_tail, fade_edges)
from dsp import write_ogg  # noqa: E402
from sea_creatures import mix_at  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/item"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100


def tick(r, f: float, weight: float = 1.0) -> np.ndarray:
    """Ein Tick eines Uhrwerks: ein harter, kurzer Anschlag durch zwei metallische Resonanzen."""
    n = int(0.12 * SR)
    x = r.standard_normal(n) * env_ad(n, SR, 0.0002, 0.0018)
    y = resonator(x, f, 28.0, SR) * 2.2 + resonator(x, f * 2.71, 22.0, SR) * 0.9 + resonator(x, f * 0.5, 12.0, SR) * 0.6 * weight
    y += filt(x, "high", 3000, SR, 2) * 0.5
    return y * env_ad(n, SR, 0.0005, 0.07)


def clock_bell(f0: float, dur: float, seed: int, decay: float = 1.1) -> np.ndarray:
    """Eine kleine Uhrglocke: fast harmonisch, hell, mit leisem Schweben."""
    b = bell(f0, dur, SR, ratios=(1.0, 2.0, 2.98, 4.16, 5.43), amps=(1.0, 0.5, 0.22, 0.1, 0.05), decay=decay, seed=seed)
    n = len(b)
    fade = int(min(n * 0.35, 0.4 * SR))
    b[n - fade:] *= np.linspace(1.0, 0.0, fade) ** 2
    return b


def rewind(seed: int) -> np.ndarray:
    r = rng(seed)
    dur = 2.7
    n = int(dur * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    # der Sog: Rauschen, das anschwillt und nach oben gleitet, und plötzlich abreißt (rückwärts abgespielter Schwall)
    swell = np.clip(t / 1.25, 0.0, 1.0) ** 2.4 * np.clip((1.3 - t) / 0.025, 0.0, 1.0)
    noise = r.standard_normal(n)
    for lo, hi, g in ((200, 900, 0.8), (900, 3200, 0.6), (3200, 9000, 0.35)):
        y += filt(noise, "band", [lo, hi], SR, 2) * swell * g
    y += tone(80 + 260 * np.clip(t / 1.3, 0, 1) ** 1.5, swell * 0.5, SR, harmonics=((1, 1.0), (2, 0.4), (3, 0.15)))
    # rückwärts rasende Ticks: immer dichter
    at = 0.05
    gap = 0.16
    k = 0
    while at < 1.28:
        mix_at(y, tick(r, 2400 + 300 * (k % 2), 0.6), at, 0.5 + 0.4 * at)
        at += gap
        gap = max(0.022, gap * 0.86)
        k += 1
    # der Doppelschlag der Uhrglocke
    mix_at(y, clock_bell(1318.0, 1.2, seed + 1, 1.3), 1.3, 0.9)
    mix_at(y, clock_bell(1975.0, 1.0, seed + 2, 1.6), 1.42, 0.6)
    return y


def stall(seed: int) -> np.ndarray:
    r = rng(seed)
    dur = 0.8
    y = np.zeros(int(dur * SR))
    mix_at(y, tick(r, r.uniform(1500, 1700), 1.4), 0.0, 1.0)
    mix_at(y, tick(r, r.uniform(1100, 1250), 1.6), r.uniform(0.2, 0.26), 0.9)
    # das Uhrwerk hängt: ein kurzes, metallisches Schnarren
    n = int(0.22 * SR)
    buzz = filt(r.standard_normal(n), "band", [900, 2600], SR, 2) * (0.6 + 0.4 * np.sign(np.sin(2 * np.pi * 42 * np.arange(n) / SR)))
    buzz *= np.linspace(1.0, 0.0, n) ** 1.5
    mix_at(y, buzz * env_ad(n, SR, 0.01, 0.16) * 0.35, 0.36, 1.0)
    return y


def ready(seed: int) -> np.ndarray:
    r = rng(seed)
    dur = 2.3
    y = np.zeros(int(dur * SR))
    for i in range(3):
        mix_at(y, tick(r, 2000 + 200 * (i % 2), 0.4), 0.05 + i * 0.22, 0.35)
    mix_at(y, clock_bell(880.0, 1.3, seed + 3, 1.1), 0.7, 0.7)
    mix_at(y, clock_bell(1318.0, 1.1, seed + 4, 1.3), 0.95, 0.55)
    return y


# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil), Ziel-Pegel dBFS)
SYNTH = {
    "timewarden_rewind": (rewind, 2, (1.2, 0.3), -13.0),
    "timewarden_tick": (stall, 3, (0.6, 0.2), -15.0),
    "timewarden_ready": (ready, 1, (1.0, 0.3), -17.0),
}

# Ereignis: Liste der Klänge und Reichweite
EVENTS = {
    "item.timewarden.rewind": (["timewarden_rewind"], 24),
    "item.timewarden.tick": (["timewarden_tick"], 16),
    "item.timewarden.ready": (["timewarden_ready"], 12),
}


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    files = []
    for i in range(count):
        x = fn(7100 + i * 41 + zlib.crc32(name.encode()) % 991)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 1500 + i, stereo=False, damping=6000), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.05)
        x = soft_clip(fit_level(x, level, -2.0))
        path = OUT / f"{name}{i + 1}.ogg"
        write_ogg(x.astype(np.float32), SR, path, quality=5.0)
        files.append(f"kingdomsatwar:item/{name}{i + 1}")
        if preview is not None:
            from ambience import spectrogram
            spectrogram(x, SR, preview / f"{name}{i + 1}.png", f"{name}{i + 1}")
    print(f"  {name}: {count} Varianten")
    return files


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--preview", type=Path, help="Ordner für Spektrogramme")
    args = ap.parse_args()
    if args.preview:
        args.preview.mkdir(parents=True, exist_ok=True)
    OUT.mkdir(parents=True, exist_ok=True)
    rendered = {name: render(name, args.preview) for name in SYNTH}
    events = {}
    for key, (sounds, reach) in EVENTS.items():
        entries = []
        for s in sounds:
            entries += [{"name": f, "attenuation_distance": reach} for f in rendered[s]]
        events[key] = {"sounds": entries, "subtitle": f"subtitles.kingdomsatwar.{key}"}
    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8")) if SOUNDS_JSON.exists() else {}
    data.update(events)
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"{len(events)} Klangereignisse geschrieben")


if __name__ == "__main__":
    main()
