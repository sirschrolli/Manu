"""Wählt aus den gestalteten Stimmen die zwei besten Referenzen: fehlerfrei gesprochen, tief, klar, ruhiges Tempo."""
import json
import sys
from pathlib import Path

folder = Path(sys.argv[1])
lang = sys.argv[2]
report = json.loads((folder / f"eval_parler_{lang}.json").read_text())
scored = []
for name, m in report.items():
    if m["wer"] > 0.2 or m["f0_median"] <= 0:
        continue
    score = -abs(m["f0_median"] - 100) / 25 + m["hnr"] / 8 - m["wer"] * 4 - abs(m["chars_per_s"] - 11) / 4
    scored.append((round(score, 3), name))
scored.sort(reverse=True)
print(json.dumps(scored, indent=1))
picks = [n for _, n in scored[:2]]
(folder / f"refs_{lang}.json").write_text(json.dumps(picks))
