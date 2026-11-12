"""
Bausteine der Umgebungsklänge (siehe ambience.py): Rauschen mit zeitlich veränderlichem Spektrum, nahtlos geschlossene
Klangteppiche, Hall, Panorama und kleine Synthesizer für Töne, Glocken und Formanten.

Alle Teppiche sind *kreisförmig*: Rauschen, Steuerkurven, Ereignisse und Hall laufen über das Ende hinaus wieder am
Anfang weiter. So schließt jede Schleife ohne Naht, und Minecraft kann sie endlos wiederholen.
"""
from __future__ import annotations

import math

import numpy as np
from scipy import signal


# =====================================================================================
# Zufall und Steuerkurven
# =====================================================================================

def rng(seed: int) -> np.random.Generator:
    return np.random.default_rng(seed)


def periodic_curve(n: int, seed: int, cycles: tuple[int, int] = (1, 10), falloff: float = 1.0) -> np.ndarray:
    """Glatte Zufallskurve mit Werten 0…1, die nach n Werten genau wieder bei sich selbst ankommt."""
    r = rng(seed)
    t = np.arange(n) / n
    y = np.zeros(n)
    for k in range(cycles[0], cycles[1] + 1):
        y += r.uniform(0.3, 1.0) / k ** falloff * np.sin(2 * np.pi * k * t + r.uniform(0, 2 * np.pi))
    y -= y.min()
    return y / (y.max() + 1e-12)


def smoothstep(x: np.ndarray, lo: float = 0.0, hi: float = 1.0) -> np.ndarray:
    u = np.clip((x - lo) / (hi - lo), 0.0, 1.0)
    return u * u * (3 - 2 * u)


def resample_curve(c: np.ndarray, n: int) -> np.ndarray:
    """Steuerkurve (kreisförmig) auf n Werte strecken."""
    m = len(c)
    x = np.arange(n) * m / n
    i = np.floor(x).astype(int)
    f = x - i
    return c[i % m] * (1 - f) + c[(i + 1) % m] * f


# =====================================================================================
# Rauschen mit veränderlichem Spektrum (kreisförmige STFT)
# =====================================================================================

def shaped_noise(n: int, sr: int, gain_fn, seed: int, nfft: int = 2048, hop: int | None = None) -> np.ndarray:
    """
    Weißes Rauschen, dessen Spektrum sich mit der Zeit ändert: {@code gain_fn(t, f)} bekommt die Mittenzeiten der Fenster
    (Sekunden, Spalte) und die Frequenzen (Hz, Zeile) und liefert die Verstärkung je Fenster und Frequenz (T×F).
    Das Ergebnis ist kreisförmig (Länge n muss ein Vielfaches von hop sein).
    """
    hop = hop or nfft // 4
    assert n % hop == 0, (n, hop)
    x = rng(seed).standard_normal(n)
    frames = n // hop
    win = np.hanning(nfft + 1)[:-1]
    idx = (np.arange(frames)[:, None] * hop + np.arange(nfft)[None, :] - nfft // 2) % n
    spec = np.fft.rfft(x[idx] * win, axis=1)
    t = np.arange(frames) * hop / sr
    f = np.fft.rfftfreq(nfft, 1 / sr)
    g = gain_fn(t[:, None], f[None, :])
    spec *= g
    y_frames = np.fft.irfft(spec, n=nfft, axis=1) * win
    y = np.bincount(idx.ravel(), weights=y_frames.ravel(), minlength=n)
    y /= np.sum(win ** 2) / hop
    return y


def colored_noise(n: int, sr: int, seed: int, slope_db_oct: float = -3.0, lo: float = 20.0, hi: float | None = None) -> np.ndarray:
    """Kreisförmiges Rauschen mit festem Spektralabfall (−3 dB/Okt. = rosa, −6 = braun)."""
    spec = np.fft.rfft(rng(seed).standard_normal(n))
    f = np.fft.rfftfreq(n, 1 / sr)
    g = (np.maximum(f, lo) / 1000.0) ** (slope_db_oct / 6.0206)
    g[f < lo] *= (f[f < lo] / lo) ** 2
    if hi is not None:
        g /= 1 + (f / hi) ** 4
    y = np.fft.irfft(spec * g, n=n)
    return y / (np.std(y) + 1e-12)


def band(f: np.ndarray, center, width_oct: float) -> np.ndarray:
    """Glockenförmiges Band um {@code center} (Hz) mit Breite in Oktaven (Gauß im log. Maßstab)."""
    lf = np.log2(np.maximum(f, 1.0))
    return np.exp(-0.5 * ((lf - np.log2(center)) / width_oct) ** 2)


def tilt(f: np.ndarray, db_oct: float, ref: float = 1000.0) -> np.ndarray:
    return (np.maximum(f, 20.0) / ref) ** (db_oct / 6.0206)


def lowpass_gain(f: np.ndarray, fc, order: int = 2) -> np.ndarray:
    return 1.0 / np.sqrt(1 + (f / fc) ** (2 * order))


def highpass_gain(f: np.ndarray, fc, order: int = 2) -> np.ndarray:
    return 1.0 / np.sqrt(1 + (fc / np.maximum(f, 1e-3)) ** (2 * order))


# =====================================================================================
# Filter (nicht kreisförmig, für Einzelklänge)
# =====================================================================================

def sos(kind: str, f, sr: int, order: int = 2):
    top = sr * 0.45
    if isinstance(f, (list, tuple)):
        f = [min(float(v), top) for v in f]
        if f[0] >= f[1]:
            f = [f[1] * 0.7, f[1]]
    else:
        f = min(float(f), top)
    return signal.butter(order, f, kind, fs=sr, output="sos")


def filt(x: np.ndarray, kind: str, f, sr: int, order: int = 2) -> np.ndarray:
    return signal.sosfilt(sos(kind, f, sr, order), x, axis=0)


def filt_circular(x: np.ndarray, kind: str, f, sr: int, order: int = 2) -> np.ndarray:
    """Filter ohne Naht: zweimal durchlaufen lassen und den zweiten Durchgang behalten."""
    y = signal.sosfilt(sos(kind, f, sr, order), np.concatenate([x, x]), axis=0)
    return y[len(x):]


def resonator(x: np.ndarray, f0: float, q: float, sr: int) -> np.ndarray:
    """Schmaler Bandpass (Resonanz) – für Holz, Eis und Glasklang."""
    b, a = signal.iirpeak(min(f0, sr * 0.45), q, fs=sr)
    return signal.lfilter(b, a, x)


# =====================================================================================
# Töne
# =====================================================================================

def phase_of(freq: np.ndarray, sr: int, phase0: float = 0.0) -> np.ndarray:
    return phase0 + 2 * np.pi * np.cumsum(freq) / sr


def tone(freq: np.ndarray, amp: np.ndarray, sr: int, harmonics=((1, 1.0), (2, 0.08), (3, 0.02)), phase0: float = 0.0) -> np.ndarray:
    """Ton mit veränderlicher Frequenz und Lautstärke (Obertöne über der halben Abtastrate fallen weg)."""
    ph = phase_of(freq, sr, phase0)
    y = np.zeros_like(freq, dtype=float)
    for h, a in harmonics:
        mask = (freq * h) < sr * 0.45
        y += a * np.sin(h * ph) * mask
    return y * amp


def saw(freq: np.ndarray, amp: np.ndarray, sr: int, partials: int = 24, rolloff: float = 1.0, phase0: float = 0.0) -> np.ndarray:
    """Bandbegrenzter Sägezahn (additiv)."""
    ph = phase_of(freq, sr, phase0)
    y = np.zeros_like(freq, dtype=float)
    for h in range(1, partials + 1):
        mask = (freq * h) < sr * 0.45
        y += np.sin(h * ph) / h ** rolloff * mask
    return y * amp


def glide(n: int, f0: float, f1: float, shape: float = 1.0) -> np.ndarray:
    """Frequenzverlauf von f0 nach f1 (logarithmisch; shape > 1: erst langsam, dann schnell)."""
    u = np.linspace(0.0, 1.0, n) ** shape
    return f0 * (f1 / f0) ** u


def curve(n: int, points: list[tuple[float, float]]) -> np.ndarray:
    """Stückweise linearer Verlauf über n Werte; points = [(0…1, wert), …]."""
    xs = [p[0] for p in points]
    ys = [p[1] for p in points]
    return np.interp(np.linspace(0.0, 1.0, n), xs, ys)


def env_ad(n: int, sr: int, attack: float, decay: float | None = None, curve_pow: float = 1.0) -> np.ndarray:
    """Hüllkurve: Anstieg, dann (exponentielles) Abklingen bis zum Ende."""
    a = max(1, int(attack * sr))
    e = np.ones(n)
    e[:a] = np.linspace(0.0, 1.0, a) ** curve_pow
    if decay is not None:
        t = np.arange(n - a) / sr
        e[a:] = np.exp(-t / decay)
    return e


def env_asr(n: int, sr: int, attack: float, release: float) -> np.ndarray:
    a = max(1, min(n, int(attack * sr)))
    r = max(1, min(n - a, int(release * sr)))
    e = np.ones(n)
    e[:a] = 0.5 - 0.5 * np.cos(np.linspace(0, np.pi, a))
    e[n - r:] = 0.5 + 0.5 * np.cos(np.linspace(0, np.pi, r))
    return e


def bell(f0: float, dur: float, sr: int, ratios=(1.0, 2.76, 5.40, 8.93), amps=(1.0, 0.45, 0.25, 0.12),
         decay: float = 1.2, seed: int = 0) -> np.ndarray:
    """Glocke/Kristall: unharmonische Teiltöne, die höheren klingen schneller ab."""
    r = rng(seed)
    n = int(dur * sr)
    t = np.arange(n) / sr
    y = np.zeros(n)
    for i, (rt, a) in enumerate(zip(ratios, amps)):
        f = f0 * rt * (1 + r.uniform(-0.002, 0.002))
        if f > sr * 0.45:
            continue
        d = decay / (1 + i * 0.9)
        beat = 1 + 0.15 * np.sin(2 * np.pi * r.uniform(0.5, 3.0) * t)
        y += a * np.sin(2 * np.pi * f * t + r.uniform(0, 2 * np.pi)) * np.exp(-t / d) * beat
    y *= env_ad(n, sr, 0.002)
    return y


def formant_filter(x: np.ndarray, sr: int, formants: list[tuple[float, float, float]]) -> np.ndarray:
    """Feste Formanten (Frequenz, Bandbreite, Pegel) – für Stimmen, Krächzen und Rufe."""
    y = np.zeros_like(x)
    for f, bw, g in formants:
        b, a = signal.iirpeak(min(f, sr * 0.45), max(1.0, f / bw), fs=sr)
        y += g * signal.lfilter(b, a, x)
    return y


# =====================================================================================
# Teppich (kreisförmiger Stereo-Puffer)
# =====================================================================================

class Bed:
    """Nahtlos geschlossener Stereo-Klangteppich: Ereignisse über das Ende laufen am Anfang weiter."""

    def __init__(self, seconds: float, sr: int, hop: int = 1024):
        n = int(round(seconds * sr / hop)) * hop
        self.n = n
        self.sr = sr
        self.buf = np.zeros((n, 2))

    @property
    def seconds(self) -> float:
        return self.n / self.sr

    def add(self, x: np.ndarray, t0: float = 0.0, pan: float = 0.0, gain: float = 1.0) -> None:
        """Mono (Panorama −1…1) oder Stereo einmischen, ab Zeitpunkt t0 (s), über das Ende hinaus kreisförmig."""
        x = np.asarray(x, dtype=float)
        if x.ndim == 1:
            left, right = pan_gains(pan)
            x = np.stack([x * left, x * right], axis=1)
        start = int(t0 * self.sr) % self.n
        idx = (start + np.arange(len(x))) % self.n
        self._add_fast(idx, x * gain)

    def _add_fast(self, idx: np.ndarray, x: np.ndarray) -> None:
        self.buf[:, 0] += np.bincount(idx, weights=x[:, 0], minlength=self.n)
        self.buf[:, 1] += np.bincount(idx, weights=x[:, 1], minlength=self.n)

    def add_stereo(self, left: np.ndarray, right: np.ndarray, gain: float = 1.0) -> None:
        self.buf[:, 0] += left[:self.n] * gain
        self.buf[:, 1] += right[:self.n] * gain

    def reverb(self, ir: np.ndarray, wet: float, dry: float = 1.0) -> None:
        """Kreisförmiger Hall (die Fahne am Ende klingt am Anfang aus)."""
        n = self.n
        out = np.zeros_like(self.buf)
        for c in range(2):
            h = ir[:, c % ir.shape[1]]
            hp = np.zeros(n)
            m = min(len(h), n)
            hp[:m] = h[:m]
            out[:, c] = np.fft.irfft(np.fft.rfft(self.buf[:, c]) * np.fft.rfft(hp), n=n)
        self.buf = dry * self.buf + wet * out


def pan_gains(pan: float) -> tuple[float, float]:
    """Gleiche Leistung: −1 = links, 0 = Mitte, 1 = rechts."""
    a = (np.clip(pan, -1, 1) + 1) * np.pi / 4
    return float(np.cos(a)), float(np.sin(a))


def distance(x: np.ndarray, sr: int, meters: float) -> np.ndarray:
    """Entfernung: leiser und dumpfer (Luft schluckt die Höhen)."""
    fc = max(1200.0, 16000.0 / (1 + meters / 25.0))
    y = filt(x, "low", fc, sr, 1)
    return y / (1 + meters / 12.0)


def room(sr: int, rt60: float, seed: int, predelay: float = 0.02, damping: float = 5000.0, stereo: bool = True,
         echo: float | None = None, echo_gain: float = 0.35) -> np.ndarray:
    """Synthetische Raumantwort (Wald, Schlucht, Höhle …), wahlweise mit deutlichem Echo (Felswand)."""
    r = rng(seed)
    length = int(sr * (rt60 * 1.1 + predelay + (echo or 0)))
    ch = 2 if stereo else 1
    t = np.arange(length) / sr
    decay = np.exp(-6.9078 * t / rt60)
    noise = r.standard_normal((length, ch))
    lo = signal.sosfilt(sos("low", damping, sr, 2), noise, axis=0)
    hi = noise - lo
    tail = lo * decay[:, None] + hi * (decay ** 2.5)[:, None]
    ir = np.zeros((length, ch))
    pd = int(predelay * sr)
    ir[pd:] = tail[:length - pd]
    ir[:pd + int(0.004 * sr)] *= np.linspace(0, 1, pd + int(0.004 * sr))[:, None]
    if echo:
        d = int(echo * sr)
        for c in range(ch):
            spread = int(r.uniform(0.0, 0.02) * sr)
            k = min(length - 1, d + spread)
            ir[k:k + 6, c] += echo_gain * np.hanning(6) * 6
    ir /= np.sqrt(np.sum(ir ** 2, axis=0, keepdims=True)) + 1e-12
    return ir


def apply_room(x: np.ndarray, ir: np.ndarray, wet: float) -> np.ndarray:
    """Hall für Einzelklänge (Mono rein, Mono raus, mit Fahne)."""
    h = ir[:, 0]
    w = signal.fftconvolve(x, h)
    out = np.zeros(len(w))
    out[:len(x)] += x
    return out + wet * w


# =====================================================================================
# Abschluss: Lautheit, Spitzen, Übergänge
# =====================================================================================

def rms_db(x: np.ndarray) -> float:
    return 20 * math.log10(float(np.sqrt(np.mean(x ** 2))) + 1e-12)


def peak_db(x: np.ndarray) -> float:
    return 20 * math.log10(float(np.max(np.abs(x))) + 1e-12)


def bass_presence(x: np.ndarray, sr: int, gain: float, cutoff: float = 180.0, top: float = 1600.0) -> np.ndarray:
    """
    Lässt sehr tiefe Klänge (schwere Schritte, Aufschläge, Gebrüll riesiger Wesen) auch auf kleinen Lautsprechern hörbar
    werden (seit 1.1.4): Der Bass wird sanft gesättigt; die so entstehenden Obertöne (cutoff·1,2 bis top Hz) kommen dazu,
    und das Ohr „hört“ den tiefen Grundton daraus von selbst wieder hinzu (fehlender Grundton). Auf guten Lautsprechern
    bleibt der Klang fast gleich, nur etwas griffiger.
    """
    low = filt(x, "low", cutoff, sr, 4)
    peak = float(np.max(np.abs(low))) + 1e-12
    harm = np.tanh(3.0 * low / peak) * peak
    harm = filt(filt(harm, "high", cutoff * 1.2, sr, 4), "low", top, sr, 2)
    return x + gain * harm


def small_speaker_loss(x: np.ndarray, sr: int) -> float:
    """Um wie viel dB die Lautheit (LUFS) sinkt, wenn alles unter 200 Hz fehlt – wie auf kleinen Lautsprechern."""
    import pyloudnorm
    xx = x if len(x) >= int(0.4 * sr) else np.concatenate([x, np.zeros(int(0.4 * sr) - len(x))])
    meter = pyloudnorm.Meter(sr, block_size=0.2)
    full = meter.integrated_loudness(xx)
    small = meter.integrated_loudness(filt(xx, "high", 200.0, sr, 4))
    return float(small - full) if np.isfinite(full) and np.isfinite(small) else 0.0


def audible_bass(x: np.ndarray, sr: int, target: float = -8.0, max_gain: float = 2.5) -> np.ndarray:
    """So wenig bass_presence wie nötig, bis höchstens target dB auf kleinen Lautsprechern verloren gehen."""
    if small_speaker_loss(x, sr) >= target:
        return x
    y = x
    for gain in (0.5, 0.8, 1.2, 1.7, 2.1, max_gain):
        y = bass_presence(x, sr, gain)
        if small_speaker_loss(y, sr) >= target:
            break
    return y


def soft_clip(x: np.ndarray, ceiling: float = 0.89) -> np.ndarray:
    """Weiche Begrenzung (nur die lautesten Spitzen werden sanft gerundet)."""
    k = ceiling
    return np.where(np.abs(x) < 0.7 * k, x, np.sign(x) * (0.7 * k + 0.3 * k * np.tanh((np.abs(x) - 0.7 * k) / (0.3 * k))))


def fit_level(x: np.ndarray, target_rms_db: float, max_peak_db: float = -3.0) -> np.ndarray:
    """Auf Ziel-Effektivwert bringen; ist die Spitze dann zu hoch, leiser machen statt zu verzerren."""
    g = 10 ** ((target_rms_db - rms_db(x)) / 20)
    y = x * g
    p = peak_db(y)
    if p > max_peak_db:
        y *= 10 ** ((max_peak_db - p) / 20)
    return y


def trim_tail(x: np.ndarray, sr: int, thresh_db: float = -60.0, pad: float = 0.05) -> np.ndarray:
    """Stille am Ende abschneiden (mit kurzem Ausblenden)."""
    a = np.abs(x)
    th = np.max(a) * 10 ** (thresh_db / 20)
    idx = np.nonzero(a > th)[0]
    if len(idx) == 0:
        return x
    end = min(len(x), idx[-1] + int(pad * sr))
    y = x[:end].copy()
    f = min(len(y), int(0.04 * sr))
    y[-f:] *= np.linspace(1, 0, f)
    return y


def fade_edges(x: np.ndarray, sr: int, fin: float = 0.003, fout: float = 0.03) -> np.ndarray:
    y = x.copy()
    a = min(len(y), max(1, int(fin * sr)))
    b = min(len(y), max(1, int(fout * sr)))
    y[:a] *= np.linspace(0, 1, a)
    y[-b:] *= np.linspace(1, 0, b)
    return y
