"""
Spricht die Intro-Zeilen mit Chatterbox (MIT-Lizenz) in der Stimme der Referenzaufnahmen.

Jede Zeile wird einzeln erzeugt (wie später im Spiel) und mit einer Pause verbunden:
  raw_1.wav – Referenz A, ruhig-feierlich          (exaggeration 0.6, cfg 0.35)
  raw_2.wav – Referenz B (tiefer, getragener)      (exaggeration 0.65, cfg 0.3)
  raw_3.wav – Referenz B, sehr dramatisch          (exaggeration 0.9, cfg 0.25)

  chatterbox_epic.py <ordner> <sprache>
"""
import sys
from pathlib import Path

import torch
import torchaudio

_load = torch.load
torch.load = lambda *a, **k: _load(*a, **{**k, "map_location": torch.device("cpu")})
torch.set_num_threads(4)

from chatterbox.mtl_tts import ChatterboxMultilingualTTS  # noqa: E402
from texts import LINES  # noqa: E402

folder, lang = Path(sys.argv[1]), sys.argv[2]
model = ChatterboxMultilingualTTS.from_pretrained(device="cpu")
settings = [("1", "ref_A.wav", 0.6, 0.35, 0.8), ("2", "ref_B.wav", 0.65, 0.3, 0.8), ("3", "ref_B.wav", 0.9, 0.25, 0.85)]
for label, ref, exaggeration, cfg, temperature in settings:
    pieces = []
    for i, line in enumerate(LINES[lang]):
        torch.manual_seed(11 + i)
        wav = model.generate(line, language_id=lang, audio_prompt_path=str(folder / ref),
                             exaggeration=exaggeration, cfg_weight=cfg, temperature=temperature)
        pieces.append(wav)
        pieces.append(torch.zeros(1, int(model.sr * 0.9)))
    out = torch.cat(pieces[:-1], dim=1)
    torchaudio.save(str(folder / f"raw_{label}_{lang}.wav"), out, model.sr)
    print("fertig:", label, ref, exaggeration, cfg, flush=True)
