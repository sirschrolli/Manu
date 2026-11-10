#!/usr/bin/env python3
"""
Erzeugt die Klänge Schrollis des Schöpfers, seiner Seraphlinge, des Altars und des Zepters: Chöre (schwebende, verstimmte
Stimmen durch Vokalformanten), Glocken, kristallene Funken, Lichtsäulen, Strahlen, der Sog der Weltkugel, das Anhalten und
Weiterlaufen der Zeit, „Es werde Licht!“, die Supernova, die Welten und ihre Einschläge. Alles entsteht aus Rauschen und
Tönen (siehe ambience_dsp.py); es werden keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/creator_sounds.py [--only name ...]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/creator/<name><n>.ogg
    Einträge "entity.schrolli.*", "entity.seraphling.*", "block.creator_altar.*", "item.creator_scepter.cast" in sounds.json
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, bell, env_ad, env_asr, fade_edges, filt, fit_level, glide, rng, room, saw,  # noqa: E402
                          soft_clip, tone, trim_tail)
from dsp import write_ogg  # noqa: E402
from star_sounds import choir, chime, crystal, horn, mix_at, noise, shimmer, thud, whoosh  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/creator"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100

# Akkorde (Hz): C-Dur, a-Moll, F-Dur, G-Dur, D-Dur (je in der mittleren Lage)
CHORDS = [
    [261.6, 329.6, 392.0, 523.3],
    [220.0, 261.6, 329.6, 440.0],
    [174.6, 220.0, 261.6, 349.2],
    [196.0, 246.9, 293.7, 392.0],
    [293.7, 370.0, 440.0, 587.3],
]


def n_of(seconds: float) -> int:
    return int(seconds * SR)


def env_swell(n: int, attack: float, release: float, peak: float = 0.5) -> np.ndarray:
    t = np.linspace(0.0, 1.0, n)
    up = np.clip(t / max(1e-3, peak), 0, 1) ** attack
    down = np.clip((1.0 - t) / max(1e-3, 1.0 - peak), 0, 1) ** release
    return np.minimum(up, down)


def tape(n: int, f0: float, f1: float, seed: int) -> np.ndarray:
    """Ein Bandstopp/-start: ein Akkord, dessen Tonhöhe abwärts (oder aufwärts) gleitet."""
    y = np.zeros(n)
    for f in (f0, f0 * 1.26, f0 * 1.5):
        y += saw(glide(n, f, f * f1 / f0, 2.0 if f1 < f0 else 0.5), np.ones(n), SR, partials=14, rolloff=1.4)
    return filt(y, "low", 3000, SR, 2) * 0.4 + filt(noise(n, seed), "band", [300, 2000], SR, 2) * 0.05


# =====================================================================================
# Schrolli
# =====================================================================================

def ambient(seed: int) -> np.ndarray:
    n = n_of(2.6)
    chord = CHORDS[seed % 3]
    y = choir(n, chord[:3], seed, "o") * env_swell(n, 1.5, 1.2, 0.45) * 0.8
    y += shimmer(n, seed + 1, 30.0) * 0.08
    return y


def hurt(seed: int) -> np.ndarray:
    n = n_of(1.0)
    y = chime(880.0 * (1.0 + (seed % 3) * 0.06), 1.0, seed, 3.0) * 0.6
    y += chime(1318.5, 1.0, seed + 1, 4.0) * 0.3
    c = choir(n, [392.0, 466.2], seed, "a")
    y[:n] += c * env_ad(n, SR, 0.01, 0.4) * 0.5
    return y


def death(seed: int) -> np.ndarray:
    n = n_of(6.5)
    y = np.zeros(n)
    for f in CHORDS[0]:
        y += choir(n, [f], seed + int(f), "a") * 0.3
    t = np.linspace(0, 1, n)
    pitch = np.clip(1.0 - t * 0.3, 0.6, 1.0)
    y = y * (1.0 - t) ** 1.2
    y += shimmer(n, seed + 3, 50.0) * 0.15 * (1.0 - t)
    mix_at(y, chime(130.8, 5.0, seed + 5, 0.6) * 0.8, 0.0)
    mix_at(y, whoosh(n_of(2.0), seed + 7, 4000, 300) * env_ad(n_of(2.0), SR, 1.6, 0.4) * 0.3, 3.8)
    return y * (0.7 + 0.3 * pitch)


def awaken(seed: int) -> np.ndarray:
    n = n_of(5.2)
    y = np.zeros(n)
    t = np.linspace(0, 1, n)
    for k, f in enumerate([130.8, 196.0, 261.6, 329.6, 392.0, 523.3]):
        voice = choir(n, [f], seed + k, "a") * np.clip((t - k * 0.06) * 2.2, 0, 1) ** 1.5
        y += voice * 0.22
    y += shimmer(n, seed + 9, 80.0) * 0.18 * t
    h = horn(n_of(1.6), 523.3, 523.3, seed)
    mix_at(y, h * 0.35, 3.4)
    mix_at(y, chime(261.6, 2.0, seed + 11, 1.0) * 0.9, 3.4)
    mix_at(y, thud(n_of(1.6), 70, 40, 1.2) * 0.8, 3.4)
    return y


def ascend(seed: int) -> np.ndarray:
    n = n_of(6.4)
    t = np.linspace(0, 1, n)
    y = np.zeros(n)
    for k, f in enumerate([110.0, 164.8, 220.0, 277.2, 329.6, 440.0]):
        gl = glide(n, f, f * 2.0, 1.6)
        y += saw(gl, np.ones(n), SR, partials=16, rolloff=1.3) * 0.06 * np.clip(t * 1.4, 0, 1)
        y += choir(n, [f * 2], seed + k, "a") * 0.12 * t ** 1.5
    rumble = filt(noise(n, seed + 4), "low", 120, SR, 2) * t ** 2 * 1.6
    y += rumble + shimmer(n, seed + 6, 120.0) * 0.25 * t
    y = filt(y, "high", 40, SR, 2)
    boom = thud(n_of(2.6), 90, 30, 2.0) * 1.4 + filt(noise(n_of(2.6), seed + 8), "low", 900, SR, 2) * env_ad(n_of(2.6), SR, 0.005, 1.4)
    mix_at(y, boom, 5.0)
    mix_at(y, chime(523.3, 2.4, seed + 10, 0.9) * 0.8, 5.0)
    return y


def voice(seed: int) -> np.ndarray:
    n = n_of(1.6)
    chord = CHORDS[seed % 5]
    return choir(n, chord, seed, "a" if seed % 2 else "o") * env_swell(n, 1.0, 1.6, 0.3) + shimmer(n, seed + 1, 40.0) * 0.06


def spear(seed: int) -> np.ndarray:
    f = 1760.0 * (1.0 + (seed % 5) * 0.08)
    return crystal(f, 0.6, seed, 2.5) * 0.7 + chime(f / 2, 0.6, seed + 1, 3.0) * 0.4


def spear_fly(seed: int) -> np.ndarray:
    n = n_of(0.7)
    return whoosh(n, seed, 500, 3200, 0.7) * env_ad(n, SR, 0.05, 0.6) * 1.3 + shimmer(n, seed + 2, 60.0) * 0.12


def pillar(seed: int) -> np.ndarray:
    n = n_of(1.8)
    y = np.zeros(n)
    y += filt(noise(n, seed), "low", 1600, SR, 2) * env_ad(n, SR, 0.004, 0.8) * 1.1
    y += thud(n, 110, 50, 0.7) * 0.8
    y += chime(523.3 * (1.0 + 0.05 * (seed % 3)), 1.8, seed + 1, 1.6) * 0.6
    y += choir(n, [523.3, 659.3, 784.0], seed + 2, "a") * env_ad(n, SR, 0.01, 0.9) * 0.35
    y += shimmer(n, seed + 3, 90.0) * 0.18
    return y


def charge(seed: int) -> np.ndarray:
    n = n_of(1.9)
    t = np.linspace(0, 1, n)
    y = tone(glide(n, 220.0, 880.0, 1.5), t ** 1.4, SR, harmonics=((1, 1.0), (2, 0.4), (3, 0.15))) * 0.3
    y += choir(n, [329.6, 392.0, 523.3], seed, "e") * t ** 1.8 * 0.6
    y += shimmer(n, seed + 1, 140.0) * 0.25 * t
    return y * env_asr(n, SR, 0.05, 0.08)


def beam(seed: int) -> np.ndarray:
    n = n_of(1.9)
    t = np.arange(n) / SR
    f = 220.0 * (1.0 + 0.004 * np.sin(2 * np.pi * 6.0 * t))
    y = filt(saw(f, np.ones(n), SR, partials=30, rolloff=0.9), "band", [400, 5000], SR, 2) * 0.35
    y += choir(n, [440.0, 554.4, 659.3], seed, "e") * 0.4
    y += shimmer(n, seed + 1, 200.0) * 0.2
    crackle = filt(noise(n, seed + 2), "high", 4000, SR, 2) * (rng(seed).random(n) > 0.995) * 3.0
    return (y + crackle) * env_asr(n, SR, 0.08, 0.3)


def smite(seed: int) -> np.ndarray:
    n = n_of(1.4)
    y = thud(n, 80, 35, 0.9) * 1.4
    y += filt(noise(n, seed), "band", [200, 3000], SR, 2) * env_ad(n, SR, 0.002, 0.35) * 1.0
    y += crystal(1200.0, 1.4, seed + 1, 1.0) * 0.5
    y += chime(261.6, 1.4, seed + 2, 1.5) * 0.6
    return y


def halo(seed: int) -> np.ndarray:
    n = n_of(1.3)
    t = np.arange(n) / SR
    am = 0.5 + 0.5 * np.sin(2 * np.pi * (18.0 + 6.0 * t) * t)
    y = tone(np.full(n, 660.0), am, SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.3), (5, 0.1))) * 0.35
    y += whoosh(n, seed, 800, 2500, 0.8) * am * 0.6
    y += shimmer(n, seed + 1, 80.0) * 0.15
    return y * env_asr(n, SR, 0.05, 0.3)


def orb(seed: int) -> np.ndarray:
    n = n_of(2.4)
    t = np.arange(n) / SR
    lfo = 0.6 + 0.4 * np.sin(2 * np.pi * 3.0 * t)
    y = saw(glide(n, 110.0, 55.0, 1.0), lfo, SR, partials=20, rolloff=1.2)
    y = filt(y, "low", 900, SR, 2) * 0.5
    y += filt(noise(n, seed), "low", 300, SR, 2) * lfo * 0.6
    y += whoosh(n, seed + 1, 3000, 400, 0.6) * 0.3
    return y * env_asr(n, SR, 0.2, 0.6)


def blast(seed: int) -> np.ndarray:
    n = n_of(1.5)
    y = filt(noise(n, seed), "low", 5000, SR, 2) * env_ad(n, SR, 0.002, 0.45) * 1.2
    y += thud(n, 120, 45, 0.6) * 0.7
    y += crystal(2000.0, 1.5, seed + 1, 1.2) * 0.4
    y += shimmer(n, seed + 2, 150.0) * 0.25
    return y


def wings(seed: int) -> np.ndarray:
    n = n_of(1.0)
    y = np.zeros(n)
    for k in range(2):
        s = n_of(0.25 * k)
        m = n_of(0.5)
        w = whoosh(m, seed + k, 150, 900, 1.0) * env_ad(m, SR, 0.08, 0.35)
        mix_at(y, w * 1.4, s / SR)
    return y + shimmer(n, seed + 5, 20.0) * 0.05


def feathers(seed: int) -> np.ndarray:
    n = n_of(1.1)
    y = np.zeros(n)
    r = rng(seed)
    for k in range(9):
        m = n_of(0.3)
        w = whoosh(m, seed + k, r.uniform(800, 1500), r.uniform(2500, 5000), 0.6) * env_ad(m, SR, 0.02, 0.2)
        mix_at(y, w * 0.6, r.uniform(0.0, 0.7))
    return y + shimmer(n, seed + 20, 60.0) * 0.12


def blink(seed: int) -> np.ndarray:
    n = n_of(0.8)
    t = np.linspace(0, 1, n)
    y = filt(noise(n, seed), "high", 3000, SR, 2) * t ** 3 * 0.5
    mix_at(y, crystal(2637.0, 0.4, seed + 1, 3.0) * 0.7, 0.55)
    return y


def choir_ring(seed: int) -> np.ndarray:
    n = n_of(1.7)
    return choir(n, CHORDS[(seed + 3) % 5], seed, "a") * env_swell(n, 0.8, 1.4, 0.25) * 0.9 + shimmer(n, seed + 1, 60.0) * 0.1


def judgment(seed: int) -> np.ndarray:
    n = n_of(3.4)
    y = bell(65.4, 3.4, SR, ratios=(1.0, 2.0, 2.4, 3.0, 4.2, 5.4), amps=(1.0, 0.6, 0.5, 0.35, 0.2, 0.12), decay=0.5, seed=seed) * 1.2
    y += choir(n, [130.8, 196.0, 261.6], seed + 1, "o") * env_ad(n, SR, 0.3, 2.4) * 0.3
    y += thud(n, 60, 40, 1.5) * 0.4
    return y


def timestop(seed: int) -> np.ndarray:
    n = n_of(2.2)
    t = np.linspace(0, 1, n)
    y = filt(noise(n, seed), "high", 2500, SR, 2) * t ** 4 * 0.6
    y += tape(n, 440.0, 110.0, seed + 1) * (1.0 - t) ** 0.5 * 0.8
    mix_at(y, bell(98.0, 1.6, SR, decay=0.8, seed=seed + 2) * 0.8, 0.4)
    y += choir(n, [220.0, 233.1], seed + 3, "o") * t * 0.2
    return y


def time_resume(seed: int) -> np.ndarray:
    n = n_of(1.5)
    t = np.linspace(0, 1, n)
    y = tape(n, 110.0, 440.0, seed) * np.clip(t * 3, 0, 1) * 0.7
    y += whoosh(n, seed + 1, 300, 4000, 0.6) * t * 0.5
    mix_at(y, crystal(1760.0, 0.6, seed + 2, 2.0) * 0.6, 0.9)
    return y


def tick(seed: int) -> np.ndarray:
    n = n_of(0.35)
    click = filt(noise(n, seed), "band", [2000, 6000], SR, 2) * env_ad(n, SR, 0.0005, 0.015) * 1.5
    wood = tone(np.full(n, 1100.0 if seed % 2 else 1300.0), env_ad(n, SR, 0.001, 0.06), SR, harmonics=((1, 1.0), (2.7, 0.4)))
    return click + wood * 0.5


def word(seed: int) -> np.ndarray:
    n = n_of(3.0)
    y = np.zeros(n)
    for k, f in enumerate([261.6, 329.6, 392.0, 523.3, 659.3, 784.0, 1046.5]):
        y += choir(n, [f], seed + k, "a") * 0.2
    y *= env_ad(n, SR, 0.02, 2.4)
    y += shimmer(n, seed + 10, 260.0) * 0.4 * env_ad(n, SR, 0.01, 2.0)
    y += filt(noise(n, seed + 11), "high", 6000, SR, 2) * env_ad(n, SR, 0.005, 0.8) * 0.4
    mix_at(y, chime(1046.5, 2.5, seed + 12, 1.0) * 0.7, 0.0)
    return y


def supernova(seed: int) -> np.ndarray:
    n = n_of(5.5)
    y = thud(n, 70, 22, 3.0) * 1.8
    y += filt(noise(n, seed), "low", 2500, SR, 2) * env_ad(n, SR, 0.003, 2.2) * 1.5
    y += filt(noise(n, seed + 1), "high", 3000, SR, 2) * env_ad(n, SR, 0.002, 0.7) * 0.6
    y += choir(n, [261.6, 392.0, 523.3, 784.0], seed + 2, "a") * env_ad(n, SR, 0.05, 3.5) * 0.4
    y += shimmer(n, seed + 3, 200.0) * env_ad(n, SR, 0.2, 4.5) * 0.4
    return y


def planet(seed: int) -> np.ndarray:
    n = n_of(1.9)
    t = np.linspace(0, 1, n)
    y = filt(noise(n, seed), "low", 200, SR, 2) * (0.5 + t) * 0.8
    y += tone(glide(n, 110.0, 330.0, 0.8), np.clip(t * 2, 0, 1), SR, harmonics=((1, 1.0), (1.5, 0.4), (2, 0.3))) * 0.25
    mix_at(y, chime(659.3 + 60 * (seed % 3), 1.0, seed + 1, 2.0) * 0.5, 1.2)
    return y * env_asr(n, SR, 0.1, 0.4)


def impact_heavy(seed: int) -> np.ndarray:
    n = n_of(1.8)
    y = thud(n, 65, 28, 1.2) * 1.8
    y += filt(noise(n, seed), "low", 1500, SR, 2) * env_ad(n, SR, 0.002, 0.7) * 1.3
    r = rng(seed)
    for _ in range(14):
        m = n_of(0.05)
        crack = filt(noise(m, int(r.integers(1, 1 << 30))), "band", [800, 5000], SR, 2) * env_ad(m, SR, 0.001, 0.03)
        mix_at(y, crack * r.uniform(0.2, 0.6), r.uniform(0.05, 1.0))
    return y


def star(seed: int) -> np.ndarray:
    n = n_of(1.1)
    y = tone(glide(n_of(0.5), 2600.0, 900.0, 1.0), env_asr(n_of(0.5), SR, 0.05, 0.1), SR, harmonics=((1, 1.0), (2, 0.3))) * 0.2
    y = np.concatenate([y, np.zeros(n - len(y))])
    mix_at(y, crystal(1568.0 + 100 * (seed % 3), 0.7, seed, 1.6) * 0.8, 0.45)
    mix_at(y, filt(noise(n_of(0.4), seed + 1), "band", [1000, 6000], SR, 2) * env_ad(n_of(0.4), SR, 0.002, 0.15), 0.45)
    return y


def stun(seed: int) -> np.ndarray:
    n = n_of(1.7)
    y = choir(n, [392.0, 466.2], seed, "a") * env_ad(n, SR, 0.01, 1.0) * 0.4
    y += tape(n, 330.0, 110.0, seed + 1) * 0.4 * env_ad(n, SR, 0.01, 1.2)
    mix_at(y, thud(n_of(1.0), 90, 40, 0.6) * 1.2, 0.8)
    return y


def heal(seed: int) -> np.ndarray:
    n = n_of(1.7)
    y = np.zeros(n)
    for k, f in enumerate([523.3, 659.3, 784.0, 1046.5]):
        mix_at(y, chime(f, 1.2, seed + k, 2.0) * 0.4, k * 0.12)
    y += choir(n, [261.6, 329.6, 392.0], seed + 9, "o") * env_swell(n, 1.0, 1.4, 0.4) * 0.4
    return y


# =====================================================================================
# Seraphlinge, Altar, Zepter
# =====================================================================================

def seraph_ambient(seed: int) -> np.ndarray:
    n = n_of(1.0)
    f = 784.0 * (1.0 + 0.06 * (seed % 3))
    return choir(n, [f, f * 1.25], seed, "e") * env_swell(n, 1.0, 1.5, 0.3) * 0.7 + shimmer(n, seed + 1, 40.0) * 0.1


def seraph_hurt(seed: int) -> np.ndarray:
    return crystal(2093.0 * (1.0 + 0.05 * (seed % 3)), 0.5, seed, 3.0) * 0.8


def seraph_death(seed: int) -> np.ndarray:
    n = n_of(1.1)
    t = np.linspace(0, 1, n)
    return shimmer(n, seed, 200.0) * (1.0 - t) * 0.6 + tone(glide(n, 880.0, 1760.0, 1.0), (1.0 - t) ** 2, SR) * 0.2


def seraph_shoot(seed: int) -> np.ndarray:
    n = n_of(0.4)
    return crystal(2637.0 * (1.0 + 0.04 * (seed % 3)), 0.4, seed, 4.0) * 0.5 + whoosh(n, seed + 1, 1500, 5000, 0.6) * env_ad(n, SR, 0.01, 0.25) * 0.5


def altar_awaken(seed: int) -> np.ndarray:
    n = n_of(4.4)
    t = np.linspace(0, 1, n)
    y = choir(n, [196.0, 293.7, 392.0, 587.3], seed, "a") * t ** 1.3 * 0.7
    for k in range(6):
        mix_at(y, chime(523.3 * (1.0 + k * 0.125), 1.2, seed + k, 1.8) * 0.3, 0.5 + k * 0.55)
    y += shimmer(n, seed + 20, 90.0) * 0.2 * t
    return y


def altar_hum(seed: int) -> np.ndarray:
    n = n_of(2.0)
    t = np.arange(n) / SR
    f = 130.8 * (1.0 + 0.004 * np.sin(2 * np.pi * 0.7 * t))
    return tone(f, env_swell(n, 1.0, 1.0, 0.5), SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.3), (4, 0.15), (6, 0.08))) * 0.6 \
        + shimmer(n, seed, 20.0) * 0.06


def scepter_cast(seed: int) -> np.ndarray:
    n = n_of(0.7)
    return crystal(1975.5, 0.7, seed, 2.0) * 0.5 + whoosh(n, seed + 1, 600, 4000, 0.7) * env_ad(n, SR, 0.02, 0.4) * 0.8


# name: (Erzeuger, Varianten, Raum (Nachhall, Anteil) oder None, Lautstärke (RMS dB), Dateistamm)
SYNTH = {
    "ambient": (ambient, 3, (2.4, 0.4), -24.0),
    "hurt": (hurt, 3, (1.6, 0.3), -17.0),
    "death": (death, 1, (3.6, 0.5), -14.0),
    "awaken": (awaken, 1, (3.4, 0.45), -13.0),
    "ascend": (ascend, 1, (3.6, 0.45), -12.0),
    "voice": (voice, 3, (2.8, 0.45), -17.0),
    "spear": (spear, 3, (1.6, 0.3), -18.0),
    "spear_fly": (spear_fly, 3, (1.0, 0.2), -18.0),
    "pillar": (pillar, 3, (2.2, 0.35), -14.0),
    "charge": (charge, 2, (1.8, 0.3), -17.0),
    "beam": (beam, 2, (1.6, 0.3), -16.0),
    "smite": (smite, 2, (2.0, 0.35), -13.0),
    "halo": (halo, 2, (1.4, 0.25), -17.0),
    "orb": (orb, 2, (2.0, 0.35), -15.0),
    "blast": (blast, 3, (2.0, 0.35), -14.0),
    "wings": (wings, 3, (1.2, 0.25), -15.0),
    "feathers": (feathers, 2, (1.2, 0.25), -17.0),
    "blink": (blink, 2, (1.4, 0.3), -17.0),
    "choir": (choir_ring, 3, (2.6, 0.45), -16.0),
    "judgment": (judgment, 2, (3.4, 0.45), -13.0),
    "timestop": (timestop, 1, (2.8, 0.45), -14.0),
    "time_resume": (time_resume, 1, (2.0, 0.35), -15.0),
    "tick": (tick, 2, (1.2, 0.3), -18.0),
    "word": (word, 1, (3.4, 0.5), -12.0),
    "supernova": (supernova, 1, (3.8, 0.5), -11.0),
    "planet": (planet, 2, (2.2, 0.35), -16.0),
    "impact": (impact_heavy, 3, (2.4, 0.35), -12.0),
    "star": (star, 3, (1.6, 0.3), -16.0),
    "stun": (stun, 1, (2.4, 0.4), -15.0),
    "heal": (heal, 2, (2.4, 0.4), -17.0),
    "seraph_ambient": (seraph_ambient, 3, (1.6, 0.35), -22.0),
    "seraph_hurt": (seraph_hurt, 3, (1.2, 0.3), -18.0),
    "seraph_death": (seraph_death, 1, (1.6, 0.35), -17.0),
    "seraph_shoot": (seraph_shoot, 3, (1.0, 0.25), -19.0),
    "altar_awaken": (altar_awaken, 1, (3.0, 0.45), -14.0),
    "altar_hum": (altar_hum, 2, (2.0, 0.35), -20.0),
    "scepter_cast": (scepter_cast, 3, (1.2, 0.25), -17.0),
}

# Ereignis: (Klang, Reichweite)
EVENTS = {f"entity.schrolli.{name}": (name, reach) for name, reach in (
    ("ambient", 48), ("hurt", 48), ("death", 160), ("awaken", 160), ("ascend", 160), ("voice", 160), ("spear", 48),
    ("spear_fly", 48), ("pillar", 64), ("charge", 96), ("beam", 96), ("smite", 80), ("halo", 64), ("orb", 80), ("blast", 80),
    ("wings", 64), ("feathers", 64), ("blink", 64), ("choir", 96), ("judgment", 128), ("timestop", 160), ("time_resume", 160),
    ("tick", 96), ("word", 160), ("supernova", 200), ("planet", 96), ("impact", 96), ("star", 64), ("stun", 96), ("heal", 96))}
EVENTS.update({
    "entity.seraphling.ambient": ("seraph_ambient", 24), "entity.seraphling.hurt": ("seraph_hurt", 24),
    "entity.seraphling.death": ("seraph_death", 32), "entity.seraphling.shoot": ("seraph_shoot", 32),
    "block.creator_altar.awaken": ("altar_awaken", 64), "block.creator_altar.hum": ("altar_hum", 24),
    "item.creator_scepter.cast": ("scepter_cast", 24),
})


def files_of(name: str) -> list[str]:
    return [f"kingdomsatwar:creator/{name}{i + 1}" for i in range(SYNTH[name][1])]


def render(name: str) -> list[str]:
    fn, count, space, level = SYNTH[name]
    files = []
    for i in range(count):
        x = fn(7000 + i * 41 + zlib.crc32(name.encode()) % 991)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 2300 + i, stereo=False, damping=7500), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.06)
        x = soft_clip(fit_level(x, level, -2.0))
        path = OUT / f"{name}{i + 1}.ogg"
        write_ogg(x.astype(np.float32), SR, path, quality=5.0)
        files.append(f"kingdomsatwar:creator/{name}{i + 1}")
    print(f"  {name}: {count} Varianten")
    return files


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--only", nargs="*", help="nur diese Klänge neu erzeugen (die übrigen Dateien bleiben)")
    args = ap.parse_args()
    OUT.mkdir(parents=True, exist_ok=True)
    rendered = {name: render(name) if not args.only or name in args.only else files_of(name) for name in SYNTH}
    events = {}
    for key, (sound, reach) in EVENTS.items():
        events[key] = {"sounds": [{"name": f, "attenuation_distance": reach} for f in rendered[sound]],
                       "subtitle": f"subtitles.kingdomsatwar.{key}"}
    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8")) if SOUNDS_JSON.exists() else {}
    data.update(events)
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"{len(events)} Klangereignisse geschrieben")


if __name__ == "__main__":
    main()
