#!/usr/bin/env python3
"""
Erzeugt die Klänge der Tiere der eigenen Biome (0.85/0.86), für die Minecraft keine passenden Laute hat: das lachende
"kjau-kjau-kjau" der Sturmmöwe, ihr Kreischen und Flügelklatschen, der durchdringende Adlerschrei des Greifs, sein
löwenhaftes Grollen und seine schweren Schwingenschläge, das nasale Schnattern der Flamingos. Alles entsteht aus Rauschen und Tönen (Bausteine aus
monster_sounds.py und ambience_dsp.py); es werden keine Minecraft-Dateien kopiert.

Aufruf (aus dem Projektordner):
    python3 tools/audio/beast_sounds.py [--only name ...] [--preview /pfad]
Ergebnis:
    src/main/resources/assets/kingdomsatwar/sounds/entity/beast/<name><n>.ogg
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

from ambience_dsp import apply_room, curve, fade_edges, fit_level, rng, room, soft_clip, trim_tail  # noqa: E402
from dsp import write_ogg  # noqa: E402
from monster_sounds import SR, clack, mix_at, pulse_voice, swept, tone_voice  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/entity/beast"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"


# =====================================================================================
# Sturmmöwe
# =====================================================================================

def _gull_note(seed: int, f0: float, dur: float, shape) -> np.ndarray:
    """Eine Silbe des Möwenrufs: ein heller, rauer, gleitender Laut ("kjau")."""
    r = rng(seed)
    n = int(dur * SR)
    f = f0 * curve(n, shape)
    amp = curve(n, [(0, 0), (0.06, 1), (0.55, 0.85), (1, 0)])
    return pulse_voice(f, amp, seed, vowel="a", size=1.55, jitter=0.07, shimmer=0.25, sub=0.15, breath=0.18,
                       bright=6800 + r.uniform(-400, 400))


def gull_call(seed: int) -> np.ndarray:
    """Der lachende Ruf: ein langgezogenes "kjiiau", dann vier bis sechs kurze "kjau-kjau", jedes etwas tiefer."""
    r = rng(seed)
    y = np.zeros(int(2.2 * SR))
    t = 0.02
    base = r.uniform(820, 940)
    mix_at(y, _gull_note(seed, base * 1.15, 0.42, [(0, 0.95), (0.25, 1.32), (0.7, 1.12), (1, 0.82)]), t, 1.0)
    t += 0.46
    for k in range(4 + int(r.integers(0, 3))):
        f0 = base * (1.05 - k * 0.035) * r.uniform(0.97, 1.03)
        mix_at(y, _gull_note(seed + 10 + k, f0, r.uniform(0.15, 0.19), [(0, 0.92), (0.3, 1.22), (1, 0.8)]), t, 0.85 - k * 0.06)
        t += r.uniform(0.2, 0.25)
    return y


def gull_hurt(seed: int) -> np.ndarray:
    """Schmerz: ein schrilles, kurzes Kreischen."""
    return _gull_note(seed, rng(seed).uniform(1000, 1150), 0.26, [(0, 1.0), (0.2, 1.35), (1, 0.9)])


def gull_death(seed: int) -> np.ndarray:
    """Tod: ein langes, absinkendes Klagen."""
    return _gull_note(seed, rng(seed).uniform(900, 980), 0.7, [(0, 1.1), (0.15, 1.25), (1, 0.55)])


def gull_flap(seed: int) -> np.ndarray:
    """Flügelklatschen beim Auffliegen: drei, vier schnelle, helle Luftstöße."""
    r = rng(seed)
    y = np.zeros(int(0.8 * SR))
    for k in range(3 + int(r.integers(0, 2))):
        mix_at(y, swept(0.12, [(0, 700), (0.5, 1800), (1, 1100)], 1.0, [(0, 0), (0.3, 1), (1, 0)], seed + k, flicker=0.5,
                        flicker_rate=(50.0, 70.0)), k * r.uniform(0.13, 0.16), 1.0 - k * 0.12)
    return y


def gull_step(seed: int) -> np.ndarray:
    """Ein leiser Tritt mit Schwimmfüßen auf Sand oder Fels."""
    return swept(0.07, [(0, 1600), (1, 900)], 1.2, [(0, 0), (0.15, 1), (1, 0)], seed)


# =====================================================================================
# Greif
# =====================================================================================

def griffin_screech(seed: int) -> np.ndarray:
    """Der Schrei: ein durchdringendes "kriiieh" wie bei einem Adler, das hoch einsetzt, kurz steigt und rau abfällt –
    darunter ein Hauch Löwengrollen."""
    r = rng(seed)
    n = int(r.uniform(0.9, 1.15) * SR)
    y = np.zeros(int(1.6 * SR))
    f = r.uniform(1450, 1600) * curve(n, [(0, 0.9), (0.12, 1.12), (0.45, 1.05), (1, 0.7)])
    amp = curve(n, [(0, 0), (0.04, 1), (0.6, 0.8), (1, 0)])
    mix_at(y, pulse_voice(f, amp, seed, vowel="e", size=1.7, jitter=0.05, shimmer=0.22, sub=0.1, breath=0.3, bright=9000), 0.02, 1.0)
    # ein zweiter, etwas tieferer Teil ("kriieh-ah")
    n2 = int(0.35 * SR)
    f2 = f[-1] * curve(n2, [(0, 1.0), (0.3, 1.08), (1, 0.85)])
    mix_at(y, pulse_voice(f2, curve(n2, [(0, 0), (0.1, 0.7), (1, 0)]), seed + 1, vowel="a", size=1.6, jitter=0.08, shimmer=0.3,
                          breath=0.35, bright=8000), 0.02 + n / SR * 0.92, 0.7)
    nr = int(0.8 * SR)
    mix_at(y, pulse_voice(np.full(nr, r.uniform(85, 100)), curve(nr, [(0, 0), (0.2, 1), (1, 0)]), seed + 2, vowel="o", size=0.8,
                          jitter=0.18, shimmer=0.45, sub=0.4, breath=0.3, bright=1600), 0.1, 0.25)
    return y


def griffin_growl(seed: int) -> np.ndarray:
    """Drohen: ein tiefes, rollendes Löwengrollen, darüber ein heiseres Zischen aus dem Schnabel."""
    r = rng(seed)
    n = int(r.uniform(0.9, 1.2) * SR)
    f = r.uniform(70, 82) * curve(n, [(0, 0.9), (0.3, 1.1), (1, 0.85)])
    y = pulse_voice(f, curve(n, [(0, 0), (0.15, 1), (0.7, 0.85), (1, 0)]), seed, vowel="o", size=0.75, jitter=0.22, shimmer=0.5,
                    sub=0.45, breath=0.35, bright=1800)
    hiss = swept(n / SR, [(0, 2600), (1, 3600)], 1.2, [(0, 0), (0.3, 0.6), (1, 0)], seed + 3)
    out = np.zeros(max(len(y), len(hiss)))
    mix_at(out, y, 0.0, 1.0)
    mix_at(out, hiss, 0.0, 0.25)
    return out


def griffin_wings(seed: int) -> np.ndarray:
    """Schwingenschläge: zwei, drei tiefe, wuchtige Luftstöße mit flatternden Federn."""
    r = rng(seed)
    y = np.zeros(int(1.2 * SR))
    for k in range(2 + int(r.integers(0, 2))):
        mix_at(y, swept(0.34, [(0, 180), (0.45, 520), (1, 260)], 1.2, [(0, 0), (0.35, 1), (1, 0)], seed + k, flicker=0.4,
                        flicker_rate=(26.0, 40.0)), k * r.uniform(0.32, 0.38), 1.0 - k * 0.15)
    return y


def griffin_hurt(seed: int) -> np.ndarray:
    """Schmerz: ein kurzer, schriller Schrei, der in ein Fauchen kippt."""
    r = rng(seed)
    n = int(0.3 * SR)
    f = r.uniform(1300, 1450) * curve(n, [(0, 1.0), (0.2, 1.18), (1, 0.75)])
    y = np.zeros(int(0.6 * SR))
    mix_at(y, pulse_voice(f, curve(n, [(0, 0), (0.05, 1), (1, 0)]), seed, vowel="e", size=1.6, jitter=0.1, shimmer=0.35, breath=0.4,
                          bright=8500), 0.0, 1.0)
    mix_at(y, swept(0.25, [(0, 2000), (1, 3200)], 1.1, [(0, 0), (0.2, 0.8), (1, 0)], seed + 1), 0.22, 0.5)
    return y


def griffin_death(seed: int) -> np.ndarray:
    """Tod: ein langer, brechender Schrei, ein letztes Grollen, die Schwingen sinken raschelnd zusammen."""
    r = rng(seed)
    n = int(1.1 * SR)
    f = r.uniform(1350, 1450) * curve(n, [(0, 1.05), (0.15, 1.15), (1, 0.45)])
    y = np.zeros(int(2.2 * SR))
    mix_at(y, pulse_voice(f, curve(n, [(0, 0), (0.05, 1), (0.6, 0.7), (1, 0)]), seed, vowel="a", size=1.5, jitter=0.12, shimmer=0.4,
                          breath=0.4, bright=7000), 0.0, 1.0)
    mix_at(y, griffin_growl(seed + 5), 0.8, 0.5)
    mix_at(y, swept(0.5, [(0, 900), (1, 300)], 1.4, [(0, 0), (0.3, 0.8), (1, 0)], seed + 7, flicker=0.5), 1.4, 0.4)
    return y


def griffin_strike(seed: int) -> np.ndarray:
    """Der Hieb mit den Krallen: ein scharfes Zischen durch die Luft und ein trockener Schlag."""
    r = rng(seed)
    y = np.zeros(int(0.5 * SR))
    mix_at(y, swept(0.16, [(0, 1200), (1, 3400)], 0.7, [(0, 0), (0.6, 1), (1, 0)], seed), 0.0, 0.8)
    mix_at(y, clack([(700, 1.0), (1500, 0.6), (2600, 0.3)], 0.03, seed + 1, q=10), 0.14 + r.uniform(0, 0.02), 0.9)
    return y


def griffin_step(seed: int) -> np.ndarray:
    """Ein Tritt: weiche Pranke, harte Krallen auf Stein."""
    r = rng(seed)
    y = np.zeros(int(0.25 * SR))
    mix_at(y, swept(0.1, [(0, 600), (1, 300)], 1.2, [(0, 0), (0.2, 1), (1, 0)], seed), 0.0, 0.8)
    mix_at(y, clack([(2400, 1.0), (3800, 0.5)], 0.01, seed + 1, q=18), 0.03 + r.uniform(0, 0.02), 0.35)
    return y


# =====================================================================================
# Flamingo (0.86)
# =====================================================================================

def _honk(seed: int, f0: float, dur: float, shape, vowel: str = "a") -> np.ndarray:
    """Ein nasaler Gänselaut: tiefe Pulse, enger Rachen ("ä"), etwas Rauheit."""
    r = rng(seed)
    n = int(dur * SR)
    f = f0 * curve(n, shape)
    amp = curve(n, [(0, 0), (0.08, 1), (0.6, 0.9), (1, 0)])
    return pulse_voice(f, amp, seed, vowel=vowel, size=1.25, jitter=0.05, shimmer=0.18, sub=0.3, breath=0.08,
                       bright=3200 + r.uniform(-200, 200))


def flamingo_call(seed: int) -> np.ndarray:
    """Das Schnattern einer Schar: zwei bis vier kurze, nasale "ga-gag", dazwischen ein längeres "gaaag"."""
    r = rng(seed)
    y = np.zeros(int(1.6 * SR))
    t = 0.02
    base = r.uniform(300, 360)
    for k in range(2 + int(r.integers(0, 3))):
        long = k == 1
        dur = r.uniform(0.24, 0.3) if long else r.uniform(0.09, 0.12)
        shape = [(0, 0.95), (0.3, 1.12), (1, 0.85)] if long else [(0, 1.0), (0.5, 1.1), (1, 0.9)]
        mix_at(y, _honk(seed + k, base * r.uniform(0.95, 1.08), dur, shape), t, 1.0 if long else 0.8)
        t += dur + r.uniform(0.06, 0.12)
    return y


def flamingo_flap(seed: int) -> np.ndarray:
    """Aufflattern: weiche, tiefere Flügelschläge."""
    r = rng(seed)
    y = np.zeros(int(1.0 * SR))
    for k in range(3 + int(r.integers(0, 2))):
        mix_at(y, swept(0.16, [(0, 450), (0.5, 1100), (1, 700)], 1.1, [(0, 0), (0.3, 1), (1, 0)], seed + k, flicker=0.4,
                        flicker_rate=(40.0, 55.0)), k * r.uniform(0.17, 0.2), 1.0 - k * 0.1)
    return y


def flamingo_step(seed: int) -> np.ndarray:
    """Ein Schritt im flachen Wasser: ein leises Plätschern."""
    r = rng(seed)
    y = swept(0.12, [(0, 900), (0.4, 2400), (1, 1500)], 1.4, [(0, 0), (0.1, 1), (0.4, 0.5), (1, 0)], seed, flicker=0.6,
              flicker_rate=(30.0, 60.0))
    return y * r.uniform(0.8, 1.0)


# =====================================================================================
# Ausgabe
# =====================================================================================

# Name: (Funktion, Varianten, Raum (Nachhall s, Anteil) oder None, Ziel-Pegel dBFS)
SYNTH = {
    "storm_gull_call": (gull_call, 3, (1.2, 0.18), -15.0),
    "storm_gull_hurt": (gull_hurt, 3, (0.5, 0.1), -15.0),
    "storm_gull_death": (gull_death, 1, (0.8, 0.15), -15.0),
    "storm_gull_flap": (gull_flap, 3, (0.4, 0.08), -19.0),
    "storm_gull_step": (gull_step, 4, None, -26.0),
    "griffin_screech": (griffin_screech, 3, (2.4, 0.35), -13.0),
    "griffin_growl": (griffin_growl, 3, (0.9, 0.15), -15.0),
    "griffin_wings": (griffin_wings, 3, (0.9, 0.15), -16.0),
    "griffin_hurt": (griffin_hurt, 3, (0.8, 0.15), -14.0),
    "griffin_death": (griffin_death, 1, (1.8, 0.3), -14.0),
    "griffin_strike": (griffin_strike, 3, (0.5, 0.1), -15.0),
    "griffin_step": (griffin_step, 4, None, -23.0),
    "flamingo_call": (flamingo_call, 4, (0.9, 0.15), -15.0),
    "flamingo_flap": (flamingo_flap, 3, (0.4, 0.08), -18.0),
    "flamingo_step": (flamingo_step, 4, None, -26.0),
}

# Wesen: {Klang: Reichweite in Blöcken}
REACH = {
    "storm_gull": {"call": 48, "hurt": 20, "death": 24, "flap": 16, "step": 10},
    "griffin": {"screech": 96, "growl": 24, "wings": 32, "hurt": 32, "death": 48, "strike": 20, "step": 14},
    "flamingo": {"call": 40, "flap": 16, "step": 10},
}

EVENTS = {f"entity.{creature}.{sound}": ([f"{creature}_{sound}"], reach)
          for creature, sounds in REACH.items() for sound, reach in sounds.items()}


def files_of(name: str) -> list[str]:
    return [f"kingdomsatwar:entity/beast/{name}{i + 1}" for i in range(SYNTH[name][1])]


def render(name: str, preview: Path | None) -> list[str]:
    fn, count, space, level = SYNTH[name]
    for i in range(count):
        x = fn(7000 + i * 41 + zlib.crc32(name.encode()) % 997)
        if space is not None:
            x = apply_room(x, room(SR, space[0], 1100 + i, stereo=False, damping=7000), space[1])
        x = trim_tail(x, SR, -55.0)
        x = fade_edges(x, SR, 0.002, 0.04)
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
