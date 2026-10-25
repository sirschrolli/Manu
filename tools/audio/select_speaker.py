#!/usr/bin/env python3
"""
Hilfsskript: misst viele Sprecher der englischen Piper-Stimme "libritts_r" und
schlägt tiefe, ruhige und saubere Männerstimmen für den Erzähler vor.

Aufruf:
    python3 tools/audio/select_speaker.py --models /pfad/zu/den/modellen [--step 4] [--out ergebnis.csv]

Gemessen werden (ohne Zuhören):
  * f0_med   – Median der Grundfrequenz in Hz (tief = Erzähler-Stimme)
  * f0_semi  – Tonhöhen-Spannweite in Halbtönen (nicht zu monoton, nicht zu sprunghaft)
  * voiced   – Anteil stimmhafter Frames
  * flat     – spektrale Flachheit in stimmhaften Frames (niedriger = klarer, weniger Rauschen)
  * hnr      – grobe Harmonic-to-Noise-Ratio aus der Autokorrelation (höher = sauberer)
  * rms_cv   – Schwankung der Lautheit über stimmhafte Frames (niedriger = gleichmäßiger)
  * clip     – Anteil übersteuerter Samples im Rohsignal
  * dur      – Dauer des Testsatzes (langsamer = feierlicher)
"""
from __future__ import annotations

import argparse
import csv
import json
import wave
from pathlib import Path

import librosa
import numpy as np
from piper.voice import PiperVoice, SynthesisConfig

TEST_TEXT = ("Then the kingdoms rose, with walls of stone and towers that reached up to the clouds. "
             "But deep within the mountains, the dragons awoke.")


def synth(voice: PiperVoice, text: str, cfg: SynthesisConfig) -> tuple[np.ndarray, int]:
    chunks = list(voice.synthesize(text, syn_config=cfg))
    sr = chunks[0].sample_rate
    audio = np.concatenate([c.audio_float_array for c in chunks]).astype(np.float32)
    return audio, sr


def measure(audio: np.ndarray, sr: int) -> dict:
    clip = float(np.mean(np.abs(audio) >= 0.999))
    y = audio / (np.max(np.abs(audio)) + 1e-9)
    f0, voiced_flag, _ = librosa.pyin(y, fmin=55, fmax=320, sr=sr, frame_length=1024, hop_length=256)
    v = voiced_flag & ~np.isnan(f0)
    voiced = float(np.mean(v)) if len(v) else 0.0
    f0v = f0[v]
    f0_med = float(np.median(f0v)) if len(f0v) else 0.0
    semi = 12 * np.log2(f0v / f0_med) if len(f0v) else np.array([0.0])
    f0_semi = float(np.percentile(semi, 95) - np.percentile(semi, 5))
    flat_all = librosa.feature.spectral_flatness(y=y, n_fft=1024, hop_length=256)[0]
    rms_all = librosa.feature.rms(y=y, frame_length=1024, hop_length=256)[0]
    n = min(len(flat_all), len(v))
    flat = float(np.median(flat_all[:n][v[:n]])) if np.any(v[:n]) else 1.0
    rv = rms_all[:n][v[:n]]
    rms_cv = float(np.std(rv) / (np.mean(rv) + 1e-9)) if len(rv) else 9.9
    # grobe HNR: normierte Autokorrelation am Grundperioden-Lag je stimmhaftem Frame
    hnrs = []
    hop, fl = 256, 1024
    for i in np.where(v[:n])[0][::3]:
        seg = y[i * hop:i * hop + fl]
        if len(seg) < fl or not np.isfinite(f0[i]):
            continue
        seg = seg - np.mean(seg)
        lag = int(round(sr / f0[i]))
        if lag <= 0 or lag >= fl // 2:
            continue
        r0 = np.dot(seg, seg) + 1e-9
        r = np.dot(seg[:-lag], seg[lag:]) / r0
        r = min(max(r, 1e-4), 0.9999)
        hnrs.append(10 * np.log10(r / (1 - r)))
    hnr = float(np.median(hnrs)) if hnrs else 0.0
    return dict(f0_med=f0_med, f0_semi=f0_semi, voiced=voiced, flat=flat, hnr=hnr, rms_cv=rms_cv,
                clip=clip, dur=len(audio) / sr)


def score(m: dict) -> float:
    """Höher = besserer Erzähler (tiefe, klare, gleichmäßige, nicht monotone Männerstimme)."""
    if m["f0_med"] <= 0:
        return -99
    s = 0.0
    s -= abs(m["f0_med"] - 100) / 12          # Ziel ~100 Hz
    s += m["hnr"] / 3                         # klar
    s -= m["flat"] * 40                       # wenig Rauschen
    s -= max(0, m["rms_cv"] - 0.45) * 6       # gleichmäßig laut
    s -= abs(m["f0_semi"] - 7) / 3            # lebendige, aber ruhige Melodie
    s -= m["clip"] * 200
    s += min(m["dur"], 9) / 4                 # eher langsam
    return s


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--models", required=True)
    ap.add_argument("--step", type=int, default=4)
    ap.add_argument("--out", default="speakers.csv")
    ap.add_argument("--ids", default="", help="kommagetrennte Liste statt --step")
    args = ap.parse_args()
    model = Path(args.models) / "en_US-libritts_r-medium.onnx"
    voice = PiperVoice.load(model)
    n = json.load(open(str(model) + ".json"))["num_speakers"]
    ids = [int(x) for x in args.ids.split(",") if x] or list(range(0, n, args.step))
    rows = []
    for sid in ids:
        cfg = SynthesisConfig(speaker_id=sid, length_scale=1.1, noise_scale=0.333, noise_w_scale=0.333,
                              normalize_audio=False)
        audio, sr = synth(voice, TEST_TEXT, cfg)
        m = measure(audio, sr)
        m["speaker"] = sid
        m["score"] = score(m)
        rows.append(m)
        print(f"{sid:4d} f0={m['f0_med']:6.1f} semi={m['f0_semi']:5.1f} hnr={m['hnr']:5.1f} flat={m['flat']:.4f} "
              f"cv={m['rms_cv']:.2f} dur={m['dur']:.2f} score={m['score']:.2f}", flush=True)
    rows.sort(key=lambda r: -r["score"])
    with open(args.out, "w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
        w.writeheader()
        w.writerows(rows)
    print("Beste Kandidaten:", [r["speaker"] for r in rows[:10]])


if __name__ == "__main__":
    main()
