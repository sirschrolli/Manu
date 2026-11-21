#!/usr/bin/env python3
"""
Die Stimmen des Meervolks (0.93): Die Meerjungfrauen summen und singen kleine Melodien (pentatonisch, mit Vibrato, mal
mit geschlossenem Mund, mal auf „u“ oder „a“), beim Tauschen ein heller Zweiklang mit einem Glitzern, zustimmend ein
steigendes „mm-hm“, ablehnend ein fallendes; verletzt ein kurzer Aufschrei, im Tod eine sinkende Klage. Die Tritonwachen
brummen tief, ächzen und stöhnen. Alles entsteht aus einer kleinen Stimme (Sägezahn durch Formanten), dazu Atem,
gedämpft wie unter Wasser, mit ein paar Bläschen (siehe ambience_dsp.py und sea_creatures.py).

Aufruf (aus dem Projektordner):
    python3 tools/audio/merfolk_sounds.py [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/sea/<mermaid|triton>_<klang><n>.ogg
    Einträge "entity.mermaid.*" und "entity.triton.*" in assets/kingdomsatwar/sounds.json (die übrigen bleiben)
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, env_ad, env_asr, filt, fit_level, formant_filter, rng, room, saw, soft_clip,  # noqa: E402
                          trim_tail, fade_edges)
from dsp import write_ogg  # noqa: E402
from sea_creatures import blip, mix_at, soft_bell  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/sea"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100

# Formanten (Frequenz, Bandbreite, Pegel) einer hellen Stimme; die tiefe Stimme der Wachen liegt etwa 15 % tiefer
VOWELS = {
    "mm": [(300, 55, 1.0), (1250, 220, 0.10), (2600, 300, 0.04)],
    "oo": [(360, 60, 1.0), (820, 90, 0.42), (2700, 160, 0.10)],
    "ah": [(820, 90, 1.0), (1300, 110, 0.62), (2850, 170, 0.20)],
    "eh": [(560, 70, 1.0), (1900, 120, 0.5), (2800, 170, 0.18)],
}

# Töne (pentatonisch um D)
N = {"F#4": 369.99, "A4": 440.0, "B4": 493.88, "C#5": 554.37, "D5": 587.33, "E5": 659.25, "F#5": 739.99, "A5": 880.0, "B5": 987.77,
     "D3": 146.83, "A2": 110.0, "E3": 164.81, "G2": 98.0, "B2": 123.47}


def sing(notes: list[tuple[str | float, float, str]], seed: int, low: bool = False, vibrato: float = 0.011, breath: float = 0.05,
         glide: float = 0.05, attack: float = 0.06) -> np.ndarray:
    """Eine kleine gesungene Phrase: Töne (Höhe, Dauer, Vokal) legato mit Gleiten dazwischen, Vibrato, Atem."""
    r = rng(seed)
    pad = 0.35
    total = sum(d for _, d, _ in notes) + pad
    n = int(total * SR)
    t = np.arange(n) / SR
    freq = np.zeros(n)
    amp = np.zeros(n)
    weights = {v: np.zeros(n) for v in VOWELS}
    pos = 0.0
    prev = None
    for i, (note, dur, vowel) in enumerate(notes):
        f = N[note] if isinstance(note, str) else float(note)
        s0 = int(pos * SR)
        s1 = int((pos + dur) * SR)
        seg = s1 - s0
        if prev is None:
            freq[s0:s1] = f
        else:
            g = min(seg, int(glide * SR))
            freq[s0:s0 + g] = prev * (f / prev) ** np.linspace(0.0, 1.0, g)
            freq[s0 + g:s1] = f
        # jede Silbe setzt ein wenig neu an, die letzte klingt lang aus
        e = env_asr(seg, SR, attack if i == 0 else 0.03, 0.05 if i < len(notes) - 1 else min(dur * 0.6, 0.35))
        amp[s0:s1] = np.maximum(amp[s0:s1], e * (0.85 + 0.15 * r.random()))
        w = env_asr(seg, SR, 0.03, 0.03)
        weights[vowel][s0:s1] += w
        pos += dur
        prev = f
    freq[int(pos * SR):] = prev
    # Vibrato setzt verzögert ein, dazu ein leichtes Schwanken wie im Wasser
    vib = vibrato * np.sin(2 * np.pi * (5.2 + r.uniform(-0.4, 0.4)) * t) * np.clip(t / 0.25, 0, 1)
    wob = 0.004 * np.sin(2 * np.pi * 0.9 * t + r.uniform(0, 6))
    f = freq * (1.0 + vib + wob)
    src = saw(f, amp, SR, partials=36, rolloff=1.25)
    src += filt(r.standard_normal(n), "band", [600, 5000], SR, 2) * amp * breath
    out = np.zeros(n)
    for vowel, w in weights.items():
        if not w.any():
            continue
        formants = VOWELS[vowel]
        if low:
            formants = [(fq * 0.85, bw, g) for fq, bw, g in formants]
        out += formant_filter(src, SR, formants) * np.clip(w, 0, 1)
    # gedämpft wie unter Wasser
    out = filt(out, "low", 3600 if not low else 2400, SR, 2)
    return out


def bubbles(y: np.ndarray, r, count: int, lo: float, hi: float, start: float, end: float, gain: float, seed: int) -> None:
    for i in range(count):
        mix_at(y, blip(r.uniform(lo, hi), r.uniform(0.04, 0.1), r.uniform(0.8, 2.0), seed + i), r.uniform(start, end), gain)


# ------------------------------------------------------------------------------------- Meerjungfrauen

AMBIENT = [
    [("A4", 0.28, "mm"), ("D5", 0.32, "mm"), ("E5", 0.55, "oo")],
    [("D5", 0.24, "oo"), ("B4", 0.24, "oo"), ("A4", 0.28, "mm"), ("D5", 0.6, "oo")],
    [("F#5", 0.3, "ah"), ("E5", 0.26, "oo"), ("D5", 0.62, "oo")],
    [("B4", 0.22, "mm"), ("D5", 0.22, "mm"), ("F#5", 0.3, "oo"), ("E5", 0.55, "mm")],
]


def mermaid_ambient(seed: int) -> np.ndarray:
    r = rng(seed)
    phrase = AMBIENT[seed % len(AMBIENT)]
    y = sing(phrase, seed, vibrato=0.012)
    bubbles(y, r, 3, 900, 1800, 0.1, len(y) / SR - 0.3, 0.05, seed * 7)
    return y


def mermaid_trade(seed: int) -> np.ndarray:
    r = rng(seed)
    hi = "A5" if seed % 2 == 0 else "B5"
    y = sing([("D5", 0.16, "ah"), (hi, 0.42, "ah")], seed, vibrato=0.009)
    # ein Glitzern dazu
    for i, f in enumerate((1760.0, 2349.3, 2637.0)):
        mix_at(y, soft_bell(f * (1 + r.uniform(-0.004, 0.004)), 0.9, 0.35, seed + i), 0.18 + i * 0.07, 0.06)
    return y


def mermaid_yes(seed: int) -> np.ndarray:
    return sing([("A4", 0.14, "mm"), ("D5" if seed % 2 == 0 else "E5", 0.26, "oo")], seed, vibrato=0.006, attack=0.03)


def mermaid_no(seed: int) -> np.ndarray:
    return sing([("D5", 0.16, "mm"), ("A4" if seed % 2 == 0 else "B4", 0.26, "mm")], seed, vibrato=0.004, attack=0.03)


def mermaid_hurt(seed: int) -> np.ndarray:
    r = rng(seed)
    n = int(0.42 * SR)
    f0 = 700.0 + r.uniform(-40, 40)
    y = sing([(f0, 0.08, "ah"), (f0 * 0.74, 0.14, "ah")], seed, vibrato=0.0, breath=0.12, glide=0.07, attack=0.008)
    gasp = filt(r.standard_normal(n), "band", [900, 4200], SR, 2) * env_ad(n, SR, 0.005, 0.06)
    mix_at(y, gasp, 0.0, 0.25)
    bubbles(y, r, 5, 700, 1600, 0.05, 0.3, 0.06, seed * 11)
    return y


def mermaid_death(seed: int) -> np.ndarray:
    r = rng(seed)
    y = sing([("D5", 0.32, "oo"), ("B4", 0.32, "oo"), ("A4", 0.36, "oo"), ("F#4", 0.9, "mm")], seed, vibrato=0.014)
    y *= np.linspace(1.0, 0.55, len(y))
    bubbles(y, r, 14, 500, 1500, 0.4, len(y) / SR - 0.2, 0.06, seed * 13)
    return y


# ------------------------------------------------------------------------------------- Tritonwachen

def triton_ambient(seed: int) -> np.ndarray:
    phrases = [[("A2", 0.3, "mm"), ("D3", 0.55, "oo")], [("D3", 0.3, "mm"), ("B2", 0.5, "mm")], [("B2", 0.25, "oo"), ("E3", 0.5, "oo")]]
    return sing(phrases[seed % len(phrases)], seed, low=True, vibrato=0.006, breath=0.03)


def triton_hurt(seed: int) -> np.ndarray:
    r = rng(seed)
    y = sing([(170.0 + r.uniform(-10, 10), 0.08, "eh"), (130.0, 0.12, "ah")], seed, low=True, vibrato=0.0, breath=0.18, glide=0.06,
             attack=0.01)
    n = int(0.3 * SR)
    mix_at(y, filt(r.standard_normal(n), "band", [300, 2000], SR, 2) * env_ad(n, SR, 0.004, 0.05), 0.0, 0.3)
    bubbles(y, r, 4, 400, 1000, 0.05, 0.25, 0.05, seed * 17)
    return y


def triton_death(seed: int) -> np.ndarray:
    r = rng(seed)
    y = sing([("D3", 0.3, "ah"), ("B2", 0.35, "oo"), ("G2", 1.0, "oo")], seed, low=True, vibrato=0.01, breath=0.08)
    y *= np.linspace(1.0, 0.5, len(y))
    bubbles(y, r, 12, 350, 1100, 0.3, len(y) / SR - 0.2, 0.06, seed * 19)
    return y


SYNTH = {
    "mermaid_ambient": (mermaid_ambient, 4, (0.8, 0.22), -20.0),
    "mermaid_trade": (mermaid_trade, 2, (0.8, 0.2), -20.0),
    "mermaid_yes": (mermaid_yes, 2, (0.6, 0.18), -19.0),
    "mermaid_no": (mermaid_no, 2, (0.6, 0.18), -19.0),
    "mermaid_hurt": (mermaid_hurt, 3, (0.5, 0.15), -17.0),
    "mermaid_death": (mermaid_death, 1, (1.0, 0.25), -18.0),
    "triton_ambient": (triton_ambient, 3, (0.8, 0.2), -19.0),
    "triton_hurt": (triton_hurt, 2, (0.5, 0.15), -16.0),
    "triton_death": (triton_death, 1, (1.0, 0.25), -17.0),
}

# Ereignis: eigene Klänge und Reichweite
EVENTS = {
    "entity.mermaid.ambient": (["mermaid_ambient"], 16),
    "entity.mermaid.trade": (["mermaid_trade"], 16),
    "entity.mermaid.yes": (["mermaid_yes"], 16),
    "entity.mermaid.no": (["mermaid_no"], 16),
    "entity.mermaid.hurt": (["mermaid_hurt"], 16),
    "entity.mermaid.death": (["mermaid_death"], 16),
    "entity.triton.ambient": (["triton_ambient"], 16),
    "entity.triton.hurt": (["triton_hurt"], 16),
    "entity.triton.death": (["triton_death"], 16),
}


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    files = []
    for i in range(count):
        # fester Startwert je Variante (7000 ist durch 2 und 4 teilbar: die Melodien kommen der Reihe nach)
        x = fn(7000 + i)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 1500 + i, stereo=False, damping=4000), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.003, 0.05)
        x = soft_clip(fit_level(x, level, -3.0))
        path = OUT / f"{name}{i + 1}.ogg"
        write_ogg(x.astype(np.float32), SR, path, quality=5.0)
        files.append(f"kingdomsatwar:entity/sea/{name}{i + 1}")
        if preview is not None:
            from ambience import spectrogram
            spectrogram(x, SR, preview / f"{name}{i + 1}.png", f"{name}{i + 1}")
            write_ogg(x.astype(np.float32), SR, preview / f"{name}{i + 1}.ogg", quality=5.0)
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
