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
                          trim_tail, fade_edges, saw, glide, curve, formant_filter)
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
# Fee
# =====================================================================================

def fairy_ambient(seed: int) -> np.ndarray:
    """Ein helles Klingeln aus zwei, drei Glöckchen, dazwischen ein winziges, kicherndes Trillern."""
    r = rng(seed)
    dur = 1.8
    y = np.zeros(int(dur * SR))
    start = int(r.integers(4, len(PENTA) - 3))
    for i in range(int(r.integers(2, 4))):
        f = PENTA[min(len(PENTA) - 1, start + int(r.integers(-1, 3)))] * 1.0
        mix_at(y, glass(f, 1.0, 0.5, seed + i), 0.05 + i * r.uniform(0.09, 0.16), r.uniform(0.25, 0.4))
    # Trillern: schnell wechselnde, sehr hohe Töne
    n = int(0.28 * SR)
    t = np.arange(n) / SR
    f = 2600.0 + 380.0 * np.sign(np.sin(2 * np.pi * 18.0 * t)) + 300.0 * t / 0.28
    mix_at(y, tone(f, env_ad(n, SR, 0.01, 0.2) * 0.12, SR, harmonics=((1, 1.0), (2, 0.1))), r.uniform(0.4, 0.8))
    return filt(y, "low", 12000, SR, 2)


def fairy_hurt(seed: int) -> np.ndarray:
    """Ein kurzes, erschrockenes Klirren, das abwärts rutscht."""
    r = rng(seed)
    n = int(0.45 * SR)
    t = np.arange(n) / SR
    f = 2400.0 * np.exp(-t * 3.0) + 900.0
    y = tone(f, env_ad(n, SR, 0.004, 0.18), SR, harmonics=((1, 1.0), (2, 0.3), (3, 0.1))) * 0.5
    mix_at(y, glass(PENTA[int(r.integers(6, 10))], 0.4, 0.25, seed), 0.0, 0.3)
    return y


def fairy_death(seed: int) -> np.ndarray:
    """Eine absteigende Kaskade aus Glöckchen, die verklingt, darüber ein Hauch Flirren."""
    r = rng(seed)
    dur = 2.2
    y = np.zeros(int(dur * SR))
    top = len(PENTA) - 1
    for i in range(7):
        mix_at(y, glass(PENTA[top - i], 1.2, 0.6, seed + i), 0.05 + i * 0.1, 0.3 - i * 0.025)
    n = len(y)
    y += shimmer(n, seed + 9) * env_ad(n, SR, 0.2, 1.4) * 0.12
    return y


def fairy_gift(seed: int) -> np.ndarray:
    """Freude: ein schnelles, aufsteigendes Arpeggio, das in einem glitzernden Akkord endet."""
    r = rng(seed)
    dur = 2.0
    y = np.zeros(int(dur * SR))
    base = int(r.integers(2, 4))
    for i in range(6):
        mix_at(y, glass(PENTA[base + i], 1.3, 0.7, seed + i), 0.04 + i * 0.07, 0.28)
    for k in (base + 5, base + 3):
        mix_at(y, glass(PENTA[min(k, len(PENTA) - 1)], 1.6, 0.9, seed + 20 + k), 0.5, 0.22)
    n = len(y)
    y += shimmer(n, seed + 3) * env_ad(n, SR, 0.3, 1.2) * 0.15
    return y


# =====================================================================================
# Pilzling
# =====================================================================================

def boop(f0: float, dur: float, bend: float) -> np.ndarray:
    """Ein runder, weicher Ton mit einem kleinen Bogen in der Tonhöhe (wie ein Quieken durch Watte)."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = f0 * (1.0 + bend * np.sin(np.pi * t / dur))
    y = tone(f, env_ad(n, SR, 0.012, dur * 0.6), SR, harmonics=((1, 1.0), (2, 0.35), (3, 0.12)))
    return filt(y, "low", 3500, SR, 2)


def mushroomling_ambient(seed: int) -> np.ndarray:
    """Zwei, drei kleine Quiekser, wie ein fragendes Brummeln."""
    r = rng(seed)
    y = np.zeros(int(0.9 * SR))
    for i in range(int(r.integers(2, 4))):
        mix_at(y, boop(r.uniform(520, 760), r.uniform(0.08, 0.14), r.uniform(-0.2, 0.35)), 0.04 + i * r.uniform(0.12, 0.2), 0.5)
    return y


def mushroomling_hurt(seed: int) -> np.ndarray:
    r = rng(seed)
    y = np.zeros(int(0.5 * SR))
    mix_at(y, boop(r.uniform(820, 950), 0.16, 0.45), 0.0, 0.7)
    mix_at(y, boop(r.uniform(600, 700), 0.1, -0.3), 0.15, 0.4)
    return y


def mushroomling_death(seed: int) -> np.ndarray:
    """Ein letzter Quiekser, dann ein weiches Plopp und rieselnde Sporen."""
    r = rng(seed)
    y = np.zeros(int(1.2 * SR))
    mix_at(y, boop(780, 0.22, -0.5), 0.0, 0.6)
    n = int(0.12 * SR)
    pop = filt(r.standard_normal(n), "band", [300, 1600], SR, 2) * env_ad(n, SR, 0.002, 0.05)
    mix_at(y, pop, 0.24, 0.9)
    n2 = int(0.8 * SR)
    mix_at(y, filt(r.standard_normal(n2), "band", [3000, 9000], SR, 2) * env_ad(n2, SR, 0.05, 0.6) * 0.15, 0.28)
    return y


def mushroomling_puff(seed: int) -> np.ndarray:
    """Sporen stieben: ein weicher, dumpfer Luftstoß mit hellem Rieseln."""
    r = rng(seed)
    n = int(0.9 * SR)
    burst = filt(r.standard_normal(n), "band", [250, 2500], SR, 2) * env_ad(n, SR, 0.01, 0.3)
    hiss = filt(r.standard_normal(n), "band", [4000, 11000], SR, 2) * env_ad(n, SR, 0.06, 0.55) * 0.35
    return burst + hiss


# =====================================================================================
# Dornwicht
# =====================================================================================

def thorn_volley(seed: int) -> np.ndarray:
    """Dornen schwirren los: ein trockenes Knacken wie brechende Zweige, dann drei pfeifende Zischlaute."""
    r = rng(seed)
    y = np.zeros(int(1.0 * SR))
    for _ in range(int(r.integers(5, 9))):
        k = int(r.uniform(0.004, 0.012) * SR)
        crack = filt(r.standard_normal(k), "band", [800, 5000], SR, 2) * env_ad(k, SR, 0.0005, 0.004)
        mix_at(y, crack, r.uniform(0.0, 0.12), r.uniform(0.4, 0.9))
    for i in range(3):
        n = int(0.32 * SR)
        t = np.arange(n) / SR
        f = 3200.0 * np.exp(-t * 2.2) + 700.0
        w = filt(r.standard_normal(n), "band", [1500, 7000], SR, 2) * env_ad(n, SR, 0.02, 0.2) * 0.35
        w += tone(f, env_ad(n, SR, 0.02, 0.18) * 0.08, SR, harmonics=((1, 1.0),))
        mix_at(y, w, 0.12 + i * 0.06, 0.6)
    return y


# =====================================================================================
# Noctua, die Mottenkönigin, und die Feen-Elytren
# =====================================================================================

# eine Moll-Pentatonik für das Schlaflied (A-Moll: A C D E G)
MINOR = (440.0, 523.25, 587.33, 659.25, 783.99, 880.0, 1046.5, 1174.66, 1318.51)


def wingbeat(seed: int, depth: float = 1.0, length: float = 0.55) -> np.ndarray:
    """Ein einzelner, großer Flügelschlag: ein dumpfer Luftstoß (tief) mit einem rauschenden Nachzug."""
    r = rng(seed)
    n = int(length * SR)
    thump = filt(r.standard_normal(n), "band", [60, 420 + 200 * depth], SR, 2) * env_ad(n, SR, 0.025, 0.11) * 1.4
    air = filt(r.standard_normal(n), "band", [500, 3200], SR, 2) * env_ad(n, SR, 0.06, 0.2) * 0.35
    return (thump + air) * depth


def moth_ambient(seed: int) -> np.ndarray:
    """Ein leises Summen: zwei, drei langsame Flügelschläge, ein feines Schuppenflirren und ein ferner Glaston."""
    r = rng(seed)
    y = np.zeros(int(2.2 * SR))
    beat = r.uniform(0.32, 0.42)
    for i in range(int(r.integers(2, 4))):
        mix_at(y, wingbeat(seed + i, 0.55), 0.05 + i * beat, 0.6)
    n = len(y)
    t = np.arange(n) / SR
    chirr = filt(r.standard_normal(n), "band", [1800, 4200], SR, 2) * (0.5 + 0.5 * np.sin(2 * np.pi * 24.0 * t)) ** 2
    y += chirr * env_asr(n, SR, 0.4, 0.8) * 0.05
    mix_at(y, glass(MINOR[int(r.integers(4, 8))], 1.2, 0.7, seed + 9), r.uniform(0.3, 0.9), 0.12)
    return filt(y, "low", 9000, SR, 2)


def moth_hurt(seed: int) -> np.ndarray:
    """Ein schrilles, zirpendes Aufkreischen, das abwärts rutscht, dazu ein gläsernes Knistern."""
    r = rng(seed)
    n = int(0.55 * SR)
    t = np.arange(n) / SR
    f = 2600.0 * np.exp(-t * 2.4) + 900.0 + 120.0 * np.sin(2 * np.pi * 38.0 * t)
    y = saw(f, env_ad(n, SR, 0.008, 0.16) * 0.22, SR, partials=10, rolloff=1.3)
    y = formant_filter(y, SR, [(1500, 300, 1.0), (2900, 500, 0.7), (4200, 700, 0.3)])
    y += filt(r.standard_normal(n), "band", [2500, 8000], SR, 2) * env_ad(n, SR, 0.002, 0.05) * 0.25
    mix_at(y, glass(MINOR[int(r.integers(5, 9))], 0.4, 0.2, seed), 0.02, 0.15)
    return y


def moth_death(seed: int) -> np.ndarray:
    """Ein langer, sinkender Klageton, die Flügelschläge werden langsamer, dann zerstieben die Funken."""
    r = rng(seed)
    dur = 3.8
    y = np.zeros(int(dur * SR))
    n = int(2.6 * SR)
    t = np.arange(n) / SR
    f = glide(n, 1900.0, 260.0, 1.4) * (1.0 + 0.02 * np.sin(2 * np.pi * 6.0 * t))
    wail = saw(f, env_ad(n, SR, 0.05, 0.9) * 0.2, SR, partials=8, rolloff=1.4)
    mix_at(y, formant_filter(wail, SR, [(900, 250, 1.0), (2400, 450, 0.6)]), 0.0, 1.0)
    at = 0.0
    gap = 0.3
    for i in range(6):
        mix_at(y, wingbeat(seed + i, 0.8 - i * 0.11), at, 0.8)
        at += gap
        gap *= 1.35
    start = int(r.integers(5, 9))
    for i in range(9):
        mix_at(y, glass(MINOR[max(0, start - i)] * 2.0, 1.4, 0.6, seed + 20 + i), 1.6 + i * 0.12, 0.16 - i * 0.012)
    y += shimmer(len(y), seed + 3) * env_asr(len(y), SR, 1.8, 1.4) * 0.08
    return filt(y, "low", 10000, SR, 2)


def moth_screech(seed: int) -> np.ndarray:
    """Ein unheimlicher, gellender Schrei: zwei gegeneinander verstimmte, zitternde Stimmen, steigend und fallend."""
    r = rng(seed)
    n = int(1.5 * SR)
    t = np.arange(n) / SR
    contour = curve(n, [(0.0, 1200.0), (0.25, 2100.0), (0.6, 1900.0), (1.0, 1100.0)])
    y = np.zeros(n)
    for k, detune in enumerate((1.0, 1.013, 0.987)):
        vib = 1.0 + 0.025 * np.sin(2 * np.pi * (11.0 + k) * t + k)
        y += saw(contour * detune * vib, env_asr(n, SR, 0.05, 0.6) * 0.14, SR, partials=12, rolloff=1.2)
    y = formant_filter(y, SR, [(1700, 350, 1.0), (3100, 500, 0.8), (5200, 900, 0.3)])
    y += filt(r.standard_normal(n), "band", [3000, 9000], SR, 2) * env_asr(n, SR, 0.05, 0.5) * 0.08
    return y


def moth_awaken(seed: int) -> np.ndarray:
    """Seide reißt (ein knisterndes Reißen), ein tiefes Atmen, dann steigt ein schimmernder Akkord empor."""
    r = rng(seed)
    dur = 4.2
    y = np.zeros(int(dur * SR))
    # Reißen: dichter werdende Knister
    for i in range(160):
        at = 1.3 * (i / 160.0) ** 0.6 + r.uniform(0, 0.04)
        k = int(r.uniform(0.002, 0.008) * SR)
        crack = filt(r.standard_normal(k), "band", [900, 6500], SR, 2) * env_ad(k, SR, 0.0005, 0.003)
        mix_at(y, crack, at, r.uniform(0.15, 0.5) * (0.4 + 0.6 * i / 160.0))
    n = int(1.6 * SR)
    breath = filt(r.standard_normal(n), "band", [120, 900], SR, 2) * env_asr(n, SR, 0.7, 0.8) * 0.5
    mix_at(y, breath, 1.1, 1.0)
    # zwei große Flügelschläge – sie entfaltet sich
    mix_at(y, wingbeat(seed + 1, 1.0, 0.8), 1.9, 0.9)
    mix_at(y, wingbeat(seed + 2, 1.0, 0.8), 2.45, 0.9)
    for i in range(8):
        mix_at(y, glass(MINOR[min(len(MINOR) - 1, i)] * 1.0, 1.8, 0.8, seed + 30 + i), 2.0 + i * 0.13, 0.2)
    t = np.arange(int(2.0 * SR)) / SR
    pad = np.zeros(len(t))
    for k, f in enumerate((220.0, 329.63, 440.0, 523.25)):
        pad += tone(np.full(len(t), f), env_asr(len(t), SR, 0.8, 0.9) * (0.25 / (k + 1)), SR, harmonics=((1, 1.0), (2, 0.15)))
    mix_at(y, pad, 2.1, 0.8)
    return filt(y, "low", 10000, SR, 2)


def moth_gust(seed: int) -> np.ndarray:
    """Ein gewaltiger Flügelschlag: ein dumpfes Wummern, dann rauschende, wirbelnde Luft."""
    r = rng(seed)
    y = np.zeros(int(1.1 * SR))
    mix_at(y, wingbeat(seed, 1.3, 0.9), 0.0, 1.2)
    n = int(0.9 * SR)
    t = np.arange(n) / SR
    swirl = filt(r.standard_normal(n), "band", [400, 2600], SR, 2) * (0.6 + 0.4 * np.sin(2 * np.pi * 7.0 * t))
    mix_at(y, swirl * env_ad(n, SR, 0.08, 0.3) * 0.45, 0.05)
    y += shimmer(len(y), seed + 1, 5000, 11000) * env_ad(len(y), SR, 0.1, 0.4) * 0.06
    return y


def moth_scales(seed: int) -> np.ndarray:
    """Glitzernde Schuppen rieseln: viele winzige, helle Klicks über einem sinkenden Rauschen."""
    r = rng(seed)
    dur = 1.6
    y = np.zeros(int(dur * SR))
    for i in range(70):
        f = r.uniform(3200, 8500)
        k = int(0.05 * SR)
        tt = np.arange(k) / SR
        ping = np.sin(2 * np.pi * f * tt) * np.exp(-tt / 0.012)
        mix_at(y, ping, r.uniform(0.0, dur - 0.1) ** 1.1, r.uniform(0.04, 0.12))
    n = len(y)
    t = np.arange(n) / SR
    hiss = filt(r.standard_normal(n), "band", [2500, 9000], SR, 2) * env_asr(n, SR, 0.2, 0.9) * 0.08
    return y + hiss * (1.0 - 0.4 * t / dur)


def moth_beam_charge(seed: int) -> np.ndarray:
    """Mondlicht sammelt sich: ein ansteigendes, klingendes Summen, über dem das Flirren dichter wird."""
    r = rng(seed)
    n = int(1.9 * SR)
    t = np.arange(n) / SR
    f = glide(n, 220.0, 880.0, 0.8)
    y = tone(f, env_asr(n, SR, 0.4, 0.3) * 0.22, SR, harmonics=((1, 1.0), (2, 0.35), (3, 0.15), (5, 0.06)))
    y *= 1.0 + 0.3 * np.sin(2 * np.pi * (4.0 + 6.0 * t / 1.9) * t)
    y += shimmer(n, seed, 3500, 11000) * (t / 1.9) ** 1.5 * 0.2
    for i in range(4):
        mix_at(y, glass(MINOR[4 + i], 0.8, 0.5, seed + i), 0.5 + i * 0.3, 0.1)
    return y


def moth_beam(seed: int) -> np.ndarray:
    """Der Mondstrahl schlägt ein: ein heller Kristallakkord, ein tiefer Schlag, verklingendes Zischen."""
    r = rng(seed)
    y = np.zeros(int(1.6 * SR))
    for k, f in enumerate((1318.51, 1760.0, 2093.0, 2637.0)):
        mix_at(y, bell(f * r.uniform(0.99, 1.01), 1.4, SR, decay=0.6, seed=seed + k), 0.0, 0.22 - k * 0.03)
    n = int(0.6 * SR)
    thump = filt(r.standard_normal(n), "low", 220, SR, 2) * env_ad(n, SR, 0.004, 0.12) * 1.6
    mix_at(y, thump, 0.0)
    n2 = int(1.4 * SR)
    hiss = filt(r.standard_normal(n2), "band", [3000, 10000], SR, 2) * env_ad(n2, SR, 0.01, 0.4) * 0.2
    mix_at(y, hiss, 0.02)
    return y


def hum_voice(f: np.ndarray, amp: np.ndarray) -> np.ndarray:
    """Eine summende, fast menschliche Stimme („uuh“): Sägezahn durch Formanten."""
    v = saw(f, amp, SR, partials=16, rolloff=1.6)
    return formant_filter(v, SR, [(380, 90, 1.0), (900, 160, 0.45), (2500, 400, 0.12)])


def moth_lullaby(seed: int) -> np.ndarray:
    """Das Schlaflied: eine langsame, wiegende Melodie in Moll, gesummt, mit gläsernem Echo und weichem Grundton."""
    r = rng(seed)
    melody = [(4, 0.5), (5, 0.5), (7, 0.9), (6, 0.45), (5, 0.45), (4, 0.9), (2, 0.45), (3, 0.45), (4, 1.2)]
    total = sum(d for _, d in melody) + 1.6
    y = np.zeros(int(total * SR))
    at = 0.1
    for i, (step, d) in enumerate(melody):
        n = int((d + 0.35) * SR)
        t = np.arange(n) / SR
        f0 = MINOR[step] * 0.5
        f = f0 * (1.0 + 0.012 * np.sin(2 * np.pi * 5.2 * t) * np.clip(t / 0.25, 0, 1))
        voice = hum_voice(f, env_asr(n, SR, 0.08, 0.3) * 0.5)
        mix_at(y, voice, at, 1.0)
        mix_at(y, glass(MINOR[step], 1.4, 0.7, seed + i), at + 0.02, 0.07)
        at += d
    n = len(y)
    t = np.arange(n) / SR
    drone = tone(np.full(n, 110.0) * (1.0 + 0.002 * np.sin(2 * np.pi * 0.3 * t)), env_asr(n, SR, 0.8, 1.2) * 0.12, SR,
                 harmonics=((1, 1.0), (2, 0.4), (3, 0.15)))
    y += drone
    y += shimmer(n, seed + 5, 5000, 11000) * env_asr(n, SR, 1.0, 1.0) * 0.04
    return filt(y, "low", 8000, SR, 2)


def moth_dive(seed: int) -> np.ndarray:
    """Sturzflug: ein sinkendes Pfeifen und anschwellendes Brausen der Luft."""
    r = rng(seed)
    n = int(1.2 * SR)
    t = np.arange(n) / SR
    f = glide(n, 2400.0, 650.0, 1.2)
    whistle = tone(f, env_asr(n, SR, 0.08, 0.3) * 0.1, SR, harmonics=((1, 1.0), (2, 0.12)))
    roar = filt(r.standard_normal(n), "band", [150, 1800], SR, 2) * np.clip(t / 0.9, 0, 1) ** 1.5 * env_asr(n, SR, 0.02, 0.25) * 0.6
    swish = filt(r.standard_normal(n), "band", [1500, 6000], SR, 2) * env_asr(n, SR, 0.3, 0.3) * 0.12
    return whistle + roar + swish


def elytra_flap(seed: int) -> np.ndarray:
    """Flügelschlag der Feen-Elytren: ein kräftiger, weicher Luftstoß und ein winziges, helles Glitzern."""
    r = rng(seed)
    y = np.zeros(int(0.8 * SR))
    mix_at(y, wingbeat(seed, 0.75, 0.6), 0.0, 1.0)
    mix_at(y, glass(PENTA[int(r.integers(6, 10))], 0.6, 0.35, seed + 1), 0.04, 0.1)
    return y


# =====================================================================================
# Ausgabe
# =====================================================================================

# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil) oder None, Ziel-Pegel dBFS)
SYNTH = {
    "portal_ambient": (portal_ambient, 3, (2.0, 0.4), -27.0),
    "portal_light": (portal_light, 2, (1.8, 0.35), -17.0),
    "portal_travel": (portal_travel, 2, (1.6, 0.35), -19.0),
    "fairy_ambient": (fairy_ambient, 4, (1.2, 0.3), -25.0),
    "fairy_hurt": (fairy_hurt, 2, (0.6, 0.2), -21.0),
    "fairy_death": (fairy_death, 1, (1.4, 0.3), -21.0),
    "fairy_gift": (fairy_gift, 2, (1.4, 0.3), -19.0),
    "mushroomling_ambient": (mushroomling_ambient, 3, (0.4, 0.15), -24.0),
    "mushroomling_hurt": (mushroomling_hurt, 2, (0.4, 0.15), -21.0),
    "mushroomling_death": (mushroomling_death, 1, (0.6, 0.2), -21.0),
    "mushroomling_puff": (mushroomling_puff, 2, (0.6, 0.2), -20.0),
    "thorn_volley": (thorn_volley, 2, (0.8, 0.2), -17.0),
    "moth_ambient": (moth_ambient, 3, (1.6, 0.3), -23.0),
    "moth_hurt": (moth_hurt, 3, (1.0, 0.25), -18.0),
    "moth_death": (moth_death, 1, (2.4, 0.4), -17.0),
    "moth_screech": (moth_screech, 2, (2.0, 0.35), -16.0),
    "moth_awaken": (moth_awaken, 1, (2.4, 0.4), -17.0),
    "moth_gust": (moth_gust, 3, (1.6, 0.3), -15.0),
    "moth_scales": (moth_scales, 2, (1.4, 0.3), -21.0),
    "moth_beam_charge": (moth_beam_charge, 1, (1.8, 0.35), -19.0),
    "moth_beam": (moth_beam, 2, (1.8, 0.35), -15.0),
    "moth_lullaby": (moth_lullaby, 1, (2.6, 0.45), -18.0),
    "moth_dive": (moth_dive, 2, (1.2, 0.25), -16.0),
    "elytra_flap": (elytra_flap, 3, (0.8, 0.2), -19.0),
}

# Ereignis: Liste der Klänge (eigene über ihren Namen in SYNTH) und Reichweite der eigenen
EVENTS = {
    "block.fae_portal.ambient": (["portal_ambient"], 12),
    "block.fae_portal.light": (["portal_light"], 24),
    "block.fae_portal.travel": (["portal_travel"], None),
    "entity.fairy.ambient": (["fairy_ambient"], 12),
    "entity.fairy.hurt": (["fairy_hurt"], 16),
    "entity.fairy.death": (["fairy_death"], 16),
    "entity.fairy.gift": (["fairy_gift"], 16),
    "entity.mushroomling.ambient": (["mushroomling_ambient"], 12),
    "entity.mushroomling.hurt": (["mushroomling_hurt"], 16),
    "entity.mushroomling.death": (["mushroomling_death"], 16),
    "entity.mushroomling.puff": (["mushroomling_puff"], 16),
    "entity.thorn_wight.volley": (["thorn_volley"], 32),
    "entity.moth_queen.ambient": (["moth_ambient"], 32),
    "entity.moth_queen.hurt": (["moth_hurt"], 40),
    "entity.moth_queen.death": (["moth_death"], 64),
    "entity.moth_queen.screech": (["moth_screech"], 64),
    "entity.moth_queen.awaken": (["moth_awaken"], 64),
    "entity.moth_queen.gust": (["moth_gust"], 48),
    "entity.moth_queen.scales": (["moth_scales"], 40),
    "entity.moth_queen.beam_charge": (["moth_beam_charge"], 48),
    "entity.moth_queen.beam": (["moth_beam"], 48),
    "entity.moth_queen.lullaby": (["moth_lullaby"], 48),
    "entity.moth_queen.dive": (["moth_dive"], 48),
    "item.fae_elytra.flap": (["elytra_flap"], 16),
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
