"""
Eigene Intro-Musik: Komposition im Code (MIDI über mido) und Wiedergabe mit tinysoundfont
und dem Soundfont FluidR3_GM. Danach Mischung mit Hall, Lautheit und Blenden (siehe build_audio.py).

Tonart d-Moll, 72 BPM, 4/4-Takt. Die Abschnitte richten sich nach dem Zeitplan der Erzählung:
  intro    – leiser Beginn: tiefe Streicher (Orgelpunkt), Harfe
  world    – Welt und Königreiche: Streicherflächen, Harfen-Arpeggien, Flöte mit dem Thema
  dragons  – die Drachen erwachen: Tremolo-Streicher, Posaunen/Tuba, Chor, Pauken-Wirbel (Steigerung)
  wonder   – Eier und Erze: Harfe, Celesta, Holzbläser, weiche Streicher (geheimnisvoll)
  knights  – Ritter und Burg: Hörner mit dem Hauptthema, Pauken, kleine Trommel, Streicher-Ostinato
  finale   – Auflösung nach D-Dur auf der letzten Zeile, voller Klang, dann Ausklang
"""
from __future__ import annotations

import math
import random
from dataclasses import dataclass, field
from pathlib import Path

import mido
import numpy as np
import tinysoundfont

# ---------------------------------------------------------------------------
# Noten und Akkorde
# ---------------------------------------------------------------------------

NOTE = {"C": 0, "C#": 1, "Db": 1, "D": 2, "D#": 3, "Eb": 3, "E": 4, "F": 5, "F#": 6, "Gb": 6,
        "G": 7, "G#": 8, "Ab": 8, "A": 9, "A#": 10, "Bb": 10, "B": 11}


def n(name: str) -> int:
    """'D4' -> 62 (MIDI)."""
    pitch, octave = name[:-1], int(name[-1])
    return 12 * (octave + 1) + NOTE[pitch]


CHORDS = {
    "Dm": ("D", ["D", "F", "A"]), "Gm": ("G", ["G", "Bb", "D"]), "Bb": ("Bb", ["Bb", "D", "F"]),
    "F": ("F", ["F", "A", "C"]), "C": ("C", ["C", "E", "G"]), "A": ("A", ["A", "C#", "E"]),
    "Asus": ("A", ["A", "D", "E"]), "A7": ("A", ["A", "C#", "E", "G"]), "D": ("D", ["D", "F#", "A"]),
    "Bbmaj7": ("Bb", ["Bb", "D", "F", "A"]), "Dm/C": ("C", ["C", "D", "F", "A"]), "Gm6": ("G", ["G", "Bb", "D", "E"]),
}


def chord_pcs(chord: str) -> list[int]:
    return [NOTE[x] for x in CHORDS[chord][1]]


def root_pc(chord: str) -> int:
    return NOTE[CHORDS[chord][0]]


def voice_chord(chord: str, prev: list[int] | None, low: int = 55, high: int = 76, voices: int = 4) -> list[int]:
    """Enge Lage (Akkordtöne aufwärts geschichtet, Grundton bei Bedarf verdoppelt) mit möglichst
    kleinen Stimmbewegungen zum vorigen Akkord."""
    pcs = chord_pcs(chord)
    cands = []
    for start in range(low, high + 1):
        if start % 12 not in pcs:
            continue
        k = pcs.index(start % 12)
        notes = [start]
        while len(notes) < voices:
            k = (k + 1) % len(pcs)
            p = notes[-1] + 1
            while p % 12 != pcs[k]:
                p += 1
            notes.append(p)
        if notes[-1] <= high:
            cands.append(notes)
    if not cands:
        r = root_pc(chord)
        base = low + ((r - low) % 12)
        return [base + x for x in (0, 4, 7, 12)][:voices]
    if prev is None:
        target = (low + high) / 2
        return min(cands, key=lambda c: abs(sum(c) / len(c) - target))
    return min(cands, key=lambda c: sum(abs(a - b) for a, b in zip(c, sorted(prev))))


def bass_note(chord: str, octave_low: int = 33) -> int:
    r = root_pc(chord)
    p = octave_low + ((r - octave_low) % 12)
    return p


# ---------------------------------------------------------------------------
# Partitur
# ---------------------------------------------------------------------------

@dataclass
class Track:
    name: str
    channel: int
    program: int
    bank: int = 0
    drums: bool = False
    pan: int = 64
    volume: int = 100
    events: list = field(default_factory=list)   # (beat, kind, a, b) kind: on/off/cc


class Score:
    def __init__(self, bpm: float, seed: int = 11):
        self.bpm = bpm
        self.tracks: dict[str, Track] = {}
        self.rng = random.Random(seed)
        self.length_beats = 0.0

    def track(self, name, channel, program, bank=0, drums=False, pan=64, volume=100) -> Track:
        t = Track(name, channel, program, bank, drums, pan, volume)
        self.tracks[name] = t
        return t

    def note(self, track: str, beat: float, dur: float, pitch: int, vel: int, human: bool = True) -> None:
        t = self.tracks[track]
        jit = max(-0.02, min(0.02, self.rng.gauss(0, 0.009))) * self.bpm / 60 if human else 0.0
        v = int(max(1, min(127, vel + (self.rng.randint(-4, 4) if human else 0))))
        on = max(0.0, beat + jit)
        t.events.append((on, 1, pitch, v))
        t.events.append((on + dur * 0.98, 0, pitch, 0))
        self.length_beats = max(self.length_beats, beat + dur)

    def cc(self, track: str, beat: float, ctrl: int, value: int) -> None:
        self.tracks[track].events.append((beat, 2, ctrl, int(max(0, min(127, value)))))

    def ramp(self, track: str, b0: float, b1: float, v0: float, v1: float, ctrl: int = 11, steps_per_beat: int = 4,
             curve: float = 1.0) -> None:
        steps = max(1, int((b1 - b0) * steps_per_beat))
        for i in range(steps + 1):
            x = i / steps
            self.cc(track, b0 + (b1 - b0) * x, ctrl, v0 + (v1 - v0) * (x ** curve))

    def seconds(self, beat: float) -> float:
        return beat * 60.0 / self.bpm

    def to_midi(self, path: Path) -> None:
        """Speichert die Partitur als MIDI-Datei (zum Nachhören/Bearbeiten in anderen Programmen)."""
        tpb = 480
        mid = mido.MidiFile(ticks_per_beat=tpb)
        tempo = mido.MidiTrack()
        tempo.append(mido.MetaMessage("set_tempo", tempo=int(60_000_000 / self.bpm), time=0))
        mid.tracks.append(tempo)
        for t in self.tracks.values():
            tr = mido.MidiTrack()
            tr.append(mido.MetaMessage("track_name", name=t.name, time=0))
            ch = 9 if t.drums else t.channel
            if not t.drums:
                tr.append(mido.Message("program_change", program=t.program, channel=ch, time=0))
            tr.append(mido.Message("control_change", control=10, value=t.pan, channel=ch, time=0))
            tr.append(mido.Message("control_change", control=7, value=t.volume, channel=ch, time=0))
            last = 0
            for beat, kind, a, b in sorted(t.events, key=lambda e: (e[0], e[1])):
                tick = int(round(beat * tpb))
                dt = max(0, tick - last)
                last = max(last, tick)
                if kind == 1:
                    tr.append(mido.Message("note_on", note=a, velocity=b, channel=ch, time=dt))
                elif kind == 0:
                    tr.append(mido.Message("note_off", note=a, velocity=0, channel=ch, time=dt))
                else:
                    tr.append(mido.Message("control_change", control=a, value=b, channel=ch, time=dt))
            mid.tracks.append(tr)
        mid.save(path)


# ---------------------------------------------------------------------------
# Komposition
# ---------------------------------------------------------------------------

THEME = [  # (Beat im 8-Takt-Thema, Dauer, Note)
    (0, 2, "D5"), (2, 1, "A4"), (3, 1, "D5"),
    (4, 1.5, "F5"), (5.5, 0.5, "E5"), (6, 1, "D5"), (7, 1, "C5"),
    (8, 2, "Bb4"), (10, 1, "A4"), (11, 1, "G4"),
    (12, 4, "A4"),
    (16, 2, "D5"), (18, 1, "A4"), (19, 1, "D5"),
    (20, 1.5, "F5"), (21.5, 0.5, "G5"), (22, 2, "A5"),
    (24, 1, "G5"), (25, 1, "F5"), (26, 1, "E5"), (27, 1, "C#5"),
    (28, 4, "D5"),
]
THEME_CHORDS = ["Dm", "Bb", "Gm", "A", "Dm", "F", "Gm", "Dm"]   # ein Akkord pro Takt (Takt 7: Gm -> A)


def progression(section: str, bars: int) -> list[str]:
    base = {
        "intro": ["Dm"],
        "world": ["Dm", "Bb", "Gm", "A", "Dm", "F", "C", "A"],
        "dragons": ["Dm", "Dm", "Bb", "Gm", "Dm/C", "Bb", "Gm", "A"],
        "wonder": ["Bbmaj7", "F", "Gm", "Dm", "Bbmaj7", "C", "Gm6", "Asus"],
        "knights": THEME_CHORDS,
        "finale": ["Bb", "Gm", "A"],
    }[section]
    if section == "intro":
        return ["Dm"] * bars
    if section == "knights":
        if bars >= 8:
            return [THEME_CHORDS[i % 8] for i in range(bars)]
        if bars >= 4:
            return ["Dm", "Bb", "Gm", "A"] if bars == 4 else (["Dm", "Bb", "Gm", "Dm/C", "Bb", "Gm"][:bars - 1] + ["A"])
        return {1: ["Dm"], 2: ["Dm", "A"]}.get(bars, ["Dm", "Bb", "A"])
    end = {"world": "A", "dragons": "A", "wonder": "A", "knights": "A", "finale": "A"}[section]
    seq = [base[i % len(base)] for i in range(bars)]
    if bars >= 2:
        seq[-1] = end
    if bars >= 3 and seq[-2] == seq[-1]:
        seq[-2] = "Dm/C"            # absteigender Bass statt doppeltem Akkord
    return seq


def compose(sections: list[tuple[str, int]], bpm: float = 72.0, outro_bars: int = 2, seed: int = 11) -> Score:
    """sections: Liste (Name, Takte) in Reihenfolge. Danach folgt der D-Dur-Schluss (outro_bars + 1 Takt)."""
    sc = Score(bpm, seed)
    sc.track("str_pad", 0, 49, pan=52, volume=100)      # Slow Strings
    sc.track("str_trem", 1, 44, pan=58, volume=100)     # Tremolo
    sc.track("str_ost", 2, 48, pan=44, volume=100)      # Strings (rhythmisch)
    sc.track("cello", 3, 42, pan=82, volume=100)
    sc.track("bass", 4, 43, pan=78, volume=100)         # Kontrabass
    sc.track("harp", 5, 46, pan=34, volume=100)
    sc.track("celesta", 6, 8, pan=92, volume=100)
    sc.track("flute", 7, 73, pan=50, volume=100)
    sc.track("oboe", 8, 68, pan=74, volume=100)
    sc.track("horns", 10, 60, pan=86, volume=100)
    sc.track("trombones", 11, 57, pan=70, volume=100)
    sc.track("tuba", 12, 58, pan=66, volume=100)
    sc.track("timpani", 13, 47, pan=64, volume=100)
    sc.track("choir", 14, 52, pan=64, volume=100)
    sc.track("bells", 15, 14, pan=60, volume=100)
    sc.track("perc", 9, 48, bank=128, drums=True, pan=64, volume=100)   # Orchestra Kit

    beat = 0.0
    prev_pad: list[int] | None = None
    for si, (sec, bars) in enumerate(sections):
        chords = progression(sec, bars)
        s0 = beat
        s1 = beat + bars * 4
        # ---------------- Dynamik je Abschnitt ----------------
        if sec == "intro":
            sc.ramp("cello", s0, s1, 70, 95); sc.ramp("bass", s0, s1, 70, 95); sc.ramp("harp", s0, s1, 90, 100)
        elif sec == "world":
            sc.ramp("str_pad", s0, s1, 82, 98); sc.ramp("harp", s0, s1, 100, 104); sc.ramp("flute", s0, s1, 92, 100)
            sc.ramp("cello", s0, s0 + 4, 95, 90); sc.ramp("bass", s0, s0 + 4, 95, 92)
        elif sec == "dragons":
            sc.ramp("str_trem", s0, s1, 62, 122, curve=1.3); sc.ramp("trombones", s0, s1, 70, 118, curve=1.2)
            sc.ramp("tuba", s0, s1, 70, 115, curve=1.2); sc.ramp("choir", s0, s1, 70, 112)
            sc.ramp("timpani", s0, s1, 80, 125); sc.ramp("bass", s0, s1, 90, 115); sc.ramp("cello", s0, s1, 88, 112)
        elif sec == "wonder":
            sc.ramp("str_pad", s0, s1, 92, 80); sc.ramp("harp", s0, s1, 104, 98); sc.ramp("celesta", s0, s1, 96, 90)
            sc.ramp("oboe", s0, s1, 95, 92); sc.ramp("flute", s0, s1, 95, 92); sc.ramp("choir", s0, s1, 78, 70)
            sc.ramp("bass", s0, s0 + 2, 115, 90); sc.ramp("cello", s0, s0 + 2, 112, 92)
        elif sec == "knights":
            for tr, v in (("horns", 112), ("trombones", 104), ("tuba", 100), ("str_ost", 106), ("timpani", 116),
                          ("bass", 108), ("cello", 106), ("str_pad", 100)):
                sc.ramp(tr, s0, s0 + 1, v - 10, v)
        elif sec == "finale":
            for tr, v in (("str_pad", 108), ("horns", 108), ("choir", 100), ("trombones", 100), ("tuba", 98),
                          ("timpani", 110), ("bass", 108), ("cello", 106), ("harp", 104), ("bells", 100)):
                sc.ramp(tr, s0, s1, v - 6, v)

        for bi, ch in enumerate(chords):
            b = beat + bi * 4
            pcs = chord_pcs(ch)
            pad = voice_chord(ch, prev_pad, 55, 76, 4)
            prev_pad = pad
            root2 = bass_note(ch, 33)
            last_bar = bi == len(chords) - 1

            if sec == "intro":
                sc.note("cello", b, 4, n("D3"), 58)
                sc.note("bass", b, 4, n("D2"), 62)
                _harp_arp(sc, b, ch, vel=52, pattern="rise")
            elif sec == "world":
                for p in pad:
                    sc.note("str_pad", b, 4, p, 62, human=False)
                sc.note("bass", b, 4, root2, 64)
                sc.note("cello", b, 4, root2 + 12, 56)
                _harp_arp(sc, b, ch, vel=58, pattern="wave")
            elif sec == "dragons":
                # Tremolo-Streicher, tiefe Blechbläser-Schwellen, Chor, Pauken
                for p in voice_chord(ch, None, 50, 69, 4):
                    sc.note("str_trem", b, 4, p, 78, human=False)
                sc.note("bass", b, 4, root2, 74)
                sc.note("cello", b, 4, root2 + 12, 70)
                if bi % 2 == 0 or last_bar:
                    dur = 8 if (bi % 2 == 0 and bi + 1 < len(chords)) else 4
                    for p in (root2 + 12, root2 + 19):
                        sc.note("trombones", b, dur, p, 72)
                    sc.note("tuba", b, dur, root2, 70)
                    for p in voice_chord(ch, None, 52, 67, 3):
                        sc.note("choir", b, dur, p, 66, human=False)
                # Pauken: Akzent zu Taktbeginn, ab der zweiten Hälfte Wirbel
                timp_root = n("D2") if root_pc(ch) in (NOTE["D"], NOTE["Bb"], NOTE["G"]) else n("A1") + 12
                if bi < len(chords) // 2:
                    sc.note("timpani", b, 1, timp_root, 78)
                    sc.note("perc", b, 1, 35, 60)        # Konzert-Basstrommel
                else:
                    _roll(sc, "timpani", b, 4, timp_root, 60, 100)
                if last_bar:
                    sc.note("perc", b + 3.5, 0.5, 38, 70)
            elif sec == "wonder":
                for p in pad:
                    sc.note("str_pad", b, 4, p, 54, human=False)
                sc.note("bass", b, 4, root2, 56)
                _harp_arp(sc, b, ch, vel=60, pattern="wave16" if bi % 2 else "wave")
                _celesta(sc, b, ch, bi)
                if bi in (1, 5):
                    sc.note("oboe", b, 2, _top(ch, 70, 79), 60)
                    sc.note("oboe", b + 2, 2, _top(ch, 66, 76), 56)
                if bi in (3, 7) or last_bar:
                    sc.note("flute", b, 4, _top(ch, 74, 84), 54)
                if bi % 2 == 0:
                    for p in voice_chord(ch, None, 60, 72, 3):
                        sc.note("choir", b, 4, p, 50, human=False)
            elif sec == "knights":
                for p in pad:
                    sc.note("str_pad", b, 4, p, 66, human=False)
                # Streicher-Ostinato in Achteln
                for k in range(8):
                    p = root2 + 24 if k % 4 != 3 else root2 + 31
                    sc.note("str_ost", b + k * 0.5, 0.45, p, 74 if k % 2 == 0 else 62)
                for k in range(4):
                    sc.note("bass", b + k, 0.9, root2, 76 if k % 2 == 0 else 66)
                sc.note("cello", b, 4, root2 + 12, 70)
                # Pauken auf 1 und 3, kleine Trommel im Marschrhythmus
                timp = n("D2") if root_pc(ch) in (NOTE["D"], NOTE["Bb"], NOTE["G"], NOTE["F"]) else n("A2")
                sc.note("timpani", b, 1, timp, 92)
                sc.note("timpani", b + 2, 1, timp + 7 if timp + 7 <= n("A2") else timp, 84)
                for off, vel in ((0, 70), (0.75, 50), (1, 60), (2, 66), (2.5, 50), (2.75, 52), (3, 60), (3.5, 48)):
                    sc.note("perc", b + off, 0.25, 38, vel)
                sc.note("perc", b, 0.5, 35, 70)
                # Blechbläser: Akkorde auf 1 (+ 3 im zweiten Halbtakt)
                for p in (root2 + 12, root2 + 19):
                    sc.note("trombones", b, 1.8, p, 70)
                    sc.note("trombones", b + 2, 1.8, p, 64)
                sc.note("tuba", b, 1.8, root2, 66)
                if bi == 0:
                    sc.note("perc", b, 2, 57, 72)          # Becken zum Einsatz der Hörner
            elif sec == "finale":
                for p in pad:
                    sc.note("str_pad", b, 4, p, 74, human=False)
                for p in voice_chord(ch, None, 60, 74, 4):
                    sc.note("choir", b, 4, p, 72, human=False)
                sc.note("bass", b, 4, root2, 76)
                sc.note("cello", b, 4, root2 + 12, 74)
                for p in (root2 + 12, root2 + 19, root2 + 24):
                    sc.note("horns", b, 4, p, 70)
                sc.note("tuba", b, 4, root2, 70)
                _harp_arp(sc, b, ch, vel=62, pattern="wave16")
                if last_bar:
                    _roll(sc, "timpani", b + 1, 3, n("A1") + 12, 70, 112)
        # ---------------- Melodien ----------------
        if sec == "world" and bars >= 2:
            # Flöte spielt den Vordersatz des Themas, weich und weit oben
            _melody(sc, "flute", s0, THEME, max_beats=bars * 4, vel=56, upto=16)
        if sec == "knights":
            if bars >= 4:
                _melody(sc, "horns", s0, THEME, max_beats=bars * 4, transpose=-12, vel=92, upto=32 if bars >= 8 else 16)
                if bars >= 8:
                    _melody(sc, "oboe", s0 + 16, THEME, max_beats=max(0, bars * 4 - 16), vel=60, start_at=16, upto=32)
            else:
                # verdichteter Vordersatz: D A D | F E D C | A
                short = [(0, 2, "D5"), (2, 1, "A4"), (3, 1, "D5"), (4, 1.5, "F5"), (5.5, 0.5, "E5"), (6, 1, "D5"),
                         (7, 1, "C5"), (8, 4, "A4")]
                if bars == 2:
                    short = [(0, 2, "D5"), (2, 1, "A4"), (3, 1, "D5"), (4, 4, "C#5")]
                _melody(sc, "horns", s0, short, max_beats=bars * 4, transpose=-12, vel=94, upto=99)
        beat = s1

    # ---------------- Schluss: D-Dur ----------------
    b = beat
    final = voice_chord("D", prev_pad, 57, 78, 4)
    total_tail = (outro_bars + 1) * 4
    for p in final:
        sc.note("str_pad", b, total_tail, p, 84, human=False)
    for p in (n("D4"), n("F#4"), n("A4"), n("D5")):
        sc.note("choir", b, total_tail, p, 80, human=False)
    for p in (n("D3"), n("A3"), n("F#4")):
        sc.note("horns", b, total_tail - 1, p, 82)
    sc.note("trombones", b, total_tail - 2, n("D3"), 70)
    sc.note("trombones", b, total_tail - 2, n("A3"), 66)
    sc.note("tuba", b, total_tail - 1, n("D2"), 74)
    sc.note("bass", b, total_tail, n("D2"), 82)
    sc.note("cello", b, total_tail, n("D3"), 80)
    sc.note("timpani", b, 2, n("D2"), 112)
    sc.note("perc", b, 4, 57, 92)            # Becken
    sc.note("perc", b, 2, 35, 92)
    sc.note("bells", b, 4, n("D5"), 92)
    sc.note("bells", b + 4, 4, n("A4"), 74)
    sc.note("bells", b + 8, 6, n("D5"), 66)
    sc.note("flute", b + 4, total_tail - 6, n("F#5"), 62)
    # Harfe: aufsteigendes D-Dur-Glissando über den Schluss
    for k, p in enumerate([n("D3"), n("A3"), n("D4"), n("F#4"), n("A4"), n("D5"), n("F#5"), n("A5"), n("D6")]):
        sc.note("harp", b + 0.25 * k, 6, p, 64 - k * 2)
    for k, p in enumerate([n("D5"), n("F#5"), n("A5"), n("D6")]):
        sc.note("celesta", b + 4 + k * 0.5, 2, p, 60 - k * 4)
    # Ausklang: alles weich zurücknehmen
    for tr in ("str_pad", "choir", "horns", "trombones", "tuba", "bass", "cello", "harp", "flute"):
        sc.ramp(tr, b + 4, b + total_tail, 112 if tr != "harp" else 104, 50, curve=0.8)
    sc.length_beats = max(sc.length_beats, b + total_tail + 2)
    return sc


def _top(chord: str, low: int, high: int) -> int:
    pcs = chord_pcs(chord)
    for p in range(high, low - 1, -1):
        if p % 12 in pcs:
            return p
    return high


def _harp_arp(sc: Score, b: float, chord: str, vel: int, pattern: str) -> None:
    r = bass_note(chord, 38)          # Grundton um D2..C#3
    pcs = chord_pcs(chord)
    third = (pcs[1] - pcs[0]) % 12
    fifth = (pcs[2] - pcs[0]) % 12
    seq = [r, r + fifth, r + 12, r + 12 + third, r + 12 + fifth, r + 24, r + 12 + fifth, r + 12 + third]
    if pattern == "rise":
        for k, p in enumerate(seq[:6] + [r + 24 + third, r + 24 + fifth]):
            sc.note("harp", b + k * 0.5, 2.5, p, vel - k)
    elif pattern == "wave":
        for k, p in enumerate(seq):
            sc.note("harp", b + k * 0.5, 1.6, p, vel + (6 if k == 0 else 0) - (k % 4))
    elif pattern == "wave16":
        ext = seq + [r + 24 + third, r + 24 + fifth, r + 36, r + 24 + fifth, r + 24 + third, r + 24, r + 12 + fifth, r + 12 + third]
        for k, p in enumerate(ext):
            sc.note("harp", b + k * 0.25, 1.2, p, vel - 4 + (6 if k % 4 == 0 else 0))


def _celesta(sc: Score, b: float, chord: str, bi: int) -> None:
    pcs = chord_pcs(chord)
    notes = [p for p in range(n("A5"), n("E7")) if p % 12 in pcs]
    rng = random.Random(100 + bi)
    picks = sorted(rng.sample(notes, min(4, len(notes))), reverse=bi % 2 == 1)
    for k, p in enumerate(picks):
        sc.note("celesta", b + 0.5 + k * 0.75, 1.0, p, 50 - k * 3)


def _roll(sc: Score, track: str, b: float, beats: float, pitch: int, v0: int, v1: int) -> None:
    steps = int(beats * 8)
    for i in range(steps):
        x = i / max(1, steps - 1)
        sc.note(track, b + i / 8, 0.2, pitch, int(v0 + (v1 - v0) * x ** 1.5), human=False)


def _melody(sc: Score, track: str, b0: float, theme: list, max_beats: float, transpose: int = 0, vel: int = 80,
            start_at: float = 0, upto: float = 32) -> None:
    for beat, dur, name in theme:
        if beat < start_at or beat >= upto:
            continue
        rel = beat - start_at
        if rel >= max_beats:
            break
        d = min(dur, max_beats - rel)
        accent = 8 if beat % 4 == 0 else 0
        sc.note(track, b0 + rel, d, n(name) + transpose, vel + accent)


# ---------------------------------------------------------------------------
# Wiedergabe (je Spur ein eigener "Stem", damit Lautstärken und Hall frei gemischt werden können)
# ---------------------------------------------------------------------------

def render_stems(sc: Score, sf2: Path, sr: int = 44100, tail_seconds: float = 4.0) -> dict[str, np.ndarray]:
    synth = tinysoundfont.Synth(samplerate=sr, gain=-6)
    sfid = synth.sfload(str(sf2), max_voices=256)
    total = int((sc.seconds(sc.length_beats) + tail_seconds) * sr)
    stems: dict[str, np.ndarray] = {}
    for t in sc.tracks.values():
        if not t.events:
            continue
        synth.sounds_off()
        ch = 9 if t.drums else t.channel
        synth.program_select(ch, sfid, t.bank, t.program, is_drums=t.drums)
        synth.control_change(ch, 7, t.volume)
        synth.control_change(ch, 10, t.pan)
        synth.control_change(ch, 11, 100)
        events = sorted(t.events, key=lambda e: (e[0], e[1]))
        out = np.zeros((total, 2), dtype=np.float32)
        pos = 0
        for beat, kind, a, b in events:
            at = min(total, int(sc.seconds(beat) * sr))
            if at > pos:
                out[pos:at] = np.frombuffer(synth.generate_simple(at - pos), dtype=np.float32).reshape(-1, 2)
                pos = at
            if kind == 1:
                synth.noteon(ch, a, b)
            elif kind == 0:
                synth.noteoff(ch, a)
            else:
                synth.control_change(ch, a, b)
        if pos < total:
            out[pos:] = np.frombuffer(synth.generate_simple(total - pos), dtype=np.float32).reshape(-1, 2)
        stems[t.name] = out
    return stems
