package io.github.sirschrolli.kingdomsatwar.client.model.spell;

import io.github.sirschrolli.kingdomsatwar.client.renderer.spell.SpellFxRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

/**
 * Bewegte Modelle der Zauberwirkungen (Geometrie aus tools/entitygen): Felsspitze, Steinfaust, Dornenranke, Giftblüte
 * und Eiskerker. Alle stehen mit dem Fuß auf dem Boden (Modell-y = 24) und brechen aus ihm hervor.
 */
public final class SpellFxModels {
    private SpellFxModels() {}

    /** Weiches Herauswachsen: 0 → 1 mit kurzem Überschwingen. */
    static float easeOutBack(float t) {
        t = Mth.clamp(t, 0.0f, 1.0f) - 1.0f;
        float c = 1.9f;
        return 1.0f + t * t * ((c + 1.0f) * t + c);
    }

    static float easeOut(float t) {
        t = Mth.clamp(t, 0.0f, 1.0f);
        return 1.0f - (1.0f - t) * (1.0f - t);
    }

    /** Wie weit die Wirkung zu sehen ist: wächst in {@code in} Ticks heraus, versinkt in den letzten {@code out} Ticks. */
    static float presence(SpellFxRenderState s, float in, float out, boolean bounce) {
        float rise = bounce ? easeOutBack(s.age / in) : easeOut(s.age / in);
        float sink = easeOut((s.life - s.age) / out);
        return Math.min(rise, sink);
    }

    /** Fester Zufall 0–1 aus der Kennung der Wirkung. */
    static float rand(SpellFxRenderState s, int salt) {
        int h = s.seed * 0x9E3779B1 + salt * 0x85EBCA6B;
        h ^= h >>> 15;
        h *= 0x2C1B3C6D;
        h ^= h >>> 12;
        return (h & 0xFFFF) / 65535.0f;
    }

    /** Felsspitze: bricht schräg aus dem Boden, bleibt stehen und versinkt wieder; Geröll bleibt am Fuß. */
    public static final class Spike extends Model<SpellFxRenderState> {
        private final ModelPart spike;
        private final ModelPart rubble;

        public Spike(ModelPart root) {
            super(root, RenderTypes::entityCutout);
            this.spike = root.getChild("spike");
            this.rubble = root.getChild("rubble");
        }

        @Override
        public void setupAnim(SpellFxRenderState s) {
            super.setupAnim(s);
            float p = presence(s, 3.0f, 7.0f, true);
            this.spike.y += (1.0f - p) * 46.0f;
            this.spike.xRot = (rand(s, 1) - 0.5f) * 0.45f;
            this.spike.zRot = (rand(s, 2) - 0.5f) * 0.45f;
            this.spike.yRot = rand(s, 3) * Mth.TWO_PI;
            this.rubble.yRot = rand(s, 4) * Mth.TWO_PI;
            this.rubble.visible = s.age > 0.5f;
        }
    }

    /** Steinfaust: schlägt aus dem Boden nach oben, die Faust ballt sich im Aufstieg, dann sinkt der Arm zurück. */
    public static final class Fist extends Model<SpellFxRenderState> {
        private final ModelPart arm;
        private final ModelPart fist;

        public Fist(ModelPart root) {
            super(root, RenderTypes::entityCutout);
            this.arm = root.getChild("arm");
            this.fist = this.arm.getChild("fist");
        }

        @Override
        public void setupAnim(SpellFxRenderState s) {
            super.setupAnim(s);
            float p = presence(s, 4.0f, 9.0f, true);
            this.arm.y += (1.0f - p) * 40.0f;
            float punch = easeOut(s.age / 5.0f);
            this.fist.xRot = -0.55f * (1.0f - punch) + Mth.sin(s.age * 0.6f) * 0.02f;
            this.arm.zRot = (rand(s, 5) - 0.5f) * 0.2f;
            this.arm.xRot = (rand(s, 6) - 0.5f) * 0.16f;
        }
    }

    /** Dornenranke: windet sich aus dem Boden, wiegt sich und peitscht einmal hart nach vorn. */
    public static final class Vine extends Model<SpellFxRenderState> {
        private final ModelPart[] segments = new ModelPart[6];

        public Vine(ModelPart root) {
            super(root, RenderTypes::entityCutout);
            ModelPart part = root.getChild("seg0");
            this.segments[0] = part;
            for (int i = 1; i < this.segments.length; i++) {
                part = part.getChild("seg" + i);
                this.segments[i] = part;
            }
        }

        @Override
        public void setupAnim(SpellFxRenderState s) {
            super.setupAnim(s);
            float p = presence(s, 5.0f, 9.0f, false);
            this.segments[0].y += (1.0f - p) * 54.0f;
            float whipStart = s.param * s.life;
            float w = (s.age - whipStart) / 7.0f;
            float whip = w <= 0.0f || w >= 1.0f ? 0.0f : Mth.sin(w * Mth.PI);
            float phase = rand(s, 7) * Mth.TWO_PI;
            for (int i = 0; i < this.segments.length; i++) {
                ModelPart seg = this.segments[i];
                float k = (i + 1) / (float) this.segments.length;
                seg.xRot += Mth.sin(s.age * 0.22f + i * 0.8f + phase) * 0.14f * k - whip * 0.32f * k;
                seg.zRot += Mth.cos(s.age * 0.17f + i * 0.9f + phase) * 0.12f * k;
            }
            // die Ranke steht zur Stelle hin gedreht (die Wirkung trägt die Richtung), nur wenig zufällig
            this.segments[0].yRot = (rand(s, 8) - 0.5f) * 0.6f;
        }
    }

    /** Giftblüte: wächst, öffnet die Blätter, atmet Sporen aus und welkt zuletzt. */
    public static final class Flower extends Model<SpellFxRenderState> {
        private final ModelPart stem;
        private final ModelPart bulb;
        private final ModelPart[] petals = new ModelPart[6];
        private final ModelPart[] leaves = new ModelPart[4];

        public Flower(ModelPart root) {
            super(root, RenderTypes::entityCutout);
            this.stem = root.getChild("stem");
            this.bulb = this.stem.getChild("bulb");
            for (int i = 0; i < this.petals.length; i++) {
                this.petals[i] = this.bulb.getChild("petal" + i);
            }
            for (int i = 0; i < this.leaves.length; i++) {
                this.leaves[i] = this.stem.getChild("leaf" + i);
            }
        }

        @Override
        public void setupAnim(SpellFxRenderState s) {
            super.setupAnim(s);
            float grow = easeOut(s.age / 16.0f);
            float wilt = Mth.clamp((s.age - (s.life - 16)) / 16.0f, 0.0f, 1.0f);
            float scale = Math.max(0.01f, grow * (1.0f - wilt * 0.35f));
            this.stem.xScale = this.stem.yScale = this.stem.zScale = scale;
            this.stem.xRot += Mth.sin(s.age * 0.07f) * 0.05f + wilt * 0.25f;
            float open = easeOut((s.age - 14.0f) / 10.0f);
            float breathe = s.age > 24 && wilt <= 0.0f ? Mth.sin(s.age * 0.31f) * 0.08f : 0.0f;
            for (int i = 0; i < this.petals.length; i++) {
                // geschlossen: Knospe; offen: ein weiter Kelch; welk: die Blätter hängen herab
                this.petals[i].xRot += -1.36f + 0.9f * open + breathe + wilt * 1.2f;
            }
            float pulse = 1.0f + (s.age > 24 ? Mth.sin(s.age * 0.31f) * 0.06f : 0.0f);
            this.bulb.xScale = this.bulb.yScale = this.bulb.zScale = pulse;
            for (int i = 0; i < this.leaves.length; i++) {
                this.leaves[i].xRot += Mth.sin(s.age * 0.1f + i) * 0.06f + wilt * 0.4f;
            }
        }
    }

    /** Eiskerker: Kristall um Kristall schießt empor, zuletzt schließt sich das Dach; beim Zerspringen weiten sie sich. */
    public static final class Prison extends Model<SpellFxRenderState> {
        private final ModelPart[] shards = new ModelPart[8];
        private final ModelPart cap;

        public Prison(ModelPart root) {
            super(root, RenderTypes::entityTranslucent);
            for (int i = 0; i < this.shards.length; i++) {
                this.shards[i] = root.getChild("shard" + i);
            }
            this.cap = root.getChild("cap");
        }

        @Override
        public void setupAnim(SpellFxRenderState s) {
            super.setupAnim(s);
            float burst = Mth.clamp((s.age - (s.life - 3)) / 3.0f, 0.0f, 1.0f);
            for (int i = 0; i < this.shards.length; i++) {
                float g = easeOutBack((s.age - i * 0.6f) / 3.0f);
                float scale = Math.max(0.01f, g) * (1.0f + burst * 0.25f);
                ModelPart shard = this.shards[i];
                shard.xScale = shard.zScale = scale;
                shard.yScale = Math.max(0.01f, g);
            }
            float c = easeOutBack((s.age - 6.0f) / 3.0f);
            this.cap.xScale = this.cap.yScale = this.cap.zScale = Math.max(0.01f, c) * (1.0f + burst * 0.3f);
            this.cap.y -= burst * 4.0f;
        }
    }
}
