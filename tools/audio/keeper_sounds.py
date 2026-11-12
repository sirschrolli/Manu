#!/usr/bin/env python3
"""
Erzeugt die Klänge des Steinhüters (0.96): eine Stimme aus mahlendem Fels mit dem Nachklang von Kristall – das Erwachen
(Gestein knirscht, Moos und Steine rieseln, ein Ton schwillt in den Kristallen), das Brüllen (ein tiefes Grollen aus der
Tiefe des Steins mit klingenden Obertönen), Faustschlag und Stampfer (gewaltige Schläge, Gestein bricht, die Erschütterung
rollt), das Graben und Werfen des Felsbrockens und sein Zerschellen, das Aufbrechen der Mondsteinkristalle, das Laden und
Brennen des Mondstrahls, das Wachsen und Zerspringen des Kristallpanzers, schwere Schritte, Schmerz und das Zerfallen im
Tod. Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py); es werden keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/keeper_sounds.py [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/fae/keeper_<name><n>.ogg
    Einträge "entity.stone_keeper.*" in assets/kingdomsatwar/sounds.json
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, bell, curve, env_ad, env_asr, fade_edges, filt, fit_level, formant_filter, glide, rng,  # noqa: E402
                          room, saw, soft_clip, tone, trim_tail)
from ambience_dsp import audible_bass  # noqa: E402
from dsp import write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/fae"
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

def thud(n: int, f0: float, f1: float, decay: float) -> np.ndarray:
    """Ein tiefer Schlag: ein Sinus, der rasch nach unten gleitet."""
    return tone(glide(n, f0, f1, 0.4), env_ad(n, SR, 0.002, decay), SR, harmonics=((1, 1.0), (2, 0.3), (3, 0.08)))


def impact(n: int, seed: int, lo: float, hi: float, decay: float) -> np.ndarray:
    return filt(noise(n, seed), "band", [lo, hi], SR, 2) * env_ad(n, SR, 0.0008, decay)


def gravel(n: int, seed: int, density: float, lo: float = 700.0, hi: float = 5000.0) -> np.ndarray:
    """Rieselndes Geröll: viele kleine, harte Klicks (Steinchen, die aufschlagen)."""
    r = rng(seed)
    y = np.zeros(n)
    for _ in range(int(density * n / SR)):
        at = int(r.uniform(0, max(1, n - 600)))
        length = int(r.uniform(60, 500))
        y[at:at + length] += r.standard_normal(length) * np.exp(-np.arange(length) / (length * 0.18)) * r.uniform(0.2, 1.0)
    return filt(y, "band", [lo, hi], SR, 2)


def grind(n: int, seed: int, lo: float = 60.0, hi: float = 700.0, rate: float = 9.0) -> np.ndarray:
    """Mahlender Fels: tiefes Rauschen, das stockend auf und ab geht (Stein reibt an Stein)."""
    r = rng(seed)
    t = np.arange(n) / SR
    x = filt(noise(n, seed), "band", [lo, hi], SR, 2)
    stutter = 0.55 + 0.45 * np.abs(np.sin(2 * np.pi * rate * t + r.uniform(0, 6.28))) ** 0.6
    wobble = 0.8 + 0.2 * np.sin(2 * np.pi * r.uniform(1.0, 2.5) * t)
    return x * stutter * wobble


def chime(f0: float, dur: float, seed: int, decay: float = 1.4) -> np.ndarray:
    """Kristall: ein reiner, unharmonischer Klang wie ein angeschlagener Bergkristall."""
    return bell(f0, dur, SR, ratios=(1.0, 2.32, 4.25, 6.9), amps=(1.0, 0.42, 0.2, 0.08), decay=decay, seed=seed)


def stone_voice(n: int, seed: int, f0: float, f1: float, vowel: str = "o") -> np.ndarray:
    """Die Stimme des Hüters: ein tiefer, rauer Grundton (wie ein Berg, der spricht), mit Formanten und mahlendem Fels."""
    r = rng(seed)
    t = np.arange(n) / SR
    f = glide(n, f0, f1, 1.1) * (1.0 + 0.03 * np.sin(2 * np.pi * 5.0 * t) + 0.02 * r.standard_normal(n).cumsum() / np.sqrt(SR))
    rough = 1.0 + 0.45 * np.sin(2 * np.pi * r.uniform(18, 26) * t)
    y = saw(f, rough, SR, partials=36, rolloff=1.25)
    formants = {"o": [(380, 90, 1.0), (760, 130, 0.55), (2400, 260, 0.12)],
                "u": [(300, 80, 1.0), (620, 110, 0.4), (2200, 260, 0.08)],
                "a": [(600, 120, 1.0), (1000, 150, 0.6), (2500, 280, 0.2)]}[vowel]
    y = formant_filter(y, SR, formants) * 0.55 + y * 0.1
    return y + grind(n, seed + 3, 50, 500, r.uniform(7, 12)) * 0.6


def shimmer(n: int, seed: int, lo: float = 5000.0, hi: float = 14000.0) -> np.ndarray:
    """Funkeln: feines, helles Rieseln (Mondstaub)."""
    r = rng(seed)
    t = np.arange(n) / SR
    x = filt(noise(n, seed), "band", [lo, hi], SR, 2)
    return x * (0.5 + 0.5 * np.sin(2 * np.pi * r.uniform(6, 11) * t) ** 2)


# =====================================================================================
# Klänge
# =====================================================================================

def ambient(seed: int) -> np.ndarray:
    """Er atmet: Fels mahlt leise auf und ab, tief brummt der Stein, ein Kristall klingt nach."""
    r = rng(seed)
    n = int(2.6 * SR)
    y = np.zeros(n)
    m = int(2.1 * SR)
    shape = curve(m, [(0.0, 0.0), (0.4, 1.0), (0.6, 0.6), (0.85, 0.9), (1.0, 0.0)])
    mix_at(y, grind(m, seed, 50, 450, r.uniform(5, 8)) * shape, 0.1, 0.8)
    mix_at(y, stone_voice(int(1.2 * SR), seed + 1, 46.0, 42.0, "u") * env_asr(int(1.2 * SR), SR, 0.35, 0.5), 0.7, 0.45)
    mix_at(y, chime(r.uniform(1200, 1600), 1.6, seed + 2, decay=1.1), r.uniform(0.6, 1.4), 0.06)
    return filt(y, "low", 7000, SR, 2)


def hurt(seed: int) -> np.ndarray:
    """Schmerz: Stein splittert, ein kurzes, dumpfes Aufgrollen, Splitter klirren."""
    r = rng(seed)
    n = int(0.9 * SR)
    y = np.zeros(n)
    mix_at(y, impact(int(0.12 * SR), seed, 700, 5000, 0.02), 0.0, 1.0)
    mix_at(y, thud(int(0.3 * SR), 130, 60, 0.06), 0.0, 0.6)
    m = int(0.5 * SR)
    mix_at(y, stone_voice(m, seed + 1, 72.0, 56.0, "a") * env_ad(m, SR, 0.02, 0.18), 0.02, 0.8)
    mix_at(y, gravel(int(0.5 * SR), seed + 2, 40) * env_ad(int(0.5 * SR), SR, 0.01, 0.2), 0.03, 0.4)
    mix_at(y, chime(r.uniform(1800, 2400), 0.6, seed + 3, decay=0.35), 0.0, 0.08)
    return y


def death(seed: int) -> np.ndarray:
    """Tod: ein langes, sinkendes Grollen; das Licht erlischt (ein Kristallton verklingt) – dann bricht er zusammen,
    Gestein poltert, Geröll rieselt nach."""
    n = int(5.0 * SR)
    y = np.zeros(n)
    m = int(2.8 * SR)
    shape = curve(m, [(0.0, 0.0), (0.1, 1.0), (0.6, 0.7), (1.0, 0.0)])
    mix_at(y, stone_voice(m, seed, 62.0, 30.0, "o") * shape, 0.0, 1.0)
    mix_at(y, chime(880.0, 3.0, seed + 1, decay=2.2) * curve(int(3.0 * SR), [(0.0, 1.0), (1.0, 0.0)]), 0.3, 0.12)
    mix_at(y, chime(1318.0, 2.6, seed + 2, decay=1.8), 0.5, 0.06)
    for k, at in enumerate((2.6, 2.75, 2.95, 3.2)):
        mix_at(y, thud(int(0.6 * SR), 90 - k * 10, 34, 0.18), at, 0.8 - k * 0.12)
        mix_at(y, impact(int(0.15 * SR), seed + 10 + k, 200, 3000, 0.04), at, 0.6)
    k = int(2.2 * SR)
    mix_at(y, filt(noise(k, seed + 3), "low", 300, SR, 2) * env_ad(k, SR, 0.05, 0.8), 2.6, 1.1)
    mix_at(y, gravel(k, seed + 4, 90, 500, 4000) * env_ad(k, SR, 0.1, 0.9), 2.65, 0.7)
    return filt(y, "low", 8000, SR, 2)


def step(seed: int) -> np.ndarray:
    """Ein schwerer Schritt aus Stein: dumpfer Aufprall, Geröll knirscht."""
    n = int(0.6 * SR)
    y = np.zeros(n)
    mix_at(y, thud(int(0.35 * SR), 95, 42, 0.09), 0.0, 1.0)
    mix_at(y, impact(int(0.12 * SR), seed, 250, 2200, 0.03), 0.0, 0.6)
    mix_at(y, gravel(int(0.35 * SR), seed + 1, 50, 600, 4000) * env_ad(int(0.35 * SR), SR, 0.005, 0.12), 0.01, 0.35)
    return y


def awaken(seed: int) -> np.ndarray:
    """Erwachen: Fels knirscht und verschiebt sich, Steine und Moos rieseln, in den Kristallen schwillt ein Ton an."""
    n = int(3.6 * SR)
    y = np.zeros(n)
    m = int(3.2 * SR)
    shape = curve(m, [(0.0, 0.0), (0.2, 0.8), (0.5, 1.0), (0.85, 0.7), (1.0, 0.0)])
    mix_at(y, grind(m, seed, 45, 600, 6.0) * shape, 0.0, 1.0)
    mix_at(y, gravel(m, seed + 1, 70, 600, 5000) * shape, 0.2, 0.6)
    t = np.arange(m) / SR
    swell = curve(m, [(0.0, 0.0), (0.6, 0.6), (0.9, 1.0), (1.0, 0.0)])
    hum = tone(glide(m, 220.0, 330.0, 1.3), swell * 0.25, SR, harmonics=((1, 1.0), (2, 0.4), (3, 0.2), (5, 0.08)))
    mix_at(y, hum * (1.0 + 0.1 * np.sin(2 * np.pi * 5.5 * t)), 0.3, 0.6)
    for k, at in enumerate((0.4, 1.1, 1.9, 2.6)):
        mix_at(y, thud(int(0.5 * SR), 80, 38, 0.15), at, 0.5)
        mix_at(y, impact(int(0.12 * SR), seed + 20 + k, 300, 2500, 0.03), at, 0.4)
    return filt(y, "low", 9000, SR, 2)


def roar(seed: int) -> np.ndarray:
    """Das Brüllen: ein gewaltiges, tiefes Grollen aus dem Stein, darüber klingen die Kristalle mit; Geröll bebt."""
    n = int(3.0 * SR)
    y = np.zeros(n)
    m = int(2.5 * SR)
    shape = curve(m, [(0.0, 0.0), (0.1, 0.85), (0.3, 1.0), (0.75, 0.85), (1.0, 0.0)])
    mix_at(y, stone_voice(m, seed, 55.0, 70.0, "o") * shape, 0.04, 1.0)
    mix_at(y, stone_voice(m, seed + 4, 82.0, 98.0, "a") * shape * 0.45, 0.06, 1.0)
    t = np.arange(m) / SR
    ring = tone(np.full(m, 330.0), shape * 0.2, SR, harmonics=((1, 1.0), (2.01, 0.5), (3.02, 0.3), (4.4, 0.15)))
    mix_at(y, ring * (1.0 + 0.2 * np.sin(2 * np.pi * 6.0 * t)), 0.1, 0.5)
    mix_at(y, gravel(m, seed + 8, 60, 500, 4000) * shape, 0.15, 0.4)
    mix_at(y, filt(noise(m, seed + 9), "low", 140, SR, 2) * shape, 0.0, 0.9)
    return filt(y, "low", 8000, SR, 2)


def smash(seed: int) -> np.ndarray:
    """Faustschlag in den Boden: ein gewaltiger Schlag, Gestein bricht auf, Brocken poltern, die Erschütterung rollt nach."""
    n = int(2.6 * SR)
    y = np.zeros(n)
    mix_at(y, thud(int(1.3 * SR), 85, 30, 0.38), 0.0, 1.0)
    mix_at(y, impact(int(0.25 * SR), seed, 150, 4000, 0.06), 0.0, 1.1)
    m = int(2.0 * SR)
    mix_at(y, filt(noise(m, seed + 1), "low", 240, SR, 2) * env_ad(m, SR, 0.02, 0.7), 0.02, 1.3)
    mix_at(y, gravel(m, seed + 2, 80, 600, 5000) * env_ad(m, SR, 0.03, 0.55), 0.05, 0.8)
    mix_at(y, chime(rng(seed).uniform(600, 760), 1.8, seed + 3, decay=1.0), 0.0, 0.1)
    return y


def stomp(seed: int) -> np.ndarray:
    """Stampfer: ein dumpfer, tiefer Schlag – dann rollt die Erschütterung als Grollen davon."""
    n = int(3.0 * SR)
    y = np.zeros(n)
    mix_at(y, thud(int(1.0 * SR), 70, 28, 0.3), 0.0, 1.1)
    mix_at(y, impact(int(0.2 * SR), seed, 120, 2400, 0.05), 0.0, 0.8)
    m = int(2.6 * SR)
    t = np.arange(m) / SR
    roll = filt(noise(m, seed + 1), "low", 200, SR, 2) * curve(m, [(0.0, 0.0), (0.05, 1.0), (0.5, 0.6), (1.0, 0.0)])
    mix_at(y, roll * (0.8 + 0.2 * np.sin(2 * np.pi * 9.0 * t)), 0.02, 1.4)
    mix_at(y, gravel(m, seed + 2, 60, 500, 4000) * env_ad(m, SR, 0.05, 0.8), 0.08, 0.6)
    return y


def dig(seed: int) -> np.ndarray:
    """Er reißt einen Brocken aus dem Boden: Fels kracht und reißt, Erde bröselt."""
    n = int(1.6 * SR)
    y = np.zeros(n)
    m = int(1.2 * SR)
    mix_at(y, grind(m, seed, 80, 1200, 14.0) * curve(m, [(0.0, 0.0), (0.15, 1.0), (0.7, 0.8), (1.0, 0.0)]), 0.0, 1.0)
    for k, at in enumerate((0.05, 0.3, 0.55, 0.8)):
        mix_at(y, impact(int(0.1 * SR), seed + 10 + k, 300, 3500, 0.025), at, 0.7)
    mix_at(y, gravel(m, seed + 1, 90, 400, 4000) * env_ad(m, SR, 0.05, 0.6), 0.1, 0.7)
    mix_at(y, thud(int(0.5 * SR), 70, 40, 0.15), 0.6, 0.6)
    return y


def throw(seed: int) -> np.ndarray:
    """Wurf: ein schwerer Schwung durch die Luft, ein Ächzen aus Stein."""
    n = int(1.2 * SR)
    y = np.zeros(n)
    whoosh = filt(noise(n, seed), "band", [150, 1200], SR, 2) * curve(n, [(0.0, 0.0), (0.35, 1.0), (0.6, 0.35), (1.0, 0.0)])
    mix_at(y, whoosh, 0.0, 1.0)
    m = int(0.6 * SR)
    mix_at(y, stone_voice(m, seed + 1, 66.0, 52.0, "a") * env_ad(m, SR, 0.05, 0.25), 0.05, 0.6)
    return y


def boulder(seed: int) -> np.ndarray:
    """Der Brocken zerschellt: ein harter Aufschlag, Fels zerspringt in Stücke, Mondsteinsplitter klirren, Geröll rieselt."""
    r = rng(seed)
    n = int(2.4 * SR)
    y = np.zeros(n)
    mix_at(y, thud(int(0.9 * SR), 110, 36, 0.25), 0.0, 1.0)
    mix_at(y, impact(int(0.3 * SR), seed, 200, 6000, 0.07), 0.0, 1.2)
    m = int(1.8 * SR)
    mix_at(y, gravel(m, seed + 1, 120, 500, 6000) * env_ad(m, SR, 0.01, 0.6), 0.02, 1.0)
    for k in range(5):
        mix_at(y, chime(r.uniform(1400, 3200), 0.9, seed + 10 + k, decay=0.5), r.uniform(0.02, 0.3), 0.07)
    mix_at(y, filt(noise(m, seed + 2), "low", 260, SR, 2) * env_ad(m, SR, 0.02, 0.5), 0.0, 0.8)
    return y


def crystal(seed: int) -> np.ndarray:
    """Ein Mondsteinkristall bricht aus dem Boden: ein scharfes Krachen, dann ein heller, aufsteigender Kristallklang."""
    r = rng(seed)
    n = int(1.6 * SR)
    y = np.zeros(n)
    mix_at(y, impact(int(0.1 * SR), seed, 900, 9000, 0.02), 0.0, 1.0)
    mix_at(y, thud(int(0.3 * SR), 140, 70, 0.07), 0.0, 0.5)
    f0 = r.uniform(900, 1250)
    mix_at(y, chime(f0, 1.4, seed + 1, decay=0.9), 0.01, 0.35)
    mix_at(y, chime(f0 * 1.5, 1.2, seed + 2, decay=0.7), 0.03, 0.18)
    m = int(0.9 * SR)
    mix_at(y, shimmer(m, seed + 3) * env_ad(m, SR, 0.01, 0.3), 0.0, 0.2)
    mix_at(y, gravel(int(0.5 * SR), seed + 4, 60, 700, 5000) * env_ad(int(0.5 * SR), SR, 0.005, 0.2), 0.0, 0.4)
    return y


def beam_charge(seed: int) -> np.ndarray:
    """Der Mondstrahl lädt: ein Ton aus Licht steigt an, Kristalle klingen immer schneller, Funkeln wird dichter."""
    n = int(1.9 * SR)
    y = np.zeros(n)
    t = np.arange(n) / SR
    rise = curve(n, [(0.0, 0.0), (0.85, 1.0), (1.0, 0.7)])
    hum = tone(glide(n, 110.0, 440.0, 1.6), rise * 0.5, SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.3), (4, 0.15), (6, 0.08)))
    mix_at(y, hum * (1.0 + 0.15 * np.sin(2 * np.pi * (4.0 + 10.0 * t) * t)), 0.0, 0.7)
    mix_at(y, shimmer(n, seed) * rise, 0.0, 0.25)
    r = rng(seed + 1)
    at = 0.1
    while at < 1.8:
        mix_at(y, chime(r.uniform(1500, 2600), 0.5, seed + int(at * 100), decay=0.3), at, 0.08)
        at += max(0.05, 0.3 * (1.0 - at / 1.9))
    return y


def beam(seed: int) -> np.ndarray:
    """Der Mondstrahl brennt: ein heller, flirrender Akkord aus Licht, darunter ein Brummen, Funkeln."""
    n = int(2.0 * SR)
    t = np.arange(n) / SR
    env = curve(n, [(0.0, 0.0), (0.04, 1.0), (0.85, 0.9), (1.0, 0.0)])
    y = np.zeros(n)
    for f, a in ((330.0, 0.5), (495.0, 0.35), (660.0, 0.3), (990.0, 0.12)):
        mix_at(y, tone(np.full(n, f) * (1.0 + 0.004 * np.sin(2 * np.pi * 5.0 * t)), env * a, SR,
                       harmonics=((1, 1.0), (2, 0.2))), 0.0, 1.0)
    mix_at(y, tone(np.full(n, 82.5), env * 0.6, SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.25))), 0.0, 0.6)
    mix_at(y, shimmer(n, seed, 4000, 12000) * env, 0.0, 0.35)
    y *= 1.0 + 0.12 * np.sin(2 * np.pi * 11.0 * t)
    return y


def armor(seed: int) -> np.ndarray:
    """Der Kristallpanzer wächst: knisternde, klirrende Kristalle, die sich aneinander schieben; ein tiefes Summen schwillt."""
    r = rng(seed)
    n = int(2.4 * SR)
    y = np.zeros(n)
    m = int(2.0 * SR)
    swell = curve(m, [(0.0, 0.0), (0.7, 1.0), (1.0, 0.0)])
    mix_at(y, gravel(m, seed, 160, 2500, 12000) * swell, 0.0, 0.6)
    for k in range(14):
        at = 0.05 + k * 0.12 + r.uniform(0, 0.05)
        mix_at(y, chime(r.uniform(1000, 2800), 0.6, seed + 30 + k, decay=0.35), at, 0.06 + 0.012 * k)
    mix_at(y, tone(glide(m, 140.0, 200.0, 1.0), swell * 0.4, SR, harmonics=((1, 1.0), (2, 0.4), (3, 0.2))), 0.1, 0.6)
    mix_at(y, chime(660.0, 1.8, seed + 2, decay=1.4), 1.3, 0.2)
    return y


def shatter(seed: int) -> np.ndarray:
    """Der Panzer zerspringt: ein gleißendes Klirren, Splitter fliegen und regnen herab."""
    r = rng(seed)
    n = int(2.6 * SR)
    y = np.zeros(n)
    mix_at(y, impact(int(0.2 * SR), seed, 1500, 14000, 0.05), 0.0, 1.0)
    mix_at(y, thud(int(0.5 * SR), 160, 60, 0.12), 0.0, 0.5)
    for k in range(22):
        at = r.uniform(0.0, 0.25) if k < 10 else r.uniform(0.25, 1.4)
        mix_at(y, chime(r.uniform(1200, 4200), 0.8, seed + 40 + k, decay=r.uniform(0.2, 0.6)), at, 0.08)
    m = int(2.0 * SR)
    mix_at(y, gravel(m, seed + 1, 140, 2000, 12000) * env_ad(m, SR, 0.01, 0.8), 0.02, 0.6)
    mix_at(y, shimmer(m, seed + 2) * env_ad(m, SR, 0.01, 0.6), 0.0, 0.3)
    return y


# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil) oder None, Ziel-Pegel dBFS)
SYNTH = {
    "ambient": (ambient, 3, (1.6, 0.3), -22.0),
    "hurt": (hurt, 3, (1.0, 0.2), -17.0),
    "death": (death, 1, (2.6, 0.4), -14.0),
    "step": (step, 4, (0.6, 0.15), -21.0),
    "awaken": (awaken, 1, (2.4, 0.35), -15.0),
    "roar": (roar, 2, (2.4, 0.35), -14.0),
    "smash": (smash, 2, (2.0, 0.35), -13.0),
    "stomp": (stomp, 2, (2.2, 0.35), -13.0),
    "dig": (dig, 2, (1.2, 0.25), -16.0),
    "throw": (throw, 2, (1.0, 0.2), -17.0),
    "boulder": (boulder, 3, (1.6, 0.3), -14.0),
    "crystal": (crystal, 4, (1.4, 0.3), -17.0),
    "beam_charge": (beam_charge, 1, (1.6, 0.3), -16.0),
    "beam": (beam, 1, (1.6, 0.3), -15.0),
    "armor": (armor, 1, (1.8, 0.35), -15.0),
    "shatter": (shatter, 2, (1.8, 0.35), -13.0),
}

# Seit 1.1.4 nach Lautheit (LUFS) statt nach Effektivwert eingepegelt: der harte Aufschlag begrenzte den Pegel, der
# Schmerz klang zu schwach. Name: Lautheit in LUFS
LUFS = {"hurt": -18.0}

# Ereignis: (Klänge, Reichweite)
EVENTS = {f"entity.stone_keeper.{name}": ([name], reach) for name, reach in (
    ("ambient", 24), ("hurt", 32), ("death", 64), ("step", 16), ("awaken", 48), ("roar", 64), ("smash", 48), ("stomp", 48),
    ("dig", 32), ("throw", 32), ("boulder", 40), ("crystal", 32), ("beam_charge", 40), ("beam", 48), ("armor", 48),
    ("shatter", 48))}


def files_of(name: str) -> list[str]:
    return [f"kingdomsatwar:fae/keeper_{name}{i + 1}" for i in range(SYNTH[name][1])]


# Seit 1.1.4: diese sehr tiefen Klänge bekommen Obertöne ihres Basses, damit sie auch auf kleinen Lautsprechern
# (Laptop, Fernseher, Kopfhörer ohne Bass) zu hören sind – siehe ambience_dsp.audible_bass.
AUDIBLE = {"step", "stomp"}


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    files = []
    for i in range(count):
        x = fn(8100 + i * 43 + zlib.crc32(name.encode()) % 997)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 1700 + i, stereo=False, damping=6000), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.05)
        if name in AUDIBLE:
            x = audible_bass(x, SR)
        if name in LUFS:
            from dragon_sounds import fit_lufs
            x = fit_lufs(x, LUFS[name])
        else:
            x = soft_clip(fit_level(x, level, -2.0))
        path = OUT / f"keeper_{name}{i + 1}.ogg"
        write_ogg(x.astype(np.float32), SR, path, quality=5.0)
        files.append(f"kingdomsatwar:fae/keeper_{name}{i + 1}")
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
