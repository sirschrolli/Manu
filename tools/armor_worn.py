#!/usr/bin/env python3
"""
Getragene Rüstungen im Vanilla-Stil: Stahl, Nachtstahl, Abyssit, Konterit und die sechs Drachenschuppen-Rüstungen.

Jede Rüstung besteht aus drei Texturen (Schema der Vanilla-Modelle, siehe HumanoidModel):
  * humanoid/<name>.png           (64x32) Helm (Kopf), Brustpanzer (Rumpf + Arme), Stiefel (untere Beine)
  * humanoid_leggings/<name>.png  (64x32) Beinschutz (Gürtel am Rumpf + obere Beine)
  * humanoid_baby/<name>.png      (64x64) alles zusammen für Baby-Zombies & Co. (eigenes, kleineres Modell)

Gemalt wird Fläche für Fläche (jeder Quader hat oben/unten/rechts/vorn/links/hinten): Platten mit leichter Fase
(oben/links heller, unten/rechts dunkler – das Spiel schattiert die Seiten zusätzlich selbst), Säume, Nieten,
Nähte und Muster je Satz. Die Details folgen den Symbolen aus tools/items_armor.py: Visierhelm mit Messingkamm,
Mondsichel-Helm, leuchtende Augenschlitze, Kristallplatten, Drachenschuppen mit Bauchplatten.

Wird von tools/generate_textures.py aufgerufen: build(save).
"""
from __future__ import annotations

from dataclasses import dataclass
from typing import Callable

from PIL import Image

import item_style as st
import items_armor as icons
import items_gear as gear

Rect = tuple[int, int, int, int]  # x, y, Breite, Höhe


# =============================================================================
# Quader und Flächen
# =============================================================================

@dataclass(frozen=True)
class Box:
    u: int
    v: int
    w: int
    h: int
    d: int

    def faces(self) -> dict[str, Rect]:
        u, v, w, h, d = self.u, self.v, self.w, self.h, self.d
        return {
            "top": (u + d, v, w, d),
            "bottom": (u + d + w, v, w, d),
            "right": (u, v + d, d, h),
            "front": (u + d, v + d, w, h),
            "left": (u + d + w, v + d, d, h),
            "back": (u + 2 * d + w, v + d, w, h),
        }


SIDES = ("right", "front", "left", "back")

# Erwachsene (64x32): Kopf, Rumpf, Arm, Bein
ADULT = {
    "head": Box(0, 0, 8, 8, 8),
    "body": Box(16, 16, 8, 12, 4),
    "arm": Box(40, 16, 4, 12, 4),
    "leg": Box(0, 16, 4, 12, 4),
}

# Baby (64x64): siehe HumanoidModel.createBabyArmorMesh (Größen gerundet)
BABY = {
    "head": Box(0, 0, 9, 8, 8),
    "body": Box(0, 17, 6, 5, 3),
    "waist": Box(0, 36, 6, 2, 3),
    "left_arm": Box(30, 17, 2, 5, 3),
    "right_arm": Box(30, 25, 2, 5, 3),
    "right_leg": Box(18, 17, 3, 4, 3),
    "left_leg": Box(18, 24, 3, 4, 3),
    "right_foot": Box(0, 25, 3, 1, 3),
    "left_foot": Box(0, 29, 3, 1, 3),
}


class Canvas:
    """Raster aus (Slot, Stufe) in Texturgröße."""

    def __init__(self, w: int, h: int):
        self.w, self.h = w, h
        self.g: list[list[st.Px | None]] = [[None] * w for _ in range(h)]

    def set(self, x: int, y: int, px: st.Px | None) -> None:
        if 0 <= x < self.w and 0 <= y < self.h:
            self.g[y][x] = px

    def get(self, x: int, y: int) -> st.Px | None:
        return self.g[y][x] if 0 <= x < self.w and 0 <= y < self.h else None

    def shift(self, x: int, y: int, delta: int, lo: int = 1, hi: int = 6) -> None:
        p = self.get(x, y)
        if p is not None:
            self.set(x, y, (p[0], st.clamp(p[1] + delta, lo, hi)))

    def image(self, ramps: dict[str, st.Ramp]) -> Image.Image:
        img = Image.new("RGBA", (self.w, self.h), (0, 0, 0, 0))
        px = img.load()
        for y in range(self.h):
            for x in range(self.w):
                p = self.g[y][x]
                if p is not None:
                    px[x, y] = ramps[p[0]][st.clamp(p[1], 0, len(ramps[p[0]]) - 1)]
        return img


class Face:
    """Eine Quaderfläche: Koordinaten relativ zur Fläche (0,0 = oben links)."""

    def __init__(self, cv: Canvas, rect: Rect, name: str):
        self.cv = cv
        self.x0, self.y0, self.w, self.h = rect
        self.name = name

    def set(self, x: int, y: int, px: st.Px | None) -> None:
        if 0 <= x < self.w and 0 <= y < self.h:
            self.cv.set(self.x0 + x, self.y0 + y, px)

    def get(self, x: int, y: int) -> st.Px | None:
        if 0 <= x < self.w and 0 <= y < self.h:
            return self.cv.get(self.x0 + x, self.y0 + y)
        return None

    def shift(self, x: int, y: int, delta: int) -> None:
        if 0 <= x < self.w and 0 <= y < self.h:
            self.cv.shift(self.x0 + x, self.y0 + y, delta)

    def clear(self, rows: range | None = None, cols: range | None = None) -> None:
        for y in rows if rows is not None else range(self.h):
            for x in cols if cols is not None else range(self.w):
                self.set(x, y, None)

    def plate(self, slot: str, level: int = 4, rows: range | None = None, bevel: bool = True) -> None:
        """Platte mit Fase: obere Reihe +1, linke Spalte +1, untere Reihe -1, rechte Spalte -1."""
        rows = rows if rows is not None else range(self.h)
        top, bottom = rows.start, rows.stop - 1
        for y in rows:
            for x in range(self.w):
                lv = level
                if bevel:
                    if y == top or x == 0:
                        lv += 1
                    if y == bottom or x == self.w - 1:
                        lv -= 1
                self.set(x, y, (slot, lv))

    def hline(self, y: int, px: st.Px, cols: range | None = None) -> None:
        for x in cols if cols is not None else range(self.w):
            self.set(x, y, px)

    def vline(self, x: int, px: st.Px, rows: range | None = None) -> None:
        for y in rows if rows is not None else range(self.h):
            self.set(x, y, px)

    def center_cols(self, width: int = 2) -> range:
        start = (self.w - width) // 2
        return range(start, start + width)


def faces(cv: Canvas, box: Box, which: tuple[str, ...] = SIDES + ("top",)) -> dict[str, Face]:
    all_faces = box.faces()
    return {name: Face(cv, all_faces[name], name) for name in which}


# =============================================================================
# Muster
# =============================================================================

def scales(f: Face, slot: str = "A", rows: range | None = None) -> None:
    """Schuppen: 3x2-Zellen, oben Mitte heller, unten links/rechts dunkler, jede zweite Reihe versetzt."""
    pattern = ((0, 1, 0), (-1, 0, -1))
    for y in rows if rows is not None else range(f.h):
        for x in range(f.w):
            p = f.get(x, y)
            if p is None or p[0] != slot:
                continue
            gy = f.y0 + y
            gx = f.x0 + x
            u = (gx + (gy // 2) % 2) % 3
            f.shift(x, y, pattern[gy % 2][u])


def facets(f: Face, slot: str = "A", seed: int = 0) -> None:
    """Kristallplatten: eine helle Schliffkante quer über die Fläche, dahinter eine etwas dunklere Facette."""
    edge = (f.w + f.h) // 2 + (seed % 3) - 1
    for y in range(f.h):
        for x in range(f.w):
            p = f.get(x, y)
            if p is None or p[0] != slot:
                continue
            k = x + y
            if k == edge:
                f.shift(x, y, 1)
            elif k > edge:
                f.shift(x, y, -1)


# =============================================================================
# Satz-Beschreibung
# =============================================================================

@dataclass
class Style:
    name: str
    ramps: dict[str, st.Ramp]
    helmet: str            # "visor" (Stahl), "crescent" (Nachtstahl), "glow" (Abyssit), "crystal" (Konterit), "dragon"
    trim: str = "B"        # Slot der Säume
    texture: str = ""      # "", "scales", "facets"
    seam: str | None = None  # Slot glühender Nähte (Abyssit) bzw. Akzente


def surface(f: Face, s: Style, level: int = 4, rows: range | None = None) -> None:
    f.plate("A", level, rows)
    if s.texture == "scales":
        scales(f, rows=rows)
    elif s.texture == "facets":
        facets(f, seed=len(f.name))


# -----------------------------------------------------------------------------
# Helm
# -----------------------------------------------------------------------------

def helmet(fs: dict[str, Face], s: Style) -> None:
    for name in ("top",) + SIDES:
        surface(fs[name], s)
    front, back, top = fs["front"], fs["back"], fs["top"]
    right, left = fs["right"], fs["left"]
    T = s.trim
    cc = front.center_cols(2)
    if s.helmet == "visor":
        # Messingkamm von vorn über den Scheitel nach hinten, T-Visier, Nieten
        for x in top.center_cols(2):
            top.vline(x, (T, 5 if x == cc.start else 4))
        for x in cc:
            front.vline(x, (T, 5 if x == cc.start else 4), range(0, 2))
            back.vline(x, (T, 4 if x == cc.start else 3), range(0, 3))
        front.hline(3, ("A", 0), range(1, front.w - 1))
        for x in cc:
            front.vline(x, ("A", 0), range(4, 7))
        front.hline(front.h - 1, ("A", 2))
        for side in (right, left):
            side.set(side.w // 2, 3, (T, 5))
            side.hline(side.h - 1, ("A", 2))
        back.hline(5, ("A", 2))
        back.hline(back.h - 1, (T, 3))
    elif s.helmet == "crescent":
        # offenes Gesicht mit Mondsilber-Stirnband und Mondsichel auf der Stirn
        front.clear(range(3, front.h), range(2, front.w - 2))
        front.hline(2, (T, 4))
        front.set(cc.start - 1, 0, (T, 5))
        front.set(cc.stop, 0, (T, 4))
        for x in cc:
            front.set(x, 1, (T, 5))
        for side in (right, left):
            side.hline(2, (T, 4))
            side.hline(side.h - 1, ("A", 2))
        back.hline(2, (T, 3))
        for x in top.center_cols(2):
            top.vline(x, ("A", 5))
    elif s.helmet == "glow":
        # geschlossener Helm, leuchtende schräge Augenschlitze, Naht über dem Scheitel
        G = s.seam
        front.hline(0, ("A", 5))
        for i, (x, y) in enumerate(((1, 3), (2, 3), (2, 4), (3, 4))):
            front.set(x, y, (G, 5 if i % 2 else 4))
            front.set(front.w - 1 - x, y, (G, 5 if i % 2 else 4))
        for x in cc:
            front.vline(x, ("A", 2), range(5, front.h))
        for x in top.center_cols(2):
            top.vline(x, (G, 3))
        for x in cc:
            back.vline(x, (G, 3), range(0, 4))
        for side in (right, left):
            side.vline(side.w // 2, ("A", 2), range(2, side.h))
    elif s.helmet == "crystal":
        # Kristallkamm auf dem Scheitel, Sehschlitz, Stahlsäume
        G = s.seam
        for x in range(top.w):
            for y in range(top.h):
                if (x + y) % 3 == 0:
                    top.set(x, y, (G, 5 if (x + y) % 2 else 4))
        front.hline(3, ("A", 0), range(1, front.w - 1))
        front.hline(front.h - 1, (T, 3))
        for side in (right, left):
            side.hline(side.h - 1, (T, 3))
        back.hline(back.h - 1, (T, 3))
        for x in cc:
            front.set(x, 0, (G, 5))
            front.set(x, 1, (G, 4))
    elif s.helmet == "dragon":
        # Schuppenhelm mit offenem Gesicht: Stirnplatte mit Akzentstein, Hornansätze an den vorderen Ecken,
        # Stachelkamm über Scheitel und Nacken
        front.clear(range(4, front.h), range(2, front.w - 2))
        front.hline(3, ("B", 4), range(1, front.w - 1))
        for x in cc:
            front.set(x, 2, (s.seam, 5 if x == cc.start else 4))
        for x in top.center_cols(2):
            for y in range(0, top.h, 2):
                top.set(x, y, ("C", 5 if x == top.center_cols(2).start else 4))
        for x in cc:
            back.vline(x, ("C", 3), range(0, 5, 2))
        # Hornansätze: obere vordere Ecken (Vorderseite, Seiten, Oberseite)
        for x, lv in ((0, 5), (1, 4)):
            front.set(x, 0, ("C", lv))
            front.set(front.w - 1 - x, 0, ("C", lv - 1))
        right.set(right.w - 1, 0, ("C", 4))
        right.set(right.w - 2, 0, ("C", 3))
        right.set(right.w - 1, 1, ("C", 3))
        left.set(0, 0, ("C", 4))
        left.set(1, 0, ("C", 3))
        left.set(0, 1, ("C", 3))
        for x in (0, 1, top.w - 2, top.w - 1):
            top.set(x, top.h - 1, ("C", 5 if x < 2 else 4))


# -----------------------------------------------------------------------------
# Brustpanzer: Rumpf und Arme
# -----------------------------------------------------------------------------

def chestplate(body: dict[str, Face], arm: dict[str, Face], s: Style, baby: bool = False) -> None:
    for name in ("top",) + SIDES:
        surface(body[name], s)
    front, back = body["front"], body["back"]
    T = s.trim
    h = front.h
    cc = front.center_cols(2)
    # Halsausschnitt (V) mit Saum
    if not baby:
        front.clear(range(0, 1), range(2, front.w - 2))
        front.clear(range(1, 2), range(3, front.w - 3))
        front.set(2, 1, (T, 5))
        front.set(front.w - 3, 1, (T, 4))
        front.set(3, 2, (T, 5))
        front.set(front.w - 4, 2, (T, 4))
    if s.texture == "scales" and not baby:
        # helle Bauchplatten in der Mitte
        for y in range(3, h - 1):
            for x in range(2, front.w - 2):
                front.set(x, y, ("B", (5 if y % 2 else 4) - (1 if x >= front.w // 2 else 0)))
    elif s.helmet == "visor":
        for y in range(3 if not baby else 0, h - 3):
            front.set(cc.start, y, ("A", 5))
            front.set(cc.stop - 1, y, ("A", 3))
        if not baby:
            front.hline(h - 3, ("A", 2))
            front.hline(h - 2, ("A", 4))
            front.hline(h - 1, (T, 3))
    elif s.helmet == "crescent":
        # Mondsichel mit Nachtkristall
        y0 = 4 if not baby else 1
        for x, y, lv in ((3, 0, 5), (4, 0, 5), (2, 1, 5), (2, 2, 4), (3, 3, 4), (4, 3, 3)):
            front.set(x - (0 if not baby else 1), y0 + y, (T, lv))
        front.set(4 - (0 if not baby else 1), y0 + 1, ("E", 5))
        if not baby:
            front.hline(h - 1, (T, 3))
    elif s.helmet == "glow":
        # glühende Y-Naht
        G = s.seam
        if not baby:
            for i in range(3):
                front.set(1 + i, 2 + i, (G, 4))
                front.set(front.w - 2 - i, 2 + i, (G, 3))
            for x in cc:
                front.vline(x, (G, 4 if x == cc.start else 3), range(5, h - 1))
        else:
            for x in cc:
                front.vline(x, (G, 4), range(1, front.h))
    elif s.helmet == "crystal":
        G = s.seam
        for x, y, lv in ((3, 4, 5), (4, 4, 4), (2, 5, 5), (3, 5, 4), (4, 5, 4), (5, 5, 3), (3, 6, 4), (4, 6, 3)):
            if baby:
                x, y = x - 1, y - 4
            front.set(x, y, (G, lv))
        if not baby:
            front.hline(h - 1, (T, 3))
    back.hline(back.h // 2, ("A", 3))
    # Arme: Schulterstück oben, Armschiene unten, dazwischen frei
    pad = range(0, 5) if not baby else range(0, 3)
    guard = range(8, 12) if not baby else range(4, 5)
    for name in SIDES:
        f = arm[name]
        f.clear()
        surface(f, s, rows=pad)
        f.hline(pad.stop - 1, (T, 4 if name in ("front", "right") else 3))
        if not baby:
            surface(f, s, rows=guard)
            f.hline(guard.start, (T, 4 if name in ("front", "right") else 3))
            if s.seam and s.helmet == "glow":
                f.set(f.w // 2, 2, (s.seam, 4))
    surface(arm["top"], s)


# -----------------------------------------------------------------------------
# Beinschutz (innere Ebene): Gürtel am Rumpf, Beinschienen
# -----------------------------------------------------------------------------

def leggings(body: dict[str, Face], leg: dict[str, Face], s: Style, belt_rows: range, leg_rows: range,
             baby: bool = False) -> None:
    T = s.trim
    for name in SIDES:
        f = body[name]
        f.clear()
        surface(f, s, rows=belt_rows)
        f.hline(belt_rows.start, (T, 5 if name in ("front", "right") else 4))
        if name == "front" and not baby:
            for x in f.center_cols(2):
                f.set(x, belt_rows.start, ("E" if s.helmet == "crescent" else T, 6 if s.helmet == "crescent" else 5))
    for name in SIDES:
        f = leg[name]
        f.clear()
        surface(f, s, rows=leg_rows)
        # Kniekachel
        knee = leg_rows.start + (leg_rows.stop - leg_rows.start) // 2
        if name == "front" and not baby:
            knee_px = (T, 4) if s.helmet == "visor" else ("A", 5)
            for x in range(f.w):
                f.set(x, knee, knee_px if x < f.w - 1 else (knee_px[0], knee_px[1] - 1))
            if s.seam and s.helmet == "glow":
                f.set(1, knee + 2, (s.seam, 4))
    surface(leg["top"], s)


# -----------------------------------------------------------------------------
# Stiefel (äußere Ebene, untere Beine)
# -----------------------------------------------------------------------------

def boots(leg: dict[str, Face], s: Style, rows: range, baby: bool = False) -> None:
    T = s.trim
    for name in SIDES:
        f = leg[name]
        f.clear()
        surface(f, s, rows=rows)
        f.hline(rows.start, (T, 5 if name in ("front", "right") else 4))
        if name == "front" and s.texture == "scales" and not baby:
            f.set(0, rows.stop - 1, ("C", 5))
            f.set(f.w - 1, rows.stop - 1, ("C", 4))
    bottom = leg.get("bottom")
    if bottom is not None:
        surface(bottom, s, level=2)


# =============================================================================
# Sätze
# =============================================================================

def styles() -> list[Style]:
    out = [
        Style("steel", {"A": gear.STEEL, "B": gear.BRASS}, "visor"),
        Style("nightsteel", {"A": gear.NIGHT, "B": gear.MOONSILVER, "E": gear.NIGHT_GEM}, "crescent"),
        Style("abyssite", {"A": gear.ABYSS, "B": gear.ABYSS, "D": gear.ABYSS_GLOW}, "glow", trim="A", seam="D"),
        Style("konterit", {"A": icons.KONTERIT, "B": gear.STEEL_DARK, "D": icons.KONTERIT_GLOW}, "crystal",
              texture="facets", seam="D"),
    ]
    for kind in icons.DRAGONS:
        ramps = icons.dragon_ramps(kind)
        out.append(Style(f"{kind}_dragon_scale", ramps, "dragon", texture="scales", seam="D"))
    return out


def adult_layers(s: Style) -> tuple[Image.Image, Image.Image]:
    outer = Canvas(64, 32)
    helmet(faces(outer, ADULT["head"]), s)
    chestplate(faces(outer, ADULT["body"]), faces(outer, ADULT["arm"]), s)
    boots(faces(outer, ADULT["leg"], SIDES + ("bottom",)), s, range(6, 12))
    inner = Canvas(64, 32)
    leggings(faces(inner, ADULT["body"]), faces(inner, ADULT["leg"]), s, range(7, 12), range(0, 9))
    return outer.image(s.ramps), inner.image(s.ramps)


def baby_layer(s: Style) -> Image.Image:
    cv = Canvas(64, 64)
    helmet(faces(cv, BABY["head"]), s)
    body = faces(cv, BABY["body"])
    for side in ("left_arm", "right_arm"):
        chestplate(body, faces(cv, BABY[side]), s, baby=True)
    waist = faces(cv, BABY["waist"])
    for side in ("left_leg", "right_leg"):
        leggings(waist, faces(cv, BABY[side]), s, range(0, 2), range(0, 3), baby=True)
    for side in ("left_foot", "right_foot"):
        boots(faces(cv, BABY[side], SIDES + ("bottom",)), s, range(0, 1), baby=True)
    return cv.image(s.ramps)


def build(save: Callable[[str, Image.Image], None]) -> None:
    for s in styles():
        outer, inner = adult_layers(s)
        save(f"entity/equipment/humanoid/{s.name}", outer)
        save(f"entity/equipment/humanoid_leggings/{s.name}", inner)
        save(f"entity/equipment/humanoid_baby/{s.name}", baby_layer(s))


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda k, v: out.__setitem__(k, v))
    zoom = 6
    sheet = Image.new("RGBA", (3 * 64 * zoom + 40, len(out) // 3 * (64 * zoom + 10)), (139, 139, 139, 255))
    for i, (k, img) in enumerate(out.items()):
        big = img.resize((img.width * zoom, img.height * zoom), Image.NEAREST)
        sheet.alpha_composite(big, ((i % 3) * (64 * zoom + 20), (i // 3) * (64 * zoom + 10)))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "armor_worn.png")
