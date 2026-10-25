"""
Gestaltet mit Parler-TTS (Apache 2.0) künstliche Stimmen eines älteren Mannes – nur nach Beschreibung,
ohne Vorbild einer echten Person.

  parler_design.py design <ordner> <sprache> <seeds>   – kurze Sätze mit allen Beschreibungen und Seeds
  parler_design.py sample <ordner> <sprache>           – volle Hörprobe mit der besten gestalteten Stimme
"""
import json
import re
import sys
import time
from pathlib import Path

import soundfile as sf
import torch
from parler_tts import ParlerTTSForConditionalGeneration
from transformers import AutoTokenizer, set_seed

from texts import DESCRIPTIONS, DESIGN, SAMPLE

torch.set_num_threads(4)
MODE, OUT, LANG = sys.argv[1], Path(sys.argv[2]), sys.argv[3]
OUT.mkdir(parents=True, exist_ok=True)
MODEL = "parler-tts/parler-tts-mini-multilingual-v1.1"

model = ParlerTTSForConditionalGeneration.from_pretrained(MODEL).eval()
tokenizer = AutoTokenizer.from_pretrained(MODEL)
description_tokenizer = AutoTokenizer.from_pretrained(model.config.text_encoder._name_or_path)
sr = model.config.sampling_rate


def generate(text: str, description: str, seed: int):
    set_seed(seed)
    desc = description_tokenizer(description, return_tensors="pt")
    prompt = tokenizer(text, return_tensors="pt")
    with torch.inference_mode():
        audio = model.generate(input_ids=desc.input_ids, attention_mask=desc.attention_mask,
                               prompt_input_ids=prompt.input_ids, prompt_attention_mask=prompt.attention_mask,
                               do_sample=True, temperature=0.9)
    return audio.cpu().numpy().squeeze()


if MODE == "design":
    results = []
    for d, description in enumerate(DESCRIPTIONS):
        for seed in (int(s) for s in sys.argv[4].split(",")):
            t0 = time.time()
            audio = generate(DESIGN[LANG], description, seed)
            name = f"parler_{LANG}_d{d}_s{seed}.wav"
            sf.write(OUT / name, audio, sr)
            results.append({"file": name, "description": d, "seed": seed, "seconds": round(time.time() - t0, 1)})
            print(results[-1], flush=True)
    (OUT / f"parler_{LANG}.json").write_text(json.dumps(results, indent=1))
else:
    best = json.loads((OUT / f"refs_{LANG}.json").read_text())[0]
    d, seed = (int(x) for x in re.match(r".*_d(\d+)_s(\d+)\.wav", best).groups())
    sf.write(OUT / f"sample_parler_{LANG}.wav", generate(SAMPLE[LANG], DESCRIPTIONS[d], seed), sr)
    print("fertig:", best, flush=True)
