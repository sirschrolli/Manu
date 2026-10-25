#!/usr/bin/env bash
# Hörproben fertig machen: natürlich (nur ein Hauch Raum, damit die Stimme menschlich bleibt) – plus die beste
# Stimme mit etwas mehr Hall und der Intro-Musik darunter.
#   finish_samples.sh <ordner> <datei1.wav> [datei2.wav ...]
set -euo pipefail
dir=$1; shift
cd "$dir"
n=1
for f in "$@"; do
  sox "$f" -r 44100 "probe_${n}.wav" gain -3 highpass 50 equalizer 120 1.0q +1.5 \
      compand 0.01,0.20 -70,-70,-40,-30,-15,-14,0,-8 -2 -90 0.05 pad 0.2 0.6 reverb 12 50 40 0 6 -12 norm -1.5
  n=$((n+1))
done
if [ -f intro_music.ogg ] && [ -f "$1" ]; then
  sox "$1" -r 44100 voice_hall.wav gain -3 highpass 50 equalizer 120 1.0q +2 \
      compand 0.01,0.20 -70,-70,-40,-30,-15,-14,0,-8 -2 -90 0.05 pad 0.2 1.8 reverb 30 45 80 0 15 -8 norm -1.5
  len=$(soxi -D voice_hall.wav)
  total=$(python3 -c "print(round($len + 3.5, 2))")
  sox intro_music.ogg -r 44100 -c 1 music_cut.wav trim 4.5 "$total" fade t 0.5 "$total" 2.5 vol 0.30
  sox voice_hall.wav voice_pad.wav pad 2.2 0
  sox -m music_cut.wav voice_pad.wav probe_1_mit_musik.wav norm -1
  rm -f music_cut.wav voice_pad.wav voice_hall.wav
fi
ls -la
