"""
Texte für die Erzählerstimme (Intro und Kapitel der Geschichte) und die Soldaten-Sprachzeilen.

Jede Zeile hat den angezeigten Text ("text") und – nur wo nötig – eine abweichende
Schreibweise für die Sprachsynthese ("tts"), z. B. um die Aussprache zu verbessern.
"""
from __future__ import annotations

# ---------------------------------------------------------------------------
# Erzähl-Intro „Das Siegel“ (11 Zeilen). Pause = Sekunden Stille NACH der Zeile (vor dem Ausrichten
# auf den Takt der Musik). "section" = Musikabschnitt, zu dem die Zeile gehört (die Namen sind die der
# Musik: world = die Dunkelheit, dragons = die sechs Drachen und das Siegel, wonder = die Last des Siegels,
# knights = der Auftrag an den Spieler, finale = die Warnung und der Titel).
# "slow" = zusätzlicher Faktor für das Sprechtempo (größer = langsamer, feierlicher).
# ---------------------------------------------------------------------------
NARRATION = [
    dict(section="world", pause=1.2, slow=1.06,
         de="Es gab eine Zeit, da stieg aus der Tiefe dieser Welt eine Dunkelheit empor, die alles Licht verschlang.",
         en="There was a time when a darkness rose from the depths of this world and swallowed every light."),
    dict(section="world", pause=1.6,
         de="Wälder verdorrten, Flüsse wurden schwarz, und die Menschen flohen vor den Schatten.",
         en="Forests withered, rivers turned black, and the people fled before the shadows."),
    dict(section="dragons", pause=1.0,
         de="Da erhoben sich sechs Drachen – geboren aus Feuer, Eis, Blitz, Natur, Erde und Wasser.",
         en="Then six dragons rose up – born of fire, ice, lightning, nature, earth and water."),
    dict(section="dragons", pause=1.0,
         de="Gemeinsam schmiedeten sie ein Siegel und verbannten die Dunkelheit tief unter die Erde.",
         en="Together they forged a Seal and banished the darkness deep beneath the earth."),
    dict(section="dragons", pause=1.6,
         de="Jeder von ihnen trägt seitdem einen Splitter dieses Siegels in seinem Herzen.",
         en="Ever since, each of them has carried a shard of that Seal in its heart."),
    dict(section="wonder", pause=1.0,
         de="Doch die Jahrhunderte vergingen, und die Last des Siegels machte sie wild.",
         en="But the centuries passed, and the burden of the Seal drove them wild."),
    dict(section="wonder", pause=1.6,
         de="Sie hüten ihre Nester – in Asche und Eis, auf Sturmgipfeln, in uralten Hainen, tief im Fels und am Grund "
            "des Meeres – und dulden niemanden in ihrer Nähe.",
         en="They guard their nests – in ash and ice, on stormy peaks, in ancient groves, deep within the rock and at "
            "the bottom of the sea – and suffer no one to come near."),
    dict(section="knights", pause=1.0,
         de="Nun ist deine Zeit gekommen. Gründe ein Königreich, sammle Ritter um dich und stelle ein Heer auf.",
         en="Now your time has come. Found a kingdom, gather knights around you and raise an army."),
    dict(section="knights", pause=1.5,
         de="Bezwinge die sechs Drachen und bring ihre Splitter zurück in den Kreis der Steine.",
         en="Defeat the six dragons and bring their shards back to the ring of stones."),
    dict(section="finale", pause=1.3, slow=1.06,
         de="Doch hüte dich: Was das Siegel verschließt, wartet nur darauf, dass es bricht.",
         en="But beware: what the Seal holds back is only waiting for it to break."),
    dict(section="finale", pause=0.0, slow=1.18,
         de="Dies ist dein Königreich.",
         en="This is your kingdom."),
]

# ---------------------------------------------------------------------------
# Der Erzähler zu den Kapiteln der Geschichte (dieselbe Stimme wie im Intro). Die Titelkarte eines Kapitels
# zeigt den Text und spielt die Stimme dazu; nach einem Sieg über einen Drachen folgen „fallen_<element>“ und
# „remaining_<n>“ (wie viele Drachen noch übrig sind) aufeinander. Datei: sounds/story/<id>_<de|en>.ogg.
# ---------------------------------------------------------------------------
STORY = [
    dict(id="kingdom",
         de="Jedes Königreich beginnt mit einem einzigen Schritt. Such dir ein Stück Land, pflanze dort dein Banner – "
            "und gewinne den Ersten, der dir sein Schwert anvertraut.",
         en="Every kingdom begins with a single step. Find a piece of land, plant your banner there – and win the first "
            "who will entrust you with his sword."),
    dict(id="chronicle",
         de="Nun folgen dir die ersten Getreuen. Schlag die Chronik auf: Sie erzählt von einem Kreis aus sechs Steinen, "
            "in dem das Siegel ruht. Dort beginnt dein eigentlicher Weg.",
         en="Now the first of the faithful follow you. Open the Chronicle: it tells of a ring of six stones where the "
            "Seal rests. That is where your true path begins."),
    dict(id="dragon_1",
         de="Sechs Steine, sechs leere Nischen. Jeder Drache trägt seinen Splitter im Herzen – und keiner von ihnen wird "
            "ihn freiwillig hergeben. Der Drachenkompass weist dir den Weg.",
         en="Six stones, six empty niches. Each dragon carries its shard in its heart – and not one of them will give it "
            "up willingly. The dragon compass will show you the way."),
    dict(id="fallen_fire",
         de="Pyrrhon, der Aschenfürst, ist gefallen. Seine Glut ist erloschen, doch sein Splitter brennt noch.",
         en="Pyrrhon, the Ash Lord, has fallen. His embers have died, but his shard still burns."),
    dict(id="fallen_ice",
         de="Glaciara, die Frostkönigin, ist gefallen. Das ewige Eis ist verstummt.",
         en="Glaciara, the Frost Queen, has fallen. The eternal ice has fallen silent."),
    dict(id="fallen_lightning",
         de="Voltarion, der Sturmbringer, ist gefallen. Über den Gipfeln verzieht sich das Gewitter.",
         en="Voltarion, the Stormbringer, has fallen. Above the peaks, the storm is passing."),
    dict(id="fallen_nature",
         de="Verdantis, die Hüterin des Hains, ist gefallen. Im uralten Wald ist es still geworden.",
         en="Verdantis, Guardian of the Grove, has fallen. The ancient forest has grown still."),
    dict(id="fallen_earth",
         de="Terragon, der Bergbrecher, ist gefallen. Die Erde hat aufgehört zu beben.",
         en="Terragon, the Mountainbreaker, has fallen. The earth has stopped trembling."),
    dict(id="fallen_water",
         de="Thalassa, die Herrin der Tiefe, ist gefallen. Das Meer liegt ruhig.",
         en="Thalassa, Mistress of the Deep, has fallen. The sea lies calm."),
    dict(id="remaining_5",
         de="Noch fünf Drachen bewachen das Siegel. Bring den Splitter zu seinem Stein im Kreis.",
         en="Five dragons still guard the Seal. Bring the shard to its stone in the circle."),
    dict(id="remaining_4",
         de="Noch vier Drachen. Mit jedem Splitter wird das Siegel schwächer – und das Dunkel darunter unruhiger.",
         en="Four dragons remain. With every shard the Seal grows weaker – and the darkness beneath grows restless."),
    dict(id="remaining_3",
         de="Die Hälfte ist vollbracht. Noch drei Drachen – und aus der Tiefe dringt ein Flüstern.",
         en="Half the task is done. Three dragons remain – and a whisper rises from the deep."),
    dict(id="remaining_2",
         de="Noch zwei Drachen. Wer nachts am Siegelkreis lauscht, hört es unter den Steinen pochen.",
         en="Two dragons remain. Whoever listens at the seal circle by night hears something knocking beneath the stones."),
    dict(id="remaining_1",
         de="Nur noch ein Drache bewacht das Siegel. Ruht auch sein Splitter im Kreis, wird es brechen. Sei bereit für "
            "das, was darunter wartet.",
         en="Only one dragon still guards the Seal. Once its shard, too, rests in the circle, the Seal will break. Be "
            "ready for what waits beneath."),
    dict(id="all_fallen",
         de="Der letzte Drache ist gefallen. Sechs Splitter, sechs Steine – das Siegel wartet auf dich.",
         en="The last dragon has fallen. Six shards, six stones – the Seal is waiting for you."),
    dict(id="seal",
         de="Das Siegel ist gebrochen. Was die Drachen so lange verschlossen hielten, ist erwacht. Das Tor steht offen – "
            "und dahinter wartet die Schattenwelt.",
         en="The Seal is broken. What the dragons kept locked away for so long has awoken. The gate stands open – and "
            "beyond it lies the Shadow World."),
    dict(id="crown",
         de="Die Schattenwelt. Seit Jahrhunderten lebt hier das Volk der Umbrer, und irgendwo in der Dunkelheit wartet "
            "Nyxar. Doch vergiss nicht: Daheim wartet ein Reich auf seine Krone.",
         en="The Shadow World. For centuries the Umbran folk have lived here, and somewhere in the darkness, Nyxar "
            "waits. But do not forget: back home, a realm awaits its crown."),
    dict(id="nyxar",
         de="Nyxar, der Schattendrache, ist gefallen. Die Schatten weichen zurück – doch sie vergessen nicht.",
         en="Nyxar, the Shadow Dragon, has fallen. The shadows recede – but they do not forget."),
    dict(id="crowned",
         de="Und so endet die Chronik des Siegels. Ein Königreich, geboren aus einem einzigen Schritt, hat seine Krone "
            "gefunden. Lang lebe die Krone!",
         en="And so ends the Chronicle of the Seal. A kingdom, born of a single step, has found its crown. Long live "
            "the crown!"),
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
    "subtitles.kingdomsatwar.story.narration": ("Erzähler spricht", "Narrator speaks"),
    "subtitles.kingdomsatwar.voice.soldier.recruit": ("Soldat meldet sich zum Dienst", "Soldier reports for duty"),
    "subtitles.kingdomsatwar.voice.soldier.follow": ("Soldat folgt", "Soldier follows"),
    "subtitles.kingdomsatwar.voice.soldier.hold": ("Soldat hält die Stellung", "Soldier holds position"),
    "subtitles.kingdomsatwar.voice.soldier.attack": ("Soldat ruft zum Angriff", "Soldier battle cry"),
    "subtitles.kingdomsatwar.voice.soldier.victory": ("Soldat jubelt", "Soldier cheers"),
    "subtitles.kingdomsatwar.voice.soldier.idle": ("Soldat murmelt", "Soldier mutters"),
}
