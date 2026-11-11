#!/usr/bin/env python3
"""
Die Fische des Angelns (seit 0.79): Bilder der Gegenstände (16×16) der 80 von Hand gestalteten Arten. Die 916 Arten der
Biome (seit 0.81) malt tools/fish_gen.py mit denselben Werkzeugen; Schattenrisse braucht es nicht mehr – das Fischbuch färbt
das Bild einer noch nie gefangenen Art dunkel ein.

Jeder Fisch liegt schräg im Bild wie die Fische von Minecraft: der Schwanz unten links, der Kopf oben rechts, der Rücken
oben links, der Bauch unten rechts. Gezeichnet wird er aus wenigen Angaben – Umriss des Leibs (Halbbreite entlang der
Achse), Schwanzflosse (Länge, Spreizung, Gabelung), Rücken- und Afterflosse, Farben für Rücken, Flanke und Bauch, ein
Muster (Tupfen, Bänder, Längsstreif, Leuchtpunkte …) und Besonderheiten (Schwert, Barteln, Angel, Segel, Stacheln, Zähne).
Wesen, die keine Fischgestalt haben (Oktopus, Kalmar, Seepferdchen, Assel, Perlboot, Trilobit …), sind von Hand gesetzt.

Aufruf:
    python3 tools/fish_textures.py                 # schreibt alle Bilder
    python3 tools/fish_textures.py --preview x.png # nur eine vergrößerte Übersicht
"""
from __future__ import annotations

import math
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from pixelart import hexc, mix  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src/main/resources/assets/kingdomsatwar/textures")
S = 16
SS = 4                          # Unterabtastung je Pixel
R2 = math.sqrt(0.5)
TAIL = (1.3, 14.7)              # Ende der Schwanzflosse
CLEAR = (0, 0, 0, 0)


def h01(*a) -> float:
    h = 2166136261
    for v in a:
        h = ((h ^ (int(v) & 0xFFFFFFFF)) * 16777619) & 0xFFFFFFFF
    h ^= h >> 13
    h = (h * 1274126177) & 0xFFFFFFFF
    return (h & 0xFFFF) / 0xFFFF


def interp(points, u):
    if u <= points[0][0]:
        return points[0][1]
    for (u0, w0), (u1, w1) in zip(points, points[1:]):
        if u <= u1:
            k = (u - u0) / max(1e-6, u1 - u0)
            k = k * k * (3 - 2 * k)
            return w0 + (w1 - w0) * k
    return points[-1][1]


# Grundformen: Leib (Anteil der Leibeslänge, Halbbreite), Länge, Schwanz (Länge, Spreizung, Gabel), Rückenflosse (von, bis, Höhe),
# Afterflosse (von, bis, Höhe)
SHAPES = {
    "trout": dict(len=17.0, body=[(0, .55), (.2, 1.45), (.5, 1.95), (.8, 1.65), (1, .55)], tail=(3.2, 2.4, .5), dorsal=(.42, .66, 1.3), anal=(.22, .4, .9)),
    "perch": dict(len=16.0, body=[(0, .55), (.2, 1.8), (.5, 2.55), (.8, 2.0), (1, .7)], tail=(2.9, 2.3, .35), dorsal=(.36, .82, 1.7), anal=(.2, .42, 1.0)),
    "carp": dict(len=16.5, body=[(0, .65), (.2, 2.0), (.5, 2.8), (.8, 2.25), (1, .9)], tail=(3.1, 2.7, .45), dorsal=(.35, .8, 1.2), anal=(.2, .38, 1.0)),
    "pike": dict(len=18.0, body=[(0, .5), (.2, 1.25), (.6, 1.5), (.84, 1.25), (1, .45)], tail=(2.9, 2.3, .55), dorsal=(.14, .3, 1.4), anal=(.12, .28, 1.2)),
    "catfish": dict(len=17.0, body=[(0, .5), (.3, 1.35), (.6, 1.95), (.86, 2.2), (1, 1.3)], tail=(2.8, 2.0, .0), dorsal=(.52, .64, 1.0), anal=(.1, .5, .8)),
    "eel": dict(len=18.0, body=[(0, .35), (.3, .95), (.75, 1.1), (1, .65)], tail=(0.6, 0.4, .0), dorsal=(.05, .8, .55), anal=(.05, .6, .4), wave=1.7),
    "sturgeon": dict(len=18.0, body=[(0, .4), (.3, 1.2), (.6, 1.6), (.84, 1.3), (1, .3)], tail=(3.2, 2.4, .5, 1.5), dorsal=(.1, .24, 1.1), anal=(.08, .2, .8)),
    "tuna": dict(len=17.5, body=[(0, .32), (.2, 1.5), (.5, 2.3), (.8, 1.85), (1, .5)], tail=(2.6, 3.3, .85), dorsal=(.45, .6, 1.5), anal=(.22, .34, 1.2)),
    "sword": dict(len=14.0, body=[(0, .3), (.2, 1.3), (.5, 1.9), (.8, 1.6), (1, .5)], tail=(2.4, 3.0, .85), dorsal=(.6, .8, 2.3), anal=(.2, .32, 1.0)),
    "round": dict(len=13.5, body=[(0, .7), (.22, 2.5), (.5, 3.1), (.78, 2.7), (1, 1.1)], tail=(2.2, 1.6, .1), dorsal=(.2, .34, .9), anal=(.18, .3, .8)),
    "sunfish": dict(len=12.5, body=[(0, 2.4), (.15, 3.3), (.5, 3.9), (.8, 3.2), (1, 1.2)], tail=(1.0, 3.0, .0), dorsal=(.1, .36, 3.6), anal=(.1, .36, 3.4)),
    "flat": dict(len=15.5, body=[(0, .5), (.2, 2.3), (.5, 3.1), (.8, 2.5), (1, .8)], tail=(2.6, 1.9, .1), dorsal=(.08, .92, .9), anal=(.08, .88, .9)),
    "angler": dict(len=14.0, body=[(0, .45), (.3, 1.3), (.6, 2.7), (.86, 3.1), (1, 1.7)], tail=(2.4, 1.8, .1), dorsal=(.2, .34, .9), anal=(.15, .3, .8)),
    "viper": dict(len=17.5, body=[(0, .35), (.3, .95), (.7, 1.25), (.88, 1.5), (1, .9)], tail=(2.2, 1.6, .5), dorsal=(.55, .64, 2.0), anal=(.1, .3, .7)),
    "hatchet": dict(len=12.5, body=[(0, .3), (.3, 1.5), (.6, 3.0), (.85, 2.7), (1, 1.0)], tail=(2.4, 1.9, .5), dorsal=(.4, .6, .9), anal=(.2, .5, .6)),
    "small": dict(len=12.5, body=[(0, .4), (.25, 1.2), (.5, 1.6), (.8, 1.35), (1, .5)], tail=(2.4, 1.9, .5), dorsal=(.42, .64, 1.0), anal=(.22, .4, .7)),
    "ribbon": dict(len=18.5, body=[(0, .3), (.2, .8), (.8, 1.1), (1, .85)], tail=(0.5, 0.3, .0), dorsal=(.0, .96, 1.0), anal=(0, 0, 0), wave=0.9),
    "shark": dict(len=18.0, body=[(0, .4), (.25, 1.2), (.55, 1.9), (.8, 1.6), (1, .4)], tail=(3.0, 2.6, .55, 1.6), dorsal=(.48, .62, 2.1), anal=(.2, .3, .8)),
    "coel": dict(len=16.5, body=[(0, .9), (.2, 2.0), (.5, 2.6), (.8, 2.2), (1, .9)], tail=(3.0, 2.3, .0), dorsal=(.5, .64, 1.3), anal=(.22, .36, 1.2)),
    "goldfish": dict(len=14.0, body=[(0, .6), (.2, 1.9), (.5, 2.6), (.8, 2.1), (1, .9)], tail=(4.2, 3.2, .55), dorsal=(.35, .7, 1.4), anal=(.2, .36, 1.1)),
    "piranha": dict(len=14.5, body=[(0, .6), (.22, 2.0), (.52, 2.9), (.8, 2.5), (1, 1.3)], tail=(2.8, 2.4, .45), dorsal=(.4, .62, 1.2), anal=(.15, .42, 1.1)),
    "arapaima": dict(len=18.0, body=[(0, .7), (.3, 1.65), (.7, 1.9), (1, .85)], tail=(2.2, 1.9, .0), dorsal=(.06, .32, 1.1), anal=(.06, .3, 1.0)),
    "skipper": dict(len=13.0, body=[(0, .45), (.3, 1.1), (.7, 1.5), (.9, 1.7), (1, 1.2)], tail=(2.4, 1.6, .0), dorsal=(.4, .7, 2.0), anal=(.2, .4, .5)),
    "armored": dict(len=17.5, body=[(0, .5), (.25, 1.6), (.55, 2.6), (.8, 3.2), (1, 2.2)], tail=(3.0, 2.4, .3, 1.4), dorsal=(.3, .46, 1.4), anal=(.15, .3, 1.0)),
    "sail": dict(len=14.5, body=[(0, .3), (.2, 1.3), (.5, 1.9), (.8, 1.6), (1, .5)], tail=(2.4, 3.1, .85), dorsal=(.3, .86, 3.4), anal=(.2, .32, 1.0)),
    # seit 0.81 (die Arten der Biome)
    "bream": dict(len=15.0, body=[(0, .55), (.2, 2.2), (.5, 3.3), (.8, 2.6), (1, .9)], tail=(3.0, 2.6, .5), dorsal=(.3, .76, 1.3), anal=(.15, .45, 1.1)),
    "angel": dict(len=12.0, body=[(0, .5), (.3, 1.8), (.55, 2.4), (.82, 2.0), (1, .8)], tail=(2.6, 1.9, .3), dorsal=(.36, .6, 4.6), anal=(.34, .56, 4.2), sweep=True),
    "flyer": dict(len=15.0, body=[(0, .35), (.25, 1.1), (.55, 1.5), (.8, 1.25), (1, .5)], tail=(2.9, 2.7, .7), dorsal=(.46, .84, 3.1), anal=(.42, .72, 2.3)),
    "box": dict(len=12.5, body=[(0, .9), (.15, 2.5), (.5, 2.75), (.85, 2.55), (1, 1.6)], tail=(2.2, 1.5, .0), dorsal=(.14, .28, 1.0), anal=(.12, .26, .9)),
    "mahi": dict(len=17.0, body=[(0, .35), (.3, 1.3), (.7, 2.25), (.9, 2.55), (1, 1.7)], tail=(2.8, 3.0, .8), dorsal=(.14, .96, 1.2), anal=(.14, .5, .8)),
    "whale": dict(len=17.5, body=[(0, .5), (.2, 1.6), (.5, 2.9), (.8, 3.3), (1, 2.1)], tail=(2.8, 3.5, .6), dorsal=(.3, .42, .9), anal=(.0, .0, .0)),
    "moray": dict(len=18.0, body=[(0, .5), (.3, 1.3), (.75, 1.6), (1, 1.05)], tail=(0.7, 0.5, .0), dorsal=(.05, .9, .7), anal=(.05, .6, .45), wave=1.5),
    "gar": dict(len=15.5, body=[(0, .4), (.2, .95), (.6, 1.15), (.86, 1.0), (1, .4)], tail=(2.6, 2.0, .45), dorsal=(.1, .24, 1.2), anal=(.08, .22, 1.0)),
}


def fish(shape: str, back: str, side: str, belly: str, fin: str, pattern=None, eye="#101018", **extra) -> Image.Image:
    """Zeichnet einen Fisch. pattern: (Art, Farbe[, Wert]); extra: bill, barbels, lure, teeth, spines, glow, tailcol, fintip, finlets."""
    sp = dict(SHAPES[shape])
    sp.update({k: v for k, v in extra.items() if k in ("len", "body", "tail", "dorsal", "anal", "wave", "sweep")})
    cb, cs, cl, cf = hexc(back), hexc(side), hexc(belly), hexc(fin)
    ctail = hexc(extra.get("tailcol", fin))
    ctip = hexc(extra["fintip"]) if extra.get("fintip") else None
    length = sp["len"]
    tail = sp["tail"]
    tail_len, tail_spread, fork = tail[0], tail[1], tail[2]
    upper = tail[3] if len(tail) > 3 else 1.0       # oberer Schwanzlappen länger (Stör, Hai)
    wave = sp.get("wave", 0.0)
    sweep = sp.get("sweep", False)      # Flossen wie Wimpel: hinten am höchsten, nach vorn auslaufend
    seed = sum(map(ord, shape + back + side))
    line = mix(cb, (8, 8, 14, 255), 0.62)
    body_len = length - tail_len
    kinds = [[None] * (S * SS) for _ in range(S * SS)]

    def center(a):
        return wave * math.sin(a * 0.62 + 0.6) * min(1.0, a / 5.0) if wave else 0.0

    for sy in range(S * SS):
        for sx in range(S * SS):
            px_, py_ = (sx + 0.5) / SS, (sy + 0.5) / SS
            dx, dy = px_ - TAIL[0], py_ - TAIL[1]
            a = (dx - dy) * R2                       # entlang der Achse (Schwanz -> Kopf)
            v = (dx + dy) * R2 - center(a)           # quer (+ = Bauch)
            kind = None
            if 0.0 <= a <= length:
                u = (a - tail_len) / body_len
                if a >= tail_len - 0.3:
                    w = interp(sp["body"], max(0.0, u))
                    if abs(v) <= w:
                        kind = ("body", u, v / max(w, 0.01), a)
                    else:
                        d0, d1, dh = sp["dorsal"]
                        if dh and d0 <= u <= d1 and v < 0:
                            k = (u - d0) / max(1e-6, d1 - d0)
                            if sweep:
                                hgt = dh * (1.0 - k) ** 0.8
                            else:
                                hgt = dh * (math.sin(math.pi * min(1.0, k * 1.15)) ** 0.6 if shape not in ("sword", "shark", "sail") else (k ** 0.7))
                            if -v <= w + hgt:
                                kind = ("fin", u, (-v - w) / max(hgt, 0.01), a)
                        a0, a1, ah = sp["anal"]
                        if kind is None and ah and a0 <= u <= a1 and v > 0:
                            k = (u - a0) / max(1e-6, a1 - a0)
                            hgt = ah * ((1.0 - k) ** 0.8 if sweep else math.sin(math.pi * k) ** 0.6)
                            if v <= w + hgt:
                                kind = ("fin", u, (v - w) / max(hgt, 0.01), a)
                if kind is None and a <= tail_len + 0.5:
                    k = 1.0 - a / max(tail_len, 0.01)       # 1 am Ende der Flosse, 0 am Schwanzstiel
                    base = interp(sp["body"], 0.0)
                    hgt = base + (tail_spread - base) * max(0.0, k) ** 0.85
                    if v < 0:
                        hgt *= upper
                    notch = fork * tail_spread * max(0.0, (k - 0.35) / 0.65)
                    if abs(v) <= hgt and abs(v) >= notch and k >= -0.2:
                        kind = ("tail", k, abs(v) / max(hgt, 0.01), a)
            bill = extra.get("bill", 0.0)
            if kind is None and bill and length <= a <= length + bill and abs(v + 0.15) <= 0.42 * (1.0 - (a - length) / bill) + 0.12:
                kind = ("bill", 0, 0, a)
            kinds[sy][sx] = kind
    img = Image.new("RGBA", (S, S), CLEAR)
    px = img.load()
    for y in range(S):
        for x in range(S):
            votes = {}
            for j in range(SS):
                for i in range(SS):
                    k = kinds[y * SS + j][x * SS + i]
                    if k is not None:
                        votes.setdefault(k[0], []).append(k)
            total = sum(len(v) for v in votes.values())
            if total < SS * SS * 0.45:
                continue
            name = max(votes, key=lambda n: len(votes[n]))
            samples = votes[name]
            u = sum(s[1] for s in samples) / len(samples)
            t = sum(s[2] for s in samples) / len(samples)
            a = sum(s[3] for s in samples) / len(samples)
            if name == "body":
                col = cb if t < -0.38 else (cl if t > 0.34 else cs)
                if -0.38 <= t < -0.1:
                    col = mix(cb, cs, 0.6)
                col = paint_pattern(pattern, col, cb, cs, cl, u, t, a, x, y, seed)
            elif name == "fin":
                col = (ctip if ctip else mix(cf, line, 0.35)) if t > 0.7 else cf
                if int(a * 1.6) % 2 == 0:
                    col = mix(col, line, 0.16)
            elif name == "tail":
                col = (ctip if ctip else mix(ctail, line, 0.3)) if (u > 0.72 or t > 0.82) else ctail
                if int(t * 3.0) % 2 == 1:
                    col = mix(col, line, 0.14)
            else:
                col = mix(cs, cb, 0.4)
            px[x, y] = col
    extras(img, sp, extra, length, tail_len, cb, cs, cl, cf, line, center)
    # Auge
    ea = length - max(1.6, 0.14 * body_len) - extra.get("eye_back", 0.0)
    ev = -0.25 * interp(sp["body"], (ea - tail_len) / body_len) + center(ea)
    ex = TAIL[0] + (ea + ev) * R2
    ey = TAIL[1] + (-ea + ev) * R2
    exi, eyi = int(ex), int(ey)
    if 0 <= exi < S and 0 <= eyi < S and px[exi, eyi][3]:
        px[exi, eyi] = hexc(eye)
        if extra.get("big_eye") and exi > 0 and px[exi - 1, eyi][3]:
            px[exi - 1, eyi] = (240, 244, 250, 255)
    outline(img, line)
    glows(img, extra)
    return img


def paint_pattern(pattern, col, cb, cs, cl, u, t, a, x, y, seed):
    if not pattern:
        return col
    kind, pc = pattern[0], hexc(pattern[1])
    val = pattern[2] if len(pattern) > 2 else None
    if kind == "spots":
        if t < 0.3 and h01(x, y, seed) < (val or 0.22):
            return pc
    elif kind == "bars":
        n = val or 5
        if t < 0.45 and int(u * n * 2) % 2 == 1 and 0.08 < u < 0.92:
            return mix(col, pc, 0.85)
    elif kind == "line":
        if -0.1 <= t <= 0.22 and 0.05 < u < 0.9:
            return pc
    elif kind == "belly":
        if t > 0.1:
            return mix(col, pc, min(1.0, 0.5 + t))
    elif kind == "patches":
        if h01(int(a * 0.55), int((t + 1) * 1.6), seed) < (val or 0.45):
            return pc
    elif kind == "scales":
        if (x + y) % 2 == 0 and -0.4 < t < 0.5:
            return mix(col, pc, 0.45)
    elif kind == "mottle":
        if h01(x, y, seed + 3) < (val or 0.35):
            return mix(col, pc, 0.7)
    elif kind == "tailfade":
        return mix(col, pc, max(0.0, 1.0 - u * 2.2))
    elif kind == "headfade":
        return mix(col, pc, max(0.0, (u - 0.55) * 2.0))
    elif kind == "saddle":
        n = val or 4
        if t < -0.05 and int(u * n * 2) % 2 == 1 and 0.1 < u < 0.9:
            return mix(col, pc, 0.9)
    elif kind == "stripes":
        if (-0.62 <= t <= -0.36 or 0.0 <= t <= 0.24) and 0.06 < u < 0.92:
            return pc
    elif kind == "collar":
        if (val or 0.56) <= u <= (val or 0.56) + 0.16:
            return pc
    elif kind == "mask":
        if u > 0.78:
            return mix(col, pc, 0.85)
    elif kind == "backline":
        if t < -0.5 and 0.05 < u < 0.95:
            return pc
    elif kind == "halves":
        if u < 0.5:
            return mix(col, pc, 0.8)
    return col


def put(img, x, y, col):
    if 0 <= x < S and 0 <= y < S:
        img.load()[x, y] = col


def at(a, v):
    """Bildpunkt zu Achsenlage a und Querlage v."""
    return int(TAIL[0] + (a + v) * R2), int(TAIL[1] + (-a + v) * R2)


def extras(img, sp, extra, length, tail_len, cb, cs, cl, cf, line, center):
    body_len = length - tail_len
    if extra.get("barbels"):
        for k, (da, dv) in enumerate(((0.4, 1.2), (1.0, 2.0), (0.2, 2.6))[:extra["barbels"]]):
            put(img, *at(length - 0.4 + da * 0.3, interp(sp["body"], 0.95) + dv * 0.5), mix(cl, line, 0.35))
            put(img, *at(length + 0.2 + da * 0.5, interp(sp["body"], 0.95) + dv * 0.75), mix(cl, line, 0.35))
    if extra.get("lure"):
        col = hexc(extra["lure"])
        a0 = length - 2.6
        w = interp(sp["body"], (a0 - tail_len) / body_len)
        for k in range(3):
            put(img, *at(a0 + k * 0.9, -w - 0.6 - k * 0.8), mix(cb, line, 0.2))
        gx, gy = at(a0 + 3.0, -w - 2.6)
        put(img, gx, gy, col)
        put(img, gx + 1, gy, mix(col, (255, 255, 255, 255), 0.6))
    if extra.get("teeth"):
        w = interp(sp["body"], 0.97)
        for k in range(extra["teeth"]):
            put(img, *at(length - 0.3 - k * 1.1, w * 0.55 + 0.2), (244, 240, 226, 255))
    if extra.get("spines"):
        col = hexc(extra["spines"])
        for k in range(5):
            a = tail_len + body_len * (0.25 + 0.14 * k)
            w = interp(sp["body"], (a - tail_len) / body_len)
            put(img, *at(a, -w - 1.9), col)
            put(img, *at(a + 0.3, -w - 2.9), mix(col, (255, 255, 255, 255), 0.4))
            if k % 2 == 0:
                put(img, *at(a, w + 1.6), col)
    if extra.get("finlets"):
        col = hexc(extra["finlets"])
        for k in range(3):
            a = tail_len + 0.5 + k * 1.3
            w = interp(sp["body"], (a - tail_len) / body_len)
            put(img, *at(a, -w - 0.55), col)
            put(img, *at(a, w + 0.55), col)
    if extra.get("scutes"):
        col = hexc(extra["scutes"])
        for k in range(6):
            a = tail_len + body_len * (0.14 + 0.13 * k)
            put(img, *at(a, 0.0), col)
    if extra.get("crest"):
        col = hexc(extra["crest"])
        for k in range(3):
            put(img, *at(length - 1.2 - k * 0.5, -interp(sp["body"], 0.9) - 1.6 - k * 0.9), col)
    if extra.get("plates"):
        col = hexc(extra["plates"])
        for k in range(4):
            a = length - 1.2 - k * 1.5
            w = interp(sp["body"], (a - tail_len) / body_len)
            for j in range(-2, 3):
                put(img, *at(a, j * w * 0.42), col)


def glows(img, extra):
    if extra.get("glow"):
        col = hexc(extra["glow"])
        px = img.load()
        pts = [(x, y) for y in range(S) for x in range(S) if px[x, y][3]]
        seed = len(pts)
        k = 0
        for (x, y) in pts:
            # Leuchtpunkte entlang der Bauchseite: innen liegende Pixel, deren Nachbar unten rechts der Rand ist
            if x + 1 < S and y + 1 < S and px[x + 1, y + 1][3] and (x + 2 >= S or y + 2 >= S or not px[min(S - 1, x + 2), min(S - 1, y + 2)][3]):
                if (x - y + seed) % 3 == 0:
                    px[x, y] = col
                    k += 1


def outline(img, col):
    px = img.load()
    edge = []
    for y in range(S):
        for x in range(S):
            if px[x, y][3]:
                continue
            for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
                if 0 <= nx < S and 0 <= ny < S and px[nx, ny][3]:
                    edge.append((x, y))
                    break
    for x, y in edge:
        px[x, y] = col


def ascii_sprite(rows, colors) -> Image.Image:
    img = Image.new("RGBA", (S, S), CLEAR)
    px = img.load()
    assert len(rows) == S, len(rows)
    for y, row in enumerate(rows):
        assert len(row) == S, (y, row)
        for x, ch in enumerate(row):
            if ch != ".":
                px[x, y] = hexc(colors[ch])
    return img


# =============================================================================================
# Wesen ohne Fischgestalt: aus Ellipsen und Strichen gesetzt
# =============================================================================================

class Painter:
    """Formen in Reihenfolge (spätere liegen oben); je Pixel entscheidet die Mehrheit der Abtastpunkte."""

    def __init__(self):
        self.shapes = []
        self.dots = []

    def ellipse(self, cx, cy, rx, ry, col, rot=0.0):
        c, s = math.cos(math.radians(rot)), math.sin(math.radians(rot))

        def inside(x, y):
            dx, dy = x - cx, y - cy
            u, v = dx * c + dy * s, -dx * s + dy * c
            return (u / rx) ** 2 + (v / ry) ** 2 <= 1.0
        self.shapes.append((inside, hexc(col)))
        return self

    def stroke(self, pts, w0, w1, col):
        segs = []
        total = sum(math.dist(a, b) for a, b in zip(pts, pts[1:])) or 1.0
        run = 0.0
        for a, b in zip(pts, pts[1:]):
            d = math.dist(a, b)
            segs.append((a, b, run / total, (run + d) / total))
            run += d

        def inside(x, y):
            for (a, b, t0, t1) in segs:
                ax, ay = a
                bx, by = b
                vx, vy = bx - ax, by - ay
                ll = vx * vx + vy * vy or 1e-6
                k = max(0.0, min(1.0, ((x - ax) * vx + (y - ay) * vy) / ll))
                d = math.hypot(x - (ax + vx * k), y - (ay + vy * k))
                w = w0 + (w1 - w0) * (t0 + (t1 - t0) * k)
                if d <= w / 2.0:
                    return True
            return False
        self.shapes.append((inside, hexc(col)))
        return self

    def dot(self, x, y, col):
        self.dots.append((x, y, hexc(col)))
        return self

    def dome(self, cx, cy, rx, ry, col):
        """Die obere Hälfte einer Ellipse (eine Kuppel mit geradem Rand bei cy)."""
        self.shapes.append((lambda x, y: y <= cy and ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1.0, hexc(col)))
        return self

    def render(self, line="#14121c", shade=0.22) -> Image.Image:
        img = Image.new("RGBA", (S, S), CLEAR)
        px = img.load()
        for y in range(S):
            for x in range(S):
                votes = {}
                for j in range(SS):
                    for i in range(SS):
                        sx, sy = x + (i + 0.5) / SS, y + (j + 0.5) / SS
                        for inside, col in reversed(self.shapes):
                            if inside(sx, sy):
                                votes[col] = votes.get(col, 0) + 1
                                break
                if sum(votes.values()) >= SS * SS * 0.45:
                    px[x, y] = max(votes, key=votes.get)
        # Licht von oben links: Kanten dort heller, unten rechts dunkler
        src = img.copy().load()
        for y in range(S):
            for x in range(S):
                if not src[x, y][3]:
                    continue
                up = y == 0 or x == 0 or not src[x - 1, y][3] or not src[x, y - 1][3]
                down = y == S - 1 or x == S - 1 or not src[x + 1, y][3] or not src[x, y + 1][3]
                if up and not down:
                    px[x, y] = mix(src[x, y], (255, 255, 255, 255), shade)
                elif down and not up:
                    px[x, y] = mix(src[x, y], (0, 0, 10, 255), shade + 0.06)
        for x, y, col in self.dots:
            put(img, x, y, col)
        outline(img, hexc(line))
        return img


def octopus(body="#c8503a", dark="#8a2e22", light="#f0a078", eye="#fff4c0"):
    p = Painter()
    arms = [((4.6, 8.0), (2.6, 10.5), (2.4, 13.0), (3.6, 14.2)), ((6.2, 8.6), (5.2, 11.2), (5.8, 13.6)),
            ((7.6, 8.8), (7.4, 11.6), (8.4, 14.0)), ((9.4, 8.8), (10.2, 11.4), (9.8, 13.8)),
            ((10.8, 8.4), (12.2, 10.8), (11.8, 13.4)), ((11.8, 7.8), (13.8, 9.8), (14.0, 12.6), (12.9, 13.9))]
    for i, arm in enumerate(arms):
        p.stroke(list(arm), 2.0, 0.9, dark if i % 2 else body)
    p.ellipse(8.2, 5.4, 4.0, 3.5, body)
    p.ellipse(7.0, 4.0, 1.6, 1.1, light)
    p.dot(6, 6, eye).dot(10, 6, eye).dot(6, 7, "#101018").dot(10, 7, "#101018")
    p.dot(3, 12, light).dot(8, 12, light).dot(13, 11, light)
    return p.render(line=dark and "#3a1410")


def squid(body, dark, light, eye="#101018", glow=None, fins=True, cloak=None):
    """Ein Kalmar, schräg wie die Fische: der Mantel oben rechts, die Arme unten links."""
    p = Painter()
    for k, (ex, ey) in enumerate(((1.6, 12.6), (2.8, 14.2), (4.2, 14.6), (1.4, 10.6))):
        p.stroke([(6.4, 9.6), ((6.4 + ex) / 2 + 0.6 * (k % 2), (9.6 + ey) / 2), (ex, ey)], 1.5, 0.8, dark if k % 2 else body)
    if cloak:
        p.ellipse(5.4, 10.6, 3.4, 2.6, cloak, rot=-45)
    p.ellipse(9.4, 6.6, 4.6, 2.5, body, rot=-45)
    if fins:
        p.ellipse(12.6, 2.6, 1.9, 1.2, dark, rot=20)
        p.ellipse(13.4, 4.6, 1.2, 1.9, dark, rot=20)
    p.ellipse(10.2, 5.4, 2.2, 0.9, light, rot=-45)
    p.dot(7, 8, eye)
    if glow:
        for (x, y) in ((9, 8), (11, 6), (12, 4), (3, 12), (5, 13)):
            p.dot(x, y, glow)
    return p.render(line="#16101c")


def seahorse(body="#e8a02a", head="#f0b43a", snout="#d8902a", light="#f8d870", crest="#c87a1e", line="#5a2e0a", eye="#101018"):
    p = Painter()
    p.stroke([(8.6, 6.4), (8.0, 8.4), (8.6, 10.6), (7.6, 12.6), (6.0, 13.4), (5.0, 12.4), (5.8, 11.4)], 3.0, 1.0, body)
    p.stroke([(8.4, 6.0), (9.6, 4.6)], 2.6, 2.6, body)
    p.ellipse(9.4, 3.8, 2.0, 1.7, head)
    p.stroke([(10.4, 4.2), (13.2, 5.2)], 1.3, 1.0, snout)
    p.stroke([(7.0, 7.6), (5.4, 8.6), (5.6, 10.0)], 1.2, 0.8, light)
    p.stroke([(8.6, 2.2), (9.0, 1.2)], 1.0, 0.8, crest)
    p.dot(9, 3, eye).dot(8, 8, light).dot(8, 10, light)
    return p.render(line=line)


def bands(img: Image.Image, every: int, strength: float, skip=()) -> Image.Image:
    """Dunkle Querbänder (Panzerglieder) quer zur schrägen Körperachse."""
    px = img.load()
    for y in range(1, S - 1):
        for x in range(1, S - 1):
            if px[x, y][3] and (x - y) % every == 0 and px[x - 1, y][3] and px[x + 1, y][3] and px[x, y - 1][3] and px[x, y + 1][3] \
                    and px[x, y] not in skip:
                px[x, y] = mix(px[x, y], (10, 8, 14, 255), strength)
    return img


def isopod(body="#b8a8ba", light="#d8cad8", head="#c8bac8", legs="#8a7a90", feelers="#7a6a80", line="#3a3040", eye="#101018"):
    p = Painter()
    p.stroke([(12.6, 3.6), (14.6, 1.4)], 1.0, 0.8, feelers)
    p.stroke([(13.0, 4.8), (15.2, 3.8)], 1.0, 0.8, feelers)
    for k in range(4):
        p.stroke([(3.6 + k * 1.9, 13.4 - k * 1.9), (5.4 + k * 1.9, 14.6 - k * 1.9)], 1.0, 0.8, legs)
    p.ellipse(7.6, 8.4, 5.9, 3.4, body, rot=-45)
    p.ellipse(6.4, 7.2, 4.6, 1.5, light, rot=-45)
    p.ellipse(11.2, 4.8, 2.3, 2.7, head, rot=-45)
    p.dot(11, 3, eye).dot(13, 5, eye)
    return bands(p.render(line=line), 3, 0.38, skip=(hexc(eye),))


def vampire_squid():
    p = Painter()
    # der Umhang zwischen den Armen, unten gezackt
    for k in range(5):
        p.stroke([(3.4 + k * 2.3, 10.6), (2.9 + k * 2.55, 14.6)], 2.4, 0.9, "#6a1220" if k % 2 else "#52101a")
    p.ellipse(8.0, 10.0, 5.6, 2.8, "#5c0e1a")
    # Ohrflossen und Kopf
    p.ellipse(4.0, 4.2, 1.9, 1.1, "#6a1220", rot=-35)
    p.ellipse(12.0, 4.2, 1.9, 1.1, "#6a1220", rot=35)
    p.ellipse(8.0, 6.2, 3.7, 4.2, "#8a1a28")
    p.ellipse(6.8, 4.0, 1.5, 1.2, "#c23a40")
    p.dot(6, 7, "#8adcff").dot(10, 7, "#8adcff").dot(6, 8, "#e8f8ff").dot(10, 8, "#e8f8ff")
    p.dot(4, 12, "#ff8a8a").dot(8, 13, "#ff8a8a").dot(12, 12, "#ff8a8a")
    return p.render(line="#22060c")


def nautilus(shell="#f4ecdc", stripe="#c8582a", flesh="#e8b89a", arm_a="#e0a888", arm_b="#f0c8a8", line="#4a2a1a", eye="#101018"):
    p = Painter()
    for k, (ex, ey) in enumerate(((2.0, 9.6), (2.4, 11.6), (3.8, 13.2), (5.8, 13.8))):
        p.stroke([(6.6, 10.0), (ex, ey)], 1.3, 0.7, arm_b if k % 2 else arm_a)
    p.ellipse(9.2, 7.2, 5.2, 4.9, shell)
    for k in range(5):
        a = math.radians(-150 + k * 42)
        p.stroke([(9.2 + math.cos(a) * 1.4, 7.2 + math.sin(a) * 1.4), (9.2 + math.cos(a) * 5.0, 7.2 + math.sin(a) * 5.0)], 1.5, 1.9, stripe)
    p.ellipse(8.6, 7.8, 1.5, 1.4, shell)
    p.ellipse(6.2, 9.8, 1.9, 1.6, flesh)
    p.dot(6, 9, eye).dot(9, 7, stripe)
    return p.render(line=line)


def trilobite(body="#7a6a52", ridge="#a8967c", head="#8e7c62", brow="#b4a288", tail="#4e4232", line="#2a2218", eye="#101018"):
    p = Painter()
    p.stroke([(3.0, 13.6), (1.4, 15.0)], 0.9, 0.6, tail)
    p.ellipse(7.4, 8.6, 5.7, 3.6, body, rot=-45)
    p.ellipse(7.4, 8.6, 5.0, 1.3, ridge, rot=-45)
    p.ellipse(10.8, 5.2, 3.0, 3.6, head, rot=-45)
    p.ellipse(11.4, 4.6, 1.5, 1.9, brow, rot=-45)
    p.dot(9, 4, eye).dot(12, 7, eye)
    return bands(p.render(line=line), 2, 0.3, skip=(hexc(eye),))


def krakling():
    p = Painter()
    arms = [((5.0, 8.4), (3.0, 10.6), (3.4, 13.0)), ((6.6, 9.0), (6.0, 11.8), (7.0, 13.8)), ((8.4, 9.0), (9.0, 11.8), (8.4, 14.0)),
            ((10.0, 8.6), (11.4, 11.0), (10.8, 13.4))]
    for i, arm in enumerate(arms):
        p.stroke(list(arm), 1.9, 0.8, "#5a1e4a" if i % 2 else "#7a2a5e")
    # zwei lange Fangarme mit Keulen
    p.stroke([(4.2, 7.6), (1.8, 8.4), (1.2, 11.0)], 1.2, 0.8, "#7a2a5e")
    p.stroke([(11.6, 7.6), (14.0, 8.8), (14.4, 11.4)], 1.2, 0.8, "#7a2a5e")
    p.ellipse(1.4, 12.0, 1.1, 1.4, "#c86a9a")
    p.ellipse(14.4, 12.4, 1.1, 1.4, "#c86a9a")
    p.ellipse(8.0, 5.0, 3.6, 4.0, "#8a3268")
    p.ellipse(6.8, 3.2, 1.5, 1.3, "#c86a9a")
    p.dot(6, 6, "#ffd24a").dot(10, 6, "#ffd24a").dot(6, 7, "#101018").dot(10, 7, "#101018")
    return p.render(line="#2a0c22")


def ray(body, light, edge, dots=(), dot="#fff4b0", eye="#101018", line="#14121c"):
    """Ein Rochen, schräg wie die Fische: die Schwingen quer zur Achse, der dünne Schwanz unten links."""
    p = Painter()
    p.stroke([(7.4, 8.6), (4.4, 11.6), (1.6, 14.4)], 1.5, 0.7, edge)
    p.ellipse(8.9, 7.1, 6.7, 3.0, body, rot=45)          # die Schwingen
    p.ellipse(9.7, 6.3, 4.1, 2.5, body, rot=-45)         # der Leib
    p.ellipse(4.6, 2.9, 1.6, 1.0, edge, rot=45)          # Flügelspitzen
    p.ellipse(13.1, 11.4, 1.6, 1.0, edge, rot=45)
    p.ellipse(9.5, 6.5, 3.0, 1.1, light, rot=-45)        # heller Rückenstreif
    p.stroke([(11.6, 3.4), (13.0, 2.6)], 1.0, 0.8, edge) # Kopfflossen
    p.stroke([(12.6, 4.4), (13.4, 3.0)], 1.0, 0.8, edge)
    p.dot(10, 4, eye).dot(12, 6, eye)
    for (x, y) in dots:
        p.dot(x, y, dot)
    return p.render(line=line)


def crab(body, dark, light, claw=None, eye="#101018", line="#1c1014"):
    """Eine Krabbe von vorn: der Panzer in der Mitte, die Scheren erhoben, drei Beinpaare."""
    p = Painter()
    claw = claw or body
    for k in range(3):
        p.stroke([(5.2 - k * 0.2, 10.0 + k * 0.7), (2.9 - k * 0.1, 11.0 + k * 1.0), (1.9 + k * 0.7, 13.0 + k * 0.6)], 1.2, 0.8, dark)
        p.stroke([(10.8 + k * 0.2, 10.0 + k * 0.7), (13.1 + k * 0.1, 11.0 + k * 1.0), (14.1 - k * 0.7, 13.0 + k * 0.6)], 1.2, 0.8, dark)
    p.stroke([(4.6, 8.4), (2.6, 7.0), (2.8, 5.0)], 1.5, 1.3, dark)
    p.stroke([(11.4, 8.4), (13.4, 7.0), (13.2, 5.0)], 1.5, 1.3, dark)
    p.ellipse(3.3, 3.7, 2.0, 1.6, claw, rot=-35)
    p.ellipse(12.7, 3.7, 2.0, 1.6, claw, rot=35)
    p.ellipse(8.0, 9.6, 4.5, 3.1, body)
    p.ellipse(8.0, 8.5, 3.1, 1.2, light)
    p.stroke([(6.5, 7.2), (6.3, 5.9)], 0.9, 0.9, dark)
    p.stroke([(9.5, 7.2), (9.7, 5.9)], 0.9, 0.9, dark)
    p.dot(6, 5, eye).dot(9, 5, eye)
    p.dot(4, 2, "#00000000").dot(12, 2, "#00000000")
    img = p.render(line=line)
    px = img.load()
    # die Scherenkerbe
    for (x, y) in ((3, 2), (12, 2)):
        if px[x, y][3]:
            px[x, y] = hexc(line)
    return img


def shrimp(body, dark, light, claws=False, eye="#101018", line="#1c1014", feeler=None):
    """Garnele oder Krebs, schräg wie die Fische: der Schwanzfächer unten links, die Fühler oben rechts."""
    p = Painter()
    feeler = feeler or dark
    p.stroke([(11.6, 5.0), (13.6, 2.4), (15.2, 1.6)], 0.9, 0.7, feeler)
    p.stroke([(12.2, 5.8), (14.6, 4.6), (15.4, 5.6)], 0.9, 0.7, feeler)
    for k in range(4):
        p.stroke([(5.6 + k * 1.5, 11.4 - k * 1.5), (7.0 + k * 1.5, 12.8 - k * 1.5)], 0.9, 0.7, dark)
    if claws:
        p.stroke([(10.4, 7.8), (12.6, 9.0)], 1.3, 1.2, dark)
        p.stroke([(9.0, 6.0), (10.2, 3.6)], 1.3, 1.2, dark)
        p.ellipse(13.6, 9.6, 1.7, 1.3, body, rot=30)
        p.ellipse(10.6, 2.6, 1.3, 1.7, body, rot=20)
    p.ellipse(2.6, 13.4, 1.9, 1.5, dark, rot=-45)
    p.stroke([(3.4, 12.6), (5.4, 11.0), (7.6, 9.2), (9.4, 7.2), (10.8, 5.8)], 2.4, 3.6, body)
    p.stroke([(3.8, 11.6), (6.0, 9.6), (8.4, 7.2), (10.2, 5.2)], 0.9, 1.3, light)
    p.ellipse(11.4, 5.2, 1.9, 1.6, body, rot=-45)
    p.dot(12, 4, eye)
    return bands(p.render(line=line), 3, 0.3, skip=(hexc(eye),))


def jelly(bell, dark, light, glow=None, line="#16101c", arms=5):
    """Eine Qualle: die Glocke oben, darunter wehende Fäden."""
    p = Painter()
    xs = (3.6, 5.8, 8.0, 10.2, 12.4) if arms == 5 else (4.4, 6.8, 9.2, 11.6)
    for k, x in enumerate(xs):
        sway = 0.9 if k % 2 else -0.9
        p.stroke([(x, 8.2), (x + sway, 10.4), (x - sway * 0.6, 12.6), (x + sway * 0.8, 14.6 - (k % 2) * 0.8)], 1.2, 0.7, dark if k % 2 else bell)
    p.dome(8.0, 8.6, 5.6, 7.0, bell)
    p.ellipse(7.0, 4.4, 2.6, 1.5, light, rot=-20)
    p.stroke([(3.2, 8.0), (12.8, 8.0)], 1.3, 1.3, dark)
    img = p.render(line=line, shade=0.16)
    if glow:
        px = img.load()
        for (x, y) in ((5, 5), (8, 3), (11, 5), (6, 11), (10, 12)):
            if px[x, y][3]:
                px[x, y] = hexc(glow)
    return img


def snail(shell, stripe, flesh, light, eye="#101018", line="#1c1410"):
    """Eine Schnecke von der Seite: das gewundene Haus, darunter der Fuß, vorn die Fühler."""
    p = Painter()
    p.stroke([(2.0, 13.2), (6.0, 13.6), (11.0, 13.4), (13.6, 12.2)], 2.0, 2.4, flesh)
    p.stroke([(12.6, 11.4), (13.4, 8.8)], 1.0, 0.9, flesh)
    p.stroke([(13.8, 11.8), (15.0, 9.6)], 1.0, 0.9, flesh)
    p.ellipse(7.4, 8.0, 5.0, 4.6, shell)
    p.ellipse(7.6, 8.2, 3.3, 3.0, stripe)
    p.ellipse(7.8, 8.4, 2.1, 1.9, shell)
    p.ellipse(8.0, 8.6, 0.9, 0.8, stripe)
    p.ellipse(5.6, 5.4, 1.7, 1.0, light, rot=-30)
    p.dot(13, 8, eye).dot(15, 9, eye)
    return p.render(line=line)


def starfish(body, dark, light, line="#1c1014", arms=5):
    """Ein Seestern: fünf Arme, gepunktet."""
    p = Painter()
    cx, cy = 8.0, 8.4
    tips = []
    for k in range(arms):
        a = math.radians(-90 + k * 360.0 / arms)
        tips.append((cx + math.cos(a) * 6.6, cy + math.sin(a) * 6.6))
        p.stroke([(cx, cy), tips[-1]], 3.6, 1.0, body)
    p.ellipse(cx, cy, 2.4, 2.4, body)
    p.ellipse(cx - 0.4, cy - 0.5, 1.2, 1.1, light)
    for (tx, ty) in tips:
        p.dot(int(cx + (tx - cx) * 0.55), int(cy + (ty - cy) * 0.55), dark)
    return p.render(line=line)


def olm(body, dark, light, gills, eye="#101018", line="#1c1014"):
    """Molch oder Olm, schräg wie die Fische: der Ruderschwanz unten links, vorn die Kiemenbüschel."""
    p = Painter()
    for (x, y, dx, dy) in ((5.6, 11.6, 1.6, 1.4), (4.6, 10.4, -1.5, -1.3), (10.0, 7.2, 1.6, 1.4), (9.0, 6.0, -1.5, -1.3)):
        p.stroke([(x, y), (x + dx, y + dy)], 1.0, 0.8, dark)
    p.stroke([(1.4, 14.6), (2.8, 13.0), (4.4, 12.0)], 0.9, 2.2, dark)
    p.stroke([(4.0, 12.2), (6.2, 10.4), (8.4, 8.2), (10.4, 6.4)], 2.4, 2.9, body)
    p.stroke([(4.6, 11.2), (7.0, 9.0), (9.6, 6.6)], 0.8, 1.0, light)
    for k, (gx, gy) in enumerate(((9.2, 3.6), (10.2, 2.8), (13.0, 6.8), (13.8, 5.8))):
        p.stroke([(11.2, 5.2), (gx, gy)], 0.9, 0.8, gills)
    p.ellipse(11.8, 5.0, 2.2, 1.8, body, rot=-45)
    p.dot(12, 4, eye)
    return p.render(line=line)


# =============================================================================================
# Die Arten
# =============================================================================================

def species() -> dict:
    f = fish
    return {
        # Süßwasser
        "brook_trout": lambda: f("trout", "#4a5a2e", "#a8924e", "#f2e4b4", "#c8702a", ("spots", "#d4402a", 0.22)),
        "river_perch": lambda: f("perch", "#3e5a28", "#8aa03a", "#f0eab8", "#d8502a", ("bars", "#2a3a1c", 5)),
        "carp": lambda: f("carp", "#5a4a22", "#b08a3a", "#f0dc9a", "#8a5a2a", ("scales", "#7a6026"), barbels=1),
        "pike": lambda: f("pike", "#3a5a2e", "#7a9a48", "#eef0c8", "#b8683a", ("spots", "#e8eaa0", 0.3)),
        "catfish": lambda: f("catfish", "#3a3a40", "#6a6a70", "#d8d4c8", "#4a4a52", ("mottle", "#2a2a30", 0.3), barbels=3),
        "eel": lambda: f("eel", "#2e3a2a", "#5a6a3e", "#d8d49a", "#3a4630"),
        "sturgeon": lambda: f("sturgeon", "#4a4e58", "#8a8e96", "#e8e6dc", "#5a5e68", scutes="#e4e4d8"),
        "goldfish": lambda: f("goldfish", "#e8641a", "#ff9a2a", "#ffe08a", "#ffb44a", ("patches", "#fff4d8", 0.25)),
        "piranha": lambda: f("piranha", "#3a4a5a", "#8a9aa8", "#e03a2a", "#c8302a", ("spots", "#d8e0e8", 0.15), teeth=2),
        "arapaima": lambda: f("arapaima", "#3a4a3a", "#6a8a6a", "#d8e0c0", "#c83a2a", ("tailfade", "#d8402a"), tailcol="#c8302a"),
        "mudskipper": lambda: f("skipper", "#5a4a32", "#8a7a52", "#d8cc9c", "#4a8ab8", ("spots", "#7ad0e8", 0.2), big_eye=True),
        "arctic_char": lambda: f("trout", "#3a5a6a", "#7aa0aa", "#f0704a", "#e85a3a", ("spots", "#ffd0c0", 0.18)),
        "rainbow_trout": lambda: f("trout", "#4a6a4a", "#b8c0b0", "#f4f0e0", "#8a9a8a", ("line", "#e8608a")),
        "desert_pupfish": lambda: f("small", "#2a5a9a", "#5aa8e8", "#f4e0a0", "#e8c44a", ("bars", "#1e3e78", 5), len=11.5),
        "cave_tetra": lambda: f("small", "#e0a8ac", "#f6d2d4", "#fff0f0", "#f0b8c0", eye="#e89aa2", len=12.0),
        "dripstone_loach": lambda: f("pike", "#6a4a32", "#a87a52", "#ecd0a4", "#8a6242", ("bars", "#4a3222", 6), barbels=2, len=15.0),
        "sculk_eel": lambda: f("eel", "#04141c", "#0a2a36", "#0e4a56", "#062028", ("spots", "#1ad0d8", 0.16), eye="#9af8ff", glow="#2ae0e8"),
        "blossom_koi": lambda: f("carp", "#f8a8c8", "#ffe4ee", "#ffffff", "#f080a8", ("patches", "#f0609a", 0.34), barbels=1),
        "gloom_pike": lambda: f("pike", "#1e2a22", "#3a4a3a", "#8a9a80", "#2a3a2e", ("mottle", "#101810", 0.35), eye="#c8e060"),
        "shroom_perch": lambda: f("perch", "#a02a22", "#d84a3a", "#f4e8d8", "#f0e8d8", ("spots", "#fff8f0", 0.3)),
        # Meer
        "herring": lambda: f("small", "#3a5a7a", "#b8c8d8", "#f4f8fc", "#7a8a9a", len=14.0),
        "mackerel": lambda: f("tuna", "#1e5a6a", "#9ac8c0", "#f2f6f0", "#5a7a80", ("bars", "#123c48", 7), finlets="#3a6a70", len=16.0),
        "redfish": lambda: f("perch", "#b02a1e", "#e85a3a", "#ffc8a8", "#d83a22", big_eye=True),
        "plaice": lambda: f("flat", "#6a5a3a", "#8a7a4e", "#a8966a", "#7a6a44", ("spots", "#f07a2a", 0.14)),
        "tuna": lambda: f("tuna", "#1e3a6a", "#7a9ac8", "#eef2f8", "#e8c83a", finlets="#e8c83a"),
        "swordfish": lambda: f("sword", "#28386a", "#6a82b8", "#e8eef8", "#3a4a7a", bill=5.0),
        "sunfish": lambda: f("sunfish", "#56606e", "#a8b2bc", "#eef2f6", "#4a5462", ("spots", "#e0e6ec", 0.2), big_eye=True),
        "seahorse": seahorse,
        "parrotfish": lambda: f("carp", "#1e9a9a", "#3ac8a0", "#f0e86a", "#e85a9a", ("patches", "#e8609a", 0.3)),
        "lionfish": lambda: f("perch", "#8a2a1e", "#f0e4d0", "#fff4e8", "#c84a2a", ("bars", "#8a2a1e", 6), spines="#c84a2a"),
        "octopus": octopus,
        "catshark": lambda: f("shark", "#7a6a4e", "#a8966e", "#ece4cc", "#8a7a58", ("spots", "#3a3020", 0.2)),
        "halibut": lambda: f("flat", "#3e4a3a", "#5a6a52", "#7a8a70", "#4a5a44", ("mottle", "#2a3428", 0.3), len=17.0),
        # Tiefsee
        "lanternfish": lambda: f("small", "#1e2a4a", "#3a4a7a", "#6a7aa8", "#2a3a5a", glow="#7af0ff", big_eye=True),
        "hatchetfish": lambda: f("hatchet", "#3a4250", "#a8b4c4", "#e8f0f8", "#6a7484", glow="#9af4ff", big_eye=True),
        "anglerfish": lambda: f("angler", "#2a1e1a", "#4a342a", "#6a4a3a", "#3a2a22", lure="#8af8ff", teeth=3),
        "viperfish": lambda: f("viper", "#16202e", "#2e4258", "#4a6a88", "#22303e", teeth=3, glow="#6af0e8"),
        "firefly_squid": lambda: squid("#5a3a8a", "#3a2460", "#9a7ad0", glow="#7af8ff"),
        "giant_isopod": isopod,
        "oarfish": lambda: f("ribbon", "#b8c4d0", "#dce4ec", "#f4f8fc", "#e83a3a", ("bars", "#8a98a8", 9), crest="#e83a3a"),
        "ghost_shark": lambda: f("shark", "#5a5a7a", "#9a9ab8", "#e4e4f0", "#6a6a8a", big_eye=True, eye="#3af0c8"),
        "coelacanth": lambda: f("coel", "#1e2e5a", "#3a528a", "#6a82b0", "#2a3e6e", ("spots", "#e8f0ff", 0.16)),
        "vampire_squid": vampire_squid,
        # die Lava des Nethers
        "magma_carp": lambda: f("carp", "#3a1408", "#a83a10", "#ffb02a", "#e85a10", ("scales", "#ffd24a"), eye="#ffe890", glow="#ffe060"),
        "crimson_snapper": lambda: f("perch", "#5a0a12", "#b01e2a", "#f06a5a", "#7a0e1a", ("bars", "#3a060c", 5), teeth=2, eye="#ffd24a"),
        "warp_fin": lambda: f("goldfish", "#0e4a4a", "#16a08a", "#7af0d0", "#3ad8c0", ("patches", "#0a6a6a", 0.3), eye="#e84a8a"),
        "soul_eel": lambda: f("eel", "#1a3a44", "#3a9aa8", "#b8f8ff", "#2a6a78", ("bars", "#7ae8f0", 7), eye="#ffffff", glow="#e8ffff"),
        "basalt_pike": lambda: f("pike", "#1c1c22", "#4a4a54", "#8a8a92", "#2e2e36", ("mottle", "#e85a10", 0.16), eye="#ff8a2a"),
        "ember_ray": lambda: ray("#7a2208", "#e8641a", "#4a1204", dots=((7, 9), (10, 8), (6, 5), (11, 10)), dot="#ffd24a", eye="#ffe890", line="#1e0602"),
        # die Leere des Ends und der Leere
        "ender_salmon": lambda: f("tuna", "#1a0e2a", "#4a2a7a", "#c8a0f0", "#2a1846", ("spots", "#e0b0ff", 0.16), eye="#e87af8"),
        "chorus_fish": lambda: f("round", "#6a4a7a", "#a888b8", "#e8d8f0", "#8a6a9a", ("patches", "#c8a8d8", 0.3), eye="#3a1a4a"),
        "void_ray": lambda: ray("#140e26", "#4a3a8a", "#0a0616", dots=((7, 9), (10, 8), (6, 5), (11, 10), (8, 6)), dot="#e8e0ff", eye="#c88aff", line="#04020a"),
        "stardust_minnow": lambda: f("small", "#1a1a3a", "#3a3a7a", "#8a8ad0", "#2a2a5a", ("spots", "#fff8c0", 0.16), eye="#ffffff", glow="#fff8c0", len=11.5),
        "crystal_fin": lambda: f("perch", "#5a8ad0", "#a8d0f8", "#f0faff", "#d8f0ff", ("scales", "#ffffff"), eye="#2a4a8a", spines="#c8e8ff"),
        # Feenreich, Schattenwelt, Abgrund
        "glimmerfish": lambda: f("small", "#c85aa8", "#f09ad8", "#ffe4f8", "#7ae8d8", glow="#fff0a0", big_eye=True),
        "mushroom_swimmer": lambda: f("round", "#8a2ab0", "#c85ae0", "#f8d8ff", "#5ae8d0", ("spots", "#fff0a0", 0.28), big_eye=True),
        "moonlily_koi": lambda: f("carp", "#d8d0f0", "#f4f0ff", "#ffffff", "#b8a8f0", ("patches", "#8a6ae0", 0.4), barbels=1),
        "thorn_pike": lambda: f("pike", "#2a4a1e", "#5a8a32", "#d8e8a0", "#8a3a5a", ("line", "#c84a7a"), spines="#3a5a22", eye="#e84a8a"),
        "mist_carp": lambda: f("carp", "#4a4a5e", "#8a8aa0", "#d0d0e0", "#6a6a82", ("mottle", "#b8b8d0", 0.3), eye="#e8e8ff"),
        "blind_pike": lambda: f("pike", "#2a2030", "#5a4a68", "#a898b8", "#3a2e46", eye="#6a5878", teeth=3),
        "shadow_eel": lambda: f("eel", "#1a1026", "#3a2456", "#6a48a0", "#2a1840", eye="#d8a0ff", glow="#b46cff"),
        "soulfish": lambda: f("small", "#3a5a6a", "#7ac8d0", "#d8fcff", "#5aa8b8", eye="#ffffff", glow="#e8ffff"),
        "ember_catfish": lambda: f("catfish", "#2a1410", "#6a2a18", "#e8782a", "#3a1c14", ("mottle", "#ff8a2a", 0.2), barbels=3, eye="#ffd24a"),
        "chaos_fin": lambda: f("perch", "#2a1c26", "#5a3a4a", "#ff7a2a", "#a47cf0", ("patches", "#e8541a", 0.3), eye="#ffd24a"),
        "rift_eel": lambda: f("eel", "#1c1420", "#3e2c4a", "#7c4ad0", "#ff6a1a", ("bars", "#a47cf0", 8), eye="#ffe08a", glow="#ccb0ff"),
        # Legenden
        "old_one": lambda: f("carp", "#3a4a2a", "#6a7a3a", "#c8c08a", "#4a5a30", ("mottle", "#8aa04a", 0.4), barbels=3, len=17.8),
        "goldscale": lambda: f("trout", "#c88a1a", "#ffd24a", "#fff4c0", "#e8a020", ("scales", "#fff0a0"), eye="#5a2a0a"),
        "stormsail": lambda: f("sail", "#1e2a6a", "#5a7ae0", "#e0ecff", "#3a4ac8", ("bars", "#9ad8ff", 6), bill=4.5),
        "deep_king": lambda: f("angler", "#1a1228", "#3a2a52", "#5a3a7a", "#2a1c40", lure="#ffd24a", teeth=4, glow="#ffd24a", len=15.5),
        "inferno_king": lambda: f("armored", "#2a0c06", "#8a1e0a", "#ff8a1a", "#e84a0a", plates="#1a0804", teeth=3, eye="#ffe890", glow="#ffe060"),
        "dragon_koi": lambda: f("carp", "#1a1030", "#5a2a9a", "#e0c0ff", "#e87af8", ("scales", "#c89aff"), barbels=3, crest="#ffd24a", eye="#ffd24a",
                                glow="#f0b0ff", len=17.8),
        "moonshine_koi": lambda: f("carp", "#a8c8f0", "#e8f4ff", "#ffffff", "#9ad8ff", ("patches", "#5a9ae8", 0.35), glow="#ffffff", barbels=1),
        # Legenden seit 0.81
        "night_sovereign": lambda: f("catfish", "#0e0a1a", "#2a1a4a", "#6a48a0", "#1a1030", ("mottle", "#b46cff", 0.2), barbels=3, eye="#e8d0ff", glow="#c89aff",
                                     len=17.8),
        "rift_leviathan": lambda: f("moray", "#1c1420", "#4a2c5a", "#ff7a2a", "#a47cf0", ("saddle", "#ff9a3a", 6), teeth=3, crest="#ffd24a", eye="#ffe08a",
                                    glow="#ffb86a"),
        "star_whale": lambda: f("whale", "#141438", "#34348a", "#c8c8f4", "#22225a", ("spots", "#fff8c0", 0.14), glow="#fff8c0", eye="#ffffff"),
        "lightless_one": lambda: olm("#f4e4e0", "#d0b0b0", "#ffffff", "#e83a5a", eye="#f4e4e0", line="#4a2a30"),
        "frost_emperor": lambda: f("sturgeon", "#2a4a8a", "#7ab8f0", "#f0faff", "#b48cf0", ("backline", "#ffffff"), scutes="#ffffff", eye="#101830", glow="#e0f8ff"),
        "reef_queen": lambda: f("goldfish", "#c8287a", "#ff6aa8", "#ffe4a0", "#3ad8d0", ("bars", "#7a1a5a", 4), fintip="#ffe04a", crest="#ffd24a", glow="#fff0a0"),
        "bog_mother": lambda: f("catfish", "#2a3a1e", "#5a6a32", "#c8c88a", "#3a4a26", ("mottle", "#8aa04a", 0.35), barbels=3, eye="#e8e060", len=17.6,
                                body=[(0, .55), (.3, 1.55), (.6, 2.25), (.86, 2.5), (1, 1.45)]),
        "sun_king": lambda: f("arapaima", "#c8701a", "#ffc02a", "#fff4b0", "#e84a1a", ("scales", "#fff0a0"), fintip="#ffe890", eye="#5a1a0a", glow="#fff4b0"),
    }


def later() -> dict:
    """Wesen des Urmeers – sie warten auf ihre Dimension und werden noch nicht geschrieben."""
    return {
        "nautilus": nautilus,
        "trilobite": trilobite,
        "krakling": krakling,
        "primal_fish": lambda: fish("armored", "#4a3a2a", "#7a6648", "#c8b890", "#5a4a36", plates="#3a2e22", teeth=3),
    }


def items() -> dict:
    fillet = ascii_sprite([
        "................",
        "................",
        "..........OOO...",
        "........OOLLMO..",
        "......OOLLMMMO..",
        "....OOLLMMMDMO..",
        "...OLLMMGMMMDO..",
        "..OLMMGGMMMDDO..",
        "..OMMMMMMGDDO...",
        "..OMDMMGGDDO....",
        "..ODDDDDDDO.....",
        "...ODDDDOO......",
        "....OOOO........",
        "................",
        "................",
        "................",
    ], {"O": "#5a3214", "L": "#f8dca0", "M": "#e8b060", "D": "#b8742e", "G": "#8a4a1a"})
    worm = ascii_sprite([
        "................",
        "................",
        "................",
        "..........OOO...",
        ".........OLLMO..",
        "........OLMOMO..",
        ".......OLMO.OO..",
        "......OLMO......",
        ".....OMMO.......",
        "....OMDO........",
        "...OMDO..OO.....",
        "...OMDOOOMMO....",
        "...ODMMMMDDO....",
        "....ODDDDOO.....",
        ".....OOOO.......",
        "................",
    ], {"O": "#4a1e22", "L": "#f0a0a0", "M": "#d87078", "D": "#a84a56"})
    glow = ascii_sprite([
        "................",
        "......G..G......",
        "...G........G...",
        "......OOOO......",
        ".....OHHLLO.....",
        "..G.OHHLLLMO.G..",
        "....OHLLLMMO....",
        "....OLLLMMMO....",
        "....OLLMMMDO....",
        ".....OMMMDO.....",
        "......OOOO......",
        "...G...OO...G...",
        ".......OSO......",
        "......OSO.......",
        "......OO........",
        "................",
    ], {"O": "#1e4a4a", "H": "#f0ffff", "L": "#9af8f0", "M": "#4ad8d0", "D": "#2a9a9a", "G": "#c8fffa", "S": "#b8b8c0"})
    # der Fischbeutel (seit 0.81): ein Beutel aus Segeltuch mit Zugkordel, aus dem ein Fischschwanz ragt
    bag = ascii_sprite([
        "................",
        "..........O..O..",
        ".........OTOOTO.",
        "....OO...OTTTTO.",
        "...ORROO..OTtO..",
        "...OrRRROOOtO...",
        "....OOoooottO...",
        "...OHLLLLLMO....",
        "..OHLLLLLLMDO...",
        "..OLLLLFFLMDO...",
        ".OHLFLFFFFLMDO..",
        ".OLLFFFFFfFMDO..",
        ".OLLFLFFFFMDDO..",
        ".OLMMMMFFMDDDO..",
        "..OMDDDDDDDDO...",
        "...OOOOOOOOO....",
    ], {"O": "#1e2a26", "H": "#a8d0b8", "L": "#7aae96", "M": "#568a76", "D": "#3a6456", "o": "#284038", "R": "#e8c88a", "r": "#b08a4a",
        "T": "#b8d8ec", "t": "#6a9ac0", "F": "#f0f0d8", "f": "#2a4a40"})
    return {"item/fish_fillet_cooked": fillet, "item/bait_worm": worm, "item/bait_glow": glow, "item/fish_bag": bag}


def build_all() -> dict:
    out = {}
    for name, make in species().items():
        out[f"item/fish/{name}"] = make()
    out.update(items())
    return out


def build(put) -> None:
    """Für tools/generate_textures.py."""
    for key, img in build_all().items():
        put(key, img)


def preview(target: str) -> None:
    imgs = build_all()
    names = [n for n in imgs if n.startswith("item/")]
    cols = 9
    cell = 16 * 6 + 12
    rows = (len(names) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * cell + 12, rows * cell * 2 + 24), (40, 44, 52, 255))
    for half, bg in enumerate(((198, 178, 138, 255), (36, 62, 96, 255))):
        for i, n in enumerate(names):
            x, y = 12 + (i % cols) * cell, 12 + (i // cols) * cell + half * rows * cell
            tile = Image.new("RGBA", (96, 96), bg)
            tile.alpha_composite(imgs[n].resize((96, 96), Image.NEAREST))
            sheet.alpha_composite(tile, (x, y))
    sheet.save(target)
    print(target, len(names), "Bilder")


def main() -> None:
    if "--preview" in sys.argv:
        preview(sys.argv[sys.argv.index("--preview") + 1])
        return
    for key, img in build_all().items():
        path = os.path.join(TEX, key + ".png")
        os.makedirs(os.path.dirname(path), exist_ok=True)
        img.save(path)
    print(len(build_all()), "Bilder geschrieben")


if __name__ == "__main__":
    main()
