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
from star_sounds import choir, horn, mix_at, noise, shimmer, thud, whoosh  # noqa: E402

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
# Warme Bausteine (seit 0.76): tiefe Glocken, dunkler Chor, weiches Glas, Luft statt Zischen – nichts Grelles
# =====================================================================================

def lp(y: np.ndarray, f: float = 3000.0, order: int = 2) -> np.ndarray:
    return filt(y, "low", f, SR, order)


def dark_choir(n: int, freqs: list[float], seed: int, vowel: str = "o") -> np.ndarray:
    """Ein dunkler, weicher Chor: die Stimmen tief gefiltert, darunter ein runder Grundton."""
    c = choir(n, freqs, seed, vowel)
    body = np.zeros(n)
    for f in freqs:
        body += tone(np.full(n, f), np.ones(n), SR, harmonics=((1, 1.0), (2, 0.18)))
    return lp(c, 1700.0) * 0.9 + body / max(1, len(freqs)) * 0.12


def warm_bell(f0: float, dur: float, seed: int, decay: float = 1.0) -> np.ndarray:
    """Eine tiefe, runde Glocke (fast harmonisch, die hohen Teiltöne leise)."""
    return lp(bell(f0, dur, SR, ratios=(1.0, 2.0, 3.0, 4.2), amps=(1.0, 0.3, 0.1, 0.04), decay=decay, seed=seed), 2400.0)


def soft_glass(f0: float, dur: float, seed: int, decay: float = 1.8) -> np.ndarray:
    """Weiches Glas in der Mittellage (statt splitternder Kristalle)."""
    return lp(bell(f0, dur, SR, ratios=(1.0, 2.0, 2.97), amps=(1.0, 0.16, 0.04), decay=decay, seed=seed), 2000.0)


def air(n: int, seed: int, f0: float, f1: float, q: float = 0.9) -> np.ndarray:
    """Ein Luftzug in tiefer und mittlerer Lage."""
    return lp(whoosh(n, seed, f0, f1, q), 1700.0)


def glow(n: int, seed: int, density: float = 16.0) -> np.ndarray:
    """Ein leises, warmes Funkeln (nicht über 3,5 kHz)."""
    return lp(shimmer(n, seed, density, 1200.0, 3400.0), 3000.0) * 0.35


def low_metal(f0: float, dur: float, seed: int, decay: float = 0.3) -> np.ndarray:
    """Gedämpftes Metall: eine Klinge, die auf etwas trifft (unharmonisch, aber rasch und tief gefiltert)."""
    return lp(bell(f0, dur, SR, ratios=(1.0, 2.41, 3.87, 5.2), amps=(1.0, 0.45, 0.2, 0.08), decay=decay, seed=seed), 1900.0)


def tape(n: int, f0: float, f1: float, seed: int) -> np.ndarray:
    """Ein Bandstopp/-start: ein Akkord, dessen Tonhöhe gleitet (weich gefiltert)."""
    y = np.zeros(n)
    for f in (f0, f0 * 1.26, f0 * 1.5):
        y += saw(glide(n, f, f * f1 / f0, 2.0 if f1 < f0 else 0.5), np.ones(n), SR, partials=10, rolloff=1.6)
    return lp(y, 1300.0) * 0.4 + lp(filt(noise(n, seed), "band", [150, 900], SR, 2), 1000.0) * 0.04


# Akkorde eine Oktave tiefer als früher (C, a, F, G, D)
LOW_CHORDS = [[f * 0.5 for f in chord] for chord in CHORDS]


# =====================================================================================
# Schrolli
# =====================================================================================

def ambient(seed: int) -> np.ndarray:
    n = n_of(2.8)
    chord = LOW_CHORDS[seed % 3][:3]
    return dark_choir(n, chord, seed, "o") * env_swell(n, 1.6, 1.3, 0.45) * 0.8 + glow(n, seed + 1, 6.0)


def hurt(seed: int) -> np.ndarray:
    n = n_of(0.9)
    f = 196.0 * (1.0 + (seed % 3) * 0.06)
    y = thud(n, 95, 55, 0.25) * 0.7
    mix_at(y, low_metal(f, 0.9, seed, 0.35) * 0.55, 0.0)
    y += dark_choir(n, [f * 0.75, f * 0.9], seed + 1, "o") * env_ad(n, SR, 0.01, 0.35) * 0.35
    return y


def death(seed: int) -> np.ndarray:
    n = n_of(6.5)
    t = np.linspace(0, 1, n)
    y = dark_choir(n, [65.4, 98.0, 130.8, 196.0], seed, "o") * (1.0 - t) ** 1.1 * 0.8
    y += glow(n, seed + 3, 12.0) * (1.0 - t)
    mix_at(y, warm_bell(65.4, 5.0, seed + 5, 0.6) * 0.9, 0.0)
    mix_at(y, air(n_of(2.2), seed + 7, 900, 160) * env_ad(n_of(2.2), SR, 1.6, 0.5) * 0.4, 3.6)
    return y


def awaken(seed: int) -> np.ndarray:
    n = n_of(5.2)
    t = np.linspace(0, 1, n)
    y = np.zeros(n)
    for k, f in enumerate([65.4, 98.0, 130.8, 164.8, 196.0, 261.6]):
        y += dark_choir(n, [f], seed + k, "o") * np.clip((t - k * 0.06) * 2.2, 0, 1) ** 1.5 * 0.24
    y += glow(n, seed + 9, 14.0) * t
    mix_at(y, lp(horn(n_of(1.6), 261.6, 261.6, seed), 1400.0) * 0.3, 3.4)
    mix_at(y, warm_bell(130.8, 2.0, seed + 11, 1.0) * 0.9, 3.4)
    mix_at(y, thud(n_of(1.6), 70, 38, 1.2) * 0.9, 3.4)
    return y


def ascend(seed: int) -> np.ndarray:
    n = n_of(6.4)
    t = np.linspace(0, 1, n)
    y = np.zeros(n)
    for k, f in enumerate([55.0, 82.4, 110.0, 138.6, 164.8, 220.0]):
        gl = glide(n, f, f * 2.0, 1.6)
        y += lp(saw(gl, np.ones(n), SR, partials=10, rolloff=1.6), 1200.0) * 0.07 * np.clip(t * 1.4, 0, 1)
        y += dark_choir(n, [f * 2], seed + k, "o") * 0.12 * t ** 1.5
    y += filt(noise(n, seed + 4), "low", 110, SR, 2) * t ** 2 * 1.6
    y += glow(n, seed + 6, 18.0) * t
    y = filt(y, "high", 35, SR, 2)
    boom = thud(n_of(2.6), 85, 28, 2.0) * 1.4 + lp(noise(n_of(2.6), seed + 8), 600.0) * env_ad(n_of(2.6), SR, 0.008, 1.4)
    mix_at(y, boom, 5.0)
    mix_at(y, warm_bell(261.6, 2.4, seed + 10, 0.9) * 0.7, 5.0)
    return y


def voice(seed: int) -> np.ndarray:
    n = n_of(1.6)
    chord = LOW_CHORDS[seed % 5]
    return dark_choir(n, chord, seed, "o") * env_swell(n, 1.0, 1.6, 0.3)


def spear(seed: int) -> np.ndarray:
    n = n_of(0.6)
    f = (494.0, 523.3, 587.3)[seed % 3]
    return soft_glass(f, 0.6, seed, 2.2) * 0.5 + thud(n, 150, 110, 0.12) * 0.25


def spear_fly(seed: int) -> np.ndarray:
    n = n_of(0.6)
    return air(n, seed, 240, 900, 0.7) * env_ad(n, SR, 0.03, 0.5)


def pillar(seed: int) -> np.ndarray:
    n = n_of(1.8)
    y = lp(noise(n, seed), 800.0) * env_ad(n, SR, 0.006, 0.7) * 1.0
    y += thud(n, 95, 45, 0.7) * 0.9
    mix_at(y, warm_bell(261.6 * (1.0 + 0.05 * (seed % 3)), 1.8, seed + 1, 1.4) * 0.45, 0.0)
    y += dark_choir(n, [261.6, 329.6, 392.0], seed + 2, "o") * env_ad(n, SR, 0.02, 0.9) * 0.3
    return y


def charge(seed: int) -> np.ndarray:
    n = n_of(1.9)
    t = np.linspace(0, 1, n)
    y = lp(tone(glide(n, 110.0, 330.0, 1.5), t ** 1.4, SR, harmonics=((1, 1.0), (2, 0.3), (3, 0.08))), 1500.0) * 0.35
    y += dark_choir(n, [164.8, 196.0, 261.6], seed, "o") * t ** 1.8 * 0.6
    y += glow(n, seed + 1, 14.0) * t
    return y * env_asr(n, SR, 0.06, 0.1)


def beam(seed: int) -> np.ndarray:
    n = n_of(1.9)
    t = np.arange(n) / SR
    f = 110.0 * (1.0 + 0.004 * np.sin(2 * np.pi * 5.0 * t))
    y = lp(saw(f, np.ones(n), SR, partials=14, rolloff=1.4), 1100.0) * 0.4
    y += dark_choir(n, [220.0, 277.2, 329.6], seed, "o") * 0.4
    y += lp(filt(noise(n, seed + 2), "band", [200, 900], SR, 2), 1000.0) * 0.08
    return y * env_asr(n, SR, 0.1, 0.35)


def smite(seed: int) -> np.ndarray:
    n = n_of(1.4)
    y = thud(n, 75, 33, 0.9) * 1.4
    y += lp(noise(n, seed), 1100.0) * env_ad(n, SR, 0.004, 0.35) * 0.9
    mix_at(y, low_metal(220.0, 1.0, seed + 1, 0.5) * 0.35, 0.0)
    mix_at(y, warm_bell(130.8, 1.4, seed + 2, 1.2) * 0.5, 0.0)
    return y


def halo(seed: int) -> np.ndarray:
    n = n_of(1.3)
    t = np.arange(n) / SR
    am = 0.55 + 0.45 * np.sin(2 * np.pi * (12.0 + 4.0 * t) * t)
    y = lp(tone(np.full(n, 330.0), am, SR, harmonics=((1, 1.0), (2, 0.3), (3, 0.1))), 1400.0) * 0.35
    y += air(n, seed, 300, 900, 0.8) * am * 0.6
    return y * env_asr(n, SR, 0.06, 0.35)


def orb(seed: int) -> np.ndarray:
    n = n_of(2.4)
    t = np.arange(n) / SR
    lfo = 0.6 + 0.4 * np.sin(2 * np.pi * 2.5 * t)
    y = lp(saw(glide(n, 110.0, 55.0, 1.0), lfo, SR, partials=14, rolloff=1.4), 700.0) * 0.5
    y += filt(noise(n, seed), "low", 260, SR, 2) * lfo * 0.6
    y += air(n, seed + 1, 1200, 250, 0.6) * 0.3
    return y * env_asr(n, SR, 0.2, 0.6)


def blast(seed: int) -> np.ndarray:
    n = n_of(1.5)
    y = lp(noise(n, seed), 1400.0) * env_ad(n, SR, 0.005, 0.45) * 1.1
    y += thud(n, 110, 45, 0.6) * 0.8
    mix_at(y, soft_glass(392.0, 1.5, seed + 1, 1.4) * 0.3, 0.0)
    y += glow(n, seed + 2, 16.0)
    return y


def wings(seed: int) -> np.ndarray:
    n = n_of(1.0)
    y = np.zeros(n)
    for k in range(2):
        m = n_of(0.5)
        w = air(m, seed + k, 110, 520, 1.0) * env_ad(m, SR, 0.07, 0.35)
        mix_at(y, w * 1.4, 0.25 * k)
    return y


def feathers(seed: int) -> np.ndarray:
    n = n_of(1.0)
    y = np.zeros(n)
    r = rng(seed)
    for k in range(6):
        m = n_of(0.3)
        w = air(m, seed + k, r.uniform(350, 600), r.uniform(800, 1300), 0.6) * env_ad(m, SR, 0.03, 0.2)
        mix_at(y, w * 0.6, r.uniform(0.0, 0.6))
    return y


def blink(seed: int) -> np.ndarray:
    n = n_of(0.8)
    t = np.linspace(0, 1, n)
    y = air(n, seed, 300, 1200, 0.8) * t ** 2.5 * 0.7
    mix_at(y, soft_glass(659.3, 0.5, seed + 1, 2.4) * 0.35, 0.55)
    return y


def choir_ring(seed: int) -> np.ndarray:
    n = n_of(1.7)
    return dark_choir(n, LOW_CHORDS[(seed + 3) % 5], seed, "o") * env_swell(n, 0.8, 1.4, 0.25) * 0.9


def judgment(seed: int) -> np.ndarray:
    n = n_of(3.4)
    y = lp(bell(65.4, 3.4, SR, ratios=(1.0, 2.0, 2.4, 3.0, 4.2), amps=(1.0, 0.5, 0.35, 0.2, 0.08), decay=0.5, seed=seed), 1800.0) * 1.2
    y += dark_choir(n, [130.8, 196.0, 261.6], seed + 1, "o") * env_ad(n, SR, 0.3, 2.4) * 0.3
    y += thud(n, 60, 40, 1.5) * 0.4
    return y


def timestop(seed: int) -> np.ndarray:
    n = n_of(2.2)
    t = np.linspace(0, 1, n)
    y = tape(n, 220.0, 55.0, seed + 1) * (1.0 - t) ** 0.5 * 0.8
    mix_at(y, warm_bell(98.0, 1.6, seed + 2, 0.8) * 0.8, 0.4)
    y += dark_choir(n, [110.0, 116.5], seed + 3, "o") * t * 0.25
    y += lp(noise(n, seed), 500.0) * t ** 3 * 0.2
    return y


def time_resume(seed: int) -> np.ndarray:
    n = n_of(1.5)
    t = np.linspace(0, 1, n)
    y = tape(n, 55.0, 220.0, seed) * np.clip(t * 3, 0, 1) * 0.7
    y += air(n, seed + 1, 200, 900, 0.6) * t * 0.5
    mix_at(y, soft_glass(523.3, 0.6, seed + 2, 2.2) * 0.35, 0.9)
    return y


def tick(seed: int) -> np.ndarray:
    n = n_of(0.3)
    tock = tone(np.full(n, 480.0 if seed % 2 else 540.0), env_ad(n, SR, 0.002, 0.07), SR, harmonics=((1, 1.0), (2.4, 0.25)))
    knock = lp(noise(n, seed), 1200.0) * env_ad(n, SR, 0.001, 0.02) * 0.4
    return lp(tock * 0.6 + knock, 1800.0)


def word(seed: int) -> np.ndarray:
    n = n_of(3.0)
    y = np.zeros(n)
    for k, f in enumerate([130.8, 164.8, 196.0, 261.6, 329.6, 392.0, 523.3]):
        y += dark_choir(n, [f], seed + k, "a" if k > 3 else "o") * 0.22
    y *= env_ad(n, SR, 0.03, 2.4)
    y += glow(n, seed + 10, 30.0) * env_ad(n, SR, 0.02, 2.0) * 1.5
    mix_at(y, warm_bell(261.6, 2.5, seed + 12, 1.0) * 0.6, 0.0)
    return y


def supernova(seed: int) -> np.ndarray:
    n = n_of(5.5)
    y = thud(n, 68, 22, 3.0) * 1.8
    y += lp(noise(n, seed), 1300.0) * env_ad(n, SR, 0.006, 2.2) * 1.4
    y += dark_choir(n, [130.8, 196.0, 261.6, 392.0], seed + 2, "o") * env_ad(n, SR, 0.06, 3.5) * 0.45
    y += glow(n, seed + 3, 30.0) * env_ad(n, SR, 0.2, 4.5) * 1.5
    return y


def planet(seed: int) -> np.ndarray:
    n = n_of(1.9)
    t = np.linspace(0, 1, n)
    y = filt(noise(n, seed), "low", 180, SR, 2) * (0.5 + t) * 0.8
    y += lp(tone(glide(n, 82.4, 196.0, 0.8), np.clip(t * 2, 0, 1), SR, harmonics=((1, 1.0), (1.5, 0.35), (2, 0.25))), 1000.0) * 0.3
    mix_at(y, soft_glass(392.0 + 30 * (seed % 3), 1.0, seed + 1, 2.0) * 0.4, 1.2)
    return y * env_asr(n, SR, 0.1, 0.4)


def impact_heavy(seed: int) -> np.ndarray:
    n = n_of(1.8)
    y = thud(n, 62, 26, 1.2) * 1.8
    y += lp(noise(n, seed), 900.0) * env_ad(n, SR, 0.004, 0.7) * 1.3
    r = rng(seed)
    for _ in range(8):
        m = n_of(0.06)
        crack = lp(filt(noise(m, int(r.integers(1, 1 << 30))), "band", [300, 2000], SR, 2), 1800.0) * env_ad(m, SR, 0.002, 0.04)
        mix_at(y, crack * r.uniform(0.15, 0.4), r.uniform(0.05, 1.0))
    return y


def star(seed: int) -> np.ndarray:
    n = n_of(1.0)
    m = n_of(0.5)
    y = lp(tone(glide(m, 880.0, 260.0, 1.0), env_asr(m, SR, 0.05, 0.1), SR, harmonics=((1, 1.0), (2, 0.15))), 1500.0) * 0.18
    y = np.concatenate([y, np.zeros(n - len(y))])
    mix_at(y, soft_glass(523.3 + 40 * (seed % 3), 0.6, seed, 2.0) * 0.4, 0.45)
    mix_at(y, thud(n_of(0.4), 120, 70, 0.15) * 0.4, 0.45)
    return y


def stun(seed: int) -> np.ndarray:
    n = n_of(1.7)
    y = dark_choir(n, [196.0, 233.1], seed, "o") * env_ad(n, SR, 0.02, 1.0) * 0.4
    y += tape(n, 165.0, 55.0, seed + 1) * 0.4 * env_ad(n, SR, 0.02, 1.2)
    mix_at(y, thud(n_of(1.0), 85, 38, 0.6) * 1.2, 0.8)
    return y


def heal(seed: int) -> np.ndarray:
    n = n_of(1.7)
    y = np.zeros(n)
    for k, f in enumerate([261.6, 329.6, 392.0, 523.3]):
        mix_at(y, soft_glass(f, 1.2, seed + k, 2.2) * 0.3, k * 0.14)
    y += dark_choir(n, [130.8, 164.8, 196.0], seed + 9, "o") * env_swell(n, 1.0, 1.4, 0.4) * 0.4
    return y


# ---------------------------------------------------------------- Nahkampf (seit 0.76)

def blade(seed: int) -> np.ndarray:
    """Die Klinge der Schöpfung erscheint: ein Chor schwillt an, ein tiefes Klingen, ein Luftzug."""
    n = n_of(1.8)
    t = np.linspace(0, 1, n)
    y = dark_choir(n, [98.0, 146.8, 196.0], seed, "o") * env_swell(n, 1.2, 1.4, 0.35) * 0.55
    y += air(n, seed + 1, 250, 1100, 0.8) * np.clip(t * 4.0, 0, 1) * (1.0 - t) * 0.5
    mix_at(y, low_metal(196.0, 1.5, seed + 2, 1.4) * 0.4, 0.35)
    mix_at(y, warm_bell(98.0, 1.4, seed + 3, 0.9) * 0.6, 0.35)
    return y


def swing(seed: int) -> np.ndarray:
    """Ein Hieb mit der Klinge: ein schneller, tiefer Luftzug."""
    n = n_of(0.45)
    r = rng(seed)
    f0 = r.uniform(160, 220)
    return air(n, seed, f0, f0 * 4.0, 0.7) * env_ad(n, SR, 0.01, 0.3) * 1.3


def slash(seed: int) -> np.ndarray:
    """Die Klinge trifft: ein dumpfer Schlag, gedämpftes Metall."""
    n = n_of(0.9)
    y = thud(n, 115, 60, 0.3) * 1.2
    mix_at(y, low_metal(165.0 * (1.0 + 0.07 * (seed % 3)), 0.9, seed, 0.28) * 0.55, 0.0)
    y += lp(noise(n, seed + 1), 1200.0) * env_ad(n, SR, 0.003, 0.12) * 0.6
    return y


def dash(seed: int) -> np.ndarray:
    """Ansturm: ein Sturm aus Luft, darunter ein steigender, tiefer Ton."""
    n = n_of(1.0)
    t = np.linspace(0, 1, n)
    y = air(n, seed, 130, 650, 0.8) * env_asr(n, SR, 0.04, 0.4) * 1.2
    y += lp(tone(glide(n, 70.0, 140.0, 0.7), env_asr(n, SR, 0.05, 0.4), SR, harmonics=((1, 1.0), (2, 0.3))), 600.0) * 0.4
    y += glow(n, seed + 2, 8.0) * t
    return y


def whirl(seed: int) -> np.ndarray:
    """Wirbelklinge: kreisendes Rauschen, das an- und abschwillt."""
    n = n_of(1.6)
    t = np.arange(n) / SR
    am = 0.45 + 0.55 * np.abs(np.sin(np.pi * 3.2 * t))
    return air(n, seed, 180, 700, 0.9) * am * env_asr(n, SR, 0.08, 0.3) * 1.2


def guard(seed: int) -> np.ndarray:
    """Schwingenwacht: die Schwingen schließen sich, ein tiefes Glas klingt lange nach."""
    n = n_of(1.8)
    y = air(n_of(0.6), seed, 120, 480, 1.0) * env_ad(n_of(0.6), SR, 0.05, 0.4) * 1.1
    y = np.concatenate([y, np.zeros(n - len(y))])
    mix_at(y, soft_glass(293.7, 1.6, seed + 1, 1.2) * 0.45, 0.2)
    y += dark_choir(n, [146.8, 220.0], seed + 2, "o") * env_swell(n, 1.0, 1.4, 0.3) * 0.3
    return y


def parry(seed: int) -> np.ndarray:
    """Abgewehrt: ein tiefer Gong, ein kurzer Stoß."""
    n = n_of(1.4)
    y = lp(bell(130.8 * (1.0 + 0.04 * (seed % 3)), 1.4, SR, ratios=(1.0, 2.1, 3.3, 4.6), amps=(1.0, 0.4, 0.15, 0.05), decay=0.7,
                seed=seed), 1600.0) * 0.8
    y += thud(n, 100, 55, 0.2) * 0.8
    mix_at(y, soft_glass(392.0, 1.0, seed + 1, 1.8) * 0.25, 0.0)
    return y


def land(seed: int) -> np.ndarray:
    """Er landet: ein schwerer Aufprall, Staub und Geröll."""
    n = n_of(1.6)
    y = thud(n, 62, 30, 1.0) * 1.7
    y += lp(noise(n, seed), 550.0) * env_ad(n, SR, 0.005, 0.8) * 1.2
    r = rng(seed)
    for _ in range(6):
        m = n_of(0.07)
        mix_at(y, lp(filt(noise(m, int(r.integers(1, 1 << 30))), "band", [250, 1500], SR, 2), 1500.0)
               * env_ad(m, SR, 0.002, 0.05) * r.uniform(0.15, 0.35), r.uniform(0.05, 0.7))
    return y


# =====================================================================================
# Seraphlinge, Altar, Zepter
# =====================================================================================

def seraph_ambient(seed: int) -> np.ndarray:
    n = n_of(1.0)
    f = 392.0 * (1.0 + 0.06 * (seed % 3))
    return lp(dark_choir(n, [f, f * 1.25], seed, "o"), 1500.0) * env_swell(n, 1.0, 1.5, 0.3) * 0.7


def seraph_hurt(seed: int) -> np.ndarray:
    return soft_glass(587.3 * (1.0 + 0.05 * (seed % 3)), 0.5, seed, 3.0) * 0.6


def seraph_death(seed: int) -> np.ndarray:
    n = n_of(1.0)
    t = np.linspace(0, 1, n)
    return glow(n, seed, 20.0) * (1.0 - t) + lp(tone(glide(n, 523.3, 261.6, 1.0), (1.0 - t) ** 2, SR), 1200.0) * 0.25


def seraph_shoot(seed: int) -> np.ndarray:
    n = n_of(0.35)
    return soft_glass(659.3 * (1.0 + 0.04 * (seed % 3)), 0.35, seed, 4.0) * 0.35 \
        + air(n, seed + 1, 350, 1200, 0.6) * env_ad(n, SR, 0.01, 0.2) * 0.5


def altar_awaken(seed: int) -> np.ndarray:
    n = n_of(4.4)
    t = np.linspace(0, 1, n)
    y = dark_choir(n, [98.0, 146.8, 196.0, 293.7], seed, "o") * t ** 1.3 * 0.7
    for k in range(5):
        mix_at(y, soft_glass(261.6 * (1.0 + k * 0.125), 1.2, seed + k, 2.0) * 0.22, 0.5 + k * 0.6)
    y += glow(n, seed + 20, 12.0) * t
    return y


def altar_hum(seed: int) -> np.ndarray:
    n = n_of(2.0)
    t = np.arange(n) / SR
    f = 130.8 * (1.0 + 0.004 * np.sin(2 * np.pi * 0.7 * t))
    return lp(tone(f, env_swell(n, 1.0, 1.0, 0.5), SR, harmonics=((1, 1.0), (2, 0.45), (3, 0.2), (4, 0.08))), 1200.0) * 0.6


def scepter_cast(seed: int) -> np.ndarray:
    n = n_of(0.6)
    return soft_glass(523.3, 0.6, seed, 2.4) * 0.35 + air(n, seed + 1, 260, 1000, 0.7) * env_ad(n, SR, 0.02, 0.35) * 0.8


# name: (Erzeuger, Varianten, Raum (Nachhall, Anteil) oder None, Lautstärke (RMS dB))
# seit 0.76 deutlich leiser: häufige Klänge um -28 dB, mittlere um -24 dB, die großen Augenblicke um -20 dB
SYNTH = {
    "ambient": (ambient, 3, (2.4, 0.4), -31.0),
    "hurt": (hurt, 3, (1.4, 0.25), -27.0),
    "death": (death, 1, (3.6, 0.5), -21.0),
    "awaken": (awaken, 1, (3.4, 0.45), -20.0),
    "ascend": (ascend, 1, (3.6, 0.45), -20.0),
    "voice": (voice, 3, (2.8, 0.45), -25.0),
    "spear": (spear, 3, (1.4, 0.25), -29.0),
    "spear_fly": (spear_fly, 3, (1.0, 0.2), -29.0),
    "pillar": (pillar, 3, (2.0, 0.35), -23.0),
    "charge": (charge, 2, (1.8, 0.3), -26.0),
    "beam": (beam, 2, (1.6, 0.3), -25.0),
    "smite": (smite, 2, (2.0, 0.35), -21.0),
    "halo": (halo, 2, (1.4, 0.25), -26.0),
    "orb": (orb, 2, (2.0, 0.35), -24.0),
    "blast": (blast, 3, (2.0, 0.35), -23.0),
    "wings": (wings, 3, (1.2, 0.25), -24.0),
    "feathers": (feathers, 2, (1.2, 0.25), -28.0),
    "blink": (blink, 2, (1.4, 0.3), -26.0),
    "choir": (choir_ring, 3, (2.6, 0.45), -24.0),
    "judgment": (judgment, 2, (3.4, 0.45), -21.0),
    "timestop": (timestop, 1, (2.8, 0.45), -21.0),
    "time_resume": (time_resume, 1, (2.0, 0.35), -22.0),
    "tick": (tick, 2, (1.2, 0.3), -31.0),
    "word": (word, 1, (3.4, 0.5), -20.0),
    "supernova": (supernova, 1, (3.8, 0.5), -19.0),
    "planet": (planet, 2, (2.2, 0.35), -24.0),
    "impact": (impact_heavy, 3, (2.4, 0.35), -20.0),
    "star": (star, 3, (1.6, 0.3), -27.0),
    "stun": (stun, 1, (2.4, 0.4), -22.0),
    "heal": (heal, 2, (2.4, 0.4), -25.0),
    "blade": (blade, 2, (2.2, 0.4), -22.0),
    "swing": (swing, 4, (1.0, 0.2), -25.0),
    "slash": (slash, 3, (1.2, 0.25), -22.0),
    "dash": (dash, 2, (1.4, 0.25), -22.0),
    "whirl": (whirl, 2, (1.4, 0.25), -24.0),
    "guard": (guard, 2, (2.0, 0.35), -24.0),
    "parry": (parry, 3, (1.8, 0.3), -21.0),
    "land": (land, 2, (2.0, 0.3), -20.0),
    "seraph_ambient": (seraph_ambient, 3, (1.6, 0.35), -33.0),
    "seraph_hurt": (seraph_hurt, 3, (1.2, 0.3), -29.0),
    "seraph_death": (seraph_death, 1, (1.6, 0.35), -27.0),
    "seraph_shoot": (seraph_shoot, 3, (1.0, 0.25), -30.0),
    "altar_awaken": (altar_awaken, 1, (3.0, 0.45), -22.0),
    "altar_hum": (altar_hum, 2, (2.0, 0.35), -27.0),
    "scepter_cast": (scepter_cast, 3, (1.2, 0.25), -26.0),
}

# Ereignis: (Klang, Reichweite)
EVENTS = {f"entity.schrolli.{name}": (name, reach) for name, reach in (
    ("ambient", 48), ("hurt", 48), ("death", 160), ("awaken", 160), ("ascend", 160), ("voice", 160), ("spear", 48),
    ("spear_fly", 48), ("pillar", 64), ("charge", 96), ("beam", 96), ("smite", 80), ("halo", 64), ("orb", 80), ("blast", 80),
    ("wings", 64), ("feathers", 64), ("blink", 64), ("choir", 96), ("judgment", 128), ("timestop", 160), ("time_resume", 160),
    ("tick", 96), ("word", 160), ("supernova", 200), ("planet", 96), ("impact", 96), ("star", 64), ("stun", 96), ("heal", 96),
    ("blade", 96), ("swing", 48), ("slash", 48), ("dash", 64), ("whirl", 48), ("guard", 64), ("parry", 64), ("land", 96))}
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
        x = lp(x, 4200.0)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 2300 + i, stereo=False, damping=7500), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.06)
        x = soft_clip(fit_level(x, level, -6.0))
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
