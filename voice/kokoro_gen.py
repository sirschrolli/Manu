"""Englische Hörprobe mit Kokoro (Apache 2.0), britische ältere Männerstimme."""
import sys
from pathlib import Path

import numpy as np
import soundfile as sf
from kokoro import KPipeline

from texts import SAMPLE

folder = Path(sys.argv[1])
pipeline = KPipeline(lang_code="b")
for voice in ("bm_george", "bm_fable"):
    parts = [audio for _, _, audio in pipeline(SAMPLE["en"], voice=voice, speed=0.88)]
    sf.write(folder / f"sample_kokoro_{voice}_en.wav", np.concatenate(parts), 24000)
    print("fertig:", voice, flush=True)
