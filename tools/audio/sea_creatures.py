#!/usr/bin/env python3
"""
Erzeugt die Stimmen der Meereswesen: das gläserne Schimmern und Nesseln der Leuchtqualle, das unheimliche Summen des
Anglerfischs und das Klappern, Kneifen und Knacken der Riesenkrabbe. Alles entsteht aus Rauschen und Tönen (siehe
ambience_dsp.py). Wo Vanilla-Klänge passen (Gischt, Bisse, Wasser), verweist sounds.json nur auf sie – es werden keine
Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/sea_creatures.py [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/sea/<name><n>.ogg
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

from ambience_dsp import (apply_room, bell, env_ad, env_asr, filt, fit_level, resonator, rng, room, soft_clip, tone,  # noqa: E402
                          trim_tail, fade_edges)
from dsp import write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/sea"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100


def mix_at(y: np.ndarray, x: np.ndarray, start: float, gain: float = 1.0) -> None:
    s = int(start * SR)
    m = max(0, min(len(x), len(y) - s))
    y[s:s + m] += x[:m] * gain


def blip(f0: float, dur: float, rise: float, seed: int) -> np.ndarray:
    """Ein Bläschen: ein kurzer, nach oben gleitender Ton mit weichem Einsatz."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = f0 * (1.0 + rise * t / dur)
    return tone(f, env_ad(n, SR, 0.004, dur * 0.35), SR, harmonics=((1, 1.0), (2, 0.15)))


def soft_bell(f0: float, dur: float, decay: float, seed: int) -> np.ndarray:
    """Ein weicher Glasklang (fast harmonisch, die Obertöne leise), der bis zum Ende sanft ausgeblendet wird – sonst knackt es."""
    b = bell(f0, dur, SR, ratios=(1.0, 2.0, 3.01, 4.2), amps=(1.0, 0.18, 0.06, 0.02), decay=decay, seed=seed)
    n = len(b)
    fade = int(min(n * 0.4, 0.5 * SR))
    b[n - fade:] *= np.linspace(1.0, 0.0, fade) ** 2
    return b


def click(r, f: float, q: float, body: float = 0.0) -> np.ndarray:
    """Ein harter Klick wie Chitin auf Chitin: ein Rauschimpuls durch eine Resonanz, auf Wunsch mit tiefem Körper."""
    n = int(0.06 * SR)
    x = r.standard_normal(n) * env_ad(n, SR, 0.0003, 0.0025)
    y = resonator(x, f, q, SR) * 3.0 + filt(filt(x, "high", 2500, SR, 2), "low", 9000, SR, 2) * 0.3
    if body > 0.0:
        y += resonator(x, f * 0.28, 8.0, SR) * body
    return y


# =====================================================================================
# Leuchtqualle
# =====================================================================================

def jelly_ambient(seed: int) -> np.ndarray:
    """Ein gläsernes Schimmern, das sanft anschwillt und verklingt, dazu ein paar aufsteigende Bläschen."""
    r = rng(seed)
    dur = 3.4
    y = np.zeros(int(dur * SR))
    scale = (1046.5, 1174.7, 1318.5, 1568.0, 1760.0, 2093.0)
    for i in range(int(r.integers(3, 5))):
        b = soft_bell(scale[int(r.integers(0, len(scale)))] * r.choice([0.5, 1.0]), 1.8, 0.8, seed + i)
        b *= env_ad(len(b), SR, r.uniform(0.08, 0.25))
        mix_at(y, b, 0.1 + i * r.uniform(0.18, 0.3), r.uniform(0.25, 0.45))
    for _ in range(int(r.integers(2, 5))):
        mix_at(y, blip(r.uniform(500, 900), r.uniform(0.05, 0.09), r.uniform(0.5, 1.2), seed), r.uniform(0.0, 1.8), r.uniform(0.15, 0.3))
    return filt(y, "low", 6500, SR, 2)


def jelly_sting(seed: int) -> np.ndarray:
    """Nesseln: ein elektrisches Knistern mit einem kurzen, sirrenden Ton, der abfällt."""
    r = rng(seed)
    dur = 0.55
    n = int(dur * SR)
    y = np.zeros(n)
    for _ in range(int(r.integers(14, 24))):
        k = int(r.uniform(0.003, 0.012) * SR)
        burst = filt(r.standard_normal(k), "band", [2500, 9000], SR, 2) * env_ad(k, SR, 0.0005, 0.003)
        mix_at(y, burst, r.uniform(0.0, 0.32), r.uniform(0.3, 0.9))
    t = np.arange(n) / SR
    f = 2600.0 * np.exp(-t * 4.0) + 700.0
    y += tone(f, env_ad(n, SR, 0.003, 0.12), SR, harmonics=((1, 1.0), (3, 0.3), (5, 0.12))) * 0.35
    return y


# =====================================================================================
# Anglerfisch
# =====================================================================================

def angler_ambient(seed: int) -> np.ndarray:
    """Ein tiefes, schwankendes Summen aus der Finsternis, darüber ein einzelnes, hohes, gläsernes Klingen (der Köder)."""
    r = rng(seed)
    dur = 4.2
    n = int(dur * SR)
    t = np.arange(n) / SR
    f0 = r.uniform(52, 68)
    f = f0 * (1.0 + 0.03 * np.sin(2 * np.pi * 0.7 * t) + 0.015 * np.sin(2 * np.pi * 2.3 * t))
    hum = tone(f, env_asr(n, SR, 0.8, 1.4), SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.3), (4, 0.15), (5, 0.08)))
    hum = filt(hum, "low", 420, SR, 2) * 0.8
    noise = filt(r.standard_normal(n), "band", [80, 400], SR, 2) * env_asr(n, SR, 1.0, 1.2) * 0.12
    y = hum + noise
    ping = soft_bell(r.uniform(1900, 2500), 2.2, 1.2, seed)
    mix_at(y, ping, r.uniform(0.6, 1.4), 0.22)
    return y


# =====================================================================================
# Riesenkrabbe
# =====================================================================================

def crab_clack(seed: int) -> np.ndarray:
    """Klappern: drei bis sechs schnelle, harte Klicks der Scheren, mal näher, mal ferner."""
    r = rng(seed)
    y = np.zeros(int(0.9 * SR))
    at = 0.02
    for i in range(int(r.integers(3, 7))):
        c = click(r, r.uniform(2200, 3600), r.uniform(6, 12), body=r.uniform(0.2, 0.5))
        mix_at(y, c, at, r.uniform(0.6, 1.0))
        at += r.uniform(0.06, 0.13)
    return y


def crab_pinch(seed: int) -> np.ndarray:
    """Kneifen: ein einzelner, kräftiger Knack mit einem dumpfen Körper."""
    r = rng(seed)
    y = np.zeros(int(0.4 * SR))
    mix_at(y, click(r, r.uniform(1800, 2400), 5.0, body=1.2), 0.0, 1.0)
    mix_at(y, click(r, r.uniform(2600, 3200), 8.0, body=0.3), r.uniform(0.04, 0.06), 0.5)
    return y


def crab_hurt(seed: int) -> np.ndarray:
    """Getroffen: der Panzer knackt, Chitin schabt."""
    r = rng(seed)
    y = np.zeros(int(0.55 * SR))
    mix_at(y, click(r, r.uniform(1400, 1900), 4.0, body=1.4), 0.0, 1.0)
    k = int(0.25 * SR)
    scrape = filt(r.standard_normal(k), "band", [1500, 5000], SR, 2) * env_ad(k, SR, 0.01, 0.07)
    scrape *= 1.0 + 0.8 * np.sin(2 * np.pi * r.uniform(40, 70) * np.arange(k) / SR)
    mix_at(y, scrape, 0.03, 0.45)
    for i in range(2):
        mix_at(y, click(r, r.uniform(2500, 3500), 9.0), 0.12 + i * r.uniform(0.05, 0.09), 0.5)
    return y


def crab_death(seed: int) -> np.ndarray:
    """Der Panzer bricht, die Scheren klicken noch ein paarmal, immer leiser."""
    r = rng(seed)
    y = np.zeros(int(1.4 * SR))
    mix_at(y, click(r, 1500, 3.5, body=1.6), 0.0, 1.0)
    mix_at(y, click(r, 1200, 3.0, body=1.2), 0.07, 0.8)
    at = 0.3
    for i in range(6):
        mix_at(y, click(r, r.uniform(2400, 3400), 9.0), at, 0.6 * 0.72 ** i)
        at += r.uniform(0.1, 0.18)
    return y


# =====================================================================================
# Ausgabe
# =====================================================================================

# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil) oder None, Ziel-Pegel dBFS)
SYNTH = {
    "glow_jellyfish_ambient": (jelly_ambient, 3, (1.8, 0.35), -26.0),
    "glow_jellyfish_sting": (jelly_sting, 2, (0.6, 0.15), -19.0),
    "anglerfish_ambient": (angler_ambient, 3, (2.2, 0.35), -22.0),
    "giant_crab_clack": (crab_clack, 4, (0.4, 0.1), -21.0),
    "giant_crab_pinch": (crab_pinch, 2, (0.4, 0.1), -17.0),
    "giant_crab_hurt": (crab_hurt, 2, (0.4, 0.1), -17.0),
    "giant_crab_death": (crab_death, 1, (0.6, 0.15), -17.0),
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
    "entity.manta_ray.hurt": ([v(f"mob/squid/hurt{i}", 0.6) for i in range(1, 5)], None),
    "entity.manta_ray.death": ([v(f"mob/squid/death{i}", 0.55) for i in range(1, 4)], None),
    "entity.manta_ray.leap": ([v(f"mob/dolphin/jump{i}", 0.65, 1.0, 32) for i in range(1, 4)], None),
    "entity.manta_ray.splash": ([v("liquid/heavy_splash", 0.75, 1.0, 48)] + [v(f"mob/dolphin/splash{i}", 0.6, 1.0, 48) for i in range(1, 4)], None),
    "entity.glow_jellyfish.ambient": (["glow_jellyfish_ambient"], 12),
    "entity.glow_jellyfish.hurt": ([v(f"mob/slime/small{i}", 1.25, 0.7) for i in range(1, 6)], None),
    "entity.glow_jellyfish.death": ([v(f"mob/slime/small{i}", 1.05, 0.8) for i in range(1, 6)], None),
    "entity.glow_jellyfish.sting": (["glow_jellyfish_sting"], 16),
    "entity.shark.hurt": ([v(f"entity/fish/hurt{i}", 0.55) for i in range(1, 5)], None),
    "entity.shark.death": ([v(f"entity/fish/hurt{i}", 0.45) for i in range(1, 5)], None),
    "entity.shark.bite": ([v(f"mob/ravager/bite{i}", 1.15) for i in range(1, 4)], None),
    "entity.anglerfish.ambient": (["anglerfish_ambient"], 20),
    "entity.anglerfish.hurt": ([v(f"mob/guardian/guardian_hit{i}", 1.3) for i in range(1, 5)], None),
    "entity.anglerfish.death": ([v("mob/guardian/guardian_death", 1.25)], None),
    "entity.anglerfish.snap": ([v("mob/evocation_illager/fangs", 1.2)], None),
    "entity.anglerfish.bite": ([v(f"mob/ravager/bite{i}", 1.35) for i in range(1, 4)], None),
    "entity.giant_crab.clack": (["giant_crab_clack"], 16),
    "entity.giant_crab.pinch": (["giant_crab_pinch"], 16),
    "entity.giant_crab.hurt": (["giant_crab_hurt"], 16),
    "entity.giant_crab.death": (["giant_crab_death"], 16),
    "entity.giant_crab.step": ([v(f"mob/spider/step{i}", 1.7, 0.5) for i in range(1, 5)], None),
}


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    files = []
    for i in range(count):
        x = fn(3000 + i * 31 + zlib.crc32(name.encode()) % 991)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 900 + i, stereo=False, damping=6000), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.04)
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
    args = ap.parse_args()
    if args.preview:
        args.preview.mkdir(parents=True, exist_ok=True)
    OUT.mkdir(parents=True, exist_ok=True)
    rendered = {name: render(name, args.preview) for name in SYNTH}
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
