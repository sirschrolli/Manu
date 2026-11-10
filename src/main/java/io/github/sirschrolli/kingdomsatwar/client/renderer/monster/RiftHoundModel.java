package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Rissjäger: Er läuft geduckt mit langen Sprüngen, der Rücken federt, der Peitschenschwanz schlägt in Wellen; die drei
 * Kristallsplitter über ihm kreisen und steigen auf und ab. Beim Biss stößt der Kopf vor und das gespaltene Maul schnappt;
 * zum Heulen setzt er sich halb, reckt den Kopf und reißt das Maul auf.
 */
public class RiftHoundModel extends EntityModel<RiftHoundRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart tail;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart leftFront;
    private final ModelPart rightFront;
    private final ModelPart leftFrontShin;
    private final ModelPart rightFrontShin;
    private final ModelPart leftHind;
    private final ModelPart rightHind;
    private final ModelPart leftHindShin;
    private final ModelPart rightHindShin;
    private final ModelPart[] shards;

    public RiftHoundModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.tail = this.body.getChild("tail");
        this.tail2 = this.tail.getChild("tail2");
        this.tail3 = this.tail2.getChild("tail3");
        this.leftFront = root.getChild("left_front_leg");
        this.rightFront = root.getChild("right_front_leg");
        this.leftFrontShin = this.leftFront.getChild("left_front_shin");
        this.rightFrontShin = this.rightFront.getChild("right_front_shin");
        this.leftHind = root.getChild("left_hind_leg");
        this.rightHind = root.getChild("right_hind_leg");
        this.leftHindShin = this.leftHind.getChild("left_hind_shin");
        this.rightHindShin = this.rightHind.getChild("right_hind_shin");
        this.shards = new ModelPart[] {root.getChild("shard1"), root.getChild("shard2"), root.getChild("shard3")};
    }

    @Override
    public void setupAnim(RiftHoundRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(1.0f, state.walkAnimationSpeed);

        // ------------------------------------------------------------ Laufen: lange Sprünge, der Rücken federt
        float run = pos * 0.75f;
        this.leftFront.xRot += Mth.sin(run) * 0.9f * speed;
        this.rightFront.xRot += Mth.sin(run + 0.5f) * 0.9f * speed;
        this.leftHind.xRot += Mth.sin(run + Mth.PI) * 0.8f * speed;
        this.rightHind.xRot += Mth.sin(run + Mth.PI + 0.5f) * 0.8f * speed;
        this.leftFrontShin.xRot += Math.max(0.0f, Mth.sin(run + 1.2f)) * -0.6f * speed;
        this.rightFrontShin.xRot += Math.max(0.0f, Mth.sin(run + 1.7f)) * -0.6f * speed;
        this.leftHindShin.xRot += Math.max(0.0f, Mth.sin(run + Mth.PI + 1.2f)) * 0.5f * speed;
        this.rightHindShin.xRot += Math.max(0.0f, Mth.sin(run + Mth.PI + 1.7f)) * 0.5f * speed;
        this.body.xRot += Mth.sin(run) * 0.08f * speed;
        float bob = Mth.abs(Mth.cos(run)) * -0.8f * speed;
        this.body.y += bob;
        this.head.yRot += Mth.clamp(state.yRot, -50.0f, 50.0f) * Anim.DEG * 0.8f;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * Anim.DEG * 0.6f - Mth.sin(run) * 0.06f * speed;
        // der Peitschenschwanz in Wellen
        this.tail.yRot += Mth.sin(t * 0.2f) * 0.25f;
        this.tail2.yRot += Mth.sin(t * 0.2f - 0.8f) * 0.35f;
        this.tail3.yRot += Mth.sin(t * 0.2f - 1.6f) * 0.45f;
        this.tail.xRot += -0.25f * speed;
        // das Maul steht im Leerlauf einen Spalt offen und zittert
        this.jaw.xRot += 0.06f + Mth.sin(t * 0.6f) * 0.02f;

        // ------------------------------------------------------------ die schwebenden Splitter
        for (int i = 0; i < this.shards.length; i++) {
            ModelPart s = this.shards[i];
            s.y += Mth.sin(t * 0.1f + i * 2.1f) * 1.2f + bob;
            s.yRot += t * (0.04f + i * 0.015f) + i;
            s.zRot += Mth.sin(t * 0.07f + i) * 0.15f;
        }

        // ------------------------------------------------------------ Biss
        float bite = Anim.seconds(state.bite, t);
        if (bite >= 0.0f && bite < 0.45f) {
            float open = Anim.bump(bite, 0.08f, 0.12f, 0.2f);
            float lunge = Anim.bump(bite, 0.1f, 0.18f, 0.4f);
            this.jaw.xRot += 0.9f * open;
            this.head.xRot += 0.2f * lunge;
            this.head.z += -1.2f * lunge;
            this.body.xRot += 0.06f * lunge;
        }

        // ------------------------------------------------------------ Heulen
        float howl = Anim.seconds(state.howl, t);
        if (howl >= 0.0f && howl < 1.7f) {
            float up = Anim.bump(howl, 0.3f, 1.3f, 1.6f);
            this.body.xRot += -0.35f * up;
            this.body.y += 1.2f * up;
            this.head.xRot += -0.85f * up;
            this.jaw.xRot += 0.7f * up + Mth.sin(t * 1.3f) * 0.05f * up;
            this.leftHind.xRot += -0.5f * up;
            this.rightHind.xRot += -0.5f * up;
            this.leftHindShin.xRot += 0.8f * up;
            this.rightHindShin.xRot += 0.8f * up;
            this.tail.xRot += 0.4f * up;
        }
    }
}
