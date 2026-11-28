"""
Kleine Audio-Werkzeugkiste (nur numpy/scipy/pyloudnorm + ffmpeg zum Kodieren):
Filter, Kompressor, Hall, Lautheits-Normalisierung (LUFS), True-Peak-Limiter, OGG-Export.
"""
from __future__ import annotations

import json
import math
import subprocess
import tempfile
from pathlib import Path

import numpy as np
import pyloudnorm
import soundfile as sf
from scipy import signal

# ---------------------------------------------------------------------------
# Grundlagen
# ---------------------------------------------------------------------------

def db(x: float) -> float:
    return 20 * math.log10(max(x, 1e-12))


def undb(d: float) -> float:
    return 10 ** (d / 20)


def as_2d(x: np.ndarray) -> np.ndarray:
    return x[:, None] if x.ndim == 1 else x


def resample(x: np.ndarray, sr_in: int, sr_out: int) -> np.ndarray:
    if sr_in == sr_out:
        return x
    g = math.gcd(sr_in, sr_out)
    return signal.resample_poly(x, sr_out // g, sr_in // g, axis=0).astype(np.float32)


def trim_silence(x: np.ndarray, sr: int, thresh_db: float = -45.0, pad: float = 0.03) -> np.ndarray:
    """Schneidet Stille am Anfang und Ende ab (Schwelle relativ zum Spitzenpegel)."""
    mono = np.abs(as_2d(x)).max(axis=1)
    win = max(1, int(0.01 * sr))
    env = np.convolve(mono, np.ones(win) / win, mode="same")
    peak = env.max() + 1e-12
    idx = np.where(env > peak * undb(thresh_db))[0]
    if len(idx) == 0:
        return x
    a = max(0, idx[0] - int(pad * sr))
    b = min(len(x), idx[-1] + int(pad * sr))
    return x[a:b]


def fade(x: np.ndarray, sr: int, fade_in: float = 0.0, fade_out: float = 0.0) -> np.ndarray:
    y = as_2d(x).copy()
    n_in, n_out = int(fade_in * sr), int(fade_out * sr)
    if n_in > 0:
        y[:n_in] *= (np.sin(np.linspace(0, np.pi / 2, n_in)) ** 2)[:, None]
    if n_out > 0:
        y[-n_out:] *= (np.cos(np.linspace(0, np.pi / 2, n_out)) ** 2)[:, None]
    return y if x.ndim == 2 else y[:, 0]


# ---------------------------------------------------------------------------
# Filter (RBJ-Kochbuch-Biquads)
# ---------------------------------------------------------------------------

def _biquad(kind: str, f0: float, sr: int, q: float = 0.707, gain_db: float = 0.0) -> np.ndarray:
    a_ = 10 ** (gain_db / 40)
    w0 = 2 * math.pi * f0 / sr
    cw, sw = math.cos(w0), math.sin(w0)
    alpha = sw / (2 * q)
    if kind == "lowpass":
        b = [(1 - cw) / 2, 1 - cw, (1 - cw) / 2]; a = [1 + alpha, -2 * cw, 1 - alpha]
    elif kind == "highpass":
        b = [(1 + cw) / 2, -(1 + cw), (1 + cw) / 2]; a = [1 + alpha, -2 * cw, 1 - alpha]
    elif kind == "peak":
        b = [1 + alpha * a_, -2 * cw, 1 - alpha * a_]; a = [1 + alpha / a_, -2 * cw, 1 - alpha / a_]
    elif kind == "lowshelf":
        s = 2 * math.sqrt(a_) * alpha
        b = [a_ * ((a_ + 1) - (a_ - 1) * cw + s), 2 * a_ * ((a_ - 1) - (a_ + 1) * cw), a_ * ((a_ + 1) - (a_ - 1) * cw - s)]
        a = [(a_ + 1) + (a_ - 1) * cw + s, -2 * ((a_ - 1) + (a_ + 1) * cw), (a_ + 1) + (a_ - 1) * cw - s]
    elif kind == "highshelf":
        s = 2 * math.sqrt(a_) * alpha
        b = [a_ * ((a_ + 1) + (a_ - 1) * cw + s), -2 * a_ * ((a_ - 1) + (a_ + 1) * cw), a_ * ((a_ + 1) + (a_ - 1) * cw - s)]
        a = [(a_ + 1) - (a_ - 1) * cw + s, 2 * ((a_ - 1) - (a_ + 1) * cw), (a_ + 1) - (a_ - 1) * cw - s]
    else:
        raise ValueError(kind)
    b, a = np.array(b) / a[0], np.array(a) / a[0]
    return np.concatenate([b, a])[None, :]


def eq(x: np.ndarray, sr: int, bands: list[tuple]) -> np.ndarray:
    """bands: Liste von (Art, Frequenz, Güte, Gain-dB)."""
    sos = np.concatenate([_biquad(k, f, sr, q, g) for k, f, q, g in bands], axis=0)
    return signal.sosfilt(sos, x, axis=0).astype(np.float32)


# ---------------------------------------------------------------------------
# Dynamik
# ---------------------------------------------------------------------------

def compressor(x: np.ndarray, sr: int, threshold_db: float = -20.0, ratio: float = 2.5,
               attack: float = 0.008, release: float = 0.12, knee_db: float = 6.0) -> np.ndarray:
    """Einfacher RMS-Kompressor (gemeinsame Hüllkurve für alle Kanäle), blockweise berechnet."""
    y = as_2d(x)
    block = 32
    n = len(y) // block + 1
    pad = np.zeros((n * block - len(y), y.shape[1]), dtype=y.dtype)
    yy = np.concatenate([y, pad])
    rms = np.sqrt(np.mean(yy.reshape(n, block, -1) ** 2, axis=(1, 2)) + 1e-12)
    lvl = 20 * np.log10(rms)
    over = lvl - threshold_db
    gr = np.where(over <= -knee_db / 2, 0.0,
                  np.where(over >= knee_db / 2, over * (1 - 1 / ratio),
                           (1 - 1 / ratio) * (over + knee_db / 2) ** 2 / (2 * knee_db)))
    a_att = math.exp(-block / (attack * sr))
    a_rel = math.exp(-block / (release * sr))
    g = np.zeros(n)
    cur = 0.0
    for i in range(n):
        t = gr[i]
        cur = a_att * cur + (1 - a_att) * t if t > cur else a_rel * cur + (1 - a_rel) * t
        g[i] = cur
    gain = 10 ** (-np.repeat(g, block)[:len(y)] / 20)
    out = y * gain[:, None]
    return out if x.ndim == 2 else out[:, 0]


def true_peak_db(x: np.ndarray, sr: int) -> float:
    up = signal.resample_poly(as_2d(x), 4, 1, axis=0)
    return db(float(np.max(np.abs(up))))


def limiter(x: np.ndarray, sr: int, ceiling_db: float = -1.5, lookahead: float = 0.005, release: float = 0.08) -> np.ndarray:
    """Look-ahead-Spitzenbegrenzer auf Basis des 4-fach überabgetasteten Pegels."""
    y = as_2d(x).astype(np.float64)
    ceil = undb(ceiling_db)
    up = np.abs(signal.resample_poly(y, 4, 1, axis=0)).max(axis=1)[:4 * len(y)]
    if len(up) < 4 * len(y):
        up = np.pad(up, (0, 4 * len(y) - len(up)))
    peak = up.reshape(len(y), 4).max(axis=1)
    need = np.minimum(1.0, ceil / np.maximum(peak, 1e-12))
    la = max(1, int(lookahead * sr))
    # Minimum über das Look-ahead-Fenster (Gain sinkt rechtzeitig vor der Spitze)
    from scipy.ndimage import minimum_filter1d
    need = minimum_filter1d(need, size=2 * la + 1, origin=0)
    # weiches Erholen
    a_rel = math.exp(-1 / (release * sr))
    g = np.empty_like(need)
    cur = 1.0
    for i in range(len(need)):
        t = need[i]
        cur = t if t < cur else a_rel * cur + (1 - a_rel) * t
        g[i] = cur
    out = (y * g[:, None]).astype(np.float32)
    return out if x.ndim == 2 else out[:, 0]


def lufs(x: np.ndarray, sr: int) -> float:
    meter = pyloudnorm.Meter(sr)
    return float(meter.integrated_loudness(as_2d(x).astype(np.float64)))


def normalize(x: np.ndarray, sr: int, target_lufs: float, ceiling_db: float = -1.5) -> np.ndarray:
    """Auf Ziel-Lautheit bringen, dann True-Peak begrenzen (und Lautheit nachjustieren)."""
    y = x.astype(np.float32)
    for _ in range(3):
        cur = lufs(y, sr)
        if not math.isfinite(cur):
            return y
        y = (y * undb(target_lufs - cur)).astype(np.float32)
        if true_peak_db(y, sr) > ceiling_db:
            y = limiter(y, sr, ceiling_db - 0.2)
        if abs(lufs(y, sr) - target_lufs) < 0.15:
            break
    return y


# ---------------------------------------------------------------------------
# Hall (Faltung mit synthetischer Impulsantwort)
# ---------------------------------------------------------------------------

def impulse_response(sr: int, rt60: float, predelay: float = 0.02, stereo: bool = False,
                     damping_hz: float = 6000.0, seed: int = 7, early: int = 8) -> np.ndarray:
    rng = np.random.default_rng(seed)
    length = int(sr * (rt60 * 1.2 + predelay))
    ch = 2 if stereo else 1
    t = np.arange(length) / sr
    decay = np.exp(-6.9078 * t / rt60)                     # -60 dB nach rt60
    noise = rng.standard_normal((length, ch))
    # höhere Frequenzen klingen schneller ab: zwei Bänder mit unterschiedlichem Abklingen
    lo = signal.sosfilt(signal.butter(2, damping_hz, "low", fs=sr, output="sos"), noise, axis=0)
    hi = noise - lo
    tail = lo * decay[:, None] + hi * (decay ** 2.2)[:, None]
    ir = np.zeros((length, ch))
    pd = int(predelay * sr)
    ir[pd:] = tail[:length - pd]
    # frühe Reflexionen
    for k in range(early):
        d = pd + int(rng.uniform(0.003, 0.06) * sr)
        if d < length:
            ir[d] += rng.uniform(0.3, 0.8) * (1 if rng.random() > 0.5 else -1) * np.exp(-k * 0.35)
    ir /= np.sqrt(np.sum(ir ** 2, axis=0, keepdims=True)) + 1e-12
    return ir.astype(np.float32)


def reverb(x: np.ndarray, ir: np.ndarray, wet: float, keep_length: bool = False) -> np.ndarray:
    """Mischt trockenes Signal mit Hallfahne. x mono (N,) oder stereo (N,2)."""
    y = as_2d(x)
    if ir.shape[1] == 2 and y.shape[1] == 1:
        y = np.repeat(y, 2, axis=1)
    wet_sig = np.stack([signal.fftconvolve(y[:, c], ir[:, c % ir.shape[1]]) for c in range(y.shape[1])], axis=1)
    out = np.zeros_like(wet_sig)
    out[:len(y)] += y
    out += wet * wet_sig
    if keep_length:
        out = out[:len(y)]
    return (out if x.ndim == 2 or ir.shape[1] == 2 else out[:, 0]).astype(np.float32)


# ---------------------------------------------------------------------------
# Export und Messung
# ---------------------------------------------------------------------------

def write_ogg(x: np.ndarray, sr: int, path: Path, quality: float = 5.0) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as tmp:
        wav = Path(tmp) / "a.wav"
        sf.write(wav, as_2d(x), sr, subtype="FLOAT")
        subprocess.run(["ffmpeg", "-loglevel", "error", "-y", "-i", str(wav), "-c:a", "libvorbis",
                        "-q:a", str(quality), "-ar", str(sr), "-map_metadata", "-1", str(path)], check=True)


def read_wav(path: Path) -> tuple[np.ndarray, int]:
    """Liest eine Audiodatei als Mono-Float32."""
    y, sr = sf.read(path, dtype="float32", always_2d=True)
    return y.mean(axis=1).astype(np.float32), sr


def write_wav(x: np.ndarray, sr: int, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    sf.write(path, as_2d(x), sr, subtype="FLOAT")


def measure_file(path: Path) -> dict:
    """Misst eine Datei mit ffmpeg (EBU R128): integrierte Lautheit, True Peak, Dauer."""
    r = subprocess.run(["ffmpeg", "-hide_banner", "-nostats", "-i", str(path), "-af", "ebur128=peak=true",
                        "-f", "null", "-"], capture_output=True, text=True)
    txt = r.stderr
    def grab(key: str) -> float:
        import re
        m = re.findall(key + r":\s+(-?[\d.]+|-inf)", txt)
        return float(m[-1]) if m and m[-1] != "-inf" else float("-inf")
    probe = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration:stream=channels,sample_rate",
                            "-of", "json", str(path)], capture_output=True, text=True)
    info = json.loads(probe.stdout)
    return dict(lufs=grab("I"), true_peak=grab("Peak"), duration=float(info["format"]["duration"]),
                channels=int(info["streams"][0]["channels"]), sample_rate=int(info["streams"][0]["sample_rate"]))


def momentary_loudness(x: np.ndarray, sr: int, window: float = 0.4, hop: float = 0.1) -> tuple[np.ndarray, np.ndarray]:
    """Momentane Lautheit nach BS.1770 (K-Gewichtung, 400-ms-Fenster). Gibt (Zeiten, LUFS) zurück."""
    meter = pyloudnorm.Meter(sr)
    y = as_2d(x).astype(np.float64)
    for f in meter._filters.values():
        y = np.stack([f.apply_filter(y[:, c]) for c in range(y.shape[1])], axis=1)
    win, h = int(window * sr), int(hop * sr)
    power = np.sum(y ** 2, axis=1)
    csum = np.concatenate([[0.0], np.cumsum(power)])
    starts = np.arange(0, max(1, len(power) - win), h)
    ms = (csum[starts + win] - csum[starts]) / win
    times = (starts + win / 2) / sr
    return times, -0.691 + 10 * np.log10(ms + 1e-12)
