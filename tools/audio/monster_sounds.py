#!/usr/bin/env python3
"""
Erzeugt die Klänge der neuen Monster (0.69): das Knurren, Heulen und Fauchen des Werwolfs, das Gurgeln, Schmatzen und
Aufsteigen des Moorschlurfers (dazu sein klatschender Schlamm), das Grollen, Auftauchen, Spucken und Beißen des
Glutwurms sowie das Flüstern, die Aschewolke, das Seelenfeuer und das Verwehen des Aschengeists – und die der Monster
der Schattenwelt und des Ends (0.71): Ruf, Stoß und Schwingen des Seelenreihers, Röhren, Ansturm und Kristallstampfen des
Nachtwidders, Knurren, Heulen und Phasensprung des Rissjägers, Rascheln, Peitschen und Schnappen des Chorusschlunds. Alles entsteht aus
Rauschen und Tönen (siehe ambience_dsp.py); es werden keine Minecraft-Dateien kopiert.

Bausteine:
- Stimmen aus einzelnen Stimmlippen-Pulsen (unregelmäßig = rau, knurrend) durch Vokalformanten (Größe des Rachens),
- reine Stimmen (Heulen, Winseln, Klagen) aus wenigen Obertönen mit Vibrato,
- Rauschen, dessen Band über die Zeit gleitet (Wischen, Zischen, Schmatzen, Flüstern) – ohne Nähte (STFT),
- Blasen (gleitender, abklingender Ton), Knistern, Holzknarren (Ruckgleiten durch Resonanzen), Zähne und Schläge.

Aufruf (aus dem Projektordner):
    python3 tools/audio/monster_sounds.py [--only name ...] [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/monster/<name><n>.ogg
    Einträge "entity.<wesen>.<klang>" in assets/kingdomsatwar/sounds.json
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, band, colored_noise, curve, fade_edges, filt, fit_level, formant_filter, glide,  # noqa: E402
                          highpass_gain, lowpass_gain, resonator, rng, room, shaped_noise, smoothstep, soft_clip, tilt, tone,
                          trim_tail)
from ambience_dsp import audible_bass  # noqa: E402
from dsp import write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/monster"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100
HOP = 512

VOWELS = {
    "u": [(320, 80, 1.0), (700, 110, 0.4), (2300, 300, 0.08)],
    "o": [(450, 90, 1.0), (800, 120, 0.55), (2600, 300, 0.1)],
    "a": [(720, 110, 1.0), (1150, 150, 0.6), (2500, 300, 0.15)],
    "e": [(500, 100, 1.0), (1700, 200, 0.5), (2500, 300, 0.2)],
    "r": [(380, 120, 1.0), (1100, 200, 0.5), (2200, 300, 0.2)],
}

# Holz unter Spannung: wenige, deutlich klingende Eigenfrequenzen (Frequenz, Güte, Pegel)
WOOD = ((380, 18, 1.0), (870, 20, 0.7), (1540, 22, 0.45), (2650, 24, 0.25))


# =====================================================================================
# Grundlagen
# =====================================================================================

def norm(x: np.ndarray, peak: float = 1.0) -> np.ndarray:
    return x * (peak / (np.max(np.abs(x)) + 1e-12))


def fade(x: np.ndarray) -> np.ndarray:
    """Weiche Kanten für jedes Teilstück (1 ms hinein, bis 12 ms hinaus) – keine Klicks an Nahtstellen."""
    y = x.copy()
    a = max(1, min(int(0.001 * SR), len(y) // 8))
    b = max(1, min(int(0.012 * SR), len(y) // 4))
    y[:a] *= np.linspace(0.0, 1.0, a)
    y[-b:] *= np.linspace(1.0, 0.0, b)
    return y


def mix_at(y: np.ndarray, x: np.ndarray, start: float, gain: float = 1.0) -> None:
    s = int(start * SR)
    if s >= len(y) or len(x) == 0:
        return
    x = fade(x)
    m = min(len(x), len(y) - s)
    y[s:s + m] += x[:m] * gain


def smooth_env(n: int, values: np.ndarray) -> np.ndarray:
    """Weicher Verlauf durch wenige, gleichmäßig verteilte Stützwerte."""
    return np.interp(np.arange(n), np.linspace(0, n - 1, len(values)), values)


def frames_len(dur: float) -> int:
    return max(4, int(np.ceil(dur * SR / HOP))) * HOP


def at_curve(points, u: float) -> float:
    return float(np.interp(u, [p[0] for p in points], [p[1] for p in points]))


def tract(vowel: str, size: float = 1.0, widen: float = 1.0) -> list[tuple[float, float, float]]:
    """Vokalformanten; size < 1 = größerer Rachen (tiefer), widen > 1 = breitere, weichere Formanten."""
    return [(f * size, bw * size * widen, g) for f, bw, g in VOWELS[vowel]]


# =====================================================================================
# Stimmen
# =====================================================================================

def pulse_voice(f: np.ndarray, amp: np.ndarray, seed: int, vowel: str = "o", size: float = 1.0, jitter: float = 0.02,
                shimmer: float = 0.08, sub: float = 0.0, breath: float = 0.0, bright: float = 3500.0, dry: float = 0.15) -> np.ndarray:
    """
    Eine Tierstimme aus einzelnen Stimmlippen-Pulsen: jitter/shimmer machen Abstand und Stärke der Pulse unregelmäßig (rau,
    knurrend), sub schwächt jeden zweiten Puls (Unterton, Knarren), breath mischt Luftrauschen im Takt der Pulse dazu.
    """
    n = len(f)
    r = rng(seed)
    exc = np.zeros(n)
    t = r.uniform(0.0, 1.0 / max(20.0, float(f[0])))
    k = 0
    while True:
        i = int(t * SR)
        if i >= n:
            break
        a = amp[i] * max(0.0, 1.0 + shimmer * r.standard_normal())
        if sub and k % 2:
            a *= 1.0 - sub
        exc[i] += a
        t += max(0.3, 1.0 + jitter * r.standard_normal()) / max(20.0, float(f[i]))
        k += 1
    # Stimmlippen-Puls (−12 dB/Okt. über dem Grundton) und Abstrahlung am Maul (+6 dB/Okt.)
    src = filt(exc, "low", float(np.median(f)) * 1.5, SR, 2)
    src = norm(np.diff(src, prepend=0.0))
    y = formant_filter(src, SR, tract(vowel, size)) + dry * src
    if breath > 0:
        pe = filt(np.abs(exc), "low", 40.0, SR, 1)
        env = 0.5 * amp / (np.max(amp) + 1e-12) + 0.5 * pe / (np.max(pe) + 1e-12)
        turb = filt(r.standard_normal(n) * env, "band", [250, 5000], SR, 2)
        turb = formant_filter(turb, SR, tract(vowel, size, 2.0))
        y = y + breath * norm(turb, float(np.max(np.abs(y))))
    y = filt(y, "low", bright, SR, 2)
    return norm(y, float(np.max(amp)))


def tone_voice(f: np.ndarray, amp: np.ndarray, seed: int, harmonics=((1, 1.0), (2, 0.3), (3, 0.12), (4, 0.05)),
               vib_depth=0.0, vib_rate: float = 5.5, vowel: str | None = None, wet: float = 0.5, breath: float = 0.0,
               chorus: float = 0.0) -> np.ndarray:
    """Eine reine, singende Stimme (Heulen, Winseln, Klagen): wenige Obertöne, Vibrato, leichtes Schwanken."""
    n = len(f)
    r = rng(seed)
    t = np.arange(n) / SR
    depth = np.asarray(vib_depth) if np.ndim(vib_depth) else np.full(n, float(vib_depth))
    drift = smooth_env(n, r.uniform(-1.0, 1.0, max(2, int(n / SR * 3)))) * 0.004
    fv = f * (1.0 + depth * np.sin(2 * np.pi * vib_rate * t + r.uniform(0, 2 * np.pi)) + drift)
    y = tone(fv, amp, SR, harmonics=harmonics)
    if chorus:
        y = y + chorus * tone(fv * 1.006, amp, SR, harmonics=harmonics, phase0=1.3)
    if vowel:
        w = formant_filter(y, SR, tract(vowel))
        y = (1.0 - wet) * y + wet * norm(w, float(np.max(np.abs(y))))
    if breath:
        b = filt(r.standard_normal(n), "band", [600, 4500], SR, 2) * amp
        y = y + breath * norm(b, float(np.max(np.abs(y))))
    return norm(y, float(np.max(amp)))


# =====================================================================================
# Geräusche
# =====================================================================================

def swept(dur: float, centers, width: float, amps, seed: int, tilt_db: float = 0.0, lo: float | None = None,
          hi: float | None = None, flicker: float = 0.0, flicker_rate=(14.0, 26.0)) -> np.ndarray:
    """Rauschen in einem Band (Breite in Oktaven), dessen Mitte über die Zeit gleitet; flicker = flackernde Lautstärke."""
    n = frames_len(dur)
    total = n / SR
    xs = [p[0] for p in centers]
    cs = [p[1] for p in centers]

    def gain(t, f):
        c = np.interp(t[:, 0] / total, xs, cs)[:, None]
        g = band(f, c, width)
        if tilt_db:
            g = g * tilt(f, tilt_db)
        if lo:
            g = g * highpass_gain(f, lo, 2)
        if hi:
            g = g * lowpass_gain(f, hi, 2)
        return g

    y = shaped_noise(n, SR, gain, seed)
    env = curve(n, amps)
    if flicker > 0:
        r = rng(seed + 77)
        env = env * smooth_env(n, r.uniform(1.0 - flicker, 1.0, max(4, int(total * r.uniform(*flicker_rate)))))
    return norm(y * env, max(p[1] for p in amps))


def whisper(dur: float, seed: int, amps, rate: float = 5.5, shift: float = 1.3) -> np.ndarray:
    """Unverständliches Flüstern: gehauchtes Rauschen durch wandernde Vokale, dazwischen Zischlaute, in Silben."""
    n = frames_len(dur)
    total = n / SR
    r = rng(seed)
    k = max(4, int(total * rate) + 2)
    times = np.linspace(0.0, total, k)
    keys = list(VOWELS)
    vs = [VOWELS[keys[int(r.integers(len(keys)))]] for _ in range(k)]
    f1 = np.array([v[0][0] for v in vs]) * shift * r.uniform(0.92, 1.08, k)
    f2 = np.array([v[1][0] for v in vs]) * shift * r.uniform(0.92, 1.08, k)
    f3 = np.array([v[2][0] for v in vs]) * shift
    fric = (r.uniform(0, 1, k) > 0.7) * r.uniform(0.5, 1.0, k)
    loud = r.uniform(0.35, 1.0, k)
    ph = r.uniform(0, np.pi)

    def gain(t, f):
        tt = t[:, 0]
        c1 = np.interp(tt, times, f1)[:, None]
        c2 = np.interp(tt, times, f2)[:, None]
        c3 = np.interp(tt, times, f3)[:, None]
        fr = np.interp(tt, times, fric)[:, None]
        syl = (0.2 + 0.8 * np.sin(np.pi * rate * tt + ph) ** 2)[:, None]
        ld = np.interp(tt, times, loud)[:, None] * syl
        voc = band(f, c1, 0.22) + 0.75 * band(f, c2, 0.2) + 0.35 * band(f, c3, 0.18)
        s = 0.45 * band(f, 5000, 0.25) + 0.3 * band(f, 3200, 0.3)
        return (voc * (1.0 - 0.7 * fr) + s * fr) * ld * highpass_gain(f, 300, 2)

    y = shaped_noise(n, SR, gain, seed) * curve(n, amps)
    return norm(y, max(p[1] for p in amps))


def rumble(dur: float, seed: int, hi: float, amps) -> np.ndarray:
    """Tiefes Grollen und Wogen (braunes Rauschen)."""
    n = int(dur * SR)
    return norm(colored_noise(n, SR, seed, -6.0, lo=25.0, hi=hi) * curve(n, amps))


def bubble(f0: float, cycles: float, rise: float) -> np.ndarray:
    """Eine Blase: ein kurzer, nach oben gleitender Ton, der in wenigen Schwingungen verklingt."""
    d = cycles / f0
    m = max(16, int(d * SR))
    t = np.arange(m) / SR
    f = f0 * (1.0 + rise * t / d)
    a = np.exp(-t / (d / 4.5)) * np.minimum(1.0, t / 0.0012)
    return np.sin(2 * np.pi * np.cumsum(f) / SR) * a


def bubbles(dur: float, seed: int, rate: float, f_lo: float, f_hi: float, cycles=(8.0, 18.0), rise=(0.3, 0.9), env=None) -> np.ndarray:
    """Viele Blasen (Poisson-verteilt); große Blasen klingen tiefer und länger."""
    n = int(dur * SR)
    r = rng(seed)
    y = np.zeros(n)
    for _ in range(r.poisson(rate * dur)):
        at = r.uniform(0.0, dur)
        f0 = float(np.exp(r.uniform(np.log(f_lo), np.log(f_hi))))
        g = r.uniform(0.3, 1.0) * (at_curve(env, at / dur) if env else 1.0)
        mix_at(y, bubble(f0, r.uniform(*cycles), r.uniform(*rise)), at, g)
    return norm(y)


def lava_bubbles(dur: float, seed: int, rate: float, env=None) -> np.ndarray:
    """Zähe Lava: tiefe, dicke Blasen ('blobb'), die mit einem leisen Zischen platzen."""
    n = int(dur * SR)
    r = rng(seed)
    y = np.zeros(n)
    for _ in range(r.poisson(rate * dur)):
        at = r.uniform(0.0, dur)
        f0 = float(np.exp(r.uniform(np.log(55), np.log(150))))
        b = bubble(f0, r.uniform(4.0, 8.0), r.uniform(0.8, 1.4))
        g = r.uniform(0.4, 1.0) * (at_curve(env, at / dur) if env else 1.0)
        mix_at(y, b, at, g)
        k = int(0.04 * SR)
        pop = filt(r.standard_normal(k) * np.exp(-np.arange(k) / SR / 0.008), "band", [1100, 3200], SR, 2)
        mix_at(y, norm(pop), at + len(b) / SR * 0.85, g * 0.18)
    return norm(y)


def crackle(dur: float, seed: int, rate: float, lo: float = 1800.0, hi: float = 6000.0, env=None) -> np.ndarray:
    """Knistern: viele winzige Knackser, wenige laute, viele leise."""
    n = int(dur * SR)
    r = rng(seed)
    y = np.zeros(n)
    for _ in range(r.poisson(rate * dur)):
        at = r.uniform(0.0, dur)
        k = max(8, int(r.uniform(0.0006, 0.0025) * SR))
        burst = r.standard_normal(k) * np.exp(-np.arange(k) / (k / 3.0))
        g = min(3.0, r.pareto(2.2) + 0.15) * (at_curve(env, at / dur) if env else 1.0)
        i = int(at * SR)
        m = min(k, n - i)
        y[i:i + m] += burst[:m] * g
    return norm(filt(y, "band", [lo, hi], SR, 2))


def clack(modes, decay: float, seed: int, q: float = 12.0) -> np.ndarray:
    """Ein harter Schlag (Zähne, Knochen, Stein): ein Anstoß, der einige Eigenfrequenzen klingen lässt."""
    m = int(decay * 7 * SR)
    r = rng(seed)
    exc = np.zeros(m)
    exc[0] = 1.0
    k = min(m, int(0.002 * SR))
    exc[:k] += r.standard_normal(k) * 0.4 * np.exp(-np.arange(k) / (k / 4))
    y = np.zeros(m)
    for f, g in modes:
        y += g * resonator(exc, f * r.uniform(0.97, 1.03), q, SR)
    return norm(filt(y * np.exp(-np.arange(m) / SR / decay), "low", 7000.0, SR, 2))


def thud(dur: float, f0: float, f1: float, seed: int, noise_hi: float = 300.0, grit: float = 0.6) -> np.ndarray:
    """Ein dumpfer Aufprall: ein tiefer, fallender Ton und dumpfes Rauschen."""
    m = int(dur * SR)
    t = np.arange(m) / SR
    body = tone(glide(m, f0, f1, 0.5), np.exp(-t / (dur / 5)) * np.minimum(1.0, t / 0.002), SR, harmonics=((1, 1.0), (2, 0.2)))
    nz = norm(filt(rng(seed).standard_normal(m), "low", noise_hi, SR, 2) * np.exp(-t / (dur / 9)))
    return norm(body + grit * nz)


def creak(dur: float, rates, seed: int, modes=WOOD, amps=None) -> np.ndarray:
    """Knarren: Holz rutscht ruckweise (Ruckgleiten) – Anstöße in wechselndem Takt durch die Eigenfrequenzen des Holzes."""
    n = int(dur * SR)
    r = rng(seed)
    rate = curve(n, rates)
    exc = np.zeros(n)
    t = r.uniform(0.0, 0.005)
    while t < dur - 1e-4:
        i = int(t * SR)
        exc[i] = r.uniform(0.4, 1.0)
        t += r.uniform(0.8, 1.2) / max(5.0, float(rate[i]))
    exc = filt(exc, "low", 2500.0, SR, 2)
    y = np.zeros(n)
    for f, q, g in modes:
        y += g * resonator(exc, f * r.uniform(0.95, 1.05), q, SR)
    return norm(filt(y, "low", 4000.0, SR, 2) * curve(n, amps or [(0, 0), (0.1, 1), (0.8, 0.8), (1, 0)]))


def squelch(dur: float, seed: int, hi: float = 1500.0, lo: float = 330.0, width: float = 0.42) -> np.ndarray:
    """Schmatzen im Schlamm: ein enges Band, das rasch nach unten gleitet, dazu ein paar kleine Blasen."""
    r = rng(seed)
    y = swept(dur, [(0, hi * r.uniform(0.85, 1.15)), (0.3, hi * 0.5), (1, lo)], width, [(0, 0), (0.05, 1), (0.35, 0.6), (1, 0)], seed)
    b = bubbles(dur, seed + 1, 5.0 / dur, 260, 900, cycles=(5, 10))
    out = np.zeros(max(len(y), len(b)))
    out[:len(y)] += y
    out[:len(b)] += 0.35 * b
    return out


# =====================================================================================
# Werwolf
# =====================================================================================

def werewolf_growl(seed: int) -> np.ndarray:
    """Ein tiefes, rollendes Knurren aus der Brust, das in zwei, drei Atemzügen an- und abschwillt."""
    r = rng(seed)
    n = int(r.uniform(1.3, 1.7) * SR)
    u = np.linspace(0.0, 1.0, n)
    f = r.uniform(58, 68) * curve(n, [(0, 0.9), (0.3, 1.06), (0.65, 1.0), (1, 0.88)]) * (1 + 0.04 * smooth_env(n, r.uniform(-1, 1, 8)))
    swells = int(r.integers(2, 4))
    amp = curve(n, [(0, 0), (0.07, 1), (0.85, 0.85), (1, 0)]) * (0.55 + 0.45 * np.sin(np.pi * swells * u + 0.3) ** 2)
    return pulse_voice(f, amp, seed, vowel="r", size=0.72, jitter=0.12, shimmer=0.35, sub=0.35, breath=0.4, bright=2800)


def werewolf_howl(seed: int) -> np.ndarray:
    """Das Heulen: setzt rau und tief an, steigt in einem langen Bogen, hält zitternd und sinkt – tiefer als ein Wolf."""
    r = rng(seed)
    n = int(3.6 * SR)
    t = np.arange(n) / SR
    k = r.uniform(0.94, 1.06)
    f = curve(n, [(0, 190), (0.07, 230), (0.22, 400), (0.34, 440), (0.7, 455), (0.8, 430), (0.9, 350), (1, 270)]) * k
    amp = curve(n, [(0, 0), (0.05, 0.5), (0.22, 1.0), (0.78, 0.95), (0.92, 0.45), (1, 0)])
    y = tone_voice(f, amp, seed, harmonics=((1, 1.0), (2, 0.38), (3, 0.16), (4, 0.07), (5, 0.03)),
                   vib_depth=0.014 * smoothstep(t, 0.9, 1.6), vib_rate=5.2, vowel="u", wet=0.45, breath=0.05, chorus=0.25)
    # rau am Anfang und Ende: halbe Frequenz moduliert (Unterton des großen Brustkorbs)
    rough = curve(n, [(0, 0.5), (0.15, 0.1), (0.8, 0.05), (1, 0.4)])
    y = y * (1.0 + rough * np.sin(np.pi * np.cumsum(f) / SR))
    return y + 0.18 * tone(f * 0.5, amp, SR, harmonics=((1, 1.0), (2, 0.2)))


def werewolf_snarl(seed: int) -> np.ndarray:
    """Fauchen mit gefletschten Zähnen: ein kurzer, wütender, rauer Stoß, scharfe Luft zwischen den Zähnen."""
    r = rng(seed)
    n = int(0.8 * SR)
    f = r.uniform(95, 112) * curve(n, [(0, 0.85), (0.12, 1.12), (0.5, 1.0), (1, 0.8)])
    amp = curve(n, [(0, 0), (0.04, 1), (0.45, 0.85), (1, 0)])
    y = pulse_voice(f, amp, seed, vowel="a", size=0.75, jitter=0.18, shimmer=0.4, sub=0.4, breath=0.65, bright=4500)
    mix_at(y, swept(0.8, [(0, 3600), (1, 3000)], 0.4, [(0, 0), (0.05, 1), (0.35, 0.35), (1, 0)], seed + 3), 0.0, 0.15)
    return y


def werewolf_hurt(seed: int) -> np.ndarray:
    """Schmerz: ein kurzes Aufjaulen, das sofort in ein Knurren kippt."""
    r = rng(seed)
    y = np.zeros(int(0.7 * SR))
    m = int(0.26 * SR)
    yelp = tone_voice(glide(m, r.uniform(680, 760), 420, 0.7), curve(m, [(0, 0), (0.06, 1), (0.5, 0.7), (1, 0)]), seed,
                      harmonics=((1, 1.0), (2, 0.5), (3, 0.3), (4, 0.15), (5, 0.08)), vowel="a", wet=0.6, breath=0.08)
    mix_at(y, yelp, 0.0, 1.0)
    k = int(0.45 * SR)
    growl = pulse_voice(r.uniform(80, 92) * curve(k, [(0, 1.1), (1, 0.85)]), curve(k, [(0, 0), (0.15, 1), (1, 0)]), seed + 5,
                        vowel="r", size=0.75, jitter=0.15, shimmer=0.35, sub=0.3, breath=0.4, bright=3000)
    mix_at(y, growl, 0.17, 0.65)
    return y


def werewolf_death(seed: int) -> np.ndarray:
    """Tod: ein langes, absinkendes Winseln, ein letztes rasselndes Ausatmen, der schwere Körper schlägt auf."""
    y = np.zeros(int(2.6 * SR))
    m = int(1.3 * SR)
    t = np.arange(m) / SR
    whine = tone_voice(glide(m, 600, 250, 1.1), curve(m, [(0, 0), (0.08, 1), (0.6, 0.7), (1, 0)]), seed,
                       harmonics=((1, 1.0), (2, 0.3), (3, 0.12)), vib_depth=0.02 * smoothstep(t, 0.3, 1.0), vib_rate=6.5,
                       vowel="u", wet=0.5, breath=0.1)
    mix_at(y, whine, 0.0)
    k = int(0.8 * SR)
    rattle = pulse_voice(46.0 * curve(k, [(0, 1.1), (1, 0.8)]), curve(k, [(0, 0), (0.1, 1), (0.6, 0.5), (1, 0)]), seed + 3,
                         vowel="o", size=0.7, jitter=0.25, shimmer=0.5, sub=0.4, breath=0.9, bright=2200)
    mix_at(y, rattle, 1.1, 0.55)
    mix_at(y, thud(0.6, 85, 38, seed + 7, 320), 1.75, 0.9)
    mix_at(y, swept(0.5, [(0, 900), (1, 500)], 0.9, [(0, 0), (0.05, 1), (1, 0)], seed + 8), 1.76, 0.15)
    return y


def werewolf_step(seed: int) -> np.ndarray:
    """Ein schwerer, weicher Pfotentritt, raschelndes Laub, ein leises Krallenklicken."""
    r = rng(seed)
    y = np.zeros(int(0.3 * SR))
    mix_at(y, thud(0.2, r.uniform(70, 85), 45, seed, 260), 0.0, 1.0)
    mix_at(y, swept(0.12, [(0, 2400), (1, 1800)], 0.7, [(0, 0), (0.05, 1), (1, 0)], seed + 1), 0.0, 0.07)
    mix_at(y, clack([(2300, 1.0), (3900, 0.6)], 0.008, seed + 2, q=20), r.uniform(0.01, 0.03), 0.06)
    return y


# =====================================================================================
# Moorschlurfer
# =====================================================================================

def mire_ambient(seed: int) -> np.ndarray:
    """Ein nasses, gurgelndes Stöhnen aus einem großen, schlammgefüllten Leib, dazu Blubbern und Schwappen."""
    r = rng(seed)
    dur = 2.4
    n = int(dur * SR)
    f = r.uniform(48, 56) * curve(n, [(0, 0.95), (0.4, 1.08), (1, 0.9)])
    amp = curve(n, [(0, 0), (0.18, 0.9), (0.7, 0.75), (1, 0)])
    y = pulse_voice(f, amp, seed, vowel="o", size=0.62, jitter=0.08, shimmer=0.25, sub=0.25, breath=0.35, bright=1300)
    mix_at(y, bubbles(dur, seed + 2, 7, 120, 420, env=[(0, 0.4), (0.5, 1), (1, 0.5)]), 0.0, 0.3)
    mix_at(y, swept(dur, [(0, 520), (0.5, 380), (1, 450)], 0.6, [(0, 0), (0.3, 1), (1, 0)], seed + 3, flicker=0.6,
                    flicker_rate=(4, 8)), 0.0, 0.12)
    return y


def mire_hurt(seed: int) -> np.ndarray:
    r = rng(seed)
    y = np.zeros(int(0.75 * SR))
    mix_at(y, squelch(0.38, seed), 0.0, 0.8)
    m = int(0.5 * SR)
    moan = pulse_voice(glide(m, r.uniform(125, 140), 82, 0.8), curve(m, [(0, 0), (0.1, 1), (1, 0)]), seed + 2, vowel="o",
                       size=0.68, jitter=0.12, shimmer=0.3, sub=0.3, breath=0.4, bright=1700)
    mix_at(y, moan, 0.03, 1.0)
    return y


def mire_death(seed: int) -> np.ndarray:
    """Er zerfällt: ein langes, absinkendes Stöhnen, der Schlamm sackt schmatzend zusammen, Blasen steigen auf."""
    y = np.zeros(int(3.0 * SR))
    m = int(1.8 * SR)
    moan = pulse_voice(glide(m, 100, 42, 1.0), curve(m, [(0, 0), (0.08, 1), (0.7, 0.6), (1, 0)]), seed, vowel="o", size=0.62,
                       jitter=0.15, shimmer=0.35, sub=0.35, breath=0.5, bright=1300)
    mix_at(y, moan, 0.0)
    for k in range(4):
        mix_at(y, squelch(0.45, seed + 3 + k, hi=1300 - k * 150), 0.5 + k * 0.33, 0.55)
    mix_at(y, thud(0.7, 70, 35, seed + 9, 220), 1.55, 0.6)
    mix_at(y, bubbles(1.6, seed + 10, 12, 140, 520, env=[(0, 1), (1, 0.2)]), 1.2, 0.3)
    return y


def mire_step(seed: int) -> np.ndarray:
    r = rng(seed)
    y = np.zeros(int(0.42 * SR))
    mix_at(y, thud(0.18, r.uniform(80, 95), 50, seed, 240), 0.0, 0.7)
    mix_at(y, squelch(0.32, seed + 1, hi=r.uniform(1100, 1400), lo=300), 0.04, 0.8)
    return y


def mire_throw(seed: int) -> np.ndarray:
    """Er reißt einen Klumpen aus dem Boden (saugendes Schmatzen) und schleudert ihn (nasser Luftzug)."""
    y = np.zeros(int(0.9 * SR))
    mix_at(y, swept(0.3, [(0, 380), (0.6, 1300), (1, 900)], 0.4, [(0, 0), (0.2, 1), (0.7, 0.7), (1, 0)], seed), 0.0, 0.7)
    mix_at(y, bubbles(0.3, seed + 1, 18, 300, 900, cycles=(5, 9)), 0.05, 0.25)
    mix_at(y, swept(0.5, [(0, 380), (0.4, 1100), (1, 450)], 0.85, [(0, 0), (0.35, 1), (1, 0)], seed + 2), 0.24, 0.8)
    return y


def mire_grasp(seed: int) -> np.ndarray:
    """Wurzeln packen zu: nasses Holz knarrt und ächzt unter Zug, der Schlamm schmatzt."""
    r = rng(seed)
    y = np.zeros(int(1.2 * SR))
    for k in range(3):
        d = r.uniform(0.3, 0.5)
        c = creak(d, [(0, r.uniform(25, 40)), (0.6, r.uniform(70, 110)), (1, r.uniform(40, 60))], seed + k,
                  amps=[(0, 0), (0.15, 1), (0.8, 0.8), (1, 0)])
        mix_at(y, c, 0.05 + k * 0.28 + r.uniform(0, 0.08), 0.42)
    mix_at(y, squelch(0.4, seed + 7), 0.0, 0.6)
    return y


def mire_rise(seed: int) -> np.ndarray:
    """Er steigt aus dem Schlamm: das Blubbern schwillt an, saugendes Reißen, Schlamm rinnt ab, dann ein tiefes Stöhnen."""
    y = np.zeros(int(2.4 * SR))
    mix_at(y, bubbles(1.3, seed, 32, 120, 600, env=[(0, 0.15), (0.75, 1.0), (1, 0.5)]), 0.0, 0.5)
    for k in range(3):
        mix_at(y, squelch(0.5, seed + 2 + k, hi=1200 + 150 * k, lo=280), 0.55 + k * 0.22, 0.6)
    mix_at(y, swept(1.4, [(0, 800), (0.5, 600), (1, 450)], 0.7, [(0, 0), (0.15, 1), (0.6, 0.7), (1, 0)], seed + 6,
                    flicker=0.7, flicker_rate=(10, 16)), 0.7, 0.3)
    m = int(1.1 * SR)
    moan = pulse_voice(curve(m, [(0, 52), (0.4, 70), (1, 56)]), curve(m, [(0, 0), (0.25, 1), (0.8, 0.7), (1, 0)]), seed + 9,
                       vowel="o", size=0.62, jitter=0.1, shimmer=0.3, sub=0.3, breath=0.45, bright=1300)
    mix_at(y, moan, 1.15, 1.0)
    return y


def mire_splat(seed: int) -> np.ndarray:
    """Ein Schlammklumpen klatscht auf und spritzt."""
    y = np.zeros(int(0.55 * SR))
    mix_at(y, thud(0.15, 120, 55, seed, 400), 0.0, 0.8)
    mix_at(y, swept(0.3, [(0, 2400), (0.3, 1000), (1, 550)], 0.75, [(0, 0), (0.03, 1), (0.25, 0.35), (1, 0)], seed + 1), 0.0, 0.8)
    mix_at(y, bubbles(0.4, seed + 2, 30, 600, 1700, cycles=(4, 8), env=[(0, 1), (1, 0.2)]), 0.05, 0.25)
    return y


# =====================================================================================
# Glutwurm
# =====================================================================================

def wyrm_ambient(seed: int) -> np.ndarray:
    """Ein tiefes Grollen aus der Glut, darunter zähe Lavablasen, darüber ein leises, flackerndes Zischen."""
    r = rng(seed)
    dur = 2.8
    n = int(dur * SR)
    f = r.uniform(38, 45) * curve(n, [(0, 0.95), (0.5, 1.1), (1, 0.9)])
    amp = curve(n, [(0, 0), (0.2, 0.9), (0.75, 0.75), (1, 0)])
    y = pulse_voice(f, amp, seed, vowel="o", size=0.55, jitter=0.1, shimmer=0.3, sub=0.3, breath=0.3, bright=900)
    mix_at(y, lava_bubbles(dur, seed + 1, 3.5), 0.0, 0.45)
    mix_at(y, swept(dur, [(0, 3600), (1, 3000)], 0.6, [(0, 0), (0.3, 1), (1, 0)], seed + 2, flicker=0.8, flicker_rate=(20, 35)), 0.0, 0.05)
    return y


def wyrm_hurt(seed: int) -> np.ndarray:
    """Getroffen: ein schmerzvolles Brüllen, Dampf zischt aus den Rissen, Basalt splittert."""
    r = rng(seed)
    y = np.zeros(int(0.8 * SR))
    m = int(0.6 * SR)
    roar = pulse_voice(glide(m, r.uniform(165, 185), 105, 0.8), curve(m, [(0, 0), (0.06, 1), (0.5, 0.7), (1, 0)]), seed,
                       vowel="a", size=0.6, jitter=0.15, shimmer=0.35, sub=0.3, breath=0.5, bright=2400)
    mix_at(y, roar, 0.02, 1.0)
    mix_at(y, swept(0.5, [(0, 3300), (1, 2600)], 0.55, [(0, 0), (0.04, 1), (0.4, 0.3), (1, 0)], seed + 1), 0.0, 0.16)
    mix_at(y, clack([(620, 1.0), (1350, 0.6), (2500, 0.3)], 0.025, seed + 2, q=9), 0.0, 0.45)
    return y


def wyrm_death(seed: int) -> np.ndarray:
    """Tod: ein langes, ersterbendes Brüllen, die Glut erkaltet knisternd, ein letzter dumpfer Schlag."""
    y = np.zeros(int(3.4 * SR))
    m = int(2.1 * SR)
    roar = pulse_voice(glide(m, 150, 40, 1.0), curve(m, [(0, 0), (0.06, 1), (0.6, 0.6), (1, 0)]), seed, vowel="a", size=0.6,
                       jitter=0.18, shimmer=0.4, sub=0.35, breath=0.55, bright=2000)
    mix_at(y, roar, 0.0)
    mix_at(y, swept(2.2, [(0, 3200), (1, 2400)], 0.6, [(0, 0), (0.05, 1), (1, 0)], seed + 1, flicker=0.5), 0.1, 0.1)
    mix_at(y, crackle(2.4, seed + 2, 22, env=[(0, 0.2), (0.3, 1.0), (1, 0.0)]), 0.8, 0.12)
    mix_at(y, thud(0.9, 62, 30, seed + 3, 200), 2.15, 0.8)
    return y


def wyrm_surface(seed: int) -> np.ndarray:
    """Er bricht aus der Lava: die Glut wogt auf, schwere Spritzer und Blasen, dann ein Brüllen."""
    r = rng(seed)
    y = np.zeros(int(2.6 * SR))
    mix_at(y, rumble(1.1, seed, 380, [(0, 0), (0.12, 1), (1, 0)]), 0.0, 0.9)
    mix_at(y, swept(0.9, [(0, 500), (0.2, 1300), (1, 450)], 1.0, [(0, 0), (0.1, 1), (1, 0)], seed + 1), 0.0, 0.45)
    mix_at(y, lava_bubbles(1.4, seed + 2, 16, env=[(0, 1), (1, 0.3)]), 0.05, 0.4)
    k = int(1.5 * SR)
    f = curve(k, [(0, 68), (0.22, 112), (0.7, 98), (1, 58)]) * r.uniform(0.95, 1.05)
    roar = pulse_voice(f, curve(k, [(0, 0), (0.1, 1), (0.75, 0.8), (1, 0)]), seed + 3, vowel="a", size=0.6, jitter=0.12,
                       shimmer=0.3, sub=0.3, breath=0.5, bright=2600)
    mix_at(y, roar, 0.42, 1.0)
    mix_at(y, swept(1.4, [(0, 3400), (1, 2800)], 0.6, [(0, 0), (0.2, 1), (1, 0)], seed + 4, flicker=0.6), 0.45, 0.07)
    return y


def wyrm_spit(seed: int) -> np.ndarray:
    """Spucken: ein kehliges Gurgeln, dann ein feuriger Stoß, der knisternd verweht."""
    r = rng(seed)
    y = np.zeros(int(1.0 * SR))
    m = int(0.28 * SR)
    gurgle = pulse_voice(np.full(m, r.uniform(70, 85)), curve(m, [(0, 0), (0.3, 1), (1, 0)]), seed, vowel="o", size=0.6,
                         jitter=0.25, shimmer=0.5, sub=0.4, breath=0.6, bright=1400)
    mix_at(y, gurgle, 0.0, 0.6)
    mix_at(y, swept(0.7, [(0, 450), (0.15, 1500), (1, 650)], 1.0, [(0, 0), (0.06, 1), (0.4, 0.55), (1, 0)], seed + 1, hi=6000, tilt_db=-3.0,
                    flicker=0.35), 0.17, 0.95)
    mix_at(y, thud(0.3, 110, 55, seed + 2, 300), 0.17, 0.5)
    mix_at(y, crackle(0.7, seed + 3, 40, env=[(0, 1), (1, 0)]), 0.2, 0.1)
    return y


def wyrm_bite(seed: int) -> np.ndarray:
    """Biss: ein scharfes Einatmen, die Kiefer schlagen hart zusammen, ein kurzes Knurren."""
    r = rng(seed)
    y = np.zeros(int(0.75 * SR))
    mix_at(y, swept(0.16, [(0, 2800), (1, 3400)], 0.5, [(0, 0), (0.5, 1), (1, 0)], seed), 0.0, 0.12)
    mix_at(y, clack([(1800, 1.0), (3000, 0.7), (4300, 0.4)], 0.014, seed + 1, q=24), 0.13, 0.75)
    mix_at(y, thud(0.16, 160, 70, seed + 2, 500), 0.13, 0.7)
    k = int(0.4 * SR)
    growl = pulse_voice(r.uniform(88, 100) * curve(k, [(0, 1.1), (1, 0.85)]), curve(k, [(0, 0), (0.12, 1), (1, 0)]), seed + 3,
                        vowel="a", size=0.62, jitter=0.15, shimmer=0.35, sub=0.3, breath=0.5, bright=2200)
    mix_at(y, growl, 0.15, 0.7)
    return y


def wyrm_dive(seed: int) -> np.ndarray:
    """Abtauchen: ein schwerer Schwall, eine große Blase, die Lava schließt sich blubbernd über ihm."""
    y = np.zeros(int(1.6 * SR))
    mix_at(y, rumble(0.9, seed, 600, [(0, 0), (0.1, 1), (1, 0)]), 0.0, 0.8)
    mix_at(y, swept(0.7, [(0, 1200), (0.3, 700), (1, 350)], 0.9, [(0, 0), (0.06, 1), (1, 0)], seed + 1), 0.0, 0.4)
    mix_at(y, bubble(62, 6, 1.3), 0.2, 0.6)
    mix_at(y, lava_bubbles(1.2, seed + 2, 12, env=[(0, 1), (1, 0.1)]), 0.25, 0.4)
    return y


# =====================================================================================
# Aschengeist
# =====================================================================================

def wraith_ambient(seed: int) -> np.ndarray:
    """Flüstern im Wind, darunter ein hohler, schwebender Ton."""
    r = rng(seed)
    y = np.zeros(int(2.4 * SR))
    mix_at(y, whisper(2.3, seed, [(0, 0), (0.2, 1), (0.8, 0.8), (1, 0)], rate=r.uniform(4.5, 6.0), shift=1.3), 0.0, 1.0)
    m = int(2.3 * SR)
    t = np.arange(m) / SR
    drone = tone_voice(r.uniform(170, 210) * (1 + 0.015 * np.sin(2 * np.pi * 0.6 * t)), curve(m, [(0, 0), (0.3, 1), (0.8, 0.8), (1, 0)]),
                       seed + 1, harmonics=((1, 1.0), (2, 0.25), (3, 0.08)), vowel="o", wet=0.4, chorus=0.8)
    mix_at(y, drone, 0.0, 0.18)
    mix_at(y, swept(2.3, [(0, 600), (0.5, 1000), (1, 650)], 0.9, [(0, 0), (0.4, 1), (1, 0)], seed + 2, flicker=0.4,
                    flicker_rate=(3, 6)), 0.0, 0.25)
    return y


def wraith_hurt(seed: int) -> np.ndarray:
    """Getroffen: ein kurzer, geisterhafter Schrei, ein scharfes Einatmen, knisternde Asche."""
    r = rng(seed)
    y = np.zeros(int(0.7 * SR))
    m = int(0.45 * SR)
    shriek = tone_voice(glide(m, r.uniform(900, 1000), 600, 0.8), curve(m, [(0, 0), (0.05, 1), (0.5, 0.6), (1, 0)]), seed,
                        harmonics=((1, 1.0), (2, 0.4), (3, 0.18), (4, 0.06)), vib_depth=0.02, vib_rate=7.5, vowel="e", wet=0.5,
                        chorus=0.5)
    mix_at(y, shriek, 0.02, 0.55)
    mix_at(y, swept(0.35, [(0, 2000), (1, 1500)], 0.9, [(0, 0), (0.12, 1), (1, 0)], seed + 1), 0.0, 0.5)
    mix_at(y, crackle(0.45, seed + 2, 45, env=[(0, 1), (1, 0)]), 0.0, 0.1)
    return y


def wraith_death(seed: int) -> np.ndarray:
    """Er zerfällt: ein absinkendes Klagen, verwehendes Flüstern, ein Windstoß trägt die Asche fort."""
    y = np.zeros(int(3.0 * SR))
    m = int(1.7 * SR)
    t = np.arange(m) / SR
    wail = tone_voice(glide(m, 720, 160, 1.0), curve(m, [(0, 0), (0.06, 1), (0.6, 0.6), (1, 0)]), seed,
                      harmonics=((1, 1.0), (2, 0.3), (3, 0.15), (5, 0.05)), vib_depth=0.025 * smoothstep(t, 0.1, 0.8), vib_rate=6.0,
                      vowel="o", wet=0.5, chorus=0.6)
    mix_at(y, wail, 0.0, 0.55)
    mix_at(y, whisper(1.5, seed + 1, [(0, 1), (1, 0)], rate=7.0, shift=1.35), 0.0, 0.7)
    mix_at(y, swept(1.9, [(0, 420), (0.35, 1500), (1, 500)], 1.1, [(0, 0), (0.3, 1), (1, 0)], seed + 2), 0.8, 0.6)
    mix_at(y, crackle(2.0, seed + 3, 20, env=[(0, 1), (1, 0)]), 0.6, 0.07)
    return y


def wraith_cloud(seed: int) -> np.ndarray:
    """Die Aschewolke wallt auf: ein tiefer, weicher Stoß, der sich ausbreitet, darin Flüstern und feines Rieseln."""
    y = np.zeros(int(2.0 * SR))
    mix_at(y, swept(1.7, [(0, 140), (0.3, 420), (1, 260)], 1.3, [(0, 0), (0.12, 1), (0.5, 0.55), (1, 0)], seed), 0.0, 1.0)
    mix_at(y, rumble(0.8, seed + 1, 180, [(0, 0), (0.1, 1), (1, 0)]), 0.0, 0.5)
    mix_at(y, whisper(1.5, seed + 2, [(0, 0), (0.3, 1), (1, 0)], rate=6.5, shift=1.3), 0.2, 0.35)
    mix_at(y, swept(1.6, [(0, 6000), (1, 5000)], 0.45, [(0, 0), (0.25, 1), (1, 0)], seed + 3, flicker=0.9, flicker_rate=(30, 50)), 0.1, 0.07)
    return y


def wraith_bolt(seed: int) -> np.ndarray:
    """Seelenfeuer: eine flackernde Flamme fliegt davon, darin ein unheimlich singender Ton."""
    r = rng(seed)
    y = np.zeros(int(1.0 * SR))
    mix_at(y, swept(0.85, [(0, 700), (0.25, 1900), (1, 800)], 0.7, [(0, 0), (0.08, 1), (0.5, 0.5), (1, 0)], seed, hi=6000,
                    flicker=0.35, flicker_rate=(18, 30)), 0.0, 0.85)
    m = int(0.8 * SR)
    f0 = r.uniform(740, 820)
    sing = tone_voice(glide(m, f0, f0 * 0.66, 1.0), curve(m, [(0, 0), (0.05, 1), (0.45, 0.6), (1, 0)]), seed + 1,
                      harmonics=((1, 1.0), (2, 0.22), (3, 0.06)), vib_depth=0.025, vib_rate=9.0, chorus=0.9)
    mix_at(y, sing, 0.0, 0.32)
    mix_at(y, thud(0.25, 140, 75, seed + 2, 300), 0.0, 0.3)
    return y


def wraith_fade(seed: int) -> np.ndarray:
    """Verwehen: ein anschwellender, aufsteigender Luftzug, der in feinem Glitzern endet."""
    r = rng(seed)
    y = np.zeros(int(1.3 * SR))
    mix_at(y, swept(1.0, [(0, 350), (0.8, 2600), (1, 3000)], 1.0, [(0, 0), (0.72, 1), (0.85, 0.35), (1, 0)], seed), 0.0, 1.0)
    for _ in range(14):
        f = r.uniform(2200, 5200)
        d = r.uniform(0.08, 0.2)
        m = int(d * SR)
        t = np.arange(m) / SR
        mix_at(y, np.sin(2 * np.pi * f * t) * np.exp(-t / (d / 4)) * np.minimum(1.0, t / 0.003), r.uniform(0.6, 1.1),
               r.uniform(0.03, 0.07))
    return y


# =====================================================================================
# Seelenreiher (0.71)
# =====================================================================================

def soul_glint(dur: float, seed: int, count: int = 8, lo: float = 2400.0, hi: float = 5600.0) -> np.ndarray:
    """Feines Seelenglitzern: einzelne hohe, rasch verklingende Töne."""
    r = rng(seed)
    y = np.zeros(int(dur * SR))
    for _ in range(count):
        f = r.uniform(lo, hi)
        d = r.uniform(0.06, 0.18)
        m = int(d * SR)
        t = np.arange(m) / SR
        mix_at(y, np.sin(2 * np.pi * f * t) * np.exp(-t / (d / 4)) * np.minimum(1.0, t / 0.003), r.uniform(0.0, dur * 0.8),
               r.uniform(0.3, 1.0))
    return norm(y)


def heron_call(seed: int) -> np.ndarray:
    """Der Ruf: ein heiseres, lautes "Kraaak" wie beim Graureiher – manchmal zweimal –, dahinter ein Hauch Seelenglitzern."""
    r = rng(seed)
    y = np.zeros(int(1.3 * SR))
    for k in range(1 + int(r.integers(0, 2))):
        n = int(r.uniform(0.38, 0.52) * SR)
        f = r.uniform(250, 300) * curve(n, [(0, 0.9), (0.15, 1.08), (0.7, 1.0), (1, 0.82)])
        amp = curve(n, [(0, 0), (0.05, 1), (0.6, 0.85), (1, 0)])
        mix_at(y, pulse_voice(f, amp, seed + k, vowel="a", size=0.95, jitter=0.16, shimmer=0.45, sub=0.45, breath=0.35, bright=4200),
               0.05 + k * r.uniform(0.5, 0.62), 1.0)
    mix_at(y, soul_glint(1.0, seed + 9, 6), 0.1, 0.06)
    return y


def heron_stab(seed: int) -> np.ndarray:
    """Der Stoß: ein scharfes Zischen durch die Luft und das harte Klacken des Schnabels."""
    r = rng(seed)
    y = np.zeros(int(0.45 * SR))
    mix_at(y, swept(0.16, [(0, 1600), (1, 3800)], 0.6, [(0, 0), (0.6, 1), (1, 0)], seed), 0.0, 0.7)
    mix_at(y, clack([(1900, 1.0), (3100, 0.6), (4700, 0.3)], 0.02, seed + 1, q=16), 0.13 + r.uniform(0, 0.02), 0.9)
    return y


def heron_wings(seed: int) -> np.ndarray:
    """Schwingenschläge: zwei, drei tiefe, weiche Luftstöße, in denen es leise flattert."""
    r = rng(seed)
    y = np.zeros(int(1.0 * SR))
    beats = 2 + int(r.integers(0, 2))
    for k in range(beats):
        mix_at(y, swept(0.28, [(0, 260), (0.5, 700), (1, 380)], 1.1, [(0, 0), (0.35, 1), (1, 0)], seed + k, flicker=0.45,
                        flicker_rate=(30.0, 45.0)), k * r.uniform(0.26, 0.32), 1.0 - k * 0.15)
    return y


def heron_hurt(seed: int) -> np.ndarray:
    """Schmerz: ein schrilles, kurzes Kreischen."""
    r = rng(seed)
    n = int(0.32 * SR)
    f = r.uniform(560, 640) * curve(n, [(0, 1.0), (0.2, 1.15), (1, 0.7)])
    return pulse_voice(f, curve(n, [(0, 0), (0.04, 1), (0.5, 0.7), (1, 0)]), seed, vowel="e", size=1.1, jitter=0.12, shimmer=0.4,
                       sub=0.3, breath=0.3, bright=5200)


def heron_death(seed: int) -> np.ndarray:
    """Tod: ein langer, absinkender Ruf, ein letztes Flattern, ein Platschen."""
    y = np.zeros(int(2.4 * SR))
    n = int(1.2 * SR)
    f = 290.0 * curve(n, [(0, 1.0), (0.2, 1.05), (1, 0.55)])
    mix_at(y, pulse_voice(f, curve(n, [(0, 0), (0.06, 1), (0.7, 0.6), (1, 0)]), seed, vowel="a", size=0.95, jitter=0.2, shimmer=0.5,
                          sub=0.5, breath=0.45, bright=3800), 0.0, 1.0)
    mix_at(y, heron_wings(seed + 3), 0.9, 0.5)
    mix_at(y, squelch(0.4, seed + 5, hi=1100, lo=260), 1.7, 0.6)
    mix_at(y, soul_glint(0.9, seed + 7, 10), 1.4, 0.08)
    return y


def heron_step(seed: int) -> np.ndarray:
    """Waten: ein leises Platschen, kleine Tropfen."""
    r = rng(seed)
    y = np.zeros(int(0.35 * SR))
    mix_at(y, swept(0.18, [(0, r.uniform(1300, 1700)), (1, 700)], 0.5, [(0, 0), (0.08, 1), (1, 0)], seed), 0.0, 0.8)
    mix_at(y, bubbles(0.3, seed + 1, 18.0, 500, 1400, cycles=(4, 8)), 0.02, 0.3)
    return y


# =====================================================================================
# Nachtwidder (0.71)
# =====================================================================================

def crystal_pings(dur: float, seed: int, count: int = 10) -> np.ndarray:
    """Nachtkristall: klingende Splitter (zwei Eigentöne je Splitter, lang ausklingend)."""
    r = rng(seed)
    y = np.zeros(int(dur * SR))
    for _ in range(count):
        f = r.uniform(1800, 4800)
        d = r.uniform(0.15, 0.45)
        m = int(d * SR)
        t = np.arange(m) / SR
        tone_ = np.sin(2 * np.pi * f * t) + 0.45 * np.sin(2 * np.pi * f * 2.76 * t)
        mix_at(y, tone_ * np.exp(-t / (d / 5)) * np.minimum(1.0, t / 0.001), r.uniform(0.0, dur * 0.5), r.uniform(0.3, 1.0))
    return norm(y)


def ram_ambient(seed: int) -> np.ndarray:
    """Ein tiefes, schnaubendes Brummen."""
    r = rng(seed)
    n = int(r.uniform(0.6, 0.8) * SR)
    f = r.uniform(82, 96) * curve(n, [(0, 0.95), (0.3, 1.05), (1, 0.85)])
    y = pulse_voice(f, curve(n, [(0, 0), (0.1, 1), (0.7, 0.8), (1, 0)]), seed, vowel="o", size=0.75, jitter=0.1, shimmer=0.3,
                    sub=0.3, breath=0.5, bright=2600)
    mix_at(y, swept(0.3, [(0, 900), (1, 600)], 0.8, [(0, 0), (0.1, 1), (1, 0)], seed + 2), len(y) / SR * 0.6, 0.2)
    return y


def ram_bleat(seed: int) -> np.ndarray:
    """Das Röhren: ein tiefes, bebendes Blöken, das in ein Brüllen übergeht (Tremolo wie bei Schafen, aber gewaltig)."""
    r = rng(seed)
    n = int(1.15 * SR)
    t = np.arange(n) / SR
    f = r.uniform(125, 140) * curve(n, [(0, 0.9), (0.2, 1.15), (0.6, 1.1), (1, 0.8)])
    trem = 1.0 + 0.35 * np.sin(2 * np.pi * r.uniform(10, 13) * t) * curve(n, [(0, 1), (0.6, 0.6), (1, 0.2)])
    amp = curve(n, [(0, 0), (0.06, 1), (0.75, 0.9), (1, 0)]) * trem
    y = pulse_voice(f, amp, seed, vowel="e", size=0.8, jitter=0.08, shimmer=0.25, sub=0.35, breath=0.35, bright=3600)
    y = y + 0.4 * pulse_voice(f * 0.5, amp, seed + 1, vowel="a", size=0.7, jitter=0.15, shimmer=0.35, sub=0.4, breath=0.2, bright=2000)
    return y


def ram_scrape(seed: int) -> np.ndarray:
    """Scharren: der Huf reißt über Stein und Frost."""
    r = rng(seed)
    y = np.zeros(int(0.45 * SR))
    mix_at(y, swept(0.35, [(0, 2600), (1, 1500)], 0.8, [(0, 0), (0.1, 1), (0.6, 0.7), (1, 0)], seed, flicker=0.6), 0.0, 0.8)
    mix_at(y, crackle(0.35, seed + 1, 140.0, 1500, 6000), 0.02, 0.5)
    mix_at(y, thud(0.15, r.uniform(110, 130), 70, seed + 2, 400), 0.0, 0.4)
    return y


def ram_charge(seed: int) -> np.ndarray:
    """Der Ansturm: ein wütendes Schnauben, dann donnernde Hufe."""
    r = rng(seed)
    y = np.zeros(int(1.3 * SR))
    mix_at(y, swept(0.3, [(0, 1800), (1, 700)], 1.0, [(0, 0), (0.08, 1), (1, 0)], seed), 0.0, 0.8)
    t = 0.32
    for k in range(9):
        mix_at(y, thud(0.18, r.uniform(65, 85), 40, seed + 3 + k, 380, grit=0.8), t, 0.9 - k * 0.04)
        t += r.uniform(0.09, 0.12)
    return y


def ram_impact(seed: int) -> np.ndarray:
    """Aufprall: ein schwerer Schlag, die Kristallhörner klirren nach, Splitter rieseln."""
    y = np.zeros(int(1.4 * SR))
    mix_at(y, thud(0.6, 95, 38, seed, 420, grit=0.9), 0.0, 1.0)
    mix_at(y, clack([(2300, 1.0), (3550, 0.7), (5200, 0.4)], 0.18, seed + 1, q=40), 0.0, 0.5)
    mix_at(y, crystal_pings(1.0, seed + 2, 7), 0.05, 0.25)
    mix_at(y, crackle(0.6, seed + 3, 90.0, 1200, 5000), 0.05, 0.3)
    return y


def ram_stomp(seed: int) -> np.ndarray:
    """Stampfen: zwei Hufe schlagen nieder, der Boden birst, ein Ring aus Kristallsplittern klirrt."""
    y = np.zeros(int(1.6 * SR))
    mix_at(y, thud(0.5, 80, 34, seed, 380, grit=1.0), 0.0, 1.0)
    mix_at(y, thud(0.5, 74, 32, seed + 1, 380, grit=1.0), 0.07, 0.9)
    mix_at(y, crackle(0.8, seed + 2, 260.0, 900, 6000), 0.05, 0.55)
    mix_at(y, crystal_pings(1.2, seed + 3, 16), 0.08, 0.45)
    return y


def ram_hurt(seed: int) -> np.ndarray:
    """Schmerz: ein kurzes, gequetschtes Blöken."""
    r = rng(seed)
    n = int(0.38 * SR)
    t = np.arange(n) / SR
    f = r.uniform(190, 220) * curve(n, [(0, 1.1), (1, 0.8)])
    amp = curve(n, [(0, 0), (0.05, 1), (1, 0)]) * (1.0 + 0.3 * np.sin(2 * np.pi * 14 * t))
    return pulse_voice(f, amp, seed, vowel="e", size=0.85, jitter=0.12, shimmer=0.35, sub=0.35, breath=0.3, bright=3800)


def ram_death(seed: int) -> np.ndarray:
    """Tod: ein langes, absinkendes Blöken, ein schwerer Fall, die Hörner klirren ein letztes Mal."""
    y = np.zeros(int(2.4 * SR))
    n = int(1.4 * SR)
    t = np.arange(n) / SR
    f = 150.0 * curve(n, [(0, 1.0), (0.3, 1.05), (1, 0.6)])
    amp = curve(n, [(0, 0), (0.06, 1), (0.7, 0.6), (1, 0)]) * (1.0 + 0.3 * np.sin(2 * np.pi * 9 * t))
    mix_at(y, pulse_voice(f, amp, seed, vowel="e", size=0.8, jitter=0.15, shimmer=0.4, sub=0.45, breath=0.45, bright=3000), 0.0)
    mix_at(y, thud(0.7, 70, 30, seed + 3, 360, grit=0.9), 1.45, 1.0)
    mix_at(y, crystal_pings(0.8, seed + 4, 5), 1.5, 0.2)
    return y


def ram_step(seed: int) -> np.ndarray:
    """Ein Hufschlag auf Stein."""
    r = rng(seed)
    y = np.zeros(int(0.25 * SR))
    mix_at(y, clack([(r.uniform(850, 1000), 1.0), (1750, 0.5), (2900, 0.25)], 0.03, seed, q=10), 0.0, 0.6)
    mix_at(y, thud(0.15, r.uniform(90, 110), 60, seed + 1, 320), 0.0, 0.5)
    return y


# =====================================================================================
# Rissjäger (0.71)
# =====================================================================================

def ring_mod(y: np.ndarray, f: float, depth: float) -> np.ndarray:
    t = np.arange(len(y)) / SR
    return y * (1.0 - depth + depth * np.sin(2 * np.pi * f * t))


def hound_ambient(seed: int) -> np.ndarray:
    """Hohles Knurren, als käme es aus großer Entfernung durch die Leere: tief, dazu ein schwebender Oberton."""
    r = rng(seed)
    n = int(r.uniform(1.0, 1.3) * SR)
    f = r.uniform(68, 78) * curve(n, [(0, 0.95), (0.4, 1.05), (1, 0.9)])
    amp = curve(n, [(0, 0), (0.12, 1), (0.8, 0.8), (1, 0)])
    y = pulse_voice(f, amp, seed, vowel="u", size=0.7, jitter=0.1, shimmer=0.3, sub=0.4, breath=0.3, bright=2400)
    y = ring_mod(y, r.uniform(28, 36), 0.35)
    mix_at(y, tone_voice(f * 6.0, amp * 0.3, seed + 1, harmonics=((1, 1.0), (2, 0.2)), vib_depth=0.01, vib_rate=3.0), 0.0, 0.08)
    return y


def hound_howl(seed: int) -> np.ndarray:
    """Das Heulen der Leere: zwei Stimmen im Abstand einer Quinte, gegeneinander verstimmt, mit tiefem Schwebeton."""
    r = rng(seed)
    n = int(2.8 * SR)
    t = np.arange(n) / SR
    k = r.uniform(0.95, 1.05)
    f = curve(n, [(0, 260), (0.15, 380), (0.3, 520), (0.7, 500), (0.85, 420), (1, 330)]) * k
    amp = curve(n, [(0, 0), (0.1, 0.6), (0.3, 1.0), (0.8, 0.9), (1, 0)])
    vib = 0.02 * smoothstep(t, 0.6, 1.3)
    y = tone_voice(f, amp, seed, harmonics=((1, 1.0), (2, 0.3), (3, 0.12)), vib_depth=vib, vib_rate=4.6, vowel="u", wet=0.4, chorus=0.4)
    y = y + 0.55 * tone_voice(f * 1.498, amp * 0.8, seed + 1, harmonics=((1, 1.0), (2, 0.2)), vib_depth=vib * 1.3, vib_rate=4.1,
                              vowel="o", wet=0.4)
    y = y + 0.25 * tone(f * 0.25, amp, SR, harmonics=((1, 1.0), (2, 0.3)))
    return y


def hound_phase(seed: int) -> np.ndarray:
    """Phasensprung: ein rückwärts anschwellendes Rauschen, das jäh abreißt, darunter ein Absacken, dahinter Glitzern."""
    y = np.zeros(int(1.0 * SR))
    mix_at(y, swept(0.5, [(0, 400), (1, 3000)], 1.2, [(0, 0), (0.9, 1), (1, 0)], seed), 0.0, 1.0)
    m = int(0.5 * SR)
    mix_at(y, tone(glide(m, 120, 45, 0.8), curve(m, [(0, 0), (0.1, 1), (1, 0)]), SR, harmonics=((1, 1.0), (2, 0.25))), 0.42, 0.7)
    mix_at(y, soul_glint(0.5, seed + 2, 12, 2600, 6400), 0.45, 0.25)
    return y


def hound_bite(seed: int) -> np.ndarray:
    """Biss: Zähne schlagen aufeinander, ein kurzes Fauchen."""
    r = rng(seed)
    y = np.zeros(int(0.5 * SR))
    mix_at(y, clack([(1200, 1.0), (2500, 0.7), (3800, 0.4)], 0.02, seed, q=14), 0.0, 1.0)
    n = int(0.3 * SR)
    snarl = pulse_voice(r.uniform(110, 130) * curve(n, [(0, 1.1), (1, 0.8)]), curve(n, [(0, 0), (0.08, 1), (1, 0)]), seed + 1,
                        vowel="a", size=0.8, jitter=0.18, shimmer=0.4, sub=0.4, breath=0.6, bright=4200)
    mix_at(y, ring_mod(snarl, 40.0, 0.3), 0.02, 0.5)
    return y


def hound_hurt(seed: int) -> np.ndarray:
    """Schmerz: ein kurzes Aufjaulen mit metallischem Nachklang."""
    r = rng(seed)
    m = int(0.35 * SR)
    yelp = tone_voice(glide(m, r.uniform(700, 780), 380, 0.7), curve(m, [(0, 0), (0.05, 1), (0.5, 0.6), (1, 0)]), seed,
                      harmonics=((1, 1.0), (2, 0.5), (3, 0.25), (4, 0.1)), vowel="a", wet=0.5, breath=0.1)
    return ring_mod(yelp, r.uniform(55, 70), 0.4)


def hound_death(seed: int) -> np.ndarray:
    """Tod: ein absinkendes Winseln, das sich in die Leere auflöst – Rauschen und Glitzern verwehen."""
    y = np.zeros(int(2.2 * SR))
    m = int(1.2 * SR)
    t = np.arange(m) / SR
    whine = tone_voice(glide(m, 640, 180, 1.0), curve(m, [(0, 0), (0.06, 1), (0.6, 0.6), (1, 0)]), seed,
                       harmonics=((1, 1.0), (2, 0.3), (3, 0.1)), vib_depth=0.03 * smoothstep(t, 0.2, 0.9), vib_rate=7.0, vowel="u",
                       wet=0.5)
    mix_at(y, ring_mod(whine, 45.0, 0.35), 0.0)
    mix_at(y, swept(1.0, [(0, 2800), (1, 300)], 1.2, [(0, 0), (0.1, 1), (1, 0)], seed + 2), 0.8, 0.6)
    mix_at(y, soul_glint(1.0, seed + 3, 16, 2400, 6400), 0.9, 0.25)
    return y


def hound_step(seed: int) -> np.ndarray:
    """Weiche Pfoten, ein feines Kristallticken."""
    r = rng(seed)
    y = np.zeros(int(0.25 * SR))
    mix_at(y, thud(0.12, r.uniform(85, 100), 55, seed, 300), 0.0, 0.8)
    mix_at(y, clack([(r.uniform(3200, 3800), 1.0), (5200, 0.5)], 0.01, seed + 1, q=30), 0.01, 0.12)
    return y


# =====================================================================================
# Chorusschlund (0.71)
# =====================================================================================

# Chorusholz: höher und heller als das Holz des Moorschlurfers
CHORUS = ((620, 22, 1.0), (1380, 24, 0.6), (2350, 26, 0.35), (3900, 28, 0.2))


def rustle(dur: float, seed: int, amps) -> np.ndarray:
    """Rascheln trockener Blätter und Stängel."""
    return swept(dur, [(0, 3400), (0.5, 2600), (1, 3000)], 0.9, amps, seed, flicker=0.75, flicker_rate=(35.0, 60.0))


def maw_ambient(seed: int) -> np.ndarray:
    """Rascheln, ein leises Knarren der Chorusglieder, darunter ein gedämpftes Schnurren aus dem Schlund."""
    r = rng(seed)
    y = np.zeros(int(1.6 * SR))
    mix_at(y, rustle(1.2, seed, [(0, 0), (0.2, 1), (0.7, 0.8), (1, 0)]), 0.0, 0.5)
    mix_at(y, creak(0.6, [(0, 18), (1, 30)], seed + 1, modes=CHORUS, amps=[(0, 0), (0.2, 1), (1, 0)]), r.uniform(0.2, 0.6), 0.5)
    n = int(1.0 * SR)
    mix_at(y, pulse_voice(np.full(n, r.uniform(48, 56)), curve(n, [(0, 0), (0.3, 1), (1, 0)]), seed + 2, vowel="u", size=0.6,
                          jitter=0.08, shimmer=0.2, sub=0.3, breath=0.2, bright=900), 0.3, 0.35)
    return y


def maw_open(seed: int) -> np.ndarray:
    """Die Blüte klappt auf: ein schnelles Knarren, ein feuchtes Lösen, ein Hauch."""
    y = np.zeros(int(0.9 * SR))
    mix_at(y, creak(0.45, [(0, 25), (1, 60)], seed, modes=CHORUS, amps=[(0, 0), (0.1, 1), (0.8, 0.7), (1, 0)]), 0.0, 0.9)
    mix_at(y, squelch(0.3, seed + 1, hi=1800, lo=500, width=0.5), 0.25, 0.45)
    mix_at(y, swept(0.4, [(0, 700), (1, 1400)], 1.0, [(0, 0), (0.4, 1), (1, 0)], seed + 2), 0.35, 0.25)
    return y


def maw_lash(seed: int) -> np.ndarray:
    """Peitschenhieb: ein rasend schnelles Zischen, dann der Knall."""
    y = np.zeros(int(0.5 * SR))
    mix_at(y, swept(0.14, [(0, 500), (1, 4200)], 0.8, [(0, 0), (0.8, 1), (1, 0)], seed), 0.0, 0.8)
    k = int(0.03 * SR)
    crack = rng(seed + 1).standard_normal(k) * np.exp(-np.arange(k) / (k / 5))
    mix_at(y, norm(filt(crack, "band", [1200, 7000], SR, 2)), 0.13, 1.0)
    mix_at(y, clack([(2900, 1.0), (4400, 0.5)], 0.015, seed + 2, q=12), 0.13, 0.4)
    return y


def maw_bite(seed: int) -> np.ndarray:
    """Zuschnappen: ein saftiges Schmatzen, Zähne schlagen aufeinander, es knirscht."""
    y = np.zeros(int(0.6 * SR))
    mix_at(y, squelch(0.3, seed, hi=1300, lo=350, width=0.45), 0.0, 0.7)
    mix_at(y, clack([(900, 1.0), (1900, 0.6), (3100, 0.3)], 0.03, seed + 1, q=10), 0.06, 1.0)
    mix_at(y, crackle(0.25, seed + 2, 120.0, 1500, 5000), 0.08, 0.35)
    return y


def maw_spit(seed: int) -> np.ndarray:
    """Spucken: ein tiefes "Plopp", ein Luftstoß."""
    r = rng(seed)
    y = np.zeros(int(0.5 * SR))
    mix_at(y, bubble(r.uniform(110, 140), 5.0, 1.6), 0.0, 1.0)
    mix_at(y, swept(0.18, [(0, 1500), (1, 700)], 1.0, [(0, 0), (0.1, 1), (1, 0)], seed + 1), 0.02, 0.45)
    return y


def maw_teleport(seed: int) -> np.ndarray:
    """Verschwinden: ein schimmerndes, auf- und absteigendes Wabern, darüber feines Glitzern."""
    r = rng(seed)
    n = int(0.9 * SR)
    t = np.arange(n) / SR
    f = curve(n, [(0, 300), (0.3, 900), (0.6, 500), (1, 1100)]) * r.uniform(0.92, 1.08)
    amp = curve(n, [(0, 0), (0.08, 1), (0.8, 0.7), (1, 0)]) * (0.6 + 0.4 * np.sin(2 * np.pi * 16 * t) ** 2)
    y = tone(f, amp, SR, harmonics=((1, 1.0), (2, 0.35), (3, 0.15)))
    mix_at(y, soul_glint(0.8, seed + 1, 14, 2000, 6000), 0.1, 0.3)
    return y


def maw_hurt(seed: int) -> np.ndarray:
    """Schmerz: Knarren und ein wütendes Zischen."""
    y = np.zeros(int(0.5 * SR))
    mix_at(y, creak(0.35, [(0, 40), (1, 20)], seed, modes=CHORUS, amps=[(0, 0), (0.05, 1), (1, 0)]), 0.0, 0.9)
    mix_at(y, swept(0.35, [(0, 3600), (1, 2600)], 0.6, [(0, 0), (0.1, 1), (1, 0)], seed + 1), 0.02, 0.5)
    return y


def maw_death(seed: int) -> np.ndarray:
    """Welken: ein langes, absinkendes Knarren, Blätter rascheln zu Boden, ein letztes Seufzen entweicht."""
    y = np.zeros(int(2.2 * SR))
    mix_at(y, creak(1.4, [(0, 30), (1, 8)], seed, modes=CHORUS, amps=[(0, 0), (0.1, 1), (0.8, 0.6), (1, 0)]), 0.0, 0.9)
    mix_at(y, rustle(1.0, seed + 1, [(0, 0), (0.3, 1), (1, 0)]), 0.6, 0.5)
    mix_at(y, swept(1.0, [(0, 1200), (1, 400)], 1.1, [(0, 0), (0.2, 1), (1, 0)], seed + 2), 1.0, 0.35)
    return y


# =====================================================================================
# Ausgabe
# =====================================================================================

# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil) oder None, Ziel-Pegel dBFS)
SYNTH = {
    "werewolf_growl": (werewolf_growl, 3, (0.8, 0.15), -17.0),
    "werewolf_howl": (werewolf_howl, 2, (2.8, 0.4), -14.0),
    "werewolf_snarl": (werewolf_snarl, 3, (0.6, 0.12), -15.0),
    "werewolf_hurt": (werewolf_hurt, 3, (0.6, 0.12), -15.0),
    "werewolf_death": (werewolf_death, 1, (1.4, 0.2), -15.0),
    "werewolf_step": (werewolf_step, 4, None, -22.0),
    "mire_shambler_ambient": (mire_ambient, 3, (1.0, 0.2), -18.0),
    "mire_shambler_hurt": (mire_hurt, 3, (0.6, 0.12), -16.0),
    "mire_shambler_death": (mire_death, 1, (1.4, 0.2), -15.0),
    "mire_shambler_step": (mire_step, 4, None, -21.0),
    "mire_shambler_throw": (mire_throw, 2, (0.6, 0.12), -17.0),
    "mire_shambler_grasp": (mire_grasp, 2, (0.7, 0.15), -16.0),
    "mire_shambler_rise": (mire_rise, 1, (1.2, 0.2), -15.0),
    "mire_shambler_splat": (mire_splat, 3, (0.5, 0.1), -16.0),
    "ember_wyrm_ambient": (wyrm_ambient, 3, (1.4, 0.25), -18.0),
    "ember_wyrm_hurt": (wyrm_hurt, 3, (0.8, 0.15), -15.0),
    "ember_wyrm_death": (wyrm_death, 1, (1.8, 0.3), -14.0),
    "ember_wyrm_surface": (wyrm_surface, 2, (1.8, 0.3), -13.0),
    "ember_wyrm_spit": (wyrm_spit, 3, (0.9, 0.18), -15.0),
    "ember_wyrm_bite": (wyrm_bite, 2, (0.6, 0.12), -14.0),
    "ember_wyrm_dive": (wyrm_dive, 2, (1.2, 0.22), -15.0),
    "ash_wraith_ambient": (wraith_ambient, 3, (1.8, 0.35), -19.0),
    "ash_wraith_hurt": (wraith_hurt, 3, (1.0, 0.25), -16.0),
    "ash_wraith_death": (wraith_death, 1, (2.0, 0.35), -15.0),
    "ash_wraith_cloud": (wraith_cloud, 2, (1.6, 0.3), -15.0),
    "ash_wraith_bolt": (wraith_bolt, 3, (1.2, 0.25), -16.0),
    "ash_wraith_fade": (wraith_fade, 2, (1.4, 0.3), -16.0),
    # 0.71
    "soul_heron_call": (heron_call, 3, (1.6, 0.3), -16.0),
    "soul_heron_stab": (heron_stab, 3, (0.5, 0.1), -15.0),
    "soul_heron_wings": (heron_wings, 3, (0.8, 0.15), -17.0),
    "soul_heron_hurt": (heron_hurt, 3, (0.8, 0.15), -15.0),
    "soul_heron_death": (heron_death, 1, (1.6, 0.3), -15.0),
    "soul_heron_step": (heron_step, 4, None, -23.0),
    "night_ram_ambient": (ram_ambient, 3, (0.8, 0.15), -18.0),
    "night_ram_bleat": (ram_bleat, 3, (1.6, 0.3), -14.0),
    "night_ram_scrape": (ram_scrape, 3, (0.6, 0.12), -17.0),
    "night_ram_charge": (ram_charge, 2, (1.0, 0.2), -14.0),
    "night_ram_impact": (ram_impact, 3, (1.2, 0.25), -13.0),
    "night_ram_stomp": (ram_stomp, 2, (1.4, 0.28), -13.0),
    "night_ram_hurt": (ram_hurt, 3, (0.6, 0.12), -15.0),
    "night_ram_death": (ram_death, 1, (1.4, 0.25), -15.0),
    "night_ram_step": (ram_step, 4, None, -22.0),
    "rift_hound_ambient": (hound_ambient, 3, (2.2, 0.4), -18.0),
    "rift_hound_howl": (hound_howl, 2, (3.2, 0.45), -14.0),
    "rift_hound_phase": (hound_phase, 3, (1.8, 0.35), -15.0),
    "rift_hound_bite": (hound_bite, 3, (0.6, 0.12), -15.0),
    "rift_hound_hurt": (hound_hurt, 3, (1.0, 0.2), -15.0),
    "rift_hound_death": (hound_death, 1, (2.4, 0.4), -15.0),
    "rift_hound_step": (hound_step, 4, None, -23.0),
    "chorus_maw_ambient": (maw_ambient, 3, (1.0, 0.2), -19.0),
    "chorus_maw_open": (maw_open, 2, (0.8, 0.15), -16.0),
    "chorus_maw_lash": (maw_lash, 3, (0.6, 0.12), -15.0),
    "chorus_maw_bite": (maw_bite, 3, (0.6, 0.12), -15.0),
    "chorus_maw_spit": (maw_spit, 3, (0.6, 0.12), -16.0),
    "chorus_maw_teleport": (maw_teleport, 2, (1.4, 0.3), -16.0),
    "chorus_maw_hurt": (maw_hurt, 3, (0.6, 0.12), -16.0),
    "chorus_maw_death": (maw_death, 1, (1.4, 0.25), -15.0),
}

# Wesen: {Klang: Reichweite in Blöcken}
REACH = {
    "werewolf": {"growl": 24, "howl": 96, "snarl": 24, "hurt": 24, "death": 32, "step": 16},
    "mire_shambler": {"ambient": 24, "hurt": 24, "death": 32, "step": 16, "throw": 24, "grasp": 24, "rise": 32, "splat": 20},
    "ember_wyrm": {"ambient": 32, "hurt": 32, "death": 48, "surface": 64, "spit": 40, "bite": 32, "dive": 40},
    "ash_wraith": {"ambient": 24, "hurt": 24, "death": 32, "cloud": 32, "bolt": 32, "fade": 32},
    "soul_heron": {"call": 48, "stab": 20, "wings": 24, "hurt": 24, "death": 32, "step": 12},
    "night_ram": {"ambient": 20, "bleat": 48, "scrape": 20, "charge": 40, "impact": 32, "stomp": 32, "hurt": 24, "death": 32, "step": 16},
    "rift_hound": {"ambient": 24, "howl": 80, "phase": 32, "bite": 20, "hurt": 24, "death": 32, "step": 12},
    "chorus_maw": {"ambient": 16, "open": 20, "lash": 24, "bite": 20, "spit": 24, "teleport": 32, "hurt": 20, "death": 24},
}

# Ereignis "entity.<wesen>.<klang>": (Klänge, Reichweite)
EVENTS = {f"entity.{creature}.{sound}": ([f"{creature}_{sound}"], reach)
          for creature, sounds in REACH.items() for sound, reach in sounds.items()}


def files_of(name: str) -> list[str]:
    return [f"kingdomsatwar:entity/monster/{name}{i + 1}" for i in range(SYNTH[name][1])]


# Seit 1.1.4: diese sehr tiefen Klänge bekommen Obertöne ihres Basses, damit sie auch auf kleinen Lautsprechern
# (Laptop, Fernseher, Kopfhörer ohne Bass) zu hören sind – siehe ambience_dsp.audible_bass.
AUDIBLE = {"rift_hound_step"}


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    for i in range(count):
        x = fn(5000 + i * 37 + zlib.crc32(name.encode()) % 983)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 900 + i, stereo=False, damping=6500), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.04)
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
    for key, (sounds, reach) in EVENTS.items():
        entries = []
        for s in sounds:
            entries += [{"name": f, "attenuation_distance": reach} for f in rendered[s]]
        events[key] = {"sounds": entries, "subtitle": f"subtitles.kingdomsatwar.{key}"}
    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8")) if SOUNDS_JSON.exists() else {}
    data.update(events)
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"{len(events)} Klangereignisse geschrieben")


if __name__ == "__main__":
    main()
