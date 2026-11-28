#!/usr/bin/env python3
"""
Texturen der Heldenfähigkeiten und Heldentitel (seit 1.11):

- hero/ability/<name>.png (24x24): die acht aktiven Fähigkeiten – Sturmlauf (geflügelter Stiefel mit Windstreifen),
  Kampfschrei (Kriegshorn mit Schallwellen), Eiserne Haut (Schild mit Nieten), Zweiter Atem (Herz mit Lebensfunken),
  Wirbelschlag (Schwert im Wirbel), Adlerblick (Auge mit Schwingen), Himmelssturz (ein Komet schlägt ein) und Letztes
  Gefecht (ein Schwert im Boden, umlodert);
- hero/title/<name>.png (24x24): die Siegel der sechs Titel hinter dem Unsterblichen – Lichtbringer (Strahlensonne),
  Drachenbezwinger (Drachenkopf), Sonnenwächter (Sonne über einem Schild), Sternenschmied (Stern über einem Hammer),
  Weltenhüter (eine Welt mit Ring) und Ewiger (eine ewige Flamme in einem Lichtkranz);
- hero_wheel/base.png (256x256, genutzt 200x200): das Fähigkeitenrad – dunkle Scheibe mit Goldringen und einem
  Achtstern aus warmem Licht (die Fassungen und Zeiger teilt es sich mit dem Zauberkreis).

Wie die Zaubersymbole: Vektor in achtfacher Größe, verkleinert, dunkler Umriss, weicher Schein.

Aufruf:  python3 tools/hero_ability_textures.py [Zielordner für die Texturen] [vorschau.png]
"""
from __future__ import annotations

import math
import sys
from pathlib import Path

from PIL import Image, ImageDraw

sys.path.insert(0, str(Path(__file__).resolve().parent))

from magic_textures import (BIG, GOLD_DARK, GOLD_LIGHT, GOLD_MID, SS, _disc, _down, _ring, _ss_canvas, ellipse_mask,  # noqa: E402
                            fill_mask, finish_icon, glow_layer, poly_mask, radial, rgba, star_points, vertical)
from pixelart import CLEAR, mix  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
TEXTURES = ROOT / "src/main/resources/assets/kingdomsatwar/textures"

ABILITIES = ("sprint", "war_cry", "iron_skin", "second_wind", "whirlwind", "eagle_eye", "skyfall", "last_stand")
TITLES = ("lightbringer", "dragonslayer", "sunwarden", "starforger", "worldkeeper", "eternal")


def canvas() -> Image.Image:
    return Image.new("RGBA", (BIG, BIG), CLEAR)


def line_mask(points: list[tuple[float, float]], width: float) -> Image.Image:
    m = Image.new("L", (BIG, BIG), 0)
    d = ImageDraw.Draw(m)
    d.line([(x * SS, y * SS) for x, y in points], fill=255, width=max(1, round(width * SS)), joint="curve")
    for x, y in (points[0], points[-1]):
        r = width / 2
        d.ellipse([(x - r) * SS, (y - r) * SS, (x + r) * SS, (y + r) * SS], fill=255)
    return m


def arc_mask(cx: float, cy: float, r: float, start: float, end: float, width: float) -> Image.Image:
    m = Image.new("L", (BIG, BIG), 0)
    ImageDraw.Draw(m).arc([(cx - r) * SS, (cy - r) * SS, (cx + r) * SS, (cy + r) * SS], start, end, fill=255,
                          width=max(1, round(width * SS)))
    return m


def subtract(a: Image.Image, b: Image.Image) -> Image.Image:
    from PIL import ImageChops
    return ImageChops.subtract(a, b)


# =============================================================================================
# Fähigkeiten
# =============================================================================================

def icon_sprint() -> Image.Image:
    """Ein Stiefel mit Flügel an der Ferse, dahinter Windstreifen."""
    img = canvas()
    streaks = Image.new("L", (BIG, BIG), 0)
    for y, x0, x1 in ((7.0, 0.8, 7.0), (12.0, 0.4, 5.6), (17.0, 1.2, 6.8)):
        streaks.paste(line_mask([(x0, y), (x1, y)], 1.3), (0, 0), line_mask([(x0, y), (x1, y)], 1.3))
    fill_mask(img, streaks, rgba("#b8f4ff", 220))
    boot = poly_mask([(9.0, 3.0), (15.5, 3.0), (15.5, 13.0), (21.5, 16.0), (22.8, 19.0), (22.8, 21.5), (8.6, 21.5), (8.6, 17.5)])
    img.alpha_composite(glow_layer(boot, rgba("#6ae0ff"), 1.2, 0.7))
    fill_mask(img, boot, vertical(BIG, rgba("#9ad8ff"), rgba("#2a6ec8"), 2 * SS, 22 * SS))
    fill_mask(img, poly_mask([(8.6, 19.6), (22.8, 19.6), (22.8, 21.5), (8.6, 21.5)]), rgba("#1c3c78"))
    fill_mask(img, poly_mask([(9.0, 3.0), (15.5, 3.0), (15.5, 5.0), (9.0, 5.0)]), rgba("#e8f8ff"))
    # der Flügel an der Ferse
    wing = poly_mask([(9.4, 9.0), (3.0, 4.0), (5.0, 8.4), (2.0, 7.6), (4.6, 11.4), (2.4, 11.6), (6.4, 14.2), (9.4, 14.0)])
    fill_mask(img, wing, vertical(BIG, rgba("#ffffff"), rgba("#bfe6ff"), 4 * SS, 14 * SS))
    for x0, y0, x1, y1 in ((4.0, 6.0, 8.8, 10.2), (3.6, 9.6, 8.8, 12.0)):
        fill_mask(img, line_mask([(x0, y0), (x1, y1)], 0.5), rgba("#7ab8e8"))
    return finish_icon(img, outline=(6, 22, 48, 255), glow=rgba("#7ae4ff"))


def bezier(p0, p1, p2, t: float) -> tuple[float, float]:
    return ((1 - t) ** 2 * p0[0] + 2 * (1 - t) * t * p1[0] + t * t * p2[0], (1 - t) ** 2 * p0[1] + 2 * (1 - t) * t * p1[1] + t * t * p2[1])


def tube(p0, p1, p2, w0: float, w1: float, steps: int = 24) -> list[tuple[float, float]]:
    """Umriss eines gebogenen Rohrs entlang einer Bézierkurve, das von w0 auf w1 breiter wird."""
    left, right = [], []
    for i in range(steps + 1):
        t = i / steps
        x, y = bezier(p0, p1, p2, t)
        x2, y2 = bezier(p0, p1, p2, min(1.0, t + 0.01))
        x1, y1 = bezier(p0, p1, p2, max(0.0, t - 0.01))
        dx, dy = x2 - x1, y2 - y1
        n = math.hypot(dx, dy) or 1.0
        nx, ny = -dy / n, dx / n
        w = (w0 + (w1 - w0) * t ** 1.6) / 2
        left.append((x + nx * w, y + ny * w))
        right.append((x - nx * w, y - ny * w))
    return left + right[::-1]


def icon_war_cry() -> Image.Image:
    """Ein Kriegshorn (Bronze mit Silberbändern), gebogen, der Trichter nach rechts oben – daraus brechen Schallwellen."""
    img = canvas()
    p0, p1, p2 = (2.4, 20.6), (4.4, 9.0), (14.6, 7.0)
    horn = poly_mask(tube(p0, p1, p2, 1.6, 8.4))
    img.alpha_composite(glow_layer(horn, rgba("#ffb24a"), 1.0, 0.6))
    fill_mask(img, horn, radial(BIG, 8.0 * SS, 9.0 * SS, 14.0 * SS, rgba("#ffe6b0"), rgba("#94501a"), 1.1))
    for t in (0.35, 0.62, 0.86):
        x, y = bezier(p0, p1, p2, t)
        x2, y2 = bezier(p0, p1, p2, t + 0.01)
        dx, dy = x2 - x, y2 - y
        n = math.hypot(dx, dy) or 1.0
        nx, ny = -dy / n, dx / n
        w = (1.6 + (8.4 - 1.6) * t ** 1.6) / 2 + 0.4
        fill_mask(img, line_mask([(x + nx * w, y + ny * w), (x - nx * w, y - ny * w)], 1.0), rgba("#e8eef8"))
    # der Trichter: dunkle Öffnung
    fill_mask(img, ellipse_mask(15.0, 7.0, 1.6, 4.0), rgba("#2a1206"))
    # Schallwellen
    for r, alpha, width in ((3.6, 255, 1.3), (6.4, 230, 1.2), (9.2, 180, 1.1)):
        fill_mask(img, arc_mask(15.6, 7.0, r, -55, 55, width), rgba("#fff3c8", alpha))
    return finish_icon(img, outline=(40, 18, 4, 255), glow=rgba("#ffb84a"))


def icon_iron_skin() -> Image.Image:
    """Ein Wappenschild aus Eisen mit Rand und Nieten, ein Glanz quer darüber."""
    img = canvas()
    shield = poly_mask([(3.0, 2.6), (21.0, 2.6), (21.0, 11.0), (18.6, 17.0), (12.0, 22.4), (5.4, 17.0), (3.0, 11.0)])
    img.alpha_composite(glow_layer(shield, rgba("#c8d8ff"), 1.1, 0.6))
    fill_mask(img, shield, vertical(BIG, rgba("#e8eef8"), rgba("#5a6478"), 2 * SS, 22 * SS))
    inner = poly_mask([(5.4, 4.8), (18.6, 4.8), (18.6, 10.6), (16.6, 15.6), (12.0, 19.6), (7.4, 15.6), (5.4, 10.6)])
    fill_mask(img, inner, vertical(BIG, rgba("#b8c4d6"), rgba("#46506a"), 4 * SS, 20 * SS))
    fill_mask(img, poly_mask([(11.2, 4.8), (12.8, 4.8), (12.8, 19.0), (11.2, 19.0)]), rgba("#d8e2f0"))
    fill_mask(img, poly_mask([(5.4, 9.6), (18.6, 9.6), (18.6, 11.0), (5.4, 11.0)]), rgba("#d8e2f0"))
    for (x, y) in ((4.4, 3.8), (19.6, 3.8), (4.4, 10.4), (19.6, 10.4), (12.0, 21.0)):
        fill_mask(img, ellipse_mask(x, y, 0.9, 0.9), rgba("#ffffff"))
    fill_mask(img, poly_mask([(6.0, 5.4), (9.0, 5.4), (6.0, 9.0)]), rgba("#ffffff", 200))
    return finish_icon(img, outline=(14, 18, 30, 255), glow=rgba("#b8ccff"))


def icon_second_wind() -> Image.Image:
    """Ein leuchtendes Herz, aus dem grüne Lebensfunken steigen."""
    img = canvas()
    heart = Image.new("L", (BIG, BIG), 0)
    d = ImageDraw.Draw(heart)
    d.ellipse([3.0 * SS, 5.0 * SS, 12.6 * SS, 14.6 * SS], fill=255)
    d.ellipse([11.4 * SS, 5.0 * SS, 21.0 * SS, 14.6 * SS], fill=255)
    d.polygon([(3.4 * SS, 11.4 * SS), (20.6 * SS, 11.4 * SS), (12.0 * SS, 21.6 * SS)], fill=255)
    img.alpha_composite(glow_layer(heart, rgba("#ff5a7a"), 1.3, 0.8))
    fill_mask(img, heart, radial(BIG, 9.0 * SS, 9.0 * SS, 14.0 * SS, rgba("#ffc0cc"), rgba("#b0142e"), 1.0))
    fill_mask(img, ellipse_mask(7.6, 8.6, 2.0, 1.6), rgba("#ffffff", 230))
    # Lebensfunken (ein Kreuz und zwei Sterne)
    fill_mask(img, poly_mask([(16.6, 1.0), (18.4, 1.0), (18.4, 3.0), (20.4, 3.0), (20.4, 4.8), (18.4, 4.8), (18.4, 6.8),
                              (16.6, 6.8), (16.6, 4.8), (14.6, 4.8), (14.6, 3.0), (16.6, 3.0)]), rgba("#9cff8a"))
    fill_mask(img, poly_mask(star_points(4.0, 3.0, 2.0, 0.5, 4)), rgba("#d8ffc8"))
    fill_mask(img, poly_mask(star_points(21.6, 9.4, 1.6, 0.45, 4)), rgba("#d8ffc8"))
    return finish_icon(img, outline=(48, 6, 16, 255), glow=rgba("#ff6a86"))


def icon_whirlwind() -> Image.Image:
    """Ein Schwert, schräg, in einem Wirbel aus drei Bögen mit Pfeilspitzen (er dreht sich)."""
    img = canvas()
    swirl = Image.new("L", (BIG, BIG), 0)
    for a0 in (-80, 40, 160):
        swirl.paste(255, (0, 0), arc_mask(12.0, 12.0, 10.0, a0, a0 + 92, 1.8))
        tip = math.radians(a0 + 92)
        tx, ty = 12.0 + math.cos(tip) * 10.0, 12.0 + math.sin(tip) * 10.0
        tang = tip + math.pi / 2
        nx, ny = math.cos(tip), math.sin(tip)
        swirl.paste(255, (0, 0), poly_mask([(tx + nx * 2.0, ty + ny * 2.0), (tx - nx * 2.0, ty - ny * 2.0),
                                            (tx + math.cos(tang) * 3.0, ty + math.sin(tang) * 3.0)]))
    img.alpha_composite(glow_layer(swirl, rgba("#a8e8ff"), 1.0, 0.8))
    fill_mask(img, swirl, rgba("#e6f8ff"))
    blade = poly_mask([(16.6, 4.0), (19.4, 4.4), (19.8, 7.2), (10.0, 16.4), (7.6, 14.0)])
    fill_mask(img, blade, vertical(BIG, rgba("#ffffff"), rgba("#8ea0b8"), 4 * SS, 16 * SS))
    fill_mask(img, line_mask([(18.6, 5.2), (9.2, 14.6)], 0.6), rgba("#c8d4e4"))
    guard = poly_mask([(6.0, 12.4), (7.4, 11.0), (13.0, 16.6), (11.6, 18.0)])
    fill_mask(img, guard, rgba("#f2c14e"))
    fill_mask(img, line_mask([(8.6, 15.4), (5.4, 18.6)], 1.7), rgba("#7a4a22"))
    fill_mask(img, ellipse_mask(4.8, 19.2, 1.3, 1.3), rgba("#f2c14e"))
    return finish_icon(img, outline=(10, 22, 40, 255), glow=rgba("#9ae0ff"))


def icon_eagle_eye() -> Image.Image:
    """Ein goldenes Auge mit Schwingen zu beiden Seiten."""
    img = canvas()
    for side in (-1, 1):
        pts = [(12.0 + side * 4.0, 9.6), (12.0 + side * 11.4, 4.0), (12.0 + side * 10.4, 8.0), (12.0 + side * 11.8, 8.6),
               (12.0 + side * 9.8, 11.4), (12.0 + side * 10.8, 12.4), (12.0 + side * 7.6, 14.4), (12.0 + side * 4.0, 13.6)]
        wing = poly_mask(pts)
        fill_mask(img, wing, vertical(BIG, rgba("#fff3c0"), rgba("#c88a22"), 4 * SS, 15 * SS))
        for k in range(3):
            y = 7.4 + k * 2.6
            fill_mask(img, line_mask([(12.0 + side * (5.6 + k * 0.6), y + 1.2), (12.0 + side * (9.4 - k * 0.8), y)], 0.45),
                      rgba("#a06a14"))
    eye = poly_mask([(4.6, 12.0), (8.0, 8.2), (12.0, 7.0), (16.0, 8.2), (19.4, 12.0), (16.0, 15.8), (12.0, 17.0), (8.0, 15.8)])
    img.alpha_composite(glow_layer(eye, rgba("#ffd24a"), 1.2, 0.8))
    fill_mask(img, eye, rgba("#fffbe8"))
    fill_mask(img, ellipse_mask(12.0, 12.0, 4.0, 4.0), radial(BIG, 12.0 * SS, 12.0 * SS, 4.0 * SS, rgba("#ffe07a"), rgba("#b8720e"), 0.9))
    fill_mask(img, ellipse_mask(12.0, 12.0, 1.7, 2.6), rgba("#1a0e04"))
    fill_mask(img, ellipse_mask(13.2, 10.6, 0.9, 0.9), rgba("#ffffff"))
    return finish_icon(img, outline=(44, 26, 4, 255), glow=rgba("#ffd24a"))


def icon_skyfall() -> Image.Image:
    """Ein Komet stürzt schräg herab und schlägt ein: ein Sternenkranz aus Splittern über einer Bodenlinie."""
    img = canvas()
    tail = poly_mask([(0.6, 0.6), (5.2, 0.4), (16.4, 10.8), (11.4, 15.6), (0.4, 4.6)])
    fill_mask(img, tail, radial(BIG, 14.0 * SS, 13.0 * SS, 18.0 * SS, rgba("#ffffff"), rgba("#4a7ae8", 90), 0.75))
    inner = poly_mask([(3.0, 2.0), (6.0, 2.4), (14.6, 11.0), (12.6, 13.0), (2.4, 4.4)])
    fill_mask(img, inner, rgba("#d8ecff", 200))
    burst = poly_mask(star_points(14.4, 15.4, 7.6, 2.6, 8, rot=-67.5))
    img.alpha_composite(glow_layer(burst, rgba("#9ac8ff"), 1.0, 0.8))
    fill_mask(img, burst, radial(BIG, 14.4 * SS, 15.4 * SS, 7.6 * SS, rgba("#ffffff"), rgba("#6aa8ff"), 0.9))
    fill_mask(img, ellipse_mask(14.4, 15.4, 3.4, 3.4), radial(BIG, 13.6 * SS, 14.6 * SS, 3.6 * SS, rgba("#ffffff"), rgba("#bfe0ff"), 1.0))
    fill_mask(img, poly_mask([(1.0, 21.6), (23.0, 21.6), (23.0, 23.0), (1.0, 23.0)]), rgba("#6a5a48"))
    return finish_icon(img, outline=(6, 16, 44, 255), glow=rgba("#8ac4ff"))


def icon_last_stand() -> Image.Image:
    """Ein Schwert steckt im Boden, umlodert von roten Flammen – wer hier steht, fällt nicht."""
    img = canvas()
    flames = poly_mask([(4.0, 21.0), (3.2, 14.0), (5.6, 16.4), (5.4, 9.0), (8.6, 13.0), (9.4, 5.0), (12.0, 10.4), (14.4, 4.4),
                        (15.6, 12.4), (18.6, 8.6), (18.6, 15.4), (21.0, 13.4), (20.2, 21.0)])
    img.alpha_composite(glow_layer(flames, rgba("#ff3a2a"), 1.3, 0.9))
    fill_mask(img, flames, vertical(BIG, rgba("#ffd25a"), rgba("#c81e10"), 4 * SS, 21 * SS))
    fill_mask(img, poly_mask([(7.0, 21.0), (8.4, 15.0), (10.2, 17.0), (12.0, 12.0), (13.8, 17.0), (15.6, 15.0), (17.0, 21.0)]),
              rgba("#ffe9a0"))
    blade = poly_mask([(10.8, 3.0), (13.2, 3.0), (13.2, 17.6), (12.0, 19.6), (10.8, 17.6)])
    fill_mask(img, blade, vertical(BIG, rgba("#ffffff"), rgba("#9aa6ba"), 3 * SS, 19 * SS))
    fill_mask(img, poly_mask([(7.4, 3.6), (16.6, 3.6), (16.6, 5.2), (7.4, 5.2)]), rgba("#f2c14e"))
    fill_mask(img, poly_mask([(11.0, 0.4), (13.0, 0.4), (13.0, 3.6), (11.0, 3.6)]), rgba("#6a3a1a"))
    fill_mask(img, poly_mask([(1.0, 20.6), (23.0, 20.6), (23.0, 23.0), (1.0, 23.0)]), rgba("#4a3a30"))
    return finish_icon(img, outline=(44, 6, 4, 255), glow=rgba("#ff4a30"))


ABILITY_ICONS = {
    "sprint": icon_sprint, "war_cry": icon_war_cry, "iron_skin": icon_iron_skin, "second_wind": icon_second_wind,
    "whirlwind": icon_whirlwind, "eagle_eye": icon_eagle_eye, "skyfall": icon_skyfall, "last_stand": icon_last_stand,
}


# =============================================================================================
# Titel
# =============================================================================================

def seal(img: Image.Image, rim: tuple, well_top: tuple, well_bottom: tuple) -> None:
    """Ein runder Siegelgrund: Metallrand mit Kerben, darin eine gewölbte Scheibe."""
    disc = ellipse_mask(12.0, 12.0, 11.0, 11.0)
    fill_mask(img, disc, radial(BIG, 9.0 * SS, 8.0 * SS, 16.0 * SS, mix(rim, rgba("#ffffff"), 0.55), mix(rim, rgba("#000000"), 0.45), 1.0))
    for i in range(16):
        a = math.radians(i * 22.5)
        x, y = 12.0 + math.cos(a) * 10.2, 12.0 + math.sin(a) * 10.2
        fill_mask(img, ellipse_mask(x, y, 0.55, 0.55), mix(rim, rgba("#ffffff"), 0.7))
    fill_mask(img, ellipse_mask(12.0, 12.0, 8.6, 8.6), vertical(BIG, well_top, well_bottom, 3 * SS, 21 * SS))


def title_lightbringer() -> Image.Image:
    img = canvas()
    seal(img, rgba("#f6dc96"), rgba("#3a2a5a"), rgba("#120a22"))
    rays = poly_mask(star_points(12.0, 12.0, 8.2, 3.0, 12))
    img.alpha_composite(glow_layer(rays, rgba("#fff6c8"), 0.8, 0.9))
    fill_mask(img, rays, radial(BIG, 12.0 * SS, 12.0 * SS, 8.2 * SS, rgba("#ffffff"), rgba("#ffd25a"), 0.8))
    fill_mask(img, ellipse_mask(12.0, 12.0, 2.6, 2.6), rgba("#ffffff"))
    return finish_icon(img, outline=(40, 30, 10, 255), glow=rgba("#fff0a0"))


def title_dragonslayer() -> Image.Image:
    img = canvas()
    seal(img, rgba("#d8a050"), rgba("#4a1414"), rgba("#1a0606"))
    head = poly_mask([(6.0, 15.6), (8.6, 9.6), (12.2, 7.2), (15.0, 4.2), (14.6, 7.8), (17.6, 9.0), (18.6, 12.0), (15.4, 12.6),
                      (17.4, 15.0), (12.6, 15.0), (10.6, 18.4)])
    fill_mask(img, head, vertical(BIG, rgba("#ff7a5a"), rgba("#8a1a14"), 4 * SS, 19 * SS))
    fill_mask(img, ellipse_mask(13.2, 9.8, 0.9, 0.7), rgba("#ffe07a"))
    fill_mask(img, line_mask([(8.0, 18.6), (17.6, 5.8)], 1.2), rgba("#e8eef8"))
    fill_mask(img, line_mask([(6.4, 16.0), (9.6, 19.6)], 1.2), rgba("#f2c14e"))
    return finish_icon(img, outline=(40, 8, 6, 255), glow=rgba("#ff7a4a"))


def title_sunwarden() -> Image.Image:
    img = canvas()
    seal(img, rgba("#ffb84a"), rgba("#4a2a10"), rgba("#1a0c04"))
    sun = poly_mask(star_points(12.0, 9.4, 6.0, 3.4, 8))
    fill_mask(img, sun, radial(BIG, 12.0 * SS, 9.4 * SS, 6.0 * SS, rgba("#fffbe0"), rgba("#ff9a20"), 0.8))
    shield = poly_mask([(7.6, 11.6), (16.4, 11.6), (16.4, 15.0), (12.0, 19.6), (7.6, 15.0)])
    fill_mask(img, shield, vertical(BIG, rgba("#e8eef8"), rgba("#6a748a"), 11 * SS, 20 * SS))
    fill_mask(img, poly_mask([(11.4, 12.4), (12.6, 12.4), (12.6, 18.4), (11.4, 18.4)]), rgba("#ffb84a"))
    return finish_icon(img, outline=(44, 22, 4, 255), glow=rgba("#ffb84a"))


def title_starforger() -> Image.Image:
    img = canvas()
    seal(img, rgba("#b8a8ff"), rgba("#1a1848"), rgba("#08061a"))
    star = poly_mask(star_points(12.0, 8.6, 5.4, 2.2, 5))
    img.alpha_composite(glow_layer(star, rgba("#c8b8ff"), 0.8, 0.9))
    fill_mask(img, star, radial(BIG, 12.0 * SS, 8.6 * SS, 5.4 * SS, rgba("#ffffff"), rgba("#9a7aff"), 0.8))
    fill_mask(img, line_mask([(12.0, 13.4), (12.0, 19.6)], 1.4), rgba("#8a5a2a"))
    fill_mask(img, poly_mask([(8.4, 12.2), (15.6, 12.2), (15.6, 15.0), (8.4, 15.0)]), vertical(BIG, rgba("#e8eef8"), rgba("#6a748a"),
                                                                                                12 * SS, 15 * SS))
    return finish_icon(img, outline=(14, 10, 40, 255), glow=rgba("#b0a0ff"))


def title_worldkeeper() -> Image.Image:
    img = canvas()
    seal(img, rgba("#7ae8d8"), rgba("#0a2a3a"), rgba("#04101a"))
    world = ellipse_mask(12.0, 12.0, 5.6, 5.6)
    fill_mask(img, world, radial(BIG, 10.4 * SS, 10.4 * SS, 7.0 * SS, rgba("#9ad8ff"), rgba("#1a4ab0"), 1.0))
    fill_mask(img, poly_mask([(8.0, 10.0), (10.6, 8.0), (12.4, 9.6), (11.0, 12.0), (12.6, 14.6), (10.0, 15.6), (8.4, 13.4)]),
              rgba("#5ad06a"))
    fill_mask(img, poly_mask([(13.6, 8.4), (16.0, 9.4), (16.8, 12.4), (14.6, 11.6)]), rgba("#5ad06a"))
    ring = arc_mask(12.0, 12.0, 8.0, 160, 380, 1.2)
    fill_mask(img, ring, rgba("#fff0b0"))
    return finish_icon(img, outline=(4, 22, 30, 255), glow=rgba("#7ae8d8"))


def title_eternal() -> Image.Image:
    img = canvas()
    seal(img, rgba("#fff4e0"), rgba("#2a1a4a"), rgba("#0a0618"))
    for i in range(8):
        a = math.radians(i * 45 - 90)
        hue = (rgba("#ff9aaa"), rgba("#ffd27a"), rgba("#fff7a0"), rgba("#a8ffb0"), rgba("#9ae8ff"), rgba("#a8b8ff"), rgba("#d8a8ff"),
               rgba("#ffb8e8"))[i]
        fill_mask(img, ellipse_mask(12.0 + math.cos(a) * 7.0, 12.0 + math.sin(a) * 7.0, 1.1, 1.1), hue)
    flame = poly_mask([(12.0, 4.6), (15.6, 10.6), (15.4, 14.6), (12.0, 17.6), (8.6, 14.6), (8.4, 10.6), (10.6, 11.6)])
    img.alpha_composite(glow_layer(flame, rgba("#fff4c8"), 1.0, 1.0))
    fill_mask(img, flame, vertical(BIG, rgba("#ffffff"), rgba("#ffb84a"), 4 * SS, 18 * SS))
    fill_mask(img, poly_mask([(12.0, 9.0), (13.8, 12.4), (12.0, 15.6), (10.2, 12.4)]), rgba("#ffffff"))
    return finish_icon(img, outline=(30, 20, 44, 255), glow=rgba("#fff0d0"))


TITLE_SEALS = {
    "lightbringer": title_lightbringer, "dragonslayer": title_dragonslayer, "sunwarden": title_sunwarden,
    "starforger": title_starforger, "worldkeeper": title_worldkeeper, "eternal": title_eternal,
}


# =============================================================================================
# Fähigkeitenrad
# =============================================================================================

NAVY = rgba("#1c1830")
NAVY_DEEP = rgba("#0a0814")
AMBER = rgba("#ffc860")


def wheel_base() -> Image.Image:
    size = 200
    c = size / 2
    img, d, k = _ss_canvas(size, size)
    for r in range(97, 0, -1):
        t = r / 97
        alpha = round(150 + 70 * (1 - t) ** 1.5)
        col = mix(NAVY, NAVY_DEEP, 1 - t)[:3] + (alpha,)
        _disc(d, k, c, c, r, col)
    _ring(d, k, c, c, 98.0, 2.2, GOLD_DARK)
    _ring(d, k, c, c, 97.6, 1.2, GOLD_MID)
    _ring(d, k, c, c, 98.6, 0.6, GOLD_LIGHT[:3] + (180,))
    _ring(d, k, c, c, 86.0, 1.0, GOLD_DARK[:3] + (220,))
    _ring(d, k, c, c, 85.6, 0.5, GOLD_MID[:3] + (200,))
    for i in range(64):
        a = math.radians(i * 360 / 64 - 90)
        r0, r1 = (93.0, 97.5) if i % 8 == 0 else (95.5, 97.5)
        d.line([((c + math.cos(a) * r0) * k, (c + math.sin(a) * r0) * k), ((c + math.cos(a) * r1) * k, (c + math.sin(a) * r1) * k)],
               fill=GOLD_MID[:3] + (230 if i % 8 == 0 else 150,), width=max(1, round(0.8 * k)))
    # Achtstern durch die acht Fassungen (Abstand 74)
    pts = [(c + math.sin(math.radians(i * 45)) * 74, c - math.cos(math.radians(i * 45)) * 74) for i in range(8)]
    for step, alpha in ((3, 70), (2, 40)):
        for i in range(8):
            a, b = pts[i], pts[(i + step) % 8]
            d.line([(a[0] * k, a[1] * k), (b[0] * k, b[1] * k)], fill=AMBER[:3] + (alpha,), width=max(1, round(0.9 * k)))
    _ring(d, k, c, c, 74.0, 0.8, AMBER[:3] + (60,))
    for r in range(52, 0, -1):
        t = r / 52
        _disc(d, k, c, c, r, mix(rgba("#2a2244"), rgba("#07060e"), t ** 0.7)[:3] + (235,))
    _ring(d, k, c, c, 52.5, 2.0, GOLD_DARK)
    _ring(d, k, c, c, 52.2, 1.0, GOLD_MID)
    _ring(d, k, c, c, 50.0, 0.5, AMBER[:3] + (120,))
    for i in range(8):
        a = math.radians(i * 45 - 90)
        x, y = c + math.cos(a) * 52.4, c + math.sin(a) * 52.4
        _disc(d, k, x, y, 2.0, GOLD_MID)
        _disc(d, k, x - 0.4, y - 0.4, 0.9, GOLD_LIGHT)
    out = Image.new("RGBA", (256, 256), CLEAR)
    out.alpha_composite(_down(img, size, size))
    return out


def main() -> None:
    target = Path(sys.argv[1]) if len(sys.argv) > 1 and not sys.argv[1].endswith(".png") else TEXTURES
    preview = next((a for a in sys.argv[1:] if a.endswith(".png")), None)
    icons = {}
    for name, fn in ABILITY_ICONS.items():
        icons["ability/" + name] = fn()
    for name, fn in TITLE_SEALS.items():
        icons["title/" + name] = fn()
    for name, img in icons.items():
        path = target / "gui/sprites/hero" / (name + ".png")
        path.parent.mkdir(parents=True, exist_ok=True)
        img.save(path)
    base = target / "gui/hero_wheel/base.png"
    base.parent.mkdir(parents=True, exist_ok=True)
    wheel = wheel_base()
    wheel.save(base)
    print(f"{len(icons)} Symbole und das Fähigkeitenrad geschrieben nach {target}")
    if preview:
        scale = 6
        names = list(icons)
        sheet = Image.new("RGBA", (len(names) * (24 * scale + 8), 24 * scale + 220), (34, 28, 48, 255))
        for i, name in enumerate(names):
            sheet.alpha_composite(icons[name].resize((24 * scale, 24 * scale), Image.NEAREST), (i * (24 * scale + 8), 0))
        sheet.alpha_composite(wheel.crop((0, 0, 200, 200)), (0, 24 * scale + 12))
        sheet.save(preview)


if __name__ == "__main__":
    main()
