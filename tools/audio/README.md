# Audio-Werkzeuge (Erzählerstimme, Intro-Musik, Soldaten-Stimmen)

Alle Audiodateien der Mod werden aus Text und Noten erzeugt – es gibt keine fremden Aufnahmen
und keine echten Sprecher. Ergebnis:

| Was | Datei(en) |
|---|---|
| Erzählerstimme des Intros, Deutsch / Englisch (11 Zeilen) | `src/main/resources/assets/kingdomsatwar/sounds/intro/narration_de_01.ogg` … `narration_en_11.ogg` |
| Erzähler zu den Kapiteln der Geschichte (19 Zeilen, DE/EN) | `…/sounds/story/<id>_<de\|en>.ogg`, Sprechdauern in `…/story/narration.json` |
| Intro-Musik (gestreamt) | `…/sounds/intro/intro_music.ogg` |
| Soldaten-Sprachzeilen (6 Arten, je 3 Sätze × 2 Stimmen, DE und EN) | `…/sounds/voice/soldier/<art>_<sprache>_<nr><variante>.ogg` |
| Zeitplan des Intros | `…/assets/kingdomsatwar/intro/intro.json` |
| Sound-Ereignisse | `…/assets/kingdomsatwar/sounds.json` |

## Die Erzählerstimme (Qwen3-TTS)

Der Erzähler des Intros ist eine **eigens entworfene, künstliche Stimme**: ein älterer Mann mit tiefer, rauer
Stimme, der wie ein alter König eine Heldensage am Feuer erzählt. Sie entsteht mit dem freien Sprachmodell
[Qwen3-TTS](https://github.com/QwenLM/Qwen3-TTS) (Apache-2.0) in drei Schritten, die Skripte liegen in `narrator/`:

1. `design_voice.py` – die Stimme wird nur **in Worten beschrieben** (VoiceDesign-Modell) und spricht eine kurze
   Hörprobe. Das Ergebnis liegt bei: `narrator/narrator_reference.mp3` (Zufallswert 11).
2. `narrate.py` – das Base-Modell übernimmt die Stimme aus dieser Hörprobe und spricht alle Zeilen (Intro und Kapitel) auf
   Deutsch und Englisch, je drei Takes (ohne Grafikkarte etwa 1,5 Stunden).
3. `pick_takes.py` – Whisper hört alle Takes ab und wählt je Zeile den, der am deutlichsten zu verstehen ist.

Auf GitHub erledigt das der Workflow **Erzählerstimme** (`.github/workflows/narrator.yml`): Er startet, wenn
`narrator/request.txt` geändert wird, verteilt das Sprechen auf acht Rechner, wählt die Takes, bearbeitet sie mit
`narrator/finish.py` nach (fertige `.ogg`-Dateien, `narrator/narration_timing.json`, `story/narration.json`) und legt das
Ergebnis als Commit auf den Branch. Musik und Zeitplan entstehen danach lokal:
`build_audio.py --only music --work <ordner>` mit einer Kopie von `narration_timing.json` in `<ordner>`.

Danach baut `build_audio.py` daraus die fertigen Dateien – mit **leichter** Nachbearbeitung (`--narration-style
natural`: sanfter EQ, behutsame Kompression, ein Hauch Raum), denn die Stimme klingt schon von sich aus
natürlich – und berechnet den Zeitplan und die Musik passend zu den neuen Zeilen:

```bash
pip install torch --index-url https://download.pytorch.org/whl/cpu
pip install qwen-tts soundfile numpy faster-whisper jiwer
python3 tools/audio/narrator/narrate.py                      # -> build/narrator/takes
python3 tools/audio/narrator/pick_takes.py                   # -> build/narrator/lines
python3 tools/audio/build_audio.py --models /pfad/zu/den/modellen --only narration,music \
        --narration-source build/narrator/lines --narration-style natural
```

Ohne `--narration-source` spricht `build_audio.py` den Erzähler wie früher mit Piper (siehe unten).

## Benötigt

* Python 3 mit `pip install piper-tts mido numpy scipy soundfile pyloudnorm librosa matplotlib`
  und `pip install --no-deps tinysoundfont` (ohne `--no-deps` will es PyAudio installieren, das hier nicht gebraucht wird)
* `ffmpeg` mit libvorbis
* Ein Ordner mit den Modellen (werden **nicht** im Repository gespeichert):
  * `de_DE-thorsten_emotional-medium.onnx` + `.onnx.json` – deutsche Soldaten (Emotionen „neutral“ und „angry“)
  * `en_US-libritts_r-medium.onnx` + `.onnx.json` – englische Soldaten (und Piper-Erzähler, falls ohne Qwen)
  * `de_DE-thorsten-high.onnx` + `.onnx.json` – nur für den Piper-Erzähler (falls ohne Qwen)
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
Erzählerzeile in drei „Takes“ erzeugt und der beste genommen, und der Zeitplan in `intro.json` wird
immer zu den neuen Dateien passend berechnet.

## Wie es funktioniert

* **Texte:** `lines.py` (Erzähler und Soldaten). Angezeigter Text und gesprochener Text können sich
  unterscheiden (Schlüssel `de_tts` / `en_tts`), falls ein Wort falsch ausgesprochen wird.
* **Stimmen:** `tts.py` – Nachbearbeitung aller Stimmen: Stille abschneiden, sanfter EQ (mehr Wärme,
  weichere Höhen), leichte Kompression, dezenter Raumhall, Lautheit **-16 LUFS** (Erzähler) bzw.
  **-15 LUFS** (Soldaten), True Peak unter -1,5 dBTP, Mono-OGG mit 44,1 kHz. Die Soldaten spricht
  Piper, mit eigenen Pausen an Doppelpunkt, Gedankenstrich und Satzende.
* **Piper-Erzähler (Ersatz ohne Qwen):** Deutsch Thorsten, Englisch LibriTTS-R-Sprecher **680**
  (ausgewählt mit `select_speaker.py` aus über 220 gemessenen Sprechern: tiefe Männerstimme,
  ruhiges Erzähltempo, hohe Klarheit).
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

> **Erzählerstimme:** erzeugt mit dem freien Sprachmodell [Qwen3-TTS](https://github.com/QwenLM/Qwen3-TTS)
> (Apache-2.0) – eine eigens beschriebene, künstliche Stimme, keine echte Person.
> **Soldatenstimmen:** erzeugt mit der freien Sprachsynthese [Piper](https://github.com/rhasspy/piper).
> Deutsche Stimmen: [Thorsten-Voice](https://github.com/thorstenMueller/Thorsten-Voice) von Thorsten Müller (CC0).
> Englische Stimmen: Piper-Stimme „libritts_r“, trainiert auf [LibriTTS-R](https://www.openslr.org/141/)
> (CC BY 4.0, Y. Koizumi u. a.; Sprecher 680, 248 und 788).
> **Musik:** eigene Komposition, gespielt mit dem Soundfont FluidR3_GM von Frank Wen (MIT-Lizenz).

## Umgebungsklänge der Biome (`ambience.py`)

`ambience.py` erzeugt alle Klangteppiche und Einzelklänge der Biome ohne Aufnahmen – nur aus Rauschen und Tönen
(Bausteine in `ambience_dsp.py`): Wind mit Böen und Pfeifen, Laub und Schilf, Grillen, Grashüpfer, Hummeln, Vogelstimmen
(Amsel, Meise, Fink, Kuckuck, Specht, Waldkauz, Nachtigall, Krähe, Möwe, Brachvogel, Rohrdommel, Bussard, Lerche), Frösche
und Unken, Brandung, Lagune, Moor, Glut, Lava, Eis, Gewitter, Höhlentropfen, das Dröhnen und Flüstern der Schattenwelt.
Die Teppiche sind kreisförmig berechnet (Rauschen, Steuerkurven, Ereignisse und Hall laufen über das Ende hinaus wieder am
Anfang weiter) und schließen deshalb ohne Naht.

```bash
python3 tools/audio/ambience.py                     # alles neu (etwa eine Minute)
python3 tools/audio/ambience.py --only leaves,bird_owl --preview /tmp/spektren
```

Es schreibt die `.ogg`-Dateien nach `…/sounds/ambient/`, die Einträge `ambient.bed.*`/`ambient.spot.*` in `sounds.json`
und `registry/AmbientSounds.java`. Welche Klänge wo erklingen, steht in `world/ambience/AmbienceProfiles.java`.

## Stimmen der Wesen der Leere (`void_creatures.py`)

`void_creatures.py` erzeugt die Stimmen der vier Wesen der Leere aus denselben Bausteinen: das nasse Gurgeln und das
anschwellende Ansaugen des Leerenschlunds, das kristallene Summen, Erwachen und Aufladen des Splitterwächters, das
zerhackte Knistern, Flackern, Zerfallen und Springen des Zerrlings (Körner, die springen und sich wiederholen, grob
abgetastet) und die gleitenden Rufe des Sternenrochens. Dazu der **Zerbrochene König**: ein tiefes, trauerndes
Summen wie von einem fernen Chor (Sägezahn durch Vokal-Formanten), brechender Marmor mit kurzem Stöhnen, ein gewaltiger,
rauer Schrei, das Erwachen (Trümmer klicken immer schneller zusammen, ein Chor schwillt an und bricht hell auf), das
schwere Fauchen des Wischhiebs, der Schwerkraftbruch (ein schwebendes Anschwellen, nach 2,1 Sekunden ein donnernder
Schlag – genau, wenn die Hände zusammenschlagen), das Brausen des Sternenstrahls und sein Zerfallen in einer Kaskade aus
zerspringendem Kristall. Wo Vanilla-Klänge passen (Biss, Schritte, splitternder Amethyst, Treffer), verweist
`sounds.json` nur auf sie.

```bash
python3 tools/audio/void_creatures.py [--preview /tmp/spektren]
```

Es schreibt die `.ogg`-Dateien nach `…/sounds/entity/void/` und die Einträge `entity.<wesen>.<klang>` in `sounds.json`.

## Klänge der Wesen und Bosse (seit 1.1.3/1.1.4)

Jedes Klangereignis gehört genau **einem** Skript; es schreibt nur seine eigenen Einträge in `sounds.json` (die
Untertitel bleiben). Alle Klang-Skripte kennen `--only name …` (nur diese Klänge neu, die übrigen Dateien bleiben) und
die meisten `--preview /pfad` (Spektrogramme und `.wav` zum Prüfen).

| Skript | Wesen |
|---|---|
| `dragon_sounds.py` | die Drachen (Stimme, Schwingen, Schritte, Biss, Atem aller sechs Elemente, das Ei) |
| `sea_boss_sounds.py` | Kraken (Biss, Schmerz, Tinte, Schlag, Woge, Strudel), Karkinos, Nyxar |
| `creature_sounds.py` | Höhlentroll, Lindwurm, Hirsch, Urelch, Gans, Flamingo (Schmerz, Tod), Moorschnapper, Hai, Anglerfisch, Mantarochen, Riesenkrabbe (Schritt), Leuchtqualle (Schmerz, Tod), Leerenschlund (Biss, Schmerz, Tod), Kommandostab |
| `monster_sounds.py`, `beast_sounds.py`, `sea_creatures.py`, `void_creatures.py`, `kraken_sounds.py` und die Skripte der Bosse | die übrigen eigenen Klänge |
| `../sound_events.py` | Verweise auf Klänge von Minecraft, wo sie gut passen (Wildschwein, Schneeleopard, Banditen, Soldaten …) |

**Lautheit:** Die neueren Skripte pegeln nach Lautheit (`fit_lufs` in `dragon_sounds.py`, LUFS mit sanftem Begrenzer),
die älteren nach Effektivwert (`fit_level`); sehr kurze, harte Klänge, die dabei zu leise blieben, stehen dort in `LUFS`.

**Kleine Lautsprecher:** Sehr tiefe Klänge (schwere Schritte, Aufschläge, Gebrüll riesiger Wesen) verlieren auf Laptops,
Fernsehern und manchen Kopfhörern fast alles. Die Skripte führen sie in `AUDIBLE`; `ambience_dsp.audible_bass` gibt
ihnen dann so viele Obertöne ihres Basses, dass oberhalb von 200 Hz höchstens 8 dB Lautheit verloren gehen – das Ohr hört
den tiefen Grundton daraus wieder hinzu.
