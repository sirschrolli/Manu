package io.github.sirschrolli.kingdomsatwar.client.renderer.serpent;

import io.github.sirschrolli.kingdomsatwar.entity.serpent.QuetzaraEntity;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Was Quetzara (oder ihre abgestreifte Haut) für ein Bild braucht: Kopf, Kiefer, Krause, Nacken und die Glieder entlang der Spur. */
public class QuetzaraRenderState extends EntityRenderState {
    public float headYaw;
    public float headPitch;
    public float jaw;
    public float ruff;
    /** Wie weit die Zunge herausschnellt (0 … 1). */
    public float tongue;
    public boolean asleep;
    public boolean molted;
    public boolean hurt;
    /** Die abgestreifte Haut: leere Augen, aufgerissener Kopf. */
    public boolean shed;
    /** Sichtbarkeit (getarnt kaum zu sehen, die Haut zerfällt). */
    public float alpha = 1.0f;
    public float deathTime;
    /** Der Nacken, bezogen auf die Lage des Wesens. */
    public double jx;
    public double jy;
    public double jz;
    public final Segment[] segments = new Segment[QuetzaraEntity.SEGMENTS + 1];

    public QuetzaraRenderState() {
        for (int i = 0; i < this.segments.length; i++) {
            this.segments[i] = new Segment();
        }
    }

    /** Ein Glied (oder der Schwanz): Lage relativ zum Wesen, Richtung, Dicke. */
    public static class Segment extends EntityRenderState {
        public double dx;
        public double dy;
        public double dz;
        public float yaw;
        public float pitch;
        public float roll;
        public float girth = 1.0f;
        public int index;
        public boolean visible = true;
    }
}
