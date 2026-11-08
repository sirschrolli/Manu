#!/usr/bin/env python3
"""
Erzeugt die Stimmen der Wesen der Leere: Gurgeln und Ansaugen des Leerenschlunds, das Erwachen und Aufladen des
Splitterwächters, das Knistern, Flackern und Springen des Zerrlings und den Gesang des Sternenrochens. Alles entsteht
aus Rauschen und Tönen (siehe ambience_dsp.py). Wo Vanilla-Klänge passen (Bisse, Schritte, splitternder Amethyst),
verweist sounds.json nur auf sie – es werden keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/void_creatures.py [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/void/<name><n>.ogg
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

from ambience_dsp import (apply_room, bell, curve, env_ad, env_asr, filt, fit_level, formant_filter, rng, room, saw,  # noqa: E402
                          soft_clip, tone, trim_tail, fade_edges)
from dsp import write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/void"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100


def concat(*parts) -> np.ndarray:
    out = []
    for p in parts:
        out.append(np.zeros(int(p * SR)) if isinstance(p, (int, float)) else np.asarray(p, dtype=float))
    return np.concatenate(out)


def mix_at(y: np.ndarray, x: np.ndarray, start: float, gain: float = 1.0) -> None:
    s = int(start * SR)
    m = max(0, min(len(x), len(y) - s))
    y[s:s + m] += x[:m] * gain


# =====================================================================================
# Leerenschlund
# =====================================================================================

def maw_ambient(seed: int) -> np.ndarray:
    """Tiefes, nasses Gurgeln: ein schwankender Ton durch wandernde Formanten, dazu blubbernde Klicks."""
    r = rng(seed)
    n = int(r.uniform(1.4, 2.0) * SR)
    t = np.arange(n) / SR
    f0 = r.uniform(62, 90)
    f = curve(n, [(0, f0 * 0.9), (0.3, f0 * r.uniform(1.1, 1.3)), (0.7, f0), (1, f0 * 0.8)])
    f = f * (1 + 0.05 * np.sin(2 * np.pi * r.uniform(5, 9) * t))
    y = saw(f, env_asr(n, SR, 0.15, 0.4), SR, partials=30, rolloff=1.3)
    lo, hi = r.uniform(250, 380), r.uniform(600, 900)
    y = formant_filter(y, SR, [(lo, 90, 1.0), (hi, 160, 0.6), (2200, 400, 0.15)])
    for i in range(int(r.integers(4, 9))):
        m = int(0.03 * SR)
        click = np.sin(2 * np.pi * np.cumsum(np.linspace(r.uniform(300, 600), r.uniform(900, 1500), m)) / SR) * env_ad(m, SR, 0.002)
        mix_at(y, click, r.uniform(0.1, n / SR - 0.1), r.uniform(0.15, 0.35))
    return filt(y, "low", 3000, SR, 2)


def maw_inhale(seed: int) -> np.ndarray:
    """Ansaugen: ein anschwellendes, saugendes Rauschen, das von tief nach hoch fährt, darunter ein steigendes Dröhnen."""
    r = rng(seed)
    n = int(2.6 * SR)
    t = np.arange(n) / SR
    noise = r.standard_normal(n)
    y = np.zeros(n)
    # gleitendes Bandpass-Rauschen in Blöcken (Mitte steigt von 220 auf 1800 Hz)
    block = 2048
    for s in range(0, n, block // 2):
        e = min(n, s + block)
        k = s / n
        center = 220 * (1800 / 220) ** k
        seg = filt(noise[s:e], "band", [center * 0.6, center * 1.6], SR, 2) * np.hanning(e - s)
        y[s:e] += seg
    y *= curve(n, [(0, 0.05), (0.7, 0.9), (0.92, 1.0), (1, 0.0)])
    rumble = saw(curve(n, [(0, 45), (1, 95)]), curve(n, [(0, 0.0), (0.2, 0.4), (0.9, 0.7), (1, 0.0)]), SR, partials=10, rolloff=1.5)
    y += filt(rumble, "low", 400, SR, 2) * 0.8
    y += 0.25 * np.sin(2 * np.pi * np.cumsum(curve(n, [(0, 300), (1, 700)])) / SR) * curve(n, [(0, 0), (0.5, 0.2), (0.95, 0.5), (1, 0)])
    return y


# =====================================================================================
# Splitterwächter
# =====================================================================================

def warden_ambient(seed: int) -> np.ndarray:
    """Ein tiefes, kristallenes Summen und ein Knirschen im Marmor."""
    r = rng(seed)
    n = int(r.uniform(1.8, 2.4) * SR)
    t = np.arange(n) / SR
    f0 = r.uniform(55, 70)
    y = tone(np.full(n, f0), env_asr(n, SR, 0.4, 0.6), SR, harmonics=((1, 1.0), (2, 0.5), (3, 0.3), (5, 0.12), (8, 0.05)))
    y *= 1 + 0.3 * np.sin(2 * np.pi * r.uniform(2.0, 3.5) * t)
    for i in range(2):
        b = bell(r.uniform(900, 1600), 1.6, SR, ratios=(1.0, 2.0, 2.99, 4.13), amps=(1.0, 0.3, 0.15, 0.08), decay=1.0, seed=seed + i)
        mix_at(y, b, r.uniform(0.2, n / SR - 0.8), 0.18)
    grind = filt(r.standard_normal(n), "band", [180, 900], SR, 2) * env_asr(n, SR, 0.3, 0.5) * (0.5 + 0.5 * np.sin(2 * np.pi * 11 * t) > 0.6)
    return y + grind * 0.12


def warden_awaken(seed: int) -> np.ndarray:
    """Erwachen: Stein knirscht, ein Akkord aus Kristall schwillt an und flammt auf."""
    r = rng(seed)
    n = int(3.0 * SR)
    t = np.arange(n) / SR
    y = np.zeros(n)
    for f, a in ((110.0, 0.6), (164.81, 0.45), (220.0, 0.4), (329.63, 0.3), (440.0, 0.22), (659.26, 0.12)):
        fr = curve(n, [(0, f * 0.94), (0.4, f), (1, f)])
        y += tone(fr, curve(n, [(0, 0), (0.37, a), (0.47, a * 1.1), (0.75, a * 0.3), (1, 0)]), SR, harmonics=((1, 1.0), (2, 0.2), (3, 0.08)))
    grind = filt(r.standard_normal(n), "band", [120, 700], SR, 2) * curve(n, [(0, 0.4), (0.3, 0.8), (0.4, 0.0), (1, 0)])
    y += grind * 0.5
    for i in range(5):
        b = bell(float(r.choice([1318.5, 1760.0, 1975.5, 2637.0])), 1.2, SR, decay=0.8, seed=seed + 10 + i)
        mix_at(y, b, 0.9 + i * 0.07, 0.25)
    return y


def warden_charge(seed: int) -> np.ndarray:
    """Aufladen: ein gläsernes Sirren, das steigt und anschwillt."""
    r = rng(seed)
    n = int(1.0 * SR)
    t = np.arange(n) / SR
    f = curve(n, [(0, 380), (1, 1150)]) * (1 + 0.01 * np.sin(2 * np.pi * 18 * t))
    y = tone(f, curve(n, [(0, 0.0), (0.8, 0.8), (1, 0.0)]), SR, harmonics=((1, 1.0), (2.01, 0.4), (3.02, 0.2), (5.4, 0.1)))
    y += 0.5 * tone(f * 1.5, curve(n, [(0, 0.0), (0.85, 0.5), (1, 0.0)]), SR)
    shimmer = filt(r.standard_normal(n), "band", [3000, 8000], SR, 2) * curve(n, [(0, 0), (0.9, 0.15), (1, 0)])
    return y + shimmer


# =====================================================================================
# Zerrling
# =====================================================================================

def decimate(y: np.ndarray, hold: int, levels: float) -> np.ndarray:
    z = np.repeat(y[::hold], hold)[:len(y)]
    return np.round(z * levels) / levels


def stutter(r, src: np.ndarray, n: int, grain: float, keep: float = 0.7) -> np.ndarray:
    """Zerhackt eine Quelle in kleine Körner und wiederholt manche – wie eine springende Aufnahme."""
    y = np.zeros(n)
    g = int(grain * SR)
    pos = 0
    read = 0
    while pos < n - g:
        if r.random() < keep:
            seg = src[read:read + g]
            if len(seg) < g:
                read = 0
                seg = src[:g]
            y[pos:pos + g] += seg * env_asr(g, SR, 0.002, 0.003)
        if r.random() > 0.35:
            read += g
        pos += g
    return y


def distortling_ambient(seed: int) -> np.ndarray:
    """Knistern und ein zerhacktes, fremdes Flüstern."""
    r = rng(seed)
    n = int(r.uniform(1.2, 1.8) * SR)
    t = np.arange(n) / SR
    src = filt(r.standard_normal(n), "band", [900, 5000], SR, 2) * (0.5 + 0.5 * np.sin(2 * np.pi * r.uniform(3, 6) * t))
    voice = saw(curve(n, [(0, r.uniform(140, 220)), (0.5, r.uniform(250, 380)), (1, r.uniform(120, 180))]), np.ones(n), SR, 20, 1.2)
    voice = formant_filter(voice, SR, [(r.uniform(400, 700), 120, 1.0), (r.uniform(1100, 1800), 200, 0.5)])
    y = stutter(r, src * 0.6 + voice * 0.5, n, r.uniform(0.025, 0.05))
    y = decimate(y, int(r.integers(3, 6)), 6.0)
    return filt(y, "low", 7000, SR, 2) * env_asr(n, SR, 0.02, 0.1)


def distortling_hurt(seed: int) -> np.ndarray:
    """Getroffen: ein kurzer, greller Bildfehler mit kreischendem Ton."""
    r = rng(seed)
    n = int(r.uniform(0.35, 0.5) * SR)
    f = curve(n, [(0, r.uniform(900, 1300)), (1, r.uniform(300, 500))])
    y = np.sign(np.sin(2 * np.pi * np.cumsum(f) / SR)) * 0.5 + filt(r.standard_normal(n), "high", 2000, SR, 2) * 0.4
    y = stutter(r, y, n, 0.018, keep=0.8)
    y = decimate(y, int(r.integers(2, 5)), 5.0)
    return filt(y, "low", 8000, SR, 2) * env_ad(n, SR, 0.005)


def distortling_death(seed: int) -> np.ndarray:
    """Zerfall: der Ton sinkt, zerbröselt in immer gröbere Körner und verlischt."""
    r = rng(seed)
    n = int(1.6 * SR)
    f = curve(n, [(0, 700), (0.4, 300), (1, 60)])
    y = np.sign(np.sin(2 * np.pi * np.cumsum(f) / SR)) * 0.4 + filt(r.standard_normal(n), "band", [500, 4000], SR, 2) * 0.5
    out = np.zeros(n)
    for k, (a, b) in enumerate(((0.0, 0.3), (0.3, 0.6), (0.6, 1.0))):
        s, e = int(a * n), int(b * n)
        seg = stutter(r, y[s:e], e - s, 0.02 + k * 0.03, keep=0.8 - k * 0.2)
        out[s:e] = decimate(seg, 2 + k * 3, 6.0 - k * 2)
    return out * curve(n, [(0, 1.0), (0.7, 0.6), (1, 0.0)])


def distortling_blink(seed: int) -> np.ndarray:
    """Sprung: ein digitales „Bzzt“ mit einem schnellen Pfeifen nach oben."""
    r = rng(seed)
    n = int(0.45 * SR)
    f = curve(n, [(0, r.uniform(200, 300)), (0.6, r.uniform(1800, 2600)), (1, r.uniform(3000, 4000))])
    sweep = np.sin(2 * np.pi * np.cumsum(f) / SR) * curve(n, [(0, 0.2), (0.5, 0.8), (1, 0)])
    buzz = np.sign(np.sin(2 * np.pi * r.uniform(90, 140) * np.arange(n) / SR)) * curve(n, [(0, 0.7), (0.4, 0.3), (1, 0)])
    crackle = (r.random(n) < 0.03) * r.uniform(-1, 1, n) * curve(n, [(0, 1.0), (0.6, 0.6), (1, 0)])
    y = sweep * 0.6 + buzz * 0.4 + crackle * 0.5
    return filt(decimate(y, 2, 8.0), "low", 9000, SR, 2) * env_asr(n, SR, 0.004, 0.06)


def distortling_attack(seed: int) -> np.ndarray:
    """Krallenhieb: ein zischender Streich, der zwischendurch abreißt."""
    r = rng(seed)
    n = int(0.3 * SR)
    y = filt(r.standard_normal(n), "band", [curve(n, [(0, 800), (1, 3000)]).mean() * 0.5, 7000], SR, 2)
    y *= curve(n, [(0, 0), (0.25, 1.0), (1, 0)])
    y = stutter(r, y, n, 0.012, keep=0.85)
    return decimate(y, 2, 8.0)


# =====================================================================================
# Sternenrochen
# =====================================================================================

def ray_call(seed: int, low: float, high: float, notes=(2, 3), dur=(0.6, 1.2)) -> np.ndarray:
    r = rng(seed)
    parts = []
    for i in range(int(r.integers(*notes))):
        n = int(r.uniform(*dur) * SR)
        t = np.arange(n) / SR
        f0 = r.uniform(low, high)
        f = curve(n, [(0, f0), (0.4, f0 * r.uniform(1.15, 1.45)), (1, f0 * r.uniform(0.8, 1.05))])
        f = f * (1 + 0.01 * np.sin(2 * np.pi * r.uniform(4.5, 6) * t))
        parts.append(tone(f, env_asr(n, SR, 0.15, 0.35), SR, harmonics=((1, 1.0), (2, 0.3), (3, 0.12), (4, 0.05))))
        parts.append(r.uniform(0.08, 0.25))
    y = concat(*parts)
    return formant_filter(y, SR, [(700, 350, 1.0), (1600, 500, 0.3)])


def ray_ambient(seed: int) -> np.ndarray:
    return ray_call(seed, 300, 460)


def ray_hurt(seed: int) -> np.ndarray:
    """Ein kurzer, gequälter Ruf, der abbricht und fällt."""
    r = rng(seed)
    n = int(0.6 * SR)
    f = curve(n, [(0, r.uniform(600, 750)), (0.3, r.uniform(800, 900)), (1, r.uniform(300, 400))])
    y = tone(f, env_ad(n, SR, 0.02), SR, harmonics=((1, 1.0), (2, 0.4), (3, 0.2), (5, 0.08)))
    return formant_filter(y, SR, [(800, 400, 1.0), (1800, 600, 0.4)])


def ray_death(seed: int) -> np.ndarray:
    """Ein langer, sinkender Ruf, über dem ein Schimmer verklingt."""
    r = rng(seed)
    n = int(2.4 * SR)
    f = curve(n, [(0, 520), (0.2, 600), (1, 160)])
    y = tone(f, curve(n, [(0, 0), (0.08, 1.0), (0.7, 0.6), (1, 0)]), SR, harmonics=((1, 1.0), (2, 0.35), (3, 0.15)))
    for i in range(6):
        b = bell(float(r.choice([1568.0, 1760.0, 2093.0, 2349.3])), 1.4, SR, decay=0.9, seed=seed + i)
        mix_at(y, b, 0.6 + i * 0.22, 0.18)
    return formant_filter(y, SR, [(700, 400, 1.0), (1500, 500, 0.35)])


# =====================================================================================
# Ausgabe
# =====================================================================================

# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil) oder None, Ziel-Pegel dBFS)
SYNTH = {
    "void_maw_ambient": (maw_ambient, 3, (1.6, 0.25), -18.0),
    "void_maw_inhale": (maw_inhale, 2, (2.0, 0.3), -15.0),
    "shard_warden_ambient": (warden_ambient, 3, (1.4, 0.25), -19.0),
    "shard_warden_awaken": (warden_awaken, 1, (2.2, 0.35), -15.0),
    "shard_warden_charge": (warden_charge, 2, (1.2, 0.25), -17.0),
    "distortling_ambient": (distortling_ambient, 3, (0.8, 0.2), -21.0),
    "distortling_hurt": (distortling_hurt, 3, None, -16.0),
    "distortling_death": (distortling_death, 1, (1.2, 0.25), -15.0),
    "distortling_blink": (distortling_blink, 3, (0.6, 0.15), -16.0),
    "distortling_attack": (distortling_attack, 2, None, -17.0),
    "star_ray_ambient": (ray_ambient, 4, (2.5, 0.35), -18.0),
    "star_ray_hurt": (ray_hurt, 2, (1.5, 0.25), -16.0),
    "star_ray_death": (ray_death, 1, (2.5, 0.35), -15.0),
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
    "entity.void_maw.ambient": (["void_maw_ambient"], 24),
    "entity.void_maw.inhale": (["void_maw_inhale"], 32),
    "entity.void_maw.bite": ([v(f"mob/ravager/bite{i}", 1.25) for i in range(1, 4)], None),
    "entity.void_maw.hurt": ([v(f"mob/guardian/guardian_hit{i}", 0.6) for i in range(1, 5)], None),
    "entity.void_maw.death": ([v("mob/guardian/guardian_death", 0.5)], None),
    "entity.shard_warden.ambient": (["shard_warden_ambient"], 20),
    "entity.shard_warden.hurt": ([v(f"block/amethyst/break{i}", 0.55, 1.0) for i in range(1, 5)]
                                 + [v(f"mob/irongolem/hit{i}", 0.75) for i in range(1, 5)], None),
    "entity.shard_warden.death": ([v("mob/irongolem/death", 0.6)], None),
    "entity.shard_warden.step": ([v(f"mob/irongolem/walk{i}", 0.7) for i in range(1, 5)], None),
    "entity.shard_warden.awaken": (["shard_warden_awaken"], 32),
    "entity.shard_warden.charge": (["shard_warden_charge"], 24),
    "entity.shard_warden.shoot": ([v(f"block/amethyst_cluster/break{i}", 1.35) for i in range(1, 5)], None),
    "entity.shard_warden.slam": ([v("mob/warden/attack_impact_1", 0.7), v("mob/warden/attack_impact_2", 0.7)], None),
    "entity.warden_shard.hit": ([v(f"block/amethyst/break{i}", 1.3, 0.8) for i in range(1, 5)], None),
    "entity.distortling.ambient": (["distortling_ambient"], 16),
    "entity.distortling.hurt": (["distortling_hurt"], 16),
    "entity.distortling.death": (["distortling_death"], 16),
    "entity.distortling.blink": (["distortling_blink"], 20),
    "entity.distortling.attack": (["distortling_attack"], 16),
    "entity.star_ray.ambient": (["star_ray_ambient"], 64),
    "entity.star_ray.hurt": (["star_ray_hurt"], 32),
    "entity.star_ray.death": (["star_ray_death"], 32),
}


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    files = []
    for i in range(count):
        x = fn(2000 + i * 31 + zlib.crc32(name.encode()) % 991)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 700 + i, stereo=False, damping=6000), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.04)
        x = soft_clip(fit_level(x, level, -2.0))
        path = OUT / f"{name}{i + 1}.ogg"
        write_ogg(x.astype(np.float32), SR, path, quality=5.0)
        files.append(f"kingdomsatwar:entity/void/{name}{i + 1}")
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
