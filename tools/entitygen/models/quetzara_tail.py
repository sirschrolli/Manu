"""Quetzara: der Schwanz mit dem Federbusch (siehe quetzara.py)."""
from .quetzara import build_tail as build, materials  # noqa: F401
from .quetzara import PALETTES as _P

PALETTES = {"quetzara_tail": _P["quetzara"], "quetzara_tail_molted": _P["quetzara_molted"], "quetzara_tail_shed": _P["quetzara_shed"]}
