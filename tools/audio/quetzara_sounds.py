#!/usr/bin/env python3
"""
Die Klänge des Schlangentempels (1.10): Quetzara, die Gefiederte Schlange, und der Schlangenaltar.

Quetzara ist eine Riesenschlange mit Federn: Sie zischt (ein scharfes, breites Rauschen hoch oben, das an- und abschwillt),
ihre Federn rasseln (ein schnelles Klappern wie bei einer Klapperschlange, dazu Rascheln), und wenn sie zornig ist, kreischt
sie wie ein Raubvogel. Ihre Schuppen gleiten schabend über Stein. Im Einzelnen:

  * das leise Zischen, wenn sie lauert; das laute Zischen (Warnung); das Erwachen (ein tiefes Grollen, das Zischen schwillt
    an, die Federn rascheln, zuletzt ein Rasseln); ihr Kreischen (ein Raubvogelschrei über einem Zischen); das Rasseln;
  * der Biss (ein Sausen, dann das Zuschnappen), das Vorschnellen (ein schnelles Sausen), die Giftspucke (ein nasses
    Ausspeien), das Zerplatzen des Gifts (es spritzt und zischt), der Federsturm (Flattern und Sausen), eine Feder trifft
    (ein dumpfes Tok), die Schwanzpeitsche (ein Sausen, das in einem Knall endet), das Würgen (Schuppen knarren, ein Ächzen,
    ein Knacken), die Häutung (Haut reißt wie trockenes Papier, darunter ein Schaben), die leere Haut platzt (ein Knall und
    Gas), das Gleiten der Schuppen;
  * Schmerz (ein kurzes Kreischen mit Zischen), der Tod (ein langes, fallendes Kreischen, das Zischen erstirbt, ein Aufschlag);
  * der Schlangenaltar: das goldene Schlangenbild zerspringt (Gold klirrt, ein tiefer Glockenton), ein Goldblock wird
    geopfert (helle Glocken über einem Summen).

Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py); es werden keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/quetzara_sounds.py [--preview /pfad] [--only name …]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/serpent/<klang><n>.ogg
    Einträge "entity.quetzara.*", "block.serpent_altar.*" in assets/kingdomsatwar/sounds.json
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, audible_bass, bell, curve, env_ad, fade_edges, filt, fit_level, glide, rng, room,  # noqa: E402
                          soft_clip, tone, trim_tail)
from dsp import write_ogg  # noqa: E402
from forge_sounds import crackle, impact, mix_at, thud  # noqa: E402
from hour_warden_sounds import clatter, whoosh  # noqa: E402
from kraken_sounds import moving_resonator  # noqa: E402
from monster_sounds import pulse_voice  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/serpent"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100


def length(dur: float) -> int:
    return int(dur * SR)


# =====================================================================================
# Bausteine
# =====================================================================================

def hiss(r, dur: float, lo: float = 2800.0, hi: float = 9500.0, env=None, flutter: float = 0.0) -> np.ndarray:
    """Ein Zischen: breites Rauschen hoch oben, mit einem Hauch Tiefe; {@code flutter} lässt es zittern."""
    n = length(dur)
    t = np.arange(n) / SR
    y = filt(r.standard_normal(n), "band", [lo, hi], SR, 3)
    y += filt(r.standard_normal(n), "band", [lo * 0.45, lo], SR, 2) * 0.25
    if flutter > 0.0:
        y *= 1.0 + flutter * np.sin(2 * np.pi * r.uniform(9.0, 14.0) * t)
    return y * curve(n, env or [(0.0, 0.0), (0.08, 1.0), (0.7, 0.85), (1.0, 0.0)])


def rattle(r, dur: float, rate: float = 48.0, env=None) -> np.ndarray:
    """Ein Rasseln: dicht gereihte, trockene Klicks (wie Schuppen und Kiele, die gegeneinander schlagen)."""
    n = length(dur)
    y = np.zeros(n)
    t = 0.0
    while t < dur - 0.01:
        click = impact(length(0.012), int(r.integers(1 << 30)), 1800.0, 7500.0, 0.0025)
        mix_at(y, click, t, r.uniform(0.6, 1.0))
        t += 1.0 / (rate * r.uniform(0.85, 1.15))
    return y * curve(n, env or [(0.0, 0.0), (0.06, 1.0), (0.8, 0.9), (1.0, 0.0)])


def rustle(r, dur: float, env=None) -> np.ndarray:
    """Federn rascheln: weiches Rauschen, schnell flatternd."""
    n = length(dur)
    t = np.arange(n) / SR
    y = filt(r.standard_normal(n), "band", [900.0, 5200.0], SR, 2)
    y *= 0.55 + 0.45 * np.abs(np.sin(2 * np.pi * r.uniform(18.0, 26.0) * t + r.uniform(0, 6)))
    return y * curve(n, env or [(0.0, 0.0), (0.2, 1.0), (0.8, 0.8), (1.0, 0.0)])


def scales(r, dur: float, env=None) -> np.ndarray:
    """Schuppen gleiten über Stein: ein schabendes Rauschen, das langsam an- und abschwillt."""
    n = length(dur)
    t = np.arange(n) / SR
    y = filt(r.standard_normal(n), "band", [400.0, 3800.0], SR, 2)
    y *= 0.5 + 0.5 * np.sin(2 * np.pi * r.uniform(1.5, 2.6) * t + r.uniform(0, 6)) ** 2
    y += crackle(n, int(r.integers(1 << 30)), 40.0, 1500.0, 6000.0) * 0.3
    return y * curve(n, env or [(0.0, 0.0), (0.25, 1.0), (0.75, 1.0), (1.0, 0.0)])


def screech(r, dur: float, f0: float, f1: float, seed: int, env=None) -> np.ndarray:
    """Ein Raubvogelschrei: eine helle, raue Stimme, die gleitet und bricht, darunter ein Zischen."""
    n = length(dur)
    f = glide(n, f0, f1, 0.7) * (1.0 + 0.04 * np.sin(2 * np.pi * 7.0 * np.arange(n) / SR))
    a = curve(n, env or [(0.0, 0.0), (0.06, 1.0), (0.55, 0.9), (1.0, 0.0)])
    v = pulse_voice(f, a, seed, "e", size=0.35, jitter=0.06, shimmer=0.18, sub=0.25, breath=0.4, bright=7000.0)
    v = filt(v, "high", 500.0, SR, 2)
    return v + hiss(r, dur, env=env) * 0.25


def growl(r, dur: float, f0: float, env=None) -> np.ndarray:
    """Ein tiefes Grollen aus der Brust der Schlange."""
    n = length(dur)
    y = tone(glide(n, f0, f0 * 0.82, 1.0), np.ones(n), SR, harmonics=((1, 1.0), (2, 0.4), (3, 0.2), (4, 0.1)))
    y *= 1.0 + 0.3 * np.sin(2 * np.pi * r.uniform(11.0, 16.0) * np.arange(n) / SR)
    y += filt(r.standard_normal(n), "low", 300.0, SR, 2) * 0.6
    return y * curve(n, env or [(0.0, 0.0), (0.3, 1.0), (0.8, 0.9), (1.0, 0.0)])


def snap(r, seed: int) -> np.ndarray:
    """Das Zuschnappen der Kiefer: ein harter, kurzer Schlag mit Knacken."""
    y = np.zeros(length(0.5))
    mix_at(y, impact(length(0.08), seed, 600.0, 6000.0, 0.012), 0.0, 1.0)
    mix_at(y, thud(length(0.3), 160.0, 70.0, 0.06), 0.0, 0.8)
    mix_at(y, crackle(length(0.1), seed + 1, 300.0, 2000.0, 8000.0), 0.0, 0.4)
    return y


def chime(f0: float, dur: float, seed: int, decay: float = 1.2) -> np.ndarray:
    return bell(f0, dur, SR, ratios=(1.0, 2.0, 3.01, 4.2), amps=(1.0, 0.4, 0.2, 0.1), decay=decay, seed=seed)


# =====================================================================================
# Quetzara
# =====================================================================================

def ambient(seed: int, i: int) -> np.ndarray:
    """Sie lauert: ein leises Zischen, die Federn rascheln, die Schuppen schaben."""
    r = rng(seed)
    y = np.zeros(length(2.4))
    mix_at(y, hiss(r, 1.2 + 0.3 * i, env=[(0.0, 0.0), (0.3, 1.0), (0.7, 0.8), (1.0, 0.0)]), 0.1, 0.6)
    mix_at(y, rustle(r, 1.4), 0.6, 0.35)
    mix_at(y, scales(r, 2.0), 0.2, 0.3)
    return y


def hiss_loud(seed: int, i: int) -> np.ndarray:
    """Die Warnung: ein langes, lautes Zischen, das sich aufbäumt."""
    r = rng(seed)
    return hiss(r, 1.3 + 0.25 * i, 2400.0, 10000.0, [(0.0, 0.0), (0.05, 1.0), (0.4, 0.9), (0.85, 0.7), (1.0, 0.0)], flutter=0.12)


def awaken(seed: int, i: int) -> np.ndarray:
    """Sie erwacht: ein tiefes Grollen, das Zischen schwillt an, die Federn der Krause rascheln, zuletzt ein Rasseln."""
    r = rng(seed)
    y = np.zeros(length(3.6))
    mix_at(y, growl(r, 2.4, 58.0, [(0.0, 0.0), (0.4, 1.0), (1.0, 0.0)]), 0.0, 0.9)
    mix_at(y, hiss(r, 2.4, env=[(0.0, 0.0), (0.7, 1.0), (1.0, 0.0)]), 0.6, 0.7)
    mix_at(y, rustle(r, 1.6), 1.2, 0.5)
    mix_at(y, rattle(r, 1.0), 2.4, 0.6)
    return y


def screech_call(seed: int, i: int) -> np.ndarray:
    """Ihr Kreischen: ein Raubvogelschrei, der hoch ansetzt und bricht, über einem Zischen."""
    r = rng(seed)
    y = np.zeros(length(2.2))
    mix_at(y, screech(r, 1.5, 1180.0 + 60.0 * i, 760.0, seed), 0.0, 1.0)
    mix_at(y, screech(r, 0.6, 1400.0, 1100.0, seed + 7, [(0.0, 0.0), (0.1, 1.0), (1.0, 0.0)]), 1.2, 0.55)
    mix_at(y, growl(r, 1.6, 70.0), 0.0, 0.35)
    return y


def rattle_call(seed: int, i: int) -> np.ndarray:
    """Das Rasseln der Federn und Schuppen (wie eine Klapperschlange)."""
    r = rng(seed)
    y = np.zeros(length(1.4))
    mix_at(y, rattle(r, 1.1, 44.0 + 6.0 * i), 0.0, 1.0)
    mix_at(y, rustle(r, 1.0), 0.1, 0.4)
    return y


def strike(seed: int, i: int) -> np.ndarray:
    """Sie schnellt vor: ein schnelles, scharfes Sausen."""
    r = rng(seed)
    y = np.zeros(length(0.7))
    mix_at(y, whoosh(r, 0.45, 500.0, 2400.0, 1.4, 0.7), 0.0, 1.0)
    mix_at(y, hiss(r, 0.3, env=[(0.0, 0.0), (0.1, 1.0), (1.0, 0.0)]), 0.0, 0.4)
    return y


def bite(seed: int, i: int) -> np.ndarray:
    """Der Biss: das Zuschnappen, ein kurzes Zischen danach."""
    r = rng(seed)
    y = np.zeros(length(0.9))
    mix_at(y, snap(r, seed), 0.0, 1.0)
    mix_at(y, hiss(r, 0.4, env=[(0.0, 0.0), (0.1, 1.0), (1.0, 0.0)]), 0.12, 0.35)
    return y


def spit(seed: int, i: int) -> np.ndarray:
    """Die Giftspucke: ein nasses, kräftiges Ausspeien."""
    r = rng(seed)
    n = length(0.6)
    y = np.zeros(n)
    burst = filt(r.standard_normal(length(0.25)), "band", [500.0, 3000.0], SR, 2) * env_ad(length(0.25), SR, 0.004, 0.08)
    mix_at(y, burst, 0.0, 1.0)
    mix_at(y, crackle(length(0.3), seed + 2, 120.0, 900.0, 4000.0), 0.02, 0.5)
    mix_at(y, whoosh(r, 0.4, 1200.0, 500.0, 1.2, 0.2), 0.05, 0.4)
    return y


def splash(seed: int, i: int) -> np.ndarray:
    """Das Gift zerplatzt: es spritzt, dann zischt und blubbert es."""
    r = rng(seed)
    y = np.zeros(length(1.4))
    mix_at(y, impact(length(0.2), seed, 300.0, 3000.0, 0.05), 0.0, 1.0)
    mix_at(y, crackle(length(1.1), seed + 1, 160.0, 600.0, 3000.0) * curve(length(1.1), [(0.0, 1.0), (1.0, 0.0)]), 0.05, 0.6)
    mix_at(y, hiss(r, 1.1, 1500.0, 6000.0, [(0.0, 0.0), (0.1, 1.0), (1.0, 0.0)]), 0.05, 0.4)
    return y


def feathers(seed: int, i: int) -> np.ndarray:
    """Der Federsturm: die Krause schlägt (Flattern), ein Fächer Federn saust davon."""
    r = rng(seed)
    y = np.zeros(length(1.0))
    mix_at(y, rustle(r, 0.5, [(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)]), 0.0, 0.8)
    for k in range(5):
        mix_at(y, whoosh(r, 0.35, 1600.0 + 200.0 * k, 600.0, 1.6, 0.3), 0.12 + k * 0.03, 0.35)
    return y


def feather_hit(seed: int, i: int) -> np.ndarray:
    """Eine Feder trifft: ein kurzes, dumpfes Tok."""
    r = rng(seed)
    y = np.zeros(length(0.3))
    mix_at(y, impact(length(0.06), seed, 400.0, 2600.0, 0.01), 0.0, 1.0)
    mix_at(y, thud(length(0.15), 260.0, 140.0, 0.03), 0.0, 0.5)
    return y


def whip(seed: int, i: int) -> np.ndarray:
    """Die Schwanzpeitsche: ein Sausen, das in einem scharfen Knall endet."""
    r = rng(seed)
    y = np.zeros(length(1.0))
    mix_at(y, whoosh(r, 0.4, 300.0, 1800.0, 1.2, 0.85), 0.0, 0.9)
    mix_at(y, impact(length(0.05), seed, 1500.0, 12000.0, 0.006), 0.38, 1.0)
    mix_at(y, thud(length(0.3), 120.0, 60.0, 0.08), 0.38, 0.6)
    return y


def squeeze(seed: int, i: int) -> np.ndarray:
    """Das Würgen: Schuppen knarren, ein dumpfes Ächzen, ein Knacken."""
    r = rng(seed)
    y = np.zeros(length(1.4))
    n = length(1.0)
    creak = moving_resonator(r.standard_normal(n), glide(n, 260.0, 180.0, 1.0), 10.0) * curve(n, [(0.0, 0.0), (0.3, 1.0), (1.0, 0.0)])
    mix_at(y, creak, 0.0, 0.8)
    mix_at(y, growl(r, 1.0, 64.0), 0.0, 0.6)
    mix_at(y, crackle(length(0.25), seed + 4, 200.0, 900.0, 5000.0), 0.55, 0.6)
    return y


def molt(seed: int, i: int) -> np.ndarray:
    """Die Häutung: trockene Haut reißt wie Papier, darunter schaben die Schuppen; ein Zischen."""
    r = rng(seed)
    y = np.zeros(length(2.4))
    tear = crackle(length(1.6), seed, 600.0, 1200.0, 9000.0) * curve(length(1.6), [(0.0, 0.2), (0.5, 1.0), (1.0, 0.0)])
    mix_at(y, tear, 0.0, 0.9)
    mix_at(y, scales(r, 2.0), 0.2, 0.6)
    mix_at(y, hiss(r, 1.2), 0.9, 0.4)
    return y


def skin_burst(seed: int, i: int) -> np.ndarray:
    """Die leere Haut platzt: ein Knall, dann strömt Gas zischend aus."""
    r = rng(seed)
    y = np.zeros(length(1.6))
    mix_at(y, impact(length(0.15), seed, 200.0, 4000.0, 0.03), 0.0, 1.0)
    mix_at(y, thud(length(0.4), 120.0, 50.0, 0.1), 0.0, 0.8)
    mix_at(y, hiss(r, 1.3, 1200.0, 7000.0, [(0.0, 0.0), (0.05, 1.0), (1.0, 0.0)]), 0.04, 0.7)
    return y


def slither(seed: int, i: int) -> np.ndarray:
    """Die Schuppen gleiten über den Stein."""
    r = rng(seed)
    return scales(r, 0.9 + 0.2 * i)


def hurt(seed: int, i: int) -> np.ndarray:
    """Schmerz: ein kurzes Kreischen, ein Zischen."""
    r = rng(seed)
    y = np.zeros(length(0.9))
    mix_at(y, screech(r, 0.45, 1020.0 + 80.0 * i, 820.0, seed, [(0.0, 0.0), (0.08, 1.0), (1.0, 0.0)]), 0.0, 1.0)
    mix_at(y, hiss(r, 0.5), 0.05, 0.4)
    return y


def death(seed: int, i: int) -> np.ndarray:
    """Der Tod: ein langes, fallendes Kreischen, das Zischen erstirbt, der schwere Leib schlägt auf."""
    r = rng(seed)
    y = np.zeros(length(4.0))
    mix_at(y, screech(r, 2.4, 1100.0, 380.0, seed, [(0.0, 0.0), (0.05, 1.0), (0.6, 0.6), (1.0, 0.0)]), 0.0, 1.0)
    mix_at(y, hiss(r, 2.8, env=[(0.0, 0.5), (0.3, 1.0), (1.0, 0.0)]), 0.3, 0.5)
    mix_at(y, thud(length(1.2), 80.0, 34.0, 0.4), 2.2, 0.9)
    mix_at(y, scales(r, 1.2, [(0.0, 1.0), (1.0, 0.0)]), 2.3, 0.4)
    return y


def altar_shatter(seed: int, i: int) -> np.ndarray:
    """Das Schlangenbild zerspringt: Gold klirrt, die Stücke springen, ein tiefer Glockenton verklingt."""
    r = rng(seed)
    y = np.zeros(length(3.0))
    mix_at(y, impact(length(0.1), seed, 800.0, 9000.0, 0.02), 0.0, 1.0)
    mix_at(y, clatter(r, 1.4, 10, 1400.0, 3600.0), 0.02, 0.7)
    mix_at(y, chime(196.0, 2.8, seed + 2, 1.6), 0.05, 0.45)
    return y


def altar_offer(seed: int, i: int) -> np.ndarray:
    """Ein Goldblock wird geopfert: helle Glocken über einem Summen, ein Zischen aus der Tiefe."""
    r = rng(seed)
    y = np.zeros(length(3.0))
    for k, f in enumerate((523.25, 659.25, 783.99, 1046.5)):
        mix_at(y, chime(f, 2.4, seed + k, 1.1), 0.12 * k, 0.4)
    n = length(2.6)
    hum = tone(np.full(n, 98.0), curve(n, [(0.0, 0.0), (0.3, 1.0), (1.0, 0.0)]), SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.2)))
    mix_at(y, hum, 0.0, 0.35)
    mix_at(y, hiss(r, 1.0), 1.6, 0.25)
    return y


# Klang: (Erzeuger, Varianten, Raum (Nachhall, Anteil), Lautstärke in dB)
SYNTH = {
    "quetzara_ambient": (ambient, 3, (1.4, 0.3), -21.0),
    "quetzara_hiss": (hiss_loud, 3, (1.6, 0.3), -15.0),
    "quetzara_awaken": (awaken, 1, (2.4, 0.4), -13.0),
    "quetzara_screech": (screech_call, 2, (2.2, 0.35), -13.0),
    "quetzara_rattle": (rattle_call, 3, (1.2, 0.25), -16.0),
    "quetzara_strike": (strike, 3, (0.9, 0.2), -15.0),
    "quetzara_bite": (bite, 3, (1.0, 0.2), -14.0),
    "quetzara_spit": (spit, 2, (1.0, 0.2), -15.0),
    "quetzara_splash": (splash, 2, (1.2, 0.25), -16.0),
    "quetzara_feathers": (feathers, 3, (1.2, 0.25), -15.0),
    "quetzara_feather_hit": (feather_hit, 3, (0.6, 0.15), -18.0),
    "quetzara_whip": (whip, 2, (1.2, 0.25), -13.0),
    "quetzara_squeeze": (squeeze, 3, (1.0, 0.2), -15.0),
    "quetzara_molt": (molt, 2, (1.6, 0.3), -14.0),
    "quetzara_skin_burst": (skin_burst, 2, (1.4, 0.3), -14.0),
    "quetzara_slither": (slither, 3, (0.8, 0.15), -21.0),
    "quetzara_hurt": (hurt, 3, (1.2, 0.25), -15.0),
    "quetzara_death": (death, 1, (2.4, 0.4), -13.0),
    "serpent_altar_shatter": (altar_shatter, 1, (2.4, 0.4), -14.0),
    "serpent_altar_offer": (altar_offer, 1, (2.4, 0.4), -15.0),
}

# Ereignis: (Klang, Reichweite)
EVENTS = {
    "entity.quetzara.ambient": ("quetzara_ambient", 24),
    "entity.quetzara.hiss": ("quetzara_hiss", 40),
    "entity.quetzara.awaken": ("quetzara_awaken", 64),
    "entity.quetzara.screech": ("quetzara_screech", 64),
    "entity.quetzara.rattle": ("quetzara_rattle", 40),
    "entity.quetzara.strike": ("quetzara_strike", 32),
    "entity.quetzara.bite": ("quetzara_bite", 32),
    "entity.quetzara.spit": ("quetzara_spit", 40),
    "entity.quetzara.splash": ("quetzara_splash", 32),
    "entity.quetzara.feathers": ("quetzara_feathers", 40),
    "entity.quetzara.feather_hit": ("quetzara_feather_hit", 20),
    "entity.quetzara.whip": ("quetzara_whip", 40),
    "entity.quetzara.squeeze": ("quetzara_squeeze", 32),
    "entity.quetzara.molt": ("quetzara_molt", 48),
    "entity.quetzara.skin_burst": ("quetzara_skin_burst", 40),
    "entity.quetzara.slither": ("quetzara_slither", 16),
    "entity.quetzara.hurt": ("quetzara_hurt", 32),
    "entity.quetzara.death": ("quetzara_death", 64),
    "block.serpent_altar.shatter": ("serpent_altar_shatter", 48),
    "block.serpent_altar.offer": ("serpent_altar_offer", 48),
}

# sehr tiefe Klänge bekommen Obertöne ihres Basses, damit man sie auch auf kleinen Lautsprechern hört
AUDIBLE = {"quetzara_awaken", "quetzara_squeeze", "quetzara_death", "quetzara_whip"}


def files_of(name: str) -> list[str]:
    return [f"kingdomsatwar:entity/serpent/{name}{i + 1}" for i in range(SYNTH[name][1])]


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    for i in range(count):
        x = fn(21000 + i * 37 + zlib.crc32(name.encode()) % 997, i)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 4100 + i, stereo=False, damping=6000), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.06)
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
