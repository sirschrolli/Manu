#!/usr/bin/env python3
"""
Bossleisten der Drachen-Bosse (Version 2): je Element ein eigener, verzierter Rahmen.

  * links ein Medaillon mit dem Bildnis des Bosses (aus dem 3D-Modell gerendert, doppelte Auflösung)
  * die Leiste selbst (182 x 9) in einem Metallrahmen mit Nieten, darüber zwei Fassungen für die Phasen-Edelsteine
    (60 % und 30 %), rechts eine Spitze in Form des Elements (Flamme, Kristall, Blitz, Blatt, Fels, Flosse)
  * Verzierungen oben und unten: Flammen, Eiskristalle und Eiszapfen, Blitze, Ranken mit Blättern, Felszacken mit
    Bernstein, Wellenkämme mit Gischt
  * Füllung mit wanderndem Muster (wird im Spiel verschoben), leere Leiste, Spur für erlittenen Schaden, Glanz,
    leuchtende Aura für die letzte Phase, Edelsteine (leuchtend / zersprungen)

Aufbau einer Textur (256 x 256), siehe BossBarRenderer.java:
  (0, 0)     240 x 44  Rahmen; Fenster der Leiste bei (48, 18), 182 x 9; Medaillon-Mitte (21, 22)
  (0, 48)    256 x 9   Füllung (nahtlos kachelbar, wird verschoben)
  (0, 58)    182 x 9   leere Leiste
  (0, 68)    182 x 9   Schadensspur
  (184, 58)   16 x 9   Glanz
  (204, 58)    9 x 9   Edelstein leuchtend      (214, 58) Edelstein zersprungen
  (0, 80)    240 x 44  Aura (letzte Phase)
  (0, 128)    64 x 64  Bildnis (wird auf 32 x 32 gezeichnet)
  (64, 128)   16 x 15  Namensband, Mittelstück (kachelbar)
  (80, 128)   16 x 18  Namensband, linkes Ende mit Schwalbenschwanz   (96, 128) rechtes Ende

Seit 0.94 sitzen die Edelsteine genau bei den Kampfphasen des jeweiligen Bosses (PHASES, wie in BossBarRenderer.java).
"""
from __future__ import annotations

import math
import sys
from pathlib import Path

from PIL import Image, ImageEnhance

sys.path.insert(0, str(Path(__file__).resolve().parent))

from pixelart import CLEAR, hexc, mix  # noqa: E402

FRAME_W, FRAME_H = 240, 44
BAR_X, BAR_Y, BAR_W, BAR_H = 48, 18, 182, 9
EMB_X, EMB_Y, EMB_R = 21, 22, 20.0
# die Edelsteine der Leiste, die gerade entsteht (frame() setzt sie je Boss aus PHASES)
GEMS = (0.6, 0.3)
# Kampfphasen je Boss: bei diesem Anteil des Lebens beginnt die nächste Phase – dort sitzt je ein Edelstein, der im Spiel
# zerspringt, sobald die Phase erreicht ist. Muss mit den Stilen in BossBarRenderer.java übereinstimmen.
PHASES = {
    "fire": (0.6, 0.3), "ice": (0.6, 0.3), "lightning": (0.6, 0.3), "nature": (0.6, 0.3), "earth": (0.6, 0.3),
    "water": (0.6, 0.3), "shadow": (0.6, 0.3),
    "void": (0.5, 0.25),                 # Zorn bei der Hälfte, unter einem Viertel neun Kronensplitter
    "reef": (0.66, 0.4),                 # Ruf ins Riff, dann die Flut
    "kraken": (0.67, 0.34),              # zweimal ruft er die Tiefe, beim zweiten Mal gerät er in Raserei
    "keeper": (0.5,),                    # unter halber Kraft legt er den Kristallpanzer an
    "sea_witch": (0.5,),                 # unter halber Kraft rast sie vor Zorn
    "soul_shepherd": (0.5,),             # unter halber Kraft läutet er im Zorn
    "star_weaver": (0.5,),               # unter halber Kraft rast sie
    "hour_warden": (0.5,),               # unter halber Kraft schlägt die Zwölfte Stunde
    "moth": (0.5,), "forge": (0.5,), "star": (0.5,), "anni": (0.5,), "anni2": (0.5,),
    "mystic": (0.6, 0.3),                # Zugabe und großes Finale
    "schrolli": (0.7, 0.4),              # erste Gestalt: zweimal schneller
    "schrolli_true": (0.6, 0.45, 0.25),  # Nova, Apokalypse, Neuschöpfung
    "schrolli_almighty": (0.66, 0.25),   # zweimal das Sonnengericht
    "world_eater": (0.66, 0.5, 0.33),    # Ruf der Welten, Zorn, Ruf der Welten
    "ignara": (0.5,),                    # unter halber Kraft steigt sie als Phönix auf
    "phasmor": (0.5,),                   # unter halber Kraft setzt er die Krone auf (Phasensturm)
    "sordin": (0.5,),                    # unter halber Kraft das Fortissimo
    "robber_knight": (0.5,),             # unter halber Kraft wird er aus dem Sattel geworfen
}
# die Erzmagier wechseln bei zwei Dritteln und einem Drittel die Phase
MAGE_PHASES = (0.66, 0.33)

# Metall dunkel / mittel / hell, Akzent, Leuchten, dunkler Grund
ELEMENTS = {
    "fire": dict(m0="#1c1412", m1="#4a3a34", m2="#9a8478", acc="#ff6a1a", glow="#ffd27a", deep="#2a0806",
                 f0="#5a0c04", f1="#d8360a", f2="#ff9a2a", f3="#ffe9a0"),
    "ice": dict(m0="#1a2430", m1="#4e6478", m2="#b8d0e0", acc="#6ad0f8", glow="#e8fbff", deep="#0a1c2a",
                f0="#0e3a66", f1="#2a86d0", f2="#7ad0f8", f3="#eafcff"),
    "lightning": dict(m0="#16141e", m1="#3c3a4c", m2="#a8a4bc", acc="#ffd21a", glow="#fff6b0", deep="#100e20",
                      f0="#5a4206", f1="#c89a10", f2="#ffd84a", f3="#fffbe0"),
    "nature": dict(m0="#1e160e", m1="#5a4428", m2="#a88e60", acc="#5ac832", glow="#d8ffa0", deep="#0c1c0a",
                   f0="#14460e", f1="#2e8a1e", f2="#6cd03a", f3="#d4ff9a"),
    "earth": dict(m0="#1e1a16", m1="#4e463c", m2="#a49684", acc="#e09a3a", glow="#ffe2a0", deep="#20140a",
                  f0="#5a2e08", f1="#b0661a", f2="#e8a440", f3="#fff0c0"),
    "water": dict(m0="#0e1a22", m1="#2e4a5a", m2="#8ab4c4", acc="#28c0f0", glow="#c8f6ff", deep="#04182a",
                  f0="#063a5e", f1="#0e78b8", f2="#3cc4ec", f3="#d8faff"),
    "shadow": dict(m0="#0c0812", m1="#2e2442", m2="#8a7ca8", acc="#b46cff", glow="#f0e0ff", deep="#12061e",
                   f0="#2a0a4e", f1="#6420b0", f2="#a660ff", f3="#f2e4ff"),
    # der Zerbrochene König (Boss der Leere): Sternenmarmor mit Goldsaum, Füllung wie ein Sternennebel
    "void": dict(m0="#16121f", m1="#4a4262", m2="#d2cae6", acc="#d6a23c", glow="#f4ecff", deep="#0a0618",
                 f0="#1a0c40", f1="#4c26a0", f2="#9a6ae8", f3="#f4eeff", gold="#e2b04a", gold_dark="#7a5420"),
    # Karkinos, der Riffkoloss: ausgebleichter Muschelkalk mit Goldsaum, darauf ein Korallenriff; Füllung wie das
    # Licht einer Lagune (Wellennetz und Blasen)
    "reef": dict(m0="#2a2219", m1="#7a6850", m2="#eadfc4", acc="#2ad8c8", glow="#d4fff4", deep="#03222a",
                 f0="#043e48", f1="#0a8486", f2="#38d4c4", f3="#e4fff6", gold="#e8b84a", gold_dark="#7a5a1e",
                 coral="#ee5a40", coral_dark="#7e2420", coral_light="#ffb08a", fan="#c25ab8", fan_dark="#5e2058",
                 sponge="#f0a428", sponge_dark="#7a4a10", brain="#d8c060", brain_dark="#7a6420",
                 anemone="#7ae8a8", anemone_dark="#1e6a4c", star="#ff8a3a"),
    # Noctua, die Mottenkönigin: Mondsilber mit Goldsaum, darauf Mottenflügel, Seidenfäden und Mondsicheln; Füllung wie
    # schimmernder Flügelstaub (Schuppenreihen, Glitzer)
    "moth": dict(m0="#1a1428", m1="#5a4e78", m2="#e2dcf2", acc="#ff8ad0", glow="#fff0fa", deep="#140c24",
                 f0="#3a1458", f1="#8a3aa8", f2="#e47ac8", f3="#fff0fa", gold="#ffd878", gold_dark="#8a6420",
                 wing="#9ee8c8", wing_dark="#3a8a74", wing_light="#e4fff2", spot="#ff7ab8", spot_dark="#5a1e4e",
                 silk="#f4eefc", silk_dark="#a898c4"),
    # Malleus, der Glutschmied: geschwärztes Eisen mit Nieten und glühenden Nähten, dahinter Hammer und Speer gekreuzt;
    # Füllung wie fließendes, glühendes Metall mit hellen Rissen
    "forge": dict(m0="#141012", m1="#4a4246", m2="#a89c98", acc="#ff7a1a", glow="#fff0c0", deep="#1c0804",
                  f0="#3e0a02", f1="#a8300a", f2="#ff8a24", f3="#fff2b8", gold="#e6ac48", gold_dark="#7a5218",
                  iron="#2e2c32", iron_light="#7c7880", ember="#ff9a30", ember_dark="#8a2406", wood="#4a2e1e"),
    # Asteria, die Sternenreiterin: poliertes Silber mit Goldsaum, dahinter die Schwingen ihres Sternenrochens und die
    # Sternenlanze; Füllung wie der Nachthimmel mit Nebelschleiern und funkelnden Sternen
    "star": dict(m0="#121628", m1="#5a6488", m2="#dce4f6", acc="#9ad8ff", glow="#f2f8ff", deep="#060a22",
                 f0="#0b1236", f1="#2a3d8a", f2="#7ab0ff", f3="#eef6ff", gold="#e8c25a", gold_dark="#8a6018",
                 night="#141c48", night_dark="#070b22", nebula="#7a3ab4", nebula_light="#c08ae8", silver="#c8d0e4",
                 silver_dark="#5a6488", crystal="#9ad0ff"),
    # MissMystik, die Feen-Zauberkünstlerin: violetter Samt mit Goldsaum, dahinter ein Fächer aus Zauberkarten und ihr
    # Zauberstab; Füllung wie ein Harlekin-Rautenmuster aus Samt, in dem Pailletten funkeln
    "mystic": dict(m0="#140c1e", m1="#4e3470", m2="#dcc8f4", acc="#ff7ac8", glow="#fff0fa", deep="#10061a",
                   f0="#2e0a44", f1="#7a2496", f2="#e46ad0", f3="#fff0fa", gold="#ffd86a", gold_dark="#8a6020",
                   card="#fbf7ff", card_dark="#a898c0", red="#e8344a", black="#1c1428", wand="#121016",
                   confetti="#4ae0e8"),
    # Anni der Troll: dunkles Metall mit Farbsäumen (magenta und cyan), hinter dem Medaillon der Bannhammer mit fehlender
    # Textur, ringsum lose Pixel; Füllung wie ein kaputtes Bild: Zeilen, verrutschte Blöcke, Farbrauschen
    "anni": dict(m0="#121218", m1="#3e3e4c", m2="#b8b8c8", acc="#f800f8", glow="#c8fcff", deep="#0a040e",
                 f0="#3a0040", f1="#8a0a8a", f2="#f040c8", f3="#d8ffff", cyan="#22d8e8", cyan_dark="#0a5a66",
                 red="#ff3040", green="#40ff70", blue="#4080ff", yellow="#ffd400", wood="#5a3a20", wood_dark="#2a1a0c",
                 stone="#8a8a92", stone_dark="#4a4a52", iron="#2e2e36"),
    # Anni der Troll 2.0 (aus der Sicherung wiederhergestellt): derselbe Rahmen, aber Säume, Füllung und Schein in Alarmrot
    "anni2": dict(m0="#160c0e", m1="#4a2e34", m2="#d0b0b8", acc="#ff2040", glow="#ffd0d4", deep="#120406",
                  f0="#3a0008", f1="#8a0a18", f2="#ff3a48", f3="#ffe0e0", cyan="#ff8a30", cyan_dark="#6a2a0a",
                  red="#ff3040", green="#40ff70", blue="#4080ff", yellow="#ffd400", wood="#5a3a20", wood_dark="#2a1a0c",
                  stone="#8a8a92", stone_dark="#4a4a52", iron="#2e2e36"),
    # Schrolli der Schöpfer: Elfenbein und Gold, hinter dem Medaillon zwei Schwingen aus weißen Federn mit goldenen
    # Spitzen, oben ein Kranz aus Strahlen, unten goldene Tropfen, als Spitze der achtstrahlige Zepterstern; Füllung aus
    # strömendem goldenem Licht mit Strahlen und Funkeln
    "schrolli": dict(m0="#3a2808", m1="#a8802a", m2="#fff4cc", acc="#ffc83a", glow="#fff8dc", deep="#2a1a06",
                     f0="#7a4a06", f1="#e0a01c", f2="#ffe070", f3="#ffffff", gold="#ffd24a", gold_dark="#8a5e14",
                     feather="#fbf8f0", feather_mid="#d8d4e4", feather_dark="#8c88a4", tip="#f2c44a", gem_blue="#3a94e8",
                     gem_light="#c8ecff", ray="#fff2b0"),
    # Schrolli der Schöpfer – Wahre Gestalt: dasselbe in den Farben des Weltalls – violettes Metall mit Goldsaum, Schwingen
    # wie ein Sternenhimmel mit schillernden Spitzen, ringsum Augen und Sterne; Füllung aus Nebeln und Sternen
    "schrolli_true": dict(m0="#140a2a", m1="#5a3c9a", m2="#f0e2ff", acc="#c080ff", glow="#f8f0ff", deep="#0a0418",
                          f0="#1a0838", f1="#5a2a9a", f2="#c870f0", f3="#ffffff", gold="#ffd86a", gold_dark="#8a6020",
                          feather="#4a2a92", feather_mid="#22125e", feather_dark="#0a0630", tip="#a0f0ff", gem_blue="#ffe07a",
                          gem_light="#fff8dc", ray="#e8d0ff", prism1="#ffa0a8", prism2="#e890ff", prism3="#80e0ff"),
    # Schrolli der Allmächtige (dritte Gestalt): Weißgold mit Glutsaum; hinter dem Medaillon die Schwingen aus weißen Federn,
    # deren Spitzen in Flammen stehen, auf dem Rahmen eine Sonnenkrone aus Flammenzungen und Strahlen, darunter tropft
    # flüssiges Gold, als Spitze die Sonne; auf dem Ring des Medaillons sieben Sonnensteine; Füllung wie das Plasma einer
    # Sonne (Körnung, Bögen, Funken)
    "schrolli_almighty": dict(m0="#3a1406", m1="#b8601c", m2="#fff0c4", acc="#ff7a1a", glow="#fff2c8", deep="#2c0a02",
                              f0="#a82206", f1="#f8600e", f2="#ffc040", f3="#fffbea", gold="#ffd24a", gold_dark="#8a4a10",
                              feather="#fffaf0", feather_mid="#e8d4b4", feather_dark="#9a7656", ray="#fff2b0",
                              flame0="#5a0a04", flame1="#b82408", flame2="#f0600e", flame3="#ffa024", flame4="#ffd65a",
                              flame5="#fff8d8", sun_dark="#a8280a", sun="#ff8a1e", sun_light="#ffe27a"),
    # der Weltenfresser: Chaosgestein in Platten mit glühenden Fugen; auf dem Rahmen Stücke verschlungener Welten (Wiese mit
    # Baum, Netherrack mit Magma, Endstein mit Chorus, Eis mit Zacken), darunter sein Gebiss, hinter dem Medaillon drei
    # Kronensplitter und ein Hauer, als Spitze die Kristallflosse seines Schwanzes; Füllung wie die Glut seines Schlunds,
    # durch die violette Risse ziehen
    "world_eater": dict(m0="#140f13", m1="#54444e", m2="#ab97a2", acc="#ff5a14", glow="#ffe0a0", deep="#220604",
                        f0="#2e0204", f1="#a01c08", f2="#ff7418", f3="#fff0c0",
                        seam0="#5a0a04", seam1="#e8541a", seam2="#ffd88a",
                        crystal0="#2a1052", crystal1="#6a3cc0", crystal2="#a47cf0", crystal3="#f0e6ff",
                        gold="#e8b84a", gold_dark="#7a5218", tooth="#ddd0b4", tooth_light="#fbf4e4", tooth_dark="#8a7a64",
                        gum="#7a1418", gum_dark="#3a0608",
                        grass="#5a8a32", grass_light="#94c45c", dirt="#6a482e", dirt_dark="#3e2818", wood="#6a4a2c",
                        wood_dark="#3a2614", leaves="#2e5a1e", leaves_light="#5c9a3e", netherrack="#7e2222",
                        netherrack_dark="#481010", magma="#ff8a2a", magma_light="#ffe08a", fungus="#d8342a",
                        fungus_light="#ff7a5a", endstone="#e0daa4", endstone_dark="#a49e66", chorus="#7a4a86",
                        chorus_light="#d0a8d8", ice="#a8ccf4", ice_dark="#5a86c4", ice_light="#f0f8ff"),
    # der Kraken (seit 0.94): Bronze mit grüner Patina und Seepocken; zwei purpurrote Arme liegen auf dem Rahmen und
    # klammern sich mit den Spitzen daran fest, als Spitze ein eingerollter Arm, unter der Leiste Tang und Tinte; Füllung wie
    # seine Tinte – wirbelnde Schlieren in Purpur, darin türkis leuchtende Punkte wie auf seiner Haut
    "kraken": dict(m0="#0f1517", m1="#38494a", m2="#9cbab0", acc="#36dcd2", glow="#d6fff8", deep="#160409",
                   f0="#26040e", f1="#6a1428", f2="#c43c4a", f3="#ffd2c6", gold="#c8a050", gold_dark="#6a4a1e",
                   skin0="#22060e", skin1="#561422", skin2="#7e2230", skin3="#a43a40", skin4="#c8584e",
                   under0="#9a4e5a", under1="#cc7e88", under2="#eab0b0", sucker="#f6d8d6", sucker_dark="#6e2a38",
                   spot="#3ad0e0", spot_light="#a8faff", barnacle="#d8d2c2", barnacle_dark="#6a665c",
                   weed="#3e5a2c", weed_light="#6e8a3e", ink="#1c0818", ink_light="#4a1a3e"),
    # der Steinhüter (seit 0.96): behauene Quader aus Feenstein, in deren Fugen Mondlicht schimmert; hinter dem Medaillon
    # wächst sein Kristallkamm hervor, auf dem Rahmen liegen Polster aus Lavendelmoos, aus denen kleine Mondsteinkristalle
    # sprießen, darunter hängen Leuchtranken mit Blüten; als Spitze ein liegender Kristall. Füllung wie milchiger Mondstein,
    # über den ein bläulicher Schimmer zieht
    "keeper": dict(m0="#2a2536", m1="#645c76", m2="#c4bcd4", acc="#7cb4ff", glow="#e2f0ff", deep="#0c1230",
                   f0="#14245e", f1="#3a5cb4", f2="#9ec2f2", f3="#f4f9ff",
                   seam0="#1e3e9a", seam1="#4f8cf0", seam2="#cfe6ff",
                   moss0="#3a2a62", moss1="#62489a", moss2="#9478cc", moss3="#c8b2ec",
                   crystal0="#24367e", crystal1="#4f6cc8", crystal2="#8eacf0", crystal3="#e8f2ff",
                   vine="#28403a", vine_light="#4e7468", bloom_pink="#ff9ad0", bloom_cyan="#9af2ff",
                   bloom_light="#f2fffe", spore="#ffb0e4"),
    # die Meerhexe (seit 0.97): geschwärzte Bronze mit Grünspan; hinter dem Medaillon fächert schwarze Koralle mit leuchtenden
    # Polypen, auf dem Rahmen sprießen kleine Korallen und liegen Perlen, unter der Leiste treibt Tang mit leuchtenden Spitzen;
    # als Spitze eine Korallengabel mit einer leuchtenden Kugel. Füllung wie ihr Trank – meergrün wirbelnd, mit Blasen
    "sea_witch": dict(m0="#0a1614", m1="#2e4a44", m2="#8ac4b0", acc="#5ae8b8", glow="#dcfff2", deep="#03100c",
                      f0="#04201a", f1="#0e5a48", f2="#2ab890", f3="#dcfff4",
                      coral0="#0a060a", coral1="#24161f", coral2="#4a3443", coral3="#7a5a6c",
                      polyp="#ff7ac8", polyp_light="#ffd4f0", kelp="#1a4424", kelp_light="#3e7a3e", kelp_glow="#7affc8",
                      pearl="#f2eefa", pearl_dark="#9a92b4", orb="#3ccad8", orb_light="#c4fff8"),
    # der Seelenhirte (seit 0.98): geschwärztes Eisen mit verwittertem Bein; hinter dem Medaillon rollen sich zwei gerippte
    # Widderhörner, unter der Leiste hängen an Ketten Talismane (Knochen, ein Schädel, Laternchen), als Spitze der Haken seines
    # Stabs mit der Laterne voll Seelenfeuer; über dem Rahmen treiben Irrlichter. Füllung wie Seelenfeuer – züngelnde Flammen
    "soul_shepherd": dict(m0="#0c0d10", m1="#3a3644", m2="#b4acc0", acc="#4fe0d0", glow="#e0fffa", deep="#03100f",
                          f0="#032a28", f1="#0c6a62", f2="#2ac8bc", f3="#e8fffb",
                          horn0="#1e160e", horn1="#4a3a26", horn2="#86704f", horn3="#c8b28a",
                          iron0="#121316", iron1="#2b2e35", iron2="#50555f", iron3="#8a919c",
                          bone="#e2d8c0", bone_dark="#8e846e", soul="#2ac8bc", soul_light="#c4fff6", wood="#3e2d1e"),
    # die Sternenweberin (seit 0.99): nachtblaues Chitin mit Silbersaum; hinter dem Medaillon greifen vier gegliederte
    # Spinnenbeine nach oben und unten, als Spitze ein Stern in einem Knoten aus Sternenseide; unter der Leiste hängen an Fäden
    # Kokons und Sterne, über der Oberkante funkeln Sterne. Füllung wie ihr Netz: Nachthimmel, durchzogen von leuchtenden Fäden
    "star_weaver": dict(m0="#0a0b18", m1="#2c3052", m2="#9ca8d8", acc="#8ad8ff", glow="#effbff", deep="#030616",
                        f0="#040822", f1="#16246a", f2="#4a7ee0", f3="#eaf6ff",
                        chitin0="#08091a", chitin1="#1d214b", chitin2="#3f4e86", chitin3="#7286c0",
                        silk="#d6e4ff", silk_dark="#7486b8", star="#fff4c4", star2="#9ad8ff", nebula="#8048bc"),
    # der Stundenwächter (seit 1.01): Uhrbronze mit Messingsaum; hinter dem Medaillon ein Ring aus Zahnradzähnen, über der
    # Oberkante greifen Zahnräder ineinander, als Spitze ein gebläuter Minutenzeiger mit Rautenspitze, unter der Leiste hängen
    # an Ketten Uhrgewichte und ein Pendel. Füllung wie sein Uhrwerk: glühendes Bernstein mit einer Minutenbahn
    "hour_warden": dict(m0="#1e1006", m1="#6c4016", m2="#e8bc62", acc="#ffb347", glow="#fff0c4", deep="#120802",
                        f0="#2a1004", f1="#8a3a08", f2="#ffb347", f3="#fff0c4",
                        brass0="#5a3c0e", brass1="#a07420", brass2="#deb44a", brass3="#fff0b8",
                        blued0="#0b1030", blued1="#283a86", blued2="#7e96dc", enamel="#ebe1ca", ink="#1f233a"),
    # Ignara, die Flammenpriesterin (seit 1.2): Tempelgold mit Feueropalen in der Oberkante, darüber lodern Flammenzungen (am
    # Medaillon hoch wie ihr Flammenhaar), unter der Leiste schlängelt sich ihre Flammenpeitsche, als Spitze ein großer
    # Feueropal; die Füllung züngelnde Flammen mit den grünen und blauen Blitzen eines Opals
    "ignara": dict(m0="#2a1208", m1="#8a561c", m2="#f2c860", acc="#ff6a1a", glow="#fff0c0", deep="#2a0604",
                   f0="#4a0806", f1="#b8280a", f2="#ff8a24", f3="#fff2b8", gold="#f2c45a", gold_dark="#8a5a18",
                   opal0="#5a0c0a", opal1="#c8300e", opal2="#ff8a2a", opal3="#ffe08a", flash_g="#5af0a0", flash_b="#62c8ff",
                   leather="#6a1e12", leather_light="#a8442a", leather_dark="#2e0a08"),
    # Phasmor, der Leerwandlerkönig (seit 1.5): Leerenstahl mit Purpur; hinter dem Medaillon der türkise Strahlenkranz seines
    # Kragens, über der Oberkante schweben Phasitsplitter mit blassen Nachbildern, darunter liegt die Phasenklinge, als Spitze
    # ein Phasit in Purpur; die Füllung geschliffener Phasit, über dessen Facetten ein Schimmer zieht
    "phasmor": dict(m0="#0b0912", m1="#3a3456", m2="#a9a2cc", acc="#3fe6cc", glow="#d8fff8", deep="#031816",
                    f0="#03201c", f1="#0b6a5e", f2="#3ce0c8", f3="#e0fff8",
                    crystal0="#04403a", crystal1="#0c9888", crystal2="#3ce0c8", crystal3="#d8fffa",
                    purpur="#c48ccc", purpur_dark="#6a3a74", purpur_light="#ecc8f0",
                    grip="#241c38", grip_light="#4a3e66", ray="#86f4e0"),
    # Sordin, der Kantor der Stille (seit 1.7): Schiefer und grün angelaufenes Kupfer; hinter dem Medaillon der Fächer seiner
    # Orgelpfeifen, über der Oberkante schweben Noten mit blassen Echoringen, darunter liegt die Stimmgabel, als Spitze ein
    # Hallkristall in Kupfer; die Füllung Schallwellen, über die helle Kämme laufen
    "sordin": dict(m0="#0a0d14", m1="#2e3646", m2="#8a96aa", acc="#29dfeb", glow="#e6ffff", deep="#021418",
                   f0="#021a1e", f1="#0b6f7c", f2="#29dfeb", f3="#e6ffff",
                   copper="#c8763e", copper_dark="#6a3a20", copper_light="#f2b98a",
                   verdigris="#5aa58a", verdigris_dark="#25594b", verdigris_light="#9ed8bc",
                   crystal0="#1e3e5c", crystal1="#4a86b0", crystal2="#9cd8ff", crystal3="#f2fbff",
                   leather="#1e4e52", leather_light="#3a8286"),
    # Kuno von Rabenfels, der Raubritter (seit 1.8): geschwärztes Eisen mit Gold, hinter dem Medaillon zwei gekreuzte Lanzen
    # mit Wimpeln (Gold und Schwarz), über der Oberkante Zinnen und zwei fliegende Raben, darunter liegt seine Lanze mit dem
    # Wimpel, als Spitze ein Rabenkopf; die Füllung Gold mit Sparren, die nach vorn weisen (wie sein Ansturm)
    "robber_knight": dict(m0="#0c0b0e", m1="#38343a", m2="#a49c94", acc="#e4c254", glow="#fff6cc", deep="#1a1206",
                          f0="#2a1c04", f1="#8a6414", f2="#e4c254", f3="#fff6cc",
                          gold="#e4c254", gold_dark="#8a6414", gold_light="#fff0a8",
                          steel="#9aa2ac", steel_dark="#4a5058", steel_light="#e2e8ee",
                          wood="#6e4a26", wood_dark="#3a2612", wood_light="#9a6e40",
                          raven="#141418", raven_light="#3e404c", stone="#4a4648", stone_light="#7a7470", stone_dark="#26242a"),
}

OUTLINE = (12, 10, 12, 255)


def c(pal: dict, key: str):
    return hexc(pal[key])


def put(px, img, x, y, col):
    if 0 <= x < img.width and 0 <= y < img.height:
        px[x, y] = col


def hash01(*a) -> float:
    h = 2166136261
    for v in a:
        h = ((h ^ (int(v) & 0xFFFFFFFF)) * 16777619) & 0xFFFFFFFF
    h ^= h >> 13
    h = (h * 1274126177) & 0xFFFFFFFF
    return (h & 0xFFFF) / 0xFFFF


# =============================================================================================
# Rahmen
# =============================================================================================

def frame_border(img: Image.Image, pal: dict) -> None:
    """Metallrahmen um die Leiste: dunkle Außenlinie, Fase (oben/links hell, unten/rechts dunkel), Innenschatten."""
    px = img.load()
    m0, m1, m2 = c(pal, "m0"), c(pal, "m1"), c(pal, "m2")
    x0, x1, y0, y1 = BAR_X - 3, BAR_X + BAR_W + 2, BAR_Y - 3, BAR_Y + BAR_H + 2
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if BAR_X <= x < BAR_X + BAR_W and BAR_Y <= y < BAR_Y + BAR_H:
                continue
            ring = min(x - x0, x1 - x, y - y0, y1 - y)
            if ring == 0:
                col = OUTLINE
            elif ring == 1:
                col = mix(m2, m1, 0.15) if (y - y0 <= 1 or x - x0 <= 1) and not (y1 - y <= 1) else mix(m1, m0, 0.35)
            else:
                col = mix(m0, OUTLINE, 0.3)
            px[x, y] = col
    # Nieten oben und unten
    for x in range(x0 + 9, x1 - 4, 22):
        if any(abs(x - (BAR_X + round(BAR_W * f))) < 7 for f in GEMS):
            continue
        for y in (y0 + 1, y1 - 1):
            put(px, img, x, y, m2)
            put(px, img, x + 1, y, mix(m1, m0, 0.4))
    # Fassungen der Edelsteine (oberhalb des Rahmens)
    for f in GEMS:
        gx = BAR_X + round(BAR_W * f)
        for dy in range(-6, 1):
            half = 5 - abs(dy + 3) + 1
            for dx in range(-half, half + 1):
                x, y = gx + dx, y0 + dy
                edge = abs(dx) == half or dy in (-6, 0)
                put(px, img, x, y, OUTLINE if edge else (mix(m1, m0, 0.2) if dx < 0 else mix(m0, OUTLINE, 0.2)))


def emblem(img: Image.Image, pal: dict) -> None:
    """Medaillon: Ring aus Metall (Licht von oben links), dunkler Grund mit Schimmer in der Farbe des Elements."""
    px = img.load()
    m0, m1, m2 = c(pal, "m0"), c(pal, "m1"), c(pal, "m2")
    deep, acc = c(pal, "deep"), c(pal, "acc")
    # Brücke zum Rahmen
    for y in range(BAR_Y - 1, BAR_Y + BAR_H + 1):
        for x in range(int(EMB_X + EMB_R) - 2, BAR_X - 2):
            edge = y in (BAR_Y - 1, BAR_Y + BAR_H)
            px[x, y] = OUTLINE if edge else mix(m1, m0, 0.3 + 0.4 * (y - BAR_Y) / BAR_H)
    for y in range(FRAME_H):
        for x in range(0, int(EMB_X + EMB_R) + 2):
            d = math.hypot(x - EMB_X + 0.5, y - EMB_Y + 0.5)
            if d > EMB_R:
                continue
            ang = math.atan2(y - EMB_Y, x - EMB_X)
            if d > EMB_R - 1.0:
                px[x, y] = OUTLINE
            elif d > EMB_R - 4.0:
                lit = 0.5 + 0.5 * math.cos(ang + 2.35)          # oben links hell
                col = mix(m0, m2, 0.15 + 0.7 * lit)
                if d > EMB_R - 1.8:
                    col = mix(col, m0, 0.35)
                px[x, y] = col
            elif d > EMB_R - 5.0:
                px[x, y] = OUTLINE
            else:
                t = d / (EMB_R - 5.0)
                px[x, y] = mix(mix(acc, deep, 0.25), mix(deep, OUTLINE, 0.2), t ** 1.3)


def gem(pal: dict, broken: bool) -> Image.Image:
    img = Image.new("RGBA", (9, 9), CLEAR)
    px = img.load()
    acc, glow = c(pal, "acc"), c(pal, "glow")
    for y in range(9):
        for x in range(9):
            d = abs(x - 4) + abs(y - 4)
            if d > 4:
                continue
            if d == 4:
                px[x, y] = OUTLINE
                continue
            if broken:
                col = (58, 54, 60, 255) if (x + y) % 3 else (40, 38, 44, 255)
                if (x == 3 and y in (2, 3)) or (x == 4 and y in (4, 5)) or (x == 5 and y == 6):
                    col = (20, 18, 22, 255)
            else:
                t = (x + y) / 8
                col = mix(glow, acc, min(1.0, t * 1.2))
                if (x, y) in ((3, 2), (2, 3), (3, 3)):
                    col = (255, 255, 255, 255)
            px[x, y] = col
    return img


# =============================================================================================
# Verzierungen je Element
# =============================================================================================

def deco_fire(img: Image.Image, pal: dict) -> None:
    """Flammenzungen oben auf dem Rahmen (am Medaillon und am Ende höher), Glutfunken darunter, Flamme als Spitze."""
    px = img.load()
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    top = BAR_Y - 3

    def flame(cx: int, base: int, h: int, w: int):
        for k in range(h):
            t = k / max(1, h - 1)
            half = max(0, round((w / 2) * (1 - t) ** 0.7 * (0.9 + 0.2 * math.sin(k * 1.7 + cx))))
            sway = round(math.sin(k * 0.8 + cx * 0.3) * t * 1.2)
            for dx in range(-half, half + 1):
                inner = abs(dx) < half
                col = f1 if t < 0.35 else (f2 if t < 0.7 else f3)
                if not inner:
                    col = mix(f0, f1, 0.4) if t < 0.5 else f1
                elif abs(dx) <= max(0, half - 2) and t < 0.6:
                    col = f2 if t < 0.3 else f3
                put(px, img, cx + dx + sway, base - k, col)
    for x in range(BAR_X - 2, BAR_X + BAR_W + 2, 5):
        rel = (x - BAR_X) / BAR_W
        hgt = 3 + round(4 * (abs(rel - 0.5) * 2) ** 2 + 2 * hash01(x, 7))
        flame(x, top - 1, hgt, 4)
    # Krone aus Flammen auf dem Medaillon
    for k, ang in enumerate(range(-150, -20, 26)):
        a = math.radians(ang)
        x = round(EMB_X + math.cos(a) * (EMB_R - 1))
        y = round(EMB_Y + math.sin(a) * (EMB_R - 1))
        flame(x, y, 4 + (k % 2) * 2, 4)
    # Glutfunken unterhalb
    for x in range(BAR_X + 4, BAR_X + BAR_W, 9):
        if hash01(x, 3) > 0.55:
            put(px, img, x, BAR_Y + BAR_H + 4 + round(hash01(x, 5) * 4), f2 if hash01(x, 9) > 0.5 else f1)
    # Spitze: liegende Flamme nach rechts
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(10):
        half = round(5 * (1 - k / 10) ** 0.8)
        for dy in range(-half, half + 1):
            col = OUTLINE if abs(dy) == half else (f1 if abs(dy) > half - 2 else (f2 if k < 6 else f3))
            put(px, img, ex + k, ey + dy + round(math.sin(k * 0.9) * k / 8), col)


def deco_ice(img: Image.Image, pal: dict) -> None:
    """Eiskristalle oben, Eiszapfen unten, Kristallspitzen am Medaillon, Kristall als Spitze."""
    px = img.load()
    f1, f2, f3 = c(pal, "f1"), c(pal, "f2"), c(pal, "f3")
    m0 = c(pal, "m0")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def crystal(cx: int, base: int, h: int, w: int, up: bool = True):
        for k in range(h):
            half = max(0, round((w / 2) * (1 - k / h)))
            for dx in range(-half, half + 1):
                if abs(dx) == half:
                    col = mix(f1, m0, 0.3)
                else:
                    col = f3 if dx < 0 else (f2 if dx == 0 else f1)
                put(px, img, cx + dx, base - k if up else base + k, col)
    for x in range(BAR_X + 3, BAR_X + BAR_W, 11):
        h = 4 + round(5 * hash01(x, 2))
        crystal(x, top - 1, h, 4)
        if hash01(x, 4) > 0.4:
            crystal(x + 3, top - 1, max(2, h - 3), 2)
    for x in range(BAR_X + 6, BAR_X + BAR_W - 2, 7):
        crystal(x, bottom + 1, 3 + round(6 * hash01(x, 6)), 2, up=False)
    for ang in (-90, -135, -45, 180):
        a = math.radians(ang)
        crystal(round(EMB_X + math.cos(a) * (EMB_R - 2)), round(EMB_Y + math.sin(a) * (EMB_R - 2)), 6, 4,
                up=ang != 180)
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(10):
        half = round(4 * (1 - abs(k - 3) / 7)) if k > 3 else k + 1
        for dy in range(-half, half + 1):
            col = OUTLINE if abs(dy) == half else (f3 if dy < 0 else f1)
            put(px, img, ex + k, ey + dy, col)


def deco_lightning(img: Image.Image, pal: dict) -> None:
    """Goldene Zickzack-Einlage im Rahmen, kleine Blitze oben, Funken, Blitz als Spitze."""
    px = img.load()
    f2, f3, acc = c(pal, "f2"), c(pal, "f3"), c(pal, "acc")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2
    for x in range(BAR_X - 2, BAR_X + BAR_W + 2):
        zig = (x // 3) % 2
        put(px, img, x, top + 1 if zig else top + 2, acc)
        put(px, img, x, bottom - 1 if not zig else bottom - 2, mix(acc, (0, 0, 0, 255), 0.35))

    def bolt(x: int, y: int, length: int, seed: int):
        cx = x
        for k in range(length):
            if k % 3 == 2:
                cx += 1 if hash01(seed, k) > 0.5 else -1
            put(px, img, cx, y - k, f3 if k % 2 else f2)
            put(px, img, cx + 1, y - k, OUTLINE)
    for i, x in enumerate(range(BAR_X + 10, BAR_X + BAR_W, 26)):
        bolt(x, top - 1, 4 + (i % 3) * 2, x)
    for x in range(BAR_X, BAR_X + BAR_W, 6):
        if hash01(x, 11) > 0.7:
            put(px, img, x, top - 3 - round(hash01(x, 12) * 5), f3)
        if hash01(x, 13) > 0.75:
            put(px, img, x, bottom + 2 + round(hash01(x, 14) * 4), f2)
    for ang in range(-160, 170, 40):
        a = math.radians(ang)
        x = round(EMB_X + math.cos(a) * (EMB_R - 2.5))
        y = round(EMB_Y + math.sin(a) * (EMB_R - 2.5))
        put(px, img, x, y, f3)
    # Blitz nach rechts
    ex, ey = BAR_X + BAR_W + 3, BAR_Y - 1
    shape = ["xx.......", ".xxx.....", "..xxxx...", "...xxxxx.", "....xxx..", "...xxxx..", "....xxxxx", ".....xxx.",
             "......xx.", ".......x."]
    for dy, row in enumerate(shape):
        for dx, ch in enumerate(row):
            if ch == "x":
                put(px, img, ex + dx, ey + dy, f3 if dx < 4 else f2)
    for dy, row in enumerate(shape):
        for dx, ch in enumerate(row):
            if ch == "x":
                for ox, oy in ((1, 0), (0, 1), (-1, 0), (0, -1)):
                    nx, ny = dx + ox, dy + oy
                    inside = 0 <= ny < len(shape) and 0 <= nx < len(shape[ny]) and shape[ny][nx] == "x"
                    tx, ty = ex + nx, ey + ny
                    if not inside and 0 <= tx < img.width and 0 <= ty < img.height and px[tx, ty][3] == 0:
                        put(px, img, tx, ty, OUTLINE)


def deco_nature(img: Image.Image, pal: dict) -> None:
    """Ranken winden sich um den Rahmen, mit Blättern und ein paar Blüten; ein Blatt als Spitze."""
    px = img.load()
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    blossom = hexc("#f07ab0")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def leaf(x: int, y: int, d: int):
        cells = [(0, 0), (d, 0), (d, -1), (2 * d, -1), (d, 1), (0, -1)]
        for i, (dx, dy) in enumerate(cells):
            put(px, img, x + dx, y + dy, f2 if i < 3 else f1)
        put(px, img, x + 2 * d, y - 1, f3)
    for x in range(BAR_X - 3, BAR_X + BAR_W + 3):
        yt = top - 1 + round(1.6 * math.sin(x * 0.21))
        yb = bottom + 1 + round(1.6 * math.sin(x * 0.21 + 2.0))
        put(px, img, x, yt, f0)
        put(px, img, x, yt - 1, f1)
        put(px, img, x, yb, f0)
        put(px, img, x, yb + 1, mix(f0, f1, 0.4))
        if x % 9 == 0:
            leaf(x, yt - 2, 1 if (x // 9) % 2 else -1)
        if x % 11 == 5:
            leaf(x, yb + 2, -1 if (x // 11) % 2 else 1)
        if x % 31 == 13:
            put(px, img, x, yt - 3, blossom)
            put(px, img, x + 1, yt - 3, hexc("#ffd0e4"))
    for ang in range(-170, 180, 34):
        a = math.radians(ang)
        leaf(round(EMB_X + math.cos(a) * (EMB_R + 0.5)), round(EMB_Y + math.sin(a) * (EMB_R + 0.5)), 1 if ang < 0 else -1)
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(10):
        half = round(4.5 * math.sin(math.pi * (k + 0.5) / 10))
        for dy in range(-half, half + 1):
            col = OUTLINE if abs(dy) == half else (f3 if dy == 0 else (f2 if dy < 0 else f1))
            put(px, img, ex + k, ey + dy, col)


def deco_earth(img: Image.Image, pal: dict) -> None:
    """Zackige Felsen auf dem Rahmen, Bernsteinkristalle, Geröll darunter, Felsspitze am Ende."""
    px = img.load()
    m0, m1, m2 = c(pal, "m0"), c(pal, "m1"), c(pal, "m2")
    f2, f3 = c(pal, "f2"), c(pal, "f3")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2
    x = BAR_X - 3
    while x < BAR_X + BAR_W + 2:
        w = 5 + round(5 * hash01(x, 1))
        h = 2 + round(4 * hash01(x, 2))
        for dx in range(w):
            t = dx / max(1, w - 1)
            colh = round(h * (1 - abs(t - 0.4) * 1.4))
            for k in range(max(1, colh)):
                col = m2 if dx < w * 0.4 else (m1 if dx < w * 0.8 else mix(m1, m0, 0.5))
                if k == colh - 1:
                    col = mix(col, m2, 0.5)
                put(px, img, x + dx, top - 1 - k, col)
        x += w
    for i, cx in enumerate((BAR_X + 30, BAR_X + 85, BAR_X + 140)):
        for j, (dx, h) in enumerate(((0, 6), (2, 4), (-2, 3))):
            for k in range(h):
                put(px, img, cx + dx, top - 2 - k, f3 if k > h - 3 else f2)
                put(px, img, cx + dx + 1, top - 2 - k, mix(f2, m0, 0.4))
    for gx in range(BAR_X + 3, BAR_X + BAR_W, 7):
        if hash01(gx, 5) > 0.5:
            put(px, img, gx, bottom + 1, m1)
            put(px, img, gx + 1, bottom + 1, m0)
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(9):
        half = round(5 * (1 - k / 9))
        for dy in range(-half, half + 1):
            col = OUTLINE if abs(dy) == half else (m2 if dy < 0 else m1)
            put(px, img, ex + k, ey + dy, col)


def deco_water(img: Image.Image, pal: dict) -> None:
    """Wellenkämme mit Gischt oben, Luftblasen unten, Flosse als Spitze."""
    px = img.load()
    f1, f2, f3 = c(pal, "f1"), c(pal, "f2"), c(pal, "f3")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2
    period = 14
    for x in range(BAR_X - 3, BAR_X + BAR_W + 3):
        u = ((x - BAR_X) % period) / period
        crest = round(4 * math.sin(math.pi * u) ** 3)
        for k in range(crest + 1):
            col = f1 if k < crest - 1 else f2
            put(px, img, x, top - 1 - k, col)
        if crest >= 3:
            put(px, img, x, top - 2 - crest, f3)
        if 0.6 < u < 0.75:
            put(px, img, x, top - 1 - crest, f3)
    for x in range(BAR_X + 5, BAR_X + BAR_W, 13):
        y = bottom + 3 + round(hash01(x, 2) * 4)
        r = 1 if hash01(x, 3) > 0.5 else 0
        for dx in range(-r, r + 1):
            for dy in range(-r, r + 1):
                put(px, img, x + dx, y + dy, f2 if (dx, dy) != (-r, -r) else f3)
        put(px, img, x - r, y - r, f3)
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(10):
        up = round(6 * (k / 9) ** 1.4)
        down = round(4 * (k / 9) ** 1.4)
        for dy in range(-up, down + 1):
            if k < 9 and abs(dy) < 2 + k // 3:
                col = OUTLINE if dy in (-up, down) else (f3 if dy < 0 else f1)
                put(px, img, ex + k, ey + dy, col)


def deco_shadow(img: Image.Image, pal: dict) -> None:
    """Dornige Nachtkristalle oben (zum Medaillon hin höher), Seelenfunken darunter, eine Krone aus Kristallen am
    Medaillon und ein wachendes Auge als Spitze."""
    px = img.load()
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    m0 = c(pal, "m0")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def shard(cx: int, base: int, h: int, w: int, lean: float, up: bool = True):
        for k in range(h):
            t = k / max(1, h - 1)
            half = max(0, round((w / 2) * (1 - t)))
            off = round(lean * k)
            for dx in range(-half, half + 1):
                if abs(dx) == half:
                    col = mix(f0, m0, 0.35)
                else:
                    col = f3 if (dx < 0 and t > 0.4) else (f2 if dx <= 0 else f1)
                put(px, img, cx + dx + off, base - k if up else base + k, col)
    for x in range(BAR_X - 1, BAR_X + BAR_W + 1, 6):
        rel = (x - BAR_X) / BAR_W
        h = 3 + round(5 * (1 - rel) ** 2 + 3 * hash01(x, 11))
        shard(x, top - 1, h, 3 if h < 6 else 4, (hash01(x, 13) - 0.5) * 0.5)
    # Seelenfunken unter der Leiste, wie aufsteigende Irrlichter
    for x in range(BAR_X + 3, BAR_X + BAR_W, 8):
        if hash01(x, 17) > 0.45:
            y = bottom + 2 + round(hash01(x, 19) * 5)
            put(px, img, x, y, f3)
            put(px, img, x, y + 1, f2)
            put(px, img, x + (1 if hash01(x, 23) > 0.5 else -1), y + 2, f1)
    # Kristallkrone am Medaillon
    for k, ang in enumerate(range(-160, -10, 22)):
        a = math.radians(ang)
        x = round(EMB_X + math.cos(a) * (EMB_R - 1))
        y = round(EMB_Y + math.sin(a) * (EMB_R - 1))
        shard(x, y, 5 + (k % 2) * 3, 3, math.cos(a) * 0.5)
    # Spitze: ein wachendes Auge (Mandelform, leuchtende Pupille)
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(11):
        half = round(4.2 * math.sin(math.pi * k / 10))
        for dy in range(-half, half + 1):
            if abs(dy) == half:
                col = OUTLINE
            elif abs(k - 5) <= 1 and abs(dy) <= 1:
                col = f3 if (k, dy) == (5, 0) else f2
            else:
                col = mix(m0, f0, 0.6)
            put(px, img, ex + k, ey + dy, col)


def deco_void(img: Image.Image, pal: dict) -> None:
    """Schwebende Bruchstücke aus Sternenmarmor über der Leiste (am Medaillon dichter), dazwischen Sterne; darunter
    Sternenstaub; am Medaillon die Krone aus sieben Kristallsplittern des Königs; als Spitze sein zersprungenes Herz."""
    px = img.load()
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    m0, m1, m2 = c(pal, "m0"), c(pal, "m1"), c(pal, "m2")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def chunk(cx: int, cy: int, w: int, h: int, seed: int):
        """Ein kleines Bruchstück: unregelmäßiger Umriss, oben hell, eine goldene Kante."""
        for dy in range(h):
            for dx in range(w):
                corner = (dx in (0, w - 1)) and (dy in (0, h - 1)) and hash01(seed, dx, dy) > 0.35
                if corner:
                    continue
                edge = dx in (0, w - 1) or dy in (0, h - 1)
                if edge:
                    col = OUTLINE
                elif dy == 1:
                    col = m2
                elif dy == h - 2 and hash01(seed, 3) > 0.4:
                    col = gold
                else:
                    col = mix(m1, m2, 0.35 + 0.3 * hash01(seed, dx, dy))
                put(px, img, cx + dx, cy + dy, col)

    def star(x: int, y: int, big: bool):
        put(px, img, x, y, f3)
        if big:
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                put(px, img, x + dx, y + dy, f2)

    # ein zerbrochener Fries: am Medaillon liegen die Stücke noch auf dem Rahmen, nach rechts lösen sie sich, steigen
    # und werden kleiner, bis nur noch Sterne treiben
    x = BAR_X + 1
    while x < BAR_X + BAR_W - 6:
        rel = (x - BAR_X) / BAR_W
        w = max(3, round(7 - 4 * rel + hash01(x, 31)))
        h = 4 if rel < 0.55 else 3
        lift = round(rel * 8 + (hash01(x, 41) - 0.5) * 2.0 * min(1.0, rel * 3))
        near_gem = any(abs(x + w // 2 - (BAR_X + round(BAR_W * f))) < 8 for f in GEMS)
        if not near_gem and rel < 0.9:
            chunk(x, top - lift - h + 1, w, h, x)
            if hash01(x, 89) > 0.55 and w >= 4:
                put(px, img, x + 1 + round(hash01(x, 97) * (w - 3)), top - lift - h + 3, f2)   # leuchtender Riss
        gap = 2 + round(rel * 6 + hash01(x, 59) * 2)
        if rel > 0.25 and hash01(x, 43) > 0.4:
            star(x + w + gap // 2, top - lift - 3 - round(hash01(x, 47) * 5), hash01(x, 53) > 0.75)
        x += w + gap
    # Sternenstaub unter der Leiste
    for x in range(BAR_X + 2, BAR_X + BAR_W, 6):
        if hash01(x, 61) > 0.6:
            y = bottom + 2 + round(hash01(x, 67) * 4)
            put(px, img, x, y, f3 if hash01(x, 71) > 0.5 else f2)
            if hash01(x, 73) > 0.8:
                put(px, img, x + 1, y + 1, f1)
    # die Krone aus sieben Kristallsplittern über dem Medaillon (die mittlere am höchsten)

    def shard(cx: int, base: int, h: int, w: int, lean: float):
        for k2 in range(h):
            t = k2 / max(1, h - 1)
            half = max(0, round((w / 2) * (1 - t)))
            off = round(lean * k2)
            for dx in range(-half, half + 1):
                if abs(dx) == half:
                    col = mix(f0, m0, 0.35)
                else:
                    col = f3 if (dx < 0 and t > 0.35) else (f2 if dx <= 0 else f1)
                put(px, img, cx + dx + off, base - k2, col)
    angles = [-90 + (i - 3) * 22 for i in range(7)]
    for i, ang in enumerate(angles):
        a = math.radians(ang)
        x = round(EMB_X + math.cos(a) * (EMB_R - 1))
        y = round(EMB_Y + math.sin(a) * (EMB_R - 1))
        h = 10 if i == 3 else (8 if i in (2, 4) else 6)
        shard(x, y, h, 3 if i != 3 else 4, math.cos(a) * 0.55)
    # goldener Reif um das Medaillon (oben)
    for ang in range(-170, -9, 4):
        a = math.radians(ang)
        put(px, img, round(EMB_X + math.cos(a) * (EMB_R + 0.2)), round(EMB_Y + math.sin(a) * (EMB_R + 0.2)),
            gold if ang < -90 else gold_dark)
    # Spitze: das zersprungene Herz – zwei Kristallhälften, dazwischen ein leuchtender Spalt
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for kx in range(12):
        half = round(4.6 * math.sin(math.pi * (kx + 0.5) / 12) + (1.2 if kx in (2, 3, 8, 9) else 0.0))
        for dy in range(-half, half + 1):
            gap = abs(kx - 6 + (1 if dy > 0 else 0) * (dy % 2)) <= 0
            if abs(dy) == half:
                col = OUTLINE
            elif gap:
                col = f3
            else:
                t = (dy + half) / max(1, 2 * half)
                col = mix(f2, f0, t) if kx < 6 else mix(f1, f0, t * 0.8)
                if kx in (2, 3) and dy == -half + 1:
                    col = f3
            put(px, img, ex + kx, ey + dy, col)


def deco_reef(img: Image.Image, pal: dict) -> None:
    """Ein Korallenriff wächst auf dem Rahmen (am Medaillon hoch und dicht, nach rechts niedriger, am Ende nur noch
    Muscheln und ein Seestern): Geweihkorallen, Gorgonienfächer, Tischkorallen, Hirnkorallen, Röhrenschwämme, Seegras
    und Anemonen, dazwischen zwei kleine Fische. Unter der Leiste hängen Seepocken und Rotalgen, Blasen steigen auf.
    Um das Medaillon ein Kranz aus Korallenästen mit Goldreif, als Spitze die geöffnete Schere des Karkinos."""
    px = img.load()
    f1, f2, f3 = (c(pal, k) for k in ("f1", "f2", "f3"))
    m0, m1, m2 = c(pal, "m0"), c(pal, "m1"), c(pal, "m2")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    coral, coral_dark, coral_light = c(pal, "coral"), c(pal, "coral_dark"), c(pal, "coral_light")
    fan, fan_dark = c(pal, "fan"), c(pal, "fan_dark")
    sponge, sponge_dark = c(pal, "sponge"), c(pal, "sponge_dark")
    brain, brain_dark = c(pal, "brain"), c(pal, "brain_dark")
    anemone, anemone_dark = c(pal, "anemone"), c(pal, "anemone_dark")
    star_col = c(pal, "star")
    white = (255, 250, 236, 255)
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2
    base = top - 1                                   # unterste Zeile über dem Rahmen

    def line(x0: float, y0: float, x1: float, y1: float, col0, col1, width: int = 1):
        n = max(1, round(max(abs(x1 - x0), abs(y1 - y0))))
        for k in range(n + 1):
            t = k / n
            x, y = round(x0 + (x1 - x0) * t), round(y0 + (y1 - y0) * t)
            col = mix(col0, col1, t)
            put(px, img, x, y, col)
            if width > 1:
                put(px, img, x + 1, y, mix(col, OUTLINE, 0.35))

    def staghorn(x: float, y: float, ang: float, length: float, depth: int, seed: int, palette=None):
        """Geweihkoralle: dicker Stamm, Äste verzweigen sich, an den Spitzen helle Polypen."""
        dark, mid, light = palette or (coral_dark, coral, coral_light)
        x1, y1 = x + math.cos(ang) * length, y + math.sin(ang) * length
        line(x, y, x1, y1, mix(dark, mid, 0.4) if depth >= 2 else mid, mid if depth else light, 2 if depth >= 2 else 1)
        if depth == 0:
            put(px, img, round(x1), round(y1), light)
            put(px, img, round(x1), round(y1) - 1 if math.sin(ang) < 0 else round(y1) + 1, white)
            return
        spread = 0.5 + 0.25 * hash01(seed, 1)
        staghorn(x1, y1, ang - spread, length * 0.7, depth - 1, seed * 3 + 1, palette)
        staghorn(x1, y1, ang + spread * 0.9, length * 0.62, depth - 1, seed * 3 + 2, palette)
        if depth >= 2 and hash01(seed, 4) > 0.4:
            staghorn((x + x1) / 2, (y + y1) / 2, ang + (spread + 0.3) * (1 if hash01(seed, 5) > 0.5 else -1),
                     length * 0.45, 0, seed * 3 + 3, palette)

    def gorgonian(cx: int, r: int, seed: int):
        """Gorgonienfächer, von vorn gesehen: fünf feine Adern strahlen vom Fuß aus und verzweigen sich, am Rand
        hellere Polypen – dazwischen bleibt der Fächer offen."""
        for k in range(5):
            a = math.radians(-150 + k * 30 + (hash01(seed, k) - 0.5) * 10)
            mx, my = cx + math.cos(a) * r * 0.55, base + math.sin(a) * r * 0.5
            line(cx, base, mx, my, fan_dark, mix(fan_dark, fan, 0.6))
            for side in (-0.28, 0.28):
                ex, ey = cx + math.cos(a + side) * r, base + math.sin(a + side) * r * 0.92
                line(mx, my, ex, ey, mix(fan_dark, fan, 0.6), fan)
                put(px, img, round(ex), round(ey), mix(fan, white, 0.5))
        put(px, img, cx, base, fan_dark)

    def table_coral(cx: int, h: int, w: int, seed: int):
        """Tischkoralle: dünner Fuß, oben eine flache, gewellte Platte."""
        for k in range(h):
            put(px, img, cx, base - k, mix(f1, OUTLINE, 0.35))
        for dx in range(-w, w + 1):
            y = base - h - (1 if abs(dx) < w - 1 and hash01(seed, dx) > 0.7 else 0)
            put(px, img, cx + dx, y, mix(f2, white, 0.25) if dx < 0 else f2)
            put(px, img, cx + dx, y + 1, mix(f1, OUTLINE, 0.25) if abs(dx) < w else OUTLINE)
        put(px, img, cx - w, base - h - 1, mix(f2, white, 0.5))

    def brain_coral(cx: int, w: int, h: int, seed: int):
        """Hirnkoralle: Kuppel mit gewundenen Furchen, oben links im Licht."""
        for dy in range(h + 1):
            for dx in range(-w, w + 1):
                e = (dx / (w + 0.5)) ** 2 + (dy / (h + 0.4)) ** 2
                if e > 1.0:
                    continue
                if e > 0.78:
                    col = mix(brain_dark, OUTLINE, 0.25)
                elif (dx + round(1.6 * math.sin(dy * 1.9 + seed))) % 3 == 0:
                    col = brain_dark
                else:
                    col = mix(brain, white, 0.35) if (dx < 0 and dy > h // 2) else brain
                put(px, img, cx + dx, base - dy, col)

    def sponge_tubes(cx: int, heights):
        """Röhrenschwämme: je zwei Pixel breit, oben eine dunkle Öffnung."""
        for i, h in enumerate(heights):
            x = cx + i * 3
            for k in range(h):
                t = k / max(1, h - 1)
                put(px, img, x, base - k, mix(sponge, (255, 236, 170, 255), 0.4 * t))
                put(px, img, x + 1, base - k, sponge_dark if k < h - 1 else sponge)
            put(px, img, x, base - h, mix(sponge, white, 0.4))
            put(px, img, x + 1, base - h, sponge)
            put(px, img, x, base - h + 1, OUTLINE)
            put(px, img, x + 1, base - h + 1, mix(sponge_dark, OUTLINE, 0.5))

    def anemone_tuft(cx: int, h: int, seed: int, pink: bool = False):
        """Anemone: kurzer Fuß, darüber ein Büschel geschwungener Tentakel mit leuchtenden Spitzen."""
        light = (255, 150, 200, 255) if pink else anemone
        dark = (120, 30, 80, 255) if pink else anemone_dark
        put(px, img, cx, base, dark)
        put(px, img, cx + 1, base, dark)
        put(px, img, cx, base - 1, mix(dark, light, 0.4))
        put(px, img, cx + 1, base - 1, mix(dark, light, 0.4))
        for i, lean in enumerate((-1.4, -0.8, -0.25, 0.25, 0.8, 1.4)):
            x = cx + 0.5 + lean * 0.5
            hh = h - abs(round(lean))
            for k in range(hh):
                x += lean * (0.18 + 0.12 * k) + 0.3 * math.sin(seed + k * 1.3 + i)
                put(px, img, round(x), base - 2 - k, mix(dark, light, 0.3 + 0.7 * k / max(1, hh - 1)))
            put(px, img, round(x), base - 2 - hh, f3)

    def seagrass(cx: int, h: int, seed: int):
        """Seegras: zwei, drei schmale Halme, die in der Strömung schwanken."""
        for b in range(2 + (1 if hash01(seed, 9) > 0.5 else 0)):
            x = cx + b * 2
            for k in range(h - b * 2):
                sway = round(1.2 * math.sin(k * 0.7 + seed + b))
                col = mix((40, 96, 40, 255), (150, 210, 90, 255), k / max(1, h - 1))
                put(px, img, x + sway, base - k, col)

    def scallop(cx: int):
        for dy in range(3):
            half = 2 if dy < 2 else 1
            for dx in range(-half, half + 1):
                col = mix(m2, coral_light, 0.4) if dx % 2 == 0 else mix(m1, coral, 0.35)
                put(px, img, cx + dx, base - dy, col)
        put(px, img, cx - 2, base - 2, OUTLINE if False else mix(m2, coral_light, 0.2))

    def starfish(cx: int, cy: int):
        for k in range(5):
            a = -math.pi / 2 + k * math.tau / 5
            for r in range(1, 3):
                put(px, img, round(cx + math.cos(a) * r), round(cy + math.sin(a) * r), star_col if r < 2 else coral)
        put(px, img, cx, cy, mix(star_col, (255, 230, 160, 255), 0.5))

    def fish(x: int, y: int, body, stripe, facing: int):
        """Ein kleiner Rifffisch (4 x 3 Pixel) mit Schwanzflosse."""
        for dx in range(4):
            for dy in (-1, 0, 1):
                if abs(dy) == 1 and dx in (0, 3):
                    continue
                col = stripe if dx == 2 else body
                put(px, img, x + dx * facing, y + dy, col)
        put(px, img, x + (3 if facing > 0 else -3), y - 1 if False else y, OUTLINE)        # Auge vorn
        put(px, img, x - facing, y - 1, body)
        put(px, img, x - facing, y + 1, body)

    # Fuß des Riffs: Geröll aus Kalk und Korallenbruch auf dem Rahmen (Lücken zwischen den Gruppen)
    def rubble(x0: int, x1: int):
        for x in range(x0, x1):
            col = mix(m1, m2, 0.25 + 0.5 * hash01(x, 3)) if hash01(x, 2) > 0.25 else mix(coral_dark, coral, 0.5)
            put(px, img, x, base, col)
            if hash01(x, 4) > 0.55:
                put(px, img, x, base - 1, mix(m1, m2, 0.5 * hash01(x, 6)))

    # das Riff: Gruppen von zwei, drei Korallen, nach rechts niedriger und lichter
    groups = []
    x = BAR_X + 1
    i = 0
    while x < BAR_X + BAR_W - 6:
        rel = (x - BAR_X) / BAR_W
        width = 13 + round(hash01(x, 1) * 6) - round(rel * 5)
        if any(abs(x + width / 2 - (BAR_X + round(BAR_W * f))) < width / 2 + 6 for f in GEMS):
            x += 4
            continue
        groups.append((x, width, rel, i))
        x += width + 2 + round(rel * 6)
        i += 1
    for gx, width, rel, i in groups:
        if rel < 0.86:
            rubble(gx, gx + width)
    for gx, width, rel, i in groups:
        scale = 1.0 - 0.55 * rel
        kind = i % 5
        seed = gx * 7 + i
        if rel >= 0.86:
            scallop(gx + 2)
            if i % 2 == 0:
                anemone_tuft(gx + 7, 4, seed, pink=True)
            continue
        if kind == 0:
            staghorn(gx + 4, base, -math.pi / 2 - 0.15, 4.2 * scale + 1.0, 2, seed)
            anemone_tuft(gx + 9, max(3, round(5 * scale)), seed)
        elif kind == 1:
            gorgonian(gx + 6, max(4, round(7 * scale)), seed)
            seagrass(gx + 12, max(4, round(8 * scale)), seed)
        elif kind == 2:
            brain_coral(gx + 4, 4 if scale > 0.7 else 3, 3, seed)
            sponge_tubes(gx + 9, [max(2, round(h * scale)) for h in (7, 9, 5)][: 3 if scale > 0.6 else 2])
        elif kind == 3:
            table_coral(gx + 5, max(3, round(6 * scale)), 5 if scale > 0.7 else 4, seed)
            staghorn(gx + 12, base, -math.pi / 2 + 0.2, 3.2 * scale + 1.0, 1, seed, (fan_dark, fan, (255, 190, 240, 255)))
        else:
            anemone_tuft(gx + 3, max(3, round(6 * scale)), seed, pink=True)
            seagrass(gx + 8, max(4, round(9 * scale)), seed)
            brain_coral(gx + 13, 2, 2, seed)
    starfish(BAR_X + BAR_W - 14, base - 2)
    # zwei kleine Fische: ein Anemonenfisch nahe dem Medaillon, ein blauer Doktorfisch weiter rechts
    fish(BAR_X + round(BAR_W * 0.42), top - 11, (255, 128, 30, 255), white, 1)
    fish(BAR_X + round(BAR_W * 0.74), top - 8, (60, 120, 230, 255), (255, 214, 60, 255), -1)
    # Seepocken und Rotalgen unter der Leiste, aufsteigende Blasen
    for x in range(BAR_X + 3, BAR_X + BAR_W - 3, 7):
        h = hash01(x, 19)
        if h > 0.62:
            for dx in range(-1, 2):
                put(px, img, x + dx, bottom + 1, mix(m2, m1, 0.2 + 0.25 * (dx + 1)))
            put(px, img, x, bottom + 2, mix(m1, m0, 0.3))
            put(px, img, x - 1, bottom + 2, mix(m2, m1, 0.5))
            put(px, img, x, bottom + 1, mix(m0, OUTLINE, 0.5))                 # Öffnung
        elif h > 0.4:
            length = 3 + round(hash01(x, 21) * 4)
            for k in range(length):
                sway = round(0.8 * math.sin(k * 0.9 + x))
                put(px, img, x + sway, bottom + 1 + k, mix(coral_dark, coral, k / max(1, length - 1)))
    for x in range(BAR_X + 9, BAR_X + BAR_W, 14):
        y = bottom + 6 + round(hash01(x, 23) * 6)
        if hash01(x, 29) > 0.5:
            for dx, dy in ((0, -1), (-1, 0), (1, 0), (0, 1)):
                put(px, img, x + dx, y + dy, f2)
            put(px, img, x - 1, y - 1, f3)
            put(px, img, x + 2, y - 4, f2)
        else:
            put(px, img, x, y, f2)
            put(px, img, x + 1, y - 2, f3)
    # Kranz aus Korallenästen um das Medaillon, darüber ein Goldreif
    for k, ang in enumerate((-158, -136, -112, -68, -44, -22)):
        a = math.radians(ang)
        bx = EMB_X + math.cos(a) * (EMB_R - 0.5)
        by = EMB_Y + math.sin(a) * (EMB_R - 0.5)
        staghorn(bx, by, a, 3.6, 1, 50 + k)
    for ang in range(-178, -1, 3):
        a = math.radians(ang)
        put(px, img, round(EMB_X + math.cos(a) * (EMB_R + 0.3)), round(EMB_Y + math.sin(a) * (EMB_R + 0.3)),
            gold if ang < -90 else gold_dark)
    # Spitze: die geöffnete Schere des Karkinos
    claw = ["OO.....",
            "LLOO...",
            "HLLLOO.",
            "LLLLLLO",
            "CCCCCLO",
            "CCOOOWO",
            "CO.....",
            "CCOOWO.",
            "DCCCCO.",
            "DDDDO..",
            "OOOO..."]
    cols = {"O": OUTLINE, "L": coral_light, "H": white, "C": coral, "D": coral_dark, "W": (60, 30, 28, 255)}
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2 - 6
    for dy, row in enumerate(claw):
        for dx, ch in enumerate(row):
            if ch != ".":
                put(px, img, ex + dx, ey + dy, cols[ch])


def deco_moth(img: Image.Image, pal: dict) -> None:
    """Hinter dem Medaillon breitet die Mottenkönigin ihre Flügel aus (Vorderflügel nach oben, Hinterflügel mit langen
    Schwänzen nach unten, mit Augenflecken); oben auf dem Rahmen sitzen gefiederte Fühler und kleine Mondsicheln,
    unter der Leiste hängen Seidenfäden mit Tautropfen; zwei kleine Falter schwirren über der Leiste. Als Spitze eine
    liegende Mondsichel mit Stern."""
    px = img.load()
    f2, f3 = c(pal, "f2"), c(pal, "f3")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    wing, wing_dark, wing_light = c(pal, "wing"), c(pal, "wing_dark"), c(pal, "wing_light")
    spot, spot_dark = c(pal, "spot"), c(pal, "spot_dark")
    silk, silk_dark = c(pal, "silk"), c(pal, "silk_dark")
    m1 = c(pal, "m1")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def wing_shape(cx: float, cy: float, rx: float, ry: float, ang: float, tail: float):
        """Ein Flügel als gedrehte Ellipse, der Rand hell, innen Adern, ein Augenfleck; optional ein Schwanz."""
        ca, sa = math.cos(ang), math.sin(ang)
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                u = (dx * ca + dy * sa) / rx
                v = (-dx * sa + dy * ca) / ry
                d = u * u + v * v
                if d > 1.0 or u < 0.0:
                    continue
                col = mix(wing_dark, wing, 0.35 + 0.65 * u)
                if d > 0.72:
                    col = wing_light if d < 0.86 else mix(wing_dark, OUTLINE, 0.3)
                elif abs(math.sin(math.atan2(v, u) * 5.0)) < 0.12:
                    col = mix(wing_dark, wing, 0.3)
                es = (u - 0.62) ** 2 + (v * 1.2) ** 2
                if es < 0.012:
                    col = f3
                elif es < 0.03:
                    col = spot
                elif es < 0.05:
                    col = gold
                elif es < 0.07:
                    col = spot_dark
                put(px, img, x, y, col)
        if tail > 0:
            tx, ty = cx + ca * rx * 0.9, cy + sa * rx * 0.9
            for k in range(int(tail)):
                t = k / max(1, tail - 1)
                put(px, img, round(tx + ca * k * 0.8 + t * t * 3.0), round(ty + sa * k * 0.8), wing if k < tail - 1 else wing_light)

    # Flügel, die hinter dem Medaillon hervorkommen: oben der Vorderflügel, unten der Hinterflügel mit Schwanz
    wing_shape(EMB_X + 13, EMB_Y - 8, 27, 8.5, math.radians(-17), 0)
    wing_shape(EMB_X + 12, EMB_Y + 8, 19, 6.0, math.radians(20), 8)
    # gefiederte Fühler über dem Medaillon
    for side in (-1, 1):
        x0, y0 = EMB_X + side * 4, EMB_Y - EMB_R + 1
        for k in range(9):
            x = round(x0 + side * k * 0.6)
            y = y0 - k
            put(px, img, x, y, m1 if k < 8 else gold)
            if 1 < k < 8 and k % 2 == 0:
                put(px, img, x - side, y, mix(m1, silk, 0.4))
                put(px, img, x + side, y + 1, mix(m1, silk, 0.25))
    # kleine Mondsicheln auf dem Rahmen (nach rechts offen), daneben ein Stern
    crescent = (".gg.", "g..o", "g...", "g..o", ".gg.")
    for x0 in range(BAR_X + 34, BAR_X + BAR_W - 10, 38):
        for dy, row in enumerate(crescent):
            for dx, ch in enumerate(row):
                if ch == "g":
                    put(px, img, x0 + dx, top - 7 + dy, gold if dy < 3 else gold_dark)
                elif ch == "o":
                    put(px, img, x0 + dx, top - 7 + dy, mix(gold, f3, 0.5))
        put(px, img, x0 + 6, top - 8, f3)
    # Seidenfäden mit Tautropfen unter der Leiste
    for x in range(BAR_X + 11, BAR_X + BAR_W - 4, 17):
        length = 3 + round(hash01(x, 41) * 7)
        for k in range(length):
            put(px, img, x, bottom + 1 + k, silk if k % 4 else silk_dark)
        put(px, img, x, bottom + 1 + length, f3)
        put(px, img, x, bottom + 2 + length, f2)
    # zwei kleine Falter über der Leiste
    for (mx, my) in ((BAR_X + 70, top - 9), (BAR_X + 140, top - 7)):
        for dx, dy, col in ((-2, -1, wing), (-1, -1, wing_light), (-1, 0, wing), (0, 0, silk_dark), (1, 0, wing), (1, -1, wing_light),
                            (2, -1, wing), (-1, 1, wing_dark), (1, 1, wing_dark), (0, -1, silk_dark)):
            put(px, img, mx + dx, my + dy, col)
    # Spitze: eine liegende Mondsichel mit Stern
    ex, ey = BAR_X + BAR_W + 2, BAR_Y + BAR_H // 2
    for k in range(9):
        a = math.radians(-120 + k * 30)
        put(px, img, round(ex + 3 + math.cos(a) * 4), round(ey + math.sin(a) * 4), gold if 1 <= k <= 7 else gold_dark)
        put(px, img, round(ex + 3 + math.cos(a) * 3), round(ey + math.sin(a) * 3), mix(gold, f3, 0.4) if 2 <= k <= 6 else gold_dark)
    put(px, img, ex + 7, ey - 1, f3)
    put(px, img, ex + 7, ey, f2)


def deco_forge(img: Image.Image, pal: dict) -> None:
    """Hinter dem Medaillon gekreuzt: der Schmiedehammer (Kopf oben links) und der Glutspeer (glühende Klinge oben
    rechts); oben auf dem Rahmen kleine Glutzungen mit aufstiebenden Funken, unter der Leiste tropft geschmolzenes Metall
    (die Tropfen glühen); als Spitze die Klinge des Glutspeers."""
    px = img.load()
    f1, f2, f3 = c(pal, "f1"), c(pal, "f2"), c(pal, "f3")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    iron, iron_light = c(pal, "iron"), c(pal, "iron_light")
    ember, ember_dark, wood = c(pal, "ember"), c(pal, "ember_dark"), c(pal, "wood")
    m1 = c(pal, "m1")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def stick(x0: float, y0: float, x1: float, y1: float, width: float, col_fn):
        length = math.hypot(x1 - x0, y1 - y0)
        ax, ay = (x1 - x0) / length, (y1 - y0) / length
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                dx, dy = x + 0.5 - x0, y + 0.5 - y0
                t = dx * ax + dy * ay
                off = -dx * ay + dy * ax
                if 0 <= t <= length:
                    w = width(t / length) if callable(width) else width
                    if abs(off) <= w:
                        put(px, img, x, y, col_fn(t / length, off, w))

    # der Speer: von unten links nach oben rechts, die Klinge glüht
    def spear_col(t, off, w):
        if t < 0.72:
            return OUTLINE if abs(off) > w - 0.6 else (wood if off < 0 else mix(wood, OUTLINE, 0.4))
        if t < 0.76:
            return gold if off < 0 else gold_dark
        if abs(off) > w - 0.6:
            return ember_dark
        return f3 if abs(off) < 0.6 else (f2 if off < 0 else ember)
    stick(EMB_X - 16, EMB_Y + 22, EMB_X + 40, EMB_Y - 21, lambda u: 1.1 if u < 0.72 else (2.6 if u < 0.76 else 2.6 * (1 - (u - 0.76) / 0.24) ** 0.8 + 0.3),
          spear_col)
    # der Hammer: von unten rechts nach oben links, oben der Kopf quer
    def handle_col(t, off, w):
        return OUTLINE if abs(off) > w - 0.6 else (mix(wood, iron_light, 0.15) if off < 0 else wood)
    stick(EMB_X + 17, EMB_Y + 21, EMB_X - 13, EMB_Y - 17, 1.1, handle_col)
    hx, hy = EMB_X - 13, EMB_Y - 17
    for y in range(-6, 7):
        for x in range(-6, 7):
            u = (x - y) / 1.414
            v = (x + y) / 1.414
            if abs(u) <= 5.2 and abs(v) <= 2.6:
                edge = abs(u) > 4.4 or abs(v) > 1.9
                face = abs(u) > 4.0
                col = OUTLINE if edge and not face else (ember if face else (iron_light if v < -0.6 else iron))
                put(px, img, hx + x, hy + y, col)
    # Glutzungen und Funken oben auf dem Rahmen
    for x in range(BAR_X + 6, BAR_X + BAR_W - 2, 11):
        hgt = 2 + round(hash01(x, 13) * 3)
        for k in range(hgt):
            t = k / max(1, hgt - 1)
            col = f1 if t < 0.4 else (f2 if t < 0.8 else f3)
            put(px, img, x, top - 1 - k, col)
            if k == 0:
                put(px, img, x - 1, top - 1, ember_dark)
                put(px, img, x + 1, top - 1, ember_dark)
        if hash01(x, 17) > 0.45:
            put(px, img, x + round(hash01(x, 19) * 4) - 2, top - hgt - 2 - round(hash01(x, 23) * 3), f2 if hash01(x, 29) > 0.5 else f3)
    # geschmolzenes Metall tropft von der Leiste
    for x in range(BAR_X + 9, BAR_X + BAR_W - 4, 13):
        length = 2 + round(hash01(x, 31) * 4)
        for k in range(length):
            put(px, img, x, bottom + 1 + k, ember_dark if k < length - 2 else ember)
        put(px, img, x, bottom + 1 + length, f2)
        put(px, img, x, bottom + 2 + length, f3)
        put(px, img, x + 1, bottom + 1, ember_dark)
    # Spitze: die Klinge des Glutspeers nach rechts
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(12):
        half = round(4.2 * (1 - k / 12) ** 0.75) if k > 1 else 2
        for dy in range(-half, half + 1):
            if abs(dy) == half:
                col = OUTLINE if k > 1 else gold_dark
            elif k <= 1:
                col = gold
            else:
                col = f3 if dy == 0 else (f2 if dy < 0 else ember)
            put(px, img, ex + k, ey + dy, col)
    put(px, img, ex + 12, ey, ember_dark)
    _ = m1


def deco_star(img: Image.Image, pal: dict) -> None:
    """Hinter dem Medaillon breitet der Sternenrochen seine Schwingen aus (Nachthimmel mit Sternen, violette Vorderkante,
    nach außen spitz zulaufend); schräg dahinter die Sternenlanze mit leuchtender Kristallspitze. Oben auf dem Rahmen
    funkeln kleine vierzackige Sterne, unter der Leiste fallen Sternschnuppen; als Spitze ein Sternkristall."""
    px = img.load()
    f2, f3 = c(pal, "f2"), c(pal, "f3")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    night, night_dark = c(pal, "night"), c(pal, "night_dark")
    nebula, nebula_light = c(pal, "nebula"), c(pal, "nebula_light")
    silver, silver_dark, crystal = c(pal, "silver"), c(pal, "silver_dark"), c(pal, "crystal")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def wing(cx: float, cy: float, length: float, lead: float, trail: float, ang: float):
        """Ein Rochenflügel ab (cx, cy) nach rechts: vorn die Vorderkante (Neigung lead), hinten geschwungen zur Spitze."""
        ca, sa = math.cos(ang), math.sin(ang)
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                u = dx * ca + dy * sa
                v = -dx * sa + dy * ca
                if u < 0.0 or u > length:
                    continue
                k = u / length
                v_lead = -lead * (1.0 - k)
                v_trail = trail * (1.0 - k) ** 1.4 - 1.2 * math.sin(math.pi * k)
                if v < v_lead or v > v_trail:
                    continue
                edge = v < v_lead + 1.0
                rim = v > v_trail - 0.9 or u > length - 1.2
                col = mix(night_dark, night, 0.4 + 0.6 * (1.0 - k))
                if hash01(x, y, 131) > 0.93:
                    col = f3 if hash01(x, y, 137) > 0.5 else crystal
                if edge:
                    col = nebula_light if v < v_lead + 0.5 else nebula
                elif rim:
                    col = mix(nebula, OUTLINE, 0.35)
                put(px, img, x, y, col)

    wing(EMB_X + 10, EMB_Y - 9, 34, 6.0, 9.0, math.radians(-24))
    # die Lanze: von unten links nach oben rechts hinter dem Medaillon, die Kristallspitze leuchtet
    x0, y0, x1, y1 = EMB_X - 17, EMB_Y + 20, EMB_X + 30, EMB_Y - 21
    length = math.hypot(x1 - x0, y1 - y0)
    ax, ay = (x1 - x0) / length, (y1 - y0) / length
    for y in range(FRAME_H):
        for x in range(FRAME_W):
            dx, dy = x + 0.5 - x0, y + 0.5 - y0
            t = (dx * ax + dy * ay) / length
            off = -dx * ay + dy * ax
            if not 0.0 <= t <= 1.0:
                continue
            if t < 0.8:
                w = 1.1
                if abs(off) > w:
                    continue
                col = OUTLINE if abs(off) > w - 0.55 else ((gold if off < 0 else gold_dark) if (t * 26) % 1.0 < 0.22 else
                                                          (silver if off < 0 else silver_dark))
            elif t < 0.84:
                w = 2.7
                if abs(off) > w:
                    continue
                col = OUTLINE if abs(off) > w - 0.6 else (gold if off < 0 else gold_dark)
            else:
                u = (t - 0.84) / 0.16
                w = 2.4 * max(0.0, 1.0 - u) ** 0.8 + 0.3
                if abs(off) > w:
                    continue
                col = mix(crystal, OUTLINE, 0.4) if abs(off) > w - 0.6 else (f3 if abs(off) < 0.7 else crystal)
            put(px, img, x, y, col)

    def star(sx: int, sy: int, r: int, col, core):
        for k in range(-r, r + 1):
            shade = col if abs(k) < r else mix(col, OUTLINE, 0.3)
            put(px, img, sx + k, sy, shade)
            put(px, img, sx, sy + k, shade)
        put(px, img, sx, sy, core)

    # kleine Sterne oben auf dem Rahmen
    for x in range(BAR_X + 14, BAR_X + BAR_W - 6, 27):
        big = hash01(x, 151) > 0.5
        star(x, top - 4 - round(hash01(x, 157) * 2), 2 if big else 1, crystal, f3)
    # Sternschnuppen unter der Leiste (schräg fallend, mit Schweif)
    for x in range(BAR_X + 18, BAR_X + BAR_W - 6, 31):
        length = 4 + round(hash01(x, 163) * 4)
        for k in range(length):
            t = k / max(1, length - 1)
            put(px, img, x - k, bottom + 2 + k // 2, mix(f2, nebula, t * 0.6) if k else f3)
        put(px, img, x + 1, bottom + 2, crystal)
    # Spitze: ein Sternkristall nach rechts
    ex, ey = BAR_X + BAR_W + 3, BAR_Y + BAR_H // 2
    for k in range(11):
        half = round(3.6 * (1 - k / 11) ** 0.75) if k > 1 else 2
        for dy in range(-half, half + 1):
            if abs(dy) == half:
                col = OUTLINE if k > 1 else gold_dark
            elif k <= 1:
                col = gold
            else:
                col = f3 if dy == 0 else (crystal if dy < 0 else f2)
            put(px, img, ex + k, ey + dy, col)
    for dy in (-5, -4, 4, 5):
        put(px, img, ex + 4, ey + dy, crystal if abs(dy) == 4 else OUTLINE)
    put(px, img, ex + 11, ey, f2)


def deco_mystic(img: Image.Image, pal: dict) -> None:
    """Hinter dem Medaillon ein Fächer aus fünf Zauberkarten (weiß mit Goldrand, rote und schwarze Farben), schräg dahinter
    ihr Zauberstab (schwarz mit weißen Spitzen); oben auf dem Rahmen die vier Kartenfarben, unter der Leiste rieselt
    Konfetti. Als Spitze die weiße Spitze des Zauberstabs mit einem Funkelstern."""
    px = img.load()
    f2, f3 = c(pal, "f2"), c(pal, "f3")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    card, card_dark = c(pal, "card"), c(pal, "card_dark")
    red, black, wand = c(pal, "red"), c(pal, "black"), c(pal, "wand")
    confetti = [c(pal, "acc"), gold, c(pal, "confetti"), f2, card]
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2
    pips = {"heart": (".#.#.", "#####", "#####", ".###.", "..#.."),
            "spade": ("..#..", ".###.", "#####", "#####", "..#.."),
            "diamond": ("..#..", ".###.", "#####", ".###.", "..#.."),
            "club": (".###.", ".###.", "#####", "#.#.#", "..#..")}

    # der Zauberstab: von unten links nach oben rechts hinter dem Medaillon
    x0, y0, x1, y1 = EMB_X - 15, EMB_Y + 19, EMB_X + 34, EMB_Y - 20
    length = math.hypot(x1 - x0, y1 - y0)
    for k in range(int(length) + 1):
        t = k / length
        x = x0 + (x1 - x0) * t
        y = y0 + (y1 - y0) * t
        tip = t < 0.08 or t > 0.9
        for o in (-1, 0, 1):
            col = OUTLINE if o != 0 else (card if tip else wand)
            put(px, img, round(x + o * 0.7), round(y + o * 0.7), col)

    def card_at(cx: float, cy: float, ang: float, suit: str):
        """Eine kleine Karte (8 x 11) um (cx, cy) gedreht: Goldrand, weiße Fläche, die Farbe in der Mitte – nie über der
        Leiste (die bleibt frei)."""
        ca, sa = math.cos(ang), math.sin(ang)
        w, h = 4.0, 5.6
        red_suit = suit in ("heart", "diamond")
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                if x >= BAR_X - 3 and y >= BAR_Y - 3:
                    continue
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                u = dx * ca + dy * sa
                v = -dx * sa + dy * ca
                if abs(u) > w or abs(v) > h:
                    continue
                if abs(u) > w - 0.9 or abs(v) > h - 0.9:
                    col = OUTLINE
                elif abs(u) > w - 1.8 or abs(v) > h - 1.8:
                    col = gold if (u + v) < 0 else gold_dark
                else:
                    col = mix(card, card_dark, 0.1 + 0.25 * (v + h) / (2 * h))
                    gx, gy = int(math.floor(u + 1.5)), int(math.floor(v + 1.5))
                    small_pip = {"heart": ("#.#", "###", ".#."), "spade": (".#.", "###", "#.#"),
                                 "diamond": (".#.", "###", ".#."), "club": (".#.", "###", ".#.")}[suit]
                    if 0 <= gx < 3 and 0 <= gy < 3 and small_pip[gy][gx] == "#":
                        col = red if red_suit else black
                put(px, img, x, y, col)

    # ein Fächer aus vier Karten steigt hinter dem Medaillon nach rechts oben auf
    for i, suit in enumerate(("club", "diamond", "spade", "heart")):
        ang = math.radians(-35 + i * 20)
        card_at(EMB_X + 17 + i * 6.0, EMB_Y - 12 + abs(i - 1.5) * 1.2, ang, suit)

    # die vier Kartenfarben auf dem Rahmen
    small = {"heart": ("#.#", "###", ".#."), "spade": (".#.", "###", "#.#"), "diamond": (".#.", "###", ".#."),
             "club": (".#.", "###", ".#.")}
    order = ("spade", "heart", "club", "diamond")
    for k, x in enumerate(range(BAR_X + 12, BAR_X + BAR_W - 6, 22)):
        suit = order[k % 4]
        col = red if suit in ("heart", "diamond") else card
        for gy, row in enumerate(small[suit]):
            for gx, ch in enumerate(row):
                if ch == "#":
                    put(px, img, x + gx - 1, top - 3 + gy, col)
        for gx, gy in ((-2, -1), (2, -1), (-2, 3), (2, 3)):
            put(px, img, x + gx, top - 3 + gy, gold_dark)
    # Konfetti unter der Leiste
    for x in range(BAR_X + 4, BAR_X + BAR_W - 4):
        h = hash01(x, 977)
        if h > 0.82:
            y = bottom + 1 + int(hash01(x, 979) * 5)
            put(px, img, x, y, confetti[int(hash01(x, 983) * len(confetti)) % len(confetti)])
            if h > 0.93:
                put(px, img, x + 1, y, confetti[int(hash01(x, 991) * len(confetti)) % len(confetti)])
    # Spitze: die weiße Spitze des Zauberstabs mit Funkelstern
    ex, ey = BAR_X + BAR_W + 2, BAR_Y + BAR_H // 2
    for k in range(9):
        for dy in (-1, 0, 1):
            col = OUTLINE if abs(dy) == 1 else (card if k > 4 else wand)
            put(px, img, ex + k, ey + dy, col)
    sx, sy = ex + 11, ey
    for d in range(-3, 4):
        put(px, img, sx + d, sy, f3 if abs(d) < 2 else gold)
        put(px, img, sx, sy + d, f3 if abs(d) < 2 else gold)
    put(px, img, sx + 1, sy + 1, gold)
    put(px, img, sx - 1, sy - 1, gold)
    put(px, img, sx + 1, sy - 1, gold)
    put(px, img, sx - 1, sy + 1, gold)


def deco_anni(img: Image.Image, pal: dict) -> None:
    """Hinter dem Medaillon schräg der Bannhammer (Baumstamm-Stiel, Steinklotz, dessen vordere Hälfte fehlende Textur ist);
    um den Rahmen schweben lose Pixel in Rot, Grün, Blau, Magenta und Cyan, der Rahmen hat Farbsäume (oben magenta, unten
    cyan), und an der Spitze sitzt ein gelbes Warndreieck."""
    px = img.load()
    acc, cyan = c(pal, "acc"), c(pal, "cyan")
    wood, wood_dark = c(pal, "wood"), c(pal, "wood_dark")
    stone, stone_dark, iron = c(pal, "stone"), c(pal, "stone_dark"), c(pal, "iron")
    pixels = [c(pal, k) for k in ("red", "green", "blue", "acc", "cyan", "yellow")]
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    # der Stiel: von unten links nach oben rechts hinter dem Medaillon, der Kopf rechts oben über dem Rahmen
    x0, y0, x1, y1 = EMB_X - 16, EMB_Y + 18, EMB_X + 27, EMB_Y - 14
    length = math.hypot(x1 - x0, y1 - y0)
    for k in range(int(length) + 1):
        t = k / length
        x = x0 + (x1 - x0) * t
        y = y0 + (y1 - y0) * t
        for o in (-1, 0, 1):
            col = OUTLINE if o != 0 else (wood if (k // 3) % 2 else wood_dark)
            put(px, img, round(x + o * 0.7), round(y + o * 0.7), col)
    # der Kopf: ein schräger Klotz um das obere Ende, die vordere Hälfte fehlende Textur
    hx, hy = x1 + 2, y1 + 1
    ang = math.atan2(y1 - y0, x1 - x0)
    ca, sa = math.cos(ang), math.sin(ang)
    for y in range(FRAME_H):
        for x in range(FRAME_W):
            if x >= BAR_X - 3 and y >= BAR_Y - 3:
                continue
            dx, dy = x + 0.5 - hx, y + 0.5 - hy
            u = dx * ca + dy * sa            # entlang des Stiels
            v = -dx * sa + dy * ca           # quer
            if abs(u) > 4.2 or abs(v) > 7.2:
                continue
            if abs(u) > 3.4 or abs(v) > 6.4:
                col = OUTLINE
            elif v < 0:
                col = acc if (int(math.floor(u + 8)) // 2 + int(math.floor(v + 8)) // 2) % 2 == 0 else (8, 0, 10, 255)
            else:
                col = mix(stone, stone_dark, 0.2 + 0.5 * hash01(x, y, 5)) if abs(v - 2.5) > 0.7 else iron
            put(px, img, x, y, col)
    # Farbsäume am Rahmen: oben magenta, unten cyan (verrutscht)
    for x in range(BAR_X - 2, BAR_X + BAR_W + 2):
        if hash01(x // 6, 81) > 0.45:
            put(px, img, x, top - 1, acc)
        if hash01(x // 5, 83) > 0.5:
            put(px, img, x + 1, bottom + 1, cyan)
    # lose Pixel um den Rahmen (je 2 x 2)
    for i in range(26):
        x = BAR_X + int(hash01(i, 91) * (BAR_W - 4))
        above = hash01(i, 93) > 0.5
        y = top - 4 - int(hash01(i, 95) * 6) if above else bottom + 2 + int(hash01(i, 97) * 4)
        col = pixels[int(hash01(i, 99) * len(pixels)) % len(pixels)]
        for gx in range(2):
            for gy in range(2):
                if 0 <= y + gy < FRAME_H:
                    put(px, img, x + gx, y + gy, col)
    # Spitze: ein gelbes Warndreieck mit schwarzem Ausrufezeichen (am rechten Ende des Rahmens)
    yellow = c(pal, "yellow")
    ex, ey = 234, BAR_Y + BAR_H // 2
    for y in range(-6, 6):
        half = (y + 6) * 0.45
        for x in range(-6, 7):
            if abs(x) > half + 0.3:
                continue
            edge = abs(x) > half - 1.0 or y == 5
            col = OUTLINE if edge else yellow
            if x == 0 and (-2 <= y <= 1 or y == 3):
                col = OUTLINE
            put(px, img, ex + x, ey + y, col)


def deco_schrolli(img: Image.Image, pal: dict) -> None:
    """Hinter dem Medaillon zwei Schwingen (eine nach oben rechts erhoben, eine nach unten rechts gesenkt), jede aus fünf
    gefächerten Federn mit Schaft und Spitze; oben auf dem Rahmen ein Kranz aus Strahlen mit Funkeln dazwischen, unter der
    Leiste goldene Tropfen; als Spitze der achtstrahlige Zepterstern um einen Himmelsstein. In der Wahren Gestalt sind die
    Federn ein Sternenhimmel mit schillernden Spitzen, die Strahlen Sterne und die Tropfen Sternenstaub."""
    px = img.load()
    truly = "prism1" in pal
    f3 = c(pal, "f3")
    gold, gold_dark = c(pal, "gold"), c(pal, "gold_dark")
    feather, feather_mid, feather_dark = c(pal, "feather"), c(pal, "feather_mid"), c(pal, "feather_dark")
    tip, ray = c(pal, "tip"), c(pal, "ray")
    gem_blue, gem_light = c(pal, "gem_blue"), c(pal, "gem_light")
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    def quill(x0: float, y0: float, ang: float, length: float, width: float, seed: int):
        """Eine Feder ab (x0, y0) in Richtung ang: spitz zulaufend, mit Schaft, Fahne und farbiger Spitze."""
        ca, sa = math.cos(ang), math.sin(ang)
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                dx, dy = x + 0.5 - x0, y + 0.5 - y0
                u = dx * ca + dy * sa
                v = -dx * sa + dy * ca
                if u < 0.0 or u > length:
                    continue
                k = u / length
                half = width * (0.55 + 0.45 * math.sin(math.pi * min(1.0, k * 1.15))) * (1.0 - max(0.0, k - 0.82) * 3.2)
                if half <= 0.2 or abs(v) > half:
                    continue
                edge = abs(v) > half - 0.75
                if edge:
                    col = OUTLINE
                elif abs(v) < 0.45 and k < 0.9:
                    col = mix(feather_mid, feather_dark, 0.35)                       # Schaft
                else:
                    lit = 0.35 + 0.65 * (1.0 - (v + half) / (2 * half))          # Licht von oben
                    col = mix(feather_mid, feather, lit)
                    if truly and hash01(x, y, seed, 211) > 0.9:
                        col = f3 if hash01(x, y, seed, 213) > 0.5 else c(pal, "prism3")
                    if k > 0.68:
                        t = (k - 0.68) / 0.32
                        tipcol = tip
                        if truly:
                            tipcol = mix(c(pal, "prism1"), c(pal, "prism3"), hash01(seed, 217)) if t > 0.5 else c(pal, "prism2")
                        col = mix(col, tipcol, min(1.0, t * 1.6))
                    elif not truly and (u * 1.3 + seed) % 5.0 < 0.6:
                        col = mix(col, feather_mid, 0.6)                             # Rippen der Fahne
                put(px, img, x, y, col)

    # die erhobene Schwinge: fünf Federn, nach außen länger und flacher
    for j in range(5):
        k = j / 4
        quill(EMB_X + 12 + 3 * j, EMB_Y - 9 - j * 0.6, math.radians(-46 + 22 * k), 17 + 13 * k, 3.1 - 0.3 * k, j)
    # die gesenkte Schwinge: vier Federn
    for j in range(4):
        k = j / 3
        quill(EMB_X + 12 + 3 * j, EMB_Y + 8 + j * 0.5, math.radians(38 - 20 * k), 14 + 9 * k, 2.8 - 0.3 * k, 20 + j)

    def spark(sx: int, sy: int, r: int, col, core):
        for k in range(-r, r + 1):
            shade = col if abs(k) < r else mix(col, OUTLINE, 0.25)
            put(px, img, sx + k, sy, shade)
            put(px, img, sx, sy + k, shade)
        put(px, img, sx, sy, core)

    # der Kranz aus Strahlen auf dem Rahmen (nicht über den Fassungen der Edelsteine)
    gems = [BAR_X + round(BAR_W * f) for f in GEMS]
    for i, x in enumerate(range(BAR_X + 10, BAR_X + BAR_W - 4, 9)):
        if any(abs(x - g) < 9 for g in gems):
            continue
        h = 3 + (2 if i % 2 == 0 else 0) + (1 if i % 4 == 0 else 0)
        if truly:
            spark(x, top - h, 1 if i % 2 else 2, ray, f3)
            continue
        for dy in range(h):
            half = 1 if dy < h - 2 else 0
            for dx in range(-half, half + 1):
                col = OUTLINE if abs(dx) == half and half > 0 else (ray if dx <= 0 else gold)
                put(px, img, x + dx, top - 1 - dy, col)
        put(px, img, x, top - h, f3)
        if i % 3 == 1:
            spark(x + 4, top - 6, 1, ray, f3)
    # unter der Leiste: goldene Tropfen (in der Wahren Gestalt Sternenstaub)
    for i, x in enumerate(range(BAR_X + 6, BAR_X + BAR_W - 2, 12)):
        length = 2 + (i % 3)
        for dy in range(length):
            col = gold if dy < length - 1 else (f3 if not truly else ray)
            if truly:
                col = mix(ray, f3, dy / max(1, length - 1)) if hash01(x, dy, 223) > 0.3 else c(pal, "prism2")
            put(px, img, x, bottom + 1 + dy, col)
            if not truly:
                put(px, img, x + 1, bottom + 1 + dy, gold_dark if dy < length - 1 else OUTLINE)
        if truly and i % 2 == 0:
            spark(x + 6, bottom + 4, 1, ray, f3)
    # Spitze: der achtstrahlige Zepterstern um einen Himmelsstein
    ex, ey = BAR_X + BAR_W + 4, BAR_Y + BAR_H // 2
    for ang in range(0, 360, 45):
        r = 4 if ang % 90 == 0 else 3
        ca, sa = math.cos(math.radians(ang)), math.sin(math.radians(ang))
        for k in range(1, r + 1):
            put(px, img, round(ex + ca * k), round(ey + sa * k), gold if k < r else ray)
    for dx in (-1, 0, 1):
        for dy in (-1, 0, 1):
            put(px, img, ex + dx, ey + dy, gem_light if dx == -1 and dy == -1 else gem_blue)
    put(px, img, ex, ey, gem_light)


def post_schrolli(img: Image.Image, pal: dict) -> None:
    """Nach dem Medaillon: in der Wahren Gestalt sieben Augen auf seinem Ring (wie auf den Rädern im Rad)."""
    if "prism1" not in pal:
        return
    px = img.load()
    for k in range(7):
        a = math.radians(-150 + k * 50)
        x = round(EMB_X - 0.5 + math.cos(a) * (EMB_R - 2.5))
        y = round(EMB_Y - 0.5 + math.sin(a) * (EMB_R - 2.5))
        put(px, img, x - 1, y, c(pal, "f3"))
        put(px, img, x + 1, y, c(pal, "f3"))
        put(px, img, x, y - 1, c(pal, "gold"))
        put(px, img, x, y + 1, c(pal, "gold_dark"))
        put(px, img, x, y, c(pal, "tip"))


# =============================================================================================
# Schrolli der Allmächtige (dritte Gestalt) und der Weltenfresser
# =============================================================================================

def _ramp(cols, t: float):
    """Verlauf über mehrere Stützfarben (t von 0 bis 1)."""
    t = max(0.0, min(1.0, t)) * (len(cols) - 1)
    i = min(len(cols) - 2, int(t))
    return mix(cols[i], cols[i + 1], t - i)


def _outline(layer: Image.Image, col) -> None:
    """Freie Pixel, die (4er-Nachbarschaft) an die Form auf der Ebene grenzen, bekommen die Kontur."""
    src = layer.load()
    w, h = layer.size
    edge = []
    for y in range(h):
        for x in range(w):
            if src[x, y][3]:
                continue
            for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
                if 0 <= nx < w and 0 <= ny < h and src[nx, ny][3]:
                    edge.append((x, y))
                    break
    for x, y in edge:
        src[x, y] = col


def _worley(x: float, y: float, cell: float, period: int, seed: int) -> tuple[float, float]:
    """Abstand zum nächsten und zweitnächsten Zellpunkt; waagrecht nahtlos (period Zellen × cell = 256)."""
    cx, cy = int(math.floor(x / cell)), int(math.floor(y / cell))
    d1 = d2 = 99.0
    for gy in range(cy - 1, cy + 2):
        for gx in range(cx - 1, cx + 2):
            wx = gx % period
            fx = (gx + 0.15 + 0.7 * hash01(wx, gy, seed)) * cell
            fy = (gy + 0.15 + 0.7 * hash01(wx, gy, seed + 1)) * cell
            d = math.hypot(x + 0.5 - fx, y + 0.5 - fy)
            if d < d1:
                d1, d2 = d, d1
            elif d < d2:
                d2 = d
    return d1, d2


def _gem_sockets(px, img: Image.Image, pal: dict, fill0, fill1) -> None:
    """Fassungen der Phasen-Edelsteine über dem Rahmen (wie in frame_border)."""
    y0 = BAR_Y - 3
    for f in GEMS:
        gx = BAR_X + round(BAR_W * f)
        for dy in range(-6, 1):
            half = 5 - abs(dy + 3) + 1
            for dx in range(-half, half + 1):
                x, y = gx + dx, y0 + dy
                edge = abs(dx) == half or dy in (-6, 0)
                put(px, img, x, y, OUTLINE if edge else (fill0 if dx < 0 else fill1))


def deco_almighty(img: Image.Image, pal: dict) -> None:
    """Schrolli der Allmächtige: hinter dem Medaillon die beiden Schwingen wie in den ersten Gestalten, doch die Spitzen der
    Federn stehen in Flammen (sie züngeln und teilen sich); auf dem Rahmen eine Sonnenkrone – Flammenzungen im Wechsel mit
    goldenen Strahlen –, unter der Leiste tropft flüssiges Gold, dazwischen fallen Funken; als Spitze die Sonne selbst."""
    px = img.load()
    fire = [c(pal, f"flame{k}") for k in range(6)]
    feather, feather_mid, feather_dark = c(pal, "feather"), c(pal, "feather_mid"), c(pal, "feather_dark")
    gold, gold_dark, ray = c(pal, "gold"), c(pal, "gold_dark"), c(pal, "ray")
    sun, sun_dark, sun_light = c(pal, "sun"), c(pal, "sun_dark"), c(pal, "sun_light")
    ember_line = mix(OUTLINE, fire[0], 0.6)
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    wing_fire = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    wf = wing_fire.load()
    tips = []

    def flame_quill(x0: float, y0: float, ang: float, length: float, width: float, seed: int):
        """Eine Feder wie in deco_schrolli, deren Spitze brennt: ab 56 % der Länge schlägt eine Flamme aus ihr, breiter als
        die Feder, die über sie hinaus züngelt (die Flammen aller Federn kommen auf eine eigene Ebene mit Glutkontur)."""
        ca, sa = math.cos(ang), math.sin(ang)
        u_fire, u_end = length * 0.6, length * 1.42
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                dx, dy = x + 0.5 - x0, y + 0.5 - y0
                u = dx * ca + dy * sa
                v = -dx * sa + dy * ca
                if u < 0.0 or u > u_end:
                    continue
                k = u / length
                if u < u_fire + 1.2:
                    half = width * (0.55 + 0.45 * math.sin(math.pi * min(1.0, k * 1.15)))
                    if abs(v) <= half:
                        if abs(v) > half - 0.75:
                            col = OUTLINE
                        elif abs(v) < 0.45:
                            col = mix(feather_mid, feather_dark, 0.3)
                        else:
                            lit = 0.35 + 0.65 * (1.0 - (v + half) / (2 * half))
                            col = mix(feather_mid, feather, lit)
                            if (u * 1.3 + seed) % 5.0 < 0.6:
                                col = mix(col, feather_mid, 0.5)
                        put(px, img, x, y, col)
                if u >= u_fire:
                    t = (u - u_fire) / (u_end - u_fire)
                    vv = v - 1.5 * math.sin(t * 4.6 + seed * 1.9) * t
                    half = width * 1.02 * (1.0 - t) ** 0.8 * (0.86 + 0.24 * math.sin(t * 8.0 + seed))
                    if abs(vv) <= half and half > 0.3 and not (t > 0.5 and abs(vv + 0.4) < (t - 0.5) * half * 1.7):
                        core = 1.0 - abs(vv) / half
                        wf[x, y] = _ramp(fire[1:], 0.92 - 0.72 * t + 0.4 * core)
        tips.append((x0 + ca * u_end, y0 + sa * u_end))

    # die erhobene Schwinge: fünf Federn (flacher als in den ersten Gestalten, damit die Flammen Platz haben)
    for j in range(5):
        k = j / 4
        flame_quill(EMB_X + 12 + 3 * j, EMB_Y - 9 - j * 0.6, math.radians(-40 + 20 * k), 12.5 + 9.5 * k, 3.1 - 0.3 * k, j)
    # die gesenkte Schwinge: vier Federn
    for j in range(4):
        k = j / 3
        flame_quill(EMB_X + 12 + 3 * j, EMB_Y + 8 + j * 0.5, math.radians(36 - 18 * k), 11.5 + 7.5 * k, 2.8 - 0.3 * k, 20 + j)
    _outline(wing_fire, ember_line)
    img.alpha_composite(wing_fire)
    px = img.load()
    # Funken, die von den brennenden Spitzen stieben
    for i, (tx, ty) in enumerate(tips):
        for n in range(2):
            sx = round(tx + 1.5 + 3.0 * hash01(i, n, 341))
            sy = round(ty - 1.0 - 3.0 * hash01(i, n, 343))
            if 0 <= sy < FRAME_H and not (BAR_X - 3 <= sx and BAR_Y - 3 <= sy <= BAR_Y + BAR_H + 2):
                put(px, img, sx, sy, fire[5] if n == 0 else fire[3])

    # die Sonnenkrone auf dem Rahmen: Flammenzungen im Wechsel mit goldenen Strahlen (nicht über den Fassungen)
    gems = [BAR_X + round(BAR_W * f) for f in GEMS]
    flames = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    fp = flames.load()

    def tongue(cx: float, base: int, h: int, w: float, lean: float, seed: int):
        for k in range(h):
            t = k / max(1, h - 1)
            half = (w / 2) * (1.0 - t) ** 0.65 * (0.92 + 0.16 * math.sin(k * 1.9 + seed))
            mid = cx + lean * t ** 1.4 + 0.5 * math.sin(k * 0.9 + seed) * t
            for x in range(round(mid - half), round(mid + half) + 1):
                d = abs(x - mid) / max(0.6, half + 0.5)
                heat = 1.12 - t * 0.8 - d * 0.5
                y = base - k
                if 0 <= x < FRAME_W and 0 <= y < FRAME_H:
                    fp[x, y] = _ramp(fire[1:], heat)

    rays = []
    for i, x in enumerate(range(BAR_X + 22, BAR_X + BAR_W - 2, 5)):
        if any(abs(x - g) < 8 for g in gems):
            continue
        if i % 2 == 0:
            tongue(x, top - 1, 4 + (2 if i % 4 == 0 else 0) + round(hash01(x, 301)), 4.6, (hash01(x, 303) - 0.5) * 1.6, i)
        else:
            rays.append((x, 7 + (2 if i % 4 == 1 else 0) + round(hash01(x, 305))))
    for x, h in rays:
        for dy in range(h):
            half = 1 if dy < h - 3 else 0
            for dx in range(-half, half + 1):
                col = OUTLINE if abs(dx) == half and half > 0 else (ray if dx <= 0 else gold)
                if dy >= h - 2:
                    col = c(pal, "f3")
                put(px, img, x + dx, top - 1 - dy, col)
    _outline(flames, ember_line)
    img.alpha_composite(flames)
    px = img.load()
    # kleine Funken über der Krone
    for x, h in rays:
        if hash01(x, 307) > 0.55:
            sx, sy = x + (3 if hash01(x, 309) > 0.5 else -3), top - h - 1
            put(px, img, sx, sy, fire[5])
            if hash01(x, 311) > 0.5:
                for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    put(px, img, sx + ox, sy + oy, fire[3])

    # unter der Leiste: flüssiges Gold, das in glühenden Tropfen abreißt, dazwischen Funken
    for i, x in enumerate(range(BAR_X + 4, BAR_X + BAR_W - 2, 9)):
        length = 2 + round(3 * hash01(x, 321))
        for dy in range(length):
            t = dy / max(1, length - 1)
            put(px, img, x, bottom + 1 + dy, _ramp((gold, fire[4], fire[5]), t))
            put(px, img, x + 1, bottom + 1 + dy, gold_dark if t < 0.7 else fire[3])
        if hash01(x, 323) > 0.4:
            dy = length + 1 + round(2 * hash01(x, 325))
            put(px, img, x, bottom + 1 + dy, fire[5])
            put(px, img, x + 1, bottom + 1 + dy, fire[3])
            put(px, img, x, bottom + 2 + dy, fire[3])
            put(px, img, x + 1, bottom + 2 + dy, fire[2])
        if hash01(x, 327) > 0.6:
            put(px, img, x + 4, bottom + 4 + round(5 * hash01(x, 329)), fire[4] if hash01(x, 331) > 0.5 else fire[3])

    # Spitze: die Sonne – eine glühende Scheibe mit einem Kranz aus langen und kurzen Strahlen
    ex, ey = BAR_X + BAR_W + 4, BAR_Y + BAR_H // 2
    for k in range(16):
        a = math.radians(k * 22.5)
        r = 5.6 if k % 4 == 0 else (4.8 if k % 2 == 0 else 4.2)
        ca, sa = math.cos(a), math.sin(a)
        steps = int(r * 3)
        for s in range(steps + 1):
            rr = 2.6 + (r - 2.6) * s / steps
            put(px, img, round(ex + ca * rr), round(ey + sa * rr), fire[3] if rr < r - 0.8 else fire[4])
    for y in range(ey - 4, ey + 5):
        for x in range(ex - 4, ex + 5):
            d = math.hypot(x - ex, y - ey)
            if d <= 3.0:
                col = _ramp((fire[5], sun_light, sun, sun_dark), d / 3.0)
                if d > 2.4:
                    col = mix(col, ember_line, 0.55)
                put(px, img, x, y, col)
    put(px, img, ex - 1, ey - 1, (255, 255, 255, 255))


def post_almighty(img: Image.Image, pal: dict) -> None:
    """Nach dem Medaillon: sieben Sonnensteine auf seinem Ring (wo in der Wahren Gestalt die Augen waren)."""
    px = img.load()
    for k in range(7):
        a = math.radians(-150 + k * 50)
        x = round(EMB_X - 0.5 + math.cos(a) * (EMB_R - 2.5))
        y = round(EMB_Y - 0.5 + math.sin(a) * (EMB_R - 2.5))
        put(px, img, x - 1, y, c(pal, "flame3"))
        put(px, img, x + 1, y, c(pal, "sun_dark"))
        put(px, img, x, y - 1, c(pal, "flame4"))
        put(px, img, x, y + 1, c(pal, "sun_dark"))
        put(px, img, x, y, c(pal, "flame5"))


def frame_border_stone(img: Image.Image, pal: dict) -> None:
    """Rahmen des Weltenfressers: Platten aus Chaosgestein (jede an ihrem Anfang heller, wie gewölbt), dazwischen glühende
    Fugen wie die Nähte seiner Glieder, ein paar glimmende Risse; dazu die Fassungen der Edelsteine."""
    px = img.load()
    m0, m1, m2 = c(pal, "m0"), c(pal, "m1"), c(pal, "m2")
    seam0, seam1, seam2 = c(pal, "seam0"), c(pal, "seam1"), c(pal, "seam2")
    x0, x1, y0, y1 = BAR_X - 3, BAR_X + BAR_W + 2, BAR_Y - 3, BAR_Y + BAR_H + 2
    joints = [x for x in range(x0 + 15, x1 - 5, 16) if not any(abs(x - (BAR_X + round(BAR_W * f))) < 6 for f in GEMS)]
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if BAR_X <= x < BAR_X + BAR_W and BAR_Y <= y < BAR_Y + BAR_H:
                continue
            ring = min(x - x0, x1 - x, y - y0, y1 - y)
            prev = max([j for j in joints if j <= x], default=x0)
            along = (x - prev) / 16.0                                   # 0 am Anfang einer Platte
            n = hash01(x // 2, y, 401)
            if ring == 0:
                col = OUTLINE
            elif ring == 1:
                upper = (y - y0 <= 1 or x - x0 <= 1) and not (y1 - y <= 1)
                col = mix(m2, m1, 0.25 + 0.5 * along + 0.2 * n) if upper else mix(m1, m0, 0.3 + 0.4 * along + 0.2 * n)
            else:
                col = mix(m0, OUTLINE, 0.25 + 0.2 * n)
            if x in joints and ring >= 1:
                col = seam2 if ring == 2 else seam1
            elif ring >= 1 and (x - 1 in joints or x + 1 in joints):
                col = mix(col, seam0, 0.5)
            px[x, y] = col
    # glimmende Risse in der oberen und unteren Kante
    for i, x in enumerate(range(x0 + 7, x1 - 4, 23)):
        if hash01(x, 403) < 0.5:
            continue
        y = y0 + 1 if i % 2 == 0 else y1 - 1
        for k in range(3):
            put(px, img, x + k, y + (1 if (k == 1 and y == y0 + 1) else (-1 if k == 1 else 0)), seam1 if k != 1 else seam2)
    _gem_sockets(px, img, pal, mix(m1, m0, 0.2), mix(m0, OUTLINE, 0.2))


def deco_world_eater(img: Image.Image, pal: dict) -> None:
    """Der Weltenfresser: hinter dem Medaillon oben drei seiner Kronensplitter (violette Kristalle in goldener Fassung),
    unten ein gebogener Hauer; auf dem Rahmen – seinem Rücken – Stücke verschlungener Welten (Wiese mit Eiche, Netherrack mit
    Magma und Karmesinpilz, Endstein mit Chorus, Packeis mit Zacken), dazwischen Glut; unter der Leiste sein Gebiss (Zahnfleisch
    und Zähne, die Fänge zur Mitte gebogen); als Spitze die Kristallflosse seines Schwanzes."""
    px = img.load()
    g = {k: c(pal, k) for k in pal}
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    # die Kronensplitter: violette Kristalle, wo sie hinter dem Ring hervortreten in Gold gefasst
    shards = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    sp = shards.load()

    def shard(x0: float, y0: float, ang: float, length: float, width: float, seed: int):
        ca, sa = math.cos(ang), math.sin(ang)
        for y in range(FRAME_H):
            for x in range(FRAME_W):
                dx, dy = x + 0.5 - x0, y + 0.5 - y0
                u = dx * ca + dy * sa
                v = -dx * sa + dy * ca
                if u < 0.0 or u > length:
                    continue
                k = u / length
                half = width if k < 0.7 else width * (1.0 - (k - 0.7) / 0.3) + 0.15
                if abs(v) > half:
                    continue
                ring = math.hypot(x + 0.5 - EMB_X, y + 0.5 - EMB_Y)
                if EMB_R - 0.5 <= ring <= EMB_R + 2.2:
                    col = g["gold"] if v < 0.3 else g["gold_dark"]
                    if abs(v) > half - 0.8 or abs(ring - (EMB_R + 2.2)) < 0.6:
                        col = mix(g["gold_dark"], OUTLINE, 0.4)
                elif v < -half * 0.2:
                    col = g["crystal2"]
                elif v < half * 0.15:
                    col = g["crystal3"] if k > 0.25 else g["crystal2"]
                else:
                    col = g["crystal1"]
                if abs(v) > half - 0.7 and not (EMB_R - 0.5 <= ring <= EMB_R + 2.2):
                    col = mix(col, g["crystal0"], 0.55)
                if k > 0.8 and v < 0.5:
                    col = mix(col, g["crystal3"], 0.5)
                if hash01(x, y, seed, 411) > 0.94 and abs(v) < half - 0.8:
                    col = g["crystal3"]
                sp[x, y] = col

    shard(29.0, 12.0, math.radians(-60), 14.0, 2.4, 1)
    shard(33.0, 12.5, math.radians(-40), 19.0, 2.8, 2)
    shard(36.5, 14.0, math.radians(-22), 21.0, 2.4, 3)
    _outline(shards, OUTLINE)
    img.alpha_composite(shards)
    px = img.load()

    # der Hauer: von hinter dem Medaillon nach unten rechts gebogen, die Spitze wieder nach oben
    tusk = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    tp = tusk.load()
    p0, p1, p2 = (30.0, 33.0), (45.0, 46.5), (61.0, 34.5)
    samples = []
    for i in range(241):
        t = i / 240
        bx = (1 - t) ** 2 * p0[0] + 2 * (1 - t) * t * p1[0] + t * t * p2[0]
        by = (1 - t) ** 2 * p0[1] + 2 * (1 - t) * t * p1[1] + t * t * p2[1]
        tx = 2 * (1 - t) * (p1[0] - p0[0]) + 2 * t * (p2[0] - p1[0])
        ty = 2 * (1 - t) * (p1[1] - p0[1]) + 2 * t * (p2[1] - p1[1])
        n = math.hypot(tx, ty)
        samples.append((t, bx, by, -ty / n, tx / n, 2.9 * (1.0 - t) ** 0.8 + 0.25))
    for y in range(26, FRAME_H):
        for x in range(24, 66):
            best = None
            for (t, bx, by, nx, ny, r) in samples:
                d = math.hypot(x + 0.5 - bx, y + 0.5 - by)
                if d <= r and (best is None or d - r < best[0]):
                    side = (x + 0.5 - bx) * nx + (y + 0.5 - by) * ny
                    best = (d - r, t, side / r)
            if best is None:
                continue
            _, t, side = best
            col = _ramp((g["tooth_light"], g["tooth"], g["tooth_dark"]), 0.5 + 0.55 * side)
            if any(abs(t - b) < 0.012 for b in (0.3, 0.46)):
                col = mix(col, g["tooth_dark"], 0.6)
            if t < 0.12:
                col = mix(col, g["gum_dark"], 0.5)
            tp[x, y] = col
    _outline(tusk, OUTLINE)
    img.alpha_composite(tusk)
    px = img.load()

    # die verschlungenen Welten auf seinem Rücken
    worlds = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    wp = worlds.load()
    base = top - 1

    def wput(x, y, col):
        if 0 <= x < FRAME_W and 0 <= y < FRAME_H:
            wp[x, y] = col

    def chunk(cx: int, w: int, h: int, surface, surface_light, body, body_dark, seed: int):
        """Ein Brocken Welt auf dem Rücken: oben die Oberfläche (zwei Reihen), darunter Gestein, unregelmäßig gebrochen."""
        x0 = cx - w // 2
        for x in range(x0, x0 + w):
            rel = (x - x0) / max(1, w - 1)
            hh = h - (1 if (rel < 0.12 or rel > 0.88) else 0) - (1 if hash01(x, seed, 421) > 0.8 else 0)
            for k in range(hh):
                y = base - k
                if k >= hh - 1:
                    col = surface_light if hash01(x, seed, 423) > 0.45 else surface
                elif k >= hh - 2:
                    col = surface if hash01(x, k, seed, 425) > 0.25 else body
                else:
                    col = body if hash01(x, k, seed, 427) > 0.3 else body_dark
                    if k == 0:
                        col = mix(col, body_dark, 0.5)
                wput(x, y, col)
        return x0

    def surface_y(x: int, cx: int, w: int, h: int, seed: int) -> int:
        x0 = cx - w // 2
        rel = (x - x0) / max(1, w - 1)
        hh = h - (1 if (rel < 0.12 or rel > 0.88) else 0) - (1 if hash01(x, seed, 421) > 0.8 else 0)
        return base - hh

    # Wiese mit Eiche und Blume
    cx, w, h, s = 75, 17, 4, 1
    chunk(cx, w, h, g["grass"], g["grass_light"], g["dirt"], g["dirt_dark"], s)
    tx = cx - 1
    ty = surface_y(tx, cx, w, h, s)
    for k in range(4):
        wput(tx, ty - k, g["wood"])
        wput(tx + 1, ty - k, g["wood_dark"])
    for y in range(ty - 11, ty - 3):
        for x in range(tx - 5, tx + 7):
            d = math.hypot((x - tx - 0.5) / 5.3, (y - (ty - 6.8)) / 3.7)
            if d > 1.0 or (d > 0.86 and hash01(x, y, 431) > 0.5):
                continue
            lit = (x - tx) < 0 and y < ty - 6
            col = g["leaves_light"] if (lit and hash01(x, y, 433) > 0.3) else g["leaves"]
            if hash01(x, y, 435) > 0.82:
                col = mix(col, g["leaves_light"], 0.6)
            wput(x, y, col)
    fx = cx + 4
    fy = surface_y(fx, cx, w, h, s)
    wput(fx, fy, g["grass_light"])
    wput(fx, fy - 1, (230, 56, 48, 255))
    wput(cx - 6, surface_y(cx - 6, cx, w, h, s), g["grass_light"])
    wput(cx + 6, surface_y(cx + 6, cx, w, h, s), (255, 214, 64, 255))

    # Netherrack mit einem Becken aus Magma, einem Karmesinpilz und Flämmchen (zwischen den Edelsteinen bei einem Drittel und
    # der Hälfte)
    cx, w, h, s = 124, 13, 4, 2
    chunk(cx, w, h, g["netherrack"], g["fungus"], g["netherrack"], g["netherrack_dark"], s)
    for x in range(cx - 1, cx + 4):
        y = surface_y(x, cx, w, h, s) + 1
        wput(x, y, g["magma_light"] if x in (cx, cx + 1) else g["magma"])
        wput(x, y + 1, g["magma"] if x in (cx, cx + 2) else g["netherrack_dark"])
    px_ = cx - 4
    py_ = surface_y(px_, cx, w, h, s)
    for k in range(3):
        wput(px_, py_ - k, (226, 196, 150, 255))
    for x in range(px_ - 3, px_ + 4):
        wput(x, py_ - 3, g["fungus"])
    for x in range(px_ - 2, px_ + 3):
        wput(x, py_ - 4, g["fungus_light"] if x < px_ else g["fungus"])
    wput(px_ - 2, py_ - 3, (255, 222, 120, 255))
    wput(px_ + 2, py_ - 4, (255, 222, 120, 255))
    for fxp, fh in ((cx + 5, 3), (cx + 6, 2)):
        fy = surface_y(fxp, cx, w, h, s)
        for k in range(fh):
            wput(fxp, fy - k, (255, 236, 140, 255) if k == 0 else (255, 150, 40, 255))

    # Endstein mit einer Chorus-Pflanze
    cx, w, h, s = 186, 16, 4, 3
    chunk(cx, w, h, g["endstone"], g["endstone"], g["endstone"], g["endstone_dark"], s)
    sx = cx
    sy = surface_y(sx, cx, w, h, s)
    stem = [(0, 0), (0, -1), (0, -2), (0, -3), (0, -4), (-1, -4), (-2, -4), (-2, -5), (-2, -6), (1, -2), (2, -2), (2, -3),
            (2, -4), (2, -5), (2, -6), (2, -7), (0, -5), (0, -6), (0, -7), (0, -8)]
    for ox, oy in stem:
        wput(sx + ox, sy + oy, g["chorus"])
    for ox, oy in ((-2, -7), (2, -8), (0, -9)):
        for bx, by in ((0, 0), (1, 0), (0, -1), (1, -1)):
            wput(sx + ox + bx - (1 if ox < 0 else 0), sy + oy + by, g["chorus_light"] if (bx, by) != (1, 0) else g["chorus"])
    # Packeis mit Eiszacken
    cx, w, h, s = 215, 16, 4, 4
    chunk(cx, w, h, g["ice_light"], g["ice_light"], g["ice"], g["ice_dark"], s)
    for ox, sh in ((-5, 4), (-2, 6), (0, 9), (2, 5), (5, 3)):
        x = cx + ox
        y = surface_y(x, cx, w, h, s)
        for k in range(sh):
            wput(x, y - k, g["ice_light"] if k > sh - 3 else g["ice"])
            if k < sh - 3:
                wput(x + 1, y - k, g["ice_dark"])
    _outline(worlds, OUTLINE)
    img.alpha_composite(worlds)
    px = img.load()
    # Glut zwischen den Welten
    for i, x in enumerate(range(BAR_X + 6, BAR_X + BAR_W - 4, 7)):
        if hash01(x, 441) > 0.62 and not any(abs(x - X) < 11 for X in (75, 124, 186, 215)) \
                and not any(abs(x - (BAR_X + round(BAR_W * f))) < 8 for f in GEMS):
            y = top - 2 - round(4 * hash01(x, 443))
            put(px, img, x, y, g["seam2"] if hash01(x, 445) > 0.5 else g["seam1"])

    # das Gebiss unter der Leiste: Zahnfleisch, daraus Zähne; die großen Fänge zur Mitte gebogen
    teeth = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    tq = teeth.load()
    for x in range(BAR_X - 1, BAR_X + BAR_W + 1):
        tq[x, bottom + 1] = g["gum"] if hash01(x, 451) > 0.2 else g["gum_dark"]
    mid = BAR_X + BAR_W / 2
    for i, x in enumerate(range(BAR_X + 4, BAR_X + BAR_W - 2, 6)):
        big = i % 3 == 1
        width = 4.6 if big else 3.0
        length = (7 if big else 3) + (1 if hash01(x, 453) > 0.6 else 0)
        bend = (1.6 if big else 0.4) * (1 if x < mid else -1)
        for k in range(length):
            t = k / max(1, length - 1)
            half = (width / 2) * (1.0 - t) ** 0.85
            cxk = x + bend * t * t
            for xx in range(round(cxk - half), round(cxk + half) + 1):
                d = (xx - cxk) / max(0.5, half + 0.5)
                col = _ramp((g["tooth_light"], g["tooth"], g["tooth_dark"]), 0.45 + 0.6 * d)
                if k == 0:
                    col = mix(col, g["gum"], 0.35)
                tq[xx, bottom + 2 + k] = col
    _outline(teeth, OUTLINE)
    img.alpha_composite(teeth)
    px = img.load()

    # Spitze: die Kristallflosse am Schwanz – zwei Schwingen aus Kristall, dazwischen ein Dorn
    fin = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    fq = fin.load()
    ex, ey = BAR_X + BAR_W + 2, BAR_Y + BAR_H // 2
    for y in range(ey - 12, ey + 13):
        for x in range(ex, FRAME_W - 1):
            rx = x - ex
            dy = y + 0.5 - (ey + 0.5)
            reach = 3.2 + rx * 1.55
            if abs(dy) > reach or abs(dy) < rx * 0.95 - 1.2:
                continue
            if rx > 5 and abs(dy) < reach - 3.0:
                continue
            outer = abs(dy) / max(1.0, reach)
            col = _ramp((g["crystal1"], g["crystal2"], g["crystal3"]), 0.2 + 0.7 * outer)
            if dy > 0:
                col = mix(col, g["crystal0"], 0.3)
            fq[x, y] = col
    for x in range(ex, ex + 6):
        fq[x, ey] = g["crystal3"] if x > ex + 2 else g["crystal2"]
    _outline(fin, OUTLINE)
    img.alpha_composite(fin)


def post_world_eater(img: Image.Image, pal: dict) -> None:
    """Nach dem Medaillon: der Ring ist sein Rachen – acht Lippen aus Chaosgestein mit glühenden Fugen dazwischen."""
    px = img.load()
    seam1, seam2 = c(pal, "seam1"), c(pal, "seam2")
    for y in range(FRAME_H):
        for x in range(0, int(EMB_X + EMB_R) + 2):
            d = math.hypot(x - EMB_X + 0.5, y - EMB_Y + 0.5)
            if not (EMB_R - 4.0 < d <= EMB_R - 1.0):
                continue
            ang = math.degrees(math.atan2(y - EMB_Y + 0.5, x - EMB_X + 0.5)) % 45.0
            if ang < 3.2 or ang > 45.0 - 3.2:
                px[x, y] = seam2 if EMB_R - 3.0 < d <= EMB_R - 2.0 else seam1


# =============================================================================================
# Der Kraken (seit 0.94)
# =============================================================================================

def _arm(pts: list, radius, pal: dict, side: int, seed: int) -> Image.Image:
    """Ein Krakenarm als Schlauch entlang der (dicht liegenden) Punkte pts, radius(t) von 0 (Ansatz) bis 1 (Spitze).
    side = +1: die blasse Unterseite mit den Saugnäpfen liegt rechts der Laufrichtung (im Bild), -1 links – so zeigen die
    Saugnäpfe zur Leiste und in jeder Krümmung nach innen. Oben purpurrote Haut mit Glanzstreif und türkis leuchtenden
    Punkten, dunkle Kontur."""
    layer = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    lp = layer.load()
    skin = [c(pal, f"skin{k}") for k in range(5)]
    under0, under1, under2 = c(pal, "under0"), c(pal, "under1"), c(pal, "under2")
    sucker, sucker_dark = c(pal, "sucker"), c(pal, "sucker_dark")
    spot, spot_light = c(pal, "spot"), c(pal, "spot_light")
    lengths = [0.0]
    for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
        lengths.append(lengths[-1] + math.hypot(x1 - x0, y1 - y0))
    total = lengths[-1]
    best: dict = {}
    for i, (cx, cy) in enumerate(pts):
        j0, j1 = max(0, i - 1), min(len(pts) - 1, i + 1)
        tx, ty = pts[j1][0] - pts[j0][0], pts[j1][1] - pts[j0][1]
        tl = math.hypot(tx, ty) or 1.0
        tx, ty = tx / tl, ty / tl
        nx, ny = -ty * side, tx * side
        t = lengths[i] / total
        r = radius(t)
        for y in range(int(cy - r - 2), int(cy + r + 3)):
            for x in range(int(cx - r - 2), int(cx + r + 3)):
                ox, oy = x + 0.5 - cx, y + 0.5 - cy
                along = ox * tx + oy * ty
                across = ox * nx + oy * ny
                if abs(along) > 0.55 or abs(across) > r:
                    continue
                d = abs(across)
                if (x, y) not in best or d < best[(x, y)][0]:
                    best[(x, y)] = (d, across / max(r, 0.01), t, lengths[i], r)
    for (x, y), (d, v, t, s, r) in best.items():
        if not (0 <= x < FRAME_W and 0 <= y < FRAME_H):
            continue
        if r < 1.3:
            col = under1 if v > 0.35 else (skin[3] if v > -0.4 else skin[2])
        elif v > 0.28:
            col = under0
            phase = (s + 1.7 * hash01(seed, 3)) % 3.6
            if phase < 2.0 and v < 0.98:
                # Saugnäpfe: ein heller Ring, zur Kante hin die dunkle Öffnung
                col = sucker if (v < 0.64 or phase < 0.55 or phase > 1.45) else sucker_dark
        elif v > -0.12:
            col = skin[2]
        elif v > -0.58:
            col = skin[4] if (t < 0.85 and -0.42 < v < -0.24) else skin[3]
        else:
            col = skin[1]
        if r >= 1.3 and -0.6 < v < 0.1 and hash01(int(s // 3.0), seed, 7) > 0.84 and (s % 3.0) < 1.0:
            col = spot_light if hash01(int(s // 3.0), seed, 9) > 0.6 else spot
        if v > 0.3 and t > 0.92:
            col = under2
        lp[x, y] = col
    _outline(layer, (20, 4, 10, 255))
    return layer


def deco_kraken(img: Image.Image, pal: dict) -> None:
    """Der Kraken greift nach der Leiste: ein Arm kommt oben hinter dem Medaillon hervor, liegt in Wellen auf dem Rahmen und
    klammert sich mit der Spitze an ihm fest, ein zweiter tut es unten; auf dem Rahmen Seepocken, darüber Luftblasen, unter
    der Leiste hängen Tangfäden und Tropfen seiner Tinte."""
    px = img.load()
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2
    barnacle, barnacle_dark = c(pal, "barnacle"), c(pal, "barnacle_dark")
    weed, weed_light = c(pal, "weed"), c(pal, "weed_light")
    ink, ink_light = c(pal, "ink"), c(pal, "ink_light")
    acc, glow = c(pal, "acc"), c(pal, "glow")

    # Tangfäden unter der Leiste (rechte Hälfte), sie wiegen sich leicht
    for k, x in enumerate(range(BAR_X + 112, BAR_X + BAR_W - 4, 10)):
        length = 4 + round(5 * hash01(x, 31))
        for dy in range(length):
            sx = x + round(0.9 * math.sin(dy * 0.9 + k))
            put(px, img, sx, bottom + 1 + dy, weed_light if dy % 3 == 0 else weed)
    # Tintentropfen
    for x in (BAR_X + 127, BAR_X + 147, BAR_X + 171):
        h = 2 + round(2 * hash01(x, 37))
        for dy in range(h):
            put(px, img, x, bottom + 1 + dy, ink)
        put(px, img, x, bottom + 1 + h, ink_light)
        put(px, img, x - 1, bottom + 1 + h, ink)
        put(px, img, x + 1, bottom + 1 + h, ink)
        put(px, img, x, bottom + 2 + h, ink)
    # Luftblasen über dem Rahmen (rechts, wo der Arm nicht liegt)
    for x in range(BAR_X + 62, BAR_X + BAR_W - 6, 9):
        if hash01(x, 41) < 0.35 or any(abs(x - (BAR_X + round(BAR_W * f))) < 7 for f in GEMS):
            continue
        y = 2 + round(hash01(x, 43) * 9)
        if hash01(x, 47) > 0.55:
            for dx, dy in ((0, -1), (1, 0), (0, 1), (-1, 0)):
                put(px, img, x + dx, y + dy, mix(acc, glow, 0.4))
            put(px, img, x - 1, y - 1, glow)
        else:
            put(px, img, x, y, mix(acc, glow, 0.6))

    # oben: der Arm kommt hinter dem Medaillon hervor, liegt in Wellen auf dem Rahmen und hakt sich vor dem ersten Edelstein
    # mit der Spitze ein
    end = BAR_X + round(BAR_W * min(GEMS)) - 12
    pts = []
    x = 30.0
    while x <= end:
        rise = max(0.0, (40.0 - x) / 10.0)
        pts.append((x, 10.2 + 1.2 * math.sin((x - 30.0) / 10.0) - 3.0 * rise))
        x += 0.25
    hx, hy, hr = float(end), 14.4, 4.2
    for k in range(1, 61):
        ang = math.radians(-90.0 + 128.0 * k / 60)
        pts.append((hx + hr * math.cos(ang), hy + 0.2 + hr * math.sin(ang)))
    img.alpha_composite(_arm(pts, lambda t: 3.3 - 2.3 * t ** 0.85, pal, 1, 3))
    # unten: der zweite Arm, länger, hakt sich von unten ein
    pts = []
    x = 32.0
    while x <= 150.0:
        drop = max(0.0, (40.0 - x) / 8.0)
        pts.append((x, 34.4 + 1.2 * math.sin((x - 32.0) / 11.0 + 1.2) + 2.5 * drop))
        x += 0.25
    hx, hy, hr = 150.0, 29.8, 3.8
    for k in range(1, 61):
        ang = math.radians(90.0 - 128.0 * k / 60)
        pts.append((hx + hr * math.cos(ang), hy + hr * math.sin(ang)))
    img.alpha_composite(_arm(pts, lambda t: 3.1 - 2.1 * t ** 0.85, pal, -1, 5))

    # Seepocken auf dem Rahmen: kleine Kegel mit dunkler Öffnung
    def barnacle_at(bx: int, by: int, up: bool):
        d = -1 if up else 1
        put(px, img, bx - 1, by, barnacle_dark)
        put(px, img, bx, by, barnacle)
        put(px, img, bx + 1, by, mix(barnacle, barnacle_dark, 0.5))
        put(px, img, bx, by + d, OUTLINE)
        put(px, img, bx - 1, by + d, barnacle)
        put(px, img, bx + 1, by + d, barnacle_dark)
    for x in (BAR_X + 76, BAR_X + 84, BAR_X + 136, BAR_X + 150, BAR_X + 176):
        barnacle_at(x, top - 1, True)
    for x in (BAR_X + 112, BAR_X + 166):
        barnacle_at(x, bottom + 1, False)


def post_kraken(img: Image.Image, pal: dict) -> None:
    """Nach dem Rahmen: die Spitze – ein eingerollter Arm am Ende der Leiste – und Seepocken auf dem Ring des Medaillons."""
    px = img.load()
    pts = []
    cx, cy = 234.6, 22.0
    turns = 1.25
    for k in range(0, 241):
        t = k / 240
        ang = math.radians(150.0 + 360.0 * turns * t)
        r = 6.2 - 4.4 * t
        pts.append((cx + r * math.cos(ang) * 0.82, cy + r * math.sin(ang)))
    # Ansatz am Rahmen: ein kurzes Stück von links
    lead = [(229.0 + 0.25 * k, 26.5 + 0.2 * k) for k in range(0, 4)]
    tip = _arm(lead + pts, lambda t: 2.3 - 1.5 * t, pal, 1, 11)
    tp = tip.load()
    for y in range(FRAME_H):
        for x in range(BAR_X + BAR_W, FRAME_W):
            if tp[x, y][3]:
                px[x, y] = tp[x, y]
    barnacle, barnacle_dark = c(pal, "barnacle"), c(pal, "barnacle_dark")
    for ang in (-150, -112, 160, 128):
        a = math.radians(ang)
        x = round(EMB_X + math.cos(a) * (EMB_R - 2.5))
        y = round(EMB_Y + math.sin(a) * (EMB_R - 2.5))
        put(px, img, x, y, barnacle)
        put(px, img, x + 1, y, barnacle_dark)
        put(px, img, x, y + 1, barnacle_dark)


def fill_strip_kraken() -> Image.Image:
    """Füllung des Kraken: seine Tinte – wirbelnde Schlieren in Purpurrot, dunkle Flecken wie auf seiner Haut, dazwischen
    einzelne türkis leuchtende Punkte."""
    pal = ELEMENTS["kraken"]
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    spot, spot_light = c(pal, "spot"), c(pal, "spot_light")
    tau = math.tau
    img = Image.new("RGBA", (256, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        top = 1.0 - y / (BAR_H - 1)
        for x in range(256):
            swirl = 0.5 + 0.5 * math.sin(tau * x / 64 + 2.3 * math.sin(tau * y / 18 + tau * x / 32))
            curl = 0.5 + 0.5 * math.sin(tau * x / 32 - 1.6 * math.cos(tau * y / 9 + tau * x / 128))
            d1, _ = _worley(x, y * 1.4, 8.0, 32, 517)
            blot = max(0.0, 1.0 - d1 / 2.4)
            v = 0.25 + 0.45 * swirl + 0.2 * curl - 0.35 * blot
            col = mix(mix(f0, f1, 0.3 + 0.6 * top), f2, max(0.0, v - 0.3) * 1.25)
            if swirl > 0.92 and curl > 0.5:
                col = mix(col, f3, 0.55)
            h = hash01(x, y, 521)
            if h > 0.982 and 1 <= y <= BAR_H - 2:
                col = spot_light if h > 0.992 else spot
            if y == 1:
                col = mix(col, f3, 0.3)
            if y == BAR_H - 1:
                col = mix(col, f0, 0.55)
            px[x, y] = col
    return img


# =============================================================================================
# Die Meerhexe (seit 0.97)
# =============================================================================================

def _witch_coral(layer: Image.Image, g: dict, x0: float, y0: float, ang: float, length: float, width: float, depth: int,
                 seed: int) -> None:
    """Ein Ast schwarzer Koralle: ein sich verjüngender Strang, der sich gabelt; an den Spitzen leuchtende Polypen."""
    lp = layer.load()
    steps = max(2, int(length * 2))
    x, y = x0, y0
    for i in range(steps + 1):
        k = i / steps
        w = width * (1.0 - 0.55 * k)
        px_, py_ = x0 + math.cos(ang) * length * k, y0 + math.sin(ang) * length * k
        r = max(0.5, w)
        for yy in range(int(py_ - r - 1), int(py_ + r + 2)):
            for xx in range(int(px_ - r - 1), int(px_ + r + 2)):
                if not (0 <= xx < FRAME_W and 0 <= yy < FRAME_H):
                    continue
                d = math.hypot(xx + 0.5 - px_, yy + 0.5 - py_)
                if d > r:
                    continue
                shade = 0.25 + 0.5 * (1.0 - depth / 3.0) + (0.2 if (xx + 0.5 - px_) * math.sin(ang) - (yy + 0.5 - py_) * math.cos(ang) < 0 else 0.0)
                lp[xx, yy] = _ramp((g["coral0"], g["coral1"], g["coral2"], g["coral3"]), min(1.0, shade))
        x, y = px_, py_
    if depth >= 2 or length < 3.0:
        for dx, dy in ((0, 0), (1, 0), (0, 1)):
            xx, yy = int(x) + dx, int(y) + dy
            if 0 <= xx < FRAME_W and 0 <= yy < FRAME_H:
                lp[xx, yy] = g["polyp_light"] if (dx, dy) == (0, 0) else g["polyp"]
        return
    for side in (-1, 1):
        a = ang + side * math.radians(24 + 14 * hash01(seed, side, 701))
        _witch_coral(layer, g, x, y, a, length * (0.62 + 0.1 * hash01(seed, side, 703)), width * 0.6, depth + 1, seed * 3 + side)


def deco_sea_witch(img: Image.Image, pal: dict) -> None:
    """Die Meerhexe: hinter dem Medaillon fächert schwarze Koralle (oben vier Äste, unten zwei), an den Spitzen leuchtende
    Polypen; unter der Leiste treibt Tang mit leuchtenden Spitzen, darüber steigen Blasen; als Spitze eine Korallengabel, die
    eine leuchtende Kugel hält. (Kleine Korallen und Perlen auf dem Rahmen folgen in post_sea_witch.)"""
    g = {k: c(pal, k) for k in pal}
    bottom = BAR_Y + BAR_H + 2
    fan = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    for i, (x0, y0, ang, length, width) in enumerate((
            (30.0, 14.0, -100.0, 9.0, 1.8), (33.0, 14.0, -70.0, 11.0, 2.0), (36.0, 15.0, -45.0, 10.0, 1.8),
            (38.0, 17.0, -20.0, 8.0, 1.5), (36.0, 30.0, 35.0, 8.0, 1.5), (33.0, 33.0, 65.0, 7.0, 1.3))):
        _witch_coral(fan, g, x0, y0, math.radians(ang), length, width, 0, i + 1)
    _outline(fan, OUTLINE)
    img.alpha_composite(fan)
    # die Spitze: eine Korallengabel mit einer leuchtenden Kugel
    tip = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    tp = tip.load()
    for i, (ang, length) in enumerate(((-30.0, 6.0), (30.0, 6.0))):
        _witch_coral(tip, g, 228.0, 22.5, math.radians(ang), length, 1.1, 2, 31 + i)
    for y in range(17, 29):
        for x in range(228, 240):
            d = math.hypot(x + 0.5 - 234.0, y + 0.5 - 22.5)
            if d <= 3.6:
                tp[x, y] = _ramp((g["orb_light"], g["orb"], g["f1"]), d / 3.6) if d > 0.8 else g["glow"]
    _outline(tip, OUTLINE)
    img.alpha_composite(tip)
    # Tang unter der Leiste, mit leuchtenden Spitzen
    px = img.load()
    kelp = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    kp = kelp.load()
    for i, x0 in enumerate((58, 74, 93, 110, 131, 149, 172, 190, 207, 223)):
        length = 4 + round(hash01(x0, 709) * 7)
        phase = hash01(x0, 711) * math.tau
        x = x0
        for k in range(length):
            x = x0 + round(1.1 * math.sin(k * 0.7 + phase))
            put(kp, kelp, x, bottom + 1 + k, g["kelp_light"] if k % 3 == 1 else g["kelp"])
        put(kp, kelp, x, bottom + 1 + length, g["kelp_glow"])
    _outline(kelp, OUTLINE)
    kl = kelp.load()
    for y in range(FRAME_H):
        for x in range(FRAME_W):
            if kl[x, y][3] and y > bottom:
                px[x, y] = kl[x, y]
    # Blasen über dem Rahmen
    for x in range(BAR_X + 60, BAR_X + BAR_W - 6, 11):
        if hash01(x, 713) < 0.4 or any(abs(x - (BAR_X + round(BAR_W * f))) < 7 for f in GEMS):
            continue
        y = 2 + round(hash01(x, 717) * 8)
        if hash01(x, 719) > 0.5:
            for dx, dy in ((0, -1), (1, 0), (0, 1), (-1, 0)):
                put(px, img, x + dx, y + dy, mix(g["acc"], g["glow"], 0.4))
            put(px, img, x - 1, y - 1, g["glow"])
        else:
            put(px, img, x, y, mix(g["acc"], g["glow"], 0.6))


def post_sea_witch(img: Image.Image, pal: dict) -> None:
    """Nach dem Rahmen: kleine Korallen sprießen aus der Oberkante (mit leuchtenden Polypen), dazwischen liegen Perlen."""
    g = {k: c(pal, k) for k in pal}
    px = img.load()
    top = BAR_Y - 3
    corals = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    for i, (x0, ang, length) in enumerate(((70, -100.0, 6.0), (98, -80.0, 7.0), (140, -95.0, 5.0), (176, -75.0, 6.5), (205, -105.0, 5.5))):
        if any(abs(x0 - (BAR_X + round(BAR_W * f))) < 8 for f in GEMS):
            continue
        _witch_coral(corals, g, x0 + 0.5, top + 0.5, math.radians(ang), length, 1.1, 1, 51 + i)
    _outline(corals, OUTLINE)
    cp = corals.load()
    for y in range(0, top + 1):
        for x in range(FRAME_W):
            if cp[x, y][3]:
                px[x, y] = cp[x, y]
    for x0 in (84, 121, 158, 192):
        if any(abs(x0 - (BAR_X + round(BAR_W * f))) < 8 for f in GEMS):
            continue
        for dx, dy, col in ((0, 0, g["pearl"]), (1, 0, g["pearl"]), (0, 1, g["pearl_dark"]), (1, 1, g["pearl_dark"]), (0, -1, OUTLINE),
                            (1, -1, OUTLINE), (-1, 0, OUTLINE), (2, 0, OUTLINE)):
            put(px, img, x0 + dx, top - 1 + dy, col)


# =============================================================================================
# Der Seelenhirte (seit 0.98)
# =============================================================================================

def _ram_horn(layer: Image.Image, g: dict, cx: float, cy: float, r0: float, r1: float, a0: float, a1: float, w0: float,
              w1: float) -> None:
    """Ein gerolltes Widderhorn: ein sich verjüngender Strang auf einer Spirale (Halbmesser r0 → r1, Winkel a0 → a1 in Grad,
    Breite w0 → w1), quer gerippt – helle Wülste, dunkle Rillen, zur Spitze heller."""
    lp = layer.load()
    steps = 140
    cols = (g["horn0"], g["horn1"], g["horn2"], g["horn3"])
    for i in range(steps + 1):
        k = i / steps
        a = math.radians(a0 + (a1 - a0) * k)
        r = r0 + (r1 - r0) * k
        w = w0 + (w1 - w0) * k
        px_, py_ = cx + math.cos(a) * r, cy + math.sin(a) * r
        arc = abs(a1 - a0) * math.pi / 180.0 * (r0 + r1) / 2.0 * k
        ridge = (arc / 1.7) % 1.0
        for yy in range(int(py_ - w - 1), int(py_ + w + 2)):
            for xx in range(int(px_ - w - 1), int(px_ + w + 2)):
                if not (0 <= xx < FRAME_W and 0 <= yy < FRAME_H):
                    continue
                d = math.hypot(xx + 0.5 - px_, yy + 0.5 - py_)
                if d > w:
                    continue
                # außen (vom Mittelpunkt der Spirale weg) heller
                out = ((xx + 0.5 - cx) * math.cos(a) + (yy + 0.5 - cy) * math.sin(a)) - r
                shade = 0.3 + 0.45 * k + (0.18 if out > 0 else -0.05) + (0.16 if ridge < 0.35 else -0.1 if ridge > 0.8 else 0.0)
                lp[xx, yy] = _ramp(cols, max(0.0, min(1.0, shade)))


def deco_soul_shepherd(img: Image.Image, pal: dict) -> None:
    """Der Seelenhirte: hinter dem Medaillon rollen sich oben und unten zwei Widderhörner; als Spitze der eiserne Haken seines
    Stabs, an dem eine Laterne voll Seelenfeuer hängt; unter der Leiste hängen an Ketten Talismane. (Die Irrlichter über dem
    Rahmen folgen in post_soul_shepherd.)"""
    g = {k: c(pal, k) for k in pal}
    horns = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    _ram_horn(horns, g, 38.0, 9.0, 7.5, 2.0, 160.0, -200.0, 3.0, 1.0)
    _ram_horn(horns, g, 38.0, 35.0, 7.5, 2.0, 200.0, 560.0, 3.0, 1.0)
    _outline(horns, OUTLINE)
    img.alpha_composite(horns)
    # die Spitze: der Stab kommt aus der Leiste, sein eiserner Haken krümmt sich nach oben herum, an der Spitze die Laterne
    tip = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    tp = tip.load()
    for x in range(225, 231):
        for y in (21, 22):
            put(tp, tip, x, y, mix(g["wood"], g["horn2"], 0.35) if y == 21 else g["wood"])
    put(tp, tip, 230, 21, g["iron2"])
    put(tp, tip, 230, 22, g["iron1"])
    for i in range(80):                                       # der Haken: von links unten über den Scheitel nach rechts
        a = math.radians(200.0 - i * 2.6)
        x, y = 233.5 + math.cos(a) * 3.2, 18.0 - math.sin(a) * 3.2
        put(tp, tip, int(round(x)), int(round(y)), g["iron2"] if math.sin(a) > 0.3 else g["iron1"])
        put(tp, tip, int(round(x - math.cos(a) * 0.9)), int(round(y + math.sin(a) * 0.9)), g["iron1"])
    put(tp, tip, 237, 19, g["iron1"])
    put(tp, tip, 237, 20, g["iron2"])                         # die Kette
    for y in range(21, 28):                                   # die Laterne
        for x in range(235, 240):
            if y in (21, 27):
                put(tp, tip, x, y, g["iron2"] if y == 21 else g["iron1"])
            elif x in (235, 239):
                put(tp, tip, x, y, g["iron1"])
            else:
                d = abs(x - 237) + abs(y - 24)
                put(tp, tip, x, y, g["soul_light"] if d == 0 else g["glow"] if d == 1 else g["soul"])
    _outline(tip, OUTLINE)
    img.alpha_composite(tip)
    # unter der Leiste: Ketten mit Talismanen
    px = img.load()
    bottom = BAR_Y + BAR_H + 2
    charms = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    cp = charms.load()
    for i, x0 in enumerate((62, 88, 117, 141, 168, 194, 214)):
        if any(abs(x0 - (BAR_X + round(BAR_W * f))) < 6 for f in GEMS):
            continue
        length = 2 + round(hash01(x0, 921) * 4)
        for k in range(length):
            put(cp, charms, x0, bottom + 1 + k, g["iron2"] if k % 2 == 0 else g["iron1"])
        y = bottom + 1 + length
        kind = i % 3
        if kind == 0:                                          # ein Knochen
            for dy in range(3):
                put(cp, charms, x0, y + dy, g["bone"] if dy != 1 else g["bone_dark"])
            put(cp, charms, x0 - 1, y + 2, g["bone"])
            put(cp, charms, x0 + 1, y + 2, g["bone"])
        elif kind == 1:                                        # ein Schädelchen mit leuchtenden Augen
            for dx in (-1, 0, 1):
                for dy in (0, 1):
                    put(cp, charms, x0 + dx, y + dy, g["bone"])
            put(cp, charms, x0 - 1, y + 1, g["soul"])
            put(cp, charms, x0 + 1, y + 1, g["soul"])
            put(cp, charms, x0, y + 2, g["bone_dark"])
        else:                                                  # ein Laternchen
            put(cp, charms, x0, y, g["iron2"])
            put(cp, charms, x0, y + 1, g["soul_light"])
            put(cp, charms, x0, y + 2, g["soul"])
            put(cp, charms, x0, y + 3, g["iron1"])
    _outline(charms, OUTLINE)
    kl = charms.load()
    for y in range(FRAME_H):
        for x in range(FRAME_W):
            if kl[x, y][3] and y > bottom:
                px[x, y] = kl[x, y]


def post_soul_shepherd(img: Image.Image, pal: dict) -> None:
    """Nach dem Rahmen: kleine Irrlichter (Flämmchen aus Seelenfeuer) treiben über der Oberkante."""
    g = {k: c(pal, k) for k in pal}
    px = img.load()
    top = BAR_Y - 3
    for x0 in range(BAR_X + 22, BAR_X + BAR_W - 10, 17):
        if any(abs(x0 - (BAR_X + round(BAR_W * f))) < 8 for f in GEMS) or hash01(x0, 923) < 0.25:
            continue
        y = top - 4 - round(hash01(x0, 925) * 5)
        for (dx, dy, col) in ((0, 0, g["soul_light"]), (0, 1, g["glow"]), (-1, 1, g["soul"]), (1, 1, g["soul"]), (0, 2, g["soul"]),
                              (0, -1, g["soul"])):
            put(px, img, x0 + dx, y + dy, col)
        for (dx, dy) in ((0, -2), (-1, -1), (1, -1), (-2, 1), (2, 1), (-1, 2), (1, 2), (0, 3), (-1, 0), (1, 0)):
            if px[x0 + dx, y + dy][3] == 0:
                put(px, img, x0 + dx, y + dy, OUTLINE)


# =============================================================================================
# Ignara, die Flammenpriesterin (seit 1.2)
# =============================================================================================

def _ignara_tongue(layer: Image.Image, g: dict, cx: int, base: int, h: int, w: int, seed: float) -> None:
    """Eine Flammenzunge: unten breit und rot, in der Mitte golden, die Spitze fast weiß; sie schwingt leicht zur Seite."""
    lp = layer.load()
    for k in range(h):
        t = k / max(1, h - 1)
        half = max(0, round((w / 2) * (1 - t) ** 0.7))
        sway = round(math.sin(k * 0.8 + seed) * t * 1.4)
        for dx in range(-half, half + 1):
            edge = abs(dx) == half and half > 0
            if t < 0.3:
                col = g["f1"] if edge else g["f2"]
            elif t < 0.75:
                col = g["f1"] if edge else (g["f3"] if dx == 0 else g["f2"])
            else:
                col = g["f2"] if edge else g["f3"]
            put(lp, layer, cx + dx + sway, base - k, col)


def deco_ignara(img: Image.Image, pal: dict) -> None:
    """Über dem Rahmen lodern Flammenzungen – am Medaillon hoch wie Ignaras Flammenhaar, nach rechts niedriger; unter der
    Leiste schlängelt sich ihre Flammenpeitsche (goldener Griff am Medaillon, die Schnur in Wellen, die Spitze brennt); als
    Spitze der Leiste ein großer Feueropal in goldener Fassung."""
    g = {k: c(pal, k) for k in pal}
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2
    # Flammenzungen über der Oberkante
    flames = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    for x in range(BAR_X - 3, BAR_X + BAR_W - 2, 6):
        if any(abs(x - (BAR_X + round(BAR_W * f))) < 7 for f in GEMS):
            continue
        rel = max(0.0, (x - BAR_X) / BAR_W)
        hgt = 2 + round(8 * (1.0 - rel) ** 2.2 + 3.5 * hash01(x, 1301) ** 1.5)
        _ignara_tongue(flames, g, x, top - 1, hgt, 5 if rel < 0.25 else 4, hash01(x, 1303) * 6.0)
    _outline(flames, g["f0"])
    img.alpha_composite(flames)
    # die Flammenpeitsche unter der Leiste
    whip = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    wp = whip.load()
    # der Griff: Gold, schräg vom Medaillon nach rechts unten
    for k in range(7):
        x, y = 34 + k, 33 + k // 3
        put(wp, whip, x, y, g["gold"] if k % 3 else g["gold_dark"])
        put(wp, whip, x, y + 1, g["gold_dark"])
    put(wp, whip, 41, 34, g["opal2"])
    put(wp, whip, 41, 35, g["opal1"])
    # die Schnur: Wellen, die nach rechts flacher und dünner werden
    x_end = BAR_X + 118
    prev = None
    for x in range(42, x_end):
        u = (x - 42) / (x_end - 42)
        y = bottom + 7 + 2.6 * math.sin(u * math.tau * 2.4 + 0.4) * (1.0 - 0.45 * u) - 2.0 * (1.0 - u) ** 3
        yy = int(round(y))
        thick = 2 if u < 0.55 else 1
        for d in range(thick):
            col = g["leather_light"] if d == 0 and thick == 2 else g["leather"]
            put(wp, whip, x, yy + d, col)
        if prev is not None and abs(yy - prev) > 1:
            for fill in range(min(yy, prev) + 1, max(yy, prev)):
                put(wp, whip, x, fill, g["leather"])
        prev = yy
    # die brennende Spitze
    for k, (dx, dy, col) in enumerate(((0, 0, g["f1"]), (1, 0, g["f2"]), (2, -1, g["f2"]), (2, 0, g["f3"]), (3, -1, g["f3"]),
                                       (1, -1, g["f1"]), (3, -2, g["f2"]), (4, -2, g["f3"]), (2, 1, g["f1"]))):
        put(wp, whip, x_end + dx, prev + dy, col)
    _outline(whip, OUTLINE)
    img.alpha_composite(whip)
    # Spitze der Leiste: ein großer Feueropal, von Gold gefasst
    tip = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    tp = tip.load()
    cy = BAR_Y + BAR_H // 2
    for k in range(9):
        x = 231 + k
        half = min(k * 1.3 + 0.5, (8 - k) * 0.8 + 0.5)
        for y in range(cy - 6, cy + 7):
            d = y + 0.5 - (cy + 0.5)
            if abs(d) > half:
                continue
            if k == 0:
                col = g["gold"] if d < 0 else g["gold_dark"]
            elif d < -0.6:
                col = g["opal3"] if k < 4 else g["opal2"]
            elif d < 0.6:
                col = g["opal2"]
            else:
                col = g["opal1"] if k < 5 else g["opal0"]
            put(tp, tip, x, y, col)
    put(tp, tip, 233, cy - 2, (255, 255, 255, 255))
    put(tp, tip, 235, cy - 1, g["flash_g"])
    put(tp, tip, 236, cy + 1, g["flash_b"])
    put(tp, tip, 234, cy + 2, g["flash_g"])
    _outline(tip, OUTLINE)
    img.alpha_composite(tip)


def post_ignara(img: Image.Image, pal: dict) -> None:
    """Nach dem Rahmen: Feueropale sitzen in der Ober- und Unterkante (statt der Nieten), jeder mit einem grünen oder blauen
    Blitz."""
    g = {k: c(pal, k) for k in pal}
    px = img.load()
    x0, x1, y0, y1 = BAR_X - 3, BAR_X + BAR_W + 2, BAR_Y - 3, BAR_Y + BAR_H + 2
    for i, x in enumerate(range(x0 + 9, x1 - 4, 22)):
        if any(abs(x - (BAR_X + round(BAR_W * f))) < 7 for f in GEMS):
            continue
        for y in (y0 + 1, y1 - 1):
            put(px, img, x, y, g["opal3"])
            put(px, img, x + 1, y, g["flash_g"] if (i + y) % 2 else g["flash_b"])
            put(px, img, x - 1, y, g["opal1"])
            put(px, img, x + 2, y, g["opal1"])
            put(px, img, x, y - 1 if y == y0 + 1 else y + 1, g["gold"])
            put(px, img, x + 1, y - 1 if y == y0 + 1 else y + 1, g["gold_dark"])


def fill_strip_ignara() -> Image.Image:
    """Füllung: züngelnde Flammen (unten am hellsten), darin hie und da der grüne oder blaue Blitz eines Feueropals."""
    pal = ELEMENTS["ignara"]
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    flash = (c(pal, "flash_g"), c(pal, "flash_b"))
    img = Image.new("RGBA", (256, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        for x in range(256):
            v = fill_value("ignara", x, y)
            if v >= 0.99:
                col = flash[int(hash01(x, y, 1311) * 2) % 2]
            elif v > 0.6:
                col = mix(f2, f3, ((v - 0.6) / 0.35) ** 1.6 * 0.75)
            else:
                col = mix(f0, f1, min(1.0, max(0.0, v) / 0.6 + 0.1))
            if y == BAR_H - 1:
                col = mix(col, f0, 0.4)
            px[x, y] = col
    return img


# =============================================================================================
# Phasmor, der Leerwandlerkönig (seit 1.5)
# =============================================================================================

def _phasmor_ghost(layer: Image.Image, g: dict, dx: int, dy: int, alpha: int) -> Image.Image:
    """Das Nachbild einer Form: dieselben Pixel, verschoben und blass türkis (er springt durch die Phasen)."""
    ghost = Image.new("RGBA", layer.size, CLEAR)
    src, gp = layer.load(), ghost.load()
    w, h = layer.size
    for y in range(h):
        for x in range(w):
            if src[x, y][3] and 0 <= x + dx < w and 0 <= y + dy < h:
                gp[x + dx, y + dy] = mix(g["acc"], g["glow"], 0.35)[:3] + (alpha,)
    return ghost


def _phasmor_shard(layer: Image.Image, g: dict, x0: float, y0: float, ang: float, length: float, width: float) -> None:
    """Ein schwebender Phasitsplitter: an beiden Enden spitz (unten kurz, oben lang), links die helle Fläche, in der Mitte
    die Kante im Licht, rechts die Schattenseite."""
    lp = layer.load()
    ca, sa = math.cos(ang), math.sin(ang)
    waist = 0.32
    reach = int(length + width + 2)
    for y in range(max(0, int(y0) - reach), min(FRAME_H, int(y0) + reach + 1)):
        for x in range(max(0, int(x0) - reach), min(FRAME_W, int(x0) + reach + 1)):
            dx, dy = x + 0.5 - x0, y + 0.5 - y0
            u = dx * ca + dy * sa
            v = -dx * sa + dy * ca
            if u < 0.0 or u > length:
                continue
            k = u / length
            half = width * (k / waist if k < waist else (1.0 - k) / (1.0 - waist)) + 0.35
            if abs(v) > half:
                continue
            s = v / max(half, 0.01)
            if s < -0.25:
                col = g["crystal2"]
            elif s < 0.25:
                col = g["crystal3"] if 0.15 < k < 0.85 else g["crystal2"]
            else:
                col = g["crystal1"]
            if k < 0.12:
                col = mix(col, g["crystal0"], 0.4)
            lp[x, y] = col


def deco_phasmor(img: Image.Image, pal: dict) -> None:
    """Phasmor, der Leerwandlerkönig: hinter dem Medaillon strahlt der türkise Kranz seines Kragens; über der Oberkante
    schweben Phasitsplitter (wie über seinen Schultern), jeder mit einem blassen Nachbild; unter der Leiste liegt seine
    Phasenklinge (Knauf mit Phasit, umwickelter Griff, Parierstange aus Purpur mit Splittern, die Kristallklinge mit
    leuchtendem Grat); als Spitze ein Phasit in einer Fassung aus Purpur, darüber und darunter seine Nachbilder."""
    g = {k: c(pal, k) for k in pal}
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    # der Strahlenkranz hinter dem Medaillon: abwechselnd lange und kurze Strahlen, an der Wurzel breiter
    rays = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    rp = rays.load()
    for i, deg in enumerate(range(-68, 69, 12)):
        if abs(deg) < 14:
            continue                                     # dort sitzt die Brücke zur Leiste
        a = math.radians(deg)
        reach = EMB_R + (9.0 if i % 2 == 0 else 5.5)
        r = EMB_R - 2.0
        while r <= reach:
            k = (r - EMB_R + 2.0) / (reach - EMB_R + 2.0)
            x = EMB_X + math.cos(a) * r
            y = EMB_Y + math.sin(a) * r
            col = g["crystal3"] if k > 0.8 else (g["ray"] if k > 0.4 else g["crystal2"])
            put(rp, rays, int(x), int(y), col)
            if k < 0.45:
                put(rp, rays, int(x + math.sin(a) * 0.9), int(y - math.cos(a) * 0.9), g["crystal1"])
            r += 0.5
    _outline(rays, OUTLINE)
    img.alpha_composite(rays)

    # schwebende Splitter über der Oberkante (nach rechts kleiner), mit Nachbild
    shards = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    for i, x in enumerate(range(BAR_X + 8, BAR_X + BAR_W - 6, 15)):
        if any(abs(x - (BAR_X + round(BAR_W * f))) < 9 for f in GEMS):
            continue
        rel = (x - BAR_X) / BAR_W
        length = 10.5 - 4.5 * rel + 2.0 * hash01(x, 1521)
        tilt = (hash01(x, 1523) - 0.5) * 34.0
        hover = 4 + round(hash01(x, 1525) * 2)
        _phasmor_shard(shards, g, x + 0.5, top - hover + 0.5, math.radians(-90.0 + tilt), length, 1.25 + 0.5 * (1.0 - rel))
    img.alpha_composite(_phasmor_ghost(shards, g, -3, 1, 80))
    _outline(shards, OUTLINE)
    img.alpha_composite(shards)

    # die Phasenklinge unter der Leiste
    blade = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    bp = blade.load()
    cy = bottom + 6
    # Knauf (Phasit) und Griff (umwickelt)
    for dx, dy, col in ((0, 0, g["crystal2"]), (1, 0, g["crystal3"]), (0, -1, g["crystal1"]), (1, -1, g["crystal2"]),
                        (0, 1, g["crystal0"]), (1, 1, g["crystal1"]), (-1, 0, g["crystal1"])):
        put(bp, blade, 31 + dx, cy + dy, col)
    for x in range(33, 43):
        put(bp, blade, x, cy - 1, g["grip_light"] if x % 3 == 0 else g["grip"])
        put(bp, blade, x, cy, g["grip"] if x % 3 else g["grip_light"])
        put(bp, blade, x, cy + 1, mix(g["grip"], OUTLINE, 0.4))
    # Parierstange: Purpur, in der Mitte ein Phasit, oben und unten je ein Splitter
    for y in range(cy - 4, cy + 5):
        put(bp, blade, 43, y, g["purpur_light"] if y < cy else g["purpur"])
        put(bp, blade, 44, y, g["purpur"] if y < cy else g["purpur_dark"])
    put(bp, blade, 43, cy, g["crystal3"])
    put(bp, blade, 44, cy, g["crystal2"])
    for sx, sy, sd in ((44, cy - 5, -1), (44, cy + 5, 1)):
        for k in range(3):
            put(bp, blade, sx + k, sy + sd * k, g["crystal2"] if k < 2 else g["crystal3"])
    # die Klinge: geschliffener Kristall, oben die helle Fase, in der Mitte der leuchtende Grat, unten die Schattenseite
    x0, x_end = 45, BAR_X + 104
    for x in range(x0, x_end + 1):
        u = (x - x0) / (x_end - x0)
        half = 2.4 if u < 0.82 else 2.4 * (1.0 - (u - 0.82) / 0.18)
        for dy in range(-2, 3):
            if abs(dy) > half + 0.25:
                continue
            if dy == -2:
                col = g["crystal3"]
            elif dy == -1:
                col = g["crystal2"]
            elif dy == 0:
                col = g["glow"] if hash01(x, 1527) > 0.18 else (255, 255, 255, 255)
            elif dy == 1:
                col = g["crystal1"]
            else:
                col = g["crystal0"]
            if u > 0.82 and dy != 0:
                col = mix(col, g["crystal3"], 0.3)
            put(bp, blade, x, cy + dy, col)
    _outline(blade, OUTLINE)
    img.alpha_composite(blade)

    # die Spitze: ein Phasit, gefasst von zwei Purpurkrallen, darüber und darunter sein Nachbild
    tip = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    ty = BAR_Y + BAR_H / 2
    _keeper_crystal(tip, g, 229.0, ty, 0.0, 10.6, 2.6, 1541)
    tp = tip.load()
    for y in (int(ty) - 4, int(ty) + 4):
        for x in (232, 233):
            put(tp, tip, x, y, g["purpur_light"] if y < ty else g["purpur"])
    put(tp, tip, 234, int(ty) - 4, g["purpur_dark"])
    put(tp, tip, 234, int(ty) + 4, g["purpur_dark"])
    ghosts = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    _keeper_crystal(ghosts, g, 230.0, ty - 3.5, math.radians(-30.0), 7.0, 1.6, 1543)
    _keeper_crystal(ghosts, g, 230.0, ty + 3.5, math.radians(30.0), 6.0, 1.5, 1545)
    img.alpha_composite(_phasmor_ghost(ghosts, g, 0, 0, 120))
    _outline(tip, OUTLINE)
    img.alpha_composite(tip)


def _sordin_pipe(layer: Image.Image, g: dict, x0: int, base: int, height: int, width: int = 3) -> None:
    """Eine Orgelpfeife: Kupfer, grün angelaufen, links im Licht; oben offen (dunkel), unten der Mund mit Sculk-Leuchten."""
    lp = layer.load()
    for y in range(base - height, base + 1):
        for k in range(width):
            x = x0 + k
            if y == base - height:
                col = g["copper_light"] if k == 0 else g["copper"]                     # der Rand oben
            elif y == base - height + 1 and 0 < k < width - 1:
                col = OUTLINE                                                          # oben offen
            elif base - 4 <= y <= base - 3 and 0 < k < width - 1:
                col = g["acc"] if y == base - 3 else OUTLINE                           # der Mund
            else:
                patina = hash01(x, y, 1621) > 0.55
                col = (g["verdigris_light"] if k == 0 else g["verdigris"] if k < width - 1 else g["verdigris_dark"]) if patina \
                    else (g["copper_light"] if k == 0 else g["copper"] if k < width - 1 else g["copper_dark"])
            put(lp, layer, x, y, col)


def _sordin_note(layer: Image.Image, g: dict, x: int, y: int, flag: bool) -> None:
    """Eine Achtelnote (Kopf, Hals, Fähnchen) in Türkis."""
    lp = layer.load()
    for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
        put(lp, layer, x + dx, y + dy, g["glow"] if (dx, dy) == (0, 0) else g["acc"])
    for k in range(1, 5):
        put(lp, layer, x + 2, y + 1 - k, g["acc"])
    if flag:
        put(lp, layer, x + 3, y - 3, g["acc"])
        put(lp, layer, x + 4, y - 2, g["f1"])


def deco_sordin(img: Image.Image, pal: dict) -> None:
    """Sordin, der Kantor der Stille: hinter dem Medaillon der Fächer seiner Orgelpfeifen (die Mitte am höchsten); über der
    Oberkante schweben Noten, von blassen Echoringen umgeben; unter der Leiste liegt seine Stimmgabel (Knauf mit Hallkristall,
    seegrüner Griff, Bügel aus Kupfer, zwei Zinken aus Hallkristall); als Spitze ein Hallkristall in Kupfer."""
    g = {k: c(pal, k) for k in pal}
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    # der Pfeifenfächer hinter dem Medaillon
    pipes = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    for i, (dx, h) in enumerate(((-14, 11), (-9, 15), (-4, 19), (1, 21), (6, 19), (11, 15), (16, 11))):
        _sordin_pipe(pipes, g, EMB_X + dx - 1, EMB_Y - 10 + (3 if i in (0, 6) else 1 if i in (1, 5) else 0), h)
    _outline(pipes, OUTLINE)
    img.alpha_composite(pipes)

    # schwebende Noten über der Oberkante, mit Echoringen
    notes = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    rings = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    rp = rings.load()
    for i, x in enumerate(range(BAR_X + 10, BAR_X + BAR_W - 8, 17)):
        if any(abs(x - (BAR_X + round(BAR_W * f))) < 9 for f in GEMS):
            continue
        hover = 4 + round(hash01(x, 1623) * 3)
        y = top - hover
        _sordin_note(notes, g, x, y, i % 2 == 0)
        r = 3.5 + hash01(x, 1625) * 1.5
        for k in range(24):
            a = math.tau * k / 24
            put(rp, rings, int(x + 1 + math.cos(a) * r), int(y + math.sin(a) * r * 0.6), g["f1"][:3] + (110,))
    img.alpha_composite(rings)
    _outline(notes, OUTLINE)
    img.alpha_composite(notes)

    # die Stimmgabel unter der Leiste
    fork = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    fp = fork.load()
    cy = bottom + 6
    for dx, dy, col in ((0, 0, g["crystal2"]), (1, 0, g["crystal3"]), (0, -1, g["crystal1"]), (1, -1, g["crystal2"]),
                        (0, 1, g["crystal0"]), (1, 1, g["crystal1"]), (-1, 0, g["crystal1"])):
        put(fp, fork, 31 + dx, cy + dy, col)
    for x in range(33, 47):
        put(fp, fork, x, cy - 1, g["leather_light"] if x % 3 == 0 else g["leather"])
        put(fp, fork, x, cy, g["leather"] if x % 3 else g["leather_light"])
        put(fp, fork, x, cy + 1, mix(g["leather"], OUTLINE, 0.4))
    for x in range(47, 52):
        for dy in (-1, 0, 1):
            put(fp, fork, x, cy + dy, g["copper_light"] if dy < 0 else g["copper"] if dy == 0 else g["copper_dark"])
    # der Bügel (senkrecht) und die beiden Zinken nach rechts
    for y in range(cy - 4, cy + 5):
        put(fp, fork, 52, y, g["copper_light"] if y < cy else g["copper"])
        put(fp, fork, 53, y, g["copper"] if y < cy else g["copper_dark"])
    x_end = BAR_X + 100
    for zy in (cy - 3, cy + 3):
        for x in range(54, x_end + 1):
            ring = (x - 54) % 9 == 4
            put(fp, fork, x, zy - 1, g["crystal3"] if ring else g["crystal2"])
            put(fp, fork, x, zy, g["glow"] if ring else g["crystal1"])
    _outline(fork, OUTLINE)
    img.alpha_composite(fork)

    # die Spitze: ein Hallkristall, gefasst von zwei Kupferkrallen
    tip = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    ty = BAR_Y + BAR_H / 2
    _keeper_crystal(tip, {**g, "crystal0": g["crystal0"], "crystal1": g["crystal1"], "crystal2": g["crystal2"], "crystal3": g["crystal3"]},
                    229.0, ty, 0.0, 10.6, 2.6, 1631)
    tp = tip.load()
    for y in (int(ty) - 4, int(ty) + 4):
        for x in (232, 233):
            put(tp, tip, x, y, g["copper_light"] if y < ty else g["copper"])
    put(tp, tip, 234, int(ty) - 4, g["copper_dark"])
    put(tp, tip, 234, int(ty) + 4, g["copper_dark"])
    _outline(tip, OUTLINE)
    img.alpha_composite(tip)


def post_sordin(img: Image.Image, pal: dict) -> None:
    """Nach dem Rahmen: statt der Nieten kleine Kupferknöpfe mit Grünspan in Ober- und Unterkante."""
    g = {k: c(pal, k) for k in pal}
    px = img.load()
    x0, x1, y0, y1 = BAR_X - 3, BAR_X + BAR_W + 2, BAR_Y - 3, BAR_Y + BAR_H + 2
    for x in range(x0 + 9, x1 - 4, 22):
        if any(abs(x - (BAR_X + round(BAR_W * f))) < 7 for f in GEMS):
            continue
        for y in (y0 + 1, y1 - 1):
            put(px, img, x, y, g["copper_light"])
            put(px, img, x + 1, y, g["verdigris"])
            put(px, img, x - 1, y, g["copper_dark"])


def _rk_pennant(layer: Image.Image, g: dict, x: int, y: int, w: int, h: int, right: bool) -> None:
    """Ein Wimpel mit Schwalbenschwanz: oben Gold, unten Schwarz (geteilt), an der Stange bei (x, y), weht nach rechts oder
    links."""
    lp = layer.load()
    for dx in range(w):
        for dy in range(h):
            # der Schwalbenschwanz: hinten ein Keil heraus
            depth = dx - (w - 4)
            if depth > 0 and abs(dy - (h - 1) / 2) < depth * 0.6:
                continue
            col = (g["gold_light"] if dy == 0 else g["gold"]) if dy < h / 2 else (g["raven_light"] if dy == h // 2 else g["raven"])
            put(lp, layer, x + (dx if right else -dx), y + dy + (dx // 4 if right else dx // 4), col)


def _rk_raven(layer: Image.Image, g: dict, x: int, y: int, flip: bool) -> None:
    """Ein fliegender Rabe (von der Seite, Flügel hoch): schwarz, der Schnabel grau, ein goldener Augenpunkt."""
    art = ["....##.......",
           ".....###.....",
           "......####...",
           ".##..######..",
           "#B#########..",
           ".....#######.",
           "......##..##.",
           ]
    lp = layer.load()
    for dy, row in enumerate(art):
        for dx, ch in enumerate(row):
            if ch == ".":
                continue
            xx = x + (12 - dx if flip else dx)
            if ch == "B":
                col = g["gold"]
            elif dx == 0:
                col = g["steel_dark"]
            else:
                col = g["raven_light"] if dy <= 2 and ch == "#" and dx % 3 == 0 else g["raven"]
            put(lp, layer, xx, y + dy, col)


def deco_robber_knight(img: Image.Image, pal: dict) -> None:
    """Kuno von Rabenfels, der Raubritter: hinter dem Medaillon zwei gekreuzte Lanzen, deren Spitzen oben mit Wimpeln
    hervorschauen; über der Oberkante Zinnen aus dunklem Stein mit goldenen Deckeln und zwei fliegende Raben; unter der Leiste
    liegt seine Lanze (Griff, Brechscheibe, Schaft mit goldenen Bändern, die Spitze, ein Wimpel); als Spitze ein Rabenkopf."""
    g = {k: c(pal, k) for k in pal}
    top, bottom = BAR_Y - 3, BAR_Y + BAR_H + 2

    # zwei gekreuzte Lanzen hinter dem Medaillon (nur außerhalb des Medaillons zu sehen)
    lances = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    lp = lances.load()
    for sx in (-1, 1):
        for k in range(-30, 31):
            x = EMB_X + sx * k * 0.72
            y = EMB_Y - k * 0.72
            if k > 22:                                                 # die Spitze: Stahl, spitz zulaufend
                for o in (-1, 0, 1)[: 3 if k < 27 else 1]:
                    put(lp, lances, int(round(x + o * 0.7)), int(round(y + o * 0.7 * sx)),
                        g["steel_light"] if o < 0 else g["steel"] if o == 0 else g["steel_dark"])
                continue
            band = k in (12, 18, 21)
            for o in (0, 1):
                col = (g["gold_light"] if o == 0 else g["gold_dark"]) if band else (g["wood_light"] if o == 0 else g["wood"])
                put(lp, lances, int(round(x + o * 0.7 * sx)), int(round(y + o * 0.7)), col)
    _rk_pennant(lances, g, EMB_X + 15, EMB_Y - 19, 9, 6, True)
    _rk_pennant(lances, g, EMB_X - 15, EMB_Y - 19, 9, 6, False)
    _outline(lances, OUTLINE)
    img.alpha_composite(lances)

    # Zinnen über der Oberkante (dunkler Stein, goldener Deckel), dazwischen frei; nicht über den Edelsteinen
    merlons = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    mp = merlons.load()
    for x0 in range(BAR_X + 4, BAR_X + BAR_W - 6, 13):
        if any(abs(x0 + 3 - (BAR_X + round(BAR_W * f))) < 9 for f in GEMS):
            continue
        for y in range(top - 5, top):
            for x in range(x0, x0 + 6):
                if y == top - 5:
                    col = g["gold_light"] if x == x0 else g["gold"] if x < x0 + 5 else g["gold_dark"]
                elif x == x0:
                    col = g["stone_light"]
                elif x == x0 + 5:
                    col = g["stone_dark"]
                else:
                    col = g["stone"] if hash01(x, y, 1801) > 0.25 else g["stone_dark"]
                put(mp, merlons, x, y, col)
    _outline(merlons, OUTLINE)
    img.alpha_composite(merlons)

    # zwei Raben fliegen über der Leiste
    ravens = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    _rk_raven(ravens, g, BAR_X + 70, 1, False)
    _rk_raven(ravens, g, BAR_X + 132, 3, True)
    _outline(ravens, OUTLINE)
    img.alpha_composite(ravens)

    # die Lanze unter der Leiste
    lance = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    lp = lance.load()
    cy = bottom + 6
    for x in range(BAR_X - 2, BAR_X + 14):                             # der Griff (Leder) mit Knauf
        put(lp, lance, x, cy - 1, g["raven_light"] if x % 3 == 0 else g["raven"])
        put(lp, lance, x, cy, g["raven"] if x % 3 else g["raven_light"])
    for dy, half in ((-3, 1), (-2, 2), (-1, 3), (0, 3), (1, 3), (2, 2), (3, 1)):   # die Brechscheibe (ein Kegel aus Stahl)
        for k in range(half + 2):
            x = BAR_X + 14 + k
            put(lp, lance, x, cy + dy, g["steel_light"] if dy < 0 else g["steel"] if dy == 0 else g["steel_dark"])
    x_tip = BAR_X + 150
    for x in range(BAR_X + 19, x_tip):                                 # der Schaft, zur Spitze hin dünner, mit goldenen Bändern
        band = (x - BAR_X) % 24 < 2
        thin = x > BAR_X + 110
        put(lp, lance, x, cy - 1, (g["gold_light"] if band else g["wood_light"]))
        if not thin:
            put(lp, lance, x, cy, g["gold_dark"] if band else g["wood"])
    for k in range(10):                                                # die Spitze
        x = x_tip + k
        put(lp, lance, x, cy - 1, g["steel_light"])
        if k < 6:
            put(lp, lance, x, cy, g["steel_dark"])
        if k < 3:
            put(lp, lance, x, cy - 2, g["steel"])
    pen = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    _rk_pennant(pen, g, x_tip - 4, cy, 14, 5, False)
    lance.alpha_composite(pen)
    _outline(lance, OUTLINE)
    img.alpha_composite(lance)

    # die Spitze: ein goldener Rabenkopf von der Seite (das Wappentier, wie aus Messing gegossen), Auge und Schnabel dunkel
    tip = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    tp = tip.load()
    ty = int(BAR_Y + BAR_H / 2) - 5
    head = ["...####......",
            "..######.....",
            ".###E####....",
            "##########BB.",
            "###########BB",
            "##########BB.",
            ".#######.....",
            "..#####......",
            "...###......."]
    for dy, row in enumerate(head):
        for dx, ch in enumerate(row):
            if ch == ".":
                continue
            if ch == "E":
                col = g["raven"]
            elif ch == "B":
                col = g["steel_light"] if dy < 4 else g["steel"] if dy == 4 else g["steel_dark"]
            else:
                col = g["gold_light"] if dy < 2 or dx == 0 else g["gold"] if dy < 6 else g["gold_dark"]
            put(tp, tip, 226 + dx, ty + dy, col)
    _outline(tip, OUTLINE)
    img.alpha_composite(tip)


def post_robber_knight(img: Image.Image, pal: dict) -> None:
    """Nach dem Rahmen: statt der Nieten goldene Buckel in Ober- und Unterkante."""
    g = {k: c(pal, k) for k in pal}
    px = img.load()
    x0, x1, y0, y1 = BAR_X - 3, BAR_X + BAR_W + 2, BAR_Y - 3, BAR_Y + BAR_H + 2
    for x in range(x0 + 9, x1 - 4, 22):
        if any(abs(x - (BAR_X + round(BAR_W * f))) < 7 for f in GEMS):
            continue
        for y in (y0 + 1, y1 - 1):
            put(px, img, x, y, g["gold_light"])
            put(px, img, x + 1, y, g["gold"])
            put(px, img, x - 1, y, g["gold_dark"])


def post_phasmor(img: Image.Image, pal: dict) -> None:
    """Nach dem Rahmen: statt der Nieten kleine Phasite in Purpur in Ober- und Unterkante."""
    g = {k: c(pal, k) for k in pal}
    px = img.load()
    x0, x1, y0, y1 = BAR_X - 3, BAR_X + BAR_W + 2, BAR_Y - 3, BAR_Y + BAR_H + 2
    for x in range(x0 + 9, x1 - 4, 22):
        if any(abs(x - (BAR_X + round(BAR_W * f))) < 7 for f in GEMS):
            continue
        for y in (y0 + 1, y1 - 1):
            put(px, img, x, y, g["crystal3"])
            put(px, img, x + 1, y, g["crystal2"])
            put(px, img, x - 1, y, g["purpur_dark"])
            put(px, img, x + 2, y, g["purpur_dark"])


# =============================================================================================
# Die Sternenweberin (seit 0.99)
# =============================================================================================

def _weaver_leg(layer: Image.Image, g: dict, pts: list, width: float) -> None:
    """Ein gegliedertes Spinnenbein durch die Punkte pts (Hüfte, Knie, … Fuß): jedes Glied etwas dünner, oben im Licht,
    unten im Schatten; die Gelenke leuchten in Sternenlicht, am Fuß eine Kralle."""
    lp = layer.load()
    for i in range(len(pts) - 1):
        (x0, y0), (x1, y1) = pts[i], pts[i + 1]
        w0 = width * (1.0 - 0.22 * i)
        w1 = width * (1.0 - 0.22 * (i + 1)) if i + 1 < len(pts) - 1 else 0.55
        length = math.hypot(x1 - x0, y1 - y0)
        ax, ay = (x1 - x0) / length, (y1 - y0) / length
        for y in range(max(0, int(min(y0, y1) - width - 2)), min(FRAME_H, int(max(y0, y1) + width + 3))):
            for x in range(max(0, int(min(x0, x1) - width - 2)), min(FRAME_W, int(max(x0, x1) + width + 3))):
                dx, dy = x + 0.5 - x0, y + 0.5 - y0
                t = (dx * ax + dy * ay) / length
                if not -0.05 <= t <= 1.05:
                    continue
                off = -dx * ay + dy * ax
                w = w0 + (w1 - w0) * max(0.0, min(1.0, t))
                if abs(off) > w:
                    continue
                s = off / max(w, 0.01)
                col = g["chitin3"] if s < -0.45 else g["chitin2"] if s < 0.15 else g["chitin1"]
                if (t * length) % 4.0 < 0.9 and abs(s) < 0.6:          # feine Ringe am Glied
                    col = mix(col, g["chitin0"], 0.5)
                lp[x, y] = col
    for (x, y) in pts[1:-1]:                                            # leuchtende Gelenke
        for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
            lp[int(x) + dx - 1, int(y) + dy - 1] = g["star2"] if (dx, dy) != (0, 0) else g["glow"]
    fx, fy = pts[-1]                                                    # die Kralle
    lp[int(fx), int(fy)] = g["m2"]


def _weaver_star(px, img, x: int, y: int, r: int, g: dict) -> None:
    """Ein Stern mit vier langen und vier kurzen Zacken, die Mitte weiß."""
    for k in range(-r, r + 1):
        shade = g["star"] if abs(k) < r else g["star2"]
        put(px, img, x + k, y, shade)
        put(px, img, x, y + k, shade)
    if r >= 2:
        for dx, dy in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
            put(px, img, x + dx, y + dy, g["star2"])
    put(px, img, x, y, g["glow"])


def deco_star_weaver(img: Image.Image, pal: dict) -> None:
    """Die Sternenweberin: hinter dem Medaillon greifen vier gegliederte Spinnenbeine nach oben und unten (die Gelenke leuchten);
    als Spitze ein Knoten aus Sternenseide, in dem ein Stern hängt; unter der Leiste hängen an Fäden eingesponnene Kokons und
    Sterne. (Die Sterne über der Oberkante folgen in post_star_weaver.)"""
    g = {k: c(pal, k) for k in pal}
    legs = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    _weaver_leg(legs, g, [(33.0, 12.0), (45.0, 1.5), (60.0, 7.0), (66.0, 12.5)], 2.1)
    _weaver_leg(legs, g, [(37.0, 15.0), (56.0, 4.5), (75.0, 11.0), (80.0, 15.0)], 1.9)
    _weaver_leg(legs, g, [(33.0, 32.0), (45.0, 42.5), (60.0, 37.0), (66.0, 31.5)], 2.1)
    _weaver_leg(legs, g, [(37.0, 29.0), (56.0, 39.5), (75.0, 33.0), (80.0, 29.0)], 1.9)
    _outline(legs, OUTLINE)
    img.alpha_composite(legs)
    # die Spitze: Fäden laufen von der Leiste zu einem Knoten, in dem ein Stern hängt
    tip = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    tp = tip.load()
    cx, cy = 236, 22
    for (x0, y0) in ((230, 19), (230, 25), (230, 22)):
        n = 12
        for k in range(n + 1):
            t = k / n
            put(tp, tip, int(round(x0 + (cx - x0) * t)), int(round(y0 + (cy - y0) * t)), g["silk"] if k % 3 else g["silk_dark"])
    for a in range(0, 360, 45):                                         # der Knoten: kurze Fäden ringsum
        for r in (4, 5):
            put(tp, tip, int(round(cx + math.cos(math.radians(a)) * r)), int(round(cy + math.sin(math.radians(a)) * r)), g["silk_dark"])
    _weaver_star(tp, tip, cx, cy, 3, g)
    _outline(tip, OUTLINE)
    img.alpha_composite(tip)
    # unter der Leiste: Fäden mit Kokons und Sternen
    px = img.load()
    bottom = BAR_Y + BAR_H + 2
    hang = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    hp = hang.load()
    for i, x0 in enumerate((92, 113, 139, 160, 184, 203, 221)):
        if any(abs(x0 - (BAR_X + round(BAR_W * f))) < 6 for f in GEMS):
            continue
        length = 2 + round(hash01(x0, 941) * 5)
        for k in range(length):
            put(hp, hang, x0, bottom + 1 + k, g["silk"] if k % 2 == 0 else g["silk_dark"])
        y = bottom + 1 + length
        if i % 2 == 0:                                                  # ein Kokon
            for dy, half in enumerate((0, 1, 1, 1, 0)):
                for dx in range(-half, half + 1):
                    put(hp, hang, x0 + dx, y + dy, g["silk"] if (dx + dy) % 2 == 0 else g["silk_dark"])
        else:                                                           # ein Stern
            _weaver_star(hp, hang, x0, y + 2, 2, g)
    _outline(hang, OUTLINE)
    hl = hang.load()
    for y in range(FRAME_H):
        for x in range(FRAME_W):
            if hl[x, y][3] and y > bottom:
                px[x, y] = hl[x, y]


def post_star_weaver(img: Image.Image, pal: dict) -> None:
    """Nach dem Rahmen: kleine Sterne funkeln über der Oberkante, dazwischen ein feiner Faden."""
    g = {k: c(pal, k) for k in pal}
    px = img.load()
    top = BAR_Y - 3
    for x0 in range(BAR_X + 40, BAR_X + BAR_W - 8, 19):
        if any(abs(x0 - (BAR_X + round(BAR_W * f))) < 8 for f in GEMS) or hash01(x0, 951) < 0.3:
            continue
        y = top - 3 - round(hash01(x0, 953) * 6)
        r = 2 if hash01(x0, 957) > 0.55 else 1
        for (dx, dy) in [(k, 0) for k in range(-r - 1, r + 2)] + [(0, k) for k in range(-r - 1, r + 2)]:
            if px[x0 + dx, y + dy][3] == 0:
                put(px, img, x0 + dx, y + dy, OUTLINE)
        _weaver_star(px, img, x0, y, r, g)


# =============================================================================================
# Der Stundenwächter (seit 1.01)
# =============================================================================================

def _clock_gear(layer: Image.Image, g: dict, cx: float, cy: float, r: float, teeth: int, turn: float) -> None:
    """Ein Zahnrad von vorn: Zähne, Kranz, vier Speichen, Nabe mit Bernstein (Licht von oben links)."""
    lp = layer.load()
    for y in range(int(cy - r - 2), int(cy + r + 3)):
        for x in range(int(cx - r - 2), int(cx + r + 3)):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            d = math.hypot(dx, dy)
            if d > r:
                continue
            a = math.atan2(dy, dx) - turn
            root = r - 1.3
            if d > root:
                k = (a / math.tau * teeth) % 1.0
                if abs(k - 0.5) > 0.25:
                    continue
            lit = -(dx + dy) / max(0.5, d) * 0.5
            if d < 1.2:
                col = g["acc"] if d < 0.8 else g["brass1"]
            elif d > root - 1.0:
                col = mix(g["brass1"], g["brass3"], 0.35 + 0.4 * lit) if d > root else mix(g["brass0"], g["brass2"], 0.45 + 0.4 * lit)
            else:
                spoke = any(abs(math.sin(a - i * math.pi / 2)) * d < 0.7 and math.cos(a - i * math.pi / 2) > 0 for i in range(4))
                if not spoke:
                    continue
                col = mix(g["brass0"], g["brass2"], 0.4 + 0.3 * lit)
            put(lp, layer, x, y, col)


def deco_hour_warden(img: Image.Image, pal: dict) -> None:
    """Der Stundenwächter: hinter dem Medaillon ein Kranz aus zwölf Zähnen (wie ein Zahnrad, die vier Hauptstunden in Bernstein);
    über der Oberkante greifen Zahnräder ineinander; als Spitze ein gebläuter Minutenzeiger mit durchbrochener Raute; unter der
    Leiste hängen an Ketten Uhrgewichte und in der Mitte ein Pendel mit Bernsteinscheibe."""
    g = {k: c(pal, k) for k in pal}
    # der Zahnkranz hinter dem Medaillon
    ring = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    rp = ring.load()
    for k in range(12):
        a = k * math.tau / 12 - math.pi / 2
        for dr in (0.0, 1.0, 2.0):
            for dw in (-1.0, 0.0, 1.0):
                x = EMB_X + math.cos(a) * (EMB_R + 0.8 + dr) - math.sin(a) * dw
                y = EMB_Y + math.sin(a) * (EMB_R + 0.8 + dr) + math.cos(a) * dw
                main = k % 3 == 0
                col = (g["acc"] if dr < 2.0 else g["glow"]) if main else (g["brass2"] if dw < 0 else g["brass1"])
                put(rp, ring, int(round(x - 0.5)), int(round(y - 0.5)), col)
    _outline(ring, OUTLINE)
    img.alpha_composite(ring)
    # Zahnräder über der Oberkante
    gears = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    for (gx, gy, r, teeth, turn) in ((70.0, 10.0, 5.6, 10, 0.2), (80.5, 8.0, 3.6, 7, 0.5), (205.0, 10.5, 4.8, 9, 0.1),
                                     (214.0, 8.5, 3.2, 6, 0.7)):
        if any(abs(gx - (BAR_X + round(BAR_W * f))) < 9 for f in GEMS):
            continue
        _clock_gear(gears, g, gx, gy, r, teeth, turn)
    _outline(gears, OUTLINE)
    img.alpha_composite(gears)
    # die Spitze: ein gebläuter Minutenzeiger (Schaft, durchbrochene Raute, Spitze) – nach rechts
    tip = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    tp = tip.load()
    for x in range(230, 240):
        t = (x - 230) / 9.0
        half = 0.7 if t < 0.15 else max(0.5, 3.4 * (1.0 - abs(t - 0.5) / 0.5))
        for y in range(17, 29):
            d = abs(y + 0.5 - 22.5)
            if d > half:
                continue
            if 0.35 < t < 0.65 and d < half - 1.4:
                continue                                                # durchbrochen
            put(tp, tip, x, y, g["blued2"] if y < 22 else g["blued1"] if d > 0.6 else g["brass2"])
    _outline(tip, OUTLINE)
    img.alpha_composite(tip)
    # unter der Leiste: Ketten mit Uhrgewichten, in der Mitte das Pendel
    px = img.load()
    bottom = BAR_Y + BAR_H + 2
    hang = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    hp = hang.load()
    for i, x0 in enumerate((96, 120, 162, 186, 212)):
        if any(abs(x0 - (BAR_X + round(BAR_W * f))) < 6 for f in GEMS):
            continue
        length = 2 + round(hash01(x0, 1201) * 4)
        for k in range(length):
            put(hp, hang, x0, bottom + 1 + k, g["brass2"] if k % 2 == 0 else g["brass0"])
        y = bottom + 1 + length
        for dy, half in enumerate((1, 1, 1, 1, 0)):                     # ein Uhrgewicht wie ein Tannenzapfen
            for dx in range(-half, half + 1):
                put(hp, hang, x0 + dx, y + dy, g["brass2"] if (dx + dy) % 2 == 0 else g["brass1"])
    # das Pendel (in der Mitte der Leiste)
    px0 = BAR_X + BAR_W // 2 + 3
    if not any(abs(px0 - (BAR_X + round(BAR_W * f))) < 6 for f in GEMS):
        for k in range(5):
            put(hp, hang, px0, bottom + 1 + k, g["brass1"])
        for dy in range(-2, 3):
            for dx in range(-2, 3):
                if abs(dx) + abs(dy) <= 3:
                    d = abs(dx) + abs(dy)
                    put(hp, hang, px0 + dx, bottom + 8 + dy, g["glow"] if d == 0 else g["acc"] if d < 3 else g["brass2"])
    _outline(hang, OUTLINE)
    hl = hang.load()
    for y in range(FRAME_H):
        for x in range(FRAME_W):
            if hl[x, y][3] and y > bottom:
                px[x, y] = hl[x, y]


# =============================================================================================
# Der Steinhüter (seit 0.96)
# =============================================================================================

def _keeper_crystal(layer: Image.Image, g: dict, x0: float, y0: float, ang: float, length: float, width: float,
                    seed: int) -> None:
    """Ein Mondsteinkristall von der Seite: ein sechskantiges Prisma (oben links die helle Fläche, in der Mitte die Kante im
    Licht, unten rechts die Schattenseite), das in eine Spitze ausläuft; am Fuß dunkler, darin ein paar Glitzerpunkte."""
    lp = layer.load()
    ca, sa = math.cos(ang), math.sin(ang)
    tip0 = 0.68
    reach = int(length + width + 2)
    for y in range(max(0, int(y0) - reach), min(FRAME_H, int(y0) + reach + 1)):
        for x in range(max(0, int(x0) - reach), min(FRAME_W, int(x0) + reach + 1)):
            dx, dy = x + 0.5 - x0, y + 0.5 - y0
            u = dx * ca + dy * sa
            v = -dx * sa + dy * ca
            if u < 0.0 or u > length:
                continue
            k = u / length
            half = width if k < tip0 else width * (1.0 - (k - tip0) / (1.0 - tip0)) + 0.2
            if abs(v) > half:
                continue
            s = v / max(half, 0.01)
            if s < -0.3:
                col = g["crystal2"]
            elif s < 0.2:
                col = g["crystal3"] if k > 0.18 else g["crystal2"]
            else:
                col = g["crystal1"]
            if k < 0.22:
                col = mix(col, g["crystal0"], 0.55 * (1.0 - k / 0.22))
            if k > tip0 and s < 0.2:
                col = mix(col, g["crystal3"], 0.45)
            if abs(s) > 0.78 and half > 1.1:
                col = mix(col, g["crystal0"], 0.35)
            if hash01(x, y, seed, 611) > 0.93 and abs(s) < 0.6 and k > 0.25:
                col = g["crystal3"]
            lp[x, y] = col


def deco_keeper(img: Image.Image, pal: dict) -> None:
    """Der Steinhüter: hinter dem Medaillon wächst sein Kristallkamm hervor (oben vier Mondsteinkristalle, fächerförmig,
    unten zwei kleine); unter der Leiste hängen Leuchtranken mit Blättchen und je einer leuchtenden Blüte, dazwischen
    hängt Moos über die Kante; als Spitze ein liegender Kristall mit zwei kleinen. (Moos und Kristalle auf dem Rahmen
    folgen in post_keeper, über dem Rahmen.)"""
    g = {k: c(pal, k) for k in pal}
    bottom = BAR_Y + BAR_H + 2

    # der Kristallkamm hinter dem Medaillon
    comb = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    for i, (x0, y0, ang, length, width) in enumerate((
            (31.0, 15.0, -80.0, 14.0, 2.2), (33.5, 15.0, -57.0, 16.5, 2.8), (36.0, 16.0, -35.0, 17.5, 2.4),
            (38.0, 17.0, -15.0, 12.5, 1.8), (36.0, 30.0, 30.0, 11.0, 1.9), (34.0, 33.0, 56.0, 9.0, 1.6))):
        _keeper_crystal(comb, g, x0, y0, math.radians(ang), length, width, i + 1)
    _outline(comb, OUTLINE)
    img.alpha_composite(comb)

    # die Spitze: ein liegender Kristall, dazu zwei kleine schräg darüber und darunter
    tip = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    _keeper_crystal(tip, g, 229.0, 22.5, 0.0, 10.4, 2.4, 11)
    _keeper_crystal(tip, g, 230.0, 19.5, math.radians(-38.0), 7.0, 1.5, 12)
    _keeper_crystal(tip, g, 230.0, 25.5, math.radians(34.0), 6.0, 1.4, 13)
    _outline(tip, OUTLINE)
    img.alpha_composite(tip)

    # Leuchtranken unter der Leiste: sie schwingen ein wenig, tragen Blättchen und enden in einer Blüte
    px = img.load()
    vines = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    vp = vines.load()
    for i, x0 in enumerate((57, 70, 96, 108, 127, 151, 175, 186, 203, 221)):
        length = 4 + round(hash01(x0, 613) * 7)
        phase = hash01(x0, 617) * math.tau
        x = x0
        for k in range(length):
            x = x0 + round(0.9 * math.sin(k * 0.75 + phase))
            put(vp, vines, x, bottom + 1 + k, g["vine_light"] if k % 3 == 1 else g["vine"])
            if k % 3 == 2 and k < length - 1:
                side = 1 if (k // 3 + i) % 2 else -1
                put(vp, vines, x + side, bottom + 1 + k, mix(g["vine_light"], g["moss2"], 0.35))
        bloom = g["bloom_pink"] if i % 2 else g["bloom_cyan"]
        by = bottom + 2 + length
        for dx, dy in ((0, 0), (-1, 0), (1, 0), (0, -1), (0, 1)):
            put(vp, vines, x + dx, by + dy, g["bloom_light"] if (dx, dy) == (0, 0) else bloom)
    # Moos, das über die Unterkante hängt
    for x0, width in ((63, 4), (89, 3), (118, 5), (166, 4), (196, 3), (212, 4)):
        for dx in range(width):
            depth = 1 + (1 if 0 < dx < width - 1 else 0) + (1 if hash01(x0 + dx, 619) > 0.7 else 0)
            for k in range(depth):
                put(vp, vines, x0 + dx, bottom + 1 + k, g["moss1"] if k < depth - 1 else g["moss0"])
    _outline(vines, OUTLINE)
    vl = vines.load()
    for y in range(FRAME_H):
        for x in range(FRAME_W):
            if vl[x, y][3] and y > bottom:
                px[x, y] = vl[x, y]


def post_keeper(img: Image.Image, pal: dict) -> None:
    """Nach dem Rahmen: Polster aus Lavendelmoos auf der Oberkante (mit Sporen), aus drei davon sprießen kleine
    Mondsteinkristalle; Moos auch auf dem Ring des Medaillons, oben rechts am Fuß des Kristallkamms und unten links."""
    g = {k: c(pal, k) for k in pal}
    px = img.load()
    top = BAR_Y - 3

    def gem_free(x: int) -> bool:
        return all(abs(x - (BAR_X + round(BAR_W * f))) > 8 for f in GEMS)

    cushions = [(60, 14, 3.0), (88, 18, 3.6), (116, 12, 2.6), (163, 16, 3.4), (190, 12, 2.4), (214, 14, 3.2)]
    sprouts = {88: ((-3, -100.0, 7.0, 1.5), (1, -78.0, 9.0, 1.7), (4, -58.0, 5.5, 1.3)),
               163: ((-2, -84.0, 7.5, 1.6), (2, -62.0, 5.0, 1.2)),
               214: ((-3, -112.0, 5.0, 1.2), (0, -88.0, 8.0, 1.6), (3, -70.0, 5.5, 1.3))}
    crystals = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    for cx, spec in sprouts.items():
        for j, (dx, ang, length, width) in enumerate(spec):
            _keeper_crystal(crystals, g, cx + dx + 0.5, top - 0.5, math.radians(ang), length, width, cx + j)
    _outline(crystals, OUTLINE)
    cp = crystals.load()
    for y in range(0, top):
        for x in range(FRAME_W):
            if cp[x, y][3]:
                px[x, y] = cp[x, y]

    moss = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    mp = moss.load()
    for cx, width, height in cushions:
        for x in range(cx - width // 2, cx + width // 2 + 1):
            if not gem_free(x):
                continue
            t = (x - cx) / (width / 2 + 0.5)
            if abs(t) >= 1.0:
                continue
            h = height * (1.0 - t * t) ** 0.6 + (hash01(x, 631) - 0.5) * 1.1
            y_top = top + 1 - max(1, round(h))
            y_bot = top + 1 + (1 if hash01(x, 633) > 0.55 else 0)
            for y in range(y_top, y_bot + 1):
                k = (y - y_top) / max(1, y_bot - y_top)
                col = _ramp((g["moss3"], g["moss2"], g["moss1"], g["moss0"]), k * 0.9 + 0.1 * hash01(x, y, 635))
                if y == y_top and hash01(x, 637) > 0.82:
                    col = g["spore"] if hash01(x, 639) > 0.5 else g["bloom_cyan"]
                mp[x, y] = col
    # Moos auf dem Ring des Medaillons: oben rechts (am Fuß des Kristallkamms) und unten links
    for a0, a1, seed in ((-72.0, -34.0, 641), (124.0, 146.0, 643)):
        for k in range(int((a1 - a0) * 1.4)):
            ang = math.radians(a0 + k / 1.4)
            hump = 0.4 + 1.1 * math.sin(math.pi * k / ((a1 - a0) * 1.4)) + (hash01(k // 3, seed) - 0.5) * 0.9
            inner = EMB_R - 1.5 - 1.5 * math.sin(math.pi * k / ((a1 - a0) * 1.4))
            for r10 in range(int(inner * 10), int((EMB_R + hump) * 10), 4):
                r = r10 / 10.0
                x = round(EMB_X - 0.5 + math.cos(ang) * r)
                y = round(EMB_Y - 0.5 + math.sin(ang) * r)
                if not (0 <= x < FRAME_W and 0 <= y < FRAME_H):
                    continue
                t = (r - inner) / (EMB_R + hump - inner)
                col = _ramp((g["moss0"], g["moss1"], g["moss2"], g["moss3"]), t)
                if t > 0.8 and hash01(x, y, seed + 1) > 0.86:
                    col = g["spore"] if hash01(x, y, seed + 2) > 0.5 else g["bloom_cyan"]
                mp[x, y] = col
    # Kontur nur nach außen, nie in das Fenster der Leiste
    _outline(moss, OUTLINE)
    ml = moss.load()
    for y in range(FRAME_H):
        for x in range(FRAME_W):
            if not ml[x, y][3] or (BAR_X <= x < BAR_X + BAR_W and BAR_Y <= y < BAR_Y + BAR_H):
                continue
            if ml[x, y] == OUTLINE and px[x, y][3] and px[x, y] != OUTLINE:
                continue                                       # auf dem Rahmen selbst keine zusätzliche Kontur
            px[x, y] = ml[x, y]


def fill_strip_keeper() -> Image.Image:
    """Füllung des Steinhüters: milchiger Mondstein – weiche Wolken im Blau, über die schräg ein heller Schimmer zieht
    (mit bläulichem Saum, wie das Licht in einem echten Mondstein), dazwischen einzelne Glitzerpunkte."""
    pal = ELEMENTS["keeper"]
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    acc = c(pal, "acc")
    tau = math.tau
    img = Image.new("RGBA", (256, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        top = 1.0 - y / (BAR_H - 1)
        for x in range(256):
            cloud = fill_value("keeper", x, y)
            col = mix(mix(f0, f1, 0.25 + 0.55 * top), f2, max(0.0, cloud - 0.3) * 0.9)
            s = ((x + 2.2 * y + 7.0 * math.sin(tau * x / 128)) % 128) / 128.0
            band = math.exp(-((s - 0.5) / 0.085) ** 2)
            rim = math.exp(-((s - 0.62) / 0.045) ** 2)
            col = mix(col, f3, 0.7 * band)
            col = mix(col, acc, 0.45 * rim)
            if hash01(x, y, 651) > 0.984 and 1 <= y <= BAR_H - 2:
                col = f3
            if y == 1:
                col = mix(col, f3, 0.3)
            if y == BAR_H - 1:
                col = mix(col, f0, 0.55)
            px[x, y] = col
    return img


# =============================================================================================
# Namensband (seit 0.94)
# =============================================================================================

BAN_H, BAN_TAIL_H, BAN_CAP, BAN_TILE = 15, 18, 16, 16
# Abweichungen vom Standard (dunkler Stoff im Ton des Bosses, Borte aus Gold oder hellem Metall)
BANNER_COLORS = {
    "anni": dict(trim="#f800f8", trim_dark="#22d8e8", fabric="#14101c"),
    "anni2": dict(trim="#ff2040", trim_dark="#ff8a30", fabric="#1a0a10"),
    "world_eater": dict(trim="#e8541a", trim_dark="#54444e", fabric="#1c0a0a"),
    "keeper": dict(trim="#a8d2ff", trim_dark="#4e5a8a", fabric="#141428"),
    "sea_witch": dict(trim="#5ae8b8", trim_dark="#2e4a44", fabric="#06120f"),
    "soul_shepherd": dict(trim="#4fe0d0", trim_dark="#3a3644", fabric="#0a1012"),
    "star_weaver": dict(trim="#9ad8ff", trim_dark="#2c3052", fabric="#070a1c"),
    "hour_warden": dict(trim="#e8bc62", trim_dark="#6c4016", fabric="#140a04"),
    "phasmor": dict(trim="#c48ccc", trim_dark="#6a3a74", fabric="#08070f"),
    "sordin": dict(trim="#5aa58a", trim_dark="#25594b", fabric="#0a0d14"),
    "robber_knight": dict(trim="#e4c254", trim_dark="#8a6414", fabric="#121014"),
}


def banner_colors(element: str) -> dict:
    pal = ELEMENTS[element]
    deep, acc, glow = c(pal, "deep"), c(pal, "acc"), c(pal, "glow")
    out = dict(fabric=mix(mix(deep, acc, 0.16), OUTLINE, 0.12), gem=acc, gem_light=glow,
               trim=c(pal, "gold") if "gold" in pal else c(pal, "m2"),
               trim_dark=c(pal, "gold_dark") if "gold_dark" in pal else c(pal, "m1"))
    for key, value in BANNER_COLORS.get(element, {}).items():
        out[key] = hexc(value)
    out["fabric_light"] = mix(out["fabric"], acc, 0.22)
    out["fabric_dark"] = mix(out["fabric"], OUTLINE, 0.55)
    return out


def _band_pixel(bc: dict, x: int, y: int):
    """Farbe des Bandes in Zeile y: Kontur, Borte (hell, dunkel), Stoff mit sanftem Verlauf, Borte, Kontur."""
    if y in (0, BAN_H - 1):
        return OUTLINE
    if y == 1:
        return mix(bc["trim"], (255, 255, 255, 255), 0.45) if x % 8 == 3 else bc["trim"]
    if y == BAN_H - 2:
        return bc["trim"]
    if y in (2, BAN_H - 3):
        return bc["trim_dark"]
    t = (y - 3) / (BAN_H - 7)
    col = mix(bc["fabric_light"], bc["fabric_dark"], 0.1 + 0.75 * t ** 1.2)
    if (x + y) % 8 == 0:
        col = mix(col, bc["fabric_light"], 0.12)                # feines Webmuster
    return col


def banner_tile(element: str) -> Image.Image:
    bc = banner_colors(element)
    img = Image.new("RGBA", (BAN_TILE, BAN_H), CLEAR)
    px = img.load()
    for y in range(BAN_H):
        for x in range(BAN_TILE):
            px[x, y] = _band_pixel(bc, x, y)
    return img


def banner_cap(element: str) -> Image.Image:
    """Linkes Ende des Bandes: dahinter ein Schwalbenschwanz, der drei Zeilen tiefer hängt, vorn die Kante des Bandes mit
    Borte und einem kleinen Zierstein. Das rechte Ende ist das Spiegelbild."""
    bc = banner_colors(element)
    img = Image.new("RGBA", (BAN_CAP, BAN_TAIL_H), CLEAR)
    px = img.load()
    t0, t1 = 3, BAN_TAIL_H - 1
    mid = (t0 + t1) / 2
    for y in range(t0, t1 + 1):
        notch = round(4.0 * (1.0 - abs(y - mid) / ((t1 - t0) / 2)))
        for x in range(notch, 11):
            if y in (t0, t1) or x == notch:
                col = OUTLINE
            elif y in (t0 + 1, t1 - 1):
                col = bc["trim_dark"]
            else:
                col = mix(bc["fabric"], bc["fabric_dark"], 0.3 + 0.5 * (y - t0) / (t1 - t0))
            if y >= BAN_H and x >= 6:
                col = mix(col, OUTLINE, 0.4)                     # Schatten unter dem Band
            px[x, y] = col
    for y in range(BAN_H):
        for x in range(8, BAN_CAP):
            if x == 8:
                col = OUTLINE
            elif x == 9 and 0 < y < BAN_H - 1:
                col = bc["trim"]
            elif x == 10 and 1 < y < BAN_H - 2:
                col = bc["trim_dark"]
            else:
                col = _band_pixel(bc, x, y)
            px[x, y] = col
    gx, gy = 13, BAN_H // 2
    for y in range(gy - 2, gy + 3):
        for x in range(gx - 2, gx + 3):
            d = abs(x - gx) + abs(y - gy)
            if d == 2:
                px[x, y] = OUTLINE
            elif d < 2:
                px[x, y] = bc["gem_light"] if (x - gx, y - gy) in ((-1, 0), (0, -1)) else bc["gem"]
    return img


POST = {"schrolli": post_schrolli, "schrolli_true": post_schrolli, "schrolli_almighty": post_almighty,
        "world_eater": post_world_eater, "kraken": post_kraken, "keeper": post_keeper, "sea_witch": post_sea_witch,
        "soul_shepherd": post_soul_shepherd, "star_weaver": post_star_weaver, "ignara": post_ignara, "phasmor": post_phasmor,
        "sordin": post_sordin, "robber_knight": post_robber_knight}
BORDERS = {"world_eater": frame_border_stone, "keeper": frame_border_stone}


DECOS = {"schrolli": deco_schrolli, "schrolli_true": deco_schrolli, "schrolli_almighty": deco_almighty,
         "world_eater": deco_world_eater, "anni": deco_anni, "anni2": deco_anni, "mystic": deco_mystic, "star": deco_star, "forge": deco_forge, "moth": deco_moth, "void": deco_void, "reef": deco_reef, "shadow": deco_shadow, "fire": deco_fire, "ice": deco_ice, "lightning": deco_lightning, "nature": deco_nature, "earth": deco_earth,
         "water": deco_water, "kraken": deco_kraken, "keeper": deco_keeper, "sea_witch": deco_sea_witch,
         "soul_shepherd": deco_soul_shepherd, "star_weaver": deco_star_weaver, "hour_warden": deco_hour_warden,
         "ignara": deco_ignara, "phasmor": deco_phasmor, "sordin": deco_sordin, "robber_knight": deco_robber_knight}


def frame(element: str, phases: tuple | None = None) -> Image.Image:
    global GEMS
    GEMS = phases or PHASES[element]
    pal = ELEMENTS[element]
    img = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    DECOS[element](img, pal)
    BORDERS.get(element, frame_border)(img, pal)
    emblem(img, pal)
    if element in POST:
        POST[element](img, pal)
    return img


# =============================================================================================
# Füllung, leere Leiste, Spur, Glanz, Aura
# =============================================================================================

def fill_value(element: str, x: int, y: int) -> float:
    tau = math.tau
    if element == "fire":
        return 0.5 + 0.5 * math.sin(tau * x / 32 + 1.6 * math.sin(tau * y / 9 + tau * x / 64))
    if element == "ice":
        band = ((x + 2 * y) // 6) % 4
        return (0.75 if band == 0 else 0.3) + 0.15 * math.sin(tau * x / 64)
    if element == "lightning":
        u = (x / 8.0) % 2.0
        tri = u if u < 1.0 else 2.0 - u                     # Zickzack-Bogen quer durch die Leiste
        arc = 1.5 + 5.0 * tri + (1.0 if (x // 16) % 2 else 0.0)
        d = abs(y - arc)
        return 1.0 if d < 0.6 else (0.72 if d < 1.6 else 0.32 + 0.08 * math.sin(tau * x / 32))
    if element == "nature":
        vein = ((x - 2 * y) % 16) < 2
        return 0.75 if vein else 0.4 + 0.15 * math.sin(tau * x / 32) * math.cos(tau * y / 9)
    if element == "earth":
        return 0.35 + 0.4 * hash01(x // 2, y // 2, 7) * (0.5 + 0.5 * math.sin(tau * x / 64))
    if element == "water":
        return 0.5 + 0.5 * math.sin(tau * (x / 32 - y / 9))
    if element == "void":
        # ein Sternennebel: weiche Schleier, darin einzelne helle Sterne
        neb = 0.4 + 0.2 * math.sin(tau * x / 64 + 1.4 * math.sin(tau * y / 9)) + 0.12 * math.sin(tau * x / 32 - tau * y / 18)
        return 1.0 if hash01(x, y, 83) > 0.965 else neb
    if element == "reef":
        # Licht einer Lagune: ein schwankendes Netz heller Linien, dazwischen einzelne Blasen
        line1 = abs(math.sin(math.pi * (x / 16 + 0.35 * math.sin(tau * (y / 18 + x / 64)))))
        line2 = abs(math.sin(math.pi * (-x / 16 + y / 4.5 + 0.3 * math.sin(tau * x / 32))))
        near = min(line1, line2)
        if hash01(x, y, 91) > 0.975:
            return 1.0
        return 0.92 if near < 0.14 else (0.66 if near < 0.3 else 0.36 + 0.1 * math.sin(tau * x / 64))
    if element == "star":
        # Nachthimmel: Nebelschleier, die langsam ziehen, darin funkelnde Sterne und ab und zu ein heller Stern
        neb = 0.32 + 0.18 * math.sin(tau * x / 64 + 1.2 * math.sin(tau * y / 9)) + 0.1 * math.sin(tau * x / 32 + tau * y / 18)
        h = hash01(x, y, 59)
        if h > 0.97:
            return 1.0
        if h > 0.94:
            return 0.8
        return neb
    if element == "forge":
        # fließendes, glühendes Metall: langsame Wellen, darin helle Risse und einzelne Funken
        flow = 0.46 + 0.2 * math.sin(tau * x / 64 + 1.3 * math.sin(tau * y / 9 + tau * x / 128))
        crack = abs(math.sin(math.pi * (x / 13 + 0.45 * math.sin(tau * (y / 9 + x / 48)))))
        if hash01(x, y, 47) > 0.975:
            return 1.0
        return 0.9 if crack < 0.1 else (0.7 if crack < 0.22 else flow)
    if element == "moth":
        # Flügelstaub: versetzte Reihen kleiner Schuppen (Bögen), dazwischen Glitzer
        row = y // 3
        sx = (x + (row % 2) * 3) % 6
        arc = abs(sx - 2.5) / 2.5
        scale = 0.42 + 0.3 * (1.0 - arc) * (1.0 if y % 3 else 0.6)
        if hash01(x, y, 61) > 0.97:
            return 1.0
        return scale + 0.08 * math.sin(tau * x / 64)
    if element == "mystic":
        # Harlekin-Rauten aus Samt im Wechsel hell und dunkel, darin funkelnde Pailletten
        a = (x / 8.0 + y / 4.5) % 2.0
        b = (x / 8.0 - y / 4.5) % 2.0
        cell = (int(a) + int(b)) % 2
        edge = min(a % 1.0, 1.0 - a % 1.0, b % 1.0, 1.0 - b % 1.0)
        if hash01(x, y, 67) > 0.972:
            return 1.0
        if edge < 0.07:
            return 0.82
        return 0.62 if cell else 0.36
    if element in ("anni", "anni2"):
        # ein kaputtes Bild: Zeilen, verrutschte Blöcke (4 x 1), dazwischen helle Fehlerpixel
        block = hash01(x // 4 + (y % 3) * 7, y, 71)
        if hash01(x, y, 73) > 0.975:
            return 1.0
        if y % 3 == 0:
            return 0.3
        return 0.4 + block * 0.45
    if element == "schrolli":
        # strömendes goldenes Licht: weiche Wellen, schräge Strahlen und Funkeln
        flow = 0.48 + 0.16 * math.sin(tau * x / 64 + 1.2 * math.sin(tau * y / 9)) + 0.08 * math.sin(tau * x / 32 - tau * y / 18)
        beam = ((x + 2 * y) % 32) < 2
        if hash01(x, y, 227) > 0.972:
            return 1.0
        return 0.88 if beam else flow
    if element == "schrolli_true":
        # das Weltall: violette Nebel, die ziehen, Sterne und ab und zu ein heller Stern
        neb = 0.36 + 0.2 * math.sin(tau * x / 64 + 1.5 * math.sin(tau * y / 9)) + 0.12 * math.sin(tau * x / 32 + tau * y / 9)
        h = hash01(x, y, 229)
        if h > 0.968:
            return 1.0
        if h > 0.94:
            return 0.78
        return neb
    if element == "schrolli_almighty":
        # das Plasma einer Sonne: wogende Glut mit feiner Körnung, darin Bögen aus Licht und einzelne Funken
        flow = 0.5 + 0.5 * math.sin(tau * x / 64 + 1.5 * math.sin(tau * y / 9 + tau * x / 128))
        d1, d2 = _worley(x, y * 1.2, 4.0, 64, 431)
        grain = min(1.0, (d2 - d1) / 2.0)
        arc = abs(y - (4.0 + 3.0 * math.sin(tau * x / 64 + 0.7)))
        if arc < 0.6 and math.sin(tau * x / 128) > 0.2:
            return 1.0
        if hash01(x, y, 433) > 0.978:
            return 1.0
        return 0.36 + 0.28 * flow + 0.14 * grain + (0.12 if arc < 1.5 and math.sin(tau * x / 128) > 0.2 else 0.0)
    if element == "world_eater":
        # Krusten auf Glut: zwischen den Schollen fließt es hell
        d1, d2 = _worley(x, y * 1.35, 256 / 24, 24, 451)
        crack = d2 - d1
        return 0.95 if crack < 0.75 else (0.7 if crack < 1.9 else 0.3 + 0.1 * math.sin(tau * x / 64) + 0.06 * hash01(x, y, 453))
    if element == "kraken":
        # Schlieren der Tinte
        return 0.5 + 0.5 * math.sin(tau * x / 64 + 2.3 * math.sin(tau * y / 18 + tau * x / 32))
    if element == "keeper":
        # milchiger Mondstein: weiche Wolken (der Schimmer kommt in fill_strip_keeper dazu)
        return (0.5 + 0.22 * math.sin(tau * x / 64 + 1.3 * math.sin(tau * y / 9 + tau * x / 128))
                + 0.14 * math.sin(tau * x / 32 - tau * y / 18))
    if element == "sea_witch":
        # ihr Trank: wirbelnde Schlieren, dazwischen helle Blasen
        if hash01(x // 2, y // 2, 731) > 0.95:
            return 1.0
        return 0.5 + 0.32 * math.sin(tau * x / 48 + 2.0 * math.sin(tau * y / 12 + tau * x / 96)) + 0.12 * math.sin(tau * x / 24 - tau * y / 9)
    if element == "star_weaver":
        # ihr Netz: Nachthimmel mit Nebelschleiern, durchzogen von schrägen, leuchtenden Fäden, dazwischen Sterne
        if hash01(x, y, 961) > 0.972:
            return 1.0
        if (x + 3 * y) % 23 == 0 or (x - 3 * y) % 29 == 0:
            return 0.86
        return 0.38 + 0.16 * math.sin(tau * x / 64 + 1.4 * math.sin(tau * y / 9 + tau * x / 128)) + 0.06 * math.sin(tau * x / 16)
    if element == "hour_warden":
        # sein Uhrwerk: glühendes Bernstein in sanften Wellen, darin eine Minutenbahn (alle sechs Pixel ein heller Strich, jeder
        # fünfte länger) und ab und zu ein Funke
        if x % 6 == 0 and (y <= 2 or (x % 30 == 0 and y <= 4)):
            return 0.95
        if hash01(x, y, 1207) > 0.975:
            return 1.0
        return 0.48 + 0.2 * math.sin(tau * x / 64 + 1.2 * math.sin(tau * y / 9 + tau * x / 128)) + 0.08 * math.sin(tau * x / 16)
    if element == "ignara":
        # Flammen, die nach oben dünner werden; selten blitzt ein Opal (1.0)
        tongue = 0.5 + 0.5 * math.sin(tau * x / 16 + 1.6 * math.sin(tau * x / 64) + 0.6 * math.sin(tau * y / 9 + tau * x / 32))
        rel = 1.0 - y / (BAR_H - 1)
        h = 0.35 + 0.65 * tongue
        if hash01(x, y, 1309) > 0.982 and 0 < y < BAR_H - 1:
            return 1.0
        if rel < h:
            return 0.62 + 0.33 * (1.0 - rel / h)
        return 0.32 - 0.14 * (rel - h)
    if element == "robber_knight":
        # Gold mit Sparren, die nach vorn weisen (wie sein Ansturm); selten blitzt es
        s = (x + abs(y - (BAR_H - 1) / 2) * 2.0) % 16.0
        if hash01(x, y, 1811) > 0.986:
            return 1.0
        if s < 1.6:
            return 0.92
        if s < 3.2:
            return 0.7
        return 0.46 + 0.1 * math.sin(math.tau * x / 64.0) + 0.04 * (1.0 if y < BAR_H / 2 else -1.0)
    if element == "sordin":
        # Schallwellen: Kämme, die schräg über die Leiste laufen (wie Ringe auf dem Wasser); selten glimmt eine Note
        wave = math.sin(math.tau * x / 22.0 + 1.3 * math.sin(math.tau * y / 9.0) + 0.6 * math.sin(math.tau * x / 70.0))
        if hash01(x, y, 1611) > 0.985:
            return 1.0
        if wave > 0.9:
            return 0.92
        if wave > 0.6:
            return 0.66
        return 0.3 + 0.16 * (wave + 1.0) / 2.0 + 0.05 * math.sin(math.tau * y / 9.0)
    if element == "phasmor":
        # geschliffener Phasit: Facetten (Zellen) in verschiedener Helligkeit mit hellen Kanten, darüber zieht schräg ein
        # Schimmer; selten funkelt es
        d1, d2 = _worley(x, y * 1.3, 8.0, 32, 1531)
        if hash01(x, y, 1533) > 0.982:
            return 1.0
        if (x + 2 * y) % 64 < 5:
            return 0.93
        if d2 - d1 < 0.7:
            return 0.86
        cell = hash01(int(d1 * 7.0), int(d2 * 3.0), 1535)
        return 0.3 + 0.24 * cell + 0.1 * math.sin(math.tau * x / 64 + 0.8 * math.sin(math.tau * y / 9))
    if element == "soul_shepherd":
        # Seelenfeuer: züngelnde Flammen, die nach oben dünner werden, dazwischen helle Funken
        tongue = 0.5 + 0.5 * math.sin(tau * x / 16 + 1.8 * math.sin(tau * x / 64) + 0.5 * math.sin(tau * y / 9 + tau * x / 32))
        rel = 1.0 - y / (BAR_H - 1)                          # 0 unten … 1 oben
        h = 0.3 + 0.7 * tongue
        if hash01(x, y // 2, 927) > 0.975 and rel > 0.3:
            return 1.0
        if rel < h:
            return 0.6 + 0.32 * (1.0 - rel / h)               # in der Flamme: unten am hellsten
        return 0.3 - 0.12 * (rel - h)
    if element == "shadow":
        # wabernder Rauch, durch den einzelne Seelen ziehen
        smoke = 0.42 + 0.22 * math.sin(tau * x / 64 + 2.0 * math.sin(tau * y / 9 + tau * x / 32))
        return 1.0 if hash01(x // 2, y // 3, 29) > 0.93 else smoke
    return 0.5


def fill_strip_world_eater() -> Image.Image:
    """Füllung des Weltenfressers: die Glut seines Schlunds – dunkle Krusten, zwischen denen es hell fließt, und ein violetter
    Riss, der sich hindurchzieht und funkelt (wie der Riss in seinem Rachen)."""
    pal = ELEMENTS["world_eater"]
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    cr1, cr2, cr3 = c(pal, "crystal1"), c(pal, "crystal2"), c(pal, "crystal3")
    tau = math.tau
    img = Image.new("RGBA", (256, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        top = 1.0 - y / (BAR_H - 1)
        for x in range(256):
            v = fill_value("world_eater", x, y)
            flow = 0.5 + 0.5 * math.sin(tau * x / 64 + 1.3 * math.sin(tau * y / 9 + tau * x / 128))
            if v > 0.9:
                col = mix(f2, f3, 0.45 + 0.3 * flow)
            elif v > 0.6:
                col = mix(f1, f2, 0.5 + 0.3 * flow)
            else:
                col = mix(f0, f1, 0.2 + 0.4 * top + 0.25 * flow)
            gate = math.sin(tau * x / 128 + 0.3)
            if gate > 0.2:
                # der Riss: ein gezackter Spalt, aus dem violettes Licht dringt
                ry = 4.0 + 2.2 * math.sin(tau * x / 64 + 0.8) + (hash01(x // 3, 471) - 0.5) * 1.8
                d = abs(y - ry)
                if d < 0.6:
                    col = cr3 if hash01(x, 461) > 0.55 else cr2
                elif d < 1.6:
                    col = mix(col, cr1, 0.6 * min(1.0, (gate - 0.2) * 4))
                elif d < 2.6 and hash01(x, y, 463) > 0.9:
                    col = cr2
            if y == 1:
                col = mix(col, f3, 0.25)
            if y == BAR_H - 1:
                col = mix(col, f0, 0.5)
            px[x, y] = col
    return img


def fill_strip(element: str) -> Image.Image:
    if element == "world_eater":
        return fill_strip_world_eater()
    if element == "kraken":
        return fill_strip_kraken()
    if element == "keeper":
        return fill_strip_keeper()
    if element == "ignara":
        return fill_strip_ignara()
    pal = ELEMENTS[element]
    f0, f1, f2, f3 = (c(pal, k) for k in ("f0", "f1", "f2", "f3"))
    img = Image.new("RGBA", (256, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        top = 1.0 - y / (BAR_H - 1)
        for x in range(256):
            v = fill_value(element, x, y)
            base = mix(f0, f1, 0.35 + 0.65 * top)
            col = mix(base, f2, max(0.0, v - 0.35) * 1.2)
            if v > 0.85:
                col = mix(col, f3, (v - 0.85) / 0.15 * 0.8)
            if y == 1:
                col = mix(col, f3, 0.35)
            if y == BAR_H - 1:
                col = mix(col, f0, 0.5)
            px[x, y] = col
    return img


def empty_strip(element: str) -> Image.Image:
    pal = ELEMENTS[element]
    deep = c(pal, "deep")
    img = Image.new("RGBA", (BAR_W, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        for x in range(BAR_W):
            v = 0.25 + 0.1 * fill_value(element, x, y)
            col = mix(OUTLINE, deep, v * 1.6)
            if y == 0:
                col = mix(col, OUTLINE, 0.6)
            elif y == BAR_H - 1:
                col = mix(col, deep, 0.5)
            px[x, y] = col
    return img


def trail_strip(element: str) -> Image.Image:
    pal = ELEMENTS[element]
    glow = c(pal, "glow")
    img = Image.new("RGBA", (BAR_W, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        for x in range(BAR_W):
            a = 210 if 0 < y < BAR_H - 1 else 150
            px[x, y] = glow[:3] + (a,)
    return img


def shine_strip() -> Image.Image:
    img = Image.new("RGBA", (16, BAR_H), CLEAR)
    px = img.load()
    for y in range(BAR_H):
        for x in range(16):
            d = abs((x - y * 0.7) - 6.0)
            if d < 3.5:
                px[x, y] = (255, 255, 255, int(255 * (1 - d / 3.5) * 0.8))
    return img


def aura(element: str, frame_img: Image.Image) -> Image.Image:
    """Weicher Schein rund um Rahmen und Medaillon (für die letzte Phase)."""
    pal = ELEMENTS[element]
    glow, acc = c(pal, "glow"), c(pal, "acc")
    src = frame_img.load()
    img = Image.new("RGBA", (FRAME_W, FRAME_H), CLEAR)
    px = img.load()
    solid = [[src[x, y][3] > 0 for y in range(FRAME_H)] for x in range(FRAME_W)]
    for y in range(FRAME_H):
        for x in range(FRAME_W):
            if solid[x][y] or (BAR_X <= x < BAR_X + BAR_W and BAR_Y <= y < BAR_Y + BAR_H):
                continue                                # nicht über der Füllung (die leuchtet selbst)
            best = 99.0
            for dy in range(-4, 5):
                for dx in range(-4, 5):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < FRAME_W and 0 <= ny < FRAME_H and solid[nx][ny]:
                        best = min(best, math.hypot(dx, dy))
            if best < 4.5:
                t = 1.0 - best / 4.5
                px[x, y] = mix(acc, glow, t)[:3] + (int(230 * t ** 1.5),)
    return img


# =============================================================================================
# Bildnis aus dem 3D-Modell
# =============================================================================================

def _sun_behind(head: Image.Image) -> Image.Image:
    """Bildnis des Allmächtigen: hinter seinem Kopf lodert eine Sonne aus Flammenzungen; der Kopf bekommt eine dunkle Kontur,
    damit sich Krone und Haar von den Flammen abheben."""
    pal = ELEMENTS["schrolli_almighty"]
    fire = [c(pal, f"flame{k}") for k in range(6)]
    out = Image.new("RGBA", (64, 64), CLEAR)
    op = out.load()
    hp = head.load()
    cx, cy = 32.0, 27.0
    for y in range(64):
        for x in range(64):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            r = math.hypot(dx, dy)
            a = math.atan2(dy, dx)
            tongue = 0.5 + 0.5 * math.cos(14 * a + 1.4 * math.sin(3 * a + 0.5))
            reach = 19.0 + 12.0 * tongue ** 1.5 + 2.0 * math.sin(5 * a + 0.4)
            if r > reach:
                continue
            t = r / reach
            col = _ramp((fire[1], fire[2], fire[3], fire[4], fire[5]), 1.05 - 0.95 * t)
            if t > 0.9:
                col = mix(col, fire[0], 0.35)
            op[x, y] = col
    solid = [[hp[x, y][3] > 140 for y in range(64)] for x in range(64)]
    for y in range(64):
        for x in range(64):
            if solid[x][y]:
                op[x, y] = hp[x, y][:3] + (255,)
            elif any(0 <= x + dx < 64 and 0 <= y + dy < 64 and solid[x + dx][y + dy]
                     for dx in (-2, -1, 0, 1, 2) for dy in (-2, -1, 0, 1, 2) if abs(dx) + abs(dy) <= 2):
                op[x, y] = (40, 8, 4, 255)
    return out


def portrait(element: str) -> Image.Image:
    """Kopf des Bosses schräg von vorn, aus dem Modell gerendert und auf 64 x 64 verkleinert, kreisrund beschnitten,
    mit dunkler Kontur (2 Pixel = 1 Pixel im Spiel), damit er sich vom Grund des Medaillons abhebt."""
    from entitygen.models import (asteria, boss_earth, boss_fire, boss_ice, boss_lightning, boss_nature, boss_shadow, boss_water,
                                  glutschmied, kraken, miss_mystik, moth_queen, reef_colossus, shattered_king, anni_troll,
                                  schrolli, sea_witch, soul_shepherd, star_weaver, stone_keeper, world_eater, hour_warden, ignara,
                                  phasmor, sordin, robber_knight)
    from entitygen.paint import paint_model
    from entitygen.preview import render
    mod = {"fire": boss_fire, "ice": boss_ice, "lightning": boss_lightning, "nature": boss_nature, "earth": boss_earth,
           "water": boss_water, "shadow": boss_shadow, "void": shattered_king, "reef": reef_colossus, "moth": moth_queen,
           "forge": glutschmied, "star": asteria, "mystic": miss_mystik, "anni": anni_troll, "anni2": anni_troll,
           "schrolli": schrolli, "schrolli_true": schrolli, "schrolli_almighty": schrolli, "world_eater": world_eater,
           "kraken": kraken, "keeper": stone_keeper, "sea_witch": sea_witch, "soul_shepherd": soul_shepherd,
           "star_weaver": star_weaver, "hour_warden": hour_warden, "ignara": ignara, "phasmor": phasmor, "sordin": sordin,
           "robber_knight": robber_knight}[element]
    m = mod.build()
    m.pack()
    variant = "anni_troll_v2" if element == "anni2" else element if element.startswith("schrolli") else next(iter(mod.PALETTES))
    tex, glow = paint_model(m, mod.materials(variant), mod.PALETTES[variant], seed=100, params={"variant": variant})
    if element.startswith("schrolli"):
        # Kopf mit Haar, Krone und Heiligenschein (in der Wahren Gestalt mit dem Strahlenkranz), die Schultern
        def strip_schrolli(bone):
            if bone.name.startswith(("wing_", "left_", "right_", "scepter", "orb", "robe", "true_rings", "halo", "true_corona",
                                     "hair_tip", "almighty", "blade")):
                bone.cubes.clear()
            for child in bone.children:
                strip_schrolli(child)
        strip_schrolli(m.root)
    if element == "void":
        # nur Kopf, Bart und Krone (Schultern, Hände und Trümmer würden das kleine Bildnis überladen)
        keep = {"body", "head", "beard", "beard_tip", "crown"} | {f"crown_{k}" for k in range(7)}

        def strip(bone):
            if bone.name not in keep:
                bone.cubes.clear()
            for child in bone.children:
                strip(child)
        strip(m.root)
    if element == "reef":
        # Karkinos von vorn: Panzer mit Riff, Stielaugen, Stirn (ohne Beine, Tangsträhnen und Speer)
        def strip_extra(bone):
            if bone.name.startswith(("kelp", "spear")) or "leg" in bone.name:
                bone.cubes.clear()
            for child in bone.children:
                strip_extra(child)
        strip_extra(m.root)
    if element == "moth":
        # nur Kopf mit Krone und Fühlern, Brust und der Ansatz der Flügel
        def strip_moth(bone):
            if bone.name.startswith(("left_leg", "right_leg", "abdomen", "tail", "left_wing", "right_wing", "left_hind", "right_hind")):
                bone.cubes.clear()
            for child in bone.children:
                strip_moth(child)
        strip_moth(m.root)
    if element == "ignara":
        # Kopf mit Maske, Krone und Flammenhaar, Hals und Kragen (ohne Arme, Peitsche, Opalrad, Schwingen und Rock)
        def strip_ignara(bone):
            if bone.name.startswith(("left_", "right_", "whip", "orb", "wheel", "shard", "skirt", "robe", "hem", "cape", "sash")):
                bone.cubes.clear()
            for child in bone.children:
                strip_ignara(child)
        strip_ignara(m.root)
    if element == "phasmor":
        # der Kopf mit Krone und Kiefer vor dem Kragen mit dem Strahlenkranz, darunter Schultergrat und Brustpanzer (ohne Arme,
        # Klinge, Beine, Mantel und Wappenrock)
        def strip_phasmor(bone):
            if bone.name.startswith(("left_", "right_", "blade", "mantle", "tabard")):
                bone.cubes.clear()
            for child in bone.children:
                strip_phasmor(child)
        strip_phasmor(m.root)
    if element == "sordin":
        # die Kapuze mit dem singenden Mund und den Fühlern vor dem Fächer der Pfeifen, darunter der Schulterkragen (ohne Arme,
        # Gabel, Beine und Kutte)
        def strip_sordin(bone):
            if bone.name.startswith(("left_", "right_", "fork", "skirt")):
                bone.cubes.clear()
            for child in bone.children:
                strip_sordin(child)
        strip_sordin(m.root)
    if element == "robber_knight":
        # der Topfhelm mit dem Raben auf der Zimier vor dem Mantel, darunter Kettenkragen und Wappenrock (ohne Ross, Arme,
        # Beine, Lanze, Schild und Morgenstern)
        def strip_knight(bone):
            if bone.name.startswith(("horse", "trapper", "saddle", "neck", "mane", "head", "ear_", "leg_", "shin_", "tail", "tabard",
                                     "horn", "arm_", "forearm_", "hand_", "thigh_", "lance", "pennant", "shield", "mace")):
                bone.cubes.clear()
            for child in bone.children:
                strip_knight(child)
        strip_knight(m.root)
    if element == "forge":
        # der Widderschädel mit Hörnern, Flammen und Kiefer (ohne Arme, Waffen und Beine)
        def strip_forge(bone):
            if bone.name.startswith(("left_arm", "right_arm", "left_forearm", "right_forearm", "hammer", "spear", "left_leg",
                                     "right_leg", "apron")):
                bone.cubes.clear()
            for child in bone.children:
                strip_forge(child)
        strip_forge(m.root)
    if element == "star":
        # Asterias Helm mit Krone und Schweif, die Schultern (ohne Rochen, Lanze, Schild und Umhang)
        def strip_star(bone):
            if bone.name not in ("torso", "helm", "crown", "plume", "plume_mid", "plume_tip", "rider"):
                bone.cubes.clear()
            for child in bone.children:
                strip_star(child)
        strip_star(m.root)
    if element == "mystic":
        # Kopf mit Maske, Zylinder, Feder und Pferdeschwanz (ohne Leib, Arme und Flügel)
        def strip_mystic(bone):
            if bone.name not in ("head", "hat", "feather", "ponytail", "ponytail_tip"):
                bone.cubes.clear()
            for child in bone.children:
                strip_mystic(child)
        strip_mystic(m.root)
    if element == "kraken":
        # von schräg unten: der Kopf mit den goldenen Augen, darüber der Ansatz des Mantels, darunter die Arme, die sich
        # ausbreiten (ohne Fangarme, Flossen und die langen Armglieder)
        def strip_kraken(bone):
            name = bone.name
            if name.startswith(("tentacle", "fin")) or (name.startswith("arm") and "_" in name and int(name.split("_")[1]) >= 3):
                bone.cubes.clear()
            if name == "mantle":
                del bone.cubes[5:]
            for child in bone.children:
                strip_kraken(child)
        strip_kraken(m.root)
    if element == "keeper":
        # der Kopf mit Stirnkristallen und Mondstein, dahinter der Kristallkamm, die Schulterfelsen mit ihren Kristallen (ohne
        # Unterarme, Beine, Ranken, Felsbrocken und Kristallpanzer)
        def strip_keeper(bone):
            name = bone.name
            if name.startswith(("left_leg", "right_leg", "left_shin", "right_shin", "left_forearm", "right_forearm", "vine",
                                "boulder", "armor")):
                bone.cubes.clear()
            if name in ("left_arm", "right_arm"):
                del bone.cubes[1:]                       # nur der Schulterfels
            for child in bone.children:
                strip_keeper(child)
        strip_keeper(m.root)
    if element == "sea_witch":
        # der Kopf mit Krone, treibenden Strähnen und Haar, die Schultern mit dem Tangumhang (ohne Arme, Stab und Schwanz)
        def strip_witch(bone):
            if bone.name.startswith(("left_arm", "right_arm", "staff", "tail", "fan_", "spines", "fluke", "cape")):
                bone.cubes.clear()
            for child in bone.children:
                strip_witch(child)
        strip_witch(m.root)
    if element == "soul_shepherd":
        # Kapuze, Widderschädel und Hörner, darunter das Schaffell (ohne Arme, Stab, Kutte, Umhang und Irrlichter)
        def strip_shepherd(bone):
            if bone.name.startswith(("left_arm", "right_arm", "crook", "hook", "lantern", "hand_bell", "skirt", "hem", "left_leg",
                                     "right_leg", "wisp", "cloak", "charm", "mantle_rag")):
                bone.cubes.clear()
            for child in bone.children:
                strip_shepherd(child)
        strip_shepherd(m.root)
    # die Sternenweberin (seit 0.99) ganz, schräg von oben: die acht Beine ringsum, der Hinterleib voller Sternennebel
    if element == "hour_warden":
        # der Helm mit den glühenden Augen vor dem Zifferblatt (ohne Arme, Klingen, Beine und kreisende Zahnräder)
        def strip_warden(bone):
            if bone.name.startswith(("arm_", "fore_", "minute_blade", "hour_blade", "leg_", "shin_", "knee_gear", "hips", "tasset",
                                     "pendulum", "weight_", "orbit_")):
                bone.cubes.clear()
            for child in bone.children:
                strip_warden(child)
        strip_warden(m.root)
    if element in ("anni", "anni2"):
        # Kopf mit Grinsen, Bildschirmauge, Ohren und Haarschopf (ohne Leib, Arme und Hammer)
        def strip_anni(bone):
            if bone.name not in ("head", "jaw", "left_ear", "right_ear", "tuft"):
                bone.cubes.clear()
            if bone.name == "tuft":
                # nur der Ansatz des Schopfs (sonst wäre das Gesicht im Bildnis zu klein)
                del bone.cubes[2:]
            for child in bone.children:
                strip_anni(child)
        strip_anni(m.root)
    zoom = {"kraken": 1.5, "schrolli": 1.0, "schrolli_true": 1.0, "schrolli_almighty": 1.0, "world_eater": 1.35, "nature": 0.82, "earth": 1.15, "water": 1.1, "void": 1.05, "reef": 1.25, "moth": 0.8, "forge": 0.86, "star": 0.92, "mystic": 0.95,
            "anni": 1.0, "anni2": 1.0, "keeper": 0.8, "sea_witch": 1.5, "soul_shepherd": 1.12, "star_weaver": 0.95, "hour_warden": 0.9, "ignara": 0.85, "phasmor": 0.88, "sordin": 0.72,
            "robber_knight": 1.2}.get(element, 1.2)
    yaw = {"kraken": -20, "schrolli": -22, "schrolli_true": -22, "schrolli_almighty": -22, "world_eater": -6, "water": -50, "earth": -38, "void": -26, "reef": -20, "moth": -8, "forge": -14, "star": -24, "mystic": -22, "anni": -24,
           "anni2": -24, "keeper": -20, "sea_witch": -22, "soul_shepherd": -14, "star_weaver": -20, "hour_warden": -16, "ignara": -18, "phasmor": -10, "sordin": -14,
           "robber_knight": -24}.get(element, -42)
    pitch = {"kraken": -18, "schrolli": 4, "schrolli_true": 4, "schrolli_almighty": 4, "world_eater": 4, "reef": 8, "moth": 4, "forge": 4, "star": 4, "mystic": 2, "anni": 2, "anni2": 2, "keeper": -5, "sea_witch": 2, "soul_shepherd": 4, "star_weaver": 48, "hour_warden": 4, "ignara": 4, "phasmor": 8, "sordin": 6, "robber_knight": 6}.get(element, 6)
    big = render(m, tex, glow, yaw, pitch, 448, pose=getattr(mod, "FOLD_POSE", None), background=(0, 0, 0, 0), zoom=zoom,
                 focus=["body"] if element in ("reef", "kraken", "star_weaver") else ["helm"] if element == "star"
                 else ["lock_l", "lock_r", "crown"] if element == "ignara" else ["helm"] if element == "robber_knight" else ["head"])
    img = big.resize((64, 64), Image.LANCZOS)
    img = ImageEnhance.Brightness(img).enhance({"kraken": 1.85, "fire": 1.9, "earth": 1.5, "lightning": 1.9, "nature": 1.6, "shadow": 2.4,
                                                 "void": 1.0, "reef": 1.35, "moth": 1.25, "forge": 1.65, "star": 1.15, "mystic": 1.2,
                                                 "anni": 1.45, "anni2": 1.45, "schrolli": 1.5, "schrolli_true": 1.5,
                                                 "schrolli_almighty": 1.45, "world_eater": 2.0, "keeper": 1.3, "sea_witch": 1.35,
                                                 "soul_shepherd": 1.7, "star_weaver": 2.3, "hour_warden": 1.6,
                                                 "ignara": 1.3, "phasmor": 1.45, "sordin": 1.9,
                                                 "robber_knight": 1.5}.get(element, 1.4))
    img = ImageEnhance.Color(img).enhance(1.15)
    img = ImageEnhance.Contrast(img).enhance(1.12)
    if element == "schrolli_almighty":
        img = _sun_behind(img)
    px = img.load()
    solid = [[False] * 64 for _ in range(64)]
    for y in range(64):
        for x in range(64):
            r, g, b, a = px[x, y]
            d = math.hypot(x - 31.5, y - 31.5)
            on = a > 140 and d <= 29.5
            solid[x][y] = on
            px[x, y] = (r, g, b, 255) if on else (0, 0, 0, 0)
    out = Image.new("RGBA", (64, 64), CLEAR)
    op = out.load()
    for y in range(64):
        for x in range(64):
            if solid[x][y]:
                op[x, y] = px[x, y]
                continue
            near = any(0 <= x + dx < 64 and 0 <= y + dy < 64 and solid[x + dx][y + dy]
                       for dx in (-2, -1, 0, 1, 2) for dy in (-2, -1, 0, 1, 2) if abs(dx) + abs(dy) <= 3)
            if near and math.hypot(x - 31.5, y - 31.5) <= 31.5:
                op[x, y] = (10, 8, 12, 255)
    return out


def mage_portrait(element: str) -> Image.Image:
    """Bildnis eines Erzmagiers: Kopf mit Kopfschmuck schräg von vorn (Zubehör anderer Elemente entfernt)."""
    from entitygen.models import mage
    from entitygen.paint import paint_model
    from entitygen.preview import render
    m = mage.build()
    m.pack()
    hidden = (mage.ALL_GEAR - mage.GEAR[element]) | {"left_arm", "right_arm"}

    def strip(bone):
        if bone.name in hidden:
            bone.cubes.clear()
            for child in bone.children:
                strip_all(child)
            return
        for child in bone.children:
            strip(child)

    def strip_all(bone):
        bone.cubes.clear()
        for child in bone.children:
            strip_all(child)

    tex, glow = paint_model(m, mage.materials(element), mage.PALETTES[element], seed=100 + mage.ELEMENTS.index(element),
                            params={"variant": element})
    strip(m.root)
    big = render(m, tex, glow, -28, 4, 448, background=(0, 0, 0, 0), zoom=1.08, focus=["head"])
    img = big.resize((64, 64), Image.LANCZOS)
    img = ImageEnhance.Brightness(img).enhance({"fire": 1.2, "earth": 1.25, "lightning": 1.5}.get(element, 1.4))
    img = ImageEnhance.Color(img).enhance(1.15)
    img = ImageEnhance.Contrast(img).enhance(1.12)
    px = img.load()
    solid = [[False] * 64 for _ in range(64)]
    for y in range(64):
        for x in range(64):
            r, g, b, a = px[x, y]
            d = math.hypot(x - 31.5, y - 31.5)
            on = a > 140 and d <= 29.5
            solid[x][y] = on
            px[x, y] = (r, g, b, 255) if on else (0, 0, 0, 0)
    out = Image.new("RGBA", (64, 64), CLEAR)
    op = out.load()
    for y in range(64):
        for x in range(64):
            if solid[x][y]:
                op[x, y] = px[x, y]
                continue
            near = any(0 <= x + dx < 64 and 0 <= y + dy < 64 and solid[x + dx][y + dy]
                       for dx in (-2, -1, 0, 1, 2) for dy in (-2, -1, 0, 1, 2) if abs(dx) + abs(dy) <= 3)
            if near and math.hypot(x - 31.5, y - 31.5) <= 31.5:
                op[x, y] = (10, 8, 12, 255)
    return out


def mage_bossbar(element: str) -> Image.Image:
    """Bossleiste eines Erzmagiers: Rahmen und Füllung seines Elements (Edelsteine bei seinen Phasen), im Medaillon sein
    Bildnis."""
    img = bossbar(element, MAGE_PHASES, portrait_img=mage_portrait(element))
    return img


def bossbar(element: str, phases: tuple | None = None, portrait_img: Image.Image | None = None) -> Image.Image:
    img = Image.new("RGBA", (256, 256), CLEAR)
    fr = frame(element, phases)
    img.alpha_composite(fr, (0, 0))
    img.alpha_composite(fill_strip(element), (0, 48))
    img.alpha_composite(empty_strip(element), (0, 58))
    img.alpha_composite(trail_strip(element), (0, 68))
    img.alpha_composite(shine_strip(), (184, 58))
    img.alpha_composite(gem(ELEMENTS[element], False), (204, 58))
    img.alpha_composite(gem(ELEMENTS[element], True), (214, 58))
    img.alpha_composite(aura(element, fr), (0, 80))
    img.alpha_composite(portrait_img if portrait_img is not None else portrait(element), (0, 128))
    img.alpha_composite(banner_tile(element), (64, 128))
    cap = banner_cap(element)
    img.alpha_composite(cap, (80, 128))
    img.alpha_composite(cap.transpose(Image.FLIP_LEFT_RIGHT), (96, 128))
    return img


def preview(target: str, only: list | None = None) -> None:
    """Vorschau: die Leisten zusammengesetzt (wie im Spiel, bei 70 % und 25 % Leben, mit Namensband), dreifach vergrößert."""
    elements = only or list(ELEMENTS)
    top = 6
    scene_h = top + 13 + FRAME_H + 2
    out = Image.new("RGBA", (FRAME_W * 3 * 2 + 30, len(elements) * 3 * (scene_h + 4) + 20), (46, 52, 60, 255))
    for i, element in enumerate(elements):
        tex = bossbar(element)
        for j, hp in enumerate((0.7, 0.25)):
            scene = Image.new("RGBA", (FRAME_W, scene_h), (0, 0, 0, 0))
            # Namensband (Platz für einen Namen von 96 Pixeln)
            cx = BAR_X + BAR_W // 2
            x0, x1 = cx - 48 - 4, cx + 48 + 4
            scene.alpha_composite(tex.crop((80, 128, 96, 128 + BAN_TAIL_H)), (x0 - BAN_CAP, top - 4))
            for x in range(x0, x1, BAN_TILE):
                w = min(BAN_TILE, x1 - x)
                scene.alpha_composite(tex.crop((64, 128, 64 + w, 128 + BAN_H)), (x, top - 4))
            scene.alpha_composite(tex.crop((96, 128, 112, 128 + BAN_TAIL_H)), (x1, top - 4))
            fy = top + 13
            scene.alpha_composite(tex.crop((0, 58, BAR_W, 58 + BAR_H)), (BAR_X, fy + BAR_Y))
            fill = round(BAR_W * hp)
            trail = min(BAR_W, fill + 18)
            scene.alpha_composite(tex.crop((0, 68, trail, 68 + BAR_H)), (BAR_X, fy + BAR_Y))
            scene.alpha_composite(tex.crop((20, 48, 20 + fill, 48 + BAR_H)), (BAR_X, fy + BAR_Y))
            if hp < 0.3:
                scene.alpha_composite(tex.crop((0, 80, FRAME_W, 80 + FRAME_H)), (0, fy))
            scene.alpha_composite(tex.crop((0, 0, FRAME_W, FRAME_H)), (0, fy))
            p = tex.crop((0, 128, 64, 192)).resize((32, 32), Image.NEAREST)
            scene.alpha_composite(p, (EMB_X - 16, fy + EMB_Y - 16))
            for f in GEMS:
                gx = BAR_X + round(BAR_W * f)
                g = tex.crop((214, 58, 223, 67)) if hp < f else tex.crop((204, 58, 213, 67))
                scene.alpha_composite(g, (gx - 4, fy + BAR_Y - 3 - 7))
            big = scene.resize((scene.width * 3, scene.height * 3), Image.NEAREST)
            out.alpha_composite(big, (10 + j * (FRAME_W * 3 + 10), 10 + i * 3 * (scene_h + 4)))
    out.save(target)


if __name__ == "__main__":
    preview(sys.argv[1] if len(sys.argv) > 1 else "/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad/v2/bars_new.png",
            sys.argv[2].split(",") if len(sys.argv) > 2 else None)
