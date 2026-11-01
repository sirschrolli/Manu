"""
Pferde: neun Rassen auf dem Vanilla-kompatiblen Skelett (siehe equine.py), damit Sattel und Rossharnisch passen.

  * Fuchs       – kupferrot mit Blesse und weißer Fessel hinten links
  * Brauner     – braun mit schwarzen Beinen, Mähne und Schweif, kleiner Stern
  * Friese      – tiefschwarz, lange Seitenmähne, schwarzer Fesselbehang
  * Schimmel    – Apfelschimmel mit runden hellen Tupfen, silberne Mähne
  * Schecke     – weiß mit großen braunen Platten, weiße Beine
  * Haflinger   – goldfuchsfarben mit flachsblonder Mähne und Blesse
  * Kaltblut    – großes, schweres Arbeitspferd: dunkelbraun, weißer Fesselbehang, Laterne (größer)
  * Falbe       – sandgelb mit Aalstrich, Zebrastreifen an den Beinen (Steppe, Savanne)
  * Pony        – klein, schokobraun mit silberner Mähne und dichtem Schopf (kleiner)

Knochen-Namen werden in KawHorseModel (Java) verwendet.
"""
from __future__ import annotations

from ..geometry import Model
from ..paint import ramp
from . import equine, farm

BREEDS = ["fuchs", "brauner", "friese", "schimmel", "schecke", "haflinger", "kaltblut", "falbe", "pony"]

_COMMON = {"eye": ramp("#0a0604", "#5a3a1e"), "hoof": ramp("#1a1612", "#5a5048"), "hoof_light": ramp("#6a5a44", "#d8c8a4"),
           "white": ramp("#b8b4ac", "#ffffff"), "skin": ramp("#16141a", "#5a525a"), "skin_pink": ramp("#7a5050", "#e8b0a8")}

PALETTES = {
    "horse_fuchs": {**_COMMON, "fur": ramp("#5a240c", "#e0884a"), "belly": ramp("#8a4a24", "#f0a868"),
                    "mane": ramp("#4a1e0a", "#d8884a"), "points": ramp("#4a1e0a", "#a8582a")},
    "horse_brauner": {**_COMMON, "fur": ramp("#3a1a0a", "#b86a38"), "belly": ramp("#5a2e16", "#c88a58"),
                      "mane": ramp("#0a0808", "#3a3230"), "points": ramp("#0a0808", "#3a3230")},
    "horse_friese": {**_COMMON, "fur": ramp("#08080c", "#4a4c58"), "belly": ramp("#0c0c10", "#3a3a44"),
                     "mane": ramp("#060608", "#3e4048"), "points": ramp("#060608", "#2a2a30"),
                     "feather": ramp("#060608", "#3a3c44")},
    "horse_schimmel": {**_COMMON, "fur": ramp("#4a4c52", "#c8c8cc"), "belly": ramp("#8a8a90", "#f0f0f2"),
                       "mane": ramp("#6a6a70", "#f4f4f6"), "points": ramp("#2a2a30", "#7a7a80"),
                       "feather": ramp("#8a8a90", "#f4f4f6")},
    "horse_schecke": {**_COMMON, "fur": ramp("#3a1c0c", "#a8602e"), "belly": ramp("#4a2410", "#b87038"),
                      "mane": ramp("#1a0e08", "#e8e0d4"), "points": ramp("#3a1c0c", "#a8602e"),
                      "white": ramp("#c0bcb4", "#ffffff")},
    "horse_haflinger": {**_COMMON, "fur": ramp("#6a3410", "#e8a050"), "belly": ramp("#9a5a24", "#f8c070"),
                        "mane": ramp("#a8885a", "#fff6dc"), "points": ramp("#6a3410", "#c88040"),
                        "feather": ramp("#a8885a", "#fff6dc")},
    "horse_kaltblut": {**_COMMON, "fur": ramp("#22120a", "#7a4a2a"), "belly": ramp("#3a2214", "#8a5a38"),
                       "mane": ramp("#0a0806", "#3a2c24"), "points": ramp("#0a0806", "#3a2c24"),
                       "feather": ramp("#b0aaa0", "#fffcf4")},
    "horse_falbe": {**_COMMON, "fur": ramp("#7a5a2a", "#f0d28e"), "belly": ramp("#a88a5a", "#fbe8b8"),
                    "mane": ramp("#120c08", "#4a3a2a"), "points": ramp("#120c08", "#4a3a2a")},
    "horse_pony": {**_COMMON, "fur": ramp("#26140c", "#8a5a3c"), "belly": ramp("#3a2216", "#9a6a4a"),
                   "mane": ramp("#8a8680", "#f8f6f0"), "points": ramp("#26140c", "#7a4a30"),
                   "feather": ramp("#8a8680", "#f8f6f0")},
}
for _p in PALETTES.values():
    _p["back"] = _p["fur"]
    _p.setdefault("feather", _p["mane"])
    _p["patch"] = _p["white"]

FEATURES = {
    "horse_fuchs": {"face": "blaze", "socks": {"left_hind_leg": 0.32}},
    "horse_brauner": {"face": "star", "points": True, "ear_rims": True},
    "horse_friese": {"points": True},
    "horse_schimmel": {"dapples": True, "points": True, "contrast": 0.1},
    "horse_schecke": {"pinto": True, "face": "bald",
                      "socks": {n: 0.55 for n in equine.LEGS}},
    "horse_haflinger": {"face": "blaze"},
    "horse_kaltblut": {"face": "blaze", "points": True, "socks": {n: 0.5 for n in equine.LEGS}},
    "horse_falbe": {"dorsal": True, "points": True, "leg_stripes": True, "ear_tips": True},
    "horse_pony": {"face": "star", "socks": {"right_front_leg": 0.25}},
}

DRAPE = {"horse_friese", "horse_schimmel", "horse_haflinger", "horse_kaltblut", "horse_pony"}
BIG_FORELOCK = {"horse_friese", "horse_kaltblut", "horse_pony"}
FEATHERING = {"horse_friese", "horse_kaltblut", "horse_pony"}


def build() -> Model:
    m = Model("horse", 128, 128)
    equine.skeleton(m, donkey=False)
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
        "mane": equine.hair("mane", seed=91),
        "tail": equine.hair("mane", seed=93),
        "feather": farm.shaggy("feather", seed=95, length=1.4, contrast=0.3),
    }


def hidden(variant: str) -> list[str]:
    out = []
    if variant not in DRAPE:
        out.append("mane_drape")
    out.append("forelock" if variant in BIG_FORELOCK else "forelock_big")
    if variant not in FEATHERING:
        out += ["feather_" + n for n in equine.LEGS]
    return out
