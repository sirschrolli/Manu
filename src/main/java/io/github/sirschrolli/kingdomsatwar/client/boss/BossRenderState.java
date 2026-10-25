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
    public float flapPhase;
    /** 0 = am Boden (Flügel angelegt), 1 = in der Luft. */
    public float airBlend;
    /** Schräglage im Kurvenflug (Grad). */
    public float bank;
    /** Neigung im Flug (Grad, positiv = Nase nach oben). */
    public float flightPitch;
    /** Drehung des Körpers vor 0..n Ticks relativ zu jetzt (Grad) – für nachschwingenden Schwanz und Hals. */
    public final float[] yawLag = new float[48];
    /** Helligkeit der leuchtenden Teile (0..1+): pulsiert, glüht vor dem Odem auf, erlischt im Tod. */
    public float glow = 1.0f;
}
