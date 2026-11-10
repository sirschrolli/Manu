#!/usr/bin/env python3
"""
Die Musik der Schöpfung (0.75) – komponiert im Code (Partitur wie music.py) und mit dem Soundfont FluidR3_GM gespielt:

* ``music/creation`` – die stille Musik des Hofs: ein schwebender Chor in D-Dur (lydisch), Harfe, Celesta, Glockenspiel,
  weiche Streicher und eine Flöte (66 BPM, etwa zwei Minuten, als Schleife);
* ``music/creation_battle`` – der Kampf gegen Schrolli den Schöpfer: Orgel, Chor, Streicher-Ostinato, Blechbläser mit dem
  Thema des Schöpfers, Pauken und Trommeln in d-Moll (136 BPM);
* ``music/creation_true`` – die Wahre Gestalt: dasselbe Thema entfesselt, schneller, höher (e-Moll, 152 BPM), mit
  Orchesterschlägen, Fanfaren und doppelter Trommel;
* ``music/creation_almighty`` (seit 0.78) – Schrolli der Allmächtige im Herzen der Sonne: das Thema als Hymne (fis-Moll,
  164 BPM), Chor und Hörner eine Oktave höher, Trompeten in jedem Teil, Blech auf jedem Achtel, Trommeln in Sechzehnteln,
  Glocken und Orchesterschläge.

Aufruf (aus dem Projektordner):
    python3 tools/audio/creator_music.py --sf2 /pfad/zu/FluidR3_GM.sf2 [--only creation creation_battle creation_true creation_almighty]
"""
from __future__ import annotations

import argparse
import json
import math
import random
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))

import dsp  # noqa: E402
import music  # noqa: E402
from music import CHORDS, Score, bass_note, n, voice_chord  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent.parent
OUT = ROOT / "src/main/resources/assets/kingdomsatwar/sounds/music"
SOUNDS_JSON = ROOT / "src/main/resources/assets/kingdomsatwar/sounds.json"
SR = 44100

CHORDS.update({
    "Am": ("A", ["A", "C", "E"]), "Em": ("E", ["E", "G", "B"]), "G": ("G", ["G", "B", "D"]), "Bm": ("B", ["B", "D", "F#"]),
    "Dmaj7": ("D", ["D", "F#", "A", "C#"]), "Gmaj7": ("G", ["G", "B", "D", "F#"]), "Bm7": ("B", ["B", "D", "F#", "A"]),
    "Em7": ("E", ["E", "G", "B", "D"]), "Asus4": ("A", ["A", "D", "E"]), "Cmaj7": ("C", ["C", "E", "G", "B"]),
    "B": ("B", ["B", "D#", "F#"]), "E": ("E", ["E", "G#", "B"]), "Fmaj7": ("F", ["F", "A", "C", "E"]), "Cm": ("C", ["C", "Eb", "G"]),
    "F#": ("F#", ["F#", "A#", "C#"]), "F#m": ("F#", ["F#", "A", "C#"]), "Ab": ("Ab", ["Ab", "C", "Eb"]),
})

# GM-Programme
CHOIR, VOICE, ORGAN, STRINGS, SLOW_STRINGS, HARP, TIMPANI, CELESTA, GLOCK, BELLS = 52, 53, 19, 48, 49, 46, 47, 8, 9, 14
HORN, TRUMPET, TROMBONE, BRASS, HIT, FLUTE, PAD, CONTRABASS, PIZZ = 60, 56, 57, 61, 55, 73, 89, 43, 45


def transpose_chord(chord: str, semis: int) -> str:
    """Akkordname um Halbtöne verschieben (nur für die verwendeten Namen)."""
    if semis == 0:
        return chord
    names = ["C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"]
    root = CHORDS[chord][0]
    rest = chord[len(root):]
    idx = (music.NOTE[root] + semis) % 12
    new = names[idx] + rest
    if new not in CHORDS:
        pcs = [(music.NOTE[x] + semis) % 12 for x in CHORDS[chord][1]]
        CHORDS[new] = (names[idx], [names[p] for p in pcs])
    return new


# =====================================================================================
# Die stille Musik der Schöpfung
# =====================================================================================

def compose_creation(seed: int = 5) -> Score:
    sc = Score(66.0, seed)
    sc.track("choir", 0, CHOIR, pan=64, volume=96)
    sc.track("strings", 1, SLOW_STRINGS, pan=54, volume=90)
    sc.track("harp", 2, HARP, pan=80, volume=88)
    sc.track("celesta", 3, CELESTA, pan=44, volume=84)
    sc.track("glock", 4, GLOCK, pan=70, volume=70)
    sc.track("pad", 5, PAD, pan=64, volume=80)
    sc.track("flute", 6, FLUTE, pan=58, volume=82)
    sc.track("bass", 7, CONTRABASS, pan=64, volume=80)
    prog = ["Dmaj7", "Dmaj7", "Bm7", "Bm7", "Gmaj7", "Gmaj7", "Asus4", "A",
            "Dmaj7", "Dmaj7", "Em7", "Em7", "Gmaj7", "Gmaj7", "Asus4", "A"] * 2
    r = random.Random(seed)
    prev = None
    penta = [n("D5"), n("E5"), n("F#5"), n("A5"), n("B5"), n("D6"), n("E6"), n("F#6")]
    melody = [(0, 3, "F#5"), (3, 1, "E5"), (4, 2, "D5"), (6, 2, "A4"), (8, 3, "B4"), (11, 1, "C#5"), (12, 4, "D5"),
              (16, 3, "F#5"), (19, 1, "G5"), (20, 2, "A5"), (22, 2, "F#5"), (24, 3, "E5"), (27, 1, "D5"), (28, 4, "E5")]
    for bar, chord in enumerate(prog):
        b = bar * 4.0
        voices = voice_chord(chord, prev, 55, 76, 4)
        prev = voices
        swell = 48 + int(10 * math.sin(bar / len(prog) * math.pi * 2.0 + 1.0))
        for p in voices:
            sc.note("choir", b, 4.0, p, swell)
            sc.note("pad", b, 4.0, p - 12, 40)
        sc.note("strings", b, 4.0, bass_note(chord, 38), 46)
        sc.note("strings", b, 4.0, voices[1] - 12, 38)
        sc.note("bass", b, 4.0, bass_note(chord, 33), 50)
        # Harfe: aufsteigendes Arpeggio in Achteln über zwei Oktaven
        pcs = music.chord_pcs(chord)
        notes = sorted({p for p in range(n("D4"), n("D6")) if p % 12 in pcs})
        for k in range(8):
            sc.note("harp", b + k * 0.5, 1.5, notes[(k * 2 + bar) % len(notes)], 44 + (k % 4 == 0) * 10)
        # Celesta: vereinzelte Funken
        beat = 0.0
        while beat < 4.0:
            if r.random() < 0.45:
                sc.note("celesta", b + beat, 1.0, r.choice(penta), r.randint(36, 50))
            beat += r.choice([1.0, 1.5, 2.0])
        if bar % 4 == 0:
            sc.note("glock", b, 3.0, voices[-1] + 12, 52)
    # die Flöte singt in der zweiten Hälfte
    start = 16 * 4.0
    for beat, dur, name in melody:
        sc.note("flute", start + beat, dur * 0.95, n(name), 62)
        sc.note("flute", start + 32.0 + beat, dur * 0.95, n(name) + 12 if beat > 16 else n(name), 56)
    sc.ramp("choir", 0.0, 8.0, 40, 110, ctrl=11)
    sc.ramp("strings", 0.0, 8.0, 40, 110, ctrl=11)
    return sc


# =====================================================================================
# Der Kampf gegen den Schöpfer (und die Wahre Gestalt)
# =====================================================================================

# Das Thema des Schöpfers (d-Moll, je Takt vier Schläge): (Beat, Dauer, Note)
THEME = [
    (0, 1.5, "D5"), (1.5, 0.5, "A4"), (2, 1, "D5"), (3, 1, "F5"),
    (4, 1.5, "E5"), (5.5, 0.5, "D5"), (6, 1, "C5"), (7, 1, "A4"),
    (8, 1.5, "Bb4"), (9.5, 0.5, "C5"), (10, 1, "D5"), (11, 1, "F5"),
    (12, 3, "E5"), (15, 1, "C#5"),
    (16, 1.5, "D5"), (17.5, 0.5, "A4"), (18, 1, "D5"), (19, 1, "F5"),
    (20, 1.5, "G5"), (21.5, 0.5, "F5"), (22, 1, "E5"), (23, 1, "D5"),
    (24, 1, "C#5"), (25, 1, "E5"), (26, 1, "A5"), (27, 1, "G5"),
    (28, 4, "D5"),
]
THEME_B = [
    (0, 2, "G5"), (2, 1, "F5"), (3, 1, "D5"),
    (4, 2, "F5"), (6, 1, "E5"), (7, 1, "D5"),
    (8, 2, "D5"), (10, 1, "F5"), (11, 1, "Bb5"),
    (12, 3, "A5"), (15, 1, "E5"),
    (16, 2, "G5"), (18, 1, "A5"), (19, 1, "Bb5"),
    (20, 2, "A5"), (22, 1, "F5"), (23, 1, "D5"),
    (24, 2, "Eb5"), (26, 1, "D5"), (27, 1, "C5"),
    (28, 4, "A4"),
]


def compose_battle(level: int, seed: int = 11) -> Score:
    """level 0: der Schöpfer, 1: die Wahre Gestalt, 2: der Allmächtige (seit 0.78)."""
    true_form = level >= 1
    almighty = level >= 2
    semis = 4 if almighty else 2 if true_form else 0
    bpm = 164.0 if almighty else 152.0 if true_form else 136.0
    sc = Score(bpm, seed)
    sc.track("organ", 0, ORGAN, pan=64, volume=88)
    sc.track("choir", 1, CHOIR, pan=64, volume=100)
    sc.track("strings", 2, STRINGS, pan=40, volume=96)
    sc.track("lowstr", 3, STRINGS, pan=88, volume=92)
    sc.track("horns", 4, HORN, pan=76, volume=100)
    sc.track("trumpets", 5, TRUMPET, pan=52, volume=92)
    sc.track("brass", 6, BRASS, pan=64, volume=96)
    sc.track("timpani", 7, TIMPANI, pan=64, volume=110)
    sc.track("bells", 8, BELLS, pan=60, volume=80)
    sc.track("drums", 9, 0, drums=True, pan=64, volume=104)
    sc.track("bass", 10, CONTRABASS, pan=64, volume=96)
    sc.track("hits", 11, HIT, pan=64, volume=96)
    sc.track("voices", 12, VOICE, pan=64, volume=90)
    r = random.Random(seed)

    def T(note: str) -> int:
        return n(note) + semis

    intro = ["Dm"] * 4
    part_a = ["Dm", "Bb", "C", "Am", "Dm", "Gm", "A", "A"] * 2
    part_b = ["Gm", "Dm", "Bb", "A", "Gm", "Dm", "Eb", "A"] * 2
    bridge = ["Bb", "C", "Dm", "Dm", "Bb", "C", "A", "A"]
    sections = [("intro", intro), ("a", part_a), ("b", part_b), ("a2", part_a), ("bridge", bridge)]
    bar = 0
    prev = None
    for name, prog in sections:
        for i, raw in enumerate(prog):
            chord = transpose_chord(raw, semis)
            b = bar * 4.0
            voices = voice_chord(chord, prev, 55, 74, 4)
            prev = voices
            root = bass_note(chord, 36)
            pcs = music.chord_pcs(chord)
            full = name != "intro"
            loud = 90 if name in ("a2", "bridge") else 80
            if true_form:
                loud = min(118, loud + 14)
            if almighty:
                loud = min(124, loud + 6)
            # Orgel und Chor: getragene Akkorde
            for p in voices:
                sc.note("organ", b, 4.0, p - 12, 62 if full else 50 + i * 6)
                up = 12 if true_form and name == "a2" or almighty and full else 0
                sc.note("choir", b, 4.0, p + up, loud - 12 if full else 46 + i * 8)
            sc.note("organ", b, 4.0, root - 12, 70)
            # Bass in Achteln (punktiert)
            if full:
                for k, (at, d) in enumerate([(0, 0.75), (0.75, 0.75), (1.5, 0.5), (2, 0.75), (2.75, 0.75), (3.5, 0.5)]):
                    sc.note("bass", b + at, d, root + (12 if k == 4 else 0), 84)
                    sc.note("lowstr", b + at, d, root + 12, 70)
            # Streicher-Ostinato in Sechzehnteln
            if full:
                arp = sorted({p for p in range(n("A4") + semis - 3, n("E6") + semis) if p % 12 in pcs})
                pattern = [0, 2, 1, 2, 0, 3, 1, 2] if name != "b" else [0, 1, 2, 3, 2, 1, 0, 1]
                steps = 16
                for k in range(steps):
                    p = arp[min(len(arp) - 1, pattern[k % len(pattern)] + (k // 8))]
                    sc.note("strings", b + k * 0.25, 0.22, p, 62 + (k % 4 == 0) * 14 + (8 if true_form else 0))
            # Blech: Akzente auf 1 und 3+
            if full:
                accents = (0.0, 0.5, 1.5, 2.0, 2.5, 3.5) if almighty else (0.0, 1.5, 2.5, 3.5) if true_form else (0.0, 2.5)
                for at in accents:
                    for p in voices[:3]:
                        sc.note("brass", b + at, 0.45, p - 12, loud - 6)
            # Pauken und Trommeln
            if name == "intro":
                for k in range(16):
                    sc.note("timpani", b + k * 0.25, 0.25, root - 12, 40 + i * 14 + k * 2)
                if i == 3:
                    sc.note("drums", b + 3.5, 0.5, 49, 110)
            else:
                sc.note("timpani", b, 1.0, root - 12, 100)
                sc.note("timpani", b + 2.0, 1.0, root - 5, 92)
                kick = [0, 1.5, 2, 3.5] if not true_form else [0, 0.75, 1.5, 2, 2.75, 3.5]
                if almighty:
                    kick = [0, 0.5, 0.75, 1.5, 2, 2.5, 2.75, 3.5]
                for at in kick:
                    sc.note("drums", b + at, 0.3, 36, 104)
                for at in (1.0, 3.0):
                    sc.note("drums", b + at, 0.3, 38, 96)
                if true_form:
                    steps = 16 if almighty else 8
                    for k in range(steps):
                        sc.note("drums", b + k * 4.0 / steps, 0.2, 42, 56 + (k % 2) * 12)
                if i % 4 == 0 or almighty and i % 2 == 0:
                    sc.note("drums", b, 1.0, 49, 112)
                    sc.note("hits", b, 0.5, voices[0], 100 if true_form else 80)
                if i % 8 == 7:
                    for k, tom in enumerate((50, 48, 47, 45, 43, 41)):
                        sc.note("drums", b + 2.0 + k * 0.33, 0.3, tom, 90 + k * 3)
            if (i % 8 == 0 or almighty and i % 4 == 0) and full:
                sc.note("bells", b, 3.0, voices[-1], 76 if almighty else 70)
            bar += 1
    # das Thema: Hörner (Teil A), Chor (Teil B), Trompeten in der Wiederholung
    starts = {"a": 4, "b": 20, "a2": 36}
    for sec, start in starts.items():
        theme = THEME_B if sec == "b" else THEME
        for rep in (0, 1):
            for beat, dur, note in theme:
                at = (start + rep * 8) * 4.0 + beat
                p = T(note)
                if sec == "b":
                    sc.note("voices", at, dur * 0.95, p, 96 if true_form else 88)
                    sc.note("choir", at, dur * 0.95, p + 12, 70)
                    if almighty:
                        sc.note("trumpets", at, dur * 0.9, p, 90)
                else:
                    sc.note("horns", at, dur * 0.9, p - 12 if not true_form else p, 104)
                    if sec == "a2" or true_form:
                        sc.note("trumpets", at, dur * 0.9, p, 96)
                    if almighty:
                        sc.note("voices", at, dur * 0.95, p + 12, 84)
    # Brücke: Fanfare der Trompeten
    fanfare = [(0, 0.5, "A4"), (0.5, 0.5, "A4"), (1, 1, "D5"), (2, 1, "F5"), (3, 1, "A5"), (4, 2, "G5"), (6, 2, "E5")]
    for rep in range(4):
        for beat, dur, note in fanfare:
            sc.note("trumpets", (52 + rep * 2) * 4.0 + beat, dur * 0.9, T(note) + (rep // 2) * 2, 100)
    sc.ramp("choir", 0.0, 16.0, 50, 115, ctrl=11)
    return sc


# =====================================================================================
# Mischung
# =====================================================================================

# seit 0.76 weicher: Trompeten, Glocken, Celesta, Glockenspiel und Orchesterschläge leiser, der Chor etwas zurück
LEVELS = {
    "choir": -6.5, "voices": -8.0, "organ": -11.0, "strings": -8.0, "lowstr": -11.0, "horns": -6.0, "trumpets": -12.0, "brass": -10.0,
    "timpani": -7.0, "bells": -18.0, "drums": -8.0, "bass": -10.0, "hits": -14.0,
    "harp": -9.0, "celesta": -16.0, "glock": -20.0, "pad": -13.0, "flute": -10.0,
}
SENDS = {"choir": 0.45, "voices": 0.45, "organ": 0.4, "strings": 0.3, "lowstr": 0.25, "horns": 0.35, "trumpets": 0.3, "brass": 0.25,
         "timpani": 0.3, "bells": 0.5, "drums": 0.15, "bass": 0.1, "hits": 0.35, "harp": 0.4, "celesta": 0.5, "glock": 0.5, "pad": 0.4,
         "flute": 0.4}


def render(sc: Score, sf2: Path, name: str, target_lufs: float, rt60: float) -> None:
    stems = music.render_stems(sc, sf2, SR, tail_seconds=3.0)
    total = int(sc.seconds(sc.length_beats) * SR) + int(2.5 * SR)
    mix = np.zeros((total, 2), np.float32)
    send = np.zeros_like(mix)
    for stem, st in stems.items():
        st = st[:total]
        loud = dsp.lufs(st, SR)
        if not math.isfinite(loud):
            continue
        g = dsp.undb(LEVELS.get(stem, -10.0) - 23.0 - loud)
        st = st * g
        mix[:len(st)] += st
        send[:len(st)] += st * SENDS.get(stem, 0.3)
    hall = dsp.impulse_response(SR, rt60=rt60, predelay=0.03, stereo=True, damping_hz=5000, seed=31, early=12)
    wet = dsp.reverb(send, hall, wet=1.0)[:len(mix)] - send
    master = mix + 0.85 * wet
    # warm statt grell: die Höhen sanft zurück, unten etwas mehr Fülle
    master = dsp.eq(master, SR, [("highpass", 30, 0.707, 0), ("lowshelf", 140, 0.707, 1.0), ("highshelf", 5000, 0.707, -4.0),
                                 ("lowpass", 11000, 0.707, 0)])
    master = dsp.fade(master, SR, fade_in=0.4, fade_out=2.4)
    master = dsp.normalize(master, SR, target_lufs, ceiling_db=-1.5)
    master = dsp.limiter(master, SR, -1.0)
    OUT.mkdir(parents=True, exist_ok=True)
    dsp.write_ogg(master, SR, OUT / f"{name}.ogg", quality=5)
    print(f"  {name}: {len(master) / SR:.1f} s")


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--sf2", type=Path, required=True)
    ap.add_argument("--only", nargs="*")
    args = ap.parse_args()
    todo = {
        "creation": lambda: render(compose_creation(), args.sf2, "creation", -23.0, 3.6),
        "creation_battle": lambda: render(compose_battle(0), args.sf2, "creation_battle", -20.5, 2.6),
        "creation_true": lambda: render(compose_battle(1), args.sf2, "creation_true", -20.0, 2.4),
        "creation_almighty": lambda: render(compose_battle(2), args.sf2, "creation_almighty", -19.5, 2.3),
    }
    for key, fn in todo.items():
        if not args.only or key in args.only:
            fn()
    data = json.loads(SOUNDS_JSON.read_text(encoding="utf-8"))
    for key in todo:
        data[f"music.{key}"] = {"sounds": [{"name": f"kingdomsatwar:music/{key}", "stream": True}]}
    SOUNDS_JSON.write_text(json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print("Musik der Schöpfung geschrieben")


if __name__ == "__main__":
    main()
