#!/usr/bin/env python3
"""
Erzeugt die Stimmen der Wesen der Leere: Gurgeln und Ansaugen des Leerenschlunds, das Erwachen und Aufladen des
Splitterwächters, das Knistern, Flackern und Springen des Zerrlings, den Gesang des Sternenrochens und Stimme, Schrei,
Hiebe, Schwerkraftbruch, Sternenstrahl, Risse der Leere (Klauen schrammen über Stein, die Leere knistert und bricht
hervor) und Leerenbeben des Zerbrochenen Königs. Alles entsteht
aus Rauschen und Tönen (siehe ambience_dsp.py). Wo Vanilla-Klänge passen (Bisse, Schritte, splitternder Amethyst),
verweist sounds.json nur auf sie – es werden keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/void_creatures.py [--only name ...] [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/void/<name><n>.ogg
    Einträge "entity.<wesen>.<klang>" in assets/kingdomsatwar/sounds.json
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, bell, curve, env_ad, env_asr, filt, fit_level, formant_filter, rng, room, saw,  # noqa: E402
                          soft_clip, tone, trim_tail, fade_edges)
from dsp import write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/void"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100


def concat(*parts) -> np.ndarray:
    out = []
    for p in parts:
        out.append(np.zeros(int(p * SR)) if isinstance(p, (int, float)) else np.asarray(p, dtype=float))
    return np.concatenate(out)


def mix_at(y: np.ndarray, x: np.ndarray, start: float, gain: float = 1.0) -> None:
    s = int(start * SR)
    m = max(0, min(len(x), len(y) - s))
    y[s:s + m] += x[:m] * gain


# =====================================================================================
# Leerenschlund
# =====================================================================================

def maw_ambient(seed: int) -> np.ndarray:
    """Tiefes, nasses Gurgeln: ein schwankender Ton durch wandernde Formanten, dazu blubbernde Klicks."""
    r = rng(seed)
    n = int(r.uniform(1.4, 2.0) * SR)
    t = np.arange(n) / SR
    f0 = r.uniform(62, 90)
    f = curve(n, [(0, f0 * 0.9), (0.3, f0 * r.uniform(1.1, 1.3)), (0.7, f0), (1, f0 * 0.8)])
    f = f * (1 + 0.05 * np.sin(2 * np.pi * r.uniform(5, 9) * t))
    y = saw(f, env_asr(n, SR, 0.15, 0.4), SR, partials=30, rolloff=1.3)
    lo, hi = r.uniform(250, 380), r.uniform(600, 900)
    y = formant_filter(y, SR, [(lo, 90, 1.0), (hi, 160, 0.6), (2200, 400, 0.15)])
    for i in range(int(r.integers(4, 9))):
        m = int(0.03 * SR)
        click = np.sin(2 * np.pi * np.cumsum(np.linspace(r.uniform(300, 600), r.uniform(900, 1500), m)) / SR) * env_ad(m, SR, 0.002)
        mix_at(y, click, r.uniform(0.1, n / SR - 0.1), r.uniform(0.15, 0.35))
    return filt(y, "low", 3000, SR, 2)


def maw_inhale(seed: int) -> np.ndarray:
    """Ansaugen: ein anschwellendes, saugendes Rauschen, das von tief nach hoch fährt, darunter ein steigendes Dröhnen."""
    r = rng(seed)
    n = int(2.6 * SR)
    t = np.arange(n) / SR
    noise = r.standard_normal(n)
    y = np.zeros(n)
    # gleitendes Bandpass-Rauschen in Blöcken (Mitte steigt von 220 auf 1800 Hz)
    block = 2048
    for s in range(0, n, block // 2):
        e = min(n, s + block)
        k = s / n
        center = 220 * (1800 / 220) ** k
        seg = filt(noise[s:e], "band", [center * 0.6, center * 1.6], SR, 2) * np.hanning(e - s)
        y[s:e] += seg
    y *= curve(n, [(0, 0.05), (0.7, 0.9), (0.92, 1.0), (1, 0.0)])
    rumble = saw(curve(n, [(0, 45), (1, 95)]), curve(n, [(0, 0.0), (0.2, 0.4), (0.9, 0.7), (1, 0.0)]), SR, partials=10, rolloff=1.5)
    y += filt(rumble, "low", 400, SR, 2) * 0.8
    y += 0.25 * np.sin(2 * np.pi * np.cumsum(curve(n, [(0, 300), (1, 700)])) / SR) * curve(n, [(0, 0), (0.5, 0.2), (0.95, 0.5), (1, 0)])
    return y


# =====================================================================================
# Splitterwächter
# =====================================================================================

def warden_ambient(seed: int) -> np.ndarray:
    """Ein tiefes, kristallenes Summen und ein Knirschen im Marmor."""
    r = rng(seed)
    n = int(r.uniform(1.8, 2.4) * SR)
    t = np.arange(n) / SR
    f0 = r.uniform(55, 70)
    y = tone(np.full(n, f0), env_asr(n, SR, 0.4, 0.6), SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.3), (5, 0.12), (8, 0.05)))
    y *= 1 + 0.3 * np.sin(2 * np.pi * r.uniform(2.0, 3.5) * t)
    for i in range(2):
        b = bell(r.uniform(900, 1600), 1.6, SR, ratios=(1.0, 2.0, 2.99, 4.13), amps=(1.0, 0.3, 0.15, 0.08), decay=1.0, seed=seed + i)
        mix_at(y, b, r.uniform(0.2, n / SR - 0.8), 0.18)
    grind = filt(r.standard_normal(n), "band", [180, 900], SR, 2) * env_asr(n, SR, 0.3, 0.5) * (0.5 + 0.5 * np.sin(2 * np.pi * 11 * t) > 0.6)
    return y + grind * 0.12


def warden_awaken(seed: int) -> np.ndarray:
    """Erwachen: Stein knirscht, ein Akkord aus Kristall schwillt an und flammt auf."""
    r = rng(seed)
    n = int(3.0 * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    for f, a in ((110.0, 0.6), (164.81, 0.45), (220.0, 0.4), (329.63, 0.3), (440.0, 0.22), (659.26, 0.12)):
        fr = curve(n, [(0, f * 0.94), (0.4, f), (1, f)])
        y += tone(fr, curve(n, [(0, 0), (0.37, a), (0.47, a * 1.1), (0.75, a * 0.3), (1, 0)]), SR, harmonics=((1, 1.0), (2, 0.2), (3, 0.08)))
    grind = filt(r.standard_normal(n), "band", [120, 700], SR, 2) * curve(n, [(0, 0.4), (0.3, 0.8), (0.4, 0.0), (1, 0)])
    y += grind * 0.5
    for i in range(5):
        b = bell(float(r.choice([1318.5, 1760.0, 1975.5, 2637.0])), 1.2, SR, decay=0.8, seed=seed + 10 + i)
        mix_at(y, b, 0.9 + i * 0.07, 0.25)
    return y


def warden_charge(seed: int) -> np.ndarray:
    """Aufladen: ein gläsernes Sirren, das steigt und anschwillt."""
    r = rng(seed)
    n = int(1.0 * SR)
    t = np.arange(n) / SR
    f = curve(n, [(0, 380), (1, 1150)]) * (1 + 0.01 * np.sin(2 * np.pi * 18 * t))
    y = tone(f, curve(n, [(0, 0.0), (0.8, 0.8), (1, 0.0)]), SR, harmonics=((1, 1.0), (2.01, 0.4), (3.02, 0.2), (5.4, 0.1)))
    y += 0.5 * tone(f * 1.5, curve(n, [(0, 0.0), (0.85, 0.5), (1, 0.0)]), SR)
    shimmer = filt(r.standard_normal(n), "band", [3000, 8000], SR, 2) * curve(n, [(0, 0), (0.9, 0.15), (1, 0)])
    return y + shimmer


# =====================================================================================
# Zerrling
# =====================================================================================

def decimate(y: np.ndarray, hold: int, levels: float) -> np.ndarray:
    z = np.repeat(y[::hold], hold)[:len(y)]
    return np.round(z * levels) / levels


def stutter(r, src: np.ndarray, n: int, grain: float, keep: float = 0.7) -> np.ndarray:
    """Zerhackt eine Quelle in kleine Körner und wiederholt manche – wie eine springende Aufnahme."""
    y = np.zeros(n)
    g = int(grain * SR)
    pos = 0
    read = 0
    while pos < n - g:
        if r.random() < keep:
            seg = src[read:read + g]
            if len(seg) < g:
                read = 0
                seg = src[:g]
            y[pos:pos + g] += seg * env_asr(g, SR, 0.002, 0.003)
        if r.random() > 0.35:
            read += g
        pos += g
    return y


def distortling_ambient(seed: int) -> np.ndarray:
    """Knistern und ein zerhacktes, fremdes Flüstern."""
    r = rng(seed)
    n = int(r.uniform(1.2, 1.8) * SR)
    t = np.arange(n) / SR
    src = filt(r.standard_normal(n), "band", [900, 5000], SR, 2) * (0.5 + 0.5 * np.sin(2 * np.pi * r.uniform(3, 6) * t))
    voice = saw(curve(n, [(0, r.uniform(140, 220)), (0.5, r.uniform(250, 380)), (1, r.uniform(120, 180))]), np.ones(n), SR, 20, 1.2)
    voice = formant_filter(voice, SR, [(r.uniform(400, 700), 120, 1.0), (r.uniform(1100, 1800), 200, 0.5)])
    y = stutter(r, src * 0.6 + voice * 0.5, n, r.uniform(0.025, 0.05))
    y = decimate(y, int(r.integers(3, 6)), 6.0)
    return filt(y, "low", 7000, SR, 2) * env_asr(n, SR, 0.02, 0.1)


def distortling_hurt(seed: int) -> np.ndarray:
    """Getroffen: ein kurzer, greller Bildfehler mit kreischendem Ton."""
    r = rng(seed)
    n = int(r.uniform(0.35, 0.5) * SR)
    f = curve(n, [(0, r.uniform(900, 1300)), (1, r.uniform(300, 500))])
    y = np.sign(np.sin(2 * np.pi * np.cumsum(f) / SR)) * 0.5 + filt(r.standard_normal(n), "high", 2000, SR, 2) * 0.4
    y = stutter(r, y, n, 0.018, keep=0.8)
    y = decimate(y, int(r.integers(2, 5)), 5.0)
    return filt(y, "low", 8000, SR, 2) * env_ad(n, SR, 0.005)


def distortling_death(seed: int) -> np.ndarray:
    """Zerfall: der Ton sinkt, zerbröselt in immer gröbere Körner und verlischt."""
    r = rng(seed)
    n = int(1.6 * SR)
    f = curve(n, [(0, 700), (0.4, 300), (1, 60)])
    y = np.sign(np.sin(2 * np.pi * np.cumsum(f) / SR)) * 0.4 + filt(r.standard_normal(n), "band", [500, 4000], SR, 2) * 0.5
    out = np.zeros(n)
    for k, (a, b) in enumerate(((0.0, 0.3), (0.3, 0.6), (0.6, 1.0))):
        s, e = int(a * n), int(b * n)
        seg = stutter(r, y[s:e], e - s, 0.02 + k * 0.03, keep=0.8 - k * 0.2)
        out[s:e] = decimate(seg, 2 + k * 3, 6.0 - k * 2)
    return out * curve(n, [(0, 1.0), (0.7, 0.6), (1, 0.0)])


def distortling_blink(seed: int) -> np.ndarray:
    """Sprung: ein digitales „Bzzt“ mit einem schnellen Pfeifen nach oben."""
    r = rng(seed)
    n = int(0.45 * SR)
    f = curve(n, [(0, r.uniform(200, 300)), (0.6, r.uniform(1800, 2600)), (1, r.uniform(3000, 4000))])
    sweep = np.sin(2 * np.pi * np.cumsum(f) / SR) * curve(n, [(0, 0.2), (0.5, 0.8), (1, 0)])
    buzz = np.sign(np.sin(2 * np.pi * r.uniform(90, 140) * np.arange(n) / SR)) * curve(n, [(0, 0.7), (0.4, 0.3), (1, 0)])
    crackle = (r.random(n) < 0.03) * r.uniform(-1, 1, n) * curve(n, [(0, 1.0), (0.6, 0.6), (1, 0)])
    y = sweep * 0.6 + buzz * 0.4 + crackle * 0.5
    return filt(decimate(y, 2, 8.0), "low", 9000, SR, 2) * env_asr(n, SR, 0.004, 0.06)


def distortling_attack(seed: int) -> np.ndarray:
    """Krallenhieb: ein zischender Streich, der zwischendurch abreißt."""
    r = rng(seed)
    n = int(0.3 * SR)
    y = filt(r.standard_normal(n), "band", [curve(n, [(0, 800), (1, 3000)]).mean() * 0.5, 7000], SR, 2)
    y *= curve(n, [(0, 0), (0.25, 1.0), (1, 0)])
    y = stutter(r, y, n, 0.012, keep=0.85)
    return decimate(y, 2, 8.0)


# =====================================================================================
# Sternenrochen
# =====================================================================================

def ray_call(seed: int, low: float, high: float, notes=(2, 3), dur=(0.6, 1.2)) -> np.ndarray:
    r = rng(seed)
    parts = []
    for i in range(int(r.integers(*notes))):
        n = int(r.uniform(*dur) * SR)
        t = np.arange(n) / SR
        f0 = r.uniform(low, high)
        f = curve(n, [(0, f0), (0.4, f0 * r.uniform(1.15, 1.45)), (1, f0 * r.uniform(0.8, 1.05))])
        f = f * (1 + 0.01 * np.sin(2 * np.pi * r.uniform(4.5, 6) * t))
        parts.append(tone(f, env_asr(n, SR, 0.15, 0.35), SR, harmonics=((1, 1.0), (2, 0.3), (3, 0.12), (4, 0.05))))
        parts.append(r.uniform(0.08, 0.25))
    y = concat(*parts)
    return formant_filter(y, SR, [(700, 350, 1.0), (1600, 500, 0.3)])


def ray_ambient(seed: int) -> np.ndarray:
    return ray_call(seed, 300, 460)


def ray_hurt(seed: int) -> np.ndarray:
    """Ein kurzer, gequälter Ruf, der abbricht und fällt."""
    r = rng(seed)
    n = int(0.6 * SR)
    f = curve(n, [(0, r.uniform(600, 750)), (0.3, r.uniform(800, 900)), (1, r.uniform(300, 400))])
    y = tone(f, env_ad(n, SR, 0.02), SR, harmonics=((1, 1.0), (2, 0.4), (3, 0.2), (5, 0.08)))
    return formant_filter(y, SR, [(800, 400, 1.0), (1800, 600, 0.4)])


def ray_death(seed: int) -> np.ndarray:
    """Ein langer, sinkender Ruf, über dem ein Schimmer verklingt."""
    r = rng(seed)
    n = int(2.4 * SR)
    f = curve(n, [(0, 520), (0.2, 600), (1, 160)])
    y = tone(f, curve(n, [(0, 0), (0.08, 1.0), (0.7, 0.6), (1, 0)]), SR, harmonics=((1, 1.0), (2, 0.35), (3, 0.15)))
    for i in range(6):
        b = bell(float(r.choice([1568.0, 1760.0, 2093.0, 2349.3])), 1.4, SR, decay=0.9, seed=seed + i)
        mix_at(y, b, 0.6 + i * 0.22, 0.18)
    return formant_filter(y, SR, [(700, 400, 1.0), (1500, 500, 0.35)])


# =====================================================================================
# Der Zerbrochene König
# =====================================================================================

def _bell(*args, **kwargs) -> np.ndarray:
    """Wie bell(), aber sanft ausgeblendet (sonst knackt es, wo die Glocke abgeschnitten wird)."""
    b = bell(*args, **kwargs)
    k = max(1, len(b) * 2 // 5)
    b[-k:] *= 0.5 + 0.5 * np.cos(np.linspace(0, np.pi, k))
    return b


def _voice(n: int, f: np.ndarray, amp: np.ndarray, vowel: str, r, rough: float = 0.0) -> np.ndarray:
    """Eine tiefe, hallende Stimme: Sägezahn durch die Formanten eines Vokals, leicht schwankend, auf Wunsch rau."""
    t = np.arange(n) / SR
    f = f * (1 + 0.006 * np.sin(2 * np.pi * r.uniform(4.5, 5.5) * t))
    y = saw(f, amp, SR, partials=40, rolloff=1.15)
    if rough > 0:
        # Rauheit: eine langsamere Unterschwingung (Strohbass) moduliert die Stimme
        y *= 1 + rough * np.sin(np.cumsum(f * 0.5) * 2 * np.pi / SR)
    formants = {
        "o": [(420, 90, 1.0), (780, 120, 0.6), (2400, 300, 0.12)],
        "a": [(680, 110, 1.0), (1150, 150, 0.55), (2500, 300, 0.15)],
        "u": [(320, 70, 1.0), (650, 110, 0.35), (2300, 300, 0.08)],
    }[vowel]
    return formant_filter(y, SR, formants)


def king_ambient(seed: int) -> np.ndarray:
    """Ein tiefes, trauerndes Summen wie von einem fernen Chor, darüber ein kristallener Schimmer, darunter Stein."""
    r = rng(seed)
    n = int(r.uniform(2.6, 3.2) * SR)
    t = np.arange(n) / SR
    f0 = r.uniform(52, 60)
    y = np.zeros(n)
    for ratio, gain, vowel in ((1.0, 1.0, "o"), (1.5, 0.45, "u"), (2.0, 0.3, "o")):
        f = curve(n, [(0, f0 * ratio * 1.02), (0.5, f0 * ratio), (1, f0 * ratio * r.uniform(0.92, 0.96))])
        y += gain * _voice(n, f, env_asr(n, SR, 0.6, 1.0), vowel, r)
    y *= 1 + 0.15 * np.sin(2 * np.pi * r.uniform(0.3, 0.6) * t)
    for i in range(3):
        b = _bell(float(r.choice([987.8, 1318.5, 1480.0, 1975.5])), 2.0, SR, ratios=(1.0, 2.0, 3.01, 4.2),
                 amps=(1.0, 0.3, 0.12, 0.05), decay=1.3, seed=seed + i)
        mix_at(y, b, r.uniform(0.3, n / SR - 1.2), 0.07)
    grind = filt(r.standard_normal(n), "band", [90, 500], SR, 2) * curve(n, [(0, 0), (0.4, 0.4), (0.7, 0.1), (1, 0)])
    return filt(y, "low", 4500, SR, 2) + grind * 0.08


def king_hurt(seed: int) -> np.ndarray:
    """Marmor bricht: ein trockenes Krachen und Splittern, dazu ein kurzes, tiefes Stöhnen."""
    r = rng(seed)
    n = int(1.0 * SR)
    y = np.zeros(n)
    crack = filt(r.standard_normal(int(0.25 * SR)), "band", [400, 4500], SR, 2) * env_ad(int(0.25 * SR), SR, 0.002, 0.04)
    mix_at(y, crack, 0.0, 1.0)
    for i in range(int(r.integers(5, 9))):
        m = int(r.uniform(0.01, 0.03) * SR)
        chip = filt(r.standard_normal(m), "band", [1500, 7000], SR, 2) * env_ad(m, SR, 0.001, 0.008)
        mix_at(y, chip, r.uniform(0.02, 0.35), r.uniform(0.2, 0.5))
    m = int(0.75 * SR)
    f = curve(m, [(0, r.uniform(78, 88)), (0.3, r.uniform(70, 76)), (1, 55)])
    groan = _voice(m, f, curve(m, [(0, 0), (0.08, 1.0), (0.5, 0.6), (1, 0)]), "a", r, rough=0.35)
    mix_at(y, groan, 0.04, 0.9)
    b = _bell(float(r.choice([1661.2, 1864.7, 2217.5])), 0.9, SR, decay=0.5, seed=seed)
    mix_at(y, b, 0.03, 0.18)
    return y


def king_death(seed: int) -> np.ndarray:
    """Ein langes, sinkendes Klagen, Stein bricht und Kristall zerspringt in einer Kaskade; am Ende verklingt ein Akkord."""
    r = rng(seed)
    n = int(5.2 * SR)
    y = np.zeros(n)
    m = int(3.6 * SR)
    f = curve(m, [(0, 92), (0.15, 104), (0.6, 70), (1, 38)])
    wail = _voice(m, f, curve(m, [(0, 0), (0.06, 1.0), (0.55, 0.8), (1, 0)]), "a", r, rough=0.25)
    wail += 0.5 * _voice(m, f * 1.5, curve(m, [(0, 0), (0.1, 0.6), (0.6, 0.3), (1, 0)]), "o", r)
    mix_at(y, wail, 0.0, 1.0)
    rumble = filt(r.standard_normal(n), "low", 160, SR, 2) * curve(n, [(0, 0), (0.1, 0.8), (0.65, 1.0), (0.8, 0.2), (1, 0)])
    y += rumble * 0.9
    for i in range(26):
        at = 0.6 + (i / 26) ** 1.4 * 3.4 + r.uniform(-0.05, 0.05)
        b = _bell(float(r.choice([1046.5, 1318.5, 1568.0, 1760.0, 2093.0, 2637.0, 3136.0])), 1.6, SR, decay=0.9, seed=seed + 40 + i)
        mix_at(y, b, at, r.uniform(0.08, 0.2))
        if i % 3 == 0:
            k = int(0.2 * SR)
            crash = filt(r.standard_normal(k), "band", [600, 6000], SR, 2) * env_ad(k, SR, 0.002, 0.05)
            mix_at(y, crash, at, 0.35)
    # der letzte Akkord (a-Moll, hoch, sehr leise verklingend)
    k = int(2.4 * SR)
    for fr, a in ((440.0, 0.25), (523.25, 0.2), (659.26, 0.18), (880.0, 0.1)):
        mix_at(y, tone(np.full(k, fr), curve(k, [(0, 0), (0.15, a), (1, 0)]), SR, harmonics=((1, 1.0), (2, 0.2), (3, 0.06))), 2.8, 0.6)
    return y


def king_awaken(seed: int) -> np.ndarray:
    """Erwachen: ein Grollen steigt, Trümmer klicken immer schneller zusammen, ein Chor schwillt an und bricht hell auf."""
    r = rng(seed)
    n = int(3.8 * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    rumble = filt(r.standard_normal(n), "low", 140, SR, 2) * curve(n, [(0, 0.2), (0.7, 1.0), (0.8, 0.5), (1, 0)])
    y += rumble * 0.9
    # Trümmer, die sich zusammenfügen: Klicks, deren Abstand schrumpft
    at = 0.15
    while at < 2.8:
        k = int(r.uniform(0.015, 0.04) * SR)
        click = filt(r.standard_normal(k), "band", [300, 2500], SR, 2) * env_ad(k, SR, 0.001, 0.01)
        mix_at(y, click, at, r.uniform(0.25, 0.6))
        at += max(0.035, 0.32 * (1 - at / 3.0) ** 1.5) * r.uniform(0.7, 1.3)
    # der Chor (D-Moll), steigt von unten in den Akkord
    for f, a, vowel in ((73.42, 1.0, "o"), (110.0, 0.6, "o"), (146.83, 0.5, "a"), (174.61, 0.35, "a"), (220.0, 0.3, "o")):
        fr = curve(n, [(0, f * 0.9), (0.62, f), (1, f)])
        y += a * _voice(n, fr, curve(n, [(0, 0), (0.35, 0.3), (0.72, 1.0), (0.82, 0.6), (1, 0)]), vowel, r) * 0.5
    # heller Aufbruch
    for i in range(6):
        b = _bell(float(r.choice([1174.7, 1396.9, 1760.0, 2349.3])), 1.6, SR, decay=1.0, seed=seed + 10 + i)
        mix_at(y, b, 2.75 + i * 0.05, 0.22)
    whoosh = filt(r.standard_normal(n), "band", [600, 4000], SR, 2) * curve(n, [(0, 0), (0.6, 0.0), (0.72, 0.6), (0.85, 0.0), (1, 0)])
    return y + whoosh * 0.3 + 0.0 * t


def king_roar(seed: int) -> np.ndarray:
    """Ein gewaltiger, tiefer Schrei: rau, mit Atem und einem kristallenen Nachklang."""
    r = rng(seed)
    n = int(2.4 * SR)
    f = curve(n, [(0, 58), (0.12, 84), (0.55, 92), (1, 62)]) * r.uniform(0.96, 1.04)
    amp = curve(n, [(0, 0), (0.06, 0.8), (0.18, 1.0), (0.7, 0.85), (1, 0)])
    y = _voice(n, f, amp, "a", r, rough=0.55)
    y += 0.55 * _voice(n, f * 2.01, amp, "o", r, rough=0.3)
    breath = filt(r.standard_normal(n), "band", [500, 3500], SR, 2) * amp
    y = soft_clip(y * 1.6, 0.9) + breath * 0.25
    for i in range(3):
        b = _bell(float(r.choice([1318.5, 1568.0, 1975.5])), 1.6, SR, decay=1.1, seed=seed + i)
        mix_at(y, b, 0.25 + i * 0.12, 0.15)
    return y


def king_sweep(seed: int) -> np.ndarray:
    """Wischhieb: ein schweres, tiefes Fauchen, das von hinten nach vorn zieht, Stein schrammt dabei."""
    r = rng(seed)
    n = int(1.1 * SR)
    noise = r.standard_normal(n)
    y = np.zeros(n)
    block = 2048
    for s0 in range(0, n, block // 2):
        e = min(n, s0 + block)
        k = s0 / n
        center = 180 + 900 * np.sin(np.pi * min(1.0, k * 1.2)) ** 2
        y[s0:e] += filt(noise[s0:e], "band", [center * 0.55, center * 1.8], SR, 2) * np.hanning(e - s0)
    y *= curve(n, [(0, 0), (0.35, 0.5), (0.55, 1.0), (0.75, 0.4), (1, 0)])
    grind = filt(r.standard_normal(n), "band", [120, 600], SR, 2) * curve(n, [(0, 0), (0.45, 0.0), (0.6, 0.6), (0.9, 0.2), (1, 0)])
    return y + grind * 0.5


def king_gravity(seed: int) -> np.ndarray:
    """Schwerkraftbruch: ein unheimliches, schwebendes Anschwellen (wie rückwärts) – nach 2,1 s ein donnernder Schlag."""
    r = rng(seed)
    n = int(3.4 * SR)
    t = np.arange(n) / SR
    swell_n = int(2.1 * SR)
    y = np.zeros(n)
    st = np.arange(swell_n) / SR
    f = curve(swell_n, [(0, 110), (1, 220)]) * (1 + 0.04 * np.sin(2 * np.pi * curve(swell_n, [(0, 2), (1, 9)]) * st))
    amp = curve(swell_n, [(0, 0.0), (0.85, 0.8), (1, 1.0)]) ** 1.5
    swell = tone(f, amp, SR, harmonics=((1, 1.0), (1.5, 0.5), (2.0, 0.4), (3.01, 0.2), (4.02, 0.1)))
    swell += 0.6 * tone(f * 0.5 * 1.003, amp, SR, harmonics=((1, 1.0), (2, 0.3)))
    hiss = filt(r.standard_normal(swell_n), "band", [1500, 6000], SR, 2) * amp
    mix_at(y, swell + hiss * 0.15, 0.0, 1.0)
    # der Schlag
    k = int(1.3 * SR)
    boom = filt(r.standard_normal(k), "low", 220, SR, 2) * env_ad(k, SR, 0.003, 0.35)
    thud = tone(curve(k, [(0, 70), (1, 36)]), env_ad(k, SR, 0.002, 0.3), SR, harmonics=((1, 1.0), (2, 0.4), (3, 0.15)))
    crack = filt(r.standard_normal(k), "band", [800, 7000], SR, 2) * env_ad(k, SR, 0.001, 0.06)
    mix_at(y, boom * 1.2 + thud * 1.0 + crack * 0.5, 2.1, 1.0)
    return y + 0.0 * t


def king_beam(seed: int) -> np.ndarray:
    """Sternenstrahl: ein Aufbruch, dann ein starkes, schimmerndes Brausen mit Knistern, das nach vier Sekunden erlischt."""
    r = rng(seed)
    n = int(4.4 * SR)
    t = np.arange(n) / SR
    amp = curve(n, [(0, 0), (0.03, 1.0), (0.85, 0.85), (0.93, 0.3), (1, 0)])
    y = np.zeros(n)
    for f, a in ((146.83, 0.6), (220.0, 0.5), (293.66, 0.35), (440.0, 0.25), (587.33, 0.12)):
        ff = f * (1 + 0.003 * np.sin(2 * np.pi * r.uniform(0.2, 0.5) * t + r.uniform(0, 6)))
        y += a * saw(ff, amp, SR, partials=16, rolloff=1.4)
    # Phasenwandern: ein langsam kreisender Kammfilter-Effekt durch zwei verschobene Kopien
    shift = (0.002 + 0.0015 * np.sin(2 * np.pi * 0.35 * t)) * SR
    idx = np.clip(np.arange(n) - shift.astype(int), 0, n - 1)
    y = y + y[idx] * 0.7
    roar = filt(r.standard_normal(n), "band", [300, 5000], SR, 2) * amp
    y += roar * 0.35
    crackle = np.zeros(n)
    for i in range(int(4.4 * 30)):
        k = int(0.006 * SR)
        mix_at(crackle, filt(r.standard_normal(k), "high", 3000, SR, 2) * env_ad(k, SR, 0.0005, 0.002), r.uniform(0.05, 4.0), r.uniform(0.2, 0.6))
    y += crackle * 0.4
    k = int(0.6 * SR)
    burst = filt(r.standard_normal(k), "band", [200, 3000], SR, 2) * env_ad(k, SR, 0.002, 0.15)
    mix_at(y, burst, 0.0, 1.2)
    return y


def king_rift(seed: int) -> np.ndarray:
    """Risse der Leere: ein tiefes Knurren beim Ausholen, dann schrammen Klauen über Stein (raues, ruckendes Kratzen),
    und die Risse knistern vor Leerenenergie (helles, elektrisches Zirpen), darunter ein dunkles Grollen."""
    r = rng(seed)
    n = int(2.6 * SR)
    y = np.zeros(n)
    # Knurren beim Ausholen
    m = int(0.8 * SR)
    f = curve(m, [(0, 62), (0.5, 74), (1, 58)])
    growl = _voice(m, f, curve(m, [(0, 0), (0.2, 0.7), (0.8, 0.6), (1, 0)]), "o", r, rough=0.7)
    mix_at(y, soft_clip(growl * 1.4, 0.9), 0.0, 0.6)
    # Klauen in Stein: Rauschen, ruckweise geöffnet, durch ein tiefes Band
    k = int(1.1 * SR)
    grit = r.standard_normal(k)
    knots = np.arange(0, k + 600, 600)
    jerk = np.interp(np.arange(k), knots, r.uniform(0.15, 1.0, len(knots)))
    scrape = filt(grit, "band", [250, 2400], SR, 2) * jerk * curve(k, [(0, 0), (0.05, 1.0), (0.7, 0.8), (1, 0)])
    mix_at(y, scrape, 0.62, 0.9)
    stone = filt(r.standard_normal(k), "low", 300, SR, 2) * curve(k, [(0, 0), (0.08, 1.0), (1, 0)])
    mix_at(y, stone, 0.62, 0.8)
    # Knistern der Risse
    crackle = np.zeros(n)
    for _ in range(140):
        q = int(r.uniform(0.002, 0.008) * SR)
        burst = filt(r.standard_normal(q), "high", 2500, SR, 2) * env_ad(q, SR, 0.0004, 0.002)
        mix_at(crackle, burst, r.uniform(0.85, 2.4), r.uniform(0.2, 0.7))
    y += crackle * 0.5
    t = np.arange(n) / SR
    zap = tone(np.full(n, 1760.0) * (1 + 0.02 * np.sin(2 * np.pi * 37 * t)), curve(n, [(0, 0), (0.33, 0), (0.4, 0.4), (0.9, 0.25), (1, 0)]), SR,
               harmonics=((1, 1.0), (2, 0.3), (3, 0.2)))
    y += zap * 0.06
    rumble = filt(r.standard_normal(n), "low", 120, SR, 2) * curve(n, [(0, 0), (0.3, 0.2), (0.6, 0.6), (1, 0)])
    return y + rumble * 0.6


def king_erupt(seed: int) -> np.ndarray:
    """Die Leere bricht hervor: ein dumpfer Stoß von unten, ein aufsteigendes Fauchen, splitternder Kristall."""
    r = rng(seed)
    n = int(1.6 * SR)
    y = np.zeros(n)
    k = int(0.9 * SR)
    thud = tone(curve(k, [(0, 90), (1, 40)]), env_ad(k, SR, 0.002, 0.2), SR, harmonics=((1, 1.0), (2, 0.4), (3, 0.1)))
    mix_at(y, thud, 0.0, 1.0)
    boom = filt(r.standard_normal(k), "low", 260, SR, 2) * env_ad(k, SR, 0.003, 0.18)
    mix_at(y, boom, 0.0, 1.0)
    m = int(1.0 * SR)
    noise = r.standard_normal(m)
    rise = np.zeros(m)
    block = 2048
    for s0 in range(0, m, block // 2):
        e = min(m, s0 + block)
        c = 300 * (12.0 ** (s0 / m))
        rise[s0:e] += filt(noise[s0:e], "band", [c * 0.6, c * 1.7], SR, 2) * np.hanning(e - s0)
    mix_at(y, rise * curve(m, [(0, 0), (0.08, 1.0), (1, 0)]), 0.02, 0.6)
    for i in range(3):
        b = _bell(float(r.uniform(1500, 3200)), 0.9, SR, decay=0.5, seed=seed + i)
        mix_at(y, b, 0.03 + i * 0.02, 0.1)
    return y


def king_quake(seed: int) -> np.ndarray:
    """Leerenbeben: ein drohendes Anschwellen, während die Fäuste sich heben – nach 1,05 s ein gewaltiger Schlag, der
    Boden grollt nach, ein dunkler, schwingender Ton rollt fort."""
    r = rng(seed)
    n = int(3.6 * SR)
    y = np.zeros(n)
    hit = 1.05
    m = int(hit * SR)
    f = curve(m, [(0, 55), (1, 82)])
    swell = tone(f, curve(m, [(0, 0), (0.7, 0.5), (1, 1.0)]) ** 1.4, SR, harmonics=((1, 1.0), (1.5, 0.4), (2, 0.3), (3, 0.15)))
    hiss = filt(r.standard_normal(m), "band", [400, 2500], SR, 2) * curve(m, [(0, 0), (0.8, 0.4), (1, 1.0)]) ** 2
    mix_at(y, swell * 0.7 + hiss * 0.3, 0.0, 1.0)
    k = int(2.4 * SR)
    thud = tone(curve(k, [(0, 66), (0.3, 34), (1, 28)]), env_ad(k, SR, 0.002, 0.6), SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.2)))
    boom = filt(r.standard_normal(k), "low", 200, SR, 2) * env_ad(k, SR, 0.003, 0.5)
    crack = filt(r.standard_normal(k), "band", [700, 6000], SR, 2) * env_ad(k, SR, 0.001, 0.07)
    fade = curve(k, [(0, 1.0), (0.75, 1.0), (1, 0.0)])
    mix_at(y, (thud * 1.2 + boom * 1.3 + crack * 0.6) * fade, hit, 1.0)
    t = np.arange(k) / SR
    ring = tone(np.full(k, 98.0) * (1 + 0.01 * np.sin(2 * np.pi * 3.2 * t)), env_ad(k, SR, 0.05, 1.1), SR,
                harmonics=((1, 1.0), (2.02, 0.5), (3.01, 0.25), (4.5, 0.1)))
    mix_at(y, ring * fade, hit + 0.02, 0.35)
    rumble = filt(r.standard_normal(k), "low", 90, SR, 2) * curve(k, [(0, 0), (0.05, 1.0), (1, 0)])
    mix_at(y, rumble, hit, 0.9)
    return y


# =====================================================================================
# Ausgabe
# =====================================================================================

# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil) oder None, Ziel-Pegel dBFS)
SYNTH = {
    "void_maw_ambient": (maw_ambient, 3, (1.6, 0.25), -18.0),
    "void_maw_inhale": (maw_inhale, 2, (2.0, 0.3), -15.0),
    "shard_warden_ambient": (warden_ambient, 3, (1.4, 0.25), -19.0),
    "shard_warden_awaken": (warden_awaken, 1, (2.2, 0.35), -15.0),
    "shard_warden_charge": (warden_charge, 2, (1.2, 0.25), -17.0),
    "distortling_ambient": (distortling_ambient, 3, (0.8, 0.2), -21.0),
    "distortling_hurt": (distortling_hurt, 3, None, -16.0),
    "distortling_death": (distortling_death, 1, (1.2, 0.25), -15.0),
    "distortling_blink": (distortling_blink, 3, (0.6, 0.15), -16.0),
    "distortling_attack": (distortling_attack, 2, None, -17.0),
    "star_ray_ambient": (ray_ambient, 4, (2.5, 0.35), -18.0),
    "star_ray_hurt": (ray_hurt, 2, (1.5, 0.25), -16.0),
    "star_ray_death": (ray_death, 1, (2.5, 0.35), -15.0),
    "shattered_king_ambient": (king_ambient, 3, (2.6, 0.35), -17.0),
    "shattered_king_hurt": (king_hurt, 3, (1.2, 0.2), -14.0),
    "shattered_king_death": (king_death, 1, (3.0, 0.35), -13.0),
    "shattered_king_awaken": (king_awaken, 1, (3.0, 0.35), -13.0),
    "shattered_king_roar": (king_roar, 2, (2.4, 0.3), -12.0),
    "shattered_king_sweep": (king_sweep, 2, (1.2, 0.2), -14.0),
    "shattered_king_gravity": (king_gravity, 1, (2.6, 0.3), -13.0),
    "shattered_king_beam": (king_beam, 1, (2.0, 0.25), -14.0),
    "shattered_king_rift": (king_rift, 2, (1.8, 0.25), -14.0),
    "shattered_king_erupt": (king_erupt, 3, (1.4, 0.2), -14.0),
    "shattered_king_quake": (king_quake, 2, (2.6, 0.3), -12.0),
}


def v(path: str, pitch: float = 1.0, volume: float = 1.0, reach: int | None = None) -> dict:
    e = {"name": f"minecraft:{path}"}
    if pitch != 1.0:
        e["pitch"] = pitch
    if volume != 1.0:
        e["volume"] = volume
    if reach is not None:
        e["attenuation_distance"] = reach
    return e


# Ereignis: Liste der Klänge (eigene über ihren Namen in SYNTH, Vanilla über v(...)) und Reichweite der eigenen
EVENTS = {
    "entity.void_maw.ambient": (["void_maw_ambient"], 24),
    "entity.void_maw.inhale": (["void_maw_inhale"], 32),
    "entity.void_maw.bite": ([v(f"mob/ravager/bite{i}", 1.25) for i in range(1, 4)], None),
    "entity.void_maw.hurt": ([v(f"mob/guardian/guardian_hit{i}", 0.6) for i in range(1, 5)], None),
    "entity.void_maw.death": ([v("mob/guardian/guardian_death", 0.5)], None),
    "entity.shard_warden.ambient": (["shard_warden_ambient"], 20),
    "entity.shard_warden.hurt": ([v(f"block/amethyst/break{i}", 0.55, 1.0) for i in range(1, 5)]
                                 + [v(f"mob/irongolem/hit{i}", 0.75) for i in range(1, 5)], None),
    "entity.shard_warden.death": ([v("mob/irongolem/death", 0.6)], None),
    "entity.shard_warden.step": ([v(f"mob/irongolem/walk{i}", 0.7) for i in range(1, 5)], None),
    "entity.shard_warden.awaken": (["shard_warden_awaken"], 32),
    "entity.shard_warden.charge": (["shard_warden_charge"], 24),
    "entity.shard_warden.shoot": ([v(f"block/amethyst_cluster/break{i}", 1.35) for i in range(1, 5)], None),
    "entity.shard_warden.slam": ([v("mob/warden/attack_impact_1", 0.7), v("mob/warden/attack_impact_2", 0.7)], None),
    "entity.warden_shard.hit": ([v(f"block/amethyst/break{i}", 1.3, 0.8) for i in range(1, 5)], None),
    "entity.distortling.ambient": (["distortling_ambient"], 16),
    "entity.distortling.hurt": (["distortling_hurt"], 16),
    "entity.distortling.death": (["distortling_death"], 16),
    "entity.distortling.blink": (["distortling_blink"], 20),
    "entity.distortling.attack": (["distortling_attack"], 16),
    "entity.star_ray.ambient": (["star_ray_ambient"], 64),
    "entity.star_ray.hurt": (["star_ray_hurt"], 32),
    "entity.star_ray.death": (["star_ray_death"], 32),
    "entity.shattered_king.ambient": (["shattered_king_ambient"], 48),
    "entity.shattered_king.hurt": (["shattered_king_hurt"], 40),
    "entity.shattered_king.death": (["shattered_king_death"], 96),
    "entity.shattered_king.awaken": (["shattered_king_awaken"], 96),
    "entity.shattered_king.roar": (["shattered_king_roar"], 96),
    "entity.shattered_king.sweep": (["shattered_king_sweep"], 40),
    "entity.shattered_king.gravity": (["shattered_king_gravity"], 64),
    "entity.shattered_king.beam": (["shattered_king_beam"], 64),
    "entity.shattered_king.rift": (["shattered_king_rift"], 48),
    "entity.shattered_king.erupt": (["shattered_king_erupt"], 40),
    "entity.shattered_king.quake": (["shattered_king_quake"], 80),
}


def files_of(name: str) -> list[str]:
    return [f"kingdomsatwar:entity/void/{name}{i + 1}" for i in range(SYNTH[name][1])]


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    files = []
    for i in range(count):
        x = fn(2000 + i * 31 + zlib.crc32(name.encode()) % 991)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 700 + i, stereo=False, damping=6000), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.04)
        x = soft_clip(fit_level(x, level, -2.0))
        path = OUT / f"{name}{i + 1}.ogg"
        write_ogg(x.astype(np.float32), SR, path, quality=5.0)
        files.append(f"kingdomsatwar:entity/void/{name}{i + 1}")
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
    rendered = {name: render(name, args.preview) if not args.only or name in args.only else files_of(name) for name in SYNTH}
    events = {}
    for key, (sounds, reach) in EVENTS.items():
        entries = []
        for s in sounds:
            if isinstance(s, str):
                entries += [{"name": f, "attenuation_distance": reach} if reach else f for f in rendered[s]]
            else:
                entries.append(s)
        events[key] = {"sounds": entries, "subtitle": f"subtitles.kingdomsatwar.{key}"}
    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8")) if SOUNDS_JSON.exists() else {}
    data.update(events)
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"{len(events)} Klangereignisse geschrieben")


if __name__ == "__main__":
    main()
