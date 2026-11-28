"""
Die Sternenweberin (0.99): die Herrin des Sternennetzes in der Leere. Als die Welten zerbrachen, fing sie die fallenden
Sterne in ihrem Netz – seither webt sie aus ihrem Licht neue Sternbilder. Im Spiel doppelt so groß (gut sechs Blöcke von
Fuß zu Fuß).

  * eine riesige Spinne: der Vorderleib aus nachtblauem, schimmerndem Chitin mit feinen, strahlenförmigen Rillen und
    silbernen Rändern; vorn acht Augen wie Sterne (zwei große in der Mitte), darunter die Kieferklauen mit silbernen Giftzähnen
    und zwei Taster
  * der Hinterleib groß und rund wie ein Stück Nachthimmel: oben eine Spiralgalaxie (violett und blau, in der Mitte ein
    rosaweißer Kern), überall Sterne; aus dem Rücken wachsen drei Kristalle der Leere; hinten die Spinnwarzen, aus denen
    Sternenseide leuchtet
  * acht lange, dünne Beine aus drei Gliedern (Schenkel, Schiene, Fuß): nachtblau mit silbernen Bändern, die Gelenke
    leuchten im Licht der Sterne

Boden-Regel: y = 24 ist der Boden, oben ist -y, vorn ist -z. Die Knochen-Namen verwendet StarWeaverModel (Java):
body (Vorderleib, Augen) > abdomen (Hinterleib) > crystal_0..2, spinnerets; body > fangs > fang_l / fang_r; body > palp_l,
palp_r (> *_tip); die Beine hängen an der Wurzel: leg_<l|r><1-4> (Hüfte, dreht um y) > _femur > _tibia > _tarsus.
"""
from __future__ import annotations

import math

from ..geometry import Model
from ..paint import Ctx, hash01, hexc, mix, noise3, pick, rel_y

PALETTES = {
    "star_weaver": {
        "chitin": [hexc(c) for c in ("#06070e", "#0b0e1c", "#12172e", "#1a2142", "#232c56", "#2f3b6c", "#3f4e86", "#5466a2",
                                     "#7286c0")],
        "silver": [hexc(c) for c in ("#2a2f3e", "#454c60", "#656e86", "#8b94aa", "#b3bacb", "#dbe0ea", "#f6f8fc")],
        "night": [hexc(c) for c in ("#04040b", "#08091a", "#0e1029", "#151839", "#1d214b", "#272b60")],
        "nebula": [hexc(c) for c in ("#26124a", "#3d1c6e", "#5a2c94", "#8048bc", "#ab76dc", "#d8b0f4", "#f6e8ff")],
        "nebula_blue": [hexc(c) for c in ("#0f2a52", "#18447c", "#2462a8", "#438cd0", "#78b8ea", "#bce0fa")],
        "star": [hexc(c) for c in ("#6a8ad0", "#9ab8ee", "#cadcff", "#eef4ff", "#ffffff")],
        "glow": [hexc(c) for c in ("#2462c8", "#4486ec", "#74aeff", "#acd2ff", "#dfeeff", "#ffffff")],
        "crystal": [hexc(c) for c in ("#2a1c64", "#43309a", "#6450c8", "#8a78e4", "#b6a6f4", "#e2d8ff")],
        "core": [hexc(c) for c in ("#7a3a8a", "#b45aa8", "#e48ac4", "#ffc4e4", "#fff0f8")],
    }
}

_LIT = {"top": 0.1, "front": 0.03, "west": 0.0, "east": -0.03, "back": -0.06, "bottom": -0.16}

# Beine: Hüfte (x, z am Vorderleib), Drehung um y (links; rechts gespiegelt), Längen von Schenkel, Schiene und Fuß
HIP_Y = 15.0
LEGS = [
    (4.6, -4.6, 54.0, 12.5, 14.5, 8.0),
    (5.0, -1.6, 24.0, 11.5, 13.5, 7.5),
    (5.0, 1.4, -10.0, 10.0, 12.0, 7.0),
    (4.6, 4.2, -40.0, 12.0, 14.0, 8.0),
]
FEMUR_UP = 38.0       # der Schenkel steigt so steil an (Grad)
TIBIA_DOWN = 52.0     # die Schiene fällt so steil ab
THICK = (2.6, 2.0, 1.3)
# der Hinterleib: Halbachsen und Mitte (im Knochen "abdomen")
ABDOMEN_RX, ABDOMEN_RY, ABDOMEN_RZ, ABDOMEN_Z = 7.6, 7.6, 8.8, 8.4


def tarsus_down(femur: float, tibia: float, tarsus: float) -> float:
    """Wie steil der Fuß abfallen muss, damit seine Spitze gerade den Boden berührt (Grad)."""
    height = (24.0 - HIP_Y) + femur * math.sin(math.radians(FEMUR_UP)) - tibia * math.sin(math.radians(TIBIA_DOWN))
    return math.degrees(math.asin(max(0.35, min(1.0, height / tarsus))))


def _spike(m: Model, parent: str, name: str, pivot, rot, height: float, width: float, material: str) -> None:
    """Ein Kristall: breiter Grund, schmale Spitze (wächst nach oben, -y)."""
    m.bone(name, parent, pivot, rot)
    m.cube(name, (-width / 2, -height * 0.6, -width / 2), (width, height * 0.6, width), material, tag="crystal:0")
    m.bone(name + "_tip", name, (0.0, -height * 0.6 + 0.2, 0.0), (0.0, 0.0, 0.0))
    w2 = width * 0.55
    m.cube(name + "_tip", (-w2 / 2, -height * 0.45, -w2 / 2), (w2, height * 0.45, w2), material, tag="crystal:1")


def build() -> Model:
    m = Model("star_weaver", 256, 256)
    m.tex_res = 2

    # ------------------------------------------------------------------ der Vorderleib (mit Kopf und Augen)
    m.bone("body", pivot=(0.0, HIP_Y, 0.0))
    # gestufte Wölbung (unten breit, oben schmal), vorn etwas schmaler als hinten
    for y0, y1, hx, z0, z1 in ((1.4, 3.0, 4.2, -5.4, 5.4), (-1.6, 1.4, 5.2, -6.2, 6.0), (-3.0, -1.6, 4.6, -5.8, 5.4),
                               (-4.0, -3.0, 3.6, -5.0, 4.4), (-4.6, -4.0, 2.2, -3.6, 2.8)):
        m.cube("body", (-hx, y0, z0), (2.0 * hx, y1 - y0, z1 - z0), "carapace")
    m.cube("body", (-3.8, -2.8, -8.6), (7.6, 4.8, 2.7), "head")                     # der Kopf vorn
    m.cube("body", (-3.0, -3.6, -8.2), (6.0, 0.9, 2.4), "head")
    # die acht Augen: vorn zwei große und zwei kleine, oben vier (x, y, z, Größe, Tiefe, Material)
    eyes = [(-2.05, -2.3, -9.1, 1.9, 0.7, "eye"), (0.15, -2.3, -9.1, 1.9, 0.7, "eye"),
            (-3.4, -1.2, -8.9, 1.0, 0.5, "eye_small"), (2.4, -1.2, -8.9, 1.0, 0.5, "eye_small"),
            (-1.8, -4.0, -7.6, 1.0, 0.8, "eye_small"), (0.8, -4.0, -7.6, 1.0, 0.8, "eye_small"),
            (-3.4, -3.3, -6.9, 0.9, 0.7, "eye_small"), (2.5, -3.3, -6.9, 0.9, 0.7, "eye_small")]
    for x, y, z, size, depth, material in eyes:
        m.cube("body", (x, y, z), (size, size, depth), material)
    # der Stiel zum Hinterleib
    m.cube("body", (-1.6, -1.3, 5.6), (3.2, 2.6, 2.2), "carapace")

    # die Kieferklauen mit den Giftzähnen
    m.bone("fangs", "body", pivot=(0.0, 1.2, -8.2), rotation=(-8.0, 0.0, 0.0))
    for sx, side in ((1, "l"), (-1, "r")):
        x0 = 0.25 if sx > 0 else -2.55
        m.cube("fangs", (x0, -0.4, -1.6), (2.3, 3.6, 2.5), "chelicera")
        m.bone(f"fang_{side}", "fangs", pivot=(1.4 * sx, 3.1, -0.8), rotation=(-18.0, 0.0, 26.0 * -sx))
        m.cube(f"fang_{side}", (-0.45, 0.0, -0.45), (0.9, 2.4, 0.9), "fang")
        m.cube(f"fang_{side}", (-0.3, 2.2, -0.75), (0.6, 1.2, 0.6), "fang")
    # die Taster
    for sx, side in ((1, "l"), (-1, "r")):
        m.bone(f"palp_{side}", "body", pivot=(2.9 * sx, 0.6, -8.0), rotation=(-30.0, 14.0 * sx, 0.0))
        m.cube(f"palp_{side}", (-0.65, -0.65, -4.2), (1.3, 1.3, 4.4), "leg_femur")
        m.bone(f"palp_{side}_tip", f"palp_{side}", pivot=(0.0, 0.0, -4.0), rotation=(46.0, 0.0, 0.0))
        m.cube(f"palp_{side}_tip", (-0.55, -0.55, -3.2), (1.1, 1.1, 3.4), "leg_tarsus")

    # ------------------------------------------------------------------ der Hinterleib: ein Stück Nachthimmel
    m.bone("abdomen", "body", pivot=(0.0, -0.6, 7.0), rotation=(10.0, 0.0, 0.0))
    # ein gestuftes Ellipsoid aus waagerechten Scheiben (rund statt kantig); Mitte bei z = ABDOMEN_Z
    for y0, y1 in ((-7.6, -6.4), (-6.4, -4.8), (-4.8, -2.6), (-2.6, 2.4), (2.4, 4.6), (4.6, 6.2), (6.2, 7.2)):
        ym = (y0 + y1) / 2.0
        k = math.sqrt(max(0.0, 1.0 - (ym / ABDOMEN_RY) ** 2))
        hx, hz = ABDOMEN_RX * k, ABDOMEN_RZ * k
        m.cube("abdomen", (-hx, y0, ABDOMEN_Z - hz), (2.0 * hx, y1 - y0, 2.0 * hz), "abdomen")
    # in der mittleren Scheibe noch die Rundung an den Seiten und vorn/hinten
    m.cube("abdomen", (-ABDOMEN_RX * 0.78, -2.0, ABDOMEN_Z - ABDOMEN_RZ - 0.6), (ABDOMEN_RX * 1.56, 3.8, 0.6), "abdomen")
    m.cube("abdomen", (-ABDOMEN_RX * 0.78, -2.0, ABDOMEN_Z + ABDOMEN_RZ), (ABDOMEN_RX * 1.56, 3.8, 0.6), "abdomen")
    m.cube("abdomen", (-ABDOMEN_RX - 0.6, -2.0, ABDOMEN_Z - ABDOMEN_RZ * 0.75), (0.6, 3.8, ABDOMEN_RZ * 1.5), "abdomen")
    m.cube("abdomen", (ABDOMEN_RX, -2.0, ABDOMEN_Z - ABDOMEN_RZ * 0.75), (0.6, 3.8, ABDOMEN_RZ * 1.5), "abdomen")
    # drei Kristalle der Leere wachsen aus dem Rücken
    for i, (x, y, z, height, width, rot) in enumerate(((0.3, -7.0, 4.6, 5.6, 2.2, (-24.0, 0.0, 6.0)),
                                                        (-3.6, -5.6, 9.6, 4.2, 1.7, (-30.0, 0.0, -22.0)),
                                                        (3.4, -5.4, 12.0, 3.6, 1.5, (-38.0, 0.0, 24.0)))):
        _spike(m, "abdomen", f"crystal_{i}", (x, y, z), rot, height, width, "crystal")
    # die Spinnwarzen hinten unten
    m.bone("spinnerets", "abdomen", pivot=(0.0, 4.0, ABDOMEN_Z + ABDOMEN_RZ * 0.86), rotation=(30.0, 0.0, 0.0))
    for x in (-1.4, 0.3):
        m.cube("spinnerets", (x, -0.6, 0.0), (1.1, 1.1, 2.2), "spinneret")
    m.cube("spinnerets", (-0.5, 0.6, 0.2), (1.0, 1.0, 1.8), "spinneret")

    # ------------------------------------------------------------------ acht Beine (an der Wurzel, damit die Füße stehen)
    for sx, side in ((1, "l"), (-1, "r")):
        for i, (hx, hz, yaw, fl, tl, sl) in enumerate(LEGS):
            hip = f"leg_{side}{i + 1}"
            m.bone(hip, pivot=(hx * sx, HIP_Y, hz), rotation=(0.0, yaw * sx, 0.0))
            down = tarsus_down(fl, tl, sl)
            # links ragt das Bein nach +x (zRot < 0 hebt es), rechts nach -x (gespiegelt)
            m.bone(hip + "_femur", hip, pivot=(0.0, 0.0, 0.0), rotation=(0.0, 0.0, -FEMUR_UP * sx))
            fw, tw, sw = THICK
            m.cube(hip + "_femur", (0.0 if sx > 0 else -fl, -fw / 2, -fw / 2), (fl, fw, fw), "leg_femur", tag=f"leg:{fl:.2f}")
            m.bone(hip + "_tibia", hip + "_femur", pivot=(fl * sx, 0.0, 0.0), rotation=(0.0, 0.0, (FEMUR_UP + TIBIA_DOWN) * sx))
            m.cube(hip + "_tibia", (-0.3 if sx > 0 else -tl + 0.3, -tw / 2, -tw / 2), (tl, tw, tw), "leg_tibia", tag=f"leg:{tl:.2f}")
            m.cube(hip + "_tibia", (-0.9 if sx > 0 else -0.5, -tw / 2 - 0.25, -tw / 2 - 0.25), (1.4, tw + 0.5, tw + 0.5), "joint")
            m.bone(hip + "_tarsus", hip + "_tibia", pivot=(tl * sx, 0.0, 0.0), rotation=(0.0, 0.0, (down - TIBIA_DOWN) * sx))
            m.cube(hip + "_tarsus", (-0.2 if sx > 0 else -sl + 0.2, -sw / 2, -sw / 2), (sl, sw, sw), "leg_tarsus", tag=f"leg:{sl:.2f}")
            m.cube(hip + "_tarsus", (-0.7 if sx > 0 else -0.5, -sw / 2 - 0.25, -sw / 2 - 0.25), (1.2, sw + 0.5, sw + 0.5), "joint")
            m.cube(hip + "_tarsus", (sl - 0.9 if sx > 0 else -sl - 0.1, -sw / 2 + 0.1, -sw / 2 + 0.1), (1.0, sw - 0.2, sw - 0.2), "claw")
    return m


# =============================================================================================
# Bemalen
# =============================================================================================

def _carapace(ctx: Ctx):
    """Der Vorderleib: nachtblaues Chitin mit Glanz, oben feine Rillen, die strahlenförmig von einer Mulde ausgehen;
    silberne Ränder."""
    x, y, z = ctx.local
    v = 0.42 + _LIT[ctx.face] + (noise3(x * 0.9, y * 0.9, z * 0.9, 811) - 0.5) * 0.12
    if ctx.face == "top":
        a = math.atan2(z + 0.5, x)
        r = math.hypot(x, z + 0.5)
        if r < 1.1:
            v -= 0.22                                                         # die Mulde
        elif (int((a + math.pi) / (math.pi / 6.0) * 2.0) % 2 == 0) and r > 1.6:
            v -= 0.1                                                          # die Rillen
        if r > 3.2 and r < 4.0:
            v += 0.08
    if ctx.face in ("west", "east", "front", "back") and ctx.cube.origin[1] > -2.0 and ctx.t == ctx.h - 1:
        return pick(ctx.pal["silver"], 0.32)                                  # ein silberner Saum unten am Rand
    # ein heller Glanz quer über die Wölbung
    if ctx.face == "top" and abs(x * 0.6 + z * 0.4 + 1.0) < 0.6:
        v += 0.14
    return pick(ctx.pal["chitin"], v)


def _head(ctx: Ctx):
    """Der Kopf: wie der Vorderleib, vorn etwas heller (zwischen den Augen ein feiner Strich)."""
    v = 0.48 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v, 813) - 0.5) * 0.08
    if ctx.face == "front" and abs(ctx.local[0]) < 0.3:
        v -= 0.12
    if ctx.face == "bottom":
        v -= 0.1
    return pick(ctx.pal["chitin"], v)


def _eye(ctx: Ctx):
    """Ein großes Auge: ein Stern – innen weiß, außen blau, mit einem Glanzpunkt; es leuchtet."""
    cx, cy = (ctx.w - 1) / 2, (ctx.h - 1) / 2
    d = math.hypot(ctx.s - cx, ctx.t - cy) / max(1.0, cx + 0.5)
    col = pick(ctx.pal["glow"], 1.0 - 0.6 * d)
    if ctx.s == 0 and ctx.t == 0:
        col = pick(ctx.pal["star"], 1.0)
    ctx.glow = col
    return col


def _eye_small(ctx: Ctx):
    col = pick(ctx.pal["glow"], 0.75 if (ctx.s + ctx.t) % 2 == 0 else 0.6)
    ctx.glow = col
    return col


def _chelicera(ctx: Ctx):
    """Die Kieferklauen: dunkles Chitin, unten silbern."""
    ry = rel_y(ctx)
    if ry > 0.78 and ctx.face not in ("top", "bottom"):
        return pick(ctx.pal["silver"], 0.5 + 0.2 * (ry - 0.78) * 4)
    return pick(ctx.pal["chitin"], 0.36 + _LIT[ctx.face] - 0.1 * ry + (hash01(ctx.u, ctx.v, 817) - 0.5) * 0.06)


def _fang(ctx: Ctx):
    """Die Giftzähne: silberweiß, zur Spitze heller, die Spitze glimmt."""
    ry = rel_y(ctx)
    col = pick(ctx.pal["silver"], 0.55 + 0.4 * ry + _LIT[ctx.face])
    if ry > 0.8 and ctx.cube.size[1] < 1.5:
        ctx.glow = mix((0, 0, 0, 255), col, 0.6)
    return col


def _leg_value(ctx: Ctx, length: float) -> tuple[float, float]:
    """Wo auf dem Glied (0 am Ansatz, 1 an der Spitze) und Grundhelligkeit."""
    x = ctx.local[0]
    t = abs(x) / max(0.5, length)
    v = 0.4 + _LIT[ctx.face] + (hash01(ctx.u, ctx.v, 821) - 0.5) * 0.06
    return min(1.0, t), v


def _leg(kind: str):
    """Ein Beinglied: nachtblau mit einem silbernen Band kurz vor dem Ende, oben ein Glanz; der Fuß zur Spitze silbern."""
    def paint(ctx: Ctx):
        tag = ctx.cube.tag or ""
        length = float(tag.split(":")[1]) if tag.startswith("leg:") else ctx.cube.size[0]
        t, v = _leg_value(ctx, length)
        if ctx.face == "top":
            v += 0.08
        if 0.82 < t < 0.9 and ctx.face != "bottom":
            return pick(ctx.pal["silver"], 0.55 + _LIT[ctx.face])
        if kind == "tarsus" and t > 0.6:
            return pick(ctx.pal["silver"], 0.35 + 0.45 * (t - 0.6) / 0.4 + _LIT[ctx.face])
        if kind == "femur" and (int(t * 9) % 3 == 0):
            v -= 0.06                                                         # feine Ringe
        return pick(ctx.pal["chitin"], v - 0.1 * t)
    return paint


def _joint(ctx: Ctx):
    """Das Gelenk: ein leuchtender Ring im Licht der Sterne."""
    col = pick(ctx.pal["glow"], 0.55 + 0.25 * (1.0 if (ctx.s + ctx.t) % 3 == 0 else 0.0) + _LIT[ctx.face])
    ctx.glow = mix((0, 0, 0, 255), col, 0.85)
    return col


def _claw(ctx: Ctx):
    col = pick(ctx.pal["silver"], 0.75 + _LIT[ctx.face])
    ctx.glow = mix((0, 0, 0, 255), col, 0.35)
    return col


def _abdomen(ctx: Ctx):
    """Der Hinterleib, ein Stück Nachthimmel: oben eine Spiralgalaxie mit rosaweißem Kern und zwei Armen (violett und
    blau), an den Seiten Nebelschleier; überall Sterne, ein paar davon groß und leuchtend."""
    x, y, z = ctx.local
    v = 0.45 + _LIT[ctx.face] * 1.5 + (noise3(x * 0.5, y * 0.5, z * 0.5, 831) - 0.5) * 0.2
    col = pick(ctx.pal["night"], v)
    if ctx.face == "bottom":
        return pick(ctx.pal["night"], 0.2 + (hash01(ctx.u, ctx.v, 833) - 0.5) * 0.1)
    # die Galaxie (oben, um die Mitte des Rückens)
    gx, gz = x - 0.0, z - ABDOMEN_Z
    r = math.hypot(gx, gz)
    if ctx.face == "top" and r < 8.4:
        a = math.atan2(gz, gx)
        arm = 0.0
        for k in (0.0, math.pi):
            phase = (a - k - 1.9 * math.log(r + 0.7)) % (2.0 * math.pi)
            d = min(phase, 2.0 * math.pi - phase)
            arm = max(arm, math.exp(-(d * d) / 0.45))
        fade = max(0.0, 1.0 - r / 8.4)
        neb = arm * (0.35 + 0.65 * fade)
        dust = noise3(x * 0.7, 0.0, z * 0.7, 843)
        if neb > 0.12:
            blue = (a + 0.6 * math.log(r + 0.7)) % (2 * math.pi) < math.pi
            tone = pick(ctx.pal["nebula_blue"] if blue else ctx.pal["nebula"], 0.3 + 0.6 * neb + (dust - 0.5) * 0.3)
            col = mix(col, tone, min(1.0, 0.45 + neb * 0.7))
            if neb > 0.45:
                ctx.glow = mix((0, 0, 0, 255), col, min(1.0, 0.25 + 0.6 * neb))
        if r < 2.6:
            core = pick(ctx.pal["core"], 1.0 - r * 0.28)
            col = mix(col, core, max(0.0, 1.0 - r / 2.8))
            ctx.glow = mix((0, 0, 0, 255), col, max(0.0, 0.95 - r * 0.25))
    elif ctx.face in ("west", "east", "back", "front"):
        # Nebelschleier an den Seiten
        n = noise3(x * 0.35 + 3.0, y * 0.45, z * 0.35, 837)
        if n > 0.55:
            tone = pick(ctx.pal["nebula"] if z > ABDOMEN_Z else ctx.pal["nebula_blue"], 0.2 + (n - 0.55) * 1.8)
            col = mix(col, tone, min(0.75, (n - 0.55) * 2.8))
    # Sterne
    h = hash01(ctx.u, ctx.v, 839)
    if h > 0.955:
        star = pick(ctx.pal["star"], 0.4 + (h - 0.955) * 13.0)
        ctx.glow = mix((0, 0, 0, 255), star, 0.9 if h > 0.985 else 0.6)
        return star
    return col


def _crystal(ctx: Ctx):
    """Kristall der Leere: violette Facetten, zur Spitze hell; die Spitze leuchtet."""
    tag = ctx.cube.tag or "crystal:0"
    t = (1.0 - rel_y(ctx)) * 0.5 + (0.5 if tag.endswith(":1") else 0.0)
    facet = ((ctx.s + ctx.t) // 2) % 3
    v = 0.3 + 0.6 * t + (0.1 if facet == 0 else -0.05 if facet == 1 else 0.0) + _LIT[ctx.face]
    col = pick(ctx.pal["crystal"], v)
    if t > 0.45:
        ctx.glow = mix((0, 0, 0, 255), col, min(1.0, (t - 0.45) * 2.0))
    return col


def _spinneret(ctx: Ctx):
    """Die Spinnwarzen: dunkel, an der Spitze das Leuchten der Sternenseide."""
    oz, d = ctx.cube.origin[2], ctx.cube.size[2]
    t = (ctx.local[2] - oz) / d if d > 0 else 0.0
    if t > 0.6 or ctx.face == "back":
        col = pick(ctx.pal["glow"], 0.7 + 0.3 * t)
        ctx.glow = col
        return col
    return pick(ctx.pal["chitin"], 0.35 + _LIT[ctx.face])


def materials(variant: str) -> dict:
    return {
        "carapace": _carapace, "head": _head, "eye": _eye, "eye_small": _eye_small, "chelicera": _chelicera, "fang": _fang,
        "leg_femur": _leg("femur"), "leg_tibia": _leg("tibia"), "leg_tarsus": _leg("tarsus"), "joint": _joint, "claw": _claw,
        "abdomen": _abdomen, "crystal": _crystal, "spinneret": _spinneret,
    }
