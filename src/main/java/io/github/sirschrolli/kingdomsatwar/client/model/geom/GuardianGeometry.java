package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Riffwächter (Knochen und Stacheln wie beim Vanilla-Wächter; der uralte wird im Spiel ×2,35 gezeichnet).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class GuardianGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private GuardianGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6F, 10F, -8F, 12F, 12F, 16F)
                .texOffs(57, 0).addBox(-8F, 10F, -6F, 2F, 12F, 12F)
                .texOffs(86, 0).addBox(6F, 10F, -6F, 2F, 12F, 12F)
                .texOffs(46, 29).addBox(-6F, 8F, -6F, 12F, 2F, 12F)
                .texOffs(0, 48).addBox(-6F, 22F, -6F, 12F, 2F, 12F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_spike0 = p_head.addOrReplaceChild("spike0", CubeListBuilder.create()
                .texOffs(53, 63).addBox(-1F, -2.5F, -1F, 2F, 5F, 2F)
                .texOffs(60, 82).addBox(-0.5F, -5.5F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(0F, 7.92F, 8.08F, 5.4978F, 0F, 0F));
        PartDefinition p_spike1 = p_head.addOrReplaceChild("spike1", CubeListBuilder.create()
                .texOffs(62, 63).addBox(-1F, -2.5F, -1F, 2F, 5F, 2F)
                .texOffs(65, 82).addBox(-0.5F, -5.5F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(0F, 7.9568F, -8.0432F, 0.7854F, 0F, 0F));
        PartDefinition p_spike2 = p_head.addOrReplaceChild("spike2", CubeListBuilder.create()
                .texOffs(71, 63).addBox(-1F, -2.5F, -1F, 2F, 5F, 2F)
                .texOffs(70, 82).addBox(-0.5F, -5.5F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(7.9667F, 8.0333F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_spike3 = p_head.addOrReplaceChild("spike3", CubeListBuilder.create()
                .texOffs(80, 63).addBox(-1F, -2.5F, -1F, 2F, 5F, 2F)
                .texOffs(75, 82).addBox(-0.5F, -5.5F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(-7.9208F, 8.0792F, 0F, 0F, 0F, 5.4978F));
        PartDefinition p_spike4 = p_head.addOrReplaceChild("spike4", CubeListBuilder.create()
                .texOffs(89, 63).addBox(-1F, -2.5F, -1F, 2F, 5F, 2F)
                .texOffs(80, 82).addBox(-0.5F, -5.5F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(-7.9477F, 16F, -7.9477F, 1.5708F, 0.7854F, 0F));
        PartDefinition p_spike5 = p_head.addOrReplaceChild("spike5", CubeListBuilder.create()
                .texOffs(98, 63).addBox(-1F, -2.5F, -1F, 2F, 5F, 2F)
                .texOffs(85, 82).addBox(-0.5F, -5.5F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(8.0227F, 16F, -8.0227F, 1.5708F, 5.4978F, 0F));
        PartDefinition p_spike6 = p_head.addOrReplaceChild("spike6", CubeListBuilder.create()
                .texOffs(107, 63).addBox(-1F, -2.5F, -1F, 2F, 5F, 2F)
                .texOffs(90, 82).addBox(-0.5F, -5.5F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(8.0768F, 16F, 8.0768F, 1.5708F, 3.927F, 0F));
        PartDefinition p_spike7 = p_head.addOrReplaceChild("spike7", CubeListBuilder.create()
                .texOffs(116, 63).addBox(-1F, -2.5F, -1F, 2F, 5F, 2F)
                .texOffs(95, 82).addBox(-0.5F, -5.5F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(-8.0603F, 16F, 8.0603F, 1.5708F, 2.3562F, 0F));
        PartDefinition p_spike8 = p_head.addOrReplaceChild("spike8", CubeListBuilder.create()
                .texOffs(0, 74).addBox(-1F, -2.5F, -1F, 2F, 5F, 2F)
                .texOffs(100, 82).addBox(-0.5F, -5.5F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(0F, 23.9884F, 7.9884F, 3.927F, 0F, 0F));
        PartDefinition p_spike9 = p_head.addOrReplaceChild("spike9", CubeListBuilder.create()
                .texOffs(9, 74).addBox(-1F, -2.5F, -1F, 2F, 5F, 2F)
                .texOffs(105, 82).addBox(-0.5F, -5.5F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(0F, 23.9271F, -7.9271F, 2.3562F, 0F, 0F));
        PartDefinition p_spike10 = p_head.addOrReplaceChild("spike10", CubeListBuilder.create()
                .texOffs(18, 74).addBox(-1F, -2.5F, -1F, 2F, 5F, 2F)
                .texOffs(110, 82).addBox(-0.5F, -5.5F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(7.9329F, 23.9329F, 0F, 0F, 0F, 2.3562F));
        PartDefinition p_spike11 = p_head.addOrReplaceChild("spike11", CubeListBuilder.create()
                .texOffs(27, 74).addBox(-1F, -2.5F, -1F, 2F, 5F, 2F)
                .texOffs(115, 82).addBox(-0.5F, -5.5F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(-8.0004F, 24.0004F, 0F, 0F, 0F, 3.927F));
        PartDefinition p_eye = p_head.addOrReplaceChild("eye", CubeListBuilder.create()
                .texOffs(46, 74).addBox(-2.5F, 14F, 0F, 5F, 4F, 1F),
                PartPose.offset(0F, 0F, -8.25F));
        PartDefinition p_tail0 = p_head.addOrReplaceChild("tail0", CubeListBuilder.create()
                .texOffs(87, 48).addBox(-2F, 14F, 7F, 4F, 4F, 8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tail1 = p_tail0.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(0, 63).addBox(0F, 14F, 0F, 3F, 3F, 7F),
                PartPose.offset(-1.5F, 0.5F, 14F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(31, 63).addBox(0F, 14F, 0F, 2F, 2F, 6F)
                .texOffs(0, 29).addBox(1F, 10.5F, 3F, 1F, 9F, 9F),
                PartPose.offset(0.5F, 0.5F, 6F));
        PartDefinition p_brow_right = p_head.addOrReplaceChild("brow_right", CubeListBuilder.create()
                .texOffs(85, 74).addBox(-5.4F, -1F, -0.7F, 5.4F, 1.8F, 1.3F),
                PartPose.offsetAndRotation(-0.3F, 13.7F, -8.2F, 0F, 0F, 0.2618F));
        PartDefinition p_brow_left = p_head.addOrReplaceChild("brow_left", CubeListBuilder.create()
                .texOffs(102, 74).addBox(0F, -1F, -0.7F, 5.4F, 1.8F, 1.3F),
                PartPose.offsetAndRotation(0.3F, 13.7F, -8.2F, 0F, 0F, -0.2618F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(64, 74).addBox(-4.5F, 19.6F, -8.7F, 9F, 2.2F, 1F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_crest = p_head.addOrReplaceChild("crest", CubeListBuilder.create()
                .texOffs(21, 29).addBox(-0.5F, 3F, -4.5F, 1F, 5F, 11F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fin_right = p_head.addOrReplaceChild("fin_right", CubeListBuilder.create()
                .texOffs(49, 48).addBox(-1F, -2.5F, 0F, 1F, 6F, 8F),
                PartPose.offsetAndRotation(-8F, 18.5F, -3F, 0F, -0.384F, 0F));
        PartDefinition p_fin_left = p_head.addOrReplaceChild("fin_left", CubeListBuilder.create()
                .texOffs(68, 48).addBox(0F, -2.5F, 0F, 1F, 6F, 8F),
                PartPose.offsetAndRotation(8F, 18.5F, -3F, 0F, 0.384F, 0F));
        PartDefinition p_growth = p_head.addOrReplaceChild("growth", CubeListBuilder.create()
                .texOffs(119, 74).addBox(2.5F, 6.8F, -2.5F, 2F, 1.2F, 2F)
                .texOffs(120, 82).addBox(-4.5F, 7F, 2F, 1.5F, 1F, 1.5F)
                .texOffs(18, 82).addBox(8F, 12.5F, 0.5F, 1F, 1.6F, 1.6F)
                .texOffs(25, 82).addBox(-9F, 16.5F, -3.5F, 1F, 1.4F, 1.4F)
                .texOffs(0, 87).addBox(-2F, 7.2F, 4.6F, 1.2F, 0.8F, 1.2F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_crust = p_head.addOrReplaceChild("crust", CubeListBuilder.create()
                .texOffs(0, 82).addBox(-3.5F, 6.8F, -4.6F, 2F, 1.2F, 2F)
                .texOffs(9, 87).addBox(0.4F, 7.1F, 1F, 1.4F, 0.9F, 1.4F)
                .texOffs(9, 82).addBox(4F, 6.9F, 3F, 1.6F, 1.1F, 1.6F)
                .texOffs(32, 82).addBox(8F, 16F, -4.5F, 1F, 1.6F, 1.6F)
                .texOffs(39, 82).addBox(8F, 18.5F, 2.5F, 0.9F, 1.2F, 1.2F)
                .texOffs(46, 82).addBox(-9F, 12F, 1.5F, 1F, 1.8F, 1.8F)
                .texOffs(53, 82).addBox(-9F, 19F, 3F, 0.9F, 1.3F, 1.3F)
                .texOffs(18, 87).addBox(-5F, 24F, -2.5F, 1.6F, 0.9F, 1.6F)
                .texOffs(27, 87).addBox(2.5F, 24F, 3F, 1.2F, 0.8F, 1.2F)
                .texOffs(36, 87).addBox(6.2F, 14F, 6.2F, 1.2F, 1.2F, 1F)
                .texOffs(43, 87).addBox(-6.6F, 20F, -7.4F, 1F, 1F, 1F)
                .texOffs(48, 63).addBox(-4.5F, 24F, -3.5F, 1F, 7F, 1F)
                .texOffs(21, 63).addBox(3.5F, 24F, 1.5F, 1F, 9F, 1F)
                .texOffs(41, 74).addBox(-1.2F, 24F, 4F, 1F, 5F, 1F)
                .texOffs(36, 74).addBox(6.4F, 22F, -4F, 1F, 6F, 1F)
                .texOffs(26, 63).addBox(-7.2F, 22F, 2.5F, 1F, 8F, 1F)
                .texOffs(59, 74).addBox(1F, 24F, -4.6F, 1F, 4F, 1F),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
