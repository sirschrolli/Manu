#!/usr/bin/env python3
"""
Der Klang der Rüstung der Lauscher (1.6): die Echoortung.

  * Der Ruf – ein leiser, gläserner Ton aus Hallkristall (zwei Teiltöne, die leicht gegeneinander schweben), ein weicher
    Atemzug aus tiefem Sculk-Brummen darunter, und danach das Echo: derselbe Ton noch zwei-, dreimal, jedes Mal leiser,
    dumpfer und ein wenig tiefer, als käme er von fernen Wänden der Höhle zurück.

Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py, sea_creatures.py); es werden keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/echo_sounds.py [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/item/listener_echo<n>.ogg
    Eintrag "item.listener_armor.echo" in assets/kingdomsatwar/sounds.json (die übrigen bleiben)
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, bell, env_ad, filt, fit_level, rng, room, soft_clip, tone, trim_tail,  # noqa: E402
                          fade_edges)
from dsp import write_ogg  # noqa: E402
from sea_creatures import mix_at  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/item"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100


def crystal_ping(f0: float, dur: float, seed: int, bright: float = 1.0) -> np.ndarray:
    """Ein Anschlag auf Hallkristall: fast rein, ein Teilton eine Duodezime darüber, ein zarter Glanz ganz oben."""
    b = bell(f0, dur, SR, ratios=(1.0, 1.006, 3.01, 5.97), amps=(1.0, 0.55, 0.22 * bright, 0.07 * bright), decay=0.75,
             seed=seed)
    n = len(b)
    fade = int(min(n * 0.4, 0.3 * SR))
    b[n - fade:] *= np.linspace(1.0, 0.0, fade) ** 2
    # der weiche Anschlag: ein Hauch Rauschen um den Grundton
    r = rng(seed + 11)
    m = int(0.05 * SR)
    hiss = filt(r.standard_normal(m), "band", [f0 * 1.8, f0 * 4.5], SR, 2) * env_ad(m, SR, 0.001, 0.03) * 0.25 * bright
    b[:m] += hiss
    return b


def breath(seed: int, dur: float) -> np.ndarray:
    """Ein tiefes, weiches Brummen wie das Atmen von Sculk: kaum zu hören, gibt dem Ruf Gewicht."""
    r = rng(seed)
    n = int(dur * SR)
    t = np.arange(n) / SR
    amp = np.clip(t / 0.06, 0.0, 1.0) * np.exp(-t / 0.28)
    f = 92.0 * (1.0 - 0.08 * np.clip(t / dur, 0.0, 1.0))
    y = tone(f, amp * 0.5, SR, harmonics=((1, 1.0), (2, 0.35), (3, 0.12)))
    y += filt(r.standard_normal(n), "band", [140, 520], SR, 2) * amp * 0.18
    return y


def call(seed: int) -> np.ndarray:
    r = rng(seed)
    dur = 2.4
    y = np.zeros(int(dur * SR))
    f0 = r.uniform(1480.0, 1620.0)
    mix_at(y, breath(seed + 1, 0.9), 0.0, 0.8)
    mix_at(y, crystal_ping(f0, 1.4, seed + 2), 0.0, 1.0)
    # die Echos: von fernen Wänden, jedes leiser, dumpfer und etwas tiefer
    at = 0.0
    for k, (gap, gain, cut) in enumerate(((0.30, 0.42, 4200.0), (0.34, 0.24, 2600.0), (0.40, 0.12, 1700.0))):
        at += gap * r.uniform(0.92, 1.08)
        e = crystal_ping(f0 * (0.995 - 0.004 * k), 1.1, seed + 3 + k, bright=0.6 - 0.15 * k)
        e = filt(e, "low", cut, SR, 2)
        mix_at(y, e, at, gain)
    return y


# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil), Ziel-Pegel dBFS)
SYNTH = {
    "listener_echo": (call, 3, (1.6, 0.35), -19.0),
}

# Ereignis: Liste der Klänge und Reichweite
EVENTS = {
    "item.listener_armor.echo": (["listener_echo"], 16),
}


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    files = []
    for i in range(count):
        x = fn(8300 + i * 37 + zlib.crc32(name.encode()) % 991)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 1700 + i, stereo=False, damping=5000), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.08)
        x = soft_clip(fit_level(x, level, -3.0))
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
