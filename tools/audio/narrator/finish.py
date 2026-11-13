#!/usr/bin/env python3
"""
Schritt 4 – die gewählten Takes zu fertigen Dateien der Mod machen (leichte Nachbearbeitung „natural“):

  * Intro: sounds/intro/narration_<de|en>_<NN>.ogg und die Sprechdauern in narrator/narration_timing.json
    (daraus berechnet build_audio.py danach Zeitplan und Musik: --only music mit dieser Datei im Arbeitsordner)
  * Kapitel: sounds/story/<id>_<de|en>.ogg und assets/kingdomsatwar/story/narration.json

    python3 tools/audio/narrator/finish.py [ordner]

Standard-Ordner: build/narrator (darin lines/ aus pick_takes.py). Benötigt sox und ffmpeg.
"""
import json
import shutil
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent))
import build_audio  # noqa: E402


def main() -> None:
    folder = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("build/narrator")
    work = folder / "work"
    work.mkdir(parents=True, exist_ok=True)
    print("Intro ...", flush=True)
    narr = build_audio.load_narration(folder / "lines", work, "natural")
    timing = {f"{k[0]}:{k[1]}": {"speech": v["speech"], "length": v["length"]} for k, v in narr.items()}
    (HERE / "narration_timing.json").write_text(json.dumps(timing, indent=1) + "\n", encoding="utf-8")
    print("Kapitel ...", flush=True)
    build_audio.load_story(folder / "lines", work, "natural")
    report = folder / "takes_report.json"
    if report.exists():
        shutil.copy(report, HERE / "takes_report.json")
    print("Fertig.")


if __name__ == "__main__":
    main()
