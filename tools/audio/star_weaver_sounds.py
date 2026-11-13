#!/usr/bin/env python3
"""
Die Klänge der Sternenweberin (0.99): eine riesige Spinne aus Chitin und Sternenlicht. Wenn sie wacht, ein leises Klicken der
Mundwerkzeuge über einem fernen, gläsernen Summen (die Sterne in ihrem Netz); getroffen ein scharfes, knisterndes Zischen
mit Chitinsplittern; ihre acht Beine tippen auf die Fäden (ein hölzern-gläsernes Tappen, das im Netz nachschwingt); ihr
Erwachen (das Netz erzittert, ein anschwellendes Summen, ein Fauchen und ein Kranz heller Töne). Dazu die Angriffe: der
Faden (ein schnelles, hohes Sirren, das gespannt nachklingt), der Netzballen (ein feuchtes Spucken und ein Klatschen), das
Sternbild (Töne, die nacheinander aufleuchten und sich zu einem Akkord verbinden), der Sternenfall (fallendes Pfeifen und
gläserne Einschläge), der Kokon (Fäden, die sich schnell und eng umwickeln), die Brut (ein Gewimmel vieler kleiner Klicks),
der Fall von oben (ein Sausen und ein schwerer, federnder Aufprall ins Netz), der Biss (Klacken der Klauen, ein nasses
Zuschnappen), die Spindel (ein surrendes Kreisen, das schneller wird), ihr Zorn (ein schriller, zerrissener Schrei über
einem dissonanten Sternenakkord) und ihr Tod (ein sinkendes Zischen, die Töne erlöschen einer nach dem anderen). Die Brut,
die Sternspinnlinge, zirpt und klickt hell und klein.

Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py, keeper_sounds.py, sea_creatures.py); es werden keine
Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/star_weaver_sounds.py [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/void/star_weaver_<klang><n>.ogg
    Einträge "entity.star_weaver.<klang>" in assets/kingdomsatwar/sounds.json (die übrigen bleiben)
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, env_ad, env_asr, filt, fit_level, glide, rng, room, soft_clip, tone, trim_tail,  # noqa: E402
                          fade_edges)
from dsp import write_ogg  # noqa: E402
from keeper_sounds import chime, impact, thud  # noqa: E402
from kraken_sounds import moving_resonator  # noqa: E402
from sea_creatures import click, mix_at, soft_bell  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/void"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100

# die Tonleiter ihres Netzes: eine helle, schwebende Pentatonik (Sternenlicht)
STARS = (523.3, 587.3, 698.5, 784.0, 880.0, 1046.5, 1174.7, 1396.9, 1568.0, 1760.0)


# =====================================================================================
# Bausteine
# =====================================================================================

def chitter(r, dur: float, density: float, lo: float = 1800.0, hi: float = 4200.0, body: float = 0.0) -> np.ndarray:
    """Klickende Mundwerkzeuge: Gruppen harter Chitinklicks, mal dichter, mal lockerer."""
    n = int(dur * SR)
    y = np.zeros(n)
    t = 0.0
    while t < dur - 0.08:
        burst = r.integers(2, 6)
        f = r.uniform(lo, hi)
        for k in range(burst):
            mix_at(y, click(r, f * r.uniform(0.92, 1.08), r.uniform(12, 30), body) * r.uniform(0.4, 1.0), t + k * r.uniform(0.018, 0.035))
        t += burst * 0.03 + r.exponential(1.0 / density)
    return y


def shimmer(r, dur: float, count: int, gain: float = 1.0, low: int = 0, high: int = len(STARS)) -> np.ndarray:
    """Fernes Sternenleuchten: weiche Glastöne aus ihrer Tonleiter, verstreut."""
    n = int(dur * SR)
    y = np.zeros(n)
    for i in range(count):
        f = STARS[r.integers(low, high)] * r.choice((1.0, 2.0))
        mix_at(y, soft_bell(f, r.uniform(0.7, 1.4), r.uniform(0.3, 0.7), 51 + i), r.uniform(0, max(0.01, dur - 0.6)), r.uniform(0.2, 0.45) * gain)
    return y


def hum(dur: float, f0: float, f1: float, attack: float, release: float, wobble: float = 0.0) -> np.ndarray:
    """Ein gläsernes Summen (das Netz schwingt): reine Töne in Oktaven und Quinten, leicht schwebend."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = glide(n, f0, f1, 1.0) * (1.0 + wobble * np.sin(2 * np.pi * 5.2 * t))
    env = env_asr(n, SR, attack, release)
    y = tone(f, env, SR, harmonics=((1, 1.0), (2, 0.35), (3, 0.12), (4, 0.06)))
    y += tone(f * 1.5, env * 0.35, SR, harmonics=((1, 1.0), (2, 0.2)))
    y += tone(f * 1.003, env * 0.5, SR, harmonics=((1, 1.0),))      # Schwebung
    return y


def hiss(r, dur: float, lo: float, hi: float, attack: float, release: float) -> np.ndarray:
    """Ein Fauchen/Zischen: helles Rauschen mit Hüllkurve."""
    n = int(dur * SR)
    return filt(r.standard_normal(n), "band", [lo, hi], SR, 2) * env_asr(n, SR, attack, release)


def zip_thread(r, dur: float, f0: float, f1: float) -> np.ndarray:
    """Ein Faden schnellt hinaus: ein sirrendes Rauschen, dessen Resonanz rasch gleitet, dazu ein dünner, gespannter Ton."""
    n = int(dur * SR)
    sweep = glide(n, f0, f1, 0.7)
    y = moving_resonator(r.standard_normal(n), sweep, 9.0) * env_ad(n, SR, 0.004, dur * 0.45)
    y += tone(sweep * 1.5, env_ad(n, SR, 0.003, dur * 0.3) * 0.25, SR, harmonics=((1, 1.0), (2, 0.3)))
    return y


def twang(f0: float, dur: float, seed: int) -> np.ndarray:
    """Ein gespannter Faden schwingt nach: ein heller, fast harmonischer Ton mit leichtem Vibrato."""
    r = rng(seed)
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = f0 * (1.0 + 0.006 * np.sin(2 * np.pi * r.uniform(5, 8) * t))
    return tone(f, np.exp(-t / (dur * 0.35)) * env_ad(n, SR, 0.002), SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.3), (4, 0.15), (6, 0.06)))


def splat(r, dur: float, lo: float = 300.0, hi: float = 2600.0) -> np.ndarray:
    """Ein klebriges Klatschen: ein kurzer, feuchter Rauschstoß mit tiefem Körper."""
    n = int(dur * SR)
    y = filt(r.standard_normal(n), "band", [lo, hi], SR, 2) * env_ad(n, SR, 0.002, dur * 0.25)
    y += thud(n, 140.0, 70.0, dur * 0.3) * 0.6
    return y


# =====================================================================================
# Die Weberin
# =====================================================================================

def ambient(seed: int) -> np.ndarray:
    """Sie wacht: leises Klicken der Mundwerkzeuge, darunter das ferne Summen der Sterne in ihrem Netz."""
    r = rng(seed)
    dur = 3.2
    y = np.zeros(int(dur * SR))
    mix_at(y, chitter(r, dur, 2.2, 1600, 3600, 0.4), 0.0, 0.55)
    mix_at(y, hum(dur, r.choice((261.6, 293.7)), r.choice((261.6, 293.7)), 0.8, 1.2, 0.002), 0.0, 0.12)
    mix_at(y, shimmer(r, dur, 3, 0.6), 0.0)
    return y


def hurt(seed: int) -> np.ndarray:
    """Getroffen: ein scharfes, knisterndes Zischen, splitterndes Chitin, ein kurzer, schiefer Ton."""
    r = rng(seed)
    dur = 0.9
    n = int(dur * SR)
    y = np.zeros(n)
    mix_at(y, hiss(r, 0.6, 2500, 9000, 0.01, 0.45), 0.0, 0.8)
    mix_at(y, chitter(r, 0.45, 18.0, 2800, 5200), 0.0, 0.7)
    mix_at(y, impact(int(0.3 * SR), seed, 900, 6000, 0.05), 0.0, 0.5)
    f = r.choice(STARS[3:7])
    bent = tone(glide(int(0.5 * SR), f, f * 0.84, 1.0), env_ad(int(0.5 * SR), SR, 0.004, 0.2), SR, harmonics=((1, 1.0), (2, 0.4), (3, 0.2)))
    mix_at(y, bent, 0.02, 0.35)
    return y


def death(seed: int) -> np.ndarray:
    """Ihr Tod: ein langes, sinkendes Zischen, die Beine scharren ein letztes Mal, dann erlöschen die Sterne einer nach dem anderen."""
    r = rng(seed)
    dur = 4.6
    n = int(dur * SR)
    y = np.zeros(n)
    sweep = glide(int(2.6 * SR), 5200.0, 700.0, 1.4)
    mix_at(y, moving_resonator(r.standard_normal(int(2.6 * SR)), sweep, 5.0) * env_asr(int(2.6 * SR), SR, 0.05, 1.6), 0.0, 0.8)
    mix_at(y, chitter(r, 1.6, 9.0, 1400, 3600, 0.6), 0.1, 0.6)
    mix_at(y, hum(2.4, 220.0, 110.0, 0.1, 2.0, 0.006), 0.0, 0.25)
    for i, f in enumerate(STARS[::-1][:7]):
        mix_at(y, soft_bell(f, 1.2, 0.5, 70 + i), 1.4 + i * 0.36, 0.45 - i * 0.04)
    return y


def step(seed: int) -> np.ndarray:
    """Acht Beine tippen auf die Fäden: helles, hölzern-gläsernes Tappen, ein Faden schwingt nach."""
    r = rng(seed)
    dur = 0.7
    y = np.zeros(int(dur * SR))
    for k in range(r.integers(3, 5)):
        mix_at(y, click(r, r.uniform(900, 1500), r.uniform(6, 12), 0.8), k * r.uniform(0.07, 0.11), r.uniform(0.6, 1.0))
    mix_at(y, twang(r.uniform(180, 260), 0.6, seed + 3), 0.0, 0.18)
    return y


def awaken(seed: int) -> np.ndarray:
    """Ihr Erwachen: das Netz erzittert, ein Summen schwillt an, sie faucht – und ein Kranz heller Töne leuchtet auf."""
    r = rng(seed)
    dur = 4.4
    n = int(dur * SR)
    y = np.zeros(n)
    mix_at(y, hum(3.2, 110.0, 220.0, 2.2, 0.9, 0.004), 0.0, 0.45)
    for k in range(10):
        mix_at(y, twang(r.uniform(140, 320), 0.9, seed + k), 0.15 * k, 0.12)
    mix_at(y, chitter(r, 1.4, 14.0, 1600, 4200, 0.5), 1.6, 0.7)
    mix_at(y, hiss(r, 1.4, 1500, 8000, 0.25, 0.9), 2.5, 0.85)
    for i, f in enumerate(STARS[2:8]):
        mix_at(y, soft_bell(f * 2, 1.5, 0.6, 90 + i), 2.7 + i * 0.09, 0.35)
    return y


def fury(seed: int) -> np.ndarray:
    """Ihr Zorn: ein schriller, zerrissener Schrei über einem dissonanten Sternenakkord, das Netz bebt."""
    r = rng(seed)
    dur = 3.4
    n = int(dur * SR)
    y = np.zeros(n)
    sweep = glide(int(1.8 * SR), 2600.0, 4800.0, 0.8)
    scream = moving_resonator(r.standard_normal(int(1.8 * SR)), sweep, 7.0) * env_asr(int(1.8 * SR), SR, 0.08, 0.9)
    t = np.arange(int(1.8 * SR)) / SR
    scream *= 1.0 - 0.5 * (1.0 + np.sin(2 * np.pi * 38.0 * t))      # zerrissen
    mix_at(y, scream, 0.1, 0.9)
    m = int(2.8 * SR)
    chord = sum(tone(np.full(m, f), env_asr(m, SR, 0.2, 1.6) * 0.3, SR, harmonics=((1, 1.0), (2, 0.3), (3, 0.1)))
                for f in (440.0, 466.2, 659.3, 698.5))                # schräg: kleine Sekunden
    mix_at(y, chord, 0.3, 0.55)
    mix_at(y, chitter(r, 1.2, 20.0, 2000, 5000), 0.0, 0.5)
    mix_at(y, thud(int(0.8 * SR), 90.0, 45.0, 0.3), 0.0, 0.5)
    return y


# =====================================================================================
# Angriffe
# =====================================================================================

def thread(seed: int) -> np.ndarray:
    """Der Faden: ein schnelles, hohes Sirren, das gespannt nachklingt."""
    r = rng(seed)
    dur = 1.1
    y = np.zeros(int(dur * SR))
    mix_at(y, zip_thread(r, 0.32, r.uniform(1800, 2400), r.uniform(5200, 7000)), 0.0, 0.9)
    mix_at(y, twang(r.uniform(520, 700), 0.8, seed + 1), 0.22, 0.3)
    return y


def web(seed: int) -> np.ndarray:
    """Der Netzballen: ein feuchtes Spucken und ein Klatschen, Fäden sirren."""
    r = rng(seed)
    dur = 1.0
    y = np.zeros(int(dur * SR))
    mix_at(y, hiss(r, 0.22, 900, 5000, 0.005, 0.15), 0.0, 0.7)
    mix_at(y, splat(r, 0.3), 0.04, 0.8)
    mix_at(y, zip_thread(r, 0.4, 3000, 1600), 0.1, 0.35)
    return y


def constellation(seed: int) -> np.ndarray:
    """Das Sternbild: Töne leuchten nacheinander auf und verbinden sich zu einem hellen Akkord."""
    r = rng(seed)
    dur = 3.4
    n = int(dur * SR)
    y = np.zeros(n)
    notes = [STARS[i] for i in sorted(r.choice(len(STARS), 6, replace=False))]
    for i, f in enumerate(notes):
        mix_at(y, chime(f, 2.6 - i * 0.2, 110 + i, decay=1.2), i * 0.22, 0.4)
    m = int(1.8 * SR)
    chord = sum(tone(np.full(m, f), env_asr(m, SR, 0.4, 1.0) * 0.18, SR, harmonics=((1, 1.0), (2, 0.2))) for f in notes[::2])
    mix_at(y, chord, 1.3, 1.0)
    return y


def starfall(seed: int) -> np.ndarray:
    """Der Sternenfall: fallendes Pfeifen, dann gläserne, funkelnde Einschläge."""
    r = rng(seed)
    dur = 2.8
    n = int(dur * SR)
    y = np.zeros(n)
    for k in range(4):
        m = int(0.7 * SR)
        f0 = r.uniform(2400, 3400)
        whistle = tone(glide(m, f0, f0 * 0.45, 1.3), env_asr(m, SR, 0.05, 0.25) * 0.4, SR, harmonics=((1, 1.0), (2, 0.15)))
        whistle += moving_resonator(r.standard_normal(m), glide(m, f0 * 1.4, f0 * 0.6, 1.3), 6.0) * env_asr(m, SR, 0.05, 0.2) * 0.3
        start = k * 0.42 + r.uniform(0, 0.08)
        mix_at(y, whistle, start, 0.6)
        mix_at(y, impact(int(0.35 * SR), seed + k, 600, 7000, 0.06), start + 0.68, 0.7)
        mix_at(y, soft_bell(r.choice(STARS) * 2, 0.9, 0.35, 120 + k), start + 0.68, 0.35)
    return y


def cocoon(seed: int) -> np.ndarray:
    """Der Kokon: Fäden umwickeln schnell und eng – ein schnelles, kreisendes Sirren, das immer höher wird, dann ein Zuziehen."""
    r = rng(seed)
    dur = 2.0
    n = int(dur * SR)
    y = np.zeros(n)
    m = int(1.5 * SR)
    t = np.arange(m) / SR
    sweep = glide(m, 900.0, 2600.0, 1.0) * (1.0 + 0.35 * np.sin(2 * np.pi * (4.0 + 6.0 * t) * t))
    mix_at(y, moving_resonator(r.standard_normal(m), sweep, 10.0) * env_asr(m, SR, 0.1, 0.25), 0.0, 0.8)
    for k in range(6):
        mix_at(y, zip_thread(r, 0.2, r.uniform(1500, 2500), r.uniform(3000, 5000)), k * 0.22, 0.35)
    mix_at(y, splat(r, 0.25, 500, 3500), 1.45, 0.6)
    mix_at(y, twang(r.uniform(300, 400), 0.5, seed + 4), 1.5, 0.25)
    return y


def brood(seed: int) -> np.ndarray:
    """Die Brut schlüpft: Eier platzen, ein Gewimmel vieler kleiner Klicks."""
    r = rng(seed)
    dur = 2.0
    y = np.zeros(int(dur * SR))
    for k in range(5):
        mix_at(y, splat(r, 0.15, 800, 4000), k * r.uniform(0.12, 0.2), 0.45)
    mix_at(y, chitter(r, 1.8, 26.0, 3200, 6400), 0.2, 0.7)
    mix_at(y, shimmer(r, 1.6, 4, 0.5, 5), 0.2)
    return y


def drop(seed: int) -> np.ndarray:
    """Der Fall von oben: ein Sausen, ein schwerer, federnder Aufprall ins Netz, die Fäden schwingen nach."""
    r = rng(seed)
    dur = 2.0
    n = int(dur * SR)
    y = np.zeros(n)
    m = int(0.7 * SR)
    mix_at(y, moving_resonator(r.standard_normal(m), glide(m, 600.0, 2400.0, 0.6), 4.0) * env_asr(m, SR, 0.3, 0.05), 0.0, 0.6)
    mix_at(y, thud(int(0.9 * SR), 80.0, 38.0, 0.35), 0.68, 1.2)
    mix_at(y, impact(int(0.3 * SR), seed, 300, 3000, 0.07), 0.68, 0.6)
    for k in range(5):
        mix_at(y, twang(r.uniform(90, 220), 1.0, seed + 7 + k), 0.7 + k * 0.03, 0.2)
    return y


def bite(seed: int) -> np.ndarray:
    """Der Biss: die Klauen klacken, ein nasses Zuschnappen."""
    r = rng(seed)
    dur = 0.7
    y = np.zeros(int(dur * SR))
    mix_at(y, click(r, r.uniform(1100, 1600), 14.0, 1.2), 0.0, 1.0)
    mix_at(y, click(r, r.uniform(1100, 1600), 14.0, 1.2), 0.06, 0.9)
    mix_at(y, splat(r, 0.2, 400, 3000), 0.08, 0.7)
    mix_at(y, hiss(r, 0.3, 2000, 7000, 0.01, 0.2), 0.0, 0.3)
    return y


def spindle(seed: int) -> np.ndarray:
    """Die Spindel: ein surrendes Kreisen, das schneller und höher wird, Fäden schwirren um sie."""
    r = rng(seed)
    dur = 2.6
    n = int(dur * SR)
    t = np.arange(n) / SR
    rate = 3.0 + 9.0 * (t / dur) ** 1.5
    sweep = 700.0 * (1.0 + 0.6 * t / dur) * (1.0 + 0.4 * np.sin(2 * np.pi * np.cumsum(rate) / SR))
    y = moving_resonator(r.standard_normal(n), sweep, 8.0) * env_asr(n, SR, 0.4, 0.5)
    y += hum(dur, 196.0, 392.0, 0.6, 0.5, 0.003) * 0.25
    return y


def spiderling(seed: int) -> np.ndarray:
    """Ein Sternspinnling: helles, kleines Zirpen und Klicken."""
    r = rng(seed)
    dur = 0.8
    y = np.zeros(int(dur * SR))
    mix_at(y, chitter(r, 0.6, 9.0, 4200, 7600), 0.0, 0.7)
    m = int(0.18 * SR)
    f = r.uniform(2600, 3400)
    chirp = tone(glide(m, f, f * 1.25, 1.0), env_ad(m, SR, 0.005, 0.06), SR, harmonics=((1, 1.0), (2, 0.2)))
    mix_at(y, chirp, r.uniform(0.05, 0.3), 0.35)
    return y


# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil) oder None, Ziel-Pegel dBFS)
SYNTH = {
    "ambient": (ambient, 3, (2.2, 0.35), -23.0),
    "hurt": (hurt, 3, (1.0, 0.2), -17.0),
    "death": (death, 1, (3.0, 0.45), -14.0),
    "step": (step, 4, (0.8, 0.2), -22.0),
    "awaken": (awaken, 1, (2.6, 0.4), -15.0),
    "thread": (thread, 3, (1.4, 0.3), -18.0),
    "web": (web, 3, (1.0, 0.25), -17.0),
    "constellation": (constellation, 2, (2.4, 0.4), -16.0),
    "starfall": (starfall, 2, (2.0, 0.35), -15.0),
    "cocoon": (cocoon, 2, (1.4, 0.3), -16.0),
    "brood": (brood, 2, (1.4, 0.3), -17.0),
    "drop": (drop, 2, (1.6, 0.3), -14.0),
    "bite": (bite, 3, (0.8, 0.2), -16.0),
    "spindle": (spindle, 2, (1.6, 0.3), -16.0),
    "fury": (fury, 1, (2.6, 0.4), -13.0),
    "spiderling": (spiderling, 4, (0.6, 0.15), -21.0),
}

# Ereignis: Reichweite
EVENTS = {f"entity.star_weaver.{name}": reach for name, reach in (
    ("ambient", 24), ("hurt", 32), ("death", 64), ("step", 20), ("awaken", 48), ("thread", 32), ("web", 32), ("constellation", 48),
    ("starfall", 48), ("cocoon", 32), ("brood", 32), ("drop", 48), ("bite", 24), ("spindle", 40), ("fury", 64), ("spiderling", 16))}


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    files = []
    for i in range(count):
        x = fn(9900 + i * 37 + zlib.crc32(name.encode()) % 991)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 2300 + i, stereo=False, damping=5200), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.05)
        x = soft_clip(fit_level(x, level, -2.0))
        path = OUT / f"star_weaver_{name}{i + 1}.ogg"
        write_ogg(x.astype(np.float32), SR, path, quality=5.0)
        files.append(f"kingdomsatwar:entity/void/star_weaver_{name}{i + 1}")
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
    events = {}
    for name in SYNTH:
        files = render(name, args.preview)
        reach = EVENTS[f"entity.star_weaver.{name}"]
        events[f"entity.star_weaver.{name}"] = {"sounds": [{"name": f, "attenuation_distance": reach} for f in files],
                                                "subtitle": f"subtitles.kingdomsatwar.entity.star_weaver.{name}"}
    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8")) if SOUNDS_JSON.exists() else {}
    data.update(events)
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"{len(events)} Klangereignisse geschrieben")


if __name__ == "__main__":
    main()
