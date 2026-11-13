package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Werwolfs.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WerewolfGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private WerewolfGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_hips = root.addOrReplaceChild("hips", CubeListBuilder.create()
                .texOffs(52, 37).addBox(-4.5F, -3F, -3F, 9F, 5F, 6F)
                .texOffs(17, 37).addBox(-5F, -1.5F, -3.5F, 10F, 4F, 7F, new CubeDeformation(-0.2F, -0.3F, -0.3F)),
                PartPose.offset(0F, 4F, 1.5F));
        PartDefinition p_body = p_hips.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.5F, -13F, -4F, 11F, 13F, 8F)
                .texOffs(0, 22).addBox(-6.5F, -15.5F, -3.5F, 13F, 5F, 9F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(0, 73).addBox(-4.5F, -17F, -0.5F, 9F, 3F, 5F)
                .texOffs(50, 50).addBox(-2F, -14F, 3.5F, 4F, 9F, 2F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offsetAndRotation(0F, -2.5F, 0F, 0.5585F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(45, 22).addBox(-4F, -6.4F, -5.75F, 8F, 7F, 7F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(21, 62).addBox(-4F, -2.2F, -4.8F, 8F, 4F, 5F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(83, 37).addBox(-2.5F, -5F, -12.15F, 5F, 4F, 7F, new CubeDeformation(-0.2F, -0.4F, -0.15F))
                .texOffs(71, 73).addBox(-2F, -5.35F, -11F, 4F, 1F, 6F, new CubeDeformation(-0.3F, -0.05F, -0.4F))
                .texOffs(19, 90).addBox(-1F, -5.25F, -12.4F, 2F, 2F, 1F, new CubeDeformation(0F, -0.35F, 0F))
                .texOffs(26, 90).addBox(-3.65F, -6.15F, -6.1F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.3F))
                .texOffs(33, 90).addBox(1.65F, -6.15F, -6.1F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.45F, -0.3F))
                .texOffs(0, 90).addBox(-3.5F, -6.6F, -6.4F, 7F, 1F, 2F, new CubeDeformation(-0.1F, 0F, -0.4F))
                .texOffs(54, 90).addBox(-2.25F, -1.9F, -11.85F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.15F))
                .texOffs(59, 90).addBox(1.25F, -1.9F, -11.85F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(0F, -14F, -3.5F, -0.5236F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(48, 62).addBox(-2F, -0.1F, -6.85F, 4F, 2F, 7F, new CubeDeformation(0F, -0.1F, -0.15F))
                .texOffs(74, 90).addBox(-1.9F, -0.9F, -6.7F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(79, 90).addBox(0.9F, -0.9F, -6.7F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.4F, -5F, 0.1396F, 0F, 0F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(53, 82).addBox(-1.5F, -2.7F, -1F, 3F, 3F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.3F))
                .texOffs(40, 90).addBox(-1F, -4.4F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offsetAndRotation(2.6F, -6.2F, -1.2F, -0.2094F, 0F, 0.2443F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(64, 82).addBox(-1.5F, -2.7F, -1F, 3F, 3F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.3F))
                .texOffs(47, 90).addBox(-1F, -4.4F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offsetAndRotation(-2.6F, -6.2F, -1.2F, -0.2094F, 0F, -0.2443F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(39, 0).addBox(-2.5F, -1.75F, -2.5F, 5F, 12F, 5F, new CubeDeformation(-0.3F, -0.25F, -0.1F))
                .texOffs(63, 50).addBox(-2.5F, -2F, -3F, 5F, 4F, 6F, new CubeDeformation(0F, 0F, -0.4F)),
                PartPose.offsetAndRotation(6.4F, -11.5F, -0.5F, -0.4538F, 0F, -0.1396F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(76, 22).addBox(-2F, -0.25F, -2F, 4F, 10F, 4F, new CubeDeformation(-0.1F, -0.25F, 0F))
                .texOffs(29, 73).addBox(-2.5F, 9F, -2.6F, 5F, 3F, 5F, new CubeDeformation(-0.3F, 0F, -0.2F))
                .texOffs(86, 82).addBox(-2F, 11.5F, -2.35F, 1F, 3F, 1F, new CubeDeformation(-0.15F, -0.3F, -0.15F))
                .texOffs(91, 82).addBox(-0.5F, 11.5F, -2.35F, 1F, 3F, 1F, new CubeDeformation(-0.15F, -0.3F, -0.15F))
                .texOffs(96, 82).addBox(1F, 11.5F, -2.35F, 1F, 3F, 1F, new CubeDeformation(-0.15F, -0.3F, -0.15F))
                .texOffs(64, 90).addBox(1.4F, 10F, -1.75F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 9.8F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(60, 0).addBox(-2.5F, -1.75F, -2.5F, 5F, 12F, 5F, new CubeDeformation(-0.3F, -0.25F, -0.1F))
                .texOffs(86, 50).addBox(-2.5F, -2F, -3F, 5F, 4F, 6F, new CubeDeformation(0F, 0F, -0.4F)),
                PartPose.offsetAndRotation(-6.4F, -11.5F, -0.5F, -0.4538F, 0F, 0.1396F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(93, 22).addBox(-2F, -0.25F, -2F, 4F, 10F, 4F, new CubeDeformation(-0.1F, -0.25F, 0F))
                .texOffs(50, 73).addBox(-2.5F, 9F, -2.6F, 5F, 3F, 5F, new CubeDeformation(-0.3F, 0F, -0.2F))
                .texOffs(101, 82).addBox(-2F, 11.5F, -2.35F, 1F, 3F, 1F, new CubeDeformation(-0.15F, -0.3F, -0.15F))
                .texOffs(106, 82).addBox(-0.5F, 11.5F, -2.35F, 1F, 3F, 1F, new CubeDeformation(-0.15F, -0.3F, -0.15F))
                .texOffs(111, 82).addBox(1F, 11.5F, -2.35F, 1F, 3F, 1F, new CubeDeformation(-0.15F, -0.3F, -0.15F))
                .texOffs(69, 90).addBox(-2.4F, 10F, -1.75F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 9.8F, 0F, -0.5236F, 0F, 0F));
        PartDefinition p_left_leg = p_hips.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(81, 0).addBox(-2.5F, -0.75F, -2.9F, 5F, 9F, 6F, new CubeDeformation(-0.1F, -0.25F, -0.4F))
                .texOffs(0, 50).addBox(-3F, 0.25F, -2.9F, 6F, 5F, 6F, new CubeDeformation(-0.4F, -0.25F, -0.2F)),
                PartPose.offsetAndRotation(3.4F, -0.5F, 0.5F, -0.4538F, 0F, 0F));
        PartDefinition p_left_shin = p_left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(110, 22).addBox(-2F, -0.1F, -2F, 4F, 8F, 4F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(19, 82).addBox(-2F, -0.2F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.1F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 7.6F, 0.3F, 1.0123F, 0F, 0F));
        PartDefinition p_left_foot = p_left_shin.addOrReplaceChild("left_foot", CubeListBuilder.create()
                .texOffs(92, 62).addBox(-1.5F, -0.2F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(92, 73).addBox(-2F, 4.4F, -4.5F, 4F, 2F, 5F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(84, 90).addBox(-1.7F, 5F, -5.4F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(89, 90).addBox(-0.5F, 5F, -5.4F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(94, 90).addBox(0.7F, 5F, -5.4F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offsetAndRotation(0F, 7.4F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_right_leg = p_hips.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(104, 0).addBox(-2.5F, -0.75F, -2.9F, 5F, 9F, 6F, new CubeDeformation(-0.1F, -0.25F, -0.4F))
                .texOffs(25, 50).addBox(-3F, 0.25F, -2.9F, 6F, 5F, 6F, new CubeDeformation(-0.4F, -0.25F, -0.2F)),
                PartPose.offsetAndRotation(-3.4F, -0.5F, 0.5F, -0.4538F, 0F, 0F));
        PartDefinition p_right_shin = p_right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(0, 37).addBox(-2F, -0.1F, -2F, 4F, 8F, 4F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(36, 82).addBox(-2F, -0.2F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.1F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 7.6F, 0.3F, 1.0123F, 0F, 0F));
        PartDefinition p_right_foot = p_right_shin.addOrReplaceChild("right_foot", CubeListBuilder.create()
                .texOffs(105, 62).addBox(-1.5F, -0.2F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(0, 82).addBox(-2F, 4.4F, -4.5F, 4F, 2F, 5F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(99, 90).addBox(-1.7F, 5F, -5.4F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(104, 90).addBox(-0.5F, 5F, -5.4F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(109, 90).addBox(0.7F, 5F, -5.4F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offsetAndRotation(0F, 7.4F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_tail = p_hips.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(71, 62).addBox(-2F, -1.4F, 0F, 4F, 3F, 6F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(0F, -1.5F, 3F, 0.8378F, 0F, 0F));
        PartDefinition p_tail_tip = p_tail.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                .texOffs(0, 62).addBox(-2F, -1.9F, -0.25F, 4F, 4F, 6F, new CubeDeformation(-0.1F, -0.3F, -0.25F))
                .texOffs(75, 82).addBox(-1.5F, -1.4F, 5.5F, 3F, 3F, 2F, new CubeDeformation(-0.3F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 6F, 0.2443F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
