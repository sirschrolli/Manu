"""
Beschreibung von Mob-Modellen (Knochen + Quader) in Python.

Aus einer Beschreibung entstehen:
  * Java-Code für das Modell (LayerDefinition mit PartDefinitions), siehe :meth:`Model.java_class`
  * die Textur-Aufteilung (UV) – automatisch gepackt, siehe :meth:`Model.pack`
  * die Grundlage für das Bemalen der Textur (siehe paint.py)

Koordinaten wie in Minecraft-Entity-Modellen: 1 Einheit = 1/16 Block, Y zeigt nach UNTEN,
-Z ist vorne (Blickrichtung des Mobs), Winkel in Grad.
"""
from __future__ import annotations

import math
from dataclasses import dataclass, field


@dataclass
class Cube:
    bone: "Bone"
    origin: tuple[float, float, float]
    size: tuple[float, float, float]
    material: str | dict = "body"
    inflate: float = 0.0
    tag: str = ""
    uv: tuple[int, int] | None = None
    faces: tuple[str, ...] | None = None  # None = alle Seiten

    @property
    def uv_size(self) -> tuple[int, int]:
        w, h, d = (int(math.ceil(v)) for v in self.size)
        return 2 * (w + d), d + h

    def int_size(self) -> tuple[int, int, int]:
        return tuple(int(math.ceil(v)) for v in self.size)


@dataclass
class Bone:
    name: str
    parent: "Bone | None"
    pivot: tuple[float, float, float]
    rotation: tuple[float, float, float] = (0.0, 0.0, 0.0)
    cubes: list[Cube] = field(default_factory=list)
    children: list["Bone"] = field(default_factory=list)

    def world_pivot(self) -> tuple[float, float, float]:
        """Ungefähre Lage im Modell (ohne Drehungen) – nur für Muster beim Bemalen."""
        if self.parent is None:
            return self.pivot
        px, py, pz = self.parent.world_pivot()
        return px + self.pivot[0], py + self.pivot[1], pz + self.pivot[2]


class Model:
    def __init__(self, name: str, tex_w: int, tex_h: int):
        self.name = name
        self.tex_w = tex_w
        self.tex_h = tex_h
        # Pixel je Modell-Einheit in der gemalten Textur. Die UVs im Java-Code bleiben in Einheiten (TEXTURE_WIDTH =
        # tex_w); Minecraft rechnet mit normierten UVs, daher darf das PNG ein Vielfaches davon groß sein (feinere Details).
        self.tex_res = 1
        self.root = Bone("root", None, (0.0, 0.0, 0.0))
        self.bones: dict[str, Bone] = {"root": self.root}

    # ------------------------------------------------------------------ Aufbau
    def bone(self, name: str, parent: str = "root", pivot=(0.0, 0.0, 0.0), rotation=(0.0, 0.0, 0.0)) -> Bone:
        if name in self.bones:
            raise ValueError(f"Knochen doppelt: {name}")
        p = self.bones[parent]
        b = Bone(name, p, tuple(float(v) for v in pivot), tuple(float(v) for v in rotation))
        p.children.append(b)
        self.bones[name] = b
        return b

    def cube(self, bone: str, origin, size, material="body", inflate=0.0, tag="", faces=None) -> Cube:
        b = self.bones[bone]
        c = Cube(b, tuple(float(v) for v in origin), tuple(float(v) for v in size), material, inflate, tag, None,
                 tuple(faces) if faces else None)
        b.cubes.append(c)
        return c

    def all_cubes(self) -> list[Cube]:
        out: list[Cube] = []

        def walk(b: Bone):
            out.extend(b.cubes)
            for ch in b.children:
                walk(ch)

        walk(self.root)
        return out

    # ------------------------------------------------------------------ UV packen
    def pack(self, padding: int = 1) -> None:
        """Einfacher Regal-Packer: große Teile zuerst, zeilenweise von links nach rechts."""
        cubes = sorted(self.all_cubes(), key=lambda c: (-c.uv_size[1], -c.uv_size[0]))
        x = y = 0
        shelf_h = 0
        for c in cubes:
            w, h = c.uv_size
            if w == 0 or h == 0:
                w, h = max(w, 1), max(h, 1)
            if x + w > self.tex_w:
                x = 0
                y += shelf_h + padding
                shelf_h = 0
            if y + h > self.tex_h:
                raise ValueError(f"Textur {self.name} zu klein ({self.tex_w}x{self.tex_h}) für alle Teile")
            c.uv = (x, y)
            x += w + padding
            shelf_h = max(shelf_h, h)

    # ------------------------------------------------------------------ Java
    def java_body(self) -> str:
        lines: list[str] = []

        def fmt(v: float) -> str:
            s = f"{v:.4f}".rstrip("0").rstrip(".")
            if s in ("-0", ""):
                s = "0"
            return s + "F"

        def emit(b: Bone, parent_var: str):
            var = "p_" + b.name
            cube_parts = []
            for c in b.cubes:
                u, v = c.uv
                ox, oy, oz = c.origin
                w, h, d = c.size
                if c.inflate:
                    cube_parts.append(f".texOffs({u}, {v}).addBox({fmt(ox)}, {fmt(oy)}, {fmt(oz)}, {fmt(w)}, {fmt(h)}, {fmt(d)}, "
                                      f"new CubeDeformation({fmt(c.inflate)}))")
                else:
                    cube_parts.append(f".texOffs({u}, {v}).addBox({fmt(ox)}, {fmt(oy)}, {fmt(oz)}, {fmt(w)}, {fmt(h)}, {fmt(d)})")
            cubes = "CubeListBuilder.create()" + "".join("\n                " + p for p in cube_parts)
            px, py, pz = b.pivot
            rx, ry, rz = (math.radians(a) for a in b.rotation)
            pose = (f"PartPose.offsetAndRotation({fmt(px)}, {fmt(py)}, {fmt(pz)}, {fmt(rx)}, {fmt(ry)}, {fmt(rz)})"
                    if any(b.rotation) else f"PartPose.offset({fmt(px)}, {fmt(py)}, {fmt(pz)})")
            lines.append(f"        PartDefinition {var} = {parent_var}.addOrReplaceChild(\"{b.name}\", {cubes},\n                {pose});")
            for ch in b.children:
                emit(ch, var)

        for ch in self.root.children:
            emit(ch, "root")
        return "\n".join(lines)

    def java_class(self, package: str, class_name: str, doc: str) -> str:
        return f"""package {package};

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * {doc}
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class {class_name} {{
    public static final int TEXTURE_WIDTH = {self.tex_w};
    public static final int TEXTURE_HEIGHT = {self.tex_h};

    private {class_name}() {{}}

    public static MeshDefinition createMesh() {{
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
{self.java_body()}
        return mesh;
    }}

    public static LayerDefinition createLayer() {{
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }}
}}
"""


# ---------------------------------------------------------------------------------------------
# Lage der sechs Seiten eines Quaders auf der Textur (wie ModelPart.Cube in Minecraft 26.3)
# ---------------------------------------------------------------------------------------------

def face_rects(c: Cube) -> dict[str, tuple[int, int, int, int]]:
    """Seite -> (u, v, Breite, Höhe). 'top'/'bottom' sind die sichtbare Ober-/Unterseite."""
    U, V = c.uv
    w, h, d = c.int_size()
    return {
        "top": (U + d, V, w, d),            # Modell-minY (oben). Zeile 0 = hinten, letzte Zeile = vorne
        "bottom": (U + d + w, V, w, d),     # Modell-maxY (unten). Zeile 0 = hinten, letzte Zeile = vorne
        "west": (U, V + d, d, h),           # minX-Seite. Spalte 0 = hinten, rechts = vorne
        "front": (U + d, V + d, w, h),      # minZ (vorne). Spalte 0 = minX
        "east": (U + d + w, V + d, d, h),   # maxX-Seite. Spalte 0 = vorne, rechts = hinten
        "back": (U + 2 * d + w, V + d, w, h),  # maxZ (hinten). Spalte 0 = maxX
    }


def texel_to_local(c: Cube, face: str, s: int, t: int, res: int = 1) -> tuple[float, float, float]:
    """Mittelpunkt eines Texels (Spalte s, Zeile t in der Seite) als Modellpunkt (Knochen-lokal).

    ``res``: Pixel je Einheit der Textur (s und t zählen dann in Pixeln)."""
    ox, oy, oz = c.origin
    w, h, d = c.size
    fx = lambda i, n, length: (i + 0.5) / max(n * res, 1) * length  # noqa: E731
    iw, ih, idd = c.int_size()
    if face == "top":
        return ox + fx(s, iw, w), oy, oz + d - fx(t, idd, d)
    if face == "bottom":
        return ox + fx(s, iw, w), oy + h, oz + d - fx(t, idd, d)
    if face == "front":
        return ox + fx(s, iw, w), oy + fx(t, ih, h), oz
    if face == "back":
        return ox + w - fx(s, iw, w), oy + fx(t, ih, h), oz + d
    if face == "west":
        return ox, oy + fx(t, ih, h), oz + d - fx(s, idd, d)
    if face == "east":
        return ox + w, oy + fx(t, ih, h), oz + fx(s, idd, d)
    raise ValueError(face)
