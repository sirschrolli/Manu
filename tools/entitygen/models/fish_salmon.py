"""
Lachs und Rotlachs auf den Knochen des Vanilla-Lachses (SalmonModel liest nur "body_back" für das Schwanzschlagen;
alle Vanilla-Knochen bleiben mit Namen, Eltern und Drehpunkten erhalten). Die drei Vanilla-Größen entstehen wie bei
Vanilla durch Skalieren derselben Schicht (klein 0.5, mittel 1.0, groß 1.5 – MeshTransformer.scaling).

  * lachs:    Atlantischer Lachs – stahlblauer Rücken, silberne Flanken mit schwarzen Tupfen über der Seitenlinie,
              weißer Bauch, gegabelte Schwanzflosse, kleine Fettflosse
  * rotlachs: laichender Rotlachs – leuchtend karminroter Leib, olivgrüner Kopf mit hellem Hakenkiefer, Laichbuckel
              vor der Rückenflosse

Eigene Teile: Schnauze am Kopf, verjüngter Hinterleib (drei Quader in body_back), Fettflosse (der Quader von
top_back_fin), Bauch- und Afterflosse als Kind-Knochen, beim Rotlachs Hakenkiefer ("kype") und Laichbuckel ("hump").
Beim Lachs sind Hakenkiefer und Buckel in der Textur durchsichtig (der Renderer muss nichts ausblenden).
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import Ctx, pick, ramp, rel_y, shade
from .fish_cod import eye_texel, fin_membrane, jitter_dot, tone


def _pal(**kw) -> dict:
    return {k: ramp(*v, steps=8) for k, v in kw.items()}


PALETTES = {
    "lachs": _pal(back=("#18242c", "#5a7484"), upper=("#3e5462", "#b4c6d0"), flank=("#7e8c96", "#f2f6f8"),
                  belly=("#b8c0c6", "#ffffff"), spot=("#0c1014", "#2a3038"), fin=("#1c262e", "#6e808c"),
                  finedge=("#10161c", "#3a4650"), head=("#18242c", "#5a7484"), jaw=("#a8b2b8", "#f4f6f8"),
                  iris=("#5a646c", "#dce2e6"), mouth=("#3a444a", "#8a969e")),
    "rotlachs": _pal(back=("#3a060c", "#a8141e"), upper=("#5a0a12", "#d02830"), flank=("#6a0c14", "#f04040"),
                     belly=("#7a1a1e", "#f8706a"), spot=("#2a0408", "#5a0a10"), fin=("#3a0c0e", "#b42a26"),
                     finedge=("#1e0606", "#5a1410"), head=("#1a2610", "#7a8e44"), jaw=("#9aa084", "#f0f2de"),
                     iris=("#4a5420", "#c8c870"), mouth=("#2a3018", "#7a8060")),
}


def build() -> Model:
    m = Model("fish_salmon", 64, 32)
    m.bone("body_front", pivot=(0, 20, -7.2))
    m.cube("body_front", (-1.5, -2.5, 0), (3, 5, 8), "body")
    m.bone("hump", "body_front", pivot=(0, -2.5, 1))                    # Laichbuckel (nur der Rotlachs)
    m.cube("hump", (-1, -1, 0), (2, 1, 4), "hump")
    m.bone("body_back", pivot=(0, 20, 0.8))
    m.cube("body_back", (-1.5, -2.5, 0), (3, 5, 3), "body")             # Hinterleib, zum Schwanz hin schmaler
    m.cube("body_back", (-1.5, -2, 3), (3, 4, 2), "body")
    m.cube("body_back", (-1, -1.5, 5), (2, 3, 3), "body")               # Schwanzstiel
    m.bone("head", pivot=(0, 20, -7.2))
    m.cube("head", (-1, -2, -3), (2, 4, 3), "head")
    m.cube("head", (-1, -1, -4), (2, 3, 1), "snout")                    # Schnauze
    m.bone("kype", "head", pivot=(0, -1, -4))                           # Hakenkiefer (nur der Rotlachs)
    m.cube("kype", (-1, 0, -1), (2, 1, 1), "kype")                      # verlängerter Oberkiefer …
    m.cube("kype", (-0.5, 0, -2), (1, 2, 1), "kype")                    # … mit nach unten gebogener Spitze
    m.bone("back_fin", "body_back", pivot=(0, 0, 8))
    m.cube("back_fin", (0, -3.5, 0), (0, 7, 5), "tail")                 # gegabelte Schwanzflosse
    m.bone("top_front_fin", "body_front", pivot=(0, -4.5, 5))
    m.cube("top_front_fin", (0, -1, -2), (0, 3, 4), "dorsal_fin")       # Rückenflosse
    m.bone("top_back_fin", "body_back", pivot=(0, -4.5, -1))
    m.cube("top_back_fin", (0, 1, 6), (0, 2, 2), "adipose_fin")         # Fettflosse kurz vor dem Schwanz
    m.bone("pelvic_fin", "body_front", pivot=(0, 2.5, 5))
    m.cube("pelvic_fin", (0, 0, 0), (0, 2, 2), "low_fin")               # Bauchflosse
    m.bone("anal_fin", "body_back", pivot=(0, 2, 3))
    m.cube("anal_fin", (0, 0, 0), (0, 2, 2), "low_fin")                 # Afterflosse
    m.bone("right_fin", pivot=(-1.5, 21.5, -7.2), rotation=(0, 0, -45))
    m.cube("right_fin", (-2, 0, 0), (2, 0, 3), "pec_fin")
    m.bone("left_fin", pivot=(1.5, 21.5, -7.2), rotation=(0, 0, 45))
    m.cube("left_fin", (0, 0, 0), (2, 0, 3), "pec_fin")
    return m


def preview_pose(variant: str) -> dict:
    return {"body_back": (0, 8, 0)}


def hidden(variant: str) -> list[str]:
    return [] if variant == "rotlachs" else ["kype", "hump"]


# =============================================================================================
# Malen
# =============================================================================================

def _red(ctx: Ctx) -> bool:
    return ctx.params.get("variant") == "rotlachs"


def _spot(ctx: Ctx, a: float, b: float, density: float) -> bool:
    """Schwarze Tupfen des Atlantischen Lachses (der Rotlachs hat keine)."""
    return not _red(ctx) and jitter_dot(ctx, a, b, density, seed=61)


def _side(ctx: Ctx, rel: float, wz: float, wy: float) -> tuple:
    """Flanke nach Höhe im Quader (0 = oben, 1 = unten)."""
    if _red(ctx):
        if rel < 0.25:
            return tone(ctx, "back", 0.66, seed=1)
        if rel > 0.8:
            return tone(ctx, "belly", 0.62, seed=2)
        return tone(ctx, "flank", 0.64 if rel < 0.5 else 0.72, seed=3)
    if rel < 0.25:
        return tone(ctx, "back", 0.44, seed=4)                          # stahlblauer Rücken
    if rel < 0.45:
        col = tone(ctx, "upper", 0.6, seed=5)                           # Übergang mit schwarzen Tupfen
        return pick(ctx.pal["spot"], 0.4) if _spot(ctx, wz + 1, wy, 0.6) else col
    if rel < 0.65:
        col = tone(ctx, "flank", 0.86, seed=6)                          # heller Silberstreif, vereinzelt getupft
        return pick(ctx.pal["spot"], 0.5) if _spot(ctx, wz + 3, wy, 0.22) else col
    if rel < 0.8:
        return tone(ctx, "flank", 0.72, seed=7)
    return tone(ctx, "belly", 0.86, seed=8)


def _body(ctx: Ctx):
    x, y, z = ctx.world
    if ctx.face == "top":
        col = tone(ctx, "back", 0.56 if _red(ctx) else 0.4, seed=11)
        return pick(ctx.pal["spot"], 0.5) if _spot(ctx, x + 4, z, 0.25) else col
    if ctx.face == "bottom":
        return tone(ctx, "belly", 0.62 if _red(ctx) else 0.74, seed=12)
    if ctx.face in ("front", "back"):
        # Stufen zwischen den Quadern: dieselben Bänder wie die Flanke, etwas dunkler
        return shade(_side(ctx, rel_y(ctx), z + 7, y + 7), 0.86)
    return _side(ctx, rel_y(ctx), z, y)


def _hump(ctx: Ctx):
    if not _red(ctx):
        return None
    return tone(ctx, "back", 0.5 if ctx.face == "top" else 0.56, seed=14)


def _head(ctx: Ctx):
    x, y, z = ctx.world
    red = _red(ctx)
    if ctx.face == "top":
        return tone(ctx, "head", 0.42, seed=21)
    if ctx.face == "bottom":
        return tone(ctx, "jaw", 0.66, seed=22)
    if ctx.face in ("front", "back"):
        return tone(ctx, "head", 0.4, seed=23)
    # Seiten: Auge vorn in der zweiten Reihe, dahinter der Augenring, Kiemendeckel als Bogen am Hinterrand
    if -10.2 < z < -9.2 and 19 < y < 20:
        return eye_texel(ctx)
    if -9.2 < z < -8.2 and 19 < y < 20:
        return pick(ctx.pal["iris"], 0.6)
    if -8.2 < z < -7.2 and 19 < y < 22:
        return pick(ctx.pal["head"] if y < 21 else ctx.pal["jaw"], 0.3 if y < 21 else 0.35)   # Kiemendeckel-Rand
    if y < 19:
        return tone(ctx, "head", 0.44, seed=24)
    if y < 20:
        return tone(ctx, "head", 0.62 if red else 0.58, seed=25)
    if y < 21:
        return tone(ctx, "head" if red else "flank", 0.72 if red else 0.66, seed=26)          # Wange
    return tone(ctx, "jaw", 0.72, seed=27)


def _snout(ctx: Ctx):
    x, y, z = ctx.world
    if ctx.face == "top":
        return tone(ctx, "head", 0.4, seed=31)
    if ctx.face == "bottom":
        return tone(ctx, "jaw", 0.62, seed=32)
    if y < 20:
        return tone(ctx, "head", 0.46, seed=33)                         # Oberkiefer
    if y < 21:
        if ctx.face == "front":
            return pick(ctx.pal["mouth"], 0.4)                          # Maul
        return tone(ctx, "head", 0.66, seed=34)
    if ctx.face != "front":
        return pick(ctx.pal["mouth"], 0.55)                             # Maulwinkel
    return tone(ctx, "jaw", 0.7, seed=35)                               # Unterkiefer


def _kype(ctx: Ctx):
    if not _red(ctx):
        return None
    if ctx.local[1] > 1:
        return tone(ctx, "jaw", 0.8, seed=36)                           # helle Hakenspitze
    if ctx.face == "top":
        return tone(ctx, "head", 0.42, seed=37)
    if ctx.face == "bottom":
        return tone(ctx, "jaw", 0.6, seed=38)
    return tone(ctx, "head", 0.5, seed=39)


def _dorsal_fin(ctx: Ctx):
    _, ly, lz = ctx.local
    # Segel: vorn hoch, nach hinten abfallend (lz -2..2, ly -1..2; 2 = Ansatz)
    above = 2 - ly                                                      # 0 = Ansatz am Rücken
    height = 3.0 - (lz + 2) * 0.55
    if above > height:
        return None
    col = fin_membrane(ctx, "fin", "finedge", above / max(0.01, height), lz + 2, base=0.5, ray_axis=0)
    if _spot(ctx, lz + 3, ly + 5, 0.35):
        col = pick(ctx.pal["spot"], 0.4)
    return col


def _adipose(ctx: Ctx):
    _, ly, lz = ctx.local
    if ly < 2 and lz < 7:
        return None                                                     # kleiner, nach hinten geneigter Lappen
    return tone(ctx, "back", 0.5 if _red(ctx) else 0.42, seed=41)


def _low_fin(ctx: Ctx):
    _, ly, lz = ctx.local
    if ly > 1 and lz > 1:
        return None                                                     # dreieckig, Spitze nach hinten unten
    return fin_membrane(ctx, "fin", "finedge", ly / 2.0, lz, base=0.56, ray_axis=0)


def _pec_fin(ctx: Ctx):
    lx, _, lz = ctx.local
    reach = abs(lx) / 2.0
    if reach > 0.5 and lz > 2:
        return None
    return fin_membrane(ctx, "fin", "finedge", reach * 0.9 + lz * 0.08, lz, base=0.56, ray_axis=0)


def _tail(ctx: Ctx):
    _, ly, lz = ctx.local
    half = 1.6 + min(1.9, lz * 0.75)                                    # schmal am Stiel, zum Rand hin breit
    if abs(ly) > half:
        return None
    if lz > 3 and abs(ly) < (lz - 3) * 1.1 + 0.4:
        return None                                                     # Gabelung
    along = min(1.0, lz / 4.6 + abs(ly) * 0.04)
    return fin_membrane(ctx, "fin", "finedge", along, ly + 3.5, base=0.48, ray_axis=1)


def materials(variant: str) -> dict:
    return {"body": _body, "hump": _hump, "head": _head, "snout": _snout, "kype": _kype, "tail": _tail,
            "dorsal_fin": _dorsal_fin, "adipose_fin": _adipose, "low_fin": _low_fin, "pec_fin": _pec_fin}
