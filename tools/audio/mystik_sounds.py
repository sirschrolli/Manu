#!/usr/bin/env python3
"""
Erzeugt die Klänge von MissMystik, der Feen-Zauberkünstlerin: ihr Summen (eine kleine Spieluhr-Melodie), ihr Kichern,
Schmerz und die letzte Verbeugung (eine Spieluhr, die langsam abläuft), Trommelwirbel und „Tada!“-Fanfare, Applaus,
wirbelnde und treffende Karten, auffliegende Lichttauben, Verschwinden und Wiederauftauchen, klingende Spiegel, den
wirbelnden Zylinder und seinen Sog, Schwerter aus Licht und ihren Stoß, das Aufflammen der Scheinwerfer, das „Zieh eine
Karte!“-Motiv und Feuerwerk – dazu das Kartendeck und den Verschwindetrick des Zylinders. Alles entsteht aus Rauschen und
Tönen (siehe ambience_dsp.py); es werden keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/mystik_sounds.py [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/mystik/<name><n>.ogg
    Einträge "entity.miss_mystik.*", "item.mystic_deck.throw" und "item.mystic_top_hat.vanish" in sounds.json
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
                          rng, room, saw, soft_clip, tone, trim_tail)
from dsp import write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/mystik"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100
# eine verspielte Tonleiter (a-Moll mit erhöhter Sexte: ein wenig geheimnisvoll)
SCALE = [440.0, 493.9, 523.3, 587.3, 659.3, 740.0, 830.6, 880.0, 987.8, 1046.5, 1174.7, 1318.5]


def mix_at(y: np.ndarray, x: np.ndarray, start: float, gain: float = 1.0) -> None:
    s = int(start * SR)
    m = max(0, min(len(x), len(y) - s))
    y[s:s + m] += x[:m] * gain


def noise(n: int, seed: int) -> np.ndarray:
    return rng(seed).standard_normal(n)


# =====================================================================================
# Bausteine
# =====================================================================================

def musicbox(f0: float, dur: float, seed: int) -> np.ndarray:
    """Ein Spieluhr-Ton: ein heller, fast harmonischer Anschlag mit metallischem Glanz."""
    return bell(f0, dur, SR, ratios=(1.0, 3.0, 5.01, 7.1), amps=(1.0, 0.28, 0.1, 0.04), decay=1.8, seed=seed)


def chime(f0: float, dur: float, seed: int, decay: float = 1.4) -> np.ndarray:
    return bell(f0, dur, SR, ratios=(1.0, 2.01, 3.02, 4.17, 5.43), amps=(1.0, 0.35, 0.18, 0.08, 0.04), decay=decay, seed=seed)


def glass(f0: float, dur: float, seed: int, decay: float = 0.6) -> np.ndarray:
    return bell(f0, dur, SR, ratios=(1.0, 1.47, 2.31, 3.17, 4.53, 6.1), amps=(1.0, 0.8, 0.6, 0.4, 0.25, 0.15), decay=decay, seed=seed)


def metal(f0: float, dur: float, seed: int, decay: float = 0.9) -> np.ndarray:
    """Ein klingendes Schwert: unharmonische, helle Teiltöne, lang schwingend."""
    return bell(f0, dur, SR, ratios=(1.0, 2.43, 3.87, 5.31, 7.02), amps=(1.0, 0.6, 0.4, 0.22, 0.1), decay=decay, seed=seed)


def shimmer(n: int, seed: int, density: float = 60.0, lo: float = 4000.0, hi: float = 12000.0) -> np.ndarray:
    r = rng(seed)
    y = np.zeros(n)
    for _ in range(int(density * n / SR)):
        at = int(r.uniform(0, max(1, n - 2000)))
        length = int(r.uniform(300, 1800))
        f = r.uniform(3000, 9000)
        t = np.arange(length) / SR
        y[at:at + length] += np.sin(2 * np.pi * f * t) * np.exp(-t * r.uniform(18, 40)) * r.uniform(0.2, 1.0)
    return filt(y, "band", [lo, hi], SR, 2)


def whoosh(n: int, seed: int, f0: float, f1: float, q: float = 1.0) -> np.ndarray:
    x = noise(n, seed)
    out = np.zeros(n)
    steps = 24
    for i in range(steps):
        a, b = i * n // steps, (i + 1) * n // steps
        f = f0 * (f1 / f0) ** (i / (steps - 1))
        seg = filt(x[max(0, a - 512):b], "band", [max(40.0, f / (1.0 + q)), min(18000.0, f * (1.0 + q))], SR, 2)
        out[a:b] = seg[-(b - a):]
    return out


def horn(n: int, f0: float, f1: float, seed: int) -> np.ndarray:
    t = np.arange(n) / SR
    f = glide(n, f0, f1, 0.5) * (1.0 + 0.007 * np.sin(2 * np.pi * 5.4 * t) * np.clip(t / 0.3, 0, 1))
    y = saw(f, np.ones(n), SR, partials=30, rolloff=1.05)
    y = formant_filter(y, SR, [(700, 200, 1.0), (1300, 260, 0.8), (2600, 400, 0.4)])
    return y * env_asr(n, SR, 0.03, 0.25)


def voice(n: int, f0: float, f1: float, vowel: str, seed: int) -> np.ndarray:
    """Eine helle Frauenstimme (eine Silbe): Sägezahn mit Vibrato durch Vokalformanten, ein wenig Hauch."""
    t = np.arange(n) / SR
    f = glide(n, f0, f1, 0.6) * (1.0 + 0.012 * np.sin(2 * np.pi * 6.0 * t))
    y = saw(f, np.ones(n), SR, partials=24, rolloff=1.2)
    formants = {"i": [(310, 80, 1.0), (2500, 200, 0.6), (3300, 300, 0.3)],
                "e": [(450, 90, 1.0), (2200, 200, 0.55), (3000, 300, 0.25)],
                "a": [(900, 130, 1.0), (1300, 160, 0.6), (3000, 300, 0.2)],
                "o": [(500, 100, 1.0), (900, 140, 0.45), (2800, 300, 0.12)]}[vowel]
    y = formant_filter(y, SR, formants)
    breath = filt(noise(n, seed), "band", [2000, 7000], SR, 2) * 0.05
    return (y + breath) * env_asr(n, SR, 0.01, 0.04)


def snare(seed: int, gain: float = 1.0) -> np.ndarray:
    n = int(0.12 * SR)
    body = tone(glide(n, 240, 180, 0.5), env_ad(n, SR, 0.001, 0.04), SR, harmonics=((1, 1.0), (2, 0.3)))
    rattle = filt(noise(n, seed), "band", [1500, 8000], SR, 2) * env_ad(n, SR, 0.001, 0.06)
    return (body * 0.5 + rattle) * gain


def clap(seed: int) -> np.ndarray:
    r = rng(seed)
    n = int(0.06 * SR)
    x = filt(noise(n, seed), "band", [r.uniform(700, 1100), r.uniform(2400, 3600)], SR, 2)
    return x * env_ad(n, SR, 0.0005, r.uniform(0.008, 0.016))


def thud(n: int, f0: float, f1: float, decay: float) -> np.ndarray:
    return tone(glide(n, f0, f1, 0.4), env_ad(n, SR, 0.002, decay), SR, harmonics=((1, 1.0), (2, 0.25), (3, 0.06)))


def impact(n: int, seed: int, lo: float, hi: float, decay: float) -> np.ndarray:
    return filt(noise(n, seed), "band", [lo, hi], SR, 2) * env_ad(n, SR, 0.0008, decay)


def flutter(n: int, seed: int, rate: float = 13.0) -> np.ndarray:
    """Flügelschlag: Rauschen im Takt der Flügel."""
    t = np.arange(n) / SR
    r = rng(seed)
    am = np.clip(np.sin(2 * np.pi * (rate + r.uniform(-2, 2)) * t + r.uniform(0, 6)), 0, None) ** 3
    return filt(noise(n, seed), "band", [400, 3500], SR, 2) * am


# =====================================================================================
# Klänge
# =====================================================================================

def ambient(seed: int) -> np.ndarray:
    """Sie summt: eine kleine Spieluhr-Melodie aus fünf, sechs Tönen, ein Hauch Glitzern."""
    r = rng(seed)
    n = int(2.6 * SR)
    y = np.zeros(n)
    idx = int(r.integers(2, 6))
    t = 0.0
    for k in range(int(r.integers(5, 7))):
        idx = int(np.clip(idx + r.choice([-2, -1, 1, 2, 3]), 0, len(SCALE) - 1))
        mix_at(y, musicbox(SCALE[idx], 1.4, seed + k), t, 0.5 if k % 2 == 0 else 0.4)
        t += r.choice([0.22, 0.22, 0.33, 0.44])
    mix_at(y, shimmer(n, seed + 9, 25) * env_asr(n, SR, 0.3, 1.0), 0.0, 0.2)
    return y


def laugh(seed: int) -> np.ndarray:
    """Ihr Kichern: vier, fünf helle Silben, absteigend, mit Hauch – und ein Glitzern."""
    r = rng(seed)
    n = int(1.6 * SR)
    y = np.zeros(n)
    f = r.uniform(620, 700)
    for k in range(int(r.integers(4, 6))):
        m = int(r.uniform(0.07, 0.1) * SR)
        mix_at(y, voice(m, f, f * 0.93, "i" if k % 2 else "e", seed + k), 0.05 + k * r.uniform(0.11, 0.14), 0.7)
        f *= r.uniform(0.9, 0.95)
    mix_at(y, shimmer(n, seed + 7, 40) * env_ad(n, SR, 0.05, 0.9), 0.0, 0.25)
    return y


def hurt(seed: int) -> np.ndarray:
    """Schmerz: ein kurzer, heller Aufschrei („ah!“) und ein schiefer Glockenton."""
    r = rng(seed)
    n = int(0.8 * SR)
    y = np.zeros(n)
    m = int(0.22 * SR)
    f = r.uniform(560, 640)
    mix_at(y, voice(m, f * 1.15, f * 0.85, "a", seed), 0.0, 0.8)
    mix_at(y, glass(r.uniform(1300, 1600), 0.6, seed + 1, 0.3), 0.0, 0.18)
    return y


def death(seed: int) -> np.ndarray:
    """Die letzte Verbeugung: eine Spieluhr, die immer langsamer wird und tiefer klingt, ein sanfter Schlussakkord."""
    n = int(4.4 * SR)
    y = np.zeros(n)
    melody = [9, 7, 8, 6, 4, 5, 2, 3, 0]
    t = 0.0
    for k, idx in enumerate(melody):
        slow = 1.0 - k * 0.025
        mix_at(y, musicbox(SCALE[idx] * slow, 1.6, seed + k), t, 0.5)
        t += 0.2 + k * 0.05
    for k, f in enumerate((220.0, 329.6, 440.0, 554.4)):
        mix_at(y, chime(f, 2.4, seed + 20 + k, 1.6), t + 0.1, 0.18)
    mix_at(y, shimmer(n, seed + 30, 50) * curve(n, [(0, 0.3), (0.7, 1.0), (1, 0.0)]), 0.0, 0.3)
    return y


def drumroll(seed: int) -> np.ndarray:
    """Trommelwirbel: ein Snare-Wirbel, der schneller und lauter wird, am Ende ein Beckenschlag."""
    r = rng(seed)
    n = int(1.8 * SR)
    y = np.zeros(n)
    t = 0.0
    k = 0
    while t < 1.55:
        rate = 14.0 + t * 10.0
        gain = 0.25 + 0.75 * (t / 1.55) ** 1.5
        mix_at(y, snare(seed + k, gain * r.uniform(0.85, 1.0)), t, 0.7)
        t += 1.0 / rate * r.uniform(0.92, 1.08)
        k += 1
    m = int(0.3 * SR)
    mix_at(y, impact(m, seed + 99, 3000, 12000, 0.12), 1.55, 0.4)
    return y


def tada(seed: int) -> np.ndarray:
    """„Tada!“: ein kurzer Vorschlag, dann ein strahlender Bläserakkord, ein Beckenschlag und ein Funkenregen."""
    r = rng(seed)
    n = int(2.4 * SR)
    y = np.zeros(n)
    base = r.uniform(392, 415)
    for f in (base, base * 1.26, base * 1.5):
        mix_at(y, horn(int(0.14 * SR), f, f, seed), 0.0, 0.22)
    for k, f in enumerate((base, base * 1.26, base * 1.5, base * 2.0)):
        m = int(1.4 * SR)
        mix_at(y, horn(m, f, f, seed + k) * curve(m, [(0, 0.0), (0.03, 1.0), (0.6, 0.7), (1, 0.0)]), 0.2, 0.25)
    m = int(1.6 * SR)
    mix_at(y, impact(m, seed + 7, 3000, 14000, 0.6) * 0.6, 0.2, 0.5)
    mix_at(y, shimmer(n, seed + 9, 120) * curve(n, [(0, 0.0), (0.12, 1.0), (1, 0.0)]), 0.0, 0.4)
    return y


def applause(seed: int) -> np.ndarray:
    """Applaus: viele Hände, die einsetzen, anschwellen und abebben."""
    r = rng(seed)
    n = int(3.6 * SR)
    y = np.zeros(n)
    k = 0
    for i in range(1400):
        t = r.uniform(0.0, 3.4)
        density = np.interp(t, [0.0, 0.5, 2.2, 3.4], [0.2, 1.0, 0.9, 0.0])
        if r.uniform() > density:
            continue
        mix_at(y, clap(seed + k), t, r.uniform(0.15, 0.4))
        k += 1
    mix_at(y, shimmer(n, seed + 3, 20), 0.0, 0.1)
    return y


def card_throw(seed: int) -> np.ndarray:
    """Karten wirbeln: drei, vier kurze, helle Luftstöße mit flatterndem Papier."""
    r = rng(seed)
    n = int(0.7 * SR)
    y = np.zeros(n)
    for k in range(int(r.integers(3, 5))):
        m = int(0.22 * SR)
        w = whoosh(m, seed + k, r.uniform(2500, 3500), r.uniform(5000, 7000), 0.7) * curve(m, [(0, 0.0), (0.2, 1.0), (1, 0.0)])
        mix_at(y, w + flutter(m, seed + 10 + k, 40) * 0.3, k * r.uniform(0.04, 0.07), 0.6)
    return y


def card_hit(seed: int) -> np.ndarray:
    """Eine Karte trifft: ein Papierklatsch und ein kleines Klingen."""
    r = rng(seed)
    n = int(0.5 * SR)
    y = np.zeros(n)
    mix_at(y, impact(int(0.05 * SR), seed, 1500, 8000, 0.01), 0.0, 1.0)
    mix_at(y, glass(r.uniform(2400, 3000), 0.4, seed + 1, 0.25), 0.0, 0.2)
    return y


def doves(seed: int) -> np.ndarray:
    """Tauben flattern auf: ein Fingerschnippen, viele Flügel, ein Gurren, ein Glitzern."""
    r = rng(seed)
    n = int(1.8 * SR)
    y = np.zeros(n)
    mix_at(y, impact(int(0.03 * SR), seed, 2000, 9000, 0.004), 0.0, 0.9)
    for k in range(6):
        m = int(r.uniform(0.8, 1.2) * SR)
        mix_at(y, flutter(m, seed + k, r.uniform(10, 15)) * curve(m, [(0, 0.0), (0.1, 1.0), (1, 0.0)]), 0.05 + k * 0.05, 0.4)
    m = int(0.5 * SR)
    coo = tone(glide(m, 420, 330, 0.5), curve(m, [(0, 0.0), (0.2, 1.0), (0.6, 0.6), (1, 0.0)]), SR, harmonics=((1, 1.0), (2, 0.15)))
    mix_at(y, filt(coo, "low", 1200, SR, 2), 0.5, 0.15)
    mix_at(y, shimmer(n, seed + 20, 50) * env_ad(n, SR, 0.05, 1.2), 0.0, 0.3)
    return y


def vanish(seed: int) -> np.ndarray:
    """Verschwinden: ein dumpfes „Puff“, ein absinkender Pfeifton, Funken."""
    n = int(1.3 * SR)
    y = np.zeros(n)
    m = int(0.5 * SR)
    puff = filt(noise(m, seed), "low", 900, SR, 2) * env_ad(m, SR, 0.004, 0.18)
    mix_at(y, puff, 0.0, 1.0)
    mix_at(y, thud(int(0.3 * SR), 120, 60, 0.08), 0.0, 0.5)
    w = int(0.6 * SR)
    whistle = tone(glide(w, 2400, 700, 0.6), curve(w, [(0, 0.0), (0.1, 1.0), (1, 0.0)]), SR, harmonics=((1, 1.0),))
    mix_at(y, whistle, 0.05, 0.12)
    mix_at(y, shimmer(n, seed + 5, 70) * env_ad(n, SR, 0.01, 0.7), 0.0, 0.35)
    return y


def reappear(seed: int) -> np.ndarray:
    """Wiederauftauchen: ein anschwellendes Rauschen (rückwärts), dann ein helles „Pling“ und Glitzern."""
    n = int(1.4 * SR)
    y = np.zeros(n)
    m = int(0.5 * SR)
    swell = (filt(noise(m, seed), "band", [800, 6000], SR, 2) * env_ad(m, SR, 0.004, 0.2))[::-1]
    mix_at(y, swell, 0.0, 0.7)
    for k, f in enumerate((SCALE[7], SCALE[11])):
        mix_at(y, chime(f, 1.2, seed + k, 1.4), 0.48 + k * 0.06, 0.35)
    mix_at(y, shimmer(n, seed + 5, 90) * curve(n, [(0, 0.0), (0.35, 1.0), (1, 0.0)]), 0.0, 0.35)
    return y


def mirrors(seed: int) -> np.ndarray:
    """Spiegel klingen: eine Kaskade gläserner Töne, ein schimmernder Strich."""
    r = rng(seed)
    n = int(2.0 * SR)
    y = np.zeros(n)
    for k in range(7):
        mix_at(y, glass(SCALE[int(r.integers(4, 12))] * 2.0, 1.2, seed + k, 0.8), k * 0.09, 0.22)
    w = whoosh(int(1.2 * SR), seed + 20, 3000, 9000, 0.5) * curve(int(1.2 * SR), [(0, 0.0), (0.5, 1.0), (1, 0.0)])
    mix_at(y, w, 0.1, 0.25)
    return y


def hat_throw(seed: int) -> np.ndarray:
    """Der Zylinder wirbelt: ein drehender, pulsierender Luftzug."""
    n = int(1.0 * SR)
    t = np.arange(n) / SR
    w = whoosh(n, seed, 900, 2600, 0.7) * (0.5 + 0.5 * np.sin(2 * np.pi * 9.0 * t)) * curve(n, [(0, 0.0), (0.15, 1.0), (1, 0.0)])
    return w


def vortex(seed: int) -> np.ndarray:
    """Der Sog: ein kreisender Wind, der immer höher und schneller wird, dazu einsaugendes Glitzern."""
    n = int(2.8 * SR)
    t = np.arange(n) / SR
    y = whoosh(n, seed, 200, 1800, 0.8) * (0.6 + 0.4 * np.sin(2 * np.pi * (2.0 + t * 3.0) * t))
    y *= curve(n, [(0, 0.0), (0.2, 0.7), (0.85, 1.0), (1, 0.0)])
    y += shimmer(n, seed + 3, 60) * curve(n, [(0, 0.0), (0.7, 1.0), (1, 0.0)]) * 0.4
    return y


def swords(seed: int) -> np.ndarray:
    """Schwerter erscheinen: acht helle, metallische Klänge kurz nacheinander und ein schimmernder Hauch."""
    r = rng(seed)
    n = int(2.0 * SR)
    y = np.zeros(n)
    for k in range(8):
        mix_at(y, metal(r.uniform(1400, 2200), 1.2, seed + k, 1.0), k * 0.05, 0.2)
    mix_at(y, whoosh(int(0.8 * SR), seed + 30, 4000, 8000, 0.5) * env_ad(int(0.8 * SR), SR, 0.1, 0.5), 0.0, 0.25)
    return y


def stab(seed: int) -> np.ndarray:
    """Die Schwerter stoßen zu: harte metallische Schläge, fast gleichzeitig."""
    r = rng(seed)
    n = int(1.2 * SR)
    y = np.zeros(n)
    for k in range(5):
        mix_at(y, impact(int(0.06 * SR), seed + k, 1500, 9000, 0.012), k * 0.018, 0.6)
        mix_at(y, metal(r.uniform(900, 1500), 0.8, seed + 10 + k, 0.7), k * 0.018, 0.12)
    mix_at(y, thud(int(0.3 * SR), 150, 70, 0.07), 0.0, 0.4)
    return y


def spotlight(seed: int) -> np.ndarray:
    """Die Scheinwerfer flammen auf: ein schweres „Klack“, ein Brummen, ein aufsteigendes Surren."""
    n = int(2.0 * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    mix_at(y, impact(int(0.08 * SR), seed, 400, 4000, 0.02), 0.0, 1.0)
    mix_at(y, thud(int(0.3 * SR), 90, 60, 0.1), 0.0, 0.5)
    buzz = tone(np.full(n, 100.0), curve(n, [(0, 0.0), (0.05, 1.0), (0.8, 0.6), (1, 0.0)]), SR,
                harmonics=((1, 1.0), (2, 0.6), (3, 0.4), (4, 0.2)))
    mix_at(y, buzz, 0.0, 0.12)
    whine = tone(glide(n, 900, 2400, 0.5), curve(n, [(0, 0.0), (0.2, 1.0), (1, 0.0)]), SR, harmonics=((1, 1.0),))
    mix_at(y, whine * (0.7 + 0.3 * np.sin(2 * np.pi * 7 * t)), 0.0, 0.05)
    return y


def card_trick(seed: int) -> np.ndarray:
    """„Zieh eine Karte!“: ein geheimnisvolles Motiv – drei tiefe Bläsertöne, gezupfte Karten, ein Tremolo."""
    n = int(2.2 * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    for k, f in enumerate((220.0, 207.7, 261.6)):
        m = int((0.25 if k < 2 else 1.1) * SR)
        mix_at(y, horn(m, f, f, seed + k), k * 0.28, 0.35)
    for k in range(6):
        mix_at(y, impact(int(0.02 * SR), seed + 10 + k, 2000, 6000, 0.004), 0.05 + k * 0.035, 0.35)
    trem = chime(SCALE[4] * 2, 1.6, seed + 20, 1.2) * (0.6 + 0.4 * np.sin(2 * np.pi * 9 * t[:int(1.6 * SR)]))
    mix_at(y, trem, 0.6, 0.15)
    return y


def firework(seed: int) -> np.ndarray:
    """Feuerwerk: eine pfeifend aufsteigende Rakete, ein Knall, ein Knistern."""
    r = rng(seed)
    n = int(2.6 * SR)
    y = np.zeros(n)
    m = int(0.7 * SR)
    mix_at(y, tone(glide(m, 900, 2600, 0.6), curve(m, [(0, 0.0), (0.2, 1.0), (1, 0.6)]), SR, harmonics=((1, 1.0),)), 0.0, 0.1)
    mix_at(y, whoosh(m, seed, 1500, 4000, 0.5) * curve(m, [(0, 0.0), (0.3, 1.0), (1, 0.4)]), 0.0, 0.3)
    mix_at(y, thud(int(0.6 * SR), 90, 40, 0.18), 0.7, 0.9)
    mix_at(y, impact(int(0.4 * SR), seed + 1, 200, 3000, 0.12), 0.7, 0.8)
    for k in range(70):
        mix_at(y, impact(int(0.02 * SR), seed + 10 + k, 3000, 10000, 0.004), 0.8 + r.uniform(0, 1.4), r.uniform(0.05, 0.25))
    return y


def deck_throw(seed: int) -> np.ndarray:
    """Das Kartendeck: ein Fächer aus Karten zischt davon."""
    n = int(0.5 * SR)
    y = np.zeros(n)
    for k in range(3):
        m = int(0.2 * SR)
        mix_at(y, whoosh(m, seed + k, 3000, 6500, 0.6) * curve(m, [(0, 0.0), (0.2, 1.0), (1, 0.0)]), k * 0.035, 0.5)
    mix_at(y, shimmer(n, seed + 9, 30), 0.0, 0.15)
    return y


def hat_vanish(seed: int) -> np.ndarray:
    """Der Verschwindetrick des Zylinders: ein „Puff“ und ein kleines Glockenspiel."""
    n = int(1.0 * SR)
    y = np.zeros(n)
    m = int(0.4 * SR)
    mix_at(y, filt(noise(m, seed), "low", 1000, SR, 2) * env_ad(m, SR, 0.004, 0.15), 0.0, 0.9)
    for k, f in enumerate((SCALE[11], SCALE[9], SCALE[7])):
        mix_at(y, musicbox(f, 0.8, seed + k), 0.08 + k * 0.07, 0.25)
    return y


# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil) oder None, Ziel-Pegel dBFS)
SYNTH = {
    "ambient": (ambient, 3, (1.8, 0.35), -22.0),
    "laugh": (laugh, 3, (1.4, 0.3), -17.0),
    "hurt": (hurt, 3, (1.0, 0.2), -17.0),
    "death": (death, 1, (2.4, 0.4), -16.0),
    "drumroll": (drumroll, 1, (1.4, 0.25), -15.0),
    "tada": (tada, 2, (2.2, 0.35), -13.0),
    "applause": (applause, 1, (1.6, 0.3), -17.0),
    "card_throw": (card_throw, 3, (0.8, 0.2), -18.0),
    "card_hit": (card_hit, 3, (0.6, 0.15), -19.0),
    "doves": (doves, 2, (1.4, 0.3), -17.0),
    "vanish": (vanish, 2, (1.6, 0.3), -15.0),
    "reappear": (reappear, 2, (1.6, 0.3), -15.0),
    "mirrors": (mirrors, 2, (2.0, 0.4), -17.0),
    "hat_throw": (hat_throw, 2, (1.0, 0.2), -17.0),
    "vortex": (vortex, 1, (1.6, 0.3), -15.0),
    "swords": (swords, 2, (1.8, 0.35), -16.0),
    "stab": (stab, 2, (1.2, 0.25), -14.0),
    "spotlight": (spotlight, 2, (1.4, 0.25), -15.0),
    "card_trick": (card_trick, 2, (2.0, 0.35), -16.0),
    "firework": (firework, 3, (2.4, 0.35), -14.0),
    "deck_throw": (deck_throw, 3, (0.6, 0.15), -19.0),
    "hat_vanish": (hat_vanish, 2, (1.0, 0.2), -17.0),
}

# Ereignis: (Klänge, Reichweite)
EVENTS = {f"entity.miss_mystik.{name}": ([name], reach) for name, reach in (
    ("ambient", 32), ("laugh", 48), ("hurt", 32), ("death", 96), ("drumroll", 64), ("tada", 96), ("applause", 64),
    ("card_throw", 32), ("card_hit", 24), ("doves", 48), ("vanish", 48), ("reappear", 48), ("mirrors", 48), ("hat_throw", 32),
    ("vortex", 48), ("swords", 48), ("stab", 48), ("spotlight", 64), ("card_trick", 64), ("firework", 96))}
EVENTS["item.mystic_deck.throw"] = (["deck_throw"], 16)
EVENTS["item.mystic_top_hat.vanish"] = (["hat_vanish"], 16)


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    files = []
    for i in range(count):
        x = fn(9200 + i * 47 + zlib.crc32(name.encode()) % 991)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 1900 + i, stereo=False, damping=7000), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.06)
        x = soft_clip(fit_level(x, level, -2.0))
        stem = {"deck_throw": "mystic_deck_throw", "hat_vanish": "mystic_hat_vanish"}.get(name, f"miss_mystik_{name}")
        path = OUT / f"{stem}{i + 1}.ogg"
        write_ogg(x.astype(np.float32), SR, path, quality=5.0)
        files.append(f"kingdomsatwar:mystik/{stem}{i + 1}")
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
