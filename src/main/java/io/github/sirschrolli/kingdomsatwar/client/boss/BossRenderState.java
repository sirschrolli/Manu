package io.github.sirschrolli.kingdomsatwar.client.boss;

import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Alles, was zum Zeichnen eines Drachen-Bosses in diesem Bild gebraucht wird. */
public class BossRenderState extends LivingEntityRenderState {
    public DragonBossEntity.State bossState = DragonBossEntity.State.FIGHT;
    public DragonBossEntity.MoveMode moveMode = DragonBossEntity.MoveMode.GROUND;
    /** Laufende Angriffs-Animation ({@link io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims}). */
    public int attack;
    /** Sekunden seit Beginn des Angriffs. */
    public float attackTime;
    /** Sekunden im aktuellen Zustand (Erwachen, Einschlafen). */
    public float stateTime;
    /** Sekunden seit dem Todesstoß. */
    public float deathSeconds;
    public int phase = 1;
    /** Noch verborgen (z. B. Glaciara im Eiskristall, bis er zerspringt): wird nicht gezeichnet. */
    public boolean hidden;
    /** Wut (0 = volles Leben … 1 = fast besiegt): die leuchtenden Teile glühen heller und pulsieren schneller. */
    public float rage;
    /** Gestärkt (Kristallpanzer, Aufladung …): die leuchtenden Teile glühen hell. */
    public boolean empowered;
    public float flapPhase;
    /** 0 = am Boden (Flügel angelegt), 1 = in der Luft. */
    public float airBlend;
    /** Schräglage im Kurvenflug (Grad). */
    public float bank;
    /** Neigung im Flug (Grad, positiv = Nase nach oben). */
    public float flightPitch;
    /** Helligkeit der leuchtenden Teile (0..1+): pulsiert, glüht vor dem Odem auf, erlischt im Tod. */
    public float glow = 1.0f;
    /** Laufende Laune ({@code BossAnims.MOOD_*}), Sekunden seit ihrem Beginn und Richtung (±1). */
    public int idleEvent;
    public float idleTime;
    public float idleSide = 1.0f;

    // ---- Ergebnis der Federn (DragonRig) – das Modell wendet es an
    private static final float[] NONE = new float[0];
    public float[] neckPitch = NONE;
    public float[] neckYaw = NONE;
    public float[] neckRoll = NONE;
    public float[] tailYaw = NONE;
    public float[] tailPitch = NONE;
    public float headPitch;
    public float headYaw;
    public float headRoll;
    public float jaw;
    public float bodyY;
    public float bodyPitch;
    public float bodyRoll;
    public float bodyYaw;
    public float chest = 1.0f;
    /** Je Seite (0 = links, 1 = rechts): wie weit Oberarm, Unterarm und Hand angelegt sind (1 = ganz). */
    public final float[] foldArm = {1.0f, 1.0f};
    public final float[] foldFore = {1.0f, 1.0f};
    public final float[] foldHand = {1.0f, 1.0f};
    public float flapAmp;
    public float flap;
    public float raise;
    public float sweep;
    public float spread;
    public final float[][] legs = new float[4][4];

    void ensureRig(int necks, int tails) {
        if (this.neckPitch.length != necks) {
            this.neckPitch = new float[necks];
            this.neckYaw = new float[necks];
            this.neckRoll = new float[necks];
        }
        if (this.tailYaw.length != tails) {
            this.tailYaw = new float[tails];
            this.tailPitch = new float[tails];
        }
    }
}
