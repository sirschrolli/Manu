"""
Der Weltenfresser: eine kolossale Schlange aus dem Abgrund, die Bruchstücke ganzer Welten verschlingt.

  * Kopf: ein schwerer Schädel aus Chaosgestein-Platten, durch deren Fugen es glüht; vorn ein runder Rachen wie bei einem
    Neunauge – acht gezahnte Lippen, die sich wie eine Blüte öffnen, dahinter ein Ring aus Zähnen und ein glühender
    Schlund, in dem ein Riss wogt; sechs weißglühende Augen, zwei gebogene Hauer. Aus Stirn und Nacken ragen noch die
    sieben **Kronensplitter**, mit denen der Zerbrochene König ihn einst versiegelte.
  * Leibglied: gepanzerte Ringe mit glühender Naht vorn; auf dem Rücken trägt jedes ein Stück einer verschlungenen Welt
    (Wiese mit Baum, Netherrack mit Magma, Endstein mit Chorus, Eis mit Zacken) – der Renderer zeigt je Glied eines davon.
  * Schwanz: läuft spitz zu und endet in einer Flosse aus Kristall.
  * Weltbrocken: was er ausspeit – ein grober Brocken in vier Arten (Glut, Frost, End, Schatten).

Der Kopf blickt nach vorn (−z); alle Teile werden im Spiel doppelt so groß gezeichnet.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, Voronoi3D, hash01, hexc, mix, noise3, pick, rel_y

_EATER = {
    "plate": [hexc(c) for c in ("#1c151a", "#2b2128", "#3b2f37", "#4d3f48", "#61515b", "#776670", "#907d88")],
    "belly": [hexc(c) for c in ("#2a1c1c", "#3a2724", "#4e3630", "#64473c", "#7c5a4a", "#94705a")],
    "seam": [hexc(c) for c in ("#5a0a04", "#a8240a", "#e8541a", "#ff9a3a", "#ffd88a", "#fff6dc")],
    "crystal": [hexc(c) for c in ("#3a1a6a", "#5a2ea0", "#7c4ad0", "#a47cf0", "#ccb0ff", "#f0e6ff")],
    "gold": [hexc(c) for c in ("#5a3a10", "#8a5c18", "#c08a2a", "#e8b84a", "#fff0a0")],
    "tooth": [hexc(c) for c in ("#5a4c3c", "#8a7a64", "#b8a88c", "#ddd0b4", "#f6eedc")],
    "gum": [hexc(c) for c in ("#2a0608", "#4a0c10", "#6e1418", "#922228", "#b83a36")],
    "throat": [hexc(c) for c in ("#3a0204", "#7a0a08", "#c8240c", "#ff6a1a", "#ffb04a", "#fff0c0")],
    "eye": [hexc(c) for c in ("#a02a00", "#ff7a12", "#ffd24a", "#fff6c8", "#ffffff")],
    # die verschlungenen Welten auf dem Rücken
    "grass": [hexc(c) for c in ("#2c4a1a", "#3e6424", "#548232", "#6ea042", "#8cbc58")],
    "dirt": [hexc(c) for c in ("#3a2616", "#4e3420", "#64442c", "#7a563a", "#906a4a")],
    "wood": [hexc(c) for c in ("#2e2012", "#46301c", "#5e4228", "#765636")],
    "leaves": [hexc(c) for c in ("#1a3410", "#26481a", "#346024", "#467a30", "#5c943e")],
    "netherrack": [hexc(c) for c in ("#3a0e0e", "#5a1616", "#7a2020", "#962c28", "#b04036")],
    "fungus": [hexc(c) for c in ("#5a0a0a", "#8a1212", "#b81e1a", "#d83a2a")],
    "endstone": [hexc(c) for c in ("#8a8456", "#a8a26c", "#c4be86", "#dcd6a0", "#eeeabc")],
    "chorus": [hexc(c) for c in ("#3e2048", "#5a3266", "#7a4a86", "#9a68a6", "#c098c8")],
    "ice": [hexc(c) for c in ("#4a6ea8", "#6a92cc", "#8eb4e6", "#b4d2f6", "#dcecff")],
}

PALETTES = {"world_eater": _EATER}

# der Weltbrocken in vier Arten: (Gestein, Glut)
_CHUNKS = {
    "glut": ("netherrack", "seam"),
    "frost": ("ice", "ice"),
    "end": ("endstone", "crystal"),
    "shadow": ("plate", "crystal"),
}
CHUNK_PALETTES = {name: dict(_EATER, rock=_EATER[rock], hot=_EATER[hot]) for name, (rock, hot) in _CHUNKS.items()}

LIPS = 8
LIP_RING = 11.0
MAW_Z = -24.0


def build() -> Model:
    """Der Kopf."""
    m = Model("world_eater", 512, 512)
    m.bone("head", pivot=(0, 0, 0))
    # Schädel: ein achteckiger Kern (zweimal derselbe Quader, einer um 45° gedreht), Stirnwülste, Nacken, Kehle
    m.cube("head", (-18, -16, -12), (36, 32, 28), "plate")
    m.bone("skull_oct", "head", pivot=(0, 0, 0), rotation=(0, 0, 45))
    m.cube("skull_oct", (-17, -17, -11), (34, 34, 26), "plate")
    m.cube("head", (-14, -13, 15), (28, 26, 7), "plate")
    m.cube("head", (-10, -9, 21), (20, 18, 4), "plate")
    m.cube("head", (-13, 14, -10), (26, 5, 24), "belly")
    for sx in (1, -1):
        brow = f"brow_{'l' if sx > 0 else 'r'}"
        m.bone(brow, "head", pivot=(9.0 * sx, -17.0, -14.0), rotation=(12, 0, 16 * sx))
        m.cube(brow, (-9.0, -4.0, -2.0), (18.0, 6.0, 24.0), "plate")
    for sx in (1, -1):
        x0 = 17.0 if sx > 0 else -23.0
        m.cube("head", (x0, -11, -15), (6, 20, 20), "plate")
        # sechs Augen: drei je Seite, schräg hintereinander unter dem Stirnwulst
        for k in range(3):
            ex = 22.9 if sx > 0 else -23.9
            m.cube("head", (ex, -9.0 + k * 3.4, -13.5 + k * 4.0), (1.0, 2.2, 3.0), "eye")
    # das Gehäuse des Rachens: ein achteckiger Ring um die Öffnung (innen rohes Fleisch), dahinter eine glühende Naht
    for bone in ("head", "snout_oct"):
        if bone == "snout_oct":
            m.bone("snout_oct", "head", pivot=(0, 0, 0), rotation=(0, 0, 45))
        m.cube(bone, (-15, -15, -22), (30, 7, 10), {"bottom": "gum", "*": "plate"})
        m.cube(bone, (-15, 8, -22), (30, 7, 10), {"top": "gum", "*": "plate"})
        m.cube(bone, (8, -8, -22), (7, 16, 10), {"west": "gum", "*": "plate"})
        m.cube(bone, (-15, -8, -22), (7, 16, 10), {"east": "gum", "*": "plate"})
    m.cube("head", (-16.5, -16.5, -12.6), (33, 33, 0.6), "seam")
    # der Schlund tief in der Öffnung: glühend, davor ein Ring aus Zähnen
    m.cube("head", (-8, -8, -13.0), (16, 16, 1.0), "throat")
    for k in range(12):
        a = 2 * math.pi * k / 12
        r = 6.4
        m.cube("head", (math.cos(a) * r - 0.9, math.sin(a) * r - 0.9, -19.0), (1.8, 1.8, 3.0), "tooth")
    # die acht Lippen: je ein Drehgelenk am Ring (öffnen: um die eigene x-Achse)
    for k in range(LIPS):
        a = 360.0 * k / LIPS
        base = f"lip{k}_base"
        m.bone(base, "head", pivot=(0, 0, MAW_Z + 2.0), rotation=(0, 0, a))
        lip = f"lip{k}"
        m.bone(lip, base, pivot=(0, -LIP_RING, 0), rotation=(-18, 0, 0))
        mat = {"bottom": "gum", "*": "plate"}
        m.cube(lip, (-4.6, -2.6, -9.0), (9.2, 3.4, 9.0), mat)
        m.cube(lip, (-3.0, -2.2, -14.0), (6.0, 2.8, 5.0), mat)
        m.cube(lip, (-1.4, -1.8, -17.0), (2.8, 2.2, 3.0), "plate")
        for j, z in enumerate((-4.0, -10.0)):
            for side in (-1, 1):
                m.cube(lip, (side * 2.0 - 0.8, 0.6, z), (1.6, 3.2 - j * 0.6, 1.6), "tooth")
    # zwei gebogene Hauer unten an den Seiten
    for side, sx in (("left", 1), ("right", -1)):
        tusk = f"tusk_{side}"
        m.bone(tusk, "head", pivot=(13.5 * sx, 11.0, -20.0), rotation=(28, 14 * sx, 6 * sx))
        m.cube(tusk, (-2.0, -2.0, -10.0), (4.0, 4.0, 10.0), "tooth")
        tip = f"tusk_{side}_tip"
        m.bone(tip, tusk, pivot=(0, 0, -10.0), rotation=(-34, 0, 0))
        m.cube(tip, (-1.5, -1.5, -8.0), (3.0, 3.0, 8.0), "tooth")
        m.cube(tip, (-0.8, -0.8, -12.0), (1.6, 1.6, 4.0), "tooth")
    # die sieben Kronensplitter: aus Stirn und Nacken, schräg nach hinten und außen
    shards = [(0, -19, -4, (-38, 0, 0), 22), (9, -19, 0, (-30, -18, -22), 17), (-9, -19, 0, (-30, 18, 22), 18),
              (15, -16, 8, (-20, -30, -40), 13), (-15, -16, 8, (-22, 30, 40), 14), (5, -18, 13, (-58, -10, -10), 12),
              (-6, -18, 14, (-62, 12, 12), 11)]
    for i, (x, y, z, rot, length) in enumerate(shards):
        b = f"shard{i}"
        m.bone(b, "head", pivot=(x, y, z), rotation=rot)
        w = 4.4 if length > 16 else 3.6
        m.cube(b, (-w / 2 - 0.8, -2.2, -w / 2 - 0.8), (w + 1.6, 2.2, w + 1.6), "gold")       # goldene Fassung
        m.cube(b, (-w / 2, -length, -w / 2), (w, length, w), "crystal")
        m.cube(b, (-w / 4, -length - 3.0, -w / 4), (w / 2, 3.0, w / 2), "crystal")
    return m


SEG_LEN = 32.0
WORLDS = ("overworld", "nether", "end", "frost")


def build_segment() -> Model:
    """Ein Leibglied mit den vier möglichen Weltstücken auf dem Rücken (je Glied ist nur eines zu sehen)."""
    m = Model("world_eater_segment", 512, 256)
    m.bone("body", pivot=(0, 0, 0))
    h = SEG_LEN / 2
    m.cube("body", (-15, -15, -h), (30, 30, SEG_LEN), "plate")
    m.bone("body_oct", "body", pivot=(0, 0, 0), rotation=(0, 0, 45))
    m.cube("body_oct", (-14.5, -14.5, -h + 1), (29, 29, SEG_LEN - 2), "plate")
    m.cube("body", (-17, -19, -h + 2), (34, 5, SEG_LEN - 4), "plate")
    m.cube("body", (-13, 14, -h + 2), (26, 4, SEG_LEN - 4), "belly")
    for sx in (1, -1):
        x0 = 15.0 if sx > 0 else -19.0
        m.cube("body", (x0, -12, -h + 3), (4, 22, SEG_LEN - 6), "plate")
        # zwei kleine Kristalle seitlich (das Siegel wächst mit)
        cb = f"side_crystal_{'l' if sx > 0 else 'r'}"
        m.bone(cb, "body", pivot=(18.0 * sx, -6.0, 2.0), rotation=(-20, 0, -58 * sx))
        m.cube(cb, (-1.6, -9.0, -1.6), (3.2, 9.0, 3.2), "crystal")
    # die glühende Naht vorn: ein Ring, der an das vordere Glied anschließt
    _seam_ring(m, "body", 16.0, -h - 0.6, 1.6)
    # Weltstücke auf dem Rücken
    m.bone("world_overworld", "body", pivot=(0, -19, 0), rotation=(4, 8, -6))
    m.cube("world_overworld", (-11, -7, -11), (22, 7, 22), {"top": "grass", "*": "dirt"})
    m.cube("world_overworld", (-1.5, -19, -1.5), (3, 12, 3), "wood")
    m.cube("world_overworld", (-7, -28, -7), (14, 10, 14), "leaves")
    m.cube("world_overworld", (-4, -31, -4), (8, 3, 8), "leaves")
    m.bone("world_nether", "body", pivot=(0, -19, 0), rotation=(-5, -10, 7))
    m.cube("world_nether", (-11, -7, -11), (22, 7, 22), "netherrack")
    m.cube("world_nether", (-6, -8, -5), (7, 1, 7), "seam")
    m.cube("world_nether", (3, -15, 1), (2, 8, 2), "fungus")
    m.cube("world_nether", (-1, -19, -3), (10, 4, 10), "fungus")
    m.bone("world_end", "body", pivot=(0, -19, 0), rotation=(6, 14, 5))
    m.cube("world_end", (-11, -7, -11), (22, 7, 22), "endstone")
    m.cube("world_end", (-1.5, -21, -1.5), (3, 14, 3), "chorus")
    m.cube("world_end", (-7.5, -16, -1.5), (6, 3, 3), "chorus")
    m.cube("world_end", (-7.5, -22, -1.5), (3, 6, 3), "chorus")
    m.cube("world_end", (1.5, -18, -1.5), (5, 3, 3), "chorus")
    m.cube("world_end", (3.5, -24, -1.5), (3, 6, 3), "chorus")
    m.cube("world_end", (-2.5, -25, -2.5), (5, 4, 5), "crystal")
    m.bone("world_frost", "body", pivot=(0, -19, 0), rotation=(-4, -16, -5))
    m.cube("world_frost", (-11, -7, -11), (22, 7, 22), "ice")
    m.cube("world_frost", (-6, -22, -4), (5, 15, 5), "ice")
    m.cube("world_frost", (2, -16, 1), (4, 9, 4), "ice")
    m.cube("world_frost", (-1, -27, -2.5), (3, 6, 3), "ice")
    return m


def _seam_ring(m: Model, bone: str, r: float, z: float, depth: float) -> None:
    """Eine glühende Fuge als Ring (vier Leisten) um den Leib bei {@code z}."""
    w = 2.0
    m.cube(bone, (-r - 0.5, -r - 0.5, z), (2 * r + 1, w, depth), "seam")
    m.cube(bone, (-r - 0.5, r - 1.5, z), (2 * r + 1, w, depth), "seam")
    m.cube(bone, (-r - 0.5, -r + 1.5, z), (w, 2 * r - 3, depth), "seam")
    m.cube(bone, (r - 1.5, -r + 1.5, z), (w, 2 * r - 3, depth), "seam")


def build_tail() -> Model:
    """Der Schwanz: drei schlanker werdende Stücke und eine Flosse aus Kristall."""
    m = Model("world_eater_tail", 256, 256)
    m.bone("tail", pivot=(0, 0, 0))
    m.cube("tail", (-13, -13, -16), (26, 26, 20), "plate")
    m.bone("tail_oct", "tail", pivot=(0, 0, 0), rotation=(0, 0, 45))
    m.cube("tail_oct", (-12.5, -12.5, -15), (25, 25, 18), "plate")
    m.cube("tail", (-14, -16.5, -14), (28, 4, 16), "plate")
    _seam_ring(m, "tail", 14.0, -16.6, 1.4)
    m.bone("tail2", "tail", pivot=(0, 0, 4), rotation=(4, 0, 0))
    m.cube("tail2", (-9, -9, 0), (18, 18, 16), "plate")
    m.cube("tail2", (-10, -11.5, 2), (20, 3, 12), "plate")
    m.bone("tail3", "tail2", pivot=(0, 0, 16), rotation=(6, 0, 0))
    m.cube("tail3", (-5.5, -5.5, 0), (11, 11, 14), "plate")
    m.bone("fin", "tail3", pivot=(0, 0, 14), rotation=(0, 0, 0))
    m.cube("fin", (-1.2, -18, 0), (2.4, 36, 8), "crystal")
    m.cube("fin", (-1.2, -12, 8), (2.4, 24, 8), "crystal")
    m.cube("fin", (-1.2, -6, 16), (2.4, 12, 6), "crystal")
    m.cube("fin", (-3.0, -3.0, -1), (6, 6, 6), "gold")
    return m


def build_chunk() -> Model:
    """Ein ausgespiener Weltbrocken: ein grober Klumpen mit glühenden Rissen."""
    m = Model("world_chunk", 128, 64)
    m.bone("chunk", pivot=(0, 0, 0))
    m.cube("chunk", (-6, -6, -6), (12, 12, 12), "rock")
    m.cube("chunk", (-8, -4, -3), (3, 7, 6), "rock")
    m.cube("chunk", (5, -5, -4), (3, 8, 7), "rock")
    m.cube("chunk", (-4, -8.5, -4), (8, 3, 7), "rock")
    m.cube("chunk", (-3, 5, -5), (7, 3, 8), "rock")
    m.cube("chunk", (-4, -3, 5), (6, 6, 3), "rock")
    m.cube("chunk", (-1.5, -1.5, -7), (3, 3, 1.5), "hot")
    return m


# =============================================================================================
# Bemalung
# =============================================================================================

_LIT = {"top": 0.12, "front": 0.04, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.2}
_TILES = Voronoi3D(5.5, 91)


def _rs_rt(ctx: Ctx) -> tuple[float, float]:
    return (ctx.s + 0.5) / max(ctx.w, 1), (ctx.t + 0.5) / max(ctx.h, 1)


def _plate(ctx: Ctx):
    """
    Panzer aus Chaosgestein: schräg gestaffelte Platten mit hellem Grat und dunkler Fuge; in manchen Fugen glüht es
    (als liefe dort eine Weltennaht), dazu feine Splitter fremder Welten im Stein.
    """
    x, y, z = ctx.world
    vein = noise3(x * 0.09, y * 0.09, z * 0.09, 93)
    if abs(vein - 0.5) < 0.018:
        col = pick(ctx.pal["seam"], 0.62 + (hash01(ctx.u, ctx.v, 89) - 0.5) * 0.2)     # eine glühende Weltennaht
        ctx.glow = col
        return col
    d1, d2 = _TILES(x, y, z)
    edge = d2 - d1
    v = 0.56 + _LIT[ctx.face] + (noise3(x * 0.4, y * 0.4, z * 0.4, 92) - 0.5) * 0.16
    if edge < 0.45:
        return pick(ctx.pal["plate"], v - 0.32)                    # die Fuge zwischen zwei Platten
    if edge < 1.0:
        v += 0.14                                                   # heller Grat an der Plattenkante
    r = hash01(ctx.u, ctx.v, 94)
    if r > 0.985:
        return pick(ctx.pal["crystal"], 0.5)
    if r > 0.975:
        return pick(ctx.pal["netherrack"], 0.6)
    return pick(ctx.pal["plate"], v)


def _belly(ctx: Ctx):
    """Bauchplatten: quer gerippt, heller, mit dunklen Fugen."""
    x, y, z = ctx.world
    band = (z * 0.25) % 1.0
    v = 0.48 + _LIT[ctx.face] + (noise3(x * 0.5, y * 0.5, z * 0.5, 95) - 0.5) * 0.14
    if band < 0.12:
        v -= 0.3
    elif band > 0.85:
        v += 0.1
    return pick(ctx.pal["belly"], v)


def _seam(ctx: Ctx):
    rs, rt = _rs_rt(ctx)
    d = max(abs(rs - 0.5), abs(rt - 0.5))
    col = pick(ctx.pal["seam"], 0.9 - d * 0.6 + (hash01(ctx.u, ctx.v, 96) - 0.5) * 0.15)
    ctx.glow = col
    return col


def _crystal(ctx: Ctx):
    """Kronensplitter: violett, zur Spitze heller, mit hellen Kanten; die Mitte glimmt."""
    rs, rt = _rs_rt(ctx)
    edge = rs < 0.18 or rs > 0.82
    tip = 1.0 - rel_y(ctx)
    v = 0.36 + tip * 0.42 + (0.22 if edge else 0.0) + _LIT[ctx.face] * 0.5 + (hash01(ctx.u, ctx.v, 97) - 0.5) * 0.08
    col = pick(ctx.pal["crystal"], v)
    if not edge and hash01(ctx.u // 2, ctx.v // 3, 98) > 0.55:
        ctx.glow = mix((0, 0, 0, 255), col, 0.55 + tip * 0.4)
    return col


def _gold(ctx: Ctx):
    rs, rt = _rs_rt(ctx)
    v = 0.55 + _LIT[ctx.face] + (0.2 if abs(rt - 0.5) < 0.15 else 0.0) - (0.15 if rs < 0.12 or rs > 0.88 else 0.0)
    return pick(ctx.pal["gold"], v)


def _tooth(ctx: Ctx):
    return pick(ctx.pal["tooth"], 0.72 + _LIT[ctx.face] - rel_y(ctx) * 0.1 + (hash01(ctx.u, ctx.v, 99) - 0.5) * 0.12)


def _gum(ctx: Ctx):
    """Das Innere der Lippen: dunkelrotes Fleisch mit glühenden Adern zum Schlund hin."""
    x, y, z = ctx.world
    vein = noise3(x * 0.6, y * 0.6, z * 0.6, 100)
    if abs(vein - 0.5) < 0.04:
        col = pick(ctx.pal["seam"], 0.5)
        ctx.glow = mix((0, 0, 0, 255), col, 0.7)
        return col
    return pick(ctx.pal["gum"], 0.45 + (noise3(x, y, z, 101) - 0.5) * 0.3)


def _throat(ctx: Ctx):
    """Der Schlund: Ringe aus Glut um einen weißen Kern – dort wogt der Riss."""
    rs, rt = _rs_rt(ctx)
    d = math.hypot(rs - 0.5, rt - 0.5) * 2.0
    ring = 0.5 + 0.5 * math.sin(d * 14.0)
    v = 1.0 - d * 0.75 + ring * 0.12 + (hash01(ctx.u, ctx.v, 102) - 0.5) * 0.08
    col = pick(ctx.pal["throat"], v)
    ctx.glow = col
    return col


def _eye(ctx: Ctx):
    rs, rt = _rs_rt(ctx)
    d = max(abs(rs - 0.5), abs(rt - 0.5)) * 2.0
    col = pick(ctx.pal["eye"], 1.0 - d * 0.7)
    ctx.glow = col
    return col


def _noisy(key: str, base: float = 0.5, amp: float = 0.3, scale: float = 0.8, seed: int = 110):
    def paint(ctx: Ctx):
        x, y, z = ctx.world
        return pick(ctx.pal[key], base + _LIT[ctx.face] + (noise3(x * scale, y * scale, z * scale, seed) - 0.5) * amp
                    + (hash01(ctx.u, ctx.v, seed + 1) - 0.5) * 0.12)
    return paint


def _grass(ctx: Ctx):
    return _noisy("grass", 0.55, 0.35, 0.9, 111)(ctx)


def _dirt(ctx: Ctx):
    # oben ein Grassaum wie beim Grasblock
    if ctx.face not in ("top", "bottom") and ctx.t < 2:
        return _noisy("grass", 0.45, 0.3, 0.9, 112)(ctx)
    return _noisy("dirt", 0.5, 0.35, 0.9, 113)(ctx)


def _wood(ctx: Ctx):
    rs, _ = _rs_rt(ctx)
    return pick(ctx.pal["wood"], 0.5 + _LIT[ctx.face] - (0.15 if int(rs * 3) == 1 else 0.0))


def _netherrack(ctx: Ctx):
    x, y, z = ctx.world
    if hash01(ctx.u, ctx.v, 114) > 0.96:
        col = pick(ctx.pal["seam"], 0.55)
        ctx.glow = mix((0, 0, 0, 255), col, 0.6)
        return col
    return _noisy("netherrack", 0.5, 0.45, 1.1, 115)(ctx)


def _fungus(ctx: Ctx):
    if ctx.face == "top" and hash01(ctx.u, ctx.v, 116) > 0.85:
        col = pick(ctx.pal["seam"], 0.7)
        ctx.glow = mix((0, 0, 0, 255), col, 0.5)
        return col
    return _noisy("fungus", 0.5, 0.3, 1.0, 117)(ctx)


def _rock(ctx: Ctx):
    """Weltbrocken: das Gestein seiner Welt, durchzogen von glühenden Rissen in ihrer Farbe."""
    x, y, z = ctx.world
    crack = noise3(x * 0.35, y * 0.35, z * 0.35, 120)
    if abs(crack - 0.5) < 0.02:
        col = pick(ctx.pal["hot"], 0.75)
        ctx.glow = col
        return col
    return pick(ctx.pal["rock"], 0.5 + _LIT[ctx.face] + (noise3(x, y, z, 121) - 0.5) * 0.4)


def _hot(ctx: Ctx):
    col = pick(ctx.pal["hot"], 0.85 + (hash01(ctx.u, ctx.v, 122) - 0.5) * 0.2)
    ctx.glow = col
    return col


def materials(variant: str) -> dict:
    return {"plate": _plate, "belly": _belly, "seam": _seam, "crystal": _crystal, "gold": _gold, "tooth": _tooth, "gum": _gum,
            "throat": _throat, "eye": _eye, "grass": _grass, "dirt": _dirt, "wood": _wood,
            "leaves": _noisy("leaves", 0.5, 0.45, 1.2, 118), "netherrack": _netherrack, "fungus": _fungus,
            "endstone": _noisy("endstone", 0.55, 0.35, 1.0, 119), "chorus": _noisy("chorus", 0.5, 0.35, 1.2, 123),
            "ice": _noisy("ice", 0.55, 0.3, 0.7, 124), "rock": _rock, "hot": _hot}
