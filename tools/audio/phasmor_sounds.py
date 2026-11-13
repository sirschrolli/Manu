#!/usr/bin/env python3
"""
Die Klänge des Phasenheiligtums (1.5): Phasmor, der Leerwandlerkönig, summt tief und verzerrt wie die Leere selbst, über ihm
klingt seine Krone aus Phasit wie ferne Glasglocken; kniend vor dem Thron summt seine Klinge (zwei Kristalltöne, die
gegeneinander schweben). Beim Erwachen reißt er die Klinge aus dem Stein und brüllt, die Krone flammt in einem hellen
Kristallakkord auf. Dazu: das Schwirren und der Hieb der Phasenklinge (Wind, dann ein gläsernes Klirren), der Phasensprung
(die Luft saugt sich zusammen, ein gläserner Knall, ein Ton, der nach unten fällt), das Flimmern der Trugbilder (ein Chor
verstimmter Glocken), das Aufsteigen der Brocken (Stein knirscht, ein Ton steigt), ihr Wurf und Aufschlag, die Leerenrisse
(ein tiefes Ansaugen, dann ein Schlag und klirrende Splitter), das Krümmen im Wasser (Zischen und ein gequältes Wimmern),
Schmerz und Tod (ein langes, fallendes Stöhnen; der Kristallakkord stürzt in sich zusammen). Außerdem das Aufleuchten des
Leerenthrons und der Phasenhieb der Klinge in der Hand des Spielers. Alles entsteht aus Rauschen und Tönen (siehe
ambience_dsp.py); es werden keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/phasmor_sounds.py [--preview /pfad] [--only name …]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/end/<klang><n>.ogg
    Einträge "entity.phasmor.*", "block.void_throne.kindle", "item.phase_blade.dash" in assets/kingdomsatwar/sounds.json
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, bell, curve, env_ad, env_asr, fade_edges, filt, fit_level, formant_filter, rng, room, saw,  # noqa: E402
                          soft_clip, trim_tail)
from ambience_dsp import audible_bass  # noqa: E402
from dsp import write_ogg  # noqa: E402
from forge_sounds import crackle, impact, mix_at, noise, thud  # noqa: E402
from mystik_sounds import whoosh  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/end"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100

# die Töne der Krone: E-Moll mit hinzugefügter None (kalt und weit)
CROWN_NOTES = (659.25, 987.77, 1318.5, 1479.98, 1975.5)


# =====================================================================================
# Bausteine
# =====================================================================================

def glass(f0: float, dur: float, seed: int, decay: float = 0.6) -> np.ndarray:
    """Ein Phasitkristall klingt: hell, gläsern, leicht unharmonisch, sanft ausgeblendet."""
    b = bell(f0, dur, SR, ratios=(1.0, 2.41, 3.73, 5.2), amps=(1.0, 0.45, 0.25, 0.12), decay=decay, seed=seed)
    k = max(1, int(min(len(b) * 0.35, 0.3 * SR)))
    b[len(b) - k:] *= np.linspace(1.0, 0.0, k) ** 2
    return b


def voice(n: int, f: np.ndarray, amp: np.ndarray, vowel: str, seed: int, rough: float = 0.0, warp: float = 0.0) -> np.ndarray:
    """Die Stimme des Königs: tief, hohl, wie durch die Leere gesprochen – ein Sägezahn durch die Formanten eines Vokals,
    dazu ein zweiter, leicht verstimmter (die Stimme ist nicht ganz da), auf Wunsch rau und schwankend."""
    r = rng(seed)
    t = np.arange(n) / SR
    wob = 1 + 0.008 * np.sin(2 * np.pi * r.uniform(4.0, 5.5) * t) + warp * np.sin(2 * np.pi * r.uniform(0.6, 1.2) * t)
    y = saw(f * wob, amp, SR, partials=40, rolloff=1.15)
    y += 0.6 * saw(f * wob * 1.012, amp, SR, partials=30, rolloff=1.2)
    if rough > 0:
        y *= 1 + rough * np.sin(np.cumsum(f * 0.5) * 2 * np.pi / SR)
    formants = {
        "o": [(420, 90, 1.0), (780, 120, 0.6), (2400, 300, 0.12)],
        "a": [(700, 110, 1.0), (1150, 150, 0.55), (2500, 300, 0.15)],
        "u": [(320, 70, 1.0), (650, 110, 0.35), (2300, 300, 0.08)],
    }[vowel]
    return formant_filter(y, SR, formants)


def suck(n: int, seed: int, f0: float = 300.0, f1: float = 4000.0) -> np.ndarray:
    """Die Luft saugt sich zusammen: ein Rauschen, das anschwillt und nach oben streicht – dann reißt es ab."""
    return whoosh(n, seed, f0, f1, 0.9) * curve(n, [(0.0, 0.0), (0.85, 1.0), (1.0, 0.0)])


def chime_chord(seed: int, notes=CROWN_NOTES, dur: float = 2.4, spread: float = 0.05, decay: float = 1.2) -> np.ndarray:
    """Ein Akkord aus Kristallglocken, leicht gestaffelt."""
    n = int((dur + spread * len(notes) + 0.2) * SR)
    y = np.zeros(n)
    for i, f in enumerate(notes):
        mix_at(y, glass(f, dur, seed + i, decay=decay), i * spread, 0.5 / (1 + i * 0.15))
    return y


# =====================================================================================
# Stimme
# =====================================================================================

def ambient(seed: int, v: int) -> np.ndarray:
    """Ein tiefes, verzerrtes Summen wie die Leere selbst; darüber klingt die Krone wie ferne Glasglocken."""
    r = rng(seed)
    n = int(r.uniform(2.4, 3.0) * SR)
    f0 = [62.0, 58.0, 66.0][v % 3]
    f = curve(n, [(0, f0 * 1.03), (0.5, f0), (1, f0 * 0.95)])
    y = voice(n, f, env_asr(n, SR, 0.5, 0.9), "u", seed, warp=0.02) * 0.9
    y += voice(n, f * 1.5, env_asr(n, SR, 0.7, 0.9), "o", seed + 1, warp=0.03) * 0.3
    for i in range(2 + v % 2):
        mix_at(y, glass(float(r.choice(CROWN_NOTES)) * r.choice([1.0, 2.0]), 1.6, seed + 10 + i, decay=0.9), r.uniform(0.3, n / SR - 1.7), 0.07)
    return filt(y, "low", 4200, SR, 2)


def guard(seed: int, v: int) -> np.ndarray:
    """Die Klinge summt: zwei Kristalltöne, die langsam gegeneinander schweben, über einem leisen, tiefen Brummen."""
    r = rng(seed)
    n = int(3.4 * SR)
    t = np.arange(n) / SR
    base = [880.0, 830.6][v % 2]
    env = curve(n, [(0.0, 0.0), (0.3, 1.0), (0.75, 0.8), (1.0, 0.0)])
    hum = (np.sin(2 * np.pi * base * t) + 0.8 * np.sin(2 * np.pi * (base + r.uniform(1.6, 2.6)) * t)
           + 0.25 * np.sin(2 * np.pi * base * 1.5 * t + 0.4)) * env
    drone = np.sin(2 * np.pi * 55.0 * t) * 0.5 * env + filt(noise(n, seed), "low", 220, SR, 2) * 0.2 * env
    y = hum * 0.4 + drone * 0.5
    mix_at(y, glass(base * 2, 1.4, seed + 3, decay=0.8), 0.4, 0.12)
    return y


def hurt(seed: int, v: int) -> np.ndarray:
    """Ein kurzes, verzerrtes Knurren; Kristall klirrt."""
    r = rng(seed)
    n = int(0.8 * SR)
    hi = r.uniform(120, 150)
    f = curve(n, [(0.0, hi), (0.25, hi * 0.85), (1.0, hi * 0.6)])
    y = voice(n, f, env_ad(n, SR, 0.01, 0.28), "a", seed, rough=0.35) * 0.9
    mix_at(y, glass(r.uniform(1900, 2400), 0.5, seed + 1, decay=0.25), 0.02, 0.25)
    mix_at(y, impact(int(0.15 * SR), seed + 2, 1500, 7000, 0.03), 0.0, 0.3)
    return y


def death(seed: int, v: int) -> np.ndarray:
    """Ein langes, fallendes Stöhnen; der Kristallakkord der Krone stürzt in sich zusammen; die Leere saugt ihn ein."""
    dur = 7.0
    n = int(dur * SR)
    y = np.zeros(n)
    m = int(4.2 * SR)
    f = curve(m, [(0.0, 105.0), (0.3, 90.0), (1.0, 38.0)])
    mix_at(y, voice(m, f, curve(m, [(0.0, 0.0), (0.05, 1.0), (0.7, 0.6), (1.0, 0.0)]), "o", seed, rough=0.2, warp=0.03), 0.0, 1.0)
    for i, fr in enumerate(reversed(CROWN_NOTES)):
        mix_at(y, glass(fr * (1.0 - i * 0.04), 1.6, seed + 10 + i, decay=0.7), 0.6 + i * 0.45, 0.16)
    k = int(1.6 * SR)
    mix_at(y, suck(k, seed + 3, 200.0, 3000.0), 3.8, 0.45)
    mix_at(y, thud(int(1.2 * SR), 70.0, 28.0, 0.5), 5.35, 0.7)
    return y


def awaken(seed: int, v: int) -> np.ndarray:
    """Er holt Atem, reißt die Klinge aus dem Stein (Knirschen, ein Kristallklang) und brüllt; die Krone flammt auf."""
    dur = 5.4
    n = int(dur * SR)
    y = np.zeros(n)
    m = int(1.0 * SR)
    mix_at(y, whoosh(m, seed, 200.0, 1200.0, 1.2) * curve(m, [(0.0, 0.0), (0.7, 1.0), (1.0, 0.0)]), 0.0, 0.3)
    g = int(0.6 * SR)
    grind = filt(noise(g, seed + 1), "band", [120, 900], SR, 2) * curve(g, [(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)])
    mix_at(y, grind, 0.9, 0.6)
    mix_at(y, glass(1318.5, 1.2, seed + 2, decay=0.6), 1.4, 0.35)
    k = int(2.4 * SR)
    f = curve(k, [(0.0, 60.0), (0.35, 105.0), (0.75, 98.0), (1.0, 70.0)])
    mix_at(y, voice(k, f, curve(k, [(0.0, 0.0), (0.12, 1.0), (0.7, 0.8), (1.0, 0.0)]), "a", seed + 3, rough=0.4, warp=0.02), 1.7, 1.0)
    mix_at(y, voice(k, f * 2.0, curve(k, [(0.0, 0.0), (0.15, 1.0), (0.7, 0.6), (1.0, 0.0)]), "o", seed + 4, rough=0.2), 1.72, 0.3)
    mix_at(y, chime_chord(seed + 5, dur=2.2), 2.5, 0.6)
    return y


def crown(seed: int, v: int) -> np.ndarray:
    """Die Krone flammt auf: ein Rauschen schwillt an (rückwärts), dann ein heller Kristallakkord, der weit nachklingt."""
    dur = 4.0
    n = int(dur * SR)
    y = np.zeros(n)
    m = int(1.0 * SR)
    mix_at(y, suck(m, seed, 400.0, 6000.0), 0.0, 0.5)
    notes = CROWN_NOTES if v % 2 == 0 else tuple(f * 1.1225 for f in CROWN_NOTES)
    mix_at(y, chime_chord(seed + 1, notes=notes, dur=2.6, spread=0.03, decay=1.4), 0.95, 1.0)
    mix_at(y, thud(int(0.8 * SR), 90.0, 40.0, 0.3), 0.95, 0.5)
    return y


# =====================================================================================
# Klinge
# =====================================================================================

def swing(seed: int, v: int) -> np.ndarray:
    """Die Phasenklinge schwirrt durch die Luft: ein schneller Wind, in dem der Kristall leise mitsingt."""
    r = rng(seed)
    n = int(0.8 * SR)
    y = np.zeros(n)
    m = int(0.55 * SR)
    mix_at(y, whoosh(m, seed, 300.0, r.uniform(2600, 3400), 0.8) * curve(m, [(0.0, 0.0), (0.6, 1.0), (1.0, 0.0)]), 0.0, 1.0)
    mix_at(y, glass(r.uniform(1700, 2100), 0.5, seed + 1, decay=0.3), 0.25, 0.08)
    return y


def slash(seed: int, v: int) -> np.ndarray:
    """Der Hieb: Wind, ein gläsernes Klirren, ein dumpfer Schlag."""
    r = rng(seed)
    n = int(1.1 * SR)
    y = np.zeros(n)
    m = int(0.22 * SR)
    mix_at(y, whoosh(m, seed, 500.0, 4800.0, 0.8) * curve(m, [(0.0, 0.0), (0.85, 1.0), (1.0, 0.2)]), 0.0, 0.7)
    mix_at(y, glass(r.uniform(2200, 2700), 0.8, seed + 1, decay=0.35), 0.2, 0.45)
    mix_at(y, impact(int(0.2 * SR), seed + 2, 300, 3000, 0.05), 0.2, 0.5)
    mix_at(y, thud(int(0.5 * SR), 110.0, 50.0, 0.12), 0.2, 0.5)
    return y


def blink(seed: int, v: int) -> np.ndarray:
    """Der Phasensprung: die Luft saugt sich zusammen, ein gläserner Knall, ein Ton fällt in die Tiefe."""
    r = rng(seed)
    n = int(0.9 * SR)
    y = np.zeros(n)
    m = int(0.28 * SR)
    mix_at(y, suck(m, seed, 500.0, 5000.0), 0.0, 0.6)
    mix_at(y, glass(r.uniform(2500, 3100), 0.4, seed + 1, decay=0.15), 0.27, 0.4)
    k = int(0.55 * SR)
    t = np.arange(k) / SR
    f = curve(k, [(0.0, r.uniform(900, 1100)), (1.0, r.uniform(120, 180))])
    drop = np.sin(2 * np.pi * np.cumsum(f) / SR) * env_ad(k, SR, 0.002, 0.18)
    mix_at(y, drop, 0.28, 0.35)
    mix_at(y, filt(noise(k, seed + 2), "band", [200, 1600], SR, 2) * env_ad(k, SR, 0.002, 0.12), 0.28, 0.25)
    _ = t
    return y


def illusion(seed: int, v: int) -> np.ndarray:
    """Die Trugbilder flimmern: ein Chor verstimmter Glocken, die zittern, dazu ein Flüstern."""
    r = rng(seed)
    n = int(1.8 * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    for i in range(5):
        f = float(r.choice(CROWN_NOTES)) * r.uniform(0.985, 1.015)
        b = glass(f, 1.4, seed + i, decay=0.7) * (0.6 + 0.4 * np.sin(2 * np.pi * r.uniform(5, 9) * np.arange(int(1.4 * SR)) / SR))
        mix_at(y, b, i * 0.07, 0.25)
    whisper = filt(noise(n, seed + 9), "band", [1800, 7000], SR, 2) * curve(n, [(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)])
    y += whisper * 0.12 * (0.5 + 0.5 * np.sin(2 * np.pi * 7.0 * t))
    return y


def lift(seed: int, v: int) -> np.ndarray:
    """Die Brocken steigen auf: Stein knirscht, Staub rieselt, ein Ton steigt."""
    r = rng(seed)
    n = int(1.6 * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    grind = filt(noise(n, seed), "band", [90, 700], SR, 2) * (0.6 + 0.4 * (r.random(n) < 0.3)) * curve(n, [(0.0, 0.0), (0.15, 1.0), (0.7, 0.7), (1.0, 0.0)])
    y += grind * 0.7
    y += crackle(n, seed + 1, 50, 800, 4000) * curve(n, [(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)]) * 0.4
    f = curve(n, [(0.0, 180.0), (1.0, 460.0)])
    y += np.sin(2 * np.pi * np.cumsum(f) / SR) * curve(n, [(0.0, 0.0), (0.4, 0.5), (1.0, 0.0)]) * 0.25
    _ = t
    return y


def throw(seed: int, v: int) -> np.ndarray:
    """Ein schwerer Wurf: tiefer Wind."""
    n = int(0.6 * SR)
    return whoosh(n, seed, 120.0, 1400.0, 1.1) * curve(n, [(0.0, 0.0), (0.4, 1.0), (1.0, 0.0)])


def boulder_impact(seed: int, v: int) -> np.ndarray:
    """Ein Brocken zerschellt: ein Krachen, ein Schlag, Geröll."""
    n = int(1.2 * SR)
    y = np.zeros(n)
    mix_at(y, impact(int(0.4 * SR), seed, 200, 5000, 0.08), 0.0, 1.0)
    mix_at(y, thud(int(0.7 * SR), 95.0, 38.0, 0.2), 0.0, 0.8)
    mix_at(y, crackle(int(0.9 * SR), seed + 1, 90, 600, 5000) * curve(int(0.9 * SR), [(0.0, 1.0), (1.0, 0.0)]), 0.03, 0.5)
    return y


def rift_open(seed: int, v: int) -> np.ndarray:
    """Die Leerenrisse öffnen sich: ein tiefes Ansaugen, die Leere knistert, darunter ein Brummen."""
    r = rng(seed)
    n = int(1.8 * SR)
    t = np.arange(n) / SR
    y = filt(noise(n, seed), "low", 900, SR, 2) * curve(n, [(0.0, 0.0), (0.6, 1.0), (1.0, 0.3)]) * 0.7
    y += crackle(n, seed + 1, 70, 1500, 8000) * curve(n, [(0.0, 0.2), (1.0, 1.0)]) * 0.35
    y += np.sin(2 * np.pi * r.uniform(48, 56) * t) * curve(n, [(0.0, 0.0), (0.5, 1.0), (1.0, 0.6)]) * 0.45
    return y


def rift_erupt(seed: int, v: int) -> np.ndarray:
    """Die Risse brechen auf: ein Schlag aus der Tiefe, ein Stoß aus Rauschen, klirrende Splitter."""
    r = rng(seed)
    n = int(2.0 * SR)
    y = np.zeros(n)
    mix_at(y, thud(int(1.2 * SR), 75.0, 28.0, 0.45), 0.0, 1.0)
    k = int(0.8 * SR)
    mix_at(y, filt(noise(k, seed), "band", [150, 3500], SR, 2) * env_ad(k, SR, 0.004, 0.25), 0.0, 0.7)
    for i in range(4):
        mix_at(y, glass(r.uniform(1800, 3200), 0.6, seed + 5 + i, decay=0.25), 0.05 + i * 0.07, 0.15)
    return y


def stagger(seed: int, v: int) -> np.ndarray:
    """Wasser! Ein Zischen wie auf heißem Stein, dazu ein gequältes, zitterndes Wimmern."""
    r = rng(seed)
    n = int(1.5 * SR)
    t = np.arange(n) / SR
    y = filt(noise(n, seed), "band", [2500, 10000], SR, 2) * curve(n, [(0.0, 0.0), (0.04, 1.0), (0.3, 0.35), (1.0, 0.0)]) * 0.3
    f = curve(n, [(0.0, 160.0), (0.4, 120.0), (1.0, 80.0)]) * (1 + 0.04 * np.sin(2 * np.pi * 9.0 * t))
    y += voice(n, f, curve(n, [(0.0, 0.0), (0.08, 1.0), (0.8, 0.5), (1.0, 0.0)]), "u", seed + 1, rough=0.3) * 0.8
    _ = r
    return y


def kindle(seed: int, v: int) -> np.ndarray:
    """Der Leerenthron leuchtet auf: ein weiches Anschwellen, dann ein Kristallakkord."""
    n = int(3.4 * SR)
    y = np.zeros(n)
    m = int(0.8 * SR)
    mix_at(y, suck(m, seed, 300.0, 3000.0), 0.0, 0.35)
    notes = CROWN_NOTES[:4] if v % 2 == 0 else CROWN_NOTES[1:]
    mix_at(y, chime_chord(seed + 1, notes=notes, dur=2.0, spread=0.08, decay=1.1), 0.75, 0.8)
    return y


def dash(seed: int, v: int) -> np.ndarray:
    """Der Phasenhieb in der Hand des Spielers: ein schneller Wind, ein gläserner Knall – leichter als beim König."""
    r = rng(seed)
    n = int(0.8 * SR)
    y = np.zeros(n)
    m = int(0.2 * SR)
    mix_at(y, suck(m, seed, 600.0, 5500.0), 0.0, 0.5)
    mix_at(y, whoosh(int(0.35 * SR), seed + 1, 800.0, 4000.0, 0.8) * curve(int(0.35 * SR), [(0.0, 1.0), (1.0, 0.0)]), 0.18, 0.5)
    mix_at(y, glass(r.uniform(2400, 2900), 0.5, seed + 2, decay=0.2), 0.19, 0.35)
    return y


# Klang: (Funktion, Varianten, Raum (Größe, Anteil) oder None, Lautheit LUFS)
SYNTH = {
    "phasmor_ambient": (ambient, 3, (1.4, 0.3), -20.0),
    "phasmor_guard": (guard, 2, (1.6, 0.35), -22.0),
    "phasmor_hurt": (hurt, 3, (0.8, 0.2), -16.0),
    "phasmor_death": (death, 1, (2.0, 0.35), -15.0),
    "phasmor_awaken": (awaken, 1, (2.0, 0.35), -14.0),
    "phasmor_crown": (crown, 2, (2.0, 0.4), -15.0),
    "phasmor_swing": (swing, 3, (0.8, 0.15), -18.0),
    "phasmor_slash": (slash, 3, (0.9, 0.2), -15.0),
    "phasmor_blink": (blink, 3, (1.0, 0.25), -16.0),
    "phasmor_illusion": (illusion, 2, (1.4, 0.35), -18.0),
    "phasmor_lift": (lift, 2, (1.0, 0.2), -17.0),
    "phasmor_throw": (throw, 2, (0.8, 0.15), -18.0),
    "phasmor_impact": (boulder_impact, 3, (1.0, 0.2), -15.0),
    "phasmor_rift_open": (rift_open, 2, (1.4, 0.3), -17.0),
    "phasmor_rift_erupt": (rift_erupt, 2, (1.6, 0.3), -14.0),
    "phasmor_stagger": (stagger, 2, (1.0, 0.2), -16.0),
    "void_throne_kindle": (kindle, 2, (1.8, 0.35), -17.0),
    "phase_blade_dash": (dash, 3, (0.8, 0.2), -17.0),
}

# Ereignis: (Klang, Reichweite)
EVENTS = {
    "entity.phasmor.ambient": ("phasmor_ambient", 24),
    "entity.phasmor.guard": ("phasmor_guard", 32),
    "entity.phasmor.hurt": ("phasmor_hurt", 32),
    "entity.phasmor.death": ("phasmor_death", 64),
    "entity.phasmor.awaken": ("phasmor_awaken", 64),
    "entity.phasmor.crown": ("phasmor_crown", 64),
    "entity.phasmor.swing": ("phasmor_swing", 32),
    "entity.phasmor.slash": ("phasmor_slash", 40),
    "entity.phasmor.blink": ("phasmor_blink", 48),
    "entity.phasmor.illusion": ("phasmor_illusion", 40),
    "entity.phasmor.lift": ("phasmor_lift", 40),
    "entity.phasmor.throw": ("phasmor_throw", 32),
    "entity.phasmor.impact": ("phasmor_impact", 40),
    "entity.phasmor.rift_open": ("phasmor_rift_open", 40),
    "entity.phasmor.rift_erupt": ("phasmor_rift_erupt", 48),
    "entity.phasmor.stagger": ("phasmor_stagger", 32),
    "block.void_throne.kindle": ("void_throne_kindle", 32),
    "item.phase_blade.dash": ("phase_blade_dash", 24),
}

# sehr tiefe Klänge bekommen Obertöne ihres Basses, damit man sie auch auf kleinen Lautsprechern hört
AUDIBLE = {"phasmor_ambient", "phasmor_rift_erupt", "phasmor_death", "phasmor_guard"}


def files_of(name: str) -> list[str]:
    return [f"kingdomsatwar:entity/end/{name}{i + 1}" for i in range(SYNTH[name][1])]


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    for i in range(count):
        x = fn(15100 + i * 37 + zlib.crc32(name.encode()) % 997, i)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 2500 + i, stereo=False, damping=6500), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.05)
        if name in AUDIBLE:
            x = audible_bass(x, SR)
        x = soft_clip(fit_level(x, level, -2.0))
        write_ogg(x.astype(np.float32), SR, OUT / f"{name}{i + 1}.ogg", quality=5.0)
        if preview is not None:
            from ambience import spectrogram
            spectrogram(x, SR, preview / f"{name}{i + 1}.png", f"{name}{i + 1}")
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
    events = {}
    for key, (name, reach) in EVENTS.items():
        entries = [{"name": f, "attenuation_distance": reach} for f in rendered[name]]
        events[key] = {"sounds": entries, "subtitle": f"subtitles.kingdomsatwar.{key}"}
    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8")) if SOUNDS_JSON.exists() else {}
    data.update(events)
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"{len(events)} Klangereignisse geschrieben")


if __name__ == "__main__":
    main()
