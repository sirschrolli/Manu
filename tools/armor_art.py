#!/usr/bin/env python3
"""
Getragene Rüstungen, von Hand gepixelt (Fläche für Fläche als Zeichnung), in der Formensprache der Vanilla-Rüstungen:

  * wenig Kontrast: Ränder nur eine Stufe dunkler als die Plattenmitte (wie bei Eisen/Diamant), dadurch wirken die
    Platten glatt und weich statt gekachelt,
  * innen ein sanfter Verlauf von oben links (hell) nach unten rechts mit kleinem Glanzfleck,
  * wenige, bewusst gesetzte Details je Satz (Kamm, Visier, Mondsichel, glühende Nähte, Kristallkanten, Schuppen,
    Bauchplatten, Hörner).

Alle Sätze teilen sich dieselben Grundzeichnungen (BASE); darüber liegt je Satz eine Zeichnung mit seinen Details
('.' = Grundzeichnung behalten, ',' = durchsichtig machen). Zeichen: Ziffern 0-6 = Hauptmaterial (A), a-g = Saum (B),
h-n = Horn (C), o-u = Leuchten/Akzent (D), A-G = Edelstein (E).

Ausgabe je Satz: humanoid/<name> (64x32), humanoid_leggings/<name> (64x32), humanoid_baby/<name> (64x64).
Wird von tools/generate_textures.py aufgerufen: build(save).
"""
from __future__ import annotations

from dataclasses import dataclass, field
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


Art = list[str]
Part = dict[str, Art]


def mirror(art: Art) -> Art:
    return [row[::-1] for row in art]


def over(base: Art, top: Art | None) -> Art:
    """Legt eine Detailzeichnung über die Grundzeichnung ('.' behalten, ',' durchsichtig)."""
    if top is None:
        return list(base)
    out = []
    for y, row in enumerate(base):
        t = top[y] if y < len(top) else ""
        out.append("".join(
            ("." if (x < len(t) and t[x] == ",") else (t[x] if x < len(t) and t[x] != "." else ch))
            for x, ch in enumerate(row)))
    return out


def fit(art: Art, w: int, h: int) -> Art:
    """Passt eine Zeichnung auf w x h an; erste und letzte Zeile/Spalte bleiben erhalten (Ränder)."""
    sh, sw = len(art), len(art[0])

    def pick(n_src: int, n_dst: int) -> list[int]:
        if n_dst == 1:
            return [n_src // 2]
        return [min(n_src - 1, round(i * (n_src - 1) / (n_dst - 1))) for i in range(n_dst)]

    return ["".join(art[r][c] for c in pick(sw, w)) for r in pick(sh, h)]


def paint(cv: Canvas, box: Box, art: Part) -> None:
    faces = box.faces()
    for name, rows in art.items():
        x0, y0, w, h = faces[name]
        if len(rows) != h or any(len(r) != w for r in rows):
            raise ValueError(f"{name}: erwartet {w}x{h}, bekommen {len(rows[0])}x{len(rows)}")
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if ch not in ".,":
                    cv.set(x0 + x, y0 + y, st.CHAR_TO_PX[ch])


# =============================================================================
# Grundzeichnungen (glatte Platten)
# =============================================================================

BASE: dict[str, Art] = {
    "head_top": [
        "33233233",
        "35554443",
        "35544443",
        "35444443",
        "34444433",
        "34444433",
        "34443333",
        "33233233",
    ],
    "head_front_closed": [
        "33233233",
        "35554443",
        "35544443",
        "30000003",
        "35444443",
        "34444433",
        "34443333",
        "33333333",
    ],
    "head_front_open": [
        "33233233",
        "35554443",
        "35544443",
        "354,,443",
        "34,,,,33",
        "34,,,,33",
        "34,,,,33",
        "33,,,,33",
    ],
    "head_side": [
        "33233233",
        "35555443",
        "35554443",
        "35444443",
        "34444433",
        "34444433",
        "34443333",
        "33233233",
    ],
    "head_back": [
        "33233233",
        "35554443",
        "35544443",
        "34444433",
        "32233223",
        "34444433",
        "34443333",
        "33233233",
    ],
    "body_front": [
        "33,,,,33",
        "35,,,,43",
        "354,,443",
        "35544443",
        "35544443",
        "35444443",
        "34444433",
        "34444433",
        "34444333",
        ",333333,",
        ",,3333,,",
        ",,,,,,,,",
    ],
    "body_back": [
        "33,,,,33",
        "35,,,,43",
        "355,,443",
        "35544443",
        "35444433",
        "34444433",
        "33233233",
        "34444433",
        "34444333",
        ",333333,",
        ",,,,,,,,",
        ",,,,,,,,",
    ],
    "body_side": [
        "3333",
        "3553",
        "3543",
        "3443",
        "3443",
        "3443",
        "3223",
        "3443",
        "3333",
        ",,,,",
        ",,,,",
        ",,,,",
    ],
    "body_top": [
        "33333333",
        "35544443",
        "34444433",
        "33333333",
    ],
    "arm_side": [
        "3333",
        "3563",
        "3553",
        "3443",
        "3333",
        ",,,,",
        ",,,,",
        ",,,,",
        "3223",
        "3553",
        "3443",
        "3333",
    ],
    "arm_top": [
        "3333",
        "3563",
        "3443",
        "3333",
    ],
    "boot_side": [
        ",,,,",
        ",,,,",
        ",,,,",
        ",,,,",
        ",,,,",
        ",,,,",
        "3333",
        "3553",
        "3443",
        "3223",
        "3553",
        "3333",
    ],
    "boot_bottom": [
        "2222",
        "2332",
        "2332",
        "2222",
    ],
    "belt_front": [",,,,,,,,"] * 7 + [
        "33333333",
        "35544433",
        "35444433",
        "34444333",
        "33333333",
    ],
    "belt_side": [",,,,"] * 7 + ["3333", "3553", "3443", "3443", "3333"],
    "thigh_side": [
        "3553",
        "3543",
        "3443",
        "3443",
        "3223",
        "3553",
        "3553",
        "3443",
        "3333",
        ",,,,",
        ",,,,",
        ",,,,",
    ],
    "thigh_top": [
        "3333",
        "3553",
        "3443",
        "3333",
    ],
}


# =============================================================================
# Satz-Beschreibung
# =============================================================================

@dataclass
class Design:
    """Detailzeichnungen eines Satzes; fehlende Einträge = glatte Grundplatte."""
    name: str
    ramps: dict[str, st.Ramp]
    open_face: bool = False
    texture: str = ""                       # "" | "scales" | "facets"
    d: dict[str, Art] = field(default_factory=dict)


def _art(design: Design, key: str, base_key: str | None = None) -> Art:
    base = BASE[base_key or key]
    if design.texture == "scales":
        base = scale(base, key)
    elif design.texture == "facets":
        base = facet(base, key)
    return over(base, design.d.get(key))


def scale(art: Art, key: str) -> Art:
    """
    Schuppen von Hand angelegt: Reihen aus 4 Pixel breiten, 3 Pixel hohen Schuppen – oben hell, unten mit dunklem
    Bogen –, jede Reihe um 2 Pixel versetzt, sodass die Schuppen übereinanderliegen.
    """
    tile = ["4554", "3443", "2332", "5445", "4334", "3223"]
    out = []
    for y, row in enumerate(art):
        line = []
        for x, ch in enumerate(row):
            if ch in "45" and 0 < x < len(row) - 1 and 0 < y < len(art) - 1:
                line.append(tile[(y - 1) % 6][x % 4])
            else:
                line.append(ch)
        out.append("".join(line))
    return out


def facet(art: Art, key: str) -> Art:
    """Kristallplatten: eine helle Schliffkante quer über die Platte, dahinter eine Stufe dunkler."""
    out = []
    h = len(art)
    w = len(art[0])
    edge = (w + h) // 2
    for y, row in enumerate(art):
        line = []
        for x, ch in enumerate(row):
            if ch in "45" and 0 < x < w - 1 and 0 < y < h - 1:
                k = x + y
                if k == edge:
                    line.append("6" if ch == "5" else "5")
                elif k > edge:
                    line.append(str(int(ch) - 1))
                else:
                    line.append(ch)
            else:
                line.append(ch)
        out.append("".join(line))
    return out


def parts(design: Design) -> tuple[Part, Part, Part, Part, Part, Part]:
    head_front = "head_front_open" if design.open_face else "head_front_closed"
    side = _art(design, "head_side")
    head = {
        "top": _art(design, "head_top"),
        "front": _art(design, "head_front", head_front),
        "right": side,
        "left": mirror(_art(design, "head_side_left", "head_side")) if "head_side_left" in design.d else mirror(side),
        "back": _art(design, "head_back"),
    }
    body_side = _art(design, "body_side")
    body = {
        "front": _art(design, "body_front"),
        "back": _art(design, "body_back"),
        "right": body_side,
        "left": mirror(body_side),
        "top": _art(design, "body_top"),
    }
    arm_side = _art(design, "arm_side")
    arm = {
        "front": arm_side,
        "right": arm_side,
        "left": mirror(arm_side),
        "back": [r.replace("6", "5") for r in arm_side],
        "top": _art(design, "arm_top"),
    }
    boot_side = _art(design, "boot_side")
    boots = {
        "front": _art(design, "boot_front", "boot_side"),
        "right": boot_side,
        "left": mirror(boot_side),
        "back": boot_side,
        "bottom": BASE["boot_bottom"],
    }
    belt_side = _art(design, "belt_side")
    belt = {
        "front": _art(design, "belt_front"),
        "back": _art(design, "belt_back", "belt_front"),
        "right": belt_side,
        "left": mirror(belt_side),
    }
    thigh_side = _art(design, "thigh_side")
    thighs = {
        "front": _art(design, "thigh_front", "thigh_side"),
        "right": thigh_side,
        "left": mirror(thigh_side),
        "back": thigh_side,
        "top": _art(design, "thigh_top"),
    }
    return head, body, arm, boots, belt, thighs


# =============================================================================
# Die Sätze
# =============================================================================

# Gemeinsame Saum-Zeichnungen
TRIM_ARM = [".", ".", ".", ".", "edcb", ".", ".", ".", "dccb"]
TRIM_BOOT = ["."] * 6 + ["edcb"]
TRIM_BELT = ["."] * 7 + ["fedddccb"]
TRIM_BELT_SIDE = ["."] * 7 + ["edcb"]


def steel() -> Design:
    """Stahl: Ritterhelm mit T-Visier und Messingkamm, Brustgrat, Messingsäume."""
    crest = ["...ed...", "...fd...", "...fd...", "...fd...", "...fd...", "...fd...", "...fd...", "...ed..."]
    return Design("steel", {"A": st.ramp("#9ca6b2", dark=0.3, light=0.97, chroma_dark=1.5, hue_dark=10),
                            "B": gear.BRASS}, d={
        "head_top": crest,
        "head_front": ["...ec...", "...fd...", ".", ".", "...00...", "...00...", "...00..."],
        "head_side": [".", ".", ".", ".....g..", "......1.", ".", "......1."],
        "head_back": ["...ed...", "...fd...", "...ec..."],
        "body_front": [".", ".f....d.", "..f..d..", "...fd...", "...64...", "...64...", "...54...", "...54...",
                       "...54...", ".", "..dccb.."],
        "arm_side": TRIM_ARM,
        "boot_side": TRIM_BOOT,
        "boot_front": TRIM_BOOT + [".", ".", ".", "3563"],
        "belt_front": ["."] * 7 + ["fedgedcb"],
        "belt_back": TRIM_BELT,
        "belt_side": TRIM_BELT_SIDE,
        "thigh_front": [".", ".", ".", ".", ".", "3663"],
    })


def nightsteel() -> Design:
    """Nachtstahl: offener Helm mit Mondsichel-Kamm und Silberstirnband, Mondsichel mit Nachtkristall auf der Brust."""
    return Design("nightsteel", {"A": st.ramp("#4a4d8c", dark=0.16, light=0.82, chroma_light=0.55, hue_light=4, cool=285),
                                 "B": gear.MOONSILVER, "E": gear.NIGHT_GEM}, open_face=True, d={
        "head_top": [".", ".", ".", "...ff...", "...fe...", ".", ".", "..e..d.."],
        "head_front": ["..e..d..", "...fe...", "fffeeddc"],
        "head_side": [".", ".", "feeeddcc", ".", ".", ".....f..", ".", "."],
        "head_back": [".", ".", "eeddddcc"],
        "body_front": [".", ".f....d.", "..f..d..", "...ed...", ".", "..gf....", ".f......", ".f..F...",
                       "..ed....", ".", "..dccb.."],
        "arm_side": TRIM_ARM,
        "boot_side": TRIM_BOOT,
        "boot_front": TRIM_BOOT,
        "belt_front": ["."] * 7 + ["feeFEdcb"],
        "belt_back": TRIM_BELT,
        "belt_side": TRIM_BELT_SIDE,
        "thigh_front": [".", ".", ".", ".", "eddc"],
    })


def abyssite() -> Design:
    """Abyssit: geschlossener Helm mit leuchtenden, schrägen Augenschlitzen und glühenden Nähten."""
    return Design("abyssite", {"A": st.ramp("#3a2a48", dark=0.12, light=0.62, chroma_dark=1.0, cool=300, hue_light=0),
                               "D": gear.ABYSS_GLOW}, d={
        "head_top": ["...rq...", "...rq...", "...rq...", "...rq...", "...rq...", "...rq...", "...rq...", "...rq..."],
        "head_front": ["...rq...", "...rq...", ".", "3ts33st3", "33tsst33", "34400433", "34400433"],
        "head_side": [".", ".", ".", ".", ".", ".", "."],
        "head_back": ["...rq...", "...rq...", "...rq...", "...qp..."],
        "body_front": [".", ".", ".", ".r....q.", "..r..q..", "...sr...", "...rq...", "...rq...", "...qp..."],
        "body_back": [".", ".", ".", "...rq...", "...rq...", "...qp..."],
        "arm_side": [".", ".", ".", ".", "qrrq", ".", ".", ".", "qrrq"],
        "boot_side": TRIM_BOOT[:6] + ["qrrq"],
        "boot_front": TRIM_BOOT[:6] + ["qrrq", ".", ".", ".", ".r.."],
        "belt_front": ["."] * 7 + ["qrrssrrq"],
        "belt_back": ["."] * 7 + ["qrrrrrrq"],
        "belt_side": ["."] * 7 + ["qrrq"],
        "thigh_front": [".", ".", ".", ".", "qrrq"],
    })


def konterit() -> Design:
    """Konterit: Kristallplatten mit Schliffkante, Kristallkamm auf dem Helm, dunkle Stahlsäume."""
    return Design("konterit", {"A": icons.KONTERIT, "B": gear.STEEL_DARK, "D": icons.KONTERIT_GLOW},
                  texture="facets", d={
        "head_top": [".", "..t..s..", ".tts.sr.", "..s..r..", ".", "...ts...", "..tssr..", "...sr..."],
        "head_front": ["...ts...", "...sr...", ".", ".", ".", ".", ".", "eeddddcc"],
        "head_side": [".", ".", ".", ".", ".", ".", ".", "eeddddcc"],
        "head_back": [".", ".", ".", ".", ".", ".", ".", "eeddddcc"],
        "body_front": [".", ".e....d.", "..e..d..", "...ed...", ".", "...ts...", "..tssr..", "...sr...", ".", ".",
                       "..dccb.."],
        "arm_side": TRIM_ARM,
        "boot_side": TRIM_BOOT,
        "boot_front": TRIM_BOOT,
        "belt_front": ["."] * 7 + ["eedssdcc"],
        "belt_back": ["."] * 7 + ["eeddddcc"],
        "belt_side": ["."] * 7 + ["eddc"],
    })


def dragon(kind: str) -> Design:
    """Drachenschuppen: Schuppen, helle Bauchplatten, Stirnplatte mit Stein, Hornansätze und Stachelkamm, Krallen."""
    ramps = icons.dragon_ramps(kind)
    belly = [".", ".", ".", "..feed..", "..edcc..", "..feed..", "..edcc..", "..feed..", "..edcc..", "...dc..."]
    return Design(f"{kind}_dragon_scale", ramps, open_face=True, texture="scales", d={
        "head_top": ["...mk...", ".", "...mk...", ".", "...lk...", ".", "...lk...", "nm....lk"],
        "head_front": ["nm....lk", "m......k", "eeetseed"],
        "head_side": ["......lm", ".......l", ".", ".", ".", ".", ".", "."],
        "head_side_left": ["......kl", ".......k"],
        "head_back": ["...lk...", ".", "...lk...", ".", "...kj..."],
        "body_front": [".", ".", "."] + belly[3:],
        "arm_side": [".", ".", ".", ".", "3333"],
        "boot_front": TRIM_BOOT[:6] + ["3333", ".", ".", ".", ".", "n..l"],
        "belt_front": ["."] * 7 + ["eeffeedc"],
        "belt_back": ["."] * 7 + ["eeeeddcc"],
        "belt_side": ["."] * 7 + ["eddc"],
    })


def designs() -> list[Design]:
    return [steel(), nightsteel(), abyssite(), konterit()] + [dragon(k) for k in icons.DRAGONS]


# =============================================================================
# Zusammensetzen
# =============================================================================

def adult(design: Design) -> tuple[Image.Image, Image.Image]:
    head, body, arm, boots, belt, thighs = parts(design)
    outer = Canvas(64, 32)
    paint(outer, ADULT["head"], head)
    paint(outer, ADULT["body"], body)
    paint(outer, ADULT["arm"], arm)
    paint(outer, ADULT["leg"], boots)
    inner = Canvas(64, 32)
    paint(inner, ADULT["body"], belt)
    paint(inner, ADULT["leg"], thighs)
    return outer.image(design.ramps), inner.image(design.ramps)


def baby(design: Design) -> Image.Image:
    """Baby-Modell: dieselben Zeichnungen, auf die kleineren Quader eingepasst."""
    head, body, arm, boots, belt, thighs = parts(design)
    cv = Canvas(64, 64)

    def fitted(art: Part, box: Box, rows: slice | None = None) -> Part:
        out = {}
        for name, (_, _, w, h) in box.faces().items():
            if name not in art:
                continue
            src = art[name]
            if rows is not None and name in SIDES:
                src = src[rows]
            if not any(ch not in ".," for row in src for ch in row):
                continue
            out[name] = fit(src, w, h)
        return out

    paint(cv, BABY["head"], fitted(head, BABY["head"]))
    paint(cv, BABY["body"], fitted(body, BABY["body"], slice(0, 9)))
    for side in ("left_arm", "right_arm"):
        paint(cv, BABY[side], fitted(arm, BABY[side], slice(0, 5)))
    paint(cv, BABY["waist"], fitted(belt, BABY["waist"], slice(7, 12)))
    for side in ("left_leg", "right_leg"):
        paint(cv, BABY[side], fitted(thighs, BABY[side], slice(0, 9)))
    for side in ("left_foot", "right_foot"):
        paint(cv, BABY[side], fitted(boots, BABY[side], slice(9, 12)))
    return cv.image(design.ramps)


def build(save: Callable[[str, Image.Image], None]) -> None:
    for design in designs():
        outer, inner = adult(design)
        save(f"entity/equipment/humanoid/{design.name}", outer)
        save(f"entity/equipment/humanoid_leggings/{design.name}", inner)
        save(f"entity/equipment/humanoid_baby/{design.name}", baby(design))


if __name__ == "__main__":
    import sys

    out: dict[str, Image.Image] = {}
    build(lambda k, v: out.__setitem__(k, v))
    zoom = 5
    keys = list(out)
    sheet = Image.new("RGBA", (3 * 64 * zoom + 40, (len(keys) // 3) * (64 * zoom + 8)), (139, 139, 139, 255))
    for i, k in enumerate(keys):
        img = out[k]
        big = img.resize((img.width * zoom, img.height * zoom), Image.NEAREST)
        sheet.alpha_composite(big, ((i % 3) * (64 * zoom + 20), (i // 3) * (64 * zoom + 8)))
    sheet.save(sys.argv[1] if len(sys.argv) > 1 else "armor_art.png")
