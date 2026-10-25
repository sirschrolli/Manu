"""
Endgültige Erzählerstimme mit Qwen3-TTS (Apache-2.0): Die gewählte Hörprobe ist die Vorlage – ihre Stimme wird
geklont (ICL-Modus des Base-Modells), damit alle Zeilen gleich klingen. Alle neun Intro-Zeilen auf Deutsch und
Englisch, je mehrere Takes; pick_takes.py wählt danach mit Whisper den besten.

  qwen_final.py <vorlage.mp3> <ordner> <takes>
"""
import subprocess
import sys
import time
from pathlib import Path

import numpy as np
import soundfile as sf
import torch
from qwen_tts import Qwen3TTSModel

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "tools" / "audio"))
from lines import NARRATION  # noqa: E402
from texts import SAMPLE  # noqa: E402

torch.set_num_threads(4)
ref_path, folder, takes = Path(sys.argv[1]), Path(sys.argv[2]), int(sys.argv[3])
out = folder / "takes"
out.mkdir(parents=True, exist_ok=True)

pcm = subprocess.run(["ffmpeg", "-loglevel", "error", "-i", str(ref_path), "-f", "f32le", "-ac", "1", "-ar", "24000", "-"],
                     check=True, capture_output=True).stdout
ref_audio = (np.frombuffer(pcm, dtype=np.float32).copy(), 24000)
model = Qwen3TTSModel.from_pretrained("Qwen/Qwen3-TTS-12Hz-1.7B-Base", device_map="cpu", dtype=torch.float32)
prompt = model.create_voice_clone_prompt(ref_audio=ref_audio, ref_text=SAMPLE["de"])

for lang, language in (("de", "German"), ("en", "English")):
    for i, line in enumerate(NARRATION, start=1):
        text = line.get(f"{lang}_tts", line[lang])
        for k in range(takes):
            torch.manual_seed(100 * i + 7 * k + (0 if lang == "de" else 1000))
            start = time.time()
            wavs, sr = model.generate_voice_clone(text=text, language=language, voice_clone_prompt=prompt)
            sf.write(str(out / f"{lang}_{i:02d}_{k}.wav"), wavs[0], sr)
            print(f"{lang} Zeile {i} Take {k}: {len(wavs[0]) / sr:.2f} s in {time.time() - start:.0f} s", flush=True)
