package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Riesenkrabbe (Scheren mit beweglichem Finger, sechs Laufbeine in zwei Gliedern, Stielaugen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class GiantCrabGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 96;

    private GiantCrabGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7F, -2.5F, -5.25F, 14F, 4F, 7F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(77, 12).addBox(-6F, -2.45F, 1.5F, 12F, 4F, 3F, new CubeDeformation(-0.2F, -0.15F, 0F))
                .texOffs(15, 23).addBox(-4.5F, -2.4F, 4.1F, 9F, 4F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.4F))
                .texOffs(43, 0).addBox(-6.5F, -3.3F, -4.7F, 13F, 1F, 9F, new CubeDeformation(-0.3F, -0.2F, -0.3F))
                .texOffs(23, 12).addBox(-5F, -3.9F, -3.7F, 10F, 1F, 7F, new CubeDeformation(-0.2F, -0.2F, -0.3F))
                .texOffs(62, 23).addBox(-3F, -4.4F, -2.4F, 6F, 1F, 4F, new CubeDeformation(-0.2F, -0.3F, 0F))
                .texOffs(88, 0).addBox(-6F, 1.1F, -4F, 12F, 2F, 8F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(5, 36).addBox(-3F, -2.5F, -5.8F, 6F, 2F, 1F, new CubeDeformation(-0.4F, -0.3F, -0.2F))
                .texOffs(98, 36).addBox(-6F, -2.2F, -5.7F, 1F, 1F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(103, 36).addBox(-3.5F, -2.2F, -5.7F, 1F, 1F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(108, 36).addBox(2.5F, -2.2F, -5.7F, 1F, 1F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(113, 36).addBox(5F, -2.2F, -5.7F, 1F, 1F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(118, 36).addBox(6.1F, -2F, -2.1F, 1F, 1F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(123, 36).addBox(-7.1F, -2F, -2.1F, 1F, 1F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(0F, 18.5F, 0F));
        PartDefinition p_left_eye = p_body.addOrReplaceChild("left_eye", CubeListBuilder.create()
                .texOffs(123, 31).addBox(-0.5F, -2.6F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(105, 31).addBox(-1F, -3.65F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.45F, -0.45F)),
                PartPose.offset(1.6F, -2.6F, -4.6F));
        PartDefinition p_right_eye = p_body.addOrReplaceChild("right_eye", CubeListBuilder.create()
                .texOffs(0, 36).addBox(-0.5F, -2.6F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(114, 31).addBox(-1F, -3.65F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.45F, -0.45F)),
                PartPose.offset(-1.6F, -2.6F, -4.6F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(105, 23).addBox(-0.25F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offsetAndRotation(5.5F, 0.5F, -4F, 0F, -0.6109F, 0F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(108, 12).addBox(-1.5F, -1.5F, -4F, 3F, 3F, 4F, new CubeDeformation(-0.3F, -0.3F, 0F)),
                PartPose.offsetAndRotation(4.5F, 0F, 0F, 0F, 1.2217F, 0F));
        PartDefinition p_left_claw = p_left_forearm.addOrReplaceChild("left_claw", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-2.5F, -2F, -5.625F, 5F, 4F, 6F, new CubeDeformation(-0.375F, -0.125F, -0.375F))
                .texOffs(51, 23).addBox(-1.8687F, -2.1437F, -8.25F, 2F, 3F, 3F, new CubeDeformation(-0.0437F, -0.4688F, 0F)),
                PartPose.offset(0F, 0F, -4F));
        PartDefinition p_left_claw_finger = p_left_claw.addOrReplaceChild("left_claw_finger", CubeListBuilder.create()
                .texOffs(38, 23).addBox(-1.9563F, -1.375F, -3.625F, 2F, 2F, 4F, new CubeDeformation(-0.0437F, -0.0625F, -0.375F)),
                PartPose.offsetAndRotation(1.725F, 1.475F, -5.25F, 0F, -0.2094F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 31).addBox(-4.75F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offsetAndRotation(-5.5F, 0.5F, -4F, 0F, 0.6109F, 0F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(0, 23).addBox(-1.5F, -1.5F, -4F, 3F, 3F, 4F, new CubeDeformation(-0.3F, -0.3F, 0F)),
                PartPose.offsetAndRotation(-4.5F, 0F, 0F, 0F, -1.2217F, 0F));
        PartDefinition p_right_claw = p_right_forearm.addOrReplaceChild("right_claw", CubeListBuilder.create()
                .texOffs(58, 12).addBox(-2F, -1.5F, -4.6F, 4F, 3F, 5F, new CubeDeformation(-0.3F, 0F, -0.4F))
                .texOffs(83, 23).addBox(-1.635F, -1.475F, -6.9F, 2F, 2F, 3F, new CubeDeformation(-0.235F, -0.175F, -0.3F)),
                PartPose.offset(0F, 0F, -4F));
        PartDefinition p_right_claw_finger = p_right_claw.addOrReplaceChild("right_claw_finger", CubeListBuilder.create()
                .texOffs(94, 23).addBox(-1.765F, -1.3F, -2.8F, 2F, 2F, 3F, new CubeDeformation(-0.235F, -0.25F, -0.2F)),
                PartPose.offsetAndRotation(1.3F, 1.1F, -4.2F, 0F, 0.2094F, 0F));
        PartDefinition p_left_leg_0 = p_body.addOrReplaceChild("left_leg_0", CubeListBuilder.create()
                .texOffs(15, 31).addBox(0F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(0F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(6.5F, 0.5F, -1.8F, 0F, -0.2443F, -0.5236F));
        PartDefinition p_left_leg_0_low = p_left_leg_0.addOrReplaceChild("left_leg_0_low", CubeListBuilder.create()
                .texOffs(20, 36).addBox(0F, -0.5F, -0.5F, 5F, 1F, 1F),
                PartPose.offsetAndRotation(5F, 0F, 0F, 0F, 0F, 1.309F));
        PartDefinition p_left_leg_1 = p_body.addOrReplaceChild("left_leg_1", CubeListBuilder.create()
                .texOffs(30, 31).addBox(0F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(0F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(6.5F, 0.5F, 0.8F, 0F, 0F, -0.5236F));
        PartDefinition p_left_leg_1_low = p_left_leg_1.addOrReplaceChild("left_leg_1_low", CubeListBuilder.create()
                .texOffs(33, 36).addBox(0F, -0.5F, -0.5F, 5F, 1F, 1F),
                PartPose.offsetAndRotation(5F, 0F, 0F, 0F, 0F, 1.309F));
        PartDefinition p_left_leg_2 = p_body.addOrReplaceChild("left_leg_2", CubeListBuilder.create()
                .texOffs(45, 31).addBox(0F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(0F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(6.5F, 0.5F, 3.4F, 0F, 0.2443F, -0.5236F));
        PartDefinition p_left_leg_2_low = p_left_leg_2.addOrReplaceChild("left_leg_2_low", CubeListBuilder.create()
                .texOffs(46, 36).addBox(0F, -0.5F, -0.5F, 5F, 1F, 1F),
                PartPose.offsetAndRotation(5F, 0F, 0F, 0F, 0F, 1.309F));
        PartDefinition p_right_leg_0 = p_body.addOrReplaceChild("right_leg_0", CubeListBuilder.create()
                .texOffs(60, 31).addBox(-5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(0F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(-6.5F, 0.5F, -1.8F, 0F, 0.2443F, 0.5236F));
        PartDefinition p_right_leg_0_low = p_right_leg_0.addOrReplaceChild("right_leg_0_low", CubeListBuilder.create()
                .texOffs(59, 36).addBox(-5F, -0.5F, -0.5F, 5F, 1F, 1F),
                PartPose.offsetAndRotation(-5F, 0F, 0F, 0F, 0F, -1.309F));
        PartDefinition p_right_leg_1 = p_body.addOrReplaceChild("right_leg_1", CubeListBuilder.create()
                .texOffs(75, 31).addBox(-5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(0F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(-6.5F, 0.5F, 0.8F, 0F, 0F, 0.5236F));
        PartDefinition p_right_leg_1_low = p_right_leg_1.addOrReplaceChild("right_leg_1_low", CubeListBuilder.create()
                .texOffs(72, 36).addBox(-5F, -0.5F, -0.5F, 5F, 1F, 1F),
                PartPose.offsetAndRotation(-5F, 0F, 0F, 0F, 0F, -1.309F));
        PartDefinition p_right_leg_2 = p_body.addOrReplaceChild("right_leg_2", CubeListBuilder.create()
                .texOffs(90, 31).addBox(-5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(0F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(-6.5F, 0.5F, 3.4F, 0F, -0.2443F, 0.5236F));
        PartDefinition p_right_leg_2_low = p_right_leg_2.addOrReplaceChild("right_leg_2_low", CubeListBuilder.create()
                .texOffs(85, 36).addBox(-5F, -0.5F, -0.5F, 5F, 1F, 1F),
                PartPose.offsetAndRotation(-5F, 0F, 0F, 0F, 0F, -1.309F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
