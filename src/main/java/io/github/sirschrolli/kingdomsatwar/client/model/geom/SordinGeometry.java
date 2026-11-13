package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Sordin, dem Kantor der Stille (1.7; im Spiel 1,3-fach vergrößert).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SordinGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private SordinGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(9, 0).addBox(-4.5F, -16F, -2.5F, 9F, 16F, 5F, new CubeDeformation(-0.3F, 0F, -0.1F))
                .texOffs(39, 72).addBox(-2.5F, -12.6F, -3.15F, 5F, 5F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.2F))
                .texOffs(91, 26).addBox(1.9F, -16F, -3.05F, 2F, 15F, 1F, new CubeDeformation(-0.3F, -0.4F, -0.3F))
                .texOffs(98, 26).addBox(-3.9F, -16F, -3.05F, 2F, 15F, 1F, new CubeDeformation(-0.3F, -0.4F, -0.3F))
                .texOffs(44, 60).addBox(-5F, -2.05F, -3F, 10F, 2F, 6F, new CubeDeformation(-0.4F, -0.25F, -0.2F))
                .texOffs(52, 72).addBox(1.6F, -0.5F, -3.1F, 1F, 5F, 1F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(0, 60).addBox(-5.5F, -17F, -3F, 11F, 5F, 6F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_skirt = p_body.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(91, 0).addBox(-5F, 0F, -3F, 10F, 11F, 6F, new CubeDeformation(-0.4F, 0F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_skirt_low = p_skirt.addOrReplaceChild("skirt_low", CubeListBuilder.create()
                .texOffs(56, 0).addBox(-5F, -0.2F, -3.5F, 10F, 11F, 7F, new CubeDeformation(0F, -0.2F, -0.4F)),
                PartPose.offset(0F, 11F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(31, 44).addBox(-3F, -7F, -2.8F, 6F, 7F, 6F)
                .texOffs(0, 26).addBox(-4F, -8.3F, -3.6F, 8F, 9F, 8F, new CubeDeformation(-0.2F, -0.1F, -0.1F)),
                PartPose.offset(0F, -16.4F, -0.2F));
        PartDefinition p_hood_tip = p_head.addOrReplaceChild("hood_tip", CubeListBuilder.create()
                .texOffs(77, 60).addBox(-1.5F, -1.4F, -0.3F, 3F, 3F, 5F, new CubeDeformation(0F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, -8F, 3.2F, -0.6632F, 0F, 0F));
        PartDefinition p_feeler_l_0 = p_head.addOrReplaceChild("feeler_l_0", CubeListBuilder.create()
                .texOffs(108, 72).addBox(-0.5F, -3.8F, -0.5F, 1F, 4F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(2F, -7.8F, 0.4F, -0.3142F, 0F, 0.4538F));
        PartDefinition p_feeler_l_1 = p_feeler_l_0.addOrReplaceChild("feeler_l_1", CubeListBuilder.create()
                .texOffs(113, 80).addBox(-0.5F, -2.985F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.015F, -0.1F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.2443F, 0F, 0.2094F));
        PartDefinition p_feeler_l_2 = p_feeler_l_1.addOrReplaceChild("feeler_l_2", CubeListBuilder.create()
                .texOffs(118, 80).addBox(-0.5F, -2.715F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.19F, -0.285F, -0.19F)),
                PartPose.offsetAndRotation(0F, -2.72F, 0F, -0.3142F, 0F, 0.2793F));
        PartDefinition p_feeler_r_0 = p_head.addOrReplaceChild("feeler_r_0", CubeListBuilder.create()
                .texOffs(113, 72).addBox(-0.5F, -3.8F, -0.5F, 1F, 4F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-2F, -7.8F, 0.4F, -0.3142F, 0F, -0.4538F));
        PartDefinition p_feeler_r_1 = p_feeler_r_0.addOrReplaceChild("feeler_r_1", CubeListBuilder.create()
                .texOffs(123, 80).addBox(-0.5F, -2.985F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.015F, -0.1F)),
                PartPose.offsetAndRotation(0F, -3.35F, 0F, -0.2443F, 0F, -0.2094F));
        PartDefinition p_feeler_r_2 = p_feeler_r_1.addOrReplaceChild("feeler_r_2", CubeListBuilder.create()
                .texOffs(0, 85).addBox(-0.5F, -2.715F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.19F, -0.285F, -0.19F)),
                PartPose.offsetAndRotation(0F, -2.72F, 0F, -0.3142F, 0F, -0.2793F));
        PartDefinition p_pipes = p_body.addOrReplaceChild("pipes", CubeListBuilder.create()
                .texOffs(20, 72).addBox(-3.5F, -2F, -0.7F, 7F, 4F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(0F, -12.5F, 2.6F));
        PartDefinition p_pipe_0 = p_pipes.addOrReplaceChild("pipe_0", CubeListBuilder.create()
                .texOffs(56, 44).addBox(-1F, -11F, -1F, 2F, 11F, 2F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(0, 80).addBox(-1.5F, -11.7F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(-2.85F, -1F, 0.4F, -0.2443F, 0F, -0.8029F));
        PartDefinition p_pipe_1 = p_pipes.addOrReplaceChild("pipe_1", CubeListBuilder.create()
                .texOffs(33, 26).addBox(-1F, -15F, -1F, 2F, 15F, 2F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(13, 80).addBox(-1.5F, -15.7F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(-1.9F, -1F, 0.4F, -0.2094F, 0F, -0.5411F));
        PartDefinition p_pipe_2 = p_pipes.addOrReplaceChild("pipe_2", CubeListBuilder.create()
                .texOffs(38, 0).addBox(-1F, -19F, -1F, 2F, 19F, 2F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(26, 80).addBox(-1.5F, -19.7F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(-0.95F, -1F, 0.4F, -0.1745F, 0F, -0.2618F));
        PartDefinition p_pipe_3 = p_pipes.addOrReplaceChild("pipe_3", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1F, -23F, -1F, 2F, 23F, 2F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(39, 80).addBox(-1.5F, -23.7F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, -1F, 0.4F, -0.1396F, 0F, 0F));
        PartDefinition p_pipe_4 = p_pipes.addOrReplaceChild("pipe_4", CubeListBuilder.create()
                .texOffs(47, 0).addBox(-1F, -19F, -1F, 2F, 19F, 2F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(52, 80).addBox(-1.5F, -19.7F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0.95F, -1F, 0.4F, -0.1745F, 0F, 0.2618F));
        PartDefinition p_pipe_5 = p_pipes.addOrReplaceChild("pipe_5", CubeListBuilder.create()
                .texOffs(42, 26).addBox(-1F, -15F, -1F, 2F, 15F, 2F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(65, 80).addBox(-1.5F, -15.7F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(1.9F, -1F, 0.4F, -0.2094F, 0F, 0.5411F));
        PartDefinition p_pipe_6 = p_pipes.addOrReplaceChild("pipe_6", CubeListBuilder.create()
                .texOffs(65, 44).addBox(-1F, -11F, -1F, 2F, 11F, 2F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(78, 80).addBox(-1.5F, -11.7F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(2.85F, -1F, 0.4F, -0.2443F, 0F, 0.8029F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(65, 26).addBox(-1.5F, -1.6F, -1.5F, 3F, 13F, 3F),
                PartPose.offset(5F, -14.6F, 0F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(13, 44).addBox(-1F, -0.25F, -1F, 2F, 12F, 2F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(92, 44).addBox(-2F, -0.6F, -2F, 4F, 8F, 4F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offset(0F, 11.2F, 0F));
        PartDefinition p_left_hand = p_left_forearm.addOrReplaceChild("left_hand", CubeListBuilder.create()
                .texOffs(77, 72).addBox(-1.5F, -0.4F, -1F, 3F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, 0F))
                .texOffs(57, 72).addBox(-1.175F, 2F, -1.225F, 1F, 5F, 1F, new CubeDeformation(-0.225F, 0F, -0.225F))
                .texOffs(0, 72).addBox(-0.475F, 1.7F, -1.225F, 1F, 6F, 1F, new CubeDeformation(-0.225F, -0.3F, -0.225F))
                .texOffs(5, 72).addBox(0.225F, 1.7F, -1.225F, 1F, 6F, 1F, new CubeDeformation(-0.225F, -0.3F, -0.225F))
                .texOffs(62, 72).addBox(0.225F, 1.6F, -0.025F, 1F, 5F, 1F, new CubeDeformation(-0.225F, -0.4F, -0.225F)),
                PartPose.offset(0F, 11.5F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(78, 26).addBox(-1.5F, -1.6F, -1.5F, 3F, 13F, 3F),
                PartPose.offset(-5F, -14.6F, 0F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(22, 44).addBox(-1F, -0.25F, -1F, 2F, 12F, 2F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(109, 44).addBox(-2F, -0.6F, -2F, 4F, 8F, 4F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offset(0F, 11.2F, 0F));
        PartDefinition p_right_hand = p_right_forearm.addOrReplaceChild("right_hand", CubeListBuilder.create()
                .texOffs(88, 72).addBox(-1.5F, -0.4F, -1F, 3F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, 0F))
                .texOffs(67, 72).addBox(-1.175F, 2F, -1.225F, 1F, 5F, 1F, new CubeDeformation(-0.225F, 0F, -0.225F))
                .texOffs(10, 72).addBox(-0.475F, 1.7F, -1.225F, 1F, 6F, 1F, new CubeDeformation(-0.225F, -0.3F, -0.225F))
                .texOffs(15, 72).addBox(0.225F, 1.7F, -1.225F, 1F, 6F, 1F, new CubeDeformation(-0.225F, -0.3F, -0.225F))
                .texOffs(72, 72).addBox(0.225F, 1.6F, -0.025F, 1F, 5F, 1F, new CubeDeformation(-0.225F, -0.4F, -0.225F)),
                PartPose.offset(0F, 11.5F, 0F));
        PartDefinition p_fork = p_right_hand.addOrReplaceChild("fork", CubeListBuilder.create()
                .texOffs(35, 60).addBox(-1F, -4.5F, -1F, 2F, 9F, 2F, new CubeDeformation(-0.35F, 0F, -0.35F))
                .texOffs(104, 80).addBox(-1F, 4.2F, -1F, 2F, 2F, 2F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(99, 72).addBox(-1F, -7.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(91, 80).addBox(-2F, -8.4F, -1F, 4F, 2F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.3F))
                .texOffs(51, 26).addBox(0.8F, -23F, -1F, 1F, 15F, 2F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(58, 26).addBox(-1.8F, -23F, -1F, 1F, 15F, 2F, new CubeDeformation(0F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 3.4F, -0.2F, 1.5708F, 0F, 0F));
        PartDefinition p_left_leg = p_body.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(105, 26).addBox(-1.5F, 0F, -1.5F, 3F, 12F, 3F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(2F, 0.5F, 0F));
        PartDefinition p_left_shin = p_left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(74, 44).addBox(-1F, -0.25F, -1F, 2F, 11F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(94, 60).addBox(-1.5F, 9.75F, -3.8F, 3F, 2F, 5F, new CubeDeformation(-0.25F, -0.25F, -0.4F)),
                PartPose.offset(0F, 12F, 0F));
        PartDefinition p_right_leg = p_body.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 44).addBox(-1.5F, 0F, -1.5F, 3F, 12F, 3F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(-2F, 0.5F, 0F));
        PartDefinition p_right_shin = p_right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(83, 44).addBox(-1F, -0.25F, -1F, 2F, 11F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(111, 60).addBox(-1.5F, 9.75F, -3.8F, 3F, 2F, 5F, new CubeDeformation(-0.25F, -0.25F, -0.4F)),
                PartPose.offset(0F, 12F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
