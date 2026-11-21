#!/usr/bin/env python3
"""
Erzeugt die Klänge von Malleus, dem Glutschmied: der helle, nachschwingende Schlag auf den Amboss, das Brüllen (ein
tiefes, raues Grollen mit Feuerprasseln), Speerstoß, Hammerschlag auf das Dach, Funkenflug, Glutwurf und das Zerplatzen
der Glutklumpen, der Ruf der Gesellen, das Anfachen der Esse, Atmen, Schmerz, Tod und schwere Schritte. Alles entsteht
aus Rauschen und Tönen (siehe ambience_dsp.py); es werden keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/forge_sounds.py [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/forge/<name><n>.ogg
    Einträge "entity.glutschmied.*" in assets/kingdomsatwar/sounds.json
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
                          trim_tail, fade_edges, saw, glide, curve, formant_filter)
from ambience_dsp import audible_bass  # noqa: E402
from dsp import write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/forge"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100


def mix_at(y: np.ndarray, x: np.ndarray, start: float, gain: float = 1.0) -> None:
    s = int(start * SR)
    m = max(0, min(len(x), len(y) - s))
    y[s:s + m] += x[:m] * gain


def noise(n: int, seed: int) -> np.ndarray:
    return rng(seed).standard_normal(n)


# =====================================================================================
# Bausteine
# =====================================================================================

def anvil_ring(f0: float, dur: float, seed: int, decay: float = 1.1) -> np.ndarray:
    """Der Klang des Ambosses: unharmonische, hell nachschwingende Teiltöne (geschmiedetes Eisen)."""
    return bell(f0, dur, SR, ratios=(1.0, 2.32, 3.88, 5.61, 7.4, 9.9), amps=(1.0, 0.7, 0.42, 0.25, 0.14, 0.07), decay=decay, seed=seed)


def impact(n: int, seed: int, lo: float, hi: float, decay: float) -> np.ndarray:
    """Ein kurzer Aufprall: Rauschen in einem Band, sehr schnell abklingend."""
    return filt(noise(n, seed), "band", [lo, hi], SR, 2) * env_ad(n, SR, 0.0008, decay)


def thud(n: int, f0: float, f1: float, decay: float) -> np.ndarray:
    """Ein tiefer Schlag: ein Sinus, der rasch nach unten gleitet."""
    return tone(glide(n, f0, f1, 0.4), env_ad(n, SR, 0.002, decay), SR, harmonics=((1, 1.0), (2, 0.25), (3, 0.06)))


def crackle(n: int, seed: int, density: float, lo: float = 2500.0, hi: float = 9000.0) -> np.ndarray:
    """Feuerknistern: einzelne, zufällige Knackser (gefiltert)."""
    r = rng(seed)
    y = np.zeros(n)
    count = int(density * n / SR)
    for _ in range(count):
        at = int(r.uniform(0, n - 400))
        length = int(r.uniform(40, 300))
        y[at:at + length] += r.standard_normal(length) * np.exp(-np.arange(length) / (length * 0.25)) * r.uniform(0.3, 1.0)
    return filt(y, "band", [lo, hi], SR, 2)


def fire_roar(n: int, seed: int) -> np.ndarray:
    """Das Fauchen eines großen Feuers: tiefes, wogendes Rauschen."""
    t = np.arange(n) / SR
    r = rng(seed)
    x = filt(noise(n, seed), "band", [120, 1400], SR, 2)
    return x * (0.7 + 0.3 * np.sin(2 * np.pi * r.uniform(2.0, 4.0) * t + r.uniform(0, 6.28)))


def growl(n: int, seed: int, f0: float, f1: float, vowel: str = "a") -> np.ndarray:
    """Ein tiefes, raues Grollen: Sägezahn mit Zittern und Formanten, dazu eine raue Rauschschicht."""
    r = rng(seed)
    t = np.arange(n) / SR
    f = glide(n, f0, f1, 1.2) * (1.0 + 0.04 * np.sin(2 * np.pi * 7.0 * t) + 0.02 * r.standard_normal(n).cumsum() / np.sqrt(SR))
    rough = 1.0 + 0.35 * np.sin(2 * np.pi * r.uniform(28, 36) * t)
    y = saw(f, rough, SR, partials=40, rolloff=1.1)
    formants = {"a": [(650, 120, 1.0), (1100, 160, 0.7), (2600, 300, 0.25)],
                "o": [(450, 100, 1.0), (800, 140, 0.6), (2500, 300, 0.15)],
                "u": [(330, 90, 1.0), (700, 120, 0.4), (2300, 300, 0.1)]}[vowel]
    y = formant_filter(y, SR, formants) * 0.6 + y * 0.12
    rasp = filt(noise(n, seed + 1), "band", [300, 1600], SR, 2) * rough * 0.35
    return y + rasp


# =====================================================================================
# Klänge
# =====================================================================================

def clang(seed: int) -> np.ndarray:
    """Hammer auf Amboss: ein harter Anschlag, ein heller, nachschwingender Klang, ein kurzes Zischen der Glut."""
    r = rng(seed)
    y = np.zeros(int(2.0 * SR))
    f0 = r.uniform(820, 980)
    mix_at(y, anvil_ring(f0, 1.9, seed, decay=r.uniform(0.9, 1.3)), 0.0, 0.55)
    mix_at(y, anvil_ring(f0 * 1.51, 1.2, seed + 1, decay=0.5), 0.0, 0.18)
    mix_at(y, impact(int(0.08 * SR), seed + 2, 1500, 9000, 0.012), 0.0, 0.9)
    mix_at(y, thud(int(0.25 * SR), 180, 90, 0.05), 0.0, 0.5)
    n = int(0.6 * SR)
    mix_at(y, filt(noise(n, seed + 3), "band", [3500, 10000], SR, 2) * env_ad(n, SR, 0.01, 0.18), 0.03, 0.08)
    return y


def roar(seed: int) -> np.ndarray:
    """Das Brüllen: anschwellendes, tiefes Grollen, das aufsteigt und abfällt; darunter das Fauchen der Esse."""
    dur = 2.6
    n = int(dur * SR)
    y = np.zeros(n)
    m = int(2.2 * SR)
    shape = curve(m, [(0.0, 0.0), (0.12, 0.8), (0.3, 1.0), (0.75, 0.85), (1.0, 0.0)])
    g = growl(m, seed, 70.0, 95.0, "a") * shape
    g2 = growl(m, seed + 5, 105.0, 130.0, "o") * shape * 0.5
    mix_at(y, g, 0.05, 1.0)
    mix_at(y, g2, 0.08, 1.0)
    mix_at(y, fire_roar(n, seed + 9) * env_asr(n, SR, 0.3, 1.0), 0.0, 0.35)
    mix_at(y, crackle(n, seed + 11, 40), 0.0, 0.25)
    return filt(y, "low", 7000, SR, 2)


def thrust(seed: int) -> np.ndarray:
    """Speerstoß: ein scharfes, aufsteigendes Zischen durch die Luft, dahinter Flammen."""
    r = rng(seed)
    n = int(0.55 * SR)
    t = np.arange(n) / SR
    sweep = np.zeros(n)
    x = noise(n, seed)
    for lo, hi, g in ((600, 2400, 0.6), (1500, 6000, 0.4)):
        sweep += filt(x, "band", [lo, hi], SR, 2) * g
    env = curve(n, [(0.0, 0.0), (0.25, 1.0), (0.45, 0.6), (1.0, 0.0)])
    y = sweep * env
    y += fire_roar(n, seed + 3) * env_ad(n, SR, 0.06, 0.2) * 0.5
    y += crackle(n, seed + 4, 30) * 0.3
    y *= 1.0 + 0.2 * np.sin(2 * np.pi * r.uniform(18, 26) * t)
    return y


def slam(seed: int) -> np.ndarray:
    """Hammerschlag auf das Dach: ein gewaltiger Schlag, Gestein bricht, die Schockwelle rollt, das Eisen klingt nach."""
    dur = 2.4
    n = int(dur * SR)
    y = np.zeros(n)
    mix_at(y, thud(int(1.2 * SR), 90, 34, 0.35), 0.0, 1.0)
    mix_at(y, impact(int(0.2 * SR), seed, 200, 4000, 0.05), 0.0, 1.0)
    m = int(1.8 * SR)
    rumble = filt(noise(m, seed + 1), "low", 260, SR, 2) * env_ad(m, SR, 0.02, 0.6)
    mix_at(y, rumble, 0.02, 1.4)
    debris = crackle(m, seed + 2, 60, 900, 5000) * env_ad(m, SR, 0.05, 0.5)
    mix_at(y, debris, 0.06, 0.7)
    mix_at(y, anvil_ring(310.0, 1.8, seed + 3, decay=0.8), 0.0, 0.12)
    return y


def sparks(seed: int) -> np.ndarray:
    """Funkenflug: ein heller Schlag Metall auf Metall, dann prasseln Funken davon."""
    y = np.zeros(int(1.4 * SR))
    mix_at(y, anvil_ring(rng(seed).uniform(1300, 1600), 1.0, seed, decay=0.45), 0.0, 0.4)
    mix_at(y, impact(int(0.05 * SR), seed + 1, 2500, 12000, 0.008), 0.0, 0.8)
    n = int(1.2 * SR)
    mix_at(y, crackle(n, seed + 2, 140, 3000, 12000) * env_ad(n, SR, 0.02, 0.4), 0.02, 0.9)
    mix_at(y, filt(noise(n, seed + 3), "band", [4000, 11000], SR, 2) * env_ad(n, SR, 0.01, 0.25), 0.0, 0.15)
    return y


def throw(seed: int) -> np.ndarray:
    """Glutwurf: ein schwerer Schwung, ein dumpfes Abreißen geschmolzenen Metalls, ein Zischen."""
    n = int(0.9 * SR)
    y = np.zeros(n)
    whoosh = filt(noise(n, seed), "band", [250, 1600], SR, 2) * curve(n, [(0.0, 0.0), (0.3, 1.0), (0.6, 0.3), (1.0, 0.0)])
    mix_at(y, whoosh, 0.0, 0.8)
    m = int(0.3 * SR)
    blob = filt(noise(m, seed + 1), "band", [120, 600], SR, 2) * env_ad(m, SR, 0.005, 0.06)
    mix_at(y, blob, 0.12, 1.0)
    hiss = filt(noise(n, seed + 2), "band", [3000, 9000], SR, 2) * env_ad(n, SR, 0.08, 0.3)
    mix_at(y, hiss, 0.1, 0.25)
    return y


def glob(seed: int) -> np.ndarray:
    """Ein Glutklumpen zerplatzt: ein satter Klatsch, Spritzer, dann zischt und knistert die Glut aus."""
    dur = 1.6
    n = int(dur * SR)
    y = np.zeros(n)
    m = int(0.25 * SR)
    mix_at(y, filt(noise(m, seed), "band", [150, 1800], SR, 2) * env_ad(m, SR, 0.002, 0.04), 0.0, 1.2)
    mix_at(y, thud(int(0.3 * SR), 140, 60, 0.06), 0.0, 0.6)
    hiss = filt(noise(n, seed + 1), "band", [2500, 10000], SR, 2) * env_ad(n, SR, 0.03, 0.45)
    mix_at(y, hiss, 0.02, 0.4)
    mix_at(y, crackle(n, seed + 2, 50) * env_ad(n, SR, 0.05, 0.5), 0.05, 0.6)
    return y


def call(seed: int) -> np.ndarray:
    """Ruf der Gesellen: ein tiefer, hallender Ruf, wie aus einem Schlot; Knochen klappern in der Ferne."""
    dur = 2.2
    n = int(dur * SR)
    y = np.zeros(n)
    m = int(1.6 * SR)
    shape = curve(m, [(0.0, 0.0), (0.2, 1.0), (0.7, 0.9), (1.0, 0.0)])
    mix_at(y, growl(m, seed, 58.0, 52.0, "u") * shape, 0.0, 1.0)
    mix_at(y, growl(m, seed + 3, 87.0, 78.0, "o") * shape * 0.4, 0.0, 1.0)
    r = rng(seed + 7)
    for i in range(10):
        clack = impact(int(0.04 * SR), seed + 20 + i, 1200, 5000, 0.006)
        mix_at(y, clack, 0.9 + i * 0.07 + r.uniform(0, 0.04), 0.25)
    return filt(y, "low", 6000, SR, 2)


def stoke(seed: int) -> np.ndarray:
    """Die Esse wird angefacht: ein tiefer Schlag, dann schießt eine Wand aus Flammen fauchend empor."""
    dur = 2.6
    n = int(dur * SR)
    y = np.zeros(n)
    mix_at(y, thud(int(0.8 * SR), 75, 38, 0.25), 0.0, 0.9)
    m = int(2.3 * SR)
    t = np.arange(m) / SR
    roar_ = filt(noise(m, seed), "low", 2400, SR, 2) * (0.6 + 0.4 * np.clip(t / 0.5, 0, 1))
    swell = curve(m, [(0.0, 0.0), (0.12, 1.0), (0.5, 0.75), (1.0, 0.0)])
    mix_at(y, (roar_ * 0.6 + fire_roar(m, seed + 1)) * swell, 0.05, 1.0)
    mix_at(y, crackle(m, seed + 2, 90) * swell, 0.1, 0.6)
    return y


def ambient(seed: int) -> np.ndarray:
    """Er atmet: ein tiefes, rasselndes Ein- und Ausatmen, dazu das leise Knistern der Glut."""
    r = rng(seed)
    dur = 2.4
    n = int(dur * SR)
    y = np.zeros(n)
    m = int(1.6 * SR)
    breath = filt(noise(m, seed), "band", [180, 900], SR, 2) * curve(m, [(0.0, 0.0), (0.35, 0.8), (0.55, 0.4), (0.8, 1.0), (1.0, 0.0)])
    mix_at(y, breath, 0.1, 0.6)
    mix_at(y, growl(int(1.0 * SR), seed + 1, 55.0, 50.0, "u") * env_asr(int(1.0 * SR), SR, 0.3, 0.5), 0.8, 0.45)
    mix_at(y, crackle(n, seed + 2, 25), 0.0, 0.3)
    _ = r
    return filt(y, "low", 6000, SR, 2)


def hurt(seed: int) -> np.ndarray:
    """Schmerz: ein kurzes, wütendes Aufgrollen, Eisen klirrt."""
    n = int(0.6 * SR)
    y = np.zeros(n)
    mix_at(y, growl(int(0.45 * SR), seed, 95.0, 75.0, "a") * env_ad(int(0.45 * SR), SR, 0.01, 0.15), 0.0, 1.0)
    mix_at(y, anvil_ring(rng(seed).uniform(600, 760), 0.5, seed + 1, decay=0.2), 0.0, 0.18)
    mix_at(y, impact(int(0.05 * SR), seed + 2, 900, 5000, 0.01), 0.0, 0.5)
    return y


def death(seed: int) -> np.ndarray:
    """Tod: ein langes, sinkendes Brüllen; die Esse in seiner Brust erlischt zischend, Gestein bröckelt."""
    dur = 4.2
    n = int(dur * SR)
    y = np.zeros(n)
    m = int(2.6 * SR)
    shape = curve(m, [(0.0, 0.0), (0.1, 1.0), (0.6, 0.7), (1.0, 0.0)])
    mix_at(y, growl(m, seed, 92.0, 42.0, "o") * shape, 0.0, 1.0)
    k = int(2.2 * SR)
    hiss = filt(noise(k, seed + 1), "band", [2000, 9000], SR, 2) * curve(k, [(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)])
    mix_at(y, hiss, 1.6, 0.35)
    rumble = filt(noise(k, seed + 2), "low", 300, SR, 2) * env_ad(k, SR, 0.3, 0.8)
    mix_at(y, rumble, 1.9, 0.9)
    mix_at(y, crackle(k, seed + 3, 50, 800, 4000) * env_ad(k, SR, 0.2, 0.7), 2.0, 0.6)
    return filt(y, "low", 8000, SR, 2)


def step(seed: int) -> np.ndarray:
    """Ein schwerer Schritt: dumpfer Aufprall, Eisen klirrt leise, Asche knirscht."""
    n = int(0.5 * SR)
    y = np.zeros(n)
    mix_at(y, thud(int(0.3 * SR), 110, 50, 0.07), 0.0, 1.0)
    mix_at(y, impact(int(0.12 * SR), seed, 300, 2500, 0.03), 0.0, 0.5)
    mix_at(y, anvil_ring(rng(seed).uniform(1100, 1400), 0.3, seed + 1, decay=0.08), 0.01, 0.06)
    mix_at(y, crackle(int(0.2 * SR), seed + 2, 60, 1500, 6000), 0.0, 0.2)
    return y


# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil) oder None, Ziel-Pegel dBFS)
SYNTH = {
    "clang": (clang, 4, (1.6, 0.3), -15.0),
    "roar": (roar, 2, (2.2, 0.35), -14.0),
    "thrust": (thrust, 3, (1.0, 0.2), -17.0),
    "slam": (slam, 2, (2.0, 0.35), -13.0),
    "sparks": (sparks, 3, (1.2, 0.25), -17.0),
    "throw": (throw, 3, (1.0, 0.2), -18.0),
    "glob": (glob, 3, (1.2, 0.25), -17.0),
    "call": (call, 2, (2.4, 0.4), -15.0),
    "stoke": (stoke, 2, (2.0, 0.35), -14.0),
    "ambient": (ambient, 3, (1.4, 0.3), -22.0),
    "hurt": (hurt, 3, (1.0, 0.2), -17.0),
    "death": (death, 1, (2.6, 0.4), -14.0),
    "step": (step, 4, (0.6, 0.15), -21.0),
}

# Ereignis: (Klänge, Reichweite)
EVENTS = {f"entity.glutschmied.{name}": ([name], reach) for name, reach in (
    ("clang", 32), ("roar", 64), ("thrust", 32), ("slam", 48), ("sparks", 32), ("throw", 32), ("glob", 24), ("call", 64),
    ("stoke", 48), ("ambient", 24), ("hurt", 32), ("death", 64), ("step", 16))}


# Seit 1.1.4: diese sehr tiefen Klänge bekommen Obertöne ihres Basses, damit sie auch auf kleinen Lautsprechern
# (Laptop, Fernseher, Kopfhörer ohne Bass) zu hören sind – siehe ambience_dsp.audible_bass.
AUDIBLE = {"step"}


def files_of(name: str) -> list[str]:
    return [f"kingdomsatwar:forge/glutschmied_{name}{i + 1}" for i in range(SYNTH[name][1])]


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    files = []
    for i in range(count):
        x = fn(7000 + i * 41 + zlib.crc32(name.encode()) % 991)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 1500 + i, stereo=False, damping=6000), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.05)
        if name in AUDIBLE:
            x = audible_bass(x, SR)
        x = soft_clip(fit_level(x, level, -2.0))
        path = OUT / f"glutschmied_{name}{i + 1}.ogg"
        write_ogg(x.astype(np.float32), SR, path, quality=5.0)
        files.append(f"kingdomsatwar:forge/glutschmied_{name}{i + 1}")
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
