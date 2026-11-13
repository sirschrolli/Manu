package io.github.sirschrolli.kingdomsatwar.client.model.sea;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.sea.KrakenRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Der Kraken: Seine acht Arme wogen unablässig in langsamen Wellen, die Flossen am Mantel schlagen, der Mantel hebt und
 * senkt sich mit dem Atem. Seine Angriffe:
 * <ul>
 *     <li><b>Tentakelschlag:</b> ein Fangarm steigt hoch über ihn, rollt sich zurück, verharrt – und peitscht herab;</li>
 *     <li><b>Armfeger:</b> die vier Arme einer Seite heben sich und fegen quer über das Wasser;</li>
 *     <li><b>Würgegriff:</b> ein Fangarm schnellt gerade vor, rollt sich um die Beute und zieht sie zum Schnabel;</li>
 *     <li><b>Tintenwolke:</b> er bäumt sich zurück, der Mantel zieht sich zusammen, der Schnabel öffnet sich;</li>
 *     <li><b>Strudel:</b> alle Arme strecken sich zur Seite und kreisen (das Drehen übernimmt der Renderer);</li>
 *     <li><b>Flutwelle:</b> alle Arme steigen empor und schlagen zugleich aufs Wasser;</li>
 *     <li><b>Brüllen</b> (beim Erwachen und in der Raserei): Arme weit gespreizt und erhoben, der Schnabel weit offen.</li>
 * </ul>
 * Seit 1.00 schwimmt er: der Kopf neigt sich in die Fahrt, der Mantel weht zurück, die Arme strömen nach hinten und rudern im
 * Takt (vorziehen über dem Wasser, kraftvoll zurück darunter), die Flossen schlagen. Dazu:
 * <ul>
 *     <li><b>Rammstoß:</b> er bäumt sich auf, bläht den Mantel, spreizt die Arme – und schießt vor, den Kopf voran, der Mantel
 *     presst sich zusammen, die Arme liegen flach nach hinten, die Fangarme stoßen nach vorn;</li>
 *     <li><b>Abtauchen:</b> die Arme heben sich, während er versinkt (das Versinken übernimmt der Renderer);</li>
 *     <li><b>Ausbruch:</b> er schnellt empor, Arme und Fangarme fliegen auseinander, der Schnabel reißt auf – dann krachen die
 *     Arme aufs Wasser;</li>
 *     <li><b>Geysirfeld:</b> die vorderen Arme heben sich und klatschen aufs Wasser, danach bebt er mit erhobenen Armen;</li>
 *     <li><b>Tentakelkäfig:</b> die Fangarme stoßen tief ins Wasser, die Arme spannen sich – und reißen zurück, wenn der Käfig
 *     zuschlägt.</li>
 * </ul>
 * Im Tod erschlaffen die Arme, er sinkt zurück in den Schlund (das Versinken übernimmt der Renderer).
 */
public class KrakenModel extends EntityModel<KrakenRenderState> {
    private static final int ARMS = 8;
    private static final int ARM_PARTS = 6;
    private static final int TENTACLE_PARTS = 8;

    private final ModelPart body;
    private final ModelPart mantle;
    private final ModelPart finL;
    private final ModelPart finR;
    private final ModelPart beak;
    private final ModelPart[][] arms = new ModelPart[ARMS][ARM_PARTS];
    /** 0: linker, 1: rechter Fangarm; das letzte Glied ist die Keule. */
    private final ModelPart[][] tentacles = new ModelPart[2][TENTACLE_PARTS + 1];

    public KrakenModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.mantle = this.body.getChild("mantle");
        this.finL = this.mantle.getChild("fin_left");
        this.finR = this.mantle.getChild("fin_right");
        this.beak = this.body.getChild("beak");
        for (int i = 0; i < ARMS; i++) {
            ModelPart part = this.body.getChild("arm" + i);
            this.arms[i][0] = part;
            for (int k = 1; k < ARM_PARTS; k++) {
                part = part.getChild("arm" + i + "_" + k);
                this.arms[i][k] = part;
            }
        }
        String[] names = {"tentacle_left", "tentacle_right"};
        for (int s = 0; s < 2; s++) {
            ModelPart part = this.body.getChild(names[s]);
            this.tentacles[s][0] = part;
            for (int k = 1; k < TENTACLE_PARTS; k++) {
                part = part.getChild(names[s] + "_" + k);
                this.tentacles[s][k] = part;
            }
            this.tentacles[s][TENTACLE_PARTS] = part.getChild(names[s] + "_club");
        }
    }

    @Override
    public void setupAnim(KrakenRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float rage = state.enraged ? 1.6f : 1.0f;

        // Atem: der Mantel hebt und weitet sich, neigt sich ein wenig
        float breath = Mth.sin(t * 0.06f * rage);
        this.mantle.xScale = 1.0f + breath * 0.04f;
        this.mantle.zScale = 1.0f + breath * 0.04f;
        this.mantle.yScale = 1.0f - breath * 0.025f;
        this.mantle.xRot += Mth.sin(t * 0.031f) * 0.05f;
        this.mantle.zRot += Mth.sin(t * 0.027f + 1.0f) * 0.04f;
        this.finL.zRot += Mth.sin(t * 0.13f * rage) * 0.28f;
        this.finR.zRot -= Mth.sin(t * 0.13f * rage) * 0.28f;
        this.body.y += Mth.sin(t * 0.045f) * 0.6f;
        this.body.xRot += Mth.sin(t * 0.04f + 0.5f) * 0.03f;

        // die Arme wogen: Wellen laufen von der Wurzel zur Spitze, jeder Arm in eigenem Takt
        for (int i = 0; i < ARMS; i++) {
            for (int k = 0; k < ARM_PARTS; k++) {
                ModelPart p = this.arms[i][k];
                float w = t * 0.075f * rage + i * 0.9f - k * 0.55f;
                p.xRot += Mth.sin(w) * (0.05f + 0.035f * k);
                p.yRot += Mth.sin(t * 0.05f + i * 1.7f - k * 0.4f) * (0.03f + 0.02f * k);
            }
        }
        for (int s = 0; s < 2; s++) {
            for (int k = 0; k < TENTACLE_PARTS; k++) {
                ModelPart p = this.tentacles[s][k];
                float w = t * 0.06f * rage + s * 2.1f - k * 0.45f;
                p.xRot += Mth.sin(w) * (0.03f + 0.02f * k);
                p.yRot += Mth.sin(t * 0.045f + s * 1.3f - k * 0.35f) * 0.03f;
            }
        }

        this.swim(state.swim, t, rage);
        this.slam(state.slamLeft, t, 0);
        this.slam(state.slamRight, t, 1);
        this.sweep(state.sweepLeft, t, true);
        this.sweep(state.sweepRight, t, false);
        this.grab(state.grab, t, state.grabSide);
        this.ink(state.ink, t);
        this.whirl(state.whirl, t);
        this.wave(state.wave, t);
        this.roar(state.roar, t);
        this.charge(state.charge, t);
        this.dive(state.dive, t);
        this.breach(state.breach, t);
        this.geyser(state.geyser, t);
        this.cage(state.cage, t);

        if (state.deathTime > 0.0f) {
            // Tod: die Arme erschlaffen und sinken, der Mantel kippt nach hinten
            float d = Mth.clamp(state.deathTime / 40.0f, 0.0f, 1.0f);
            for (ModelPart[] arm : this.arms) {
                arm[0].xRot += d * 0.7f;
                for (int k = 1; k < ARM_PARTS; k++) {
                    arm[k].xRot += d * 0.2f;
                }
            }
            for (ModelPart[] tentacle : this.tentacles) {
                tentacle[0].xRot += d * 0.9f;
            }
            this.mantle.xRot -= d * 0.5f;
            this.beak.y += d * 1.5f;
        }
    }

    /**
     * Tentakelschlag: der Fangarm steigt schräg nach vorn empor (gestreckt, nie über die Senkrechte – sonst kippte er auf
     * die andere Seite), holt auf seiner Seite aus, verharrt – und schlägt nach vorn herab.
     */
    private void slam(net.minecraft.world.entity.AnimationState anim, float t, int side) {
        float s = Anim.seconds(anim, t);
        if (s < 0.0f || s > 2.6f) {
            return;
        }
        ModelPart[] arm = this.tentacles[side];
        float up = Anim.bump(s, 0.8f, 1.15f, 1.35f);
        float down = Anim.bump(s - 1.15f, 0.2f, 0.55f, 1.4f);
        arm[0].xRot += -1.45f * up + 0.7f * down;
        // (größeres yRot dreht nach rechts, -X im Modell): der linke Fangarm holt nach links aus, der rechte nach rechts
        arm[0].yRot += (side == 0 ? -0.3f : 0.3f) * up;
        for (int k = 1; k <= TENTACLE_PARTS; k++) {
            // gestreckt (die Krümmung der Ruhelage aufheben), beim Schlag peitscht die Spitze nach
            arm[k].xRot += 0.1f * up + 0.08f * down;
        }
    }

    /** Armfeger: die vier Arme einer Seite heben sich und fegen herum. */
    private void sweep(net.minecraft.world.entity.AnimationState anim, float t, boolean left) {
        float s = Anim.seconds(anim, t);
        if (s < 0.0f || s > 2.2f) {
            return;
        }
        float lift = Anim.bump(s, 0.5f, 1.3f, 2.0f);
        // von hinten nach vorn: erst weit zurück, dann quer herum
        float swing = s < 0.6f ? -Mth.sin(s / 0.6f * Mth.HALF_PI) : -1.0f + 2.0f * Mth.clamp((s - 0.6f) / 0.5f, 0.0f, 1.0f);
        for (int i = 0; i < ARMS; i++) {
            // die Arme 4 bis 7 sitzen links (im Modell bei +X), 0 bis 3 rechts
            boolean onSide = left ? i >= 4 : i < 4;
            if (!onSide) {
                continue;
            }
            ModelPart[] arm = this.arms[i];
            arm[0].xRot -= 0.75f * lift;
            arm[0].yRot += (left ? 1.0f : -1.0f) * swing * 0.9f * lift;
            for (int k = 1; k < ARM_PARTS; k++) {
                arm[k].xRot += 0.12f * lift;
            }
        }
    }

    /** Würgegriff: der Fangarm schnellt gerade vor, rollt sich ein und zieht zurück. */
    private void grab(net.minecraft.world.entity.AnimationState anim, float t, int side) {
        float s = Anim.seconds(anim, t);
        if (s < 0.0f || s > 3.6f) {
            return;
        }
        ModelPart[] arm = this.tentacles[side];
        float reach = Anim.bump(s, 0.35f, 0.7f, 1.2f);
        float coil = Anim.bump(s - 0.55f, 0.3f, 2.6f, 3.0f);
        arm[0].xRot += -0.35f * reach - 0.6f * coil;
        for (int k = 1; k <= TENTACLE_PARTS; k++) {
            // vorgestreckt: die Biegung der Ruhelage aufheben; im Griff: stark eingerollt
            arm[k].xRot += 0.08f * reach - (k >= TENTACLE_PARTS - 3 ? 0.55f : 0.12f) * coil;
        }
    }

    /** Tintenwolke: Zurückbäumen, Mantel zusammenziehen, Schnabel auf. */
    private void ink(net.minecraft.world.entity.AnimationState anim, float t) {
        float s = Anim.seconds(anim, t);
        if (s < 0.0f || s > 2.0f) {
            return;
        }
        float a = Anim.bump(s, 0.5f, 1.0f, 1.8f);
        float squeeze = Anim.bump(s - 0.8f, 0.15f, 0.35f, 0.8f);
        this.body.xRot -= 0.25f * a;
        this.mantle.xScale -= 0.12f * squeeze;
        this.mantle.zScale -= 0.12f * squeeze;
        this.mantle.yScale += 0.08f * squeeze;
        this.beak.y += 1.8f * a;
        for (ModelPart[] arm : this.arms) {
            arm[0].xRot -= 0.3f * a;
        }
    }

    /** Strudel: die Arme strecken sich flach nach außen (das Kreisen übernimmt der Renderer). */
    private void whirl(net.minecraft.world.entity.AnimationState anim, float t) {
        float s = Anim.seconds(anim, t);
        if (s < 0.0f || s > 6.5f) {
            return;
        }
        float a = Anim.bump(s, 0.8f, 5.0f, 6.2f);
        for (ModelPart[] arm : this.arms) {
            arm[0].xRot -= 0.35f * a;
            for (int k = 1; k < ARM_PARTS; k++) {
                arm[k].xRot += 0.18f * a;
                arm[k].yRot += 0.12f * a;
            }
        }
    }

    /** Flutwelle: alle Arme hoch empor – und zugleich herab aufs Wasser. */
    private void wave(net.minecraft.world.entity.AnimationState anim, float t) {
        float s = Anim.seconds(anim, t);
        if (s < 0.0f || s > 3.0f) {
            return;
        }
        float up = Anim.bump(s, 1.0f, 1.4f, 1.6f);
        float down = Anim.bump(s - 1.4f, 0.2f, 0.6f, 1.4f);
        for (ModelPart[] arm : this.arms) {
            arm[0].xRot += -1.5f * up + 0.5f * down;
            for (int k = 1; k < ARM_PARTS; k++) {
                arm[k].xRot += -0.18f * up + 0.1f * down;
            }
        }
        for (ModelPart[] tentacle : this.tentacles) {
            tentacle[0].xRot += -1.2f * up + 0.4f * down;
        }
        this.beak.y += 1.2f * up;
    }

    /** Brüllen: Arme weit gespreizt und erhoben, der Schnabel weit offen, der Mantel bebt. */
    private void roar(net.minecraft.world.entity.AnimationState anim, float t) {
        float s = Anim.seconds(anim, t);
        if (s < 0.0f || s > 3.2f) {
            return;
        }
        float a = Anim.bump(s, 0.6f, 2.4f, 3.0f);
        for (int i = 0; i < ARMS; i++) {
            ModelPart[] arm = this.arms[i];
            arm[0].xRot -= 1.0f * a;
            for (int k = 1; k < ARM_PARTS; k++) {
                arm[k].xRot -= 0.1f * a;
            }
        }
        for (ModelPart[] tentacle : this.tentacles) {
            tentacle[0].xRot -= 1.1f * a;
        }
        this.beak.y += 2.2f * a;
        this.mantle.zRot += Mth.sin(t * 1.3f) * 0.05f * a;
        this.body.xRot -= 0.15f * a;
    }

    /** Gierwinkel des Arms {@code i} in der Ruhelage (0 vorn; die Arme 0 bis 3 sitzen rechts, 4 bis 7 links). */
    private static float armYaw(int i) {
        return 0.3927f + i * 0.7854f;
    }

    /** In diese Richtung (Vorzeichen des Gierwinkels) streicht Arm {@code i} nach hinten. */
    private static float backward(int i) {
        return i < 4 ? 1.0f : -1.0f;
    }

    /**
     * Schwimmen ({@code s}: 0 ruhig, 1 volle Fahrt): der Kopf neigt sich in die Fahrt, der Mantel weht zurück und pumpt im Takt,
     * die Arme strömen nach hinten und rudern (vorziehen hoch, zurück tief), die Flossen schlagen kräftiger.
     */
    private void swim(float s, float t, float rage) {
        if (s < 0.01f) {
            return;
        }
        s = Math.min(s, 1.3f);
        float phase = t * 0.185f * rage;
        float stroke = Mth.sin(phase);
        float lift = Mth.cos(phase);
        this.body.xRot += 0.16f * s;
        this.body.y += stroke * 0.7f * s;
        this.mantle.xRot -= 0.22f * s;
        float pump = Math.max(0.0f, stroke) * 0.06f * s;
        this.mantle.xScale -= pump;
        this.mantle.zScale -= pump;
        this.mantle.yScale += pump * 0.6f;
        this.finL.zRot += Mth.sin(t * 0.32f) * 0.3f * s;
        this.finR.zRot -= Mth.sin(t * 0.32f) * 0.3f * s;
        for (int i = 0; i < ARMS; i++) {
            ModelPart[] arm = this.arms[i];
            float front = Mth.cos(armYaw(i));
            float back = backward(i);
            // nach hinten strömen (die vorderen Arme weiter), dazu der Ruderschlag
            arm[0].yRot += back * s * (0.2f + 0.24f * (1.0f + front) * 0.5f + 0.22f * stroke);
            arm[0].xRot += s * (-0.16f + 0.12f * lift);
            for (int k = 1; k < ARM_PARTS; k++) {
                arm[k].xRot += s * (0.05f + 0.03f * lift);
                arm[k].yRot += back * s * 0.03f * Mth.sin(phase - k * 0.6f);
            }
        }
        for (ModelPart[] tentacle : this.tentacles) {
            tentacle[0].xRot += 0.25f * s;
            for (int k = 1; k <= TENTACLE_PARTS; k++) {
                tentacle[k].xRot += 0.03f * s * Mth.sin(phase - k * 0.5f);
            }
        }
    }

    /** Rammstoß: aufbäumen, Mantel blähen, Arme spreizen – dann vorschießen, Mantel gepresst, Arme flach nach hinten. */
    private void charge(net.minecraft.world.entity.AnimationState anim, float t) {
        float s = Anim.seconds(anim, t);
        if (s < 0.0f || s > 3.3f) {
            return;
        }
        float wind = Anim.bump(s, 0.9f, 1.15f, 1.35f);
        float dash = Anim.bump(s - 1.15f, 0.15f, 0.8f, 1.9f);
        this.body.xRot += -0.18f * wind + 0.42f * dash;
        this.mantle.xRot -= 0.3f * dash;
        float puff = 0.1f * wind - 0.16f * dash;
        this.mantle.xScale += puff;
        this.mantle.zScale += puff;
        this.mantle.yScale -= puff * 0.6f;
        this.beak.y += 1.4f * wind;
        for (int i = 0; i < ARMS; i++) {
            ModelPart[] arm = this.arms[i];
            float back = backward(i);
            arm[0].xRot += -0.42f * wind - 0.32f * dash;
            arm[0].yRot += back * (-0.25f * wind + 0.85f * dash);
            for (int k = 1; k < ARM_PARTS; k++) {
                arm[k].xRot += -0.06f * wind + 0.1f * dash;
            }
        }
        for (int side = 0; side < 2; side++) {
            ModelPart[] tentacle = this.tentacles[side];
            // die Fangarme stoßen gebündelt nach vorn
            tentacle[0].xRot += -0.3f * wind - 0.45f * dash;
            tentacle[0].yRot += (side == 0 ? 0.35f : -0.35f) * dash;
            for (int k = 1; k <= TENTACLE_PARTS; k++) {
                tentacle[k].xRot += 0.06f * dash;
            }
        }
    }

    /** Abtauchen: die Arme heben sich, während er versinkt (bis er wieder ausbricht). */
    private void dive(net.minecraft.world.entity.AnimationState anim, float t) {
        float s = Anim.seconds(anim, t);
        if (s < 0.0f || s > 4.6f) {
            return;
        }
        float a = Anim.bump(s, 0.5f, 3.6f, 4.6f);
        this.body.xRot -= 0.12f * a;
        this.beak.y += 0.8f * a;
        for (ModelPart[] arm : this.arms) {
            arm[0].xRot -= 0.85f * a;
            for (int k = 1; k < ARM_PARTS; k++) {
                arm[k].xRot -= 0.07f * a;
            }
        }
        for (ModelPart[] tentacle : this.tentacles) {
            tentacle[0].xRot -= 0.9f * a;
        }
    }

    /** Ausbruch: er schnellt empor, Arme und Fangarme fliegen auseinander, der Schnabel reißt auf – dann krachen die Arme herab. */
    private void breach(net.minecraft.world.entity.AnimationState anim, float t) {
        float s = Anim.seconds(anim, t);
        if (s < 0.0f || s > 2.2f) {
            return;
        }
        float burst = Anim.bump(s, 0.15f, 0.8f, 1.5f);
        float crash = Anim.bump(s - 0.85f, 0.2f, 0.45f, 1.3f);
        this.body.xRot -= 0.22f * burst;
        this.mantle.yScale += 0.1f * burst;
        this.mantle.xRot += 0.15f * burst;
        this.beak.y += 2.6f * burst;
        for (int i = 0; i < ARMS; i++) {
            ModelPart[] arm = this.arms[i];
            arm[0].xRot += -1.25f * burst + 0.45f * crash;
            arm[0].yRot += backward(i) * -0.15f * burst;
            for (int k = 1; k < ARM_PARTS; k++) {
                arm[k].xRot += -0.12f * burst + 0.08f * crash;
            }
        }
        for (ModelPart[] tentacle : this.tentacles) {
            tentacle[0].xRot += -1.35f * burst + 0.5f * crash;
            for (int k = 1; k <= TENTACLE_PARTS; k++) {
                tentacle[k].xRot -= 0.05f * burst;
            }
        }
    }

    /** Geysirfeld: die vorderen Arme heben sich und klatschen aufs Wasser; danach bebt er mit erhobenen Armen. */
    private void geyser(net.minecraft.world.entity.AnimationState anim, float t) {
        float s = Anim.seconds(anim, t);
        if (s < 0.0f || s > 4.4f) {
            return;
        }
        float up = Anim.bump(s, 0.35f, 0.42f, 0.52f);
        float down = Anim.bump(s - 0.45f, 0.08f, 0.35f, 1.0f);
        float hold = Anim.bump(s - 0.7f, 0.5f, 3.0f, 3.7f);
        float tremble = Mth.sin(t * 1.1f) * 0.05f * hold;
        this.body.xRot += 0.14f * down - 0.06f * hold;
        this.beak.y += 1.0f * hold;
        for (int i = 0; i < ARMS; i++) {
            ModelPart[] arm = this.arms[i];
            boolean front = i == 0 || i == 1 || i == 6 || i == 7;
            if (front) {
                arm[0].xRot += -1.25f * up + 0.55f * down;
                for (int k = 1; k < ARM_PARTS; k++) {
                    arm[k].xRot += -0.1f * up + 0.1f * down;
                }
            }
            arm[0].xRot += -0.3f * hold + tremble;
        }
        for (ModelPart[] tentacle : this.tentacles) {
            tentacle[0].xRot += -0.5f * hold + tremble;
        }
    }

    /** Tentakelkäfig: die Fangarme stoßen tief ins Wasser, die Arme spannen sich – und reißen zurück, wenn der Käfig zuschlägt. */
    private void cage(net.minecraft.world.entity.AnimationState anim, float t) {
        float s = Anim.seconds(anim, t);
        if (s < 0.0f || s > 5.0f) {
            return;
        }
        float plunge = Anim.bump(s, 0.5f, 4.0f, 4.6f);
        float yank = Anim.bump(s - 3.45f, 0.2f, 0.35f, 0.8f);
        this.body.xRot += 0.12f * plunge + 0.2f * yank;
        this.mantle.zRot += Mth.sin(t * 0.9f) * 0.03f * plunge;
        for (ModelPart[] tentacle : this.tentacles) {
            tentacle[0].xRot += 0.95f * plunge - 0.5f * yank;
            for (int k = 1; k <= TENTACLE_PARTS; k++) {
                tentacle[k].xRot += 0.1f * plunge;
            }
        }
        for (int i = 0; i < ARMS; i++) {
            ModelPart[] arm = this.arms[i];
            arm[0].xRot += -0.28f * plunge + 0.35f * yank;
            arm[0].yRot += backward(i) * 0.12f * plunge * Mth.sin(t * 0.25f + i);
            for (int k = 1; k < ARM_PARTS; k++) {
                arm[k].xRot += 0.04f * plunge;
            }
        }
    }
}
