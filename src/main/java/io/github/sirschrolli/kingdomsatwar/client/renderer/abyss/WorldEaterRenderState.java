package io.github.sirschrolli.kingdomsatwar.client.renderer.abyss;

import io.github.sirschrolli.kingdomsatwar.entity.abyss.WorldEaterEntity;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;

/** Was der Weltenfresser für ein Bild braucht: Kopf, Rachen, die Glieder entlang der Bahn und der Rissatem. */
public class WorldEaterRenderState extends EntityRenderState {
    public float yaw;
    public float pitch;
    public float maw;
    public boolean hurt;
    public float deathTime;
    public boolean wrath;
    public boolean awakening;
    public boolean breathing;
    public Vec3 breathFrom = Vec3.ZERO;
    public Vec3 breathTo = Vec3.ZERO;
    public final Segment[] segments = new Segment[WorldEaterEntity.SEGMENTS + 1];

    public WorldEaterRenderState() {
        for (int i = 0; i < this.segments.length; i++) {
            this.segments[i] = new Segment();
        }
    }

    /** Ein Glied (oder der Schwanz): Lage relativ zum Kopf, Richtung, welches Weltstück es trägt. */
    public static class Segment extends EntityRenderState {
        public double dx;
        public double dy;
        public double dz;
        public float yaw;
        public float pitch;
        public float roll;
        public int index;
        public int world;
        public boolean broken;
    }
}
