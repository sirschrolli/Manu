package io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.phys.Vec3;

/** Zeichenzustand des Zerbrochenen Königs. */
public class ShatteredKingRenderState extends LivingEntityRenderState {
    public final AnimationState awaken = new AnimationState();
    public final AnimationState slamLeft = new AnimationState();
    public final AnimationState slamRight = new AnimationState();
    public final AnimationState sweep = new AnimationState();
    public final AnimationState volley = new AnimationState();
    public final AnimationState gravity = new AnimationState();
    public final AnimationState summon = new AnimationState();
    public final AnimationState beam = new AnimationState();

    /** Ziel des Faustschlags im Modellraum (Pixel, wie die Knochen), damit die Hand genau dort aufschlägt. */
    public float slamX;
    public float slamY = 42.0f;
    public float slamZ = -36.0f;
    /** Ziel des Faustschlags in der Welt, relativ zum König (für den Warnkreis am Boden), und wie hell der Kreis ist. */
    public Vec3 slamOffset = Vec3.ZERO;
    public float slamWarn;

    /** Sternenstrahl: läuft er, wie stark, und von wo bis wo (relativ zum König). */
    public boolean beaming;
    public float beamPower;
    public Vec3 beamFrom = Vec3.ZERO;
    public Vec3 beamTo = Vec3.ZERO;
    public boolean beamHit;
}
