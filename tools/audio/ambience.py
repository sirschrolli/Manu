#!/usr/bin/env python3
"""
Erzeugt die Umgebungsklänge der Biome: nahtlose Klangteppiche (Wind, Blätter, Grillen, Vogelchor, Brandung, Glut,
Eis, Schattenklänge …) und Einzelklänge, die im Spiel an zufälligen Orten um den Spieler erklingen (Vogelrufe, knarrende
Bäume, knackendes Eis, Flüstern …). Alles entsteht aus Rauschen und Tönen – es gibt keine Aufnahmen und keine fremden
Klänge; wo Vanilla-Klänge mitspielen, verweist sounds.json nur auf sie (es werden keine Minecraft-Dateien kopiert).

Welche Klänge in welchem Biom zu hören sind, legt die Mod in {@code client/ambience/AmbienceProfiles.java} fest.

Aufruf (aus dem Projektordner):
    python3 tools/audio/ambience.py               # alle Klänge
    python3 tools/audio/ambience.py --only wind_gale,bird_owl --preview /pfad  # einzelne, mit Spektrogrammen
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/ambient/bed/<name>.ogg      (Stereo, Schleife)
    src/main/resources/assets/kingdomsatwar/sounds/ambient/spot/<name><n>.ogg  (Mono, positioniert)
    Einträge "ambient.bed.<name>" und "ambient.spot.<name>" in assets/kingdomsatwar/sounds.json
    src/main/java/.../registry/AmbientSounds.java (Liste der Ereignisse)
"""
from __future__ import annotations

import argparse
import json
import math
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (Bed, apply_room, band, bell, curve, distance, env_ad, env_asr, filt, filt_circular,  # noqa: E402
                          fit_level, formant_filter, glide, highpass_gain, lowpass_gain, periodic_curve, resample_curve,
                          resonator, rng, room, saw, shaped_noise, smoothstep, tilt, tone, trim_tail, fade_edges,
                          colored_noise, soft_clip, pan_gains)
from dsp import write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/ambient"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
JAVA = ROOT / "src/main/java/io/github/sirschrolli/kingdomsatwar/registry/AmbientSounds.java"

BED_SR = 32000      # Teppiche mit hellen Anteilen (Grillen, Blätter)
LOW_SR = 22050      # dunkle Teppiche (Grollen, Dröhnen, Wasser)
ONE_SR = 44100      # Einzelklänge

BED_LEVEL = -23.0   # Effektivwert der Teppiche (dBFS) – die Lautstärke im Spiel kommt aus dem Profil
SPOT_LEVEL = -19.0  # Einzelklänge


# =====================================================================================
# Hilfen
# =====================================================================================

def ctl(c: np.ndarray, t, seconds: float):
    """Wert einer kreisförmigen Steuerkurve (über die ganze Schleife) zu Zeiten t (Sekunden)."""
    m = len(c)
    x = (np.asarray(t) / seconds * m) % m
    i = np.floor(x).astype(int)
    f = x - i
    return c[i % m] * (1 - f) + c[(i + 1) % m] * f


def audio_curve(c: np.ndarray, n: int) -> np.ndarray:
    return resample_curve(c, n)


def concat(sr: int, *parts) -> np.ndarray:
    """Teile hintereinander: Arrays werden angehängt, Zahlen sind Pausen in Sekunden."""
    out = []
    for p in parts:
        if isinstance(p, (int, float)):
            out.append(np.zeros(int(p * sr)))
        else:
            out.append(np.asarray(p, dtype=float))
    return np.concatenate(out) if out else np.zeros(1)


def note(sr: int, dur: float, f0: float, f1: float | None = None, *, shape: float = 1.0, vib: float = 0.0,
         vib_rate: float = 25.0, harm=((1, 1.0), (2, 0.07), (3, 0.015)), att: float = 0.01, rel: float = 0.03,
         amp: float = 1.0, breath: float = 0.0, seed: int = 0) -> np.ndarray:
    n = max(8, int(dur * sr))
    f = glide(n, f0, f1 if f1 is not None else f0, shape)
    if vib:
        t = np.arange(n) / sr
        f = f * (1 + vib * np.sin(2 * np.pi * vib_rate * t + rng(seed).uniform(0, 6.28)))
    a = env_asr(n, sr, att, rel) * amp
    y = tone(f, a, sr, harm)
    if breath:
        fm = float(np.median(f))
        nz = filt(rng(seed + 1).standard_normal(n), "band", [fm * 0.75, fm * 1.6], sr, 2)
        nz = filt(nz, "low", fm * 2.5, sr, 2)
        y += breath * nz * a / (np.std(nz) + 1e-9) * 0.3
    return y


def finish_spot(x: np.ndarray, sr: int, level: float = SPOT_LEVEL) -> np.ndarray:
    x = x - np.mean(x)
    x = trim_tail(x, sr)
    x = fade_edges(x, sr, 0.004, 0.06)
    return soft_clip(fit_level(x, level, -4.0))


def finish_bed(bed: Bed, level: float = BED_LEVEL) -> np.ndarray:
    y = bed.buf - np.mean(bed.buf, axis=0, keepdims=True)
    y = fit_level(y, level, -5.0)
    return soft_clip(y)


# =====================================================================================
# Vögel und andere Tierstimmen (Einzelklänge, auch im Chor der Teppiche)
# =====================================================================================

def bird_blackbird(sr: int, seed: int) -> np.ndarray:
    """Amsel: flötende, weiche Strophe mit Gleittönen, am Ende ein leises, hohes Zwitschern."""
    r = rng(seed)
    base = r.uniform(1700, 2350)
    parts = []
    for i in range(int(r.integers(4, 7))):
        f0 = base * r.uniform(0.82, 1.32)
        f1 = f0 * r.uniform(0.72, 1.32)
        parts += [note(sr, r.uniform(0.09, 0.22), f0, f1, shape=r.uniform(0.6, 1.7), vib=r.choice([0.0, 0.0, 0.012]),
                       vib_rate=28, harm=((1, 1.0), (2, 0.13), (3, 0.03)), att=0.018, rel=0.045, amp=r.uniform(0.7, 1.0),
                       seed=seed + i), r.uniform(0.03, 0.09)]
    for j in range(int(r.integers(4, 9))):
        f0 = r.uniform(4300, 6600)
        parts += [note(sr, r.uniform(0.018, 0.036), f0, f0 * r.uniform(0.6, 0.85), att=0.003, rel=0.009,
                       harm=((1, 1.0), (2, 0.05)), amp=0.45), r.uniform(0.012, 0.03)]
    return concat(sr, *parts)


def bird_tit(sr: int, seed: int) -> np.ndarray:
    """Kohlmeise: „zi-zi-bä“ – helles Doppelmotiv, mehrmals wiederholt."""
    r = rng(seed)
    hi = r.uniform(4600, 5700)
    lo = hi * r.uniform(0.64, 0.78)
    parts = []
    for i in range(int(r.integers(3, 6))):
        parts += [note(sr, 0.075, hi, hi * 0.93, att=0.005, rel=0.016, harm=((1, 1.0), (2, 0.04))), 0.035,
                  note(sr, 0.11, lo * 1.03, lo * 0.95, att=0.006, rel=0.03, harm=((1, 1.0), (2, 0.06))), 0.075]
    return concat(sr, *parts)


def bird_finch(sr: int, seed: int) -> np.ndarray:
    """Buchfink: schmetternder Triller, der abwärts läuft und schneller wird, dann ein Schnörkel."""
    r = rng(seed)
    parts = []
    k = int(r.integers(5, 8))
    for i in range(k):
        top = 6100 - i * r.uniform(150, 230)
        parts += [note(sr, 0.045, top, top * 0.55, shape=0.7, att=0.003, rel=0.01, harm=((1, 1.0), (2, 0.05))),
                  0.05 - i * 0.004]
    for i in range(int(r.integers(3, 5))):
        f0 = r.uniform(3600, 4000) - i * 150
        parts += [note(sr, 0.055, f0, f0 * 0.78, att=0.004, rel=0.012), 0.035]
    parts += [note(sr, 0.06, 2500, 4300, shape=0.8, att=0.006, rel=0.01), 0.01,
              note(sr, 0.12, 4500, 2300, shape=1.4, att=0.004, rel=0.04, harm=((1, 1.0), (2, 0.1)))]
    return concat(sr, *parts)


def bird_cuckoo(sr: int, seed: int) -> np.ndarray:
    """Kuckuck: zwei weiche Töne in einer kleinen Terz, zwei- bis dreimal."""
    r = rng(seed)
    f1 = r.uniform(640, 760)
    f2 = f1 * r.uniform(0.8, 0.84)
    parts = []
    for i in range(int(r.integers(2, 4))):
        parts += [note(sr, 0.16, f1 * 1.01, f1 * 0.97, att=0.03, rel=0.05, harm=((1, 1.0), (2, 0.16), (3, 0.04)), breath=0.25,
                       seed=seed + i), 0.12,
                  note(sr, 0.28, f2 * 1.01, f2 * 0.96, att=0.035, rel=0.12, harm=((1, 1.0), (2, 0.14), (3, 0.03)), breath=0.25,
                       seed=seed + 7 + i), 0.45]
    return concat(sr, *parts)


def woodpecker(sr: int, seed: int) -> np.ndarray:
    """Specht: rasches Trommeln auf hohlem Holz, erst lauter, dann leiser."""
    r = rng(seed)
    k = int(r.integers(12, 23))
    rate = r.uniform(14, 19)
    modes = [(r.uniform(780, 980), 9.0, 1.0), (r.uniform(1550, 1850), 11.0, 0.6), (r.uniform(2500, 3000), 12.0, 0.35)]
    total = int((k / rate + 0.3) * sr)
    y = np.zeros(total)
    for i in range(k):
        t = i / rate * (1 + 0.12 * (1 - i / k))
        s = int(t * sr)
        imp = np.zeros(int(0.06 * sr))
        imp[:int(0.002 * sr)] = r.standard_normal(int(0.002 * sr))
        strike = sum(g * resonator(imp, f, q, sr) for f, q, g in modes)
        tt = np.arange(len(imp)) / sr
        strike += 0.6 * np.sin(2 * np.pi * 190 * tt) * np.exp(-tt / 0.012)
        a = min(1.0, (i + 1) / 3) * (1.0 if i < k - 4 else (k - i) / 5)
        y[s:s + len(strike)] += strike[:max(0, min(len(strike), total - s))] * a * r.uniform(0.85, 1.0)
    return y


def owl_hoot(sr: int, seed: int) -> np.ndarray:
    """Waldkauz: „huuu … hu … hu-hu-huuuuu“ – tief, weich, am Ende bebend."""
    r = rng(seed)
    f = r.uniform(370, 440)
    h = ((1, 1.0), (2, 0.1), (3, 0.03))
    a = note(sr, 0.5, f * 1.02, f * 0.96, att=0.08, rel=0.15, harm=h, breath=0.35, seed=seed)
    b = note(sr, 0.12, f * 0.98, f * 0.96, att=0.02, rel=0.05, harm=h, breath=0.3, seed=seed + 1)
    c1 = note(sr, 0.09, f * 1.0, f * 0.98, att=0.015, rel=0.04, harm=h, breath=0.3, seed=seed + 2)
    c2 = note(sr, 0.09, f * 1.01, f * 0.99, att=0.015, rel=0.04, harm=h, breath=0.3, seed=seed + 3)
    d = note(sr, 0.95, f * 1.04, f * 0.95, vib=0.03, vib_rate=8.5, att=0.03, rel=0.4, harm=h, breath=0.35, seed=seed + 4)
    return concat(sr, a, 0.9, b, 0.28, c1, 0.06, c2, 0.08, d)


def owl_kewick(sr: int, seed: int) -> np.ndarray:
    """Waldkauz-Weibchen: ein scharfes „ku-witt“."""
    r = rng(seed)
    f = r.uniform(1250, 1450)
    src = saw(glide(int(0.26 * sr), f, f * 1.35, 0.6) * curve(int(0.26 * sr), [(0, 1), (0.6, 1.1), (1, 0.85)]),
              env_asr(int(0.26 * sr), sr, 0.02, 0.09), sr, partials=8, rolloff=1.6)
    y = formant_filter(src, sr, [(1600, 500, 1.0), (3000, 700, 0.4)])
    return concat(sr, y, 0.5, y * 0.8) if r.random() < 0.4 else y


def crow(sr: int, seed: int) -> np.ndarray:
    """Krähe: ein heiseres „krah“, ein- bis dreimal."""
    r = rng(seed)
    parts = []
    for i in range(int(r.integers(1, 4))):
        n = int(r.uniform(0.28, 0.42) * sr)
        t = np.arange(n) / sr
        f0 = r.uniform(310, 420)
        f = glide(n, f0 * 1.05, f0 * 0.92, 1.0) * (1 + 0.04 * np.sin(2 * np.pi * r.uniform(30, 45) * t))
        rough = 1 + 0.5 * np.sin(2 * np.pi * r.uniform(40, 60) * t) * r.uniform(0.6, 1.0)
        src = saw(f, env_asr(n, sr, 0.02, 0.09) * rough, sr, partials=18, rolloff=1.05)
        src += 0.25 * rng(seed + i).standard_normal(n) * env_asr(n, sr, 0.01, 0.1)
        y = formant_filter(src, sr, [(950, 260, 1.0), (1550, 320, 0.65), (2650, 450, 0.25)])
        parts += [filt(y, "low", 4500, sr, 2), r.uniform(0.25, 0.45)]
    return concat(sr, *parts)


def gull(sr: int, seed: int) -> np.ndarray:
    """Möwe: nasale „kiau“-Rufe oder ein lachendes „ha-ha-ha“."""
    r = rng(seed)
    parts = []
    if r.random() < 0.5:
        for i in range(int(r.integers(2, 4))):
            n = int(r.uniform(0.28, 0.38) * sr)
            f = curve(n, [(0, 880), (0.2, 1480), (1, 1050)]) * r.uniform(0.92, 1.08)
            src = saw(f, env_asr(n, sr, 0.015, 0.08), sr, partials=12, rolloff=1.25)
            parts += [formant_filter(src, sr, [(1350, 420, 1.0), (2700, 600, 0.45), (4100, 800, 0.15)]), r.uniform(0.12, 0.25)]
    else:
        f0 = r.uniform(1050, 1250)
        for i in range(int(r.integers(6, 10))):
            n = int(0.11 * sr)
            f = glide(n, f0 * 1.04, f0 * 0.96) * (1 - i * 0.012)
            src = saw(f, env_asr(n, sr, 0.01, 0.04), sr, partials=12, rolloff=1.3)
            parts += [formant_filter(src, sr, [(1300, 400, 1.0), (2600, 600, 0.4)]), 0.055]
    return concat(sr, *parts)


def curlew(sr: int, seed: int) -> np.ndarray:
    """Brachvogel: aufsteigende, klagende Pfiffe „kur-li“, dann ein perlender, steigender Triller."""
    r = rng(seed)
    parts = []
    for i in range(2):
        parts += [note(sr, 0.45, r.uniform(1650, 1800), r.uniform(2400, 2650), shape=1.6, att=0.05, rel=0.07,
                       harm=((1, 1.0), (2, 0.05)), breath=0.08, seed=seed + i), 0.22]
    n = int(r.uniform(1.0, 1.4) * sr)
    t = np.arange(n) / sr
    rate = np.linspace(17, 27, n)
    am = 0.5 + 0.5 * np.sin(2 * np.pi * np.cumsum(rate) / sr)
    f = glide(n, 2200, 2950, 1.2) * (1 + 0.035 * np.sin(2 * np.pi * np.cumsum(rate) / sr))
    trill = tone(f, env_asr(n, sr, 0.15, 0.25) * am ** 1.5, sr, ((1, 1.0), (2, 0.04)))
    return concat(sr, *parts, trill)


def bittern(sr: int, seed: int) -> np.ndarray:
    """Rohrdommel: dumpfes, tiefes „u-WUMB“ aus dem Schilf, drei- bis viermal."""
    r = rng(seed)
    parts = []
    f = r.uniform(140, 160)
    for i in range(int(r.integers(3, 5))):
        u = note(sr, 0.12, f * 1.12, f * 1.1, att=0.03, rel=0.05, harm=((1, 1.0), (2, 0.2)), amp=0.4)
        w = note(sr, 0.38, f, f * 0.84, att=0.05, rel=0.16, harm=((1, 1.0), (2, 0.38), (3, 0.14), (4, 0.05)))
        w = np.tanh(w * 1.6) / 1.6
        parts += [u, 0.05, w, r.uniform(0.75, 1.0)]
    return filt(concat(sr, *parts), "low", 900, sr, 2)


def buzzard(sr: int, seed: int) -> np.ndarray:
    """Mäusebussard: ein langgezogener, abfallender Schrei „hiiääh“ hoch über der Schlucht."""
    r = rng(seed)
    n = int(r.uniform(0.85, 1.05) * sr)
    t = np.arange(n) / sr
    f = glide(n, r.uniform(2700, 3000), r.uniform(1600, 1800), 0.7)
    rasp = 1 + 0.25 * np.sin(2 * np.pi * 85 * t)
    src = saw(f, env_asr(n, sr, 0.05, 0.3) * rasp, sr, partials=6, rolloff=1.7)
    src += 0.04 * rng(seed).standard_normal(n) * env_asr(n, sr, 0.05, 0.3)
    y = filt(src, "band", [900, 7000], sr, 2)
    return concat(sr, y, 0.6, y * 0.6) if r.random() < 0.35 else y


def nightingale(sr: int, seed: int) -> np.ndarray:
    """Nachtigall: anschwellende „piu“-Rufe, ein schnalzender Triller, ein gluckerndes Motiv."""
    r = rng(seed)
    parts = []
    k = int(r.integers(4, 7))
    for i in range(k):
        n = int(0.17 * sr)
        f = curve(n, [(0, 1150), (0.45, 1650), (1, 1200)]) * r.uniform(0.97, 1.03)
        parts += [tone(f, env_asr(n, sr, 0.03, 0.06) * (0.35 + 0.65 * i / (k - 1)), sr, ((1, 1.0), (2, 0.08))), 0.07]
    parts.append(0.08)
    for i in range(int(r.integers(8, 15))):
        f0 = 3400 if i % 2 == 0 else 3700
        parts += [note(sr, 0.03, f0, f0 * 0.8, att=0.002, rel=0.008, harm=((1, 1.0), (2, 0.12))), 0.025]
    parts.append(0.1)
    for i in range(int(r.integers(3, 6))):
        parts += [note(sr, 0.07, 1850, 1450, att=0.006, rel=0.02, harm=((1, 1.0), (2, 0.2), (3, 0.05))), 0.05]
    return concat(sr, *parts)


def skylark_line(sr: int, seconds: float, seed: int) -> np.ndarray:
    """Feldlerche: endloses, perlendes Trillern hoch in der Luft (für den Teppich der Wiesen)."""
    r = rng(seed)
    out = []
    total = 0.0
    while total < seconds:
        f0 = r.uniform(2800, 6300)
        reps = int(r.choice([1, 1, 2, 3, 4, 6]))
        d = r.uniform(0.03, 0.08)
        for k in range(reps):
            out.append(note(sr, d, f0, f0 * r.uniform(0.8, 1.2), att=0.004, rel=0.01, harm=((1, 1.0), (2, 0.05)),
                            amp=r.uniform(0.5, 1.0)))
            g = r.uniform(0.008, 0.03)
            out.append(np.zeros(int(g * sr)))
            total += d + g
        if r.random() < 0.08:
            g = r.uniform(0.2, 0.6)
            out.append(np.zeros(int(g * sr)))
            total += g
    y = np.concatenate(out)
    return y[:int(seconds * sr)]


def wolf_howl(sr: int, seed: int) -> np.ndarray:
    """Ferner Wolf: anschwellendes, lang gehaltenes Heulen, das am Ende abfällt."""
    r = rng(seed)
    n = int(r.uniform(2.4, 3.2) * sr)
    t = np.arange(n) / sr
    f0 = r.uniform(330, 380)
    f = curve(n, [(0, f0), (0.22, f0 * 1.65), (0.75, f0 * 1.6), (1, f0 * 1.15)])
    f *= 1 + 0.012 * np.sin(2 * np.pi * 5.5 * t) * smoothstep(t / t[-1], 0.2, 0.5)
    src = saw(f, env_asr(n, sr, 0.35, 0.6), sr, partials=10, rolloff=1.9)
    vowel = curve(n, [(0, 0.0), (0.3, 1.0), (1, 0.6)])
    y = formant_filter(src, sr, [(700, 260, 1.0), (1100, 300, 0.5)]) * (0.6 + 0.4 * vowel)
    y += 0.4 * tone(f, env_asr(n, sr, 0.35, 0.6), sr, ((1, 1.0),))
    return y


def frog_croak(sr: int, seed: int) -> np.ndarray:
    """Wasserfrosch: knarrende Pulsfolge „qua-qua-qua“."""
    r = rng(seed)
    k = int(r.integers(3, 8))
    rate = r.uniform(12, 20)
    f0 = r.uniform(170, 260)
    pulse_n = int(0.028 * sr)
    total = int((k / rate + 0.1) * sr)
    y = np.zeros(total)
    for i in range(k):
        s = int(i / rate * sr)
        tt = np.arange(pulse_n) / sr
        p = saw(np.full(pulse_n, f0 * r.uniform(0.97, 1.03)), env_ad(pulse_n, sr, 0.003, 0.012), sr, partials=14, rolloff=1.1)
        p = formant_filter(p, sr, [(750, 250, 1.0), (1450, 350, 0.55)])
        y[s:s + pulse_n] += p * r.uniform(0.75, 1.0)
    return y


def toad_call(sr: int, seed: int, f: float) -> np.ndarray:
    """Unke: ein weicher, melancholischer Ruf „uuh“."""
    return note(sr, 0.24, f * 1.02, f * 0.98, att=0.045, rel=0.13, harm=((1, 1.0), (2, 0.05), (3, 0.01)), breath=0.06, seed=seed)


# =====================================================================================
# Holz, Eis, Fels, Feuer, Wasser, Strom (Einzelklänge und Ereignisse)
# =====================================================================================

def stick_slip(sr: int, seed: int, dur: float, rate_curve, modes, noise: float = 0.15) -> np.ndarray:
    """Knarren: viele winzige Ruckler (Haft-Gleit-Reibung) regen Resonanzen an – Holz, Eis oder Tauwerk."""
    r = rng(seed)
    n = int(dur * sr)
    rate = curve(n, rate_curve)
    ph = np.cumsum(rate) / sr
    ticks = np.nonzero(np.diff(np.floor(ph)) > 0)[0]
    exc = np.zeros(n)
    exc[ticks] = r.uniform(0.6, 1.0, len(ticks))
    exc += noise * r.standard_normal(n) * 0.05
    y = sum(g * resonator(exc, f, q, sr) for f, q, g in modes)
    return y * env_asr(n, sr, 0.12, 0.25)


def wood_creak(sr: int, seed: int) -> np.ndarray:
    """Alter Baum im Wind: langsames, tiefes Knarren."""
    r = rng(seed)
    base = r.uniform(140, 210)
    modes = [(base, 18, 1.0), (base * 2.3, 16, 0.6), (base * 3.9, 14, 0.3), (base * 6.1, 12, 0.12)]
    dur = r.uniform(0.9, 1.8)
    y = stick_slip(sr, seed, dur, [(0, 18), (0.3, r.uniform(45, 70)), (0.7, r.uniform(25, 40)), (1, 12)], modes)
    if r.random() < 0.5:
        y = concat(sr, y, r.uniform(0.15, 0.4), stick_slip(sr, seed + 3, dur * 0.6, [(0, 20), (0.5, 50), (1, 15)], modes) * 0.7)
    return filt(y, "low", 3000, sr, 2)


def twig_snap(sr: int, seed: int) -> np.ndarray:
    """Ein Ast knackt irgendwo im Unterholz."""
    r = rng(seed)
    n = int(0.12 * sr)
    exc = np.zeros(n)
    for k in range(int(r.integers(1, 4))):
        s = int(r.uniform(0, 0.03) * sr)
        exc[s:s + 40] += r.standard_normal(40) * r.uniform(0.5, 1.0)
    y = sum(resonator(exc, f, 7, sr) * g for f, g in ((r.uniform(1100, 1600), 1.0), (r.uniform(2600, 3400), 0.6), (5200, 0.3)))
    return y


def ice_crack(sr: int, seed: int) -> np.ndarray:
    """Eis knackt: scharfer Knall, metallisches Nachklingen und das typische, fallende „Piuu“ im Eis."""
    r = rng(seed)
    n = int(1.4 * sr)
    t = np.arange(n) / sr
    y = np.zeros(n)
    y[:int(0.003 * sr)] = r.standard_normal(int(0.003 * sr)) * 2
    ring = sum(a * np.sin(2 * np.pi * f * t) * np.exp(-t / d)
               for f, a, d in ((r.uniform(1800, 2400), 0.5, 0.25), (r.uniform(3300, 4100), 0.35, 0.18), (r.uniform(5200, 6400), 0.2, 0.12)))
    pew_f = glide(n, r.uniform(3500, 5000), r.uniform(250, 400), 0.35)
    pew = tone(pew_f, np.exp(-t / r.uniform(0.18, 0.3)), sr, ((1, 1.0), (2, 0.15)))
    thump = np.sin(2 * np.pi * 70 * t) * np.exp(-t / 0.05)
    y += 0.5 * ring + 0.6 * pew + 0.5 * thump
    return y


def ice_creak(sr: int, seed: int) -> np.ndarray:
    """Gletschereis arbeitet: ein hohes, singendes Knarren."""
    r = rng(seed)
    base = r.uniform(320, 520)
    modes = [(base, 30, 1.0), (base * 1.7, 28, 0.7), (base * 2.9, 24, 0.45), (base * 4.6, 20, 0.2)]
    y = stick_slip(sr, seed, r.uniform(0.7, 1.4), [(0, 30), (0.4, r.uniform(110, 180)), (0.8, 70), (1, 25)], modes, 0.3)
    return filt(y, "low", 4500, sr, 2)


def pebbles(sr: int, seed: int) -> np.ndarray:
    """Steinchen rieseln eine Felswand hinab und springen."""
    r = rng(seed)
    n = int(2.2 * sr)
    y = np.zeros(n)
    t = 0.0
    k = int(r.integers(8, 20))
    for i in range(k):
        t += r.uniform(0.04, 0.22) * (1 + i * 0.03)
        s = int(t * sr)
        if s >= n - 2000:
            break
        exc = np.zeros(600)
        exc[:12] = r.standard_normal(12)
        click = resonator(exc, r.uniform(1500, 4500), 6, sr) + 0.5 * resonator(exc, r.uniform(700, 1200), 5, sr)
        y[s:s + 600] += click * r.uniform(0.3, 1.0) * (1 - i / (k + 4))
    return y


def steam_vent(sr: int, seed: int) -> np.ndarray:
    """Eine Glutspalte faucht Dampf und Rauch."""
    r = rng(seed)
    dur = r.uniform(1.2, 2.2)
    n = int(dur * sr)
    t = np.arange(n) / sr
    turb = 0.7 + 0.3 * filt(r.standard_normal(n), "low", 18, sr, 2) * 8
    hiss = filt(r.standard_normal(n), "band", [1200, 7000], sr, 2) * env_asr(n, sr, 0.08, 0.6) * np.clip(turb, 0.2, 1.4)
    hiss = filt(hiss, "low", 6000, sr, 2)
    chuff = filt(r.standard_normal(n), "low", 300, sr, 2) * np.exp(-t / 0.12) * 3
    return hiss + chuff


def rumble(sr: int, seed: int, dur: float = 3.5, cutoff: float = 90.0) -> np.ndarray:
    """Fernes Grollen aus der Tiefe (Vulkan, Lawine, Erde)."""
    r = rng(seed)
    n = int(dur * sr)
    y = filt(r.standard_normal(n), "low", cutoff, sr, 4) * 6
    swell = curve(n, [(0, 0), (0.2, 1.0), (0.5, 0.7), (0.7, 0.9), (1, 0)])
    return y * swell


def thunder_far(sr: int, seed: int) -> np.ndarray:
    """Ferner Donner: mehrere überlagerte, dumpfe Rollen."""
    r = rng(seed)
    n = int(r.uniform(4.0, 6.0) * sr)
    y = np.zeros(n)
    for k in range(int(r.integers(3, 6))):
        s = int(r.uniform(0, 0.4) * n)
        m = n - s
        roll = filt(r.standard_normal(m), "low", r.uniform(160, 320), sr, 3) * 5
        t = np.arange(m) / sr
        e = (1 - np.exp(-t / r.uniform(0.08, 0.3))) * np.exp(-t / r.uniform(0.8, 1.8))
        y[s:] += roll * e * r.uniform(0.5, 1.0)
    return y


def zap(sr: int, seed: int) -> np.ndarray:
    """Knisternde Entladung: ein Bündel scharfer Funken mit summendem Bogen."""
    r = rng(seed)
    dur = r.uniform(0.25, 0.6)
    n = int(dur * sr)
    t = np.arange(n) / sr
    y = np.zeros(n)
    rate = r.uniform(300, 1800)
    times = np.cumsum(r.exponential(1 / rate, int(rate * dur * 1.5)))
    for tt in times[times < dur - 0.003]:
        s = int(tt * sr)
        y[s:s + 3] += r.uniform(-1, 1, 3)
    y = filt(y, "high", 1500, sr, 2) * 2.5
    buzz = saw(np.full(n, r.uniform(90, 130)) * (1 + 0.02 * np.sin(2 * np.pi * 7 * t)), np.ones(n), sr, 20, 1.0)
    y += 0.25 * filt(buzz, "band", [400, 4000], sr, 2)
    return y * env_asr(n, sr, 0.01, 0.08)


def bubble(sr: int, f0: float, dur: float, seed: int, rise: float = 0.7) -> np.ndarray:
    """Eine Blase platzt (Minnaert-Resonanz, die beim Aufsteigen höher wird)."""
    n = int(dur * sr)
    t = np.arange(n) / sr
    f = f0 * (1 + rise * t / dur)
    return np.sin(2 * np.pi * np.cumsum(f) / sr) * np.exp(-t / (dur / 4)) * env_ad(n, sr, 0.001)


def bog_burp(sr: int, seed: int) -> np.ndarray:
    """Gas steigt im Moor auf: ein Schwall dumpfer Blasen."""
    r = rng(seed)
    n = int(1.2 * sr)
    y = np.zeros(n)
    t = 0.0
    for i in range(int(r.integers(3, 8))):
        t += r.uniform(0.02, 0.14)
        f0 = r.uniform(110, 420) if i < 2 else r.uniform(250, 900)
        b = bubble(sr, f0, r.uniform(0.04, 0.12), seed + i)
        s = int(t * sr)
        y[s:s + len(b)] += b[:max(0, min(len(b), n - s))] * r.uniform(0.5, 1.0)
    mud = filt(r.standard_normal(n), "low", 500, sr, 2) * env_ad(n, sr, 0.02, 0.15) * 2
    return y + mud * 0.3


def drip(sr: int, seed: int) -> np.ndarray:
    """Ein Tropfen fällt in eine Pfütze (für Höhlen und Grotten)."""
    r = rng(seed)
    n = int(0.25 * sr)
    t = np.arange(n) / sr
    f0 = r.uniform(1300, 2600)
    chirp = np.sin(2 * np.pi * np.cumsum(glide(n, f0 * 0.7, f0 * 1.8, 0.5)) / sr) * np.exp(-t / 0.012)
    plink = np.sin(2 * np.pi * f0 * 1.3 * t) * np.exp(-t / r.uniform(0.025, 0.05))
    splash = filt(r.standard_normal(n), "band", [1500, 6000], sr, 2) * np.exp(-t / 0.006) * 0.3
    return chirp * 0.7 + plink * 0.45 + splash


def whale_moan(sr: int, seed: int) -> np.ndarray:
    """Fernes Rufen eines großen Meerestiers: tiefe, langsam gleitende Töne."""
    r = rng(seed)
    n = int(r.uniform(3.0, 4.5) * sr)
    f0 = r.uniform(85, 120)
    f = curve(n, [(0, f0), (0.3, f0 * 1.45), (0.6, f0 * 1.25), (1, f0 * 0.95)])
    y = saw(f, env_asr(n, sr, 0.6, 1.2), sr, partials=8, rolloff=2.2)
    y = formant_filter(y, sr, [(260, 120, 1.0), (560, 200, 0.5)])
    return filt(y, "low", 1400, sr, 2)


def sparkle(sr: int, seed: int, scale=(1174.7, 1318.5, 1480.0, 1760.0, 1975.5), octave: float = 2.0) -> np.ndarray:
    """Funkelnde Glöckchen (Zauber im Hain, Glimmerweiten)."""
    r = rng(seed)
    n = int(2.4 * sr)
    y = np.zeros(n)
    t = 0.0
    for i in range(int(r.integers(3, 7))):
        f = r.choice(scale) * octave
        b = bell(f, 1.6, sr, ratios=(1.0, 2.0, 3.01, 4.2), amps=(1.0, 0.25, 0.1, 0.05), decay=0.7, seed=seed + i)
        s = int(t * sr)
        y[s:s + len(b)] += b[:max(0, min(len(b), n - s))] * r.uniform(0.4, 1.0)
        t += r.uniform(0.05, 0.22)
    return y


# --------------------------------------------------------------------- Schattenwelt

def whisper(sr: int, seed: int, syllables: tuple[int, int] = (4, 9)) -> np.ndarray:
    """Flüstern ohne Worte: stimmlose Silben aus Rauschen, Zischlauten und wandernden Formanten."""
    r = rng(seed)
    vowels = [(300, 870, 2250), (390, 2000, 2550), (530, 1840, 2480), (660, 1720, 2410), (730, 1090, 2440),
              (570, 840, 2410), (440, 1020, 2240), (325, 700, 2530)]
    parts = []
    for i in range(int(r.integers(*syllables))):
        if r.random() < 0.55:
            m = int(r.uniform(0.04, 0.09) * sr)
            cons = filt(r.standard_normal(m), "band", [r.uniform(2800, 4000), r.uniform(6000, 8000)], sr, 2)
            parts.append(cons * env_asr(m, sr, 0.012, 0.03) * r.uniform(0.15, 0.35))
        m = int(r.uniform(0.11, 0.22) * sr)
        v0 = vowels[int(r.integers(len(vowels)))]
        v1 = vowels[int(r.integers(len(vowels)))]
        nz = r.standard_normal(m)
        y = np.zeros(m)
        half = m // 2
        for fi in range(3):
            for seg, (fa, fb) in enumerate(((v0[fi], (v0[fi] + v1[fi]) / 2), ((v0[fi] + v1[fi]) / 2, v1[fi]))):
                sl = slice(seg * half, m if seg else half)
                y[sl] += resonator(nz[sl], (fa + fb) / 2, 5 + fi * 2, sr) * (1.0, 0.7, 0.35)[fi]
        parts.append(filt(y, "low", 3400, sr, 2) * env_asr(m, sr, 0.03, 0.06) * r.uniform(0.6, 1.0))
        parts.append(np.zeros(int(r.uniform(0.02, 0.12) * sr)))
    return np.concatenate(parts)


def shadow_call(sr: int, seed: int) -> np.ndarray:
    """Ein Vogel, den es nur in der Schattenwelt gibt: ein langer, fallender, doppelt klingender Klageruf."""
    r = rng(seed)
    n = int(r.uniform(1.4, 2.0) * sr)
    t = np.arange(n) / sr
    f0 = r.uniform(1150, 1400)
    f = curve(n, [(0, f0), (0.15, f0 * 1.08), (1, f0 * 0.55)]) * (1 + 0.012 * np.sin(2 * np.pi * 6 * t))
    a = env_asr(n, sr, 0.12, 0.6)
    y = tone(f, a, sr, ((1, 1.0), (2, 0.06))) + 0.7 * tone(f * 1.012, a, sr, ((1, 1.0),), phase0=1.0)
    if r.random() < 0.5:
        y = concat(sr, y, 0.4, y[:int(n * 0.6)] * 0.6)
    return y


def soul_wail(sr: int, seed: int) -> np.ndarray:
    """Ein fernes Seufzen und Klagen aus dem Seelensumpf (Formanten „oo“ → „aa“)."""
    r = rng(seed)
    n = int(r.uniform(1.8, 2.8) * sr)
    t = np.arange(n) / sr
    f0 = r.uniform(190, 260)
    f = curve(n, [(0, f0), (0.35, f0 * 1.5), (0.7, f0 * 1.35), (1, f0 * 0.9)]) * (1 + 0.01 * np.sin(2 * np.pi * 5 * t))
    src = saw(f, env_asr(n, sr, 0.4, 0.7), sr, partials=14, rolloff=1.5)
    oo = formant_filter(src, sr, [(330, 120, 1.0), (700, 180, 0.5), (2400, 400, 0.08)])
    aa = formant_filter(src, sr, [(720, 180, 1.0), (1100, 220, 0.6), (2500, 400, 0.12)])
    morph = curve(n, [(0, 0), (0.5, 1), (1, 0.3)])
    breath = filt(filt(r.standard_normal(n), "band", [300, 2500], sr, 2), "low", 2500, sr, 2) * env_asr(n, sr, 0.3, 0.6) * 0.12
    return filt(oo * (1 - morph) + aa * morph, "low", 5000, sr, 2) + breath


def crystal_chime(sr: int, seed: int) -> np.ndarray:
    """Ein Kristall singt auf: klare Glockentöne mit langem Nachklang."""
    r = rng(seed)
    scale = (554.4, 622.3, 740.0, 830.6, 987.8, 1108.7, 1244.5)
    n = int(4.0 * sr)
    y = np.zeros(n)
    t = 0.0
    for i in range(int(r.integers(1, 4))):
        f = float(r.choice(scale)) * float(r.choice([1.0, 2.0]))
        b = bell(f, 3.5, sr, ratios=(1.0, 2.0, 2.99, 4.13, 5.42), amps=(1.0, 0.35, 0.2, 0.1, 0.06), decay=2.0, seed=seed + i)
        s = int(t * sr)
        y[s:s + len(b)] += b[:max(0, min(len(b), n - s))] * r.uniform(0.6, 1.0)
        t += r.uniform(0.15, 0.5)
    return y


def deep_moan(sr: int, seed: int) -> np.ndarray:
    """Aus der Tiefe dröhnt etwas Großes – tief, langsam, mit Schwebung."""
    r = rng(seed)
    n = int(r.uniform(3.0, 4.0) * sr)
    t = np.arange(n) / sr
    f0 = r.uniform(48, 62)
    f = curve(n, [(0, f0), (0.4, f0 * 1.3), (1, f0 * 0.85)])
    y = saw(f, env_asr(n, sr, 0.8, 1.2), sr, partials=12, rolloff=1.6) + 0.6 * saw(f * 1.007, env_asr(n, sr, 0.8, 1.2), sr, 12, 1.6)
    y = formant_filter(y, sr, [(180, 80, 1.0), (420, 140, 0.4)])
    return filt(y, "low", 900, sr, 2)


def wing_flutter(sr: int, seed: int) -> np.ndarray:
    """Weiche Flügelschläge, die vorüberziehen."""
    r = rng(seed)
    k = int(r.integers(4, 9))
    rate = r.uniform(5, 9)
    n = int((k / rate + 0.3) * sr)
    y = np.zeros(n)
    for i in range(k):
        m = int(0.09 * sr)
        s = int(i / rate * sr)
        w = filt(r.standard_normal(m), "band", [250, 2200], sr, 2) * env_asr(m, sr, 0.02, 0.06)
        y[s:s + m] += w * math.sin(math.pi * (i + 0.5) / k)
    return y


def shadow_creak(sr: int, seed: int) -> np.ndarray:
    """Schattenholz ächzt – tiefer und langsamer als jeder Baum der Oberwelt, mit fremder Schwebung."""
    r = rng(seed)
    base = r.uniform(90, 130)
    modes = [(base, 22, 1.0), (base * 2.71, 20, 0.7), (base * 4.4, 18, 0.35), (base * 7.3, 16, 0.12)]
    y = stick_slip(sr, seed, r.uniform(1.4, 2.4), [(0, 10), (0.4, r.uniform(28, 40)), (0.8, 18), (1, 8)], modes)
    return filt(y, "low", 2200, sr, 2)


# =====================================================================================
# Klangteppiche
# =====================================================================================

def gust_curve(seconds: float, seed: int, calm: float = 0.12, strong: float = 0.92, cycles=(1, 14), falloff=0.85) -> np.ndarray:
    m = int(round(seconds * 100))
    return smoothstep(periodic_curve(m, seed, cycles, falloff), calm, strong)


def wind(bed: Bed, seed: int, *, lo: float, hi: float, floor: float = 0.25, body: float = 0.6, whistle: float = 0.0,
         whistles=((640, 1050), (920, 1500)), slope: float = -4.0, hp: float = 45.0, gusts=None, gain: float = 1.0,
         width: float = 0.25) -> np.ndarray:
    """Wind: rauschender Luftstrom, dessen Klangfarbe mit den Böen steigt; bei Sturm pfeift er in Kanten."""
    S = bed.seconds
    g = gusts if gusts is not None else gust_curve(S, seed)
    out = []
    for ch, shift in ((0, -width), (1, width)):
        def gain_fn(t, f, shift=shift):
            gt = ctl(g, t + shift, S)
            fc = lo * (hi / lo) ** gt
            G = tilt(f, slope) * lowpass_gain(f, fc, 2) * highpass_gain(f, hp, 2)
            G = G + body * band(f, fc * 0.75, 0.42) * tilt(f, slope * 0.4)
            if whistle > 0:
                for k, (w0, w1) in enumerate(whistles):
                    fw = w0 * (w1 / w0) ** ctl(g, t + shift + 0.6 * k, S)
                    G = G + whistle * ctl(g, t + shift, S) ** 2.4 * band(f, fw, 0.01) * (0.6 + 0.4 * k)
            return G * (floor + (1 - floor) * gt ** 1.5)
        out.append(shaped_noise(bed.n, bed.sr, gain_fn, seed * 7 + ch, nfft=4096, hop=1024))
    bed.add_stereo(out[0], out[1], gain)
    return g


def rustle(bed: Bed, seed: int, gusts: np.ndarray, *, lo: float = 1600, hi: float = 9500, flutter=(4.0, 28.0),
           depth: float = 0.75, gain: float = 1.0, ticks: float = 1.0) -> None:
    """Rascheln von Laub oder Schilf: helles Rauschen, das mit den Böen anschwillt und flatternd bebt."""
    n, sr, S = bed.n, bed.sr, bed.seconds
    for ch in range(2):
        base = shaped_noise(n, sr, lambda t, f: highpass_gain(f, lo, 2) * lowpass_gain(f, hi, 2) * tilt(f, -3.5, lo * 1.5), seed + ch, 1024, 512)
        fl = colored_noise(n, sr, seed + 10 + ch, 0.0, lo=flutter[0], hi=flutter[1])
        fl = np.clip(0.5 + 0.32 * fl, 0.0, 1.0) ** 1.4
        g = audio_curve(gusts, n)
        if ch == 1:
            g = np.roll(g, int(0.35 * sr))
        env = (0.12 + 0.88 * g ** 1.3) * ((1 - depth) + depth * fl)
        y = base / (np.std(base) + 1e-9) * env
        bed.add_stereo(y if ch == 0 else np.zeros(n), y if ch == 1 else np.zeros(n), gain)
    # einzelne Blätter, die klickend aneinanderschlagen
    r = rng(seed + 99)
    count = int(S * 6 * ticks)
    for i in range(count):
        t = r.uniform(0, S)
        if r.random() > ctl(gusts, t, S) ** 1.2:
            continue
        m = int(0.004 * sr)
        x = filt(r.standard_normal(m), "high", 2500, sr, 2) * np.hanning(m)
        bed.add(x, t, r.uniform(-0.9, 0.9), gain * r.uniform(0.15, 0.5))


def cricket_chirp(sr: int, f0: float, pulses: int, pulse_rate: float, pulse_len: float, r) -> np.ndarray:
    n = int((pulses / pulse_rate + 0.03) * sr)
    y = np.zeros(n)
    L = int(pulse_len * sr)
    tt = np.arange(L) / sr
    pulse_env = np.sin(np.pi * np.arange(L) / L) ** 2
    for p in range(pulses):
        s = int(p / pulse_rate * sr)
        f = f0 * (1 - 0.012 * tt / pulse_len)
        ph = 2 * np.pi * np.cumsum(f) / sr
        y[s:s + L] += (np.sin(ph) + 0.06 * np.sin(2 * ph)) * pulse_env * r.uniform(0.85, 1.0)
    return y


def crickets(bed: Bed, seed: int, *, count: int = 10, f_range=(3900, 5200), rate=(2.0, 3.6), pulses=(3, 5),
             pulse_rate=(26, 36), tree: int = 2, chorus: float = 0.35, chorus_low: float = 0.4, gain: float = 1.0) -> None:
    """Nächtliches Zirpen: einzelne Feldgrillen ringsum, dazu leise trillernde Baumgrillen und ein ferner Chor."""
    r = rng(seed)
    S, sr, n = bed.seconds, bed.sr, bed.n
    for i in range(count):
        f0 = r.uniform(*f_range)
        period = 1 / r.uniform(*rate)
        k = int(r.integers(pulses[0], pulses[1] + 1))
        pr = r.uniform(*pulse_rate)
        dist = r.uniform(2.5, 22)
        pan = r.uniform(-0.95, 0.95)
        amp = 1.0 / (1 + dist / 5)
        chirp = distance(cricket_chirp(sr, f0, k, pr, 0.6 / pr, r), sr, dist)
        # Grillen setzen auch mal aus
        pause_at = r.uniform(0, S)
        pause_len = r.uniform(0, S * 0.35)
        t = r.uniform(0, period)
        while t < S:
            if not (pause_at <= t < pause_at + pause_len or pause_at <= t + S < pause_at + pause_len):
                bed.add(chirp, t, pan, gain * amp * r.uniform(0.85, 1.0))
            t += period * r.uniform(0.96, 1.04)
    tt = np.arange(n) / sr
    for j in range(tree):
        f = round(r.uniform(2350, 2900) * S) / S
        pr = round(r.uniform(42, 55) * S) / S
        am = (0.5 + 0.5 * np.sin(2 * np.pi * pr * tt)) ** 6
        swell = audio_curve(smoothstep(periodic_curve(int(S * 10), seed + j, (1, 4)), 0.1, 0.9), n)
        y = np.sin(2 * np.pi * f * tt) * am * (0.2 + 0.8 * swell)
        d = r.uniform(8, 25)
        y = filt_circular(y, "low", max(1200.0, 16000.0 / (1 + d / 25.0)), sr, 1) / (1 + d / 12.0)
        bed.add(y, 0.0, r.uniform(-0.7, 0.7), gain * 0.22)
    if chorus > 0:
        for ch in range(2):
            y = shaped_noise(n, sr, lambda t, f: band(f, 4500, 0.12) + chorus_low * band(f, 2700, 0.1), seed + 40 + ch, 1024, 512)
            am = 0.55 + 0.45 * np.sin(2 * np.pi * (round(31 * S) / S) * tt + ch)
            y = y / (np.std(y) + 1e-9) * am * 0.06
            bed.add_stereo(y if ch == 0 else np.zeros(n), y if ch == 1 else np.zeros(n), gain * chorus)


def grasshoppers(bed: Bed, seed: int, *, count: int = 5, gain: float = 1.0) -> None:
    """Grashüpfer am Tag: schnarrende Versfolgen aus der Wiese."""
    r = rng(seed)
    S, sr = bed.seconds, bed.sr
    for i in range(count):
        dist = r.uniform(2, 14)
        pan = r.uniform(-0.9, 0.9)
        rate = r.uniform(10, 16)
        k = int(r.uniform(1.2, 2.4) * rate)
        syl = int(r.uniform(0.025, 0.045) * sr)
        y = np.zeros(int((k / rate + 0.1) * sr))
        for s in range(k):
            st = int(s / rate * sr)
            x = filt(r.standard_normal(syl), "band", [5000, min(14000, sr * 0.45)], sr, 2)
            x *= (0.5 + 0.5 * np.sin(2 * np.pi * r.uniform(220, 320) * np.arange(syl) / sr)) * np.hanning(syl)
            y[st:st + syl] += x * (0.3 + 0.7 * s / k)
        y = distance(y, sr, dist)
        t = r.uniform(0, S)
        while t < S + 0.5:
            bed.add(y, t % S, pan, gain / (1 + dist / 6) * r.uniform(0.7, 1.0))
            t += r.uniform(4, 9)


def hum(bed: Bed, seed: int, *, flies: int = 3, gain: float = 1.0) -> None:
    """Hummeln und Fliegen, die vorbeisummen."""
    r = rng(seed)
    S, sr = bed.seconds, bed.sr
    for i in range(flies):
        dur = r.uniform(2.0, 4.0)
        n = int(dur * sr)
        t = np.arange(n) / sr
        f0 = r.uniform(170, 260)
        doppler = 1 + 0.035 * np.tanh((t - dur / 2) * -2.5)
        f = f0 * doppler * (1 + 0.02 * np.sin(2 * np.pi * r.uniform(1.5, 3.5) * t))
        y = saw(f, np.sin(np.pi * t / dur) ** 2, sr, partials=16, rolloff=1.3)
        y = filt(y, "low", 2500, sr, 2)
        pan0 = r.uniform(-1, 1)
        x = np.stack([y * np.cos((np.clip(pan0 + (t / dur - 0.5) * 1.4, -1, 1) + 1) * np.pi / 4),
                      y * np.sin((np.clip(pan0 + (t / dur - 0.5) * 1.4, -1, 1) + 1) * np.pi / 4)], axis=1)
        bed.add(x, r.uniform(0, S), 0, gain * r.uniform(0.25, 0.5))


def song_chorus(bed: Bed, seed: int, songs, *, count: int, dist=(18, 60), ir=None, wet: float = 0.35, gain: float = 1.0) -> None:
    """Ferner Vogelchor: einzelne Strophen verschiedener Vögel, verteilt über die Schleife."""
    r = rng(seed)
    S, sr = bed.seconds, bed.sr
    for i in range(count):
        fn = songs[int(r.integers(len(songs)))]
        x = fn(sr, seed * 100 + i)
        d = r.uniform(*dist)
        x = distance(x, sr, d)
        if ir is not None:
            x = apply_room(x, ir, wet)
        bed.add(x, (i + r.uniform(0.0, 0.8)) * S / count, r.uniform(-0.85, 0.85), gain * r.uniform(0.6, 1.0))


def waves(bed: Bed, seed: int, *, count: int, heavy: float = 0.0, gain: float = 1.0) -> None:
    """Brandung: Welle um Welle – heranrollen, brechen, auslaufen (bei Sturm mit rasselndem Geröll)."""
    r = rng(seed)
    S, sr, n = bed.seconds, bed.sr, bed.n
    spacing = S / count
    for i in range(count):
        t0 = i * spacing + r.uniform(-0.15, 0.15) * spacing
        m = int(r.uniform(7.0, 9.0) * sr)
        tt = np.arange(m) / sr
        nz = r.standard_normal(m)
        swell = filt(nz, "low", 380 + 200 * heavy, sr, 2) * curve(m, [(0, 0), (0.28, 1.0), (0.38, 0.5), (1, 0)])
        crash_at = 0.3 * m / sr + r.uniform(-0.2, 0.2)
        ce = np.where(tt < crash_at, 0, (1 - np.exp(-(tt - crash_at) / 0.3)) ** 2 * np.exp(-(tt - crash_at) / (1.2 + 0.6 * heavy)))
        crash = filt(filt(r.standard_normal(m), "band", [120, 5500], sr, 2), "low", 4000, sr, 1) * ce
        we = np.where(tt < crash_at + 0.3, 0, (1 - np.exp(-(tt - crash_at - 0.3) / 0.5)) * np.exp(-(tt - crash_at - 0.3) / 2.4))
        fizz = 0.55 + 0.45 * np.clip(filt(r.standard_normal(m), "band", [40, 140], sr, 2) * 6, -1, 1)
        wash = filt(filt(r.standard_normal(m), "band", [1200, 7000], sr, 2), "low", 3500, sr, 1) * we * fizz
        y = 1.2 * swell + (1.0 + 0.6 * heavy) * crash + 0.4 * wash
        if heavy > 0:
            be = np.where(tt < crash_at + 1.6, 0, np.exp(-(tt - crash_at - 1.6) / 1.4)) * (1 - np.exp(-np.maximum(0, tt - crash_at - 1.6) / 0.3))
            rattle = np.zeros(m)
            clicks = r.integers(0, m, int(3000 * heavy))
            rattle[clicks] = r.uniform(-1, 1, len(clicks))
            rattle = filt(rattle, "high", 2200, sr, 2) * 4
            y += heavy * (rattle + filt(r.standard_normal(m), "low", 700, sr, 2) * 0.8) * be
        y *= curve(m, [(0, 1), (0.7, 1), (1, 0)]) ** 1.5
        pan = r.uniform(-0.6, 0.6)
        x = np.stack([y * pan_gains(pan - 0.25)[0], y * pan_gains(pan + 0.25)[1]], axis=1)
        bed.add(x, t0, 0, gain * r.uniform(0.75, 1.0))
    # fernes Meer zwischen den Wellen
    for ch in range(2):
        sea = shaped_noise(n, sr, lambda t, f: lowpass_gain(f, 500, 2) * highpass_gain(f, 40, 2) * tilt(f, -3), seed + 70 + ch, 2048, 512)
        bed.add_stereo(sea * (ch == 0) * 0.35, sea * (ch == 1) * 0.35, gain)


def lapping(bed: Bed, seed: int, *, rate: float = 1.0, gain: float = 1.0) -> None:
    """Sanftes Plätschern am Ufer einer Lagune."""
    r = rng(seed)
    S, sr = bed.seconds, bed.sr
    t = 0.0
    while t < S:
        m = int(r.uniform(0.08, 0.2) * sr)
        x = filt(r.standard_normal(m), "band", [r.uniform(250, 500), r.uniform(900, 1800)], sr, 2) * env_asr(m, sr, 0.01, 0.1)
        bed.add(x, t, r.uniform(-0.8, 0.8), gain * r.uniform(0.3, 0.9))
        if r.random() < 0.4:
            b = bubble(sr, r.uniform(500, 1400), r.uniform(0.02, 0.05), int(t * 100))
            bed.add(b, t + r.uniform(0.02, 0.1), r.uniform(-0.8, 0.8), gain * 0.25)
        t += r.uniform(0.35, 1.4) / rate


def bog_bed(bed: Bed, seed: int, *, rate: float = 1.0, gain: float = 1.0, deep: float = 1.0) -> None:
    """Blubbern im Moor: einzelne Blasen, ab und zu ein Schwall, dazu schmatzender Schlamm."""
    r = rng(seed)
    S, sr = bed.seconds, bed.sr
    t = 0.0
    while t < S:
        if r.random() < 0.25:
            x = bog_burp(sr, int(r.integers(1 << 30)))
        else:
            x = bubble(sr, r.uniform(140, 800), r.uniform(0.03, 0.1), int(r.integers(1 << 30)))
        x = distance(x, sr, r.uniform(2, 15))
        bed.add(x, t, r.uniform(-0.9, 0.9), gain * r.uniform(0.3, 1.0))
        t += r.exponential(0.7 / rate)
    for i in range(int(S / 5 * deep)):
        x = bubble(sr, r.uniform(60, 120), r.uniform(0.15, 0.3), i, rise=0.4) * 1.2
        bed.add(x, r.uniform(0, S), r.uniform(-0.6, 0.6), gain * 0.7)


def crackle(bed: Bed, seed: int, *, rate: float = 18.0, pops: float = 0.3, hiss: float = 0.25, gain: float = 1.0) -> None:
    """Glut knistert: Funken und kleine Knalle, darunter leises Zischen."""
    r = rng(seed)
    S, sr, n = bed.seconds, bed.sr, bed.n
    t = 0.0
    while t < S:
        cluster = int(r.choice([1, 1, 1, 2, 3, 4]))
        for c in range(cluster):
            m = int(r.uniform(0.001, 0.006) * sr)
            fc = r.uniform(1200, 7000)
            x = filt(r.standard_normal(max(m, 8)), "band", [fc * 0.6, min(fc * 1.6, sr * 0.45)], sr, 2) * np.hanning(max(m, 8))
            bed.add(x, t + c * r.uniform(0.003, 0.03), r.uniform(-0.9, 0.9), gain * min(1.0, r.pareto(2.5) * 0.3 + 0.1))
        t += r.exponential(1 / rate)
    for i in range(int(S * pops)):
        m = int(0.03 * sr)
        tt = np.arange(m) / sr
        x = np.sin(2 * np.pi * r.uniform(300, 700) * tt) * np.exp(-tt / 0.01) + 0.6 * r.standard_normal(m) * np.exp(-tt / 0.005)
        bed.add(x, r.uniform(0, S), r.uniform(-0.8, 0.8), gain * r.uniform(0.3, 0.8))
    if hiss > 0:
        for ch in range(2):
            h = shaped_noise(n, sr, lambda t, f: highpass_gain(f, 3000, 2) * lowpass_gain(f, 11000, 2), seed + 50 + ch, 1024, 512)
            h = h / (np.std(h) + 1e-9) * 0.05 * hiss
            bed.add_stereo(h * (ch == 0), h * (ch == 1), gain)


def lava_bed(bed: Bed, seed: int, *, gain: float = 1.0) -> None:
    """Glühender Fels: tiefes Brodeln, zäh fließende Lava und dumpf platzende Blasen."""
    r = rng(seed)
    S, sr, n = bed.seconds, bed.sr, bed.n
    swell = gust_curve(S, seed, 0.1, 0.95, (1, 6))
    for ch in range(2):
        def g(t, f, ch=ch):
            s = ctl(swell, t + ch * 0.7, S)
            return (lowpass_gain(f, 110, 3) * (0.5 + 0.5 * s) + 0.25 * lowpass_gain(f, 450, 2) * highpass_gain(f, 80, 2) * (0.3 + 0.7 * s))
        y = shaped_noise(n, sr, g, seed + ch, 4096, 1024)
        bed.add_stereo(y * (ch == 0), y * (ch == 1), gain)
    t = 0.0
    while t < S:
        m = int(0.45 * sr)
        tt = np.arange(m) / sr
        f0 = r.uniform(55, 110)
        b = np.sin(2 * np.pi * np.cumsum(glide(m, f0, f0 * 1.7, 0.6)) / sr) * np.exp(-tt / 0.12) * env_ad(m, sr, 0.01)
        b += filt(r.standard_normal(m), "low", 1800, sr, 2) * np.exp(-tt / 0.05) * 0.4
        bed.add(b, t, r.uniform(-0.7, 0.7), gain * r.uniform(0.3, 0.9) * 2.5)
        t += r.uniform(0.8, 2.6)


def drone(bed: Bed, seed: int, notes, *, partials: int = 8, rolloff: float = 1.6, detune: float = 6.0, lp=(500, 1600),
          lfo=(1, 4), breath: float = 0.3, breath_lp: float = 500, shimmer: float = 0.0, shimmer_ratio=(6, 8, 10, 12),
          gain: float = 1.0) -> None:
    """
    Dröhnen der Schattenwelt: übereinandergelegte, leicht verstimmte Töne, deren Klangfarbe sich langsam öffnet und
    schließt, dazu ein Atem aus Rauschen und ein gläsernes Schimmern hoch darüber. Alle Teiltöne schwingen in ganzen
    Perioden über die Schleife, so schließt sie ohne Naht.
    """
    r = rng(seed)
    S, sr, n = bed.seconds, bed.sr, bed.n
    t = np.arange(n) / sr
    opening = audio_curve(smoothstep(periodic_curve(int(S * 20), seed, lfo, 1.0), 0.05, 0.95), n)
    fc = lp[0] * (lp[1] / lp[0]) ** opening
    for ni, (f0, level) in enumerate(notes):
        for vi, cents in enumerate((-detune, 0.0, detune)):
            fv = f0 * 2 ** (cents / 1200)
            amp_lfo = audio_curve(periodic_curve(int(S * 20), seed + ni * 10 + vi, lfo, 1.0), n)
            pan = (vi - 1) * 0.6 + r.uniform(-0.15, 0.15)
            y = np.zeros(n)
            for h in range(1, partials + 1):
                fh = round(fv * h * S) / S
                if fh > sr * 0.45:
                    break
                a = 1.0 / h ** rolloff * lowpass_gain(np.full(n, fh), fc, 2)
                y += a * np.sin(2 * np.pi * fh * t + r.uniform(0, 2 * np.pi))
            bed.add(y * level * (0.45 + 0.55 * amp_lfo), 0.0, pan, gain * 0.35)
    if breath > 0:
        sw = gust_curve(S, seed + 5, 0.0, 1.0, (1, 5))
        for ch in range(2):
            b = shaped_noise(n, sr, lambda tt, f: lowpass_gain(f, breath_lp, 2) * highpass_gain(f, 50, 2) * (0.2 + 0.8 * ctl(sw, tt + ch, S)),
                             seed + 60 + ch, 2048, 512)
            b = b / (np.std(b) + 1e-9) * 0.12 * breath
            bed.add_stereo(b * (ch == 0), b * (ch == 1), gain)
    if shimmer > 0:
        base = notes[0][0]
        for k, ratio in enumerate(shimmer_ratio):
            f = round(base * ratio * S) / S
            if f > sr * 0.45:
                continue
            a = audio_curve(smoothstep(periodic_curve(int(S * 20), seed + 80 + k, (1, 5)), 0.3, 1.0), n)
            bed.add(np.sin(2 * np.pi * f * t + k) * a, 0.0, r.uniform(-0.8, 0.8), gain * shimmer * 0.08)


def heartbeat(bed: Bed, period: float, *, f: float = 50.0, gain: float = 1.0) -> None:
    """Ein langsamer Herzschlag tief unter dem Boden („lub-dub“)."""
    sr, S = bed.sr, bed.seconds
    k = max(1, round(S / period))
    period = S / k
    m = int(0.35 * sr)
    tt = np.arange(m) / sr
    lub = np.sin(2 * np.pi * np.cumsum(glide(m, f * 1.2, f * 0.8)) / sr) * np.exp(-tt / 0.07) * env_ad(m, sr, 0.008)
    dub = np.sin(2 * np.pi * np.cumsum(glide(m, f * 1.35, f * 0.9)) / sr) * np.exp(-tt / 0.05) * env_ad(m, sr, 0.006)
    for i in range(k):
        bed.add(lub, i * period, 0, gain)
        bed.add(dub * 0.7, i * period + 0.28, 0, gain)


def scatter(bed: Bed, seed: int, fn, *, count: int, dist=(5, 30), ir=None, wet: float = 0.4, gain: float = 1.0, sr: int | None = None) -> None:
    """Einzelklänge über die Schleife verteilen (Tropfen, Glöckchen, Knarren …)."""
    r = rng(seed)
    S = bed.seconds
    for i in range(count):
        x = fn(bed.sr, seed * 1000 + i)
        x = distance(x, bed.sr, r.uniform(*dist))
        if ir is not None:
            x = apply_room(x, ir, wet)
        bed.add(x, (i + r.uniform(0.0, 0.85)) * S / count, r.uniform(-0.9, 0.9), gain * r.uniform(0.5, 1.0))


# =====================================================================================
# Die Teppiche der Biome
# =====================================================================================

def bed_wind_breeze():
    b = Bed(16, BED_SR, 1024)
    wind(b, 11, lo=260, hi=820, floor=0.35, body=0.45, slope=-4.5)
    return b


def bed_wind_gale():
    b = Bed(20, BED_SR, 1024)
    g = gust_curve(20, 12, 0.05, 0.85, (1, 16), 0.7)
    wind(b, 12, lo=380, hi=1900, floor=0.22, body=0.8, whistle=0.9, slope=-3.5, gusts=g)
    return b


def bed_wind_coast():
    b = Bed(16, BED_SR, 1024)
    wind(b, 13, lo=320, hi=1100, floor=0.45, body=0.55, whistle=0.15, slope=-4.0, gusts=gust_curve(16, 13, 0.2, 0.95, (1, 10)))
    return b


def bed_wind_dry():
    b = Bed(16, BED_SR, 1024)
    g = wind(b, 14, lo=420, hi=1600, floor=0.3, body=0.4, slope=-3.0)
    # feiner Staub und Asche, die über den Boden treiben
    r = rng(14)
    for ch in range(2):
        grain = np.zeros(b.n)
        idx = r.integers(0, b.n, 9000)
        grain[idx] = r.uniform(-1, 1, len(idx))
        grain = filt_circular(grain, "high", 3500, b.sr, 2)
        env = 0.08 + 0.92 * audio_curve(g, b.n) ** 3
        grain = grain / (np.std(grain) + 1e-9) * env * 0.025
        b.add_stereo(grain * (ch == 0), grain * (ch == 1))
    return b


def bed_wind_moor():
    b = Bed(20, BED_SR, 1024)
    wind(b, 15, lo=220, hi=700, floor=0.3, body=1.1, whistle=0.25, whistles=((380, 520), (560, 760)), slope=-5.0,
         gusts=gust_curve(20, 15, 0.1, 0.9, (1, 8), 1.1))
    return b


def bed_leaves():
    b = Bed(16, BED_SR, 1024)
    g = gust_curve(16, 21, 0.15, 0.95, (1, 12))
    rustle(b, 21, g, hi=8000, ticks=0.6)
    return b


def bed_reeds():
    b = Bed(16, BED_SR, 1024)
    g = gust_curve(16, 22, 0.1, 0.9, (1, 9), 1.1)
    rustle(b, 22, g, lo=700, hi=6000, flutter=(1.2, 9.0), depth=0.6, ticks=0.3)
    return b


def bed_birds_forest():
    b = Bed(24, BED_SR, 1024)
    ir = room(BED_SR, 1.4, 31, predelay=0.03, damping=4000)
    song_chorus(b, 31, [bird_blackbird, bird_tit, bird_finch, bird_blackbird, bird_finch], count=16, dist=(18, 55), ir=ir, wet=0.4)
    song_chorus(b, 32, [bird_cuckoo, woodpecker], count=2, dist=(45, 80), ir=ir, wet=0.6, gain=0.7)
    return b


def bed_birds_meadow():
    b = Bed(24, BED_SR, 1024)
    for k, (d, pan, g) in enumerate(((45, -0.3, 1.0), (70, 0.5, 0.7))):
        lark = fade_edges(distance(skylark_line(BED_SR, b.seconds, 40 + k), BED_SR, d), BED_SR, 0.05, 0.05)
        b.add(lark, k * 3.0, pan, g)
    ir = room(BED_SR, 0.9, 33, damping=5000)
    song_chorus(b, 33, [bird_finch, bird_tit], count=6, dist=(25, 60), ir=ir, wet=0.25, gain=0.8)
    return b


def bed_crickets():
    b = Bed(16, BED_SR, 1024)
    crickets(b, 41)
    return b


def bed_crickets_dry():
    b = Bed(16, BED_SR, 1024)
    crickets(b, 42, count=6, f_range=(4800, 6400), rate=(1.4, 2.4), pulses=(2, 3), pulse_rate=(40, 55), tree=0, chorus=0.12, chorus_low=0.0)
    return b


def bed_insects_day():
    b = Bed(16, BED_SR, 1024)
    grasshoppers(b, 43)
    hum(b, 44)
    return b


def bed_frogs():
    b = Bed(20, BED_SR, 1024)
    r = rng(51)
    ir = room(BED_SR, 1.6, 51, damping=3500)
    for i in range(7):
        x = apply_room(distance(frog_croak(BED_SR, 510 + i), BED_SR, r.uniform(4, 30)), ir, 0.3)
        t = r.uniform(0, 20)
        for k in range(int(r.integers(2, 6))):
            b.add(x, (t + k * r.uniform(0.6, 1.4)) % 20, r.uniform(-0.9, 0.9), r.uniform(0.5, 1.0))
    for i in range(4):
        f = r.uniform(430, 560)
        period = r.uniform(1.1, 2.0)
        x = apply_room(distance(toad_call(BED_SR, 520 + i, f), BED_SR, r.uniform(6, 25)), ir, 0.4)
        t = r.uniform(0, period)
        pan = r.uniform(-0.9, 0.9)
        while t < 20:
            b.add(x, t, pan, 0.8)
            t += period * r.uniform(0.97, 1.03)
    return b


def bed_surf_gentle():
    b = Bed(24, BED_SR, 1024)
    waves(b, 61, count=3, heavy=0.0)
    return b


def bed_surf_storm():
    b = Bed(24, BED_SR, 1024)
    waves(b, 62, count=4, heavy=1.0, gain=1.2)
    return b


def bed_lagoon():
    b = Bed(16, BED_SR, 1024)
    lapping(b, 63)
    waves(b, 64, count=2, heavy=0.0, gain=0.25)
    return b


def bed_bog():
    b = Bed(16, BED_SR, 1024)
    bog_bed(b, 71, rate=1.4)
    return b


def bed_embers():
    b = Bed(16, BED_SR, 1024)
    crackle(b, 81)
    return b


def bed_lava():
    b = Bed(16, LOW_SR, 1024)
    lava_bed(b, 82)
    return b


def bed_ice():
    b = Bed(20, BED_SR, 1024)
    for ch in range(2):
        y = shaped_noise(b.n, b.sr, lambda t, f: lowpass_gain(f, 140, 3) * highpass_gain(f, 30, 2), 91 + ch, 4096, 1024)
        b.add_stereo(y * (ch == 0) * 0.6, y * (ch == 1) * 0.6)
    ir = room(BED_SR, 2.2, 91, damping=6000)
    scatter(b, 92, ice_creak, count=4, dist=(15, 50), ir=ir, wet=0.4, gain=0.6)
    scatter(b, 93, lambda sr, s: bell(rng(s).uniform(3000, 6000), 0.8, sr, decay=0.25, seed=s), count=14, dist=(3, 20), ir=ir, wet=0.5, gain=0.25)
    return b


def bed_storm_charge():
    b = Bed(16, BED_SR, 1024)
    S, sr, n = b.seconds, b.sr, b.n
    t = np.arange(n) / sr
    f = round(118 * S) / S
    trem = audio_curve(smoothstep(periodic_curve(int(S * 20), 101, (1, 6)), 0.55, 1.0), n)
    buzz = sum(np.sin(2 * np.pi * f * h * t) / h ** 1.1 for h in range(1, 18) if f * h < sr * 0.45)
    buzz = filt_circular(buzz, "band", [180, 3000], sr, 2) * trem * 0.06
    b.add(buzz, 0, 0.0, 1.0)
    scatter(b, 102, zap, count=7, dist=(4, 25), gain=0.9)
    scatter(b, 103, lambda sr, s: thunder_far(sr, s), count=1, dist=(30, 40), gain=0.8)
    return b


def bed_drips():
    b = Bed(20, BED_SR, 1024)
    ir = room(BED_SR, 2.4, 111, predelay=0.03, damping=4000, echo=0.33, echo_gain=0.4)
    r = rng(111)
    for spot in range(4):
        period = r.uniform(1.1, 2.9)
        variants = [apply_room(drip(BED_SR, 1110 + spot * 10 + k), ir, 0.45) for k in range(3)]
        pan = r.uniform(-0.9, 0.9)
        g = r.uniform(0.3, 1.0)
        t = r.uniform(0, period)
        while t < b.seconds:
            b.add(variants[int(r.integers(3))], t, pan, g * r.uniform(0.85, 1.0))
            t += period * r.uniform(0.92, 1.08)
    scatter(b, 112, drip, count=6, dist=(2, 15), ir=ir, wet=0.45, gain=0.6)
    for ch in range(2):
        y = shaped_noise(b.n, b.sr, lambda t, f: lowpass_gain(f, 220, 2) * highpass_gain(f, 30, 2), 113 + ch, 2048, 512)
        b.add_stereo(y * (ch == 0) * 0.15, y * (ch == 1) * 0.15)
    return b


def bed_deep_water():
    b = Bed(20, LOW_SR, 1024)
    S = b.seconds
    sw = gust_curve(S, 121, 0.0, 1.0, (1, 3), 1.0)
    for ch in range(2):
        y = shaped_noise(b.n, b.sr, lambda t, f: lowpass_gain(f, 240, 3) * highpass_gain(f, 28, 2) * (0.2 + 0.8 * ctl(sw, t + ch * 1.5, S)),
                         121 + ch, 4096, 1024)
        b.add_stereo(y * (ch == 0), y * (ch == 1))
    ir = room(LOW_SR, 4.0, 122, damping=1500)
    scatter(b, 123, whale_moan, count=1, dist=(40, 50), ir=ir, wet=0.7, gain=0.6)
    scatter(b, 124, lambda sr, s: bubble(sr, rng(s).uniform(600, 1600), 0.03, s), count=10, dist=(3, 12), gain=0.25)
    return b


def bed_magic_grove():
    b = Bed(20, BED_SR, 1024)
    drone(b, 131, [(293.66, 0.6), (369.99, 0.45), (440.0, 0.5), (659.26, 0.3)], partials=3, rolloff=2.2, detune=4,
          lp=(900, 2600), lfo=(1, 3), breath=0.0, gain=0.5)
    ir = room(BED_SR, 3.0, 132, damping=7000)
    scatter(b, 133, lambda sr, s: sparkle(sr, s, octave=2.0), count=7, dist=(3, 18), ir=ir, wet=0.6, gain=0.5)
    g = gust_curve(20, 134, 0.2, 0.9, (1, 8))
    rustle(b, 134, g, gain=0.35)
    return b


def bed_earth_rumble():
    b = Bed(16, LOW_SR, 1024)
    S, sr, n = b.seconds, b.sr, b.n
    sw = gust_curve(S, 141, 0.0, 1.0, (1, 4))
    for ch in range(2):
        y = shaped_noise(n, sr, lambda t, f: lowpass_gain(f, 75, 4) * (0.3 + 0.7 * ctl(sw, t + ch, S)), 141 + ch, 4096, 1024)
        b.add_stereo(y * (ch == 0), y * (ch == 1))
    t = np.arange(n) / sr
    f = round(34 * S) / S
    b.add(np.sin(2 * np.pi * f * t) * (0.3 + 0.7 * audio_curve(sw, n)) * 0.25, 0, 0)
    scatter(b, 142, pebbles, count=3, dist=(8, 30), gain=0.5)
    return b


# --------------------------------------------------------------------- Schattenwelt

def _shadow(seconds: float, sr: int = LOW_SR) -> Bed:
    return Bed(seconds, sr, 1024)


def bed_shadow_forest():
    b = _shadow(24, BED_SR)
    drone(b, 201, [(73.42, 0.8), (110.0, 0.55), (174.61, 0.35)], partials=7, rolloff=1.7, detune=7, lp=(300, 1100), breath=0.4)
    ir = room(BED_SR, 3.2, 202, damping=3500)
    scatter(b, 203, shadow_creak, count=3, dist=(10, 30), ir=ir, wet=0.5, gain=0.55)
    scatter(b, 204, lambda sr, s: whisper(sr, s, (2, 4)), count=2, dist=(12, 25), ir=ir, wet=0.8, gain=0.25)
    b.reverb(room(BED_SR, 3.5, 205, damping=3000), 0.35)
    return b


def bed_shadow_wastes():
    b = _shadow(24, BED_SR)
    wind(b, 211, lo=200, hi=640, floor=0.2, body=1.2, whistle=0.35, whistles=((300, 420), (450, 610)), slope=-5.0, gain=0.7)
    drone(b, 212, [(82.41, 0.6), (123.47, 0.4)], partials=6, rolloff=1.8, detune=9, lp=(250, 700), breath=0.0, gain=0.8)
    b.reverb(room(BED_SR, 4.0, 213, damping=2500), 0.3)
    return b


def bed_shadow_crystal():
    b = _shadow(24, BED_SR)
    drone(b, 221, [(69.3, 0.5), (277.18, 0.35), (415.3, 0.3), (622.25, 0.22)], partials=4, rolloff=2.0, detune=5,
          lp=(600, 3200), breath=0.1, shimmer=1.0, shimmer_ratio=(16, 20, 24, 30))
    ir = room(BED_SR, 4.5, 222, damping=8000)
    scatter(b, 223, crystal_chime, count=4, dist=(8, 30), ir=ir, wet=0.7, gain=0.45)
    b.reverb(ir, 0.3)
    return b


def bed_shadow_marsh():
    b = _shadow(24, BED_SR)
    drone(b, 231, [(55.0, 0.8), (82.41, 0.5), (130.81, 0.3)], partials=8, rolloff=1.5, detune=11, lp=(180, 600), breath=0.5, breath_lp=350)
    bog_bed(b, 232, rate=0.6, gain=0.7)
    ir = room(BED_SR, 3.5, 233, damping=2500)
    scatter(b, 234, soul_wail, count=2, dist=(30, 60), ir=ir, wet=0.8, gain=0.35)
    return b


def bed_shadow_peaks():
    b = _shadow(20, BED_SR)
    wind(b, 241, lo=300, hi=1500, floor=0.15, body=0.9, whistle=0.7, whistles=((520, 800), (780, 1150)), slope=-3.8, gain=0.9)
    drone(b, 242, [(61.74, 0.6), (62.6, 0.6)], partials=6, rolloff=1.8, detune=3, lp=(200, 600), breath=0.0, gain=0.6)
    return b


def bed_shadow_glimmer():
    b = _shadow(24, BED_SR)
    drone(b, 251, [(174.61, 0.55), (220.0, 0.45), (261.63, 0.45), (329.63, 0.35), (523.25, 0.2)], partials=4, rolloff=2.0,
          detune=6, lp=(700, 2400), lfo=(1, 3), breath=0.15, breath_lp=900)
    ir = room(BED_SR, 3.5, 252, damping=8000)
    scatter(b, 253, lambda sr, s: sparkle(sr, s, scale=(698.5, 784.0, 880.0, 1046.5, 1174.7), octave=2.0), count=9, dist=(3, 15),
            ir=ir, wet=0.6, gain=0.5)
    b.reverb(ir, 0.3)
    return b


def bed_shadow_sea():
    b = _shadow(24, LOW_SR)
    S = b.seconds
    sw = gust_curve(S, 261, 0.0, 1.0, (1, 3))
    for ch in range(2):
        y = shaped_noise(b.n, b.sr, lambda t, f: lowpass_gain(f, 300, 3) * highpass_gain(f, 30, 2) * (0.2 + 0.8 * ctl(sw, t + ch * 2, S)),
                         261 + ch, 4096, 1024)
        b.add_stereo(y * (ch == 0), y * (ch == 1))
    drone(b, 262, [(41.2, 0.7), (61.74, 0.4)], partials=6, rolloff=1.8, detune=8, lp=(150, 500), breath=0.0, gain=0.8)
    ir = room(LOW_SR, 5.0, 263, damping=1200)
    scatter(b, 264, whale_moan, count=2, dist=(40, 60), ir=ir, wet=0.8, gain=0.5)
    return b


def bed_shadow_isles():
    b = _shadow(20, BED_SR)
    wind(b, 271, lo=420, hi=1300, floor=0.3, body=0.5, whistle=0.2, slope=-3.5, gain=0.6)
    drone(b, 272, [(146.83, 0.4), (220.0, 0.35), (329.63, 0.3)], partials=3, rolloff=2.2, detune=5, lp=(900, 3000), breath=0.0,
          shimmer=0.8, shimmer_ratio=(8, 12, 15, 18))
    ir = room(BED_SR, 4.0, 273, damping=8000)
    scatter(b, 274, lambda sr, s: bell(rng(s).choice([1174.7, 1318.5, 1760.0, 1975.5]), 3.0, sr, decay=1.6, seed=s),
            count=5, dist=(10, 35), ir=ir, wet=0.7, gain=0.35)
    return b


def bed_shadow_depths():
    b = _shadow(24, LOW_SR)
    drone(b, 281, [(32.7, 0.9), (49.0, 0.5), (65.4, 0.35)], partials=10, rolloff=1.4, detune=12, lp=(90, 320), breath=0.4, breath_lp=250)
    heartbeat(b, 1.6, f=48.0, gain=0.55)
    ir = room(LOW_SR, 3.5, 282, damping=2500, echo=0.4)
    scatter(b, 283, drip, count=8, dist=(4, 20), ir=ir, wet=0.7, gain=0.4)
    return b


def bed_whispers():
    b = _shadow(20, BED_SR)
    ir = room(BED_SR, 3.0, 291, damping=4000)
    scatter(b, 292, lambda sr, s: whisper(sr, s), count=4, dist=(4, 18), ir=ir, wet=0.7, gain=0.6)
    return b


BEDS = {
    "wind_breeze": bed_wind_breeze, "wind_gale": bed_wind_gale, "wind_coast": bed_wind_coast, "wind_dry": bed_wind_dry,
    "wind_moor": bed_wind_moor, "leaves": bed_leaves, "reeds": bed_reeds, "birds_forest": bed_birds_forest,
    "birds_meadow": bed_birds_meadow, "crickets": bed_crickets, "crickets_dry": bed_crickets_dry, "insects_day": bed_insects_day,
    "frogs": bed_frogs, "surf_gentle": bed_surf_gentle, "surf_storm": bed_surf_storm, "lagoon": bed_lagoon, "bog": bed_bog,
    "embers": bed_embers, "lava": bed_lava, "ice": bed_ice, "storm_charge": bed_storm_charge, "drips": bed_drips,
    "deep_water": bed_deep_water, "magic_grove": bed_magic_grove, "earth_rumble": bed_earth_rumble,
    "shadow_forest": bed_shadow_forest, "shadow_wastes": bed_shadow_wastes, "shadow_crystal": bed_shadow_crystal,
    "shadow_marsh": bed_shadow_marsh, "shadow_peaks": bed_shadow_peaks, "shadow_glimmer": bed_shadow_glimmer,
    "shadow_sea": bed_shadow_sea, "shadow_isles": bed_shadow_isles, "shadow_depths": bed_shadow_depths, "whispers": bed_whispers,
}


# =====================================================================================
# Einzelklänge (je Satz mehrere Varianten) und ihr Raum
# =====================================================================================

FOREST = ("forest", 1.3, 0.25)      # Raumname, Nachhall (s), Anteil
FIELD = ("field", 0.7, 0.12)
CANYON = ("canyon", 2.2, 0.4)
CAVE = ("cave", 2.4, 0.4)
VAST = ("vast", 4.0, 0.55)
ICE = ("ice", 2.0, 0.35)
DRY = None

# Satz: (Funktion, Varianten, Raum, Reichweite in Blöcken, Untertitel DE, Untertitel EN)
SPOTS = {
    "bird_blackbird": (bird_blackbird, 4, FOREST, 48, "Amsel singt", "Blackbird sings"),
    "bird_tit": (bird_tit, 3, FOREST, 40, "Meise ruft", "Tit calls"),
    "bird_finch": (bird_finch, 3, FOREST, 44, "Fink schmettert", "Finch sings"),
    "bird_cuckoo": (bird_cuckoo, 2, FOREST, 80, "Kuckuck ruft", "Cuckoo calls"),
    "woodpecker": (woodpecker, 3, FOREST, 64, "Specht trommelt", "Woodpecker drums"),
    "bird_owl": (lambda sr, s: owl_hoot(sr, s) if s % 3 else owl_kewick(sr, s), 3, FOREST, 64, "Eule ruft", "Owl hoots"),
    "bird_nightingale": (nightingale, 2, FOREST, 56, "Nachtigall singt", "Nightingale sings"),
    "bird_crow": (crow, 3, FIELD, 56, "Krähe krächzt", "Crow caws"),
    "bird_gull": (gull, 3, FIELD, 56, "Möwe kreischt", "Gull cries"),
    "bird_curlew": (curlew, 2, FIELD, 80, "Brachvogel ruft", "Curlew calls"),
    "bird_bittern": (bittern, 2, FIELD, 72, "Rohrdommel dröhnt", "Bittern booms"),
    "bird_buzzard": (buzzard, 2, CANYON, 96, "Bussard schreit", "Buzzard cries"),
    "wolf_howl": (wolf_howl, 3, VAST, 96, "Wolf heult in der Ferne", "Distant wolf howls"),
    "wood_creak": (wood_creak, 3, FOREST, 32, "Baum knarrt", "Tree creaks"),
    "twig_snap": (twig_snap, 3, FOREST, 24, "Ast knackt", "Twig snaps"),
    "ice_crack": (ice_crack, 3, ICE, 64, "Eis knackt", "Ice cracks"),
    "ice_creak": (ice_creak, 3, ICE, 40, "Eis ächzt", "Ice groans"),
    "pebbles": (pebbles, 3, CANYON, 32, "Steine rieseln", "Pebbles fall"),
    "steam_vent": (steam_vent, 2, DRY, 32, "Glutspalte faucht", "Vent hisses"),
    "rumble": (lambda sr, s: rumble(sr, s), 2, VAST, 96, "Grollen aus der Tiefe", "Deep rumble"),
    "thunder_far": (thunder_far, 3, DRY, 128, "Donner in der Ferne", "Distant thunder"),
    "zap": (zap, 3, DRY, 24, "Funken knistern", "Sparks crackle"),
    "bog_burp": (bog_burp, 3, DRY, 20, "Moor blubbert", "Bog bubbles"),
    "drip": (drip, 4, CAVE, 24, "Wasser tropft", "Water drips"),
    "whale_far": (whale_moan, 2, VAST, 128, "Rufen aus der Tiefe", "Call from the deep"),
    "sparkle": (lambda sr, s: sparkle(sr, s), 3, VAST, 20, "Glöckchen funkeln", "Chimes sparkle"),
    "whisper": (whisper, 5, VAST, 20, "Flüstern", "Whispers"),
    "shadow_call": (shadow_call, 3, VAST, 80, "Schattenvogel klagt", "Shadow bird wails"),
    "soul_wail": (soul_wail, 3, VAST, 64, "Seelen klagen", "Souls wail"),
    "crystal_chime": (crystal_chime, 5, VAST, 40, "Kristall singt", "Crystal chimes"),
    "deep_moan": (deep_moan, 2, VAST, 96, "Etwas dröhnt in der Tiefe", "Something groans below"),
    "wing_flutter": (wing_flutter, 2, DRY, 20, "Flügel schlagen", "Wings flutter"),
    "shadow_creak": (shadow_creak, 3, FOREST, 36, "Schattenholz ächzt", "Shadow wood groans"),
}

# Sätze, die nur auf Vanilla-Klänge verweisen: (Pfade, Tonhöhe, Lautstärke, Reichweite, Untertitel DE/EN)
VANILLA_SPOTS = {
    "dragon_far": ([f"mob/enderdragon/growl{i}" for i in range(1, 5)], 0.55, 0.7, 128, "Drache brüllt in der Ferne", "Distant dragon roar"),
    "dragon_wings": ([f"mob/enderdragon/wings{i}" for i in range(1, 7)], 0.6, 0.6, 96, "Schwingen rauschen", "Great wings beat"),
    "fireflies": ([f"block/firefly_bush/firefly_bush{i}" for i in range(1, 12)], 1.0, 0.6, 24, "Glühwürmchen summen", "Fireflies buzz"),
    "amethyst": ([f"block/amethyst/resonate{i}" for i in range(1, 5)], 0.8, 0.8, 48, "Kristall schwingt", "Crystal resonates"),
    "soul_voices": ([f"ambient/nether/soulsand_valley/voices{i}" for i in range(1, 6)] + [f"ambient/nether/soulsand_valley/whisper{i}" for i in range(1, 9)],
                    0.85, 0.5, 48, "Stimmen", "Voices"),
    "basalt": ([f"ambient/nether/basalt_deltas/{n}" for n in ("debris1", "debris2", "debris3", "long_debris1", "long_debris2", "basaltground1",
                                                            "basaltground2", "basaltground3", "plode1", "plode2", "plode3")],
               0.9, 0.5, 48, "Fels bröckelt", "Rock crumbles"),
    "thunder": ([f"ambient/weather/thunder{i}" for i in range(1, 4)], 0.6, 0.35, 160, "Donnergrollen", "Thunder rolls"),
    "dolphin": ([f"mob/dolphin/idle_water{i}" for i in range(1, 11)], 1.0, 0.5, 48, "Delfin pfeift", "Dolphin whistles"),
    "frog": ([f"mob/frog/idle{i}" for i in range(1, 9)], 0.9, 0.6, 32, "Frosch quakt", "Frog croaks"),
    "whale": (["ambient/underwater/additions/bass_whale1", "ambient/underwater/additions/bass_whale2"], 0.8, 0.6, 128,
              "Walgesang", "Whale song"),
    "pale_moss": ([f"block/pale_hanging_moss/pale_hanging_moss{i}" for i in range(1, 16)], 0.8, 0.7, 32, "Etwas raschelt", "Something rustles"),
    "warped_creak": ([f"ambient/nether/warped_forest/creak{i}" for i in range(1, 6)], 0.75, 0.6, 40, "Schattenholz knarrt", "Shadow wood creaks"),
}


def write_spot_set(name: str, spec, preview: Path | None) -> list[str]:
    fn, count, space, _, _, _ = spec
    files = []
    for i in range(count):
        x = fn(ONE_SR, 1000 + i * 37 + zlib.crc32(name.encode()) % 997)
        if space is not None:
            _, rt, wet = space
            x = apply_room(x, room(ONE_SR, rt, 500 + i, stereo=False, damping=4500), wet)
        x = finish_spot(x, ONE_SR)
        path = OUT / "spot" / f"{name}{i + 1}.ogg"
        write_ogg(x.astype(np.float32), ONE_SR, path, quality=4.0)
        files.append(f"kingdomsatwar:ambient/spot/{name}{i + 1}")
        if preview is not None:
            spectrogram(x, ONE_SR, preview / f"spot_{name}{i + 1}.png", f"{name}{i + 1}")
    print(f"  Einzelklang {name}: {count} Varianten")
    return files


def write_bed(name: str, fn, preview: Path | None) -> str:
    bed = fn()
    y = finish_bed(bed)
    path = OUT / "bed" / f"{name}.ogg"
    write_ogg(y.astype(np.float32), bed.sr, path, quality=3.0)
    if preview is not None:
        spectrogram(y.mean(axis=1), bed.sr, preview / f"bed_{name}.png", name)
    # Naht prüfen: Sprung am Schleifenende im Vergleich zu gewöhnlichen Schritten
    jump = float(np.max(np.abs(y[0] - y[-1])))
    step = float(np.percentile(np.abs(np.diff(y, axis=0)), 99.9))
    print(f"  Teppich {name}: {bed.seconds:.1f} s, {bed.sr} Hz, Naht {jump:.4f} (99.9 % Schritt {step:.4f})")
    return f"kingdomsatwar:ambient/bed/{name}"


def spectrogram(x: np.ndarray, sr: int, path: Path, title: str) -> None:
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    fig, ax = plt.subplots(figsize=(10, 3.2), dpi=80)
    spec, _, _ = plt.mlab.specgram(x, NFFT=1024, Fs=sr, noverlap=768)
    top = 10 * np.log10(np.max(spec) + 1e-20)
    ax.specgram(x, NFFT=1024, Fs=sr, noverlap=768, cmap="magma", vmin=top - 75, vmax=top)
    ax.set_ylim(0, min(sr / 2, 12000))
    ax.set_title(title)
    fig.tight_layout()
    fig.savefig(path)
    plt.close(fig)


def subtitle_key(name: str) -> str:
    return f"subtitles.kingdomsatwar.ambient.{name}"


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--only", default="", help="nur diese Teppiche/Sätze (Komma-getrennt)")
    ap.add_argument("--preview", type=Path, help="Ordner für Spektrogramme")
    args = ap.parse_args()
    only = set(filter(None, args.only.split(",")))
    if args.preview:
        args.preview.mkdir(parents=True, exist_ok=True)

    events: dict = {}
    for name, fn in BEDS.items():
        if only and name not in only:
            continue
        events[f"ambient.bed.{name}"] = {"sounds": [{"name": write_bed(name, fn, args.preview)}]}
    for name, spec in SPOTS.items():
        if only and name not in only:
            continue
        files = write_spot_set(name, spec, args.preview)
        events[f"ambient.spot.{name}"] = {"sounds": [{"name": f, "attenuation_distance": spec[3]} for f in files],
                                          "subtitle": subtitle_key(name)}
    for name, (paths, pitch, volume, reach, _, _) in VANILLA_SPOTS.items():
        if only and name not in only:
            continue
        events[f"ambient.spot.{name}"] = {"sounds": [{"name": f"minecraft:{p}", "pitch": pitch, "volume": volume,
                                                      "attenuation_distance": reach} for p in paths],
                                          "subtitle": subtitle_key(name)}

    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8")) if SOUNDS_JSON.exists() else {}
    data.update(events)
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    if not only:
        write_java()
    print(f"{len(events)} Klangereignisse geschrieben")


def subtitles() -> dict:
    subs = {name: (spec[5], spec[4]) for name, spec in SPOTS.items()}
    subs.update({name: (spec[5], spec[4]) for name, spec in VANILLA_SPOTS.items()})
    return subs


def wrap_names(names: list[str], per_line: int = 6) -> str:
    rows = [", ".join(f'"{n}"' for n in names[i:i + per_line]) for i in range(0, len(names), per_line)]
    return ",\n".join("            " + r for r in rows)


def write_java() -> None:
    beds = list(BEDS)
    spots = list(SPOTS) + list(VANILLA_SPOTS)
    lines = [
        "package io.github.sirschrolli.kingdomsatwar.registry;",
        "",
        "import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;",
        "import java.util.LinkedHashMap;",
        "import java.util.List;",
        "import java.util.Map;",
        "import net.minecraft.core.registries.Registries;",
        "import net.minecraft.sounds.SoundEvent;",
        "import net.neoforged.neoforge.registries.DeferredHolder;",
        "import net.neoforged.neoforge.registries.DeferredRegister;",
        "",
        "/**",
        " * Klangereignisse der Umgebungsklänge (erzeugt von tools/audio/ambience.py – nicht von Hand ändern): Teppiche",
        " * ({@code ambient.bed.*}, nahtlose Schleifen) und Einzelklänge ({@code ambient.spot.*}, an zufälligen Orten um den",
        " * Spieler). Welche wo erklingen, steht in {@code client.ambience.AmbienceProfiles}.",
        " */",
        "public final class AmbientSounds {",
        "    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, KingdomsAtWar.MODID);",
        "    public static final List<String> BEDS = List.of(",
        wrap_names(beds) + ");",
        "    public static final List<String> SPOTS = List.of(",
        wrap_names(spots) + ");",
        "    /** Untertitel der Einzelklänge: Englisch, Deutsch. */",
        "    public static final Map<String, List<String>> SUBTITLES = Map.ofEntries(",
        ",\n".join(f'            Map.entry("{k}", List.of("{v[0]}", "{v[1]}"))' for k, v in subtitles().items()),
        "    );",
        "    private static final Map<String, DeferredHolder<SoundEvent, SoundEvent>> EVENTS = new LinkedHashMap<>();",
        "",
        "    static {",
        "        for (String bed : BEDS) {",
        "            register(\"ambient.bed.\" + bed);",
        "        }",
        "        for (String spot : SPOTS) {",
        "            register(\"ambient.spot.\" + spot);",
        "        }",
        "    }",
        "",
        "    private AmbientSounds() {}",
        "",
        "    private static void register(String id) {",
        "        EVENTS.put(id, SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(KingdomsAtWar.id(id))));",
        "    }",
        "",
        "    /** Ein Teppich (Schleife). */",
        "    public static SoundEvent bed(String name) {",
        "        return EVENTS.get(\"ambient.bed.\" + name).get();",
        "    }",
        "",
        "    /** Ein Satz Einzelklänge. */",
        "    public static SoundEvent spot(String name) {",
        "        return EVENTS.get(\"ambient.spot.\" + name).get();",
        "    }",
        "",
        "    /** Gibt es dieses Ereignis? (für Tests) */",
        "    public static boolean has(String id) {",
        "        return EVENTS.containsKey(id);",
        "    }",
        "}",
        "",
    ]
    JAVA.write_text("\n".join(lines), encoding="utf-8")


if __name__ == "__main__":
    main()
