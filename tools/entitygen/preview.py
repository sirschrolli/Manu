"""
Schnelle Vorschau eines Mob-Modells (geometry.Model + Textur) als Bild – ohne Minecraft zu starten.

Zeichnet alle Quader mit ihrer Textur aus beliebigen Blickwinkeln (Maler-Algorithmus, einfache Beleuchtung,
Leucht-Textur obenauf). Optional mit zusätzlichen Knochen-Drehungen ("Pose"), um z. B. angelegte Flügel zu prüfen.

    from entitygen.preview import render_views
    render_views(model, texture, glow, "vorschau.png", pose={"wing_l": (0, -60, 0)})
"""
from __future__ import annotations

import math

import numpy as np
from PIL import Image

from .geometry import Bone, Cube, Model, face_rects

FACE_NORMALS = {"top": (0, -1, 0), "bottom": (0, 1, 0), "west": (-1, 0, 0), "east": (1, 0, 0),
                "front": (0, 0, -1), "back": (0, 0, 1)}


def _rot_zyx(rx: float, ry: float, rz: float) -> np.ndarray:
    """Wie Quaternionf.rotationZYX(z, y, x) in Minecraft: zuerst X, dann Y, dann Z."""
    cx, sx = math.cos(rx), math.sin(rx)
    cy, sy = math.cos(ry), math.sin(ry)
    cz, sz = math.cos(rz), math.sin(rz)
    mx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
    my = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    mz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    return mz @ my @ mx


def _bone_transforms(model: Model, pose: dict | None) -> dict[str, tuple[np.ndarray, np.ndarray]]:
    pose = pose or {}
    out: dict[str, tuple[np.ndarray, np.ndarray]] = {}

    def walk(b: Bone, rot: np.ndarray, pos: np.ndarray):
        extra = pose.get(b.name, (0.0, 0.0, 0.0))
        rx, ry, rz = (math.radians(b.rotation[i] + extra[i]) for i in range(3))
        p = pos + rot @ (np.array(b.pivot) / 16.0)
        r = rot @ _rot_zyx(rx, ry, rz)
        if len(extra) >= 6:   # Skalierung wie ModelPart.xScale/yScale/zScale (wirkt auch auf Kinder)
            r = r @ np.diag([extra[3], extra[4], extra[5]])
        out[b.name] = (r, p)
        for ch in b.children:
            walk(ch, r, p)

    walk(model.root, np.eye(3), np.zeros(3))
    return out


def _face_corners(c: Cube, face: str) -> list[tuple[float, float, float]]:
    """Ecken der Fläche in Textur-Reihenfolge (oben links, oben rechts, unten rechts, unten links), Knochen-lokal."""
    ox, oy, oz = c.origin
    w, h, d = c.size
    i = c.inflate
    x0, x1, y0, y1, z0, z1 = ox - i, ox + w + i, oy - i, oy + h + i, oz - i, oz + d + i
    if face == "top":      # Zeile 0 = hinten (maxZ), letzte = vorne
        return [(x0, y0, z1), (x1, y0, z1), (x1, y0, z0), (x0, y0, z0)]
    if face == "bottom":
        return [(x0, y1, z1), (x1, y1, z1), (x1, y1, z0), (x0, y1, z0)]
    if face == "front":    # Spalte 0 = minX, Zeile 0 = oben (minY)
        return [(x0, y0, z0), (x1, y0, z0), (x1, y1, z0), (x0, y1, z0)]
    if face == "back":     # Spalte 0 = maxX
        return [(x1, y0, z1), (x0, y0, z1), (x0, y1, z1), (x1, y1, z1)]
    if face == "west":     # minX, Spalte 0 = hinten
        return [(x0, y0, z1), (x0, y0, z0), (x0, y1, z0), (x0, y1, z1)]
    if face == "east":     # maxX, Spalte 0 = vorne
        return [(x1, y0, z0), (x1, y0, z1), (x1, y1, z1), (x1, y1, z0)]
    raise ValueError(face)


def _homography(src: list[tuple[float, float]], dst: list[tuple[float, float]]) -> list[float]:
    """Koeffizienten für Image.transform(PERSPECTIVE): bildet Ziel-Pixel (src = Bildschirm) auf Textur (dst) ab."""
    a = []
    b = []
    for (x, y), (u, v) in zip(src, dst):
        a.append([x, y, 1, 0, 0, 0, -u * x, -u * y])
        a.append([0, 0, 0, x, y, 1, -v * x, -v * y])
        b.extend([u, v])
    sol, *_ = np.linalg.lstsq(np.array(a, dtype=float), np.array(b, dtype=float), rcond=None)
    return list(sol)


def _descendants(model: Model, names) -> set[str]:
    out: set[str] = set()

    def walk(b: Bone, inside: bool):
        inside = inside or b.name in names
        if inside:
            out.add(b.name)
        for ch in b.children:
            walk(ch, inside)

    walk(model.root, False)
    return out


def render(model: Model, texture: Image.Image, glow: Image.Image | None, yaw: float, pitch: float,
           size: int = 640, pose: dict | None = None, background=(54, 58, 66, 255), zoom: float = 1.0,
           focus=None) -> Image.Image:
    """Ein Blickwinkel. yaw = Drehung um die Hochachse (0 = von vorn), pitch = von oben (positiv).

    ``focus``: Knochen-Namen – der Ausschnitt richtet sich nur nach diesen Knochen (und ihren Kindern), z. B. ["head"]
    für eine Nahaufnahme des Kopfes."""
    focus_set = _descendants(model, set(focus)) if focus else None
    focus_pts = []
    tf = _bone_transforms(model, pose)
    res = max(1, texture.width // model.tex_w)
    cam = _rot_zyx(math.radians(pitch), 0, 0) @ _rot_zyx(0, math.radians(yaw), 0)
    light = np.array([0.35, -0.85, -0.4])
    light /= np.linalg.norm(light)
    quads = []
    all_pts = []
    for c in model.all_cubes():
        r, p = tf[c.bone.name]
        for face, (u0, v0, fw, fh) in face_rects(c).items():
            if c.faces and face not in c.faces:
                continue
            if fw == 0 or fh == 0:
                continue
            corners = [p + r @ (np.array(pt) / 16.0) for pt in _face_corners(c, face)]
            n = r @ np.array(FACE_NORMALS[face], dtype=float)
            n = n / (np.linalg.norm(n) or 1.0)
            cs = [cam @ pt for pt in corners]
            nc = cam @ n
            flat = min(c.size) == 0
            if nc[2] > 1e-6 and not flat:   # zeigt von der Kamera weg (Kamera blickt in +Z)
                continue
            shade = 0.55 + 0.45 * max(0.0, float(-(n @ light))) if not flat else 0.85
            quads.append((sum(q[2] for q in cs) / 4, cs, (u0, v0, fw, fh), shade))
            all_pts.extend(cs)
            if focus_set is not None and c.bone.name in focus_set:
                focus_pts.extend(cs)
    if not all_pts:
        return Image.new("RGBA", (size, size), background)
    pts = np.array(focus_pts if focus_pts else all_pts)
    mn, mx = pts.min(axis=0), pts.max(axis=0)
    span = max(mx[0] - mn[0], mx[1] - mn[1]) or 1.0
    scale = size * 0.9 / span * zoom
    cx, cy = (mn[0] + mx[0]) / 2, (mn[1] + mx[1]) / 2

    def to_screen(q):
        return (size / 2 + (q[0] - cx) * scale, size / 2 + (q[1] - cy) * scale)

    canvas = Image.new("RGBA", (size, size), background)
    glow_canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    for depth, cs, (u0, v0, fw, fh), shade in sorted(quads, key=lambda q: -q[0]):
        scr = [to_screen(q) for q in cs]
        xs = [s[0] for s in scr]
        ys = [s[1] for s in scr]
        bx0, by0 = max(0, int(math.floor(min(xs)))), max(0, int(math.floor(min(ys))))
        bx1, by1 = min(size, int(math.ceil(max(xs))) + 1), min(size, int(math.ceil(max(ys))) + 1)
        if bx1 <= bx0 or by1 <= by0:
            continue
        local = [(s[0] - bx0, s[1] - by0) for s in scr]
        dst = [(0, 0), (fw * res, 0), (fw * res, fh * res), (0, fh * res)]
        try:
            coeffs = _homography(local, dst)
        except np.linalg.LinAlgError:
            continue
        tile = texture.crop((u0 * res, v0 * res, (u0 + fw) * res, (v0 + fh) * res))
        if shade < 0.999:
            arr = np.array(tile, dtype=np.float32)
            arr[..., :3] *= shade
            tile = Image.fromarray(arr.clip(0, 255).astype(np.uint8), "RGBA")
        warped = tile.transform((bx1 - bx0, by1 - by0), Image.PERSPECTIVE, coeffs, Image.NEAREST,
                                fillcolor=(0, 0, 0, 0))
        canvas.alpha_composite(warped, (bx0, by0))
        if glow is not None:
            # Leuchten unter dieser Fläche löschen (verdeckt), dann ihr eigenes Leuchten eintragen
            glow_canvas.paste(Image.new("RGBA", warped.size, (0, 0, 0, 0)), (bx0, by0), warped.split()[3])
            gt = glow.crop((u0 * res, v0 * res, (u0 + fw) * res, (v0 + fh) * res))
            if gt.getbbox():
                gw = gt.transform((bx1 - bx0, by1 - by0), Image.PERSPECTIVE, coeffs, Image.NEAREST, fillcolor=(0, 0, 0, 0))
                glow_canvas.alpha_composite(gw, (bx0, by0))
    if glow is not None:
        g = np.array(glow_canvas, dtype=np.float32)
        base = np.array(canvas, dtype=np.float32)
        a = g[..., 3:4] / 255.0
        base[..., :3] = np.clip(base[..., :3] * (1 - a * 0.5) + g[..., :3] * a, 0, 255)
        canvas = Image.fromarray(base.astype(np.uint8), "RGBA")
    return canvas


def render_views(model: Model, texture: Image.Image, glow: Image.Image | None, target: str,
                 views=((30, 20), (150, 20), (90, 0), (0, 80)), size: int = 640, pose: dict | None = None,
                 zoom: float = 1.0, focus=None) -> None:
    """Mehrere Blickwinkel nebeneinander in eine Datei (Standard: schräg vorn, schräg hinten, Seite, von oben)."""
    imgs = [render(model, texture, glow, yaw, pitch, size, pose, zoom=zoom, focus=focus) for yaw, pitch in views]
    cols = 2
    rows = (len(imgs) + 1) // 2
    sheet = Image.new("RGBA", (cols * size, rows * size), (30, 32, 36, 255))
    for i, im in enumerate(imgs):
        sheet.paste(im, ((i % cols) * size, (i // cols) * size))
    sheet.save(target)
