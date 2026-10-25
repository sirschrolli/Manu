"""
Wählt je Intro-Zeile den besten Take: Whisper muss den Text erkennen (niedrigste Wortfehlerrate),
bei Gleichstand gewinnt der Take mit der Dauer am nächsten am Mittelwert (keine abgeschnittenen oder
gedehnten Aufnahmen). Zeilen mit "slow" werden zusätzlich etwas langsamer gemacht.

  pick_takes.py <ordner>
"""
import json
import re
import shutil
import subprocess
import sys
import wave
from pathlib import Path

import jiwer
from faster_whisper import WhisperModel

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "tools" / "audio"))
from lines import NARRATION  # noqa: E402

folder = Path(sys.argv[1])
takes = folder / "takes"
lines_out = folder / "lines"
lines_out.mkdir(parents=True, exist_ok=True)
model = WhisperModel("small", device="cpu", compute_type="int8")


def words(s: str) -> str:
    return " ".join(re.sub(r"[^\wäöüß ]", " ", s.lower()).split())


def duration(path: Path) -> float:
    with wave.open(str(path)) as w:
        return w.getnframes() / w.getframerate()


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
            segments, _ = model.transcribe(str(f), language=lang, beam_size=5)
            heard = " ".join(s.text for s in segments).strip()
            wer = jiwer.wer(words(line[lang]), words(heard))
            scored.append((round(wer, 3), abs(d - mean), f, heard, round(d, 2)))
        scored.sort(key=lambda s: (s[0], s[1]))
        wer, _, best, heard, d = scored[0]
        target = lines_out / f"narration_raw_{lang}_{i:02d}.wav"
        slow = line.get("slow", 1.0)
        if slow > 1.0:
            subprocess.run(["sox", str(best), str(target), "tempo", "-s", f"{1.0 / slow:.3f}"], check=True)
        else:
            shutil.copy(best, target)
        report[f"{lang}_{i:02d}"] = {"take": best.name, "wer": wer, "heard": heard, "duration": d,
                                     "alle": [(s[2].name, s[0], s[4]) for s in scored]}
        print(lang, i, best.name, "WER", wer, "|", heard, flush=True)
(folder / "takes_report.json").write_text(json.dumps(report, indent=1, ensure_ascii=False))
