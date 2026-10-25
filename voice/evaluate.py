"""
Misst Stimmen (Tonhöhe, Klarheit, Tempo) und prüft mit Whisper, ob der Text richtig gesprochen wurde.

  evaluate.py <ordner> <muster> <sprache> <design|sample> <bericht.json>
"""
import json
import re
import sys
from pathlib import Path

import jiwer
import numpy as np
import parselmouth
from faster_whisper import WhisperModel

from texts import DESIGN, SAMPLE

folder, pattern, lang, kind, report_name = Path(sys.argv[1]), sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5]
text = (DESIGN if kind == "design" else SAMPLE)[lang].replace("…", "")
model = WhisperModel("small", device="cpu", compute_type="int8")


def words(s: str) -> str:
    return " ".join(re.sub(r"[^\wäöüß ]", " ", s.lower()).split())


def measure(path: Path) -> dict:
    snd = parselmouth.Sound(str(path))
    pitch = snd.to_pitch(time_step=0.01, pitch_floor=60, pitch_ceiling=300)
    f0 = pitch.selected_array["frequency"]
    f0 = f0[f0 > 0]
    harm = snd.to_harmonicity_cc(time_step=0.01, minimum_pitch=60)
    hnr_vals = harm.values[harm.values > -199]
    point = parselmouth.praat.call(snd, "To PointProcess (periodic, cc)", 60, 300)
    jitter = parselmouth.praat.call(point, "Get jitter (local)", 0, 0, 0.0001, 0.02, 1.3)
    shimmer = parselmouth.praat.call([snd, point], "Get shimmer (local)", 0, 0, 0.0001, 0.02, 1.3, 1.6)
    segments, _ = model.transcribe(str(path), language=lang, beam_size=5)
    heard = " ".join(s.text for s in segments).strip()
    duration = snd.get_total_duration()
    st = 12 * np.log2(f0 / np.median(f0)) if len(f0) else np.zeros(1)
    return {
        "f0_median": round(float(np.median(f0)), 1) if len(f0) else 0.0,
        "betonung_st": round(float(np.std(st)), 2),       # Tonhöhen-Bewegung in Halbtönen (mehr = lebhafter)
        "f0_p10": round(float(np.percentile(f0, 10)), 1) if len(f0) else 0.0,
        "f0_p90": round(float(np.percentile(f0, 90)), 1) if len(f0) else 0.0,
        "hnr": round(float(np.mean(hnr_vals)), 1) if len(hnr_vals) else 0.0,
        "jitter": round(float(jitter), 4), "shimmer": round(float(shimmer), 4),
        "duration": round(duration, 2), "chars_per_s": round(len(text) / max(duration, 0.1), 1),
        "wer": round(jiwer.wer(words(text), words(heard)), 3), "heard": heard,
    }


report = {}
for path in sorted(folder.glob(pattern)):
    report[path.name] = measure(path)
    print(path.name, report[path.name], flush=True)
(folder / report_name).write_text(json.dumps(report, indent=1, ensure_ascii=False))
