#!/usr/bin/env bash
# Epische Nachbearbeitung: Wärme (Bass), weniger Schärfe, minimal langsamer, sanfte Kompression,
# Hall wie in einer großen Halle. Dazu eine Fassung mit der Intro-Musik darunter.
#   epic.sh <ordner> <sprache>
set -euo pipefail
cd "$1"
lang=$2
for n in 1 2 3; do
  [ -f "raw_${n}_${lang}.wav" ] || continue
  sox "raw_${n}_${lang}.wav" -r 44100 "sample_${n}_${lang}.wav" \
      highpass 65 lowpass 12000 bass +4 110 0.7q equalizer 3200 1.2q -1.5 \
      tempo -s 0.97 compand 0.02,0.25 -70,-70,-45,-28,-15,-12,0,-6 -3 -90 0.05 \
      pad 0.3 2.2 channels 2 reverb 42 45 100 70 22 -5 norm -1.5
done
# Fassung mit Musik: Ausschnitt der Intro-Musik ab 4,5 s, Stimme ab 2,2 s darüber, Musik leiser
if [ -f sample_1_${lang}.wav ] && [ -f intro_music.ogg ]; then
  for n in 1 2 3; do
    [ -f "sample_${n}_${lang}.wav" ] || continue
    len=$(soxi -D "sample_${n}_${lang}.wav")
    total=$(python3 -c "print(round($len + 3.5, 2))")
    sox intro_music.ogg -r 44100 -c 2 music_cut.wav trim 4.5 "$total" fade t 0.5 "$total" 2.5 vol 0.32
    sox "sample_${n}_${lang}.wav" voice_pad.wav pad 2.2 0
    sox -m music_cut.wav voice_pad.wav "music_${n}_${lang}.wav" norm -1
    rm -f music_cut.wav voice_pad.wav
  done
fi
ls -la
