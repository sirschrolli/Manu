#!/usr/bin/env python3
"""
Eigene Klänge der Drachen (seit 1.1.2) – bisher liehen sie sich Klänge von Minecraft (Enderdrache, Verwüster, Schnüffler,
Lohe, Wächter). Jetzt haben sie eine eigene Stimme: ein großer Rachen, ein tiefer Brustkorb, Schuppen und Lederschwingen.

- Stimme: Stimmlippen-Pulse (rau, unregelmäßig, mit Knarren) durch die Formanten eines großen Rachens, der von „a“ zu „o“ und
  „u“ wandert; darunter ein zweiter, halb so hoher Brustton, darüber das Fauchen (Rauschen um 2 kHz) und ein Grollen.
- Schwingen: ein Luftstoß (gleitendes Rauschen), das Flattern der Flughaut und ein dumpfer Schlag.
- Schritte: ein schwerer, tiefer Aufprall, Kies unter den Klauen, zwei Krallen.
- Atem je Element: Feuer (Flammenwand mit Knistern), Eis (zischender Frost, Kristalle), Blitz (Summen, Funkenbögen, Zischen),
  Natur (Böe, raschelndes Laub, Pollen), Erde (Grollen, Kies, Felsbrocken), Wasser (rauschender Strahl, Blasen, Gischt).
- das Ei: Knacken der Schale und das Schlüpfen mit dem ersten Zirpen.

Die Tonhöhe stellt das Spiel nach dem Alter ein (Schlüpfling 1,7 … Ältester 0,8); die Klänge sind für einen ausgewachsenen
Drachen gebaut. Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py, monster_sounds.py); keine fremden Dateien.

Aufruf (aus dem Projektordner):
    python3 tools/audio/dragon_sounds.py [--only name ...] [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/dragon/<name><n>.ogg
    die Einträge "entity.dragon.*" und "block.dragon_egg.*" in assets/kingdomsatwar/sounds.json
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, bell, colored_noise, curve, fade_edges, filt, fit_level, glide, rng, room, soft_clip,  # noqa: E402
                          tone, trim_tail)
from dsp import write_ogg  # noqa: E402
from monster_sounds import (SR, at_curve, bubbles, clack, crackle, frames_len, mix_at, norm, pulse_voice, rumble, smooth_env,  # noqa: E402
                            squelch, swept, thud, tone_voice)

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/dragon"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"


# =====================================================================================
# Grundlagen
# =====================================================================================

def fit(x: np.ndarray, n: int) -> np.ndarray:
    """Auf genau n Werte bringen (abschneiden oder mit Stille auffüllen)."""
    return x[:n] if len(x) >= n else np.concatenate([x, np.zeros(n - len(x))])


def saturate(x: np.ndarray, drive: float) -> np.ndarray:
    """Weiche Sättigung (Kehle unter Druck): mehr Obertöne, die Spitzen runder."""
    peak = float(np.max(np.abs(x))) + 1e-12
    return np.tanh(drive * x / peak) / np.tanh(drive) * peak


def morph_voice(f: np.ndarray, amp: np.ndarray, seed: int, vowels, size: float, **kw) -> np.ndarray:
    """
    Eine Stimme, deren Rachen sich bewegt: dieselben Pulse durch mehrere Vokale, überblendet nach der Zeit
    (vowels = [(0…1, „a“), …]).
    """
    n = len(f)
    u = np.linspace(0.0, 1.0, n)
    names = list(dict.fromkeys(v for _, v in vowels))
    layers = {v: pulse_voice(f, amp, seed, v, size=size, **kw) for v in names}
    weights = {v: np.zeros(n) for v in names}
    xs = [p[0] for p in vowels]
    for i, v in enumerate(names):
        ys = [1.0 if name == v else 0.0 for _, name in vowels]
        weights[v] = np.interp(u, xs, ys)
    total = sum(weights.values()) + 1e-9
    y = sum(layers[v] * weights[v] / total for v in names)
    return norm(y, float(np.max(amp)))


def snort(dur: float, seed: int, gain: float = 1.0) -> np.ndarray:
    """Ein Schnauben durch die Nüstern: kurzes, gehauchtes Rauschen mit einem Hauch Flattern."""
    return swept(dur, [(0.0, 1100.0), (0.5, 1600.0), (1.0, 1000.0)], 1.2, [(0.0, 0.0), (0.12, gain), (0.5, 0.7 * gain), (1.0, 0.0)],
                 seed, lo=400.0, hi=3800.0, flicker=0.45, flicker_rate=(30.0, 45.0))


def gust(dur: float, centers, width: float, amps, seed: int, flicker: float = 0.25) -> np.ndarray:
    """Ein Luftstoß: Rauschen, dessen Band gleitet, leicht flackernd."""
    return swept(dur, centers, width, amps, seed, flicker=flicker, flicker_rate=(10.0, 18.0))


def sparkle(dur: float, seed: int, rate: float, lo: float, hi: float, decay=(0.04, 0.16), env=None) -> np.ndarray:
    """Kristallene Pünktchen (Frost, Pollen, Funkeln): kurze, hohe Glockentöne zu zufälligen Zeiten."""
    n = int(dur * SR)
    r = rng(seed)
    y = np.zeros(n)
    for _ in range(r.poisson(rate * dur)):
        at = r.uniform(0.0, dur * 0.95)
        f0 = float(np.exp(r.uniform(np.log(lo), np.log(hi))))
        g = r.uniform(0.25, 1.0) * (at_curve(env, at / dur) if env else 1.0)
        mix_at(y, bell(f0, r.uniform(*decay) * 3.0, SR, decay=r.uniform(*decay), seed=int(r.integers(1 << 30))), at, g)
    return norm(y)


def zap(f0: float, f1: float, dur: float, seed: int) -> np.ndarray:
    """Ein Funkenbogen: ein fallender, rauer Pfiff (Sinus mit Rauschen moduliert)."""
    m = max(32, int(dur * SR))
    r = rng(seed)
    t = np.arange(m) / SR
    f = glide(m, f0, f1, 0.6) * (1.0 + 0.04 * r.standard_normal(m))
    a = np.exp(-t / (dur / 3.0)) * np.minimum(1.0, t / 0.0015)
    y = np.sin(2 * np.pi * np.cumsum(f) / SR) * a
    y = y * (0.6 + 0.4 * np.abs(r.standard_normal(m)))
    return norm(filt(y, "high", 300.0, SR, 2))


def baby_chirp(seed: int, dur: float, f0: float) -> np.ndarray:
    """Ein junges Zirpen: ein heller, trillernder Ton mit einem Hauch Rauheit."""
    n = frames_len(dur)
    r = rng(seed)
    t = np.arange(n) / SR
    rise = r.uniform(1.12, 1.3)
    f = curve(n, [(0.0, f0), (0.35, f0 * rise), (1.0, f0 * r.uniform(0.85, 1.0))])
    f = f * (1.0 + 0.035 * np.sin(2 * np.pi * r.uniform(13.0, 19.0) * t))
    amp = curve(n, [(0.0, 0.0), (0.12, 1.0), (0.6, 0.75), (1.0, 0.0)])
    y = tone_voice(f, amp, seed, harmonics=((1, 1.0), (2, 0.35), (3, 0.15), (4, 0.06)), vowel="e", wet=0.35, breath=0.12)
    rasp = pulse_voice(f * 0.33, amp * 0.6, seed + 1, "e", size=1.45, jitter=0.05, shimmer=0.12, breath=0.3, bright=5000.0)
    return norm(y + 0.3 * rasp)


# =====================================================================================
# Stimme
# =====================================================================================

def dragon_ambient(seed: int) -> np.ndarray:
    """Ein tiefes, schnurrendes Grollen aus dem Brustkorb, manchmal mit einem Schnauben."""
    r = rng(seed)
    dur = r.uniform(1.7, 2.3)
    n = frames_len(dur)
    base = r.uniform(52.0, 64.0)
    f = curve(n, [(0.0, base * 0.95), (0.35, base * r.uniform(1.06, 1.16)), (0.75, base * 1.02), (1.0, base * 0.9)])
    amp = curve(n, [(0.0, 0.0), (0.18, 0.8), (0.4, 1.0), (0.75, 0.8), (1.0, 0.0)])
    voice = morph_voice(f, amp, seed, [(0.0, "o"), (0.6, "o"), (1.0, "u")], 0.55, jitter=0.045, shimmer=0.14, sub=0.45,
                        breath=0.65, bright=2600.0)
    chest = pulse_voice(f * 0.5, amp, seed + 1, "u", size=0.5, jitter=0.04, shimmer=0.1, sub=0.3, breath=0.2, bright=700.0)
    y = np.zeros(n + int(0.6 * SR))
    mix_at(y, voice, 0.0, 1.0)
    mix_at(y, chest, 0.0, 0.5)
    mix_at(y, rumble(dur, seed + 2, 95.0, [(0.0, 0.0), (0.4, 1.0), (1.0, 0.0)]), 0.0, 0.25)
    if r.uniform() < 0.6:
        mix_at(y, snort(r.uniform(0.22, 0.32), seed + 3), dur * r.choice([0.02, 0.78]), 0.15)
    return saturate(y, 1.6)


def dragon_baby_ambient(seed: int) -> np.ndarray:
    """Ein Schlüpfling: zwei, drei kleine Zirplaute, mal fragend, mal zufrieden."""
    r = rng(seed)
    count = int(r.integers(2, 4))
    y = np.zeros(int(1.1 * SR))
    at = 0.0
    for k in range(count):
        d = r.uniform(0.12, 0.22)
        mix_at(y, baby_chirp(seed + 10 * k, d, r.uniform(680.0, 900.0)), at, r.uniform(0.7, 1.0))
        at += d + r.uniform(0.03, 0.09)
    return y[:int((at + 0.2) * SR)]


def dragon_hurt(seed: int) -> np.ndarray:
    """Ein kurzer, schmerzerfüllter Aufschrei: steil hinauf, rau, dann abfallend."""
    r = rng(seed)
    dur = r.uniform(0.6, 0.85)
    n = frames_len(dur)
    peak = r.uniform(160.0, 190.0)
    f = curve(n, [(0.0, peak * 0.7), (0.15, peak), (0.45, peak * 0.85), (1.0, peak * 0.52)])
    amp = curve(n, [(0.0, 0.0), (0.05, 1.0), (0.35, 0.85), (1.0, 0.0)])
    voice = morph_voice(f, amp, seed, [(0.0, "a"), (0.7, "a"), (1.0, "o")], 0.68, jitter=0.09, shimmer=0.22, sub=0.25,
                        breath=0.5, bright=4500.0)
    hiss = swept(dur, [(0.0, 2400.0), (1.0, 1700.0)], 1.1, [(0.0, 0.0), (0.06, 1.0), (0.5, 0.5), (1.0, 0.0)], seed + 1, flicker=0.4)
    y = np.zeros(n + int(0.3 * SR))
    mix_at(y, voice, 0.0, 1.0)
    mix_at(y, hiss, 0.0, 0.3)
    mix_at(y, pulse_voice(f * 0.5, amp, seed + 2, "u", size=0.55, jitter=0.06, sub=0.2, bright=800.0), 0.0, 0.35)
    return saturate(y, 2.2)


def dragon_death(seed: int) -> np.ndarray:
    """Ein letztes, langes Brüllen, das in ein Röcheln sinkt; dann schlägt der schwere Körper auf."""
    r = rng(seed)
    dur = r.uniform(2.8, 3.3)
    n = frames_len(dur)
    f = curve(n, [(0.0, 105.0), (0.18, 138.0), (0.45, 112.0), (0.7, 74.0), (1.0, 44.0)]) * r.uniform(0.93, 1.05)
    amp = curve(n, [(0.0, 0.0), (0.07, 1.0), (0.4, 0.85), (0.7, 0.45), (0.9, 0.18), (1.0, 0.0)])
    voice = morph_voice(f, amp, seed, [(0.0, "a"), (0.4, "a"), (0.7, "o"), (1.0, "u")], 0.6, jitter=0.07, shimmer=0.2,
                        sub=0.4, breath=0.7, bright=3600.0)
    chest = pulse_voice(f * 0.5, amp, seed + 1, "u", size=0.5, jitter=0.06, sub=0.35, breath=0.25, bright=750.0)
    rasp = swept(dur, [(0.0, 2200.0), (0.5, 1800.0), (1.0, 900.0)], 1.2, [(0.0, 0.0), (0.1, 0.8), (0.6, 0.4), (1.0, 0.0)],
                 seed + 2, flicker=0.4)
    exhale = swept(1.1, [(0.0, 900.0), (1.0, 500.0)], 1.6, [(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)], seed + 3, flicker=0.2)
    y = np.zeros(n + int(1.6 * SR))
    mix_at(y, voice, 0.0, 1.0)
    mix_at(y, chest, 0.0, 0.5)
    mix_at(y, rasp, 0.0, 0.22)
    mix_at(y, exhale, dur * 0.82, 0.35)
    mix_at(y, thud(0.9, 62.0, 34.0, seed + 4, noise_hi=260.0, grit=0.7), dur * 0.88, 0.9)
    mix_at(y, rumble(1.0, seed + 5, 120.0, [(0.0, 1.0), (1.0, 0.0)]), dur * 0.88, 0.4)
    return saturate(y, 1.9)


def dragon_roar(seed: int) -> np.ndarray:
    """
    Das Brüllen: Anlauf aus der Brust, der Rachen öffnet sich weit (a), die Stimme steigt, bricht rau und fällt; darunter der
    Brustton, darüber das Fauchen, dazu ein Grollen wie fernes Donnern.
    """
    r = rng(seed)
    dur = r.uniform(2.2, 2.7)
    n = frames_len(dur)
    top = r.uniform(118.0, 138.0)
    f = curve(n, [(0.0, top * 0.55), (0.12, top * 0.8), (0.35, top), (0.6, top * 0.93), (0.85, top * 0.72), (1.0, top * 0.5)])
    f = f * (1.0 + 0.015 * np.sin(2 * np.pi * r.uniform(5.0, 7.0) * np.arange(n) / SR))
    amp = curve(n, [(0.0, 0.0), (0.05, 0.55), (0.16, 1.0), (0.7, 0.92), (0.88, 0.55), (1.0, 0.0)])
    voice = morph_voice(f, amp, seed, [(0.0, "o"), (0.15, "a"), (0.7, "a"), (1.0, "o")], 0.6, jitter=0.07, shimmer=0.18,
                        sub=0.35, breath=0.6, bright=5200.0)
    voice = saturate(voice, 2.6)
    chest = pulse_voice(f * 0.5, amp, seed + 1, "u", size=0.48, jitter=0.05, shimmer=0.12, sub=0.25, breath=0.2, bright=850.0)
    rasp = swept(dur, [(0.0, 1800.0), (0.3, 2800.0), (0.75, 2500.0), (1.0, 1600.0)], 1.1,
                 [(0.0, 0.0), (0.15, 0.9), (0.7, 0.75), (1.0, 0.0)], seed + 2, flicker=0.45, flicker_rate=(24.0, 40.0))
    thunder = rumble(dur + 0.6, seed + 3, 100.0, [(0.0, 0.0), (0.25, 1.0), (0.8, 0.6), (1.0, 0.0)])
    y = np.zeros(n + int(1.2 * SR))
    mix_at(y, voice, 0.0, 1.0)
    mix_at(y, chest, 0.0, 0.45)
    mix_at(y, rasp, 0.0, 0.42)
    mix_at(y, thunder, 0.05, 0.3)
    return saturate(y, 1.3)


def dragon_grow(seed: int) -> np.ndarray:
    """Der Drache wächst: ein aufsteigender, glänzender Klang, dann ein stolzer, kurzer Ruf."""
    r = rng(seed)
    y = np.zeros(int(2.6 * SR))
    for k, f0 in enumerate([392.0, 523.3, 659.3, 784.0, 1046.5]):
        mix_at(y, bell(f0, 1.6, SR, decay=0.9, seed=seed + k), 0.09 * k, 0.55 - 0.05 * k)
    mix_at(y, sparkle(1.4, seed + 7, 26.0, 2500.0, 6500.0, env=[(0.0, 0.3), (0.4, 1.0), (1.0, 0.0)]), 0.1, 0.25)
    dur = 0.85
    n = frames_len(dur)
    f = curve(n, [(0.0, 105.0), (0.3, 150.0), (1.0, 112.0)])
    amp = curve(n, [(0.0, 0.0), (0.1, 1.0), (0.7, 0.8), (1.0, 0.0)])
    call = morph_voice(f, amp, seed + 3, [(0.0, "a"), (1.0, "o")], 0.66, jitter=0.05, shimmer=0.14, sub=0.2, breath=0.45,
                       bright=4000.0)
    mix_at(y, saturate(call, 1.6), 0.62, 0.8)
    return y


# =====================================================================================
# Körper
# =====================================================================================

def dragon_flap(seed: int) -> np.ndarray:
    """Ein Schlag der großen Lederschwingen: Luftstoß, flatternde Flughaut, dumpfer Schlag."""
    r = rng(seed)
    dur = r.uniform(0.55, 0.75)
    size = r.uniform(0.85, 1.15)
    y = np.zeros(int((dur + 0.4) * SR))
    push = gust(dur, [(0.0, 420.0 * size), (0.3, 230.0 * size), (1.0, 140.0 * size)], 1.7,
                [(0.0, 0.0), (0.18, 1.0), (0.45, 0.55), (1.0, 0.0)], seed, flicker=0.15)
    mix_at(y, push, 0.0, 1.0)
    n = int(0.32 * SR)
    t = np.arange(n) / SR
    rate = r.uniform(17.0, 24.0)
    flutter = swept(0.32, [(0.0, 1100.0 * size), (1.0, 700.0 * size)], 1.2, [(0.0, 0.0), (0.25, 1.0), (1.0, 0.0)], seed + 1)
    flutter = fit(flutter, n) * (0.55 + 0.45 * np.sin(2 * np.pi * rate * t) ** 2)
    mix_at(y, flutter, 0.06, 0.32)
    mix_at(y, thud(0.32, 78.0 * size, 52.0 * size, seed + 2, noise_hi=220.0, grit=0.35), 0.1, 0.55)
    snap = swept(0.07, [(0.0, 2600.0), (1.0, 1800.0)], 1.0, [(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)], seed + 3)
    mix_at(y, snap, 0.17, 0.1)
    return y


def dragon_step(seed: int) -> np.ndarray:
    """Ein schwerer Schritt: tiefer Aufprall, Kies unter der Klaue, zwei Krallen auf Stein."""
    r = rng(seed)
    y = np.zeros(int(0.55 * SR))
    mix_at(y, thud(0.4, r.uniform(58.0, 70.0), r.uniform(38.0, 46.0), seed, noise_hi=320.0, grit=0.55), 0.0, 0.5)
    knock = swept(0.16, [(0.0, 340.0), (1.0, 170.0)], 1.2, [(0.0, 0.0), (0.05, 1.0), (0.4, 0.4), (1.0, 0.0)], seed + 4)
    mix_at(y, knock, 0.0, 0.9)
    mix_at(y, crackle(0.22, seed + 1, 320.0, lo=700.0, hi=3200.0, env=[(0.0, 1.0), (1.0, 0.0)]), 0.01, 0.45)
    for k in range(2):
        modes = [(r.uniform(2000.0, 2600.0), 1.0), (r.uniform(3300.0, 4200.0), 0.55)]
        mix_at(y, clack(modes, 0.012, seed + 5 + k, q=10.0), 0.015 + 0.03 * k + r.uniform(0.0, 0.01), 0.18)
    return y


def dragon_bite(seed: int) -> np.ndarray:
    """Das Zuschnappen: ein kurzer Luftzug, harte Zähne, darunter ein Knurren."""
    r = rng(seed)
    y = np.zeros(int(0.65 * SR))
    mix_at(y, gust(0.1, [(0.0, 700.0), (1.0, 1400.0)], 1.2, [(0.0, 0.0), (0.6, 1.0), (1.0, 0.2)], seed), 0.0, 0.3)
    teeth = [(r.uniform(1300.0, 1600.0), 1.0), (r.uniform(2500.0, 2900.0), 0.75), (r.uniform(3900.0, 4500.0), 0.5)]
    mix_at(y, clack(teeth, 0.03, seed + 1, q=16.0), 0.085, 1.0)
    mix_at(y, clack([(f * 0.97, g) for f, g in teeth], 0.02, seed + 2, q=12.0), 0.1, 0.4)
    snap = swept(0.03, [(0.0, 4200.0), (1.0, 3000.0)], 0.9, [(0.0, 0.0), (0.15, 1.0), (1.0, 0.0)], seed + 5)
    mix_at(y, snap, 0.083, 0.35)
    dur = 0.42
    n = frames_len(dur)
    f = curve(n, [(0.0, 120.0), (0.3, 132.0), (1.0, 92.0)])
    amp = curve(n, [(0.0, 0.0), (0.12, 1.0), (1.0, 0.0)])
    growl = pulse_voice(f, amp, seed + 3, "a", size=0.66, jitter=0.08, shimmer=0.2, sub=0.3, breath=0.5, bright=3500.0)
    mix_at(y, filt(saturate(growl, 1.8), "high", 140.0, SR, 2), 0.05, 0.28)
    return y


def dragon_eat(seed: int) -> np.ndarray:
    """Fressen: zwei, drei schmatzende Bissen, Knochen knacken, ein zufriedenes Grummeln."""
    r = rng(seed)
    y = np.zeros(int(1.0 * SR))
    at = 0.0
    for k in range(int(r.integers(2, 4))):
        chomp = [(r.uniform(650.0, 850.0), 1.0), (r.uniform(1200.0, 1500.0), 0.5), (r.uniform(2300.0, 2800.0), 0.25)]
        mix_at(y, clack(chomp, 0.025, seed + k, q=9.0), at, 0.7)
        mix_at(y, squelch(0.16, seed + 10 + k, hi=1800.0, lo=420.0), at + 0.01, 0.35)
        if r.uniform() < 0.5:
            mix_at(y, crackle(0.08, seed + 20 + k, 500.0, lo=1500.0, hi=5000.0), at + 0.02, 0.18)
        at += r.uniform(0.17, 0.24)
    n = frames_len(0.5)
    f = curve(n, [(0.0, 70.0), (1.0, 62.0)])
    amp = curve(n, [(0.0, 0.0), (0.3, 1.0), (1.0, 0.0)])
    mix_at(y, pulse_voice(f, amp, seed + 30, "u", size=0.55, jitter=0.04, sub=0.4, breath=0.4, bright=1500.0), at, 0.35)
    return y[:int((at + 0.55) * SR)]


# =====================================================================================
# Atem der Elemente
# =====================================================================================

def breath_fire(seed: int) -> np.ndarray:
    """Feuer: ein Fauchen, das zu einer brüllenden Flammenwand wird, darin Knistern und Knallen."""
    r = rng(seed)
    dur = r.uniform(1.4, 1.7)
    y = np.zeros(int((dur + 0.5) * SR))
    mix_at(y, gust(0.3, [(0.0, 2600.0), (1.0, 800.0)], 1.4, [(0.0, 0.0), (0.4, 1.0), (1.0, 0.3)], seed), 0.0, 0.5)
    roar = swept(dur, [(0.0, 520.0), (0.5, 420.0), (1.0, 360.0)], 2.2, [(0.0, 0.0), (0.12, 1.0), (0.8, 0.85), (1.0, 0.0)],
                 seed + 1, flicker=0.5, flicker_rate=(8.0, 14.0))
    mix_at(y, roar, 0.05, 1.0)
    mix_at(y, rumble(dur, seed + 2, 220.0, [(0.0, 0.0), (0.15, 1.0), (0.8, 0.7), (1.0, 0.0)]), 0.05, 0.5)
    mix_at(y, crackle(dur, seed + 3, 140.0, lo=1500.0, hi=6000.0, env=[(0.0, 0.2), (0.2, 1.0), (0.8, 0.8), (1.0, 0.0)]), 0.08, 0.35)
    for k in range(int(r.integers(2, 5))):
        mix_at(y, clack([(r.uniform(300.0, 600.0), 1.0), (r.uniform(900.0, 1400.0), 0.4)], 0.03, seed + 10 + k, q=4.0),
               r.uniform(0.2, dur * 0.85), 0.2)
    return y


def breath_ice(seed: int) -> np.ndarray:
    """Eis: ein eisiger, zischender Sturm, darin klirrende Kristalle und knisternder Frost."""
    r = rng(seed)
    dur = r.uniform(1.4, 1.7)
    y = np.zeros(int((dur + 0.6) * SR))
    hiss = swept(dur, [(0.0, 6200.0), (0.4, 4800.0), (1.0, 3800.0)], 1.3, [(0.0, 0.0), (0.1, 1.0), (0.8, 0.8), (1.0, 0.0)],
                 seed, flicker=0.3, flicker_rate=(12.0, 20.0))
    mix_at(y, hiss, 0.0, 0.3)
    mix_at(y, gust(dur, [(0.0, 1500.0), (0.5, 950.0), (1.0, 700.0)], 1.8, [(0.0, 0.0), (0.15, 1.0), (0.8, 0.7), (1.0, 0.0)],
                   seed + 1, flicker=0.3), 0.0, 1.0)
    mix_at(y, sparkle(dur, seed + 2, 34.0, 2600.0, 7000.0, decay=(0.05, 0.25),
                      env=[(0.0, 0.2), (0.3, 1.0), (1.0, 0.4)]), 0.05, 0.45)
    mix_at(y, crackle(dur, seed + 3, 90.0, lo=3500.0, hi=9000.0, env=[(0.0, 0.0), (0.3, 1.0), (1.0, 0.2)]), 0.1, 0.2)
    return y


def breath_lightning(seed: int) -> np.ndarray:
    """Blitz: ein Knall zu Beginn, ein bösartiges Summen, darin springende Funkenbögen und Knistern."""
    r = rng(seed)
    dur = r.uniform(1.2, 1.5)
    y = np.zeros(int((dur + 0.6) * SR))
    n = int(dur * SR)
    t = np.arange(n) / SR
    hum_f = r.uniform(105.0, 125.0) * (1.0 + 0.02 * smooth_env(n, r.uniform(-1.0, 1.0, 12)))
    hum = tone(hum_f, curve(n, [(0.0, 0.0), (0.1, 1.0), (0.8, 0.8), (1.0, 0.0)]), SR,
               harmonics=tuple((k, 1.0 / k) for k in range(1, 18)))
    hum = hum * (0.65 + 0.35 * np.abs(np.sin(2 * np.pi * 50.0 * t)))
    mix_at(y, filt(hum, "band", [180.0, 4200.0], SR, 2), 0.0, 0.28)
    mix_at(y, thud(0.6, 70.0, 38.0, seed, noise_hi=400.0, grit=0.8), 0.0, 0.6)
    mix_at(y, crackle(dur, seed + 1, 420.0, lo=2000.0, hi=9000.0, env=[(0.0, 1.0), (0.4, 0.7), (1.0, 0.0)]), 0.0, 0.6)
    for k in range(int(r.integers(9, 15))):
        mix_at(y, zap(r.uniform(3500.0, 6500.0), r.uniform(300.0, 700.0), r.uniform(0.04, 0.09), seed + 10 + k),
               r.uniform(0.0, dur * 0.9), r.uniform(0.35, 0.7))
    sizzle = swept(dur, [(0.0, 5000.0), (1.0, 3500.0)], 1.0, [(0.0, 0.0), (0.1, 1.0), (1.0, 0.0)], seed + 2, flicker=0.6,
                   flicker_rate=(40.0, 60.0))
    mix_at(y, sizzle, 0.0, 0.35)
    return y


def breath_nature(seed: int) -> np.ndarray:
    """Natur: eine warme Böe voller raschelnder Blätter, darin leise Pollen wie Glöckchen."""
    r = rng(seed)
    dur = r.uniform(1.4, 1.7)
    y = np.zeros(int((dur + 0.6) * SR))
    mix_at(y, gust(dur, [(0.0, 700.0), (0.4, 1400.0), (1.0, 900.0)], 1.6, [(0.0, 0.0), (0.18, 1.0), (0.8, 0.7), (1.0, 0.0)],
                   seed, flicker=0.35), 0.0, 0.8)
    leaves = crackle(dur, seed + 1, 900.0, lo=1800.0, hi=7000.0, env=[(0.0, 0.0), (0.2, 1.0), (0.8, 0.8), (1.0, 0.0)])
    n = len(leaves)
    leaves = leaves * (0.6 + 0.4 * smooth_env(n, r.uniform(0.0, 1.0, 24)))
    mix_at(y, leaves, 0.0, 0.35)
    mix_at(y, sparkle(dur, seed + 2, 9.0, 1800.0, 3600.0, decay=(0.15, 0.4), env=[(0.0, 0.0), (0.4, 1.0), (1.0, 0.5)]), 0.1, 0.2)
    mix_at(y, rumble(dur, seed + 3, 160.0, [(0.0, 0.0), (0.3, 1.0), (1.0, 0.0)]), 0.0, 0.2)
    return y


def breath_earth(seed: int) -> np.ndarray:
    """Erde: ein tiefes Grollen, Kies und Sand prasseln, dazwischen schlagen Felsbrocken auf."""
    r = rng(seed)
    dur = r.uniform(1.4, 1.7)
    y = np.zeros(int((dur + 0.6) * SR))
    mix_at(y, rumble(dur, seed, 170.0, [(0.0, 0.0), (0.12, 1.0), (0.8, 0.8), (1.0, 0.0)]), 0.0, 0.45)
    mix_at(y, crackle(dur, seed + 1, 620.0, lo=400.0, hi=2600.0, env=[(0.0, 0.2), (0.2, 1.0), (0.85, 0.8), (1.0, 0.0)]), 0.0, 0.9)
    mix_at(y, gust(dur, [(0.0, 650.0), (1.0, 380.0)], 2.0, [(0.0, 0.0), (0.15, 1.0), (1.0, 0.0)], seed + 2, flicker=0.3), 0.0, 0.7)
    for k in range(int(r.integers(3, 6))):
        mix_at(y, thud(0.3, r.uniform(80.0, 120.0), r.uniform(45.0, 60.0), seed + 10 + k, noise_hi=600.0, grit=0.9),
               r.uniform(0.15, dur * 0.85), r.uniform(0.3, 0.55))
    return y


def breath_water(seed: int) -> np.ndarray:
    """Wasser: ein rauschender Strahl unter Druck, Blasen und sprühende Gischt."""
    r = rng(seed)
    dur = r.uniform(1.4, 1.7)
    y = np.zeros(int((dur + 0.6) * SR))
    jet = swept(dur, [(0.0, 1600.0), (0.4, 1200.0), (1.0, 900.0)], 2.0, [(0.0, 0.0), (0.1, 1.0), (0.8, 0.85), (1.0, 0.0)],
                seed, flicker=0.35, flicker_rate=(14.0, 24.0))
    mix_at(y, jet, 0.0, 0.9)
    mix_at(y, bubbles(dur, seed + 1, 45.0, 280.0, 1300.0, env=[(0.0, 0.3), (0.3, 1.0), (1.0, 0.4)]), 0.05, 0.4)
    spray = swept(dur, [(0.0, 4200.0), (1.0, 3600.0)], 1.2, [(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)], seed + 2, flicker=0.5,
                  flicker_rate=(30.0, 50.0))
    mix_at(y, spray, 0.08, 0.3)
    mix_at(y, rumble(dur, seed + 3, 140.0, [(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)]), 0.0, 0.3)
    return y


# =====================================================================================
# Das Ei
# =====================================================================================

def shell_crack(seed: int, size: float = 1.0) -> np.ndarray:
    r = rng(seed)
    y = np.zeros(int(0.3 * SR))
    modes = [(r.uniform(2400.0, 2900.0) / size, 1.0), (r.uniform(3900.0, 4500.0) / size, 0.7), (r.uniform(5600.0, 6400.0) / size, 0.4)]
    mix_at(y, clack(modes, 0.008 * size, seed, q=7.0), 0.0, 1.0)
    mix_at(y, crackle(0.14 * size, seed + 1, 700.0, lo=2000.0, hi=8000.0, env=[(0.0, 1.0), (1.0, 0.0)]), 0.004, 0.35)
    return y


def egg_crack(seed: int) -> np.ndarray:
    """Die Schale knackt: ein, zwei helle Risse und leises Bröseln (manchmal piepst es drinnen)."""
    r = rng(seed)
    y = np.zeros(int(0.7 * SR))
    at = 0.0
    for k in range(int(r.integers(1, 3))):
        mix_at(y, shell_crack(seed + k), at, 1.0 - 0.3 * k)
        at += r.uniform(0.06, 0.12)
    if r.uniform() < 0.4:
        mix_at(y, baby_chirp(seed + 9, 0.09, r.uniform(1050.0, 1300.0)), at + 0.08, 0.25)
    return y


def egg_hatch(seed: int) -> np.ndarray:
    """Das Schlüpfen: Risse in immer schnellerer Folge, die Schale bricht auf – und das erste Zirpen."""
    r = rng(seed)
    y = np.zeros(int(1.8 * SR))
    at = 0.0
    gap = 0.22
    for k in range(5):
        mix_at(y, shell_crack(seed + k, 1.0 + 0.1 * k), at, 0.7 + 0.06 * k)
        at += gap
        gap *= 0.72
    mix_at(y, gust(0.18, [(0.0, 3000.0), (1.0, 1800.0)], 1.2, [(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)], seed + 20), at, 0.3)
    mix_at(y, crackle(0.35, seed + 21, 900.0, lo=1500.0, hi=7000.0, env=[(0.0, 1.0), (1.0, 0.0)]), at, 0.45)
    mix_at(y, baby_chirp(seed + 30, 0.18, 820.0), at + 0.35, 0.8)
    mix_at(y, baby_chirp(seed + 31, 0.22, 900.0), at + 0.62, 0.7)
    return y[:int((at + 1.0) * SR)]


# =====================================================================================
# Ausgabe
# =====================================================================================

# Name: (Erzeuger, Varianten, Raum (Nachhall s, Anteil) oder None, Lautheit in LUFS)
SYNTH = {
    "ambient": (dragon_ambient, 4, (0.9, 0.12), -19.0),
    "baby_ambient": (dragon_baby_ambient, 4, (0.5, 0.1), -19.0),
    "hurt": (dragon_hurt, 4, (0.8, 0.12), -15.0),
    "death": (dragon_death, 2, (1.6, 0.2), -14.0),
    "roar": (dragon_roar, 4, (1.8, 0.2), -13.0),
    "grow": (dragon_grow, 1, (1.2, 0.2), -16.0),
    "flap": (dragon_flap, 4, (0.5, 0.08), -18.0),
    "step": (dragon_step, 4, None, -20.0),
    "bite": (dragon_bite, 3, (0.5, 0.08), -16.0),
    "eat": (dragon_eat, 3, None, -19.0),
    "breath_fire": (breath_fire, 3, (0.9, 0.12), -15.0),
    "breath_ice": (breath_ice, 3, (0.9, 0.12), -16.0),
    "breath_lightning": (breath_lightning, 3, (0.9, 0.12), -15.5),
    "breath_nature": (breath_nature, 3, (0.9, 0.12), -16.0),
    "breath_earth": (breath_earth, 3, (0.9, 0.12), -15.5),
    "breath_water": (breath_water, 3, (0.9, 0.12), -16.0),
    "egg_crack": (egg_crack, 4, None, -21.0),
    "egg_hatch": (egg_hatch, 2, (0.5, 0.1), -17.0),
}

# Ereignis: (Klänge, Reichweite in Blöcken)
EVENTS = {
    "entity.dragon.ambient": (["ambient"], 32),
    "entity.dragon.baby_ambient": (["baby_ambient"], 16),
    "entity.dragon.hurt": (["hurt"], 32),
    "entity.dragon.death": (["death"], 64),
    "entity.dragon.roar": (["roar"], 96),
    "entity.dragon.grow": (["grow"], 24),
    "entity.dragon.flap": (["flap"], 48),
    "entity.dragon.step": (["step"], 24),
    "entity.dragon.bite": (["bite"], 24),
    "entity.dragon.eat": (["eat"], 16),
    "entity.dragon.breath.fire": (["breath_fire"], 48),
    "entity.dragon.breath.ice": (["breath_ice"], 48),
    "entity.dragon.breath.lightning": (["breath_lightning"], 48),
    "entity.dragon.breath.nature": (["breath_nature"], 48),
    "entity.dragon.breath.earth": (["breath_earth"], 48),
    "entity.dragon.breath.water": (["breath_water"], 48),
    "block.dragon_egg.crack": (["egg_crack"], 16),
    "block.dragon_egg.hatch": (["egg_hatch"], 24),
}


def fit_lufs(x: np.ndarray, target: float) -> np.ndarray:
    """Auf eine Lautheit (LUFS) bringen – tiefe Klänge klingen bei gleicher Energie leiser als helle."""
    import pyloudnorm
    meter = pyloudnorm.Meter(SR, block_size=min(0.4, max(0.05, len(x) / SR / 2)))
    loud = meter.integrated_loudness(x)
    if not np.isfinite(loud):
        return fit_level(x, target, -1.5)
    from dsp import limiter
    # bis zu 8 dB dürfen die Spitzen (Zähne, Aufprall) sanft begrenzt werden, mehr nicht
    gain = 10 ** ((target - loud) / 20)
    peak = float(np.max(np.abs(x))) * gain
    gain = min(gain, gain * 10 ** (-1.0 / 20) * 10 ** (8.0 / 20) / max(peak, 1e-12))
    return limiter(x * gain, SR, ceiling_db=-1.0, lookahead=0.003, release=0.05).astype(np.float64)


def files_of(name: str) -> list[str]:
    return [f"kingdomsatwar:entity/dragon/{name}{i + 1}" for i in range(SYNTH[name][1])]


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    for i in range(count):
        x = fn(7000 + i * 41 + zlib.crc32(name.encode()) % 997)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 1300 + i, stereo=False, damping=6000), space[1])
        x = filt(x, "high", 28.0, SR, 2)
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.05)
        x = fit_lufs(x, level)
        write_ogg(x.astype(np.float32), SR, OUT / f"{name}{i + 1}.ogg", quality=5.0)
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
    OUT.mkdir(parents=True, exist_ok=True)
    rendered = {name: render(name, args.preview) if not args.only or name in args.only else files_of(name) for name in SYNTH}
    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8")) if SOUNDS_JSON.exists() else {}
    for key, (sounds, reach) in EVENTS.items():
        entries = []
        for s in sounds:
            entries += [{"name": f, "attenuation_distance": reach} for f in rendered[s]]
        old = data.get(key, {})
        data[key] = {"sounds": entries, "subtitle": old.get("subtitle", f"subtitles.kingdomsatwar.{key}")}
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"{len(EVENTS)} Klangereignisse geschrieben")


if __name__ == "__main__":
    main()
