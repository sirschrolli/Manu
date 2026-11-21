#!/usr/bin/env python3
"""
Eigene Klänge für Wesen, die sich bisher Klänge von Minecraft liehen, wo diese nicht recht passten.

Seit 1.1.3:
- Höhlentroll: ein großer, behäbiger Troll mit steinerner Haut – brummt und grummelt tief durch die Nase, grunzt bei Schmerz,
  stöhnt lange im Tod und fällt schwer; im Tageslicht knirscht und knackt er zu Stein; seine Keule saust und kracht, seine
  Schritte dröhnen auf Kies.
- Lindwurm: ein flügelloser Drache wie eine riesige Schlange – zischt und grollt, schnappt zu, kreischt rau, wenn er
  getroffen wird; seine Schuppen schaben über den Boden.

Seit 1.1.4 (bisher Fuchs, Rind, Esel, Papagei, Schildkröte, Fisch, Wächter …):
- Hirsch: leises, nasales Fiepen und Schnauben, ein gepresster Klagelaut, leichte Hufe auf Waldboden.
- Urelch: tiefe Grunzer, der Brunftruf (ein Grollen, das sich zu einem langen, hellen, rau unterlegten Pfeifen aufschwingt),
  Schnauben, Gebrüll, Kauen, das Krachen der Schaufeln, schwere Hufe.
- Gans: das nasale „a-honk“, ihr Zischen mit Rasseln aus der Kehle, Schreie und Flattern.
- Flamingo: Schmerz und Tod in derselben nasalen Stimme wie sein Schnattern (beast_sounds.py).
- Moorschnapper: kehliges Knarren mit Blasen, Fauchen, das harte Zuschnappen des Hornschnabels, Panzer und Schlamm.
- Kommandostab: ein kurzes Hornsignal (der Stab stimmt es je Befehl höher oder tiefer).
- Hai, Anglerfisch, Mantarochen, Riesenkrabbe (Schritte), Leuchtqualle (Schmerz, Tod): Bisse, Sog, Gischt, Blasen, Glas.
- Leerenschlund: Biss, Schmerz und Tod passend zu seinem Gurgeln und Ansaugen (void_creatures.py).

Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py, monster_sounds.py, dragon_sounds.py); keine fremden Dateien.
Wo Klänge von Minecraft gut passen (Wildschwein, Schneeleopard, Banditen, Soldaten), bleiben die Verweise in
tools/sound_events.py.

Aufruf (aus dem Projektordner):
    python3 tools/audio/creature_sounds.py [--only name ...] [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/<ordner>/<name><n>.ogg (entity/beast, entity/sea, entity/void, item)
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

from ambience_dsp import apply_room, curve, fade_edges, filt, formant_filter, rng, room, saw, tone, trim_tail  # noqa: E402
from beast_sounds import _honk  # noqa: E402
from dragon_sounds import fit, fit_lufs, gust, morph_voice, saturate, snort, sparkle  # noqa: E402
from dsp import write_ogg  # noqa: E402
from monster_sounds import (SR, bubbles, clack, crackle, frames_len, mix_at, norm, pulse_voice, ring_mod, rumble,  # noqa: E402
                            squelch, swept, thud, tone_voice)
from sea_creatures import soft_bell  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
SOUNDS = ROOT / "src/main/resources/assets/kingdomsatwar/sounds"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
# Ordner der Klänge (unter sounds/)
FOLDERS = {"beast": "entity/beast", "sea": "entity/sea", "void": "entity/void", "item": "item"}


# =====================================================================================
# Höhlentroll
# =====================================================================================

def troll_ambient(seed: int) -> np.ndarray:
    """Ein tiefes, behäbiges Grummeln durch die Nase, das Maul halb offen – manchmal mit einem Schniefen davor."""
    r = rng(seed)
    dur = r.uniform(1.3, 1.8)
    n = frames_len(dur)
    base = r.uniform(78.0, 92.0)
    f = curve(n, [(0.0, base), (0.3, base * r.uniform(1.08, 1.18)), (0.65, base * 0.98), (1.0, base * 0.86)])
    amp = curve(n, [(0.0, 0.0), (0.12, 1.0), (0.5, 0.8), (0.8, 0.75), (1.0, 0.0)])
    voice = morph_voice(f, amp, seed, [(0.0, "u"), (0.35, "o"), (0.75, "o"), (1.0, "u")], 0.62, jitter=0.07, shimmer=0.16,
                        sub=0.3, breath=0.35, bright=2200.0)
    y = np.zeros(n + int(0.6 * SR))
    lead = 0.0
    if r.random() < 0.5:
        mix_at(y, snort(0.35, seed + 1, 0.8), 0.0, 0.25)
        lead = 0.32
    mix_at(y, saturate(voice, 1.5), lead, 1.0)
    return y


def troll_hurt(seed: int) -> np.ndarray:
    """Ein kurzes, dumpfes Aufgrunzen vor Schmerz."""
    r = rng(seed)
    dur = r.uniform(0.45, 0.6)
    n = frames_len(dur)
    top = r.uniform(130.0, 150.0)
    f = curve(n, [(0.0, top * 0.8), (0.18, top), (1.0, top * 0.62)])
    amp = curve(n, [(0.0, 0.0), (0.06, 1.0), (0.4, 0.75), (1.0, 0.0)])
    voice = morph_voice(f, amp, seed, [(0.0, "a"), (0.5, "o"), (1.0, "u")], 0.66, jitter=0.1, shimmer=0.24, sub=0.35,
                        breath=0.5, bright=2600.0)
    y = np.zeros(n + int(0.3 * SR))
    mix_at(y, saturate(voice, 2.0), 0.0, 1.0)
    mix_at(y, gust(0.2, [(0.0, 900.0), (1.0, 600.0)], 1.4, [(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)], seed + 1), 0.0, 0.18)
    return y


def troll_death(seed: int) -> np.ndarray:
    """Ein langes, sinkendes Stöhnen, ein letzter Atemzug – dann fällt der schwere Leib, und Steinchen rieseln."""
    r = rng(seed)
    dur = r.uniform(1.8, 2.1)
    n = frames_len(dur)
    f = curve(n, [(0.0, 120.0), (0.2, 132.0), (0.6, 96.0), (1.0, 62.0)]) * r.uniform(0.95, 1.05)
    amp = curve(n, [(0.0, 0.0), (0.08, 1.0), (0.55, 0.8), (0.85, 0.35), (1.0, 0.0)])
    voice = morph_voice(f, amp, seed, [(0.0, "a"), (0.4, "o"), (1.0, "u")], 0.62, jitter=0.08, shimmer=0.2, sub=0.4,
                        breath=0.6, bright=2400.0)
    y = np.zeros(n + int(1.4 * SR))
    mix_at(y, saturate(voice, 1.6), 0.0, 1.0)
    fall = dur * 0.86
    mix_at(y, thud(0.8, 58.0, 32.0, seed + 1, noise_hi=320.0, grit=0.8), fall, 0.9)
    mix_at(y, crackle(0.9, seed + 2, 600.0, lo=500.0, hi=3000.0, env=[(0.0, 1.0), (0.3, 0.5), (1.0, 0.0)]), fall + 0.02, 0.45)
    return y


def troll_petrify(seed: int) -> np.ndarray:
    """Er wird zu Stein: ein Knirschen zieht durch den Leib, es knackt und mahlt, zuletzt ein harter Schlag wie Fels."""
    r = rng(seed)
    dur = r.uniform(1.1, 1.3)
    y = np.zeros(int((dur + 0.5) * SR))
    grind = swept(dur, [(0.0, 300.0), (0.6, 520.0), (1.0, 380.0)], 1.4, [(0.0, 0.0), (0.2, 0.8), (0.85, 1.0), (1.0, 0.0)], seed,
                  flicker=0.6, flicker_rate=(18.0, 30.0))
    mix_at(y, grind, 0.0, 0.7)
    mix_at(y, crackle(dur, seed + 1, 500.0, lo=700.0, hi=3600.0, env=[(0.0, 0.2), (0.7, 1.0), (1.0, 0.6)]), 0.0, 0.6)
    for k in range(int(r.integers(3, 6))):
        modes = [(r.uniform(900.0, 1400.0), 1.0), (r.uniform(2200.0, 3000.0), 0.5)]
        mix_at(y, clack(modes, 0.02, seed + 10 + k, q=7.0), r.uniform(0.1, dur * 0.9), r.uniform(0.25, 0.5))
    mix_at(y, thud(0.4, 90.0, 55.0, seed + 2, noise_hi=700.0, grit=0.9), dur * 0.92, 0.7)
    mix_at(y, rumble(dur, seed + 3, 130.0, [(0.0, 0.0), (0.6, 1.0), (1.0, 0.0)]), 0.0, 0.35)
    return y


def troll_smash(seed: int) -> np.ndarray:
    """Die Keule saust herab und kracht auf: ein schwerer Luftzug, Holz auf Stein, der Boden bebt kurz."""
    r = rng(seed)
    y = np.zeros(int(1.0 * SR))
    mix_at(y, gust(0.3, [(0.0, 260.0), (0.85, 900.0), (1.0, 600.0)], 1.4, [(0.0, 0.0), (0.85, 1.0), (1.0, 0.2)], seed, flicker=0.15),
           0.0, 0.5)
    hit = 0.27
    knock = swept(0.2, [(0.0, 520.0), (1.0, 230.0)], 1.2, [(0.0, 0.0), (0.04, 1.0), (0.35, 0.4), (1.0, 0.0)], seed + 1)
    mix_at(y, knock, hit, 1.0)
    wood = [(r.uniform(320.0, 380.0), 1.0), (r.uniform(780.0, 900.0), 0.5), (r.uniform(1500.0, 1700.0), 0.25)]
    mix_at(y, clack(wood, 0.035, seed + 2, q=5.0), hit, 0.7)
    mix_at(y, thud(0.5, 62.0, 36.0, seed + 3, noise_hi=380.0, grit=0.8), hit, 0.6)
    mix_at(y, crackle(0.4, seed + 4, 900.0, lo=500.0, hi=2800.0, env=[(0.0, 1.0), (1.0, 0.0)]), hit, 0.45)
    return saturate(y, 1.2)


def troll_step(seed: int) -> np.ndarray:
    """Ein schwerer, barfüßiger Schritt: dumpf, dazu knirschender Kies."""
    r = rng(seed)
    y = np.zeros(int(0.5 * SR))
    mix_at(y, thud(0.32, r.uniform(66.0, 76.0), 42.0, seed, noise_hi=280.0, grit=0.5), 0.0, 0.5)
    knock = swept(0.14, [(0.0, 330.0), (1.0, 170.0)], 1.2, [(0.0, 0.0), (0.06, 1.0), (0.4, 0.35), (1.0, 0.0)], seed + 1)
    mix_at(y, knock, 0.0, 0.9)
    mix_at(y, crackle(0.2, seed + 2, 700.0, lo=500.0, hi=2600.0, env=[(0.0, 1.0), (1.0, 0.0)]), 0.01, 0.5)
    return y


# =====================================================================================
# Lindwurm
# =====================================================================================

def hiss(dur: float, seed: int, amps, center: float = 4200.0) -> np.ndarray:
    """Ein Zischen durch die Zähne: hohes, enges Rauschen, das leicht flackert."""
    return swept(dur, [(0.0, center), (1.0, center * 0.85)], 0.9, amps, seed, flicker=0.25, flicker_rate=(12.0, 20.0))


def worm_ambient(seed: int) -> np.ndarray:
    """Ein langes Zischen, darunter ein rasselndes Grollen aus der Kehle."""
    r = rng(seed)
    dur = r.uniform(1.3, 1.7)
    n = frames_len(dur)
    y = np.zeros(n + int(0.5 * SR))
    mix_at(y, hiss(dur, seed, [(0.0, 0.0), (0.15, 1.0), (0.7, 0.8), (1.0, 0.0)], r.uniform(3600.0, 4600.0)), 0.0, 0.55)
    f = curve(n, [(0.0, 70.0), (0.4, 82.0), (1.0, 64.0)]) * r.uniform(0.92, 1.08)
    amp = curve(n, [(0.0, 0.0), (0.2, 1.0), (0.8, 0.7), (1.0, 0.0)])
    growl = pulse_voice(f, amp, seed + 1, "o", size=0.6, jitter=0.12, shimmer=0.3, sub=0.5, breath=0.4, bright=1800.0)
    mix_at(y, saturate(growl, 1.8), 0.05, 0.75)
    return y


def worm_bite(seed: int) -> np.ndarray:
    """Er schnellt vor und schnappt: ein scharfes Zischen, harte Zähne, ein kurzes Knurren."""
    r = rng(seed)
    y = np.zeros(int(0.7 * SR))
    mix_at(y, hiss(0.16, seed, [(0.0, 0.0), (0.5, 1.0), (1.0, 0.3)], 3800.0), 0.0, 0.45)
    teeth = [(r.uniform(1400.0, 1700.0), 1.0), (r.uniform(2600.0, 3000.0), 0.7), (r.uniform(4000.0, 4600.0), 0.4)]
    mix_at(y, clack(teeth, 0.028, seed + 1, q=15.0), 0.14, 1.0)
    dur = 0.36
    n = frames_len(dur)
    f = curve(n, [(0.0, 115.0), (0.3, 128.0), (1.0, 88.0)])
    amp = curve(n, [(0.0, 0.0), (0.12, 1.0), (1.0, 0.0)])
    growl = pulse_voice(f, amp, seed + 2, "a", size=0.7, jitter=0.1, shimmer=0.25, sub=0.35, breath=0.5, bright=3000.0)
    mix_at(y, filt(saturate(growl, 1.8), "high", 120.0, SR, 2), 0.12, 0.45)
    return y


def worm_hurt(seed: int) -> np.ndarray:
    """Ein rauer, zischender Aufschrei."""
    r = rng(seed)
    dur = r.uniform(0.45, 0.6)
    n = frames_len(dur)
    top = r.uniform(200.0, 240.0)
    f = curve(n, [(0.0, top * 0.75), (0.15, top), (1.0, top * 0.6)])
    amp = curve(n, [(0.0, 0.0), (0.05, 1.0), (0.4, 0.7), (1.0, 0.0)])
    voice = morph_voice(f, amp, seed, [(0.0, "a"), (1.0, "e")], 0.75, jitter=0.12, shimmer=0.3, sub=0.3, breath=0.7, bright=4200.0)
    y = np.zeros(n + int(0.3 * SR))
    mix_at(y, saturate(voice, 2.2), 0.0, 0.9)
    mix_at(y, hiss(dur, seed + 1, [(0.0, 0.0), (0.1, 1.0), (1.0, 0.0)], 3400.0), 0.0, 0.4)
    return filt(y, "low", 7000.0, SR, 2)


def worm_death(seed: int) -> np.ndarray:
    """Ein langes Grollen, das in ein ersterbendes Zischen übergeht; der lange Leib schlägt auf."""
    r = rng(seed)
    dur = r.uniform(1.7, 2.0)
    n = frames_len(dur)
    f = curve(n, [(0.0, 150.0), (0.2, 170.0), (0.6, 110.0), (1.0, 60.0)]) * r.uniform(0.95, 1.05)
    amp = curve(n, [(0.0, 0.0), (0.08, 1.0), (0.6, 0.6), (1.0, 0.0)])
    voice = morph_voice(f, amp, seed, [(0.0, "a"), (0.5, "o"), (1.0, "u")], 0.68, jitter=0.1, shimmer=0.26, sub=0.4,
                        breath=0.6, bright=3000.0)
    y = np.zeros(n + int(1.2 * SR))
    mix_at(y, saturate(voice, 1.8), 0.0, 1.0)
    mix_at(y, hiss(dur * 0.9, seed + 1, [(0.0, 0.0), (0.4, 0.4), (0.8, 1.0), (1.0, 0.0)], 3600.0), dur * 0.3, 0.45)
    for k in range(3):
        mix_at(y, thud(0.4, 70.0 - 6 * k, 40.0, seed + 2 + k, noise_hi=300.0, grit=0.6), dur * 0.8 + 0.13 * k, 0.6 - 0.12 * k)
    return y


def worm_slither(seed: int) -> np.ndarray:
    """Schuppen schaben über den Boden: ein raues, wellenförmig an- und abschwellendes Schleifen, dazu etwas Kies."""
    r = rng(seed)
    dur = r.uniform(0.75, 0.95)
    n = frames_len(dur)
    t = np.arange(n) / SR
    rate = r.uniform(6.0, 9.0)
    ripple = 0.55 + 0.45 * np.abs(np.sin(np.pi * rate * t + r.uniform(0, 3)))
    scrape = swept(dur, [(0.0, 950.0), (0.5, 1250.0), (1.0, 850.0)], 1.3, [(0.0, 0.0), (0.2, 1.0), (0.8, 0.9), (1.0, 0.0)], seed,
                   flicker=0.5, flicker_rate=(30.0, 50.0))
    y = np.zeros(n + int(0.2 * SR))
    mix_at(y, filt(fit(scrape, n) * ripple, "low", 3800.0, SR, 4), 0.0, 0.8)
    mix_at(y, swept(dur, [(0.0, 380.0), (1.0, 300.0)], 1.6, [(0.0, 0.0), (0.3, 1.0), (1.0, 0.0)], seed + 1), 0.0, 0.45)
    mix_at(y, crackle(dur, seed + 2, 220.0, lo=600.0, hi=3000.0, env=[(0.0, 0.0), (0.4, 1.0), (1.0, 0.0)]), 0.0, 0.3)
    return y


# =====================================================================================
# Hilfen für Tiere
# =====================================================================================

def wing_flutter(dur: float, seed: int, rate: float = 11.0, center: float = 1100.0) -> np.ndarray:
    """Hastiges Flattern: Flügelschläge als an- und abschwellendes, flackerndes Rauschen."""
    r = rng(seed)
    n = frames_len(dur)
    t = np.arange(n) / SR
    beat = 0.25 + 0.75 * np.abs(np.sin(np.pi * rate * t + r.uniform(0.0, 3.0))) ** 3
    w = swept(dur, [(0.0, center * 0.8), (0.5, center), (1.0, center * 0.7)], 1.3,
              [(0.0, 0.0), (0.15, 1.0), (0.8, 0.8), (1.0, 0.0)], seed, flicker=0.3)
    return fit(w, n) * beat


def hoof(seed: int, body: float, click: float, size: float = 1.0) -> np.ndarray:
    """Ein Huf setzt auf: ein Pochen (body Hz), ein harter Klick des Horns (click Hz), etwas Erde oder Laub."""
    r = rng(seed)
    y = np.zeros(int(0.45 * SR))
    mix_at(y, thud(0.12 + 0.2 * size, body * r.uniform(0.92, 1.08), body * 0.6, seed, noise_hi=500.0, grit=0.5), 0.0, 0.55)
    horn = [(click * r.uniform(0.9, 1.1), 1.0), (click * r.uniform(2.1, 2.4), 0.45)]
    mix_at(y, clack(horn, 0.01 + 0.008 * size, seed + 1, q=6.0), 0.0, 0.5)
    mix_at(y, crackle(0.1 + 0.08 * size, seed + 2, 450.0, lo=1200.0, hi=6000.0, env=[(0.0, 1.0), (1.0, 0.0)]), 0.005, 0.35)
    return y


# =====================================================================================
# Hirsch
# =====================================================================================

def deer_bleat(seed: int, f0: float, dur: float, harsh: float = 0.0, fall: float = 0.82) -> np.ndarray:
    """Ein nasales Fiepen, wie es Hirschkühe und Kälber rufen: hoch, kurz, mit viel Luft; bei Schmerz rau und gepresst."""
    n = frames_len(dur)
    f = curve(n, [(0.0, f0 * 0.9), (0.2, f0 * 1.08), (0.65, f0), (1.0, f0 * fall)])
    amp = curve(n, [(0.0, 0.0), (0.1, 1.0), (0.6, 0.8), (1.0, 0.0)])
    v = morph_voice(f, amp, seed, [(0.0, "e"), (0.45, "a"), (1.0, "e")], 1.2, jitter=0.03 + 0.05 * harsh,
                    shimmer=0.08 + 0.12 * harsh, sub=0.1 + 0.2 * harsh, breath=0.5, bright=5200.0)
    return saturate(v, 1.2 + 1.3 * harsh)


def deer_ambient(seed: int) -> np.ndarray:
    """Meist ein, zwei leise Fieptöne; manchmal schnaubt der Hirsch nur kurz durch die Nüstern."""
    r = rng(seed)
    y = np.zeros(int(1.6 * SR))
    if r.random() < 0.65:
        t = 0.0
        for i in range(1 if r.random() < 0.55 else 2):
            d = r.uniform(0.26, 0.4)
            mix_at(y, deer_bleat(seed + i, r.uniform(360.0, 450.0), d), t, 1.0 - 0.25 * i)
            t += d + r.uniform(0.2, 0.34)
    else:
        mix_at(y, snort(0.3, seed, 1.0), 0.0, 1.0)
        if r.random() < 0.5:
            mix_at(y, snort(0.22, seed + 1, 0.8), r.uniform(0.38, 0.5), 0.7)
    return y


def deer_hurt(seed: int) -> np.ndarray:
    """Ein gepresster, hoher Klagelaut."""
    r = rng(seed)
    y = np.zeros(int(0.7 * SR))
    mix_at(y, deer_bleat(seed, r.uniform(520.0, 600.0), r.uniform(0.24, 0.32), harsh=0.7, fall=0.75), 0.0, 1.0)
    return y


def deer_death(seed: int) -> np.ndarray:
    """Ein langer, sinkender Klagelaut; dann sinkt der Körper ins Laub."""
    r = rng(seed)
    dur = r.uniform(0.75, 0.9)
    y = np.zeros(int((dur + 0.8) * SR))
    mix_at(y, deer_bleat(seed, r.uniform(560.0, 620.0), dur, harsh=0.45, fall=0.5), 0.0, 1.0)
    mix_at(y, thud(0.35, 130.0, 75.0, seed + 1, noise_hi=500.0, grit=0.7), dur + 0.08, 0.5)
    mix_at(y, crackle(0.35, seed + 2, 500.0, lo=1500.0, hi=6000.0, env=[(0.0, 1.0), (1.0, 0.0)]), dur + 0.09, 0.3)
    return y


def deer_step(seed: int) -> np.ndarray:
    """Ein leichter Huf auf Waldboden."""
    r = rng(seed)
    return hoof(seed, r.uniform(150.0, 190.0), r.uniform(850.0, 1050.0), size=0.0)


# =====================================================================================
# Urelch
# =====================================================================================

def elk_grunt(seed: int, f0: float, dur: float, size: float = 0.72, rough: float = 0.2) -> np.ndarray:
    """Ein kurzes, kehliges Grunzen aus einer großen Brust."""
    n = frames_len(dur)
    f = curve(n, [(0.0, f0 * 0.9), (0.25, f0), (1.0, f0 * 0.8)])
    amp = curve(n, [(0.0, 0.0), (0.12, 1.0), (0.55, 0.7), (1.0, 0.0)])
    v = pulse_voice(f, amp, seed, "o", size=size, jitter=0.08, shimmer=rough, sub=0.3, breath=0.55, bright=2400.0)
    return saturate(v, 1.6)


def elk_ambient(seed: int) -> np.ndarray:
    """Zwei, drei tiefe Grunzer, dazwischen hörbares Atmen."""
    r = rng(seed)
    y = np.zeros(int(1.9 * SR))
    t = 0.0
    for i in range(int(r.integers(2, 4))):
        d = r.uniform(0.15, 0.24)
        mix_at(y, elk_grunt(seed + i, r.uniform(76.0, 94.0), d), t, 1.0 - 0.15 * i)
        mix_at(y, gust(0.25, [(0.0, 700.0), (1.0, 500.0)], 1.4, [(0.0, 0.0), (0.3, 1.0), (1.0, 0.0)], seed + 20 + i), t + d, 0.15)
        t += d + r.uniform(0.25, 0.42)
    return y


def elk_bugle(seed: int) -> np.ndarray:
    """
    Der Brunftruf des Bullen: ein Grollen schwingt sich zu einem langen, hellen Pfeifen auf, rau unterlegt, das weit über das
    Land hallt; danach ein paar kehlige Grunzer. So lang wie die Rufbewegung (Kopf hoch nach 0,4 s, wieder unten nach 2,8 s).
    """
    r = rng(seed)
    y = np.zeros(int(3.8 * SR))
    # Anlauf: ein Grollen, das steigt
    d1 = r.uniform(0.38, 0.46)
    n1 = frames_len(d1)
    f1 = curve(n1, [(0.0, 110.0), (0.6, 170.0), (1.0, 300.0)])
    a1 = curve(n1, [(0.0, 0.0), (0.3, 0.7), (1.0, 1.0)])
    growl = pulse_voice(f1, a1, seed, "o", size=0.8, jitter=0.07, shimmer=0.2, sub=0.3, breath=0.5, bright=3000.0)
    mix_at(y, saturate(growl, 1.8), 0.0, 0.6)
    # der Pfiff, darunter eine Oktave tiefer die raue Stimme
    d2 = r.uniform(1.4, 1.65)
    n2 = frames_len(d2)
    top = r.uniform(640.0, 760.0)
    f2 = curve(n2, [(0.0, 300.0), (0.1, top), (0.55, top * r.uniform(1.02, 1.06)), (0.85, top * 0.96), (1.0, top * 0.55)])
    a2 = curve(n2, [(0.0, 0.0), (0.08, 1.0), (0.8, 0.9), (1.0, 0.0)])
    whistle = tone_voice(f2, a2, seed + 1, harmonics=((1, 1.0), (2, 0.45), (3, 0.18), (4, 0.07)), vib_depth=0.009, vib_rate=6.0,
                         breath=0.2)
    rasp = morph_voice(f2 * 0.5, a2 * 0.8, seed + 2, [(0.0, "o"), (0.3, "a"), (1.0, "a")], 0.85, jitter=0.05, shimmer=0.16,
                       sub=0.25, breath=0.45, bright=4200.0)
    mix_at(y, whistle, d1 - 0.04, 1.0)
    mix_at(y, saturate(rasp, 2.0), d1 - 0.04, 0.32)
    # Grunzer zum Schluss
    t = d1 + d2 + 0.08
    for i in range(int(r.integers(2, 5))):
        d = r.uniform(0.09, 0.13)
        mix_at(y, elk_grunt(seed + 10 + i, r.uniform(100.0, 125.0), d, size=0.8), t, 0.55)
        t += d + r.uniform(0.09, 0.15)
    return y


def elk_angry(seed: int) -> np.ndarray:
    """Ein scharfes Schnauben, dann ein tiefes, drohendes Gebrüll."""
    r = rng(seed)
    y = np.zeros(int(1.3 * SR))
    mix_at(y, snort(0.35, seed, 1.0), 0.0, 0.8)
    dur = r.uniform(0.55, 0.7)
    n = frames_len(dur)
    f = curve(n, [(0.0, 90.0), (0.3, 125.0), (1.0, 80.0)]) * r.uniform(0.95, 1.05)
    amp = curve(n, [(0.0, 0.0), (0.1, 1.0), (0.7, 0.8), (1.0, 0.0)])
    v = morph_voice(f, amp, seed + 1, [(0.0, "o"), (0.5, "a"), (1.0, "o")], 0.72, jitter=0.1, shimmer=0.28, sub=0.4, breath=0.6,
                    bright=2600.0)
    mix_at(y, saturate(v, 2.4), 0.3, 1.0)
    return y


def elk_hurt(seed: int) -> np.ndarray:
    """Ein kurzes, raues Brüllen."""
    r = rng(seed)
    dur = r.uniform(0.4, 0.5)
    n = frames_len(dur)
    top = r.uniform(190.0, 220.0)
    f = curve(n, [(0.0, top * 0.8), (0.2, top), (1.0, top * 0.62)])
    amp = curve(n, [(0.0, 0.0), (0.06, 1.0), (0.5, 0.75), (1.0, 0.0)])
    v = morph_voice(f, amp, seed, [(0.0, "a"), (1.0, "o")], 0.75, jitter=0.1, shimmer=0.25, sub=0.35, breath=0.5, bright=3000.0)
    y = np.zeros(n + int(0.3 * SR))
    mix_at(y, saturate(v, 2.2), 0.0, 1.0)
    return y


def elk_death(seed: int) -> np.ndarray:
    """Ein langes, sinkendes Brüllen; die Hufe schlagen, der schwere Leib stürzt."""
    r = rng(seed)
    dur = r.uniform(1.5, 1.7)
    n = frames_len(dur)
    f = curve(n, [(0.0, 170.0), (0.15, 190.0), (0.6, 120.0), (1.0, 66.0)]) * r.uniform(0.95, 1.05)
    amp = curve(n, [(0.0, 0.0), (0.06, 1.0), (0.55, 0.75), (0.85, 0.35), (1.0, 0.0)])
    v = morph_voice(f, amp, seed, [(0.0, "a"), (0.5, "o"), (1.0, "u")], 0.72, jitter=0.1, shimmer=0.25, sub=0.4, breath=0.6,
                    bright=2600.0)
    y = np.zeros(n + int(1.4 * SR))
    mix_at(y, saturate(v, 2.0), 0.0, 1.0)
    for k in range(3):
        mix_at(y, hoof(seed + 3 + k, 110.0, 700.0, size=0.6), dur * 0.55 + 0.17 * k, 0.45)
    fall = dur * 0.92
    mix_at(y, thud(0.8, 55.0, 32.0, seed + 9, noise_hi=320.0, grit=0.8), fall, 1.0)
    mix_at(y, crackle(0.6, seed + 10, 500.0, lo=700.0, hi=4000.0, env=[(0.0, 1.0), (1.0, 0.0)]), fall + 0.01, 0.4)
    return y


def elk_eat(seed: int) -> np.ndarray:
    """Er rupft Gras und kaut: ein Reißen, dann rhythmisches, dumpfes Mahlen."""
    r = rng(seed)
    y = np.zeros(int(1.3 * SR))
    mix_at(y, swept(0.18, [(0.0, 2500.0), (1.0, 3800.0)], 1.2, [(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)], seed, flicker=0.5,
                    flicker_rate=(40.0, 60.0)), 0.0, 0.35)
    mix_at(y, crackle(0.18, seed + 1, 900.0, lo=1500.0, hi=6000.0, env=[(0.0, 0.5), (0.5, 1.0), (1.0, 0.0)]), 0.0, 0.3)
    t = 0.3
    for i in range(int(r.integers(3, 5))):
        chew = crackle(0.12, seed + 2 + i, 600.0, lo=500.0, hi=2500.0, env=[(0.0, 0.0), (0.3, 1.0), (1.0, 0.0)])
        mix_at(y, filt(chew, "low", 3000.0, SR, 2), t, 0.5)
        mix_at(y, thud(0.08, 160.0, 120.0, seed + 10 + i, noise_hi=500.0, grit=0.6), t, 0.25)
        t += r.uniform(0.17, 0.22)
    return y


def elk_impact(seed: int) -> np.ndarray:
    """Die Schaufeln krachen gegen etwas: zwei harte, hölzerne Schläge kurz nacheinander, dazu ein dumpfer Stoß."""
    r = rng(seed)
    y = np.zeros(int(0.9 * SR))
    antler = [(r.uniform(380.0, 450.0), 1.0), (r.uniform(950.0, 1150.0), 0.6), (r.uniform(1900.0, 2300.0), 0.35),
              (r.uniform(3200.0, 3600.0), 0.15)]
    mix_at(y, clack(antler, 0.06, seed, q=8.0), 0.0, 1.0)
    mix_at(y, clack([(f * r.uniform(1.05, 1.15), g) for f, g in antler], 0.04, seed + 1, q=8.0), r.uniform(0.03, 0.05), 0.6)
    mix_at(y, thud(0.4, 85.0, 50.0, seed + 2, noise_hi=350.0, grit=0.7), 0.0, 0.8)
    mix_at(y, crackle(0.3, seed + 3, 500.0, lo=800.0, hi=4000.0, env=[(0.0, 1.0), (1.0, 0.0)]), 0.01, 0.35)
    return y


def elk_step(seed: int) -> np.ndarray:
    """Ein schwerer Huf."""
    r = rng(seed)
    return hoof(seed, r.uniform(80.0, 95.0), r.uniform(600.0, 750.0), size=1.0)


# =====================================================================================
# Gans und Flamingo
# =====================================================================================

def goose_honk(seed: int, f0: float, dur: float, harsh: float = 0.0) -> np.ndarray:
    """Ein Gänseruf: hart einsetzend, nasal und voll Obertöne, am Ende kurz absinkend („a-honk“)."""
    n = frames_len(dur)
    f = curve(n, [(0.0, f0 * 0.78), (0.12, f0 * 1.04), (0.7, f0), (1.0, f0 * 0.86)])
    amp = curve(n, [(0.0, 0.0), (0.06, 1.0), (0.65, 0.85), (1.0, 0.0)])
    v = morph_voice(f, amp, seed, [(0.0, "a"), (0.4, "e"), (1.0, "e")], 1.35, jitter=0.02 + 0.04 * harsh,
                    shimmer=0.06 + 0.1 * harsh, sub=0.12 + 0.2 * harsh, breath=0.12, bright=6500.0)
    nasal = filt(v, "band", [1600.0, 3200.0], SR, 2)
    return saturate(v + 1.4 * nasal, 2.4 + harsh)


def goose_ambient(seed: int) -> np.ndarray:
    """Ein bis drei Rufe hintereinander."""
    r = rng(seed)
    y = np.zeros(int(1.4 * SR))
    t = 0.0
    base = r.uniform(320.0, 370.0)
    for i in range(int(r.integers(1, 4))):
        d = r.uniform(0.16, 0.24)
        mix_at(y, goose_honk(seed + i, base * r.uniform(0.96, 1.1), d), t, 1.0 - 0.1 * i)
        t += d + r.uniform(0.07, 0.14)
    return y


def goose_hiss(seed: int) -> np.ndarray:
    """Die Gans faucht mit offenem Schnabel: ein langes, flackerndes Zischen, darunter ein Rasseln aus der Kehle."""
    r = rng(seed)
    dur = r.uniform(0.8, 1.1)
    n = frames_len(dur)
    y = np.zeros(n + int(0.3 * SR))
    mix_at(y, swept(dur, [(0.0, 3200.0), (0.5, 4200.0), (1.0, 3600.0)], 1.3, [(0.0, 0.0), (0.12, 1.0), (0.75, 0.8), (1.0, 0.0)],
                    seed, flicker=0.35, flicker_rate=(8.0, 14.0)), 0.0, 1.0)
    f = np.full(n, r.uniform(55.0, 70.0))
    amp = curve(n, [(0.0, 0.0), (0.2, 1.0), (0.8, 0.8), (1.0, 0.0)])
    mix_at(y, pulse_voice(f, amp, seed + 1, "e", size=1.3, jitter=0.15, shimmer=0.4, breath=1.0, bright=5000.0), 0.0, 0.35)
    return filt(y, "low", 8000.0, SR, 2)


def goose_hurt(seed: int) -> np.ndarray:
    """Ein schriller, rauer Schrei, manchmal ein zweiter hinterher."""
    r = rng(seed)
    y = np.zeros(int(0.8 * SR))
    d = r.uniform(0.17, 0.22)
    mix_at(y, goose_honk(seed, r.uniform(480.0, 540.0), d, harsh=0.9), 0.0, 1.0)
    if r.random() < 0.5:
        mix_at(y, goose_honk(seed + 1, r.uniform(440.0, 500.0), d * 0.8, harsh=0.9), d + 0.05, 0.7)
    return y


def goose_death(seed: int) -> np.ndarray:
    """Drei sinkende Schreie, die Flügel schlagen ein letztes Mal."""
    r = rng(seed)
    y = np.zeros(int(1.8 * SR))
    t = 0.0
    for i, (f0, d) in enumerate([(520.0, 0.2), (430.0, 0.22), (340.0, 0.32)]):
        mix_at(y, goose_honk(seed + i, f0 * r.uniform(0.95, 1.05), d, harsh=0.7 - 0.2 * i), t, 1.0 - 0.15 * i)
        t += d + r.uniform(0.08, 0.14)
    mix_at(y, wing_flutter(0.9, seed + 9, rate=r.uniform(9.0, 12.0)), 0.15, 0.4)
    return y


def flamingo_hurt(seed: int) -> np.ndarray:
    """Ein erschrockenes, nasales „gäg-gäg“ (dieselbe Stimme wie sein Schnattern)."""
    r = rng(seed)
    y = np.zeros(int(0.7 * SR))
    base = r.uniform(380.0, 430.0)
    mix_at(y, saturate(_honk(seed, base, r.uniform(0.13, 0.17), [(0, 1.0), (0.3, 1.18), (1, 0.85)]), 2.0), 0.0, 1.0)
    mix_at(y, saturate(_honk(seed + 1, base * 1.05, 0.09, [(0, 1.0), (0.5, 1.1), (1, 0.9)]), 2.0), r.uniform(0.2, 0.26), 0.7)
    return y


def flamingo_death(seed: int) -> np.ndarray:
    """Drei sinkende Rufe, ein Flattern, dann fällt er ins flache Wasser."""
    r = rng(seed)
    y = np.zeros(int(1.6 * SR))
    base = r.uniform(360.0, 400.0)
    t = 0.0
    for i, d in enumerate([0.16, 0.2, 0.3]):
        mix_at(y, saturate(_honk(seed + i, base * (1.1 - 0.15 * i), d, [(0, 1.0), (0.3, 1.1), (1, 0.75)]), 1.8), t, 1.0 - 0.2 * i)
        t += d + r.uniform(0.08, 0.12)
    mix_at(y, wing_flutter(0.7, seed + 9, rate=r.uniform(8.0, 10.0), center=900.0), 0.1, 0.35)
    mix_at(y, swept(0.25, [(0, 900), (0.4, 2400), (1, 1500)], 1.4, [(0, 0), (0.1, 1), (0.4, 0.5), (1, 0)], seed + 10, flicker=0.6,
                    flicker_rate=(30.0, 60.0)), t + 0.05, 0.4)
    return y


# =====================================================================================
# Moorschnapper
# =====================================================================================

def snapper_croak(seed: int, f0: float, dur: float, rough: float = 0.45) -> np.ndarray:
    """Ein kehliges Knarren: sehr langsame, unregelmäßige Pulse wie ein knarrendes Brett, durch einen engen Rachen."""
    n = frames_len(dur)
    f = f0 * curve(n, [(0.0, 0.9), (0.4, 1.05), (1.0, 0.85)])
    amp = curve(n, [(0.0, 0.0), (0.2, 1.0), (0.7, 0.7), (1.0, 0.0)])
    return pulse_voice(f, amp, seed, "u", size=0.8, jitter=0.2, shimmer=rough, sub=0.4, breath=0.6, bright=1800.0)


def snapper_ambient(seed: int) -> np.ndarray:
    """Ein Ausatmen, ein kehliges Knarren, manchmal blubbert es um ihn im Wasser."""
    r = rng(seed)
    y = np.zeros(int(1.6 * SR))
    if r.random() < 0.5:
        mix_at(y, bubbles(0.8, seed, 14.0, 140.0, 520.0, cycles=(6.0, 12.0)), 0.0, 0.5)
    mix_at(y, swept(0.4, [(0.0, 1400.0), (1.0, 1100.0)], 1.4, [(0.0, 0.0), (0.3, 1.0), (1.0, 0.0)], seed + 2), 0.0, 0.25)
    mix_at(y, snapper_croak(seed + 1, r.uniform(48.0, 58.0), r.uniform(0.5, 0.7)), r.uniform(0.1, 0.4), 0.9)
    return y


def snapper_hiss(seed: int) -> np.ndarray:
    """Weit aufgerissenes Maul: ein scharfes Fauchen, darunter rasselt die Kehle."""
    r = rng(seed)
    dur = r.uniform(0.6, 0.85)
    n = frames_len(dur)
    y = np.zeros(n + int(0.3 * SR))
    mix_at(y, swept(dur, [(0.0, 2400.0), (0.3, 3200.0), (1.0, 2600.0)], 1.6, [(0.0, 0.0), (0.08, 1.0), (0.7, 0.75), (1.0, 0.0)],
                    seed, flicker=0.3), 0.0, 1.0)
    f = np.full(n, r.uniform(38.0, 46.0))
    amp = curve(n, [(0.0, 0.0), (0.1, 1.0), (1.0, 0.0)])
    mix_at(y, pulse_voice(f, amp, seed + 1, "a", size=0.9, jitter=0.25, shimmer=0.5, breath=1.0, bright=3500.0), 0.0, 0.45)
    return filt(y, "low", 8000.0, SR, 2)


def snapper_snap(seed: int) -> np.ndarray:
    """Der Hornschnabel schlägt zu: ein Luftzug, dann ein harter, trockener Knall."""
    r = rng(seed)
    y = np.zeros(int(0.5 * SR))
    mix_at(y, swept(0.08, [(0.0, 1500.0), (1.0, 2600.0)], 1.0, [(0.0, 0.0), (1.0, 1.0)], seed), 0.0, 0.25)
    beak = [(r.uniform(1000.0, 1250.0), 1.0), (r.uniform(2200.0, 2600.0), 0.7), (r.uniform(3700.0, 4300.0), 0.4)]
    mix_at(y, clack(beak, 0.022, seed + 1, q=10.0), 0.07, 1.0)
    mix_at(y, thud(0.12, 160.0, 110.0, seed + 2, noise_hi=700.0, grit=0.4), 0.07, 0.35)
    return y


def snapper_hurt(seed: int) -> np.ndarray:
    """Ein kurzes Fauchen mit einem höheren, gepressten Knarren."""
    r = rng(seed)
    y = np.zeros(int(0.7 * SR))
    mix_at(y, swept(0.25, [(0.0, 2600.0), (1.0, 2200.0)], 1.5, [(0.0, 0.0), (0.1, 1.0), (1.0, 0.0)], seed, flicker=0.3), 0.0, 0.6)
    mix_at(y, saturate(snapper_croak(seed + 1, r.uniform(70.0, 85.0), r.uniform(0.25, 0.32), rough=0.55), 1.8), 0.02, 1.0)
    return y


def snapper_death(seed: int) -> np.ndarray:
    """Ein langes, ersterbendes Ausatmen und Knarren; der Panzer schlägt schwer auf, Blasen steigen."""
    r = rng(seed)
    y = np.zeros(int(2.4 * SR))
    mix_at(y, swept(1.2, [(0.0, 2400.0), (1.0, 1500.0)], 1.6, [(0.0, 0.0), (0.08, 1.0), (1.0, 0.0)], seed, flicker=0.25), 0.0, 0.6)
    mix_at(y, snapper_croak(seed + 1, r.uniform(60.0, 70.0), 0.9, rough=0.5), 0.05, 0.9)
    shell = [(r.uniform(330.0, 380.0), 1.0), (r.uniform(780.0, 880.0), 0.5)]
    mix_at(y, clack(shell, 0.05, seed + 2, q=4.0), 1.05, 0.6)
    mix_at(y, thud(0.5, 75.0, 45.0, seed + 3, noise_hi=350.0, grit=0.7), 1.05, 0.8)
    mix_at(y, bubbles(0.9, seed + 4, 16.0, 150.0, 600.0, cycles=(6.0, 12.0)), 1.1, 0.35)
    return y


def snapper_step(seed: int) -> np.ndarray:
    """Ein schwerer Fuß im Schlamm, der Panzer schabt."""
    r = rng(seed)
    y = np.zeros(int(0.5 * SR))
    mix_at(y, squelch(0.25, seed, hi=1100.0, lo=280.0), 0.0, 0.6)
    mix_at(y, thud(0.2, r.uniform(100.0, 120.0), 70.0, seed + 1, noise_hi=400.0, grit=0.5), 0.0, 0.5)
    mix_at(y, swept(0.25, [(0.0, 700.0), (1.0, 900.0)], 1.0, [(0.0, 0.0), (0.4, 1.0), (1.0, 0.0)], seed + 2), 0.05, 0.25)
    return y


# =====================================================================================
# Kommandostab
# =====================================================================================

def horn(f: np.ndarray, amp: np.ndarray, seed: int) -> np.ndarray:
    """Ein Kriegshorn: ein obertonreicher Ton durch ein enges Rohr; je kräftiger geblasen, desto heller."""
    r = rng(seed)
    n = len(f)
    t = np.arange(n) / SR
    fv = f * (1.0 + 0.004 * np.sin(2 * np.pi * 4.8 * t + r.uniform(0.0, 6.0)))
    raw = saw(fv, amp, SR, partials=30, rolloff=1.1)
    body = formant_filter(raw, SR, [(520.0, 140.0, 1.0), (1150.0, 220.0, 0.55), (2500.0, 400.0, 0.2)])
    dark = filt(body, "low", 1100.0, SR, 2)
    bright = filt(body, "low", 3600.0, SR, 2)
    a = amp / (np.max(amp) + 1e-12)
    y = dark * (1.0 - a) + bright * a
    y = y + 0.06 * norm(filt(r.standard_normal(n), "band", [800, 3000], SR, 2), float(np.max(np.abs(y)))) * a
    return saturate(norm(y, float(np.max(amp))), 1.4)


def baton_command(seed: int) -> np.ndarray:
    """Ein kurzes Hornsignal: ein Anstoß auf dem Grundton, dann die Quarte, gehalten."""
    r = rng(seed)
    y = np.zeros(int(1.6 * SR))
    base = float(r.choice([196.0, 207.7, 220.0]))
    t = 0.0
    for i, (ratio, d) in enumerate([(1.0, 0.16), (4.0 / 3.0, 0.42)]):
        n = frames_len(d)
        f = base * ratio * curve(n, [(0.0, 0.96), (0.12, 1.0), (1.0, 1.0)])
        amp = curve(n, [(0.0, 0.0), (0.12, 1.0), (0.75, 0.85), (1.0, 0.0)])
        mix_at(y, horn(f, amp, seed + i), t, 0.85 if i == 0 else 1.0)
        t += d + 0.04
    return y


# =====================================================================================
# Meer: Hai, Anglerfisch, Mantarochen, Riesenkrabbe, Leuchtqualle
# =====================================================================================

def water_churn(dur: float, seed: int, center: float = 900.0, amps=None) -> np.ndarray:
    """Aufgewühltes Wasser: flackerndes Rauschen im mittleren Band."""
    return swept(dur, [(0.0, center * 0.7), (0.4, center * 1.4), (1.0, center)], 1.6, amps or [(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)],
                 seed, flicker=0.5, flicker_rate=(18.0, 30.0))


def shark_bite(seed: int) -> np.ndarray:
    """Der Hai schießt heran und beißt: Wasser rauscht, die Kiefer schlagen schwer zu, es knirscht, Blasen steigen."""
    r = rng(seed)
    y = np.zeros(int(0.9 * SR))
    mix_at(y, swept(0.18, [(0.0, 300.0), (1.0, 900.0)], 1.6, [(0.0, 0.0), (1.0, 1.0)], seed), 0.0, 0.35)
    teeth = [(r.uniform(900.0, 1100.0), 1.0), (r.uniform(1800.0, 2200.0), 0.6), (r.uniform(3000.0, 3400.0), 0.3)]
    mix_at(y, clack(teeth, 0.03, seed + 1, q=6.0), 0.16, 0.8)
    mix_at(y, thud(0.35, 120.0, 60.0, seed + 2, noise_hi=500.0, grit=0.6), 0.16, 0.9)
    mix_at(y, crackle(0.25, seed + 3, 700.0, lo=600.0, hi=3000.0, env=[(0.0, 1.0), (1.0, 0.0)]), 0.17, 0.4)
    mix_at(y, bubbles(0.5, seed + 4, 40.0, 200.0, 900.0, cycles=(6.0, 12.0)), 0.2, 0.35)
    return filt(y, "low", 3200.0, SR, 2)


def shark_hurt(seed: int) -> np.ndarray:
    """Er schlägt mit dem Schwanz um sich: zwei, drei Wasserstöße, ein dumpfer Ruck, Blasen."""
    r = rng(seed)
    y = np.zeros(int(0.9 * SR))
    for k in range(int(r.integers(2, 4))):
        mix_at(y, water_churn(0.18, seed + k, r.uniform(800.0, 1200.0)), 0.12 * k + r.uniform(0.0, 0.03), 0.8 - 0.15 * k)
    mix_at(y, thud(0.25, 90.0, 55.0, seed + 9, noise_hi=400.0, grit=0.5), 0.0, 0.5)
    mix_at(y, bubbles(0.6, seed + 10, 50.0, 180.0, 800.0, cycles=(6.0, 12.0)), 0.05, 0.45)
    return filt(y, "low", 3500.0, SR, 2)


def shark_death(seed: int) -> np.ndarray:
    """Ein letztes, schwächer werdendes Schlagen, dann steigen nur noch Blasen auf."""
    r = rng(seed)
    y = np.zeros(int(2.6 * SR))
    for k in range(4):
        mix_at(y, water_churn(0.25, seed + k, r.uniform(700.0, 1100.0)), 0.16 * k + r.uniform(0.0, 0.04), 0.9 - 0.15 * k)
    mix_at(y, thud(0.3, 85.0, 50.0, seed + 8, noise_hi=350.0, grit=0.5), 0.0, 0.5)
    mix_at(y, bubbles(1.8, seed + 9, 22.0, 150.0, 700.0, cycles=(6.0, 14.0), env=[(0.0, 1.0), (1.0, 0.0)]), 0.3, 0.5)
    mix_at(y, rumble(1.6, seed + 10, 180.0, [(0.0, 0.0), (0.1, 1.0), (1.0, 0.0)]), 0.1, 0.35)
    return filt(y, "low", 3000.0, SR, 2)


def angler_bite(seed: int) -> np.ndarray:
    """Das riesige Maul schlägt zu: ein Sog, dann schließen sich die Nadelzähne mit nassem Knacken."""
    r = rng(seed)
    y = np.zeros(int(0.9 * SR))
    mix_at(y, swept(0.14, [(0.0, 900.0), (1.0, 250.0)], 1.4, [(0.0, 0.0), (0.6, 1.0), (1.0, 0.3)], seed), 0.0, 0.6)
    teeth = [(r.uniform(1700.0, 2100.0), 1.0), (r.uniform(2900.0, 3400.0), 0.6), (r.uniform(4300.0, 4900.0), 0.3)]
    mix_at(y, clack(teeth, 0.02, seed + 1, q=9.0), 0.12, 0.8)
    mix_at(y, squelch(0.3, seed + 2, hi=1200.0, lo=300.0), 0.12, 0.6)
    mix_at(y, thud(0.25, 110.0, 70.0, seed + 3, noise_hi=450.0, grit=0.5), 0.12, 0.6)
    mix_at(y, bubbles(0.5, seed + 4, 30.0, 220.0, 900.0, cycles=(6.0, 12.0)), 0.16, 0.3)
    return filt(y, "low", 3800.0, SR, 2)


def angler_snap(seed: int) -> np.ndarray:
    """Blitzschnelles Zuschnappen: ein kurzes Schlucken und ein hartes Klacken."""
    r = rng(seed)
    y = np.zeros(int(0.5 * SR))
    mix_at(y, swept(0.06, [(0.0, 1400.0), (1.0, 400.0)], 1.2, [(0.0, 0.0), (0.5, 1.0), (1.0, 0.0)], seed), 0.0, 0.5)
    teeth = [(r.uniform(2000.0, 2400.0), 1.0), (r.uniform(3500.0, 4000.0), 0.5)]
    mix_at(y, clack(teeth, 0.015, seed + 1, q=10.0), 0.05, 1.0)
    mix_at(y, bubbles(0.3, seed + 2, 25.0, 300.0, 1100.0, cycles=(5.0, 10.0)), 0.06, 0.25)
    return filt(y, "low", 4200.0, SR, 2)


def angler_groan(seed: int, dur: float, f0: float, f1: float) -> np.ndarray:
    """Ein tiefes, blubberndes Stöhnen aus dem großen Bauch."""
    n = frames_len(dur)
    f = curve(n, [(0.0, f0), (0.3, f0 * 1.08), (1.0, f1)])
    amp = curve(n, [(0.0, 0.0), (0.15, 1.0), (0.7, 0.7), (1.0, 0.0)])
    return pulse_voice(f, amp, seed, "u", size=0.75, jitter=0.12, shimmer=0.3, sub=0.4, breath=0.4, bright=1500.0)


def angler_hurt(seed: int) -> np.ndarray:
    """Ein kurzes Stöhnen im Schwall von Blasen."""
    r = rng(seed)
    y = np.zeros(int(0.9 * SR))
    mix_at(y, angler_groan(seed, r.uniform(0.35, 0.45), r.uniform(70.0, 85.0), 55.0), 0.0, 1.0)
    mix_at(y, bubbles(0.45, seed + 1, 60.0, 140.0, 600.0, cycles=(6.0, 12.0)), 0.0, 0.6)
    return filt(y, "low", 2600.0, SR, 2)


def angler_death(seed: int) -> np.ndarray:
    """Ein langes, sinkendes Stöhnen, ein Schwall Blasen – und der Köder erlischt mit einem letzten, fallenden Klingen."""
    r = rng(seed)
    y = np.zeros(int(2.6 * SR))
    mix_at(y, angler_groan(seed, 1.3, 80.0, 42.0), 0.0, 1.0)
    mix_at(y, bubbles(1.6, seed + 1, 30.0, 120.0, 500.0, cycles=(6.0, 14.0), env=[(0.0, 1.0), (1.0, 0.1)]), 0.1, 0.55)
    n = frames_len(0.9)
    f = curve(n, [(0.0, r.uniform(1900.0, 2400.0)), (1.0, 700.0)])
    lure = tone(f, curve(n, [(0.0, 0.0), (0.03, 1.0), (1.0, 0.0)]) ** 1.5, SR, harmonics=((1, 1.0), (2, 0.15)))
    mix_at(y, lure, 0.9, 0.12)
    return filt(y, "low", 3000.0, SR, 2)


def manta_leap(seed: int) -> np.ndarray:
    """Der Rochen schnellt aus dem Wasser: rauschende Gischt, die abreißt, Tropfen sprühen, ein Schlag der Flossen."""
    r = rng(seed)
    y = np.zeros(int(1.0 * SR))
    mix_at(y, swept(0.45, [(0.0, 500.0), (0.6, 1800.0), (1.0, 2600.0)], 1.8, [(0.0, 0.0), (0.4, 1.0), (0.7, 0.8), (1.0, 0.0)], seed,
                    flicker=0.35), 0.0, 1.0)
    mix_at(y, crackle(0.6, seed + 1, 260.0, lo=2000.0, hi=7500.0, env=[(0.0, 0.2), (0.3, 1.0), (1.0, 0.0)]), 0.15, 0.4)
    mix_at(y, gust(0.3, [(0.0, 300.0), (1.0, 700.0)], 1.5, [(0.0, 0.0), (0.5, 1.0), (1.0, 0.0)], seed + 2), r.uniform(0.18, 0.24), 0.35)
    return y


def manta_splash(seed: int) -> np.ndarray:
    """Der schwere Leib klatscht flach aufs Wasser: ein Schlag, ein breiter Schwall, Tropfen und Blasen."""
    r = rng(seed)
    y = np.zeros(int(1.6 * SR))
    mix_at(y, thud(0.4, r.uniform(80.0, 95.0), 50.0, seed, noise_hi=500.0, grit=0.8), 0.0, 0.9)
    mix_at(y, swept(0.9, [(0.0, 1500.0), (0.15, 2600.0), (1.0, 900.0)], 2.4, [(0.0, 0.0), (0.03, 1.0), (0.3, 0.5), (1.0, 0.0)],
                    seed + 1, flicker=0.4), 0.0, 1.0)
    mix_at(y, crackle(1.0, seed + 2, 200.0, lo=1800.0, hi=7000.0, env=[(0.0, 1.0), (1.0, 0.0)]), 0.1, 0.35)
    mix_at(y, bubbles(0.8, seed + 3, 30.0, 200.0, 900.0), 0.2, 0.3)
    return y


def manta_hurt(seed: int) -> np.ndarray:
    """Ein jäher Schlag der großen Flossen unter Wasser: ein dumpfer Wasserstoß, aufsteigende Blasen."""
    y = np.zeros(int(0.9 * SR))
    mix_at(y, gust(0.3, [(0.0, 250.0), (0.5, 520.0), (1.0, 300.0)], 1.6, [(0.0, 0.0), (0.35, 1.0), (1.0, 0.0)], seed), 0.0, 1.0)
    mix_at(y, water_churn(0.3, seed + 1, 800.0), 0.05, 0.5)
    mix_at(y, bubbles(0.5, seed + 2, 40.0, 180.0, 800.0, cycles=(6.0, 12.0)), 0.08, 0.5)
    return filt(y, "low", 3000.0, SR, 2)


def manta_death(seed: int) -> np.ndarray:
    """Drei matter werdende Flossenschläge, dann sinkt er, und Blasen steigen."""
    y = np.zeros(int(2.4 * SR))
    for k in range(3):
        mix_at(y, gust(0.4, [(0.0, 220.0), (0.5, 450.0), (1.0, 260.0)], 1.6, [(0.0, 0.0), (0.35, 1.0), (1.0, 0.0)], seed + k), 0.45 * k,
               0.9 - 0.25 * k)
    mix_at(y, bubbles(1.8, seed + 5, 18.0, 150.0, 700.0, cycles=(6.0, 14.0), env=[(0.0, 1.0), (1.0, 0.0)]), 0.2, 0.5)
    mix_at(y, rumble(1.5, seed + 6, 160.0, [(0.0, 0.0), (0.2, 1.0), (1.0, 0.0)]), 0.0, 0.3)
    return filt(y, "low", 2800.0, SR, 2)


def crab_step(seed: int) -> np.ndarray:
    """Spitze Beine tippen nacheinander auf: zwei, drei harte Chitin-Klicks und etwas Sand."""
    r = rng(seed)
    y = np.zeros(int(0.3 * SR))
    for k in range(int(r.integers(2, 4))):
        tip = [(r.uniform(1800.0, 2400.0), 1.0), (r.uniform(3600.0, 4400.0), 0.5)]
        mix_at(y, clack(tip, 0.008, seed + k, q=11.0), 0.03 * k + r.uniform(0.0, 0.012), r.uniform(0.5, 1.0))
    mix_at(y, crackle(0.12, seed + 9, 500.0, lo=1500.0, hi=5000.0, env=[(0.0, 1.0), (1.0, 0.0)]), 0.0, 0.3)
    return y


def jelly_hurt(seed: int) -> np.ndarray:
    """Die Glocke zuckt zusammen: ein weiches Quatschen und ein kurzes, gläsernes Flackern ihres Leuchtens."""
    r = rng(seed)
    y = np.zeros(int(0.9 * SR))
    mix_at(y, squelch(0.22, seed, hi=1500.0, lo=500.0, width=0.5), 0.0, 0.8)
    for k in range(int(r.integers(2, 4))):
        mix_at(y, soft_bell(r.uniform(1800.0, 3200.0), 0.5, 0.18, seed + 1 + k), 0.03 + 0.06 * k, 0.14)
    return y


def jelly_death(seed: int) -> np.ndarray:
    """Sie fällt in sich zusammen: langes Quatschen, das Leuchten erlischt in sinkenden Glastönen."""
    r = rng(seed)
    y = np.zeros(int(2.0 * SR))
    mix_at(y, squelch(0.5, seed, hi=1300.0, lo=300.0, width=0.5), 0.0, 0.9)
    f0 = r.uniform(2600.0, 3000.0)
    for k in range(5):
        mix_at(y, soft_bell(f0 * (0.84 ** k), 0.9, 0.3, seed + 1 + k), 0.12 + 0.13 * k, 0.18 - 0.025 * k)
    mix_at(y, bubbles(0.8, seed + 9, 18.0, 300.0, 1000.0, cycles=(5.0, 10.0)), 0.1, 0.2)
    return y


# =====================================================================================
# Leerenschlund
# =====================================================================================

def maw_bite(seed: int) -> np.ndarray:
    """Der Schlund schnappt zu: ein Sog ins Nichts, ein schwerer, nasser Biss, dahinter hallt die Leere."""
    r = rng(seed)
    y = np.zeros(int(1.2 * SR))
    mix_at(y, swept(0.2, [(0.0, 2400.0), (1.0, 500.0)], 1.4, [(0.0, 0.0), (0.7, 1.0), (1.0, 0.2)], seed), 0.0, 0.5)
    jaw = [(r.uniform(420.0, 520.0), 1.0), (r.uniform(1100.0, 1300.0), 0.6), (r.uniform(2300.0, 2700.0), 0.3)]
    mix_at(y, clack(jaw, 0.04, seed + 1, q=7.0), 0.18, 0.9)
    mix_at(y, thud(0.35, 95.0, 50.0, seed + 2, noise_hi=400.0, grit=0.6), 0.18, 0.8)
    mix_at(y, squelch(0.3, seed + 3, hi=900.0, lo=200.0), 0.19, 0.4)
    n = frames_len(0.7)
    hum = tone(np.full(n, r.uniform(55.0, 65.0)), curve(n, [(0.0, 0.0), (0.1, 1.0), (1.0, 0.0)]), SR,
               harmonics=((1, 1.0), (2, 0.5), (3, 0.25)))
    mix_at(y, ring_mod(hum, r.uniform(28.0, 36.0), 0.5), 0.2, 0.4)
    return y


def maw_hurt(seed: int) -> np.ndarray:
    """Ein gurgelndes, verzerrtes Aufheulen aus dem Schlund."""
    r = rng(seed)
    dur = r.uniform(0.45, 0.6)
    n = frames_len(dur)
    f = curve(n, [(0.0, 90.0), (0.2, 140.0), (1.0, 70.0)]) * r.uniform(0.9, 1.1)
    amp = curve(n, [(0.0, 0.0), (0.08, 1.0), (0.5, 0.7), (1.0, 0.0)])
    v = morph_voice(f, amp, seed, [(0.0, "a"), (0.5, "o"), (1.0, "u")], 0.8, jitter=0.12, shimmer=0.3, sub=0.4, breath=0.4,
                    bright=2800.0)
    y = np.zeros(n + int(0.4 * SR))
    mix_at(y, ring_mod(saturate(v, 2.2), r.uniform(30.0, 45.0), 0.45), 0.0, 1.0)
    mix_at(y, bubbles(0.4, seed + 1, 40.0, 200.0, 800.0, cycles=(5.0, 10.0)), 0.05, 0.3)
    return y


def maw_death(seed: int) -> np.ndarray:
    """
    Der Schlund fällt in sich zusammen: ein sinkendes Gurgeln, ein Sog, der jäh abreißt, ein tiefer Schlag – dann ein letztes
    Flirren der Leere.
    """
    r = rng(seed)
    y = np.zeros(int(3.0 * SR))
    dur = 1.2
    n = frames_len(dur)
    f = curve(n, [(0.0, 120.0), (0.3, 130.0), (1.0, 40.0)]) * r.uniform(0.95, 1.05)
    amp = curve(n, [(0.0, 0.0), (0.08, 1.0), (0.7, 0.6), (1.0, 0.0)])
    v = morph_voice(f, amp, seed, [(0.0, "a"), (0.6, "o"), (1.0, "u")], 0.75, jitter=0.12, shimmer=0.3, sub=0.45, breath=0.5,
                    bright=2400.0)
    mix_at(y, ring_mod(saturate(v, 2.0), 33.0, 0.4), 0.0, 1.0)
    mix_at(y, filt(swept(0.6, [(0.0, 300.0), (1.0, 2600.0)], 1.4, [(0.0, 0.0), (0.9, 1.0), (1.0, 0.0)], seed + 1), "low", 5000.0, SR, 2),
           0.8, 0.6)
    mix_at(y, thud(0.8, 60.0, 30.0, seed + 2, noise_hi=250.0, grit=0.6), 1.4, 0.9)
    mix_at(y, sparkle(1.2, seed + 3, 10.0, 900.0, 2400.0, decay=(0.2, 0.5), env=[(0.0, 1.0), (1.0, 0.0)]), 1.45, 0.2)
    return y


# =====================================================================================
# Tabelle
# =====================================================================================

# Name: (Erzeuger, Varianten, Raum (Nachhall s, Anteil) oder None, Lautheit in LUFS, Ordner – siehe FOLDERS)
SYNTH = {
    "cave_troll_ambient": (troll_ambient, 4, (0.6, 0.12), -20.0, "beast"),
    "cave_troll_hurt": (troll_hurt, 4, (0.5, 0.1), -16.5, "beast"),
    "cave_troll_death": (troll_death, 2, (0.8, 0.14), -16.0, "beast"),
    "cave_troll_petrify": (troll_petrify, 3, (0.7, 0.12), -17.5, "beast"),
    "cave_troll_smash": (troll_smash, 3, (0.6, 0.1), -15.5, "beast"),
    "cave_troll_step": (troll_step, 4, None, -21.0, "beast"),
    "lindworm_ambient": (worm_ambient, 4, (0.5, 0.1), -20.0, "beast"),
    "lindworm_bite": (worm_bite, 3, (0.4, 0.08), -17.0, "beast"),
    "lindworm_hurt": (worm_hurt, 4, (0.5, 0.1), -16.5, "beast"),
    "lindworm_death": (worm_death, 2, (0.8, 0.14), -16.0, "beast"),
    "lindworm_slither": (worm_slither, 4, None, -23.0, "beast"),
    # seit 1.1.4
    "deer_ambient": (deer_ambient, 4, (0.5, 0.1), -24.0, "beast"),
    "deer_hurt": (deer_hurt, 3, (0.4, 0.08), -19.0, "beast"),
    "deer_death": (deer_death, 2, (0.6, 0.1), -19.0, "beast"),
    "deer_step": (deer_step, 5, None, -27.0, "beast"),
    "primeval_elk_ambient": (elk_ambient, 4, (0.7, 0.12), -21.0, "beast"),
    "primeval_elk_bugle": (elk_bugle, 3, (2.2, 0.28), -15.0, "beast"),
    "primeval_elk_angry": (elk_angry, 3, (0.6, 0.1), -17.0, "beast"),
    "primeval_elk_hurt": (elk_hurt, 4, (0.6, 0.1), -17.0, "beast"),
    "primeval_elk_death": (elk_death, 2, (0.9, 0.14), -16.0, "beast"),
    "primeval_elk_eat": (elk_eat, 3, None, -25.0, "beast"),
    "primeval_elk_impact": (elk_impact, 3, (0.5, 0.1), -16.0, "beast"),
    "primeval_elk_step": (elk_step, 5, None, -23.0, "beast"),
    "goose_ambient": (goose_ambient, 4, (0.4, 0.08), -19.0, "beast"),
    "goose_hiss": (goose_hiss, 3, (0.4, 0.08), -20.0, "beast"),
    "goose_hurt": (goose_hurt, 3, (0.4, 0.08), -17.0, "beast"),
    "goose_death": (goose_death, 2, (0.5, 0.1), -17.0, "beast"),
    "flamingo_hurt": (flamingo_hurt, 3, (0.4, 0.08), -17.0, "beast"),
    "flamingo_death": (flamingo_death, 2, (0.5, 0.1), -17.0, "beast"),
    "mire_snapper_ambient": (snapper_ambient, 4, (0.5, 0.1), -23.0, "beast"),
    "mire_snapper_hiss": (snapper_hiss, 3, (0.4, 0.08), -18.0, "beast"),
    "mire_snapper_snap": (snapper_snap, 3, (0.3, 0.06), -16.0, "beast"),
    "mire_snapper_hurt": (snapper_hurt, 3, (0.4, 0.08), -18.0, "beast"),
    "mire_snapper_death": (snapper_death, 2, (0.6, 0.1), -17.0, "beast"),
    "mire_snapper_step": (snapper_step, 4, None, -24.0, "beast"),
    "command_baton": (baton_command, 3, (1.4, 0.2), -19.0, "item"),
    "shark_bite": (shark_bite, 3, (0.5, 0.12), -16.0, "sea"),
    "shark_hurt": (shark_hurt, 3, (0.5, 0.12), -18.0, "sea"),
    "shark_death": (shark_death, 2, (0.8, 0.15), -18.0, "sea"),
    "anglerfish_bite": (angler_bite, 3, (0.6, 0.15), -17.0, "sea"),
    "anglerfish_snap": (angler_snap, 3, (0.5, 0.12), -18.0, "sea"),
    "anglerfish_hurt": (angler_hurt, 3, (0.6, 0.15), -19.0, "sea"),
    "anglerfish_death": (angler_death, 2, (1.0, 0.2), -18.0, "sea"),
    "manta_ray_leap": (manta_leap, 3, (0.5, 0.1), -18.0, "sea"),
    "manta_ray_splash": (manta_splash, 3, (0.6, 0.12), -15.0, "sea"),
    "manta_ray_hurt": (manta_hurt, 3, (0.6, 0.15), -20.0, "sea"),
    "manta_ray_death": (manta_death, 2, (1.0, 0.2), -19.0, "sea"),
    "giant_crab_step": (crab_step, 4, None, -25.0, "sea"),
    "glow_jellyfish_hurt": (jelly_hurt, 3, (0.8, 0.2), -21.0, "sea"),
    "glow_jellyfish_death": (jelly_death, 2, (1.2, 0.25), -20.0, "sea"),
    "void_maw_bite": (maw_bite, 3, (1.4, 0.25), -16.0, "void"),
    "void_maw_hurt": (maw_hurt, 3, (1.2, 0.2), -17.0, "void"),
    "void_maw_death": (maw_death, 1, (2.0, 0.3), -16.0, "void"),
}

# Ereignis: (Klang, Reichweite in Blöcken)
EVENTS = {
    "entity.cave_troll.ambient": ("cave_troll_ambient", 24),
    "entity.cave_troll.hurt": ("cave_troll_hurt", 24),
    "entity.cave_troll.death": ("cave_troll_death", 32),
    "entity.cave_troll.petrify": ("cave_troll_petrify", 24),
    "entity.cave_troll.smash": ("cave_troll_smash", 24),
    "entity.cave_troll.step": ("cave_troll_step", 16),
    "entity.lindworm.ambient": ("lindworm_ambient", 20),
    "entity.lindworm.bite": ("lindworm_bite", 20),
    "entity.lindworm.hurt": ("lindworm_hurt", 20),
    "entity.lindworm.death": ("lindworm_death", 24),
    "entity.lindworm.slither": ("lindworm_slither", 12),
    "entity.deer.ambient": ("deer_ambient", 16),
    "entity.deer.hurt": ("deer_hurt", 16),
    "entity.deer.death": ("deer_death", 16),
    "entity.deer.step": ("deer_step", 12),
    "entity.primeval_elk.ambient": ("primeval_elk_ambient", 24),
    "entity.primeval_elk.bugle": ("primeval_elk_bugle", 64),
    "entity.primeval_elk.angry": ("primeval_elk_angry", 24),
    "entity.primeval_elk.hurt": ("primeval_elk_hurt", 24),
    "entity.primeval_elk.death": ("primeval_elk_death", 32),
    "entity.primeval_elk.eat": ("primeval_elk_eat", 12),
    "entity.primeval_elk.impact": ("primeval_elk_impact", 24),
    "entity.primeval_elk.step": ("primeval_elk_step", 16),
    "entity.goose.ambient": ("goose_ambient", 24),
    "entity.goose.hiss": ("goose_hiss", 16),
    "entity.goose.hurt": ("goose_hurt", 16),
    "entity.goose.death": ("goose_death", 16),
    "entity.flamingo.hurt": ("flamingo_hurt", 16),
    "entity.flamingo.death": ("flamingo_death", 16),
    "entity.mire_snapper.ambient": ("mire_snapper_ambient", 16),
    "entity.mire_snapper.hiss": ("mire_snapper_hiss", 16),
    "entity.mire_snapper.snap": ("mire_snapper_snap", 16),
    "entity.mire_snapper.hurt": ("mire_snapper_hurt", 16),
    "entity.mire_snapper.death": ("mire_snapper_death", 20),
    "entity.mire_snapper.step": ("mire_snapper_step", 12),
    "item.command_baton.command": ("command_baton", 24),
    "entity.shark.bite": ("shark_bite", 20),
    "entity.shark.hurt": ("shark_hurt", 20),
    "entity.shark.death": ("shark_death", 24),
    "entity.anglerfish.bite": ("anglerfish_bite", 20),
    "entity.anglerfish.snap": ("anglerfish_snap", 16),
    "entity.anglerfish.hurt": ("anglerfish_hurt", 16),
    "entity.anglerfish.death": ("anglerfish_death", 20),
    "entity.manta_ray.leap": ("manta_ray_leap", 32),
    "entity.manta_ray.splash": ("manta_ray_splash", 48),
    "entity.manta_ray.hurt": ("manta_ray_hurt", 20),
    "entity.manta_ray.death": ("manta_ray_death", 24),
    "entity.giant_crab.step": ("giant_crab_step", 12),
    "entity.glow_jellyfish.hurt": ("glow_jellyfish_hurt", 12),
    "entity.glow_jellyfish.death": ("glow_jellyfish_death", 16),
    "entity.void_maw.bite": ("void_maw_bite", 24),
    "entity.void_maw.hurt": ("void_maw_hurt", 24),
    "entity.void_maw.death": ("void_maw_death", 32),
}


def files_of(name: str) -> list[str]:
    folder = FOLDERS[SYNTH[name][4]]
    return [f"kingdomsatwar:{folder}/{name}{i + 1}" for i in range(SYNTH[name][1])]


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level, folder = SYNTH[name]
    out = SOUNDS / FOLDERS[folder]
    out.mkdir(parents=True, exist_ok=True)
    for i in range(count):
        x = fn(5300 + i * 43 + zlib.crc32(name.encode()) % 983)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 2100 + i, stereo=False, damping=6000), space[1])
        x = filt(x, "high", 30.0, SR, 2)
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.05)
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
