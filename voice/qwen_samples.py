"""
Hörproben mit Qwen3-TTS (Apache-2.0): eine eigene, künstlich gestaltete Stimme (keine echte Person),
beschrieben in Worten (VoiceDesign). Mehrere Beschreibungen und Zufallswerte; evaluate.py wählt aus.
Runde 2: noch stärkere Betonung und natürlichere, menschlichere Sprechweise.

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
    "bard": "An elderly male bard, around seventy, with a deep, weathered and husky voice, telling an ancient legend "
            "to a hushed audience. He speaks slowly and theatrically: he stresses the key words heavily, lets his "
            "voice swell with awe and then drop almost to a whisper, and pauses for suspense. Warm, breathy, with "
            "natural breathing - completely human, like a real person.",
    "grandfather": "A kind old grandfather with a low, warm and slightly trembling voice tells a magical story by the "
                   "fireplace. Very natural, intimate and emotional speech with clear emphasis on important words, "
                   "gentle dramatic pauses and lively intonation, exactly like a real person reading aloud.",
    "trailer": "A mature male voice actor in his sixties with a rich, deep, cinematic baritone narrates an epic fantasy "
               "film. Strong dynamic emphasis, building tension, powerfully stressed words, dramatic pauses and "
               "audible breaths. Natural, human and expressive, not synthetic.",
}

model = Qwen3TTSModel.from_pretrained("Qwen/Qwen3-TTS-12Hz-1.7B-VoiceDesign", device_map="cpu", dtype=torch.float32)
for name, description in DESCRIPTIONS.items():
    for seed in (11, 23):
        torch.manual_seed(seed)
        start = time.time()
        wavs, sr = model.generate_voice_design(text=TEXT, language="German", instruct=description)
        sf.write(str(folder / f"qwen_{name}_{seed}.wav"), wavs[0], sr)
        print(f"{name} {seed}: {len(wavs[0]) / sr:.2f} s Audio in {time.time() - start:.0f} s", flush=True)
