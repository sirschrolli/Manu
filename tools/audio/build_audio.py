#!/usr/bin/env python3
"""
Erzeugt ALLE Audiodateien der Mod mit einem Befehl:

  * Erzählerstimme für das Intro: entweder vorab erzeugte Zeilen (--narration-source, z. B. die Qwen3-TTS-Stimme
    aus tools/audio/narrator) oder Piper (Deutsch: Thorsten, Englisch: LibriTTS-R 680)
  * eigene Intro-Musik (komponiert in music.py, gespielt mit FluidR3_GM)
  * Zeitplan des Intros (assets/kingdomsatwar/intro/intro.json)
  * Soldaten-Sprachzeilen (Deutsch/Englisch, je zwei Stimmfärbungen)
  * assets/kingdomsatwar/sounds.json

Aufruf (aus dem Projektordner):
    python3 tools/audio/build_audio.py --models /pfad/zu/den/modellen
oder mit Umgebungsvariable:
    KAW_AUDIO_MODELS=/pfad/zu/den/modellen python3 tools/audio/build_audio.py

Optionen:
    --only narration,music,voices   nur einzelne Teile neu erzeugen (Standard: alles;
                                    "--only none" schreibt nur intro.json und sounds.json neu)
    --work /pfad                    Ordner für Zwischendateien (MIDI, Stems, Spektrogramme, Bericht)
    --narration-source /pfad        vorab erzeugte Erzählerzeilen narration_raw_<de|en>_<01..09>.wav verwenden
    --narration-style natural       dazu nur leichte Nachbearbeitung (für schon sehr natürliche Stimmen wie Qwen3-TTS;
                                    Standard "epic": kräftigere Nachbearbeitung mit mehr Hall)

Benötigt: pip install piper-tts tinysoundfont mido numpy scipy soundfile pyloudnorm librosa matplotlib
          (tinysoundfont ggf. mit --no-deps installieren) sowie ffmpeg mit libvorbis.
Hinweis: Die Sprachsynthese enthält Zufall; jede Neuerzeugung klingt minimal anders.
         Der Zeitplan (intro.json) wird darum immer passend zu den neuen Dateien berechnet.
"""
from __future__ import annotations

import argparse
import json
import math
import os
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
import dsp  # noqa: E402
import music  # noqa: E402
import tts  # noqa: E402
from lines import NARRATION, SOLDIER, SUBTITLES  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "kingdomsatwar"
SOUNDS = ASSETS / "sounds"
INTRO_DIR = ASSETS / "intro"
SR = tts.OUT_SR
NS = "kingdomsatwar"

# --- Stimmen -------------------------------------------------------------------------------
DE_NARRATOR = tts.VoiceSpec("de_DE-thorsten-high.onnx", None, length_scale=1.2, noise_scale=0.55, noise_w=0.55)
# LibriTTS-R Sprecher 680: tiefe, ruhige, sehr saubere Stimme mit langsamem Erzähltempo
# (gemessen mit select_speaker.py: f0 ~110 Hz, höchste HNR und geringste Rauschanteile der Kandidaten)
EN_NARRATOR = tts.VoiceSpec("en_US-libritts_r-medium.onnx", 680, length_scale=1.08, noise_scale=0.4, noise_w=0.35)

EMOTION = {"amused": 0, "angry": 1, "disgusted": 2, "drunk": 3, "neutral": 4, "sleepy": 5, "surprised": 6, "whisper": 7}
SOLDIER_DE = {  # Variante a/b: zwei Stimmfärbungen derselben Stimme
    "a": dict(length=1.0, pitch=0.0),
    "b": dict(length=1.06, pitch=-1.6),
}
SOLDIER_EN = {  # zwei verschiedene LibriTTS-R-Sprecher
    "a": dict(speaker=248, length=0.98, pitch=0.0),   # tief, kräftig
    "b": dict(speaker=788, length=0.96, pitch=0.0),   # klar, jünger
}

BPM = 72.0
BEAT = 60.0 / BPM
BAR = 4 * BEAT
LEAD_BARS = 2                   # zwei Takte leises Vorspiel vor der ersten Zeile
OUTRO_AFTER_LAST = 4.6          # Sekunden Musik nach der letzten Zeile (Titelbild)


def ceil_to(t: float, step: float) -> float:
    return math.ceil(round(t / step, 6)) * step


def speech_length(x: np.ndarray, sr: int) -> float:
    """Dauer bis zum letzten deutlich hörbaren Sample (ohne Hallfahne)."""
    env = np.abs(x)
    win = int(0.02 * sr)
    env = np.convolve(env, np.ones(win) / win, mode="same")
    idx = np.where(env > env.max() * dsp.undb(-38))[0]
    return (idx[-1] / sr) if len(idx) else len(x) / sr


# =============================================================================================
# 1. Erzählerstimme
# =============================================================================================

TARGET_RATE = {"de": 2.55, "en": 2.75}    # Wörter pro Sekunde reine Sprechzeit (ruhiges Erzähltempo)
MAX_LENGTH_SCALE = 1.42                  # darüber klingt Piper gedehnt


def _take_quality(y: np.ndarray, sr: int) -> dict:
    """Bewertet eine Aufnahme: Klarheit (HNR), Rauschen, Gleichmäßigkeit."""
    from select_speaker import measure
    m = measure(y, sr)
    return m


def build_narration(models: Path, work: Path, takes: int = 3) -> dict:
    """Erzeugt jede Zeile in mehreren "Takes" (Tempo-Varianten) und nimmt den besten:
    Sprechtempo nahe am Ziel, saubere Stimme (hohe HNR, wenig Rauschen), keine Übersteuerung."""
    room = dsp.impulse_response(SR, rt60=0.65, predelay=0.012, stereo=False, damping_hz=5000, seed=3)
    info = {}
    log = []
    for lang, spec in (("de", DE_NARRATOR), ("en", EN_NARRATOR)):
        for i, line in enumerate(NARRATION, start=1):
            text = line.get(f"{lang}_tts", line[lang])
            words = len(line[lang].replace("–", " ").split())
            slow = line.get("slow", 1.0)
            best = None
            pauses = sum(p for _, p in tts.split_segments(text))
            for k in range(takes):
                ls = min(MAX_LENGTH_SCALE, spec.length_scale * slow * (0.97 + 0.06 * k))
                take_spec = tts.VoiceSpec(spec.model, spec.speaker, ls, spec.noise_scale, spec.noise_w)
                raw, sr = tts.synthesize(models, take_spec, text)
                q = _take_quality(raw, sr)
                rate = words / max(0.1, q["dur"] - pauses)
                target = TARGET_RATE[lang] / slow
                score = -abs(rate - target) * 1.2 + q["hnr"] * 0.5 - q["flat"] * 300 - q["clip"] * 100
                log.append(dict(lang=lang, line=i, take=k, length_scale=round(ls, 3), dur=round(q["dur"], 2),
                                rate=round(rate, 2), hnr=round(q["hnr"], 2), score=round(score, 2)))
                if best is None or score > best[0]:
                    best = (score, raw, sr, ls, rate)
            _, raw, sr, ls, rate = best
            y = tts.narrator_chain(raw, sr, room, target_lufs=-16.0)
            path = SOUNDS / "intro" / f"narration_{lang}_{i:02d}.ogg"
            dsp.write_ogg(y, SR, path, quality=5)
            dsp.write_wav(y, SR, work / "narration" / f"narration_{lang}_{i:02d}.wav")
            info[(lang, i)] = dict(file=path, speech=speech_length(y, SR), length=len(y) / SR)
            print(f"  Erzähler {lang} {i}: {info[(lang, i)]['speech']:.2f} s (Tempo {rate:.2f} W/s, length_scale {ls:.2f})",
                  flush=True)
    json.dump(log, open(work / "narration_takes.json", "w"), indent=1)
    return info


def load_narration(source: Path, work: Path, style: str = "epic") -> dict:
    """Vorab erzeugte Erzählerzeilen übernehmen und nachbearbeiten (style: "epic" oder "natural")."""
    info = {}
    for lang in ("de", "en"):
        for i in range(1, len(NARRATION) + 1):
            raw, sr = dsp.read_wav(source / f"narration_raw_{lang}_{i:02d}.wav")
            y = tts.epic_narrator_chain(raw, sr, target_lufs=-16.0, style=style)
            path = SOUNDS / "intro" / f"narration_{lang}_{i:02d}.ogg"
            dsp.write_ogg(y, SR, path, quality=5)
            dsp.write_wav(y, SR, work / "narration" / f"narration_{lang}_{i:02d}.wav")
            info[(lang, i)] = dict(file=path, speech=speech_length(y, SR), length=len(y) / SR)
            print(f"  Erzähler {lang} {i}: {info[(lang, i)]['speech']:.2f} s", flush=True)
    return info


def plan_timeline(narr: dict) -> dict:
    """Zeilen auf Schläge/Takte der Musik ausrichten; Abschnittsanfänge und die letzte Zeile auf Taktanfänge."""
    t = LEAD_BARS * BAR
    lines = []
    for i, line in enumerate(NARRATION, start=1):
        first_of_section = i == 1 or NARRATION[i - 2]["section"] != line["section"]
        t = ceil_to(t, BAR if (first_of_section or i == len(NARRATION)) else BEAT)
        d_de, d_en = narr[("de", i)]["speech"], narr[("en", i)]["speech"]
        lines.append(dict(index=i, section=line["section"], start=round(t, 3),
                          duration={"de": round(d_de, 3), "en": round(d_en, 3)}))
        t = t + max(d_de, d_en) + line["pause"]
    last = lines[-1]
    total = last["start"] + max(last["duration"].values()) + OUTRO_AFTER_LAST
    # Musikabschnitte (in Takten)
    starts = {}
    for ln in lines:
        starts.setdefault(ln["section"], ln["start"])
    order = ["world", "dragons", "wonder", "knights", "finale"]
    sections = [("intro", LEAD_BARS)]
    for k, sec in enumerate(order):
        a = starts[sec]
        b = starts[order[k + 1]] if k + 1 < len(order) else last["start"]
        sections.append((sec, int(round((b - a) / BAR))))
    tail_bars = math.ceil((total - last["start"]) / BAR)
    return dict(lines=lines, total=round(total, 3), sections=sections, tail_bars=tail_bars,
                section_starts={k: round(v, 3) for k, v in starts.items()}, final_chord=round(last["start"], 3))


# =============================================================================================
# 2. Musik
# =============================================================================================

STEM_LEVEL = {  # relative Lautheit der Spuren in LU (Mischungs-Ziel)
    "str_pad": 0.0, "str_trem": 1.0, "str_ost": -3.0, "cello": -7.0, "bass": -7.0, "harp": -2.5, "celesta": -8.0,
    "flute": -3.0, "oboe": -4.5, "horns": 1.0, "trombones": -2.5, "tuba": -5.5, "timpani": -2.5, "choir": -3.5,
    "bells": -7.0, "perc": -7.0,
}
REVERB_SEND = {
    "str_pad": 0.32, "str_trem": 0.3, "str_ost": 0.22, "cello": 0.22, "bass": 0.15, "harp": 0.3, "celesta": 0.38,
    "flute": 0.3, "oboe": 0.28, "horns": 0.3, "trombones": 0.26, "tuba": 0.18, "timpani": 0.25, "choir": 0.4,
    "bells": 0.42, "perc": 0.22,
}


def _smooth_steps(points: list[tuple[float, float]], n: int) -> np.ndarray:
    """Lineare Hüllkurve in dB aus (Zeit, dB)-Punkten -> Faktor je Sample."""
    t = np.arange(n) / SR
    xs = [p[0] for p in points]
    ys = [p[1] for p in points]
    return 10 ** (np.interp(t, xs, ys) / 20)


def macro_envelope(plan: dict, n: int) -> np.ndarray:
    """Großer Spannungsbogen (leiser Anfang, Steigerung zu den Drachen, geheimnisvoll leise,
    kräftige Ritter, Höhepunkt auf der letzten Zeile) und Absenken der Musik, während gesprochen wird."""
    st = plan["section_starts"]
    end = plan["total"]
    arc = [(0.0, -9.0), (st["world"], -5.5), (st["dragons"] - 0.1, -4.0), (st["dragons"], -4.0),
           (st["wonder"] - 0.4, 0.0), (st["wonder"] + 0.6, -4.5), (st["knights"] - 0.2, -4.0), (st["knights"], -1.5),
           (st["finale"], -1.5), (plan["final_chord"] - 0.1, -0.5), (plan["final_chord"] + 0.2, 1.0), (end, 1.0)]
    env = _smooth_steps(arc, n)
    # Ducking: -3.5 dB während der Erzählung (beide Sprachen haben dieselben Startzeiten)
    duck_db = np.zeros(n)
    t = np.arange(n) / SR
    for ln in plan["lines"]:
        a = ln["start"] - 0.15
        b = ln["start"] + max(ln["duration"].values()) + 0.1
        ramp = 0.25
        x = np.clip(np.minimum((t - a) / ramp, (b + ramp - t) / ramp), 0, 1)
        duck_db = np.minimum(duck_db, -3.5 * x)
    return env * 10 ** (duck_db / 20)


SPEECH_CAP = -21.0      # max. momentane Lautheit der Musik, während gesprochen wird (Stimme: -16 LUFS)
FREE_CAP = -15.5        # max. momentane Lautheit in Sprechpausen, Vor- und Nachspiel


def voice_aware_leveling(x: np.ndarray, plan: dict) -> np.ndarray:
    """Hält die Musik unter der Stimme leise genug für gute Verständlichkeit, lässt sie aber in den
    Pausen und im Nachspiel aufblühen (automatische "Fader-Fahrt")."""
    times, loud = dsp.momentary_loudness(x, SR)
    cap = np.full_like(loud, FREE_CAP)
    for ln in plan["lines"]:
        a = ln["start"] - 0.65          # Absenkung ist schon vor dem ersten Wort vollständig
        b = ln["start"] + max(ln["duration"].values()) + 0.25
        w = np.clip(np.minimum((times - a) / 0.4, (b + 0.4 - times) / 0.4), 0, 1)
        cap = np.minimum(cap, FREE_CAP + (SPEECH_CAP - FREE_CAP) * w)
    gain = np.minimum(0.0, cap - loud)
    # glätten: schnell absenken (0,2 s), langsam wieder freigeben (0,8 s)
    hop = times[1] - times[0] if len(times) > 1 else 0.1
    a_att, a_rel = math.exp(-hop / 0.2), math.exp(-hop / 0.8)
    g = np.empty_like(gain)
    cur = 0.0
    for i, t in enumerate(gain):
        cur = a_att * cur + (1 - a_att) * t if t < cur else a_rel * cur + (1 - a_rel) * t
        g[i] = cur
    # Rückwärts-Glättung, damit das Absenken etwas VOR dem Lautheitsanstieg beginnt
    g2 = np.empty_like(g)
    cur = 0.0
    for i in range(len(g) - 1, -1, -1):
        cur = min(g[i], a_att * cur + (1 - a_att) * g[i])
        g2[i] = cur
    t_samples = np.arange(len(x)) / SR
    gain_lin = 10 ** (np.interp(t_samples, times, g2) / 20)
    return (x * gain_lin[:, None]).astype(np.float32)


def build_music(models: Path, work: Path, plan: dict) -> Path:
    sections = plan["sections"]
    sc = music.compose(sections, bpm=BPM, outro_bars=max(1, plan["tail_bars"] - 1))
    (work / "music").mkdir(parents=True, exist_ok=True)
    sc.to_midi(work / "music" / "intro_music.mid")
    stems = music.render_stems(sc, models / "FluidR3_GM.sf2", SR, tail_seconds=4.0)
    total_n = int(plan["total"] * SR)
    mix = np.zeros((total_n + int(4.0 * SR), 2), np.float32)
    send = np.zeros_like(mix)
    report = {}
    for name, st in stems.items():
        st = st[:len(mix)]
        loud = dsp.lufs(st, SR)
        if not math.isfinite(loud):
            continue
        g = dsp.undb(STEM_LEVEL.get(name, -6.0) - 23.0 - loud)   # Bezug: 0 LU = -23 LUFS je Spur
        st = st * g
        mix[:len(st)] += st
        send[:len(st)] += st * REVERB_SEND.get(name, 0.25)
        report[name] = round(loud, 1)
        dsp.write_wav(st, SR, work / "music" / "stems" / f"{name}.wav")
    hall = dsp.impulse_response(SR, rt60=2.3, predelay=0.028, stereo=True, damping_hz=4500, seed=21, early=12)
    wet = dsp.reverb(send, hall, wet=1.0)[:len(mix)] - send          # nur die Hallfahne
    master = mix + 0.9 * wet
    master = dsp.eq(master, SR, [
        ("highpass", 32, 0.707, 0),
        ("lowshelf", 130, 0.707, -2.0),  # weniger Bass-Teppich unter der Männerstimme
        ("peak", 2300, 0.8, -2.5),      # Platz für die Stimme
        ("highshelf", 7000, 0.707, 2.0),  # etwas mehr "Luft" (oberhalb der Sprachverständlichkeit)
    ])
    master = master[:total_n]
    master = master * macro_envelope(plan, len(master))[:, None]
    master = dsp.fade(master, SR, fade_in=0.35, fade_out=4.2)
    master = dsp.normalize(master, SR, -20.0, ceiling_db=-1.5)
    master = voice_aware_leveling(master, plan)
    master = dsp.limiter(master, SR, -1.5)
    path = SOUNDS / "intro" / "intro_music.ogg"
    dsp.write_ogg(master, SR, path, quality=6)
    dsp.write_wav(master, SR, work / "music" / "intro_music.wav")
    json.dump(report, open(work / "music" / "stem_loudness.json", "w"), indent=1)
    return path


# =============================================================================================
# 3. Soldaten-Stimmen
# =============================================================================================

def build_voices(models: Path, work: Path) -> dict:
    room = dsp.impulse_response(SR, rt60=0.35, predelay=0.006, stereo=False, damping_hz=4500, seed=5)
    files: dict[str, list[str]] = {}
    for cat, data in SOLDIER.items():
        for lang in ("de", "en"):
            key = f"voice.soldier.{cat}.{lang}"
            files[key] = []
            for i, text in enumerate(data[lang], start=1):
                variants = SOLDIER_DE if lang == "de" else SOLDIER_EN
                for var, v in variants.items():
                    if lang == "de":
                        spec = tts.VoiceSpec("de_DE-thorsten_emotional-medium.onnx", EMOTION[data["emotion"]],
                                             length_scale=v["length"], noise_scale=0.6, noise_w=0.7)
                    else:
                        spec = tts.VoiceSpec("en_US-libritts_r-medium.onnx", v["speaker"], length_scale=v["length"],
                                             noise_scale=0.45, noise_w=0.5)
                    raw, sr = tts.synthesize(models, spec, text)
                    y = tts.soldier_chain(raw, sr, room, pitch=v["pitch"], target_lufs=-15.0)
                    rel = f"voice/soldier/{cat}_{lang}_{i}{var}"
                    dsp.write_ogg(y, SR, SOUNDS / f"{rel}.ogg", quality=4)
                    files[key].append(f"{NS}:{rel}")
        print(f"  Soldat {cat}: {len(files[f'voice.soldier.{cat}.de'])} + {len(files[f'voice.soldier.{cat}.en'])} Dateien",
              flush=True)
    return files


# =============================================================================================
# 4. sounds.json und intro.json
# =============================================================================================

def write_intro_json(plan: dict) -> None:
    INTRO_DIR.mkdir(parents=True, exist_ok=True)
    langs = {}
    for lang, code in (("de", "de_de"), ("en", "en_us")):
        langs[code] = {"lines": [
            {"key": f"intro.{NS}.line{ln['index']}", "sound": f"{NS}:intro.narration.{lang}.{ln['index']}",
             "start": ln["start"], "duration": ln["duration"][lang]} for ln in plan["lines"]]}
    data = {
        "music": f"{NS}:intro.music",
        "length": plan["total"],
        "title_card_start": round(plan["lines"][-1]["start"] + max(plan["lines"][-1]["duration"].values()) + 0.4, 3),
        "fallback_language": "en_us",
        "sections": [{"name": "intro", "start": 0.0}] + [
            {"name": k, "start": v} for k, v in sorted(plan["section_starts"].items(), key=lambda kv: kv[1])],
        "final_chord": plan["final_chord"],
        "languages": langs,
    }
    with open(INTRO_DIR / "intro.json", "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def write_sounds_json(voice_files: dict | None) -> None:
    path = ASSETS / "sounds.json"
    data = {}
    if path.exists():
        data = json.load(open(path, encoding="utf-8"))
    data[f"intro.music"] = {"sounds": [{"name": f"{NS}:intro/intro_music", "stream": True}],
                            "subtitle": "subtitles.kingdomsatwar.intro.music"}
    for lang in ("de", "en"):
        for i in range(1, len(NARRATION) + 1):
            data[f"intro.narration.{lang}.{i}"] = {"sounds": [f"{NS}:intro/narration_{lang}_{i:02d}"],
                                                   "subtitle": "subtitles.kingdomsatwar.intro.narration"}
    if voice_files:
        for key, files in voice_files.items():
            cat = key.split(".")[2]
            data[key] = {"sounds": files, "subtitle": f"subtitles.kingdomsatwar.voice.soldier.{cat}"}
    ordered = dict(sorted(data.items()))
    with open(path, "w", encoding="utf-8") as f:
        json.dump(ordered, f, indent=2, ensure_ascii=False)
        f.write("\n")


# =============================================================================================
# 5. Bericht und Spektrogramme
# =============================================================================================

def report(work: Path) -> None:
    rows = []
    for p in sorted(SOUNDS.rglob("*.ogg")):
        m = dsp.measure_file(p)
        rows.append(dict(file=str(p.relative_to(SOUNDS)), **m))
    with open(work / "report.json", "w") as f:
        json.dump(rows, f, indent=1)
    print(f"{'Datei':44s} {'LUFS':>7s} {'TP':>6s} {'Dauer':>6s} Kan.")
    for r in rows:
        print(f"{r['file']:44s} {r['lufs']:7.1f} {r['true_peak']:6.1f} {r['duration']:6.2f} {r['channels']}")


def spectrograms(work: Path) -> None:
    import librosa
    import librosa.display
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    files = [("Erzähler DE, Zeile 6", work / "narration" / "narration_de_06.wav"),
             ("Narrator EN, line 6", work / "narration" / "narration_en_06.wav")]
    fig, axes = plt.subplots(len(files), 1, figsize=(14, 4 * len(files)))
    for ax, (title, p) in zip(np.atleast_1d(axes), files):
        if not p.exists():
            continue
        y, sr = librosa.load(p, sr=None, mono=True)
        S = librosa.amplitude_to_db(np.abs(librosa.stft(y, n_fft=2048, hop_length=512)), ref=np.max)
        librosa.display.specshow(S, sr=sr, hop_length=512, x_axis="time", y_axis="log", ax=ax, vmin=-80, vmax=0, cmap="magma")
        ax.set_title(title)
    plt.tight_layout()
    plt.savefig(work / "spectrogram_narration.png", dpi=60)
    plt.close(fig)
    mp = work / "music" / "intro_music.wav"
    if mp.exists():
        y, sr = librosa.load(mp, sr=None, mono=True)
        fig, axes = plt.subplots(2, 1, figsize=(16, 8), gridspec_kw={"height_ratios": [3, 1]})
        S = librosa.amplitude_to_db(np.abs(librosa.stft(y, n_fft=4096, hop_length=1024)), ref=np.max)
        librosa.display.specshow(S, sr=sr, hop_length=1024, x_axis="time", y_axis="log", ax=axes[0], vmin=-80, vmax=0, cmap="magma")
        axes[0].set_title("Intro-Musik")
        rms = librosa.feature.rms(y=y, frame_length=4096, hop_length=1024)[0]
        t = np.arange(len(rms)) * 1024 / sr
        axes[1].plot(t, 20 * np.log10(rms + 1e-9))
        axes[1].set_xlim(0, t[-1]); axes[1].set_ylabel("RMS dB"); axes[1].grid(True)
        plt.tight_layout()
        plt.savefig(work / "spectrogram_music.png", dpi=60)
        plt.close(fig)


# =============================================================================================

def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--models", default=os.environ.get("KAW_AUDIO_MODELS"))
    ap.add_argument("--only", default="narration,music,voices")
    ap.add_argument("--work", default=os.environ.get("KAW_AUDIO_WORK", str(ROOT / "build" / "audio-work")))
    ap.add_argument("--narration-source", default=None)
    ap.add_argument("--narration-style", default="epic", choices=["epic", "natural"])
    args = ap.parse_args()
    if not args.models:
        sys.exit("Bitte --models oder KAW_AUDIO_MODELS angeben (Ordner mit den Piper-Stimmen und FluidR3_GM.sf2).")
    models = Path(args.models)
    work = Path(args.work)
    work.mkdir(parents=True, exist_ok=True)
    parts = set(args.only.split(","))

    narr_cache = work / "narration_timing.json"
    if "narration" in parts or "music" in parts and not narr_cache.exists():
        print("Erzählerstimme ...")
        if args.narration_source:
            narr = load_narration(Path(args.narration_source), work, args.narration_style)
        else:
            narr = build_narration(models, work)
        json.dump({f"{k[0]}:{k[1]}": {"speech": v["speech"], "length": v["length"]} for k, v in narr.items()},
                  open(narr_cache, "w"), indent=1)
    narr = {(k.split(":")[0], int(k.split(":")[1])): v for k, v in json.load(open(narr_cache)).items()}
    plan = plan_timeline(narr)
    json.dump(plan, open(work / "timeline.json", "w"), indent=1)
    print("Zeitplan:", plan["sections"], "Gesamt", plan["total"], "s")
    write_intro_json(plan)

    if "music" in parts:
        print("Musik ...")
        build_music(models, work, plan)

    voice_files = None
    if "voices" in parts:
        print("Soldaten-Stimmen ...")
        voice_files = build_voices(models, work)
    elif (ASSETS / "sounds.json").exists():
        old = json.load(open(ASSETS / "sounds.json", encoding="utf-8"))
        voice_files = {k: v["sounds"] for k, v in old.items() if k.startswith("voice.soldier.")}
    write_sounds_json(voice_files)

    report(work)
    spectrograms(work)
    print("Untertitel-Schlüssel:", ", ".join(SUBTITLES))
    print("Fertig. Zwischendateien:", work)


if __name__ == "__main__":
    main()
