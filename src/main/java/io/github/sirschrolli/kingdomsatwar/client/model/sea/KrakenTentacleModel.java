package io.github.sirschrolli.kingdomsatwar.client.model.sea;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.sea.KrakenTentacleEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;

/**
 * Ein Arm des Kraken, der aus dem Wasser bricht (Tentakelkäfig, seit 1.00): sechs Glieder wie seine Arme (dieselbe Textur –
 * Saugnäpfe zur Mitte des Käfigs), senkrecht aus dem Wasser. Er steigt empor, wiegt sich wie eine Schlange, krümmt sich, je
 * enger der Käfig wird, immer weiter zur Mitte, holt aus und schlägt nieder; dann versinkt er. Abgeschlagen zuckt er zurück
 * und sinkt rasch.
 */
public class KrakenTentacleModel extends EntityModel<KrakenTentacleModel.State> {
    private static final int PARTS = 6;
    /** So tief (Modellpunkte) steckt der Arm, bevor er auftaucht. */
    private static final float HIDDEN = 64.0f;

    /** Was der Arm zum Zeichnen braucht. */
    public static class State extends EntityRenderState {
        public float age;
        /** Seit wann (Ticks) der Arm abgeschlagen ist, sonst -1. */
        public float cutAge = -1.0f;
        public float yaw;
        public int seed;
    }

    private final ModelPart[] parts = new ModelPart[PARTS];

    public KrakenTentacleModel(ModelPart root) {
        super(root);
        ModelPart part = root.getChild("seg0");
        this.parts[0] = part;
        for (int k = 1; k < PARTS; k++) {
            part = part.getChild("seg" + k);
            this.parts[k] = part;
        }
    }

    /** Wie ein Arm des Kraken (Maße und Textur von „arm0“ aus {@code KrakenGeometry}), aber aufrecht. */
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p0 = root.addOrReplaceChild("seg0", CubeListBuilder.create()
                .texOffs(130, 84).addBox(-3.5F, -3.5F, -11F, 7F, 7F, 11F, new CubeDeformation(0F, -0.35F, 0F)),
                PartPose.offsetAndRotation(0F, 24F, 0F, -Mth.HALF_PI, 0F, 0F));
        PartDefinition p1 = p0.addOrReplaceChild("seg1", CubeListBuilder.create()
                .texOffs(111, 124).addBox(-3F, -3F, -10F, 6F, 6F, 10F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -10.6F, 0.06F, 0F, 0F));
        PartDefinition p2 = p1.addOrReplaceChild("seg2", CubeListBuilder.create()
                .texOffs(177, 142).addBox(-2.5F, -2.5F, -9F, 5F, 5F, 9F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -9.6F, 0.08F, 0F, 0F));
        PartDefinition p3 = p2.addOrReplaceChild("seg3", CubeListBuilder.create()
                .texOffs(37, 174).addBox(-2.5F, -2F, -8F, 5F, 4F, 8F, new CubeDeformation(-0.45F, -0.155F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -8.6F, 0.12F, 0F, 0F));
        PartDefinition p4 = p3.addOrReplaceChild("seg4", CubeListBuilder.create()
                .texOffs(162, 187).addBox(-2F, -1.5F, -7.75F, 4F, 3F, 8F, new CubeDeformation(-0.4F, -0.06F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -7.6F, 0.18F, 0F, 0F));
        p4.addOrReplaceChild("seg5", CubeListBuilder.create()
                .texOffs(207, 212).addBox(-1.5F, -1.5F, -7F, 3F, 3F, 7F, new CubeDeformation(-0.3F, -0.42F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.1F, 0.26F, 0F, 0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        float age = state.age;
        float rise = Mth.clamp(age / KrakenTentacleEntity.RISE, 0.0f, 1.0f);
        rise = rise * rise * (3.0f - 2.0f * rise);
        float sink = Mth.clamp((age - KrakenTentacleEntity.SINK_FROM) / (float) (KrakenTentacleEntity.LIFE - KrakenTentacleEntity.SINK_FROM), 0.0f,
                1.0f);
        float whip = 0.0f;
        if (state.cutAge >= 0.0f) {
            sink = Math.max(sink, Mth.clamp((state.cutAge - 2.0f) / (KrakenTentacleEntity.CUT_SINK - 3.0f), 0.0f, 1.0f));
            whip = Anim.bump(state.cutAge, 2.0f, 3.0f, 8.0f);
        }
        float down = Mth.clamp(1.0f - rise + sink * sink, 0.0f, 1.0f);
        this.parts[0].y += down * HIDDEN;

        float close = KrakenTentacleEntity.closing(age);
        // ausholen und niederschlagen
        float windup = Anim.bump(age - (KrakenTentacleEntity.SLAM - 14), 7.0f, 9.0f, 11.0f);
        float strike = Anim.bump(age - (KrakenTentacleEntity.SLAM - 4), 4.0f, 7.0f, 14.0f);
        float seed = (state.seed % 17) * 0.7f;
        this.parts[0].xRot += 0.12f * close + 0.18f * strike - 0.1f * windup;
        this.parts[0].zRot += Mth.sin(age * 0.09f + seed) * 0.06f;
        for (int k = 0; k < PARTS; k++) {
            ModelPart p = this.parts[k];
            float w = age * 0.17f + seed - k * 0.6f;
            // wiegen wie eine Schlange, je enger der Käfig, desto weiter zur Mitte gekrümmt
            p.xRot += Mth.sin(w) * (0.04f + 0.025f * k) + (0.02f + 0.035f * k) * close;
            p.zRot += Mth.sin(age * 0.12f + seed * 1.3f - k * 0.5f) * (0.04f + 0.03f * k);
            p.xRot += -0.14f * windup + 0.36f * strike;
            p.xRot -= 0.32f * whip;
        }
    }
}
