#!/usr/bin/env python3
"""
Schritt 2 – alle Intro-Zeilen in der entworfenen Stimme sprechen (Qwen3-TTS Base, Apache-2.0).

Die Hörprobe aus design_voice.py (mitgeliefert: narrator_reference.mp3) dient als Vorlage; das Base-Modell übernimmt
daraus die Stimme (Voice-Clone im ICL-Modus), so klingen alle neun Zeilen auf Deutsch und Englisch gleich. Jede Zeile
entsteht in mehreren Takes; pick_takes.py wählt danach den besten.

    python3 tools/audio/narrator/narrate.py [vorlage.mp3] [ordner] [takes]

Standard: narrator_reference.mp3, build/narrator, 3 Takes. Ergebnis: <ordner>/takes/<de|en>_<NN>_<k>.wav.
Dauer ohne Grafikkarte: etwa 1–2 Minuten je Take (54 Takes ≈ 1,5 Stunden).

Benötigt: pip install torch --index-url https://download.pytorch.org/whl/cpu && pip install qwen-tts soundfile numpy
sowie ffmpeg (zum Einlesen der Vorlage).
"""
import subprocess
import sys
import time
from pathlib import Path

import numpy as np
import soundfile as sf
import torch
from qwen_tts import Qwen3TTSModel

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent))
from lines import NARRATION  # noqa: E402

# Transkript der Vorlage (der Wortlaut aus design_voice.py)
REFERENCE_TRANSCRIPT = ("Einst lag diese Welt still und unberührt. "
                        "Doch tief in den Bergen, in Vulkanen, Eis und Meer, erwachten die Drachen.")


def main() -> None:
    ref_path = Path(sys.argv[1]) if len(sys.argv) > 1 else HERE / "narrator_reference.mp3"
    folder = Path(sys.argv[2]) if len(sys.argv) > 2 else Path("build/narrator")
    takes = int(sys.argv[3]) if len(sys.argv) > 3 else 3
    out = folder / "takes"
    out.mkdir(parents=True, exist_ok=True)
    torch.set_num_threads(4)

    pcm = subprocess.run(["ffmpeg", "-loglevel", "error", "-i", str(ref_path), "-f", "f32le", "-ac", "1", "-ar", "24000",
                          "-"], check=True, capture_output=True).stdout
    ref_audio = (np.frombuffer(pcm, dtype=np.float32).copy(), 24000)
    model = Qwen3TTSModel.from_pretrained("Qwen/Qwen3-TTS-12Hz-1.7B-Base", device_map="cpu", dtype=torch.float32)
    prompt = model.create_voice_clone_prompt(ref_audio=ref_audio, ref_text=REFERENCE_TRANSCRIPT)

    for lang, language in (("de", "German"), ("en", "English")):
        for i, line in enumerate(NARRATION, start=1):
            text = line.get(f"{lang}_tts", line[lang])
            for k in range(takes):
                torch.manual_seed(100 * i + 7 * k + (0 if lang == "de" else 1000))
                start = time.time()
                wavs, sr = model.generate_voice_clone(text=text, language=language, voice_clone_prompt=prompt)
                sf.write(str(out / f"{lang}_{i:02d}_{k}.wav"), wavs[0], sr)
                print(f"{lang} Zeile {i} Take {k}: {len(wavs[0]) / sr:.2f} s in {time.time() - start:.0f} s", flush=True)


if __name__ == "__main__":
    main()
