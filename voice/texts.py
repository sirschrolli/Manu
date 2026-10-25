"""Texte für die Hörproben der Erzählerstimme (Zeilen 1 und 3 des Intros)."""

LINES = {
    "de": ["Einst lag diese Welt still und unberührt.",
           "Doch tief in den Bergen, in Vulkanen, Eis und Meer, erwachten die Drachen."],
    "en": ["Once, this world lay silent and untouched.",
           "But deep in the mountains, in volcanoes, ice and sea, the dragons awoke."],
}
SAMPLE = {lang: " ".join(lines) for lang, lines in LINES.items()}
DESIGN = SAMPLE
