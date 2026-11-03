#!/usr/bin/env python3
"""
Erzeugt die Texturen der Bürger (128x64, eigene Pixelkunst im Vanilla-Stil), je Beruf drei Gesichter; das dritte ist
eine Frau (lange Haare und Kleid).

Aufbau einer Textur (siehe client/model/CitizenModel.java):
  * links (64x64) das gewohnte Spieler-Schema mit zweiter Schicht (Kappe, Weste, Gürtel, Schürze, Stulpen)
  * rechts die Zusatzteile: Krempe (64,0), Hut/Helm (64,16), Kappe/Barett (96,16), lange Haare (64,28), Feder (84,28),
    Grubenlampe (92,28), Geldbeutel (98,28), Kartenköcher (106,28), Schürze bzw. Rock vorn (64,42) und hinten (64,52)

Aufruf (aus dem Projektordner):
    python3 tools/generate_citizens.py            # schreibt alle Texturen
    python3 tools/generate_citizens.py --preview  # zusätzlich eine Vorschau (Vorder- und Rückansicht)
"""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
import generate_soldiers as gs  # noqa: E402
from generate_soldiers import (  # noqa: E402
    GOLD, HAIRS, IRISES, LEATHER, LINEN, OLIVE, SKINS, WOOL, Skin, boots, cloth, face_codes, h01, leather, paint_head, pal,
)
from pixelart import CLEAR, hexc, mix  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
TEX = ROOT / "src" / "main" / "resources" / "assets" / "kingdomsatwar" / "textures" / "entity" / "citizen"
SCRATCH = Path("/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad")

gs.PARTS.update({
    "brim": (64, 0, 14, 1, 14),
    "crown": (64, 16, 8, 4, 8),
    "cap": (96, 16, 8, 2, 8),
    "long_hair": (64, 28, 9, 7, 1),
    "feather": (84, 28, 1, 5, 3),
    "lamp": (92, 28, 2, 2, 1),
    "pouch": (98, 28, 2, 3, 2),
    "scroll": (106, 28, 3, 10, 3),
    "flap_front": (64, 42, 9, 7, 1),
    "flap_back": (64, 52, 9, 7, 1),
})

SIDES = ("right", "front", "left", "back")
ALL = ("top", "bottom", "right", "front", "left", "back")

# =============================================================================
# Stoffe
# =============================================================================

STRAW = pal("#5e4618", "#8a6a28", "#b48e3c", "#d4b05a", "#ead27e", "#f6e6a8")
SHIRT = pal("#6e624a", "#8f8264", "#b3a582", "#cfc29e", "#e6dbbb")
WHITE = pal("#8c887c", "#b0aca0", "#cfccc2", "#e6e3da", "#f6f4ee")
BROWN = pal("#2a1a10", "#3e2818", "#563a22", "#6e4c2e", "#86603c")
CHARCOAL = pal("#15151a", "#202128", "#2d2f37", "#3d4049", "#50545e")
PLAID_RED = pal("#3a0c0c", "#5c1414", "#86201c", "#a8302a", "#c44a3a")
NAVY = pal("#0f1630", "#18234a", "#22305f", "#2f4178", "#425692")
BURGUNDY = pal("#2a0a14", "#45101f", "#62182c", "#80243a", "#9c3a4e")
TEAL = pal("#0c2426", "#133638", "#1b4a4c", "#256062", "#347a7a")
MOSS = pal("#1a2412", "#26341a", "#344624", "#445a30", "#587040")
SKY = pal("#1c3550", "#284a6c", "#36608a", "#4a78a4", "#6492bc")
ROSE = pal("#4a1a24", "#6a2634", "#8a3646", "#a84c5a", "#c06a74")
BRASS = pal("#4a3410", "#7a5a1c", "#a8822c", "#cfa848", "#ead27e")
IRON = pal("#26292e", "#3b4048", "#565c66", "#757c86", "#9aa1aa")


def plaid(base: list, dark: list):
    """Karomuster (Holzfäller): rote Grundfläche, dunkle Streifen jede vierte Reihe und Spalte."""
    def f(x, y, w, h):
        if y % 4 == 2 and x % 4 == 1:
            return dark[0]
        if y % 4 == 2 or x % 4 == 1:
            return dark[1]
        return base[3] if h01("plaid", x, y) > 0.15 else base[2]
    return f


def soot(paint, amount: float = 0.18, seed: str = "soot"):
    """Rußflecken über einem Muster."""
    def f(x, y, w, h):
        c = paint(x, y, w, h) if callable(paint) else paint
        r = h01(seed, x, y)
        return mix(c, hexc("#141414"), 0.35) if r < amount else c
    return f


# =============================================================================
# Gemeinsame Bausteine
# =============================================================================

def torso(s: Skin, front_paint, back_paint=None, side_paint=None, top=None, bottom=None):
    back_paint = back_paint or front_paint
    side_paint = side_paint or front_paint
    s.fill("body", "front", front_paint)
    s.fill("body", "back", back_paint)
    s.fill("body", "right", side_paint)
    s.fill("body", "left", side_paint)
    s.fill("body", "top", top or side_paint)
    s.fill("body", "bottom", bottom or side_paint)


def belt(s: Skin, row: int, ramp=LEATHER, buckle=BRASS, parts=("body", "jacket")):
    for part in parts:
        s.band(part, row, lambda x, y, w, h: ramp[2] if (x + y) % 3 else ramp[1])
        x0, y0, w, h = gs.face_rect(part, "front")
        s.set(x0 + 3, y0 + row, buckle[3])
        s.set(x0 + 4, y0 + row, buckle[2])


def arms(s: Skin, sleeve_paint, sleeve_rows: int, skin: list, gloves=None, glove_from: int = 9, cuff=None):
    """Ärmel bis Zeile {@code sleeve_rows-1}, darunter Haut (oder Handschuhe ab {@code glove_from})."""
    for arm in ("rarm", "larm"):
        def f(x, y, w, h, arm=arm):
            if y < sleeve_rows:
                return sleeve_paint(x, y, w, h) if callable(sleeve_paint) else sleeve_paint
            if gloves is not None and y >= glove_from:
                return gloves[2] if y < 11 else gloves[1]
            return skin[1] if y == 11 else skin[2] if (x + y) % 5 else skin[3]
        s.fill_all(arm, f, SIDES)
        s.fill(arm, "top", sleeve_paint)
        s.fill(arm, "bottom", (gloves or skin)[1])
        if cuff is not None and sleeve_rows < 12:
            s.band(arm, sleeve_rows - 1, cuff)


def legs(s: Skin, trousers_paint, boot_ramp=None, boot_from: int = 8, cuff: bool = True):
    for leg in ("rleg", "lleg"):
        s.fill_all(leg, trousers_paint, ("top", "right", "front", "left", "back"))
        if boot_ramp is not None:
            boots(s, leg, boot_from, boot_ramp, cuff=cuff)


def clear_outer(s: Skin):
    for part in ("hat", "jacket", "rsleeve", "lsleeve", "rpants", "lpants"):
        s.clear(part)


def long_hair(s: Skin, hair: list):
    """Lange Haare über den Rücken (Zusatzteil) und Strähnen an den Kopfseiten (zweite Schicht)."""
    def strand(x, y, w, h):
        i = 2 + (1 if (x * 3 + y) % 5 == 0 else 0) - (1 if x % 3 == 2 else 0)
        if y >= h - 1:
            i -= 1
        return hair[max(0, min(4, i))]
    s.fill_all("long_hair", strand)
    s.fill("long_hair", "top", hair[3])
    # Strähnen seitlich am Kopf (hinten), die zum Rücken fallen
    side = ["........", "......cd", ".....cdc", ".....cbc", "....bcbc", "....cbcb", "....bcbb", "....abba"]
    codes = {"a": hair[0], "b": hair[1], "c": hair[2], "d": hair[3]}
    s.art("hat", "right", side, codes)
    s.art("hat", "left", [r[::-1] for r in side], codes)
    s.art("hat", "back", ["........", "........", "cdccdccd", "cbccbccb", "bcbbcbbc", "bbcbbcbb", "abbaabba", "abbaabba"], codes)


def dress_legs(s: Skin, fabric: list, shoe: list, seed: str):
    """Kleid: die Beine tragen bis zum Knöchel den Stoff des Kleides, darunter Schuhe."""
    for leg in ("rleg", "lleg"):
        def f(x, y, w, h, leg=leg):
            if y >= 10:
                return shoe[1] if y == 11 else shoe[2]
            i = 2
            if x in (1,) and y > 2:
                i = 1
            if h01(seed, leg, x, y) > 0.92:
                i = 3
            if y == 9:
                i = 1
            return fabric[i]
        s.fill_all(leg, f, ("right", "front", "left", "back"))
        s.fill(leg, "top", fabric[2])
        s.fill(leg, "bottom", shoe[0])


def skirt_flaps(s: Skin, fabric: list, seed: str, hem=None):
    """Vorder- und Rückteil des Rocks: Falten, unten ein Saum."""
    def f(x, y, w, h):
        i = 2
        if x % 3 == 1:
            i = 1
        elif x % 3 == 2 and y < h - 2:
            i = 3
        if y >= h - 1:
            return hem if hem is not None else fabric[1]
        if h01(seed, x, y) > 0.94:
            i += 1
        return fabric[max(0, min(len(fabric) - 1, i))]
    for part in ("flap_front", "flap_back"):
        s.fill_all(part, f)
        s.fill(part, "top", fabric[2])


def apron_flap(s: Skin, ramp: list, seed: str, strings=None):
    """Untere Schürze (vorn): glatter Stoff oder Leder mit Saum."""
    def f(x, y, w, h):
        i = 3 if x not in (0, w - 1) else 2
        if y == h - 1:
            i = 1
        elif h01(seed, x, y) < 0.1:
            i -= 1
        return ramp[i]
    s.fill_all("flap_front", f)
    s.fill("flap_front", "top", ramp[2])


def apron_body(s: Skin, ramp: list, from_row: int, seed: str, bib: bool):
    """Schürze auf dem Körper (zweite Schicht): mit Latz (Schmied) oder nur ab der Hüfte (Wirt)."""
    x0, y0, w, h = gs.face_rect("jacket", "front")
    for y in range(h):
        for x in range(w):
            inside = y >= from_row or (bib and 1 <= x <= 6 and y >= 1)
            if inside:
                i = 3 if x not in (0, w - 1) else 2
                if h01(seed, x, y) < 0.1:
                    i -= 1
                s.set(x0 + x, y0 + y, ramp[i])
    if bib:
        # Bänder über die Schultern
        for f_ in ("top",):
            x0, y0, w, h = gs.face_rect("jacket", f_)
            for dy in range(h):
                s.set(x0 + 1, y0 + dy, ramp[1])
                s.set(x0 + 6, y0 + dy, ramp[1])
        x0, y0, w, h = gs.face_rect("jacket", "back")
        for y in range(0, 8):
            s.set(x0 + 1 + y // 2, y0 + y, ramp[1])
            s.set(x0 + 6 - y // 2, y0 + y, ramp[1])
    # Band um die Hüfte
    s.band("jacket", from_row if not bib else 7, ramp[1])


# =============================================================================
# Kopfbedeckungen
# =============================================================================

def straw_hat(s: Skin):
    def weave(x, y, w, h):
        i = 3 if (x + y) % 2 else 4
        if h01("straw", x, y) < 0.12:
            i -= 1
        return STRAW[i]
    # Krempe: oben geflochten, nach außen etwas dunkler; unten im Schatten
    def top(x, y, w, h):
        c = weave(x, y, w, h)
        edge = min(x, y, w - 1 - x, h - 1 - y)
        return mix(c, STRAW[1], 0.35) if edge == 0 else c
    s.fill("brim", "top", top)
    s.fill("brim", "bottom", lambda x, y, w, h: STRAW[2] if (x + y) % 2 else STRAW[1])
    for f_ in SIDES:
        s.fill("brim", f_, STRAW[2])
    # Hutkopf mit rotem Band unten
    s.fill("crown", "top", weave)
    for f_ in SIDES:
        s.fill("crown", f_, lambda x, y, w, h: hexc("#8a2a20") if y == 3 else weave(x, y, w, h))
    s.clear("crown", ("bottom",))


def miner_helmet(s: Skin):
    hard = pal("#2a1a0e", "#43301c", "#5e4428", "#7a5a36", "#957048")
    def shell(x, y, w, h):
        i = 3 if (x + y) % 4 else 2
        if y == 0:
            i = 4
        return hard[i]
    s.fill("crown", "top", lambda x, y, w, h: hard[4] if 2 <= x <= 5 and 2 <= y <= 5 else hard[3])
    for f_ in SIDES:
        s.fill("crown", f_, lambda x, y, w, h: IRON[2] if y == 3 else shell(x, y, w, h))
    s.clear("crown", ("bottom",))
    # Lampe: Messinggehäuse, vorn ein warmes Licht
    s.fill_all("lamp", BRASS[2])
    s.fill("lamp", "front", lambda x, y, w, h: hexc("#fff2b0") if (x + y) % 2 == 0 else hexc("#ffd060"))
    s.fill("lamp", "top", BRASS[3])


def cap(s: Skin, ramp: list, band=None, brooch=None, knit: bool = False):
    def top(x, y, w, h):
        if knit:
            return ramp[3] if (x // 2 + y) % 2 else ramp[2]
        if 3 <= x <= 4 and 3 <= y <= 4:
            return ramp[4]
        return ramp[3] if h01("cap", x, y) > 0.15 else ramp[2]
    s.fill("cap", "top", top)
    for f_ in SIDES:
        def side(x, y, w, h):
            if y == 1 and band is not None:
                return band
            if knit:
                return ramp[2] if x % 2 else ramp[3]
            return ramp[3] if y == 0 else ramp[2]
        s.fill("cap", f_, side)
    s.clear("cap", ("bottom",))
    if brooch is not None:
        x0, y0, w, h = gs.face_rect("cap", "front")
        s.set(x0 + 5, y0, brooch[4])
        s.set(x0 + 5, y0 + 1, brooch[2])


def feather(s: Skin, color: list):
    def f(x, y, w, h):
        if y == 0:
            return color[4]
        return color[3] if x % 2 == 0 else color[2]
    s.fill_all("feather", f)
    s.fill("feather", "right", lambda x, y, w, h: WHITE[4] if y < 2 else color[3] if x == 1 else color[2])
    s.fill("feather", "left", lambda x, y, w, h: WHITE[4] if y < 2 else color[3] if x == 1 else color[2])


def pouch(s: Skin, ramp=LEATHER):
    s.fill_all("pouch", lambda x, y, w, h: ramp[3] if y > 0 else ramp[4])
    s.fill("pouch", "top", ramp[2])
    s.fill("pouch", "front", lambda x, y, w, h: GOLD[3] if (x, y) == (0, 1) else ramp[3] if y > 0 else ramp[4])


def scroll_case(s: Skin):
    case = pal("#24160c", "#3a2414", "#54361e", "#6e4a2a", "#8a6038")
    def body(x, y, w, h):
        if y in (0, h - 1):
            return BRASS[3] if y == 0 else BRASS[1]
        if y == 4:
            return BRASS[2]
        return case[3] if x == 0 else case[2]
    for f_ in SIDES:
        s.fill("scroll", f_, body)
    s.fill("scroll", "top", lambda x, y, w, h: WHITE[4] if (x + y) % 2 else hexc("#e8dcc0"))
    s.fill("scroll", "bottom", BRASS[1])


def spectacles(s: Skin):
    """Drahtbrille auf der zweiten Kopfschicht (vor den Augen)."""
    wire = BRASS[3]
    x0, y0, w, h = gs.face_rect("hat", "front")
    for (x, y) in ((0, 4), (3, 4), (4, 4), (7, 4), (1, 3), (2, 3), (5, 3), (6, 3), (1, 5), (2, 5), (5, 5), (6, 5)):
        s.set(x0 + x, y0 + y, wire)
    for (x, y) in ((1, 4), (2, 4), (5, 4), (6, 4)):
        s.set(x0 + x, y0 + y, (220, 235, 245, 70))


# =============================================================================
# Berufe
# =============================================================================

def base(look: tuple, feminine: bool) -> tuple[Skin, list, list]:
    s = Skin()
    skin_name, hair_name, iris, face = look
    skin, hair = SKINS[skin_name], HAIRS[hair_name]
    paint_head(s, skin, hair, IRISES[iris], face, long_hair=feminine)
    clear_outer(s)
    if feminine:
        long_hair(s, hair)
    return s, skin, hair


def builder(look, feminine: bool) -> Skin:
    s, skin, hair = base(look, feminine)
    felt = pal("#2e2012", "#46321c", "#5e4426", "#785832", "#906c40")
    cap(s, felt, band=felt[0])
    vest = LEATHER
    shirt = cloth(SHIRT, 3, "bshirt", folds=(2,))
    def front(x, y, w, h):
        if y == 0 and 2 <= x <= 5:
            return skin[2] if x in (3, 4) else SHIRT[2]
        if x in (0, 1, 6, 7):
            return leather(vest, 3, "bvest")(x, y, w, h)
        return shirt(x, y, w, h)
    torso(s, front, leather(vest, 3, "bvestb"), leather(vest, 3, "bvests"), top=SHIRT[3])
    belt(s, 8)
    # Zollstock und Hammer am Gürtel (zweite Schicht)
    x0, y0, w, h = gs.face_rect("jacket", "front")
    for y in range(8, 12):
        s.set(x0 + 6, y0 + y, hexc("#e2c24a") if y % 2 else hexc("#3a3020"))
    x0, y0, w, h = gs.face_rect("jacket", "right")
    for y in range(7, 11):
        s.set(x0 + 1, y0 + y, BROWN[3])
    s.set(x0 + 0, y0 + 7, IRON[3])
    s.set(x0 + 1, y0 + 7, IRON[2])
    s.set(x0 + 2, y0 + 7, IRON[3])
    arms(s, cloth(SHIRT, 3, "bsleeve", folds=(1,)), 6, skin, cuff=SHIRT[1])
    if feminine:
        skirt_flaps(s, OLIVE, "bskirt")
        dress_legs(s, OLIVE, BROWN, "bdress")
    else:
        legs(s, cloth(BROWN, 3, "btrousers", folds=(1,), shade_bottom=False), pal("#1a120c", "#2a1d13", "#3a2a1c", "#4c3826", "#5e4632"), 8)
        # Knieflicken
        for leg in ("rleg", "lleg"):
            s.art(leg, "front", ["....", "....", "....", "....", ".ee.", ".ee.", "....", "....", "....", "....", "....", "...."],
                  {"e": BROWN[4]})
    pouch(s)
    return s


def farmer(look, feminine: bool) -> Skin:
    s, skin, hair = base(look, feminine)
    straw_hat(s)
    tunic = OLIVE if not feminine else SKY
    rope = STRAW[2]
    def front(x, y, w, h):
        if y <= 1 and 2 <= x <= 5:
            return SHIRT[3] if y == 0 else SHIRT[2]
        if y == 2 and x in (3, 4):
            return SHIRT[2]
        return cloth(tunic, 3, "ftunic", folds=(2, 5))(x, y, w, h)
    torso(s, front, cloth(tunic, 3, "ftunicb", folds=(3,)), cloth(tunic, 3, "ftunics"), top=tunic[3])
    for part in ("body",):
        s.band(part, 8, lambda x, y, w, h: rope if (x + y) % 2 else STRAW[3])
    arms(s, cloth(SHIRT, 3, "fsleeve", folds=(1,)), 7, skin, cuff=SHIRT[1])
    if feminine:
        apron_body(s, WHITE, 6, "fapron", bib=False)
        skirt_flaps(s, tunic, "fskirt", hem=tunic[1])
        # weiße Schürze über dem Rock (vorn)
        def ap(x, y, w, h):
            if 1 <= x <= 7:
                return WHITE[3] if y < h - 1 else WHITE[2]
            return tunic[2]
        s.fill("flap_front", "front", ap)
        dress_legs(s, tunic, BROWN, "fdress")
    else:
        legs(s, cloth(BROWN, 3, "ftrousers", folds=(2,), shade_bottom=False))
        # Wadenwickel aus Leinen und einfache Schuhe
        for leg in ("rleg", "lleg"):
            s.rows(leg, 6, 9, lambda x, y, w, h: SHIRT[2] if (x + y) % 2 else SHIRT[1])
            s.rows(leg, 10, 11, lambda x, y, w, h: BROWN[2] if y == 10 else BROWN[1])
            s.fill(leg, "bottom", BROWN[0])
    return s


def lumberjack(look, feminine: bool) -> Skin:
    s, skin, hair = base(look, feminine)
    knit = pal("#3a0c0c", "#5c1414", "#7e1e1a", "#9c2c24", "#b84030")
    cap(s, knit, band=WHITE[3], knit=True)
    shirt = plaid(PLAID_RED, pal("#1a0a0a", "#2e1210"))
    torso(s, shirt, shirt, shirt, top=PLAID_RED[2])
    # Hosenträger (vorn und hinten)
    for f_ in ("front", "back"):
        x0, y0, w, h = gs.face_rect("body", f_)
        for y in range(0, 8):
            s.set(x0 + 1, y0 + y, LEATHER[2])
            s.set(x0 + 6, y0 + y, LEATHER[2])
    belt(s, 8, parts=("body",))
    arms(s, plaid(PLAID_RED, pal("#1a0a0a", "#2e1210")), 8, skin, gloves=LEATHER, glove_from=10, cuff=PLAID_RED[1])
    if feminine:
        skirt_flaps(s, MOSS, "lskirt")
        dress_legs(s, MOSS, BROWN, "ldress")
    else:
        legs(s, cloth(NAVY, 2, "ltrousers", folds=(1,), shade_bottom=False), pal("#160f0a", "#24180f", "#342418", "#463222", "#5a4430"), 6)
    return s


def miner(look, feminine: bool) -> Skin:
    s, skin, hair = base(look, feminine)
    miner_helmet(s)
    # Ruß im Gesicht
    x0, y0, w, h = gs.face_rect("head", "front")
    for (x, y) in ((1, 5), (6, 6), (2, 7)):
        s.set(x0 + x, y0 + y, mix(skin[1], hexc("#202020"), 0.4))
    work = soot(cloth(CHARCOAL, 3, "mshirt", folds=(2, 5)), 0.12, "msoot")
    torso(s, work, work, work, top=CHARCOAL[3])
    # Gurt der Tasche quer über die Brust
    x0, y0, w, h = gs.face_rect("jacket", "front")
    for i in range(8):
        s.set(x0 + i, y0 + 1 + i, LEATHER[2])
    x0, y0, w, h = gs.face_rect("jacket", "back")
    for i in range(8):
        s.set(x0 + 7 - i, y0 + 1 + i, LEATHER[2])
    belt(s, 9, ramp=BROWN, buckle=IRON)
    arms(s, soot(cloth(CHARCOAL, 3, "msleeve"), 0.1, "msoot2"), 12, skin)
    for arm in ("rarm", "larm"):
        s.rows(arm, 9, 11, lambda x, y, w, h: LEATHER[2] if y < 11 else LEATHER[1])
    # Schulterpolster (zweite Schicht)
    for sleeve in ("rsleeve", "lsleeve"):
        s.fill(sleeve, "top", leather(LEATHER, 3, "mpad"))
        s.rows(sleeve, 0, 1, leather(LEATHER, 3, "mpad2"))
    if feminine:
        skirt_flaps(s, BROWN, "mskirt")
        dress_legs(s, BROWN, CHARCOAL, "mdress")
    else:
        legs(s, soot(cloth(BROWN, 2, "mtrousers", folds=(1,), shade_bottom=False), 0.1, "msoot3"),
             pal("#120e0c", "#1e1814", "#2c241e", "#3c3229", "#5a4a3c"), 7)
        for pants in ("rpants", "lpants"):
            s.art(pants, "front", ["....", "....", "....", "....", "bccb", "cddc", "bccb", "....", "....", "....", "....", "...."],
                  {"b": LEATHER[1], "c": LEATHER[2], "d": LEATHER[3]})
    return s


def smith(look, feminine: bool) -> Skin:
    s, skin, hair = base(look, feminine)
    x0, y0, w, h = gs.face_rect("head", "front")
    for (x, y) in ((1, 6), (6, 5)):
        s.set(x0 + x, y0 + y, mix(skin[1], hexc("#202020"), 0.35))
    shirt = cloth(CHARCOAL, 3, "sshirt", folds=(3,))
    torso(s, shirt, shirt, shirt, top=CHARCOAL[3])
    apron = pal("#2a1a0e", "#43301c", "#5e4428", "#7a5a36", "#957048")
    apron_body(s, apron, 2, "sapron", bib=True)
    # kurze Ärmel, nackte Unterarme, dicke Handschuhe
    arms(s, cloth(CHARCOAL, 3, "ssleeve"), 4, skin, gloves=apron, glove_from=8, cuff=CHARCOAL[1])
    for sleeve in ("rsleeve", "lsleeve"):
        s.rows(sleeve, 8, 9, lambda x, y, w, h: apron[3] if y == 8 else apron[2])
    apron_flap(s, apron, "sflap")
    if feminine:
        def back(x, y, w, h):
            return cloth(BURGUNDY, 2, "sskirt", folds=(1, 4, 7))(x, y, w, h)
        s.fill_all("flap_back", back)
        dress_legs(s, BURGUNDY, CHARCOAL, "sdress")
    else:
        legs(s, cloth(CHARCOAL, 2, "strousers", folds=(1,), shade_bottom=False), pal("#120e0c", "#1e1814", "#2c241e", "#3c3229", "#5a4a3c"), 8)
    return s


def merchant(look, feminine: bool) -> Skin:
    s, skin, hair = base(look, feminine)
    cap(s, BURGUNDY, band=BURGUNDY[1], brooch=GOLD)
    feather(s, pal("#3a0c0c", "#6e1a18", "#a02c24", "#c8483a", "#fbf6e6"))
    coat = NAVY if not feminine else BURGUNDY
    def front(x, y, w, h):
        if y == 0 and 2 <= x <= 5:
            return WHITE[4] if x in (3, 4) else WHITE[3]
        if x in (3, 4) and y < 8:
            return GOLD[3] if y % 2 == 1 else coat[3]
        return cloth(coat, 3, "mcoat", folds=(1, 6))(x, y, w, h)
    torso(s, front, cloth(coat, 3, "mcoatb", folds=(2, 5)), cloth(coat, 3, "mcoats"), top=coat[3])
    # Goldborte am Saum und an den Schultern (zweite Schicht)
    s.band("jacket", 0, GOLD[2], ("right", "left"))
    belt(s, 8, ramp=BROWN, buckle=GOLD)
    def sleeve(x, y, w, h):
        if y in (2, 5) and x % 2 == 0:
            return GOLD[3]
        return coat[3] if y < 6 else coat[2]
    arms(s, sleeve, 11, skin, cuff=WHITE[4])
    if feminine:
        skirt_flaps(s, coat, "mskirt", hem=GOLD[2])
        dress_legs(s, coat, CHARCOAL, "mdress")
    else:
        legs(s, cloth(CHARCOAL, 3, "mhose", folds=(2,), shade_bottom=False), pal("#2a1206", "#40200e", "#5a3018", "#7a4422", "#96603a"), 7)
    pouch(s, pal("#3a2a08", "#5a4210", "#8a6a20", "#b48e30", "#d8b450"))
    return s


def innkeeper(look, feminine: bool) -> Skin:
    s, skin, hair = base(look, feminine)
    # rote Wangen
    x0, y0, w, h = gs.face_rect("head", "front")
    s.set(x0 + 1, y0 + 5, mix(skin[2], hexc("#c04040"), 0.35))
    s.set(x0 + 6, y0 + 5, mix(skin[2], hexc("#c04040"), 0.35))
    vest = MOSS if not feminine else ROSE
    shirt = cloth(WHITE, 3, "ishirt", folds=(1,))
    def front(x, y, w, h):
        if x in (0, 1, 6, 7) and y < 9:
            return cloth(vest, 3, "ivest")(x, y, w, h)
        return shirt(x, y, w, h)
    torso(s, front, cloth(vest, 3, "ivestb"), cloth(vest, 3, "ivests"), top=WHITE[3])
    x0, y0, w, h = gs.face_rect("body", "front")
    for y in (2, 4, 6):
        s.set(x0 + 1, y0 + y, BRASS[3])
    apron_body(s, WHITE, 7, "iapron", bib=False)
    apron_flap(s, WHITE, "iflap")
    arms(s, cloth(WHITE, 3, "isleeve", folds=(2,)), 6, skin, cuff=WHITE[2])
    # Geschirrtuch über der Schulter (zweite Schicht, linke Schulter)
    s.fill("lsleeve", "top", lambda x, y, w, h: hexc("#c84a3a") if (x + y) % 2 else WHITE[4])
    s.rows("lsleeve", 0, 1, lambda x, y, w, h: hexc("#c84a3a") if (x + y) % 2 else WHITE[4])
    if feminine:
        def back(x, y, w, h):
            return cloth(vest, 2, "iskirt", folds=(1, 4, 7))(x, y, w, h)
        s.fill_all("flap_back", back)
        dress_legs(s, vest, BROWN, "idress")
    else:
        legs(s, cloth(BROWN, 3, "itrousers", folds=(2,), shade_bottom=False), pal("#1a120c", "#2a1d13", "#3a2a1c", "#4c3826", "#5e4632"), 9)
    return s


def cartographer(look, feminine: bool) -> Skin:
    s, skin, hair = base(look, feminine)
    velvet = pal("#0c1e10", "#14301a", "#1e4426", "#2a5a32", "#3a7242")
    cap(s, velvet, band=GOLD[1])
    spectacles(s)
    coat = TEAL
    def front(x, y, w, h):
        if y == 0:
            return coat[4]
        if x == 3 and y % 3 == 1:
            return BRASS[3]
        if x in (3, 4):
            return coat[2]
        return cloth(coat, 3, "ccoat", folds=(1, 6))(x, y, w, h)
    torso(s, front, cloth(coat, 3, "ccoatb", folds=(3,)), cloth(coat, 3, "ccoats"), top=coat[3])
    # Riemen des Kartenköchers (quer über die Brust)
    x0, y0, w, h = gs.face_rect("jacket", "front")
    for i in range(8):
        s.set(x0 + 7 - i, y0 + i, LEATHER[2])
    belt(s, 8, parts=("body",))
    arms(s, cloth(coat, 3, "csleeve", folds=(1,)), 10, skin, cuff=coat[4])
    # Tintenflecken an den Fingern
    for arm in ("rarm",):
        x0, y0, w, h = gs.face_rect(arm, "front")
        s.set(x0 + 1, y0 + 11, hexc("#20305a"))
    if feminine:
        skirt_flaps(s, coat, "cskirt", hem=coat[4])
        dress_legs(s, coat, BROWN, "cdress")
    else:
        # langer Mantel: die zweite Schicht der Beine trägt den Mantelsaum
        legs(s, cloth(CHARCOAL, 3, "ctrousers", shade_bottom=False), pal("#1a120c", "#2a1d13", "#3a2a1c", "#4c3826", "#5e4632"), 8)
        for pants in ("rpants", "lpants"):
            s.rows(pants, 0, 4, cloth(coat, 3, "ccoatl" + pants, folds=(1,)))
            s.band(pants, 4, coat[1])
    scroll_case(s)
    return s


PROFESSIONS = {
    "builder": (builder, [("light", "brown", "blue", "mustache"), ("tan", "black", "brown", "stubble"), ("light", "blond", "green", "woman")]),
    "farmer": (farmer, [("medium", "brown", "brown", "beard"), ("light", "blond", "blue", "plain"), ("light", "red", "green", "freckles")]),
    "lumberjack": (lumberjack, [("light", "red", "blue", "fullbeard"), ("tan", "brown", "brown", "beard"), ("medium", "chestnut", "hazel", "woman")]),
    "miner": (miner, [("light", "gray", "gray", "fullbeard"), ("dark", "black", "brown", "stubble"), ("tan", "black", "brown", "woman")]),
    "smith": (smith, [("medium", "black", "brown", "fullbeard"), ("light", "brown", "gray", "mustache"), ("light", "red", "green", "freckles")]),
    "merchant": (merchant, [("light", "brown", "green", "mustache"), ("dark", "black", "brown", "plain"), ("light", "blond", "blue", "woman")]),
    "innkeeper": (innkeeper, [("light", "chestnut", "brown", "fullbeard"), ("medium", "gray", "blue", "mustache"), ("tan", "brown", "hazel", "woman")]),
    "cartographer": (cartographer, [("light", "gray", "blue", "beard"), ("medium", "brown", "green", "plain"), ("dark", "black", "brown", "woman")]),
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
    print(f"{len(skins)} Bürger-Texturen geschrieben")
    if "--preview" in sys.argv:
        scale = 6
        keys = list(skins)
        cols = 6
        rows = (len(keys) + cols - 1) // cols
        sheet = Image.new("RGBA", (cols * 36 * scale, rows * 34 * scale), (90, 110, 130, 255))
        for i, key in enumerate(keys):
            for j, back in enumerate((False, True)):
                v = gs.front_view(skins[key], back).resize((16 * scale, 32 * scale), Image.NEAREST)
                sheet.alpha_composite(v, ((i % cols) * 36 * scale + j * 17 * scale, (i // cols) * 34 * scale + 4))
        sheet.save(SCRATCH / "citizen_preview.png")
        print("Vorschau:", SCRATCH / "citizen_preview.png")


if __name__ == "__main__":
    main()
