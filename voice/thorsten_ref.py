"""
Baut die Referenzaufnahmen für das Stimmklonen aus dem Thorsten-Voice-Datensatz 2022.10.

Thorsten Müller hat seine Stimme ausdrücklich für alle freigegeben (CC0, https://zenodo.org/records/7265581).
Gewählt werden ruhige, lange Aussagesätze; daraus entstehen zwei Referenzen (~11 s):
  ref_A.wav – natürlich
  ref_B.wav – langsamer und etwas tiefer (wirkt älter und getragener)

  thorsten_ref.py <datensatz.zip> <ausgabeordner>
"""
import random
import subprocess
import sys
import wave
import zipfile
from pathlib import Path

zip_path, out = Path(sys.argv[1]), Path(sys.argv[2])
work = out / "thorsten"
work.mkdir(parents=True, exist_ok=True)
z = zipfile.ZipFile(zip_path)
names = z.namelist()
wavs = {Path(n).stem: n for n in names if n.lower().endswith(".wav")}
print("Dateien:", len(names), "WAV:", len(wavs), flush=True)

sentences = []
for n in names:
    if not n.lower().endswith(".csv"):
        continue
    for line in z.read(n).decode("utf-8", "replace").splitlines():
        parts = line.split("|")
        if len(parts) < 2 or parts[0] not in wavs:
            continue
        text = parts[1].strip()
        if text.endswith(".") and 70 <= len(text) <= 150 and not any(c.isdigit() for c in text) \
                and "?" not in text and "!" not in text and '"' not in text:
            sentences.append((parts[0], text))
print("passende Sätze:", len(sentences), flush=True)
random.Random(2022).shuffle(sentences)


def duration(path: Path) -> float:
    with wave.open(str(path)) as w:
        return w.getnframes() / w.getframerate()


def build(name: str, picks, effects: list[str]):
    parts, total, used = [], 0.0, []
    for sid, text in picks:
        target = work / f"{sid}.wav"
        target.write_bytes(z.read(wavs[sid]))
        # Stille am Anfang und Ende entfernen, auf 24 kHz mono bringen
        trimmed = work / f"{sid}_t.wav"
        subprocess.run(["sox", str(target), "-r", "24000", "-c", "1", str(trimmed), "silence", "1", "0.05", "0.5%",
                        "reverse", "silence", "1", "0.05", "0.5%", "reverse"], check=True)
        d = duration(trimmed)
        if d < 3.5:
            continue
        parts.append(str(trimmed))
        used.append(text)
        total += d
        if total >= 11.0:
            break
    pause = work / "pause.wav"
    subprocess.run(["sox", "-n", "-r", "24000", "-c", "1", str(pause), "trim", "0", "0.35"], check=True)
    joined = []
    for p in parts:
        joined += [p, str(pause)]
    subprocess.run(["sox", *joined[:-1], str(out / name), *effects, "norm", "-1"], check=True)
    print(name, round(duration(out / name), 2), "s:", " | ".join(used), flush=True)


build("ref_A.wav", sentences[:12], [])
build("ref_B.wav", sentences[12:24], ["tempo", "-s", "0.93", "pitch", "-150"])
