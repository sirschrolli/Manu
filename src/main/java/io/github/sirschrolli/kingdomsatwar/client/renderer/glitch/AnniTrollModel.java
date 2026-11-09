package io.github.sirschrolli.kingdomsatwar.client.renderer.glitch;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.AnniTrollEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Anni der Troll. Er stapft gebeugt, den Bannhammer tief vor sich, der Haarschopf wippt, die Pixelkrone kreist, die
 * Brocken seines linken Unterarms und die losen Pixel schweben auf und ab, der Kiefer mahlt. Für jeden Angriff nimmt er
 * seine Haltung ein (weich überblendet):
 * <ul>
 *     <li>Lag-Hieb: den Hammer hoch über den Kopf – erstarrt, solange er hängt –, dann kracht der Hammer herab;</li>
 *     <li>Gummiband: ausgebreitete Arme beim Springen, zuletzt ein Rundumschlag;</li>
 *     <li>Fehler-Klone: die Arme weit, der Körper bebt, dann lacht er;</li>
 *     <li>Fehlende Textur und Zeilenfehler: den Hammer beidhändig empor und in den Boden;</li>
 *     <li>Pixelregen: den Hammer zum Himmel, den Kopf im Nacken;</li>
 *     <li>Rückruck: den zerfallenen Arm ausgestreckt, dann ein Ansturm mit Hieb;</li>
 *     <li>Absturz: in einer verdrehten Haltung erstarrt, dann richtet er sich auf und schlägt zu;</li>
 *     <li>Wiederherstellung: Er sackt zusammen wie im Tod und erstarrt; als Anni 2.0 reckt er den Hammer, schlägt zu, lacht;</li>
 *     <li>Speedhack: ausholen (erstarrt, solange er zielt), dann vorgebeugt mit rudernden Beinen voran;</li>
 *     <li>Lachen: den Kopf im Nacken, die Hand am Bauch; Tod: die Knie geben nach, er sackt zusammen.</li>
 * </ul>
 * Dazu kommen Bildfehler: Für einzelne Ticks springt ein Glied in eine falsche Haltung (im Fehlermodus öfter).
 */
public class AnniTrollModel extends EntityModel<AnniTrollRenderState> {
    private static final float D = Anim.DEG;
    private static final AnniTrollEntity.Attack[] ATTACKS = AnniTrollEntity.Attack.values();

    // Haltungen: 0–2 rechter Arm, 3–5 rechter Unterarm, 6 Hammer, 7–9 linker Arm, 10–12 linker Unterarm, 13–15 Rumpf,
    // 16–18 Kopf, 19 Kiefer, 20 rechtes Bein, 21 linkes Bein (Grad, zu den Grundwinkeln hinzu)
    private static final int N = 22;
    private static final float[] IDLE = {8, 0, 4, -24, 0, 0, -62, 6, 0, -4, -20, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0};
    private static final float[] RAISE = {-160, 0, 12, -15, 0, 0, -20, -45, 0, -25, -35, 0, 0, -14, 0, 0, 10, 0, 0, 18, 0, 0};
    private static final float[] RAISE2 = {-160, 0, 12, -15, 0, 0, -20, -160, 0, -14, -15, 0, 0, -16, 0, 0, 12, 0, 0, 22, 0, 0};
    private static final float[] SLAM = {-70, 0, 6, -10, 0, 0, -15, -20, 0, -30, -20, 0, 0, 22, 0, 0, -20, 0, 0, 10, -20, 15};
    private static final float[] SLAM2 = {-70, 0, 6, -10, 0, 0, -15, -70, 0, -8, -10, 0, 0, 24, 0, 0, -18, 0, 0, 14, -20, 15};
    private static final float[] SKY = {-178, 0, 4, -4, 0, 0, 0, -30, 0, -62, -10, 0, 0, -16, 0, 0, 30, 0, 0, 26, 0, 0};
    private static final float[] POINT = {12, 0, 6, -30, 0, 0, -62, -96, -10, -8, -4, 0, 0, -6, -10, 0, -4, -8, 0, 8, 0, 0};
    private static final float[] WIND = {-70, 70, 50, -20, 0, 0, -40, -10, 0, -40, -30, 0, 0, -4, 35, 0, 0, -20, 0, 14, 0, 0};
    private static final float[] SWING = {-80, -60, -10, -10, 0, 0, -30, -20, 0, -20, -20, 0, 0, 10, -40, 0, -6, 20, 0, 20, -10, 10};
    private static final float[] SPLIT = {-20, 0, 72, -10, 0, 0, -50, -20, 0, -72, -10, 0, 0, -8, 0, 0, 8, 0, 0, 22, 0, 0};
    private static final float[] LAUGH = {6, 0, 10, -30, 0, 0, -62, -48, 0, 18, -64, 0, 0, -12, 0, 0, -34, 0, 0, 32, 0, 0};
    private static final float[] CRASH = {-110, 30, 50, -60, 0, 0, -90, -30, 0, -100, 20, 0, 0, 6, 22, -8, 4, 0, 35, 34, -26, 20};
    private static final float[] DEATH = {40, 0, 14, -10, 0, 0, -30, 34, 0, -14, -10, 0, 0, 38, 0, 6, -16, 0, 10, 30, -40, -36};

    private final ModelPart body;
    private final ModelPart skirt;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart earL;
    private final ModelPart earR;
    private final ModelPart tuft;
    private final ModelPart crown;
    private final ModelPart[] crownPx = new ModelPart[3];
    private final ModelPart armR;
    private final ModelPart foreR;
    private final ModelPart handR;
    private final ModelPart hammer;
    private final ModelPart[] hammerPx = new ModelPart[3];
    private final ModelPart armL;
    private final ModelPart foreL;
    private final ModelPart[] shards = new ModelPart[3];
    private final ModelPart handL;
    private final ModelPart legR;
    private final ModelPart shinR;
    private final ModelPart legL;
    private final ModelPart shinL;
    private final ModelPart[] pixels = new ModelPart[6];
    private final ModelPart[] all;
    private final float[] p = new float[N];

    public AnniTrollModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.skirt = this.body.getChild("skirt");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.earL = this.head.getChild("left_ear");
        this.earR = this.head.getChild("right_ear");
        this.tuft = this.head.getChild("tuft");
        this.crown = this.head.getChild("crown");
        for (int i = 0; i < 3; i++) {
            this.crownPx[i] = this.crown.getChild("crown_" + i);
        }
        this.armR = this.body.getChild("right_arm");
        this.foreR = this.armR.getChild("right_forearm");
        this.handR = this.foreR.getChild("right_hand");
        this.hammer = this.handR.getChild("hammer");
        for (int i = 0; i < 3; i++) {
            this.hammerPx[i] = this.hammer.getChild("hammer_px_" + i);
        }
        this.armL = this.body.getChild("left_arm");
        this.foreL = this.armL.getChild("left_forearm");
        for (int i = 0; i < 3; i++) {
            this.shards[i] = this.foreL.getChild("left_shard_" + i);
        }
        this.handL = this.foreL.getChild("left_hand");
        this.legR = root.getChild("right_leg");
        this.shinR = this.legR.getChild("right_shin");
        this.legL = root.getChild("left_leg");
        this.shinL = this.legL.getChild("left_shin");
        for (int i = 0; i < 6; i++) {
            this.pixels[i] = this.body.getChild("pixel_" + i);
        }
        this.all = new ModelPart[] {this.body, this.head, this.jaw, this.armR, this.foreR, this.hammer, this.armL, this.foreL, this.legR,
                this.legL, this.tuft, this.earL, this.earR};
    }

    private void blend(float[] target, float w) {
        if (w <= 0.0f) {
            return;
        }
        for (int i = 0; i < N; i++) {
            this.p[i] += (target[i] - this.p[i]) * Math.min(1.0f, w);
        }
    }

    private static void add(ModelPart part, float x, float y, float z) {
        part.xRot += x * D;
        part.yRot += y * D;
        part.zRot += z * D;
    }

    /** Fest für einen Tick, sonst zufällig (für Bildfehler, die einen Tick lang stehen bleiben). */
    private static float hash(int seed, int tick, int salt) {
        int h = seed * 73_856_093 ^ tick * 19_349_663 ^ salt * 83_492_791;
        h ^= h >>> 13;
        h *= 0x5bd1e995;
        h ^= h >>> 15;
        return (h & 0xFFFF) / 65535.0f;
    }

    @Override
    public void setupAnim(AnniTrollRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float a = state.attackTime;
        AnniTrollEntity.Attack attack = state.attack >= 0 && state.attack < ATTACKS.length ? ATTACKS[state.attack] : AnniTrollEntity.Attack.NONE;
        System.arraycopy(IDLE, 0, this.p, 0, N);
        float walk = attack == AnniTrollEntity.Attack.NONE && !state.lagging ? Math.min(1.0f, state.walkAnimationSpeed * 1.6f) : 0.0f;
        float pos = state.walkAnimationPos;
        boolean glitch = state.glitchMode;
        float dashLean = 0.0f;

        // ---------------------------------------------------------- Haltung je Angriff
        switch (attack) {
            case SPAWN -> {
                float k = a - AnniTrollEntity.SPAWN_ASSEMBLE;
                this.blend(SKY, Anim.bump(k - 14.0f, 6.0f, 18.0f, 28.0f));
                this.blend(LAUGH, Anim.bump(k - 22.0f, 5.0f, 16.0f, 24.0f));
            }
            case SMASH -> {
                // ausholen, hängen (so lange der Ladekreis dreht), dann kracht er herab
                boolean released = !state.lagging && a > AnniTrollEntity.SMASH_RAISE && state.sinceLag >= 0.0f
                        && state.sinceLag <= a - AnniTrollEntity.SMASH_RAISE + 1.0f;
                float raise = Math.min(1.0f, a / 10.0f);
                if (released) {
                    raise = Math.max(0.0f, 1.0f - state.sinceLag / 2.0f);
                }
                this.blend(RAISE, raise);
                if (released) {
                    this.blend(SLAM, Anim.bump(state.sinceLag, 2.0f, 9.0f, 18.0f));
                }
            }
            case RUBBERBAND -> {
                int hops = AnniTrollEntity.hops(glitch, state.version2);
                float last = AnniTrollEntity.RUBBER_FIRST + (hops - 1) * AnniTrollEntity.RUBBER_STEP;
                this.blend(SPLIT, Anim.bump(a, 4.0f, last, last + 5.0f) * 0.6f);
                this.blend(WIND, Anim.bump(a - last - 5.0f, 6.0f, 8.0f, 10.0f));
                this.blend(SWING, Anim.bump(a - last - 12.0f, 2.0f, 8.0f, 18.0f));
            }
            case CLONES -> {
                this.blend(SPLIT, Anim.bump(a, 6.0f, AnniTrollEntity.CLONES_SPLIT + 4.0f, AnniTrollEntity.CLONES_SPLIT + 10.0f));
                this.blend(LAUGH, Anim.bump(a - AnniTrollEntity.CLONES_SPLIT - 6.0f, 5.0f, 18.0f, 24.0f));
            }
            case MISSING -> {
                this.blend(RAISE2, Anim.bump(a, 10.0f, AnniTrollEntity.MISSING_SLAM - 2.0f, AnniTrollEntity.MISSING_SLAM));
                this.blend(SLAM2, Anim.bump(a - AnniTrollEntity.MISSING_SLAM + 2.0f, 2.0f, 12.0f, 22.0f));
            }
            case SCANLINE -> {
                for (int at : new int[] {AnniTrollEntity.SCAN_LOW, AnniTrollEntity.SCAN_HIGH}) {
                    this.blend(RAISE2, Anim.bump(a - at + 10.0f, 7.0f, 8.0f, 10.0f));
                    this.blend(SLAM2, Anim.bump(a - at + 2.0f, 2.0f, 10.0f, 18.0f));
                }
            }
            case PIXEL_RAIN -> {
                this.blend(SKY, Anim.bump(a, 10.0f, AnniTrollEntity.RAIN_END, AnniTrollEntity.RAIN_END + 12.0f));
            }
            case ROLLBACK -> {
                this.blend(POINT, Anim.bump(a, 8.0f, AnniTrollEntity.ROLLBACK_SNAP, AnniTrollEntity.ROLLBACK_SNAP + 4.0f));
                this.blend(RAISE, Anim.bump(a - AnniTrollEntity.ROLLBACK_SNAP, 4.0f, 10.0f, 12.0f));
                this.blend(SLAM, Anim.bump(a - AnniTrollEntity.ROLLBACK_SNAP - 10.0f, 2.0f, 8.0f, 16.0f));
            }
            case CRASH -> {
                float frozen = a < AnniTrollEntity.CRASH_RESTART ? Math.min(1.0f, a / 3.0f)
                        : Math.max(0.0f, 1.0f - (a - AnniTrollEntity.CRASH_RESTART) / 8.0f);
                this.blend(CRASH, frozen);
                this.blend(RAISE2, Anim.bump(a - AnniTrollEntity.CRASH_RESTART - 2.0f, 5.0f, 7.0f, 8.0f));
                this.blend(SLAM2, Anim.bump(a - AnniTrollEntity.CRASH_WAVE + 1.0f, 2.0f, 10.0f, 18.0f));
                this.blend(LAUGH, Anim.bump(a - AnniTrollEntity.CRASH_WAVE - 10.0f, 5.0f, 14.0f, 20.0f));
            }
            case LAUGH -> this.blend(LAUGH, Anim.bump(a, 5.0f, 22.0f, 30.0f));
            case REVIVE -> {
                this.blend(DEATH, a < AnniTrollEntity.REVIVE_SHATTER ? Math.min(1.0f, a / 10.0f) : 0.0f);
                float k = a - AnniTrollEntity.REVIVE_ASSEMBLE;
                this.blend(SKY, Anim.bump(k, 2.0f, 4.0f, 6.0f));
                this.blend(SLAM2, Anim.bump(k - 4.0f, 2.0f, 10.0f, 16.0f));
                this.blend(LAUGH, Anim.bump(k - 16.0f, 5.0f, 18.0f, 26.0f));
            }
            case SPEEDHACK -> {
                float k = a - AnniTrollEntity.HACK_FIRST;
                int dash = k < 0.0f ? -1 : (int) (k / AnniTrollEntity.HACK_STEP);
                if (dash >= 0 && dash < AnniTrollEntity.HACK_DASHES) {
                    float p = k - dash * AnniTrollEntity.HACK_STEP;
                    this.blend(WIND, Anim.bump(p, 4.0f, AnniTrollEntity.HACK_AIM, AnniTrollEntity.HACK_AIM + 3.0f));
                    this.blend(SWING, Anim.bump(p - AnniTrollEntity.HACK_AIM, 2.0f, AnniTrollEntity.HACK_DASH - 2.0f, AnniTrollEntity.HACK_DASH + 6.0f)
                            * 0.7f);
                    dashLean = Anim.bump(p - AnniTrollEntity.HACK_AIM, 2.0f, AnniTrollEntity.HACK_DASH, AnniTrollEntity.HACK_DASH + 6.0f);
                } else if (dash >= AnniTrollEntity.HACK_DASHES) {
                    this.blend(LAUGH, Anim.bump(k - AnniTrollEntity.HACK_DASHES * AnniTrollEntity.HACK_STEP, 3.0f, 10.0f, 14.0f));
                }
            }
            default -> { }
        }
        if (state.deathTime > 0.0f) {
            this.blend(DEATH, Math.min(1.0f, state.deathTime / 14.0f));
        }

        // ---------------------------------------------------------- anwenden
        float[] q = this.p;
        add(this.armR, q[0], q[1], q[2]);
        add(this.foreR, q[3], q[4], q[5]);
        add(this.hammer, q[6], 0.0f, 0.0f);
        add(this.armL, q[7], q[8], q[9]);
        add(this.foreL, q[10], q[11], q[12]);
        add(this.body, q[13], q[14], q[15]);
        add(this.head, q[16], q[17], q[18]);
        add(this.jaw, q[19], 0.0f, 0.0f);
        add(this.legR, q[20], 0.0f, 0.0f);
        add(this.legL, q[21], 0.0f, 0.0f);

        // ---------------------------------------------------------- Gehen, Atmen, Blick
        if (walk > 0.0f) {
            float swing = Mth.cos(pos * 0.6662f) * 24.0f * walk;
            add(this.legR, swing, 0.0f, 0.0f);
            add(this.legL, -swing, 0.0f, 0.0f);
            add(this.shinR, Math.max(0.0f, -swing) * 0.6f, 0.0f, 0.0f);
            add(this.shinL, Math.max(0.0f, swing) * 0.6f, 0.0f, 0.0f);
            add(this.armL, -swing * 0.7f, 0.0f, 0.0f);
            add(this.armR, swing * 0.25f, 0.0f, 0.0f);
            add(this.body, 0.0f, Mth.sin(pos * 0.6662f) * 5.0f * walk, Mth.sin(pos * 0.6662f) * 2.0f * walk);
            this.body.y += Math.abs(Mth.sin(pos * 0.6662f)) * 1.2f * walk;
        }
        if (dashLean > 0.0f) {
            // Speedhack: weit vorgebeugt, die Beine rudern
            add(this.body, 26.0f * dashLean, 0.0f, 0.0f);
            float run = Mth.cos(t * 1.7f) * 42.0f * dashLean;
            add(this.legR, run, 0.0f, 0.0f);
            add(this.legL, -run, 0.0f, 0.0f);
            add(this.shinR, Math.max(0.0f, -run) * 0.8f, 0.0f, 0.0f);
            add(this.shinL, Math.max(0.0f, run) * 0.8f, 0.0f, 0.0f);
            add(this.armL, -run * 0.5f, 0.0f, 0.0f);
        }
        float breathe = Mth.sin(t * 0.08f);
        add(this.body, breathe * 1.2f, 0.0f, 0.0f);
        add(this.jaw, Mth.sin(t * 0.13f) * 2.5f + 1.5f, 0.0f, 0.0f);
        boolean frozen = attack == AnniTrollEntity.Attack.CRASH && a < AnniTrollEntity.CRASH_RESTART
                || attack == AnniTrollEntity.Attack.REVIVE && a < AnniTrollEntity.REVIVE_SHATTER || state.lagging;
        if (!frozen && state.deathTime <= 0.0f) {
            this.head.yRot += Mth.clamp(state.yRot, -45.0f, 45.0f) * D * 0.6f;
            this.head.xRot += Mth.clamp(state.xRot, -25.0f, 25.0f) * D * 0.4f;
        }
        if (attack == AnniTrollEntity.Attack.LAUGH || attack == AnniTrollEntity.Attack.CLONES && a > AnniTrollEntity.CLONES_SPLIT
                || attack == AnniTrollEntity.Attack.SPAWN && a > AnniTrollEntity.SPAWN_ASSEMBLE + 20
                || attack == AnniTrollEntity.Attack.REVIVE && a > AnniTrollEntity.REVIVE_ASSEMBLE + 16) {
            // Lachen: der Bauch bebt, der Kiefer klappert
            add(this.body, Mth.sin(t * 1.4f) * 2.0f, 0.0f, 0.0f);
            add(this.jaw, Mth.sin(t * 1.9f) * 6.0f, 0.0f, 0.0f);
        }

        // ---------------------------------------------------------- Haarschopf, Ohren, Krone, Pixel, Brocken
        add(this.tuft, Mth.sin(t * 0.07f) * 3.0f - walk * Mth.cos(pos * 1.3324f) * 3.0f, 0.0f, Mth.sin(t * 0.05f) * 3.0f);
        if (hash(state.seed, (int) (t / 4.0f), 7) > 0.9f) {
            add(this.earL, 0.0f, 0.0f, -14.0f);
        }
        if (hash(state.seed, (int) (t / 4.0f), 8) > 0.92f) {
            add(this.earR, 0.0f, 0.0f, 14.0f);
        }
        this.crown.yRot += t * (glitch ? 0.1f : 0.05f);
        for (int i = 0; i < 3; i++) {
            this.crownPx[i].y += Mth.sin(t * 0.11f + i * 2.1f) * 0.8f;
            this.crownPx[i].xRot += t * 0.05f + i;
            this.crownPx[i].yRot += t * 0.07f;
        }
        for (int i = 0; i < 6; i++) {
            this.pixels[i].y += Mth.sin(t * 0.07f + i * 1.3f) * 1.3f;
            this.pixels[i].x += Mth.cos(t * 0.05f + i * 2.0f) * 0.5f;
            this.pixels[i].yRot += t * 0.04f * (i % 2 == 0 ? 1 : -1);
            this.pixels[i].xRot += t * 0.03f;
        }
        float drift = glitch ? 2.0f : 1.0f;
        for (int i = 0; i < 3; i++) {
            this.shards[i].y += (Mth.sin(t * 0.12f + i * 1.7f) * 0.5f + 0.5f) * 0.7f * drift * (i + 1) * 0.5f;
            this.shards[i].x += Mth.sin(t * 0.09f + i * 2.3f) * 0.3f * drift;
            this.shards[i].yRot += Mth.sin(t * 0.06f + i) * 0.08f * drift;
        }
        this.handL.y += 0.6f + (Mth.sin(t * 0.1f) * 0.5f + 0.5f) * 0.8f * drift;
        for (int i = 0; i < 3; i++) {
            this.hammerPx[i].y += Mth.sin(t * 0.13f + i * 2.0f) * 0.8f;
            this.hammerPx[i].x += Mth.cos(t * 0.1f + i) * 0.4f;
        }

        // ---------------------------------------------------------- Bildfehler: ein Glied springt für einen Tick in eine falsche Haltung
        int tick = (int) t;
        float chance = frozen ? 0.5f : state.version2 ? 0.16f : glitch ? 0.12f : 0.05f;
        if (state.deathTime > 0.0f) {
            chance = 0.4f;
        }
        if (hash(state.seed, tick, 1) < chance) {
            ModelPart part = this.all[(int) (hash(state.seed, tick, 2) * this.all.length) % this.all.length];
            part.xRot += (hash(state.seed, tick, 3) - 0.5f) * 1.6f;
            part.yRot += (hash(state.seed, tick, 4) - 0.5f) * 1.6f;
            part.zRot += (hash(state.seed, tick, 5) - 0.5f) * 1.2f;
        }
        if (hash(state.seed, tick, 6) < chance * 0.6f) {
            // ein Brocken versetzt: der Kopf oder der Hammer rutscht ein Stück zur Seite
            ModelPart part = hash(state.seed, tick, 9) < 0.5f ? this.head : this.hammer;
            part.x += (hash(state.seed, tick, 10) - 0.5f) * 4.0f;
            part.y += (hash(state.seed, tick, 11) - 0.5f) * 2.0f;
        }
    }
}
