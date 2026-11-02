# Krieg der Königreiche – Plan und Stand

Nach der Geschichte „Das Siegel“ (Version 0.37) folgt das große Kriegssystem. Diese Datei beschreibt den Plan und
hält fest, was schon fertig ist.

**Festgelegt mit dem Auftraggeber (02.11.2026):**

| Frage | Entscheidung |
|---|---|
| Was zuerst? | **Das Kriegssystem mit Belagerungswaffen** (Dragon Architect und Drachen im Krieg danach) |
| Gegen wen? | **Feindliche KI-Königreiche** mit eigenen Burgen, Heeren und Bannern in der Welt |
| Wer bedient die Belagerungswaffen? | **Der Spieler selbst und auf Befehl auch seine Soldaten** |

---

## 1. Die feindlichen Königreiche

In jeder Welt herrschen **vier fremde Reiche**, jedes mit eigenem Namen, Herrscher, Wappen und Farben und einer
**Stammburg** in eigener Bauweise. Sie liegen weit draußen (etwa 900 bis 2500 Blöcke vom Weltursprung), je eins in
einer Himmelsrichtung, auf ebenem Land, mit Abstand zu den Drachennestern, zum Siegelkreis und zu den Dörfern.

| Reich | Herrscher | Wappen | Bauweise der Burg |
|---|---|---|---|
| **Königreich Eisenmark** | König Harald der Graue | weißer Turm auf Purpur | grauer Stein, Schieferdächer |
| **Herzogtum Rabenstein** | Herzogin Morwen | schwarzer Rabe auf Rot | dunkler Stein, rote Banner |
| **Fürstentum Dornfels** | Fürst Kasimir | goldene Sonne auf Orange | Sandstein und Terrakotta |
| **Grafschaft Silberau** | Graf Ansgar | weißer Schwan auf Grün | heller Kalkstein, grüne Dächer |

**Die Stammburg** (etwa 70 × 70 Blöcke): Ringmauer mit Wehrgang und Zinnen, vier Ecktürme, ein Torhaus mit
Fallgitter, im Hof Kaserne, Stall, Schmiede, Brunnen und Kapelle, in der Mitte der **Bergfried** mit dem **Thronsaal**,
in dem das **Reichsbanner** hängt. Wappen und Farben des Reichs an Bannern, Wandteppichen und Schilden.

**Die Besatzung:** feindliche Krieger in den Farben ihres Reichs – Fußknechte mit Schwert und Schild, Bogenschützen
auf den Mauern, Armbrustschützen in den Türmen, Ritter, ein Hauptmann am Tor und der **Herrscher** selbst im Thronsaal
(mit eigener Leiste wie ein Boss). Sie bewachen ihre Posten, schlagen Alarm, wenn jemand die Burg betritt, und
verfolgen Eindringlinge nur bis an den Rand ihres Landes. Gefallene Krieger ersetzt das Reich nach und nach.

**Die Eroberung:** Ist der Herrscher gefallen und kein Verteidiger mehr in der Nähe des Reichsbanners, setzt der
Spieler am Banner sein eigenes Zeichen: Die Burg gehört ihm. Die Banner wechseln die Farbe, die übrigen Krieger
ergeben sich, und die Burg kann eine **Garnison** aus eigenen Soldaten aufnehmen. Jede eroberte Burg bringt jeden Tag
**Tribut** in Münzen.

## 2. Belagerungswaffen

Gebaut am Feldherrentisch, aufgestellt wie ein Fahrzeug. Der Spieler setzt sich hinein, zielt mit der Maus und
schießt; mit dem Kommandostab weist er eine Mannschaft aus eigenen Soldaten zu und gibt ihr ein Ziel.

| Waffe | Wirkung |
|---|---|
| **Rammbock** | fahrbares Schutzdach mit Widder: bricht Tore, Fallgitter und Türen |
| **Katapult** | wirft Steinkugeln im Bogen: reißt Löcher in Mauern und Dächer |
| **Balliste** | schießt schwere Bolzen flach und weit: gegen Krieger, Reiter und andere Belagerungswaffen |
| **Tribok** | die große Wurfmaschine: weiter und verheerender als das Katapult, langsam nachzuladen |
| **Belagerungsturm** (später) | rollt an die Mauer, oben fällt eine Brücke auf den Wehrgang |

Belagerungswaffen beschädigen nur, was sie treffen (keine Kettenreaktionen), und nur außerhalb geschützter Bereiche
(eigene Siedlung, Siegelkreis).

## 3. Der Krieg geht weiter

- **Gegenangriffe:** Wer ein Reich angreift, ist mit ihm im Krieg – es schickt dann von Zeit zu Zeit einen
  Heerhaufen mit Hauptmann gegen die eigene Siedlung (abschaltbar).
- **Neue Truppen:** Speerträger, Armbrustschützen, Schildträger, schwere Fußknechte, berittene Ritter und
  Belagerungsknechte für die eigene Armee.
- **Drachen im Krieg** (danach): Drachen greifen auf Befehl Burgen und Heere an.

---

## Reihenfolge und Stand

| Schritt | Inhalt | Stand |
|---|---|---|
| K1a | Die vier Reiche: Namen, Herrscher, Wappen, Lage in der Welt, Zustand (Besatzung, erobert, im Krieg) | ✅ |
| K1b | Die Stammburgen in vier Bauweisen mit Thronsaal und Reichsbanner | ✅ |
| K1c | Feindliche Krieger in den Farben ihres Reichs, Posten, Alarm, Nachschub | ✅ |
| K1d | Der Herrscher, die Eroberung und der Tribut | ✅ |
| K1e | Die Reiche finden: Grenzmeldung, Karten beim Kartografen, `/kaw realms` | ✅ |
| K1f | Tests, Bilder, Doku, Version 0.38.0 | ✅ |
| M | Die Arkane Sphäre: Erzmagier ziehen Spieler und Truppen in ihre Arena-Dimension (Version 0.39.0) | ✅ |
| K2 | Belagerungswaffen: Rammbock, Katapult, Balliste, Tribok | 🔨 |
| K3 | Gegenangriffe der Reiche, neue Truppen | ⏳ |

## Was in 0.38.0 fertig ist

- **Stammburgen in der Welt:** genau vier je Welt, je Reich eine, 900 bis 2400 Blöcke vom Ursprung, nie in der Nähe
  von Nestern, Siegelkreis, Magiertürmen oder Dörfern. Bauplatz eingeebnet, ringsum ein sanfter Übergang ins Gelände.
- **Besatzung:** 28 Krieger je Burg auf festen Posten – Fußknechte, Wallschützen auf den Mauern, Armbrustschützen auf
  den Türmen, Burgritter im Thronsaal, der Burghauptmann am Tor und der Herrscher (mit Bossleiste; schwer angeschlagen
  trinkt er einmal einen Heiltrank). Wer die Mauern betritt, löst Alarm aus; wer einen Krieger erschlägt, liegt mit dem
  Reich im Krieg.
- **Nachschub:** Für jede Minute, die niemand in der Nähe der Burg ist, rückt ein gefallener Krieger nach – nie mehr,
  als gefallen sind. Der Herrscher kehrt nie zurück.
- **Eroberung:** Ist der Herrscher gefallen und steht kein Verteidiger mehr im Umkreis von 16 Blöcken um das
  Reichsbanner hinter dem Thron, genügt ein Rechtsklick auf das Banner: alle Banner der Burg tragen dann das Wappen des
  Spielers (blau mit goldenem Kreuz), die übrigen Krieger ziehen ab.
- **Tribut:** Jeden Tag Bronzemünzen und Waren des Landes in der Schatztruhe in der Kammer des Herrschers (höchstens
  eine Woche sammelt sich an).
- **Finden:** Beim ersten Betreten des Landes erscheint der Name des Reichs und seines Herrschers; der Kartograf
  verkauft eine Karte zur nächsten noch freien Stammburg (Banner des Reichs als Zeichen). `/kaw realms` (Operatoren)
  zeigt Lage und Zustand, `/kaw realms check` prüft, ob die Burgen in der Welt stehen.
