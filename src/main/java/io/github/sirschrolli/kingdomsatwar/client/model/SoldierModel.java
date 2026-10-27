package io.github.sirschrolli.kingdomsatwar.client.model;

import io.github.sirschrolli.kingdomsatwar.client.renderer.KingdomHumanoidRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.util.Mth;

/**
 * Menschen-Modell der Soldaten und Banditen: wie ein Spieler (mit zweiter Hautschicht für Helme, Rüstung
 * und Kleidung), dazu ein wehender Umhang, eine Helmfeder und ein Köcher. Die Textur ist 128x64 groß:
 * links das gewohnte Spieler-Schema, rechts die Zusatzteile.
 *
 * <p>Animationen: Gehen, Zuschlagen, Bogenspannen und Schildheben wie bei Spielern; dazu Salutieren
 * (bei Befehlen) und Jubeln mit erhobener Waffe (nach einem Sieg).
 */
public class SoldierModel extends HumanoidModel<KingdomHumanoidRenderState> {
    private final ModelPart cape;
    private final ModelPart plume;
    private final ModelPart quiver;

    public SoldierModel(ModelPart root) {
        super(root);
        this.cape = this.body.getChild("cape");
        this.plume = this.head.getChild("plume");
        this.quiver = this.body.getChild("quiver");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, false);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");
        PartDefinition head = root.getChild("head");
        // Umhang: hängt an den Schultern hinter dem Rücken
        body.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(64, 0).addBox(-5.0f, 0.0f, 0.0f, 10.0f, 16.0f, 1.0f),
                PartPose.offset(0.0f, 0.0f, 2.3f));
        // Helmfeder: Halter und schräg nach hinten stehende Feder
        head.addOrReplaceChild("plume", CubeListBuilder.create()
                        .texOffs(64, 20).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 2.0f, 2.0f)
                        .texOffs(72, 20).addBox(-0.5f, -8.0f, -1.5f, 1.0f, 6.0f, 4.0f),
                PartPose.offsetAndRotation(0.0f, -8.4f, 1.5f, -0.35f, 0.0f, 0.0f));
        // Köcher: schräg auf dem Rücken, Öffnung über der rechten Schulter
        body.addOrReplaceChild("quiver", CubeListBuilder.create()
                        .texOffs(64, 32).addBox(-2.0f, -1.0f, 0.0f, 4.0f, 11.0f, 3.0f)
                        .texOffs(64, 48).addBox(-1.5f, -4.0f, 0.5f, 3.0f, 3.0f, 2.0f),
                PartPose.offsetAndRotation(0.5f, 1.0f, 2.3f, 0.0f, 0.0f, -0.5f));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(KingdomHumanoidRenderState state) {
        super.setupAnim(state);
        if (state.salute > 0.0f) {
            float e = envelope(state.salute);
            // rechte Hand an die Stirn
            this.rightArm.xRot = Mth.lerp(e, this.rightArm.xRot, -2.25f);
            this.rightArm.yRot = Mth.lerp(e, this.rightArm.yRot, -0.55f);
            this.rightArm.zRot = Mth.lerp(e, this.rightArm.zRot, 0.15f);
            this.head.xRot = Mth.lerp(e, this.head.xRot, -0.05f);
        }
        if (state.cheer > 0.0f) {
            float e = envelope(state.cheer);
            float pump = Mth.sin(state.cheer * Mth.PI * 6.0f) * 0.18f;
            // Waffe hoch in die Luft, der andere Arm ballt die Faust
            this.rightArm.xRot = Mth.lerp(e, this.rightArm.xRot, -3.05f + pump);
            this.rightArm.yRot = Mth.lerp(e, this.rightArm.yRot, 0.0f);
            this.rightArm.zRot = Mth.lerp(e, this.rightArm.zRot, -0.28f);
            this.leftArm.xRot = Mth.lerp(e, this.leftArm.xRot, -0.7f - pump * 0.5f);
            this.leftArm.zRot = Mth.lerp(e, this.leftArm.zRot, 0.15f);
            this.head.xRot = Mth.lerp(e, this.head.xRot, -0.35f);
        }
        this.cape.visible = state.cape;
        this.plume.visible = state.plume;
        this.quiver.visible = state.quiver;
        if (state.cape) {
            this.cape.xRot = (6.0f + state.capeLean + state.capeFlap) * Mth.DEG_TO_RAD;
            if (state.isCrouching) {
                this.cape.xRot += 0.3f;
            }
        }
    }

    /** Sanft hinein, halten, sanft heraus. */
    private static float envelope(float t) {
        float in = Mth.clamp(t / 0.2f, 0.0f, 1.0f);
        float out = Mth.clamp((1.0f - t) / 0.25f, 0.0f, 1.0f);
        float e = Math.min(in, out);
        return e * e * (3.0f - 2.0f * e);
    }
}
