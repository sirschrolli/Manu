"""
Hörproben mit Qwen3-TTS (Apache-2.0): eine eigene, künstlich gestaltete Stimme (keine echte Person),
beschrieben in Worten (VoiceDesign). Mehrere Beschreibungen und Zufallswerte; evaluate.py wählt aus.

  qwen_samples.py <ordner>
"""
import sys
import time
from pathlib import Path

import soundfile as sf
import torch
from qwen_tts import Qwen3TTSModel

torch.set_num_threads(4)
folder = Path(sys.argv[1])
folder.mkdir(parents=True, exist_ok=True)

TEXT = ("Einst … lag diese Welt still und unberührt.\n"
        "Doch tief in den Bergen, in Vulkanen, Eis und Meer … erwachten die Drachen.")

DESCRIPTIONS = {
    "sage": "An old man in his late sixties with a deep, warm, slightly raspy voice narrates an epic fantasy legend. "
            "Slow, solemn and dramatic, with strong emphasis on important words, rising and falling intonation and "
            "meaningful pauses. Completely natural and human, full of emotion, like a great audiobook narrator.",
    "king": "A deep-voiced elderly male storyteller, like a wise old king telling a heroic saga by the fire. "
            "Powerful, resonant chest voice with a gravelly texture. Expressive, passionate delivery with dramatic "
            "emphasis, suspenseful pauses and a sense of awe. Natural human speech, not robotic.",
    "erzaehler": "Ein älterer Mann um die siebzig mit tiefer, rauer und warmer Stimme erzählt eine epische Sage. "
                 "Langsam und feierlich, mit starker Betonung wichtiger Wörter, Spannung in der Stimme und "
                 "dramatischen Pausen. Sehr menschlich und ausdrucksstark, wie ein Hörbuchsprecher.",
}

model = Qwen3TTSModel.from_pretrained("Qwen/Qwen3-TTS-12Hz-1.7B-VoiceDesign", device_map="cpu", dtype=torch.float32)
for name, description in DESCRIPTIONS.items():
    for seed in (11, 23):
        torch.manual_seed(seed)
        start = time.time()
        wavs, sr = model.generate_voice_design(text=TEXT, language="German", instruct=description)
        sf.write(str(folder / f"qwen_{name}_{seed}.wav"), wavs[0], sr)
        print(f"{name} {seed}: {len(wavs[0]) / sr:.2f} s Audio in {time.time() - start:.0f} s", flush=True)
