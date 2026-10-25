#!/usr/bin/env python3
"""
Schritt 3 – je Intro-Zeile den besten Take wählen.

Whisper hört jeden Take ab; es gewinnt der Take mit der niedrigsten Wortfehlerrate, bei Gleichstand der mit der
Dauer am nächsten am Mittelwert (keine abgeschnittenen oder gedehnten Aufnahmen). Die Gewinner landen als
<ordner>/lines/narration_raw_<de|en>_<NN>.wav – genau das, was build_audio.py mit --narration-source erwartet.
Ein Bericht steht danach in <ordner>/takes_report.json.

    python3 tools/audio/narrator/pick_takes.py [ordner] [--slow]

Standard-Ordner: build/narrator. Mit --slow werden Zeilen, die in lines.py ein "slow" haben, etwas gedehnt
(braucht sox) – für die Qwen-Stimme nicht nötig, sie spricht von sich aus ruhig genug.

Benötigt: pip install faster-whisper jiwer numpy sowie ffmpeg.
"""
import json
import re
import shutil
import subprocess
import sys
import wave
from pathlib import Path

import jiwer
import numpy as np
from faster_whisper import WhisperModel

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from lines import NARRATION  # noqa: E402


def words(s: str) -> str:
    return " ".join(re.sub(r"[^\wäöüß ]", " ", s.lower()).split())


def duration(path: Path) -> float:
    with wave.open(str(path)) as w:
        return w.getnframes() / w.getframerate()


def main() -> None:
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    folder = Path(args[0]) if args else Path("build/narrator")
    use_slow = "--slow" in sys.argv[1:]
    takes = folder / "takes"
    lines_out = folder / "lines"
    lines_out.mkdir(parents=True, exist_ok=True)
    model = WhisperModel("small", device="cpu", compute_type="int8")

    report = {}
    for lang in ("de", "en"):
        for i, line in enumerate(NARRATION, start=1):
            files = sorted(takes.glob(f"{lang}_{i:02d}_*.wav"))
            if not files:
                continue
            durs = [duration(f) for f in files]
            mean = sum(durs) / len(durs)
            scored = []
            for f, d in zip(files, durs):
                pcm = subprocess.run(["ffmpeg", "-loglevel", "error", "-i", str(f), "-f", "f32le", "-ac", "1", "-ar",
                                      "16000", "-"], check=True, capture_output=True).stdout
                segments, _ = model.transcribe(np.frombuffer(pcm, dtype=np.float32), language=lang, beam_size=5)
                heard = " ".join(s.text for s in segments).strip()
                wer = jiwer.wer(words(line[lang]), words(heard))
                scored.append((round(wer, 3), abs(d - mean), f, heard, round(d, 2)))
            scored.sort(key=lambda s: (s[0], s[1]))
            wer, _, best, heard, d = scored[0]
            target = lines_out / f"narration_raw_{lang}_{i:02d}.wav"
            slow = line.get("slow", 1.0) if use_slow else 1.0
            if slow > 1.0:
                subprocess.run(["sox", str(best), str(target), "tempo", "-s", f"{1.0 / slow:.3f}"], check=True)
            else:
                shutil.copy(best, target)
            report[f"{lang}_{i:02d}"] = {"take": best.name, "wer": wer, "heard": heard, "duration": d,
                                         "all": [(s[2].name, s[0], s[4]) for s in scored]}
            print(lang, i, best.name, "WER", wer, "|", heard, flush=True)
    (folder / "takes_report.json").write_text(json.dumps(report, indent=1, ensure_ascii=False), encoding="utf-8")


if __name__ == "__main__":
    main()
