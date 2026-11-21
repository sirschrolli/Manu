#!/usr/bin/env python3
"""
Die Stimme der Meerhexe (0.97): ein Flüstern und Summen in Moll, wenn sie schwebt; ein Fauchen, wenn man sie trifft; ihr
Erwachen (ein tiefer Atemzug, ein steigendes Summen, ein gläsernes Funkeln); ihr Lachen (kurze, fallende Silben); ihr
Sirenengesang (eine lange, betörende Melodie in Moll, zweistimmig, eine Oktave darunter ein dunkler Schatten); ihr Schrei im
Zorn; ihr sinkender Klagelaut im Tod. Dazu die Zauber: das Wirken (Rauschen und Glas), fliegende und zerplatzende Tinte, der
Strudel (kreisendes Rauschen über tiefem Grollen), die Blase, der Ruf nach den Haien, aufbrechende Korallen, der Fluch (ein
dunkler, flüsternder Akkord) und der peitschende Schwanz. Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py,
merfolk_sounds.py und kraken_sounds.py); es werden keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/sea_witch_sounds.py [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/sea/sea_witch_<klang><n>.ogg
    Einträge "entity.sea_witch.<klang>" in assets/kingdomsatwar/sounds.json (die übrigen bleiben)
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, env_ad, env_asr, filt, fit_level, formant_filter, glide, resonator, rng, room, saw,  # noqa: E402
                          soft_clip, tone, trim_tail, fade_edges)
from dsp import write_ogg  # noqa: E402
from kraken_sounds import gush, moving_resonator  # noqa: E402
from merfolk_sounds import VOWELS, sing  # noqa: E402
from sea_creatures import blip, click, mix_at, soft_bell  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/sea"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100

# Töne in d-Moll (harmonisch), dazu ein paar tiefe
F = {"D4": 293.66, "E4": 329.63, "F4": 349.23, "G4": 392.0, "A4": 440.0, "Bb4": 466.16, "C#5": 554.37, "D5": 587.33,
     "E5": 659.25, "F5": 698.46, "G5": 783.99, "A5": 880.0, "D3": 146.83, "A2": 110.0, "F3": 174.61, "C#4": 277.18}


def notes(seq):
    return [(F[n] if isinstance(n, str) else n, d, v) for n, d, v in seq]


def bubbles(y: np.ndarray, r, count: int, lo: float, hi: float, start: float, end: float, gain: float, seed: int) -> None:
    for i in range(count):
        mix_at(y, blip(r.uniform(lo, hi), r.uniform(0.04, 0.12), r.uniform(0.6, 1.8), seed + i), r.uniform(start, max(start, end)), gain)


def whisper(r, dur: float, vowel: str = "eh", gain_env=None) -> np.ndarray:
    """Ein Flüstern: Rauschen durch Formanten, mit Silben (Amplitudenwellen)."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    src = filt(r.standard_normal(n), "band", [400, 7000], SR, 2)
    syll = 0.5 + 0.5 * np.sin(2 * np.pi * r.uniform(3.5, 5.5) * t + r.uniform(0, 6)) ** 2
    env = env_asr(n, SR, 0.15, 0.35) * syll
    y = formant_filter(src, SR, [(f * 1.08, bw * 1.6, g) for f, bw, g in VOWELS[vowel]]) * env
    return y


# =====================================================================================
# Stimme
# =====================================================================================

def ambient(seed: int) -> np.ndarray:
    """Ein leises Summen in Moll, darunter ein Flüstern, ein paar Blasen."""
    r = rng(seed)
    phrases = [[("A4", 0.4, "mm"), ("Bb4", 0.35, "mm"), ("A4", 0.3, "mm"), ("F4", 0.7, "oo")],
               [("D5", 0.35, "mm"), ("C#5", 0.3, "mm"), ("A4", 0.8, "oo")]]
    y = sing(notes(phrases[seed % 2]), seed, vibrato=0.016, breath=0.08) * 0.8
    w = whisper(r, len(y) / SR * 0.9, "eh")
    mix_at(y, w, 0.15, 0.6)
    bubbles(y, r, 5, 500, 1300, 0.1, len(y) / SR - 0.3, 0.06, seed * 3)
    return y


def hurt(seed: int) -> np.ndarray:
    """Ein Fauchen und ein kurzer, schriller Laut."""
    r = rng(seed)
    dur = 0.6
    n = int(dur * SR)
    y = filt(r.standard_normal(n), "band", [1800, 8000], SR, 2) * env_ad(n, SR, 0.01, 0.35) * 0.9
    hi = r.uniform(620, 760)
    cry = sing([(hi, 0.18, "ah"), (hi * 0.8, 0.22, "eh")], seed, vibrato=0.02, glide=0.08, attack=0.01)
    mix_at(y, cry, 0.03, 0.9)
    return y


def death(seed: int) -> np.ndarray:
    """Ein langer, sinkender Klagelaut, der in Gurgeln und aufsteigende Blasen zerfließt."""
    r = rng(seed)
    wail = sing([(F["A5"], 0.5, "ah"), (F["F5"], 0.6, "ah"), (F["D5"], 0.7, "oo"), (F["A4"], 1.2, "oo")], seed, vibrato=0.025, glide=0.25)
    dur = len(wail) / SR + 1.4
    n = int(dur * SR)
    y = np.zeros(n)
    mix_at(y, wail, 0.0, 1.0)
    mix_at(y, gush(r, 2.4, 80, 900, 0.6, 1.6), 1.6, 0.6)
    bubbles(y, r, 30, 120, 500, 1.6, dur - 0.2, 0.2, seed)
    return y


def awaken(seed: int) -> np.ndarray:
    """Ein tiefer Atemzug, ein steigendes Summen in Moll – und ein gläsernes Funkeln, als sich die Augen öffnen."""
    r = rng(seed)
    dur = 3.2
    n = int(dur * SR)
    y = np.zeros(n)
    breath = filt(r.standard_normal(int(1.0 * SR)), "band", [300, 3000], SR, 2) * env_asr(int(1.0 * SR), SR, 0.7, 0.2) * 0.35
    mix_at(y, breath, 0.0, 1.0)
    hum = sing(notes([("D4", 0.5, "mm"), ("F4", 0.5, "mm"), ("A4", 0.5, "mm"), ("D5", 0.9, "oo")]), seed, vibrato=0.014)
    mix_at(y, hum, 0.8, 0.9)
    for i, f in enumerate((1174.7, 1396.9, 1760.0, 2349.3)):
        mix_at(y, soft_bell(f, 1.2, 0.5, seed + i), 2.2 + i * 0.05, 0.12)
    bubbles(y, r, 10, 400, 1200, 0.9, 2.8, 0.06, seed + 9)
    return y


def laugh(seed: int) -> np.ndarray:
    """Ein Lachen: kurze, abgehackte, fallende Silben („ha-ha-ha“), dahinter ein heiseres Kichern."""
    r = rng(seed)
    base = r.uniform(520, 600)
    seq = []
    for i in range(5 + seed % 2):
        f = base * (1.0 - 0.06 * i) * (1.0 + r.uniform(-0.02, 0.02))
        seq.append((f, 0.13, "ah" if i % 2 == 0 else "eh"))
        seq.append((f * 0.9, 0.05, "mm"))
    y = sing(seq, seed, vibrato=0.01, glide=0.02, attack=0.01, breath=0.2)
    n = len(y)
    gate = np.ones(n)
    period = int(0.18 * SR)
    for s in range(0, n, period):
        gate[s + int(0.13 * SR):s + period] *= 0.25
    y = y * gate
    y += filt(rng(seed + 1).standard_normal(n), "band", [1200, 5000], SR, 2) * env_asr(n, SR, 0.05, 0.3) * 0.08
    return y


def song(seed: int) -> np.ndarray:
    """Der Sirenengesang: eine lange, betörende Melodie in d-Moll, eine Oktave darunter ein dunkler Schatten, weit im Raum."""
    r = rng(seed)
    melody = [("A4", 0.45, "oo"), ("D5", 0.55, "ah"), ("E5", 0.35, "ah"), ("F5", 0.65, "ah"), ("E5", 0.35, "oo"),
              ("D5", 0.4, "oo"), ("C#5", 0.45, "ah"), ("D5", 1.1, "oo")]
    hi = sing(notes(melody), seed, vibrato=0.022, glide=0.09, breath=0.06)
    lo = sing([(f * 0.5, d, v) for f, d, v in notes(melody)], seed + 1, low=True, vibrato=0.012, glide=0.12, breath=0.03)
    n = max(len(hi), len(lo)) + int(0.6 * SR)
    y = np.zeros(n)
    mix_at(y, hi, 0.0, 1.0)
    mix_at(y, lo, 0.02, 0.45)
    t = np.arange(n) / SR
    y += tone(F["D3"] * (1.0 + 0.003 * np.sin(2 * np.pi * 0.3 * t)), env_asr(n, SR, 0.8, 1.0), SR,
              harmonics=((1, 1.0), (2, 0.3), (3, 0.1))) * 0.08
    bubbles(y, r, 8, 700, 1600, 0.2, n / SR - 0.4, 0.04, seed + 3)
    return y


def fury(seed: int) -> np.ndarray:
    """Ein Schrei: hoch gleitend, rau und verzerrt, darunter ein nasses Grollen."""
    r = rng(seed)
    scream = sing([(780.0, 0.25, "ah"), (980.0, 0.7, "eh"), (720.0, 0.6, "ah")], seed, vibrato=0.04, glide=0.15, attack=0.02, breath=0.3)
    scream = np.tanh(scream * 3.0) * 0.6
    n = len(scream) + int(0.8 * SR)
    y = np.zeros(n)
    mix_at(y, scream, 0.0, 1.0)
    t = np.arange(n) / SR
    env = env_asr(n, SR, 0.2, 0.8)
    y += filt(r.standard_normal(n), "low", 260, SR, 3) * env * 1.4
    y += tone(48.0 * (1.0 - 0.1 * t / (n / SR)), env, SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.25))) * 0.5
    bubbles(y, r, 20, 150, 600, 0.3, n / SR - 0.3, 0.12, seed)
    return y


# =====================================================================================
# Zauber
# =====================================================================================

def cast(seed: int) -> np.ndarray:
    """Zauber wirken: ein ansteigendes Rauschen, gläserne Funken, Bläschen."""
    r = rng(seed)
    dur = 1.1
    n = int(dur * SR)
    t = np.arange(n) / SR
    sweep = 400 + 2600 * (t / dur) ** 1.5
    y = moving_resonator(r.standard_normal(n), sweep, 4.0) * env_asr(n, SR, 0.6, 0.25) * 0.5
    for i in range(5):
        mix_at(y, soft_bell(r.uniform(1400, 3200), 0.6, 0.3, seed + i), 0.45 + i * 0.09, 0.07)
    bubbles(y, r, 6, 600, 1500, 0.1, 0.9, 0.06, seed + 7)
    return y


def ink(seed: int) -> np.ndarray:
    """Ein Tintengeschoss: ein nasser Schwall, der davonzischt."""
    r = rng(seed)
    dur = 0.55
    n = int(dur * SR)
    t = np.arange(n) / SR
    sweep = 1800 * (1.0 - 0.6 * t / dur) + 300
    y = moving_resonator(r.standard_normal(n), sweep, 3.0) * env_ad(n, SR, 0.01, 0.3) * 0.6
    mix_at(y, blip(r.uniform(200, 320), 0.12, 1.4, seed), 0.0, 0.5)
    return y


def ink_hit(seed: int) -> np.ndarray:
    """Tinte zerplatzt: ein dumpfer Klatsch, dann ein paar Bläschen."""
    r = rng(seed)
    dur = 0.6
    n = int(dur * SR)
    y = filt(r.standard_normal(n), "low", 900, SR, 2) * env_ad(n, SR, 0.002, 0.12) * 1.4
    bubbles(y, r, 6, 300, 900, 0.05, 0.45, 0.12, seed)
    return y


def whirlpool(seed: int) -> np.ndarray:
    """Der Strudel: kreisendes Rauschen (die Resonanz wandert auf und ab), darunter tiefes Grollen, viele Blasen."""
    r = rng(seed)
    dur = 3.0
    n = int(dur * SR)
    t = np.arange(n) / SR
    swirl = 600 + 450 * np.sin(2 * np.pi * (1.2 + 0.8 * t / dur) * t)
    env = env_asr(n, SR, 0.4, 0.9)
    y = moving_resonator(r.standard_normal(n), swirl, 3.5) * env * 0.55
    y += filt(r.standard_normal(n), "low", 150, SR, 3) * env * 1.6
    bubbles(y, r, 30, 200, 900, 0.2, dur - 0.3, 0.1, seed)
    return y


def bubble(seed: int) -> np.ndarray:
    """Die Blase schließt sich: ein großes, hohles „Blubb“, das nach oben gleitet."""
    r = rng(seed)
    dur = 0.8
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = 140 * (1.0 + 2.2 * (t / dur) ** 0.7)
    y = tone(f, env_ad(n, SR, 0.01, 0.4), SR, harmonics=((1, 1.0), (2, 0.25), (3, 0.08))) * 0.8
    y += resonator(r.standard_normal(n) * env_ad(n, SR, 0.002, 0.08), 420, 8.0, SR) * 0.4
    bubbles(y, r, 8, 400, 1200, 0.2, 0.7, 0.08, seed)
    return y


def summon(seed: int) -> np.ndarray:
    """Der Ruf nach den Haien: ein tiefer, langer Ruf, dann rauscht etwas Großes heran."""
    r = rng(seed)
    call = sing(notes([("D3", 0.6, "oo"), ("A2", 1.0, "oo")]), seed, low=True, vibrato=0.01, glide=0.2)
    dur = len(call) / SR + 1.2
    n = int(dur * SR)
    y = np.zeros(n)
    mix_at(y, call, 0.0, 1.0)
    mix_at(y, gush(r, 1.4, 150, 1200, 0.5, 0.7), 1.0, 0.8)
    bubbles(y, r, 14, 200, 700, 1.0, dur - 0.2, 0.1, seed)
    return y


def coral(seed: int) -> np.ndarray:
    """Korallen brechen hervor: knackende Klicks und ein dumpfer Stoß aus dem Grund."""
    r = rng(seed)
    dur = 0.7
    n = int(dur * SR)
    y = filt(r.standard_normal(n), "low", 400, SR, 2) * env_ad(n, SR, 0.002, 0.15) * 1.2
    for i in range(int(r.integers(6, 10))):
        mix_at(y, click(r, r.uniform(1400, 3200), r.uniform(6, 14), 0.3), r.uniform(0.0, 0.25), r.uniform(0.25, 0.5))
    bubbles(y, r, 5, 500, 1300, 0.1, 0.6, 0.06, seed)
    return y


def curse(seed: int) -> np.ndarray:
    """Der Fluch der Tiefe: ein dunkler, verstimmter Akkord schwillt an, darüber Flüstern."""
    r = rng(seed)
    dur = 2.4
    n = int(dur * SR)
    t = np.arange(n) / SR
    env = env_asr(n, SR, 1.2, 0.8)
    y = np.zeros(n)
    for f, g in ((F["D3"], 1.0), (F["F3"] * 1.004, 0.7), (F["A2"] * 0.996, 0.8), (F["C#4"] * 1.006, 0.35)):
        y += saw(np.full(n, f) * (1.0 + 0.004 * np.sin(2 * np.pi * r.uniform(0.2, 0.5) * t)), env, SR, partials=12, rolloff=1.6) * g * 0.25
    y = filt(y, "low", 1400, SR, 2)
    mix_at(y, whisper(r, 2.0, "eh"), 0.2, 0.7)
    mix_at(y, whisper(r, 1.6, "ah"), 0.6, 0.5)
    return y


def tail(seed: int) -> np.ndarray:
    """Der Schwanzhieb: ein schnelles Rauschen durchs Wasser, dann ein Schlag."""
    r = rng(seed)
    dur = 0.7
    n = int(dur * SR)
    t = np.arange(n) / SR
    sweep = 300 + 2200 * np.sin(np.pi * np.clip(t / 0.45, 0, 1))
    y = moving_resonator(r.standard_normal(n), sweep, 2.5) * env_ad(n, SR, 0.08, 0.3) * 0.6
    hit = filt(r.standard_normal(int(0.2 * SR)), "low", 700, SR, 2) * env_ad(int(0.2 * SR), SR, 0.001, 0.06)
    mix_at(y, hit, 0.32, 1.2)
    return y


# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil) oder None, Ziel-Pegel dBFS)
SYNTH = {
    "ambient": (ambient, 2, (1.6, 0.3), -22.0),
    "hurt": (hurt, 3, (1.0, 0.2), -17.0),
    "death": (death, 1, (2.6, 0.4), -14.0),
    "awaken": (awaken, 1, (2.4, 0.4), -16.0),
    "laugh": (laugh, 2, (1.8, 0.35), -16.0),
    "song": (song, 1, (2.8, 0.45), -16.0),
    "cast": (cast, 2, (1.4, 0.3), -18.0),
    "ink": (ink, 3, (0.8, 0.2), -18.0),
    "ink_hit": (ink_hit, 3, (0.8, 0.2), -18.0),
    "whirlpool": (whirlpool, 1, (1.6, 0.3), -15.0),
    "bubble": (bubble, 2, (1.0, 0.25), -17.0),
    "summon": (summon, 1, (2.4, 0.4), -15.0),
    "coral": (coral, 3, (1.2, 0.25), -16.0),
    "curse": (curse, 1, (2.4, 0.4), -16.0),
    "tail": (tail, 2, (0.9, 0.2), -16.0),
    "fury": (fury, 1, (2.4, 0.4), -13.0),
}

# Ereignis: Reichweite
EVENTS = {f"entity.sea_witch.{name}": reach for name, reach in (
    ("ambient", 24), ("hurt", 32), ("death", 64), ("awaken", 48), ("laugh", 48), ("song", 48), ("cast", 32), ("ink", 32),
    ("ink_hit", 24), ("whirlpool", 40), ("bubble", 32), ("summon", 48), ("coral", 32), ("curse", 40), ("tail", 32), ("fury", 64))}


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    files = []
    for i in range(count):
        x = fn(7300 + i * 41 + zlib.crc32(name.encode()) % 997)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 1900 + i, stereo=False, damping=4500), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.05)
        x = soft_clip(fit_level(x, level, -2.0))
        path = OUT / f"sea_witch_{name}{i + 1}.ogg"
        write_ogg(x.astype(np.float32), SR, path, quality=5.0)
        files.append(f"kingdomsatwar:entity/sea/sea_witch_{name}{i + 1}")
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
    events = {}
    for name in SYNTH:
        files = render(name, args.preview)
        reach = EVENTS[f"entity.sea_witch.{name}"]
        events[f"entity.sea_witch.{name}"] = {"sounds": [{"name": f, "attenuation_distance": reach} for f in files],
                                              "subtitle": f"subtitles.kingdomsatwar.entity.sea_witch.{name}"}
    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8")) if SOUNDS_JSON.exists() else {}
    data.update(events)
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"{len(events)} Klangereignisse geschrieben")


if __name__ == "__main__":
    main()
