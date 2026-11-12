#!/usr/bin/env python3
"""
Vorschau der Wesen-Modelle aus tools/entitygen ohne Spiel: baut ein Modell, bemalt es und zeichnet es von mehreren Seiten
(Ruhehaltung, so wie die Knochen im Modell stehen; die Animationen aus dem Java-Modell fehlen).

Aufruf (aus dem Projektordner):  python3 tools/entity_preview.py <modul> [variante] [ausgabe.png] [pose=…] [views=…]
z. B.  python3 tools/entity_preview.py soul_heron
       python3 tools/entity_preview.py soul_heron "pose=neck1:84,0,0 left_wing:90,0,84@1,1.12,1.3" "views=-35,12;90,0"

pose: je Knochen eine Zusatzdrehung in Grad (x,y,z), wahlweise mit Skalierung @x,y,z – wie die Animation in Java, die zu
den Ruhewinkeln addiert. views: Blickrichtungen (Gierwinkel,Neigung) durch Semikolon getrennt. hide=a,b blendet Knochen aus;
gear=<beruf> zeigt bei Modellen mit Zubehör je Beruf (GEAR) nur das des Berufs.
"""
from __future__ import annotations

import importlib
import math
import sys
from pathlib import Path

import numpy as np
from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
from entitygen.geometry import face_rects, texel_to_local  # noqa: E402
from entitygen.paint import paint_model  # noqa: E402


def _rot(deg):
    ax, ay, az = (math.radians(a) for a in deg)
    rx = np.array([[1, 0, 0], [0, math.cos(ax), -math.sin(ax)], [0, math.sin(ax), math.cos(ax)]])
    ry = np.array([[math.cos(ay), 0, math.sin(ay)], [0, 1, 0], [-math.sin(ay), 0, math.cos(ay)]])
    rz = np.array([[math.cos(az), -math.sin(az), 0], [math.sin(az), math.cos(az), 0], [0, 0, 1]])
    return rz @ ry @ rx


POSE: dict = {}
# Knochen, die nicht gezeichnet werden (samt Kindern) – "hide=a,b" oder "gear=<beruf>" bei Modellen mit GEAR/ALL_GEAR
HIDDEN: set = set()


def _hidden(bone) -> bool:
    while bone is not None:
        if bone.name in HIDDEN:
            return True
        bone = bone.parent
    return False


def _bone_world(bone):
    """(Drehung, Verschiebung) des Knochens im Modell – wie ModelPart.translateAndRotate (mit der Zusatzhaltung POSE)."""
    if bone.parent is None:
        return np.eye(3), np.zeros(3)
    pr, pt = _bone_world(bone.parent)
    extra, scale, shift = POSE.get(bone.name, ((0.0, 0.0, 0.0), (1.0, 1.0, 1.0), (0.0, 0.0, 0.0)))
    rot = tuple(a + b for a, b in zip(bone.rotation, extra))
    r = pr @ _rot(rot) @ np.diag(scale)
    t = pt + pr @ (np.array(bone.pivot, dtype=float) + np.array(shift, dtype=float))
    return r, t


def _parse_pose(text: str) -> dict:
    """"knochen:rx,ry,rz[@sx,sy,sz][#dx,dy,dz]" – Zusatzdrehung (Grad), Skalierung, Verschiebung des Drehpunkts."""
    pose = {}
    for item in text.split():
        name, _, rest = item.partition(":")
        rest, _, sh = rest.partition("#")
        rot, _, sc = rest.partition("@")
        r = tuple(float(v) for v in rot.split(",")) if rot else (0.0, 0.0, 0.0)
        k = tuple(float(v) for v in sc.split(",")) if sc else (1.0, 1.0, 1.0)
        d = tuple(float(v) for v in sh.split(",")) if sh else (0.0, 0.0, 0.0)
        pose[name] = (r, k, d)
    return pose


_NORMAL = {"top": (0, -1, 0), "bottom": (0, 1, 0), "west": (-1, 0, 0), "east": (1, 0, 0), "front": (0, 0, -1), "back": (0, 0, 1)}


def render(model, tex: Image.Image, glow: Image.Image, yaw: float, pitch: float, size: int = 520, scale: float | None = None,
           night: bool = False) -> Image.Image:
    res = getattr(model, "tex_res", 1)
    t_img = np.asarray(tex.convert("RGBA"), dtype=np.float32)
    g_img = np.asarray(glow.convert("RGBA"), dtype=np.float32)
    view = _rot((pitch, yaw, 0))
    quads = []
    for cube in model.all_cubes():
        if _hidden(cube.bone):
            continue
        r, t = _bone_world(cube.bone)
        for face, (u0, v0, fw, fh) in face_rects(cube).items():
            if cube.faces and face not in cube.faces:
                continue
            pw, ph = fw * res, fh * res
            if pw <= 0 or ph <= 0:
                continue
            o = np.array(texel_to_local(cube, face, -0.5, -0.5, res))
            a = np.array(texel_to_local(cube, face, pw - 0.5, -0.5, res)) - o
            b = np.array(texel_to_local(cube, face, -0.5, ph - 0.5, res)) - o
            n = np.array(_NORMAL[face], dtype=float)
            # in die Welt (Modellraum: y nach unten) und dann in die Ansicht (y nach oben)
            ow, aw, bw, nw = t + r @ o, r @ a, r @ b, r @ n
            flip = np.array([1.0, -1.0, 1.0])
            ow, aw, bw, nw = view @ (ow * flip), view @ (aw * flip), view @ (bw * flip), view @ (nw * flip)
            quads.append((ow, aw, bw, nw, (u0 * res, v0 * res, pw, ph)))
    pts = np.array([q[0] for q in quads] + [q[0] + q[1] + q[2] for q in quads])
    lo, hi = pts.min(axis=0), pts.max(axis=0)
    span = max(hi[0] - lo[0], hi[1] - lo[1], 1.0)
    k = scale or (size * 0.86 / span)
    cx, cy = (lo[0] + hi[0]) / 2, (lo[1] + hi[1]) / 2
    img = np.zeros((size, size, 3), dtype=np.float32)
    img[:] = (14, 16, 22) if night else (62, 66, 76)
    zbuf = np.full((size, size), -1e9, dtype=np.float32)
    light = np.array([-0.4, 0.75, -0.55])
    light /= np.linalg.norm(light)
    for ow, aw, bw, nw, (tu, tv, pw, ph) in quads:
        if nw[2] > 1e-6:          # Rückseite (Kamera schaut nach +z)
            continue
        A = np.array([size / 2 + (ow[0] - cx) * k, size / 2 - (ow[1] - cy) * k])
        E1 = np.array([aw[0] * k, -aw[1] * k])
        E2 = np.array([bw[0] * k, -bw[1] * k])
        det = E1[0] * E2[1] - E1[1] * E2[0]
        if abs(det) < 1e-6:
            continue
        xs = [A[0], A[0] + E1[0], A[0] + E2[0], A[0] + E1[0] + E2[0]]
        ys = [A[1], A[1] + E1[1], A[1] + E2[1], A[1] + E1[1] + E2[1]]
        x0, x1 = max(0, int(min(xs))), min(size - 1, int(max(xs)) + 1)
        y0, y1 = max(0, int(min(ys))), min(size - 1, int(max(ys)) + 1)
        if x1 < x0 or y1 < y0:
            continue
        gx, gy = np.meshgrid(np.arange(x0, x1 + 1) + 0.5, np.arange(y0, y1 + 1) + 0.5)
        rx, ry = gx - A[0], gy - A[1]
        s = (rx * E2[1] - ry * E2[0]) / det
        tt = (E1[0] * ry - E1[1] * rx) / det
        m = (s >= 0) & (s < 1) & (tt >= 0) & (tt < 1)
        if not m.any():
            continue
        depth = -(ow[2] + s * aw[2] + tt * bw[2])
        ui = np.clip((tu + s * pw).astype(int), 0, t_img.shape[1] - 1)
        vi = np.clip((tv + tt * ph).astype(int), 0, t_img.shape[0] - 1)
        col = t_img[vi, ui]
        gcol = g_img[vi, ui]
        sub = (slice(y0, y1 + 1), slice(x0, x1 + 1))
        ok = m & (col[..., 3] > 8) & (depth > zbuf[sub])
        zbuf[sub][ok] = depth[ok]
        shade = 0.55 + 0.45 * max(0.0, float(-(nw @ light))) if not night else 0.18
        rgb = col[..., :3] * shade
        lit = gcol[..., 3] > 8
        rgb[lit] = np.maximum(rgb[lit], gcol[..., :3][lit])
        img[sub][ok] = rgb[ok]
    return Image.fromarray(np.clip(img, 0, 255).astype("uint8"), "RGB")


def main(argv: list[str]) -> None:
    views = [(-35, 12, False), (90, 0, False), (180 + 30, 15, False), (-35, 12, True)]
    gear = None
    for a in list(argv):
        if a.startswith("hide="):
            HIDDEN.update(a[5:].split(","))
            argv.remove(a)
            continue
        if a.startswith("gear="):
            gear = a[5:]
            argv.remove(a)
            continue
        if a.startswith("pose="):
            POSE.update(_parse_pose(a[5:]))
            argv.remove(a)
        elif a.startswith("views="):
            views = [(float(v.split(",")[0]), float(v.split(",")[1]), False) for v in a[6:].split(";")]
            argv.remove(a)
    name = argv[0]
    module = importlib.import_module(f"entitygen.models.{name}")
    variant = argv[1] if len(argv) > 1 and not argv[1].endswith(".png") else next(iter(module.PALETTES))
    out = next((a for a in argv[1:] if a.endswith(".png")), f"/tmp/claude-0/-home-claude/50786bb5-6cc3-5907-a25e-13bee43305bb/scratchpad/view/prev_{name}.png")
    if gear is not None and hasattr(module, "GEAR"):
        HIDDEN.update(set(module.ALL_GEAR) - set(module.GEAR[gear]))
    model = module.build()
    model.pack()
    tex, glow = paint_model(model, module.materials(variant), module.PALETTES[variant], seed=100, params={"variant": variant})
    tiles = [render(model, tex, glow, yaw, pitch, night=night) for yaw, pitch, night in views]
    sheet = Image.new("RGB", (sum(t.width for t in tiles), tiles[0].height))
    x = 0
    for t in tiles:
        sheet.paste(t, (x, 0))
        x += t.width
    sheet.save(out)
    print(out, f"{len(model.all_cubes())} Quader")


if __name__ == "__main__":
    main(sys.argv[1:])
