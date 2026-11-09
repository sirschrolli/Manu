#!/usr/bin/env python3
"""
Erzeugt die Klänge von Asteria, der Sternenreiterin: ihr Ruf (ein heller Hornruf über einem schwebenden Chor), das
Herabstoßen in der Lichtsäule, der Sturmritt, der Lanzentreffer, das Pfeifen fallender Sterne und ihr kristallenes
Zerspringen, das Glockenspiel der Sternbilder, Wurf und Einschlag des Kometen, der Schwingenschlag des Rochens, das
Anschwellen und Losbrechen der Supernova, das Summen des Rochens, Schmerz und Tod, das Schwarze Loch (ein saugender,
wirbelnder Sog über einem tiefen Grollen), sein Zusammensturz und das helle Zischen der Mondsicheln – dazu der
Sternenschub der Sternenschwingen. Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py); es werden keine Minecraft-Dateien
kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/star_sounds.py [--only name ...] [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/star/<name><n>.ogg
    Einträge "entity.asteria.*" und "item.star_wings.boost" in assets/kingdomsatwar/sounds.json
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
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/star"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100
# eine helle, schwebende Tonleiter (Pentatonik auf E)
PENTA = [659.3, 740.0, 830.6, 987.8, 1108.7, 1318.5, 1480.0, 1661.2, 1975.5]


def mix_at(y: np.ndarray, x: np.ndarray, start: float, gain: float = 1.0) -> None:
    s = int(start * SR)
    m = max(0, min(len(x), len(y) - s))
    y[s:s + m] += x[:m] * gain


def noise(n: int, seed: int) -> np.ndarray:
    return rng(seed).standard_normal(n)


# =====================================================================================
# Bausteine
# =====================================================================================

def chime(f0: float, dur: float, seed: int, decay: float = 1.4) -> np.ndarray:
    """Ein heller Glockenton (wie ein Kristall angeschlagen): fast harmonische, lang schwingende Teiltöne."""
    return bell(f0, dur, SR, ratios=(1.0, 2.01, 3.02, 4.17, 5.43), amps=(1.0, 0.35, 0.18, 0.08, 0.04), decay=decay, seed=seed)


def crystal(f0: float, dur: float, seed: int, decay: float = 0.5) -> np.ndarray:
    """Splitterndes Glas: unharmonische, sehr helle Teiltöne, rasch verklingend."""
    return bell(f0, dur, SR, ratios=(1.0, 1.47, 2.31, 3.17, 4.53, 6.1), amps=(1.0, 0.8, 0.6, 0.4, 0.25, 0.15), decay=decay, seed=seed)


def shimmer(n: int, seed: int, density: float = 60.0, lo: float = 4000.0, hi: float = 12000.0) -> np.ndarray:
    """Glitzern: viele winzige, helle Funken (gefilterte Knackser mit Nachklang)."""
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
    """Ein Luftzug: Rauschen durch ein wanderndes Bandpassfilter (in Stücken), dazu die Hüllkurve."""
    x = noise(n, seed)
    out = np.zeros(n)
    steps = 24
    for i in range(steps):
        a, b = i * n // steps, (i + 1) * n // steps
        f = f0 * (f1 / f0) ** (i / (steps - 1))
        seg = filt(x[max(0, a - 512):b], "band", [max(40.0, f / (1.0 + q)), min(18000.0, f * (1.0 + q))], SR, 2)
        out[a:b] = seg[-(b - a):]
    return out


def choir(n: int, freqs: list[float], seed: int, vowel: str = "a") -> np.ndarray:
    """Ein schwebender Chor: leicht verstimmte Sägezähne mit Vibrato durch Vokalformanten."""
    r = rng(seed)
    t = np.arange(n) / SR
    y = np.zeros(n)
    for k, f in enumerate(freqs):
        for d in (-1, 1):
            vib = 1.0 + 0.006 * np.sin(2 * np.pi * r.uniform(4.6, 5.6) * t + r.uniform(0, 6.28)) + d * 0.003
            y += saw(np.full(n, f) * vib, np.ones(n), SR, partials=18, rolloff=1.3) * (0.6 if k else 1.0)
    formants = {"a": [(800, 120, 1.0), (1150, 160, 0.6), (2900, 300, 0.25)],
                "o": [(450, 100, 1.0), (800, 140, 0.5), (2830, 300, 0.12)],
                "e": [(400, 90, 1.0), (2000, 200, 0.5), (2800, 300, 0.25)]}[vowel]
    return formant_filter(y, SR, formants) * 0.5 + filt(y, "low", 2500, SR, 2) * 0.08


def horn(n: int, f0: float, f1: float, seed: int) -> np.ndarray:
    """Ein heller Hornruf: Sägezahn mit Blechbläser-Formanten, sanftem Einsatz und leichtem Vibrato."""
    t = np.arange(n) / SR
    f = glide(n, f0, f1, 0.5) * (1.0 + 0.008 * np.sin(2 * np.pi * 5.2 * t) * np.clip(t / 0.4, 0, 1))
    bright = np.clip(t / 0.25, 0, 1)
    y = saw(f, np.ones(n), SR, partials=30, rolloff=1.05)
    y = formant_filter(y, SR, [(700, 200, 1.0), (1300, 260, 0.8), (2500, 400, 0.4 * float(bright.mean()))])
    return y * env_asr(n, SR, 0.06, 0.35)


def hum(n: int, f0: float, seed: int) -> np.ndarray:
    """Das tiefe Summen des Rochens: langsam schwebende Sinustöne."""
    t = np.arange(n) / SR
    r = rng(seed)
    f = f0 * (1.0 + 0.03 * np.sin(2 * np.pi * r.uniform(0.3, 0.6) * t))
    return tone(f, np.ones(n), SR, harmonics=((1, 1.0), (2, 0.35), (3, 0.12), (5, 0.04)))


def thud(n: int, f0: float, f1: float, decay: float) -> np.ndarray:
    return tone(glide(n, f0, f1, 0.4), env_ad(n, SR, 0.002, decay), SR, harmonics=((1, 1.0), (2, 0.25), (3, 0.06)))


def impact(n: int, seed: int, lo: float, hi: float, decay: float) -> np.ndarray:
    return filt(noise(n, seed), "band", [lo, hi], SR, 2) * env_ad(n, SR, 0.0008, decay)


# =====================================================================================
# Klänge
# =====================================================================================

def cry(seed: int) -> np.ndarray:
    """Ihr Ruf: ein heller Hornruf in zwei Stufen über einem schwebenden Chor, darüber Glitzern."""
    r = rng(seed)
    n = int(2.8 * SR)
    y = np.zeros(n)
    base = r.uniform(392, 440)
    h1 = horn(int(0.7 * SR), base, base * 1.02, seed)
    h2 = horn(int(1.4 * SR), base * 1.5, base * 1.5, seed + 1) * curve(int(1.4 * SR), [(0, 0.0), (0.08, 1.0), (0.7, 0.85), (1, 0.0)])
    mix_at(y, h1, 0.0, 0.6)
    mix_at(y, h2, 0.55, 0.7)
    m = int(2.4 * SR)
    mix_at(y, choir(m, [base * 2, base * 3, base * 2.5], seed + 2, "a") * env_asr(m, SR, 0.4, 1.0), 0.25, 0.35)
    mix_at(y, shimmer(n, seed + 3, 50) * env_asr(n, SR, 0.4, 1.2), 0.0, 0.3)
    return y


def descent(seed: int) -> np.ndarray:
    """Herabstoßen: ein langer, schimmernder Luftzug von oben herab, ein anschwellender Chor, Glocken."""
    n = int(4.0 * SR)
    y = np.zeros(n)
    w = whoosh(n, seed, 6000, 500, 0.6) * curve(n, [(0, 0.0), (0.2, 0.6), (0.7, 1.0), (1, 0.0)])
    mix_at(y, w, 0.0, 0.5)
    mix_at(y, choir(n, [330.0, 495.0, 660.0], seed + 1, "o") * curve(n, [(0, 0.0), (0.5, 0.7), (0.85, 1.0), (1, 0.0)]), 0.0, 0.3)
    for k, f in enumerate((PENTA[8], PENTA[6], PENTA[5], PENTA[3], PENTA[1])):
        mix_at(y, chime(f, 2.0, seed + 10 + k, 1.2), 0.4 + k * 0.45, 0.22)
    mix_at(y, shimmer(n, seed + 3, 80) * curve(n, [(0, 0.2), (0.6, 1.0), (1, 0.0)]), 0.0, 0.35)
    return y


def charge(seed: int) -> np.ndarray:
    """Sturmritt: ein kurzer Hornstoß, dann ein mächtiger, schneller werdender Luftzug voller Funken."""
    r = rng(seed)
    n = int(2.0 * SR)
    y = np.zeros(n)
    base = r.uniform(420, 470)
    mix_at(y, horn(int(0.5 * SR), base, base * 1.06, seed), 0.0, 0.5)
    w = whoosh(int(1.6 * SR), seed + 1, 300, 3000, 0.8) * curve(int(1.6 * SR), [(0, 0.0), (0.5, 0.8), (0.85, 1.0), (1, 0.0)])
    mix_at(y, w, 0.25, 0.8)
    mix_at(y, shimmer(n, seed + 2, 70), 0.2, 0.3)
    return y


def lance_hit(seed: int) -> np.ndarray:
    """Lanzentreffer: ein harter Stoß, Metall klirrt, ein heller Kristallton."""
    r = rng(seed)
    n = int(1.1 * SR)
    y = np.zeros(n)
    mix_at(y, impact(int(0.1 * SR), seed, 800, 7000, 0.02), 0.0, 1.0)
    mix_at(y, thud(int(0.3 * SR), 160, 70, 0.06), 0.0, 0.7)
    mix_at(y, crystal(r.uniform(1800, 2300), 0.8, seed + 1, 0.35), 0.0, 0.25)
    return y


def starfall(seed: int) -> np.ndarray:
    """Fallende Sterne: mehrere Pfeiftöne, die hoch beginnen und nach unten gleiten, mit rauschendem Schweif."""
    r = rng(seed)
    n = int(2.4 * SR)
    y = np.zeros(n)
    for k in range(4):
        m = int(r.uniform(1.0, 1.5) * SR)
        t = np.arange(m) / SR
        f = glide(m, r.uniform(2600, 3400), r.uniform(700, 1100), 0.7) * (1.0 + 0.01 * np.sin(2 * np.pi * 9.0 * t))
        whistle = tone(f, curve(m, [(0, 0.0), (0.1, 1.0), (0.85, 0.7), (1, 0.0)]), SR, harmonics=((1, 1.0), (2, 0.1)))
        tail = whoosh(m, seed + k, 4000, 1200, 0.5) * curve(m, [(0, 0.0), (0.2, 0.5), (1, 0.0)])
        mix_at(y, whistle * 0.35 + tail * 0.5, k * 0.25, 0.8)
    return y


def star_impact(seed: int) -> np.ndarray:
    """Ein Stern schlägt ein: ein dumpfer Knall und splitterndes Kristallglas, darüber Glitzern."""
    r = rng(seed)
    n = int(1.6 * SR)
    y = np.zeros(n)
    mix_at(y, thud(int(0.5 * SR), 120, 45, 0.12), 0.0, 0.9)
    mix_at(y, impact(int(0.2 * SR), seed, 600, 9000, 0.04), 0.0, 0.8)
    for k in range(3):
        mix_at(y, crystal(r.uniform(1400, 3200), 1.0, seed + 1 + k, 0.4), 0.0 + k * 0.03, 0.18)
    mix_at(y, shimmer(n, seed + 7, 90) * env_ad(n, SR, 0.005, 0.5), 0.0, 0.4)
    return y


def constellation(seed: int) -> np.ndarray:
    """Ein Sternbild entsteht: ein Glockenspiel, das Ton für Ton aufsteigt und lange nachklingt."""
    r = rng(seed)
    n = int(3.2 * SR)
    y = np.zeros(n)
    start = r.integers(0, 3)
    for k in range(5):
        f = PENTA[min(len(PENTA) - 1, start + k * 1 + (1 if k > 2 else 0))]
        mix_at(y, chime(f, 2.4, seed + k, 1.6), k * 0.16, 0.3)
        mix_at(y, chime(f * 2.0, 1.2, seed + 10 + k, 0.8), k * 0.16 + 0.01, 0.08)
    mix_at(y, shimmer(n, seed + 20, 30) * env_asr(n, SR, 0.3, 1.5), 0.0, 0.2)
    return y


def comet_throw(seed: int) -> np.ndarray:
    """Komet: ein kurzes Ausholen, dann ein heller, steigender Luftzug, dem ein Funkenschweif folgt."""
    n = int(1.4 * SR)
    y = np.zeros(n)
    w = whoosh(int(1.1 * SR), seed, 600, 5000, 0.7) * curve(int(1.1 * SR), [(0, 0.0), (0.3, 1.0), (1, 0.0)])
    mix_at(y, w, 0.05, 0.8)
    mix_at(y, shimmer(n, seed + 1, 90) * curve(n, [(0, 0.0), (0.3, 1.0), (1, 0.0)]), 0.1, 0.4)
    m = int(0.9 * SR)
    mix_at(y, tone(glide(m, 900, 1800, 0.5), env_ad(m, SR, 0.05, 0.4), SR), 0.08, 0.12)
    return y


def comet_impact(seed: int) -> np.ndarray:
    """Der Komet zerplatzt: ein tiefer Schlag, eine Welle aus Licht (Chorakkord) und zerspringendes Kristall."""
    r = rng(seed)
    n = int(2.6 * SR)
    y = np.zeros(n)
    mix_at(y, thud(int(0.9 * SR), 90, 32, 0.25), 0.0, 1.0)
    mix_at(y, impact(int(0.3 * SR), seed, 300, 6000, 0.08), 0.0, 0.9)
    for k in range(4):
        mix_at(y, crystal(r.uniform(1200, 3000), 1.4, seed + 1 + k, 0.5), k * 0.02, 0.14)
    m = int(2.0 * SR)
    mix_at(y, choir(m, [523.3, 784.0, 1046.5], seed + 9, "a") * env_ad(m, SR, 0.02, 1.0), 0.03, 0.25)
    mix_at(y, shimmer(n, seed + 11, 100) * env_ad(n, SR, 0.01, 0.9), 0.0, 0.4)
    return y


def wingbeat(seed: int) -> np.ndarray:
    """Der Rochen schlägt die Schwingen: ein tiefes, weiches Wummern und ein mächtiger Luftstoß."""
    n = int(1.6 * SR)
    y = np.zeros(n)
    m = int(0.6 * SR)
    whomp = filt(noise(m, seed), "low", 260, SR, 2) * env_ad(m, SR, 0.03, 0.22)
    mix_at(y, whomp, 0.0, 2.2)
    mix_at(y, thud(int(0.5 * SR), 70, 40, 0.15), 0.02, 0.6)
    w = whoosh(int(1.2 * SR), seed + 1, 2000, 300, 0.9) * env_ad(int(1.2 * SR), SR, 0.04, 0.5)
    mix_at(y, w, 0.05, 0.7)
    return y


def supernova_charge(seed: int) -> np.ndarray:
    """Das Licht sammelt sich: ein Brummen, das über drei Sekunden steigt, ein wachsender Chor, immer dichteres Glitzern."""
    n = int(3.4 * SR)
    y = np.zeros(n)
    rise = curve(n, [(0, 0.0), (0.2, 0.3), (0.9, 1.0), (1, 0.6)])
    mix_at(y, tone(glide(n, 90, 420, 1.4), rise, SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.25), (4, 0.12))), 0.0, 0.35)
    mix_at(y, choir(n, [220.0, 330.0, 440.0, 660.0], seed, "o") * rise, 0.0, 0.3)
    mix_at(y, shimmer(n, seed + 1, 160) * rise ** 2, 0.0, 0.5)
    mix_at(y, whoosh(n, seed + 2, 200, 6000, 0.6) * rise * 0.6, 0.0, 0.4)
    return y


def supernova(seed: int) -> np.ndarray:
    """Supernova: ein gewaltiger Knall, ein strahlender Akkord, der langsam verhallt, und ein Regen aus Funken."""
    n = int(5.0 * SR)
    y = np.zeros(n)
    mix_at(y, thud(int(1.6 * SR), 70, 24, 0.5), 0.0, 1.2)
    mix_at(y, impact(int(0.6 * SR), seed, 100, 9000, 0.2), 0.0, 1.0)
    m = int(4.2 * SR)
    mix_at(y, choir(m, [329.6, 493.9, 659.3, 987.8], seed + 1, "a") * curve(m, [(0, 0.0), (0.03, 1.0), (0.4, 0.6), (1, 0.0)]), 0.02, 0.45)
    for k, f in enumerate((PENTA[0], PENTA[3], PENTA[5], PENTA[8])):
        mix_at(y, chime(f, 3.5, seed + 5 + k, 2.0), 0.02, 0.18)
    mix_at(y, shimmer(n, seed + 9, 200) * curve(n, [(0, 1.0), (0.6, 0.4), (1, 0.0)]), 0.0, 0.5)
    return y


def ambient(seed: int) -> np.ndarray:
    """Das Summen des Rochens, dazu ein, zwei leise Glockentöne."""
    r = rng(seed)
    n = int(2.6 * SR)
    y = np.zeros(n)
    mix_at(y, hum(n, r.uniform(58, 72), seed) * curve(n, [(0, 0.0), (0.3, 1.0), (0.7, 1.0), (1, 0.0)]), 0.0, 0.5)
    for k in range(2):
        mix_at(y, chime(PENTA[int(r.integers(2, 8))], 1.6, seed + k, 1.2), 0.4 + k * 0.7, 0.12)
    return y


def hurt(seed: int) -> np.ndarray:
    """Schmerz: die Rüstung klirrt, der Rochen stößt einen kurzen, klagenden Laut aus."""
    r = rng(seed)
    n = int(0.9 * SR)
    y = np.zeros(n)
    mix_at(y, impact(int(0.08 * SR), seed, 1500, 9000, 0.015), 0.0, 0.8)
    mix_at(y, crystal(r.uniform(900, 1300), 0.6, seed + 1, 0.25), 0.0, 0.2)
    m = int(0.6 * SR)
    wail = tone(glide(m, r.uniform(520, 600), r.uniform(380, 420), 0.8), curve(m, [(0, 0.0), (0.1, 1.0), (1, 0.0)]), SR,
                harmonics=((1, 1.0), (2, 0.3), (3, 0.1)))
    mix_at(y, wail, 0.04, 0.3)
    return y


def death(seed: int) -> np.ndarray:
    """Tod: ein absinkender Chorakkord, ein letzter Hornton, Glocken, die verklingen, und ein langes Glitzern."""
    n = int(5.0 * SR)
    y = np.zeros(n)
    m = int(4.4 * SR)
    t = np.arange(m) / SR
    fall = 1.0 - 0.12 * t / t[-1]
    c = choir(m, [440.0, 659.3, 880.0], seed, "o")
    mix_at(y, c * curve(m, [(0, 0.0), (0.1, 1.0), (0.6, 0.7), (1, 0.0)]), 0.0, 0.4)
    mix_at(y, tone(392.0 * fall, curve(m, [(0, 0.0), (0.05, 1.0), (0.5, 0.4), (1, 0.0)]), SR, harmonics=((1, 1.0), (2, 0.3))), 0.0, 0.12)
    mix_at(y, horn(int(1.2 * SR), 440, 392, seed + 1), 0.2, 0.3)
    for k, f in enumerate(reversed(PENTA[:6])):
        mix_at(y, chime(f, 2.0, seed + 3 + k, 1.4), 0.8 + k * 0.35, 0.15)
    mix_at(y, shimmer(n, seed + 9, 70) * curve(n, [(0, 0.5), (0.5, 1.0), (1, 0.0)]), 0.0, 0.35)
    return y


def boost(seed: int) -> np.ndarray:
    """Sternenschub: ein zischender Raketenstoß und ein Funkenregen."""
    n = int(1.3 * SR)
    y = np.zeros(n)
    w = whoosh(n, seed, 900, 4500, 0.9) * curve(n, [(0, 0.0), (0.06, 1.0), (0.4, 0.6), (1, 0.0)])
    mix_at(y, w, 0.0, 0.9)
    mix_at(y, shimmer(n, seed + 1, 110) * env_ad(n, SR, 0.02, 0.6), 0.0, 0.5)
    m = int(0.5 * SR)
    mix_at(y, thud(m, 140, 70, 0.1), 0.0, 0.4)
    return y


# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil) oder None, Ziel-Pegel dBFS)
def black_hole(seed: int) -> np.ndarray:
    """Schwarzes Loch: ein tiefes, schwebendes Grollen, darüber ein Sog, der immer schneller wirbelt (Rauschen durch ein
    kreisendes Filter), ein dunkler Chor, der absinkt, und Funken, die hineinstürzen."""
    n = int(3.8 * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    grow = curve(n, [(0, 0.0), (0.12, 0.55), (0.85, 1.0), (1, 0.7)])
    # Grollen: zwei tiefe Töne, die gegeneinander schweben
    rumble = tone(glide(n, 38, 62, 1.3), grow, SR, harmonics=((1, 1.0), (2, 0.4), (3, 0.15))) \
        + tone(glide(n, 39.5, 64, 1.3), grow, SR, harmonics=((1, 0.8), (2, 0.3)))
    mix_at(y, rumble, 0.0, 0.5)
    # der Sog: ein Bandpass, dessen Mitte im Kreis läuft – das Kreisen wird schneller
    x = noise(n, seed)
    rate = 1.6 + 5.0 * (t / t[-1]) ** 1.4
    phase = np.cumsum(rate) / SR * 2 * np.pi
    center = 900 * 2.0 ** (1.4 * np.sin(phase))
    out = np.zeros(n)
    steps = 160
    for i in range(steps):
        a, b = i * n // steps, (i + 1) * n // steps
        f = float(center[(a + b) // 2])
        seg = filt(x[max(0, a - 512):b], "band", [f / 1.6, f * 1.6], SR, 2)
        out[a:b] = seg[-(b - a):]
    mix_at(y, out * grow, 0.0, 0.55)
    # ein dunkler Chor, der langsam absinkt
    m = int(3.6 * SR)
    ch = choir(m, [110.0, 164.8, 207.7], seed + 3, "o")
    ch = ch * curve(m, [(0, 0.0), (0.3, 0.7), (1, 1.0)])
    mix_at(y, filt(ch, "low", 1400, SR, 2), 0.25, 0.35)
    # Funken, die hineinstürzen: hohe Pfeiftöne, die nach unten gleiten
    r = rng(seed + 7)
    for k in range(9):
        d = r.uniform(0.35, 0.7)
        q = int(d * SR)
        f0 = r.uniform(2400, 4200)
        mix_at(y, tone(glide(q, f0, f0 * 0.35, 1.0), env_ad(q, SR, 0.02, d * 0.5), SR), r.uniform(0.5, 3.1), 0.05)
    mix_at(y, shimmer(n, seed + 9, 70) * grow ** 2, 0.0, 0.25)
    return y


def collapse(seed: int) -> np.ndarray:
    """Zusammensturz: alles wird in den Kern gesogen (ein steigendes Zischen, immer lauter) – dann ein gewaltiger Schlag,
    ein strahlender Akkord und zerspringendes Kristall."""
    r = rng(seed)
    n = int(3.2 * SR)
    y = np.zeros(n)
    hit = 0.42
    m = int(hit * SR)
    suck = whoosh(m, seed, 300, 7000, 0.7) * curve(m, [(0, 0.0), (0.7, 0.6), (0.97, 1.0), (1, 0.0)]) ** 1.5
    mix_at(y, suck, 0.0, 0.9)
    mix_at(y, tone(glide(m, 200, 1200, 2.0), curve(m, [(0, 0.0), (0.9, 0.6), (1, 0.0)]), SR), 0.0, 0.12)
    mix_at(y, thud(int(1.8 * SR), 64, 22, 0.55), hit, 1.3)
    mix_at(y, impact(int(0.6 * SR), seed + 1, 80, 8000, 0.22), hit, 1.0)
    for k in range(5):
        mix_at(y, crystal(r.uniform(900, 2600), 1.6, seed + 2 + k, 0.6), hit + k * 0.015, 0.13)
    c = int(2.6 * SR)
    mix_at(y, choir(c, [207.7, 311.1, 415.3, 622.3], seed + 8, "a") * curve(c, [(0, 0.0), (0.03, 1.0), (0.4, 0.5), (1, 0.0)]), hit, 0.35)
    mix_at(y, shimmer(n, seed + 9, 160) * curve(n, [(0, 0.0), (hit / 3.2, 0.0), (hit / 3.2 + 0.02, 1.0), (1, 0.0)]), 0.0, 0.45)
    return y


def crescent(seed: int) -> np.ndarray:
    """Mondsichel: ein scharfes, helles Zischen (wie eine Klinge durch die Luft), darin ein klingender Kristallton."""
    n = int(1.3 * SR)
    y = np.zeros(n)
    m = int(0.75 * SR)
    swish = whoosh(m, seed, 1200, 7500, 0.5) * curve(m, [(0, 0.0), (0.12, 1.0), (0.35, 0.55), (1, 0.0)])
    mix_at(y, swish, 0.0, 0.9)
    mix_at(y, filt(noise(int(0.3 * SR), seed + 1), "band", [180, 900], SR, 2) * env_ad(int(0.3 * SR), SR, 0.03, 0.12), 0.0, 0.5)
    ring = chime(PENTA[5], 1.2, seed + 2, 1.6)
    mix_at(y, ring * curve(len(ring), [(0, 1.0), (0.7, 1.0), (1, 0.0)]), 0.04, 0.16)
    glass = crystal(PENTA[8] * 1.5, 0.9, seed + 3, 0.7)
    mix_at(y, glass * curve(len(glass), [(0, 1.0), (0.7, 1.0), (1, 0.0)]), 0.05, 0.08)
    mix_at(y, shimmer(n, seed + 4, 120) * env_ad(n, SR, 0.02, 0.35), 0.0, 0.3)
    return y


SYNTH = {
    "cry": (cry, 2, (2.6, 0.4), -14.0),
    "descent": (descent, 1, (3.0, 0.45), -15.0),
    "charge": (charge, 2, (1.8, 0.3), -14.0),
    "impact": (lance_hit, 3, (1.2, 0.25), -16.0),
    "starfall": (starfall, 2, (2.2, 0.35), -16.0),
    "star_impact": (star_impact, 3, (1.8, 0.3), -15.0),
    "constellation": (constellation, 2, (2.8, 0.45), -17.0),
    "comet_throw": (comet_throw, 2, (1.4, 0.25), -16.0),
    "comet_impact": (comet_impact, 2, (2.4, 0.4), -13.0),
    "wingbeat": (wingbeat, 2, (1.6, 0.3), -14.0),
    "supernova_charge": (supernova_charge, 1, (2.8, 0.4), -15.0),
    "supernova": (supernova, 1, (3.4, 0.5), -12.0),
    "ambient": (ambient, 3, (2.2, 0.4), -23.0),
    "hurt": (hurt, 3, (1.0, 0.2), -17.0),
    "death": (death, 1, (3.4, 0.5), -14.0),
    "boost": (boost, 2, (1.0, 0.2), -17.0),
    "black_hole": (black_hole, 1, (2.4, 0.35), -15.0),
    "collapse": (collapse, 2, (3.0, 0.45), -13.0),
    "crescent": (crescent, 3, (1.4, 0.3), -16.0),
}

# Ereignis: (Klänge, Reichweite)
EVENTS = {f"entity.asteria.{name}": ([name], reach) for name, reach in (
    ("cry", 96), ("descent", 96), ("charge", 64), ("impact", 32), ("starfall", 64), ("star_impact", 48), ("constellation", 48),
    ("comet_throw", 48), ("comet_impact", 64), ("wingbeat", 48), ("supernova_charge", 96), ("supernova", 128), ("ambient", 32),
    ("hurt", 32), ("death", 96), ("black_hole", 64), ("collapse", 80), ("crescent", 40))}
EVENTS["item.star_wings.boost"] = (["boost"], 24)


def files_of(name: str) -> list[str]:
    stem = "star_wings_boost" if name == "boost" else f"asteria_{name}"
    return [f"kingdomsatwar:star/{stem}{i + 1}" for i in range(SYNTH[name][1])]


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    files = []
    for i in range(count):
        x = fn(9000 + i * 43 + zlib.crc32(name.encode()) % 997)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 1700 + i, stereo=False, damping=7000), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.06)
        x = soft_clip(fit_level(x, level, -2.0))
        stem = "star_wings_boost" if name == "boost" else f"asteria_{name}"
        path = OUT / f"{stem}{i + 1}.ogg"
        write_ogg(x.astype(np.float32), SR, path, quality=5.0)
        files.append(f"kingdomsatwar:star/{stem}{i + 1}")
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
