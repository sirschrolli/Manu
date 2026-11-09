package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MothQueenEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Mottenkönigin: weite, ruhige Flügelschläge (die Hinterflügel folgen versetzt), schwebendes Auf und Ab, der Hinterleib
 * pendelt, die gefiederten Fühler tasten. Je Angriff eine eigene Haltung: wuchtige Schläge beim Flügelsturm, ein
 * Flirren beim Schuppenregen, hoch erhobene Flügel beim Mondstrahl, angelegte Flügel im Sturzflug, weit ausgebreitete,
 * langsam wogende Flügel beim Schlaflied; beim Erwachen entfalten sie sich aus dem Kokon.
 */
public class MothQueenModel extends EntityModel<FaeRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart abdomen;
    private final ModelPart tail;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftHind;
    private final ModelPart rightHind;
    private final ModelPart leftAntenna;
    private final ModelPart rightAntenna;
    private final ModelPart[] legs = new ModelPart[6];

    public MothQueenModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.abdomen = this.body.getChild("abdomen");
        this.tail = this.abdomen.getChild("tail");
        this.leftWing = this.body.getChild("left_wing");
        this.rightWing = this.body.getChild("right_wing");
        this.leftHind = this.body.getChild("left_hind_wing");
        this.rightHind = this.body.getChild("right_hind_wing");
        this.leftAntenna = this.head.getChild("left_antenna");
        this.rightAntenna = this.head.getChild("right_antenna");
        for (int i = 0; i < 3; i++) {
            this.legs[i] = this.body.getChild("left_leg" + i);
            this.legs[i + 3] = this.body.getChild("right_leg" + i);
        }
    }

    @Override
    public void setupAnim(FaeRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        MothQueenEntity.Attack attack = MothQueenEntity.Attack.values()[Mth.clamp(state.attack, 0, MothQueenEntity.Attack.values().length - 1)];
        float a = state.attackTime;
        float speed = state.enraged ? 0.32f : 0.24f;
        float amp = 0.55f;
        float lift = 0.0f;
        float sweep = 0.0f;
        switch (attack) {
            case GUST -> {
                // drei wuchtige Schläge (Ticks 12, 24, 36)
                float phase = (a % 12) / 12.0f;
                float beat = Mth.sin(phase * Mth.TWO_PI);
                this.setWings(-0.15f + beat * 1.05f, -0.1f + Mth.sin(phase * Mth.TWO_PI - 0.6f) * 0.8f);
                this.body.xRot += -0.25f;
                this.finish(state, t);
                return;
            }
            case SCALES -> {
                speed = 0.75f;
                amp = 0.3f;
                lift = 0.2f;
            }
            case SWARM -> {
                speed = 0.6f;
                amp = 0.45f;
                this.head.xRot -= 0.5f * Anim.bump(a / 20.0f, 0.2f, 0.6f, 1.2f);
            }
            case BEAM -> {
                lift = -0.9f;
                amp = 0.18f;
                speed = 0.12f;
            }
            case DIVE -> {
                float fold = a < 12 ? a / 12.0f : 1.0f;
                sweep = 0.9f * fold;
                amp = 0.12f * (1.0f - fold) + 0.04f;
                speed = 0.5f;
                this.body.xRot += 0.6f * fold;
            }
            case LULLABY -> {
                speed = 0.08f;
                amp = 0.35f;
                lift = 0.25f;
                this.head.zRot += Mth.sin(t * 0.08f) * 0.15f;
            }
            case ECLIPSE -> {
                // die Flügel weit und fast reglos ausgebreitet, der Kopf erhoben – sie verdunkelt den Mond
                lift = -0.75f;
                amp = 0.12f;
                speed = 0.09f;
                this.head.xRot -= 0.35f * Math.min(1.0f, a / 10.0f);
            }
            case COCOON -> {
                // schnelles Schwirren, der Kopf senkt sich: Sie spinnt den Faden
                speed = 0.85f;
                amp = 0.28f;
                lift = 0.1f;
                this.head.xRot += 0.45f * Anim.bump(a / 24.0f, 0.2f, 0.7f, 1.0f);
                this.body.xRot += 0.2f * Anim.bump(a / 24.0f, 0.2f, 0.7f, 1.0f);
            }
            case AWAKEN -> {
                float open = Mth.clamp((a - 10.0f) / 50.0f, 0.0f, 1.0f);
                float shut = 1.0f - open;
                // anfangs zusammengefaltet über dem Rücken, dann entfalten
                this.setWings(-1.4f * shut + (-0.2f + Mth.sin(t * 0.3f) * 0.4f) * open,
                        -1.3f * shut + (-0.1f + Mth.sin(t * 0.3f - 0.5f) * 0.3f) * open);
                this.leftWing.yRot -= 0.4f * shut;
                this.rightWing.yRot += 0.4f * shut;
                this.finish(state, t);
                return;
            }
            case STUNNED -> {
                // am Boden: Flügel flach ausgebreitet und zitternd, der Kopf hängt
                float twitch = Mth.sin(t * 1.3f) * 0.04f;
                this.setWings(0.55f + twitch, 0.5f - twitch);
                this.head.xRot += 0.45f;
                this.leftAntenna.xRot += 0.5f;
                this.rightAntenna.xRot += 0.5f;
                this.finish(state, t);
                this.body.y += 1.5f;
                return;
            }
            default -> { }
        }
        float flap = Mth.sin(t * speed);
        float hind = Mth.sin(t * speed - 0.6f);
        this.setWings(-0.15f - lift + flap * amp, -0.1f - lift * 0.8f + hind * amp * 0.8f);
        this.leftWing.yRot -= sweep;
        this.rightWing.yRot += sweep;
        this.leftHind.yRot -= sweep * 0.6f;
        this.rightHind.yRot += sweep * 0.6f;
        this.finish(state, t);
    }

    /** Flügel links positiv (nach oben), rechts gespiegelt. */
    private void setWings(float fore, float hind) {
        this.leftWing.zRot = -fore;
        this.rightWing.zRot = fore;
        this.leftHind.zRot = -hind;
        this.rightHind.zRot = hind;
    }

    private void finish(FaeRenderState state, float t) {
        this.body.y += Mth.sin(t * 0.12f) * 1.2f;
        this.body.xRot += Mth.sin(t * 0.12f + 1.0f) * 0.05f;
        this.abdomen.xRot += 0.12f + Mth.sin(t * 0.1f) * 0.08f;
        this.tail.xRot += 0.1f + Mth.sin(t * 0.1f - 0.6f) * 0.1f;
        this.leftAntenna.xRot += Mth.sin(t * 0.17f) * 0.12f;
        this.rightAntenna.xRot += Mth.sin(t * 0.17f + 0.8f) * 0.12f;
        this.head.yRot += Mth.clamp(state.yRot, -40.0f, 40.0f) * Anim.DEG * 0.5f;
        for (int i = 0; i < 6; i++) {
            this.legs[i].xRot += Mth.sin(t * 0.15f + i) * 0.12f;
        }
    }
}
