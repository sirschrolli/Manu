#!/usr/bin/env python3
"""
Erzeugt die Klänge der Himmelswacht der Bosse (seit 0.82): den Warnton, wenn ein Boss einen Flieger ins Visier nimmt (drei
rasch aufsteigende Glockenschläge über einem dunklen Puls), das Aufladen des Siegels (ein anschwellendes, steigendes
Summen mit Kristallflirren), das Lösen des Bannpfeils (ein tiefer Schlag und ein aufreißender Luftzug), den Treffer (Glas
zerspringt, die Schwingen flattern und versagen, ein fallender Ton), das Verpuffen, den Annäherungston der Warnanzeige und
das helle Aufatmen, wenn der Flugbann endet. Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py); es werden
keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/sky_sounds.py [--only name ...] [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/sky/<name><n>.ogg
    Einträge "boss.sky_ward.*" in assets/kingdomsatwar/sounds.json
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, bell, env_ad, env_asr, fade_edges, filt, fit_level, glide, rng, room, saw, soft_clip, tone,  # noqa: E402
                          trim_tail)
from dsp import write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/sky"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100


def mix_at(y: np.ndarray, x: np.ndarray, start: float, gain: float = 1.0) -> None:
    s = int(start * SR)
    m = max(0, min(len(x), len(y) - s))
    y[s:s + m] += x[:m] * gain


def noise(n: int, seed: int) -> np.ndarray:
    return rng(seed).standard_normal(n)


def chime(f0: float, dur: float, seed: int, decay: float = 0.9) -> np.ndarray:
    """Ein heller, klarer Glockenschlag."""
    return bell(f0, dur, SR, ratios=(1.0, 2.01, 3.02, 4.17, 5.43), amps=(1.0, 0.4, 0.2, 0.09, 0.04), decay=decay, seed=seed)


def crystal(f0: float, dur: float, seed: int, decay: float = 0.4) -> np.ndarray:
    """Splitterndes Glas: unharmonische, sehr helle Teiltöne."""
    return bell(f0, dur, SR, ratios=(1.0, 1.47, 2.31, 3.17, 4.53, 6.1), amps=(1.0, 0.8, 0.6, 0.4, 0.25, 0.15), decay=decay, seed=seed)


def whoosh(n: int, seed: int, f0: float, f1: float, q: float = 1.0) -> np.ndarray:
    """Ein Luftzug: Rauschen durch ein wanderndes Bandpassfilter."""
    x = noise(n, seed)
    out = np.zeros(n)
    steps = 24
    for i in range(steps):
        a, b = i * n // steps, (i + 1) * n // steps
        f = f0 * (f1 / f0) ** (i / (steps - 1))
        seg = filt(x[max(0, a - 512):b], "band", [max(40.0, f / (1.0 + q)), min(18000.0, f * (1.0 + q))], SR, 2)
        out[a:b] = seg[-(b - a):]
    return out


def thud(n: int, f0: float, f1: float, decay: float) -> np.ndarray:
    return tone(glide(n, f0, f1, 0.4), env_ad(n, SR, 0.002, decay), SR, harmonics=((1, 1.0), (2, 0.25), (3, 0.06)))


# =====================================================================================
# Klänge
# =====================================================================================

def mark(seed: int) -> np.ndarray:
    """Ins Visier genommen: drei rasch aufsteigende Glockenschläge (kleine Terzen), darunter ein dunkler, drängender Puls."""
    n = int(1.25 * SR)
    y = np.zeros(n)
    base = 880.0 * (1.0 + (seed % 3 - 1) * 0.01)
    for i, ratio in enumerate((1.0, 1.189, 1.414)):
        mix_at(y, chime(base * ratio, 0.8, seed + i, 0.5), i * 0.13, 0.9)
        mix_at(y, chime(base * ratio * 2.0, 0.4, seed + 10 + i, 0.2), i * 0.13, 0.25)
    # der Puls: zwei kurze, tiefe Stöße
    for i in range(3):
        p = int(0.22 * SR)
        mix_at(y, tone(np.full(p, 110.0), env_ad(p, SR, 0.004, 0.06), SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.3))), i * 0.13, 0.55)
    t = np.arange(n) / SR
    y += filt(noise(n, seed + 20), "band", [5000, 11000], SR, 2) * np.exp(-t / 0.35) * 0.05
    return y


def charge(seed: int) -> np.ndarray:
    """Das Siegel lädt: ein Summen, das anschwillt und steigt, Kristallflirren, das dichter wird – und bricht ab."""
    n = int(1.5 * SR)
    t = np.arange(n) / SR
    u = t / t[-1]
    f = glide(n, 70.0, 300.0, 1.6)
    swell = u ** 1.6
    y = saw(f, swell, SR, partials=14, rolloff=1.25) * 0.5
    y += tone(f * 2.0, swell, SR, harmonics=((1, 1.0), (2, 0.3))) * 0.35
    y += tone(f * 3.01, swell ** 2, SR) * 0.2
    y = filt(y, "low", 3200, SR, 2)
    y += whoosh(n, seed, 300.0, 5200.0, 0.8) * swell * 0.5
    # Kristallflirren: immer dichter
    r = rng(seed + 1)
    for _ in range(26):
        at = float(np.sqrt(r.uniform(0.02, 1.0))) * 1.36
        mix_at(y, crystal(r.uniform(2200.0, 5200.0) * (1.0 + at * 0.5), 0.22, int(r.integers(1, 9999)), 0.09), at, 0.12 + 0.2 * at / 1.36)
    y *= env_asr(n, SR, 0.08, 0.03)
    return y


def launch(seed: int) -> np.ndarray:
    """Der Bannpfeil löst sich: ein tiefer Schlag, ein Luftzug, der aufreißt und davonjagt, ein heller Schweif."""
    n = int(1.3 * SR)
    y = np.zeros(n)
    mix_at(y, thud(int(0.6 * SR), 170.0, 42.0, 0.2), 0.0, 1.0)
    mix_at(y, filt(noise(int(0.12 * SR), seed), "band", [900, 5200], SR, 2) * env_ad(int(0.12 * SR), SR, 0.001, 0.03), 0.0, 0.8)
    w = int(0.9 * SR)
    mix_at(y, whoosh(w, seed + 1, 500.0, 6400.0, 0.9) * env_ad(w, SR, 0.03, 0.3), 0.01, 0.75)
    g = int(0.8 * SR)
    mix_at(y, tone(glide(g, 520.0, 1900.0, 0.6), env_ad(g, SR, 0.01, 0.2), SR, harmonics=((1, 1.0), (2, 0.4), (3, 0.15))), 0.02, 0.25)
    mix_at(y, crystal(2600.0, 0.5, seed + 2, 0.25), 0.02, 0.2)
    return y


def hit(seed: int) -> np.ndarray:
    """Treffer: Glas zerspringt, ein dumpfer Schlag – dann flattern die Schwingen, versagen, und der Ton fällt in die Tiefe."""
    n = int(1.7 * SR)
    y = np.zeros(n)
    r = rng(seed)
    # der Schlag
    mix_at(y, thud(int(0.5 * SR), 210.0, 48.0, 0.16), 0.0, 1.0)
    mix_at(y, filt(noise(int(0.2 * SR), seed), "band", [1200, 9000], SR, 2) * env_ad(int(0.2 * SR), SR, 0.0008, 0.05), 0.0, 0.9)
    # das Zerspringen
    for i in range(9):
        mix_at(y, crystal(r.uniform(1700.0, 5200.0), 0.5, seed + 3 + i, r.uniform(0.12, 0.35)), r.uniform(0.0, 0.16), r.uniform(0.2, 0.42))
    # das Flattern: Rauschstöße, erst rasch, dann langsamer und matter
    at = 0.12
    gap = 0.055
    for i in range(9):
        p = int(0.07 * SR)
        flap = filt(noise(p, seed + 30 + i), "band", [350, 2400], SR, 2) * env_ad(p, SR, 0.006, 0.02)
        mix_at(y, flap, at, 0.55 * (1.0 - i / 11.0))
        at += gap
        gap *= 1.16
    # der fallende Ton: es geht abwärts
    g = int(1.2 * SR)
    fall = tone(glide(g, 620.0, 85.0, 0.7), env_asr(g, SR, 0.05, 0.6), SR, harmonics=((1, 1.0), (2, 0.3), (3, 0.12)))
    mix_at(y, fall, 0.12, 0.33)
    mix_at(y, whoosh(g, seed + 50, 3200.0, 240.0, 1.0) * env_asr(g, SR, 0.08, 0.5), 0.12, 0.3)
    return y


def fizzle(seed: int) -> np.ndarray:
    """Der Bannpfeil verpufft: ein kurzer, weicher Hauch, ein paar verlöschende Funken."""
    n = int(0.7 * SR)
    y = whoosh(n, seed, 2600.0, 380.0, 1.1) * env_ad(n, SR, 0.01, 0.14) * 0.9
    r = rng(seed + 1)
    for i in range(5):
        mix_at(y, crystal(r.uniform(2400.0, 4800.0), 0.25, seed + 4 + i, 0.08), r.uniform(0.0, 0.25), 0.16)
    return y


def ping(seed: int) -> np.ndarray:
    """Annäherungston der Warnanzeige: ein kurzer, klarer Schlag (die Tonhöhe stellt das Spiel)."""
    n = int(0.16 * SR)
    y = tone(np.full(n, 1180.0), env_ad(n, SR, 0.002, 0.03), SR, harmonics=((1, 1.0), (2, 0.25), (3, 0.08)))
    y += tone(np.full(n, 2360.0), env_ad(n, SR, 0.001, 0.012), SR) * 0.2
    return y


def release(seed: int) -> np.ndarray:
    """Der Flugbann endet: ein Luftzug, der sich hebt, darüber zwei helle Glockentöne (eine Quinte aufwärts)."""
    n = int(1.3 * SR)
    y = np.zeros(n)
    w = int(0.7 * SR)
    mix_at(y, whoosh(w, seed, 400.0, 3600.0, 1.0) * env_asr(w, SR, 0.2, 0.35), 0.0, 0.45)
    mix_at(y, chime(784.0, 1.0, seed + 1, 0.6), 0.12, 0.7)
    mix_at(y, chime(1174.7, 1.0, seed + 2, 0.7), 0.3, 0.75)
    mix_at(y, chime(2349.3, 0.6, seed + 3, 0.3), 0.3, 0.18)
    return y


# Name: (Funktion, Varianten, Raum (Nachhall in s, Anteil) oder None, Pegel in dB)
SYNTH = {
    "mark": (mark, 2, (1.2, 0.2), -15.0),
    "charge": (charge, 2, (1.6, 0.25), -15.0),
    "launch": (launch, 2, (2.0, 0.3), -14.0),
    "hit": (hit, 2, (1.6, 0.25), -14.0),
    "fizzle": (fizzle, 3, (1.0, 0.2), -20.0),
    "ping": (ping, 1, None, -16.0),
    "release": (release, 2, (1.6, 0.3), -18.0),
}

# Ereignis: (Klang, Reichweite in Blöcken)
EVENTS = {f"boss.sky_ward.{name}": (name, reach) for name, reach in (
    ("mark", 24), ("charge", 80), ("launch", 96), ("hit", 48), ("fizzle", 32), ("ping", 0), ("release", 16))}


def files_of(name: str) -> list[str]:
    return [f"kingdomsatwar:sky/{name}{i + 1}" for i in range(SYNTH[name][1])]


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    files = []
    for i in range(count):
        x = fn(8200 + i * 47 + zlib.crc32(name.encode()) % 997)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 2300 + i, stereo=False, damping=7000), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.05)
        x = soft_clip(fit_level(x, level, -2.0))
        path = OUT / f"{name}{i + 1}.ogg"
        write_ogg(x.astype(np.float32), SR, path, quality=5.0)
        files.append(f"kingdomsatwar:sky/{name}{i + 1}")
        if preview is not None:
            from ambience import spectrogram
            spectrogram(x, SR, preview / f"{name}{i + 1}.png", f"{name}{i + 1}")
    print(f"  {name}: {count} Varianten")
    return files


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--preview", type=Path, help="Ordner für Spektrogramme")
    ap.add_argument("--only", nargs="*", help="nur diese Klänge neu erzeugen (die übrigen Dateien bleiben)")
    args = ap.parse_args()
    if args.preview:
        args.preview.mkdir(parents=True, exist_ok=True)
    OUT.mkdir(parents=True, exist_ok=True)
    rendered = {name: render(name, args.preview) if not args.only or name in args.only else files_of(name) for name in SYNTH}
    events = {}
    for key, (sound, reach) in EVENTS.items():
        entries = [{"name": f, "attenuation_distance": reach} if reach else f for f in rendered[sound]]
        events[key] = {"sounds": entries, "subtitle": f"subtitles.kingdomsatwar.{key}"}
    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8")) if SOUNDS_JSON.exists() else {}
    data.update(events)
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"{len(events)} Klangereignisse geschrieben")


if __name__ == "__main__":
    main()
