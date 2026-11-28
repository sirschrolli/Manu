#!/usr/bin/env python3
"""
Die Wappenzeichen der vier Reiche als Bannermuster (wie die Vanilla-Muster: weiß mit Alpha, das Spiel färbt sie ein):

  * entity/banner/tower.png, entity/shield/tower.png – Eisenmark: ein Burgturm mit Zinnen, Fenster und Tor
  * entity/banner/raven.png, entity/shield/raven.png – Rabenstein: ein schwarzer Rabe im Profil
  * entity/banner/sun.png,   entity/shield/sun.png   – Dornfels: eine Sonne mit Gesicht und acht Strahlen
  * entity/banner/swan.png,  entity/shield/swan.png  – Silberau: ein Schwan mit gebogenem Hals über Wellen

Gezeichnet wird mit Flächen und Linien in sechzehnfacher Auflösung, dann auf das Pixelraster gebracht (Bannerfront 20 × 40,
Schildfläche 10 × 20). Innere Linien (Mauerfugen, Federn, Gesicht) sind etwas dunkler, so wirken die Zeichen plastisch.
Die Rückseite des Banners ist wie bei Vanilla die gespiegelte Vorderseite, etwas dunkler.

Aufruf: python3 tools/realm_banners.py [--preview DATEI]
"""
from __future__ import annotations

import math
import sys
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "src" / "main" / "resources" / "assets" / "kingdomsatwar" / "textures" / "entity"
SS = 16          # Überabtastung
W, H = 20, 40    # Bannerfront

FULL = 255
DETAIL = 196


class Canvas:
    """Zeichenfläche in Bannerpixeln (x 0..20, y 0..40), intern sechzehnfach."""

    def __init__(self) -> None:
        self.body = Image.new("L", (W * SS, H * SS), 0)
        self.detail = Image.new("L", (W * SS, H * SS), 0)
        self.holes = Image.new("L", (W * SS, H * SS), 0)
        self.d_body = ImageDraw.Draw(self.body)
        self.d_detail = ImageDraw.Draw(self.detail)
        self.d_holes = ImageDraw.Draw(self.holes)

    @staticmethod
    def _p(pts):
        return [(x * SS, y * SS) for x, y in pts]

    def poly(self, pts, layer: str = "body") -> None:
        self._draw(layer).polygon(self._p(pts), fill=255)

    def rect(self, x0, y0, x1, y1, layer: str = "body") -> None:
        self._draw(layer).rectangle((x0 * SS, y0 * SS, x1 * SS - 1, y1 * SS - 1), fill=255)

    def ellipse(self, cx, cy, rx, ry, layer: str = "body", angle: float = 0.0) -> None:
        if angle == 0.0:
            self._draw(layer).ellipse(((cx - rx) * SS, (cy - ry) * SS, (cx + rx) * SS, (cy + ry) * SS), fill=255)
            return
        a = math.radians(angle)
        pts = []
        for i in range(72):
            t = i / 72 * math.tau
            x, y = rx * math.cos(t), ry * math.sin(t)
            pts.append((cx + x * math.cos(a) - y * math.sin(a), cy + x * math.sin(a) + y * math.cos(a)))
        self.poly(pts, layer)

    def line(self, pts, width: float, layer: str = "body") -> None:
        d = self._draw(layer)
        pp = self._p(pts)
        d.line(pp, fill=255, width=max(1, round(width * SS)))
        r = width * SS / 2
        for x, y in pp:
            d.ellipse((x - r, y - r, x + r, y + r), fill=255)

    def curve(self, p0, p1, p2, p3, width0: float, width1: float | None = None, layer: str = "body", steps: int = 48) -> None:
        """Kubische Bézierkurve mit (linear) wechselnder Breite."""
        width1 = width0 if width1 is None else width1
        d = self._draw(layer)
        for i in range(steps + 1):
            t = i / steps
            x = (1 - t) ** 3 * p0[0] + 3 * (1 - t) ** 2 * t * p1[0] + 3 * (1 - t) * t ** 2 * p2[0] + t ** 3 * p3[0]
            y = (1 - t) ** 3 * p0[1] + 3 * (1 - t) ** 2 * t * p1[1] + 3 * (1 - t) * t ** 2 * p2[1] + t ** 3 * p3[1]
            r = (width0 + (width1 - width0) * t) * SS / 2
            d.ellipse((x * SS - r, y * SS - r, x * SS + r, y * SS + r), fill=255)

    def ascii(self, rows, x0: int, y0: int) -> None:
        """Pixelkunst: '#' Fläche, '+' Fläche mit dunklerer Zeichnung."""
        for dy, row in enumerate(rows):
            for dx, ch in enumerate(row):
                if ch in "#+":
                    self.rect(x0 + dx, y0 + dy, x0 + dx + 1, y0 + dy + 1)
                if ch == "+":
                    self.rect(x0 + dx, y0 + dy, x0 + dx + 1, y0 + dy + 1, "detail")

    def _draw(self, layer: str) -> ImageDraw.ImageDraw:
        return {"body": self.d_body, "detail": self.d_detail, "holes": self.d_holes}[layer]

    def raster(self, scale: float = 1.0, offset: tuple[float, float] = (0.0, 0.0), w: int = W, h: int = H,
               threshold: float = 0.5) -> list[list[int]]:
        """Auf ein Pixelraster bringen: 0 = leer, sonst Helligkeit des Musters."""
        size = (max(1, round(W * scale)), max(1, round(H * scale)))
        body = self.body.resize(size, Image.BOX)
        detail = self.detail.resize(size, Image.BOX)
        holes = self.holes.resize(size, Image.BOX)
        out = [[0] * w for _ in range(h)]
        ox, oy = offset
        for y in range(size[1]):
            for x in range(size[0]):
                tx, ty = x + round(ox), y + round(oy)
                if not (0 <= tx < w and 0 <= ty < h):
                    continue
                cover = body.getpixel((x, y)) / 255
                if cover < threshold or holes.getpixel((x, y)) / 255 >= 0.5:
                    continue
                out[ty][tx] = DETAIL if detail.getpixel((x, y)) / 255 >= 0.45 else FULL
        return out


# =============================================================================
# Die vier Zeichen (Bannerpixel: x 0..20, y 0..40; innerhalb der Bordüre bleibt x 3..17, y 3..37)
# =============================================================================

def tower() -> Canvas:
    c = Canvas()
    # Sockel, Schaft, Gesims, Zinnenkranz
    c.rect(5, 32, 15, 34)
    c.rect(6, 15, 14, 32)
    c.rect(5, 13, 15, 15)
    for x0 in (5, 9, 13):
        c.rect(x0, 10, x0 + 2, 13)
    # Fahne auf dem mittleren Zinnen
    c.line([(10.0, 5.6), (10.0, 10.2)], 0.8)
    c.poly([(10.3, 5.4), (14.2, 6.8), (10.3, 8.2)])
    # Tor mit Rundbogen und Fallgitter, Fenster
    c.rect(8, 27, 12, 32, "holes")
    c.ellipse(10, 27, 2, 1.6, "holes")
    c.rect(9, 18, 11, 22, "holes")
    c.ellipse(10, 18, 1, 0.9, "holes")
    # Mauerfugen
    for y in (17.5, 21.5, 25.5, 29.5):
        c.rect(6, y, 14, y + 0.6, "detail")
    for x, y in ((8, 15.5), (12, 15.5), (7.5, 19.5), (12.5, 19.5), (8, 23.5), (12, 23.5), (7, 27.5), (13, 27.5)):
        c.rect(x, y, x + 0.6, y + 2, "detail")
    c.rect(5, 14.6, 15, 15.2, "detail")
    c.rect(5, 31.6, 15, 32.2, "detail")
    return c


def raven() -> Canvas:
    c = Canvas()
    # Leib, Hals, Kopf, Schnabel
    c.ellipse(10.6, 21.2, 4.9, 3.5, angle=28)
    c.poly([(5.3, 16.6), (8.2, 14.4), (11.0, 18.4), (7.8, 21.0)])
    c.ellipse(6.6, 15.4, 2.5, 2.3)
    c.poly([(4.6, 14.4), (3.2, 16.3), (4.9, 17.1), (6.0, 16.6)])
    # Flügel bis zum Schwanzansatz, Schwanz
    c.poly([(8.4, 17.6), (13.4, 19.6), (16.6, 24.2), (16.2, 27.0), (12.0, 25.0), (8.6, 21.6)])
    c.poly([(13.2, 23.4), (16.8, 29.6), (15.2, 30.6), (11.8, 25.6)])
    # Beine mit Krallen
    c.line([(9.6, 24.0), (9.2, 28.6)], 0.9)
    c.line([(11.4, 24.4), (11.6, 28.6)], 0.9)
    c.line([(7.8, 29.0), (10.6, 29.0)], 0.8)
    c.line([(10.4, 29.0), (13.0, 29.0)], 0.8)
    # Auge, Flügelfedern, Schnabelspalt
    c.ellipse(6.4, 14.9, 0.55, 0.55, "holes")
    c.line([(9.6, 19.0), (14.6, 22.6)], 0.55, "detail")
    c.line([(10.2, 21.0), (15.4, 25.0)], 0.55, "detail")
    c.line([(11.6, 23.6), (15.8, 26.6)], 0.55, "detail")
    c.line([(3.6, 16.0), (5.4, 15.9)], 0.45, "detail")
    return c


SUN = (
    "......##......",
    "..#...##...#..",
    "...#..##..#...",
    "....######....",
    "...########...",
    "..##########..",
    "####+####+####",
    "##############",
    "..##########..",
    "...##++++##...",
    "....######....",
    "...#..##..#...",
    "..#...##...#..",
    "......##......",
)


def sun() -> Canvas:
    """Die Sonne ist von Hand gesetzt (acht Strahlen, Gesicht): so bleibt sie auch klein klar."""
    c = Canvas()
    c.ascii(SUN, 3, 13)
    return c


def swan() -> Canvas:
    c = Canvas()
    # Leib und gewölbter Flügel
    c.ellipse(11.2, 24.6, 5.4, 2.7)
    c.poly([(8.2, 23.6), (10.0, 20.2), (13.0, 18.4), (16.2, 18.6), (16.8, 21.0), (16.4, 24.4), (12.0, 25.2)])
    c.poly([(15.0, 23.0), (17.0, 21.4), (16.6, 25.4)])
    # S-förmiger Hals und Kopf mit Schnabel
    c.curve((7.2, 24.2), (3.6, 21.0), (8.4, 15.6), (6.6, 12.4), 1.9, 1.3)
    c.ellipse(6.4, 11.9, 1.6, 1.25)
    c.poly([(5.0, 11.6), (3.0, 13.0), (5.2, 12.9)])
    # Wellen
    for y in (29.0, 31.6):
        pts = [(4.0 + i * 0.25, y + math.sin((4.0 + i * 0.25) * 1.25) * 0.55) for i in range(49)]
        c.line(pts, 0.85)
    # Auge, Schnabel, Federn
    c.ellipse(6.6, 11.6, 0.45, 0.45, "holes")
    c.poly([(5.0, 11.6), (3.0, 13.0), (5.2, 12.9)], "detail")
    c.line([(10.8, 21.6), (15.6, 20.2)], 0.5, "detail")
    c.line([(10.6, 23.2), (16.0, 22.4)], 0.5, "detail")
    return c


EMBLEMS = {"tower": tower, "raven": raven, "sun": sun, "swan": swan}


# =============================================================================
# Texturen
# =============================================================================

def banner_texture(front: list[list[int]]) -> Image.Image:
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for y in range(H):
        for x in range(W):
            v = front[y][x]
            if v:
                img.putpixel((1 + x, 1 + y), (v, v, v, 255))
                back = round(v * 0.92)
                img.putpixel((22 + (W - 1 - x), 1 + y), (back, back, back, 255))
    return img


def shield_texture(front: list[list[int]]) -> Image.Image:
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for y in range(20):
        for x in range(10):
            v = front[y][x]
            if v:
                img.putpixel((2 + x, 2 + y), (v, v, v, 255))
    return img


def build() -> dict[str, Image.Image]:
    out: dict[str, Image.Image] = {}
    for name, draw in EMBLEMS.items():
        canvas = draw()
        out[f"banner/{name}"] = banner_texture(canvas.raster())
        # Schild: halbe Größe; die Schildfläche (10 × 20) entspricht der Bannerfront
        scale = 0.62
        out[f"shield/{name}"] = shield_texture(canvas.raster(scale=scale, offset=(5 - 10 * scale, 10.2 - 20.5 * scale), w=10, h=20,
                                                             threshold=0.45))
    return out


def preview(images: dict[str, Image.Image], path: Path) -> None:
    """Banner und Schilde so eingefärbt, wie das Spiel sie zeigt (Feld, Zeichen, Bordüre)."""
    colors = {"tower": ((60, 68, 170), (249, 255, 254), (157, 157, 151)),
              "raven": ((176, 46, 38), (29, 29, 33), (29, 29, 33)),
              "sun": ((249, 128, 29), (254, 216, 61), (131, 84, 50)),
              "swan": ((94, 124, 22), (249, 255, 254), (249, 255, 254))}
    zoom = 8
    sheet = Image.new("RGB", ((W * zoom + 20) * 4 + 20 + (10 * zoom + 20) * 4, H * zoom + 40), (40, 40, 40))
    for i, (name, (field, charge, border)) in enumerate(colors.items()):
        for kind, (w, h, ox, oy) in (("banner", (W, H, 1, 1)), ("shield", (10, 20, 2, 2))):
            tex = images[f"{kind}/{name}"]
            img = Image.new("RGB", (w, h), field)
            for y in range(h):
                for x in range(w):
                    a = tex.getpixel((ox + x, oy + y))
                    if a[3]:
                        f = a[0] / 255
                        img.putpixel((x, y), tuple(round(ch * f) for ch in charge))
                    edge = min(x, y, w - 1 - x, h - 1 - y)
                    lim = 2 if kind == "banner" else 1
                    if edge < lim:
                        img.putpixel((x, y), border)
            img = img.resize((w * zoom, h * zoom), Image.NEAREST)
            if kind == "banner":
                sheet.paste(img, (20 + i * (W * zoom + 20), 20))
            else:
                sheet.paste(img, (20 + 4 * (W * zoom + 20) + i * (10 * zoom + 20), 20))
    sheet.save(path)


def main() -> None:
    images = build()
    for key, img in images.items():
        target = OUT / f"{key}.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        img.save(target)
    print(f"{len(images)} Bannermuster geschrieben nach {OUT}")
    if "--preview" in sys.argv:
        preview(images, Path(sys.argv[sys.argv.index("--preview") + 1]))


if __name__ == "__main__":
    main()
