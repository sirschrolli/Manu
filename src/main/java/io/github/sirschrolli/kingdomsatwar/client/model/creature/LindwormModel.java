package io.github.sirschrolli.kingdomsatwar.client.model.creature;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.CreatureRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Lindwurm: der Leib schlängelt sich in einer Welle, die von vorne nach hinten läuft (schneller beim Kriechen);
 * Biss mit vorschnellendem Kopf, Schwanzhieb als Peitschenschlag.
 */
public class LindwormModel extends EntityModel<CreatureRenderState> {
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart[] segments = new ModelPart[8];

    public LindwormModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.leftLeg = this.body.getChild("left_leg");
        this.rightLeg = this.body.getChild("right_leg");
        ModelPart parent = this.body;
        for (int i = 0; i < this.segments.length; i++) {
            this.segments[i] = parent.getChild("segment" + (i + 1));
            parent = this.segments[i];
        }
    }

    @Override
    public void setupAnim(CreatureRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);

        // Schlängeln: Welle läuft nach hinten, Kopf bleibt ruhig nach vorne gerichtet
        float phase = t * (0.12f + 0.25f * speed) + pos * 0.35f;
        float amplitude = 0.1f + 0.22f * speed;
        this.body.yRot += Mth.sin(phase) * amplitude * 0.35f;
        this.neck.yRot -= Mth.sin(phase) * amplitude * 0.5f;
        for (int i = 0; i < this.segments.length; i++) {
            this.segments[i].yRot += Mth.sin(phase - (i + 1) * 0.75f) * amplitude * (0.8f + i * 0.06f);
        }
        this.leftLeg.xRot += Anim.leg(pos, speed, 0.0f, 1.0f);
        this.rightLeg.xRot += Anim.leg(pos, speed, Mth.PI, 1.0f);
        this.leftLeg.zRot -= 0.15f;
        this.rightLeg.zRot += 0.15f;

        float yaw = Mth.clamp(state.yRot, -60.0f, 60.0f) * DEG;
        this.neck.yRot += yaw * 0.45f;
        this.head.yRot += yaw * 0.45f;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * DEG * 0.5f;
        // Atmen, ab und zu zischen
        this.neck.xRot += Mth.sin(t * 0.07f) * 0.03f;
        this.jaw.xRot += Math.max(0.0f, Mth.sin(t * 0.045f) - 0.7f) * 0.6f;

        float bite = Anim.seconds(state.attack, t);
        if (bite >= 0.0f) {
            float lunge = Anim.bump(bite, 0.12f, 0.22f, 0.55f);
            float open = bite < 0.14f ? bite / 0.14f : Math.max(0.0f, 1.0f - (bite - 0.14f) / 0.1f);
            this.neck.xRot += 0.45f * lunge;
            this.head.xRot -= 0.25f * lunge;
            this.jaw.xRot += 0.8f * open;
        }
        float sweep = Anim.seconds(state.special, t);
        if (sweep >= 0.0f && sweep < 1.2f) {
            this.body.yRot -= 0.25f * Anim.bump(sweep, 0.3f, 0.45f, 0.9f);
            for (int i = 2; i < this.segments.length; i++) {
                this.segments[i].yRot += 0.55f * Anim.bump(sweep - (i - 2) * 0.035f, 0.2f, 0.32f, 0.75f);
            }
            this.jaw.xRot += 0.5f * Anim.bump(sweep, 0.1f, 0.4f, 0.6f);
        }
    }
}
