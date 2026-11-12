#!/usr/bin/env python3
"""
Die Klänge des Seelenhirten (0.98): ein tiefer, hohler Atem durch den Widderschädel, fern ein Glöckchen und Seelenflüstern
(wenn er wacht); ein knöchernes Knacken und Stöhnen, wenn man ihn trifft; schwere Schritte im Morast mit dem Klopfen des
Stabs; sein Erwachen (ein langer Atemzug, ein anschwellendes Grollen, gläsernes Aufglimmen der Augen); die Seelenglocke (ein
tiefer, schwerer Bronzeschlag mit langem Nachklang). Dazu die Angriffe: die klirrende Kette des Hakenzugs, das Fauchen der
geschwungenen Laterne, aufsteigende, gläserne Irrlichter (und ihr Zerstieben), der Nebelgang (ein Zischen, das in sich
zusammenfällt), der Pferch (ein Kreis aus Feuer, der auflodert), der Hirtenstoß (ein schwerer Schlag in den Grund), der
Seelensturm (kreisender Wind voller Stimmen), sein Zorn (ein rauer Schrei und ein misstönender Glockenschlag) und sein Tod
(ein sinkender Klagelaut, der in Wind und aufsteigende Seelen zerfließt; zuletzt ein leiser Glockenschlag).

Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py, keeper_sounds.py, sea_creatures.py); es werden keine
Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/soul_shepherd_sounds.py [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/shadow/soul_shepherd_<klang><n>.ogg
    Einträge "entity.soul_shepherd.<klang>" in assets/kingdomsatwar/sounds.json (die übrigen bleiben)
"""
from __future__ import annotations

import argparse
import json
import sys
import zlib
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

from ambience_dsp import (apply_room, bell, env_ad, env_asr, filt, fit_level, formant_filter, glide, resonator, rng, room, saw,  # noqa: E402
                          soft_clip, tone, trim_tail, fade_edges)
from dsp import write_ogg  # noqa: E402
from keeper_sounds import gravel, impact, thud  # noqa: E402
from kraken_sounds import gush, moving_resonator  # noqa: E402
from merfolk_sounds import VOWELS  # noqa: E402
from sea_creatures import click, mix_at, soft_bell  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/shadow"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100


# =====================================================================================
# Bausteine
# =====================================================================================

def breath(r, dur: float, inhale: bool, lo: float = 180.0, hi: float = 1600.0) -> np.ndarray:
    """Ein hohler Atemzug durch Knochen: Rauschen durch die Formanten von „oo“, an- oder abschwellend."""
    n = int(dur * SR)
    src = filt(r.standard_normal(n), "band", [lo, hi], SR, 2)
    y = formant_filter(src, SR, [(f * 0.7, bw * 1.4, g) for f, bw, g in VOWELS["oo"]])
    env = env_asr(n, SR, dur * (0.7 if inhale else 0.15), dur * (0.25 if inhale else 0.75))
    return y * env


def groan(r, dur: float, f0: float, f1: float, vowel: str = "oo", rough: float = 0.0) -> np.ndarray:
    """Ein tiefes Stöhnen: eine Sägezahnstimme durch Vokal-Formanten, gleitend, auf Wunsch rau (Flattern)."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = glide(n, f0, f1, 0.8) * (1.0 + 0.012 * np.sin(2 * np.pi * 4.6 * t))
    amp = env_asr(n, SR, min(0.3, dur * 0.25), dur * 0.4)
    if rough > 0.0:
        amp *= 1.0 - rough * 0.5 * (1.0 + np.sin(2 * np.pi * 31.0 * t + r.uniform(0, 6)))
    v = saw(f, amp, SR, partials=30, rolloff=1.1)
    return formant_filter(v, SR, VOWELS[vowel])


def whispers(r, dur: float, count: int, gain: float = 1.0) -> np.ndarray:
    """Seelenflüstern: mehrere leise, gehauchte Stimmen, die auftauchen und vergehen."""
    n = int(dur * SR)
    y = np.zeros(n)
    t = np.arange(n) / SR
    for i in range(count):
        src = filt(r.standard_normal(n), "band", [700, 6500], SR, 2)
        vowel = ("eh", "ah", "oo")[i % 3]
        w = formant_filter(src, SR, [(f * r.uniform(1.0, 1.3), bw * 1.8, g) for f, bw, g in VOWELS[vowel]])
        start, length = r.uniform(0, dur * 0.6), r.uniform(dur * 0.3, dur * 0.7)
        env = np.clip(1.0 - np.abs((t - start - length / 2) / (length / 2)), 0.0, 1.0) ** 1.5
        syll = 0.55 + 0.45 * np.sin(2 * np.pi * r.uniform(3.0, 6.0) * t + r.uniform(0, 6)) ** 2
        y += w * env * syll * gain
    return y


def toll(f0: float, dur: float, seed: int, decay: float = 2.6, bright: float = 1.0) -> np.ndarray:
    """Ein Bronzeschlag: die Teiltöne einer großen Glocke (Hum, Grundton, Terz, Quinte, Nominal …), der Anschlag metallisch."""
    ratios = (0.5, 1.0, 1.19, 1.5, 2.0, 2.52, 2.66, 3.01, 4.0)
    amps = (0.7, 1.0, 0.5 * bright, 0.32, 0.55 * bright, 0.2 * bright, 0.16, 0.14 * bright, 0.08 * bright)
    y = bell(f0, dur, SR, ratios=ratios, amps=amps, decay=decay, seed=seed)
    r = rng(seed + 5)
    n = len(y)
    strike = filt(r.standard_normal(int(0.08 * SR)), "band", [1500, 7000], SR, 2) * env_ad(int(0.08 * SR), SR, 0.0005, 0.012)
    y[:len(strike)] += strike * 0.5
    fade = int(min(n * 0.3, 0.6 * SR))
    y[n - fade:] *= np.linspace(1.0, 0.0, fade) ** 2
    return y


def chain_rattle(r, dur: float, density: float) -> np.ndarray:
    """Eine klirrende Kette: viele harte, metallische Klicks."""
    n = int(dur * SR)
    y = np.zeros(n)
    for _ in range(int(density * dur)):
        x = click(r, r.uniform(2400, 5200), r.uniform(18, 40))
        mix_at(y, x * r.uniform(0.3, 1.0), r.uniform(0, dur - 0.07))
    return y


def fire_whoosh(r, dur: float, lo: float, hi: float, attack: float) -> np.ndarray:
    """Fauchendes Feuer: Rauschen, dessen Klangfarbe nach oben streicht, dazu Knistern."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    base = gush(r, dur, lo, hi, attack, dur * 0.6)
    sweep = lo * 1.5 * (hi / lo) ** np.clip(t / dur, 0, 1) ** 0.5
    body = moving_resonator(r.standard_normal(n), sweep, 3.0) * env_asr(n, SR, attack, dur * 0.6) * 0.5
    crackle = np.zeros(n)
    for _ in range(int(dur * 40)):
        at = int(r.uniform(0, max(1, n - 400)))
        length = int(r.uniform(40, 300))
        crackle[at:at + length] += r.standard_normal(length) * np.exp(-np.arange(length) / (length * 0.15)) * r.uniform(0.2, 0.9)
    crackle = filt(crackle, "high", 1800, SR, 2) * env_asr(n, SR, attack, dur * 0.5)
    return base + body + crackle * 0.6


def glass(r, count: int, dur: float, lo: float, hi: float, rise: bool) -> np.ndarray:
    """Gläsernes Funkeln: hohe, weiche Glockentöne, auf Wunsch aufsteigend."""
    n = int(dur * SR)
    y = np.zeros(n)
    for i in range(count):
        k = i / max(1, count - 1)
        f = lo * (hi / lo) ** (k if rise else r.uniform(0, 1))
        mix_at(y, soft_bell(f, r.uniform(0.4, 0.9), r.uniform(0.15, 0.35), 31 + i), (dur * 0.75) * k + r.uniform(0, 0.05),
               r.uniform(0.25, 0.5))
    return y


def wind(r, dur: float, lo: float, hi: float, speed: float) -> np.ndarray:
    """Wind, der kreist: Rauschen durch eine wandernde Resonanz."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    sweep = np.sqrt(lo * hi) * (hi / lo) ** (0.5 * np.sin(2 * np.pi * speed * t + r.uniform(0, 6)))
    return moving_resonator(r.standard_normal(n), sweep, 4.0)


# =====================================================================================
# Stimme
# =====================================================================================

def ambient(seed: int) -> np.ndarray:
    """Ein tiefer, hohler Atemzug (ein und aus), darunter ein Brummen; fern ein Glöckchen, ein Flüstern."""
    r = rng(seed)
    dur = 3.4
    y = np.zeros(int(dur * SR))
    mix_at(y, breath(r, 1.3, True), 0.0, 0.9)
    mix_at(y, breath(r, 1.6, False), 1.3, 1.0)
    mix_at(y, groan(r, 1.5, 62.0, 55.0, "oo") * 0.35, 1.4)
    mix_at(y, soft_bell(1318.5 if seed % 2 else 1174.7, 1.4, 0.6, seed), 0.6 + 0.8 * (seed % 2), 0.12)
    mix_at(y, whispers(r, 2.6, 2, 0.25), 0.5)
    return y


def hurt(seed: int) -> np.ndarray:
    """Ein knöchernes Knacken, ein kurzes, hohles Stöhnen."""
    r = rng(seed)
    y = np.zeros(int(0.9 * SR))
    mix_at(y, click(r, r.uniform(900, 1400), 12.0, body=1.2), 0.0, 1.4)
    mix_at(y, click(r, r.uniform(1800, 2600), 18.0), 0.03, 0.8)
    mix_at(y, groan(r, 0.7, r.uniform(95, 115), r.uniform(70, 80), "ah", rough=0.5), 0.04, 0.9)
    return y


def death(seed: int) -> np.ndarray:
    """Ein langer, sinkender Klagelaut; er zerfließt in Wind, viele Seelen steigen gläsern auf – zuletzt ein leiser Glockenschlag."""
    r = rng(seed)
    dur = 5.0
    y = np.zeros(int(dur * SR))
    mix_at(y, groan(r, 2.6, 120.0, 48.0, "oo", rough=0.25), 0.0, 1.0)
    mix_at(y, groan(r, 2.4, 180.0, 70.0, "ah") * 0.35, 0.1)
    w = wind(r, 3.8, 300, 2400, 0.35) * env_asr(int(3.8 * SR), SR, 1.2, 1.6)
    mix_at(y, w, 1.0, 0.5)
    mix_at(y, glass(r, 9, 2.2, 700, 2600, True), 1.8, 1.0)
    mix_at(y, toll(196.0, 2.0, seed, decay=1.4, bright=0.6), 3.0, 0.45)
    return y


def step(seed: int) -> np.ndarray:
    """Ein schwerer Schritt im Morast: dumpfer Tritt, Schmatzen; bei jedem zweiten klopft der Stab auf."""
    r = rng(seed)
    y = np.zeros(int(0.7 * SR))
    n = int(0.35 * SR)
    mix_at(y, thud(n, 95.0, 55.0, 0.08), 0.0, 0.9)
    squelch = filt(r.standard_normal(int(0.22 * SR)), "band", [250, 1800], SR, 2) * env_ad(int(0.22 * SR), SR, 0.01, 0.08)
    mix_at(y, squelch, 0.02, 0.5)
    if seed % 2 == 0:
        knock = resonator(r.standard_normal(int(0.05 * SR)) * env_ad(int(0.05 * SR), SR, 0.0004, 0.004), 620.0, 14.0, SR)
        mix_at(y, knock * 2.5, 0.18, 0.6)
    return y


def awaken(seed: int) -> np.ndarray:
    """Ein langer Atemzug, ein anschwellendes, tiefes Grollen – und gläsern glimmen die Augen auf."""
    r = rng(seed)
    dur = 3.6
    y = np.zeros(int(dur * SR))
    mix_at(y, breath(r, 1.6, True, 150, 1400), 0.0, 1.1)
    mix_at(y, groan(r, 2.0, 50.0, 78.0, "oo", rough=0.15), 1.2, 0.9)
    mix_at(y, glass(r, 5, 1.0, 900, 1900, True), 2.1, 0.9)
    mix_at(y, whispers(r, 3.0, 3, 0.35), 0.3)
    return y


def fury(seed: int) -> np.ndarray:
    """Sein Zorn: ein rauer, hohler Schrei, darunter Grollen – und ein misstönender Glockenschlag."""
    r = rng(seed)
    dur = 3.4
    y = np.zeros(int(dur * SR))
    scream = groan(r, 1.8, 150.0, 210.0, "ah", rough=0.6)
    scream = np.tanh(scream * 3.0) * 0.5
    mix_at(y, scream, 0.0, 1.0)
    mix_at(y, groan(r, 1.9, 58.0, 46.0, "oo", rough=0.4), 0.0, 0.8)
    mix_at(y, toll(185.0, 2.2, seed + 3, decay=1.6) * 0.6 + toll(196.0 * 1.06, 2.2, seed + 4, decay=1.4) * 0.4, 1.2, 0.8)
    mix_at(y, whispers(r, 2.4, 4, 0.4), 0.4)
    return y


# =====================================================================================
# Glocke und Angriffe
# =====================================================================================

def bell_toll(seed: int) -> np.ndarray:
    """Die Seelenglocke: ein tiefer, schwerer Bronzeschlag mit langem, schwebendem Nachklang; darin ein Hauch von Stimmen."""
    r = rng(seed)
    dur = 4.6
    y = np.zeros(int(dur * SR))
    f0 = (196.0, 174.6)[seed % 2]
    mix_at(y, toll(f0, dur, seed, decay=3.0), 0.0, 1.0)
    mix_at(y, whispers(r, 3.0, 3, 0.18), 0.5)
    return y


def hook(seed: int) -> np.ndarray:
    """Der Hakenzug: ein Sausen, die Kette klirrt hinaus – und zieht rasselnd zurück."""
    r = rng(seed)
    dur = 1.2
    y = np.zeros(int(dur * SR))
    mix_at(y, gush(r, 0.5, 600, 4000, 0.15, 0.2), 0.0, 0.6)
    mix_at(y, chain_rattle(r, 0.45, 60), 0.12, 0.8)
    mix_at(y, chain_rattle(r, 0.4, 45), 0.62, 0.6)
    mix_at(y, click(r, 1400, 10.0, body=1.0), 0.55, 0.8)
    return y


def lantern(seed: int) -> np.ndarray:
    """Der Laternenschwung: ein Fauchen von Seelenfeuer, das im Bogen vorbeizieht, darunter ein dumpfes „Wumm“."""
    r = rng(seed)
    dur = 1.4
    y = np.zeros(int(dur * SR))
    mix_at(y, fire_whoosh(r, 1.1, 300, 3200, 0.3), 0.0, 1.0)
    mix_at(y, thud(int(0.5 * SR), 90.0, 50.0, 0.18), 0.3, 0.6)
    return y


def wisps(seed: int) -> np.ndarray:
    """Irrlichter: hohe, gläserne Töne, die nacheinander aufsteigen, dazu ein Hauchen."""
    r = rng(seed)
    dur = 2.0
    y = np.zeros(int(dur * SR))
    mix_at(y, glass(r, 7, 1.6, 880, 2640, True), 0.0, 1.0)
    mix_at(y, whispers(r, 1.8, 2, 0.4), 0.1)
    mix_at(y, gush(r, 1.2, 1500, 7000, 0.4, 0.5), 0.2, 0.25)
    return y


def wisp_hit(seed: int) -> np.ndarray:
    """Ein Irrlicht zerstiebt: ein weiches „Pff“ und ein Funkeln."""
    r = rng(seed)
    y = np.zeros(int(0.7 * SR))
    mix_at(y, gush(r, 0.3, 500, 5000, 0.004, 0.08), 0.0, 1.0)
    mix_at(y, glass(r, 3, 0.4, 1600, 3000, False), 0.02, 0.6)
    return y


def fog(seed: int) -> np.ndarray:
    """Nebelgang: ein Zischen, das anschwillt und in sich zusammenfällt, tiefes Rauschen, Flüstern."""
    r = rng(seed)
    dur = 1.8
    n = int(dur * SR)
    y = np.zeros(n)
    hiss = filt(r.standard_normal(n), "band", [1200, 8000], SR, 2) * env_asr(n, SR, 0.6, 0.9)
    mix_at(y, hiss, 0.0, 0.5)
    mix_at(y, wind(r, dur, 120, 700, 0.5) * env_asr(n, SR, 0.5, 1.0), 0.0, 0.6)
    mix_at(y, whispers(r, 1.5, 3, 0.4), 0.15)
    return y


def pen(seed: int) -> np.ndarray:
    """Der Pferch: ein Kreis aus Seelenfeuer lodert auf – Fauchen ringsum, ein tiefer Ton schwillt an."""
    r = rng(seed)
    dur = 2.2
    n = int(dur * SR)
    y = np.zeros(n)
    mix_at(y, fire_whoosh(r, 1.8, 200, 2600, 0.5), 0.0, 1.0)
    drone = tone(glide(n, 98.0, 110.0, 1.0), env_asr(n, SR, 0.6, 0.9), SR, harmonics=((1, 1.0), (2, 0.4), (3, 0.2), (5, 0.08)))
    mix_at(y, drone, 0.0, 0.35)
    return y


def crash(seed: int) -> np.ndarray:
    """Der Hirtenstoß: der Stab fährt in den Grund – ein schwerer Schlag, ein Krachen, Erde und Steine rieseln."""
    r = rng(seed)
    dur = 1.6
    y = np.zeros(int(dur * SR))
    mix_at(y, thud(int(0.9 * SR), 70.0, 32.0, 0.35), 0.0, 1.2)
    mix_at(y, impact(int(0.4 * SR), seed, 200, 3000, 0.08), 0.0, 0.9)
    mix_at(y, gravel(int(1.2 * SR), seed + 1, 40.0, 600, 4000) * env_ad(int(1.2 * SR), SR, 0.02, 0.5), 0.08, 0.35)
    mix_at(y, toll(392.0, 1.0, seed, decay=0.4, bright=0.5), 0.0, 0.18)
    return y


def storm(seed: int) -> np.ndarray:
    """Der Seelensturm: kreisender Wind voller Stimmen, ein dunkler Akkord darunter."""
    r = rng(seed)
    dur = 4.2
    n = int(dur * SR)
    y = np.zeros(n)
    env = env_asr(n, SR, 1.0, 1.4)
    mix_at(y, wind(r, dur, 200, 2200, 0.9) * env, 0.0, 0.8)
    mix_at(y, wind(r, dur, 400, 3600, 1.3) * env, 0.0, 0.4)
    mix_at(y, whispers(r, dur, 7, 0.55), 0.0)
    t = np.arange(n) / SR
    chord = sum(tone(np.full(n, f), env * 0.3, SR, harmonics=((1, 1.0), (2, 0.25))) for f in (73.4, 87.3, 110.0, 103.8))
    mix_at(y, chord * (0.7 + 0.3 * np.sin(2 * np.pi * 0.7 * t)), 0.0, 0.45)
    return y


# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil) oder None, Ziel-Pegel dBFS)
SYNTH = {
    "ambient": (ambient, 2, (1.8, 0.3), -22.0),
    "hurt": (hurt, 3, (1.0, 0.2), -17.0),
    "death": (death, 1, (2.8, 0.4), -14.0),
    "step": (step, 4, None, -21.0),
    "awaken": (awaken, 1, (2.4, 0.4), -16.0),
    "bell": (bell_toll, 2, (2.6, 0.35), -14.0),
    "hook": (hook, 2, (1.0, 0.2), -17.0),
    "lantern": (lantern, 2, (1.2, 0.25), -16.0),
    "wisps": (wisps, 2, (1.8, 0.35), -18.0),
    "wisp_hit": (wisp_hit, 3, (0.8, 0.2), -19.0),
    "fog": (fog, 2, (1.6, 0.35), -17.0),
    "pen": (pen, 1, (1.8, 0.3), -15.0),
    "crash": (crash, 2, (1.4, 0.3), -14.0),
    "storm": (storm, 1, (2.2, 0.35), -15.0),
    "fury": (fury, 1, (2.4, 0.4), -13.0),
}

# Ereignis: Reichweite
EVENTS = {f"entity.soul_shepherd.{name}": reach for name, reach in (
    ("ambient", 24), ("hurt", 32), ("death", 64), ("step", 20), ("awaken", 48), ("bell", 64), ("hook", 32), ("lantern", 32),
    ("wisps", 40), ("wisp_hit", 24), ("fog", 32), ("pen", 40), ("crash", 48), ("storm", 48), ("fury", 64))}


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    files = []
    for i in range(count):
        x = fn(9100 + i * 41 + zlib.crc32(name.encode()) % 997)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 2100 + i, stereo=False, damping=4200), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.05)
        x = soft_clip(fit_level(x, level, -2.0))
        path = OUT / f"soul_shepherd_{name}{i + 1}.ogg"
        write_ogg(x.astype(np.float32), SR, path, quality=5.0)
        files.append(f"kingdomsatwar:entity/shadow/soul_shepherd_{name}{i + 1}")
        if preview is not None:
            from ambience import spectrogram
            spectrogram(x, SR, preview / f"{name}{i + 1}.png", f"{name}{i + 1}")
    print(f"  {name}: {count} Varianten")
    return files


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--preview", type=Path, help="Ordner für Spektrogramme")
    args = ap.parse_args()
    if args.preview:
        args.preview.mkdir(parents=True, exist_ok=True)
    OUT.mkdir(parents=True, exist_ok=True)
    events = {}
    for name in SYNTH:
        files = render(name, args.preview)
        reach = EVENTS[f"entity.soul_shepherd.{name}"]
        events[f"entity.soul_shepherd.{name}"] = {"sounds": [{"name": f, "attenuation_distance": reach} for f in files],
                                                  "subtitle": f"subtitles.kingdomsatwar.entity.soul_shepherd.{name}"}
    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8")) if SOUNDS_JSON.exists() else {}
    data.update(events)
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"{len(events)} Klangereignisse geschrieben")


if __name__ == "__main__":
    main()
