"""Spricht die Hörprobe mit Chatterbox (MIT-Lizenz) in der zuvor gestalteten, künstlichen Stimme."""
import json
import sys
from pathlib import Path

import torch
import torchaudio

_load = torch.load
torch.load = lambda *a, **k: _load(*a, **{**k, "map_location": torch.device("cpu")})
torch.set_num_threads(4)

from chatterbox.mtl_tts import ChatterboxMultilingualTTS  # noqa: E402
from texts import SAMPLE  # noqa: E402

folder = Path(sys.argv[1])
lang = sys.argv[2]
refs = json.loads((folder / f"refs_{lang}.json").read_text())
model = ChatterboxMultilingualTTS.from_pretrained(device="cpu")
settings = [("A", 0.5, 0.35), ("B", 0.6, 0.3)]
for (label, exaggeration, cfg), ref in zip(settings, refs + refs[:1]):
    torch.manual_seed(7)
    wav = model.generate(SAMPLE[lang], language_id=lang, audio_prompt_path=str(folder / ref),
                         exaggeration=exaggeration, cfg_weight=cfg, temperature=0.75)
    torchaudio.save(str(folder / f"sample_chatterbox_{label}_{lang}.wav"), wav, model.sr)
    print("fertig:", label, ref, flush=True)
