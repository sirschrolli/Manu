#!/usr/bin/env python3
"""
Erzeugt die Texturen der untoten Dorfbewohner (128x64, gleiches Schema wie tools/generate_villagers.py und dasselbe
Modell, client/model/CitizenModel.java): jeder Beruf mit seinen drei Gesichtern – fahlgrüne, fleckige Haut mit Wunden,
eingefallene Augen mit glimmendem Rot, die Kleidung verblichen, verdreckt und zerrissen. Wer geheilt wird, ist danach
wieder genau der Dorfbewohner gleichen Berufs und Gesichts.

Die Bilder entstehen aus denselben Mal-Funktionen wie die der Lebenden (mit untoter Haut und untoten Augen) und
werden danach gealtert: Stoff ausgeblichen und verschmutzt, Risse (in der Grundschicht scheint die Haut durch, in der
zweiten Schicht und an den Zusatzteilen fehlt der Stoff), Flecken, Blut; Haare stumpf und schütter.

Aufruf (aus dem Projektordner):
    python3 tools/generate_undead_villagers.py            # schreibt alle Texturen
    python3 tools/generate_undead_villagers.py --preview  # zusätzlich eine Vorschau
Ergebnis: textures/entity/undead_villager/<beruf>_<n>.png und <beruf>_<n>_glow.png (die glimmenden Augen)
"""
from __future__ import annotations

import colorsys
import sys
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
import generate_soldiers as gs  # noqa: E402
import generate_villagers as gv  # noqa: E402
from pixelart import CLEAR, hexc, mix  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
TEX = ROOT / "src" / "main" / "resources" / "assets" / "kingdomsatwar" / "textures" / "entity" / "undead_villager"
SCRATCH = Path("/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad")

SKIN = gs.pal("#2c3a27", "#435638", "#5a7049", "#72895c", "#8ea372")
IRIS = hexc("#ff5532")
SOCKET = hexc("#16110d")
FLESH = gs.pal("#3a1210", "#5e1e1a", "#82322a", "#a04a3c")
BONE = gs.pal("#9a927c", "#c4bca4", "#e2dccb")
GRIME = hexc("#3a3a22")
BLOOD = gs.pal("#2e0806", "#4e0e0a", "#6e1812")
GLOW = hexc("#ff7a3a")

# zweite Schicht und Zusatzteile: Risse lassen hier den Stoff ganz verschwinden
OUTER = ("hat", "jacket", "rsleeve", "lsleeve", "rpants", "lpants")


def _region(u: int, v: int) -> str | None:
    """Welcher Teil an dieser Stelle des Schemas liegt (Zusatzteile rechts zählen als ein Bereich)."""
    if u >= 64:
        return "extra"
    for part, (pu, pv, w, h, d) in gs.PARTS.items():
        if pu >= 64:
            continue
        if pu <= u < pu + 2 * (w + d) and pv <= v < pv + d + h:
            return part
    return None


def _hsv_shift(c, sat: float, val: float, toward=None, amount: float = 0.0):
    r, g, b, a = c
    h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
    r2, g2, b2 = colorsys.hsv_to_rgb(h, s * sat, v * val)
    out = (round(r2 * 255), round(g2 * 255), round(b2 * 255), a)
    return mix(out, toward, amount) if toward is not None else out


def _noise(u: int, v: int, seed: str, cell: int = 2) -> float:
    """Grober, fester Zufall über Zellen (damit Flecken und Risse ein paar Pixel groß werden)."""
    return gs.h01(seed, u // cell, v // cell)


def _face_of(region: str, u: int, v: int) -> tuple[str, int, int, int, int] | None:
    """(Seite, Spalte, Zeile, Breite, Höhe) eines Pixels in einem Teil des Schemas."""
    for face in ("top", "bottom", "right", "front", "left", "back"):
        x0, y0, w, h = gs.face_rect(region, face)
        if x0 <= u < x0 + w and y0 <= v < y0 + h:
            return face, u - x0, v - y0, w, h
    return None


def _hem(region: str, u: int, v: int, key: str) -> bool:
    """Ausgefranster Saum: die unterste Reihe von Ärmeln, Hemd und Hosenbeinen reißt unregelmäßig aus."""
    if region not in ("rsleeve", "lsleeve", "rpants", "lpants"):
        return False
    f = _face_of(region, u, v)
    if f is None or f[0] in ("top", "bottom"):
        return False
    face, s, t, w, h = f
    cut = h - 1 - int(gs.h01(key, "hem", region, face, s) * 2.2)
    # nur bei kurzen Ärmeln (zweite Schicht) bzw. dort, wo ohnehin Stoff endet
    return t >= cut and gs.h01(key, "hemon", region) > 0.35


def zombify(img: Image.Image, key: str, hair: list) -> tuple[Image.Image, Image.Image]:
    out = img.copy()
    glow = Image.new("RGBA", img.size, CLEAR)
    src, px, gx = img.load(), out.load(), glow.load()
    skin = set(SKIN)
    hairs = set(hair)
    for v in range(img.height):
        for u in range(img.width):
            c = src[u, v]
            if c[3] == 0:
                continue
            region = _region(u, v)
            if c[:3] == IRIS[:3]:
                gx[u, v] = GLOW
                continue
            if c[:3] == SOCKET[:3]:
                continue
            if c in skin:
                if region in ("rarm", "larm", "body", "rleg", "lleg") and _noise(u, v, key + "rot", 2) > 0.965:
                    px[u, v] = FLESH[2] if gs.h01(key, u, v) > 0.35 else BONE[1]     # offene Wunde mit Knochen
                elif _noise(u, v, key + "blotch", 3) > 0.84:
                    px[u, v] = mix(c, SKIN[0], 0.35)                                # dunkle Flecken
                continue
            if c in hairs:
                # Haare: stumpf und grau durchsetzt
                px[u, v] = _hsv_shift(c, 0.55, 0.88, hexc("#6a6a5a"), 0.15)
                continue
            # Stoff, Leder, Metall: ausgeblichen und verschmutzt, wenige Risse, ausgefranste Säume
            tear = _noise(u, v, key + "tear", 2) > 0.968 or _hem(region or "", u, v, key)
            if tear:
                if region in OUTER or region == "extra":
                    px[u, v] = CLEAR
                    continue
                if region in ("body", "rarm", "larm", "rleg", "lleg"):
                    px[u, v] = SKIN[2] if gs.h01(key, u, v, 1) > 0.4 else SKIN[1]
                    continue
            aged = _hsv_shift(c, 0.62, 0.88, GRIME, 0.16)
            if _noise(u, v, key + "stain", 4) > 0.86:
                aged = mix(aged, GRIME, 0.3)
            px[u, v] = aged
    _blood(px, key)
    return out, glow


def _blood(px, key: str):
    """Ein paar dunkle Blutspuren: vom Mund über das Kinn und vorn auf den Kragen."""
    fx, fy, fw, fh = gs.face_rect("head", "front")
    for s in (3, 4):
        if gs.h01(key, "chin", s) > 0.4:
            px[fx + s, fy + 7] = BLOOD[2]
    bx, by, bw, bh = gs.face_rect("jacket", "front")
    for i in range(3):
        s = 2 + int(gs.h01(key, "drip", i) * 4)
        for t in range(int(1 + gs.h01(key, "len", i) * 3)):
            if px[bx + s, by + t][3] > 0:
                px[bx + s, by + t] = BLOOD[1 if t else 2]


def all_skins() -> dict[str, tuple[Image.Image, Image.Image]]:
    gs.SKINS["undead"] = SKIN
    gs.IRISES["undead"] = IRIS
    eye_white = gs.EYE_WHITE
    gs.EYE_WHITE = SOCKET
    try:
        out: dict[str, tuple[Image.Image, Image.Image]] = {}
        for name, (painter, looks) in gv.PROFESSIONS.items():
            for i, look in enumerate(looks):
                undead = ("undead", look[1], "undead") + tuple(look[3:])
                key = f"{name}_{i + 1}"
                img = painter(undead, i == len(looks) - 1).img
                out[key] = zombify(img, key, gs.HAIRS[look[1]])
        return out
    finally:
        gs.EYE_WHITE = eye_white
        del gs.SKINS["undead"]
        del gs.IRISES["undead"]


def main():
    skins = all_skins()
    TEX.mkdir(parents=True, exist_ok=True)
    for key, (img, glow) in skins.items():
        img.save(TEX / f"{key}.png")
        glow.save(TEX / f"{key}_glow.png")
    print(f"{len(skins)} Texturen untoter Dorfbewohner geschrieben")
    if "--preview" in sys.argv:
        scale = 5
        keys = list(skins)
        cols = 9
        rows = (len(keys) + cols - 1) // cols
        sheet = Image.new("RGBA", (cols * 36 * scale, rows * 34 * scale), (90, 110, 130, 255))
        for i, key in enumerate(keys):
            for j, back in enumerate((False, True)):
                v = gs.front_view(skins[key][0], back).resize((16 * scale, 32 * scale), Image.NEAREST)
                sheet.alpha_composite(v, ((i % cols) * 36 * scale + j * 17 * scale, (i // cols) * 34 * scale + 4))
        sheet.save(SCRATCH / "undead_villager_preview.png")
        print("Vorschau:", SCRATCH / "undead_villager_preview.png")


if __name__ == "__main__":
    main()
