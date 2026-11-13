#!/usr/bin/env python3
"""
Grafiken der Heldenstufe (eigene Pixelkunst, alles hier gezeichnet; die Hand beim Streicheln stammt aus PatPat, siehe
src/main/resources/licenses/patpat/NOTICE.md):

  gui/sprites/hero/panel            Fenster: dunkles Nachtblau, goldener Rahmen mit Eckstücken und Rubinen (Neun-Teile)
  gui/sprites/hero/inset            vertieftes Feld (Neun-Teile)
  gui/sprites/hero/card[_hover]     Zeile eines Talents (Neun-Teile)
  gui/sprites/hero/button[_highlighted|_disabled]   Knöpfe im selben Stil (Neun-Teile, 200 × 20 wie bei Minecraft)
  gui/sprites/hero/plus[_highlighted|_disabled]     runder Goldknopf mit Plus (13 × 13)
  gui/sprites/hero/emblem/<rang>    Rangabzeichen 48 × 48: Knappe (Bronze), Recke (Silber, Lorbeer), Ritter (Gold, Krone),
                                    Held (Gold und Smaragde), Kriegsherr (Platin, Schwingen, Diamant), Legende (Feuergold,
                                    Strahlenkranz, Schwingen, Rubin)
  gui/sprites/hero/badge/<rang>     dieselben Abzeichen klein (15 × 15) für Rangleiste und Anzeige
  gui/sprites/hero/rays|glow|ring|sparkle   Licht für die Aufstiegs-Szene – additiv gezeichnet, also vormultipliziert
                                    (Schwarz = nichts, Weiß = volles Licht)

    python3 tools/hero_gui_textures.py            # schreibt nach src/main/resources/assets/kingdomsatwar/...
    python3 tools/hero_gui_textures.py ORDNER     # schreibt stattdessen nach ORDNER (zum Ansehen)
"""
from __future__ import annotations

import json
import math
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/kingdomsatwar"

RGBA = tuple[int, int, int, int]


def hexc(h: str, a: int = 255) -> RGBA:
    h = h.lstrip("#")
    return int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a


def mix(a: RGBA, b: RGBA, t: float) -> RGBA:
    t = max(0.0, min(1.0, t))
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(4))  # type: ignore[return-value]


CLEAR = (0, 0, 0, 0)
INK = hexc("0B0910")
BG = hexc("1B1726")
BG_LIGHT = hexc("231E31")
GOLD_HI = hexc("FFE7A0")
GOLD = hexc("E2B24A")
GOLD_MID = hexc("B5832C")
GOLD_DK = hexc("6E4A1A")
RUBY = (hexc("FF8A8A"), hexc("D8323A"), hexc("7A1018"))


def save(img: Image.Image, path: Path, nine: tuple | None = None) -> None:
    """Speichert ein Bild; mit {@code nine} = (Breite, Höhe, Rand) oder (Breite, Höhe, links, oben, rechts, unten) als Neun-Teile."""
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path)
    meta = path.with_name(path.name + ".mcmeta")
    if nine is not None:
        w, h = nine[0], nine[1]
        border = nine[2] if len(nine) == 3 else {"left": nine[2], "top": nine[3], "right": nine[4], "bottom": nine[5]}
        meta.write_text(json.dumps({"gui": {"scaling": {"type": "nine_slice", "width": w, "height": h, "border": border}}}, indent=4) + "\n")
    elif meta.exists():
        meta.unlink()


# =====================================================================================================================
# Fenster, Felder, Knöpfe
# =====================================================================================================================

def gem(px, cx: float, cy: float, r: float, colors: tuple[RGBA, RGBA, RGBA]) -> None:
    """Ein runder geschliffener Stein: oben links hell, unten rechts dunkel, ein weißer Glanzpunkt."""
    hi, mid, dk = colors
    for y in range(math.floor(cy - r - 1), math.ceil(cy + r + 1)):
        for x in range(math.floor(cx - r - 1), math.ceil(cx + r + 1)):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            d = math.hypot(dx, dy)
            if d <= r:
                shade = (-dx - dy) / (2 * r) + 0.5
                px[x, y] = mix(dk, mid, shade * 1.6) if shade < 0.62 else mix(mid, hi, (shade - 0.62) * 2.6)
            elif d <= r + 0.9:
                px[x, y] = GOLD_DK
    hx, hy = round(cx - r * 0.45 - 0.5), round(cy - r * 0.45 - 0.5)
    px[hx, hy] = (255, 255, 255, 255)


def panel() -> Image.Image:
    size, border = 64, 10
    img = Image.new("RGBA", (size, size), BG)
    px = img.load()
    rings = [INK, GOLD_DK, GOLD, GOLD_MID, GOLD_DK, INK]
    for i, color in enumerate(rings):
        for k in range(i, size - i):
            for (x, y) in ((k, i), (k, size - 1 - i), (i, k), (size - 1 - i, k)):
                c = color
                if color == GOLD and (x == i or y == i) and not (x == size - 1 - i or y == size - 1 - i):
                    c = GOLD_HI  # Licht von oben links
                px[x, y] = c
    # runde Außenecken
    for (x, y) in ((0, 0), (1, 0), (0, 1)):
        for (fx, fy) in ((x, y), (size - 1 - x, y), (x, size - 1 - y), (size - 1 - x, size - 1 - y)):
            px[fx, fy] = CLEAR
    # Eckstücke: goldene Platte mit Fase und einem Rubin
    for (ox, oy) in ((0, 0), (size - border, 0), (0, size - border), (size - border, size - border)):
        for y in range(1, border - 1):
            for x in range(1, border - 1):
                fx, fy = ox + x, oy + y
                edge = x in (1, border - 2) or y in (1, border - 2)
                if edge:
                    px[fx, fy] = INK if (x == border - 2 or y == border - 2) else GOLD_DK
                else:
                    t = (x + y) / (2 * (border - 2))
                    px[fx, fy] = mix(GOLD_HI, GOLD_MID, t)
        gem(px, ox + border / 2, oy + border / 2, 2.2, RUBY)
    # das Innere bleibt glatt: es wird gekachelt, jede Linie darin wiederholte sich
    return img


def inset() -> Image.Image:
    img = Image.new("RGBA", (16, 16), hexc("120F1B"))
    px = img.load()
    for k in range(16):
        px[k, 0] = hexc("07060B")
        px[0, k] = hexc("07060B")
        px[k, 15] = hexc("3D3552")
        px[15, k] = hexc("3D3552")
    for k in range(1, 15):
        px[k, 1] = hexc("0C0A13")
        px[1, k] = hexc("0C0A13")
    px[15, 0] = hexc("231E31")
    px[0, 15] = hexc("231E31")
    return img


def card(hover: bool) -> Image.Image:
    w, h = 32, 20
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    border = GOLD_MID if hover else hexc("3A3350")
    top = GOLD if hover else hexc("4A4166")
    for y in range(h):
        for x in range(w):
            if (x in (0, w - 1)) and (y in (0, h - 1)):
                continue
            if x in (0, w - 1) or y in (0, h - 1):
                px[x, y] = top if y == 0 else border
            else:
                px[x, y] = mix(hexc("2C2640" if hover else "251F36"), hexc("201B2E" if hover else "1D1829"), y / h)
    return img


def button(state: str) -> Image.Image:
    w, h = 200, 20
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    if state == "disabled":
        outer, rim, top, bg0, bg1 = INK, hexc("4A4458"), hexc("5A536A"), hexc("25222D"), hexc("1C1A22")
    elif state == "highlighted":
        outer, rim, top, bg0, bg1 = INK, GOLD, GOLD_HI, hexc("4A3C6A"), hexc("2E2544")
    else:
        outer, rim, top, bg0, bg1 = INK, GOLD_MID, GOLD, hexc("3A2F55"), hexc("241D36")
    for y in range(h):
        for x in range(w):
            if (x in (0, w - 1)) and (y in (0, h - 1)):
                continue
            if x in (0, w - 1) or y in (0, h - 1):
                px[x, y] = outer
            elif x in (1, w - 2) or y in (1, h - 2):
                px[x, y] = top if y == 1 else rim if y != h - 2 else mix(rim, INK, 0.45)
            else:
                px[x, y] = mix(bg0, bg1, (y - 2) / (h - 4))
    # ein Hauch Glanz unter der oberen Kante
    for x in range(2, w - 2):
        px[x, 2] = mix(px[x, 2], (255, 255, 255, 255), 0.10 if state != "disabled" else 0.03)
    return img


def plus(state: str) -> Image.Image:
    size = 13
    img = Image.new("RGBA", (size, size), CLEAR)
    px = img.load()
    c = (size - 1) / 2
    if state == "disabled":
        hi, mid, dk, sign = hexc("8A8496"), hexc("5E596A"), hexc("3A3644"), hexc("2A2731")
    elif state == "highlighted":
        hi, mid, dk, sign = hexc("FFF4C8"), hexc("F2C65A"), hexc("A8741E"), hexc("4A2A08")
    else:
        hi, mid, dk, sign = GOLD_HI, GOLD, GOLD_MID, hexc("5A3610")
    for y in range(size):
        for x in range(size):
            dx, dy = x - c, y - c
            d = math.hypot(dx, dy)
            if d <= 5.6:
                shade = (-dx - dy) / 11.0 + 0.5
                px[x, y] = mix(dk, mid, shade * 1.7) if shade < 0.6 else mix(mid, hi, (shade - 0.6) * 2.5)
            elif d <= 6.5:
                px[x, y] = INK
    for k in range(-3, 4):
        for t in (0, 1):
            px[int(c) + k, int(c) - 1 + t] = sign
            px[int(c) - 1 + t, int(c) + k] = sign
    return img


# =====================================================================================================================
# Rangabzeichen
# =====================================================================================================================

RANKS = ("squire", "warrior", "knight", "hero", "warlord", "legend", "myth", "demigod", "titan", "starwalker", "immortal")
# Metall des Rings: hell, mittel, dunkel
METAL = {
    "squire": (hexc("F2B47C"), hexc("C07A42"), hexc("6A3A18")),
    "warrior": (hexc("FFFFFF"), hexc("B9C3D0"), hexc("5E6878")),
    "knight": (hexc("FFF0B0"), hexc("E4B23E"), hexc("7A5216")),
    "hero": (hexc("FFF0B0"), hexc("E4B23E"), hexc("7A5216")),
    "warlord": (hexc("FFFFFF"), hexc("C8E8F2"), hexc("5A7E92")),
    "legend": (hexc("FFF6C8"), hexc("FFB43A"), hexc("A03A10")),
    # seit 0.77: die Ränge hinter der Legende
    "myth": (hexc("F6EEFF"), hexc("B48CF0"), hexc("4A2884")),
    "demigod": (hexc("FFFFF6"), hexc("F4E0A0"), hexc("8C6C2C")),
    "titan": (hexc("FFD8B8"), hexc("C8643A"), hexc("561E0C")),
    "starwalker": (hexc("EEF6FF"), hexc("82AAEC"), hexc("1C3874")),
    "immortal": (hexc("FFFFFF"), hexc("FFEBB0"), hexc("A8782A")),
}
GEMS = {
    "knight": RUBY,
    "hero": (hexc("B8FFD8"), hexc("2FC46E"), hexc("0E5A30")),
    "warlord": (hexc("F2FFFF"), hexc("5FE0F2"), hexc("1A6E8A")),
    "legend": (hexc("FFD0E0"), hexc("FF3A5A"), hexc("7A0A24")),
    "myth": (hexc("F2DCFF"), hexc("A85AF0"), hexc("481886")),
    "demigod": (hexc("FFFFFF"), hexc("BDF2FF"), hexc("4AA6C8")),
    "titan": (hexc("FFE0B8"), hexc("FF5A2A"), hexc("781A08")),
    "starwalker": (hexc("DCEEFF"), hexc("3E7CFF"), hexc("10286E")),
    "immortal": (hexc("FFFFFF"), hexc("FFB8F0"), hexc("6A8AFF")),
}
LAUREL = {
    "warrior": (hexc("E6EBF2"), hexc("9AA6B6"), hexc("4E5868")),
    "knight": (hexc("FFE58A"), hexc("D49A2E"), hexc("6E4614")),
    "hero": (hexc("A8F0A0"), hexc("3EA84A"), hexc("1A5420")),
    "warlord": (hexc("FFE58A"), hexc("D49A2E"), hexc("6E4614")),
    "legend": (hexc("FFE58A"), hexc("E8A02E"), hexc("7A3E10")),
    "myth": (hexc("EEE0FF"), hexc("9C7ACC"), hexc("482E70")),
    "demigod": (hexc("FFF6C8"), hexc("E8C450"), hexc("7A5A16")),
    "titan": (hexc("FFCC94"), hexc("B8642A"), hexc("561E08")),
    "starwalker": (hexc("F2F8FF"), hexc("A8BCDC"), hexc("48587A")),
    "immortal": (hexc("FFFFFF"), hexc("FFE58A"), hexc("A8782A")),
}
WINGS = {
    "warlord": (hexc("FFFFFF"), hexc("D2E2EE"), hexc("7890A8")),
    "legend": (hexc("FFF2C0"), hexc("FFB447"), hexc("B04A12")),
    "myth": (hexc("FFFFFF"), hexc("DCCCF6"), hexc("7864AC")),
    "demigod": (hexc("FFFFFF"), hexc("FFF2C8"), hexc("C8A050")),
    "titan": (hexc("FFD4AC"), hexc("A85A2A"), hexc("481C0A")),
    "starwalker": (hexc("EAF2FF"), hexc("7C9CE2"), hexc("1C2C6C")),
    "immortal": (hexc("FFFFFF"), hexc("FFF4D2"), hexc("E0B050")),
}
# Strahlenkranz hinter allem (Legende und höher): Anzahl, lange und kurze Strahlen, Breite, Farben
RAYS = {
    "legend": (16, 23.4, 20.0, 0.26, (hexc("FFF6C8"), hexc("FFC24A"), hexc("C05A10"))),
    "myth": (12, 23.4, 19.0, 0.30, (hexc("F8EEFF"), hexc("C890FF"), hexc("6A2AB0"))),
    "demigod": (24, 23.0, 20.5, 0.30, (hexc("FFFFFF"), hexc("FFF0B4"), hexc("C89A40"))),
    "titan": (8, 23.6, 21.0, 0.34, (hexc("FFE4C4"), hexc("FF7A3A"), hexc("8A260A"))),
    "starwalker": (8, 23.8, 16.5, 0.18, (hexc("FFFFFF"), hexc("A8C8FF"), hexc("3A5AC8"))),
    "immortal": (16, 23.8, 21.0, 0.24, (hexc("FFFFFF"), hexc("FFEBA6"), hexc("E08AC8"))),
}
# ein Heiligenschein-Ring hinter dem Abzeichen (Halbgott, Unsterblicher)
HALO = {"demigod": hexc("FFF4C8"), "immortal": hexc("FFFFFF")}
DISK = (hexc("2E2840"), hexc("120F1A"))


def _prism(angle: float) -> RGBA:
    """Ein zarter Regenbogenton je Winkel (für den Unsterblichen)."""
    import colorsys
    r, g, b = colorsys.hsv_to_rgb((angle / (2 * math.pi)) % 1.0, 0.32, 1.0)
    return (int(r * 255), int(g * 255), int(b * 255), 255)


def _shade(colors: tuple[RGBA, RGBA, RGBA], t: float) -> RGBA:
    hi, mid, dk = colors
    return mix(dk, mid, t * 2.0) if t < 0.5 else mix(mid, hi, (t - 0.5) * 2.0)


def emblem(rank: str, size: int = 48) -> Image.Image:
    """
    Das Abzeichen eines Rangs (48 × 48; klein 15 × 15 nur Ring, Scheibe und Stein). Jedes Pixel wird aus Formen bestimmt
    (4 × 4 Proben je Pixel, dann hart geschnitten), vorn nach hinten: Stein der Krone, Krone, Ring mit Nieten, Scheibe,
    Lorbeerzweige, Schwingen, Strahlenkranz.
    """
    small = size < 24
    tier = RANKS.index(rank)
    if small:
        cx, cy = size / 2.0, size / 2.0
        r_disk, r_ring = 4.6, 6.6
    else:
        cx, cy = size / 2.0, size / 2.0 + 1.0
        r_disk, r_ring = 10.4, 13.8

    def leaf(x: float, y: float, lx: float, ly: float, a: float, length: float, width: float) -> float | None:
        """Ein spitzes Blatt bei (lx, ly) in Richtung a: Wert 0 (Mittelrippe) bis 1 (Rand), sonst None."""
        ux, uy = math.cos(a), math.sin(a)
        u = (x - lx) * ux + (y - ly) * uy
        v = -(x - lx) * uy + (y - ly) * ux
        if u < -length * 0.15 or u > length:
            return None
        half = width * math.sin(math.pi * max(0.0, (u + length * 0.15) / (length * 1.15)))
        if abs(v) > half:
            return None
        return v / max(0.01, half)

    def sample(x: float, y: float) -> RGBA | None:
        dx, dy = x - cx, y - cy
        d = math.hypot(dx, dy)
        ang = math.atan2(dy, dx)
        # Stein oben (Ritter und höher)
        if tier >= 2:
            gy = cy - r_ring - (4.2 if not small else 0.3)
            gr = 2.7 if not small else 1.6
            if math.hypot(dx, y - gy) <= gr:
                return _shade(GEMS[rank], 0.5 + (-dx - (y - gy)) / (2.6 * gr))
        # Krone: fünf Zacken auf einem Reif über dem Ring (nur groß)
        if tier >= 2 and not small:
            band_top = cy - r_ring - 1.6
            if band_top - 0.2 <= y <= cy - r_ring + 1.8 and abs(dx) <= 8.6:
                return _shade(METAL[rank], 0.7 if y < band_top + 1.2 else 0.42)
            spikes = ((-7.4, 4.4, 1.5), (-3.8, 6.0, 1.6), (0.0, 8.4, 1.9), (3.8, 6.0, 1.6), (7.4, 4.4, 1.5))
            if rank in ("titan", "immortal"):
                spikes = ((-8.6, 3.6, 1.2), (-5.8, 5.2, 1.3), (-2.9, 6.8, 1.4), (0.0, 8.8, 1.8), (2.9, 6.8, 1.4), (5.8, 5.2, 1.3),
                          (8.6, 3.6, 1.2))
            for (sx, hgt, w) in spikes:
                top = band_top - hgt
                if top <= y <= band_top and abs(dx - sx) <= w * (y - top) / hgt + 0.35:
                    return _shade(METAL[rank], 0.82 if dx < sx else 0.5)
        # Ring und Scheibe
        if d <= r_disk:
            return mix(DISK[0], DISK[1], 0.25 + 0.75 * (d / r_disk) ** 1.5)
        if d <= r_ring:
            light = 0.5 + 0.5 * math.cos(ang + math.radians(135))  # Licht von oben links
            inner = (d - r_disk) / (r_ring - r_disk)
            bevel = 1.0 - abs(inner - 0.45) * 2
            t = 0.22 + 0.62 * light * (0.5 + 0.5 * bevel)
            if inner < 0.18:
                t *= 0.7  # innere Kante dunkler
            if tier >= 3 and not small:
                for k in range(4):
                    a = math.radians(45 + 90 * k)
                    rr = (r_disk + r_ring) / 2
                    rx, ry = cx + math.cos(a) * rr, cy + math.sin(a) * rr
                    if math.hypot(x - rx, y - ry) <= 1.45:
                        return _shade(GEMS[rank], 0.8 if (x - rx) + (y - ry) < 0 else 0.38)
            if rank == "immortal":
                # der Ring des Unsterblichen schillert in allen Farben des Lichts
                return mix(_shade(METAL[rank], t), _prism(ang), 0.42)
            return _shade(METAL[rank], t)
        if d <= r_ring + 1.0:
            return INK
        if small:
            return None
        # Lorbeerzweige (Recke und höher): ein Stiel auf einem Bogen, Blätter paarweise nach außen
        if tier >= 1:
            lr = r_ring + 2.6
            for side in (-1, 1):
                # Stiel: von unten (90°) bis seitlich oben (210° links / -30° rechts)
                a_end = math.radians(212) if side < 0 else math.radians(-32)
                a0 = math.radians(98) if side < 0 else math.radians(82)
                a_here = ang if side < 0 else ang
                inside = (a0 <= (ang % (2 * math.pi)) <= a_end) if side < 0 else (a_end <= ang <= a0)
                if inside and abs(d - lr) <= 0.55:
                    return _shade(LAUREL[rank], 0.3)
                for k in range(6):
                    t = k / 5.0
                    a = a0 + (a_end - a0) * t if side < 0 else a0 + (a_end - a0) * t
                    bx, by = cx + math.cos(a) * lr, cy + math.sin(a) * lr
                    # Richtung des Zweigs (Tangente zum Ende hin) und die zwei Blätter schräg nach außen und innen
                    tang = a + (math.pi / 2 if side < 0 else -math.pi / 2)
                    for (off, ln) in ((-0.55, 4.6), (0.55, 3.8)):
                        la = tang + off * side * (1 if side < 0 else -1)
                        v = leaf(x, y, bx, by, la, ln - t * 0.8, 1.55 - t * 0.2)
                        if v is not None:
                            return _shade(LAUREL[rank], 0.78 if v < -0.15 else 0.52 if v < 0.35 else 0.3)
        # Schwingen (Kriegsherr, Legende): drei Federreihen, nach außen und oben gefegt
        if tier >= 4:
            ax = abs(dx)
            if ax >= r_ring - 1.0:
                tt = (ax - (r_ring - 1.0)) / (23.6 - (r_ring - 1.0))
                if 0.0 <= tt <= 1.0:
                    top_edge = -2.5 - tt * 12.5 + tt * tt * 2.0
                    for row, depth in ((0, 4.2), (1, 7.6), (2, 10.6)):
                        reach = 1.0 - row * 0.22
                        if tt > reach:
                            continue
                        # Federspitzen als Zacken an der Unterkante
                        teeth = 1.1 * abs(((tt * 7.0) % 1.0) - 0.5)
                        bottom = top_edge + depth * (1.0 - tt * 0.35) - teeth * 2.2
                        if top_edge <= dy <= bottom:
                            shade = (0.88, 0.62, 0.4)[row] - (dy - top_edge) / max(0.5, bottom - top_edge) * 0.18
                            if rank == "immortal" and tt > 0.55:
                                return mix(_shade(WINGS[rank], shade), _prism(tt * 9.0 + row), 0.45)
                            return _shade(WINGS[rank], shade)
        # Heiligenschein-Ring (Halbgott, Unsterblicher)
        if rank in HALO and abs(d - 17.4) <= 0.62:
            return mix(HALO[rank], hexc("C89A40"), 0.15 + 0.35 * (0.5 + 0.5 * math.sin(ang * 2.0)))
        # kleine Sterne (Mythos, Sternenwanderer): vier Zacken, oben schräg zwischen Krone und Schwingen
        if rank in ("myth", "starwalker", "immortal"):
            for (sa, sr, size) in ((-62.0, 20.6, 2.6), (-118.0, 20.6, 2.6), (-90.0, 22.4, 0.0)):
                if size <= 0.0:
                    continue
                sxp, syp = cx + math.cos(math.radians(sa)) * sr, cy + math.sin(math.radians(sa)) * sr
                ex, ey = abs(x - sxp), abs(y - syp)
                if (ex <= 0.55 and ey <= size) or (ey <= 0.55 and ex <= size) or (ex + ey <= 1.2):
                    return mix(hexc("FFFFFF"), RAYS[rank][4][1], 0.25)
        # Strahlenkranz (Legende und höher) hinter allem
        if tier >= 5:
            rays_n, long_len, short_len, width, colors = RAYS[rank]
            a = (ang + math.pi) / (2 * math.pi) * rays_n
            frac = abs(a - round(a))
            length = long_len if round(a) % 2 == 0 else short_len
            if r_ring < d <= length and frac <= width * (1 - (d - r_ring) / (length - r_ring)) + 0.03:
                col = _shade(colors, 0.85 - (d - r_ring) / (length - r_ring) * 0.55)
                if rank == "immortal":
                    col = mix(col, _prism(round(a) / rays_n * 2 * math.pi), 0.35)
                return col
        return None

    img = Image.new("RGBA", (size, size), CLEAR)
    px = img.load()
    n = 4
    for y in range(size):
        for x in range(size):
            hits = 0
            first = None
            for sy in range(n):
                for sx in range(n):
                    c = sample(x + (sx + 0.5) / n, y + (sy + 0.5) / n)
                    if c is not None:
                        hits += 1
                        first = first or c
            if hits * 2 >= n * n:
                px[x, y] = sample(x + 0.5, y + 0.5) or first
    # eine dunkle Kontur außen herum
    out = img.copy()
    opx = out.load()
    for y in range(size):
        for x in range(size):
            if px[x, y][3] == 0:
                for (nx, ny) in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
                    if 0 <= nx < size and 0 <= ny < size and px[nx, ny][3] > 0 and px[nx, ny] != INK:
                        opx[x, y] = INK
                        break
    return out


# =====================================================================================================================
# Licht für die Aufstiegs-Szene (vormultipliziert: Helligkeit in RGB)
# =====================================================================================================================

def light(size: int, fn) -> Image.Image:
    img = Image.new("RGBA", (size, size), CLEAR)
    px = img.load()
    c = (size - 1) / 2
    for y in range(size):
        for x in range(size):
            dx, dy = (x - c) / (size / 2), (y - c) / (size / 2)
            v = max(0.0, min(1.0, fn(dx, dy)))
            g = round(v * 255)
            px[x, y] = (g, g, g, 255) if g > 0 else CLEAR
    return img


def rays(dx: float, dy: float) -> float:
    r = math.hypot(dx, dy)
    if r > 1.0:
        return 0.0
    a = math.atan2(dy, dx)
    beam = (0.5 + 0.5 * math.cos(a * 12)) ** 5 + 0.55 * (0.5 + 0.5 * math.cos(a * 12 + math.pi)) ** 14
    fade = (1.0 - r) ** 1.4 * min(1.0, r / 0.18)
    return beam * fade * 1.15


def glow(dx: float, dy: float) -> float:
    r = math.hypot(dx, dy)
    return math.exp(-(r / 0.42) ** 2 * 1.6) * (1.0 if r < 1.0 else 0.0)


def ring(dx: float, dy: float) -> float:
    r = math.hypot(dx, dy)
    return math.exp(-((r - 0.84) / 0.06) ** 2) + 0.35 * math.exp(-((r - 0.84) / 0.16) ** 2)


def sparkle() -> Image.Image:
    size = 9
    img = Image.new("RGBA", (size, size), CLEAR)
    px = img.load()
    c = 4
    for y in range(size):
        for x in range(size):
            dx, dy = abs(x - c), abs(y - c)
            v = 0.0
            if dy == 0:
                v = max(v, 1.0 - dx / 4.6)
            if dx == 0:
                v = max(v, 1.0 - dy / 4.6)
            if dx == 1 and dy == 1:
                v = max(v, 0.42)
            if dx + dy == 1:
                v = max(v, 0.9)
            g = round(v * 255)
            if g > 0:
                px[x, y] = (g, g, g, 255)
    return img


# =====================================================================================================================
# Der Weg zur Legende: Landschaft, Wegsteine, Stufensteine, Banner, Mond, Wahrzeichen der Ränge
# =====================================================================================================================

def _periodic(x: float, terms: list[tuple[float, float, float]]) -> float:
    return sum(a * math.sin(2 * math.pi * x * f / 256.0 + ph) for (a, f, ph) in terms)


def mountains_far() -> Image.Image:
    """Ferne Berge (256 × 80, waagerecht kachelbar): Grate mit Mondlicht an der Kante und Schnee auf den Gipfeln."""
    w, h = 256, 80
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    body, rim, snow, deep = hexc("1E1A3A"), hexc("3A3366"), hexc("8E8AB8"), hexc("17142E")
    for x in range(w):
        ridge = 34 + _periodic(x, [(13, 2, 0.0), (8, 5, 1.0), (4, 11, 2.0), (2, 23, 0.4)])
        top = int(round(ridge))
        for y in range(max(0, top), h):
            depth = (y - top) / max(1.0, h - top)
            px[x, y] = mix(body, deep, depth)
        if 0 <= top < h:
            px[x, top] = rim
            if top < 26:   # Schnee auf den hohen Gipfeln
                for k in range(0, min(4, 27 - top)):
                    if top + k < h and (x + k) % 3 != 0:
                        px[x, top + k] = mix(snow, body, k / 4)
    return img


def hills_near() -> Image.Image:
    """Nahe Hügel (256 × 64, kachelbar) mit Tannen auf dem Kamm."""
    w, h = 256, 64
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    body, rim = hexc("120F24"), hexc("26203F")
    ridge = [26 + _periodic(x, [(7, 3, 0.3), (4, 7, 1.7), (2, 13, 0.9)]) for x in range(w)]
    for x in range(w):
        top = int(round(ridge[x]))
        for y in range(max(0, top), h):
            px[x, y] = body
        if 0 <= top < h:
            px[x, top] = rim
    # Tannen: alle paar Pixel eine, verschieden hoch
    for i, tx in enumerate(range(3, w, 11)):
        hgt = 9 + (i * 7) % 8
        base = int(round(ridge[tx])) + 1
        for dy in range(hgt):
            half = int((dy + 1) * 3.2 / hgt) + (1 if dy > hgt * 0.6 else 0)
            y = base - hgt + dy
            for dx in range(-half, half + 1):
                xx = (tx + dx) % w
                if 0 <= y < h:
                    px[xx, y] = rim if dx == -half and dy % 3 == 0 else body
        for y in range(base - 1, min(h, base + 2)):
            px[tx % w, y] = body
    return img


def foreground() -> Image.Image:
    """Vordergrund (256 × 56, kachelbar): Grasbüschel, Büsche und Felsen als dunkle Schattenrisse mit Mondlicht oben."""
    w, h = 256, 56
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    body, rim = hexc("06050C"), hexc("1E1A34")
    ground = [40 + _periodic(x, [(3, 2, 0.4), (2, 5, 1.1), (1, 13, 2.2)]) for x in range(w)]
    for x in range(w):
        for y in range(int(ground[x]), h):
            px[x, y] = body
    # Felsen
    for (cx, rx, ry) in ((40, 14, 9), (150, 10, 7), (210, 18, 11)):
        for y in range(h):
            for x in range(cx - rx - 1, cx + rx + 2):
                dx, dy = (x - cx) / rx, (y - (ground[x % w] - ry * 0.4)) / ry
                if dx * dx + dy * dy <= 1.0:
                    px[x % w, y] = body
    # Büsche: Gruppen kleiner Kreise
    for (cx, r) in ((95, 9), (108, 7), (182, 8), (240, 6)):
        for y in range(h):
            for x in range(cx - r - 1, cx + r + 2):
                if (x - cx) ** 2 + (y - (ground[x % w] - r * 0.6)) ** 2 <= r * r:
                    px[x % w, y] = body
    # Grasbüschel: dünne, schräge Halme
    for i, gx in enumerate(range(2, w, 5)):
        base = int(ground[gx])
        for blade in range(3):
            lean = (blade - 1) * 0.35 + ((i * 7) % 5 - 2) * 0.08
            height = 5 + (i * 13 + blade * 5) % 8
            for k in range(height):
                x = int(round(gx + blade + lean * k)) % w
                y = base - k
                if 0 <= y < h:
                    px[x, y] = body
    # Mondlicht an den oberen Kanten
    out = img.copy()
    opx = out.load()
    for y in range(1, h):
        for x in range(w):
            if px[x, y] == body and px[x, y - 1][3] == 0:
                opx[x, y] = rim
    return out


def stone(lit: bool) -> Image.Image:
    """Ein Wegstein (6 × 5): grau oder golden leuchtend."""
    img = Image.new("RGBA", (6, 5), CLEAR)
    px = img.load()
    if lit:
        hi, mid, dk = hexc("FFF0B8"), hexc("E8B84A"), hexc("8A5E1C")
    else:
        hi, mid, dk = hexc("7A7290"), hexc("4E4762"), hexc("2A253A")
    shape = [".xxxx.", "xxxxxx", "xxxxxx", "xxxxxx", ".xxxx."]
    for y, row in enumerate(shape):
        for x, c in enumerate(row):
            if c == "x":
                px[x, y] = hi if (y == 0 or (y == 1 and x < 3)) else dk if y == 4 or x == 5 else mid
    return img


def node(kind: str) -> Image.Image:
    """Stufenstein: klein (13) oder groß (19, für jede zehnte Stufe); grau, golden oder hell (die eigene Stufe)."""
    big = kind.startswith("big")
    size = 19 if big else 13
    state = kind.split("_")[-1] if "_" in kind else "locked"
    img = Image.new("RGBA", (size, size), CLEAR)
    px = img.load()
    c = (size - 1) / 2
    r_out = size / 2 - 0.6
    r_in = r_out - (3.6 if big else 2.6)
    if state == "done":
        ring, center = (hexc("FFF0B0"), hexc("E2B24A"), hexc("7A5216")), (hexc("FFE7A0"), hexc("C8902E"), hexc("6E4A1A"))
    elif state == "current":
        ring, center = (hexc("FFFFFF"), hexc("FFE58A"), hexc("B8862A")), (hexc("FFFFFF"), hexc("FFF2C0"), hexc("E8B84A"))
    else:
        ring, center = (hexc("8A8299"), hexc("5A5370"), hexc("2E2840")), (hexc("2A2438"), hexc("1E1A2B"), hexc("15121F"))
    for y in range(size):
        for x in range(size):
            dx, dy = x - c, y - c
            d = math.hypot(dx, dy)
            light = 0.5 + 0.5 * (-dx - dy) / max(0.01, d) * 0.7 if d > 0 else 0.5
            if d <= r_in:
                t = 0.5 + 0.5 * (-dx - dy) / (2 * r_in)
                px[x, y] = _shade(center, t if state != "locked" else 0.25 + t * 0.3)
            elif d <= r_out:
                px[x, y] = _shade(ring, light)
            elif d <= r_out + 1.0:
                px[x, y] = INK
    return img


def banner() -> Image.Image:
    """Ein Band unter den Rangsteinen (64 × 14, Neun-Teile): Purpur mit Goldsaum, die Enden gegabelt."""
    w, h = 64, 14
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    for y in range(h):
        for x in range(w):
            # gegabelte Enden: an den Rändern eine Kerbe in der Mitte
            edge = min(x, w - 1 - x)
            if edge < 4 and abs(y - (h - 1) / 2) < (4 - edge) * 0.9:
                continue
            if y in (0, h - 1):
                px[x, y] = INK
            elif y in (1, h - 2):
                px[x, y] = GOLD if y == 1 else GOLD_MID
            else:
                px[x, y] = mix(hexc("5A2A86"), hexc("3A1660"), (y - 2) / (h - 4))
    return img


def moon() -> Image.Image:
    size = 40
    img = Image.new("RGBA", (size, size), CLEAR)
    px = img.load()
    c = (size - 1) / 2
    craters = [(14, 15, 3.2), (24, 22, 2.4), (18, 27, 1.8), (27, 13, 1.6), (11, 24, 1.4)]
    for y in range(size):
        for x in range(size):
            d = math.hypot(x - c, y - c)
            if d <= 15.5:
                t = 0.62 + 0.38 * (-(x - c) - (y - c)) / 31.0
                col = mix(hexc("9E9AB8"), hexc("F4F0FF"), t)
                for (cx, cy, r) in craters:
                    if math.hypot(x - cx, y - cy) <= r:
                        col = mix(col, hexc("6E6A8E"), 0.35)
                px[x, y] = col
            elif d <= 16.4:
                px[x, y] = mix(hexc("C8C4E0"), CLEAR, 0.3)
    return img


def landmark(rank: str) -> Image.Image:
    """
    Das Wahrzeichen eines Rangs als Schattenriss vor dem Nachthimmel (96 × 72): Dorf (Knappe), Wachturm mit Palisade
    (Recke), Burg (Ritter), Schrein im Wald (Held), Festung auf dem Felsen (Kriegsherr), Gipfel mit Kronenburg und Drache
    (Legende). Fenster leuchten warm, Fahnen in der Farbe des Rangs, die Kanten fängt das Mondlicht.
    """
    w, h = 96, 72
    img = Image.new("RGBA", (w, h), CLEAR)
    px = img.load()
    dark = hexc("17132C")
    flag = {"squire": hexc("D08C52"), "warrior": hexc("D9E1EA"), "knight": hexc("F2C14E"), "hero": hexc("52D98F"),
            "warlord": hexc("6EE2F5"), "legend": hexc("FF9A3C"), "myth": hexc("C08CFF"), "demigod": hexc("FFE9A6"),
            "titan": hexc("FF6B4A"), "starwalker": hexc("7FB8FF"), "immortal": hexc("FFF4E0")}[rank]
    win = hexc("FFD27A")
    lit: list[tuple[int, int]] = []
    glows: list[tuple[int, int, tuple]] = []

    def glow(x, y, col):
        glows.append((int(x), int(y), col))

    def disc(cx0, cy0, rx, ry):
        for y in range(int(cy0 - ry) - 1, int(cy0 + ry) + 2):
            for x in range(int(cx0 - rx) - 1, int(cx0 + rx) + 2):
                if 0 <= x < w and 0 <= y < h and ((x + 0.5 - cx0) / rx) ** 2 + ((y + 0.5 - cy0) / ry) ** 2 <= 1.0:
                    px[x, y] = dark

    def rect(x0, y0, x1, y1):
        for y in range(max(0, y0), min(h, y1)):
            for x in range(max(0, x0), min(w, x1)):
                px[x, y] = dark

    def tri(x0, x1, top, base):
        mid = (x0 + x1) / 2
        for y in range(top, base):
            t = (y - top + 1) / max(1, base - top)
            half = (x1 - x0) / 2 * t
            for x in range(int(mid - half), int(mid + half) + 1):
                if 0 <= x < w and 0 <= y < h:
                    px[x, y] = dark

    def battlements(x0, x1, y):
        for x in range(x0, x1, 3):
            rect(x, y - 2, x + 2, y)

    def flagpole(x, top, length=7):
        for y in range(top, top + length):
            if 0 <= y < h:
                px[x, y] = dark
        for k in range(4):
            for j in range(3 - k // 2):
                if 0 <= x + 1 + k < w and 0 <= top + j < h:
                    px[x + 1 + k, top + j] = flag

    def window(x, y, tall=2):
        for k in range(tall):
            lit.append((x, y + k))

    ground = h - 4
    rect(0, ground, w, h)
    if rank == "squire":
        for (x0, wd, ht) in ((14, 16, 10), (36, 20, 13), (62, 15, 9)):
            rect(x0, ground - ht, x0 + wd, ground)
            tri(x0 - 3, x0 + wd + 2, ground - ht - 8, ground - ht + 1)
            window(x0 + wd // 2 - 1, ground - ht + 4)
            window(x0 + wd // 2 + 2, ground - ht + 4)
        rect(48, ground - 25, 51, ground - 18)   # Schornstein
        flagpole(78, ground - 20, 20)
    elif rank == "warrior":
        for x in range(4, w - 4, 3):
            rect(x, ground - 8 - (x * 7 % 3), x + 2, ground)
            px[x, ground - 9 - (x * 7 % 3)] = dark
        rect(40, ground - 40, 56, ground)
        rect(36, ground - 46, 60, ground - 40)
        battlements(36, 60, ground - 46)
        window(46, ground - 30, 3)
        window(47, ground - 20, 3)
        flagpole(48, ground - 58, 12)
    elif rank == "knight":
        rect(20, ground - 22, 76, ground)
        battlements(20, 76, ground - 22)
        for tx in (14, 74):
            rect(tx, ground - 34, tx + 9, ground)
            tri(tx - 2, tx + 10, ground - 44, ground - 33)
            window(tx + 4, ground - 26, 3)
        rect(40, ground - 44, 56, ground - 22)
        battlements(40, 56, ground - 44)
        window(44, ground - 36, 3)
        window(50, ground - 36, 3)
        rect(45, ground - 9, 51, ground)
        for x in range(45, 51):
            lit.append((x, ground - 9))
        flagpole(47, ground - 56, 12)
        flagpole(18, ground - 54, 10)
        flagpole(78, ground - 54, 10)
    elif rank == "hero":
        for i, tx in enumerate(range(6, w, 13)):
            ht = 22 + (i * 11) % 14
            tri(tx - 6, tx + 6, ground - ht, ground)
            rect(tx - 1, ground - 3, tx + 1, ground)
        rect(36, ground - 26, 41, ground)
        rect(55, ground - 26, 60, ground)
        rect(34, ground - 31, 62, ground - 26)
        for y in range(ground - 24, ground):
            for x in range(41, 55):
                px[x, y] = CLEAR if (y - (ground - 24)) > 2 else dark
        for k in range(3):
            lit.append((47 + k, ground - 18))
            lit.append((47 + k, ground - 17))
        win = flag
    elif rank == "warlord":
        for y in range(ground - 20, ground):
            t = (y - (ground - 20)) / 20
            half = 30 + 18 * t
            for x in range(int(48 - half), int(48 + half)):
                if 0 <= x < w:
                    px[x, y] = dark
        rect(22, ground - 40, 74, ground - 20)
        battlements(22, 74, ground - 40)
        for tx in (16, 44, 72):
            rect(tx, ground - 54 + (6 if tx != 44 else 0), tx + 9, ground - 20)
            battlements(tx, tx + 9, ground - 54 + (6 if tx != 44 else 0))
            window(tx + 4, ground - 44 + (6 if tx != 44 else 0), 3)
        for x in range(28, 70, 7):
            window(x, ground - 32)
        flagpole(48, ground - 66, 12)
        flagpole(20, ground - 58, 10)
        flagpole(76, ground - 58, 10)
    elif rank == "myth":
        # ein Tempel aus alter Zeit, Säulen zum Teil gebrochen – über ihm schwebt ein leuchtender Kristall
        rect(18, ground - 3, 78, ground)
        rect(21, ground - 6, 75, ground - 3)
        rect(24, ground - 8, 72, ground - 6)
        cols = ((27, 26), (36, 26), (45, 26), (54, 18), (63, 11))
        for (x0, ht) in cols:
            rect(x0, ground - 8 - ht, x0 + 4, ground - 8)
            rect(x0 - 1, ground - 8 - ht, x0 + 5, ground - 7 - ht)
        rect(24, ground - 37, 58, ground - 34)                  # Gebälk (nur über den heilen Säulen)
        for y in range(ground - 46, ground - 37):              # Giebel, zur Hälfte
            t = (y - (ground - 46)) / 9
            for x in range(int(41 - 17 * t), int(41 + 17 * t) + 1):
                if x < 58:
                    px[x, y] = dark
        for (x, y) in ((70, ground - 3), (72, ground - 4), (75, ground - 2)):    # Trümmer
            rect(x, y - 2, x + 3, y + 1)
        kx, ky = 48, 12                                          # der Kristall
        for y in range(ky - 8, ky + 9):
            half = (8 - abs(y - ky)) * 0.55
            for x in range(int(kx - half), int(kx + half) + 1):
                glow(x, y, mix(hexc("F6E8FF"), flag, abs(x - kx) / max(1.0, half + 0.5)))
        for k in range(1, 4):
            glow(kx, ky + 9 + k * 3, mix(flag, hexc("17132C"), 0.4))
    elif rank == "demigod":
        # ein Palast auf einer Wolkenbank – Kuppel, zwei schlanke Türme, ein leuchtendes Tor
        for (cx0, cy0, rx, ry) in ((16, ground - 6, 14, 6), (34, ground - 9, 16, 8), (56, ground - 8, 18, 7), (78, ground - 6, 15, 6),
                                   (46, ground - 3, 40, 4)):
            disc(cx0, cy0, rx, ry)
        rect(30, ground - 34, 66, ground - 14)
        disc(48, ground - 34, 12, 11)
        rect(47, ground - 50, 49, ground - 44)
        for tx in (24, 68):
            rect(tx, ground - 44, tx + 5, ground - 14)
            tri(tx - 1, tx + 6, ground - 54, ground - 43)
            window(tx + 2, ground - 36, 3)
        for y in range(ground - 26, ground - 14):              # Tor
            for x in range(44, 53):
                if y > ground - 24 or abs(x - 48) <= 2:
                    glow(x, y, mix(hexc("FFFFFF"), flag, (y - (ground - 26)) / 12))
        for x in range(34, 64, 6):
            window(x, ground - 30)
        flagpole(48, ground - 58, 8)
    elif rank == "titan":
        # ein Koloss auf einer Klippe, das Schwert vor sich auf den Fels gestützt, die Augen glühen
        for y in range(ground - 10, ground):
            t = (y - (ground - 10)) / 10
            for x in range(int(30 - 16 * t), int(66 + 18 * t)):
                if 0 <= x < w:
                    px[x, y] = dark
        rect(38, ground - 30, 45, ground - 10)               # Beine
        rect(51, ground - 30, 58, ground - 10)
        rect(36, ground - 50, 60, ground - 29)               # Rumpf
        rect(32, ground - 52, 64, ground - 46)               # Schultern
        disc(48, ground - 57, 4.6, 5.2)                      # Kopf
        tri(43, 54, ground - 66, ground - 60)                # Helmkamm
        rect(28, ground - 50, 33, ground - 34)               # Arme hinab zum Schwert
        rect(63, ground - 50, 68, ground - 34)
        rect(46, ground - 40, 50, ground - 10)               # die Klinge vor ihm
        rect(40, ground - 42, 56, ground - 40)               # Parierstange
        for (ex, ey) in ((46, ground - 58), (50, ground - 58)):
            glow(ex, ey, hexc("FFD0A0"))
            glow(ex, ey + 1, flag)
    elif rank == "starwalker":
        # eine Sternwarte: Turm mit Kuppel, Spalt und Fernrohr; ein Strahl aus Sternenlicht steigt auf
        rect(10, ground - 6, 86, ground)
        rect(38, ground - 40, 58, ground - 6)
        disc(48, ground - 40, 12, 10)
        rect(30, ground - 18, 38, ground - 6)
        rect(58, ground - 22, 70, ground - 6)
        tri(57, 71, ground - 30, ground - 21)
        for k in range(14):                                   # Fernrohr schräg nach rechts oben
            for j in range(3):
                x, y = 52 + k, ground - 46 - k + j
                if 0 <= x < w and 0 <= y < h:
                    px[x, y] = dark
        for k in range(1, 18):                                # der Sternenstrahl
            glow(66 + k, ground - 61 - k, mix(hexc("FFFFFF"), flag, k / 18))
        for (x, y) in ((80, 8), (88, 16), (72, 6), (90, 4), (14, 10), (24, 4)):
            glow(x, y, hexc("FFFFFF"))
        window(46, ground - 30, 3)
        window(50, ground - 20, 3)
        window(63, ground - 14)
    elif rank == "immortal":
        # die Himmelszitadelle auf einem schwebenden Fels, hinter der höchsten Spitze ein Ring aus Licht
        for a in range(0, 360, 3):                            # der Lichtring
            for rr in (19.0, 19.6):
                x = 48 + math.cos(math.radians(a)) * rr
                y = 24 + math.sin(math.radians(a)) * rr * 0.92
                glow(x, y, mix(hexc("FFFFFF"), flag, 0.3 + 0.3 * math.sin(math.radians(a * 2))))
        for y in range(ground - 14, ground + 3):              # der schwebende Fels (darunter Luft)
            t = (y - (ground - 14)) / 17
            half = 34 * (1 - t ** 1.6)
            for x in range(int(48 - half), int(48 + half) + 1):
                if 0 <= x < w and 0 <= y < h:
                    px[x, y] = dark
        for y in range(ground, h):
            for x in range(w):
                if px[x, y] == dark and abs(x - 48) > 12 - (y - ground) * 3:
                    px[x, y] = CLEAR
        rect(42, ground - 50, 54, ground - 14)                # die hohe Spitze
        tri(40, 56, ground - 64, ground - 49)
        for (tx, ht) in ((24, 26), (32, 34), (62, 34), (70, 26)):
            rect(tx, ground - 14 - ht, tx + 6, ground - 14)
            tri(tx - 1, tx + 7, ground - 22 - ht, ground - 13 - ht)
            window(tx + 3, ground - ht, 3)
        rect(30, ground - 22, 66, ground - 14)
        battlements(30, 66, ground - 22)
        for k in range(3):
            window(46 + k * 2, ground - 40, 4)
        for k in range(1, 9):
            glow(48, ground - 64 - k, mix(hexc("FFFFFF"), flag, k / 9))
    else:
        tri(4, 92, 6, ground)
        rect(34, 8, 62, 20)
        for k in range(5):
            rect(34 + k * 6, 2 + (4 if k % 2 else 0), 38 + k * 6, 10)
        window(41, 13)
        window(47, 12, 3)
        window(53, 13)
        # ein Drache im Flug, links oben
        for (x, y) in ((8, 14), (9, 13), (10, 13), (11, 14), (12, 14), (13, 13), (14, 12), (15, 12), (16, 13), (12, 15), (13, 15),
                       (6, 12), (5, 11), (17, 11), (18, 10)):
            px[x, y] = dark
        flagpole(48, -4, 8)
    # Mondlicht an den oberen Kanten
    out = img.copy()
    opx = out.load()
    for y in range(1, h):
        for x in range(w):
            if px[x, y] == dark and px[x, y - 1][3] == 0:
                opx[x, y] = hexc("2E2752")
    for (x, y) in lit:
        if 0 <= x < w and 0 <= y < h:
            opx[x, y] = win
    for (x, y, col) in glows:
        if 0 <= x < w and 0 <= y < h:
            opx[x, y] = col
    return out


# =====================================================================================================================

def main() -> None:
    out = Path(sys.argv[1]) if len(sys.argv) > 1 else ASSETS
    sprites = out / "textures/gui/sprites/hero"
    save(panel(), sprites / "panel.png", (64, 64, 10))
    save(inset(), sprites / "inset.png", (16, 16, 3))
    save(card(False), sprites / "card.png", (32, 20, 3))
    save(card(True), sprites / "card_hover.png", (32, 20, 3))
    for state, name in (("normal", "button"), ("highlighted", "button_highlighted"), ("disabled", "button_disabled")):
        save(button(state), sprites / f"{name}.png", (200, 20, 4))
    for state, name in (("normal", "plus"), ("highlighted", "plus_highlighted"), ("disabled", "plus_disabled")):
        save(plus(state), sprites / f"{name}.png")
    for rank in RANKS:
        save(emblem(rank, 48), sprites / "emblem" / f"{rank}.png")
        save(emblem(rank, 15), sprites / "badge" / f"{rank}.png")
    save(light(64, rays), sprites / "rays.png")
    save(light(32, glow), sprites / "glow.png")
    save(light(64, ring), sprites / "ring.png")
    save(sparkle(), sprites / "sparkle.png")
    path = sprites / "path"
    save(mountains_far(), path / "ridge.png")
    # Platzhalter: Das höchste Bild im Bilderatlas der Oberfläche fehlt im Spiel (es bleibt das Ersatzbild) – dieses leere
    # Bild ist höher als alle anderen und nimmt diesen Platz ein, damit die Berge erscheinen
    save(Image.new("RGBA", (8, 160), CLEAR), path / "atlas_spacer.png")
    save(hills_near(), path / "hills_near.png")
    save(foreground(), path / "foreground.png")
    save(stone(False), path / "stone.png")
    save(stone(True), path / "stone_lit.png")
    for kind in ("small_locked", "small_done", "small_current", "big_locked", "big_done", "big_current"):
        save(node(kind), path / f"node_{kind}.png")
    save(banner(), path / "banner.png", (64, 14, 7, 4, 7, 4))
    save(moon(), path / "moon.png")
    for rank in RANKS:
        save(landmark(rank), path / f"landmark_{rank}.png")
    print("Grafiken der Heldenstufe geschrieben nach", out)


if __name__ == "__main__":
    main()
