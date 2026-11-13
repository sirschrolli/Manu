#!/usr/bin/env python3
"""
Vorschau eines Block-Modells (Modell-JSON wie aus der Datengenerierung) als Bild – ohne Minecraft zu starten.

Zeichnet alle Elemente mit ihren Texturen aus mehreren Blickwinkeln (Maler-Algorithmus, Schattierung der Seiten wie im
Spiel, leuchtende Flächen heller). Gedrehte Elemente werden wie im Spiel gedreht: Euler-Winkel x, y, z um ihren
Ursprung, zuerst um X, dann Y, dann Z (Matrix4f.rotationZYX), oder um eine einzelne Achse ("axis"/"angle").
Texturen aus dem eigenen Namensraum kommen aus src/main/resources, Vanilla-Texturen aus einem entpackten Spielverzeichnis
(--vanilla, das Verzeichnis mit assets/minecraft).

    python3 tools/block_preview.py MODELL.json AUSGABE.png [--vanilla PFAD] [--size 420]
"""
from __future__ import annotations

import argparse
import json
import math
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
OWN = ROOT / "src" / "main" / "resources" / "assets"

NORMALS = {"north": (0, 0, -1), "south": (0, 0, 1), "west": (-1, 0, 0), "east": (1, 0, 0), "up": (0, 1, 0), "down": (0, -1, 0)}
SHADE = {"up": 1.0, "down": 0.5, "north": 0.8, "south": 0.8, "west": 0.6, "east": 0.6}


def rot_x(a: float) -> np.ndarray:
    c, s = math.cos(a), math.sin(a)
    return np.array([[1, 0, 0], [0, c, -s], [0, s, c]])


def rot_y(a: float) -> np.ndarray:
    c, s = math.cos(a), math.sin(a)
    return np.array([[c, 0, s], [0, 1, 0], [-s, 0, c]])


def rot_z(a: float) -> np.ndarray:
    c, s = math.cos(a), math.sin(a)
    return np.array([[c, -s, 0], [s, c, 0], [0, 0, 1]])


def element_rotation(rot: dict | None) -> tuple[np.ndarray, np.ndarray]:
    if not rot:
        return np.eye(3), np.zeros(3)
    origin = np.array(rot.get("origin", [8, 8, 8]), dtype=float)
    if "axis" in rot:
        a = math.radians(rot.get("angle", 0.0))
        m = {"x": rot_x, "y": rot_y, "z": rot_z}[rot["axis"]](a)
    else:
        m = rot_z(math.radians(rot.get("z", 0.0))) @ rot_y(math.radians(rot.get("y", 0.0))) @ rot_x(math.radians(rot.get("x", 0.0)))
    return m, origin


def face_corners(f: np.ndarray, t: np.ndarray, face: str) -> list[np.ndarray]:
    """Ecken in Texturreihenfolge: oben links, oben rechts, unten rechts, unten links."""
    x0, y0, z0 = f
    x1, y1, z1 = t
    pts = {
        "north": [(x1, y1, z0), (x0, y1, z0), (x0, y0, z0), (x1, y0, z0)],
        "south": [(x0, y1, z1), (x1, y1, z1), (x1, y0, z1), (x0, y0, z1)],
        "west": [(x0, y1, z0), (x0, y1, z1), (x0, y0, z1), (x0, y0, z0)],
        "east": [(x1, y1, z1), (x1, y1, z0), (x1, y0, z0), (x1, y0, z1)],
        "up": [(x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)],
        "down": [(x0, y0, z1), (x1, y0, z1), (x1, y0, z0), (x0, y0, z0)],
    }[face]
    return [np.array(p, dtype=float) for p in pts]


def default_uv(f, t, face: str) -> tuple[float, float, float, float]:
    x0, y0, z0 = f
    x1, y1, z1 = t
    return {
        "north": (16 - x1, 16 - y1, 16 - x0, 16 - y0),
        "south": (x0, 16 - y1, x1, 16 - y0),
        "west": (z0, 16 - y1, z1, 16 - y0),
        "east": (16 - z1, 16 - y1, 16 - z0, 16 - y0),
        "up": (x0, z0, x1, z1),
        "down": (x0, 16 - z1, x1, 16 - z0),
    }[face]


def homography(src, dst) -> list[float]:
    a, b = [], []
    for (x, y), (u, v) in zip(src, dst):
        a.append([x, y, 1, 0, 0, 0, -u * x, -u * y])
        a.append([0, 0, 0, x, y, 1, -v * x, -v * y])
        b.extend([u, v])
    sol, *_ = np.linalg.lstsq(np.array(a, dtype=float), np.array(b, dtype=float), rcond=None)
    return list(sol)


def load_texture(ref: str, vanilla: Path | None, own: Path = OWN) -> Image.Image:
    ns, _, path = ref.partition(":") if ":" in ref else ("minecraft", ":", ref)
    base = own if ns != "minecraft" else (vanilla / "assets" if vanilla else None)
    if base is None:
        return Image.new("RGBA", (16, 16), (128, 128, 128, 255))
    file = base / ns / "textures" / f"{path}.png"
    if not file.exists():
        return Image.new("RGBA", (16, 16), (255, 0, 255, 255))
    img = Image.open(file).convert("RGBA")
    return img.crop((0, 0, img.width, img.width))   # nur das erste Bild einer Animation


def resolve(textures: dict, key: str) -> str:
    seen = 0
    while key.startswith("#") and seen < 8:
        key = textures.get(key[1:], "")
        seen += 1
    return key


def render(model: dict, vanilla: Path | None, yaw: float, pitch: float, size: int, ambient: float = 1.0,
           background=(38, 40, 46, 255), own: Path = OWN) -> Image.Image:
    textures = model.get("textures", {})
    cache: dict[str, Image.Image] = {}
    # yaw 0: Blick nach Süden (+Z), man sieht die Nordseite; pitch > 0: von oben
    cam = rot_x(math.radians(-pitch)) @ rot_y(math.radians(yaw))
    quads = []
    for e in model.get("elements", []):
        f = np.array(e["from"], dtype=float)
        t = np.array(e["to"], dtype=float)
        m, origin = element_rotation(e.get("rotation"))
        glow = e.get("light_emission", 0)
        for face, spec in e.get("faces", {}).items():
            corners = [m @ (p - origin) + origin - 8.0 for p in face_corners(f, t, face)]
            n = m @ np.array(NORMALS[face], dtype=float)
            cs = [cam @ p for p in corners]
            nc = cam @ n
            if nc[2] > 1e-6:          # Kamera blickt in +Z: abgewandt
                continue
            near = max(NORMALS, key=lambda k: float(np.dot(NORMALS[k], n)))
            shade = SHADE[near] if e.get("shade", True) else 1.0
            light = 1.0 if glow >= 12 else ambient + (1 - ambient) * glow / 15.0
            ref = resolve(textures, spec.get("texture", ""))
            if ref not in cache:
                cache[ref] = load_texture(ref, vanilla, own)
            uv = spec.get("uv") or default_uv(e["from"], e["to"], face)
            quads.append((sum(q[2] for q in cs) / 4, cs, cache[ref], uv, shade * light, spec.get("rotation", 0)))
    canvas = Image.new("RGBA", (size, size), background)
    scale = size / 22.0

    def to_screen(q):
        return (size / 2 - q[0] * scale, size / 2 - q[1] * scale + size * 0.08)

    for _, cs, tex, uv, shade, uvrot in sorted(quads, key=lambda q: -q[0]):
        scr = [to_screen(q) for q in cs]
        xs = [s[0] for s in scr]
        ys = [s[1] for s in scr]
        bx0, by0 = max(0, int(math.floor(min(xs)))), max(0, int(math.floor(min(ys))))
        bx1, by1 = min(size, int(math.ceil(max(xs))) + 1), min(size, int(math.ceil(max(ys))) + 1)
        if bx1 <= bx0 or by1 <= by0:
            continue
        res = tex.width / 16.0
        u0, v0, u1, v1 = (c * res for c in uv)
        dst = [(u0, v0), (u1, v0), (u1, v1), (u0, v1)]
        for _ in range(int(uvrot) // 90):
            dst = dst[1:] + dst[:1]
        local = [(s[0] - bx0, s[1] - by0) for s in scr]
        try:
            coeffs = homography(local, dst)
        except np.linalg.LinAlgError:
            continue
        img = tex
        if shade < 0.999:
            arr = np.array(tex, dtype=np.float32)
            arr[..., :3] *= shade
            img = Image.fromarray(arr.clip(0, 255).astype(np.uint8), "RGBA")
        warped = img.transform((bx1 - bx0, by1 - by0), Image.PERSPECTIVE, coeffs, Image.NEAREST, fillcolor=(0, 0, 0, 0))
        canvas.alpha_composite(warped, (bx0, by0))
    return canvas


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("model")
    ap.add_argument("out")
    ap.add_argument("--vanilla", type=Path)
    ap.add_argument("--size", type=int, default=420)
    ap.add_argument("--ambient", type=float, default=0.55, help="Helligkeit nicht leuchtender Flächen (Höhle: niedrig)")
    ap.add_argument("--own", type=Path, default=OWN, help="Verzeichnis mit den eigenen Assets (statt src/main/resources/assets)")
    args = ap.parse_args()
    model = json.loads(Path(args.model).read_text())
    views = [(180, 12), (215, 24), (270, 10), (160, 55), (0, 15), (125, 30)]
    imgs = [render(model, args.vanilla, yaw, pitch, args.size, args.ambient, own=args.own) for yaw, pitch in views]
    cols = 3
    rows = (len(imgs) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * args.size, rows * args.size), (20, 22, 26, 255))
    for i, im in enumerate(imgs):
        sheet.paste(im, ((i % cols) * args.size, (i // cols) * args.size))
    sheet.save(args.out)


if __name__ == "__main__":
    main()
