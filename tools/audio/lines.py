"""
Texte für die Erzählerstimme und die Soldaten-Sprachzeilen.

Jede Zeile hat den angezeigten Text ("text") und – nur wo nötig – eine abweichende
Schreibweise für die Sprachsynthese ("tts"), z. B. um die Aussprache zu verbessern.
"""
from __future__ import annotations

# ---------------------------------------------------------------------------
# Erzähl-Intro (9 Zeilen). Pause = Sekunden Stille NACH der Zeile (vor dem Ausrichten
# auf den Takt der Musik). "section" = Musikabschnitt, zu dem die Zeile gehört.
# "slow" = zusätzlicher Faktor für das Sprechtempo (größer = langsamer, feierlicher).
# ---------------------------------------------------------------------------
NARRATION = [
    dict(section="world", pause=1.0, slow=1.06,
         de="Einst lag diese Welt still und unberührt.",
         en="Once, this world lay silent and untouched."),
    dict(section="world", pause=1.6,
         de="Dann erhoben sich die Königreiche – mit Mauern aus Stein und Türmen, die bis an die Wolken reichten.",
         en="Then the kingdoms rose – with walls of stone and towers that reached up to the clouds."),
    dict(section="dragons", pause=1.1,
         de="Doch tief in den Bergen, in Vulkanen, Eis und Meer, erwachten die Drachen.",
         en="But deep within the mountains, in volcanoes, ice and sea, the dragons awoke."),
    dict(section="dragons", pause=1.6,
         de="Sechs Arten – geboren aus Feuer, Eis, Blitz, Natur, Erde und Wasser.",
         en="Six kinds – born of fire, ice, lightning, nature, earth and water."),
    dict(section="wonder", pause=1.2,
         de="Wer ihre Eier findet und sie mit Geduld aufzieht, gewinnt einen Verbündeten, mächtiger als jede Armee.",
         en="Whoever finds their eggs and raises them with patience gains an ally mightier than any army."),
    dict(section="wonder", pause=1.5,
         de="In der Tiefe warten seltene Erze: Draconium, Aetherit, Konterit – der Stoff, aus dem Legenden geschmiedet werden.",
         en="Deep below, rare ores lie waiting: Draconium, Aetherit, Konterit – the stuff that legends are forged from."),
    dict(section="knights", pause=1.4,
         de="Sammle Ritter und Soldaten um dich, errichte deine Burg und verteidige dein Land.",
         en="Gather knights and soldiers around you, raise your castle and defend your land."),
    dict(section="finale", pause=1.2, slow=1.08,
         de="Die Welt ist bereit. Die Drachen warten.",
         en="The world is ready. The dragons are waiting."),
    dict(section="finale", pause=0.0, slow=1.18,
         de="Dies ist dein Königreich.",
         en="This is your kingdom."),
]

# ---------------------------------------------------------------------------
# Soldaten-Sprachzeilen. Pro Kategorie mehrere Sätze; jeder Satz wird in zwei
# Stimmfärbungen erzeugt (Varianten a/b), damit mehrere Soldaten nicht gleich klingen.
# "emotion" gilt für die deutsche Stimme thorsten_emotional.
# ---------------------------------------------------------------------------
SOLDIER = {
    "recruit": dict(emotion="neutral", de=["Zu Befehl!", "Ich diene Euch, mein Herr!", "Mein Schwert gehört Euch!"],
                    en=["At your command!", "I serve you, my lord!", "My sword is yours!"]),
    "follow": dict(emotion="neutral", de=["Ich folge Euch!", "Wie Ihr befehlt!", "Direkt hinter Euch!"],
                   en=["I will follow you!", "As you command!", "Right behind you!"]),
    "hold": dict(emotion="neutral", de=["Ich halte die Stellung!", "Hier kommt keiner durch!", "Verstanden, ich bleibe hier."],
                 en=["I will hold this position!", "None shall pass!", "Understood, I will stay here."]),
    "attack": dict(emotion="angry", de=["Für das Königreich!", "Angriff!", "Zu den Waffen!"],
                   en=["For the kingdom!", "Attack!", "To arms!"]),
    "victory": dict(emotion="angry", de=["Der Feind ist besiegt!", "Sieg!", "Sie fliehen!"],
                    en=["The enemy is defeated!", "Victory!", "They are fleeing!"]),
    "idle": dict(emotion="neutral", de=["Alles ruhig.", "Keine Feinde in Sicht.", "Eine lange Wache heute."],
                 en=["All quiet.", "No enemies in sight.", "A long watch today."]),
}

# Untertitel (Schlüssel -> Deutsch/Englisch) für die Übersetzungsdateien
SUBTITLES = {
    "subtitles.kingdomsatwar.intro.narration": ("Erzähler spricht", "Narrator speaks"),
    "subtitles.kingdomsatwar.intro.music": ("Intro-Musik spielt", "Intro music plays"),
    "subtitles.kingdomsatwar.voice.soldier.recruit": ("Soldat meldet sich zum Dienst", "Soldier reports for duty"),
    "subtitles.kingdomsatwar.voice.soldier.follow": ("Soldat folgt", "Soldier follows"),
    "subtitles.kingdomsatwar.voice.soldier.hold": ("Soldat hält die Stellung", "Soldier holds position"),
    "subtitles.kingdomsatwar.voice.soldier.attack": ("Soldat ruft zum Angriff", "Soldier battle cry"),
    "subtitles.kingdomsatwar.voice.soldier.victory": ("Soldat jubelt", "Soldier cheers"),
    "subtitles.kingdomsatwar.voice.soldier.idle": ("Soldat murmelt", "Soldier mutters"),
}
