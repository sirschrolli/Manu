"""
Esel und Maultiere auf dem Vanilla-kompatiblen Skelett (siehe equine.py; passt zu Vanilla-Sattel und Packtaschen).

  * Grauesel        – mausgrau mit dunklem Schulterkreuz, hellem Maul, Augenringen und Bauch, gestreifte Beine
  * Brauner Esel    – dunkelbraun mit mehlfarbenem Maul
  * Maultier        – braun wie ein Pferd, aber mit Eselsohren und hellem Maul
  * Rappmaultier    – schwarzbraun mit hellem Maul

Knochen-Namen werden in KawDonkeyModel (Java) verwendet; Esel tragen eine Schweifquaste, Maultiere einen Pferdeschweif.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import ramp
from . import equine

_COMMON = {"eye": ramp("#0a0604", "#4a3220"), "hoof": ramp("#1a1612", "#5a5048"), "hoof_light": ramp("#6a5a44", "#d8c8a4"),
           "white": ramp("#b8b4ac", "#ffffff"), "skin": ramp("#16141a", "#4a4448"), "skin_pink": ramp("#6a4a48", "#d8a8a0"),
           "leather": ramp("#3a1e0c", "#a8683a"), "strap": ramp("#1e120a", "#6a4428"), "brass": ramp("#6a4a10", "#f8d860")}

PALETTES = {
    "donkey_grau": {**_COMMON, "fur": ramp("#4a4440", "#b0a89e"), "belly": ramp("#a8a094", "#f4f0e8"),
                    "mane": ramp("#16120e", "#4a4038"), "points": ramp("#2a2420", "#6a6058")},
    "donkey_braun": {**_COMMON, "fur": ramp("#2a1a10", "#8a6448"), "belly": ramp("#a8988a", "#f0e8dc"),
                     "mane": ramp("#120c08", "#3a2c22"), "points": ramp("#1a120c", "#5a4434")},
    "mule_braun": {**_COMMON, "fur": ramp("#3a1e0e", "#b0703e"), "belly": ramp("#a89078", "#f0dcc4"),
                   "mane": ramp("#120a06", "#4a3022"), "points": ramp("#120a06", "#4a3022")},
    "mule_rappe": {**_COMMON, "fur": ramp("#100c0c", "#4a3c38"), "belly": ramp("#8a7a6e", "#d8c8b8"),
                   "mane": ramp("#080606", "#2e2624"), "points": ramp("#080606", "#2e2624")},
}
for _p in PALETTES.values():
    _p["back"] = _p["fur"]
    _p["patch"] = _p["white"]

FEATURES = {
    "donkey_grau": {"mealy": True, "cross": True, "dorsal": True, "leg_stripes": True, "ear_tips": True, "ear_rims": True},
    "donkey_braun": {"mealy": True, "dorsal": True, "ear_tips": True},
    "mule_braun": {"mealy": True, "points": True, "ear_tips": True},
    "mule_rappe": {"mealy": True, "ear_tips": True},
}


def build() -> Model:
    m = Model("donkey", 128, 128)
    equine.skeleton(m, donkey=True)
    return m


def materials(variant: str) -> dict:
    f = FEATURES[variant]
    return {
        "coat": equine.coat(f),
        "neck": equine.neck(f),
        "head": equine.head(f),
        "mouth": equine.mouth(f),
        "leg": equine.leg(f),
        "ear": equine.ear(f),
        "mane": equine.hair("mane", seed=97, highlights=False),
        "tail": equine.hair("mane", seed=98),
        "tail_core": equine.coat(f),
        "chest": equine.chest,
    }


def hidden(variant: str) -> list[str]:
    if variant.startswith("mule"):
        return ["left_ear", "right_ear", "tail_tuft"]
    return ["left_ear_mule", "right_ear_mule", "tail_hair"]
