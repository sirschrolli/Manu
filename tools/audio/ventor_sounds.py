#!/usr/bin/env python3
"""
Die Klänge der Arena der Prüfungen (1.9): Ventor, der Gladiator der Winde, das Arenasiegel, der Sturmdreizack und das
Wurfnetz.

Ventor ist ein Standbild aus Bronze, das der Wind füllt: Wenn er getroffen wird, klingt das Erz (tief, unharmonisch, lange
nachschwingend), und immer heult und braust der Wirbel unter ihm. Seine Stimme hallt dumpf aus dem Helm. Im Einzelnen:

  * das Aufsteigen (ein Wirbel erhebt sich, Sand rieselt, oben ein Windstoß), sein Ruf (ein Kampfschrei aus dem Helm, das Erz
    klingt mit), die Ränge (eine Menge aus Geisterstimmen jubelt, fern und hallend), sein Atem (der Wind heult leise um ihn);
  * Schmerz (Bronze klingt, der Wind stockt), der Tod (der Wind erstirbt, fallend), der Sturz des Standbilds (ein schwerer
    Schlag, das Erz dröhnt, Stücke klirren);
  * der Wind sammelt sich (anschwellend), die Böe (ein mächtiger Stoß), der Aufprall an der Wand, der Dreizackstoß (ein
    schnelles Sausen, Erz schabt), der Treffer (Metall auf Metall, dumpf darunter), das Netz (schwirrend über dem Kopf
    geschwungen, die Gewichte klicken), der Wurf (der Dreizack pfeift davon), die Windhosen (mehrere heulende Wirbel), der
    Aufstieg und der Sturzflug (steigendes und fallendes Pfeifen), der Aufschlag (ein dumpfer Donner, eine Böe, Sand), der
    Zyklon (ein tosender, pulsierender Wirbel), der entfesselte Sturm (Brüllen, Heulen, die Menge tobt), die Sturmböe (ein
    heranrauschender Stoß);
  * eine Windhose heult; das Arenasiegel: ein Käfig ist bezwungen (ein heller Glockenton, ein Windhauch), alle sind bezwungen
    (ein Hornruf, Glocken, die Menge jubelt); die Böe des Sturmdreizacks; das Wurfnetz verfängt sich.

Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py); es werden keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/ventor_sounds.py [--preview /pfad] [--only name …]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/arena/<klang><n>.ogg
    Einträge "entity.ventor.*", "entity.wind_vortex.howl", "block.arena_seal.*", "item.storm_trident.burst",
    "item.gladiator_net.snare" in assets/kingdomsatwar/sounds.json
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, audible_bass, bell, curve, env_ad, filt, fit_level, glide, rng, room, soft_clip, tone,  # noqa: E402
                          trim_tail, fade_edges)
from creature_sounds import horn  # noqa: E402
from dsp import write_ogg  # noqa: E402
from forge_sounds import crackle, impact, mix_at, thud  # noqa: E402
from hour_warden_sounds import clank, clatter, whoosh  # noqa: E402
from kraken_sounds import moving_resonator  # noqa: E402
from monster_sounds import pulse_voice  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/arena"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100


# =====================================================================================
# Bausteine
# =====================================================================================

def length(dur: float) -> int:
    return int(dur * SR)


def gale(r, dur: float, lo: float, hi: float, speed: float, q: float = 3.0, env=None) -> np.ndarray:
    """Wind, der kreist und braust: Rauschen durch eine Resonanz, die zwischen lo und hi wandert."""
    n = length(dur)
    t = np.arange(n) / SR
    sweep = np.sqrt(lo * hi) * (hi / lo) ** (0.5 * np.sin(2 * np.pi * speed * t + r.uniform(0, 6)))
    y = moving_resonator(r.standard_normal(n), sweep, q)
    y += filt(r.standard_normal(n), "band", [lo * 0.7, hi * 1.4], SR, 2) * 0.35
    return y * curve(n, env or [(0.0, 0.0), (0.2, 1.0), (0.8, 1.0), (1.0, 0.0)])


def howl(r, dur: float, f0: float, f1: float, q: float = 18.0, env=None) -> np.ndarray:
    """Ein Heulen: schmales Rauschband, dessen Mitte von f0 nach f1 gleitet (Wind in einer Ritze), mit leichtem Flattern."""
    n = length(dur)
    t = np.arange(n) / SR
    f = glide(n, f0, f1, 1.0) * (1.0 + 0.03 * np.sin(2 * np.pi * r.uniform(4.0, 7.0) * t))
    y = moving_resonator(r.standard_normal(n), f, q)
    return y * curve(n, env or [(0.0, 0.0), (0.25, 1.0), (0.75, 0.9), (1.0, 0.0)])


def bronze(r, f0: float, dur: float, decay: float = 0.9, gain: float = 1.0) -> np.ndarray:
    """Angeschlagenes Erz eines Standbilds: tief, unharmonisch, lange nachklingend."""
    y = clank(r, f0, dur, decay, 0.7) * gain
    y += clank(r, f0 * 1.51, dur, decay * 0.7, 0.4) * 0.35 * gain
    return y


def helm_voice(f0: float, dur: float, seed: int, vowel: str = "a", shape=None, amp=None, rough: float = 0.05) -> np.ndarray:
    """Eine Männerstimme aus dem Helm: rau, dumpf (das Visier dämpft), mit einem metallischen Mitklingen."""
    n = length(dur)
    f = f0 * curve(n, shape or [(0.0, 0.92), (0.15, 1.0), (1.0, 1.0)])
    a = curve(n, amp or [(0.0, 0.0), (0.08, 1.0), (0.75, 0.85), (1.0, 0.0)])
    v = pulse_voice(f, a, seed, vowel, size=1.0, jitter=rough, shimmer=0.1, sub=0.15, breath=0.25, bright=2600.0)
    from ambience_dsp import resonator
    ring = resonator(v, 830.0, 22.0, SR) * 0.25 + resonator(v, 1460.0, 26.0, SR) * 0.15
    return filt(v, "low", 2400.0, SR, 2) + ring


def crowd(r, dur: float, voices: int = 26, swell=None) -> np.ndarray:
    """Die Ränge jubeln: viele Stimmen (Rufe, die anschwellen und abbrechen), ein Rauschen aus Händen und Atem darunter."""
    n = length(dur)
    y = np.zeros(n)
    for k in range(voices):
        start = r.uniform(0.0, dur * 0.35)
        d = r.uniform(0.6, dur - start)
        m = length(d)
        f0 = r.uniform(110.0, 260.0)
        shape = [(0.0, 0.9), (0.3, r.uniform(1.0, 1.15)), (1.0, r.uniform(0.85, 1.05))]
        amp = [(0.0, 0.0), (r.uniform(0.05, 0.25), 1.0), (r.uniform(0.5, 0.8), 0.8), (1.0, 0.0)]
        f = f0 * curve(m, shape)
        a = curve(m, amp)
        v = pulse_voice(f, a, int(r.integers(1 << 30)), r.choice(["a", "o", "e"]), size=1.0, jitter=0.04, shimmer=0.12, sub=0.05,
                        breath=0.35, bright=3200.0)
        mix_at(y, v, start, r.uniform(0.25, 0.6))
    hands = np.zeros(n)
    for _ in range(int(dur * 70)):
        at = r.uniform(0.0, dur * 0.95)
        mix_at(hands, impact(length(0.03), int(r.integers(1 << 30)), 900.0, 5200.0, 0.01), at, r.uniform(0.1, 0.3))
    y += hands
    y += filt(r.standard_normal(n), "band", [300.0, 2800.0], SR, 2) * 0.18
    return y * curve(n, swell or [(0.0, 0.0), (0.2, 1.0), (0.7, 0.9), (1.0, 0.0)])


def chime(f0: float, dur: float, seed: int, decay: float = 1.2) -> np.ndarray:
    """Ein heller Glockenton (Kupfer und Kristall)."""
    return bell(f0, dur, SR, ratios=(1.0, 2.0, 3.01, 4.2), amps=(1.0, 0.4, 0.2, 0.1), decay=decay, seed=seed)


# =====================================================================================
# Ventor
# =====================================================================================

def awaken(seed: int, i: int) -> np.ndarray:
    """Er steigt auf: ein Wirbel erhebt sich (das Brausen steigt), Sand rieselt, oben ein Windstoß und ein Heulen."""
    r = rng(seed)
    y = np.zeros(length(3.2))
    mix_at(y, gale(r, 2.6, 180.0, 1400.0, 0.9, env=[(0.0, 0.0), (0.6, 0.7), (0.85, 1.0), (1.0, 0.0)]), 0.0, 0.9)
    mix_at(y, howl(r, 2.4, 300.0, 900.0, env=[(0.0, 0.0), (0.7, 1.0), (1.0, 0.0)]), 0.2, 0.35)
    mix_at(y, crackle(length(2.0), seed + 3, 260.0, 900.0, 6000.0) * curve(length(2.0), [(0.0, 0.0), (0.5, 1.0), (1.0, 0.0)]), 0.3, 0.25)
    mix_at(y, whoosh(r, 0.9, 220.0, 1100.0, 1.2, 0.35), 2.2, 0.9)
    return y


def shout(seed: int, i: int) -> np.ndarray:
    """Sein Ruf an die Ränge: ein Kampfschrei aus dem Helm (zwei Silben, die zweite steigt), das Erz klingt mit."""
    r = rng(seed)
    y = np.zeros(length(1.8))
    mix_at(y, helm_voice(118.0, 0.45, seed, "a", [(0.0, 0.9), (0.3, 1.0), (1.0, 0.96)]), 0.0, 0.9)
    mix_at(y, helm_voice(124.0, 0.9, seed + 1, "e", [(0.0, 0.95), (0.25, 1.12), (0.8, 1.15), (1.0, 1.0)],
                         [(0.0, 0.0), (0.06, 1.0), (0.8, 0.9), (1.0, 0.0)]), 0.48, 1.0)
    mix_at(y, bronze(r, 220.0, 1.2, 0.8), 0.48, 0.25)
    return y


def crowd_cheer(seed: int, i: int) -> np.ndarray:
    """Die Ränge jubeln: eine Menge aus Geisterstimmen, fern und hallend."""
    r = rng(seed)
    y = crowd(r, 3.6, 28)
    return filt(y, "band", [180.0, 4200.0], SR, 2)


def ambient(seed: int, i: int) -> np.ndarray:
    """Sein Atem: der Wind heult leise um ihn, darunter ein Brausen; dann und wann knarrt das Erz."""
    r = rng(seed)
    y = np.zeros(length(2.6))
    mix_at(y, gale(r, 2.6, 160.0, 700.0, 0.6), 0.0, 0.7)
    mix_at(y, howl(r, 1.8, 420.0 + 60.0 * i, 610.0 + 40.0 * i), 0.4, 0.35)
    mix_at(y, bronze(r, 160.0, 0.9, 0.5), 1.4, 0.08)
    return y


def hurt(seed: int, i: int) -> np.ndarray:
    """Schmerz: das Erz klingt hell auf, der Wind stockt (ein kurzes Fauchen)."""
    r = rng(seed)
    y = np.zeros(length(1.0))
    mix_at(y, bronze(r, 300.0 + 40.0 * i, 0.9, 0.55), 0.0, 1.0)
    mix_at(y, impact(length(0.15), seed + 1, 300.0, 3000.0, 0.03), 0.0, 0.5)
    mix_at(y, whoosh(r, 0.4, 900.0, 300.0, 1.4, 0.2), 0.05, 0.45)
    return y


def death(seed: int, i: int) -> np.ndarray:
    """Der Wind erstirbt: ein langes, fallendes Heulen, das Brausen wird leiser und leiser, das Erz stöhnt."""
    r = rng(seed)
    y = np.zeros(length(3.4))
    mix_at(y, howl(r, 3.0, 900.0, 140.0, env=[(0.0, 0.0), (0.1, 1.0), (0.6, 0.6), (1.0, 0.0)]), 0.0, 0.8)
    mix_at(y, gale(r, 3.2, 120.0, 900.0, 0.5, env=[(0.0, 1.0), (0.5, 0.5), (1.0, 0.0)]), 0.0, 0.7)
    mix_at(y, bronze(r, 140.0, 2.2, 1.6), 0.4, 0.3)
    return y


def fall(seed: int, i: int) -> np.ndarray:
    """Das Standbild stürzt: ein schwerer, tiefer Schlag, das Erz dröhnt lange, Stücke klirren und rollen."""
    r = rng(seed)
    y = np.zeros(length(2.4))
    mix_at(y, thud(length(1.0), 90.0, 38.0, 0.35), 0.0, 1.0)
    mix_at(y, bronze(r, 120.0, 2.2, 1.5), 0.0, 0.7)
    mix_at(y, bronze(r, 260.0, 1.4, 0.8), 0.02, 0.35)
    mix_at(y, clatter(r, 1.4, 7, 600.0, 1800.0), 0.12, 0.45)
    mix_at(y, crackle(length(0.8), seed + 5, 220.0, 600.0, 4000.0) * env_ad(length(0.8), SR, 0.002, 0.3), 0.0, 0.3)
    return y


def gather(seed: int, i: int) -> np.ndarray:
    """Der Wind sammelt sich: ein Brausen, das anschwillt und steigt."""
    r = rng(seed)
    y = np.zeros(length(1.1))
    mix_at(y, gale(r, 1.0, 250.0, 1500.0, 1.6, env=[(0.0, 0.0), (0.8, 1.0), (1.0, 0.0)]), 0.0, 0.9)
    mix_at(y, howl(r, 0.9, 400.0, 1100.0, env=[(0.0, 0.0), (0.85, 1.0), (1.0, 0.0)]), 0.1, 0.35)
    return y


def gust(seed: int, i: int) -> np.ndarray:
    """Die Böe: ein mächtiger Stoß (ein tiefer Wumms, dann ein breites Rauschen, das davonzieht)."""
    r = rng(seed)
    y = np.zeros(length(1.5))
    mix_at(y, thud(length(0.4), 110.0, 50.0, 0.12), 0.0, 0.6)
    mix_at(y, whoosh(r, 1.3, 1600.0, 260.0, 0.9, 0.12), 0.0, 1.0)
    mix_at(y, whoosh(r, 1.1, 3200.0, 700.0, 1.4, 0.1), 0.02, 0.4)
    mix_at(y, crackle(length(0.9), seed + 2, 240.0, 1200.0, 7000.0) * env_ad(length(0.9), SR, 0.01, 0.4), 0.05, 0.2)
    return y


def slam(seed: int, i: int) -> np.ndarray:
    """Jemand prallt gegen eine Wand: ein dumpfer Schlag, Steine knirschen."""
    r = rng(seed)
    y = np.zeros(length(0.8))
    mix_at(y, thud(length(0.5), 140.0, 60.0, 0.12), 0.0, 1.0)
    mix_at(y, impact(length(0.25), seed + 1, 200.0, 2400.0, 0.05), 0.0, 0.7)
    mix_at(y, crackle(length(0.5), seed + 2, 300.0, 900.0, 5000.0) * env_ad(length(0.5), SR, 0.002, 0.18), 0.01, 0.3)
    return y * (1.0 + 0.0 * r.random())


def thrust(seed: int, i: int) -> np.ndarray:
    """Der Dreizackstoß: ein schnelles, scharfes Sausen, das Erz des Arms schabt."""
    r = rng(seed)
    y = np.zeros(length(0.8))
    mix_at(y, whoosh(r, 0.45, 700.0, 2600.0, 1.6, 0.6), 0.0, 1.0)
    mix_at(y, clank(r, 1900.0, 0.4, 0.12, 0.8), 0.32, 0.25)
    return y


def stab(seed: int, i: int) -> np.ndarray:
    """Der Treffer: Metall auf Metall (drei Zinken), darunter ein dumpfer Schlag."""
    r = rng(seed)
    y = np.zeros(length(0.9))
    mix_at(y, thud(length(0.4), 130.0, 60.0, 0.1), 0.0, 0.8)
    for k, f in enumerate((1250.0, 1610.0, 2050.0)):
        mix_at(y, clank(r, f * r.uniform(0.97, 1.03), 0.6, 0.18, 0.9), 0.004 * k, 0.4)
    mix_at(y, impact(length(0.2), seed + 3, 600.0, 6000.0, 0.03), 0.0, 0.6)
    return y


def net_swing(seed: int, i: int) -> np.ndarray:
    """Das Netz schwirrt über dem Kopf: Seile sausen im Kreis (schneller werdend), die Bleigewichte klicken."""
    r = rng(seed)
    y = np.zeros(length(1.3))
    t = 0.0
    period = 0.32
    while t < 1.05:
        mix_at(y, whoosh(r, 0.22, 500.0, 1300.0, 1.8, 0.5), t, 0.7)
        mix_at(y, clank(r, r.uniform(2400.0, 3200.0), 0.1, 0.03, 0.6), t + 0.1, 0.12)
        t += period
        period = max(0.17, period * 0.82)
    mix_at(y, whoosh(r, 0.35, 1200.0, 400.0, 1.4, 0.2), 1.05, 0.8)
    return y


def throw(seed: int, i: int) -> np.ndarray:
    """Der Wurf: ein Ausholen, dann pfeift der Dreizack davon (ein fallender Pfiff mit Windfaden)."""
    r = rng(seed)
    y = np.zeros(length(1.4))
    mix_at(y, whoosh(r, 0.35, 400.0, 1500.0, 1.4, 0.7), 0.0, 0.8)
    n = length(1.0)
    whistle = tone(glide(n, 2400.0, 1100.0, 0.8), curve(n, [(0.0, 0.0), (0.05, 1.0), (0.6, 0.5), (1.0, 0.0)]), SR,
                   harmonics=((1, 1.0), (2, 0.12)))
    mix_at(y, whistle * 0.25 + howl(r, 1.0, 2200.0, 900.0, 30.0) * 0.6, 0.3, 0.8)
    return y


def vortex(seed: int, i: int) -> np.ndarray:
    """Die Windhosen: mehrere heulende Wirbel erheben sich nacheinander, darunter ein Brausen."""
    r = rng(seed)
    y = np.zeros(length(2.8))
    for k in range(3):
        mix_at(y, howl(r, 1.8, 250.0 + 90.0 * k, 700.0 + 160.0 * k, env=[(0.0, 0.0), (0.4, 1.0), (1.0, 0.0)]), 0.25 * k, 0.5)
    mix_at(y, gale(r, 2.6, 150.0, 1100.0, 1.4), 0.0, 0.8)
    return y


def rise(seed: int, i: int) -> np.ndarray:
    """Er steigt in die Luft: ein steigendes Sausen und Pfeifen."""
    r = rng(seed)
    y = np.zeros(length(1.3))
    mix_at(y, whoosh(r, 1.1, 300.0, 1800.0, 1.3, 0.7), 0.0, 1.0)
    mix_at(y, howl(r, 1.1, 500.0, 1500.0, 20.0), 0.05, 0.4)
    return y


def dive(seed: int, i: int) -> np.ndarray:
    """Der Sturzflug: ein Pfeifen, das fällt und lauter wird."""
    r = rng(seed)
    y = np.zeros(length(1.1))
    mix_at(y, howl(r, 1.0, 2000.0, 500.0, 24.0, env=[(0.0, 0.2), (0.85, 1.0), (1.0, 0.0)]), 0.0, 0.8)
    mix_at(y, whoosh(r, 1.0, 1400.0, 300.0, 1.2, 0.85), 0.0, 0.9)
    return y


def impact_boom(seed: int, i: int) -> np.ndarray:
    """Der Aufschlag: ein dumpfer Donner, eine Böe nach allen Seiten, Sand prasselt."""
    r = rng(seed)
    y = np.zeros(length(2.0))
    mix_at(y, thud(length(1.2), 80.0, 32.0, 0.4), 0.0, 1.0)
    mix_at(y, impact(length(0.4), seed + 1, 120.0, 1800.0, 0.08), 0.0, 0.8)
    mix_at(y, whoosh(r, 1.2, 1400.0, 200.0, 1.0, 0.1), 0.02, 0.8)
    mix_at(y, crackle(length(1.4), seed + 2, 320.0, 700.0, 5200.0) * curve(length(1.4), [(0.0, 0.0), (0.05, 1.0), (1.0, 0.0)]),
           0.05, 0.4)
    mix_at(y, bronze(r, 180.0, 1.2, 0.7), 0.0, 0.25)
    return y


def cyclone(seed: int, i: int) -> np.ndarray:
    """Der Zyklon: ein tosender Wirbel, der pulsiert (schneller werdend), darin heult es."""
    r = rng(seed)
    dur = 3.0
    n = length(dur)
    t = np.arange(n) / SR
    rate = 2.5 + 4.5 * np.clip(t / dur, 0, 1)
    pulse = 0.6 + 0.4 * np.sin(2 * np.pi * np.cumsum(rate) / SR)
    y = gale(r, dur, 200.0, 1800.0, 1.8, env=[(0.0, 0.0), (0.15, 1.0), (0.85, 1.0), (1.0, 0.0)]) * pulse
    mix_at(y, howl(r, dur, 500.0, 900.0, 16.0) * pulse, 0.0, 0.5)
    return y


def rage(seed: int, i: int) -> np.ndarray:
    """Der Sturm bricht los: er brüllt aus dem Helm, der Wind heult auf, die Menge tobt."""
    r = rng(seed)
    y = np.zeros(length(3.4))
    mix_at(y, helm_voice(96.0, 1.4, seed, "o", [(0.0, 0.9), (0.3, 1.08), (1.0, 0.92)], rough=0.09), 0.0, 1.0)
    mix_at(y, gale(r, 3.0, 160.0, 1600.0, 1.2, env=[(0.0, 0.0), (0.3, 1.0), (0.8, 0.8), (1.0, 0.0)]), 0.2, 0.9)
    mix_at(y, howl(r, 2.4, 300.0, 1200.0), 0.4, 0.45)
    mix_at(y, crowd(r, 2.8, 20), 0.6, 0.35)
    return y


def storm_gust(seed: int, i: int) -> np.ndarray:
    """Die Sturmböe naht: ein Rauschen, das von fern heranzieht und anschwillt."""
    r = rng(seed)
    y = np.zeros(length(1.7))
    mix_at(y, gale(r, 1.6, 200.0, 1200.0, 0.8, env=[(0.0, 0.0), (0.9, 1.0), (1.0, 0.0)]), 0.0, 1.0)
    mix_at(y, howl(r, 1.5, 300.0, 900.0, env=[(0.0, 0.0), (0.9, 1.0), (1.0, 0.0)]), 0.05, 0.3)
    return y


def vortex_howl(seed: int, i: int) -> np.ndarray:
    """Eine Windhose heult (kleiner als er selbst, höher)."""
    r = rng(seed)
    y = np.zeros(length(1.7))
    mix_at(y, howl(r, 1.6, 600.0 + 80.0 * i, 950.0 + 60.0 * i, 14.0), 0.0, 0.7)
    mix_at(y, gale(r, 1.6, 300.0, 1600.0, 2.0), 0.0, 0.6)
    return y


# =====================================================================================
# Siegel, Dreizack, Netz
# =====================================================================================

def seal_cage(seed: int, i: int) -> np.ndarray:
    """Ein Käfig ist bezwungen: ein heller Glockenton (zwei Töne, eine Quinte), ein Windhauch zieht zum Podest."""
    r = rng(seed)
    y = np.zeros(length(2.0))
    mix_at(y, whoosh(r, 0.9, 400.0, 1400.0, 1.4, 0.75), 0.0, 0.5)
    mix_at(y, chime(1046.5, 1.6, seed), 0.55, 0.7)
    mix_at(y, chime(1568.0, 1.4, seed + 1), 0.7, 0.45)
    return y


def seal_ready(seed: int, i: int) -> np.ndarray:
    """Alle Käfige sind bezwungen: ein Hornruf (Quarte aufwärts), Glocken, die Menge jubelt."""
    r = rng(seed)
    y = np.zeros(length(4.2))
    t = 0.0
    for k, (ratio, d) in enumerate([(1.0, 0.4), (4.0 / 3.0, 1.2)]):
        m = length(d)
        f = 196.0 * ratio * curve(m, [(0.0, 0.96), (0.1, 1.0), (1.0, 1.0)])
        a = curve(m, [(0.0, 0.0), (0.1, 1.0), (0.8, 0.9), (1.0, 0.0)])
        mix_at(y, horn(f, a, seed + k), t, 0.8)
        t += d + 0.08
    for k, f in enumerate((784.0, 1046.5, 1318.5)):
        mix_at(y, chime(f, 1.8, seed + 10 + k), 0.4 + 0.12 * k, 0.35)
    mix_at(y, crowd(r, 3.0, 24), 1.0, 0.5)
    return y


def trident_burst(seed: int, i: int) -> np.ndarray:
    """Die Böe des Sturmdreizacks beim Aufschlag: ein Wumms, ein Windstoß, das Erz klingt."""
    r = rng(seed)
    y = np.zeros(length(1.1))
    mix_at(y, thud(length(0.35), 150.0, 70.0, 0.1), 0.0, 0.7)
    mix_at(y, whoosh(r, 0.9, 1800.0, 400.0, 1.1, 0.1), 0.0, 0.9)
    mix_at(y, clank(r, 1500.0, 0.8, 0.3, 0.9), 0.0, 0.25)
    return y


def net_snare(seed: int, i: int) -> np.ndarray:
    """Das Netz verfängt sich: Seile schnappen straff, die Gewichte klirren."""
    r = rng(seed)
    y = np.zeros(length(0.8))
    mix_at(y, impact(length(0.12), seed + 1, 300.0, 2600.0, 0.03), 0.0, 0.8)
    mix_at(y, whoosh(r, 0.25, 900.0, 300.0, 1.6, 0.2), 0.0, 0.5)
    mix_at(y, clatter(r, 0.6, 4, 1800.0, 3200.0), 0.05, 0.3)
    return y


# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil), Ziel-Pegel dBFS)
SYNTH = {
    "ventor_awaken": (awaken, 1, (2.4, 0.4), -13.0),
    "ventor_shout": (shout, 2, (2.0, 0.35), -13.0),
    "ventor_crowd": (crowd_cheer, 2, (3.0, 0.5), -15.0),
    "ventor_ambient": (ambient, 3, (1.6, 0.3), -20.0),
    "ventor_hurt": (hurt, 3, (1.2, 0.25), -15.0),
    "ventor_death": (death, 1, (2.4, 0.4), -14.0),
    "ventor_fall": (fall, 2, (1.6, 0.3), -14.0),
    "ventor_gather": (gather, 2, (1.2, 0.25), -16.0),
    "ventor_gust": (gust, 3, (1.4, 0.3), -13.0),
    "ventor_slam": (slam, 3, (1.0, 0.2), -15.0),
    "ventor_thrust": (thrust, 3, (0.9, 0.18), -16.0),
    "ventor_stab": (stab, 3, (1.0, 0.2), -14.0),
    "ventor_net": (net_swing, 2, (1.0, 0.2), -16.0),
    "ventor_throw": (throw, 2, (1.2, 0.22), -15.0),
    "ventor_vortex": (vortex, 2, (1.8, 0.35), -14.0),
    "ventor_rise": (rise, 2, (1.2, 0.25), -15.0),
    "ventor_dive": (dive, 2, (1.2, 0.25), -14.0),
    "ventor_impact": (impact_boom, 2, (1.8, 0.35), -13.0),
    "ventor_cyclone": (cyclone, 2, (1.6, 0.3), -14.0),
    "ventor_rage": (rage, 1, (2.6, 0.4), -13.0),
    "ventor_storm": (storm_gust, 2, (2.0, 0.35), -15.0),
    "wind_vortex_howl": (vortex_howl, 3, (1.4, 0.3), -18.0),
    "arena_seal_cage": (seal_cage, 2, (2.4, 0.4), -15.0),
    "arena_seal_ready": (seal_ready, 1, (3.0, 0.45), -13.0),
    "storm_trident_burst": (trident_burst, 3, (1.2, 0.25), -15.0),
    "gladiator_net_snare": (net_snare, 2, (0.8, 0.15), -17.0),
}

# Ereignis: (Klang, Reichweite)
EVENTS = {
    "entity.ventor.awaken": ("ventor_awaken", 64),
    "entity.ventor.shout": ("ventor_shout", 64),
    "entity.ventor.crowd": ("ventor_crowd", 64),
    "entity.ventor.ambient": ("ventor_ambient", 24),
    "entity.ventor.hurt": ("ventor_hurt", 32),
    "entity.ventor.death": ("ventor_death", 64),
    "entity.ventor.fall": ("ventor_fall", 48),
    "entity.ventor.gather": ("ventor_gather", 32),
    "entity.ventor.gust": ("ventor_gust", 48),
    "entity.ventor.slam": ("ventor_slam", 32),
    "entity.ventor.thrust": ("ventor_thrust", 32),
    "entity.ventor.stab": ("ventor_stab", 32),
    "entity.ventor.net": ("ventor_net", 32),
    "entity.ventor.throw": ("ventor_throw", 40),
    "entity.ventor.vortex": ("ventor_vortex", 48),
    "entity.ventor.rise": ("ventor_rise", 40),
    "entity.ventor.dive": ("ventor_dive", 48),
    "entity.ventor.impact": ("ventor_impact", 56),
    "entity.ventor.cyclone": ("ventor_cyclone", 48),
    "entity.ventor.rage": ("ventor_rage", 64),
    "entity.ventor.storm": ("ventor_storm", 56),
    "entity.wind_vortex.howl": ("wind_vortex_howl", 24),
    "block.arena_seal.cage": ("arena_seal_cage", 64),
    "block.arena_seal.ready": ("arena_seal_ready", 64),
    "item.storm_trident.burst": ("storm_trident_burst", 32),
    "item.gladiator_net.snare": ("gladiator_net_snare", 24),
}

# sehr tiefe Klänge bekommen Obertöne ihres Basses, damit man sie auch auf kleinen Lautsprechern hört
AUDIBLE = {"ventor_fall", "ventor_impact", "ventor_gust", "ventor_slam", "ventor_rage"}


def files_of(name: str) -> list[str]:
    return [f"kingdomsatwar:entity/arena/{name}{i + 1}" for i in range(SYNTH[name][1])]


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    for i in range(count):
        x = fn(19100 + i * 37 + zlib.crc32(name.encode()) % 997, i)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 3900 + i, stereo=False, damping=6000), space[1])
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
