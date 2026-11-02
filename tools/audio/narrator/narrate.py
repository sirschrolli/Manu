#!/usr/bin/env python3
"""
Schritt 2 – alle Zeilen des Erzählers in der entworfenen Stimme sprechen (Qwen3-TTS Base, Apache-2.0).

Die Hörprobe aus design_voice.py (mitgeliefert: narrator_reference.mp3) dient als Vorlage; das Base-Modell übernimmt
daraus die Stimme (Voice-Clone im ICL-Modus), so klingen alle Zeilen auf Deutsch und Englisch gleich – die elf Zeilen
des Intros und die Zeilen zu den Kapiteln der Geschichte (lines.STORY). Jede Zeile entsteht in mehreren Takes;
pick_takes.py wählt danach den besten.

    python3 tools/audio/narrator/narrate.py [--ref vorlage.mp3] [--out ordner] [--takes 3] [--shard i/n]
                                            [--only intro|story]

Standard: narrator_reference.mp3, build/narrator, 3 Takes, alle Zeilen. Mit --shard i/n spricht das Skript nur jede
n-te Aufgabe (ab der i-ten, gezählt ab 0) – so lässt sich die Arbeit auf mehrere Rechner verteilen.
Ergebnis: <ordner>/takes/<de|en>_<NN>_<k>.wav (Intro) und <ordner>/takes/story_<id>_<de|en>_<k>.wav (Kapitel).
Dauer ohne Grafikkarte: etwa 1–2 Minuten je Take.

Benötigt: pip install torch --index-url https://download.pytorch.org/whl/cpu && pip install qwen-tts soundfile numpy
sowie ffmpeg (zum Einlesen der Vorlage).
"""
import argparse
import subprocess
import sys
import time
import zlib
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent))
from lines import NARRATION, STORY  # noqa: E402

# Transkript der Vorlage (der Wortlaut aus design_voice.py)
REFERENCE_TRANSCRIPT = ("Einst lag diese Welt still und unberührt. "
                        "Doch tief in den Bergen, in Vulkanen, Eis und Meer, erwachten die Drachen.")
LANGUAGES = (("de", "German"), ("en", "English"))


def tasks(only: str | None) -> list[tuple[str, str, str, str, int]]:
    """Alle Aufgaben: (Dateiname ohne Take, Sprache, Sprachname, Text, Zufallsbasis)."""
    out = []
    for lang, language in LANGUAGES:
        if only in (None, "intro"):
            for i, line in enumerate(NARRATION, start=1):
                seed = 100 * i + (0 if lang == "de" else 1000)
                out.append((f"{lang}_{i:02d}", lang, language, line.get(f"{lang}_tts", line[lang]), seed))
        if only in (None, "story"):
            for line in STORY:
                seed = 5000 + zlib.crc32(line["id"].encode()) % 40000 + (0 if lang == "de" else 50000)
                out.append((f"story_{line['id']}_{lang}", lang, language, line.get(f"{lang}_tts", line[lang]), seed))
    return out


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--ref", default=str(HERE / "narrator_reference.mp3"))
    ap.add_argument("--out", default="build/narrator")
    ap.add_argument("--takes", type=int, default=3)
    ap.add_argument("--shard", default="0/1")
    ap.add_argument("--only", choices=["intro", "story"], default=None)
    args = ap.parse_args()
    shard, shards = (int(v) for v in args.shard.split("/"))
    todo = tasks(args.only)[shard::shards]
    out = Path(args.out) / "takes"
    out.mkdir(parents=True, exist_ok=True)
    print(f"Teil {shard + 1} von {shards}: {len(todo)} Zeilen zu je {args.takes} Takes", flush=True)

    import numpy as np
    import soundfile as sf
    import torch
    from qwen_tts import Qwen3TTSModel

    torch.set_num_threads(4)
    pcm = subprocess.run(["ffmpeg", "-loglevel", "error", "-i", args.ref, "-f", "f32le", "-ac", "1", "-ar", "24000",
                          "-"], check=True, capture_output=True).stdout
    ref_audio = (np.frombuffer(pcm, dtype=np.float32).copy(), 24000)
    model = Qwen3TTSModel.from_pretrained("Qwen/Qwen3-TTS-12Hz-1.7B-Base", device_map="cpu", dtype=torch.float32)
    prompt = model.create_voice_clone_prompt(ref_audio=ref_audio, ref_text=REFERENCE_TRANSCRIPT)

    for name, lang, language, text, seed in todo:
        for k in range(args.takes):
            target = out / f"{name}_{k}.wav"
            if target.exists():
                continue
            torch.manual_seed(seed + 7 * k)
            start = time.time()
            wavs, sr = model.generate_voice_clone(text=text, language=language, voice_clone_prompt=prompt)
            sf.write(str(target), wavs[0], sr)
            print(f"{name} Take {k}: {len(wavs[0]) / sr:.2f} s in {time.time() - start:.0f} s", flush=True)


if __name__ == "__main__":
    main()
