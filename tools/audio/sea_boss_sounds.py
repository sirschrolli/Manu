#!/usr/bin/env python3
"""
Eigene Klänge für den Kraken, Karkinos (den Riffkoloss) und Nyxar (seit 1.1.3) – bisher liehen sie sich für Biss, Schmerz,
Tinte, Schläge, Wellen, Wirbel, Graben, Ausbruch, Lanze, Schritte, die Gezeitenkraft, den Schattenatem und den Schrei Klänge
von Minecraft (Verwüster, Tintenfisch, Wächter, Delfin, Schnüffler, Schildkröte, Dreizack, Ghast).

- Kraken: ein riesiger Kopffüßer unter Wasser – ein harter Schnabel, gurgelnde, tiefe Stimme, Tintenstrahl, Tentakel, die
  aufs Wasser schlagen, eine Woge, die sich aufbaut und bricht, ein Strudel.
- Karkinos: ein Koloss aus Panzer und Riff – Chitin, das knackt und klickt, Sand, der rieselt und spritzt, ein Strahl aus
  Gezeitenwasser, eine tiefe, fremde Flut.
- Nyxar: der Drache der Schatten – ein dunkler, saugender Atem mit schwebenden, verstimmten Tönen und ein geisterhafter Schrei.

Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py, monster_sounds.py, dragon_sounds.py); keine fremden Dateien.

Aufruf (aus dem Projektordner):
    python3 tools/audio/sea_boss_sounds.py [--only name ...] [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/sea/<name><n>.ogg (Kraken, Karkinos)
    src/main/resources/assets/kingdomsatwar/sounds/entity/shadow/<name><n>.ogg (Nyxar)
    die Einträge in assets/kingdomsatwar/sounds.json (Untertitel bleiben)
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import apply_room, colored_noise, curve, fade_edges, filt, glide, rng, room, tone, trim_tail  # noqa: E402
from dragon_sounds import fit, fit_lufs, gust, morph_voice, saturate, sparkle  # noqa: E402
from dsp import write_ogg  # noqa: E402
from monster_sounds import (SR, bubbles, clack, crackle, frames_len, mix_at, norm, pulse_voice, rumble, squelch, swept, thud,  # noqa: E402
                            tone_voice, whisper)

ROOT = Path(__file__).resolve().parent.parent.parent
SOUNDS = ROOT / "src/main/resources/assets/kingdomsatwar/sounds"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"


# =====================================================================================
# Wasser
# =====================================================================================

def splash(dur: float, seed: int, size: float = 1.0) -> np.ndarray:
    """Ein großer Platscher: ein breiter Rauschstoß, dessen Höhen rasch sinken, dazu fallende Tropfen."""
    n = frames_len(dur)
    t = np.arange(n) / SR
    body = colored_noise(n, SR, seed, -2.0, lo=120.0, hi=9000.0)
    # die Höhen schließen sich (große Masse Wasser): zwei Bänder, das obere klingt schneller ab
    low = filt(body, "low", 1400.0 * size ** -0.3, SR, 2) * np.exp(-t / (0.35 * dur))
    high = filt(body, "high", 1800.0, SR, 2) * np.exp(-t / (0.12 * dur))
    attack = np.minimum(1.0, t / 0.006)
    y = (low + 0.55 * high) * attack
    drops = fit(crackle(dur, seed + 1, 260.0 * size, lo=1400.0, hi=6500.0, env=[(0.0, 0.0), (0.1, 1.0), (0.6, 0.35), (1.0, 0.0)]), n)
    y = norm(y) + 0.22 * drops
    y += 0.25 * fit(bubbles(dur, seed + 2, 30.0, 240.0, 1200.0, env=[(0.0, 0.0), (0.15, 1.0), (1.0, 0.0)]), n)
    return norm(y)


def wash(dur: float, seed: int, center: float, amps) -> np.ndarray:
    """Rauschendes Wasser, das hin und her wogt."""
    return swept(dur, [(0.0, center * 0.8), (0.5, center * 1.15), (1.0, center * 0.7)], 2.2, amps, seed, flicker=0.3,
                 flicker_rate=(3.0, 6.0))


def murky(x: np.ndarray, cutoff: float = 2600.0) -> np.ndarray:
    """Unter Wasser gedämpft: Höhen weg, etwas mehr Bauch."""
    return filt(x, "low", cutoff, SR, 4)


# =====================================================================================
# Kraken
# =====================================================================================

def kraken_bite(seed: int) -> np.ndarray:
    """Der Schnabel schnappt zu: verdrängtes Wasser, ein harter, hohler Schnabel, Knirschen, ein nasses Schmatzen."""
    r = rng(seed)
    y = np.zeros(int(0.9 * SR))
    mix_at(y, murky(gust(0.16, [(0.0, 500.0), (1.0, 1200.0)], 1.4, [(0.0, 0.0), (0.7, 1.0), (1.0, 0.3)], seed)), 0.0, 0.45)
    beak = [(r.uniform(520.0, 640.0), 1.0), (r.uniform(1150.0, 1350.0), 0.7), (r.uniform(2100.0, 2400.0), 0.35)]
    mix_at(y, clack(beak, 0.045, seed + 1, q=7.0), 0.14, 1.0)
    mix_at(y, clack([(f * 1.04, g) for f, g in beak], 0.03, seed + 2, q=6.0), 0.162, 0.45)
    mix_at(y, crackle(0.16, seed + 3, 900.0, lo=500.0, hi=2600.0, env=[(0.0, 1.0), (1.0, 0.0)]), 0.15, 0.4)
    mix_at(y, murky(squelch(0.32, seed + 4, hi=1300.0, lo=240.0)), 0.18, 0.55)
    mix_at(y, thud(0.4, r.uniform(68.0, 78.0), 40.0, seed + 5, noise_hi=260.0, grit=0.5), 0.14, 0.55)
    return saturate(y, 1.4)


def kraken_hurt(seed: int) -> np.ndarray:
    """Ein gurgelnder, tiefer Aufschrei aus einem riesigen Leib, Wasser und Blasen quellen hervor."""
    r = rng(seed)
    dur = r.uniform(0.85, 1.1)
    n = frames_len(dur)
    peak = r.uniform(88.0, 102.0)
    f = curve(n, [(0.0, peak * 0.7), (0.18, peak), (0.5, peak * 0.86), (1.0, peak * 0.55)])
    f = f * (1.0 + 0.05 * np.sin(2 * np.pi * r.uniform(7.0, 11.0) * np.arange(n) / SR))   # gurgelndes Zittern
    amp = curve(n, [(0.0, 0.0), (0.06, 1.0), (0.4, 0.8), (1.0, 0.0)])
    voice = morph_voice(f, amp, seed, [(0.0, "o"), (0.5, "u"), (1.0, "u")], 0.5, jitter=0.1, shimmer=0.25, sub=0.45,
                        breath=0.45, bright=2400.0)
    y = np.zeros(n + int(0.5 * SR))
    mix_at(y, murky(saturate(voice, 2.0), 2200.0), 0.0, 1.0)
    mix_at(y, bubbles(dur + 0.3, seed + 1, 55.0, 140.0, 700.0, cycles=(6.0, 14.0), env=[(0.0, 0.2), (0.2, 1.0), (1.0, 0.0)]), 0.05, 0.5)
    mix_at(y, wash(dur, seed + 2, 420.0, [(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)]), 0.0, 0.3)
    return y


def kraken_ink(seed: int) -> np.ndarray:
    """Eine Wolke Tinte: ein dumpfes Ausstoßen, ein gedrückter Strahl, Blasen, die nachquellen."""
    r = rng(seed)
    dur = r.uniform(1.1, 1.35)
    y = np.zeros(int((dur + 0.5) * SR))
    mix_at(y, thud(0.35, 58.0, 36.0, seed, noise_hi=220.0, grit=0.4), 0.0, 0.6)
    jet = swept(dur, [(0.0, 700.0), (0.25, 520.0), (1.0, 260.0)], 1.7, [(0.0, 0.0), (0.05, 1.0), (0.5, 0.65), (1.0, 0.0)],
                seed + 1, flicker=0.35, flicker_rate=(16.0, 26.0))
    mix_at(y, murky(jet, 2000.0), 0.02, 1.0)
    mix_at(y, murky(squelch(0.4, seed + 2, hi=1100.0, lo=200.0)), 0.0, 0.5)
    mix_at(y, bubbles(dur, seed + 3, 40.0, 160.0, 800.0, env=[(0.0, 0.1), (0.3, 1.0), (1.0, 0.0)]), 0.15, 0.45)
    return y


def kraken_slam(seed: int) -> np.ndarray:
    """Ein Tentakel peitscht herab und schlägt aufs Wasser: Zischen, ein dumpfer Schlag, eine Wand aus Gischt, Grollen."""
    r = rng(seed)
    y = np.zeros(int(2.2 * SR))
    whip = gust(0.28, [(0.0, 380.0), (0.8, 1500.0), (1.0, 900.0)], 1.3, [(0.0, 0.0), (0.85, 1.0), (1.0, 0.2)], seed, flicker=0.15)
    mix_at(y, whip, 0.0, 0.5)
    hit = 0.26
    mix_at(y, thud(0.7, r.uniform(55.0, 62.0), 30.0, seed + 1, noise_hi=420.0, grit=0.9), hit, 1.0)
    mix_at(y, splash(1.5, seed + 2, 1.4), hit, 0.85)
    mix_at(y, rumble(1.6, seed + 3, 120.0, [(0.0, 1.0), (0.4, 0.6), (1.0, 0.0)]), hit, 0.45)
    return saturate(y, 1.3)


def kraken_wave(seed: int) -> np.ndarray:
    """Eine Woge baut sich auf, türmt sich und bricht – danach zieht das Wasser rauschend ab."""
    r = rng(seed)
    dur = r.uniform(2.1, 2.5)
    y = np.zeros(int((dur + 0.8) * SR))
    swell = swept(1.2, [(0.0, 260.0), (1.0, 820.0)], 2.0, [(0.0, 0.0), (0.8, 1.0), (1.0, 1.0)], seed, flicker=0.2, flicker_rate=(4.0, 7.0))
    mix_at(y, swell, 0.0, 0.7)
    mix_at(y, rumble(1.3, seed + 1, 110.0, [(0.0, 0.0), (0.85, 1.0), (1.0, 0.8)]), 0.0, 0.4)
    crash = 1.15
    mix_at(y, splash(1.4, seed + 2, 1.6), crash, 1.0)
    mix_at(y, wash(dur - crash + 0.4, seed + 3, 650.0, [(0.0, 1.0), (0.4, 0.6), (1.0, 0.0)]), crash + 0.1, 0.55)
    return y


def kraken_whirl(seed: int) -> np.ndarray:
    """Ein Strudel: kreisendes, saugendes Wasser, gurgelnde Blasen, ein tiefes Brausen."""
    r = rng(seed)
    dur = r.uniform(2.4, 2.8)
    n = frames_len(dur)
    t = np.arange(n) / SR
    spin = r.uniform(1.3, 1.8)
    base = colored_noise(n, SR, seed, -3.0, lo=80.0, hi=4000.0)
    # die Mitte des Rauschens kreist (zwei Bänder im Wechsel), die Lautstärke pulst im Takt des Wirbels
    a = filt(filt(base, "high", 260.0, SR, 2), "low", 560.0, SR, 2)
    b = filt(filt(base, "high", 520.0, SR, 2), "low", 1100.0, SR, 2)
    phase = 2 * np.pi * spin * t
    y = norm(a) * (0.6 + 0.4 * np.sin(phase)) + 0.7 * norm(b) * (0.6 + 0.4 * np.sin(phase + np.pi))
    y = y * (0.85 + 0.15 * np.sin(2 * np.pi * spin * 2.0 * t + 1.0))
    env = curve(n, [(0.0, 0.0), (0.25, 1.0), (0.75, 0.9), (1.0, 0.0)])
    out = np.zeros(n + int(0.4 * SR))
    mix_at(out, norm(y) * env, 0.0, 0.85)
    mix_at(out, bubbles(dur, seed + 1, 34.0, 110.0, 520.0, cycles=(6.0, 12.0), env=[(0.0, 0.0), (0.3, 1.0), (0.8, 0.8), (1.0, 0.0)]), 0.0, 0.45)
    mix_at(out, rumble(dur, seed + 2, 90.0, [(0.0, 0.0), (0.3, 1.0), (0.8, 0.9), (1.0, 0.0)]), 0.0, 0.5)
    return out


# =====================================================================================
# Karkinos, der Riffkoloss
# =====================================================================================

def chitin(seed: int, low: float = 1.0) -> np.ndarray:
    """Ein Schlag auf Chitin: hohl, kurz, nicht klirrend."""
    r = rng(seed)
    modes = [(r.uniform(700.0, 860.0) * low, 1.0), (r.uniform(1500.0, 1800.0) * low, 0.6), (r.uniform(2500.0, 2900.0) * low, 0.3)]
    return clack(modes, 0.03, seed, q=6.5)


def reef_burrow(seed: int) -> np.ndarray:
    """Er gräbt sich ein: Schaufeln voll Sand, rieselnder Sand, die Beine kratzen, darunter ein Grollen."""
    r = rng(seed)
    dur = r.uniform(1.5, 1.8)
    y = np.zeros(int((dur + 0.4) * SR))
    mix_at(y, rumble(dur, seed, 140.0, [(0.0, 0.0), (0.2, 1.0), (0.8, 0.8), (1.0, 0.0)]), 0.0, 0.45)
    scoops = int(r.integers(4, 6))
    for k in range(scoops):
        at = k * dur / scoops + r.uniform(0.0, 0.08)
        mix_at(y, crackle(0.3, seed + 10 + k, 1100.0, lo=350.0, hi=2400.0, env=[(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)]), at, 0.8)
        mix_at(y, swept(0.32, [(0.0, 1800.0), (1.0, 900.0)], 1.4, [(0.0, 0.0), (0.25, 1.0), (1.0, 0.0)], seed + 20 + k), at, 0.35)
        mix_at(y, chitin(seed + 30 + k, 0.8), at + 0.02, 0.18)
    mix_at(y, swept(dur, [(0.0, 3200.0), (1.0, 2600.0)], 1.0, [(0.0, 0.0), (0.3, 1.0), (1.0, 0.0)], seed + 2, flicker=0.4), 0.1, 0.12)
    return y


def reef_erupt(seed: int) -> np.ndarray:
    """Er bricht aus dem Sand: ein Beben schwillt an, der Boden birst, Sand und Brocken prasseln nieder."""
    r = rng(seed)
    y = np.zeros(int(3.0 * SR))
    mix_at(y, rumble(1.3, seed, 120.0, [(0.0, 0.0), (0.9, 1.0), (1.0, 1.0)]), 0.0, 0.6)
    # das Beben kündigt sich an: der Boden mahlt, Sand rieselt immer stärker
    mix_at(y, swept(1.1, [(0.0, 220.0), (1.0, 480.0)], 1.6, [(0.0, 0.0), (0.9, 1.0), (1.0, 0.8)], seed + 6, flicker=0.4,
                    flicker_rate=(10.0, 18.0)), 0.0, 0.5)
    mix_at(y, crackle(1.1, seed + 7, 500.0, lo=400.0, hi=2400.0, env=[(0.0, 0.0), (0.6, 0.4), (1.0, 1.0)]), 0.0, 0.45)
    burst = 1.05
    mix_at(y, thud(0.9, 52.0, 28.0, seed + 1, noise_hi=500.0, grit=1.0), burst, 1.0)
    mix_at(y, crackle(1.4, seed + 2, 1300.0, lo=300.0, hi=3000.0, env=[(0.0, 1.0), (0.3, 0.6), (1.0, 0.0)]), burst, 0.8)
    mix_at(y, swept(1.2, [(0.0, 1500.0), (1.0, 600.0)], 2.0, [(0.0, 1.0), (0.3, 0.6), (1.0, 0.0)], seed + 3, flicker=0.3), burst, 0.55)
    mix_at(y, rumble(1.6, seed + 4, 160.0, [(0.0, 1.0), (1.0, 0.0)]), burst, 0.5)
    for k in range(int(r.integers(4, 7))):
        mix_at(y, thud(0.25, r.uniform(90.0, 140.0), 60.0, seed + 10 + k, noise_hi=700.0, grit=0.9), burst + r.uniform(0.2, 1.4),
               r.uniform(0.25, 0.45))
    mix_at(y, chitin(seed + 5, 0.7), burst + 0.05, 0.35)
    return saturate(y, 1.25)


def reef_hurt(seed: int) -> np.ndarray:
    """Getroffen: der Panzer knackt, ein kurzes, tiefes Brummen, ein paar Blasen."""
    r = rng(seed)
    y = np.zeros(int(0.75 * SR))
    mix_at(y, chitin(seed, r.uniform(0.85, 1.1)), 0.0, 1.0)
    mix_at(y, crackle(0.12, seed + 1, 1400.0, lo=900.0, hi=3600.0, env=[(0.0, 1.0), (1.0, 0.0)]), 0.0, 0.45)
    dur = r.uniform(0.38, 0.5)
    n = frames_len(dur)
    f = curve(n, [(0.0, 96.0), (0.25, 104.0), (1.0, 70.0)]) * r.uniform(0.92, 1.08)
    amp = curve(n, [(0.0, 0.0), (0.1, 1.0), (1.0, 0.0)])
    grunt = pulse_voice(f, amp, seed + 2, "o", size=0.55, jitter=0.1, shimmer=0.25, sub=0.4, breath=0.35, bright=1800.0)
    mix_at(y, murky(saturate(grunt, 1.8), 1800.0), 0.03, 0.75)
    mix_at(y, bubbles(0.5, seed + 3, 30.0, 200.0, 900.0), 0.05, 0.3)
    return y


def reef_lance(seed: int) -> np.ndarray:
    """Die Lanze aus Gezeitenwasser: ein Strahl unter gewaltigem Druck schießt hervor, schlägt ein, ein tiefer Schlag hallt."""
    r = rng(seed)
    y = np.zeros(int(2.0 * SR))
    jet = swept(0.65, [(0.0, 500.0), (0.6, 1700.0), (1.0, 2300.0)], 1.5, [(0.0, 0.0), (0.2, 0.7), (1.0, 1.0)], seed, flicker=0.35,
                flicker_rate=(22.0, 34.0))
    mix_at(y, jet, 0.0, 0.8)
    mix_at(y, sparkle(0.9, seed + 1, 30.0, 1800.0, 4200.0, decay=(0.05, 0.14)), 0.1, 0.18)
    hit = 0.62
    mix_at(y, splash(1.1, seed + 2, 1.1), hit, 0.75)
    boom = thud(1.1, r.uniform(48.0, 54.0), 28.0, seed + 3, noise_hi=300.0, grit=0.6)
    mix_at(y, boom, hit, 0.9)
    mix_at(y, rumble(1.2, seed + 4, 100.0, [(0.0, 1.0), (1.0, 0.0)]), hit, 0.4)
    return saturate(y, 1.2)


def reef_slam(seed: int) -> np.ndarray:
    """Die Schere schlägt nieder: ein tiefer Aufprall, hohles Chitin, Sand spritzt, der Boden grollt nach."""
    r = rng(seed)
    y = np.zeros(int(1.5 * SR))
    mix_at(y, gust(0.18, [(0.0, 300.0), (1.0, 900.0)], 1.3, [(0.0, 0.0), (0.9, 1.0), (1.0, 0.3)], seed), 0.0, 0.35)
    hit = 0.16
    mix_at(y, thud(0.75, r.uniform(52.0, 58.0), 30.0, seed + 1, noise_hi=460.0, grit=0.9), hit, 0.55)
    knock = swept(0.22, [(0.0, 420.0), (1.0, 190.0)], 1.3, [(0.0, 0.0), (0.04, 1.0), (0.35, 0.45), (1.0, 0.0)], seed + 5)
    mix_at(y, knock, hit, 1.0)
    mix_at(y, chitin(seed + 2, 0.8), hit, 0.9)
    mix_at(y, chitin(seed + 6, 1.05), hit + 0.025, 0.4)
    mix_at(y, crackle(0.7, seed + 3, 1300.0, lo=350.0, hi=2800.0, env=[(0.0, 1.0), (0.4, 0.5), (1.0, 0.0)]), hit, 0.9)
    mix_at(y, rumble(1.1, seed + 4, 120.0, [(0.0, 1.0), (1.0, 0.0)]), hit, 0.3)
    return saturate(y, 1.3)


def reef_step(seed: int) -> np.ndarray:
    """Ein schwerer Schritt: ein dumpfer Tritt in den Sand, die Beinspitzen klicken nacheinander auf."""
    r = rng(seed)
    y = np.zeros(int(0.55 * SR))
    mix_at(y, thud(0.35, r.uniform(70.0, 82.0), 44.0, seed, noise_hi=300.0, grit=0.6), 0.0, 0.45)
    knock = swept(0.16, [(0.0, 360.0), (1.0, 180.0)], 1.2, [(0.0, 0.0), (0.05, 1.0), (0.4, 0.4), (1.0, 0.0)], seed + 5)
    mix_at(y, knock, 0.0, 0.9)
    mix_at(y, crackle(0.18, seed + 1, 900.0, lo=400.0, hi=2600.0, env=[(0.0, 1.0), (1.0, 0.0)]), 0.0, 0.6)
    for k in range(3):
        modes = [(r.uniform(1700.0, 2100.0), 1.0), (r.uniform(2900.0, 3400.0), 0.45)]
        mix_at(y, clack(modes, 0.014, seed + 2 + k, q=8.0), 0.02 + 0.045 * k + r.uniform(0.0, 0.015), 0.3)
    return y


def reef_tide(seed: int) -> np.ndarray:
    """Die Gezeitenkraft: ein tiefer, fremder Ruf aus dem Riff, das Wasser hebt sich, Licht glitzert darin."""
    r = rng(seed)
    dur = r.uniform(2.1, 2.4)
    n = frames_len(dur)
    t = np.arange(n) / SR
    base = r.uniform(62.0, 68.0)
    f = curve(n, [(0.0, base * 0.9), (0.4, base * 1.12), (1.0, base * 0.95)]) * (1.0 + 0.012 * np.sin(2 * np.pi * 4.5 * t))
    amp = curve(n, [(0.0, 0.0), (0.25, 1.0), (0.75, 0.85), (1.0, 0.0)])
    call = tone_voice(f, amp, seed, harmonics=((1, 1.0), (2, 0.5), (3, 0.3), (5, 0.12), (7, 0.05)), vowel="o", wet=0.5,
                      breath=0.15, chorus=0.25)
    fifth = tone_voice(f * 1.5, amp * 0.6, seed + 1, harmonics=((1, 1.0), (2, 0.3), (3, 0.1)), vowel="u", wet=0.5, chorus=0.2)
    y = np.zeros(n + int(0.8 * SR))
    mix_at(y, call, 0.0, 0.8)
    mix_at(y, fifth, 0.0, 0.35)
    mix_at(y, swept(dur, [(0.0, 240.0), (0.6, 640.0), (1.0, 420.0)], 2.0, [(0.0, 0.0), (0.4, 1.0), (1.0, 0.0)], seed + 2, flicker=0.2,
                    flicker_rate=(3.0, 5.0)), 0.0, 0.75)
    mix_at(y, sparkle(dur, seed + 3, 18.0, 1500.0, 3600.0, decay=(0.08, 0.2), env=[(0.0, 0.0), (0.5, 1.0), (1.0, 0.2)]), 0.0, 0.15)
    return y


# =====================================================================================
# Nyxar
# =====================================================================================

def nyxar_breath(seed: int) -> np.ndarray:
    """Der Schattenatem: erst ein Einsaugen, dann ein dunkler, rauschender Strom, darin schwebende, verstimmte Töne und Flüstern."""
    r = rng(seed)
    dur = r.uniform(1.7, 2.0)
    n = frames_len(dur)
    t = np.arange(n) / SR
    y = np.zeros(n + int(0.9 * SR))
    inhale = swept(0.45, [(0.0, 900.0), (1.0, 1500.0)], 1.4, [(0.0, 0.0), (0.8, 1.0), (1.0, 0.0)], seed, flicker=0.3)
    mix_at(y, inhale, 0.0, 0.35)
    stream = swept(dur, [(0.0, 420.0), (0.4, 300.0), (1.0, 220.0)], 2.2, [(0.0, 0.0), (0.1, 1.0), (0.8, 0.8), (1.0, 0.0)],
                   seed + 1, flicker=0.3, flicker_rate=(8.0, 14.0))
    mix_at(y, filt(stream, "low", 3200.0, SR, 4), 0.35, 0.9)
    amp = curve(n, [(0.0, 0.0), (0.15, 1.0), (0.8, 0.8), (1.0, 0.0)])
    drone = np.zeros(n)
    for f0, g in ((55.0, 1.0), (58.4, 0.8), (82.4, 0.45), (116.5, 0.3), (164.8, 0.12)):
        f = f0 * (1.0 + 0.01 * np.sin(2 * np.pi * r.uniform(0.3, 0.8) * t + r.uniform(0, 6)))
        drone += g * tone(f, amp, SR, harmonics=((1, 1.0), (2, 0.25), (3, 0.08)))
    mix_at(y, norm(drone), 0.35, 0.5)
    mix_at(y, whisper(dur, seed + 2, [(0.0, 0.0), (0.3, 1.0), (1.0, 0.0)], rate=6.5, shift=1.25), 0.4, 0.25)
    return y


def nyxar_shriek(seed: int) -> np.ndarray:
    """Der Schrei: eine hohe, geisterhafte Stimme mit einer verstimmten Schwester, darunter rau und kratzend – nicht schrill."""
    r = rng(seed)
    dur = r.uniform(1.4, 1.7)
    n = frames_len(dur)
    t = np.arange(n) / SR
    top = r.uniform(520.0, 600.0)
    f = curve(n, [(0.0, top * 0.72), (0.2, top), (0.55, top * 0.94), (1.0, top * 0.66)])
    f = f * (1.0 + 0.022 * np.sin(2 * np.pi * r.uniform(5.5, 7.0) * t))
    amp = curve(n, [(0.0, 0.0), (0.12, 1.0), (0.6, 0.85), (1.0, 0.0)])
    ghost = tone_voice(f, amp, seed, harmonics=((1, 1.0), (2, 0.35), (3, 0.15), (4, 0.06)), vowel="a", wet=0.6, breath=0.25,
                       chorus=0.35)
    sister = tone_voice(f * 1.059, amp * 0.55, seed + 1, harmonics=((1, 1.0), (2, 0.2)), vowel="o", wet=0.6, chorus=0.3)
    rasp = pulse_voice(f * 0.5, amp * 0.8, seed + 2, "a", size=0.75, jitter=0.1, shimmer=0.3, sub=0.3, breath=0.6, bright=3800.0)
    y = np.zeros(n + int(1.0 * SR))
    mix_at(y, ghost, 0.0, 0.8)
    mix_at(y, sister, 0.03, 0.4)
    mix_at(y, saturate(rasp, 2.2), 0.0, 0.45)
    mix_at(y, rumble(dur, seed + 3, 120.0, [(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)]), 0.0, 0.25)
    return filt(y, "low", 6000.0, SR, 2)


# =====================================================================================
# Tabelle
# =====================================================================================

# Name: (Erzeuger, Varianten, Raum (Nachhall s, Anteil) oder None, Lautheit in LUFS, Ordner)
SYNTH = {
    "kraken_bite": (kraken_bite, 3, (0.5, 0.1), -16.0, "sea"),
    "kraken_hurt": (kraken_hurt, 4, (0.9, 0.14), -15.5, "sea"),
    "kraken_ink": (kraken_ink, 3, (0.8, 0.14), -18.0, "sea"),
    "kraken_slam": (kraken_slam, 3, (1.0, 0.14), -14.0, "sea"),
    "kraken_wave": (kraken_wave, 3, (1.0, 0.12), -15.5, "sea"),
    "kraken_whirl": (kraken_whirl, 3, (1.0, 0.15), -17.0, "sea"),
    "reef_colossus_burrow": (reef_burrow, 3, (0.5, 0.08), -18.0, "sea"),
    "reef_colossus_erupt": (reef_erupt, 2, (0.9, 0.12), -14.5, "sea"),
    "reef_colossus_hurt": (reef_hurt, 4, (0.6, 0.1), -16.0, "sea"),
    "reef_colossus_lance": (reef_lance, 3, (0.9, 0.12), -15.0, "sea"),
    "reef_colossus_slam": (reef_slam, 3, (0.8, 0.12), -14.5, "sea"),
    "reef_colossus_step": (reef_step, 4, None, -20.0, "sea"),
    "reef_colossus_tide": (reef_tide, 2, (1.4, 0.2), -16.5, "sea"),
    "nyxar_breath": (nyxar_breath, 3, (1.4, 0.2), -15.5, "shadow"),
    "nyxar_shriek": (nyxar_shriek, 3, (1.8, 0.25), -15.0, "shadow"),
}

# Ereignis: (Klang, Reichweite in Blöcken)
EVENTS = {
    "entity.kraken.bite": ("kraken_bite", 40),
    "entity.kraken.hurt": ("kraken_hurt", 48),
    "entity.kraken.ink": ("kraken_ink", 32),
    "entity.kraken.slam": ("kraken_slam", 64),
    "entity.kraken.wave": ("kraken_wave", 64),
    "entity.kraken.whirl": ("kraken_whirl", 48),
    "entity.reef_colossus.burrow": ("reef_colossus_burrow", 32),
    "entity.reef_colossus.erupt": ("reef_colossus_erupt", 64),
    "entity.reef_colossus.hurt": ("reef_colossus_hurt", 32),
    "entity.reef_colossus.lance": ("reef_colossus_lance", 48),
    "entity.reef_colossus.slam": ("reef_colossus_slam", 48),
    "entity.reef_colossus.step": ("reef_colossus_step", 24),
    "entity.reef_colossus.tide": ("reef_colossus_tide", 64),
    "entity.nyxar.breath": ("nyxar_breath", 64),
    "entity.nyxar.shriek": ("nyxar_shriek", 96),
}


def files_of(name: str) -> list[str]:
    folder = SYNTH[name][4]
    return [f"kingdomsatwar:entity/{folder}/{name}{i + 1}" for i in range(SYNTH[name][1])]


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level, folder = SYNTH[name]
    out = SOUNDS / "entity" / folder
    out.mkdir(parents=True, exist_ok=True)
    for i in range(count):
        x = fn(9100 + i * 37 + zlib.crc32(name.encode()) % 991)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 1700 + i, stereo=False, damping=5500), space[1])
        x = filt(x, "high", 26.0, SR, 2)
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.06)
        x = fit_lufs(x, level)
        write_ogg(x.astype(np.float32), SR, out / f"{name}{i + 1}.ogg", quality=5.0)
        if preview is not None:
            from ambience import spectrogram
            spectrogram(x, SR, preview / f"{name}{i + 1}.png", f"{name}{i + 1}")
            from dsp import write_wav
            write_wav(x.astype(np.float32), SR, preview / f"{name}{i + 1}.wav")
    print(f"  {name}: {count} Varianten")
    return files_of(name)


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--preview", type=Path, help="Ordner für Spektrogramme")
    ap.add_argument("--only", nargs="*", help="nur diese Klänge neu erzeugen (die übrigen Dateien bleiben)")
    args = ap.parse_args()
    if args.preview:
        args.preview.mkdir(parents=True, exist_ok=True)
    rendered = {name: render(name, args.preview) if not args.only or name in args.only else files_of(name) for name in SYNTH}
    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8"))
    for key, (sound, reach) in EVENTS.items():
        old = data.get(key, {})
        data[key] = {"sounds": [{"name": f, "attenuation_distance": reach} for f in rendered[sound]],
                     "subtitle": old.get("subtitle", f"subtitles.kingdomsatwar.{key}")}
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"{len(EVENTS)} Klangereignisse geschrieben")


if __name__ == "__main__":
    main()
