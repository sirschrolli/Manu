# Kingdoms at War – Dragon Warfare

![Kingdoms at War](src/main/resources/kingdomsatwar_banner.png)

Eine eigene, mittelalterliche Minecraft-Mod für **Minecraft Java 26.3** mit **NeoForge**.
Baue dein Königreich, sammle Drachenerze, bereite dich auf die Drachen vor – und später auf Armeen,
Belagerungen und NPC-Baumeister.

> **Testversion 0.1.0 – Phase 1 („Technische Grundlage“).**
> Das Grundprojekt kompiliert, startet in einem echten Minecraft-26.3-Server und besteht alle automatischen Tests.
> Drachen als Lebewesen, NPCs, Armeen und das Bausystem folgen in den nächsten Phasen.

Das vollständige Konzept steht in [docs/KONZEPT.md](docs/KONZEPT.md) (mit allen Konzeptbildern).

---

## Entwicklungsstand

| Bereich | Stand |
|---|---|
| NeoForge-Projekt für Minecraft 26.3, Java 25, Gradle | ✅ fertig |
| Automatischer Build + Tests auf GitHub | ✅ fertig |
| Neue Erze mit Weltgenerierung, Rohstoffe, Stahl | ✅ fertig |
| Sechs Dracheneier, Drachenschuppen, Drachenei-Fragmente, Drachenfutter | ✅ fertig (Ausbrüten folgt in Phase 4) |
| Mittelalterliche Baublöcke (Stein, Dächer, Fachwerk) mit Treppen, Stufen, Mauern | ✅ erster Teil |
| Feldfrüchte, Gerichte, Zierblumen (wachsen auch wild) | ✅ erster Teil |
| Deutsch und Englisch | ✅ fertig |
| NPCs, Soldaten, Rekrutierung mit Smaragden | ⏳ Phase 2 |
| Dragon Architect (Gebäude scannen, NPCs bauen lassen, .litematic/.schem/.nbt) | ⏳ Phase 3 |
| Drachen (Ausbrüten, 5 Wachstumsstufen, Zähmen, Reiten, Fliegen) | ⏳ Phase 4 |
| Belagerungswaffen, Fahrzeuge, weitere Mobs, Modrinth-Veröffentlichung | ⏳ Phase 5 |

---

## Herunterladen und spielen

**Voraussetzungen**

- Minecraft Java Edition **26.3**
- **NeoForge 26.3.0.98** oder neuer ([NeoForge-Installer](https://neoforged.net/))
- Java 25 – bringt der Minecraft-Launcher automatisch mit

**Die Test-JAR bekommen**

1. Auf GitHub oben auf **Actions** klicken.
2. Den neuesten erfolgreichen Lauf von **Build** öffnen (grüner Haken).
3. Unten bei **Artifacts** die Datei **kingdomsatwar-jar** herunterladen und entpacken.

Sobald es ein Release gibt, liegt die JAR auch dauerhaft unter **Releases**.

**Installieren**

1. NeoForge für 26.3 installieren und das NeoForge-Profil einmal im Launcher starten.
2. Die Datei `kingdomsatwar-0.1.0.jar` in den Ordner `mods` kopieren
   (Windows: `%appdata%\.minecraft\mods`).
3. Minecraft mit dem NeoForge-Profil starten. Im Kreativmodus gibt es vier neue Tabs.

> Tipp: Für eine neue Welt lohnt sich ein Test in einer **neuen** Welt, damit die neuen Erze und Blumen generiert werden.

---

## Inhalte der Testversion

### Dragon Warfare – Erze und Rohstoffe

| Erz | Fundort | Werkzeug | Ergibt | Verwendung |
|---|---|---|---|---|
| Kohlenstofferz | überall, Höhe 0 bis 192 | jede Spitzhacke | Kohlenstoff | Brennstoff, Fackeln, Stahl |
| Edelstein-Erz | tief, Höhe −64 bis 16 | Eisenspitzhacke | Edelstein | Drachenei-Fragmente, Deko |
| Konteriterz | Berge, Höhe 32 bis 256 | Eisenspitzhacke | Konterit | Drachenei-Fragmente, später Flugausrüstung |
| Aetheriterz (leuchtet leicht) | Höhe −48 bis 48 | Eisenspitzhacke | Aetherit | Drachenei-Fragmente, später Magie |
| Draconiumerz (leuchtet) | ganz tief, Höhe −64 bis −16, sehr selten | Diamantspitzhacke | Draconium | Drachenei-Fragmente, später Drachenrüstung |
| Wyverniterz | hohe Berge, ab Höhe 100 | Eisenspitzhacke | Wyvernit | später Flugtechnik |

Alle Erze gibt es auch als Tiefenschiefer-Variante (außer Wyvernit). Aus 9 Rohstoffen wird jeweils ein Speicherblock.
Glück (Fortune) erhöht die Ausbeute, Behutsamkeit (Silk Touch) liefert den Erzblock.

**Stahl:** 1 Eisenbarren + 2 Kohlenstoff → Stahlmischung → im Hochofen → Stahlbarren. 9 Stahlbarren → Stahlblock.
Kohlenstoff gibt es auch aus Holzkohle (Hochofen).

### Dragon Warfare – Drachen

| Drachenart | Ei-Rezept: 4 Drachenei-Fragmente im Kreuz um … |
|---|---|
| Feuerdrache | Magmablock |
| Eisdrache | Blaueis |
| Blitzdrache | Blitzableiter |
| Naturdrache | Moosblock |
| Erddrache | Roheisenblock |
| Wasserdrache | Prismarin |

- **Drachenei-Fragment:** Draconium + Aetherit + Konterit + Edelstein (formlos).
- **Drachenschuppen** (6 Arten) und **Drachenfutter** sind schon da und werden mit den Drachen in Phase 4 wichtig.
- Die Eier lassen sich setzen und abbauen. Das Ausbrüten kommt mit dem Drachensystem.

### Kingdoms at War – Burgen und Bauen

| Baustoff | Herstellung | Varianten |
|---|---|---|
| Mittelalterlicher Stein | Steinmetz aus Stein oder Bruchstein | Treppe, Stufe, Mauer |
| Grober Naturstein | Steinmetz aus Bruchstein, oder 2 Bruchstein + 2 Kies | Treppe, Stufe, Mauer |
| Behauener Sandstein | Steinmetz aus Sandstein | Treppe, Stufe, Mauer |
| Dunkle Steinziegel | Steinmetz aus Tiefenschiefer | Treppe, Stufe, Mauer |
| Helle Kalksteinziegel | Steinmetz aus Kalzit | Treppe, Stufe, Mauer |
| Verwitterte Steinziegel | Steinmetz aus Steinziegeln oder Stein | Treppe, Stufe, Mauer |
| Moosige Mauersteine | Steinmetz aus bemoosten Steinziegeln oder bemoostem Bruchstein | Treppe, Stufe, Mauer |
| Gerissene Steinblöcke | Steinmetz aus rissigen Steinziegeln oder Stein | Treppe, Stufe, Mauer |
| Steinpfeiler | 2 mittelalterliche Steine übereinander oder Steinmetz | – |
| Rote / Braune / Helle Dachziegel | Steinmetz aus gefärbter Keramik (rot auch aus Ziegeln) | Treppe, Stufe |
| Dunkle Schieferplatten | Steinmetz aus Tiefenschieferfliesen | Treppe, Stufe |
| Holzschindeln | 4 Holzstufen im Quadrat | Treppe, Stufe |
| Kupferdach | Steinmetz aus Kupferblock (4 Stück) | Treppe, Stufe |
| Holzbalken | 2 Stämme übereinander | – |
| Fachwerk (gerade, diagonal, Kreuz) | Holzbalken + Kalzit; Muster durch erneutes Herstellen wechseln | – |

Treppen, Stufen und Mauern gehen auch an der Werkbank mit den üblichen Vanilla-Mustern.

### Kingdoms at War – Landwirtschaft und Nahrung

- **Tomaten, Kohl, Gerste, Zwiebeln:** Samen fallen manchmal beim Abbauen von Gras.
  Bauern-Dorfbewohner pflanzen die neuen Samen ebenfalls an. Aus Tomaten und Kohl lassen sich wieder Samen herstellen.
- **Brötchen:** 2 Gerste. **Gemüseeintopf:** Tomate, Kohl, Zwiebel, Karotte, Schüssel.
  **Fleischpastete:** gebratenes Rindfleisch, Zwiebel, Weizen, Ei. **Apfelkuchen:** 2 Äpfel, Zucker, Ei, Weizen.
- **Zierblumen** (wachsen wild, passen in Blumentöpfe):
  Lavendel (Ebenen), Gartenrose (Wälder), Feuerlilie (Savannen), Frostblume (verschneite Gebiete).

---

## Für Entwickler

Das Projekt ist ein normales NeoForge-Projekt mit [ModDevGradle](https://github.com/neoforged/ModDevGradle).

| Befehl | Wirkung |
|---|---|
| `./gradlew build` | Mod bauen → `build/libs/kingdomsatwar-0.1.0.jar` |
| `./gradlew runClient` | Minecraft mit der Mod zum Testen starten |
| `./gradlew runData` | Modelle, Rezepte, Loot-Tables, Tags, Weltgenerierung und Übersetzungen neu erzeugen |
| `./gradlew runGameTestServer` | Automatische Tests in einem Minecraft-Server ausführen |
| `python3 tools/generate_textures.py` | Alle Texturen neu zeichnen |

Unter Windows statt `./gradlew` einfach `gradlew.bat` verwenden. Java 25 wird benötigt (lädt Gradle bei Bedarf selbst).

**Aufbau**

```
src/main/java/io/github/sirschrolli/kingdomsatwar/
├── KingdomsAtWar.java        Hauptklasse der Mod
├── registry/                 Blöcke, Gegenstände, Kreativ-Tabs, Tags
├── block/                    eigene Blockklassen (Drachenei, Feldfrucht)
├── dragon/                   die sechs Drachenarten
├── worldgen/                 Erzadern und wilde Blumen
├── loot/                     eigene Loot-Tables
├── datagen/                  Datengenerierung (erzeugt src/generated/resources)
└── gametest/                 automatische Tests
src/main/resources/           Texturen, Logo, Teststruktur
src/generated/resources/      von runData erzeugte Dateien (nicht von Hand bearbeiten)
tools/                        Skripte für Texturen und Teststruktur
docs/                         Konzept und Konzeptbilder
```

Bei jedem Push baut GitHub Actions die Mod, führt die Datengenerierung und die GameTests aus
und stellt die JAR als Artefakt bereit (`.github/workflows/build.yml`).
Ein Tag wie `v0.1.0` erzeugt automatisch ein Release mit der JAR (`.github/workflows/release.yml`).

---

## Nächste Schritte (aus dem Konzept)

1. **Phase 2 – NPCs und Gebäude:** erste NPCs und Soldaten, einfache KI, Rekrutierung mit Smaragden, erste Gebäude und Befehle (`/kaw`).
2. **Phase 3 – Dragon Architect:** Bereiche markieren, Bauvorlagen speichern, NPC-Bauarbeiter (Block für Block oder abschnittsweise), Import von `.nbt`, `.schem` und `.litematic`.
3. **Phase 4 – Drachen:** Ausbrüten, fünf Wachstumsstufen, Zähmen, Reiten, Fliegen, Drachen-KI; weitere Mobs, Pflanzen und Gerichte.
4. **Phase 5 – Gesamtsystem:** Kriegssystem mit Belagerungswaffen und Fahrzeugen, alles verbinden, Modpack zusammenstellen, Modrinth.

## Lizenz

Alle Rechte vorbehalten (siehe `mod_license` in `gradle.properties` – kann vor einer Veröffentlichung geändert werden).
Die Texturen sind eigene Pixelkunst; es werden keine Original-Minecraft-Dateien mitgeliefert.
