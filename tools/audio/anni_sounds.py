#!/usr/bin/env python3
"""
Erzeugt die Klänge von Anni der Troll, dem Boss, der wie ein Spielfehler glitcht: ihr Kichern und ihr Lachen (eine
raue, tiefe Trollstimme, die hängen bleibt), Schmerz und den endgültigen Absturz (die Stimme läuft langsamer und tiefer,
zerfällt in grobe Stufen, der Bildschirm geht aus), schwere Schritte, das Laden und das Verbindungsende, Ausholen und
Hammerschlag, Hänger (der Ton bleibt im Puffer stecken und brummt), Sprünge, nicht registrierte Treffer, Klone
(Verdoppeln, Absturz, Zerplatzen), fehlende Texturen, Pixelregen und Einschläge, Speichern und Rückruck (ein
rückwärts laufendes Band), die fegende Zeile, den Fehlerklang, den Neustart und die Wiederherstellung als Anni 2.0 – dazu den Fehlerkern (Summen, Starten,
Absturz), den Bannhammer (Ruckler und Bann) und die Fehlerflügel (Blinzeln).

Die Bildfehler werden hörbar: grobe Abtastung und wenige Stufen (Bitcrusher), hängende Pufferstücke, Aussetzer,
vertauschte und rückwärts laufende Schnipsel, Rechteck- und Pulstöne wie aus alten Spielen. Damit nichts nervt, sind
die Höhen gezähmt und die häufigen Klänge kurz und leise. Alles entsteht aus Rauschen und Tönen (siehe
ambience_dsp.py); es werden keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/anni_sounds.py [--preview /pfad] [--only name,name]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/anni/<name><n>.ogg
    Einträge "entity.anni_troll.*", "block.error_core.*", "item.bannhammer.*" und "item.glitch_wings.blink" in sounds.json
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, curve, env_ad, env_asr, fade_edges, filt, fit_level, formant_filter, glide,  # noqa: E402
                          phase_of, resonator, rng, room, saw, soft_clip, tone, trim_tail)
from dsp import write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/anni"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100


def mix_at(y: np.ndarray, x: np.ndarray, start: float, gain: float = 1.0) -> None:
    s = max(0, int(start * SR))
    m = max(0, min(len(x), len(y) - s))
    y[s:s + m] += x[:m] * gain


def noise(n: int, seed: int) -> np.ndarray:
    return rng(seed).standard_normal(n)


def norm(x: np.ndarray) -> np.ndarray:
    return x / (np.max(np.abs(x)) + 1e-9)


def lp(x: np.ndarray, f: float = 9000.0) -> np.ndarray:
    return filt(x, "low", f, SR, 2)


def hp(x: np.ndarray, f: float) -> np.ndarray:
    return filt(x, "high", f, SR, 2)


def note(semi: float, base: float = 220.0) -> float:
    return base * 2.0 ** (semi / 12.0)


def pad(x: np.ndarray, seconds: float) -> np.ndarray:
    return np.concatenate([x, np.zeros(int(seconds * SR))])


# =====================================================================================
# Bausteine: Spieltöne
# =====================================================================================

def square(freq: np.ndarray, amp: np.ndarray, duty: float = 0.5, partials: int = 31) -> np.ndarray:
    """Ein bandbegrenzter Puls- bzw. Rechteckton (wie aus einer alten Spielkonsole)."""
    harmonics = []
    for k in range(1, partials + 1):
        a = abs(np.sin(np.pi * k * duty)) / k
        if a > 1e-4:
            harmonics.append((k, a))
    return tone(freq, amp, SR, harmonics=tuple(harmonics))


def chip(f0: float, dur: float, duty: float = 0.5, decay: float = 0.15, f1: float | None = None, attack: float = 0.002,
         partials: int = 31) -> np.ndarray:
    """Ein Spielton: Puls mit kurzem Anschlag und Abklingen, auf Wunsch gleitend."""
    n = int(dur * SR)
    f = np.full(n, f0) if f1 is None else glide(n, f0, f1, 1.0)
    return square(f, env_ad(n, SR, attack, decay) * env_asr(n, SR, 0.0005, 0.01), duty, partials)


def bits(dur: float, seed: int, lo: float = 600.0, hi: float = 2400.0, step: tuple[float, float] = (0.008, 0.022)) -> np.ndarray:
    """Datenzirpen: ein Ton, der in schnellen Sprüngen die Höhe wechselt (wie ein altes Modem, aber leise und weich)."""
    r = rng(seed)
    n = int(dur * SR)
    f = np.zeros(n)
    a = 0
    while a < n:
        m = int(r.uniform(*step) * SR)
        f[a:a + m] = r.uniform(lo, hi)
        a += m
    return square(f, env_asr(n, SR, 0.005, 0.012), 0.5, partials=7)


def thud(n: int, f0: float, f1: float, decay: float) -> np.ndarray:
    return tone(glide(n, f0, f1, 0.4), env_ad(n, SR, 0.002, decay), SR, harmonics=((1, 1.0), (2, 0.25), (3, 0.06)))


def impact(n: int, seed: int, lo: float, hi: float, decay: float) -> np.ndarray:
    return filt(noise(n, seed), "band", [lo, hi], SR, 2) * env_ad(n, SR, 0.0008, decay)


def click(seed: int, lo: float = 1500.0, hi: float = 7000.0) -> np.ndarray:
    return impact(int(0.02 * SR), seed, lo, hi, 0.003)


def knock(f0: float, seed: int, decay: float = 0.05) -> np.ndarray:
    """Ein hölzerner Schlag (Holzblock, Richterhammer): ein Stoß durch zwei Resonanzen."""
    n = int(0.25 * SR)
    x = noise(n, seed) * env_ad(n, SR, 0.0003, 0.003)
    y = resonator(x, f0, 18.0, SR) + 0.5 * resonator(x, f0 * 2.71, 22.0, SR)
    return norm(y) * env_ad(n, SR, 0.0005, decay)


def sweep(x: np.ndarray, points: list[tuple[float, float]], q: float = 0.8) -> np.ndarray:
    """Ein Bandpass, dessen Mitte dem Verlauf points (0…1, Hz) folgt."""
    n = len(x)
    out = np.zeros(n)
    steps = 32
    fc = curve(steps, points)
    for i in range(steps):
        a, b = i * n // steps, (i + 1) * n // steps
        f = fc[i]
        seg = filt(x[max(0, a - 1024):b], "band", [max(40.0, f / (1.0 + q)), min(18000.0, f * (1.0 + q))], SR, 2)
        out[a:b] = seg[-(b - a):]
    return out


def whoosh(n: int, seed: int, f0: float, f1: float, q: float = 0.9) -> np.ndarray:
    return sweep(noise(n, seed), [(0.0, f0), (1.0, f1)], q)


def flange(x: np.ndarray, rate: float = 0.8, depth: float = 0.004, base: float = 0.0008) -> np.ndarray:
    """Ein Kammfilter mit wanderndem Abstand: verdoppelt, verschmiert (für die Klone)."""
    n = len(x)
    t = np.arange(n) / SR
    d = (base + depth * (0.5 + 0.5 * np.sin(2 * np.pi * rate * t))) * SR
    return 0.65 * x + 0.65 * np.interp(np.arange(n) - d, np.arange(n), x, left=0.0)


# =====================================================================================
# Bausteine: Bildfehler, hörbar gemacht
# =====================================================================================

def crush(x: np.ndarray, bits_: int = 6, hold: int = 3) -> np.ndarray:
    """Grobe Abtastung (jeder hold-te Wert wird gehalten) und wenige Stufen."""
    y = x[(np.arange(len(x)) // hold) * hold] if hold > 1 else x.copy()
    peak = np.max(np.abs(y)) + 1e-9
    q = 2 ** (bits_ - 1)
    return np.round(y / peak * q) / q * peak


def freeze(x: np.ndarray, at: float, seg: float, reps: int, fall: float = 1.0, gain: float = 1.0) -> np.ndarray:
    """Der Ton hängt: das Stück [at, at+seg] wiederholt sich reps-mal (auf Wunsch jedes Mal etwas tiefer), danach geht
    es weiter, als wäre nichts gewesen."""
    a = int(at * SR)
    m = max(16, int(seg * SR))
    piece = x[a:a + m].copy()
    loops = []
    for i in range(reps):
        p = piece
        ratio = fall ** i
        if ratio != 1.0:
            p = np.interp(np.arange(int(m / ratio)) * ratio, np.arange(m), piece)
        loops.append(p * gain ** i)
    return np.concatenate([x[:a]] + loops + [x[a + m:]])


def scramble(x: np.ndarray, seed: int, chunk: float = 0.035, prob: float = 0.3) -> np.ndarray:
    """Schnipsel geraten durcheinander: rückwärts, falsch schnell, grob gestuft oder doppelt."""
    r = rng(seed)
    y = x.copy()
    m = int(chunk * SR)
    for a in range(m, len(x) - 3 * m, m):
        if r.random() >= prob:
            continue
        mode = int(r.integers(0, 4))
        seg = x[a:a + m]
        if mode == 0:
            y[a:a + m] = seg[::-1]
        elif mode == 1:
            ratio = float(r.choice([0.5, 1.5, 2.0]))
            src = x[a:a + int(m * ratio) + 2]
            y[a:a + m] = np.interp(np.arange(m) * ratio, np.arange(len(src)), src)
        elif mode == 2:
            y[a:a + m] = crush(seg, 4, 8)
        else:
            y[a:a + m] = x[a - m:a]
    return y


def dropouts(x: np.ndarray, seed: int, rate: float = 5.0, lo: float = 0.01, hi: float = 0.035) -> np.ndarray:
    """Aussetzer: kurze Lücken, hart geschnitten."""
    r = rng(seed)
    y = x.copy()
    n = len(x)
    for _ in range(max(1, int(rate * n / SR))):
        a = int(r.uniform(0.1, 0.9) * n)
        y[a:a + int(r.uniform(lo, hi) * SR)] = 0.0
    return y


def varispeed(x: np.ndarray, rate: np.ndarray) -> np.ndarray:
    """Spielt x mit wechselnder Geschwindigkeit ab (1 = normal, 0.5 = halb so schnell und eine Oktave tiefer)."""
    pos = np.cumsum(rate) - rate[0]
    pos = pos[pos < len(x) - 1]
    return np.interp(pos, np.arange(len(x)), x)


def buffer_loop(src: np.ndarray, at: float, grain: float, seconds: float, sag: float = 0.0) -> np.ndarray:
    """Der Tonpuffer hängt: ein Schnipsel von grain Sekunden wiederholt sich – das Brummen eines abgestürzten Spiels."""
    a = int(at * SR)
    g = max(32, int(grain * SR))
    piece = src[a:a + g]
    reps = max(1, int(seconds * SR / g))
    return np.concatenate([piece * (1.0 - sag * k / reps) for k in range(reps)])


# =====================================================================================
# Bausteine: Annis Stimme
# =====================================================================================

# Formanten eines großen Rachens (etwa 0,85 der Werte einer Männerstimme): Frequenz, Bandbreite, Pegel
TROLL = {
    "a": [(600, 110, 1.0), (980, 130, 0.65), (2150, 220, 0.22), (2900, 300, 0.07)],
    "o": [(400, 90, 1.0), (690, 110, 0.6), (2050, 220, 0.14)],
    "u": [(285, 70, 1.0), (600, 100, 0.4), (2050, 220, 0.08)],
    "e": [(410, 90, 1.0), (1550, 170, 0.45), (2150, 240, 0.2)],
    "ö": [(390, 90, 1.0), (1230, 150, 0.5), (1980, 240, 0.18)],
    "m": [(240, 60, 1.0), (1050, 200, 0.06)],
}


def troll(n: int, f0: float, f1: float, vowel: str, seed: int, growl: float = 0.4, shape: float = 0.7,
          breath: float = 0.05) -> np.ndarray:
    """Eine Silbe von Anni: eine raue, tiefe Stimme aus einem großen Rachen – jede zweite Schwingung schwächer (das
    Knurren), dazu ein Kratzen im Takt der Stimmbänder. Spitze 1."""
    r = rng(seed)
    t = np.arange(n) / SR
    wobble = norm(filt(noise(n, seed + 1), "low", 16, SR, 2))
    f = glide(n, f0, f1, shape) * (1.0 + 0.013 * np.sin(2 * np.pi * r.uniform(4.5, 6.0) * t + r.uniform(0, 6)) + 0.012 * wobble)
    ph = phase_of(f, SR)
    src = saw(f, np.ones(n), SR, partials=48, rolloff=1.05)
    src *= 1.0 - growl * (0.5 + 0.5 * np.sin(0.5 * ph))
    rasp = filt(noise(n, seed + 2), "band", [250, 3000], SR, 2) * (0.5 + 0.5 * np.sin(ph)) ** 6
    y = norm(formant_filter(src + rasp * (0.6 + growl), SR, TROLL[vowel]))
    chest = norm(filt(src, "low", 260, SR, 2))
    air = filt(noise(n, seed + 3), "band", [500, 4000], SR, 2) * breath
    return norm(y + 0.35 * chest + air) * env_asr(n, SR, 0.02, 0.06)


def breath_h(m: int, vowel: str, seed: int) -> np.ndarray:
    """Der Hauch vor einer Silbe („h“), durch den Rachen geformt."""
    y = norm(formant_filter(noise(m, seed), SR, TROLL[vowel]))
    return y * env_asr(m, SR, 0.01, 0.03) * 0.3


def syllable(f0: float, f1: float, vowel: str, dur: float, seed: int, h: float = 0.045, growl: float = 0.4,
             shape: float = 0.7) -> np.ndarray:
    n = int((dur + h) * SR)
    y = np.zeros(n)
    if h > 0:
        mix_at(y, breath_h(int((h + 0.02) * SR), vowel, seed + 50), 0.0)
    mix_at(y, troll(int(dur * SR), f0, f1, vowel, seed, growl, shape), h)
    return y


def power_off(seed: int) -> np.ndarray:
    """Ein Bildschirm geht aus: ein fallender Pfeifton, ein dumpfer Schlag, verklingendes Knistern."""
    n = int(1.2 * SR)
    y = np.zeros(n)
    m = int(0.38 * SR)
    mix_at(y, tone(glide(m, 1700, 55, 0.5), env_ad(m, SR, 0.002, 0.16), SR, harmonics=((1, 1.0), (2, 0.15))), 0.0, 0.5)
    mix_at(y, thud(int(0.5 * SR), 95, 38, 0.1), 0.0, 0.8)
    m = int(1.1 * SR)
    static = lp(noise(m, seed), 5000) * curve(m, [(0, 0.0), (0.03, 0.6), (1.0, 0.0)]) ** 2
    mix_at(y, static, 0.02, 0.12)
    for k in range(4):
        mix_at(y, click(seed + 10 + k), 0.15 + k * rng(seed + k).uniform(0.08, 0.2), 0.25)
    return y


# =====================================================================================
# Klänge: Anni
# =====================================================================================

def ambient(seed: int) -> np.ndarray:
    """Sie kichert in sich hinein: zwei, drei tiefe „hm-hö“, der letzte Laut bleibt kurz im Puffer hängen, dazu ein
    leises Datenzirpen."""
    r = rng(seed)
    y = np.zeros(int(1.6 * SR))
    f = r.uniform(140, 158)
    t = 0.04
    last = t
    for k in range(int(r.integers(2, 4))):
        d = r.uniform(0.12, 0.16)
        v = "m" if k == 0 else str(r.choice(["ö", "e", "a"]))
        mix_at(y, syllable(f * 1.1, f * 0.9, v, d, seed + k, h=0.035 if k else 0.0, growl=0.5), t, 0.8)
        last = t
        t += d + r.uniform(0.06, 0.1)
        f *= r.uniform(0.93, 0.97)
    y = freeze(y, last + 0.06, r.uniform(0.035, 0.05), int(r.integers(3, 5)), fall=0.985, gain=0.92)
    y = pad(y, 0.4)
    mix_at(y, bits(r.uniform(0.18, 0.3), seed + 9, 700, 1900), r.uniform(0.15, 0.6), 0.07)
    return y


def laugh(seed: int) -> np.ndarray:
    """Ihr Lachen: „HA-ha-hö-ha-HAAA“ – rau und tief. Der letzte Lacher bleibt hängen und stottert (jedes Mal ein wenig
    tiefer), dann zerfällt er grob gestuft. Manchmal piepst danach noch ein kleines, schiefes Spielmotiv."""
    r = rng(seed)
    y = np.zeros(int(2.2 * SR))
    f = r.uniform(170, 186)
    t = 0.02
    count = int(r.integers(4, 6))
    last = t
    for k in range(count):
        final = k == count - 1
        d = 0.36 if final else r.uniform(0.12, 0.15)
        v = "a" if k == 0 or final else str(r.choice(["a", "ö", "e"]))
        rise = 1.15 if k == 1 else 1.08
        mix_at(y, syllable(f * rise, f * (0.78 if final else 0.88), v, d, seed + k, h=0.045, growl=0.45), t, 1.0 if k else 0.9)
        last = t
        t += d + r.uniform(0.04, 0.06)
        f *= r.uniform(0.95, 0.98)
    y = scramble(y, seed + 20, 0.03, 0.07)
    y = freeze(y, last + 0.13, r.uniform(0.045, 0.06), int(r.integers(5, 8)), fall=0.975, gain=0.95)
    # das Ende des Lachers grob gestuft
    cut = int((last + 0.3 + 0.3) * SR)
    tail = y[cut:].copy()
    y[cut:] = 0.45 * tail + 0.55 * lp(crush(tail, 4, 6), 6500)
    y = pad(y, 0.5)
    if r.random() < 0.6:
        end = t + 0.35
        base = r.uniform(420, 470)
        for k, s in enumerate((0, -1, -6)):
            mix_at(y, chip(note(s, base), 0.16 if k < 2 else 0.32, 0.25, 0.08 if k < 2 else 0.16), end + k * 0.1, 0.1)
    return y


def hurt(seed: int) -> np.ndarray:
    """Getroffen: ein kurzes, raues „Uargh!“, das in Schnipsel zerfällt, dazu ein Fehlerblitz."""
    r = rng(seed)
    y = np.zeros(int(0.8 * SR))
    f = r.uniform(185, 205)
    mix_at(y, syllable(f * 1.25, f * 0.78, str(r.choice(["a", "ö"])), r.uniform(0.2, 0.25), seed, h=0.025, growl=0.55, shape=0.5), 0.0)
    y = scramble(y, seed + 5, 0.028, 0.35)
    m = int(0.06 * SR)
    mix_at(y, lp(crush(noise(m, seed + 6), 3, 12), 6000) * env_ad(m, SR, 0.001, 0.02), r.uniform(0.04, 0.1), 0.18)
    mix_at(y, chip(r.uniform(700, 860), 0.12, 0.25, 0.04, f1=r.uniform(260, 340)), 0.01, 0.1)
    return y


def death(seed: int) -> np.ndarray:
    """Endgültiger Absturz: ein langes „Nööö…“, das hängt, dann wie ein Band langsamer und tiefer läuft, immer gröber
    zerfällt – und der Bildschirm geht aus."""
    groan = np.zeros(int(1.7 * SR))
    mix_at(groan, troll(int(0.1 * SR), 200, 195, "m", seed, growl=0.4), 0.0, 0.7)
    mix_at(groan, troll(int(1.55 * SR), 205, 150, "ö", seed + 1, growl=0.55, shape=1.2), 0.07)
    groan = freeze(groan, 0.55, 0.06, 5, fall=0.985, gain=0.95)
    slowed = varispeed(groan, curve(int(len(groan) / 0.5), [(0, 1.0), (0.3, 0.85), (1.0, 0.1)]))
    w = curve(len(slowed), [(0, 0.0), (0.35, 0.3), (1.0, 1.0)])
    slowed = slowed * (1 - w) + lp(crush(slowed, 4, 10), 5000) * w
    slowed = dropouts(slowed, seed + 3, 2.0, 0.015, 0.05)
    y = np.zeros(len(slowed) + int(1.6 * SR))
    mix_at(y, slowed, 0.0)
    t_off = len(slowed) / SR - 0.15
    mix_at(y, power_off(seed + 7), t_off, 0.9)
    return y


def step(seed: int) -> np.ndarray:
    """Ein schwerer Schritt: dumpfer Stoß, Erde und Kies – und ganz selten ein winziges digitales Knistern."""
    r = rng(seed)
    y = np.zeros(int(0.55 * SR))
    mix_at(y, thud(int(0.5 * SR), r.uniform(72, 86), 38, 0.085), 0.0, 1.0)
    mix_at(y, impact(int(0.2 * SR), seed, 140, 1300, 0.035), 0.0, 0.5)
    for k in range(4):
        mix_at(y, impact(int(0.05 * SR), seed + 10 + k, 700, 3800, 0.008), r.uniform(0.005, 0.07), 0.1)
    if r.random() < 0.6:
        mix_at(y, lp(crush(impact(int(0.04 * SR), seed + 20, 1000, 6000, 0.01), 3, 6), 6500), r.uniform(0.03, 0.07), 0.12)
    return y


def boot(seed: int) -> np.ndarray:
    """Das Laden: ein dumpfes Einschalten, ein anschwellendes Brummen, leises Datenzirpen und eine Tonfolge, die immer
    schneller steigt wie ein Ladebalken – bei 2,3 s (wenn sie sich zusammensetzt) ein heller Akkord."""
    r = rng(seed)
    y = np.zeros(int(3.6 * SR))
    mix_at(y, thud(int(0.6 * SR), 90, 38, 0.14), 0.0, 0.8)
    m = int(2.3 * SR)
    hum = tone(glide(m, 38, 60, 0.7), curve(m, [(0, 0.0), (0.15, 0.45), (0.95, 1.0), (1.0, 0.0)]), SR,
               harmonics=((1, 1.0), (2, 0.55), (3, 0.38), (4, 0.22), (5, 0.15), (7, 0.07)))
    mix_at(y, hum, 0.0, 0.3)
    m = int(1.5 * SR)
    mix_at(y, bits(1.5, seed + 1, 700, 2400) * curve(m, [(0, 0.0), (0.3, 1.0), (1.0, 0.5)]), 0.45, 0.07)
    scale = [0, 3, 7, 10]
    t, k, gap = 0.3, 0, 0.21
    while t < 2.15:
        f = note(scale[k % 4] + 12 * (k // 4), 110.0)
        mix_at(y, chip(f, 0.13, 0.25, 0.05), t, 0.16)
        t += gap
        gap = max(0.05, gap * 0.87)
        k += 1
    hit = 2.27
    for s, a in ((0, 1.0), (7, 0.8), (12, 0.8), (15, 0.6), (19, 0.45)):
        mix_at(y, chip(note(s, 220.0), 1.2, 0.5 if s < 12 else 0.25, 0.4), hit, 0.09 * a)
    mix_at(y, lp(crush(impact(int(0.25 * SR), seed + 2, 300, 6000, 0.06), 4, 5), 7000), hit, 0.45)
    mix_at(y, thud(int(0.6 * SR), 110, 40, 0.12), hit, 0.6)
    mix_at(y, whoosh(int(0.5 * SR), seed + 3, 600, 4000) * curve(int(0.5 * SR), [(0, 0.0), (0.9, 1.0), (1.0, 0.0)]), hit - 0.48, 0.25)
    return y


def disconnect(seed: int) -> np.ndarray:
    """Verbindung verloren: drei fallende Töne, ein Rauschstoß, der Ton läuft aus wie ein Band – dann ist sie fort."""
    y = np.zeros(int(2.6 * SR))
    for k, s in enumerate((7, 3, -2)):
        mix_at(y, chip(note(s, 440.0), 0.16 if k < 2 else 0.4, 0.5, 0.09 if k < 2 else 0.2), 0.02 + k * 0.17, 0.2)
    m = int(0.5 * SR)
    burst = lp(crush(noise(m, seed), 4, 4), 6500) * curve(m, [(0, 0.0), (0.05, 1.0), (1.0, 0.0)]) ** 2
    mix_at(y, burst, 0.62, 0.2)
    src = np.zeros(int(0.9 * SR))
    mix_at(src, troll(int(0.5 * SR), 160, 120, "o", seed + 2, growl=0.5), 0.0, 0.6)
    mix_at(src, chip(note(-2, 440.0), 0.8, 0.5, 0.4), 0.0, 0.3)
    mix_at(y, varispeed(src, curve(int(len(src) / 0.45), [(0, 1.0), (1, 0.05)])), 0.7, 0.5)
    mix_at(y, power_off(seed + 5), 1.6, 0.6)
    return y


def windup(seed: int) -> np.ndarray:
    """Ausholen: ein gepresstes Knurren, ein steigender, grob gestufter Ladeton und der Luftzug des Hammers."""
    r = rng(seed)
    y = np.zeros(int(0.9 * SR))
    mix_at(y, syllable(r.uniform(150, 165), r.uniform(185, 200), "a", 0.55, seed, h=0.06, growl=0.7, shape=1.0), 0.03, 0.55)
    m = int(0.75 * SR)
    charge = saw(glide(m, 90, 560, 1.6), curve(m, [(0, 0.0), (0.75, 0.8), (1.0, 1.0)]), SR, partials=20, rolloff=1.2)
    mix_at(y, lp(crush(charge, 5, 2), 4800) * env_asr(m, SR, 0.01, 0.04), 0.0, 0.3)
    w = int(0.45 * SR)
    mix_at(y, whoosh(w, seed + 3, 300, 1600) * curve(w, [(0, 0.0), (0.7, 1.0), (1.0, 0.0)]), 0.38, 0.35)
    return y


def smash(seed: int) -> np.ndarray:
    """Der Hammer kracht: ein tiefer Schlag, Erde und Brocken – und das Krachen hallt als grob gestuftes Stottern nach."""
    r = rng(seed)
    y = np.zeros(int(1.9 * SR))
    mix_at(y, thud(int(1.3 * SR), 72, 27, 0.33), 0.0, 1.0)
    mix_at(y, impact(int(0.7 * SR), seed, 70, 900, 0.13), 0.0, 0.85)
    mix_at(y, impact(int(0.3 * SR), seed + 1, 600, 5000, 0.045), 0.0, 0.35)
    for k in range(10):
        mix_at(y, impact(int(0.08 * SR), seed + 10 + k, 350, 3200, r.uniform(0.01, 0.03)), r.uniform(0.06, 0.75), r.uniform(0.05, 0.13))
    hit = y[:int(0.05 * SR)].copy()
    mix_at(y, lp(crush(y[:int(0.25 * SR)].copy(), 4, 6), 6500), 0.0, 0.3)
    for k in range(4):
        mix_at(y, lp(crush(hit, 4, 4 + 2 * k), 5500), 0.13 + k * 0.075, 0.38 * 0.7 ** k)
    return y


def lag(seed: int) -> np.ndarray:
    """Ein Hänger: das letzte Stückchen Ton bleibt im Puffer stecken und brummt („brrrrt“) – dann springt es weiter."""
    r = rng(seed)
    src = np.zeros(int(0.4 * SR))
    mix_at(src, troll(int(0.3 * SR), r.uniform(120, 150), r.uniform(110, 135), str(r.choice(["a", "o"])), seed, growl=0.5), 0.0, 0.7)
    mix_at(src, chip(r.uniform(180, 260), 0.35, 0.5, 0.3), 0.0, 0.35)
    mix_at(src, impact(int(0.3 * SR), seed + 1, 200, 3000, 0.2), 0.0, 0.25)
    loop = buffer_loop(src, r.uniform(0.08, 0.14), r.uniform(0.028, 0.045), r.uniform(0.38, 0.5), sag=0.25)
    y = np.zeros(len(loop) + int(0.25 * SR))
    mix_at(y, lp(loop, 4500) * env_asr(len(loop), SR, 0.004, 0.01), 0.0)
    mix_at(y, click(seed + 3), len(loop) / SR, 0.5)
    mix_at(y, chip(r.uniform(900, 1100), 0.05, 0.5, 0.015), len(loop) / SR + 0.01, 0.12)
    return y


def teleport(seed: int) -> np.ndarray:
    """Ein Sprung: ein fallender Zischton, ein grob gestufter Rauschstoß – und ein steigendes Piepen, wo sie auftaucht."""
    r = rng(seed)
    y = np.zeros(int(0.45 * SR))
    m = int(0.17 * SR)
    z = square(glide(m, r.uniform(1300, 1700), r.uniform(110, 160), 0.6), env_ad(m, SR, 0.001, 0.07), 0.25, partials=21)
    mix_at(y, crush(z, 5, 3), 0.0, 0.45)
    mix_at(y, crush(impact(int(0.12 * SR), seed, 400, 6500, 0.03), 3, 8), 0.0, 0.4)
    mix_at(y, chip(r.uniform(480, 620), 0.12, 0.5, 0.04, f1=r.uniform(1000, 1300)), r.uniform(0.12, 0.17), 0.3)
    return lp(y, 8500)


def miss(seed: int) -> np.ndarray:
    """Treffer nicht registriert: ein Luftzug ins Leere und zwei leise, tiefer werdende Töne."""
    r = rng(seed)
    y = np.zeros(int(0.55 * SR))
    w = int(0.2 * SR)
    mix_at(y, whoosh(w, seed, 700, 2600) * env_ad(w, SR, 0.03, 0.05), 0.0, 0.3)
    f = r.uniform(300, 340)
    mix_at(y, chip(f, 0.11, 0.5, 0.05, partials=11), 0.05, 0.22)
    mix_at(y, chip(f / 2 ** 0.5, 0.22, 0.5, 0.09, partials=11), 0.17, 0.22)
    return lp(y, 6500)


def clone(seed: int) -> np.ndarray:
    """Sie verdoppelt sich: ein „Heh!“ im Chor aus drei, vier Annis (versetzt und verstimmt), dazu steigende
    Spieltöne und ein verschmierter Luftzug."""
    r = rng(seed)
    y = np.zeros(int(1.3 * SR))
    f = r.uniform(165, 180)
    for k, (ratio, delay, g) in enumerate(((1.0, 0.0, 1.0), (0.89, 0.045, 0.75), (1.12, 0.09, 0.65), (0.8, 0.14, 0.5))):
        mix_at(y, syllable(f * ratio * 1.1, f * ratio * 0.92, "e", 0.17, seed + k, h=0.04, growl=0.45), 0.05 + delay, 0.5 * g)
    base = r.uniform(300, 350)
    for k in range(4):
        b = base * 2 ** (k * 4 / 12)
        mix_at(y, chip(b, 0.22, 0.25, 0.07, f1=b * 1.5), 0.08 + k * 0.08, 0.13)
    m = int(1.0 * SR)
    mix_at(y, flange(whoosh(m, seed + 7, 500, 3200) * env_asr(m, SR, 0.15, 0.5), 0.9), 0.0, 0.3)
    return flange(y, 1.3, 0.003)


def clone_crash(seed: int) -> np.ndarray:
    """Ein Klon stürzt ab: ein dumpfer Knall, grob gestuftes Krachen, fallende Spieltöne und splitternde Pixel."""
    r = rng(seed)
    y = np.zeros(int(1.3 * SR))
    mix_at(y, thud(int(0.9 * SR), 95, 33, 0.2), 0.0, 0.9)
    mix_at(y, lp(crush(impact(int(0.5 * SR), seed, 150, 6000, 0.1), 4, 5), 7000), 0.0, 0.65)
    for k in range(3):
        mix_at(y, chip(r.uniform(600, 900) * (1 + k * 0.3), 0.4, 0.25, 0.15, f1=r.uniform(60, 110)), 0.02 + k * 0.05, 0.13)
    for k in range(12):
        mix_at(y, chip(r.uniform(1200, 2800), 0.03, 0.5, 0.008, partials=9), r.uniform(0.05, 0.65), r.uniform(0.03, 0.08))
    return y


def clone_pop(seed: int) -> np.ndarray:
    """Ein Klon zerplatzt: ein „Plopp“ mit fallendem Piepen und ein paar Pixelsplittern."""
    r = rng(seed)
    y = np.zeros(int(0.4 * SR))
    mix_at(y, chip(r.uniform(900, 1100), 0.12, 0.5, 0.03, f1=r.uniform(250, 320), partials=15), 0.0, 0.35)
    mix_at(y, lp(crush(impact(int(0.06 * SR), seed, 800, 8000, 0.015), 3, 6), 7500), 0.0, 0.45)
    mix_at(y, thud(int(0.2 * SR), 230, 90, 0.035), 0.0, 0.5)
    for k in range(5):
        mix_at(y, chip(r.uniform(1500, 3000), 0.025, 0.5, 0.006, partials=7), r.uniform(0.02, 0.2), 0.06)
    return y


def missing(seed: int) -> np.ndarray:
    """Die Textur fehlt: ein dumpfer Schlag, dann ein schiefer, summender Rechteckklang (Grundton, Tritonus, Oktave)
    und ein Knistern im Takt eines Schachbretts."""
    r = rng(seed)
    y = np.zeros(int(1.8 * SR))
    m = int(1.6 * SR)
    t = np.arange(m) / SR
    env = curve(m, [(0, 0.0), (0.06, 1.0), (0.6, 0.7), (1.0, 0.0)])
    f = r.uniform(54, 62)
    for ratio, a in ((1.0, 1.0), (2 ** 0.5, 0.55), (2.0, 0.45), (2.12, 0.25)):
        fr = np.full(m, f * ratio) * (1 + 0.004 * np.sin(2 * np.pi * r.uniform(0.3, 1.2) * t))
        mix_at(y, square(fr, env * a, 0.5, partials=21), 0.0, 0.16)
    for k in range(13):
        mix_at(y, click(seed + k, 2000, 6000), 0.1 + k * 0.11, 0.18 if k % 2 == 0 else 0.1)
    mix_at(y, thud(int(0.5 * SR), 120, 42, 0.12), 0.0, 0.6)
    return lp(y, 6000)


def rain(seed: int) -> np.ndarray:
    """Pixelregen: ein Grollen, ein Rauschen von oben – und viele kleine Spieltöne, die herabfallen."""
    r = rng(seed)
    y = np.zeros(int(2.4 * SR))
    m = int(2.2 * SR)
    mix_at(y, whoosh(m, seed, 3000, 500) * curve(m, [(0, 0.0), (0.3, 1.0), (1.0, 0.0)]), 0.0, 0.2)
    t = 0.05
    while t < 2.0:
        f0 = r.uniform(900, 2100)
        mix_at(y, chip(f0, 0.14, 0.25, 0.05, f1=f0 * 0.5, partials=15), t, r.uniform(0.06, 0.12))
        t += r.uniform(0.04, 0.11)
    mix_at(y, thud(int(1.0 * SR), 60, 34, 0.4), 0.0, 0.4)
    return lp(y, 8000)


def pixel(seed: int) -> np.ndarray:
    """Ein Pixelblock schlägt ein: ein kurzer Stoß, grob gestuftes Knirschen und ein kleiner fallender Ton."""
    r = rng(seed)
    y = np.zeros(int(0.45 * SR))
    mix_at(y, thud(int(0.3 * SR), r.uniform(110, 140), 50, 0.055), 0.0, 0.8)
    mix_at(y, lp(crush(impact(int(0.15 * SR), seed, 400, 6000, 0.028), 3, int(r.integers(4, 9))), 7000), 0.0, 0.55)
    mix_at(y, chip(r.uniform(400, 650), 0.08, 0.5, 0.025, f1=r.uniform(150, 240), partials=15), 0.0, 0.2)
    return y


def rollback_mark(seed: int) -> np.ndarray:
    """Gespeichert: zwei Klicks wie ein Auslöser, ein kleines steigendes Spielmotiv und ein Uhrticken."""
    r = rng(seed)
    y = np.zeros(int(1.0 * SR))
    base = r.uniform(520, 560)
    for k, tt in enumerate((0.0, 0.045)):
        mix_at(y, click(seed + k, 1500, 7000), tt, 0.4)
    mix_at(y, chip(base, 0.16, 0.5, 0.07), 0.04, 0.22)
    mix_at(y, chip(base * 1.5, 0.35, 0.5, 0.16), 0.15, 0.22)
    mix_at(y, chip(base * 2, 0.5, 0.25, 0.22), 0.26, 0.12)
    for k in range(3):
        mix_at(y, knock(1900 if k % 2 == 0 else 1500, seed + 5 + k, 0.03), 0.5 + k * 0.14, 0.14)
    return y


def rollback(seed: int) -> np.ndarray:
    """Rückruck: alles läuft rückwärts und immer schneller (ein zurückspulendes Band, zwitschernd) – dann ein harter
    Schnapp, und alle stehen wieder dort, wo sie waren."""
    r = rng(seed)
    src = np.zeros(int(1.4 * SR))
    mix_at(src, syllable(170, 140, "a", 0.3, seed, growl=0.5), 0.1, 0.5)
    mix_at(src, impact(int(0.8 * SR), seed + 1, 200, 3000, 0.3), 0.3, 0.4)
    for k in range(5):
        mix_at(src, chip(r.uniform(300, 900), 0.15, 0.5, 0.06), 0.1 + k * 0.2, 0.25)
    rev = varispeed(src[::-1], curve(int(0.95 * SR), [(0, 0.8), (1, 2.6)]))
    y = np.zeros(int(1.5 * SR))
    mix_at(y, lp(rev, 6500) * curve(len(rev), [(0, 0.2), (1, 1.0)]), 0.0, 0.6)
    m = len(rev)
    t = np.arange(m) / SR
    rate = 6.0 + 14.0 * t / (m / SR)
    lfo = (np.cumsum(rate) / SR) % 1.0
    whine = tone(500 + 900 * lfo, curve(m, [(0, 0.0), (0.2, 0.6), (1, 1.0)]), SR, harmonics=((1, 1.0), (2, 0.2)))
    mix_at(y, whine, 0.0, 0.08)
    snap = m / SR
    mix_at(y, thud(int(0.4 * SR), 170, 48, 0.07), snap, 0.9)
    mix_at(y, impact(int(0.1 * SR), seed + 5, 1000, 7000, 0.018), snap, 0.5)
    mix_at(y, knock(1300, seed + 6, 0.04), snap, 0.3)
    return y


def scan(seed: int) -> np.ndarray:
    """Die Zeile fegt: ein Brummen, dessen Klangfarbe einmal herum wandert wie ein Radarstrahl, ein leiser, weicher
    Bildröhrenton und ein Luftzug, wenn sie vorbeizieht."""
    r = rng(seed)
    y = np.zeros(int(2.0 * SR))
    m = int(1.8 * SR)
    env = curve(m, [(0, 0.0), (0.05, 1.0), (0.85, 0.8), (1.0, 0.0)])
    buzz = saw(np.full(m, r.uniform(104, 116)), np.ones(m), SR, partials=60, rolloff=0.9)
    mix_at(y, sweep(buzz, [(0, 400), (0.5, 3200), (1, 600)], 0.6) * env, 0.0, 0.5)
    mix_at(y, tone(np.full(m, 60.0), env, SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.3))), 0.0, 0.25)
    mix_at(y, tone(np.full(m, 6300.0), env, SR, harmonics=((1, 1.0),)), 0.0, 0.012)
    w = int(0.6 * SR)
    mix_at(y, whoosh(w, seed, 500, 2800) * env_asr(w, SR, 0.25, 0.3), 0.55, 0.28)
    return y


def crash(seed: int) -> np.ndarray:
    """Der Fehlerklang: ein kurzes Durcheinander aus Stimme, Spieltönen und Rauschen – dann hängt der Ton und brummt
    (ein Schnipsel von 40 ms, immer wieder), bricht hart ab, und nach einer Stille kommen zwei tiefe Fehlertöne."""
    r = rng(seed)
    pre = np.zeros(int(0.5 * SR))
    mix_at(pre, syllable(190, 230, "a", 0.32, seed, h=0.04, growl=0.6), 0.0, 0.8)
    for k in range(4):
        mix_at(pre, chip(r.uniform(300, 1200), 0.1, 0.25, 0.05), 0.05 + k * 0.09, 0.25)
    mix_at(pre, lp(crush(noise(int(0.5 * SR), seed + 1), 4, 6), 5000) * 0.15, 0.0)
    pre = scramble(pre, seed + 2, 0.03, 0.5)
    loop = buffer_loop(pre, 0.24, 0.042, 1.5, sag=0.15)
    loop = lp(loop, 4500)
    y = np.zeros(int(3.4 * SR))
    mix_at(y, pre, 0.0)
    mix_at(y, loop, 0.5, 0.95)
    end = 0.5 + len(loop) / SR
    mix_at(y, click(seed + 3, 500, 5000), end, 0.6)
    mix_at(y, chip(196.0, 0.25, 0.5, 0.1, partials=15), end + 0.4, 0.3)
    mix_at(y, chip(146.8, 0.5, 0.5, 0.2, partials=15), end + 0.65, 0.3)
    return y


def restart(seed: int) -> np.ndarray:
    """Neustart: ein dumpfes Einschalten, ein steigender Ton, ein schneller Lauf nach oben und ein strahlender Akkord."""
    r = rng(seed)
    y = np.zeros(int(2.4 * SR))
    mix_at(y, thud(int(0.6 * SR), 72, 34, 0.15), 0.0, 0.8)
    m = int(0.6 * SR)
    mix_at(y, tone(glide(m, 80, 900, 1.3), curve(m, [(0, 0.0), (0.8, 1.0), (1.0, 0.0)]), SR, harmonics=((1, 1.0), (2, 0.3))), 0.0, 0.13)
    for k, s in enumerate((0, 7, 12, 19)):
        mix_at(y, chip(note(s, 220.0), 0.15, 0.25, 0.06), 0.45 + k * 0.075, 0.18)
    hit = 0.45 + 4 * 0.075
    for s, a in ((0, 1.0), (3, 0.8), (7, 0.8), (12, 0.7), (19, 0.4)):
        mix_at(y, chip(note(s, 220.0), 1.3, 0.5 if s < 12 else 0.25, 0.45), hit, 0.08 * a)
    mix_at(y, lp(crush(impact(int(0.2 * SR), seed + 1, 300, 6000, 0.05), 4, 4), 7000), hit, 0.35)
    mix_at(y, bits(0.4, seed + 2, 900, 2400), hit + 0.1 + r.uniform(0, 0.1), 0.05)
    return y


def revive(seed: int) -> np.ndarray:
    """Die Wiederherstellung (genau auf die Ticks ihrer Vorführung gelegt): ein Aufschrei, der im Puffer hängen bleibt und
    abreißt („Schwerer Ausnahmefehler“), der Ton läuft aus – bei 1,5 s zerspringt sie in Pixel. Leises Suchen (Knistern,
    Datenzirpen), bei 2,4 s „Sicherung gefunden“ (zwei steigende Töne), dann füllt sich die Leiste: eine Tonfolge, die immer
    schneller steigt, ein anschwellendes Brummen und ein rückwärts laufender Sog – bei 5,6 s setzt sie sich als Anni 2.0
    zusammen: ein tiefer Schlag und ein finsterer, verminderter Akkord."""
    r = rng(seed)
    y = np.zeros(int(7.2 * SR))
    # Ausnahmefehler: Aufschrei, hängender Puffer, Abriss
    pre = np.zeros(int(0.45 * SR))
    mix_at(pre, syllable(200, 240, "a", 0.3, seed, h=0.03, growl=0.6), 0.0, 0.8)
    mix_at(pre, chip(note(6, 220.0), 0.3, 0.25, 0.2), 0.02, 0.2)
    pre = scramble(pre, seed + 1, 0.03, 0.4)
    mix_at(y, pre, 0.0)
    loop = lp(buffer_loop(pre, 0.2, 0.038, 0.5, sag=0.2), 4500)
    mix_at(y, loop, 0.3, 0.8)
    mix_at(y, click(seed + 2, 500, 5000), 0.8, 0.5)
    # der Ton läuft aus
    src = np.zeros(int(0.6 * SR))
    for s_, a in ((0, 1.0), (3, 0.8), (6, 0.7)):
        mix_at(src, chip(note(s_, 220.0), 0.6, 0.5, 0.5), 0.0, 0.12 * a)
    mix_at(src, troll(int(0.5 * SR), 170, 150, "o", seed + 3, growl=0.5), 0.0, 0.35)
    mix_at(y, varispeed(src, curve(int(len(src) / 0.5), [(0, 1.0), (1, 0.06)])), 0.85, 0.6)
    # bei 1,5 s: sie zerspringt in Pixel
    t0 = 1.5
    mix_at(y, thud(int(0.9 * SR), 110, 32, 0.22), t0, 0.9)
    mix_at(y, lp(crush(impact(int(0.5 * SR), seed + 4, 300, 7000, 0.09), 4, 4), 7500), t0, 0.6)
    for k in range(22):
        f = r.uniform(900, 2600)
        mix_at(y, chip(f, 0.09, 0.5, 0.025, f1=f * 0.6, partials=9), t0 + r.uniform(0.0, 0.7), r.uniform(0.03, 0.08))
    # Suchen: leises Knistern und Zirpen
    m = int(0.9 * SR)
    mix_at(y, lp(noise(m, seed + 5), 4000) * curve(m, [(0, 0.0), (0.2, 1.0), (1, 0.6)]) * 0.04, 1.6)
    mix_at(y, bits(0.7, seed + 6, 600, 1800) * curve(int(0.7 * SR), [(0, 0.0), (0.3, 1.0), (1, 0.4)]), 1.7, 0.05)
    for k in range(4):
        mix_at(y, knock(1500, seed + 7 + k, 0.025), 1.75 + k * 0.16, 0.07)
    # bei 2,4 s: gefunden
    mix_at(y, chip(note(3, 440.0), 0.14, 0.5, 0.07), 2.4, 0.2)
    mix_at(y, chip(note(10, 440.0), 0.4, 0.5, 0.18), 2.52, 0.2)
    # 2,8 … 5,6 s: die Leiste füllt sich
    scale = [0, 3, 6, 9]
    t, k, gap = 2.8, 0, 0.22
    while t < 5.45:
        f = note(scale[k % 4] + 12 * (k // 4), 110.0)
        mix_at(y, chip(f, 0.12, 0.25, 0.045), t, 0.13)
        t += gap
        gap = max(0.045, gap * 0.9)
        k += 1
    m = int(2.8 * SR)
    hum = tone(glide(m, 36, 72, 1.4), curve(m, [(0, 0.0), (0.2, 0.4), (1.0, 1.0)]), SR,
               harmonics=((1, 1.0), (2, 0.55), (3, 0.38), (4, 0.22), (5, 0.15), (7, 0.07)))
    mix_at(y, hum, 2.8, 0.3)
    mix_at(y, bits(2.6, seed + 8, 800, 2600) * curve(int(2.6 * SR), [(0, 0.0), (0.4, 0.6), (1.0, 1.0)]), 2.9, 0.05)
    w = int(1.2 * SR)
    mix_at(y, (whoosh(w, seed + 9, 4000, 500) * env_ad(w, SR, 0.01, 0.5))[::-1], 5.6 - 1.2, 0.3)
    # bei 5,6 s: Anni 2.0
    hit = 5.6
    mix_at(y, thud(int(1.4 * SR), 80, 28, 0.38), hit, 1.0)
    mix_at(y, lp(crush(impact(int(0.35 * SR), seed + 10, 200, 6000, 0.07), 4, 5), 7000), hit, 0.55)
    for s_, a in ((0, 1.0), (3, 0.85), (6, 0.8), (12, 0.6), (15, 0.4)):
        mix_at(y, chip(note(s_, 110.0), 1.5, 0.5 if s_ < 12 else 0.25, 0.5), hit, 0.1 * a)
    return y


# =====================================================================================
# Klänge: Fehlerkern, Bannhammer, Fehlerflügel
# =====================================================================================

def core_hum(seed: int) -> np.ndarray:
    """Der Fehlerkern summt: ein leises, weiches Brummen, ein wenig Datenzirpen und Knistern."""
    r = rng(seed)
    m = int(2.2 * SR)
    t = np.arange(m) / SR
    env = curve(m, [(0, 0.0), (0.25, 1.0), (0.75, 1.0), (1.0, 0.0)])
    f = r.uniform(50, 58) * (1 + 0.004 * np.sin(2 * np.pi * 0.7 * t))
    y = 0.5 * tone(f, env, SR, harmonics=((1, 1.0), (2, 0.6), (3, 0.45), (4, 0.25), (5, 0.18), (6, 0.1), (8, 0.05)))
    for k in range(int(r.integers(1, 3))):
        mix_at(y, bits(r.uniform(0.15, 0.3), seed + k, 800, 2000), r.uniform(0.3, 1.5), 0.05)
    for k in range(5):
        mix_at(y, click(seed + 20 + k, 2000, 6000), r.uniform(0.1, 2.0), 0.06)
    return lp(y, 6000)


def core_on(seed: int) -> np.ndarray:
    """Der Kern springt an: ein Schlag, ein steigender Ton, drei steigende Piepser und ein anschwellendes Brummen."""
    y = np.zeros(int(1.8 * SR))
    mix_at(y, thud(int(0.6 * SR), 85, 36, 0.14), 0.0, 0.8)
    m = int(0.5 * SR)
    mix_at(y, tone(glide(m, 60, 1200, 1.4), curve(m, [(0, 0.0), (0.8, 1.0), (1.0, 0.0)]), SR, harmonics=((1, 1.0), (2, 0.25))), 0.0, 0.12)
    for k, s in enumerate((0, 5, 12)):
        mix_at(y, chip(note(s, 440.0), 0.14 if k < 2 else 0.35, 0.25, 0.06 if k < 2 else 0.14), 0.45 + k * 0.12, 0.17)
    m = int(1.2 * SR)
    mix_at(y, tone(np.full(m, 55.0), curve(m, [(0, 0.0), (0.4, 1.0), (1.0, 0.0)]), SR,
                   harmonics=((1, 1.0), (2, 0.5), (3, 0.35), (4, 0.2))), 0.4, 0.25)
    return y


def core_off(seed: int) -> np.ndarray:
    """Der Kern stürzt ab: ein Rauschstoß, der Ton läuft aus, der Bildschirm geht aus."""
    y = np.zeros(int(1.8 * SR))
    m = int(0.3 * SR)
    mix_at(y, lp(crush(noise(m, seed), 4, 5), 6000) * env_ad(m, SR, 0.002, 0.08), 0.0, 0.3)
    src = np.zeros(int(0.6 * SR))
    mix_at(src, tone(np.full(len(src), 55.0), np.ones(len(src)), SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.35), (4, 0.2))), 0.0, 0.5)
    mix_at(src, chip(note(0, 440.0), 0.6, 0.25, 0.3), 0.0, 0.25)
    mix_at(y, varispeed(src, curve(int(len(src) / 0.5), [(0, 1.0), (1, 0.05)])), 0.05, 0.5)
    mix_at(y, power_off(seed + 3), 0.45, 0.7)
    return y


def jolt(seed: int) -> np.ndarray:
    """Ruckler: ein elektrisches Knattern, ein grob gestuftes Brummen und ein Ruck zurück (ein rückwärts laufender
    Luftzug)."""
    y = np.zeros(int(0.6 * SR))
    m = int(0.25 * SR)
    t = np.arange(m) / SR
    gate = (np.sin(2 * np.pi * 55 * t) > 0.3).astype(float)
    mix_at(y, filt(noise(m, seed), "band", [1500, 6500], SR, 2) * gate * env_ad(m, SR, 0.002, 0.08), 0.0, 0.45)
    mix_at(y, crush(square(np.full(m, 110.0), env_ad(m, SR, 0.002, 0.09), 0.5, 21), 4, 6), 0.0, 0.3)
    w = int(0.22 * SR)
    mix_at(y, (whoosh(w, seed + 2, 3200, 400) * env_ad(w, SR, 0.005, 0.12))[::-1], 0.18, 0.4)
    mix_at(y, thud(int(0.2 * SR), 150, 60, 0.04), 0.39, 0.5)
    return lp(y, 8500)


def ban(seed: int) -> np.ndarray:
    """Bann: ein Richterhammer kracht (Holz auf Holz, dazu ein tiefer Schlag), grob gestuftes Krachen – und ein tiefer,
    fallender Summer."""
    r = rng(seed)
    y = np.zeros(int(1.4 * SR))
    mix_at(y, knock(r.uniform(560, 640), seed, 0.08), 0.0, 0.9)
    mix_at(y, thud(int(0.9 * SR), 80, 30, 0.22), 0.0, 1.0)
    mix_at(y, lp(crush(impact(int(0.4 * SR), seed + 1, 200, 6000, 0.08), 4, 5), 7000), 0.0, 0.5)
    m = int(0.65 * SR)
    mix_at(y, square(glide(m, 220, 110, 1.0), env_asr(m, SR, 0.01, 0.15), 0.5, 21), 0.2, 0.18)
    mix_at(y, square(glide(m, 233, 116, 1.0), env_asr(m, SR, 0.01, 0.15), 0.5, 21), 0.2, 0.12)
    for k in range(10):
        mix_at(y, chip(r.uniform(1200, 2800), 0.03, 0.5, 0.008, partials=9), r.uniform(0.02, 0.5), 0.05)
    return y


def blink(seed: int) -> np.ndarray:
    """Blinzeln: ein schneller, grob gestufter Luftzug und ein kleines steigendes Piepen."""
    r = rng(seed)
    y = np.zeros(int(0.4 * SR))
    w = int(0.22 * SR)
    mix_at(y, crush(whoosh(w, seed, 500, 3400) * curve(w, [(0, 0.0), (0.6, 1.0), (1.0, 0.0)]), 5, 3), 0.0, 0.55)
    mix_at(y, chip(r.uniform(700, 850), 0.1, 0.25, 0.035, f1=r.uniform(1400, 1700), partials=15), 0.13, 0.22)
    return lp(y, 6500)


# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil) oder None, Ziel-Pegel dBFS, Dateistamm)
SYNTH = {
    "ambient": (ambient, 3, (1.0, 0.18), -22.0, "anni_troll_ambient"),
    "laugh": (laugh, 3, (1.4, 0.22), -16.0, "anni_troll_laugh"),
    "hurt": (hurt, 3, (0.9, 0.15), -17.0, "anni_troll_hurt"),
    "death": (death, 1, (2.0, 0.28), -16.0, "anni_troll_death"),
    "step": (step, 4, (0.7, 0.12), -19.0, "anni_troll_step"),
    "boot": (boot, 1, (1.6, 0.25), -17.0, "anni_troll_boot"),
    "disconnect": (disconnect, 1, (1.4, 0.22), -17.0, "anni_troll_disconnect"),
    "windup": (windup, 2, (1.0, 0.15), -18.0, "anni_troll_windup"),
    "smash": (smash, 3, (1.8, 0.25), -13.0, "anni_troll_smash"),
    "lag": (lag, 3, (0.6, 0.1), -21.0, "anni_troll_lag"),
    "teleport": (teleport, 3, (0.9, 0.15), -19.0, "anni_troll_teleport"),
    "miss": (miss, 3, (0.7, 0.12), -22.0, "anni_troll_miss"),
    "clone": (clone, 2, (1.4, 0.22), -17.0, "anni_troll_clone"),
    "clone_crash": (clone_crash, 2, (1.4, 0.22), -15.0, "anni_troll_clone_crash"),
    "clone_pop": (clone_pop, 3, (0.8, 0.15), -19.0, "anni_troll_clone_pop"),
    "missing": (missing, 2, (1.2, 0.2), -18.0, "anni_troll_missing"),
    "rain": (rain, 2, (1.6, 0.25), -18.0, "anni_troll_rain"),
    "pixel": (pixel, 4, (0.8, 0.15), -19.0, "anni_troll_pixel"),
    "rollback_mark": (rollback_mark, 2, (1.2, 0.2), -19.0, "anni_troll_rollback_mark"),
    "rollback": (rollback, 2, (1.2, 0.2), -16.0, "anni_troll_rollback"),
    "scan": (scan, 2, (1.4, 0.2), -18.0, "anni_troll_scan"),
    "crash": (crash, 1, (1.2, 0.18), -17.0, "anni_troll_crash"),
    "restart": (restart, 1, (1.8, 0.25), -16.0, "anni_troll_restart"),
    "revive": (revive, 1, (1.8, 0.25), -16.0, "anni_troll_revive"),
    "core_hum": (core_hum, 3, (1.0, 0.15), -27.0, "error_core_hum"),
    "core_on": (core_on, 1, (1.4, 0.2), -18.0, "error_core_on"),
    "core_off": (core_off, 1, (1.4, 0.2), -18.0, "error_core_off"),
    "jolt": (jolt, 2, (0.8, 0.15), -18.0, "bannhammer_jolt"),
    "ban": (ban, 2, (1.4, 0.22), -15.0, "bannhammer_ban"),
    "blink": (blink, 3, (0.8, 0.12), -20.0, "glitch_wings_blink"),
}

# Ereignis: (Klänge, Reichweite)
EVENTS = {f"entity.anni_troll.{name}": ([name], reach) for name, reach in (
    ("ambient", 32), ("laugh", 64), ("hurt", 32), ("death", 96), ("step", 24), ("boot", 64), ("disconnect", 64),
    ("windup", 32), ("smash", 64), ("lag", 24), ("teleport", 32), ("miss", 24), ("clone", 48), ("clone_crash", 48),
    ("clone_pop", 24), ("missing", 32), ("rain", 48), ("pixel", 24), ("rollback_mark", 48), ("rollback", 48), ("scan", 48),
    ("crash", 96), ("restart", 96), ("revive", 96))}
EVENTS["block.error_core.hum"] = (["core_hum"], 16)
EVENTS["block.error_core.on"] = (["core_on"], 48)
EVENTS["block.error_core.off"] = (["core_off"], 48)
EVENTS["item.bannhammer.jolt"] = (["jolt"], 16)
EVENTS["item.bannhammer.ban"] = (["ban"], 32)
EVENTS["item.glitch_wings.blink"] = (["blink"], 16)


def render(name: str, preview: Path | None, write: bool) -> list[str]:
    fn, count, space, level, stem = SYNTH[name]
    files = []
    for i in range(count):
        files.append(f"kingdomsatwar:anni/{stem}{i + 1}")
        if not write:
            continue
        x = fn(6700 + i * 53 + zlib.crc32(name.encode()) % 997)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 2300 + i, stereo=False, damping=6500), space[1])
        x = hp(x, 28.0)
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.05)
        x = soft_clip(fit_level(x, level, -2.0))
        write_ogg(x.astype(np.float32), SR, OUT / f"{stem}{i + 1}.ogg", quality=5.0)
        if preview is not None:
            from ambience import spectrogram
            spectrogram(x, SR, preview / f"{name}{i + 1}.png", f"{name}{i + 1}")
    if write:
        print(f"  {name}: {count} Varianten")
    return files


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--preview", type=Path, help="Ordner für Spektrogramme")
    ap.add_argument("--only", help="nur diese Klänge neu erzeugen (Komma-getrennt)")
    args = ap.parse_args()
    only = set(args.only.split(",")) if args.only else None
    if args.preview:
        args.preview.mkdir(parents=True, exist_ok=True)
    OUT.mkdir(parents=True, exist_ok=True)
    rendered = {name: render(name, args.preview, only is None or name in only) for name in SYNTH}
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
