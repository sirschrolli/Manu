#!/usr/bin/env python3
"""
Schritt 1 – die Erzählerstimme entwerfen (Qwen3-TTS VoiceDesign, Apache-2.0).

Die Stimme ist rein künstlich: Sie wird nur in Worten beschrieben, es gibt keine echte Person und keine fremde
Aufnahme als Vorlage. Das Ergebnis ist eine kurze Hörprobe; aus ihr spricht narrate.py danach alle Intro-Zeilen in
genau derselben Stimme. Die mitgelieferte narrator_reference.mp3 ist so entstanden (Beschreibung unten,
Zufallswert 11). Je nach Version von PyTorch und Prozessor klingt eine Neuerzeugung minimal anders.

    python3 tools/audio/narrator/design_voice.py ziel.wav [zufallswert]

Benötigt: pip install torch --index-url https://download.pytorch.org/whl/cpu && pip install qwen-tts soundfile
(lädt beim ersten Start das Modell Qwen/Qwen3-TTS-12Hz-1.7B-VoiceDesign, ca. 4,5 GB; läuft auch ohne Grafikkarte).
"""
import sys
import time

import soundfile as sf
import torch
from qwen_tts import Qwen3TTSModel

# Gesprochener Text der Hörprobe – narrate.py braucht genau diesen Wortlaut als Transkript der Vorlage.
REFERENCE_TEXT = ("Einst … lag diese Welt still und unberührt.\n"
                  "Doch tief in den Bergen, in Vulkanen, Eis und Meer … erwachten die Drachen.")

DESCRIPTION = ("A deep-voiced elderly male storyteller, like a wise old king telling a heroic saga by the fire. "
               "Powerful, resonant chest voice with a gravelly texture. Expressive, passionate delivery with dramatic "
               "emphasis, suspenseful pauses and a sense of awe. Natural human speech, not robotic.")


def main() -> None:
    target = sys.argv[1]
    seed = int(sys.argv[2]) if len(sys.argv) > 2 else 11
    torch.set_num_threads(4)
    model = Qwen3TTSModel.from_pretrained("Qwen/Qwen3-TTS-12Hz-1.7B-VoiceDesign", device_map="cpu",
                                          dtype=torch.float32)
    torch.manual_seed(seed)
    start = time.time()
    wavs, sr = model.generate_voice_design(text=REFERENCE_TEXT, language="German", instruct=DESCRIPTION)
    sf.write(target, wavs[0], sr)
    print(f"{target}: {len(wavs[0]) / sr:.2f} s Stimme in {time.time() - start:.0f} s")


if __name__ == "__main__":
    main()
