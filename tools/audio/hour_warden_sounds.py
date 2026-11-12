#!/usr/bin/env python3
"""
Die Klänge des Stundenwächters (1.01): ein Ritter aus Uhrbronze mit einem Uhrwerk in der Brust und einem Zifferblatt hinter dem
Kopf. Wenn er wacht, ein ruhiges Tick-Tack über leisem Zahnradsurren; getroffen klirrt Bronze, eine Feder schnarrt; seine
Schritte sind schwer und metallisch (ein dumpfer Schlag, ein Klirren, ein winziges Ratschen). Sein Erwachen: ein Uhrwerk wird
aufgezogen (immer schnellere Ratschen), dann setzt das Ticken ein. Dazu die Angriffe: der Zeigerhieb (ein Sausen und ein
heller Klingenklang), der Stundenschlag (ein Aufprall und ein tiefer Glockenschlag), die Zahnräder (anschwellendes Surren,
ein metallisches Klirren beim Aufprall), der Zeitstopp (ein Sog, ein trockenes Klicken – dann Stille, in der ein gläserner Ton
steht), das Erstarren (ein schwebender, gläserner Klang), das Pendel (ein riesiges, tiefes Sausen, das Ächzen der Stange),
die Sanduhr (rinnender Sand, ein feines Glöckchen) und ihr Bersten (Glas springt, Sand prasselt), der Zeitsprung (ein
rückwärts gespieltes Sausen), das Rückspulen (Ticks, die rückwärts rasen, ein aufsteigender Sog), das Taumeln (eine Feder
springt, Teile klappern), die Zwölfte Stunde (Zahnräder kreischen schneller und schneller, eine Glocke) und die große Glocke
selbst. Im Sterben läuft das Uhrwerk aus: Ticks mit immer längeren Pausen, ein sinkendes Mahlen, die letzte Glocke, und dann
fallen Zahnräder klirrend zu Boden. Dazu der Minutenzeiger: die volle Stunde (eine helle Glocke) und sein Zeitsprung.

Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py, chronit_sounds.py, kraken_sounds.py); es werden keine
Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/hour_warden_sounds.py [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/abyss/hour_warden_<klang><n>.ogg,
    src/main/resources/assets/kingdomsatwar/sounds/item/minute_hand_<klang><n>.ogg
    Einträge "entity.hour_warden.<klang>" und "item.minute_hand.<klang>" in assets/kingdomsatwar/sounds.json (die übrigen bleiben)
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, bell, env_ad, env_asr, filt, fit_level, glide, resonator, rng, room, soft_clip, tone,  # noqa: E402
                          trim_tail, fade_edges)
from ambience_dsp import audible_bass  # noqa: E402
from chronit_sounds import clock_bell, tick  # noqa: E402
from dsp import write_ogg  # noqa: E402
from kraken_sounds import moving_resonator, soften  # noqa: E402
from sea_creatures import mix_at  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT_ENTITY = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/abyss"
OUT_ITEM = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/item"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100


# =====================================================================================
# Bausteine
# =====================================================================================

def clank(r, f0: float, dur: float, decay: float = 0.35, bright: float = 1.0) -> np.ndarray:
    """Angeschlagene Bronze: ein harter Anschlag, der durch vier unharmonische Resonanzen klingt."""
    n = int(dur * SR)
    x = r.standard_normal(n) * env_ad(n, SR, 0.0003, 0.004)
    y = np.zeros(n)
    for ratio, q, a in ((1.0, 40.0, 1.0), (2.71, 55.0, 0.6), (5.38, 70.0, 0.35 * bright), (8.13, 80.0, 0.18 * bright)):
        f = f0 * ratio * r.uniform(0.99, 1.01)
        if f < SR * 0.45:
            y += resonator(x, f, q, SR) * a
    y += filt(x, "high", 3500, SR, 2) * 0.4 * bright
    return y * env_ad(n, SR, 0.0005, decay)


def great_bell(f0: float, dur: float, seed: int, decay: float = 3.4) -> np.ndarray:
    """Eine große Turmglocke: Summton, Prime, kleine Terz, Quinte, Nominal und höhere Teiltöne, dazu der Anschlag."""
    b = bell(f0, dur, SR, ratios=(0.5, 1.0, 1.19, 1.5, 2.0, 2.52, 2.98, 4.05), amps=(0.75, 0.9, 0.55, 0.32, 1.0, 0.28, 0.22, 0.1),
             decay=decay, seed=seed)
    r = rng(seed + 7)
    n = len(b)
    strike = filt(r.standard_normal(n), "band", [1800, 5200], SR, 2) * env_ad(n, SR, 0.0005, 0.03) * 0.6
    fade = int(min(n * 0.3, 0.6 * SR))
    out = b + strike
    out[n - fade:] *= np.linspace(1.0, 0.0, fade) ** 2
    return out


def whoosh(r, dur: float, f0: float, f1: float, width: float = 1.3, peak: float = 0.5) -> np.ndarray:
    """Ein Sausen: Rauschen durch eine Resonanz, die von f0 nach f1 gleitet; am lautesten bei {@code peak} (Anteil)."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    env = np.where(t < dur * peak, (t / (dur * peak)) ** 2, ((dur - t) / (dur * (1 - peak))) ** 1.5)
    centre = f0 + (f1 - f0) * np.clip(t / dur, 0, 1)
    return moving_resonator(r.standard_normal(n), centre, width) * env


def ratchet(r, dur: float, rate0: float, rate1: float, f: float = 2600.0, weight: float = 0.5) -> np.ndarray:
    """Ein Ratschen (ein aufgezogenes Uhrwerk): Ticks, deren Takt von rate0 nach rate1 je Sekunde wandert."""
    n = int(dur * SR)
    y = np.zeros(n)
    t = 0.0
    while t < dur - 0.13:
        rate = rate0 + (rate1 - rate0) * t / dur
        mix_at(y, tick(r, f * r.uniform(0.95, 1.05), weight), t, 0.4 + 0.3 * r.random())
        t += 1.0 / max(1.0, rate)
    return y


def sand(r, dur: float, density: float = 900.0) -> np.ndarray:
    """Rinnender Sand: ein dichtes, feines Prasseln."""
    n = int(dur * SR)
    y = np.zeros(n)
    for _ in range(int(density * dur)):
        at = int(r.uniform(0, max(1, n - 300)))
        length = int(r.uniform(30, 220))
        y[at:at + length] += r.standard_normal(length) * np.exp(-np.arange(length) / (length * 0.2)) * r.uniform(0.1, 0.6)
    return filt(y, "band", [1500, 9000], SR, 2)


def glass_tone(f0: float, dur: float, seed: int) -> np.ndarray:
    """Ein stehender, gläserner Ton (wie ein gestrichenes Glas), leise schwebend."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    r = rng(seed)
    amp = env_asr(n, SR, 0.15, dur * 0.5)
    y = tone(f0 * (1.0 + 0.003 * np.sin(2 * np.pi * 4.5 * t)), amp, SR, harmonics=((1, 1.0), (2, 0.25), (3, 0.12), (5, 0.05)))
    y += tone(f0 * 1.501 * (1.0 + 0.002 * np.sin(2 * np.pi * 3.1 * t)), amp * 0.4, SR, harmonics=((1, 1.0), (2, 0.2)))
    return y + filt(r.standard_normal(n), "band", [f0 * 2.5, f0 * 4.0], SR, 2) * amp * 0.04


def spring(r, dur: float, f0: float) -> np.ndarray:
    """Eine Feder springt: ein schwingender, absinkender Ton mit Flattern."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = glide(n, f0, f0 * 0.55, 0.6) * (1.0 + 0.12 * np.sin(2 * np.pi * 22.0 * t) * np.exp(-t * 3.0))
    return tone(f, env_ad(n, SR, 0.002, dur * 0.45), SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.3), (4, 0.15))) * 0.5


def clatter(r, dur: float, count: int, lo: float = 900.0, hi: float = 2600.0) -> np.ndarray:
    """Teile, die klirrend aufschlagen und hüpfen."""
    n = int(dur * SR)
    y = np.zeros(n)
    for i in range(count):
        at = r.uniform(0, dur * 0.7)
        f = r.uniform(lo, hi)
        for b in range(r.integers(1, 4)):
            mix_at(y, clank(r, f * r.uniform(0.97, 1.03), 0.25, 0.08 + 0.05 * r.random(), 0.8), at + b * r.uniform(0.05, 0.12),
                   r.uniform(0.2, 0.6) / (1 + b))
    return y


def thump(dur: float, f0: float, f1: float, decay: float) -> np.ndarray:
    n = int(dur * SR)
    return tone(glide(n, f0, f1, 0.4), env_ad(n, SR, 0.002, decay), SR, harmonics=((1, 1.0), (2, 0.35), (3, 0.1)))


# =====================================================================================
# Der Stundenwächter
# =====================================================================================

def ambient(seed: int) -> np.ndarray:
    r = rng(seed)
    dur = 2.0
    y = np.zeros(int(dur * SR))
    for k in range(4):
        mix_at(y, tick(r, 1650 if k % 2 == 0 else 1250, 1.2), 0.05 + k * 0.48, 0.8 if k % 2 == 0 else 0.6)
    n = len(y)
    whirr = filt(r.standard_normal(n), "band", [600, 1400], SR, 2) * (0.5 + 0.5 * np.sin(2 * np.pi * 9.0 * np.arange(n) / SR)) ** 2
    y += whirr * env_asr(n, SR, 0.3, 0.5) * 0.08
    return y


def hurt(seed: int) -> np.ndarray:
    r = rng(seed)
    y = np.zeros(int(0.7 * SR))
    mix_at(y, clank(r, r.uniform(330, 420), 0.6, 0.32), 0.0, 1.0)
    mix_at(y, spring(r, 0.35, r.uniform(700, 900)), 0.03, 0.35)
    mix_at(y, ratchet(r, 0.25, 30, 45, 2900, 0.3), 0.08, 0.4)
    return y


def death(seed: int) -> np.ndarray:
    r = rng(seed)
    dur = 6.0
    n = int(dur * SR)
    y = np.zeros(n)
    t = np.arange(n) / SR
    # das Uhrwerk läuft aus: Ticks mit immer längeren Pausen
    at, gap = 0.0, 0.18
    k = 0
    while at < 3.2:
        mix_at(y, tick(r, 1500 - k * 25, 1.3), at, 0.7)
        at += gap
        gap *= 1.16
        k += 1
    # ein sinkendes Mahlen
    grind = moving_resonator(r.standard_normal(n), 900.0 * np.exp(-t / 2.0) + 120.0, 2.0)
    y += grind * env_asr(n, SR, 0.05, 2.5) * np.clip(1.0 - t / 3.4, 0, 1) * 0.5
    # die letzte Glocke, tief
    mix_at(y, great_bell(98.0, 3.6, seed + 3, 3.0), 3.0, 0.9)
    # Zahnräder fallen klirrend
    mix_at(y, clatter(r, 2.2, 9), 3.2, 0.7)
    return soften(y, 0.6)


def step(seed: int) -> np.ndarray:
    r = rng(seed)
    y = np.zeros(int(0.5 * SR))
    mix_at(y, thump(0.4, r.uniform(85, 105), 45, 0.18), 0.0, 1.0)
    mix_at(y, clank(r, r.uniform(500, 640), 0.3, 0.09, 0.6), 0.005, 0.35)
    mix_at(y, tick(r, r.uniform(2200, 2600), 0.3), 0.06, 0.25)
    return y


def awaken(seed: int) -> np.ndarray:
    r = rng(seed)
    dur = 3.0
    n = int(dur * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    # aufgezogen: Ratschen, immer schneller
    mix_at(y, ratchet(r, 1.4, 6, 26, 2300, 0.6), 0.0, 0.9)
    # ein anschwellendes Summen der Zahnräder
    hum = tone(glide(n, 60, 110, 0.8), env_asr(n, SR, 1.0, 1.0) * 0.5, SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.3), (5, 0.1)))
    y += hum * np.clip(t / 1.4, 0, 1)
    # dann setzt das Ticken ein
    for k in range(4):
        mix_at(y, tick(r, 1650 if k % 2 == 0 else 1250, 1.4), 1.5 + k * 0.33, 0.9)
    return soften(y, 0.5)


def sweep(seed: int) -> np.ndarray:
    r = rng(seed)
    y = np.zeros(int(0.9 * SR))
    mix_at(y, whoosh(r, 0.45, 500, 2600, 1.2, 0.6), 0.0, 1.0)
    shing = clank(r, r.uniform(1300, 1600), 0.7, 0.45, 1.3)
    mix_at(y, filt(shing, "high", 900, SR, 2), 0.22, 0.4)
    return y


def strike(seed: int) -> np.ndarray:
    r = rng(seed)
    y = np.zeros(int(3.4 * SR))
    mix_at(y, whoosh(r, 0.35, 300, 1200, 1.4, 0.8), 0.0, 0.5)
    mix_at(y, thump(0.8, 70, 34, 0.4), 0.3, 1.0)
    mix_at(y, clank(r, 240, 0.8, 0.3), 0.3, 0.7)
    mix_at(y, great_bell(185.0 + r.uniform(-6, 6), 3.0, seed + 5, 2.4), 0.32, 0.8)
    return y


def gears(seed: int) -> np.ndarray:
    r = rng(seed)
    dur = 0.9
    n = int(dur * SR)
    t = np.arange(n) / SR
    y = ratchet(r, dur, 20, 70, 3100, 0.4) * 0.6
    whirr = moving_resonator(r.standard_normal(n), 500 + 1800 * np.clip(t / dur, 0, 1), 2.5)
    y += whirr * env_asr(n, SR, 0.2, 0.3) * 0.5
    return y


def gear_hit(seed: int) -> np.ndarray:
    r = rng(seed)
    y = np.zeros(int(0.6 * SR))
    mix_at(y, clank(r, r.uniform(600, 820), 0.55, 0.22), 0.0, 1.0)
    mix_at(y, thump(0.2, 140, 80, 0.06), 0.0, 0.4)
    return y


def time_stop(seed: int) -> np.ndarray:
    r = rng(seed)
    dur = 3.0
    n = int(dur * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    # ein Sog, der anschwillt und abreißt
    swell = np.clip(t / 0.5, 0, 1) ** 3 * (t < 0.5)
    y += filt(r.standard_normal(n), "band", [700, 7000], SR, 2) * swell * 0.7
    # das Klicken
    mix_at(y, tick(r, 2100, 2.0), 0.5, 1.2)
    mix_at(y, clank(r, 880, 0.4, 0.06, 1.2), 0.5, 0.5)
    # dann Stille, in der ein gläserner Ton steht
    mix_at(y, glass_tone(1318.5, 2.3, seed + 11), 0.56, 0.35)
    mix_at(y, glass_tone(659.3, 2.3, seed + 12), 0.56, 0.25)
    return soften(y, 0.6)


def frozen(seed: int) -> np.ndarray:
    r = rng(seed)
    y = np.zeros(int(1.8 * SR))
    for i, f in enumerate((1568.0, 2093.0, 2637.0)):
        mix_at(y, clock_bell(f * r.uniform(0.99, 1.01), 1.4, seed + i, 1.6), i * 0.06, 0.35)
    mix_at(y, glass_tone(1046.5, 1.6, seed + 9), 0.0, 0.25)
    return y


def pendulum(seed: int) -> np.ndarray:
    r = rng(seed)
    dur = 1.8
    y = np.zeros(int(dur * SR))
    mix_at(y, whoosh(r, 1.6, 90, 420, 1.0, 0.55), 0.0, 1.0)
    mix_at(y, whoosh(r, 1.4, 220, 900, 1.6, 0.55) * 0.4, 0.1, 1.0)
    # das Ächzen der Stange
    n = int(0.5 * SR)
    creak = moving_resonator(r.standard_normal(n), 260 + 80 * np.sin(np.linspace(0, 6, n)), 6.0) * env_asr(n, SR, 0.05, 0.3)
    mix_at(y, creak * 0.25, 0.0, 1.0)
    return y


def hourglass(seed: int) -> np.ndarray:
    r = rng(seed)
    dur = 2.4
    y = np.zeros(int(dur * SR))
    n = len(y)
    y += sand(r, dur, 1400) * env_asr(n, SR, 0.3, 0.8) * 0.8
    mix_at(y, clock_bell(1760.0, 1.2, seed + 3, 1.2), 0.1, 0.4)
    mix_at(y, clock_bell(2349.0, 1.0, seed + 4, 1.0), 0.28, 0.25)
    return y


def hourglass_burst(seed: int) -> np.ndarray:
    r = rng(seed)
    y = np.zeros(int(1.4 * SR))
    mix_at(y, thump(0.6, 90, 45, 0.25), 0.0, 0.8)
    n = int(0.5 * SR)
    crack = filt(r.standard_normal(n), "band", [2500, 9000], SR, 2) * env_ad(n, SR, 0.0004, 0.06)
    mix_at(y, crack, 0.0, 0.7)
    mix_at(y, sand(r, 1.0, 2600) * env_ad(int(1.0 * SR), SR, 0.002, 0.35), 0.01, 1.0)
    for i in range(5):
        mix_at(y, clank(r, r.uniform(2400, 4200), 0.2, 0.05, 1.4), r.uniform(0.0, 0.2), 0.15)
    return y


def blink(seed: int) -> np.ndarray:
    r = rng(seed)
    w = whoosh(r, 0.6, 2400, 400, 1.2, 0.85)
    y = np.zeros(int(0.8 * SR))
    mix_at(y, w, 0.0, 1.0)
    mix_at(y, tick(r, 2200, 1.0), 0.52, 0.6)
    return y


def rewind(seed: int) -> np.ndarray:
    r = rng(seed)
    dur = 3.2
    n = int(dur * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    # Ticks, die rückwärts rasen (jeder Tick rückwärts gespielt, immer dichter)
    at, gap = 0.05, 0.2
    while at < dur - 0.3:
        tk = tick(r, 1900 + 400 * r.random(), 0.8)[::-1]
        mix_at(y, tk, at, 0.4 + 0.3 * at / dur)
        at += gap
        gap = max(0.03, gap * 0.9)
    # ein aufsteigender Sog
    swell = np.clip(t / dur, 0, 1) ** 1.5
    y += moving_resonator(r.standard_normal(n), 200 + 1600 * swell, 1.8) * swell * 0.5
    y += tone(80 + 240 * swell, swell * 0.3, SR, harmonics=((1, 1.0), (2, 0.4)))
    return soften(y, 0.25)


def stagger(seed: int) -> np.ndarray:
    r = rng(seed)
    y = np.zeros(int(1.6 * SR))
    mix_at(y, spring(r, 0.8, r.uniform(420, 520)), 0.0, 1.0)
    mix_at(y, clank(r, 300, 0.6, 0.25), 0.0, 0.6)
    mix_at(y, clatter(r, 1.2, 6, 1200, 3200), 0.08, 0.6)
    # ein Tick, der aus dem Takt springt
    for at in (0.3, 0.42, 0.75, 0.81, 1.2):
        mix_at(y, tick(r, 1500, 1.0), at, 0.4)
    return y


def fury(seed: int) -> np.ndarray:
    r = rng(seed)
    dur = 3.4
    n = int(dur * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    # Zahnräder kreischen, schneller und schneller
    mix_at(y, ratchet(r, 1.8, 15, 90, 3000, 0.5), 0.0, 0.8)
    scream_f = 300 + 900 * np.clip(t / 1.8, 0, 1) ** 1.4
    scream = moving_resonator(r.standard_normal(n), scream_f, 8.0) * env_asr(n, SR, 0.4, 0.6) * np.clip(1.0 - (t - 1.8) / 0.4, 0, 1)
    y += scream * 0.6
    y += tone(scream_f * 0.5, env_asr(n, SR, 0.4, 0.6) * 0.25 * np.clip(1.0 - (t - 1.8) / 0.4, 0, 1), SR,
              harmonics=((1, 1.0), (2, 0.6), (3, 0.4), (4, 0.25), (5, 0.15)))
    mix_at(y, great_bell(147.0, 3.0, seed + 5, 2.6), 1.8, 1.0)
    return soften(y, 0.5)


def toll(seed: int) -> np.ndarray:
    r = rng(seed)
    y = np.zeros(int(4.6 * SR))
    mix_at(y, great_bell(130.8 + r.uniform(-2, 2), 4.4, seed, 3.6), 0.0, 1.0)
    return y


# =====================================================================================
# Der Minutenzeiger
# =====================================================================================

def hand_hour(seed: int) -> np.ndarray:
    r = rng(seed)
    y = np.zeros(int(2.6 * SR))
    mix_at(y, clank(r, 980, 0.5, 0.15, 1.0), 0.0, 0.5)
    mix_at(y, great_bell(392.0, 2.4, seed + 1, 1.8), 0.0, 0.8)
    mix_at(y, clock_bell(1568.0, 1.2, seed + 2, 1.2), 0.04, 0.3)
    return y


def hand_jump(seed: int) -> np.ndarray:
    r = rng(seed)
    y = np.zeros(int(0.7 * SR))
    mix_at(y, whoosh(r, 0.5, 2800, 600, 1.2, 0.8), 0.0, 0.9)
    mix_at(y, tick(r, 2400, 0.8), 0.42, 0.6)
    return y


# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil) oder None, Ziel-Pegel dBFS, Ordner/Präfix, Ereignis, Reichweite)
SYNTH = {
    "ambient": (ambient, 3, (1.4, 0.3), -22.0, "entity", 20),
    "hurt": (hurt, 3, (1.0, 0.2), -16.0, "entity", 32),
    "death": (death, 1, (3.0, 0.4), -13.0, "entity", 64),
    "step": (step, 4, (0.8, 0.2), -21.0, "entity", 20),
    "awaken": (awaken, 1, (2.4, 0.35), -15.0, "entity", 48),
    "sweep": (sweep, 3, (1.0, 0.25), -15.0, "entity", 32),
    "strike": (strike, 2, (2.4, 0.35), -13.0, "entity", 48),
    "gears": (gears, 3, (1.0, 0.25), -17.0, "entity", 32),
    "gear_hit": (gear_hit, 3, (0.8, 0.2), -17.0, "entity", 24),
    "time_stop": (time_stop, 1, (2.6, 0.4), -14.0, "entity", 64),
    "frozen": (frozen, 2, (1.6, 0.3), -18.0, "entity", 16),
    "pendulum": (pendulum, 2, (2.0, 0.35), -14.0, "entity", 48),
    "hourglass": (hourglass, 2, (1.4, 0.3), -17.0, "entity", 32),
    "hourglass_burst": (hourglass_burst, 2, (1.2, 0.25), -14.0, "entity", 32),
    "blink": (blink, 3, (1.0, 0.25), -16.0, "entity", 32),
    "rewind": (rewind, 2, (2.0, 0.35), -14.0, "entity", 48),
    "stagger": (stagger, 2, (1.2, 0.25), -14.0, "entity", 40),
    "fury": (fury, 1, (2.6, 0.4), -13.0, "entity", 64),
    "toll": (toll, 2, (3.2, 0.45), -12.0, "entity", 96),
    "hour": (hand_hour, 2, (1.8, 0.3), -14.0, "item", 24),
    "jump": (hand_jump, 2, (0.8, 0.2), -16.0, "item", 16),
}


# Seit 1.1.4: diese sehr tiefen Klänge bekommen Obertöne ihres Basses, damit sie auch auf kleinen Lautsprechern
# (Laptop, Fernseher, Kopfhörer ohne Bass) zu hören sind – siehe ambience_dsp.audible_bass.
AUDIBLE = {"step"}


def files_of(name: str) -> list[str]:
    count, kind = SYNTH[name][1], SYNTH[name][4]
    if kind == "entity":
        return [f"kingdomsatwar:entity/abyss/hour_warden_{name}{i + 1}" for i in range(count)]
    return [f"kingdomsatwar:item/minute_hand_{name}{i + 1}" for i in range(count)]


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level, kind, _ = SYNTH[name]
    files = []
    for i in range(count):
        x = fn(13100 + i * 41 + zlib.crc32(name.encode()) % 991)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 3100 + i, stereo=False, damping=5600), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.05)
        if name in AUDIBLE:
            x = audible_bass(x, SR)
        x = soft_clip(fit_level(x, level, -2.0))
        if kind == "entity":
            path = OUT_ENTITY / f"hour_warden_{name}{i + 1}.ogg"
            files.append(f"kingdomsatwar:entity/abyss/hour_warden_{name}{i + 1}")
        else:
            path = OUT_ITEM / f"minute_hand_{name}{i + 1}.ogg"
            files.append(f"kingdomsatwar:item/minute_hand_{name}{i + 1}")
        write_ogg(x.astype(np.float32), SR, path, quality=5.0)
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
    OUT_ENTITY.mkdir(parents=True, exist_ok=True)
    OUT_ITEM.mkdir(parents=True, exist_ok=True)
    events = {}
    for name, (_, _, _, _, kind, reach) in SYNTH.items():
        files = render(name, args.preview) if not args.only or name in args.only else files_of(name)
        key = f"entity.hour_warden.{name}" if kind == "entity" else f"item.minute_hand.{name}"
        events[key] = {"sounds": [{"name": f, "attenuation_distance": reach} for f in files], "subtitle": f"subtitles.kingdomsatwar.{key}"}
    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8")) if SOUNDS_JSON.exists() else {}
    data.update(events)
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"{len(events)} Klangereignisse geschrieben")


if __name__ == "__main__":
    main()
