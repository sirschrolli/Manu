#!/usr/bin/env python3
"""
Erzeugt die Texturen der Dorfbewohner (128x64, eigene Pixelkunst im Vanilla-Stil), je Beruf drei Gesichter; das dritte
ist eine Frau (lange Haare und Kleid). Das Modell ist das der Bürger (client/model/CitizenModel.java), die Zusatzteile
je Beruf stehen in client/renderer/VillagerRenderer.java.

Aufbau einer Textur wie bei tools/generate_citizens.py, dazu:
  * hohe Bäckermütze (64,0) und Gugel (92,0) – beide auf der Fläche der Krempe (niemand trägt beides)
  * Umhängetasche (106,41)

Aufruf (aus dem Projektordner):
    python3 tools/generate_villagers.py            # schreibt alle Texturen
    python3 tools/generate_villagers.py --preview  # zusätzlich eine Vorschau (Vorder- und Rückansicht)
"""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
import generate_citizens as gc  # noqa: E402
import generate_soldiers as gs  # noqa: E402
from generate_citizens import (  # noqa: E402
    BRASS, BROWN, BURGUNDY, CHARCOAL, IRON, MOSS, NAVY, ROSE, SHIRT, SKY, STRAW, TEAL, WHITE, apron_body, apron_flap, arms, belt, cap,
    dress_legs, feather, legs, pouch, skirt_flaps, soot, spectacles, torso,
)


def base(look: tuple, feminine: bool):
    """Kopf, Gesicht und (bei Frauen) lange Haare; die fünfte Angabe eines Gesichts ist eine Farbvariante."""
    return gc.base(tuple(look[:4]), feminine)
from generate_soldiers import CHAIN, FOREST, GOLD, LEATHER, OLIVE, boots, cloth, h01, leather, pal  # noqa: E402
from pixelart import CLEAR, hexc, mix  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
TEX = ROOT / "src" / "main" / "resources" / "assets" / "kingdomsatwar" / "textures" / "entity" / "villager"
SCRATCH = Path("/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad")

gs.PARTS.update({
    "tall_hat": (64, 0, 7, 6, 7),
    "hood": (92, 0, 9, 3, 5),
    "satchel": (106, 41, 4, 4, 2),
})

SIDES = ("right", "front", "left", "back")

# =============================================================================
# Stoffe
# =============================================================================

HOMESPUN = pal("#4a4032", "#62574a", "#7c705e", "#968a74", "#ada28a")
FELT = pal("#1e1610", "#2e2218", "#3f3022", "#52402e", "#66513c")
FLEECE = pal("#8a8070", "#aaa08e", "#c8bfac", "#ddd5c4", "#efe9dc")
CREAM = pal("#8c8270", "#ada290", "#cbc2ac", "#e0d8c4", "#f0eadb")
OILCLOTH = pal("#2a2c1a", "#3a3e24", "#4c512f", "#5f663c", "#757d4c")
STONE = pal("#4a4a4c", "#5f5f62", "#77777a", "#8f8f92", "#a8a8aa")
INK = pal("#0c0e18", "#141826", "#1c2236", "#283048", "#36405c")
HERB = pal("#1f2a16", "#2d3c1e", "#3c5028", "#4e6634", "#627c42")
RUST = pal("#3a1a0c", "#582814", "#76381e", "#944a28", "#b06036")
BLOOD = hexc("#7a1414")
FLOUR = hexc("#f2efe6")


def speckle(paint, color, amount: float, seed: str):
    """Flecken (Mehl, Staub, Blut) über einem Muster."""
    def f(x, y, w, h):
        c = paint(x, y, w, h) if callable(paint) else paint
        return mix(c, color, 0.55) if h01(seed, x, y) < amount else c
    return f


def strap(s, ramp=LEATHER, front_part="jacket", rising=True):
    """Riemen quer über die Brust (vorn) und den Rücken (hinten)."""
    x0, y0, w, h = gs.face_rect(front_part, "front")
    for i in range(8):
        s.set(x0 + (i if rising else 7 - i), y0 + i, ramp[2] if i % 3 else ramp[3])
    x0, y0, w, h = gs.face_rect(front_part, "back")
    for i in range(8):
        s.set(x0 + (7 - i if rising else i), y0 + i, ramp[2] if i % 3 else ramp[3])


def satchel(s, ramp=LEATHER, flap=None, buckle=BRASS, sprigs=None, net=None):
    """Tasche hinten an der Hüfte: Leder mit Klappe und Schnalle; {@code sprigs}: Kräuter oben heraus; {@code net}: Netz."""
    def body(x, y, w, h):
        if net is not None:
            return net[1] if (x + y) % 2 == 0 else net[3]
        i = 3 if h01("satchel", x, y) > 0.15 else 2
        if y == h - 1:
            i = 1
        return ramp[i]
    for f in SIDES:
        s.fill("satchel", f, body)
    s.fill("satchel", "bottom", ramp[1])
    if sprigs is not None:
        s.fill("satchel", "top", lambda x, y, w, h: sprigs[3] if (x + y) % 2 else sprigs[2])
    elif net is not None:
        s.fill("satchel", "top", net[2])
    else:
        s.fill("satchel", "top", ramp[2])
    if net is None:
        # Klappe mit Schnalle auf der Rückseite (die sieht man von hinten)
        x0, y0, w, h = gs.face_rect("satchel", "back")
        for x in range(w):
            s.set(x0 + x, y0, (flap or ramp)[4])
            s.set(x0 + x, y0 + 1, (flap or ramp)[3])
        s.set(x0 + 1, y0 + 2, buckle[3])
        s.set(x0 + 2, y0 + 2, buckle[2])
    if sprigs is not None:
        x0, y0, w, h = gs.face_rect("satchel", "back")
        s.set(x0, y0, sprigs[4])
        s.set(x0 + 3, y0, sprigs[3])


def hood(s, ramp, lining=None, seed="hood"):
    """Gugel: Kragen auf den Schultern, hinten die zurückgeschlagene Kapuze mit Zipfel."""
    def wool(x, y, w, h):
        i = 3 if h01(seed, x, y) > 0.14 else 2
        if y == h - 1:
            i -= 1
        return ramp[i]
    for f in SIDES:
        s.fill("hood", f, wool)
    s.fill("hood", "top", lambda x, y, w, h: ramp[4] if h01(seed + "t", x, y) > 0.8 else ramp[3])
    s.clear("hood", ("bottom",))
    # die Kapuze liegt hinten als Dreieck (Zipfel nach unten), mit dunklem Saum
    x0, y0, w, h = gs.face_rect("hood", "back")
    for y in range(h):
        for x in range(w):
            if abs(x - 4) <= 3 - y:
                s.set(x0 + x, y0 + y, ramp[4] if y == 0 else ramp[3])
    s.set(x0 + 4, y0 + 2, ramp[1])
    if lining is not None:
        x0, y0, w, h = gs.face_rect("hood", "front")
        for x in range(2, 7):
            s.set(x0 + x, y0, lining)


def felt_hat(s, ramp=FELT, band=None):
    """Breitkrempiger Filzhut (Schäfer)."""
    def felt(x, y, w, h):
        i = 3 if h01("felt", x, y) > 0.16 else 2
        return ramp[i]
    def top(x, y, w, h):
        edge = min(x, y, w - 1 - x, h - 1 - y)
        return ramp[2] if edge == 0 else felt(x, y, w, h)
    s.fill("brim", "top", top)
    s.fill("brim", "bottom", ramp[1])
    for f in SIDES:
        s.fill("brim", f, ramp[2])
    s.fill("crown", "top", lambda x, y, w, h: ramp[4] if 2 <= x <= 5 and 3 <= y <= 4 else felt(x, y, w, h))
    for f in SIDES:
        s.fill("crown", f, lambda x, y, w, h: (band or ramp[1]) if y == 3 else felt(x, y, w, h))
    s.clear("crown", ("bottom",))


def toque(s):
    """Hohe weiße Bäckermütze mit Falten, unten ein festes Band."""
    def side(x, y, w, h):
        if y == h - 1:
            return WHITE[3]
        if y == h - 2:
            return WHITE[2]
        return WHITE[4] if x % 2 == 0 else WHITE[3]
    for f in SIDES:
        s.fill("tall_hat", f, side)
    s.fill("tall_hat", "top", lambda x, y, w, h: WHITE[4] if (x * y) % 3 else WHITE[3])
    s.clear("tall_hat", ("bottom",))


def eyepatch(s):
    """Augenklappe über dem rechten Auge, das Band läuft um den Kopf (zweite Schicht)."""
    x0, y0, w, h = gs.face_rect("hat", "front")
    for x in range(8):
        s.set(x0 + x, y0 + 3, hexc("#1a1414"))
    s.set(x0 + 1, y0 + 4, hexc("#141010"))
    s.set(x0 + 2, y0 + 4, hexc("#201818"))
    s.set(x0 + 1, y0 + 5, hexc("#1a1414"))
    s.set(x0 + 2, y0 + 5, hexc("#1a1414"))
    for f in ("right", "left", "back"):
        x0, y0, w, h = gs.face_rect("hat", f)
        for x in range(w):
            s.set(x0 + x, y0 + 3, hexc("#1a1414"))


def coif(s, ramp):
    """Kopftuch der Frauen (zweite Kopfschicht): bedeckt Scheitel und Hinterkopf, vorn ein Saum über der Stirn."""
    def cloth_(x, y, w, h):
        return ramp[3] if h01("coif", x, y) > 0.15 else ramp[2]
    s.fill("hat", "top", cloth_)
    x0, y0, w, h = gs.face_rect("hat", "front")
    for x in range(8):
        s.set(x0 + x, y0, ramp[3])
    s.set(x0, y0 + 1, ramp[2])
    s.set(x0 + 7, y0 + 1, ramp[2])
    for f in ("right", "left"):
        x0, y0, w, h = gs.face_rect("hat", f)
        for y in range(5):
            for x in range(8):
                if (f == "right" and x <= 5 - y) or (f == "left" and x >= 2 + y) or y <= 1:
                    s.set(x0 + x, y0 + y, cloth_(x, y, 8, 8))
    x0, y0, w, h = gs.face_rect("hat", "back")
    for y in range(6):
        for x in range(8):
            s.set(x0 + x, y0 + y, ramp[2] if y == 5 else cloth_(x, y, 8, 8))


def knee_pads(s, ramp=LEATHER):
    for pants in ("rpants", "lpants"):
        s.art(pants, "front", ["....", "....", "....", "....", "bccb", "cddc", "bccb", "....", "....", "....", "....", "...."],
              {"b": ramp[1], "c": ramp[2], "d": ramp[3]})


def wrapped_calves(s, ramp=SHIRT):
    for leg in ("rleg", "lleg"):
        s.rows(leg, 6, 9, lambda x, y, w, h: ramp[2] if (x + y) % 2 else ramp[1])
        s.rows(leg, 10, 11, lambda x, y, w, h: BROWN[2] if y == 10 else BROWN[1])
        s.fill(leg, "bottom", BROWN[0])


DARK_BOOTS = pal("#160f0a", "#24180f", "#342418", "#463222", "#5a4430")
BLACK_BOOTS = pal("#0f0c0b", "#1a1614", "#26201c", "#342c26", "#443a32")


# =============================================================================
# Berufe
# =============================================================================

def peasant(look, feminine: bool) -> gs.Skin:
    """Tagelöhner: Gugel aus grober Wolle, ungefärbter Kittel mit Strick als Gürtel, gewickelte Waden."""
    s, skin, hair = base(look, feminine)
    hood_ramp = {0: pal("#2e2216", "#45331f", "#5c442a", "#735536", "#8a6844"),
                 1: pal("#1e2616", "#2c3820", "#3b4a2a", "#4c5e36", "#5f7244"),
                 2: pal("#3a1c18", "#552a22", "#70382c", "#8a4838", "#a25a46")}[look[4] if len(look) > 4 else 0]
    hood(s, hood_ramp, seed="phood" + look[0])
    smock = cloth(HOMESPUN, 3, "psmock" + look[0], folds=(2, 5))
    def front(x, y, w, h):
        if y == 0 and 2 <= x <= 5:
            return skin[2] if x in (3, 4) else HOMESPUN[2]
        return smock(x, y, w, h)
    torso(s, front, cloth(HOMESPUN, 3, "psmockb", folds=(3,)), cloth(HOMESPUN, 3, "psmocks"), top=HOMESPUN[3])
    s.band("body", 8, lambda x, y, w, h: STRAW[2] if (x + y) % 2 else STRAW[1])
    # Flicken auf dem Kittel
    x0, y0, w, h = gs.face_rect("body", "back")
    for (x, y) in ((5, 3), (6, 3), (5, 4), (6, 4)):
        s.set(x0 + x, y0 + y, HOMESPUN[4] if (x + y) % 2 else HOMESPUN[1])
    arms(s, cloth(HOMESPUN, 3, "psleeve", folds=(1,)), 8, skin, cuff=HOMESPUN[1])
    if feminine:
        coif(s, pal("#8c8270", "#ada290", "#cbc2ac", "#e0d8c4", "#f0eadb"))
        skirt_flaps(s, BROWN, "pskirt", hem=BROWN[1])
        dress_legs(s, BROWN, DARK_BOOTS, "pdress")
    else:
        legs(s, cloth(BROWN, 3, "ptrousers", folds=(1,), shade_bottom=False))
        wrapped_calves(s)
    return s


def fisher(look, feminine: bool) -> gs.Skin:
    """Fischer: Strickmütze, Wollpullover mit Zopfmuster, geölte Latzhose bis zur Brust mit Trägern, Netzbeutel."""
    s, skin, hair = base(look, feminine)
    cap(s, pal("#101a2a", "#18263c", "#22344f", "#2e4464", "#3e5678"), band=pal("#101a2a", "#18263c")[1], knit=True)
    knit = CREAM
    def sweater(x, y, w, h):
        if x % 4 == 1:
            return knit[2] if y % 2 else knit[3]   # Zopf
        return knit[3] if h01("fsweater", x, y) > 0.12 else knit[2]
    torso(s, sweater, sweater, sweater, top=knit[3])
    # Latzhose aus Öltuch: ab Brusthöhe, mit Trägern über die Schultern
    x0, y0, w, h = gs.face_rect("jacket", "front")
    for y in range(4, 12):
        for x in range(8):
            s.set(x0 + x, y0 + y, OILCLOTH[3] if h01("fbib", x, y) > 0.12 else OILCLOTH[2])
    for y in range(0, 4):
        s.set(x0 + 1, y0 + y, OILCLOTH[2])
        s.set(x0 + 6, y0 + y, OILCLOTH[2])
    s.set(x0 + 1, y0 + 4, BRASS[3])
    s.set(x0 + 6, y0 + 4, BRASS[3])
    for f in ("right", "left", "back"):
        x0, y0, w, h = gs.face_rect("jacket", f)
        for y in range(7, 12):
            for x in range(w):
                s.set(x0 + x, y0 + y, OILCLOTH[3] if h01("fbib" + f, x, y) > 0.12 else OILCLOTH[2])
    x0, y0, w, h = gs.face_rect("jacket", "back")
    for y in range(0, 7):
        s.set(x0 + 2 + y // 3, y0 + y, OILCLOTH[2])
        s.set(x0 + 5 - y // 3, y0 + y, OILCLOTH[2])
    arms(s, sweater, 10, skin, cuff=knit[1])
    if feminine:
        skirt_flaps(s, OILCLOTH, "fskirt", hem=OILCLOTH[1])
        dress_legs(s, OILCLOTH, BLACK_BOOTS, "fdress")
    else:
        legs(s, cloth(OILCLOTH, 3, "fwaders", folds=(1,), shade_bottom=False), BLACK_BOOTS, 7)
    # Netzbeutel mit einem Fisch darin
    satchel(s, net=pal("#3a3020", "#5a4a30", "#7a6848", "#9a8a68"))
    x0, y0, w, h = gs.face_rect("satchel", "back")
    for (x, y, c) in ((1, 1, "#7e96a8"), (2, 1, "#9ab0c0"), (2, 2, "#7e96a8"), (3, 2, "#5e7488")):
        s.set(x0 + x, y0 + y, hexc(c))
    return s


def shepherd(look, feminine: bool) -> gs.Skin:
    """Schäfer: breiter Filzhut, Schaffellweste über braunem Kittel, Ledertasche, graue Wollhose."""
    s, skin, hair = base(look, feminine)
    felt_hat(s, band=pal("#3a2a18", "#6a1e18")[1])
    tunic = cloth(BROWN, 3, "stunic", folds=(2, 5))
    def fleece(x, y, w, h):
        r = h01("fleece", x, y)
        return FLEECE[4] if r > 0.75 else FLEECE[3] if r > 0.3 else FLEECE[2]
    def front(x, y, w, h):
        if x in (0, 1, 6, 7):
            return fleece(x, y, w, h)
        if y == 0 and 2 <= x <= 5:
            return skin[2] if x in (3, 4) else BROWN[3]
        return tunic(x, y, w, h)
    torso(s, front, fleece, fleece, top=FLEECE[3])
    belt(s, 9, parts=("body",))
    arms(s, cloth(BROWN, 3, "ssleeve", folds=(1,)), 11, skin, cuff=BROWN[1])
    strap(s, LEATHER, rising=False)
    if feminine:
        skirt = pal("#2a2a20", "#3c3c2e", "#50503e", "#66664f", "#7e7e64")
        skirt_flaps(s, skirt, "sskirt", hem=skirt[1])
        dress_legs(s, skirt, DARK_BOOTS, "sdress")
    else:
        legs(s, cloth(gs.WOOL, 3, "strousers", folds=(1,), shade_bottom=False), DARK_BOOTS, 8)
    satchel(s, LEATHER)
    return s


def fletcher(look, feminine: bool) -> gs.Skin:
    """Bogner: grüne Kappe mit roter Feder, grüner Waffenrock unter einem Lederwams, Armschutz, Köcher voller Pfeile."""
    s, skin, hair = base(look, feminine)
    cap(s, FOREST, band=LEATHER[2])
    feather(s, pal("#3a0c0c", "#6e1a18", "#a02c24", "#c8483a", "#fbf6e6"))
    green = cloth(FOREST, 3, "fltunic", folds=(1, 6))
    def front(x, y, w, h):
        if 2 <= x <= 5 and y < 8:
            return leather(LEATHER, 3, "fljerkin")(x, y, w, h) if not (x in (3, 4) and y % 3 == 1) else BRASS[3]
        return green(x, y, w, h)
    torso(s, front, green, green, top=FOREST[3])
    # Lederwams auf dem Rücken
    x0, y0, w, h = gs.face_rect("body", "back")
    for y in range(8):
        for x in range(1, 7):
            s.set(x0 + x, y0 + y, leather(LEATHER, 3, "fljerkinb")(x, y, w, h))
    belt(s, 8, parts=("body",))
    arms(s, cloth(FOREST, 3, "flsleeve", folds=(1,)), 12, skin)
    # Armschutz am linken Unterarm
    s.rows("larm", 7, 10, lambda x, y, w, h: LEATHER[3] if y in (7, 10) else LEATHER[2])
    for arm in ("rarm", "larm"):
        s.band(arm, 11, skin[2])
    # Köcher: Leder mit Messingbeschlag, oben die Federn der Pfeile (weiß und rot)
    def quiver(x, y, w, h):
        if y == 0:
            return BRASS[3]
        if y in (4, 8):
            return LEATHER[1]
        return LEATHER[3] if x == 0 else LEATHER[2]
    for f in SIDES:
        s.fill("scroll", f, quiver)
    s.fill("scroll", "top", lambda x, y, w, h: [hexc("#f4efe4"), hexc("#b83a2c"), hexc("#d8d0c0")][(x + y) % 3])
    s.fill("scroll", "bottom", LEATHER[1])
    strap(s, LEATHER, rising=True)
    if feminine:
        skirt_flaps(s, FOREST, "flskirt", hem=FOREST[1])
        dress_legs(s, FOREST, DARK_BOOTS, "fldress")
    else:
        legs(s, cloth(BROWN, 3, "fltrousers", folds=(1,), shade_bottom=False), DARK_BOOTS, 6)
    return s


def scribe(look, feminine: bool) -> gs.Skin:
    """Schreiber: lange dunkle Gelehrtenrobe mit weißem Kragen, Brille, schwarze Kappe, tintenfleckige Finger, Büchertasche."""
    s, skin, hair = base(look, feminine)
    spectacles(s)
    if not feminine:
        # schwarze Kappe (zweite Kopfschicht, nur der Scheitel)
        s.fill("hat", "top", lambda x, y, w, h: INK[2] if (x + y) % 3 else INK[3])
        for f in ("front", "right", "left", "back"):
            x0, y0, w, h = gs.face_rect("hat", f)
            for x in range(w):
                s.set(x0 + x, y0, INK[2])
    robe = cloth(INK, 3, "srobe", folds=(1, 6))
    def front(x, y, w, h):
        if y <= 1 and 2 <= x <= 5:
            return WHITE[4] if y == 0 else WHITE[3]
        if x in (3, 4) and y > 1:
            return INK[2]
        return robe(x, y, w, h)
    torso(s, front, cloth(INK, 3, "sroberb", folds=(3,)), cloth(INK, 3, "srobes"), top=INK[3])
    s.band("body", 8, lambda x, y, w, h: BURGUNDY[3] if (x + y) % 2 else BURGUNDY[2])
    arms(s, cloth(INK, 3, "ssleeve", folds=(1,)), 11, skin, cuff=WHITE[3])
    x0, y0, w, h = gs.face_rect("rarm", "front")
    s.set(x0 + 1, y0 + 11, hexc("#20305a"))
    s.set(x0 + 2, y0 + 11, hexc("#20305a"))
    skirt_flaps(s, INK, "sskirt" + look[0], hem=INK[1])
    dress_legs(s, INK, BLACK_BOOTS, "sdress")
    strap(s, BROWN, rising=False)
    satchel(s, BROWN, flap=LEATHER)
    # ein Buchrücken schaut aus der Tasche
    x0, y0, w, h = gs.face_rect("satchel", "top")
    s.set(x0 + 1, y0, hexc("#8a2a20"))
    s.set(x0 + 2, y0, hexc("#203a6a"))
    return s


def herbalist(look, feminine: bool) -> gs.Skin:
    """Kräuterkundige: Gugel und lange moosgrüne Kutte, Gürtel mit Fläschchen, Kräutertasche."""
    s, skin, hair = base(look, feminine)
    hood(s, pal("#26180e", "#3a2616", "#4e3620", "#62462c", "#785a3a"), seed="hhood" + look[0])
    robe = cloth(HERB, 3, "hrobe", folds=(2, 5))
    torso(s, robe, cloth(HERB, 3, "hrobeb", folds=(3,)), cloth(HERB, 3, "hrobes"), top=HERB[3])
    belt(s, 8, ramp=BROWN, buckle=BRASS)
    # Fläschchen am Gürtel (zweite Schicht)
    x0, y0, w, h = gs.face_rect("jacket", "front")
    for (x, c1, c2) in ((1, "#c83a3a", "#ff8a7a"), (5, "#3a7ac8", "#8ac0ff"), (6, "#5ab040", "#a8e888")):
        s.set(x0 + x, y0 + 9, hexc(c1))
        s.set(x0 + x, y0 + 10, hexc(c2))
        s.set(x0 + x, y0 + 8, hexc("#e8e4dc"))
    arms(s, cloth(HERB, 3, "hsleeve", folds=(1,)), 11, skin, cuff=HERB[1])
    skirt_flaps(s, HERB, "hskirt" + look[0], hem=HERB[1])
    dress_legs(s, HERB, DARK_BOOTS, "hdress")
    strap(s, BROWN, rising=True)
    satchel(s, LEATHER, sprigs=pal("#1a3a12", "#2a5a1c", "#3c7a28", "#5a9a3a", "#8ac85a"))
    return s


def smith_base(s, skin, shirt_paint, shirt_ramp, apron, cuff, gloves=True):
    torso(s, shirt_paint, shirt_paint, shirt_paint, top=shirt_ramp[3])
    apron_body(s, apron, 2, "vapron", bib=True)
    arms(s, shirt_paint, 4, skin, gloves=apron if gloves else None, glove_from=8, cuff=cuff)
    for sleeve in ("rsleeve", "lsleeve"):
        s.rows(sleeve, 8, 9, lambda x, y, w, h: apron[3] if y == 8 else apron[2])
    apron_flap(s, apron, "vflap")


def armorer(look, feminine: bool) -> gs.Skin:
    """Rüstungsschmied: Kettenhemd unter der Lederschürze, Lederkappe mit Nackenschutz, schwere Handschuhe."""
    s, skin, hair = base(look, feminine)
    x0, y0, w, h = gs.face_rect("head", "front")
    s.set(x0 + 1, y0 + 6, mix(skin[1], hexc("#202020"), 0.35))
    mail = gs.chainmail()
    apron = pal("#2a1a0e", "#43301c", "#5e4428", "#7a5a36", "#957048")
    smith_base(s, skin, mail, CHAIN + [CHAIN[3]], apron, CHAIN[1])
    # Kettenhaube um Hals und Schultern (zweite Kopfschicht unten, Ärmel oben)
    for f in ("right", "left", "back"):
        x0, y0, w, h = gs.face_rect("hat", f)
        for y in range(5, 8):
            for x in range(w):
                s.set(x0 + x, y0 + y, CHAIN[2] if (x + y) % 2 else CHAIN[1])
    for sleeve in ("rsleeve", "lsleeve"):
        s.fill(sleeve, "top", lambda x, y, w, h: CHAIN[2] if (x + y) % 2 else CHAIN[3])
        s.rows(sleeve, 0, 1, lambda x, y, w, h: CHAIN[2] if (x + y) % 2 else CHAIN[1])
    if feminine:
        skirt_flaps(s, CHARCOAL, "askirt", hem=CHARCOAL[1])
        s.fill_all("flap_front", lambda x, y, w, h: apron[3] if x not in (0, 8) else apron[2])
        dress_legs(s, CHARCOAL, BLACK_BOOTS, "adress")
    else:
        legs(s, cloth(CHARCOAL, 2, "atrousers", folds=(1,), shade_bottom=False), BLACK_BOOTS, 8)
    return s


def weaponsmith(look, feminine: bool) -> gs.Skin:
    """Waffenschmied: dunkelrotes Hemd, schwarze Lederschürze, Augenklappe (der Ältere), Funkenspuren."""
    s, skin, hair = base(look, feminine)
    if look[3] in ("fullbeard", "mustache"):
        eyepatch(s)
    shirt = soot(cloth(RUST, 3, "wshirt", folds=(3,)), 0.1, "wsoot")
    apron = pal("#0e0c0c", "#1a1716", "#282422", "#38322e", "#4a423c")
    smith_base(s, skin, shirt, RUST, apron, RUST[1])
    belt(s, 8, ramp=BROWN, buckle=IRON, parts=("jacket",))
    if feminine:
        skirt_flaps(s, BURGUNDY, "wskirt", hem=BURGUNDY[1])
        s.fill_all("flap_front", lambda x, y, w, h: apron[3] if x not in (0, 8) else apron[2])
        dress_legs(s, BURGUNDY, BLACK_BOOTS, "wdress")
    else:
        legs(s, cloth(CHARCOAL, 2, "wtrousers", folds=(1,), shade_bottom=False), BLACK_BOOTS, 8)
    return s


def toolsmith(look, feminine: bool) -> gs.Skin:
    """Werkzeugschmied: Lederkappe, blaues Arbeitshemd, braune Schürze mit Zange und Hammer in den Taschen."""
    s, skin, hair = base(look, feminine)
    cap(s, LEATHER, band=LEATHER[1])
    blue = pal("#16243e", "#203458", "#2c4672", "#3a5a8c", "#4e70a6")
    shirt = cloth(blue, 3, "tshirt", folds=(2, 5))
    apron = pal("#2a1a0e", "#43301c", "#5e4428", "#7a5a36", "#957048")
    smith_base(s, skin, shirt, blue, apron, blue[1], gloves=False)
    # Werkzeug in der Schürzentasche
    x0, y0, w, h = gs.face_rect("jacket", "front")
    for y in range(7, 11):
        s.set(x0 + 2, y0 + y, IRON[3] if y == 7 else BROWN[3])
    s.set(x0 + 5, y0 + 7, IRON[3])
    s.set(x0 + 6, y0 + 7, IRON[3])
    s.set(x0 + 5, y0 + 8, IRON[2])
    s.set(x0 + 6, y0 + 9, IRON[2])
    if feminine:
        skirt_flaps(s, blue, "tskirt", hem=blue[1])
        s.fill_all("flap_front", lambda x, y, w, h: apron[3] if x not in (0, 8) else apron[2])
        dress_legs(s, blue, DARK_BOOTS, "tdress")
    else:
        legs(s, cloth(BROWN, 3, "ttrousers", folds=(1,), shade_bottom=False), DARK_BOOTS, 8)
        knee_pads(s)
    return s


def butcher(look, feminine: bool) -> gs.Skin:
    """Metzger: weiße Stoffkappe, rot-weiß gestreiftes Hemd, weiße Schürze mit Blutflecken."""
    s, skin, hair = base(look, feminine)
    cap(s, WHITE, band=hexc("#a82a2a"))
    red = pal("#3a0c0c", "#5c1414", "#86201c", "#a8302a", "#c44a3a")
    def stripes(x, y, w, h):
        return red[3] if x % 2 == 0 else WHITE[3]
    torso(s, stripes, stripes, stripes, top=WHITE[3])
    apron_body(s, WHITE, 3, "bapron", bib=True)
    x0, y0, w, h = gs.face_rect("jacket", "front")
    for (x, y) in ((2, 6), (3, 6), (5, 9), (2, 10), (6, 4)):
        s.set(x0 + x, y0 + y, BLOOD)
    arms(s, stripes, 5, skin, cuff=red[1])
    apron_flap(s, WHITE, "bflap")
    x0, y0, w, h = gs.face_rect("flap_front", "front")
    for (x, y) in ((2, 1), (6, 3), (4, 5)):
        s.set(x0 + x, y0 + y, BLOOD)
    if feminine:
        skirt_flaps(s, red, "bskirt", hem=red[1])
        s.fill_all("flap_front", lambda x, y, w, h: WHITE[3] if x not in (0, 8) else WHITE[2])
        x0, y0, w, h = gs.face_rect("flap_front", "front")
        for (x, y) in ((2, 1), (6, 3), (4, 5)):
            s.set(x0 + x, y0 + y, BLOOD)
        dress_legs(s, red, BLACK_BOOTS, "bdress")
    else:
        legs(s, cloth(CHARCOAL, 3, "btrousers", folds=(1,), shade_bottom=False), BLACK_BOOTS, 8)
    return s


def tanner(look, feminine: bool) -> gs.Skin:
    """Gerber: aufgekrempelte Ärmel, fleckiges Hemd, dicke Lederschürze, braune Hände vom Gerben."""
    s, skin, hair = base(look, feminine)
    shirt = speckle(cloth(SHIRT, 2, "gshirt", folds=(2, 5)), hexc("#5a3a1c"), 0.08, "gstain")
    apron = pal("#2a160a", "#40220f", "#5a3216", "#74441f", "#8e5a2c")
    torso(s, shirt, shirt, shirt, top=SHIRT[3])
    apron_body(s, apron, 3, "gapron", bib=True)
    arms(s, shirt, 4, skin, cuff=SHIRT[1])
    # aufgekrempelte Ärmel und braun gefärbte Hände
    for arm in ("rarm", "larm"):
        s.band(arm, 4, SHIRT[3])
        s.rows(arm, 10, 11, lambda x, y, w, h: mix(skin[1], hexc("#4a2a10"), 0.45))
    apron_flap(s, apron, "gflap")
    if feminine:
        skirt_flaps(s, BROWN, "gskirt", hem=BROWN[1])
        s.fill_all("flap_front", lambda x, y, w, h: apron[3] if x not in (0, 8) else apron[2])
        dress_legs(s, BROWN, DARK_BOOTS, "gdress")
    else:
        legs(s, cloth(BROWN, 3, "gtrousers", folds=(1,), shade_bottom=False), DARK_BOOTS, 7)
    return s


def mason(look, feminine: bool) -> gs.Skin:
    """Steinmetz: staubige graue Filzkappe, staubiges Hemd, Lederbeutel mit Meißel, Knieschoner."""
    s, skin, hair = base(look, feminine)
    dust = hexc("#d8d4cc")
    cap(s, STONE, band=STONE[1])
    shirt = speckle(cloth(STONE, 3, "mshirt", folds=(2, 5)), dust, 0.12, "mdust")
    def front(x, y, w, h):
        if y == 0 and 2 <= x <= 5:
            return skin[2] if x in (3, 4) else STONE[2]
        return shirt(x, y, w, h)
    torso(s, front, shirt, shirt, top=STONE[3])
    belt(s, 8, ramp=LEATHER, buckle=IRON, parts=("body",))
    arms(s, shirt, 6, skin, cuff=STONE[1])
    for arm in ("rarm", "larm"):
        s.rows(arm, 6, 11, speckle(lambda x, y, w, h: skin[2] if (x + y) % 5 else skin[3], dust, 0.1, "marm" + arm))
    pouch(s, LEATHER)
    x0, y0, w, h = gs.face_rect("pouch", "top")
    s.set(x0, y0, IRON[4])
    if feminine:
        skirt_flaps(s, STONE, "mskirt", hem=STONE[1])
        dress_legs(s, STONE, DARK_BOOTS, "mdress")
    else:
        legs(s, speckle(cloth(BROWN, 3, "mtrousers", folds=(1,), shade_bottom=False), dust, 0.06, "mtd"), DARK_BOOTS, 8)
        knee_pads(s)
    return s


def baker(look, feminine: bool) -> gs.Skin:
    """Bäcker: hohe weiße Mütze, weiße Jacke mit doppelter Knopfreihe, Mehlstaub, weiße Schürze, karierte Hose."""
    s, skin, hair = base(look, feminine)
    toque(s)
    # Mehl auf Wange und Nase
    x0, y0, w, h = gs.face_rect("head", "front")
    s.set(x0 + 6, y0 + 5, mix(skin[3], FLOUR, 0.6))
    jacket = speckle(cloth(WHITE, 3, "kjacket", folds=(1, 6)), FLOUR, 0.1, "kflour")
    def front(x, y, w, h):
        if x in (2, 5) and y in (1, 3, 5):
            return WHITE[1]
        return jacket(x, y, w, h)
    torso(s, front, jacket, jacket, top=WHITE[3])
    apron_body(s, WHITE, 6, "kapron", bib=False)
    s.band("jacket", 6, hexc("#c8b48a"))
    arms(s, jacket, 7, skin, cuff=WHITE[2])
    for arm in ("rarm", "larm"):
        s.rows(arm, 7, 11, speckle(lambda x, y, w, h: skin[2] if (x + y) % 5 else skin[3], FLOUR, 0.25, "karm" + arm))
    apron_flap(s, WHITE, "kflap")
    if feminine:
        blue = pal("#1c2a44", "#26385a", "#324a74", "#405e8e", "#5474a8")
        skirt_flaps(s, blue, "kskirt", hem=blue[1])
        s.fill_all("flap_front", lambda x, y, w, h: WHITE[3] if x not in (0, 8) else WHITE[2])
        dress_legs(s, blue, BLACK_BOOTS, "kdress")
    else:
        def check(x, y, w, h):
            return CHARCOAL[3] if (x // 1 + y // 1) % 2 else WHITE[2]
        legs(s, check, BLACK_BOOTS, 10)
    return s


# =============================================================================
# Berufe, die wie die Bürger der eigenen Stadt gekleidet sind (mit eigenen Gesichtern)
# =============================================================================

def farmer(look, feminine):
    return gc.farmer(look[:4], feminine)


def cartographer(look, feminine):
    return gc.cartographer(look[:4], feminine)


def innkeeper(look, feminine):
    return gc.innkeeper(look[:4], feminine)


def merchant(look, feminine):
    return gc.merchant(look[:4], feminine)


# Gesichter: (Hautton, Haare, Augen, Gesicht[, Farbvariante der Gugel])
PROFESSIONS = {
    "peasant": (peasant, [("medium", "brown", "brown", "stubble", 0), ("light", "blond", "blue", "plain", 1),
                          ("tan", "black", "brown", "woman", 2)]),
    "farmer": (farmer, [("tan", "black", "brown", "fullbeard"), ("light", "red", "green", "stubble"), ("medium", "blond", "blue", "woman")]),
    "fisher": (fisher, [("light", "gray", "blue", "fullbeard"), ("tan", "brown", "hazel", "stubble"), ("light", "red", "green", "freckles")]),
    "shepherd": (shepherd, [("medium", "gray", "brown", "beard"), ("light", "blond", "blue", "plain"), ("tan", "chestnut", "hazel", "woman")]),
    "fletcher": (fletcher, [("light", "brown", "green", "mustache"), ("dark", "black", "brown", "stubble"), ("light", "blond", "blue", "woman")]),
    "scribe": (scribe, [("light", "gray", "gray", "beard"), ("medium", "brown", "brown", "plain"), ("light", "chestnut", "green", "woman")]),
    "cartographer": (cartographer, [("tan", "brown", "brown", "mustache"), ("light", "blond", "blue", "plain"),
                                    ("medium", "red", "green", "woman")]),
    "herbalist": (herbalist, [("light", "gray", "green", "fullbeard"), ("tan", "black", "brown", "plain"), ("light", "red", "green", "freckles")]),
    "armorer": (armorer, [("medium", "black", "brown", "fullbeard"), ("light", "red", "blue", "stubble"), ("tan", "brown", "hazel", "woman")]),
    "weaponsmith": (weaponsmith, [("light", "brown", "gray", "mustache"), ("dark", "black", "brown", "stubble"), ("light", "blond", "blue", "woman")]),
    "toolsmith": (toolsmith, [("medium", "chestnut", "brown", "beard"), ("light", "blond", "green", "plain"), ("tan", "black", "brown", "woman")]),
    "butcher": (butcher, [("light", "red", "blue", "fullbeard"), ("medium", "brown", "brown", "mustache"), ("light", "brown", "hazel", "woman")]),
    "tanner": (tanner, [("tan", "brown", "brown", "stubble"), ("light", "gray", "blue", "beard"), ("medium", "black", "brown", "woman")]),
    "mason": (mason, [("light", "gray", "gray", "fullbeard"), ("medium", "brown", "brown", "stubble"), ("light", "chestnut", "hazel", "woman")]),
    "baker": (baker, [("light", "blond", "blue", "mustache"), ("medium", "brown", "brown", "plain"), ("light", "red", "green", "freckles")]),
    "innkeeper": (innkeeper, [("medium", "brown", "brown", "beard"), ("light", "red", "blue", "fullbeard"), ("light", "blond", "green", "woman")]),
    "merchant": (merchant, [("tan", "black", "brown", "beard"), ("light", "gray", "blue", "mustache"), ("medium", "chestnut", "hazel", "woman")]),
}


def all_skins() -> dict[str, Image.Image]:
    out: dict[str, Image.Image] = {}
    for name, (painter, looks) in PROFESSIONS.items():
        for i, look in enumerate(looks):
            out[f"{name}_{i + 1}"] = painter(look, i == len(looks) - 1).img
    return out


def main():
    skins = all_skins()
    TEX.mkdir(parents=True, exist_ok=True)
    for key, img in skins.items():
        img.save(TEX / f"{key}.png")
    print(f"{len(skins)} Dorfbewohner-Texturen geschrieben")
    if "--preview" in sys.argv:
        scale = 5
        keys = list(skins)
        cols = 9
        rows = (len(keys) + cols - 1) // cols
        sheet = Image.new("RGBA", (cols * 36 * scale, rows * 34 * scale), (90, 110, 130, 255))
        for i, key in enumerate(keys):
            for j, back in enumerate((False, True)):
                v = gs.front_view(skins[key], back).resize((16 * scale, 32 * scale), Image.NEAREST)
                sheet.alpha_composite(v, ((i % cols) * 36 * scale + j * 17 * scale, (i // cols) * 34 * scale + 4))
        sheet.save(SCRATCH / "villager_preview.png")
        print("Vorschau:", SCRATCH / "villager_preview.png")


if __name__ == "__main__":
    main()
