package io.github.sirschrolli.kingdomsatwar.client.renderer.glitch;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Was Annis Modell zum Zeichnen braucht (auch für ihre Klone und Nachbilder). */
public class AnniTrollRenderState extends LivingEntityRenderState {
    public int attack;
    public float attackTime;
    /** Hängt sie gerade (Ladekreis)? */
    public boolean lagging;
    /** Ticks seit Beginn des Hängens (oder -1). */
    public float lagTime = -1.0f;
    /** Ticks seit dem Loslassen (oder -1, solange sie hängt). */
    public float sinceLag = 999.0f;
    public boolean glitchMode;
    /** Anni 2.0 (aus der Sicherung wiederhergestellt): rote Augen, noch mehr Bildfehler. */
    public boolean version2;
    /** Ein Fehler-Klon (einfarbig, durchscheinend) oder ein Nachbild (erstarrt, verblassend)? */
    public boolean clone;
    public boolean afterimage;
    /** Farbe des Klons oder Nachbilds (RGB) und Deckkraft (0–1). */
    public int tint = 0xFFFFFF;
    public float alpha = 1.0f;
    /** Für zufällige, aber pro Tick feste Bildfehler. */
    public int seed;
    /** Versatz der Farbabbilder im Modellraum (Blöcke). */
    public float splitX;
    public float splitZ;
}
