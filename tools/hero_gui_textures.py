#!/usr/bin/env python3
"""
Grafiken der Heldenstufe (eigene Pixelkunst, alles hier gezeichnet):

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
  particle/pat_hand_0..4            die Hand, die beim Streicheln den Kopf tätschelt (fünf Bilder)

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


def save(img: Image.Image, path: Path, nine: tuple[int, int, int] | None = None) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path)
    meta = path.with_name(path.name + ".mcmeta")
    if nine is not None:
        w, h, border = nine
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
    # eine feine Goldlinie innen, mit Abstand zum Rahmen
    for k in range(border + 2, size - border - 2):
        px[k, border + 1] = mix(BG, GOLD_DK, 0.55)
        px[border + 1, k] = mix(BG, GOLD_DK, 0.55)
        px[k, size - border - 2] = mix(BG, GOLD_DK, 0.35)
        px[size - border - 2, k] = mix(BG, GOLD_DK, 0.35)
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

RANKS = ("squire", "warrior", "knight", "hero", "warlord", "legend")
# Metall des Rings: hell, mittel, dunkel
METAL = {
    "squire": (hexc("F2B47C"), hexc("C07A42"), hexc("6A3A18")),
    "warrior": (hexc("FFFFFF"), hexc("B9C3D0"), hexc("5E6878")),
    "knight": (hexc("FFF0B0"), hexc("E4B23E"), hexc("7A5216")),
    "hero": (hexc("FFF0B0"), hexc("E4B23E"), hexc("7A5216")),
    "warlord": (hexc("FFFFFF"), hexc("C8E8F2"), hexc("5A7E92")),
    "legend": (hexc("FFF6C8"), hexc("FFB43A"), hexc("A03A10")),
}
GEMS = {
    "knight": RUBY,
    "hero": (hexc("B8FFD8"), hexc("2FC46E"), hexc("0E5A30")),
    "warlord": (hexc("F2FFFF"), hexc("5FE0F2"), hexc("1A6E8A")),
    "legend": (hexc("FFD0E0"), hexc("FF3A5A"), hexc("7A0A24")),
}
LAUREL = {
    "warrior": (hexc("E6EBF2"), hexc("9AA6B6"), hexc("4E5868")),
    "knight": (hexc("FFE58A"), hexc("D49A2E"), hexc("6E4614")),
    "hero": (hexc("A8F0A0"), hexc("3EA84A"), hexc("1A5420")),
    "warlord": (hexc("FFE58A"), hexc("D49A2E"), hexc("6E4614")),
    "legend": (hexc("FFE58A"), hexc("E8A02E"), hexc("7A3E10")),
}
WINGS = {
    "warlord": (hexc("FFFFFF"), hexc("D2E2EE"), hexc("7890A8")),
    "legend": (hexc("FFF2C0"), hexc("FFB447"), hexc("B04A12")),
}
DISK = (hexc("2E2840"), hexc("120F1A"))


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
            for (sx, hgt, w) in ((-7.4, 4.4, 1.5), (-3.8, 6.0, 1.6), (0.0, 8.4, 1.9), (3.8, 6.0, 1.6), (7.4, 4.4, 1.5)):
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
                            return _shade(WINGS[rank], shade)
        # Strahlenkranz (Legende) hinter allem
        if tier >= 5:
            rays_n = 16
            a = (ang + math.pi) / (2 * math.pi) * rays_n
            frac = abs(a - round(a))
            length = 23.4 if round(a) % 2 == 0 else 20.0
            if r_ring < d <= length and frac <= 0.26 * (1 - (d - r_ring) / (length - r_ring)) + 0.03:
                return _shade((hexc("FFF6C8"), hexc("FFC24A"), hexc("C05A10")), 0.85 - (d - r_ring) / (length - r_ring) * 0.55)
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
# Die Hand fürs Streicheln
# =====================================================================================================================

SKIN = (hexc("FFE0BC"), hexc("EDB98A"), hexc("CD8E5E"), hexc("8E5634"))
SLEEVE = (hexc("9A66D6"), hexc("6A36A2"), hexc("40186C"))
CUFF = (hexc("FFE7A0"), hexc("E2B24A"), hexc("8A5E1C"))
NAIL = hexc("FBE3D6")


def _capsule(px: float, py: float, ax: float, ay: float, bx: float, by: float, r: float) -> float:
    vx, vy = bx - ax, by - ay
    t = max(0.0, min(1.0, ((px - ax) * vx + (py - ay) * vy) / (vx * vx + vy * vy)))
    return math.hypot(px - (ax + vx * t), py - (ay + vy * t)) - r


def _ellipse(px: float, py: float, cx: float, cy: float, rx: float, ry: float) -> float:
    return (math.hypot((px - cx) / rx, (py - cy) / ry) - 1.0) * min(rx, ry)


def _round_rect(px: float, py: float, cx: float, cy: float, hx: float, hy: float, r: float) -> float:
    qx, qy = abs(px - cx) - hx + r, abs(py - cy) - hy + r
    return math.hypot(max(qx, 0.0), max(qy, 0.0)) + min(max(qx, qy), 0.0) - r


def hand(frame: int) -> Image.Image:
    """
    Die Hand von der Seite, die Finger nach rechts: Bild 0 erhoben, 1 im Herabkommen, 2 liegt auf, 3 drückt (flacher, die
    Finger biegen sich über den Kopf), 4 hebt sich wieder. Links schaut der Ärmel heraus (Purpur mit Goldsaum – die
    Legende). Gezeichnet aus Abstandsfeldern: Handballen, drei Fingerglieder, Daumen; Licht von oben links.
    """
    size = 32
    lift = (-5.5, -2.5, 0.0, 1.0, -2.0)[frame]
    squash = (1.0, 0.98, 0.93, 0.84, 0.95)[frame]
    curl = (-0.6, 0.2, 0.9, 1.8, 0.8)[frame]
    base = 20.0 + lift

    def parts(x: float, y: float) -> dict[str, float]:
        palm = min(_ellipse(x, y, 13.6, base - 4.3 * squash, 6.6, 4.7 * squash),
                   _capsule(x, y, 8.6, base - 4.4 * squash, 15.0, base - 3.6 * squash, 4.1 * squash))
        p0 = (17.6, base - 4.4 * squash)
        p1 = (23.4, base - 3.7 + curl * 0.7)
        p2 = (28.4, base - 1.6 + curl * 1.9)
        fingers = min(_capsule(x, y, *p0, *p1, 3.0 * (0.94 + 0.06 * squash)), _capsule(x, y, *p1, *p2, 2.35))
        thumb = _capsule(x, y, 11.2, base - 0.9, 19.4, base + 0.6 + curl * 0.35, 1.6)
        sleeve = _round_rect(x, y, 4.6, base - 4.6, 3.9, 6.2, 1.6)
        return {"palm": palm, "fingers": fingers, "thumb": thumb, "sleeve": sleeve}

    def union(x: float, y: float) -> float:
        return min(parts(x, y).values())

    light = (-0.42, -0.91)
    img = Image.new("RGBA", (size, size), CLEAR)
    px = img.load()
    for y in range(size):
        for x in range(size):
            cx, cy = x + 0.5, y + 0.5
            hits = sum(1 for sy in range(4) for sx in range(4) if union(x + (sx + 0.5) / 4, y + (sy + 0.5) / 4) <= 0.0)
            if hits < 8:
                continue
            ps = parts(cx, cy)
            part = min(ps, key=ps.get)
            if ps["thumb"] <= 0.0:
                part = "thumb"
            if ps["sleeve"] <= 0.0:
                part = "sleeve"
            d = ps[part]
            e = 0.25
            gx = (parts(cx + e, cy)[part] - parts(cx - e, cy)[part]) / (2 * e)
            gy = (parts(cx, cy + e)[part] - parts(cx, cy - e)[part]) / (2 * e)
            gl = math.hypot(gx, gy) or 1.0
            lit = (gx * light[0] + gy * light[1]) / gl          # 1: die Kante zeigt zum Licht
            rim = max(0.0, 1.0 - (-d) / 1.6)                    # 1 am Rand, 0 innen
            if part == "sleeve":
                if cx >= 6.8:
                    tone = CUFF[0] if lit > 0.3 and rim > 0.3 else CUFF[2] if lit < -0.3 and rim > 0.3 else CUFF[1]
                else:
                    tone = SLEEVE[0] if lit > 0.3 and rim > 0.3 else SLEEVE[2] if lit < -0.25 and rim > 0.3 else SLEEVE[1]
                px[x, y] = tone
                continue
            v = lit * rim
            if part == "thumb":
                v += 0.15
            tone = SKIN[0] if v > 0.38 else SKIN[1] if v > -0.22 else SKIN[2]
            # Fugen zwischen den Fingern (zwei feine Linien längs) und die Knöchel-Falte
            if part == "fingers" and cx > 19.5 and cx < 27.5 and -d > 0.9:
                p1y = base - 3.7 + curl * 0.7
                rel = (cy - (p1y + (cx - 23.4) * 0.25 * (1 + curl * 0.3)))
                if abs(rel + 0.6) < 0.5 and int(cx) % 3 != 0:
                    tone = mix(SKIN[1], SKIN[2], 0.75)
            if part == "palm" and abs(ps["fingers"]) < 0.55 and cy < base - 3.0:
                tone = mix(SKIN[1], SKIN[2], 0.6)
            if part == "palm" and abs(ps["thumb"]) < 0.6 and cy < base:
                tone = SKIN[2]
            px[x, y] = tone
    # Fingernagel an der Spitze (oben), ein Glanzpunkt auf dem Handrücken
    tip_x = 28.4 + 2.35 * 0.7
    tip_y = base - 1.6 + curl * 1.9 - 1.2
    for (nx, ny) in ((tip_x - 1.0, tip_y), (tip_x - 2.0, tip_y - 0.4)):
        ix, iy = int(nx), int(ny)
        if 0 <= ix < size and 0 <= iy < size and px[ix, iy][3] > 0:
            px[ix, iy] = NAIL
    hx, hy = 12, int(base - 4.3 * squash - 3.4 * squash)
    if px[hx, hy][3] > 0:
        px[hx, hy] = mix(SKIN[0], (255, 255, 255, 255), 0.5)
        px[hx + 1, hy] = mix(SKIN[0], (255, 255, 255, 255), 0.3)
    # Kontur
    out = img.copy()
    opx = out.load()
    for y in range(size):
        for x in range(size):
            if px[x, y][3] == 0:
                for (nx, ny) in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
                    if 0 <= nx < size and 0 <= ny < size and px[nx, ny][3] > 0:
                        opx[x, y] = hexc("24103C") if px[nx, ny] in SLEEVE + CUFF else SKIN[3]
                        break
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
    particles = out / "textures/particle"
    particles.mkdir(parents=True, exist_ok=True)
    for frame in range(5):
        hand(frame).save(particles / f"pat_hand_{frame}.png")
    defs = out / "particles"
    defs.mkdir(parents=True, exist_ok=True)
    (defs / "pat_hand.json").write_text(json.dumps({"textures": [f"kingdomsatwar:pat_hand_{i}" for i in range(5)]}, indent=2) + "\n")
    print("Grafiken der Heldenstufe geschrieben nach", out)


if __name__ == "__main__":
    main()
