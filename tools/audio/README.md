# Audio-Werkzeuge (Erzählerstimme, Intro-Musik, Soldaten-Stimmen)

Alle Audiodateien der Mod werden mit einem Befehl aus Text und Noten erzeugt –
es gibt keine fremden Aufnahmen. Ergebnis:

| Was | Datei(en) |
|---|---|
| Erzählerstimme Deutsch / Englisch (9 Zeilen) | `src/main/resources/assets/kingdomsatwar/sounds/intro/narration_de_01.ogg` … `narration_en_09.ogg` |
| Intro-Musik (gestreamt) | `…/sounds/intro/intro_music.ogg` |
| Soldaten-Sprachzeilen (6 Arten, je 3 Sätze × 2 Stimmen, DE und EN) | `…/sounds/voice/soldier/<art>_<sprache>_<nr><variante>.ogg` |
| Zeitplan des Intros | `…/assets/kingdomsatwar/intro/intro.json` |
| Sound-Ereignisse | `…/assets/kingdomsatwar/sounds.json` |

## Benötigt

* Python 3 mit `pip install piper-tts mido numpy scipy soundfile pyloudnorm librosa matplotlib`
  und `pip install --no-deps tinysoundfont` (ohne `--no-deps` will es PyAudio installieren, das hier nicht gebraucht wird)
* `ffmpeg` mit libvorbis
* Ein Ordner mit den Modellen (werden **nicht** im Repository gespeichert):
  * `de_DE-thorsten-high.onnx` + `.onnx.json` – deutscher Erzähler
  * `de_DE-thorsten_emotional-medium.onnx` + `.onnx.json` – deutsche Soldaten (Emotionen „neutral“ und „angry“)
  * `en_US-libritts_r-medium.onnx` + `.onnx.json` – englischer Erzähler und englische Soldaten
  * `FluidR3_GM.sf2` – Soundfont für die Musik

  Die Stimmen gibt es unter <https://huggingface.co/rhasspy/piper-voices> (Ordner `de/de_DE/thorsten/high`,
  `de/de_DE/thorsten_emotional/medium`, `en/en_US/libritts_r/medium`), den Soundfont z. B. im
  Debian/Ubuntu-Paket `fluid-soundfont-gm` (`/usr/share/sounds/sf2/FluidR3_GM.sf2`).

## Erzeugen

```bash
python3 tools/audio/build_audio.py --models /pfad/zu/den/modellen
# oder
KAW_AUDIO_MODELS=/pfad/zu/den/modellen python3 tools/audio/build_audio.py
```

Optionen:

* `--only narration,music,voices` – nur einzelne Teile neu erzeugen. Wird nur die Musik neu gebaut,
  nimmt das Skript die gespeicherten Längen der Erzählerzeilen aus dem Arbeitsordner.
* `--work /pfad` – Ordner für Zwischendateien (Standard: `build/audio-work`): MIDI-Datei der Musik,
  einzelne Instrumentenspuren, Spektrogramme, Messbericht `report.json`, Zeitplan `timeline.json`.

Die Sprachsynthese enthält Zufall – jede Neuerzeugung klingt minimal anders. Darum wird jede
Erzählerzeile in drei „Takes“ erzeugt und der beste genommen (ruhiges Tempo, saubere Stimme), und
der Zeitplan in `intro.json` wird immer zu den neuen Dateien passend berechnet.

## Wie es funktioniert

* **Texte:** `lines.py` (Erzähler und Soldaten). Angezeigter Text und gesprochener Text können sich
  unterscheiden (Schlüssel `de_tts` / `en_tts`), falls ein Wort falsch ausgesprochen wird.
* **Stimmen:** `tts.py` – Piper-Synthese mit eigenen Pausen an Doppelpunkt, Gedankenstrich und Satzende,
  danach Nachbearbeitung: Stille abschneiden, sanfter EQ (mehr Wärme, weichere Höhen), leichte
  Kompression, dezenter Raumhall, Lautheit **-16 LUFS** (Erzähler) bzw. **-15 LUFS** (Soldaten),
  True Peak unter -1,5 dBTP, Mono-OGG mit 44,1 kHz.
* **Englischer Erzähler:** LibriTTS-R-Sprecher **680**. Ausgewählt mit `select_speaker.py` aus über
  220 gemessenen Sprechern: tiefe Männerstimme (Grundfrequenz ca. 110 Hz), langsamstes Erzähltempo,
  höchste Klarheit (Harmonic-to-Noise-Ratio) und geringster Rauschanteil der Kandidaten, gleichmäßige
  Lautstärke und keine Aussetzer.
* **Musik:** `music.py` komponiert ein eigenes Stück in d-Moll (72 BPM) passend zum Zeitplan:
  leiser Beginn (tiefe Streicher, Harfe), Thema in der Flöte, Steigerung mit Tremolo-Streichern,
  Posaunen, Chor und Pauken, wenn die Drachen erwachen, geheimnisvolle Harfe und Celesta bei Eiern
  und Erzen, Hörner mit dem Hauptthema und Marschtrommel bei den Rittern, und die Auflösung nach
  **D-Dur** genau auf der letzten Zeile. Jede Instrumentenspur wird einzeln mit tinysoundfont gespielt,
  dann gemischt (Panorama, Hall-Anteil je Instrument, Konzertsaal-Hall). Während gesprochen wird,
  bleibt die Musik automatisch leiser (höchstens -21 LUFS momentan), in Pausen und im Nachspiel darf
  sie aufblühen (bis -15,5 LUFS). Gesamt-Lautheit dadurch etwa -22 LUFS.
* **Zeitplan:** Jede Zeile beginnt auf einem Schlag der Musik, jeder neue Abschnitt und die letzte
  Zeile auf einem Taktanfang. Deutsch und Englisch verwenden dieselben Startzeiten (jeweils genug
  Platz für die längere der beiden Fassungen), so passt eine einzige Musik zu beiden Sprachen.

## Lizenzen und Danksagung (für die README)

> **Audio:** Alle Stimmen wurden mit der freien Sprachsynthese [Piper](https://github.com/rhasspy/piper) erzeugt.
> Deutsche Stimmen: [Thorsten-Voice](https://github.com/thorstenMueller/Thorsten-Voice) von Thorsten Müller (CC0).
> Englische Stimmen: Piper-Stimme „libritts_r“, trainiert auf [LibriTTS-R](https://www.openslr.org/141/)
> (CC BY 4.0, Y. Koizumi u. a.; Sprecher 680, 248 und 788).
> Musik: eigene Komposition, gespielt mit dem Soundfont FluidR3_GM von Frank Wen (MIT-Lizenz).
