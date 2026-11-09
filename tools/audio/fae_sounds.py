#!/usr/bin/env python3
"""
Erzeugt die Klänge des Feenreichs: das Schimmern des Feenportals (ein leises, gläsernes Flirren mit fernen Glöckchen),
das Aufleuchten beim Entzünden (eine aufsteigende Kaskade aus Glockentönen über einem warmen Akkord) und den Wirbel
der Reise. Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py); es werden keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/fae_sounds.py [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/fae/<name><n>.ogg
    Einträge "block.fae_portal.*" in assets/kingdomsatwar/sounds.json
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, bell, env_ad, env_asr, filt, fit_level, rng, room, soft_clip, tone,  # noqa: E402
                          trim_tail, fade_edges)
from dsp import write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/fae"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100

# eine pentatonische Leiter (Dur), damit jede Tonfolge freundlich klingt
PENTA = (523.25, 587.33, 659.25, 783.99, 880.0, 1046.5, 1174.66, 1318.51, 1567.98, 1760.0, 2093.0)


def mix_at(y: np.ndarray, x: np.ndarray, start: float, gain: float = 1.0) -> None:
    s = int(start * SR)
    m = max(0, min(len(x), len(y) - s))
    y[s:s + m] += x[:m] * gain


def glass(f0: float, dur: float, decay: float, seed: int) -> np.ndarray:
    """Ein weicher Glasklang (fast harmonisch), sanft ausgeblendet."""
    b = bell(f0, dur, SR, ratios=(1.0, 2.0, 3.01, 4.2), amps=(1.0, 0.2, 0.07, 0.02), decay=decay, seed=seed)
    n = len(b)
    fade = int(min(n * 0.4, 0.5 * SR))
    b[n - fade:] *= np.linspace(1.0, 0.0, fade) ** 2
    return b


def shimmer(n: int, seed: int, lo: float = 4500.0, hi: float = 11000.0) -> np.ndarray:
    """Feines, flirrendes Rauschen hoch oben (wie Staub im Licht), langsam wogend."""
    r = rng(seed)
    x = filt(r.standard_normal(n), "band", [lo, hi], SR, 2)
    t = np.arange(n) / SR
    wobble = 0.55 + 0.45 * np.sin(2 * np.pi * (0.7 + r.uniform(0, 0.4)) * t + r.uniform(0, 6.28))
    return x * wobble


# =====================================================================================
# Portal
# =====================================================================================

def portal_ambient(seed: int) -> np.ndarray:
    """Das Schimmern des Portals: ein warmer, schwebender Akkord, darüber Flirren und ein paar ferne Glöckchen."""
    r = rng(seed)
    dur = 4.2
    n = int(dur * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    root = r.choice([261.63, 293.66, 329.63])
    for k, ratio in enumerate((1.0, 1.5, 2.0, 2.5198)):
        f = root * ratio * (1.0 + 0.003 * np.sin(2 * np.pi * (0.2 + 0.07 * k) * t + k))
        y += tone(f, env_asr(n, SR, 1.2, 1.6) * (0.5 / (k + 1)), SR, harmonics=((1, 1.0), (2, 0.12)))
    y += shimmer(n, seed + 1) * env_asr(n, SR, 0.8, 1.4) * 0.18
    for i in range(int(r.integers(3, 6))):
        f = PENTA[int(r.integers(3, len(PENTA)))]
        mix_at(y, glass(f, 1.6, 0.9, seed + 10 + i), r.uniform(0.2, 2.6), r.uniform(0.12, 0.25))
    return filt(y, "low", 9000, SR, 2)


def portal_light(seed: int) -> np.ndarray:
    """Das Entzünden: ein heller Hauch, dann eine aufsteigende Kaskade aus Glockentönen über einem warmen Akkord."""
    r = rng(seed)
    dur = 3.6
    n = int(dur * SR)
    y = np.zeros(n)
    # der Hauch
    whoosh = filt(r.standard_normal(int(0.9 * SR)), "band", [1200, 7000], SR, 2) * env_ad(int(0.9 * SR), SR, 0.35, 0.5)
    mix_at(y, whoosh, 0.0, 0.25)
    # die Kaskade
    start = int(r.integers(0, 3))
    for i in range(8):
        f = PENTA[min(len(PENTA) - 1, start + i)]
        mix_at(y, glass(f, 1.8, 0.75, seed + i), 0.25 + i * 0.085, 0.32 - i * 0.015)
    # der Akkord, der danach stehen bleibt
    t = np.arange(int(2.6 * SR)) / SR
    chord = np.zeros(len(t))
    for k, f in enumerate((392.0, 493.88, 587.33, 783.99)):
        chord += tone(np.full(len(t), f), env_ad(len(t), SR, 0.25, 1.4) * (0.3 / (k + 1)), SR, harmonics=((1, 1.0), (2, 0.1)))
    mix_at(y, chord, 0.6, 0.6)
    return filt(y, "low", 11000, SR, 2)


def portal_travel(seed: int) -> np.ndarray:
    """Die Reise: ein anschwellender, sich drehender Wirbel aus Flirren und gleitenden Glastönen."""
    r = rng(seed)
    dur = 3.0
    n = int(dur * SR)
    t = np.arange(n) / SR
    y = shimmer(n, seed, 2500, 9000) * env_asr(n, SR, 1.6, 0.9) * 0.4
    sweep = 400.0 * np.exp(t * 0.9)
    y += tone(sweep, env_asr(n, SR, 1.4, 1.0) * 0.18, SR, harmonics=((1, 1.0), (2, 0.25), (3, 0.08)))
    for i in range(6):
        f = PENTA[int(r.integers(4, len(PENTA)))]
        mix_at(y, glass(f, 1.2, 0.6, seed + i), r.uniform(0.6, 2.4), 0.16)
    return y


# =====================================================================================
# Ausgabe
# =====================================================================================

# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil) oder None, Ziel-Pegel dBFS)
SYNTH = {
    "portal_ambient": (portal_ambient, 3, (2.0, 0.4), -27.0),
    "portal_light": (portal_light, 2, (1.8, 0.35), -17.0),
    "portal_travel": (portal_travel, 2, (1.6, 0.35), -19.0),
}

# Ereignis: Liste der Klänge (eigene über ihren Namen in SYNTH) und Reichweite der eigenen
EVENTS = {
    "block.fae_portal.ambient": (["portal_ambient"], 12),
    "block.fae_portal.light": (["portal_light"], 24),
    "block.fae_portal.travel": (["portal_travel"], None),
}


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    files = []
    for i in range(count):
        x = fn(5000 + i * 37 + zlib.crc32(name.encode()) % 997)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 1300 + i, stereo=False, damping=7000), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.05)
        x = soft_clip(fit_level(x, level, -2.0))
        path = OUT / f"{name}{i + 1}.ogg"
        write_ogg(x.astype(np.float32), SR, path, quality=5.0)
        files.append(f"kingdomsatwar:fae/{name}{i + 1}")
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
            entries += [{"name": f, "attenuation_distance": reach} if reach else f for f in rendered[s]]
        events[key] = {"sounds": entries, "subtitle": f"subtitles.kingdomsatwar.{key}"}
    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8")) if SOUNDS_JSON.exists() else {}
    data.update(events)
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"{len(events)} Klangereignisse geschrieben")


if __name__ == "__main__":
    main()
