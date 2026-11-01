# Kingdoms at War – Dragon Warfare

![Kingdoms at War](src/main/resources/kingdomsatwar_banner.png)

Eine eigene, mittelalterliche Minecraft-Mod für **Minecraft Java 26.3** mit **NeoForge**.
Vor langer Zeit verschlossen sechs Elementardrachen die **Schattenwelt** – jeder trägt ein Sechstel des Siegels in
seinem Herzen. Gründe dein Königreich, wirb Ritter und Soldaten an, baue deine Burg, spüre die riesigen Nester der
Drachen auf und bezwinge sie. Aus jedem besiegten Drachen bleibt ein Ei, aus dem dein eigener Drache schlüpft – und
ein Splitter des Siegels. Der sechste Splitter bricht das Siegel …

> **Testversion 0.12.0 – Die Monster.**
> Die Geschichte mit Chronik, Kapiteln und Siegelkreis, **sechs riesige Drachen-Bosse** mit eigenem Körperbau,
> eigenen Angriffen und je einem gewaltigen Nest in einem eigenen Biom, **Nyxar, der Schattendrache**, in seiner
> Schattenzitadelle und die Bauwerke der Schattenwelt, die **Wirtschaft** mit Münzen, Stadt, Bürgern, Handel und
> Krönung, die **Magie** mit sechs Erzmagiern in ihren Magiertürmen, **acht eigene Biome** – dazu Drachen zum Reiten,
> Soldaten, Kreaturen, Ausrüstung, Bau-Blöcke, Essen und das erzählte Intro.
> Dazu **eigene Dörfer** in vier Bauweisen mit **eigenen Dorfbewohnern** in 17 Berufen.
> In 0.9 kam **kein einziges Vanilla-Bauwerk mehr in der Oberwelt** – an ihrer Stelle stehen **17 ganz eigene
> Bauwerke**: der Sternensturz mit dem Portal ins Ende, die Wasserstollen der Alten, Gezeitenschrein, Seedrachen-Gebeine,
> Karawanserei, Schlangentempel, Hexenbaum, Frostgrotte, Räuberlager, Köhlerei, Glutpforte, Muschelfeld, Korallenpalast,
> das verschüttete Kloster, die Stille Kathedrale im Reich des Sculk, die Arena der Prüfungen und die Raubritterburg.
> Alles, was man dort finden musste, bleibt erhältlich.
> In 0.10 kamen **eigene Hof- und Weidetiere statt der Vanilla-Tiere** – Hausrind, Hausschwein, Schaf, Huhn, Pferd,
> Esel, Maultier, Ziege und Hase in **40 Rassen**.
> In 0.11 kamen **eigene Wildtiere statt der Vanilla-Wildtiere** – Wolf, Fuchs, Bär, Panda, Hauskatze, Wildkatze,
> Papagei, Biene, Fledermaus, Gürteltier, Kamel und Lama (samt Händlerlama) in **50 Arten und Rassen** mit eigenen
> Modellen und Fellbildern: Grau-, Polar-, Schwarz-, Timber-, Steppen- und Rotwolf, der Fennek mit Riesenohren,
> Braunbär mit Schulterbuckel, Luchs mit Ohrpinseln, Hummel, Vampirfledermaus, Trampeltier mit zwei Höckern …
> Zähmen, Halsband, Wolfspanzer, Teppiche, Sattel, Bienenstöcke, der Papagei auf der Schulter und alle Beute bleiben,
> wie man sie kennt.
> Neu in 0.12: **eigene Monster statt der Vanilla-Monster** – alle Untoten und Nachtwesen der Oberwelt in 19 Arten und
> 39 Gestalten, dazu 51 untote Dorfbewohner: Wiedergänger als Bauer, Söldner oder Mönch, Mumien in Leinenbinden,
> Wasserleichen im Streifenhemd, Gerippe mit Ritterhelm oder Köcher, Frost-, Moor- und Dörrgebein, Kriecher mit Moos,
> Kakteen, Eiszapfen oder Leuchtpilzen, Spinnen nach Land, glimmende Höhlenspinnen, Schleime mit Gesicht, Hexen mit
> spitzem Hut, Nachtmahre, untote Pferde und Dörrkamele. Untote Dorfbewohner lassen sich heilen und werden wieder genau
> der Dorfbewohner, der sie waren. Spinnenreiter, Hühnerreiter, Skelettfallen, Überfälle, Prüfkammern und alle Beute
> funktionieren wie gewohnt.
> Alles läuft auf einem echten Minecraft-26.3-Server und besteht die automatischen Tests (133 GameTests).
> **Als Nächstes:** die übrigen Monster (Illager, Wächter, Brise, Knarz, Wärter), Wassertiere und Golems, danach
> Nether und End.

Das Konzept steht in [docs/KONZEPT.md](docs/KONZEPT.md) (mit allen Konzeptbildern), die Geschichte und der Plan des
großen Umbaus in [docs/DAS_SIEGEL.md](docs/DAS_SIEGEL.md).

![Die sechs Drachen-Bosse](docs/bilder/drachen_bosse.jpg)

---

## Entwicklungsstand

| Bereich | Stand |
|---|---|
| NeoForge-Projekt für Minecraft 26.3, Java 25, Gradle; Build und Tests auf GitHub | ✅ fertig |
| Erzähltes Intro beim ersten Betreten einer Welt (Kameraflug mit Drachen, Musik, Erzähler DE/EN) | ✅ fertig |
| Geschichte „Das Siegel“: Chronik als Buch, Kapitel mit Titelkarten, Siegelkreis mit sechs Monolithen, Siegelbruch, Schattentor | ✅ fertig |
| Sechs Drachen-Bosse (je ca. 25 Blöcke), jeder mit eigenem Körperbau, eigener Erweckung, eigenen Angriffen und Bossleiste | ✅ fertig |
| Sechs riesige Nester – genau eins je Art und Welt – mit eigenem Biom und neuen Blöcken; Drachenkompass | ✅ fertig |
| Eigene Drachen: Eier nur von besiegten Bossen, fünf Wachstumsstufen, Reiten, Fliegen, Odem, Drachenpanzer | ✅ fertig |
| Ritter und Soldaten: Rekrutierungstisch, Befehle mit dem Kommandostab, Banditen | ✅ fertig |
| Wilde Kreaturen: Wildschwein, Rothirsch, Gans, Irrlicht, Höhlentroll, Lindwurm | ✅ fertig |
| Erze mit Bestimmung: Stahl, Konterit, Aetherit, Draconium, Wyvernit, Edelstein | ✅ fertig |
| Ausrüstung: Stahl, Konterit, Draconium, Drachenschuppen (mit Set-Bonus), Gleiter, Krone, Trollkeule | ✅ fertig |
| Magie: Aetherit-Stab, sieben Zauber in drei Stufen, Zaubertafeln, Magietisch, Zauberkreis (Radialmenü) | ✅ fertig |
| Sechs Erzmagier als Bosse, jeder in seinem eigenen Magierturm (sechs Bauweisen, je Element im passenden Land) | ✅ fertig |
| Bauen: Steine, Dächer, Fachwerk, Putz, Bleiglas, Burgtür, Fensterläden, Laternen, Kronleuchter, Zinnen, Möbel | ✅ fertig |
| Landwirtschaft: sieben Feldfrüchte, Gerichte, Getränke, Festmahl, Zierblumen | ✅ fertig |
| Deutsch und Englisch | ✅ fertig |
| Schattenwelt: eigene Dimension mit Biomen, Schattenvolk und Dörfern, Erzen, Kreaturen; Nyxar und seine Schattenzitadelle | ✅ fertig |
| Bauwerke der Schattenwelt: Mondtempel mit Gruft, Kristallminen, Mondleuchttürme, Ruinen der alten Welt, Felsbrücken | ✅ fertig |
| Wirtschaft: Münzen, Stadtbanner, Bürger mit acht Berufen, Baumeister und zwölf Gebäude, Handel, Ansehen, Krönung | ✅ fertig |
| Acht eigene Oberwelt-Biome mit eigenem Boden, eigenen Bäumen, Pflanzen, Felsen, Farben und Klängen | ✅ fertig |
| Eigene Dörfer in vier Bauweisen, Dorfbewohner in 17 Berufen mit Tagesablauf, Handel, Gerüchten und Dorfwachen | ✅ fertig |
| Keine Vanilla-Bauwerke mehr in der Oberwelt: 17 eigene Bauwerke an ihrer Stelle, alle wichtigen Funde bleiben erhältlich | ✅ fertig |
| Eigene Hof- und Weidetiere statt Vanilla: Rind, Schwein, Schaf, Huhn, Pferd, Esel, Maultier, Ziege, Hase in 40 Rassen | ✅ fertig |
| Eigene Wildtiere statt Vanilla: Wolf, Fuchs, Bär, Panda, Katzen, Papagei, Biene, Fledermaus, Gürteltier, Kamel, Lama in 50 Arten | ✅ fertig |
| Eigene Monster statt Vanilla: Untote und Nachtwesen in 19 Arten und 39 Gestalten, 51 untote Dorfbewohner (heilbar) | ✅ fertig |
| Übrige Monster (Illager, Wächter, Brise, Knarz, Wärter), Wassertiere und Golems statt Vanilla; Nether und End | 🔨 in Arbeit |
| Dragon Architect, Belagerungswaffen, Kriegssystem, Modrinth-Veröffentlichung | ⏳ später |

---

## Herunterladen und spielen

**Voraussetzungen**

- Minecraft Java Edition **26.3**
- **NeoForge 26.3.0.98** oder neuer ([NeoForge-Installer](https://neoforged.net/))
- Java 25 – bringt der Minecraft-Launcher automatisch mit

**Die Test-JAR bekommen**

1. Auf GitHub oben auf **Actions** klicken.
2. Den neuesten erfolgreichen Lauf von **Build** öffnen (grüner Haken).
3. Unten bei **Artifacts** die Datei **kingdomsatwar-0.12.0.jar** herunterladen
   (dafür musst du bei GitHub angemeldet sein).

Sobald es ein Release gibt, liegt die JAR auch dauerhaft unter **Releases**.

**Installieren**

1. NeoForge für 26.3 installieren und das NeoForge-Profil einmal im Launcher starten.
2. Die Datei `kingdomsatwar-0.12.0.jar` in den Ordner `mods` kopieren
   (Windows: `%appdata%\.minecraft\mods`).
3. Minecraft mit dem NeoForge-Profil starten und eine **neue** Welt anlegen: Die neuen Biome gibt es nur in neu
   angelegten Welten; Dörfer, die neuen Bauwerke, Erze, Kreaturen, Blumen, der Siegelkreis, die Drachennester und
   Burgruinen entstehen nur in neu erzeugten Gebieten.

Beim ersten Betreten einer Welt läuft das **Intro**: ein Kameraflug über die Welt, die sechs Drachen und dein
Königreich, mit eigener Musik und Erzähler. Überspringen mit Leertaste, Enter oder Esc, wieder ansehen mit `/kaw intro`.
Danach bekommst du die **Chronik des Siegels** – sie erzählt die Geschichte und zeigt dir den nächsten Schritt.
Im Kreativmodus gibt es sieben neue Tabs.

---

## Inhalte

### Die Geschichte: „Das Siegel“

Die **Chronik des Siegels** ist ein aufgeschlagenes Buch: links die Kapitel (erreicht, aktuell, noch verborgen) und
der Zustand des Siegels, rechts die Ziele des Kapitels, Hinweise und die Erzählung. Zu Beginn jedes Kapitels erscheint
eine Titelkarte.

| Kapitel | Was zu tun ist |
|---|---|
| Prolog | das Intro – die Welt, die Drachen, das Siegel |
| I Ein neues Königreich | mit dem Stadtbanner die Siedlung gründen und den ersten Bürger oder Soldaten anheuern |
| II Die Chronik des Siegels | den **Siegelkreis** finden (die Chronik verrät die Richtung) |
| III–VIII Die sechs Drachen | die Drachen in beliebiger Reihenfolge bezwingen und ihre Splitter einsetzen |
| IX Das Siegel bricht | der sechste Splitter öffnet das Tor in die Schattenwelt |

Der **Siegelkreis** liegt genau einmal pro Welt einige Hundert Blöcke vom Weltursprung entfernt auf ebenem Land:
eine runde Plattform aus Siegelstein mit sechs unzerstörbaren **Monolithen** – Gegensätze stehen sich gegenüber
(Feuer und Eis, Blitz und Wasser, Natur und Erde) – und dem großen Siegel in der Mitte. Jeder besiegte Drache
hinterlässt einen **Siegelsplitter**; in seinen Sockel eingesetzt, erwacht der Monolith und leuchtet in seiner Farbe.
**Der sechste Splitter bricht das Siegel:** Lichtsäulen, eine wachsende Lichtkugel, Risse, ein Blitz, eine
Druckwelle – und das Siegel wird zum **Schattentor**. Kapitel I und II gehören jedem Spieler selbst, die Drachen und
das Siegel der ganzen Welt.

![Der Siegelbruch: Lichtsäulen, Lichtkugel, Blitz und die Säule aus Schatten](docs/bilder/siegelbruch.jpg)

### Die Drachen-Bosse

Sechs einzigartige Bosse, je etwa 25 Blöcke lang. Jeder hat einen eigenen Körperbau (runde Formen, einzelne Zähne,
spitz zulaufende Hörner und Stacheln, Texturen in doppelter Auflösung), weiche, lebendige Bewegungen (Hals und
Schwanz schwingen nach, kraftvoller Flügelschlag, sauber angelegte Flügel, Gangarten mit Körperrollen, Atmen,
Ausholen und Nachschwingen), eine eigene Erweckung und eine eigene Bossleiste.

![Die neuen Bossleisten (links volle Kraft, rechts letzte Phase)](docs/bilder/bossleisten.jpg)

| Drache | Körperbau | Erweckung | Besondere Angriffe |
|---|---|---|---|
| **Pyrrhon**, der Aschenfürst (Feuer) | schwerer Drache mit vier Beinen, geschwungenen Hörnern und glühendem Feuersack | steigt aus dem Lavasee, Ascheregen | Flammenodem (auch im Flug), Feuerball-Salve, Meteorregen, Lava-Geysire, Flammenring |
| **Glaciara**, die Frostkönigin (Eis) | schlank, langer Hals, Kristallkrone, Flügel mit Eisfedern | ruht unsichtbar im Eiskristall, bis er zerspringt | Frostodem, Eisspeere aus dem Boden, Schneesturm, Eisgefängnis, Eiszapfenregen |
| **Voltarion**, der Sturmbringer (Blitz) | Wyvern: Flügel als Vorderbeine, Peitschenschwanz mit Klinge, Blitzstreifen | Gewitter zieht auf, er fährt mit einem Blitz herab | Kettenblitz, Blitzfeld, Sturzflug-Ansturm, Gewitter, Donnerschrei (betäubt) |
| **Verdantis**, die Hüterin des Hains (Natur) | Hirschgeweih, Hauer, Flügel aus Blättern, Moosmantel und Bäumchen auf dem Rücken | erhebt sich aus dem überwucherten Hügel | Giftsporen, Wurzelfessel, Dornensäulen, Samenregen, Rankendiener, Heilung aus den Blüten |
| **Terragon**, der Bergbrecher (Erde) | flügellos, massig, Felsplatten und Kristalle, Keulenschwanz | Erdbeben, bricht aus dem Boden | Felsbrocken, Erdbeben-Stampfer, Stalagmitenreihe, Eingraben und Auftauchen, Kristallpanzer |
| **Thalassa**, die Herrin der Tiefe (Wasser) | Seeschlange mit Flossen, Kopfkragen und Schwanzflosse | Strudel, steigt aus der Tiefe | Wasserstrahl, Flutwelle, Strudel, Blasengefängnis, Tiefensprung |

Dazu kommen Biss, Klaue, Schwanzhieb, Flügelschlag, Stampfen und Brüllen. Jeder Angriff kündigt sich an (Ausholen,
Ausführen, Erholen).

- **Erwacht erst aus nächster Nähe:** Der Drache schläft, bis man höchstens fünf Blöcke an ihn herankommt – wer nur
  am Nest vorbeiläuft, weckt ihn nicht (und sieht auch noch keine Bossleiste).
- **Trefferzonen:** Kopf, Hals, Körper, Flügel und Schwanz nehmen unterschiedlich viel Schaden.
- **Je mehr Schaden, desto gefährlicher:** Mit sinkendem Leben schlägt der Drache härter zu (bis zu 70 % mehr
  Schaden), greift schneller an (bis zu 40 %), bewegt sich schneller, macht kürzere Pausen, hängt Angriffe
  aneinander und setzt seine großen Angriffe häufiger ein.
- **Drei Phasen und ein letztes Aufbäumen:** Bei 60 % und 30 % Leben bäumt er sich brüllend auf – kurz
  unverwundbar, eine Druckwelle fegt über die Arena – und entfesselt dann seinen großen Angriff. Ab der zweiten Phase
  schlagen Gefahren seines Elements bei den Gegnern ein (Meteore, Eiszapfen, Blitze, Dornensaat, Felsbrocken,
  Wasserfontänen), jede mit leuchtendem Warnkreis; in der letzten Phase doppelt so oft, dazu eine Wut-Aura. Unter
  12 % bäumt er sich ein letztes Mal auf. Die Lebensleiste wächst mit dem Schwierigkeitsgrad und der Zahl der Spieler.
- **Bossleisten:** Jedes Element hat einen eigenen Rahmen (Flammen, Eiskristalle, Blitze, Ranken, Felsen, Wellen) mit
  dem Bildnis des Drachen, zwei Edelsteinen für die Phasen (sie zerspringen), strömender Füllung, einer Spur für
  erlittenen Schaden und Wut-Glühen in der letzten Phase.
- **Gemacht für den Kampf mit dem eigenen Heer:** Die Drachen greifen auch Soldaten an, Flächenangriffe treffen alle.
- Verlassen alle Spieler das Nest, kehrt der Drache in seinen Schlaf zurück und heilt sich.
- **Sieg:** Auf dem Nest-Altar erscheint im Lichtstrahl ein **reines Drachenei**, darüber schwebt der
  **Siegelsplitter**; dazu fallen viele Drachenschuppen, Draconium, Edelsteine und Schätze des Nests.

### Nyxar und die Schattenzitadelle

Hinter dem Siegel wartet der Endgegner: **Nyxar, der Schattendrache**, der „Lichtverschlinger“ – größer als die
sechs Elementdrachen (ca. 30 Blöcke), fast schwarz, mit einem Kamm aus Nachtkristallen, drei Augenpaaren, einer Krone
aus sechs Hörnern und zerfetzten Schwingen mit leuchtenden Adern. Zwischen seinen Rippen glimmt Seelenlicht.

- **Die Schattenzitadelle** steht genau einmal in der Schattenwelt (900–1700 Blöcke von der Mitte): eine achteckige
  Festung aus Schattenstein mit acht Türmen (Wohngeschosse, Leitern, Truhen, Turmhelme mit Kristallspitzen), Wehrgang
  mit Zinnen und leuchtenden Schießscharten, einem Torhaus mit Fallgitter und Freitreppe. Im offenen Thronhof liegt ein
  Siegel aus Seelenkristall, ein Prozessionsweg mit Seelenfeuer-Schalen führt zum Podest mit Nyxars Thron zwischen
  zwei Obelisken. In der Schattenwelt zeigt der **Drachenkompass** zur Zitadelle.
- **Erwachen:** Wer den Thronhof betritt, sieht Schatten sich spiralförmig sammeln – Nyxar nimmt Gestalt an, reißt
  die Schwingen auf und brüllt.
- **Angriffe:** Biss, Prankenhieb, Schwanzhieb, Flügelschlag, Schattenodem (auch im Flug, zehrt und verdunkelt die
  Sicht), Seelenpfeil-Salve, **Schattenschrei** (Dunkelheit und Schwäche), **Schattensprung** (zerfließt, gleitet als
  Seelenspur über den Boden und schlägt hinter seinem Ziel zu), **Schattenklauen** (greifen aus dunklen Flecken unter
  allen Gegnern empor), **Seelenraub** (Seelenfäden heilen ihn – weit genug weglaufen zerreißt sie), **Finsternis**
  (die Arena versinkt im Dunkel, Irrlichter jagen die Spieler) und in der letzten Phase **Schattenregen**.
- Magie und Gift von Nicht-Spielern prallen an ihm ab, Feuer trifft ihn härter.
- **Sieg:** Licht bricht über dem Thronhof durch, das **Herz der Schatten** schwebt dort, wo er fiel, dazu Abyssit,
  Seelenkristalle und Schattenessenz – und beim ersten Sieg endet die Geschichte vom Siegel.

### Bauwerke der Schattenwelt

Neben den Dörfern der Umbrer und der Zitadelle liegen überall in der Schattenwelt kleinere Bauwerke – etwa alle
200–300 Blöcke eines. Jedes sucht sich passendes Gelände (eben genug, nie unter Wasser, Leuchttürme nur an der Küste),
hält Abstand zu Dörfern und anderen Bauwerken und steht auf einer Lichtung. Mit `/locate structure` findet man sie
(`kingdomsatwar:moon_temple`, `crystal_mine`, `lighthouse`, `old_world_ruin`).

- **Verlassener Mondtempel** (Schattenwald, Bleiche Öde, Glimmerweiten): auf einem dreistufigen Sockel ein Säulenportikus
  mit Rosenfenster aus Nachtkristall, ein Schiff mit zwei Säulenreihen, Bänken und hängenden Mondlaternen, ein steiles
  Steindach, an drei Stellen eingestürzt (darunter Trümmer, Moos und Glimmerranken), und hinten die Apsis mit
  Halbkuppel, Mondaltar, Seelenkristall und einer Mondsichel aus Mondsilber. Eine Treppe führt hinab in die **Gruft**
  mit Sarkophagen – zwei Truhen mit Mondsilber, Seelenkristallen, Schattenessenz, verzauberten Büchern, selten einer
  Mondsilberklinge oder der Abyssit-Schmiedevorlage.
- **Kristallmine** (Kristallschluchten, Schattengipfel, Bleiche Öde): ein Förderturm aus Schattenholz über dem Schacht, eine
  Leiter 26 Blöcke hinab in eine Halle mit Stützbalken, Seelenlaternen und Truhe. Zwei Stollen mit Gleisen: nach
  Osten zu einer **Lore mit Beute**, nach Westen in eine **Kristallkammer** voller Nachtkristall – mit einem Spawner,
  aus dem Kristallkriecher kommen. In den Wänden glitzern Seelenkristall- und Mondsilberadern.
- **Mondleuchtturm** (an den Küsten): Felssockel, ein runder, nach oben schmaler werdender Turm mit Streifen aus
  Mondsilber, drei Geschosse mit Leiter, eine Galerie und der gläserne Laternenraum mit leuchtender Lampe unter einem
  Zeltdach. Daneben das Haus des Wärters (Bett, Ofen, Schornstein, Truhe) und ein Steg aufs Meer hinaus.
- **Ruine der alten Welt** (Glimmerweiten, Bleiche Öde, Schattenwald, Seelensumpf): ein Hof aus der Zeit vor dem Siegel – bemooste
  Mauerreste aus Bruchstein, Eichenpfosten, ein eingestürzter Balken, Kamin, Amboss, Truhe mit Vorräten der alten Welt
  (Stahl, Eisen, Saatgut, manchmal eine Karte) und ein Brunnen.
- **Felsbrücken**: natürliche Bögen aus Schattenstein über Schluchten und Senken, oben bewachsen, unten hängen
  Glimmerranken.

### Die neue Oberwelt

![Die acht neuen Biome](docs/bilder/biome.jpg)

In jeder neuen Welt wachsen **acht eigene Biome** zwischen den bekannten Landschaften. Jedes hat seinen eigenen Boden,
eigene Bäume, Pflanzen und Felsen, eigene Farben für Himmel, Nebel, Wasser, Gras und Laub, eigene Partikel in der
Luft, eigene Musik und Klänge.

| Biom | Wo | Was es ausmacht |
|---|---|---|
| **Aschefelder** | in einem Teil der Wüsten | Asche, Aschenerde und Vulkangestein; verkohlte Haine, Aschedünen, Glutspalten mit glühendem Grund, rauchende Schlote, Felsnadeln aus Vulkangestein, Feuerlilien; Asche weht durch die Luft |
| **Frostgipfel** | auf allen vereisten Gipfeln und einem Teil der Schneehänge | Schnee, Gletschereis, Packeis, Raureifstein, tückische Pulverschnee-Mulden; Eisnadeln mit Adern aus Blaueis und Frostkristallen auf den Spitzen, Eiszapfen unter Überhängen, Schneewehen, Frostblumen |
| **Sturmküste** | an allen steinigen Küsten | Sturmstein mit Kiesbändern; Brandungstürme aus Sturmstein und Schiefer, Findlinge in der Brandung, Fulgurite mit Sturmkristallen, vom Wind gebeugte Fichten, fernes Donnergrollen und Funken in der Luft |
| **Urhain** | in einem Teil der kühlen Wälder | uralte Riesenbäume aus Urholz auf Wurzelfüßen, mit Ranken voller Leuchtbeeren; umgestürzte, bemooste Stämme, Podsol, Moos und Leuchtmoos, Farne, Leuchtkappen, Hainglocken, Dornengestrüpp, Laubstreu und Glühwürmchen |
| **Felsschluchten** | auf den Savannen-Hochebenen und den windgepeitschten Savannen | Felswände in Bändern aus Tuff, Schiefer, Kalzit, Andesit, Terrakotta und Tropfstein; hohe Felsnadeln, Geröll, Bernsteindrusen, Akazien und trockenes Gras |
| **Korallenküste** | an den warmen Stränden und im flachen Wasser davor | weißer Sand, türkise Lagunen mit Korallengärten, Riffen, Seegras und Meeresgurken; Palmen, Perlmuttfelsen, Schildkröten und Tropenfische |
| **Königsauen** | in einem Teil der gemäßigten Ebenen | wogende Blumenwiesen aus Lavendel, Kornblumen, Gartenrosen, Margeriten und Mohn; mächtige Königseichen, kleine Eichenhaine, einzelne Birken; Rothirsche |
| **Nebelmoor** | in den kühlen Sümpfen | Torf, Schlamm und Moos; Mooraugen mit Schilf und Seerosen, tote Bäume und Moorbirken mit fahlem Moos, Blasseichen, Mondblumen; trübes Wasser und dichter Nebel |

- **Torf** brennt im Ofen so lange wie ein Boot, **Schilf** wächst im flachen Wasser und am Ufer.
- Die Biome entstehen in den Weltarten **Standard**, **Große Biome** und **Zerklüftet** – auch auf Servern. Bestehende
  Welten behalten ihre Landschaft; die neuen Biome gibt es nur in neu angelegten Welten.
- Drachennester, Magiertürme, der Siegelkreis, Erze, Blumen und Kreaturen kennen die neuen Biome (siehe die Tabellen
  unten).
- Finden mit `/locate biome kingdomsatwar:ash_fields` (bzw. `frost_peaks`, `storm_coast`, `primeval_forest`,
  `rock_gorges`, `coral_coast`, `royal_meadows`, `mist_moor`).

### Dörfer und Dorfbewohner

![Dörfer in den vier Bauweisen: Fachwerk, Nordisch, Lehm und Pfahlbau](docs/bilder/doerfer.jpg)

Die Vanilla-Dörfer sind fort – an ihrer Stelle stehen **eigene Dörfer in vier Bauweisen**, jede passend zu ihrem Land:

| Bauweise | Wo | Wie sie aussieht |
|---|---|---|
| **Fachwerk** | Ebenen, Sonnenblumenebenen, Wiesen, Laub-, Birken- und Blumenwälder, Königsauen | weißer Kalkputz zwischen dunklen Balken auf einem Sockel aus Mauerstein, rote oder braune Ziegeldächer, Bleiglasfenster mit grünen oder roten Läden |
| **Nordisch** | Taiga, verschneite Taiga, Schneeebenen, Urwald-Taiga | Blockhäuser aus Fichtenstämmen auf einem Feldsteinsockel, steile Dächer aus Schiefer oder Holzschindeln |
| **Lehm** | Wüste und Savanne | Lehm- und Kalkputz auf behauenem Sandstein, Flachdächer mit Brüstung, vorstehende Balken, bunte Sonnensegel |
| **Pfahlbau** | Sumpf, Mangrovensumpf, Nebelmoor | Holzhäuser auf Pfählen über Wasser und Schlamm, Stege statt Wegen, Schindeldächer |

- **Der Dorfplatz** in der Mitte ist gepflastert (im Moor eine Plattform auf Pfählen). Darauf steht ein Brunnen mit
  Dach und Dorfglocke, eine Dorflinde, ein Maibaum, ein Springbrunnen oder eine Feuerstelle; ringsum Bänke, Laternen
  und Marktstände.
- Vom Platz führen **Wege** in alle Richtungen, mit Seitengassen, Stufen am Hang und Brücken über Wasser. Daran stehen
  im Schnitt **17 Gebäude**: Wohnhäuser in drei Größen, Gasthaus, Schmiede, Bäckerei, meist eine Kapelle und vier bis
  sechs Werkstätten (Schreibstube, Kartenhaus, Kräuterhaus, Metzgerei, Gerberei, Steinmetz, Bogner). Am Dorfrand
  liegen Windmühle, Scheune, Felder (auch hinter den Häusern), eine Schafweide und am Wasser eine Fischerhütte mit Steg
  und Boot; an den Wegenden stehen Wachtürme.
- In den Truhen liegt, was in das Haus gehört (Vorräte, Werkzeug, Fisch, Wolle, Kräuter …), dazu ein paar Münzen.
- Dörfer liegen nie in der Nähe eines Drachennests (mindestens rund 450 Blöcke entfernt) und nicht am Siegelkreis.
  Vanilla-Dörfer und die Außenposten der Plünderer gibt es nicht mehr.
- Finden mit `/locate structure kingdomsatwar:village_timber` (bzw. `village_nordic`, `village_clay`, `village_stilt`).

![Die Dorfbewohner in ihren 17 Berufen](docs/bilder/dorfbewohner.jpg)

**Die Bewohner** sind Männer und Frauen in 17 Berufen, jeder mit eigenem Namen und eigener Kleidung (Strohhut, Kapuze,
Kappe mit Feder, Schürze, Bäckermütze, Gewand, Umhängetasche, Köcher …). **Rechtsklick** öffnet den **Handel** – mit
Münzen, wie bei den Bürgern deiner Siedlung; dein Ansehen senkt die Preise, nach einer Pause gibt es neue Ware.
**Schleichen + Rechtsklick** fängt ein **Gespräch** an.

| Beruf | Wohnt und arbeitet in | Handelt mit |
|---|---|---|
| Tagelöhner | Wohnhaus, Gasthaus | – (erzählt dafür gern) |
| Bauer | großem Haus, Windmühle, Scheune, Feld | Feldfrüchten, Saatgut und Brot |
| Fischer | Fischerhütte | Fisch, Eimern, Angel (auch verzaubert) und Booten |
| Schäfer | Schafweide | Wolle, Farben, Betten, Bannern, Gemälden und dem Webstuhl |
| Bogner | Bogner | Pfeilen, Bögen und Armbrüsten (auch verzaubert) und Trankpfeilen |
| Schreiber | Schreibstube | verzauberten Büchern, Uhr, Kompass und Namensschild |
| Kartograf | Kartenhaus | Karten zum nächsten Dorf, zum Siegelkreis, zu einem Magierturm oder einem Drachennest; Fernrohr, Drachenkompass |
| Kräuterkundiger | Kräuterhaus | Redstone, Lapis, Glowstone, Enderperlen, Erfahrungsfläschchen, Tränken und dem Braustand |
| Rüstungsschmied | Schmiede | Eisen-, Ketten- und Stahlrüstung, Schild, verzauberter Diamantrüstung |
| Waffenschmied | Schmiede | Eisen- und Stahlwaffen, Speer, verzauberten Waffen |
| Werkzeugschmied | Schmiede | Stein-, Eisen- und Stahlwerkzeug, auch verzaubert |
| Metzger | Metzgerei | Fleisch, Wildbret und Fleischpastete |
| Gerber | Gerberei | gefärbter Lederrüstung, Pferderüstung, Sattel, Bündel und Leine |
| Steinmetz | Steinmetz | Steinen, Terrakotta, Quarz und den Bau-Blöcken der Mod |
| Bäcker | Bäckerei | Brot, Gebäck, Kuchen und Gerichten |
| Wirt | Gasthaus | Essen, Bier, Wein und Met |
| Händler | Marktstand am Platz | Alltäglichem und wechselnden Raritäten aus der Ferne |

![Ein Tag im Dorf: Arbeit, Dorfplatz, Handel, Gerüchte und die Nacht im eigenen Bett](docs/bilder/dorfleben.jpg)

**Ein Tag im Dorf:**
- **Morgens** gehen alle an die Arbeit: mit dem Werkzeug ihres Berufs in der Hand, mit Hammerschlägen und Funken am
  Amboss, Staub beim Steinmetz, Rauch beim Bäcker und Metzger; der Bauer geht übers Feld, erntet und sät gleich nach.
  Zwischen den Schichten machen sie Pause.
- **Nachmittags** trifft sich das Dorf auf dem Platz.
- **Abends** geht jeder heim, **nachts** schläft jeder in seinem eigenen Bett.
- Bei **Regen** stellen sie sich unter, vor Monstern laufen sie davon.

**Gerüchte:** Im Gespräch erzählen die Bewohner, was man sich so erzählt – wo ein Drache haust (mit Himmelsrichtung und
ungefährer Entfernung), wo der Siegelkreis liegt, wo das nächste Dorf ist, ob Regen kommt, und was man in ihrem Beruf
wissen sollte.

**Dorfwachen:** Schwertkämpfer am Dorfplatz und Bogenschützen auf den Wachtürmen beschützen die Bewohner vor Monstern –
und vor dir: Wer einen Bewohner angreift, hat die Wachen am Hals, und wer einen tötet, verliert 40 Ansehen.
Dorfwachen lassen sich nicht anheuern.

### Bauwerke der Oberwelt

![Die eigenen Bauwerke der Oberwelt](docs/bilder/bauwerke.jpg)

Kein einziges Vanilla-Bauwerk entsteht mehr in der Oberwelt. An die Stelle jedes Bauwerks tritt ein **ganz eigenes
Gegenstück** – andere Gestalt, anderes Thema, aber dieselbe Rolle in der Welt: Es steht im selben Land, ist ungefähr
gleich häufig, und was man dort unbedingt braucht (das Portal ins Ende, das Herz des Meeres, die Prüfungsschlüssel und
Tresore, die Totems der Hexer …), findet man weiterhin. In den Truhen liegt die Beute des alten Bauwerks, dazu eigene
Funde und Münzen.

| Bauwerk | statt | Wo | Was man dort findet |
|---|---|---|---|
| **Sternensturz** | Festung | wie die Festungen in Ringen um den Ursprung | ein Meteoritenkrater (oder ein Sternfels auf dem Wasser bzw. am Hang), im glühenden Kern das **Portal ins Ende**; eine Wendeltreppe hinab, ein Lager der Sternforscher mit Fernrohr |
| **Wasserstollen der Alten** | Mine | tief unter der Erde | gewölbte Kanäle mit Wasserrinnen, Säulenzisternen, Treppen, ein Brunnenhaus an der Oberfläche |
| **Gezeitenschrein** | vergrabener Schatz | Strände und Korallenküste | ein Schrein mit Säulen und einem Dreizack aus Prismarin, unter dem Altar das **Herz des Meeres** |
| **Gebeine eines Seedrachen** | Schiffswrack | im Meer und an Stränden | ein riesiges Drachenskelett, im Brustkorb die verschluckte Fracht (Karten, Ladung, Schatz) |
| **Karawanserei** | Wüstentempel | Wüste | eine verfallene Herberge mit Arkadenhof und Brunnen; darunter der Schatzkeller – mit Falle |
| **Schlangentempel** | Dschungeltempel | Dschungel | ein steinerner Schlangenkopf auf einer Lichtung, eine Pfeilfalle im Schlund, eine Halle mit Wassergraben darunter |
| **Hexenbaum** | Sumpfhütte | Sumpf und Nebelmoor | ein hohler, abgestorbener Riesenbaum, unten die Höhle der Hexe, oben eine Plattform in der Krone |
| **Frostgrotte** | Iglu | verschneite Ebenen, Taiga und Hänge | die Felsgrotte eines Einsiedlers mit Holzvorbau, unter dem Teppich eine Luke zum Keller |
| **Räuberlager** | Plünderer-Außenposten | Ebenen, Savanne, Taiga, Wüste … | eine Palisade mit Wachturm und Zelten, ein Gefangener im Käfig, Wegelagerer mit Armbrüsten |
| **Köhlerei** | verlassenes Lager | Wälder und Taiga | ein rauchender Meiler auf einer Lichtung, Holzstapel, Unterstand, ein vergrabenes Versteck |
| **Glutpforte** | Portalruine | überall in der Oberwelt | ein zerbrochenes Tor aus Obsidian zwischen Basaltsäulen, ringsum glühender Boden; wer die Steine ergänzt, kann es entzünden |
| **Muschelfeld** | Ozeanruinen | Ozeane | Riesenmuscheln mit leuchtenden Perlen und Truhen, alte Scherben im Sand (warm mit Korallen, kalt mit Seetang) |
| **Korallenpalast** | Ozeanmonument | tiefe Ozeane | eine Halle unter einer Glaskuppel, vier Türme aus lebenden Korallen, ein Thron auf **acht Goldblöcken**, ein alter Wächter, die **Schwammgrotte** |
| **Verschüttetes Kloster** | Pfadruinen | Taiga, Urwald, Dschungel | nur der Glockenturm schaut aus dem Waldboden; unten Kreuzgang, Skriptorium, Kapelle – und Funde im Kies für den Pinsel |
| **Stille Kathedrale** | antike Stadt | tief im Reich des Sculk | eine Kathedrale aus Tiefenschiefer in einer riesigen Höhle: Türme, Fensterrose, Strebebögen, eine Kupferorgel – und Kreischer, die den Wärter rufen |
| **Arena der Prüfungen** | Prüfungskammern | tief unter der Erde | ein Kolosseum aus Sandstein in einer Höhle; sechs Käfige mit **Prüfungsspawnern**, der Spawner der Böen in der Mitte, **Tresore** (einer unheilvoll) auf der Ehrentribüne |
| **Raubritterburg** | Waldanwesen | dunkler Wald, Blasser Garten | eine Burg auf einem Hügel mit Graben und Brücke, Ringmauer, Rundtürme, Bergfried mit Verlies (gefangene Hellegeister), Palas – und die Leute des Raubritters (Diener, **Hexer**, Armbrustschützen) |

- Enderaugen finden den Sternensturz; Schatzkarten, Ozean-Entdeckerkarten, Waldkarten, Prüfungskarten und die
  übrigen Karten der Kartografen zeigen auf die neuen Bauwerke.
- Finden mit `/locate structure kingdomsatwar:fallen_star` (bzw. `aqueduct`, `tide_shrine`, `sea_dragon`,
  `caravanserai`, `serpent_temple`, `witch_tree`, `frost_grotto`, `bandit_camp`, `charcoal_camp`, `ember_gate`,
  `clam_field`, `coral_palace`, `buried_abbey`, `silent_cathedral`, `trial_arena`, `robber_castle`).
- Die neuen Bauwerke entstehen nur in neu erzeugten Chunks.

### Die Nester

![Die sechs Nester](docs/bilder/nester.jpg)

Jedes Nest ist ein riesiges Bauwerk, das es **genau einmal pro Welt** gibt – immer im passenden Biom. Um jedes Nest
liegt ein eigenes Biom von gut 200 Blöcken Durchmesser, das die Landschaft mit neuen Blöcken umgestaltet und eigene
Farben, Partikel und Geräusche hat.

| Nest | Wo | Biom ringsum |
|---|---|---|
| Pyrrhons **Vulkankrater** | Badlands, Wüste, Aschefelder | **Aschelande:** Aschedünen, Aschenerde, verkohlte Bäume, Vulkangestein mit Glutrissen, rauchende Glutspalten, fallende Asche |
| Glaciaras **Gletscherkessel** | Eiszapfen-Ebene, Schneeebene, Schneehänge, Frostgipfel | **Frostkessel:** Gletschereis, Raureifstein, Frosterde, Frostkristalle, Eiszapfen, Schneetreiben |
| Voltarions **Sturmgipfel** | zerklüftete und steinige Gipfel | **Sturmklippen:** Sturmstein, geladener Sturmstein, Fulgurit, Sturmkristalle, Kupferadern, verfallene Wachtürme |
| Verdantis' Nest im **Uralten Hain** | Dschungel, dunkler Wald, Urhain | **Uralter Hain:** Riesenbäume aus Urholz, Leuchtmoos, Leuchtkappen, Dornengestrüpp, knorrige Wurzeln |
| Terragons **Felsschlucht** | windgepeitschte Hügel, Felsschluchten | **Zerklüftete Weiten:** Felsnadeln, Schiefer, Geröll, Spaltgestein, Bernsteindrusen |
| Thalassas **Versunkene Grotte** | am Grund des tiefen Ozeans | **Abgrundgrotte:** Abgrundstein und -sand, Leuchtkorallen, Perlmutt, Schlote, Walknochen, versunkene Säulen |

**Nester finden**

- Der **Drachenkompass** (Kompass, Draconium, 2 Edelsteine, Aetherit) zeigt zum nächsten Nest; Schleichen +
  Rechtsklick wählt das nächste.
- Die **Chronik** kennt Gerüchte über jedes Nest („weit im eisigen Norden …“), nach der Entdeckung die genaue
  Entfernung.
- Rauchsäulen und Warnungen in der Statuszeile kündigen ein Nest schon von Weitem an.
- Nester liegen weit auseinander, Dörfer und andere Bauwerke halten großen Abstand zu ihnen.

### Drachen

![Die sechs Drachenarten](docs/bilder/drachen.jpg)

Wilde Drachen gibt es nicht mehr: **Dracheneier bekommt man nur von besiegten Drachen-Bossen.** Ein Ei schlüpft,
wenn es am richtigen Ort liegt **und ein Spieler in der Nähe ist** (bis 24 Blöcke) – ein Drache schlüpft nie allein,
und der Schlüpfling gehört dem, der dabei war.

| Drachenart | Zum Ausbrüten … | Odem |
|---|---|---|
| Feuerdrache | nahe Lava oder Feuer legen | Flammen, setzen in Brand |
| Eisdrache | auf Eis oder Schnee legen | Frost, verlangsamt und lässt erstarren |
| Blitzdrache | bei Gewitter oder neben einem Blitzableiter | Funken und Blitzschläge |
| Naturdrache | im Hellen auf Gras, nahe Pflanzen | Sporen, vergiften |
| Erddrache | unter der Erde auf Stein | Geröll, wirft zurück und lähmt |
| Wasserdrache | unter Wasser | Wasserstrahl, wirft zurück und löscht Feuer |

- **Fünf Wachstumsstufen:** Schlüpfling, Jungdrache, Halbwüchsiger (kann fliegen und Odem speien), Ausgewachsen
  (reitbar), Uralt. Füttern mit Fleisch oder **Drachenfutter** (rohes Rind-, Schweine- und Hammelfleisch + Süßbeeren,
  ergibt 2) lässt ihn schneller wachsen und heilt ihn.
- **Schleichen + Rechtsklick:** folgen / sitzen / frei umherstreifen. **Rechtsklick** auf einen ausgewachsenen
  Drachen: aufsitzen. Springen zum Abheben und Steigen, Schleichen zum Sinken, **R** speit Odem.
- **Drachenpanzer** aus Stahl oder Draconium (feuerfest) für ausgewachsene Drachen; mit der Schere abnehmen.
- **Drachenschuppen** fallen von den Bossen und liegen in den Nestern (mit dem Pinsel freilegen).

### Ritter und Soldaten

![Hauptmann, Schwertkämpfer, Ritter und Bogenschütze](docs/bilder/soldaten.jpg)

Am **Rekrutierungstisch** (2 Smaragde, Buch, 3 Bretter, 2 Stöcke) wirbst du Soldaten an – bezahlt wird der Sold in
Münzen:

| Soldat | Sold | Rolle |
|---|---|---|
| Schwertkämpfer | 5 Silber | Nahkämpfer – das Rückgrat jeder Armee |
| Bogenschütze | 7 Silber | Fernkämpfer, schießt nie auf Freunde |
| Ritter | 1 Gold 5 Silber + 2 Stahlbarren | Plattenrüstung und Schild, hält die vorderste Reihe |
| Hauptmann | 2 Gold 5 Silber + 1 Edelstein | macht Soldaten in seiner Nähe stärker |

Jede **Kaserne** deiner Siedlung erlaubt acht Soldaten mehr.

Mit dem **Kommandostab** gibst du Befehle: auf einen Feind zeigen = Angriff, auf den Boden zeigen = Stellung halten
(Schleichen: Patrouille), in den Himmel zeigen = alle folgen, Schleichen + eigenen Soldaten anklicken = entlassen.
Soldaten nehmen Waffen und Rüstung an, die du ihnen gibst, und verletzen weder dich noch deine Tiere und Drachen.
**Banditen** streifen nachts durch die Lande und hausen manchmal in Burgruinen.

### Münzen, Siedlung und Krönung

Bezahlt wird mit **Bronze-, Silber- und Goldmünzen** (je zehn sind eine der nächstgrößeren; Rechtsklick wechselt zehn
in eine größere, Schleichen + Rechtsklick eine in zehn kleinere). Münzen lassen besiegte Monster und Banditen fallen,
sie liegen in den Truhen der Bauwerke, die Drachen-Bosse hüten Gold – und deine Bürger kaufen dir Waren ab.

**Das Stadtbanner** (Banner, Goldbarren, Stock, 3 Steinziegel) gründet deine **Siedlung** – jeder Spieler hat eine,
mit einem Namen, den du ändern kannst. Rechtsklick auf das Banner öffnet das **Stadtfenster**: Übersicht (Stufe,
Betten, Gebiet, Ansehen, Baustellen), Anheuern, Bewohner, Bauen und Krone. Nur du kannst dein Banner abbauen; fällt es,
bleibt die Siedlung bestehen, bis es wieder steht.

**Bürger** heuerst du mit Münzen an. Jeder braucht ein Bett (die ersten zwei schlafen im Lager am Banner) und – außer
dem Baumeister – seine Werkstatt. Tagsüber arbeiten sie, nachts gehen sie ins Bett; Zombies, Plünderer und Banditen
haben es auf sie abgesehen.

| Beruf | Lohn | Werkstatt | Arbeit |
|---|---|---|---|
| Baumeister | 4 Silber | – | baut die Baustellen Block für Block nach Plan |
| Bauer | 3 Silber | Feld | erntet, sät nach, pflegt die Pflanzen, bringt die Ernte in die Truhe |
| Holzfäller | 3 Silber | Holzfällerhütte | fällt wilde Bäume rund um die Hütte und pflanzt nach |
| Bergmann | 5 Silber | Mine | steigt in den Schacht und hackt an der Erzwand (Kohle, Eisen, Kupfer, selten Gold und Edelsteine) |
| Schmied | 8 Silber | Schmiede | handelt mit Stahlwaffen und -rüstungen |
| Händler | 8 Silber | Markt | kauft Ernte, Holz und Stein, verkauft Baustoffe und Nützliches |
| Wirt | 6 Silber | Taverne | verkauft Essen, Bier, Wein, Met und das Festmahl |
| Kartograf | 1 Gold | Kartenhaus | zeichnet Karten zum Siegelkreis und zu Drachennestern |

**Bauen:** Im Stadtfenster nimmst du einen **Bauplan**; mit ihm in der Hand zeigen Lichtpunkte den Umriss, Rechtsklick
auf den Boden richtet die Baustelle ein (Schleichen + Rechtsklick dreht den Plan). Der **Baumeister** räumt den Platz,
legt das Fundament und setzt dann Block für Block – mit Hammerschlag und Staub.

| Stufe (ab Bewohnern) | Gebäude |
|---|---|
| Lager (0) | Wohnhaus (2 Betten), Feld, Holzfällerhütte |
| Weiler (4) | Brunnen, Schmiede |
| Dorf (8) | Mine, Markt, Taverne |
| Marktflecken (14) | Bürgerhaus (4 Betten), Kaserne, Kartenhaus |
| Stadt (22) | Thronsaal |

**Ansehen** gewinnst du durch Anheuern, fertige Gebäude, Handel, besiegte Monster in deiner Siedlung und vor allem durch
besiegte Drachen. Es senkt die Preise deiner Bürger (bis 30 %). Jeder Laden macht nach 16 Handeln eine Pause von
drei Stunden – in **Echtzeit**, auch wenn das Spiel aus ist (beides einstellbar).

**Die Krönung:** Wer das Siegel gebrochen hat, eine Stadt regiert, legendäres Ansehen genießt und die **Königskrone**
trägt, setzt sich im eigenen **Thronsaal** auf den Thron: Glocken läuten, goldenes Licht steigt auf, die Bürger eilen
herbei, Feuerwerk erhellt den Himmel – und du wirst **König** oder **Königin**. Von da an geben dir deine Bürger alles
umsonst.

### Hof- und Weidetiere

![Die eigenen Hof- und Weidetiere](docs/bilder/hoftiere.jpg)

Die Tiere der Vanilla-Welt gibt es nicht mehr – an ihrer Stelle leben **eigene Hof- und Weidetiere** mit eigenen
Modellen, Fellbildern und Bewegungen, jedes in mehreren **Rassen**, die zum Land passen: zottelige Hochlandrinder und
Wollschweine im Norden, Steppenrinder mit Leierhörnern und Wüstenhasen im Süden, Schneeziegen an den Schneehängen.
Alles Gewohnte funktioniert wie bisher: Milch, Wolle (scheren, färben, nachwachsen), Eier, Reiten, Zähmen, Sattel,
Rossharnisch, Packtaschen, Ziegenhörner und die Beute beim Schlachten. Vanilla-Spawn-Eier, Befehle und geworfene Eier
bringen ebenfalls die neuen Tiere hervor; schon vorhandene Tiere einer alten Welt bleiben, wie sie sind.

| Tier | statt | Rassen | Besonderheiten |
|---|---|---|---|
| **Hausrind** | Kuh, Pilzkuh | Fleckvieh, Braunvieh, Hochlandrind, Steppenrind, Pilzrind | Hörner je Rasse, Stirnschopf beim Hochlandrind; das Pilzrind trägt Pilze auf dem Rücken und füllt Schüsseln mit Pilzsuppe |
| **Hausschwein** | Schwein | Landschwein, Sattelschwein, buntes Schwein, Wollschwein | Schlappohren, Ringelschwanz; mit Sattel und Karottenrute reitbar |
| **Schaf** | Schaf | Merinoschaf, Heidschnucke, Schwarzkopfschaf | Wolle in jeder Farbe, Heidschnucken mit Schneckenhörnern und natürlich grauer Wolle |
| **Huhn** | Huhn | weiße, braune, schwarze Henne, Sperberhenne, Hahn | Hennen legen weiße, braune oder (Sperberhenne) blaue Eier; der Hahn mit großem Kamm und Sichelfedern legt keine |
| **Pferd** | Pferd | Fuchs, Brauner, Friese, Apfelschimmel, Schecke, Haflinger, Kaltblut, Falbe, Pony | Abzeichen, lange Mähnen, Fesselbehang; das Kaltblut ist größer und kräftiger, das Pony klein; Herden meist einer Rasse |
| **Esel** | Esel | Grauesel, brauner Esel | lange Ohren, Schweifquaste, Packtaschen; mit einem Pferd gibt es Maultiere |
| **Maultier** | Maultier | Maultier, Rappmaultier | Eselsohren, Pferdeschweif, trägt Packtaschen |
| **Ziege** | Ziege | Steinbock, Gämse, Schneeziege, Hausziege | Hörner je Art (einzeln abbrechbar), Zottelfell der Schneeziege; rammt und springt wie bisher |
| **Hase** | Kaninchen | Feldhase, Schneehase, Wüstenhase, Wildkaninchen, Scheckenkaninchen, Mordshase | hoppelt mit gestreckten Läufen; der Mordshase mit glühenden Augen greift an (nur per Befehl) |

### Wildtiere

![Die eigenen Wildtiere](docs/bilder/wildtiere.jpg)

Auch die Wildtiere der Vanilla-Welt sind ersetzt: Überall, wo Vanilla ein Wildtier vorsieht, lebt mit dem gleichen
Gewicht ein **eigenes Gegenstück** – mit eigenem Modell und Fellbild, aber den vertrauten Bewegungen (Sitzen, Schlafen,
Lauern, Rollen, Einrollen, Aufrichten). Die Art richtet sich nach dem Land: Polarwolf und Polarfuchs im Schnee, Fennek
und Dromedar in der Wüste, Luchs in der Taiga. Zähmen, Halsbänder, Wolfspanzer, Teppiche, Packtaschen, Sattel,
Bienenstöcke, Honig, Gürteltier-Hornplatten und alle Beute bleiben, wie man sie kennt; Vanilla-Spawn-Eier, Bienen aus
Nestern, Dorfkatzen und die Lamas des fahrenden Händlers werden zu den neuen Tieren.

| Tier | statt | Arten und Rassen | Besonderheiten |
|---|---|---|---|
| **Wolf** | Wolf | Grauwolf, Polarwolf, Schwarzwolf, Timberwolf, Steppenwolf, Rotwolf | Rudel einer Art; wütend mit roten Augen; eigenes Halsband, Wolfspanzer; jagt auch die eigenen Schafe, Hasen und Füchse |
| **Fuchs** | Fuchs | Rotfuchs, Polarfuchs, Wüstenfuchs (Fennek), Silberfuchs | Riesenohren beim Fennek, schläft mit geschlossenen Augen, trägt Dinge im Maul, pflückt Beeren |
| **Bär** | Eisbär | Eisbär, Braunbär, Schwarzbär | neu in Taiga und Wäldern; Schulterbuckel beim Braunbären; richtet sich zum Angriff auf |
| **Panda** | Panda | ein Gesicht je Wesenszug | faul, besorgt, verspielt, schwach, aggressiv oder braun – man sieht es an den Augenflecken |
| **Hauskatze** | Katze | getigert, roter Kater, Siam, Kartäuser, Glückskatze, Perser, Smoking, Hexenkatze | eigenes Halsband; die schwarze Hexenkatze wohnt am Hexenbaum |
| **Wildkatze** | Ozelot | Ozelot, Luchs, Waldkatze | Luchs mit Ohrpinseln, Stummelschwanz und dichtem Fell in Taiga und Schnee, Waldkatze in den Wäldern |
| **Papagei** | Papagei | Hellroter Ara, Hyazinthara, Amazone, Gelbbrustara, Graupapagei | Krummschnabel, Federhaube; tanzt, ahmt Monster nach und sitzt auf der Schulter |
| **Biene** | Biene | Honigbiene, Hummel | pelzige Hummeln in kühlen Gegenden; Pollen an den Beinen, Zorn, Stich, Nester und Bienenstöcke |
| **Fledermaus** | Fledermaus | Abendsegler, Mausohr, Vampirfledermaus | Flughäute mit Fingerknochen; Vampire in Dschungel und Sumpf |
| **Gürteltier** | Gürteltier | Neunbinden-Gürteltier, Kugelgürteltier | Panzer mit Gürteln und Schilden, rollt sich zur Kugel ein, wirft Hornplatten ab |
| **Kamel** | Kamel | Dromedar, Trampeltier | Trampeltier mit zwei Höckern neu in Tafelbergen und auf den Savannen-Hochebenen; Sattel, zwei Reiter, Sprint |
| **Lama** | Lama, Händlerlama | creme, weiß, braun, grau | dichtes Vlies, Bananenohren; spuckt, zieht in Karawanen, trägt Teppiche und Packtaschen |

### Monster

![Die eigenen Monster](docs/bilder/monster.jpg)

Auch die Untoten und Nachtwesen der Vanilla-Welt sind ersetzt: Wo Vanilla nachts oder in Höhlen ein Monster erscheinen
lässt, kommt mit dem gleichen Gewicht ein **eigenes Gegenstück** – mit eigenem Modell und Bild, aber denselben Angriffen,
Schwächen und derselben Beute. Die Gestalt richtet sich nach dem Land: Mönche und Söldner unter den Wiedergängern,
Fischer in den Flüssen, Wüstenkriecher mit Kakteenstacheln, Schneespinnen mit weißem Pelz, Höhlenkriecher mit
Leuchtpilzen. Die Augen der Untoten glimmen in der Nacht. Spinnenreiter, Hühnerreiter, Mumien auf Dörrkamelen,
Skelettfallen im Gewitter, Hexen bei Überfällen und die Monster der Prüfkammern kommen ebenfalls als eigene Wesen;
Vanilla-Spawn-Eier und Befehle bringen die neuen Monster hervor.

| Monster | statt | Gestalten | Besonderheiten |
|---|---|---|---|
| **Wiedergänger** | Zombie | Bauer, Söldner, Mönch | zerrissener Kittel, rostiges Kettenhemd mit Eisenhut oder Kutte mit Kapuze; Kinder und Hühnerreiter; steckt Dorfbewohner an |
| **Mumie** | Wüstenzombie | Mumie, Wüstenleiche | Leinenbinden oder Turban; Schlag mit Hunger; reitet manchmal ein Dörrkamel |
| **Wasserleiche** | Ertrunkener | Matrose, Fischer | Seetang im Haar oder Strickmütze und Ölzeug; schwimmt, wirft Dreizacke; Fischer meist in Flüssen |
| **Untoter Dorfbewohner** | Zombie-Dorfbewohner | 17 Berufe mit je drei Gesichtern | verweste Tracht des Berufs; mit Trank der Schwäche und goldenem Apfel heilbar – er wird wieder genau der Dorfbewohner, der er war, mit Namen, Beruf, Dorf und Bett |
| **Gerippe** | Skelett | Knochen, Knochenritter, Knochenjäger | Rippen mit Lücken, Unterkiefer, Knöchel; Ritter mit rostigem Nasalhelm und zerfetztem Wappenrock, Jäger mit Kapuze und Köcher |
| **Frostgebein** | Eiswanderer | – | Pelzumhang und Raureif; Pfeile der Langsamkeit |
| **Moorgebein** | Sumpfskelett | – | Moos und Pilze (abscherbar); Giftpfeile |
| **Dörrgebein** | Parched | – | Wüstenbinden; Pfeile der Schwäche; reitet mit der Mumie auf dem Dörrkamel |
| **Kriecher** | Creeper | Wald, Wüste, Schnee, Höhle, Dschungel | Moos, Kakteenstacheln, Eiszapfen, Leuchtpilze oder Blüten; bei Blitz aufgeladen |
| **Spinne** | Spinne | Wald, Wüste, Dschungel, Schnee | gegliederte Beine, leuchtende Augen; trägt manchmal ein Gerippe |
| **Höhlenspinne** | Höhlenspinne | – | giftgrün gezeichnet und glimmend; giftig |
| **Schleim** | Schleim | Sumpfschleim, Höhlenschleim | durchscheinende Hülle mit Gesicht; teilt sich in kleinere Schleime gleicher Art |
| **Schwefelwürfel** | Sulfur Cube | – | durchscheinend mit Kern; verschluckt Blöcke und trägt sie sichtbar in sich |
| **Hexe** | Hexe | Moor-, Nacht-, Kräuterhexe | Spitzhut mit Krempe, Warze, Kräuterbeutel und leuchtende Fläschchen; wirft Tränke; wohnt am Hexenbaum |
| **Nachtmahr** | Phantom | dunkel, bleich | zerfranste Flughäute und glühende Augen; kommt zu dem, der lange nicht schläft |
| **Silberfischchen** | Silberfischchen | silbern, aus der Tiefe | wohnt in befallenen Steinen und ruft Artgenossen |
| **Zombiepferd** | Zombiepferd | grau, verwester Rappe | nachts auf den Ebenen, oft mit einem Wiedergänger samt Speer im Sattel; zähmbar und reitbar |
| **Skelettpferd** | Skelettpferd | bleich, uralt | Skelettfalle im Gewitter mit vier Reitern |
| **Dörrkamel** | Camel Husk | Dörrkamel, Mumienkamel | vertrocknet oder bandagiert, mit Satteldecke; trägt Mumie und Dörrgebein |

### Wilde Kreaturen

![Wildschweine, Hirsche, Gänse, Irrlicht, Höhlentroll und Lindwurm](docs/bilder/kreaturen.jpg)

| Kreatur | Lebensraum | Besonderheit | Beute |
|---|---|---|---|
| Wildschwein | Wälder, Taiga | greift an, wenn man es reizt | Schweinefleisch, Leder |
| Rothirsch | Wälder, Wiesen | scheu, Hirsche mit Geweih | Wildbret, Leder |
| Gans | Flüsse, Sümpfe, Ebenen | grau oder weiß, legt Federn, faucht | Federn, Geflügel |
| Irrlicht | Sümpfe, dunkle Wälder (nachts) | leuchtet, weicht aus, verblasst am Morgen | Irrlichtstaub |
| Höhlentroll | tief unter der Erde | riesig und stark, versteinert im Sonnenlicht | Leder, Eisenklumpen, selten Edelstein oder Trollkeule |
| Lindwurm | Berge, Tafelländer | flügelloser Drache, Biss und Schwanzhieb | Erddrachen-Schuppen, selten Draconium |

### Erze und Rohstoffe

| Erz | Fundort | Werkzeug | Verwendung |
|---|---|---|---|
| Kohlenstofferz | überall, Höhe 0 bis 192 | jede Spitzhacke | Brennstoff, **Stahl** |
| Edelstein-Erz | tief, Höhe −64 bis 16 | Eisenspitzhacke | Drachenkompass, Königskrone, Hauptleute |
| Konteriterz | Berge, um Höhe 150 | Eisenspitzhacke | leichte **Konteritrüstung** |
| Aetheriterz (leuchtet) | Höhe −48 bis 48 | Eisenspitzhacke | **Magie**: Aetherit-Stab, Runen, Aetheritlaterne |
| Draconiumerz (leuchtet) | ganz tief, sehr selten | Diamantspitzhacke | **Draconium-Waffen** (setzen in Brand), Drachenpanzer |
| Wyverniterz | hohe Berge, ab Höhe 100 | Eisenspitzhacke | **Wyvernit-Gleiter** |

**Stahl:** 1 Eisenbarren + 2 Kohlenstoff → Stahlmischung → im Hochofen → Stahlbarren (auch Kohlenstoff aus Holzkohle).
Aus 9 Rohstoffen wird jeweils ein Speicherblock.

### Ausrüstung

![Rüstungen aus Stahl, Konterit und Drachenschuppen](docs/bilder/ausruestung.jpg)

- **Stahl:** Werkzeuge, Schwert und Rüstung – stärker als Eisen.
- **Konteritrüstung:** leicht, je Teil +4 % Tempo und sanftere Stürze.
- **Draconiumschwert und -axt:** mit Lohenruten als Griff, setzen Gegner in Brand.
- **Drachenschuppen-Rüstung** (6 Arten) mit Set-Bonus: Feuerresistenz (Feuer), Tempo und Frostschutz (Eis),
  doppeltes Tempo (Blitz), Regeneration (Natur), Resistenz (Erde), Wasseratmung und Delfinsgunst (Wasser).
- **Wyvernit-Gleiter:** gleitet wie Elytren. **Königskrone:** Glück +2. **Trollkeule:** schwer, schleudert Gegner weg.

### Magie

![Zauberkreis, Magietisch und Zauber](docs/bilder/magie.jpg)

Der **Aetherit-Stab** (Aetherit, Irrlichtstaub, Stöcke) beherrscht von Anfang an den **Aether-Blitz**. Weitere Zauber
lernt er am **Magietisch** aus **Zaubertafeln**: Stab, Tafel und Aetherit einlegen, „Einschreiben“ – unter kreisenden
Runen schreibt sich der Zauber in den Stab. Jede weitere Tafel desselben Zaubers hebt ihn eine Stufe höher (bis III);
der Aether-Blitz wird mit Aetherit allein stärker.

| Zauber | Tafel | Stufe I | Stufe II | Stufe III |
|---|---|---|---|---|
| Aether-Blitz | – | Strahl reiner Magie | mehr Schaden, größere Reichweite | springt auf einen zweiten Feind über |
| Feuerball | Ignatius | setzt Feinde in Brand (nie Blöcke) | drei Feuerbälle im Fächer | zerbersten in einer Feuerwolke |
| Frosthauch | Isolde | verlangsamt, unterkühlt, friert Wasser | kälter und weiter | friert Feinde fest |
| Blitzschlag | Fulgor | Blitz an die anvisierte Stelle | springt auf zwei weitere Feinde | Kettenblitz: bis zu vier Feinde |
| Heilende Blüte | Sylvana | heilt dich und Verbündete | dazu ein Schild | Wurzeln packen Feinde |
| Erdstoß | Gorvan | schleudert Feinde fort | stärker und weiter | Felsdornen lassen taumeln |
| Woge | Marena | drängt zurück, löscht Feuer, Wasseratmung | heilt dich dabei | Wasserschild: weniger Schaden |

- **Einschreiben** kostet Erfahrung und Aetherit: Stufe I 3 Stufen + 1 Aetherit, II 6 + 2, III 9 + 3
  (Aether-Blitz: II 4 + 3, III 8 + 6). Der **Magietisch** entsteht aus Aetherit, Buch, Gold, Obsidian und poliertem
  Tiefenschiefer.
- **Zauberkreis:** Taste **V** halten, mit der Maus einen Zauber wählen (oder mit 1–7). Über der Schnellleiste zeigt
  ein Zeichen den gewählten Zauber und seine Abklingzeit; jeder Zauber kühlt für sich ab.
- **Zaubertafeln** lassen die Erzmagier fallen (je die Tafel ihres Elements); selten liegt eine in den Truhen ihrer
  Türme, in der Sturmwarte oder am Thron der Tiefe. Alte **Runen** aus früheren Versionen lehren wie eine Tafel.
- Zauber kosten Haltbarkeit; Reparatur mit Aetherit.

### Die Erzmagier und ihre Türme

![Die sechs Magiertürme](docs/bilder/magiertuerme.jpg)

Sechs Erzmagier hüten die Zaubertafeln der Elemente – jeder in seinem eigenen **Magierturm**, im Land seines Elements:

| Erzmagier | Turm | Land |
|---|---|---|
| **Ignatius**, Erzmagier der Flammen | schwarzer Basalt, ziegelrotes Dach, eine Flamme auf der Spitze | Wüste, Tafelberge, Aschefelder |
| **Isolde**, Erzmagierin des Frosts | Reifstein und Packeis, Schieferdach, Frostkristall | Schneeebene, Eisdornen, Schneetaiga, Hain |
| **Fulgor**, Erzmagier der Stürme | Tiefenschiefer und Kupfer, grünes Kupferdach, Blitzableiter | Bergwiese, windige Hügel, Sturmküste |
| **Sylvana**, Erzmagierin der Wildnis | bemoostes Mauerwerk, Holzschindeln, eine blühende Krone | Wälder, Dschungel, Kirschhain, Urhain |
| **Gorvan**, Erzmagier des Gesteins | Tuff und Lehmziegel, braune Ziegel, Bernstein | Savanne, Ebene, Taiga, Felsschluchten |
| **Marena**, Erzmagierin der Gezeiten | Prismarin und Perlmutt, dunkles Prismarindach | Strand, Sumpf, Mangrovensumpf, Korallenküste, Nebelmoor |

- Über eine Wendeltreppe an der Wand geht es durch **Eingangshalle**, **Bibliothek** (mit den Aufzeichnungen des
  Magiers auf dem Lesepult – darin Hinweise für den Kampf), **Laboratorium** (mit Magietisch und Braustand) und
  **Kammer** (mit Zaubertisch) hinauf ins **Sanktum**. In jedem Geschoss steht eine Truhe.
- Im Sanktum meditiert der Erzmagier über seinem Ritualkreis. Betrittst du den Saal, **erwacht** er: Er wirft
  Geschosse, die dir folgen (ein beherzter Schlag wirft sie zurück), wirkt den Zauber seines Elements, blinzelt
  durch den Saal, ruft **Spiegelbilder** (zerplatzen beim ersten Treffer) und hüllt sich in einen **Runenschild**
  (bricht nach vier Schlägen). Bei zwei und einem Drittel seiner Kraft folgt eine neue **Phase**, und mit ihr sein
  **großer Zauber**: Meteorregen, Blizzard, Gewittersturm, Wildwuchs, Felsbeben oder Mahlstrom – jeder Einschlag
  kündigt sich am Boden an.
- Gegen sein eigenes Element ist er gefeit. Wer ins Geschoss darunter flieht, entkommt ihm – dann kehrt er geheilt an
  seinen Platz zurück.
- Auch mitten im Getümmel bleibt die Sicht frei: Einschläge werfen ihre Funken, Splitter und Wolken nicht direkt vor
  die Augen dessen, den sie treffen, und Schwächungen durch Bosse (wie auch die Wirkungen eigener Zauber) zeigen sich
  als Zeichen in der Anzeige statt als Wirbel vor den Augen – das gilt ebenso im Kampf gegen die Drachen.
- Besiegt lässt er seine **Zaubertafel**, Goldmünzen, Aetherit und ein verzaubertes Buch fallen; die Truhe im Sanktum
  hält weiteren Schatz bereit.

![Kampf gegen die Erzmagier](docs/bilder/erzmagier.jpg)

### Bauen und Einrichten

![Eine Halle aus den neuen Bau-Blöcken](docs/bilder/burg.jpg)

| Baustoff | Herstellung | Varianten |
|---|---|---|
| Mittelalterlicher Stein, Grober Naturstein, Behauener Sandstein, Dunkle Steinziegel, Helle Kalksteinziegel, Verwitterte Steinziegel, Moosige Mauersteine, Gerissene Steinblöcke | Steinmetz aus Vanilla-Gestein | Treppe, Stufe, Mauer |
| Rote, braune und helle Dachziegel, Dunkle Schieferplatten, Holzschindeln, Kupferdach | Steinmetz bzw. Werkbank | Treppe, Stufe |
| Steinpfeiler, Holzbalken, Fachwerk (gerade, diagonal, Kreuz) | Werkbank / Steinmetz | – |
| Kalkputz (Sand, Ton, Knochenmehl) und Lehmputz (Ton, Erde, Weizen) | Werkbank (ergibt 4) | Treppe, Stufe |
| Bleiglas (8 Glas + Eisenklumpen) und Bleiglasscheiben | Werkbank | – |
| Burgtür (Bretter mit einem Eisenband), grüne und rote Fensterläden mit Herz | Werkbank | – |
| Stahllaterne, Aetheritlaterne, Kronleuchter mit acht Kerzen | Werkbank | – |
| Zinnen aus mittelalterlichem Stein, dunklen Steinziegeln oder hellen Kalksteinziegeln | Werkbank / Steinmetz | – |
| Eichenstuhl, Eichenbank, Eichentisch (verbindet sich zur Tafel), Thron | Werkbank | – |

Auf **Stühle, Bänke und den Thron** setzt man sich per Rechtsklick. **Zinnen** in einer Reihe ergeben von selbst
Zahn und Scharte. Der **Kronleuchter** hängt an der Decke.

### Landwirtschaft und Nahrung

![Felder und Festmahle auf der Tafel](docs/bilder/landwirtschaft.jpg)

- **Feldfrüchte:** Tomaten, Kohl, Gerste, Zwiebeln, **Hopfen**, **Weintrauben** und **Knoblauch** – Samen und
  Pflanzknollen fallen manchmal beim Abbauen von Gras, Bauern-Dorfbewohner bauen sie an.
- **Gerichte:** Brötchen, Gemüseeintopf, Fleischpastete, Apfelkuchen, Knoblauchbrot, Wildbret.
- **Getränke** (die Flasche bleibt übrig): **Bier** (Gerste, Hopfen) macht stark, **Rotwein** (Trauben) heilt langsam,
  **Met** (Honigwaben, Hopfen) schenkt Extraherzen – wer zu viel trinkt, dem wird schwindelig.
- **Festmahl:** ein Braten mit Beilagen auf einem Brett für die Tafel – vier üppige Portionen, direkt vom Tisch.
- **Zierblumen** (wachsen wild, passen in Blumentöpfe): Lavendel, Gartenrose, Feuerlilie, Frostblume und die
  **Mondblume**, die nachts leuchtet.

### Bauwerke in der Welt

- **Der Siegelkreis** – einmal pro Welt, auf ebenem Land unweit des Weltursprungs (siehe „Die Geschichte“).
- **Die sechs Drachennester** – je eins pro Welt, mit eigenem Biom (siehe „Die Nester“). In ihren Schuttfeldern
  liegen Fundstücke, die man mit dem Pinsel freilegt.
- **Die sechs Magiertürme** – im Land ihres Elements (siehe „Die Erzmagier und ihre Türme“), mit
  `/locate structure kingdomsatwar:fire_mage_tower` (bzw. `ice_`, `lightning_`, `nature_`, `earth_`, `water_`) zu finden.
- **Die Dörfer** in vier Bauweisen, je nach Land (siehe „Dörfer und Dorfbewohner“), mit
  `/locate structure kingdomsatwar:village_timber` (bzw. `village_nordic`, `village_clay`, `village_stilt`) zu finden.
- **Die 17 Bauwerke der Oberwelt** an Stelle der Vanilla-Bauwerke (siehe „Bauwerke der Oberwelt“) – vom
  Sternensturz mit dem Portal ins Ende bis zur Raubritterburg im dunklen Wald.
- **Burgruinen** in Ebenen, Wäldern und der Taiga: verfallene Mauern mit Ecktürmen und Zinnen, eine Truhe mit Beute
  (Vorräte, Stahl, Edelsteine, mit Glück Runen, der Aetherit-Stab, die Königskrone oder ein Drachenkompass) – und
  manchmal Banditen.

---

## Befehle, Tasten, Einstellungen

| | |
|---|---|
| `/kaw intro` | das Intro noch einmal ansehen |
| `/kaw army <swordsman\|archer\|knight\|captain> [Anzahl]` | (nur Operatoren) Soldaten sofort in den Dienst stellen, z. B. `/kaw army knight 4` |
| `/kaw lair` · `/kaw lair awaken` · `/kaw lair reset` | (nur Operatoren) Lage aller Nester anzeigen, den Drachen im Nest wecken, ein Nest zurücksetzen |
| `/kaw citadel` · `/kaw citadel tp` · `/kaw boss nyxar` | (nur Operatoren) Lage der Schattenzitadelle anzeigen, an den Fuß ihrer Freitreppe reisen, Nyxar vor sich erwecken |
| `/kaw story reset\|card <Kapitel>\|chronicle` | (nur Operatoren) Geschichte zurücksetzen, eine Titelkarte zeigen, die Chronik geben |
| `/kaw seal` · `/kaw seal shard\|insert <Element>` · `/kaw seal break\|reset` | (nur Operatoren) Zustand des Siegels, Splitter geben oder einsetzen, das Siegel brechen oder zurücksetzen |
| **R** | Drachenodem beim Reiten (änderbar unter Optionen → Steuerung) |
| **V** (halten) | Zauberkreis: Zauber des Aetherit-Stabs wählen (änderbar unter Optionen → Steuerung) |
| `showIntro` (Client) | Intro beim ersten Betreten einer Welt zeigen (Standard: an) |
| `armyLimit` (Server) | höchstens so viele Soldaten je Spieler (Standard: 32, je Kaserne 8 mehr) |
| `tradesBeforePause` (Server) | so oft kann man mit einem Bürger oder Dorfbewohner handeln, bevor sein Laden Pause macht (Standard: 16) |
| `tradePauseMinutes` (Server) | so lange dauert die Pause in echten Minuten (Standard: 180) |

---

## Für Entwickler

Das Projekt ist ein normales NeoForge-Projekt mit [ModDevGradle](https://github.com/neoforged/ModDevGradle).

| Befehl | Wirkung |
|---|---|
| `./gradlew build` | Mod bauen → `build/libs/kingdomsatwar-0.12.0.jar` |
| `./gradlew runClient` | Minecraft mit der Mod zum Testen starten |
| `./gradlew runData` | Modelle, Rezepte, Loot-Tables, Tags, Weltgenerierung und Übersetzungen neu erzeugen |
| `./gradlew runGameTestServer` | Automatische Tests in einem Minecraft-Server ausführen |
| `python3 tools/generate_textures.py` | Alle Texturen neu zeichnen (`--preview datei.png` für eine Übersicht) |
| `python3 tools/generate_entities.py` | Modelle und Texturen der Lebewesen neu erzeugen |
| `python3 tools/generate_citizens.py` | Texturen der Bürger neu zeichnen (`--preview` für eine Übersicht) |
| `python3 tools/generate_villagers.py` | Texturen der Dorfbewohner neu zeichnen (`--preview` für eine Übersicht) |
| `python3 tools/generate_undead_villagers.py` | Texturen der untoten Dorfbewohner aus denen der Dorfbewohner ableiten (`--preview`) |
| `python3 tools/audio/build_audio.py` | Musik, Intro-Zeitplan und Stimmen neu bauen (siehe `tools/audio/README.md`) |

Unter Windows statt `./gradlew` einfach `gradlew.bat` verwenden. Java 25 wird benötigt (lädt Gradle bei Bedarf selbst).

**Aufbau**

```
src/main/java/io/github/sirschrolli/kingdomsatwar/
├── KingdomsAtWar.java        Hauptklasse der Mod
├── registry/                 Blöcke, Gegenstände, Lebewesen, Geräusche, Kreativ-Tabs, Tags
├── block/                    eigene Blöcke (Drachenei, Möbel, Zinne, Kronleuchter, Festmahl …)
├── entity/                   Drachen, Drachen-Bosse (entity/boss), Soldaten, Banditen, Kreaturen, Dorfbewohner (entity/villager), Sitz,
│                             die Hof- und Weidetiere an Stelle der Vanilla-Tiere (entity/creature/farm),
│                             die Wildtiere an Stelle der Vanilla-Wildtiere (entity/creature/wild),
│                             die Monster an Stelle der Vanilla-Monster (entity/creature/monster)
├── dragon/  army/  magic/    Drachenarten, Rekrutierung und Befehle, Zauber
├── economy/                  Münzen, Ansehen, Siedlung und Baupläne (economy/town), Bürger und Handel (economy/citizen)
├── story/  world/            Chronik, Kapitel, Siegel und Schattentor
├── item/                     besondere Ausrüstung
├── worldgen/                 Erzadern, Blumen, Burgruinen, Nest-Biome, Nester (worldgen/lair), Siegelkreis (worldgen/seal),
│                             die acht Oberwelt-Biome mit Biomquelle, Böden und Features (worldgen/overworld),
│                             die Dörfer mit Planer, Wegen, Plätzen und Gebäuden (worldgen/village),
│                             die 17 Bauwerke der Oberwelt an Stelle der Vanilla-Bauwerke (worldgen/site)
├── client/                   Modelle, Renderer, Intro, Oberflächen
├── datagen/                  Datengenerierung (erzeugt src/generated/resources)
└── gametest/                 automatische Tests
src/main/resources/           Texturen, Geräusche, Musik, Intro
src/generated/resources/      von runData erzeugte Dateien (nicht von Hand bearbeiten)
tools/                        Skripte für Texturen, Modelle, Audio und Teststruktur
docs/                         Konzept und Bilder
```

Bei jedem Push baut GitHub Actions die Mod, führt die Datengenerierung und die GameTests aus
und stellt die JAR als Artefakt bereit (`.github/workflows/build.yml`).
Ein Tag wie `v0.12.0` erzeugt automatisch ein Release mit der JAR (`.github/workflows/release.yml`).

---

## Nächste Schritte

Der ganze Plan steht in [docs/DAS_SIEGEL.md](docs/DAS_SIEGEL.md):

1. **Eigene Welt** – die acht Oberwelt-Biome, die Dörfer, die 17 Bauwerke der Oberwelt, die Hof- und Weidetiere, die
   Wildtiere und die Untoten und Nachtwesen sind fertig, als Nächstes:
   - die übrigen **Monster** – Illager samt Überfällen, Plagegeister, Verwüster, Wächter, Brise, Knarz und Wärter;
   - **Wassertiere und Golems** – Fische, Tintenfische, Delfine, Schildkröten, Axolotl, Frösche, Eisen- und
     Schneegolem, Hilfsgeist und Schnüffler; alles, was sie fallen lassen, bleibt erhältlich;
   - zuletzt **Nether und End**.
2. Danach Dragon Architect, Belagerungswaffen, Kriegssystem und die Veröffentlichung auf Modrinth.

## Danksagung

- **Erzählerstimme:** erzeugt mit dem freien Sprachmodell [Qwen3-TTS](https://github.com/QwenLM/Qwen3-TTS)
  (Apache-2.0) – eine eigens beschriebene, künstliche Stimme, keine echte Person.
- **Soldatenstimmen:** erzeugt mit der freien Sprachsynthese [Piper](https://github.com/rhasspy/piper).
  Deutsche Stimmen: [Thorsten-Voice](https://github.com/thorstenMueller/Thorsten-Voice) von Thorsten Müller (CC0).
  Englische Stimmen: Piper-Stimme „libritts_r“, trainiert auf [LibriTTS-R](https://www.openslr.org/141/)
  (CC BY 4.0, Y. Koizumi u. a.; Sprecher 680, 248 und 788).
- **Musik:** eigene Komposition, gespielt mit dem Soundfont FluidR3_GM von Frank Wen (MIT-Lizenz).
- **Geräusche der Lebewesen:** nutzen die Original-Geräusche von Minecraft mit veränderter Tonhöhe;
  es werden keine Minecraft-Dateien mitgeliefert (siehe `tools/sound_events.py`).

## Lizenz

Alle Rechte vorbehalten (siehe `mod_license` in `gradle.properties` – kann vor einer Veröffentlichung geändert werden).
Die Texturen sind eigene Pixelkunst; es werden keine Original-Minecraft-Dateien mitgeliefert.
