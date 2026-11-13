#!/usr/bin/env python3
"""
Die Klänge der Raubritterburg (1.8): Kuno von Rabenfels, der Raubritter, sein Rabenbanner und sein Morgenstern.

Kuno ist ein Mann in schwerer Plattenrüstung über einem Kettenhemd: Jede Bewegung klirrt (Platten klingen tief und kurz,
das Kettenhemd rieselt hell), seine Stimme ist rau und laut. Sein Ross wiehert und schnaubt mit den Klängen des Spiels (die
Pferdeklänge werden nur beim Namen genannt, nicht kopiert). Im Einzelnen:

  * das Erwachen (das Kriegshorn, die Rüstung rasselt, ein Ruf), der Ansturm (ein Schlachtruf, die Lanze senkt sich),
    der Lanzenstoß (Holz und Eisen auf Eisen), die brechende Lanze (ein Knall, Splitter, Stücke fallen), das Stampfen
    des Rosses (zwei schwere Hufschläge, die Erde bebt), das Horn (drei Stöße, die seine Leute rufen);
  * zu Fuß: der Hieb (ein schweres Sausen), der Schmetterschlag (der Morgenstern fährt in den Boden, Steine fliegen,
    das Eisen klingt nach), der Wirbel (der Morgenstern kreist sausend um ihn), der Schildstoß (Holz, Eisen, Schlag),
    der Sturz vom Pferd (Platten krachen auf den Boden, ein Stöhnen);
  * Schmerz (ein Klirren und ein Grunzen), der Tod (ein Stöhnen, die Rüstung fällt in sich zusammen), Schritte (Eisenschuh,
    Kettenhemd);
  * das Rabenbanner zerreißt (Tuch reißt, ein Rabe krächzt), der Schmetterschlag des Morgensterns in der Hand des Spielers.

Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py); es werden keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/robber_knight_sounds.py [--preview /pfad] [--only name …]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/castle/<klang><n>.ogg
    Einträge "entity.robber_knight.*", "block.raven_banner.tear", "item.morning_star.smash" in assets/kingdomsatwar/sounds.json
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, audible_bass, curve, env_ad, env_asr, fade_edges, filt, fit_level, glide, rng, room,  # noqa: E402
                          soft_clip, tone, trim_tail)
from creature_sounds import horn  # noqa: E402
from dsp import write_ogg  # noqa: E402
from forge_sounds import anvil_ring, crackle, impact, mix_at, thud  # noqa: E402
from hour_warden_sounds import clank, clatter, whoosh  # noqa: E402
from monster_sounds import pulse_voice  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/castle"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100

#: Holz unter Spannung (Eschenholz der Lanze): Frequenz, Güte, Pegel
ASH_WOOD = ((310.0, 14.0, 1.0), (760.0, 16.0, 0.65), (1380.0, 18.0, 0.4), (2420.0, 20.0, 0.22))


# =====================================================================================
# Bausteine
# =====================================================================================

def length(dur: float) -> int:
    return int(dur * SR)


def mail(r, dur: float, density: float = 220.0, gain: float = 0.6) -> np.ndarray:
    """Ein Kettenhemd in Bewegung: viele winzige, helle Ringe, die aneinanderschlagen (ein metallisches Rieseln)."""
    n = length(dur)
    y = np.zeros(n)
    for _ in range(int(density * dur)):
        at = r.uniform(0.0, max(0.0, dur - 0.06))
        mix_at(y, clank(r, r.uniform(3600.0, 7800.0), 0.05, 0.01 + 0.012 * r.random(), 0.6), at, r.uniform(0.04, 0.18))
    return filt(y, "high", 2400.0, SR, 2) * gain


def plates(r, dur: float, count: int, lo: float = 380.0, hi: float = 900.0, decay: float = 0.09) -> np.ndarray:
    """Platten einer Rüstung, die gegeneinanderschlagen: kurze, tiefere Klänge, gedämpft (Leder und Polster darunter)."""
    n = length(dur)
    y = np.zeros(n)
    for _ in range(count):
        at = r.uniform(0.0, dur * 0.8)
        mix_at(y, clank(r, r.uniform(lo, hi), 0.3, decay * r.uniform(0.7, 1.3), 0.5), at, r.uniform(0.25, 0.6))
    return filt(y, "low", 5200.0, SR, 2)


def voice(f0: float, dur: float, seed: int, vowel: str = "a", shape=None, amp=None, rough: float = 0.035, breath: float = 0.22,
          bright: float = 4200.0) -> np.ndarray:
    """Eine raue Männerstimme (Kuno ruft, brüllt, stöhnt): Stimmlippen-Pulse durch den Mundraum, etwas Luft."""
    n = length(dur)
    f = f0 * curve(n, shape or [(0.0, 0.92), (0.15, 1.0), (1.0, 1.0)])
    a = curve(n, amp or [(0.0, 0.0), (0.08, 1.0), (0.75, 0.85), (1.0, 0.0)])
    return pulse_voice(f, a, seed, vowel, size=1.0, jitter=rough, shimmer=0.1, sub=0.12, breath=breath, bright=bright)


def wood_knock(r, dur: float, gain: float = 1.0) -> np.ndarray:
    """Ein Schlag auf Holz: kurzer Anschlag, die Eigenfrequenzen des Schafts klingen kurz nach."""
    from ambience_dsp import resonator
    n = length(dur)
    x = r.standard_normal(n) * env_ad(n, SR, 0.0004, 0.006)
    y = np.zeros(n)
    for f, q, a in ASH_WOOD:
        y += resonator(x, f * r.uniform(0.95, 1.05), q, SR) * a
    return y * env_ad(n, SR, 0.0005, dur * 0.35) * gain


def hoof(seed: int, weight: float = 1.0) -> np.ndarray:
    """Ein schwerer Hufschlag auf festem Boden: der dumpfe Schlag, das Knirschen darunter."""
    r = rng(seed)
    n = length(0.5)
    y = thud(n, 95.0 * r.uniform(0.9, 1.1), 42.0, 0.16) * weight
    y += impact(n, seed + 1, 180.0, 1400.0, 0.03) * 0.55 * weight
    y += crackle(n, seed + 2, 140.0, 900.0, 4200.0) * env_ad(n, SR, 0.001, 0.08) * 0.25
    return y


def rubble(seed: int, dur: float, density: float = 260.0) -> np.ndarray:
    """Erde und Steine, die hochfliegen und niederprasseln."""
    n = length(dur)
    y = crackle(n, seed, density, 700.0, 5200.0)
    return y * curve(n, [(0.0, 0.0), (0.04, 1.0), (0.4, 0.55), (1.0, 0.0)])


# =====================================================================================
# Kuno
# =====================================================================================

def awaken(seed: int, i: int) -> np.ndarray:
    """Er erwacht: das Kriegshorn (zwei Stöße, der zweite steigt zur Quarte und wird gehalten), die Rüstung rasselt, als er die
    Lanze hebt, und ein lauter Ruf."""
    r = rng(seed)
    y = np.zeros(length(3.4))
    t = 0.0
    for k, (ratio, d) in enumerate([(1.0, 0.32), (4.0 / 3.0, 1.0)]):
        m = length(d)
        f = 146.83 * ratio * curve(m, [(0.0, 0.95), (0.1, 1.0), (1.0, 1.0)])
        a = curve(m, [(0.0, 0.0), (0.1, 1.0), (0.8, 0.9), (1.0, 0.0)])
        mix_at(y, horn(f, a, seed + k), t, 0.9)
        t += d + 0.06
    mix_at(y, plates(r, 0.8, 7), 1.3, 0.8)
    mix_at(y, mail(r, 0.9, 260.0), 1.25, 0.7)
    mix_at(y, voice(128.0, 0.75, seed + 5, "a", [(0.0, 0.9), (0.2, 1.08), (0.7, 1.12), (1.0, 0.95)]), 2.15, 0.95)
    return y


def charge(seed: int, i: int) -> np.ndarray:
    """Der Ansturm: ein Schlachtruf, der ansteigt („Haaa!“), die Lanze senkt sich sausend, Rüstung und Kettenhemd klirren."""
    r = rng(seed)
    y = np.zeros(length(2.0))
    mix_at(y, voice(132.0 + 8.0 * i, 1.2, seed, "a", [(0.0, 0.88), (0.15, 1.0), (0.8, 1.22), (1.0, 1.1)],
                    [(0.0, 0.0), (0.06, 0.8), (0.6, 1.0), (0.9, 0.9), (1.0, 0.0)], rough=0.05, breath=0.3), 0.0, 1.0)
    mix_at(y, whoosh(r, 0.55, 260.0, 120.0, 1.2, 0.6), 0.55, 0.5)
    mix_at(y, plates(r, 0.9, 6), 0.3, 0.6)
    mix_at(y, mail(r, 1.2, 200.0), 0.2, 0.5)
    return y


def lance_hit(seed: int, i: int) -> np.ndarray:
    """Der Lanzenstoß trifft: ein schwerer, tiefer Schlag, das Holz der Lanze dröhnt, die Spitze kracht auf Eisen."""
    r = rng(seed)
    y = np.zeros(length(1.0))
    mix_at(y, thud(length(0.6), 110.0, 46.0, 0.2), 0.0, 1.0)
    mix_at(y, impact(length(0.25), seed + 1, 250.0, 3800.0, 0.04), 0.0, 0.8)
    mix_at(y, wood_knock(r, 0.5), 0.004, 0.7)
    mix_at(y, clank(r, r.uniform(520.0, 640.0), 0.6, 0.18, 0.9), 0.006, 0.55)
    mix_at(y, mail(r, 0.4, 160.0), 0.02, 0.35)
    return y


def lance_break(seed: int, i: int) -> np.ndarray:
    """Die Lanze bricht am Schild: ein trockener Knall, Holz splittert knisternd, Stücke fallen und hüpfen auf dem Boden."""
    r = rng(seed)
    y = np.zeros(length(1.8))
    mix_at(y, impact(length(0.12), seed, 600.0, 9000.0, 0.012), 0.0, 1.0)
    mix_at(y, thud(length(0.4), 140.0, 60.0, 0.12), 0.0, 0.7)
    mix_at(y, wood_knock(r, 0.6, 1.0), 0.002, 0.9)
    splinter = crackle(length(0.45), seed + 2, 1600.0, 1500.0, 9000.0) * curve(length(0.45), [(0.0, 1.0), (0.3, 0.6), (1.0, 0.0)])
    mix_at(y, splinter, 0.01, 0.55)
    for k in range(5):                                   # Stücke der Lanze fallen (Holz, hohl)
        at = 0.45 + k * r.uniform(0.12, 0.2)
        mix_at(y, wood_knock(r, 0.25, 0.5), at, 0.5 / (1 + 0.3 * k))
    mix_at(y, clank(r, 880.0, 0.5, 0.12, 0.8), 0.6, 0.25)   # die eiserne Spitze klirrt
    return y


def stomp(seed: int, i: int) -> np.ndarray:
    """Das Ross steigt und stampft: zwei schwere Hufschläge kurz nacheinander, die Erde bebt, Steinchen prasseln."""
    r = rng(seed)
    y = np.zeros(length(1.2))
    mix_at(y, hoof(seed, 1.2), 0.0, 1.0)
    mix_at(y, hoof(seed + 9, 1.0), 0.13 + 0.02 * r.random(), 0.9)
    mix_at(y, thud(length(0.9), 60.0, 30.0, 0.35), 0.0, 0.9)
    mix_at(y, rubble(seed + 3, 0.9, 220.0), 0.02, 0.4)
    mix_at(y, mail(r, 0.4, 160.0), 0.05, 0.3)
    return y


def war_horn(seed: int, i: int) -> np.ndarray:
    """Das Horn ruft seine Leute: zwei kurze Stöße und ein langer, der sich zur Quinte hebt und am Ende bricht."""
    y = np.zeros(length(3.6))
    base = (130.81, 138.59)[i % 2]
    t = 0.0
    for k, (ratio, d, rise) in enumerate([(1.0, 0.26, 1.0), (1.0, 0.26, 1.0), (1.0, 1.6, 1.5)]):
        m = length(d)
        if rise > 1.0:
            f = base * curve(m, [(0.0, 0.95), (0.12, 1.0), (0.35, 1.0), (0.5, rise), (0.92, rise), (1.0, rise * 0.94)])
        else:
            f = base * ratio * curve(m, [(0.0, 0.95), (0.15, 1.0), (1.0, 1.0)])
        a = curve(m, [(0.0, 0.0), (0.1, 1.0), (0.85, 0.9), (1.0, 0.0)])
        mix_at(y, horn(f, a, seed + k), t, 1.0)
        t += d + 0.08
    return y


# =====================================================================================
# Zu Fuß
# =====================================================================================

def swing(seed: int, i: int) -> np.ndarray:
    """Der Morgenstern saust: ein schweres, tiefes Sausen, dabei klirrt das Kettenhemd."""
    r = rng(seed)
    y = np.zeros(length(0.8))
    mix_at(y, whoosh(r, 0.5, 420.0 + 40.0 * i, 160.0, 1.1, 0.55), 0.0, 1.0)
    mix_at(y, whoosh(r, 0.45, 1500.0, 600.0, 1.4, 0.55), 0.03, 0.25)
    mix_at(y, mail(r, 0.5, 140.0), 0.0, 0.3)
    return y


def smash(seed: int, i: int) -> np.ndarray:
    """Der Schmetterschlag: der Morgenstern fährt in den Boden – ein gewaltiger Schlag, die Erde reißt auf, Steine fliegen,
    das Eisen klingt nach."""
    r = rng(seed)
    y = np.zeros(length(1.8))
    mix_at(y, whoosh(r, 0.3, 380.0, 140.0, 1.1, 0.8), 0.0, 0.6)
    at = 0.24
    mix_at(y, thud(length(1.2), 82.0, 30.0, 0.42), at, 1.0)
    mix_at(y, impact(length(0.3), seed + 1, 160.0, 3200.0, 0.06), at, 0.9)
    mix_at(y, rubble(seed + 2, 1.2, 320.0), at + 0.01, 0.55)
    mix_at(y, anvil_ring(310.0 * (1.0 + 0.04 * i), 0.9, seed + 3, decay=0.35), at, 0.22)
    mix_at(y, plates(r, 0.5, 5), at, 0.4)
    return y


def whirl(seed: int, i: int) -> np.ndarray:
    """Der Wirbel: Kuno dreht sich, der Morgenstern kreist – mehrere Sausen im Takt der Drehung, dazwischen Rüstung."""
    r = rng(seed)
    y = np.zeros(length(1.3))
    for k in range(3):
        mix_at(y, whoosh(r, 0.42, 520.0 - 40 * k, 180.0, 1.1, 0.5), 0.05 + k * 0.34, 0.9 - 0.1 * k)
    mix_at(y, mail(r, 1.1, 180.0), 0.0, 0.4)
    mix_at(y, plates(r, 1.1, 6), 0.0, 0.35)
    return y


def bash(seed: int, i: int) -> np.ndarray:
    """Der Schildstoß: der Schild (Holz unter Eisen und Leder) prallt schwer auf – dumpf, dann der Eisenbuckel."""
    r = rng(seed)
    y = np.zeros(length(0.9))
    mix_at(y, thud(length(0.45), 130.0, 60.0, 0.12), 0.0, 1.0)
    mix_at(y, impact(length(0.2), seed + 1, 200.0, 1800.0, 0.035), 0.0, 0.9)
    mix_at(y, wood_knock(r, 0.35, 0.8), 0.002, 0.6)
    mix_at(y, clank(r, r.uniform(640.0, 760.0), 0.45, 0.14, 0.8), 0.004, 0.45)
    mix_at(y, voice(118.0, 0.28, seed + 4, "u", [(0.0, 1.0), (1.0, 0.86)], [(0.0, 0.0), (0.1, 1.0), (1.0, 0.0)]), 0.0, 0.45)
    return y


def fall(seed: int, i: int) -> np.ndarray:
    """Der Sturz vom Pferd: ein Stöhnen, Platten krachen auf den Boden, das Kettenhemd rasselt, dann liegt er still."""
    r = rng(seed)
    y = np.zeros(length(2.2))
    mix_at(y, voice(122.0, 0.5, seed, "o", [(0.0, 1.1), (1.0, 0.82)], [(0.0, 0.0), (0.1, 1.0), (1.0, 0.0)], rough=0.06), 0.0, 0.8)
    at = 0.42
    mix_at(y, thud(length(0.9), 96.0, 36.0, 0.3), at, 1.0)
    mix_at(y, impact(length(0.3), seed + 1, 180.0, 2600.0, 0.05), at, 0.8)
    mix_at(y, clatter(r, 1.1, 9, 420.0, 1300.0), at, 0.7)
    mix_at(y, mail(r, 0.9, 320.0), at, 0.6)
    mix_at(y, rubble(seed + 3, 0.6, 140.0), at, 0.3)
    return y


def hurt(seed: int, i: int) -> np.ndarray:
    """Schmerz: ein Treffer auf die Platten (Klirren) und ein kurzes, raues Grunzen."""
    r = rng(seed)
    y = np.zeros(length(0.7))
    mix_at(y, clank(r, r.uniform(420.0, 620.0), 0.5, 0.16, 0.9), 0.0, 0.6)
    mix_at(y, voice(112.0 + 10.0 * i, 0.36, seed + 1, ("u", "o", "a")[i % 3], [(0.0, 1.12), (1.0, 0.9)],
                    [(0.0, 0.0), (0.12, 1.0), (1.0, 0.0)], rough=0.06), 0.03, 1.0)
    mix_at(y, mail(r, 0.35, 160.0), 0.0, 0.3)
    return y


def death(seed: int, i: int) -> np.ndarray:
    """Der Tod: ein langes Stöhnen, das absinkt; er bricht zusammen, die Rüstung fällt klirrend in sich, ein letzter Schlag."""
    r = rng(seed)
    y = np.zeros(length(4.0))
    mix_at(y, voice(118.0, 1.6, seed, "o", [(0.0, 1.05), (0.3, 1.0), (1.0, 0.62)], [(0.0, 0.0), (0.1, 1.0), (0.6, 0.7), (1.0, 0.0)],
                    rough=0.07, breath=0.35), 0.0, 1.0)
    mix_at(y, clatter(r, 1.4, 12, 380.0, 1200.0), 1.2, 0.8)
    mix_at(y, mail(r, 1.3, 300.0), 1.2, 0.6)
    mix_at(y, thud(length(1.2), 88.0, 32.0, 0.4), 1.5, 1.0)
    mix_at(y, impact(length(0.3), seed + 2, 160.0, 2400.0, 0.06), 1.5, 0.7)
    mix_at(y, clank(r, 470.0, 1.2, 0.5, 0.7), 2.3, 0.3)       # der Helm rollt aus
    return y


def step(seed: int, i: int) -> np.ndarray:
    """Ein Schritt zu Fuß: der Eisenschuh setzt auf, die Platten klappern leise, das Kettenhemd rieselt."""
    r = rng(seed)
    y = np.zeros(length(0.5))
    mix_at(y, thud(length(0.3), 85.0, 45.0, 0.07), 0.0, 1.0)
    mix_at(y, impact(length(0.1), seed + 1, 300.0, 2500.0, 0.015), 0.0, 0.5)
    mix_at(y, plates(r, 0.25, 2, 520.0, 900.0, 0.05), 0.01, 0.4)
    mix_at(y, mail(r, 0.3, 120.0), 0.01, 0.35)
    return y


# =====================================================================================
# Banner und Morgenstern
# =====================================================================================

def caw(seed: int, f0: float) -> np.ndarray:
    """Ein Rabe krächzt („kraa“): rau, nasal, die Tonhöhe fällt am Ende."""
    n = length(0.42)
    f = f0 * curve(n, [(0.0, 0.9), (0.15, 1.05), (0.7, 1.0), (1.0, 0.8)])
    a = curve(n, [(0.0, 0.0), (0.05, 1.0), (0.7, 0.8), (1.0, 0.0)])
    y = pulse_voice(f, a, seed, "a", size=0.42, jitter=0.09, shimmer=0.25, sub=0.35, breath=0.25, bright=6000.0)
    return filt(y, "high", 300.0, SR, 2)


def tear(seed: int, i: int) -> np.ndarray:
    """Das Rabenbanner zerreißt: Tuch reißt in Stößen (ein trockenes Ratschen), ein Fetzen flattert, ein Rabe krächzt."""
    r = rng(seed)
    y = np.zeros(length(2.0))
    n = length(0.75)
    t = np.arange(n) / SR
    rate = 60.0 + 90.0 * t / t[-1]                      # die Fäden reißen immer schneller
    pulses = 0.5 + 0.5 * np.sign(np.sin(2 * np.pi * np.cumsum(rate) / SR))
    rip = filt(r.standard_normal(n), "band", [900.0, 6500.0], SR, 2) * (0.35 + 0.65 * pulses)
    rip *= curve(n, [(0.0, 0.0), (0.05, 0.7), (0.5, 1.0), (0.85, 0.8), (1.0, 0.0)])
    mix_at(y, filt(rip, "low", 5200.0, SR, 2), 0.0, 0.7)
    mix_at(y, whoosh(r, 0.4, 900.0, 400.0, 1.5, 0.4), 0.7, 0.35)  # der Fetzen flattert
    mix_at(y, caw(seed + 4, 520.0 + 30.0 * i), 0.95, 1.0)
    mix_at(y, caw(seed + 5, 480.0 + 30.0 * i), 1.42, 0.75)
    return y


def star_smash(seed: int, i: int) -> np.ndarray:
    """Der Schmetterschlag des Morgensterns in der Hand des Spielers: ein schwerer Schlag in den Boden, Erde und Steine."""
    r = rng(seed)
    y = np.zeros(length(1.4))
    mix_at(y, whoosh(r, 0.22, 420.0, 160.0, 1.1, 0.8), 0.0, 0.5)
    at = 0.16
    mix_at(y, thud(length(0.9), 90.0, 34.0, 0.32), at, 1.0)
    mix_at(y, impact(length(0.25), seed + 1, 180.0, 3400.0, 0.05), at, 0.85)
    mix_at(y, rubble(seed + 2, 0.9, 260.0), at + 0.01, 0.5)
    mix_at(y, anvil_ring(360.0 * (1.0 + 0.05 * i), 0.7, seed + 3, decay=0.28), at, 0.18)
    return y


# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil), Ziel-Pegel dBFS)
SYNTH = {
    "robber_knight_awaken": (awaken, 1, (1.8, 0.35), -13.0),
    "robber_knight_charge": (charge, 2, (1.4, 0.3), -14.0),
    "robber_knight_lance_hit": (lance_hit, 3, (1.0, 0.22), -14.0),
    "robber_knight_lance_break": (lance_break, 2, (1.2, 0.25), -14.0),
    "robber_knight_stomp": (stomp, 3, (1.2, 0.25), -14.0),
    "robber_knight_horn": (war_horn, 2, (2.4, 0.4), -13.0),
    "robber_knight_swing": (swing, 3, (0.8, 0.15), -18.0),
    "robber_knight_smash": (smash, 2, (1.2, 0.25), -13.0),
    "robber_knight_whirl": (whirl, 2, (0.9, 0.18), -16.0),
    "robber_knight_bash": (bash, 3, (0.9, 0.18), -15.0),
    "robber_knight_fall": (fall, 2, (1.2, 0.25), -14.0),
    "robber_knight_hurt": (hurt, 3, (0.9, 0.2), -16.0),
    "robber_knight_death": (death, 1, (1.8, 0.35), -14.0),
    "robber_knight_step": (step, 3, (0.5, 0.12), -22.0),
    "raven_banner_tear": (tear, 2, (1.4, 0.3), -16.0),
    "morning_star_smash": (star_smash, 3, (1.0, 0.2), -15.0),
}

# Ereignis: (Klang, Reichweite)
EVENTS = {
    "entity.robber_knight.awaken": ("robber_knight_awaken", 64),
    "entity.robber_knight.charge": ("robber_knight_charge", 56),
    "entity.robber_knight.lance_hit": ("robber_knight_lance_hit", 40),
    "entity.robber_knight.lance_break": ("robber_knight_lance_break", 48),
    "entity.robber_knight.stomp": ("robber_knight_stomp", 40),
    "entity.robber_knight.horn": ("robber_knight_horn", 80),
    "entity.robber_knight.swing": ("robber_knight_swing", 24),
    "entity.robber_knight.smash": ("robber_knight_smash", 48),
    "entity.robber_knight.whirl": ("robber_knight_whirl", 32),
    "entity.robber_knight.bash": ("robber_knight_bash", 32),
    "entity.robber_knight.fall": ("robber_knight_fall", 48),
    "entity.robber_knight.hurt": ("robber_knight_hurt", 32),
    "entity.robber_knight.death": ("robber_knight_death", 64),
    "entity.robber_knight.step": ("robber_knight_step", 16),
    "block.raven_banner.tear": ("raven_banner_tear", 40),
    "item.morning_star.smash": ("morning_star_smash", 24),
}

# sehr tiefe Klänge bekommen Obertöne ihres Basses, damit man sie auch auf kleinen Lautsprechern hört
AUDIBLE = {"robber_knight_stomp", "robber_knight_smash", "robber_knight_fall", "robber_knight_death", "morning_star_smash",
           "robber_knight_lance_hit"}


def files_of(name: str) -> list[str]:
    return [f"kingdomsatwar:entity/castle/{name}{i + 1}" for i in range(SYNTH[name][1])]


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    for i in range(count):
        x = fn(18100 + i * 37 + zlib.crc32(name.encode()) % 997, i)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 2900 + i, stereo=False, damping=6000), space[1])
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
