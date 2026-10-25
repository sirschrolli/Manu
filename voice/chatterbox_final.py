"""
Spricht alle Intro-Zeilen (Deutsch und Englisch) mit Chatterbox (MIT-Lizenz) in der gewählten Stimme,
jede Zeile in mehreren Takes (pick_takes.py wählt danach mit Whisper den besten).

  chatterbox_final.py <ordner> <Variante A|B|C> <Takes>
"""
import sys
from pathlib import Path

import torch
import torchaudio

_load = torch.load
torch.load = lambda *a, **k: _load(*a, **{**k, "map_location": torch.device("cpu")})
torch.set_num_threads(4)

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "tools" / "audio"))
from lines import NARRATION  # noqa: E402

# Variante: (Referenz, exaggeration, cfg_weight, temperature) – wie bei den Hörproben
VARIANTS = {"A": ("ref_B.wav", 0.65, 0.3, 0.8), "B": ("ref_B.wav", 0.9, 0.25, 0.85), "C": ("ref_A.wav", 0.6, 0.35, 0.8)}

folder, variant, takes = Path(sys.argv[1]), sys.argv[2], int(sys.argv[3])
ref, exaggeration, cfg, temperature = VARIANTS[variant]
out = folder / "takes"
out.mkdir(parents=True, exist_ok=True)

for lang in ("de", "en"):
    if lang == "de":
        from chatterbox.mtl_tts import ChatterboxMultilingualTTS
        model = ChatterboxMultilingualTTS.from_pretrained(device="cpu")
        speak = lambda text: model.generate(text, language_id="de", audio_prompt_path=str(folder / ref),  # noqa: E731
                                            exaggeration=exaggeration, cfg_weight=cfg, temperature=temperature)
    else:
        from chatterbox.tts import ChatterboxTTS
        model = ChatterboxTTS.from_pretrained(device="cpu")
        speak = lambda text: model.generate(text, audio_prompt_path=str(folder / ref),  # noqa: E731
                                            exaggeration=exaggeration, cfg_weight=cfg, temperature=temperature)
    for i, line in enumerate(NARRATION, start=1):
        text = line.get(f"{lang}_tts", line[lang])
        for k in range(takes):
            torch.manual_seed(100 * i + 7 * k + (0 if lang == "de" else 1000))
            wav = speak(text)
            torchaudio.save(str(out / f"{lang}_{i:02d}_{k}.wav"), wav, model.sr)
            print(f"{lang} Zeile {i} Take {k}: {wav.shape[-1] / model.sr:.2f} s", flush=True)
    del model
