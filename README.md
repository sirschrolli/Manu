# Kingdoms at War – Dragon Warfare

![Kingdoms at War](src/main/resources/kingdomsatwar_banner.png)

Eine eigene, mittelalterliche Minecraft-Mod für **Minecraft Java 26.3** mit **NeoForge**.
Vor langer Zeit verschlossen sechs Elementardrachen die **Schattenwelt** – jeder trägt ein Sechstel des Siegels in
seinem Herzen. Gründe dein Königreich, wirb Ritter und Soldaten an, baue deine Burg, spüre die riesigen Nester der
Drachen auf und bezwinge sie. Aus jedem besiegten Drachen bleibt ein Ei, aus dem dein eigener Drache schlüpft – und
ein Splitter des Siegels. Der sechste Splitter bricht das Siegel …

> **Testversion 0.4.1 – Der Schattendrache.**
> Die Geschichte mit Chronik, Kapiteln und Siegelkreis, **sechs riesige Drachen-Bosse** mit eigenem Körperbau,
> eigenen Angriffen und je einem gewaltigen Nest in einem eigenen Biom – dazu alles aus Phase 2: Drachen zum Reiten,
> Soldaten, Kreaturen, Ausrüstung, Magie, Bau-Blöcke, Essen und das erzählte Intro.
> Neu in 0.4: **Nyxar, der Schattendrache** – der Endgegner in seiner **Schattenzitadelle** hinter dem Siegel – und
> in 0.4.1 die übrigen **Bauwerke der Schattenwelt**: Mondtempel mit Gruft, Kristallminen, Mondleuchttürme an der
> Küste, Ruinen der alten Welt und Felsbrücken über den Schluchten.
> Alles läuft auf einem echten Minecraft-26.3-Server und besteht die automatischen Tests (54 GameTests).
> **Als Nächstes:** Wirtschaft und Stadt, dann Magie und eigene Biome.

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
| Magie: Aetherit-Stab mit sechs Runen | ✅ fertig |
| Bauen: Steine, Dächer, Fachwerk, Putz, Bleiglas, Burgtür, Fensterläden, Laternen, Kronleuchter, Zinnen, Möbel | ✅ fertig |
| Landwirtschaft: sieben Feldfrüchte, Gerichte, Getränke, Festmahl, Zierblumen | ✅ fertig |
| Deutsch und Englisch | ✅ fertig |
| Schattenwelt: eigene Dimension mit Biomen, Schattenvolk und Dörfern, Erzen, Kreaturen; Nyxar und seine Schattenzitadelle | ✅ fertig |
| Bauwerke der Schattenwelt: Mondtempel mit Gruft, Kristallminen, Mondleuchttürme, Ruinen der alten Welt, Felsbrücken | ✅ fertig |
| Wirtschaft mit Münzen, wachsende Stadt, Ansehen, Krönung | ⏳ geplant |
| Magier-Bosse, Magietisch, Zauber-Radialmenü | ⏳ geplant |
| Eigene Biome, Dörfer und Tiere statt Vanilla (auch Nether und End) | ⏳ geplant |
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
3. Unten bei **Artifacts** die Datei **kingdomsatwar-0.4.1.jar** herunterladen
   (dafür musst du bei GitHub angemeldet sein).

Sobald es ein Release gibt, liegt die JAR auch dauerhaft unter **Releases**.

**Installieren**

1. NeoForge für 26.3 installieren und das NeoForge-Profil einmal im Launcher starten.
2. Die Datei `kingdomsatwar-0.4.1.jar` in den Ordner `mods` kopieren
   (Windows: `%appdata%\.minecraft\mods`).
3. Minecraft mit dem NeoForge-Profil starten und eine **neue** Welt anlegen: Erze, Kreaturen, Blumen, der
   Siegelkreis, die Drachennester und Burgruinen entstehen nur in neu erzeugten Gebieten.

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
| I Ein neues Königreich | den ersten Soldaten anwerben |
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

### Die Nester

![Die sechs Nester](docs/bilder/nester.jpg)

Jedes Nest ist ein riesiges Bauwerk, das es **genau einmal pro Welt** gibt – immer im passenden Biom. Um jedes Nest
liegt ein eigenes Biom von gut 200 Blöcken Durchmesser, das die Landschaft mit neuen Blöcken umgestaltet und eigene
Farben, Partikel und Geräusche hat.

| Nest | Wo | Biom ringsum |
|---|---|---|
| Pyrrhons **Vulkankrater** | Badlands, Wüste | **Aschelande:** Aschedünen, Aschenerde, verkohlte Bäume, Vulkangestein mit Glutrissen, rauchende Glutspalten, fallende Asche |
| Glaciaras **Gletscherkessel** | Eiszapfen-Ebene, vereiste Gipfel | **Frostkessel:** Gletschereis, Raureifstein, Frosterde, Frostkristalle, Eiszapfen, Schneetreiben |
| Voltarions **Sturmgipfel** | zerklüftete und steinige Gipfel | **Sturmklippen:** Sturmstein, geladener Sturmstein, Fulgurit, Sturmkristalle, Kupferadern, verfallene Wachtürme |
| Verdantis' Nest im **Uralten Hain** | Dschungel, dunkler Wald | **Uralter Hain:** Riesenbäume aus Urholz, Leuchtmoos, Leuchtkappen, Dornengestrüpp, knorrige Wurzeln |
| Terragons **Felsschlucht** | windgepeitschte Hügel, Savannen-Hochebene | **Zerklüftete Weiten:** Felsnadeln, Schiefer, Geröll, Spaltgestein, Bernsteindrusen |
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

Am **Rekrutierungstisch** (2 Smaragde, Buch, 3 Bretter, 2 Stöcke) wirbst du Soldaten an:

| Soldat | Preis | Rolle |
|---|---|---|
| Schwertkämpfer | 5 Smaragde | Nahkämpfer – das Rückgrat jeder Armee |
| Bogenschütze | 7 Smaragde | Fernkämpfer, schießt nie auf Freunde |
| Ritter | 12 Smaragde + 2 Stahlbarren | Plattenrüstung und Schild, hält die vorderste Reihe |
| Hauptmann | 20 Smaragde + 1 Edelstein | macht Soldaten in seiner Nähe stärker |

Mit dem **Kommandostab** gibst du Befehle: auf einen Feind zeigen = Angriff, auf den Boden zeigen = Stellung halten
(Schleichen: Patrouille), in den Himmel zeigen = alle folgen, Schleichen + eigenen Soldaten anklicken = entlassen.
Soldaten nehmen Waffen und Rüstung an, die du ihnen gibst, und verletzen weder dich noch deine Tiere und Drachen.
**Banditen** streifen nachts durch die Lande und hausen manchmal in Burgruinen.

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

![Der Aetherit-Stab mit der Blitzrune](docs/bilder/magie.jpg)

Der **Aetherit-Stab** (Aetherit, Irrlichtstaub, Stöcke) wirkt per Rechtsklick einen **Aether-Blitz**.
Hältst du eine **Rune** in der anderen Hand, wirkt er stattdessen deren Zauber:

| Rune | Zutat (+ Tiefenschiefer, Aetherit, Irrlichtstaub) | Zauber |
|---|---|---|
| Feuerrune | Lohenstaub | Feuerball (verbrennt Wesen, aber keine Blöcke) |
| Eisrune | Packeis | Frosthauch: verlangsamt, lässt erstarren, friert Wasser |
| Blitzrune | Kupferbarren | ruft einen Blitz herab |
| Naturrune | Moosblock | heilt dich und deine Verbündeten, Pflanzen sprießen |
| Erdrune | Roheisen | Erdstoß: schleudert Feinde fort |
| Wasserrune | Prismarinscherbe | Woge: drängt zurück, löscht Feuer, Wasseratmung |

Zauber kosten Haltbarkeit und brauchen eine kurze Pause. Reparatur mit Aetherit; Haltbarkeit und Reparatur lassen sich
anverzaubern.

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
| `showIntro` (Client) | Intro beim ersten Betreten einer Welt zeigen (Standard: an) |
| `armyLimit` (Server) | höchstens so viele Soldaten je Spieler (Standard: 32) |

---

## Für Entwickler

Das Projekt ist ein normales NeoForge-Projekt mit [ModDevGradle](https://github.com/neoforged/ModDevGradle).

| Befehl | Wirkung |
|---|---|
| `./gradlew build` | Mod bauen → `build/libs/kingdomsatwar-0.4.1.jar` |
| `./gradlew runClient` | Minecraft mit der Mod zum Testen starten |
| `./gradlew runData` | Modelle, Rezepte, Loot-Tables, Tags, Weltgenerierung und Übersetzungen neu erzeugen |
| `./gradlew runGameTestServer` | Automatische Tests in einem Minecraft-Server ausführen |
| `python3 tools/generate_textures.py` | Alle Texturen neu zeichnen (`--preview datei.png` für eine Übersicht) |
| `python3 tools/generate_entities.py` | Modelle und Texturen der Lebewesen neu erzeugen |
| `python3 tools/audio/build_audio.py` | Musik, Intro-Zeitplan und Stimmen neu bauen (siehe `tools/audio/README.md`) |

Unter Windows statt `./gradlew` einfach `gradlew.bat` verwenden. Java 25 wird benötigt (lädt Gradle bei Bedarf selbst).

**Aufbau**

```
src/main/java/io/github/sirschrolli/kingdomsatwar/
├── KingdomsAtWar.java        Hauptklasse der Mod
├── registry/                 Blöcke, Gegenstände, Lebewesen, Geräusche, Kreativ-Tabs, Tags
├── block/                    eigene Blöcke (Drachenei, Möbel, Zinne, Kronleuchter, Festmahl …)
├── entity/                   Drachen, Drachen-Bosse (entity/boss), Soldaten, Banditen, Kreaturen, Sitz
├── dragon/  army/  magic/    Drachenarten, Rekrutierung und Befehle, Zauber
├── story/  world/            Chronik, Kapitel, Siegel und Schattentor
├── item/                     besondere Ausrüstung
├── worldgen/                 Erzadern, Blumen, Burgruinen, Nest-Biome, Nester (worldgen/lair), Siegelkreis (worldgen/seal)
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
Ein Tag wie `v0.4.1` erzeugt automatisch ein Release mit der JAR (`.github/workflows/release.yml`).

---

## Nächste Schritte

Der ganze Plan steht in [docs/DAS_SIEGEL.md](docs/DAS_SIEGEL.md):

1. **Die Schattenwelt** hinter dem Siegel: eigene Dimension mit sieben Biomen, schwebenden Inseln und tiefen
   Abgründen, das Schattenvolk der Umbrer mit eigenen Dörfern, neue Erze und Ausrüstung, Kreaturen, Bauwerke und
   **Nyxar, der Schattendrache**.
2. **Wirtschaft und Stadt:** Münzen, Berufe, eine wachsende Stadt, Ansehen, Handel und die Krönung.
3. **Magie:** Magier-Bosse, Magietisch und ein Zauber-Radialmenü.
4. **Eigene Welt:** eigene Biome, Dörfer und Tiere statt der Vanilla-Inhalte, auch im Nether und End.
5. Danach Dragon Architect, Belagerungswaffen, Kriegssystem und die Veröffentlichung auf Modrinth.

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
