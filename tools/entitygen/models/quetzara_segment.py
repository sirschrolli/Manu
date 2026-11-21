"""Quetzara: ein Glied des Leibs (siehe quetzara.py)."""
from .quetzara import build_segment as build, materials  # noqa: F401
from .quetzara import PALETTES as _P

PALETTES = {"quetzara_segment": _P["quetzara"], "quetzara_segment_molted": _P["quetzara_molted"],
            "quetzara_segment_shed": _P["quetzara_shed"]}
