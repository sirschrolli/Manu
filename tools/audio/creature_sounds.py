#!/usr/bin/env python3
"""
Eigene Klänge für Wesen, die sich bisher Klänge von Minecraft liehen (seit 1.1.3): zuerst der Höhlentroll und der Lindwurm.

- Höhlentroll: ein großer, behäbiger Troll mit steinerner Haut – brummt und grummelt tief durch die Nase, grunzt bei Schmerz,
  stöhnt lange im Tod und fällt schwer; im Tageslicht knirscht und knackt er zu Stein; seine Keule saust und kracht, seine
  Schritte dröhnen auf Kies.
- Lindwurm: ein flügelloser Drache wie eine riesige Schlange – zischt und grollt, schnappt zu, kreischt rau, wenn er
  getroffen wird; seine Schuppen schaben über den Boden.

Alles entsteht aus Rauschen und Tönen (siehe ambience_dsp.py, monster_sounds.py, dragon_sounds.py); keine fremden Dateien.

Aufruf (aus dem Projektordner):
    python3 tools/audio/creature_sounds.py [--only name ...] [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/beast/<name><n>.ogg
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

from ambience_dsp import apply_room, curve, fade_edges, filt, rng, room, trim_tail  # noqa: E402
from dragon_sounds import fit, fit_lufs, gust, morph_voice, saturate, snort  # noqa: E402
from dsp import write_ogg  # noqa: E402
from monster_sounds import SR, clack, crackle, frames_len, mix_at, pulse_voice, rumble, swept, thud  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/beast"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"


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
# Tabelle
# =====================================================================================

# Name: (Erzeuger, Varianten, Raum (Nachhall s, Anteil) oder None, Lautheit in LUFS)
SYNTH = {
    "cave_troll_ambient": (troll_ambient, 4, (0.6, 0.12), -20.0),
    "cave_troll_hurt": (troll_hurt, 4, (0.5, 0.1), -16.5),
    "cave_troll_death": (troll_death, 2, (0.8, 0.14), -16.0),
    "cave_troll_petrify": (troll_petrify, 3, (0.7, 0.12), -17.5),
    "cave_troll_smash": (troll_smash, 3, (0.6, 0.1), -15.5),
    "cave_troll_step": (troll_step, 4, None, -21.0),
    "lindworm_ambient": (worm_ambient, 4, (0.5, 0.1), -20.0),
    "lindworm_bite": (worm_bite, 3, (0.4, 0.08), -17.0),
    "lindworm_hurt": (worm_hurt, 4, (0.5, 0.1), -16.5),
    "lindworm_death": (worm_death, 2, (0.8, 0.14), -16.0),
    "lindworm_slither": (worm_slither, 4, None, -23.0),
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
}


def files_of(name: str) -> list[str]:
    return [f"kingdomsatwar:entity/beast/{name}{i + 1}" for i in range(SYNTH[name][1])]


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    OUT.mkdir(parents=True, exist_ok=True)
    for i in range(count):
        x = fn(5300 + i * 43 + zlib.crc32(name.encode()) % 983)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 2100 + i, stereo=False, damping=6000), space[1])
        x = filt(x, "high", 30.0, SR, 2)
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.05)
        x = fit_lufs(x, level)
        write_ogg(x.astype(np.float32), SR, OUT / f"{name}{i + 1}.ogg", quality=5.0)
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
