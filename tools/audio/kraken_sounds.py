#!/usr/bin/env python3
"""
Die Stimme des Krakens (0.91): sein tiefes, nasses Brüllen, das Gurgeln und Schmatzen, wenn er ruht, das Tosen, mit dem er
aus dem Schlund emporsteigt, das Schmatzen eines zupackenden Fangarms, das Rauschen seiner fegenden Arme und sein langes,
sinkendes Stöhnen im Tod. Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py und sea_creatures.py). Wo
Vanilla-Klänge passen (Bisse, Tinte, Gischt, Blasen), verweist sounds.json nur auf sie – es werden keine Minecraft-Dateien
kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/kraken_sounds.py [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/sea/kraken_<klang><n>.ogg
    Einträge "entity.kraken.<klang>" in assets/kingdomsatwar/sounds.json (die übrigen Einträge bleiben, wie sie sind)
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, env_ad, env_asr, filt, fit_level, resonator, rng, room, soft_clip, tone,  # noqa: E402
                          trim_tail, fade_edges)
from ambience_dsp import audible_bass  # noqa: E402
from dsp import write_ogg  # noqa: E402
from sea_creatures import blip, click, mix_at  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/sea"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100


def moving_resonator(x: np.ndarray, freq: np.ndarray, q: float, seg: int = 256) -> np.ndarray:
    """Resonanz, deren Frequenz wandert (der Filterzustand läuft über die Abschnitte weiter – sonst knackt es)."""
    from scipy import signal
    n = len(x)
    out = np.zeros(n)
    zi = np.zeros(2)
    for s0 in range(0, n, seg):
        f = float(min(freq[min(s0 + seg // 2, n - 1)], SR * 0.45))
        b, a = signal.iirpeak(f, q, fs=SR)
        part = x[s0:s0 + seg]
        y, zi = signal.lfilter(b, a, part, zi=zi)
        out[s0:s0 + len(part)] = y
    return out


def wet_formants(noise: np.ndarray, t: np.ndarray, formants, r) -> np.ndarray:
    """Rauschen durch wandernde Resonanzen (wie ein riesiger, nasser Rachen)."""
    body = np.zeros(len(noise))
    for f0, q, g, speed in formants:
        sweep = f0 * (1.0 + 0.3 * np.sin(2 * np.pi * speed * t + r.uniform(0, 6)))
        body += moving_resonator(noise, sweep, q) * g
    return body


def gush(r, dur: float, lo: float, hi: float, attack: float, decay: float) -> np.ndarray:
    """Ein Schwall Rauschen, der weich einsetzt und bis zum Ende ganz verklingt (keine harten Kanten)."""
    n = int(dur * SR)
    e = env_ad(n, SR, attack, decay, curve_pow=2.0)
    e[-int(0.3 * n):] *= np.linspace(1.0, 0.0, int(0.3 * n)) ** 2
    return filt(r.standard_normal(n), "band", [lo, hi], SR, 2) * e


def bubbles(y: np.ndarray, r, count: int, lo: float, hi: float, start: float, end: float, gain: float, seed: int) -> None:
    for i in range(count):
        mix_at(y, blip(r.uniform(lo, hi), r.uniform(0.04, 0.12), r.uniform(0.6, 1.8), seed + i), r.uniform(start, end), gain)


def roar(seed: int) -> np.ndarray:
    """Ein gewaltiges, nasses Brüllen: ein sehr tiefer Grundton, darüber gurgelnde Resonanzen, die langsam wandern, ein Zittern
    wie Wasser in einer riesigen Kehle, dazu aufsteigende Blasen."""
    r = rng(seed)
    dur = 3.4
    n = int(dur * SR)
    t = np.arange(n) / SR
    env = env_asr(n, SR, 0.35, 1.1)
    tremble = 0.75 + 0.25 * np.sin(2 * np.pi * r.uniform(17, 26) * t)
    body = wet_formants(r.standard_normal(n), t, ((130, 4.0, 1.0, 0.45), (300, 5.0, 0.8, 0.6), (620, 6.5, 0.4, 0.8), (1100, 8.0, 0.15, 1.1)), r)
    y = body * env * tremble * 0.55
    f0 = r.uniform(34, 42) * (1.0 + 0.12 * np.sin(2 * np.pi * 0.5 * t)) * (1.0 - 0.15 * t / dur)
    y += tone(f0, env, SR, harmonics=((1, 1.0), (2, 0.6), (3, 0.35), (4, 0.2), (5, 0.1))) * 0.7
    y += filt(r.standard_normal(n), "low", 120, SR, 3) * env * 2.2
    bubbles(y, r, 26, 90, 320, 0.2, dur - 0.3, 0.22, seed)
    return y


def ambient(seed: int) -> np.ndarray:
    """Ruhend: tiefes Grollen, nasses Schmatzen der Arme, ein paar Blasen."""
    r = rng(seed)
    dur = 2.6
    n = int(dur * SR)
    t = np.arange(n) / SR
    env = env_asr(n, SR, 0.5, 0.9)
    y = filt(r.standard_normal(n), "band", [45, 200], SR, 2) * env * 1.6
    y += tone(r.uniform(38, 50) + 5 * np.sin(2 * np.pi * 0.4 * t), env, SR, harmonics=((1, 1.0), (2, 0.45), (3, 0.2))) * 0.4
    at = r.uniform(0.2, 0.6)
    for i in range(int(r.integers(2, 5))):
        mix_at(y, squelch(r, r.uniform(0.18, 0.32)), at, r.uniform(0.35, 0.6))
        at += r.uniform(0.35, 0.6)
    bubbles(y, r, 8, 150, 420, 0.3, dur - 0.2, 0.18, seed + 50)
    return y


def squelch(r, dur: float) -> np.ndarray:
    """Ein nasses Schmatzen: Rauschen durch eine rasch fallende Resonanz, am Ende ein kleiner Ploppen."""
    n = int(dur * SR)
    k = np.arange(n) / n
    out = moving_resonator(r.standard_normal(n), 900.0 * (1.0 - 0.75 * k) + 120.0, 5.0, seg=128)
    out *= env_ad(n, SR, 0.01, dur * 0.8)
    pop = click(r, r.uniform(500, 900), 3.0, body=0.8)
    mix_at(out, pop, dur * 0.7, 0.5)
    return out


def rise(seed: int) -> np.ndarray:
    """Er steigt empor: ein Grollen schwillt an, Wasser rauscht und schäumt, Blasen über Blasen – am Ende bricht es durch."""
    r = rng(seed)
    dur = 4.6
    n = int(dur * SR)
    t = np.arange(n) / SR
    swell = np.clip(t / (dur * 0.75), 0.0, 1.0) ** 1.6
    rumble = filt(r.standard_normal(n), "low", 160, SR, 3) * 3.0
    rush = filt(r.standard_normal(n), "band", [300, 3500], SR, 2) * 0.6
    y = (rumble + rush * swell) * (0.25 + 0.75 * swell)
    y += tone(28 + 14 * swell, 0.3 + 0.7 * swell, SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.25))) * 0.6
    bubbles(y, r, 70, 80, 600, 0.3, dur - 0.4, 0.2, seed)
    # der Durchbruch: ein schwerer Schwall
    mix_at(y, gush(r, 1.6, 150, 4500, 0.12, 0.45) * 2.2, dur - 1.7, 1.0)
    return y * env_asr(n, SR, 0.05, 0.4)


def grab(seed: int) -> np.ndarray:
    """Ein Fangarm packt zu: ein nasser Klatscher, das Saugen der Näpfe, ein Ploppen."""
    r = rng(seed)
    dur = 0.9
    y = np.zeros(int(dur * SR))
    mix_at(y, gush(r, 0.45, 150, 2600, 0.003, 0.07) * 2.2, 0.0, 1.0)
    mix_at(y, squelch(r, 0.45), 0.08, 0.9)
    for i in range(4):
        mix_at(y, click(r, r.uniform(700, 1300), 4.0, body=0.6), 0.25 + i * r.uniform(0.07, 0.12), 0.35)
    return y


def sweep(seed: int) -> np.ndarray:
    """Die Arme fegen durchs Wasser: ein schweres Rauschen, das an- und abschwillt, darunter ein dumpfes Wogen."""
    r = rng(seed)
    dur = 1.4
    n = int(dur * SR)
    t = np.arange(n) / SR
    k = t / dur
    out = moving_resonator(r.standard_normal(n), 250.0 + 1400.0 * np.sin(np.pi * k) ** 2, 1.6)
    env = np.sin(np.pi * np.clip(t / dur, 0, 1)) ** 1.5
    y = out * env * 1.4 + filt(r.standard_normal(n), "low", 140, SR, 2) * env * 1.8
    bubbles(y, r, 10, 200, 600, 0.2, dur - 0.2, 0.15, seed)
    return y


def death(seed: int) -> np.ndarray:
    """Im Tod: ein langes Stöhnen, das tiefer und leiser wird, Blasen steigen, ein letzter, tiefer Schluck."""
    r = rng(seed)
    dur = 5.2
    n = int(dur * SR)
    t = np.arange(n) / SR
    fall = 1.0 - 0.45 * np.clip(t / dur, 0, 1)
    env = env_asr(n, SR, 0.3, 2.5) * (1.0 - 0.6 * np.clip(t / dur, 0, 1))
    body = wet_formants(r.standard_normal(n), t, ((120, 4.0, 1.0, 0.3), (260, 5.0, 0.7, 0.4), (520, 6.0, 0.3, 0.5)), r)
    y = body * env * 0.5
    y += tone(40 * fall, env, SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.3), (4, 0.15))) * 0.7
    bubbles(y, r, 40, 70, 300, 0.5, dur - 0.3, 0.2, seed + 7)
    gulp = tone(np.full(int(0.6 * SR), 30.0), env_ad(int(0.6 * SR), SR, 0.02, 0.5), SR, harmonics=((1, 1.0), (2, 0.4))) * 0.9
    mix_at(y, gulp, dur - 0.9, 1.0)
    return y


def soften(y: np.ndarray, tail: float, lowpass: float | None = None) -> np.ndarray:
    """Weiches Ende (die letzten {@code tail} Sekunden klingen ganz aus) und, wenn gewünscht, keine schrillen Höhen."""
    if lowpass is not None:
        y = filt(y, "low", lowpass, SR, 2)
    k = int(tail * SR)
    if 0 < k < len(y):
        y = y.copy()
        y[-k:] *= np.cos(np.linspace(0.0, np.pi / 2.0, k)) ** 2
    return y


def swim(seed: int) -> np.ndarray:
    """Ein Ruderschlag seiner Arme (seit 1.00): ein tiefes, weiches Wummern verdrängten Wassers, ein Rauschen, ein paar Blasen."""
    r = rng(seed)
    dur = 1.5
    n = int(dur * SR)
    t = np.arange(n) / SR
    env = np.sin(np.pi * np.clip(t / dur, 0, 1)) ** 2
    whoosh = moving_resonator(r.standard_normal(n), 170.0 + 520.0 * np.sin(np.pi * t / dur) ** 2, 1.4) * env
    thump = tone(np.full(n, r.uniform(30, 38)), env_ad(n, SR, 0.12, 0.9), SR, harmonics=((1, 1.0), (2, 0.4))) * 0.8
    y = whoosh * 0.9 + thump + filt(r.standard_normal(n), "low", 110, SR, 2) * env * 1.3
    bubbles(y, r, 7, 140, 380, 0.35, dur - 0.2, 0.1, seed)
    return y


def charge(seed: int) -> np.ndarray:
    """Der Rammstoß (seit 1.00): er saugt Wasser in den Mantel – ein tiefes, gurgelndes Ziehen, das anschwillt –, dann schießt
    es als Strahl heraus: ein gewaltiges Brausen, darunter ein Grollen, dahinter Blasen."""
    r = rng(seed)
    dur = 2.6
    n = int(dur * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    # einsaugen (bis 1,1 s): Rauschen durch eine steigende Resonanz, gurgelnd
    k = np.clip(t / 1.1, 0.0, 1.0)
    suck_env = (k ** 1.8) * (t < 1.15)
    suck = moving_resonator(r.standard_normal(n), 120.0 + 700.0 * k ** 1.5, 3.0) * suck_env
    y += suck * 1.1
    y += tone(28 + 20 * k, suck_env * 0.6, SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.25))) * 0.6
    # der Strahl
    jet = np.zeros(n)
    mix_at(jet, gush(r, 1.5, 90, 5000, 0.02, 0.6) * 2.6, 1.1, 1.0)
    mix_at(jet, filt(r.standard_normal(int(1.4 * SR)), "low", 140, SR, 3) * env_ad(int(1.4 * SR), SR, 0.03, 1.0) * 3.2, 1.1, 1.0)
    y += jet
    bubbles(y, r, 40, 90, 520, 1.15, dur - 0.2, 0.18, seed)
    return soften(y, 0.6, 6000)


def dive(seed: int) -> np.ndarray:
    """Er taucht ab (seit 1.00): ein schwerer Schwall, der nach unten zieht, Gurgeln, aufsteigende Blasen, ein Grollen, das in
    der Tiefe verklingt."""
    r = rng(seed)
    dur = 2.6
    n = int(dur * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    mix_at(y, gush(r, 1.0, 120, 3200, 0.05, 0.35) * 1.8, 0.0, 1.0)
    fall = 1.0 - 0.6 * np.clip(t / dur, 0, 1)
    env = env_asr(n, SR, 0.2, 1.6)
    y += moving_resonator(r.standard_normal(n), 420.0 * fall + 80.0, 4.0) * env * 0.8
    y += tone(36 * fall, env * 0.8, SR, harmonics=((1, 1.0), (2, 0.4), (3, 0.2))) * 0.7
    y += filt(r.standard_normal(n), "low", 100, SR, 3) * env * 1.6
    bubbles(y, r, 45, 70, 420, 0.2, dur - 0.2, 0.2, seed + 3)
    return y


def breach(seed: int) -> np.ndarray:
    """Er bricht aus der Tiefe hervor (seit 1.00): ein kurzes, anschwellendes Grollen, dann ein gewaltiger Ausbruch aus Wasser
    und ein nasses Brüllen; zuletzt prasselt die Gischt zurück aufs Meer."""
    r = rng(seed)
    dur = 3.6
    n = int(dur * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    swell = np.clip(t / 0.45, 0, 1) ** 2 * (t < 0.5)
    y += filt(r.standard_normal(n), "low", 160, SR, 3) * swell * 2.5
    mix_at(y, gush(r, 1.8, 80, 6000, 0.01, 0.55) * 3.0, 0.45, 1.0)
    rn = int(2.6 * SR)
    rt = np.arange(rn) / SR
    renv = env_asr(rn, SR, 0.08, 1.2)
    body = wet_formants(r.standard_normal(rn), rt, ((140, 4.0, 1.0, 0.5), (320, 5.0, 0.8, 0.7), (680, 6.5, 0.35, 0.9)), r)
    roar_part = body * renv * 0.5 + tone(r.uniform(36, 44) * (1.0 - 0.12 * rt / 2.6), renv, SR,
                                        harmonics=((1, 1.0), (2, 0.6), (3, 0.3), (4, 0.15))) * 0.6
    mix_at(y, roar_part, 0.5, 1.0)
    # die Gischt prasselt zurück
    rain = filt(r.standard_normal(n), "band", [1500, 7000], SR, 2) * np.clip((t - 1.2) / 0.3, 0, 1) * np.exp(-np.clip(t - 1.5, 0, None) * 1.4)
    y += rain * 0.5
    bubbles(y, r, 30, 90, 400, 0.6, dur - 0.3, 0.16, seed + 9)
    return soften(y, 1.0, 6500)


def geyser(seed: int) -> np.ndarray:
    """Ein Geysir (seit 1.00): ein Zischen, das sich in einem Atemzug aufbaut, dann schießt die Säule empor – Brausen und
    Rauschen – und fällt prasselnd zurück."""
    r = rng(seed)
    dur = 2.0
    n = int(dur * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    hiss_env = np.clip(t / 0.25, 0, 1) ** 2 * np.exp(-np.clip(t - 0.25, 0, None) * 1.6)
    y += filt(r.standard_normal(n), "band", [2500, 9000], SR, 2) * hiss_env * 0.7
    mix_at(y, gush(r, 1.4, 150, 5500, 0.015, 0.5) * 2.2, 0.18, 1.0)
    y += filt(r.standard_normal(n), "low", 130, SR, 3) * np.clip((t - 0.15) / 0.05, 0, 1) * np.exp(-np.clip(t - 0.2, 0, None) * 2.5) * 2.4
    rain = filt(r.standard_normal(n), "band", [1200, 6500], SR, 2) * np.clip((t - 0.7) / 0.2, 0, 1) * np.exp(-np.clip(t - 0.9, 0, None) * 2.2)
    y += rain * 0.45
    return soften(y, 0.7, 7000)


def tentacle(seed: int) -> np.ndarray:
    """Arme brechen aus dem Wasser (seit 1.00): mehrere Schwälle kurz nacheinander, das nasse Schmatzen der Saugnäpfe, ein
    tiefes Grollen darunter."""
    r = rng(seed)
    dur = 1.9
    n = int(dur * SR)
    t = np.arange(n) / SR
    y = filt(r.standard_normal(n), "low", 120, SR, 3) * env_asr(n, SR, 0.1, 1.0) * 1.4
    at = 0.0
    for i in range(5):
        mix_at(y, gush(r, 0.7, 160, 3800, 0.01, 0.22) * r.uniform(1.0, 1.7), at, 1.0)
        mix_at(y, squelch(r, r.uniform(0.2, 0.34)), at + r.uniform(0.08, 0.2), r.uniform(0.35, 0.55))
        at += r.uniform(0.09, 0.2)
    for i in range(6):
        mix_at(y, click(r, r.uniform(600, 1200), 4.0, body=0.6), r.uniform(0.4, 1.4), 0.3)
    return soften(y, 0.5, 7000)


# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil), Ziel-Pegel dBFS)
SYNTH = {
    "kraken_roar": (roar, 2, (2.2, 0.35), -12.0),
    "kraken_ambient": (ambient, 3, (1.4, 0.3), -18.0),
    "kraken_rise": (rise, 1, (2.0, 0.3), -12.0),
    "kraken_grab": (grab, 2, (0.8, 0.2), -14.0),
    "kraken_sweep": (sweep, 2, (1.0, 0.25), -15.0),
    "kraken_death": (death, 1, (2.4, 0.35), -12.0),
    "kraken_swim": (swim, 3, (1.0, 0.25), -19.0),
    "kraken_charge": (charge, 2, (1.4, 0.3), -12.0),
    "kraken_dive": (dive, 2, (1.6, 0.3), -13.0),
    "kraken_breach": (breach, 2, (2.0, 0.35), -11.0),
    "kraken_geyser": (geyser, 3, (1.2, 0.3), -13.0),
    "kraken_tentacle": (tentacle, 2, (1.2, 0.25), -13.0),
}


def v(path: str, pitch: float = 1.0, volume: float = 1.0, reach: int | None = None) -> dict:
    e = {"name": f"minecraft:{path}"}
    if pitch != 1.0:
        e["pitch"] = pitch
    if volume != 1.0:
        e["volume"] = volume
    if reach is not None:
        e["attenuation_distance"] = reach
    return e


# Ereignis: Liste der Klänge (eigene über ihren Namen in SYNTH, Vanilla über v(...)) und Reichweite der eigenen
EVENTS = {
    "entity.kraken.ambient": (["kraken_ambient"], 40),
    # Biss, Schmerz, Tinte, Schlag, Welle und Strudel des Kraken: sea_boss_sounds.py
    "entity.kraken.death": (["kraken_death"], 80),
    "entity.kraken.rise": (["kraken_rise"], 80),
    "entity.kraken.roar": (["kraken_roar"], 80),
    "entity.kraken.sweep": (["kraken_sweep"], 48),
    "entity.kraken.grab": (["kraken_grab"], 40),
    "entity.kraken.swim": (["kraken_swim"], 40),
    "entity.kraken.charge": (["kraken_charge"], 64),
    "entity.kraken.dive": (["kraken_dive"], 64),
    "entity.kraken.breach": (["kraken_breach"], 80),
    "entity.kraken.geyser": (["kraken_geyser"], 48),
    "entity.kraken.tentacle": (["kraken_tentacle"], 48),
}


# Seit 1.1.4: diese sehr tiefen Klänge bekommen Obertöne ihres Basses, damit sie auch auf kleinen Lautsprechern
# (Laptop, Fernseher, Kopfhörer ohne Bass) zu hören sind – siehe ambience_dsp.audible_bass.
AUDIBLE = {"kraken_death", "kraken_roar", "kraken_ambient"}


def files_of(name: str) -> list[str]:
    return [f"kingdomsatwar:entity/sea/{name}{i + 1}" for i in range(SYNTH[name][1])]


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    files = []
    for i in range(count):
        x = fn(5000 + i * 37 + zlib.crc32(name.encode()) % 997)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 1300 + i, stereo=False, damping=5000), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.05)
        if name in AUDIBLE:
            x = audible_bass(x, SR)
        x = soft_clip(fit_level(x, level, -2.0))
        path = OUT / f"{name}{i + 1}.ogg"
        write_ogg(x.astype(np.float32), SR, path, quality=5.0)
        files.append(f"kingdomsatwar:entity/sea/{name}{i + 1}")
        if preview is not None:
            from ambience import spectrogram
            spectrogram(x, SR, preview / f"{name}{i + 1}.png", f"{name}{i + 1}")
    print(f"  {name}: {count} Varianten")
    return files


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
            if isinstance(s, str):
                entries += [{"name": f, "attenuation_distance": reach} if reach else f for f in rendered[s]]
            else:
                entries.append(s)
        events[key] = {"sounds": entries, "subtitle": f"subtitles.kingdomsatwar.{key}"}
    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8")) if SOUNDS_JSON.exists() else {}
    data.update(events)
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"{len(events)} Klangereignisse geschrieben")


if __name__ == "__main__":
    main()
