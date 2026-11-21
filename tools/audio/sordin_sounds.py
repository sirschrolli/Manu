#!/usr/bin/env python3
"""
Die Klänge der Stillen Kathedrale (1.7): Sordin, der Kantor der Stille, und seine Orgel.

Die Orgel ist aus Labialpfeifen gebaut (Grundton, Oktave, Duodezime, ein Hauch Wind und das „Spucken“ beim Ansprechen),
dazu im Fortissimo Zungenstimmen (scharfer Sägezahn) und ein Pedal, das man mehr spürt als hört. Die Stimmgabel klingt
fast rein (ein langer Sinus, ein kurzer, heller Klirrton darüber), der Kantor selbst singt mit hohler Stimme. Im Einzelnen:

  * die Orgel, die lautlos spielt (von fern ein gedämpfter, langsamer Akkordwechsel), das Summen des Kantors;
  * das Erwachen (eine dissonante Schwellung, die sich in einen Moll-Akkord löst, darüber eine Stimme, am Ende die Gabel);
  * der Hieb (Wind, dann die Gabel), der Anschlag der Gabel, die Schallwelle (ein Sog, ein tiefer Schlag, ein greller Ton);
  * der Akkord (ein Orgelstoß in d-Moll), der Notenschwarm (eine Stimme, die eine Tonleiter hinaufläuft, mit Flöten);
  * die Stille („Psst“ und ein Bordun, der verklingt), das Fortissimo (das volle Werk mit Pedal), das Taumeln (ein
    verstimmter Cluster, als hielte jemand die falschen Tasten), Schmerz (zwei Pfeifen einen Halbton auseinander), der Tod
    (der Wind geht aus, der Akkord sinkt und stirbt), die Schritte (Tuch und Knochen);
  * das Aufleuchten des Orgelpults und die Resonanz der Stimmgabel in der Hand des Spielers.

Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py); es werden keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/sordin_sounds.py [--preview /pfad] [--only name …]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/deep/<klang><n>.ogg
    Einträge "entity.sordin.*", "block.organ_desk.kindle", "item.tuning_fork.resonate" in assets/kingdomsatwar/sounds.json
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, audible_bass, curve, env_ad, env_asr, fade_edges, filt, fit_level, formant_filter, glide,  # noqa: E402
                          rng, room, saw, soft_clip, tone, trim_tail)
from dsp import write_ogg  # noqa: E402
from forge_sounds import impact, mix_at, noise, thud  # noqa: E402
from mystik_sounds import whoosh  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/deep"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100

D3, F3, A3, C4, D4, E4, F4, G4, A4, C5, D5 = 146.83, 174.61, 220.0, 261.63, 293.66, 329.63, 349.23, 392.0, 440.0, 523.25, 587.33


# =====================================================================================
# Bausteine
# =====================================================================================

def pipe(f: float, dur: float, seed: int, attack: float = 0.06, release: float = 0.25, breath: float = 0.08, chiff: float = 0.5,
         bright: float = 1.0, fvar: np.ndarray | None = None) -> np.ndarray:
    """Eine Labialpfeife: Grundton, Oktave, Duodezime und Superoktave (mit etwas Wind darin), beim Ansprechen ein kurzes
    „Spucken“ (Rauschen um die Oktave), dann ein ruhiger Ton mit leisem Schweben."""
    r = rng(seed)
    n = int(dur * SR)
    t = np.arange(n) / SR
    freq = np.full(n, f) * (1 + 0.0015 * np.sin(2 * np.pi * r.uniform(4.5, 5.5) * t)) if fvar is None else fvar
    env = env_asr(n, SR, attack, release)
    y = tone(freq, env, SR, harmonics=((1, 1.0), (2, 0.42 * bright), (3, 0.22 * bright), (4, 0.1 * bright), (6, 0.04 * bright)))
    wind = filt(r.standard_normal(n), "band", [f * 1.6, f * 3.2], SR, 2) * env * breath
    m = min(n, int(0.06 * SR))
    burst = filt(r.standard_normal(m), "band", [f * 1.8, f * 2.6], SR, 2) * env_ad(m, SR, 0.002, 0.03) * chiff
    y += wind
    y[:m] += burst
    return y


def reed(f: float, dur: float, seed: int, attack: float = 0.04, release: float = 0.3) -> np.ndarray:
    """Eine Zungenstimme (Trompete): ein heller, schnarrender Sägezahn durch zwei Formanten."""
    r = rng(seed)
    n = int(dur * SR)
    t = np.arange(n) / SR
    freq = f * (1 + 0.002 * np.sin(2 * np.pi * r.uniform(5.0, 6.0) * t))
    y = saw(freq, env_asr(n, SR, attack, release), SR, partials=36, rolloff=0.9)
    return formant_filter(y, SR, [(f * 2.0, f * 0.8, 0.7), (1400.0, 600.0, 0.6), (2800.0, 900.0, 0.25)]) + y * 0.15


def chord(freqs, dur: float, seed: int, **kw) -> np.ndarray:
    """Mehrere Pfeifen zugleich (je etwas verstimmt, wie in einer echten Orgel)."""
    r = rng(seed)
    y = np.zeros(int(dur * SR))
    for i, f in enumerate(freqs):
        y += pipe(f * (1 + r.uniform(-0.0015, 0.0015)), dur, seed + 11 * i, **kw) / len(freqs) ** 0.5
    return y


def fork(f: float, dur: float, seed: int, clang: float = 0.35) -> np.ndarray:
    """Die Stimmgabel: ein fast reiner, langer Ton, darüber kurz der helle Klirrton (der sechste Teilton der Zinken)."""
    r = rng(seed)
    n = int(dur * SR)
    t = np.arange(n) / SR
    y = np.sin(2 * np.pi * f * t + r.uniform(0, 6.28)) * np.exp(-t / (dur * 0.45))
    y *= 1 + 0.06 * np.sin(2 * np.pi * r.uniform(1.0, 2.0) * t)                    # das Schweben der beiden Zinken
    y += clang * np.sin(2 * np.pi * f * 6.27 * t) * np.exp(-t / 0.06)
    m = min(n, int(0.01 * SR))
    y[:m] += filt(r.standard_normal(m), "high", 2500, SR, 2) * env_ad(m, SR, 0.0005, 0.004) * 0.6
    y *= env_ad(n, SR, 0.001, None)
    k = max(1, int(n * 0.25))
    y[n - k:] *= np.linspace(1.0, 0.0, k) ** 2                                      # sanft ausklingen (kein Knacken am Ende)
    return y


def sing(n: int, f: np.ndarray, amp: np.ndarray, vowel: str, seed: int) -> np.ndarray:
    """Die Stimme des Kantors: hohl und alt, ein Sägezahn durch die Formanten eines Vokals, leicht verstimmt doppelt."""
    r = rng(seed)
    t = np.arange(n) / SR
    wob = 1 + 0.006 * np.sin(2 * np.pi * r.uniform(4.5, 5.5) * t)
    y = saw(f * wob, amp, SR, partials=40, rolloff=1.2) + 0.5 * saw(f * wob * 1.008, amp, SR, partials=30, rolloff=1.25)
    formants = {"a": [(700, 110, 1.0), (1150, 150, 0.55), (2600, 300, 0.12)],
                "o": [(450, 90, 1.0), (800, 120, 0.55), (2500, 300, 0.1)],
                "u": [(330, 70, 1.0), (680, 110, 0.32), (2400, 300, 0.06)]}[vowel]
    return formant_filter(y, SR, formants)


# =====================================================================================
# Die Klänge
# =====================================================================================

def ambient(seed: int, i: int) -> np.ndarray:
    """Er summt: ein tiefes „u“ unter einem kaum hörbaren Pfeifenton."""
    dur = 2.4
    n = int(dur * SR)
    f = np.full(n, [D3, F3 * 0.75, A3 * 0.66][i % 3] * 0.5)
    y = sing(n, f, curve(n, [(0, 0), (0.25, 0.7), (0.75, 0.6), (1, 0)]), "u", seed) * 0.8
    mix_at(y, pipe(D4 if i % 2 else A3, 1.8, seed + 1, attack=0.3, release=0.8, chiff=0.0) * 0.18, 0.3)
    return y


def organ(seed: int, i: int) -> np.ndarray:
    """Die Orgel, die lautlos spielt: von fern, gedämpft, ein langsamer Wechsel zweier Moll-Akkorde."""
    dur = 5.0
    y = np.zeros(int(dur * SR))
    progressions = [((D3, F3, A3, D4), (C4 * 0.5 * 1.5, E4 * 0.5 * 1.5, G4 * 0.5 * 1.5)),
                    ((A3 * 0.5, C4 * 0.5, E4 * 0.5, A3), (D3, F3, A3)),
                    ((F3 * 0.5 * 1.5, A3 * 0.75, C4 * 0.75), (D3, F3, A3, D4))]
    first, second = progressions[i % len(progressions)]
    mix_at(y, chord(first, 2.8, seed, attack=0.6, release=1.2, chiff=0.1), 0.0)
    mix_at(y, chord(second, 2.6, seed + 50, attack=0.6, release=1.4, chiff=0.1), 2.2)
    return filt(y, "low", 1400, SR, 2)                                                 # fern und gedämpft


def awaken(seed: int, i: int) -> np.ndarray:
    """Er erwacht: eine dissonante Schwellung (Halbtöne übereinander), die sich in d-Moll löst; darüber singt er „ah“; am
    Ende schlägt er die Gabel an."""
    dur = 4.6
    n = int(dur * SR)
    y = np.zeros(n)
    cluster = chord((D3, D3 * 1.0595, F3, F3 * 1.0595, A3 * 0.944), 2.0, seed, attack=1.2, release=0.4, chiff=0.0, breath=0.12)
    mix_at(y, cluster, 0.0, 0.9)
    mix_at(y, chord((D3 * 0.5, D3, F3, A3, D4), 2.4, seed + 7, attack=0.08, release=1.0, chiff=0.6), 1.85, 1.0)
    m = int(2.2 * SR)
    voice = sing(m, glide(m, A3, D4, 0.6), curve(m, [(0, 0), (0.15, 0.8), (0.8, 0.7), (1, 0)]), "a", seed + 3)
    mix_at(y, voice, 1.9, 0.55)
    mix_at(y, fork(A4, 1.2, seed + 9), 3.4, 0.9)
    return y


def swing(seed: int, i: int) -> np.ndarray:
    """Der Hieb: die Gabel saust durch die Luft (ein Wind, der steigt und fällt), dann summt sie nach."""
    n = int(0.7 * SR)
    y = whoosh(n, seed, 300, 2400, 0.9) * curve(n, [(0, 0), (0.45, 1.0), (1, 0)]) * 0.9
    mix_at(y, fork(A4 * (1.0 + 0.05 * i), 0.5, seed + 1, clang=0.2) * 0.25, 0.3)
    return y


def fork_strike(seed: int, i: int) -> np.ndarray:
    """Der Anschlag der Gabel: ein heller Klirrton, dann ein langer, reiner Ton."""
    return fork([A4, A4 * 1.122, A4 * 0.891][i % 3], 2.2, seed, clang=0.45)


def sonic(seed: int, i: int) -> np.ndarray:
    """Die Schallwelle: die Luft saugt sich zusammen, ein tiefer Schlag, darüber ein greller, schwebender Ton, der verhallt."""
    dur = 2.0
    n = int(dur * SR)
    y = np.zeros(n)
    m = int(0.35 * SR)
    mix_at(y, whoosh(m, seed, 200, 3000, 1.2) * curve(m, [(0, 0), (0.9, 1.0), (1, 0)]) * 0.6, 0.0)
    mix_at(y, thud(int(0.9 * SR), 120, 38, 0.3) * 1.3, 0.33)
    mix_at(y, impact(int(0.3 * SR), seed + 1, 300, 3000, 0.06) * 0.7, 0.33)
    k = int(1.4 * SR)
    t = np.arange(k) / SR
    ring = (np.sin(2 * np.pi * 880 * t) + 0.6 * np.sin(2 * np.pi * 886.5 * t) + 0.3 * np.sin(2 * np.pi * 1320 * t)) * np.exp(-t / 0.45)
    mix_at(y, ring * env_ad(k, SR, 0.005, None) * 0.45, 0.34)
    return y


def chord_stab(seed: int, i: int) -> np.ndarray:
    """Ein Akkord: ein kurzer Stoß des Orgelwerks in d-Moll (Pfeifen und Zungen), der im Raum verhallt."""
    dur = 1.6
    y = chord((D3 * 0.5, D3, F3, A3, D4), dur, seed, attack=0.02, release=0.9, chiff=0.8)
    y += 0.35 * sum(reed(f, dur, seed + 20 + k) for k, f in enumerate((D3, A3))) * env_ad(int(dur * SR), SR, 0.02, 0.5)
    return y


def notes(seed: int, i: int) -> np.ndarray:
    """Der Notenschwarm: er singt eine Tonleiter hinauf („a“), zwei Flöten laufen mit, oben bleibt ein Ton stehen."""
    dur = 2.6
    y = np.zeros(int(dur * SR))
    scale = (D4, E4, F4, G4, A4, C5, D5)
    for k, f in enumerate(scale):
        at = 0.12 + k * 0.22
        m = int(0.42 * SR)
        mix_at(y, sing(m, np.full(m, f / 2), env_asr(m, SR, 0.03, 0.15), "a", seed + k) * 0.45, at)
        mix_at(y, pipe(f * 2, 0.38, seed + 30 + k, attack=0.02, release=0.15, breath=0.18, chiff=0.7, bright=0.4) * 0.35, at)
    mix_at(y, pipe(D5 * 2, 0.9, seed + 99, attack=0.05, release=0.6, breath=0.2, bright=0.4) * 0.3, 0.12 + 7 * 0.22)
    return y


def hush(seed: int, i: int) -> np.ndarray:
    """Die Stille: ein langes „Psst“, darunter ein Bordun, der verklingt (und danach nichts)."""
    dur = 2.2
    n = int(dur * SR)
    r = rng(seed)
    m = int(1.0 * SR)
    sh = filt(r.standard_normal(m), "band", [2400, 7500], SR, 2) * curve(m, [(0, 0), (0.1, 1.0), (0.7, 0.8), (1, 0)])
    y = np.zeros(n)
    mix_at(y, sh * 0.8, 0.0)
    mix_at(y, chord((D3 * 0.5, A3 * 0.5), 1.8, seed + 1, attack=0.2, release=1.2, chiff=0.0) * 0.5, 0.1)
    return y


def fortissimo(seed: int, i: int) -> np.ndarray:
    """Das Fortissimo: das volle Werk – Pfeifen in allen Oktaven, Zungen, darunter das Pedal –, das anschwillt und mit einem
    Schlag losbricht."""
    dur = 4.0
    n = int(dur * SR)
    y = np.zeros(n)
    swell = chord((D3 * 0.5, D3, F3, A3, D4), 1.9, seed, attack=1.6, release=0.1, chiff=0.0, breath=0.12) * 0.8
    mix_at(y, swell, 0.0)
    full = chord((D3 * 0.25, D3 * 0.5, D3, F3, A3, D4, F4, A4, D5), 2.1, seed + 5, attack=0.03, release=1.2, chiff=0.9)
    for k, f in enumerate((D3, F3, A3, D4)):
        full += 0.3 * reed(f, 2.1, seed + 40 + k, attack=0.03, release=1.2)
    mix_at(y, full, 1.9, 1.0)
    mix_at(y, thud(int(1.4 * SR), 70, 30, 0.6) * 1.2, 1.9)
    return y


def stagger(seed: int, i: int) -> np.ndarray:
    """Jemand spielt auf der Orgel: ein verstimmter Cluster (falsche Tasten), dazu stöhnt der Kantor auf."""
    dur = 2.4
    n = int(dur * SR)
    y = chord((F3, F3 * 1.06, G4 * 0.5 * 1.03, C4 * 1.02, C4 * 1.08), 1.8, seed, attack=0.02, release=0.6, chiff=0.9) * 0.9
    out = np.zeros(n)
    mix_at(out, y, 0.0)
    m = int(1.6 * SR)
    groan = sing(m, glide(m, D3 * 0.6, D3 * 0.45, 0.8), curve(m, [(0, 0), (0.2, 0.9), (1, 0)]), "o", seed + 3)
    mix_at(out, groan * 0.6, 0.3)
    return out


def hurt(seed: int, i: int) -> np.ndarray:
    """Schmerz: zwei Pfeifen einen Halbton auseinander kreischen kurz auf, das Tuch raschelt."""
    dur = 0.7
    f = [A4, C5, G4][i % 3]
    y = chord((f, f * 1.0595), dur, seed, attack=0.01, release=0.3, chiff=1.0, bright=1.4)
    y += impact(int(dur * SR), seed + 1, 800, 5000, 0.08) * 0.3
    return y


def death(seed: int, i: int) -> np.ndarray:
    """Der Tod: der Wind geht aus – der Akkord sinkt (die Bälge leeren sich) und stirbt, zuletzt ein Seufzen."""
    dur = 4.4
    n = int(dur * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    sink = 1.0 - 0.45 * np.clip((t - 0.6) / 3.2, 0.0, 1.0) ** 1.5
    for k, f in enumerate((D3 * 0.5, D3, F3, A3, D4)):
        y += pipe(f, dur, seed + k, attack=0.05, release=1.6, chiff=0.3, fvar=f * sink) * 0.45
    y *= curve(n, [(0, 1.0), (0.5, 0.8), (0.85, 0.25), (1, 0.0)])
    m = int(1.4 * SR)
    sigh = filt(rng(seed + 9).standard_normal(m), "band", [400, 2200], SR, 2) * curve(m, [(0, 0), (0.2, 0.7), (1, 0)])
    mix_at(y, sigh * 0.4, 2.9)
    return y


def step(seed: int, i: int) -> np.ndarray:
    """Ein Schritt: schweres Tuch, das über den Boden streicht, und ein Knöchel, der auf Stein tippt."""
    n = int(0.35 * SR)
    r = rng(seed)
    y = filt(r.standard_normal(n), "band", [300, 2400], SR, 2) * env_ad(n, SR, 0.02, 0.12) * 0.6
    mix_at(y, impact(int(0.08 * SR), seed + 1, 1200, 4500, 0.012) * 0.5, 0.05 + 0.02 * i)
    return y


def kindle(seed: int, i: int) -> np.ndarray:
    """Das Orgelpult leuchtet auf: ein weicher Akkord, darüber ein gläsernes Glitzern (Hallkristall)."""
    dur = 2.6
    y = chord((D4, F4, A4) if i == 0 else (D3, A3, D4), dur, seed, attack=0.3, release=1.2, chiff=0.2) * 0.8
    for k in range(5):
        mix_at(y, fork(1200 + 340 * k, 0.6, seed + 20 + k, clang=0.1) * 0.12, 0.25 + k * 0.12)
    return y


def resonate(seed: int, i: int) -> np.ndarray:
    """Die Resonanz der Stimmgabel in der Hand: der Anschlag, ein kurzer Schallstoß, dann ein langer Ton."""
    dur = 1.8
    n = int(dur * SR)
    y = np.zeros(n)
    mix_at(y, fork(A4 * (1.0 + 0.03 * i), dur - 0.05, seed, clang=0.4), 0.0)
    mix_at(y, thud(int(0.5 * SR), 160, 60, 0.15) * 0.7, 0.04)
    m = int(0.4 * SR)
    mix_at(y, whoosh(m, seed + 3, 1500, 300, 1.0) * curve(m, [(0, 0), (0.15, 1.0), (1, 0)]) * 0.5, 0.04)
    return y


# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil), Ziel-Pegel dBFS)
SYNTH = {
    "sordin_ambient": (ambient, 3, (2.0, 0.4), -20.0),
    "sordin_organ": (organ, 3, (3.2, 0.55), -19.0),
    "sordin_hurt": (hurt, 3, (1.6, 0.35), -16.0),
    "sordin_death": (death, 1, (3.0, 0.45), -14.0),
    "sordin_step": (step, 3, (0.6, 0.15), -22.0),
    "sordin_awaken": (awaken, 1, (3.0, 0.45), -13.0),
    "sordin_swing": (swing, 3, (0.9, 0.2), -18.0),
    "sordin_fork": (fork_strike, 3, (1.8, 0.35), -17.0),
    "sordin_sonic": (sonic, 2, (1.6, 0.3), -13.0),
    "sordin_chord": (chord_stab, 2, (2.6, 0.45), -14.0),
    "sordin_notes": (notes, 2, (2.4, 0.45), -16.0),
    "sordin_hush": (hush, 2, (2.4, 0.45), -18.0),
    "sordin_fortissimo": (fortissimo, 1, (3.2, 0.5), -12.0),
    "sordin_stagger": (stagger, 2, (2.0, 0.4), -15.0),
    "organ_desk_kindle": (kindle, 2, (2.4, 0.45), -17.0),
    "tuning_fork_resonate": (resonate, 3, (1.2, 0.25), -16.0),
}

# Ereignis: (Klang, Reichweite)
EVENTS = {
    "entity.sordin.ambient": ("sordin_ambient", 24),
    "entity.sordin.organ": ("sordin_organ", 48),
    "entity.sordin.hurt": ("sordin_hurt", 32),
    "entity.sordin.death": ("sordin_death", 64),
    "entity.sordin.step": ("sordin_step", 16),
    "entity.sordin.awaken": ("sordin_awaken", 64),
    "entity.sordin.swing": ("sordin_swing", 32),
    "entity.sordin.fork": ("sordin_fork", 40),
    "entity.sordin.sonic": ("sordin_sonic", 56),
    "entity.sordin.chord": ("sordin_chord", 56),
    "entity.sordin.notes": ("sordin_notes", 48),
    "entity.sordin.hush": ("sordin_hush", 40),
    "entity.sordin.fortissimo": ("sordin_fortissimo", 72),
    "entity.sordin.stagger": ("sordin_stagger", 40),
    "block.organ_desk.kindle": ("organ_desk_kindle", 32),
    "item.tuning_fork.resonate": ("tuning_fork_resonate", 24),
}

# sehr tiefe Klänge bekommen Obertöne ihres Basses, damit man sie auch auf kleinen Lautsprechern hört
AUDIBLE = {"sordin_ambient", "sordin_fortissimo", "sordin_death", "sordin_sonic"}


def files_of(name: str) -> list[str]:
    return [f"kingdomsatwar:entity/deep/{name}{i + 1}" for i in range(SYNTH[name][1])]


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    for i in range(count):
        x = fn(17100 + i * 37 + zlib.crc32(name.encode()) % 997, i)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 2700 + i, stereo=False, damping=6000), space[1])
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
