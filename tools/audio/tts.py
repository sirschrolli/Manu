"""
Sprachsynthese mit Piper und die Nachbearbeitung für Erzähler- und Soldatenstimmen.
"""
from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path

import numpy as np
from piper.voice import PiperVoice, SynthesisConfig

import dsp

OUT_SR = 44100


@dataclass
class VoiceSpec:
    model: str                 # Dateiname des Modells (ohne Pfad)
    speaker: int | None = None
    length_scale: float = 1.0
    noise_scale: float = 0.667
    noise_w: float = 0.8
    pitch_semitones: float = 0.0   # Nachträgliche Tonhöhenänderung (leichtes Umfärben)


_VOICE_CACHE: dict[str, PiperVoice] = {}


def load_voice(models_dir: Path, name: str) -> PiperVoice:
    if name not in _VOICE_CACHE:
        _VOICE_CACHE[name] = PiperVoice.load(models_dir / name)
    return _VOICE_CACHE[name]


# Pausen zwischen Satzteilen (Sekunden)
PAUSE = {".": 0.55, "!": 0.5, "?": 0.55, ":": 0.38, "–": 0.32, ";": 0.35}


def split_segments(text: str) -> list[tuple[str, float]]:
    """Teilt einen Satz an Satzzeichen mit eigener Pause; Kommas übernimmt Piper selbst."""
    parts = re.split(r"(\s+–\s+|(?<=[.!?:;])\s+)", text.strip())
    segs: list[tuple[str, float]] = []
    buf = ""
    for p in parts:
        if not p:
            continue
        if re.fullmatch(r"\s+–\s+", p):
            if buf:
                segs.append((buf.strip(), PAUSE["–"]))
                buf = ""
        elif re.fullmatch(r"\s+", p):
            continue
        else:
            if buf:
                segs.append((buf.strip(), PAUSE.get(buf.strip()[-1], 0.3)))
            buf = p
    if buf:
        segs.append((buf.strip(), 0.0))
    return segs


def synthesize(models_dir: Path, spec: VoiceSpec, text: str) -> tuple[np.ndarray, int]:
    voice = load_voice(models_dir, spec.model)
    cfg = SynthesisConfig(speaker_id=spec.speaker, length_scale=spec.length_scale,
                          noise_scale=spec.noise_scale, noise_w_scale=spec.noise_w, normalize_audio=False)
    pieces = []
    sr = voice.config.sample_rate
    for seg, pause in split_segments(text):
        chunks = list(voice.synthesize(seg, syn_config=cfg))
        audio = np.concatenate([c.audio_float_array for c in chunks]).astype(np.float32)
        audio = dsp.trim_silence(audio, sr, -42, 0.02)
        pieces.append(audio)
        if pause > 0:
            pieces.append(np.zeros(int(pause * sr), dtype=np.float32))
    return np.concatenate(pieces), sr


def pitch_shift(x: np.ndarray, sr: int, semitones: float) -> np.ndarray:
    """Einfaches Umfärben per Neuabtastung (verändert Tonhöhe und Dauer leicht – für kleine Werte gedacht)."""
    if abs(semitones) < 1e-3:
        return x
    factor = 2 ** (semitones / 12)
    n = int(round(len(x) / factor))
    from scipy import signal
    return signal.resample(x, n).astype(np.float32)


def narrator_chain(x: np.ndarray, sr: int, ir: np.ndarray, target_lufs: float = -16.0) -> np.ndarray:
    y = dsp.resample(x, sr, OUT_SR)
    y = dsp.trim_silence(y, OUT_SR, -45, 0.04)
    y = dsp.eq(y, OUT_SR, [
        ("highpass", 75, 0.707, 0),        # Rumpeln weg
        ("lowshelf", 210, 0.707, 2.0),     # Wärme
        ("peak", 3100, 1.0, -1.2),         # etwas weniger Schärfe
        ("highshelf", 7200, 0.707, -2.5),  # sanft abgerundete Höhen
        ("lowpass", 10500, 0.707, 0),
    ])
    y = dsp.compressor(y, OUT_SR, threshold_db=-24, ratio=2.2, attack=0.006, release=0.15)
    y = np.concatenate([np.zeros(int(0.03 * OUT_SR), np.float32), y, np.zeros(int(0.45 * OUT_SR), np.float32)])
    y = dsp.reverb(y, ir, wet=0.11, keep_length=True)   # dezenter Raumklang
    y = dsp.fade(y, OUT_SR, 0.01, 0.25)
    return dsp.normalize(y, OUT_SR, target_lufs, ceiling_db=-1.7)


def soldier_chain(x: np.ndarray, sr: int, ir: np.ndarray, pitch: float = 0.0, target_lufs: float = -15.0) -> np.ndarray:
    y = pitch_shift(x, sr, pitch)
    y = dsp.resample(y, sr, OUT_SR)
    y = dsp.trim_silence(y, OUT_SR, -42, 0.02)
    y = dsp.eq(y, OUT_SR, [
        ("highpass", 90, 0.707, 0),
        ("lowshelf", 180, 0.707, 1.5),
        ("peak", 2200, 0.9, 1.5),          # mehr Präsenz/Durchsetzungskraft im Spiel
        ("highshelf", 8000, 0.707, -2.0),
        ("lowpass", 11000, 0.707, 0),
    ])
    y = dsp.compressor(y, OUT_SR, threshold_db=-22, ratio=3.0, attack=0.004, release=0.1)
    y = np.concatenate([y, np.zeros(int(0.12 * OUT_SR), np.float32)])
    y = dsp.reverb(y, ir, wet=0.05, keep_length=True)
    y = dsp.fade(y, OUT_SR, 0.004, 0.06)
    return dsp.normalize(y, OUT_SR, target_lufs, ceiling_db=-1.6)
