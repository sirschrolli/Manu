#!/usr/bin/env bash
# Hörproben fertig machen: leicht (warm, wenig Hall) und episch (mehr Hall) – plus Fassung mit Intro-Musik.
#   finish_samples.sh <ordner> <datei1.wav> [datei2.wav ...]
set -euo pipefail
dir=$1; shift
cd "$dir"
n=1
for f in "$@"; do
  sox "$f" -r 44100 "probe_${n}_warm.wav" gain -4 highpass 60 bass +3 110 0.7q equalizer 3200 1.2q -1 \
      compand 0.02,0.25 -70,-70,-45,-30,-15,-13,0,-7 -3 -90 0.05 pad 0.2 0.8 reverb 18 50 60 0 10 -9 norm -1.5
  sox "$f" -r 44100 "probe_${n}_episch.wav" gain -4 highpass 60 bass +4 110 0.7q equalizer 3200 1.2q -1.5 \
      compand 0.02,0.25 -70,-70,-45,-28,-15,-12,0,-6 -3 -90 0.05 pad 0.2 2.0 reverb 42 45 100 0 22 -5 norm -1.5
  n=$((n+1))
done
if [ -f intro_music.ogg ] && [ -f probe_1_episch.wav ]; then
  len=$(soxi -D probe_1_episch.wav)
  total=$(python3 -c "print(round($len + 3.5, 2))")
  sox intro_music.ogg -r 44100 -c 1 music_cut.wav trim 4.5 "$total" fade t 0.5 "$total" 2.5 vol 0.32
  sox probe_1_episch.wav voice_pad.wav pad 2.2 0
  sox -m music_cut.wav voice_pad.wav probe_1_mit_musik.wav norm -1
  rm -f music_cut.wav voice_pad.wav
fi
ls -la
