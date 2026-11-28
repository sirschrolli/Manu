#!/usr/bin/env python3
"""
Die Klänge des Flammenheiligtums (1.2): Ignara, die Flammenpriesterin, summt leise ein Lied in Phrygisch-Dur (der Tonleiter
mit der übermäßigen Sekunde, die nach Wüste und Tempel klingt); vor dem Altar singt sie einen feierlichen Hymnus über einem
tiefen Summton, ein Tempelgong klingt nach. Beim Erwachen holt sie tief Atem, ihr Gesang steigt, und eine Wand aus Flammen
schießt empor. Dazu: der Knall ihrer Flammenpeitsche (der Überschallknall der Schnur: eine kurze N-Welle, davor das Pfeifen
der Schnur, danach eine Feuerfahne), das Kreisen der Peitsche, die Lavawoge (ein Schlag, dann rollt das Magma brodelnd heran),
die Flammensäulen, das Sammeln der Glut, zerspringende Opalsplitter (Kristallklang), ihr Ruf nach den Glutgeistern, die
Phönixgestalt (ein gewaltiges Auflodern, ein hoher Vogelschrei, Flügelschläge), der Sturzflug, Schmerz und Tod (ein langer,
sinkender Klagelaut; die Flammen verlöschen zischend). Außerdem das Entzünden des Flammenaltars und der Knall der
Flammenpeitsche in der Hand des Spielers. Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py); es werden keine
Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/flame_priest_sounds.py [--preview /pfad] [--only name …]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/nether/<klang><n>.ogg
    Einträge "entity.ignara.*", "block.flame_altar.ignite", "item.flame_whip.crack" in assets/kingdomsatwar/sounds.json
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, bell, curve, env_ad, env_asr, fade_edges, filt, fit_level, formant_filter, glide,  # noqa: E402
                          resonator, rng, room, saw, soft_clip, tone, trim_tail)
from ambience_dsp import audible_bass  # noqa: E402
from dsp import write_ogg  # noqa: E402
from forge_sounds import crackle, fire_roar, impact, mix_at, noise, thud  # noqa: E402
from mystik_sounds import whoosh  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/nether"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100

# Phrygisch-Dur auf E (E F Gis A H C D): die übermäßige Sekunde F–Gis klingt nach Tempel und Wüste
NOTE = {"B2": 123.47, "E3": 164.81, "B3": 246.94, "E4": 329.63, "F4": 349.23, "G#4": 415.30, "A4": 440.0, "B4": 493.88,
        "C5": 523.25, "D5": 587.33, "E5": 659.25, "F5": 698.46, "G#5": 830.61, "B5": 987.77, "E6": 1318.5}

# Vokale einer Frauenstimme (Formant, Bandbreite, Anteil)
VOX = {
    "mm": [(300, 55, 1.0), (1250, 220, 0.10), (2600, 300, 0.04)],
    "oo": [(380, 65, 1.0), (880, 95, 0.40), (2700, 170, 0.10)],
    "oh": [(520, 80, 1.0), (920, 100, 0.50), (2800, 180, 0.14)],
    "ah": [(850, 100, 1.0), (1250, 120, 0.62), (2850, 180, 0.22), (3900, 260, 0.06)],
    "eh": [(600, 75, 1.0), (2050, 130, 0.50), (2900, 180, 0.20)],
}


# =====================================================================================
# Bausteine
# =====================================================================================

def chant(notes, seed: int, vibrato: float = 0.012, breath: float = 0.05, glide_s: float = 0.06, attack: float = 0.08,
          bright: float = 6500.0, rough: float = 0.0, release: float = 0.4) -> np.ndarray:
    """Gesang: Töne (Höhe, Dauer, Vokal) legato mit Gleiten dazwischen, spät einsetzendem Vibrato und etwas Atem."""
    r = rng(seed)
    pad = 0.4
    total = sum(d for _, d, _ in notes) + pad
    n = int(total * SR)
    t = np.arange(n) / SR
    freq = np.zeros(n)
    amp = np.zeros(n)
    weights = {v: np.zeros(n) for v in VOX}
    pos = 0.0
    prev = None
    for i, (note, dur, vowel) in enumerate(notes):
        f = NOTE[note] if isinstance(note, str) else float(note)
        s0 = int(pos * SR)
        s1 = int((pos + dur) * SR)
        seg = s1 - s0
        if prev is None:
            freq[s0:s1] = f
        else:
            g = max(1, min(seg, int(glide_s * SR)))
            freq[s0:s0 + g] = prev * (f / prev) ** np.linspace(0.0, 1.0, g)
            freq[s0 + g:s1] = f
        last = i == len(notes) - 1
        e = env_asr(seg, SR, attack if i == 0 else 0.035, 0.05 if not last else min(dur * 0.6, release))
        amp[s0:s1] = np.maximum(amp[s0:s1], e * (0.88 + 0.12 * r.random()))
        weights[vowel][s0:s1] += env_asr(seg, SR, 0.04, 0.04)
        pos += dur
        prev = f
    freq[int(pos * SR):] = prev
    vib = vibrato * np.sin(2 * np.pi * (5.4 + r.uniform(-0.3, 0.3)) * t) * np.clip((t - 0.15) / 0.3, 0, 1)
    drift = 0.003 * np.sin(2 * np.pi * 0.7 * t + r.uniform(0, 6))
    f = freq * (1.0 + vib + drift)
    src = saw(f, amp, SR, partials=40, rolloff=1.3)
    if rough > 0.0:
        src *= 1.0 + rough * np.sin(2 * np.pi * r.uniform(30, 40) * t)
    src += filt(r.standard_normal(n), "band", [800, 7000], SR, 2) * amp * breath
    out = np.zeros(n)
    for v, w in weights.items():
        if w.any():
            out += formant_filter(src, SR, VOX[v]) * np.clip(w, 0, 1)
    return filt(out, "low", bright, SR, 2)


def flame(n: int, seed: int, lo: float = 150.0, hi: float = 2200.0, shape=((0.0, 0.0), (0.15, 1.0), (0.5, 0.7), (1.0, 0.0))) -> np.ndarray:
    """Ein Flammenstoß: fauchendes Rauschen, weich an- und abschwellend."""
    x = filt(noise(n, seed), "band", [lo, hi], SR, 2)
    return x * curve(n, list(shape))


def gong(f0: float, dur: float, seed: int, decay: float = 1.6) -> np.ndarray:
    """Ein kleiner Tempelgong aus Messing: warm, mit leicht schwebenden Teiltönen, bis zum Ende ausgeblendet."""
    b = bell(f0, dur, SR, ratios=(1.0, 2.0, 2.76, 4.07, 5.4), amps=(1.0, 0.35, 0.28, 0.12, 0.06), decay=decay, seed=seed)
    k = int(min(len(b) * 0.35, 0.6 * SR))
    b[len(b) - k:] *= np.linspace(1.0, 0.0, k) ** 2
    return b


def crystal(f0: float, dur: float, seed: int, decay: float = 0.35) -> np.ndarray:
    """Ein Opal klingt: hell, gläsern, unharmonisch."""
    b = bell(f0, dur, SR, ratios=(1.0, 2.32, 3.11, 4.7, 6.3), amps=(1.0, 0.6, 0.45, 0.3, 0.18), decay=decay, seed=seed)
    k = int(min(len(b) * 0.3, 0.2 * SR))
    b[len(b) - k:] *= np.linspace(1.0, 0.0, k) ** 2
    return b


def snap(seed: int) -> np.ndarray:
    """Der Knall der Peitsche: die Spitze durchbricht die Schallmauer – eine sehr kurze N-Welle, ein heller Rauschstoß,
    ein kurzes Nachklingen der Schnur."""
    r = rng(seed)
    n = int(0.12 * SR)
    y = np.zeros(n)
    k = max(8, int(0.0011 * SR))
    w = np.concatenate([np.linspace(0.0, 1.0, k // 3), np.linspace(1.0, -0.8, k), np.linspace(-0.8, 0.0, k // 2)])
    y[:len(w)] += w
    m = int(0.04 * SR)
    burst = r.standard_normal(m) * env_ad(m, SR, 0.0001, 0.0035)
    y[:m] += filt(burst, "high", 900, SR, 2) * 0.9
    y[:m] += resonator(burst, r.uniform(1700, 2300), 9.0, SR) * 2.5
    y[:m] += resonator(burst, r.uniform(3400, 4200), 12.0, SR) * 1.5
    # gesättigt: dichter und lauter, ohne höhere Spitze; dazu der Widerhall vom Boden (wenige Millisekunden später)
    y = np.tanh(2.5 * y) / np.tanh(2.5)
    out = y.copy()
    for delay, gain in ((0.0042, 0.45), (0.0097, 0.22)):
        d = int(delay * SR)
        out[d:] += filt(y, "low", 6000, SR, 1)[:n - d] * gain
    return out


# =====================================================================================
# Stimme
# =====================================================================================

AMBIENT = [
    [("E4", 0.35, "mm"), ("F4", 0.3, "mm"), ("G#4", 0.5, "oo"), ("F4", 0.3, "mm"), ("E4", 0.9, "oo")],
    [("B4", 0.35, "mm"), ("C5", 0.3, "mm"), ("B4", 0.3, "mm"), ("G#4", 0.45, "oo"), ("A4", 0.3, "mm"), ("G#4", 0.8, "oo")],
    [("A4", 0.4, "mm"), ("G#4", 0.35, "mm"), ("F4", 0.4, "oo"), ("E4", 1.0, "mm")],
]


def ambient(seed: int, v: int) -> np.ndarray:
    """Sie summt leise vor sich hin; die Glut knistert."""
    voice = chant(AMBIENT[v % len(AMBIENT)], seed, vibrato=0.01, breath=0.07, bright=5000.0)
    n = len(voice) + int(0.3 * SR)
    y = np.zeros(n)
    mix_at(y, voice, 0.0, 0.8)
    mix_at(y, crackle(n, seed + 3, 22), 0.0, 0.22)
    mix_at(y, fire_roar(n, seed + 5) * env_asr(n, SR, 0.4, 0.6), 0.0, 0.08)
    return y


PRAYERS = [
    [("E4", 0.55, "ah"), ("G#4", 0.45, "ah"), ("A4", 0.4, "ah"), ("B4", 0.8, "ah"), ("A4", 0.35, "oh"), ("G#4", 0.4, "oh"),
     ("F4", 0.5, "oh"), ("E4", 1.3, "oo")],
    [("B4", 0.5, "ah"), ("C5", 0.4, "ah"), ("B4", 0.35, "ah"), ("A4", 0.45, "oh"), ("G#4", 0.7, "ah"), ("F4", 0.4, "oh"),
     ("G#4", 0.45, "oh"), ("E4", 1.3, "oo")],
]


def pray(seed: int, v: int) -> np.ndarray:
    """Der Hymnus vor dem Altar: eine feierliche Melodie über einem tiefen, gesummten Grundton; ein Gong klingt nach."""
    melody = PRAYERS[v % len(PRAYERS)]
    voice = chant(melody, seed, vibrato=0.016, breath=0.05, glide_s=0.09, bright=6500.0, release=0.8)
    total = sum(d for _, d, _ in melody)
    drone = chant([("E3", total + 0.3, "oo")], seed + 1, vibrato=0.004, breath=0.02, attack=0.6, bright=2400.0, release=1.0)
    n = max(len(voice), len(drone)) + int(1.0 * SR)
    y = np.zeros(n)
    mix_at(y, gong(NOTE["E4"], 3.2, seed + 2, decay=1.8), 0.0, 0.18)
    mix_at(y, drone, 0.05, 0.32)
    mix_at(y, voice, 0.25, 1.0)
    mix_at(y, crackle(n, seed + 3, 18), 0.0, 0.15)
    return y


def hurt(seed: int, v: int) -> np.ndarray:
    """Ein scharfes Einatmen, ein kurzer Schmerzlaut; die Flammen fauchen auf."""
    r = rng(seed)
    n = int(0.75 * SR)
    y = np.zeros(n)
    hi = r.uniform(640, 760)
    cry = chant([(hi, 0.12, "ah"), (hi * 0.78, 0.22, "eh")], seed, vibrato=0.02, glide_s=0.07, attack=0.008, breath=0.25,
                bright=7000.0, release=0.15)
    m = int(0.12 * SR)
    gasp = filt(noise(m, seed + 1), "band", [900, 6000], SR, 2) * env_ad(m, SR, 0.005, 0.04)
    mix_at(y, gasp, 0.0, 0.5)
    mix_at(y, cry, 0.03, 1.0)
    mix_at(y, filt(noise(n, seed + 2), "band", [2500, 9000], SR, 2) * env_ad(n, SR, 0.01, 0.2), 0.0, 0.22)
    mix_at(y, crackle(n, seed + 3, 40), 0.0, 0.3)
    return y


def death(seed: int, v: int) -> np.ndarray:
    """Ein langer, sinkender Klagelaut; die Flammen werden leiser und verlöschen zischend, der Gong klingt ein letztes Mal."""
    wail = chant([("E5", 0.5, "ah"), ("D5", 0.45, "ah"), ("C5", 0.5, "ah"), ("B4", 0.6, "oh"), ("G#4", 0.7, "oh"), ("E4", 1.4, "oo")],
                 seed, vibrato=0.025, glide_s=0.22, breath=0.12, bright=6500.0, release=1.0)
    dur = 5.6
    n = int(dur * SR)
    y = np.zeros(n)
    mix_at(y, wail, 0.0, 1.0)
    m = int(4.0 * SR)
    mix_at(y, fire_roar(m, seed + 1) * curve(m, [(0.0, 0.0), (0.05, 1.0), (0.6, 0.5), (1.0, 0.0)]), 0.0, 0.45)
    mix_at(y, crackle(m, seed + 2, 70) * curve(m, [(0.0, 1.0), (0.7, 0.4), (1.0, 0.0)]), 0.0, 0.4)
    k = int(1.6 * SR)
    hiss = filt(noise(k, seed + 3), "band", [2200, 9000], SR, 2) * curve(k, [(0.0, 0.0), (0.12, 1.0), (1.0, 0.0)])
    mix_at(y, hiss, 3.2, 0.18)
    mix_at(y, gong(NOTE["E4"] * 0.5, 2.0, seed + 4, decay=1.4), 3.6, 0.16)
    return y


def awaken(seed: int, v: int) -> np.ndarray:
    """Sie holt tief Atem, ihr Gesang steigt – und eine Wand aus Flammen schießt empor, Opale klingen."""
    dur = 4.6
    n = int(dur * SR)
    y = np.zeros(n)
    m = int(1.1 * SR)
    inhale = whoosh(m, seed, 300.0, 1800.0, 1.2) * curve(m, [(0.0, 0.0), (0.7, 1.0), (1.0, 0.0)])
    mix_at(y, inhale, 0.0, 0.35)
    rise = [("E4", 0.4, "ah"), ("G#4", 0.35, "ah"), ("B4", 0.35, "ah"), ("E5", 1.3, "ah")]
    mix_at(y, chant(rise, seed + 1, vibrato=0.022, glide_s=0.08, attack=0.05, breath=0.08, bright=7500.0, release=0.6), 0.95, 1.0)
    mix_at(y, chant([(NOTE[k] * 0.5, d, w) for k, d, w in rise], seed + 2, vibrato=0.012, glide_s=0.1, bright=3000.0,
                    release=0.6), 0.97, 0.35)
    burst_at = 2.3
    mix_at(y, thud(int(0.9 * SR), 80.0, 34.0, 0.25), burst_at, 0.6)
    k = int(2.2 * SR)
    swell = curve(k, [(0.0, 0.0), (0.08, 1.0), (0.5, 0.7), (1.0, 0.0)])
    mix_at(y, (fire_roar(k, seed + 3) + filt(noise(k, seed + 4), "low", 2400, SR, 2) * 0.5) * swell, burst_at, 0.8)
    mix_at(y, crackle(k, seed + 5, 90) * swell, burst_at + 0.05, 0.5)
    for i, f in enumerate((NOTE["E6"], NOTE["B5"] * 1.5, NOTE["G#5"] * 2.0)):
        mix_at(y, crystal(f, 1.2, seed + 10 + i, decay=0.5), burst_at + 0.1 + i * 0.06, 0.08)
    return y


def call(seed: int, v: int) -> np.ndarray:
    """Sie ruft die Glutgeister: ein befehlender, hallender Ruf, eine Oktave darunter ein Schatten; Feuer wirbelt heran."""
    melody = [[("E5", 0.22, "ah"), ("B4", 0.2, "ah"), ("E5", 0.95, "ah")],
              [("B4", 0.2, "ah"), ("E5", 0.25, "ah"), ("G#5", 0.85, "ah")]][v % 2]
    dur = 2.8
    n = int(dur * SR)
    y = np.zeros(n)
    mix_at(y, chant(melody, seed, vibrato=0.025, attack=0.02, breath=0.12, rough=0.12, bright=8000.0, release=0.4), 0.0, 1.0)
    mix_at(y, chant([(NOTE[k] * 0.5, d, w) for k, d, w in melody], seed + 1, vibrato=0.012, attack=0.02, bright=3200.0,
                    release=0.4), 0.01, 0.4)
    m = int(2.0 * SR)
    t = np.arange(m) / SR
    whirl = whoosh(m, seed + 2, 300.0, 1400.0, 0.9) * (0.6 + 0.4 * np.sin(2 * np.pi * 2.2 * t) ** 2)
    mix_at(y, whirl * curve(m, [(0.0, 0.0), (0.3, 1.0), (0.7, 0.6), (1.0, 0.0)]), 0.6, 0.45)
    mix_at(y, crackle(m, seed + 3, 60) * curve(m, [(0.0, 0.0), (0.3, 1.0), (1.0, 0.0)]), 0.7, 0.35)
    return y


# =====================================================================================
# Peitsche
# =====================================================================================

def lash(seed: int, v: int) -> np.ndarray:
    """Der Peitschenhieb: die Schnur pfeift, knallt – und eine Feuerfahne faucht hinterher."""
    r = rng(seed)
    dur = 1.2
    n = int(dur * SR)
    y = np.zeros(n)
    m = int(0.17 * SR)
    swish = whoosh(m, seed, 500.0, 4200.0, 0.8) * curve(m, [(0.0, 0.0), (0.85, 1.0), (1.0, 0.2)])
    mix_at(y, swish, 0.0, 0.7)
    at = 0.17
    mix_at(y, snap(seed + 1), at, 0.75)
    k = int(0.95 * SR)
    mix_at(y, flame(k, seed + 2, 200.0, 2600.0, ((0.0, 0.0), (0.06, 1.0), (0.4, 0.5), (1.0, 0.0))), at, 0.7)
    mix_at(y, crackle(k, seed + 3, 70) * curve(k, [(0.0, 1.0), (1.0, 0.0)]), at + 0.02, 0.45)
    _ = r
    return y


def swing(seed: int, v: int) -> np.ndarray:
    """Die Peitsche kreist: die Schnur schwirrt im Takt durch die Luft, die Flamme an ihrer Spitze flattert."""
    r = rng(seed)
    dur = 0.9
    n = int(dur * SR)
    t = np.arange(n) / SR
    rate = r.uniform(4.0, 5.2)
    whirr = whoosh(n, seed, 380.0, 1700.0, 1.0) * (0.25 + 0.75 * np.sin(np.pi * rate * t) ** 4)
    env = curve(n, [(0.0, 0.0), (0.2, 1.0), (0.75, 0.9), (1.0, 0.0)])
    y = whirr * env
    y += fire_roar(n, seed + 1) * env * 0.25
    y += crackle(n, seed + 2, 35) * env * 0.2
    return y


def whip_crack(seed: int, v: int) -> np.ndarray:
    """Der Knall der Flammenpeitsche in der Hand des Spielers: kürzer und leichter als bei der Priesterin."""
    dur = 0.85
    n = int(dur * SR)
    y = np.zeros(n)
    m = int(0.13 * SR)
    mix_at(y, whoosh(m, seed, 600.0, 4500.0, 0.8) * curve(m, [(0.0, 0.0), (0.85, 1.0), (1.0, 0.2)]), 0.0, 0.65)
    mix_at(y, snap(seed + 1), 0.13, 0.7)
    k = int(0.65 * SR)
    mix_at(y, flame(k, seed + 2, 250.0, 2800.0, ((0.0, 0.0), (0.06, 1.0), (0.4, 0.4), (1.0, 0.0))), 0.13, 0.5)
    mix_at(y, crackle(k, seed + 3, 50) * curve(k, [(0.0, 1.0), (1.0, 0.0)]), 0.15, 0.35)
    return y


# =====================================================================================
# Zauber
# =====================================================================================

def lava_blob(seed: int) -> np.ndarray:
    """Eine Blase im Magma platzt: ein satter, tiefer Plopp."""
    r = rng(seed)
    n = int(0.22 * SR)
    f0 = r.uniform(70, 160)
    body = tone(glide(n, f0, f0 * 1.8, 0.6), env_ad(n, SR, 0.004, 0.05), SR, harmonics=((1, 1.0), (2, 0.3), (3, 0.1)))
    pop = filt(noise(n, seed + 1), "band", [200, 1400], SR, 2) * env_ad(n, SR, 0.001, 0.015)
    return body + pop * 0.6


def wave(seed: int, v: int) -> np.ndarray:
    """Die Lavawoge: ein schwerer Schlag, dann rollt das Magma brodelnd und fauchend heran."""
    r = rng(seed)
    dur = 2.8
    n = int(dur * SR)
    y = np.zeros(n)
    mix_at(y, thud(int(1.0 * SR), 85.0, 30.0, 0.3), 0.0, 1.0)
    mix_at(y, impact(int(0.15 * SR), seed, 150, 3000, 0.04), 0.0, 0.8)
    m = int(2.6 * SR)
    t = np.arange(m) / SR
    surge = curve(m, [(0.0, 0.0), (0.08, 1.0), (0.6, 0.7), (1.0, 0.0)])
    rumble = filt(noise(m, seed + 1), "low", 400, SR, 2) * (0.7 + 0.3 * np.sin(2 * np.pi * r.uniform(2.5, 3.5) * t))
    mix_at(y, rumble * surge, 0.03, 1.2)
    mix_at(y, fire_roar(m, seed + 2) * surge, 0.05, 0.55)
    for i in range(14):
        mix_at(y, lava_blob(seed + 20 + i), r.uniform(0.3, 2.2), r.uniform(0.25, 0.5))
    mix_at(y, filt(noise(m, seed + 3), "band", [2500, 9000], SR, 2) * env_ad(m, SR, 0.05, 0.6), 0.05, 0.18)
    return y


def pillar(seed: int, v: int) -> np.ndarray:
    """Eine Flammensäule bricht aus dem Boden: ein dumpfer Stoß, dann schießt das Feuer fauchend empor; Gestein prasselt."""
    dur = 2.2
    n = int(dur * SR)
    y = np.zeros(n)
    mix_at(y, thud(int(0.6 * SR), 70.0, 32.0, 0.15), 0.0, 0.9)
    m = int(0.9 * SR)
    mix_at(y, filt(noise(m, seed), "low", 300, SR, 2) * env_ad(m, SR, 0.01, 0.35), 0.0, 1.0)
    k = int(1.8 * SR)
    env = curve(k, [(0.0, 0.0), (0.1, 1.0), (0.5, 0.8), (1.0, 0.0)])
    mix_at(y, whoosh(k, seed + 1, 200.0, 1800.0, 1.5) * env, 0.05, 0.8)
    mix_at(y, fire_roar(k, seed + 2) * env, 0.05, 0.6)
    mix_at(y, crackle(k, seed + 3, 100) * curve(k, [(0.0, 1.0), (1.0, 0.0)]), 0.08, 0.45)
    mix_at(y, crackle(k, seed + 4, 50, 900, 5000) * env_ad(k, SR, 0.02, 0.4), 0.0, 0.5)
    return y


def charge(seed: int, v: int) -> np.ndarray:
    """Sie sammelt die Glut: ein steigender Ton (Grundton und Quinte), anschwellendes Fauchen, immer dichteres Knistern."""
    dur = 1.6
    n = int(dur * SR)
    y = np.zeros(n)
    m = int(1.25 * SR)
    rise = curve(m, [(0.0, 0.0), (0.85, 1.0), (1.0, 0.0)])
    chord = tone(glide(m, 220.0, 440.0, 1.6), rise, SR, harmonics=((1, 1.0), (2, 0.3), (3, 0.12)))
    chord += tone(glide(m, 330.0, 660.0, 1.6), rise, SR, harmonics=((1, 1.0), (2, 0.2))) * 0.6
    mix_at(y, chord, 0.0, 0.25)
    mix_at(y, whoosh(m, seed, 200.0, 3000.0, 0.6) * rise, 0.0, 0.6)
    mix_at(y, crackle(m, seed + 1, 90) * rise, 0.0, 0.5)
    k = int(0.4 * SR)
    mix_at(y, flame(k, seed + 2, 150.0, 1800.0, ((0.0, 0.0), (0.1, 1.0), (1.0, 0.0))), 1.15, 0.5)
    return y


def shard(seed: int, v: int) -> np.ndarray:
    """Ein Opalsplitter zerspringt: ein heller, gläserner Klang, feine Splitter klirren, ein kleiner Feuerstoß."""
    r = rng(seed)
    dur = 1.0
    n = int(dur * SR)
    y = np.zeros(n)
    mix_at(y, crystal(r.uniform(1800, 2600), 0.9, seed, decay=0.35), 0.0, 0.5)
    mix_at(y, impact(int(0.05 * SR), seed + 1, 2500, 12000, 0.006), 0.0, 0.8)
    for i in range(6):
        mix_at(y, crystal(r.uniform(3000, 6000), 0.3, seed + 10 + i, decay=0.08), r.uniform(0.0, 0.25), 0.12)
    k = int(0.45 * SR)
    mix_at(y, flame(k, seed + 2, 300.0, 2500.0, ((0.0, 0.0), (0.1, 1.0), (1.0, 0.0))), 0.0, 0.4)
    mix_at(y, crackle(k, seed + 3, 60), 0.02, 0.25)
    return y


def phoenix(seed: int, v: int) -> np.ndarray:
    """Die Phönixgestalt: ein gewaltiges Auflodern, ein hoher, aufsteigender Vogelschrei, Flügelschläge, ein Funkenregen."""
    r = rng(seed)
    dur = 3.6
    n = int(dur * SR)
    y = np.zeros(n)
    mix_at(y, thud(int(1.0 * SR), 72.0, 30.0, 0.3), 0.0, 0.9)
    k = int(3.2 * SR)
    swell = curve(k, [(0.0, 0.0), (0.06, 1.0), (0.5, 0.6), (1.0, 0.0)])
    mix_at(y, (fire_roar(k, seed + 1) + filt(noise(k, seed + 2), "low", 2600, SR, 2) * 0.4) * swell, 0.0, 0.55)
    cry = chant([("E5", 0.15, "ah"), ("B5", 0.45, "eh"), ("G#5", 0.65, "ah")], seed + 3, vibrato=0.03, glide_s=0.12, attack=0.03,
                breath=0.15, rough=0.08, bright=9000.0, release=0.4)
    mix_at(y, cry, 0.3, 1.8)
    m = int(1.25 * SR)
    whistle = tone(curve(m, [(0.0, 1320.0), (0.35, 1980.0), (1.0, 1660.0)]) * (1.0 + 0.02 * np.sin(2 * np.pi * 7.0 * np.arange(m) / SR)),
                   env_asr(m, SR, 0.05, 0.4), SR, harmonics=((1, 1.0), (2, 0.15)))
    mix_at(y, whistle, 0.32, 0.18)
    for i, at in enumerate((1.5, 2.0, 2.5)):
        b = int(0.35 * SR)
        mix_at(y, whoosh(b, seed + 10 + i, 150.0, 600.0, 1.0) * env_ad(b, SR, 0.08, 0.12), at, 0.6)
        mix_at(y, thud(int(0.25 * SR), 60.0, 40.0, 0.06), at + 0.05, 0.35)
    j = int(2.6 * SR)
    mix_at(y, crackle(j, seed + 4, 120, 4000, 12000) * curve(j, [(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)]), 0.5, 0.35)
    _ = r
    return y


def dive(seed: int, v: int) -> np.ndarray:
    """Der Sturzflug: ein Schrei, dann pfeift und rauscht es immer lauter herab, das Feuer faucht."""
    dur = 1.6
    n = int(dur * SR)
    y = np.zeros(n)
    mix_at(y, chant([("B5", 0.18, "eh"), ("E5", 0.3, "ah")], seed, vibrato=0.03, glide_s=0.1, attack=0.02, bright=9000.0,
                    release=0.2), 0.0, 0.6)
    m = int(1.45 * SR)
    rise = curve(m, [(0.0, 0.0), (0.85, 1.0), (1.0, 0.0)])
    mix_at(y, whoosh(m, seed + 1, 2500.0, 600.0, 0.8) * rise, 0.05, 0.8)
    mix_at(y, tone(glide(m, 1400.0, 500.0, 0.8), rise, SR, harmonics=((1, 1.0), (2, 0.1))), 0.05, 0.08)
    mix_at(y, fire_roar(m, seed + 2) * rise, 0.05, 0.6)
    mix_at(y, crackle(m, seed + 3, 60) * rise, 0.05, 0.3)
    return y


def ignite(seed: int, v: int) -> np.ndarray:
    """Der Flammenaltar entzündet sich: ein Auflodern, ein heiliger Gongschlag, die Glut knistert."""
    dur = 3.0
    n = int(dur * SR)
    y = np.zeros(n)
    mix_at(y, thud(int(0.6 * SR), 90.0, 45.0, 0.12), 0.0, 0.7)
    k = int(1.5 * SR)
    mix_at(y, flame(k, seed, 150.0, 2500.0, ((0.0, 0.0), (0.08, 1.0), (0.4, 0.6), (1.0, 0.0))), 0.0, 0.8)
    mix_at(y, gong(NOTE["E5"], 2.8, seed + 1, decay=1.4), 0.05, 0.25)
    mix_at(y, gong(NOTE["B5"], 2.2, seed + 2, decay=1.0), 0.1, 0.1)
    mix_at(y, crackle(k, seed + 3, 60) * curve(k, [(0.0, 1.0), (1.0, 0.0)]), 0.05, 0.4)
    return y


# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil) oder None, Ziel-Pegel dBFS)
SYNTH = {
    "ignara_ambient": (ambient, 3, (1.2, 0.25), -22.0),
    "ignara_pray": (pray, 2, (2.6, 0.4), -19.0),
    "ignara_hurt": (hurt, 3, (0.9, 0.2), -17.0),
    "ignara_death": (death, 1, (2.8, 0.45), -14.0),
    "ignara_awaken": (awaken, 1, (2.6, 0.4), -14.0),
    "ignara_call": (call, 2, (2.6, 0.45), -15.0),
    "ignara_lash": (lash, 3, (1.0, 0.25), -15.0),
    "ignara_swing": (swing, 3, (0.8, 0.15), -19.0),
    "ignara_wave": (wave, 2, (1.8, 0.3), -13.0),
    "ignara_pillar": (pillar, 3, (1.6, 0.3), -14.0),
    "ignara_charge": (charge, 2, (1.2, 0.25), -18.0),
    "ignara_shard": (shard, 4, (1.0, 0.25), -18.0),
    "ignara_phoenix": (phoenix, 1, (2.4, 0.4), -13.0),
    "ignara_dive": (dive, 2, (1.2, 0.25), -15.0),
    "flame_altar_ignite": (ignite, 2, (2.0, 0.35), -16.0),
    "flame_whip_crack": (whip_crack, 3, (0.8, 0.2), -16.0),
}

# Ereignis: (Klang, Reichweite)
EVENTS = {
    "entity.ignara.ambient": ("ignara_ambient", 24),
    "entity.ignara.pray": ("ignara_pray", 32),
    "entity.ignara.hurt": ("ignara_hurt", 32),
    "entity.ignara.death": ("ignara_death", 64),
    "entity.ignara.awaken": ("ignara_awaken", 64),
    "entity.ignara.call": ("ignara_call", 64),
    "entity.ignara.lash": ("ignara_lash", 40),
    "entity.ignara.swing": ("ignara_swing", 32),
    "entity.ignara.wave": ("ignara_wave", 48),
    "entity.ignara.pillar": ("ignara_pillar", 48),
    "entity.ignara.charge": ("ignara_charge", 32),
    "entity.ignara.shard": ("ignara_shard", 24),
    "entity.ignara.phoenix": ("ignara_phoenix", 64),
    "entity.ignara.dive": ("ignara_dive", 48),
    "block.flame_altar.ignite": ("flame_altar_ignite", 32),
    "item.flame_whip.crack": ("flame_whip_crack", 24),
}

# sehr tiefe Klänge bekommen Obertöne ihres Basses, damit man sie auch auf kleinen Lautsprechern hört
AUDIBLE = {"ignara_wave", "ignara_pillar"}


def files_of(name: str) -> list[str]:
    return [f"kingdomsatwar:entity/nether/{name}{i + 1}" for i in range(SYNTH[name][1])]


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    for i in range(count):
        x = fn(9100 + i * 37 + zlib.crc32(name.encode()) % 997, i)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 2300 + i, stereo=False, damping=6500), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.05)
        if name in AUDIBLE:
            x = audible_bass(x, SR)
        x = soft_clip(fit_level(x, level, -2.0))
        write_ogg(x.astype(np.float32), SR, OUT / f"{name}{i + 1}.ogg", quality=5.0)
        if preview is not None:
            from ambience import spectrogram
            spectrogram(x, SR, preview / f"{name}{i + 1}.png", f"{name}{i + 1}")
    print(f"  {name}: {count} Varianten")
    return files_of(name)


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
    for key, (name, reach) in EVENTS.items():
        entries = [{"name": f, "attenuation_distance": reach} for f in rendered[name]]
        events[key] = {"sounds": entries, "subtitle": f"subtitles.kingdomsatwar.{key}"}
    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8")) if SOUNDS_JSON.exists() else {}
    data.update(events)
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"{len(events)} Klangereignisse geschrieben")


if __name__ == "__main__":
    main()
