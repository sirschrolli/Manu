"""Wählt aus eval_<name>.json die besten Hörproben: Text richtig (WER), dann lebhafte Betonung und klare Stimme.
  pick_best.py <ordner> <bericht.json> <anzahl>"""
import json
import sys
from pathlib import Path

folder, report, count = Path(sys.argv[1]), sys.argv[2], int(sys.argv[3])
data = json.loads((folder / report).read_text())
ranked = sorted(data.items(), key=lambda kv: (kv[1]["wer"] > 0.1, kv[1]["wer"], -kv[1]["betonung_st"] - kv[1]["hnr"] * 0.05))
# höchstens eine Probe je Beschreibung, damit die Auswahl verschieden klingt
chosen, seen = [], set()
for name, m in ranked:
    kind = name.split("_")[1]
    if kind in seen:
        continue
    seen.add(kind)
    chosen.append(name)
    if len(chosen) == count:
        break
print(" ".join(chosen))
(folder / "chosen.txt").write_text("\n".join(chosen))
