"""
Mischt eine Hörprobe des ganzen Intros (Musik + Erzähler nach dem Zeitplan in intro.json).

  preview_mix.py <assets/kingdomsatwar> <sprache de_de|en_us> <ziel.wav>
"""
import json
import subprocess
import sys
from pathlib import Path

assets, lang, target = Path(sys.argv[1]), sys.argv[2], sys.argv[3]
intro = json.loads((assets / "intro" / "intro.json").read_text(encoding="utf-8"))
sounds = assets / "sounds" / "intro"
inputs = ["-i", str(sounds / "intro_music.ogg")]
filters = ["[0:a]volume=0.85[m]"]
labels = ["[m]"]
for n, line in enumerate(intro["languages"][lang]["lines"], start=1):
    name = line["sound"].split(":")[1].replace("intro.narration.", "")    # z. B. de.1
    code, idx = name.split(".")
    inputs += ["-i", str(sounds / f"narration_{code}_{int(idx):02d}.ogg")]
    delay = int(line["start"] * 1000)
    filters.append(f"[{n}:a]adelay={delay}|{delay},volume=1.0[v{n}]")
    labels.append(f"[v{n}]")
filters.append("".join(labels) + f"amix=inputs={len(labels)}:normalize=0,alimiter=limit=0.95[out]")
subprocess.run(["ffmpeg", "-loglevel", "error", "-y", *inputs, "-filter_complex", ";".join(filters), "-map", "[out]",
                "-ac", "2", target], check=True)
print("Vorschau:", target)
